package svgrenderer.shapes.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import translator.graphics.shapes.gradients.GradientStop;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.graphics.Color;
import translator.graphics.shapes.Arc;
import translator.graphics.shapes.CubicCurve;
import translator.graphics.shapes.QuadraticCurve;
import translator.graphics.shapes.Segment;
import translator.graphics.shapes.Spline;
import translator.graphics.shapes.builders.configurations.FillRule;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.LineCap;

public class SplineConverter extends GraphicConverter<Spline> {
    
    public SplineConverter() {
    }

    public Element convert() {
        Element pathElement = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "path");
        
        StringBuilder pathDescription = new StringBuilder();
        boolean first = true;
        for(Segment currentSegment : shape.getShapes()){
            if (first) {
                pathDescription.append("M");
                pathDescription.append(SvgFormatting.formatCoordinate(currentSegment.getBeginPoint().getX()));
                pathDescription.append(" ");
                pathDescription.append(SvgFormatting.formatCoordinate(currentSegment.getBeginPoint().getY()));
                pathDescription.append(" ");
                first = false;
            }
            pathDescription.append(getAction(currentSegment));
        }        
        
        if (shape.isClosed()) {
            pathDescription.append("Z");
        }
        pathElement.setAttribute("d", pathDescription.toString());
        
        StringBuilder color = buildColor(shape.getColor());
        
        StringBuilder fillColor = null;
        if (shape.getFillColor() != null) {
            fillColor = buildColor(shape.getFillColor());
        }
        
        pathElement.setAttribute("stroke", color.toString());
        if (shape.getStrokeWidth() != 0) {
            pathElement.setAttribute("stroke-width", String.valueOf(shape.getStrokeWidth()));
        } else {
            pathElement.setAttribute("stroke-width", String.valueOf(ShapeConverterConstants.DEFAULT_STROKE_WIDTH));
        }
        
        if(shape.getFillRule() == FillRule.EvenOdd){
            pathElement.setAttribute("fill-rule", "evenodd");
        }
        
        if (shape.getLineCap() == LineCap.Round) {
            pathElement.setAttribute("stroke-linecap", "round");
        } else if (shape.getLineCap() == LineCap.Square) {
            pathElement.setAttribute("stroke-linecap", "square");
        }
        
        if(shape.isDashed()){
            pathElement.setAttribute("stroke-dasharray",String.valueOf(shape.getDashLength()));
        }        
        
        if(shape.isTranslate()){
            pathElement.setAttribute("transform", "translate("
                    +SvgFormatting.formatCoordinate(shape.getMoveX()) + "," 
                    +SvgFormatting.formatCoordinate(shape.getMoveY()) +")");
        }
        
        if(shape.getLineJoin() == LineJoin.Round){
            pathElement.setAttribute("stroke-linejoin", "round");
        } else if(shape.getLineJoin() == LineJoin.Miter){
            pathElement.setAttribute("stroke-linejoin", "miter");
            
            //The value of miterlimit must be a number greater than or equal to 1. Any other value is an error 
            if(shape.getMiterLimit() >= 1){
                pathElement.setAttribute("stroke-miterlimit", String.valueOf(shape.getMiterLimit()));
            }
        } else {
            pathElement.setAttribute("stroke-linejoin", "bevel");
        }
        
        Element result;
        if(shape.isShaded()){
            // create a group that holds the gradient and the path
            result = parentDocument.createElementNS(parentDocument.getNamespaceURI(), "g");
            Element shadedGradient = createRadialGradientDefinition();
            result.appendChild(shadedGradient);
            
            // set fill to reference shaded gradient
            pathElement.setAttribute("fill", "url(#" + getDefaultGradientId() + ")");
            result.appendChild(pathElement);
        } else if(shape.isFill()){
            if (fillColor != null) {
                pathElement.setAttribute("fill", fillColor.toString());
            } else {
                pathElement.setAttribute("fill", color.toString());
            }
            // make result be the path element
            result = pathElement;
        } else {
            pathElement.setAttribute("fill", "none");
            // make result be the path element
            result = pathElement;
        }

        pathElement.setAttribute("shape-rendering", "geometricPrecision");
        
        return result;
    }
    
    private String getAction(Segment segment) {
        String action;
        StringBuilder result = new StringBuilder();        
        if (segment instanceof Arc) {
            Arc arcedSegment = (Arc) segment;
            result.append("A");
            result.append(arcedSegment.getXRadius());
            result.append(",");
            result.append(arcedSegment.getYRadius());
            result.append(" ");
            result.append(arcedSegment.getAngle());
            result.append(" ");
            result.append((arcedSegment.isLargeArc()) ? "1" : "0");
            result.append(",");
            result.append((arcedSegment.isSweepPositive()) ? "1" : "0");
            result.append(" ");
        }
        else if (segment instanceof CubicCurve) {
            CubicCurve cubicSegment = (CubicCurve) segment;
            result.append("C");
            result.append(SvgFormatting.formatCoordinate(cubicSegment.getControlPoint().getX()));
            result.append(" ");
            result.append(SvgFormatting.formatCoordinate(cubicSegment.getControlPoint().getY()));
            result.append(" ");
            result.append(SvgFormatting.formatCoordinate(cubicSegment.getControlPoint2().getX()));
            result.append(" ");
            result.append(SvgFormatting.formatCoordinate(cubicSegment.getControlPoint2().getY()));
            result.append(" ");
        }
        else if (segment instanceof QuadraticCurve) {
            QuadraticCurve quadraticSegment = (QuadraticCurve) segment;
            result.append("Q");
            result.append(SvgFormatting.formatCoordinate(quadraticSegment.getControlPoint().getX()));
            result.append(" ");
            result.append(SvgFormatting.formatCoordinate(quadraticSegment.getControlPoint().getY()));
            result.append(" ");
        }
        else {
            if(segment.isMoveTo()){
                result.append("M");
            }
            else {
                result.append("L");
            }
        }
        result.append(SvgFormatting.formatCoordinate(segment.getEndPoint().getX()));
        result.append(" ");
        result.append(SvgFormatting.formatCoordinate(segment.getEndPoint().getY()));
        result.append(" ");
        
        return result.toString();
    }
}
