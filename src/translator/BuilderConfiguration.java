package translator;

import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.FillRule;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.ShapeRendering;
import translator.graphics.shapes.gradients.Gradient;

public abstract class BuilderConfiguration {
    
    public static final String SEGMENT_BUILDER_ID = "translator.graphics.shapes.builders.SegmentBuilder";
    public static final String CURVE_BUILDER_ID = "translator.graphics.shapes.builders.CurveBuilder";
    public static final String CUBIC_CURVE_BUILDER_ID = "translator.graphics.shapes.builders.CubicCurveBuilder";
    public static final String QUADRATIC_CURVE_BUILDER_ID = "translator.graphics.shapes.builders.QuadraticCurveBuilder";
    public static final String ARC_BUILDER_ID = "translator.graphics.shapes.builders.ArcBuilder";
    public static final String COMPOSITE_SHAPE_BUILDER_ID = "translator.graphics.shapes.builders.CompositeShape";
    public static final String CIRCLE_BUILDER_ID = "translator.graphics.shapes.builders.CircleBuilder";
    public static final String ELLIPSE_BUILDER_ID = "translator.graphics.shapes.builders.EllipseBuilder";
    public static final String GROUP_BUILDER_ID = "translator.graphics.shapes.builders.GroupBuilder";
    public static final String RECTANGLE_BUILDER_ID = "translator.graphics.shapes.builders.RectangleBuilder";
    public static final String SPLINE_BUILDER_ID = "translator.graphics.shapes.builders.SplineBuilder";
    public static final String POLYGON_BUILDER_ID = "translator.graphics.shapes.builders.PolygonBuilder";
    public static final String TEXT_BUILDER_ID = "translator.graphics.shapes.builders.TextBuilder";
    public static final String IMAGE_BUILDER_ID = "translator.graphics.shapes.builders.ImageBuilder";
    
    private String id;
    private String builderId;
    private String strokeStyle;
    private double strokeWidth;
    private Color color;
    private Color fillColor;
    private boolean shaded;
    private Gradient gradient;
    private boolean fill;
    private int zOrder;
    private FillRule fillRule;
    private LineCap lineCap;
    private boolean dashed;
    private double dashLength;
    private double angle;
    private ShapeRendering shapeRendering;
    private boolean display;
    private double miterLimit;
    
    public BuilderConfiguration(String builderId) {
        this.builderId = builderId;
        this.lineCap = LineCap.Butt;
        this.display = true;
    }

    public String getBuilderId() {
        return builderId;
    }

    public void setBuilderId(String builderId) {
        this.builderId = builderId;
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

    public void setStrokeStyle(String strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    public FillRule getFillRule() {
        return fillRule;
    }

    public void setFillRule(FillRule fillRule) {
        this.fillRule = fillRule;
    }
    
    public LineCap getLineCap() {
        return lineCap;
    }
    
    public void setLineCap(LineCap lineCap) {
        this.lineCap = lineCap;
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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
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

    public boolean isDisplay() {
        return display;
    }
    
    public void setDisplay(boolean display) {
        this.display = display;
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

}
