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

import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCodeSystemStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ImportCodeSystemStatusResponseType ImportCodeSystemStatus(ImportCodeSystemStatusRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCodeSystem ImportCodeSystemStatus ======");

    // Return-Informationen anlegen
    ImportCodeSystemStatusResponseType response = new ImportCodeSystemStatusResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Status wiedergeben
    response.setIsRunning(StaticStatus.exportRunning);
    response.setCurrentIndex(StaticStatus.importCount);
    response.setTotalCount(StaticStatus.importTotal);

    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ImportCodeSystemStatusRequestType Request,
          ImportCodeSystemStatusResponseType Response)
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
