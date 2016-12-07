
package translator.processors.cdxml;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import translator.utils.Point;

/**
 * Wrapper that encapsulates the functionality of an <code>AffineTransform</code>
 * to expose an API similar to <code>Matrix3D</code>.
 */
public class Matrix2D {
    
    private AffineTransform transform;
    
    public Matrix2D() {
        this(new AffineTransform());
    }
    
    private Matrix2D(AffineTransform transform) {
        this.transform = transform;
    }
    
    /**
     * Returns the internal matrix elements in an array of 6 doubles
     */
    public double[] getMatrixValues() {
        double[] result = new double[6];
        transform.getMatrix(result);
        return result;
    }
    
    /**
     * Converts a translator.utils.Point to a java.awt.Point2D to adapt it for the
     * AffineTransform operations.
     */
    private static Point2D.Double getPoint2D(Point point) {
        return new Point2D.Double(point.getX(), point.getY());
    }
    
    /**
     * Transforms the given point using the matrix.
     */
    public Point transform(Point originalPoint) {
        Point result = new Point();
        
        // convert to Point2D
        Point2D.Double originalPoint2D = getPoint2D(originalPoint);
        
        // perform transformation
        transform.transform(originalPoint2D, originalPoint2D);
        
        // put results into a translator.utils.Point
        result.setX(originalPoint2D.getX());
        result.setY(originalPoint2D.getY());
        
        return result;
    }
    
    public void translate(Point translateToPoint) {
        // Taken from C++
        translate(translateToPoint.getX(), translateToPoint.getY());
    }
    
    public void translate(double x, double y) {
        // Taken from C++
        double[] matrix = getMatrixValues();
        matrix[4] += x;
        matrix[5] += y;
        transform.setTransform(matrix[0], matrix[1],
                matrix[2], matrix[3], matrix[4], matrix[5]);
    }
    
    public void rotate(double angle) {
        // Taken from C++
        double[] matrix = getMatrixValues();
        
        double cosAngle = Math.cos(angle);
	double sinAngle = Math.sin(angle);
	double m0, m1;
        
        m0 = cosAngle * matrix[0] - sinAngle * matrix[1];
	m1 = cosAngle * matrix[1] + sinAngle * matrix[0];
	matrix[0] = m0;
	matrix[1] = m1;
        
        m0 = cosAngle * matrix[2] - sinAngle * matrix[3];
	m1 = cosAngle * matrix[3] + sinAngle * matrix[2];
	matrix[2] = m0;
	matrix[3] = m1;

	m0 = cosAngle * matrix[4] - sinAngle * matrix[5];
	m1 = cosAngle * matrix[5] + sinAngle * matrix[4];
	matrix[4] = m0;
	matrix[5] = m1;
        
        transform.setTransform(matrix[0], matrix[1],
                matrix[2], matrix[3], matrix[4], matrix[5]);
    }
    
    public void skewXY(double angleX, double angleY) {
        // Taken from C++
        double tanAngleX = Math.tan(angleX);
        double tanAngleY = Math.tan(angleY);
        
        double[] matrix = getMatrixValues();
        
        double m1 = tanAngleX * matrix[0] + matrix[1];
	double m3 = tanAngleX * matrix[2] + matrix[3];
	double m0 = tanAngleY * matrix[1] + matrix[0];
	double m2 = tanAngleY * matrix[3] + matrix[2];
        
        matrix[0] = m0;
	matrix[1] = m1;
	matrix[2] = m2;
	matrix[3] = m3;
        
        transform.setTransform(matrix[0], matrix[1],
                matrix[2], matrix[3], matrix[4], matrix[5]);
    }
    
    public void scale(double scale){
        // Taken from C++
        scaleXY(scale, scale);
    }
    
    public void scaleXY(double xScale, double yScale) {
        // Taken from C++
        double[] matrix = getMatrixValues();
        for (int i = 0; i < matrix.length; i++) {
            // 0, 2 and 4 are multiplied by xScale
            // 1, 3 and 5 are multiplied by yScale
            if (i % 2 == 0) {
                matrix[i] *= xScale;
            } else {
                matrix[i] *= yScale;
            }
        }
        transform.setTransform(matrix[0], matrix[1],
                matrix[2], matrix[3], matrix[4], matrix[5]);
    }
    
    /**
     * Returns the inverse matrix
     */
    public Matrix2D getInverseMatrix() {
        Matrix2D result;
        try {
            // delegate to AffineTransform
            result = new Matrix2D(transform.createInverse());
        } catch (NoninvertibleTransformException ex) {
            result = null;
        }
        return result;
    }
    
    /**
     * Returns the matrix's determinant
     */
    public double determinant() {
        // Taken from C++
        return transform.getDeterminant();
    }
    
    /**
     * Returns whether this transformation is the identity
     */
    public boolean isIdentity() {
        // Taken from C++
        return transform.isIdentity();
    }
}
