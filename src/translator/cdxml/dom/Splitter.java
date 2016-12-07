//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.04.03 at 09:18:46 AM ACT 
//


package translator.cdxml.dom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="PageDefinition" default="Undefined">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Undefined"/>
 *             &lt;enumeration value="Center"/>
 *             &lt;enumeration value="TL4"/>
 *             &lt;enumeration value="IDTerm"/>
 *             &lt;enumeration value="FlushLeft"/>
 *             &lt;enumeration value="FlushRight"/>
 *             &lt;enumeration value="Reaction1"/>
 *             &lt;enumeration value="Reaction2"/>
 *             &lt;enumeration value="MulticolumnTL4"/>
 *             &lt;enumeration value="MulticolumnNonTL4"/>
 *             &lt;enumeration value="UserDefined"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="p" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "splitter")
public class Splitter {

    @XmlAttribute(name = "PageDefinition")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String pageDefinition;
    @XmlAttribute
    protected String p;

    /**
     * Gets the value of the pageDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageDefinition() {
        if (pageDefinition == null) {
            return "Undefined";
        } else {
            return pageDefinition;
        }
    }

    /**
     * Sets the value of the pageDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageDefinition(String value) {
        this.pageDefinition = value;
    }

    /**
     * Gets the value of the p property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getP() {
        return p;
    }

    /**
     * Sets the value of the p property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setP(String value) {
        this.p = value;
    }

}