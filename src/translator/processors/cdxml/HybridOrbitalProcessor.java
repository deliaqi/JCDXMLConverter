
package translator.processors.cdxml;

import java.util.ArrayList;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;

/**
 * Processes a hybrid orbital.
 */
public class HybridOrbitalProcessor extends OrbitalProcessor {
    
    public HybridOrbitalProcessor() {
    }
    
    protected void process() {
        ParsedElement orbital = getElement();
        
        // Taken from C++ code
        
        // build first smaller lobe
        SplineConfiguration firstLobe = buildLobeSpline(center, -2*deltaX/5, -2*deltaY/5, false);
        if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_HYBRID_PLUS)) {
            if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) == -1) {
                /* set a stroke width of lineWidth so shaded attribute looks similar
                   ChemDraw sets an outline to the gradient */
                firstLobe.setStrokeWidth(lineWidth);
                firstLobe.setShaded(true);
                firstLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
            } else { // filled
                firstLobe.setFill(true);
                firstLobe.setFillColor(color);
                firstLobe.setStrokeWidth(lineWidth);
            }
        } else { // hybridMinus orbital
            firstLobe.setStrokeWidth(lineWidth);
            // make it opaque
            firstLobe.setFill(true);
            firstLobe.setFillColor(backgroundColor);
        }
        firstLobe.setColor(color);
        firstLobe.setZOrder(zOrder);
        
        // build second lobe
        SplineConfiguration secondLobe = buildLobeSpline(center, deltaX, deltaY, false);
        if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_HYBRID_MINUS)) {
            if (orbitalType.indexOf(ParseElementDefinition.ORBITAL_TYPE_FILLED) == -1) {
                /* set a stroke width of lineWidth so shaded attribute looks similar
                   ChemDraw sets an outline to the gradient */
                secondLobe.setStrokeWidth(lineWidth);
                secondLobe.setShaded(true);
                secondLobe.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
            } else { // filled
                secondLobe.setFill(true);
                secondLobe.setFillColor(color);
                secondLobe.setStrokeWidth(lineWidth);
            }
        } else { // hybridPlus orbital
            secondLobe.setStrokeWidth(lineWidth);
            // make it opaque
            secondLobe.setFill(true);
            secondLobe.setFillColor(backgroundColor);
        }
        secondLobe.setColor(color);
        secondLobe.setZOrder(zOrder);
        
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
