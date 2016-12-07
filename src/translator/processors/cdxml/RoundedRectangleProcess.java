package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class RoundedRectangleProcess extends CommonRectangleProcessor{
    private double cornerRadius;
    private List<Point> roundedRectanglePoints = new ArrayList();
    private static final double DEFAULT_CORNER_RADIUS = 6;
    //Default cornder radius taken from the C++ code
    private static final double CORNER_RADIUS_SCALE_FACTOR = 100;
    
    //Constant to obtains the control points, taken from C++ code
    private static final double CONTROL_POINTS_SCALE = 0.55197;
    
    //Rectangle Points
    private static final int TOP_LEFT = 0;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_RIGHT = 2;
    private static final int BOTTOM_LEFT = 3;
    
    //Rounded Rectangle Points
    private static final int TOP_LEFT_POINT_1 = 0;
    private static final int TOP_LEFT_CONTROL_POINT_1 = 1;
    private static final int TOP_LEFT_CONTROL_POINT_2 = 2;
    private static final int TOP_LEFT_POINT_2 = 3;
    
    private static final int TOP_RIGHT_POINT_1 = 4;
    private static final int TOP_RIGHT_CONTROL_POINT_1 = 5;
    private static final int TOP_RIGHT_CONTROL_POINT_2 = 6;
    private static final int TOP_RIGHT_POINT_2 = 7;
    
    private static final int BOTTOM_RIGHT_POINT_1 = 8;
    private static final int BOTTOM_RIGHT_CONTROL_POINT_1 = 9;
    private static final int BOTTOM_RIGHT_CONTROL_POINT_2 = 10;
    private static final int BOTTOM_RIGHT_POINT_2 = 11;
    
    private static final int BOTTOM_LEFT_POINT_1 = 12;
    private static final int BOTTOM_LEFT_CONTROL_POINT_1 = 13;
    private static final int BOTTOM_LEFT_CONTROL_POINT_2 = 14;
    private static final int BOTTOM_LEFT_POINT_2 = 15;
    
    public RoundedRectangleProcess() {
    }
    
    protected void configure(){
        super.configure();
        if(getElement().hasAttribute(ParseElementDefinition.GRAPHIC_CORNER_RADIUS)){
            cornerRadius = Double.parseDouble(getElement().getAttribute(ParseElementDefinition.GRAPHIC_CORNER_RADIUS)) / CORNER_RADIUS_SCALE_FACTOR * getLineWidth();
        }else{
            cornerRadius = DEFAULT_CORNER_RADIUS;
        }
    }
    
    protected void cleanup() {
        cornerRadius = 0;
        roundedRectanglePoints = null;
        super.cleanup();
    }
    
    protected void process(){
        ShapeBuilderConfiguration resultingConfiguration = null;
        SplineConfiguration configuration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        List<SegmentConfiguration> segments = new ArrayList();
        ParsedElement rectangle = getElement();
        
        roundedRectanglePoints = calculateRoundedPoints(getRectanglePoints());
        segments = calculateRoundedSplines(roundedRectanglePoints);
        configuration = new SplineConfiguration(segments);
        
        if(isShadowed()){            
            List<Point> shadowRoundedPoints = getShadowRectangle(roundedRectanglePoints, center, majorAxisEnd, minorAxisEnd);
            Area roundedArea = convertRoundedRectangleToArea(roundedRectanglePoints);
            Area shadowArea = convertRoundedRectangleToArea(shadowRoundedPoints);
            shadowArea.subtract(roundedArea);
            SplineConfiguration shadowConfig = new SplineConfiguration(
                    getEnvironment().createConfigurationFromArea(shadowArea));
            ((SplineConfiguration) shadowConfig).setColor(getColor().fadeRGB(getColor(), CDXMLEnvironment.getInstance().getShadowRatio()));
            ((SplineConfiguration) shadowConfig).setFill(true);
            
            innerShapes.add(shadowConfig);
        }
        
        if(isFaded()){
            SplineConfiguration fadeConfig = new SplineConfiguration(segments);
            ((SplineConfiguration) fadeConfig).setColor(getFadedColor());
            ((SplineConfiguration) fadeConfig).setFill(true);
            innerShapes.add(fadeConfig);
        }
        
        if(isShaded()){
            SplineConfiguration fadeConfig = new SplineConfiguration(segments);
            ((SplineConfiguration) fadeConfig).setColor(getColor());
            ((SplineConfiguration) fadeConfig).setShaded(true);
            ((SplineConfiguration) fadeConfig).setGradient(
                    RadialGradient.getOvalGradient(getElement().getId(), getColor()));
            ((SplineConfiguration) fadeConfig).setId(ParseElementDefinition.GRAPHIC_ROUNDED_RECTANGLE);
            innerShapes.add(fadeConfig);
        }
        
        ((SplineConfiguration) configuration).setStrokeWidth(getWidth());
        ((SplineConfiguration) configuration).setColor(getColor());
        ((SplineConfiguration) configuration).setDashed(isDashed());
        ((SplineConfiguration) configuration).setDashLength(getHashSpacing());
        ((SplineConfiguration) configuration).setFill(isFilled());
        innerShapes.add(configuration);
        
        resultingConfiguration = new CompositeShapeConfiguration("Rectangle", innerShapes);
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(getZOrder());
        setResultingConfiguration(resultingConfiguration);
    }
    
    /**
     * Calculate the rounded rectangle points from the original rectangle
     * doing an offset of the corner radius size
     */
    private List<Point> calculateRoundedPoints(List<Point> rectanglePoint){
        List<Point> result = new ArrayList();
        
        Point topLeftPoint1;
        Point topLeftControl1;
        Point topLeftControl2;
        Point topLeftPoint2;
        
        Point topRightPoint1;
        Point topRightControl1;
        Point topRightControl2;
        Point topRightPoint2;
        
        Point bottomRightPoint1;
        Point bottomRightControl1;
        Point bottomRightControl2;
        Point bottomRightPoint2;
        
        Point bottomLeftPoint1;
        Point bottomLeftControl1;
        Point bottomLeftControl2;
        Point bottomLeftPoint2;
        
        double widthAngle = GeometricOperations.angle(rectanglePoint.get(TOP_LEFT), rectanglePoint.get(TOP_RIGHT));
        double heightAngle = GeometricOperations.angle(rectanglePoint.get(TOP_LEFT), rectanglePoint.get(BOTTOM_LEFT));
        
        double cornerRadiusX = cornerRadius;
        double cornerRadiusY = cornerRadius;
        
        double width = GeometricOperations.distance(rectanglePoint.get(TOP_LEFT), rectanglePoint.get(TOP_RIGHT));
        double height = GeometricOperations.distance(rectanglePoint.get(TOP_LEFT), rectanglePoint.get(BOTTOM_LEFT));
        
        //Taken From C++
        //if the width of the rectangle its greater than the corner radius
        //then use the cornerRadiusX as width/2
        if (width / 2 < cornerRadius){
            cornerRadiusX = width / 2;
        }
        //if the height of the rectangle its greater than the corner radius
        //then use the cornerRadiusY as width/2
        if (height / 2 < cornerRadius){
            cornerRadiusY = height / 2;
        }            
        
        double cornerCtrlX = CONTROL_POINTS_SCALE * cornerRadiusX;
        double cornerCtrlY = CONTROL_POINTS_SCALE * cornerRadiusY;
        
        //Calculate the points to make the top left bezier curve
        topLeftPoint1 = GeometricOperations.offset(rectanglePoint.get(TOP_LEFT), heightAngle, cornerRadiusY);
        topLeftControl1 = GeometricOperations.offset(rectanglePoint.get(TOP_LEFT), heightAngle, cornerRadiusY - cornerCtrlY);
        topLeftControl2 = GeometricOperations.offset(rectanglePoint.get(TOP_LEFT), widthAngle, cornerRadiusX - cornerCtrlX);
        topLeftPoint2 = GeometricOperations.offset(rectanglePoint.get(TOP_LEFT), widthAngle, cornerRadiusX);
        
        //Calculate the points to make the top right bezier curve
        topRightPoint1 = GeometricOperations.offset(rectanglePoint.get(TOP_RIGHT), widthAngle, -cornerRadiusX);
        topRightControl1 = GeometricOperations.offset(rectanglePoint.get(TOP_RIGHT), widthAngle, -cornerRadiusX + cornerCtrlX);
        topRightControl2 = GeometricOperations.offset(rectanglePoint.get(TOP_RIGHT), heightAngle, cornerRadiusY - cornerCtrlY);
        topRightPoint2 = GeometricOperations.offset(rectanglePoint.get(TOP_RIGHT), heightAngle, cornerRadiusY);
        
        //Calculate the points to make the bottom right bezier curve
        bottomRightPoint1 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_RIGHT), heightAngle, -cornerRadiusY);
        bottomRightControl1 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_RIGHT), heightAngle, -cornerRadiusY + cornerCtrlY);
        bottomRightControl2 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_RIGHT), widthAngle, -cornerRadiusX + cornerCtrlX);
        bottomRightPoint2 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_RIGHT), widthAngle, -cornerRadiusX);
        
        //Calculate the points to make the bottom left bezier curve
        bottomLeftPoint1 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_LEFT), widthAngle, cornerRadiusX);
        bottomLeftControl1 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_LEFT), widthAngle, cornerRadiusX - cornerCtrlX);
        bottomLeftControl2 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_LEFT), heightAngle, -cornerRadiusY + cornerCtrlY);
        bottomLeftPoint2 = GeometricOperations.offset(rectanglePoint.get(BOTTOM_LEFT), heightAngle, -cornerRadiusY);
        
        result.add(topLeftPoint1);
        result.add(topLeftControl1);
        result.add(topLeftControl2);
        result.add(topLeftPoint2);
        
        result.add(topRightPoint1);
        result.add(topRightControl1);
        result.add(topRightControl2);
        result.add(topRightPoint2);
        
        result.add(bottomRightPoint1);
        result.add(bottomRightControl1);
        result.add(bottomRightControl2);
        result.add(bottomRightPoint2);
        
        result.add(bottomLeftPoint1);
        result.add(bottomLeftControl1);
        result.add(bottomLeftControl2);
        result.add(bottomLeftPoint2);
        
        return result;
    }
    
    /**
     * Create the rounded rectangle configurations using the rounded points and rectangle points
     */
    private List<SegmentConfiguration> calculateRoundedSplines(List<Point> roundedPoints){
        
        List<SegmentConfiguration> result = new ArrayList();
        
        result.add(new SegmentConfiguration(roundedPoints.get(TOP_LEFT_POINT_2), roundedPoints.get(TOP_RIGHT_POINT_1)));
        result.add(new CubicCurveConfiguration(roundedPoints.get(TOP_RIGHT_POINT_1), roundedPoints.get(TOP_RIGHT_POINT_2),
                roundedPoints.get(TOP_RIGHT_CONTROL_POINT_1), roundedPoints.get(TOP_RIGHT_CONTROL_POINT_2)));
        
        result.add(new SegmentConfiguration(roundedPoints.get(TOP_RIGHT_POINT_2), roundedPoints.get(BOTTOM_RIGHT_POINT_1)));
        result.add(new CubicCurveConfiguration(roundedPoints.get(BOTTOM_RIGHT_POINT_1), roundedPoints.get(BOTTOM_RIGHT_POINT_2),
                roundedPoints.get(BOTTOM_RIGHT_CONTROL_POINT_1), roundedPoints.get(BOTTOM_RIGHT_CONTROL_POINT_2)));
        
        result.add(new SegmentConfiguration(roundedPoints.get(BOTTOM_RIGHT_POINT_2), roundedPoints.get(BOTTOM_LEFT_POINT_1)));
        result.add(new CubicCurveConfiguration(roundedPoints.get(BOTTOM_LEFT_POINT_1), roundedPoints.get(BOTTOM_LEFT_POINT_2),
                roundedPoints.get(BOTTOM_LEFT_CONTROL_POINT_1), roundedPoints.get(BOTTOM_LEFT_CONTROL_POINT_2)));
        
        result.add(new SegmentConfiguration(roundedPoints.get(BOTTOM_LEFT_POINT_1), roundedPoints.get(TOP_LEFT_POINT_1)));
        result.add(new CubicCurveConfiguration(roundedPoints.get(TOP_LEFT_POINT_1), roundedPoints.get(TOP_LEFT_POINT_2),
                roundedPoints.get(TOP_LEFT_CONTROL_POINT_1), roundedPoints.get(TOP_LEFT_CONTROL_POINT_2)));
        
        return result;
    }
    
    /**
     * Create an rounded rectangle Area from the rounded rectangle points and rectangle points.
     * It is used to build the shadows
     */
    private Area convertRoundedRectangleToArea(List<Point> roundedPoints){
        Area area = null;
        ExtendedGeneralPath curvePath = new ExtendedGeneralPath(new GeneralPath());
        
        curvePath.moveTo(roundedPoints.get(TOP_LEFT_POINT_2).getX(), roundedPoints.get(TOP_LEFT_POINT_2).getY());
        curvePath.lineTo(roundedPoints.get(TOP_RIGHT_POINT_1).getX(), roundedPoints.get(TOP_RIGHT_POINT_1).getY());
        curvePath.curveTo(roundedPoints.get(TOP_RIGHT_CONTROL_POINT_1).getX(), roundedPoints.get(TOP_RIGHT_CONTROL_POINT_1).getY(),
                roundedPoints.get(TOP_RIGHT_CONTROL_POINT_2).getX(), roundedPoints.get(TOP_RIGHT_CONTROL_POINT_2).getY(),
                roundedPoints.get(TOP_RIGHT_POINT_2).getX(), roundedPoints.get(TOP_RIGHT_POINT_2).getY());
        
        curvePath.lineTo(roundedPoints.get(BOTTOM_RIGHT_POINT_1).getX(), roundedPoints.get(BOTTOM_RIGHT_POINT_1).getY());
        curvePath.curveTo(roundedPoints.get(BOTTOM_RIGHT_CONTROL_POINT_1).getX(), roundedPoints.get(BOTTOM_RIGHT_CONTROL_POINT_1).getY(),
                roundedPoints.get(BOTTOM_RIGHT_CONTROL_POINT_2).getX(), roundedPoints.get(BOTTOM_RIGHT_CONTROL_POINT_2).getY(),
                roundedPoints.get(BOTTOM_RIGHT_POINT_2).getX(), roundedPoints.get(BOTTOM_RIGHT_POINT_2).getY());
        
        curvePath.lineTo(roundedPoints.get(BOTTOM_LEFT_POINT_1).getX(), roundedPoints.get(BOTTOM_LEFT_POINT_1).getY());
        curvePath.curveTo(roundedPoints.get(BOTTOM_LEFT_CONTROL_POINT_1).getX(), roundedPoints.get(BOTTOM_LEFT_CONTROL_POINT_1).getY(),
                roundedPoints.get(BOTTOM_LEFT_CONTROL_POINT_2).getX(), roundedPoints.get(BOTTOM_LEFT_CONTROL_POINT_2).getY(),
                roundedPoints.get(BOTTOM_LEFT_POINT_2).getX(), roundedPoints.get(BOTTOM_LEFT_POINT_2).getY());
        
        curvePath.lineTo(roundedPoints.get(TOP_LEFT_POINT_1).getX(), roundedPoints.get(TOP_LEFT_POINT_1).getY());
        curvePath.curveTo(roundedPoints.get(TOP_LEFT_CONTROL_POINT_1).getX(), roundedPoints.get(TOP_LEFT_CONTROL_POINT_1).getY(),
                roundedPoints.get(TOP_LEFT_CONTROL_POINT_2).getX(), roundedPoints.get(TOP_LEFT_CONTROL_POINT_2).getY(),
                roundedPoints.get(TOP_LEFT_POINT_2).getX(), roundedPoints.get(TOP_LEFT_POINT_2).getY());
        
        area = new Area(curvePath);
        
        return area;
    }
}
