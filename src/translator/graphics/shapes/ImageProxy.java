package translator.graphics.shapes;

import translator.cddom.properties.BoundingBox;
import translator.graphics.Image;
import translator.utils.Point;

/**
 * Class that implements the Proxy Pattern.
 * This class has only the reference where
 * the real image is, so the converter that
 * uses it works with the image proxy without
 * knowing the rest of the implementation
 * to render an image file
 */
public class ImageProxy extends Shape{
    
    private String imageName;
    private String imageDirectory;
    private Point cornerPoint;
    private double width;
    private double height;
    private boolean rotate;
    private Point rotationCenter;
    private double rotationAngle;
    
    public ImageProxy() {
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public String getImageDirectory() {
        return imageDirectory;
    }
    
    public void setImageDirectory(String imageDirectory) {
        this.imageDirectory = imageDirectory;
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
    
}
