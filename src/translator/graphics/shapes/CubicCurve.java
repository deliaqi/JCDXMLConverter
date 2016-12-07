package translator.graphics.shapes;

import translator.utils.Point;

public class CubicCurve extends QuadraticCurve {
    
    public CubicCurve() {
    }

    public Point getControlPoint2() {
        return getPoint(3);
    }

    public void setControlPoint2(Point controlPoint2) {
        addPoint(controlPoint2, 3);
    }
    
}
