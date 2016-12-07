
package translator.graphics.shapes.builders.configurations;

import translator.BuilderConfiguration;
import translator.utils.Point;

public class CircleConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    
    private Point center;
    private double radius;    
    
    public CircleConfiguration(Point center, double radius) {
        super(CIRCLE_BUILDER_ID);
        this.center = center;
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }    

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }
}
