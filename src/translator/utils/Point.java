package translator.utils;

public class Point {
    
    public static final int POINT_DEFAULT_VALUE = 0;
    
    private double x;
    private double y;
    private double z;
    
    private boolean omit = false;
    
    public Point(){
        this(0, 0, 0, false);
    }
    
    public Point(double x, double y) {
        this(x, y, 0, false);
    }
    
    public Point(double x, double y, double z){
        this(x, y, z, false);
    }
    
    public Point(double x, double y, boolean omit) {
        this(x, y, 0, omit);
    }
    
    public Point(Point point){
        this(point.getX(), point.getY(), point.getZ(), point.isOmit());
    }
    
    public Point(String coords) {
        this(coords, false);
    }
    
    public Point(double x, double y, double z, boolean omit){
        this.x = x;
        this.y = y;
        this.z = z;
        this.omit = omit;
    }
    
    public Point(String coords, boolean omit) {
        String[] parts = coords.split(" ");
        
        this.x = Double.parseDouble(parts[0]);
        this.y = Double.parseDouble(parts[1]);
        
        if (parts.length == 3) {
            this.z = Double.parseDouble(parts[2]);
        }
        
        this.omit = omit;
    }
    
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
    
    public Point add(Point point) {
        return new Point(x + point.getX(), y + point.getY(), z + point.getZ());
    }
    
    public Point subtract(Point point) {
        return new Point(x - point.getX(), y - point.getY(), z - point.getZ());
    }
    
    public Point byScalar(double k) {
        return new Point(x * k, y * k, z * k);
    }
    
    public double dotProduct(Point point){
        return x * point.getX() + y * point.getY() + z * point.getZ();
    }
    
    public Point offset(Vector vector){
        return this.add(vector);
    }
    
    public Point scaleTo(double newLength){
        Point result = new Point();
        
        double length = GeometricOperations.distance(this);
        
        if (length > Double.MIN_VALUE){
            result = byScalar(newLength / length);
        }
        
        return result;
    }
    
    public boolean equals(Object object){
        boolean result;
        if(object instanceof Point){
            Point point = (Point)object;
            
            if(Double.compare(getX(), point.getX()) == 0 &&
                    Double.compare(getY(), point.getY()) == 0 &&
                    Double.compare(getZ(), point.getZ()) == 0){
                result = true;
            }
            else {
                result = false;
            }
        }
        else {
            result = false;
        }
        
        return result;
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

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
    
}
