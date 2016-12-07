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

public class GProteinGamaProcessor extends BioShapeProcessor{
    
    //To transform the CDXML values in screen coordinates
    private static final double SCALE_FACTOR = 20;
    
    public static final int BIO_SHAPE_G_PROTEIN_GAMMA_H1_PARAMETER = 500;
    public static final int BIO_SHAPE_G_PROTEIN_GAMMA_H2_PARAMETER = 1000;
    public static final int BIO_SHAPE_G_PROTEIN_GAMMA_W_PARAMETER = 1000;
    
    public GProteinGamaProcessor() {
    }

    protected void createProgram() {
        programScript.append("500 H1 1000 H2 1000 W ");
        programScript.append("1 0 H1 2 control ");
        programScript.append("0 H1 moveto ");
        programScript.append("W 2 div H1 W 2 div H1 W 0 curveto ");
        programScript.append("W 2 div H2 neg W 2 div H2 neg 0 H2 neg curveto ");
        programScript.append("finishx ");
    }

    protected void configure() {
        super.configure();
        
        double upperHeight = -1;
        double lowerHeight = -1;
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_UPPER_HEIGHT)){
            upperHeight = Double.parseDouble(
                    getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_UPPER_HEIGHT));
        }
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_LOWER_HEIGHT)){
            lowerHeight = Double.parseDouble(
                    getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_LOWER_HEIGHT));
        }
        
        if(upperHeight < 0){
            programParameters.add(new CDPParam(BIO_SHAPE_G_PROTEIN_GAMMA_H1_PARAMETER));
        } else {
            programParameters.add(new CDPParam(upperHeight * SCALE_FACTOR));
        }
        
        if(lowerHeight < 0){
            programParameters.add(new CDPParam(BIO_SHAPE_G_PROTEIN_GAMMA_H2_PARAMETER));
        } else {
            programParameters.add(new CDPParam(lowerHeight * SCALE_FACTOR));
        }
        
        programParameters.add(new CDPParam(BIO_SHAPE_G_PROTEIN_GAMMA_W_PARAMETER));
    }
    
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
