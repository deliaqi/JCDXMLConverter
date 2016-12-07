package translator.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Grid {
    
    private List<String> allowIds = new ArrayList();
    
    private Hashtable<String, GridSection> sections = new Hashtable();
    
    private Hashtable<String, Point> lastPoints = new Hashtable();
    private Hashtable<String, Integer> lastBeginsX = new Hashtable();
    private Hashtable<String, Integer> lastBeginsY = new Hashtable();
    
    private List<IntersectionPoint> intersectionPoints = new ArrayList();
    
    int splitFactor;
    
    public Grid(List<String> allowIds, int splitFactor) {
        this.allowIds.addAll(allowIds);
        this.splitFactor = splitFactor;
    }
    
    public Grid(String allowId, int splitFactor) {
        this.allowIds.add(allowId);
        this.splitFactor = splitFactor;
    }
    
    /**
     *Add the point in the current section and series
     */
    public void addPoint(Point point, String id){
        GridSection section = findSection(point);
        
        Point lastPoint = lastPoints.get(id);
        
        section.addPoint(point, id);
        
        if(lastPoint != null){
            int lastBeginX = lastBeginsX.get(id);
            int lastBeginY = lastBeginsY.get(id);
            
            Line newLine = new Line(id, lastPoint, point);
        
            //Create a possible intersection line
            //Adds the line to all the sections that it affects
            section.addLine(newLine);
            if(section.getBeginX() != lastBeginX ||
                    section.getBeginY() != lastBeginY){
                findSection(
                        new Point(lastBeginX, lastBeginY)).addLine(
                        newLine, section.getCurrentSeries());
            } 
            if(section.getBeginX() != lastBeginX &&
                    section.getBeginY() != lastBeginY){
                int newBeginX = lastBeginX - section.getBeginX();
                int newBeginY = lastBeginY - section.getBeginY();

                findSection(
                        new Point(section.getBeginX() + newBeginX, section.getBeginY())).addLine(
                        newLine, section.getCurrentSeries());
                findSection(
                        new Point(section.getBeginX(), section.getBeginY() + newBeginY)).addLine(
                        newLine, section.getCurrentSeries());
            }
        }
        
        lastBeginsX.put(id, section.getBeginX());
        lastBeginsY.put(id, section.getBeginY());
        
        lastPoints.put(id, point);
    }
    
    /**
     *Increase the series of the all sections
     */
    public void nextSeries(){
        for(GridSection section : sections.values()){
            section.nextSeries();
        }
    }
    
    public void clearIntersectionPoints(){
        intersectionPoints = new ArrayList();
    }
    
    /**
     *Return all the found intersection points in the all sections a series
     */
    public List<IntersectionPoint> getIntersectionPoints(){
        if(intersectionPoints.isEmpty()){
            for(GridSection section : sections.values()){
                intersectionPoints.addAll(section.getIntersections());
            }
        }
        
        return intersectionPoints;
    }
    
    public List<IntersectionPoint> getIntersectionPoints(String id){
        List<IntersectionPoint> result = new ArrayList();
        
        for(IntersectionPoint point : getIntersectionPoints()){
            if(point.getId().equals(id)){
                result.add(point);
            }
        }
        
        return result;
    }
    
    public List<Line> getIntersectionLines(String id){
        List<Line> result = new ArrayList();
        List<IntersectionPoint> points = getIntersectionPoints(id);
        
        for(IntersectionPoint point : points){
            result.add(new Line(
                    GeometricOperations.offset(
                    point, point.getAlphaAngle() + Math.PI / 2, 0.1),
                    GeometricOperations.offset(
                    point, point.getAlphaAngle() - Math.PI / 2, 0.1)));
        }
        
        return result;
    }
    
    /**
     *Find the section corresponding to the reference point
     *
     *@param point Reference point
     */
    private GridSection findSection(Point point){
        GridSection result = sections.get(createSectionId(point));
        
        if(result == null){
            result = createSection(point);
        }
        
        return result;
    }
    
    /**
     *Create a section to contain the reference point
     *
     *@param point Reference point
     */
    private GridSection createSection(Point point){
        int beginX = ((int) (point.getX() / splitFactor)) * splitFactor;
        int beginY = ((int) (point.getY() / splitFactor)) * splitFactor;
        
        GridSection section = new GridSection(createSectionId(point), allowIds,
                beginX, beginX + splitFactor, beginY, beginY + splitFactor);
        
        sections.put(section.getId(), section);
        
        return section;
    }
    
    private String createSectionId(Point point){
        return createSectionId((int)point.getX(), (int)point.getY());
    }
    
    private String createSectionId(int beginX, int beginY){
        return Integer.toString(
                beginX / splitFactor * splitFactor) + 
                Integer.toString(
                beginY / splitFactor * splitFactor);
    }
    
}
