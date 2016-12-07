package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.Point;

public class RectangleProcessor extends CommonRectangleProcessor {
    
    public RectangleProcessor() {
    }
    
    public void process(){
 
        ShapeBuilderConfiguration resultingConfiguration = null;
        SplineConfiguration configuration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        ParsedElement rectangle = getElement();
        configuration = new SplineConfiguration(getRectanglePoints(), true);
                        
        if(isShadowed()){
            List<Point> shadowRectanglePoints = getShadowRectangle(getRectanglePoints(), center, majorAxisEnd, minorAxisEnd);
            Area shadowArea = convertRectangleToArea(shadowRectanglePoints);
            Area rectangleArea = convertRectangleToArea(getRectanglePoints());
            shadowArea.subtract(rectangleArea);
            SplineConfiguration shadowConfig = new SplineConfiguration(createConfigurationFromRectangleArea(shadowArea));
            ((SplineConfiguration) shadowConfig).setColor(getColor().fadeRGB(getColor(), CDXMLEnvironment.getInstance().getShadowRatio()));
            ((SplineConfiguration) shadowConfig).setFill(true);
            innerShapes.add(shadowConfig);
        }
        
        if(isFaded()){
            SplineConfiguration fadeConfig = new SplineConfiguration(getRectanglePoints(), true);
            ((SplineConfiguration) fadeConfig).setColor(getFadedColor());
            ((SplineConfiguration) fadeConfig).setFill(true);
            innerShapes.add(fadeConfig);
        }
        
        if(isShaded()){
            SplineConfiguration fadeConfig = new SplineConfiguration(getRectanglePoints(), true);
            ((SplineConfiguration) fadeConfig).setColor(getColor());
            ((SplineConfiguration) fadeConfig).setShaded(true);
            ((SplineConfiguration) fadeConfig).setGradient(
                    RadialGradient.getOvalGradient(getElement().getId(), getColor()));
            ((SplineConfiguration) fadeConfig).setId(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE);
            innerShapes.add(fadeConfig);
        }
        
        if(!isFilled()){
            ((SplineConfiguration) configuration).setStrokeWidth(getWidth());
        }
        
        ((SplineConfiguration) configuration).setColor(getColor());
        ((SplineConfiguration) configuration).setDashed(isDashed());
        ((SplineConfiguration) configuration).setDashLength(getHashSpacing());
        ((SplineConfiguration) configuration).setFill(isFilled());
        innerShapes.add(configuration);
        
        resultingConfiguration = new CompositeShapeConfiguration("Rectangle", innerShapes);
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(zOrder);
        setResultingConfiguration(resultingConfiguration);
        
    }
    
    protected List<SegmentConfiguration> createConfigurationFromRectangleArea(Area bondArea){
        PathIterator areaSegments = bondArea.getPathIterator(null);
        List<SegmentConfiguration> result = new ArrayList();
        while(!areaSegments.isDone()){
            double[] currentSegment = new double[6];
            double[] lastPoint = new double[2];
            int type = areaSegments.currentSegment(currentSegment);
            if(type == areaSegments.SEG_LINETO || type == areaSegments.SEG_MOVETO){
                Point segmentPointBegin;
                Point segmentPointEnd;
                lastPoint[0] = currentSegment[0];
                lastPoint[1] = currentSegment[1];
                segmentPointBegin = new Point(lastPoint[0], lastPoint[1]);
                segmentPointEnd = new Point(currentSegment[0], currentSegment[1]);
                result.add(new SegmentConfiguration(segmentPointBegin, segmentPointEnd));
            }
            areaSegments.next();
        }
        return result;
    }
}
