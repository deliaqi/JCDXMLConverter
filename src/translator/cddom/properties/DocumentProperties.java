package translator.cddom.properties;

import translator.graphics.Color;

public class DocumentProperties {
    
    private BoundingBox boundingBox;
    
    private int numberOfPages;
    
    private double lineWidth;
    private double marginWidth;
    private double boldWidth;
    private double bondSpacing;
    private double hashSpacing;
    private Color backgroundColor;
    
    public DocumentProperties() {
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getPages() {
        return numberOfPages;
    }

    public void setPages(int pages) {
        this.numberOfPages = pages;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public double getMarginWidth() {
        return marginWidth;
    }

    public void setMarginWidth(double marginWidth) {
        this.marginWidth = marginWidth;
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

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
}
