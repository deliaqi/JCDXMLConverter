package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.AlgebraicOperations;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public abstract class BioShapeProcessor extends DrawingAttributesProcessor {
    
    protected static final char SPACE_SEPARATOR = ' ';
    protected static final char NEW_LINE_SEPARATOR = '\n';
    protected static final char CARRIAGE_RETURN_SEPARATOR = '\r';
    
    protected static final String COMMAND_ADD = "add";
    protected static final String COMMAND_SUB = "sub";
    protected static final String COMMAND_MUL = "mul";
    protected static final String COMMAND_DIV = "div";
    protected static final String COMMAND_SIN = "sin";
    protected static final String COMMAND_COS = "cos";
    protected static final String COMMAND_TAN = "tan";
    protected static final String COMMAND_NEG = "neg";
    protected static final String COMMAND_REG = "reg";
    protected static final String COMMAND_SQRT = "sqrt";
    protected static final String COMMAND_DIST = "dist";
    protected static final String COMMAND_SINXY = "sinxy";
    protected static final String COMMAND_COSXY = "cosxy";
    protected static final String COMMAND_MOVE = "move";
    protected static final String COMMAND_MOVE_TO = "moveto";
    protected static final String COMMAND_CURVE = "curve";
    protected static final String COMMAND_CURVE_TO = "curveto";
    protected static final String COMMAND_GX_CURVE = "gxcurve";
    protected static final String COMMAND_GX_CURVE_TO = "gxcurveto";
    protected static final String COMMAND_GY_CURVE = "gycurve";
    protected static final String COMMAND_GY_CURVE_TO = "gycurveto";
    protected static final String COMMAND_LINE = "line";
    protected static final String COMMAND_LINE_TO = "lineto";
    protected static final String COMMAND_H_LINE = "hline";
    protected static final String COMMAND_V_LINE = "vline";
    protected static final String COMMAND_CAP = "cap";
    protected static final String COMMAND_CAP_TO = "capto";
    protected static final String COMMAND_FINISH_X = "finishx";
    protected static final String COMMAND_FINISH_Y = "finishy";
    protected static final String COMMAND_FLIP_X = "flipx";
    protected static final String COMMAND_FLIP_Y = "flipy";
    protected static final String COMMAND_OFFSET_POINTS = "offsetpoints";
    protected static final String COMMAND_CURVE_POINTS = "curvepoints";
    protected static final String COMMAND_CONTROL = "control";
    
    protected static final int MAXIMUM_FADE_PERCENT = 1000;
    protected static final int DEFAULT_FADE_PERCENT = 300;
    protected static final int DEFAULT_DASH_LENGTH = 4;
    
    //To transform the CDXML values in screen coordinates.
    protected static final int FADE_PERCENT_SCALE_FACTOR = 10;
    protected static final int TRANSFORMATION_MATRIX_SCALE_FACTOR = 1000;
    // Taken from C++ code
    protected static final double MINIMUM_SPLINE_SEGMENT_DISTANCE = 20;
    protected static final double MINIMUM_SPLINE_PRECISION = 0.125;
    
    protected static final int TRANSFORMATION_MATRIX_DEFAULT_VALUE = 0;
    //To use when a line intersects a circle.
    protected static final int NO_INTERSECTION_POINTS = 0;
    protected static final int ONE_INTERSECTION_POINT = 1;
    protected static final int TWO_INTERSECTION_POINTS = 2;
               
    //These are Bio shape attributes
    protected String bioShapeType;
    protected String fillType;
    protected String lineType;
    protected Point position;
    protected Point minorAxis;
    protected Point majorAxis;
    protected int fadePercent;
    //These are parameters associated with the program script processor.
    protected List<CDPParam> programParameters;
    protected double width;
    
    //Pre-processor properties
    protected StringBuilder programScript;
    protected List<String> programWords;
    protected List<String> shapeTypes;
    protected int currentWordIndex;
    
    //Limit coordinates
    protected double xMin = Double.MAX_VALUE;
    protected double yMin = Double.MAX_VALUE;
    protected double xMax = 0;
    protected double yMax = 0;
    
    //This matrix is used for making changes to the shapes, e.g. rotation changes.
    protected Matrix2D transformationMatrix;
    
    //To indicate if the shape must be fliped
    protected boolean flipY = true;
    
    public BioShapeProcessor() {
    }
    
    protected void configure() {
        super.configure();
        
        position = parseCoords(
                getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_POSITION), getElement());
        bioShapeType = getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_TYPE);
        
        minorAxis = parseCoords(
                getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_MINOR_AXIS_END_3D), getElement());
        majorAxis = parseCoords(
                getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_MAJOR_AXIS_END_3D), getElement());
        
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_FILL_TYPE)){
            fillType = getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_FILL_TYPE);
        }
        
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_LINE_TYPE)){
            lineType = getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_LINE_TYPE);
        }
        
        setAttributesForDrawingElement();
        width = getLineWidth();
        
        if(lineType != null && lineType.indexOf(ParseElementDefinition.BIO_SHAPE_LINE_TYPE_BOLD) != -1){
            width = getBoldWidth();
        }
        
        if (getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_FADE_PERCENT)){
            fadePercent = Integer.parseInt(
                getElement().getAttribute(ParseElementDefinition.BIO_SHAPE_FADE_PERCENT)) / FADE_PERCENT_SCALE_FACTOR;
        }else{
            fadePercent = DEFAULT_FADE_PERCENT / FADE_PERCENT_SCALE_FACTOR;
        }
        
        programWords = new ArrayList();
        programParameters = new ArrayList();
    }
    
    protected String getTypeString(){
        return lineType + "_" + fillType;
    }
    
    protected void process() {
        double xDistance = GeometricOperations.distance(position, majorAxis) * 2;
        double yDistance = GeometricOperations.distance(position, minorAxis) * 2;
        
        //Create the Bio shape script.
        programScript = new StringBuilder();
        createProgram();
        
        //Tokenize the string of script.
        programWords = new ArrayList();
        createProgramWords();
        
        List<CDPControl> programControlParameters = new ArrayList();
        
        //Process the script.
        List<List<Point>> shapes = processAlgorithm(programParameters, programControlParameters);
        
        // flag to assign first curve's bounds to the overall bounds
        boolean initializeMaxMin = true;
        
        //Find the maximun and minimun values for the x coordinates and y coordinates
        if (!shapes.isEmpty()){
            for(List<Point> shape : shapes){
                shape = new ArrayList(shape); // work with a copy
                
                // Taken from C++ code
                // Change y coordinate sign
                for (Point shapePoint : shape) {
                    shapePoint.setY(-shapePoint.getY());
                }
                
                // Add first and last points twice in the list
                shape.add(0, shape.get(0));
                shape.add(shape.get(shape.size() - 1));
                
                List<SegmentConfiguration> bezierCurves = buildSegmentBeziers(shape);
                for (SegmentConfiguration curve : bezierCurves) {
                    CubicCurveConfiguration cubicCurve = (CubicCurveConfiguration) curve;
                    
                    // Taken from C++ code
                    double curveDistance =
                            GeometricOperations.distance(cubicCurve.getBeginPoint(), cubicCurve.getControlPoint()) +
                            GeometricOperations.distance(cubicCurve.getControlPoint(), cubicCurve.getControlPoint2()) +
                            GeometricOperations.distance(cubicCurve.getControlPoint2(), cubicCurve.getEndPoint());
                    
                    double precision;
                    if (curveDistance < MINIMUM_SPLINE_SEGMENT_DISTANCE) {
                        precision = MINIMUM_SPLINE_PRECISION;
                    } else {
                        precision = MINIMUM_SPLINE_SEGMENT_DISTANCE / curveDistance;
                        if (precision > MINIMUM_SPLINE_PRECISION) {
                            precision = MINIMUM_SPLINE_PRECISION; // At least 8 segments
                        }
                    }
                    // Convert bezier curve to list of real points
                    List<Point> curvePoints = AlgebraicOperations.bezierFunction(precision, cubicCurve);
                    
                    Point firstPoint = curvePoints.get(0);
                    double curveXMax = firstPoint.getX();
                    double curveXMin = firstPoint.getX();
                    double curveYMax = firstPoint.getY();
                    double curveYMin = firstPoint.getY();
                    
                    for (int i = 1; i < curvePoints.size(); i++) {
                        Point point = curvePoints.get(i);
                        curveXMax = Math.max(curveXMax, point.getX());
                        curveXMin = Math.min(curveXMin, point.getX());
                        
                        curveYMax = Math.max(curveYMax, point.getY());
                        curveYMin = Math.min(curveYMin, point.getY());
                    }
                    
                    // if this is the first curve
                    if (initializeMaxMin) {
                        xMax = curveXMax;
                        xMin = curveXMin;
                        yMax = curveYMax;
                        yMin = curveYMin;
                        initializeMaxMin = false;
                    } else {
                        xMax = Math.max(xMax, curveXMax);
                        xMin = Math.min(xMin, curveXMin);
                        yMax = Math.max(yMax, curveYMax);
                        yMin = Math.min(yMin, curveYMin);
                    }
                }
            }
        }
        
        //scale the biodraw when the linewidth is modified        
        xMax -= (lineWidth/2 - environment.getLineWidth()/2);
        xMin += (lineWidth/2 - environment.getLineWidth()/2);
        yMax -= (lineWidth/2 - environment.getLineWidth()/2);
        yMin += (lineWidth/2 - environment.getLineWidth()/2);
        
        //Calculate the shape width and shape height
        double shapesWidth = xMax - xMin;
        double shapesHeight = yMax - yMin;
        
        transformationMatrix = new Matrix2D();
        
        //Transform the shapes using the matrix.
        if (useMinorAxis()) {
            transformationMatrix.translate(-xMin - shapesWidth / 2, -yMin - shapesHeight / 2);
            if (shapesWidth != 0 && shapesHeight != 0){
                transformationMatrix.scaleXY(xDistance/shapesWidth, yDistance/shapesHeight);
            }
            transformationMatrix.rotate(Math.atan2(majorAxis.getY() - position.getY(), majorAxis.getX() - position.getX()));
            transformationMatrix.translate(position);
        } else {
            transformationMatrix.scaleXY(
                    Math.sqrt(xDistance * xDistance + yDistance * yDistance) / TRANSFORMATION_MATRIX_SCALE_FACTOR,
                    TRANSFORMATION_MATRIX_DEFAULT_VALUE);
            transformationMatrix.rotate(Math.atan2(majorAxis.getY() - minorAxis.getY(),
                    majorAxis.getX() - minorAxis.getX()));
            transformationMatrix.translate(position.subtract(majorAxis.add(minorAxis)));
        }
        
        //Convert list of points into spline.
        Collection<ShapeBuilderConfiguration> configurations = createSplines(shapes);
        
        CompositeShapeConfiguration resultingConfiguration =
                new CompositeShapeConfiguration(bioShapeType, configurations);
        
        resultingConfiguration.setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    protected void cleanup() {
        position = null;
        bioShapeType = null;
        programWords = null;
        currentWordIndex = 0;
        fillType = null;
        lineType = null;
        minorAxis = null;
        majorAxis = null;
        fadePercent = 0;
        programParameters = null;
        xMin = Double.MAX_VALUE;
        yMin = Double.MAX_VALUE;
        xMax = 0;
        yMax = 0;
        transformationMatrix = null;
        
        super.cleanup();
    }
    
    protected void transformControls(List<CDPControl> programControlParameters, Matrix2D matrix){
        if(!programControlParameters.isEmpty()){
            for (CDPControl control : programControlParameters){
                Point transformedPoint = matrix.transform(control.getPoint());
                control.setPoint(transformedPoint);
            }
        }
    }
    
    /**
     *This method is called only for the generic splines
     */
    protected Collection<ShapeBuilderConfiguration> createSplines(List<List<Point>> shapes){
        return createGenericSpline(shapes);
    }
    
    /**
     *Transform the resulting points obtained by the script processing in splines
     */
    private Collection<ShapeBuilderConfiguration> createGenericSpline(List<List<Point>> shapes){
        Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
        Point lastPoint = new Point(Point.POINT_DEFAULT_VALUE, Point.POINT_DEFAULT_VALUE);
        
        for(int i = 0; i < shapes.size(); i++){
            List<Point> shape = shapes.get(i);
            
            List<Point> translatedShape = new ArrayList();
            
            if(!shape.isEmpty()){
                for(Point point : shape){
                    translatedShape.add(transformationMatrix.transform(point));
                }
            }
            List<SegmentConfiguration> curves = new ArrayList();
            
            List<Point> currentPoints = new ArrayList();
            
            for(int j = 1; j < translatedShape.size(); j += 3){
                CubicCurveConfiguration curve = new CubicCurveConfiguration(
                        translatedShape.get(j - 1),     //Begin point
                        translatedShape.get(j + 2),     //End point
                        translatedShape.get(j),         //Control point 1
                        translatedShape.get(j + 1));    //Control point 2
                
                curves.add(curve);
            }
            
            SplineConfiguration shapeConfiguration = new SplineConfiguration(curves);
            
            //Set the drawing attributes
            shapeConfiguration.setStrokeWidth(width);
            shapeConfiguration.setFill(true);
            shapeConfiguration.setLineJoin(LineJoin.Bevel);
            shapeConfiguration.setClosed(true);
            
            
            if(fillType.equals(ParseElementDefinition.BIO_SHAPE_FILL_TYPE_SOLID)){
                shapeConfiguration.setFillColor(getColor()); //Solid fill
            } else {
                if(fillType.equals(ParseElementDefinition.BIO_SHAPE_FILL_TYPE_NONE) &&
                        fadePercent == MAXIMUM_FADE_PERCENT/FADE_PERCENT_SCALE_FACTOR){
                    shapeConfiguration.setFill(false); //None fill
                } else {
                    shapeConfiguration.setFillColor(Color.fadeRGB(getColor(), fadePercent)); //Faded
                }
            }
            
            if(lineType != null && lineType.indexOf(ParseElementDefinition.BIO_SHAPE_LINE_TYPE_DASHED) != -1){
                shapeConfiguration.setDashed(true);
                shapeConfiguration.setDashLength(getHashSpacing());
            }
            
            shapeConfiguration.setColor(getColor());
            
            configurations.add(shapeConfiguration);
        }
        
        return configurations;
    }
    
    /**
     *Create the generics splines cheking the shaded attribute
     */
    protected Collection<ShapeBuilderConfiguration> createShadedSplines(List<List<Point>> shapes) {
        Collection<ShapeBuilderConfiguration> result = createGenericSpline(shapes);
        
            if(isShaded()){
                for(ShapeBuilderConfiguration shape : result){
                    ((SplineConfiguration)shape).setShaded(isShaded());
                    ((SplineConfiguration)shape).setStrokeWidth(getLineWidth());
                    ((SplineConfiguration)shape).setGradient(
                            RadialGradient.getOvalGradient(
                            getElement().getId(), Color.fadeRGB(getColor(), fadePercent)));
                }
            }
        
        return result;
    }
    
    protected abstract void createProgram();
    
    protected boolean useMinorAxis() {
        boolean useMinorAxis = true;
        return useMinorAxis;
    }
    
    /**
     *Calculate the extreme points of the shape
     */
    protected List<Point> calculateExtremeCoordinates(List<Point> points){
        List<Point> result = new ArrayList();
        
        double xMaxCoordinate = 0;
        double xMinCoordinate = Double.MAX_VALUE;
        
        double yMaxCoordinate = 0;
        double yMinCoordinate = Double.MAX_VALUE;
        
        if(!points.isEmpty()) {
            for(Point point : points){
                xMaxCoordinate = Math.max(xMaxCoordinate, point.getX());
                xMinCoordinate = Math.min(xMinCoordinate, point.getX());
                
                yMaxCoordinate = Math.max(yMaxCoordinate, point.getY());
                yMinCoordinate = Math.min(yMinCoordinate, point.getY());
            }
        }
        
        result.add(new Point(xMinCoordinate, yMinCoordinate));
        result.add(new Point(xMaxCoordinate, yMaxCoordinate));
        
        return result;
    }
    
    /**
     * This method processes the script created by each spline. This script describes
     * the bio shape perimeter
     * It has determined commands and values to describe each  shape.
     * Taken from ChemDraw 12 C++
     */
    protected List<List<Point>> processAlgorithm(List<CDPParam> programParameters, List<CDPControl> programControlParameters){
        List<List<Point>> result = new ArrayList();
        List<Double> stack = new ArrayList();
        List<String> names = new ArrayList();
        List<Double> defaults = new ArrayList();
        
        List<Point> currentShapePoints = new ArrayList();
        
        currentWordIndex = 0;
        
        int pix;
        int stackSize;
        
        int programParametersSize = programParameters.size();
        
        String currentWord;
        while(true){
            currentWord = getNextWord();
            if (currentWord == null){
                break;
            }
            
            char firstChar = currentWord.charAt(FIRST_ELEMENT);
            stackSize = stack.size();
            
            if (Character.isDigit(firstChar) || firstChar == '-' || firstChar == '+'){
                stack.add(Double.parseDouble(currentWord));
            } else if ((pix = findName(names, currentWord)) >= 0) {
                stack.add(pix >= programParametersSize ? defaults.get(pix) : getParameter(pix));
            } else if (currentWord.equals(COMMAND_ADD)) {
                if(stackSize >= 2){
                    stack = processCommandRemovingLastElement(stack, (stack.get(stackSize-2) + stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_SUB)) {
                if(stackSize >= 2){
                    stack = processCommandRemovingLastElement(stack, (stack.get(stackSize-2) - stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_MUL)) {
                if(stackSize >= 2){
                    stack = processCommandRemovingLastElement(stack, (stack.get(stackSize-2) * stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_DIV)) {
                if(stackSize >= 2 && stack.get(stackSize-1) != 0){
                    stack = processCommandRemovingLastElement(stack, (stack.get(stackSize-2) / stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_SIN)) {
                if(stackSize > 0  && stack.get(stackSize-1) != 0){
                    stack = processCommand(stack, Math.sin(Math.PI / 180 * stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_COS)) {
                if(stackSize > 0  && stack.get(stackSize-1) != 0){
                    stack = processCommand(stack, Math.cos(Math.PI / 180 * stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_TAN)) {
                if(stackSize > 0  && stack.get(stackSize-1) != 0){
                    stack = processCommand(stack, Math.tan(Math.PI / 180 * stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_NEG)) {
                if(stackSize > 0){
                    stack = processCommand(stack, -stack.get(stackSize-1));
                }
            } else if (currentWord.equals(COMMAND_SQRT)) {
                if(stackSize > 0){
                    stack = processCommand(stack,  Math.sqrt(stack.get(stackSize-1)));
                }
            } else if (currentWord.equals(COMMAND_DIST)) {
                if(stackSize >= 2){
                    double x = stack.get(stackSize-2);
                    double y = stack.get(stackSize-1);
                    stack = processCommandRemovingLastElement(stack, Math.sqrt(x*x+y*y));
                }
            } else if (currentWord.equals(COMMAND_SINXY)){
                if(stackSize >= 2){
                    double x = stack.get(stackSize-2);
                    double y = stack.get(stackSize-1);
                    if(Math.sqrt(x*x+y*y) != 0){
                        stack = processCommandRemovingLastElement(stack, y / Math.sqrt(x*x+y*y));
                    }
                }
            } else if (currentWord.equals(COMMAND_COSXY)) {
                if(stackSize >= 2){
                    double x = stack.get(stackSize-2);
                    double y = stack.get(stackSize-1);
                    if(Math.sqrt(x*x+y*y) != 0){
                        stack = processCommandRemovingLastElement(stack, x / Math.sqrt(x*x+y*y));
                    }
                }
            } else if (currentWord.equals(COMMAND_MOVE) || currentWord.equals(COMMAND_MOVE_TO)) { // Finish the current shape begin a new one.
                currentShapePoints = new ArrayList();
                if(stackSize >= 2){
                    currentShapePoints.add(new Point(stack.get(stackSize-2), stack.get(stackSize-1)));
                    result.add(currentShapePoints);
                    stack = stack.subList(0, stackSize - 2);
                }
            } else if (currentWord.equals(COMMAND_CURVE) || currentWord.equals(COMMAND_CURVE_TO)) {
                currentShapePoints = processCommandCurveOrCurveTo(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_GX_CURVE) ||
                    currentWord.equals(COMMAND_GX_CURVE_TO) ||
                    currentWord.equals(COMMAND_GY_CURVE) ||
                    currentWord.equals(COMMAND_GY_CURVE_TO)) { // Generated curves.
                currentShapePoints = processCommandGXGYCurveOrCurveTo(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_LINE) || currentWord.equals(COMMAND_LINE_TO)) {
                currentShapePoints = processCommandLineOrLineTo(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_H_LINE)) {
                currentShapePoints = processCommandHLineOrVLine(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_V_LINE)) {
                currentShapePoints = processCommandHLineOrVLine(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_CAP) || currentWord.equals(COMMAND_CAP_TO)) {
                currentShapePoints = processCommandCapOrCapTo(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_FINISH_X) || currentWord.equals(COMMAND_FINISH_Y)) {
                currentShapePoints = processCommandFinishXOrFinishY(stack, currentShapePoints, currentWord);
            } else if (currentWord.equals(COMMAND_FLIP_X) || currentWord.equals(COMMAND_FLIP_Y)) {
                result = processCommandFlipXOrFlipY(result, currentWord);
            } else if (currentWord.equals(COMMAND_OFFSET_POINTS)) {
                processCommandOffsetPoints(stack, result, programControlParameters);
            } else if (currentWord.equals(COMMAND_CURVE_POINTS)) {
                result = processCommandCurvePoints(stack, result);
                stack = new ArrayList();
            } else if (currentWord.equals(COMMAND_CONTROL)) {
                programControlParameters = processCommandControl(stack, programControlParameters);
            } else {                                    // The first time, if a symbol is found, do not use.
                names.add(currentWord);
                defaults.add(stack.get(stackSize-1));   // Formal parameters must be listed at the beginning.
                //This removes unnecessary elements just for mantaining the stack as little as possible.
                stack = stack.subList(0, stackSize-1);
            }
        }
        
        return result;
    }
    
    /**
     * This method processes: COMMAND_SIN, COMMAND_COS, COMMAND_TAN, COMMAND_NEG and COMMAND_SQRT
     */
    private List<Double> processCommand(List<Double> stack, Double valueToSet){
        int stackSize = stack.size();
        stack.set(stackSize-1, valueToSet);
        return stack;
    }
    
    /**
     * This method processes: COMMAND_ADD, COMMAND_SUB, COMMAND_MUL, COMMAND_DIV,
     * COMMAND_DIST, COMMAND_SINXY and COMMAND_COSXY
     */
    private List<Double> processCommandRemovingLastElement(List<Double> stack, Double valueToSet) {
        int stackSize = stack.size();
        stack.set(stackSize-2, valueToSet);
        stack.remove(stackSize-1);
        return stack;
    }
    
    /**
     * This method processes: COMMAND_CURVE and COMMAND_CURVE_TO
     */
    private List<Point> processCommandCurveOrCurveTo(List<Double> stack, List<Point> currentShapePoints, String currentWord){
        int currentShapePointsSize = currentShapePoints.size();
        int stackSize = stack.size();
        double x = 0;
        double y = 0;
        if (!currentWord.equals(COMMAND_CURVE_TO)) {
            if (currentShapePointsSize > 0) {
                x = currentShapePoints.get(currentShapePointsSize-1).getX();
                y = currentShapePoints.get(currentShapePointsSize-1).getY();
            }
        }
        for(int i = stackSize - 6; i < stackSize; i += 2){
            currentShapePoints.add(new Point(stack.get(i) + x, stack.get(i + 1) + y));
        }
        if(stackSize >= 6) {
            stack = stack.subList(0, stackSize - 6);
        }
        return currentShapePoints;
    }
    
    /**
     * This method processes:COMMAND_GX_CURVE, COMMAND_GX_CURVE_TO, COMMAND_GY_CURVE and COMMAND_GY_CURVE_TO
     */
    private List<Point> processCommandGXGYCurveOrCurveTo(List<Double> stack, List<Point> currentShapePoints, String currentWord){
        int currentShapePointsSize = currentShapePoints.size();
        int stackSize = stack.size();
        double x = 0;
        double y = 0;
        if (currentShapePointsSize > 0) {
            if (!currentWord.equals(COMMAND_GX_CURVE_TO) && !currentWord.equals(COMMAND_GY_CURVE_TO)) {
                x = currentShapePoints.get(currentShapePointsSize-1).getX();
                y = currentShapePoints.get(currentShapePointsSize-1).getY();
            }
            
            Point point1 = new Point(currentShapePoints.get(currentShapePointsSize-1));
            Point point2 = new Point(stack.get(stackSize-2) + x, stack.get(stackSize-1) + y);
            if (currentWord.equals(COMMAND_GX_CURVE) || currentWord.equals(COMMAND_GX_CURVE_TO)) {
                currentShapePoints.add(new Point((point1.getX() + point2.getX()) / 2, point1.getY()));
                currentShapePoints.add(new Point(point2.getX(), (point1.getY() + point2.getY()) / 2));
            } else {
                currentShapePoints.add(new Point(point1.getX(), (point1.getY() + point2.getY()) / 2));
                currentShapePoints.add(new Point((point1.getX() + point2.getX()) / 2, point2.getY()));
            }
            currentShapePoints.add(point2);
            if(stackSize >= 2){
                stack = stack.subList(0, stackSize - 2);
            }
        }
        return currentShapePoints;
    }
    
    /**
     * This method processes: COMMAND_LINE and COMMAND_LINE_TO
     */
    private List<Point> processCommandLineOrLineTo(List<Double> stack, List<Point> currentShapePoints, String currentWord){
        int currentShapePointsSize = currentShapePoints.size();
        int stackSize = stack.size();
        double x = 0;
        double y = 0;
        
        if (currentShapePointsSize > 0) {
            if (!currentWord.equals(COMMAND_LINE_TO)) {
                x = currentShapePoints.get(currentShapePointsSize-1).getX();
                y = currentShapePoints.get(currentShapePointsSize-1).getY();
            }
            
            currentShapePoints.add(
                    new Point(currentShapePoints.get(currentShapePointsSize-1).getX(),
                    currentShapePoints.get(currentShapePointsSize-1).getY()));                          // Duplicate last point.
            if (stackSize >= 2) {
                currentShapePoints.add(new Point(stack.get(stackSize-2) + x, stack.get(stackSize-1) + y));  // Insert element twice.
                currentShapePoints.add(new Point(stack.get(stackSize-2) + x, stack.get(stackSize-1) + y));
                stack = stack.subList(0, stackSize - 2);
            }
        }
        return currentShapePoints;
    }
    
    /**
     * This method processes: COMMAND_H_LINE and COMMAND_V_LINE
     */
    private List<Point> processCommandHLineOrVLine(List<Double> stack, List<Point> currentShapePoints, String currentWord){
        int currentShapePointsSize = currentShapePoints.size();
        int stackSize = stack.size();
        double x = 0;
        double y = 0;
        
        if (currentShapePointsSize > 0) {
            x = currentShapePoints.get(currentShapePointsSize-1).getX();
            y = currentShapePoints.get(currentShapePointsSize-1).getY();
            
            currentShapePoints.add(new Point(x, y));                                    // Duplicate last point.
            if (stackSize > 0) {
                if (currentWord.equals(COMMAND_H_LINE)) {
                    currentShapePoints.add(new Point(stack.get(stackSize-1) + x, y));   // Insert element twice.
                    currentShapePoints.add(new Point(stack.get(stackSize-1) + x, y));
                } else if (currentWord.equals(COMMAND_V_LINE)) {
                    currentShapePoints.add(new Point(x, stack.get(stackSize-1) + y));   // Insert element twice.
                    currentShapePoints.add(new Point(x, stack.get(stackSize-1) + y));
                }
                stack.remove(stackSize-1);
            }
        }
        return currentShapePoints;
    }
    
    /**
     *This method processes: COMMAND_CAP and COMMAND_CAP_TO
     */
    private List<Point> processCommandCapOrCapTo(List<Double> stack, List<Point> currentShapePoints, String currentWord){
        int currentShapePointsSize = currentShapePoints.size();
        int stackSize = stack.size();
        double x = 0;
        double y = 0;
        
        if (currentShapePointsSize > 0) {
            if (!currentWord.equals(COMMAND_CAP_TO)) {
                x = currentShapePoints.get(currentShapePointsSize-1).getX();
                y = currentShapePoints.get(currentShapePointsSize-1).getY();
            }
            if (stackSize >= 2) {
                currentShapePoints.addAll(calcCapCurves(
                        new Point(stack.get(stackSize-2) + x, stack.get(stackSize-1) + y),
                        currentShapePoints));
                stack = stack.subList(0, stackSize - 2);
            }
        }
        return currentShapePoints;
    }
    
    /**
     * This method processes: COMMAND_FINISH_X and COMMAND_FINISH_Y
     */
    private List<Point> processCommandFinishXOrFinishY(List<Double> stack, List<Point> currentShapePoints, String currentWord){
        int currentShapePointsSize = currentShapePoints.size();
        double x = 0;
        double y = 0;
        
        boolean flipx = (currentWord.equals(COMMAND_FINISH_X));
        
        for (int i = currentShapePointsSize - 2; i >= 0; i--) {
            x = currentShapePoints.get(i).getX();
            y = currentShapePoints.get(i).getY();
            currentShapePoints.add(new Point(flipx ? -x : x, flipx ? y : -y));
        }
        return currentShapePoints;
    }
    
    /**
     * This method processes: COMMAND_FLIP_X and COMMAND_FLIP_Y
     */
    private List<List<Point>> processCommandFlipXOrFlipY(List<List<Point>> result, String currentWord){
        int resultCount = result.size();
        boolean flipx = currentWord.equals(COMMAND_FLIP_X);
        
        for (int j = 0; j < resultCount; j++) {
            List<Point> points = new ArrayList();
            List<Point> shapePoints = result.get(j);
            
            if(!shapePoints.isEmpty()){
                for(Point shapePoint : shapePoints){
                    if(flipx){
                        points.add(new Point(-shapePoint.getX(), shapePoint.getY()));
                    } else {
                        points.add(new Point(shapePoint.getX(), -shapePoint.getY()));
                    }
                }
            }
            result.add(points);
        }
        return result;
    }
    
    /**
     * This method processes: COMMAND_OFFSET_POINTS
     */
    private void processCommandOffsetPoints(List<Double> stack, List<List<Point>> result, List<CDPControl> programControlParameters){
        int resultCount = result.size();
        int stackSize = stack.size();
        double distantX = 0;
        double distantY = 0;
        if (stackSize >= 2) {
            distantX = stack.get(stackSize-2);
            distantY = stack.get(stackSize-1);
            for (int j = 0; j < resultCount; j++) {
                List<Point> points = result.get(j);
                int pointsSize = points.size();
                
                for (int i = 0; i < pointsSize; i++) {
                    points.get(i).setX(points.get(i).getX() + distantX);
                    points.get(i).setY(points.get(i).getY() + distantY);
                }
            }
            stack = stack.subList(0, stackSize - 2);
        }
        int programControlParametersSize = programControlParameters.size();
        for (int i = 0; i < programControlParametersSize; i++){
            programControlParameters.get(i).getPoint().setX(programControlParameters.get(i).getPoint().getX() + distantX);
            programControlParameters.get(i).getPoint().setY(programControlParameters.get(i).getPoint().getY() + distantY);
        }
    }
    
    /**
     * This method processes: COMMAND_CURVE_POINTS
     */
    private List<List<Point>> processCommandCurvePoints(List<Double> stack, List<List<Point>> result){
        int stackSize = stack.size();
        List<Point> shapePoints = new ArrayList();
        for (int i = 0; i < stackSize; i += 2){
            int j = (i + 2) % stackSize;
            shapePoints.add(new Point(stack.get(j), stack.get(j+1)));
        }
        if (stackSize >= 4) {
            shapePoints.add(new Point(stack.get(2), stack.get(3))); // Repeat starting point.
            result.add(shapePoints);
        }
        return result;
    }
    
    /**
     * This method processes: COMMAND_CONTROL
     */
    private List<CDPControl> processCommandControl(List<Double> stack, List<CDPControl> programControlParameters){
        int stackSize = stack.size();
        CDPControl commandControlParameters = new CDPControl();
        if (stackSize >= 4) {
            commandControlParameters.setIxParam(stack.get(stackSize-4).intValue());
            Point point = new Point(stack.get(stackSize-3), stack.get(stackSize-2));
            commandControlParameters.setPoint(point);
            
            commandControlParameters.setDragAngle(stack.get(stackSize-1).intValue());
            programControlParameters.add(commandControlParameters);
            
            stack = stack.subList(0, stackSize-3);
        }
        return programControlParameters;
    }
    
    /*
     *Taken from ChemDraw 12 C++
     */
    private int findName(List<String> names, String nameToFind){
        int result = -1;
        for (int i = 0; i < names.size(); i++){
            if (names.get(i).equals(nameToFind)){
                result = i;
                return result;
            }
        }
        return result;
    }
    
    /**
     *Split the script string
     *Taken from ChemDraw 12 C++
     */
    protected void createProgramWords(){
        programWords = new ArrayList();
        String programScript = this.programScript.toString().trim();
        
        int beginIndex = 0;
        
        for(int i = 0; i < programScript.length(); i++){
            if(programScript.charAt(i) == SPACE_SEPARATOR
                    || programScript.charAt(i) == NEW_LINE_SEPARATOR
                    || programScript.charAt(i) == CARRIAGE_RETURN_SEPARATOR){
                
                String newWord = programScript.substring(beginIndex, i).trim();
                
                if(newWord != null){
                    programWords.add(newWord);
                }
                
                beginIndex = ++i;
            }
        }
        
        programWords.add(programScript.substring(beginIndex));
    }
    
    /*
     *Taken from ChemDraw 12 C++
     */
    private String getNextWord(){
        String result = null;
        
        if(currentWordIndex < programWords.size()){
            result = programWords.get(currentWordIndex);
            currentWordIndex++;
        }
        
        return result;
    }
    
    /*
     * Taken from ChemDraw 12 C++
     * This method calculates the curves determined by the Cap and CapTo methods.
     */
    private List<Point> calcCapCurves(Point newPoint, List<Point> points){
        List<Point> result = new ArrayList();
        
        if (points == null){
            return null;
        }
        
        int pointsSize = points.size();
        Point previousPoint = points.get(
                pointsSize < 4 ? pointsSize-1 :
                    points.get(pointsSize-1) == points.get(pointsSize-2) && points.get(pointsSize-3) == points.get(pointsSize-4) ?
                        pointsSize-4 : pointsSize-2);
        Point rootPoint = points.get(pointsSize-1);
        
        double distance = GeometricOperations.distance(rootPoint, newPoint);
        
        CircleIntersectionResult circleIntersection =
                circleIntersectsLine2D(rootPoint, distance, rootPoint, previousPoint);
        
        Point point1 = circleIntersection.getIntersectionPoint1();
        Point point2 = circleIntersection.getIntersectionPoint2();
        
        int numberIntersections = circleIntersection.getNumberIntersections();
        if (numberIntersections < 1){
            return result;
        } else if (numberIntersections > 1
                && GeometricOperations.distance(previousPoint, point2) > GeometricOperations.distance(previousPoint, point1)){
            point1 = circleIntersection.getIntersectionPoint2();
            point2 = circleIntersection.getIntersectionPoint1();
        }
        
        result.add(point1);
        result.add(point1.add(newPoint.subtract(rootPoint)));
        result.add(newPoint);
        
        return result;
    }
    
    /*
     *Taken from ChemDraw 12 C++
     * Find whether a line intersects a circle, and return the point(s) of intersection.
     * The return value is the number of points of intersection: 0, 1, or 2
     * If there are no points of intersection,
     *  then both intersectionPt1 and intersectionPt2 are unpopulated and should be ignored
     * If there is one point of intersection (the line is tangent to the circle),
     *  then intersectionPt1 holds that point while intersectionPt2 is unpopulated and should be ignored
     * If there are two points of intersection,
     *  then intersectionPt1 and intersectionPt2 hold those two points (unordered)
     */
    private CircleIntersectionResult circleIntersectsLine2D(Point circleCenter, double circleRadius,
            Point linePoint1, Point linePoint2){
        CircleIntersectionResult result = new CircleIntersectionResult();
        
        boolean allowBeforePoint1 = true;
        boolean allowAfterPoint2 = true;
        Point linePoint = nearestPointOnLine2D(circleCenter, linePoint1, linePoint2, allowBeforePoint1, allowAfterPoint2);
        double distanceToLine = GeometricOperations.distance(linePoint, circleCenter);
        
        if (distanceToLine > circleRadius){
            result.setNumberIntersections(NO_INTERSECTION_POINTS);
        } else if (distanceToLine == circleRadius){
            result.setIntersectionPoint1(linePoint);
            
            result.setNumberIntersections(ONE_INTERSECTION_POINT);
        } else {
            double angle = 0;
            if (circleRadius != 0) {
                angle = Math.asin(distanceToLine / circleRadius);
            }
            
            Point alongLine = linePoint2.subtract(linePoint1).byScalar(1 / 2);
            alongLine = alongLine.scaleTo(Math.cos(angle) * circleRadius);
            
            result.setIntersectionPoint1(linePoint.add(alongLine));
            result.setIntersectionPoint2(linePoint.subtract(alongLine));
            
            result.setNumberIntersections(TWO_INTERSECTION_POINTS);
        }
        
        return result;
    }
    
    /*
     *Taken from ChemDraw 12 C++
     *Find the point on a line (or line segment or ray) closest to the given point
     */
    private Point nearestPointOnLine2D(Point point,
            Point point1, Point point2, boolean allowBeforePoint1, boolean allowAfterPoint2){
        if (point1.equals(point2)){
            if (allowBeforePoint1 || allowAfterPoint2){
                return point;
            }
            
            return point1;
        }
        
        Point delta = point2.subtract(point1);
        delta = delta.byScalar(1 / delta.dotProduct(delta));
        Point pma = point.subtract(point1);
        
        double x = pma.dotProduct(delta);
        
        Point returnValue;
        if (!allowBeforePoint1 && x < 0){
            returnValue = point1;
        } else if (!allowAfterPoint2 && x > 1){
            returnValue = point2;
        } else {
            returnValue = point1.add((point2.subtract(point1).byScalar(x)));
        }
        
        return returnValue;
    }
    
    protected List<Point> offset(List<Point> points, double xDistance, double yDistance){
        List<Point> newPoints = new ArrayList();
        
        if(!points.isEmpty()){
            for(Point point : points){
                newPoints.add(new Point(point.getX() + xDistance, point.getY() + yDistance));
            }
        }
        
        return newPoints;
    }
    
    protected List<Point> flip(List<Point> points, boolean horizontal, boolean vertical){
        List<Point> newPoints = new ArrayList();
        
        List<Point> extreme = calculateExtremeCoordinates(points);
        Point extremeBegin = extreme.get(FIRST_ELEMENT);
        Point extremeEnd = extreme.get(SECOND_ELEMENT);
        
        if(!points.isEmpty()){
            for(Point point : points){
                double newXCoordinate = point.getX();
                double newYCoordinate = point.getY();
                
                if(vertical){
                    newYCoordinate = extremeBegin.getY() + (extremeEnd.getY() - newYCoordinate);
                }
                
                if(horizontal){
                    newXCoordinate = extremeBegin.getX() + (extremeEnd.getX() - newXCoordinate);
                }
                
                newPoints.add(new Point(newXCoordinate, newYCoordinate));
            }
        }
        return newPoints;
    }
    
    protected List<Point> transformPoint(List<Point> points, Matrix2D matrix){
        List<Point> newPoints = new ArrayList();
        
        if(!points.isEmpty()){
            for(Point point : points){
                newPoints.add(matrix.transform(
                        new Point(point.getX(), -point.getY())));
            }
        }
        
        return newPoints;
    }
    
    protected double getParameter(int index){
        return programParameters.get(index).getValue();
    }
    
    /*
     *Taken from ChemDraw 12 C++
     */
    private class CircleIntersectionResult {
        
        private int numberIntersections;
        private Point intersectionPoint1;
        private Point intersectionPoint2;
        
        public int getNumberIntersections() {
            return numberIntersections;
        }
        
        public void setNumberIntersections(int numberIntersections) {
            this.numberIntersections = numberIntersections;
        }
        
        public Point getIntersectionPoint1() {
            return intersectionPoint1;
        }
        
        public void setIntersectionPoint1(Point intersectionPoint1) {
            this.intersectionPoint1 = intersectionPoint1;
        }
        
        public Point getIntersectionPoint2() {
            return intersectionPoint2;
        }
        
        public void setIntersectionPoint2(Point intersectionPoint2) {
            this.intersectionPoint2 = intersectionPoint2;
        }
        
    }
    
    /*
     *To encapsulate the properties for the different parts of Bio shapes
     */
    protected class CurveParameters {
        
        private int fadePercent;
        private boolean dash;
        private boolean bold;
        private boolean filled;
        private boolean faded;
        private boolean shaded;
        private Color color;
        
        public CurveParameters(){
            
        }
        
        public int getFadePercent() {
            return fadePercent;
        }
        
        public void setFadePercent(int fadePercent) {
            this.fadePercent = fadePercent;
        }
        
        public boolean isDash() {
            return dash;
        }
        
        public void setDash(boolean dash) {
            this.dash = dash;
        }
        
        public boolean isBold() {
            return bold;
        }
        
        public void setBold(boolean bold) {
            this.bold = bold;
        }
        
        public boolean isFilled() {
            return filled;
        }
        
        public void setFilled(boolean filled) {
            this.filled = filled;
        }
        
        public boolean isFaded() {
            return faded;
        }
        
        public void setFaded(boolean faded) {
            this.faded = faded;
        }
        
        public Color getColor() {
            return color;
        }
        
        public void setColor(Color color) {
            this.color = color;
        }

        public boolean isShaded() {
            return shaded;
        }

        public void setShaded(boolean shaded) {
            this.shaded = shaded;
        }
        
    }
    
    /*
     *Taken from ChemDraw 12 C++
     */
    protected class CDPControl {
        
        private int ixParam;    // parameter to be changed by the control
        private int dragAngle;  // direction of dragging 0-0, 1-45, 2-90 etc.
        private Point point;    // display point
        
        public CDPControl() {
        }
        
        public CDPControl(Point point) {
            this.point = point;
        }
        
        public int getIxParam() {
            return ixParam;
        }
        
        public void setIxParam(int ixParam) {
            this.ixParam = ixParam;
        }
        
        public int getDragAngle() {
            return dragAngle;
        }
        
        public void setDragAngle(int dragAngle) {
            this.dragAngle = dragAngle;
        }
        
        public Point getPoint() {
            return point;
        }
        
        public void setPoint(Point point) {
            this.point = point;
        }
        
    }
    
    /*
     *Taken from ChemDraw 12 C++
     *These are parameters associated with the program script processor.
     */
    protected class CDPParam {
        private double value;
        private double vmin;
        private double vmax;
        
        public CDPParam() {
            value = 0;
            vmin = 0;
            vmax = 1.0e100;
        }
        
        public CDPParam(double value) {
            vmin = 0;
            vmax = 1.e100;
            setValue(value);
        }
        
        public double getValue() {
            return value;
        }
        
        public void setValue(double value){
            this.value = value < vmin ? vmin : value > vmax ? vmax : value;
        }
        
        public void setMax(double max) {
            vmax = max;
        }
        
        public void setMin(double min) {
            vmin = min;
        }
        
        public double getMax() {
            return vmax;
        }
        
        public double getMin() {
            return vmin;
        }
    }
    
}
