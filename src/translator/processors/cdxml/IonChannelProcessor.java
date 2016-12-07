package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class IonChannelProcessor extends BioShapeProcessor{
    
    public IonChannelProcessor() {
    }

    protected void createProgram() {
        programScript = new StringBuilder();
        
        programScript.append("500 w 1000 h ");
        programScript.append("1.5 w mul h 2 mul moveto ");
        programScript.append("1.5 w mul h 2 mul w h w 0 curveto ");
        programScript.append("w 3 mul h 2 mul neg gycurveto ");
        programScript.append("w 4 mul 0 gxcurveto ");
        programScript.append("w 2 mul h gycurveto ");
        programScript.append("1.5 w mul h 2 mul gycurveto ");
        programScript.append("flipx");
    }

    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
