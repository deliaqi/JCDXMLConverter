//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.02.06 at 09:25:57 AM ART 
//


package translator.domains.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import translator.domains.xml.DomainConfiguration;
import translator.domains.xml.DomainVerifier;
import translator.domains.xml.ObjectFactory;
import translator.domains.xml.Parser;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the translator.domains.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DomainConfiguration_QNAME = new QName("", "domain-configuration");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: translator.domains.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DomainConfiguration }
     * 
     */
    public DomainConfiguration createDomainConfiguration() {
        return new DomainConfiguration();
    }

    /**
     * Create an instance of {@link DomainVerifier }
     * 
     */
    public DomainVerifier createDomainVerifier() {
        return new DomainVerifier();
    }

    /**
     * Create an instance of {@link Parser }
     * 
     */
    public Parser createParser() {
        return new Parser();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DomainConfiguration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "domain-configuration")
    public JAXBElement<DomainConfiguration> createDomainConfiguration(DomainConfiguration value) {
        return new JAXBElement<DomainConfiguration>(_DomainConfiguration_QNAME, DomainConfiguration.class, null, value);
    }

}
