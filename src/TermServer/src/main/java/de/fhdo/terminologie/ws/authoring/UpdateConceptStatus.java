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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Mathias Aschhoff 2014-07-23: updated by Robert Mützner
 * <robert.muetzner@fh-dortmund.de>
 */
public class UpdateConceptStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateConceptStatusResponseType UpdateConceptStatus(UpdateConceptStatusRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== UpdateConceptStatus gestartet ======");

    UpdateConceptStatusResponseType response = new UpdateConceptStatusResponseType();
    response.setReturnInfos(new ReturnType());

    //Parameter prüfen
    if (validateParameter(parameter, response) == false)
      return response; // Fehler bei den Parametern

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

    logger.debug("loggedIn: " + loggedIn);

    try
    {
      CodeSystemEntity cse = parameter.getCodeSystemEntity();
      //Long csvId = parameter.getCodeSystemVersionId();
      CodeSystemEntityVersion csev = (CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0];

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try
      {
        CodeSystemEntityVersion csev_db = (CodeSystemEntityVersion) hb_session.get(CodeSystemEntityVersion.class, csev.getVersionId());
        logger.debug("csev gelesen mit ID: " + csev_db.getVersionId());

        if (csev_db.getStatusDeactivatedDate() == null)
          csev_db.setStatusDeactivatedDate(new Date());
        if (csev_db.getStatusWorkflowDate() == null)
          csev_db.setStatusWorkflowDate(new Date());
        
        if (csev.getStatusVisibility() != null && csev.getStatusVisibility().intValue() != csev_db.getStatusVisibility().intValue())
        {
          csev_db.setStatusVisibility(csev.getStatusVisibility());
          csev_db.setStatusVisibilityDate(new Date());
        }
        
        if (csev.getStatusDeactivated()!= null && csev.getStatusDeactivated().intValue() != csev_db.getStatusDeactivated().intValue())
        {
          csev_db.setStatusDeactivated(csev.getStatusDeactivated());
          csev_db.setStatusDeactivatedDate(new Date());
        }
        
        if (csev.getStatusWorkflow()!= null && csev.getStatusWorkflow().intValue() != csev_db.getStatusWorkflow().intValue())
        {
          csev_db.setStatusWorkflow(csev.getStatusWorkflow());
          csev_db.setStatusWorkflowDate(new Date());
        }
        

        hb_session.update(csev_db);

        long csvId = 0;
        for (CodeSystemVersionEntityMembership csvem : csev_db.getCodeSystemEntity().getCodeSystemVersionEntityMemberships())
        {
          csvId = csvem.getCodeSystemVersion().getVersionId();
          logger.debug("Version-ID: " + csvId);
        }

        if (csvId > 0)
          LastChangeHelper.updateLastChangeDate(true, csvId, hb_session);

        tx.commit();
        response.getReturnInfos().setCount(1);
      }
      catch (Exception e)
      {
        logger.error("Fehler bei 'UpdateConceptStatus'-Hibernate: " + e.getLocalizedMessage());
        LoggingOutput.outputException(e, this);

        tx.rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptStatus': " + e.getLocalizedMessage());

        e.printStackTrace();
      }
      finally
      {
        hb_session.close();
      }
      if (true)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Status erfolgreich geändert.");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateConceptStatus': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'UpdateConceptStatus': " + e.getLocalizedMessage());
      LoggingOutput.outputException(e, this);

      e.printStackTrace();
    }
    return response;
  }

  private boolean validateParameter(UpdateConceptStatusRequestType Request, UpdateConceptStatusResponseType Response)
  {
    boolean erfolg = true;

    CodeSystemEntity cse = Request.getCodeSystemEntity();
    if (cse == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntity darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      Set<CodeSystemEntityVersion> csevSet = cse.getCodeSystemEntityVersions();
      if (csevSet != null)
      {
        if (csevSet.size() > 1)
        {
          Response.getReturnInfos().setMessage("Die CodeSystemEntity-Version-Liste darf maximal einen Eintrag haben!");
          erfolg = false;
        }
        else if (csevSet.size() == 1)
        {
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csevSet.toArray()[0];

          if (csev.getVersionId() == null || csev.getVersionId() == 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss eine ID für die CodeSystemEntity-Version angegeben werden!");
            erfolg = false;
          }
        }

      }
      else
      {
        Response.getReturnInfos().setMessage("CodeSystemEntity-Version darf nicht NULL sein!");
        erfolg = false;
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
