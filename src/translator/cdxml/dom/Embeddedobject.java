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
 *       &lt;attribute name="BMP" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="BoundingBox" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CompressedEnhancedMetafile" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CompressedOLEObject" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="CompressedWindowsMetafile" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Edition" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="EditionAlias" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="EnhancedMetafile" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="GIF" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="JPEG" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="MacPICT" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="OLEObject" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="PNG" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="RotationAngle" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="SupersededBy" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="TIFF" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="UncompressedEnhancedMetafileSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="UncompressedOLEObjectSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="UncompressedWindowsMetafileSize" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="WindowsMetafile" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
@XmlRootElement(name = "embeddedobject")
public class Embeddedobject {

    @XmlElements({
        @XmlElement(name = "annotation", type = Annotation.class),
        @XmlElement(name = "objecttag", type = Objecttag.class)
    })
    protected List<Object> objecttagOrAnnotation;
    @XmlAttribute(name = "BMP")
    protected String bmp;
    @XmlAttribute(name = "BoundingBox")
    protected String boundingBox;
    @XmlAttribute(name = "CompressedEnhancedMetafile")
    protected String compressedEnhancedMetafile;
    @XmlAttribute(name = "CompressedOLEObject")
    protected String compressedOLEObject;
    @XmlAttribute(name = "CompressedWindowsMetafile")
    protected String compressedWindowsMetafile;
    @XmlAttribute(name = "Edition")
    protected String edition;
    @XmlAttribute(name = "EditionAlias")
    protected String editionAlias;
    @XmlAttribute(name = "EnhancedMetafile")
    protected String enhancedMetafile;
    @XmlAttribute(name = "GIF")
    protected String gif;
    @XmlAttribute(name = "JPEG")
    protected String jpeg;
    @XmlAttribute(name = "MacPICT")
    protected String macPICT;
    @XmlAttribute(name = "OLEObject")
    protected String oleObject;
    @XmlAttribute(name = "PNG")
    protected String png;
    @XmlAttribute(name = "RotationAngle")
    protected String rotationAngle;
    @XmlAttribute(name = "SupersededBy")
    protected String supersededBy;
    @XmlAttribute(name = "TIFF")
    protected String tiff;
    @XmlAttribute(name = "UncompressedEnhancedMetafileSize")
    protected String uncompressedEnhancedMetafileSize;
    @XmlAttribute(name = "UncompressedOLEObjectSize")
    protected String uncompressedOLEObjectSize;
    @XmlAttribute(name = "UncompressedWindowsMetafileSize")
    protected String uncompressedWindowsMetafileSize;
    @XmlAttribute(name = "WindowsMetafile")
    protected String windowsMetafile;
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
     * Gets the value of the bmp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBMP() {
        return bmp;
    }

    /**
     * Sets the value of the bmp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBMP(String value) {
        this.bmp = value;
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
     * Gets the value of the compressedEnhancedMetafile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompressedEnhancedMetafile() {
        return compressedEnhancedMetafile;
    }

    /**
     * Sets the value of the compressedEnhancedMetafile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompressedEnhancedMetafile(String value) {
        this.compressedEnhancedMetafile = value;
    }

    /**
     * Gets the value of the compressedOLEObject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompressedOLEObject() {
        return compressedOLEObject;
    }

    /**
     * Sets the value of the compressedOLEObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompressedOLEObject(String value) {
        this.compressedOLEObject = value;
    }

    /**
     * Gets the value of the compressedWindowsMetafile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompressedWindowsMetafile() {
        return compressedWindowsMetafile;
    }

    /**
     * Sets the value of the compressedWindowsMetafile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompressedWindowsMetafile(String value) {
        this.compressedWindowsMetafile = value;
    }

    /**
     * Gets the value of the edition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEdition() {
        return edition;
    }

    /**
     * Sets the value of the edition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEdition(String value) {
        this.edition = value;
    }

    /**
     * Gets the value of the editionAlias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEditionAlias() {
        return editionAlias;
    }

    /**
     * Sets the value of the editionAlias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEditionAlias(String value) {
        this.editionAlias = value;
    }

    /**
     * Gets the value of the enhancedMetafile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnhancedMetafile() {
        return enhancedMetafile;
    }

    /**
     * Sets the value of the enhancedMetafile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnhancedMetafile(String value) {
        this.enhancedMetafile = value;
    }

    /**
     * Gets the value of the gif property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGIF() {
        return gif;
    }

    /**
     * Sets the value of the gif property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGIF(String value) {
        this.gif = value;
    }

    /**
     * Gets the value of the jpeg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJPEG() {
        return jpeg;
    }

    /**
     * Sets the value of the jpeg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJPEG(String value) {
        this.jpeg = value;
    }

    /**
     * Gets the value of the macPICT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacPICT() {
        return macPICT;
    }

    /**
     * Sets the value of the macPICT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacPICT(String value) {
        this.macPICT = value;
    }

    /**
     * Gets the value of the oleObject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOLEObject() {
        return oleObject;
    }

    /**
     * Sets the value of the oleObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOLEObject(String value) {
        this.oleObject = value;
    }

    /**
     * Gets the value of the png property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPNG() {
        return png;
    }

    /**
     * Sets the value of the png property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPNG(String value) {
        this.png = value;
    }

    /**
     * Gets the value of the rotationAngle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Sets the value of the rotationAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotationAngle(String value) {
        this.rotationAngle = value;
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
     * Gets the value of the tiff property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTIFF() {
        return tiff;
    }

    /**
     * Sets the value of the tiff property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTIFF(String value) {
        this.tiff = value;
    }

    /**
     * Gets the value of the uncompressedEnhancedMetafileSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUncompressedEnhancedMetafileSize() {
        return uncompressedEnhancedMetafileSize;
    }

    /**
     * Sets the value of the uncompressedEnhancedMetafileSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUncompressedEnhancedMetafileSize(String value) {
        this.uncompressedEnhancedMetafileSize = value;
    }

    /**
     * Gets the value of the uncompressedOLEObjectSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUncompressedOLEObjectSize() {
        return uncompressedOLEObjectSize;
    }

    /**
     * Sets the value of the uncompressedOLEObjectSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUncompressedOLEObjectSize(String value) {
        this.uncompressedOLEObjectSize = value;
    }

    /**
     * Gets the value of the uncompressedWindowsMetafileSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUncompressedWindowsMetafileSize() {
        return uncompressedWindowsMetafileSize;
    }

    /**
     * Sets the value of the uncompressedWindowsMetafileSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUncompressedWindowsMetafileSize(String value) {
        this.uncompressedWindowsMetafileSize = value;
    }

    /**
     * Gets the value of the windowsMetafile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWindowsMetafile() {
        return windowsMetafile;
    }

    /**
     * Sets the value of the windowsMetafile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWindowsMetafile(String value) {
        this.windowsMetafile = value;
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
