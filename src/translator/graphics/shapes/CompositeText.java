
package translator.graphics.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import translator.cddom.properties.BoundingBox;
import translator.graphics.shapes.builders.configurations.TextConfiguration;

/**
 * Text object that contains <code>Text</code> instances inside,
 * representing individual lines of text.<br>
 * Lines must be added through the <code>addLine()</code> method.<br>
 * This is only a container, format and position information must be specified
 * per line, using the <code>Text</code> class.
 */
public class CompositeText extends CompositeShape<Text> {
    
    private int justification = Text.LEFT_JUSTIFICATION;
    private int alignment = Text.LEFT_ALIGNMENT;
    private int rotationAngle;
    private double lineHeight;
    private BoundingBox boundingBox;
    
    /** Creates a new instance of CompositeText */
    public CompositeText() {
        
    }
    
    /**
     * Adds a line of text to the composite text.
     */
    public void addLine(Text line) {
        super.addShape(line);
    }
    
    /**
     * Inserts a line of text to the composite text, at the specified index.
     */
    public void insertLine(int index, Text line) {
        super.addShape(index, line);
    }
    
    /**
     * Returns the lines of text in this composite text.
     */
    public List<Text> getLines() {
        return super.getShapes();
    }
    
    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public int getJustification() {
        return justification;
    }

    public void setJustification(int justification) {
        this.justification = justification;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }
    
}
