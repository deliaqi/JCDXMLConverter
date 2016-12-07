package translator.graphics.shapes.builders.configurations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import translator.BuilderConfiguration;
import translator.graphics.Color;

public class CompositeShapeConfiguration extends BuilderConfiguration implements ShapeBuilderConfiguration {
    
    private String id;
    private Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
    
    public CompositeShapeConfiguration(String id, Collection<ShapeBuilderConfiguration> configurations) {
        super(COMPOSITE_SHAPE_BUILDER_ID);
        
        this.id = id;
        this.configurations.addAll(configurations);
    }
    
    public void addConfiguration(ShapeBuilderConfiguration configuration) {
        configurations.add(configuration);
    }
    
    public Collection<ShapeBuilderConfiguration> getConfigurations() {
        return Collections.unmodifiableCollection(configurations);
    }

    public void setConfigurations(Collection<ShapeBuilderConfiguration> configurations) {
        this.configurations = configurations;
    }

    public String getId() {
        return id;
    }
    
    public void setColor(Color newColor){
        for(ShapeBuilderConfiguration configuration : getConfigurations()){
            if(((BuilderConfiguration)configuration).getColor() == null){
                ((BuilderConfiguration)configuration).setColor(newColor);
            }
        }
    }
    
    public void setFill(boolean fill){
        for(ShapeBuilderConfiguration configuration : getConfigurations()){
            ((BuilderConfiguration)configuration).setFill(fill);
        }
    }
    
    public void setStrokeWidth(double strokeWidth){
        for(ShapeBuilderConfiguration configuration : getConfigurations()){
            ((BuilderConfiguration)configuration).setStrokeWidth(strokeWidth);
        }
    }
}
