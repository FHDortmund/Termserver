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
    "qualifierUI",
    "qualifierName"
})
@XmlRootElement(name = "QualifierReferredTo")
public class QualifierReferredTo {

    @XmlElement(name = "QualifierUI", required = true)
    protected String qualifierUI;
    @XmlElement(name = "QualifierName", required = true)
    protected QualifierName qualifierName;

    /**
     * Ruft den Wert der qualifierUI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifierUI() {
        return qualifierUI;
    }

    /**
     * Legt den Wert der qualifierUI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifierUI(String value) {
        this.qualifierUI = value;
    }

    /**
     * Ruft den Wert der qualifierName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QualifierName }
     *     
     */
    public QualifierName getQualifierName() {
        return qualifierName;
    }

    /**
     * Legt den Wert der qualifierName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifierName }
     *     
     */
    public void setQualifierName(QualifierName value) {
        this.qualifierName = value;
    }

}
