package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.ArcConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.GeometricOperations;
import translator.utils.Line;
import translator.utils.Point;
import translator.utils.StringUtils;

public class HollowArrowProcessor extends ArcProcessor{
    
    private static final int ARROW_SCALE_FACTOR = 10;
        
    private double arrowheadWidth;
    private ParsedElement arrow;
    private double spacing;
    
    private Point startTopBegin = null;
    private Point startBottomBegin = null;
    
    private Point endTopBegin = null;
    private Point endBottomBegin = null;
    
    private Point headBasePoint;
    private Point tailBasePoint;
    
    private Point topHead;
    private Point bottomHead;
    private Point topTail;
    private Point bottomTail;
    
    private double majorAxisAngle;
    private double minorAxisAngle;
    private double arrowWidth = 0;
    
    private boolean arrowAtHead = false;
    private boolean arrowAtTail = false;
    
    public HollowArrowProcessor() {
    }
    
    protected void cleanup() {
        arrowheadWidth = 0;
        arrow = null;
        spacing = 0;
        arrowWidth = 0;
        super.cleanup();
    }
    
    protected void configure(){
        super.configure();
        arrow = getElement();
        if(arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH)){
            arrowheadWidth = Double.parseDouble(arrow.getAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH)) / ARROW_SCALE_FACTOR;
        }
    }
    
    protected void process(){
        super.configure();
        ShapeBuilderConfiguration resultingConfiguration = null;
        SplineConfiguration configuration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        List<SegmentConfiguration> segments = new ArrayList();
        List<Point> arrowPoints = new ArrayList();
        
        spacing = shaftSpacing;
        arrowAtHead = arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD);
        arrowAtTail = arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL);
        
        //If the hollow arrow is curved.
        if (arrow.hasAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE)) {
            segments = processCurvedHollowArrow(segments);
        }else{
            //If the hollow arrow is straight
            segments = processStraightHollowArrow(segments);
        }
        configuration = new SplineConfiguration(segments);
        
        if (arrow.hasAttribute(ParseElementDefinition.ARROW_LINE_TYPE)){
            lineType = arrow.getAttribute(ParseElementDefinition.ARROW_LINE_TYPE);
        }
        if ( arrow.hasAttribute(ParseElementDefinition.ARROW_FILL_TYPE)){
            fillType = arrow.getAttribute(ParseElementDefinition.ARROW_FILL_TYPE);
        }
        
        setAttributesForDrawingElement();
        
        if(isFaded() || isFilled()){
            configuration.setFill(true);
            if(isFaded()){
                configuration.setFillColor(getFadedColor());
            }            
        }
        
        if(isShaded()){
            configuration.setShaded(true);
            configuration.setGradient(RadialGradient.getOvalGradient(arrow.getId(), getColor()));
        }
        
        configuration.setColor(getColor());
        configuration.setClosed(true);
        configuration.setDashed(isDashed());
        configuration.setDashLength(getDashLength());
        configuration.setLineJoin(LineJoin.Miter);
        
        //if the fillType isn't "Filled" the stroke width takes the current value of  the width
        //else if the lineType is Bold we must set the actual value of the bold width to the configuration
        if(!isFilled() || isBold() || (isShaded() && isDashed())) {
            configuration.setStrokeWidth(getWidth());
        }
        
        innerShapes.add(configuration);
        
        if (arrow.hasAttribute(ParseElementDefinition.ARROW_NO_GO)) {
            innerShapes.addAll(getNoGoParts(arrow.getAttribute(ParseElementDefinition.ARROW_NO_GO), arrowWidth));
        }
        
        resultingConfiguration = new CompositeShapeConfiguration(
                ParseElementDefinition.ARROW_HOLLOW, innerShapes);
        
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(getZOrder());
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    /**
     * This method processes hollow curved arrows.
     */
    private List<SegmentConfiguration> processCurvedHollowArrow(List<SegmentConfiguration> segments) {
        majorAxisAngle = GeometricOperations.angle(center, majorAxisEnd);
        minorAxisAngle = GeometricOperations.angle(center, minorAxisEnd);
        
        headBasePoint = calculateBasePoint(arrowStart, arrowEnd, center, angularSize, arrowCenterSize, true);
        tailBasePoint = calculateBasePoint(arrowStart, arrowEnd, center, angularSize, arrowCenterSize, false);
        
        double angleHeadBase = GeometricOperations.angle(arrowStart, headBasePoint);
        double angleTailBase = GeometricOperations.angle(arrowEnd, tailBasePoint);
        
        double tailAngle = GeometricOperations.angle(arrowEnd, center);
        double headAngle = GeometricOperations.angle(arrowStart, center);
        
        //This value modifies the angular size according to the size of the arrowhead
        double minusAngle = Math.acos(GeometricOperations.cosine(arrowStart, arrowEnd, headBasePoint)) * 2;
        
        topHead = GeometricOperations.offset(arrowStart, headAngle, spacing / 2);
        bottomHead = GeometricOperations.offset(arrowStart, headAngle, (-spacing) / 2);
        topTail = GeometricOperations.offset(arrowEnd, tailAngle, spacing / 2);
        bottomTail = GeometricOperations.offset(arrowEnd, tailAngle, (-spacing) / 2);
        
        //the arrowWidth its the distance between the base of the arrow head
        //and the end point of the arrow head
        arrowWidth = GeometricOperations.distance(headBasePoint, arrowStart);
        
        if (arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD)) {
            startTopBegin = GeometricOperations.offset(headBasePoint, angleHeadBase + Math.PI / 2, spacing / 2);
            startBottomBegin = GeometricOperations.offset(headBasePoint, angleHeadBase - Math.PI / 2, spacing / 2);
        }
        
        if (arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL)) {
            endTopBegin = GeometricOperations.offset(tailBasePoint, angleTailBase - Math.PI / 2, spacing / 2);
            endBottomBegin = GeometricOperations.offset(tailBasePoint, angleTailBase + Math.PI / 2, spacing / 2);
        }
        
        Point majorAxisEndIn = GeometricOperations.offset(majorAxisEnd, majorAxisAngle, (-spacing) / 2);
        Point majorAxisEndOut = GeometricOperations.offset(majorAxisEnd, majorAxisAngle, spacing / 2);
        Point minorAxisEndIn = GeometricOperations.offset(minorAxisEnd, minorAxisAngle, (-spacing) / 2);
        Point minorAxisEndOut = GeometricOperations.offset(minorAxisEnd, minorAxisAngle, spacing / 2);
        
        boolean isHead = true;
        if (arrowAtHead && arrowAtTail) {
            segments = processCurvedArrowHeadAndTail(segments, majorAxisEndIn, majorAxisEndOut, minorAxisEndIn, minorAxisEndOut, minusAngle);
        } else if (arrowAtHead) {
            segments = processCurvedArrowHeadOrTail(segments, majorAxisEndIn, majorAxisEndOut, minorAxisEndIn, minorAxisEndOut, minusAngle, isHead);
        } else if (arrowAtTail) {
            segments = processCurvedArrowHeadOrTail(segments, majorAxisEndIn, majorAxisEndOut, minorAxisEndIn, minorAxisEndOut, minusAngle, !isHead);
        } else {   //When the curved hollow arrow has not arrowhead neither at tail nor at head.
            segments = processCurvedArrowWithoutArrowhead(segments, majorAxisEndIn, majorAxisEndOut, minorAxisEndIn, minorAxisEndOut);
        }
        
        return segments;
    }
    
    /**
     * This method processes hollow straight arrows.
     */
    private List<SegmentConfiguration> processStraightHollowArrow(List<SegmentConfiguration> segments) {
        double angle = GeometricOperations.angle(arrowStart, arrowEnd);
        double arrowLength = GeometricOperations.distance(arrowStart, arrowEnd);
        
        //taken from C++ code
        double arrowHeadLength = 0;
        
        //when the arrow length is greater than the arrowCenterSize
        //the value of the arrowCenterSize must be 2/3 of the arrow lenght
        //This proportion is used in the C++ arrow code
        boolean getArrowWid = false;
        arrowHeadLength = calculateProportionalArrowHeadLengthAndWid(getArrowWid);
        
        headBasePoint = GeometricOperations.offset(arrowStart, angle, arrowHeadLength);
        tailBasePoint = GeometricOperations.offset(arrowEnd, angle, -arrowHeadLength);
        
        topHead = GeometricOperations.offset(arrowStart, angle - Math.PI / 2, spacing / 2);
        bottomHead = GeometricOperations.offset(arrowStart, angle + Math.PI / 2, spacing / 2);
        topTail = GeometricOperations.offset(arrowEnd, angle - Math.PI / 2, spacing / 2);
        bottomTail = GeometricOperations.offset(arrowEnd, angle + Math.PI / 2, spacing / 2);
        
        if (arrowAtHead) {
            startTopBegin = GeometricOperations.offset(bottomHead, angle, arrowHeadLength);
            startBottomBegin = GeometricOperations.offset(topHead, angle, arrowHeadLength);
            arrowWidth = GeometricOperations.distance(headBasePoint, arrowStart);
        }
        if (arrowAtTail) {
            endTopBegin = GeometricOperations.offset(bottomTail, angle, -arrowHeadLength);
            endBottomBegin = GeometricOperations.offset(topTail, angle, -arrowHeadLength);
            arrowWidth = GeometricOperations.distance(tailBasePoint, arrowEnd);
        }
        boolean isHead = true;
        if (arrowAtHead && !arrowAtTail) {
            segments = processStraightArrowHeadOrTail(segments, isHead);
        } else if (!arrowAtHead && arrowAtTail) {
            isHead = false;
            segments = processStraightArrowHeadOrTail(segments, isHead);
        } else if (arrowAtHead && arrowAtTail) {
            segments = processStraightArrowHeadAndTail(segments);
        } else {
            segments = processStraightArrowWithoutArrowhead(segments);
        }
        
        return segments;
    }
    
    /**
     * This method processes the head and tail segment configuration for curved arrows.
     */
    private List<SegmentConfiguration> processCurvedArrowHeadAndTail(List<SegmentConfiguration> segments, translator.utils.Point majorAxisEndIn,
            translator.utils.Point majorAxisEndOut, translator.utils.Point minorAxisEndIn, translator.utils.Point minorAxisEndOut, double minusAngle) {
        double minusAngleArrowAndTail = minusAngle * 2;
        segments.addAll(calculateArc(
                (angularSize < 0 ? startTopBegin : endTopBegin),
                (angularSize < 0 ? endTopBegin : startTopBegin),
                (angularSize < 0 ? majorAxisEndOut : majorAxisEndIn),
                (angularSize < 0 ? minorAxisEndOut : minorAxisEndIn),
                center, false, minusAngleArrowAndTail));
        
        segments.addAll(getArrowHead(
                (angularSize < 0 ? arrowEnd : arrowStart),
                (angularSize < 0 ? tailBasePoint : headBasePoint), arrowWidth, angularSize < 0 ? false : true));
        
        segments.addAll(calculateArc(
                (angularSize < 0 ? startBottomBegin : endBottomBegin),
                (angularSize < 0 ? endBottomBegin : startBottomBegin),
                (angularSize < 0 ? majorAxisEndIn : majorAxisEndOut),
                (angularSize < 0 ? minorAxisEndIn : minorAxisEndOut),
                center, true, minusAngleArrowAndTail));
        
        segments.addAll(getArrowHead((angularSize < 0 ? arrowStart : arrowEnd),
                (angularSize < 0 ? headBasePoint : tailBasePoint), arrowWidth, angularSize < 0 ? false : true));
        
        return segments;
    }
    
    /**
     * This method processes the head or the tail segment configuration for curved arrows.
     */
    private List<SegmentConfiguration> processCurvedArrowHeadOrTail(List<SegmentConfiguration> segments, translator.utils.Point majorAxisEndIn, translator.utils.Point majorAxisEndOut,
            translator.utils.Point minorAxisEndIn, translator.utils.Point minorAxisEndOut, double minusAngle, boolean isHead) {
        if (angularSize < 0) {
            segments.addAll(calculateArc(
                    (isHead ? startTopBegin : bottomHead),
                    (isHead ? bottomTail : endTopBegin), majorAxisEndOut, minorAxisEndOut, center,
                    (isHead ? true : false), minusAngle));
        } else {
            segments.addAll(calculateArc(
                    (isHead ? topTail : endTopBegin),
                    (isHead ? startTopBegin : topHead), majorAxisEndIn, minorAxisEndIn, center, (isHead ? false : true), minusAngle));
        }
        //get Arrowhead
        segments.addAll(getArrowHead(
                (isHead ? arrowStart : arrowEnd), (isHead ? headBasePoint : tailBasePoint), arrowWidth, (isHead ? true : false)));
        
        if (angularSize < 0) {
            segments.addAll(calculateArc(
                    (isHead ? startBottomBegin : topHead),
                    (isHead ? topTail : endBottomBegin), majorAxisEndIn, minorAxisEndIn, center, (isHead ? false : true), minusAngle));
        } else {
            segments.addAll(calculateArc(
                    (isHead ? bottomTail : endBottomBegin),
                    (isHead ? startBottomBegin : bottomHead), majorAxisEndOut, minorAxisEndOut, center, (isHead ? true : false), minusAngle));
        }
        
        return segments;
    }
    
    /**
     * This method processes the segment configuration for curved arrows without any arrowhead.
     */
    private List<SegmentConfiguration> processCurvedArrowWithoutArrowhead(List<SegmentConfiguration> segments, translator.utils.Point majorAxisEndIn, translator.utils.Point majorAxisEndOut, translator.utils.Point minorAxisEndIn, translator.utils.Point minorAxisEndOut) {
        segments.addAll(calculateArc(
                (angularSize < 0 ? topHead : bottomTail),
                (angularSize < 0 ? topTail : bottomHead),
                (angularSize < 0 ? majorAxisEndIn : majorAxisEndOut),
                (angularSize < 0 ? minorAxisEndIn : minorAxisEndOut),
                center, (angularSize < 0 ? true : false)));
        segments.add(new SegmentConfiguration((angularSize < 0 ? topHead : bottomHead), (angularSize < 0 ? bottomHead : topHead)));
        segments.addAll(calculateArc(
                (angularSize < 0 ? bottomHead : topTail),
                (angularSize < 0 ? bottomTail : topHead),
                (angularSize < 0 ? majorAxisEndOut : majorAxisEndIn),
                (angularSize < 0 ? minorAxisEndOut : minorAxisEndIn),
                center, (angularSize < 0 ? false : true)));
        
        return segments;
    }
    
    /**
     * This method processes the head or the tail segment configuration for straight arrows.
     */
    private List<SegmentConfiguration> processStraightArrowHeadOrTail(List<SegmentConfiguration> segments, boolean isHead) {
        segments.add(new SegmentConfiguration(
                (isHead ? bottomTail : bottomHead), (isHead ? startTopBegin : endTopBegin)));
        segments.addAll(getArrowHead(
                (isHead ? arrowStart : arrowEnd),
                (isHead ? headBasePoint : tailBasePoint),
                arrowWidth, (isHead ? true : false)));
        segments.add(new SegmentConfiguration(
                (isHead ? startBottomBegin : endBottomBegin), (isHead ? topTail : topHead)));
        
        return segments;
    }
    
    /**
     * This method processes the head and the tail segment configuration for straight arrows.
     */
    private List<SegmentConfiguration> processStraightArrowHeadAndTail(List<SegmentConfiguration> segments) {
        segments.addAll(getArrowHead(arrowEnd, tailBasePoint, arrowWidth, false));
        segments.add(new SegmentConfiguration(endBottomBegin, startBottomBegin));
        segments.addAll(getArrowHead(arrowStart, headBasePoint, arrowWidth, false));
        segments.add(new SegmentConfiguration(startTopBegin, endTopBegin));
        
        return segments;
    }
    
    /**
     * This method processes the segment configuration for straight arrows without any arrowhead.
     */
    private List<SegmentConfiguration> processStraightArrowWithoutArrowhead(List<SegmentConfiguration> segments) {
        segments.add(new SegmentConfiguration(bottomTail, bottomHead));
        segments.add(new SegmentConfiguration(bottomHead, topHead));
        segments.add(new SegmentConfiguration(topHead, topTail));
        segments.add(new SegmentConfiguration(topTail, bottomTail));
        
        return segments;
    }
    
    /**
     * This method gets the arrohead for a hollow arrow.
     */
    public List<SegmentConfiguration> getArrowHead(Point head, Point base, double arrowWidth, boolean inverse){
        List<SegmentConfiguration> result = new ArrayList();
        double angle = GeometricOperations.angle(base, head);
        
        double offsetAngle = angle + Math.PI / 2;
        Point leftBeginArrow = GeometricOperations.offset(base, offsetAngle, spacing/2);
        Point leftEndArrow = GeometricOperations.offset(base, offsetAngle, arrowWidth);
        
        offsetAngle = angle - Math.PI / 2;
        Point rightBeginArrow = GeometricOperations.offset(base, offsetAngle, spacing/2);
        Point rightEndArrow = GeometricOperations.offset(base, offsetAngle, arrowWidth);
        
        if(inverse){
            result.add(new SegmentConfiguration(rightBeginArrow, rightEndArrow));
            result.add(new SegmentConfiguration(rightEndArrow, head));
            result.add(new SegmentConfiguration(head, leftEndArrow));
            result.add(new SegmentConfiguration(leftEndArrow, leftBeginArrow));
        }else{
            result.add(new SegmentConfiguration(leftBeginArrow, leftEndArrow));
            result.add(new SegmentConfiguration(leftEndArrow, head));
            result.add(new SegmentConfiguration(head, rightEndArrow));
            result.add(new SegmentConfiguration(rightEndArrow, rightBeginArrow));
        }
        return result;
    }
    
    public List<SegmentConfiguration> calculateArc(Point head, Point tail, Point majorAxisEnd, Point minorAxisEnd, Point center, boolean inverse){
        return calculateArc(head, tail, majorAxisEnd, minorAxisEnd, center, inverse, 0);
    }
    
    public List<SegmentConfiguration> calculateArc(Point head, Point tail, Point majorAxisEnd, Point minorAxisEnd, Point center, boolean inverse, double minusAngle){
        List<SegmentConfiguration> segments = new ArrayList();
        double arcAngle = Math.abs(angularSize) - Math.toDegrees(minusAngle);
        arcAngle = -arcAngle;
        
        double angleBetweenAxis = GeometricOperations.angle(new Line(center, majorAxisEnd)) - GeometricOperations.angle(new Line(center, minorAxisEnd));
        int angleBetweenAxisInteger = (int) Math.round(Math.toDegrees(angleBetweenAxis));
        
        if (Math.abs(angleBetweenAxisInteger) != 90) {
            
            Point transformedHead = getMatrix().transform(head);
            Point transformedCenter = getMatrix().transform(center);
            
            double arrowRadius = GeometricOperations.distance(transformedCenter, transformedHead);
            double startAngle = Math.toDegrees(Math.atan2(transformedHead.getY() - transformedCenter.getY(), transformedHead.getX() - transformedCenter.getX()));
            
            Point edge = new Point(transformedCenter.getX() + arrowRadius, transformedCenter.getY());
            
            List<Point> arcPoints = calculateTransformedArcPoints(transformedCenter, edge, startAngle + 90, arcAngle);
            if (arcPoints.size() > 0){
                for (Point arcPoint : arcPoints) {
                    Point transformedArcPoint = getInverseMatrix().transform(arcPoint);
                    arcPoint.setX(transformedArcPoint.getX());
                    arcPoint.setY(transformedArcPoint.getY());
                }
            }
            
             // Build bezier curves for the arc drawn towards the left or towards the right.
            if(!inverse){
                for (int i = 1; i + 3 < arcPoints.size() - 1; i += 3) {
                    Point controlPoint1 = arcPoints.get(i);
                    Point controlPoint2 = arcPoints.get(i + 1);
                    Point controlPoint3 = arcPoints.get(i + 2);
                    Point controlPoint4 = arcPoints.get(i + 3);
                    CubicCurveConfiguration arcCurve = new CubicCurveConfiguration(
                            controlPoint1, controlPoint4, controlPoint2, controlPoint3);
                    segments.add(arcCurve);
                }
            }else{
                for (int i = arcPoints.size() - 2; i - 3 > 0; i -= 3) {
                    Point controlPoint1 = arcPoints.get(i);
                    Point controlPoint2 = arcPoints.get(i - 1);
                    Point controlPoint3 = arcPoints.get(i - 2);
                    Point controlPoint4 = arcPoints.get(i - 3);
                    CubicCurveConfiguration arcCurve = new CubicCurveConfiguration(
                            controlPoint1, controlPoint4, controlPoint2, controlPoint3);
                    segments.add(arcCurve);
                }
            }
        }else{
            ArcConfiguration arcSegment;
            
            xRadius = GeometricOperations.distance(center, majorAxisEnd);
            yRadius = GeometricOperations.distance(center, minorAxisEnd);
            
            if(!inverse){
                arcSegment = new ArcConfiguration(head, tail,
                        xRadius, yRadius, 0, (Math.abs(arcAngle) >= 180), (arcAngle > 0));
            }else{
                arcSegment = new ArcConfiguration(tail, head,
                        xRadius, yRadius, 0, (Math.abs(arcAngle) >= 180), (arcAngle < 0));
            }
            
            
            segments.add(arcSegment);
        }
        
        return segments;
    }
    
    protected String getTypeString() {
        String result;
        if (lineType == null) {
            result = StringUtils.EMPTY_STRING;
        } else {
            result = lineType + StringUtils.UNDERSCORE;
        }
        if (fillType != null) {
            result += fillType;
        }
        return result;
    }
}
