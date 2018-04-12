//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.04.09 um 11:53:09 AM CEST 
//


package clamlBindingXSD;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}Caption" minOccurs="0"/&gt;
 *         &lt;element ref="{}THead" minOccurs="0"/&gt;
 *         &lt;element ref="{}TBody" minOccurs="0"/&gt;
 *         &lt;element ref="{}TFoot" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
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

    @XmlElement(name = "Caption")
    protected Caption caption;
    @XmlElement(name = "THead")
    protected THead tHead;
    @XmlElement(name = "TBody")
    protected TBody tBody;
    @XmlElement(name = "TFoot")
    protected TFoot tFoot;
    @XmlAttribute(name = "class")
    protected String clazz;

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

}
