package translator.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class JoinPoint {
    
    // used to determine almost identical slopes and treat them as identical
    // difference must be less than minimal
    private static final double MINIMAL_SLOPE_DIFFERENCE = 0.000001;
    
    // tangent of ten degrees or so
    private static final double CD_TAN_10 = 0.175;

    //Cosine value to determine if two bonds are colinear
    private static final double COSINE_LIMIT = Math.cos(Math.toRadians(0.5));
    
    //Constans to reference points inisde an array
    private static final int LEFTBEGIN = 0;
    private static final int LEFTEND = 1;
    private static final int BEGIN = 2;
    private static final int END = 3;
    
    private String nodeId;
    
    protected Point center;
    
    private Hashtable<String, Point> ends = new Hashtable();
    private Hashtable<String, Double> beginWidths = new Hashtable();
    private Hashtable<String, Double> endWidths = new Hashtable();
    private Hashtable<String, Boolean> omitPoints = new Hashtable();
    
    private Hashtable<String, JoinPointResult> results = new Hashtable();
    
    public JoinPoint(Point center, String nodeId) {
        this.center = center;
        this.nodeId = nodeId;
    }
    
    public void addPoint(String id, Point point){
        addPoint(id, point, 1);
    }
    
    public void addPoint(String id, Point point, double width){
        addPoint(id, point, width, width);
    }
    
    public void addPoint(String id, Point point, double beginWidth, double endWidth){
        ends.put(id, point);
        beginWidths.put(id, new Double(beginWidth));
        endWidths.put(id, new Double(endWidth));
    }
    
    private void calculate(){
        JoinPointResult resultPoints = null;
        SortedSet<GeometricLine> rays = new TreeSet();
        
        for(String id : ends.keySet()){
            Point end = ends.get(id);
            Double beginWidth = beginWidths.get(id);
            Double endWidth = endWidths.get(id);
            
            GeometricLine ray = new GeometricLine(id,
                    new Point(center.getX(), center.getY()),
                    new Point(end.getX(), end.getY()),
                    beginWidth.doubleValue(), endWidth.doubleValue());
            
            rays.add(ray);
        }
        
        if(rays.size() > 1){
            
            List<Point> intersectionPoints = new ArrayList();

            Point[] lastPoints = new Point[] {rays.last().getLeftBegin(),
                                        rays.last().getLeftEnd(), rays.last().getBegin(), rays.last().getEnd()};
            for(GeometricLine ray : rays){
                Point intersectionPoint = GeometricOperations.intersection(
                        ray.getRightBegin(), ray.getRightEnd(),
                        lastPoints[LEFTBEGIN], lastPoints[LEFTEND]);
                
                //check if the two bonds are colinear
                Double cosine = Math.abs(GeometricOperations.cosine(lastPoints[BEGIN].getX(),lastPoints[BEGIN].getY(),lastPoints[END].getX(),lastPoints[END].getY(),ray.getEnd().getX(),ray.getEnd().getY()));
                if (cosine > COSINE_LIMIT){
                    intersectionPoint.setOmit(true);
                }
                
                lastPoints = new Point[] {ray.getLeftBegin(), ray.getLeftEnd(), ray.getBegin(), ray.getEnd()};

                intersectionPoints.add(intersectionPoint);
            }
            
            int k = 0;

            GeometricLine lastRay = rays.last();
            for(GeometricLine ray : rays){
                if(Double.compare(ray.getBeginWidth(), lastRay.getBeginWidth()) != 0){
                    // bonds are a different width at this end
                    if(Math.abs(lastRay.getSlope() - ray.getSlope()) < CD_TAN_10){
                        // Treat nearly horizontal and nearly vertical bonds as exactly such.
                        intersectionPoints.get(k).setOmit(true);
                    }
                } else if (Math.abs(lastRay.getSlope() - ray.getSlope()) < MINIMAL_SLOPE_DIFFERENCE) {
                    // bonds are the same width at this end
                    // and their slopes are almost identical
                    intersectionPoints.get(k).setOmit(true);
                }
                
                double lengthOfRay;
                double distanceToNode;
                
                for(Point intersectionPoint : intersectionPoints){
                    lengthOfRay = GeometricOperations.distance(ray.getBegin(), ray.getEnd());
                    distanceToNode = GeometricOperations.distance(center, intersectionPoint);
                    // The distance to the intersection points is bigger
                    // than the length of the ray.
                    if(distanceToNode > lengthOfRay ){
                        intersectionPoints.get(k).setOmit(true);
                    }
                }

                k++;
                lastRay = ray;
            }

            k = 0;
            for(GeometricLine ray : rays){
                Point point1 = intersectionPoints.get(k);
                Point point2 = intersectionPoints.get((k+1)%intersectionPoints.size());                
                
                if(point1.isOmit() && point2.isOmit()){
                    resultPoints = new JoinPointResult(
                        new Point(ray.getRightBegin().getX(), ray.getRightBegin().getY()),
                        center, 
                        new Point(ray.getLeftBegin().getX(), ray.getLeftBegin().getY()),
                        nodeId);
                }
                else if(point1.isOmit()){
                    resultPoints = new JoinPointResult(
                        new Point(ray.getRightBegin().getX(), ray.getRightBegin().getY()),
                        center, 
                        new Point(point2.getX(), point2.getY()),
                        nodeId);
                }
                else if(point2.isOmit()){
                    resultPoints = new JoinPointResult(
                        new Point(point1.getX(), point1.getY()),
                        center, 
                        new Point(ray.getLeftBegin().getX(), ray.getLeftBegin().getY()),
                        nodeId);
                }
                else{
                    resultPoints = new JoinPointResult(
                        new Point(point1.getX(), point1.getY()),
                        center, 
                        new Point(point2.getX(), point2.getY()),
                        nodeId);
                }
                
                results.put(ray.getId(), resultPoints);
                
                k++;
            }
        }
        else if(rays.size() == 1) {
            // there is only one bond joined at this point
            // so don't calculate any mitering
            
            GeometricLine ray = rays.last();
            
            // this is a free end
            resultPoints = getFreeEndSegmentResult(ray);
            
            results.put(ray.getId(), resultPoints);
        }
    }
    
    /**
     * Returns the segment result (defined by right-center-left points)
     * for the specified ray, when the ray is the only one attached to this
     * join point (the end is a free, not mitered, end).
     *
     * @param ray The ray to calculate the end segment
     * @returns The <code>JoinPointResult</code> instance containing the
     *          (right-center-left) points collection.
     */
    protected JoinPointResult getFreeEndSegmentResult(GeometricLine ray) {
        Point rightBegin = new Point(ray.getRightBegin().getX(), ray.getRightBegin().getY());
        Point leftBegin = new Point(ray.getLeftBegin().getX(), ray.getLeftBegin().getY());
        Point centerBegin = center;
        return new JoinPointResult(rightBegin, centerBegin, leftBegin, nodeId);
    }
    
    public JoinPointResult getResult(String id){
        if (results.isEmpty()) {
            calculate();
        }
        
        return results.get(id);
    }
}
