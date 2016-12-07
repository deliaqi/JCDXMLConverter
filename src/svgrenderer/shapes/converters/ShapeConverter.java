package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.Shape;

public interface ShapeConverter<S extends Shape> {
    
    double DEFAULT_STROKE_WIDTH = 0.01;
    
    public void setShape(S shape);
    
    public void setDocument(Document document);
    
    public Element convert();
    
}
