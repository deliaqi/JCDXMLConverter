package translator.domains;

import java.util.Collection;

/**
 * This interface provides information about available Parsers and their
 * respective domains.
 * A Parser's domain is comprised of all the types of Sources that it can handle.
 * The way the information about domains is provided depends on implementations
 * of this interface. An XMLDomainConfiguration class is provided with the
 * framework to use an XML file to store the configuration.
 * 
 * Note that this interface is called equal to the class inside xml directory.
 * In C#, this interface is called IDomainConfiguration
 */
public interface DomainConfiguration {
    
    public Collection<String> getParsers();
    
    /**
     * Causes the configuration to be loaded into memory in the implementation's
     * proprietary way.
     */
    public void loadConfiguration() throws DomainConfigurationException;
    
    /**
     * Returns a collection of DomainVerifier objects that are declared to serve
     * a given Parser in checking for support for different types of sources.
     * The name of the Parser is passed in as a parameter.
     */
    public Collection<translator.domains.DomainVerifier> getVerifiers(String parserName) throws DomainConfigurationException;
    
}
