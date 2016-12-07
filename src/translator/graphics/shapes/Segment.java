package translator.graphics.shapes;

import translator.utils.Point;

public class Segment extends Shape {
    
    private boolean moveTo;
    
    public Segment() {
    }
    
    public Point getBeginPoint() {
        return getPoint(0);
    }
    
    public void setBeginPoint(Point point) {
        addPoint(point, 0);
    }
    
    public Point getEndPoint() {
        return getPoint(1);
    }
    
    public void setEndPoint(Point point) {
        addPoint(point, 1);
    }

    public boolean isMoveTo() {
        return moveTo;
    }

    public void setMoveTo(boolean moveTo) {
        this.moveTo = moveTo;
    }

}
