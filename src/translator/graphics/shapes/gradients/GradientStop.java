
package translator.graphics.shapes.gradients;

import translator.graphics.Color;

/**
 * Represents a change in the gradient color, at a certain offset.
 */
public class GradientStop {
    
    private double offset;
    private Color color;
    
    /**
     * Creates a GradientStop at the specified offset, with the specified color.
     */
    public GradientStop(double offset, Color color) {
        this.offset = offset;
        this.color = color;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
}
