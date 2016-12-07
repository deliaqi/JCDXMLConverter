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
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence>
 *         &lt;element ref="{}bracketattachment" maxOccurs="unbounded"/>
 *         &lt;element ref="{}bracketedgroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="BracketUsage">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Unspecified"/>
 *             &lt;enumeration value="Anypolymer"/>
 *             &lt;enumeration value="Component"/>
 *             &lt;enumeration value="Copolymer"/>
 *             &lt;enumeration value="CopolymerAlternating"/>
 *             &lt;enumeration value="CopolymerBlock"/>
 *             &lt;enumeration value="CopolymerRandom"/>
 *             &lt;enumeration value="Crosslink"/>
 *             &lt;enumeration value="Generic"/>
 *             &lt;enumeration value="Graft"/>
 *             &lt;enumeration value="Mer"/>
 *             &lt;enumeration value="MixtureOrdered"/>
 *             &lt;enumeration value="MixtureUnordered"/>
 *             &lt;enumeration value="Modification"/>
 *             &lt;enumeration value="Monomer"/>
 *             &lt;enumeration value="MultipleGroup"/>
 *             &lt;enumeration value="SRU"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="BracketedObjectIDs" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ComponentOrder" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="PolymerFlipType">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Unspecified"/>
 *             &lt;enumeration value="NoFlip"/>
 *             &lt;enumeration value="Flip"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="PolymerRepeatPattern">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="HeadToTail"/>
 *             &lt;enumeration value="HeadToHead"/>
 *             &lt;enumeration value="EitherUnknown"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="RepeatCount" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="SRULabel" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bracketattachment",
    "bracketedgroup"
})
@XmlRootElement(name = "bracketedgroup")
public class Bracketedgroup {

    @XmlElement(required = true)
    protected List<Bracketattachment> bracketattachment;
    protected List<Bracketedgroup> bracketedgroup;
    @XmlAttribute(name = "BracketUsage")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String bracketUsage;
    @XmlAttribute(name = "BracketedObjectIDs")
    protected String bracketedObjectIDs;
    @XmlAttribute(name = "ComponentOrder")
    protected String componentOrder;
    @XmlAttribute(name = "PolymerFlipType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String polymerFlipType;
    @XmlAttribute(name = "PolymerRepeatPattern")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String polymerRepeatPattern;
    @XmlAttribute(name = "RepeatCount")
    protected String repeatCount;
    @XmlAttribute(name = "SRULabel")
    protected String sruLabel;
    @XmlAttribute
    protected String id;

    /**
     * Gets the value of the bracketattachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bracketattachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBracketattachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bracketattachment }
     * 
     * 
     */
    public List<Bracketattachment> getBracketattachment() {
        if (bracketattachment == null) {
            bracketattachment = new ArrayList<Bracketattachment>();
        }
        return this.bracketattachment;
    }

    /**
     * Gets the value of the bracketedgroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bracketedgroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBracketedgroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bracketedgroup }
     * 
     * 
     */
    public List<Bracketedgroup> getBracketedgroup() {
        if (bracketedgroup == null) {
            bracketedgroup = new ArrayList<Bracketedgroup>();
        }
        return this.bracketedgroup;
    }

    /**
     * Gets the value of the bracketUsage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBracketUsage() {
        return bracketUsage;
    }

    /**
     * Sets the value of the bracketUsage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBracketUsage(String value) {
        this.bracketUsage = value;
    }

    /**
     * Gets the value of the bracketedObjectIDs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBracketedObjectIDs() {
        return bracketedObjectIDs;
    }

    /**
     * Sets the value of the bracketedObjectIDs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBracketedObjectIDs(String value) {
        this.bracketedObjectIDs = value;
    }

    /**
     * Gets the value of the componentOrder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentOrder() {
        return componentOrder;
    }

    /**
     * Sets the value of the componentOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentOrder(String value) {
        this.componentOrder = value;
    }

    /**
     * Gets the value of the polymerFlipType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolymerFlipType() {
        return polymerFlipType;
    }

    /**
     * Sets the value of the polymerFlipType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolymerFlipType(String value) {
        this.polymerFlipType = value;
    }

    /**
     * Gets the value of the polymerRepeatPattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolymerRepeatPattern() {
        return polymerRepeatPattern;
    }

    /**
     * Sets the value of the polymerRepeatPattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolymerRepeatPattern(String value) {
        this.polymerRepeatPattern = value;
    }

    /**
     * Gets the value of the repeatCount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepeatCount() {
        return repeatCount;
    }

    /**
     * Sets the value of the repeatCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepeatCount(String value) {
        this.repeatCount = value;
    }

    /**
     * Gets the value of the sruLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSRULabel() {
        return sruLabel;
    }

    /**
     * Sets the value of the sruLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSRULabel(String value) {
        this.sruLabel = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}