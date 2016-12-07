
package translator.utils;

public class Rectangle {
     
    
    private double left;
    private double right;
    private double top;
    private double bottom;
        
    /** Creates a new instance of Rectangle */
    public Rectangle() {
    }
    
    public Rectangle(double topParam, double leftParam, double bottomParam, double rightParam)
    {
        setTop(topParam);
        setLeft(leftParam);
        setBottom(bottomParam);
        setRight(rightParam);
    }
    
    public translator.utils.Rectangle inflate(double inflateFactor)
    {
        Rectangle result = null;
        left -= inflateFactor;
        top -= inflateFactor; 
        right += inflateFactor;
        bottom += inflateFactor;

        result = this;
        return result;
    }
    
    public translator.utils.Point center()
    {
        translator.utils.Point result = null;            
        result = new translator.utils.Point((this.getLeft() + this.getRight()) / 2, (this.getTop() + this.getBottom()) / 2);
        return result;
    }        
    
    public void setLeft(double left){
        this.left = left;
    }
    
    public double getLeft(){
        return this.left;
    }
    
    public void setRight(double right){
        this.right = right;
    }
    
    public double getRight(){
        return this.right;
    }
    
    public void setTop(double top){
        this.top = top;
    }
    
    public double getTop(){
        return this.top;
    }

    public void setBottom(double bottom){
        this.bottom = bottom;
    }
    
    public double getBottom(){
        return this.bottom;
    }
}
