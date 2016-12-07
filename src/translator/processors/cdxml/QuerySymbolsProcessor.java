package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.graphics.shapes.builders.configurations.EllipseConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.QuadraticCurveConfiguration;
import translator.graphics.shapes.builders.configurations.RectangleConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class QuerySymbolsProcessor extends SymbolProcessor {
    
    private static final int DEFAULT_SYMBOL_LABEL_SIZE = 12;    
    private static final double DEFAULT_SYMBOL_LABEL_MID_WIDTH = 9.375;   
    public static final double DEFAULT_SYMBOL_BASELINE_OFFSET = 3.275;   
    
    public QuerySymbolsProcessor() {
    }

    protected void process() {
        List<Point> boundingBox = parsePoints(getBoundingBox(), getElement());
        
        Point beginPoint = boundingBox.get(0);
        Point endPoint = boundingBox.get(1);
        List<Point> boundingBoxText;
        
        if(getElement().hasAttribute(
                ParseElementDefinition.BOUNDING_BOX_TEXT)){
            boundingBoxText = parsePoints(getElement().getAttribute(
                    ParseElementDefinition.BOUNDING_BOX_TEXT), getElement());
        }else{
            
            boundingBoxText = new ArrayList();
       
            List<Point> boundingBoxGraphic = parsePoints(getElement().getAttribute(
                ParseElementDefinition.GRAPHIC_BOUNDING_BOX), getElement());
            
            boundingBoxText.add(new Point(boundingBoxGraphic.get(0).getX()- DEFAULT_SYMBOL_LABEL_MID_WIDTH,
                                          boundingBoxGraphic.get(0).getY()- DEFAULT_SYMBOL_LABEL_SIZE/2));            
            boundingBoxText.add(new Point(boundingBoxGraphic.get(0).getX()+ DEFAULT_SYMBOL_LABEL_MID_WIDTH, 
                                          boundingBoxGraphic.get(0).getY()+ DEFAULT_SYMBOL_LABEL_SIZE/2));            

        }
        
        Point beginPointText = boundingBoxText.get(0);
        Point endPointText = boundingBoxText.get(1);
               
        //taken from C++ code
        double curveOffset = 4;      
                
        Point textPoint;        
        if(getElement().hasAttribute(
                ParseElementDefinition.TEXT_POSITION)){
            textPoint = parseCoords(getElement().getAttribute(
                    ParseElementDefinition.TEXT_POSITION), getElement());
        }else{            
            textPoint = new Point(beginPoint.getY(), beginPoint.getY() + DEFAULT_SYMBOL_BASELINE_OFFSET);
            
        }
        
        double bottomOffset = endPointText.getY() - textPoint.getY();
        
        //Taken from C++
        double size = Math.max(GeometricOperations.distance(beginPoint,endPoint),1);
        double margin = size / 4;             
        
        beginPointText.setX(beginPointText.getX() - margin);
        beginPointText.setY(beginPointText.getY() - margin);
        
        endPointText.setX(endPointText.getX() + margin);
        endPointText.setY(endPointText.getY() + margin);
        
        //Create box to cotain the text
        List<SegmentConfiguration> rectangleSegments = new ArrayList();

        //taken from C++ code
        //segment top
        rectangleSegments.add(new SegmentConfiguration(
                new Point(beginPointText.getX() + curveOffset, beginPointText.getY()),
                new Point(endPointText.getX() - curveOffset, beginPointText.getY())));
        //corner top right 
        rectangleSegments.add(new QuadraticCurveConfiguration(
                new Point(endPointText.getX() - curveOffset, beginPointText.getY()),
                new Point(endPointText.getX(), beginPointText.getY() + curveOffset),
                new Point(endPointText.getX(), beginPointText.getY())));
        //segment right
        rectangleSegments.add(new SegmentConfiguration(
                new Point(endPointText.getX(), beginPointText.getY() + curveOffset),
                new Point(endPointText.getX(), endPointText.getY() - curveOffset - bottomOffset)));
        //corner bottom right 
        rectangleSegments.add(new QuadraticCurveConfiguration(
                new Point(endPointText.getX(), endPointText.getY() - curveOffset - bottomOffset),
                new Point(endPointText.getX() - curveOffset, endPointText.getY() - bottomOffset),
                new Point(endPointText.getX(), endPointText.getY() - bottomOffset)));
        //segment bottom
        rectangleSegments.add(new SegmentConfiguration(
                new Point(endPointText.getX() - curveOffset, endPointText.getY() - bottomOffset),
                new Point(beginPointText.getX() + curveOffset, endPointText.getY() - bottomOffset)));
        //corner bottom left
        rectangleSegments.add(new QuadraticCurveConfiguration(
                new Point(beginPointText.getX() + curveOffset, endPointText.getY() - bottomOffset),
                new Point(beginPointText.getX(), endPointText.getY() - curveOffset - bottomOffset),
                new Point(beginPointText.getX(), endPointText.getY() - bottomOffset)));
        //segment left
        rectangleSegments.add(new SegmentConfiguration(
                new Point(beginPointText.getX(), endPointText.getY() - curveOffset - bottomOffset),
                new Point(beginPointText.getX(), beginPointText.getY() + curveOffset)));
        //corner top left 
        rectangleSegments.add(new QuadraticCurveConfiguration(
                new Point(beginPointText.getX(), beginPointText.getY() - curveOffset),
                new Point(beginPointText.getX() + curveOffset, beginPointText.getY()),
                new Point(beginPointText.getX(), beginPointText.getY())));
        
        SplineConfiguration resultingConfiguration = new SplineConfiguration(rectangleSegments);
        
        resultingConfiguration.setLineJoin(LineJoin.Round);
        resultingConfiguration.setStrokeWidth(getLineWidth());
        resultingConfiguration.setColor(getColor());
        resultingConfiguration.setZOrder(getZOrder());
        
        setResultingConfiguration(resultingConfiguration);
    }
    
}
