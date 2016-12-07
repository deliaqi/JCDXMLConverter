package translator.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GeometricOperations {
    
    private static double INFINITE = 50000000;
    private static double COMPARE_PRECISION = 0.00001;
    
    public GeometricOperations() {
    }
    
    public static double angle(Line segment){
        return angle(segment.getBegin().getX(), segment.getBegin().getY(),
                segment.getEnd().getX(), segment.getEnd().getY());
    }
    
    public static double angle(Point point1, Point point2){
        return angle(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }
    
    public static double angle(double xPoint1, double yPoint1, double xPoint2, double yPoint2){
        double m = slope(xPoint1, yPoint1, xPoint2, yPoint2);
        double angle = Math.atan(m);
        
        if(xPoint1 > xPoint2){
            angle += Math.PI;
        }
        
        return angle;
    }
    
    public static double addAngle(double originalAngle, double angle) {
        double result = originalAngle + angle;
        if (result > Math.PI * 1.5) {
            result -= Math.PI * 2;
        } else if (result < -Math.PI / 2) {
            result += Math.PI * 2;
        }
        return result;
    }
    
    public static double substractAngle(double originalAngle, double angle) {
        return addAngle(originalAngle, -angle);
    }
    
    public static double slope(Line segment){
        return slope(segment.getBegin().getX(), segment.getBegin().getY(),
                segment.getEnd().getX(), segment.getEnd().getY());
    }
    
    public static double slope(double xPoint1, double yPoint1, double xPoint2, double yPoint2){
        if(xPoint2 - xPoint1 == 0){
            if (yPoint2 < yPoint1) {
                return -INFINITE;
            }
            return INFINITE;
        }
                
        return (yPoint2 - yPoint1) / (xPoint2 - xPoint1);
    }
    
    public static double slopeIntercept(Line segment){
        return slopeIntercept(segment.getBegin().getX(), segment.getBegin().getY(), segment.getSlope());
    }
    
    public static double slopeIntercept(double x, double y, double slope){
        return y - slope * x;
    }
    
    public static Point offset(Point beginPoint, double angle, double offset){
        return offset(beginPoint.getX(), beginPoint.getY(), angle, offset);
    }    
    
    public static Point offset(double xBeginPoint, double yBeginPoint, double angle, double offset){
        double newX = offset * Math.cos(angle);
        double newY = offset * Math.sin(angle);
        
        return new Point(xBeginPoint + newX, yBeginPoint + newY);
    }
    
    public static double distance(Point point){
        return distance(point.getX(), point.getY(), point.getZ());
    }
    
    public static double distance(double xPoint, double yPoint){
        return distance(xPoint, yPoint, 0);
    }
    
    public static double distance(double xPoint, double yPoint, double zPoint){
        return distance(xPoint, yPoint, zPoint, 0, 0, 0);
    }
    
    public static double distance(Point point1, Point point2){
        return distance(point1.getX(), point1.getY(),
                point2.getX(), point2.getY());
    }
    
    public static double distance(double xPoint1, double yPoint1, double xPoint2, double yPoint2) {
        return distance(xPoint1, yPoint1, 0, xPoint2, yPoint2, 0);
    }
    
    public static double distance(double xPoint1, double yPoint1, double zPoint1, double xPoint2, double yPoint2, double zPoint2) {
        return Math.sqrt(
                    Math.pow(
                        xPoint1 - xPoint2, 2) +
                    Math.pow(
                        yPoint1 - yPoint2, 2) +
                    Math.pow(
                        zPoint1 - zPoint2, 2));
    }
    
    public static double signedDistance(Point point1, Point point2, double lineAngle){
        return signedDistance(point1.getX(), point1.getY(),
                point2.getX(), point2.getY(), lineAngle);
    }
    
    public static double signedDistance(double xPoint1, double yPoint1, 
            double xPoint2, double yPoint2, double lineAngle){
        
        double distance = distance(xPoint1, yPoint1, xPoint2, yPoint2);
        double newAngle = angle(xPoint1, yPoint1, xPoint2, yPoint2);
        
        if(Math.abs(lineAngle - newAngle) > COMPARE_PRECISION){
            distance *= -1;
        }
        
        return distance;
    }
    
    public static Point realIntersection(
            Point beginSegment1, Point endSegment1, 
            Point beginSegment2, Point endSegment2){
        
        double firstLineAngle = 
                angle(beginSegment1, endSegment1);
        double firstLineDistance = 
                distance(beginSegment1, endSegment1);
        
        double secondLineAngle = 
                angle(beginSegment2, endSegment2);
        double secondLineDistance = 
                distance(beginSegment2, endSegment2);
        
        Point result = intersection(
                beginSegment1, endSegment1, 
                beginSegment2, endSegment2);
        
        double firstControlDistance = signedDistance(
                beginSegment1, result, firstLineAngle);
        
        double secondControlDistance = signedDistance(
                beginSegment2, result, secondLineAngle);
        
        if(!((firstControlDistance > 0 && firstControlDistance <= firstLineDistance) && 
                (secondControlDistance > 0 && secondControlDistance <= secondLineDistance))){
            result = null;
        }
        
        return result;
    }
     
    /**
     * Verify if exist almost a real intersection between one of the lines of the
     * first list and one line of the second list.
     */
    public static boolean realIntersection(List<Line> lines1, List<Line> lines2){
        boolean result = false;
        
        for(Line line1:lines1){
            for (Line line2: lines2) {                
                Point intersectionPoint = realIntersection(line1.getBegin(),
                        line1.getEnd(), line2.getBegin(), line2.getEnd());
                
                if(intersectionPoint != null){
                    result = true;
                }                
            }        
        }       
        
        return result;        
    }
    
    public static Point intersection(
            Point beginSegment1, Point endSegment1, 
            Point beginSegment2, Point endSegment2){
        
        Point point = intersection(
                beginSegment1.getX(), beginSegment1.getY(),
                endSegment1.getX(), endSegment1.getY(),
                beginSegment2.getX(), beginSegment2.getY(),
                endSegment2.getX(), endSegment2.getY());
        
        return point;
    }
    
    public static Point intersection(
            double beginXSegment1, double beginYSegment1, 
            double endXSegment1, double endYSegment1, 
            double beginXSegment2, double beginYSegment2,
            double endXSegment2, double endYSegment2){
        
        double m1 = slope(beginXSegment1, beginYSegment1, endXSegment1, endYSegment1);
        
        double m2 = slope(beginXSegment2, beginYSegment2, endXSegment2, endYSegment2);
        
        double b1 = beginYSegment1 - m1 * beginXSegment1;
        double b2 = beginYSegment2 - m2 * beginXSegment2;
        
        double superX = (b2 - b1) / (m1 - m2);
        double superY = m1 * superX + b1;
        
        return new Point(superX, superY);
    }
    
    public static Point forceIntersection(
            Point beginSegment1, Point endSegment1, 
            Point beginSegment2, Point endSegment2){
        
        Point point = forceIntersection(
                beginSegment1.getX(), beginSegment1.getY(),
                endSegment1.getX(), endSegment1.getY(),
                beginSegment2.getX(), beginSegment2.getY(),
                endSegment2.getX(), endSegment2.getY());
        
        return point;
    }
    
    public static Point forceIntersection(
            double beginXSegment1, double beginYSegment1, 
            double endXSegment1, double endYSegment1, 
            double beginXSegment2, double beginYSegment2,
            double endXSegment2, double endYSegment2){
        
        Point superPoint = intersection(
                beginXSegment1, beginYSegment1, 
                endXSegment1, endYSegment1, 
                beginXSegment2, beginYSegment2,
                endXSegment2, endYSegment2);
        
        double superX = superPoint.getX();
        double superY = superPoint.getY();
        
        if(Double.isNaN(superX) || Double.isInfinite(superX)){
            superX = endXSegment1;
        }
        
        if(Double.isNaN(superY) || Double.isInfinite(superY)){
            superY = endYSegment1;
        }
        
        return new Point(superX, superY);
    }

    public static Quadrant quadrant(double xCenter, double yCenter, double x, double y){
        Quadrant result = Quadrant.First;
        if(x >= xCenter && y >= yCenter){
            result = Quadrant.First;
        }
        else if(x >= xCenter && y < yCenter){
            result = Quadrant.Second;
        }
        else if(x < xCenter && y < yCenter){
            result = Quadrant.Third;
        }
        else if(x < xCenter && y >= yCenter){
            result = Quadrant.Fourth;
        }
        
        return result;
    }
    
    public static double orientation(
            double xBegin, double yBegin,
            double xEnd, double yEnd,
            double xExternalPoint, double yExternalPoint){
        
        return ((xBegin - xExternalPoint) * (yEnd - yExternalPoint)) - 
                ((xEnd - xExternalPoint) * (yBegin - yExternalPoint));
    }
    
    public static double cosine(Point point1, Point point2, Point point3){
        return cosine(point1.getX(), point1.getY(), point2.getX(), point2.getY(), point3.getX(), point3.getY());
    }
    
    public static double cosine(double xPoint1, double yPoint1,
                             double xPoint2, double yPoint2,
                             double xPoint3, double yPoint3){
	double distance12 = GeometricOperations.distance(xPoint1, yPoint1, xPoint2, yPoint2);
        double distanceSquare12 = Math.pow(distance12, 2);
        double distance23 = GeometricOperations.distance(xPoint2, yPoint2, xPoint3, yPoint3);
        double distanceSquare23 = Math.pow(distance23, 2);
        
        double result;
        
	if (distanceSquare12 < 1 || distanceSquare23 < 1){
            result = 0;
        } else {
            double distance13 = GeometricOperations.distance(xPoint1, yPoint1, xPoint3, yPoint3);
            double distanceSquare13 = Math.pow(distance13, 2);
            double numerator = distanceSquare12 + distanceSquare23 - distanceSquare13;
            double denominator = distance12 * distance23;
            result = numerator / (2 * denominator);
        }
        
	return result;
    }
    
    public static boolean isInsideTriangle(Point triangleVertixA, Point triangleVertixB, Point triangleVertixC, Point testPoint) {
        double sideAB = (testPoint.getY()-triangleVertixA.getY())*(triangleVertixB.getX()-triangleVertixA.getX()) -
                (testPoint.getX()-triangleVertixA.getX())*(triangleVertixB.getY()-triangleVertixA.getY());
        double sideCA = (testPoint.getY()-triangleVertixC.getY())*(triangleVertixA.getX()-triangleVertixC.getX()) -
                (testPoint.getX()-triangleVertixC.getX())*(triangleVertixA.getY()-triangleVertixC.getY());
        double sideBC = (testPoint.getY()-triangleVertixB.getY())*(triangleVertixC.getX()-triangleVertixB.getX()) -
                (testPoint.getX()-triangleVertixB.getX())*(triangleVertixC.getY()-triangleVertixB.getY());
        
        return (sideAB * sideBC > 0) && (sideBC * sideCA > 0);
    }
    
    public static double distanceToLine(Point testPoint, Line line) {
        double deltaX = line.getEnd().getX() - line.getBegin().getX();
        double deltaY = line.getEnd().getY() - line.getBegin().getY();
        
	double denominator = Math.pow(deltaX, 2) + Math.pow(deltaY, 2);
	
 	double pointDeltaX = testPoint.getX() - line.getBegin().getX();
        double pointDeltaY = testPoint.getY() - line.getBegin().getY();
        
        double numerator = Math.pow(pointDeltaX * deltaY - pointDeltaY * deltaX, 2);
        
        return Math.sqrt(Math.abs(numerator / denominator));
    }
    
    public static Point crossProduct(Point point1, Point point2){
        return new Point(
                point1.getY() * point2.getZ() - point1.getZ() * point2.getY(),
                point1.getZ() * point2.getX() - point1.getX() * point2.getZ(),
                point1.getX() * point2.getY() - point1.getY() * point2.getX());
    }
    
    /** <summary>
     * Return the angle (in radians) between two vector
     *</summary>
     *<param name="vector1">Vector</param>
     *<param name="vector2">Vector</param>
     *<returns>double</returns>
     */
    public static double getAngle3D(Vector vector1, Vector vector2){
        double result;

        double distance1 = GeometricOperations.lengthSquared3D(vector1);
        double distance2 = GeometricOperations.lengthSquared3D(vector2);

        if (distance1 == 0){
            if (distance2 > 0)
                result = 1;
            else
                result = -1;
        }
        else if (distance2 == 0){
            if (distance1 > 0)
                result = 1;
            else
                result = -1;
        }
        else{
            double distance12 = Math.sqrt(distance1 * distance2);
            if (distance12 == 0){
                result = 1;
            }
            else{
                result = vector1.dotProduct(vector2) / distance12;

                if (result < -1)
                    result = -1;
                else if (result > 1)
                    result = 1;
                else
                    result = result;
            }
        }

        return Math.acos(result);
    }

    public static double lengthSquared3D(Vector p1){ 
        return p1.getX() * p1.getX() + p1.getY() * p1.getY() + p1.getZ() * p1.getZ(); 
    }
    
    /*
     *Method taken from the ChemUtil.cpp of c++
     */
    public static double findLargestAngleAroundPoint(List<Double> thetas, List<Double> interestingPos)
    {
        Double result = 0.0;
        Double theta = 0.0;

        if (thetas == null || thetas.size() == 0){
            return result;
        }            

        //Sort by position (increasing clockwise)
        Collections.sort(thetas);

        //Find the greatest difference between two successive positions
        thetas.add(thetas.get(0) + 2 * Math.PI);
        double maxt = 0;
        double maxk = thetas.get(thetas.size() - 1);
        int index = 0;
        for (int i = 0; i < thetas.size() - 1; i++){
            theta = thetas.get(i + 1) - thetas.get(i);

            // Two angles are treated as identical if they are within 3 degrees
            // In such a case, see if there is an "interesting" bond opposite this angle,
            // where an "interesting" bond is anything other than a plain single bond
            if (thetas.get(i) != thetas.get(0) && Math.abs(theta - maxt) < Math.PI / 60){
                if (interestingPos != null && interestingPos.size() != 0){
                    for (int j = 0; j < interestingPos.size(); j++){
                        Double oppTheta = interestingPos.get(j) + Math.PI;
                        if (oppTheta > 2 * Math.PI){
                            oppTheta -= 2 * Math.PI;
                        }
                        if (oppTheta > thetas.get(i) && oppTheta < thetas.get(i + 1)){
                            maxt = theta;
                            maxk = thetas.get(i);
                            index = i;
                        }
                    }
                } else {
                    Double thisDir = (thetas.get(i) + thetas.get(i+ 1)) / 2 + 2 * Math.PI;
                    Double thatDir = (maxk == thetas.get(thetas.size() - 1) ? (Math.PI / 2) : ((maxk + (thetas.get(index + 1))) / 2 + 2 * Math.PI));
                    // prefer angles towards top of screen
                    if ((int)Math.floor(thisDir / Math.PI) % 2 == 1 && (int)Math.floor(thatDir / Math.PI) % 2 != 1){
                        // ...unless the current angle is very close to horizontal (CSBR-23440)
                        if (Math.abs(Math.sin(thatDir)) > 0.0001){
                            maxt = theta;
                            maxk = thetas.get(i);
                            index = i;
                        }
                    }
                            }
                    }
            else if (theta > maxt)
            {
                maxt = theta;
                maxk = thetas.get(i);
                index = i;
            }                        
        }
        if (maxk == thetas.get(thetas.size() - 1))
            return 0;

        // Find the direction in the middle of the max difference
        theta = (maxk + (thetas.get(index + 1))) / 2;
        // Make sure the result is in [0,2*PI)
        if (theta >= 2 * Math.PI)
            theta -= 2 * Math.PI;
        if (theta < 0)
            theta += 2 * Math.PI;            

        result = theta;
        return result;
    }    
}
