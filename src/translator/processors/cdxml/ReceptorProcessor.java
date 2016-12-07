package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.processors.cdxml.BioShapeProcessor.CDPParam;
import translator.utils.Point;

public class ReceptorProcessor extends BioShapeProcessor{
    
    public ReceptorProcessor() {
    }
    
    protected void createProgram() {
        programScript.append("2000 R 500 r 1000 h1 1000 h2 1000 h3 1000 h4 ");
        programScript.append("2 r h1 0 control ");
        programScript.append("0 0 moveto ");
        programScript.append("R 2 mul 0 R h1 r h1 curve ");
        programScript.append("0 h2 line ");
        programScript.append("R 3 mul 0 R 1.5 mul h3 h4 add R 1.5 mul h3 h4 add curve ");
        programScript.append("0 h4 neg 0 h4 neg R 1.5 mul r add neg h4 neg curve ");
        programScript.append("finishx ");
    }

    protected void configure() {
        super.configure();
        
        double neckWidth = Double.parseDouble(
                getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_NECK_WIDTH)) * 20;
        
        programParameters.add(new CDPParam(2000));
        programParameters.add(new CDPParam(neckWidth));
        programParameters.add(new CDPParam(1000));
        programParameters.add(new CDPParam(1000));
        programParameters.add(new CDPParam(1000));
        programParameters.add(new CDPParam(1000));
    }
    
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
