package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import translator.ParseElementDefinition;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;

public class PlasmidRegionProcessor extends HollowArrowProcessor{
    //These are the only attributes that are diferentes to the arrow
    private double regionStart;
    private double regionEnd;
    private double regionOffset;
    
    public PlasmidRegionProcessor() {
    }    
    
    protected void configure(){        
        super.configure();
        
        regionStart = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.PLASMID_REGION_START));
        regionEnd = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.PLASMID_REGION_END));
        regionOffset = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.PLASMID_REGION_OFFSET));
    }
    
    protected void cleanup() {
        regionStart = 0;
        regionEnd = 0;
        regionOffset = 0;
        super.cleanup();
    }
    
    public void process() {
        super.process();
        ShapeBuilderConfiguration hollowArrowParts = resultingConfiguration;
        Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
        //add the configurations from the hollow arrow processor
        configurations.add(hollowArrowParts);
        
        CompositeShapeConfiguration resultingConfiguration = 
                new CompositeShapeConfiguration(ParseElementDefinition.PLASMID_MAP, configurations);
        resultingConfiguration.setColor(color);
        resultingConfiguration.setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfiguration);
    }
    
}
