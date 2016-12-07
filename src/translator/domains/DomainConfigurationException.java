package translator.domains;

import translator.TranslatorException;

public class DomainConfigurationException extends TranslatorException {
    
    public DomainConfigurationException() {
    }
    
    public DomainConfigurationException(String message) {
        super(message);
    }
}
