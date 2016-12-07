package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import translator.graphics.shapes.CompositeShape;
import translator.graphics.shapes.Shape;

public class CompositeShapeConverter implements ShapeConverter<CompositeShape> {
    
    private CompositeShape<Shape> shape;
    private Document parentDocument;
    
    public CompositeShapeConverter() {
    }

    public void setShape(CompositeShape shape) {
        this.shape = shape;
    }
    
    public void setDocument(Document parentDocument) {
        this.parentDocument = parentDocument;
    }

    public Element convert() {
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "g");
        
        result.setAttribute("id", shape.getId());
        
        for(Shape innerShape : shape.getShapes()){
            ShapeConverter converter = 
                    ConverterFactory.getInstance().getConverter(innerShape);
            converter.setDocument(parentDocument);
            
            Element innerElement = converter.convert();
            result.appendChild((Node) innerElement);
        }
        
        
        
        return result;
    }
    
}
