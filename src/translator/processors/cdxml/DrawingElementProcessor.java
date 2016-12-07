package translator.processors.cdxml;

import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Vector;

/**
 * Class that calculate the major axis, minor axis and center for the
 * drawing elements when doesn't exist in the cdxml
 */
public class DrawingElementProcessor extends GraphicProcessor{
        
    public DrawingElementProcessor() {
    }
    
    protected void configure(){
        super.configure();
        
    }
    
    /**
     * Calculate the Major Axis, Minor Axis and Center
     */
    protected void calculateBasicAttributes(){
        ParsedElement element = getElement();
        
        if (element.hasAttribute(ParseElementDefinition.GRAPHIC_CENTER_3D)) {
            center = parseCoords(element.getAttribute(ParseElementDefinition.GRAPHIC_CENTER_3D), element);
        }else{
            calculateDefaultCenter();
        }
        
        if (element.hasAttribute(ParseElementDefinition.GRAPHIC_MAJOR_AXIS_END_3D)) {
            majorAxisEnd = parseCoords(element.getAttribute(ParseElementDefinition.GRAPHIC_MAJOR_AXIS_END_3D), element);
        }else{
            calculateDefaultMajorAxis();
        }
        
        if (element.hasAttribute(ParseElementDefinition.GRAPHIC_MINOR_AXIS_END_3D)) {
            minorAxisEnd = parseCoords(element.getAttribute(ParseElementDefinition.GRAPHIC_MINOR_AXIS_END_3D), element);
        }else{
            calculateDefaultMinorAxis();
        }
    }
    
    /**
     * Algorithm taken from C++ code
     * If there is no center then calculate it
     */
    private void calculateDefaultCenter(){        
        List<Point> boundingBoxPoints = parsePoints(boundingBox, getElement());
        
        if(getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_OVAL) ||
                getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_ORBITAL)){
            center = boundingBoxPoints.get(1);
            
        }else if(getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE)){
            double distance = GeometricOperations.distance(boundingBoxPoints.get(FIRST_ELEMENT), boundingBoxPoints.get(SECOND_ELEMENT));
            double angle = GeometricOperations.angle(boundingBoxPoints.get(SECOND_ELEMENT), boundingBoxPoints.get(FIRST_ELEMENT));
            center = GeometricOperations.offset(boundingBoxPoints.get(SECOND_ELEMENT), angle, distance / 2);
        }
    }
    
    /**
     * Algorithm taken from C++ code
     * If there is no major Axis then calculate it
     */
    private void calculateDefaultMajorAxis(){        
        List<Point> boundingBoxPoints = parsePoints(boundingBox, getElement());
        
        if(getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_OVAL) ||
                getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_ORBITAL)){
            majorAxisEnd = boundingBoxPoints.get(FIRST_ELEMENT);
            
        }else if(getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE)){
            majorAxisEnd = new Point(boundingBoxPoints.get(FIRST_ELEMENT).getX(), center.getY());
        }
    }
    
    /**
     * Algorithm taken from C++ code
     * If there is no minor Axis then calculate it
     */
    private void calculateDefaultMinorAxis(){        
        List<Point> boundingBoxPoints = parsePoints(boundingBox, getElement());
        
        if(getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_OVAL)  ||
                getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_ORBITAL)){
            
            Vector minorAxis = new Vector(boundingBoxPoints.get(FIRST_ELEMENT).subtract(boundingBoxPoints.get(SECOND_ELEMENT)));
            boolean isOval = false;
            
            if(getElement().hasAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE)){
                if(!getElement().getAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE).contains(ParseElementDefinition.GRAPHIC_CIRCLE)){
                    isOval = true;
                }
            }else if(getElement().hasAttribute(ParseElementDefinition.GRAPHIC_ORBITAL_TYPE)){
                isOval = true;
            }
            
            if(isOval){
                minorAxis = minorAxis.scaleTo(minorAxis.getLength() * DEFAULT_OVAL_ECCENTRICITY);                                
            }
            
            minorAxisEnd = new Point(boundingBoxPoints.get(SECOND_ELEMENT).getX() - minorAxis.getY(), boundingBoxPoints.get(SECOND_ELEMENT).getY() + minorAxis.getX());
        }else if(getElement().getAttribute(ParseElementDefinition.GRAPHIC_TYPE).equalsIgnoreCase(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE)){
            minorAxisEnd = new Point(center.getX(), boundingBoxPoints.get(FIRST_ELEMENT).getY());
            
        }
    }
    
}
