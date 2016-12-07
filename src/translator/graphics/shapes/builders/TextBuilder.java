package translator.graphics.shapes.builders;

import translator.BuilderConfiguration;
import translator.graphics.shapes.CompositeShape;
import translator.graphics.shapes.CompositeText;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.Text;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.utils.Point;

public class TextBuilder extends ShapeBuilder {

    public TextBuilder(ShapeBuilderConfiguration configuration) {
        super(configuration);
    }

    public Shape build() {
        CompositeText result = new CompositeText();
        
        CompositeTextConfiguration configuration = (CompositeTextConfiguration) getConfiguration();
        
        result.setId(configuration.getId());
        
        result.setJustification(configuration.getJustification());
        result.setRotationAngle(configuration.getRotationAngle());
        result.setLineHeight(configuration.getLineHeight());
        
        result.setZOrder(configuration.getZOrder());
        result.setDisplay(configuration.isDisplay());
        result.setBoundingBox(configuration.getBoundingBox());
        
        for (TextConfiguration lineConfiguration : configuration.getLines()) {
            Text line = new Text();
            
            line.setPoint(new Point(lineConfiguration.getX(), lineConfiguration.getY()));
            //This value indicates inter-word space in addition to the default space between words.
            line.setwordSpacing(lineConfiguration.getWordSpacing());
            
            // parts ids are their indexes in the list
            int partIndex = 0;
            for(String value : lineConfiguration.getParts()){
                String partId = String.valueOf(partIndex++);
                line.addPart(value, lineConfiguration.getFont(partId), lineConfiguration.getColor(partId));
            }
            
            result.addShape(line);
        }
        
        return result;
    }
    
}
