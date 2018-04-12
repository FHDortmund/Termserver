//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.20 um 10:44:07 AM CEST 
//


package clamlBindingXSD;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "caption",
    "tHead",
    "tBody",
    "tFoot"
})
@XmlRootElement(name = "Table")
public class Table {

    @XmlAttribute(name = "class")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String clazz;
    @XmlElement(name = "Caption")
    protected Caption caption;
    @XmlElement(name = "THead")
    protected THead tHead;
    @XmlElement(name = "TBody")
    protected TBody tBody;
    @XmlElement(name = "TFoot")
    protected TFoot tFoot;

    /**
     * Ruft den Wert der clazz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Legt den Wert der clazz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Ruft den Wert der caption-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Caption }
     *     
     */
    public Caption getCaption() {
        return caption;
    }

    /**
     * Legt den Wert der caption-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Caption }
     *     
     */
    public void setCaption(Caption value) {
        this.caption = value;
    }

    /**
     * Ruft den Wert der tHead-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link THead }
     *     
     */
    public THead getTHead() {
        return tHead;
    }

    /**
     * Legt den Wert der tHead-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link THead }
     *     
     */
    public void setTHead(THead value) {
        this.tHead = value;
    }

    /**
     * Ruft den Wert der tBody-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TBody }
     *     
     */
    public TBody getTBody() {
        return tBody;
    }

    /**
     * Legt den Wert der tBody-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TBody }
     *     
     */
    public void setTBody(TBody value) {
        this.tBody = value;
    }

    /**
     * Ruft den Wert der tFoot-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TFoot }
     *     
     */
    public TFoot getTFoot() {
        return tFoot;
    }

    /**
     * Legt den Wert der tFoot-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TFoot }
     *     
     */
    public void setTFoot(TFoot value) {
        this.tFoot = value;
    }

}
