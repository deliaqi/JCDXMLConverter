package translator.graphics.shapes.builders.configurations;

import translator.BuilderConfiguration;
import translator.utils.Point;

public class RectangleConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    
    private Point beginPoint;
    private Point endPoint;
    
    public RectangleConfiguration(Point beginPoint, Point endPoint) {
        super(RECTANGLE_BUILDER_ID);
        this.beginPoint = beginPoint;
        this.endPoint = endPoint;
    }

    public Point getBeginPoint() {
        return beginPoint;
    }

    public void setBeginPoint(Point beginPoint) {
        this.beginPoint = beginPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }
}
