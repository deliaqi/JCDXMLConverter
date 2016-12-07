
package translator.graphics.shapes.builders.configurations;

import translator.BuilderConfiguration;
import translator.utils.Point;

public class SegmentConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    
    private Point beginPoint;
    private Point endPoint;
    boolean relativeCoordinates;
    private boolean moveTo;
    
    public SegmentConfiguration(Point beginPoint, Point endPoint) {
        this(beginPoint, endPoint, false);
    }    
    
    public SegmentConfiguration(Point beginPoint, Point endPoint, boolean moveTo) {
        super(SEGMENT_BUILDER_ID);
        this.beginPoint = beginPoint;
        this.endPoint = endPoint;
        this.moveTo = moveTo;
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

    public boolean isMoveTo() {
        return moveTo;
    }

    public void setMoveTo(boolean moveTo) {
        this.moveTo = moveTo;
    }

}
