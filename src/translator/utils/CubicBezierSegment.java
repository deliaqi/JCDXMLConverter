package translator.utils;

public class CubicBezierSegment extends BezierSegment{

    public CubicBezierSegment() {
        setBeginPoint(new Point());
        setEndPoint(new Point());
        setControlPoint1(new Point());
        setControlPoint2(new Point());
    }
    
    public CubicBezierSegment(CubicBezierSegment segment) {
        super(segment);
    }
    
    public CubicBezierSegment(Point beginPoint, Point endPoint, Point controlPoint1, Point controlPoint2) {
        setBeginPoint(beginPoint);
        setEndPoint(endPoint);
        setControlPoint1(controlPoint1);
        setControlPoint2(controlPoint2);
    }
    
    public void setControlPoint1(Point controlPoint){
        addControlPoint(0, controlPoint);
    }
    
    public Point getControlPoint1(){
        return getControlPoint(0);
    }
    
    public void setControlPoint2(Point controlPoint){
        addControlPoint(1, controlPoint);
    }
    
    public Point getControlPoint2(){
        return getControlPoint(1);
    }
}
