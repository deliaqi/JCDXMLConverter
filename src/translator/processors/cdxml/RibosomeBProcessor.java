package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class RibosomeBProcessor extends BioShapeProcessor{
    
    public RibosomeBProcessor() {
    }

    protected void createProgram() {
        programScript.append("201.23 267.33 199 289.12 196.88 309.87 205.5 276.37 222.63 294.5 240.21 313.11 243.12 305.69 254.5 295.75 265.38 286.25 267.49 286.5 277.63 296 287.63 305.37 288.27 311.74 310.25 294 328.38 279.37 336.75 316.12 333.5 290.5 330.74 268.76 322.54 260.04 297.75 253.87 273.13 247.75 253 248 234.38 254.5 216.95 260.58 ");
        programScript.append("curvepoints ");
    }

    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
