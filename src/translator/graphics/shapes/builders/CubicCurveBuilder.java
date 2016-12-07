package translator.graphics.shapes.builders;

import translator.graphics.shapes.CubicCurve;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class CubicCurveBuilder extends ShapeBuilder{
    
    public CubicCurveBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {
        CubicCurveConfiguration configuration = (CubicCurveConfiguration) getConfiguration();
        
        CubicCurve result = new CubicCurve();
        
        result.setZOrder(configuration.getZOrder());
        
        result.setBeginPoint(configuration.getBeginPoint());
        result.setEndPoint(configuration.getEndPoint());
        result.setControlPoint(configuration.getControlPoint());
        result.setControlPoint2(configuration.getControlPoint2());
        
        if(configuration.getStrokeStyle() != null){
            result.setStrokeStyle(configuration.getStrokeStyle());
        }
        
        result.setColor(configuration.getColor());
        
        return result;
    }
    
}
