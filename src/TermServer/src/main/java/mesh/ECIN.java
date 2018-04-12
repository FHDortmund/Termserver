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
    "descriptorReferredTo",
    "qualifierReferredTo"
})
@XmlRootElement(name = "ECIN")
public class ECIN {

    @XmlElement(name = "DescriptorReferredTo", required = true)
    protected DescriptorReferredTo descriptorReferredTo;
    @XmlElement(name = "QualifierReferredTo", required = true)
    protected QualifierReferredTo qualifierReferredTo;

    /**
     * Ruft den Wert der descriptorReferredTo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DescriptorReferredTo }
     *     
     */
    public DescriptorReferredTo getDescriptorReferredTo() {
        return descriptorReferredTo;
    }

    /**
     * Legt den Wert der descriptorReferredTo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptorReferredTo }
     *     
     */
    public void setDescriptorReferredTo(DescriptorReferredTo value) {
        this.descriptorReferredTo = value;
    }

    /**
     * Ruft den Wert der qualifierReferredTo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QualifierReferredTo }
     *     
     */
    public QualifierReferredTo getQualifierReferredTo() {
        return qualifierReferredTo;
    }

    /**
     * Legt den Wert der qualifierReferredTo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifierReferredTo }
     *     
     */
    public void setQualifierReferredTo(QualifierReferredTo value) {
        this.qualifierReferredTo = value;
    }

}
