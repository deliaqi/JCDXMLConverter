package translator.processors.cdxml;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.EllipseConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class OvalProcessor extends CommonCircleProcessor{
    
    //Constant to access the Bounding box points
    private static final int TOP_LEFT_CORNER = 0;
    private static final int BOTTOM_RIGHT_CORNER = 1;
    
    protected Point shadowMajorAxisEnd;
    protected Point shadowMinorAxisEnd;
    protected Matrix3D shadowMatrix;
    protected Point shadowCenter;
    
    public OvalProcessor() {
    }
    
    protected void cleanup() {
        shadowCenter = null;
        shadowMajorAxisEnd = null;
        shadowMinorAxisEnd = null;
        shadowMatrix = null;
        matrix = null;
        inverseMatrix = null;
        super.cleanup();
    }
    
    protected void process(){
        ShapeBuilderConfiguration resultingConfiguration = null;
        EllipseConfiguration configuration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        ParsedElement oval = getElement();
        
        shadowMajorAxisEnd = new Point(getMajorAxisEnd());
        shadowMinorAxisEnd = new Point(getMinorAxisEnd());
        shadowCenter = new Point(center);
        
        double majorAxisAngle = GeometricOperations.angle(majorAxisEnd.getX(), majorAxisEnd.getY(),
                center.getX(), center.getY());
        double minorAxisAngle = GeometricOperations.angle(minorAxisEnd.getX(), minorAxisEnd.getY(),
                center.getX(), center.getY());
        
        if (!isDashed()){
            majorAxisEnd = GeometricOperations.offset(majorAxisEnd, majorAxisAngle, getLineWidth() / 2);
            minorAxisEnd = GeometricOperations.offset(minorAxisEnd, minorAxisAngle, getLineWidth() / 2);
        }
        List<SegmentConfiguration> ellipseSegments = new ArrayList();
        
        Point transformedCenter = getMatrix().transform(center);
        Point transformedMajorAxisEnd = getMatrix().transform(majorAxisEnd);
        
        List<Point> ovalPoints = calculateTransformedOvalPoints(transformedCenter, transformedMajorAxisEnd, 1);
        for (Point ovalPoint : ovalPoints) {
            Point transformedArcPoint = getInverseMatrix().transform(ovalPoint);
            ovalPoint.setX(transformedArcPoint.getX());
            ovalPoint.setY(transformedArcPoint.getY());
        }
        
        // Add last closing curve
        Point endPoint = ovalPoints.get(1);
        Point controlPoint2 = ovalPoints.get(0);
        
        ovalPoints.add(controlPoint2);
        ovalPoints.add(endPoint);
        ovalPoints.add(ovalPoints.get(2)); // Needed by buildSplineBeziers, will be ignored
        
        SplineConfiguration ellipse = buildSplineBeziers(ovalPoints);
        
        // set id to match shape and gradient in case it's shaded
        ellipse.setId(oval.getId());
        
        ellipse.setColor(getColor());
        ellipse.setStrokeWidth(getWidth());
        ellipse.setFill(isFilled());
        ellipse.setDashed(isDashed());
        ellipse.setDashLength(hashSpacing);
        ellipse.setClosed(true);
        
        boolean isOrbital = orbitalType != null ;
        
        // orbital must not be opaque
        if (isOrbital && !isFilled() && !isShaded()) {
            ellipse.setFill(true);
            if(faded){
                ellipse.setFillColor(fadedColor);
            }else{
                ellipse.setFillColor(backgroundColor);
            }
        }
        
        //if the Oval is faded and not is a Orbital
        if(faded && !isOrbital ){
            ellipse.setFill(true);
            ellipse.setFillColor(fadedColor);
        }
        
        if(isShaded()){
            ellipse.setShaded(isShaded());
            ellipse.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
        }
        
        if (isShadowed()) {
            Area ellipseArea = convertSplineToArea(ovalPoints);
            innerShapes.add(createShadow(ellipseArea));
        }
        
        innerShapes.add(ellipse);
        
        resultingConfiguration = new CompositeShapeConfiguration("Oval", innerShapes);
        ((CompositeShapeConfiguration)resultingConfiguration).setZOrder(getZOrder());
        setResultingConfiguration(resultingConfiguration);
    }
    
    private ShapeBuilderConfiguration createShadow(Area ellipseArea){
        
        ParsedElement oval = getElement();
        
        double majorAxisAngle = GeometricOperations.angle(shadowMajorAxisEnd.getX(), shadowMajorAxisEnd.getY(),
                shadowCenter.getX(), shadowCenter.getY());
        double minorAxisAngle = GeometricOperations.angle(shadowMinorAxisEnd.getX(), shadowMinorAxisEnd.getY(),
                shadowCenter.getX(), shadowCenter.getY());
        
        double offset = Math.hypot(getShadowSize(), getShadowSize()) * getLineWidth();
        
        double offsetAngle = minorAxisAngle + (majorAxisAngle - minorAxisAngle) / 2;
        
        Point offsetCenter = GeometricOperations.offset(shadowCenter, offsetAngle, 0.001);
        
        boolean inside = GeometricOperations.isInsideTriangle(shadowMinorAxisEnd, shadowCenter, shadowMajorAxisEnd, offsetCenter);
        
        if (inside){
            offsetCenter = GeometricOperations.offset(shadowCenter, offsetAngle, offset);
        }else{
            offsetCenter = GeometricOperations.offset(shadowCenter, offsetAngle, -offset);
        }
        
        double deltaX = offsetCenter.getX() - shadowCenter.getX();
        double deltaY = offsetCenter.getY() - shadowCenter.getY();
        
        List<SegmentConfiguration> ellipseSegments = new ArrayList<SegmentConfiguration>();
        
        translator.utils.Point transformedCenter = getShadowMatrix().transform(center);
        translator.utils.Point transformedMajorAxisEnd = getShadowMatrix().transform(shadowMajorAxisEnd);
        translator.utils.Point transformedMinorAxisEnd = getShadowMatrix().transform(shadowMinorAxisEnd);
        
        List<translator.utils.Point> ovalPoints = calculateTransformedOvalPoints(transformedCenter, transformedMajorAxisEnd, 1);//CalculateShadowTransformedOvalPoints(transformedCenter, transformedMajorAxisEnd, transformedMinorAxisEnd, 1);
        for(translator.utils.Point ovalPoint : ovalPoints){
            translator.utils.Point transformedArcPoint = getInverseMatrix().transform(ovalPoint);
            ovalPoint.setX(transformedArcPoint.getX());
            ovalPoint.setY(transformedArcPoint.getY());
        }
        
        // Add last closing curve
        translator.utils.Point endPoint = ovalPoints.get(1);
        translator.utils.Point controlPoint2 = ovalPoints.get(0);
        
        ovalPoints.add(controlPoint2);
        ovalPoints.add(endPoint);
        ovalPoints.add(ovalPoints.get(2)); // Needed by buildSplineBeziers, will be ignored
        
        Area shadowArea = convertSplineToArea(ovalPoints);
        
        shadowArea.transform(AffineTransform.getTranslateInstance(deltaX, deltaY));
        
        shadowArea.subtract(ellipseArea);
        
        SplineConfiguration shadowConfig = new SplineConfiguration(environment.createConfigurationFromArea(shadowArea));
        
        shadowConfig.setFillColor(getColor().fadeRGB(getColor(), CDXMLEnvironment.getInstance().getShadowRatio()));
        shadowConfig.setFill(true);
        shadowConfig.setClosed(true);
        
        return shadowConfig;
    }
    
    protected Matrix3D getShadowMatrix() {
        if (shadowMatrix == null) {
            shadowMatrix = getMatrix(shadowMinorAxisEnd, shadowMajorAxisEnd, shadowCenter);
        }
        return shadowMatrix;
    }
    
}

