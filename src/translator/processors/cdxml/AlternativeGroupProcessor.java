package translator.processors.cdxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.RectangleConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.Point;

public class AlternativeGroupProcessor extends CDXMLProcessor {

    private Point beginTextFramePoint;
    private Point endTextFramePoint;
    
    private Point beginGroupFramePoint;
    private Point endGroupFramePoint;
    
    public AlternativeGroupProcessor() {
    }

    /**
     * Build a named alternative group base on the following attributes: 
     * Title
     * Group Frame Box 
     * Text Frame Box and 
     * contained fragments
     */
    protected void process() {
        Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
        
        //Create the table
        RectangleConfiguration rectangle = new RectangleConfiguration(
                beginTextFramePoint, endGroupFramePoint);
        
        configurations.add(rectangle);
        
        SegmentConfiguration line = new SegmentConfiguration(
                beginGroupFramePoint, 
                new Point(endGroupFramePoint.getX(), endTextFramePoint.getY()));
        line.setStrokeWidth(getLineWidth());
        
        configurations.add(line);
        
        CompositeShapeConfiguration resultingConfiguration = new CompositeShapeConfiguration(
                ParseElementDefinition.ALTERNATIVE_GROUP, configurations);
        
        resultingConfiguration.setColor(getColor());
        resultingConfiguration.setZOrder(getZOrder());
        
        setResultingConfiguration(resultingConfiguration);
    }

    protected void configure() {
        super.configure();
        
        List<Point> textFrame = parsePoints(
                getElement().getAttribute(ParseElementDefinition.ALTERNATIVE_GROUP_TEXT_FRAME),
                getElement());
        
        List<Point> groupFrame = parsePoints(
                getElement().getAttribute(ParseElementDefinition.ALTERNATIVE_GROUP_GROUP_FRAME),
                getElement());
        
        beginTextFramePoint = textFrame.get(FIRST_ELEMENT);
        endTextFramePoint = textFrame.get(SECOND_ELEMENT);
        
        beginGroupFramePoint = groupFrame.get(FIRST_ELEMENT);
        endGroupFramePoint = groupFrame.get(SECOND_ELEMENT);
    }

    protected void cleanup() {
        super.cleanup();
    }
}
