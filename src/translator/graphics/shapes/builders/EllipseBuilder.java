package translator.graphics.shapes.builders;

import translator.BuilderConfiguration;
import translator.graphics.shapes.Ellipse;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.EllipseConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class EllipseBuilder extends ShapeBuilder{
        
    public EllipseBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }
    
    public Shape build() {
        Ellipse result = new Ellipse();
        result.setZOrder(
                ((BuilderConfiguration)getConfiguration()).getZOrder());
        
        EllipseConfiguration configuration = (EllipseConfiguration) getConfiguration();
        
        result.setCenter(configuration.getCenter());
        result.setRadiusX(configuration.getRadiusX());
        result.setRadiusY(configuration.getRadiusY());
        result.setFill(configuration.isFill());
        result.setColor(configuration.getColor());
        result.setStrokeStyle(configuration.getStrokeStyle());
        result.setStrokeWidth(configuration.getStrokeWidth());
        result.setDashed(configuration.isDashed());
        result.setDashLength(configuration.getDashLength());
        result.setRotationAngle(configuration.getRotationAngle());
        
        return result;
    }
    
}
