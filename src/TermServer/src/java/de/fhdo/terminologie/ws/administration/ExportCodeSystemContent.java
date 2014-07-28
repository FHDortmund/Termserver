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

import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration._export.ExportCSV;
import de.fhdo.terminologie.ws.administration._export.ExportClaml;
import de.fhdo.terminologie.ws.administration._export.ExportCodeSystemSVS;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Bernhard Rimatzki
 */
public class ExportCodeSystemContent
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Erstellt eine neue Domäne mit den angegebenen Parametern
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public ExportCodeSystemContentResponseType ExportCodeSystemContent(ExportCodeSystemContentRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== ExportCodeSystemContent gestartet ======");
    }

    // Return-Informationen anlegen
    ExportCodeSystemContentResponseType response = new ExportCodeSystemContentResponseType();
    response.setReturnInfos(new ReturnType());

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

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    loggedIn = true;
    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return response;
    }

    if (parameter == null || parameter.getExportInfos() == null || parameter.getExportInfos().getFormatId() == null)
    {

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Request->ExportInfos->formatId darf nicht NULL sein!");
      return response;
    }

    long formatId = parameter.getExportInfos().getFormatId();

    if (formatId == ExportCodeSystemContentRequestType.EXPORT_CLAML_ID)
    {
      try
      {
        ExportClaml exportClaML = new ExportClaml();
        response = exportClaML.export(parameter);

        /*response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
         response.getReturnInfos().setStatus(ReturnType.Status.OK);
         response.getReturnInfos().setMessage("ClaML-Export abgeschlossen.");
         response.getReturnInfos().setCount(exportClaML.getCountImported());*/
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim ClaML-Export: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ExportCodeSystemContentRequestType.EXPORT_CSV_ID)
    {
      try
      {
        ExportCSV exportCSV = new ExportCSV(parameter);

        String s = exportCSV.exportCSV(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(exportCSV.getCountExported());
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim CSV-Export: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ExportCodeSystemContentRequestType.EXPORT_SVS_ID)
    {
      try
      {
        ExportCodeSystemSVS exportSVS = new ExportCodeSystemSVS(parameter);

        String s = exportSVS.exportSVS(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(exportSVS.getCountExported());
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim SVS-Export: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Das Export-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ExportCodeSystemContentRequestType.getPossibleFormats());
    }

    return response;

  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ExportCodeSystemContentRequestType parameter, ExportCodeSystemContentResponseType response)
  {
    String s = "";
    if(parameter.getCodeSystem() == null)
    {
      s = "Es muss ein Codesystem mitgegeben werden.";
    }
    else
    {
      if(parameter.getCodeSystem().getId() == null || parameter.getCodeSystem().getId().longValue() == 0)
      {
        s = "Es muss eine ID für ein Codesystem mitgegeben werden.";
      }
    }

    if(s.length() > 0)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage(s);
      return false;
    }
    

    return true;
  }
}
