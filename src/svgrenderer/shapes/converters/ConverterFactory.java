package svgrenderer.shapes.converters;

import java.util.Hashtable;
import translator.graphics.shapes.Circle;
import translator.graphics.shapes.CompositeShape;
import translator.graphics.shapes.CompositeText;
import translator.graphics.shapes.Ellipse;
import translator.graphics.shapes.EmbeddedObject;
import translator.graphics.shapes.Rectangle;
import translator.graphics.shapes.Segment;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.Spline;

public final class ConverterFactory {
    
    private static ConverterFactory instance;
    private Hashtable<String, ShapeConverter> converters = new Hashtable();
    
    static{
        instance = new ConverterFactory();
        
        instance.addConverter(CompositeShape.class.getName(), new CompositeShapeConverter());
        instance.addConverter(Segment.class.getName(), new SegmentConverter());
        instance.addConverter(Spline.class.getName(), new SplineConverter());
        instance.addConverter(CompositeText.class.getName(), new TextConverter());
        instance.addConverter(Rectangle.class.getName(), new RectangleConverter());
        instance.addConverter(Circle.class.getName(), new CircleConverter());
        instance.addConverter(Ellipse.class.getName(), new EllipseConverter());
        instance.addConverter(EmbeddedObject.class.getName(), new ImageConverter());
    }
    
    private ConverterFactory() {
    }
    
    public static ConverterFactory getInstance(){
        return instance;
    }
    
    public ShapeConverter getConverter(Shape shapeToConvert){
        ShapeConverter suitableConverter = converters.get(shapeToConvert.getClass().getName());
        
        suitableConverter.setShape(shapeToConvert);
        
        return suitableConverter;
    }
    
    public void addConverter(String shapeName, ShapeConverter converter){
        converters.put(shapeName, converter);
    }
}
