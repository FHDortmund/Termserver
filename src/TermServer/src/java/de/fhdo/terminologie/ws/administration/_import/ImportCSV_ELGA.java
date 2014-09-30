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
import de.fhdo.terminologie.db.hibernate.*;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCSV_ELGA
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;
  private int lowestLevel = 1;

  private boolean onlyCSV = true; //Only CSV for this case
  private Long csId = 0L;
  private Long csvId = 0L;
  private String resultStr = "";

  public ImportCSV_ELGA(ImportCodeSystemRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCSV gestartet ======");

    parameter = _parameter;
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

  public String importCSV(ImportCodeSystemResponseType reponse)
  {
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
      csv.setTextQualifier('\'');
      csv.setUseTextQualifier(true);

      csv.readHeaders();
      logger.debug("Anzahl Header: " + csv.getHeaderCount());

      for (String s_head : csv.getHeaders())
      {
        logger.debug("Header: " + s_head);
      }

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createCodeSystem(hb_session) == false)
        {
          // Fehlermeldung
          hb_session.getTransaction().commit();
          hb_session.close();
          return "CodeSystem konnte nicht erstellt werden!";
        }

        // MetadatenParameter speichern => ELGA Specific Level/Type 
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        int startIndex = 7;
        int countMp = csv.getHeaderCount() - startIndex;
        for (int i = 0; i < countMp; i++)
        {

          String mdText = "";
          MetadataParameter mp = null;
          mdText = firstCharUpperCase(replaceApo(csv.getHeader(startIndex + i)));

          //Check if parameter already set in case of new Version!
          String hql = "select distinct mp from MetadataParameter mp";
          hql += " join fetch mp.codeSystem cs";

          HQLParameterHelper parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("mp.", "paramName", mdText);

          // Parameter hinzufügen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");
          logger.debug("HQL: " + hql);

          // Query erstellen
          org.hibernate.Query q = hb_session.createQuery(hql);
          // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
          parameterHelper.applyParameter(q);

          List<MetadataParameter> mpList = q.list();
          for (MetadataParameter mParameter : mpList)
          {

            if (mParameter.getCodeSystem().getId().equals(parameter.getCodeSystem().getId()))
              mp = mParameter;
          }

          if (mp == null)
          {

            mp = new MetadataParameter();
            mp.setParamName(mdText);
            mp.setCodeSystem(parameter.getCodeSystem());
            hb_session.save(mp);
          }

          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit CodeSystem-ID: " + mp.getCodeSystem().getId() + ", MD-ID: " + mp.getId());
        }

        //Adding Version Information 
        CodeSystem cs = (CodeSystem) parameter.getCodeSystem();

        for (CodeSystemVersion csvL : parameter.getCodeSystem().getCodeSystemVersions())
        {

          if (csvL.getVersionId().equals(cs.getCurrentVersionId()))
          {

            cs.getCodeSystemVersions().clear();
            cs.getCodeSystemVersions().add(csvL);
            break;
          }
        }
        parameter.setCodeSystem(cs);

        csId = cs.getId();
        csvId = cs.getCodeSystemVersions().iterator().next().getVersionId();

        while (csv.readRecord())
        {

          CreateConceptRequestType request = new CreateConceptRequestType();

          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(replaceApo(csv.get("code")));
          csc.setIsPreferred(true);
          csc.setTerm(replaceApo(csv.get("displayName")));
          csc.setTermAbbrevation("");
          csc.setDescription(replaceApo(csv.get("concept_beschreibung")));
          csc.setMeaning(replaceApo(csv.get("deutsch")));
          csc.setHints(replaceApo(csv.get("hinweise")));

          logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
          membership.setIsMainClass(Boolean.TRUE);
          membership.setIsAxis(Boolean.FALSE);

          request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
          request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);

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
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session, "");

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {

              count++;
              // Metadaten einfügen
              for (int i = 0; i < countMp; i++)
              {

                // Metadaten einfügen
                String mdValue = csv.get(startIndex + i);//Achtung in Maps lowerCase
                String mdParam = firstCharUpperCase(replaceApo(csv.getHeader(startIndex + i)));
                if (mdValue != null && mdValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                  String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                  hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                  HQLParameterHelper parameterHelper = new HQLParameterHelper();
                  parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get(mdParam));
                  parameterHelper.addParameter("csev.", "versionId", response.getCodeSystemEntity().getCurrentVersionId());

                  // Parameter hinzufügen (immer mit AND verbunden)
                  hql += parameterHelper.getWhere("");
                  logger.debug("HQL: " + hql);

                  // Query erstellen
                  org.hibernate.Query q = hb_session.createQuery(hql);
                  // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                  parameterHelper.applyParameter(q);

                  List<CodeSystemMetadataValue> valueList = q.list();

                  if (valueList.size() == 1)
                  {
                    valueList.get(0).setParameterValue(mdValue);
                  }

                  if (mdParam.equals("Level"))
                  {

                    if (Integer.valueOf(mdValue) < lowestLevel)
                      lowestLevel = Integer.valueOf(mdValue);
                  }

                  logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                  hb_session.update(valueList.get(0));
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
          resultStr = DeleteTermHelperWS.deleteCS_CSV(hb_session, onlyCSV, csId, csvId);
          hb_session.getTransaction().rollback();
          reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          hb_session.getTransaction().commit();
          String sortMessage = "";
          if (sort(parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId()))
          {
            sortMessage = "Sortierung erfolgreich!";
          }
          else
          {
            sortMessage = "Sortierung fehlgeschlagen!";
          }

          countImported = count;
          reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler | " + sortMessage);
          reponse.setCodeSystem(parameter.getCodeSystem());
        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        logger.error(ex.getMessage());
        s = "Fehler beim Import einer CSV-Datei: " + ex.getLocalizedMessage();

        try
        {
          hb_session.getTransaction().rollback();

          resultStr = DeleteTermHelperWS.deleteCS_CSV(hb_session, onlyCSV, csId, csvId);

          logger.info("[ImportSVS.java] Rollback durchgeführt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportSVS.java] Rollback fehlgeschlagen!");
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
      s = "Fehler beim Import: " + ex.getLocalizedMessage();
      logger.error(s);
    }

    return s;
  }

  private boolean createCodeSystem(org.hibernate.Session hb_session)
  {
    ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).setStatus(1);
//    if (parameter.getImportInfos().getRole().equals(CODES.ROLE_ADMIN))
//      ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).setStatus(1);
//
//    if (parameter.getImportInfos().getRole().equals(CODES.ROLE_INHALTSVERWALTER))
//      ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).setStatus(0);

    // TODO zunächst prüfen, ob CodeSystem bereits existiert
    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
    request.setCodeSystem(parameter.getCodeSystem());
    request.setLoginToken(parameter.getLoginToken());

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      return false;
    }
    parameter.setCodeSystem(resp.getCodeSystem());

    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    return true;
  }

  private boolean sort(long codeSystemVersionId)
  {

    //get List of concepts or whatever
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    boolean s = false;
    try
    {
      String hql = "select distinct csev from CodeSystemEntityVersion csev";
      hql += " join fetch csev.codeSystemMetadataValues csmv";
      hql += " join fetch csmv.metadataParameter mp";
      hql += " join fetch csev.codeSystemEntity cse";
      hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
      hql += " join fetch csvem.codeSystemVersion csv";

      HQLParameterHelper parameterHelper = new HQLParameterHelper();
      parameterHelper.addParameter("csv.", "versionId", codeSystemVersionId);

      // Parameter hinzufügen (immer mit AND verbunden)
      hql += parameterHelper.getWhere("");
      logger.debug("HQL: " + hql);

      // Query erstellen
      org.hibernate.Query q = hb_session.createQuery(hql);

      // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
      parameterHelper.applyParameter(q);

      List<CodeSystemEntityVersion> csevList = q.list();

      HashMap<Integer, CodeSystemEntityVersion> workList = new HashMap<Integer, CodeSystemEntityVersion>();
      ArrayList<Integer> sortList = new ArrayList<Integer>();

      Iterator<CodeSystemEntityVersion> iter = csevList.iterator();
      while (iter.hasNext())
      {
        boolean found = false;
        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) iter.next();

        //Get the Level
        int level = 0;
        Iterator<CodeSystemMetadataValue> iter1 = csev.getCodeSystemMetadataValues().iterator();
        while (iter1.hasNext())
        {
          CodeSystemMetadataValue csmv = (CodeSystemMetadataValue) iter1.next();
          if (csmv.getMetadataParameter().getParamName().equals("Level"))
          {

            String cleaned = csmv.getParameterValue();

            if (cleaned.contains(" "))
              cleaned = cleaned.replace(" ", "");

            level = Integer.valueOf(cleaned);
          }
        }

        if (level == lowestLevel)
        {

          CodeSystemVersionEntityMembershipId csvemId = null;
          Set<CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
          Iterator<CodeSystemVersionEntityMembership> it = csvemSet.iterator();
          while (it.hasNext())
          {

            CodeSystemVersionEntityMembership member = (CodeSystemVersionEntityMembership) it.next();
            if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                    && member.getCodeSystemVersion().getVersionId().equals(codeSystemVersionId))
            {

              csvemId = member.getId();
            }
          }

          CodeSystemVersionEntityMembership c = (CodeSystemVersionEntityMembership) hb_session.get(CodeSystemVersionEntityMembership.class, csvemId);
          c.setIsMainClass(Boolean.TRUE);
          hb_session.update(c);

          workList.put(level, csev);
          sortList.add(level);
        }
        else
        {

          int size = sortList.size();
          int count = 0;
          while (!found)
          {

            if ((sortList.get(size - (1 + count)) - level) == -1)
            {

              //Setting MemberShip isMainClass false
              CodeSystemVersionEntityMembershipId csvemId = null;
              Set<CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
              Iterator<CodeSystemVersionEntityMembership> it = csvemSet.iterator();
              while (it.hasNext())
              {

                CodeSystemVersionEntityMembership member = (CodeSystemVersionEntityMembership) it.next();
                if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                        && member.getCodeSystemVersion().getVersionId().equals(codeSystemVersionId))
                {

                  csvemId = member.getId();
                }
              }

              CodeSystemVersionEntityMembership c = (CodeSystemVersionEntityMembership) hb_session.get(CodeSystemVersionEntityMembership.class, csvemId);
              c.setIsMainClass(Boolean.FALSE);
              hb_session.update(c);

              found = true;
              sortList.add(level);
              workList.put(level, csev);
              CodeSystemEntityVersion csevPrev = workList.get(sortList.get(size - (1 + count)));

              CodeSystemEntityVersionAssociation association = new CodeSystemEntityVersionAssociation();
              association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
              association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csevPrev.getVersionId());
              association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
              association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csev.getVersionId());
              association.setAssociationKind(2); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
              association.setLeftId(csevPrev.getVersionId()); // immer linkes Element also csev1
              association.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
              association.getAssociationType().setCodeSystemEntityVersionId(4L);
              // Weitere Attribute setzen
              association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
              association.setStatusDate(new Date());
              association.setInsertTimestamp(new Date());
              // Beziehung abspeichern
              hb_session.save(association);

              CodeSystemEntityVersion csevLoc = (CodeSystemEntityVersion)hb_session.get(CodeSystemEntityVersion.class, csevPrev.getVersionId());
              if(csevLoc.getIsLeaf() != Boolean.FALSE){
                  csevLoc.setIsLeaf(Boolean.FALSE);
                  hb_session.update(csevLoc);
              }
              
            }
            else
            {
              count++;
              found = false;
            }
          }
        }
      }

      hb_session.getTransaction().commit();
      s = true;
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      s = false;
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
    return s;
  }

  private String firstCharUpperCase(String str)
  {

    String a = str.substring(0, 1);
    String b = str.substring(1);
    a = a.toUpperCase();
    return a + b;
  }

  private String replaceApo(String str)
  {

    if (str.startsWith("\"") && str.endsWith("\""))
    {

      str = str.replaceFirst("\"", "");
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }
}
