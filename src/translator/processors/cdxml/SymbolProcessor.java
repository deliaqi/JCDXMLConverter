
package translator.processors.cdxml;

import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.processors.Processor;
import translator.utils.Point;

public abstract class SymbolProcessor extends CDXMLProcessor {
    
    protected double symbolSize;
    protected Point symbolStart;
    protected Point symbolEnd;
    protected String symbolType;
    
    public SymbolProcessor() {
    }
    
    protected void configure() {
        super.configure();
        ParsedElement symbol = getElement();
        CDXMLEnvironment environment = (CDXMLEnvironment) symbol.getEnvironment();
        
        if (symbol.hasAttribute(ParseElementDefinition.SYMBOL_TYPE)) {
            symbolType = symbol.getAttribute(ParseElementDefinition.SYMBOL_TYPE);
        }
        if (boundingBox != null) {
            List<Point> symbolPoints = parsePoints(boundingBox, symbol); // returns start and end points
            symbolStart = symbolPoints.get(0);
            symbolEnd = symbolPoints.get(1);
        }
    }
    
    protected void cleanup() {
        symbolSize = 0;
        symbolStart = null;
        symbolEnd = null;
        symbolType = null;
        super.cleanup();
    }
    
}
