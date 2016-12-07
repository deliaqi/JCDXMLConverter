package translator.graphics.shapes;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import translator.cddom.properties.BoundingBox;
import translator.utils.Point;

public class Ellipse extends Shape {
    
    private double radiusX;
    private double radiusY;
    private double rotationAngle;
    
    public Ellipse() {
    }
    
    public Point getCenter(){
        return getPoint(0);
    }
    
    public void setCenter(Point center) {
        addPoint(center, 0);
    }

    public double getRadiusX() {
        return radiusX;
    }

    public void setRadiusX(double radiusX) {
        this.radiusX = radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    public void setRadiusY(double radiusY) {
        this.radiusY = radiusY;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
    }
    
    public BoundingBox getBoundingBox(){
        
        double uperLeftX = getCenter().getX() - radiusX;
        double uperLeftY = getCenter().getY() - radiusY;
        
        Ellipse2D ellipse = new Ellipse2D.Double(uperLeftX, uperLeftY, getRadiusX() * 2, getRadiusY() * 2);        
        
        java.awt.Shape transformedShape = AffineTransform.getRotateInstance(getRotationAngle()).createTransformedShape(ellipse);
               
        Rectangle2D shapeBounds = transformedShape.getBounds2D();
           
        //Calculate the resulting BoundingBox.        
        return new BoundingBox(new Point(shapeBounds.getMinX() - getStrokeWidth(), shapeBounds.getMinY() - getStrokeWidth()),
                shapeBounds.getWidth() + getStrokeWidth() * 2,
                shapeBounds.getHeight() + getStrokeWidth() * 2);   
        
    }
    
}
