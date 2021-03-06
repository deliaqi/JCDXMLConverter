//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.04.03 at 09:18:46 AM ACT 
//


package translator.cdxml.dom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}t" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="CrossReferenceContainer" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CrossReferenceDocument" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CrossReferenceIdentifier" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CrossReferenceSequence" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "t"
})
@XmlRootElement(name = "crossreference")
public class Crossreference {

    protected List<T> t;
    @XmlAttribute(name = "CrossReferenceContainer")
    protected String crossReferenceContainer;
    @XmlAttribute(name = "CrossReferenceDocument")
    protected String crossReferenceDocument;
    @XmlAttribute(name = "CrossReferenceIdentifier", required = true)
    protected String crossReferenceIdentifier;
    @XmlAttribute(name = "CrossReferenceSequence", required = true)
    protected String crossReferenceSequence;

    /**
     * Gets the value of the t property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the t property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link T }
     * 
     * 
     */
    public List<T> getT() {
        if (t == null) {
            t = new ArrayList<T>();
        }
        return this.t;
    }

    /**
     * Gets the value of the crossReferenceContainer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossReferenceContainer() {
        return crossReferenceContainer;
    }

    /**
     * Sets the value of the crossReferenceContainer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossReferenceContainer(String value) {
        this.crossReferenceContainer = value;
    }

    /**
     * Gets the value of the crossReferenceDocument property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossReferenceDocument() {
        return crossReferenceDocument;
    }

    /**
     * Sets the value of the crossReferenceDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossReferenceDocument(String value) {
        this.crossReferenceDocument = value;
    }

    /**
     * Gets the value of the crossReferenceIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossReferenceIdentifier() {
        return crossReferenceIdentifier;
    }

    /**
     * Sets the value of the crossReferenceIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossReferenceIdentifier(String value) {
        this.crossReferenceIdentifier = value;
    }

    /**
     * Gets the value of the crossReferenceSequence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossReferenceSequence() {
        return crossReferenceSequence;
    }

    /**
     * Sets the value of the crossReferenceSequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossReferenceSequence(String value) {
        this.crossReferenceSequence = value;
    }

}
