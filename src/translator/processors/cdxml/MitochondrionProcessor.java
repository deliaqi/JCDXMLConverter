package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class MitochondrionProcessor extends BioShapeProcessor {
    
    //Taken from C++
    private static final int DEFAULT_FADE_PERCENTAGE = 30;
    
    //Constant to reference the inner and outer spline
    private static final int OUTER_SPLINE = 0;
    private static final int INNER_SPLINE = 1;
    
    public MitochondrionProcessor() {
    }

    protected void createProgram() {
        programScript.append("18.94 336.38 18.94 336.38 9.56 409.88 50.81 511.88 284.06 510.38 501.82 508.97 532.69 426.38 535.69 348.38 537.19 309.4 498 211.88 281.06 211.88 32.23 211.88 ");
        programScript.append("curvepoints ");
        programScript.append("66.56 326.63 66.56 326.63 49.31 328.13 50.06 366.55 63.56 395.14 76.66 422.87 79.87 439.2 50.06 401.73 36 386.25 27 347.25 40.5 321.75 47.87 303.09 63.56 297.56 85.31 302.81 111.38 309.1 115.83 361.32 116.63 391.13 117.53 425.07 155.72 430.89 140.06 382.88 132.38 356.06 123.71 318.9 144.19 276.38 162.2 238.09 191.52 222.51 225.19 244.13 269.81 271.13 202.95 388.11 213 429.75 222.56 469.5 260.38 460.49 267 438 274.5 412.5 259.94 319.43 267.19 285.38 280.69 232.88 311.81 225.38 346.69 243.38 381.64 261.45 365.32 387.74 363.95 403.79 362.19 424.38 382.17 424.38 388.64 408.16 404.87 367.42 382.23 294.33 397.65 274.83 425.28 239.89 459.14 279.18 461.06 306.19 462.56 331.31 454.89 366.22 453.56 391.69 452.25 417 461.44 424.31 475.5 393.94 489.56 365.07 475.31 297.56 493.31 303.56 512.23 309.99 516.19 341.25 511.31 370.5 502.13 411.38 496.69 421.5 480.94 440.63 463.62 461.98 425.01 459.28 428.25 411.75 431.81 366.38 437.14 323.99 434.06 306 430.78 286.83 415.95 285.67 414.19 311.25 412.98 328.85 419.81 414.38 398.83 450.11 377.77 485.94 345.29 477.35 336.75 436.5 328.71 398.06 354 307.24 335.81 287.25 319.69 269.63 290.55 282.25 297.58 324.77 302.81 365.25 314.25 412.5 295.88 456.38 266.27 513.78 199.7 489.9 185.81 457.28 161.06 399.12 214.26 307.11 202.13 284.25 187.49 256.71 137.29 296.2 157.31 363.38 164.58 396.43 168.94 441.38 150.75 451.13 133.58 459.44 124.31 458.9 110.81 436.92 97.31 414.93 104.81 381.75 95.06 357.75 80.81 326.71 ");
        programScript.append("curvepoints ");
    }
    
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes){
        Collection<ShapeBuilderConfiguration> result = super.createSplines(shapes);
        
        SplineConfiguration outerSpline = (SplineConfiguration)((ArrayList)result).get(OUTER_SPLINE);
        SplineConfiguration innerSpline = (SplineConfiguration)((ArrayList)result).get(INNER_SPLINE);
        
        if(isShaded()){
            outerSpline.setFill(false);
            outerSpline.setShaded(isShaded());
            outerSpline.setStrokeWidth(getLineWidth());
            outerSpline.setGradient(
                    RadialGradient.getOvalGradient(
                    getElement().getId(), Color.fadeRGB(getColor(), fadePercent)));
        }
        
        int fadeValue = 0;
        
        if (outerSpline.isFill() || outerSpline.isShaded() || fadePercent < 100){
            fadeValue = DEFAULT_FADE_PERCENTAGE / 2;
        }
        else{
            fadeValue = fadePercent;
        }
        
        innerSpline.setFillColor(Color.fadeRGB(getColor(), fadeValue ));
        
        return result;
    }

}
