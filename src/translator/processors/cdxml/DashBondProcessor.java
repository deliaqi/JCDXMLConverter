package translator.processors.cdxml;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.JoinPointResult;
import translator.utils.Point;

public class DashBondProcessor extends BondProcessor{
    
    public DashBondProcessor() {        
    }
    
    protected void cleanup() {
        super.cleanup();
    }
    
    protected void initializeBondJoinPoints() {
        super.initializeBondJoinPoints();
        recalculateBondJoinPoints();
    }
    
    protected void process() {
        ShapeBuilderConfiguration resultingConfiguration;
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
            initializeBondJoinPoints();
           
            List<ShapeBuilderConfiguration> innerShapes = new ArrayList();
            
            String display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
            String display2 = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2);
            String position = bond.getAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION);            
            
            if ((display != null && display.equalsIgnoreCase(ParseElementDefinition.BOND_DISPLAY_DASH))
              && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY_2) 
              && (display != null && display.equalsIgnoreCase(ParseElementDefinition.BOND_DISPLAY_2_DASH))
              && doublePosition == CENTER_DOUBLE_POSITION
              && (getOrder().equals(ParseElementDefinition.BOND_ORDER_2) || getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5))){ 
                //When bond order is 2 or 1.5 and bond is centered call doubleBondProcess() to 
                //build both dashed lines.
                innerShapes.addAll(doubleBondProcess());
                
            } else {
       
                double newX1 = pointBeginCenter.getX();
                double newY1 = pointBeginCenter.getY();
                double newX2 = pointEndCenter.getX();
                double newY2 = pointEndCenter.getY();

                double endOffset = 0.0;
                double beginOffset = 0.0;
                double tempOffset = 0.0;

                // If the end of the bond is joined in needed to calculate the offset 
                // corresponding to the lenght of the mittering.
                if(getEnvironment().isEndJoined(bond)){                    
                    endOffset = Math.abs(GeometricOperations.distance(pointEndCenter, pointEndLeft) * Math.cos(GeometricOperations.angle(pointEndCenter, pointEndLeft)-GeometricOperations.angle(pointEndCenter, pointBeginCenter)));
                    tempOffset = Math.abs(GeometricOperations.distance(pointEndCenter, pointEndRight) * Math.cos(GeometricOperations.angle(pointEndCenter, pointEndRight)-GeometricOperations.angle(pointEndCenter, pointBeginCenter)));
                    if(tempOffset>endOffset){
                        endOffset = tempOffset;
                    }                    
                }
                
                // If the begin of the bond is joined in needed to calculate the offset 
                // corresponding to the lenght of the mittering.                
                if(getEnvironment().isBeginJoined(bond)){
                    beginOffset = Math.abs(GeometricOperations.distance(pointBeginCenter, pointBeginLeft) * Math.cos(GeometricOperations.angle(pointBeginCenter, pointBeginLeft)-GeometricOperations.angle(pointBeginCenter, pointEndCenter)));
                    tempOffset = Math.abs(GeometricOperations.distance(pointBeginCenter, pointBeginRight) * Math.cos(GeometricOperations.angle(pointBeginCenter, pointBeginRight)-GeometricOperations.angle(pointBeginCenter, pointEndCenter)));
                    if(tempOffset>beginOffset){
                        beginOffset = tempOffset;
                    }                    
                }
                
                Point newBeginBondPoint = GeometricOperations.offset(newX1, newY1, bondAngle, getHashSpacing()+beginOffset);                
                Point newEndBondPoint = GeometricOperations.offset(newX2, newY2, bondAngle, -getHashSpacing()-endOffset);

                GeometricLine beginBondLine = new GeometricLine("Begin Bond Line", 
                        new Point(newX1, newY1), new Point(newBeginBondPoint.getX(), newBeginBondPoint.getY()), lineWidth);

                GeometricLine endBondLine = new GeometricLine("End Bond Line", 
                        new Point(newEndBondPoint.getX(), newEndBondPoint.getY()), new Point(newX2, newY2), lineWidth);

                List<Point> beginBondPoints = new ArrayList();

                beginBondPoints.add(new Point(beginBondLine.getLeftEnd()));
                beginBondPoints.add(new Point(beginBondLine.getRightEnd()));
                beginBondPoints.add(pointBeginRight);
                beginBondPoints.add(pointBeginCenter);
                beginBondPoints.add(pointBeginLeft);

                innerShapes.add(new SplineConfiguration(beginBondPoints, true));

                List<Point> endBondPoints = new ArrayList();

                endBondPoints.add(new Point(endBondLine.getLeftBegin()));
                endBondPoints.add(new Point(endBondLine.getRightBegin()));
                endBondPoints.add(pointEndLeft);
                endBondPoints.add(pointEndCenter);
                endBondPoints.add(pointEndRight);

                double originalDistance = GeometricOperations.distance(newX1, newY1, newX2, newY2);

                double distance = originalDistance - (getHashSpacing() * 2) + getHashSpacing()-endOffset-beginOffset;
                int size = new Double(((distance - getHashSpacing()) * 0.50) / getHashSpacing()).intValue();
                if(distance / size * getHashSpacing() >= (distance - getHashSpacing()) / 2){
                    size++;
                }
                double offset = distance / size;

                if(distance > getHashSpacing() * 2){
                    innerShapes.add(new SplineConfiguration(endBondPoints, true));

                    Point newBeginPoint = GeometricOperations.offset(
                            beginBondLine.getEnd().getX(), beginBondLine.getEnd().getY(),
                            bondAngle, -(getHashSpacing() / 2));

                    innerShapes.addAll(calculateDashSegment(newBeginPoint, bondAngle, distance, size));
                }
             
                if (getOrder().equals(ParseElementDefinition.BOND_ORDER_2) || getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5)) {
                    //When bond order is 2 or 1.5 and bond is not centered call doubleBondProcess() to 
                    //build the second dashed line.
                    innerShapes.addAll(doubleBondProcess());
                }

            }
            
            List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(bond.getId());
            
            //If bond is overlapped subtract the intersected area.
            if(crossBonds != null){
                Area bondArea = new Area();
                bondArea = createBondAreasFromConfigurations(innerShapes);

                for(ParsedElement crossBond : crossBonds){
                    bondArea = crossBondProcess(bondArea, crossBond);
                }

                innerShapes = new ArrayList();
                innerShapes.add(createConfigurationFromArea(bondArea));
                
            }
            
            resultingConfiguration = new CompositeShapeConfiguration(
                    bond.getAttribute(ParseElementDefinition.BOND_DISPLAY), innerShapes);
            
            ((CompositeShapeConfiguration)resultingConfiguration).setFill(true);
            ((CompositeShapeConfiguration)resultingConfiguration).setColor(getColor());
            ((CompositeShapeConfiguration)resultingConfiguration).setZOrder(zOrder);
            
            setResultingConfiguration(resultingConfiguration);
        }
    }
    
}
