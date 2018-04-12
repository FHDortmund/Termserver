//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.20 um 10:44:08 AM CEST 
//


package mesh;

import java.util.ArrayList;
import java.util.List;
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
    "conceptRelation"
})
@XmlRootElement(name = "ConceptRelationList")
public class ConceptRelationList {

    @XmlElement(name = "ConceptRelation", required = true)
    protected List<ConceptRelation> conceptRelation;

    /**
     * Gets the value of the conceptRelation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conceptRelation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConceptRelation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptRelation }
     * 
     * 
     */
    public List<ConceptRelation> getConceptRelation() {
        if (conceptRelation == null) {
            conceptRelation = new ArrayList<ConceptRelation>();
        }
        return this.conceptRelation;
    }

}
