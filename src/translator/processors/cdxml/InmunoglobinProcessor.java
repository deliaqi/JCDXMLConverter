package translator.processors.cdxml;

import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class InmunoglobinProcessor extends BioShapeProcessor{
    
    public InmunoglobinProcessor() {
    }

    protected void createProgram() {
        programScript.append("193.99 235.92 193.99 248.68 193.99 261.45 193.99 334.32 193.99 334.32 193.99 334.32 193.77 353.9 204.75 361.41 215.72 368.92 248.19 389.73 255.84 394.41 266.25 400.79 272.63 392.53 261.75 385.97 254.07 381.33 222.4 360.55 214.5 353.53 206.88 346.77 206.25 335.19 206.25 319.41 206.25 303.64 207.38 248.68 207.38 234.41 207.38 220.14 194.82 219.64 193.88 233.91 192.93 248.18 ");
        programScript.append("curvepoints ");
        programScript.append("175.23 235.92 175.23 248.68 175.23 261.45 175.23 334.32 175.23 334.32 175.23 334.32 175.45 353.9 164.47 361.41 153.5 368.92 121.03 389.73 113.38 394.41 102.97 400.79 96.59 392.53 107.47 385.97 115.15 381.33 146.82 360.55 154.72 353.53 162.34 346.77 162.97 335.19 162.97 319.41 162.97 303.64 161.84 248.68 161.84 234.41 161.84 220.14 174.4 219.64 175.34 233.91 176.29 248.18 ");
        programScript.append("curvepoints ");
        programScript.append("217.53 341.81 227.22 349.32 236.91 356.83 257.75 371.1 267.94 377.11 281.13 384.91 285.56 377.71 273.56 369.09 264.88 362.85 241.79 346.79 233.06 340.78 224.34 334.77 ");
        programScript.append("curvepoints ");
        programScript.append("151.69 341.81 142 349.32 132.31 356.83 111.47 371.1 101.28 377.11 88.09 384.91 83.66 377.71 95.66 369.09 104.34 362.85 127.43 346.79 136.16 340.78 144.88 334.77 ");
        programScript.append("curvepoints ");
    }

    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
