
package translator.processors.cdxml;

import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.Point;

public class WavyLineProcessor extends CDXMLProcessor {
    
    public WavyLineProcessor() {
    }

    protected void process() {
        ParsedElement wavyLine = getElement();
        
        if (wavyLine.hasAttribute(ParseElementDefinition.ARROW_HEAD_3D) &&
                wavyLine.hasAttribute(ParseElementDefinition.ARROW_TAIL_3D) &&
                wavyLine.hasAttribute(ParseElementDefinition.ARROW_LINE_WIDTH)) {
            
            Point beginPoint = parseCoords(wavyLine.getAttribute(ParseElementDefinition.ARROW_HEAD_3D), wavyLine);
            Point endPoint = parseCoords(wavyLine.getAttribute(ParseElementDefinition.ARROW_TAIL_3D), wavyLine);
            
            List<SegmentConfiguration> wavySegments = environment.getWavySegments(beginPoint, endPoint, boldWidth);
            
            SplineConfiguration wavySpline = new SplineConfiguration(wavySegments);
            wavySpline.setStrokeWidth(lineWidth);
            wavySpline.setColor(color);
            wavySpline.setLineCap(LineCap.Round);
            wavySpline.setZOrder(zOrder);
            setResultingConfiguration(wavySpline);
        } else {
            setResultingConfiguration(null);
        }
    }
    
}
