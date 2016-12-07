
package translator.graphics.shapes.gradients;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.Color;
import translator.utils.Point;

public class RadialGradient extends Gradient {
    
    // default shaded gradient starts with white
    private static final Color WHITE = new Color(255, 255, 255);
    
    public static final String DEFAULT_SHADED_GRADIENT_ID = "defaultShaded";
    
    // center and radius describe a circle that is the gradient's bounds
    private Point center;
    private double radius;
    
    // center of the gradient light
    private Point focus;
    
    public RadialGradient(String id, Point center, double radius, Point focus) {
        super(id);
        this.center = center;
        this.radius = radius;
        this.focus = focus;
    }
    
    /**
     * Creates the default gradient, similar to the ChemDraw shaded attribute.
     */
    public static RadialGradient getOvalGradient(String gradientId, Color fillColor) {
        Point center = new Point(0.5, 0.5);
        Point focus = new Point(0.33, 0.25);
        
        RadialGradient result = new RadialGradient(gradientId, center, 0.6, focus);
        
        // add the shaded gradient stops
        result.addStop(new GradientStop(0, WHITE));
        result.addStop(new GradientStop(25, WHITE));
        result.addStop(new GradientStop(80, Color.fadeRGB(fillColor, 75)));
        result.addStop(new GradientStop(90, fillColor));
        result.addStop(new GradientStop(100, fillColor));
        
        return result;
    }
    
    /**
     * Creates the default gradient, similar to the ChemDraw shaded attribute.
     */
    public static RadialGradient getGraphicsGradient(String gradientId, Color fillColor) {
        Point center = new Point(0.15, 0.25);
        Point focus = new Point(0.15, 0.25);
        
        RadialGradient result = new RadialGradient(gradientId, center, 1.75, focus);
        
        // add the shaded gradient stops
        result.addStop(new GradientStop(10, WHITE));
        result.addStop(new GradientStop(90, fillColor));
        
        return result;
    }
    
    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Point getFocus() {
        return focus;
    }

    public void setFocus(Point focus) {
        this.focus = focus;
    }
    
}
