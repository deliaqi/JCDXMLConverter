package translator.graphics.shapes.builders.configurations;

import translator.utils.Point;

public class CubicCurveConfiguration extends QuadraticCurveConfiguration implements CurveConfiguration {
    
    private Point controlPoint2;
    
    public CubicCurveConfiguration(Point point1, Point point2, Point controlPoint1, Point controlPoint2) {
        super(point1, point2, controlPoint1);
        
        setBuilderId(CUBIC_CURVE_BUILDER_ID);
        this.controlPoint2 = controlPoint2;
    }
    
    public Point getControlPoint2() {
        return controlPoint2;
    }

    public void setControlPoint2(Point controlPoint2) {
        this.controlPoint2 = controlPoint2;
    }
    
}
