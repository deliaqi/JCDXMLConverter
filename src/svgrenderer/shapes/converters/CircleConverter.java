package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.graphics.shapes.Circle;

public class CircleConverter extends GraphicConverter<Circle> {
    
    public CircleConverter() {
    }
    
    public Element convert() {
        Element circleElement = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "circle");
        circleElement.setAttribute("cx", SvgFormatting.formatCoordinate(shape.getCenter().getX()));
        circleElement.setAttribute("cy", SvgFormatting.formatCoordinate(shape.getCenter().getY()));
        circleElement.setAttribute("r", String.valueOf(shape.getRadius()));
        
        StringBuilder color = buildColor(shape.getColor());
        StringBuilder fillColor;
        if (shape.getFillColor() == null) {
            fillColor = color;
        } else {
            fillColor = buildColor(shape.getFillColor());
        }
        
        circleElement.setAttribute("stroke", color.toString());
        
        if (shape.getStrokeWidth() != 0) {
            circleElement.setAttribute("stroke-width", String.valueOf(shape.getStrokeWidth()));
        } else {
            circleElement.setAttribute("stroke-width", String.valueOf(ShapeConverterConstants.DEFAULT_STROKE_WIDTH));
        }
        
        Element result;
        
        if (shape.isShaded()) {
            // make result be a group to hold gradient and shape
            result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "g");
            
            Element shadedGradient = createRadialGradientDefinition();
            result.appendChild(shadedGradient);
            
            // reference the created gradient
            circleElement.setAttribute("fill", "url(#" + getDefaultGradientId() + ")");
            result.appendChild(circleElement);
        } else {
            // make result be the circle element
            result = circleElement;
            if(shape.isFill()){
                circleElement.setAttribute("fill", fillColor.toString());
            } else {
                circleElement.setAttribute("fill", "none");
            }
        }
        if(shape.isDashed()){
            circleElement.setAttribute("stroke-dasharray",String.valueOf(shape.getDashLength()));
        }
        
        return result;
    }

}
