
package translator.processors.cdxml;

import java.util.ArrayList;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;

/**
 * Processes a dxy orbital.
 */
public class DXYOrbitalProcessor extends OrbitalProcessor {
    
    public DXYOrbitalProcessor() {
    }
    
    protected void process() {
        ParsedElement orbital = getElement();
        
        // Taken from C++ code
        
        // build vertical lobes
        SplineConfiguration firstLobe = buildLobeSpline(center, deltaX, deltaY, true);
        SplineConfiguration secondLobe = buildLobeSpline(center, -deltaX, -deltaY, true);
        //Shaded and Filled orbitals must have stroke width
        firstLobe.setStrokeWidth(lineWidth);
        secondLobe.setStrokeWidth(lineWidth);
        
        if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) == -1) {
            firstLobe.setShaded(true);
            firstLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
            secondLobe.setShaded(true);
            secondLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));            
        } else {
            firstLobe.setFill(true);
            firstLobe.setFillColor(color);            
            secondLobe.setFill(true);
            secondLobe.setFillColor(color);            
        }
        
        firstLobe.setColor(color);
        firstLobe.setZOrder(zOrder);
        secondLobe.setColor(color);
        secondLobe.setZOrder(zOrder);
        
        // set id to match shape with gradient
        firstLobe.setId(orbital.getId());
        secondLobe.setId(orbital.getId());
        
        // build horizontal lobes
        SplineConfiguration thirdLobe = buildLobeSpline(center, deltaY, -deltaX, true);
        SplineConfiguration fourthLobe = buildLobeSpline(center, -deltaY, deltaX, true);
        thirdLobe.setStrokeWidth(lineWidth);
        thirdLobe.setColor(color);
        thirdLobe.setZOrder(zOrder);
        thirdLobe.setFill(true);
        thirdLobe.setFillColor(backgroundColor);
        fourthLobe.setStrokeWidth(lineWidth);
        fourthLobe.setColor(color);
        fourthLobe.setZOrder(zOrder);
        fourthLobe.setFill(true);
        fourthLobe.setFillColor(backgroundColor);
        
        // build orbital
        CompositeShapeConfiguration result = new CompositeShapeConfiguration("Orbital"+orbital.getId(), new ArrayList());
        result.addConfiguration(firstLobe);
        result.addConfiguration(secondLobe);
        result.addConfiguration(thirdLobe);
        result.addConfiguration(fourthLobe);
        result.setZOrder(zOrder);
        setResultingConfiguration(result);
    }
}
