package translator;

public class TranslatorException extends Exception {
    
    public TranslatorException() {
    }
    
    public TranslatorException(String message) {
        super(message);
    }
    
    public TranslatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
