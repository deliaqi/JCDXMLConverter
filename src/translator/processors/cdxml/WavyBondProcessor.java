
package translator.processors.cdxml;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import translator.BuilderConfiguration;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.QuadraticCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Line;
import translator.utils.Point;

public class WavyBondProcessor extends BondProcessor {
    
    private static double COMPARISON_RECISION = 0.00001;
    
    public WavyBondProcessor() {
    }
    
    protected void process() {
        initializeBondJoinPoints();
        
        List<SegmentConfiguration> arcs = getEnvironment().getWavySegments(pointBeginCenter, pointEndCenter, boldWidth);
        
        ShapeBuilderConfiguration resultingConfiguration = new SplineConfiguration(arcs);
        
        List<ParsedElement> crossBonds = getEnvironment().getCrossBonds(getElement().getId());
        if(crossBonds != null){
            
            Collection<ShapeBuilderConfiguration> configurations = new ArrayList();
            configurations.add(resultingConfiguration);
            
            for(ParsedElement crossBond : crossBonds){
                configurations = crossWavyBondProcess(configurations, crossBond);
            }
            
            resultingConfiguration = new CompositeShapeConfiguration("Wavy Bond", configurations);
        }
        
        ((BuilderConfiguration)resultingConfiguration).setFill(false);
        ((BuilderConfiguration)resultingConfiguration).setColor(getColor());
        ((BuilderConfiguration)resultingConfiguration).setStrokeWidth(getLineWidth());
        ((BuilderConfiguration)resultingConfiguration).setZOrder(zOrder);
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    private Collection<ShapeBuilderConfiguration> crossWavyBondProcess(
            Collection<ShapeBuilderConfiguration> configurations, ParsedElement crossBond) {
        Collection<ShapeBuilderConfiguration> result = null;
        Point intersectionBeginPoint = null;
        Point intersectionEndPoint = null;
        
        if (crossBond.hasAttribute(ParseElementDefinition.BOND_BEGIN)) {
            String crossBondCoordinates = getEnvironment().getCoords(crossBond.getAttribute(ParseElementDefinition.BOND_BEGIN));
            
            Point pointBeginLeft;            
            Point pointBeginRight;
            Point pointEndLeft;            
            Point pointEndRight;
            
            //obtain the points of the overlaping area
            if(crossBondCoordinates.equals(getEnvironment().getOverlapJoinPointCenter1(crossBond.getId()))){
                pointBeginLeft = new Point(getEnvironment().getOverlapJoinPointLeft1(crossBond.getId()));                
                pointBeginRight = new Point(getEnvironment().getOverlapJoinPointRight1(crossBond.getId()));
                pointEndLeft = new Point(getEnvironment().getOverlapJoinPointLeft2(crossBond.getId()));                
                pointEndRight = new Point(getEnvironment().getOverlapJoinPointRight2(crossBond.getId()));
            } else{
                pointBeginLeft = new Point(getEnvironment().getOverlapJoinPointLeft2(crossBond.getId()));                
                pointBeginRight = new Point(getEnvironment().getOverlapJoinPointRight2(crossBond.getId()));
                pointEndLeft = new Point(getEnvironment().getOverlapJoinPointLeft1(crossBond.getId()));                
                pointEndRight = new Point(getEnvironment().getOverlapJoinPointRight1(crossBond.getId()));
            }            
            
            double crossBondOrder = Double.parseDouble(crossBond.getAttribute(ParseElementDefinition.BOND_ORDER));
            
            //if the order of the cross bond is greater than 1 recalculate the overlap area
            if(crossBondOrder > 1){
                double doubleBondDistance = calculateDoublePositionOffset(crossBond, getElement());
                
                doubleBondDistance +=lineWidth; 

                double overlapBondAngle = GeometricOperations.angle(pointBeginRight, pointBeginLeft);

                pointBeginLeft = GeometricOperations.offset(pointBeginLeft, overlapBondAngle, doubleBondDistance);
                pointBeginRight = GeometricOperations.offset(pointBeginRight, overlapBondAngle, doubleBondDistance);
                pointEndLeft = GeometricOperations.offset(pointEndLeft, overlapBondAngle, doubleBondDistance);
                pointEndRight = GeometricOperations.offset(pointEndRight, overlapBondAngle, doubleBondDistance);
            }
            
            Point leftIntersectionPoint = GeometricOperations.intersection(
                    pointBeginCenter, pointEndCenter, pointBeginLeft, pointEndRight);
            
            Point rightIntersectionPoint = GeometricOperations.intersection(
                    pointBeginCenter, pointEndCenter, pointBeginRight, pointEndLeft);                    
            
            double leftDistance = GeometricOperations.distance(
                    pointBeginCenter, leftIntersectionPoint);
            
            double rightDistance = GeometricOperations.distance(
                    pointBeginCenter, rightIntersectionPoint);
            
            if(leftDistance < rightDistance){
                result = removeSegments(configurations, leftDistance, rightDistance);
            } else{
                result = removeSegments(configurations, rightDistance, leftDistance);
            }
        } else {
            result = null;
        }
        return result;
    }    
    
    private Collection<ShapeBuilderConfiguration> removeSegments(
            Collection<ShapeBuilderConfiguration> configurations, double beginDistance, double endDistance){
        
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        List<SegmentConfiguration> partialResult = null;
        
        double bondSlope = GeometricOperations.slope(
                pointBeginCenter.getX(), pointBeginCenter.getY(),
                pointEndCenter.getX(), pointEndCenter.getY());
        
        for(ShapeBuilderConfiguration configuration : configurations){
            partialResult = new ArrayList();
            
            for(SegmentConfiguration segment :
                ((SplineConfiguration)configuration).getSegments()){
                    
                    double beginCurrentDistance = GeometricOperations.distance(
                            pointBeginCenter, calculateWavyIntersectionPoint(segment.getBeginPoint()));
                    
                    double endCurrentDistance = GeometricOperations.distance(
                            pointBeginCenter, calculateWavyIntersectionPoint(segment.getEndPoint()));
                    
                    double offset = 0;
                    if(Math.abs(bondSlope) >= 0 && Math.abs(bondSlope) < 1){
                        offset = 1;
                    }
                    
                    if(endCurrentDistance + offset < beginDistance ||
                            beginCurrentDistance - offset / 2 > endDistance){
                        partialResult.add(segment);
                    } else{
                        if(partialResult.size() > 0){
                            result.add(new SplineConfiguration(partialResult));
                            partialResult = new ArrayList();
                        }
                    }
                }
                
                if(partialResult.size() > 0){
                    result.add(new SplineConfiguration(partialResult));
                }
        }
        
        return result;
    }
    
    private Point calculateWavyIntersectionPoint(Point wavyBeginPoint){
        double bondAngle = GeometricOperations.angle(pointBeginCenter, pointEndCenter);
        
        Point offsetPoint = GeometricOperations.offset(
                wavyBeginPoint, bondAngle + Math.PI / 2, 1);
        
        return GeometricOperations.intersection(
                pointBeginCenter, pointEndCenter, wavyBeginPoint, offsetPoint);
    }
    
    protected void cleanup() {
        super.cleanup();
    }
}
