package translator.domains;

import translator.TranslatorException;

public class DomainNotSupportedException extends TranslatorException {
    
    public DomainNotSupportedException() {
    }
    
    public DomainNotSupportedException(String message) {
        super(message);
    }
}
