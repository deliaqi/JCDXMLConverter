
package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

/**
 * Processes a dz2 orbital.
 */
public class DZ2OrbitalProcessor extends OrbitalProcessor {
    
    // Taken from C++ code
    private static Point[] dz2Collar = {
        new Point(-0.44556, -0.07935),
        new Point(-0.44556, 0.00000),
        new Point(-0.44556, 0.09918),
        new Point(-0.14343, 0.13733),
        new Point(0.00000, 0.13733),
        new Point(0.14343, 0.13733),
        new Point(0.44556, 0.09918),
        new Point(0.44556, 0.00000),
        new Point(0.44556, -0.07935),
        new Point(0.22125, -0.12207),
        new Point(0.14038, -0.12665),
        new Point(0.14038, -0.12665),
        new Point(0.14038, -0.12665),
        new Point(0.14038, -0.12665),
        new Point(0.10681, -0.07935),
        new Point(0.07019, 0.00000),
        new Point(0.00000, 0.00000),
        new Point(-0.07019, 0.00000),
        new Point(-0.10681, -0.07935),
        new Point(-0.14038, -0.12665),
        new Point(-0.14038, -0.12665),
        new Point(-0.14038, -0.12665),
        new Point(-0.14038, -0.12665),
        new Point(-0.22125, -0.12207)};
    
    public DZ2OrbitalProcessor() {
    }
    
    protected void process() {
        ParsedElement orbital = getElement();
        
        // Taken from C++ code
        
        // build lobes
        SplineConfiguration firstLobe = buildLobeSpline(center, deltaX, deltaY, false);
        SplineConfiguration secondLobe = buildLobeSpline(center, -deltaX, -deltaY, false);
        if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_DZ2_MINUS)) {
            if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) == -1) {
                firstLobe.setShaded(true);
                firstLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                secondLobe.setShaded(true);
                secondLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                /* set a stroke width of lineWidth so shaded attribute looks similar
                   ChemDraw sets an outline to the gradient */
                firstLobe.setStrokeWidth(lineWidth);
                secondLobe.setStrokeWidth(lineWidth);
            } else { // filled
                firstLobe.setFill(true);
                secondLobe.setFill(true);
                firstLobe.setFillColor(color);
                secondLobe.setFillColor(color);
                firstLobe.setStrokeWidth(0);
                secondLobe.setStrokeWidth(0);
            }
        } else { // dz2Plus orbital
            firstLobe.setStrokeWidth(lineWidth);
            secondLobe.setStrokeWidth(lineWidth);
            // make them opaque
            firstLobe.setFill(true);
            secondLobe.setFill(true);
            firstLobe.setFillColor(backgroundColor);
            secondLobe.setFillColor(backgroundColor);
        }
        firstLobe.setColor(color);
        secondLobe.setColor(color);
        firstLobe.setZOrder(zOrder);
        secondLobe.setZOrder(zOrder);
        
        SplineConfiguration dz2CollarSpline = buildDZ2CollarSpline(center, -deltaX, -deltaY);
        if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_DZ2_PLUS)) {
            if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) == -1) {
                dz2CollarSpline.setShaded(true);
                dz2CollarSpline.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                /* set a stroke width of lineWidth so shaded attribute looks similar
                   ChemDraw sets an outline to the gradient */
                dz2CollarSpline.setStrokeWidth(lineWidth);
            } else { // filled
                dz2CollarSpline.setFill(true);
                dz2CollarSpline.setFillColor(color);
                dz2CollarSpline.setStrokeWidth(0);
            }
        } else { // dz2Minus orbital
            dz2CollarSpline.setStrokeWidth(lineWidth);
            // make it opaque
            dz2CollarSpline.setFill(true);
            dz2CollarSpline.setFillColor(backgroundColor);
        }
        dz2CollarSpline.setColor(color);
        
        // set id to match shape with gradient
        firstLobe.setId(orbital.getId());
        secondLobe.setId(orbital.getId());
        dz2CollarSpline.setId(orbital.getId());
        
        // build orbital
        CompositeShapeConfiguration result = new CompositeShapeConfiguration("Orbital"+orbital.getId(), new ArrayList());
        result.addConfiguration(firstLobe);
        result.addConfiguration(secondLobe);
        result.addConfiguration(dz2CollarSpline);
        result.setZOrder(zOrder);
        setResultingConfiguration(result);
    }
    
    // Taken from C++ code
    // Builds a spline for the dz2 collar, using the points defined above
    protected SplineConfiguration buildDZ2CollarSpline(Point center, double deltaX, double deltaY) {
        List<Point> collarSplinePoints = new ArrayList();
        for (Point dz2Point : dz2Collar) {
            collarSplinePoints.add(new Point(
                    center.getX() + deltaX * dz2Point.getY() + deltaY * dz2Point.getX(),
                    center.getY() + deltaY * dz2Point.getY() - deltaX * dz2Point.getX()));
        }
        
        collarSplinePoints.add(collarSplinePoints.get(0)); // control point for last curve
        collarSplinePoints.add(collarSplinePoints.get(1)); // end point for last curve
        collarSplinePoints.add(collarSplinePoints.get(2)); // needed by buildSplineBeziers, will be ignored
        
        SplineConfiguration result = buildSplineBeziers(collarSplinePoints);
        result.setClosed(true);
        return result;
    }
}
