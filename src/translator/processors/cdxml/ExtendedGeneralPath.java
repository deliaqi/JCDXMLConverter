package translator.processors.cdxml;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ExtendedGeneralPath implements Shape {
    
    private GeneralPath path;
    
    public ExtendedGeneralPath(GeneralPath path) {
        this.path = path;
    }

    public Rectangle getBounds() {
        return path.getBounds();
    }

    public Rectangle2D getBounds2D() {
        return path.getBounds2D();
    }

    public boolean contains(double x, double y) {
        return path.contains(x, y);
    }

    public boolean contains(Point2D p) {
        return path.contains(p);
    }

    public boolean intersects(double x, double y, double w, double h) {
        return path.intersects(x, y, w, h);
    }

    public boolean intersects(Rectangle2D r) {
        return intersects(r);
    }

    public boolean contains(double x, double y, double w, double h) {
        return contains(x, y, w, h);
    }

    public boolean contains(Rectangle2D r) {
        return contains(r);
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return path.getPathIterator(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return path.getPathIterator(at, flatness);
    }

    public void moveTo(double d, double d0) {
        path.moveTo((float)d, (float)d0);
    }

    public void lineTo(double d, double d0) {
        path.lineTo((float)d, (float)d0);
    }

    public void quadTo(double d, double d0, double d1, double d2) {
        path.quadTo((float)d, (float)d0, (float)d1, (float)d2);
    }

    public void curveTo(double d, double d0, double d1, double d2, double d3, double d4) {
        path.curveTo((float)d, (float)d0, (float)d1, (float)d2, (float)d3, (float)d4);
    }

    public void closePath() {
        path.closePath();
    }

}
