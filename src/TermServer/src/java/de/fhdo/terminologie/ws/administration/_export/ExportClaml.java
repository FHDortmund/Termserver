/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2014 FH Dortmund: Peter Haas, Robert Muetzner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhdo.terminologie.ws.administration._export;

import clamlBindingXSD.ClaML;
import clamlBindingXSD.Identifier;
import clamlBindingXSD.Label;
import clamlBindingXSD.Meta;
import clamlBindingXSD.Rubric;
import clamlBindingXSD.RubricKind;
import clamlBindingXSD.RubricKinds;
import clamlBindingXSD.SubClass;
import clamlBindingXSD.SuperClass;
import clamlBindingXSD.Title;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.XMLFormatter;
import de.fhdo.terminologie.ws.administration.claml.MetadataDefinition.METADATA_ATTRIBUTES;
import de.fhdo.terminologie.ws.administration.claml.RubricKinds.RUBRICKINDS;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.ListConceptAssociations;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConcepts;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetails;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.ReturnType.Status;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.w3c.dom.Document;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 * (robert.muetzner@fh-dortmund.de)
 * @author Michael Heller
 */
public class ExportClaml
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  //Variablen für ImportStatus
  public static double percentageComplete = 0.0;
  public static String currentTask = "";
  private int countExported = 0;
  //private Search search;
  //private ConceptAssociations association;
  private CodeSystem codeSystem = null;
  private CodeSystemVersion codeSystemVersion = null;
  private ClaML claml = null;
  private ExportCodeSystemContentRequestType request;
  private Map<String, RubricKind> rubricKinds;
  //private HashMap metaDataMap = new HashMap();
  SimpleDateFormat sdfEN;
  long timeStart;
  //org.hibernate.Session hb_session;

  public ExportClaml()
  {
    sdfEN = new SimpleDateFormat("yyyy-MM-dd");
  }

  public ExportCodeSystemContentResponseType export(ExportCodeSystemContentRequestType req)
  {
    countExported = 0;

    request = req;
    claml = new ClaML();
    //search = new Search();
    //association = new ConceptAssociations();

    ExportCodeSystemContentResponseType returnInfos = new ExportCodeSystemContentResponseType();
    returnInfos.setReturnInfos(new ReturnType());
    returnInfos.setExportInfos(new ExportType());

    if (logger.isInfoEnabled())
    {
      logger.info("-----------------------");
      logger.info("Export ClaML gestartet ");
      logger.info("-----------------------");
    }

    timeStart = new Date().getTime();

    String packagename = clamlBindingXSD.ClaML.class.getPackage().getName();
    try
    {
      JAXBContext jc = JAXBContext.newInstance(packagename);

      codeSystem = this.request.getCodeSystem();

      // CodeSystem-Details lesen
      ReturnCodeSystemDetailsRequestType rcsdRequest = new ReturnCodeSystemDetailsRequestType();
      rcsdRequest.setCodeSystem(codeSystem);

      ReturnCodeSystemDetailsResponseType rcsdResponse = new ReturnCodeSystemDetails().ReturnCodeSystemDetails(rcsdRequest, "");

      if (rcsdResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
      {
        //hb_session = HibernateUtil.getSessionFactory().openSession();
        if (request.getExportInfos().isUpdateCheck())
        {

          if (!rcsdResponse.getCodeSystem().getCurrentVersionId().equals(codeSystem.getCodeSystemVersions().iterator().next().getVersionId()))
          {
            request.getCodeSystem().getCodeSystemVersions().iterator().next().setVersionId(rcsdResponse.getCodeSystem().getCurrentVersionId());
            codeSystem = this.request.getCodeSystem();
            // CodeSystem-Details lesen
            rcsdRequest = new ReturnCodeSystemDetailsRequestType();
            rcsdRequest.setCodeSystem(codeSystem);

            rcsdResponse = new ReturnCodeSystemDetails().ReturnCodeSystemDetails(rcsdRequest, "");
          }
        }

        if (rcsdResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
          // Codesystem aus Webservice-Antwort übernehmen
          codeSystem = rcsdResponse.getCodeSystem();

          if (codeSystem != null)
          {
            logger.debug("Codesystem geladen: " + codeSystem.getName());
            logger.debug("Codesystem-Versionen: " + codeSystem.getCodeSystemVersions().size());
            codeSystemVersion = codeSystem.getCodeSystemVersions().iterator().next();
            logger.debug("Codesystem-Version geladen: " + codeSystemVersion.getName());

            // Hilfsvariablen
            rubricKinds = new HashMap<String, RubricKind>();

            // ClaML-Details
            claml.setVersion("2.0.0");

            //Keine direkte Abhängigkeit: VokabularyType aus der Response vorhanden
            //Concepte können auch gefunden werden falls Details fehlschlägt
            // Identifier
            Identifier ident = new Identifier();
            // Herausgeber und OID übernehmen
            ident.setUid(codeSystemVersion.getOid());
            ident.setAuthority(codeSystemVersion.getSource());
            this.claml.getIdentifier().add(ident);

            // Title
            Title title = new Title();
            title.setName(codeSystem.getName());
            title.setDate(sdfEN.format(codeSystem.getInsertTimestamp()));
            title.setContent(codeSystem.getDescription());
            title.setVersion(getClamlVersionFromCS(codeSystem));

            claml.setTitle(title);

            // Metadaten zu einem Codesystem
            claml.getMeta().add(createClaMLMetadata("description",codeSystem.getDescription()));
            claml.getMeta().add(createClaMLMetadata("description_eng",codeSystem.getDescriptionEng()));
            claml.getMeta().add(createClaMLMetadata("website",codeSystem.getWebsite()));
            
            claml.getMeta().add(createClaMLMetadata("version_description",codeSystemVersion.getDescription()));
            if(codeSystemVersion.getInsertTimestamp() != null){
                claml.getMeta().add(createClaMLMetadata("insert_ts", codeSystemVersion.getInsertTimestamp().toString()));
            }else{
                claml.getMeta().add(createClaMLMetadata("insert_ts", ""));
            }
            if(codeSystemVersion.getStatusDate() != null){
                claml.getMeta().add(createClaMLMetadata("status_date", codeSystemVersion.getStatusDate().toString()));
            }else{
                claml.getMeta().add(createClaMLMetadata("status_date", ""));
            }
            if(codeSystemVersion.getExpirationDate() != null){
                claml.getMeta().add(createClaMLMetadata("expiration_date", codeSystemVersion.getExpirationDate().toString()));
            }else{
                claml.getMeta().add(createClaMLMetadata("expiration_date", ""));
            }
            if(codeSystemVersion.getLastChangeDate() != null){
                claml.getMeta().add(createClaMLMetadata("last_change_date", codeSystemVersion.getLastChangeDate().toString()));
            }else{
                claml.getMeta().add(createClaMLMetadata("last_change_date", ""));
            }
            // Konzepte erstellen
            this.createConcepts();

            // RubricKinds (werden bei createConcepts in Map hinzugefügt)
            claml.setRubricKinds(new RubricKinds());
            claml.getRubricKinds().getRubricKind().addAll(rubricKinds.values());

            Marshaller m = jc.createMarshaller();
            //  m.marshal(this.claml, new File("D:/Users/Michael/Documents/Masterprojekt1/x1gex2009/Klassifikationsdateien/exportICD.xml"));
            //ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // ClaML-Datei aus Klassen im Speicher erstellen
            m.marshal(this.claml, bos);

            long diff = (new Date().getTime() - timeStart) / 1000;
            logger.debug("ClaML-Export-Dauer: " + diff);

            try
            {
              DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
              DocumentBuilder builder = null;
              Document doc = null;

              builder = builderFactory.newDocumentBuilder();
              doc = builder.parse(new ByteArrayInputStream(bos.toByteArray()));
              TransformerFactory tf = TransformerFactory.newInstance();
              Transformer trans = tf.newTransformer();
              trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
              StringWriter writer = new StringWriter();
              trans.transform(new DOMSource(doc), new StreamResult(writer));
              String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
              XMLFormatter formatter = new XMLFormatter();
              String formattedXml = formatter.format(output);
              formattedXml = formattedXml.replace("\"", "'");
              formattedXml = formattedXml.replace("&quot;", "\"");
              formattedXml = formattedXml.replace("&amp;", "&");
              formattedXml = formattedXml.replace("&lt;", "<");
              formattedXml = formattedXml.replace("&gt;", ">");
              formattedXml = formattedXml.replace("&apos;", "'");

              // Rückgabe erstellen
              returnInfos.getExportInfos().setFilecontent(formattedXml.getBytes("UTF-8"));
            }
            catch (Exception exi)
            {
              // Rückgabe erstellen
              returnInfos.getExportInfos().setFilecontent(bos.toByteArray());
            }

            returnInfos.getExportInfos().setFormatId(ExportCodeSystemContentRequestType.EXPORT_CLAML_ID);

            returnInfos.getReturnInfos().setCount(countExported);
            returnInfos.getReturnInfos().setMessage(countExported + " Klassen erfolgreich exportiert, Dauer (s): " + diff);

            //returnInfos.setImportInformations(importtype);
            currentTask = "";
            percentageComplete = 0.0;

            returnInfos.getReturnInfos().setStatus(Status.OK);
          }
          else
          {
            logger.debug("[ExportClaml.java] Vokabular nicht gefunden");
            returnInfos.getReturnInfos().setStatus(Status.FAILURE);
            returnInfos.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            returnInfos.getReturnInfos().setMessage("Codesystem konnte nicht gefunden werden!");
          }
        }
        else
        {
          // Fehler: Codesystem kann nicht geladen bzw. gefunden werden
          returnInfos.getReturnInfos().setStatus(Status.FAILURE);
          returnInfos.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          returnInfos.getReturnInfos().setMessage(rcsdResponse.getReturnInfos().getMessage());
        }
      }
      else
      {
        // Fehler: Codesystem kann nicht geladen bzw. gefunden werden
        returnInfos.getReturnInfos().setStatus(Status.FAILURE);
        returnInfos.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        returnInfos.getReturnInfos().setMessage(rcsdResponse.getReturnInfos().getMessage());
      }

    }
    catch (JAXBException ex)
    {
      logger.debug("[ExportClaml.java] " + ex.getMessage());

    }
    finally
    {
      //if (hb_session != null && hb_session.isOpen())
      //  hb_session.close();
    }

    return returnInfos;
  }

  private Meta createClaMLMetadata(String name, String value)
  {
    Meta meta = new Meta();
    meta.setName(name);
    meta.setValue(value);
    return meta;
  }

  private String getClamlVersionFromCS(CodeSystem cs)
  {
    String s = "";

    try
    {
      CodeSystemVersion csv = cs.getCodeSystemVersions().iterator().next();
      s = csv.getName().replaceAll(cs.getName(), "").trim();
    }
    catch (Exception e)
    {
    }

    return s;
  }

  private byte[] fileToByte(File file)
  {
    byte[] data = new byte[(int) file.length()];

    try
    {
      FileInputStream fis = new FileInputStream(file);

      fis.read(data);
      fis.close();
    }
    catch (Exception ex)
    {
      logger.debug("[ExportClaml.java] " + ex.getMessage());
    }
    return data;
  }

  /*public void createVocabularyDetails()
   {
   ReturnCodeSystemDetailsRequestType detailsReq = new ReturnCodeSystemDetailsRequestType();
   detailsReq.setLoginType(this.req.getLogin());
   detailsReq.setVocabulary(codeSystem);
   ReturnCodeSystemDetailsResponseType detailsResp = new ReturnCodeSystemDetailsResponseType();
   detailsResp = this.search.ReturnCodeSystemDetails(detailsReq);
   logger.info("[ExportClaml.java] " + detailsResp.getReturnInformations().getMessage());
   if (detailsResp.getReturnInformations().getStatus() == RETURN_STATUS.OK)
   {
   // System.out.println(detailsResp.getVocabulary().getOid());
   //  System.out.println(detailsResp.getVocabulary().getId());
   // System.out.println(detailsResp.getVocabulary().getDescription());
   }

   }*/
  public void createConcepts()
  {
    ListCodeSystemConceptsRequestType conceptsReq = new ListCodeSystemConceptsRequestType();
    conceptsReq.setCodeSystem(this.codeSystem);
    conceptsReq.setLoginToken(this.request.getLoginToken());

    if (request.getExportParameter() != null && request.getExportParameter().getDateFrom() != null)
    {
      // Datum für Synchronisation hinzufügen
      conceptsReq.setCodeSystemEntity(new CodeSystemEntity());
      conceptsReq.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
      CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
      csev.setStatusVisibilityDate(request.getExportParameter().getDateFrom());
      conceptsReq.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

      logger.debug("Snych-Zeit: " + request.getExportParameter().getDateFrom().toString());
    }
    else
      logger.debug("keine Snych-Zeit angegeben");

    //Date dateDummy = new Date(0); //Fragen ! trick damit etwas geliefert wird!
    //this.codeSystem.setInsertTimestamp(dateDummy);
    //ListCodeSystemConceptsResponseType conceptsResp = new ListCodeSystemConcepts().ListCodeSystemConcepts(conceptsReq, hb_session);
    ListCodeSystemConceptsResponseType conceptsResp = new ListCodeSystemConcepts().ListCodeSystemConcepts(conceptsReq, true, "");
    //!!WIESO HIER KEINE RETURN INFORMATIONS (FALSCH BENANNT)
    logger.debug("[ExportClaml.java] " + conceptsResp.getReturnInfos().getMessage());

    //HashMap hm = new HashMap();
    clamlBindingXSD.Class clazz = null;

    //Durchlaufen der Konzepte
    if (conceptsResp.getReturnInfos().getStatus() == Status.OK)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        Iterator itEntitiy = conceptsResp.getCodeSystemEntity().iterator();
        double classCount = conceptsResp.getCodeSystemEntity().size();
        double aktCount = 0;
        int i = 0;
        String altCode = "";
        while (itEntitiy.hasNext())
        {
          i++;
          CodeSystemEntity cse = (CodeSystemEntity) itEntitiy.next();

          for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
          {
            if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
            {
              CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();
              if (csev.getStatusVisibility() == 1)
              {
                String neuCode = csc.getCode();
                if (csc.getCode() == null || csc.getCode().trim().equals(""))
                {
                  neuCode = i + "";
                }

                //Status aktuallisieren
                aktCount++;
                percentageComplete = aktCount / classCount * 100.0;
                currentTask = neuCode;

                //Neuer Code (Gruppenwechsel)
                if (!neuCode.equals(altCode))
                {
                  altCode = neuCode;
                  //Neue Klasse erstellen
                  clazz = new clamlBindingXSD.Class();

                  createMetaData(csev, clazz, hb_session);

                  //clazz.setCode(evt.getTerm().getCode());
                  clazz.setCode(neuCode);

                  //clazz.setKind(""); TODO chapter, block, category, ...
                  // wenn Metadata ClaML_ClassKind heißt, dann ist "chapter" etc. darin gespeichert
                  claml.getClazz().add(clazz);
                  countExported++;
                  //In HashMap schreiben
                  //hm.put(csev.getVersionId(), neuCode);
                }

                // Term hinzufügen
                /*Rubric rubric = new Rubric();
                 Label label = new Label();
                 label.getContent().add(csc.getTerm());
                 rubric.getLabel().add(label);

                 if (csc.getIsPreferred())
                 {
                 rubric.setKind(RUBRICKINDS.preferred.getCode());
                 addRubricKind(RUBRICKINDS.preferred.getCode());
                 }
                 clazz.getRubric().add(rubric);*/
                if (csc.getIsPreferred())
                  addRubricElement(RUBRICKINDS.preferred, csc.getTerm(), clazz);
                else
                  addRubricElement(null, csc.getTerm(), clazz);

                addRubricElement(RUBRICKINDS.note, csc.getDescription(), clazz);

                // Weitere Attribute in Metadaten speichern
                createMetadata(METADATA_ATTRIBUTES.hints.getCode(), csc.getHints(), clazz);
                createMetadata(METADATA_ATTRIBUTES.meaning.getCode(), csc.getMeaning(), clazz);
                createMetadata(METADATA_ATTRIBUTES.termAbbrevation.getCode(), csc.getTermAbbrevation(), clazz);

                if (csev.getIsLeaf() != null)
                  createMetadata(METADATA_ATTRIBUTES.isLeaf.getCode(), csev.getIsLeaf().toString(), clazz);
                if (csev.getMajorRevision() != null)
                  createMetadata(METADATA_ATTRIBUTES.majorRevision.getCode(), csev.getMajorRevision().toString(), clazz);
                if (csev.getMinorRevision() != null)
                  createMetadata(METADATA_ATTRIBUTES.minorRevision.getCode(), csev.getMinorRevision().toString(), clazz);
                if (csev.getStatusVisibility() != null)
                  createMetadata(METADATA_ATTRIBUTES.status.getCode(), csev.getStatusVisibility().toString(), clazz);
                if (csev.getStatusVisibilityDate() != null)
                  createMetadata(METADATA_ATTRIBUTES.statusDate.getCode(), csev.getStatusVisibilityDate().toString(), clazz);

                //createAssociation(cse, clazz, hm, rubric, csev, csc);
                createAssociation(cse, clazz, null, csev, csc, hb_session);

                hb_session.clear();
              }
            }
          }
        }
      }
      catch (Exception ex)
      {
        logger.error("Fehler in 'createMetaData': " + ex.getLocalizedMessage());

        ex.printStackTrace();
      }
      finally
      {
        logger.debug("Schließe Hibernate-Session (ExportClaml.java)");
        hb_session.close();
      }
    }
  }

  private void createMetadata(String name, String value, clamlBindingXSD.Class clazz)
  {
    if (value != null && value.length() > 0 && name != null && name.length() > 0)
    {
      Meta m = new Meta();
      m.setName(name);
      m.setValue(value);
      clazz.getMeta().add(m);
    }
  }

  private void addRubricElement(RUBRICKINDS kind, String value, clamlBindingXSD.Class clazz)
  {
    if (value != null && value.length() > 0)
    {
      Rubric rubric = new Rubric();
      Label label = new Label();
      label.getContent().add(value);
      rubric.getLabel().add(label);

      if (kind != null)
      {
        RubricKind rk = new RubricKind();
        rk.setName(kind.getCode());
        rubric.setKind(rk);
      }
      //rubric.setKind(kind.getCode());

      clazz.getRubric().add(rubric);

      if (kind != null)
      {
        if (rubricKinds.containsKey(kind.getCode()) == false)
        {
          RubricKind rc = new RubricKind();
          rc.setInherited("false");
          rc.setName(kind.getCode());
          rubricKinds.put(kind.getCode(), rc);
        }
      }
    }
  }

  private void addRubricKind(String kind)
  {
    if (rubricKinds.containsKey(kind) == false)
    {
      RubricKind rc = new RubricKind();
      rc.setInherited("false");
      rc.setName(kind);
      rubricKinds.put(kind, rc);
    }
  }

  public void createMetaData(CodeSystemEntityVersion csev, clamlBindingXSD.Class clazz, Session hb_session)
  {
    /*if (logger.isInfoEnabled())
     {
     logger.info("------------------------");
     logger.info("ListMetaData gestartet");
     logger.info("------------------------");
     }*/

    String hql = "from CodeSystemMetadataValue md ";
    hql += " join fetch md.metadataParameter mp ";
    hql += " where codeSystemEntityVersionId=" + csev.getVersionId();

    List<CodeSystemMetadataValue> metadataList = hb_session.createQuery(hql).list();

    if (metadataList != null)
    {
      for (CodeSystemMetadataValue metadata : metadataList)
      {
        if (metadata.getMetadataParameter() == null
                || metadata.getMetadataParameter().getParamName() == null
                || metadata.getMetadataParameter().getParamName().length() == 0)
        {
          continue;
        }

        if (metadata.getMetadataParameter().getParamName().equals("ClaML_ClassKind"))
        {
          // ClassKind setzen, der in den Metadaten gespeichert wird
          clazz.setKind(metadata.getParameterValue());
        }
        else
        {
          Meta m = new Meta();
          m.setName(metadata.getMetadataParameter().getParamName());
          m.setValue(metadata.getParameterValue());

          clazz.getMeta().add(m);
        }
      }
    }
  }

  public void createAssociation(CodeSystemEntity cse, clamlBindingXSD.Class clazz, HashMap hashMap,
          CodeSystemEntityVersion csev, CodeSystemConcept csc, Session hb_session)
  {
    /*if (csc != null && csc.getIsPreferred())
     {
     RubricKind rk = new RubricKind();
     rk.setName("preferred");
     rubric.setKind(rk);
     }*/

    //TODO:Hier nun finden der Associations (geht dies nur über List? ASSOCIATION oder wäre hier eigentlich TD richtig geht nicht!)
    ListConceptAssociationsRequestType conceptAssocReq = new ListConceptAssociationsRequestType();
    conceptAssocReq.setCodeSystemEntity(cse);
    conceptAssocReq.setLoginToken(this.request.getLoginToken());
    conceptAssocReq.setDirectionBoth(true); // beide Richtungen sind wichtig!
    //for (int i = 0; i < 2; ++i)
    {
      /*if(i == 0)
       conceptAssocReq.setReverse(false);
       else if(i == 1) conceptAssocReq.setReverse(true);
       else break;*/

      //conceptAssocReq.setReverse(true);
      //ListConceptAssociationsResponseType conceptAssocResp = new ListConceptAssociations().ListConceptAssociations(conceptAssocReq, hb_session);
      ListConceptAssociationsResponseType conceptAssocResp = new ListConceptAssociations().ListConceptAssociations(conceptAssocReq, hb_session, "");
      logger.info("[ExportClaml.java] " + conceptAssocResp.getReturnInfos().getMessage());

      if (conceptAssocResp.getReturnInfos().getStatus() == Status.OK)
      {
        for (CodeSystemEntityVersionAssociation cseva : conceptAssocResp.getCodeSystemEntityVersionAssociation())
        {
          if (cseva.getAssociationKind().intValue() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
          {
            // Taxonomische Verbindung
            long leftId = 0;
            if (cseva.getLeftId() != null)
              leftId = cseva.getLeftId().longValue();

            if (leftId > 0)
            {
              if (csev.getVersionId().longValue() != leftId)
              {
                //SuperClass sc = null;

                if (cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null
                        && cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts() != null
                        && cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().size() > 0)
                {
                  // SuperClass hinzufügen
                  CodeSystemConcept csc_super = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().iterator().next();
                  if (csc_super != null)
                  {
                    SuperClass sc = new SuperClass();
                    sc.setCode(csc_super.getCode());
                    clazz.getSuperClass().add(sc);
                  }
                }

                // Übergeordnete Verbindung
                /*Object o = hashMap.get(leftId);
                 if (o != null)
                 {
                 sc = new SuperClass();
                 sc.setCode(o.toString());
                 clazz.getSuperClass().add(sc);
                 }*/
              }
              else
              {
                // Kinder (Subelemente)
                SubClass subClass = new SubClass();
                if (cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null
                        && cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts() != null
                        && cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().size() > 0)
                {
                  // SubClass hinzufügen
                  CodeSystemConcept cscSubClass = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().iterator().next();
                  subClass.setCode(cscSubClass.getCode());
                  clazz.getSubClass().add(subClass);
                }
              }
            }
          }
          /*else
           {
           // Andere Verbindung (Ontologisch)
           if (csc.getIsPreferred() == false)
           {
           RubricKind rk = new RubricKind();
           rk.setName(cseva.getAssociationType().getForwardName());
           rubric.setKind(rk);

           addRubricKind(cseva.getAssociationType().getForwardName());
           }
           }*/
        }
      }
    }
  }
}
