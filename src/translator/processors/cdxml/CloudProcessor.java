package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class CloudProcessor extends BioShapeProcessor{
    
    public CloudProcessor() {
    }

    protected void createProgram() {
        programScript.append("0.0 219 0.92 171 2.42 123 54.92 95.25 100.67 93.75 147.15 92.23 169.67 108.75 169.67 108.75 169.67 108.75 162.17 15.75 254.42 14.25 342.16 12.82 339.92 81 347.42 81 347.42 81 353.42 51.78 409.67 53.25 495.92 55.5 468.92 147.75 468.92 147.75 468.92 147.75 528.17 180.75 528.17 234 528.17 287.25 466.72 329.74 425.42 330.75 363.92 332.25 321.92 294 321.92 294 321.92 294 305.42 338.25 270.92 338.25 236.42 338.25 217.67 300 217.67 300 217.67 300 196.69 328.5 140.42 328.5 64.67 328.5 68.42 249.75 68.42 249.75 68.42 249.75 ");
        programScript.append("curvepoints ");
    }

    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
    
}
