package translator.processors.cdxml;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import translator.BuilderConfiguration;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class WedgeHashEndBondProcessor extends BondProcessor {
    
    public WedgeHashEndBondProcessor() {
    }
    
    protected void process() {
        initializeBondJoinPoints();
        
        ShapeBuilderConfiguration resultingConfiguration = null;
        Collection<ShapeBuilderConfiguration> innerShapes = new ArrayList();
        
        double perpendicularAngle = bondAngle + (Math.PI / 2);
        
        double newX1 = pointBeginCenter.getX();
        double newY1 = pointBeginCenter.getY();
        double newX2 = pointEndCenter.getX();
        double newY2 = pointEndCenter.getY();
        
        double distance = GeometricOperations.distance(newX1, newY1, newX2, newY2);
        
        double endSpace = 0;
        
        Point beginPoint;
        Point endPoint;
        
        double dashFraction = 1;
        double spaceFraction = 0;
        
        int numberOfDashes = 1;
        if (distance > 0) {
            double dashLength = getLineWidth();
            numberOfDashes = (int) Math.round((distance - dashLength + (getHashSpacing() / 2)) / getHashSpacing());
            dashFraction = dashLength / distance;
            
            if (numberOfDashes <= 0) {
                numberOfDashes = 1;
                dashFraction = 1;
            }
            
            if (numberOfDashes > 1) {
                spaceFraction = (distance - numberOfDashes * dashLength) / (numberOfDashes - 1) / distance;
            }
        }
        
        beginPoint = new Point(newX1, newY1);
        endPoint = new Point(newX2, newY2);
        
        double wedgedWidth = getBoldWidth() * 3 / 2;
        
        Point begin = new Point(beginPoint.getX(), beginPoint.getY());
        Point end = new Point(endPoint.getX(), endPoint.getY());
        GeometricLine bondLine = new GeometricLine("bondLine", begin, end, wedgedWidth, getLineWidth());
        
        for (int i = 0; i < numberOfDashes; i++) {
            Point dashNearEnd = GeometricOperations.offset(
                    beginPoint.getX(), beginPoint.getY(), bondAngle,
                    distance * i * (dashFraction + spaceFraction));
            
            Point dashFarEnd = GeometricOperations.offset(
                    beginPoint.getX(), beginPoint.getY(), bondAngle,
                    distance * ((i + 1) * dashFraction + i * spaceFraction));
            
            Point strokeBeginLeft = GeometricOperations.offset(
                    dashNearEnd.getX(), dashNearEnd.getY(), perpendicularAngle, getLineWidth());
            
            strokeBeginLeft = GeometricOperations.intersection(
                    dashNearEnd.getX(), dashNearEnd.getY(),
                    strokeBeginLeft.getX(), strokeBeginLeft.getY(),
                    bondLine.getLeftBegin().getX(), bondLine.getLeftBegin().getY(),
                    bondLine.getLeftEnd().getX(), bondLine.getLeftEnd().getY());
            
            Point strokeBeginRight = GeometricOperations.intersection(
                    dashNearEnd.getX(), dashNearEnd.getY(),
                    strokeBeginLeft.getX(), strokeBeginLeft.getY(),
                    bondLine.getRightBegin().getX(), bondLine.getRightBegin().getY(),
                    bondLine.getRightEnd().getX(), bondLine.getRightEnd().getY());
            
            Point strokeEndLeft = GeometricOperations.offset(
                    dashFarEnd.getX(), dashFarEnd.getY(), perpendicularAngle, getLineWidth());
            
            strokeEndLeft = GeometricOperations.intersection(
                    dashFarEnd.getX(), dashFarEnd.getY(),
                    strokeEndLeft.getX(), strokeEndLeft.getY(),
                    bondLine.getLeftBegin().getX(), bondLine.getLeftBegin().getY(),
                    bondLine.getLeftEnd().getX(), bondLine.getLeftEnd().getY());
            
            Point strokeEndRight = GeometricOperations.intersection(
                    dashFarEnd.getX(), dashFarEnd.getY(),
                    strokeEndLeft.getX(), strokeEndLeft.getY(),
                    bondLine.getRightBegin().getX(), bondLine.getRightBegin().getY(),
                    bondLine.getRightEnd().getX(), bondLine.getRightEnd().getY());
            
            List<Point> dashOutlinePoints = new ArrayList();
            dashOutlinePoints.add(strokeBeginLeft);
            dashOutlinePoints.add(strokeBeginRight);
            dashOutlinePoints.add(strokeEndRight);
            dashOutlinePoints.add(strokeEndLeft);
            SplineConfiguration dashPolygon = new SplineConfiguration(dashOutlinePoints, true);
            innerShapes.add(dashPolygon);
            dashPolygon.setColor(getColor());
            dashPolygon.setFill(true);
        }
        
        List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(getElement().getId());
        if(crossBonds != null){
            Area bondArea = createBondAreasFromConfigurations(innerShapes);
            
            for(ParsedElement crossBond : crossBonds){
                bondArea = crossBondProcess(bondArea, crossBond);
            }
            
            resultingConfiguration = createConfigurationFromArea(bondArea);
        } else {
            resultingConfiguration =
                    new CompositeShapeConfiguration("Wedged Hash End Bond", innerShapes);
        }
        
        ((BuilderConfiguration)resultingConfiguration).setColor(getColor());
        ((BuilderConfiguration)resultingConfiguration).setFill(true);
        ((BuilderConfiguration)resultingConfiguration).setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfiguration);
    }
}
