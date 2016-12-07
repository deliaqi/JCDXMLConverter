package translator.processors;

import java.util.Hashtable;
import translator.ParsedElement;

public abstract class ProcessorFactory {
    
    private Hashtable<String, Processor> processors = new Hashtable();
    
    public ProcessorFactory() {
    }
    
    public Processor getProcessor(ParsedElement element){
        Processor suitableProcessor = processors.get(element.getName());
        suitableProcessor.setElement(element);
        
        return suitableProcessor;
    }
    
    public void addProcessor(String parsedElementName, Processor newProcessor) throws ProcessorException {
        if(processors.get(parsedElementName) != null){
            throw new ProcessorException("A processor already exists for this parsed element");
        }
        
        processors.put(parsedElementName, newProcessor);
    }
}
