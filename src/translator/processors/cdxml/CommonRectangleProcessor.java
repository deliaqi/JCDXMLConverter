package translator.processors.cdxml;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.graphics.Color;
import translator.cdxml.CDXMLEnvironment;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public abstract class CommonRectangleProcessor extends DrawingElementProcessor{
    private List<Point> rectanglePoints = new ArrayList();
    private String rectangleType;
    
    public CommonRectangleProcessor(){
    }
    
    protected void cleanup() {
        rectanglePoints = null;
        rectangleType = null;
        super.cleanup();
    }
    
    protected void configure(){
        super.configure();
        ParsedElement rectangle = getElement();        
        
        if(rectangle.hasAttribute(ParseElementDefinition.GRAPHIC_LINE_WIDTH)){
            setLineWidth(Double.parseDouble(rectangle.getAttribute(ParseElementDefinition.GRAPHIC_LINE_WIDTH)));
        }else{
            setLineWidth(getEnvironment().getLineWidth());
        }
        if(rectangle.hasAttribute(ParseElementDefinition.GRAPHIC_BOLD_WIDTH)){
            setBoldWidth(Double.parseDouble(rectangle.getAttribute(ParseElementDefinition.GRAPHIC_BOLD_WIDTH)));
        }else{
            setBoldWidth(getEnvironment().getBoldWidth());
        }

        if(rectangle.hasAttribute(ParseElementDefinition.HASH_SPACING)){
            setHashSpacing(Double.parseDouble(rectangle.getAttribute(ParseElementDefinition.HASH_SPACING)));
        }else{
            setHashSpacing(getEnvironment().getHashSpacing());
        }
        
        setRectanglePoints(calculateRectanglePoints(center, minorAxisEnd, majorAxisEnd));
    }
    
    protected String getTypeString() {
        if(rectangleType == null){
            setRectangleType(getElement().getAttribute(ParseElementDefinition.GRAPHIC_RECTANGLE_TYPE));
        }
        
        return rectangleType;
    }
    
    protected List<Point> calculateRectanglePoints(Point center, Point minorAxis, Point majorAxis){
        List<Point> result = new ArrayList();        
        double width = GeometricOperations.distance(majorAxis, center) * 2;
        double heigth = GeometricOperations.distance(minorAxis, center) * 2;
        double minorAngle = GeometricOperations.angle(minorAxis.getX(), minorAxis.getY(), center.getX(), center.getY());
        double majorAngle = GeometricOperations.angle(majorAxis.getX(), majorAxis.getY(), center.getX(), center.getY());        
        Point bottomLeft = GeometricOperations.offset(minorAxis, majorAngle, width/2);
        Point bottomRight = GeometricOperations.offset(minorAxis, majorAngle, -width/2);
        Point topRight = GeometricOperations.offset(majorAxis, minorAngle, heigth/2);
        Point topLeft = GeometricOperations.offset(bottomLeft, minorAngle, heigth);        
        result.add(topLeft);
        result.add(topRight);
        result.add(bottomRight);
        result.add(bottomLeft);        
        return result;
    }
    
    
    /**
     * This method is used for obtaining the shadow points list applying
     * an offset to each point of the original rectangle.
     * @param points 
     * @param center 
     * @param majorAxisEnd 
     * @param minorAxisEnd 
     */
    protected List<Point> getShadowRectangle(List<Point> points, Point center, Point majorAxisEnd, Point minorAxisEnd){
        
        List<Point> result = new ArrayList();       
        
        //This calculation is for determine the offset of the shadow.
        double offset = Math.hypot(getShadowSize(), getShadowSize()) * getLineWidth();
        
        // This calculations are for determine the direction of the shadow ussing 
        // the center point, and the axis end points.
        double majorAxisAngle = GeometricOperations.angle(center, majorAxisEnd);        
        double minorAxisLengh = GeometricOperations.distance(center, minorAxisEnd);        
        Point centerAxisEnd = GeometricOperations.offset(minorAxisEnd, majorAxisAngle, minorAxisLengh);
        double shadowAngle = GeometricOperations.angle(center, centerAxisEnd);       
        
        for(int i=0; i<points.size(); i++){
                    
            result.add(GeometricOperations.offset(points.get(i), shadowAngle, offset));
        }        
        return result;
    }
    
    protected Area convertRectangleToArea(List<Point> points){
        Area result = null;
        ExtendedGeneralPath curvePath = new ExtendedGeneralPath(new GeneralPath());
        curvePath.moveTo(points.get(0).getX(), points.get(0).getY());
        for(int i=1; i< points.size(); i++){
            curvePath.lineTo(points.get(i).getX(), points.get(i).getY());
        }
        result = new Area(curvePath);        
        return result;
    }
    
    public List<Point> getRectanglePoints() {
        return rectanglePoints;
    }
    
    public void setRectanglePoints(List<Point> rectanglePoints) {
        this.rectanglePoints = rectanglePoints;
    }
    
    public String getRectangleType() {
        return rectangleType;
    }
    
    public void setRectangleType(String rectangleType) {
        this.rectangleType = rectangleType;
    }
}
