package translator.graphics.shapes.builders;

import translator.graphics.shapes.EmbeddedObject;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.ImageConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;

/**
 * Class that builds an Image Proxy from an Image Configuration
 * 
 */
public class ImageBuilder extends ShapeBuilder{
    
    public ImageBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }
    
    public Shape build() {       
        ImageConfiguration configuration = (ImageConfiguration) getConfiguration();
        
        EmbeddedObject result = new EmbeddedObject();
        result.setZOrder(((ImageConfiguration)getConfiguration()).getZOrder());                
        result.setImageName(configuration.getImageName());        
        result.setImageBytes(configuration.getImageBytes());
        result.setCornerPoint(configuration.getCornerPoint());
        result.setWidth(configuration.getWidth());
        result.setHeight(configuration.getHeight());
        result.setRotate(configuration.isRotate());
        result.setRotationAngle(configuration.getRotationAngle());
        result.setRotationCenter(configuration.getRotationCenter());
                
        return result;
    }
    
}
