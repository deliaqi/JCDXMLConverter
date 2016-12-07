package translator.graphics.shapes.builders;

import translator.BuilderConfiguration;
import translator.graphics.shapes.Segment;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.ShapeBuilderFactory;
import translator.graphics.shapes.Spline;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;

public class SplineBuilder extends ShapeBuilder {
    
    public SplineBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {       
        SplineConfiguration configuration = (SplineConfiguration) getConfiguration();
        
        Spline result = new Spline();
        result.setZOrder(
                ((BuilderConfiguration)getConfiguration()).getZOrder());
        
        for(SegmentConfiguration segment : configuration.getSegments()){
            ShapeBuilder segmentBuilder = ShapeBuilderFactory.getInstance().getBuilder(segment);
            segment.setStrokeWidth(configuration.getStrokeWidth());
            result.addShape((Segment) segmentBuilder.build());
        }
        
        result.setId(configuration.getId());
        result.setFill(configuration.isFill());
        result.setClosed(configuration.isClosed());
        result.setColor(configuration.getColor());
        result.setFillColor(configuration.getFillColor());
        result.setStrokeStyle(configuration.getStrokeStyle());
        result.setStrokeWidth(configuration.getStrokeWidth());
        result.setFillRule(configuration.getFillRule());
        result.setLineCap(configuration.getLineCap());
        result.setDashed(configuration.isDashed());
        result.setDashLength(configuration.getDashLength());
        result.setShaded(configuration.isShaded());
        result.setGradient(configuration.getGradient());
        result.setMoveX(configuration.getMoveX());
        result.setMoveY(configuration.getMoveY());
        result.setLineJoin(configuration.getLineJoin());
        result.setScale(configuration.getScale());
        result.setTranslate(configuration.isTranslate());
        result.setMiterLimit(configuration.getMiterLimit());        
        
        return result;
    }
    
}
