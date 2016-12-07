package translator.graphics.shapes.builders.configurations;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import translator.BuilderConfiguration;
import translator.graphics.Color;
import translator.graphics.Font;

/**
 * Represents a line of text, inside a composite text.<br>
 * Instances of this class must be added to a <code>CompositeTextConfiguration</code>.
 * A line contains parts, which are runs of text with the same style attributes.
 * Parts must be added through the <code>addPart()</code> method, specifying the
 * string, font and color.
 * This class also specifies the height and position of the line.
 */
public class TextConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration{
    
    public static int LEFT_JUSTIFICATION = 0;
    public static int CENTER_JUSTIFICATION = 1;
    public static int RIGHT_JUSTIFICATION = 2;
    public static int FULL_JUSTIFICATION = 3;
    
    public static int LEFT_ALIGNMENT = 0;
    public static int CENTER_ALIGNMENT = 1;
    public static int RIGHT_ALIGNMENT = 2;
    public static int ABOVE_ALIGNMENT = 3;
    public static int BELOW_ALIGNMENT = 4;
    
    private List<String> parts = new ArrayList();
    private Hashtable<String, Font> fonts = new Hashtable();
    private Hashtable<String, Color> colors = new Hashtable();
    
    private double x;
    private double y;
    
    //This value is calculated when the Justification = Full (Alignment = Justified).
    //It indicates inter-word space in addition to the default space between words.
    private double wordSpacing = 0;
    //This value indicates the line lenght (without being justified in case of Alingment equals to Full)
    private double wholeLineLenght = 0;
    //This value indicates the white spaces amount (without the last white space in case the current line ends with one)
    private double whiteSpacesCount = 0;
    
    public TextConfiguration(double x, double y, int size) {
        super(TEXT_BUILDER_ID);
        this.x = x;
        this.y = y;
    }
    
    /**
     * Adds a part to the line of text, specifying the string, and the font and color
     * attributes to apply. The part is added at the end of the parts list.
     */
    public void addPart(String part, Font partFont, Color partColor){
       //When the part brings this kind off string "" (this is not a white space), it is not added to the list.
        if(part.length() > 0){
            parts.add(part);
            String id = String.valueOf(parts.size() - 1);
            fonts.put(id, partFont);
            colors.put(id, partColor);
        }
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getWordSpacing() {
        return wordSpacing;
    }
    
    public void setWordSpacing(double wordSpacing) {
        this.wordSpacing = wordSpacing;
    }
    
    public double getWholeLineLenght() {
        return wholeLineLenght;
    }
    
    public void setWholeLineLenght(double wholeLineLenght) {
        this.wholeLineLenght = wholeLineLenght;
    }
    
    public double getWhiteSpacesCount() {
        return whiteSpacesCount;
    }
    
    public void setWhiteSpacesCount(double whiteSpacesCount) {
        this.whiteSpacesCount = whiteSpacesCount;
    }
    
    public List<String> getParts(){
        return parts;
    }
    
    public Font getFont(String id){
        return fonts.get(id);
    }
    
    public Color getColor(String id){
        return colors.get(id);
    }
}
