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

import de.fhdo.terminologie.ws.administration._import.ImportCSSVS;
import de.fhdo.terminologie.ws.administration._import.ImportCS_CSV;
import de.fhdo.terminologie.ws.administration._import.ImportClaml;
import de.fhdo.terminologie.ws.administration._import.ImportICDBMGAT;
import de.fhdo.terminologie.ws.administration._import.ImportKAL;
import de.fhdo.terminologie.ws.administration._import.ImportKBV;
import de.fhdo.terminologie.ws.administration._import.ImportLOINC;
import de.fhdo.terminologie.ws.administration._import.ImportLeiKatAt;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;

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
      logger.info("====== ImportCodeSystem gestartet ======");

    // Return-Informationen anlegen
    ImportCodeSystemResponseType response = new ImportCodeSystemResponseType();
    response.setReturnInfos(new ReturnType());

    if (StaticStatus.importRunning)
    {
      // Fehlermeldung ausgeben (Import kann nur 1x laufen)
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Ein Import läuft bereits. Warten Sie darauf, bis dieser beendet ist und versuchen Sie es anschließend erneut.");
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

    logger.debug("Eingeloggt: " + loggedIn);

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
        ImportClaml importClaml = new ImportClaml(parameter);

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        if (StaticStatus.cancel)
          response.getReturnInfos().setMessage("Import abgebrochen.");
        else
          response.getReturnInfos().setMessage("Import abgeschlossen.");
        response.getReturnInfos().setCount(importClaml.getCountImported());
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_CSV_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        String s = "";
        int countImported = 0;

        ImportCS_CSV importCSV = new ImportCS_CSV(parameter);

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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        ImportLOINC importLOINC = new ImportLOINC(parameter);

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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
        logger.warn("LOINC Import-Ende");
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_RELATIONS_ID)
    {
      try
      {
        StaticStatus.importRunning = true;
        ImportLOINC importLOINC = new ImportLOINC(parameter);

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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
        logger.warn("LOINC-Associations Import-Ende");
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_KBV_KEYTABS_ID)
    {
      try
      {
        //StaticStatus.importRunning = true;
        ImportKBV importKBV = new ImportKBV(parameter);

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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_SVS_ID)
    {

      try
      {
        StaticStatus.importRunning = true;
        ImportCSSVS importCS_SVS = new ImportCSSVS(parameter);
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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_KAL_ID)
    {

      try
      {
        StaticStatus.importRunning = true;
        ImportKAL importKAL = new ImportKAL(parameter);
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
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
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

        e.printStackTrace();
      }

    }
    else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Das Import-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ImportCodeSystemRequestType.getPossibleFormats());
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
      Response.getReturnInfos().setMessage("ImportInfos darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      /*if (Request.getImportInfos().getFilecontent() == null)
       {
       Response.getReturnInfos().setMessage("Sie müssen eine Datei anhängen (filecontent)!");
       erfolg = false;
       }
       else*/ if (Request.getImportInfos().getFormatId() == null || Request.getImportInfos().getFormatId() == 0)
      {
        // TODO auf gültiges Format prüfen
        Response.getReturnInfos().setMessage("Sie müssen ein Import-Format angeben!");
        erfolg = false;
      }
    }

    if (Request.getLoginToken() == null || Request.getLoginToken().length() == 0)
    {
      Response.getReturnInfos().setMessage(
              "Das Login-Token darf nicht leer sein!");
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
