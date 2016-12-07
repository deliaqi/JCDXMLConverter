
package translator.graphics.shapes.builders;

import translator.BuilderConfiguration;
import translator.graphics.shapes.Segment;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class SegmentBuilder extends ShapeBuilder {
    
    public SegmentBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {       
        SegmentConfiguration configuration = (SegmentConfiguration) getConfiguration();
        
        Segment result = new Segment();
        result.setZOrder(
                ((BuilderConfiguration)getConfiguration()).getZOrder());
        
        result.setBeginPoint(configuration.getBeginPoint());
        result.setEndPoint(configuration.getEndPoint());
        
        result.setStrokeWidth(configuration.getStrokeWidth());
        
        if(configuration.getStrokeStyle() != null){
            result.setStrokeStyle(configuration.getStrokeStyle());
        }
        
        result.setColor(configuration.getColor());
        result.setDashed(configuration.isDashed());
        result.setDashLength(configuration.getDashLength());
        result.setLineCap(configuration.getLineCap());
        result.setMoveTo(configuration.isMoveTo());
        result.setShapeRendering(configuration.getShapeRendering());
        
        return result;
    }
    
}
