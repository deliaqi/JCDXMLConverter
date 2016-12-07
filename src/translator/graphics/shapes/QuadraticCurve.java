package translator.graphics.shapes;

import translator.utils.Point;

public class QuadraticCurve extends Segment implements Curve {
    
    public QuadraticCurve() {
    }
    
    public Point getControlPoint() {
        return getPoint(2);
    }

    public void setControlPoint(Point controlPoint) {
        addPoint(controlPoint, 2);
    }
}
