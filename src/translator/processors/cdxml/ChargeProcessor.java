
package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class ChargeProcessor extends SymbolProcessor {
    
    public ChargeProcessor() {
    }

    protected void process() {
        ParsedElement symbol = getElement();
        
        double chargeLineWidth = lineWidth * 4 / 5;
        
        symbolSize = GeometricOperations.distance(symbolStart, symbolEnd) * 4 / 9;
        
        double halfLineWidth = lineWidth / 2;
        
        if (symbolType != null) {
            Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
            
            if (symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_CIRCLE_MINUS) ||
                    symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_CIRCLE_PLUS)) {
                CircleConfiguration circle = new CircleConfiguration(symbolStart, symbolSize);
                circle.setStrokeWidth(chargeLineWidth);
                circle.setColor(color);
                
                innerShapes.add(circle);
            }
            
            symbolSize = GeometricOperations.distance(symbolStart, symbolEnd) * 2 / 9;
            
            Point horizontalBegin = new Point(symbolStart.getX() - symbolSize - halfLineWidth, symbolStart.getY());
            Point horizontalEnd = new Point(symbolStart.getX() + symbolSize + halfLineWidth, symbolStart.getY());
            SegmentConfiguration horizontalLine = new SegmentConfiguration(horizontalBegin, horizontalEnd);
            horizontalLine.setStrokeWidth(chargeLineWidth);
            horizontalLine.setColor(color);
            
            innerShapes.add(horizontalLine);
            
            if (symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_CIRCLE_PLUS) ||
                    symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_PLUS)) {
                Point verticalBegin = new Point(symbolStart.getX(), symbolStart.getY() - symbolSize - halfLineWidth);
                Point verticalEnd = new Point(symbolStart.getX(), symbolStart.getY() + symbolSize + halfLineWidth);
                SegmentConfiguration verticalLine = new SegmentConfiguration(verticalBegin, verticalEnd);
                verticalLine.setStrokeWidth(chargeLineWidth);
                verticalLine.setColor(color);
                
                innerShapes.add(verticalLine);
            }
            
            CompositeShapeConfiguration result = new CompositeShapeConfiguration(
                    "Charge"+symbol.getId(),
                    innerShapes);
            result.setZOrder(zOrder);
            setResultingConfiguration(result);            
        }
    }
    
}
