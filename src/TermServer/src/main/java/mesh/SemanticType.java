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
    "semanticTypeUI",
    "semanticTypeName"
})
@XmlRootElement(name = "SemanticType")
public class SemanticType {

    @XmlElement(name = "SemanticTypeUI", required = true)
    protected String semanticTypeUI;
    @XmlElement(name = "SemanticTypeName", required = true)
    protected String semanticTypeName;

    /**
     * Ruft den Wert der semanticTypeUI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSemanticTypeUI() {
        return semanticTypeUI;
    }

    /**
     * Legt den Wert der semanticTypeUI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSemanticTypeUI(String value) {
        this.semanticTypeUI = value;
    }

    /**
     * Ruft den Wert der semanticTypeName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSemanticTypeName() {
        return semanticTypeName;
    }

    /**
     * Legt den Wert der semanticTypeName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSemanticTypeName(String value) {
        this.semanticTypeName = value;
    }

}
