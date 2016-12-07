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
import javax.xml.bind.annotation.XmlElements;
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
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{}page"/>
 *         &lt;element ref="{}objecttag"/>
 *         &lt;element ref="{}annotation"/>
 *       &lt;/choice>
 *       &lt;attribute name="BoldWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BoundingBox" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LabelFace" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LabelFont" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LabelSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LineWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MarginWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="SupersededBy" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Visible" default="yes">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="yes"/>
 *             &lt;enumeration value="no"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="Z" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="bgRGBA" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="color" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="fgRGBA" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
    "pageOrObjecttagOrAnnotation"
})
@XmlRootElement(name = "table")
public class Table {

    @XmlElements({
        @XmlElement(name = "objecttag", type = Objecttag.class),
        @XmlElement(name = "annotation", type = Annotation.class),
        @XmlElement(name = "page", type = Page.class)
    })
    protected List<Object> pageOrObjecttagOrAnnotation;
    @XmlAttribute(name = "BoldWidth")
    protected String boldWidth;
    @XmlAttribute(name = "BoundingBox")
    protected String boundingBox;
    @XmlAttribute(name = "LabelFace")
    protected String labelFace;
    @XmlAttribute(name = "LabelFont")
    protected String labelFont;
    @XmlAttribute(name = "LabelSize")
    protected String labelSize;
    @XmlAttribute(name = "LineWidth")
    protected String lineWidth;
    @XmlAttribute(name = "MarginWidth")
    protected String marginWidth;
    @XmlAttribute(name = "SupersededBy")
    protected String supersededBy;
    @XmlAttribute(name = "Visible")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String visible;
    @XmlAttribute(name = "Z")
    protected String z;
    @XmlAttribute
    protected String bgRGBA;
    @XmlAttribute
    protected String color;
    @XmlAttribute
    protected String fgRGBA;
    @XmlAttribute
    protected String id;

    /**
     * Gets the value of the pageOrObjecttagOrAnnotation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pageOrObjecttagOrAnnotation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPageOrObjecttagOrAnnotation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Objecttag }
     * {@link Annotation }
     * {@link Page }
     * 
     * 
     */
    public List<Object> getPageOrObjecttagOrAnnotation() {
        if (pageOrObjecttagOrAnnotation == null) {
            pageOrObjecttagOrAnnotation = new ArrayList<Object>();
        }
        return this.pageOrObjecttagOrAnnotation;
    }

    /**
     * Gets the value of the boldWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBoldWidth() {
        return boldWidth;
    }

    /**
     * Sets the value of the boldWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBoldWidth(String value) {
        this.boldWidth = value;
    }

    /**
     * Gets the value of the boundingBox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the value of the boundingBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBoundingBox(String value) {
        this.boundingBox = value;
    }

    /**
     * Gets the value of the labelFace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelFace() {
        return labelFace;
    }

    /**
     * Sets the value of the labelFace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelFace(String value) {
        this.labelFace = value;
    }

    /**
     * Gets the value of the labelFont property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelFont() {
        return labelFont;
    }

    /**
     * Sets the value of the labelFont property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelFont(String value) {
        this.labelFont = value;
    }

    /**
     * Gets the value of the labelSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabelSize() {
        return labelSize;
    }

    /**
     * Sets the value of the labelSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabelSize(String value) {
        this.labelSize = value;
    }

    /**
     * Gets the value of the lineWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the value of the lineWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineWidth(String value) {
        this.lineWidth = value;
    }

    /**
     * Gets the value of the marginWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMarginWidth() {
        return marginWidth;
    }

    /**
     * Sets the value of the marginWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMarginWidth(String value) {
        this.marginWidth = value;
    }

    /**
     * Gets the value of the supersededBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupersededBy() {
        return supersededBy;
    }

    /**
     * Sets the value of the supersededBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupersededBy(String value) {
        this.supersededBy = value;
    }

    /**
     * Gets the value of the visible property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVisible() {
        if (visible == null) {
            return "yes";
        } else {
            return visible;
        }
    }

    /**
     * Sets the value of the visible property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVisible(String value) {
        this.visible = value;
    }

    /**
     * Gets the value of the z property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setZ(String value) {
        this.z = value;
    }

    /**
     * Gets the value of the bgRGBA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBgRGBA() {
        return bgRGBA;
    }

    /**
     * Sets the value of the bgRGBA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBgRGBA(String value) {
        this.bgRGBA = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the fgRGBA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFgRGBA() {
        return fgRGBA;
    }

    /**
     * Sets the value of the fgRGBA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFgRGBA(String value) {
        this.fgRGBA = value;
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
