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

import com.csvreader.CsvReader;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.Query;

/**
 * @since 2012-03-28
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 * 
 * LOINC-DB-Version: Database Version 2.38
 */
public class ImportLOINC
{
  private static Map<String, Long> codesMap;
  private Map<String, AssociationType> associationMap;
  
  private Map<String, MetadataParameter> metadataParameterMap;
  private static String[] metadataFields =
  {
    "COMPONENT",
    "PROPERTY",
    "TIME_ASPCT",
    "SYSTEM",
    "SCALE_TYP",
    "METHOD_TYP",
    "CLASS",
    "SOURCE",
    "CHNG_TYPE",
    "COMMENTS",
    "CONSUMER_NAME",
    "MOLAR_MASS",
    "CLASSTYPE",
    "FORMULA",
    "SPECIES",
    "EXMPL_ANSWERS",
    "ACSSYM",
    "BASE_NAME",
    "NAACCR_ID",
    "CODE_TABLE",
    "SURVEY_QUEST_TEXT",
    "SURVEY_QUEST_SRC",
    "UNITSREQUIRED",
    "SUBMITTED_UNITS",
    "RELATEDNAMES2",
    "ORDER_OBS",
    "CDISC_COMMON_TESTS",
    "HL7_FIELD_SUBFIELD_ID",
    "EXTERNAL_COPYRIGHT_NOTICE",
    "EXAMPLE_UNITS",
    "HL7_V2_DATATYPE",
    "HL7_V3_DATATYPE",
    "CURATED_RANGE_AND_UNITS",
    "DOCUMENT_SECTION",
    "EXAMPLE_UCUM_UNITS",
    "EXAMPLE_SI_UCUM_UNITS",
    "STATUS_REASON",
    "STATUS_TEXT",
    "CHANGE_REASON_PUBLIC",
    "COMMON_TEST_RANK",
    "COMMON_ORDER_RANK",
    "COMMON_SI_TEST_RANK",
    "HL7_ATTACHMENT_STRUCTURE",
    "STATUS"
  };
  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;

  public ImportLOINC(ImportCodeSystemRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportLOINC gestartet ======");

    parameter = _parameter;

  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }

  public String importLOINC_Associations(ImportCodeSystemResponseType response)
  {
    StaticStatus.importCount = 0;
    StaticStatus.importTotal = 0;

    String s = "";
    if(codesMap == null)
      codesMap = new HashMap<String, Long>();

    int count = 0, countFehler = 0;

    CsvReader csv;
    try
    {
      logger.debug("codesMap-Size: " + codesMap.size());
      
      byte[] bytes = parameter.getImportInfos().getFilecontent();
      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);
      csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
      csv.setDelimiter('\t');
      csv.setUseTextQualifier(true);
      csv.readHeaders();
      logger.debug("Anzahl Header: " + csv.getHeaderCount());
      for(int i=0;i<csv.getHeaderCount();++i)
        logger.debug("Header: " + csv.getHeader(i));

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (parameter.getCodeSystem() == null ||
          parameter.getCodeSystem().getId() == 0 ||
          parameter.getCodeSystem().getCodeSystemVersions() == null ||
          parameter.getCodeSystem().getCodeSystemVersions().size() == 0)
        {
          // Fehlermeldung
          tx.rollback();
          HibernateUtil.getSessionFactory().close();
          return "Kein Codesystem angegeben!";
        }

        // Assoziationen lesen
        associationMap = new HashMap<String, AssociationType>();
        List<AssociationType> associationList = hb_session.createQuery("from AssociationType").list();
        for (int i = 0; i < associationList.size(); ++i)
        {
          associationMap.put(associationList.get(i).getForwardName(), associationList.get(i));
        }

        logger.debug("Starte Import...");

        int countEvery = 0;
        
        // Daten laden
        while (csv.readRecord())
        {
          //logger.warn(new Date().getTime() + ", Lese Index " + StaticStatus.importCount);
          countEvery++;
          
          CreateConceptAssociationRequestType request = new CreateConceptAssociationRequestType();
          request.setLoginToken(parameter.getLoginToken());
          
          request.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());
          
          String code1 = csv.get("LOINC");
          String code2 = csv.get("MAP_TO");
          String comment = csv.get("COMMENT");
          
          //logger.warn(new Date().getTime() + ", CSV gelesen");
          //logger.warn("Code1: " + code1 + ", Code2: " + code2 + ", comment: " + comment);
          
          // CSV-ID für LOINC-Code lesen
          long csev_id1 = getCSEV_VersionIdFromCode(code1);
          long csev_id2 = getCSEV_VersionIdFromCode(code2);
          
          //logger.warn(new Date().getTime() + ", Codes gelesen");
          
          if(csev_id1 > 0 && csev_id2 > 0)
          {
            //logger.warn(new Date().getTime() + ", Füge Assoziation ein...");
            
            // IDs setzen
            request.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
            request.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
            
            request.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csev_id1);
            request.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csev_id2);
            
            request.getCodeSystemEntityVersionAssociation().setAssociationKind(1); // ontologisch
            request.getCodeSystemEntityVersionAssociation().setLeftId(csev_id1);
            
            request.getCodeSystemEntityVersionAssociation().setAssociationType(new AssociationType());
            
            // Assoziationstyp erhalten
            //logger.warn(new Date().getTime() + ", Assoziationstyp erhalten...");
            
            if(comment != null && comment.length() > 0 && associationMap.containsKey(comment))
            {
              request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(
                associationMap.get(comment).getCodeSystemEntityVersionId());
            }
            else
            {
              if(comment != null && comment.length() > 0)
              {
                // Neuen Beziehungstyp einfügen
                
                CreateConceptAssociationTypeRequestType requestAssociation = new CreateConceptAssociationTypeRequestType();
                requestAssociation.setLoginToken(parameter.getLoginToken());
                requestAssociation.setCodeSystem(parameter.getCodeSystem());
                requestAssociation.setCodeSystemEntity(new CodeSystemEntity());
                requestAssociation.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
                AssociationType at = new AssociationType();
                if (comment.length() > 48)
                  at.setForwardName(comment.substring(0, 48));
                else at.setForwardName(comment);
                at.setReverseName("");
                CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
                csev.setAssociationTypes(new HashSet<AssociationType>());
                csev.getAssociationTypes().add(at);
                requestAssociation.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
                
                CreateConceptAssociationType ccat = new CreateConceptAssociationType();
                CreateConceptAssociationTypeResponseType responseAssociation =
                  ccat.CreateConceptAssociationType(requestAssociation, hb_session, "");
                
                if(responseAssociation.getReturnInfos().getStatus() == ReturnType.Status.OK)
                {
                  CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseAssociation.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
                  request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(csev_result.getVersionId());
                }
                else request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(5l); // TODO gehört zu
                
                //logger.warn(new Date().getTime() + ", Neue Beziehung eingefügt");
              }
              else
              {
                // Standard-Beziehung nehmen
                request.getCodeSystemEntityVersionAssociation().getAssociationType().setCodeSystemEntityVersionId(5l); // TODO gehört zu
              }
            }
            
            
            //logger.warn(new Date().getTime() + ", Assoziationstyp bestimmt");
            
            StaticStatus.importCount++;
            if (StaticStatus.importCount % 100 == 0)
              logger.debug("Lese Datensatz " + StaticStatus.importCount + ", count: " + count);
            
            // Beziehung speichern
            // Dienst aufrufen (Konzept einfügen)
            //logger.warn(new Date().getTime() + ", Assoziation in DB einfügen...");
            CreateConceptAssociation cca = new CreateConceptAssociation();
            CreateConceptAssociationResponseType responseCCA = cca.CreateConceptAssociation(request, hb_session, "");
            
            if (responseCCA.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
              count++;

              if (StaticStatus.importCount % 100 == 0)
                logger.debug("Neue Beziehung hinzugefügt, ID1: " + csev_id1 + ", ID2: " + csev_id2);
            }
            else
              countFehler++;
            
            //logger.warn(new Date().getTime() + ", Assoziation in DB eingefügt");

          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht angegeben");
          }

          //logger.warn(new Date().getTime() + ", vor Speicherbereinigung");
          
          //Mimimum acceptable free memory you think your app needs 
          //long minRunningMemory = (1024 * 1024);
          //Runtime runtime = Runtime.getRuntime();
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
          
          //logger.warn(new Date().getTime() + ", nach Speicherbereinigung");
          
          
          //logger.warn(new Date().getTime() + ", Lese nächsten Record");
          csv.skipLine();
          
          if(countFehler > 10)
            break;
        }

        if (count == 0 || countFehler > 10)
        {
          tx.rollback();
          response.getReturnInfos().setMessage("Keine Beziehungen importiert. Möglicherweise ist die Fehleranzahl zu hoch. Anzahl Fehler: " + countFehler);
        }
        else
        {
          logger.debug("Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
          tx.commit();
          countImported = count;
          response.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Beziehung(en) importiert, " + countFehler + " Fehler");
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        logger.error("Fehler beim Import der LOINC-Association-Datei: " + ex.getMessage());
        s = "Fehler beim Import der LOINC-Association-Datei: " + ex.getLocalizedMessage();

        try
        {
          tx.rollback();
          logger.info("[ImportLOINC.java] Rollback durchgeführt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
        }
      }
      finally
      {
        // Session schließen
        HibernateUtil.getSessionFactory().close();
      }


    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      s = "Fehler beim LOINC-Association-Import: " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    associationMap.clear();
    associationMap = null;
    codesMap.clear();
    codesMap = null;
    
    logger.debug("ImportLOINC-Association - fertig");
    return s;
  }
  
  private long getCSEV_VersionIdFromCode(String code)
  {
    
    //List<CodeSystemConcept> list = hb_session.createQuery("from CodeSystemConcept where code=\"" + code + "\"").list();
    /*Query q = hb_session.createQuery("from CodeSystemConcept where code=:s_code");
    q.setString("s_code", code);
    List<CodeSystemConcept> list = q.list();
    
    if(list != null && list.size() > 0)
    {
      CodeSystemConcept csc = list.get(0);
      return csc.getCodeSystemEntityVersionId();
    }*/
    
    if(codesMap.containsKey(code))
      return codesMap.get(code);
    else
    {
      logger.warn("Konzept mit Code '" + code + "' nicht in Map gefunden!");
    }
    
    return 0;
  }
  
  public String importLOINC(ImportCodeSystemResponseType response)
  {
    StaticStatus.importCount = 0;
    StaticStatus.importTotal = 0;

    String s = "";

    int count = 0, countFehler = 0;
    
    codesMap = new HashMap<String, Long>();

    CsvReader csv;
    try
    {
      byte[] bytes = parameter.getImportInfos().getFilecontent();
      
      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);
      csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
      //csv = new CsvReader("C:\\Temp\\LOINCDB.TXT");
      //csv.setDelimiter('\u0009');  // oder '\t'
      csv.setDelimiter('\t');
      csv.setUseTextQualifier(true);
      csv.readHeaders();
      logger.debug("Anzahl Header: " + csv.getHeaderCount());

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (ImportCreateCodeSystem.createCodeSystem(hb_session, parameter, response) == false)
        {
          // Fehlermeldung
          tx.rollback();
          return "CodeSystem konnte nicht erstellt werden!";
        }

        // Metadaten-Parameter lesen
        metadataParameterMap = new HashMap<String, MetadataParameter>();
        List<MetadataParameter> mpList = hb_session.createQuery("from MetadataParameter").list();
        for (int i = 0; i < mpList.size(); ++i)
        {
          metadataParameterMap.put(mpList.get(i).getParamName(), mpList.get(i));
        }

        logger.debug("Starte Import...");

        // Request hier schon zusammenbauen (muss nicht tausendfach in der Schleife gemacht werden)

        //int countIndex = 0;

        // Daten laden
        // Überlegen, was alles in der Schleife erzeugt wird (Speicherproblematik)
        while (csv.readRecord())
        {
          CreateConceptRequestType request = new CreateConceptRequestType();
          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();
          csc.setIsPreferred(true);

          CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
          csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
          csev.setIsLeaf(true);

          CodeSystemConceptTranslation translation = new CodeSystemConceptTranslation();
          translation.setLanguageCd(Definitions.LANGUAGE_ENGLISH_CD);

          CreateConcept cc = new CreateConcept();

          StaticStatus.importCount++;
          if (StaticStatus.importCount % 200 == 0)
            logger.debug("Lese Datensatz " + StaticStatus.importCount + ", count: " + count);

          //request.setCodeSystemEntity(new CodeSystemEntity());
          //request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          csc.setCode(csv.get("LOINC_NUM"));
          csc.setTerm(csv.get("COMPONENT"));
          csc.setTermAbbrevation(csv.get("SHORTNAME"));

          // Sprachen prüfen (Englisch-Übersetzung hinzufügen)
          csc.getCodeSystemConceptTranslations().clear();

          String translationEnglish = csv.get("LONG_COMMON_NAME");
          if (translationEnglish != null && translationEnglish.length() > 0)
          {
            //csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());
            //CodeSystemConceptTranslation translation = new CodeSystemConceptTranslation();
            //translation.setLanguageId(Definitions.LANGUAGE_ENGLISH_ID);
            translation.setTerm(translationEnglish);
            csc.getCodeSystemConceptTranslations().add(translation);
          }

          // Daten anpassen
          if (csc.getCode().length() > 98)
          {
            csc.setCode(csc.getCode().substring(0, 98));
          }

          // Entity-Version erstellen
          if (csev.getCodeSystemConcepts() == null)
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
          else
            csev.getCodeSystemConcepts().clear();
          csev.getCodeSystemConcepts().add(csc);
          csev.setEffectiveDate(parseDate(csv.get("DATE_LAST_CHANGED")));
          csev.setStatusWorkflow(getLoincStatus(csv.get("STATUS")));
          csev.setStatusVisibility(1);
          csev.setStatusVisibilityDate(new Date());
          csev.setStatusWorkflowDate(new Date());


          // Konzept speichern
          if (csc.getCode().length() > 0)
          {
            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConceptResponseType responseCC = cc.CreateConcept(request, hb_session, "");

            if (responseCC.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
              count++;
              
              if(responseCC.getCodeSystemEntity().getCurrentVersionId() > 0)
                codesMap.put(csc.getCode(), responseCC.getCodeSystemEntity().getCurrentVersionId());

              // Metadaten zu diesem Konzept speichern
              int mdCount = 0;

              CodeSystemEntityVersion csev_result = (CodeSystemEntityVersion) responseCC.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
              mdCount = addMetadataToConcept(csv, csev_result.getVersionId(), hb_session);

              if (StaticStatus.importCount % 200 == 0)
                logger.debug("Neues Konzept hinzugefügt: " + csc.getCode() + ", Anz. Metadaten: " + mdCount);
            }
            else
              countFehler++;
          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht angegeben");
          }

          //Mimimum acceptable free memory you think your app needs 
          //long minRunningMemory = (1024 * 1024);
          Runtime runtime = Runtime.getRuntime();
          if (StaticStatus.importCount % 200 == 0)
          {
            logger.debug("FreeMemory: " + runtime.freeMemory());

            if (StaticStatus.importCount % 1000 == 0)
            {
              // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
              hb_session.flush();  
              hb_session.clear();
            }
            if (StaticStatus.importCount % 10000 == 0)
            {
              // sicherheitshalber aufrufen
              System.gc();
            }
          }
        }

        if (count == 0)
        {
          tx.rollback();
          response.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          logger.debug("Import abgeschlossen, speicher Ergebnisse in DB (commit): " + count);
          tx.commit();
          countImported = count;
          response.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        logger.error("Fehler beim Import der LOINC-Datei: " + ex.getMessage());
        s = "Fehler beim Import der LOINC-Datei: " + ex.getLocalizedMessage();

        try
        {
          tx.rollback();
          logger.info("[ImportLOINC.java] Rollback durchgeführt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportLOINC.java] Rollback fehlgeschlagen!");
        }
      }
      finally
      {
        // Session schließen
        HibernateUtil.getSessionFactory().close();
      }


    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      s = "Fehler beim LOINC-Import: " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    StaticStatus.importCount = 0;
    StaticStatus.importTotal = 0;

    logger.debug("ImportLOINC - fertig");
    return s;
  }

  /**
   * 
   * 
   * @param s Datensatz in der Schreibweise JJJJMMTT
   * @return java.util.Date
   */
  private java.util.Date parseDate(String s)
  {
    if (s == null || s.length() == 0)
    {
      logger.debug("Fehler beim Parsen des Datums: nicht angegeben");
      return new java.util.Date();
    }

    try
    {
      DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
      return (Date) formatter.parse(s);
    }
    catch (Exception e)
    {
      logger.warn("Fehler beim Parsen des Datums: " + s);
      return new java.util.Date();
    }
  }

  /**
   * 
   * @param s STATUS-Feldinhalt
   * @return Terminologieserver-Status
   */
  private int getLoincStatus(String s)
  {
    // mögliche LOINC-Status: ACTIVE, DEPRECATED, DISCOURAGED, TRIAL
    if (s == null || s.length() == 0)
    {
      logger.debug("Fehler beim Lesen des Loinc-Status: nicht angegeben");
      return 0;
    }

    if (s.equals("ACTIVE"))
      return 1;
    else if (s.equals("DEPRECATED"))
      return 2;
    else if (s.equals("DISCOURAGED"))
      return 3;
    else if (s.equals("TRIAL"))
      return 4;

    return 0;
  }

  /**
   * Fügt alle oben angegebenen Metadaten zum Konzept hinzu
   * 
   * @param csv
   * @param csevId Konzept-ID (Entity-Version-ID)
   * @param hb_session 
   */
  private int addMetadataToConcept(CsvReader csv, long csevId, org.hibernate.Session hb_session)
  {
    int mdCount = 0;
    try
    {
      for (int i = 0; i < metadataFields.length; ++i)
      {
        String content = csv.get(metadataFields[i]);

        if (content != null && content.length() > 0)
        {
          MetadataParameter mp = getMetadataParameter(metadataFields[i], hb_session);

          if (mp != null && mp.getId() > 0)
          {
            CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
            mv.setParameterValue(content);

            mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
            mv.getCodeSystemEntityVersion().setVersionId((Long) csevId);

            mv.setMetadataParameter(new MetadataParameter());
            mv.getMetadataParameter().setId(mp.getId());

            hb_session.save(mv);




            mdCount++;
          }
        }

      }
    }
    catch (Exception e)
    {
    }
    return mdCount;
  }

  private MetadataParameter getMetadataParameter(String name, org.hibernate.Session hb_session)
  {
    if (metadataParameterMap.containsKey(name))
      return metadataParameterMap.get(name);
    else
    {
      //Create metadata_parameter
      MetadataParameter mp = new MetadataParameter();
      mp.setParamName(name);

      hb_session.save(mp);
      metadataParameterMap.put(name, mp);

      logger.debug("MetadataParameter in DB hinzugefügt: " + name);

      return mp;
    }
  }
}
