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
    "conceptUI",
    "conceptName",
    "casn1Name",
    "registryNumber",
    "scopeNote",
    "translatorsEnglishScopeNote",
    "translatorsScopeNote",
    "semanticTypeList",
    "relatedRegistryNumberList",
    "conceptRelationList",
    "termList"
})
@XmlRootElement(name = "Concept")
public class Concept {

    @XmlAttribute(name = "PreferredConceptYN", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String preferredConceptYN;
    @XmlElement(name = "ConceptUI", required = true)
    protected String conceptUI;
    @XmlElement(name = "ConceptName", required = true)
    protected ConceptName conceptName;
    @XmlElement(name = "CASN1Name")
    protected String casn1Name;
    @XmlElement(name = "RegistryNumber")
    protected String registryNumber;
    @XmlElement(name = "ScopeNote")
    protected String scopeNote;
    @XmlElement(name = "TranslatorsEnglishScopeNote")
    protected String translatorsEnglishScopeNote;
    @XmlElement(name = "TranslatorsScopeNote")
    protected String translatorsScopeNote;
    @XmlElement(name = "SemanticTypeList")
    protected SemanticTypeList semanticTypeList;
    @XmlElement(name = "RelatedRegistryNumberList")
    protected RelatedRegistryNumberList relatedRegistryNumberList;
    @XmlElement(name = "ConceptRelationList")
    protected ConceptRelationList conceptRelationList;
    @XmlElement(name = "TermList", required = true)
    protected TermList termList;

    /**
     * Ruft den Wert der preferredConceptYN-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferredConceptYN() {
        return preferredConceptYN;
    }

    /**
     * Legt den Wert der preferredConceptYN-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferredConceptYN(String value) {
        this.preferredConceptYN = value;
    }

    /**
     * Ruft den Wert der conceptUI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConceptUI() {
        return conceptUI;
    }

    /**
     * Legt den Wert der conceptUI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConceptUI(String value) {
        this.conceptUI = value;
    }

    /**
     * Ruft den Wert der conceptName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConceptName }
     *     
     */
    public ConceptName getConceptName() {
        return conceptName;
    }

    /**
     * Legt den Wert der conceptName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptName }
     *     
     */
    public void setConceptName(ConceptName value) {
        this.conceptName = value;
    }

    /**
     * Ruft den Wert der casn1Name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCASN1Name() {
        return casn1Name;
    }

    /**
     * Legt den Wert der casn1Name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCASN1Name(String value) {
        this.casn1Name = value;
    }

    /**
     * Ruft den Wert der registryNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistryNumber() {
        return registryNumber;
    }

    /**
     * Legt den Wert der registryNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistryNumber(String value) {
        this.registryNumber = value;
    }

    /**
     * Ruft den Wert der scopeNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScopeNote() {
        return scopeNote;
    }

    /**
     * Legt den Wert der scopeNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScopeNote(String value) {
        this.scopeNote = value;
    }

    /**
     * Ruft den Wert der translatorsEnglishScopeNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTranslatorsEnglishScopeNote() {
        return translatorsEnglishScopeNote;
    }

    /**
     * Legt den Wert der translatorsEnglishScopeNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTranslatorsEnglishScopeNote(String value) {
        this.translatorsEnglishScopeNote = value;
    }

    /**
     * Ruft den Wert der translatorsScopeNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTranslatorsScopeNote() {
        return translatorsScopeNote;
    }

    /**
     * Legt den Wert der translatorsScopeNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTranslatorsScopeNote(String value) {
        this.translatorsScopeNote = value;
    }

    /**
     * Ruft den Wert der semanticTypeList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SemanticTypeList }
     *     
     */
    public SemanticTypeList getSemanticTypeList() {
        return semanticTypeList;
    }

    /**
     * Legt den Wert der semanticTypeList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SemanticTypeList }
     *     
     */
    public void setSemanticTypeList(SemanticTypeList value) {
        this.semanticTypeList = value;
    }

    /**
     * Ruft den Wert der relatedRegistryNumberList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RelatedRegistryNumberList }
     *     
     */
    public RelatedRegistryNumberList getRelatedRegistryNumberList() {
        return relatedRegistryNumberList;
    }

    /**
     * Legt den Wert der relatedRegistryNumberList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RelatedRegistryNumberList }
     *     
     */
    public void setRelatedRegistryNumberList(RelatedRegistryNumberList value) {
        this.relatedRegistryNumberList = value;
    }

    /**
     * Ruft den Wert der conceptRelationList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConceptRelationList }
     *     
     */
    public ConceptRelationList getConceptRelationList() {
        return conceptRelationList;
    }

    /**
     * Legt den Wert der conceptRelationList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptRelationList }
     *     
     */
    public void setConceptRelationList(ConceptRelationList value) {
        this.conceptRelationList = value;
    }

    /**
     * Ruft den Wert der termList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TermList }
     *     
     */
    public TermList getTermList() {
        return termList;
    }

    /**
     * Legt den Wert der termList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TermList }
     *     
     */
    public void setTermList(TermList value) {
        this.termList = value;
    }

}
