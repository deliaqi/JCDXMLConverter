
package translator.graphics.shapes.builders;

import translator.graphics.shapes.Arc;
import translator.graphics.shapes.Segment;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.ArcConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class ArcBuilder extends ShapeBuilder {
    
    public ArcBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {       
        ArcConfiguration configuration = (ArcConfiguration) getConfiguration();
        
        Arc result = new Arc();
        
        result.setBeginPoint(configuration.getBeginPoint());
        result.setEndPoint(configuration.getEndPoint());        
        result.setAngle(configuration.getAngle());
        result.setXRadius(configuration.getXRadius());
        result.setYRadius(configuration.getYRadius());
        result.setLargeArc(configuration.isLargeArc());
        result.setSweepPositive(configuration.isSweepPositive());
        
        if(configuration.getStrokeStyle() != null){
            result.setStrokeStyle(configuration.getStrokeStyle());
        }
        
        result.setColor(configuration.getColor());
        
        return result;
    }
    
}
