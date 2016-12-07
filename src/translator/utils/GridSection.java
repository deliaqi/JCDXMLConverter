package translator.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 *Represent a section of grid. This is a square area with width and height
 *equal to splitFactor property specified in the Grid object associated
 */
public class GridSection {
    
    private String id;
    
    private List<String> allowIds;
    
    private int series = -1;
    private Hashtable<String, ArrayList<Point>> pointBySeries = new Hashtable();
    private Hashtable<String, ArrayList<Line>> linesBySeries = new Hashtable();
    
    private int beginX;
    private int endX;
    
    private int beginY;
    private int endY;
    
    
    
    public GridSection(String id, List<String> allowIds, int beginX, int endX, int beginY, int endY) {
        this.id = id;
        
        this.beginX = beginX;
        this.endX = endX;
        this.beginY = beginY;
        this.endY = endY;
        
        this.allowIds = allowIds;
        
        nextSeries();
    }
    
    /**
     *Add the point in the current series
     *
     *@param point Point to add
     */
    public void addPoint(Point point, String id){
        if(point != null){
            List<Point> points = pointBySeries.get(createSerieId(id));
            points.add(point);
        }
    }
    
    /**
     *Add the line in the current series
     *
     *@param line Line to add
     */
    public void addLine(Line line){
        addLine(line, series);
    }
    
    /**
     *Add the line in a especify series
     *
     *@param line Line to add
     *@param series Specify series
     */
    public void addLine(Line line, int series){
        if(line != null){
            ArrayList<Line> lines = linesBySeries.get(createSerieId(line.getId(), series));
            
            if(lines == null){
                lines = new ArrayList();
                linesBySeries.put(createSerieId(line.getId(), series), lines);
            }
            
            lines.add(line);
        }
    }
    
    /**
     *Return all the intersection points of this section
     */
    public List<IntersectionPoint> getIntersections(){
        List<IntersectionPoint> result = new ArrayList();
        
        for(int i = 0; i <= series; i++){
            List<Line> lines = getLinesBySerie(i);
            if(!lines.isEmpty()){
                for(Line currentLine : lines){
                    for(int j = 0; j <= series; j++){
                        List<Line> serieLines = getLinesBySerie(j);

                        result.addAll(searchIntersections(currentLine, serieLines));
                    }
                }
            }
        }
        
        return result;
    }
    
    private List<Line> getLinesBySerie(int index){
        List<Line> result = new ArrayList();
        
        for(String id : allowIds){
            if(linesBySeries.get(createSerieId(id, index)) != null){
                result.addAll(linesBySeries.get(createSerieId(id, index)));
            }
        }
        
        return result;
    }
    
    private List<IntersectionPoint> searchIntersections(Line line, List<Line> serieLines){
        List<IntersectionPoint> result = new ArrayList();
        
        if(serieLines != null){
            for(Line currentLine : serieLines){
                Point intersectionPoint = 
                        GeometricOperations.realIntersection(
                        line.getBegin(), line.getEnd(),
                        currentLine.getBegin(), currentLine.getEnd());
                
                if(intersectionPoint != null){
                    IntersectionPoint newPoint = new IntersectionPoint(
                            line.getId(),
                            intersectionPoint.getX(),
                            intersectionPoint.getY(),
                            line.getAngle(), currentLine.getAngle());
                    
                    result.add(newPoint);
                }
            }
        }
        
        return result;
    }
    
    /**
     *Increase the current series
     */
    public void nextSeries(){
        series++;
        for(String id : allowIds){
            pointBySeries.put(createSerieId(id), new ArrayList<Point>());
        }
    }
    
    private String createSerieId(String id){
        return createSerieId(id, series);
    }
    
    private String createSerieId(String id, int serie){
        return serie + id;
    }
    
    public int getCurrentSeries(){
        return series;
    }
    
    public String getId(){
        return id;
    }

    public int getBeginX() {
        return beginX;
    }

    public int getBeginY() {
        return beginY;
    }
}
