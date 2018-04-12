//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.20 um 10:44:08 AM CEST 
//


package mesh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "concept1UI",
    "concept2UI",
    "relationAttribute"
})
@XmlRootElement(name = "ConceptRelation")
public class ConceptRelation {

    @XmlAttribute(name = "RelationName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String relationName;
    @XmlElement(name = "Concept1UI", required = true)
    protected String concept1UI;
    @XmlElement(name = "Concept2UI", required = true)
    protected String concept2UI;
    @XmlElement(name = "RelationAttribute")
    protected String relationAttribute;

    /**
     * Ruft den Wert der relationName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelationName() {
        return relationName;
    }

    /**
     * Legt den Wert der relationName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelationName(String value) {
        this.relationName = value;
    }

    /**
     * Ruft den Wert der concept1UI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConcept1UI() {
        return concept1UI;
    }

    /**
     * Legt den Wert der concept1UI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConcept1UI(String value) {
        this.concept1UI = value;
    }

    /**
     * Ruft den Wert der concept2UI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConcept2UI() {
        return concept2UI;
    }

    /**
     * Legt den Wert der concept2UI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConcept2UI(String value) {
        this.concept2UI = value;
    }

    /**
     * Ruft den Wert der relationAttribute-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelationAttribute() {
        return relationAttribute;
    }

    /**
     * Legt den Wert der relationAttribute-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelationAttribute(String value) {
        this.relationAttribute = value;
    }

}
