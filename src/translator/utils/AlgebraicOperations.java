package translator.utils;

import java.util.ArrayList;
import java.util.List;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;

public class AlgebraicOperations {
    
    public AlgebraicOperations() {
    }
    
    public static List<Point> bezierFunction(double precision, List<Point> controlPoints){
        List<Point> newPoints = new ArrayList();
        
        int order = controlPoints.size() - 1;
        
        for(double point = 0; point <= 1; point += precision){
            
            double newX = 0;
            double newY = 0;
            
            for(int k = 0; k <= order; k++){
                Point controlPoint = controlPoints.get(k);
                
                double controlPointX = controlPoint.getX();
                double controlPointY = controlPoint.getY();
                
                newX += controlPointX * (factorial(order) / (factorial(k) * (factorial(order - k)))) * Math.pow(point, k) * Math.pow((1 - point), (order - k));
                newY += controlPointY * (factorial(order) / (factorial(k) * (factorial(order - k)))) * Math.pow(point, k) * Math.pow((1 - point), (order - k));
            }
            
            newPoints.add(new Point(newX, newY));
        }
        
        return newPoints;
    }
    
    public static List<Point> bezierFunction(double precision, CubicCurveConfiguration curve) {
        List<Point> controlPoints = new ArrayList();
        controlPoints.add(curve.getBeginPoint());
        controlPoints.add(curve.getControlPoint());
        controlPoints.add(curve.getControlPoint2());
        controlPoints.add(curve.getEndPoint());
        return bezierFunction(precision, controlPoints);
    }
    
    public static int factorial(int number){
        int result;
        if(number <= 1){
            result = 1;
        }
        else{
            result = number * factorial(number - 1);
        }
        return result;
    }
    
    static public List<BezierSegment> bezierApproximation(List<Point> points, int smoothness, double precision) {
        List<BezierSegment> bezierSegments = new ArrayList();

        int pointCount = points.size();
        int endIndex;
        int bezierStart;
        int bezierEnd;
        double error;
        
        endIndex = pointCount - 1;
        bezierStart = 0;

        do {
            bezierEnd = bezierStart + 1;

            if (bezierEnd > endIndex) {
                bezierEnd = endIndex;
            }

            error = 0;

            CubicBezierSegment segment = new CubicBezierSegment();
            
            while ((bezierEnd <= endIndex) && (error < precision)) {
                segment = createBezierSegment(points, bezierStart, bezierEnd, segment, smoothness / 100.1);
                error = BezierSegmentError(points, bezierStart, bezierEnd, segment);
                bezierEnd++;
            }
            bezierEnd--;

            createBezierSegment(points, bezierStart, bezierEnd, segment,
                    smoothness / 100.1);

            bezierSegments.add(segment);
            bezierStart = bezierEnd;
        } while (!(bezierEnd == endIndex));

        return bezierSegments;
    }

    //	   Create a Bezier segment for a range of points from StartIdx to EndIdx.
    //	   Smooth is a value between 0 and 1 and describes the distance of the control points from the begin and end points
    //	   as a percentage of the total segment distance.
    static private CubicBezierSegment createBezierSegment(List<Point> points, int startIndex,
                                            int endIndex, CubicBezierSegment segment, double smooth) {
        int pointCount = points.size();

        double distance;

        Point slope1 = calculateSlopeAtPoint(points, startIndex);
        Point slope2 = calculateSlopeAtPoint(points, endIndex);

        CubicBezierSegment newSegment = new CubicBezierSegment(segment);

        if ((startIndex >= 0) && (startIndex < pointCount)) {
            newSegment.setBeginPoint(points.get(startIndex));
        }

        if ((endIndex >= 0) && (endIndex < pointCount)) {
            newSegment.setEndPoint(points.get(endIndex));
        }

        distance = GeometricOperations.distance(
                newSegment.getBeginPoint(), newSegment.getEndPoint());
        
        Point newControlPoint1 = new Point(
                (newSegment.getBeginPoint().getX() + (0.5 * distance * smooth * slope1.getX())),
                (newSegment.getBeginPoint().getY() + (0.5 * distance * smooth * slope1.getY())));
        
        Point newControlPoint2 = new Point(
                (newSegment.getEndPoint().getX() - (0.5 * distance * smooth * slope2.getX())),
                (newSegment.getEndPoint().getY() - (0.5 * distance * smooth * slope2.getY())));
        
        newSegment.setControlPoint1(newControlPoint1);
        newSegment.setControlPoint2(newControlPoint2);
        
        return newSegment;
    }

    //	   Calculate the slope (delta X and Y, scaled to length 1) of the point at Index. A weighted
    //	   average of direct neighbours (70%) and 2nd neighbours (30%) is used.
    static private Point calculateSlopeAtPoint(List<Point> points, int index) {
        Point slope = new Point();
        double distance;

        Point left = new Point(pointAtIndex(points, index - 1, true));
        Point right = new Point(pointAtIndex(points, index + 1, true));

        Point slope1 = right.subtract(left);

        distance = GeometricOperations.distance(slope1);

        if (distance > 0) {
            slope1.setX(slope1.getX() / distance);
            slope1.setY(slope1.getY() / distance);
        }

        left = new Point(pointAtIndex(points, index - 2, true));
        right = new Point(pointAtIndex(points, index + 2, true));

        Point slope2 = right.subtract(left);
        distance = GeometricOperations.distance(slope2);

        if (distance > 0) {
            slope2.setX(slope2.getX() / distance);
            slope2.setY(slope2.getY() / distance);
        }
        
        slope.setX(slope1.getX() * 0.7 + slope2.getX() * 0.3);
        slope.setY(slope1.getY() * 0.7 + slope2.getY() * 0.3);

        return slope;

    }

    static private Point pointAtIndex(List<Point> points, int index, boolean mirror) {
        int pointCount = points.size();
        Point point = new Point(0, 0);

        if ((index >= 0) && (index < pointCount)) {
            point = new Point(points.get(index));
        } 
        else if (mirror) {
            if (index < 0) {
                index = -index;

                if ((index >= 0) && (index < pointCount)) {
                    point.setX((2 * points.get(0).getX()) -
                            points.get(index).getX());
                    point.setY((2 * points.get(0).getX()) -
                            points.get(index).getY());
                }
            } else {
                index = (2 * pointCount) - index - 2;

                if ((index >= 0) && (index < pointCount)) {
                    point.setX((2 * points.get(pointCount - 1).getX()) -
                            points.get(index).getX());
                    point.setY((2 * points.get(pointCount - 1).getY()) -
                            points.get(index).getY());
                }
            }
        }
        return point;
    }

    static private double BezierSegmentError(List<Point> points, int startIndex,
                                             int endIndex, CubicBezierSegment segment) {
        int PointCount = points.size();
        double error;
        double result = 0;

        if ((endIndex - startIndex) < 2) {
            return result;
        }

        for (int i = startIndex + 1; i < endIndex; i++) {
            if ((i < 0) || (i >= PointCount)) {
                return result;
            }

            Point left = new Point(pointAtIndex(points, i - 1, true));
            Point right = new Point(pointAtIndex(points, i + 1, true));
            Point pPoint = new Point(pointAtIndex(points, i, false));
            
            double deltaX = right.getX() - left.getX();
            double deltaY = right.getY() - left.getY();
            
            double distance = GeometricOperations.distance(deltaX, deltaY);

            double u = (i - startIndex) / (endIndex - startIndex);

            if (distance == 0) {
                error = intersectBezierAndLine(segment, deltaX, deltaY, pPoint, u);
            } else {
                error = intersectBezierAndLine(segment, deltaX / distance, deltaY / distance, pPoint, u);
            }

            if (error > result) {
                result = error;
            }
        }

        return result;
    }

    //	   Intersect a Bezier segment with a line A*X + B*Y + C = 0, where C is determined from point
    //	   (PX, PY) which is on the line. U must contain a reasonable estimate, resulting exact U
    //	   will be returned. The distance from P to the bezier curve is calculated in Err
    static private double intersectBezierAndLine(
            CubicBezierSegment segment, double aComponent, double bComponent, Point point, double u) {
        double error;

        double cTol = 1e-4;
        double cIterMax = 5;
        double uOrig;
        int iterator;

        double u2 = 0;
        double u3 = 0;

        CubicBezierSegment newSegment = new CubicBezierSegment(segment);

        double cComponent = -((aComponent * point.getX()) + (bComponent * point.getY()));

        double x3 = 
                (-1 * newSegment.getBeginPoint().getX()) + 
                (3 * newSegment.getControlPoint1().getX()) + 
                (-3 * newSegment.getControlPoint2().getX()) +
                (1 * newSegment.getEndPoint().getX());

        double x2 = 
                (3 * newSegment.getBeginPoint().getX()) + 
                (-6 * newSegment.getControlPoint1().getX()) + 
                (3 * newSegment.getControlPoint2().getX());

        double x1 = 
                (-3 * newSegment.getBeginPoint().getX()) + 
                (3 * newSegment.getControlPoint1().getX());

        double x0 = 1 * newSegment.getBeginPoint().getX();

        double y3 = 
                (-1 * newSegment.getBeginPoint().getY()) + 
                (3 * newSegment.getControlPoint1().getY()) + 
                (-3 * newSegment.getControlPoint2().getY()) +
                (1 * newSegment.getEndPoint().getY());

        double y2 = 
                (3 * newSegment.getBeginPoint().getY()) + 
                (-6 * newSegment.getControlPoint1().getY()) + 
                (3 * newSegment.getControlPoint2().getY());

        double y1 = 
                (-3 * newSegment.getBeginPoint().getY()) + 
                (3 * newSegment.getControlPoint1().getY());

        double y0 = 1 * newSegment.getBeginPoint().getY();

        double polynomialCoefficient0 = (aComponent * x0) + (bComponent * y0) + cComponent;
        double polynomialCoefficient1 = (aComponent * x1) + (bComponent * y1);
        double polynomialCoefficient2 = (aComponent * x2) + (bComponent * y2);
        double polynomialCoefficient3 = (aComponent * x3) + (bComponent * y3);

        double differentialCoefficient0 = polynomialCoefficient1;
        double differentialCoefficient1 = 2 * polynomialCoefficient2;
        double differentialCoefficient2 = 3 * polynomialCoefficient3;

        iterator = 0;
        uOrig = u;

        double value = (polynomialCoefficient3 * Math.pow(u, 3)) +
                (polynomialCoefficient2 * Math.pow((u) + 
                (polynomialCoefficient1 * u) + polynomialCoefficient0, 2));

        double dValue = differentialCoefficient2 * Math.pow((u) + 
                (differentialCoefficient1 * u) + differentialCoefficient0, 2);

        do {
            iterator++;

            if (dValue != 0) {
                u = u - (value / dValue);
                u3 = Math.pow(u, 3);
                u2 = Math.pow(u, 2);

                value = (polynomialCoefficient3 * u3) +
                        (polynomialCoefficient2 * u2) +
                        (polynomialCoefficient1 * u) +
                        polynomialCoefficient0;

                dValue = (differentialCoefficient2 * u2) +
                        (differentialCoefficient1 * u) +
                        differentialCoefficient0;
            }
        } while (!((Math.abs(value) <= cTol) || (iterator > cIterMax)));

        if (Math.abs(value) > cTol) {
            u3 = Math.pow(uOrig, 3);
            u2 = Math.pow(uOrig, 2);
            u = uOrig;
        }

        error = GeometricOperations.distance(
                (x3 * u3) + (x2 * u2) + (x1 * u) + x0, point.getX(),
                (y3 * u3) + (y2 * u2) + (y1 * u) + y0, point.getY());

        return error;
    }

    public static boolean isOdd(int number)
    { 
        boolean result = false;
        if (number % 2 != 0)
            result = true;
        return result;
    }
}
