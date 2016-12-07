
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

public class DaggerProcessor extends SymbolProcessor {
    
    public DaggerProcessor() {
    }
    
    protected void process() {
        ParsedElement symbol = getElement();
        
        double daggerLineWidth = lineWidth * 4 / 5;
        
        symbolSize = GeometricOperations.distance(symbolStart, symbolEnd) / 5;
        
        double halfLineWidth = lineWidth / 2;
        
        if (symbolType != null) {
            Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
            
            
            Point verticalBegin = new Point(symbolStart.getX(), symbolStart.getY() - 2 * symbolSize - halfLineWidth);
            Point verticalEnd = new Point(symbolStart.getX(), symbolStart.getY() + 2 * symbolSize + halfLineWidth);
            SegmentConfiguration verticalLine = new SegmentConfiguration(verticalBegin, verticalEnd);
            verticalLine.setStrokeWidth(daggerLineWidth);
            verticalLine.setColor(color);
            
            innerShapes.add(verticalLine);
            
            Point horizontalHighBegin = new Point(symbolStart.getX() - symbolSize - halfLineWidth, symbolStart.getY() - symbolSize);
            Point horizontalHighEnd = new Point(symbolStart.getX() + symbolSize + halfLineWidth, symbolStart.getY() - symbolSize);
            SegmentConfiguration horizontalHighLine = new SegmentConfiguration(horizontalHighBegin, horizontalHighEnd);
            horizontalHighLine.setStrokeWidth(daggerLineWidth);
            horizontalHighLine.setColor(color);
            
            innerShapes.add(horizontalHighLine);
            if (symbolType != null){
                if (symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_DOUBLE_DAGGER)) {
                    Point horizontalLowBegin = new Point(symbolStart.getX() - symbolSize - halfLineWidth, symbolStart.getY() + symbolSize);
                    Point horizontalLowEnd = new Point(symbolStart.getX() + symbolSize + halfLineWidth, symbolStart.getY() + symbolSize);
                    SegmentConfiguration horizontalLowLine = new SegmentConfiguration(horizontalLowBegin, horizontalLowEnd);
                    horizontalLowLine.setStrokeWidth(daggerLineWidth);
                    horizontalLowLine.setColor(color);
                    
                    innerShapes.add(horizontalLowLine);
                }
            }
            CompositeShapeConfiguration chargeConfiguration = new CompositeShapeConfiguration(
                    "Charge"+symbol.getId(),
                    innerShapes);
            chargeConfiguration.setZOrder(getZOrder());
            setResultingConfiguration(chargeConfiguration);
        }
    }
    
}
