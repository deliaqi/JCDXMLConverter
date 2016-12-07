//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.05.15 at 02:23:31 PM ACT 
//


package translator.domains.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for domain-configuration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="domain-configuration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parser" type="{}parser" maxOccurs="unbounded"/>
 *         &lt;element name="document-preferences" type="{}document-preferences"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "domain-configuration", propOrder = {
    "parser",
    "documentPreferences"
})
public class DomainConfiguration {

    @XmlElement(required = true)
    protected List<Parser> parser;
    @XmlElement(name = "document-preferences", required = true)
    protected DocumentPreferences documentPreferences;

    /**
     * Gets the value of the parser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Parser }
     * 
     * 
     */
    public List<Parser> getParser() {
        if (parser == null) {
            parser = new ArrayList<Parser>();
        }
        return this.parser;
    }

    /**
     * Gets the value of the documentPreferences property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentPreferences }
     *     
     */
    public DocumentPreferences getDocumentPreferences() {
        return documentPreferences;
    }

    /**
     * Sets the value of the documentPreferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentPreferences }
     *     
     */
    public void setDocumentPreferences(DocumentPreferences value) {
        this.documentPreferences = value;
    }

}