package translator.utils;

public class GeometricLine extends Line {
    
    private double beginWidth;
    private double endWidth;
    
    private Point leftBegin;
    private Point leftEnd;
    
    private Point rightBegin;
    private Point rightEnd;
    
    public GeometricLine(String id, Point begin, Point end, double width) {
        this(id, begin, end, width, width);
    }
    
    public GeometricLine(String id, Point begin, Point end, double beginWidth, double endWidth) {
        super(id, begin, end);
        
        this.beginWidth = beginWidth;
        this.endWidth = endWidth;
        
        calculate();
    }
    
    private void calculate(){       
        leftBegin = GeometricOperations.offset(getBegin(), getAngle() + (Math.PI / 2), getBeginWidth() / 2);
        leftEnd = GeometricOperations.offset(getEnd(), getAngle() + (Math.PI / 2), getEndWidth() / 2);
        
        rightBegin = GeometricOperations.offset(getBegin(), getAngle() - (Math.PI / 2), getBeginWidth() / 2);
        rightEnd = GeometricOperations.offset(getEnd(), getAngle() - (Math.PI / 2), getEndWidth() / 2);
    }

    public Point getLeftBegin() {
        return leftBegin;
    }

    public Point getLeftEnd() {
        return leftEnd;
    }

    public Point getRightBegin() {
        return rightBegin;
    }

    public Point getRightEnd() {
        return rightEnd;
    }

    public double getBeginWidth() {
        return beginWidth;
    }

    public double getEndWidth() {
        return endWidth;
    }
}
