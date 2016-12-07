package translator.graphics.shapes;
import translator.cddom.properties.BoundingBox;
import translator.utils.Point;

public class Circle extends Shape {
        
    private double radius;
    
    public Circle() {
    }
    
    public Point getCenter(){
        return getPoint(0);
    }
    
    public void setCenter(Point center) {
        addPoint(center, 0);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    public BoundingBox getBoundingBox(){
             
        double nearestX = getCenter().getX() - getRadius();
        double nearestY = getCenter().getY() - getRadius();
        
        double farthermostX = getCenter().getX() + getRadius();
        double farthermostY = getCenter().getY() + getRadius();
        
        //Calculate the resulting BoundingBox.
        return new BoundingBox(new Point(nearestX - getStrokeWidth(), nearestY - getStrokeWidth()),
                farthermostX - nearestX + getStrokeWidth() * 2,
                farthermostY - nearestY + getStrokeWidth() * 2);   
        
    }
    
}
