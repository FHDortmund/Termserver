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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "meta",
    "identifier",
    "title",
    "authors",
    "variants",
    "classKinds",
    "usageKinds",
    "rubricKinds",
    "modifier",
    "modifierClass",
    "clazz"
})
@XmlRootElement(name = "ClaML")
public class ClaML {

    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlElement(name = "Meta")
    protected List<Meta> meta;
    @XmlElement(name = "Identifier")
    protected List<Identifier> identifier;
    @XmlElement(name = "Title", required = true)
    protected Title title;
    @XmlElement(name = "Authors")
    protected Authors authors;
    @XmlElement(name = "Variants")
    protected Variants variants;
    @XmlElement(name = "ClassKinds", required = true)
    protected ClassKinds classKinds;
    @XmlElement(name = "UsageKinds")
    protected UsageKinds usageKinds;
    @XmlElement(name = "RubricKinds", required = true)
    protected RubricKinds rubricKinds;
    @XmlElement(name = "Modifier")
    protected List<Modifier> modifier;
    @XmlElement(name = "ModifierClass")
    protected List<ModifierClass> modifierClass;
    @XmlElement(name = "Class")
    protected List<Class> clazz;

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the meta property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meta property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeta().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Meta }
     * 
     * 
     */
    public List<Meta> getMeta() {
        if (meta == null) {
            meta = new ArrayList<Meta>();
        }
        return this.meta;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Identifier }
     * 
     * 
     */
    public List<Identifier> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<Identifier>();
        }
        return this.identifier;
    }

    /**
     * Ruft den Wert der title-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Title }
     *     
     */
    public Title getTitle() {
        return title;
    }

    /**
     * Legt den Wert der title-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Title }
     *     
     */
    public void setTitle(Title value) {
        this.title = value;
    }

    /**
     * Ruft den Wert der authors-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Authors }
     *     
     */
    public Authors getAuthors() {
        return authors;
    }

    /**
     * Legt den Wert der authors-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Authors }
     *     
     */
    public void setAuthors(Authors value) {
        this.authors = value;
    }

    /**
     * Ruft den Wert der variants-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Variants }
     *     
     */
    public Variants getVariants() {
        return variants;
    }

    /**
     * Legt den Wert der variants-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Variants }
     *     
     */
    public void setVariants(Variants value) {
        this.variants = value;
    }

    /**
     * Ruft den Wert der classKinds-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ClassKinds }
     *     
     */
    public ClassKinds getClassKinds() {
        return classKinds;
    }

    /**
     * Legt den Wert der classKinds-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassKinds }
     *     
     */
    public void setClassKinds(ClassKinds value) {
        this.classKinds = value;
    }

    /**
     * Ruft den Wert der usageKinds-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UsageKinds }
     *     
     */
    public UsageKinds getUsageKinds() {
        return usageKinds;
    }

    /**
     * Legt den Wert der usageKinds-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UsageKinds }
     *     
     */
    public void setUsageKinds(UsageKinds value) {
        this.usageKinds = value;
    }

    /**
     * Ruft den Wert der rubricKinds-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RubricKinds }
     *     
     */
    public RubricKinds getRubricKinds() {
        return rubricKinds;
    }

    /**
     * Legt den Wert der rubricKinds-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RubricKinds }
     *     
     */
    public void setRubricKinds(RubricKinds value) {
        this.rubricKinds = value;
    }

    /**
     * Gets the value of the modifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Modifier }
     * 
     * 
     */
    public List<Modifier> getModifier() {
        if (modifier == null) {
            modifier = new ArrayList<Modifier>();
        }
        return this.modifier;
    }

    /**
     * Gets the value of the modifierClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modifierClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModifierClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModifierClass }
     * 
     * 
     */
    public List<ModifierClass> getModifierClass() {
        if (modifierClass == null) {
            modifierClass = new ArrayList<ModifierClass>();
        }
        return this.modifierClass;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clazz property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClazz().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Class }
     * 
     * 
     */
    public List<Class> getClazz() {
        if (clazz == null) {
            clazz = new ArrayList<Class>();
        }
        return this.clazz;
    }

}
