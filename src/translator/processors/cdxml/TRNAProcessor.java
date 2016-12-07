package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class TRNAProcessor extends BioShapeProcessor{
    
    public TRNAProcessor() {
    }

    protected void createProgram() {
        // Outer loop
        programScript.append("206.63 280.09 206.63 280.09 206.63 280.09 206.88 217.34 206.88 217.34 206.88 217.34 206.75 192.22 185.75 192.22 185.75 192.22 133.13 192.22 133.13 192.22 133.13 192.22 125.45 210.7 102.25 204.72 82.38 199.59 81.88 168.22 101.13 160.09 119.58 152.31 130.5 167.72 130.5 167.72 130.5 167.72 189.63 167.97 189.63 167.97 189.63 167.97 206.75 168.22 206.5 152.97 206.5 152.97 206.88 62.09 206.88 62.09 206.88 62.09 187.88 51.72 196.75 29.97 203.65 13.06 231 11.84 239.38 28.59 249.88 49.59 231.25 61.59 231.25 61.59 231.25 61.59 231.25 152.34 231.25 152.34 231.25 159.11 251.32 149.12 258.13 157.34 266.5 167.47 244.75 185.47 254.88 185.47 254.88 185.47 310.25 185.72 310.25 185.72 310.25 185.72 321.9 165.18 343.75 176.59 357.63 183.84 361.13 206.97 346.13 216.97 324.81 231.18 311.13 210.09 311.13 210.09 311.13 210.09 243.88 210.22 243.88 210.22 243.88 210.22 231 209.72 231 217.97 231 217.97 231.13 320.22 231.13 320.22 231.13 320.22 ");
        programScript.append("curvepoints ");

        programScript.append("228.38 278.22 moveto ");
        programScript.append("209.13 278.22 lineto ");
        programScript.append("228.38 267.3 moveto ");
        programScript.append("209.13 267.3 lineto ");
        programScript.append("228.38 256.37 moveto ");
        programScript.append("209.13 256.37 lineto ");
        programScript.append("228.38 245.45 moveto ");
        programScript.append("209.13 245.45 lineto ");
        programScript.append("228.38 234.52 moveto ");
        programScript.append("209.13 234.52 lineto ");
        programScript.append("228.38 223.59 moveto ");
        programScript.append("209.13 223.59 lineto ");
        programScript.append("228.38 131.8 moveto ");
        programScript.append("209.13 131.8 lineto ");
        programScript.append("228.38 120.87 moveto ");
        programScript.append("209.13 120.87 lineto ");
        programScript.append("228.38 109.95 moveto ");
        programScript.append("209.13 109.95 lineto ");
        programScript.append("228.38 99.02 moveto ");
        programScript.append("209.13 99.02 lineto ");
        programScript.append("228.38 88.1 moveto ");
        programScript.append("209.13 88.1 lineto ");
        programScript.append("298.35 188.09 moveto ");
        programScript.append("298.35 207.34 lineto ");
        programScript.append("287.42 188.09 moveto ");
        programScript.append("287.42 207.34 lineto ");
        programScript.append("276.5 188.09 moveto ");
        programScript.append("276.5 207.34 lineto ");
        programScript.append("265.57 188.09 moveto ");
        programScript.append("265.57 207.34 lineto ");
        programScript.append("254.65 188.09 moveto ");
        programScript.append("254.65 207.34 lineto ");
        programScript.append("174.55 170.59 moveto ");
        programScript.append("174.55 189.85 lineto ");
        programScript.append("163.62 170.59 moveto ");
        programScript.append("163.62 189.85 lineto ");
        programScript.append("152.7 170.6 moveto ");
        programScript.append("152.7 189.85 lineto ");
        programScript.append("141.77 170.59 moveto ");
        programScript.append("141.77 189.84 lineto ");
    }
    
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes){
        Collection<ShapeBuilderConfiguration> result = super.createSplines(shapes);
        
        ((SplineConfiguration)((ArrayList)result).get(0)).setClosed(false);
        
        SplineConfiguration innerSpline = (SplineConfiguration)((ArrayList)result).get(0);
        innerSpline.getSegments().remove(innerSpline.getSegments().size() - 1);
        //this curve is never dashed
        innerSpline.setDashed(false);
        
        //Increase the stroke width of the line below
        innerSpline.setStrokeWidth(getWidth() * 3);
                
        //Create the above line with background color
        SplineConfiguration innerSpline2 = new SplineConfiguration(innerSpline.getSegments());
        innerSpline2.setStrokeWidth(getWidth());
        innerSpline2.setColor(convertColor(getEnvironment().getBackgroundColor()));
        innerSpline2.setLineJoin(LineJoin.Miter);
        innerSpline2.setLineCap(LineCap.Round);
        //this curve is never dashed
        innerSpline2.setDashed(false);        
        
        result.add(innerSpline2);
        
        //if the TRNA is dashed only the horizontal and vertical inner lines should be made dashed.
        //this lines are the ones between the first and the last curves in the result list.
        if(isDashed()){            
            List<ShapeBuilderConfiguration> trnaCurves = new ArrayList();
            trnaCurves = (ArrayList)result;            
            int i;
            SplineConfiguration trnaSpline;
            for(i = 1; i < trnaCurves.size() - 1; i++){
                trnaSpline = (SplineConfiguration) trnaCurves.get(i);                
                trnaSpline.setDashed(true);
                trnaSpline.setDashLength(hashSpacing);            
            }
        }
        
        if(isShaded()){
            innerSpline.setFill(false);
            innerSpline.setShaded(isShaded());
            innerSpline.setGradient(
                    RadialGradient.getOvalGradient(
                    getElement().getId(), Color.fadeRGB(getColor(), fadePercent)));
        }
        
        return result;
    }
}
