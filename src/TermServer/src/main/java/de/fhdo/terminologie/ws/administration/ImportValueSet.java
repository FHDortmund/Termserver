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

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration._import.ImportVS_SVS;
import de.fhdo.terminologie.ws.administration._import.ImportVS_CSV;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportValueSet
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ImportValueSetResponseType ImportValueSet(ImportValueSetRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportValueSet gestartet ======");

    // Return-Informationen anlegen
    ImportValueSetResponseType response = new ImportValueSetResponseType();
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

    logger.debug("Eingeloggt: " + loggedIn);

    if (loggedIn == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Für diesen Dienst müssen Sie am Terminologieserver angemeldet sein!");
      return response;
    }

    try
    {

      long formatId = parameter.getImportInfos().getFormatId();

      if (formatId == ImportValueSetRequestType.IMPORT_CSV_ID)
      {
        ImportVS_CSV import_vs = new ImportVS_CSV(parameter);
        import_vs.importCSV(response);

        /*response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
         response.getReturnInfos().setStatus(ReturnType.Status.OK);
         response.getReturnInfos().setMessage("Import abgeschlossen.");*/
        //response.getReturnInfos().setCount(importClaml.getCountImported());
      }
      else if (formatId == ImportValueSetRequestType.IMPORT_SVS_ID)
      {
        ImportVS_SVS importVS_SVS = new ImportVS_SVS(parameter);
        importVS_SVS.importSVS(response);

      }
      else
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Das Import-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ImportValueSetRequestType.getPossibleFormats());
      }
    }
    catch (Exception e)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
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
  private boolean validateParameter(ImportValueSetRequestType Request,
          ImportValueSetResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getImportInfos() == null)
    {
      Response.getReturnInfos().setMessage("ImportInfos darf nicht NULL sein!");
      erfolg = false;
    }

    if (Request.getLoginToken() == null || Request.getLoginToken().length() == 0)
    {
      Response.getReturnInfos().setMessage(
              "Das Login-Token darf nicht leer sein!");
      erfolg = false;
    }

    if (Request.getValueSet() == null)
    {
      Response.getReturnInfos().setMessage("ValueSet darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      ValueSet vs = Request.getValueSet();

      if ((vs.getId() == null || vs.getId() == 0) && (vs.getName() == null || vs.getName().length() == 0))
      {
        Response.getReturnInfos().setMessage("Falls die Value Set-ID 0 ist, müssen Sie einen Namen für das Value Set angeben, damit ein neues angelegt werden kann. Geben Sie also entweder eine Value Set-ID an, damit die Einträge in ein vorhandenes Value Set importiert werden oder geben Sie einen Namen an, damit ein neues erstellt wird.");
        erfolg = false;
      }
      
      if(vs.getValueSetVersions() == null || vs.getValueSetVersions().size() == 0)
      {
        Response.getReturnInfos().setMessage("ValueSetVersion darf nicht NULL sein!");
        erfolg = false;
      }
      else
      {
        ValueSetVersion vsv = vs.getValueSetVersions().iterator().next();
        if(vsv.getVersionId() == null && vsv.getName() == null)
        {
          Response.getReturnInfos().setMessage("Sie müssen entweder eine ValueSet-VersionID angeben oder einen ValueSet-Namen, damit eine neue Version angelegt wird!");
          erfolg = false;
        }
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
