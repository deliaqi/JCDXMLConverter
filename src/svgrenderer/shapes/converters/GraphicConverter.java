
package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.graphics.Color;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.gradients.GradientStop;

/**
 * Base class for converters that need gradients
 */
public abstract class GraphicConverter<S extends Shape> implements ShapeConverter<S> {
    
    protected Document parentDocument;
    protected S shape;
    
    public GraphicConverter() {
    }
    
    public void setShape(S shape) {
        this.shape = shape;
    }

    public void setDocument(Document document) {
        this.parentDocument = document;
    }
    
    /**
     * Returns a <code>StringBuilder</code> that can be used to write a
     * text representation of the given <code>Color</code>.
     */
    protected StringBuilder buildColor(Color color) {
        StringBuilder result = new StringBuilder();
        if(color != null){
            result.append("rgb(");
            result.append(color.getRed());
            result.append(", ");
            result.append(color.getGreen());
            result.append(", ");
            result.append(color.getBlue());
            result.append(")");
        }
        else{
            result.append("rgb(");
            result.append(0);
            result.append(", ");
            result.append(0);
            result.append(", ");
            result.append(0);
            result.append(")");
        }
        return result;
    }
    
    /**
     * Creates a gradient definition for the document, with the shape's fill color.
     */
    protected Element createRadialGradientDefinition() {
        // gradients need to be inside a defs element
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "defs");
        Color fillColor = shape.getFillColor();
        if (fillColor == null) {
            fillColor = shape.getColor(); // use color if no different fill color is specified
        }
        RadialGradient shadedGradient = (RadialGradient)shape.getGradient();
        result.appendChild(createDOMElement(shadedGradient, parentDocument));
        return result;
    }
    
    /**
    * Creates an XML DOM element for the radial gradient, in the specified DOM document.
    */
    public Element createDOMElement(RadialGradient gradient,  Document parentDocument) {
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "radialGradient");
        result.setAttribute("id", getDefaultGradientId());
        result.setAttribute("gradientUnits", "objectBoundingBox");
        result.setAttribute("cx", SvgFormatting.formatCoordinate(gradient.getCenter().getX()));
        result.setAttribute("cy", SvgFormatting.formatCoordinate(gradient.getCenter().getY()));
        result.setAttribute("r", String.valueOf(gradient.getRadius()));
        result.setAttribute("fx", SvgFormatting.formatCoordinate(gradient.getFocus().getX()));
        result.setAttribute("fy", SvgFormatting.formatCoordinate(gradient.getFocus().getY()));
        
        for (GradientStop stop : gradient.getStops()) {
            Element stopElement = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "stop");
            stopElement.setAttribute("offset", String.valueOf(stop.getOffset()) + "%");
            
            StringBuilder colorBuilder = new StringBuilder();
            colorBuilder.append("rgb(");
            colorBuilder.append(stop.getColor().getRed());
            colorBuilder.append(", ");
            colorBuilder.append(stop.getColor().getGreen());
            colorBuilder.append(", ");
            colorBuilder.append(stop.getColor().getBlue());
            colorBuilder.append(")");
            stopElement.setAttribute("stop-color", colorBuilder.toString());
            
            result.appendChild(stopElement);
        }
        
        return result;
    }
    
    /**
     * Creates the id string for the shape's default gradient
     */
    protected String getDefaultGradientId() {
        return RadialGradient.DEFAULT_SHADED_GRADIENT_ID + shape.getGradient().getId();
    }
    
}
