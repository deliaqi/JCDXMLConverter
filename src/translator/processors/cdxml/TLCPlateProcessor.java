package translator.processors.cdxml;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Color;
import translator.graphics.CurveType;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.EllipseConfiguration;
import translator.graphics.shapes.builders.configurations.ImageConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class TLCPlateProcessor extends EmbeddedObjectProcessor {
    private Point bottomLeft;
    private Point bottomRight;
    private Point topLeft;
    private Point topRight;
    private double solventFrontFraction;
    private double originFraction;
    private Color spotColor;
    private boolean showOrigin;
    private boolean showSolventFrontFraction;
    private boolean showBorders;
    private boolean sideTicks;
    private boolean transparent;
    
    private double heightAngle;
    private double widthAngle;
    private double tlcPlateAngle;
    private double tlcPlateHeight;
    private double tlcPlateWidth;
    private double maxTail;
    private double maxWidth;
    private double maxHeight;
    private double spotWidth;
    private double spotHeight;
    private double spotTail;
    
    //taken from C++
    private static int TICK_SIDE_LIMIT = 250;
    
    protected void configure(){
        super.configure();
        
        bottomLeft = parseCoords(getElement().getAttribute(ParseElementDefinition.TLC_BOTTOM_LEFT), getElement());
        bottomRight = parseCoords(getElement().getAttribute(ParseElementDefinition.TLC_BOTTOM_RIGHT), getElement());
        topLeft = parseCoords(getElement().getAttribute(ParseElementDefinition.TLC_TOP_LEFT), getElement());
        topRight = parseCoords(getElement().getAttribute(ParseElementDefinition.TLC_TOP_RIGHT), getElement());
        
        originFraction = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.TLC_ORIGIN_FRACTION));
        solventFrontFraction = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.TLC_SOLVENT_FRONT_FRACTION));
    }
    
    protected void cleanup() {
        bottomLeft = null;
        bottomRight= null;
        topLeft= null;
        topRight= null;
        solventFrontFraction = 0;
        originFraction = 0;
        spotColor = null;
        showOrigin = false;
        showSolventFrontFraction = false;
        showBorders = false;
        sideTicks = false;
        transparent = false;
        
        heightAngle = 0;
        widthAngle = 0;
        tlcPlateAngle = 0;
        tlcPlateHeight = 0;
        tlcPlateWidth = 0;
        maxTail = 0;
        maxWidth = 0;
        maxHeight = 0;
        spotWidth = 0;
        spotHeight = 0;
        spotTail = 0;
        super.cleanup();
    }
    
    public TLCPlateProcessor() {
    }
    
    protected void process() {
        ParsedElement tlcElement = getElement();
        SplineConfiguration borderSpline = null;
        List<Point> borderPoints = new ArrayList();
        List<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        
        tlcPlateAngle = Math.atan2(topLeft.getX() - bottomLeft.getX(), bottomLeft.getY() - topLeft.getY());
        
        //get boolean attributes for show YES/NO the diferents parts of the TlcPlate
        if(tlcElement.getAttribute(ParseElementDefinition.TLC_SHOW_BORDERS).equalsIgnoreCase("yes")){
            showBorders = true;
        }else{
            showBorders = false;
        }
        if(tlcElement.getAttribute(ParseElementDefinition.TLC_SHOW_ORIGIN).equalsIgnoreCase("yes")){
            showOrigin = true;
        }else{
            showOrigin = false;
        }
        if(tlcElement.getAttribute(ParseElementDefinition.TLC_SHOW_SOLVENT_FRONT).equalsIgnoreCase("yes")){
            showSolventFrontFraction = true;
        }else{
            showSolventFrontFraction = false;
        }
        if(tlcElement.getAttribute(ParseElementDefinition.TLC_SHOW_SIDE_TICKS).equalsIgnoreCase("yes")){
            sideTicks = true;
        }else{
            sideTicks = false;
        }
        if(tlcElement.getAttribute(ParseElementDefinition.TLC_TRANSPARENT).equalsIgnoreCase("yes")){
            transparent = true;
        }else{
            transparent = false;
        }
        
        if(showBorders){
            borderPoints.add(topLeft);
            borderPoints.add(topRight);
            borderPoints.add(bottomRight);
            borderPoints.add(bottomLeft);
            borderSpline = new SplineConfiguration(borderPoints, true);
            //if the TlcPlate has not the transparent attribute add a filled TlcPlate borders with background color
            if(!transparent){
                borderSpline.setFillColor(convertColor(environment.getBackgroundColor()));
                borderSpline.setFill(true);
            }else{
                borderSpline.setFill(false);
            }
            borderSpline.setColor(getColor());
            borderSpline.setStrokeWidth(lineWidth);
            borderSpline.setLineJoin(LineJoin.Miter);
            innerShapes.add(borderSpline);
        }
        
        //initialize attributes from element
        tlcPlateHeight = GeometricOperations.distance(bottomLeft, topLeft);
        tlcPlateWidth = GeometricOperations.distance(topLeft, topRight);
        heightAngle = GeometricOperations.angle(bottomRight, topRight);
        widthAngle = GeometricOperations.angle(bottomLeft, bottomRight);
        
        Point originLeft = GeometricOperations.offset(bottomLeft, heightAngle, tlcPlateHeight * originFraction);
        Point originRight = GeometricOperations.offset(bottomRight, heightAngle, tlcPlateHeight * originFraction);
        
        Point solventFrontLeft = GeometricOperations.offset(topLeft, heightAngle, -(tlcPlateHeight * solventFrontFraction));
        Point solventFrontRight = GeometricOperations.offset(topRight, heightAngle, -(tlcPlateHeight * solventFrontFraction));
        double fractionDistance = GeometricOperations.distance(originLeft, solventFrontLeft);
        
        if(showOrigin){
            innerShapes.add(getFractionLineConfiguration(originLeft, originRight));
        }
        
        if(showSolventFrontFraction){
            innerShapes.add(getFractionLineConfiguration(solventFrontLeft, solventFrontRight));
        }
        
        if(sideTicks){
            List<ShapeBuilderConfiguration> tlcSideTicksConfiguration = new ArrayList();
            
            //taken from C++ code
            double increment = 0.25;
            if (tlcPlateHeight > TICK_SIDE_LIMIT) increment = 0.1;
            double tickOffset = 3 * lineWidth;
            
            //start to draw the side ticks in the beggining of the origin fraction line
            Point tickLeftBegin = GeometricOperations.offset(bottomLeft, heightAngle, originFraction * tlcPlateHeight);
            Point tickRightBegin = GeometricOperations.offset(bottomRight, heightAngle, originFraction * tlcPlateHeight);
            Point tickLeftEnd;
            Point tickRightEnd;
            
            //create the side ticks: starting at bottom begin point
            //move (height * increment) in the height angle
            //and calculate the other side tick point
            for (double d = increment; d < 0.99; d += increment){
                tickLeftBegin = GeometricOperations.offset(tickLeftBegin, heightAngle, fractionDistance * increment);
                tickLeftEnd = GeometricOperations.offset(tickLeftBegin, widthAngle, tickOffset);
                tickRightBegin = GeometricOperations.offset(tickRightBegin, heightAngle, fractionDistance * increment);
                tickRightEnd = GeometricOperations.offset(tickRightBegin, widthAngle, -tickOffset);
                tlcSideTicksConfiguration.add(getSideTick(tickLeftBegin, tickLeftEnd));
                tlcSideTicksConfiguration.add(getSideTick(tickRightBegin, tickRightEnd));
            }
            innerShapes.add(
                    new CompositeShapeConfiguration(ParseElementDefinition.TLC_SIDE_TICKS, tlcSideTicksConfiguration));
        }
        
        double interLaneDistance = GeometricOperations.distance(bottomLeft, bottomRight) / (tlcElement.getElements().size());
        
        //taken from C++ code
        double thickSize = 3 * lineWidth;
        
        /*Create a TreeSet and implement a comparator that use
         *the TLC_LANE_ORDER attribute created to solve an error
         *with the order of parsing the TlcLane element
         */
        Comparator<ParsedElement> tlcLaneComparator = new Comparator<ParsedElement>() {
            public int compare(ParsedElement firstElement, ParsedElement newElement) {
                double firstValue = Integer.parseInt(firstElement.getAttribute(ParseElementDefinition.TLC_LANE_ORDER));
                double newValue = Integer.parseInt(newElement.getAttribute(ParseElementDefinition.TLC_LANE_ORDER));
                int result = 0;
                if(firstValue > newValue){
                    result = 1;
                } else if(firstValue < newValue){
                    result = -1;
                } else {
                    result = 0;
                }
                return result;
            }
        };
        
        TreeSet<ParsedElement> tlcLaneTree = new TreeSet(tlcLaneComparator);
        tlcLaneTree.addAll(tlcElement.getElements(ParseElementDefinition.TLC_LANE));
        
        //process TlcLanes
        int k = 0;
        Iterator tlcLaneIterator = tlcLaneTree.iterator();
        while(tlcLaneIterator.hasNext()){
            ParsedElement tlcLane = (ParsedElement) tlcLaneIterator.next();
            k++;
            List<ShapeBuilderConfiguration> tlcLaneConfiguration = new ArrayList();
            
            //
            Point lanePointCenter = GeometricOperations.offset(originLeft, widthAngle, interLaneDistance * k);
            Point lanePointBottom = GeometricOperations.offset(lanePointCenter, heightAngle, thickSize);
            Point lanePointTop = GeometricOperations.offset(lanePointCenter, heightAngle, -thickSize);
            
            //build tlcLane Segments creating a segment from lanePointBottom
            SegmentConfiguration tlcLaneSegment = new SegmentConfiguration(lanePointBottom, lanePointTop);
            tlcLaneSegment.setStrokeWidth(lineWidth);
            tlcLaneSegment.setColor(color);
            innerShapes.add(tlcLaneSegment);
            
            //process TlcSpots
            for(ParsedElement tlcSpot : tlcLane.getElements()){
                List<EllipseConfiguration> primitiveEllipses;
                List<SplineConfiguration> splineEllipses;
                ParsedElement spotColorElement = null;
                if(tlcSpot.getElements(ParseElementDefinition.TLC_COLOR).size() > 0){
                    spotColorElement = tlcSpot.getElements(ParseElementDefinition.TLC_COLOR).get(0);
                }else{
                    spotColorElement = getEnvironment().getForegroundColor();
                }
                
                spotColor = convertColor(spotColorElement);
                if(tlcSpot.hasAttribute(ParseElementDefinition.TLC_CURVE_TYPE)){
                    curveType = Integer.parseInt(tlcSpot.getAttribute(ParseElementDefinition.TLC_CURVE_TYPE));
                }else{
                    //When the cdxml doesn't have curve Type it's Hollow case
                    curveType = CurveType.Closed.ordinal();
                }
                //obtain attributes from tlcSpot element
                double rf = Double.parseDouble(tlcSpot.getAttribute(ParseElementDefinition.TLC_RF));
                spotWidth = Double.parseDouble(tlcSpot.getAttribute(ParseElementDefinition.TLC_SPOT_WIDTH)) / 65536;
                spotHeight = Double.parseDouble(tlcSpot.getAttribute(ParseElementDefinition.TLC_SPOT_HEIGHT)) / 65536;
                spotTail = Double.parseDouble(tlcSpot.getAttribute(ParseElementDefinition.TLC_SPOT_TAIL)) / 65536;
                double laneAxisSize = GeometricOperations.distance(solventFrontLeft, originLeft);
                
                //taken from C++ code
                maxWidth = tlcPlateWidth / (tlcLaneTree.size() + 1) * 2;
                maxHeight = Math.min(tlcPlateHeight * originFraction + laneAxisSize * rf,
                        tlcPlateHeight * solventFrontFraction + laneAxisSize * (1 - rf)) * 2;
                maxTail = Math.max(tlcPlateHeight * originFraction + laneAxisSize * rf - spotHeight / 2, -tlcPlateHeight);
                Point centerSpot = GeometricOperations.offset(lanePointCenter, heightAngle, fractionDistance * rf);
                
                //If the Tlc Spot has not a Custom Pic draw the normal Spot
                if(!tlcSpot.getName().equalsIgnoreCase(ParseElementDefinition.TLC_SPOT_IMAGE)){
                    if(spotTail != 0 || Math.abs(spotHeight - spotWidth) > 0.01){
                        //Transformed Spot (use spline)
                        splineEllipses = getSpotConfigurationTransformed(centerSpot, curveType);
                        for(int i=0; i<splineEllipses.size(); i++){
                            tlcLaneConfiguration.add(splineEllipses.get(i));
                        }
                    }else{
                        //Not Transformed Spot (use ellipse configuration)
                        primitiveEllipses = getSpotConfiguration(centerSpot, curveType);
                        for(int i=0; i<primitiveEllipses.size(); i++){
                            tlcLaneConfiguration.add(primitiveEllipses.get(i));
                        }
                    }
                }else{//The Tlc Spot has a custom pic
                    ImageConfiguration imageConfiguration = getImageConfiguration((tlcSpot.getElements(
                            ParseElementDefinition.EMBEDDED_OBJECT).get(0)), tlcPlateAngle, centerSpot, zOrder);
                    tlcLaneConfiguration.add(imageConfiguration);
                }
            }
            innerShapes.add(
                    new CompositeShapeConfiguration(ParseElementDefinition.TLC_LANE, tlcLaneConfiguration));
        }
        
        resultingConfiguration = new CompositeShapeConfiguration("TlcPlate", innerShapes);
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(zOrder);
        setResultingConfiguration(resultingConfiguration);
    }
    
    protected String getTypeString() {
        if (lineType == null) {
            return "";
        } else {
            return lineType;
        }
    }
    
    /*
     * obtain a Segment configuration for each side ticks
     * with the corresponding attributes (color, stroke)
     */
    public SegmentConfiguration getSideTick(Point begin, Point end){
        SegmentConfiguration result;
        result = new SegmentConfiguration(begin, end);
        result.setStrokeWidth(lineWidth);
        result.setColor(color);
        
        return result;
    }
    
    /*
     * obtain a Segment configuration for the fraction line
     * with the corresponding attributes (color, stroke, dash)
     */
    public SegmentConfiguration getFractionLineConfiguration(Point begin, Point end){
        SegmentConfiguration result;
        result = new SegmentConfiguration(begin, end);
        result.setStrokeWidth(lineWidth);
        result.setColor(color);
        result.setDashed(true);
        result.setDashLength(hashSpacing);
        
        return result;
    }
    
    /*
     * Obtain the configuration for the not transformed spot
     * using Ellipses to create it.
     */
    public List<EllipseConfiguration> getSpotConfiguration(Point center, int curveType ){
        List<EllipseConfiguration> result = new ArrayList();
        EllipseConfiguration ellipse = null;
        EllipseConfiguration doubleEllipse = null;
        
        //taken from C++ code
        double offset = boldWidth;
        double radiusX = spotWidth / 2;
        double radiusY = spotHeight / 2;
        ellipse = new EllipseConfiguration(center, radiusX, radiusY, Math.toDegrees(widthAngle));
        ellipse.setColor(spotColor);        
        
        //Interpret the value of the curveType
        //and use it for build the spot
        int[] curveStyle = getCurveStyle(curveType);
        
        //if has double ellipse create it
        if((curveStyle.length > CurveType.Doubled.ordinal() && curveStyle[CurveType.Doubled.ordinal()] == 1)){
            radiusX = spotWidth/2 + boldWidth;
            radiusY = spotHeight/2 + boldWidth;
            doubleEllipse = new EllipseConfiguration(center, radiusX, radiusY, Math.toDegrees(widthAngle));
            doubleEllipse.setStrokeWidth(lineWidth);
            doubleEllipse.setColor(spotColor);
            result.add(doubleEllipse);
        }
        
        if((curveStyle.length > CurveType.Filled.ordinal() && curveStyle[CurveType.Filled.ordinal()] == 1)){
            ellipse.setFill(true);
        }else if((curveStyle.length > CurveType.Dashed.ordinal() && curveStyle[CurveType.Dashed.ordinal()] == 1)){
            ellipse.setDashed(true);
            ellipse.setDashLength(hashSpacing);
            ellipse.setStrokeWidth(lineWidth);
        }else if((curveStyle.length > CurveType.Bold.ordinal() && curveStyle[CurveType.Bold.ordinal()] == 1)){
            ellipse.setStrokeWidth(boldWidth);
        }else{
            //When the cdxml doesn't doesn't have curve Type it's Hollow case
            ellipse.setStrokeWidth(lineWidth);
        }
        result.add(ellipse);
        
        return result;
    }
    
    /*
     * Obtain the list of spline configuration for the transformed spot
     * using Oval Spline to create it.
     */
    public List<SplineConfiguration> getSpotConfigurationTransformed(Point centerSpot, int curveType ){
        List<SplineConfiguration> result = new ArrayList();
        SplineConfiguration ellipse;
        SplineConfiguration doubleEllipse = null;
        double actualWidth;
        double actualHeight;
        double actualTail;
        Point top;
        
        //taken from C++ code
        if (spotTail < 0){
            centerSpot.setY(centerSpot.getY() - spotTail / 2 );
        }
        
        //taken from C++ code
        actualWidth = Math.min(spotWidth, maxWidth - 2 * boldWidth);
        actualHeight = Math.max(Math.min(spotHeight, maxHeight - 2 * boldWidth), lineWidth);
        actualTail = Math.min(spotTail, maxTail);
        
        top = new Point(centerSpot.getX(), centerSpot.getY() + actualHeight / 2);
        ellipse = getOvalSplineConfiguration(centerSpot, top, actualHeight, actualWidth, actualTail,  false);
        ellipse.setColor(spotColor);
        ellipse.setStrokeWidth(lineWidth);
                
        int[] curveStyle = getCurveStyle(curveType);
        
        //if has double ellipse create it
        if((curveStyle.length > CurveType.Doubled.ordinal() && curveStyle[CurveType.Doubled.ordinal()] == 1)){
            //taken from C++ Code
            double spacing = boldWidth / lineWidth;
            
            actualWidth = Math.min(spotWidth + 2 * spacing * lineWidth, maxWidth);
            actualHeight = Math.min(spotHeight + 2 * spacing * lineWidth, maxHeight);
            top = new Point(centerSpot.getX(), centerSpot.getY() + actualHeight / 2);
            doubleEllipse = getOvalSplineConfiguration(centerSpot, top, actualHeight, actualWidth, actualTail, true);
            doubleEllipse.setStrokeWidth(lineWidth);
            doubleEllipse.setColor(spotColor);
            result.add(doubleEllipse);
        }
        
        if((curveStyle.length > CurveType.Filled.ordinal() && curveStyle[CurveType.Filled.ordinal()] == 1)){
            ellipse.setFill(true);
        }else if((curveStyle.length > CurveType.Dashed.ordinal() && curveStyle[CurveType.Dashed.ordinal()] == 1)){
            ellipse.setDashed(true);
            ellipse.setDashLength(hashSpacing);
            ellipse.setStrokeWidth(lineWidth);
        }else if((curveStyle.length > CurveType.Bold.ordinal() && curveStyle[CurveType.Bold.ordinal()] == 1)){
            ellipse.setStrokeWidth(boldWidth);
        }else{
            //When the cdxml doesn't has curve Type it's Hollow case
            ellipse.setStrokeWidth(lineWidth);
        }
        result.add(ellipse);        
        return result;
    }
    
    /*
     * Obtain the Oval Spline configuration for the
     * transformed spot (simple spot, and doubled spot)
     */
    public SplineConfiguration getOvalSplineConfiguration(Point centerSpot, Point top, double actualHeight, double actualWidth, double actualTail, boolean isDoubled){
        SplineConfiguration result = null;
        List<Point> ovalPoints = calculateTransformedOvalPoints(centerSpot, top, actualWidth / actualHeight);
        
        //Transform the points
        for(Point ovalPoint:ovalPoints){
            //use affine transform as a 2D matrix
            AffineTransform transformation = new AffineTransform();
            transformation.translate(centerSpot.getX(), centerSpot.getY());
            transformation.rotate(tlcPlateAngle);
            transformation.translate(-centerSpot.getX(), -centerSpot.getY());
            //affine transforms works with Point2D instance
            Point2D transformedPoint = new Point2D.Double(ovalPoint.getX(), ovalPoint.getY());
            transformation.transform(transformedPoint, transformedPoint);
            ovalPoint.setX(transformedPoint.getX());
            ovalPoint.setY(transformedPoint.getY());
        }
        
        // Add last closing curve
        Point endPoint = ovalPoints.get(1);
        Point controlPoint2 = ovalPoints.get(0);
        
        ovalPoints.add(controlPoint2);
        ovalPoints.add(endPoint);
        ovalPoints.add(ovalPoints.get(2)); // Needed by buildSplineBeziers, will be ignored
        
        if(spotTail != 0){
            
            //taken from C++ code
            for (int i = 3; i <= 5; ++i){
                Point tailPoint = GeometricOperations.offset(ovalPoints.get(i), tlcPlateAngle + Math.PI/2, actualTail);
                ovalPoints.remove(i);
                ovalPoints.add(i, tailPoint);
            }
            if(!isDoubled){
                if (spotTail < -spotHeight / 2 && spotHeight != 0){
                    // Things need to be somewhat un-rounded for crescent-shaped spots
                    double adjustRatio = 1 - (-spotTail - spotHeight / 2) / (spotHeight/ 2);
                    double handleDistance = GeometricOperations.distance(ovalPoints.get(2), ovalPoints.get(1));
                    handleDistance = handleDistance * adjustRatio;
                    Point tailPoint;
                    tailPoint = GeometricOperations.offset(ovalPoints.get(1), tlcPlateAngle + Math.PI/2, handleDistance);
                    ovalPoints.remove(2);
                    ovalPoints.add(2, tailPoint);
                    tailPoint = GeometricOperations.offset(ovalPoints.get(7), tlcPlateAngle + Math.PI/2, handleDistance);
                    ovalPoints.remove(6);
                    ovalPoints.add(6, tailPoint);
                }
            }
        }
        result =  buildSplineBeziers(ovalPoints);
        
        return result;
    }
    
}
