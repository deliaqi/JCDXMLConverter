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
 *         &lt;element ref="{}curve"/>
 *       &lt;/choice>
 *       &lt;attribute name="BioShapeType">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Undefined"/>
 *             &lt;enumeration value="1SubstrateEnzyme"/>
 *             &lt;enumeration value="2SubstrateEnzyme"/>
 *             &lt;enumeration value="Receptor"/>
 *             &lt;enumeration value="GProteinAlpha"/>
 *             &lt;enumeration value="GProteinBeta"/>
 *             &lt;enumeration value="GProteinGamma"/>
 *             &lt;enumeration value="Immunoglobin"/>
 *             &lt;enumeration value="IonChannel"/>
 *             &lt;enumeration value="EndoplasmicReticulum"/>
 *             &lt;enumeration value="Golgi"/>
 *             &lt;enumeration value="MembraneLine"/>
 *             &lt;enumeration value="MembraneArc"/>
 *             &lt;enumeration value="MembraneEllipse"/>
 *             &lt;enumeration value="MembraneMicelle"/>
 *             &lt;enumeration value="DNA"/>
 *             &lt;enumeration value="HelixProtein"/>
 *             &lt;enumeration value="Mitochondrion"/>
 *             &lt;enumeration value="Cloud"/>
 *             &lt;enumeration value="tRNA"/>
 *             &lt;enumeration value="RibosomeA"/>
 *             &lt;enumeration value="RibosomeB"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="BoldWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BoundingBox" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CylinderDistance" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CylinderHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CylinderWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="DNAWaveHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="DNAWaveLength" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="DNAWaveOffset" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="DNAWaveWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="EnzymeHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="EnzymeReceptorSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="EnzymeWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="FadePercent" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="FillType">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Unspecified"/>
 *             &lt;enumeration value="None"/>
 *             &lt;enumeration value="Solid"/>
 *             &lt;enumeration value="Shaded"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="GolgiHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="GolgiLength" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="GolgiWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="GproteinLowerHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="GproteinUpperHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="HashSpacing" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="HelixProteinExtra" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ImmunoglobinHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ImmunoglobinWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="LineType">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="Solid"/>
 *             &lt;enumeration value="Dashed"/>
 *             &lt;enumeration value="Bold"/>
 *             &lt;enumeration value="Wavy"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="LineWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MajorAxisEnd3D" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MembraneElementSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MembraneEndAngle" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MembraneMajorAxisSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MembraneMinorAxisSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MembraneStartAngle" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MinorAxisEnd3D" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="NeckHeight" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="NeckWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="PipeWidth" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
 *       &lt;attribute name="xyz" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "objecttagOrAnnotationOrCurve"
})
@XmlRootElement(name = "bioshape")
public class Bioshape {

    @XmlElements({
        @XmlElement(name = "objecttag", type = Objecttag.class),
        @XmlElement(name = "curve", type = Curve.class),
        @XmlElement(name = "annotation", type = Annotation.class)
    })
    protected List<Object> objecttagOrAnnotationOrCurve;
    @XmlAttribute(name = "BioShapeType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String bioShapeType;
    @XmlAttribute(name = "BoldWidth")
    protected String boldWidth;
    @XmlAttribute(name = "BoundingBox")
    protected String boundingBox;
    @XmlAttribute(name = "CylinderDistance")
    protected String cylinderDistance;
    @XmlAttribute(name = "CylinderHeight")
    protected String cylinderHeight;
    @XmlAttribute(name = "CylinderWidth")
    protected String cylinderWidth;
    @XmlAttribute(name = "DNAWaveHeight")
    protected String dnaWaveHeight;
    @XmlAttribute(name = "DNAWaveLength")
    protected String dnaWaveLength;
    @XmlAttribute(name = "DNAWaveOffset")
    protected String dnaWaveOffset;
    @XmlAttribute(name = "DNAWaveWidth")
    protected String dnaWaveWidth;
    @XmlAttribute(name = "EnzymeHeight")
    protected String enzymeHeight;
    @XmlAttribute(name = "EnzymeReceptorSize")
    protected String enzymeReceptorSize;
    @XmlAttribute(name = "EnzymeWidth")
    protected String enzymeWidth;
    @XmlAttribute(name = "FadePercent")
    protected String fadePercent;
    @XmlAttribute(name = "FillType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fillType;
    @XmlAttribute(name = "GolgiHeight")
    protected String golgiHeight;
    @XmlAttribute(name = "GolgiLength")
    protected String golgiLength;
    @XmlAttribute(name = "GolgiWidth")
    protected String golgiWidth;
    @XmlAttribute(name = "GproteinLowerHeight")
    protected String gproteinLowerHeight;
    @XmlAttribute(name = "GproteinUpperHeight")
    protected String gproteinUpperHeight;
    @XmlAttribute(name = "HashSpacing")
    protected String hashSpacing;
    @XmlAttribute(name = "HelixProteinExtra")
    protected String helixProteinExtra;
    @XmlAttribute(name = "ImmunoglobinHeight")
    protected String immunoglobinHeight;
    @XmlAttribute(name = "ImmunoglobinWidth")
    protected String immunoglobinWidth;
    @XmlAttribute(name = "LineType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lineType;
    @XmlAttribute(name = "LineWidth")
    protected String lineWidth;
    @XmlAttribute(name = "MajorAxisEnd3D")
    protected String majorAxisEnd3D;
    @XmlAttribute(name = "MembraneElementSize")
    protected String membraneElementSize;
    @XmlAttribute(name = "MembraneEndAngle")
    protected String membraneEndAngle;
    @XmlAttribute(name = "MembraneMajorAxisSize")
    protected String membraneMajorAxisSize;
    @XmlAttribute(name = "MembraneMinorAxisSize")
    protected String membraneMinorAxisSize;
    @XmlAttribute(name = "MembraneStartAngle")
    protected String membraneStartAngle;
    @XmlAttribute(name = "MinorAxisEnd3D")
    protected String minorAxisEnd3D;
    @XmlAttribute(name = "NeckHeight")
    protected String neckHeight;
    @XmlAttribute(name = "NeckWidth")
    protected String neckWidth;
    @XmlAttribute(name = "PipeWidth")
    protected String pipeWidth;
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
    @XmlAttribute
    protected String xyz;

    /**
     * Gets the value of the objecttagOrAnnotationOrCurve property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objecttagOrAnnotationOrCurve property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjecttagOrAnnotationOrCurve().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Objecttag }
     * {@link Curve }
     * {@link Annotation }
     * 
     * 
     */
    public List<Object> getObjecttagOrAnnotationOrCurve() {
        if (objecttagOrAnnotationOrCurve == null) {
            objecttagOrAnnotationOrCurve = new ArrayList<Object>();
        }
        return this.objecttagOrAnnotationOrCurve;
    }

    /**
     * Gets the value of the bioShapeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBioShapeType() {
        return bioShapeType;
    }

    /**
     * Sets the value of the bioShapeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBioShapeType(String value) {
        this.bioShapeType = value;
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
     * Gets the value of the cylinderDistance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCylinderDistance() {
        return cylinderDistance;
    }

    /**
     * Sets the value of the cylinderDistance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCylinderDistance(String value) {
        this.cylinderDistance = value;
    }

    /**
     * Gets the value of the cylinderHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCylinderHeight() {
        return cylinderHeight;
    }

    /**
     * Sets the value of the cylinderHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCylinderHeight(String value) {
        this.cylinderHeight = value;
    }

    /**
     * Gets the value of the cylinderWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCylinderWidth() {
        return cylinderWidth;
    }

    /**
     * Sets the value of the cylinderWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCylinderWidth(String value) {
        this.cylinderWidth = value;
    }

    /**
     * Gets the value of the dnaWaveHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDNAWaveHeight() {
        return dnaWaveHeight;
    }

    /**
     * Sets the value of the dnaWaveHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDNAWaveHeight(String value) {
        this.dnaWaveHeight = value;
    }

    /**
     * Gets the value of the dnaWaveLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDNAWaveLength() {
        return dnaWaveLength;
    }

    /**
     * Sets the value of the dnaWaveLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDNAWaveLength(String value) {
        this.dnaWaveLength = value;
    }

    /**
     * Gets the value of the dnaWaveOffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDNAWaveOffset() {
        return dnaWaveOffset;
    }

    /**
     * Sets the value of the dnaWaveOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDNAWaveOffset(String value) {
        this.dnaWaveOffset = value;
    }

    /**
     * Gets the value of the dnaWaveWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDNAWaveWidth() {
        return dnaWaveWidth;
    }

    /**
     * Sets the value of the dnaWaveWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDNAWaveWidth(String value) {
        this.dnaWaveWidth = value;
    }

    /**
     * Gets the value of the enzymeHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnzymeHeight() {
        return enzymeHeight;
    }

    /**
     * Sets the value of the enzymeHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnzymeHeight(String value) {
        this.enzymeHeight = value;
    }

    /**
     * Gets the value of the enzymeReceptorSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnzymeReceptorSize() {
        return enzymeReceptorSize;
    }

    /**
     * Sets the value of the enzymeReceptorSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnzymeReceptorSize(String value) {
        this.enzymeReceptorSize = value;
    }

    /**
     * Gets the value of the enzymeWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnzymeWidth() {
        return enzymeWidth;
    }

    /**
     * Sets the value of the enzymeWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnzymeWidth(String value) {
        this.enzymeWidth = value;
    }

    /**
     * Gets the value of the fadePercent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFadePercent() {
        return fadePercent;
    }

    /**
     * Sets the value of the fadePercent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFadePercent(String value) {
        this.fadePercent = value;
    }

    /**
     * Gets the value of the fillType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillType() {
        return fillType;
    }

    /**
     * Sets the value of the fillType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillType(String value) {
        this.fillType = value;
    }

    /**
     * Gets the value of the golgiHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGolgiHeight() {
        return golgiHeight;
    }

    /**
     * Sets the value of the golgiHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGolgiHeight(String value) {
        this.golgiHeight = value;
    }

    /**
     * Gets the value of the golgiLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGolgiLength() {
        return golgiLength;
    }

    /**
     * Sets the value of the golgiLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGolgiLength(String value) {
        this.golgiLength = value;
    }

    /**
     * Gets the value of the golgiWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGolgiWidth() {
        return golgiWidth;
    }

    /**
     * Sets the value of the golgiWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGolgiWidth(String value) {
        this.golgiWidth = value;
    }

    /**
     * Gets the value of the gproteinLowerHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGproteinLowerHeight() {
        return gproteinLowerHeight;
    }

    /**
     * Sets the value of the gproteinLowerHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGproteinLowerHeight(String value) {
        this.gproteinLowerHeight = value;
    }

    /**
     * Gets the value of the gproteinUpperHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGproteinUpperHeight() {
        return gproteinUpperHeight;
    }

    /**
     * Sets the value of the gproteinUpperHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGproteinUpperHeight(String value) {
        this.gproteinUpperHeight = value;
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
     * Gets the value of the helixProteinExtra property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHelixProteinExtra() {
        return helixProteinExtra;
    }

    /**
     * Sets the value of the helixProteinExtra property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHelixProteinExtra(String value) {
        this.helixProteinExtra = value;
    }

    /**
     * Gets the value of the immunoglobinHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImmunoglobinHeight() {
        return immunoglobinHeight;
    }

    /**
     * Sets the value of the immunoglobinHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImmunoglobinHeight(String value) {
        this.immunoglobinHeight = value;
    }

    /**
     * Gets the value of the immunoglobinWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImmunoglobinWidth() {
        return immunoglobinWidth;
    }

    /**
     * Sets the value of the immunoglobinWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImmunoglobinWidth(String value) {
        this.immunoglobinWidth = value;
    }

    /**
     * Gets the value of the lineType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineType() {
        return lineType;
    }

    /**
     * Sets the value of the lineType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineType(String value) {
        this.lineType = value;
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
     * Gets the value of the majorAxisEnd3D property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMajorAxisEnd3D() {
        return majorAxisEnd3D;
    }

    /**
     * Sets the value of the majorAxisEnd3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMajorAxisEnd3D(String value) {
        this.majorAxisEnd3D = value;
    }

    /**
     * Gets the value of the membraneElementSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMembraneElementSize() {
        return membraneElementSize;
    }

    /**
     * Sets the value of the membraneElementSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMembraneElementSize(String value) {
        this.membraneElementSize = value;
    }

    /**
     * Gets the value of the membraneEndAngle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMembraneEndAngle() {
        return membraneEndAngle;
    }

    /**
     * Sets the value of the membraneEndAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMembraneEndAngle(String value) {
        this.membraneEndAngle = value;
    }

    /**
     * Gets the value of the membraneMajorAxisSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMembraneMajorAxisSize() {
        return membraneMajorAxisSize;
    }

    /**
     * Sets the value of the membraneMajorAxisSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMembraneMajorAxisSize(String value) {
        this.membraneMajorAxisSize = value;
    }

    /**
     * Gets the value of the membraneMinorAxisSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMembraneMinorAxisSize() {
        return membraneMinorAxisSize;
    }

    /**
     * Sets the value of the membraneMinorAxisSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMembraneMinorAxisSize(String value) {
        this.membraneMinorAxisSize = value;
    }

    /**
     * Gets the value of the membraneStartAngle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMembraneStartAngle() {
        return membraneStartAngle;
    }

    /**
     * Sets the value of the membraneStartAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMembraneStartAngle(String value) {
        this.membraneStartAngle = value;
    }

    /**
     * Gets the value of the minorAxisEnd3D property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinorAxisEnd3D() {
        return minorAxisEnd3D;
    }

    /**
     * Sets the value of the minorAxisEnd3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinorAxisEnd3D(String value) {
        this.minorAxisEnd3D = value;
    }

    /**
     * Gets the value of the neckHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNeckHeight() {
        return neckHeight;
    }

    /**
     * Sets the value of the neckHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNeckHeight(String value) {
        this.neckHeight = value;
    }

    /**
     * Gets the value of the neckWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNeckWidth() {
        return neckWidth;
    }

    /**
     * Sets the value of the neckWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNeckWidth(String value) {
        this.neckWidth = value;
    }

    /**
     * Gets the value of the pipeWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPipeWidth() {
        return pipeWidth;
    }

    /**
     * Sets the value of the pipeWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPipeWidth(String value) {
        this.pipeWidth = value;
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

    /**
     * Gets the value of the xyz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXyz() {
        return xyz;
    }

    /**
     * Sets the value of the xyz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXyz(String value) {
        this.xyz = value;
    }

}
