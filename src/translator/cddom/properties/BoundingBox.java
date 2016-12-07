package translator.cddom.properties;

import translator.utils.Point;

public class BoundingBox {
    
    private Point position;
    
    private double width;
    private double height;
    
    public BoundingBox(Point position, double width, double height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
    
}
