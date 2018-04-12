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

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCreateCodeSystem
{

  private static Logger logger = Logger4j.getInstance().getLogger();

  public static boolean createCodeSystem(org.hibernate.Session hb_session, ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
  {
    boolean erfolg = true;
    
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("createCodeSystem: " + parameter.getCodeSystem().getName() + ", ID: " + parameter.getCodeSystem().getId());

      // TODO zunächst prüfen, ob CodeSystem bereits existiert
      CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
      request.setCodeSystem(parameter.getCodeSystem());
      request.setLoginToken(parameter.getLoginToken());

      //Code System erstellen
      CreateCodeSystem ccs = new CreateCodeSystem();
      CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");

      if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
      {
        logger.debug("createCodeSystem-Return: " + resp.getReturnInfos().getMessage());
        return false;
      }
      parameter.setCodeSystem(resp.getCodeSystem());
      response.setCodeSystem(resp.getCodeSystem());

      logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    }
    catch (Exception e)
    {
      erfolg = false;
    }
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    return erfolg;
  }
}
