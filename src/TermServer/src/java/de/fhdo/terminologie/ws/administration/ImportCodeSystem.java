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
package de.fhdo.terminologie.ws.administration;

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.ws.administration._import.ImportCSSVS;
import de.fhdo.terminologie.ws.administration._import.ImportCS_CSV;
import de.fhdo.terminologie.ws.administration._import.ImportClaml;
import de.fhdo.terminologie.ws.administration._import.ImportICDBMGAT;
import de.fhdo.terminologie.ws.administration._import.ImportKAL;
import de.fhdo.terminologie.ws.administration._import.ImportKBV;
import de.fhdo.terminologie.ws.administration._import.ImportLOINC;
import de.fhdo.terminologie.ws.administration._import.ImportLeiKatAt;
import de.fhdo.terminologie.ws.administration._import.ImportMeSH;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;
import org.hibernate.Query;

/**
 * 28.03.2012, Mützner: Hinzufügen vom LOINC-Import 27.02.2013, Mützner: KBV
 * Keytabs hinzugefügt
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCodeSystem
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ImportCodeSystemResponseType ImportCodeSystem(ImportCodeSystemRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCodeSystem started ======");

    // Return-Informationen anlegen
    ImportCodeSystemResponseType response = new ImportCodeSystemResponseType();
    response.setReturnInfos(new ReturnType());

    if (StaticStatus.importRunning)
    {
      // Fehlermeldung ausgeben (Import kann nur 1x laufen)
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Another import is running. Please wait until it is finished.");
      return response;
    }

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    AuthenticateInfos loginInfoType = null;
    if (parameter != null && parameter.getLoginToken() != null)
    {
      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType != null;
    }

    logger.debug("logged in: " + loggedIn);

    if (loggedIn == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Für diesen Dienst müssen Sie am Terminologieserver angemeldet sein!");
      return response;
    }

    long formatId = parameter.getImportInfos().getFormatId();

    if (formatId == ImportCodeSystemRequestType.IMPORT_CLAML_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        ImportClaml importClaml = new ImportClaml(loginInfoType);
        importClaml.startImport(parameter);

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        if (StaticStatus.cancel)
          response.getReturnInfos().setMessage("Import cancelled.");
        else
          response.getReturnInfos().setMessage("Import completed.");
        response.getReturnInfos().setCount(importClaml.getCountImported());
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at ClaML import: " + e.getLocalizedMessage());
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_CSV_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        String s = "";
        int countImported = 0;

        ImportCS_CSV importCSV = new ImportCS_CSV(parameter, loginInfoType);

        s = importCSV.importCSV(response, ipAddress);
        countImported = importCSV.getCountImported();

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(countImported);
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at CSV import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at CSV import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        ImportLOINC importLOINC = new ImportLOINC(parameter, loginInfoType);

        String s = importLOINC.importLOINC(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importLOINC.getCountImported());
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at LOINC import: " + s);
        }
        logger.warn("LOINC import finished");
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at LOINC import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_RELATIONS_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        ImportLOINC importLOINC = new ImportLOINC(parameter, loginInfoType);

        String s = importLOINC.importLOINC_Associations(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importLOINC.getCountImported());
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at LOINC relationship import: " + s);
        }
        logger.warn("LOINC-Associations Import-Ende");
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at LOINC relationship import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_KBV_KEYTABS_ID)
    {
      try
      {
        //StaticStatus.importRunning = true;
        ImportKBV importKBV = new ImportKBV(parameter, loginInfoType);

        String s = importKBV.importXML(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importKBV.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at KBV import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at KBV import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_SVS_ID)
    {

      try
      {
        StaticStatus.importRunning = true;
        ImportCSSVS importCS_SVS = new ImportCSSVS(parameter, loginInfoType);
        String s = importCS_SVS.importSVS(response, ipAddress);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importCS_SVS.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at SVS import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at SVS import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LeiKat_ID)
    {

      try
      {
        StaticStatus.importRunning = true;
        ImportLeiKatAt importLeiKatAt = new ImportLeiKatAt(parameter);
        String s = importLeiKatAt.importLeiKatAt(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importLeiKatAt.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_KAL_ID)
    {

      try
      {
        StaticStatus.importRunning = true;
        ImportKAL importKAL = new ImportKAL(parameter, loginInfoType);
        String s = importKAL.importKAL(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importKAL.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Error at import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_ICD_BMG_ID)
    {

      try
      {
        StaticStatus.importRunning = true;
        ImportICDBMGAT importICDBMGAT = new ImportICDBMGAT(parameter);
        String s = importICDBMGAT.importICDBMGAT(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importICDBMGAT.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_MESH_XML_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        ImportMeSH importMeSH = new ImportMeSH(loginInfoType);
        importMeSH.startImport(parameter);

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        if (StaticStatus.cancel)
          response.getReturnInfos().setMessage("Import cancelled.");
        else
          response.getReturnInfos().setMessage("Import completed.");
        response.getReturnInfos().setCount(importMeSH.getCountImported());
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        String msg = e.getLocalizedMessage();
        if (msg == null || msg.length() == 0)
        {
          if (e.getCause() != null)
            msg = e.getCause().getLocalizedMessage();
        }

        response.getReturnInfos().setMessage("Error at import: " + msg);

        LoggingOutput.outputException(e, this);
      }

    }
    else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Import format unknown with id: " + formatId + "\n" + ImportCodeSystemRequestType.getPossibleFormats());
    }

    StaticStatus.importRunning = false;
    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ImportCodeSystemRequestType Request,
                                    ImportCodeSystemResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getImportInfos() == null)
    {
      Response.getReturnInfos().setMessage("ImportInfos may not be NULL!");
      erfolg = false;
    }
    else
    {
      if (Request.getImportInfos().getFormatId() == null || Request.getImportInfos().getFormatId() == 0)
      {
        // TODO auf gültiges Format prüfen
        Response.getReturnInfos().setMessage("You have to give an import format:\n" + ImportCodeSystemRequestType.getPossibleFormats());
        erfolg = false;
      }
    }

    // check version name
    if (Request != null && Request.getCodeSystem() != null && Request.getCodeSystem().getCodeSystemVersions() != null
            && Request.getCodeSystem().getCodeSystemVersions().size() > 0 && Request.getCodeSystem().getId() > 0)
    {
      logger.debug("Check, if version for given code system with id " + Request.getCodeSystem().getId() + " already exists");
      
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        String csv_name = Request.getCodeSystem().getCodeSystemVersions().iterator().next().getName();
        Query q = hb_session.createQuery("from CodeSystemVersion csv join csv.codeSystem cs where csv.name=:csv_name and cs.id=:cs_id");
        q.setParameter("csv_name", csv_name);
        q.setParameter("cs_id", Request.getCodeSystem().getId());
        
        List list = q.list();
        if(list != null && list.size() > 0)
        {
          // name already exists
          erfolg = false;
          Response.getReturnInfos().setMessage("Codesystem-Version name already exists: " + csv_name + ". Please select a version name that does not exist.");
        }
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
    }

    if (Request.getLoginToken() == null || Request.getLoginToken().length() == 0)
    {
      Response.getReturnInfos().setMessage("Login token may not be empty!");
      erfolg = false;
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
