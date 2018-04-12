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
    "termUI",
    "string",
    "dateCreated",
    "abbreviation",
    "sortVersion",
    "entryVersion",
    "thesaurusIDlist",
    "termNote"
})
@XmlRootElement(name = "Term")
public class Term {

    @XmlAttribute(name = "ConceptPreferredTermYN", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String conceptPreferredTermYN;
    @XmlAttribute(name = "IsPermutedTermYN", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isPermutedTermYN;
    @XmlAttribute(name = "LexicalTag", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lexicalTag;
    @XmlAttribute(name = "PrintFlagYN", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String printFlagYN;
    @XmlAttribute(name = "RecordPreferredTermYN", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String recordPreferredTermYN;
    @XmlElement(name = "TermUI", required = true)
    protected String termUI;
    @XmlElement(name = "String", required = true)
    protected String string;
    @XmlElement(name = "DateCreated")
    protected DateCreated dateCreated;
    @XmlElement(name = "Abbreviation")
    protected String abbreviation;
    @XmlElement(name = "SortVersion")
    protected String sortVersion;
    @XmlElement(name = "EntryVersion")
    protected String entryVersion;
    @XmlElement(name = "ThesaurusIDlist")
    protected ThesaurusIDlist thesaurusIDlist;
    @XmlElement(name = "TermNote")
    protected String termNote;

    /**
     * Ruft den Wert der conceptPreferredTermYN-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConceptPreferredTermYN() {
        return conceptPreferredTermYN;
    }

    /**
     * Legt den Wert der conceptPreferredTermYN-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConceptPreferredTermYN(String value) {
        this.conceptPreferredTermYN = value;
    }

    /**
     * Ruft den Wert der isPermutedTermYN-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsPermutedTermYN() {
        return isPermutedTermYN;
    }

    /**
     * Legt den Wert der isPermutedTermYN-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsPermutedTermYN(String value) {
        this.isPermutedTermYN = value;
    }

    /**
     * Ruft den Wert der lexicalTag-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLexicalTag() {
        return lexicalTag;
    }

    /**
     * Legt den Wert der lexicalTag-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLexicalTag(String value) {
        this.lexicalTag = value;
    }

    /**
     * Ruft den Wert der printFlagYN-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrintFlagYN() {
        return printFlagYN;
    }

    /**
     * Legt den Wert der printFlagYN-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrintFlagYN(String value) {
        this.printFlagYN = value;
    }

    /**
     * Ruft den Wert der recordPreferredTermYN-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordPreferredTermYN() {
        return recordPreferredTermYN;
    }

    /**
     * Legt den Wert der recordPreferredTermYN-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordPreferredTermYN(String value) {
        this.recordPreferredTermYN = value;
    }

    /**
     * Ruft den Wert der termUI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTermUI() {
        return termUI;
    }

    /**
     * Legt den Wert der termUI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTermUI(String value) {
        this.termUI = value;
    }

    /**
     * Ruft den Wert der string-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getString() {
        return string;
    }

    /**
     * Legt den Wert der string-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setString(String value) {
        this.string = value;
    }

    /**
     * Ruft den Wert der dateCreated-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DateCreated }
     *     
     */
    public DateCreated getDateCreated() {
        return dateCreated;
    }

    /**
     * Legt den Wert der dateCreated-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DateCreated }
     *     
     */
    public void setDateCreated(DateCreated value) {
        this.dateCreated = value;
    }

    /**
     * Ruft den Wert der abbreviation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Legt den Wert der abbreviation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbbreviation(String value) {
        this.abbreviation = value;
    }

    /**
     * Ruft den Wert der sortVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSortVersion() {
        return sortVersion;
    }

    /**
     * Legt den Wert der sortVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSortVersion(String value) {
        this.sortVersion = value;
    }

    /**
     * Ruft den Wert der entryVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntryVersion() {
        return entryVersion;
    }

    /**
     * Legt den Wert der entryVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntryVersion(String value) {
        this.entryVersion = value;
    }

    /**
     * Ruft den Wert der thesaurusIDlist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ThesaurusIDlist }
     *     
     */
    public ThesaurusIDlist getThesaurusIDlist() {
        return thesaurusIDlist;
    }

    /**
     * Legt den Wert der thesaurusIDlist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ThesaurusIDlist }
     *     
     */
    public void setThesaurusIDlist(ThesaurusIDlist value) {
        this.thesaurusIDlist = value;
    }

    /**
     * Ruft den Wert der termNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTermNote() {
        return termNote;
    }

    /**
     * Legt den Wert der termNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTermNote(String value) {
        this.termNote = value;
    }

}
