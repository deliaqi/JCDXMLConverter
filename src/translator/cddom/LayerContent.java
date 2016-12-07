package translator.cddom;

public interface LayerContent extends Comparable {
    
    public Layer getLayer();
    
    public void setLayer(Layer layer);
    
    public int getZOrder();

    public void setZOrder(int zOrder);
}
