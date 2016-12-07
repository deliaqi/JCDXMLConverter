package translator.graphics;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import translator.ParseElementDefinition;
import translator.ParsedElement;

public class Font {
    
    private String name;
    private String charSet;
    private String size;
    private StyleElement style;
    
    private double ascentPixel;                 // ascent converted to pixels
    private double descentPixel;                // descent converted to pixels
    private double lineSpacingPixel;            // line spacing converted to pixels
    private double leadingPixel;                // leading converted to pixels
    private double lineHeightNormal;
    private double lineHeightSubSuperscript;    // line height calculated for subscripts and superscripts converted to pixels
    
    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINED = 3;
    public static final int OUTLINED = 4;
    public static final int SHADOWED = 5;
    public static final int SUBSCRIPT = 6;
    public static final int SUPERSCRIPT = 7;
    public static final int FORMULA1 = 6;
    public static final int FORMULA2 = 7;
    public static final int FORMULA = 8;
    public final static double MAX_FONT_SIZE = 1000.0;
    public final static double MIN_FONT_SIZE = 0.0;
    public final static String DEFAULT_FONT_SIZE = "10.0";
    private final static double FONT_SIZE_PERCENT_FOR_SUB_OR_SUPERSCRIPT = 0.75;
    
    public static final double DEFAULT_VALUE_CAP_HEIGHT = 5.8; //The default font family for atom labels
    public static final double DEFAULT_VALUE_DESCENT = 1.56; //The default font family for atom labels
    public static final double SLOP = 0.75;
    
    public static double BASE_LINE_SUBSCRIPT_PERCENT = 0.25;
    public static double BASE_LINE_SUPERSCRIPT_PERCENT = 0.45;
    
    public Font() {
    }
    
    public static Font createFromElement(ParsedElement stringElement, FontRenderContext renderContext) {
        Font result = new Font();
        String name = stringElement.getAttribute(ParseElementDefinition.STRING_FONT);
        String face = stringElement.getAttribute(ParseElementDefinition.STRING_FACE);
        String charset = stringElement.getAttribute(ParseElementDefinition.STRING_CHAR_SET);
        String size = stringElement.getAttribute(ParseElementDefinition.STRING_SIZE);
        
        result.setName(name);
        result.setCharSet(charset);
        result.setSize(size);
        result.setStyle(new StyleElement(face));
        
        int platformStyle = java.awt.Font.PLAIN;
        if(result.getStyle().isBold()){
            platformStyle = java.awt.Font.BOLD;
        }
        if(result.getStyle().isItalic()){
            platformStyle = java.awt.Font.ITALIC;
        }
        
        java.awt.Font platformFont = new java.awt.Font(name, platformStyle, (int) Float.parseFloat(size));
        //We need to derive the font because when instanciating it, its constructor does not accept float sizes.
        platformFont = platformFont.deriveFont(Float.parseFloat(size));
        
        TextLayout textLayout;
        
        if(stringElement.getValue().isEmpty()){
            textLayout = new TextLayout(" ", platformFont, renderContext);
        }else{
            textLayout = new TextLayout(stringElement.getValue(), platformFont, renderContext);
        }
        
        //Calculate text metrics and store into result.
        result.setAscentPixel(calculateAscentPixel(textLayout));
        result.setDescentPixel(calculateDescentPixel(textLayout));
        result.setLineSpacingPixel(calculateLineSpacingPixel(textLayout));
        result.setLeadingPixel(calculateLeadingPixel(textLayout));
        result.setLineHeightNormal(calculateLineHeight(textLayout));
        
        if (result.getStyle().isSuperscript() || result.getStyle().isSubscript()){
            //Here, we need to calculate the line height for this resized text.
            //Remember we reduce the text size to get a sub/superscript.
            float newSize = Float.parseFloat(size) * (float)FONT_SIZE_PERCENT_FOR_SUB_OR_SUPERSCRIPT;
            platformFont = platformFont.deriveFont(newSize);
            textLayout = new TextLayout(stringElement.getValue(), platformFont, renderContext);
            result.setLineHeightSubSuperscript(calculateLineHeight(textLayout));
        }
        
        return result;
    }
    
    /*
     * This method calculates the line height based on the size and the font family for a given text line.
     */
    private static double calculateLineHeight(TextLayout textLayout) {
        double lineHeight =  Math.round(textLayout.getLeading()) + Math.round(textLayout.getAscent());
        
        double ascentPixel = calculateAscentPixel(textLayout);
        double descentPixel = calculateDescentPixel(textLayout);
        double lineSpacingPixel = calculateLineSpacingPixel(textLayout);
        double leading = calculateLeadingPixel(textLayout);
        
        lineHeight = ascentPixel + descentPixel - (leading * 3) ;
        return lineHeight;
    }
    
    
    public static double calculateAscentPixel(TextLayout textLayout) {
        // Ascent value in pixels
        //Following line differs from C# code line. Ascent calculation differs between both platforms
        //but we get the same result.
        double ascentPixel = textLayout.getAscent();
        return ascentPixel;
    }
    
    public static double calculateDescentPixel(TextLayout textLayout) {
        // Descent value in pixels
        //Following line differs from C# code line. Descent calculation differs between both platforms
        //but we get the same result.
        double descentPixel = textLayout.getDescent();
        return descentPixel;
    }
    
    public static double calculateLineSpacingPixel(TextLayout textLayout) {
        //Line Spacing value in pixels
        //Following line differs from C# code line. Descent calculation differs between both platforms
        //but we get the same result.
        double lineSpacingPixel = textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
        return lineSpacingPixel;
    }
    
    public static double calculateLeadingPixel(TextLayout textLayout) {
        // Leading value in pixels
        double leadingPixel = calculateLineSpacingPixel(textLayout) 
        - calculateAscentPixel(textLayout) - calculateDescentPixel(textLayout);
        
        return leadingPixel;
    }
    
    public boolean equals(Object other) {
        Font otherFont = (Font) other;
        return (name.equals(otherFont.name) &&
                charSet.equals(otherFont.charSet) &&
                size.equals(otherFont.size));
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCharSet() {
        return charSet;
    }
    
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public StyleElement getStyle() {
        return style;
    }
    
    public void setStyle(StyleElement style) {
        this.style = style;
    }
    
    public double getAscentPixel() {
        return ascentPixel;
    }
    
    public void setAscentPixel(double ascentPixel) {
        this.ascentPixel = ascentPixel;
    }
    
    public double getDescentPixel() {
        return descentPixel;
    }
    
    public void setDescentPixel(double descentPixel) {
        this.descentPixel = descentPixel;
    }
    
    public double getLeadingPixel() {
        return leadingPixel;
    }
    
    public void setLeadingPixel(double leadingPixel) {
        this.leadingPixel = leadingPixel;
    }
    
    public double getLineSpacingPixel() {
        return lineSpacingPixel;
    }
    
    public void setLineSpacingPixel(double lineSpacingPixel) {
        this.lineSpacingPixel = lineSpacingPixel;
    }
    
    public double getLineHeightNormal() {
        return lineHeightNormal;
    }
    
    public void setLineHeightNormal(double lineHeightNormal) {
        this.lineHeightNormal = lineHeightNormal;
    }
    
    public double getLineHeightSubSuperscript() {
        return lineHeightSubSuperscript;
    }
    
    public void setLineHeightSubSuperscript(double lineHeightSubSuperscript) {
        this.lineHeightSubSuperscript = lineHeightSubSuperscript;
    }
    
}
