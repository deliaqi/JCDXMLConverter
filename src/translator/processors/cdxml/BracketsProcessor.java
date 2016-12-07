package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ArcConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.processors.Processor;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class BracketsProcessor extends CDXMLProcessor {
    
    private static final String BRACKET = "Bracket";
    private static final int BRACKET_DEFAULT_VALUE_ANGLE = 0;
    private static final double BRACE_LIP_SCALE_FACTOR = 1 - 0.55197;
    private static final int CURLY_BRACKET_OFFSET_FACTOR = 16;
    //This constant sets the the bracket lip angle
    private static final double BRACKET_AND_BRACE_LIP_ANGLE = Math.PI / 2;
    //This constant sets the the parenthesis angle
    private static final double PARANTHESIS_LIP_ANGLE = Math.PI / 3;
    
    private static final int UPPER_BELOW_BRACE_NUMBER_OF_POINTS = 4;
    private static final int CENTER_BRACE_NUMBER_OF_POINTS = 6;

    private static final int UPPER_BELOW_BRACE_FIRST_POINT = 0;
    private static final int UPPER_BELOW_BRACE_FISRT_CONTROL_POINT = 1;
    private static final int UPPER_BELOW_BRACE_SECOND_CONTROL_POINT = 2;
    private static final int UPPER_BELOW_BRACE_LAST_POINT = 3;

    private static final int CENTER_BRACE_CENTER_OUT_POINT = 0;
    private static final int CENTER_BRACE_CENTER_TOP_POINT = 1;
    private static final int CENTER_BRACE_CENTER_CONTROL_POINT_1 = 2;
    private static final int CENTER_BRACE_CENTER_CONTROL_POINT_2 = 3;
    private static final int CENTER_BRACE_CENTER_CONTROL_POINT_3 = 4;
    private static final int CENTER_BRACE_CENTER_BOTTOM_POINT = 5;
    
    private List<Point> boundingBox;
    private String bracketType;
    private Point beginPoint;
    private Point endPoint;
    
    public BracketsProcessor() {
    }
    
    public void process(){
        ParsedElement bracket = getElement();
        List<SegmentConfiguration> segments = new ArrayList();
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        ShapeBuilderConfiguration resultingConfiguration = null;
        SplineConfiguration configuration = null;
        bracketType = bracket.getAttribute(ParseElementDefinition.BRACKET_TYPE);
        boundingBox = parsePoints(getBoundingBox(), bracket);
        
        beginPoint = boundingBox.get(FIRST_ELEMENT);
        endPoint = boundingBox.get(SECOND_ELEMENT);
        double bracketSize = GeometricOperations.distance(beginPoint, endPoint);
        double bracketAngle = GeometricOperations.angle(beginPoint, endPoint);
        
        if(bracketType != null){
            if(bracketType.equalsIgnoreCase(ParseElementDefinition.BRACKET_TYPE_SQUARE)){
                segments = processSquareBracket(bracketSize, bracketAngle);
            }else if(bracketType.equalsIgnoreCase(ParseElementDefinition.BRACKET_TYPE_ROUND)){
                segments = processRoundedBracket(bracketSize, bracketAngle);
            }else if(bracketType.equalsIgnoreCase(ParseElementDefinition.BRACKET_TYPE_CURLY)){
                segments =  processCurlyBracket(bracketSize, bracketAngle);
            }
            
            configuration = new SplineConfiguration(segments);
            
            ((SplineConfiguration) configuration).setColor(color);
            ((SplineConfiguration) configuration).setStrokeWidth(lineWidth);
            ((SplineConfiguration) configuration).setLineJoin(LineJoin.Miter);
            innerShapes.add(configuration);
        }
        resultingConfiguration = new CompositeShapeConfiguration(BRACKET, innerShapes);
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(zOrder);
        setResultingConfiguration(resultingConfiguration);
    }
    
    /**
     * This method processes braces (curly brackets).
     */
    private List<SegmentConfiguration> processCurlyBracket(double bracketSize, double bracketAngle) {
        
        List<SegmentConfiguration> result = new ArrayList();
        //taken from C++ code
        //Here the bracket lip size is calculated.
        Point point = new Point(
                endPoint.getX() + (beginPoint.getY() - endPoint.getY()), endPoint.getY() + (beginPoint.getX() - endPoint.getX()));
        double angle = GeometricOperations.angle(point, endPoint);
        double offset = -(bracketSize / CURLY_BRACKET_OFFSET_FACTOR);
        point = GeometricOperations.offset(endPoint, angle, offset);
        double braceLipOffset = GeometricOperations.distance(point, endPoint);
        
        //obtain points for upper brace
        boolean isUpperBrace = true;
        Point [] upperBracePoints = obtainPointsForUpperOrBelowBrace(bracketAngle, braceLipOffset, isUpperBrace);
        
        //obtain points for center brace
        Point[] centerBracePoints = obtainPointsForCenterBrace(bracketSize, bracketAngle, braceLipOffset);
        
        //obtain points for below brace
        isUpperBrace = false;
        Point[] lowerBracePoints =  obtainPointsForUpperOrBelowBrace(bracketAngle, braceLipOffset, isUpperBrace);
        
        //create and add the Configurations for the Brace Bracket
        result.add(new CubicCurveConfiguration(upperBracePoints[UPPER_BELOW_BRACE_FIRST_POINT], 
                upperBracePoints[UPPER_BELOW_BRACE_LAST_POINT], 
                upperBracePoints[UPPER_BELOW_BRACE_FISRT_CONTROL_POINT], 
                upperBracePoints[UPPER_BELOW_BRACE_SECOND_CONTROL_POINT]));
        result.add(new SegmentConfiguration(upperBracePoints[UPPER_BELOW_BRACE_LAST_POINT], 
                centerBracePoints[CENTER_BRACE_CENTER_TOP_POINT]));
        result.add(new CubicCurveConfiguration(centerBracePoints[CENTER_BRACE_CENTER_TOP_POINT], 
                centerBracePoints[CENTER_BRACE_CENTER_OUT_POINT], 
                centerBracePoints[CENTER_BRACE_CENTER_CONTROL_POINT_1], 
                centerBracePoints[CENTER_BRACE_CENTER_CONTROL_POINT_2]));
        result.add(new CubicCurveConfiguration(centerBracePoints[CENTER_BRACE_CENTER_OUT_POINT], 
                centerBracePoints[CENTER_BRACE_CENTER_BOTTOM_POINT], 
                centerBracePoints[CENTER_BRACE_CENTER_CONTROL_POINT_2], 
                centerBracePoints[CENTER_BRACE_CENTER_CONTROL_POINT_3]));
        result.add(new SegmentConfiguration(centerBracePoints[CENTER_BRACE_CENTER_BOTTOM_POINT], 
                lowerBracePoints[UPPER_BELOW_BRACE_FIRST_POINT]));
        result.add(new CubicCurveConfiguration(lowerBracePoints[UPPER_BELOW_BRACE_FIRST_POINT], 
                lowerBracePoints[UPPER_BELOW_BRACE_LAST_POINT], 
                lowerBracePoints[UPPER_BELOW_BRACE_FISRT_CONTROL_POINT], 
                lowerBracePoints[UPPER_BELOW_BRACE_SECOND_CONTROL_POINT]));
        
        return result;
    }
    
    /**
     * This method obtains the all needed points to draw the upper brace or the lower brace.
     */
    private Point [] obtainPointsForUpperOrBelowBrace(double bracketAngle, double braceLipOffset, boolean isUpperBrace) {
        double braceLipAngle = bracketAngle + BRACKET_AND_BRACE_LIP_ANGLE;
        Point[] bracketPoints = new Point [UPPER_BELOW_BRACE_NUMBER_OF_POINTS];
        //First Point
        bracketPoints[UPPER_BELOW_BRACE_FIRST_POINT] = GeometricOperations.offset((isUpperBrace ? endPoint : beginPoint),
                (isUpperBrace ? braceLipAngle : bracketAngle),
                braceLipOffset);
        //First Control Point
        bracketPoints[UPPER_BELOW_BRACE_FISRT_CONTROL_POINT] = GeometricOperations.offset((isUpperBrace ? endPoint : beginPoint),
                (isUpperBrace ? braceLipAngle : bracketAngle),
                braceLipOffset * BRACE_LIP_SCALE_FACTOR);
        //Second Control Point
        bracketPoints[UPPER_BELOW_BRACE_SECOND_CONTROL_POINT] = GeometricOperations.offset((isUpperBrace ? endPoint : beginPoint),
                (isUpperBrace ? bracketAngle : braceLipAngle),
                (isUpperBrace ? (-braceLipOffset * BRACE_LIP_SCALE_FACTOR) : (braceLipOffset * BRACE_LIP_SCALE_FACTOR)));
        //Last Point
        bracketPoints[UPPER_BELOW_BRACE_LAST_POINT] = GeometricOperations.offset((isUpperBrace ? endPoint : beginPoint),
                (isUpperBrace ? bracketAngle : braceLipAngle),
                (isUpperBrace ? (-braceLipOffset) : braceLipOffset));
        
        return bracketPoints;
    }
    
    /**
     * This method obtains the all needed points to draw the center brace.
     */
    private Point[] obtainPointsForCenterBrace(double bracketSize, double bracketAngle, double braceLipOffset) {
        //obtain points for center brace
        double braceLipAngle = bracketAngle - BRACKET_AND_BRACE_LIP_ANGLE;
        Point[] bracketPoints = new Point[CENTER_BRACE_NUMBER_OF_POINTS];
        //Center Point
        Point center = GeometricOperations.offset(beginPoint, bracketAngle, bracketSize / 2);
        //Center Out Point
        bracketPoints[CENTER_BRACE_CENTER_OUT_POINT] = GeometricOperations.offset(center, braceLipAngle, braceLipOffset);
        //Center Top Point
        bracketPoints[CENTER_BRACE_CENTER_TOP_POINT]  = GeometricOperations.offset(center, bracketAngle, braceLipOffset);
        //Center Control Point 1
        bracketPoints[CENTER_BRACE_CENTER_CONTROL_POINT_1]  = GeometricOperations.offset(center, bracketAngle, braceLipOffset * BRACE_LIP_SCALE_FACTOR);
        //Center Control Point 2
        bracketPoints[CENTER_BRACE_CENTER_CONTROL_POINT_2] = GeometricOperations.offset(center, braceLipAngle, braceLipOffset * BRACE_LIP_SCALE_FACTOR);
        //Center Control Point 3
        bracketPoints[CENTER_BRACE_CENTER_CONTROL_POINT_3] = GeometricOperations.offset(center, bracketAngle, -(braceLipOffset * BRACE_LIP_SCALE_FACTOR));
        //Center Bottom Point
        bracketPoints[CENTER_BRACE_CENTER_BOTTOM_POINT]  = GeometricOperations.offset(center, bracketAngle, -braceLipOffset);
        
        return bracketPoints;
    }
    
    /**
     * This method processes square brackets.
     */
    private List<SegmentConfiguration> processSquareBracket(double bracketSize, double bracketAngle) {
        
        List<SegmentConfiguration> result = new ArrayList();
        //taken from C++ code        
        double lipOffset = ((bracketSize + 8 ) / 16);
        
        //Extend the perpendicular part of the bracket 
        //in a size of the linewidth/2
        lipOffset += lineWidth/2;
        
        //obtain points for the outside square bracket
        double angle = bracketAngle + BRACKET_AND_BRACE_LIP_ANGLE;
        Point beginOut = GeometricOperations.offset(beginPoint, angle, lipOffset);
        Point endOut = GeometricOperations.offset(endPoint, angle, lipOffset);
        
        //create and add the Segments Configuration
        result.add(new SegmentConfiguration(beginOut, beginPoint));
        result.add(new SegmentConfiguration(beginPoint, endPoint));
        result.add(new SegmentConfiguration(endPoint, endOut));
        
        return result;
    }
    
    /**
     * This method processes parenthesis (rounded brackets).
     */
    private List<SegmentConfiguration> processRoundedBracket(double bracketSize, double bracketAngle) {
        //obtain infomation for create the Arc Configuration
        
        List<SegmentConfiguration> result = new ArrayList();
        double angle = bracketAngle + PARANTHESIS_LIP_ANGLE;
        Point centerPoint = GeometricOperations.offset(beginPoint, angle, bracketSize);
        double radius = GeometricOperations.distance(beginPoint, centerPoint);
        //create and add the Arc Configuration
        boolean largeArc = false;
        boolean sweepPositive = true;
        result.add(new ArcConfiguration(beginPoint, endPoint, radius, radius, BRACKET_DEFAULT_VALUE_ANGLE, largeArc, sweepPositive));
        
        return result;
    }
}