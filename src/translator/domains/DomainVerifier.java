package translator.domains;

import translator.sources.Source;

/**
 * Represents an object that will analyze a given Source and determine whether
 * it is of a specific type. These objects are used by Parsers, through the
 * DomainConfiguration, to specify their domain (the types of source they can
 * handle).
 * Any DomainVerifier can be used by any Parser, even reused among Parsers.
 * For instance, a CDXMLDomainVerifier would analyze a Source to determine
 * whether it is a valid XML file and conforms to the CDXML DTD.
 */
public interface DomainVerifier {
    
    /**
     * Analyzes the given source and returns a boolean value of true if the
     * source is within the domain or false if it isn't.
     */
    public boolean isDomainSupported(Source source);
    
}
