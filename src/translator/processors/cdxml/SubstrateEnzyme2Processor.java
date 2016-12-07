package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class SubstrateEnzyme2Processor extends BioShapeProcessor{
    
    public SubstrateEnzyme2Processor() {
    }

    protected void createProgram() {
        programScript = new StringBuilder();
        
        programScript.append("1000 w 400 h ");
        programScript.append("w 0 moveto ");
        programScript.append("w h add h neg gycurveto ");
        programScript.append("h neg h 3 mul neg gycurveto ");
        programScript.append("w 2 mul neg 0 gxcurveto ");
        programScript.append("w 3 mul 2 div neg h 1.2 mul gycurveto ");
        programScript.append("w neg h 1.2 mul lineto ");
        programScript.append("w neg h 2.2 mul lineto ");
        programScript.append("w h 2 div add neg h 2.2 mul lineto ");
        programScript.append("w h 3 mul gycurveto ");
        programScript.append("w h 3 mul add h 2 mul gxcurveto ");
        programScript.append("w h 2 mul add h gycurveto ");
        programScript.append("w h add h lineto ");
        programScript.append("w 0 gxcurveto ");
    }

    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
