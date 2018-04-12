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
    "ecin",
    "ecout"
})
@XmlRootElement(name = "EntryCombination")
public class EntryCombination {

    @XmlElement(name = "ECIN", required = true)
    protected ECIN ecin;
    @XmlElement(name = "ECOUT", required = true)
    protected ECOUT ecout;

    /**
     * Ruft den Wert der ecin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ECIN }
     *     
     */
    public ECIN getECIN() {
        return ecin;
    }

    /**
     * Legt den Wert der ecin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ECIN }
     *     
     */
    public void setECIN(ECIN value) {
        this.ecin = value;
    }

    /**
     * Ruft den Wert der ecout-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ECOUT }
     *     
     */
    public ECOUT getECOUT() {
        return ecout;
    }

    /**
     * Legt den Wert der ecout-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ECOUT }
     *     
     */
    public void setECOUT(ECOUT value) {
        this.ecout = value;
    }

}
