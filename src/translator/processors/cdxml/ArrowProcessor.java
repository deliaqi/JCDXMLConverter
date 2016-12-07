
package translator.processors.cdxml;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.shapes.builders.configurations.ArrowHeadConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Vector;
import translator.utils.StringUtils;

public class ArrowProcessor extends ArcProcessor {
    
    private static final double MIN_ARROW_SIZE = 0.4;
    //This constants are used for indxing arrow points array
    private static final int HALF_ARROW_HEAD_NUMBER_OF_POINTS = 3;
    private static final int ARROW_TOP_POINT = 0;
    private static final int ARROW_BASE_POINT = 1;
    private static final int ARROW_END_POINT = 2;
    
    private boolean rightSide = false;
    private boolean leftSide = false;
    private double halfArrowDirection = 0;
    
    public ArrowProcessor() {
    }
    
    protected void cleanup() {
        rightSide = false;
        leftSide = false;
        halfArrowDirection = 0;
        super.cleanup();
    }
    
    protected void process() {
        CompositeShapeConfiguration resultingCompositeConfiguration = null;
        ParsedElement arrow = getElement();
        
        super.process();
        ShapeBuilderConfiguration arrowArc = resultingConfiguration;
        Collection<ShapeBuilderConfiguration> arrowParts = new ArrayList();
        arrowParts.add(arrowArc);
        
        double arrowHeadLength = arrowCenterSize * getLineWidth();
        arrowHeadWidth = Math.max(arrowHeadWidth, MIN_ARROW_SIZE);
        
        //taken from C++ code
        double arrowWid = Math.abs(arrowHeadSize) * getLineWidth();
        double headRatio = 0;
        if (arrowHeadSize != 0){
            headRatio = Math.abs(arrowHeadWidth / arrowHeadSize);
        }
        double startArrowAngle = GeometricOperations.angle(arrowEnd, arrowStart);
        Point startBasePoint = null;
        Point endBasePoint = null;
        
        //Obtain the base points (at end and start) for the arrow.
        if(angularSize != 0){
            boolean startHead = true;            
            startBasePoint = calculateBasePoint(arrowStart, arrowEnd, arrowCenter, angularSize, arrowCenterSize, startHead);
            startHead = false;
            
            if(getHeadStartDisplaced() == null){                
                endBasePoint = calculateBasePoint(arrowStart, arrowEnd, arrowCenter, angularSize, arrowCenterSize, startHead);                
            } else{
                angularSize /= Math.abs(arrowEquilibriumRatio);
                if(angularSize < 0){
                    endBasePoint = calculateBasePoint(getHeadStartDisplaced(), getHeadEndDisplaced(), arrowCenter, angularSize, arrowCenterSize, startHead);                    
                }else{
                    endBasePoint = calculateBasePoint(getHeadEndDisplaced(), getHeadStartDisplaced(), arrowCenter, angularSize, arrowCenterSize, startHead);
                }
            }
        } else{
            //taken from C++ code
            //Limit arrowhead length to 2/3 of entire arrow for straight arrows-
            boolean getArrowWid = false;
            arrowHeadLength = calculateProportionalArrowHeadLengthAndWid(getArrowWid);
            getArrowWid = true;
            arrowWid = calculateProportionalArrowHeadLengthAndWid(getArrowWid);
            startBasePoint = GeometricOperations.offset(arrowStart, startArrowAngle, -arrowHeadLength);
            endBasePoint = GeometricOperations.offset(arrowEnd, startArrowAngle, arrowHeadLength);
        }
        
        boolean arrowAtStart = false;
        //Obtain the arrowhead for the start of the arc.
        if(arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_HEAD)){
            arrowAtStart = true;
            arrowParts = getArrowheadEndOrStartConfiguration(arrowParts, arrowHeadLength, arrowWid, headRatio, startBasePoint, arrowAtStart);
        }
        //Obtain the arrowhead for the end of the arc.
        if(arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_TAIL)){
            arrowAtStart = false;
            arrowParts = getArrowheadEndOrStartConfiguration(arrowParts, arrowHeadLength, arrowWid, headRatio, endBasePoint, arrowAtStart);
        }        
        //Obtain the No-Go part.
        if(arrow.hasAttribute(ParseElementDefinition.ARROW_NO_GO)){
            arrowParts.addAll(getNoGoParts(arrow.getAttribute(ParseElementDefinition.ARROW_NO_GO), arrowHeadWidth));
        }
        //Obtain the Dipole part.
        if (arrow.hasAttribute(ParseElementDefinition.ARROW_DIPOLE) &&
                (arrow.getAttribute(ParseElementDefinition.ARROW_DIPOLE) != null && arrow.getAttribute(ParseElementDefinition.ARROW_DIPOLE).equalsIgnoreCase(ParseElementDefinition.ARROW_DIPOLE_YES))) {
            boolean isArc = arrow.hasAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE);
            arrowParts.addAll(getDipoleParts(startBasePoint, isArc));
        }
        
        resultingCompositeConfiguration = new CompositeShapeConfiguration(ParseElementDefinition.ARROW, arrowParts);
        resultingCompositeConfiguration.setZOrder(zOrder);
        setResultingConfiguration(resultingCompositeConfiguration);
    }
    
    /**
     * This method obtains the arrowhead configuration for the start or the end of the arc.
     */
    private Collection<ShapeBuilderConfiguration> getArrowheadEndOrStartConfiguration(Collection<ShapeBuilderConfiguration> arrowParts, double arrowHeadLength,
            double arrowWid, double headRatio, Point basePoint, boolean arrowAtStart) {
        Point topPoint = null;
        String arrowHeadEnd;
        if (arrowAtStart) {
            topPoint = arrowStart;
            arrowHeadEnd = arrowheadHead;
        } else {
            if (getHeadEndDisplaced() == null) {
                topPoint = arrowEnd;
            } else {
                if(angularSize < 0){
                    topPoint = getHeadEndDisplaced();
                }else{
                    topPoint = getHeadStartDisplaced();
                }                
            }
            arrowHeadEnd = arrowheadTail;
        }
        arrowParts.add(getArrowheadConfiguration(arrowAtStart, topPoint, basePoint, arrowHeadLength,
                arrowWid, headRatio, arrowHeadEnd));
        
        return arrowParts;
    }
    
    /**
     * This method moves the arrowhead to the proper position and then return the arrowhead configuration.
     */
    private ArrowHeadConfiguration getArrowheadConfiguration(boolean arrowAtStart, Point topPoint, Point basePoint,
            double arrowHeadLength, double arrowWid, double headRatio, String arrowheadType) {
        ArrowHeadConfiguration result;
        halfArrowDirection = 0;
        rightSide = false;
        leftSide = false;
        double angle = GeometricOperations.angle(basePoint, topPoint);
        
        //taken From C++ Code
        double endPointOffset = 0;
        double factor = 0;
        if (arrowCenterSize > 0) {
            endPointOffset = GeometricOperations.distance(basePoint, topPoint) * Math.abs(arrowHeadSize / arrowCenterSize);
        }
        Point endPoint = GeometricOperations.offset(topPoint, angle, -endPointOffset);
        if (arrowWid > 0) {
            factor = GeometricOperations.distance(basePoint, topPoint) / arrowWid / 2;
        }
        
        Point[] halfArrowHeadPoints = new Point[HALF_ARROW_HEAD_NUMBER_OF_POINTS];
        boolean isRetrosynthetic = arrowType != null && arrowType.equals(ParseElementDefinition.ARROW_TYPE_RETRO_SYNTHETIC);
        
        if(!isRetrosynthetic){
            rightSide = false;
            leftSide = false;
            //taken from C++ Code
            Point arrowDistance = new Point(headRatio * (endPoint.getY() - topPoint.getY()), -headRatio * (endPoint.getX() - topPoint.getX()));
            arrowHeadWidth = GeometricOperations.distance(arrowDistance);
            if (!arrowheadType.equals(ParseElementDefinition.ARROW_TYPE_FULL)) {
                //Move the half arrowhead perpendicularly to the arc.
                halfArrowHeadPoints = moveHalfArrowheadPerpendicularly(topPoint, basePoint, endPoint, headRatio, arrowheadType, angle, arrowAtStart);
                topPoint = halfArrowHeadPoints[ARROW_TOP_POINT];
                basePoint = halfArrowHeadPoints[ARROW_BASE_POINT];
                endPoint = halfArrowHeadPoints[ARROW_END_POINT];
            }
        } else {
            //Taken from C++ code
            //If the arrow is Retrosynthetic, then the arrowhead width is equal to the arrowhead length.
            arrowHeadWidth = arrowHeadLength;
            if (arrowheadType.equals(ParseElementDefinition.ARROW_HALF_LEFT)) {
                leftSide = true;
            } else if (arrowheadType.equals(ParseElementDefinition.ARROW_HALF_RIGHT)) {
                rightSide = true;
            }
        }
        
        if (arrowType != null && arrowType.equals(ParseElementDefinition.ARROW_TYPE_EQUILIBRIUM)) {
            //Move Equilibrium arrow half arrowhead, towards to the arc.
            halfArrowHeadPoints = moveEquilibriumArrowhead(arrowAtStart, topPoint, basePoint, endPoint, angle);
            topPoint = halfArrowHeadPoints[ARROW_TOP_POINT];
            basePoint = halfArrowHeadPoints[ARROW_BASE_POINT];
            endPoint = halfArrowHeadPoints[ARROW_END_POINT];
        }
        
        //Create the arrowhead configuration with the obtained values.
        result = createArrowhead(basePoint, topPoint, endPoint, arrowHeadWidth, angle, factor, rightSide, leftSide);
        //If the arrowhead type is hollow
        if (arrowHeadType.equals(ParseElementDefinition.ARROW_HOLLOW)) {
            result.setFill(true);
            result.setFillColor(getBackgroundColor());
        }
        result.setColor(color);
        
        return result;
    }
    
    /**
     * This method moves the half arrowhead perpendicularly to the arc when the arrowhead is not Full.
     */
    private Point [] moveHalfArrowheadPerpendicularly(Point topPoint, Point basePoint, Point endPoint, double headRatio, String arrowheadType, double angle, boolean arrowAtStart) {
        Point[] halfArrowHeadPoints = new Point[HALF_ARROW_HEAD_NUMBER_OF_POINTS];
        //The offset lenght moves the half arrowhead from the middle shaft spacing
        // to the opposite side of the arrowhead
        double halfArrowWidth = 0;        
        
        //for the curved equilibrium arrowhead
        if (arrowType != null && arrowType.equals(ParseElementDefinition.ARROW_TYPE_EQUILIBRIUM) && 
                !arrowAtStart && angularSize != 0 && arrowEquilibriumRatio != 0){
            
           halfArrowWidth = -width * 2; 
           
        }else if(!arrowHeadType.equalsIgnoreCase(ParseElementDefinition.ARROW_HOLLOW) && 
                !arrowHeadType.equalsIgnoreCase(ParseElementDefinition.ARROW_ANGLE)){
            //for the rest of the arrowhead that aren't hollow and angled
            
           halfArrowWidth = -width / 2; 
           
        }

        if (arrowheadType.equals(ParseElementDefinition.ARROW_HALF_LEFT)) {
            leftSide = true;
            halfArrowDirection = angle - Math.PI / 2;
        } else {
            rightSide = true;
            halfArrowDirection = angle + Math.PI / 2;
        }
        halfArrowHeadPoints[ARROW_TOP_POINT] = GeometricOperations.offset(topPoint, halfArrowDirection, halfArrowWidth);
        halfArrowHeadPoints[ARROW_BASE_POINT] = GeometricOperations.offset(basePoint, halfArrowDirection, halfArrowWidth);
        halfArrowHeadPoints[ARROW_END_POINT] = GeometricOperations.offset(endPoint, halfArrowDirection, halfArrowWidth);
        
        //taken from C++ code
        //if the arrowhead is "half" the arrowheadWidth must be increased in lineWidth/2
        arrowHeadWidth += getWidth()/2;
        
        return halfArrowHeadPoints;
    }
    
    /**
     * This method moves an Equilibrium arrow half arrowhead, towards to the arc.
     */
    private Point[] moveEquilibriumArrowhead(boolean arrowAtStart, Point topPoint, Point basePoint, Point endPoint, double angle) {
        //move the half arrowhead towards to the arc
        Point[] halfArrowHeadPoints = new Point[HALF_ARROW_HEAD_NUMBER_OF_POINTS];
        
        halfArrowHeadPoints[ARROW_TOP_POINT] = GeometricOperations.offset(topPoint, halfArrowDirection, shaftSpacing);
        halfArrowHeadPoints[ARROW_BASE_POINT] = GeometricOperations.offset(basePoint, halfArrowDirection, shaftSpacing);
        halfArrowHeadPoints[ARROW_END_POINT] = GeometricOperations.offset(endPoint, halfArrowDirection, shaftSpacing);
        
        int direction = 1;
        boolean displaceArrowhead = false;
        
        if (!arrowAtStart && !equilibriumAtHead) {
            direction *= -1;
            displaceArrowhead = true;
        } else if (arrowAtStart && equilibriumAtHead) {
            displaceArrowhead = true;
        }
        
        if (displaceArrowhead) {
            halfArrowHeadPoints[ARROW_TOP_POINT] = GeometricOperations.offset(halfArrowHeadPoints[ARROW_TOP_POINT], angle, direction * getDisplacedOffset());
            halfArrowHeadPoints[ARROW_BASE_POINT] = GeometricOperations.offset(halfArrowHeadPoints[ARROW_BASE_POINT], angle, direction * getDisplacedOffset());
            halfArrowHeadPoints[ARROW_END_POINT] = GeometricOperations.offset(halfArrowHeadPoints[ARROW_END_POINT], angle, direction * getDisplacedOffset());
        }
        
        return halfArrowHeadPoints;
    }
    
    /**
     * This method obtains the arrowhead configuration.
     */
    private ArrowHeadConfiguration createArrowhead(Point basePoint, Point topPoint, Point endPoint,
            double arrowHeadWidth, double angle, double factor, boolean rightSide, boolean leftSide){
        
        ParsedElement arrow = getElement();
        ArrowHeadConfiguration result = null;
        
        if(arrow.hasAttribute(ParseElementDefinition.ARROW_HEAD_TYPE)){
            if(arrow.getAttribute(ParseElementDefinition.ARROW_HEAD_TYPE).equals(ParseElementDefinition.ARROW_HOLLOW)){
                result = ArrowHeadConfiguration.getArrowHeadShape(basePoint,
                        topPoint, endPoint, arrowHeadWidth, angle, factor, rightSide, leftSide);
                result.setStrokeWidth(getWidth());
            } else if(arrow.getAttribute(ParseElementDefinition.ARROW_HEAD_TYPE).equals(ParseElementDefinition.ARROW_ANGLE)){
                result = ArrowHeadConfiguration.getFullArrowHeadAngle(topPoint,
                        endPoint, arrowHeadWidth, angle, rightSide, leftSide);
                result.setStrokeWidth(getWidth());
            } else{     // Arrow head type is Solid
                result = ArrowHeadConfiguration.getArrowHeadShape(basePoint,
                        topPoint, endPoint, arrowHeadWidth, angle, factor, rightSide, leftSide);
                result.setFill(true);
            }
        } else{     // As arrow type is unspecified, then it is considered as Solid
            result = ArrowHeadConfiguration.getArrowHeadShape(basePoint,
                    topPoint, endPoint, arrowHeadWidth, angle, factor, rightSide, leftSide);
            result.setFill(true);
        }
        
        result.setLineCap(LineCap.Butt);
        result.setLineJoin(LineJoin.Miter);
        
        double miterLimit = getMiterLimit(getMinorAngle(topPoint, basePoint, 
                endPoint, arrowHeadWidth, angle, factor));
        miterLimit = Math.round(miterLimit);
        
        result.setMiterLimit(miterLimit);
        
        return result;
    }
    
    /**
     * Obtain the minor angle of the arrowhead vertexs
     */
    public double getMinorAngle(Point startPoint, Point basePoint, Point endPoint, 
            double arrowHeadWidth, double angle, double factor){
        
        double result;
        
        double perpendicularAngle = angle + Math.PI / 2;
        
        Point cornerPoint = GeometricOperations.offset(endPoint, perpendicularAngle, arrowHeadWidth);
        double arrowheadShaft = GeometricOperations.distance(endPoint, startPoint);
        
        //obtain a point closer to the angle that describe the bezier curve of the arrowhead
        Point middleBasePoint = GeometricOperations.offset(basePoint, angle, arrowheadShaft * factor);
        
        //obtain the angles of the arrowhead vertex        
        double angle1 = Math.acos(GeometricOperations.cosine(startPoint, cornerPoint, middleBasePoint));
        double angle2 = Math.acos(GeometricOperations.cosine(cornerPoint, middleBasePoint, startPoint));
        double angle3 = Math.acos(GeometricOperations.cosine(middleBasePoint, startPoint, cornerPoint));
        
        result = Math.min(angle1, angle2);
        
        result = Math.min(result, angle3);
        
        return result;
    }
    
    /**
     * This method gets a Dipole arrow configuration.
     * A Dipole arrow is an arrow pointing to the negative end of the polar bond.
     */
    private List<ShapeBuilderConfiguration> getDipoleParts(Point basePoint, boolean isArc){
        List<ShapeBuilderConfiguration> result = new ArrayList();
        Point tailBasePoint;
        Vector dipoleDelta;
        
        if(isArc){
            AffineTransform transformation = new AffineTransform();
            Point transformedCenter = getMatrix().transform(center);
            transformation.translate(-transformedCenter.getX(), -transformedCenter.getY());
            double offset = 0;
            double offsetDivisor = angularSize * Math.PI * GeometricOperations.distance(center, arrowStart);
            if(offsetDivisor != 0) {
                offset = 1 - Math.abs(180 * arrowHeadWidth / offsetDivisor);
            }
            transformation.rotate(Math.toRadians(angularSize * offset));
            transformation.translate(transformedCenter.getX(), transformedCenter.getY());
            double[] flatMatrix = new double[6];
            transformation.getMatrix(flatMatrix);
            flatMatrix[4] *= -1;
            flatMatrix[5] *= -1;
            transformation.setTransform(flatMatrix[0], flatMatrix[1], flatMatrix[2], flatMatrix[3], flatMatrix[4], flatMatrix[5]);
            
            Point transformedHead = getMatrix().transform(arrowStart);
            Point2D transformedHead2D = new Point2D.Double(transformedHead.getX(), transformedHead.getY());
            transformation.transform(transformedHead2D, transformedHead2D);
            transformedHead.setX(transformedHead2D.getX());
            transformedHead.setY(transformedHead2D.getY());
            tailBasePoint = getInverseMatrix().transform(transformedHead);
            
            dipoleDelta = new Vector(tailBasePoint.subtract(center));
            
            if (dipoleDelta.getLength() > 0) {
                double newScale = arrowHeadWidth / dipoleDelta.getLength();
                dipoleDelta = dipoleDelta.byScalar(newScale);
            }
        } else {
            tailBasePoint = new Point(basePoint);
            tailBasePoint = CDXMLEnvironment.fixedScale2D(tailBasePoint, arrowEnd, Math.abs(arrowHeadWidth));
            
            dipoleDelta = new Vector(tailBasePoint.subtract(arrowEnd));
            
            if (dipoleDelta.getLength() > 0) {
                double newScale = arrowHeadWidth / dipoleDelta.getLength();
                dipoleDelta.byScalar(newScale);
            }
            
            dipoleDelta = dipoleDelta.getNormal();
        }
        
        Point dipoleLineBegin = tailBasePoint.offset(dipoleDelta);
        Point dipoleLineEnd = tailBasePoint.offset(dipoleDelta.byScalar(-1));
        
        SegmentConfiguration dipoleLine = new SegmentConfiguration(dipoleLineBegin, dipoleLineEnd);
        dipoleLine.setStrokeWidth(getWidth());
        dipoleLine.setColor(color);
        result.add(dipoleLine);
        
        return result;
    }
    
    protected String getTypeString() {
        if (lineType == null) {
            return StringUtils.EMPTY_STRING;
        } else {
            return lineType;
        }
    }
}
