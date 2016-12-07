package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.Segment;
import translator.graphics.shapes.builders.configurations.LineCap;

public class SegmentConverter implements ShapeConverter<Segment> {
    
    public Document parentDocument;
    public Segment shape;
    
    public SegmentConverter() { 
    }

    public void setShape(Segment shape) {
        this.shape = shape;
    }
    
    public void setDocument(Document parentDocument) {
        this.parentDocument = parentDocument;
    }

    public Element convert() {
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "line");
        result.setAttributeNS(null, "x1", SvgFormatting.formatCoordinate(shape.getBeginPoint().getX()));
        result.setAttributeNS(null, "y1", SvgFormatting.formatCoordinate(shape.getBeginPoint().getY()));
        result.setAttributeNS(null, "x2", SvgFormatting.formatCoordinate(shape.getEndPoint().getX()));
        result.setAttributeNS(null, "y2", SvgFormatting.formatCoordinate(shape.getEndPoint().getY()));
        
        StringBuilder color = new StringBuilder();
        if(shape.getColor() != null){
            color.append("rgb(");
            color.append(shape.getColor().getRed());
            color.append(", ");
            color.append(shape.getColor().getGreen());
            color.append(", ");
            color.append(shape.getColor().getBlue());
            color.append(")");
        }
        else{
            color.append("rgb(");
            color.append(0);
            color.append(", ");
            color.append(0);
            color.append(", ");
            color.append(0);
            color.append(")");
        }
        
        result.setAttributeNS(null, "stroke", color.toString());
        result.setAttributeNS(null, "stroke-width", String.valueOf(shape.getStrokeWidth()));
 
        if(shape.getStrokeStyle() != null){
            result.setAttribute("style", shape.getStrokeStyle());
        }
        
        if (shape.getLineCap() == LineCap.Round) {
            result.setAttribute("stroke-linecap", "round");
        } else if (shape.getLineCap() == LineCap.Square) {
            result.setAttribute("stroke-linecap", "square");
        }
        
        if(shape.isDashed()){
            result.setAttribute("stroke-dasharray",String.valueOf(shape.getDashLength()));
        }  

        if(shape.getShapeRendering() != null){
            result.setAttribute("shape-rendering", String.valueOf(shape.getShapeRendering()));
        }        

        return result;
    }
    
}
