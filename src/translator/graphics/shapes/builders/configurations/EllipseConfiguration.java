package translator.graphics.shapes.builders.configurations;

import translator.BuilderConfiguration;
import translator.utils.Point;

public class EllipseConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    private Point center;
    private double radiusX;
    private double radiusY;
    private double rotationAngle;
    
    public EllipseConfiguration(Point center, double radiusX, double radiusY, double rotationAngle) {
        super(ELLIPSE_BUILDER_ID);
        this.center = center;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.rotationAngle = rotationAngle;
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

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }
    
}
