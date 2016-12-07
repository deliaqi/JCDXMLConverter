/*
 * GraphicAttirbutesProcessor.java
 *
 * Created on November 7, 2007, 10:04 AM
 *
 * This class is used to set the drawing attributes for different objects
 * 
 */

package translator.processors.cdxml;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.CurveType;
/**
 *
 * @author pperinetti
 */
public abstract class DrawingAttributesProcessor extends CDXMLProcessor{
    
    /** Creates a new instance of GraphicAttirbutesProcessor */
    public DrawingAttributesProcessor() {
    }
    
    private static String MAX_FADE_PERCENT = "1000";
    
    //Variables to indicate if the attribute must e applyed to the object
    protected boolean bold;
    protected boolean dashed;
    protected boolean faded;
    protected boolean filled;
    protected boolean shaded;
    protected boolean shadowed;
    protected double width;
    protected int curveType;
    protected boolean penDouble;    
    protected double curveSpacing;      
    protected boolean closed;
    
    //Interpretation of the curve type
    protected int[] curveStyle;
    
    protected void cleanup() {
        bold = false;
        dashed = false;
        faded = false;
        filled = false;
        shaded = false;
        shadowed = false;       
        curveSpacing = 0;
        curveStyle = null;
        closed = false;
        penDouble = false;
        curveType = 0;
        width = 0;
        super.cleanup();
    }
    //Every object that extends from this class needs to implements its own typeString
    protected abstract String getTypeString();
    
    protected void configure() {
        super.configure();
        if (getElement().hasAttribute(ParseElementDefinition.SPLINE_CURVE_TYPE)) {
            curveType = Integer.parseInt(getElement().getAttribute(ParseElementDefinition.SPLINE_CURVE_TYPE));
        }
        
        //Interprete the curve type
        curveStyle = getCurveStyle(curveType);
        setAttributesFromCurveType();
    }
    
    /**
     * Interprete the curve type and set the attributes
     */
    protected void setAttributesFromCurveType(){
        
        if((curveStyle.length > CurveType.Filled.ordinal() && curveStyle[CurveType.Filled.ordinal()] == 1)){
            setFilled(true);
        }
        
        if(curveStyle.length > CurveType.Shaded.ordinal() && curveStyle[CurveType.Shaded.ordinal()] == 1){
            setShaded(true);
        }
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT)
        && !getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT).equals(MAX_FADE_PERCENT)){
            setFilled(true);
        }
        if((curveStyle.length > CurveType.Closed.ordinal() && curveStyle[CurveType.Closed.ordinal()] == 1)){
            closed = true;
        }
        if((curveStyle.length > CurveType.Dashed.ordinal() && curveStyle[CurveType.Dashed.ordinal()] == 1)){
            setDashed(true);
        }
        if((curveStyle.length > CurveType.Doubled.ordinal() && curveStyle[CurveType.Doubled.ordinal()] == 1)){
            penDouble =  true;
            if(!getElement().hasAttribute(ParseElementDefinition.SPLINE_CURVE_SPACING)){
                //The bold width is used when the spline doesn't specify curve spacing.
                curveSpacing = getBoldWidth();
            }
        }
        if((curveStyle.length > CurveType.Bold.ordinal() && curveStyle[CurveType.Bold.ordinal()] == 1)){
            setBold(true);
            setWidth(getBoldWidth());
        }
    }
    
    protected void  setAttributesForDrawingElement(){
        ParsedElement element = getElement();
        
        bold = false;
        dashed = false;
        faded = false;
        filled = false;
        shaded = false;
        shadowed = false;
        width = getLineWidth();
        
        String type = getTypeString();
        
        if (type != null){                      
            if (type.contains(ParseElementDefinition.TYPE_BOLD)){            
                bold = true;
                width = getBoldWidth();
            }

            if (type.contains(ParseElementDefinition.TYPE_DASHED)){
                dashed = true;
            }

             if(type.contains(ParseElementDefinition.TYPE_SHADOW)){
                shadowed = true;
            }
            
            if(type.contains(ParseElementDefinition.TYPE_FILLED) || type.contains(ParseElementDefinition.TYPE_SOLID)){
                filled = true;
            }
            
            if (type.contains(ParseElementDefinition.TYPE_SHADED)) {
                shaded = true;
            }
            
        }
        
        if (element.hasAttribute(ParseElementDefinition.SPLINE_FADE_PERCENT)){
            int  fadePercent = Integer.parseInt(element.getAttribute(ParseElementDefinition.SPLINE_FADE_PERCENT)) / 10;
            if (fadePercent < 100)
                faded = true;
        }
           
    }
    
    
    /**
     *Interpret the value of the curveType and return an int array
     *that describe the attributes for the spline
     */
    public int[] getCurveStyle(int curveType){
        char[] binaryStyle = Integer.toBinaryString(curveType).toCharArray();
        
        int[] curveStyles = new int[0];
        int[] aux = new int[0];
        int count = binaryStyle.length - 1;
        
        if(binaryStyle.length > CurveType.Closed.ordinal() && binaryStyle[count - CurveType.Closed.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.Closed.ordinal());
        }
        if(binaryStyle.length > CurveType.Dashed.ordinal() && binaryStyle[count - CurveType.Dashed.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.Dashed.ordinal());
        }
        if(binaryStyle.length > CurveType.Bold.ordinal() && binaryStyle[count - CurveType.Bold.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.Bold.ordinal());
        }
        if(binaryStyle.length > CurveType.ArrowAtEnd.ordinal() && binaryStyle[count - CurveType.ArrowAtEnd.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.ArrowAtEnd.ordinal());
        }
        if(binaryStyle.length > CurveType.ArrowAtStart.ordinal() && binaryStyle[count - CurveType.ArrowAtStart.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.ArrowAtStart.ordinal());
        }
        if(binaryStyle.length > CurveType.HalfArrowAtEnd.ordinal() && binaryStyle[count - CurveType.HalfArrowAtEnd.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.HalfArrowAtEnd.ordinal());
        }
        if(binaryStyle.length > CurveType.HalfArrowAtStart.ordinal() && binaryStyle[count - CurveType.HalfArrowAtStart.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.HalfArrowAtStart.ordinal());
        }
        if(binaryStyle.length > CurveType.Filled.ordinal() && binaryStyle[count - CurveType.Filled.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.Filled.ordinal());
        }
        if(binaryStyle.length > CurveType.Shaded.ordinal() && binaryStyle[count - CurveType.Shaded.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.Shaded.ordinal());
        }
        if(binaryStyle.length > CurveType.Doubled.ordinal() && binaryStyle[count - CurveType.Doubled.ordinal()] == '1'){
            curveStyles = addCurveStyle(curveStyles, CurveType.Doubled.ordinal());
        }
        
        return curveStyles;
    }
    
    /**
     *Add to the style array the attributes that curveType contains
     */
    private int[] addCurveStyle(int[] styles, int style){
        int[] aux = new int[style + 1];
        System.arraycopy(styles, 0, aux, 0, styles.length);
        
        styles = new int[style + 1];
        System.arraycopy(aux, 0, styles, 0, aux.length);
        styles[style] = 1;
        
        return styles;
    }
    
    public boolean isBold() {
        return bold;
    }
    
    public void setBold(boolean newValue) {
        bold = newValue;
    }
    
    public boolean isDashed() {
        return dashed;
    }
    
    public void setDashed(boolean newValue) {
        dashed = newValue;
    }
    
    public boolean isFaded() {
        return faded;
    }
    
    public void setFaded(boolean newValue) {
        faded = newValue;
    }
    
    public boolean isFilled() {
        return filled;
    }
    
    public void setFilled(boolean newValue) {
        filled = newValue;
    }
    
    public boolean isShaded() {
        return shaded;
    }
    
    public void setShaded(boolean newValue) {
        shaded = newValue;
    }
    
    public boolean isShadowed() {
        return shadowed;
    }
    
    public void setShadowed(boolean newValue) {
        shadowed = newValue;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double newValue) {
        width = newValue;
    }
}
