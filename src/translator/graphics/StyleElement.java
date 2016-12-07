
package translator.graphics;

import java.util.Arrays;
import translator.ParseElementDefinition;
import translator.ParsedElement;

/**
 * Encapsulates the representation and handling of font styles.
 * Allows creation from a <code>Font</code> object and querying 
 * for attributes through boolean-return methods.
 */
public class StyleElement {
    
    private boolean[] styles;
    private String face;
    
    public StyleElement(String face) {
        this.face = face;
        initializeStyles(getStyles(face));
    }
    
    private void initializeStyles(int[] fontStyles) {
        this.styles = new boolean[9];
        
        for (int i = 0; i < fontStyles.length; i++) {
            this.styles[fontStyles[i]] = true;
        }
    }
    
    private static String calculateFace(boolean[] styles) {
        int result = 0;
        for (int i = 1; i < styles.length; i++) {
            if (styles[i]) {
                if (i == styles.length - 1) {
                    // formula is subscript + superscript
                    result += (int) Math.pow(2, Font.FORMULA1 - 1);
                    result += (int) Math.pow(2, Font.FORMULA2 - 1);
                } else {
                    result += Math.pow(2, i - 1);
                }
            }
        }
        return String.valueOf(result);
    }
    
    public static String getStylesString(int[] styles) {
        int styleValue = 0;
        for (int i = 0; i < styles.length; i++) {
            if (styles[i] != Font.PLAIN) {
                if (styles[i] != Font.FORMULA) {
                    styleValue += (int) Math.pow(2, styles[i] - 1);
                } else {
                    styleValue += (int) Math.pow(2, Font.FORMULA1 - 1);
                    styleValue += (int) Math.pow(2, Font.FORMULA2 - 1);
                }
            }
        }
        return String.valueOf(styleValue);
    }
    
    public static int[] getStyles(String style){
        char[] binaryStyle = Integer.toBinaryString(Integer.parseInt(style)).toCharArray();
        
        int[] styles = new int[0];
        int[] aux = new int[0];
        
        if(binaryStyle.length >= Font.BOLD && binaryStyle[binaryStyle.length - Font.BOLD] == '1'){
            styles = addStyle(styles, Font.BOLD);
        }
        if(binaryStyle.length >= Font.ITALIC && binaryStyle[binaryStyle.length - Font.ITALIC] == '1'){
            styles = addStyle(styles, Font.ITALIC);
        }
        if(binaryStyle.length >= Font.UNDERLINED && binaryStyle[binaryStyle.length - Font.UNDERLINED] == '1'){
            styles = addStyle(styles, Font.UNDERLINED);
        }
        if(binaryStyle.length >= Font.OUTLINED && binaryStyle[binaryStyle.length - Font.OUTLINED] == '1'){
            styles = addStyle(styles, Font.OUTLINED);
        }
        if(binaryStyle.length >= Font.SHADOWED && binaryStyle[binaryStyle.length - Font.SHADOWED] == '1'){
            styles = addStyle(styles, Font.SHADOWED);
        }
        if(binaryStyle.length >= Font.FORMULA2 &&
                binaryStyle[binaryStyle.length - Font.FORMULA1] == '1' &&
                binaryStyle[binaryStyle.length - Font.FORMULA2] == '1'){
            styles = addStyle(styles, Font.FORMULA);
        } else if(binaryStyle.length >= Font.SUBSCRIPT && binaryStyle[binaryStyle.length - Font.SUBSCRIPT] == '1'){
            styles = addStyle(styles, Font.SUBSCRIPT);
        } else if(binaryStyle.length >= Font.SUPERSCRIPT && binaryStyle[binaryStyle.length - Font.SUPERSCRIPT] == '1'){
            styles = addStyle(styles, Font.SUPERSCRIPT);
        }
        
        return styles;
    }
    
    private static int[] addStyle(int[] styles, int style){
        int[] aux = new int[styles.length];
        System.arraycopy(styles, 0, aux, 0, styles.length);
        
        styles = new int[styles.length + 1];
        System.arraycopy(aux, 0, styles, 0, aux.length);
        styles[styles.length - 1] = style;
        
        return styles;
    }
    
    public StyleElement(int[] fontStyles) {
        this.face = getStylesString(fontStyles);
        initializeStyles(fontStyles);
    }
    
    private StyleElement(StyleElement otherStyle) {
        this.styles = otherStyle.styles.clone();
        this.face = otherStyle.face;
    }
    
    public StyleElement derivePlain() {
        return derive(Font.PLAIN);
    }
    
    public StyleElement deriveBold() {
        return derive(Font.BOLD);
    }
    
    public StyleElement deriveItalic() {
        return derive(Font.ITALIC);
    }
    
    public StyleElement deriveUnderlined() {
        return derive(Font.UNDERLINED);
    }
    
    public StyleElement deriveOutlined() {
        return derive(Font.OUTLINED);
    }
    
    public StyleElement deriveShadowed() {
        return derive(Font.SHADOWED);
    }
    
    public StyleElement deriveSubscript() {
        return derive(Font.SUBSCRIPT);
    }
    
    public StyleElement deriveSuperscript() {
        return derive(Font.SUPERSCRIPT);
    }
    
    public StyleElement deriveFormula() {
        return derive(Font.FORMULA);
    }
    
    /**
     * Derives a new style from the current style, 
     * setting or resetting the specified style
     */
    private StyleElement derive(int style) {
        // create a copy
        StyleElement result = new StyleElement(this);
        
        // switch the specified style
        result.styles[style] = !result.styles[style];
        
        // tells whether the style was set or reset
        boolean valueWasSet = result.styles[style];
        
        if (valueWasSet) {
            // control logically impossible combinations
            if (style == Font.PLAIN) {
                for (int s = Font.BOLD; s <= Font.SHADOWED; s++) {
                    result.styles[s] = false;
                }
            } else if (style == Font.BOLD || style == Font.ITALIC || style == Font.UNDERLINED ||
                    style == Font.OUTLINED || style == Font.SHADOWED){
                result.styles[Font.PLAIN] = false;
            } else if (style == Font.SUBSCRIPT || style == Font.SUPERSCRIPT || style == Font.FORMULA) {
                for (int s = Font.SUBSCRIPT; s <= Font.FORMULA; s++) {
                    if (s != style) {
                        result.styles[s] = false;
                    }
                }
            }
        }
        
        result.face = calculateFace(result.styles);
        
        return result;
    }
    
    public boolean equals(Object other) {
        StyleElement otherStyle = (StyleElement) other;
        return Arrays.equals(styles, otherStyle.styles);
    }
    
    public boolean isPlain() {
        boolean result = styles[Font.PLAIN];
        if (!result) {
            // consider PLAIN style if no other styles are set
            result = true;
            for (int s = Font.BOLD; s <= Font.SHADOWED; s++) {
                if (styles[s]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
    
    public boolean isBold() {
        return styles[Font.BOLD];
    }
    
    public boolean isItalic() {
        return styles[Font.ITALIC];
    }
    
    public boolean isUnderlined() {
        return styles[Font.UNDERLINED];
    }
    
    public boolean isOutlined() {
        return styles[Font.OUTLINED];
    }
    
    public boolean isShadowed() {
        return styles[Font.SHADOWED];
    }
    
    public boolean isSubscript() {
        return styles[Font.SUBSCRIPT];
    }
    
    public boolean isSuperscript() {
        return styles[Font.SUPERSCRIPT];
    }
    
    public boolean isFormula() {
        return styles[Font.FORMULA];
    }

    public String getFace() {
        return face;
    }
}
