
package translator.cdxml;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import translator.Environment;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cdxml.dom.N;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.graphics.StyleElement;
import translator.graphics.shapes.builders.configurations.ArcConfiguration;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.CubicCurveConfiguration;
import translator.graphics.shapes.builders.configurations.QuadraticCurveConfiguration;
import translator.graphics.shapes.builders.configurations.SegmentConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.processors.cdxml.BondProcessor;
import translator.processors.cdxml.CDXMLProcessor;
import translator.processors.cdxml.ExtendedGeneralPath;
import translator.utils.AlgebraicOperations;
import translator.utils.GeometricLine;
import translator.utils.GeometricOperations;
import translator.utils.JoinPointResult;
import translator.utils.OverlapJoinPoint;
import translator.utils.Point;
import translator.utils.JoinPoint;
import translator.utils.Line;

public class CDXMLEnvironment extends Environment {
    
    public static String OVERLAP_JOIN_POINT_RIGHT_1 = "overlapJoinPoint1Right";
    public static String OVERLAP_JOIN_POINT_CENTER_1 = "overlapJoinPoint1Center";
    public static String OVERLAP_JOIN_POINT_LEFT_1 = "overlapJoinPoint1Left";
    public static String OVERLAP_JOIN_POINT_RIGHT_2 = "overlapJoinPoint2Right";
    public static String OVERLAP_JOIN_POINT_CENTER_2 = "overlapJoinPoint2Center";
    public static String OVERLAP_JOIN_POINT_LEFT_2 = "overlapJoinPoint2Left";
    
    private static String RIGHT_ALIGNMENT = "Right";
    private static String LEFT_ALIGNMENT = "Left";
    private static String CENTER_ALIGNMENT = "Center";
    
    protected static int RIGHT_DOUBLE_POSITION = 0;
    protected static int LEFT_DOUBLE_POSITION = 1;
    protected static int CENTER_DOUBLE_POSITION = 2;
    
    // Taken from ChemDraw C++
    private static double COSINE_THRESHOLD_IN = Math.cos(Math.PI / 12);
    private static double COSINE_THRESHOLD_OUT = -0.9767;
    
    public static double WEDGED_WIDTH_BOLD_RATIO = 1.5;
    
    public static final double COMPARISON_RECISION = 0.00001;
    
    // Taken from ChemDraw C++
    public static final int CD_KBONDSCALE = 12;
    private static final double CD_COSINE_170_DEGREES = -0.9848;
    
    private static final double FONT_SIZE_PERCENT_FOR_SUB_SUPERSCRIPT = 0.75;
    
    //The following contants differ from the two used in C# code line
    //because of the different starting drawing points between the two platforms
    private static final double LINE_HEIGHT_PERCENT_FOR_SUPERSCRIPT = 0.55; //1.25 in C# code line
    private static final double LINE_HEIGHT_PERCENT_FOR_SUBSCRIPT = 0.25; //0.25 in C# code line
    
    private static final int RENDER_CONTEXT_WIDTH = 100;
    private static final int RENDER_CONTEXT_HIGHT = 100;
    
    private Hashtable<String, String> coordinatesByNodeId = new Hashtable();
    private Hashtable<String, ParsedElement> nodeById = new Hashtable();
    private Hashtable<String, String> marginWidthsByNodeId = new Hashtable();
    private Hashtable<String, ParsedElement> attachedText = new Hashtable();
    private Map<String, List<ParsedElement>> joinedBonds = new LinkedHashMap();
    private Map<String, Set<ParsedElement>> nodesZOrderByFragment = new HashMap();
    private Map<String, List<ParsedElement>> nodeIndexesByZOrderByFragment = new HashMap();
    private Map<ParsedElement, String> fragmentIdsByNode = new HashMap();
    private List<String> floatingAttachmentPoints = new ArrayList();
    
    private List<String> bondsId = new ArrayList();
    private Hashtable<String, ParsedElement> bonds = new Hashtable();
    
    private Hashtable<String, List<ParsedElement>> crossBonds = new Hashtable();
    
    private Hashtable<String, Hashtable<String, Point>> bondJoinPoint = new Hashtable();
    
    // The following Hashtables are used to preserve the calculated mitering points of
    //joined bonds taking care about the position of the node.
    private Hashtable<String, JoinPointResult> bondJoinPointBeginResults = new Hashtable();
    private Hashtable<String, JoinPointResult> bondJoinPointEndResults = new Hashtable();
    
    private Hashtable<String, Hashtable<String, Point>> bondOverlapJoinPoint = new Hashtable();
    
    private Hashtable<String, CompositeTextConfiguration> compositeTextConfigurations = new Hashtable();
    
    private double lineWidth;
    private double boldWidth;
    private double bondSpacing;
    private double bondLength;
    private double hashSpacing;
    private double marginWidth;
    private double captionFontSize;
    private double labelFontSize;
    private String captionFont;
    private String labelFont;
    private String labelFace;
    private String fileName;
    
    private ParsedElement backgroundColor;
    private ParsedElement foregroundColor;
    
    private Hashtable<String, ParsedElement> geometricPlaneObjects = new Hashtable();
    
    private FontRenderContext renderContext  =  null;
    
    private CDXMLEnvironment(){
        renderContext = getRenderContext();
    }
    
    protected FontRenderContext getRenderContext(){
        Graphics2D graphics = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB));
        // The following rendering hint takes into account sub-pixel accuracy.
        // When this hint is set on, getAdvance() method returns size of glyph in real number, not real Integer only
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        FontRenderContext renderContext = graphics.getFontRenderContext(); 
        return renderContext;
    }
    
    public void addN(N node) {
        addN(node, null);
    }
    
    public void addN(N node, ParsedElement text) {
        if(node.getXyz() == null){
            coordinatesByNodeId.put(node.getId(), node.getP());
        } else {
            coordinatesByNodeId.put(node.getId(), node.getXyz());
        }
        
        if (node.getMarginWidth() != null) {
            marginWidthsByNodeId.put(node.getId(), node.getMarginWidth());
        }
        
        if(text != null){
            ParsedElement attachedText = text.copy();
            
            List<ParsedElement> subElements = attachedText.getElements("S");
            
            ParsedElement string = subElements.get(0);
            
            // use the whole text as value
            StringBuilder wholeText = new StringBuilder();
            for (ParsedElement substring : subElements) {
                wholeText.append(substring.getValue());
            }
            
            attachedText.setValue(wholeText.toString());
            
            if (attachedText.hasAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT)) {
                if(attachedText.getAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT).equals(ParseElementDefinition.TEXT_ALIGNMENT_BELOW)){
                    string.setValue(string.getValue().substring(0, 1));
                } else if(attachedText.getAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT).equals(ParseElementDefinition.TEXT_ALIGNMENT_ABOVE)
                && string.getValue().length() != 1){
                    double lineHeight = getLineHeight(attachedText);
                    double spacing = lineHeight;
                    
                    String[] point = attachedText.getAttribute(ParseElementDefinition.TEXT_POSITION).split(" ");
                    double x = Double.parseDouble(point[0]);
                    double y = Double.parseDouble(point[1]);
                    
                    double rotationAngle = 0;
                    if (attachedText.hasAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE)) {
                        rotationAngle = createAngle(text.getAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE));
                        rotationAngle = Math.toRadians(rotationAngle);
                    }
                    
                    rotationAngle += Math.PI / 2;
                    Point newPosition = GeometricOperations.offset(x, y, rotationAngle, spacing);
                    
                    attachedText.addAttribute(ParseElementDefinition.TEXT_POSITION,
                            newPosition.getX() + " " + newPosition.getY());
                    
                    string.setValue(string.getValue().substring(0, 1));
                    
                }
            }
            
            this.attachedText.put(node.getId(), attachedText);
        }
        
        if (!joinedBonds.containsKey(node.getId())) {
            joinedBonds.put(node.getId(), new ArrayList());
        }
    }
    
    public void addFloatingAttachmentPoint(String nodeId) {
        floatingAttachmentPoints.add(nodeId);
    }
    
    public void addGeometricPlaneObject(String id, ParsedElement element){
        geometricPlaneObjects.put(id, element);
    }
    
    public ParsedElement getGeometricPlaneObject(String id){
        return geometricPlaneObjects.get(id);
    }
    
    public boolean isFloatingAttachmentPoint(String nodeId) {
        return floatingAttachmentPoints.contains(nodeId);
    }
    
    public void addParsedElement(ParsedElement bond) {
        if (bond.getName().endsWith("Bond")) {
            bondsId.add(bond.getId());
            bonds.put(bond.getId(), bond);
            
            bondJoinPoint.put(bond.getId(), new Hashtable());
            bondOverlapJoinPoint.put(bond.getId(), new Hashtable());
            
            if (!bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) || !bond.hasAttribute(ParseElementDefinition.BOND_END)) {
                throw new IllegalArgumentException("Element is corrupt.");
            }
            String begin = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
            joinedBonds.get(begin).add(bond);
            
            String end = bond.getAttribute(ParseElementDefinition.BOND_END);
            joinedBonds.get(end).add(bond);
        } else {
            throw new IllegalArgumentException("Element must be a bond.");
        }
    }
    
    public List<ParsedElement> getJoinedBonds(String id){
        return joinedBonds.get(id);
    }
    
    /**
     * Helper method to count the number of double bonds that are attached to a node
     */
    public int countDoubleBonds(String nodeId) {
        int result = 0;
        for (ParsedElement joinedBond : getJoinedBonds(nodeId)) {
            if (joinedBond.hasAttribute(ParseElementDefinition.BOND_ORDER) &&
                    joinedBond.getAttribute(ParseElementDefinition.BOND_ORDER).equals(ParseElementDefinition.BOND_ORDER_2)) {
                result++;
            }
        }
        return result;
    }
    
    // Taken from C++ code
    /**
     * Evaluates if the given nodeId corresponds to an allene node
     */
    public boolean isAllene(String nodeId) {
        boolean result;
        
        List<ParsedElement> joinedBonds = getJoinedBonds(nodeId);
        if (joinedBonds.size() < 2) {
            result = false;
        } else {
            List<String> nodes = new ArrayList();
            for (ParsedElement joinedBond : joinedBonds) {
                if (joinedBond.hasAttribute(ParseElementDefinition.BOND_ORDER)) {
                    String order = joinedBond.getAttribute(ParseElementDefinition.BOND_ORDER);
                    if (order.equals(ParseElementDefinition.BOND_ORDER_2) ||
                            order.equals(ParseElementDefinition.BOND_ORDER_1_5)) {
                        String otherNodeId = joinedBond.getAttribute(ParseElementDefinition.BOND_BEGIN);
                        if (otherNodeId.equals(nodeId)) {
                            otherNodeId = joinedBond.getAttribute(ParseElementDefinition.BOND_END);
                        }
                        nodes.add(otherNodeId);
                    }
                }
            }
            
            if (nodes.size() == 2) {
                Point thisNodeCoordinates = CDXMLProcessor.parseCoords(getCoords(nodeId), null);
                Point firstNodeCoordinates = CDXMLProcessor.parseCoords(getCoords(nodes.get(0)), null);
                Point secondNodeCoordinates = CDXMLProcessor.parseCoords(getCoords(nodes.get(1)), null);
                result = GeometricOperations.cosine(firstNodeCoordinates, thisNodeCoordinates, secondNodeCoordinates) < CD_COSINE_170_DEGREES;
            } else {
                result = false;
            }
        }
        
        return result;
    }
    
    public List<ParsedElement> findBeginJoinedBonds(ParsedElement bond){
        return findJoinedBonds(bond, true);
    }
    
    public List<ParsedElement> findEndJoinedBonds(ParsedElement bond){
        return findJoinedBonds(bond, false);
    }
    
    private List<ParsedElement> findJoinedBonds(ParsedElement bond, boolean beginning) {
        if (!bond.getName().endsWith(ParseElementDefinition.BOND)) {
            throw new IllegalArgumentException("Element must be a bond.");
        }
        List<ParsedElement> result = new ArrayList();
        
        if (!bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) || !bond.hasAttribute(ParseElementDefinition.BOND_END)) {
            throw new IllegalArgumentException("Element is corrupt.");
        }
        
        if (beginning) {
            String begin = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
            result.addAll(joinedBonds.get(begin));
        } else {
            String end = bond.getAttribute(ParseElementDefinition.BOND_END);
            result.addAll(joinedBonds.get(end));
        }
        
        return result;
    }
    
    public boolean isBeginJoined(ParsedElement bond){
        boolean result = false;
        List<ParsedElement> begin = findBeginJoinedBonds(bond);
        result = (begin != null && begin.size() > 1);
        
        return result;
    }
    
    public boolean isEndJoined(ParsedElement bond){
        boolean result = false;
        List<ParsedElement> end = findEndJoinedBonds(bond);
        result = (end != null && end.size() > 1);
        
        return result;
    }
    
    public boolean isJoined(ParsedElement bond1, ParsedElement bond2){
        List<ParsedElement> endJoinedBonds = findEndJoinedBonds(bond1);
        boolean result = false;
        
        for(ParsedElement endBond : endJoinedBonds){
            if(endBond.getId().equals(bond2.getId())){
                result = true;
                break;
            }
        }
        if(!result){
            List<ParsedElement> beginJoinedBonds = findBeginJoinedBonds(bond1);
            
            for(ParsedElement beginBond : beginJoinedBonds){
                if(beginBond.getId().equals(bond2.getId())){
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    public List<ParsedElement> getCrossBonds(String bondId){
        return crossBonds.get(bondId);
    }
    
    public void addAttachmentPoint(String fragmentId, ParsedElement node) {
        Set<ParsedElement> nodesZOrder = nodesZOrderByFragment.get(fragmentId);
        if (nodesZOrder == null) {
            nodesZOrder = new TreeSet(new NodeZOrderComparator());
            nodesZOrderByFragment.put(fragmentId, nodesZOrder);
        }
        nodesZOrder.add(node);
        fragmentIdsByNode.put(node, fragmentId);
    }
    
    public int getAttachmentPointRank(ParsedElement node) {
        String fragmentId = fragmentIdsByNode.get(node);
        List<ParsedElement> nodeIndexesByZOrder = nodeIndexesByZOrderByFragment.get(fragmentId);
        if (nodeIndexesByZOrder == null) {
            nodeIndexesByZOrder = new ArrayList();
            nodeIndexesByZOrderByFragment.put(fragmentId, nodeIndexesByZOrder);
        }
        if (nodeIndexesByZOrder.size() != nodesZOrderByFragment.get(fragmentId).size()) {
            nodeIndexesByZOrder.clear();
            nodeIndexesByZOrder.addAll(nodesZOrderByFragment.get(fragmentId));
        }
        return nodeIndexesByZOrder.indexOf(node) + 1;
    }
    
    public void checkCrossBond(ParsedElement bond){
        if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN)
        && bond.hasAttribute(ParseElementDefinition.BOND_END)
        && bond.hasAttribute(ParseElementDefinition.BOND_CROSSING_BOND)) {
            
            String[] crossingBonds = bond.getAttribute(ParseElementDefinition.BOND_CROSSING_BOND).split(" ");
            
            int bondZOrder = bond.getZOrder();
            
            for(String id : crossingBonds){
                
                ParsedElement crossBond = bonds.get(id);
                
                int crossBondZOrder = crossBond.getZOrder();
                
                if(bondZOrder < crossBondZOrder){
                    if(crossBonds.get(bond.getId()) == null){
                        crossBonds.put(bond.getId(), new ArrayList());
                    }
                    
                    crossBonds.get(bond.getId()).add(crossBond);
                }else{
                    if(crossBonds.get(crossBond.getId()) == null){
                        crossBonds.put(crossBond.getId(), new ArrayList());
                    }
                    crossBonds.get(crossBond.getId()).add(bond);
                }
                
            }
        }
    }
    
    /**
     * This method add the missed crossing bonds attribute to bonds that intersect
     * to another bonds.
     */
    private void addMissedCrossingBonds(){
        Point begin1;
        Point end1;
        double endWidth1;
        double beginWidth1;
        List<Line> bondLines1;
        GeometricLine bondGeometricLine1;
        
        Point begin2;
        Point end2;
        double endWidth2;
        double beginWidth2;
        List<Line> bondLines2;
        GeometricLine bondGeometricLine2;
        
        boolean addCrossing;
        
        for (ParsedElement bond1 : bonds.values()) {
            
            if (bond1.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond1.hasAttribute(ParseElementDefinition.BOND_END)) {
                
                begin1 = new Point(coordinatesByNodeId.get(bond1.getAttribute(ParseElementDefinition.BOND_BEGIN)));
                end1 = new Point(coordinatesByNodeId.get(bond1.getAttribute(ParseElementDefinition.BOND_END)));
                
                endWidth1 = BondProcessor.getEndWidth(bond1);
                beginWidth1 = BondProcessor.getBeginWidth(bond1);
                
                //This fragement is for create a list of lines that are aproximated
                //to the border of the first bond.
                bondLines1 = new ArrayList<Line>();
                
                bondGeometricLine1 = new GeometricLine(bond1.getId(), begin1, end1, beginWidth1, endWidth1);
                
                bondLines1.add(new Line(bondGeometricLine1.getLeftBegin(), bondGeometricLine1.getLeftEnd()));
                bondLines1.add(new Line(bondGeometricLine1.getLeftEnd(), bondGeometricLine1.getRightEnd()));
                bondLines1.add(new Line(bondGeometricLine1.getRightEnd(), bondGeometricLine1.getRightBegin()));
                bondLines1.add(new Line(bondGeometricLine1.getRightBegin(), bondGeometricLine1.getLeftBegin()));
                
                String[] crossingBonds1 = new String[0];
                
                if(bond1.hasAttribute(ParseElementDefinition.BOND_CROSSING_BOND)){
                    crossingBonds1 = bond1.getAttribute(ParseElementDefinition.BOND_CROSSING_BOND).split(" ");
                }
                
                for (ParsedElement bond2 : bonds.values()) {
                    
                    if (bond2.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond2.hasAttribute(ParseElementDefinition.BOND_END)
                    && !bond1.getAttribute(ParseElementDefinition.BOND_BEGIN).equals(bond2.getAttribute(ParseElementDefinition.BOND_BEGIN))
                    && !bond1.getAttribute(ParseElementDefinition.BOND_BEGIN).equals(bond2.getAttribute(ParseElementDefinition.BOND_END))
                    && !bond1.getAttribute(ParseElementDefinition.BOND_END).equals(bond2.getAttribute(ParseElementDefinition.BOND_BEGIN))
                    && !bond1.getAttribute(ParseElementDefinition.BOND_END).equals(bond2.getAttribute(ParseElementDefinition.BOND_END))) {
                        
                        begin2 = new Point(coordinatesByNodeId.get(bond2.getAttribute(ParseElementDefinition.BOND_BEGIN)));
                        end2 = new Point(coordinatesByNodeId.get(bond2.getAttribute(ParseElementDefinition.BOND_END)));
                        
                        endWidth2 = BondProcessor.getEndWidth(bond2);
                        beginWidth2 = BondProcessor.getBeginWidth(bond2);
                        
                        //This fragement is for create a list of lines that are aproximated
                        //to the border of the second bond.
                        bondLines2 = new ArrayList<Line>();
                        
                        bondGeometricLine2 = new GeometricLine(bond2.getId(), begin2, end2, beginWidth2, endWidth2);
                        
                        bondLines2.add(new Line(bondGeometricLine2.getLeftBegin(), bondGeometricLine2.getLeftEnd()));
                        bondLines2.add(new Line(bondGeometricLine2.getLeftEnd(), bondGeometricLine2.getRightEnd()));
                        bondLines2.add(new Line(bondGeometricLine2.getRightEnd(), bondGeometricLine2.getRightBegin()));
                        bondLines2.add(new Line(bondGeometricLine2.getRightBegin(), bondGeometricLine2.getLeftBegin()));
                        
                        // Verify if the bonds perimeters are intersected almos in one of its lines
                        if(GeometricOperations.realIntersection(bondLines1, bondLines2)){
                            if(bond1.getAttribute(ParseElementDefinition.BOND_CROSSING_BOND) != null){
                                
                                addCrossing = true;
                                
                                // Verify if the bond to add to the crossing bond of the fist bond was
                                // not added before to the crossing bond attribute.
                                for(String currentCrossingBond : crossingBonds1){
                                    if(currentCrossingBond.equals(bond2.getId())){
                                        addCrossing = false;
                                    }
                                }
                                
                                // Verify if both bonds don't have the same angles
                                if(Math.abs(GeometricOperations.angle(begin1, end1)) == Math.abs(GeometricOperations.angle(begin2, end2))){
                                    addCrossing = false;
                                }
                                
                                if(addCrossing){
                                    bond1.addAttribute(ParseElementDefinition.BOND_CROSSING_BOND, (bond1.getAttribute(ParseElementDefinition.BOND_CROSSING_BOND)+" "+bond2.getId()).trim());
                                }
                                
                            }else{
                                bond1.addAttribute(ParseElementDefinition.BOND_CROSSING_BOND, bond2.getId());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void calculateBondMitering(){
        Set<String> nodes = joinedBonds.keySet();
        
        addMissedCrossingBonds();
        
        for (String currentNode : nodes) {
            List<ParsedElement> bonds = joinedBonds.get(currentNode);
            
            String center = coordinatesByNodeId.get(currentNode);
            String[] parts = center.split(" ");
            double centerX = Double.parseDouble(parts[0]);
            double centerY = Double.parseDouble(parts[1]);
            
            Point centerPoint = new Point(centerX, centerY);
            
            try {
                
                JoinPoint joinPoint = new JoinPoint(centerPoint, currentNode);
                OverlapJoinPoint overlapJoinPoint = new OverlapJoinPoint(centerPoint, currentNode);
                
                for (ParsedElement bond : bonds) {
                    if (bond.hasAttribute(ParseElementDefinition.BOND_BEGIN) && bond.hasAttribute(ParseElementDefinition.BOND_END) && bond.hasAttribute(ParseElementDefinition.BOND_DISPLAY) && bond.hasAttribute(ParseElementDefinition.BOND_ORDER)) {
                        String begin = coordinatesByNodeId.get(bond.getAttribute(ParseElementDefinition.BOND_BEGIN));
                        String end = coordinatesByNodeId.get(bond.getAttribute(ParseElementDefinition.BOND_END));
                        
                        boolean centerEnd = false;
                        
                        if (begin.equals(center)) {
                            parts = end.split(" ");
                        } else {
                            parts = begin.split(" ");
                            centerEnd = true;
                        }
                        
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        Point endPoint = new Point(x, y);
                        
                        String display = bond.getAttribute(ParseElementDefinition.BOND_DISPLAY);
                        
                        boolean isJoinPoint = false;
                        double beginWidth = getLineWidth();
                        double endWidth = getLineWidth();
                        
                        // define different widths for cases where bond width for overlapJoinPoint
                        // is not the same as the bond width for joinPoint
                        double overlapBeginWidth = -1;
                        double overlapEndWidth = -1;
                        
                        double overlapWidthOffset = getMarginWidth() * 2;
                        if (bond.hasAttribute(ParseElementDefinition.BOND_MARGIN_WIDTH)) {
                            overlapWidthOffset = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_MARGIN_WIDTH)) * 2;
                        }
                        
                        if(display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD)){
                            isJoinPoint = true;
                            if(!bond.hasAttribute(ParseElementDefinition.BOND_BOLD_WIDTH)){
                                beginWidth = endWidth = getBoldWidth();
                            }else{
                                beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_BOLD_WIDTH));
                            }
                            double bondLineWidth;
                            if(!bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)){
                                bondLineWidth = getLineWidth();
                            }else{
                                bondLineWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                            }
                            if (bond.hasAttribute(ParseElementDefinition.BOND_ORDER) &&
                                    bond.getAttribute(ParseElementDefinition.BOND_ORDER).equals(ParseElementDefinition.BOND_ORDER_2)) {
                                // double bold bond
                                // line width is half plain line, plus half bold line
                                overlapBeginWidth = overlapEndWidth = calculateOverlapBondWidth(bond, beginWidth / 2 + bondLineWidth / 2);
                                if(BondProcessor.calculateDoublePosition(bond)==CENTER_DOUBLE_POSITION){
                                    isJoinPoint = false;
                                }
                            } else {
                                overlapBeginWidth = overlapEndWidth = calculateOverlapBondWidth(bond, beginWidth);
                            }
                        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HASH)){
                            if(!bond.hasAttribute(ParseElementDefinition.BOND_BOLD_WIDTH)){
                                beginWidth = endWidth = getBoldWidth();
                            }else{
                                beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_BOLD_WIDTH));
                            }
                        } else if(bond.getName().equals(ParseElementDefinition.WAVY_2_BOND)){
                            if (bond.hasAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION)) {
                                if(!bond.getAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION).equals(ParseElementDefinition.BOND_DOUBLE_POSITION_CENTER)){
                                    isJoinPoint = true;
                                    if(!bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)){
                                        beginWidth = endWidth = getLineWidth();
                                    }else{
                                        beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                                    }
                                }
                            } else {
                                isJoinPoint = true;
                                if(!bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)){
                                    beginWidth = endWidth = getLineWidth();
                                }else{
                                    beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                                }
                            }
                            // double either bond overlaps like a plain double bond
                            overlapBeginWidth = overlapEndWidth = calculateOverlapBondWidth(bond, beginWidth);
                        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WAVY)){
                            beginWidth = endWidth = getBoldWidth();
                        } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_DATIVE)) {
                            if(!centerEnd){
                                isJoinPoint = true;
                            }
                            beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                        } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_SOLID)) {
                            String order = null;
                            if (bond.hasAttribute(ParseElementDefinition.BOND_ORDER) && bond.hasAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION)) {
                                order = bond.getAttribute(ParseElementDefinition.BOND_ORDER);
                                if (order.equals(ParseElementDefinition.BOND_ORDER_3) ||
                                        !order.equals(ParseElementDefinition.BOND_ORDER_4) && !bond.getAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION).equals(ParseElementDefinition.BOND_DOUBLE_POSITION_CENTER)) {
                                    isJoinPoint = true;
                                    if(!bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)){
                                        beginWidth = endWidth = getLineWidth();
                                    }else{
                                        beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                                    }
                                }
                            } else if (bond.hasAttribute(ParseElementDefinition.BOND_ORDER)) {
                                order = bond.getAttribute(ParseElementDefinition.BOND_ORDER);
                                if(!(order.equals(ParseElementDefinition.BOND_ORDER_2) &&
                                        (BondProcessor.calculateDoublePosition(bond)==CENTER_DOUBLE_POSITION))){
                                    isJoinPoint = true;
                                }
                                
                                if(!bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)){
                                    beginWidth = endWidth = getLineWidth();
                                }else{
                                    beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                                }
                            }
                            
                            overlapBeginWidth = overlapEndWidth = calculateOverlapBondWidth(bond, beginWidth);
                        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_BEGIN) ||
                                display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_END)){
                            if(centerEnd){
                                beginWidth = BondProcessor.getEndWidth(bond);
                                endWidth = BondProcessor.getBeginWidth(bond);
                            } else{
                                beginWidth = BondProcessor.getBeginWidth(bond);
                                endWidth = BondProcessor.getEndWidth(bond);
                            }
                        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_BEGIN) ||
                                display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_END) ||
                                display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_END) ||
                                display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_BEGIN)){
                            isJoinPoint = true;
                            if(centerEnd){
                                beginWidth = BondProcessor.getEndWidth(bond);
                                endWidth = BondProcessor.getBeginWidth(bond);
                            } else{
                                beginWidth = BondProcessor.getBeginWidth(bond);
                                endWidth = BondProcessor.getEndWidth(bond);
                            }
                        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_DASH)){
                            isJoinPoint = true;
                            if(!bond.hasAttribute(ParseElementDefinition.BOND_LINE_WIDTH)){
                                beginWidth = endWidth = getLineWidth();
                            }else{
                                beginWidth = endWidth = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_LINE_WIDTH));
                            }
                            overlapBeginWidth = overlapEndWidth = calculateOverlapBondWidth(bond, beginWidth);
                        }
                        
                        if (isJoinPoint) {
                            joinPoint.addPoint(bond.getId(), endPoint, beginWidth, endWidth);
                        }
                        
                        // if overlap widths were not initialized
                        // set them as beginWidth and endWidth
                        if (overlapBeginWidth == -1) {
                            overlapBeginWidth = beginWidth;
                        }
                        if (overlapEndWidth == -1) {
                            overlapEndWidth = endWidth;
                        }
                        
                        overlapJoinPoint.addPoint(bond.getId(), endPoint,
                                overlapBeginWidth + overlapWidthOffset,
                                overlapEndWidth + overlapWidthOffset,
                                overlapWidthOffset);
                    }
                }
                
                for (ParsedElement bond : bonds) {
                    JoinPointResult newPoints = joinPoint.getResult(bond.getId());
                    
                    if (newPoints != null) {
                        String joinPointNodeId = newPoints.getNodeId();
                        String bondBeginId = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
                        String bondEndId = bond.getAttribute(ParseElementDefinition.BOND_END);
                        
                        //This add the join points result in the correct Hashtable comparing
                        //the node id with the bond begin id.
                        if(bondBeginId.equals(joinPointNodeId)){
                            setBondJoinPointBeginResult(bond.getId(), newPoints);
                        } else{
                            setBondJoinPointEndResult(bond.getId(), newPoints);
                        }
                    }
                    
                    newPoints = overlapJoinPoint.getResult(bond.getId());
                    
                    if(newPoints != null){
                        
                        Line bondCoordinates = BondProcessor.parseCoords(bond);
                        
                        String joinPointNodeId = newPoints.getNodeId();
                        String bondBeginId = bond.getAttribute(ParseElementDefinition.BOND_BEGIN);
                        String bondEndId = bond.getAttribute(ParseElementDefinition.BOND_END);
                        
                        if(joinPointNodeId.equals(bondBeginId)){
                            setOverlapJoinPointRight1(bond.getId(), newPoints.getRight());
                            setOverlapJoinPointCenter1(bond.getId(), newPoints.getCenter());
                            setOverlapJoinPointLeft1(bond.getId(), newPoints.getLeft());
                        } else{
                            setOverlapJoinPointRight2(bond.getId(), newPoints.getRight());
                            setOverlapJoinPointCenter2(bond.getId(), newPoints.getCenter());
                            setOverlapJoinPointLeft2(bond.getId(), newPoints.getLeft());
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public double calculateOverlapBondWidth(ParsedElement bond, double bondWidth) {
        double bondSpacing = getBondSpacing();
        if(bond.hasAttribute(ParseElementDefinition.BOND_SPACING)){
            bondSpacing = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_SPACING));
        }
        
        // bondSpacing is a percentage
        // calculate actual bondSpacing using bond's length
        Line bondCoordinates = BondProcessor.parseCoords(bond);
        double bondLength = GeometricOperations.distance(bondCoordinates.getBegin(), bondCoordinates.getEnd());
        bondSpacing = bondLength * bondSpacing / 100;
        
        double bondOrder = 1;
        if (bond.hasAttribute(ParseElementDefinition.BOND_ORDER)) {
            try {
                bondOrder = Double.parseDouble(bond.getAttribute(ParseElementDefinition.BOND_ORDER));
            } catch (NumberFormatException e) {
                bondOrder = 1;//When the bond order is not a number the bond order is always 1;
            }
        }
        
        // order 1.5 is the same width as 2
        if (bondOrder == 1.5) {
            bondOrder = 2;
        }
        
        if (bondOrder > 1) {
            // multiple order bonds do not add bond width to the overlap width
            bondWidth = 0;
        }
        
        // overlapping width depends on the number of spaces between bond lines
        // which are, bondOrder - 1
        return bondWidth + bondSpacing * (bondOrder - 1);
    }
    
    /*
     * This static block is part of Singleton Pattern, which is applied to handle DocumentPreferences.
     */
    static{
        instance = new CDXMLEnvironment();
    }
    
    public static Environment getInstance() {
        return instance;
    }
    
    /*
     * This method is used to clean every CDXMLEnvironment atributtes except the preferences stored in Instance.
     */
    public void cleanUpEnvironment() {
        coordinatesByNodeId.clear();
        marginWidthsByNodeId.clear();
        attachedText.clear();
        joinedBonds.clear();
        nodesZOrderByFragment.clear();
        nodeIndexesByZOrderByFragment.clear();
        fragmentIdsByNode.clear();
        floatingAttachmentPoints.clear();
        bondsId.clear();
        bonds.clear();
        crossBonds.clear();
        bondJoinPoint.clear();
        bondJoinPointBeginResults.clear();
        bondJoinPointEndResults.clear();
        bondOverlapJoinPoint.clear();
        compositeTextConfigurations.clear();
        geometricPlaneObjects.clear();
        
        lineWidth = 0;
        boldWidth = 0;
        bondSpacing = 0;
        bondLength = 0;
        hashSpacing = 0;
        marginWidth = 0;
        captionFontSize = 0;
        labelFontSize = 0;
        captionFont = null;
        labelFont = null;
        labelFace = null;
        fileName = null;
        
        backgroundColor = null;
        foregroundColor = null;
    }
    
    public JoinPointResult getBondJoinPointBeginResult(String bondId) {
        return bondJoinPointBeginResults.get(bondId);
    }
    
    public void setBondJoinPointBeginResult(String bondId, JoinPointResult bondJoinPointBeginResult) {
        bondJoinPointBeginResults.put(bondId, bondJoinPointBeginResult);
    }
    
    public JoinPointResult getBondJoinPointEndResult(String bondId) {
        return bondJoinPointEndResults.get(bondId);
    }
    
    public void setBondJoinPointEndResult(String bondId, JoinPointResult bondJoinPointEndResult) {
        bondJoinPointEndResults.put(bondId, bondJoinPointEndResult);
    }
    
    public Point getOverlapJoinPointRight1(String bondId){
        return bondOverlapJoinPoint.get(bondId).get(OVERLAP_JOIN_POINT_RIGHT_1);
    }
    
    private void setOverlapJoinPointRight1(String bondId, Point point){
        bondOverlapJoinPoint.get(bondId).put(OVERLAP_JOIN_POINT_RIGHT_1, point);
    }
    
    public Point getOverlapJoinPointCenter1(String bondId){
        return bondOverlapJoinPoint.get(bondId).get(OVERLAP_JOIN_POINT_CENTER_1);
    }
    
    private void setOverlapJoinPointCenter1(String bondId, Point point){
        bondOverlapJoinPoint.get(bondId).put(OVERLAP_JOIN_POINT_CENTER_1, point);
    }
    
    public Point getOverlapJoinPointLeft1(String bondId){
        return bondOverlapJoinPoint.get(bondId).get(OVERLAP_JOIN_POINT_LEFT_1);
    }
    
    private void setOverlapJoinPointLeft1(String bondId, Point point){
        bondOverlapJoinPoint.get(bondId).put(OVERLAP_JOIN_POINT_LEFT_1, point);
    }
    
    public Point getOverlapJoinPointRight2(String bondId){
        return bondOverlapJoinPoint.get(bondId).get(OVERLAP_JOIN_POINT_RIGHT_2);
    }
    
    private void setOverlapJoinPointRight2(String bondId, Point point){
        bondOverlapJoinPoint.get(bondId).put(OVERLAP_JOIN_POINT_RIGHT_2, point);
    }
    
    public Point getOverlapJoinPointCenter2(String bondId){
        return bondOverlapJoinPoint.get(bondId).get(OVERLAP_JOIN_POINT_CENTER_2);
    }
    
    private void setOverlapJoinPointCenter2(String bondId, Point point){
        bondOverlapJoinPoint.get(bondId).put(OVERLAP_JOIN_POINT_CENTER_2, point);
    }
    
    public Point getOverlapJoinPointLeft2(String bondId){
        return bondOverlapJoinPoint.get(bondId).get(OVERLAP_JOIN_POINT_LEFT_2);
    }
    
    private void setOverlapJoinPointLeft2(String bondId, Point point){
        bondOverlapJoinPoint.get(bondId).put(OVERLAP_JOIN_POINT_LEFT_2, point);
    }
    
    public ParsedElement getAttachedText(String nodeId){
        return attachedText.get(nodeId);
    }
    
    public String getCoords(String nodeId) {
        return coordinatesByNodeId.get(nodeId);
    }
    
    public String getNodeMarginWidth(String nodeId) {
        return marginWidthsByNodeId.get(nodeId);
    }
    
    public JoinPoint getJoinPoint(String nodeId) {
        return joinPoints.get(nodeId);
    }
    
    public void setJoinPoint(String nodeId, JoinPoint joinPoint) {
        joinPoints.put(nodeId, joinPoint);
    }
    
    public double getLineWidth() {
        return lineWidth;
    }
    
    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }
    
    public double getBoldWidth() {
        return boldWidth;
    }
    
    public void setBoldWidth(double boldWidth) {
        this.boldWidth = boldWidth;
    }
    
    public double getBondSpacing() {
        return bondSpacing;
    }
    
    public void setBondSpacing(double bondSpacing) {
        this.bondSpacing = bondSpacing;
    }
    
    public double getHashSpacing() {
        return hashSpacing;
    }
    
    public void setHashSpacing(double hashSpacing) {
        this.hashSpacing = hashSpacing;
    }
    
    public double getMarginWidth() {
        return marginWidth;
    }
    
    public void setMarginWidth(double marginWidth) {
        this.marginWidth = marginWidth;
    }
    
    public ParsedElement getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(ParsedElement backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public Point calculateShapeMiddle(Shape shape){
        double[] highestPointsInX = calculateRangeInX(shape);
        double[] highestPointsInY = calculateRangeInY(shape);
        
        double x = ((highestPointsInX[1] - highestPointsInX[0]) / 2) + highestPointsInX[0];
        double y = ((highestPointsInY[1] - highestPointsInY[0]) / 2) + highestPointsInY[0];
        
        return new Point(x, y);
    }
    
    public double[] calculateRangeInX(Shape shape){
        double min = shape.getBounds().getX();
        double max = shape.getBounds().getX() + shape.getBounds().getWidth();
        
        return new double[] {min, max};
    }
    
    public double[] calculateRangeInY(Shape shape){
        double min = shape.getBounds().getY();
        double max = shape.getBounds().getY() + shape.getBounds().getHeight();
        
        return new double[] {min, max};
    }
    
    public List<Point> getPerimeterOfShape(Shape shape){
        List<Point> points = new ArrayList();
        
        PathIterator segments = shape.getPathIterator(null);
        double[] lastPart = new double[2];
        double[] part = new double[6];
        
        while(!segments.isDone()){
            List<Point> controlPoints = new ArrayList();
            int type = segments.currentSegment(part);
            
            if(type == PathIterator.SEG_LINETO){
                controlPoints.add(new Point(lastPart[0], lastPart[1]));
                controlPoints.add(new Point(part[0], part[1]));
                
                points.addAll(AlgebraicOperations.bezierFunction(0.1, controlPoints));
            } else if(type == PathIterator.SEG_QUADTO){
                controlPoints.add(new Point(lastPart[0], lastPart[1]));
                controlPoints.add(new Point(part[0], part[1]));
                controlPoints.add(new Point(part[2], part[3]));
                
                points.addAll(AlgebraicOperations.bezierFunction(0.1, controlPoints));
            } else if(type == PathIterator.SEG_CUBICTO){
                controlPoints.add(new Point(lastPart[0], lastPart[1]));
                controlPoints.add(new Point(part[0], part[1]));
                controlPoints.add(new Point(part[2], part[3]));
                controlPoints.add(new Point(part[4], part[5]));
                
                points.addAll(AlgebraicOperations.bezierFunction(0.1, controlPoints));
            }
            
            System.arraycopy(part, 0, lastPart, 0, lastPart.length);
            segments.next();
        }
        
        return points;
    }
    
    public Area createAreaFromText(ParsedElement text){
        String value = null;
        List<ParsedElement> subElements = text.getElements("S");
        
        if(subElements.size() > 0){
            value = subElements.get(0).getValue();
        } else{
            return null;
        }
        
        return createAreaFromText(text, 0, value.length());
    }
    
    public Area createAreaFromText(ParsedElement text, int begin){
        String value = null;
        List<ParsedElement> subElements = text.getElements(ParseElementDefinition.STRING);
        
        if(subElements.size() > 0){
            value = subElements.get(0).getValue();
        } else{
            return null;
        }
        
        return createAreaFromText(text, begin, value.length());
    }
    
    private TextLayout createTextLayout(ParsedElement text, boolean rotated) {
        TextLayout result;
        if (text.hasAttribute(ParseElementDefinition.TEXT_POSITION)) {
            
            Point point = CDXMLProcessor.parseCoords(text.getAttribute(ParseElementDefinition.TEXT_POSITION), text);
            
            double x = point.getX();
            double y = point.getY();
            
            String value = "";
            String fontName = "";
            String fontSize = "";
            int styles = java.awt.Font.PLAIN;
            
            List<ParsedElement> subElements = text.getElements(ParseElementDefinition.STRING);
            if(subElements.size() > 0){
                fontName = subElements.get(0).getAttribute(ParseElementDefinition.STRING_FONT);
                fontSize = subElements.get(0).getAttribute(ParseElementDefinition.STRING_SIZE);
                value = subElements.get(0).getValue();
                StyleElement style = new StyleElement(subElements.get(0).getAttribute(ParseElementDefinition.STRING_FACE));
                if(style.isBold()){
                    styles = (styles | java.awt.Font.BOLD);
                }
                if(style.isItalic()){
                    styles = (styles | java.awt.Font.ITALIC);
                }
            }
            
            double rotationAngle = 0;
            
            if(rotated){
                if (text.hasAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE)) {
                    rotationAngle = createAngle(text.getAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE));
                }
            }
            
            result = createTextLayout(value, x, y, fontName, styles, Float.parseFloat(fontSize), rotationAngle);
        } else {
            result = null;
        }
        return result;
    }
    
    public TextLayout createTextLayout(String value, double x, double y, String fontName, int styles, float fontSize, double rotationAngle) {
        java.awt.Font textFont = new java.awt.Font(fontName, styles, 10);
        textFont = textFont.deriveFont(fontSize);
        
        if (rotationAngle != 0) {
            textFont = textFont.deriveFont(AffineTransform.getRotateInstance(Math.toRadians(rotationAngle)));
        }
        
        return new TextLayout(value, textFont,
                GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)).getFontRenderContext());
    }
    
    public double getLineHeight(ParsedElement text){
        
        //This stores metrics for each text part of the given parsed element.
        List<translator.graphics.Font> partTextFonts = new ArrayList();
        double finalLineHeight = 0;
        if(text.hasAttribute(ParseElementDefinition.TEXT_LINE_HEIGHT)
        && (!text.getAttribute(ParseElementDefinition.TEXT_LINE_HEIGHT).equalsIgnoreCase(ParseElementDefinition.TEXT_LINE_HEIGHT_AUTO))
        && (text.getAttribute(ParseElementDefinition.TEXT_LINE_HEIGHT).length() != 0)){
            finalLineHeight = Double.parseDouble(text.getAttribute(ParseElementDefinition.TEXT_LINE_HEIGHT));
        } else{
            for(ParsedElement parsedElement : text.getElements()){
                if (parsedElement.getName().equals(ParseElementDefinition.STRING)) {
                    //Get all necessary text metrics.
                    partTextFonts.add(translator.graphics.Font.createFromElement(parsedElement, renderContext));
                }
            }
            
            if (partTextFonts.size() > 0){
                for (int i = 0; i <= partTextFonts.size() - 1; i++) {
                    double lineHeight = partTextFonts.get(i).getLineHeightNormal();
                    //When having sub/superscripts, the line height has to increased. In fact is not the same as
                    //if we do not have sub/superscripts present.
                    if (partTextFonts.get(i).getStyle().isSubscript() || partTextFonts.get(i).getStyle().isSuperscript()){
                        //Line height is calculated with Superscript constant even when a Subscript is present.
                        lineHeight += partTextFonts.get(i).getLineHeightSubSuperscript() 
                        - (partTextFonts.get(i).getAscentPixel() * Font.BASE_LINE_SUPERSCRIPT_PERCENT);
                    }
                    if (lineHeight > finalLineHeight){
                        finalLineHeight = lineHeight;
                    }
                }
            }
        }
        return finalLineHeight;
    }
    
    /**
     *Create a area from composite tex configuration
     */
    public Area createAreaFromCompositeText(CompositeTextConfiguration text){
        Area textArea = new Area();
        
        //To translate the text coordinate if the alignment property is center
        double totalOffset = 0;
        
        for(TextConfiguration line : text.getLines()){
            
            //Initialize the coordinate of the new line
            double relativeX = line.getX();
            double relativeY = line.getY();
            
            if(text.getJustification() == TextConfiguration.RIGHT_JUSTIFICATION){
                for(int i = line.getParts().size() - 1; i >= 0; i--){
                    FontRenderContext renderContext = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                            new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB)).getFontRenderContext();
                    
                    String part = line.getParts().get(i);
                    if(!part.equals("")){
                        Font font = line.getFont(Integer.toString(i));
                        Color color = line.getColor(Integer.toString(i));
                        
                        TextLayout lineLayout =
                                new TextLayout(part,
                                createPlatformFontFromFont(font, text.getLineHeight()), renderContext);
                        
                        relativeX -= lineLayout.getAdvance();
                        
                        //In C#, an offset is calculated and applied to relativeY value
                        AffineTransform partTransformation = new AffineTransform();
                        partTransformation.translate(relativeX, relativeY);
                        
                        Area partArea = new Area(lineLayout.getOutline(null));
                        partArea.transform(partTransformation);
                        
                        textArea.add(partArea);
                    }
                }
            } else if(text.getJustification() == TextConfiguration.LEFT_JUSTIFICATION ||
                    text.getJustification() == TextConfiguration.CENTER_JUSTIFICATION){
                double partOffset = 0;
                
                for(int i = 0; i < line.getParts().size(); i++){
                    FontRenderContext renderContext = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                            new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB)).getFontRenderContext();
                    
                    String part = line.getParts().get(i);
                    if(!part.equals("")){
                        Font font = line.getFont(Integer.toString(i));
                        Color color = line.getColor(Integer.toString(i));
                        TextLayout lineLayout =
                                new TextLayout(part,
                                createPlatformFontFromFont(font, text.getLineHeight()), renderContext);
                        
                        //In C#, an offset is calculated and applied to relativeY value
                        AffineTransform partTransformation = new AffineTransform();
                        partTransformation.translate(relativeX, relativeY);
                        
                        Area partArea = new Area(lineLayout.getOutline(null));
                        partArea.transform(partTransformation);
                        
                        textArea.add(partArea);
                        
                        relativeX += lineLayout.getAdvance();
                        partOffset += lineLayout.getAdvance();
                    }
                }
                
                if(partOffset > totalOffset){
                    totalOffset = partOffset;
                }
            }
        }
        
        AffineTransform textTransform = new AffineTransform();
        textTransform.rotate(Math.toRadians(text.getRotationAngle()));
        
        if(text.getJustification() == TextConfiguration.CENTER_JUSTIFICATION){
            //Translate the coordinates to the middle of the longest text line
            textTransform.translate(-totalOffset / 2, 0);
        }
        
        textArea.transform(textTransform);
        
        return textArea;
    }
    
    /**
     * This method gets the "y" value for cases where a bond is attached to a subscript or to a superscript.
     * This method differs from the one in C# code line because we need to create a text layout to get the ascent value in Java.
     * In C# is not neccesary to create that text layout.
     */
    public double getSubOrSuperScriptMiddleY(Font font, double baseLineY, String subpart,  FontRenderContext renderContext) {
        double middleSubSuperscriptY = 0;
        double subSuperscriptBaseLine = 0;
        
        StyleElement style = font.getStyle();
        
        double size = Double.parseDouble(font.getSize());
        
        //We need to create the text layout from a platform font created with the normal size. This is to get the ascent of normal characters.
        java.awt.Font platformFont = new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int)Float.parseFloat(font.getSize()));
        platformFont = platformFont.deriveFont((float)Float.parseFloat(font.getSize()));
        TextLayout partLayout = new TextLayout(subpart, platformFont, renderContext);
        
        //We need to create the text layout from a platform font created with the size reduced. This is to get the ascent of a super/subscript.
        java.awt.Font subSuperscriptPlatformFont = new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int)Float.parseFloat(font.getSize()));
        subSuperscriptPlatformFont = subSuperscriptPlatformFont.deriveFont((float)(Float.parseFloat(font.getSize()) * FONT_SIZE_PERCENT_FOR_SUB_SUPERSCRIPT));
        TextLayout subSuperscriptLayout = new TextLayout(subpart, subSuperscriptPlatformFont, renderContext);
        
        //The ascent value seems bigger than the character height. It seems to include descent value also.
        double realAscent = subSuperscriptLayout.getAscent() - subSuperscriptLayout.getDescent();
        
        //Get the baseline for the sub/superscript.
        if (style.isSubscript()) {
            subSuperscriptBaseLine = baseLineY + (partLayout.getAscent()  * Font.BASE_LINE_SUBSCRIPT_PERCENT);
        } else if(style.isSuperscript()) {
            subSuperscriptBaseLine = baseLineY - (partLayout.getAscent()  * font.BASE_LINE_SUPERSCRIPT_PERCENT);
        }
        
        middleSubSuperscriptY = subSuperscriptBaseLine - realAscent / 2;
        
        return middleSubSuperscriptY;
    }
    
    /*
     * This method gets a platform font from translator engine font.
     */
    public java.awt.Font createPlatformFontFromFont(Font font, double lineHeight){
        java.awt.Font platformFont = new java.awt.Font(font.getName(),
                java.awt.Font.PLAIN,
                (int)Float.parseFloat(font.getSize()));
        
        StyleElement style = font.getStyle();
        int platformStyle = java.awt.Font.PLAIN;
        
        if(style.isBold()){
            platformStyle |= java.awt.Font.BOLD;
        }
        if(style.isItalic()){
            platformStyle |= java.awt.Font.ITALIC;
        }
        
        platformFont.deriveFont(platformStyle, Float.parseFloat(font.getSize()));
        
        AffineTransform transform = new AffineTransform();
        transform.scale(FONT_SIZE_PERCENT_FOR_SUB_SUPERSCRIPT, FONT_SIZE_PERCENT_FOR_SUB_SUPERSCRIPT);
        if(style.isSubscript()){
            //In C#, the following code line is replaced by a calling to GetRelativeYWithOffset method
            transform.translate(0, lineHeight * LINE_HEIGHT_PERCENT_FOR_SUBSCRIPT);
            
            platformFont = platformFont.deriveFont(transform);
        } else if(style.isSuperscript()){
            //In C#, the following code line is replaced by a calling to GetRelativeYWithOffset method
            transform.translate(0, -(lineHeight * LINE_HEIGHT_PERCENT_FOR_SUPERSCRIPT));
            
            platformFont = platformFont.deriveFont(transform);
        }
        
        //This is very necessary  when we have floating font-sizes
        // because when a font is defined there is no overload for float sizes, only integer sizes.
        platformFont = platformFont.deriveFont(Float.parseFloat(font.getSize()));
        
        return platformFont;
    }
    
    public Area createAreaFromText(ParsedElement text, int begin, int end){
        Area result;
        if (text.hasAttribute(ParseElementDefinition.TEXT_POSITION)) {
            Point point = CDXMLProcessor.parseCoords(text.getAttribute(ParseElementDefinition.TEXT_POSITION), text);
            
            double x = point.getX();
            double y = point.getY();
            
            TextLayout textLayout = createTextLayout(text, true);
            
            Shape textShape = textLayout.getOutline(null);
            PathIterator segments = textShape.getPathIterator(null);
            ExtendedGeneralPath newTextShape = new ExtendedGeneralPath(new GeneralPath());
            
            double[] part = new double[6];
            
            while(!segments.isDone()){
                int segmentType = segments.currentSegment(part);
                
                if(segmentType == PathIterator.SEG_MOVETO){
                    newTextShape.moveTo(part[0] + x, part[1] + y);
                } else if(segmentType == PathIterator.SEG_LINETO){
                    newTextShape.lineTo(part[0] + x, part[1] + y);
                } else if(segmentType == PathIterator.SEG_QUADTO){
                    newTextShape.quadTo(part[0] + x, part[1] + y, part[2] + x, part[3] + y);
                } else if(segmentType == PathIterator.SEG_CUBICTO){
                    newTextShape.curveTo(part[0] + x, part[1] + y,
                            part[2] + x, part[3] + y,
                            part[4] + x, part[5] + y);
                } else if(segmentType == PathIterator.SEG_CLOSE){
                    newTextShape.closePath();
                }
                
                segments.next();
            }
            
            result = new Area(newTextShape);
        } else {
            result = null;
        }
        return result;
    }
    
    public Point relocateText(ParsedElement text){
        Point result;
        if (text.hasAttribute(ParseElementDefinition.TEXT_JUSTIFICATION) && text.hasAttribute(ParseElementDefinition.TEXT_POSITION)) {
            String justification = text.getAttribute(ParseElementDefinition.TEXT_JUSTIFICATION);
            
            String value = "";
            List<ParsedElement> subElements = text.getElements(ParseElementDefinition.STRING);
            if(subElements.size() > 0){
                value = subElements.get(0).getValue();
            }
            
            Shape textShape = createAreaFromText(text);
            double[] textXRange = calculateRangeInX(textShape);
            double[] textYRange = calculateRangeInY(textShape);
            
            Shape lastCharacter =
                    createAreaFromText(text, value.length() - 1);
            double[] lastCharacterXRange = calculateRangeInX(lastCharacter);
            
            Shape firstCharacter =
                    createAreaFromText(text, 0, 1);
            double[] firstCharacterXRange = calculateRangeInX(firstCharacter);
            
            String textLocation = text.getAttribute(ParseElementDefinition.TEXT_POSITION);
            
            String[] pointCoords = textLocation.split(" ");
            double x = Double.parseDouble(pointCoords[0]);
            double y = Double.parseDouble(pointCoords[1]);
            
            result = relocateText(justification,
                    x, y, textXRange, lastCharacterXRange, firstCharacterXRange);
        } else {
            result = null;
        }
        return result;
    }
    
    private Point relocateText(String justification,
            double x, double y, double[] shapeXRange, double[] lastCharacterXRange, double[] firstCharacterXRange){
        
        if(justification.equals(RIGHT_ALIGNMENT)){
            x = x - (shapeXRange[1] - shapeXRange[0]);
        }
        
        return new Point(x, y);
    }
    
    public double createAngle(String angle){
        return (int)(Long.parseLong(angle)) / 65536.0;
    }
    
    public static Point fixedScale2D(Point newPoint, Point originalPoint, double distance) {
        double deltaX = newPoint.getX() - originalPoint.getX();
        double deltaY = newPoint.getY() - originalPoint.getY();
        if (deltaX != 0) {
            if (deltaY != 0) {
                double d = GeometricOperations.distance(newPoint, originalPoint);
                if (d == 0) {
                    deltaX = distance;
                } else {
                    deltaX = deltaX * distance / d;
                    deltaY = deltaY * distance / d;
                }
            } else {
                deltaX = (deltaX > 0) ? distance : -distance;
            }
        } else {
            deltaY = (deltaY > 0) ? distance : -distance;
        }
        
        Point result = new Point(originalPoint.getX() + deltaX, originalPoint.getY() + deltaY);
        return result;
    }
    
    public List<SegmentConfiguration> createConfigurationFromArea(GeneralPath path){
        PathIterator areaSegments = path.getPathIterator(null);
        List<SegmentConfiguration> result = new ArrayList();
        List<Point> points = new ArrayList();
        Point beginPoint = null;
        Point lastPoint = null;
        Point currentPoint;
        Point controlPoint1;
        Point controlPoint2;
        
        while(!areaSegments.isDone()){
            double[] currentSegment = new double[6];
            int type = areaSegments.currentSegment(currentSegment);
            
            if(type == PathIterator.SEG_MOVETO){
                lastPoint = new Point(currentSegment[0], currentSegment[1]);
                beginPoint = lastPoint;
            }else if(type == PathIterator.SEG_LINETO){
                currentPoint = new Point(currentSegment[0], currentSegment[1]);
                result.add(new SegmentConfiguration(lastPoint, currentPoint));
                lastPoint = new Point(currentSegment[0], currentSegment[1]);
            }else if(type == PathIterator.SEG_QUADTO){
                currentPoint = new Point(currentSegment[2], currentSegment[3]);
                controlPoint1 = new Point(currentSegment[0], currentSegment[1]);
                result.add(new QuadraticCurveConfiguration(lastPoint, currentPoint, controlPoint1));
                lastPoint = new Point(currentSegment[2], currentSegment[3]);
            }else if(type == PathIterator.SEG_CUBICTO){
                currentPoint = new Point(currentSegment[4], currentSegment[5]);
                controlPoint1 = new Point(currentSegment[0], currentSegment[1]);
                controlPoint2 = new Point(currentSegment[2], currentSegment[3]);
                result.add(new CubicCurveConfiguration(lastPoint, currentPoint, controlPoint1, controlPoint2));
                lastPoint = new Point(currentSegment[4], currentSegment[5]);
            }else if(type == PathIterator.SEG_CLOSE){
                result.add(new SegmentConfiguration(lastPoint, beginPoint));
            }
            areaSegments.next();
        }
        return result;
    }
    
    public List<SegmentConfiguration> createConfigurationFromArea(Area area){
        PathIterator areaSegments = area.getPathIterator(null);
        List<SegmentConfiguration> result = new ArrayList();
        List<Point> points = new ArrayList();
        Point beginPoint = null;
        Point lastPoint = null;
        Point currentPoint;
        Point controlPoint1;
        Point controlPoint2;
        
        while(!areaSegments.isDone()){
            double[] currentSegment = new double[6];
            int type = areaSegments.currentSegment(currentSegment);
            
            if(type == PathIterator.SEG_MOVETO){
                if(lastPoint == null){
                    lastPoint = new Point(currentSegment[0], currentSegment[1]);
                    beginPoint = lastPoint;
                } else {
                    currentPoint = new Point(currentSegment[0], currentSegment[1]);
                    result.add(new SegmentConfiguration(lastPoint, currentPoint, true));
                    lastPoint = new Point(currentSegment[0], currentSegment[1]);
                }
            }else if(type == PathIterator.SEG_LINETO){
                currentPoint = new Point(currentSegment[0], currentSegment[1]);
                result.add(new SegmentConfiguration(lastPoint, currentPoint));
                lastPoint = new Point(currentSegment[0], currentSegment[1]);
            }else if(type == PathIterator.SEG_QUADTO){
                currentPoint = new Point(currentSegment[2], currentSegment[3]);
                controlPoint1 = new Point(currentSegment[0], currentSegment[1]);
                result.add(new QuadraticCurveConfiguration(lastPoint, currentPoint, controlPoint1));
                lastPoint = new Point(currentSegment[2], currentSegment[3]);
            }else if(type == PathIterator.SEG_CUBICTO){
                currentPoint = new Point(currentSegment[4], currentSegment[5]);
                controlPoint1 = new Point(currentSegment[0], currentSegment[1]);
                controlPoint2 = new Point(currentSegment[2], currentSegment[3]);
                result.add(new CubicCurveConfiguration(lastPoint, currentPoint, controlPoint1, controlPoint2));
                lastPoint = new Point(currentSegment[4], currentSegment[5]);
            }else if(type == PathIterator.SEG_CLOSE){
                result.add(new SegmentConfiguration(lastPoint, beginPoint));
            }
            areaSegments.next();
        }
        return result;
    }
    
    public List<SegmentConfiguration> getWavySegments(Point beginPoint, Point endPoint, double lineWidth) {
        List<SegmentConfiguration> arcs = new ArrayList();
        
        double x1 = beginPoint.getX();
        double y1 = beginPoint.getY();
        double x2 = endPoint.getX();
        double y2 = endPoint.getY();
        
        double horizontalLength, verticalLength, horizontalDelta, verticalDelta;
        Point delta = lineDelta(beginPoint, endPoint, lineWidth);
        if (delta.getY() == 0) {
            horizontalDelta = 0;
            if ((delta.getX() < 0) != (y2 < y1)) {
                verticalDelta = -delta.getX();
            } else {
                verticalDelta = delta.getX();
            }
        } else if ((delta.getY() < 0) != (x2 < x1)) {
            horizontalDelta = -delta.getY();
            verticalDelta = delta.getX();
        } else {
            horizontalDelta = delta.getY();
            verticalDelta = -delta.getX();
        }
        
        double bondLength = GeometricOperations.distance(x1, y1, x2, y2);
        double halfWigSize = Math.sqrt(horizontalDelta * horizontalDelta + verticalDelta * verticalDelta);
        // multiply by 10 so rounding and module work with more digits
        if ((int)(2 * halfWigSize * 100 + 0.5) > 0 && (int)(bondLength * 100 + 0.5) % (int)(2 * halfWigSize * 100 + 0.5) != 0) {
            double newHalfWigSize = bondLength / ((bondLength + halfWigSize) / (2 * halfWigSize)) / 2;
            horizontalDelta = horizontalDelta * newHalfWigSize/halfWigSize;
            verticalDelta = verticalDelta * newHalfWigSize/halfWigSize;
            halfWigSize = newHalfWigSize;
        }
        
        horizontalLength = x1;
        verticalLength = y1;
        Point currentDelta = delta;
        
        // Taken from ChemDraw C++
        Point edgeHandleDelta = new Point(horizontalDelta * 0.55197, verticalDelta * 0.55197);
        
        double angle = GeometricOperations.angle(x1, y1, x2, y2);
        angle = Math.toDegrees(angle); // Convert to degrees
        
        Point previousPoint = null;
        boolean sweepPositive = true;
        
        do {
            ArcConfiguration arc1 = null;
            ArcConfiguration arc2 = null;
            
            Point centerPoint = new Point(horizontalLength, verticalLength);
            
            if (previousPoint != null) {
                arc1 = new ArcConfiguration(previousPoint, centerPoint,
                        halfWigSize, halfWigSize,
                        angle, false, sweepPositive);
                arc1.setStrokeWidth(getLineWidth());
                arcs.add(arc1);
            }
            
            sweepPositive = !sweepPositive;
            
            horizontalLength += horizontalDelta;
            verticalLength += verticalDelta;
            if (!((Math.abs(verticalDelta) > Math.abs(horizontalDelta)) ? ((verticalDelta < 0) ? (verticalLength >= y2) : (verticalLength <= y2)) : ((horizontalDelta < 0) ? (horizontalLength >= x2) : (horizontalLength <= x2))))
                break;
            Point edgePoint = new Point(horizontalLength + currentDelta.getX(), verticalLength + currentDelta.getY());
            
            arc2 = new ArcConfiguration(centerPoint,edgePoint, halfWigSize, halfWigSize,
                    angle, false, sweepPositive);
            arc2.setStrokeWidth(getLineWidth());
            arcs.add(arc2);
            
            previousPoint = edgePoint;
            
            horizontalLength += horizontalDelta;
            verticalLength += verticalDelta;
            currentDelta = currentDelta.byScalar(-1);
        } while ((Math.abs(verticalDelta) > Math.abs(horizontalDelta)) ? ((verticalDelta < 0) ? (verticalLength >= y2) : (verticalLength <= y2)) : ((horizontalDelta < 0) ? (horizontalLength >= x2) : (horizontalLength <= x2)));
        
        return arcs;
    }
    
    // used by wavy bond processor and wavy line processor
    private Point lineDelta(Point beginPoint, Point endPoint, double lineWidth) {
        double x1 = beginPoint.getX();
        double y1 = beginPoint.getY();
        double x2 = endPoint.getX();
        double y2 = endPoint.getY();
        Point delta = new Point(y1 - y2, x2 - x1);
        
        if (delta.getX() == 0 && delta.getY() == 0) {
            delta.setX(50);
        }
        
        if (lineWidth != 0) {
            Point offsetBeginPoint = fixedScale2D(beginPoint.add(delta), beginPoint, lineWidth / 2);
            
            double deltaX = offsetBeginPoint.getX() - x1;
            if (deltaX > 0 && deltaX < CDXMLEnvironment.COMPARISON_RECISION) { // Consider almost equal values as equals
                deltaX = 0;
            }
            double deltaY = offsetBeginPoint.getY() - y1;
            if (deltaY > 0 && deltaY < CDXMLEnvironment.COMPARISON_RECISION) { // Consider almost equal values as equals
                deltaY = 0;
            }
            
            delta.setX(deltaX);
            delta.setY(deltaY);
        } else {
            delta.setX((delta.getX() / CDXMLEnvironment.CD_KBONDSCALE) / 2);
            delta.setY((delta.getY() / CDXMLEnvironment.CD_KBONDSCALE) / 2);
        }
        
        return delta;
    }
    
    public double getLabelFontSize() {
        return labelFontSize;
    }
    
    public void setLabelFontSize(double labelFontSize) {
        this.labelFontSize = labelFontSize;
    }
    
    public String getLabelFont() {
        return labelFont;
    }
    
    public void setLabelFont(String labelFont) {
        this.labelFont = labelFont;
    }
    
    public String getLabelFace() {
        return labelFace;
    }
    
    public void setLabelFace(String labelFace) {
        this.labelFace = labelFace;
    }
    
    private class NodeZOrderComparator implements Comparator {
        private int getZOrder(Object node) {
            ParsedElement nodeElement = (ParsedElement) node;
            return nodeElement.getZOrder();
        }
        public int compare(Object node1, Object node2) {
            return getZOrder(node1) - getZOrder(node2);
        }
    }
    
    public ParsedElement getForegroundColor() {
        return foregroundColor;
    }
    
    public void setForegroundColor(ParsedElement foregroundColor) {
        this.foregroundColor = foregroundColor;
    }
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public double getCaptionFontSize() {
        return captionFontSize;
    }
    
    public void setCaptionFontSize(double captionFontSize) {
        this.captionFontSize = captionFontSize;
    }
    
    public String getCaptionFont() {
        return captionFont;
    }
    
    public void setCaptionFont(String captionFont) {
        this.captionFont = captionFont;
    }
    
    public double getBondLength() {
        return bondLength;
    }
    
    public void setBondLength(double bondLength) {
        this.bondLength = bondLength;
    }
    
    public Hashtable<String, CompositeTextConfiguration> getCompositeTextConfigurations() {
        return compositeTextConfigurations;
    }
    
    public ParsedElement getNodeById(String id){
        return nodeById.get(id);
    }
    
    public void addNodeById(String id, ParsedElement node){
        nodeById.put(id, node);
    }
}
