package translator.graphics.shapes;

import translator.graphics.shapes.builders.configurations.FillRule;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.LineCap;

public class Spline extends CompositeShape<Segment> {
    
    private FillRule fillRule;
    private boolean closed;
    private double scale;
    private boolean translate;
    private double moveX;
    private double moveY;
    private LineJoin lineJoin;
    
    public Spline() {
    }
    
    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public FillRule getFillRule() {
        return fillRule;
    }

    public void setFillRule(FillRule fillRule) {
        this.fillRule = fillRule;
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
