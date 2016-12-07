package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.processors.cdxml.BioShapeProcessor.CDPControl;
import translator.processors.cdxml.BioShapeProcessor.CDPParam;
import translator.processors.cdxml.BioShapeProcessor.CurveParameters;
import translator.utils.AlgebraicOperations;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class HelixProteinProcessor extends BioShapeProcessor{
    
    private static final int DISTANCE_DEFAULT_VALUE = 0;
    private static final int CDPPARAM_DEFAULT_VALUE = 0;
    
    private static final int TRANSFORMATION__MATRIX_X_SCALE_DEFAULT_VALUE = 1;
    private static final int TRANSFORMATION__MATRIX_Y_SCALE_DEFAULT_VALUE = -1;
    //Special kind of helix protein. It has 7 pipes and the middle strand is longer than the others.
    public static final int BIO_SHAPE_HELIX_PROTEIN_7_HELIX_BOUNDLE = 7;
    
    private static final double MIN_PIPE_DIAMETER = 0;     
    private static final double DEFAULT_VALUE_MIN_CYLINDER_HEIGHT = 0;
    private static final double DEFAULT_VALUE_MIN_CYLINDER_DISTANCE = 0;
    private static final double DEFAULT_VALUE_MIN_CYLINDER_WIDTH = 0;    
    
    private double cylinderDiameter;
    private double cylinderHeight;
    private double cylinderDistance;    // distance in between cylinders
    private double pipeDiameter;
    private double pipeExtraOffset;     // extra offset for 3rd pipe
    
    private StringBuilder firstProgram;
    private StringBuilder secondProgram;
    private StringBuilder thirdProgram;
    
    private List<CurveParameters> helixCurveParameters;
    private int pipesNumber;
    
    public HelixProteinProcessor() {
    }
    
    protected void configure() {
        super.configure();
        cylinderDiameter = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_CYLINDER_WIDTH));        
        // Taken from C++ in CDBioDraw.h, class CDPParam, method  void	SetValue(double val)
        if (cylinderDiameter < 0){
            cylinderDiameter = DEFAULT_VALUE_MIN_CYLINDER_WIDTH;
        }
        // Taken from C++ in CDBioDraw.h, class CDPParam, method  void	SetValue(double val)
        if (Double.parseDouble(getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_CYLINDER_HEIGHT)) / 2 < 0){
            cylinderHeight = DEFAULT_VALUE_MIN_CYLINDER_HEIGHT;
        }else{
            // Half the parsed value
            cylinderHeight = Double.parseDouble(getElement().getAttribute(
                    ParseElementDefinition.BIO_SHAPE_CYLINDER_HEIGHT)) / 2;
        }    
        // Half the parsed value
        cylinderDistance = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_CYLINDER_DISTANCE));
        // Taken from C++ in CDBioDraw.h, class CDPParam, method  void	SetValue(double val)
        if (cylinderDistance < 0){
            cylinderDistance = DEFAULT_VALUE_MIN_CYLINDER_DISTANCE;        
        }

        pipeDiameter = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_PIPE_WIDTH));
        pipeExtraOffset = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_HELIX_PROTEIN_EXTRA));
        
    }
    
    protected void validate(){
        // Taken from C++ (File: CDBioDraw.CPP method CDGraphicHelixProtein())
        double maxPipeDiameter = 0.3 * environment.getBondLength();        
        
        // Taken from C++ (File: CDBioDraw.h Class: CDPParam Method: SetValue(...))
        if(pipeDiameter < MIN_PIPE_DIAMETER){
            pipeDiameter = MIN_PIPE_DIAMETER;
        }else if(pipeDiameter > maxPipeDiameter){
            pipeDiameter = maxPipeDiameter;
        }
    }
    
    protected void process() {
        //process the parameters for the helix element
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
        
        validateCurveParameters();
        setAttributesForDrawingElement();
        setAttributesFromCurveType();
        
        //Convert list of points into spline.
        Collection<ShapeBuilderConfiguration> configurations = createSplines(shapes);
        
        CompositeShapeConfiguration resultingConfiguration =
                new CompositeShapeConfiguration(bioShapeType, configurations);        
        resultingConfiguration.setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    /**
     * This method set the attributes for each pipe in the helix protein 
     * when the CurveParameters don't exist in the original file
     * This case occurs for files generated in versions below 11 where the 
     * attributes are generated from the helix protein characteristics
     */
    public void validateCurveParameters(){
        //if the file doesn't have CurveParameters the create them with the general attributes
        if(!(getElement().getElements(ParseElementDefinition.BIO_SHAPE_CURVE).size() > 0)){
            
            //All the helix elements use the same attributes                 
            String fillType = "";
            String lineType = "";
            int fadePercent = -1;
            
            if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_FADE_PERCENT)){
                fadePercent = Integer.parseInt(getElement().getAttribute(
                        ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT)) / FADE_PERCENT_SCALE_FACTOR;
            }            
            if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_NONE)){
                fillType = getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_NONE);
            }
            if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE)){
                lineType = getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE);
            }
            
            //generate the CurveParameters for each pipes and connector elements (pipesNumber * 2)
            for(int i=0; i < pipesNumber * 2; i++){
                
                CurveParameters parameter = new CurveParameters();
                
                parameter.setColor(getColor());
                helixCurveParameters.add(parameter);
                
                if(lineType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE_BOLD)){
                    parameter.setBold(true);
                } else if(lineType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE_DASHED)){
                    parameter.setDash(true);
                } else if(lineType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE_DASHED_BOLD)){
                    parameter.setBold(true);
                    parameter.setDash(true);
                }
                
                if(fillType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_NONE)){
                    parameter.setFilled(false);
                }else if(fillType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_SOLID)){
                    parameter.setFilled(true);
                } else if(fillType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_SHADED)){
                    parameter.setFilled(false);
                    parameter.setShaded(true);
                }
                
                if(fadePercent > 0){
                    parameter.setFilled(true);
                } else {
                    parameter.setFilled(false);
                }
                parameter.setFadePercent(fadePercent);                
            }
        }
    }
    
    private void processParameters(){
        
        programParameters.add(new CDPParam(cylinderDiameter));
        programParameters.add(new CDPParam(cylinderHeight));
        programParameters.add(new CDPParam(cylinderDistance));
        programParameters.add(new CDPParam(pipeDiameter));
        programParameters.add(new CDPParam(pipeExtraOffset));
        
        flipY = false;
        
        helixCurveParameters = new ArrayList();
        
        if(!getElement().getElements(ParseElementDefinition.BIO_SHAPE_CURVE).isEmpty()){
            for(ParsedElement curve : getElement().getElements(ParseElementDefinition.BIO_SHAPE_CURVE)){
                CurveParameters parameter = new CurveParameters();
                
                if(curve.getElements(ParseElementDefinition.COLOR).size() > 0){
                    parameter.setColor(convertColor(curve.getElements(ParseElementDefinition.COLOR).get(0)));
                }else{
                    parameter.setColor(foregroundColor);
                }
                helixCurveParameters.add(parameter);
                
                if(curve.hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE)){
                    String lineType = curve.getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE);
                    if(lineType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE_BOLD)){
                        parameter.setBold(true);
                    } else if(lineType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE_DASHED)){
                        parameter.setDash(true);
                    } else if(lineType.equals(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE_DASHED_BOLD)){
                        parameter.setBold(true);
                        parameter.setDash(true);
                    }
                }
                
                //This condition is for checking if the fill attribute
                //must be used from curve object or bioshape object
                
                //In this case it is being used from curve object
                if(curve.hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE)){
                    if(curve.getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE).equals(
                            ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_NONE)){
                        parameter.setFilled(false);
                    } else if(curve.getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE).equals(
                            ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_SOLID)){
                        parameter.setFilled(true);
                    } else if(curve.getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE).equals(
                            ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE_SHADED)){
                        parameter.setFilled(false);
                        parameter.setShaded(true);
                    }
                } else { //In this case it is being used from bioshape object
                    if(fillType.equals(ParseElementDefinition.BIO_SHAPE_FILL_TYPE_NONE)){
                        if(curve.hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT)){
                            parameter.setFaded(true);
                        } else {
                            parameter.setFilled(false);
                        }
                    }
                }
                
                if(curve.hasAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT)){
                    parameter.setFaded(true);
                    parameter.setFadePercent(Integer.parseInt(
                            curve.getAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT)) / FADE_PERCENT_SCALE_FACTOR);
                }
            }
        }
    }
    
    protected void createProgram() {
        firstProgram = new StringBuilder();
        firstProgram.append("250 D 400 L ");
        firstProgram.append("0 0 moveto ");
        firstProgram.append("0 L line ");
        firstProgram.append("0 D 4 div D D 4 div D 0 curve ");
        firstProgram.append("0 L neg line ");
        firstProgram.append("finishy ");
        firstProgram.append("0 L neg moveto ");
        firstProgram.append("0 D 4 div D D 4 div D 0 curve ");
        firstProgram.append("0 D 4 div neg  D neg D 4 div neg D neg 0 curve ");
        
        secondProgram = new StringBuilder();
        secondProgram.append("250 D 400 L 150 W 50 d 0 E ");
        secondProgram.append("0 0 moveto ");
        secondProgram.append("D W add d add 2 div D W add d add 2 div gxcurveto ");
        secondProgram.append("E 2 mul vline ");
        secondProgram.append("0 d d neg d d neg 0 curve ");
        secondProgram.append("E 2 mul neg vline ");
        secondProgram.append("D W add d sub 2 div neg D W add d sub 2 div neg gycurve ");
        secondProgram.append("d 10 mul neg hline d neg vline ");
        secondProgram.append("0 0 lineto ");
        
        thirdProgram = new StringBuilder();
        thirdProgram.append("250 D 400 L 150 W 50 d 0 E ");
        thirdProgram.append("1 D L 0 control 2 0 L neg 0 control 3 D W add L 0 control 4 D 2 div L 0 control ");
        thirdProgram.append("0 E neg moveto ");
        thirdProgram.append("D W add d add 2 div D W add d add 2 div gxcurveto ");
        thirdProgram.append("0 d d neg d d neg 0 curve ");
        thirdProgram.append("D W add d sub 2 div neg D W add d sub 2 div neg E sub gycurve ");
        thirdProgram.append("finishx ");
    }
    
    /**
     *For this shape we need three algorithms to create a helix protein shape
     */
    protected List<List<Point>> processAlgorithm(List<CDPParam> programParameters, List<CDPControl> programControlParameters){
        List<List<Point>> result = new ArrayList();
        boolean flipHorizontally = false;
        boolean flipVertically   = false;
        //1� algorithm
        List<List<Point>> shapes = processAlgorithm(programParameters, firstProgram);
        //2� algorithm
        List<Point> terminalPipe = processAlgorithm(programParameters, secondProgram).get(0);
        //3� algorithm
        List<Point> pipe3 = processAlgorithm(programParameters, thirdProgram).get(0);
        
        programParameters.set(4, new CDPParam(CDPPARAM_DEFAULT_VALUE));
        List<Point> pipe = (List<Point>) super.processAlgorithm(programParameters, new ArrayList()).get(0);
        
        programParameters.set(4, new CDPParam(pipeExtraOffset)); //restore
        
        //Calculate the length of the shape
        double fullLength = GeometricOperations.distance(majorAxis, position) * 2;
        double length = cylinderDiameter + cylinderDistance;
        //Calculate the number of pipes for the shape
        if (length != 0){
            pipesNumber = (int)(fullLength / length);
        }
        pipesNumber = Math.max(pipesNumber, 1);
        
        Matrix2D transformationMatrix = new Matrix2D();
        
        transformationMatrix.scaleXY(TRANSFORMATION__MATRIX_X_SCALE_DEFAULT_VALUE, TRANSFORMATION__MATRIX_Y_SCALE_DEFAULT_VALUE);
        transformationMatrix.rotate(Math.atan2(majorAxis.getY() - position.getY(), majorAxis.getX() - position.getX()));
        transformationMatrix.translate(position.subtract(majorAxis).add(minorAxis));
        
        flipVertically = true;
        pipe = offset(pipe, -cylinderDistance / 2, DISTANCE_DEFAULT_VALUE);
        pipe = flip(pipe, flipHorizontally, flipVertically);
        pipe = offset(pipe, DISTANCE_DEFAULT_VALUE,  cylinderHeight);
        
        pipe3 = offset(pipe3, -cylinderDistance / 2, DISTANCE_DEFAULT_VALUE);
        pipe3 = flip(pipe3, flipHorizontally, flipVertically);
        pipe3 = offset(pipe3, 3 * length, cylinderHeight + pipeExtraOffset);
        
        //Create the bellow connections for the pipes
        for (int i = 0; i < pipesNumber + 1; i++) {
            if (AlgebraicOperations.isOdd(i) && i < pipesNumber){
                //When the shape is a 7-cylinder helix protein, The length of the strand connecting the cylinders 3
                //and 4 is distinctly longer than the others.
                result.add(transformPoint(
                        pipesNumber == BIO_SHAPE_HELIX_PROTEIN_7_HELIX_BOUNDLE && i == 3 ?
                            pipe3 : pipe, transformationMatrix));
            }
            
            pipe = offset(pipe, length, DISTANCE_DEFAULT_VALUE);
        }
        
        pipe = offset(pipe, -(pipesNumber+1) * length, DISTANCE_DEFAULT_VALUE);
        
        pipe = offset(pipe, DISTANCE_DEFAULT_VALUE, -cylinderHeight);
        pipe = flip(pipe, flipHorizontally, flipVertically);
        
        terminalPipe = flip(terminalPipe, flipHorizontally, flipVertically);
        terminalPipe = offset(terminalPipe, (pipesNumber-1) * length - cylinderDistance / 2, cylinderHeight);
        
        if (AlgebraicOperations.isOdd(pipesNumber)){
            result.add(transformPoint(terminalPipe, transformationMatrix));
        }
        
        //Create the pipes
        for (int i = 0; i < pipesNumber; i++) {
            if(!shapes.isEmpty()){
                for (List<Point> shape : shapes) {
                    shape = offset(shape, i * length, DISTANCE_DEFAULT_VALUE);
                    result.add(transformPoint(shape, transformationMatrix));
                }
            }
        }
        
        pipe = offset(pipe, DISTANCE_DEFAULT_VALUE, -cylinderHeight - (cylinderDiameter + cylinderDistance) / 2 - pipeDiameter);
        flipHorizontally = true;
        terminalPipe = flip(terminalPipe, flipHorizontally, flipVertically);
        
        List<Point> extreme = calculateExtremeCoordinates(terminalPipe);
        Point beginPoint = extreme.get(FIRST_ELEMENT);
        Point endPoint = extreme.get(SECOND_ELEMENT);
        
        terminalPipe = offset(terminalPipe, -beginPoint.getX(), -beginPoint.getY());
        terminalPipe = offset(terminalPipe,
                (cylinderDiameter - pipeDiameter) / 2, -cylinderHeight - 2 * pipeExtraOffset - (cylinderDiameter + cylinderDistance) / 2 - pipeDiameter);
        result.add(transformPoint(terminalPipe, transformationMatrix));
        
        //Create the above connections for the pipes
        for (int i = 0; i < pipesNumber; i++) {
            if (!AlgebraicOperations.isOdd(i) && i > 0){
                result.add(transformPoint(pipe, transformationMatrix));
            }
            pipe = offset(pipe, length, DISTANCE_DEFAULT_VALUE);
        }
        return result;
    }
    
   /*
    * This method implements the three created programs for helix proteins
    */
    protected List<List<Point>> processAlgorithm(List<CDPParam> programParameters, StringBuilder program) {
        List<CDPControl> programControlParameters = new ArrayList() ;
        programScript = program;
        createProgramWords();
        List<List<Point>> shapes = super.processAlgorithm(programParameters, programControlParameters);
        return shapes;
    }
    
    /*
     *This method is overridden because we need change all the properties of the
     *different parts of the helix protein shape.
     */
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes){
        ArrayList<ShapeBuilderConfiguration> result =
                (ArrayList<ShapeBuilderConfiguration>)super.createSplines(shapes);
        
        // properties are stored like this:
        // below - above - tubes
        
        // but splines are stored like this:
        // below - tubes - above
        
        // index where tube properties start
        int tubePipeParamStart = (int) Math.round(helixCurveParameters.size() / 2.0);
        // index where above properties start
        int abovePipeParamStart = tubePipeParamStart / 2;
        
        // keep counters for each type of spline
        // to retrieve the right properties
        int aboveCounter = 0;
        int belowCounter = 0;
        int tubesCounter = 0;
        
        // number of connection pipes (above/below)
        int connectionPipesNumber = (int) Math.round(pipesNumber / 2.0);
        
        // iterate through the splines
        for (int i = 0; i < result.size(); i++) {
            if (i >= connectionPipesNumber && i < result.size() - connectionPipesNumber) {
                //the next Id is used to differentiate each gradient to build the shaded attribute
                String shadedId = getElement().getId() + String.valueOf(i);
                
                // There properties are from the tube pipes in the helix protein shape
                
                CurveParameters curvesParameters = helixCurveParameters.get(tubePipeParamStart + tubesCounter++);
                SplineConfiguration spline1 = (SplineConfiguration) result.get(i);
                SplineConfiguration spline2 = (SplineConfiguration) result.get(i + 1);
                i++;
                
                int curveFadePercent = getFadePercent(curvesParameters);
                
                spline1.setColor(curvesParameters.getColor());
                spline1.setFillColor(curvesParameters.getColor());
                spline1.setFill(curvesParameters.isFilled());
                spline1.setDashed(curvesParameters.isDash());
                spline1.setDashLength(hashSpacing);
                                
                if(curvesParameters.isShaded()){                    
                    spline1.setShaded(true);                    
                    spline1.setGradient(RadialGradient.getOvalGradient(shadedId, curvesParameters.getColor()));                    
                }
                
                if(curvesParameters.isFaded()){
                    spline1.setFill(true);
                    spline1.setFillColor(Color.fadeRGB(curvesParameters.getColor(), curveFadePercent));                    
                }                
                
                spline2.setColor(curvesParameters.getColor());
                spline2.setFillColor(curvesParameters.getColor());
                spline2.setFill(curvesParameters.isFilled());
                spline2.setDashed(curvesParameters.isDash());
                spline2.setDashLength(hashSpacing);                
                                
                if(curvesParameters.isShaded()){                    
                    spline2.setShaded(true);                    
                    spline2.setGradient(RadialGradient.getOvalGradient(shadedId, curvesParameters.getColor()));
                }
                
                if(curvesParameters.isFaded()){
                    spline2.setFill(true);
                    spline2.setFillColor(Color.fadeRGB(curvesParameters.getColor(), curveFadePercent));                    
                }                
                
                if(curvesParameters.isBold()){
                    spline2.setStrokeWidth(getBoldWidth());
                    spline1.setStrokeWidth(getBoldWidth());
                }else{
                    spline2.setStrokeWidth(getLineWidth());
                    spline1.setStrokeWidth(getLineWidth());
                }
                
            } else {
                //These properties are from the connection pipes in the helix protein shape
                
                CurveParameters curveParameters;
                if (i < connectionPipesNumber) {
                    // below connection pipes
                    curveParameters = helixCurveParameters.get(belowCounter++);
                } else {
                    curveParameters = helixCurveParameters.get(abovePipeParamStart + aboveCounter++);
                }
                
                String shadedId = getElement().getId() + String.valueOf(i);
                int curveFadePercent = getFadePercent(curveParameters);
                SplineConfiguration spline = (SplineConfiguration) result.get(i);
                
                spline.setColor(curveParameters.getColor());
                spline.setFillColor(curveParameters.getColor());
                spline.setFill(curveParameters.isFilled());                
                spline.setDashed(curveParameters.isDash());
                spline.setDashLength(hashSpacing);                
                
                if(curveParameters.isBold()){
                    spline.setStrokeWidth(getBoldWidth());
                }else{
                    spline.setStrokeWidth(getLineWidth());
                }
                
                if(curveParameters.isShaded()){                    
                    spline.setShaded(true);                    
                    spline.setGradient(RadialGradient.getOvalGradient(shadedId, curveParameters.getColor()));
                }
                
                if(curveParameters.isFaded()){
                    spline.setFill(true);
                    spline.setFillColor(Color.fadeRGB(curveParameters.getColor(), curveFadePercent));                    
                }
                
                spline.setLineJoin(LineJoin.Bevel);
            }
        }
        
        return result;
    }
    
    private int getFadePercent(CurveParameters parameters){
        return parameters.getFadePercent() > 0 ? parameters.getFadePercent() : fadePercent;
    }
}
