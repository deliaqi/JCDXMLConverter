package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class RibosomeAProcessor extends BioShapeProcessor{
    
    public RibosomeAProcessor() {
    }

    protected void createProgram() {
        programScript.append("199.69 169.02 199.69 169.02 139.5 295.96 398.06 300.64 334 170.64 311.88 133.74 302 193.52 277.25 164.27 255.06 138.05 250.66 192.46 226.63 166.27 206.44 144.27 ");
        programScript.append("curvepoints ");
    }

    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
