package translator.cddom;

import java.util.ArrayList;
import java.util.List;
import translator.TranslatorException;
import translator.cddom.properties.BoundingBox;

/**
 * Class that represents a single page in a document. A document can contain
 * several pages.
 */
public class Page implements LayerContent {
    
    private String id;
    private Document containerDocument;
    private BoundingBox boundingBox;
    private Layer parentLayer;
    private List<Layer> layers = new ArrayList();
    
    private int zOrder;
    
    public Page(String id) {
        this.id = id;
    }

    public Document getDocument() {
        return containerDocument;
    }

    public void setDocument(Document document) {
        this.containerDocument = document;
    }
    
    public void addLayer(Layer layer) throws CDDOMException {
        if(layer == null){
            throw new CDDOMException("The layer can't be null");
        }
        
        layer.setPage(this);
        layers.add(layer);
    }
    
    public int compareTo(Object page) {
        if(page instanceof LayerContent){
            if((((LayerContent)page).getZOrder() - this.getZOrder()) == 0){
                return -1;
            }
            
            return -(((LayerContent)page).getZOrder() - this.getZOrder());
        }
        else {
            return Integer.MAX_VALUE;
        }
    }
    
    public List<Layer> getLayers() {
        return layers;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getId() {
        return id;
    }

    public void setLayer(Layer layer) {
        this.parentLayer = layer;
    }

    public Layer getLayer() {
        return parentLayer;
    }

    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

}
