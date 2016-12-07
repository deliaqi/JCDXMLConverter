package translator.processors.cdxml;

import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.utils.Point;
import translator.utils.GeometricOperations;

/**
 * The base class for Geometries and Constrains
 * 
 */
public class Properties3DProcessor extends ArcProcessor {
            
    protected List<Point> basisElementsPosition;
    protected String type;
    
    public Properties3DProcessor() {
    }
    
    /**
     * Find the point on a given plane closest to a given point.
     * Result is undefined if the point lies on the plane (?).
     */
    protected Point pointToPlane(Point pt, Point planeOrigin, Point planeNormal){
        Point originToPoint = pt.subtract(planeOrigin);

        // dot product; can be negative
        double distance = GeometricOperations.distance(planeNormal);

        double distToPlane = originToPoint.dotProduct(planeNormal.byScalar(1 / distance));
        // The vector we're after is parallel to the normal, and scaled by the distance to the point.
        // Its direction is opposite the normal if the point lies "below" the plane.
        return planeNormal.byScalar(distToPlane);
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
}
