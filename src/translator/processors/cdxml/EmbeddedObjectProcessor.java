package translator.processors.cdxml;

import translator.utils.Convert;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.graphics.shapes.builders.configurations.ImageConfiguration;
import translator.utils.GeometricOperations;
import translator.utils.Point;

public class EmbeddedObjectProcessor extends GraphicProcessor{
    private ParsedElement imageElement;
    private String imageBoundingBox;
    private String imageName;
    private String imageBytes;
    private double imageAngle;
    double imageWidth;
    double imageHeight;
    private Point imageCenterPoint;
    
    private List<Point> boundingBoxPoints;
    
    public EmbeddedObjectProcessor() {
    }
    
    protected  void process(){
        imageElement = getElement();
        //the embedded object can't be processed if the bounding box is not present
        if (imageElement.hasAttribute(ParseElementDefinition.EMBEDDED_OBJECT_BOUNDING_BOX)){
            imageBoundingBox = imageElement.getAttribute(
                    ParseElementDefinition.EMBEDDED_OBJECT_BOUNDING_BOX);
            
            setAttributes();
            
            imageAngle = GeometricOperations.angle(boundingBoxPoints.get(0).getX(),
                    boundingBoxPoints.get(0).getY(), boundingBoxPoints.get(1).getX(),
                    boundingBoxPoints.get(0).getY());
            
            //calculate the center point: first move height/2 from the corner point
            //in the same angle that the bounding box height, and then move
            //width/2 in the same angle of the bounding box width
            imageCenterPoint = GeometricOperations.offset(boundingBoxPoints.get(0),
                    imageAngle + Math.PI / 2, imageHeight / 2 );
            imageCenterPoint = GeometricOperations.offset(imageCenterPoint, imageAngle,
                    imageWidth / 2);
            
            
            setResultingConfiguration(getImageConfiguration(imageElement));
        }
    }
    
    /*
     *This method can be call from the classes that extends from
     *EmbeddedObjectProcessor to obtain the Image Configuration
     */
    protected ImageConfiguration getImageConfiguration(ParsedElement
            parsedEmbeddedObject, double angle, Point imageCenter, int baseZOrder){
        
        this.zOrder = parsedEmbeddedObject.getZOrder();
        this.zOrder += baseZOrder;
        
        imageElement = parsedEmbeddedObject;
        imageBoundingBox = imageElement.getAttribute(
                ParseElementDefinition.EMBEDDED_OBJECT_BOUNDING_BOX);
        this.imageCenterPoint = imageCenter;
        imageAngle = angle;
        setAttributes();
        return getImageConfiguration(parsedEmbeddedObject);
    }
    
    /* obtain the image name, imageBytes, bounding box points
     * and the width & heigh of the image
     */
    private void setAttributes(){
        imageName = imageElement.getAttribute(ParseElementDefinition.EMBEDDED_OBJECT_IMAGE_NAME);
        imageBytes = imageElement.getAttribute(ParseElementDefinition.EMBEDDED_OBJECT_IMAGE_BYTES);
        boundingBoxPoints = parsePoints(imageBoundingBox, imageElement);
        
        imageWidth = GeometricOperations.distance(boundingBoxPoints.get(0).getX(),
                boundingBoxPoints.get(0).getY(), boundingBoxPoints.get(1).getX(),
                boundingBoxPoints.get(0).getY());
        imageHeight = GeometricOperations.distance(boundingBoxPoints.get(0).getX(),
                boundingBoxPoints.get(0).getY(), boundingBoxPoints.get(0).getX(),
                boundingBoxPoints.get(1).getY());
    }
    
    private ImageConfiguration getImageConfiguration(ParsedElement
            parsedEmbeddedObject){
        ImageConfiguration imageConfiguration;
        
        //create the Image configuration
        //and set the attributes
        imageConfiguration = new ImageConfiguration();
        imageConfiguration.setImageName(imageName);
        //convert the imagebytes to base64
        imageConfiguration.setImageBytes(getImageBytesCconvertedToBase64());
        imageConfiguration.setCornerPoint(boundingBoxPoints.get(0));
        imageConfiguration.setHeight(imageHeight);
        imageConfiguration.setWidth(imageWidth);
        imageConfiguration.setZOrder(zOrder);
        imageConfiguration.setRotate(true);
        imageConfiguration.setRotationAngle(Math.toDegrees(imageAngle));
        imageConfiguration.setRotationCenter(imageCenterPoint);
        
        return imageConfiguration;
    }
    
    /**
     * Convert the byte string to Base64
     */
    private String getImageBytesCconvertedToBase64(){
        String result;
        
        byte[] imageByte = null;
        
        if(!imageName.endsWith(".emz")){
            int numberOfChars = imageBytes.length();
            imageByte = new byte[numberOfChars/2];
                        
            for (int i = 0; i < numberOfChars; i+=2) {                
                imageByte[i/2] = (byte) Integer.parseInt(imageBytes.substring(i, i+2), 16);
            }     
        }else{
            imageByte = imageBytes.getBytes();
        }
        
        result = Convert.toBase64String(imageByte);        
        
        return result;
    }
    
    protected void cleanup() {
        imageName = null;
        imageBytes = null;
        imageBoundingBox = null;
        imageAngle = 0;
        imageWidth = 0;
        imageHeight = 0;
        imageCenterPoint = null;
        imageElement = null;
        
        super.cleanup();
    }
}
