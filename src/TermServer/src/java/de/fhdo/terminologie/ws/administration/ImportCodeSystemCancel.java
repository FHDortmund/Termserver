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
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemCancelRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemCancelResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCodeSystemCancel
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ImportCodeSystemCancelResponseType ImportCodeSystemCancel(ImportCodeSystemCancelRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCodeSystem ImportCodeSystemCancel ======");

    // Return-Informationen anlegen
    ImportCodeSystemCancelResponseType response = new ImportCodeSystemCancelResponseType();
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

    if (loggedIn)
    {
      StaticStatus.cancel = true;

      // Status wiedergeben
      response.getReturnInfos().setCount(StaticStatus.importCount);
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Import abgebrochen!");
    }
    else
    {
      response.getReturnInfos().setCount(StaticStatus.importCount);
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Import abbrechen fehlgeschlagen!");
    }
    //response.setIsRunning(StaticStatus.exportRunning);
    //response.setCurrentIndex(StaticStatus.importCount);
    //response.setTotalCount(StaticStatus.importTotal);

    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ImportCodeSystemCancelRequestType Request,
          ImportCodeSystemCancelResponseType Response)
  {
    boolean erfolg = true;

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
