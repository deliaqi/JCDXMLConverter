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
 *         &lt;element ref="{}objecttag"/>
 *         &lt;element ref="{}annotation"/>
 *       &lt;/choice>
 *       &lt;attribute name="B" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BS" default="U">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="U"/>
 *             &lt;enumeration value="N"/>
 *             &lt;enumeration value="E"/>
 *             &lt;enumeration value="Z"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="BeginAttach" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BoldWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BondCircularOrdering" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BondLength" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BondSpacing" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BondSpacingAbs" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CrossingBonds" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CrossingBondss" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Display" default="Solid">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Solid"/>
 *             &lt;enumeration value="Dash"/>
 *             &lt;enumeration value="Hash"/>
 *             &lt;enumeration value="WedgedHashBegin"/>
 *             &lt;enumeration value="WedgedHashEnd"/>
 *             &lt;enumeration value="Bold"/>
 *             &lt;enumeration value="WedgeBegin"/>
 *             &lt;enumeration value="WedgeEnd"/>
 *             &lt;enumeration value="Wavy"/>
 *             &lt;enumeration value="HollowWedgeBegin"/>
 *             &lt;enumeration value="HollowWedgeEnd"/>
 *             &lt;enumeration value="WavyWedgeBegin"/>
 *             &lt;enumeration value="WavyWedgeEnd"/>
 *             &lt;enumeration value="Dot"/>
 *             &lt;enumeration value="DashDot"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="Display2" default="Solid">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Solid"/>
 *             &lt;enumeration value="Dash"/>
 *             &lt;enumeration value="Hash"/>
 *             &lt;enumeration value="WedgedHashBegin"/>
 *             &lt;enumeration value="WedgedHashEnd"/>
 *             &lt;enumeration value="Bold"/>
 *             &lt;enumeration value="WedgeBegin"/>
 *             &lt;enumeration value="WedgeEnd"/>
 *             &lt;enumeration value="Wavy"/>
 *             &lt;enumeration value="HollowWedgeBegin"/>
 *             &lt;enumeration value="HollowWedgeEnd"/>
 *             &lt;enumeration value="WavyWedgeBegin"/>
 *             &lt;enumeration value="WavyWedgeEnd"/>
 *             &lt;enumeration value="Dot"/>
 *             &lt;enumeration value="DashDot"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="DoublePosition">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Center"/>
 *             &lt;enumeration value="Right"/>
 *             &lt;enumeration value="Left"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="E" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="EndAttach" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="HashSpacing" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="IgnoreWarnings" default="no">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="yes"/>
 *             &lt;enumeration value="no"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="LabelFace" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LabelFont" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LabelSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LineWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MarginWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Order" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="RxnParticipation" default="Unspecified">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Unspecified"/>
 *             &lt;enumeration value="ReactionCenter"/>
 *             &lt;enumeration value="MakeOrBreak"/>
 *             &lt;enumeration value="ChangeType"/>
 *             &lt;enumeration value="MakeAndChange"/>
 *             &lt;enumeration value="NotReactionCenter"/>
 *             &lt;enumeration value="NoChange"/>
 *             &lt;enumeration value="Unmapped"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="ShowBondQuery" default="yes">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="yes"/>
 *             &lt;enumeration value="no"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="ShowBondRxn" default="yes">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="yes"/>
 *             &lt;enumeration value="no"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="ShowBondStereo" default="no">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="yes"/>
 *             &lt;enumeration value="no"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="SupersededBy" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Topology" default="Unspecified">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Unspecified"/>
 *             &lt;enumeration value="Ring"/>
 *             &lt;enumeration value="Chain"/>
 *             &lt;enumeration value="RingOrChain"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="Visible" default="yes">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="yes"/>
 *             &lt;enumeration value="no"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="Warning" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
    "objecttagOrAnnotation"
})
@XmlRootElement(name = "b")
public class B {

    @XmlElements({
        @XmlElement(name = "annotation", type = Annotation.class),
        @XmlElement(name = "objecttag", type = Objecttag.class)
    })
    protected List<Object> objecttagOrAnnotation;
    @XmlAttribute(name = "B", required = true)
    protected String b;
    @XmlAttribute(name = "BS")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String bs;
    @XmlAttribute(name = "BeginAttach")
    protected String beginAttach;
    @XmlAttribute(name = "BoldWidth")
    protected String boldWidth;
    @XmlAttribute(name = "BondCircularOrdering")
    protected String bondCircularOrdering;
    @XmlAttribute(name = "BondLength")
    protected String bondLength;
    @XmlAttribute(name = "BondSpacing")
    protected String bondSpacing;
    @XmlAttribute(name = "BondSpacingAbs")
    protected String bondSpacingAbs;
    @XmlAttribute(name = "CrossingBonds")
    protected String crossingBonds;
    @XmlAttribute(name = "CrossingBondss")
    protected String crossingBondss;
    @XmlAttribute(name = "Display")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String display;
    @XmlAttribute(name = "Display2")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String display2;
    @XmlAttribute(name = "DoublePosition")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String doublePosition;
    @XmlAttribute(name = "E", required = true)
    protected String e;
    @XmlAttribute(name = "EndAttach")
    protected String endAttach;
    @XmlAttribute(name = "HashSpacing")
    protected String hashSpacing;
    @XmlAttribute(name = "IgnoreWarnings")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String ignoreWarnings;
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
    @XmlAttribute(name = "Order")
    protected String order;
    @XmlAttribute(name = "RxnParticipation")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String rxnParticipation;
    @XmlAttribute(name = "ShowBondQuery")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String showBondQuery;
    @XmlAttribute(name = "ShowBondRxn")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String showBondRxn;
    @XmlAttribute(name = "ShowBondStereo")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String showBondStereo;
    @XmlAttribute(name = "SupersededBy")
    protected String supersededBy;
    @XmlAttribute(name = "Topology")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String topology;
    @XmlAttribute(name = "Visible")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String visible;
    @XmlAttribute(name = "Warning")
    protected String warning;
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
     * Gets the value of the objecttagOrAnnotation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objecttagOrAnnotation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjecttagOrAnnotation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Annotation }
     * {@link Objecttag }
     * 
     * 
     */
    public List<Object> getObjecttagOrAnnotation() {
        if (objecttagOrAnnotation == null) {
            objecttagOrAnnotation = new ArrayList<Object>();
        }
        return this.objecttagOrAnnotation;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setB(String value) {
        this.b = value;
    }

    /**
     * Gets the value of the bs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBS() {
        if (bs == null) {
            return "U";
        } else {
            return bs;
        }
    }

    /**
     * Sets the value of the bs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBS(String value) {
        this.bs = value;
    }

    /**
     * Gets the value of the beginAttach property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeginAttach() {
        return beginAttach;
    }

    /**
     * Sets the value of the beginAttach property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeginAttach(String value) {
        this.beginAttach = value;
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
     * Gets the value of the bondCircularOrdering property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBondCircularOrdering() {
        return bondCircularOrdering;
    }

    /**
     * Sets the value of the bondCircularOrdering property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBondCircularOrdering(String value) {
        this.bondCircularOrdering = value;
    }

    /**
     * Gets the value of the bondLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBondLength() {
        return bondLength;
    }

    /**
     * Sets the value of the bondLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBondLength(String value) {
        this.bondLength = value;
    }

    /**
     * Gets the value of the bondSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBondSpacing() {
        return bondSpacing;
    }

    /**
     * Sets the value of the bondSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBondSpacing(String value) {
        this.bondSpacing = value;
    }

    /**
     * Gets the value of the bondSpacingAbs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBondSpacingAbs() {
        return bondSpacingAbs;
    }

    /**
     * Sets the value of the bondSpacingAbs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBondSpacingAbs(String value) {
        this.bondSpacingAbs = value;
    }

    /**
     * Gets the value of the crossingBonds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossingBonds() {
        return crossingBonds;
    }

    /**
     * Sets the value of the crossingBonds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossingBonds(String value) {
        this.crossingBonds = value;
    }

    /**
     * Gets the value of the crossingBondss property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossingBondss() {
        return crossingBondss;
    }

    /**
     * Sets the value of the crossingBondss property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossingBondss(String value) {
        this.crossingBondss = value;
    }

    /**
     * Gets the value of the display property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplay() {
        if (display == null) {
            return "Solid";
        } else {
            return display;
        }
    }

    /**
     * Sets the value of the display property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplay(String value) {
        this.display = value;
    }

    /**
     * Gets the value of the display2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplay2() {
        if (display2 == null) {
            return "Solid";
        } else {
            return display2;
        }
    }

    /**
     * Sets the value of the display2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplay2(String value) {
        this.display2 = value;
    }

    /**
     * Gets the value of the doublePosition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoublePosition() {
        return doublePosition;
    }

    /**
     * Sets the value of the doublePosition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoublePosition(String value) {
        this.doublePosition = value;
    }

    /**
     * Gets the value of the e property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getE() {
        return e;
    }

    /**
     * Sets the value of the e property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setE(String value) {
        this.e = value;
    }

    /**
     * Gets the value of the endAttach property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndAttach() {
        return endAttach;
    }

    /**
     * Sets the value of the endAttach property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndAttach(String value) {
        this.endAttach = value;
    }

    /**
     * Gets the value of the hashSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashSpacing() {
        return hashSpacing;
    }

    /**
     * Sets the value of the hashSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashSpacing(String value) {
        this.hashSpacing = value;
    }

    /**
     * Gets the value of the ignoreWarnings property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIgnoreWarnings() {
        if (ignoreWarnings == null) {
            return "no";
        } else {
            return ignoreWarnings;
        }
    }

    /**
     * Sets the value of the ignoreWarnings property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIgnoreWarnings(String value) {
        this.ignoreWarnings = value;
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
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrder(String value) {
        this.order = value;
    }

    /**
     * Gets the value of the rxnParticipation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRxnParticipation() {
        if (rxnParticipation == null) {
            return "Unspecified";
        } else {
            return rxnParticipation;
        }
    }

    /**
     * Sets the value of the rxnParticipation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRxnParticipation(String value) {
        this.rxnParticipation = value;
    }

    /**
     * Gets the value of the showBondQuery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowBondQuery() {
        if (showBondQuery == null) {
            return "yes";
        } else {
            return showBondQuery;
        }
    }

    /**
     * Sets the value of the showBondQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowBondQuery(String value) {
        this.showBondQuery = value;
    }

    /**
     * Gets the value of the showBondRxn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowBondRxn() {
        if (showBondRxn == null) {
            return "yes";
        } else {
            return showBondRxn;
        }
    }

    /**
     * Sets the value of the showBondRxn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowBondRxn(String value) {
        this.showBondRxn = value;
    }

    /**
     * Gets the value of the showBondStereo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowBondStereo() {
        if (showBondStereo == null) {
            return "no";
        } else {
            return showBondStereo;
        }
    }

    /**
     * Sets the value of the showBondStereo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowBondStereo(String value) {
        this.showBondStereo = value;
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
     * Gets the value of the topology property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopology() {
        if (topology == null) {
            return "Unspecified";
        } else {
            return topology;
        }
    }

    /**
     * Sets the value of the topology property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopology(String value) {
        this.topology = value;
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
     * Gets the value of the warning property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarning() {
        return warning;
    }

    /**
     * Sets the value of the warning property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarning(String value) {
        this.warning = value;
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
