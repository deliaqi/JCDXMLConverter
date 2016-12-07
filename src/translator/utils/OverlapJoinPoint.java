
package translator.utils;

import java.util.Hashtable;

/**
 * Calculates bonds overlapping areas.
 * Provides a custom calculation for cases where only one bond
 * is attached to the join point.
 * These cases are not taken care of in <code>JoinPoint</code>.
 */
public class OverlapJoinPoint extends JoinPoint {
    
    // store overlapping area width difference for a bond
    private Hashtable<String, Double> overlapWidthOffsets = new Hashtable();
    private String nodeId;
    
    public OverlapJoinPoint(Point center, String nodeId) {
        super(center, nodeId);
        this.nodeId = nodeId;
    }
    
    public void addPoint(String id, Point point, double width, double overlapWidthOffset){
        addPoint(id, point, width, width, overlapWidthOffset);
    }
    
    /**
     * Adds a bond to the calculation, specifying a separate overlap width for the
     * overlapping area.
     * @param id The bond's id.
     * @param point The bond's other end-point (considering this join-point as one of the ends).
     * @param beginWidth The width of the bond at this point
     * @param endWidth The width of the bond at its other end-point
     * @param overlapWidthOffset The width difference between the bond's overlapping area and the bond's width at this point
     */
    public void addPoint(String id, Point point, double beginWidth, double endWidth, 
            double overlapWidthOffset){
        super.addPoint(id, point, beginWidth, endWidth);
        overlapWidthOffsets.put(id, new Double(overlapWidthOffset));
    }
    
    public JoinPointResult getFreeEndSegmentResult(GeometricLine ray) {
        // get width for only one side, so divide the whole offset by 2
        double overlapWidthOffset = overlapWidthOffsets.get(ray.getId()).doubleValue() / 2;
        
        Point rightBegin = GeometricOperations.offset(ray.getRightBegin(), ray.getAngle(), -overlapWidthOffset);
        Point leftBegin = GeometricOperations.offset(ray.getLeftBegin(), ray.getAngle(), -overlapWidthOffset);
        Point centerBegin = GeometricOperations.offset(center, ray.getAngle(), -overlapWidthOffset);
        
        return new JoinPointResult(rightBegin, centerBegin, leftBegin, nodeId);
    }
    
}
