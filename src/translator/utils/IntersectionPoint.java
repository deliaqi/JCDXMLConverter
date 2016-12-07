package translator.utils;

import java.util.ArrayList;
import java.util.List;

public class IntersectionPoint extends Point {
    
    private String id;
    
    private double alphaAngle;
    private double betaAngle;
    
    private List<Point> boundIntersection = new ArrayList();
    
    public IntersectionPoint(String id, double x, double y, double alphaAngel, double betaAngle) {
        this(id, x, y, 0, alphaAngel, betaAngle);
    }
    
    public IntersectionPoint(String id, double x, double y, double z, double alphaAngel, double betaAngle){
        super(x, y, z, false);
        
        this.id = id;
        this.alphaAngle = alphaAngel;
        this.betaAngle = betaAngle;
    }
    
    public IntersectionPoint(IntersectionPoint point){
        super(point.getX(), point.getY(), point.getZ(), point.isOmit());
    }
    
    /**
     *Create a bound of the intersection point
     *
     *@param curveSpacing This is the size to calculate the bound corners
     */
    public void createBound(double curveSpacing){
        if(boundIntersection.size() == 0){
            boundIntersection.add(
                    GeometricOperations.offset(
                    GeometricOperations.offset(
                    this, getAlphaAngle(), curveSpacing),
                    getBetaAngle(), curveSpacing));

            boundIntersection.add(
                    GeometricOperations.offset(
                    GeometricOperations.offset(
                    this, getAlphaAngle(), -curveSpacing),
                    getBetaAngle(), curveSpacing));

            boundIntersection.add(
                    GeometricOperations.offset(
                    GeometricOperations.offset(
                    this, getAlphaAngle(), curveSpacing),
                    getBetaAngle(), -curveSpacing));

            boundIntersection.add(
                    GeometricOperations.offset(
                    GeometricOperations.offset(
                    this, getAlphaAngle(), -curveSpacing),
                    getBetaAngle(), -curveSpacing));
        }
    }
    
    /**
     *Return the corner closest to the point
     *
     *@param point Reference point
     */
    public Point findClosestBound(Point point){
        Point result = null;
        double minDistance = Double.MAX_VALUE;
        
        for(Point intersection : boundIntersection){
            double distance = GeometricOperations.distance(
                    intersection, point);
            
            if(distance < minDistance){
                result = new Point(intersection);
                minDistance = distance;
            }
        }
        
        return result;
    }
    
    /**
     *Return the opposite corner to the reference point
     *
     *@param cornerPoint Reference point. This must be a corner point existing
     *in this intersection point object
     */
    public Point getOppositePoint(Point cornerPoint){
        Point result = null;
        
        if(cornerPoint.equals(boundIntersection.get(0))){
            result = new Point(boundIntersection.get(3));
        }
        else if(cornerPoint.equals(boundIntersection.get(1))){
            result = new Point(boundIntersection.get(2));
        }
        else if(cornerPoint.equals(boundIntersection.get(2))){
            result = new Point(boundIntersection.get(1));
        }
        else if(cornerPoint.equals(boundIntersection.get(3))){
            result = new Point(boundIntersection.get(0));
        }
        
        return result;
    }

    public double getAlphaAngle() {
        return alphaAngle;
    }

    public double getBetaAngle() {
        return betaAngle;
    }

    public List<Point> getBoundIntersection() {
        return boundIntersection;
    }

    public String getId() {
        return id;
    }
    
}
