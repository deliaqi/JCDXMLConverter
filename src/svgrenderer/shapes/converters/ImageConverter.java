package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.EmbeddedObject;

public class ImageConverter implements ShapeConverter<EmbeddedObject> {
    
    private EmbeddedObject image;
    private Document parentDocument;
        
    public ImageConverter() {
    }
    
    public void setShape(EmbeddedObject image) {
        this.image = image;
    }
    
    public void setDocument(Document parentDocument) {
        this.parentDocument = parentDocument;
    }
        
    public Element convert() {
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "image");
        result.setAttribute("x", SvgFormatting.formatCoordinate(image.getCornerPoint().getX()));
        result.setAttribute("y", SvgFormatting.formatCoordinate(image.getCornerPoint().getY()));
        result.setAttribute("width", Double.toString(image.getWidth()));
        result.setAttribute("height", Double.toString(image.getHeight()));
        result.setAttribute("preserveAspectRatio","none");
        result.setAttribute("xlink:href", "data:;base64," + image.getImageBytes());
        
        if(image.isRotate()){
            result.setAttribute("transform", "rotate(" + String.valueOf(image.getRotationAngle()) 
            + " " + SvgFormatting.formatCoordinate(image.getRotationCenter().getX()) + " " + 
                    SvgFormatting.formatCoordinate(image.getRotationCenter().getY()) + ")");
        }        
        
        return result;        
    }    
}
