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
package de.fhdo.collaboration.db;

import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner
 */
public class CollaborationSession
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static CollaborationSession instance;

  public static CollaborationSession getInstance()
  {
    if (instance == null)
      instance = new CollaborationSession();

    return instance;
  }

  String sessionID;

  public CollaborationSession()
  {
    sessionID = "";
  }

  public void setSessionID(String sessionID)
  {
    this.sessionID = sessionID;
  }

  /*public String getSessionID()
  {

    return getSessionID(SessionHelper.getCollaborationUserName());
  }*/

  public String getSessionID()
  {
    if (sessionID == null || sessionID.length() == 0)
    {
      logger.debug("Erhalte neue Session-ID");

      List<String> parameter = new LinkedList<String>();
      parameter.add("admin");  // TODO in Konfigdatei auslagern
      parameter.add("e358efa489f58062f10dd7316b65649e");  // TODO in Konfigdatei auslagern
      
//      parameter.setLogin(new LoginType());
//      parameter.getLogin().setUsername("collaboration_software:" + username); //Needed for cleaner Session Management
//      //parameter.getLogin().setPassword("collaboration33_let_me_in");  // TODO in Konfigdatei auslagern
//      parameter.getLogin().setPassword("760e065ea510f539b7bf0c6af1478add");  // TODO in Konfigdatei auslagern

      LoginResponse.Return ret = WebServiceHelper.login(parameter);
      if (ret != null && ret.getReturnInfos().getStatus() == Status.OK)
      {
        sessionID = ret.getParameterList().get(0);
        logger.debug("Session-ID: " + sessionID);
      }
      else
      {
        // TODO Fehlermeldung
        logger.warn("Fehler beim Lesen der Session-ID: ");
        if (ret != null)
          logger.warn("" + ret.getReturnInfos().getMessage());
      }
    }

    logger.debug("Gebe Session-ID collab zurück: " + sessionID);

    return sessionID;
  }
}
