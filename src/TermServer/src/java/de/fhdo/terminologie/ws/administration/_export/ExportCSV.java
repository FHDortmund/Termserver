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

import com.csvreader.CsvWriter;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;

import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetails;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType.Status;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * 30.04.2013: Export erweitert
 *
 * @author Nico Hänsch, Robert Mützner
 */
public class ExportCSV
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ExportCodeSystemContentRequestType parameter;
  private int countExported = 0;
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
  private HashMap<Integer, String> paramList = null;
  private org.hibernate.Session hb_session = null;
  boolean levelExists = false;
  ArrayList<Entry> entryList = new ArrayList<Entry>();

  public ExportCSV(ExportCodeSystemContentRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ExportCSV gestartet ======");

    parameter = _parameter;
  }

  public String exportCSV(ExportCodeSystemContentResponseType reponse)
  {
    String s = "";  // Status-Meldung
    //int count = countExported;

    CsvWriter csv;
    ExportType exportType = new ExportType();
    paramList = new HashMap<Integer, String>();
    hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      //TODO URL erstellen und setzen (Namenskonvention?) 
      //TODO ggf. auf bereits identischen Export-File prüfen
      //String csv_output_url = "/var/lib/tomcat6/webapps/csv_test_output.csv";
      //csv = new CsvWriter(new FileWriter(csv_output_url), ';');

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      csv = new CsvWriter(bos, ';', Charset.forName("ISO-8859-1")); // TODO Charset prüfen
      csv.setTextQualifier('\'');
      csv.setForceQualifier(true);

      try
      {
        //Request-Parameter für ReturnCodeSystemDetails erstellen
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] Erstelle Request-Parameter für ReturnCodeSystemDetails");

        ReturnCodeSystemDetailsRequestType requestCodeSystemDetails = new ReturnCodeSystemDetailsRequestType();
        requestCodeSystemDetails.setCodeSystem(parameter.getCodeSystem());
        if(requestCodeSystemDetails.getCodeSystem() != null && requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions() != null)
          requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions().add((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]);
        requestCodeSystemDetails.setLoginToken(parameter.getLoginToken());

        //CodeSystemDetails abrufen
        ReturnCodeSystemDetails rcsd = new ReturnCodeSystemDetails();
        ReturnCodeSystemDetailsResponseType responseCodeSystemDetails = rcsd.ReturnCodeSystemDetails(requestCodeSystemDetails, "");
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] ReturnCodeSystemDetails abgerufen");

        if (parameter.getExportInfos().isUpdateCheck())
        {
          if (responseCodeSystemDetails.getReturnInfos().getStatus() == Status.OK
                  && responseCodeSystemDetails.getCodeSystem() != null)
          {
            if (!responseCodeSystemDetails.getCodeSystem().getCurrentVersionId().equals(((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId()))
            {
              ((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).setVersionId(responseCodeSystemDetails.getCodeSystem().getCurrentVersionId());

              requestCodeSystemDetails = new ReturnCodeSystemDetailsRequestType();
              requestCodeSystemDetails.setCodeSystem(parameter.getCodeSystem());
              requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions().add((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]);
              requestCodeSystemDetails.setLoginToken(parameter.getLoginToken());

              //CodeSystemDetails abrufen
              rcsd = new ReturnCodeSystemDetails();
              responseCodeSystemDetails = rcsd.ReturnCodeSystemDetails(requestCodeSystemDetails, "");
              if (logger.isInfoEnabled())
                logger.info("[ExportCSV] ReturnCodeSystemDetails abgerufen");
            }
          }
        }

        //Request-Parameter für ListCodeSystemConcepts erstellen
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] Erstelle Request-Parameter für ListCodeSystemConcepts");

        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] ListCodeSystemConcepts abgerufen");

        String hql = "select distinct csv from CodeSystemVersion csv join csv.codeSystem cs"
                + " where cs.id=:id and"
                + " csv.versionId=:versionId";

        org.hibernate.Query q = hb_session.createQuery(hql);
        q.setLong("id", parameter.getCodeSystem().getId());
        q.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

        List<CodeSystemVersion> csvList = q.list();
        CodeSystemVersion csversion = null;
        if (csvList != null && csvList.size() == 1)
        {
          csversion = csvList.get(0);
        }

        String hqlM = "select distinct mp from MetadataParameter mp join mp.codeSystem cs"
                + " where cs.id=:id";

        org.hibernate.Query qM = hb_session.createQuery(hqlM);
        qM.setLong("id", parameter.getCodeSystem().getId());
        List<MetadataParameter> mlist = qM.list();

        //=================================================
        // CSV-Header erstellen und Dauerattribute auslesen
        //=================================================
        csv.write("code");
        csv.write("codeSystem");
        csv.write("displayName");
        csv.write("parentCode");
        csv.write("concept_Beschreibung");
        csv.write("meaning");
        csv.write("hints");
        int count = 0;
        for (MetadataParameter mp : mlist)
        {
          String para = mp.getParamName();
          String b = para.substring(0, 1);
          b = b.toLowerCase();
          para = b + para.substring(1);

          csv.write(para); //lowerCase
          paramList.put(count, mp.getParamName());//Normal UpperCase
          count++;
          if (para.equals("level"))
            levelExists = true;
        }

        if (!levelExists)
          csv.write("level");

        //ENDE Header erstellen
        csv.endRecord();

        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] CSV-Header erstellt.");

        //CSV-Inhalt erstellen
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] Erstelle CSV-Inhalt mit Konzepten...");

        int countRoot = 0;

        String hqlC = "select distinct cse from CodeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join cse.codeSystemEntityVersions csev join csev.codeSystemConcepts csc"
                + " where csv.versionId=:versionId";

        if (parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null)
        {
          // Datum für Synchronisation hinzufügen
          hqlC += " and csev.statusVisibilityDate>:dateFrom";
        }
        
        org.hibernate.Query qC = hb_session.createQuery(hqlC);
        qC.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

        if (parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null)
        {
          // Datum für Synchronisation hinzufügen
          qC.setDate("dateFrom", parameter.getExportParameter().getDateFrom());
          logger.debug("Snych-Zeit: " + parameter.getExportParameter().getDateFrom().toString());
        }
        else
          logger.debug("keine Snych-Zeit angegeben");

        List<CodeSystemEntity> cselist = qC.list();
        for (CodeSystemEntity cse : cselist)
        {
          for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
          {
            // Nur aktuellste Version exportieren
            if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
            {

                CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();
                if(csev.getStatusVisibility() == 1){
                    if(logger.isDebugEnabled())
                      logger.debug("Schreibe Code: " + csc.getCode());
                    CodeSystemVersionEntityMembership member = cse.getCodeSystemVersionEntityMemberships().iterator().next();
                    writeEntry(csv, 0, csev, member, null, csversion);
               }
            }
          }
        }
        
        /*
        for (CodeSystemEntity cse : cselist)//responseListCodeSystemConcepts.getCodeSystemEntity())
        {
          CodeSystemVersionEntityMembership member = cse.getCodeSystemVersionEntityMemberships().iterator().next();
          boolean isAxis = false;
          boolean isMainClass = false;
          if (member.getIsAxis() != null)
            isAxis = member.getIsAxis().booleanValue();
          if (member.getIsMainClass() != null)
            isMainClass = member.getIsMainClass().booleanValue();

          if (isAxis || isMainClass)
          {
            countRoot++;

            for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
            {
              // Nur aktuellste Version exportieren
              if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
              {
                writeEntry(csv, 0, csev, member, null, csversion);

                // TODO Beziehungen ausgeben
                for (CodeSystemEntityVersionAssociation assChild : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
                {
                  if (assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                  {
                    long childId = assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
                    exportChild(csv, 1, childId, cselist, csev, csversion);
                  }
                }
              }
            }
          }
        }

        if (countRoot == 0)
        {
          // Flaches Vokabular, einfach alle Einträge exportieren
          for (CodeSystemEntity cse : cselist)
          {
            for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
            {
              // Nur aktuellste Version exportieren
              if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
              {
                writeEntry(csv, 0, csev, null, null, csversion);
              }
            }
          }
        }
        */
        /*Iterator<CodeSystemEntity> it_CSE = responseListCodeSystemConcepts.getCodeSystemEntity().iterator();

         while (it_CSE.hasNext())
         {
         Iterator<CodeSystemEntityVersion> it_CSEV = it_CSE.next().getCodeSystemEntityVersions().iterator();

         while (it_CSEV.hasNext())
         {
         Iterator<CodeSystemConcept> it_CSC = it_CSEV.next().getCodeSystemConcepts().iterator();

         while (it_CSC.hasNext())
         {
         temp_CSC = it_CSC.next();

         //CSV-Zeile erstellen (CodeSystemConcept)

         //CodeSystem-Name (immer)                
         csv.write(csvEntryCodeSystem);
         //CodeSystem-Beschreibung (Parameter codeSystemInfo == true)
         if (parameter.getExportParameter().getCodeSystemInfos())
         {
         csv.write(csvEntryCodeSystemDescription);
         }
         //CodeSystem-Version (immer)
         csv.write(csvEntryCodeSystemVersion);
         //CodeSystem-Version Beschreibung (Parameter codeSystemInfo == true)
         if (parameter.getExportParameter().getCodeSystemInfos())
         {
         csv.write(csvEntryCodeSystemVersionDescription);
         }
         //CodeSystem-OID (immer)
         csv.write(csvEntryCodeSystemOid);
         //CodeSystemVersion-Ablaufdatum (Parameter codeSystemInfo == true)
         if (parameter.getExportParameter().getCodeSystemInfos())
         {
         csv.write(csvEntryCodeSystemExpirationDate);
         }


         //Code (immer)
         csv.write(temp_CSC.getCode());
         //Term (immer)
         csv.write(temp_CSC.getTerm());
         //isPreferred (immer)
         if (temp_CSC.getIsPreferred())
         {
         csv.write("Bevorzugt");
         }
         else
         {
         csv.write("Nicht bevorzugt");
         }


         //Übersetzungen (Parameter translations == true)
         if (parameter.getExportParameter().getTranslations())
         {
         //TODO Translations in CSV-Inhalt                 
         }

         //Übersetzungen (Parameter associationInfos == true)
         if (!parameter.getExportParameter().getAssociationInfos().isEmpty())
         {
         //TODO AssociationInfos CSV-Header                                
         }

         count++;
         csv.endRecord();


         } //END CodeSystemConcept
         } //END CodeSystemEntityVersion
         } //END CodeSystemEntity
         */
        //ENDE CSV-Inhalt erstellen
        //Sort
        //Collections.sort(entryList, new AlphanumComparator());

        for (Entry e : entryList)
        {
          writeCsvEntry(csv, e.level, e.csev, e.csevParent, e.csv);
        }

        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] CSV-Inhalt erstellt");

      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        logger.error(ex.getMessage());
        s = "Fehler: " + ex.getLocalizedMessage();
      }

      //CSV-Datei schliessen
      csv.close();
      //if (logger.isInfoEnabled())
      //  logger.info("[ExportCSV] CSV-Datei geschrieben. Dateipfad: " + csv_output_url);

      if (countExported > 0)
      {
        //countExported = count;
        //CSV-Datei in Byte umwandeln

        /*File file = new File(csv_output_url);
         byte[] filecontent = new byte[(int) file.length()];
         FileInputStream fileInputStream = new FileInputStream(file);
         fileInputStream.read(filecontent);
         fileInputStream.close();
         if (logger.isInfoEnabled())
         logger.info("[ExportCSV] CSV-Datei in byte[] umgewandelt. (filecontent) ");*/
        //Filecontent setzen
        exportType.setFilecontent(bos.toByteArray());
        //Export-URL setzen
        //exportType.setUrl(csv_output_url);
        exportType.setUrl("");
        reponse.getReturnInfos().setMessage("Export abgeschlossen. " + countExported + " Konzepte exportiert.");
      }
      else
      {
        reponse.getReturnInfos().setMessage("Keine Konzepte exportiert.");
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] Kein Export erstellt...");
      }

      //Format-ID (CSV) setzen
      exportType.setFormatId(ExportCodeSystemContentRequestType.EXPORT_CSV_ID);

      //ExportInfos in Response schreiben
      reponse.setExportInfos(exportType);
    }
    catch (Exception ex)
    {
      s = "Fehler: " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    //hb_session.getTransaction().commit();
    hb_session.close();
    return s;
  }

  private void exportChild(CsvWriter csv, int level, long childEntityVersionId,
          List<CodeSystemEntity> entityList, CodeSystemEntityVersion parent, CodeSystemVersion csversion) throws Exception
  {
    for (CodeSystemEntity cse : entityList)
    {
      if (cse.getCurrentVersionId().longValue() == childEntityVersionId)
      {
        for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
        {
          // Nur aktuellste Version exportieren
          if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
          {
            CodeSystemVersionEntityMembership member = null;
            if (cse.getCodeSystemVersionEntityMemberships() != null
                    && cse.getCodeSystemVersionEntityMemberships().size() > 0)
            {
              member = cse.getCodeSystemVersionEntityMemberships().iterator().next();
            }

            writeEntry(csv, level, csev, member, parent, csversion);

            // Beziehungen darunter ausgeben
            for (CodeSystemEntityVersionAssociation assChild : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
            {
              if (assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
              {
                long childId = assChild.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
                exportChild(csv, level + 1, childId, entityList, csev, csversion);
              }
            }
          }
        }

        break;
      }
    }
  }

  private void writeEntry(CsvWriter csv, int level, CodeSystemEntityVersion csev,
          CodeSystemVersionEntityMembership member, CodeSystemEntityVersion csevParent, CodeSystemVersion csversion)
  {
    Entry e = new Entry();
    e.setCsev(csev);
    e.setCsevParent(csevParent);
    e.setLevel(level);
    e.setCsv(csversion);

    entryList.add(e);
  }

  private void writeCsvEntry(CsvWriter csv, int level, CodeSystemEntityVersion csev, CodeSystemEntityVersion csevParent, CodeSystemVersion csversion) throws Exception
  {

    CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();
    if (csev.getStatusVisibility() == 1)
    {
      logger.debug("Schreibe Code: " + csc.getCode());

      csv.write(csc.getCode());
      csv.write(csversion.getOid());
      csv.write(csc.getTerm());
      if (csevParent != null && csevParent.getCodeSystemConcepts() != null && !csevParent.getCodeSystemConcepts().isEmpty())
      {
        csv.write(csevParent.getCodeSystemConcepts().iterator().next().getCode());
      }
      else
      {
        csv.write("");
      }
      csv.write(csc.getDescription());
      csv.write(csc.getMeaning());
      csv.write(csc.getHints());

      /*
       //Get vsmv for csev/cvsm
       String hqlM = "select distinct csmv from CodeSystemMetadataValue csmv join csmv.metadataParameter mp join csmv.codeSystemEntityVersion csev"
       + " where csev.versionId=:versionId";

       org.hibernate.Query qM = hb_session.createQuery(hqlM);
       qM.setLong("versionId", csev.getVersionId());
       List<CodeSystemMetadataValue> csmvList = qM.list();
       */
      Set<CodeSystemMetadataValue> csmvList = csev.getCodeSystemMetadataValues();
      for (int i = 0; i < paramList.size(); i++)
      {

        CodeSystemMetadataValue csmv = null;
        for (CodeSystemMetadataValue csmvL : csmvList)
        {

          if (csmvL.getMetadataParameter().getParamName().equals(paramList.get(i)))
          {
            csmv = csmvL;
            break;
          }
        }

        if (csmv != null && csmv.getParameterValue() != null)
        {
          csv.write(csmv.getParameterValue());
        }
        else
        {
          csv.write("");
        }
      }

      if (!levelExists)
      {
        csv.write(formatOutput(level));
      }

      /*
       if (member != null)
       {
       if (member.getIsAxis() != null && member.getIsAxis().booleanValue())
       csv.write("1");
       else
       csv.write("0");

       if (member.getIsMainClass() != null && member.getIsMainClass().booleanValue())
       csv.write("1");
       else
       csv.write("0");
       }
       else
       {
       csv.write("0");
       csv.write("0");
       }

       if (csevParent == null)
       {
       csv.write("");
       }
       else
       {
       // Parent-ID angeben
       csv.write(formatOutput(csevParent.getVersionId()));
       }
       */
      csv.endRecord();
      countExported++;
    }
  }

  private String formatOutput(Object o)
  {
    if (o == null)
      return "";

    if (o instanceof String)
      return o.toString();
    else if (o instanceof Date)
    {
      return sdf.format(o);
    }
    else if (o instanceof Integer)
    {
      return ((Integer) o).toString();
    }
    else if (o instanceof Boolean)
    {
      if (((Boolean) o).booleanValue())
        return "1";
      else
        return "0";
    }
    else
      return o.toString();
  }

  /**
   * @return the countExported
   */
  public int getCountExported()
  {
    return countExported;
  }
}
