package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.BuilderConfiguration;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class SolidBondProcessor extends BondProcessor {
    
    private static double WEDGED_WIDTH_PROPORTION = 1.5;
    
    public SolidBondProcessor() {
    }

    protected void initializeBondJoinPoints() {
        super.initializeBondJoinPoints();
        recalculateBondJoinPoints();
    }
    
    protected void process() {
        ShapeBuilderConfiguration resultingConfiguration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_END) &&
                bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) &&
                bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
            initializeBondJoinPoints();
            
            if(getEnvironment().getBondJoinPointBeginResult(bond.getId()) != null){
                List<Point> bondPoints = new ArrayList();
                
                bondPoints.add(pointBeginLeft);
                bondPoints.add(pointBeginCenter);
                bondPoints.add(pointBeginRight);
                bondPoints.add(pointEndLeft);
                bondPoints.add(pointEndCenter);
                bondPoints.add(pointEndRight);
                
                List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(bond.getId());
                if(crossBonds != null){
                    ExtendedGeneralPath path = createPath(bondPoints);
                    Area bondArea = new Area(path);
                    
                    if(getOrder().equals(ParseElementDefinition.BOND_ORDER_2) || 
                            getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5) &&
                            bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2).equals(ParseElementDefinition.BOND_DISPLAY_2_DASH)){
                        // bond is order 2
                        // or is order 1.5 AND display2 is dashed, ignore other 1.5 order bonds
                        if(doublePosition != CENTER_DOUBLE_POSITION){
                            bondArea.add(createBondAreasFromConfigurations(doubleBondProcess()));
                        }
                        else{
                            bondArea = createBondAreasFromConfigurations(doubleBondProcess());
                        }
                    }
                    else if(getOrder().equals(ParseElementDefinition.BOND_ORDER_3)){
                        bondArea.add(createBondAreasFromConfigurations(tripleBondProcess()));
                    }
                    
                    for(ParsedElement crossBond : crossBonds){
                        bondArea = crossBondProcess(bondArea, crossBond);
                    }
                    
                    innerShapes.add(createConfigurationFromArea(bondArea));
                }
                else{                    
                    if(getOrder().equals(ParseElementDefinition.BOND_ORDER_2) || 
                            getOrder().equals(ParseElementDefinition.BOND_ORDER_2_5) || 
                            getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5) &&
                            bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2).equals(ParseElementDefinition.BOND_DISPLAY_2_DASH)){
                        // bond is order 2 or is order 2.5
                        // or is order 1.5 AND display2 is dashed, ignore other 1.5 order bonds
                        doublePosition = calculateDoublePosition(bond);
                        if(doublePosition != CENTER_DOUBLE_POSITION){
                            innerShapes.add(new SplineConfiguration(bondPoints, true));
                            innerShapes.addAll(doubleBondProcess());
                        }
                        else{
                            innerShapes.addAll(doubleBondProcess());
                        }
                    }
                    else if(getOrder().equals(ParseElementDefinition.BOND_ORDER_3)){
                        innerShapes.add(new SplineConfiguration(bondPoints, true));
                        innerShapes.addAll(tripleBondProcess());
                    }
                    else if (getOrder().equals(ParseElementDefinition.BOND_ORDER_4)) {
                        innerShapes.addAll(quadrupleBondProcess());
                    }
                    else{
                        innerShapes.add(new SplineConfiguration(bondPoints, true));
                    }
                }
                
                resultingConfiguration = new CompositeShapeConfiguration(
                        bond.getAttribute(ParseElementDefinition.BOND_DISPLAY), innerShapes);
                
                ((BuilderConfiguration)resultingConfiguration).setFill(true);
                ((BuilderConfiguration)resultingConfiguration).setColor(getColor());
                ((BuilderConfiguration)resultingConfiguration).setZOrder(bond.getZOrder());
            } else {
                List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(bond.getId());
                if(crossBonds != null){
                    Area bondArea = new Area();

                    if(getOrder().equals(ParseElementDefinition.BOND_ORDER_2) ||
                            getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5) && 
                            bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2).equals(ParseElementDefinition.BOND_DISPLAY_2_DASH)){
                        // bond is order 2
                        // or is order 1.5 AND display2 is dashed, ignore other 1.5 order bonds
                        bondArea = createBondAreasFromConfigurations(doubleBondProcess());
                    }
                    else if(getOrder().equals(ParseElementDefinition.BOND_ORDER_4)){
                        bondArea = createBondAreasFromConfigurations(quadrupleBondProcess());
                    }

                    for(ParsedElement crossBond : crossBonds){
                        bondArea = crossBondProcess(bondArea, crossBond);
                    }

                    innerShapes.add(createConfigurationFromArea(bondArea));
                }
                else{
                    if(getOrder().equals(ParseElementDefinition.BOND_ORDER_2) ||
                            getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5) && 
                            bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2).equals(ParseElementDefinition.BOND_DISPLAY_2_DASH)){
                        // bond is order 2
                        // or is order 1.5 AND display2 is dashed, ignore other 1.5 order bonds
                        if(doublePosition != CENTER_DOUBLE_POSITION){
                            Point segmentPointBegin = new Point(getX1(), getY1());
                            Point segmentPointEnd = new Point(getX2(), getY2());
                            SegmentConfiguration bondSegment = new SegmentConfiguration(segmentPointBegin, segmentPointEnd);
                            bondSegment.setStrokeWidth(lineWidth);
                            innerShapes.add(bondSegment);
                            innerShapes.addAll(doubleBondProcess());
                        }
                        else{
                            innerShapes.addAll(doubleBondProcess());
                        }
                    } else if (getOrder().equals(ParseElementDefinition.BOND_ORDER_4)) {
                        innerShapes.addAll(quadrupleBondProcess());
                    }
                }
                
                resultingConfiguration = new CompositeShapeConfiguration(
                        bond.getAttribute(ParseElementDefinition.BOND_DISPLAY), innerShapes);
                
                ((BuilderConfiguration)resultingConfiguration).setFill(true);
                ((BuilderConfiguration)resultingConfiguration).setColor(getColor());
                ((BuilderConfiguration)resultingConfiguration).setZOrder(bond.getZOrder());
            }
        }
        
        setResultingConfiguration(resultingConfiguration);
    }
    
}

