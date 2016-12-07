package translator.graphics.shapes.builders.configurations;

import translator.utils.Point;

public class QuadraticCurveConfiguration extends SegmentConfiguration implements CurveConfiguration {
    
    private Point controlPoint1;
    
    public QuadraticCurveConfiguration(Point point1, Point point2, Point controlPoint) {
        super(point1, point2);
        
        setBuilderId(QUADRATIC_CURVE_BUILDER_ID);
        this.controlPoint1 = controlPoint;
    }

    public Point getControlPoint() {
        return controlPoint1;
    }

    public void setControlPoint(Point controlPoint) {
        this.controlPoint1 = controlPoint;
    }
    
}
