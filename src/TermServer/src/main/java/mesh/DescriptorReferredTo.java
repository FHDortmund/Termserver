//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.20 um 10:44:08 AM CEST 
//


package mesh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "descriptorUI",
    "descriptorName"
})
@XmlRootElement(name = "DescriptorReferredTo")
public class DescriptorReferredTo {

    @XmlElement(name = "DescriptorUI", required = true)
    protected String descriptorUI;
    @XmlElement(name = "DescriptorName", required = true)
    protected DescriptorName descriptorName;

    /**
     * Ruft den Wert der descriptorUI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptorUI() {
        return descriptorUI;
    }

    /**
     * Legt den Wert der descriptorUI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptorUI(String value) {
        this.descriptorUI = value;
    }

    /**
     * Ruft den Wert der descriptorName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DescriptorName }
     *     
     */
    public DescriptorName getDescriptorName() {
        return descriptorName;
    }

    /**
     * Legt den Wert der descriptorName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptorName }
     *     
     */
    public void setDescriptorName(DescriptorName value) {
        this.descriptorName = value;
    }

}
