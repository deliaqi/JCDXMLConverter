package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.Ellipse;

public class EllipseConverter implements ShapeConverter<Ellipse> {
    
    private Ellipse shape;
    private Document parentDocument;
        
    public EllipseConverter() {
    }
    
    public void setShape(Ellipse shape) {
        this.shape = shape;
    }
    
    public void setDocument(Document document) {
        this.parentDocument = document;
    }
    
    public Element convert() {
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "ellipse");
        result.setAttribute("cx", SvgFormatting.formatCoordinate(shape.getCenter().getX()));
        result.setAttribute("cy", SvgFormatting.formatCoordinate(shape.getCenter().getY()));
        result.setAttribute("rx", SvgFormatting.formatCoordinate(shape.getRadiusX()));
        result.setAttribute("ry", SvgFormatting.formatCoordinate(shape.getRadiusY()));
        
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
        
        result.setAttribute("stroke", color.toString());
        
        if (shape.getStrokeWidth() != 0) {
            result.setAttribute("stroke-width", String.valueOf(shape.getStrokeWidth()));
        } else {
            result.setAttribute("stroke-width", String.valueOf(ShapeConverterConstants.DEFAULT_STROKE_WIDTH));
        }
        
        if(shape.isFill()){
            result.setAttribute("fill", color.toString());
        }        
        else {
            result.setAttribute("fill", "none");
        }
        
        if(shape.isDashed()){
            result.setAttribute("stroke-dasharray", String.valueOf(shape.getDashLength()));
        }
        
        result.setAttribute("transform", "rotate("+String.valueOf(shape.getRotationAngle())+"" +
                " "+SvgFormatting.formatCoordinate(shape.getCenter().getX())+" "+SvgFormatting.formatCoordinate(shape.getCenter().getY())+")");
        
        return result;        
    }
    
}
