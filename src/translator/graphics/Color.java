package translator.graphics;

public class Color {
    
    private static final int FADE_LIMIT = 0xFFFF;
    
    private int red;
    private int green;
    private int blue;
    private int alpha;
    
    public Color(int red, int green, int blue) {
        this(red, green, blue, 1);
    }
    
    public Color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }
    
    // Extracted from C++ code
    // Fades RGB component
    private static int fade(int color, int percent) {
        int result = color;
        result = 255 - result;
        result = result * percent / 100;
        result = 255 - result;
        
        return result & FADE_LIMIT;
    }
    
    // Extracted from C++ code
    // Fades a color
    public static Color fadeRGB(Color originalColor, int percent) {
        Color result = originalColor;
        if (result.getRed() == 255 && result.getGreen() == 255 && result.getBlue() == 255){
            result = new Color(0,0,0);
        }
        result = new Color(fade(result.getRed(), percent), fade(result.getGreen(), percent), fade(result.getBlue(), percent));
        return result;
    }
    
    public boolean equals(Object other) {
        Color otherColor = (Color) other;
        return (red == otherColor.red &&
                green == otherColor.green &&
                blue == otherColor.blue &&
                alpha == otherColor.alpha);
    }
    
    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    
}
