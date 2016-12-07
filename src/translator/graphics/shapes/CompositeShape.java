package translator.graphics.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import translator.Translator;
import translator.cddom.properties.BoundingBox;

public class CompositeShape <S extends Shape> extends Shape {
    
    private String id;
    private List<S> shapes = new ArrayList();
    
    public CompositeShape() {
    }

    public List<S> getShapes() {
        return Collections.unmodifiableList(shapes);
    }
    
    public void addShape(S shape){
        shapes.add(shape);
    }
    
    public void addShape(int index, S shape) {
        shapes.add(index, shape);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public BoundingBox getBoundingBox() {
        
        BoundingBox result = null;
        
        for(S currentShape : getShapes()){
            result = Translator.getUnionOfBounds(result, currentShape.getBoundingBox());         
        }
        
        return result;
        
    }
    
}
