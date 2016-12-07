package translator.graphics.shapes;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import translator.Environment;
import translator.cddom.properties.BoundingBox;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.utils.Point;

public class Text extends Shape {
    
    public static int LEFT_JUSTIFICATION = 0;
    public static int CENTER_JUSTIFICATION = 1;
    public static int RIGHT_JUSTIFICATION = 2;
    public static int FULL_JUSTIFICATION = 3;
    
    public static int LEFT_ALIGNMENT = 0;
    public static int CENTER_ALIGNMENT = 1;
    public static int RIGHT_ALIGNMENT = 2;
    
    private int justification = LEFT_JUSTIFICATION;
    private int alignment = LEFT_ALIGNMENT;
    
     //This value indicates inter-word space in addition to the default space between words.
    private double wordSpacing;
    
    private List<String> parts = new ArrayList();
    private Hashtable<String, Font> fonts = new Hashtable();
    private Hashtable<String, Color> colors = new Hashtable();
    
    private int rotationAngle;
           
           

    public Text() {
    }
    
    public void addPart(String value, Font font, Color color){
        parts.add(value);
        
        // parts ids are their indexes in the list
        String partId = String.valueOf(parts.size() - 1);
        fonts.put(partId, font);
        colors.put(partId, color);
    }
    
    public Point getPoint(){
        return getPoint(0);
    }
    
    public void setPoint(Point point) {
        addPoint(point, 0);
    }

    public List<String> getValues(){
        return parts;
    }
    
    public Font getFont(String partId){
        return fonts.get(partId);
    }
    
    public Color getColor(String partId){
        return colors.get(partId);
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }
    
    public double getWordSpacing() {
        return wordSpacing;
    }

    public void setwordSpacing(double wordSpacing) {
        this.wordSpacing = wordSpacing;
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

    public BoundingBox getBoundingBox() {
        return null;
    }

}
