package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import translator.ParseElementDefinition;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class PlasmidMapProcessor extends CDXMLProcessor {
    private double ringRadius;
    private Point centerPoint;
    
    public PlasmidMapProcessor() {
    }    
    
    protected void configure(){
        super.configure();
        ringRadius = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.PLASMID_MAP_RING_RADIUS)) / 65536;
        centerPoint = parseCoords(getElement().getAttribute(ParseElementDefinition.PLASMID_MAP_P), getElement());
    }
    
    protected void cleanup() {
        ringRadius = 0;
        centerPoint = null;
        
        super.cleanup();
    }
    
    public void process(){
        Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
        
        //taken from C++ code
        double tickOffset = ringRadius / 20;
        
        Point bottomTick = GeometricOperations.offset(centerPoint, -Math.PI / 2, ringRadius - tickOffset);
        Point topTick = GeometricOperations.offset(centerPoint, -Math.PI / 2, ringRadius + tickOffset);
        
        SegmentConfiguration tickConfiguration = new SegmentConfiguration(bottomTick, topTick);
        tickConfiguration.setColor(color);
        tickConfiguration.setStrokeWidth(lineWidth);
        
        configurations.add(tickConfiguration);
        
        CompositeShapeConfiguration resultingConfigurationPlasmidmap = 
                new CompositeShapeConfiguration(ParseElementDefinition.PLASMID_MAP, configurations);        
        resultingConfigurationPlasmidmap.setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfigurationPlasmidmap);        
    }
}
