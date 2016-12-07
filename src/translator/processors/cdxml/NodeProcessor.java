
package translator.processors.cdxml;

import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.Font;
import translator.graphics.StyleElement;
import translator.graphics.shapes.builders.configurations.CircleConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeShapeConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.LineCap;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.graphics.shapes.builders.configurations.SplineConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.graphics.shapes.gradients.RadialGradient;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class NodeProcessor extends CDXMLProcessor {
    
    private static final double POLYMER_BEAD_FONT_SIZE_PROPORTION = 0.75;
    public static final double DIAMOND_FONT_SIZE_PROPORTION = 0.75;
    private static final String STAR_SYMBOL_FONT = "Times New Roman";
    private static final String STAR_CHARACTER = "*";
    private static final int FULL_WHITE_COLOR = 765;
    
    private Point position;
    private String nodeType;
    
    public NodeProcessor() {
    }
    
    protected void configure() {
        super.configure();
        
        ParsedElement node = getElement();
        position = parseCoords(environment.getCoords(node.getId()), null);
        if (node.hasAttribute(ParseElementDefinition.NODE_TYPE)) {
            nodeType = node.getAttribute(ParseElementDefinition.NODE_TYPE);
        }
    }
    
    protected void cleanup() {
        position = null;
        nodeType = null;
        super.cleanup();
    }
    
    protected void process() {
        ParsedElement node = getElement();
        
        if (node.hasAttribute(ParseElementDefinition.NODE_H_DOT) &&
                node.getAttribute(ParseElementDefinition.NODE_H_DOT).equalsIgnoreCase(ParseElementDefinition.NODE_H_DOT_YES) ||
                node.hasAttribute(ParseElementDefinition.NODE_H_DASH) &&
                node.getAttribute(ParseElementDefinition.NODE_H_DASH).equalsIgnoreCase(ParseElementDefinition.NODE_H_DASH_YES)) {
            
            double dotSize = 5 * lineWidth / 2;
            
            if (node.hasAttribute(ParseElementDefinition.NODE_H_DOT) &&
                    node.getAttribute(ParseElementDefinition.NODE_H_DOT).equalsIgnoreCase(ParseElementDefinition.NODE_H_DOT_YES)) {
                CircleConfiguration dotCircle = new CircleConfiguration(position, dotSize);
                dotCircle.setFill(true);
                dotCircle.setColor(color);
                dotCircle.setStrokeWidth(0);
                dotCircle.setZOrder(zOrder);
                setResultingConfiguration(dotCircle);
            } else if (node.hasAttribute(ParseElementDefinition.NODE_H_DASH) &&
                    node.getAttribute(ParseElementDefinition.NODE_H_DASH).equalsIgnoreCase(ParseElementDefinition.NODE_H_DASH_YES)) {
                
                List<ShapeBuilderConfiguration> innerShapes = new ArrayList();
                
                int dashDirection = findHDashDirection();
                
                Point dash1Begin = new Point(position.getX() - (dotSize + lineWidth) / 2,
                        position.getY() + (dotSize + lineWidth) * dashDirection - lineWidth / 2);
                Point dash1End = new Point(position.getX() + dotSize / 2, dash1Begin.getY());
                SegmentConfiguration dash1 = new SegmentConfiguration(dash1Begin, dash1End);
                dash1.setStrokeWidth(lineWidth);
                dash1.setColor(color);
                dash1.setZOrder(zOrder);
                innerShapes.add(dash1);
                
                Point dash2Begin = GeometricOperations.offset(dash1Begin, Math.PI / 2, dotSize * dashDirection);
                Point dash2End = GeometricOperations.offset(dash1End, Math.PI / 2, dotSize * dashDirection);
                SegmentConfiguration dash2 = new SegmentConfiguration(dash2Begin, dash2End);
                dash2.setStrokeWidth(lineWidth);
                dash2.setColor(color);
                dash2.setZOrder(zOrder);
                innerShapes.add(dash2);
                
                CompositeShapeConfiguration result = new CompositeShapeConfiguration("HDash"+node.getId(),
                        innerShapes);
                result.setZOrder(zOrder);
                setResultingConfiguration(result);
            }
        } else if (node.hasAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE) &&
                !node.getAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE).equals(
                ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_UNSPECIFIED)) {
            List<ParsedElement> bonds = environment.getJoinedBonds(node.getId());
            ParsedElement bond = bonds.get(0);
            
            double bondLineWidth;
            if (bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)) {
                bondLineWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
            } else {
                bondLineWidth = lineWidth;
            }
            
            double bondBoldWidth;
            if (bond.hasAttribute(ParseElementDefinition.BOND_BOLD_WIDTH)) {
                bondBoldWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_BOLD_WIDTH));
            } else {
                bondBoldWidth = boldWidth;
            }
            String externalConnectionType = node.getAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE);
            if (externalConnectionType != null){
                if (externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_POLYMER_BEAD)) {
                    double dotSize = (environment.getLabelFontSize() * POLYMER_BEAD_FONT_SIZE_PROPORTION + lineWidth * 2) * 2;
                    CircleConfiguration polymerCircle = new CircleConfiguration(position, dotSize / 2);
                    polymerCircle.setId(getElement().getId());
                    polymerCircle.setColor(color);
                    polymerCircle.setStrokeWidth(lineWidth);
                    polymerCircle.setZOrder(zOrder);
                    
                    if(color.getBlue()+color.getGreen()+color.getRed() != FULL_WHITE_COLOR) {
                        polymerCircle.setShaded(true);
                        polymerCircle.setGradient(RadialGradient.getOvalGradient(getElement().getId(), getColor()));
                    }else{
                        polymerCircle.setFill(true);
                        polymerCircle.setFillColor( color);
                    }
                    setResultingConfiguration(polymerCircle);
                } else if (externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_WAVY)) {
                    String otherNodeId;
                    if (bond.getAttribute(ParseElementDefinition.BOND_BEGIN).equals(node.getId())) {
                        otherNodeId = bond.getAttribute(ParseElementDefinition.BOND_END);
                    } else {
                        otherNodeId = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
                    }
                    
                    Point otherPoint = parseCoords(environment.getCoords(otherNodeId), node);
                    double deltaX = position.getX() - otherPoint.getX();
                    double deltaY = position.getY() - otherPoint.getY();
                    
                    double length = GeometricOperations.distance(deltaX, deltaY);
                    deltaX /= length;
                    deltaY /= length;
                    double oldDeltaX = deltaX;
                    deltaX = -deltaY;
                    deltaY = oldDeltaX;
                    
                    double dotSize = (environment.getLabelFontSize() * POLYMER_BEAD_FONT_SIZE_PROPORTION + bondLineWidth * 2);
                    deltaX *= dotSize;
                    deltaY *= dotSize;
                    
                    Point newStart = new Point(position.getX() - deltaX, position.getY() - deltaY);
                    Point newEnd = new Point(position.getX() + deltaX, position.getY() + deltaY);
                    List<SegmentConfiguration> wavySegments = environment.getWavySegments(newStart, newEnd, bondBoldWidth / 2);
                    SplineConfiguration wavySpline = new SplineConfiguration(wavySegments);
                    wavySpline.setColor(color);
                    wavySpline.setStrokeWidth(bondLineWidth);
                    wavySpline.setLineCap(LineCap.Round);
                    wavySpline.setZOrder(zOrder);
                    setResultingConfiguration(wavySpline);
                } else if (externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_STAR)) {
                    TextLayout starLayout = environment.createTextLayout(STAR_CHARACTER, position.getX(), position.getY(),
                            STAR_SYMBOL_FONT, java.awt.Font.PLAIN,
                            (float)environment.getLabelFontSize(), 0);
                    
                    // center asterisk on the atom
                    double yOffset = starLayout.getAscent() / 2;
                    double xOffset = -0.4;
                    
                    CompositeTextConfiguration starText = new CompositeTextConfiguration(node.getId());
                    
                    TextConfiguration star = new TextConfiguration(position.getX() + xOffset, position.getY() + yOffset, 1);
                    Font symbolFont = new Font();
                    symbolFont.setName(STAR_SYMBOL_FONT);
                    symbolFont.setCharSet(node.getAttribute(ParseElementDefinition.STRING_CHAR_SET));
                    symbolFont.setSize(String.valueOf(environment.getLabelFontSize()));
                    symbolFont.setStyle(new StyleElement(environment.getLabelFace()));
                    star.addPart(STAR_CHARACTER, symbolFont, color);
                    
                    starText.addLine(star);
                    
                    starText.setJustification(TextConfiguration.CENTER_JUSTIFICATION);
                    starText.setZOrder(zOrder);
                    
                    setResultingConfiguration(starText);
                } else if (externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_DIAMOND)) {
                    List<ShapeBuilderConfiguration> innerShapes = new ArrayList();
                    
                    int bondZOrder = bond.getZOrder();
                    bondZOrder += 1; // make sure it's always over the bond
                    
                    double fontSize = environment.getLabelFontSize() * DIAMOND_FONT_SIZE_PROPORTION;
                    double dotSize = (fontSize + bondLineWidth * 2);
                    double unitSize = dotSize / 2;
                    
                    Point leftCorner = new Point(position.getX() - unitSize, position.getY());
                    Point topCorner = new Point(position.getX(), position.getY() - unitSize);
                    Point rightCorner = new Point(position.getX() + unitSize, position.getY());
                    Point bottomCorner = new Point(position.getX(), position.getY() + unitSize);
                    
                    SegmentConfiguration leftTop = new SegmentConfiguration(leftCorner, topCorner);
                    SegmentConfiguration topRight = new SegmentConfiguration(topCorner, rightCorner);
                    SegmentConfiguration rightBottom = new SegmentConfiguration(rightCorner, bottomCorner);
                    
                    List<SegmentConfiguration> diamondSegments = new ArrayList();
                    diamondSegments.add(leftTop);
                    diamondSegments.add(topRight);
                    diamondSegments.add(rightBottom);
                    SplineConfiguration diamondSpline = new SplineConfiguration(diamondSegments);
                    diamondSpline.setColor(color);
                    diamondSpline.setFill(true);
                    diamondSpline.setStrokeWidth(0);
                    diamondSpline.setClosed(true);
                    diamondSpline.setZOrder(bondZOrder);
                    
                    innerShapes.add(diamondSpline);
                    
                    String rankNumber = String.valueOf(environment.getAttachmentPointRank(node));
                    String fontName = node.getAttribute(ParseElementDefinition.STRING_FONT);
                    
                    TextLayout rankLayout = environment.createTextLayout(rankNumber, position.getX(), position.getY(),
                            fontName, java.awt.Font.PLAIN,
                            (float)fontSize, 0);
                    
                    // center number on the atom
                    double yOffset = rankLayout.getAscent() * 0.4;
                    double xOffset = - 0.2;
                    
                    CompositeTextConfiguration rankIndicatorText = new CompositeTextConfiguration(node.getId());
                    
                    TextConfiguration rankIndicator = new TextConfiguration(position.getX() + xOffset, position.getY() + yOffset, 1);
                    Font labelFont = new Font();
                    labelFont.setName(fontName);
                    labelFont.setSize(String.valueOf(fontSize));
                    labelFont.setStyle(new StyleElement(environment.getLabelFace()));
                    labelFont.setCharSet(node.getAttribute(ParseElementDefinition.STRING_CHAR_SET));
                    rankIndicator.addPart(rankNumber, labelFont, convertColor(environment.getBackgroundColor()));
                    
                    rankIndicatorText.setJustification(TextConfiguration.CENTER_JUSTIFICATION);
                    rankIndicatorText.setZOrder(bondZOrder);
                    
                    rankIndicatorText.addLine(rankIndicator);
                    
                    innerShapes.add(rankIndicatorText);
                    
                    CompositeShapeConfiguration resultingAttachmentPoint = new CompositeShapeConfiguration(
                            "Diamond"+node.getId(),
                            innerShapes);
                    resultingAttachmentPoint.setZOrder(bondZOrder);
                    
                    setResultingConfiguration(resultingAttachmentPoint);
                }
            }
        } else if (nodeType != null &&
                (nodeType.equalsIgnoreCase(ParseElementDefinition.NODE_TYPE_VARIABLE_ATTACHMENT) ||
                nodeType.equalsIgnoreCase(ParseElementDefinition.NODE_TYPE_MULTI_ATTACHMENT))) {
            
            List<ShapeBuilderConfiguration> innerShapes = new ArrayList();
            
            // Taken from C++ code
            if (nodeType.equalsIgnoreCase(ParseElementDefinition.NODE_TYPE_VARIABLE_ATTACHMENT)) {
                double dotSize = lineWidth * 2;
                
                CircleConfiguration variableAttachmentDot = new CircleConfiguration(position, dotSize);
                variableAttachmentDot.setFill(true);
                variableAttachmentDot.setStrokeWidth(0);
                innerShapes.add(variableAttachmentDot);
            }
            
            double offset = - lineWidth / 2;
            Point offsetPosition = position.add(new Point(offset, offset));
            double markerSize1 = lineWidth * 2;
            double markerSize2 = lineWidth * 2 * Math.sqrt(3);
            
            SegmentConfiguration line1 = new SegmentConfiguration(
                    new Point(offsetPosition.getX(), offsetPosition.getY() - markerSize1 * 2),
                    new Point(offsetPosition.getX(), offsetPosition.getY() + markerSize1 * 2));
            line1.setStrokeWidth(lineWidth);
            SegmentConfiguration line2 = new SegmentConfiguration(
                    new Point(offsetPosition.getX() - markerSize2, offsetPosition.getY() - markerSize1),
                    new Point(offsetPosition.getX() + markerSize2, offsetPosition.getY() + markerSize1));
            line2.setStrokeWidth(lineWidth);
            SegmentConfiguration line3 = new SegmentConfiguration(
                    new Point(offsetPosition.getX() - markerSize2, offsetPosition.getY() + markerSize1),
                    new Point(offsetPosition.getX() + markerSize2, offsetPosition.getY() - markerSize1));
            line3.setStrokeWidth(lineWidth);
            
            innerShapes.add(line1);
            innerShapes.add(line2);
            innerShapes.add(line3);
            
            CompositeShapeConfiguration resultingAttachment = new CompositeShapeConfiguration(
                    nodeType+node.getId(),
                    innerShapes);
            resultingAttachment.setZOrder(zOrder);
            resultingAttachment.setColor(color);
            
            setResultingConfiguration(resultingAttachment);
        }
    }
    
    private int findHDashDirection() {
        ParsedElement node = getElement();
        
        double maxUpSlope = 0;
        double maxDownSlope = 0;
        int result = 1;
        List<ParsedElement> bonds = environment.getJoinedBonds(node.getId());
        for (ParsedElement bond : bonds) {
            String otherNodeId;
            if (bond.getAttribute(ParseElementDefinition.BOND_BEGIN).equals(node.getId())) {
                otherNodeId = bond.getAttribute(ParseElementDefinition.BOND_END);
            } else {
                otherNodeId = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
            }
            
            Point otherPoint = parseCoords(environment.getCoords(otherNodeId), node);
            double deltaX = otherPoint.getX() - position.getX();
            double deltaY = otherPoint.getY() - position.getY();
            
            if (deltaX == 0) {
                if (deltaY < 0) {
                    maxUpSlope = 9999999;
                } else {
                    maxDownSlope = 9999999;
                }
                break;
            }
            
            double slope = GeometricOperations.slope(position.getX(), position.getY(), otherPoint.getX(), otherPoint.getY());
            double slopeAbs = Math.abs(slope);
            if (deltaY < 0) {
                if (slopeAbs > maxUpSlope) {
                    maxUpSlope = slopeAbs;
                }
            } else {
                if (slopeAbs > maxDownSlope) {
                    maxDownSlope = slopeAbs;
                }
            }
        }
        if (maxUpSlope < maxDownSlope) {
            result = -1;
        }
        
        return result;
    }
}
