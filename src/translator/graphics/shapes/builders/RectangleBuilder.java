package translator.graphics.shapes.builders;

import translator.BuilderConfiguration;
import translator.graphics.shapes.Rectangle;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.RectangleConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeRendering;
import translator.utils.Point;

public class RectangleBuilder extends ShapeBuilder {
    
    public RectangleBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {
        Rectangle result = new Rectangle();
        result.setZOrder(
                ((BuilderConfiguration)getConfiguration()).getZOrder());
        
        RectangleConfiguration configuration = 
                (RectangleConfiguration) getConfiguration();
        
        result.setBeginPoint(configuration.getBeginPoint());
        result.setEndPoint(configuration.getEndPoint());
        result.setFill(configuration.isFill());
        result.setColor(configuration.getColor());
        result.setShapeRendering(ShapeRendering.crispEdges);
        
        return result;
    }
    
}
