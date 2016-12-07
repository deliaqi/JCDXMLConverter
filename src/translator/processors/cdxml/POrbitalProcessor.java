
package translator.processors.cdxml;

import java.util.ArrayList;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;

/**
 * Processes a p orbital.
 */
public class POrbitalProcessor extends OrbitalProcessor {
    
    public POrbitalProcessor() {
    }
    
    protected void process() {
        ParsedElement orbital = getElement();
        
        // Taken from C++ code
        
        // build first lobe
        SplineConfiguration firstLobe = buildLobeSpline(center, deltaX, deltaY, false);
        firstLobe.setColor(color);
        firstLobe.setZOrder(zOrder);
        if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) == -1) {
            /* set a stroke width of lineWidth/3 so shaded attribute looks similar
               ChemDraw sets an outline to the gradient */
            firstLobe.setStrokeWidth(lineWidth);
            firstLobe.setShaded(true);
            firstLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
        } else { // filled
            firstLobe.setStrokeWidth(lineWidth);
            firstLobe.setFill(true);
            firstLobe.setFillColor(color);
        }
        
        // build second lobe
        SplineConfiguration secondLobe = buildLobeSpline(center, -deltaX, -deltaY, false);
        secondLobe.setStrokeWidth(lineWidth);
        secondLobe.setColor(color);
        secondLobe.setZOrder(zOrder);
        // make it opaque
        secondLobe.setFill(true);
        secondLobe.setFillColor(backgroundColor);
        
        // set id to match shape with gradient
        firstLobe.setId(orbital.getId());
        secondLobe.setId(orbital.getId());
        
        // build orbital
        CompositeShapeConfiguration result = new CompositeShapeConfiguration("Orbital"+orbital.getId(), new ArrayList());
        result.addConfiguration(firstLobe);
        result.addConfiguration(secondLobe);
        result.setZOrder(zOrder);
        setResultingConfiguration(result);
    }
}
