package translator.utils;

import java.util.ArrayList;
import java.util.List;

public class Line implements Comparable {
    
    private static int PRECISION = 1000;
    
    private String id;
    
    private Point begin;
    private Point end;
    
    private double yIntercept;
    private double slope;
    private double angle;
    
    public Line(String id, Point begin, Point end) {
        this.id = id;
        this.begin = new Point(begin);
        this.end = new Point(end);
        
        calculate();
    }
    
    public Line(Point begin, Point end) {
        this.begin = new Point(begin);
        this.end = new Point(end);
        
        calculate();
    }
    
    public Line(Point begin, double angle, double length){
        this.begin = new Point(begin);
        this.end = GeometricOperations.offset(
                this.begin, angle, length);
        
        calculate();
    }
    
    private void calculate(){
        angle = GeometricOperations.angle(begin.getX(), begin.getY(), end.getX(), end.getY());
        slope = GeometricOperations.slope(begin.getX(), begin.getY(), end.getX(), end.getY());
        yIntercept = GeometricOperations.slopeIntercept(begin.getX(), begin.getY(), slope);
    }

    /**
     *Create a list of sublines with the specified length
     */
    public List<Line> splitLine(double subLineLength){
        List<Line> result = new ArrayList();
        Point lastPoint = begin;
        
        double distance = GeometricOperations.distance(begin, end);
        
        if(subLineLength > 0){
            Point newPoint = GeometricOperations.offset(
                begin, angle, subLineLength);
            
            double newDistance = GeometricOperations.distance(begin, newPoint);
            
            while(newDistance < distance){
                result.add(new Line(lastPoint, newPoint));
                
                lastPoint = newPoint;
                newPoint = GeometricOperations.offset(
                        lastPoint, angle, subLineLength);
            
                newDistance = GeometricOperations.distance(begin, newPoint);
            }
        }
            
        result.add(new Line(lastPoint, end));
        
        return result;
    }
    
    public Point getBegin() {
        return begin;
    }

    public void setBegin(Point begin) {
        this.begin = begin;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public double getYIntercept() {
        return yIntercept;
    }

    public double getSlope() {
        return slope;
    }

    public double getAngle() {
        return angle;
    }
    
    public String getId() {
        return id;
    }
    
    public int compareTo(Object object) {
        int result;
        if(object instanceof GeometricLine){
            Line otherLine = (Line) object;
            
            result = new Double(this.getAngle() * PRECISION - otherLine.getAngle() * PRECISION).intValue();
            
            // result can't be zero because this line will be ignored in sets
            // force value of 1 so line is positioned after otherLine in sets
            if (result == 0) {
                result = 1;
            }
        }
        else {
            result = 1;
        }
        return result;
    }
}
