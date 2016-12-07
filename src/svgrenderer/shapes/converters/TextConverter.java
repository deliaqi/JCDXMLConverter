package svgrenderer.shapes.converters;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.graphics.StyleElement;
import translator.graphics.shapes.CompositeText;
import translator.graphics.shapes.Text;

public class TextConverter implements ShapeConverter<CompositeText> {
    
    private static double FONT_SIZE_PERCENT = 0.75;
    public static  String DISPLAY_NONE = "none";
    
    public CompositeText shape;
    public Document parentDocument;
    
    public TextConverter() {
    }
    
    public void setShape(CompositeText shape) {
        this.shape = shape;
    }
    
    public void setDocument(Document document) {
        this.parentDocument = document;
    }
    
    public Element convert() {
        Element result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "g");
        Element text = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "text");
        
        text.setAttribute("xml:space", "preserve");
        
        boolean shadow = false;
        boolean outline = false;
        boolean wasTranslated = false;
        
        if(shape.getJustification() == Text.LEFT_JUSTIFICATION){
            text.setAttribute("text-anchor", "start");
        } else if(shape.getJustification() == Text.RIGHT_JUSTIFICATION){
            text.setAttribute("text-anchor", "end");
        } else if(shape.getJustification() == Text.CENTER_JUSTIFICATION){
            text.setAttribute("text-anchor", "middle");
        }
        
        text.setAttribute("text-rendering", "geometricPrecision");
        if (!shape.isDisplay()) {
            text.setAttribute("display", DISPLAY_NONE);
        }
        
        if (shape.getLines().size() > 0) {
            
            
            if(shape.getRotationAngle() != 0){
                Text firstLine = shape.getLines().get(0);
                text.setAttribute("transform", "translate("
                        + SvgFormatting.formatCoordinate(firstLine.getPoint().getX()) + ","
                        + SvgFormatting.formatCoordinate(firstLine.getPoint().getY()) + "), "
                        + "rotate(" + shape.getRotationAngle() + "), "
                        + "translate("
                        + SvgFormatting.formatCoordinate(-firstLine.getPoint().getX()) + ","
                        + SvgFormatting.formatCoordinate(-firstLine.getPoint().getY()) + ")");
            }
        }
        
        for (Text line : shape.getLines()) {
            Element lineElement = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "tspan");
            
            // this will be used when there is shadow
            Element lineShadowElement = null;
            
            lineElement.setAttribute("x", SvgFormatting.formatCoordinate(line.getPoint().getX()));
            lineElement.setAttribute("y", SvgFormatting.formatCoordinate(line.getPoint().getY()));
            
            //Word-spacing: this value indicates inter-word space in addition to the default space between words.
            if (shape.getJustification() == Text.FULL_JUSTIFICATION) {
                lineElement.setAttribute("word-spacing", String.valueOf(line.getWordSpacing()));
            }
            
            int substringIndex = 0;
            for (String substring : line.getValues()) {
                Element subTextElement = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "tspan");
                
                String partId = String.valueOf(substringIndex++);
                
                Font partFont = line.getFont(partId);
                
                subTextElement.setAttribute("font-family", partFont.getName());
                
                double fontSize = Double.parseDouble(partFont.getSize());
                
                Color partColor = line.getColor(partId);
                
                StringBuilder color = new StringBuilder();
                color.append("rgb(");
                color.append(partColor.getRed());
                color.append(", ");
                color.append(partColor.getGreen());
                color.append(", ");
                color.append(partColor.getBlue());
                color.append(")");
                
                StyleElement style = partFont.getStyle();
                if(style.isBold()){
                    subTextElement.setAttribute("font-weight", "bold");
                }
                if(style.isItalic()){
                    subTextElement.setAttribute("font-style", "italic");
                }
                if(style.isUnderlined()){
                    subTextElement.setAttribute("text-decoration", "underline");
                }
                if(style.isOutlined()){
                    outline = true;
                }
                if(style.isShadowed()){
                    shadow = true;
                }
                if(style.isSubscript()){
                    double dy = partFont.getAscentPixel() * - Font.BASE_LINE_SUBSCRIPT_PERCENT;
                    
                    subTextElement.setAttribute("baseline-shift", Double.toString(dy));
                    fontSize *= FONT_SIZE_PERCENT;
                }
                if(style.isSuperscript()){
                    double dy = partFont.getAscentPixel() * Font.BASE_LINE_SUPERSCRIPT_PERCENT;
                                        
                    subTextElement.setAttribute("baseline-shift", Double.toString(dy));
                    fontSize *= FONT_SIZE_PERCENT;
                }
                
                subTextElement.setAttribute("font-size", Double.toString(fontSize));
                
                if(outline){
                    subTextElement.setAttribute("fill", "none");
                    subTextElement.setAttribute("stroke", color.toString());
                    subTextElement.setAttribute("stroke-width", "0.1");
                } else{
                    subTextElement.setAttribute("fill", color.toString());
                }
                
                if(shadow){
                    if (lineShadowElement == null) {
                        lineShadowElement = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "tspan");
                        lineShadowElement.setAttribute("x", SvgFormatting.formatCoordinate(line.getPoint().getX() + 0.5));
                        lineShadowElement.setAttribute("y", SvgFormatting.formatCoordinate(line.getPoint().getY() + 0.5));
                    }
                    
                    Element shadowText = (Element)subTextElement.cloneNode(true);
                    shadowText.removeAttribute("fill");
                    
                    if(outline){
                        shadowText.setAttribute("fill", "none");
                        shadowText.setAttribute("stroke", "gray");
                        shadowText.setAttribute("stroke-width", "0.1");
                    } else{
                        shadowText.setAttribute("fill", "gray");
                    }
                    
                    org.w3c.dom.Text shadowContentElement = parentDocument.createTextNode(substring);
                    shadowText.appendChild(shadowContentElement);
                    
                    
                    lineShadowElement.appendChild(shadowText);
                    
                    shadow = false;
                }
                
                outline = false;
                
                org.w3c.dom.Text textContentElement = parentDocument.createTextNode(substring);
                subTextElement.appendChild(textContentElement);
                
                lineElement.appendChild(subTextElement);
            }
            
            if (lineShadowElement != null) {
                text.appendChild(lineShadowElement);
            }
            
            text.appendChild(lineElement);
        }
        
        result.appendChild(text);
        
        return result;
    }
}
