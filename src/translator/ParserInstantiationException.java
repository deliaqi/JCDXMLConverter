package translator;

import translator.TranslatorException;

public class ParserInstantiationException extends TranslatorException {
    
    public ParserInstantiationException() {
    }
    
    public ParserInstantiationException(String message) {
        super(message);
    }
    
    public ParserInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
