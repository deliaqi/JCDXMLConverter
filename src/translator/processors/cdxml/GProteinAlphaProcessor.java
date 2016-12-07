package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class GProteinAlphaProcessor extends BioShapeProcessor{
    
    public GProteinAlphaProcessor() {
    }

    protected void createProgram() {
        programScript.append("2000 H 1000 W ");
        programScript.append("0 0 moveto ");
        programScript.append("0 H lineto ");
        programScript.append("W 2 div H W H 2 div W 0 curveto ");
        programScript.append("W H 2 div neg W 2 div 0 0 0 curveto ");
    }
    
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
