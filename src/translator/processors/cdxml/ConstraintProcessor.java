package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.EllipseConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Vector;

public class ConstraintProcessor extends Properties3DProcessor {
    
    //Taken from C++
    private static double ANGSTROMS_PER_FIXED_LEN = 1.523;
    private static double DEFAULT_OVAL_ECCENTRICITY = 0.4;
    
    private Point position;
    
    private double constraintMin;
    private double constraintMax;        
    
    private Point center;
    private Point start;
    private Point end;                
    private Point basis0;
    private Point basis1;
    private Point basis2;
    private Point basis3;
    
    private Point rotatedCenter;
    private Point rotatedStart;
    private Point rotatedEnd;
    private double startAngle;
        
    public ConstraintProcessor() {
    }

    protected void configure() {
        super.configure();
        
        type = getElement().getAttribute(ParseElementDefinition.CONSTRAINT_TYPE);
        constraintMin = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.CONSTRAINT_MIN));
        constraintMax = Double.parseDouble(getElement().getAttribute(
                ParseElementDefinition.CONSTRAINT_MAX));
        
        String nodeId = getElement().getAttribute(
                ParseElementDefinition.CONSTRAINT_BASIS_OBJECT);
        
        String[] basisElements = getElement().getAttribute(
                ParseElementDefinition.GEOMETRY_BASIS_OBJECT).trim().split(" ");
        
        basisElementsPosition = new ArrayList();
        for(String element : basisElements){
            // assume basis object is a node
            String elementCoordinates = getEnvironment().getCoords(element.trim());
            
            // if element was not a node, try with geometry
            if (elementCoordinates == null) {
                ParsedElement geometry = getEnvironment().getGeometricPlaneObject(element.trim());
                if (geometry != null) {
                    List<Point> boundingBoxPoints = parsePoints(geometry.getAttribute(ParseElementDefinition.BOUNDING_BOX), getElement());
                    // obtain center point
                    Point startPoint = boundingBoxPoints.get(0);
                    Point endPoint = boundingBoxPoints.get(1);
                    Point elementCenter = new Point((endPoint.getX() - startPoint.getX()) / 2 + startPoint.getX(),
                            (endPoint.getY() - startPoint.getY()) / 2 + startPoint.getY(),
                            (endPoint.getZ() - startPoint.getZ()) / 2 + startPoint.getZ());
                    basisElementsPosition.add(elementCenter);
                }
            } else {
                basisElementsPosition.add(
                    parseCoords(elementCoordinates, getElement()));
            }
        }
    }
    
    protected void process() {
        ShapeBuilderConfiguration result = null;
        
        if(type.equals(ParseElementDefinition.CONSTRAINT_TYPE_EXCLUSION_SPHERE)){
            Collection<ShapeBuilderConfiguration> configurations = new ArrayList();

            position = basisElementsPosition.get(0);
            
            double radius = constraintMax * getEnvironment().getBondLength() / ANGSTROMS_PER_FIXED_LEN;
            double majorAxis = radius - width/2;
            double minorAxis = (radius * DEFAULT_OVAL_ECCENTRICITY) - width/2;
            
            EllipseConfiguration firstEllipse = new EllipseConfiguration(
                    position, majorAxis, majorAxis, 0);
            EllipseConfiguration secondEllipse = new EllipseConfiguration(
                    position, minorAxis, majorAxis, 0);
            EllipseConfiguration thirdEllipse = new EllipseConfiguration(
                    position, majorAxis, minorAxis, 0);

            configurations.add(firstEllipse);
            configurations.add(secondEllipse);
            configurations.add(thirdEllipse);

            CompositeShapeConfiguration resultingConfiguration = new 
                    CompositeShapeConfiguration(ParseElementDefinition.CONSTRAINT, configurations);
            resultingConfiguration.setColor(getColor());
            resultingConfiguration.setStrokeWidth(getLineWidth());
            resultingConfiguration.setZOrder(zOrder);
            
            result = resultingConfiguration;
        } else if(type.equals(ParseElementDefinition.GEOMETRY_TYPE_DISTANCE)){
            Point beginPoint = basisElementsPosition.get(0);
            Point endPoint = basisElementsPosition.get(1);
            
            SegmentConfiguration segment = new SegmentConfiguration(
                    beginPoint, endPoint);
            
            segment.setColor(getColor());
            segment.setStrokeWidth(getLineWidth());
            segment.setDashed(true);
            if (dashLength != 0){
                segment.setDashLength(dashLength);
            }else{
                segment.setDashLength(environment.getHashSpacing());
            }
            segment.setLineCap(LineCap.Butt);
            segment.setZOrder(getZOrder());
            
            result = segment;
        } else if(type.equals(ParseElementDefinition.GEOMETRY_TYPE_ANGLE)){
            boolean largerSide = false;
            //Arc angle defined by three points - taken from ChemDraw C++
            if (basisElementsPosition.size() == 3){
                calcArcFromGeometries();
                Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
                
                // Calculate the arc
                List<Point> arcPoints = computeArc3D(center, start, end, largerSide);
                
                SplineConfiguration arcSpline = buildSplineBeziers(arcPoints);
                
                configurations.add(arcSpline);
                
                CompositeShapeConfiguration resultingConfiguration =
                        new CompositeShapeConfiguration(ParseElementDefinition.GEOMETRY_TYPE_ANGLE, configurations);
                
                resultingConfiguration.setColor(color);
                resultingConfiguration.setStrokeWidth(lineWidth);
                resultingConfiguration.setZOrder(zOrder);
                
                result = resultingConfiguration;
                
            }
            
            //Dihedral angle defined by four points - taken from ChemDraw C++
            if (basisElementsPosition.size() == 4){
                calcDihedralFromGeometries();
                    Collection<ShapeBuilderConfiguration> configurations = new ArrayList();

                    // Calculate the arc                                        
                    List<Point> arcPoints = computeArc3D(center, start, end, largerSide);

                    // The ends of the arc need to be corner points, not curve points
                    Point actualStart = arcPoints.get(1);
                    Point actualEnd = arcPoints.get(arcPoints.size() - 2);
                    arcPoints.set(0, actualStart);
                    arcPoints.set(arcPoints.size() - 1, actualEnd);

                    // Draw from the near dihedral to the start of the arc, then draw the arc
                    arcPoints.add(0, basis3);
                    arcPoints.add(0, basis3);
                    arcPoints.add(0, basis3);

                    // ...then draw to the far dihedral                    
                    arcPoints.add(basis0);
                    arcPoints.add(basis0);
                    arcPoints.add(basis0);

                    // ...and back to the start of the arc
                    arcPoints.add(actualEnd);
                    arcPoints.add(actualEnd);
                    arcPoints.add(actualEnd);

                    // ...down to the point of the sector
                    arcPoints.add(center);
                    arcPoints.add(center);
                    arcPoints.add(center);

                    // ...and finally back to the start of the arc
                    arcPoints.add(actualStart);
                    arcPoints.add(actualStart);
                    arcPoints.add(actualStart);

                    SplineConfiguration arcSpline = buildSplineBeziers(arcPoints);
                    
                    configurations.add(arcSpline);                    

                    CompositeShapeConfiguration resultingConfiguration =
                        new CompositeShapeConfiguration(ParseElementDefinition.GEOMETRY_TYPE_ANGLE, configurations);

                    resultingConfiguration.setColor(color);
                    resultingConfiguration.setStrokeWidth(getLineWidth());
                    resultingConfiguration.setZOrder(zOrder);

                    result = resultingConfiguration;            

            } else {
                
            }
        }
        
        setResultingConfiguration(result);
    }

    /**
     *Calculate the points to draw a Dihedral angle defined by four points
     */
    private void calcDihedralFromGeometries(){
            Vector startVec = new Vector();
            Vector endVec = new Vector();

            basis0 = basisElementsPosition.get(0);
            basis1 = basisElementsPosition.get(1);
            basis2 = basisElementsPosition.get(2);
            basis3 = basisElementsPosition.get(3);
            center = (basisElementsPosition.get(1).add(basisElementsPosition.get(2))).byScalar(0.5);            
            startVec = new Vector(basisElementsPosition.get(0).subtract(basisElementsPosition.get(1)));            
            endVec = new Vector(basisElementsPosition.get(3).subtract(basisElementsPosition.get(2)));
                        
            calcPropertiesFromGeometries(startVec, endVec); 
    }
    
    /**
     *Calculate the points to draw a Arc angle defined by three points
     */
    private void calcArcFromGeometries(){
        Vector startVec = new Vector();
        Vector endVec = new Vector();
        
        basis0 = new Point(0.0,0.0,0.0);
        basis1 = new Point(0.0,0.0,0.0);
        basis2 = new Point(0.0,0.0,0.0);
        center = basisElementsPosition.get(1);
        startVec = new Vector(basisElementsPosition.get(0).subtract(center));
        endVec = new Vector(basisElementsPosition.get(2).subtract(center));
        
        calcPropertiesFromGeometries(startVec, endVec);
    }
    
    /**
     *Method to finish to calculate the points to draw a property.
     *This method must be called at the end of the others CalcxxxFromGeometries methods.
     *It was taken from the method CalcPropertiesFromGeometries of C++ ChemDraw.
     */
    private void calcPropertiesFromGeometries(Vector startVec, Vector endVec)
        {                        
            // Half the average length of the two vectors                        
            double length = (GeometricOperations.distance(startVec) + GeometricOperations.distance(endVec))  / 2 / 2;
            //Make sure the length is no larger than half the bond length, else it looks really weird
            length = Math.min(length, environment.getBondLength());

            startVec = startVec.normalize();
            endVec = endVec.normalize();

            start = center.add(startVec.byScalar(length));
            end = center.add(endVec.byScalar(length));

            if (basis1 != null){
                basis0 = basis1.add(startVec.byScalar(length));
            }
            if (basis2 != null){
                basis3 = basis2.add(endVec.byScalar(length));
            }
        
        if (basisElementsPosition.size()!= 3) {
                // Make sure the wedge is perpendicular to the axis
                Vector normal = new Vector(basis1.subtract(basis2));
                normal = normal.normalize();

                Point offsetToPlane = pointToPlane(start, center, normal);
                start = start.subtract(offsetToPlane);

                offsetToPlane = pointToPlane(end, center, normal);
                end = end.subtract(offsetToPlane);

                length = Math.min(GeometricOperations.distance(start, center), GeometricOperations.distance(end, center));
                startVec = new Vector(start.subtract(center));
                startVec = startVec.normalize();
                start = center.add(startVec.byScalar(length));
                endVec = new Vector(end.subtract(center));
                endVec = endVec.normalize();
                end = center.add(endVec.byScalar(length));
                
                Point intersection1 = GeometricOperations.intersection(basis0, basis1, start, start.add(basis1).subtract(center));
                if (!Double.isNaN(intersection1.getX()) && !Double.isNaN(intersection1.getY())) {
                    basis0 = intersection1;
                }
                Point intersection2 = GeometricOperations.intersection(basis3, basis2, end, end.add(basis1).subtract(center));
                if (!Double.isNaN(intersection2.getX()) && !Double.isNaN(intersection2.getY())) {
                    basis3 = intersection2;
                }
            }            
        }
    /**
     *Calculate the points to draw the arc of a dihedral property.
     */
    private List<Point> computeArc3D(Point center, Point startPoint, Point endPoint, boolean largerSide){
        List<Point> result;
        Vector start = new Vector(startPoint.subtract(center));
        Vector end = new Vector(endPoint.subtract(center));
        Vector normal = new Vector(GeometricOperations.crossProduct(start, end));

        double totAngle = GeometricOperations.getAngle3D(start, end);
        if (largerSide)
            totAngle = 2 * Math.PI - totAngle;

        Matrix3D matrix = new Matrix3D();
        matrix.translate(-center.getX(), -center.getY(), -center.getZ());

        // First, rotate the frame into the XY plane
        if (GeometricOperations.distance(normal) == 0){
            // Colinear points; just rotate around the Z axis
            matrix.rotateZ(-Math.atan2(start.getZ(), start.getX()));
        }else{
            // Noncolinear points.  Rotate the normal to be parallel to the Z axis
            matrix.rotateX(Math.atan2(normal.getY(), normal.getZ()));
            Point normalTemp = matrix.transform(center.add(normal));
            matrix.rotateY(-Math.atan2(normalTemp.getX(), normalTemp.getZ()));
        }

        rotatedCenter = matrix.transform(center);
        rotatedStart = matrix.transform(startPoint);
        rotatedEnd = matrix.transform(endPoint);

        startAngle = Math.atan2(rotatedStart.getY() - rotatedCenter.getY(), rotatedStart.getX() - rotatedCenter.getX());

        double startAngleDeg = startAngle * 180 / Math.PI + 90;
        double totAngleDeg = -totAngle * 180 / Math.PI;

        if (totAngleDeg < 0)
            totAngleDeg = -totAngleDeg;

        rotatedStart = rotatedCenter.add(new Point(GeometricOperations.distance(rotatedStart), 0));

        List<Point> arcPoints = calculateTransformedArcPoints(rotatedCenter, rotatedStart, startAngleDeg, totAngleDeg);            

        for(Point arcPoint : arcPoints){
            Point transformedArcPoint = matrix.getInverseMatrix().transform(arcPoint);
            arcPoint.setX(transformedArcPoint.getX());
            arcPoint.setY(transformedArcPoint.getY());
        }

        result = arcPoints;
        return result;
    }
    
    protected void cleanup() {
        position = null;
        type = null;
        constraintMin = 0;
        constraintMax = 0;
        
        super.cleanup();
    }
    
}
