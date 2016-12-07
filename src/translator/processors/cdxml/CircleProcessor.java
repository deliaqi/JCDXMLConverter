package translator.processors.cdxml;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Vector;

public class CircleProcessor extends CommonCircleProcessor{

    // stroke width for shadow
    // this avoids a thin space between the circle and the shadow
    private static final double SHADOW_STROKE_WIDTH = 0.07;
    
    //Constant to access the Bounding box points
    private static final int TOP_LEFT_CORNER = 0;
    private static final int BOTTOM_RIGHT_CORNER = 1;
    
    public CircleProcessor() {
    }
    
    public void process(){
        ShapeBuilderConfiguration resultingConfiguration = null;
        CircleConfiguration configuration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
  
        //if there is no minor  Axis then calculate it
        if (minorAxisEnd == null && getBoundingBox() != null){
            minorAxisEnd =  CalculateMinorAxisEnd();
        }
        
        double realRadiusX = GeometricOperations.distance(majorAxisEnd, center);
        double realRadiusY = GeometricOperations.distance(minorAxisEnd, center);
        
        // shrink radius and diameter because line width grows toward the circle's inside
        // when circle is not dashed
        double radiusX;
        double radiusY;
        if (!isDashed()) {
            radiusX = realRadiusX - getWidth() / 2;
            radiusY = realRadiusY - getWidth() / 2;
        } else {
            radiusX = realRadiusX;
            radiusY = realRadiusY;
        }
        
        double realDiameterX = realRadiusX * 2;
        double realDiameterY = realRadiusY * 2;
        
        double diameterX = radiusX * 2;
        double diameterY = radiusY * 2;
        
        double majorAxisAngle = GeometricOperations.angle(majorAxisEnd.getX(), majorAxisEnd.getY(),
                center.getX(), center.getY());
        double minorAxisAngle = GeometricOperations.angle(minorAxisEnd.getX(), minorAxisEnd.getY(),
                center.getX(), center.getY());
        
        if(isFaded()){
            CircleConfiguration fadeConfig = new CircleConfiguration(center, radiusX);
            ((CircleConfiguration) fadeConfig).setFillColor(getFadedColor());
            ((CircleConfiguration) fadeConfig).setFill(true);
            innerShapes.add(fadeConfig);
        }
        
        if(isShadowed()){
            
            double offset = Math.hypot(getShadowSize(), getShadowSize()) * getLineWidth();
            
            //This two offsets are for obtain the correct cordinate of the left corner point of the 
            //circle
            Point leftCorner = GeometricOperations.offset(center, Math.PI, diameterX/2);
            leftCorner = GeometricOperations.offset(leftCorner, Math.PI/2, -diameterY/2);
            
            //This offset is for evite a withe line between the shadow and the circle border.
            leftCorner = GeometricOperations.offset(leftCorner, GeometricOperations.angle(center,leftCorner), width * 3/4);
            
            double offsetAngle = minorAxisAngle + (majorAxisAngle - minorAxisAngle) / 2;
            
            Point testCenterShadowPoint = GeometricOperations.offset(center, offsetAngle, GeometricOperations.distance(center, majorAxisEnd) / 2);
            
            boolean inside = GeometricOperations.isInsideTriangle(minorAxisEnd, center, majorAxisEnd, testCenterShadowPoint);
            
            if(!inside){
                offsetAngle += Math.PI;
            }
            
            //This is for flipped circles.
            if(Math.sin(majorAxisAngle - minorAxisAngle) > 0){
                offsetAngle += Math.PI/2;
            }
            
            
            //Apply an offset for create the shadow.
            Point leftCornerShadow = GeometricOperations.offset(leftCorner, offsetAngle, offset -width * 3/2);
            
            Area shadowArea = convertOvalToArea(leftCornerShadow.getX(), leftCornerShadow.getY(),
                    realDiameterX, realDiameterY);
            Area circleArea = convertOvalToArea(leftCorner.getX(), leftCorner.getY(),
                    realDiameterX, realDiameterY);

            shadowArea.subtract(circleArea);
            
            SplineConfiguration shadowConfig = new SplineConfiguration(
                    getEnvironment().createConfigurationFromArea(shadowArea));
            ((SplineConfiguration) shadowConfig).setColor(getColor().fadeRGB(getColor(), CDXMLEnvironment.getInstance().getShadowRatio()));
            ((SplineConfiguration) shadowConfig).setFill(true);
            ((SplineConfiguration) shadowConfig).setStrokeWidth(SHADOW_STROKE_WIDTH);
            innerShapes.add(shadowConfig);
            
        }
        
        configuration = new CircleConfiguration(center, radiusX);
        // set id to match shape and gradient in case it's shaded
        configuration.setId(getElement().getId());
        ((CircleConfiguration) configuration).setColor(getColor());
        ((CircleConfiguration) configuration).setStrokeWidth(getWidth());
        ((CircleConfiguration) configuration).setFill(isFilled());
        ((CircleConfiguration) configuration).setDashed(isDashed());
        
        if(isShaded()){
            ((CircleConfiguration) configuration).setShaded(isShaded());
            ((CircleConfiguration) configuration).setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
        }
        
        ((CircleConfiguration) configuration).setDashLength(hashSpacing);
        
        // orbital must not be opaque
        if (orbitalType != null && !isFilled() && !isShaded() && !isFaded()) {
            ((CircleConfiguration) configuration).setFill(true);
            ((CircleConfiguration) configuration).setFillColor(convertColor(environment.getBackgroundColor()));
        }
        innerShapes.add(configuration);
        
        resultingConfiguration = new CompositeShapeConfiguration("Oval", innerShapes);
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(getZOrder());
        setResultingConfiguration(resultingConfiguration);
    }
    
    protected Point CalculateMinorAxisEnd(){
        Point calculatedMinorAxisEnd;
        
        List<Point> boundingBox = parsePoints(getBoundingBox(), getElement());
        Point beginPointBB = boundingBox.get(TOP_LEFT_CORNER);
        Point endPointBB = boundingBox.get(BOTTOM_RIGHT_CORNER);
        //Calculate the MajorAxis as ChemDraw C++ does
        Point CDMajorAxisEnd = beginPointBB.subtract(endPointBB);
        Vector vec = new Vector(- CDMajorAxisEnd.getY(), CDMajorAxisEnd.getX());
        calculatedMinorAxisEnd = new Point(vec.getX(), vec.getY());
        calculatedMinorAxisEnd = calculatedMinorAxisEnd.add(center);      
        
        return calculatedMinorAxisEnd;
    }
}
