package translator.graphics.shapes;

import translator.cddom.properties.BoundingBox;
import translator.utils.Point;

/**
 * Class that represents the embedded object 
 * 
 */
public class EmbeddedObject extends Shape{
    
    private String imageName;    
    private String imageBytes;
    private Point cornerPoint;
    private double width;
    private double height;
    private boolean rotate;
    private Point rotationCenter;
    private double rotationAngle;
    
    public EmbeddedObject() {
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public Point getCornerPoint() {
        return cornerPoint;
    }
    
    public void setCornerPoint(Point cornerPoint) {
        this.cornerPoint = cornerPoint;
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
    
    public boolean isRotate() {
        return rotate;
    }
    
    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }
    
    public Point getRotationCenter() {
        return rotationCenter;
    }
    
    public void setRotationCenter(Point rotationCenter) {
        this.rotationCenter = rotationCenter;
    }
    
    public double getRotationAngle() {
        return rotationAngle;
    }
    
    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public BoundingBox getBoundingBox(){
       
        //Calculate the resulting BoundingBox.
        return new BoundingBox(getCornerPoint(), getWidth(), getHeight());
    }

    public String getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }
    
}
