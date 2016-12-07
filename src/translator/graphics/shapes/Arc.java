
package translator.graphics.shapes;

public class Arc extends Segment {
    
    private double xRadius;
    private double yRadius;
    private double angle;
    private boolean largeArc;
    private boolean sweepPositive;
    
    public Arc() {
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
