
package translator.cdxml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import translator.ElementNotSupportedException;
import translator.ParseElementDefinition;
import translator.ParsedBoundObject;
import translator.ParsedElement;
import translator.ParsedObject;
import translator.ParsedPage;
import translator.ParsedStructure;
import translator.Parser;
import translator.ParserException;
import translator.cddom.properties.BoundingBox;
import translator.cddom.properties.DocumentProperties;
import translator.cdxml.dom.Altgroup;
import translator.cdxml.dom.Arrow;
import translator.cdxml.dom.B;
import translator.cdxml.dom.Bioshape;
import translator.cdxml.dom.Border;
import translator.cdxml.dom.CDXML;
import translator.cdxml.dom.Color;
import translator.cdxml.dom.Colortable;
import translator.cdxml.dom.Constraint;
import translator.cdxml.dom.Curve;
import translator.cdxml.dom.Embeddedobject;
import translator.cdxml.dom.Font;
import translator.cdxml.dom.Fonttable;
import translator.cdxml.dom.Fragment;
import translator.cdxml.dom.Geometry;
import translator.cdxml.dom.Graphic;
import translator.cdxml.dom.Group;
import translator.cdxml.dom.N;
import translator.cdxml.dom.Objecttag;
import translator.cdxml.dom.Page;
import translator.cdxml.dom.Plasmidmap;
import translator.cdxml.dom.Plasmidmarker;
import translator.cdxml.dom.Plasmidregion;
import translator.cdxml.dom.Rlogic;
import translator.cdxml.dom.S;
import translator.cdxml.dom.Sgcomponent;
import translator.cdxml.dom.Sgdatum;
import translator.cdxml.dom.Spectrum;
import translator.cdxml.dom.Stoichiometrygrid;
import translator.cdxml.dom.T;
import translator.cdxml.dom.Table;
import translator.cdxml.dom.Tlclane;
import translator.cdxml.dom.Tlcplate;
import translator.cdxml.dom.Tlcspot;
import translator.processors.ProcessorFactory;
import translator.processors.cdxml.CDXMLProcessor;
import translator.processors.cdxml.CDXMLProcessorFactory;
import translator.processors.cdxml.QuerySymbolsProcessor;
import translator.processors.cdxml.TextProcessor;
import translator.sources.Source;
import translator.sources.impl.FileImpl;
import translator.sources.impl.StreamImpl;
import translator.utils.Point;
import translator.utils.StringUtils;

public class CDXMLParser implements Parser<Source> {
    
    private static final String DTD_URL = "http://cdxml.azurewebsites.net/CDXML.DTD";
    
    private static final String BACKGROUND_COLOR = "2";
    private static final String FOREGROUND_COLOR = "3";
    
    private static final String RGBA_BACKGROUND_COLOR = "0xffffffff";
    private static final String RGBA_FOREGROUND_COLOR = "0xff000000";
    
    private static final String DEFAULT_ARROWHEAD_TYPE = "Solid";
    
    // taken from ChemDraw C++
    // proportion to calculate head width from the head size
    private static final double ARROW_HEAD_WIDTH_PROPORTION = 0.25;
    // proportion to calculate head center size from the head size
    private static final double ARROW_HEAD_CENTER_SIZE_PROPORTION = 0.875;
    
    // default value for arrows that come in old graphic tags
    private static final String DEFAULT_GRAPHIC_ARROW_SHAFT_SPACING = "300";
    
    private static final String DEFAULT_LINE_WIDTH = "1.0";
    private static final String DEFAULT_BOLD_WIDTH = "4.0";
    private static final String DEFAULT_BOND_SPACING = "12.0";
    private static final String DEFAULT_HASH_SPACING = "2.7";
    private static final String DEFAULT_MARGIN_WIDTH = "2.0";
    private static final String DEFAULT_LABEL_SIZE = "10";
    private static final String DEFAULT_FONT_NAME = "Arial";
    private static final String DEFAULT_STRING_CHAR_SET = "iso-8859-1";
    private static final String DEFAULT_TEXT_VISIBLE = "true";
    private static final String HEXADECIMAL_PREFIX = "0x";
    
    private static final double DEFAULT_BOTTOM = 540.0;
    private static final double DEFAULT_RIGHT = 720.0;
    
    
    //constants to acces the bounding box points
    private static final int BOUNDINGBOX_LEFT = 0;
    private static final int BOUNDINGBOX_TOP = 1;
    private static final int BOUNDINGBOX_RIGHT = 2;
    private static final int BOUNDINGBOX_BOTTOM = 3;
    
    private static final String NOT_VISIBLE = "no";
    
    private static final int SGDATUM_NUMBER_OF_DIGITS = 3;
    
    private ProcessorFactory processorFactory;
    
    private ParsedStructure resultingStructure;
    private Source parseableSource;
    
    private int lastFontId = 0;
    
    //Laben font index to the table in the CDXML file
    private String labelFontIndex;
    
    //Caption font index to the table in the CDXML file
    private String captionFontIndex;
    
    private Hashtable<String, Font> fonts = new Hashtable();
    private Hashtable<String, Color> colors = new Hashtable();
    
    private Hashtable<String, Graphic> graphics = new Hashtable();
    
    private ParsedElement documentElement;
    private DocumentProperties documentProperties;
    
    private CDXMLEnvironment environment;
    
    private static JAXBContext context;
    
    private static XMLReader internalXMLReader;
    
    private String xOffset = "0";
    private String yOffset = "0";
    private int zOrder = 0;
    
    private int pageNumber = 1;
    
    private int objectTagID = 0;
    
    static {
        try {
            context = JAXBContext.newInstance("translator.cdxml.dom");
            
            // Create custom parser and set entity resolver to ignore DTD tag.
            // This prevents the parsing from retrieving the DTD from the net.
            SAXParserFactory internalParserFactory = SAXParserFactory.newInstance();
            SAXParser internalParser = internalParserFactory.newSAXParser();
            internalXMLReader = internalParser.getXMLReader();
            internalXMLReader.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException,IOException {
                    if (systemId.equals(DTD_URL)) {
                        return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                    } else {
                        return null;
                    }
                }
            });
        } catch (JAXBException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }
    
    public CDXMLParser() {
        processorFactory = new CDXMLProcessorFactory();
    }
    
    public void setSource(Source source) {
        this.parseableSource = source;
    }
    
    public ParsedStructure getParsedStructure() throws ParserException {
        return resultingStructure;
    }
    
    public void parse() throws ParserException {
        try {
            if (context == null) {
                throw new ParserException("Context was not correctly initialized.");
            }
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            CDXML rootNode = null;
            if(parseableSource instanceof FileImpl){
                SAXSource internalSource = new SAXSource(internalXMLReader, new InputSource(new FileReader((File) parseableSource)));
                //handle invalid accessExternalDTD
                //rootNode = (CDXML) unmarshaller.unmarshal(new FileInputStream((File)parseableSource));
                rootNode = (CDXML) unmarshaller.unmarshal(internalSource);
                //handle name is null
                //rootNode.setName(((FileImpl) parseableSource).getName());
            } else if(parseableSource instanceof StreamImpl){
            	SAXSource internalSource = new SAXSource(internalXMLReader, new InputSource(new InputStreamReader(((StreamImpl) parseableSource).getInputStream())));
            	//internalSource.getInputSource().setEncoding("UTF-8");
            	rootNode = (CDXML) unmarshaller.unmarshal(internalSource);
            	//rootNode = (CDXML) unmarshaller.unmarshal(((StreamImpl) parseableSource).getInputStream());
            } 
            
            resultingStructure = new ParsedStructure();
            
            buildStructure(rootNode);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new ParserException("Unsupported file format.");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            throw new ParserException("File not found.");
        }
    }
    
    private void buildStructure(CDXML rootNode) {
        documentProperties = new DocumentProperties();
        documentElement = new ParsedElement();
        
        if(rootNode.getBoundingBox() != null){
            
            String[] boundingBoxData =  rootNode.getBoundingBox().split(" ");
            
            Point position = new Point(Double.parseDouble(boundingBoxData[0]), Double.parseDouble(boundingBoxData[1]));
            
            double width = Double.parseDouble(boundingBoxData[2]) - position.getX();
            double height = Double.parseDouble(boundingBoxData[3])  - position.getY();
            
            BoundingBox boundingBox = new BoundingBox(position, width, height);
            documentProperties.setBoundingBox(boundingBox);
            
            documentElement.addAttribute(ParseElementDefinition.BOUNDING_BOX, rootNode.getBoundingBox());
            
        }
        environment = (CDXMLEnvironment)CDXMLEnvironment.getInstance();
        environment.cleanUpEnvironment();
        
        createFontTable(rootNode);
        createColorTable(rootNode);
        
        documentProperties.setBackgroundColor(CDXMLProcessor.convertColor(environment.getBackgroundColor()));
        
        if(rootNode.getLineWidth()!=null){
            environment.setLineWidth(Double.parseDouble(rootNode.getLineWidth()));
        }else{
            environment.setLineWidth(Double.parseDouble(DEFAULT_LINE_WIDTH));
        }
        if(rootNode.getBoldWidth()!=null){
            environment.setBoldWidth(Double.parseDouble(rootNode.getBoldWidth()));
        }else{
            environment.setBoldWidth(Double.parseDouble(DEFAULT_BOLD_WIDTH));
        }
        if(rootNode.getBondSpacing()!=null){
            environment.setBondSpacing(Double.parseDouble(rootNode.getBondSpacing()));
        }else{
            environment.setBondSpacing(Double.parseDouble(DEFAULT_BOND_SPACING));
        }
        
        environment.setBondLength(Double.parseDouble(rootNode.getBondLength()));
        if(rootNode.getHashSpacing()!=null){
            environment.setHashSpacing(Double.parseDouble(rootNode.getHashSpacing()));
        }else{
            environment.setHashSpacing(Double.parseDouble(DEFAULT_HASH_SPACING));
        }
        if(rootNode.getMarginWidth()!=null){
            environment.setMarginWidth(Double.parseDouble(rootNode.getMarginWidth()));
        }else{
            environment.setMarginWidth(Double.parseDouble(DEFAULT_MARGIN_WIDTH));
        }
        
        if(rootNode.getLabelSize()!=null){
            environment.setLabelFontSize(Double.parseDouble(rootNode.getLabelSize()));
        }else{
            environment.setLabelFontSize(Double.parseDouble(DEFAULT_LABEL_SIZE));
        }
        if(rootNode.getCaptionSize()!=null){
            environment.setCaptionFontSize(Double.parseDouble(rootNode.getCaptionSize()));
        }
        captionFontIndex = rootNode.getCaptionFont();
        if(captionFontIndex!=null){
            environment.setCaptionFont(fonts.get(captionFontIndex).getName());
        }
        labelFontIndex = rootNode.getLabelFont();
        if(labelFontIndex!=null){
            environment.setLabelFont(fonts.get(labelFontIndex).getName());
        }else{
            environment.setLabelFont(DEFAULT_FONT_NAME);
        }
        environment.setLabelFace(rootNode.getLabelFace());
        environment.setFileName(rootNode.getName());
        
        
        for(Page currentPage : rootNode.getPage()){
            resultingStructure.addElement(parsePage(currentPage));
        }
        
        environment.calculateBondMitering();
    }
    
    
    private List<ParsedElement> parseNode(N node, ParsedPage currentPage, String fragmentId){
        List<ParsedElement> result = new ArrayList();
        ParsedElement newAttachedTextElement = null;
        
        ParsedElement nodeElement = new ParsedElement();
        nodeElement.setEnvironment(environment);
        nodeElement.setName(ParseElementDefinition.NODE);
        nodeElement.setId(node.getId());
        nodeElement.addAttribute(ParseElementDefinition.NODE_POSITION, node.getP());
        if(node.getZ() != null){
            nodeElement.setZOrder(Integer.parseInt(node.getZ()));
        }

        for(Object possibleText : node.getObjecttagOrAnnotationOrT()){
            if(possibleText instanceof T){
                
                T attachedText = (T) possibleText;
                newAttachedTextElement = parseText(attachedText, node.getZ(), node).get(0);
                result.add(newAttachedTextElement);
                //We must to set the page properties to the element because we need
                //use the element to store the node with all the information
                //in the environment
                newAttachedTextElement = setPageProperties(newAttachedTextElement);
            } else if(possibleText instanceof Objecttag){
                //the owner doesn't has any valid information for the
                //parseObjectTag method so we send a null parameter                
                result.addAll(parseObjectTag((Objecttag) possibleText, nodeElement));
            }
        }
        
        if(newAttachedTextElement != null){
            environment.addN(setPageOffset(node), newAttachedTextElement);
        } else{
            environment.addN(setPageOffset(node));
        }
        
        if (node.getHDot() != null && node.getHDot().equalsIgnoreCase(ParseElementDefinition.NODE_H_DOT_YES) ||
                node.getHDash() != null && node.getHDash().equalsIgnoreCase(ParseElementDefinition.NODE_H_DASH_YES) ||
                node.getNodeType() != null && (node.getNodeType().equalsIgnoreCase(ParseElementDefinition.NODE_TYPE_EXTERNAL_CONNECTION_POINT) ||
                node.getNodeType().equalsIgnoreCase(ParseElementDefinition.NODE_TYPE_VARIABLE_ATTACHMENT) ||
                node.getNodeType().equalsIgnoreCase(ParseElementDefinition.NODE_TYPE_MULTI_ATTACHMENT))) {
            
            if (node.getHDot() != null) {
                nodeElement.addAttribute(ParseElementDefinition.NODE_H_DOT, node.getHDot());
            }
            if (node.getHDash() != null) {
                nodeElement.addAttribute(ParseElementDefinition.NODE_H_DASH, node.getHDash());
            }
            if (node.getNodeType() != null) {
                nodeElement.addAttribute(ParseElementDefinition.NODE_TYPE, node.getNodeType());
            }
            if(node.getLineWidth() != null){
                nodeElement.addAttribute(ParseElementDefinition.LINE_WIDTH, node.getLineWidth());
            }
            
            nodeElement.addElement(parseColor(node.getFgRGBA(), node.getColor()));
            
            if (node.getNodeType() != null && node.getNodeType().equals(ParseElementDefinition.NODE_TYPE_EXTERNAL_CONNECTION_POINT)) {
                String externalConnectionType = node.getExternalConnectionType();
                if (externalConnectionType == null ||
                        externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_UNSPECIFIED)) {
                    // Make Diamond the default external connection type
                    externalConnectionType = ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_DIAMOND;
                }
                
                nodeElement.addAttribute(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE, externalConnectionType);
                
                // add attachment points of any type to the environment
                // so rank indicators are correctly numbered
                environment.addAttachmentPoint(fragmentId, nodeElement);
                
                if (externalConnectionType != null &&
                        externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_DIAMOND) ||
                        externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_STAR)) {
                    Font labelFont = fonts.get(labelFontIndex);
                    nodeElement.addAttribute(ParseElementDefinition.STRING_CHAR_SET, labelFont.getCharset());
                    
                    if (externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_DIAMOND)) {
                        nodeElement.addAttribute(ParseElementDefinition.STRING_FONT, labelFont.getName());
                    }
                }
                
                if (externalConnectionType != null &&
                        externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_STAR) ||
                        externalConnectionType.equalsIgnoreCase(ParseElementDefinition.NODE_EXTERNAL_CONNECTION_TYPE_WAVY)) {
                    environment.addFloatingAttachmentPoint(node.getId());
                }
            }
            result.add(nodeElement);
        }
        return result;
    }
    
    /** This method is for obtaining a ParseElement object providing the hexRGBAColor
     * and the string index of the color inside the color table. Both parameters have
     * to be obtained from the corresponding color node.*/
    private ParsedElement parseColor(String hexRGBAColor, String color){
        ParsedElement result = null;
        if(hexRGBAColor != null){
            result = parseHexadecimalRgbColor(hexRGBAColor);
        }else{
            
            Color colorNode = null;
            if(color!= null){
                colorNode = colors.get(color);
            }
            result = parseColorFromTable(colorNode);
        }
        return result;
    }
    
    /** This method is for obtaining a ParseElement object from rgb color in string hexadecimal
     * format.
     */
    private ParsedElement parseHexadecimalRgbColor(String hexRGBColor) {
        ParsedElement newColorElement = new ParsedElement();
        newColorElement.setId(ParseElementDefinition.COLOR);
        newColorElement.setName(ParseElementDefinition.COLOR);
        
        String red = String.valueOf(Integer.decode(HEXADECIMAL_PREFIX+hexRGBColor.substring(8, 10)).intValue());
        String green = String.valueOf(Integer.decode(HEXADECIMAL_PREFIX+hexRGBColor.substring(6, 8)).intValue());
        String blue = String.valueOf(Integer.decode(HEXADECIMAL_PREFIX+hexRGBColor.substring(4, 6)).intValue());
        String alpha = String.valueOf(Integer.decode(HEXADECIMAL_PREFIX+hexRGBColor.substring(2, 4)).intValue());
        
        newColorElement.addAttribute(ParseElementDefinition.COLOR_RED, red);
        newColorElement.addAttribute(ParseElementDefinition.COLOR_GREEN, green);
        newColorElement.addAttribute(ParseElementDefinition.COLOR_BLUE, blue);
        newColorElement.addAttribute(ParseElementDefinition.COLOR_ALPHA, alpha);
        
        return newColorElement;
    }
    
    
    private ParsedElement parseColorFromTable(Color colorNode){
        ParsedElement newColorElement = new ParsedElement();
        newColorElement.setId(ParseElementDefinition.COLOR);
        newColorElement.setName(ParseElementDefinition.COLOR);
        
        if(colorNode == null){
            newColorElement = environment.getForegroundColor();
        } else {
            newColorElement.addAttribute(ParseElementDefinition.COLOR_RED, String.valueOf((int)(Double.parseDouble(colorNode.getR()) * 255)));
            newColorElement.addAttribute(ParseElementDefinition.COLOR_GREEN, String.valueOf((int)(Double.parseDouble(colorNode.getG()) * 255)));
            newColorElement.addAttribute(ParseElementDefinition.COLOR_BLUE, String.valueOf((int)(Double.parseDouble(colorNode.getB()) * 255)));
            newColorElement.addAttribute(ParseElementDefinition.COLOR_ALPHA, Integer.toString(1) );
        }
        
        return newColorElement;
    }
    
    private void parseFragment(Fragment fragmentNode, ParsedPage currentPage) {
        List<ParsedElement> parsedNodes = new ArrayList();
        for(Object innerNode : fragmentNode.getNOrBOrT()){
            if(innerNode instanceof N){
                List<ParsedElement> nodeElement = parseNode((N) innerNode, currentPage, fragmentNode.getId());
                
                for (ParsedElement elem : nodeElement) {
                    if(elem.getName().equalsIgnoreCase(ParseElementDefinition.TEXT)){
                        currentPage.insertElement(setPageProperties(elem));
                    }else{
                        currentPage.addElement(setPageProperties(elem));
                    }
                }
            } else if(innerNode instanceof B){
                B bond = (B) innerNode;
                
                ParsedElement bondParsed = parseBond(bond, currentPage.getZOrder());
                currentPage.addElement(setPageProperties(bondParsed));
                
                for(Object object : bond.getObjecttagOrAnnotation()){
                    if(object instanceof Objecttag){
                        Objecttag objectTagNode = (Objecttag) object;
                        
                        List<ParsedElement> objectTagElements = parseObjectTag(objectTagNode, bondParsed);
                        for(ParsedElement objectTagElement : objectTagElements) {
                            currentPage.insertElement(setPageProperties(objectTagElement));
                        }
                    }
                }
            } else if (innerNode instanceof Curve) {
                currentPage.addElement(
                        setPageProperties(parseCurve((Curve) innerNode)));
            } else if (innerNode instanceof Graphic) {  // parse graphic tags inside fragments (e.g. symbols)
                try {
                    currentPage.addElement(
                            setPageProperties(parseGraphic((Graphic) innerNode, currentPage)));
                } catch (ElementNotSupportedException ex) {
                    // ignored not supported elements
                }
            } else if(innerNode instanceof Objecttag){
                //the owner doesn't has any valid information for the
                //parseObjectTag method so we send a null parameter
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) innerNode, null);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    currentPage.addElement(setPageProperties(objectTagElement));
                }
            } else if(innerNode instanceof  T){
                for(ParsedElement textElement : parseText((T)innerNode, null)){
                    currentPage.insertElement(setPageProperties(textElement));
                }
            }
        }
        
        // make sure all parsed nodes are a higher Z order than all the bonds that are attached to them
        // this will make attachment points be rendered over all the bonds
        for (ParsedElement parsedNode : parsedNodes) {
            List<ParsedElement> bonds = environment.getJoinedBonds(parsedNode.getId());
            
            int nodeZOrder = parsedNode.getZOrder();
            int highestZOrder = nodeZOrder;
            
            for (ParsedElement bond : bonds) {
                int bondZOrder = bond.getZOrder();
                
                if (bondZOrder > highestZOrder) {
                    highestZOrder = bondZOrder;
                }
            }
            
            int incrementedHighestZOrder = highestZOrder + 1;
            if (highestZOrder > nodeZOrder) {
                parsedNode.setZOrder(incrementedHighestZOrder);
            }
        }
    }
    
    private void parseGroup(Group groupNode, ParsedPage currentPage) {
        for (Object innerNode : groupNode.getTOrFragmentOrGroup()) {
            if (innerNode instanceof Fragment) {
                parseFragment((Fragment) innerNode, currentPage);
            } else if (innerNode instanceof Group) {
                parseGroup((Group) innerNode, currentPage);
            } else if (innerNode instanceof Curve) {
                currentPage.addElement(
                        setPageProperties(parseCurve((Curve) innerNode)));
            } else if (innerNode instanceof Graphic) {
                try {
                    Graphic graphicNode = (Graphic) innerNode;
                    
                    if(graphicNode.getSupersededBy() == null){
                        currentPage.addElement(
                                setPageProperties(parseGraphic(graphicNode, currentPage)));
                    }
                } catch (ElementNotSupportedException ex) {
                    // ignore not supported elements
                }
            } else if (innerNode instanceof Arrow) {
                currentPage.addElement(
                        setPageProperties(parseArrow((Arrow) innerNode)));
            } else if(innerNode instanceof T){
                T textNode = (T) innerNode;
                
                for(ParsedElement textElement : parseText(textNode, null)){
                    currentPage.insertElement(setPageProperties(textElement));
                }
            } else if(innerNode instanceof Objecttag){
                //the owner doesn't has any valid information for the
                //parseObjectTag method so we send a null parameter
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) innerNode, null);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    currentPage.addElement(setPageProperties(objectTagElement));
                }                
            } else if(innerNode instanceof Altgroup){
                List<ParsedElement> altGroupElements =
                        parseAlternativeGroup((Altgroup) innerNode, currentPage);
                
                for(ParsedElement altGroupElement : altGroupElements){
                    currentPage.addElement(
                        setPageProperties(altGroupElement));
                }
            } else if (innerNode instanceof Spectrum) {
                List<ParsedElement> spectrumElements =
                        parseSpectrum((Spectrum) innerNode);
                
                for(ParsedElement spectrumElement : spectrumElements){
                    currentPage.addElement(
                        setPageProperties(spectrumElement));
                }
            } else if (innerNode instanceof Plasmidmap) {
                List<ParsedElement> plasmidMapElements =
                        parsePlasmidmap((Plasmidmap) innerNode, currentPage);
                
                for(ParsedElement plasmidMapElement : plasmidMapElements){
                    currentPage.addElement(
                        setPageProperties(plasmidMapElement));
                }
            } else if (innerNode instanceof Rlogic) {
                currentPage.addElement(
                        setPageProperties(parseRLogic((Rlogic) innerNode)));
            } else if (innerNode instanceof Bioshape) {
                currentPage.addElement(
                        setPageProperties(parseBioShape((Bioshape) innerNode)));
            }
        }
    }
    
    private void createFontTable(CDXML rootNode){
        Fonttable fontTableNode = rootNode.getFonttable();
        if(fontTableNode != null){
            for(Font currentFontNode : fontTableNode.getFont()){
                fonts.put(currentFontNode.getId(), currentFontNode);
            }
        }
    }
    
    private void createColorTable(CDXML rootNode){
        Colortable colorTableNode = rootNode.getColortable();
        
        if(rootNode.getColortable() != null){
            int k = 2;
            for(Color currentColorNode : colorTableNode.getColor()){
                colors.put(Integer.toString(k++), currentColorNode);
            }
            
            //Initialization of the default background color.
            environment.setBackgroundColor(parseColorFromTable(colors.get(BACKGROUND_COLOR)));
            //Initialization of the default foreground color.
            environment.setForegroundColor(parseColorFromTable(colors.get(FOREGROUND_COLOR)));
        }else{
            //Initialization of the default background color.
            environment.setBackgroundColor(parseHexadecimalRgbColor(RGBA_BACKGROUND_COLOR));
            //Initialization of the default foreground color.
            environment.setForegroundColor(parseHexadecimalRgbColor(RGBA_FOREGROUND_COLOR));
        }
    }
    
    private ParsedElement parseTLCPlate(Tlcplate tlcPlateNode, ParsedPage currentPage){
        ParsedElement newTlcElement = new ParsedElement();
        
        newTlcElement.setId(tlcPlateNode.getId());
        newTlcElement.setEnvironment(environment);
        newTlcElement.setName(ParseElementDefinition.TLC_PLATE);
        newTlcElement.addAttribute(ParseElementDefinition.TLC_BOTTOM_LEFT, tlcPlateNode.getBottomLeft());
        newTlcElement.addAttribute(ParseElementDefinition.TLC_BOTTOM_RIGHT, tlcPlateNode.getBottomRight());
        newTlcElement.addAttribute(ParseElementDefinition.TLC_TOP_RIGHT, tlcPlateNode.getTopRight());
        newTlcElement.addAttribute(ParseElementDefinition.TLC_TOP_LEFT, tlcPlateNode.getTopLeft());
        newTlcElement.addAttribute(ParseElementDefinition.TLC_ORIGIN_FRACTION, tlcPlateNode.getOriginFraction());
        newTlcElement.addAttribute(ParseElementDefinition.TLC_SOLVENT_FRACTION, tlcPlateNode.getSolventFrontFraction());
        if(tlcPlateNode.getColor() != null){
            newTlcElement.addAttribute(ParseElementDefinition.TLC_COLOR, tlcPlateNode.getColor());
        }
        
        newTlcElement.setZOrder(Integer.parseInt(tlcPlateNode.getZ()));
        
        newTlcElement.addAttribute(ParseElementDefinition.TLC_BOUNDING_BOX, tlcPlateNode.getBoundingBox());
        
        newTlcElement.addElement(parseColor(tlcPlateNode.getFgRGBA(), tlcPlateNode.getColor()));
        
        if(tlcPlateNode.getShowBorders() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_SHOW_BORDERS, tlcPlateNode.getShowBorders());
        }
        if(tlcPlateNode.getShowOrigin() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_SHOW_ORIGIN, tlcPlateNode.getShowOrigin());
        }
        if(tlcPlateNode.getShowSideTicks() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_SHOW_SIDE_TICKS, tlcPlateNode.getShowSideTicks());
        }
        if(tlcPlateNode.getShowSolventFront() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_SHOW_SOLVENT_FRONT, tlcPlateNode.getShowSolventFront());
        }
        if(tlcPlateNode.getTransparent() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_TRANSPARENT, tlcPlateNode.getTransparent());
        }
        if(tlcPlateNode.getLineWidth() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_LINE_WIDTH, tlcPlateNode.getLineWidth());
        }else{
            newTlcElement.addAttribute(ParseElementDefinition.TLC_LINE_WIDTH, String.valueOf(environment.getLineWidth()));
        }
        if(tlcPlateNode.getBoldWidth() != null) {
            newTlcElement.addAttribute(ParseElementDefinition.TLC_BOLD_WIDTH, tlcPlateNode.getBoldWidth());
        }else{
            newTlcElement.addAttribute(ParseElementDefinition.TLC_BOLD_WIDTH, String.valueOf(environment.getBoldWidth()));
        }
        if(tlcPlateNode.getHashSpacing() != null){
            newTlcElement.addAttribute(ParseElementDefinition.TLC_HASH_SPACING, tlcPlateNode.getHashSpacing());
        }else{
            newTlcElement.addAttribute(ParseElementDefinition.TLC_HASH_SPACING, String.valueOf(environment.getHashSpacing()));
        }
        int tlcLaneOrder = 0;
        
        for(Object possibleLane : tlcPlateNode.getObjecttagOrAnnotationOrTlclane()){
            if(possibleLane instanceof Tlclane){
                Tlclane tclLaneNode = (Tlclane) possibleLane;
                ParsedElement newTlcLaneElement = new ParsedElement();
                newTlcLaneElement.setId(tclLaneNode.getId());
                newTlcLaneElement.setName(ParseElementDefinition.TLC_LANE);
                
                newTlcLaneElement.addAttribute(ParseElementDefinition.TLC_LANE_ORDER, String.valueOf(tlcLaneOrder));
                tlcLaneOrder++;
                
                for(Object possibleSpot : tclLaneNode.getObjecttagOrAnnotationOrTlcspot()){
                    if(possibleSpot instanceof Tlcspot){
                        
                        Tlcspot tlcSpotNode = (Tlcspot) possibleSpot;
                        
                        ParsedElement newTlcSpotElement = new ParsedElement();
                        
                        newTlcSpotElement.setId(tlcSpotNode.getId());
                        newTlcSpotElement.setName(ParseElementDefinition.TLC_SPOT);
                        if(tlcSpotNode.getCurveType() != null){
                            newTlcSpotElement.addAttribute(ParseElementDefinition.TLC_CURVE_TYPE, tlcSpotNode.getCurveType());
                        }
                        newTlcSpotElement.addAttribute(ParseElementDefinition.TLC_RF, tlcSpotNode.getRf());
                        newTlcSpotElement.addAttribute(ParseElementDefinition.TLC_SPOT_HEIGHT, tlcSpotNode.getHeight());
                        newTlcSpotElement.addAttribute(ParseElementDefinition.TLC_SPOT_WIDTH, tlcSpotNode.getWidth());
                        newTlcSpotElement.addAttribute(ParseElementDefinition.TLC_SPOT_TAIL, tlcSpotNode.getTail());
                        
                        newTlcSpotElement.addElement(parseColor(tlcSpotNode.getFgRGBA(), tlcSpotNode.getColor()));
                        
                        for (Object innerNode : tlcSpotNode.getObjecttagOrAnnotationOrEmbeddedobject()) {
                            if(innerNode instanceof Objecttag){
                                if(tlcSpotNode.getShowRf() != null &&
                                        tlcSpotNode.getShowRf().equalsIgnoreCase(ParseElementDefinition.TLC_SHOW_RF_YES)){
                                    Objecttag objectTagNode = (Objecttag) innerNode;
                                    
                                    for(ParsedElement textTlcSpot : parseObjectTag(objectTagNode, newTlcElement)){
                                        textTlcSpot.setId(tlcSpotNode.getId() + textTlcSpot.getId());
                                        newTlcSpotElement.addElement(textTlcSpot);
                                        currentPage.addElement(setPageProperties(textTlcSpot));
                                    }
                                }
                            }else if(innerNode instanceof Embeddedobject ){
                                Embeddedobject embeddedSpot = (Embeddedobject) innerNode;
                                String id = tlcPlateNode.getId() + StringUtils.UNDERSCORE + tclLaneNode.getId() +
                                        StringUtils.UNDERSCORE + tlcSpotNode.getId();
                                ParsedElement newEmbeddedObjectElement = parseEmbeddedObject(embeddedSpot, id, true);
                                newTlcSpotElement.setName(ParseElementDefinition.TLC_SPOT_IMAGE);
                                newTlcSpotElement.addElement(newEmbeddedObjectElement);
                                currentPage.addElement(newEmbeddedObjectElement);
                            }
                            
                        }
                        newTlcLaneElement.addElement(newTlcSpotElement);
                    }
                }
                newTlcElement.addElement(newTlcLaneElement);
            }
        }
        return newTlcElement;
    }
    
    /*
     *extract the blank spaces from the file name
     *and put '_' to separate parts
     */
    private String underscoreFileName(){
        String fileName;
        if(environment.getFileName().contains(".")){
            fileName = environment.getFileName().substring(0, environment.getFileName().lastIndexOf("."));
        }else{
            fileName = environment.getFileName();
        }
        String[] fileFormat = fileName.split(" ");
        fileName = fileFormat[0];
        for(int i=1; i<fileFormat.length; i++){
            fileName += "_" + fileFormat[i];
        }
        
        return fileName;
    }
    
    private ParsedElement parseEmbeddedObject(Embeddedobject embeddedObject, String id, boolean bound){
        ParsedElement newEmbeddedObjectElement;
        
        if(bound){
            newEmbeddedObjectElement = new ParsedBoundObject();
        }else{
            newEmbeddedObjectElement = new ParsedElement();
            newEmbeddedObjectElement.setEnvironment(environment);
        }
        
        newEmbeddedObjectElement.setName(ParseElementDefinition.EMBEDDED_OBJECT);
        newEmbeddedObjectElement.setId(id);        
        
        String fileName = underscoreFileName();        
        
        fileName += StringUtils.UNDERSCORE + id;
        if(embeddedObject.getBoundingBox() != null){
            newEmbeddedObjectElement.addAttribute(ParseElementDefinition.EMBEDDED_OBJECT_BOUNDING_BOX,
                    embeddedObject.getBoundingBox());
        }
        if(embeddedObject.getZ() !=  null){
            newEmbeddedObjectElement.setZOrder(Integer.parseInt(embeddedObject.getZ()));
        }
        if(embeddedObject.getRotationAngle() != null){
            newEmbeddedObjectElement.addAttribute(ParseElementDefinition.EMBEDDED_OBJECT_ROTATION_ANGLE,
                    embeddedObject.getRotationAngle());
        }
        
        String imageBytes = null;
        String imageFileName = null;
        
        if(embeddedObject.getJPEG() != null){
            imageFileName = fileName + ".jpg";
            imageBytes = embeddedObject.getJPEG();
        }else if(embeddedObject.getGIF() != null){
            imageFileName = fileName + ".gif";
            imageBytes = embeddedObject.getGIF();
        }else if(embeddedObject.getPNG() != null){
            imageFileName = fileName + ".png";
            imageBytes = embeddedObject.getPNG();
        }else if(embeddedObject.getTIFF() != null){
            imageFileName = fileName + ".tif";
            imageBytes = embeddedObject.getTIFF();
        }else if(embeddedObject.getBMP() != null){
            imageFileName = fileName + ".bmp";
            imageBytes = embeddedObject.getBMP();
        }else if(embeddedObject.getWindowsMetafile() != null){
            imageFileName = fileName + ".wmf";
            imageBytes = embeddedObject.getWindowsMetafile();
        }else if(embeddedObject.getCompressedEnhancedMetafile() != null){
            imageFileName = fileName + ".emz";
            imageBytes = embeddedObject.getCompressedEnhancedMetafile();
        }
        if(imageFileName != null){
            newEmbeddedObjectElement.addAttribute(ParseElementDefinition.EMBEDDED_OBJECT_IMAGE_NAME, imageFileName);
        }        
        
        newEmbeddedObjectElement.addAttribute(ParseElementDefinition.EMBEDDED_OBJECT_IMAGE_BYTES, imageBytes);
        
        return newEmbeddedObjectElement;
        
    }
    
    private ParsedElement parseBond(B bondNode, int parentZorder){
        ParsedElement newBondElement = new ParsedElement();
        
        newBondElement.setEnvironment(environment);
        
        newBondElement.setId(bondNode.getId());
        newBondElement.addAttribute(ParseElementDefinition.BOND_BEGIN, bondNode.getB());
        newBondElement.addAttribute(ParseElementDefinition.BOND_END, bondNode.getE());
        if(bondNode.getZ()!= null){
            newBondElement.setZOrder(Integer.parseInt(bondNode.getZ()));
        }else{
            newBondElement.setZOrder(parentZorder+1);
        }
        if(bondNode.getCrossingBonds() != null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_CROSSING_BOND, bondNode.getCrossingBonds());
        }
        
        if(bondNode.getBeginAttach() != null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_BEGIN_ATTACH, bondNode.getBeginAttach());
        }
        
        if(bondNode.getEndAttach() != null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_END_ATTACH, bondNode.getEndAttach());
        }
        
        String display = bondNode.getDisplay();
        String display2 = bondNode.getDisplay2();
        newBondElement.addAttribute(ParseElementDefinition.BOND_DISPLAY, display);
        
        if(display2 != null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_DISPLAY_2, display2);
        }
        
        if(display == null || display.equals(ParseElementDefinition.BOND_DISPLAY_BOLD)
        || display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_BEGIN) || display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_END)){
            newBondElement.setName(ParseElementDefinition.SOLID_BOND);
        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_BEGIN)){
            newBondElement.setName(ParseElementDefinition.WEDGE_HASH_BEGIN_BOND);
        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_WEDGE_HASH_END)){
            newBondElement.setName(ParseElementDefinition.WEDGE_HASH_END_BOND);
        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HASH)){
            newBondElement.setName(ParseElementDefinition.HASH_BOND);
        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_BEGIN)){
            newBondElement.setName(ParseElementDefinition.HOLLOW_WEDGE_BOND);
        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_HOLLOW_WEDGE_END)){
            newBondElement.setName(ParseElementDefinition.HOLLOW_WEDGE_BOND);
        } else if(display.equals(ParseElementDefinition.BOND_DISPLAY_DASH)){
            newBondElement.setName(ParseElementDefinition.DASH_BOND);
        } else if (display.equals(ParseElementDefinition.BOND_DISPLAY_WAVY)) {
            if(bondNode.getOrder() != null && bondNode.getOrder().equals(ParseElementDefinition.BOND_ORDER_2)){
                newBondElement.setName(ParseElementDefinition.WAVY_2_BOND);
            } else{
                newBondElement.setName(ParseElementDefinition.WAVY_BOND);
            }
        } else{
            newBondElement.setName(ParseElementDefinition.SOLID_BOND);
        }
        
        if(bondNode.getBondSpacing() == null && bondNode.getBondSpacingAbs() == null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_SPACING, Double.toString(environment.getBondSpacing()));
        } else{
            if(bondNode.getBondSpacing() != null){
                newBondElement.addAttribute(ParseElementDefinition.BOND_SPACING, bondNode.getBondSpacing());
            } else {
                newBondElement.addAttribute(ParseElementDefinition.BOND_SPACING, bondNode.getBondSpacingAbs());
            }
        }
        
        if(bondNode.getHashSpacing() == null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_HASH_SPACING, Double.toString(environment.getHashSpacing()));
        } else{
            newBondElement.addAttribute(ParseElementDefinition.BOND_HASH_SPACING, bondNode.getHashSpacing());
        }
        
        if(bondNode.getLineWidth() == null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_LINE_WIDTH, Double.toString(environment.getLineWidth()));
        } else{
            newBondElement.addAttribute(ParseElementDefinition.BOND_LINE_WIDTH, bondNode.getLineWidth());
        }
        
        if (bondNode.getBoldWidth() == null) {
            newBondElement.addAttribute(ParseElementDefinition.BOND_BOLD_WIDTH, Double.toString(environment.getBoldWidth()));
        } else {
            newBondElement.addAttribute(ParseElementDefinition.BOND_BOLD_WIDTH, bondNode.getBoldWidth());
        }
        
        if(bondNode.getOrder() != null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_ORDER, bondNode.getOrder());
            
            if(bondNode.getOrder().equals(ParseElementDefinition.BOND_ORDER_DATIVE)){
                newBondElement.setName(ParseElementDefinition.DATIVE_BOND);
                newBondElement.addAttribute(ParseElementDefinition.BOND_DISPLAY, ParseElementDefinition.BOND_DISPLAY_DATIVE);
            }
        } else {
            newBondElement.addAttribute(ParseElementDefinition.BOND_ORDER, ParseElementDefinition.BOND_ORDER_1);
        }
        
        if(bondNode.getDoublePosition() != null){
            newBondElement.addAttribute(ParseElementDefinition.BOND_DOUBLE_POSITION, bondNode.getDoublePosition());
        }
        
        if (bondNode.getMarginWidth() != null) {
            newBondElement.addAttribute(ParseElementDefinition.BOND_MARGIN_WIDTH, bondNode.getMarginWidth());
        }
        
        newBondElement.addElement(parseColor(bondNode.getFgRGBA(), bondNode.getColor()));
        
        environment.addParsedElement(newBondElement);
        
        return newBondElement;
    }
    
    /**
     *Obtain all the attributes to represent a rlogic tag
     */
    private ParsedElement parseRLogic(Rlogic rLogicNode){
        ParsedElement newRLogicElement = new ParsedElement();
        
        newRLogicElement.setId(rLogicNode.getId());
        
        newRLogicElement.setName(ParseElementDefinition.R_LOGIC);
        newRLogicElement.addAttribute(ParseElementDefinition.R_LOGIC_POSITION, rLogicNode.getP());
        newRLogicElement.addAttribute(ParseElementDefinition.R_LOGIC_BOUNDING_BOX, rLogicNode.getBoundingBox());
        
        //This is necesary to can be processed by TextProcessor
        newRLogicElement.addAttribute(ParseElementDefinition.TEXT_JUSTIFICATION, "0");
        
        newRLogicElement.addElement(parseColor(rLogicNode.getFgRGBA(), rLogicNode.getColor()));
        
        if(rLogicNode.getZ() != null){
            newRLogicElement.setZOrder(Integer.parseInt(rLogicNode.getZ()));
        }
        
        List<ParsedElement> stringElements = parseStringNode(rLogicNode, rLogicNode.getZ());
        
        for(ParsedElement string : stringElements){
            newRLogicElement.addElement(string);
        }
        
        newRLogicElement.setEnvironment(environment);
        
        return newRLogicElement;
    }
    
    private List<ParsedElement> parseText(T textNode, String parentZOrder){
        return parseText(textNode, parentZOrder, null);
    }
    
    private List<ParsedElement> parseText(T textNode, String parentZOrder, N parentNode){
        List<ParsedElement> result = new ArrayList();
        ParsedElement newTextElement = new ParsedElement();
        
        newTextElement.setId(ParseElementDefinition.TEXT + Integer.toString(lastFontId++));
        
        newTextElement.setName(ParseElementDefinition.TEXT);
        newTextElement.addAttribute(ParseElementDefinition.TEXT_POSITION, textNode.getP());
        newTextElement.addAttribute(ParseElementDefinition.TEXT_BOUNDING_BOX, textNode.getBoundingBox());
        
        // if text has its own Z order, use it
        if (textNode.getZ() != null) {
            newTextElement.setZOrder(Integer.parseInt(textNode.getZ()));
        } else if (parentZOrder != null) {
            // use parent's Z order plus one, so it appears over the containing object
            int textZOrder = Integer.parseInt(parentZOrder) + 1;
            newTextElement.setZOrder(textZOrder);
        }
        
        newTextElement.addElement(parseColor(textNode.getFgRGBA(), textNode.getColor()));
        
        String rotationAngle = textNode.getRotationAngle();
        if(rotationAngle != null){
            newTextElement.addAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE, rotationAngle);
        }
        
        String textVisible = textNode.getVisible();
        if (textVisible != null) {
            newTextElement.addAttribute(ParseElementDefinition.TEXT_VISIBLE, textVisible);
        }
        
        String justification = textNode.getJustification();
        newTextElement.addAttribute(ParseElementDefinition.TEXT_JUSTIFICATION, justification);
        
        if(textNode.getLabelAlignment() != null){
            newTextElement.addAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT, textNode.getLabelAlignment());
        }
        if (parentNode != null && parentNode.getLabelDisplay() != null) {
            newTextElement.addAttribute(ParseElementDefinition.TEXT_LABEL_DISPLAY, parentNode.getLabelDisplay());
        }
        if(textNode.getLineHeight() != null){
            newTextElement.addAttribute(ParseElementDefinition.TEXT_LINE_HEIGHT, textNode.getLineHeight());
        }
        List<ParsedElement> stringElements = parseStringNode(textNode, parentZOrder);
        
        for(ParsedElement string : stringElements){
            if(string.getName().equals(ParseElementDefinition.STRING)){
                //In this case the parsed element is a string node
                newTextElement.addElement(string);
            } else{
                //In this case the parsed element is a text tag node and
                //must be processed for a text processor
                result.add(string);
            }
        }
        
        newTextElement.setEnvironment(environment);
        result.add(newTextElement);
        
        TextProcessor.processFormula(newTextElement);
        
        return result;
    }
    
    private List<ParsedElement> parseStringNode(Object textObjec, String parentZOrder){
        List<ParsedElement> result = new ArrayList();
        
        int[] lineStarts = new int[0];
        
        List<Object> strings = null;
        if(textObjec instanceof T){
            T textNode = ((T) textObjec);
            strings = textNode.getSOrObjecttagOrAnnotation();
            lineStarts = createLineStarts(textNode.getLineStarts());
        } else if(textObjec instanceof Rlogic){
            Rlogic textNode = ((Rlogic) textObjec);
            strings = textNode.getSOrRlogicitem();
        }
        
        // indexes where substrings start inside the whole text
        int[] substringIndexes = new int[strings.size()];
        
        // the values of the strings inside the text
        String[] values = new String[strings.size()];
        
        int currentSubstring = 0;
        int currentPosition = 0;
        int currentLineStart = 0;
        
        // stores an offset of \n characters added
        int addedLinesOffset = 0;
                
        // iterate the strings to add \n where needed to break them into several lines
        for (Object possibleString : strings) {
            if (possibleString instanceof S) {
                S stringNode = (S) possibleString;
                
                // store the string's value
                values[currentSubstring] = stringNode.getContent();
                
                // index where the next string starts
                int nextSubstring = substringIndexes[currentSubstring] + values[currentSubstring].length();
                
                // check lineStarts values
                while (currentLineStart < lineStarts.length) {
                    // the current line start value
                    int lineStartValue = lineStarts[currentLineStart] + addedLinesOffset;
                    
                    // if current line start index falls inside the current string
                    if (lineStartValue > substringIndexes[currentSubstring] && lineStartValue <= nextSubstring) {
                        // index inside this string where the line start falls
                        int newLineIndex = lineStartValue - substringIndexes[currentSubstring];
                        
                        // if line start should be at the end of the string
                        if (newLineIndex == values[currentSubstring].length()) {
                            // if there isn't a \n already, add it
                            if (values[currentSubstring].charAt(values[currentSubstring].length() - 1) != '\n') {
                                values[currentSubstring] += "\n";
                                addedLinesOffset++;
                            }
                        }
                        // if there isn't a \n character to the left already, add it
                        else if (values[currentSubstring].charAt(newLineIndex - 1) != '\n') {
                            StringBuilder substringBuilder = new StringBuilder(values[currentSubstring]);
                            substringBuilder.insert(newLineIndex, '\n');
                            values[currentSubstring] = substringBuilder.toString();
                            addedLinesOffset++;
                        }
                        // correct the value of next substring start in case \n were added
                        nextSubstring = substringIndexes[currentSubstring] + values[currentSubstring].length();
                        currentLineStart++;
                    } else {
                        break;
                    }
                }
                
                // set next substring index in the indexes array
                if (currentSubstring < substringIndexes.length - 1) {
                    substringIndexes[currentSubstring + 1] = nextSubstring;
                }
                
                currentSubstring++;
            }
        }
        
        currentSubstring = 0;
        
        // zero padding spaces to use in string ids, for correct string ordering
        // value is number of digits in the total number of strings
        int numberOfPaddingSpaces = String.valueOf(strings.size()).length();
        
        // iterate through the strings to create parsed elements
        // but use the values[] array defined above, which contains
        // the correct line breaks in the strings
        for(Object possibleString : strings){
            if(possibleString instanceof S){
                S stringNode = (S) possibleString;
                
                ParsedElement newStringElement = new ParsedElement();
                
                newStringElement.setId(zeroPadString(String.valueOf(currentSubstring), numberOfPaddingSpaces));
                newStringElement.setName(ParseElementDefinition.STRING);
                
                newStringElement.addElement(parseColor(stringNode.getRgba(), stringNode.getColor()));
                
                String fontFace = stringNode.getFace();
                if(fontFace != null){
                    newStringElement.addAttribute(ParseElementDefinition.STRING_FACE, fontFace);
                } else{
                    newStringElement.addAttribute(ParseElementDefinition.STRING_FACE, ParseElementDefinition.STRING_FACE_0);
                }
                
                Font font = null;
                if(stringNode.getFont() == null && labelFontIndex != null){
                    font = fonts.get(labelFontIndex);
                } else if(stringNode.getFont()!=null){
                    
                    font = fonts.get(stringNode.getFont());
                }
                
                if(font != null) {
                    newStringElement.addAttribute(ParseElementDefinition.STRING_FONT, font.getName());
                    newStringElement.addAttribute(ParseElementDefinition.STRING_CHAR_SET, font.getCharset());
                }else{
                    newStringElement.addAttribute(ParseElementDefinition.STRING_FONT, DEFAULT_FONT_NAME);
                    newStringElement.addAttribute(ParseElementDefinition.STRING_CHAR_SET, DEFAULT_STRING_CHAR_SET);
                }
                if(stringNode.getSize() != null){
                    newStringElement.addAttribute(ParseElementDefinition.STRING_SIZE, stringNode.getSize());
                }else{
                    newStringElement.addAttribute(ParseElementDefinition.STRING_SIZE, DEFAULT_LABEL_SIZE);
                }
                newStringElement.setValue(values[currentSubstring++]);
                
                result.add(newStringElement);
            } else if(possibleString instanceof Objecttag){
                //the owner doesn't has any valid information for the
                //parseObjectTag method so we send a null parameter
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) possibleString, null);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    result.add(objectTagElement);
                }
            }
        }
        
        return result;
    }
    
    private int[] createLineStarts(String lineStarts){
        if(lineStarts != null){
            String[] arrayLineStarts = lineStarts.split(" ");
            int[] result = new int[arrayLineStarts.length];
            
            for(int i = 0; i < arrayLineStarts.length; i++){
                result[i] = Integer.parseInt(arrayLineStarts[i]);
            }
            
            return result;
        } else{
            return new int[0];
        }
    }
    
    private ParsedElement parseCurve(Curve curveNode){
        ParsedElement newCurveElement = new ParsedElement();
        newCurveElement.setEnvironment(environment);
        
        if(curveNode.getCurvePoints() != null){
            
            newCurveElement.setName(ParseElementDefinition.SPLINE);
            newCurveElement.addAttribute(ParseElementDefinition.SPLINE_CURVE_POINTS, curveNode.getCurvePoints());
            newCurveElement.setId(curveNode.getId());
            
            ParsedElement curveNodeColor = null ;
            
            //Adding the color to the curve element.
            newCurveElement.addElement(parseColor(curveNode.getFgRGBA(), curveNode.getColor()));
            
            String curveArrowheadType = null;
            
            if(curveNode.getArrowheadType() != null){
                /*Asign the parsed arrow head type value
                 *if the node has a parsed value.*/
                curveArrowheadType = curveNode.getArrowheadType();
            }else{
                /*Asing the default arrow head type value if
                 *the node does not have a parsed value.*/
                curveArrowheadType = DEFAULT_ARROWHEAD_TYPE;
            }
            
            //Adding the arrow head type to the curve element.
            newCurveElement.addAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_TYPE, curveArrowheadType);
            
            newCurveElement.setZOrder(Integer.parseInt(curveNode.getZ()));
            
            if(curveNode.getLineType() != null ){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_LINE_TYPE, curveNode.getLineType());
            }
            if(curveNode.getCurveType() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_CURVE_TYPE, curveNode.getCurveType());
            }
            
            if(curveNode.getCurveType()!= null && curveNode.getCurveType().equals(ParseElementDefinition.CURVE_TYPE_CLOSED)){
                /*Add the attribute spline closed with the value "yes" in the case
                 *of closed curve type.*/
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_CLOSED, ParseElementDefinition.SPLINE_CLOSED_YES);
            }else if(curveNode.getClosed() != null){
                /*Add the parsed closed value of the curve element in case of not
                 *closed curve type.*/
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_CLOSED, curveNode.getClosed());
            }
            if(curveNode.getFillType() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_FILL_TYPE, curveNode.getFillType());
            }
            if(curveNode.getFadePercent() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_FADE_PERCENT, curveNode.getFadePercent());
            }
            if(curveNode.getCurveSpacing() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_CURVE_SPACING, curveNode.getCurveSpacing());
            }
            if(curveNode.getCurveType() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_CURVE_TYPE, curveNode.getCurveType());
            }
            if(curveNode.getArrowheadHead() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_HEAD, curveNode.getArrowheadHead());
            }
            if(curveNode.getArrowheadTail() != null){
                newCurveElement.addAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_TAIL, curveNode.getArrowheadTail());
            }
            if(curveNode.getHashSpacing() != null){
                newCurveElement.addAttribute(ParseElementDefinition.HASH_SPACING, curveNode.getHashSpacing());
            }
            if(curveNode.getLineWidth() != null){
                newCurveElement.addAttribute(ParseElementDefinition.LINE_WIDTH, curveNode.getLineWidth());
            }
            if (curveNode.getBoldWidth() != null) {
                newCurveElement.addAttribute(ParseElementDefinition.BOLD_WIDTH, curveNode.getBoldWidth());
            }
            
            return newCurveElement;
            
        }else{
            return null;
        }
    }
    
    private ParsedElement parseGraphic(Graphic graphicNode, ParsedPage currentPage) throws ElementNotSupportedException {
        ParsedElement newGraphicElement = new ParsedElement();
        
        newGraphicElement.setEnvironment(environment);
        newGraphicElement.setId(graphicNode.getId());
        
        if(graphicNode.getGraphicType() != null){
            if(graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE)){
                if(graphicNode.getRectangleType().startsWith(ParseElementDefinition.GRAPHIC_ROUNDED_RECTANGLE_PLAIN)){
                    newGraphicElement.setName(ParseElementDefinition.GRAPHIC_ROUNDED_RECTANGLE);
                }else{
                    newGraphicElement.setName(ParseElementDefinition.GRAPHIC_PLAIN_RECTANGLE);
                }
                newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_RECTANGLE_TYPE, graphicNode.getRectangleType());
                
            }else if(graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_OVAL)){
                if(graphicNode.getOvalType().startsWith(ParseElementDefinition.GRAPHIC_CIRCLE)){
                    newGraphicElement.setName(ParseElementDefinition.GRAPHIC_CIRCLE);
                }else{
                    newGraphicElement.setName(ParseElementDefinition.GRAPHIC_OVAL);
                }
                newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE, graphicNode.getOvalType());
            }else if(graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_BRACKET)){
                newGraphicElement.setName(ParseElementDefinition.GRAPHIC_TYPE_BRACKET);
            }else if(graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_SYMBOL)){
                if (graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_ELECTRON) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_LONE_PAIR) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_RADICAL_ANION) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_RADICAL_CATION)) {
                    newGraphicElement.setName(ParseElementDefinition.SYMBOL_TYPE_RADICAL);
                } else if (graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_MINUS) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_PLUS) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_CIRCLE_MINUS) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_CIRCLE_PLUS)) {
                    newGraphicElement.setName(ParseElementDefinition.SYMBOL_TYPE_CHARGE);
                } else if (graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_DAGGER) ||
                        graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_DOUBLE_DAGGER)) {
                    newGraphicElement.setName(ParseElementDefinition.SYMBOL_TYPE_DAGGERS);
                } else {
                    newGraphicElement.setName(ParseElementDefinition.GRAPHIC_TYPE_SYMBOL);
                }
            }else if(graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_ARC)
            || graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_LINE)){
                newGraphicElement = parseArrow(graphicNode);
            }else if(graphicNode.getGraphicType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_TYPE_ORBITAL)) {
                if (graphicNode.getOvalType() != null && graphicNode.getOvalType().length() > 0) {
                    if(graphicNode.getOvalType().startsWith(ParseElementDefinition.GRAPHIC_CIRCLE)){
                        newGraphicElement.setName(ParseElementDefinition.GRAPHIC_CIRCLE); // will be treated by CircleProcessor
                    }else{
                        newGraphicElement.setName(ParseElementDefinition.GRAPHIC_OVAL); // will be treated by OvalProcessor
                    }
                    newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE, graphicNode.getOvalType());
                } else if (graphicNode.getOvalType() != null && graphicNode.getOrbitalType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_OVAL)) {
                    newGraphicElement.setName(ParseElementDefinition.GRAPHIC_OVAL); // must also be treated by OvalProcessor
                } else {
                    String orbitalType = graphicNode.getOrbitalType();
                    if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_LOBE)) {
                        newGraphicElement.setName(ParseElementDefinition.ORBITAL_LOBE); // will be treated by OrbitalProcessor
                    } else if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_P)) {
                        newGraphicElement.setName(ParseElementDefinition.ORBITAL_P); // will be treated by POrbitalProcessor
                    } else if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_HYBRID)) {
                        newGraphicElement.setName(ParseElementDefinition.ORBITAL_HYBRID); // will be treated by HybridOrbitalProcessor
                    } else if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_DZ2)) {
                        newGraphicElement.setName(ParseElementDefinition.ORBITAL_DZ2); // will be treated by DZ2OrbitalProcessor
                    } else if (orbitalType.startsWith(ParseElementDefinition.ORBITAL_DXY)) {
                        newGraphicElement.setName(ParseElementDefinition.ORBITAL_DXY); // will be treated by DXYOrbitalProcessor
                    } else {
                        newGraphicElement = parseOrbital(graphicNode);
                    }
                }
                newGraphicElement.addAttribute(ParseElementDefinition.ORBITAL_TYPE, graphicNode.getOrbitalType());
            }else{
                throw new ElementNotSupportedException(graphicNode.getGraphicType());
            }
        }
        newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_TYPE, graphicNode.getGraphicType());
        newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_BOUNDING_BOX, graphicNode.getBoundingBox());
        newGraphicElement.setZOrder(Integer.parseInt(graphicNode.getZ()));
        if(graphicNode.getHashSpacing() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.HASH_SPACING, graphicNode.getHashSpacing());
        }
        
        //Adding color element to the ghaphic element.
        newGraphicElement.addElement(parseColor(graphicNode.getFgRGBA(), graphicNode.getColor()));
        
        if(graphicNode.getMajorAxisEnd3D() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_MAJOR_AXIS_END_3D, graphicNode.getMajorAxisEnd3D());
        }
        if(graphicNode.getMinorAxisEnd3D() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_MINOR_AXIS_END_3D, graphicNode.getMinorAxisEnd3D());
        }
        if(graphicNode.getCenter3D() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_CENTER_3D, graphicNode.getCenter3D());
        }
        if(graphicNode.getBoldWidth() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_BOLD_WIDTH, graphicNode.getBoldWidth());
        }
        if(graphicNode.getLineWidth() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_LINE_WIDTH, graphicNode.getLineWidth());
        }
        if(graphicNode.getFadePercent() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_FADE_PERCENT, graphicNode.getFadePercent());
        }
        if(graphicNode.getCornerRadius() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_CORNER_RADIUS, graphicNode.getCornerRadius());
        }
        if(graphicNode.getShadowSize() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_RECTANGLE_TYPE_SHADOW_SIZE, graphicNode.getShadowSize());
        }
        if(graphicNode.getArrowType() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.GRAPHIC_ARROW_TYPE, graphicNode.getArrowType());
        }
        
        if(graphicNode.getBracketType() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.BRACKET_TYPE, graphicNode.getBracketType());
        }
        if(graphicNode.getLipSize() != null){
            newGraphicElement.addAttribute(ParseElementDefinition.BRACKET_LIP_SIZE, graphicNode.getLipSize());
        }
        
        if (graphicNode.getSymbolType() != null) {
            newGraphicElement.addAttribute(ParseElementDefinition.SYMBOL_TYPE, graphicNode.getSymbolType());
        }
        
        boolean hasTextElements = false;
        
        for (Object innerNode : graphicNode.getObjecttagOrAnnotationOrRepresent()) {
            if(innerNode instanceof Objecttag){
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) innerNode, newGraphicElement);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    objectTagElement.setId(graphicNode.getId() + objectTagElement.getId());
                    currentPage.addElement(setPageProperties(objectTagElement));
                }
            } else if(innerNode instanceof T){
                hasTextElements = true;
                List<ParsedElement> textElements =
                        parseText((T) innerNode, graphicNode.getZ());
                
                if(graphicNode.getGraphicType().equals(ParseElementDefinition.GRAPHIC_TYPE_SYMBOL)){
                    newGraphicElement.addAttribute(
                            ParseElementDefinition.BOUNDING_BOX_TEXT,
                            textElements.get(0).getAttribute(ParseElementDefinition.BOUNDING_BOX));
                    newGraphicElement.addAttribute(
                            ParseElementDefinition.TEXT_POSITION,
                            textElements.get(0).getAttribute(ParseElementDefinition.TEXT_POSITION));
                }
                
                for(ParsedElement textElement : textElements){
                    textElement.setId(graphicNode.getId() + textElement.getId());
                    currentPage.addElement(setPageProperties(textElement));
                }
            }
        }
        
        // This is for creating the corresponding labels for query symbols created
        // with ChemDraw old versions.
        if(!hasTextElements){
            if(graphicNode.getGraphicType().equals(ParseElementDefinition.GRAPHIC_TYPE_SYMBOL)
            && (graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_RELATIVE)
            || graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_ABSOLUTE)
            || graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_RACEMIC))){
                addDefaultSymbolLabel(graphicNode, currentPage);
            }
        }
        
        return newGraphicElement;
    }
    
    /**
     * This method is for creating the corresponding labels for query symbols created
     * with ChemDraw old versions.
     */
    private void addDefaultSymbolLabel(Graphic graphicNode, ParsedPage currentPage){
        
        ParsedElement newTextElement = new ParsedElement();
        newTextElement.setName(ParseElementDefinition.TEXT);
        String[] coordinates = graphicNode.getBoundingBox().split(" ");
        
        Point beginPoint = new Point(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
        
        //This is for calculate the text begin ussing the minor axis.
        Point textPoint = new Point(beginPoint.getX(), beginPoint.getY() + QuerySymbolsProcessor.DEFAULT_SYMBOL_BASELINE_OFFSET);
        
        newTextElement.setId(ParseElementDefinition.TEXT + graphicNode.getId());
        currentPage.addElement(setPageProperties(newTextElement));
        
        newTextElement.setZOrder(Integer.parseInt(graphicNode.getZ()));
        newTextElement.addAttribute(ParseElementDefinition.TEXT_POSITION, textPoint.getX() + " " + textPoint.getY());
        newTextElement.addAttribute(ParseElementDefinition.TEXT_VISIBLE, DEFAULT_TEXT_VISIBLE);
        newTextElement.addAttribute(ParseElementDefinition.TEXT_JUSTIFICATION, ParseElementDefinition.TEXT_ALIGNMENT_CENTER);
        
        newTextElement.setEnvironment(environment);
        
        ParsedElement newSubelement = new ParsedElement();
        
        newSubelement.setId(ParseElementDefinition.STRING+graphicNode.getId());
        newSubelement.setName(ParseElementDefinition.STRING);
        
        if(graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_RACEMIC)){
            newSubelement.setValue(ParseElementDefinition.SYMBOL_TYPE_RACEMIC_LABEL);
        }else if(graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_ABSOLUTE)){
            newSubelement.setValue(ParseElementDefinition.SYMBOL_TYPE_ABSOLUTE_LABEL);
        }else if(graphicNode.getSymbolType().equals(ParseElementDefinition.SYMBOL_TYPE_RELATIVE)){
            newSubelement.setValue(ParseElementDefinition.SYMBOL_TYPE_RELATIVE_LABEL);
        }
        
        newSubelement.setZOrder(newTextElement.getZOrder());
        
        newSubelement.addElement(parseColor(graphicNode.getFgRGBA(), graphicNode.getColor()));
        
        newSubelement.addAttribute(ParseElementDefinition.STRING_SIZE, Double.toString(environment.getCaptionFontSize()));
        newSubelement.addAttribute(ParseElementDefinition.STRING_FACE, ParseElementDefinition.STRING_FACE_0);
        newSubelement.addAttribute(ParseElementDefinition.STRING_FONT, environment.getCaptionFont());
        
        newSubelement.setEnvironment(environment);
        newTextElement.addElement(newSubelement);
        
        currentPage.addElement(newTextElement);
        
    }
    
    //This method is to parse orbitals in files generated with older versions of ChemDraw (9 and before)
    private ParsedElement parseOrbital(Graphic orbitalNode){
        ParsedElement newOrbitalElement = new ParsedElement();
        
        newOrbitalElement.setEnvironment(environment);
        newOrbitalElement.setId(orbitalNode.getId());
        
        if (orbitalNode.getOrbitalType().startsWith(ParseElementDefinition.ORBITAL_TYPE_S)){
            newOrbitalElement.setName(ParseElementDefinition.GRAPHIC_CIRCLE); // will be treated by CircleProcessor
        } else if (orbitalNode.getOrbitalType().startsWith(ParseElementDefinition.ORBITAL_TYPE_OVAL)){
            newOrbitalElement.setName(ParseElementDefinition.GRAPHIC_OVAL); // will be treated by CircleProcessor
        }
        
        newOrbitalElement.addAttribute(ParseElementDefinition.GRAPHIC_OVAL_TYPE, orbitalNode.getOrbitalType());
        //Files generated with version 9 and befor of chemdraw do not include Major and minor axis information
        //The minor axis is calculated in the corresponding processor
        if (orbitalNode.getBoundingBox() != null){
            String[] boundingBoxPoints = orbitalNode.getBoundingBox().split(" ");
            newOrbitalElement.addAttribute(ParseElementDefinition.GRAPHIC_MAJOR_AXIS_END_3D, (boundingBoxPoints[BOUNDINGBOX_LEFT] + " " + boundingBoxPoints[BOUNDINGBOX_TOP]));
            newOrbitalElement.addAttribute(ParseElementDefinition.GRAPHIC_CENTER_3D, (boundingBoxPoints[BOUNDINGBOX_RIGHT] + " " + boundingBoxPoints[BOUNDINGBOX_BOTTOM]));
        }
        
        return newOrbitalElement;
    }
    
    private ParsedElement parseArrow(Graphic arrowNode){
        ParsedElement newArrowElement = new ParsedElement();
        
        newArrowElement.setEnvironment(environment);
        
        newArrowElement.setId(arrowNode.getId() + ParseElementDefinition.ARROW);
        
        boolean isArc = false;
        
        if(arrowNode.getArrowType() == null && arrowNode.getHeadSize() == null){
            
            //Arrow headless is here beacause it must to be processed by ArcProcessor.
            newArrowElement.setName(ParseElementDefinition.ARROW_HEADLESS);
            isArc = true;
        }else {
            
            newArrowElement.setName(ParseElementDefinition.ARROW);
            
        }
        
        newArrowElement.addElement(parseColor(arrowNode.getFgRGBA(), arrowNode.getColor()));
        
        if (arrowNode.getBoundingBox() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.BOUNDING_BOX, arrowNode.getBoundingBox());
        }
        
        if (arrowNode.getZ() != null) {
            newArrowElement.setZOrder(Integer.parseInt(arrowNode.getZ()));
        }
        
        if (arrowNode.getArrowType()!= null) {
            // ArrowType property in graphic node must be translated into
            // appropriate values for ArrowheadType, ArrowheadTail and ArrowheadHead
            // properties of arrow node
            
            String arrowType = arrowNode.getArrowType();
            String arrowHead = null;
            String arrowTail = null;
            
            if(arrowType.equals(ParseElementDefinition.GRAPHIC_ARROW_TYPE_HALF_HEAD)){
                arrowHead = ParseElementDefinition.ARROW_HALF_LEFT;
                
                double arrowAngularSize = 0;
                double arrowHeadSize = 0;
                if(arrowNode.getHeadSize() != null){
                    arrowHeadSize = Double.parseDouble(arrowNode.getHeadSize());
                }
                
                if(arrowNode.getAngularSize() != null){
                    arrowAngularSize = Double.parseDouble(arrowNode.getAngularSize());
                }
                //taken from C++ code
                if(arrowHeadSize < 0 || arrowAngularSize >0){
                    arrowHead = ParseElementDefinition.ARROW_HALF_RIGHT;
                }
                
                arrowType = ParseElementDefinition.ARROW_TYPE_SOLID;
            }else if(arrowType.equals(ParseElementDefinition.GRAPHIC_ARROW_TYPE_FULL_HEAD)){
                arrowType = ParseElementDefinition.ARROW_TYPE_SOLID;
                arrowHead = ParseElementDefinition.ARROW_TYPE_FULL;
            }else if(arrowType.equals(ParseElementDefinition.ARROW_TYPE_RESONANCE)) {
                arrowType = ParseElementDefinition.ARROW_TYPE_SOLID;
                arrowHead = ParseElementDefinition.ARROW_TYPE_FULL;
                arrowTail = ParseElementDefinition.ARROW_TYPE_FULL;
            }else if(arrowType.equals(ParseElementDefinition.ARROW_TYPE_EQUILIBRIUM)) {
                arrowHead = ParseElementDefinition.ARROW_HALF_LEFT;
                arrowTail = ParseElementDefinition.ARROW_HALF_LEFT;
                double arrowHeadSize = Double.parseDouble(arrowNode.getHeadSize());
                if (arrowHeadSize < 0) {
                    arrowHead = ParseElementDefinition.ARROW_HALF_RIGHT;
                    arrowTail = ParseElementDefinition.ARROW_HALF_RIGHT;
                }
                arrowType = ParseElementDefinition.ARROW_TYPE_SOLID;
                newArrowElement.addAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING, DEFAULT_GRAPHIC_ARROW_SHAFT_SPACING);
            }else if(arrowType.equals(ParseElementDefinition.GRAPHIC_ARROW_TYPE_HOLLOW)) {
                arrowHead = ParseElementDefinition.ARROW_TYPE_FULL;
                // shaft spacing in hollow arrows is the same as head size
                newArrowElement.addAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING, arrowNode.getHeadSize());
                newArrowElement.setName(ParseElementDefinition.ARROW_HOLLOW); // will be treated by HollowArrowProcessor
            }else if(arrowType.equals(ParseElementDefinition.ARROW_TYPE_RETRO_SYNTHETIC)) {
                arrowHead = ParseElementDefinition.ARROW_TYPE_FULL;
                arrowType = ParseElementDefinition.ARROW_ANGLE;
                // shaft spacing in retrosynthetic arrows is the same as head size
                newArrowElement.addAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING, arrowNode.getHeadSize());
            }
            
            // ARROW_HEAD_HEAD and ARROW_HEAD_TYPE are seted beacause both are needed for ArrowProcessor
            if (arrowHead != null) {
                newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_HEAD, arrowHead);
            }
            if (arrowTail != null) {
                newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_TAIL, arrowTail);
            }
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_TYPE, arrowType);
            
            // set ArrowType here to be used by ArrowProcessor
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_TYPE, arrowNode.getArrowType());
        } else {
            // set default values for ArrowheadType and ArrowheadHead
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_TYPE, ParseElementDefinition.ARROW_TYPE_SOLID);
        }
        
        if(arrowNode.getLineType() != null){
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_LINE_TYPE, arrowNode.getLineType());
            if (arrowNode.getLineType().equalsIgnoreCase(ParseElementDefinition.ARROW_WAVY)) {
                // will be treated by WavyLineProcessor
                newArrowElement.setName(ParseElementDefinition.ARROW_WAVY);
            }
        }
        
        if (arrowNode.getHeadSize() != null) {
            // headSize might be negative so set the absolute value
            int headSize = Math.abs(Integer.parseInt(arrowNode.getHeadSize()));
            
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_SIZE, String.valueOf(headSize));
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH, String.valueOf(headSize * ARROW_HEAD_WIDTH_PROPORTION));
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_CENTER_SIZE, String.valueOf(headSize * ARROW_HEAD_CENTER_SIZE_PROPORTION));
        }
        
        //The split method create an array with the 4 values of the  bounding box.
        String[] boundingBoxArray = arrowNode.getBoundingBox().split(" ");
        
        //The values 0 and 1 are the x and y cordinates of the arrow head point.
        if(arrowNode.getAngularSize() != null ){
            
            String angularSize = arrowNode.getAngularSize();
            if (isArc) {
                double arcAngle = Double.parseDouble(angularSize);
                angularSize = String.valueOf(-arcAngle);
            }
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE, angularSize);
            
            
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_3D,
                    boundingBoxArray[0]+" "+boundingBoxArray[1]);
            //The values 2 and 3 are the x and y cordinates of the arrow center point.
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_CENTER_3D,
                    boundingBoxArray[2]+" "+boundingBoxArray[3]);
            
        } else {
            
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_3D,
                    boundingBoxArray[0]+" "+boundingBoxArray[1]);
            //The values 2 and 3 are the x and y cordinates of the arrow tail point.
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_TAIL_3D,
                    boundingBoxArray[2]+" "+boundingBoxArray[3]);
            
        }
        
        if (arrowNode.getMajorAxisEnd3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_MAJOR_AXIS_END_3D, arrowNode.getMajorAxisEnd3D());
        }
        
        if (arrowNode.getMinorAxisEnd3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_MINOR_AXIS_END_3D, arrowNode.getMinorAxisEnd3D());
        }
        
        if (arrowNode.getLineWidth() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH, arrowNode.getLineWidth());
        } else {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH, String.valueOf(environment.getLineWidth()));
        }
        
        if (arrowNode.getBoldWidth() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH, arrowNode.getBoldWidth());
        } else {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH, String.valueOf(environment.getBoldWidth()));
        }
        
        return newArrowElement;
    }
    
    private ParsedElement parseArrow(Arrow arrowNode){
        ParsedElement newArrowElement = new ParsedElement();
        
        newArrowElement.setEnvironment(environment);
        
        newArrowElement.setId(arrowNode.getId() + ParseElementDefinition.ARROW);
        newArrowElement.setName(ParseElementDefinition.ARROW_HEADLESS);
        
        //Remove the selected component from the HashMap
        //and use it for the current parsing
        Graphic graphicNode = graphics.remove(arrowNode.getId());
        
        newArrowElement.addElement(parseColor(arrowNode.getFgRGBA(), arrowNode.getColor()));
        
        if (arrowNode.getBoundingBox() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.BOUNDING_BOX, arrowNode.getBoundingBox());
        }
        if (arrowNode.getZ() != null) {
            newArrowElement.setZOrder(Integer.parseInt(arrowNode.getZ()));
        }
        
        if (arrowNode.getArrowheadHead() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_HEAD, arrowNode.getArrowheadHead());
            newArrowElement.setName(ParseElementDefinition.ARROW);
        }
        
        if (arrowNode.getHashSpacing() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.HASH_SPACING, arrowNode.getHashSpacing());
        } else {
            newArrowElement.addAttribute(ParseElementDefinition.HASH_SPACING, String.valueOf(environment.getHashSpacing()));
        }
        
        if (arrowNode.getArrowheadTail() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_TAIL, arrowNode.getArrowheadTail());
            newArrowElement.setName(ParseElementDefinition.ARROW);
        }
        
        if (arrowNode.getFillType()!= null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_FILL_TYPE, arrowNode.getFillType());
        }
        
        if (arrowNode.getArrowheadType() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_TYPE, arrowNode.getArrowheadType());
        }
        if(arrowNode.getArrowheadType() != null
                && arrowNode.getArrowheadType().equalsIgnoreCase(ParseElementDefinition.ARROW_HOLLOW)
                && graphicNode.getArrowType() != null
                && !graphicNode.getArrowType().equalsIgnoreCase(ParseElementDefinition.ARROW_TYPE_EQUILIBRIUM)){
            if(graphicNode.getArrowType() == null ||
                    graphicNode.getArrowType().equalsIgnoreCase(ParseElementDefinition.GRAPHIC_ARROW_TYPE_HOLLOW)
                    || arrowNode.getArrowShaftSpacing() != null){
                newArrowElement.setName(ParseElementDefinition.ARROW_HOLLOW);
            }
        }
        
        if(arrowNode.getLineType() != null){
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_LINE_TYPE, arrowNode.getLineType());
            if (arrowNode.getLineType().equalsIgnoreCase(ParseElementDefinition.ARROW_WAVY)) {
                newArrowElement.setName(ParseElementDefinition.ARROW_WAVY);
            }
        }
        if (arrowNode.getHeadSize() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_SIZE, arrowNode.getHeadSize());
        }
        
        if (arrowNode.getArrowheadCenterSize() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_CENTER_SIZE, arrowNode.getArrowheadCenterSize());
        }
        
        if (arrowNode.getArrowheadWidth() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH, arrowNode.getArrowheadWidth());
        }
        
        if (arrowNode.getHead3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_HEAD_3D, arrowNode.getHead3D());
        }
        
        if (arrowNode.getTail3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_TAIL_3D, arrowNode.getTail3D());
        }
        
        if (arrowNode.getCenter3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_CENTER_3D, arrowNode.getCenter3D());
        }
        
        if (arrowNode.getMajorAxisEnd3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_MAJOR_AXIS_END_3D, arrowNode.getMajorAxisEnd3D());
        }
        
        if (arrowNode.getMinorAxisEnd3D() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_MINOR_AXIS_END_3D, arrowNode.getMinorAxisEnd3D());
        }
        
        if (arrowNode.getLineWidth() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH, arrowNode.getLineWidth());
        } else {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH, String.valueOf(environment.getLineWidth()));
        }
        
        if (arrowNode.getBoldWidth() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH, arrowNode.getBoldWidth());
        } else {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH, String.valueOf(environment.getBoldWidth()));
        }
        
        if (arrowNode.getAngularSize() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE, arrowNode.getAngularSize());
        }
        if (arrowNode.getArrowShaftSpacing() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING, arrowNode.getArrowShaftSpacing());
        }
        
        if (arrowNode.getArrowEquilibriumRatio() != null){
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_EQUILIBRIUM_RATIO, arrowNode.getArrowEquilibriumRatio());
        }
        
        if (arrowNode.getNoGo() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_NO_GO, arrowNode.getNoGo());
        }
        
        if (arrowNode.getDipole() != null) {
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_DIPOLE, arrowNode.getDipole());
        }
        
        if(arrowNode.getFadePercent() != null){
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_FADE_PERCENT, arrowNode.getFadePercent());
        }
        
        if(graphicNode != null && graphicNode.getArrowType() != null){
            newArrowElement.addAttribute(ParseElementDefinition.ARROW_TYPE, graphicNode.getArrowType());
        }
        
        return newArrowElement;
    }
    
    private ParsedElement parseTable(Table table, ParsedPage currentPage){
        ParsedElement newTableElement = new ParsedElement();
        
        newTableElement.setEnvironment(environment);
        
        newTableElement.setId(table.getId());
        newTableElement.setName(ParseElementDefinition.TABLE);
        
        int tableZOrder = 0;
        
        newTableElement.addElement(parseColor(table.getFgRGBA(), table.getColor()));
        
        if(table.getBoldWidth() != null){
            newTableElement.addAttribute(ParseElementDefinition.TABLE_BOLD_WIDTH, table.getBoldWidth());
        }
        
        if(table.getLineWidth() != null){
            newTableElement.addAttribute(ParseElementDefinition.TABLE_LINE_WIDTH, table.getLineWidth());
        }
        
        if(table.getZ() != null){
            newTableElement.setZOrder(Integer.parseInt(table.getZ()));
            tableZOrder = newTableElement.getZOrder();
        }
        
        int lastZOrder = zOrder;
        zOrder += tableZOrder;
        
        for(Object possiblePage : table.getPageOrObjecttagOrAnnotation()){
            if(possiblePage instanceof Page){
                Page tablePage = (Page) possiblePage;
                
                ParsedElement elementPage = new ParsedElement();
                
                elementPage.setId(tablePage.getId());
                elementPage.setName(ParseElementDefinition.TABLE_PAGE);
                
                ParsedPage tablePageElement = parsePage(tablePage, elementPage);
                tablePageElement.setZOrder(zOrder + tablePageElement.getZOrder() + 1);
                currentPage.addElement(tablePageElement);
                
                newTableElement.addElement(elementPage);
            } else if(possiblePage instanceof Objecttag){
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) possiblePage, newTableElement);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    currentPage.addElement(setPageProperties(objectTagElement));
                }
            }
        }
        
        zOrder = lastZOrder;
        
        return newTableElement;
    }
    
    private List<ParsedElement> parsePlasmidmap(Plasmidmap plasmidmapNode, ParsedPage currentPage){
        List<ParsedElement> plasmidmapElements = new ArrayList();
        
        ParsedElement newPlasmidmapElement = new ParsedElement();
        
        newPlasmidmapElement.setName(ParseElementDefinition.PLASMID_MAP);
        
        newPlasmidmapElement.setEnvironment(environment);
        
        newPlasmidmapElement.setId(plasmidmapNode.getId());
        
        newPlasmidmapElement.addElement(parseColor(plasmidmapNode.getFgRGBA(), plasmidmapNode.getColor()));
        
        newPlasmidmapElement.setZOrder(Integer.parseInt(plasmidmapNode.getZ()));
        
        if(plasmidmapNode.getBoundingBox() != null){
            newPlasmidmapElement.addAttribute(ParseElementDefinition.BOUNDING_BOX,
                    plasmidmapNode.getBoundingBox());
        }
        
        newPlasmidmapElement.addAttribute(ParseElementDefinition.PLASMID_MAP_P, plasmidmapNode.getP());
        
        if (plasmidmapNode.getLineWidth() != null) {
            newPlasmidmapElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH,
                    plasmidmapNode.getLineWidth());
        } else {
            newPlasmidmapElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH,
                    String.valueOf(environment.getLineWidth()));
        }
        
        if (plasmidmapNode.getBoldWidth() != null) {
            newPlasmidmapElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH,
                    plasmidmapNode.getBoldWidth());
        } else {
            newPlasmidmapElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH,
                    String.valueOf(environment.getBoldWidth()));
        }
        
        newPlasmidmapElement.addAttribute(ParseElementDefinition.PLASMID_MAP_NUMBER_BASE_PAIRS,
                plasmidmapNode.getNumberBasePairs());
        
        newPlasmidmapElement.addAttribute(ParseElementDefinition.PLASMID_MAP_RING_RADIUS,
                plasmidmapNode.getRingRadius());
        
        plasmidmapElements.add(newPlasmidmapElement);
        
        for (Object innerNode : plasmidmapNode.getObjecttagOrAnnotationOrPlasmidregion()){
            
            if(innerNode instanceof T){
                
                if(!((T)innerNode).getVisible().equals(NOT_VISIBLE)){
                    List<ParsedElement> textElements = parseText((T) innerNode, plasmidmapNode.getZ());
                    for(ParsedElement textElement : textElements){
                        plasmidmapElements.add(textElement);
                    }
                }
            }
            if(innerNode instanceof Graphic){
                Graphic graphic = (Graphic) innerNode;
                graphic.setId(GenericId.getInstance().getId());
                graphic.setZ(plasmidmapNode.getZ());
                
                try {
                    plasmidmapElements.add(parseGraphic(graphic, currentPage));
                } catch (ElementNotSupportedException ex) {
                    ex.printStackTrace();
                }
            }
            if(innerNode instanceof Plasmidmarker){
                Plasmidmarker plasmidMarker = (Plasmidmarker) innerNode;
                
                for(Object innerMarker : plasmidMarker.getObjecttagOrAnnotationOrT()){
                    if(innerMarker instanceof T){
                        T textNode = (T) innerMarker;
                        textNode.setZ(plasmidmapNode.getZ());
                        
                        List<ParsedElement> textElements = parseText(textNode, plasmidmapNode.getZ());
                        
                        for(ParsedElement textElement : textElements){
                            plasmidmapElements.add(textElement);
                        }
                    }if(innerMarker instanceof Curve){
                        Curve curve = (Curve) innerMarker;
                        curve.setZ(plasmidmapNode.getZ());
                        curve.setId(GenericId.getInstance().getId());
                        plasmidmapElements.add(parseCurve(curve));
                    }
                }
            }
            if(innerNode instanceof Plasmidregion){
                Plasmidregion plasmidRegion = (Plasmidregion) innerNode;
                plasmidRegion.setId(GenericId.getInstance().getId());
                plasmidmapElements.add(parsePlasmidRegion(plasmidRegion, plasmidmapNode.getZ()));
            }
        }
        
        return plasmidmapElements;
    }
    
    private ParsedElement parsePlasmidRegion(Plasmidregion plasmidRegionNode, String zOrder){
        ParsedElement newPlasmidRegionElement = new ParsedElement();
        
        newPlasmidRegionElement.setName(ParseElementDefinition.PLASMID_REGION);
        
        newPlasmidRegionElement.setEnvironment(environment);
        
        newPlasmidRegionElement.setId(plasmidRegionNode.getId());
        
        newPlasmidRegionElement.setZOrder(Integer.parseInt(zOrder));
        
        if(plasmidRegionNode.getBoundingBox() != null){
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.BOUNDING_BOX, plasmidRegionNode.getBoundingBox());
        }
        
        newPlasmidRegionElement.addElement(parseColor(plasmidRegionNode.getFgRGBA(), plasmidRegionNode.getColor()));
        
        newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_LINE_WIDTH, String.valueOf(environment.getLineWidth()));
        
        newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_BOLD_WIDTH, String.valueOf(environment.getBoldWidth()));
        
        newPlasmidRegionElement.addAttribute(ParseElementDefinition.PLASMID_REGION_END, plasmidRegionNode.getRegionEnd());
        
        newPlasmidRegionElement.addAttribute(ParseElementDefinition.PLASMID_REGION_START, plasmidRegionNode.getRegionStart());
        
        newPlasmidRegionElement.addAttribute(ParseElementDefinition.PLASMID_REGION_OFFSET, plasmidRegionNode.getRegionOffset());
        
        newPlasmidRegionElement.addAttribute(ParseElementDefinition.SPLINE_FILL_TYPE, plasmidRegionNode.getFillType());
        
        if (plasmidRegionNode.getLineType() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.SPLINE_LINE_TYPE, plasmidRegionNode.getLineType());
        }
        
        if (plasmidRegionNode.getHeadSize() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_HEAD_SIZE, plasmidRegionNode.getHeadSize());
        }
        
        if (plasmidRegionNode.getArrowheadCenterSize() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_HEAD_CENTER_SIZE, plasmidRegionNode.getArrowheadCenterSize());
        }
        
        if (plasmidRegionNode.getArrowheadWidth() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_HEAD_WIDTH, plasmidRegionNode.getArrowheadWidth());
        }
        
        if (plasmidRegionNode.getHead3D() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_HEAD_3D, plasmidRegionNode.getHead3D());
        }
        
        if (plasmidRegionNode.getTail3D() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_TAIL_3D, plasmidRegionNode.getTail3D());
        }
        
        if (plasmidRegionNode.getCenter3D() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_CENTER_3D, plasmidRegionNode.getCenter3D());
        }
        
        if (plasmidRegionNode.getMajorAxisEnd3D() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_MAJOR_AXIS_END_3D, plasmidRegionNode.getMajorAxisEnd3D());
        }
        
        if (plasmidRegionNode.getMinorAxisEnd3D() != null) {
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_MINOR_AXIS_END_3D, plasmidRegionNode.getMinorAxisEnd3D());
        }
        
        if(plasmidRegionNode.getFadePercent() != null){
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.SPLINE_FADE_PERCENT, plasmidRegionNode.getFadePercent());
        }
        
        if(plasmidRegionNode.getArrowShaftSpacing() != null){
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_SHAFT_SPACING, plasmidRegionNode.getArrowShaftSpacing());
        }
        
        if(plasmidRegionNode.getAngularSize() != null){
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.ARROW_ANGULAR_SIZE, plasmidRegionNode.getAngularSize());
        }
        
        if(plasmidRegionNode.getArrowheadHead() != null){
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_HEAD, plasmidRegionNode.getArrowheadHead());
        }
        
        if(plasmidRegionNode.getArrowheadTail() != null){
            newPlasmidRegionElement.addAttribute(ParseElementDefinition.SPLINE_ARROW_HEAD_TAIL, plasmidRegionNode.getArrowheadTail());
        }
        
        return newPlasmidRegionElement;
    }
    
    /* If the bounding box wasn't originated at (0,0) this method calculate the new
     * bounding box of the page using typical dimensions (540x720, i.e. 7.5x10 inches).
     * In case that the bounding box was originated at (0,0) this method does not do
     * nothing.
     */
    private void updatePageBoundingBox(Page page){
        
        String[] boundingBoxCoordinates = null;
        
        if(page.getBoundingBox() != null){
            boundingBoxCoordinates = page.getBoundingBox().split(" ");
        }
        
        if( (page.getBoundingBox() == null)
        || (( page.getBoundingBox() != null && Double.parseDouble(boundingBoxCoordinates[0]) != 0)
        && (Double.parseDouble(boundingBoxCoordinates[1]) != 0))){
            
            // Typical dimensions are 540x720, i.e. 7.5x10 inches, given 8.5x11" paper and
            // margins of 0.5 inches.  The Page Rect originates at (0,0).
            double bottom = DEFAULT_BOTTOM;
            double right = DEFAULT_RIGHT;
            
            double heightPages = Double.parseDouble(page.getHeightPages());
            double widthPages = Double.parseDouble(page.getWidthPages());
            
            bottom *= heightPages;
            right *= widthPages;
            
            if(page.getPageOverlap() != null){
                double pageOverlap = Double.parseDouble(page.getPageOverlap());
                bottom -= (heightPages - 1) * pageOverlap;
                right -= (widthPages - 1) * pageOverlap;
            }
            
            String newBoundingBox ="0 0 "+bottom+" "+right;
            
            page.setBoundingBox(newBoundingBox);
            
        }
        
    }
    
    private ParsedPage parsePage(Page page){
        
        updatePageBoundingBox(page);
        
        return parsePage(page, null);
    }
    
    private ParsedPage parsePage(Page page, ParsedElement superElement){
        ParsedPage newPageElement = new ParsedPage(pageNumber++);
        
        newPageElement.setId(page.getId());
        
        double doubleXOffset = Double.parseDouble(xOffset);
        double doubleYOffset = Double.parseDouble(yOffset);
        
        String lastXOffset = xOffset;
        String lastYOffset = yOffset;
        
        double OffsetStrokeWidthBottom = environment.getLineWidth()/2.0;
        double OffsetStrokeWidthTop = environment.getLineWidth()/2.0;
        double OffsetStrokeWidthLeft = environment.getLineWidth()/2.0;
        double OffsetStrokeWidthRight = environment.getLineWidth()/2.0;
        
        if(page.getBoundingBox() != null){
            for(Object possibleBorder : page.getTOrFragmentOrGroup()){
                if(possibleBorder instanceof Border){
                    Border border = (Border)possibleBorder;
                    
                    if (border.getSide().equals("left")){
                        OffsetStrokeWidthLeft = Double.valueOf(border.getLineWidth())/2;
                    }else if(border.getSide().equals("top")){
                        OffsetStrokeWidthTop = Double.valueOf(border.getLineWidth())/2;
                    }else if(border.getSide().equals("bottom")){
                        OffsetStrokeWidthBottom = Double.valueOf(border.getLineWidth())/2;
                    }else if(border.getSide().equals("right")){
                        OffsetStrokeWidthRight = Double.valueOf(border.getLineWidth())/2;
                    }
                }
            }
            
            String[] boundingBoxCoordinates =
                    page.getBoundingBox().split(" ");
            
            newPageElement.setX1(Double.parseDouble(boundingBoxCoordinates[0]) + doubleXOffset + OffsetStrokeWidthLeft);
            newPageElement.setY1(Double.parseDouble(boundingBoxCoordinates[1]) + doubleYOffset + OffsetStrokeWidthTop);
            newPageElement.setX2(Double.parseDouble(boundingBoxCoordinates[2]) + doubleXOffset - OffsetStrokeWidthRight);
            newPageElement.setY2(Double.parseDouble(boundingBoxCoordinates[3]) + doubleYOffset - OffsetStrokeWidthBottom);
            
            superElement = setPageProperties(superElement);
        }
        
        if(page.getBoundsInParent() != null){
            String bounds = page.getBoundsInParent();
            
            String[] coordinates = bounds.split(" ");
            xOffset = Double.toString(Double.parseDouble(coordinates[0]) + doubleXOffset);
            yOffset = Double.toString(Double.parseDouble(coordinates[1]) + doubleYOffset);
            
            if(superElement != null){
                superElement.addAttribute(ParseElementDefinition.TABLE_PAGE_BOUNDS_IN_PARENT, bounds);
            }
        }
        
        for(Object possibleFragment : page.getTOrFragmentOrGroup()){
            ParsedElement currentElement = null;
            
            if(possibleFragment instanceof Border){
                if(superElement != null){
                    superElement.addElement(parseBorder((Border) possibleFragment));
                }
            } else if(possibleFragment instanceof Fragment){
                parseFragment((Fragment) possibleFragment, newPageElement);
            } else if (possibleFragment instanceof Group) {
                parseGroup((Group) possibleFragment, newPageElement);
            } else if(possibleFragment instanceof Tlcplate){
                currentElement =
                        setPageProperties(parseTLCPlate((Tlcplate) possibleFragment, newPageElement));
            } else if (possibleFragment instanceof T) {
                List<ParsedElement> textElements = parseText((T) possibleFragment, null);
                
                for(ParsedElement textElement : textElements){
                    currentElement = setPageProperties(textElement);
                }
            } else if (possibleFragment instanceof Rlogic){
                currentElement =
                        setPageProperties(parseRLogic((Rlogic) possibleFragment));
            } else if (possibleFragment instanceof Table){
                currentElement =
                        setPageProperties(parseTable((Table) possibleFragment, newPageElement));
            } else if(possibleFragment instanceof Curve){
                currentElement =
                        setPageProperties(parseCurve((Curve) possibleFragment));
            } else if (possibleFragment instanceof Arrow) {
                currentElement =
                        setPageProperties(parseArrow((Arrow) possibleFragment));
            } else if(possibleFragment instanceof Graphic) {
                Graphic graphic = (Graphic) possibleFragment;
                Object currentNode = possibleFragment;
                
                if(graphic.getSupersededBy() != null){
                    String supersededBy = graphic.getSupersededBy();
                    graphics.put(supersededBy, graphic);
                    
                    if(!graphicHasSupersededByArrow(graphic, page)){
                        graphic.setSupersededBy(null);
                    }
                }else{
                    
                    try {
                        //Parse graphic tag.
                        ParsedElement graphicElement = parseGraphic((Graphic) possibleFragment, newPageElement);
                        newPageElement.addElement(setPageProperties(graphicElement));
                        
                    } catch (ElementNotSupportedException ex) {
                        // ignore not supported elements
                    }
                }
                
                currentNode = graphic.getObjecttagOrAnnotationOrRepresent();
            } else if(possibleFragment instanceof Embeddedobject) {
                Embeddedobject embeddedObject = (Embeddedobject)possibleFragment;
                
                newPageElement.addElement(setPageProperties(parseEmbeddedObject(
                        embeddedObject, embeddedObject.getId(), false)));
                
            } else if(possibleFragment instanceof Stoichiometrygrid){
                List<ParsedElement> gridElements =
                        parseStoichiometryGrid((Stoichiometrygrid) possibleFragment);
                
                for(ParsedElement gridElement : gridElements){
                    newPageElement.addElement(setPageProperties(gridElement));
                }
            } else if(possibleFragment instanceof Objecttag){
                //the owner doesn't has any valid information for the
                //parseObjectTag method so we send a null parameter
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) possibleFragment, null);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    newPageElement.addElement(setPageProperties(objectTagElement));
                }
            } else if(possibleFragment instanceof Spectrum){
                List<ParsedElement> spectrumTagElements =
                        parseSpectrum((Spectrum) possibleFragment);
                
                for(ParsedElement spectrumTagElement : spectrumTagElements){
                    newPageElement.addElement(setPageProperties(spectrumTagElement));
                }
            } else if(possibleFragment instanceof Altgroup){
                List<ParsedElement> groupTagElements =
                        parseAlternativeGroup((Altgroup) possibleFragment, newPageElement);
                
                for(ParsedElement groupTagElement : groupTagElements){
                    newPageElement.addElement(setPageProperties(groupTagElement));
                }
            } else if(possibleFragment instanceof Constraint){
                List<ParsedElement> constraintElements =
                        parseConstraint((Constraint) possibleFragment);
                
                for(ParsedElement constraintElement : constraintElements){
                    newPageElement.addElement(setPageProperties(constraintElement));
                }
            } else if(possibleFragment instanceof Geometry){
                List<ParsedElement> geometryElements =
                        parseGeometric((Geometry) possibleFragment);
                
                for(ParsedElement geometryElement : geometryElements){
                    newPageElement.addElement(setPageProperties(geometryElement));
                }
            } else if(possibleFragment instanceof Bioshape){
                newPageElement.addElement(setPageProperties(parseBioShape(
                        (Bioshape) possibleFragment)));
            } else if(possibleFragment instanceof Plasmidmap){
                List<ParsedElement> plasmidmapElements = parsePlasmidmap(
                        (Plasmidmap) possibleFragment, newPageElement);
                
                for(ParsedElement plasmidmapElement : plasmidmapElements){
                    newPageElement.addElement(setPageProperties(plasmidmapElement));
                }
                
            }
            
            if(currentElement != null){
                newPageElement.addElement(currentElement);
            }
        }
        
        checkUnparsedElements(newPageElement);
        
        xOffset = lastXOffset;
        yOffset = lastYOffset;
        
        return newPageElement;
    }
    
    /**
     * This method parse all the elements in the graphic map
     *  that aren't taked for any parse method before
     */
    public void checkUnparsedElements(ParsedPage currentPage){
        Collection<Graphic> unparsedGraphics = graphics.values();
        
        try {
            for(Graphic graphic : unparsedGraphics){
                if(graphic.getSupersededBy() == null){
                    ParsedElement graphicElement = parseGraphic(graphic, currentPage);
                    currentPage.addElement(setPageProperties(graphicElement));
                }
            }
        } catch (ElementNotSupportedException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * This method check that the graphic node has the current arrow node
     * defined by the supersededBy attribute
     */
    private boolean graphicHasSupersededByArrow(Graphic graphic, Page page){
        boolean result = false;
        
        for (Object element : page.getTOrFragmentOrGroup()) {
            if(element instanceof Arrow){
                Arrow arrowElement = (Arrow) element;
                
                if(graphic.getSupersededBy() != null &&
                        graphic.getSupersededBy().equals(arrowElement.getId())){
                    
                    result = true;
                }
            }
        }
        
        return result;
    }
    
    private <P extends ParsedObject> P setPageProperties(P element){
        if(element != null){
            element.addAttribute(ParseElementDefinition.PAGE_X_OFFSET, xOffset);
            element.addAttribute(ParseElementDefinition.PAGE_Y_OFFSET, yOffset);
            
            element.setZOrder(element.getZOrder() + zOrder);
            
            return element;
        } else {
            return null;
        }
    }
    
    private N setPageOffset(N node){
        if(node != null){
            Point nodePosition = CDXMLProcessor.parseCoords(node.getP(), null);
            nodePosition.setX(nodePosition.getX() + Double.parseDouble(xOffset));
            nodePosition.setY(nodePosition.getY() + Double.parseDouble(yOffset));
            node.setP(nodePosition.getX() + " " + nodePosition.getY());
            
            return node;
        } else {
            return null;
        }
    }
    
    private ParsedElement parseBorder(Border border){
        ParsedElement newParsedElement = new ParsedElement();
        
        newParsedElement.setId(border.getId());
        newParsedElement.setName(ParseElementDefinition.TABLE_PAGE_BORDER);
        
        newParsedElement.addAttribute(
                ParseElementDefinition.TABLE_PAGE_BORDER_SIDE,
                border.getSide());
        
        if(border.getLineType() != null){
            newParsedElement.addAttribute(
                    ParseElementDefinition.TABLE_PAGE_BORDER_LINE_TYPE,
                    border.getLineType());
        }
        
        if(border.getLineWidth() != null){
            newParsedElement.addAttribute(
                    ParseElementDefinition.TABLE_PAGE_BORDER_LINE_WIDTH,
                    border.getLineWidth());
        }
        
        newParsedElement.addElement(parseColor(border.getFgRGBA(), border.getColor()));
        
        return newParsedElement;
    }
    
    private List<ParsedElement> parseStoichiometryGrid(Stoichiometrygrid grid){
        List<ParsedElement> result = new ArrayList();
        
        ParsedElement gridElement = new ParsedElement();
        
        gridElement.setEnvironment(environment);
        
        gridElement.setId(grid.getId());
        gridElement.setName(ParseElementDefinition.STOICHIOMETRY_GRID);
        
        gridElement.addElement(parseColor(grid.getFgRGBA(), grid.getColor()));
        
        gridElement.setZOrder(Integer.parseInt(grid.getZ()));
        gridElement.addAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_POSITION, grid.getP());
        gridElement.addAttribute(ParseElementDefinition.STOICHIOMETRY_GRID_PAGE_WIDTH, documentElement.getAttribute(ParseElementDefinition.BOUNDING_BOX));
        
        int numberOfDigits = String.valueOf(grid.getObjecttagOrAnnotationOrSgcomponent().size()).length();
        //Search all the SGComponent attributes
        for(Object possibleSgComponent : grid.getObjecttagOrAnnotationOrSgcomponent()){
            if(possibleSgComponent instanceof Sgcomponent){
                
                Sgcomponent component = (Sgcomponent) possibleSgComponent;
                
                //Add the attributes only if the SGComponent is visible
                if(!component.getVisible().equals(
                        ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_VISIBLE_FALSE)){
                    
                    ParsedElement componentElement = new ParsedElement();
                    componentElement.setId(zeroPadString(component.getId(), numberOfDigits));
                    componentElement.setName(ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT);
                    
                    componentElement.addAttribute(
                            ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_WIDTH, component.getWidth());
                    componentElement.addAttribute(
                            ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_IS_HEADER, component.getComponentIsHeader());
                    
                    if(component.getComponentIsReactant() != null){
                        componentElement.addAttribute(
                                ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_IS_REACTAN, component.getComponentIsReactant());
                    }
                    
                    //Search all the SGdatum attributes
                    for(Object possibleSgDatum : component.getObjecttagOrSgdatum()){
                        if(possibleSgDatum instanceof Sgdatum){
                            Sgdatum datum = (Sgdatum) possibleSgDatum;
                            
                            //Add the items only if the SGDatum is visible
                            if (datum.getVisible().equals(ParseElementDefinition.STOICHIOMETRY_GRID_DATUM_VISIBLE_TRUE)){
                                for(Object object : datum.getObjecttagOrEmbeddedobject()){
                                    if(object instanceof Objecttag){
                                        Objecttag objectTag = (Objecttag) object;
                                        for(T text : objectTag.getT()){
                                            
                                            ParsedElement textElement = new ParsedElement();
                                            
                                            textElement.setId(text.getId());
                                            textElement.setId(zeroPadString(text.getId(), SGDATUM_NUMBER_OF_DIGITS));
                                            textElement.setName(ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT);
                                            
                                            textElement.addAttribute(
                                                    ParseElementDefinition.STOICHIOMETRY_GRID_TEXT_COMPONENT_BOUNDING_BOX,
                                                    text.getBoundingBox());
                                            
                                            componentElement.addElement(textElement);
                                            
                                            //Add the text element to represent the
                                            //text only if the SGDatum is visible
                                            if(!text.getVisible().equals(
                                                    ParseElementDefinition.STOICHIOMETRY_GRID_COMPONENT_VISIBLE_FALSE)){
                                                result.addAll(parseText(text, grid.getZ()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    gridElement.addElement(componentElement);
                    
                }
            }
        }
        
        result.add(gridElement);
        
        return result;
    }
    
    /**
     *Parse all the ObjectTag attributes
     */
    private List<ParsedElement> parseObjectTag(Objecttag objectTag, ParsedElement owner){
        List<ParsedElement> result = new ArrayList();
        List<ParsedElement> texts = new ArrayList();
        ParsedElement newObjectTagElement = new ParsedElement();
        objectTagID++;
        newObjectTagElement.setId(ParseElementDefinition.OBJECT_TAG + Integer.toString(objectTagID));
        newObjectTagElement.setEnvironment(environment);
        newObjectTagElement.setName(ParseElementDefinition.OBJECT_TAG);
        
        if (owner != null){
            newObjectTagElement.setOwner(owner);
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_OWNER_ID, owner.getId());
        }
        if (objectTag.getDisplayName() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_DISPLAY_NAME, objectTag.getDisplayName());
        }
        if (objectTag.getName() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_NAME, objectTag.getName());
        }
        if (objectTag.getPersistent() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_PERSISTENT, objectTag.getPersistent());
        }
        if (objectTag.getPositioningAngle() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_POSITIONING_ANGLE, objectTag.getPositioningAngle());
        }
        if (objectTag.getPositioningOffset() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_POSITIONING_OFFSET, objectTag.getPositioningOffset());
        }
        if (objectTag.getPositioningType() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_POSITIONING_TYPE, objectTag.getPositioningType());
        }
        if (objectTag.getTagType() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_TAG_TYPE, objectTag.getTagType());
        }
        
        if (objectTag.getTracking() != null){
            newObjectTagElement.addAttribute(ParseElementDefinition.OBJECT_TAG_TRACKING, objectTag.getTracking());
        }
        if (objectTag.getValue() != null){
            newObjectTagElement.setValue(objectTag.getValue());
        }
        
        boolean haveTextValue = false;
        
        if (objectTag.getT() != null){
            for (T text : objectTag.getT()){
                for(Object posibleText : text.getSOrObjecttagOrAnnotation()){
                    if(posibleText instanceof S && ((S)posibleText).getContent() != null && !((S)posibleText).getContent().equals("")){
                        haveTextValue = true;
                    }
                }
                //This is for evite that objecTags text with empty value be
                //processed by text processor
                if(haveTextValue){
                    if (owner != null){
                        texts.addAll(parseText(text, Integer.toString(owner.getZOrder())));
                    } else{
                        texts.addAll(parseText(text, null));
                        haveTextValue = false;
                    }
                }
            }
        }
        newObjectTagElement.addAllElements(texts);
        result.add(newObjectTagElement);
        return result;
    }
    
    /**
     * Iterates all the basis object that the object be parte
     * and calculate the upper zOrder of all of them
     */
    private int getZOrderFromBasisObjects(String basisObjects, int elementZorder){
        int zOrder = 0;
        String[] arrayOfBasisObject = basisObjects.trim().split(" ");
        
        for(int i = 0; i < arrayOfBasisObject.length ; i++){
            String bondId = arrayOfBasisObject[i];
            int actualZOrder = 0;
            
            List<ParsedElement> bondsElement = environment.getJoinedBonds(bondId);
            
            if(bondsElement != null){
                for(int j = 0; j < bondsElement.size(); j++){
                    actualZOrder = environment.getJoinedBonds(bondId).get(j).getZOrder();
                    
                    if(actualZOrder > zOrder){
                        zOrder = actualZOrder;
                    }
                }
            }
        }
        
        zOrder = (zOrder > elementZorder ? zOrder : elementZorder);
        
        return zOrder;
    }
    
    /**
     *Parse all the SpectrumTag attributes
     */
    private List<ParsedElement> parseSpectrum(Spectrum spectrumTag){
        List<ParsedElement> result = new ArrayList();
        ParsedElement spectrumElement = new ParsedElement();
        
        spectrumElement.setEnvironment(environment);
        
        spectrumElement.setId(spectrumTag.getId());
        spectrumElement.setName(ParseElementDefinition.SPECTRUM);
        if(spectrumTag.getZ() != null){
            spectrumElement.setZOrder(Integer.parseInt(spectrumTag.getZ()));
        }
        if(spectrumTag.getXSpacing()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_X_SPACING, spectrumTag.getXSpacing());
        }
        if(spectrumTag.getXLow()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_X_LOW, spectrumTag.getXLow());
        }
        if(spectrumTag.getBoundingBox()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_BOUNDING_BOX, spectrumTag.getBoundingBox());
        }
        if(spectrumTag.getXAxisLabel()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_X_AXIS_LABEL, spectrumTag.getXAxisLabel());
        }
        if(spectrumTag.getYAxisLabel()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_Y_AXIS_LABEL, spectrumTag.getYAxisLabel());
        }
        if(spectrumTag.getXType()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_X_TYPE, spectrumTag.getXType());
        }
        if(spectrumTag.getYType()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_Y_TYPE, spectrumTag.getYType());
        }
        if(spectrumTag.getClazz()!= null){
            spectrumElement.addAttribute(ParseElementDefinition.SPECTRUM_CLASS, spectrumTag.getClazz());
        }
        
        spectrumElement.addElement(parseColor(spectrumTag.getFgRGBA(), spectrumTag.getColor()));
        
        StringBuilder contentValue = new StringBuilder();
        for(Object content : spectrumTag.getContent()){
            if(content instanceof String){
                contentValue.append(content);
            } else if(content instanceof Objecttag){
                //the owner doesn't has any valid information for the
                //parseObjectTag method so we send a null parameter
                List<ParsedElement> objectTagElements =
                        parseObjectTag((Objecttag) content, spectrumElement);
                
                for(ParsedElement objectTagElement : objectTagElements){
                    result.add(objectTagElement);
                }
            }
        }
        
        spectrumElement.setValue(contentValue.toString());
        result.add(spectrumElement);
        
        return result;
    }
    
    /**
     *Parse all the alternative group tag attributes
     */
    private List<ParsedElement> parseAlternativeGroup(Altgroup group, ParsedPage currentPage){
        List<ParsedElement> result = new ArrayList();
        ParsedElement groupElement = new ParsedElement();
        
        groupElement.setEnvironment(environment);
        
        groupElement.setName(ParseElementDefinition.ALTERNATIVE_GROUP);
        groupElement.setId(ParseElementDefinition.ALTERNATIVE_GROUP + group.getId());
        
        groupElement.addAttribute(
                ParseElementDefinition.ALTERNATIVE_GROUP_GROUP_FRAME,
                group.getGroupFrame());
        groupElement.addAttribute(
                ParseElementDefinition.ALTERNATIVE_GROUP_TEXT_FRAME,
                group.getTextFrame());
        
        if(group.getZ() != null){
            groupElement.setZOrder(Integer.parseInt(group.getZ()));
        }
        
        for(Object possibleText : group.getObjecttagOrAnnotationOrT()){
            if(possibleText instanceof T){
                List<ParsedElement> textElements = parseText((T) possibleText, group.getZ());
                groupElement.addElement(
                        textElements.get(0).getElements(ParseElementDefinition.COLOR).get(0));
                
                if(group.getZ() == null){
                    groupElement.setZOrder(textElements.get(0).getZOrder());
                }
                
                result.addAll(textElements);
            } else if (possibleText instanceof Fragment) {
                parseFragment((Fragment)possibleText, currentPage);
            }
        }
        
        result.add(groupElement);
        
        return result;
    }
    
    /**
     *Parse all the constraint tag attributes
     */
    public List<ParsedElement> parseConstraint(Constraint constraint){
        List<ParsedElement> result = new ArrayList();
        ParsedElement constraintElement = new ParsedElement();
        
        constraintElement.setEnvironment(environment);
        
        constraintElement.setId(constraint.getId());
        constraintElement.setName(ParseElementDefinition.CONSTRAINT);
        
        int constraintZOrder = Integer.parseInt(constraint.getZ());
        
        if(constraint.getBasisObjects() != null){
            constraintZOrder = getZOrderFromBasisObjects(constraint.getBasisObjects(), constraintZOrder);
        }
        
        constraintElement.setZOrder(constraintZOrder);
        
        constraintElement.addAttribute(ParseElementDefinition.CONSTRAINT_TYPE,
                constraint.getConstraintType());
        constraintElement.addAttribute(ParseElementDefinition.CONSTRAINT_MIN,
                constraint.getConstraintMin());
        constraintElement.addAttribute(ParseElementDefinition.CONSTRAINT_MAX,
                constraint.getConstraintMax());
        
        constraintElement.addAttribute(ParseElementDefinition.CONSTRAINT_BASIS_OBJECT,
                constraint.getBasisObjects());
        if(constraint.getHashSpacing() != null){
            constraintElement.addAttribute(ParseElementDefinition.HASH_SPACING,
                    constraint.getHashSpacing());
        }
        if(constraint.getLineWidth() != null){
            constraintElement.addAttribute(ParseElementDefinition.LINE_WIDTH,constraint.getLineWidth());
        }
        
        constraintElement.addElement(parseColor(constraint.getFgRGBA(), constraint.getColor()));
        
        for(Object possibleObjectTag : constraint.getObjecttagOrAnnotation()){
            if(possibleObjectTag instanceof Objecttag){
                List<ParsedElement> objectTagElements = parseObjectTag(
                        (Objecttag) possibleObjectTag, constraintElement);
                
                result.addAll(objectTagElements);
            }
        }
        
        result.add(constraintElement);
        
        return result;
    }
    
    /**
     *Parse all the Geometry tag attributes
     */
    public List<ParsedElement> parseGeometric(Geometry geometry){
        List<ParsedElement> result = new ArrayList();
        ParsedElement geometryElement = new ParsedElement();
        
        geometryElement.setEnvironment(environment);
        
        geometryElement.setName(ParseElementDefinition.GEOMETRY);
        geometryElement.setId(geometry.getId());
        
        if(geometry.getLineWidth() != null){
            geometryElement.addAttribute(ParseElementDefinition.GRAPHIC_LINE_WIDTH, geometry.getLineWidth());
        }
        
        geometryElement.addAttribute(
                ParseElementDefinition.GEOMETRY_TYPE, geometry.getGeometricFeature());
        
        int geometryZOrder = Integer.parseInt(geometry.getZ());;
        
        if(geometry.getBasisObjects() != null){
            geometryZOrder = getZOrderFromBasisObjects(geometry.getBasisObjects(), geometryZOrder);
        }
        
        geometryElement.setZOrder(geometryZOrder);
        
        geometryElement.addAttribute(ParseElementDefinition.GEOMETRY_BASIS_OBJECT,
                geometry.getBasisObjects());
        
        if(geometry.getRelationValue() != null){
            geometryElement.addAttribute(ParseElementDefinition.GEOMETRY_RELATION_VALUE,
                    geometry.getRelationValue());
        }
        
        if (geometry.getBoundingBox() != null) {
            geometryElement.addAttribute(ParseElementDefinition.BOUNDING_BOX, geometry.getBoundingBox());
        }
        
        // add geometries because they might reference each other
        environment.addGeometricPlaneObject(geometry.getId(), geometryElement);
        
        geometryElement.addElement(parseColor(geometry.getFgRGBA(), geometry.getColor()));
        
        for(Object possibleObjectTag : geometry.getObjecttagOrAnnotation()){
            if(possibleObjectTag instanceof Objecttag){
                List<ParsedElement> objectTagElements = parseObjectTag(
                        (Objecttag) possibleObjectTag, geometryElement);
                
                result.addAll(objectTagElements);
            }
        }
        
        result.add(geometryElement);
        
        return result;
    }
    
    public ParsedElement parseBioShape(Bioshape bioShape){
        ParsedElement bioShapeElement = new ParsedElement();
        
        bioShapeElement.setEnvironment(environment);
        
        bioShapeElement.setId(bioShape.getId());
        
        if(bioShape.getBioShapeType().equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_LINE) ||
                bioShape.getBioShapeType().equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ARC) ||
                bioShape.getBioShapeType().equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ELLIPSE) ||
                bioShape.getBioShapeType().equals(ParseElementDefinition.BIO_SHAPE_MEMBRANE_MICELLE)){
            bioShapeElement.setName(ParseElementDefinition.BIO_SHAPE_ORNAMENTED_BIO_SHAPE);
        } else {
            bioShapeElement.setName(bioShape.getBioShapeType());
        }
        
        if(bioShape.getXyz() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_POSITION, bioShape.getXyz());
        }
        
        if(bioShape.getBioShapeType() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_TYPE, bioShape.getBioShapeType());
        }
        
        if(bioShape.getMajorAxisEnd3D() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_MAJOR_AXIS_END_3D, bioShape.getMajorAxisEnd3D());
        }
        
        if(bioShape.getMinorAxisEnd3D() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_MINOR_AXIS_END_3D, bioShape.getMinorAxisEnd3D());
        }
        
        if(bioShape.getFadePercent() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_FADE_PERCENT, bioShape.getFadePercent());
        }
        
        if(bioShape.getBoundingBox() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BOUNDING_BOX, bioShape.getBoundingBox());
        }
        
        if(bioShape.getZ() != null){
            bioShapeElement.setZOrder(Integer.parseInt(bioShape.getZ()));
        }
        
        if(bioShape.getNeckWidth() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_NECK_WIDTH, bioShape.getNeckWidth());
        }
        
        if(bioShape.getCylinderWidth() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_CYLINDER_WIDTH, bioShape.getCylinderWidth());
        }
        
        if(bioShape.getCylinderHeight() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_CYLINDER_HEIGHT, bioShape.getCylinderHeight());
        }
        
        if(bioShape.getCylinderDistance() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_CYLINDER_DISTANCE, bioShape.getCylinderDistance());
        }
        
        if(bioShape.getPipeWidth() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_PIPE_WIDTH, bioShape.getPipeWidth());
        }
        
        if(bioShape.getHelixProteinExtra() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_HELIX_PROTEIN_EXTRA, bioShape.getHelixProteinExtra());
        }
        
        if(bioShape.getFillType() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_FILL_TYPE, bioShape.getFillType());
        }
        
        if(bioShape.getLineType() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_LINE_TYPE, bioShape.getLineType());
        }
        
        if(bioShape.getLineWidth() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_LINE_WIDTH, bioShape.getLineWidth());
        }
        
        if(bioShape.getBoldWidth() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_BOLD_WIDTH, bioShape.getBoldWidth());
        }
        
        if(bioShape.getHashSpacing() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_HASH_SPACING, bioShape.getHashSpacing());
        }
        
        if(bioShape.getDNAWaveHeight() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_DNA_WAVE_HEIGHT, bioShape.getDNAWaveHeight());
        }
        
        if(bioShape.getDNAWaveLength() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_DNA_WAVE_LENGTH, bioShape.getDNAWaveLength());
        }
        
        if(bioShape.getDNAWaveOffset() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_DNA_WAVE_OFFSET, bioShape.getDNAWaveOffset());
        }
        
        if(bioShape.getDNAWaveWidth() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_DNA_WAVE_WIDTH, bioShape.getDNAWaveWidth());
        }
        
        if(bioShape.getMembraneElementSize() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_MEMBRANE_ELEMENT_SIZE, bioShape.getMembraneElementSize());
        }
        
        if(bioShape.getMembraneStartAngle() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_MEMBRANE_START_ANGLE, bioShape.getMembraneStartAngle());
        }
        
        if(bioShape.getMembraneEndAngle() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_MEMBRANE_END_ANGLE, bioShape.getMembraneEndAngle());
        }
        
        if(bioShape.getGproteinUpperHeight() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_UPPER_HEIGHT, bioShape.getGproteinUpperHeight());
        }
        
        if(bioShape.getGproteinLowerHeight() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_G_PROTEIN_LOWER_HEIGHT, bioShape.getGproteinLowerHeight());
        }
        
        if(bioShape.getEnzymeReceptorSize() != null){
            bioShapeElement.addAttribute(ParseElementDefinition.BIO_SHAPE_ENZYME_RECEPTOR_SIZE, bioShape.getEnzymeReceptorSize());
        }
        
        bioShapeElement.addElement(parseColor(bioShape.getFgRGBA(), bioShape.getColor()));
        
        int k = 10;
        for(Object possibleCurve : bioShape.getObjecttagOrAnnotationOrCurve()){
            if(possibleCurve instanceof Curve){
                Curve curve = (Curve) possibleCurve;
                
                ParsedElement curveElement = new ParsedElement();
                
                curveElement.setId(Integer.toString(k++));
                curveElement.setName(ParseElementDefinition.BIO_SHAPE_CURVE);
                
                if(curve.getLineType() != null){
                    curveElement.addAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_LINE_TYPE, curve.getLineType());
                }
                
                curveElement.addElement(parseColor(curve.getFgRGBA(), curve.getColor()));
                
                if(curve.getFillType() != null){
                    curveElement.addAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FILL_TYPE, curve.getFillType());
                }
                
                if(curve.getFadePercent() != null){
                    curveElement.addAttribute(ParseElementDefinition.BIO_SHAPE_CURVE_FADE_PERCENT, curve.getFadePercent());
                }
                
                bioShapeElement.addElement(curveElement);
            }
        }
        
        return bioShapeElement;
    }
    
    public static String zeroPadString(String keyString, int numberOfDigits) {
    	/*int zerosToAdd = numberOfDigits;
    	if(keyString != null){
    		zerosToAdd = numberOfDigits - keyString.length();
    	}*/
    	int zerosToAdd = numberOfDigits - keyString.length();
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < zerosToAdd; i++) {
            padding.append('0');
        }
        return padding.append(keyString).toString();
    }
    
    public ParsedStructure getStructure() {
        return resultingStructure;
    }
    
    public ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }
    
    public DocumentProperties getDocumentProperties() {
        return documentProperties;
    }
    
}

/**
 * This class encapsulates the id generation for those elements
 * that haven't one.
 * This implements the Singleton pattern
 */
class GenericId {
    private static GenericId instance = null;
    
    //Use it for setting the Id to the element that doesn't have one.
    //All the generated Id(s) will be negative ones
    private int id = 0;
    
    private GenericId(){
        
    }
    
    static{
        instance = new GenericId();
    }
    
    public static GenericId getInstance(){
        return instance;
    }
    
    public String getId(){
        id--;
        return String.valueOf(id);
    }
}
