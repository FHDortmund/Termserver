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

import de.fhdo.terminologie.ws.administration._export.ExportValueSetCSV;
import de.fhdo.terminologie.ws.administration._export.ExportValueSetSVS;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ExportValueSetContent
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public ExportValueSetContentResponseType ExportValueSetContent(ExportValueSetContentRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== ExportValueSetContent gestartet ======");
    }

    // Return-Informationen anlegen
    ExportValueSetContentResponseType response = new ExportValueSetContentResponseType();
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

    // TODO loggedIn = true rausnehmen, nachdem der Service ausgiebig getestet ist.
    loggedIn = true;
    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return response;
    }

    long formatId = parameter.getExportInfos().getFormatId();

    if (formatId == ExportValueSetContentRequestType.EXPORT_CSV_ID)
    {
      try
      {
        ExportValueSetCSV exportCSV = new ExportValueSetCSV(parameter);

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
    } else if(formatId == ExportValueSetContentRequestType.EXPIRT_SVS_ID){
        try
      {
        ExportValueSetSVS exportSVS = new ExportValueSetSVS(parameter);

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
    }else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Das Export-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ExportValueSetContentRequestType.getPossibleFormats());
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
  private boolean validateParameter(ExportValueSetContentRequestType parameter, ExportValueSetContentResponseType response)
  {
    // TODO validate für ExportValueSetContent implementieren
    // (siehe Crossreference)
    return true;
  }
}
