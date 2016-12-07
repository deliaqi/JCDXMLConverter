package svgrenderer.shapes.converters;

import java.text.NumberFormat;

/**
 *
 * Class that format the svg output
 */
public class SvgFormatting {
    private static final int COORDINATE_PRECISION = 2;
    
    /** Creates a new instance of SvgFormatting */
    public SvgFormatting() {
    }
    
    /**
     * Transform the double coordinate into a formatted string coordinate
     */
    public static String formatCoordinate(double coordinate){
        String result;
        NumberFormat svgDecimalFormat = NumberFormat.getInstance();
        
        svgDecimalFormat.setMaximumFractionDigits(COORDINATE_PRECISION);
        result = svgDecimalFormat.format(coordinate);
        
        return result;
    }
    
}
