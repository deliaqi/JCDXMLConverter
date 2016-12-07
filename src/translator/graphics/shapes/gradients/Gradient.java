
package translator.graphics.shapes.gradients;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents gradients in SVG.
 */
public abstract class Gradient {
    
    protected String id;
    private List<GradientStop> stops;
    
    /**
     * Creates a gradient with the specified id.
     */
    public Gradient(String id) {
        this.id = id;
        stops = new ArrayList();
    }
    
    public String getId() {
        return id;
    }
    
    public void addStop(GradientStop stop) {
        getStops().add(stop);
    }
    
    public void removeStop(GradientStop stop) {
        getStops().remove(stop);
    }
    
    public void insertStop(GradientStop stop, int index) {
        getStops().add(index, stop);
    }

    public List<GradientStop> getStops() {
        return stops;
    }
    
}
