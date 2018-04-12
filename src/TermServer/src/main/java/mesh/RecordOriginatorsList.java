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
    "recordOriginator",
    "recordMaintainer",
    "recordAuthorizer"
})
@XmlRootElement(name = "RecordOriginatorsList")
public class RecordOriginatorsList {

    @XmlElement(name = "RecordOriginator", required = true)
    protected String recordOriginator;
    @XmlElement(name = "RecordMaintainer")
    protected String recordMaintainer;
    @XmlElement(name = "RecordAuthorizer")
    protected String recordAuthorizer;

    /**
     * Ruft den Wert der recordOriginator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordOriginator() {
        return recordOriginator;
    }

    /**
     * Legt den Wert der recordOriginator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordOriginator(String value) {
        this.recordOriginator = value;
    }

    /**
     * Ruft den Wert der recordMaintainer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordMaintainer() {
        return recordMaintainer;
    }

    /**
     * Legt den Wert der recordMaintainer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordMaintainer(String value) {
        this.recordMaintainer = value;
    }

    /**
     * Ruft den Wert der recordAuthorizer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordAuthorizer() {
        return recordAuthorizer;
    }

    /**
     * Legt den Wert der recordAuthorizer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordAuthorizer(String value) {
        this.recordAuthorizer = value;
    }

}
