package translator.processors.cdxml;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.graphics.StyleElement;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.LineJoin;
import translator.graphics.shapes.builders.configurations.RectangleConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.utils.Point;

public class StoichiometryGridProcessor extends CDXMLProcessor {
    
    //Taken from C++ code
    private static final double SPLIT_FACTOR_FOR_WIDTH_ATTRIBUTE = 65536.0;
    
    //For "Y", we have to set the top and the bottom offset.
    private static final double OFFSET_FACTOR_FOR_GRID_Y_OFFSET = 3.5;
    //For "X", we only have to set the right offset at the very beginning to set correctly the first x of the grid.
    private static final double OFFSET_FACTOR_FOR_GRID_X_OFFSET = 1.5;
    
    private static final String REACTANTS_TITLE = "R e a c t a n t s";
    private static final String PRODUCTS_TITLE = "P r o d u c t s";
    private static final int ROW_TEXT_SIZE = 1;
    
    private double reactantsHeight = 0;
    private double productsHeight = 0;
    private double totalHeight = 0;
    private Point pageTopPoints;
    private Point pageBottomPoints;
    private double gridWidth = 0;
    private double generalRowHeight = 0;
    
    public StoichiometryGridProcessor() {
    }
    
    protected void configure() {
        super.configure();
    }
    
    protected void cleanup() {
        reactantsHeight = 0;
        productsHeight = 0;
        totalHeight = 0;
        pageTopPoints = null;
        pageBottomPoints = null;
        gridWidth = 0;
        generalRowHeight = 0;
    }
    
    protected void process() {
        List<ShapeBuilderConfiguration> configurations = new ArrayList();
        ParsedElement grid = getElement();
        List<Point> boundingBox = parsePoints(grid.getAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_PAGE_WIDTH), grid);
        pageTopPoints = boundingBox.get(FIRST_ELEMENT);
        pageBottomPoints = boundingBox.get(SECOND_ELEMENT);        

        Point position = parseCoords(
                grid.getAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_POSITION), grid);
        
        //Components list is divided into two lists (reactant and products lists)and manage each one separately.
        ParsedElement reactantsComponents = new ParsedElement();
        ParsedElement productsComponents = new ParsedElement();
        if(grid.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT).size() > 0){
            for(ParsedElement component : grid.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT)) {
                if (component.hasAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_IS_REACTAN)
                && component.getAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_IS_REACTAN).equals(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_IS_REACTAN_TRUE)){
                    reactantsComponents.addElement(component);
                } else {
                    productsComponents.addElement(component);
                }
            }
            //Each height grid is evaluated to take the largest. Each grid has to have finally the same height.
            reactantsHeight = processGridHeight(reactantsComponents.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT).get(0));
            productsHeight = processGridHeight(productsComponents.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT).get(0));
            if (reactantsHeight > productsHeight) {
                totalHeight = reactantsHeight;
            } else {
                totalHeight = productsHeight;
            }
            //Create framework behind the grids
            RectangleConfiguration background = drawBackground(grid);
            configurations.add(background);
            //Create Reactants grid.
            configurations = drawGrid(reactantsComponents, configurations, REACTANTS_TITLE);
            //Create Products grid.
            configurations = drawGrid(productsComponents, configurations, PRODUCTS_TITLE);
        }
        
        CompositeShapeConfiguration resultingConfiguration =
                new CompositeShapeConfiguration(ParseElementDefinition.STOICHIOMETRY_GRID, configurations);
        resultingConfiguration.setColor(getColor());
        resultingConfiguration.setZOrder(zOrder);
        resultingConfiguration.setStrokeWidth(getLineWidth());
        
        setResultingConfiguration(resultingConfiguration);
    }
    
    /**
     * This method calculates the grid height basically from the first point of the bounding box to the last point.
     */
    private double processGridHeight(ParsedElement gridFirstColumn) {
        double gridHeight = 0;
        boolean first = true;
        Point beginPoint = new Point();
        Point endPoint = new Point();
        Point firstPoint = new Point();
        Point lastPoint = new Point();
        for (int i = 0; i < gridFirstColumn.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT).size(); i++) {
            List<Point> boundingBoxPoints = parsePoints(
                    gridFirstColumn.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT).get(i).getAttribute(
                    ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT_BOUNDING_BOX), gridFirstColumn);
            
            beginPoint = boundingBoxPoints.get(FIRST_ELEMENT);
            endPoint = boundingBoxPoints.get(SECOND_ELEMENT);
            
            if (first) {
                //This set generalRowHeight member variable with the row height for a later usage.
                generalRowHeight = Math.abs(endPoint.getY() - beginPoint.getY());
                first = false;
            }
            if (i == 0) {
                firstPoint = beginPoint;
            }
            if (i == gridFirstColumn.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT).size() - 1) {
                lastPoint = endPoint;
                gridHeight = Math.abs(lastPoint.getY() - firstPoint.getY());
            }
        }
        return gridHeight;
    }
    
    /**
     * This method creates the framework behind the grids.
     */
    private RectangleConfiguration drawBackground(ParsedElement components) {
        Point backgroundMinPoint = new Point();
        Point backgroundMaxPoint = new Point();
        boolean first = true;
        if(components.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT).size() > 0){
            //Here, it´s neccesary to have the max and min point of both grids.
            for(ParsedElement column : components.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT)) {
                if(components.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT).size() > 0){
                    for(ParsedElement row : column.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT)) {
                        List<translator.utils.Point> boundingBoxPoints = parsePoints(
                                row.getAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT_BOUNDING_BOX), components);
                        Point beginPoint = boundingBoxPoints.get(FIRST_ELEMENT);
                        Point endPoint = boundingBoxPoints.get(SECOND_ELEMENT);
                        
                        if (first) {
                            backgroundMinPoint = beginPoint;
                            first = false;
                        }
                        if (endPoint.getX() > backgroundMaxPoint.getX()) {
                            backgroundMaxPoint.setX(endPoint.getX());
                        }
                        if (endPoint.getY() > backgroundMaxPoint.getY()) {
                            backgroundMaxPoint.setY(endPoint.getY());
                        }
                    }
                }
            }
        }
        RectangleConfiguration background = new RectangleConfiguration(
                new Point(backgroundMinPoint.getX() - generalRowHeight, backgroundMinPoint.getY() - generalRowHeight),
                new Point(backgroundMaxPoint.getX() + generalRowHeight, backgroundMaxPoint.getY() + generalRowHeight));
        
        background.setFill(true);
        background.setStrokeWidth(getLineWidth());
        //It takes the document color
        background.setColor(getBackgroundColor());
        return background;
    }
    
    /**
     * This method draws a grid, the Reactants or the Products one. 
     * The grid is built with a rectangle for each column.
     * Each rectangle is built with splines.
     */
    private List<ShapeBuilderConfiguration> drawGrid(ParsedElement components, List<ShapeBuilderConfiguration> configurations, String gridType) {
        double columnWidth = 0;
        double rowHeight = 0;
        double minValueY = 0;
        double maxValueY = 0;
        double yOffset = 0;
        double xOffset = 0;
        boolean first = true;
        boolean headerDrawn = false;
        
        if(components.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT).size() > 0){
            for(ParsedElement component : components.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT)) {
                List<SegmentConfiguration> gridSquareBorders = new ArrayList();
                columnWidth = Double.parseDouble(component.getAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_WIDTH)) / SPLIT_FACTOR_FOR_WIDTH_ATTRIBUTE;
                gridWidth += columnWidth;   //This is a member variable to store Reactants widths in case Products grid was wrapped.
                double minValueX = 0;
                double maxValueX = 0;
                
                List<Point> boundingBoxPoints = parsePoints(
                        component.getElements(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT).get(0).getAttribute(
                        ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT_BOUNDING_BOX), components);
                translator.utils.Point beginPoint = boundingBoxPoints.get(FIRST_ELEMENT);
                translator.utils.Point endPoint = boundingBoxPoints.get(SECOND_ELEMENT);
                
                //When the total width is bigger or eqaul to the page width, the grid is wrapped.
                if (gridWidth >= pageBottomPoints.getX()) {
                    first = true;
                    gridWidth = columnWidth; 
                }
                
                if (first) {
                    //Here, min and max "Y" values are calculted for the column only at first time.
                    rowHeight = endPoint.getY() - beginPoint.getY();
                    //These double variables were inserted for resizing height and width of grids as ChemDraw C++ does.
                    yOffset = rowHeight / OFFSET_FACTOR_FOR_GRID_Y_OFFSET;
                    xOffset = rowHeight / OFFSET_FACTOR_FOR_GRID_X_OFFSET;
                    
                    minValueY = beginPoint.getY() - yOffset;
                    maxValueY = beginPoint.getY() + totalHeight + yOffset;  //All columns have the same height, previously calculated "totalheight" is used.
                    first = false;
                }
                
                minValueX = beginPoint.getX() - xOffset;
                maxValueX = beginPoint.getX() + columnWidth - xOffset;
                
                //Draw the rectangle for the column
                //Horizontal upper line
                gridSquareBorders.add(new SegmentConfiguration(
                        new Point(minValueX, minValueY),
                        new Point(maxValueX, minValueY)));
                //Right vertical line
                gridSquareBorders.add(new SegmentConfiguration(
                        new Point(maxValueX, minValueY),
                        new Point(maxValueX, maxValueY)));
                //Horizontal bottom line
                gridSquareBorders.add(new SegmentConfiguration(
                        new Point(maxValueX, maxValueY),
                        new Point(minValueX, maxValueY)));
                //Left vertical line
                gridSquareBorders.add(new SegmentConfiguration(
                        new Point(minValueX, maxValueY),
                        new Point(minValueX, minValueY)));
                
                SplineConfiguration splineConfiguration = new SplineConfiguration(gridSquareBorders);
                splineConfiguration.setLineJoin(LineJoin.Miter);
                splineConfiguration.setClosed(true);
                splineConfiguration.setStrokeWidth(getLineWidth());
                configurations.add(splineConfiguration);
                
                if (!headerDrawn) {
                    //Put the title REACTANT or PRODUCT only once.
                    CompositeTextConfiguration gridTittle = new CompositeTextConfiguration(getElement().getId() + gridType);
                    TextConfiguration textConfiguration = new TextConfiguration(minValueX + xOffset, minValueY - rowHeight / 2, ROW_TEXT_SIZE);
                    Font gridTittleFont = new Font();
                    gridTittleFont.setName(getEnvironment().getCaptionFont());
                    gridTittleFont.setSize(Double.toString(getEnvironment().getCaptionFontSize()));
                    gridTittleFont.setStyle(new StyleElement(new int[] {Font.ITALIC}));
                    
                    if(getColor() != null){
                        textConfiguration.addPart(gridType, gridTittleFont, getColor());
                    }else{
                        textConfiguration.addPart(gridType, gridTittleFont, foregroundColor);
                    }
                    
                    gridTittle.addLine(textConfiguration);
                    configurations.add(gridTittle);
                    headerDrawn = true;
                }
            }
        }
        return configurations;
    }
    
}
