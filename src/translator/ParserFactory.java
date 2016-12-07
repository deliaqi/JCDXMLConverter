package translator;

import translator.domains.DomainConfiguration;
import translator.domains.DomainConfigurationException;
import translator.domains.DomainNotSupportedException;
import translator.domains.DomainVerifier;
import translator.domains.XMLDomainConfiguration;
import translator.sources.Source;

/**
 * Factory class to create Parser instances depending on a given source.
 * It obtains a DomainConfiguration and iterates through the DomainVerifiers it
 * contains to find a suitable Parser for the Source.
 * This class is a singleton.
 */
public final class ParserFactory {
    
    /*
     * The singleton instance of ParserFactory.
     */
    private static ParserFactory instance;
    
    private DomainConfiguration domainConfiguration;
    
    static{
        try {
            instance = new ParserFactory();
            
            String domainConfigurationName = 
                    System.getProperty(XMLDomainConfiguration.DOMAIN_CONFIGURATION_PROPERTY_KEY);
            
            DomainConfiguration domainConfiguration = 
                    (DomainConfiguration)
                        Class.forName(domainConfigurationName).newInstance();
            
            domainConfiguration.loadConfiguration();
            
            instance.setDomainConfiguration(domainConfiguration);
            
        } catch (DomainConfigurationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Getter method for instance.
     */
    public static ParserFactory getInstance(){
        return instance;
    }
    
    private ParserFactory() {
    }
    
    /**
     * Finds a Parser that is suitable for the given Source.
     * This method loads the DomainConfiguration if it hasn't been loaded yet,
     * iterates through the available parsers and calls each parser's
     * DomainVerifiers to check for its support for the Source.
     * It returns the first suitable Parser.
     */
    public Parser getParser(Source source) throws TranslatorException {
        Parser result = null;
        try {
            for(String parserName : getDomainConfiguration().getParsers()){
                for(DomainVerifier verifier : getDomainConfiguration().getVerifiers(parserName)){
                    if(verifier.isDomainSupported(source)){
                        result = (Parser) Class.forName(parserName).newInstance();
                        result.setSource(source);
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            throw new ParserInstantiationException("Could not create the specified parser: " + ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            throw new ParserInstantiationException("Could not create the specified parser: " + ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            throw new ParserInstantiationException("Could not create the specified parser: " + ex.getMessage(), ex);
        }
        
        if (result == null) {
            // there was no suitable parser for this source, so throw an exception
            throw new DomainNotSupportedException("Unsupported file format.");
        }
        
        return result;
    }

    public DomainConfiguration getDomainConfiguration() {
        return domainConfiguration;
    }

    public void setDomainConfiguration(DomainConfiguration domainConfiguration) {
        this.domainConfiguration = domainConfiguration;
    }
}
