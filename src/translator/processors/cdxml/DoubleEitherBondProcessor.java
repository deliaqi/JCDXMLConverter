
package translator.processors.cdxml;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.JoinPointResult;
import translator.utils.Line;
import translator.utils.Point;

public class DoubleEitherBondProcessor extends BondProcessor {
    
    public DoubleEitherBondProcessor() {
    }
    
    /**
     * This method is used to initialize the points used for drawn 
     * bonds and can apply offsets to its nodes.
     */
    protected void initializeBondJoinPoints(){
        super.initializeBondJoinPoints();
        ParsedElement bond = getElement();
        
        JoinPointResult joinPointResultBegin = getEnvironment().getBondJoinPointBeginResult(bond.getId());
        JoinPointResult joinPointResultEnd = getEnvironment().getBondJoinPointEndResult(bond.getId());
        
        double margin = getEnvironment().getLineWidth() + getEnvironment().getMarginWidth();
        
        //The final begin position to draw is different that
        //the begin position indicated in the CDXML file
        if(!bondBegin.equals(pointBeginCenter)){
            if(joinPointResultBegin != null){
                pointBeginLeft = GeometricOperations.offset(
                        pointBeginCenter, bondAngle + Math.PI / 2, getLineWidth() / 2);
                pointBeginRight = GeometricOperations.offset(
                        pointBeginCenter, bondAngle - Math.PI / 2, getLineWidth() / 2);
            }
        }
        
        //The final end position to draw is different that
        //the end position indicated in the CDXML file
        if(!bondEnd.equals(pointEndCenter)){
            if(joinPointResultEnd != null){
                pointEndLeft = GeometricOperations.offset(
                        pointEndCenter, bondAngle - Math.PI / 2, getLineWidth() / 2);
                pointEndRight = GeometricOperations.offset(
                        pointEndCenter, bondAngle + Math.PI / 2, getLineWidth() / 2);
            }
        }
    }
    
    protected void process() {
        ShapeBuilderConfiguration resultingConfiguration = null;
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_END) &&
                bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) &&
                bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
            initializeBondJoinPoints();
            
            Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
            List<Point> bondPoints = new ArrayList();
            
            double offset = calculateBondSpacing(getBondSpacing(),
                    pointBeginCenter.getX(), pointBeginCenter.getY(),
                    pointEndCenter.getX(), pointEndCenter.getY());
            
            double angleOffset = Math.PI / 2;           
            
            if(doublePosition == CENTER_DOUBLE_POSITION){
                Point newBeginLeftPoint = GeometricOperations.offset(
                        pointBeginCenter.getX(), pointBeginCenter.getY(), 
                        bondAngle + angleOffset, offset / 2);
                
                Point newBeginRightPoint = GeometricOperations.offset(
                        pointBeginCenter.getX(), pointBeginCenter.getY(), 
                        bondAngle - angleOffset, offset / 2);
                
                Point newEndLeftPoint = GeometricOperations.offset(
                        pointEndCenter.getX(), pointEndCenter.getY(), 
                        bondAngle + angleOffset, offset / 2);
                
                Point newEndRightPoint = GeometricOperations.offset(
                        pointEndCenter.getX(), pointEndCenter.getY(), 
                        bondAngle - angleOffset, offset / 2);
                
                SegmentConfiguration segment1 = new SegmentConfiguration(
                        newBeginLeftPoint, newEndRightPoint);
                segment1.setStrokeWidth(lineWidth);
                
                SegmentConfiguration segment2 = new SegmentConfiguration(
                        newBeginRightPoint, newEndLeftPoint);
                segment2.setStrokeWidth(lineWidth); 
                
                innerShapes.add(segment1);
                innerShapes.add(segment2);
            }
            else{ 
                if(doublePosition == RIGHT_DOUBLE_POSITION){
                    angleOffset *= -1;
                }

                Point newBeginPoint = GeometricOperations.offset(
                        pointBeginCenter.getX(), pointBeginCenter.getY(), 
                        bondAngle + angleOffset, offset);
                
                Point newEndPoint = GeometricOperations.offset(
                        pointEndCenter.getX(), pointEndCenter.getY(), 
                        bondAngle + angleOffset, offset);
                
                List<ParsedElement> beginJoinedBonds = getEnvironment().findBeginJoinedBonds(bond);
                List<ParsedElement> endJoinedBonds = getEnvironment().findEndJoinedBonds(bond);
                
                Point offsetBeginDoubleBond;
                Point offsetEndDoubleBond;
                
                //if the double either has a bond atached at the begin or end, an offset needs to be done
                offsetBeginDoubleBond = calculateOffsetBeginDoubleBonds(newBeginPoint, newEndPoint, offset, true);
                offsetEndDoubleBond = calculateOffsetBeginDoubleBonds(newBeginPoint, newEndPoint, offset, false);
                
                if(offsetBeginDoubleBond != null && beginJoinedBonds.size() > 1){
                        newBeginPoint = offsetBeginDoubleBond;
                } 
                if(offsetEndDoubleBond != null && endJoinedBonds.size() > 1){
                        newEndPoint = offsetEndDoubleBond;                    
                }
                
                Line newBeginPointWidth = calculatePointWidth(
                        pointEndCenter.getX(), pointEndCenter.getY(),
                        newBeginPoint.getX(), newBeginPoint.getY());

                Line newEndPointWidth = calculatePointWidth(
                        pointBeginCenter.getX(), pointBeginCenter.getY(),
                        newEndPoint.getX(), newEndPoint.getY());

                List<Point> beginPoints = new ArrayList();
                beginPoints.add(pointBeginLeft);
                beginPoints.add(pointBeginCenter);
                beginPoints.add(pointBeginRight);
                beginPoints.add(new Point(newEndPointWidth.getBegin().getX(), newEndPointWidth.getBegin().getY()));
                beginPoints.add(new Point(newEndPointWidth.getEnd().getX(), newEndPointWidth.getEnd().getY()));

                innerShapes.add(new SplineConfiguration(beginPoints, true));

                List<Point> endPoints = new ArrayList();
                endPoints.add(pointEndLeft);
                endPoints.add(pointEndCenter);
                endPoints.add(pointEndRight);
                endPoints.add(new Point(newBeginPointWidth.getBegin().getX(), newBeginPointWidth.getBegin().getY()));
                endPoints.add(new Point(newBeginPointWidth.getEnd().getX(), newBeginPointWidth.getEnd().getY()));

                innerShapes.add(new SplineConfiguration(endPoints, true));
            }
            
            resultingConfiguration = new CompositeShapeConfiguration(
                    bond.getAttribute(ParseElementDefinition.BOND_DISPLAY), innerShapes);
            
            List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(bond.getId());
            if(crossBonds != null){
                Area bondArea = createBondAreasFromConfigurations(resultingConfiguration);

                for(ParsedElement crossBond : crossBonds){
                    bondArea = crossBondProcess(bondArea, crossBond);
                }

                resultingConfiguration = createConfigurationFromArea(bondArea);
            }
            
            ((CompositeShapeConfiguration)resultingConfiguration).setFill(true);
            ((CompositeShapeConfiguration)resultingConfiguration).setColor(getColor());
            ((CompositeShapeConfiguration)resultingConfiguration).setZOrder(bond.getZOrder());
            
            setResultingConfiguration(resultingConfiguration);
        }
    }
   
    
    private Line calculatePointWidth(double beginX, double beginY, double endX, double endY){
        double angle = GeometricOperations.angle(beginX, beginY, endX, endY);
        
        Point rightPoint = GeometricOperations.offset(
                endX, endY, angle - (Math.PI / 2), getEnvironment().getLineWidth() / 2);
        
        Point leftPoint = GeometricOperations.offset(
                endX, endY, angle + (Math.PI / 2), getEnvironment().getLineWidth() / 2);
       
        return new Line(rightPoint, leftPoint);
    }
}
