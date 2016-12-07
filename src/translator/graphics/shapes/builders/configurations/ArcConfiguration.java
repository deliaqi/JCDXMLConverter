
package translator.graphics.shapes.builders.configurations;

import translator.utils.Point;

public class ArcConfiguration extends SegmentConfiguration {
    
    private double xRadius;
    private double yRadius;
    private double angle;
    private boolean largeArc;
    private boolean sweepPositive;
    
    public ArcConfiguration(Point point1, Point point2, double xRadius, double yRadius,
            double angle, boolean largeArc, boolean sweepPositive) {
        super(point1, point2);
        
        setBuilderId(ARC_BUILDER_ID);
        
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.angle = angle;
        this.largeArc = largeArc;
        this.sweepPositive = sweepPositive;
    }

    public double getXRadius() {
        return xRadius;
    }
    
    public void setXRadius(double xRadius) {
        this.xRadius = xRadius;
    }
    
    public double getYRadius() {
        return yRadius;
    }
    
    public void setYRadius(double yRadius) {
        this.yRadius = yRadius;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    public boolean isLargeArc() {
        return largeArc;
    }
    
    public void setLargeArc(boolean largeArc) {
        this.largeArc = largeArc;
    }
    
    public boolean isSweepPositive() {
        return sweepPositive;
    }
    
    public void setSweepPositive(boolean sweepPositive) {
        this.sweepPositive = sweepPositive;
    }
    
}
