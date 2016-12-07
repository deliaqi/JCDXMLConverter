package translator.graphics.shapes;

import java.util.ArrayList;
import java.util.List;
import translator.cddom.Layer;
import translator.cddom.LayerContent;
import translator.cddom.properties.BoundingBox;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.ShapeRendering;
import translator.graphics.shapes.gradients.Gradient;
import translator.utils.Point;

public abstract class Shape implements LayerContent {
    
    // Uniquely identifies the shape
    private String id;
    
    private Layer layer;
    private static final int EQUAL = 1;
    private String strokeStyle;
    private double strokeWidth;
    private Color color;
    private Color fillColor;
    private boolean fill;
    private int zOrder;
    private boolean dashed;
    private double dashLength;
    private boolean shaded;
    private Gradient gradient;
    private LineCap lineCap;
    private ShapeRendering shapeRendering;
    private double miterLimit;
      
    public List<Point> points = new ArrayList();
    private boolean display;
    
    public Shape() {
        this.display = true;
    }
    
    protected List<Point> getPoints(){
        return points;
    }
    
    protected void addPoint(Point point){
        points.add(point);
    }
    
    protected Point getPoint(int index){
        return points.get(index);
    }
    
    protected void addPoint(Point point, int index){
        points.add(index, point);
    }

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }
    
    public double getStrokeWidth() {
        return strokeWidth;
    }
    
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    public String getStrokeStyle() {
        return strokeStyle;
    }

    public void setStrokeStyle(String style) {
        this.strokeStyle = style;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }
    
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }    
    
    public int compareTo(Object shape) {
        int result = Integer.MAX_VALUE;
        
        if(shape instanceof LayerContent){
            if((((LayerContent)shape).getZOrder() - this.getZOrder()) == 0){
                //the value of the EQUAL constant differs from the C# code line because
                //each language implements the sorting algorithm in different ways
                result = EQUAL;
            }else{
                result = this.getZOrder() - ((LayerContent)shape).getZOrder();
            }            
        }
        
        return result;
    }

    public boolean isDashed() {
        return dashed;
    }

    public void setDashed(boolean dashed) {
        this.dashed = dashed;
    }

    public double getDashLength() {
        return dashLength;
    }

    public void setDashLength(double dashLength) {
        this.dashLength = dashLength;
    }

    public LineCap getLineCap() {
        return lineCap;
    }

    public void setLineCap(LineCap lineCap) {
        this.lineCap = lineCap;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public boolean isShaded() {
        return shaded;
    }

    public void setShaded(boolean shaded) {
        this.shaded = shaded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ShapeRendering getShapeRendering(){
        return shapeRendering;
    }
    
    public void setShapeRendering( ShapeRendering shapeRendering){
        this.shapeRendering = shapeRendering;
    }

    public Gradient getGradient() {
        return gradient;
    }

    public void setGradient(Gradient gradient) {
        this.gradient = gradient;
    }

    public double getMiterLimit() {
        return miterLimit;
    }

    public void setMiterLimit(double miterLimit) {
        this.miterLimit = miterLimit;
    }
     
    public BoundingBox getBoundingBox(){
        
        boolean firstPoint = true;
        
        double nearestX = 0.0;
        double nearestY = 0.0;
        
        double farthermostX = 0.0;
        double farthermostY = 0.0;
        
        for(Point currentPoint : points){
            
            if(firstPoint ){
                
                nearestX = currentPoint.getX();
                nearestY = currentPoint.getY();
                farthermostX = currentPoint.getX();
                farthermostY = currentPoint.getY();
                firstPoint = false;
                
            }
            
            if(currentPoint.getX() < nearestX ){                
                
                nearestX = currentPoint.getX();                
                
            }else if(currentPoint.getX() > nearestX ){
                
                farthermostX = currentPoint.getX();
                
            }
            
            if(currentPoint.getY() < nearestY ){                
                
                nearestY = currentPoint.getY();                
                
            }else if(currentPoint.getY() > nearestY ){
                
                farthermostY = currentPoint.getY();
                
            }
            
        }
    
        //Calculate the resulting BoundingBox.
        return new BoundingBox(new Point(nearestX-getStrokeWidth(), nearestY-getStrokeWidth()),
                farthermostX-nearestX+getStrokeWidth()*2,                
                farthermostY-nearestY+getStrokeWidth()*2);   
        
    }
    
}
