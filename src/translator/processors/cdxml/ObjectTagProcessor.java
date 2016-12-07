
package translator.processors.cdxml;

import translator.ParsedElement;
import java.util.ArrayList;
import java.util.List;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.utils.Point;
import translator.ParseElementDefinition;
import translator.utils.Rectangle;

public class ObjectTagProcessor extends TextProcessor {
    ParsedElement objectTag;
    
    /** Creates a new instance of ObjectTagProcessor */
    public ObjectTagProcessor() {
    }
    
    protected void cleanup() {
        super.cleanup();
    }
    
    protected void process() {
        List<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        objectTag = getElement();            
                       
        for (ParsedElement text : objectTag.getElements()){                
            setElement(text);
            cleanup();                
            super.process();
            innerShapes.add(resultingConfiguration);            
        }
        resultingConfiguration = new CompositeShapeConfiguration("ObjectTag", innerShapes);
        ((CompositeShapeConfiguration) resultingConfiguration).setZOrder(getZOrder());
    }
    
    protected translator.utils.Point getPosition(){
        Point result = null;
        ObjectTagCalculatingPosition calculationStrategy = ObjectTagCalculatingPosition.getInstance((ParsedElement)objectTag.getOwner());
        
        if (calculationStrategy == null){
            result = super.getPosition();
        }else{
            String positioningType = objectTag.getAttribute(ParseElementDefinition.OBJECT_TAG_POSITIONING_TYPE);
            if (positioningType.equalsIgnoreCase("auto") 
            && objectTag.hasAttribute(ParseElementDefinition.OBJECT_TAG_POSITIONING_OFFSET)){                    

                translator.utils.Rectangle indicatorBounds;
                translator.utils.Rectangle desiredBounds;

                ParsedElement text = getElement();
                List<ParsedElement> strings = text.getElements(ParseElementDefinition.STRING);
                String boundingBox = text.getAttribute(ParseElementDefinition.TEXT_BOUNDING_BOX);
                translator.utils.Rectangle initialIndicatorBounds = parseBoundingBox(boundingBox, text);

                indicatorBounds = initialIndicatorBounds;

                Double width = indicatorBounds.getRight() - indicatorBounds.getLeft();
                
                //GetObjectTagPosition is CDBond::GetTagRect in C++
                desiredBounds = calculationStrategy.getObjectTagPosition(width);

                String position = text.getAttribute(ParseElementDefinition.TEXT_POSITION);
                translator.utils.Point positionCoords = parseCoords(position, text);

                translator.utils.Point desiredBoundsTopLeft = new translator.utils.Point(desiredBounds.getLeft(), desiredBounds.getTop());
                translator.utils.Point indicatorBoundsTopLeft = new translator.utils.Point(indicatorBounds.getLeft(), indicatorBounds.getTop());

                result = (positionCoords.add(desiredBoundsTopLeft).subtract(indicatorBoundsTopLeft));
            }else{
                result = super.getPosition();
            }
        }
        return result;
    }  
}
