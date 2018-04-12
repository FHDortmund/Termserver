//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.20 um 10:44:07 AM CEST 
//


package clamlBindingXSD;

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
    "usageKind"
})
@XmlRootElement(name = "UsageKinds")
public class UsageKinds {

    @XmlElement(name = "UsageKind", required = true)
    protected List<UsageKind> usageKind;

    /**
     * Gets the value of the usageKind property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the usageKind property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUsageKind().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UsageKind }
     * 
     * 
     */
    public List<UsageKind> getUsageKind() {
        if (usageKind == null) {
            usageKind = new ArrayList<UsageKind>();
        }
        return this.usageKind;
    }

}
