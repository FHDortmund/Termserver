package de.fhdo.mi.infobutton.service;

import com.google.gson.Gson;
import de.fhdo.mi.infobutton.service.types.CodeSystem;
import de.fhdo.mi.infobutton.service.types.CodedValue;
import de.fhdo.mi.infobutton.service.types.KnowledgeRequest;
import de.fhdo.mi.infobutton.service.types.KnowledgeResponse;
import de.fhdo.mi.infobutton.service.types.ParameterHelper;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.Definitions.ASSOCIATION_KIND;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.ws.conceptAssociation.ListConceptAssociations;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConcepts;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.hl7.AggregateKnowledgeResponse;
import org.hl7.CategoryType;
import org.hl7.ContentType;
import org.hl7.DateTimeType;
import org.hl7.EntryType;
import org.hl7.FeedType;
import org.hl7.LinkType;
import org.hl7.PersonType;
import org.hl7.UriType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class InfobuttonServiceRest
{

  private static Logger logger = Logger.getLogger(InfobuttonServiceRest.class);

  public InfobuttonServiceRest()
  {
  }

  public String processRequest(MultivaluedMap<String, String> parameters)
          throws Exception
  {
    logger.info("InfobuttonService TERMSERVER - processRequest");

    KnowledgeRequest request = new KnowledgeRequest(parameters);
    AggregateKnowledgeResponse response = new AggregateKnowledgeResponse();

    if (parameters == null || parameters.containsKey("mainSearchCriteria.v.c") == false)
    {
      //FeedType feed = new FeedType();
      //response.getFeed().add(null)
      //throw new Exception("Parameter 'mainSearchCriteria.v.c' not found!");
      logger.warn("Parameter 'mainSearchCriteria.v.c' not found!");
      return createResponse(response, request);
    }

    //throw new Exception("Test");
    // hier die Logik hinterlegen und den Feed zusammenbauen
    // MeSH Codes extrahieren
    List<CodedValue> codedValues = new LinkedList<>(); //umlsProxy.umlsMeshMapping(umlsCodes);

    if (parameters.containsKey("mainSearchCriteria.v.c"))
    {
      codedValues.add(getCodedValue(parameters.getFirst("mainSearchCriteria.v.c"),
              parameters.getFirst("mainSearchCriteria.v.dn"),
              parameters.getFirst("mainSearchCriteria.v.cs"), true));

      // Weiter suchen nach c1, c2 usw...
      boolean found;
      int index = 1;
      do
      {
        found = false;
        if (parameters.containsKey("mainSearchCriteria.v.c" + index))
        {
          codedValues.add(getCodedValue(parameters.getFirst("mainSearchCriteria.v.c" + index),
                  parameters.getFirst("mainSearchCriteria.v.dn" + index),
                  parameters.getFirst("mainSearchCriteria.v.cs" + index), true));

          found = true;
          index++;
        }
      }
      while (found);

    }

    // Antwort zusammenbauen
    FeedType feed = new FeedType();
    feed.setUpdated(new DateTimeType());
    feed.getUpdated().setValue(getXMLGregorianCalendarNow());
    feed.setTitle(ParameterHelper.createText("Terminology Server - knowledge service"));
    PersonType author = new PersonType();
    UriType uri = new UriType();
    uri.setValue("http://www.wiki.mi.fh-dortmund.de/cts2/");
    author.getNameOrUriOrEmail().add(new org.hl7.ObjectFactory().createPersonTypeName("FH Dortmund"));
    author.getNameOrUriOrEmail().add(new org.hl7.ObjectFactory().createPersonTypeUri(uri));

    // Beziehungen suchen zu den CodedValues
    feed.getEntry().addAll(getOntologicalInfos(codedValues));

//    for (Leitlinie leitlinie : leitlinien)
//    {
//      if (leitlinie.getQuelle().equalsIgnoreCase("awmf"))
//        feedAWMF.getEntry().add(createFeedFromGuideline(leitlinie));
//      else if (leitlinie.getQuelle().equalsIgnoreCase("dgk") || leitlinie.getQuelle().equalsIgnoreCase("dkg"))
//      {
//        leitlinie.setQuelle("DGK");
//        feedDGK.getEntry().add(createFeedFromGuideline(leitlinie));
//      }
//    }
//
//    if (feedAWMF.getEntry().size() > 0)
//      response.getFeed().add(feedAWMF);
    if (feed.getEntry().size() > 0)
      response.getFeed().add(feed);

    return createResponse(response, request);
  }

  private List<EntryType> getOntologicalInfos(List<CodedValue> codedValues)
  {
    logger.debug("getOntologicalInfos from codedValues...");
    List<EntryType> list = new LinkedList<EntryType>();

    for (CodedValue codedValue : codedValues)
    {
      CodeSystemEntity codeSystemEntity = findConcept(codedValue);

      if (codeSystemEntity != null)
      {
        long versionId = codeSystemEntity.getCurrentVersionId();

        for (CodeSystemEntityVersion csev : codeSystemEntity.getCodeSystemEntityVersions())
        {
          if (csev.getVersionId().longValue() == versionId)
          {
            CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];
            list.add(getEntryFromConcept(csev, csc, codedValue, -1, versionId, null));

            // Jetzt weitere Beziehungen suchen
            List<CodeSystemEntityVersionAssociation> associations = findConceptRelations(versionId);

            if (associations != null)
            {
              logger.debug("Beziehungen gefunden, Anzahl: " + associations.size());

              for (CodeSystemEntityVersionAssociation association : associations)
              {
                CodeSystemEntityVersion csev2 = null;
                CodeSystemConcept csc2 = null;

                if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
                {
                  csev2 = association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1();
                  csc2 = (CodeSystemConcept) csev2.getCodeSystemConcepts().toArray()[0];
                }
                if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                {
                  csev2 = association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();
                  csc2 = (CodeSystemConcept) csev2.getCodeSystemConcepts().toArray()[0];
                }

//                String title = "Beziehung";
//                if(association.getAssociationKind() == ASSOCIATION_KIND.CROSS_MAPPING.getCode())
//                  title = "Cross-Mapping";
//                else if(association.getAssociationKind() == ASSOCIATION_KIND.LINK.getCode())
//                  title = "Link";
//                else if(association.getAssociationKind() == ASSOCIATION_KIND.ONTOLOGY.getCode())
//                  title = "Ontologische Beziehung";
//                else if(association.getAssociationKind() == ASSOCIATION_KIND.TAXONOMY.getCode())
//                  title = "Taxonomische Beziehung";
                EntryType entry = getEntryFromConcept(csev2, csc2, codedValue, association.getAssociationKind(), versionId, association);
                list.add(entry);
              }
            }

            break;
          }
        }
      }

    }

    return list;
  }

  private EntryType getEntryFromConcept(CodeSystemEntityVersion entityVersion, CodeSystemConcept concept, CodedValue codedValue, int associationKind, long versionId, CodeSystemEntityVersionAssociation association)
  {
    EntryType entry = new EntryType();

    // Titel
    String title = "Beziehung";
    if (associationKind == ASSOCIATION_KIND.CROSS_MAPPING.getCode())
    {
      title = "Cross-Mapping";
      addCategoryToEntry(entry, "type", "cross_mapping");
    }
    else if (associationKind == ASSOCIATION_KIND.LINK.getCode())
    {
      title = "Link";
      addCategoryToEntry(entry, "type", "link");
    }
    else if (associationKind == ASSOCIATION_KIND.ONTOLOGY.getCode())
    {
      title = "Ontologische Beziehung";
      addCategoryToEntry(entry, "type", "ontology");
    }
    else if (associationKind == ASSOCIATION_KIND.TAXONOMY.getCode())
    {
      title = "Taxonomische Beziehung";
      addCategoryToEntry(entry, "type", "taxonomy");
    }
    else if(associationKind == -1)
    {
      title = "Konzept-Informationen";
      addCategoryToEntry(entry, "type", "concept_info");
    }
    
    entry.setTitle(ParameterHelper.createText(title));

    // Published
    try
    {
      DateTimeType dt = new DateTimeType();
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTime(entityVersion.getEffectiveDate());
      dt.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
      entry.setPublished(dt);
    }
    catch (Exception e)
    {

    }

//    if (leitlinie.getLeitlinieLinks() != null)
//    {
//      for (LeitlinieLink link : leitlinie.getLeitlinieLinks())
//      {
//        LinkType linkType = new org.hl7.ObjectFactory().createLinkType();
//        linkType.setHref(link.getLink());
//        linkType.setTitle(link.getName());
//        entry.getLink().add(linkType);
//      }
//    }
    entry.setSummary(ParameterHelper.createText(concept.getTerm()));
    //entry.setSource(ParameterHelper.createText(concept.));

    

    addCategoryToEntry(entry, "source_code", codedValue.getCode());
    addCategoryToEntry(entry, "source_cs_oid", codedValue.getCodeSystem().getOid());

    addCategoryToEntry(entry, "concept_version_id", concept.getCodeSystemEntityVersionId() + "");
    addCategoryToEntry(entry, "concept_code", concept.getCode());
    addCategoryToEntry(entry, "concept_description", concept.getDescription());
    addCategoryToEntry(entry, "concept_hints", concept.getHints());
    addCategoryToEntry(entry, "concept_term", concept.getTerm());
    addCategoryToEntry(entry, "concept_termAbbrevation", concept.getTermAbbrevation());
    
    if(association != null)
    {
      if(association.getAssociationType() != null && association.getAssociationType().getForwardName() != null
              && association.getAssociationType().getForwardName().length() > 0)
      {
        addCategoryToEntry(entry, "relation_name", association.getAssociationType().getForwardName());
      }
    }

    if (concept.getIsPreferred() != null)
      addCategoryToEntry(entry, "concept_preferred", concept.getIsPreferred().booleanValue() ? "1" : "0");

    return entry;
  }

  private void addCategoryToEntry(EntryType entry, String categoryName, String value)
  {
    if (value == null || value.length() == 0)
      return;

    CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
    category.setTerm(value);
    category.setScheme(categoryName);
    entry.getCategory().add(category);
  }

  private List<CodeSystemEntityVersionAssociation> findConceptRelations(long conceptVersionId)
  {
    if (conceptVersionId <= 0)
    {
      return null;
    }

    logger.debug("find findConceptRelations version-id: " + conceptVersionId);

    ListConceptAssociations lca = new ListConceptAssociations();
    ListConceptAssociationsRequestType request = new ListConceptAssociationsRequestType();

    request.setDirectionBoth(true);

    // Code zur Suche hinzufügen
    request.setCodeSystemEntity(new CodeSystemEntity());
    request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setVersionId(conceptVersionId);
    request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

    // Suche durchführen
    ListConceptAssociationsResponseType lcaResponse = lca.ListConceptAssociations(request, "");

    if (lcaResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      // excpect only 1 result (search with oid)
      return lcaResponse.getCodeSystemEntityVersionAssociation();

    }

    logger.debug("no associations found");

    return null;
  }

  private CodeSystemEntity findConcept(CodedValue codedValue)
  {
    if (codedValue == null
            || codedValue.getCodeSystem() == null || codedValue.getCodeSystem().getOid() == null
            || codedValue.getCode() == null)
    {
      return null;
    }

    String oid = codedValue.getCodeSystem().getOid();
    String code = codedValue.getCode();
    logger.debug("find concept with oid: " + oid + ", code: " + code);

    ListCodeSystemConcepts lcsc = new ListCodeSystemConcepts();
    ListCodeSystemConceptsRequestType request = new ListCodeSystemConceptsRequestType();

    request.setCodeSystem(new de.fhdo.terminologie.db.hibernate.CodeSystem());

    // OID zur Suche hinzufügen
    CodeSystemVersion csv = new CodeSystemVersion();
    csv.setOid(oid);  // search by oid
    request.getCodeSystem().getCodeSystemVersions().add(csv);

    // Code zur Suche hinzufügen
    request.setCodeSystemEntity(new CodeSystemEntity());
    request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    CodeSystemConcept csc = new CodeSystemConcept();
    csc.setCode(code);
    csev.getCodeSystemConcepts().add(csc);
    request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

    // Suche durchführen
    ListCodeSystemConceptsResponseType lcscResponse = lcsc.ListCodeSystemConcepts(request, true, "");

    if (lcscResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      // excpect only 1 result (search with oid)
      List<CodeSystemEntity> cseList = lcscResponse.getCodeSystemEntity();
      for (CodeSystemEntity cse : cseList)
      {
        logger.debug("concept found with versionId: " + cse.getCurrentVersionId());
        return cse;
      }
    }

    logger.debug("concept not found");

    return null;
  }

  public static CodedValue getCodedValue(String code, String value, String codeSystemOID, boolean mainSearchCriteria)
  {
    CodedValue cv = new CodedValue();
    cv.setCode(code);
    cv.setValue(value);
    cv.setCodeSystem(new CodeSystem(codeSystemOID, ""));

    logger.debug("Code parametriert: " + code + ", Value: " + value + ", Codesystem: " + codeSystemOID);
    //cv.setCodeSystemOID(codeSystemOID);
    //cv.setMainSearchCriteria(mainSearchCriteria);

//    if (OIDHelper.isICD10(codeSystemOID))
//    {
//      cv.setTerminologyType(CodedValue.TERMINOLOGY.ICD10);
//    }
    return cv;
  }

//  private EntryType createFeedFromGuideline(Leitlinie leitlinie)
//  {
//    EntryType entry = new EntryType();
//
//    // Titel
//    entry.setTitle(ParameterHelper.createText(leitlinie.getName()));
//
//    // Published
//    try
//    {
//      DateTimeType dt = new DateTimeType();
//      GregorianCalendar calendar = new GregorianCalendar();
//      calendar.setTime(leitlinie.getStand());
//      dt.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
//      entry.setPublished(dt);
//    }
//    catch (Exception e)
//    {
//
//    }
//
//    if (leitlinie.getLeitlinieLinks() != null)
//    {
//      for (LeitlinieLink link : leitlinie.getLeitlinieLinks())
//      {
//        LinkType linkType = new org.hl7.ObjectFactory().createLinkType();
//        linkType.setHref(link.getLink());
//        linkType.setTitle(link.getName());
//        entry.getLink().add(linkType);
//      }
//    }
//
//    entry.setSummary(ParameterHelper.createText(leitlinie.getInhalt()));
//    entry.setSource(ParameterHelper.createText(leitlinie.getQuelle()));
//
//    // weitere Attribute
//    if (leitlinie.getAwmfRegNr() != null && leitlinie.getAwmfRegNr().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getAwmfRegNr());
//      category.setScheme("AWMF Reg-Nr");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getStufe() != null && leitlinie.getStufe().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getStufe());
//      category.setScheme("Entwicklungsstufe");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getFachgesellschaften() != null && leitlinie.getFachgesellschaften().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getFachgesellschaften());
//      category.setScheme("Fachgesellschaften");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getAdressaten() != null && leitlinie.getAdressaten().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getAdressaten());
//      category.setScheme("Adressaten");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getPatientenzielgruppe() != null && leitlinie.getPatientenzielgruppe().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getPatientenzielgruppe());
//      category.setScheme("Patientenzielgruppe");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getVersorgungsbereich() != null && leitlinie.getVersorgungsbereich().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getVersorgungsbereich());
//      category.setScheme("Versorgungsbereich");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getGruendeThemenwahl() != null && leitlinie.getGruendeThemenwahl().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getGruendeThemenwahl());
//      category.setScheme("Themenwahl-Gründe");
//      entry.getCategory().add(category);
//    }
//
//    if (leitlinie.getZielorientierung() != null && leitlinie.getZielorientierung().length() > 0)
//    {
//      CategoryType category = new org.hl7.ObjectFactory().createCategoryType();
//      category.setTerm(leitlinie.getZielorientierung());
//      category.setScheme("Zielorientierung");
//      entry.getCategory().add(category);
//    }
//
//    return entry;
//  }
  public XMLGregorianCalendar getXMLGregorianCalendarNow()
          throws DatatypeConfigurationException
  {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
    XMLGregorianCalendar now
            = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    return now;
  }

  private String createResponse(AggregateKnowledgeResponse response, KnowledgeRequest request)
  {

    logger.info("InfobuttonService - createResponse");

    try
    {

      if (response == null)
        return "Reponse is null";
      //if (response.getFeed() == null || response.getFeed().size() == 0)
      //  return "no feeds found";

      logger.debug("Feeds: " + response.getFeed().size());

      //KnowledgeResponse response = new KnowledgeResponse();
      //response.setKnowledgeResponse(aggregateResponse);
      KnowledgeRequest.RESPONSE_FORMAT responseFormat = request.getResponseFormat();
      logger.debug("Format: " + responseFormat);

      JAXBContext ctx = JAXBContext.newInstance(KnowledgeResponse.class);
      Marshaller m = ctx.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      JAXBElement jaxb = new JAXBElement<AggregateKnowledgeResponse>(new QName("http://www.w3.org/2005/Atom", "aggregateKnowledgeResponse"), AggregateKnowledgeResponse.class, response);

      StringWriter sw = new StringWriter();
      m.marshal(jaxb, sw);

      String finalStr = sw.toString();

      if (responseFormat == KnowledgeRequest.RESPONSE_FORMAT.APPLICATION_JSON)
      {
        Gson gson = new Gson();
        finalStr = gson.toJson(response);
      }

      return finalStr;
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Erstellen der Antwort.", ex);
      return "Error: " + ex.getLocalizedMessage();
    }

    //return "error";
  }
}
