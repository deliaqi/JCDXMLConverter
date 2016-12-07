
package translator.graphics.shapes.builders.configurations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import translator.BuilderConfiguration;
import translator.cddom.properties.BoundingBox;

/**
 * Text object that contains <code>TextConfiguration</code> instances inside,
 * representing individual lines of text.<br>
 * Lines must be added through the <code>addLine()</code> method.<br>
 * This is only a container. Format and position information must be specified
 * per line, using the <code>TextConfiguration</code> class.
 */
public class CompositeTextConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    
    private List<TextConfiguration> lines;
    private int justification = TextConfiguration.LEFT_JUSTIFICATION;
    private int alignment = TextConfiguration.LEFT_ALIGNMENT;
    private int rotationAngle;
    private double lineHeight;
    //This value indicates the biggest lenght of all lines in this composite.
    private double biggestWholeLineLenght = 0;
    private BoundingBox boundingBox;
    
    
    public CompositeTextConfiguration() {
        this(null);
    }
    
    public CompositeTextConfiguration(String id) {
        super(TEXT_BUILDER_ID);
        setId(id);
        this.lines = new ArrayList();
    }
    
    /**
     * Adds a line of text to the composite text.
     */
    public void addLine(TextConfiguration line) {
        lines.add(line);
    }
    
    /**
     * Inserts a line of text to the composite text, at the specified index.
     */
    public void insertLine(int index, TextConfiguration line) {
        lines.add(index, line);
    }
    
    /**
     * Returns the lines of text in this composite text.
     */
    public List<TextConfiguration> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    public int getSize(){
        int result = 0;
        
        for(TextConfiguration line : getLines()){
            for(String part : line.getParts()){
                result += part.length();
            }
        }
        
        return result;
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
    
    public double getBiggestWholeLineLenght() {
        return biggestWholeLineLenght;
    }

    public void setBiggestWholeLineLenght(double biggestWholeLineLenght) {
        this.biggestWholeLineLenght = biggestWholeLineLenght;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }
}
