
package svgrenderer;

/**
 * Indicates an error with serialization of the SVG document to XML.
 */
public class SVGWriterException extends Exception {
    
    public SVGWriterException(String message) {
        super(message);
    }
    
    public SVGWriterException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
