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

public class SubstrateEnzyme1Processor extends BioShapeProcessor{
   
    protected static final int BIO_SHAPE_ENZYME_RECEPTOR_SIZE_SCALE_FACTOR = 10;
    protected static final int BIO_SHAPE_ENZYME_RECEPTOR_A_CONTROL = 500;
    
    private double enzymeReceptorSize;
    
    public SubstrateEnzyme1Processor() {
    }

    protected void createProgram() {
        programScript = new StringBuilder();
        
        programScript.append("500 a 1000 b ");
        programScript.append("1 b a sub 0 4 control ");
        programScript.append("a b add neg 0 moveto ");
        programScript.append("a b add neg a b neg b 0 b curveto ");
        programScript.append("b b  b a 2 div add a  b a curveto ");
        programScript.append("b a 2 div sub a  b a sub a 2 div b a sub 0 curveto ");
        programScript.append("finishy ");
    }

    protected void configure() {
        super.configure();
        
        double enzymeReceptorSize = Double.parseDouble(
                getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_ENZYME_RECEPTOR_SIZE)) * BIO_SHAPE_ENZYME_RECEPTOR_SIZE_SCALE_FACTOR;
        
        programParameters.add(new CDPParam(BIO_SHAPE_ENZYME_RECEPTOR_A_CONTROL));
        programParameters.add(new CDPParam(enzymeReceptorSize));
    }

    protected double getParameter(int index) {
        double result = 0;
        
        double firstParameterValue = programParameters.get(FIRST_ELEMENT).getValue();
        double secondParameterValue = programParameters.get(SECOND_ELEMENT).getValue();
        switch (index) {
            case 0:{
                result = Math.min(firstParameterValue,secondParameterValue);
                break;
            }
            case 1:{
                result = Math.max(firstParameterValue,secondParameterValue);
                break;
            }
        }
        
        return result;
    }
    
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes) {
        return createShadedSplines(shapes);
    }
}
