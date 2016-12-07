
package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.processors.Processor;
import translator.utils.Point;

public abstract class CDXMLProcessor extends Processor<CDXMLEnvironment> {
    //This constants are defined to situate positions inside a  bounding box array
    protected static final int FIRST_ELEMENT = 0;
    protected static final int SECOND_ELEMENT = 1;
    // Used in ChemDraw C++ calculations
    // equivalent to a pixel width in 1:1 scale
    protected static double ONE_PIXEL = 0.75;
    
    protected double hashSpacing;
    protected double lineWidth;
    protected double boldWidth;
    protected Color color;
    protected Color backgroundColor;
    protected Color foregroundColor;
    protected String boundingBox;
    protected int zOrder;
    
    public CDXMLProcessor() {
    }
    
    protected void configure() {
        ParsedElement element = getElement();
        if (element.hasAttribute(ParseElementDefinition.HASH_SPACING)) {
            hashSpacing = Double.parseDouble(element.getAttribute(ParseElementDefinition.HASH_SPACING));
        } else {
            hashSpacing = ((CDXMLEnvironment)element.getEnvironment()).getHashSpacing();
        }
        if (element.hasAttribute(ParseElementDefinition.LINE_WIDTH)) {
            lineWidth = Double.parseDouble(element.getAttribute(ParseElementDefinition.LINE_WIDTH));
        } else {
            lineWidth = ((CDXMLEnvironment)element.getEnvironment()).getLineWidth();
        }
        if (element.hasAttribute(ParseElementDefinition.BOLD_WIDTH)) {
            boldWidth = Double.parseDouble(element.getAttribute(ParseElementDefinition.BOLD_WIDTH));
        } else {
            boldWidth = ((CDXMLEnvironment)element.getEnvironment()).getBoldWidth();
        }
        if (element.hasAttribute(ParseElementDefinition.BOUNDING_BOX)) {
            boundingBox = element.getAttribute(ParseElementDefinition.BOUNDING_BOX);
        }
        
        zOrder = element.getZOrder();
        
        if (element.getElements(ParseElementDefinition.COLOR).size() > 0) {
            color = convertColor(element.getElements(ParseElementDefinition.COLOR).get(0));
        }
        if (environment.getBackgroundColor() != null) {
            backgroundColor = convertColor(environment.getBackgroundColor());
        }
        if (environment.getForegroundColor() != null) {
            foregroundColor = convertColor(environment.getForegroundColor());
        }
    }
    
    protected void cleanup() {
        hashSpacing = 0;
        lineWidth = 0;
        boldWidth = 0;
        color = null;
        boundingBox = null;
        zOrder = 0;
    }
    
    protected void validate(){
        
    }
    
    public static Color convertColor(ParsedElement colorElement){
        Color result = null;
        if (colorElement.hasAttribute(ParseElementDefinition.COLOR_RED) &&
                colorElement.hasAttribute(ParseElementDefinition.COLOR_GREEN) &&
                colorElement.hasAttribute(ParseElementDefinition.COLOR_BLUE)) {
            
            int red = (Integer.parseInt(colorElement.getAttribute(ParseElementDefinition.COLOR_RED)));
            int green = (Integer.parseInt(colorElement.getAttribute(ParseElementDefinition.COLOR_GREEN)));
            int blue = (Integer.parseInt(colorElement.getAttribute(ParseElementDefinition.COLOR_BLUE)));
            
            result = new Color(red, green, blue);
        }
        return result;
    }
    
    public static Point parseCoords(String pointsCoordinates, ParsedElement element) {
        double xOffset = 0;
        double yOffset = 0;
        
        if(element != null && element.hasAttribute(ParseElementDefinition.PAGE_X_OFFSET)){
            xOffset = Double.parseDouble(element.getAttribute(ParseElementDefinition.PAGE_X_OFFSET));
            yOffset = Double.parseDouble(element.getAttribute(ParseElementDefinition.PAGE_Y_OFFSET));
        }
        
        Point result;
        String[] coordinates = pointsCoordinates.split(" ");
        result = new Point(
                Double.parseDouble(coordinates[0]) + xOffset,
                Double.parseDouble(coordinates[1]) + yOffset);
        
        if(coordinates.length == 3){
            result.setZ(Double.parseDouble(coordinates[2]));
        }
        
        return result;
    }
    
    public static List<Point> parsePoints(String curvePoints, ParsedElement element) {
        double xOffset = 0;
        double yOffset = 0;
        
        if(element != null && element.hasAttribute(ParseElementDefinition.PAGE_X_OFFSET)){
            xOffset = Double.parseDouble(element.getAttribute(ParseElementDefinition.PAGE_X_OFFSET));
            yOffset = Double.parseDouble(element.getAttribute(ParseElementDefinition.PAGE_Y_OFFSET));
        }
        
        List<Point> result = new ArrayList();
        String[] coordinates = curvePoints.split(" ");
        
        for(int i = 0; i < coordinates.length; i += 2){
            result.add(new Point(
                    Double.parseDouble(coordinates[i]) + xOffset,
                    Double.parseDouble(coordinates[i + 1]) + yOffset));
        }
        
        return result;
    }
    
    /**
     * Parse a bounding box and return a rectangle
     */
    public static translator.utils.Rectangle parseBoundingBox(String pointsCoordinates, ParsedElement element){
            double xOffset = 0;
            double yOffset = 0;

            if (element != null && element.hasAttribute(ParseElementDefinition.PAGE_X_OFFSET)){
                xOffset = Double.parseDouble(element.getAttribute(ParseElementDefinition.PAGE_X_OFFSET));
                yOffset = Double.parseDouble(element.getAttribute(ParseElementDefinition.PAGE_Y_OFFSET));
            }

            translator.utils.Rectangle result;
            String[] coordinates = pointsCoordinates.split(" ");
            result = new translator.utils.Rectangle(
                    Double.parseDouble(coordinates[1]) + yOffset, Double.parseDouble(coordinates[0]) + xOffset,
                    Double.parseDouble(coordinates[3]) + yOffset, Double.parseDouble(coordinates[2]) + xOffset);

            return result;
        }
    
    /**
     * Builds bezier curves for a spline, from the given points.
     * The order of the points must be:<br>
     * <ul>
     *   <li>Start point</li>
     *   <li>Control point 1</li>
     *   <li>Control point 2</li>
     *   <li>End point</li>
     * </ul>
     */
    protected SplineConfiguration buildSplineBeziers(List<Point> splinePoints) {        
        return new SplineConfiguration(buildSegmentBeziers(splinePoints));
    }
    
    /**
     * Builds bezier curves for a segment, from the given points.
     * The order of the points must be:<br>
     * <ul>
     *   <li>Start point</li>
     *   <li>Control point 1</li>
     *   <li>Control point 2</li>
     *   <li>End point</li>
     * </ul>
     */
    protected List<SegmentConfiguration> buildSegmentBeziers(List<Point> segmentsPoints){
        List<SegmentConfiguration> segments = new ArrayList();
        for (int i = 1; i + 3 < segmentsPoints.size() - 1; i += 3) {
            Point controlPoint1 = segmentsPoints.get(i);
            Point controlPoint2 = segmentsPoints.get(i + 1);
            Point controlPoint3 = segmentsPoints.get(i + 2);
            Point controlPoint4 = segmentsPoints.get(i + 3);
            CubicCurveConfiguration splineCurve = new CubicCurveConfiguration(
                    controlPoint1, controlPoint4, controlPoint2, controlPoint3);
            segments.add(splineCurve);
        }
        
        return segments;
    }
    
    public double getHashSpacing() {
        return hashSpacing;
    }
    
    public void setHashSpacing(double hashSpacing) {
        this.hashSpacing = hashSpacing;
    }
    
    public double getLineWidth() {
        return lineWidth;
    }
    
    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }
    
    public double getBoldWidth() {
        return boldWidth;
    }
    
    public void setBoldWidth(double boldWidth) {
        this.boldWidth = boldWidth;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public String getBoundingBox() {
        return boundingBox;
    }
    
    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public int getZOrder() {
        return zOrder;
    }
    
    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }
}
