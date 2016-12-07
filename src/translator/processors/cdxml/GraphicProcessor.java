
package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Vector;

public abstract class GraphicProcessor extends SplineProcessor {
    
    protected Point center;
    protected Point majorAxisEnd;
    protected Point minorAxisEnd;
    protected double shadowSize;
    protected Matrix3D matrix;
    protected Matrix3D inverseMatrix;
    
    //Default eccentricity taken from the C++ code
    public final static double DEFAULT_OVAL_ECCENTRICITY = 0.4;
    //Default shadow size taken from the C++ code
    public final static double DEFAULT_SHADOW_SIZE = 4;
    
    public GraphicProcessor() {
    }
    
    protected void configure() {
        super.configure();
        
        calculateBasicAttributes();
        
        if (getElement().hasAttribute(ParseElementDefinition.GRAPHIC_SHADOW_SIZE)) {
            shadowSize = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.GRAPHIC_SHADOW_SIZE)) / 100;
        }else if(shadowed){
            shadowSize = DEFAULT_SHADOW_SIZE;            
        }
    }
    
    /**
     * Calculate the Major Axis, Minor Axis and Center
     */
    protected void calculateBasicAttributes(){
        ParsedElement element = getElement();
        
        if (element.hasAttribute(ParseElementDefinition.GRAPHIC_CENTER_3D)) {
            center = parseCoords(element.getAttribute(ParseElementDefinition.GRAPHIC_CENTER_3D), element);
        }
        
        if (element.hasAttribute(ParseElementDefinition.GRAPHIC_MAJOR_AXIS_END_3D)) {
            majorAxisEnd = parseCoords(element.getAttribute(ParseElementDefinition.GRAPHIC_MAJOR_AXIS_END_3D), element);
        }
        
        if (element.hasAttribute(ParseElementDefinition.GRAPHIC_MINOR_AXIS_END_3D)) {
            minorAxisEnd = parseCoords(element.getAttribute(ParseElementDefinition.GRAPHIC_MINOR_AXIS_END_3D), element);
        }
    }
    
    protected void cleanup() {
        center = null;
        majorAxisEnd = null;
        minorAxisEnd = null;
        shadowSize = 0;
        super.cleanup();
    }
    
    protected Matrix3D getMatrix(){
        if (matrix == null){
            matrix = getMatrix(minorAxisEnd, majorAxisEnd, center);
        }
        return matrix;
    }
    
    protected Matrix3D getMatrix(Point minorAxisEnd, Point majorAxisEnd, Point center){
        
        Matrix3D result = new Matrix3D();
        result.translate(-center.getX(), -center.getY(), -center.getZ());
        
        // Move major axis to positive x axis
        double majorAxisXDistance = majorAxisEnd.getX() - center.getX();
        double majorAxisYDistance = majorAxisEnd.getY() - center.getY();
        double majorAxisZDistance = majorAxisEnd.getZ() - center.getZ();
        
        if (majorAxisYDistance != 0 || majorAxisZDistance != 0) {
            if (majorAxisZDistance != 0 ) {
                double rotationAngle = -Math.atan2(majorAxisZDistance, majorAxisYDistance);
                result.rotateX(rotationAngle);
            }
            Point transformedMajorAxisEnd = result.transform(majorAxisEnd);
            if (transformedMajorAxisEnd.getY() != 0) {
                double rotationAngle = -Math.atan2(transformedMajorAxisEnd.getY(), transformedMajorAxisEnd.getX());
                result.rotateZ(rotationAngle);
            }
        }
        
        // Move minor axis to x-y plane
        Point transformedMinorAxisEnd = result.transform(minorAxisEnd);
        if (transformedMinorAxisEnd.getZ() != 0) {
            double rotationAngle = -Math.atan2(transformedMinorAxisEnd.getZ(), transformedMinorAxisEnd.getY());
            result.rotateZ(rotationAngle);
            transformedMinorAxisEnd = result.transform(minorAxisEnd);
        }
        
        // Skew the minor axis so that it is perpendicular to the major axis
        if (transformedMinorAxisEnd.getX() != 0) {
            double skewAngle = -Math.atan2(transformedMinorAxisEnd.getX(), transformedMinorAxisEnd.getY());
            result.skewY(skewAngle, 0);
            transformedMinorAxisEnd = result.transform(minorAxisEnd);
        }
        
        // Stretch the minor axis so that it is the same length as the major axis
        double transformedMinorLength = GeometricOperations.distance(transformedMinorAxisEnd);
        if (transformedMinorLength != 0) {
            double majorLength = GeometricOperations.distance(majorAxisXDistance, majorAxisYDistance, majorAxisZDistance);
            result.scaleXYZ(1, majorLength / transformedMinorLength, 1);
        }
        
        result.translate(center.getX(), center.getY(), 0);
        return result;
    }
    
    protected Matrix3D getInverseMatrix() {
        if (inverseMatrix == null) {
            inverseMatrix = getMatrix().getInverseMatrix();
        }
        return inverseMatrix;
    }
    
    protected List<Point> calculateTransformedOvalPoints(Point center, Point edge, double eccentricity) {
        List<Point> result = new ArrayList(12);
        
        // Taken from C++ code
        Point splinePoint1 = new Point();
        Point splinePoint2 = new Point();
        Point splinePoint3 = new Point();
        Point splinePoint4 = new Point();
        
        splinePoint1.setX(edge.getX() - center.getX());
        splinePoint1.setY(edge.getY() - center.getY());
        if (eccentricity != 1) {
            splinePoint2.setX(splinePoint1.getY() * eccentricity);
            splinePoint2.setY(-splinePoint1.getX() * eccentricity);
        } else {
            splinePoint1.setY(GeometricOperations.distance(splinePoint1, new Point(0,0)) * ((splinePoint1.getY() < 0) ? -1 : 1));
            splinePoint1.setX(0);
            splinePoint2.setX(splinePoint1.getY());
            splinePoint2.setY(0);
        }
        splinePoint3.setX(0.55197 * splinePoint1.getX());
        splinePoint3.setY(0.55197 * splinePoint1.getY());
        splinePoint4.setX(0.55197 * splinePoint2.getX());
        splinePoint4.setY(0.55197 * splinePoint2.getY());
        
        // Ordering of the points is important; used by ArcProcessor
        Point centerMinusPoint2 = center.subtract(splinePoint2);
        result.add(centerMinusPoint2.subtract(splinePoint3));
        result.add(centerMinusPoint2);
        result.add(centerMinusPoint2.add(splinePoint3));
        
        Point centerPlusPoint1 = center.add(splinePoint1);
        result.add(centerPlusPoint1.subtract(splinePoint4));
        result.add(centerPlusPoint1);
        result.add(centerPlusPoint1.add(splinePoint4));
        
        Point centerPlusPoint2 = center.add(splinePoint2);
        result.add(centerPlusPoint2.add(splinePoint3));
        result.add(centerPlusPoint2);
        result.add(centerPlusPoint2.subtract(splinePoint3));
        
        Point centerMinusPoint1 = center.subtract(splinePoint1);
        result.add(centerMinusPoint1.add(splinePoint4));
        result.add(centerMinusPoint1);
        result.add(centerMinusPoint1.subtract(splinePoint4));
        
        
        return result;
    }
    
    public Point getCenter() {
        return center;
    }
    
    public void setCenter(Point center) {
        this.center = center;
    }
    
    public Point getMajorAxisEnd() {
        return majorAxisEnd;
    }
    
    public void setMajorAxisEnd(Point majorAxisEnd) {
        this.majorAxisEnd = majorAxisEnd;
    }
    
    public Point getMinorAxisEnd() {
        return minorAxisEnd;
    }
    
    public void setMinorAxisEnd(Point minorAxisEnd) {
        this.minorAxisEnd = minorAxisEnd;
    }
    
    public double getShadowSize() {
        return shadowSize;
    }
    
    public void setShadowSize(double shadowSize) {
        this.shadowSize = shadowSize;
    }
    
}
