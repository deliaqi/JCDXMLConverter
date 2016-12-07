
package translator.processors.cdxml;

import java.awt.geom.AffineTransform;
import translator.utils.Point;

public class Matrix3D {
    
    private double[][] matrix;
    
    public Matrix3D() {
        matrix = new double[4][4];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                matrix[i][j] = (i == j) ? 1 : 0;
            }
        }
    }
    
    public double[][] getMatrixValues() {
        return matrix;
    }
    
    public Point transform(Point originalPoint) {
        Point result = new Point();
        
        result.setX(originalPoint.getX() * matrix[0][0] + originalPoint.getY() * matrix[1][0] + originalPoint.getZ() * matrix[2][0] + matrix[3][0]);
        result.setY(originalPoint.getX() * matrix[0][1] + originalPoint.getY() * matrix[1][1] + originalPoint.getZ() * matrix[2][1] + matrix[3][1]);
        result.setZ(originalPoint.getX() * matrix[0][2] + originalPoint.getY() * matrix[1][2] + originalPoint.getZ() * matrix[2][2] + matrix[3][2]);
        
        return result;
    }
    
    public void translate(Point translateToPoint) {
        translate(translateToPoint.getX(), translateToPoint.getY(), translateToPoint.getZ());
    }
    
    public void translate(double x, double y, double z) {
        matrix[3][0] += x;
        matrix[3][1] += y;
        matrix[3][2] += z;
    }
    
    public void rotateX(double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        
        Matrix3D resultMatrix = new Matrix3D();
        double[][] resultMatrixValues = resultMatrix.getMatrixValues();
        
        resultMatrixValues[1][1] = cosine;
        resultMatrixValues[1][2] = sine;
        resultMatrixValues[2][1] = -sine;
        resultMatrixValues[2][2] = cosine;
        
        this.multiplyBy(resultMatrix);
    }
    
    public void rotateY(double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        
        Matrix3D resultMatrix = new Matrix3D();
        double[][] resultMatrixValues = resultMatrix.getMatrixValues();
        
        resultMatrixValues[0][0] = cosine;
        resultMatrixValues[0][2] = -sine;
        resultMatrixValues[2][0] = sine;
        resultMatrixValues[2][2] = cosine;
        
        this.multiplyBy(resultMatrix);
    }
    
    public void rotateZ(double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        
        Matrix3D resultMatrix = new Matrix3D();
        double[][] resultMatrixValues = resultMatrix.getMatrixValues();
        
        resultMatrixValues[0][0] = cosine;
        resultMatrixValues[0][1] = sine;
        resultMatrixValues[1][0] = -sine;
        resultMatrixValues[1][1] = cosine;
        
        this.multiplyBy(resultMatrix);
    }
    
    public void skewX(double angleY, double angleZ) {
        Matrix3D resultMatrix = new Matrix3D();
        double[][] resultMatrixValues = resultMatrix.getMatrixValues();
        
        resultMatrixValues[0][1] = Math.tan(angleY);
        resultMatrixValues[0][2] = Math.tan(angleZ);
        
        this.multiplyBy(resultMatrix);
    }
    
    public void skewY(double angleX, double angleZ) {
        Matrix3D resultMatrix = new Matrix3D();
        double[][] resultMatrixValues = resultMatrix.getMatrixValues();
        
        resultMatrixValues[1][0] = Math.tan(angleX);
        resultMatrixValues[1][2] = Math.tan(angleZ);
        
        this.multiplyBy(resultMatrix);
    }
    
    public void skewZ(double angleX, double angleY) {
        Matrix3D resultMatrix = new Matrix3D();
        double[][] resultMatrixValues = resultMatrix.getMatrixValues();
        
        resultMatrixValues[2][0] = Math.tan(angleX);
        resultMatrixValues[2][1] = Math.tan(angleY);
        
        this.multiplyBy(resultMatrix);
    }
    
    public void scale(double scale){
        scaleXYZ(scale, scale, scale);
    }
    
    public void scaleXYZ(double xScale, double yScale, double zScale) {
        matrix[0][0] *= xScale;
        matrix[1][0] *= xScale;
        matrix[2][0] *= xScale;
        matrix[3][0] *= xScale;
        matrix[0][1] *= yScale;
        matrix[1][1] *= yScale;
        matrix[2][1] *= yScale;
        matrix[3][1] *= yScale;
        matrix[0][2] *= zScale;
        matrix[1][2] *= zScale;
        matrix[2][2] *= zScale;
        matrix[3][2] *= zScale;
    }
    
    public Matrix3D getInverseMatrix() {
        Matrix3D m = new Matrix3D();
        
        double det = determinant();
        
        if (det != 0.0) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++)
                    m.matrix[i][j] = determinantAdjointIJ(j, i) / det;
            }
        }
        
        return m;
    }
    
    public double determinant() {
        double result =
                matrix[0][3] * matrix[1][2] * matrix[2][1] * matrix[3][0] -
                matrix[0][2] * matrix[1][3] * matrix[2][1] * matrix[3][0] -
                matrix[0][3] * matrix[1][1] * matrix[2][2] * matrix[3][0] +
                matrix[0][1] * matrix[1][3] * matrix[2][2] * matrix[3][0] +
                matrix[0][2] * matrix[1][1] * matrix[2][3] * matrix[3][0] -
                matrix[0][1] * matrix[1][2] * matrix[2][3] * matrix[3][0] -
                matrix[0][3] * matrix[1][2] * matrix[2][0] * matrix[3][1] +
                matrix[0][2] * matrix[1][3] * matrix[2][0] * matrix[3][1] +
                matrix[0][3] * matrix[1][0] * matrix[2][2] * matrix[3][1] -
                matrix[0][0] * matrix[1][3] * matrix[2][2] * matrix[3][1] -
                matrix[0][2] * matrix[1][0] * matrix[2][3] * matrix[3][1] +
                matrix[0][0] * matrix[1][2] * matrix[2][3] * matrix[3][1] +
                matrix[0][3] * matrix[1][1] * matrix[2][0] * matrix[3][2] -
                matrix[0][1] * matrix[1][3] * matrix[2][0] * matrix[3][2] -
                matrix[0][3] * matrix[1][0] * matrix[2][1] * matrix[3][2] +
                matrix[0][0] * matrix[1][3] * matrix[2][1] * matrix[3][2] +
                matrix[0][1] * matrix[1][0] * matrix[2][3] * matrix[3][2] -
                matrix[0][0] * matrix[1][1] * matrix[2][3] * matrix[3][2] -
                matrix[0][2] * matrix[1][1] * matrix[2][0] * matrix[3][3] +
                matrix[0][1] * matrix[1][2] * matrix[2][0] * matrix[3][3] +
                matrix[0][2] * matrix[1][0] * matrix[2][1] * matrix[3][3] -
                matrix[0][0] * matrix[1][2] * matrix[2][1] * matrix[3][3] -
                matrix[0][1] * matrix[1][0] * matrix[2][2] * matrix[3][3] +
                matrix[0][0] * matrix[1][1] * matrix[2][2] * matrix[3][3];
        
        return result;
    }
    
    // Get determinant of a 3*3 squared matrix
    public double determinant3x3(double matrix3x3[][]) {
        double result =
                -matrix3x3[0][2] * matrix3x3[1][1] * matrix3x3[2][0] +
                matrix3x3[0][1] * matrix3x3[1][2] * matrix3x3[2][0] +
                matrix3x3[0][2] * matrix3x3[1][0] * matrix3x3[2][1] -
                matrix3x3[0][0] * matrix3x3[1][2] * matrix3x3[2][1] -
                matrix3x3[0][1] * matrix3x3[1][0] * matrix3x3[2][2] +
                matrix3x3[0][0] * matrix3x3[1][1] * matrix3x3[2][2];
        
        return result;
    }
    
    
    // Get the (i, j) element of adjoint of matrix A
    public double determinantAdjointIJ(int elementI, int elementJ) {
        double[][] adjointMatrix = new double[3][3];
        
        for (int i = 0; i < 3; i++) {
            int mi = (i >= elementI) ? i + 1 : i;
            
            for (int j = 0; j < 3; j++) {
                int mj = (j >= elementJ) ? j + 1 : j;
                
                adjointMatrix[i][j] = matrix[mi][mj];
            }
        }
        
        int sign = (elementI + elementJ) % 2 == 0 ? 1 : -1;	// using pow is ambiguous on UNIX
        return determinant3x3(adjointMatrix) * sign;
    }
    
    public void multiplyBy(Matrix3D m) {
        double[][] matrixValues = new double[4][4];
        double[][] otherMatrixValues = m.getMatrixValues();
        double s;
        
        copyMatrixValues(matrixValues);
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                s = 0;
                
                for (int c = 0; c < 4; c++) {
                    s += matrixValues[i][c] * otherMatrixValues[c][j];
                }
                
                matrix[i][j] = s;
            }
        }
    }
    
    public void copyMatrixValues(double[][] destination) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                destination[i][j] = matrix[i][j];
            }
        }
    }
    
    public boolean isIdentity() {
        boolean result = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j && matrix[i][j] != 0) {
                    result = false;
                    break;
                } else if (i == j && matrix[i][j] != 1) {
                    result = false;
                    break;
                }
            }
            if (!result) {
                break;
            }
        }
        return result;
    }
}
