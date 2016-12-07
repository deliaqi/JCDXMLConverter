package svgrenderer;

import com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import svgrenderer.shapes.converters.ConverterFactory;
import svgrenderer.shapes.converters.ShapeConverter;
import translator.cddom.Layer;
import translator.cddom.LayerContent;
import translator.cddom.Page;
import translator.cddom.properties.BoundingBox;
import translator.graphics.Color;
import translator.graphics.shapes.Shape;

public class SVGWriter {
    
    private Document document;
    private String data;
    
    private double viewportWidth;
    private double viewportHeight;
    
    public SVGWriter() {
    }
    
    public SVGWriter(double viewportWidth, double viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }
    
    public void createDocument(translator.cddom.Document translatorDocument){
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            document = builderFactory.newDocumentBuilder().newDocument();
            
            Element root = document.createElement("svg");
            document.appendChild(root);
            root.setAttribute("xmlns", "http://www.w3.org/2000/svg");
            root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");            
                        
            BoundingBox boundingBox = translatorDocument.getProperties().getBoundingBox();
            
            double x = boundingBox.getPosition().getX();
            double y = boundingBox.getPosition().getY();
            
            double width = boundingBox.getWidth();
            double height = boundingBox.getHeight();
            
            double widthOffset = 0;
            double heightOffset = 0;
            if (width < viewportWidth) {
                widthOffset = (viewportWidth - width) / 2;
                width += widthOffset;
                x -= widthOffset / 2;
            }
            if (height < viewportHeight) {
                heightOffset = (viewportHeight - height) / 2;
                height += heightOffset;
                y -= heightOffset / 2;
            }
            root.setAttribute("viewBox", x + " " + y + " " + width + " " + height);
            root.setAttribute("preserveAspectRatio", "xMidYMid");
            root.setAttribute("width", "100%");
            root.setAttribute("height", "100%");
            
            Element drawingGroup = document.createElement("g");
            drawingGroup.setAttribute("id", "drawing");
            
            // the drawing box is a rectangle that fills all the document space
            // with the background color
            Element drawingBox = document.createElement("rect");
            drawingBox.setAttribute("x", String.valueOf(x));
            drawingBox.setAttribute("y", String.valueOf(y));
            drawingBox.setAttribute("width", "100%");
            drawingBox.setAttribute("height", "100%");
            
            Color backgroundColor = translatorDocument.getProperties().getBackgroundColor();
            
            StringBuilder backgroundColorString = new StringBuilder();
            backgroundColorString.append("rgb(");
            backgroundColorString.append(backgroundColor.getRed());
            backgroundColorString.append(", ");
            backgroundColorString.append(backgroundColor.getGreen());
            backgroundColorString.append(", ");
            backgroundColorString.append(backgroundColor.getBlue());
            backgroundColorString.append(")");
            
            drawingBox.setAttribute("fill", backgroundColorString.toString());
            
            drawingGroup.appendChild(drawingBox);
            
            // write the pages
            for(Page page : translatorDocument.getPages()){
                
                Element pageGroup = createPageGroup(page, root);
                
                Layer layer = page.getLayers().get(0);
                
                drawingGroup.appendChild(pageGroup);
                addSubElements(layer, pageGroup, root);
            }
            
            root.appendChild(drawingGroup);
        } catch (DOMException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }
    
    private Element createPageGroup(Page page, Element root){
        Element clipElement = document.createElement("clipPath");
        clipElement.setAttribute("id", page.getId());

        Element clipRectangle = document.createElement("rect");
        clipRectangle.setAttribute("x", Double.toString(page.getBoundingBox().getPosition().getX()));
        clipRectangle.setAttribute("y", Double.toString(page.getBoundingBox().getPosition().getY()));
        clipRectangle.setAttribute("width", Double.toString(page.getBoundingBox().getWidth()));
        clipRectangle.setAttribute("height", Double.toString(page.getBoundingBox().getHeight()));

        clipElement.appendChild(clipRectangle);
        root.appendChild(clipElement);

        Element pageGroup = document.createElement("g");
        pageGroup.setAttribute("id", "page");
        pageGroup.setAttribute("clip-path", "url(#" + page.getId() + ")");
        
        return pageGroup;
    }
    
    private void addSubElements(Layer pageLayer, Element pageGroup, Element root){
        Iterator<LayerContent> it = pageLayer.getContents().iterator();
        while(it.hasNext()){
            LayerContent content = it.next();
            if(content instanceof Shape){
                Shape shape = (Shape) content;
                ShapeConverter converter = ConverterFactory.getInstance().getConverter(shape);
                converter.setDocument(document);

                pageGroup.appendChild(converter.convert());
            } else if(content instanceof Page){
                Page page = (Page) content;
                
                Element subPageGroup = createPageGroup(page, root);
                Layer subLayer = page.getLayers().get(0);
                
                pageGroup.appendChild(subPageGroup);
                addSubElements(subLayer, subPageGroup, root);
            }
        }
    }
    
    public void write(File file) throws SVGWriterException {
        try {
            FileWriter out = new FileWriter(file);
            serialize(out, true);
        } catch (IOException ex) {
            throw new SVGWriterException("Problem with the specified file: " + ex.getMessage(), ex);
        }
    }
    
    public void write() throws SVGWriterException {
        StringWriter out = new StringWriter();
        serialize(out, true);
        
        data = out.getBuffer().toString();
    }

    private void serialize(Writer out, boolean useEncoding) throws SVGWriterException {
        DOMImplementationRegistry registry;
        
        DOMImplementationSourceImpl source = new DOMImplementationSourceImpl();
        try {
            registry = DOMImplementationRegistry.newInstance();
            
            
        } catch (Exception ex) {
            throw new SVGWriterException("Problem instantiating the SVG serializer.", ex);
        }
        
        DOMImplementationLS loadSaveUtility = (DOMImplementationLS) source.getDOMImplementation("LS");
        LSSerializer serializer = loadSaveUtility.createLSSerializer();
        LSOutput xmlOut = loadSaveUtility.createLSOutput();
        
        
        
        xmlOut.setCharacterStream(out);
        if (useEncoding) {
            xmlOut.setEncoding("ISO-8859-1");
        }
        
        try {
            serializer.write(document, xmlOut);
        } catch (LSException ex) {
            throw new SVGWriterException("Could not serialize SVG document.", ex);
        }
    }
    
    public String getData() {
        return data;
    }
    
    private String getScalingScriptCode() {
        StringBuilder code = new StringBuilder();
        code.append("var g_element;\n");
        code.append("var SVGDoc;\n");
        code.append("var SVGRoot;\n");
        code.append("function RunScript(LoadEvent) {\n");
        code.append("top.SVGsetDimension = setDimension;\n");
        code.append("top.SVGsetScale = setScale;\n");
        code.append("SVGDoc	= LoadEvent.target.ownerDocument;\n");
        code.append("g_element = SVGDoc.getElementById(\"drawing\");\n");
        code.append("}\n\n");
        code.append("function setDimension(w,h) {\n");
        code.append("SVGDoc.documentElement.setAttribute(\"width\", w);\n");
        code.append("SVGDoc.documentElement.setAttribute(\"height\", h);\n");
        code.append("}\n\n");
        code.append("function setScale(sw, sh) {\n");
        code.append("g_element.setAttribute(\"transform\", \"scale(\" + sw + \" \" + sh +\")\");\n");
        code.append("}\n");
        
        return code.toString();
    }
}
