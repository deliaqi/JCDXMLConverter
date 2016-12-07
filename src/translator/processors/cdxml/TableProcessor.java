package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.RectangleConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeRendering;
import translator.utils.Point;

public class TableProcessor extends CDXMLProcessor{
    
    private static final String DEFAULT_ID = "Default";
    private BorderProperty defaultProperty = new BorderProperty(DEFAULT_ID);
    private Hashtable<String, BorderProperty> borderProperties = new Hashtable();
    
    public TableProcessor() {
    }
    
    protected void process() {
        ParsedElement table = getElement();
        
        //This is a Comparator Object to sort all the coordinates increasingly
        Comparator<Double> coordinatesComparator = new Comparator<Double>() {
            
            public int compare(Double firstElement, Double newElement) {
                double firstValue = firstElement.doubleValue();
                double newValue = newElement.doubleValue();
                
                int result = 0;
                
                if(firstValue > newValue){
                    result = 1;
                } else if(firstValue < newValue){
                    result = -1;
                } else {
                    result = 0;
                }
                
                return result;
            }
            
        };
        
        TreeSet<Double> xCoordinates = new TreeSet(coordinatesComparator);
        TreeSet<Double> yCoordinates = new TreeSet(coordinatesComparator);
        
        Collection<ShapeBuilderConfiguration> tableConfiguration = new ArrayList();
        
        for(ParsedElement page : table.getElements(ParseElementDefinition.TABLE_PAGE)){
            String coordinates =
                    page.getAttribute(ParseElementDefinition.TABLE_PAGE_BOUNDS_IN_PARENT);
            
            List<Point> points = parsePoints(coordinates, page);
            
            Point firstPoint = points.get(0);
            Point lastPoint = points.get(points.size() - 1);
            xCoordinates.add(firstPoint.getX());
            xCoordinates.add(lastPoint.getX());
            
            yCoordinates.add(firstPoint.getY());
            yCoordinates.add(lastPoint.getY());
            
            RectangleConfiguration backGroundRectangle =
                    new RectangleConfiguration(firstPoint,lastPoint);
            backGroundRectangle.setColor(convertColor(environment.getBackgroundColor()));
            backGroundRectangle.setShapeRendering(ShapeRendering.crispEdges);
            backGroundRectangle.setFill(true);
            
            tableConfiguration.add(backGroundRectangle);
            
            for(ParsedElement border : page.getElements(ParseElementDefinition.TABLE_PAGE_BORDER)){
                String side = border.getAttribute(ParseElementDefinition.TABLE_PAGE_BORDER_SIDE);
                String id = "";
                if(side.equals(ParseElementDefinition.TABLE_PAGE_BORDER_RIGHT_SIDE)){
                    id = createId(lastPoint.getX() ,firstPoint.getY(), lastPoint.getY());
                } else if(side.equals(ParseElementDefinition.TABLE_PAGE_BORDER_LEFT_SIDE)){
                    id = createId(firstPoint.getX() , firstPoint.getY() , lastPoint.getY());
                } else if(side.equals(ParseElementDefinition.TABLE_PAGE_BORDER_TOP_SIDE)){
                    id = createId(firstPoint.getY(), firstPoint.getX() , lastPoint.getX());
                } else if(side.equals(ParseElementDefinition.TABLE_PAGE_BORDER_BOTTOM_SIDE)){
                    id = createId(lastPoint.getY(), firstPoint.getX(), lastPoint.getX());
                }
                if(borderProperties.get(id.toString()) == null){
                    
                    BorderProperty property = new BorderProperty(id.toString());
                    
                    Color borderColor = convertColor(border.getElements(ParseElementDefinition.COLOR).get(0));
                    
                    if(borderColor != null){
                        property.setColor(borderColor);
                    }else{
                        property.setColor(color);
                    }
                    
                    if(border.hasAttribute(ParseElementDefinition.TABLE_PAGE_BORDER_LINE_WIDTH)){
                        property.setStrokeWidth(
                                Double.parseDouble(
                                border.getAttribute(
                                ParseElementDefinition.TABLE_PAGE_BORDER_LINE_WIDTH)));
                    }
                    
                    if(border.hasAttribute(ParseElementDefinition.TABLE_PAGE_BORDER_LINE_TYPE)){
                        property.setLineType(
                                border.getAttribute(
                                ParseElementDefinition.TABLE_PAGE_BORDER_LINE_TYPE));
                    }
                    
                    borderProperties.put(id.toString(), property);
                }
            }
        }
        
        
        
        double lastX = 0;
        double lastY = 0;
        
        double x = 0;
        double y = 0;
        
        for(Double xCoordinate : xCoordinates){
            x = xCoordinate.doubleValue();
            boolean firstY = true;
            for(Double yCoordinate : yCoordinates){
                if(firstY){
                    lastY = yCoordinate.doubleValue();
                    firstY = false;
                } else{
                    y = yCoordinate.doubleValue();
                    SegmentConfiguration configuration = new SegmentConfiguration(
                            new Point(x, lastY),
                            new Point(x, y));
                    
                    configuration.setShapeRendering(ShapeRendering.crispEdges);
                    
                    BorderProperty property = borderProperties.get(
                            createId(x, lastY, y));
                    
                    if(property == null){
                        property = defaultProperty;
                    }
                    
                    configuration.setStrokeWidth(property.getStrokeWidth());
                    configuration.setColor(property.getColor());
                    
                    if(property.getLineType().equals(
                            ParseElementDefinition.TABLE_PAGE_BORDER_LINE_TYPE_DASHED)){
                        configuration.setDashed(true);
                        configuration.setDashLength(property.getStrokeWidth());
                    }
                    
                    tableConfiguration.add(configuration);
                    
                    lastY = y;
                }
            }
        }
        
        List <Double> listYCoordinates = new ArrayList();
        listYCoordinates.addAll(yCoordinates);
        
        for(int i = 0; i < listYCoordinates.size(); i++){
            y = listYCoordinates.get(i).doubleValue();
            boolean firstX = true;
            
            for(Double xCoordinate : xCoordinates){
                
                if(firstX){
                    lastX = xCoordinate.doubleValue();
                    firstX = false;
                } else{
                    x = xCoordinate.doubleValue();

                    double offsetLeft  = 0.0;
                    double offsetRight = 0.0;
                    
                    if (i < listYCoordinates.size()-1){         // every horizontal segment is crossed downwards, except for the last one
                        
                        if(borderProperties.get(createId(lastX, y, listYCoordinates.get(i+1).doubleValue())) == null){
                            offsetLeft = defaultProperty.getStrokeWidth()/2.0;
                        } else {                                
                            offsetLeft  = borderProperties.get(
                                    createId(lastX, y, listYCoordinates.get(i+1).doubleValue())).getStrokeWidth()/2.0;
                        }
                        
                        if (borderProperties.get(createId(x, y, listYCoordinates.get(i+1).doubleValue())) == null){
                            offsetRight = defaultProperty.getStrokeWidth()/2.0;
                        } else {
                            offsetRight = borderProperties.get(
                                    createId(x, y, listYCoordinates.get(i+1).doubleValue())).getStrokeWidth()/2.0;
                        }
                    } else{                                    // last horizontal segment 
                        if(borderProperties.get(createId(lastX, listYCoordinates.get(i-1).doubleValue(),y)) == null){
                            offsetLeft = defaultProperty.getStrokeWidth()/2.0;
                        } else {
                            offsetLeft  = borderProperties.get(
                                    createId(lastX, listYCoordinates.get(i-1).doubleValue(),y)).getStrokeWidth()/2.0;
                        }
                        
                        if (borderProperties.get(createId(x, listYCoordinates.get(i-1).doubleValue(),y)) == null) {
                            offsetRight = defaultProperty.getStrokeWidth()/2.0;
                        } else {
                            offsetRight = borderProperties.get(
                                    createId(x, listYCoordinates.get(i-1).doubleValue(),y)).getStrokeWidth()/2.0;
                        }
                    }
                                      
                    BorderProperty property = borderProperties.get(
                            createId(y, lastX, x));
                    
                    SegmentConfiguration configuration = new SegmentConfiguration(
                            new Point(lastX - offsetLeft, y),
                            new Point(x + offsetRight, y)); 
                    
                    configuration.setShapeRendering(ShapeRendering.crispEdges);                    
                    
                    if(property == null){
                        property = defaultProperty;
                    }
                    
                    configuration.setStrokeWidth(property.getStrokeWidth());
                    configuration.setColor(property.getColor());
                    
                    if(property.getLineType().equals(
                            ParseElementDefinition.TABLE_PAGE_BORDER_LINE_TYPE_DASHED)){
                        configuration.setDashed(true);
                        configuration.setDashLength(property.getStrokeWidth());                        
                    }
                    
                    tableConfiguration.add(configuration);
                    
                    lastX = x;
                }
            }
        }
        
        CompositeShapeConfiguration resultinConfiguration =
                new CompositeShapeConfiguration("Table", tableConfiguration);
        resultinConfiguration.setZOrder(zOrder);
        
        setResultingConfiguration(resultinConfiguration);
    }
    
    private String createId(String coordinate1, String coordinate2, String coordinate3){
        return createId(Double.parseDouble(coordinate1),
                Double.parseDouble(coordinate2),
                Double.parseDouble(coordinate3));
    }
    
    private String createId(double coordinate1, double coordinate2, double coordinate3){
        StringBuilder id = new StringBuilder();
        
        id.append(coordinate1);
        id.append("-");
        id.append(coordinate2);
        id.append("-");
        id.append(coordinate3);
        
        return id.toString();
    }
    
    private class BorderProperty {
        
        private String id;
        private Color color = new Color(0, 0, 0);
        private double strokeWidth = 1;
        private String lineType = "";
        
        public BorderProperty(String id){
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
        
        public Color getColor() {
            return color;
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        public double getStrokeWidth() {
            return strokeWidth;
        }
        
        public void setStrokeWidth(double strokeWidth) {
            this.strokeWidth = strokeWidth;
        }
        
        public String getLineType() {
            return lineType;
        }
        
        public void setLineType(String lineType) {
            this.lineType = lineType;
        }
        
    }
}
