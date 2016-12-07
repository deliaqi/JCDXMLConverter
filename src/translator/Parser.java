package translator;

import translator.cddom.properties.DocumentProperties;
import translator.processors.ProcessorFactory;
import translator.sources.Source;

/**
 * Represents an object that can parse a file's contents and build a structure
 * that a Translator can work with.
 * It also provides the implementation’s ProcessorFactory from which the
 * Translator can get Processors for individual parsed elements.
 */
public interface Parser<S extends Source> {
    
    public void setSource(S source);
    
    /**
     * Returns the ParsedStructure resulting from the parse() method execution.
     */
    public ParsedStructure getParsedStructure() throws ParserException;
    
    /**
     * Parser classes should implement this method to read the contents provided
     * by the Source object and use that information to build a ParsedStructure.
     */
    // TODO: Remove after testing this works fine
    public void parse() throws ParserException;
    
    /**
     * Returns a custom ProcessorFactory instance, provided by source
     * support implementations.
     */
    public ProcessorFactory getProcessorFactory();
    
    public DocumentProperties getDocumentProperties();
    
}
