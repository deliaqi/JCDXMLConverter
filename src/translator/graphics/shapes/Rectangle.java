package translator.graphics.shapes;

import translator.utils.Point;

public class Rectangle extends Shape {

    public Rectangle() {
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
}
