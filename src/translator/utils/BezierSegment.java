package translator.utils;

import java.util.ArrayList;
import java.util.List;

public class BezierSegment {

    private Point beginPoint;
    private Point endPoint;
    
    private List<Point> controlPoints = new ArrayList();
    
    public BezierSegment() {
    }
    
    public BezierSegment(BezierSegment segment) {
        setBeginPoint(segment.getBeginPoint());
        setEndPoint(segment.getEndPoint());
        
        for(int i = 0; i < segment.controlPoints.size(); i++){
            addControlPoint(i, segment.getControlPoint(i));
        }
    }

    public Point getBeginPoint() {
        return beginPoint;
    }

    public void setBeginPoint(Point beginPoint) {
        this.beginPoint = new Point(beginPoint);
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = new Point(endPoint);
    }
    
    public void addControlPoint(int index, Point controlPoint){
        controlPoints.add(index, new Point(controlPoint));
    }
    
    public Point getControlPoint(int index){
        return controlPoints.get(index);
    }
}
