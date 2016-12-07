
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

public class RadicalProcessor extends SymbolProcessor {
    
    public RadicalProcessor() {
    }

    protected void process() {
        ParsedElement symbol = getElement();
        
        symbolSize = GeometricOperations.distance(symbolStart, symbolEnd);
        
        double halfLineWidth = lineWidth / 2;
        
        if (symbolType != null) {
            if (!symbolType.equalsIgnoreCase(
                    ParseElementDefinition.SYMBOL_TYPE_ELECTRON)) {
                symbolSize *= 2;
            }
            
            // Taken from C++ code
            symbolSize /= 9;
            
            CircleConfiguration electronCircle = new CircleConfiguration(symbolStart, symbolSize);
            electronCircle.setFill(true);
            electronCircle.setStrokeWidth(0);
            electronCircle.setColor(color);
            
            if (symbolType.equalsIgnoreCase(
                    ParseElementDefinition.SYMBOL_TYPE_ELECTRON)) {
                electronCircle.setZOrder(zOrder);
                setResultingConfiguration(electronCircle);
            } else {
                Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
                
                innerShapes.add(electronCircle);
                
                if (symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_LONE_PAIR)) {
                    double xOffset = symbolEnd.getX() - symbolStart.getX();
                    double yOffset = symbolEnd.getY() - symbolStart.getY();
                    Point offsetStart = new Point(symbolStart.getX() + xOffset, symbolStart.getY() + yOffset);
                    CircleConfiguration lonePairCircle = new CircleConfiguration(offsetStart, symbolSize);
                    lonePairCircle.setFill(true);
                    lonePairCircle.setStrokeWidth(0);
                    lonePairCircle.setColor(color);
                    
                    innerShapes.add(lonePairCircle);
                } else if (symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_RADICAL_CATION) ||
                        symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_RADICAL_ANION)) {
                    
                    symbolSize *= 2;
                    
                    double radicalLineWidth = lineWidth * 4 / 5;
                    
                    Point horizontalBegin = new Point(symbolEnd.getX() - symbolSize - halfLineWidth, symbolEnd.getY());
                    Point horizontalEnd = new Point(symbolEnd.getX() + symbolSize + halfLineWidth, symbolEnd.getY());
                    SegmentConfiguration horizontalLine = new SegmentConfiguration(horizontalBegin, horizontalEnd);
                    horizontalLine.setStrokeWidth(radicalLineWidth);
                    horizontalLine.setColor(color);
                    
                    innerShapes.add(horizontalLine);
                    
                    if (symbolType.equalsIgnoreCase(ParseElementDefinition.SYMBOL_TYPE_RADICAL_CATION)) {
                        Point verticalBegin = new Point(symbolEnd.getX(), symbolEnd.getY() - symbolSize - halfLineWidth);
                        Point verticalEnd = new Point(symbolEnd.getX(), symbolEnd.getY() + symbolSize + halfLineWidth);
                        SegmentConfiguration verticalLine = new SegmentConfiguration(verticalBegin, verticalEnd);
                        verticalLine.setStrokeWidth(radicalLineWidth);
                        verticalLine.setColor(color);
                        
                        innerShapes.add(verticalLine);
                    }
                }
                CompositeShapeConfiguration result = new CompositeShapeConfiguration(
                        "Radical"+symbol.getId(),
                        innerShapes);
                result.setZOrder(zOrder);
                setResultingConfiguration(result);
            }
        }
    }
    
}
