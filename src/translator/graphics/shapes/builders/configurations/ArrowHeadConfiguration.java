
package translator.graphics.shapes.builders.configurations;

import java.util.ArrayList;
import java.util.List;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class ArrowHeadConfiguration extends SplineConfiguration {
    
    private ArrowHeadConfiguration(List<SegmentConfiguration> segments) {
        super(segments);
    }
    
    /**
     * This method gets the arrow head shape configuration
     */
    public static ArrowHeadConfiguration getArrowHeadShape(Point basePoint, Point startPoint,
            Point endPoint, double cornerDistance, double angle, double factor, boolean right, boolean left) {
        Point leftCornerPoint = GeometricOperations.offset(endPoint, angle - Math.PI / 2, cornerDistance);
        Point rightCornerPoint = GeometricOperations.offset(endPoint, angle + Math.PI / 2, cornerDistance);
        
        Point leftBasePoint = GeometricOperations.offset(basePoint, angle - Math.PI / 2, factor * cornerDistance);
        Point rightBasePoint = GeometricOperations.offset(basePoint, angle + Math.PI / 2, factor * cornerDistance);
        
        leftBasePoint = GeometricOperations.offset(leftBasePoint, angle, factor);
        rightBasePoint = GeometricOperations.offset(rightBasePoint, angle, factor);
        
        ArrowHeadConfiguration result = null;
        
        if(!right && !left){
            SegmentConfiguration leftSide = new SegmentConfiguration(startPoint, leftCornerPoint);
            
            Point baseEndPoint = new Point(-basePoint.getX() + factor, -basePoint.getY() + factor);
            SegmentConfiguration baseCurve = new CubicCurveConfiguration(
                    leftCornerPoint, rightCornerPoint, leftBasePoint, rightBasePoint);            
            
            List<SegmentConfiguration> splineSegments = new ArrayList();
            splineSegments.add(leftSide);
            splineSegments.add(baseCurve);           
            result = new ArrowHeadConfiguration(splineSegments);
            result.setClosed(true);
        } else if(right){
            result = getLeftOrRightArrowHeadShape(basePoint, startPoint, factor, rightCornerPoint, rightBasePoint);
        } else if(left){
            result = getLeftOrRightArrowHeadShape(basePoint, startPoint, factor, leftCornerPoint, leftBasePoint);
        }
        
        return result;
    }
    
    /**
     * This method gets the right or left arrow head shape
     */
    private static ArrowHeadConfiguration getLeftOrRightArrowHeadShape(Point basePoint, Point startPoint,
            double factor, Point cornerPoint, Point endBasePoint) {
        ArrowHeadConfiguration result;
        SegmentConfiguration side = new SegmentConfiguration(startPoint, cornerPoint);
        
        Point baseEndPoint = new Point(-basePoint.getX() + factor, -basePoint.getY() + factor);
        SegmentConfiguration baseCurve = new CubicCurveConfiguration(cornerPoint, basePoint, endBasePoint, basePoint);
        
        List<SegmentConfiguration> splineSegments = new ArrayList();
        splineSegments.add(side);
        splineSegments.add(baseCurve);
        result = new ArrowHeadConfiguration(splineSegments);
        result.setClosed(true);
        return result;
    }
    
    /**
     * This method gets the arrow head shape configuration when the arrow head type is angle and full.
     */
    public static ArrowHeadConfiguration getFullArrowHeadAngle(Point startPoint, Point endPoint, double cornerDistance,
            double angle, boolean right, boolean left) {
        Point leftCornerPoint = GeometricOperations.offset(endPoint, angle + Math.PI / 2, cornerDistance);
        Point rightCornerPoint = GeometricOperations.offset(endPoint, angle - Math.PI / 2, cornerDistance);
        
        ArrowHeadConfiguration result = null;
        
        if(!right && !left){
            SegmentConfiguration rightSide = new SegmentConfiguration(rightCornerPoint, startPoint);
            SegmentConfiguration leftSide = new SegmentConfiguration(startPoint, leftCornerPoint);
            
            List<SegmentConfiguration> splineSegments = new ArrayList();
            splineSegments.add(rightSide);
            splineSegments.add(leftSide);
            result = new ArrowHeadConfiguration(splineSegments);
            result.setClosed(false);
        } else if(left) {
            result = getLeftOrRightFullHeadAngle(startPoint, rightCornerPoint);
        } else if(right) {
            result = getLeftOrRightFullHeadAngle(startPoint, leftCornerPoint);
        }
        return result;
    }
    
    /**
     * This method gets the right or left arrow head shape configuration when the arrow head type is angle and full.
     */
    private static ArrowHeadConfiguration getLeftOrRightFullHeadAngle(Point startPoint, Point cornerPoint) {
        ArrowHeadConfiguration result;
        SegmentConfiguration side = new SegmentConfiguration(cornerPoint, startPoint);
        
        List<SegmentConfiguration> splineSegments = new ArrayList();
        splineSegments.add(side);
        result = new ArrowHeadConfiguration(splineSegments);
        result.setClosed(false);
        return result;
    }
}
