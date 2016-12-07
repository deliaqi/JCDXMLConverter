package translator.cddom;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import translator.graphics.shapes.Shape;

/**
 * Class that represents a layer of drawing.
 * The concept of layers helps separating different drawing elements based on 
 * their type and purpose. Layers can be applied to separate system information 
 * drawing from user-created shapes. This concept is not the same as the Z order
 * and is not intended to replace it. Each layer in a page would have its own Z 
 * ordering.
 */
public class Layer {
    
    private Page page;
    private SortedSet<LayerContent> contents = new TreeSet();
    
    public Layer() {
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public void addContent(LayerContent content) throws CDDOMException {
        if(content == null){
            throw new CDDOMException("The shape can't be null");
        }
        
        contents.add(content);
        content.setLayer(this);
    }
    
    public SortedSet<LayerContent> getContents() {
        return contents;
    }
    
}
