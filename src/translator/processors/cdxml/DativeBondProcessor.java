package translator.processors.cdxml;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.BuilderConfiguration;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.ArrowHeadConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.JoinPointResult;
import translator.utils.Point;

public class DativeBondProcessor extends BondProcessor {
    
    //taken from C++ code
    public final double ARROW_CENTER_SIZE = 8.75;
    public final double ARROW_HEAD_SIZE = 10;
    public final double ARROW_HEAD_WIDTH = 2.5;
                
    private Point bondLeftPoint;
    private Point bondCenterPoint;
    private Point bondRightPoint;
    private Point endPoint;
    
    private double originalAngle;
    
    private double newX1;
    private double newY1;
    private double newX2;
    private double newY2;
    
    public DativeBondProcessor() {
    }

    protected void cleanup() {
        
        bondLeftPoint = null;
        bondCenterPoint = null;
        bondRightPoint = null;
        endPoint = null;
        
        originalAngle = 0.0;
        
        newX1 = 0.0;
        newY1 = 0.0;
        newX2 = 0.0;
        newY2 = 0.0;
        
    }

    protected void initializeBondJoinPoints() {
        
        super.initializeBondJoinPoints();
       
        newX1 = pointBeginCenter.getX();
        newY1 = pointBeginCenter.getY();
        newX2 = pointEndCenter.getX();
        newY2 = pointEndCenter.getY();
            
        double perpendicularBondAngle = bondAngle + Math.PI/2;
        
        if(getEnvironment().getBondJoinPointBeginResult(getElement().getId()) != null){
                
            bondLeftPoint = pointBeginLeft;
            bondCenterPoint = pointBeginCenter;
            bondRightPoint = pointBeginRight;

            if (attachmentPointOffsetBegin != 0) {
                bondLeftPoint = GeometricOperations.offset(bondLeftPoint, originalAngle, attachmentPointOffsetBegin);
                bondCenterPoint = GeometricOperations.offset(bondCenterPoint, originalAngle, attachmentPointOffsetBegin);
                bondRightPoint = GeometricOperations.offset(bondRightPoint, originalAngle, attachmentPointOffsetBegin);
            }
            
            endPoint = new Point(newX2,newY2);
        }

    }

    protected void process() {        
        ShapeBuilderConfiguration resultingConfiguration = null;
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_END) && bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
            List<Point> bondPoints = new ArrayList();
                        
            initializeBondJoinPoints();
            
            //taken from C++ code
            double arrowWidth = ARROW_HEAD_WIDTH * environment.getLineWidth();
            double arrowHeadCenterSize = ARROW_CENTER_SIZE * environment.getLineWidth();
            
            double angle = GeometricOperations.angle(bondCenterPoint, endPoint);
            
            //Calculate the points for the arrow
            Point baseArrowPoint = GeometricOperations.offset(endPoint, angle, -arrowHeadCenterSize);
            Point endArrowPoint = baseArrowPoint.subtract(endPoint).byScalar(
                    Math.abs(ARROW_HEAD_SIZE / ARROW_CENTER_SIZE)).add(endPoint);
            Point baseRightPoint = GeometricOperations.offset(baseArrowPoint, angle + Math.PI/2, lineWidth / 2);
            Point baseLeftPoint = GeometricOperations.offset(baseArrowPoint, angle + Math.PI/2, -lineWidth / 2);
            
            //Add the points to create the spline configuration for the bond
            bondPoints.add(baseRightPoint);
            bondPoints.add(bondLeftPoint);
            bondPoints.add(bondCenterPoint);
            bondPoints.add(bondRightPoint);
            bondPoints.add(baseLeftPoint);
            
            Collection<ShapeBuilderConfiguration> arrowParts = new ArrayList();
            
            //create the arrow head configuration and the bond shaft configuration
            arrowParts.add(ArrowHeadConfiguration.getArrowHeadShape(baseArrowPoint, endPoint, endArrowPoint, arrowWidth, angle, 0, false, false));
            arrowParts.add(new SplineConfiguration(bondPoints, true));
            
            resultingConfiguration = new CompositeShapeConfiguration(ParseElementDefinition.ARROW, arrowParts);
                        
            List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(getElement().getId());
            if(crossBonds != null){
                Area bondArea = createBondAreasFromConfigurations(resultingConfiguration);

                for(ParsedElement crossBond : crossBonds){
                    bondArea = crossBondProcess(bondArea, crossBond);
                }

                resultingConfiguration = createConfigurationFromArea(bondArea);
            }
            
            ((BuilderConfiguration)resultingConfiguration).setFill(true);
            ((BuilderConfiguration)resultingConfiguration).setColor(getColor());
            ((BuilderConfiguration)resultingConfiguration).setZOrder(zOrder);
            
            setResultingConfiguration(resultingConfiguration);
        }
    }
}
