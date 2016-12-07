package translator.graphics.shapes.builders.configurations;

import java.util.ArrayList;
import java.util.List;
import translator.BuilderConfiguration;
import translator.graphics.shapes.gradients.Gradient;
import translator.utils.Point;

public class SplineConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    
    private List<SegmentConfiguration> segments = new ArrayList();
    private boolean closed;    
    private boolean translate;
    private double moveX;
    private double moveY;
    private double scale;
    private LineJoin lineJoin;
    
    public SplineConfiguration(List<Point> points, boolean closed) {
        this(points, closed, FillRule.EvenOdd, LineJoin.Miter);
    }
    
    public SplineConfiguration(List<Point> points, boolean closed, LineJoin lineJoin) {
        this(points, closed, FillRule.EvenOdd, lineJoin);
    }
    
    public SplineConfiguration(List<Point> points, boolean closed, FillRule fillRule, LineJoin lineJoin) {
        super(SPLINE_BUILDER_ID);
        setFillRule(fillRule);
        setLineJoin(lineJoin);
        this.segments = new ArrayList();
        this.closed = closed;
        
        Point previousPoint = points.get(0);
               
        for (Point currentPoint : points) {
            segments.add(new SegmentConfiguration(previousPoint, currentPoint));
            
            previousPoint = currentPoint;
        }
    }    
    
    public SplineConfiguration(List<SegmentConfiguration> segments) {
        super(SPLINE_BUILDER_ID);
        setFillRule(FillRule.EvenOdd);
        setLineJoin(lineJoin.Bevel);
        this.closed = closed;
        this.segments.addAll(segments);
    }

    public List<SegmentConfiguration> getSegments() {
        return segments;
    }    
    
    public boolean isClosed() {
        return closed;
    }
    
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public double getMoveX() {
        return moveX;
    }

    public void setMoveX(double moveX) {
        this.moveX = moveX;
    }    

    public double getMoveY() {
        return moveY;
    }

    public void setMoveY(double moveY) {
        this.moveY = moveY;
    }

    public LineJoin getLineJoin() {
        return lineJoin;
    }

    public void setLineJoin(LineJoin lineJoin) {
        this.lineJoin = lineJoin;
    }

    public boolean isTranslate() {
        return translate;
    }

    public void setTranslate(boolean translate) {
        this.translate = translate;
    }
}
