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
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.*;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypes;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.log4j.Logger;
import org.hibernate.Query;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCS_CSV
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;
  private HashMap metaDataMap;
  private AuthenticateInfos loginInfoType;

  public ImportCS_CSV(ImportCodeSystemRequestType _parameter, AuthenticateInfos _loginInfoType)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCSV - FH Dortmund - gestartet ======");

    parameter = _parameter;
    loginInfoType = _loginInfoType;

    this.metaDataMap = new HashMap();
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }

  private class RelationMapType
  {

    private long entityID, entityVersionID;

    public RelationMapType(long entityID, long entityVersionID)
    {
      this.entityID = entityID;
      this.entityVersionID = entityVersionID;
    }

    /**
     * @return the entityID
     */
    public long getEntityID()
    {
      return entityID;
    }

    /**
     * @param entityID the entityID to set
     */
    public void setEntityID(long entityID)
    {
      this.entityID = entityID;
    }

    /**
     * @return the entityVersionID
     */
    public long getEntityVersionID()
    {
      return entityVersionID;
    }

    /**
     * @param entityVersionID the entityVersionID to set
     */
    public void setEntityVersionID(long entityVersionID)
    {
      this.entityVersionID = entityVersionID;
    }
  }

  public String importCSV(ImportCodeSystemResponseType reponse, String ipAddress)
  {
    StaticStatus.importTotal = 0;
    StaticStatus.importCount = 0;
    StaticStatus.importRunning = true;
    StaticStatus.exportRunning = false;
    StaticStatus.cancel = false;

    String s = "";

    int count = 0, countFehler = 0;

    CsvReader csv;
    try
    {
      byte[] bytes = parameter.getImportInfos().getFilecontent();
      logger.debug("wandle zu InputStream um...");
      InputStream is = new ByteArrayInputStream(bytes);

      //csv = new CsvReader("C:\\Temp\\notfallrel_diagnosen.csv");
      csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
      csv.setDelimiter(';');
      csv.setTextQualifier('"');
      csv.setUseTextQualifier(true);

      csv.readHeaders();
      logger.debug("Anzahl Header: " + csv.getHeaderCount());

      // Sprachen identifizieren
      // Metadaten identifizieren
      Map<Integer, String> headerTranslations = new HashMap<Integer, String>();
      Map<Integer, String> headerMetadata = new HashMap<Integer, String>();
      String[] header = csv.getHeaders();

      for (int i = 0; i < csv.getHeaderCount(); ++i)
      {

        try
        {
          if (header[i].contains("translation_"))
          {
            //long languageID = Long.parseLong(header[i].replace("translation_", ""));
            //headerTranslations.put(i, languageID);

            String languageCD = header[i].replace("translation_", "");
            headerTranslations.put(i, languageCD);
          }
        }
        catch (Exception e)
        {
        }

        try
        {
          if (header[i].contains("metadata_"))
          {
            String mdName = header[i].replace("metadata_", "");
            headerMetadata.put(i, mdName);
          }
        }
        catch (Exception e)
        {
        }
      }

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //org.hibernate.Transaction tx = hb_session.beginTransaction();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        logger.debug("Create Codesystem and Version an, if not exists...");

        if (createCodeSystem(hb_session) == false)
        {
          // Fehlermeldung
          tx.rollback();
          return "CodeSystem could not be created!";
        }

        logger.debug("analyse and save metadata...");

        // Metadaten speichern
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        for (String mdText : headerMetadata.values())
        {
          if (metaDataMap.containsKey(mdText))
          {
            logger.debug("Metadata exists already, add to map: " + metaDataMap.get(mdText) + ", " + mdText);
            headerMetadataIDs.put(mdText, (Long) metaDataMap.get(mdText));
            continue;
          }

          MetadataParameter mp = new MetadataParameter();
          mp.setParamName(mdText);
          mp.setCodeSystem(parameter.getCodeSystem());
          hb_session.save(mp);

          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("save metadata parameter: " + mdText + " with Codesystem-ID: " + mp.getCodeSystem().getId() + ", MP-ID: " + mp.getId());
        }

        Map<String, RelationMapType> relationMap = new HashMap<String, RelationMapType>();

        while (csv.readRecord())
        {
          if (StaticStatus.cancel)
            break;
          
          CreateConceptAssociationRequestType requestAssociation = null;
          CreateConceptRequestType request = new CreateConceptRequestType();

          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(csv.get("code"));
          csc.setIsPreferred(true);
          csc.setTerm(csv.get("term"));
          csc.setTermAbbrevation(csv.get("term_abbrevation"));

          logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

          // Weitere Attribute prüfen
          String s_temp;
          s_temp = csv.get("description");
          if (s_temp != null && s_temp.length() > 0)
            csc.setDescription(s_temp);

          s_temp = csv.get("is_preferred");
          if (s_temp != null)
          {
            if (s_temp.equals("1") || s_temp.equals("true"))
              csc.setIsPreferred(true);
            else
              csc.setIsPreferred(false);
          }

          boolean membershipChanged = false;
          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
          s_temp = csv.get("is_axis");
          if (s_temp != null && (s_temp.equals("1") || s_temp.equals("true")))
          {
            membership.setIsAxis(true);
            membershipChanged = true;
          }
          s_temp = csv.get("is_mainclass");
          if (s_temp != null && (s_temp.equals("1") || s_temp.equals("true")))
          {
            membership.setIsMainClass(true);
            membershipChanged = true;
          }

          if (membershipChanged)
          {
            request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
            request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);
          }

          // check relation
          s_temp = csv.get("relation");
          if (s_temp != null && s_temp.length() > 0)
          {
            if (relationMap.containsKey(s_temp) == false)
            {
              // check code from other code systems and add it to map
              logger.debug("find code from other codesystem (cross mapping) for code: " + s_temp);
              long crossmappingCsvId = 0;
              String s_temp2 = csv.get("crossmapping_csv_id");
              if (s_temp2 != null)
              {
                crossmappingCsvId = Integer.parseInt(s_temp2);
              }

              if (crossmappingCsvId > 0)
              {
                logger.debug("search code: " + s_temp + ", csvId: " + crossmappingCsvId);

                String hql = "select distinct csc from CodeSystemConcept csc"
                        + " join fetch csc.codeSystemEntityVersion csev"
                        + " join fetch csev.codeSystemEntity cse"
                        + " join cse.codeSystemVersionEntityMemberships csvem"
                        + " join csvem.codeSystemVersion csv"
                        + " where csc.code=:code and csv.versionId=" + crossmappingCsvId + " and csc.isPreferred=1";

                /*select * from code_system_concept AS csc
                 join code_system_entity_version AS csev ON csc.codeSystemEntityVersionId=csev.versionId
                 join code_system_entity AS cse ON cse.id=csev.codeSystemEntityId
                 join code_system_version_entity_membership AS csvem ON csvem.codeSystemEntityId=cse.id
                 join code_system_version AS csv ON csv.versionId=csvem.codeSystemVersionId
                 where code="A01.0" and csv.versionId=158*/
                logger.debug("HQL: " + hql);
                Query q = hb_session.createQuery(hql);
                q.setParameter("code", s_temp);
                //q.setParameter("csvId", crossmappingCsvId);

                List<CodeSystemConcept> cscList = q.list();
                logger.debug("list size: " + cscList.size());

                for (CodeSystemConcept cscTemp : cscList)
                {
                  RelationMapType newMapEntry = new RelationMapType(cscTemp.getCodeSystemEntityVersion().getCodeSystemEntity().getId(), cscTemp.getCodeSystemEntityVersion().getVersionId());
                  relationMap.put(cscTemp.getCode(), newMapEntry);
                  logger.debug("found cross mapping with entityVersionId: " + cscTemp.getCodeSystemEntityVersion().getVersionId());
                  break;
                }

                logger.debug("search finished");
              }
            }

            if (relationMap.containsKey(s_temp))
            {
              logger.debug("found code in relationMap: " + s_temp + ", assign relation now");

              RelationMapType mapEntry = relationMap.get(s_temp);

              // Es gibt eine Beziehung zu einem anderen Term
              requestAssociation = new CreateConceptAssociationRequestType();
              requestAssociation.setLoginToken(parameter.getLoginToken());
              requestAssociation.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());
              requestAssociation.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
              requestAssociation.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(mapEntry.getEntityVersionID());

              s_temp = csv.get("association_kind");
              if (s_temp != null)
                requestAssociation.getCodeSystemEntityVersionAssociation().setAssociationKind(Integer.parseInt(s_temp));
              else
                requestAssociation.getCodeSystemEntityVersionAssociation().setAssociationKind(2);

              s_temp = csv.get("association_type");
              if (s_temp != null)
              {
                String s_temp2 = csv.get("association_type_reverse");
                String reverse = "";
                if (s_temp2 != null)
                  reverse = s_temp2;

                requestAssociation.getCodeSystemEntityVersionAssociation().setAssociationType(CreateAssociationType(s_temp, reverse, ipAddress));
              }
            }
            else
            {
              logger.debug("code could not be found in relationMap, do not assign link");
            }

          }

          // Sprachen prüfen
          if (headerTranslations != null && headerTranslations.size() > 0)
          {
            csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());

            Set<Integer> spalten = headerTranslations.keySet();
            Iterator<Integer> itSpalten = spalten.iterator();

            while (itSpalten.hasNext())
            {
              Integer spalte = itSpalten.next();
              CodeSystemConceptTranslation translation = new CodeSystemConceptTranslation();
              translation.setLanguageCd(headerTranslations.get(spalte));
              translation.setTerm(csv.get(spalte));
              csc.getCodeSystemConceptTranslations().add(translation);

              logger.debug("Translation hinzufügen: " + translation.getLanguageCd() + "," + translation.getTerm());
            }
          }

          if (csc.getCode().length() == 0)
          {
            if (csc.getTerm().length() > 0)
            {
              if (csc.getTerm().length() > 98)
                csc.setCode(csc.getTerm().substring(0, 98));
              else
                csc.setCode(csc.getTerm());
            }
          }
          else if (csc.getCode().length() > 98)
          {
            csc.setCode(csc.getCode().substring(0, 98));
          }

          if (csc.getCode().length() > 0)
          {
            // Entity-Version erstellen
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            csev.setStatusVisibility(1);
            csev.setIsLeaf(true);

            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session, loginInfoType);

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
              RelationMapType newMapEntry = new RelationMapType(response.getCodeSystemEntity().getId(), response.getCodeSystemEntity().getCurrentVersionId());
              relationMap.put(csc.getCode(), newMapEntry);

              StaticStatus.importCount++;
              count++;
              
              if(count % 100 == 0)
              {
                hb_session.flush();
                hb_session.clear();
              }

              if (requestAssociation != null)
              {
                // Beziehung ebenfalls abspeichern
                requestAssociation.getCodeSystemEntityVersionAssociation().setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                requestAssociation.getCodeSystemEntityVersionAssociation().getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(
                        response.getCodeSystemEntity().getCurrentVersionId());

                // Dienst aufrufen (Beziehung erstellen)
                CreateConceptAssociation cca = new CreateConceptAssociation();
                CreateConceptAssociationResponseType responseAssociation = cca.CreateConceptAssociation(requestAssociation, hb_session, loginInfoType);

                if (responseAssociation.getReturnInfos().getStatus() == ReturnType.Status.OK)
                {
                }
                else
                  logger.debug("Beziehung nicht gespeichert: " + responseAssociation.getReturnInfos().getMessage());
              }

              if (headerMetadata != null && headerMetadata.size() > 0)
              {
                // Metadaten einfügen
                for (Integer spalte : headerMetadata.keySet())
                {
                  String mdValue = csv.get(spalte);
                  if (mdValue != null && mdValue.length() > 0)
                  {
                    CodeSystemMetadataValue csmv = new CodeSystemMetadataValue(mdValue);
                    csmv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                    //csmv.getCodeSystemEntityVersion().setVersionId(response.getCodeSystemEntity().getId());
                    csmv.getCodeSystemEntityVersion().setVersionId(response.getCodeSystemEntity().getCurrentVersionId());
                    csmv.setMetadataParameter(new MetadataParameter());
                    csmv.getMetadataParameter().setId(headerMetadataIDs.get(headerMetadata.get(spalte)));

                    logger.debug("Metadaten einfügen, MP-ID " + csmv.getMetadataParameter().getId() + ", CSEV-ID " + csmv.getCodeSystemEntityVersion().getVersionId() + ", Wert: " + csmv.getParameterValue());

                    hb_session.save(csmv);
                  }
                }
              }

            }
            else
              countFehler++;

          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht gegeben");
          }

        }

        if (count == 0)
        {
          tx.rollback();
          reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          if (StaticStatus.cancel)
          {
            tx.rollback();
            reponse.getReturnInfos().setMessage("Import abgebrochen.");
          }
          else
          {

            tx.commit();
            countImported = count;
            reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
          }

        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        LoggingOutput.outputException(ex, this);
        //logger.error(ex.getMessage());
        s = "Fehler beim Import einer CSV-Datei: " + ex.getLocalizedMessage();

        try
        {
          tx.rollback();
          logger.info("[ImportCSV.java] Rollback durchgeführt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportCSV.java] Rollback fehlgeschlagen!");
        }
      }
      finally
      {
        // Session schließen
        hb_session.close();
      }

    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      s = "Fehler beim Import: " + ex.getLocalizedMessage();
      logger.error(s);
      //ex.printStackTrace();
    }

    return s;
  }

  private boolean createCodeSystem(org.hibernate.Session hb_session)
  {
    // TODO zunächst prüfen, ob CodeSystem bereits existiert
    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
    request.setCodeSystem(parameter.getCodeSystem());
    request.setLoginToken(parameter.getLoginToken());

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      logger.debug("Fehler beim Erstellen des Codesystems: " + resp.getReturnInfos().getMessage());
      return false;
    }
    parameter.setCodeSystem(resp.getCodeSystem());

    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());

    // Read existing metadata and add to map to avoid double entries
    String hql = "select distinct mp from MetadataParameter mp "
            + " where codeSystemId=" + resp.getCodeSystem().getId();
    List<MetadataParameter> md_list = hb_session.createQuery(hql).list();

    for (MetadataParameter mp : md_list)
    {
      metaDataMap.put(mp.getParamName(), mp.getId());
      logger.debug("found metadata: " + mp.getParamName() + " with id: " + mp.getId());
    }

    return true;
  }
  private Map<String, AssociationType> associationTypeMap = null;

  private void initAssociationTypes(String ipAddress)
  {
    if (associationTypeMap == null)
    {
      logger.debug("initAssociationTypes()");

      associationTypeMap = new HashMap();
      ListConceptAssociationTypesRequestType request = new ListConceptAssociationTypesRequestType();
      request.setLoginToken(parameter.getLoginToken());

      ListConceptAssociationTypes lcat = new ListConceptAssociationTypes();
      ListConceptAssociationTypesResponseType response = lcat.ListConceptAssociationTypes(request, ipAddress);

      if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
      {
        for (int i = 0; i < response.getCodeSystemEntity().size(); ++i)
        {
          CodeSystemEntity entity = response.getCodeSystemEntity().get(i);
          Iterator<CodeSystemEntityVersion> it = entity.getCodeSystemEntityVersions().iterator();
          while (it.hasNext())
          {
            CodeSystemEntityVersion csev = it.next();
            AssociationType assType = (AssociationType) csev.getAssociationTypes().toArray()[0];
            String code = assType.getForwardName() + assType.getReverseName();

            associationTypeMap.put(code, assType);
            logger.debug("put(" + code + ")");
          }
        }
      }
    }

  }

  private AssociationType CreateAssociationType(String forwardName, String reverseName, String ipAddress)
  {
    initAssociationTypes(ipAddress);
    String key = forwardName + reverseName;

    if (associationTypeMap.containsKey(key) == false)
    {
      // TODO diese Beziehung einpflegen
      return null;
    }
    else
    {
      return associationTypeMap.get(key);
    }

  }
}
