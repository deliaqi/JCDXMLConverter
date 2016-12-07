
package translator.graphics.shapes.builders;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;

public abstract class ShapeBuilder {
    
    protected ShapeBuilderConfiguration configuration;
    
    public ShapeBuilder(ShapeBuilderConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public abstract Shape build();
    
    public ShapeBuilderConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ShapeBuilderConfiguration configuration) {
        this.configuration = configuration;
    }
    
}
