package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.processors.cdxml.BioShapeProcessor.CDPControl;
import translator.processors.cdxml.BioShapeProcessor.CDPParam;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Line;

public class OrnamentedShapeProcessor extends BioShapeProcessor {
    
    // Minimun angle between start and end point of a Membrane Arc
    // to avoid empty screen when the angle is towards to zero.
    private static final double K_MEMBRANE_ARC_MIN_ANGLE = 0.02;
    
    private static final double DEFAULT_INTERSECTION_LINE_LENGTH = 1;
    private static final double FIRST_PARAMETER_MIN_VALUE = 20;
    private static final double SCALE_FACTOR = 1000;
    
    private static final int BIO_SHAPE_MEMBRANE_LINE_CONTROL_POINT_X_VALUE = 500;
    private static final int BIO_SHAPE_MEMBRANE_LINE_POINT_X_FIRST_VALUE = 0;
    private static final int BIO_SHAPE_MEMBRANE_LINE_POINT_X_SECOND_VALUE = 1000;
    private static final int BIO_SHAPE_MEMBRANE_LINE_POINT_Y_DEFAULT_VALUE = 0;
    
    private static final int BIO_SHAPE_MEMBRANE_ARC_SEMI_MAJOR_AXIS = 1200;
    private static final int BIO_SHAPE_MEMBRANE_ARC_SEMI_MINOR_AXIS = 900;

    private static final int BIO_SHAPE_MEMBRANE_MICELLE_SEMI_MAJOR_AXIS = 1200;
    private static final int BIO_SHAPE_MEMBRANE_MICELLE_SEMI_MINOR_AXIS = 1200;

    private static final int BIO_SHAPE_MEMBRANE_ELLIPSE_SEMI_MAJOR_AXIS = 1200;
    private static final int BIO_SHAPE_MEMBRANE_ELLIPSE_SEMI_MINOR_AXIS = 900;
    
    private static final double ORNAMENT_CENTER_DIRECTION_FACTOR = 0.001;
    
    private static final double CLOSED_DISTANCE = 0.05;
    
    private static final double MIN_INTERNAL_RADIUS = 0.1;
    
    private static final double BORDER_WIDTH_DECIMAL_PRESITION_FACTOR = 1e10;
    
    //taken from c++ code
    private static final double DEFAULT_MIN_VALUE_ELEMENT_SIZE = 1;
    
    private double elementSize;
    private double startAngle;
    private double endAngle;
    
    public OrnamentedShapeProcessor() {
    }

    protected void createProgram() {
    }

    protected void configure() {
        super.configure();
        
        elementSize = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.BIO_SHAPE_MEMBRANE_ELEMENT_SIZE));
        
        //taken from c++ code
        if(elementSize < DEFAULT_MIN_VALUE_ELEMENT_SIZE){
            elementSize = DEFAULT_MIN_VALUE_ELEMENT_SIZE;
        }
        
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_MEMBRANE_START_ANGLE)){
            startAngle = Double.parseDouble(getElement().getAttribute(
                    ParseElementDefinition.BIO_SHAPE_MEMBRANE_START_ANGLE));
        }
        if(getElement().hasAttribute(ParseElementDefinition.BIO_SHAPE_MEMBRANE_END_ANGLE)){
            endAngle = Double.parseDouble(getElement().getAttribute(
                    ParseElementDefinition.BIO_SHAPE_MEMBRANE_END_ANGLE));
        }
        
        double bondLength = getEnvironment().getBondLength();
        CDPParam firstParameter = new CDPParam();
        firstParameter.setMin(FIRST_PARAMETER_MIN_VALUE);
        firstParameter.setValue(elementSize);
        programParameters.add(firstParameter);

	if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_LINE)){
	} else if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ARC)){
            programParameters = configureMembraneArc(programParameters, BIO_SHAPE_MEMBRANE_ARC_SEMI_MAJOR_AXIS, BIO_SHAPE_MEMBRANE_ARC_SEMI_MINOR_AXIS);
	} else if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_MICELLE)){
            programParameters = configureMembraneMicelleAndEllipse(programParameters, BIO_SHAPE_MEMBRANE_MICELLE_SEMI_MAJOR_AXIS, BIO_SHAPE_MEMBRANE_MICELLE_SEMI_MINOR_AXIS);
	} else if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ELLIPSE)){
            programParameters = configureMembraneMicelleAndEllipse(programParameters, BIO_SHAPE_MEMBRANE_ELLIPSE_SEMI_MAJOR_AXIS, BIO_SHAPE_MEMBRANE_ELLIPSE_SEMI_MINOR_AXIS);
	}
    }
    
    /*
     * Configures attributes for Bio Shape Membrane Arc.
     */
    protected List<CDPParam> configureMembraneArc(List<CDPParam> programParameters, int semiMajorAxis, int semiMinorAxis) {
        programParameters.add(new CDPParam(semiMajorAxis));  // semiMajorAxis, a param of ellipse
        programParameters.add(new CDPParam(semiMinorAxis));  // semiMinorAxis, b param of ellipse
        //Assuming that start angle < end angle <= (start angle + 2PI)
        //Calculate start angle param
        programParameters = calculateMembraneArcAngleParam(programParameters, - Math.toRadians(endAngle));
        //Calculate end angle param
        programParameters = calculateMembraneArcAngleParam(programParameters, - Math.toRadians(startAngle));
        return programParameters;
    }
    
    /*
     * Calculate start or end angle param for Bio Shape Membrane Arc
     */
    protected List<CDPParam> calculateMembraneArcAngleParam(List<CDPParam> programParameters, double paramValue) {
        CDPParam angleParam = new CDPParam();
        angleParam.setMin(-Math.PI * 2);
        angleParam.setMax(Math.PI * 2);
        angleParam.setValue(paramValue);
        programParameters.add(angleParam);
        return programParameters;
    }
   
    /*
     * Configures attributes for Bio Shape Membrane Micelle and Bio Shape Membrane Ellipse.
     */
    protected List<CDPParam> configureMembraneMicelleAndEllipse(List<CDPParam> programParameters, int semiMajorAxis, int semiMinorAxis)
        {
            programParameters.add(new CDPParam(semiMajorAxis));      // semiMajorAxis, a param of ellipse
            programParameters.add(new CDPParam(semiMinorAxis));      // semiMinorAxis, b param of ellipse
            programParameters.add(new CDPParam(0));                 // start angle
            programParameters.add(new CDPParam(Math.PI*2));         // end angle
            return programParameters;
        }
    
    protected void process() {
        List<CDPControl> programControlParameters = new ArrayList();
        
        //Process the script
        List<List<Point>> shapes = processAlgorithm(programParameters, programControlParameters);
        
        //Convert list of points to spline
        Collection<ShapeBuilderConfiguration> configurations = drawOrnaments(shapes.get(FIRST_ELEMENT), elementSize);
        
        CompositeShapeConfiguration resultingConfiguration = 
                new CompositeShapeConfiguration(bioShapeType, configurations);
        
        resultingConfiguration.setZOrder(zOrder);
        
        // This comprobation is for the case where fillType is "none" and 
        // the faded attribute isn't specified.
        if(!faded){
            resultingConfiguration.setFill(filled);
        }
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    /*
     * This method processes the script program for all Bio Shape Membranes
     */
    protected List<List<Point>> processAlgorithm (List<CDPParam> programParameters, List<CDPControl> programControlParameters){
        List<List<Point>> result = new ArrayList();
        
        Matrix2D transformationMatrix = new Matrix2D();
	List<Point> shape = new ArrayList();

	double distance     = 0;
	double semiMajorAxis = 0;
        double semiMinorAxis = 0;
	double startAngle   = 0;
        double endAngle     = 0;

	if (programParameters.size() >= 5) {
            distance     = programParameters.get(0).getValue();
            semiMajorAxis = programParameters.get(1).getValue();  // ellipse semi-major axis
            semiMinorAxis = programParameters.get(2).getValue();  // ellipse semi-minor axis
            startAngle   = programParameters.get(3).getValue();  // angle values does not have to be limited
            endAngle     = programParameters.get(4).getValue();
	}

        if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_LINE)){
            shape = processBioShapeMembraneLineAlgorithm(programControlParameters, transformationMatrix, shape);
        } else if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ARC)){
            if (semiMajorAxis != 0 && semiMinorAxis != 0){
                shape = processBioShapeMembraneArcAlgorithm(programControlParameters, transformationMatrix, shape,
                        GeometricOperations.distance(position, majorAxis) / semiMajorAxis, GeometricOperations.distance(position, minorAxis) / semiMinorAxis,
                        semiMajorAxis, semiMinorAxis, startAngle, endAngle, distance);
            }
        } else if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_MICELLE)){
            shape = processBioShapeMembraneMicelleOrEllipseAlgorithm(programControlParameters, transformationMatrix, shape,
                    GeometricOperations.distance(position, majorAxis) / SCALE_FACTOR, GeometricOperations.distance(position, majorAxis) / SCALE_FACTOR,
                    semiMajorAxis, semiMinorAxis, startAngle, endAngle);
        } else if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ELLIPSE)){
            if (semiMajorAxis != 0 && semiMinorAxis != 0){
                shape = processBioShapeMembraneMicelleOrEllipseAlgorithm(programControlParameters, transformationMatrix, shape,
                       GeometricOperations.distance(position, majorAxis) / semiMajorAxis, GeometricOperations.distance(position, minorAxis) / semiMinorAxis,
                       semiMajorAxis, semiMinorAxis, startAngle, endAngle);
            }
	}
	result.add(transformPoint(shape, transformationMatrix));
        return result;
    }
    
    /*
     * This method processes the algorithm only for Bio Shape Membrane Line.
     */
    private List<Point> processBioShapeMembraneLineAlgorithm(List<CDPControl> programControlParameters, Matrix2D transformationMatrix, List<Point> shape){
        transformationMatrix.rotate(Math.atan2(majorAxis.getY() - position.getY(), majorAxis.getX() - position.getX()));
        transformationMatrix.scale(GeometricOperations.distance(position, majorAxis) * 2 / SCALE_FACTOR);
        transformationMatrix.translate(position.subtract(majorAxis).add(minorAxis));

        programControlParameters.add(new CDPControl(new Point(BIO_SHAPE_MEMBRANE_LINE_CONTROL_POINT_X_VALUE, BIO_SHAPE_MEMBRANE_LINE_POINT_Y_DEFAULT_VALUE)));

        shape.add(new Point(BIO_SHAPE_MEMBRANE_LINE_POINT_X_FIRST_VALUE, BIO_SHAPE_MEMBRANE_LINE_POINT_Y_DEFAULT_VALUE));
        shape.add(new Point(BIO_SHAPE_MEMBRANE_LINE_POINT_X_FIRST_VALUE, BIO_SHAPE_MEMBRANE_LINE_POINT_Y_DEFAULT_VALUE));
        shape.add(new Point(BIO_SHAPE_MEMBRANE_LINE_POINT_X_SECOND_VALUE, BIO_SHAPE_MEMBRANE_LINE_POINT_Y_DEFAULT_VALUE));
        shape.add(new Point(BIO_SHAPE_MEMBRANE_LINE_POINT_X_SECOND_VALUE, BIO_SHAPE_MEMBRANE_LINE_POINT_Y_DEFAULT_VALUE));
        return shape;
    }
    
    /*
     * This method processes the algorithm only for Bio Shape Membrane Arc.
     */
    private List<Point> processBioShapeMembraneArcAlgorithm(List<CDPControl> programControlParameters, Matrix2D transformationMatrix,
        List<Point> shape, double xScale, double yScale, double semiMajorAxis, double semiMinorAxis,
        double startAngle, double endAngle, double distance) {
        shape = calculateBioShapeMembraneTransformationMatrix(transformationMatrix, shape,
                xScale, yScale, semiMajorAxis, semiMinorAxis, startAngle, endAngle);

        // controls[0]: ornament size control, 
        // controls[1]: membrane starting point control, 
        // controls[2]: membrane ending point.
        // The angle (startAngle) used to calculate the controls[0] and controls[1] 
        // needs to be the same value in order the controls are drawn together. 
        // The angle used to calculated the three control points, need to be between 
        // startAngle and endtAngle values, so that the control points are drawn over the 
        // membrane arc. 
        // The dist value that is used to calculate the controls[0] and controls[2] 
        // is introduced to be sure that the control points are drawn over the membrane arc, 
        // and not inside or outside it.
        double centerX = 0;
        double centerY = 0;
        double teta = 0;
        
        programControlParameters.add(new CDPControl(ellipsePoint(centerX, centerY, 
                semiMajorAxis - distance * 0.95, semiMinorAxis - distance * 0.95, teta, startAngle)));
        programControlParameters.add(new CDPControl(ellipsePoint(centerX, centerY, 
                semiMajorAxis, semiMinorAxis, teta, startAngle)));
        programControlParameters.add(new CDPControl(ellipsePoint(centerX, centerY, 
                semiMajorAxis + distance / 2, semiMinorAxis + distance / 2, teta, endAngle)));
        return shape;
    }
    
    /*
     * This method processes the script only for Bio Shape Membrane Micelle and Ellipse.
     */
    private List<Point> processBioShapeMembraneMicelleOrEllipseAlgorithm(List<CDPControl> programControlParameters, Matrix2D transformationMatrix,
        List<Point> shape, double xScale, double yScale, double semiMajorAxis, double semiMinorAxis, 
            double startAngle, double endAngle) {
        shape = calculateBioShapeMembraneTransformationMatrix(transformationMatrix, shape,
                xScale, yScale, semiMajorAxis, semiMinorAxis, startAngle, endAngle);
        programControlParameters.add(new CDPControl(ellipsePoint(0, 0, semiMajorAxis, semiMinorAxis, 0, endAngle / 2)));

        return shape;
    }
    
    /*
     * This method calculates the transformation matrix for Bio Shape Membranes Arc, Micelle and Ellipse.
     */
    private List<Point> calculateBioShapeMembraneTransformationMatrix(Matrix2D transformationMatrix, 
            List<Point> shape, double xScale, double yScale, double semiMajorAxis, double semiMinorAxis, 
            double startAngle, double endAngle) {
            transformationMatrix.scaleXY(xScale, yScale);
            transformationMatrix.rotate(Math.atan2(majorAxis.getY() - position.getY(), majorAxis.getX() - position.getX()));
            transformationMatrix.translate(position);
            shape = calcRotatedEllipticalArc(0, 0, semiMajorAxis, semiMinorAxis, 0, startAngle, endAngle);
            return shape;
    }
    
    private List<Point> calcRotatedEllipticalArc(double centerX, double centerY, double semiMajorAxis, double semiMinorAxis, 
            double teta, double startAngle, double endAngle){
        // teta = ellipse orientation
        // centerX and centerY are center point values for the ellipse which the arc belongs to.
        List<Point> shape = new ArrayList();
        
	if (startAngle < 0){
            startAngle += 2 * Math.PI;
        }
	while (endAngle < startAngle){
            endAngle += 2 * Math.PI;
        }

	if (Math.abs(endAngle - startAngle) < K_MEMBRANE_ARC_MIN_ANGLE){
            endAngle += K_MEMBRANE_ARC_MIN_ANGLE;
        }

	if (Math.abs(endAngle - startAngle) < Math.PI / 4) {
            shape = calcSmallRotatedEllipticalArc(centerX, centerY, semiMajorAxis, semiMinorAxis, teta, startAngle, endAngle, shape);
	} else {
            // The following line is different from the one in C++
            // That for drawing the ellipse direction correctly.
            // This increases the number of generated  points to form the path so, the algorithm which calculates the direction, works correctly.
            int pointsNumber = (int)(4 * Math.abs(endAngle - startAngle) / Math.PI) * 5;
            double delta = 0;
            if(pointsNumber != 0){
                delta = (endAngle - startAngle) / pointsNumber;
            }
            for (int i = 0; i < pointsNumber; i++) {
                List<Point> describedShape = new ArrayList();
                describedShape = calcSmallRotatedEllipticalArc(centerX, centerY, semiMajorAxis, semiMinorAxis, teta, 
                        startAngle + i * delta, startAngle + (i + 1) * delta, describedShape);
                if (i == 0){
                    shape = describedShape;
                } else {
                    for (int j = 1; j < 4; j++){
                        shape.add(describedShape.get(j));
                    }
                }
            }
        }
        return shape;
    }
    
    private List<Point> calcSmallRotatedEllipticalArc(double centerX, double centerY, double elipseAParam, double elipseBParam, 
            double teta, double startAngle, double endAngle, List<Point> shape){
	// teta = ellipse orientation
        // centerX and centerY are center point values for the ellipse which the arc belongs to.
        List<Point> result = new ArrayList();
        result.addAll(shape);

        result.add(null);
        result.add(null);
        result.add(null);
        result.add(null);
        
	double tanx = Math.tan((endAngle - startAngle) / 2);
	double alfa = Math.sin(endAngle - startAngle) * (Math.sqrt(4 + 3 * tanx * tanx) - 1) / 3;

	result.set(0, ellipsePoint(centerX, centerY, elipseAParam, elipseBParam, teta, startAngle));
	result.set(3, ellipsePoint(centerX, centerY, elipseAParam, elipseBParam, teta, endAngle));
	result.set(1, result.get(0).add(ellipseDerivative(centerX, centerY, elipseAParam, elipseBParam, teta, startAngle).byScalar(alfa)));
	result.set(2, result.get(3).subtract(ellipseDerivative(centerX, centerY, elipseAParam, elipseBParam, teta, endAngle).byScalar(alfa)));
        
        return result;
    }
    
    // The following implementation URL was taken from C++: 
    // implemented from http://www.spaceroots.org/documents/ellipse/elliptical-arc.pdf
    private Point ellipsePoint(double centerX, double centerY, double semiMajorAxis, double semiMinorAxis, double teta, double angle){
        Point result = new Point(
        centerX + semiMajorAxis * Math.cos(teta) * Math.cos(angle) - semiMinorAxis * Math.sin(teta) * Math.sin(angle),
        centerY + semiMajorAxis * Math.sin(teta) * Math.cos(angle) + semiMinorAxis * Math.cos(teta) * Math.sin(angle));
        return result;
    }
    
    public Point ellipseDerivative(double centerX, double centerY, double semiMajorAxis, double semiMinorAxis, double teta, double angle){
        Point result = new Point(
        - semiMajorAxis * Math.cos(teta) * Math.sin(angle) - semiMinorAxis * Math.sin(teta) * Math.cos(angle),
        - semiMajorAxis * Math.sin(teta) * Math.sin(angle) + semiMinorAxis * Math.cos(teta) * Math.cos(angle));
        return result;
    }
    
    private Collection<ShapeBuilderConfiguration> drawOrnaments(List<Point> points, double distance) {
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        List<EquiDistancePoint> equiDistancePoints = equiDistancePoints(points, distance);

        if(!equiDistancePoints.isEmpty()){
            for (EquiDistancePoint equidistancePoint : equiDistancePoints){
                    List<Point> centersPoint = calcOrnamentCenters(equidistancePoint, distance);
                    Point point1 = centersPoint.get(0);
                    Point point2 = centersPoint.get(1);

                    result.addAll(drawOrnament(point1, point2));
            }
        }
        return result;
    }
    
    private Collection<ShapeBuilderConfiguration> drawOrnament(Point point1, Point point2) {
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        
        double length = GeometricOperations.distance(point1, point2);
        double radius = length / 4;

        double offset = radius / 4;
        boolean draw1 = true;
        boolean draw2 = true;

        if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_MICELLE)){
            if(GeometricOperations.distance(point1, position) < GeometricOperations.distance(point2, position)){
                draw1 = false;
            } else {
                draw2 = false;
            }
        }
        
        double angle = GeometricOperations.angle(point1, point2);
        double separationAngle = angle + Math.PI / 2;        
        
        Line intersectionLine1 = new Line(
                new Point(GeometricOperations.offset(point1, angle, radius)),
                separationAngle, DEFAULT_INTERSECTION_LINE_LENGTH);
        
        Line intersectionLine2 = new Line(
                new Point(GeometricOperations.offset(point2, angle, -radius)),
                separationAngle, DEFAULT_INTERSECTION_LINE_LENGTH);
        
        Point intersectionPoint1 = GeometricOperations.intersection(
                    intersectionLine1.getBegin(), intersectionLine1.getEnd(), point1, point2);
            
        Point intersectionPoint2 = GeometricOperations.intersection(
                intersectionLine2.getBegin(), intersectionLine2.getEnd(), point1, point2);
                
        SplineConfiguration wavyLine = null;
        
        if(bioShapeType.equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_MICELLE)){
            wavyLine = new SplineConfiguration(
                    getEnvironment().getWavySegments(
                    draw1 ? intersectionPoint1 : intersectionPoint2, draw1 ? point2 : point1, getBoldWidth()));            

            if(dashed){
                //when the micellle is dashed increment the radius in a values of linewidth/2
                radius += width/2;
            }
            wavyLine.setStrokeWidth(width);
            wavyLine.setDashed(dashed);
            wavyLine.setDashLength(hashSpacing);
            wavyLine.setColor(color);
            
        } else {
            Point point1End = GeometricOperations.offset(
                    point1, angle, (length / 2) - (offset / 2));
            Point point2End = GeometricOperations.offset(
                    point2, angle + Math.PI, (length / 2) - (offset / 2));
            
            result = createSegment(result, separationAngle, offset, intersectionPoint1, point1End);
            result = createSegment(result, separationAngle, offset, point2End, intersectionPoint2);
            result = createSegment(result, separationAngle, -offset, intersectionPoint1, point1End);
            result = createSegment(result, separationAngle, -offset, point2End, intersectionPoint2);
        }
        
        if (draw1){
            result.add(drawOval(point1, radius));                        
        }
        if (draw2){
            result.add(drawOval(point2, radius));
        }
        //the tail must be drawed after the oval of the micelle
        if(wavyLine != null){
            result.add(wavyLine);
        }        
        
        return result;
    }
    
    /*
     * This method creates segment for ornament drawing.
     */
    private Collection<ShapeBuilderConfiguration> createSegment(Collection<ShapeBuilderConfiguration> ornamentConfiguration, 
        double separationAngle, double offset, Point beginPoint1, Point beginPoint2) {
        SegmentConfiguration segment = new SegmentConfiguration(
                GeometricOperations.offset(beginPoint1, separationAngle, offset),
                GeometricOperations.offset(beginPoint2, separationAngle, offset));
        segment.setStrokeWidth(getLineWidth());
        segment.setColor(getColor());
        ornamentConfiguration.add(segment);
        return ornamentConfiguration;
    }
    
    private CircleConfiguration drawOval(Point point, double radius){
        
        double borderWidth = getWidth();                    
        
        double borderRadius = radius - borderWidth / 2;                
    
        // The borderRadius can't be less than the half of the 
        // border with.
        if(borderRadius < MIN_INTERNAL_RADIUS){      
            // If the internal radius is less than zero the border width is needed
            // to be recalculated.
            borderWidth =  Math.abs(radius - borderWidth / 2);            
            borderRadius = radius - borderWidth / 2;  
        }
        
        CircleConfiguration oval = new CircleConfiguration(point, borderRadius);
        oval.setFill(true);        
         
        // This is for evite the scientific representation rounding the at the tenth decimals.
        borderWidth = Math.round(borderWidth * BORDER_WIDTH_DECIMAL_PRESITION_FACTOR) / BORDER_WIDTH_DECIMAL_PRESITION_FACTOR;                        
        
        oval.setStrokeWidth(borderWidth);
        
        if(fillType.equals(ParseElementDefinition.BIO_SHAPE_FILL_TYPE_SOLID)){
            oval.setFillColor(getColor()); //Solid fill
        } else if(fillType.equals(ParseElementDefinition.BIO_SHAPE_FILL_TYPE_SHADED)){
            oval.setFill(false);
            oval.setShaded(isShaded());
            oval.setStrokeWidth(getLineWidth());
            oval.setGradient(
                    RadialGradient.getOvalGradient(
                    getElement().getId(), Color.fadeRGB(getColor(), fadePercent)));
        } else {
            if(fillType.equals(ParseElementDefinition.BIO_SHAPE_FILL_TYPE_NONE) &&
                    fadePercent == MAXIMUM_FADE_PERCENT){
                oval.setFill(false); //None fill
            } else {
                oval.setFillColor(Color.fadeRGB(getColor(), fadePercent)); //Faded
            }
        }

        if(lineType != null){
            if(lineType.equals(ParseElementDefinition.BIO_SHAPE_LINE_TYPE_BOLD)){
                oval.setStrokeWidth(getBoldWidth());
            } else if(lineType.equals(ParseElementDefinition.BIO_SHAPE_LINE_TYPE_DASHED)){
                oval.setDashed(true);
                oval.setDashLength(hashSpacing);
            } else if(lineType.equals(ParseElementDefinition.BIO_SHAPE_LINE_TYPE_DASHED_BOLD)){
                oval.setStrokeWidth(getBoldWidth());
                oval.setDashed(true);
                oval.setDashLength(hashSpacing);
            }
        }
        oval.setColor(color);

        return oval;
    }
    
    private Line frameOrnaments(List<Point> points, double distance){
        Line radius = null;
        List<EquiDistancePoint> path = equiDistancePoints(points, distance);

        if(!path.isEmpty()){
            for (EquiDistancePoint equiDistancePoint : path){
                List<Point> centers = calcOrnamentCenters(equiDistancePoint, distance);
                Point point1 = centers.get(0);
                Point point2 = centers.get(1);

                if (equiDistancePoint.getPoint().equals(path.get(0).getPoint())){
                    radius = frameOrnamentCircle(point1, point2);
                } else {
                    radius = maxLine(radius, frameOrnamentCircle(point1, point2));
                }
            }
        }
        return radius;
    }
    
    private Line frameOrnamentCircle(Point point1, Point point2){
        double length = GeometricOperations.distance(point1, point2);
        double majorRadius = length / 4;
        Line radius1 = new Line(
                new Point(point1.getX() - majorRadius, point1.getY() - majorRadius),
                new Point(point1.getX() + majorRadius, point1.getY() + majorRadius));
        Line radius2 = new Line(
                new Point(point2.getX() - majorRadius, point2.getY() - majorRadius),
                new Point(point2.getX() + majorRadius, point2.getY() + majorRadius));

        return maxLine(radius1, radius2);
    }
    
    private Line maxLine(Line radius1, Line radius2){
        if (radius1 == null){
            return radius2;
        } else if (radius2 == null){
            return radius1;
        } else {
            return new Line(
                    new Point(Math.min(radius1.getBegin().getX(), radius2.getBegin().getX()),
                    Math.min(radius1.getBegin().getY(), radius2.getBegin().getY())),
                    new Point(Math.max(radius1.getEnd().getX(), radius2.getEnd().getX()),
                    Math.max(radius1.getEnd().getY(), radius2.getEnd().getY())));
        }
    }
    
    private Point offsetPoints(Point point1, Point point2, double offset){
        double distance = GeometricOperations.distance(point1, point2);
        double sina = 0;
        double cosa = 0;
        if (distance != 0){
            sina = (point2.getY() - point1.getY()) / distance;
            cosa = (point2.getX() - point1.getX()) / distance;
        }
        Point result = new Point(- offset * sina, offset * cosa);
        return result;
    }
    
    private List<EquiDistancePoint> equiDistancePoints(List<Point> pointsVector, double wholeDistance){
        List<EquiDistancePoint> result = new ArrayList();
        
        if(!pointsVector.isEmpty()){
            int i, pointsNumber = pointsVector.size();
            double length = 0;                                          // length will store the full length of the list of points
            
            for (i=1; i<pointsNumber; i++){
                length += GeometricOperations.distance(pointsVector.get(i - 1), pointsVector.get(i));
            }

            int segmentsNumber  = 0;
            if (wholeDistance != 0){
                segmentsNumber = (int)(length / wholeDistance);         // number of ornaments can be placed on the polygon
            }
            if (segmentsNumber != 0){
                wholeDistance = length / segmentsNumber;                    // correct the distance to fill the whole length
            }
            
            boolean closed = Math.abs(pointsVector.get(0).getX() - pointsVector.get(pointsNumber - 1).getX()) < CLOSED_DISTANCE
                    && Math.abs(pointsVector.get(0).getY() - pointsVector.get(pointsNumber - 1).getY()) < CLOSED_DISTANCE;
            
            Point point = pointsVector.get(0);
            double currentDistance = closed ? wholeDistance : 0;        // running distance. Be sure, that there will be a point at the beginning unless closed
            i = 1;
            while (i < pointsNumber) {
                Point point1 = new Point();
                Point point2 = new Point();
                point1 = new Point(point);
                point2 = new Point(pointsVector.get(i));
                double distanceBetweenPoint1AndPoint2 = GeometricOperations.distance(point1, point2);
                if (distanceBetweenPoint1AndPoint2 > currentDistance || distanceBetweenPoint1AndPoint2 > currentDistance/2 && i == pointsNumber-1) {	// allow ornament at the end, unless the shape is closed
                    Point newPoint = new Point();
                    newPoint.setX(point1.getX() + currentDistance / distanceBetweenPoint1AndPoint2 * (point2.getX() - point1.getX()));
                    newPoint.setY(point1.getY() + currentDistance / distanceBetweenPoint1AndPoint2 * (point2.getY() - point1.getY()));
                    
                    EquiDistancePoint resultingPoint = 
                            new EquiDistancePoint(newPoint, (point2.getY() - point1.getY()) / (point2.getX() - point1.getX()));
                    result.add(resultingPoint);
                    
                    point = newPoint;                                   // now get the distance point from the new point
                    currentDistance = wholeDistance;                    // restore original distance
                }
                else {                                                  // move to the next segment
                    currentDistance -= distanceBetweenPoint1AndPoint2;	// don't have to go that far
                    point = pointsVector.get(i);                        // begin from the new polygon point
                    i++;                                                // compare with the next
                }
            }
        }
        
        return result;
    }
    
    private List<Point> calcOrnamentCenters(EquiDistancePoint equiDistancePoint, double distance){
        List<Point> result = new ArrayList();
        
        Point point = equiDistancePoint.getPoint();
        double direction = equiDistancePoint.getDirection();
        
        Point point1 = new Point();
        Point point2 = new Point();
        
        double det = 0;

        if (Math.abs(direction) > ORNAMENT_CENTER_DIRECTION_FACTOR){
            if (direction != 0) {
                direction = -1.0 / direction;
            }
            if ((direction * direction + 1) != 0){
                det = distance * distance / (direction * direction + 1);
            }
        }

        double x = 0;
        double y = 0;
        if (det == 0.0) {
            x = point.getX();
            y = point.getY() + distance;
        } else {
            x = point.getX() + Math.sqrt(det);
            y = point.getY() + direction * (x - point.getX());
        }

        point1.setX(x);
        point1.setY(y);

        point2.setX(point.getX() - (x - point.getX()));
        point2.setY(point.getY() - (y - point.getY()));
        
        result.add(point1);
        result.add(point2);
        
        return result;
    }
    
    public double getElementSize() {
        return elementSize;
    }
    
    private class EquiDistancePoint {
        private Point point;
        private double direction;
        
        public EquiDistancePoint(Point point, double direction){
            this.point = point;
            this.direction = direction;
        }

        public Point getPoint() {
            return point;
        }

        public void setPoint(Point point) {
            this.point = point;
        }

        public double getDirection() {
            return direction;
        }

        public void setDirection(double direction) {
            this.direction = direction;
        }
        
    }
}
