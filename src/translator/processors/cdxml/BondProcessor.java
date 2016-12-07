
package translator.processors.cdxml;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.CDXMLEnvironment;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.graphics.shapes.builders.configurations.ArcConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.JoinPointResult;
import translator.utils.Line;
import translator.utils.Point;

public abstract class BondProcessor extends CDXMLProcessor {
    
    
    protected static final int LEFT_LINE = 0;
    protected static final int RIGHT_LINE = 1;
    protected static final int BEGIN_LEFT_CENTER_LINE = 2;
    protected static final int END_LEFT_CENTER_LINE = 3;
    protected static final int BEGIN_RIGHT_CENTER_LINE = 4;
    protected static final int END_RIGHT_CENTER_LINE = 5;
    
    private static final double WIDTH_CUT_AREA = 100;
    private static final double HEIGHT_CUT_AREA = 100;
    
    protected static final int RIGHT_DOUBLE_POSITION = 0;
    protected static final int LEFT_DOUBLE_POSITION = 1;
    protected static final int CENTER_DOUBLE_POSITION = 2;
    
    private static final double COSINE_THRESHOLD_IN = Math.cos(Math.PI / 12);
    private static final double COSINE_THRESHOLD_OUT = -0.9767;
    
    private static final int RENDER_CONTEXT_WIDTH = 100;
    private static final int RENDER_CONTEXT_HIGHT = 100;
    
    // Taken from C++ code
    private static final double BOND_SPACING_LINE_WIDTH_PROPORTION = 5 / 2;
    
    private static final String BOND = "bond";
    
    private static final String SIMPLE_BOND_ORDER = "1";
    private static final String DOUBLE_BOND_ORDER = "2";
    private static final String DOUBLE_DASHED_BOND_ORDER = "1.5";
    private static final String TRIPLE_BOND_ORDER = "3";
    private static final String QUADRUPLE_BOND_ORDER = "4";
    private static final String DATIVE_BOND_ORDER = "dative";
    
    private static final double MAX_MARGIN_WIDTH_PERCENTAGE = 0.25;
    
    // These coordinates define the points where the bond
    // must be drawn, their values may change with bond truncation
    // For real node coordinates use bondBegin and bondEnd.
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    
    private double bondSpacing;
    private String bondOrder;
    
    // Real node coordinates where the bond starts and ends
    protected Point bondBegin;
    protected Point bondEnd;
    
    protected boolean endMarginAdded = false;
    protected boolean beginMarginAdded = false;
    
    protected double attachmentPointOffsetBegin;
    protected double attachmentPointOffsetEnd;
    
    // contains the position when bond is order 2
    protected int doublePosition;
    
    //This points are used for each processors like reference for drawn bonds.
    protected Point pointBeginLeft;
    protected Point pointBeginCenter;
    protected Point pointBeginRight;
    protected Point pointEndLeft;
    protected Point pointEndCenter;
    protected Point pointEndRight;
    
    protected double bondAngle;
    
    
    public BondProcessor() {
    }
    
    protected void configure(){
        super.configure();
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_SPACING)
        && bond.hasAttribute(ParseElementDefinition.BOND_HASH_SPACING)
        && bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)
        && bond.hasAttribute(ParseElementDefinition.BOND_BOLD_WIDTH)) {
            
            if (bond.hasAttribute(ParseElementDefinition.BOND_CROSSING_BOND)) {
                bond.getAttribute(ParseElementDefinition.BOND_CROSSING_BOND);
                getEnvironment().checkCrossBond(bond);
            }
            
            Line bondCoordinates = parseCoords(getElement());
            
            bondBegin = bondCoordinates.getBegin();
            bondEnd = bondCoordinates.getEnd();
            
            x1 = bondCoordinates.getBegin().getX();
            y1 = bondCoordinates.getBegin().getY();
            x2 = bondCoordinates.getEnd().getX();
            y2 = bondCoordinates.getEnd().getY();
            
            // needs to be truncated at begin point
            String bondBegin = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
            if (environment.isFloatingAttachmentPoint(bondBegin)) {
                attachmentPointOffsetBegin = (environment.getLabelFontSize() * NodeProcessor.DIAMOND_FONT_SIZE_PROPORTION + lineWidth) / 2;
            }
            
            // needs to be truncated at end point
            String bondEnd = bond.getAttribute(ParseElementDefinition.BOND_END);
            if (environment.isFloatingAttachmentPoint(bondEnd)) {
                attachmentPointOffsetEnd = (environment.getLabelFontSize() * NodeProcessor.DIAMOND_FONT_SIZE_PROPORTION + lineWidth) / 2;
            }
            
            
            double originalAngle = GeometricOperations.angle(x1, y1, x2, y2);
            
            Point beginTextClosestPoint = null;
            Point endTextClosestPoint = null;
            
            if(beginTextClosestPoint != null){
                beginTextClosestPoint
                        = GeometricOperations.offset(beginTextClosestPoint.getX(),
                        beginTextClosestPoint.getY(), originalAngle, calculateBeginMargin());
                
                x1 = beginTextClosestPoint.getX();
                y1 = beginTextClosestPoint.getY();
                
                beginMarginAdded = true;
                
            }else{
                beginMarginAdded = false;
            }
            
            if(endTextClosestPoint != null){
                double endTextMargin = calculateEndMargin();
                
                endTextClosestPoint
                        = GeometricOperations.offset(endTextClosestPoint.getX(),
                        endTextClosestPoint.getY(), originalAngle, -calculateEndMargin());
                
                x2 = endTextClosestPoint.getX();
                y2 = endTextClosestPoint.getY();
                
                endMarginAdded = true;
                
            }else{
                endMarginAdded = false;
            }
            
            bondSpacing = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_SPACING));
            
            if (bond.hasAttribute(ParseElementDefinition.BOND_ORDER)) {
                bondOrder = bond.getAttribute(ParseElementDefinition.BOND_ORDER);
                
                if(bondOrder.equals(DOUBLE_DASHED_BOND_ORDER)
                && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY_2)
                && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)
                && bond.getAttribute(ParseElementDefinition.BOND_DISPLAY).equals(ParseElementDefinition.BOND_DISPLAY_DASH)
                && bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2).equals(ParseElementDefinition.BOND_DISPLAY_SOLID)){
                    
                    bondOrder = SIMPLE_BOND_ORDER;
                    
                }else if (bondOrder.equals(DOUBLE_BOND_ORDER) || bondOrder.equals(DOUBLE_DASHED_BOND_ORDER)) {
                    
                    doublePosition = calculateDoublePosition(bond);
                    
                }
                
            } else {
                bondOrder = SIMPLE_BOND_ORDER;
            }
            
        }
    }
    
    /**
     * Can be used to obtain the margin to truncate a bond when a label is attached
     * to the beginning point
     * Will default to the document's <code>MarginWidth</code> property if the node
     * does not specify a value
     */
    protected double calculateBeginMargin(){
        String beginNodeId = getElement().getAttribute(ParseElementDefinition.BOND_BEGIN);
        return calculateMargin(beginNodeId);
    }
    
    /**
     * Can be used to obtain the margin to truncate a bond when a label is attached
     * to the end point
     * Will default to the document's <code>MarginWidth</code> property if the node
     * does not specify a value
     */
    protected double calculateEndMargin() {
        String endNodeId = getElement().getAttribute(ParseElementDefinition.BOND_END);
        return calculateMargin(endNodeId);
    }
    
    private double calculateMargin(String nodeId) {
        String nodeMarginWidth = getEnvironment().getNodeMarginWidth(nodeId);
        double marginWidth;
        if (nodeMarginWidth != null) {
            marginWidth = Double.parseDouble(nodeMarginWidth);
        } else {
            marginWidth = getEnvironment().getMarginWidth();
        }
        return marginWidth;
    }
    
    protected Point getBeginTextClosestPoint(){
        
        ParsedElement bond = getElement();
        
        Point result = null;
        
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN)&& bond.hasAttribute(ParseElementDefinition.BOND_END)) {
            
            ParsedElement beginText = getEnvironment().getAttachedText(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN));
            
            double angle = GeometricOperations.angle(getX1(), getY1(), getX2(), getY2());
            double perpendicularAngle = angle + (Math.PI / 2);
            
            if (beginText != null && beginText.hasAttribute(ParseElementDefinition.TEXT_POSITION)) {
                // work with a copy of the original text element
                beginText = beginText.copy();
                
                int beginAttach = 0;
                
                if(bond.hasAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH)){
                    beginAttach = Integer.parseInt(bond.getAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH));
                }
                
                String value = beginText.getValue();
                beginText.setValue(value.substring(beginAttach, beginAttach + 1));
                
                String lastCoordinates = beginText.getAttribute(ParseElementDefinition.TEXT_POSITION);
                Point newCoordinates = environment.relocateText(beginText);
                beginText.addAttribute(ParseElementDefinition.TEXT_POSITION, newCoordinates.getX() + " " + newCoordinates.getY());
                
                Point perpendicularPoint1 =
                        getClosestPoint(new Point(getX2(), getY2()), beginText);
                
                // obtain a new colinear point of perpendicularPoint1
                // doesn't matter what offset so use 1
                Point perpendicularPoint2
                        = GeometricOperations.offset(perpendicularPoint1.getX(),
                        perpendicularPoint1.getY(),perpendicularAngle, 1);
                
                result = GeometricOperations.intersection(perpendicularPoint1,
                        perpendicularPoint2, new Point(x1, y1),
                        new Point(x2, y2) );
                
            }
            
        }
        
        return result;
    }
    
    /**
     * This method is used to initialize the points used for drawn
     * bonds and can apply offsets to its nodes.
     */
    protected void initializeBondJoinPoints(){
        ParsedElement bond = getElement();
        
        JoinPointResult joinPointResultBegin = getEnvironment().getBondJoinPointBeginResult(bond.getId());
        JoinPointResult joinPointResultEnd = getEnvironment().getBondJoinPointEndResult(bond.getId());
        
        if(joinPointResultBegin != null){
            pointBeginLeft = joinPointResultBegin.getLeft();
            pointBeginCenter = joinPointResultBegin.getCenter();
            pointBeginRight = joinPointResultBegin.getRight();
        } else {
            pointBeginCenter = new Point(x1, y1);
        }
        
        if(joinPointResultEnd != null){
            pointEndLeft = joinPointResultEnd.getLeft();
            pointEndCenter = joinPointResultEnd.getCenter();
            pointEndRight = joinPointResultEnd.getRight();
        } else {
            pointEndCenter = new Point(x2, y2);
        }
        
        int beginAttach = -1;
        if(bond.hasAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH)){
            beginAttach = Integer.parseInt(bond.getAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH));
        }
        
        int endAttach = -1;
        if(bond.hasAttribute(ParseElementDefinition.BOND_END_ATTACH)){
            endAttach = Integer.parseInt(bond.getAttribute(ParseElementDefinition.BOND_END_ATTACH));
        }
        
        Point finalBeginPosition = recalculateBeginPosition(beginAttach);
        Point finalEndPosition = recalculateEndPosition(endAttach);
        
        CompositeTextConfiguration beginCompositeText = getBeginAttached(bond);
        CompositeTextConfiguration endCompositeText = getEndAttached(bond);
        
        double beginMargin = calculateBeginMargin();
        double endMargin = calculateEndMargin();
        
        double beginWidth = 0;
        double endWidth = 0;
        //Change the begin and end position of bond it exist some attached text
        if(beginCompositeText != null || endCompositeText != null){
            
            //If the bond is not a dative one but it is a double bond we need to take an area involving both right and left bond.
            if ((!bondOrder.equals(DATIVE_BOND_ORDER)) && Double.parseDouble(bondOrder) >= Double.parseDouble(DOUBLE_DASHED_BOND_ORDER)) {
                beginWidth = getBondWidth(getElement());
                endWidth = getBondWidth(getElement());
            } else {
                beginWidth = getBeginWidth(getElement());
                endWidth = getEndWidth(getElement());
            }
            
            GeometricLine bondLine = new GeometricLine(BOND,
                    finalBeginPosition, finalEndPosition, beginWidth,endWidth);
            ExtendedGeneralPath bondPath = new ExtendedGeneralPath(new GeneralPath());
            
            bondPath.moveTo(bondLine.getLeftBegin().getX(), bondLine.getLeftBegin().getY());
            bondPath.lineTo(bondLine.getLeftEnd().getX(), bondLine.getLeftEnd().getY());
            bondPath.lineTo(bondLine.getRightEnd().getX(), bondLine.getRightEnd().getY());
            bondPath.lineTo(bondLine.getRightBegin().getX(), bondLine.getRightBegin().getY());
            bondPath.closePath();
            
            Area bondArea = new Area(bondPath);
            
            if(beginCompositeText != null){
                Point closestPoint = getClosestPoint(
                        bondArea, beginCompositeText, new Line(finalEndPosition, finalBeginPosition), beginMargin);
                
                finalBeginPosition = closestPoint;
            }
            
            if(endCompositeText != null){
                Point closestPoint = getClosestPoint(
                        bondArea, endCompositeText, new Line(finalBeginPosition, finalEndPosition), endMargin);
                
                finalEndPosition = closestPoint;
            }
        }
        
        bondAngle = GeometricOperations.angle(finalBeginPosition, finalEndPosition);
        
        //The max margin width is the 25% of the fixed lenght.
        double maxMarginWidth = GeometricOperations.distance(finalBeginPosition, finalEndPosition) * MAX_MARGIN_WIDTH_PERCENTAGE;
        
        if(beginMargin > maxMarginWidth){
            beginMargin = maxMarginWidth;
        }
        
        if(endMargin > maxMarginWidth){
            endMargin = maxMarginWidth;
        }
        
        //The final begin position to draw is different that
        //the begin position indicated in the CDXML file
        if(!finalBeginPosition.equals(pointBeginCenter)){
            pointBeginCenter = finalBeginPosition;
            
            if(joinPointResultBegin != null){
                pointBeginLeft = GeometricOperations.offset(
                        pointBeginCenter, bondAngle + Math.PI / 2, getBeginWidth(bond) / 2);
                pointBeginRight = GeometricOperations.offset(
                        pointBeginCenter, bondAngle - Math.PI / 2, getBeginWidth(bond) / 2);
                
                pointBeginLeft = GeometricOperations.offset(pointBeginLeft, bondAngle, beginMargin);
                pointBeginRight = GeometricOperations.offset(pointBeginRight, bondAngle, beginMargin);
            }
            
            pointBeginCenter = GeometricOperations.offset(pointBeginCenter, bondAngle, beginMargin);
        }
        
        //It is necessary to assign again this value for those cases where the bond attach is not the first character
        //in atom label.
        bondBegin.setX(pointBeginCenter.getX());
        
        //The final end position to draw is different that
        //the end position indicated in the CDXML file
        if(!finalEndPosition.equals(pointEndCenter)){
            pointEndCenter = finalEndPosition;
            
            if(joinPointResultEnd != null){
                pointEndLeft = GeometricOperations.offset(
                        pointEndCenter, bondAngle - Math.PI / 2, getEndWidth(bond) / 2);
                pointEndRight = GeometricOperations.offset(
                        pointEndCenter, bondAngle + Math.PI / 2, getEndWidth(bond) / 2);
                
                pointEndLeft = GeometricOperations.offset(pointEndLeft, bondAngle, -endMargin);
                pointEndRight = GeometricOperations.offset(pointEndRight, bondAngle, -endMargin);
            }
            
            pointEndCenter = GeometricOperations.offset(pointEndCenter, bondAngle, -endMargin);
        }
    }
    
    /**
     * This method is used to recalculate the points used for drawn
     * bonds and can apply offsets to its nodes when the angle of the bond hava
     * variations.
     */
    protected void recalculateBondJoinPoints(){
        if(!getElement().hasAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH)
        && !getEnvironment().isBeginJoined(getElement())){
            pointBeginLeft = GeometricOperations.offset(pointBeginCenter, bondAngle+Math.PI/2, getBeginWidth(getElement())/2);
            pointBeginRight = GeometricOperations.offset(pointBeginCenter, bondAngle+Math.PI/2, -getBeginWidth(getElement())/2);
        }
        if(!getElement().hasAttribute(ParseElementDefinition.BOND_END_ATTACH)
        && !getEnvironment().isEndJoined(getElement())){
            pointEndLeft = GeometricOperations.offset(pointEndCenter, bondAngle+Math.PI/2, -getEndWidth(getElement())/2);
            pointEndRight = GeometricOperations.offset(pointEndCenter, bondAngle+Math.PI/2, getEndWidth(getElement())/2);
        }
    }
    
    
    protected void cleanup() {
        x1 = 0;
        y1 = 0;
        x2 = 0;
        y2 = 0;
        bondSpacing = 0;
        bondOrder = null;
        endMarginAdded = false;
        beginMarginAdded = false;
        attachmentPointOffsetBegin = 0;
        attachmentPointOffsetEnd = 0;
        pointBeginLeft = null;
        pointBeginCenter = null;
        pointBeginRight = null;
        pointEndLeft = null;
        pointEndCenter = null;
        pointEndRight = null;
        doublePosition = -1;
        super.cleanup();
    }
    
    /*
     * This method gets the bond width involving each bond space and the inner spaces also.
     * This is applied for double, double dashed, triple and quadruple bonds.
     */
    private double getBondWidth(ParsedElement bond) {
        double result = 0;
        double order = Double.parseDouble(bondOrder);
        if (bondOrder.equals(DOUBLE_DASHED_BOND_ORDER)) {
            order = 2;
        }
        result = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH)) * order + bondSpacing * (order - 1);
        return result;
    }
    
    
    protected Point getEndTextClosestPoint(){
        
        ParsedElement bond = getElement();
        
        Point result = null;
        
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_END)) {
            
            ParsedElement endText = getEnvironment().getAttachedText(getElement().getAttribute(ParseElementDefinition.BOND_END));
            
            double angle = GeometricOperations.angle(getX1(), getY1(), getX2(), getY2());
            double perpendicularAngle = angle + (Math.PI / 2);
            
            if (endText != null && endText.hasAttribute(ParseElementDefinition.TEXT_POSITION)) {
                // work with a copy of the original text element
                endText = endText.copy();
                
                int endAttach = 0;
                if(bond.hasAttribute(ParseElementDefinition.BOND_END_ATTACH)){
                    endAttach = Integer.parseInt(bond.getAttribute(ParseElementDefinition.BOND_END_ATTACH));
                }
                
                String value = endText.getValue();
                endText.setValue(value.substring(endAttach, endAttach + 1));
                
                String lastCoordinates = endText.getAttribute(ParseElementDefinition.TEXT_POSITION);
                
                Point newCoordinates = environment.relocateText(endText);
                
                endText.addAttribute(ParseElementDefinition.TEXT_POSITION, newCoordinates.getX() + " " + newCoordinates.getY());
                
                Point perpendicularPoint1 =
                        getClosestPoint(new Point(getX1(), getY1()), endText);
                
                // obtain a new colinear point of perpendicularPoint1
                // doesn't matter what offset so use 1
                Point perpendicularPoint2
                        = GeometricOperations.offset(perpendicularPoint1.getX(),
                        perpendicularPoint1.getY(),perpendicularAngle, 1);
                
                result = GeometricOperations.intersection(perpendicularPoint1,
                        perpendicularPoint2, new Point(x1, y1),
                        new Point(x2, y2) );
                
            }
            
            
        }
        
        return result;
        
    }
    
    private Point recalculatePositionWithRightAlignment(CompositeTextConfiguration attached,
            Point position, int attachedIndex, boolean above){
        
        double x = position.getX();
        double y = position.getY();
        
        List<TextConfiguration> lines = new ArrayList();
        lines.addAll(attached.getLines());
        
        //If the label alignment is above the first line is over the position text
        if(above){
            Collections.reverse(lines);
        }
        
        if(attachedIndex == -1){
            //If the attached index value is -1 then the position must be the first character position
            attachedIndex = attached.getSize();
        } else if(attachedIndex == 0){
            attachedIndex = 1;
        } else {
            attachedIndex += 1;
        }
        
        //Counter to store the current index inside the attachde text
        int k = attached.getSize();
        
        for(int linesIndex = 0; linesIndex < lines.size(); linesIndex++){
            
            TextConfiguration line = lines.get(linesIndex);
            
            if(k == attachedIndex){
                break;
            }
            
            for(int i = line.getParts().size() - 1; i >= 0; i--){
                if(k == attachedIndex){
                    break;
                }
                
                Graphics2D graphics = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                        new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB));
                
                // The following rendering hint takes into account sub-pixel accuracy.
                // When this hint is set on, getAdvance() method returns size of glyph in real number, not real Integer only
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                
                FontRenderContext renderContext = graphics.getFontRenderContext();
                
                //Obtain all the attributes necesary to create a text layout for each character
                String part = line.getParts().get(i);
                Font font = line.getFont(Integer.toString(i));
                Color color = line.getColor(Integer.toString(i));
                java.awt.Font platformFont =
                        getEnvironment().createPlatformFontFromFont(font, attached.getLineHeight());
                
                for(int j = part.length(); j > 0; j--){
                    if(k == attachedIndex){
                        break;
                    }
                    //Create the text layout for each character to calculate
                    //the advance value to each character
                    TextLayout partLoyout = new TextLayout(part.substring(j - 1, j), platformFont, renderContext);
                    x -= partLoyout.getAdvance();
                    
                    k--;
                }
            }
            
            if(k <= attachedIndex && linesIndex < attached.getLines().size() - 1){
                if(above){
                    y -= attached.getLineHeight();
                } else {
                    y += attached.getLineHeight();
                }
                
                x = position.getX();
            }
        }
        
        
        return new Point(x, y);
    }
    
    private Point recalculatePositionWithLeftAlignment(CompositeTextConfiguration attached,
            Point position, int attachedIndex, boolean above){
        double x = position.getX();
        double y = position.getY();
        
        List<TextConfiguration> lines = new ArrayList();
        lines.addAll(attached.getLines());
        
        //If the label alignment is above the first line is over the position text
        if(above){
            Collections.reverse(lines);
        }
        
        if(attachedIndex == -1){
            attachedIndex = 0;
        }
        
        //Counter to store the current index inside the attachde text
        int k = 0;
        //When the attached index is equal to cero the bond is pointing to the first position.
        for(int linesIndex = 0; linesIndex < lines.size(); linesIndex++){
            TextConfiguration line = lines.get(linesIndex);
            
            if(k == attachedIndex){
                break;
            }
            
            for(int i = 0; i < line.getParts().size(); i++){
                if(k == attachedIndex){
                    break;
                }
                
                Graphics2D graphics = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                        new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB));
                
                //The following rendering hint takes into account sub-pixel accuracy.
                // When this hint is set on, getAdvance() method returns size of glyph in real number, not real Integer only
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                
                FontRenderContext renderContext = graphics.getFontRenderContext();
                
                //Obtain all the attributes necesary to create a text layout for each character
                String part = line.getParts().get(i);
                Font font = line.getFont(Integer.toString(i));
                Color color = line.getColor(Integer.toString(i));
                java.awt.Font platformFont =
                        getEnvironment().createPlatformFontFromFont(font, attached.getLineHeight());
                
                for(int j = 0; j < part.length(); j++){
                    if(k == attachedIndex){
                        break;
                    }
                    //Create the text layout for each character to calculate
                    //the advance value to each character
                    TextLayout partLoyout = new TextLayout(part.substring(j, j + 1), platformFont, renderContext);
                    x += partLoyout.getAdvance();
                    
                    k++;
                }
            }
            
            if(k <= attachedIndex && linesIndex < attached.getLines().size() - 1){
                if(above){
                    y -= attached.getLineHeight();
                } else {
                    y += attached.getLineHeight();
                }
                
                x = position.getX();
            }
        }
        
        
        return new Point(x, y);
    }
    
    protected Point recalculateBeginPosition(int attachedIndex){
        Point result = pointBeginCenter;
        
        CompositeTextConfiguration attached = getBeginAttached(getElement());
        
        if(attached != null){
            boolean above = attached.getAlignment() == TextConfiguration.ABOVE_ALIGNMENT;
            
            if(attached.getJustification() == TextConfiguration.LEFT_JUSTIFICATION){
                result = recalculatePositionWithLeftAlignment(attached, pointBeginCenter, attachedIndex, above);
            } else if(attached.getJustification() == TextConfiguration.RIGHT_JUSTIFICATION){
                result = recalculatePositionWithRightAlignment(attached, pointBeginCenter, attachedIndex, above);
            }
        }
        
        return result;
    }
    
    protected Point recalculateEndPosition(int attachedIndex){
        Point result = pointEndCenter;
        
        CompositeTextConfiguration attached = getEndAttached(getElement());
        
        if(attached != null){
            boolean above = attached.getAlignment() == TextConfiguration.ABOVE_ALIGNMENT;
            
            if(attached.getJustification() == TextConfiguration.LEFT_JUSTIFICATION){
                result = recalculatePositionWithLeftAlignment(attached, pointEndCenter, attachedIndex, above);
            } else if(attached.getJustification() == TextConfiguration.RIGHT_JUSTIFICATION){
                result = recalculatePositionWithRightAlignment(attached, pointEndCenter, attachedIndex, above);
            }
        }
        
        return result;
    }
    
    private Point calculateDefaultClosestPoint(Point begin, Point end, double margin){
        double angle = GeometricOperations.angle(begin, end);
        
        Point result = null;
        
        if(angle % Math.PI == 0 || angle % (Math.PI/2) == 0){
            result = GeometricOperations.offset(end, angle, -margin);
        } else {
            result = GeometricOperations.offset(end, angle, -margin * 1.5);
        }
        
        return result;
    }
    
    public CompositeTextConfiguration getBeginAttached(ParsedElement bond){
        CompositeTextConfiguration result = null;
        
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN)) {
            String beginAttachedId = getElement().getAttribute(ParseElementDefinition.BOND_BEGIN);
            ParsedElement beginAttached = getEnvironment().getAttachedText(beginAttachedId);
            
            if(beginAttached != null){
                result =
                        getEnvironment().getCompositeTextConfigurations().get(
                        beginAttached.getId());
            }
        }
        
        return result;
    }
    
    public CompositeTextConfiguration getEndAttached(ParsedElement bond){
        CompositeTextConfiguration result = null;
        
        if(bond.hasAttribute(ParseElementDefinition.BOND_END)){
            String endAttachedId = getElement().getAttribute(ParseElementDefinition.BOND_END);
            ParsedElement endAttached = getEnvironment().getAttachedText(endAttachedId);
            
            if(endAttached != null){
                result =
                        getEnvironment().getCompositeTextConfigurations().get(
                        endAttached.getId());
            }
        }
        
        return result;
    }
    
    public static double getBeginWidth(ParsedElement bond) {
        double result;
        if (bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY) &&
                bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH) &&
                bond.hasAttribute(ParseElementDefinition.BOND_BOLD_WIDTH)) {
            String display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
            double lineWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
            double boldWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_BOLD_WIDTH));
            double wedgeWidth = boldWidth * CDXMLEnvironment.WEDGED_WIDTH_BOLD_RATIO;
            if(display == null) {
                result = lineWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD)) {
                result = boldWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_BEGIN)) {
                result = lineWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_END)) {
                result = wedgeWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_BEGIN)){
                result = lineWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_END)){
                result = wedgeWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HASH)){
                result = boldWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_BEGIN)){
                result = lineWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_END)){
                result = wedgeWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WAVY)) {
                result = boldWidth;
            } else{
                result = lineWidth;
            }
        } else {
            result = 0;
        }
        return result;
    }
    
    public static double getEndWidth(ParsedElement bond) {
        double result;
        if (bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY) &&
                bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH) &&
                bond.hasAttribute(ParseElementDefinition.BOND_BOLD_WIDTH)) {
            String display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
            double lineWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
            double boldWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_BOLD_WIDTH));
            double wedgeWidth = boldWidth * CDXMLEnvironment.WEDGED_WIDTH_BOLD_RATIO;
            if(display == null) {
                result = lineWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD)) {
                result = boldWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_BEGIN)) {
                result = wedgeWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_END)) {
                result = lineWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_BEGIN)){
                result = wedgeWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_END)){
                result = lineWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HASH)){
                result = boldWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_BEGIN)){
                result = wedgeWidth;
            } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_END)){
                result = lineWidth;
            } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WAVY)) {
                result = boldWidth;
            } else{
                result = lineWidth;
            }
        } else {
            result = 0;
        }
        return result;
    }
    
    private Point[] calculateMiteredEndPoints(GeometricLine thisBondLine, GeometricLine bondLine) {
        Point leftIntersectionPoint = GeometricOperations.intersection(
                thisBondLine.getLeftBegin(), thisBondLine.getLeftEnd(),
                bondLine.getRightBegin(), bondLine.getRightEnd());
        Point rightIntersectionPoint = GeometricOperations.intersection(
                thisBondLine.getRightBegin(), thisBondLine.getRightEnd(),
                bondLine.getRightBegin(), bondLine.getRightEnd());
        
        double bondLineLength = GeometricOperations.distance(bondLine.getRightEnd(),
                bondLine.getRightBegin());
        double intersectionLengthLeft = bondLineLength -
                GeometricOperations.distance(leftIntersectionPoint, bondLine.getRightBegin());
        double intersectionLengthRight = bondLineLength -
                GeometricOperations.distance(rightIntersectionPoint, bondLine.getRightBegin());
        
        Point[] result = new Point[2];
        
        if (intersectionLengthLeft >= 0 || intersectionLengthRight >= 0) {
            result[0] = new Point(rightIntersectionPoint.getX(), rightIntersectionPoint.getY());
            result[1] = new Point(leftIntersectionPoint.getX(), leftIntersectionPoint.getY());
        } else {
            result[0] = new Point(thisBondLine.getRightBegin().getX(), thisBondLine.getRightBegin().getY());
            result[1] = new Point(thisBondLine.getLeftBegin().getX(), thisBondLine.getLeftBegin().getY());
        }
        
        return result;
    }
    
    protected Collection<ShapeBuilderConfiguration> doubleBondProcess(){
        ParsedElement bond = getElement();
        Collection<ShapeBuilderConfiguration> result;
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN)) {
            result = new ArrayList();
            
            double perpendicularAngle = bondAngle + (Math.PI / 2);
            
            double offset = calculateBondSpacing(
                    getBondSpacing(),
                    bondBegin.getX(), bondBegin.getY(),
                    bondEnd.getX(), bondEnd.getY());
            
            if(doublePosition != CENTER_DOUBLE_POSITION){
                if(doublePosition == RIGHT_DOUBLE_POSITION){
                    perpendicularAngle += Math.PI;
                }
                
                double newX1 = pointBeginCenter.getX();
                double newY1 = pointBeginCenter.getY();
                double newX2 = pointEndCenter.getX();
                double newY2 = pointEndCenter.getY();
                
                Point sideOffsetBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle, offset);
                Point sideOffsetEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle, offset);
                
                //calculate if there is any offset for attached bonds and apply the new point
                Point offsetBeginDoubleBond;
                Point offsetEndDoubleBond;
                
                offsetBeginDoubleBond = calculateOffsetBeginDoubleBonds(sideOffsetBeginPoint, sideOffsetEndPoint, offset);
                offsetEndDoubleBond = calculateOffsetEndDoubleBonds(sideOffsetBeginPoint, sideOffsetEndPoint, offset);
                
                if (offsetBeginDoubleBond != null){
                    sideOffsetBeginPoint = offsetBeginDoubleBond;
                }
                
                if (offsetEndDoubleBond != null){
                    sideOffsetEndPoint = offsetEndDoubleBond;
                }
                
                double doubleSegmentLength = GeometricOperations.distance(
                        sideOffsetBeginPoint.getX(), sideOffsetBeginPoint.getY(),
                        sideOffsetEndPoint.getX(), sideOffsetEndPoint.getY());
                
                if (bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY_2)) {
                    String display2 = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2);
                    if (display2 != null && display2.equals(ParseElementDefinition.BOND_DISPLAY_2_DASH)) {
                        Point newBeginBond = GeometricOperations.offset(sideOffsetBeginPoint.getX(), sideOffsetBeginPoint.getY(), bondAngle, getHashSpacing());
                        Point newEndBond = GeometricOperations.offset(sideOffsetEndPoint.getX(), sideOffsetEndPoint.getY(), bondAngle, -getHashSpacing());
                        
                        SegmentConfiguration firstDash =
                                new SegmentConfiguration(sideOffsetBeginPoint, newBeginBond);
                        
                        firstDash.setStrokeWidth(lineWidth);
                        result.add(firstDash);
                        doubleSegmentLength = doubleSegmentLength - (getHashSpacing() * 2) + getHashSpacing();
                        int size = new Double(((doubleSegmentLength - getHashSpacing()) / 2) / getHashSpacing()).intValue();
                        
                        if(doubleSegmentLength / size * getHashSpacing() >= (doubleSegmentLength - getHashSpacing()) / 2){
                            size++;
                        }
                        
                        Point newBeginPoint = GeometricOperations.offset(
                                sideOffsetBeginPoint.getX(), sideOffsetBeginPoint.getY(),
                                bondAngle, (getHashSpacing() / 2));
                        
                        result.addAll(calculateDashSegment(newBeginPoint, bondAngle, doubleSegmentLength, size));
                        
                        SegmentConfiguration lastDash =
                                new SegmentConfiguration(newEndBond, sideOffsetEndPoint);
                        lastDash.setStrokeWidth(lineWidth);
                        result.add(lastDash);
                    } else {
                        SegmentConfiguration line1 =
                                new SegmentConfiguration(sideOffsetBeginPoint, sideOffsetEndPoint);
                        line1.setStrokeWidth(lineWidth);
                        result.add(line1);
                    }
                } else {
                    SegmentConfiguration line1 =
                            new SegmentConfiguration(sideOffsetBeginPoint, sideOffsetEndPoint);
                    line1.setStrokeWidth(lineWidth);
                    result.add(line1);
                }
            } else{
                ParsedElement[] beginAppendages = findAppendages(getElement(), true);
                ParsedElement[] endAppendages = findAppendages(getElement(), false);
                
                double newX1 = pointBeginCenter.getX();
                double newY1 = pointBeginCenter.getY();
                double newX2 = pointEndCenter.getX();
                double newY2 = pointEndCenter.getY();
                
                String display2 = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2);
                String display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
                
                if (getOrder().equals(ParseElementDefinition.BOND_ORDER_2)
                || getOrder().equals(ParseElementDefinition.BOND_ORDER_1_5)
                && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY_2)
                && (display2 != null  && display2.equalsIgnoreCase(ParseElementDefinition.BOND_DISPLAY_2_DASH))){
                    
                    if (display != null && display.equalsIgnoreCase(ParseElementDefinition.BOND_DISPLAY_DASH)){
                        //Build the left dashed line.
                        buildDashedBondLine(newX1, newY1, newX2, newY2, bondAngle, -offset, result);
                    }
                    if (display.equalsIgnoreCase(ParseElementDefinition.BOND_DISPLAY_SOLID)){
                        //Build the left solid line.
                        buildSolidBondLine(newX1, newY1, newX2, newY2, bondAngle, -offset, result);
                    }
                }
                
                if ((bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY_2)
                && (display2 != null && display2.equals(ParseElementDefinition.BOND_DISPLAY_2_DASH)))){
                    //Build the right dashed line.
                    buildDashedBondLine(newX1, newY1, newX2, newY2, bondAngle, offset, result);
                    
                } else {
                    double rightBondWidth;
                    
                    double leftOffset = offset / 2;
                    double rightOffset = offset / 2;
                    
                    if(display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD)){
                        rightBondWidth = boldWidth;
                        
                        // bold double bonds are offset to the left when centered
                        double boldDoubleBondOffset = (boldWidth / 2 - lineWidth / 2) / 2;
                        leftOffset += boldDoubleBondOffset;
                        rightOffset -= boldDoubleBondOffset;
                    }else{
                        rightBondWidth = lineWidth;
                    }
                    
                    if (beginAppendages[0] == null && beginAppendages[1] == null &&
                            endAppendages[0] == null && endAppendages[1] == null) {
                        Point leftOffsetBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle, leftOffset);
                        Point leftOffsetEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle, leftOffset);
                        
                        SegmentConfiguration leftLine =
                                new SegmentConfiguration(leftOffsetBeginPoint, leftOffsetEndPoint);
                        
                        Point rightOffsetBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle, -(rightOffset));
                        Point rightOffsetEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle, -(rightOffset));
                        
                        SegmentConfiguration rightLine =
                                new SegmentConfiguration(rightOffsetBeginPoint, rightOffsetEndPoint);
                        
                        leftLine.setStrokeWidth(lineWidth);
                        rightLine.setStrokeWidth(rightBondWidth);
                        result.add(leftLine);
                        result.add(rightLine);
                    } else  {
                        
                        GeometricLine bondLeft = new GeometricLine(getElement().getId()+"Left",
                                GeometricOperations.offset(new Point(newX1, newY1), GeometricOperations.addAngle(bondAngle, Math.PI / 2), leftOffset),
                                GeometricOperations.offset(new Point(newX2, newY2), GeometricOperations.addAngle(bondAngle, Math.PI / 2), leftOffset),
                                lineWidth);
                        GeometricLine bondRight = new GeometricLine(getElement().getId()+"Right",
                                GeometricOperations.offset(new Point(newX1, newY1), GeometricOperations.addAngle(bondAngle, -Math.PI / 2), rightOffset),
                                GeometricOperations.offset(new Point(newX2, newY2), GeometricOperations.addAngle(bondAngle, -Math.PI / 2), rightOffset),
                                rightBondWidth);
                        
                        List<Point> leftPoints = new ArrayList();
                        List<Point> rightPoints = new ArrayList();
                        
                        CompositeTextConfiguration beginCompositeText = getBeginAttached(bond);
                        CompositeTextConfiguration endCompositeText = getEndAttached(bond);
                        
                        if (beginAppendages[0] != null && beginCompositeText == null) {
                            Line attachedBondCoordinates = parseCoords(beginAppendages[0]);
                            Point attachedBondBeginPoint, attachedBondEndPoint;
                            
                            if (beginAppendages[0].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))) {
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            } else {
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            }
                            GeometricLine beginLeftLine = new GeometricLine(beginAppendages[0].getId(), attachedBondBeginPoint, attachedBondEndPoint, getBeginWidth(beginAppendages[0]), getEndWidth(beginAppendages[0]));
                            
                            Point[] miteredEndPoints = calculateMiteredEndPoints(bondLeft, beginLeftLine);
                            leftPoints.add(miteredEndPoints[0]);
                            leftPoints.add(miteredEndPoints[1]);
                        } else {
                            leftPoints.add(new Point(bondLeft.getRightBegin().getX(), bondLeft.getRightBegin().getY()));
                            leftPoints.add(new Point(bondLeft.getLeftBegin().getX(), bondLeft.getLeftBegin().getY()));
                        }
                        
                        if (endAppendages[1] != null && endCompositeText == null) {
                            Line attachedBondCoordinates = parseCoords(endAppendages[1]);
                            Point attachedBondBeginPoint, attachedBondEndPoint;
                            
                            if (endAppendages[1].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))) {
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            } else {
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            }
                            GeometricLine endLeftLine = new GeometricLine(endAppendages[1].getId(), attachedBondBeginPoint, attachedBondEndPoint, getEndWidth(endAppendages[1]), getBeginWidth(endAppendages[1]));
                            
                            Point[] miteredEndPoints = calculateMiteredEndPoints(bondLeft, endLeftLine);
                            leftPoints.add(miteredEndPoints[1]);
                            leftPoints.add(miteredEndPoints[0]);
                        } else {
                            leftPoints.add(new Point(bondLeft.getLeftEnd().getX(), bondLeft.getLeftEnd().getY()));
                            leftPoints.add(new Point(bondLeft.getRightEnd().getX(), bondLeft.getRightEnd().getY()));
                        }
                        
                        if (beginAppendages[1] != null && beginCompositeText == null) {
                            Line attachedBondCoordinates = parseCoords(beginAppendages[1]);
                            Point attachedBondBeginPoint, attachedBondEndPoint;
                            
                            if (beginAppendages[1].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))) {
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            } else {
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            }
                            GeometricLine beginRightLine = new GeometricLine(beginAppendages[1].getId(), attachedBondEndPoint, attachedBondBeginPoint, getEndWidth(beginAppendages[1]), getBeginWidth(beginAppendages[1]));
                            
                            Point[] miteredEndPoints = calculateMiteredEndPoints(bondRight, beginRightLine);
                            rightPoints.add(miteredEndPoints[0]);
                            rightPoints.add(miteredEndPoints[1]);
                        } else {
                            rightPoints.add(new Point(bondRight.getRightBegin().getX(), bondRight.getRightBegin().getY()));
                            rightPoints.add(new Point(bondRight.getLeftBegin().getX(), bondRight.getLeftBegin().getY()));
                        }
                        if (endAppendages[0] != null && endCompositeText == null) {
                            Line attachedBondCoordinates = parseCoords(endAppendages[0]);
                            Point attachedBondBeginPoint, attachedBondEndPoint;
                            
                            if (endAppendages[0].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))) {
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            } else {
                                attachedBondEndPoint =
                                        new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                                
                                attachedBondBeginPoint =
                                        new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
                            }
                            GeometricLine endRightLine = new GeometricLine(endAppendages[0].getId(), attachedBondEndPoint, attachedBondBeginPoint, getBeginWidth(endAppendages[0]), getEndWidth(endAppendages[0]));
                            
                            Point[] miteredEndPoints = calculateMiteredEndPoints(bondRight, endRightLine);
                            rightPoints.add(miteredEndPoints[1]);
                            rightPoints.add(miteredEndPoints[0]);
                        } else {
                            rightPoints.add(new Point(bondRight.getLeftEnd().getX(), bondRight.getLeftEnd().getY()));
                            rightPoints.add(new Point(bondRight.getRightEnd().getX(), bondRight.getRightEnd().getY()));
                        }
                        
                        SplineConfiguration leftSpline = new SplineConfiguration(leftPoints, true);
                        leftSpline.setFill(true);
                        leftSpline.setColor(getColor());
                        result.add(leftSpline);
                        
                        SplineConfiguration rightSpline = new SplineConfiguration(rightPoints, true);
                        rightSpline.setFill(true);
                        rightSpline.setColor(getColor());
                        result.add(rightSpline);
                    }
                    
                }
            }
        } else {
            result = null;
        }
        return result;
    }
    
    
    
    /**Build dashed lines using the begin and end points and the
     * original bond angle aplying an offset.
     *
     * @param   newX1 the x cordinates of the begin point.
     * @param   newY1 the Y cordinates of the begin point.
     * @param   newX2 the x cordinates of the end point.
     * @param   newY2 the Y cordinates of the end point.
     * @param   originalAngle  bond angle, in radians.
     * @param   offset a distance used for calculate the position of the dashed line.
     * @param   result the shape builder configuration in wich the line will be added.
     */
    private void buildDashedBondLine(double newX1, double newY1,
            double newX2, double newY2, double originalAngle, double offset,
            Collection<ShapeBuilderConfiguration> result){
        //Create the side offset points applying an offset to the begin and end points.
        double perpendicularAngle = originalAngle + (Math.PI / 2);
        Point sideOffsetBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle, offset/2);
        Point sideOffsetEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle, offset/2);
        
        double doubleSegmentLength = GeometricOperations.distance(
                sideOffsetBeginPoint.getX(), sideOffsetBeginPoint.getY(),
                sideOffsetEndPoint.getX(), sideOffsetEndPoint.getY());
        
        Point newBeginBond = GeometricOperations.offset(sideOffsetBeginPoint.getX(), sideOffsetBeginPoint.getY(), originalAngle, getHashSpacing());
        Point newEndBond = GeometricOperations.offset(sideOffsetEndPoint.getX(), sideOffsetEndPoint.getY(), originalAngle, -getHashSpacing());
        
        SegmentConfiguration firstDash =
                new SegmentConfiguration(sideOffsetBeginPoint, newBeginBond);
        
        firstDash.setStrokeWidth(lineWidth);
        result.add(firstDash);
        doubleSegmentLength = doubleSegmentLength - (getHashSpacing() * 2) + getHashSpacing();
        int size = new Double(((doubleSegmentLength - getHashSpacing()) / 2) / getHashSpacing()).intValue();
        
        //Verify if a dashed segment can be added using the coresponding hash spacing.
        if(doubleSegmentLength / size * getHashSpacing() >= (doubleSegmentLength - getHashSpacing()) / 2){
            size++;
        }
        
        Point newBeginPoint = GeometricOperations.offset(
                sideOffsetBeginPoint.getX(), sideOffsetBeginPoint.getY(),
                originalAngle, (getHashSpacing() / 2));
        
        //Calculate the internal dashed segments.
        result.addAll(calculateDashSegment(newBeginPoint, originalAngle, doubleSegmentLength, size));
        
        SegmentConfiguration lastDash =
                new SegmentConfiguration(newEndBond, sideOffsetEndPoint);
        lastDash.setStrokeWidth(lineWidth);
        
        result.add(lastDash);
    }
    
    /**Build Solid line using the begin and end points and the
     * original bond angle aplying an offset.
     *
     * @param   newX1 the x cordinates of the begin point.
     * @param   newY1 the Y cordinates of the begin point.
     * @param   newX2 the x cordinates of the end point.
     * @param   newY2 the Y cordinates of the end point.
     * @param   originalAngle  bond angle, in radians.
     * @param   offset a distance used for calculate the position of the line.
     * @param   result the shape builder configuration in wich the line will be added.
     */
    private void buildSolidBondLine(double newX1, double newY1,
            double newX2, double newY2, double originalAngle, double offset,
            Collection<ShapeBuilderConfiguration> result){
        
        //Create the side offset points applying an offset to the begin and end points.
        double perpendicularAngle = originalAngle + (Math.PI / 2);
        Point sideOffsetBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle, offset/2);
        Point sideOffsetEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle, offset/2);
        
        SegmentConfiguration solidLine =
                new SegmentConfiguration(sideOffsetBeginPoint, sideOffsetEndPoint);
        solidLine.setStrokeWidth(lineWidth);
        
        result.add(solidLine);
    }
    
    protected Collection<ShapeBuilderConfiguration> tripleBondProcess(){
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        
        double perpendicularAngle = bondAngle + (Math.PI / 2);
        
        double offset = calculateBondSpacing(
                getBondSpacing(), bondBegin.getX(), bondBegin.getY(), bondEnd.getX(), bondEnd.getY());
        
        double newX1 = pointBeginCenter.getX();
        double newY1 = pointBeginCenter.getY();
        double newX2 = pointEndCenter.getX();
        double newY2 = pointEndCenter.getY();
        
        Point leftBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle, offset);
        Point leftEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle, offset);
        
        SegmentConfiguration leftLine = new SegmentConfiguration(leftBeginPoint, leftEndPoint);
        leftLine.setStrokeWidth(lineWidth);
        result.add(leftLine);
        
        Point rightBeginPoint = GeometricOperations.offset(newX1, newY1, perpendicularAngle + Math.PI, offset);
        Point rightEndPoint = GeometricOperations.offset(newX2, newY2, perpendicularAngle + Math.PI, offset);
        
        SegmentConfiguration rightLine = new SegmentConfiguration(rightBeginPoint, rightEndPoint);
        rightLine.setStrokeWidth(lineWidth);
        result.add(rightLine);
        
        return result;
    }
    
    protected Collection<ShapeBuilderConfiguration> quadrupleBondProcess() {
        Collection<ShapeBuilderConfiguration> result = new ArrayList();
        
        double perpendicularAngle = bondAngle + (Math.PI / 2);
        
        double offset = calculateBondSpacing(
                getBondSpacing(), bondBegin.getX(), bondBegin.getY(), bondEnd.getX(), bondEnd.getY());
        
        for (int i = 0; i < 2; i++) {
            Point firstLineBeginPoint = GeometricOperations.offset(
                    pointBeginCenter.getX(), pointBeginCenter.getY(), perpendicularAngle, offset / 2);
            Point firstLineEndPoint = GeometricOperations.offset(
                    pointEndCenter.getX(), pointEndCenter.getY(), perpendicularAngle, offset / 2);
            
            SegmentConfiguration firstLine = new SegmentConfiguration(firstLineBeginPoint, firstLineEndPoint);
            firstLine.setStrokeWidth(lineWidth);
            result.add(firstLine);
            
            Point secondLineBeginPoint = GeometricOperations.offset(firstLineBeginPoint.getX(), firstLineBeginPoint.getY(), perpendicularAngle, offset);
            Point secondLineEndPoint = GeometricOperations.offset(firstLineEndPoint.getX(), firstLineEndPoint.getY(), perpendicularAngle, offset);
            
            SegmentConfiguration secondLine = new SegmentConfiguration(secondLineBeginPoint, secondLineEndPoint);
            secondLine.setStrokeWidth(lineWidth);
            result.add(secondLine);
            
            // Switch to other side
            perpendicularAngle += Math.PI;
        }
        
        return result;
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
    
    protected Area crossBondProcess(Area bondArea, ParsedElement crossBond) {
        Area result;
        if (crossBond.hasAttribute(ParseElementDefinition.BOND_BEGIN)) {
            result = bondArea;
            Area overlapArea = createOverlapArea(crossBond);
            result.subtract(overlapArea);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * In this method the offset value needed to be applied te the overlapping 
     * area of a bond when the crossing bond is a double with right or double 
     * position.
     */
    protected double calculateDoublePositionOffset(ParsedElement crossBond, ParsedElement bond){
        
        double result = 0;
        
        Point bondBegin = parseCoords(bond).getBegin();
        Point bondEnd = parseCoords(bond).getEnd();
        
        Point crossBondBegin = parseCoords(crossBond).getBegin();
        Point crossBondEnd = parseCoords(crossBond).getEnd();
        
        Point centerPoint = GeometricOperations.intersection(bondBegin, bondEnd, crossBondBegin, crossBondEnd);
        
        double distanceToBondEnd = GeometricOperations.distance(centerPoint, bondEnd);
        double distanceToBondBegin = GeometricOperations.distance(centerPoint, bondBegin);        
        
        // This offset is needed to create the test point. The value of this 
        // offset is not relevant but it can´t be bigger thant the distances 
        // to the bond extrems.
        double secureOffset = Math.abs(distanceToBondBegin - distanceToBondEnd)/2;
        
        Point testPoint = GeometricOperations.offset(centerPoint, parseCoords(crossBond).getAngle()+Math.PI/2, secureOffset);
        
        distanceToBondEnd = GeometricOperations.distance(testPoint, bondEnd);
        distanceToBondBegin = GeometricOperations.distance(testPoint, bondBegin);
        
        double directionFactor = 1;
        
        // this is factor is needed to readjust the direction of the offset
        if(distanceToBondBegin > distanceToBondEnd){
            directionFactor = -1;
        }
        
        if(calculateDoublePosition(crossBond) == RIGHT_DOUBLE_POSITION){
            
            result = environment.calculateOverlapBondWidth(crossBond, getLineWidth())/2;        
            
        }else if(calculateDoublePosition(crossBond) == LEFT_DOUBLE_POSITION){
            
            result = -environment.calculateOverlapBondWidth(crossBond, getLineWidth())/2;
            
        }

        
        result *= directionFactor;
        
        return result;
        
    }
   
    /**
     * This method return the overlap area for the provided bond
     */
    protected Area createOverlapArea(ParsedElement crossBond){
        
        List<Point> overlapAreaPoints = new ArrayList();
        String crossBondCoordinates = getEnvironment().getCoords(crossBond.getAttribute(ParseElementDefinition.BOND_BEGIN));
        
        Point pointBeginLeft = new Point(getEnvironment().getOverlapJoinPointLeft1(crossBond.getId()));
        Point pointBeginCenter = new Point(getEnvironment().getOverlapJoinPointCenter1(crossBond.getId()));
        Point pointBeginRight = new Point(getEnvironment().getOverlapJoinPointRight1(crossBond.getId()));
        Point pointEndLeft = new Point(getEnvironment().getOverlapJoinPointLeft2(crossBond.getId()));
        Point pointEndCenter = new Point(getEnvironment().getOverlapJoinPointCenter2(crossBond.getId()));
        Point pointEndRight = new Point(getEnvironment().getOverlapJoinPointRight2(crossBond.getId()));

        double doublePositionOffset = calculateDoublePositionOffset(crossBond, getElement());        
        
        pointBeginLeft = GeometricOperations.offset(pointBeginLeft, bondAngle, doublePositionOffset);
        pointBeginCenter = GeometricOperations.offset(pointBeginCenter, bondAngle, doublePositionOffset);
        pointBeginRight = GeometricOperations.offset(pointBeginRight, bondAngle, doublePositionOffset);
        pointEndLeft = GeometricOperations.offset(pointEndLeft, bondAngle, doublePositionOffset);
        pointEndCenter = GeometricOperations.offset(pointEndCenter, bondAngle, doublePositionOffset);
        pointEndRight = GeometricOperations.offset(pointEndRight, bondAngle, doublePositionOffset);
        
        overlapAreaPoints.add(pointBeginLeft);
        overlapAreaPoints.add(pointBeginCenter);
        overlapAreaPoints.add(pointBeginRight);
        overlapAreaPoints.add(pointEndLeft);
        overlapAreaPoints.add(pointEndCenter);
        overlapAreaPoints.add(pointEndRight);
        
        ExtendedGeneralPath overlapPath = createPath(overlapAreaPoints);
        
        return new Area(overlapPath);
        
    }
    
    protected List<Line> createOverlapLines(ParsedElement crossBond){
        List<Line> result = new ArrayList();
        String crossBondCoordinates = getEnvironment().getCoords(crossBond.getAttribute(ParseElementDefinition.BOND_BEGIN));
        
        Point pointBeginLeft = new Point(getEnvironment().getOverlapJoinPointLeft1(crossBond.getId()));
        Point pointBeginCenter = new Point(getEnvironment().getOverlapJoinPointCenter1(crossBond.getId()));
        Point pointBeginRight = new Point(getEnvironment().getOverlapJoinPointRight1(crossBond.getId()));
        Point pointEndLeft = new Point(getEnvironment().getOverlapJoinPointLeft2(crossBond.getId()));
        Point pointEndCenter = new Point(getEnvironment().getOverlapJoinPointCenter2(crossBond.getId()));
        Point pointEndRight = new Point(getEnvironment().getOverlapJoinPointRight2(crossBond.getId()));
        
        double doublePositionOffset = calculateDoublePositionOffset(crossBond, getElement());
                
        pointBeginLeft = GeometricOperations.offset(pointBeginLeft, bondAngle, doublePositionOffset);
        pointBeginCenter = GeometricOperations.offset(pointBeginCenter, bondAngle, doublePositionOffset);
        pointBeginRight = GeometricOperations.offset(pointBeginRight, bondAngle, doublePositionOffset);
        pointEndLeft = GeometricOperations.offset(pointEndLeft, bondAngle, doublePositionOffset);
        pointEndCenter = GeometricOperations.offset(pointEndCenter, bondAngle, doublePositionOffset);
        pointEndRight = GeometricOperations.offset(pointEndRight, bondAngle, doublePositionOffset);
                        
        double crossBondAngle = GeometricOperations.angle(pointBeginLeft, pointEndRight);
        pointBeginLeft = GeometricOperations.offset(pointBeginLeft, crossBondAngle + Math.PI / 2, getLineWidth() / 2);
        pointEndRight = GeometricOperations.offset(pointEndRight, crossBondAngle + Math.PI / 2, getLineWidth() / 2);
        pointBeginRight = GeometricOperations.offset(pointBeginRight, crossBondAngle - Math.PI / 2, getLineWidth() / 2);
        pointEndLeft = GeometricOperations.offset(pointEndLeft, crossBondAngle - Math.PI / 2, getLineWidth() / 2);
        
        Line leftLine = new Line(pointBeginLeft, pointEndRight);
        Line rightLine = new Line(pointBeginRight, pointEndLeft);
        
        Line beginLeftCenterLine = new Line(pointBeginLeft, pointBeginCenter);
        Line beginRightCenterLine = new Line(pointBeginRight, pointBeginCenter);
        Line endLeftCenterLine = new Line(pointEndLeft, pointEndCenter);
        Line endRightCenterLine = new Line(pointEndRight, pointEndCenter);
        
        result.add(leftLine);
        result.add(rightLine);
        result.add(beginLeftCenterLine);
        result.add(endLeftCenterLine);
        result.add(beginRightCenterLine);
        result.add(endRightCenterLine);
        
        return result;
    }
    
    protected Area createBondAreasFromConfigurations(Collection<ShapeBuilderConfiguration> configurations){
        Area result = null;
        
        for(ShapeBuilderConfiguration configuration : configurations){
            Area bondArea = createBondAreasFromConfigurations(configuration);
            
            if(result == null){
                result = bondArea;
            } else{
                result.add(bondArea);
            }
        }
        
        return result;
    }
    
    protected Area createBondAreasFromConfigurations(ShapeBuilderConfiguration configuration){
        Area result = null;
        
        if(configuration instanceof SegmentConfiguration){
            SegmentConfiguration segment =
                    (SegmentConfiguration)configuration;
            
            result = createAreaFromSegment(segment);
        } else if(configuration instanceof SplineConfiguration){
            SplineConfiguration spline =
                    (SplineConfiguration)configuration;
            
            ExtendedGeneralPath splinePath = new ExtendedGeneralPath(new GeneralPath());
            boolean first = true;
            
            for(SegmentConfiguration currentSegment : spline.getSegments()){
                if(currentSegment instanceof ArcConfiguration){
                    if(result == null){
                        result = new Area(createAreaFromSegment(currentSegment));
                    } else{
                        result.add(createAreaFromSegment(currentSegment));
                    }
                } else{
                    if(first){
                        splinePath.moveTo(currentSegment.getBeginPoint().getX(), currentSegment.getBeginPoint().getY());
                        splinePath.lineTo(currentSegment.getEndPoint().getX(), currentSegment.getEndPoint().getY());
                        first = false;
                    } else{
                        splinePath.lineTo(currentSegment.getEndPoint().getX(), currentSegment.getEndPoint().getY());
                    }
                }
            }
            
            result = new Area();
            result.add(new Area(splinePath));
        } else if(configuration instanceof CompositeShapeConfiguration){
            CompositeShapeConfiguration compositeShape =
                    (CompositeShapeConfiguration)configuration;
            
            result = createBondAreasFromConfigurations(compositeShape.getConfigurations());
        }
        
        return result;
    }
    
    private Area createAreaFromSegment(SegmentConfiguration segment){
        if(segment instanceof ArcConfiguration){
            double angle = GeometricOperations.angle(segment.getBeginPoint(), segment.getEndPoint());
            
            Point beginLeftPoint = GeometricOperations.offset(segment.getBeginPoint(), angle + (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            Point beginRightPoint = GeometricOperations.offset(segment.getBeginPoint(), angle - (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            Point endLeftPoint = GeometricOperations.offset(segment.getEndPoint(), angle + (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            Point endRightPoint = GeometricOperations.offset(segment.getEndPoint(), angle - (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            double xRadius = ((ArcConfiguration)segment).getXRadius();
            double yRadius = ((ArcConfiguration)segment).getYRadius();
            
            boolean sweepPositive = ((ArcConfiguration)segment).isSweepPositive();
            
            Arc2D.Double arcShape = null;
            
            if(sweepPositive){
                arcShape = new Arc2D.Double(segment.getBeginPoint().getX(), segment.getEndPoint().getX(),
                        xRadius * 2, yRadius * 2, Math.PI, Math.PI / 2, Arc2D.CHORD);
            } else{
                arcShape = new Arc2D.Double(segment.getBeginPoint().getX(), segment.getBeginPoint().getX(),
                        xRadius * 2, yRadius * 2, Math.PI, Math.PI / 2, Arc2D.CHORD);
            }
            
            return new Area(arcShape);
        } else{
            double angle = GeometricOperations.angle(segment.getBeginPoint(), segment.getEndPoint());
            
            Point beginLeftPoint = GeometricOperations.offset(
                    segment.getBeginPoint(), angle + (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            Point beginRightPoint = GeometricOperations.offset(
                    segment.getBeginPoint(), angle - (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            Point endLeftPoint = GeometricOperations.offset(
                    segment.getEndPoint(), angle + (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            Point endRightPoint = GeometricOperations.offset(
                    segment.getEndPoint(), angle - (Math.PI / 2), segment.getStrokeWidth() / 2);
            
            List<Point> polygonPoints = new ArrayList();
            
            polygonPoints.add(new Point(beginLeftPoint.getX(), beginLeftPoint.getY()));
            polygonPoints.add(new Point(beginRightPoint.getX(), beginRightPoint.getY()));
            polygonPoints.add(new Point(endRightPoint.getX(), endRightPoint.getY()));
            polygonPoints.add(new Point(endLeftPoint.getX(), endLeftPoint.getY()));
            
            ExtendedGeneralPath polygonPaths = createPath(polygonPoints);
            return new Area(polygonPaths);
        }
    }
    
    protected Point getClosestPoint(Point beginPoint, ParsedElement textElement){
        double lastDistance = Double.MAX_VALUE;
        Point lastPoint = null;
        
        for(Point currentPoint :
            getEnvironment().getPerimeterOfShape(getEnvironment().createAreaFromText(textElement))){
                
                double distance = GeometricOperations.distance(
                        beginPoint.getX(), beginPoint.getY(), currentPoint.getX(), currentPoint.getY());
                
                if(distance < lastDistance){
                    lastDistance = distance;
                    lastPoint = currentPoint;
                }
            }
            
            // Return a copy in case anyone modifies the array
            return new Point(lastPoint);
    }
    
    protected Point getClosestPoint(Area bondArea, CompositeTextConfiguration text, Line bondLine, double margin){
        Area newBondArea = new Area(bondArea);
        
        Area attachedArea = getEnvironment().createAreaFromCompositeText(text);
        newBondArea.intersect(attachedArea);
        //In C# code line, scaling has to be applied to avoid rounding in bound values.
        Point result = intersectBondWithBound(newBondArea.getBounds2D(), bondLine);
        
        if(result == null){
            result = calculateDefaultClosestPoint(bondLine.getBegin(), bondLine.getEnd(), margin);
        }
        
        double distanceToEnd = GeometricOperations.distance(result, bondLine.getEnd()) ;
        
        //If the distance to the center point is not bigger than the margin width the
        //attached area bounds has to be ussed to calculate the closest point.
        if(distanceToEnd <= margin){
            newBondArea = new Area(bondArea);
            
            newBondArea.intersect(new Area(attachedArea.getBounds2D()));
            
            if (!newBondArea.isEmpty()){
                //In C# code line, scaling has to be applied to avoid rounding in bound values.
                result = intersectBondWithBound(newBondArea.getBounds2D(), bondLine);
            }
        }
        
        return result;
    }
    
    private Point intersectBondWithBound(Rectangle2D bound, Line bondLine){
        Point result = null;
        List<Line> lines = new ArrayList();
        
        lines.add(new Line(
                new Point(bound.getMinX(), bound.getMinY()),
                new Point(bound.getMaxX(), bound.getMinY())));
        
        lines.add(new Line(
                new Point(bound.getMaxX(), bound.getMinY()),
                new Point(bound.getMaxX(), bound.getMaxY())));
        
        lines.add(new Line(
                new Point(bound.getMaxX(), bound.getMaxY()),
                new Point(bound.getMinX(), bound.getMaxY())));
        
        lines.add(new Line(
                new Point(bound.getMinX(), bound.getMaxY()),
                new Point(bound.getMinX(), bound.getMinY())));
        
        double minDistance = Double.MAX_VALUE;
        
        for(Line line : lines){
            Point intersectionPoint = GeometricOperations.realIntersection(
                    line.getBegin(), line.getEnd(),
                    bondLine.getBegin(), bondLine.getEnd());
            
            if(intersectionPoint != null){
                double newDistance = GeometricOperations.distance(bondLine.getBegin(), intersectionPoint);
                if(newDistance < minDistance){
                    minDistance = newDistance;
                    result = intersectionPoint;
                }
            }
        }
        
        return result;
    }
    
    protected CompositeShapeConfiguration createConfigurationFromArea(Area bondArea){
        PathIterator areaSegments = bondArea.getPathIterator(null);
        List<ShapeBuilderConfiguration> splineConfigurations = new ArrayList();
        List<Point> splinePoints = new ArrayList();
        
        while(!areaSegments.isDone()){
            double[] currentSegment = new double[6];
            int type = areaSegments.currentSegment(currentSegment);
            
            if(type == areaSegments.SEG_CLOSE){
                splineConfigurations.add(new SplineConfiguration(splinePoints, true));
                splinePoints = new ArrayList();
            } else if(type == areaSegments.SEG_LINETO || type == areaSegments.SEG_MOVETO){
                splinePoints.add(new Point(currentSegment[0], currentSegment[1]));
            }
            
            areaSegments.next();
        }
        
        return new CompositeShapeConfiguration("Bond's parts", splineConfigurations);
    }
    
    /**This method is for calculate the dashed segments using splines.*/
    protected List<SplineConfiguration> calculateDashSegment(Point newBeginPoint,
            double angle, double distance, int size){
        
        List<SplineConfiguration> result = new ArrayList();
        double offset = distance / size;
        double perpendicularAngle = angle + Math.PI/2;
        
        for(int i = 1; i < size; i++){
            Point centerPoint1 = GeometricOperations.offset(
                    newBeginPoint.getX(), newBeginPoint.getY(),
                    angle, (offset * i) - (getHashSpacing() / 2));
            
            Point rightPoint1 = GeometricOperations.offset(centerPoint1, perpendicularAngle, lineWidth/2);
            Point leftPoint1 = GeometricOperations.offset(centerPoint1, perpendicularAngle, -lineWidth/2);
            
            Point centerPoint2 = GeometricOperations.offset(
                    newBeginPoint.getX(), newBeginPoint.getY(),
                    angle, (offset * i) + (getHashSpacing() / 2));
            
            Point rightPoint2 = GeometricOperations.offset(centerPoint2, perpendicularAngle, lineWidth/2);
            Point leftPoint2 = GeometricOperations.offset(centerPoint2, perpendicularAngle, -lineWidth/2);
            
            List<Point> splinePoints = new ArrayList();
            
            splinePoints.add(rightPoint1);
            splinePoints.add(rightPoint2);
            splinePoints.add(leftPoint2);
            splinePoints.add(leftPoint1);
            
            result.add(new SplineConfiguration(splinePoints, true));
        }
        
        return result;
    }
    
    protected double calculateBondSpacing(double bondSpacing, double x1, double y1,
            double x2, double y2){
        double distance = GeometricOperations.distance(x1, y1, x2, y2);
        
        double result;
        if (bondSpacing < 0) {
            // spacing is absolute
            result = Math.abs(bondSpacing);
        } else {
            // spacing is a percentage of bond length
            result = distance * bondSpacing / 100;
            
            // Taken from C++ code
            result = Math.max(result, lineWidth * 5 / 2);
        }
        
        ParsedElement bond = getElement();
        if (bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
            String display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
            if (display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD)) {
                result += boldWidth / 2 - lineWidth / 2;
            }
        }
        
        return result;
    }
    
    protected ExtendedGeneralPath createPath(List<Point> points){
        ExtendedGeneralPath result = new ExtendedGeneralPath(new GeneralPath());
        
        boolean first = true;
        for(Point currentPoint : points){
            if(first){
                result.moveTo(currentPoint.getX(), currentPoint.getY());
                first = false;
            } else{
                result.lineTo(currentPoint.getX(), currentPoint.getY());
            }
        }
        
        return result;
    }
    
    public static Line parseCoords(ParsedElement bond) {
        Line result;
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_END)) {
            String begin = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
            String end = bond.getAttribute(ParseElementDefinition.BOND_END);
            Point beginPoint = parseCoords(((CDXMLEnvironment)bond.getEnvironment()).getCoords(begin), null);
            Point endPoint = parseCoords(((CDXMLEnvironment)bond.getEnvironment()).getCoords(end), null);
            
            return new Line(beginPoint, endPoint);
            
        } else {
            result = null;
        }
        return result;
    }
    
    public static int calculateDoublePosition(ParsedElement bond){
        int result = RIGHT_DOUBLE_POSITION;
        
        if (bond.hasAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION)) {
            String position = bond.getAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION);
            if(position != null){
                if(position.equals(ParseElementDefinition.BOND_DOUBLE_POSITION_RIGHT)){
                    result = RIGHT_DOUBLE_POSITION;
                } else if(position.equals(ParseElementDefinition.BOND_DOUBLE_POSITION_LEFT)){
                    result = LEFT_DOUBLE_POSITION;
                } else if(position.equals(ParseElementDefinition.BOND_DOUBLE_POSITION_CENTER)){
                    result = CENTER_DOUBLE_POSITION;
                }
            }
        } else {
            CDXMLEnvironment environment = (CDXMLEnvironment) bond.getEnvironment();
            List<ParsedElement> beginJoinedBonds = environment.findBeginJoinedBonds(bond);
            List<ParsedElement> endJoinedBonds = environment.findEndJoinedBonds(bond);
            
            String bondBegin = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
            String bondEnd = bond.getAttribute(ParseElementDefinition.BOND_END);
            
            if (environment.isAllene(bondBegin) || environment.isAllene(bondEnd)) {
                result = CENTER_DOUBLE_POSITION;
            } else {
                
                ParsedElement[] beginAppendages = findAppendages(bond, true);
                ParsedElement[] endAppendages = findAppendages(bond, false);
                
                int leftAppendages = 0;
                int rightAppendages = 0;
                
                if (beginAppendages[0] != null){
                    leftAppendages++;
                }
                
                if (beginAppendages[1] != null){
                    rightAppendages++;
                }
                
                if (endAppendages[0] != null){
                    rightAppendages++;
                }
                
                if (endAppendages[1] != null){
                    leftAppendages++;
                }
                
                if (beginAppendages[0] == null || beginAppendages[1] == null || endAppendages[0] == null || endAppendages[1] == null) {
                    if (leftAppendages > rightAppendages) {
                        result = LEFT_DOUBLE_POSITION;
                    } else if (leftAppendages < rightAppendages) {
                        result = RIGHT_DOUBLE_POSITION;
                    } else {
                        String display = null;
                        String display2 = null;
                        if (bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY)) {
                            display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
                        }
                        if (bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY_2)) {
                            display2 = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY_2);
                        }
                        if ((beginAppendages[0] == null) == (beginAppendages[1] == null) &&
                                (endAppendages[0] == null) == (endAppendages[1] == null) &&
                                (beginAppendages[0] == null || endAppendages[0] == null) &&
                                (display != null && display.equals(ParseElementDefinition.BOND_DISPLAY_DASH))){
                            result = CENTER_DOUBLE_POSITION;
                        } else if ((beginAppendages[0] == null) == (beginAppendages[1] == null) &&
                                (endAppendages[0] == null) == (endAppendages[1] == null) &&
                                (beginAppendages[0] == null || endAppendages[0] == null) &&
                                (display2 != null && !display2.equals(ParseElementDefinition.BOND_DISPLAY_2_DASH) &&
                                display != null && !display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD))) {
                            
                            result = CENTER_DOUBLE_POSITION;
                        } else if (!(beginAppendages[1] != null && endAppendages[1] != null) ||
                                !(beginAppendages[0] == null && endAppendages[0] == null)) {
                            result = LEFT_DOUBLE_POSITION;
                        } else {
                            result = RIGHT_DOUBLE_POSITION;
                        }
                    }
                } else {
                    String beginLeftNode = beginAppendages[0].getAttribute(ParseElementDefinition.BOND_BEGIN);
                    if (beginLeftNode.equals(bondBegin)) {
                        beginLeftNode = beginAppendages[0].getAttribute(ParseElementDefinition.BOND_END);
                    }
                    
                    String endLeftNode = endAppendages[1].getAttribute(ParseElementDefinition.BOND_BEGIN);
                    if (endLeftNode.equals(bondEnd)) {
                        endLeftNode = endAppendages[1].getAttribute(ParseElementDefinition.BOND_END);
                    }
                    
                    String beginRightNode = beginAppendages[1].getAttribute(ParseElementDefinition.BOND_BEGIN);
                    if (beginRightNode.equals(bondBegin)) {
                        beginRightNode = beginAppendages[1].getAttribute(ParseElementDefinition.BOND_END);
                    }
                    
                    String endRightNode = endAppendages[0].getAttribute(ParseElementDefinition.BOND_BEGIN);
                    if (endRightNode.equals(bondEnd)) {
                        endRightNode = endAppendages[0].getAttribute(ParseElementDefinition.BOND_END);
                    }
                    
                    // set position to the side with more double bonds
                    // if both sides are the same or there are no double bonds
                    // set position to the side with more bonds
                    if (environment.countDoubleBonds(beginLeftNode) + environment.countDoubleBonds(endLeftNode) <
                            environment.countDoubleBonds(beginRightNode) + environment.countDoubleBonds(endRightNode)) {
                        result = RIGHT_DOUBLE_POSITION;
                    } else if (environment.countDoubleBonds(beginLeftNode) + environment.countDoubleBonds(endLeftNode) >
                            environment.countDoubleBonds(beginRightNode) + environment.countDoubleBonds(endRightNode)) {
                        result = LEFT_DOUBLE_POSITION;
                    } else {
                        if (environment.getJoinedBonds(beginLeftNode).size() + environment.getJoinedBonds(endLeftNode).size() <=
                                environment.getJoinedBonds(beginRightNode).size() + environment.getJoinedBonds(endRightNode).size()) {
                            result = RIGHT_DOUBLE_POSITION;
                        } else {
                            result = LEFT_DOUBLE_POSITION;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public static ParsedElement[] findAppendages(ParsedElement bond, boolean begin) {
        ParsedElement[] result;
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_END)) {
            CDXMLEnvironment environment = (CDXMLEnvironment) bond.getEnvironment();
            List<ParsedElement> joinedBonds;
            
            if(begin){
                joinedBonds = environment.findBeginJoinedBonds(bond);
            } else{
                joinedBonds = environment.findEndJoinedBonds(bond);
            }
            
            String nodeId = (begin)?bond.getAttribute(ParseElementDefinition.BOND_BEGIN):bond.getAttribute(ParseElementDefinition.BOND_END);
            Line bondCoords = BondProcessor.parseCoords(bond);
            
            Point nodeCoords = (begin)?new Point(bondCoords.getBegin().getX(), bondCoords.getBegin().getY()):
                new Point(bondCoords.getEnd().getX(), bondCoords.getEnd().getY());
            
            Point otherEndCoords = (begin)?new Point(bondCoords.getEnd().getX(), bondCoords.getEnd().getY()):
                new Point(bondCoords.getBegin().getX(), bondCoords.getBegin().getY());
            
            double cosinePlus  = COSINE_THRESHOLD_OUT;
            double cosineMinus = COSINE_THRESHOLD_OUT;
            
            result = new ParsedElement[2];
            
            // If the atom at the begining (or at the end, depending on begin variable)
            //of the double bond has more than one other bond appended, then return.
            // This is for position the bond with align "center".
            // This is used BondPosition attributte is not present in the source file.
            if ((bond.hasAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH) && begin && joinedBonds.size() > 1)
            || (bond.hasAttribute(ParseElementDefinition.BOND_END_ATTACH) && !begin && joinedBonds.size() > 1)) {
                return result;
            }
            
            for (ParsedElement joinedBond : joinedBonds) {
                if (joinedBond == bond)
                    continue;
                
                Line coordinates = BondProcessor.parseCoords(joinedBond);
                
                Point newCoordinate = null;
                
                if (joinedBond.getAttribute(ParseElementDefinition.BOND_BEGIN).equals(nodeId)) {
                    newCoordinate = new Point(coordinates.getEnd().getX(), coordinates.getEnd().getY());
                } else {
                    newCoordinate = new Point(coordinates.getBegin().getX(), coordinates.getBegin().getY());
                }
                
                double cosine = GeometricOperations.cosine(newCoordinate.getX(), newCoordinate.getY(),
                        nodeCoords.getX(), nodeCoords.getY(),
                        otherEndCoords.getX(), otherEndCoords.getY());
                
                double orientation = GeometricOperations.orientation(otherEndCoords.getX(), otherEndCoords.getY(),
                        nodeCoords.getX(), nodeCoords.getY(),
                        newCoordinate.getX(), newCoordinate.getY());
                
                if (orientation > 0) {
                    if (cosine > cosinePlus && cosine <= COSINE_THRESHOLD_IN) {
                        result[1] = joinedBond;
                        cosinePlus = cosine;
                    }
                } else {
                    if (cosine > cosineMinus && cosine <= COSINE_THRESHOLD_IN) {
                        result[0] = joinedBond;
                        cosineMinus = cosine;
                    }
                }
            }
        } else {
            result = null;
        }
        return result;
    }
    
    protected double getX1() {
        return x1;
    }
    
    protected double getY1() {
        return y1;
    }
    
    protected double getX2() {
        return x2;
    }
    
    protected double getY2() {
        return y2;
    }
    
    protected String getOrder() {
        return bondOrder;
    }
    
    protected double getBondSpacing() {
        return bondSpacing;
    }
    
    /**
     * Calculate the begin point of the double line in a double bond.
     */
    protected translator.utils.Point calculateOffsetBeginDoubleBonds(translator.utils.Point doubleBondBegin,
            Point doubleBondEnd, double offset){
        
        return calculateOffsetBeginDoubleBonds(doubleBondBegin, doubleBondEnd, offset, true);        
    }
    
    /**
     * Calculate the begin point of the double line in a double bond.
     */
    protected translator.utils.Point calculateOffsetBeginDoubleBonds(translator.utils.Point doubleBondBegin,
            Point doubleBondEnd, double offset, boolean begin){
        Point result = null;        
        
        //get the begin appendages bonds
        ParsedElement[] beginAppendages = findAppendages(getElement(), begin);
        Line attachedBondCoordinates;
        Point intersection1;
        Point intersection2;
        
        Point attachedBondBeginPoint = new Point();
        Point attachedBondEndPoint = new Point();
        
        if (beginAppendages[0] != null){
            attachedBondCoordinates = parseCoords(beginAppendages[0]);
            if (beginAppendages[0].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))){
                attachedBondBeginPoint = new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondEndPoint = new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }else{
                attachedBondEndPoint = new Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondBeginPoint = new Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }
            //create two imaginary lines parallel to the attachedBond using the calculated bondspacing as offset
            double angle = GeometricOperations.angle(attachedBondBeginPoint, attachedBondEndPoint);
            double perpendicularAngle = angle + (Math.PI / 2);
            translator.utils.Point imaginaryPoint1Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, -offset);
            translator.utils.Point imaginaryPoint1End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, -offset);
            
            translator.utils.Point imaginaryPoint2Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, offset);
            translator.utils.Point imaginaryPoint2End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, offset);
            
            //the new point will be the real intersection between one of the imaginary lines and the double line of the double bond.
            intersection1 = GeometricOperations.realIntersection(imaginaryPoint1Begin, imaginaryPoint1End, doubleBondBegin, doubleBondEnd);
            intersection2 = GeometricOperations.realIntersection(imaginaryPoint2Begin, imaginaryPoint2End, doubleBondBegin, doubleBondEnd);
            
            Double distance1 = Double.MAX_VALUE;
            Double distance2 = Double.MAX_VALUE;
            if (intersection1 != null){
                distance1 = GeometricOperations.distance(intersection1, doubleBondEnd);
            }
            if (intersection2 != null){
                distance2 = GeometricOperations.distance(intersection2, doubleBondEnd);
            }
            
            if (distance1 < distance2){
                result = intersection1;
            }else if (distance1 > distance2){
                result = intersection2;
            }
        }
        if (beginAppendages[1] != null){
            attachedBondCoordinates = parseCoords(beginAppendages[1]);
            if (beginAppendages[1].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))){
                attachedBondBeginPoint = new translator.utils.Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondEndPoint = new translator.utils.Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }else{
                attachedBondEndPoint = new translator.utils.Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondBeginPoint = new translator.utils.Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }
            
            //create two imaginary lines parallel to the attachedBond using the calculated bondspacing as offset
            double angle = GeometricOperations.angle(attachedBondBeginPoint, attachedBondEndPoint);
            double perpendicularAngle = angle + (Math.PI / 2);
            translator.utils.Point imaginaryPoint1Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, -offset);
            translator.utils.Point imaginaryPoint1End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, -offset);
            
            translator.utils.Point imaginaryPoint2Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, offset);
            translator.utils.Point imaginaryPoint2End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, offset);
            
            //the new point will be the real intersection between one of the imaginary lines and the double line of the double bond.
            intersection1 = GeometricOperations.realIntersection(imaginaryPoint1Begin, imaginaryPoint1End, doubleBondBegin, doubleBondEnd);
            intersection2 = GeometricOperations.realIntersection(imaginaryPoint2Begin, imaginaryPoint2End, doubleBondBegin, doubleBondEnd);
            
            Double distance1 = Double.MAX_VALUE;
            Double distance2 = Double.MAX_VALUE;
            if (intersection1 != null){
                distance1 = GeometricOperations.distance(intersection1, doubleBondEnd);
            }
            if (intersection2 != null){
                distance2 = GeometricOperations.distance(intersection2, doubleBondEnd);
            }
            
            if (distance1 < distance2){
                result = intersection1;
            }else if (distance1 > distance2){
                result = intersection2;
            }
        }
        return result;
    }
    
    /**
     *Calculate the end point of the double line in a double bond.
     */
    protected translator.utils.Point calculateOffsetEndDoubleBonds(translator.utils.Point doubleBondBegin,
            translator.utils.Point doubleBondEnd, double offset){
        translator.utils.Point result = null;
        boolean begin = false;
        
        translator.utils.Line attachedBondCoordinates;
        translator.utils.Point intersection1;
        translator.utils.Point intersection2;
        
        //get the end appendages bonds
        ParsedElement[] endAppendages = findAppendages(getElement(), begin);
        
        translator.utils.Point attachedBondBeginPoint = new translator.utils.Point();
        translator.utils.Point attachedBondEndPoint = new translator.utils.Point();
        
        if (endAppendages[0] != null){
            attachedBondCoordinates = parseCoords(endAppendages[0]);
            if (endAppendages[0].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))){
                attachedBondBeginPoint = new translator.utils.Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondEndPoint = new translator.utils.Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }else{
                attachedBondEndPoint = new translator.utils.Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondBeginPoint = new translator.utils.Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }
            //create two imaginary lines parallel to the attachedBond using the calculated bondspacing as offset
            double angle = GeometricOperations.angle(attachedBondBeginPoint, attachedBondEndPoint);
            double perpendicularAngle = angle + (Math.PI / 2);
            translator.utils.Point imaginaryPoint1Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, -offset);
            translator.utils.Point imaginaryPoint1End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, -offset);
            
            translator.utils.Point imaginaryPoint2Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, offset);
            translator.utils.Point imaginaryPoint2End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, offset);
            
            //the new point will be the real intersection between one of the imaginary lines and the double line of the double bond.
            intersection1 = GeometricOperations.realIntersection(imaginaryPoint1Begin, imaginaryPoint1End, doubleBondBegin, doubleBondEnd);
            intersection2 = GeometricOperations.realIntersection(imaginaryPoint2Begin, imaginaryPoint2End, doubleBondBegin, doubleBondEnd);
            
            Double distance1 = Double.MAX_VALUE;
            Double distance2 = Double.MAX_VALUE;
            if (intersection1 != null){
                distance1 = GeometricOperations.distance(intersection1, doubleBondBegin);
            }
            if (intersection2 != null){
                distance2 = GeometricOperations.distance(intersection2, doubleBondBegin);
            }
            
            if (distance1 < distance2){
                result = intersection1;
            }else if (distance1 > distance2){
                result = intersection2;
            }
        }
        if (endAppendages[1] != null){
            attachedBondCoordinates = parseCoords(endAppendages[1]);
            if (endAppendages[1].getAttribute(ParseElementDefinition.BOND_BEGIN).equals(getElement().getAttribute(ParseElementDefinition.BOND_BEGIN))){
                attachedBondBeginPoint = new translator.utils.Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondEndPoint = new translator.utils.Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }else{
                attachedBondEndPoint = new translator.utils.Point(attachedBondCoordinates.getBegin().getX(), attachedBondCoordinates.getBegin().getY());
                
                attachedBondBeginPoint = new translator.utils.Point(attachedBondCoordinates.getEnd().getX(), attachedBondCoordinates.getEnd().getY());
            }
            
            //create two imaginary lines parallel to the attachedBond using the calculated bondspacing as offset
            double angle = GeometricOperations.angle(attachedBondBeginPoint, attachedBondEndPoint);
            double perpendicularAngle = angle + (Math.PI / 2);
            translator.utils.Point imaginaryPoint1Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, -offset);
            translator.utils.Point imaginaryPoint1End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, -offset);
            
            translator.utils.Point imaginaryPoint2Begin = GeometricOperations.offset(attachedBondBeginPoint, perpendicularAngle, offset);
            translator.utils.Point imaginaryPoint2End = GeometricOperations.offset(attachedBondEndPoint, perpendicularAngle, offset);
            
            //the new point will be the real intersection between one of the imaginary lines and the double line of the double bond.
            intersection1 = GeometricOperations.realIntersection(imaginaryPoint1Begin, imaginaryPoint1End, doubleBondBegin, doubleBondEnd);
            intersection2 = GeometricOperations.realIntersection(imaginaryPoint2Begin, imaginaryPoint2End, doubleBondBegin, doubleBondEnd);
            
            Double distance1 = Double.MAX_VALUE;
            Double distance2 = Double.MAX_VALUE;
            if (intersection1 != null){
                distance1 = GeometricOperations.distance(intersection1, doubleBondBegin);
            }
            if (intersection2 != null){
                distance2 = GeometricOperations.distance(intersection2, doubleBondBegin);
            }
            
            if (distance1 < distance2){
                result = intersection1;
            }else if (distance1 > distance2){
                result = intersection2;
            }
        }
        
        return result;
    }
}
