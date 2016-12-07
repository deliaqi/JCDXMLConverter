package translator.cddom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import translator.cddom.properties.DocumentProperties;
import translator.graphics.EmbeddedObject;
import translator.graphics.Image;

/**
 * Class that represents a whole document.
 * It is the root of the ChemDraw DOM’s hierarchy and contains all the
 * information about a ChemDraw document. Currently, this is limited to drawing
 * and some page layout information but it will store chemical information in
 * the future. It is intended to be the main class through which navigation of
 * the DOM tree starts.
 */
public class Document {
    
    private DocumentProperties properties;
    
    private List<Page> pages = new ArrayList();    
    
    private boolean containsShadedShapes;
    
    public Document() {
    }

    public void addPage(Page page) throws CDDOMException {
        if(page == null){
            throw new CDDOMException("The page can't be null");
        }
        
        page.setDocument(this);
        pages.add(page);
    }
    
    public List<Page> getPages() {
        return pages;
    }

    public DocumentProperties getProperties() {
        return properties;
    }

    public void setProperties(DocumentProperties properties) {
        this.properties = properties;
    }

    public boolean isContainsShadedShapes() {
        return containsShadedShapes;
    }

    public void setContainsShadedShapes(boolean containsShadedShapes) {
        this.containsShadedShapes = containsShadedShapes;
    }

}
