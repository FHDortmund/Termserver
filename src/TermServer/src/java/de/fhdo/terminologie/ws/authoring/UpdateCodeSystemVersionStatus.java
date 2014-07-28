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
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Set;

/**
 *
 * @author Mathias Aschhoff
 */
public class UpdateCodeSystemVersionStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateCodeSystemVersionStatusResponseType UpdateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== UpdateCodeSystemVersionStatus gestartet ======");
    }

    UpdateCodeSystemVersionStatusResponseType response = new UpdateCodeSystemVersionStatusResponseType();
    response.setReturnInfos(new ReturnType());

    //Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)    
    boolean loggedIn = false;
    if (parameter != null && parameter.getLoginToken() != null)
    {
      AuthenticateInfos loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType != null;
    }

    if (loggedIn == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("You have to be logged in to use this service.");
      return response;
    }

    try
    {
      // CodeSystem-Version aus Parameter auslesen     
      CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        // Status ändern und in DB speichern
        CodeSystemVersion csv_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, csv.getVersionId());
        csv_db.setStatus(csv.getStatus());
        hb_session.update(csv_db);
        
        
        LastChangeHelper.updateLastChangeDate(true, csv.getVersionId(),hb_session);
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateCodeSystemVersionStatus', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'UpdateCodeSystemVersionStatus', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        hb_session.close();
      }
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("UpdateCodeSystemVersionStatus erfolgreich");
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateCodeSystemVersionStatus': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'UpdateCodeSystemVersionStatus': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private boolean validateParameter(UpdateCodeSystemVersionStatusRequestType Request, UpdateCodeSystemVersionStatusResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem == null)
    {
      Response.getReturnInfos().setMessage("CodeSystem darf nicht NULL sein!");
      erfolg = false;
    }
//    else if (codeSystem.getId() == null || codeSystem.getId() == 0)
//    {
//      Response.getReturnInfos().setMessage(
//              "Es muss eine ID für das CodeSystem angegeben werden!");
//      erfolg = false;
//    }

    Set<CodeSystemVersion> csvSet = codeSystem.getCodeSystemVersions();
    if (csvSet != null)
    {
      if (csvSet.size() > 1)
      {
        Response.getReturnInfos().setMessage(
                "Die CodeSystem-Version-Liste darf maximal einen Eintrag haben!");
        erfolg = false;
      }
      else if (csvSet.size() == 1)
      {
        CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];

        if (csv.getVersionId() == null || csv.getVersionId() == 0)
        {
          Response.getReturnInfos().setMessage(
                  "Es muss eine ID (>0) für die CodeSystem-Version angegeben werden!");
          erfolg = false;
        }
        if (csv.getStatus() == null)
        {
          Response.getReturnInfos().setMessage(
                  "Es muss ein Status für die CodeSystem-Version angegeben werden!");
          erfolg = false;
        }
      }
    }
    else
    {
      Response.getReturnInfos().setMessage(
              "Die CodeSystem-Version-Liste muss mindestens einen Eintrag haben!");
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
