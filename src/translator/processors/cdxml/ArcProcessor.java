
package translator.processors.cdxml;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.BuilderConfiguration;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.AlgebraicOperations;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.StringUtils;

public class ArcProcessor extends OvalProcessor {
    
    private static final double SPLINE_POINTS_PRECISION = 0.001;
    //This constant is used to mantain the proportion between the arrow arc lenght and the cross lenght.
    private static final int ARROW_NO_GO_SCALE_FACTOR = 2;
    private static final int ARROW_SCALE_FACTOR = 100;
    private static final String EQUILIBRIUM_ARROW_TYPE = "Equilibrium Arrow";
    //These constants are used to index right and left heads and tails arrow points inside a points array.
    private static final int ARROW_HEAD_LEFT = 0;
    private static final int ARROW_TAIL_LEFT = 1;
    private static final int ARROW_HEAD_RIGHT = 2;
    private static final int ARROW_TAIL_RIGHT = 3;
    private static final int ARROW_HEADS_AND_TAILS_NUMBER_OF_POINTS = 4;
    
    //These constants are used to index right and left heads arrow points inside a points array.
    private static final int EQUILIBRIUM_ARROW_LEFT_HEAD = 0;
    private static final int EQUILIBRIUM_ARROW_RIGHT_HEAD = 1;
    private static final int EQUILIBRIUM_ARROW_HEAD_NUMBER_OF_POINTS = 2;
    
    private static final int START_ANGLE_OFFSET_DEFAULT_VALUE = 0;
    //These params are used to compare apparent distance values when calculating arc base point
    private static final double APPARENT_DISTANCE_COMPARISON_PARAM_1 = 0.99;
    private static final double APPARENT_DISTANCE_COMPARISON_PARAM_2 = 1.01;
    
    //These constants are used for calculating major and minor axis offsets when drawing arcs.
    private static final double MAJOR_AXIS_ANGLE_OFFSET = 0;
    private static final double MINOR_AXIS_ANGLE_OFFSET = -Math.PI / 2;
    
    private static final double MID_POINT_SCALAR = 0.5;
    
    //The value that the arc may be extend to joined the arrowhead
    private static final double ARC_EXTEND_SIZE = 1;
    
    protected Point arrowStart;
    protected Point arrowEnd;
    protected Point arrowCenter;
    protected double xRadius;
    protected double yRadius;
    protected double angularSize;
    protected double arrowCenterSize;
    protected double arrowHeadWidth;
    
    protected double shaftSpacing;
    protected String arrowType;
    protected String arrowHeadType;
    protected double arrowHeadSize;
    
    private double displacedOffset;
    private Point headStartDisplaced;
    private Point headEndDisplaced;
    //It is used to set which arrow ending wil be displaced (head or tail)
    //in unbalanced equilibrium arrow.
    protected boolean equilibriumAtHead;
    //This is the ratio of the length of the left component to the right component of the equilibrium arrow.
    protected double arrowEquilibriumRatio = 0;
    private double directionAngle = 0;
    
    public ArcProcessor() {
    }
    
    protected void cleanup() {
        arrowStart = null;
        arrowEnd = null;
        arrowCenter = null;
        xRadius = 0;
        yRadius = 0;
        angularSize = 0;
        arrowCenterSize = 0;
        arrowHeadWidth = 0;
        shaftSpacing = 0;
        arrowType = StringUtils.EMPTY_STRING;
        arrowHeadType = StringUtils.EMPTY_STRING;
        arrowHeadSize = 0;
        displacedOffset = 0;
        headStartDisplaced = null;
        headEndDisplaced = null;        
        equilibriumAtHead = false;
        arrowEquilibriumRatio = 0;
        directionAngle = 0;
        
        super.cleanup();
    }
    
    protected void configure() {
        super.configure();
        
        ParsedElement arc = getElement();
        //From here, we have several "if" blocks because this class could be used for other procesors besides arrows,
        //for instance: Properties3DProcessor, and many attributes are not used in other procesors.
        if (lineType != null) {
            setAttributesForDrawingElement();
            
            if (isBold()) {
                setWidth(getBoldWidth());
            } else {
                setWidth(getLineWidth());
            }
        } else {
            setWidth(getLineWidth());
            setDashed(false);
            setFilled(false);
            setShadowed(false);
        }
        
        if (arc.hasAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE)) {
            angularSize = Double.parseDouble(arc.getAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE));
        }
        if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_3D)) {
            arrowStart = parseCoords(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_3D), arc);
        }
        if(arc.hasAttribute(ParseElementDefinition.ARROW_CENTER_3D)){
            arrowCenter = parseCoords(arc.getAttribute(ParseElementDefinition.ARROW_CENTER_3D), arc);
        }
        if(arc.hasAttribute(ParseElementDefinition.ARROW_TAIL_3D)){
            arrowEnd = parseCoords(arc.getAttribute(ParseElementDefinition.ARROW_TAIL_3D), arc);
        }else if(arrowStart != null  || arrowCenter != null || angularSize != 0){
            arrowEnd = calculateArrowEnd(arrowStart, arrowCenter, angularSize);
        }
        if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_CENTER_SIZE)) {
            arrowCenterSize = Double.parseDouble(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_CENTER_SIZE)) / ARROW_SCALE_FACTOR;
        }
        if(arc.hasAttribute(ParseElementDefinition.GRAPHIC_ARROW_TYPE)){
            arrowType = arc.getAttribute(ParseElementDefinition.GRAPHIC_ARROW_TYPE);
        }
        if(arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_TYPE)){
            arrowHeadType = arc.getAttribute(ParseElementDefinition.ARROW_HEAD_TYPE);
        }
        if(arc.hasAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING)){
            shaftSpacing = Double.parseDouble(arc.getAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING)) / ARROW_SCALE_FACTOR;
            //this mantains the proportion between line width and shaft spacing
            shaftSpacing *= getLineWidth();
        }
        if(arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_SIZE)){
            arrowHeadSize = Double.parseDouble(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_SIZE)) / ARROW_SCALE_FACTOR;
        }
        if(arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH)){
            arrowHeadWidth = Double.parseDouble(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH)) / ARROW_SCALE_FACTOR;
        }
        if (arc.hasAttribute(ParseElementDefinition.ARROW_EQUILIBRIUM_RATIO)) {
            arrowEquilibriumRatio = Double.parseDouble(arc.getAttribute(ParseElementDefinition.ARROW_EQUILIBRIUM_RATIO)) / ARROW_SCALE_FACTOR;
        }
        
    }
    
    protected void process() {
        ShapeBuilderConfiguration resultingConfiguration = null;
        ParsedElement arc = getElement();
        CDXMLEnvironment environment = (CDXMLEnvironment) arc.getEnvironment();
        
        //If the arc has curvature
        if (arc.hasAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE)) {
            //If the arc is part of an Equilibrium arrow. Note: Retrosynthetic arrows cannot have curvature.
            if(arc.hasAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING)){
                resultingConfiguration = processCurvedEquilibriumArrow(arc);
            } else{
                //If the arc is a simple one or if it is part of a simple arrow with curvature.
                resultingConfiguration = processCurvedSimpleArc(arc);
            }
        } else {  //If the arc is straight.
            double startArrowAngle = GeometricOperations.angle(arrowEnd, arrowStart);
            double arrowHeadLength = arrowCenterSize * getLineWidth();   //use the line width property instead of the actual width of the arc
            double arrowLength = arrowLength = GeometricOperations.distance(arrowStart, arrowEnd);
            
            //If the arc is part of an arrow.
            if(arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_SIZE)){
                arrowHeadLength = calculateStraightArrowHeadLenght();
            }
            //If the arc is part of an Retrosynthetic or Equilibrium straight arrow.
            if(arc.hasAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING)){
                shaftSpacing = shaftSpacing / 2;
                directionAngle = GeometricOperations.angle(arrowStart, arrowEnd);
                
                //this is for the case when the arrow is flipped vertically
                if(arrowheadHead.equalsIgnoreCase(ParseElementDefinition.ARROW_HALF_RIGHT)){
                    directionAngle = GeometricOperations.angle(arrowEnd, arrowStart);
                    if(arrowType.equals(ParseElementDefinition.ARROW_TYPE_RETRO_SYNTHETIC)){
                        shaftSpacing = -shaftSpacing;
                    }
                }
                
                Point [] arrowHeadsAndTailsPoints = new Point[ARROW_HEADS_AND_TAILS_NUMBER_OF_POINTS];
                //If the arrow is a straight Unbalanced Equilibrium.
                if (arc.hasAttribute(ParseElementDefinition.ARROW_EQUILIBRIUM_RATIO)) {
                    arrowHeadsAndTailsPoints = processStraightUnbalancedEquilibriumArrow(arrowHeadLength, startArrowAngle, arrowLength);
                } else if (arrowType.equals(ParseElementDefinition.ARROW_TYPE_RETRO_SYNTHETIC)) {
                    //If the arrow is Retrosynthetic
                    arrowHeadsAndTailsPoints = processStraightRetrosyntheticArrow(arc);
                }else{
                    //If the arrow is a straight Equilibrium.
                    arrowHeadsAndTailsPoints = processStraightEquilibriumArrow(arrowHeadLength, startArrowAngle, arrowLength);
                }
                
                double modifiedDirectionAngle = directionAngle + Math.PI / 2;
                
                Point headLeft = GeometricOperations.offset(arrowHeadsAndTailsPoints[ARROW_HEAD_LEFT], modifiedDirectionAngle, shaftSpacing);
                Point tailLeft = GeometricOperations.offset(arrowHeadsAndTailsPoints[ARROW_TAIL_LEFT], modifiedDirectionAngle, shaftSpacing);
                
                modifiedDirectionAngle = directionAngle - Math.PI / 2;
                Point headRight = GeometricOperations.offset(arrowHeadsAndTailsPoints[ARROW_HEAD_RIGHT], modifiedDirectionAngle, shaftSpacing);
                Point tailRight = GeometricOperations.offset(arrowHeadsAndTailsPoints[ARROW_TAIL_RIGHT], modifiedDirectionAngle, shaftSpacing);
                
                boolean isLeftConfiguration = true;
                SegmentConfiguration leftConfiguration = getStraightArrowWithShaftSpacingConfiguration(arc, arrowHeadLength, startArrowAngle,
                        headLeft, tailLeft, isLeftConfiguration);
                
                isLeftConfiguration = false;
                SegmentConfiguration rightConfiguration = getStraightArrowWithShaftSpacingConfiguration(arc, arrowHeadLength, startArrowAngle,
                        headRight, tailRight, isLeftConfiguration);
                
                Collection<ShapeBuilderConfiguration> arrowConfigurations = new ArrayList();
                arrowConfigurations.add(leftConfiguration);
                arrowConfigurations.add(rightConfiguration);
                
                resultingConfiguration = new CompositeShapeConfiguration(EQUILIBRIUM_ARROW_TYPE,
                        arrowConfigurations);
            }else{ //If the arc is just a simple one or if it is part of a simple straight arrow.
                resultingConfiguration = processStraightSimpleArc(arc, arrowHeadLength, startArrowAngle);
            }
        }
        
        ((BuilderConfiguration) resultingConfiguration).setColor(getColor());
        ((BuilderConfiguration) resultingConfiguration).setZOrder(zOrder);
        setResultingConfiguration(resultingConfiguration);
    }
    
    /**
     * This method processes curved arcs which are part of an Equilibrium arrow.
     * Note: Retrosynthetic arrows cannot have curvature.
     */
    private ShapeBuilderConfiguration processCurvedEquilibriumArrow(ParsedElement arc) {
        ShapeBuilderConfiguration resultingConfiguration;
        setArrowType(ParseElementDefinition.ARROW_TYPE_EQUILIBRIUM);
        shaftSpacing /= 2;
        
        directionAngle = GeometricOperations.angle(arrowStart, center);
        
        Point[] headPoints = new Point[EQUILIBRIUM_ARROW_HEAD_NUMBER_OF_POINTS];
        headPoints = calculateEquilibriumArrowHeadPoints();
        
        boolean start = true;
        boolean end = false;
        
        List<SegmentConfiguration> leftSegments = createArcAsSpline(headPoints[EQUILIBRIUM_ARROW_LEFT_HEAD],
                center, angularSize, START_ANGLE_OFFSET_DEFAULT_VALUE, start, end);
        
        List<SegmentConfiguration> rightSegments = null;
        
        if (arc.hasAttribute(ParseElementDefinition.ARROW_EQUILIBRIUM_RATIO)) {
            Point smallArrowBasePoint = null;
            double equilibriumAngularSize = angularSize;
            
            if (arrowEquilibriumRatio != 0) {
                equilibriumAngularSize /= Math.abs(arrowEquilibriumRatio);
            }
            start = false;
            rightSegments = createArcAsSpline(headPoints[EQUILIBRIUM_ARROW_RIGHT_HEAD],
                    center, equilibriumAngularSize, equilibriumAngularSize, start, end);
            headStartDisplaced = new Point(rightSegments.get(0).getBeginPoint());
            headEndDisplaced = new Point(rightSegments.get(rightSegments.size() - 1).getEndPoint());
            
            double smallArrowAngle = 0;
            
            if(angularSize < 0){
                smallArrowBasePoint = calculateBasePoint(headStartDisplaced, headEndDisplaced, center,
                        equilibriumAngularSize, arrowCenterSize - ARC_EXTEND_SIZE, false);
                end = false;
                start = false;
                smallArrowAngle = Math.toDegrees(Math.acos(GeometricOperations.cosine(
                        smallArrowBasePoint, center, headStartDisplaced)));
            }else{
                smallArrowBasePoint = calculateBasePoint(headEndDisplaced, headStartDisplaced, center,
                        equilibriumAngularSize, arrowCenterSize - ARC_EXTEND_SIZE, false);
                smallArrowAngle = -Math.toDegrees(Math.acos(GeometricOperations.cosine(
                        smallArrowBasePoint, center, headEndDisplaced)));
            }
            
            rightSegments = createArcAsSpline(smallArrowBasePoint, center, smallArrowAngle,
                    START_ANGLE_OFFSET_DEFAULT_VALUE, start, end);
            
        } else {
            start = false;
            end = true;
            rightSegments = createArcAsSpline(headPoints[EQUILIBRIUM_ARROW_RIGHT_HEAD],
                    center, angularSize, START_ANGLE_OFFSET_DEFAULT_VALUE, start, end);
        }
        
        SplineConfiguration leftConfiguration = getCurvedEquilibriumArrowConfiguration(leftSegments);
        SplineConfiguration rightConfiguration = getCurvedEquilibriumArrowConfiguration(rightSegments);
        
        Collection<ShapeBuilderConfiguration> arrowConfigurations = new ArrayList();
        arrowConfigurations.add(leftConfiguration);
        arrowConfigurations.add(rightConfiguration);
        
        resultingConfiguration = new CompositeShapeConfiguration(EQUILIBRIUM_ARROW_TYPE, arrowConfigurations);
        
        return resultingConfiguration;
    }
    
    /**
     * This method processes simple curved arcs or arcs which are part of simple curved arrows.
     */
    private SplineConfiguration processCurvedSimpleArc(ParsedElement arc) {
        SplineConfiguration resultingConfiguration;
        
        boolean start = false;
        boolean end = false;
        
        //if the arrowhead is angled then the arc must be drawn completely
        if(!arrowHeadType.equalsIgnoreCase(ParseElementDefinition.ARROW_ANGLE)){
            start = arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD);
            end = arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL);
        }
        
        List<SegmentConfiguration> segments = createArcAsSpline(arrowStart, center, angularSize, START_ANGLE_OFFSET_DEFAULT_VALUE, start, end);
        resultingConfiguration = new SplineConfiguration(segments);
        resultingConfiguration.setStrokeWidth(getWidth());
        resultingConfiguration.setLineCap(LineCap.Butt);
        resultingConfiguration.setDashed(isDashed());
        resultingConfiguration.setDashLength(getHashSpacing());
        return resultingConfiguration;
    }
    
    /**
     * This method processes simple straight arcs or arcs which are part of simple straight arrows.
     */
    private SegmentConfiguration processStraightSimpleArc(ParsedElement arc, double arrowHeadLength, double startArrowAngle) {
        SegmentConfiguration resultingConfiguration;
        Point startBasePoint = GeometricOperations.offset(arrowStart, startArrowAngle, -arrowHeadLength);
        Point endBasePoint = GeometricOperations.offset(arrowEnd, startArrowAngle, arrowHeadLength);
        
        Point beginSegmentPoint = arrowStart;
        Point endSegmentPoint = arrowEnd;
        
        if (!arc.getAttribute(ParseElementDefinition.ARROW_HEAD_TYPE).equals(ParseElementDefinition.ARROW_ANGLE)) {
            if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL)) {
                endSegmentPoint = endBasePoint;
            }
            if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD)) {
                beginSegmentPoint = startBasePoint;
            }
        }
        
        resultingConfiguration = new SegmentConfiguration(beginSegmentPoint, endSegmentPoint);
        resultingConfiguration.setStrokeWidth(getWidth());
        resultingConfiguration.setDashed(isDashed());
        resultingConfiguration.setDashLength(hashSpacing);
        
        return resultingConfiguration;
    }
    
    /**
     * This method processes straight arcs which are part of an Equilibrium arrows.
     */
    private Point[] processStraightEquilibriumArrow(double arrowHeadLength, double startArrowAngle, double arrowLength) {
        Point[] arrowHeadsAndTailsPoints = new Point[ARROW_HEADS_AND_TAILS_NUMBER_OF_POINTS];
        
        Point headLeft = arrowStart;
        Point headRight = arrowStart;
        Point tailLeft = arrowEnd;
        Point tailRight = arrowEnd;
        
        //extend the arrowStart and arrowEnd
        arrowStart = GeometricOperations.offset(arrowStart, startArrowAngle, shaftSpacing);
        arrowEnd = GeometricOperations.offset(arrowEnd, startArrowAngle, -shaftSpacing);
        
        //if the arrowhead is solid then I should extend the shaft which belongs to it
        if(!arrowHeadType.equals(ParseElementDefinition.ARROW_ANGLE)){
            headLeft = arrowStart;
            tailRight = arrowEnd;
        }
        
        arrowHeadsAndTailsPoints[ARROW_HEAD_LEFT] = headLeft;
        arrowHeadsAndTailsPoints[ARROW_TAIL_LEFT] = tailLeft;
        arrowHeadsAndTailsPoints[ARROW_HEAD_RIGHT] = headRight;
        arrowHeadsAndTailsPoints[ARROW_TAIL_RIGHT] = tailRight;
        
        return arrowHeadsAndTailsPoints;
    }
    
    /**
     * This method processes straight arcs which are part of an Unbalanced Equilibrium arrows.
     */
    private Point[] processStraightUnbalancedEquilibriumArrow(double arrowHeadLength, double startArrowAngle, double arrowLength) {
        Point[] arrowHeadsAndTailsPoints = new Point[ARROW_HEADS_AND_TAILS_NUMBER_OF_POINTS];
        
        Point headLeft = arrowStart;
        Point headRight = arrowStart;
        Point tailLeft = arrowEnd;
        Point tailRight = arrowEnd;
        
        Point startBasePoint = GeometricOperations.offset(arrowStart, startArrowAngle, -arrowHeadLength);
        double alongShaft = GeometricOperations.distance(arrowStart, arrowEnd) / 2 - GeometricOperations.distance(arrowStart, startBasePoint) / 2;
        double equilibriumProportion;
        
        //taken from C++ code
        if (arrowEquilibriumRatio < 1) {
            equilibriumProportion = (1 - arrowEquilibriumRatio);
            alongShaft *= equilibriumProportion;
            double tailOffset = arrowLength / 2 * equilibriumProportion;
            
            if (arrowheadHead.equals(ParseElementDefinition.ARROW_HALF_LEFT)) {
                alongShaft *= -1;
                equilibriumAtHead = true;
                headLeft = GeometricOperations.offset(headLeft, directionAngle, -alongShaft);
                tailLeft = GeometricOperations.offset(tailLeft, directionAngle, -tailOffset);
            } else {
                directionAngle = GeometricOperations.angle(arrowEnd, arrowStart);
                headRight = GeometricOperations.offset(headLeft, directionAngle, -tailOffset);
                tailRight = GeometricOperations.offset(tailLeft, directionAngle, alongShaft);
            }
        } else {
            if (arrowEquilibriumRatio > 0) {
                equilibriumProportion = (1 - 1 / arrowEquilibriumRatio);
                alongShaft *= equilibriumProportion;
            }
            if (arrowheadHead.equals(ParseElementDefinition.ARROW_HALF_RIGHT)) {
                directionAngle = GeometricOperations.angle(arrowEnd, arrowStart);
                alongShaft *= -1;
                equilibriumAtHead = true;
                headLeft = GeometricOperations.offset(headLeft, directionAngle, alongShaft);
                tailLeft = GeometricOperations.offset(tailLeft, directionAngle, -alongShaft);
            } else {
                headRight = GeometricOperations.offset(headLeft, directionAngle, alongShaft);
                tailRight = GeometricOperations.offset(tailLeft, directionAngle, -alongShaft);
            }
        }
        arrowHeadsAndTailsPoints[ARROW_HEAD_LEFT] = headLeft;
        arrowHeadsAndTailsPoints[ARROW_TAIL_LEFT] = tailLeft;
        arrowHeadsAndTailsPoints[ARROW_HEAD_RIGHT] = headRight;
        arrowHeadsAndTailsPoints[ARROW_TAIL_RIGHT] = tailRight;
        
        displacedOffset = alongShaft;
        
        return arrowHeadsAndTailsPoints;
    }
    
    /**
     * This method processes straight arcs which are part of an Retrosynthetic arrows.
     */
    private Point[] processStraightRetrosyntheticArrow(ParsedElement arc) {
        Point[] arrowHeadsAndTailsPoints = new Point[ARROW_HEADS_AND_TAILS_NUMBER_OF_POINTS];
        
        Point headLeft = arrowStart;
        Point headRight = arrowStart;
        Point tailLeft = arrowEnd;
        Point tailRight = arrowEnd;
        
        if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD)) {
            if(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_HEAD).equalsIgnoreCase(
                    ParseElementDefinition.ARROW_TYPE_FULL)){
                headLeft = GeometricOperations.offset(headLeft, directionAngle, shaftSpacing);
                headRight = GeometricOperations.offset(headRight, directionAngle, shaftSpacing);
            }else{
                //if the retrosynthetic arrow has a half arrowhead
                //then the oposite shaft should continue to the arrowStart point
                if(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_HEAD).equalsIgnoreCase(
                        ParseElementDefinition.ARROW_HALF_LEFT)){
                    headLeft = GeometricOperations.offset(headLeft, directionAngle, shaftSpacing);
                }
                if(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_HEAD).equalsIgnoreCase(
                        ParseElementDefinition.ARROW_HALF_RIGHT)){
                    headRight = GeometricOperations.offset(headRight, directionAngle, shaftSpacing);
                }
            }
        }
        
        if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL)) {
            if(arc.getAttribute(ParseElementDefinition.ARROW_HEAD_TAIL).equalsIgnoreCase(
                    ParseElementDefinition.ARROW_TYPE_FULL)){
                tailLeft = GeometricOperations.offset(tailLeft, directionAngle, -shaftSpacing);
                tailRight = GeometricOperations.offset(tailRight, directionAngle, -shaftSpacing);
            }else{
                //if the retrosynthetic arrow has a half arrowhead
                //then the oposite shaft should continue to the arrowEnd point
                if(!arc.getAttribute(ParseElementDefinition.ARROW_HEAD_TAIL).equalsIgnoreCase(
                        ParseElementDefinition.ARROW_HALF_LEFT)){
                    tailLeft = GeometricOperations.offset(tailLeft, directionAngle, -shaftSpacing);
                }
                if(!arc.getAttribute(ParseElementDefinition.ARROW_HEAD_TAIL).equalsIgnoreCase(
                        ParseElementDefinition.ARROW_HALF_RIGHT)){
                    tailRight = GeometricOperations.offset(tailRight, directionAngle, -shaftSpacing);
                }
            }
        }
        
        arrowHeadsAndTailsPoints[ARROW_HEAD_LEFT] = headLeft;
        arrowHeadsAndTailsPoints[ARROW_TAIL_LEFT] = tailLeft;
        arrowHeadsAndTailsPoints[ARROW_HEAD_RIGHT] = headRight;
        arrowHeadsAndTailsPoints[ARROW_TAIL_RIGHT] = tailRight;
        
        return arrowHeadsAndTailsPoints;
    }
    
    /**
     * This method gets the right and left heads for curved Equilibrium arrows.
     */
    private Point [] calculateEquilibriumArrowHeadPoints() {
        Point[] headPoints = new Point[EQUILIBRIUM_ARROW_HEAD_NUMBER_OF_POINTS];
        // When the angle is grater than zero degrees the arcs must be invert
        headPoints[EQUILIBRIUM_ARROW_LEFT_HEAD] = GeometricOperations.offset(
                arrowStart, directionAngle, (angularSize > 0 ? getShaftSpacing() : -getShaftSpacing()));
        // When the angle is smaller or equal than zero degrees the arcs must be equals
        headPoints[EQUILIBRIUM_ARROW_RIGHT_HEAD] = GeometricOperations.offset(
                arrowStart, directionAngle, ((angularSize > 0 ? -getShaftSpacing() : getShaftSpacing())));
        
        return headPoints;
    }
    
    /**
     * This method sets right or left configurations for curved equilibrium arrows.
     */
    private SplineConfiguration getCurvedEquilibriumArrowConfiguration(List<SegmentConfiguration> segments) {
        SplineConfiguration configuration = new SplineConfiguration(segments);
        configuration.setStrokeWidth(getWidth());
        configuration.setLineCap(LineCap.Butt);
        configuration.setDashed(isDashed());
        configuration.setDashLength(hashSpacing);
        
        return configuration;
    }
    
    /**
     * This method calculates head lenght for straight arrows.
     */
    private double calculateStraightArrowHeadLenght() {
        double result = 0;
        boolean getArrowWid = false;
        //Limit arc length to 2/3 of entire arrow for straight arrows
        result = calculateProportionalArrowHeadLengthAndWid(getArrowWid);
        //If the arc will be a part of an arrow, extend it to join the arrowhead
        result -= ARC_EXTEND_SIZE;
        
        return result;
    }
    
    /**
     * This method calculates an arrow head lenght and an arrow width (arrowid) proportional to arrow lenght.
     * When the arrow length is less than the arrowCenterSize,
     * the value of the arrowCenterSize must be 2/3 of the arrow lenght
     * NOTE: arrowid is the name used for "arroWith" in the source file (for example: CDXML files).
     */
    protected double calculateProportionalArrowHeadLengthAndWid(boolean getArrowWid) {
        double result = 0;
        //use the line width property instead of the actual width of the arc
        double arrowHeadLength = arrowCenterSize * getLineWidth();
        double arrowWid = Math.abs(arrowHeadSize) * getLineWidth();
        double arrowLength = GeometricOperations.distance(arrowStart, arrowEnd);
        double proportionalFactor = (2 * arrowLength) / 3;
        if (arrowLength > 0 && arrowWid > proportionalFactor) {
            arrowHeadLength = arrowHeadLength * proportionalFactor / arrowWid;
            arrowWid = proportionalFactor;
        }
        
        if(getArrowWid){
            result = arrowWid;
        }else{
            result = arrowHeadLength;
        }
        
        return result;
    }
    
    /**
     * This method gets one ending configuration of a straight arrow which have shaft spacing (Retrosynthetic and Equilibrium arrows).
     */
    private SegmentConfiguration getStraightArrowWithShaftSpacingConfiguration(ParsedElement arc, double arrowHeadLength, double startArrowAngle,
            Point headPoint, Point tailPoint, boolean isLeftConfiguration) {
        Point startBasePoint = GeometricOperations.offset(headPoint, startArrowAngle, -arrowHeadLength);
        Point endBasePoint = GeometricOperations.offset(tailPoint, startArrowAngle, arrowHeadLength);
        
        Point segmentBeginPoint = headPoint;
        Point segmentEndPoint = tailPoint;
        
        if (!arc.getAttribute(ParseElementDefinition.ARROW_HEAD_TYPE).equals(ParseElementDefinition.ARROW_ANGLE)) {
            if (arc.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD)) {
                if (isLeftConfiguration) {
                    segmentBeginPoint = startBasePoint;
                } else {
                    segmentEndPoint = endBasePoint;
                }
            }
        }
        SegmentConfiguration resultingConfiguration = new SegmentConfiguration(segmentBeginPoint, segmentEndPoint);
        resultingConfiguration.setStrokeWidth(getWidth());
        resultingConfiguration.setDashed(isDashed());
        resultingConfiguration.setDashLength(hashSpacing);
        
        return resultingConfiguration;
    }
    
    /**
     * This method calculates the base point for a curved arrow.
     */
    protected Point calculateBasePoint(Point startPoint, Point endPoint, Point centerPoint, double angularSize, double centerSize, boolean startHead){
        Point basePoint = new Point();
        
        double radius = GeometricOperations.distance(startPoint, centerPoint);
        double desiredDistance = centerSize * getLineWidth();
        
        //Take from C++ code
        double maxArrowHeadAngle = Math.PI / 180 * angularSize / 3;	// Limit arrowhead length to 1/3 of entire arrow for straight arrows
        double headAngleDivisor = Math.sqrt(Math.max(0.0, radius * radius - (desiredDistance * desiredDistance / 4)));
        double arrowHeadAngle = 2 * Math.atan2(desiredDistance / 2, headAngleDivisor);
        
        if (arrowHeadAngle > Math.abs(maxArrowHeadAngle)){
            arrowHeadAngle = Math.abs(maxArrowHeadAngle);
        }
        
        if (maxArrowHeadAngle < 0){
            arrowHeadAngle = -arrowHeadAngle;
        }
        
        Point arrowEnd = getMatrix().transform(centerPoint);
        Point arrowStart = getMatrix().transform(startPoint);
        
        int direction = 0;
        if (startHead){
            basePoint = calculateBasePointWithDerivatives(startPoint, angularSize, basePoint, desiredDistance,
                    maxArrowHeadAngle, arrowHeadAngle, arrowEnd, arrowStart, startHead);
        } else {
            arrowHeadAngle = 2 * Math.atan2(desiredDistance / 2, headAngleDivisor);
            
            if (arrowHeadAngle > Math.abs(maxArrowHeadAngle)){
                arrowHeadAngle = Math.abs(maxArrowHeadAngle);
            }
            
            if (maxArrowHeadAngle < 0){
                arrowHeadAngle = -arrowHeadAngle;
            }
            
            arrowHeadAngle = angularSize * Math.PI / 180 - arrowHeadAngle;
            basePoint = calculateBasePointWithDerivatives(endPoint, angularSize, basePoint, desiredDistance,
                    maxArrowHeadAngle, arrowHeadAngle, arrowEnd, arrowStart, startHead);
        }
        
        return basePoint;
    }
    
    /**
     * This method gets the base point calculating derivatives
     * from the start arrow point or the end arrow point.
     */
    private Point calculateBasePointWithDerivatives(Point headPoint, double angularSize, Point basePoint,
            double desiredDistance, double maxArrowHeadAngle, double obtainedArrowHeadAngle ,Point arrowEnd, Point arrowStart, boolean isStartHead) {
        int direction = 0;
        double apparentDistance = 0;
        double arrowHeadAngle = obtainedArrowHeadAngle;
        
        do
        {
            basePoint.setX(arrowEnd.getX() + Math.cos(arrowHeadAngle) * (arrowStart.getX() - arrowEnd.getX())
            - Math.sin(arrowHeadAngle) * (arrowStart.getY() - arrowEnd.getY()));
            basePoint.setY(arrowEnd.getY() + Math.sin(arrowHeadAngle) * (arrowStart.getX() - arrowEnd.getX())
            + Math.cos(arrowHeadAngle) * (arrowStart.getY() - arrowEnd.getY()));
            basePoint.setZ(arrowStart.getZ());
            
            basePoint = getInverseMatrix().transform(basePoint);
            
            apparentDistance = GeometricOperations.distance(basePoint, headPoint);
            
            if (direction == 0) {
                if (apparentDistance < APPARENT_DISTANCE_COMPARISON_PARAM_1 * desiredDistance) {
                    direction = (angularSize > 0 ? 1 : -1);
                } else if (apparentDistance > APPARENT_DISTANCE_COMPARISON_PARAM_2 * desiredDistance) {
                    direction = (angularSize > 0 ? -1 : 1);
                }
            }
            
            if ((apparentDistance < APPARENT_DISTANCE_COMPARISON_PARAM_1 * desiredDistance && ((angularSize > 0) == (direction > 0)))
            || (apparentDistance > APPARENT_DISTANCE_COMPARISON_PARAM_2 * desiredDistance && ((angularSize > 0) != (direction > 0)))) {
                if (isStartHead) {
                    arrowHeadAngle += (Math.PI / 180) * direction;
                } else {
                    arrowHeadAngle -= (Math.PI / 180) * direction;
                }
            } else {
                break;
            }
        }
        while (isStartHead ?
            (Math.abs(arrowHeadAngle) > Math.PI / 180 && Math.abs(arrowHeadAngle) < Math.abs(maxArrowHeadAngle))
            : (Math.abs(angularSize * Math.PI / 180 - arrowHeadAngle) > Math.PI / 180 && Math.abs(angularSize * Math.PI / 180 - arrowHeadAngle) < Math.abs(maxArrowHeadAngle)));
        
        return basePoint;
    }
    
    /**
     * This method creates a curved arc as an spline using bezier curves.
     */
    protected List<SegmentConfiguration> createArcAsSpline(Point headPoint, Point centerPoint, double angularSize, double startAngleOffset, boolean start, boolean end){
        
        return buildSegmentBeziers(createArc(headPoint, centerPoint, angularSize, startAngleOffset, start, end));
    }
    
    /**
     * This method creates a curved arc.
     */
    protected List<Point> createArc(Point headPoint, Point centerPoint, double angularSize, double startAngleOffset, boolean start, boolean end){
        
        //Calculate the major and minor axis end points doing an offset from the arrow center.
        if(!getElement().hasAttribute(ParseElementDefinition.ARROW_MAJOR_AXIS_END_3D)){
            majorAxisEnd = GeometricOperations.offset(arrowCenter, MAJOR_AXIS_ANGLE_OFFSET, GeometricOperations.distance(headPoint, centerPoint));
        }
        if(!getElement().hasAttribute(ParseElementDefinition.ARROW_MINOR_AXIS_END_3D)){
            minorAxisEnd = GeometricOperations.offset(arrowCenter, MINOR_AXIS_ANGLE_OFFSET, GeometricOperations.distance(headPoint, centerPoint));
        }
        
        Point transformedHead = getMatrix().transform(headPoint);
        Point transformedCenter = getMatrix().transform(centerPoint);
        
        double arrowRadius = GeometricOperations.distance(transformedCenter, transformedHead);
        double startAngle = Math.toDegrees(Math.atan2(transformedHead.getY() - transformedCenter.getY(), transformedHead.getX() - transformedCenter.getX()));
        
        boolean isEnd = true;      //isEnd is a flag which helps to know if we are working with the end or the start part of the arrow
        if(end) {
            double tailAngle = startAngle + angularSize;
            double tailArrowAngle = calculateTailOrHeadArrowAngle(centerPoint, angularSize, isEnd);
            //the angle difference is calculated between the tail angle and the tail arrow angle
            angularSize = calculateAngularSize(angularSize, tailAngle, tailArrowAngle, isEnd);
        }
        if(start) {
            isEnd = false;
            double headArrowAngle = calculateTailOrHeadArrowAngle(centerPoint, angularSize, isEnd);
            //the angle difference is calculated between the head arrow angle and the start angle
            angularSize = calculateAngularSize(angularSize, startAngle, headArrowAngle, isEnd);
            startAngle = headArrowAngle;
        }
        
        startAngle += startAngleOffset;
        
        Point edge = new Point(transformedCenter.getX() + arrowRadius, transformedCenter.getY());
        double perpendicularToStartAngle = startAngle + 90;
        List<Point> result = calculateTransformedArcPoints(transformedCenter, edge, perpendicularToStartAngle, angularSize);
        if(result.size() > 0){
            for (Point arcPoint : result) {
                Point transformedArcPoint = getInverseMatrix().transform(arcPoint);
                arcPoint.setX(transformedArcPoint.getX());
                arcPoint.setY(transformedArcPoint.getY());
            }
        }
        return result;
    }
    
    /**
     * This method calculates the arc angular size.
     */
    private double calculateAngularSize(double angularSize, double endAngle, double arrowAngle, boolean isEnd) {
        double angleDifference = 0;
        if (isEnd) {
            // when it´s the end arrow part, the difference is between the tail angle and the tail arrow angle
            angleDifference = endAngle - arrowAngle;
        } else {
            // when it´s the start arrow part, the difference is between the head arrow angle and the start angle
            angleDifference = arrowAngle - endAngle;
        }
        while (angleDifference <= -180)
            angleDifference += 360;
        while (angleDifference > 180)
            angleDifference -= 360;
        angularSize -= angleDifference;
        
        return angularSize;
    }
    
    /**
     * This method calculates the angle formed between the tail or head point and the major axis.
     */
    private double calculateTailOrHeadArrowAngle(Point centerPoint, double angularSize, boolean isEnd) {
        boolean startedHead = true;
        if (isEnd) {
            startedHead = false;
        }
        
        //If the arc will be a part of an arrow, you need to extend it to join the arrowhead
        double arrowheadCenterSize = arrowCenterSize - ARC_EXTEND_SIZE;
        
        Point baseArrowPoint = getMatrix().transform(calculateBasePoint(arrowStart, arrowEnd, arrowCenter, angularSize, arrowheadCenterSize, startedHead));
        double tailOrHeadArrowAngle = Math.toDegrees(Math.atan2(baseArrowPoint.getY() - centerPoint.getY(), baseArrowPoint.getX() - centerPoint.getX()));
        return tailOrHeadArrowAngle;
    }
    
    /**
     * This method calculates transformed arc points
     */
    protected List<Point> calculateTransformedArcPoints(Point center, Point edge, double startAngle, double totalAngle) {
        double eccentricity = 1;
        // First, pretend you need a complete circle (360 degrees)
        List<Point> result = calculateTransformedOvalPoints(center, edge, eccentricity);
        if (totalAngle < 0) {
            totalAngle = -totalAngle;
        } else {
            startAngle += totalAngle;
        }
        double totalAngleRadians = Math.toRadians(totalAngle);
        while (Math.abs(totalAngleRadians) > 2 * Math.PI) {
            if (totalAngleRadians < 0) {
                totalAngleRadians += 2 * Math.PI;
            } else {
                totalAngleRadians -= 2 * Math.PI;
            }
        }
        // The circle is calculated with four points at 0, 90, 180, 270.
        // Delete or add extra points as needed for the size that the arc really is
        int end = 0;
        int start = 0;
        if (totalAngleRadians <= Math.PI / 2) {
            // Remove points from 6 on
            end = 6;
            result = result.subList(start, end);
        } else if (totalAngleRadians <= Math.PI) {
            // Remove points from 9 on
            end = 9;
            result = result.subList(start, end);
        } else if (totalAngleRadians > Math.PI * 3 / 2) {
            for (int i = 0; i < 3; i++) {
                result.add(new Point());
            }
        }
        
        result = calculateParametricEquationDescribingBezierCurve(center, edge, result, totalAngleRadians);
        
        // Finally, rotate the entire arc so that it starts at the desired startAngle.
        AffineTransform transformation = new AffineTransform();
        transformation.translate(-center.getX(), -center.getY());
        transformation.rotate(Math.toRadians(90 + startAngle));
        transformation.translate(center.getX(), center.getY());
        double[] transformationMatrix = new double[6];
        transformation.getMatrix(transformationMatrix);
        transformationMatrix[4] *= -1;
        transformationMatrix[5] *= -1;
        if(result.size() > 0) {
            for (Point resultPoint : result) {
                double x = resultPoint.getX();
                double y = resultPoint.getY();
                
                resultPoint.setX(x * transformationMatrix[0] + y * transformationMatrix[2] + transformationMatrix[4]);
                resultPoint.setY(x * transformationMatrix[1] + y * transformationMatrix[3] + transformationMatrix[5]);
            }
        }
        return result;
    }
    
    /**
     * This method solves the parametric equations describing the Bezier curve
     */
    private List<Point> calculateParametricEquationDescribingBezierCurve(Point center, Point edge, List<Point> result, double totalAngleRadians){
        //Here result variable is modified because below references are passed
        Point splinePoint0 = result.get(result.size() - 5);
        Point splinePoint1 = result.get(result.size() - 4);
        Point splinePoint2 = result.get(result.size() - 3);
        Point splinePoint3 = result.get(result.size() - 2);
        Point splinePoint4 = result.get(result.size() - 1);
        
        // Solve the parametric equations describing the Bezier curve:
        //   x(t) = ax * t^3 + bx * t^2 + cx * t + x0
        //   y(t) = ay * t^3 + by * t^2 + cy * t + y0
        // Actually, you know that the two control handles describing the last segment
        // of the curve are the same distance from their points, since the curve is symmetric.
        // Also, you know that they are tangent to the curve at each point.
        // Accordingly, calculate the equivalent arc starting at 0 degrees and with unit radius
        // In that case, pt0 = (1,0) and pt1 = (1,tangentDist).  All you need then is tangentDist.
        // FYI - http://www.spaceroots.org/documents/ellipse/elliptical-arc.pdf
        double totalAngleRadiansAcute = totalAngleRadians;
        while (totalAngleRadiansAcute > Math.PI / 2) {
            totalAngleRadiansAcute -= Math.PI / 2;
        }
        Point pointOne = new Point(Math.cos(totalAngleRadiansAcute), Math.sin(totalAngleRadiansAcute));
        Point pointHalf = new Point(Math.cos(totalAngleRadiansAcute / 2), Math.sin(totalAngleRadiansAcute / 2));
        Point pointQuarter = new Point(Math.cos(totalAngleRadiansAcute / 4), Math.sin(totalAngleRadiansAcute / 4));
        Point pointZero = new Point(1, 0);
        double ay, by, cy;     //this are parameters to solve the equation which describes the Bezier curve.
        
        by = 14 * pointZero.getY() - 2 * pointOne.getY() + 20 * pointHalf.getY() - 32 * pointQuarter.getY();
        ay = (4 * pointOne.getY() + 4 * pointZero.getY() - 8 * pointHalf.getY() - 2 * by) / 3;
        cy = pointOne.getY() - pointZero.getY() - ay - by;
        
        double centerEdgeDistance = GeometricOperations.distance(center, edge);
        double tangentDistance = Math.abs(cy / 3 * centerEdgeDistance);
        
        // Apply the calculated tangentDistance to figure out the location of the control points.
        double angle = Math.atan2(splinePoint0.getY() - center.getY(), splinePoint0.getX() - center.getX()) - Math.PI / 2;
        splinePoint1.setX(splinePoint0.getX() + Math.cos(angle) * tangentDistance);
        splinePoint1.setY(splinePoint0.getY() + Math.sin(angle) * tangentDistance);
        splinePoint3.setX(center.getX() - Math.cos(totalAngleRadians) * centerEdgeDistance);
        splinePoint3.setY(center.getY() + Math.sin(totalAngleRadians) * centerEdgeDistance);
        angle = totalAngleRadians - Math.PI / 2;
        splinePoint2.setX(splinePoint3.getX() - Math.cos(angle) * tangentDistance);
        splinePoint2.setY(splinePoint3.getY() + Math.sin(angle) * tangentDistance);
        splinePoint4.setX(splinePoint3.getX() + Math.cos(angle) * tangentDistance);
        splinePoint4.setY(splinePoint3.getY() - Math.sin(angle) * tangentDistance);
        
        return result;
    }
    
    
    /**
     * This method obtains the middle point of the spline.
     */
    protected Point getMidPoint(boolean isArc, List<Point> splinePoints) {
        Point result = null;
        
        if (!isArc) {
            result = arrowStart.add(arrowEnd).byScalar(MID_POINT_SCALAR);
        }else{
            AffineTransform transformation = new AffineTransform();
            Point transformedCenter = getMatrix().transform(center);
            transformation.translate(-transformedCenter.getX(), -transformedCenter.getY());
            transformation.rotate(Math.toRadians(angularSize / 2));
            transformation.translate(transformedCenter.getX(), transformedCenter.getY());
            Point transformedHead = getMatrix().transform(arrowStart);
            Point2D transformedHead2D = new Point2D.Double(transformedHead.getX(), transformedHead.getY());
            double[] flatMatrix = new double[6];
            transformation.getMatrix(flatMatrix);
            flatMatrix[4] *= -1;
            flatMatrix[5] *= -1;
            transformation.setTransform(flatMatrix[0], flatMatrix[1], flatMatrix[2], flatMatrix[3], flatMatrix[4], flatMatrix[5]);
            transformation.transform(transformedHead2D, transformedHead2D);
            transformedHead.setX(transformedHead2D.getX());
            transformedHead.setY(transformedHead2D.getY());
            Point mathPoint = getInverseMatrix().transform(transformedHead);
            
            int splineCount = (splinePoints.size() - 1) / 3;
            List<Point> midCurvePoints;
            int mid = splinePoints.size() / 2;
            
            // obtain 1000 points per spline
            if (!AlgebraicOperations.isOdd(splineCount)) {
                // if even number of segments, use 2 middle beziers points
                // the first spline is from the half point index to the next four indexes
                // the second spline is from 3 indexes before the middle point to the next four indexes
                midCurvePoints = AlgebraicOperations.bezierFunction(SPLINE_POINTS_PRECISION,
                        splinePoints.subList(mid, mid + 4));
                
                midCurvePoints.addAll(0, AlgebraicOperations.bezierFunction(SPLINE_POINTS_PRECISION,
                        splinePoints.subList(mid - 3, mid + 1)));
            }else{
                // the middle spline is compose by the 4 middle points of the array of spline points
                midCurvePoints = AlgebraicOperations.bezierFunction(SPLINE_POINTS_PRECISION,
                        splinePoints.subList(mid - 2, mid + 2));
            }
            
            int midIndex = midCurvePoints.size() / 2;
            int offset = 0;
            double shortestDistance = -1;
            int direction = 0;
            while (midIndex + direction * offset > 0 && midIndex + direction * offset < midCurvePoints.size()) {
                double distance;
                if (direction == 0) {
                    if (shortestDistance == -1) {
                        shortestDistance = GeometricOperations.distance(midCurvePoints.get(midIndex), mathPoint);
                    } else {
                        double leftDistance = GeometricOperations.distance(midCurvePoints.get(midIndex + offset), mathPoint);
                        double rightDistance = GeometricOperations.distance(midCurvePoints.get(midIndex - offset), mathPoint);
                        if (leftDistance > shortestDistance && rightDistance > shortestDistance) {
                            break;
                        }
                        if (leftDistance < rightDistance) {
                            shortestDistance = leftDistance;
                            direction = 1;
                        } else {
                            shortestDistance = rightDistance;
                            direction = -1;
                        }
                    }
                } else {
                    distance = GeometricOperations.distance(midCurvePoints.get(midIndex + direction * offset), mathPoint);
                    if (distance > shortestDistance) {
                        break;
                    } else {
                        shortestDistance = distance;
                    }
                }
                offset++;
            }
            
            result = new Point(midCurvePoints.get(midIndex + direction * offset));
        }
        return result;
    }
    
    /**
     * This method builds a collection configuration of No Go arrow parts.
     */
    protected Collection<ShapeBuilderConfiguration> getNoGoParts(String arrowNoGo, double arrowHeadWidth){
        List<ShapeBuilderConfiguration> nogoParts = new ArrayList();
        
        boolean isArc = getElement().hasAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE);
        
        boolean start = getElement().hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD);
        boolean end = getElement().hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL);
        
        Point midPoint = getMidPoint(isArc, createArc(arrowStart, center, angularSize, 0, start, end));
        
        double arcDeltaX = arrowStart.getX() - arrowEnd.getX();
        double arcDeltaY = arrowStart.getY() - arrowEnd.getY();
        double length = GeometricOperations.distance(arcDeltaX, arcDeltaY);
        
        if (length > 0) {
            double newScale = ARROW_NO_GO_SCALE_FACTOR * arrowHeadWidth / length;
            arcDeltaX *= newScale;
            arcDeltaY *= newScale;
        }
        double crossDeltaX = -arcDeltaY;
        double crossDeltaY = arcDeltaX;
        // build a No Go Cross arrow configuration
        if (arrowNoGo.equalsIgnoreCase(
                ParseElementDefinition.ARROW_NO_GO_CROSS)) {
            SegmentConfiguration crossLine2 = getNoGoCrossArrowConfiguration(nogoParts, midPoint,
                    arcDeltaX, arcDeltaY, crossDeltaX, crossDeltaY);
            nogoParts.add(crossLine2);
            
        }else if(arrowNoGo.equalsIgnoreCase(
                ParseElementDefinition.ARROW_NO_GO_HASH)) {
            SegmentConfiguration hashLine2 = getNoGoHashArrowConfiguration(nogoParts, midPoint,
                    arcDeltaX, arcDeltaY, crossDeltaX, crossDeltaY);
            nogoParts.add(hashLine2);
        }
        
        return nogoParts;
        
    }
    
    /**
     * This method gets a No Go Cross arrow.
     */
    private SegmentConfiguration getNoGoCrossArrowConfiguration(List<ShapeBuilderConfiguration> nogoParts, Point midPoint,
            double arcDeltaX, double arcDeltaY, double crossDeltaX, double crossDeltaY) {
        Point crossLine1Begin = new Point(midPoint.getX() + arcDeltaX + crossDeltaX, midPoint.getY() + arcDeltaY + crossDeltaY);
        Point crossLine1End = new Point(midPoint.getX() - arcDeltaX - crossDeltaX, midPoint.getY() - arcDeltaY - crossDeltaY);
        SegmentConfiguration crossLine1 = new SegmentConfiguration(crossLine1Begin, crossLine1End);
        crossLine1.setStrokeWidth(getWidth());
        crossLine1.setColor(color);
        nogoParts.add(crossLine1);
        
        Point crossLine2Begin = new Point(midPoint.getX() - arcDeltaX + crossDeltaX, midPoint.getY() - arcDeltaY + crossDeltaY);
        Point crossLine2End = new Point(midPoint.getX() + arcDeltaX - crossDeltaX, midPoint.getY() + arcDeltaY - crossDeltaY);
        SegmentConfiguration crossLine2 = new SegmentConfiguration(crossLine2Begin, crossLine2End);
        crossLine2.setStrokeWidth(getWidth());
        crossLine2.setColor(color);
        
        return crossLine2;
    }
    
    /**
     * This method gets a No Go Hash arrow.
     */
    private SegmentConfiguration getNoGoHashArrowConfiguration(List<ShapeBuilderConfiguration> nogoParts, Point midPoint,
            double arcDeltaX, double arcDeltaY, double crossDeltaX, double crossDeltaY) {
        Point hashLine1Begin = new Point(midPoint.getX() + arcDeltaX - crossDeltaX, midPoint.getY() + arcDeltaY - crossDeltaY);
        Point hashLine1End = new Point(midPoint.getX() + crossDeltaX, midPoint.getY() + crossDeltaY);
        SegmentConfiguration hashLine1 = new SegmentConfiguration(hashLine1Begin, hashLine1End);
        hashLine1.setStrokeWidth(getWidth());
        hashLine1.setColor(color);
        nogoParts.add(hashLine1);
        
        Point hashLine2Begin = new Point(midPoint.getX() - crossDeltaX, midPoint.getY() - crossDeltaY);
        Point hashLine2End = new Point(midPoint.getX() - arcDeltaX + crossDeltaX, midPoint.getY() - arcDeltaY + crossDeltaY);
        SegmentConfiguration hashLine2 = new SegmentConfiguration(hashLine2Begin, hashLine2End);
        hashLine2.setStrokeWidth(getWidth());
        hashLine2.setColor(color);
        return hashLine2;
    }
    
    /**
     * This method is used to calculate the arrow end point using the
     *arrow start point, the arrow center point and the angular size.
     */
    private Point calculateArrowEnd(Point arrowStart, Point arrowCenter, double angularSize) {
        double pointsAngle = GeometricOperations.angle(arrowCenter, arrowStart);
        double pointsDistance = GeometricOperations.distance(arrowCenter, arrowStart);
        double offsetAngle = pointsAngle + angularSize;
        return GeometricOperations.offset(arrowCenter, offsetAngle, pointsDistance);
    }
    
    protected String getTypeString() {
        if (lineType == null) {
            return StringUtils.EMPTY_STRING;
        } else {
            return lineType;
        }
    }
    
    private Point getTransformedTailPoint() {
        AffineTransform transformation = new AffineTransform();
        Point transformedCenter = getMatrix().transform(center).byScalar(-1);
        transformation.translate(transformedCenter.getX(), transformedCenter.getY());
        transformation.rotate(Math.toRadians(getAngularSize()));
        transformedCenter = getMatrix().transform(center);
        transformation.translate(transformedCenter.getX(), transformedCenter.getY());
        Point transformedHead = getMatrix().transform(arrowStart);
        Point2D transformableHead = new Point2D.Double(transformedHead.getX(), transformedHead.getY());
        transformation.transform(transformableHead, transformableHead);
        transformedHead.setX(transformableHead.getX());
        transformedHead.setY(transformableHead.getY());
        return getInverseMatrix().transform(transformedHead);
    }
    
    protected Point getTailBase() {
        return null;
    }
    
    protected Point getHeadBase() {
        return null;
    }
    
    protected double getShaftSpacing() {
        return shaftSpacing;
    }
    
    protected boolean hasTailArrow() {
        return false; // implemented in ArrowProcessor
    }
    
    protected boolean hasHeadArrow() {
        return false; // implemented in ArrowProcessor
    }
    
    protected void setShaftSpacing(double shaftSpacing) {
        this.shaftSpacing = shaftSpacing;
    }
    
    public double getAngularSize() {
        return angularSize;
    }
    
    protected String getArrowType() {
        return arrowType;
    }
    
    public void setAngularSize(double angularSize) {
        this.angularSize = angularSize;
    }
    
    protected void setArrowType(String arrowType) {
        this.arrowType = arrowType;
    }
    
    protected double getDisplacedOffset() {
        return displacedOffset;
    }
    
    protected Point getHeadStartDisplaced() {
        return headStartDisplaced;
    }
    
    protected Point getHeadEndDisplaced() {
        return headEndDisplaced;
    }
    
}
