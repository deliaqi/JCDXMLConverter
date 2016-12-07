package translator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import translator.cddom.Document;
import translator.cddom.Layer;
import translator.cddom.LayerContent;
import translator.cddom.Page;
import translator.cddom.properties.BoundingBox;
import translator.graphics.Image;
import translator.graphics.shapes.Shape;
import translator.graphics.shapes.ShapeBuilderFactory;
import translator.graphics.shapes.builders.ShapeBuilder;
import translator.graphics.shapes.builders.configurations.ShapeBuilderConfiguration;
import translator.processors.Processor;
import translator.processors.ProcessorFactory;
import translator.sources.Source;
import translator.utils.Point;

/**
 *The Translator class drives the translation process and stores the resulting structures.
 */
public class Translator {
    
    /**
     *The Parser instance to use. This comes from the decision made in the Create Parser use case.
     */
    private Parser chosenParser;
    
    /**
     *Represents the source to translate. It can be a File or Stream implementation.
     */
    private Source sourceToTranslate;
    
    /**
     *Stores the Document instance resulting from the translation process.
     */
    private Document resultingDocument;
    
    public Translator() {
    }
    
    /**
     *Drives the translation process.
     */
    public void translate() throws TranslatorException {
        resultingDocument = new Document();
        
        chosenParser.parse();
        
        resultingDocument.setProperties(chosenParser.getDocumentProperties());
        
        ProcessorFactory processorFactory = chosenParser.getProcessorFactory();
        
        for(ParsedObject object : chosenParser.getParsedStructure().getElements()){
            if(object instanceof ParsedPage){
                List<Page> pages = translatePage((ParsedPage) object, processorFactory);
                
                for(Page page : pages){
                    resultingDocument.addPage(page);
                }
            }
        }
        BoundingBox documentBoundingBox = resultingDocument.getProperties().getBoundingBox();
        
        for(Page currentPage:resultingDocument.getPages()){
            documentBoundingBox = getUnionOfBounds(documentBoundingBox, getMaxBounds(currentPage));            
        }                  
        if(documentBoundingBox != null){
        resultingDocument.getProperties().setBoundingBox(documentBoundingBox);}
    }
    
    
    private BoundingBox getMaxBounds(LayerContent currentLayerContent){
        
        BoundingBox result = null;
        
        if (currentLayerContent instanceof Shape){
            
            result = ((Shape)currentLayerContent).getBoundingBox();
            
        }else if(currentLayerContent instanceof Page){
            
            for(Layer currentLayer : ((Page)currentLayerContent).getLayers()){
                
                for(LayerContent content : currentLayer.getContents()){
                    result = getUnionOfBounds(result, getMaxBounds(content));
                }
                
            }
                    
        }        
        return result;       
        
    }
    
    public static BoundingBox getUnionOfBounds(BoundingBox firstBoundingBox, BoundingBox secondBoundingBox){
        
        BoundingBox result = null;
        Point nearestPoint = new Point();
        Point farthermostPoint = new Point();
        
        if(firstBoundingBox == null){
            result = secondBoundingBox;
        }else if(secondBoundingBox == null){
            result = firstBoundingBox;
        }else{
            
            // Determine the nearest point to the cordinates origin.
            if(firstBoundingBox.getPosition().getX() < secondBoundingBox.getPosition().getX()){
                nearestPoint.setX(firstBoundingBox.getPosition().getX());
            }else {
                nearestPoint.setX(secondBoundingBox.getPosition().getX());
            }
            
            if(firstBoundingBox.getPosition().getY() < secondBoundingBox.getPosition().getY()){
                nearestPoint.setY(firstBoundingBox.getPosition().getY());
            }else {
                nearestPoint.setY(secondBoundingBox.getPosition().getY());
            }
            
            // Determine the farthermost point to the cordinates origin.
            Point firstPoint = new Point(firstBoundingBox.getPosition().getX()+firstBoundingBox.getWidth(),
                    firstBoundingBox.getPosition().getY()+firstBoundingBox.getHeight());
            
            Point secondPoint = new Point(secondBoundingBox.getPosition().getX()+secondBoundingBox.getWidth(),
                    secondBoundingBox.getPosition().getY()+secondBoundingBox.getHeight());
            
            if(firstPoint.getX() > secondPoint.getX()){
                farthermostPoint.setX(firstPoint.getX());
            }else {
                farthermostPoint.setX(secondPoint.getX());
            }
            
            if(firstPoint.getY() > secondPoint.getY()){
                farthermostPoint.setY(firstPoint.getY());
            }else {
                farthermostPoint.setY(secondPoint.getY());
            }
            
            //Calculate the resulting BoundingBox.            
            result = new BoundingBox(nearestPoint,
                farthermostPoint.getX()-nearestPoint.getX(),
                farthermostPoint.getY()-nearestPoint.getY());
            
        }
        return result;
    }
    
    public List<Page> translatePage(ParsedPage parsedPage, ProcessorFactory processorFactory) throws TranslatorException {
        List<Page> result = new ArrayList();
        
        Page newPage = new Page(parsedPage.getId());
        newPage.setZOrder(parsedPage.getZOrder());
        
        Layer drawingLayer = new Layer();
        newPage.addLayer(drawingLayer);
        
        //  Calculate height and width with absolute value to calculate the distance between two points
        double height = Math.abs(parsedPage.getY2() - parsedPage.getY1());
        double width = Math.abs(parsedPage.getX2() - parsedPage.getX1());
        
        BoundingBox pageBoundingBox = new BoundingBox(
                new Point(parsedPage.getX1(), parsedPage.getY1()),
                width,
                height);
        
        newPage.setBoundingBox(pageBoundingBox);
        
        result.add(newPage);
        
        for(ParsedObject object : parsedPage.getElements()){
            if(object instanceof ParsedPage){
                for(Page subPage : translatePage((ParsedPage)object, processorFactory)){
                    drawingLayer.addContent(subPage);
                }
            }else if(object instanceof ParsedElement){
                
                //if the embedded objects are located directly under the page's root
                //should be assigned to the EmbeddedObjectProcessor
                if(!(object instanceof ParsedBoundObject)){
                    ParsedElement element = (ParsedElement) object;
                    
                    Processor currentProcessor = processorFactory.getProcessor(element);
                    
                    ShapeBuilderFactory shapeBuilderFactory = ShapeBuilderFactory.getInstance();
                    ShapeBuilder suitableBuilder = null;
                    ShapeBuilderConfiguration resultingConfiguration = currentProcessor.getResultingConfiguration();
                    //the resulting configuration can be null if any essentil attribute is not present
                    //(example: the bounding box in an embedded object
                    if(resultingConfiguration != null){
                        suitableBuilder = shapeBuilderFactory.getBuilder(resultingConfiguration);
                    }
                    if(suitableBuilder != null){
                        Shape builtShape = suitableBuilder.build();
                        drawingLayer.addContent(builtShape);
                    }
                }                
            }
        }
        
        return result;
    }
    
    public Parser getParser() {
        return chosenParser;
    }
    
    public void setParser(Parser chosenParser) {
        this.chosenParser = chosenParser;
    }
    
    public Source getSource() {
        return sourceToTranslate;
    }
    
    public void setSource(Source sourceToTranslate) {
        this.sourceToTranslate = sourceToTranslate;
    }
    
    public Document getDocument() throws TranslatorException {
        return resultingDocument;
    }
    
    public void setDocument(Document resultingDocument) {
        this.resultingDocument = resultingDocument;
    }
    
}
