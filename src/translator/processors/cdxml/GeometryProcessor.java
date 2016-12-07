package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.ArrowHeadConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;
import translator.utils.Line;
import translator.utils.Vector;

public class GeometryProcessor extends Properties3DProcessor {
    
    private static final double LINE_INFLATE_FACTOR = 0.1;
    private static final int TOP_LEFT_CORNER = 0;
    private static final int BOTTOM_RIGHT_CORNER = 1;
    
    //Taken from C++
    private static double NORMAL_OFFSET = 35;
    private static double DEFAULT_FACTOR = 0;    
    private static double DEFAULT_HEAD_SIZE = 10;
    private static double DEFAULT_ARROWHEAD_CENTER_SIZE = 8.75;
    private static double DEFAULT_ARROW_WIDTH = 2.5;
    private static double ANGSTROMS_PER_FIXED_LEN = 1.523;
        
    private double relationValue;    
    
    private double xLow = Double.MAX_VALUE;
    private double yLow = Double.MAX_VALUE;
    
    private double xHigh = 0;
    private double yHigh = 0;
    
    private Point rkOffset;
    private Point rkNormal;
    private Point rkDirection;
    
    public GeometryProcessor() {
    }
    
    protected void configure(){
        super.configure();
        
        type = getElement().getAttribute(ParseElementDefinition.GEOMETRY_TYPE);
        basisElementsPosition = new ArrayList();
        
        if(getElement().hasAttribute(ParseElementDefinition.GEOMETRY_RELATION_VALUE)){
            relationValue = Double.parseDouble(getElement().getAttribute(
                    ParseElementDefinition.GEOMETRY_RELATION_VALUE));
        }
        
        String[] basisElements = getElement().getAttribute(
                ParseElementDefinition.GEOMETRY_BASIS_OBJECT).trim().split(" ");
        
        if(!type.equals(ParseElementDefinition.GEOMETRY_TYPE_NORMAL_FROM_POINT_PLANE)){
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
    }
    
    protected void process() {
        ShapeBuilderConfiguration result = null;
        
        
        //Ontain the highest and the lowest position
        for(Point position : basisElementsPosition){
            if(position.getY() < yLow){
                yLow = position.getY();
            }
            
            if(position.getX() < xLow){
                xLow = position.getX();
            }
            
            if(position.getY() > yHigh){
                yHigh = position.getY();
            }
            
            if(position.getX() > xHigh){
                xHigh = position.getX();
            }
        }
        
        if(type.equals(ParseElementDefinition.GEOMETRY_TYPE_PLANE_FROM_POINTS)){
            SplineConfiguration rectangle = createPlane();
            rectangle.setColor(getColor());
            rectangle.setStrokeWidth(getLineWidth());
            rectangle.setZOrder(getZOrder());
            
            result = rectangle;
        } else if(type.equals(ParseElementDefinition.GEOMETRY_TYPE_LINE_FROM_POINTS)){
            SegmentConfiguration segment = createLine();
            segment.setColor(getColor());
            segment.setStrokeWidth(getLineWidth());
            segment.setZOrder(getZOrder());
            
            result = segment;
        } else if(type.equals(ParseElementDefinition.GEOMETRY_TYPE_CENTROID_FROM_POINTS)){
            Point center = calculateCentroidCenter();
            CompositeShapeConfiguration resultingConfiguration =
                    createPoint(center);
            
            resultingConfiguration.setColor(getColor());
            resultingConfiguration.setStrokeWidth(getLineWidth());
            resultingConfiguration.setZOrder(getZOrder());
            
            result = resultingConfiguration;
        } else if(type.equals(ParseElementDefinition.GEOMETRY_TYPE_POINT_FROM_POINT_POINT_DISTANCE) ||
                type.equals(ParseElementDefinition.GEOMETRY_TYPE_POINT_FROM_POINT_POINT_PERCENTAGE)){
                //take from C++ code
                CompositeShapeConfiguration resultingConfiguration = null;
                Point center = calculateCenterPoint();
                resultingConfiguration = processPointFromPointPoint(center, resultingConfiguration);
                result = resultingConfiguration;
        } else if(type.equals(
                ParseElementDefinition.GEOMETRY_TYPE_NORMAL_FROM_POINT_PLANE)){
            Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
            
            String[] basisElements = getElement().getAttribute(
                ParseElementDefinition.GEOMETRY_BASIS_OBJECT).trim().split(" ");
            
            String normalCoords = getEnvironment().getCoords(basisElements[1]);
            ParsedElement planeElement = getEnvironment().getGeometricPlaneObject(basisElements[0]);                   
            
            //if the basisElements are inverted, then recalculate those values
            if(normalCoords == null){
                normalCoords = getEnvironment().getCoords(basisElements[0]);
                planeElement = getEnvironment().getGeometricPlaneObject(basisElements[1]);
            }            
            
            Point position = parseCoords(normalCoords, getElement());
            
            basisElements = planeElement.getAttribute(
                ParseElementDefinition.GEOMETRY_BASIS_OBJECT).trim().split(" ");
            
            for(String element : basisElements){
                basisElementsPosition.add(
                        parseCoords(getEnvironment().getCoords(element.trim()), planeElement));
            }
            
            SplineConfiguration rectangle = createPlane();
            Point offsetToPlane = pointToPlane(
                    position, rectangle.getSegments().get(0).getBeginPoint(), rkNormal);
            
            Point endPoint = position.subtract(offsetToPlane);
            if(GeometricOperations.distance(offsetToPlane) < 1){
                offsetToPlane = rkNormal.byScalar(-1);
            }else{
                offsetToPlane = new Vector(offsetToPlane).normalize();
            }
            
            Point beginPoint = endPoint.subtract(offsetToPlane.byScalar(-50 * getLineWidth()));            
            
            double normalAngle = GeometricOperations.angle(beginPoint, endPoint);
            double distance = GeometricOperations.distance(beginPoint, endPoint);
            
            endPoint = GeometricOperations.offset(endPoint, normalAngle, distance);
            beginPoint = GeometricOperations.offset(endPoint, normalAngle, -distance);
                        
            //taken from C++ code            
            double arrowHeadLength = DEFAULT_ARROWHEAD_CENTER_SIZE * lineWidth;            
            double arrowHeadWidth = DEFAULT_ARROW_WIDTH * lineWidth;
                        
            Point arrowPoint = GeometricOperations.offset(endPoint, normalAngle, arrowHeadLength);
            
            arrowPoint = endPoint;
            
            Point basePoint = GeometricOperations.offset(arrowPoint, normalAngle, -arrowHeadLength);
            
            double endPointOffset = GeometricOperations.distance(basePoint, arrowPoint) * Math.abs(DEFAULT_HEAD_SIZE / DEFAULT_ARROWHEAD_CENTER_SIZE);
            
            Point arrowEndPoint = GeometricOperations.offset(arrowPoint, normalAngle, -endPointOffset);
                        
            SplineConfiguration arrow = ArrowHeadConfiguration.getArrowHeadShape(
                    basePoint, arrowPoint, arrowEndPoint, arrowHeadWidth, normalAngle, DEFAULT_FACTOR, false, false);
            arrow.setFill(true);            
            
            configurations.add(arrow);
            
            SegmentConfiguration segment = new SegmentConfiguration(basePoint, beginPoint);
            segment.setDashed(true);
            segment.setDashLength(hashSpacing);
            segment.setLineCap(LineCap.Butt);
            segment.setStrokeWidth(lineWidth);
            
            configurations.add(segment);
            
            CompositeShapeConfiguration resultingConfiguration = 
                    new CompositeShapeConfiguration(
                    ParseElementDefinition.GEOMETRY_TYPE_NORMAL_FROM_POINT_PLANE,
                    configurations);
            
            resultingConfiguration.setColor(getColor());            
            resultingConfiguration.setZOrder(getZOrder());            
            result = resultingConfiguration;
        }
        
        setResultingConfiguration(result);
    }
    
    //taken from C++
    private CompositeShapeConfiguration createPoint(Point center){
        Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
        
        SegmentConfiguration firstSegment = null;
        firstSegment = new SegmentConfiguration(
                new Point(center.getX() + getLineWidth() * 5, center.getY()),
                new Point(center.getX() - getLineWidth() * 5, center.getY()));
        
        configurations.add(firstSegment);
        
        SegmentConfiguration secondSegment = null;
        secondSegment = new SegmentConfiguration(
                new Point(center.getX(), center.getY() + getLineWidth() * 5),
                new Point(center.getX(), center.getY() - getLineWidth() * 5));
        
        configurations.add(secondSegment);
        
        double angle = -Math.PI / 6;
        
        SegmentConfiguration thirdSegment = null;
        thirdSegment = new SegmentConfiguration(
                GeometricOperations.offset(center, angle, getLineWidth() * 5),
                GeometricOperations.offset(center, angle, - getLineWidth() * 5));
        
        configurations.add(thirdSegment);
        
        CompositeShapeConfiguration resultingConfiguration =
                new CompositeShapeConfiguration(
                ParseElementDefinition.GEOMETRY_TYPE_CENTROID_FROM_POINTS, configurations);
        
        return resultingConfiguration;
    }
    
    private SegmentConfiguration createLine() {
        SegmentConfiguration result = null;
        List<Point> boundingBox = parsePoints(getBoundingBox(), getElement());
        
        if (basisElementsPosition.size() > 1){
            List<Point> points = new ArrayList();
            for (Point point : basisElementsPosition){
                // The line in the coordenates of C++ are not drawn as in Java and C#
                points.add(new Point(
                        (point.getX()),
                        (point.getY()),
                        (point.getZ())));
            }
            Line line = lineFromPoints(points);
            
            Point beginPoint = line.getBegin();
            Point endPoint = line.getEnd();
            
            // Inflate by a smidge (10% on each end)
            Point v = endPoint.subtract(beginPoint).byScalar(LINE_INFLATE_FACTOR);
            endPoint = endPoint.add(v);
            beginPoint = beginPoint.subtract(v);
            
            result = new SegmentConfiguration(beginPoint, endPoint);
        }
        
        return result;
    }
    
    private Line lineFromPoints(List<Point> points){
        Point lineBeginPoint = new Point();
        Point lineEndPoint = new Point();
        
        orthogonalLineFit(points.size(), points);
        
        lineBeginPoint = new Point(rkOffset);
        lineEndPoint = lineBeginPoint.add(new Point(rkDirection));
        
        // Rotate so that the line is parallel to the X-axis
        Matrix3D m = new Matrix3D();
        m.translate(-rkOffset.getX(), -rkOffset.getY(), -rkOffset.getZ());
        m.rotateZ(-Math.atan2(rkDirection.getY(), rkDirection.getX()));
        Point lineBeginPointTemp = m.transform(lineBeginPoint);
        Point lineEndPointTemp = m.transform(lineEndPoint);
        m.rotateY(Math.atan2(lineEndPointTemp.getZ() - lineBeginPointTemp.getZ(),
                lineEndPointTemp.getX() - lineBeginPointTemp.getX()));
        lineBeginPoint = m.transform(lineBeginPoint);
        lineEndPoint = m.transform(lineEndPoint);
        
        // Adjust the current X coordinate of the result points to match the min and max values in the rotated frame
        for (Point point : points){
            Point ptTemp = m.transform(point);
            lineBeginPoint.setX(Math.min(lineBeginPoint.getX(), ptTemp.getX()));
            lineEndPoint.setX(Math.max(lineEndPoint.getX(), ptTemp.getX()));
        }
        
        // Return the result points to the actual frame
        Matrix3D mInv = m.getInverseMatrix();
        lineBeginPoint = mInv.transform(lineBeginPoint);
        lineEndPoint = mInv.transform(lineEndPoint);
        
        return new Line(lineBeginPoint, lineEndPoint);
    }
    
    private void orthogonalLineFit(int iQuantity, List<Point> akPoints){
        // compute average of points
        rkOffset = akPoints.get(0);
        int i;
        for (i = 1; i < iQuantity; i++)
            rkOffset = rkOffset.add(akPoints.get(i));
        double fInvQuantity = 1.0f/iQuantity;
        rkOffset = rkOffset.byScalar(fInvQuantity);
        
        // compute sums of products
        double fSumXX = 0.0f, fSumXY = 0.0f, fSumXZ = 0.0f;
        double fSumYY = 0.0f, fSumYZ = 0.0f, fSumZZ = 0.0f;
        for (i = 0; i < iQuantity; i++) {
            Point kDiff = akPoints.get(i).subtract(rkOffset);
            fSumXX += kDiff.getX()*kDiff.getX();
            fSumXY += kDiff.getX()*kDiff.getY();
            fSumXZ += kDiff.getX()*kDiff.getZ();
            fSumYY += kDiff.getY()*kDiff.getY();
            fSumYZ += kDiff.getY()*kDiff.getZ();
            fSumZZ += kDiff.getZ()*kDiff.getZ();
        }
        
        // setup the eigensolver
        Eigen kES = new Eigen();
        kES.matrix[0][0] = fSumYY+fSumZZ;
        kES.matrix[0][1] = -fSumXY;
        kES.matrix[0][2] = -fSumXZ;
        kES.matrix[1][0] = kES.matrix[0][1];
        kES.matrix[1][1] = fSumXX+fSumZZ;
        kES.matrix[1][2] = -fSumYZ;
        kES.matrix[2][0] = kES.matrix[0][2];
        kES.matrix[2][1] = kES.matrix[1][2];
        kES.matrix[2][2] = fSumXX+fSumYY;
        
        // compute eigenstuff, smallest eigenvalue is in last position
        kES.DecrSortEigenStuff();
        
        // unit-length direction for best-fit line
        rkDirection = new Point();
        rkDirection.setX(kES.matrix[0][2]);
        rkDirection.setY(kES.matrix[1][2]);
        rkDirection.setZ(kES.matrix[2][2]);
    }
    
    //Taken from C++
    private SplineConfiguration createPlane() {
        SplineConfiguration result = null;
        
        if (basisElementsPosition.size() > 2){
            List<Point> points = new ArrayList();
            for (Point point : basisElementsPosition){
                points.add(new Point(point));
            }
            double fit = orthogonalPlaneFit(basisElementsPosition.size(), basisElementsPosition);
            
            Point offset = rkOffset;
            Point normal = rkNormal;
            
            Point desiredNormalDir = GeometricOperations.crossProduct(
                    points.get(2).subtract(points.get(0)),
                    points.get(1).subtract(points.get(0)));
            if(GeometricOperations.distance(offset.add(normal), offset.add(desiredNormalDir)) >
                    GeometricOperations.distance(offset.add(normal), offset.subtract(desiredNormalDir))){
                normal = normal.byScalar(-1);
                rkNormal = normal;
            }
            
            result = calcCornersFromObjects(offset, normal);
        }
        
        return result;
    }
    
    //Taken from C++
    private double orthogonalPlaneFit(int iQuantity, List<Point> akPoint){
        // compute average of points
        rkOffset = akPoint.get(0);
        
        int i;
        for (i = 1; i < iQuantity; i++)
            rkOffset = rkOffset.add(akPoint.get(i));
        double fInvQuantity = 1.0f/iQuantity;
        rkOffset = rkOffset.byScalar(fInvQuantity);
        
        // compute sums of products
        double fSumXX = 0.0f, fSumXY = 0.0f, fSumXZ = 0.0f;
        double fSumYY = 0.0f, fSumYZ = 0.0f, fSumZZ = 0.0f;
        for (i = 0; i < iQuantity; i++) {
            Point kDiff = akPoint.get(i).subtract(rkOffset);
            fSumXX += kDiff.getX()*kDiff.getX();
            fSumXY += kDiff.getX()*kDiff.getY();
            fSumXZ += kDiff.getX()*kDiff.getZ();
            fSumYY += kDiff.getY()*kDiff.getY();
            fSumYZ += kDiff.getY()*kDiff.getZ();
            fSumZZ += kDiff.getZ()*kDiff.getZ();
        }
        
        // setup the eigensolver
        Eigen kES = new Eigen();
        kES.matrix[0][0] = fSumXX;
        kES.matrix[0][1] = fSumXY;
        kES.matrix[0][2] = fSumXZ;
        kES.matrix[1][0] = kES.matrix[0][1];
        kES.matrix[1][1] = fSumYY;
        kES.matrix[1][2] = fSumYZ;
        kES.matrix[2][0] = kES.matrix[0][2];
        kES.matrix[2][1] = kES.matrix[1][2];
        kES.matrix[2][2] = fSumZZ;
        
        // compute eigenstuff, smallest eigenvalue is in last position
        kES.DecrSortEigenStuff();
        
        // unit-length direction for best-fit line
        rkNormal = new Point();
        rkNormal.setX(kES.matrix[0][2]);
        rkNormal.setY(kES.matrix[1][2]);
        rkNormal.setZ(kES.matrix[2][2]);
        
        // the minimum energy
        return kES.diag[2];
    }
    
    //Taken from C++
    private SplineConfiguration calcCornersFromObjects(Point offset, Point normal) {
        // Rotate so that the normal is parallel to the Z-axis
        Matrix3D matrix = new Matrix3D();
        matrix.translate(-offset.getX(), -offset.getY(), -offset.getZ());
        matrix.rotateX(Math.atan2(normal.getY(), normal.getZ()));
        Point normalTemp = matrix.transform(offset.add(normal));
        matrix.rotateY(-Math.atan2(normalTemp.getX(), normalTemp.getZ()));        
        
        // Calculate the bounds of the objects in the rotated frame
        boolean first = true;
        
        Point beginPoint = new Point();
        Point endPoint = new Point();
        
        for (Point point : basisElementsPosition){
            Point transformPoint = matrix.transform(point);
            
            if (first){
                beginPoint = new Point(transformPoint);
                endPoint = new Point(transformPoint);
                
                first = false;
            } else {
                
                if(transformPoint.getX() < beginPoint.getX()){
                    beginPoint.setX(transformPoint.getX());
                } else if(endPoint.getX() < transformPoint.getX()){
                    endPoint.setX(transformPoint.getX());
                }
                
                if(transformPoint.getY() < beginPoint.getY()){
                    beginPoint.setY(transformPoint.getY());
                } else if(endPoint.getY() < transformPoint.getY()){
                    endPoint.setY(transformPoint.getY());
                }
            }
        }
        
        // Expand the bounds a bit so that the image for the plane doesn't come right up against the objects
        double inflateOffset = getEnvironment().getMarginWidth() * 2 - getLineWidth() / 2;
                
        beginPoint = GeometricOperations.offset(beginPoint, 0, -inflateOffset);
        beginPoint = GeometricOperations.offset(beginPoint, Math.PI/2, -inflateOffset);
        
        endPoint = GeometricOperations.offset(endPoint, 0, inflateOffset);
        endPoint = GeometricOperations.offset(endPoint, Math.PI/2, inflateOffset);
                
        // Return the result points to the actual frame
        Matrix3D mInv = matrix.getInverseMatrix();
        Point topLeft = mInv.transform(beginPoint);
        Point topRight = mInv.transform(new Point(endPoint.getX(), beginPoint.getY()));
        Point bottomRight = mInv.transform(endPoint);
        Point bottomLeft = mInv.transform(new Point(beginPoint.getX(), endPoint.getY()));
        
        List<Point> configurationPoints = new ArrayList();
        configurationPoints.add(topLeft);
        configurationPoints.add(topRight);
        configurationPoints.add(bottomRight);
        configurationPoints.add(bottomLeft);
        
        SplineConfiguration result = new SplineConfiguration(
                configurationPoints, true);
        
        return result;
    }        
    
    //Taken from C++
    private Point calculateCentroidCenter(){
        Point result = new Point();
        
        if (!basisElementsPosition.isEmpty()){
            for(Point point : basisElementsPosition){
                result = result.add(point);
            }
            
            result = result.byScalar(1.0 / basisElementsPosition.size());
        }
        
        return result;
    }
    private Point calculateCenterPoint(){
           //take from C++ code
            CompositeShapeConfiguration resultingConfiguration;
            Point center = new Point(0,0,0);
            if (basisElementsPosition.size() >= 2){
                Point beginPoint = basisElementsPosition.get(TOP_LEFT_CORNER);
                Point endPoint = basisElementsPosition.get(BOTTOM_RIGHT_CORNER);
                if (type.equals(ParseElementDefinition.GEOMETRY_TYPE_POINT_FROM_POINT_POINT_PERCENTAGE)){
                    double bondAngle = GeometricOperations.angle(beginPoint, endPoint);
                    double actualDistance = GeometricOperations.distance(beginPoint, endPoint);
                    
                    center = GeometricOperations.offset(beginPoint, bondAngle, actualDistance * relationValue);
                }else if (type.equals(ParseElementDefinition.GEOMETRY_TYPE_POINT_FROM_POINT_POINT_DISTANCE)){
                    double desiredDistance = relationValue /  ANGSTROMS_PER_FIXED_LEN * environment.getBondLength();
                    double actualDistance = GeometricOperations.distance(beginPoint, endPoint);
                    double bondAngle = GeometricOperations.angle(beginPoint, endPoint);

                    center = GeometricOperations.offset(beginPoint, bondAngle, actualDistance * desiredDistance / actualDistance);
                }   
            } 
        return center;
    }
    
    private CompositeShapeConfiguration processPointFromPointPoint(Point center, CompositeShapeConfiguration resultingConfiguration){
            //take from C++ code
            if (basisElementsPosition.size() >= 2){            
                resultingConfiguration = createPoint(center);
                resultingConfiguration.setColor(getColor());
                resultingConfiguration.setStrokeWidth(getLineWidth());
                resultingConfiguration.setZOrder(getZOrder());    
            } else{ 
                // If basisElementsPosition size is not greater or equal than 2, then do not create the object GEOMETRY_TYPE_POINT_FROM_POINT_POINT
                resultingConfiguration = new CompositeShapeConfiguration(getElement().getId(), new ArrayList());
            }
            return resultingConfiguration;
    }
    protected void cleanup(){
        type = null;
        basisElementsPosition = null;
        
        super.cleanup();
    }
}
