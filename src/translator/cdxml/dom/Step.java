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


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ReactionStepArrows" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepAtomMap" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepAtomMapAuto" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepAtomMapManual" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepObjectsAboveArrow" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepObjectsBelowArrow" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepPlusses" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepProducts" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ReactionStepReactants" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "step")
public class Step {

    @XmlAttribute(name = "ReactionStepArrows")
    protected String reactionStepArrows;
    @XmlAttribute(name = "ReactionStepAtomMap")
    protected String reactionStepAtomMap;
    @XmlAttribute(name = "ReactionStepAtomMapAuto")
    protected String reactionStepAtomMapAuto;
    @XmlAttribute(name = "ReactionStepAtomMapManual")
    protected String reactionStepAtomMapManual;
    @XmlAttribute(name = "ReactionStepObjectsAboveArrow")
    protected String reactionStepObjectsAboveArrow;
    @XmlAttribute(name = "ReactionStepObjectsBelowArrow")
    protected String reactionStepObjectsBelowArrow;
    @XmlAttribute(name = "ReactionStepPlusses")
    protected String reactionStepPlusses;
    @XmlAttribute(name = "ReactionStepProducts")
    protected String reactionStepProducts;
    @XmlAttribute(name = "ReactionStepReactants")
    protected String reactionStepReactants;
    @XmlAttribute
    protected String id;

    /**
     * Gets the value of the reactionStepArrows property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepArrows() {
        return reactionStepArrows;
    }

    /**
     * Sets the value of the reactionStepArrows property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepArrows(String value) {
        this.reactionStepArrows = value;
    }

    /**
     * Gets the value of the reactionStepAtomMap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepAtomMap() {
        return reactionStepAtomMap;
    }

    /**
     * Sets the value of the reactionStepAtomMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepAtomMap(String value) {
        this.reactionStepAtomMap = value;
    }

    /**
     * Gets the value of the reactionStepAtomMapAuto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepAtomMapAuto() {
        return reactionStepAtomMapAuto;
    }

    /**
     * Sets the value of the reactionStepAtomMapAuto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepAtomMapAuto(String value) {
        this.reactionStepAtomMapAuto = value;
    }

    /**
     * Gets the value of the reactionStepAtomMapManual property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepAtomMapManual() {
        return reactionStepAtomMapManual;
    }

    /**
     * Sets the value of the reactionStepAtomMapManual property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepAtomMapManual(String value) {
        this.reactionStepAtomMapManual = value;
    }

    /**
     * Gets the value of the reactionStepObjectsAboveArrow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepObjectsAboveArrow() {
        return reactionStepObjectsAboveArrow;
    }

    /**
     * Sets the value of the reactionStepObjectsAboveArrow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepObjectsAboveArrow(String value) {
        this.reactionStepObjectsAboveArrow = value;
    }

    /**
     * Gets the value of the reactionStepObjectsBelowArrow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepObjectsBelowArrow() {
        return reactionStepObjectsBelowArrow;
    }

    /**
     * Sets the value of the reactionStepObjectsBelowArrow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepObjectsBelowArrow(String value) {
        this.reactionStepObjectsBelowArrow = value;
    }

    /**
     * Gets the value of the reactionStepPlusses property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepPlusses() {
        return reactionStepPlusses;
    }

    /**
     * Sets the value of the reactionStepPlusses property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepPlusses(String value) {
        this.reactionStepPlusses = value;
    }

    /**
     * Gets the value of the reactionStepProducts property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepProducts() {
        return reactionStepProducts;
    }

    /**
     * Sets the value of the reactionStepProducts property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepProducts(String value) {
        this.reactionStepProducts = value;
    }

    /**
     * Gets the value of the reactionStepReactants property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReactionStepReactants() {
        return reactionStepReactants;
    }

    /**
     * Sets the value of the reactionStepReactants property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReactionStepReactants(String value) {
        this.reactionStepReactants = value;
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
