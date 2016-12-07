package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.processors.cdxml.BioShapeProcessor.CDPParam;
import translator.processors.cdxml.BioShapeProcessor.CurveParameters;
import translator.utils.AlgebraicOperations;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class DNAProcessor extends BioShapeProcessor{
    
    private static final int DISTANCE_DEFAULT_VALUE = 0;
    
    private double waveLength;
    private double waveWidth;
    private double waveHeight;
    private double waveOffset;
    
    //taken from C++ code
    private static final double CONVERT_PERCENTAGE_MIN_VALUE = 0.1;
    private static final double CONVERT_PERCENTAGE_MIN_VALUE_LENGHT = 0.2;
    
    private List<CurveParameters> dNACurveParameters;
    
    public DNAProcessor() {
    }
    
    protected void configure() {
        super.configure();
        
        waveLength = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_DNA_WAVE_LENGTH));
        waveWidth = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_DNA_WAVE_WIDTH));
        waveHeight = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_DNA_WAVE_HEIGHT));
        waveOffset = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_DNA_WAVE_OFFSET));
        
    }
    
    public void processParameters(){
        programParameters.add(new CDPParam(waveHeight));
        programParameters.add(new CDPParam(waveLength / 2));
        programParameters.add(new CDPParam(waveWidth));
        programParameters.add(new CDPParam(waveOffset));
        
        flipY = false;
        
        dNACurveParameters = new ArrayList();
        if(getElement().getElements(ParseElementDefinition.BIO_SHAPE_CURVE).size() > 0){
            for(ParsedElement curve : getElement().getElements(ParseElementDefinition.BIO_SHAPE_CURVE)){
                CurveParameters parameter = new CurveParameters();
                if(curve.getElements(ParseElementDefinition.COLOR).size() > 0){
                    parameter.setColor(convertColor(curve.getElements(ParseElementDefinition.COLOR).get(0)));
                }else{
                    parameter.setColor(convertColor(getEnvironment().getForegroundColor()));
                }
                dNACurveParameters.add(parameter);
            }
        }else{
            //If the curve count is zero those are the foregroundColor is used by default.
            CurveParameters parameter = new CurveParameters();
            parameter.setColor(getColor());
            dNACurveParameters.add(parameter);
            dNACurveParameters.add(parameter);
        }
    }
    
    protected void validate(){
        
        // Taken from C++
        double minValueWaveHeight = (getEnvironment().getBondLength() * CONVERT_PERCENTAGE_MIN_VALUE );
        double minValueWaveLenght = (getEnvironment().getBondLength() * CONVERT_PERCENTAGE_MIN_VALUE_LENGHT );
        double minValueWaveWidth = (getEnvironment().getBondLength() * CONVERT_PERCENTAGE_MIN_VALUE );
        double minValueWaveOffset = 0;
        
        if(waveHeight < minValueWaveHeight){
            waveHeight = minValueWaveHeight;
        }
        if(waveLength < minValueWaveLenght){
            waveLength = minValueWaveLenght;
        }
        if(waveWidth < minValueWaveWidth){
            waveWidth = minValueWaveWidth;
        }
        if(waveOffset < minValueWaveOffset){
            waveOffset = minValueWaveOffset;
        }
        
    }
    protected void cleanup() {
        flipY = true;
        
        waveLength = 0;
        waveWidth = 0;
        waveHeight = 0;
        waveOffset = 0;
        
        super.cleanup();
    }
    
    protected void createProgram() {
        programScript.append("1350 h 900 l 225 d 300 f ");
        programScript.append("1 f h 2 control  2 l h 0 control  3 l d add h 0 control  4 f h 2 div 0 control ");
        programScript.append("0 0 moveto l 2 div 0 l 2 div h l h curve d hline l 2 div neg 0 l 2 div neg h neg l neg h neg curve d neg hline");
    }
    
    protected void process() {
        
        processParameters();
        //Create the Bio shape script.
        programScript = new StringBuilder();
        createProgram();
        
        //Tokenize the string of script.
        programWords = new ArrayList();
        createProgramWords();
        
        List<CDPControl> programControlParameters = new ArrayList();
        
        //Process the script.
        List<List<Point>> shapes = processAlgorithm(programParameters, programControlParameters);
        
        // leave the transform to be the identity
        // points were already translated
        transformationMatrix = new Matrix2D();
        
        //Convert list of points into spline.
        Collection<ShapeBuilderConfiguration> configurations = createSplines(shapes);
        
        CompositeShapeConfiguration resultingConfiguration =
                new CompositeShapeConfiguration(bioShapeType, configurations);
        
        resultingConfiguration.setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    protected List<List<Point>> processAlgorithm(List<CDPParam> programParameters, List<CDPControl> programControlParameters){
        List<List<Point>> result = new ArrayList();
        
        List<List<Point>> shapes = super.processAlgorithm(programParameters, programControlParameters);
        
        List<Point> shape = shapes.get(FIRST_ELEMENT);
        
        //Calculate the length of the shape.
        double fullLength = GeometricOperations.distance(majorAxis, position) * 2;
        double length = programParameters.get(SECOND_ELEMENT).getValue();
        //Calculate the number of DNA waves to repeat.
        int wavesNumber = 0;
        if(length != 0) {
            wavesNumber = (int)Math.abs(fullLength / length);
        }
        wavesNumber = Math.max(wavesNumber, 1);
        
        Matrix2D transformationMatrix = new Matrix2D();
        transformationMatrix.rotate(Math.atan2(majorAxis.getY() - position.getY(), majorAxis.getX() - position.getX()));
        transformationMatrix.translate(position.subtract(majorAxis).add(minorAxis));
        
        //Calculate first path of DNA shape
        double offset = programParameters.get(3).getValue();
        for (int i = 0; i < wavesNumber; i++) {
            shape = offset(shape, AlgebraicOperations.isOdd(i) ? offset : i == 0 ? 0 : -offset, DISTANCE_DEFAULT_VALUE);
            result.add(transformPoint(shape, transformationMatrix));
            shape = offset(shape, length, DISTANCE_DEFAULT_VALUE);
            
        }
        
        //Calculate the offset to move the next shape
        shape = offset(shape, AlgebraicOperations.isOdd(wavesNumber) ? -wavesNumber * length : -wavesNumber * length - offset, DISTANCE_DEFAULT_VALUE);
        shape = flip(shape, false, true);
        
        //Calculate second path of DNA shape
        for (int j = 0; j < wavesNumber; j++) {
            shape = offset(shape, !AlgebraicOperations.isOdd(j) ? offset : -offset, DISTANCE_DEFAULT_VALUE);
            result.add(transformPoint(shape, transformationMatrix));
            shape = offset(shape, length, DISTANCE_DEFAULT_VALUE);
        }
        
        return result;
    }
    
    /*
     *This method is overridden because we need change the color of the
     *shapes in different situations
     */
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes){
        ArrayList<ShapeBuilderConfiguration> result =
                (ArrayList<ShapeBuilderConfiguration>)super.createSplines(shapes);
        
        Color color1 = null;
        Color color2 = null;
        
        if(dNACurveParameters.size() > 0){
            color1 = dNACurveParameters.get(FIRST_ELEMENT).getColor();
            color2 = dNACurveParameters.get(SECOND_ELEMENT).getColor();
        }
        
        
        boolean secondColor;
        Color dnaColor;
        //Set the color of the different shapes
        for(int i = 0; i < result.size(); i++){
            //the next Id is used to differentiate each gradient to build the shaded attribute
            String shadedId = getElement().getId() + String.valueOf(i);
            
            SplineConfiguration spline = (SplineConfiguration) result.get(i);
            
            //This manages the second part of the DNA chains array, that is
            //the front faces of the DNA chains. If the DNA chains have two colors,
            //front faces could be painted in both.
            if(i >= result.size() / 2){
                spline.setFillColor(
                        convertColor(getEnvironment().getBackgroundColor()));
                
                //Use the second color when: the DNA has odd chains and
                //the actual element is on an odd position too, and when the DNA
                //has even chains and the actual element its on a even position too
                if(AlgebraicOperations.isOdd(result.size()/2)){
                    secondColor = AlgebraicOperations.isOdd(i);
                }else{
                    secondColor = !AlgebraicOperations.isOdd(i);
                }
                
                if(dNACurveParameters.size() > 0){
                    if(secondColor){
                        dnaColor = color2;
                    } else {
                        dnaColor = color1;
                    }
                    
                    spline.setColor(dnaColor);
                    
                    if(isShaded()){
                        spline.setStrokeWidth(getLineWidth());
                        spline.setShaded(isShaded());
                        spline.setGradient(
                                RadialGradient.getOvalGradient(
                                shadedId, Color.fadeRGB(dnaColor, fadePercent)));
                    }
                }
            } else {
                //This manages the first part of the DNA chains array, that is
                //the back faces of the DNA chains. If the DNA chains have two colors,
                //back faces could be painted in both colors and the fill color is faded
                
                //Use the second color when: the DNA has odd chains and
                //the next element is on an even position, and when the
                //DNA has even chains and the actual element its on an odd position
                if(AlgebraicOperations.isOdd(result.size() / 2)){
                    secondColor = !AlgebraicOperations.isOdd(i + 1);
                }else{
                    secondColor = AlgebraicOperations.isOdd(i);
                }
                
                if(dNACurveParameters.size() > 0){
                    if(secondColor){
                        dnaColor = color2;
                    } else {
                        dnaColor = color1;
                    }
                    
                    spline.setColor(dnaColor);
                    
                    if(isShaded()){
                        spline.setStrokeWidth(getLineWidth());
                        spline.setShaded(isShaded());
                        spline.setGradient(
                                RadialGradient.getOvalGradient(
                                shadedId, Color.fadeRGB(dnaColor, fadePercent)));
                    } else {
                        spline.setFillColor(Color.fadeRGB(dnaColor, fadePercent));
                    }
                    
                }
            }
        }
        
        return result;
    }
}
