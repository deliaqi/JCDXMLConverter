package translator.utils;

public class GeometricPoint {
    
    private double x;
    private double y;
    
    private boolean omit = false;
    
    public GeometricPoint(double x, double y) {
        this(x, y, false);
    }
    
    public GeometricPoint(double x, double y, boolean omit) {
        this.x = x;
        this.y = y;
        this.omit = omit;
    }
    
    public GeometricPoint add(GeometricPoint point) {
        return new GeometricPoint(x + point.getX(), y + point.getY());
    }
    
    public GeometricPoint subtract(GeometricPoint point) {
        return new GeometricPoint(x - point.getX(), y - point.getY());
    }
    
    public GeometricPoint byScalar(double k) {
        return new GeometricPoint(x * k, y * k);
    }
    
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isOmit() {
        return omit;
    }

    public void setOmit(boolean omit) {
        this.omit = omit;
    }
    
}
