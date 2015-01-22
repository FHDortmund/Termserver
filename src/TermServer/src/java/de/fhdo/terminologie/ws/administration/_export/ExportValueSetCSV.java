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
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.search.ListValueSetContents;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetails;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ExportValueSetCSV
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ExportValueSetContentRequestType parameter;
  private int countExported = 0;
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");

  public ExportValueSetCSV(ExportValueSetContentRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ExportCSV gestartet ======");

    parameter = _parameter;
  }

  public String exportCSV(ExportValueSetContentResponseType reponse)
  {
    String s = "";  // Status-Meldung
    //int count = countExported;

    CsvWriter csv;
    ExportType exportType = new ExportType();
    HashMap<Integer, String> paramList = new HashMap<Integer, String>();

    //hb_session.getTransaction().begin();
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      csv = new CsvWriter(bos, ';', Charset.forName("ISO-8859-1")); // TODO Charset prüfen
      csv.setTextQualifier('\'');
      csv.setForceQualifier(true);

      try
      {

        //Request-Parameter für ReturnValueSetDetails erstellen
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] Erstelle Request-Parameter für ReturnValueSetDetails");

        ReturnValueSetDetailsRequestType requestValueSetDetails = new ReturnValueSetDetailsRequestType();
        requestValueSetDetails.setValueSet(parameter.getValueSet());
        requestValueSetDetails.getValueSet().getValueSetVersions().add((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]);
        requestValueSetDetails.setLoginToken(parameter.getLoginToken());

        //ValueSetDetails abrufen
        ReturnValueSetDetails rcsd = new ReturnValueSetDetails();
        ReturnValueSetDetailsResponseType responseValueSetDetails = rcsd.ReturnValueSetDetails(requestValueSetDetails, "");
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] ReturnValueSetDetails abgerufen");

        /*if (parameter.getExportInfos().isUpdateCheck())
        {
          if (responseValueSetDetails.getReturnInfos().getStatus() == ReturnType.Status.OK
                  && responseValueSetDetails.getValueSet() != null)
          {
            if (!responseValueSetDetails.getValueSet().getCurrentVersionId().equals(((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).getVersionId()))
            {
              ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).setVersionId(responseValueSetDetails.getValueSet().getCurrentVersionId());
            }
          }
        }*/
        
        

        ListValueSetContentsRequestType requestListCodeSystemConcepts = new ListValueSetContentsRequestType();
        requestListCodeSystemConcepts.setValueSet(parameter.getValueSet());
        requestListCodeSystemConcepts.getValueSet().getValueSetVersions().add((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]);
        requestListCodeSystemConcepts.setLoginToken(parameter.getLoginToken());
        //requestListCodeSystemConcepts.setLookForward(true);
        
        boolean syncEnabled = parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null;
        if(syncEnabled)
        {
          // Datum für Synchronisation hinzufgüen
          logger.debug("Datum für Synchronisation hinzufgüen: " + parameter.getExportParameter().getDateFrom().toString());
          
          ValueSetVersion vsv = ((ValueSetVersion) requestListCodeSystemConcepts.getValueSet().getValueSetVersions().toArray()[0]);
          if(vsv.getConceptValueSetMemberships() == null || vsv.getConceptValueSetMemberships().size() == 0)
          {
            ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
            cvsm.setStatusDate(parameter.getExportParameter().getDateFrom());
            vsv.getConceptValueSetMemberships().add(cvsm);
          }
          else
          {
            ((ConceptValueSetMembership)vsv.getConceptValueSetMemberships().toArray()[0]).setStatusDate(parameter.getExportParameter().getDateFrom());
          }
        }

        //ListCodeSystemConcepts abrufen
        ListValueSetContents lcsc = new ListValueSetContents();
        ListValueSetContentsResponseType responseListCodeSystemConcepts = lcsc.ListValueSetContents(requestListCodeSystemConcepts, "");
        if (logger.isInfoEnabled())
          logger.info("[ExportCSV] ListValueSetContents abgerufen");

        org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
        try
        {
          String hqlM = "select distinct mp from MetadataParameter mp join mp.valueSet vs"
                  + " where vs.id=:id";

          org.hibernate.Query qM = hb_session.createQuery(hqlM);
          qM.setLong("id", parameter.getValueSet().getId());
          List<MetadataParameter> mlist = qM.list();

        //=================================================
          // CSV-Header erstellen und Dauerattribute auslesen
          //=================================================
          //csv.write("level");
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
          }

          //ENDE Header erstellen
          csv.endRecord();

          // Flaches Vokabular, einfach alle Einträge exportieren
          for (CodeSystemEntity cse : responseListCodeSystemConcepts.getCodeSystemEntity())
          {
            for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
            {
              // Nur aktuellste Version exportieren
              if (cse.getCurrentVersionId().longValue() == csev.getVersionId().longValue())
              {

                ConceptValueSetMembership cvsm = null;
                for (ConceptValueSetMembership cvsmL : csev.getConceptValueSetMemberships())
                {

                  Long cvsmLVsvId = cvsmL.getId().getValuesetVersionId();
                  Long paramVsvId = parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId();
                  Long cvsmCsevId = cvsmL.getId().getCodeSystemEntityVersionId();
                  Long paramCsevId = csev.getVersionId();
                  if ((cvsmLVsvId.equals(paramVsvId)) && (cvsmCsevId.equals(paramCsevId)))
                  {
                    cvsm = cvsmL;
                    break;
                  }
                }
                
                // 1) nur Status=1
                // 2) alle Status > 0, wenn Synchronisierungsdatum angegeben ist
                if (cvsm != null && cvsm.getStatus() != null && (cvsm.getStatus() == 1 || (syncEnabled && cvsm.getStatus() > 0) ))
                {
                  CodeSystemConcept csc = csev.getCodeSystemConcepts().iterator().next();

                  if (logger.isDebugEnabled())
                    logger.debug("Schreibe Code: " + csc.getCode());

                  if (csc.getCode() != null)
                  {
                    csv.write(csc.getCode());
                  }
                  else
                  {
                    csv.write("");
                  }
                  /*String hqlC = "select distinct csv from CodeSystemVersion csv join csv.codeSystemVersionEntityMemberships csvem join csvem.codeSystemEntity cse join cse.codeSystemEntityVersions csev"
                   + " where csev.versionId=:versionId";

                   org.hibernate.Query qC = hb_session.createQuery(hqlC);
                   qC.setLong("versionId", csev.getVersionId());

                   List<CodeSystemVersion> csvlist = qC.list();*/

                  if (cse.getCodeSystemVersionEntityMemberships().iterator().next().getCodeSystemVersion().getOid() != null)
                  {
                    csv.write(cse.getCodeSystemVersionEntityMemberships().iterator().next().getCodeSystemVersion().getOid());
                  }
                  else
                  {
                    csv.write("");
                  }
                  /*if(csvlist.size() == 1){

                   csv.write(csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().getCodeSystemVersion().getOid());
                   }else{
                   csv.write("");
                   }*/

                  if (csc.getTerm() != null)
                  {
                    csv.write(csc.getTerm());
                  }
                  else
                  {
                    csv.write("");
                  }
                  csv.write("");
                  if (cvsm.getDescription() != null)
                  {
                    csv.write(cvsm.getDescription());
                  }
                  else
                  {
                    csv.write("");
                  }

                  if (cvsm.getMeaning() != null)
                  {
                    csv.write(cvsm.getMeaning());
                  }
                  else
                  {
                    csv.write("");
                  }

                  if (cvsm.getHints() != null)
                  {
                    csv.write(cvsm.getHints());
                  }
                  else
                  {
                    csv.write("");
                  }

                  //Get vsmv for csev/cvsm
                  String hqlMv = "select distinct vsmv from ValueSetMetadataValue vsmv join vsmv.metadataParameter mp join vsmv.codeSystemEntityVersion csev"
                          + " where vsmv.valuesetVersionId=:valuesetVersionId AND csev.versionId=:codeSystemEntityVersionId";

                  org.hibernate.Query qMv = hb_session.createQuery(hqlMv);
                  qMv.setLong("valuesetVersionId", parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                  qMv.setLong("codeSystemEntityVersionId", csev.getVersionId());
                  List<ValueSetMetadataValue> vsmvList = qMv.list();

                  for (int i = 0; i < paramList.size(); i++)
                  {

                    ValueSetMetadataValue vsmv = null;
                    for (ValueSetMetadataValue vsmvL : vsmvList)
                    {

                      if (vsmvL.getMetadataParameter().getParamName().equals(paramList.get(i)))
                      {
                        vsmv = vsmvL;
                        break;
                      }
                    }

                    if (vsmv != null && vsmv.getParameterValue() != null)
                    {
                      csv.write(vsmv.getParameterValue());
                    }
                    else
                    {
                      csv.write("");
                    }
                  }
                  csv.endRecord();
                  countExported++;
                }
              }
            }
          }

          if (logger.isInfoEnabled())
            logger.info("[ExportCSV] CSV-Inhalt erstellt");
        }
        catch (Exception ex)
        {
          logger.error(ex.getMessage());
          s = "Fehler (hibernate): " + ex.getLocalizedMessage();
        }
        finally
        {
          hb_session.close();
        }
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
      exportType.setFormatId(ExportValueSetContentRequestType.EXPORT_CSV_ID);

      //ExportInfos in Response schreiben
      reponse.setExportInfos(exportType);
      //hb_session.getTransaction().commit();

    }
    catch (Exception ex)
    {
      s = "Fehler: " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    return s;
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
