package translator.graphics.shapes.builders;

import translator.graphics.shapes.CompositeShape;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.ShapeBuilderFactory;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;

public class CompositeShapeBuilder extends ShapeBuilder {
    
    public CompositeShapeBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {
        CompositeShape result = new CompositeShape();
        result.setZOrder(
                ((CompositeShapeConfiguration)getConfiguration()).getZOrder());
        
        for(ShapeBuilderConfiguration configuration : 
            ((CompositeShapeConfiguration)getConfiguration()).getConfigurations()){
            
            ShapeBuilder builder = ShapeBuilderFactory.getInstance().getBuilder(configuration);
            
            result.addShape(builder.build());
        }
        
        result.setId(((CompositeShapeConfiguration)getConfiguration()).getId());
            
        return result;
    }
    
}
