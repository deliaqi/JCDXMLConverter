package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.ArrowHeadConfiguration;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.Color;
import translator.graphics.CurveType;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.AlgebraicOperations;
import translator.utils.GeometricOperations;
import translator.utils.IntersectionPoint;
import translator.utils.Line;
import translator.utils.Grid;
import translator.utils.Point;

public class SplineProcessor extends DrawingAttributesProcessor{
    
    //taken from C++ code
    private static final double ARROW_HEAD_HALF_WIDTH = 5.0/16.0;
    
    private static final String ARROW_HEAD_FULL = "Full";
    private static final String ARROW_HEAD_HALF_RIGHT = "HalfRight";
    private static final String ARROW_HEAD_HALF_LEFT = "HalfLeft";        
        
    private static final double FILLED_STROKE_WIDTH = 0.5;
            
    private static final int ARROWHEAD_AT_END = 0;
    private static final int ARROWHEAD_AT_BEGIN = 1;
    
    protected Color fadedColor;
    private List<Point> resultPoints;
    protected int fadePercent;
    protected String lineType;
    protected String fillType;
    protected String arrowheadHead;
    protected String arrowheadTail;
    
    protected double dashLength;    
    
    public SplineProcessor(){
    }
    
    protected void cleanup() {
        fadedColor = null;
        fadePercent = 0;        
        resultPoints = null;
        lineType = null;
        fillType = null;
        dashLength = 0;
        super.cleanup();
    }
    
    protected void configure() {
        super.configure();
        ParsedElement spline = getElement();
        
        if (spline.hasAttribute(ParseElementDefinition.SPLINE_LINE_TYPE)) {
            lineType = spline.getAttribute(ParseElementDefinition.SPLINE_LINE_TYPE);
        }
        if (spline.hasAttribute(ParseElementDefinition.SPLINE_FILL_TYPE)) {
            fillType = spline.getAttribute(ParseElementDefinition.SPLINE_FILL_TYPE);
        }
        
        super.setAttributesForDrawingElement();
        
        if (isFaded()) {
            fadePercent = Integer.parseInt(spline.getAttribute(ParseElementDefinition.SPLINE_FADE_PERCENT)) / 10;
            if(color != null){
                fadedColor = Color.fadeRGB(color, fadePercent);
            }else{
                fadedColor = Color.fadeRGB(convertColor(getEnvironment().getForegroundColor()), fadePercent);
            }
        }
        
        if (spline.hasAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_HEAD)) {
            arrowheadHead = spline.getAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_HEAD);
        }else{
            arrowheadHead = "";
        }
        
        if (spline.hasAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_TAIL)) {
            arrowheadTail = spline.getAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_TAIL);
        }else{
            arrowheadTail = "";
        }
        dashLength = hashSpacing;
    }
    
    protected void process() {
        ParsedElement spline = getElement();
        
        ShapeBuilderConfiguration resultingConfiguration = null;
        SplineConfiguration middleConfiguration = null;
        SplineConfiguration middleDoubleConfiguration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        
        if (spline.hasAttribute(ParseElementDefinition.SPLINE_CURVE_POINTS)) {
            resultPoints = parsePoints(spline.getAttribute(ParseElementDefinition.SPLINE_CURVE_POINTS), spline);
            double fillWidth = 0;
            penDouble = false;
            
            if (spline.hasAttribute(ParseElementDefinition.SPLINE_LINE_TYPE)) {
                if(isBold()){
                    fillWidth = getBoldWidth();
                }else{
                    fillWidth = getLineWidth();
                }
            }else{
                fillWidth = FILLED_STROKE_WIDTH;
            }
            
            if (spline.hasAttribute(ParseElementDefinition.SPLINE_CURVE_SPACING)) {
                curveSpacing = Double.parseDouble(spline.getAttribute(ParseElementDefinition.SPLINE_CURVE_SPACING)) / 100;                
                penDouble = true;
            }
            
            if (spline.hasAttribute(ParseElementDefinition.SPLINE_CLOSED)) {
                if(spline.getAttribute(ParseElementDefinition.SPLINE_CLOSED).equalsIgnoreCase(ParseElementDefinition.SPLINE_CLOSED_YES)){
                    closed = true;
                }else{
                    closed = false;
                }
            } else {
                closed = false;
            }
            
            boolean hasFillType = spline.hasAttribute(ParseElementDefinition.SPLINE_FILL_TYPE);
            
            //if the cdxml doesn't have the fill type attribute or has fill
            //type unspecified use the curve type interpretation
            if(!hasFillType || (hasFillType
                    && spline.getAttribute(ParseElementDefinition.SPLINE_FILL_TYPE)
                    .equals(ParseElementDefinition.SPLINE_FILL_TYPE_UNSPECIFIED))){
                
                setAttributesFromCurveType();
            }
            
            //This variable is using to represent the spline
            List<SegmentConfiguration> curves = new ArrayList();
            
            //Create the bezier curves to represented the splines
            for(int i = 1; i < (resultPoints.size() - 3); i += 3){                
                CubicCurveConfiguration curve = new CubicCurveConfiguration(resultPoints.get(i), resultPoints.get(i+3),
                        resultPoints.get(i+1), resultPoints.get(i+2));
                curves.add(curve);
            }
            
            //if its closed add the latest bezier curve
            if(closed){
                CubicCurveConfiguration curve = new CubicCurveConfiguration(
                        resultPoints.get(resultPoints.size()-2), resultPoints.get(1),
                        resultPoints.get(resultPoints.size()-1), resultPoints.get(0));
                curves.add(curve);
            }
            
            //Create the configuration object
            if(penDouble){
                //In this case we are creating two configuration, 
                //one to the left curve and other to the right curve
                middleDoubleConfiguration = new SplineConfiguration(curves);
                middleConfiguration = new SplineConfiguration(curves);
            } else{
                //In this case we are creating only one configuration
                middleConfiguration = new SplineConfiguration(curves);
            }
            
            if (isFaded()) {
                fadePercent = Integer.parseInt(spline.getAttribute(ParseElementDefinition.SPLINE_FADE_PERCENT)) / 10;
                fadedColor = Color.fadeRGB(color, fadePercent);
            }
            
            if(penDouble){
                double doubleWidth = getWidth() * 2;                
                
                doubleWidth +=getBoldWidth();
                
                //Create the back configuration with the color of the element
                middleDoubleConfiguration.setStrokeWidth(doubleWidth);
                middleDoubleConfiguration.setColor(getColor());
                
                //Create the inner configuration with the color of the background
                middleConfiguration.setStrokeWidth(getBoldWidth());
                middleConfiguration.setColor(getBackgroundColor());
                middleConfiguration.setLineCap(LineCap.Round);
                
                SplineConfiguration fadedDoubleConfiguration = null;
                SplineConfiguration shadedDoubleConfiguration = null;
                SplineConfiguration filledDoubleConfiguration = null;
                
                if(isFaded()){
                    fadedDoubleConfiguration = new SplineConfiguration(curves);
                    fadedDoubleConfiguration.setFill(true);
                    fadedDoubleConfiguration.setFillColor(fadedColor);
                }else if(isFilled()){
                    filledDoubleConfiguration = new SplineConfiguration(curves);
                    filledDoubleConfiguration.setFill(true);
                    filledDoubleConfiguration.setStrokeWidth(FILLED_STROKE_WIDTH);
                    filledDoubleConfiguration.setFillColor(color);
                }else if(isShaded()){
                    shadedDoubleConfiguration = new SplineConfiguration(curves);                    
                    shadedDoubleConfiguration.setShaded(true);
                    shadedDoubleConfiguration.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                }              
                
                if(closed){
                    middleDoubleConfiguration.setClosed(true);
                    middleDoubleConfiguration.setLineJoin(LineJoin.Bevel);
                }
                
                if(fadedDoubleConfiguration != null){
                    innerShapes.add(fadedDoubleConfiguration);
                }else if(shadedDoubleConfiguration != null){
                    innerShapes.add(shadedDoubleConfiguration);
                }else if(filledDoubleConfiguration != null){
                    innerShapes.add(filledDoubleConfiguration);
                }
                
                innerShapes.add(middleDoubleConfiguration);
                innerShapes.add(middleConfiguration);                
                
            }else{
                if(isDashed()){
                    middleConfiguration.setDashed(true);
                    middleConfiguration.setDashLength(dashLength);
                }
                if(isFaded()){
                    middleConfiguration.setFill(true);
                    middleConfiguration.setFillColor(fadedColor);
                }
                if(isFilled()){
                    middleConfiguration.setStrokeWidth(fillWidth);
                    middleConfiguration.setFill(true);
                }
                if(isShaded()){
                    if(isBold()){
                        middleConfiguration.setStrokeWidth(getWidth());
                    }else{
                        middleConfiguration.setStrokeWidth(FILLED_STROKE_WIDTH);
                    }
                    middleConfiguration.setShaded(true);
                    middleConfiguration.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                }else{
                    middleConfiguration.setStrokeWidth(getWidth());
                }
                middleConfiguration.setColor(color);
                
                if(closed){
                    middleConfiguration.setClosed(true);
                    middleConfiguration.setLineJoin(LineJoin.Bevel);
                }
                
                innerShapes.add(middleConfiguration);
            }
            
            //get Arrow Head Configuration
            ShapeBuilderConfiguration[] arrowHeads = getArrowHeads(resultPoints, penDouble);
            //add resulting arrow head shapes
            for(int i = 0; i < arrowHeads.length; i++) {
                if(arrowHeads[i] != null){
                    innerShapes.add(arrowHeads[i]);
                }
            }
            
            resultingConfiguration = new CompositeShapeConfiguration(ParseElementDefinition.SPLINE, innerShapes);
            ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(zOrder);
            setResultingConfiguration(resultingConfiguration);
            
        }
    }    
    
    protected Area convertSplineToArea(List<Point> points){
        Area area = null;
        
        ExtendedGeneralPath curvePath = new ExtendedGeneralPath(new GeneralPath());
        boolean first = true;
        
        for(int i=1; i<points.size()-4; i+=3){
            if(first){
                curvePath.moveTo(points.get(i).getX(), points.get(i).getY());
                curvePath.curveTo(points.get(i+1).getX(), points.get(i+1).getY(),
                        points.get(i+2).getX(), points.get(i+2).getY(),
                        points.get(i+3).getX(), points.get(i+3).getY());
                first = false;
            } else{
                curvePath.curveTo(points.get(i+1).getX(), points.get(i+1).getY(),
                        points.get(i+2).getX(), points.get(i+2).getY(),
                        points.get(i+3).getX(), points.get(i+3).getY());
            }
        }
        area = new Area(curvePath);
        return area;
    }
    
    /**
     *Compare the curveType with the constants, and calculate
     *the points to create the arrow head
     *
     *@returns a double dimension array containing arrow head shapes
     *for start, end, left and right
     */
    private ShapeBuilderConfiguration[] getArrowHeads(List<Point> splinePoints, boolean penDouble){
        ShapeBuilderConfiguration[] arrowHeadConfiguration = new ShapeBuilderConfiguration[2];
        
        Point basePoint = null;
        Point tipPoint = null;
        boolean arrowAtEnd = true;
        
        //calculate points for arrowhead at the end of the curve spline
        //it use the arrowheadHead to know if the arrowhead is full, half right or half left
        if((curveStyle.length > CurveType.ArrowAtEnd.ordinal() && curveStyle[CurveType.ArrowAtEnd.ordinal()] == 1) ||
                (curveStyle.length > CurveType.HalfArrowAtEnd.ordinal() && curveStyle[CurveType.HalfArrowAtEnd.ordinal()] == 1)){
            //taken from C++ code
            //get the base point from the element before last of the curve point
            //get the tipPoint from the last element of the curve point
            basePoint = splinePoints.get(splinePoints.size() - 2);
            tipPoint = splinePoints.get(splinePoints.size() - 1);
            arrowHeadConfiguration[ARROWHEAD_AT_END]= getArrowHeadPart(tipPoint, basePoint, splinePoints, arrowAtEnd, penDouble);
        }
        //calculate points for arrow head at the begin of the curve spline
        //it use the arrowheadTail to know if the arrowhead is full, half right or half left
        if((curveStyle.length > CurveType.ArrowAtStart.ordinal() && curveStyle[CurveType.ArrowAtStart.ordinal()] == 1) ||
                (curveStyle.length > CurveType.HalfArrowAtStart.ordinal() && curveStyle[CurveType.HalfArrowAtStart.ordinal()] == 1)){
            
            //taken from C++ code
            //get the base point from the second element of the curve point
            //get the tipPoint from the first element of the curve point
            basePoint = splinePoints.get(1);
            tipPoint = splinePoints.get(0);
            arrowAtEnd = false;
            arrowHeadConfiguration[ARROWHEAD_AT_BEGIN]= getArrowHeadPart(tipPoint, basePoint, splinePoints, arrowAtEnd, penDouble);
        }
        
        return arrowHeadConfiguration;
    }
    
    /**
     * Create the arrowhead configuration with the calculate values (direction, base size, width, and angle of the arrow)
     */
    public ShapeBuilderConfiguration getArrowHeadPart(Point tipPoint, Point basePoint, List<Point> splinePoints,
            boolean arrowAtEnd, boolean penDouble){
        
        // Arrow direction is undetermined. Try to base it on the inner control point
        if (tipPoint.equals(basePoint)){
            if(arrowAtEnd){
                tipPoint = splinePoints.get(splinePoints.size() - 3);
                
                if (tipPoint.equals(basePoint)){ // both control points are on top of the endpoint
                    // need to base it on the control point of the previous endpoint, if there is one
                    if (splinePoints.size() > 3){
                        tipPoint = splinePoints.get(splinePoints.size() - 4);
                    }
                }
                if (tipPoint.equals(basePoint)){ // both control points are *still* on top of the endpoint (CDBR-6509)
                    // need to base it on the control point of the previous endpoint, if there is one
                    if (splinePoints.size() > 3){
                        tipPoint = splinePoints.get(splinePoints.size() - 5);
                    }
                }
            }else{
                tipPoint = splinePoints.get(2);
                if (tipPoint.equals(basePoint)){ // both control points are on top of the endpoint
                    // need to base it on the control point of the previous endpoint, if there is one
                    if (splinePoints.size() > 3){
                        tipPoint = splinePoints.get(3);
                    }
                }
            }
            
            // Translate the control point to the other side of the headBasePt
            // so the arrow points in the correct direction.
            double dx = basePoint.getX() - tipPoint.getX();
            double dy = basePoint.getY() - tipPoint.getY();
            tipPoint = new Point(basePoint.getX() + dx, basePoint.getY() + dy);
        }
        
        
        Point startPoint;
        Point endPoint;
        double arrowWidth;
        
        //taken from C++ code.
        double baseSize = 8 * getWidth();
        double arrowAngle = GeometricOperations.angle(basePoint, tipPoint);
        
        startPoint = GeometricOperations.offset(basePoint, arrowAngle, baseSize);
        
        double x1 =  ARROW_HEAD_HALF_WIDTH * (basePoint.getY() - startPoint.getY());
        double y1 = -ARROW_HEAD_HALF_WIDTH * (basePoint.getX() - startPoint.getX());
        
        endPoint = new Point(basePoint.getX() - (y1 / 2), basePoint.getY() + (x1 / 2));
        
        startPoint.setX(startPoint.getX() - (y1 / 2));
        startPoint.setY(startPoint.getY() + (x1 / 2));
        
        arrowWidth = GeometricOperations.distance(x1, y1);
        arrowAngle = GeometricOperations.angle(basePoint, endPoint);
        
        return getArrowheadConfiguration(basePoint, startPoint, endPoint, arrowAngle, arrowWidth, penDouble, arrowAtEnd);
    }
    
    /**
     *Create the arrow head configuration for the specified points.
     *@param arrowHead it could be arrowHeadHead or arrowHeadTail
     *
     */
    private ShapeBuilderConfiguration getArrowheadConfiguration(Point basePoint, Point startPoint, Point endPoint,
            double arrowAngle, double arrowWidth, boolean penDouble, boolean arrowAtEnd){
        ArrowHeadConfiguration resultingArrowhead = null;
        boolean halfRightArrow = false;
        boolean halfLeftArrow = false;
        double halfArrowOffset = getWidth() / 2;
        String arrowheadType;
        
        //taken from C++ code
        //if the pencil tool its double the arrow head will be resized
        //in a length of (bold width * 2
        if(penDouble){
            double doubleSplineArrowScale = boldWidth * 2;
            
            startPoint = GeometricOperations.offset(startPoint, arrowAngle, -doubleSplineArrowScale);
            endPoint = GeometricOperations.offset(endPoint, arrowAngle, doubleSplineArrowScale);
            basePoint = GeometricOperations.offset(basePoint, arrowAngle, doubleSplineArrowScale);
            arrowWidth += doubleSplineArrowScale;
            
            halfArrowOffset += curveSpacing;
        }
        boolean isFullArrow = false;
        
        if(arrowAtEnd){
            arrowheadType = arrowheadHead;
            isFullArrow = (curveStyle.length > CurveType.ArrowAtEnd.ordinal() && curveStyle[CurveType.ArrowAtEnd.ordinal()] == 1);
        }else{
            arrowheadType = arrowheadTail;
            isFullArrow = (curveStyle.length > CurveType.ArrowAtStart.ordinal() && curveStyle[CurveType.ArrowAtStart.ordinal()] == 1);
        }
        
        if(arrowheadType != null && !isFullArrow){
            
            if(arrowheadType.equalsIgnoreCase(ARROW_HEAD_HALF_RIGHT)){
                halfRightArrow = true;
            }else{
                halfLeftArrow = true;
                halfArrowOffset *= -1;
            }
            basePoint = GeometricOperations.offset(basePoint, arrowAngle + Math.PI / 2, halfArrowOffset);
            startPoint = GeometricOperations.offset(startPoint, arrowAngle + Math.PI / 2, halfArrowOffset);
            endPoint = GeometricOperations.offset(endPoint, arrowAngle + Math.PI / 2, halfArrowOffset);
        }
        
        resultingArrowhead = ArrowHeadConfiguration.getArrowHeadShape(basePoint,
                startPoint, endPoint, arrowWidth, arrowAngle, 0, halfLeftArrow, halfRightArrow);
        
        resultingArrowhead.setColor(color);
        resultingArrowhead.setFill(true);
        
        return resultingArrowhead;
    }
    
    /**
     * The ratio of miter length (distance between the outer tip and the 
     * inner corner of the miter) to the line width is directly related 
     * to the angle (theta) between the segments in user space by the formula:
     * 
     * miterLength / stroke-width = 1 / sin ( theta / 2 )
     *
     * Note: we use the minor angle that exist between the linejoin of segments
     */
    protected double getMiterLimit(double theta){
        double result;
        
        result = (1 / Math.sin(theta / 2)) * width;        
        
        return result;
    }
    
    protected String getTypeString() {
        return lineType + "_" + fillType;
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
    
    public int getFadePercent() {
        return fadePercent;
    }
    
    public void setFadePercent(int fadePercent) {
        this.fadePercent = fadePercent;
    }
    
    public double getDashLength() {
        return dashLength;
    }
    
    public void setDashLength(double dashLength) {
        this.dashLength = dashLength;
    }
    
    protected Color getFadedColor() {
        return fadedColor;
    }
    
    protected void setFadedColor(Color fadedColor) {
        this.fadedColor = fadedColor;
    }
}
