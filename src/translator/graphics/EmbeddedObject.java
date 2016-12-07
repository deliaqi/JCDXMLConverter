package translator.graphics;

/**
 * Class that represents an Embedded Object that contain
 * a name and an array of bytes that is stored in the
 * document and later is written to a single file, for
 * example: jpg images, png images.
 * There will be as many classes as
 * distinct embedded object types can exist in a document
 */
public class EmbeddedObject {
    
    private String imageName;
    private String imageDirectory;
    private byte[] imageBytes;
    
    public EmbeddedObject() {
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public String getImageDirectory() {
        return imageDirectory;
    }
    
    public void setImageDirectory(String imageDirectory) {
        this.imageDirectory = imageDirectory;
    }
    
    public byte[] getImageBytes() {
        return imageBytes;
    }
    
    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
}
