package translator.graphics.shapes.builders;

import translator.graphics.shapes.QuadraticCurve;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.QuadraticCurveConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class QuadraticCurveBuilder extends ShapeBuilder{

    public QuadraticCurveBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {
        QuadraticCurveConfiguration configuration = (QuadraticCurveConfiguration) getConfiguration();
        
        QuadraticCurve curve = new QuadraticCurve();
        
        curve.setZOrder(configuration.getZOrder());
        
        curve.setBeginPoint(configuration.getBeginPoint());
        curve.setEndPoint(configuration.getEndPoint());
        
        curve.setControlPoint(configuration.getControlPoint());
        
        if(configuration.getStrokeStyle() != null){
            curve.setStrokeStyle(configuration.getStrokeStyle());
        }
        
        curve.setColor(configuration.getColor());
        
        return curve;
    }
    
}
