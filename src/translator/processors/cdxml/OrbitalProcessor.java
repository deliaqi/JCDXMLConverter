
package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class OrbitalProcessor extends GraphicProcessor {
    
    protected String orbitalType;
    protected String ovalType;
    protected double deltaX;
    protected double deltaY;
    protected Color backgroundColor;
    
    public OrbitalProcessor() {
    }
    
    protected void configure() {
        super.configure();
        
        ParsedElement orbital = getElement();
        
        if (orbital.hasAttribute(ParseElementDefinition.BOUNDING_BOX)) {
            List<Point> orbitalPoints = parsePoints(
                    orbital.getAttribute(ParseElementDefinition.BOUNDING_BOX), orbital); // returns center and major axis end points
            majorAxisEnd = orbitalPoints.get(0);
            center = orbitalPoints.get(1);
        }
        
        if (orbital.hasAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE)) {
            ovalType = orbital.getAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE);
        }
        
        if (orbital.hasAttribute(ParseElementDefinition.GRAPHIC_ORBITAL_TYPE)) {
            orbitalType = orbital.getAttribute(ParseElementDefinition.GRAPHIC_ORBITAL_TYPE);
        }
        
        // initialize deltas = major axis length
        deltaX = majorAxisEnd.getX() - center.getX();
        deltaY = majorAxisEnd.getY() - center.getY();
        
        backgroundColor = convertColor(environment.getBackgroundColor());
        
        setAttributesForDrawingElement();
    }
    
    protected void process() {
        ParsedElement orbital = getElement();
        
        SplineConfiguration result = buildLobeSpline(center, deltaX, deltaY, false);
        
        if (orbitalType != null) {
            if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) != -1) {
                // orbital is filled
                result.setFill(true);
                result.setStrokeWidth(0);
            } else if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_SHADED) != -1) {
                // orbital is shaded
                result.setShaded(true);
                result.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                /* set a stroke width of lineWidth so shaded attribute looks similar
                   ChemDraw sets an outline to the gradient */
                result.setStrokeWidth(lineWidth);
            } else {
                result.setFill(true);
                result.setFillColor(backgroundColor);
                result.setStrokeWidth(lineWidth);
            }
        } else {
            // assume hollow orbital
            result.setFill(true);
            result.setFillColor(backgroundColor);
            result.setStrokeWidth(lineWidth);
        }
        
        // set id to match shape with gradient
        result.setId(orbital.getId());
        
        result.setColor(color);
        result.setZOrder(zOrder);
        
        setResultingConfiguration(result);
    }
    
    // Constructs a spline for a single lobe
    protected SplineConfiguration buildLobeSpline(Point center, double deltaX, double deltaY, boolean dxyOrbital) {
        // Taken from C++ code
        Point point1;
        Point point2;
        Point point3;
        Point point4;
        Point point5;
        Point point6;
        
        List<Point> lobeSplinePoints = new ArrayList();
        
        if (dxyOrbital) {
            point1 = new Point();
            point2 = new Point(deltaY * 0.35228, deltaX * -0.35228);
            point3 = new Point(deltaX * 0.35741, deltaY * 0.35741);
        } else {
            point1 = new Point(deltaY * 0.15625, deltaX * -0.15625);
            point2 = new Point(deltaY * 0.29124, deltaX * -0.29124);
            point3 = new Point(deltaX * 0.51, deltaY * 0.51);
        }
        point4 = new Point(deltaX * 0.6675, deltaY * 0.6675);
	point5 = new Point(deltaX * 0.86, deltaY * 0.86);
	point6 = new Point(deltaY/4, -deltaX/4);
        
        // convenience point to use deltaX and deltaY in calculations
        Point deltaPoint = new Point(deltaX, deltaY);
        
        lobeSplinePoints.add(center.subtract(point1));
        lobeSplinePoints.add(center);
        lobeSplinePoints.add(center.add(point1));
        Point centerP2 = center.add(point2);
        lobeSplinePoints.add(centerP2.add(point3));
        lobeSplinePoints.add(centerP2.add(point4));
        lobeSplinePoints.add(centerP2.add(point5));
        Point centerDelta = center.add(deltaPoint);
        lobeSplinePoints.add(centerDelta.add(point6));
        lobeSplinePoints.add(centerDelta);
        lobeSplinePoints.add(centerDelta.subtract(point6));
        Point centerMinusP2 = center.subtract(point2);
        lobeSplinePoints.add(centerMinusP2.add(point5));
        lobeSplinePoints.add(centerMinusP2.add(point4));
        lobeSplinePoints.add(centerMinusP2.add(point3));
        lobeSplinePoints.add(lobeSplinePoints.get(0)); // control point for last curve
        lobeSplinePoints.add(lobeSplinePoints.get(1)); // end point for last curve
        lobeSplinePoints.add(lobeSplinePoints.get(2)); // needed by buildSplineBeziers, will be ignored
        
        SplineConfiguration result = buildSplineBeziers(lobeSplinePoints);
        result.setClosed(true);
        return result;
    }
    
    protected void cleanup() {
        ovalType = null;
        orbitalType = null;
        deltaX = 0;
        deltaY = 0;
        super.cleanup();
    }
    
    protected String getTypeString() {
        return getOvalType();
    }
    
    protected String getOvalType() {
        String result;
        if (ovalType != null) {
            result = ovalType;
        } else {
            result = ""; // ovalType can't be null
        }
        return result;
    }
    
    protected void setOvalType(String ovalType) {
        this.ovalType = ovalType;
    }
}
