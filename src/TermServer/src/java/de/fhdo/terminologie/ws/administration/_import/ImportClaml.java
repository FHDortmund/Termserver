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
package de.fhdo.terminologie.ws.administration._import;

import clamlBindingXSD.Label;
import clamlBindingXSD.Meta;
import clamlBindingXSD.Rubric;
import clamlBindingXSD.RubricKind;
import de.fhdo.logging.Logger4j;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;

import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import de.fhdo.terminologie.ws.administration.claml.MetadataDefinition.METADATA_ATTRIBUTES;
import de.fhdo.terminologie.ws.administration.claml.RubricKinds.RUBRICKINDS;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Michael
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) Erweiterungen,
 * Anpassung an neue Version
 */
public class ImportClaml
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  //Variablen für ImportStatus
  public static boolean isRunning = false;
  public static double percentageComplete = 0.0;
  public static String currentTask = "";
  private CodeSystem codeSystem;
  //Attribute für CreateAssociationType
  private AssociationType assoctypeTaxonomy;
  private HashMap assoctypeHashmap = new HashMap();
  private HashMap ccatresptHashmap = new HashMap();
  private CreateConceptAssociationTypeResponseType ccatrespt;
  private CreateConceptAssociationTypeResponseType ccatresptTaxonomy;
  //Attribute für CreateAllConcepts
  private CreateConceptResponseType ccsResponse;
  private HashMap referenceMap;
  private HashMap metaDataMap;
  private String loginToken;
  private int aktCount = 0;
  private org.hibernate.Session hb_session;
  private int countImported = 0;
  private AuthenticateInfos loginInfoType;

  public ImportClaml(AuthenticateInfos _loginInfoType)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportClaml Constructor ======");

    loginInfoType = _loginInfoType;
  }

  /**
   *
   * @param request
   */
  public void startImport(ImportCodeSystemRequestType request) throws Exception
  {
    StaticStatus.importTotal = 0;
    StaticStatus.importCount = 0;
    StaticStatus.importRunning = true;
    StaticStatus.exportRunning = false;
    StaticStatus.cancel = false;

    isRunning = true;

    this.loginToken = request.getLoginToken();
    this.codeSystem = request.getCodeSystem();
    this.referenceMap = new HashMap();
    this.metaDataMap = new HashMap();

    // Hibernate-Block, Session öffnen
    hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try // try-catch-Block zum Abfangen von Hibernate-Fehlern
    {
      logger.debug("Oeffne Datei...");
      byte[] bytes = request.getImportInfos().getFilecontent();
      //ByteArrayDataSource bads = new ByteArrayDataSource(request.getImportInfos().getFilecontent(), "text/xml; charset=UTF-8");

      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);

      logger.debug("loadClamlXML()");
      this.loadClamlXML(is);

      if (StaticStatus.cancel)
      {
        hb_session.getTransaction().rollback();
      }
      else
        hb_session.getTransaction().commit();
    }
    catch (Exception ex)
    {
      logger.error("ImportClaml error: " + ex.getLocalizedMessage());
      //ex.printStackTrace();

      logger.debug(ex.getMessage());
      try
      {
        hb_session.getTransaction().rollback();
        logger.info("[ImportClaml.java] Rollback durchgeführt!");
      }
      catch (Exception exRollback)
      {
        logger.info(exRollback.getMessage());
        logger.info("[ImportClaml.java] Rollback fehlgeschlagen!");
      }

      LoggingOutput.outputException(ex, this);

      throw ex;
    }
    finally
    {
      currentTask = "";
      percentageComplete = 0.0;

      // Session schließen
      hb_session.close();

      isRunning = false;
    }
  }

  /**
   * Importiert die ClaML-XML Datei
   */
  private void loadClamlXML(InputStream is) throws Exception
  {

    logger.debug("create JAXBContext");

    clamlBindingXSD.Class clazz = null;
    clamlBindingXSD.Rubric rubi = null;
    clamlBindingXSD.RubricKinds rks = new clamlBindingXSD.RubricKinds();

    /*String packagename = clamlBindingXSD.ClaML.class.getPackage().getName();
     JAXBContext jc = JAXBContext.newInstance(packagename);
     Unmarshaller u = jc.createUnmarshaller();*/
    // First create a new XMLInputFactory
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

    // Setup a new eventReader
    XMLEventReader eventReader = inputFactory.createXMLEventReader(is);

    // Read the XML document
    logger.debug("Analyze data...");

    //Attribute für CreateCodeSystem
    String authority = "";
    String uid = "";

    int countEvery = 0;

    while (eventReader.hasNext())
    {
      if (StaticStatus.cancel)
        break;

      XMLEvent event = eventReader.nextEvent();

      if (event.isStartElement())
      {
        StartElement startElement = event.asStartElement();
        String startElementName = startElement.getName().toString();
        //logger.debug("Start-Element: " + startElementName);
        //logger.debug("Is-End-Element: " + startElement.isEndElement());

        if (startElementName.equals("Title"))
        {
          Date datum = new Date();
          String title = "";
          String versionName = "";

          // We read the attributes from this tag and add the date attribute to our object
          Iterator<Attribute> attributes = startElement.getAttributes();
          while (attributes.hasNext())
          {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("name"))
            {
              title = attribute.getValue();
            }
            if (attribute.getName().toString().equals("version"))
            {
              versionName = attribute.getValue();
            }
            if (attribute.getName().toString().equals("date"))
            {
              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
              datum = sdf.parse(attribute.getValue());
            }
          }

          //String description = "";
          // TODO hier: Fehler
          event = eventReader.nextEvent();
          String description = event.asCharacters().getData();
          logger.debug("description: " + description);

          this.createCodeSystem(title, uid, versionName, datum, authority, description);
          /*if (paramter.isCreateNewVocabulary())
           {
           this.createCodeSystem(title, uid, versionName, authority);
           }
           else
           {
           this.maintainVocabularyVersion();
           }*/
        }
        else if (startElementName.equals("Identifier"))
        {
          // We read the attributes from this tag and add the date attribute to our object
          Iterator<Attribute> attributes = startElement.getAttributes();
          while (attributes.hasNext())
          {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("authority"))
            {
              authority = attribute.getValue();
            }
            if (attribute.getName().toString().equals("uid"))
            {
              uid = attribute.getValue();
            }
          }

        }
        else if (startElementName.equals("RubricKind"))
        {
          clamlBindingXSD.RubricKind rk = new clamlBindingXSD.RubricKind();

          // We read the attributes from this tag and add the date attribute to our object
          Iterator<Attribute> attributes = startElement.getAttributes();
          while (attributes.hasNext())
          {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("name"))
            {
              rk.setName(attribute.getValue());
              rks.getRubricKind().add(rk);
            }
          }

        }

        //  if (startElement.getName().toString().equals("Class") || startElement.getName().toString().equals("Modifier") ||startElement.getName().toString().equals("Class") || startElement.getName().toString().equals("ModifierClass") ) {
        else if (startElementName.equals("Class"))
        {
          clazz = new clamlBindingXSD.Class();

          // We read the attributes from this tag and add the date attribute to our object
          Iterator<Attribute> attributes = startElement.getAttributes();
          while (attributes.hasNext())
          {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("code"))
            {
              // System.out.println("CODE:  " + attribute.getValue().toString());
              clazz.setCode(attribute.getValue());

            }
            if (attribute.getName().toString().equals("kind"))
            {
              clazz.setKind(attribute.getValue());
              // System.out.println(clazz.getKind());
            }
          }

        }
        else if (startElementName.equals("Rubric"))
        {
          if (clazz != null)
          {
            rubi = new clamlBindingXSD.Rubric();
            // We read the attributes from this tag and add the date attribute to our object
            Iterator<Attribute> attributes = startElement.getAttributes();
            while (attributes.hasNext())
            {
              Attribute attribute = attributes.next();
              if (attribute.getName().toString().equals("kind"))
              {
                rubi.setKind(attribute.getValue());
              }
            }
            clazz.getRubric().add(rubi);
          }

        }
        else if (startElementName.equals("Label"))
        {
          if (rubi != null)
          {
            event = eventReader.nextEvent();
            //if(event.isCharacters()){
            clamlBindingXSD.Label l = new clamlBindingXSD.Label();
            //l.getContent().add(event.asCharacters().getData());
            if(event.isCharacters())
            {
              l.setvalue(event.asCharacters().getData());
              rubi.getLabel().add(l);
            }
            else
            {
              logger.warn("ClaML Element Label contains no characters, event-type: " + event.getEventType() + ", str: " + event.toString());
              //l.setvalue(event.toString());
            }
            
            continue;
            //}
          }

        }
        else if (startElementName.equals("Fragment"))
        {
          if (rubi != null)
          {
            if (event.isEndElement() == false)
            {
              event = eventReader.nextEvent();
              
              if(event.isEndElement() == false)
              {
                clamlBindingXSD.Label l = new clamlBindingXSD.Label();
                //l.getContent().add(event.asCharacters().getData());
                l.setvalue(event.asCharacters().getData());
                rubi.getLabel().add(l);
              }
              else logger.debug("kein Text, da End-Element");
            }
            continue;
          }

        }
        else if (startElementName.equals("SuperClass"))
        {
          if (clazz != null)
          {
            clamlBindingXSD.SuperClass sc = new clamlBindingXSD.SuperClass();
            Iterator<Attribute> attributes = startElement.getAttributes();
            while (attributes.hasNext())
            {
              Attribute attribute = attributes.next();
              if (attribute.getName().toString().equals("code"))
              {
                sc.setCode(attribute.getValue());
              }
            }

            clazz.getSuperClass().add(sc);
          }

        }
        else if (startElementName.equals("Meta"))
        {
          if (clazz != null)
          {
            Meta meta = new Meta();
            Iterator<Attribute> attributes = startElement.getAttributes();
            while (attributes.hasNext())
            {
              Attribute attribute = attributes.next();
              if (attribute.getName().toString().equals("name"))
              {
                meta.setName(attribute.getValue());
              }
              if (attribute.getName().toString().equals("value"))
              {
                meta.setValue(attribute.getValue());
              }
            }
            clazz.getMeta().add(meta);
          }
        }

      } // End start element

      if (event.isEndElement())
      {
        EndElement endElement = event.asEndElement();
        //  if (endElement.getName().toString().equals("Class")||endElement.getName().toString().equals("Modifier")||endElement.getName().toString().equals("ModifierClass")) {
        if (endElement.getName().toString().equals("Class"))
        {
          //System.out.println(clazz.getCode());
          // TODO rm
          //if (clazz.getKind() == null || paramter.getClasskinds().indexOf(clazz.getKind().toString()) >= 0)
          {
            // Jetzt Konzept erstellen
            this.CreateSingleConcept(clazz);
            if (clazz.getMeta() != null && clazz.getMeta().size() > 0)
            {
              this.createMetaData(clazz);
            }
          }
        }
      }
      if (event.isEndElement())
      {
        EndElement endElement = event.asEndElement();
        if (endElement.getName().toString().equals("RubricKinds"))
        {
          //CreateAssociationType (Unterklasse,Oberklasse)
          this.assoctypeTaxonomy = this.CreateAssociationType("ist Oberklasse von", "ist Unterklasse von");

          logger.info(this.ccatrespt.getReturnInfos().getMessage());
          if (this.ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
          {
            // System.out.println("ID: " + ccatrespt.getEntity().getEntityVersionList().get(0).getId());
            this.ccatresptTaxonomy = this.ccatrespt;

            //AssociationTypes für alle RubricKinds erstellen
            Iterator itRubricKinds = rks.getRubricKind().iterator();
            //System.out.println("anzrk:" + rks.getRubricKind().size());
            while (itRubricKinds.hasNext())
            {
              RubricKind rk = (RubricKind) itRubricKinds.next();

              AssociationType assoctypeTemp = this.CreateAssociationType(rk.getName(), rk.getName());
              this.assoctypeHashmap.put(rk.getName(), assoctypeTemp);
              // System.out.println("vorher");

              logger.info(this.ccatrespt.getReturnInfos().getMessage());
              if (this.ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
              {
                //  System.out.println("ID: " + ccatrespt.getEntity().getEntityVersionList().get(0).getId());
                this.ccatresptHashmap.put(rk.getName(), this.ccatrespt);
              }
              //  System.out.println("nacher");
            }
          }
        }
      }

      if (countEvery % 500 == 0)
      {
        //logger.debug("FreeMemory: " + runtime.freeMemory());
        // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
        hb_session.flush();
        hb_session.clear();

        if (countEvery % 1000 == 0)
        {
          // sicherheitshalber aufrufen
          System.gc();
        }
      }
      countEvery++;

    }

  }

  /*private void maintainVocabularyVersion() throws Exception
   {
   logger.debug("[ImportClaml.java] Maintain CodeSystemVersion");
   //this.auth = new Authoring();
   MaintainCodeSystemVersionRequestType req = new MaintainCodeSystemVersionRequestType();
   VersioningType v = new VersioningType();
   v.setCreateNewVersion(Boolean.TRUE); //setCreateEmptyVerion(true);
  
   req.setCodeSystem(codeSystem);
   req.setLogin(login);  //setLoginType(login);
   req.setVersioning(v); //setVersioningType(v);
  
   //TODO rausgenommen
   MaintainCodeSystemVersionResponseType resp = null;//auth.MaintainCodeSystemVersion(req);
   logger.info("[ImportClaml.java]" + resp.getReturnInfos().getMessage());
   if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
   {
   throw new Exception("[ImportClaml.java] Maintain Vocabulary Version fehlgeschlagen");
   }
   }*/
  public void createCodeSystem(String title, String uid, String versionName, Date date, String authority, String description) throws Exception
  {
    // Codesystem suchen, erst anlegen, wenn nicht vorhanden
    //this.codeSystem // <- Request

    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();

    if (codeSystem.getId() > 0)
    {
      request.setCodeSystem(codeSystem);
    }
    else
    {
      request.setCodeSystem(new CodeSystem());
    }
    request.getCodeSystem().setName(title);

    CodeSystemVersion codeSystemVersion = new CodeSystemVersion();
    codeSystemVersion.setName(title + " " + versionName);
    codeSystemVersion.setDescription(description);
    codeSystemVersion.setSource(authority);
    codeSystemVersion.setReleaseDate(date);
    codeSystemVersion.setOid(uid);

    request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
    request.getCodeSystem().getCodeSystemVersions().add(codeSystemVersion);

    request.setLoginToken(loginToken);

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");

    logger.debug(resp.getReturnInfos().getMessage());

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      throw new Exception();
    }
    this.codeSystem = resp.getCodeSystem();

    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    
    // Read existing metadata and add to map to avoid double entries
    String hql = "select distinct mp from MetadataParameter mp "
            + " where codeSystemId=" + resp.getCodeSystem().getId();
    List<MetadataParameter> md_list = hb_session.createQuery(hql).list();
    
    for(MetadataParameter mp : md_list)
    {
      metaDataMap.put(mp.getParamName(), mp.getId());
      
      logger.debug("found metadata: " + mp.getParamName() + " with id: " + mp.getId());
    }
    
    
  }

  public AssociationType CreateAssociationType(String forwardName, String reverseName)
  {
    //Associationtype erstellen
    //EntityType erstellen
    CodeSystemEntity etAssoc = new CodeSystemEntity();
    //TODO IsAxis ist nicht in CodeSystemEntity sondern in CodeSystemVersionEntityMembership
    //etAssoc.setIsAxis(false);
    //TODO Ob das wohl so richtig ist.
    Set<CodeSystemVersionEntityMembership> memlist = etAssoc.getCodeSystemVersionEntityMemberships();
    Iterator memIter = memlist.iterator();
    while (memIter.hasNext())
    {
      CodeSystemVersionEntityMembership csvem = (CodeSystemVersionEntityMembership) memIter.next();
      //if(csvem.) wenn es die korrekte Verbindung der beiden Tabellen ist, dann nur setzen
      csvem.setIsAxis(Boolean.FALSE);
    }

    Set<AssociationType> assoList = new HashSet<AssociationType>();
    AssociationType assoctype = new AssociationType();
    assoctype.setForwardName(forwardName);
    assoctype.setReverseName(reverseName);
    assoList.add(assoctype);

    Set<CodeSystemEntityVersion> evlistAssoc = new HashSet<CodeSystemEntityVersion>();
    //EntityVersionType erstellen
    CodeSystemEntityVersion evtAssoc = new CodeSystemEntityVersion();
    evtAssoc.setMajorRevision(0);
    evtAssoc.setMinorRevision(0);
    evtAssoc.setAssociationTypes(assoList);
    evtAssoc.setIsLeaf(true);

    evlistAssoc.add(evtAssoc);

    etAssoc.setCodeSystemEntityVersions(evlistAssoc);

    CreateConceptAssociationTypeRequestType ccatrt = new CreateConceptAssociationTypeRequestType();
    ccatrt.setCodeSystemEntity(etAssoc);

    ccatrt.setLoginToken(loginToken);

    CreateConceptAssociationType ccat = new CreateConceptAssociationType();
    this.ccatrespt = ccat.CreateConceptAssociationType(ccatrt, hb_session, loginInfoType);

    return assoctype;

  }

  private void CreateSingleConcept(clamlBindingXSD.Class cl) throws Exception
  {
    //Konzepte erstellen
    String code = "";
    String rubKind = "";
    String labelString = "";

    clamlBindingXSD.Class clazz = cl;
    code = clazz.getCode();
    //clazz.getKind()  // TODO Kind abspeichern für Export

    //Status aktuallisieren
    aktCount++;
    percentageComplete = aktCount;
    currentTask = code;

    /*if(aktCount % 200 == 0)
     {
     logger.error("AktClass: " + code + " (" + aktCount + ")");
     }*/
    Iterator it2 = clazz.getRubric().iterator();
    Iterator it3 = clazz.getRubric().iterator();

    boolean found = false;
    // Alle Rubrics Durchlaufen
    // Das erste mal um preferred zu suchen und anzulegen, dann das zweite mal um alle anderen anzulegen
    Rubric rubi = null;
    while (it2.hasNext())
    {
      rubi = (Rubric) it2.next();
      rubKind = (String) rubi.getKind();

      if (rubKind.equals(RUBRICKINDS.preferred.getCode()))
      {
        found = true;
        labelString = getAllRubricStrings(rubi);
        this.createPrefferedTerm(labelString, code, clazz);
      }
    }
    if (found == false && rubi != null)
    {
      rubi.setKind(RUBRICKINDS.preferred.getCode());
      labelString = getAllRubricStrings(rubi);
      this.createPrefferedTerm(labelString, code, clazz);
    }

    while (it3.hasNext() && found == true)
    {
      rubi = (Rubric) it3.next();

      rubKind = (String) rubi.getKind();

      if (!(rubKind.equals(RUBRICKINDS.preferred.getCode())
              || rubKind.equals(RUBRICKINDS.note.getCode())))
      {
        labelString = getAllRubricStrings(rubi);
        this.createNotPrefferdTerm(labelString, code, clazz, rubKind);
      }
    }
  }

  //Durchläuft alle Labels/Fragments/Paras und gibt den zusammengestzten String zurück
  //ignoriert bisher Reference
  public String getAllRubricStrings(Rubric rubric)
  {
    String returnString = "";
    Iterator itRubric = rubric.getLabel().iterator();
    while (itRubric.hasNext())
    {
      Label label = (Label) itRubric.next();
      returnString = returnString + label.getvalue();
      /*List contentList = label.getContent();
      Iterator itContent = contentList.iterator();
      while (itContent.hasNext())
      {
        Object o = itContent.next();

        if (o instanceof String)
        {
          returnString = returnString + o.toString();
        }
        else
        {
          if (o instanceof Fragment)
          {
            Fragment fragment = (Fragment) o;
            returnString = returnString + fragment.getvalue();
            List fragmentListe = fragment.getContent();
            Iterator itFragmentContent = fragmentListe.iterator();
            while (itFragmentContent.hasNext())
            {
              Object o2 = itFragmentContent.next();
              if (o2 instanceof String)
              {
                returnString = returnString + o2.toString();

              }
            }
          }
          else
          {
            if (o instanceof Para)
            {
              Para para = (Para) o;
              List paraListe = para.getContent();
              Iterator itParaContent = paraListe.iterator();
              while (itParaContent.hasNext())
              {
                Object o2 = itParaContent.next();
                if (o2 instanceof String)
                {
                  returnString = returnString + o2.toString();

                }
              }
              returnString = returnString + para.getvalue();
            }
          }
        }
      }*/
    }
    return returnString;
  }

  private void addAttributeMetadata(clamlBindingXSD.Class clazz, CodeSystemEntityVersion csev, CodeSystemConcept csc)
  {
    if (clazz != null && csev != null && csc != null)
    {
      /*for(Rubric rubric : clazz.getRubric())
       {
       String rubKind = (String) rubric.getKind();
        
       if (rubKind.equals(RUBRICKINDS.coding_hint.getCode()))
       {
       csc.
       }
       }*/

      for (Meta meta : clazz.getMeta())
      {
        try
        {
          if (meta.getName().equals(METADATA_ATTRIBUTES.hints.getCode()))
            csc.setHints(meta.getValue());
          if (meta.getName().equals(METADATA_ATTRIBUTES.meaning.getCode()))
            csc.setMeaning(meta.getValue());
          if (meta.getName().equals(METADATA_ATTRIBUTES.termAbbrevation.getCode()))
            csc.setTermAbbrevation(meta.getValue());

          if (meta.getName().equals(METADATA_ATTRIBUTES.majorRevision.getCode()))
            csev.setMajorRevision(Integer.parseInt(meta.getValue()));
          if (meta.getName().equals(METADATA_ATTRIBUTES.minorRevision.getCode()))
            csev.setMinorRevision(Integer.parseInt(meta.getValue()));
          if (meta.getName().equals(METADATA_ATTRIBUTES.status.getCode()))
            csev.setStatusVisibility(Integer.parseInt(meta.getValue()));
          if (meta.getName().equals(METADATA_ATTRIBUTES.statusDate.getCode()))
            csev.setStatusVisibilityDate(new Date(meta.getValue()));
        }
        catch (Exception ex)
        {
        }
      }
    }
  }

  public void createPrefferedTerm(String labelString, String code, clamlBindingXSD.Class clazz) throws Exception
  {
    logger.debug("createPrefferedTerm mit Code: " + code + ", Text: " + labelString);

    CreateConceptRequestType request = new CreateConceptRequestType();

    // EntityType erstellen
    CodeSystemEntity cse = new CodeSystemEntity();
    CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
    csvem.setIsAxis(false);
    if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
    {
      csvem.setIsAxis(false);
      csvem.setIsMainClass(false);
    }
    else
    {
      csvem.setIsMainClass(true);
    }

    if (clazz.getKind() != null && clazz.getKind().equals("chapter"))
    {
      csvem.setIsMainClass(true);
    }

    cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
    cse.getCodeSystemVersionEntityMemberships().add(csvem);

    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setMajorRevision(1);
    csev.setMinorRevision(0);
    csev.setStatusVisibility(1);
    csev.setIsLeaf(true);  // erstmal true, wird per Trigger auf false gesetzt, wenn eine Beziehung eingefügt wird

    CodeSystemConcept csc = new CodeSystemConcept();
    csc.setCode(code);
    csc.setTerm(labelString);
    csc.setTermAbbrevation("");
    csc.setIsPreferred(true);

    csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
    csev.getCodeSystemConcepts().add(csc);

    cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
    cse.getCodeSystemEntityVersions().add(csev);

    addAttributeMetadata(clazz, csev, csc);

    logger.debug("isMainClass: " + csvem.getIsMainClass());

    request.setCodeSystem(codeSystem);
    request.setCodeSystemEntity(cse);
    request.setLoginToken(loginToken);

    //Konzept erstellen
    CreateConcept cc = new CreateConcept();
    this.ccsResponse = cc.CreateConcept(request, hb_session, loginInfoType);

    logger.info("[ImportClaml.java]" + ccsResponse.getReturnInfos().getMessage());
    if (ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
      {
        this.createSuperclassAssociation(code, clazz);
      }

      //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
      long aktEntityVersionID = 0;

      if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
      {
        aktEntityVersionID = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
        referenceMap.put(code, aktEntityVersionID);
      }
      StaticStatus.importCount++;
      countImported++;
    }
    else
    {
      throw new Exception();
    }
  }

  public void createNotPrefferdTerm(String labelString, String code, clamlBindingXSD.Class clazz, String rubKind) throws Exception
  {
    logger.debug("createNotPrefferdTerm mit Code: " + code + ", Text: " + labelString);
    //System.out.println("test4");
    CreateConceptRequestType request = new CreateConceptRequestType();

    //EntityType erstellen
    CodeSystemEntity cse = new CodeSystemEntity();
    CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
    csvem.setIsAxis(false);
    cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
    cse.getCodeSystemVersionEntityMemberships().add(csvem);

    //EntityVersionType erstellen
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setMajorRevision(1);
    csev.setMinorRevision(0);
    csev.setStatusVisibility(1);
    csev.setIsLeaf(true);

    //TermType erstellen
    CodeSystemConcept csc = new CodeSystemConcept();
    csc.setCode(code);
    csc.setTerm(labelString);
    csc.setTermAbbrevation("");
    csc.setIsPreferred(false);

    csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
    csev.getCodeSystemConcepts().add(csc);

    cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
    cse.getCodeSystemEntityVersions().add(csev);

    addAttributeMetadata(clazz, csev, csc);

    request.setCodeSystem(codeSystem);
    request.setCodeSystemEntity(cse);
    request.setLoginToken(loginToken);

    //Konzept erstellen
    CreateConcept cc = new CreateConcept();
    this.ccsResponse = cc.CreateConcept(request, hb_session, loginInfoType);

    logger.info("[ImportClaml.java]" + ccsResponse.getReturnInfos().getMessage());
    if (ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      this.createTerm2TermAssociation(code, clazz, rubKind);
      countImported++;
      StaticStatus.importCount++;
    }
    else
    {
      //throw new Exception();
    }
  }

  public void createTerm2TermAssociation(String code, clamlBindingXSD.Class clazz, String rubkind) throws Exception
  {
    //aktuelle entityVersionID aus der Response merken:
    long aktEntityVersionID = 0;
    if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
    {
      aktEntityVersionID = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
    }

    //den eigenen Code in der HashMap suchen (ist der Code des prefferdTerms)
    long prefferedTermEntityVersionID = (Long) referenceMap.get(code);

    CodeSystemEntityVersionAssociation evat = new CodeSystemEntityVersionAssociation();
    //TODO hier hat sich die Struktur der Daten geändert muss noch mal überdacht werden

    evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
    evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(prefferedTermEntityVersionID);

    evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
    evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(aktEntityVersionID);

    evat.setAssociationKind(Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode());
    evat.setLeftId(prefferedTermEntityVersionID);

    evat.setStatus(1);
    evat.setStatusDate(new Date());

    //AssociationType und Response aus der jeweiligen Hasmap holen
    CreateConceptAssociationTypeResponseType resp = (CreateConceptAssociationTypeResponseType) ccatresptHashmap.get(rubkind);
    AssociationType atype = (AssociationType) assoctypeHashmap.get(rubkind);
    atype.setCodeSystemEntityVersionId(resp.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());

    evat.setAssociationType(atype);

    CreateConceptAssociationRequestType ccar = new CreateConceptAssociationRequestType();
    //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
    //es besitzt jedoch eine andere Struktur

    ccar.setCodeSystemEntityVersionAssociation(evat);
    ccar.setLoginToken(loginToken);

    CreateConceptAssociation cca = new CreateConceptAssociation();
    CreateConceptAssociationResponseType ccaresp = cca.CreateConceptAssociation(ccar, hb_session, loginInfoType);
    //System.out.println("test11");
    logger.info("[ImportClaml.java]" + ccaresp.getReturnInfos().getMessage());
    if (ccaresp.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      logger.info("[ImportClaml.java] Create Association Erfolgreich");

    }
    else
    {
      // throw new Exception();
    }
    //System.out.println("test8");
  }

  public void createSuperclassAssociation(String code, clamlBindingXSD.Class clazz) throws Exception
  {
    //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
    long aktEntityVersionID = 0;
    if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
    {
      aktEntityVersionID = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();
      referenceMap.put(code, aktEntityVersionID);
    }

    //Die erste SuperClass holen
    String superclazzCode = "";
    long superclazzEntityVersionID = 0;
    if (clazz.getSuperClass().iterator().hasNext())
    {
      superclazzCode = clazz.getSuperClass().iterator().next().getCode();
      //Superclass id in der Hashmap suchen
      superclazzEntityVersionID = (Long) referenceMap.get(superclazzCode);
      //System.out.println("superclassCode: " + superclazzCode + " superclassID:" + superclazzEntityVersionID + "aktCode" + code + " aktID:" + aktEntityVersionID);
    }

    CodeSystemEntityVersionAssociation evat = new CodeSystemEntityVersionAssociation();
    //TODO hier hat sich die Struktur der Daten geändert muss noch mal überdacht werden
    evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
    evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(superclazzEntityVersionID);

    evat.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
    evat.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(aktEntityVersionID);

    evat.setAssociationKind(Definitions.ASSOCIATION_KIND.TAXONOMY.getCode());
    evat.setLeftId(superclazzEntityVersionID);
    evat.setStatus(1);
    evat.setStatusDate(new Date());

    assoctypeTaxonomy.setCodeSystemEntityVersionId(ccatresptTaxonomy.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId());
    evat.setAssociationType(assoctypeTaxonomy);

    CreateConceptAssociationRequestType ccar = new CreateConceptAssociationRequestType();
    //TODO hier muss noch das CodeSystemEntityVersionAssociation in CreateConceptAssociationTypeRequestType gesetzt werden
    //es besitzt jedoch eine andere Struktur

    ccar.setCodeSystemEntityVersionAssociation(evat);
    ccar.setLoginToken(loginToken);
    CreateConceptAssociation cca = new CreateConceptAssociation();
    //TODO cca.CreateConceptAssociation( ist noch nicht implementiert

    CreateConceptAssociationResponseType ccaresp = cca.CreateConceptAssociation(ccar, hb_session, loginInfoType);

    logger.info("[ImportClaml.java]" + ccaresp.getReturnInfos().getMessage());
    if (ccaresp.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      logger.info("[ImportClaml.java] Create Association Erfolgreich");

    }
    else
    {
      //throw new Exception();
    }

  }

  private long insertMetaData(String name, String value, String code)
  {
    if (value == null || value.length() == 0)
      return 0;

    long metaDataID = 0;
    // Der SQLHelper baut die Insert-Anfrage zusammen
    if (metaDataMap.containsKey(name))
    {
      metaDataID = (Long) metaDataMap.get(name);
    }
    else
    {
      //Create metadata_parameter
      MetadataParameter mp = new MetadataParameter();
      mp.setParamName(name);
      mp.setCodeSystem(codeSystem);

      hb_session.save(mp);

      metaDataID = mp.getId();
      logger.info("[ImportClaml.java] Neues metadata_parameter mit ID: " + metaDataID + " und name: " + name);

      this.metaDataMap.put(name, metaDataID);
    }

    //Create parameter_value
    if (metaDataID == 0)
    {
      logger.warn("metaDataID ist 0 für: " + name);
    }

    // Der SQLHelper baut die Insert-Anfrage zusammen
    CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
    mv.setParameterValue(value);

    mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
    mv.getCodeSystemEntityVersion().setVersionId((Long) referenceMap.get(code));

    mv.setMetadataParameter(new MetadataParameter());
    mv.getMetadataParameter().setId(metaDataID);

    hb_session.save(mv);

    metaDataID = mv.getId();
    return metaDataID;
  }

  public void createMetaData(clamlBindingXSD.Class clazz)
  {
    if (logger.isInfoEnabled())
      logger.info("createMetaData gestartet");

    for (Meta meta : clazz.getMeta())
    {
      if (meta.getName() != null && meta.getName().length() > 0)
      {
        // Prüfen, ob es ein Metadatenattribut ist
        if (METADATA_ATTRIBUTES.isCodeValid(meta.getName()) == false)
        {
          long metaDataID = insertMetaData(meta.getName(), meta.getValue(), clazz.getCode());
          if (metaDataID > 0)
            logger.debug("[ImportClaml.java] Neues entity_version_parameter_value mit ID: " + metaDataID);
        }
      }
    }

    // ClassKind in Metadaten abspeichern, damit dieser wieder exportiert werden kann
    if (clazz.getKind() != null)
    {
      String classKind = clazz.getKind().toString();
      if (classKind.length() > 0)
      {
        long metaDataID = insertMetaData("ClaML_ClassKind", classKind, clazz.getCode());
        if (metaDataID > 0)
          logger.debug("[ImportClaml.java] Neues entity_version_parameter_value mit ID: " + metaDataID);
      }
    }
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }
}
