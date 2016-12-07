package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.QuadraticCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.utils.Point;

public abstract class CommonCircleProcessor extends DrawingElementProcessor {
    
    private List<Point> ovalPoints = new ArrayList();;
    protected String ovalType;
    protected String orbitalType;
    
    public CommonCircleProcessor() {
    }
    
    protected void cleanup() {
        ovalPoints = null;
        ovalType = null;
        environment = null;
        orbitalType = null;
        super.cleanup();
    }
    
    protected  void configure(){
        super.configure();
        
        ParsedElement oval = getElement();
        
        if(oval.hasAttribute(ParseElementDefinition.GRAPHIC_LINE_WIDTH)){
            setLineWidth(Double.parseDouble(oval.getAttribute(ParseElementDefinition.GRAPHIC_LINE_WIDTH)));
        }else{
            setLineWidth(getEnvironment().getLineWidth());
        }
        if(oval.hasAttribute(ParseElementDefinition.GRAPHIC_BOLD_WIDTH)){
            setBoldWidth(Double.parseDouble(oval.getAttribute(ParseElementDefinition.GRAPHIC_BOLD_WIDTH)));
        }else{
            setBoldWidth(getEnvironment().getBoldWidth());
        }

        // might be orbital, so retrieve type
        if (oval.hasAttribute(ParseElementDefinition.GRAPHIC_ORBITAL_TYPE)) {
            orbitalType = oval.getAttribute(ParseElementDefinition.GRAPHIC_ORBITAL_TYPE);
        }
        if(boundingBox != null){
            ovalPoints = parsePoints(boundingBox, oval);
        }
    }
    
    protected String getTypeString() {
        if(ovalType == null){            
            setOvalType(getElement().getAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE));            
        }
        
        return ovalType;
    }
    
    protected Area convertOvalToArea(double x, double y, double radius){
        return convertOvalToArea(x, y, radius, radius);
    }
    
    protected Area convertOvalToArea(double x, double y, double radiusX, double radiusY){
        GeneralPath curvePath = new GeneralPath(new Ellipse2D.Double(x, y, radiusX, radiusY));
        return new Area(curvePath);
    }
    
    public List<Point> getOvalPoints() {
        return ovalPoints;
    }
    
    public void setOvalPoints(List<Point> ovalPoints) {
        this.ovalPoints = ovalPoints;
    }
    
    public String getOvalType() {
        return ovalType;
    }
    
    public void setOvalType(String ovalType) {
        this.ovalType = ovalType;
    }
}
