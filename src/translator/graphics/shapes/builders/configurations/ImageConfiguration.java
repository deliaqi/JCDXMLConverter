package translator.graphics.shapes.builders.configurations;

import translator.BuilderConfiguration;
import translator.utils.Point;

/**
 * Class that contains information to configure an ImageBuilder
 * so it can correctly build an EmbeddedObject (Shape).
 */
public class ImageConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration{
    
    private String imageName;    
    private String imageBytes;
    private Point cornerPoint;
    private double width;
    private double height;
    private boolean rotate;
    private Point rotationCenter;
    private double rotationAngle;
    
    public ImageConfiguration() {
        super(IMAGE_BUILDER_ID);
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

    public String getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }
}
