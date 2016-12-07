
package translator;

public class ElementNotSupportedException extends Exception {
    
    public ElementNotSupportedException(String message) {
        super(message);
    }
    
    public ElementNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
