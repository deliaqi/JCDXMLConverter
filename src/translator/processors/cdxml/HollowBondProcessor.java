package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.CompositeShapeBuilder;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.JoinPointResult;
import translator.utils.Line;
import translator.utils.Point;

public class HollowBondProcessor extends BondProcessor {
      
    //This methos are heare beacause their initialization order depends of the display
    //of the bond.
    private Point joinPointLeftWidestNode;
    private Point joinPointCenterWidestNode;
    private Point joinPointRightWidestNOde;
    private Point joinPointLeftNarrowestNode;
    private Point joinPointCenterNarrowestNode;
    private Point joinPointRightNarrowestNode;
    
    public HollowBondProcessor() {
    }

    protected void cleanup() {  
        joinPointLeftWidestNode = null;
        joinPointCenterWidestNode = null;
        joinPointRightWidestNOde = null;
        joinPointLeftNarrowestNode = null;
        joinPointCenterNarrowestNode = null;
        joinPointRightNarrowestNode = null;
    }
    
    protected void process() {
        ShapeBuilderConfiguration resultingConfiguration = null;
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_END) && bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
            Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList(); 
           
            JoinPointResult beginJoinPoints = environment.getBondJoinPointBeginResult(bond.getId());
            JoinPointResult endJoinPoints = environment.getBondJoinPointEndResult(bond.getId());
            
            initializeBondJoinPoints();
            
            double bondLength = GeometricOperations.distance(joinPointCenterWidestNode, joinPointCenterNarrowestNode);
            
            // if bond length is at least twice the line width, draw a hollow bond 
            if (bondLength >= lineWidth * 2) {
                GeometricLine leftLine = new GeometricLine("leftLine",
                        new Point(joinPointLeftWidestNode.getX(), joinPointLeftWidestNode.getY()),
                        new Point(joinPointRightNarrowestNode.getX(), joinPointRightNarrowestNode.getY()), lineWidth*2);
                
                GeometricLine rightLine = new GeometricLine("rightLine",
                        new Point(joinPointRightWidestNOde.getX(), joinPointRightWidestNOde.getY()),
                        new Point(joinPointLeftNarrowestNode.getX(), joinPointLeftNarrowestNode.getY()), lineWidth*2);
             
                GeometricLine shortLeftLine = new GeometricLine("shortleftLine",
                        new Point(joinPointLeftWidestNode.getX(), joinPointLeftWidestNode.getY()),
                        new Point(joinPointCenterWidestNode.getX(), joinPointCenterWidestNode.getY()), lineWidth*2);
                
                GeometricLine shortRightLine = new GeometricLine("shortRightLine",
                        new Point(joinPointCenterWidestNode.getX(), joinPointCenterWidestNode.getY()),
                        new Point(joinPointRightWidestNOde.getX(), joinPointRightWidestNOde.getY()), lineWidth*2);
             
                Point crossPoint1 = GeometricOperations.intersection(
                        leftLine.getRightBegin(), leftLine.getRightEnd(),
                        rightLine.getLeftBegin(), rightLine.getLeftEnd());
                
                Point crossPoint2 = GeometricOperations.intersection(
                        leftLine.getRightBegin(), leftLine.getRightEnd(),
                        shortLeftLine.getLeftBegin(), shortLeftLine.getLeftEnd());
                
                Point crossPoint3 = GeometricOperations.intersection(
                        rightLine.getLeftBegin(), rightLine.getLeftEnd(),
                        shortRightLine.getLeftBegin(), shortRightLine.getLeftEnd());
                
                Point crossPoint4 = GeometricOperations.intersection(
                        shortLeftLine.getRightBegin(), shortLeftLine.getRightEnd(),
                        shortRightLine.getLeftBegin(), shortRightLine.getLeftEnd());
                
                List<Point> segment1Points = new ArrayList();
                
                segment1Points.add(new Point(leftLine.getEnd().getX(), leftLine.getEnd().getY()));
                segment1Points.add(new Point(joinPointCenterNarrowestNode.getX(), joinPointCenterNarrowestNode.getY()));
                segment1Points.add(new Point(crossPoint1.getX(), crossPoint1.getY()));
                segment1Points.add(new Point(crossPoint2.getX(), crossPoint2.getY()));
                segment1Points.add(new Point(leftLine.getBegin().getX(), leftLine.getBegin().getY()));
                
                SplineConfiguration poliSegment1 = new SplineConfiguration(segment1Points, true);
                
                List<Point> segment2Points = new ArrayList();
                
                segment2Points.add(new Point(rightLine.getEnd().getX(), rightLine.getEnd().getY()));
                segment2Points.add(new Point(joinPointCenterNarrowestNode.getX(), joinPointCenterNarrowestNode.getY()));
                segment2Points.add(new Point(crossPoint1.getX(), crossPoint1.getY()));
                segment2Points.add(new Point(crossPoint3.getX(), crossPoint3.getY()));
                segment2Points.add(new Point(rightLine.getBegin().getX(), rightLine.getBegin().getY()));
                
                SplineConfiguration poliSegment2 = new SplineConfiguration(segment2Points, true);
                
                List<Point> segment3Points = new ArrayList();
                
                segment3Points.add(new Point(shortRightLine.getBegin().getX(), shortRightLine.getBegin().getY()));
                segment3Points.add(new Point(shortRightLine.getEnd().getX(), shortRightLine.getEnd().getY()));
                segment3Points.add(new Point(crossPoint3.getX(), crossPoint3.getY()));
                segment3Points.add(new Point(shortRightLine.getLeftBegin().getX(), shortRightLine.getLeftBegin().getY()));
                
                SplineConfiguration poliSegment3 = new SplineConfiguration(segment3Points, true);
                
                List<Point> segment4Points = new ArrayList();
                
                segment4Points.add(new Point(shortLeftLine.getEnd().getX(), shortLeftLine.getEnd().getY()));
                segment4Points.add(new Point(shortLeftLine.getBegin().getX(), shortLeftLine.getBegin().getY()));
                segment4Points.add(new Point(crossPoint2.getX(), crossPoint2.getY()));
                segment4Points.add(new Point(shortRightLine.getLeftBegin().getX(), shortRightLine.getLeftBegin().getY()));
                
                SplineConfiguration poliSegment4 = new SplineConfiguration(segment4Points, true);
                
                innerShapes.add(poliSegment1);
                innerShapes.add(poliSegment2);
                innerShapes.add(poliSegment3);
                innerShapes.add(poliSegment4);
                
                if(getOrder().equals(ParseElementDefinition.BOND_ORDER_2)){
                    innerShapes.addAll(doubleBondProcess());
                } else if(getOrder().equals(ParseElementDefinition.BOND_ORDER_3)){
                    innerShapes.addAll(tripleBondProcess());
                }
            } else {
                // hollow bond is too short and geometric line intersections will be wrong
                // draw the outline's filled spline instead
                List<Point> bondPoints = new ArrayList();
                
                bondPoints.add(joinPointLeftWidestNode);
                bondPoints.add(joinPointCenterWidestNode);
                bondPoints.add(joinPointRightWidestNOde);
                bondPoints.add(joinPointLeftNarrowestNode);
                bondPoints.add(joinPointCenterNarrowestNode);
                bondPoints.add(joinPointRightNarrowestNode);
                
                innerShapes.add(new SplineConfiguration(bondPoints, true));
            }
            resultingConfiguration = new CompositeShapeConfiguration(
                    bond.getAttribute(ParseElementDefinition.BOND_DISPLAY), innerShapes);
            
            List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(bond.getId());
            
            ParsedElement beginNode = getEnvironment().getNodeById(bond.getAttribute(ParseElementDefinition.BOND_BEGIN));
            ParsedElement endNode = getEnvironment().getNodeById(bond.getAttribute(ParseElementDefinition.BOND_END));
            
            if(crossBonds != null || 
                    (beginNode != null && beginNode.hasAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE)) ||
                    (endNode != null && endNode.hasAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE))){
                List<Area> bondPartsArea = new ArrayList<Area>();
                
                for(ShapeBuilderConfiguration partConfiguration : 
                    ((CompositeShapeConfiguration)resultingConfiguration).getConfigurations()){                    
                    bondPartsArea.add(createBondAreasFromConfigurations(partConfiguration));                    
                }
                
                if(beginNode != null && 
                        beginNode.hasAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE)){
                    Collection<ShapeBuilderConfiguration> beginCutParts = attachedObjectProcess(
                                bondPartsArea, beginNode);
                    bondPartsArea = new ArrayList<Area>();
                    for(ShapeBuilderConfiguration partConfiguration : beginCutParts){
                        bondPartsArea.add(createBondAreasFromConfigurations(partConfiguration));
                    }
                }
                
                if(endNode != null &&
                        endNode.hasAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE)){
                    Collection<ShapeBuilderConfiguration> endCutParts = attachedObjectProcess(
                                bondPartsArea, endNode);
                    bondPartsArea = new ArrayList<Area>();
                    for(ShapeBuilderConfiguration partConfiguration : endCutParts){
                        bondPartsArea.add(createBondAreasFromConfigurations(partConfiguration));
                    }
                }
                
                if(crossBonds != null){
                    resultingConfiguration = new CompositeShapeConfiguration("Bond's parts",
                            crossBondProcess(bondPartsArea, crossBonds));
                } else {
                    Collection<ShapeBuilderConfiguration> bondPartsConfigurations = 
                            new ArrayList();
                    for(Area partArea : bondPartsArea){
                        bondPartsConfigurations.add(createConfigurationFromArea(partArea));
                    }
                        
                    resultingConfiguration = new CompositeShapeConfiguration("Bond's parts",
                            bondPartsConfigurations);
                }
            }
            
            ((CompositeShapeConfiguration)resultingConfiguration).setFill(true);
            ((CompositeShapeConfiguration)resultingConfiguration).setColor(getColor());
            ((CompositeShapeConfiguration)resultingConfiguration).setZOrder(zOrder);
            
            setResultingConfiguration(resultingConfiguration);
        }
    }
    
    protected void initializeBondJoinPoints() {
        super.initializeBondJoinPoints();
        double bondPerpendicularAngle = bondAngle + Math.PI/2;
        double wedgeWidth = lineWidth + boldWidth/2;
        
        String display = getElement().getAttribute(ParseElementDefinition.BOND_DISPLAY);        
        
        recalculateBondJoinPoints();
        
        if (display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_BEGIN)){
            //When the bond has the display "HollowWedgeBegin" the bond end is the biggest side.
            joinPointLeftNarrowestNode = pointBeginLeft;
            joinPointCenterNarrowestNode = pointBeginCenter;
            joinPointRightNarrowestNode = pointBeginRight;
            joinPointLeftWidestNode = pointEndLeft;
            joinPointCenterWidestNode = pointEndCenter;
            joinPointRightWidestNOde = pointEndRight;
        }else{
            //When the bond has the display "HollowWedgeEnd" the bond begin is the bigger part.
            joinPointLeftWidestNode = pointBeginLeft;
            joinPointCenterWidestNode = pointBeginCenter;
            joinPointRightWidestNOde = pointBeginRight;
            joinPointLeftNarrowestNode = pointEndLeft;
            joinPointCenterNarrowestNode = pointEndCenter;
            joinPointRightNarrowestNode = pointEndRight;

        }
    }
    
    /**
     * This method is ussed for substract the bond parts area 
     * of bonds with crossing bonds overlapping areas.
     */
    protected Collection<ShapeBuilderConfiguration> crossBondProcess(List<Area> bondPartsArea, List<ParsedElement> crossBonds) {
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        for (ParsedElement crossBond : crossBonds) {
            
            if (crossBond.hasAttribute(ParseElementDefinition.BOND_BEGIN)) {

                Area overlapArea = createOverlapArea(crossBond);
                ExtendedGeneralPath bondPath = createBondPath();
                
                for(Line line : createOverlapLines(crossBond)){
                    GeometricLine overlapLine = new GeometricLine("", line.getBegin(), line.getEnd(), getLineWidth());

                    ExtendedGeneralPath linePath = new ExtendedGeneralPath(new GeneralPath());
                    linePath.moveTo(overlapLine.getLeftBegin().getX(), overlapLine.getLeftBegin().getY());
                    linePath.lineTo(overlapLine.getLeftEnd().getX(), overlapLine.getLeftEnd().getY());
                    linePath.lineTo(overlapLine.getRightEnd().getX(), overlapLine.getRightEnd().getY());
                    linePath.lineTo(overlapLine.getRightBegin().getX(), overlapLine.getRightBegin().getY());
                    linePath.closePath();

                    Area filledArea = new Area(bondPath);
                    filledArea.intersect(new Area(linePath));

                    result.add(createConfigurationFromArea(filledArea));
                }

                for(Area bondPart:bondPartsArea){
                    bondPart.subtract(overlapArea);
                }
            } else {
                result = null;
            }
        }
        for(Area bondPart:bondPartsArea){            
             result.add(createConfigurationFromArea(bondPart));
        }
        return result;
    }
    
    private Collection<ShapeBuilderConfiguration> attachedObjectProcess(List<Area> bondPartsArea, ParsedElement node){
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        
        List<Area> newBondParts = new ArrayList();
        
        if(node.hasAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE)){
            Area attachedArea = null;
            String externalConnectionPoint = 
                    node.getAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE);

            Point position = parseCoords(
                    node.getAttribute(ParseElementDefinition.NODE_POSITION), node);

            ExtendedGeneralPath bondPath = createBondPath();

            if(externalConnectionPoint.equals(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_DIAMOND) ||
                    externalConnectionPoint.equals(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_UNSPECIFIED)){
                attachedArea = createDiamondArea(position, 10.5);
            } else if(externalConnectionPoint.equals(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_POLYMER_BEAD)){
                //To cut the hollow bond when the attached object is a polymer bead
            } else if(externalConnectionPoint.equals(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_STAR)){
                //To cut the hollow bond when the attached object is a start
            } else if(externalConnectionPoint.equals(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_WAVY)){
                //To cut the hollow bond when the attached object is a wavy
            }

            if(attachedArea != null){
                Area filledArea = new Area(bondPath);
                filledArea.intersect(attachedArea);

                result.add(createConfigurationFromArea(filledArea));

                for(Area bondPart:bondPartsArea){
                    bondPart.subtract(attachedArea);
                    newBondParts.add(bondPart);
                }
            }
        }
        
        if(newBondParts.size() > 0){
            for(Area bondPart:newBondParts){
                 result.add(createConfigurationFromArea(bondPart));
            }
        } else {
            for(Area bondPart:bondPartsArea){
                 result.add(createConfigurationFromArea(bondPart));
            }
        }
        
        return result;
    }
    
    private Area createDiamondArea(Point position, double size){
        Area result = new Area();
        
        double minusHalfLineDistance = (size / 2) - (getLineWidth());
        double majorHalfLineDistance = (size / 2) + (getLineWidth());
        
        //Create the line to represent the segment that join the top
        //point with the right point of the diamond
        ExtendedGeneralPath segmentPath = new ExtendedGeneralPath(new GeneralPath());
        segmentPath.moveTo(position.getX(), position.getY() - minusHalfLineDistance);
        segmentPath.lineTo(position.getX(), position.getY() - majorHalfLineDistance);
        segmentPath.lineTo(position.getX() + majorHalfLineDistance, position.getY());
        segmentPath.lineTo(position.getX() + minusHalfLineDistance, position.getY());
        segmentPath.closePath();
        
        result.add(new Area(segmentPath));
        
        //Create the line to represent the segment that join the botom
        //point with the right point of the diamond
        segmentPath = new ExtendedGeneralPath(new GeneralPath());
        segmentPath.moveTo(position.getX() + majorHalfLineDistance, position.getY());
        segmentPath.lineTo(position.getX() + minusHalfLineDistance, position.getY());
        segmentPath.lineTo(position.getX(), position.getY() + minusHalfLineDistance);
        segmentPath.lineTo(position.getX(), position.getY() + majorHalfLineDistance);
        segmentPath.closePath();
        
        result.add(new Area(segmentPath));
        
        //Create the line to represent the segment that join the botom
        //point with the left point of the diamond
        segmentPath = new ExtendedGeneralPath(new GeneralPath());
        segmentPath.moveTo(position.getX(), position.getY() + minusHalfLineDistance);
        segmentPath.lineTo(position.getX(), position.getY() + majorHalfLineDistance);
        segmentPath.lineTo(position.getX() - majorHalfLineDistance, position.getY());
        segmentPath.lineTo(position.getX() - minusHalfLineDistance, position.getY());
        segmentPath.closePath();
        
        result.add(new Area(segmentPath));
        
        //Create the line to represent the segment that join the top
        //point with the left point of the diamond
        segmentPath = new ExtendedGeneralPath(new GeneralPath());
        segmentPath.moveTo(position.getX() - majorHalfLineDistance, position.getY());
        segmentPath.lineTo(position.getX() - minusHalfLineDistance, position.getY());
        segmentPath.lineTo(position.getX(), position.getY() - minusHalfLineDistance);
        segmentPath.lineTo(position.getX(), position.getY() - majorHalfLineDistance);
        segmentPath.closePath();
        
        result.add(new Area(segmentPath));
        
        return result;
    }
    
    private ExtendedGeneralPath createBondPath(){
        GeometricLine bondLine = new GeometricLine("", 
                new Point(pointBeginCenter.getX(), pointBeginCenter.getY()),
                new Point(pointEndCenter.getX(), pointEndCenter.getY()), 
                getBeginWidth(getElement()), getEndWidth(getElement()));

        ExtendedGeneralPath bondPath = new ExtendedGeneralPath(new GeneralPath());
        bondPath.moveTo(bondLine.getLeftBegin().getX(), bondLine.getLeftBegin().getY());
        bondPath.lineTo(bondLine.getLeftEnd().getX(), bondLine.getLeftEnd().getY());
        bondPath.lineTo(bondLine.getRightEnd().getX(), bondLine.getRightEnd().getY());
        bondPath.lineTo(bondLine.getRightBegin().getX(), bondLine.getRightBegin().getY());
        bondPath.closePath();
        
        return bondPath;
    }
}
