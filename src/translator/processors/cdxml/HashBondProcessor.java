package translator.processors.cdxml;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class HashBondProcessor extends BondProcessor {
    
    public HashBondProcessor() {
    }

    protected void process() {
        initializeBondJoinPoints();
        
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        
        double perpendicularAngle = bondAngle + (Math.PI / 2);
                
        boolean beginJoined = false;
        boolean endJoined = false;
        
        Point beginPoint = null;
        Point endPoint = null;
        
        double newX1 = pointBeginCenter.getX();
        double newY1 = pointBeginCenter.getY();
        double newX2 = pointEndCenter.getX();
        double newY2 = pointEndCenter.getY();
        
        double distance = GeometricOperations.distance(newX1, newY1, newX2, newY2);
        
        double endSpace = 0;
        
        double dashFraction = 1;
        
        double spaceFraction = 0;
        
        int numDashes = 1;
        
        if (distance > 0) {
            double dashLen = getLineWidth();
            numDashes = (int) Math.round((distance - dashLen + (getHashSpacing() / 2)) / getHashSpacing());
            dashFraction = dashLen / distance;
            
            if (numDashes <= 0) {
                numDashes = 1;
                dashFraction = 1;
            }
            
            if (numDashes > 1) {
                spaceFraction = (distance - numDashes * dashLen) / (numDashes - 1) / distance;
            }
        }
        
        if(getEnvironment().isBeginJoined(getElement())){
            if(!beginMarginAdded){
                beginPoint = GeometricOperations.offset(newX1, newY1, bondAngle, distance * (dashFraction + spaceFraction));
                numDashes--;
            }
            else{
                beginPoint = new Point(newX1, newY1);
            }
            beginJoined = true;
        }
        else{
            beginPoint = new Point(newX1, newY1);
        }
        
        if(getEnvironment().isEndJoined(getElement())){
            if(!endMarginAdded){
                endPoint = GeometricOperations.offset(newX2, newY2, bondAngle - Math.PI, distance * (dashFraction + spaceFraction));
                numDashes--;
            }else{
                endPoint = new Point(newX2, newY2);
            }
            endJoined = true;
        } else{
            
            endPoint = new Point(newX2, newY2);
        }
        
        
        for (int i = 0; i < numDashes; i++) {
            Point dashNearEnd = GeometricOperations.offset(
                    beginPoint.getX(), beginPoint.getY(), bondAngle,
                    distance * i * (dashFraction + spaceFraction));
            
            Point dashFarEnd = GeometricOperations.offset(
                    beginPoint.getX(), beginPoint.getY(), bondAngle,
                    distance * ((i + 1) * dashFraction + i * spaceFraction));
            
            Point dashCenter = new Point(
                    (dashNearEnd.getX() + dashFarEnd.getX()) / 2,
                    (dashNearEnd.getY() + dashFarEnd.getY()) / 2);
            
            Point leftPoint = GeometricOperations.offset(
                    dashCenter.getX(), dashCenter.getY(), perpendicularAngle, getBoldWidth() / 2);
            
            Point rightPoint = GeometricOperations.offset(
                    dashCenter.getX(), dashCenter.getY(), perpendicularAngle, -getBoldWidth() / 2);
            
            SegmentConfiguration line =
                    new SegmentConfiguration(leftPoint, rightPoint);
            
            line.setColor(getColor());
            line.setStrokeWidth(getLineWidth());
            innerShapes.add(line);
        }
        
        CompositeShapeConfiguration resultingConfiguration;
        
        List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(getElement().getId());
        if(crossBonds != null){
            Area bondArea = createBondAreasFromConfigurations(innerShapes);
            
            for(ParsedElement crossBond : crossBonds){
                bondArea = crossBondProcess(bondArea, crossBond);
            }
            
            resultingConfiguration = createConfigurationFromArea(bondArea);
            resultingConfiguration.setColor(getColor());
            resultingConfiguration.setFill(true);
            resultingConfiguration.setZOrder(zOrder);
        } else{
            resultingConfiguration = new CompositeShapeConfiguration("Hash Bond", innerShapes);
            resultingConfiguration.setZOrder(zOrder);
        }
        setResultingConfiguration(resultingConfiguration);
    }
    
}
