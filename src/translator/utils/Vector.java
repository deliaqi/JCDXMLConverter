package translator.utils;

/**
 * This class uses the same operation of the Point class, 
 * but it's a way to differentiate the standard operation with Point
 * from the operations like adding a vector to a point
 */
public class Vector extends Point{
        
    public Vector(){
        this(0, 0);
    }
    
    public Vector(double x, double y) {
        this(x, y, false);
    }
    
    public Vector(double x, double y, double z){
        this(x, y, z, false);
    }
    
    public Vector(double x, double y, double z, boolean omit) {
        super(x, y, z, omit);
    }
    
    public Vector(double x, double y, boolean omit) {
        super(x, y, 0, omit);
    }
    
    public Vector(Point point){
        super(point.getX(), point.getY(), point.getZ(), point.isOmit());
    }
    
    public Vector(Vector vector){
        super(vector.getX(), vector.getY(), vector.getZ(), vector.isOmit());
    }
    
    public Vector(String coords) {
        super(coords, false);
    }
    
    public Vector scaleTo(double newLength){
        return new Vector(super.scaleTo(newLength));
    }
    
    public Vector byScalar(double k) {
        return new Vector(super.byScalar(k));
    }
    
    /**
     * Calculate the normal vector for the current vector
     */
    public Vector getNormal(){
        return new Vector(-getY(), getX());
    }
    
    /**
     * Calculate the module of the vector
     */
    public double getLength(){
        return GeometricOperations.distance(getX(), getY(), getZ());
    }
    
    /**
     * Get the versor of a vector
     */
    public Vector normalize(){
        double distance = this.getLength();
        Vector result;
        if (distance != 0) {
            result = this.byScalar(1 / distance);
        } else {
            // avoid dividing by zero
            result = new Vector(0, 0);
        }
        return result;
    }
}
