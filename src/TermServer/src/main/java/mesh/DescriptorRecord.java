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
    "descriptorUI",
    "descriptorName",
    "dateCreated",
    "dateRevised",
    "dateEstablished",
    "activeMeSHYearList",
    "allowableQualifiersList",
    "annotation",
    "historyNote",
    "onlineNote",
    "publicMeSHNote",
    "previousIndexingList",
    "entryCombinationList",
    "seeRelatedList",
    "considerAlso",
    "pharmacologicalActionList",
    "runningHead",
    "treeNumberList",
    "recordOriginatorsList",
    "conceptList"
})
@XmlRootElement(name = "DescriptorRecord")
public class DescriptorRecord {

    @XmlAttribute(name = "DescriptorClass")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String descriptorClass;
    @XmlElement(name = "DescriptorUI", required = true)
    protected String descriptorUI;
    @XmlElement(name = "DescriptorName", required = true)
    protected DescriptorName descriptorName;
    @XmlElement(name = "DateCreated", required = true)
    protected DateCreated dateCreated;
    @XmlElement(name = "DateRevised")
    protected DateRevised dateRevised;
    @XmlElement(name = "DateEstablished")
    protected DateEstablished dateEstablished;
    @XmlElement(name = "ActiveMeSHYearList", required = true)
    protected ActiveMeSHYearList activeMeSHYearList;
    @XmlElement(name = "AllowableQualifiersList")
    protected AllowableQualifiersList allowableQualifiersList;
    @XmlElement(name = "Annotation")
    protected String annotation;
    @XmlElement(name = "HistoryNote")
    protected String historyNote;
    @XmlElement(name = "OnlineNote")
    protected String onlineNote;
    @XmlElement(name = "PublicMeSHNote")
    protected String publicMeSHNote;
    @XmlElement(name = "PreviousIndexingList")
    protected PreviousIndexingList previousIndexingList;
    @XmlElement(name = "EntryCombinationList")
    protected EntryCombinationList entryCombinationList;
    @XmlElement(name = "SeeRelatedList")
    protected SeeRelatedList seeRelatedList;
    @XmlElement(name = "ConsiderAlso")
    protected String considerAlso;
    @XmlElement(name = "PharmacologicalActionList")
    protected PharmacologicalActionList pharmacologicalActionList;
    @XmlElement(name = "RunningHead")
    protected String runningHead;
    @XmlElement(name = "TreeNumberList")
    protected TreeNumberList treeNumberList;
    @XmlElement(name = "RecordOriginatorsList", required = true)
    protected RecordOriginatorsList recordOriginatorsList;
    @XmlElement(name = "ConceptList", required = true)
    protected ConceptList conceptList;

    /**
     * Ruft den Wert der descriptorClass-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptorClass() {
        if (descriptorClass == null) {
            return "1";
        } else {
            return descriptorClass;
        }
    }

    /**
     * Legt den Wert der descriptorClass-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptorClass(String value) {
        this.descriptorClass = value;
    }

    /**
     * Ruft den Wert der descriptorUI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptorUI() {
        return descriptorUI;
    }

    /**
     * Legt den Wert der descriptorUI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptorUI(String value) {
        this.descriptorUI = value;
    }

    /**
     * Ruft den Wert der descriptorName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DescriptorName }
     *     
     */
    public DescriptorName getDescriptorName() {
        return descriptorName;
    }

    /**
     * Legt den Wert der descriptorName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptorName }
     *     
     */
    public void setDescriptorName(DescriptorName value) {
        this.descriptorName = value;
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
     * Ruft den Wert der dateRevised-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DateRevised }
     *     
     */
    public DateRevised getDateRevised() {
        return dateRevised;
    }

    /**
     * Legt den Wert der dateRevised-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DateRevised }
     *     
     */
    public void setDateRevised(DateRevised value) {
        this.dateRevised = value;
    }

    /**
     * Ruft den Wert der dateEstablished-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DateEstablished }
     *     
     */
    public DateEstablished getDateEstablished() {
        return dateEstablished;
    }

    /**
     * Legt den Wert der dateEstablished-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DateEstablished }
     *     
     */
    public void setDateEstablished(DateEstablished value) {
        this.dateEstablished = value;
    }

    /**
     * Ruft den Wert der activeMeSHYearList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ActiveMeSHYearList }
     *     
     */
    public ActiveMeSHYearList getActiveMeSHYearList() {
        return activeMeSHYearList;
    }

    /**
     * Legt den Wert der activeMeSHYearList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ActiveMeSHYearList }
     *     
     */
    public void setActiveMeSHYearList(ActiveMeSHYearList value) {
        this.activeMeSHYearList = value;
    }

    /**
     * Ruft den Wert der allowableQualifiersList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AllowableQualifiersList }
     *     
     */
    public AllowableQualifiersList getAllowableQualifiersList() {
        return allowableQualifiersList;
    }

    /**
     * Legt den Wert der allowableQualifiersList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowableQualifiersList }
     *     
     */
    public void setAllowableQualifiersList(AllowableQualifiersList value) {
        this.allowableQualifiersList = value;
    }

    /**
     * Ruft den Wert der annotation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * Legt den Wert der annotation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnotation(String value) {
        this.annotation = value;
    }

    /**
     * Ruft den Wert der historyNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHistoryNote() {
        return historyNote;
    }

    /**
     * Legt den Wert der historyNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHistoryNote(String value) {
        this.historyNote = value;
    }

    /**
     * Ruft den Wert der onlineNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnlineNote() {
        return onlineNote;
    }

    /**
     * Legt den Wert der onlineNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnlineNote(String value) {
        this.onlineNote = value;
    }

    /**
     * Ruft den Wert der publicMeSHNote-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublicMeSHNote() {
        return publicMeSHNote;
    }

    /**
     * Legt den Wert der publicMeSHNote-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublicMeSHNote(String value) {
        this.publicMeSHNote = value;
    }

    /**
     * Ruft den Wert der previousIndexingList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PreviousIndexingList }
     *     
     */
    public PreviousIndexingList getPreviousIndexingList() {
        return previousIndexingList;
    }

    /**
     * Legt den Wert der previousIndexingList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PreviousIndexingList }
     *     
     */
    public void setPreviousIndexingList(PreviousIndexingList value) {
        this.previousIndexingList = value;
    }

    /**
     * Ruft den Wert der entryCombinationList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EntryCombinationList }
     *     
     */
    public EntryCombinationList getEntryCombinationList() {
        return entryCombinationList;
    }

    /**
     * Legt den Wert der entryCombinationList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EntryCombinationList }
     *     
     */
    public void setEntryCombinationList(EntryCombinationList value) {
        this.entryCombinationList = value;
    }

    /**
     * Ruft den Wert der seeRelatedList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SeeRelatedList }
     *     
     */
    public SeeRelatedList getSeeRelatedList() {
        return seeRelatedList;
    }

    /**
     * Legt den Wert der seeRelatedList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SeeRelatedList }
     *     
     */
    public void setSeeRelatedList(SeeRelatedList value) {
        this.seeRelatedList = value;
    }

    /**
     * Ruft den Wert der considerAlso-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsiderAlso() {
        return considerAlso;
    }

    /**
     * Legt den Wert der considerAlso-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsiderAlso(String value) {
        this.considerAlso = value;
    }

    /**
     * Ruft den Wert der pharmacologicalActionList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PharmacologicalActionList }
     *     
     */
    public PharmacologicalActionList getPharmacologicalActionList() {
        return pharmacologicalActionList;
    }

    /**
     * Legt den Wert der pharmacologicalActionList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PharmacologicalActionList }
     *     
     */
    public void setPharmacologicalActionList(PharmacologicalActionList value) {
        this.pharmacologicalActionList = value;
    }

    /**
     * Ruft den Wert der runningHead-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunningHead() {
        return runningHead;
    }

    /**
     * Legt den Wert der runningHead-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunningHead(String value) {
        this.runningHead = value;
    }

    /**
     * Ruft den Wert der treeNumberList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TreeNumberList }
     *     
     */
    public TreeNumberList getTreeNumberList() {
        return treeNumberList;
    }

    /**
     * Legt den Wert der treeNumberList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TreeNumberList }
     *     
     */
    public void setTreeNumberList(TreeNumberList value) {
        this.treeNumberList = value;
    }

    /**
     * Ruft den Wert der recordOriginatorsList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RecordOriginatorsList }
     *     
     */
    public RecordOriginatorsList getRecordOriginatorsList() {
        return recordOriginatorsList;
    }

    /**
     * Legt den Wert der recordOriginatorsList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RecordOriginatorsList }
     *     
     */
    public void setRecordOriginatorsList(RecordOriginatorsList value) {
        this.recordOriginatorsList = value;
    }

    /**
     * Ruft den Wert der conceptList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConceptList }
     *     
     */
    public ConceptList getConceptList() {
        return conceptList;
    }

    /**
     * Legt den Wert der conceptList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptList }
     *     
     */
    public void setConceptList(ConceptList value) {
        this.conceptList = value;
    }

}
