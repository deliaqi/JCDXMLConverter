
package translator.processors.cdxml;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import translator.ParsedElement;
import translator.Translator;
import translator.utils.Rectangle;
import translator.utils.Line;
import translator.utils.Point;
import translator.utils.GeometricOperations;
import translator.ParseElementDefinition;

/*
 * Class which has the strategy to get an instance of an inner class according the ObjectTag's owner
 * Each inner class has a different version of the method GetObjectTagPosition to get the relative position of the objectTag
 **/
public abstract class ObjectTagCalculatingPosition {    
    ParsedElement element;
    private static Hashtable<String, ObjectTagCalculatingPosition> calculatingPosition = new Hashtable();
    
    /** Creates a new instance of ObjectTagCalculatingPosition */
    public ObjectTagCalculatingPosition() {
    }
    
    /**
     *return an instance of the corresponding inner class
     */
    public static ObjectTagCalculatingPosition getInstance(ParsedElement element){
        ObjectTagCalculatingPosition result = null;
        if (element != null){
            result = calculatingPosition.get(element.getName());
            if(result != null){
                result.setElement(element);
            }            
        }
        
        return result;
    }
    
    public abstract translator.utils.Rectangle getObjectTagPosition(double width);
    
    /*
     * Return a rectangle of a tag according the text alignment
     */
    public translator.utils.Rectangle getTagRect(double wid, double theta, translator.utils.Rectangle rectangle){
        translator.utils.Rectangle result = new Rectangle();            
        translator.utils.Point point = new translator.utils.Point(rectangle.center());
        // Add a couple pixels of slop to each dimension
            
        double curFontSize = 2 * translator.graphics.Font.SLOP + translator.graphics.Font.DEFAULT_VALUE_CAP_HEIGHT;                        
        int curToler = (int)(curFontSize / 3 + 1 * translator.graphics.Font.SLOP);
            
        double height = rectangle.getBottom() - rectangle.getTop();
        double width = rectangle.getRight() - rectangle.getLeft();

        double cornerTheta = Math.atan2(height, width) + Math.PI / 12;

        if (theta <= cornerTheta || theta > 2 * Math.PI - cornerTheta){ // To the right
            result.setLeft(rectangle.getRight() + Math.cos(theta) * curToler - (1 - Math.cos(theta)) * wid + translator.graphics.Font.SLOP);
            result.setTop(point.getY() + Math.sin(theta) * curToler + (Math.abs(Math.cos(theta)) < 0.001 ? 0 : (Math.tan(theta) - 1) * curFontSize / 2));
        }else if (theta > cornerTheta && theta < Math.PI - cornerTheta){ // To the bottom
            result.setLeft(point.getX() + Math.cos(theta) * curToler - wid / 2);
            result.setTop(rectangle.getBottom() + Math.sin(theta) * curToler);
        }else if (Math.abs(Math.PI - theta) <= cornerTheta){ // To the left
            result.setLeft(rectangle.getLeft() + Math.cos(theta) * curToler + Math.cos(theta) * wid - translator.graphics.Font.SLOP);
            result.setTop(point.getY() + Math.sin(theta) * curToler - (Math.abs(Math.cos(theta)) < 0.001 ? 0 : (Math.tan(theta) + 1) * curFontSize / 2));
        }else{  // To the top
            result.setLeft(point.getX() + Math.cos(theta) * curToler - (Math.abs(Math.sin(theta)) < 0.001 ? 0 : (Math.cos(theta) / Math.sin(theta)) * curFontSize) - wid / 2);
            result.setTop(rectangle.getTop() + Math.sin(theta) * curToler - curFontSize + translator.graphics.Font.DEFAULT_VALUE_DESCENT);
            }
        result.setBottom(result.getTop() + curFontSize);
        result.setRight(result.getLeft() + wid);

        return result; 
    }
    
    public void setElement(ParsedElement element){
        this.element = element;
    }
    
    //this block has the calculatingPosition map
    static {
        calculatingPosition.put(ParseElementDefinition.SOLID_BOND, new ObjectTagRelatedBond());
    }
    
    /*
     *ObjectTagRelatedBond is the class to get the ObjectTag position relative to a bond
     */
    static class ObjectTagRelatedBond extends ObjectTagCalculatingPosition{
        public ObjectTagRelatedBond(){
            }
        
        /*
         * return the objectTag Position relative to a bond
         */
        public translator.utils.Rectangle getObjectTagPosition(double width){
            ParsedElement bond = this.element;
            translator.utils.Rectangle result = new translator.utils.Rectangle();
            Line bondCoords = BondProcessor.parseCoords(bond);
            double theta = 0.0;
            translator.utils.Point point = new translator.utils.Point((bondCoords.getBegin().add(bondCoords.getEnd())));
            point = point.byScalar(0.5);
            
            // angles to the atoms at either end of the bond                        
            double ang1 = Math.atan2(bondCoords.getEnd().getY() - point.getY(), bondCoords.getEnd().getX() - point.getX());
            double ang2 = Math.atan2(bondCoords.getBegin().getY() - point.getY(), bondCoords.getBegin().getX() - point.getX());
                
            List<Double> thetas = new ArrayList();
            thetas.add(ang1);
            thetas.add(ang1 + Math.PI / 30);
            thetas.add(ang1 - Math.PI / 30);
            thetas.add(ang2);
            thetas.add(ang2 + Math.PI / 30);
            thetas.add(ang2 - Math.PI / 30);

            ParsedElement[] beginAppendages = BondProcessor.findAppendages(bond, true);
            ParsedElement[] endAppendages = BondProcessor.findAppendages(bond, false);
            
            if (endAppendages.length < beginAppendages.length){
                thetas.add(ang1 + Math.PI / 30);
            }else if (endAppendages.length > beginAppendages.length){
                thetas.add(ang1 - Math.PI / 30);
            }

            theta = GeometricOperations.findLargestAngleAroundPoint(thetas, null);

            double iWid = 1.0;
            double jWid = 1.0;
            if (bond.hasAttribute(ParseElementDefinition.LINE_WIDTH)){
                iWid = Double.parseDouble(bond.getAttribute(ParseElementDefinition.LINE_WIDTH));
                jWid = Double.parseDouble(bond.getAttribute(ParseElementDefinition.LINE_WIDTH));
            }

            translator.utils.Rectangle rectangle = new translator.utils.Rectangle(point.getY(), point.getX(), point.getY(), point.getX());
            Double halfWid = (iWid + jWid) / 4;
            rectangle.inflate(halfWid * Math.max(Math.abs(Math.sin(ang1)), Math.abs(Math.cos(ang1))));	// inflate by half of average width results in a rect that is (average width) square

            result = getTagRect(width, theta, rectangle);
            return result;
        }
        
    }
}
