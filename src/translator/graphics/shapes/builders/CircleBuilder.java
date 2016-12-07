package translator.graphics.shapes.builders;

import translator.BuilderConfiguration;
import translator.graphics.shapes.Circle;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class CircleBuilder extends ShapeBuilder {
    
    public CircleBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {
        Circle result = new Circle();
        result.setZOrder(
                ((BuilderConfiguration)getConfiguration()).getZOrder());
        
        CircleConfiguration configuration = (CircleConfiguration) getConfiguration();
        
        // might be shaded so set the id to match shape and gradient
        result.setId(configuration.getId());
        result.setCenter(configuration.getCenter());
        result.setRadius(configuration.getRadius());
        result.setFill(configuration.isFill());
        result.setFillColor(configuration.getFillColor());
        result.setColor(configuration.getColor());
        result.setStrokeStyle(configuration.getStrokeStyle());
        result.setStrokeWidth(configuration.getStrokeWidth());
        result.setDashed(configuration.isDashed());
        result.setGradient(configuration.getGradient());
        result.setDashLength(configuration.getDashLength());
        result.setShaded(configuration.isShaded());
        
        return result;
    }
    
}
