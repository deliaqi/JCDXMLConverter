package translator.processors;

import translator.TranslatorException;

public class ProcessorException extends TranslatorException {
    
    public ProcessorException() {
    }
    
    public ProcessorException(String message) {
        super(message);
    }
}
