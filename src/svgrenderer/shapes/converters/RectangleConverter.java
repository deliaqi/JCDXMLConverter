package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.Rectangle;

public class RectangleConverter implements ShapeConverter<Rectangle> {
    
    private Rectangle shape;
    private Document parentDocument;
    
    public RectangleConverter() {
    }

    public void setShape(Rectangle shape) {
        this.shape = shape;
    }

    public void setDocument(Document parentDocument) {
        this.parentDocument = parentDocument;
    }

    public Element convert() {       
        StringBuilder buffer = new StringBuilder();
        
        //Point 1
        buffer.append("M");
        buffer.append(SvgFormatting.formatCoordinate(shape.getBeginPoint().getX()));
        buffer.append(" ");
        buffer.append(SvgFormatting.formatCoordinate(shape.getBeginPoint().getY()));
        buffer.append(" ");
        
        //Point 2
        buffer.append("L");
        buffer.append(SvgFormatting.formatCoordinate(shape.getEndPoint().getX()));
        buffer.append(" ");
        buffer.append(SvgFormatting.formatCoordinate(shape.getBeginPoint().getY()));
        buffer.append(" ");
        
        //Point 3
        buffer.append("L");
        buffer.append(SvgFormatting.formatCoordinate(shape.getEndPoint().getX()));
        buffer.append(" ");
        buffer.append(SvgFormatting.formatCoordinate(shape.getEndPoint().getY()));
        buffer.append(" ");
        
        //Point 4
        buffer.append("L");
        buffer.append(SvgFormatting.formatCoordinate(shape.getBeginPoint().getX()));
        buffer.append(" ");
        buffer.append(SvgFormatting.formatCoordinate(shape.getEndPoint().getY()));
        buffer.append(" ");
        
        buffer.append("L");
        buffer.append(SvgFormatting.formatCoordinate(shape.getBeginPoint().getX()));
        buffer.append(" ");
        buffer.append(SvgFormatting.formatCoordinate(shape.getBeginPoint().getY()));
        
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "path");
        result.setAttributeNS(null, "d", buffer.toString());
        
        StringBuilder color = null;
        if(shape.getColor() != null){
            color = new StringBuilder();
            color.append("rgb(");
            color.append(shape.getColor().getRed());
            color.append(", ");
            color.append(shape.getColor().getGreen());
            color.append(", ");
            color.append(shape.getColor().getBlue());
            color.append(")");
        }
        
        if(shape.isFill()){
            if(color != null){
                result.setAttribute("fill", color.toString());
            }
            else{
                result.setAttribute("style", "stroke:black;fill:black;");
            }
        }
        else {
            result.setAttribute("fill", "none");
            if(color != null){
                result.setAttribute("stroke", color.toString());
            } else {
                result.setAttribute("stroke", "black");
            }
        }
        if(shape.getShapeRendering() != null){
            result.setAttribute("shape-rendering", String.valueOf(shape.getShapeRendering()));
        }    
        
        return result;
    }
    
}
