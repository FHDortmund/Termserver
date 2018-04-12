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
package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import org.hibernate.Query;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class UpdateConceptAssociationStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateConceptAssociationStatusResponseType UpdateConceptAssociationStatus(UpdateConceptAssociationStatusRequestType parameter, String ipAddress)
  {
    return UpdateConceptAssociationStatus(parameter, null, ipAddress);
  }

  public UpdateConceptAssociationStatusResponseType UpdateConceptAssociationStatus(UpdateConceptAssociationStatusRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== UpdateConceptAssociationStatus gestartet ======");

    boolean createHibernateSession = (session == null);
    logger.debug("createHibernateSession: " + createHibernateSession);

    // Return-Informationen anlegen
    UpdateConceptAssociationStatusResponseType response = new UpdateConceptAssociationStatusResponseType();
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

    // TODO Lizenzen prüfen (?)

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return response;
    }

    try
    {
      long associationId = 0;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = null;
      org.hibernate.Transaction tx = null;

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        tx = hb_session.beginTransaction();
      }
      else
      {
        hb_session = session;
        //hb_session.getTransaction().begin();
      }


      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        CodeSystemEntityVersionAssociation association_param = parameter.getCodeSystemEntityVersionAssociation();
        //long id = association_param.getId();
        
        logger.debug("change status for codeSystemEntityVersionAssociation id " + association_param.getId());

        CodeSystemEntityVersionAssociation association_db = (CodeSystemEntityVersionAssociation) hb_session.get(CodeSystemEntityVersionAssociation.class, association_param.getId());
        association_db.setStatus(parameter.getCodeSystemEntityVersionAssociation().getStatus());
        association_db.setStatusDate(new Date());
        
        logger.debug("ID: " + association_db.getId() +  ", left-id: " + association_db.getLeftId());

        hb_session.merge(association_db);
        
        if(tx != null)
          tx.commit();
        
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setCount(1);
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Relation status changed successfully.");
      }
      catch (Exception e)
      {
        if(tx != null)
          tx.rollback();
        
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at 'UpdateConceptAssociationStatus', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'UpdateConceptAssociationStatus', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        if (createHibernateSession)
          hb_session.close();
      }

      

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Error at 'UpdateConceptAssociationStatus': " + e.getLocalizedMessage());

      logger.error("Error at 'UpdateConceptAssociationStatus': " + e.getLocalizedMessage());
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
  private boolean validateParameter(UpdateConceptAssociationStatusRequestType Request,
                                    UpdateConceptAssociationStatusResponseType Response)
  {
    boolean erfolg = true;

    CodeSystemEntityVersionAssociation association = Request.getCodeSystemEntityVersionAssociation();

    if (association == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      if(Definitions.STATUS_CODES.isStatusCodeValid(association.getStatus()) == false)
      {
        Response.getReturnInfos().setMessage(
          "Der Status-Code ist kein gültiger Code. Folgende Werte sid zulässig: " + Definitions.STATUS_CODES.readStatusCodes());
        erfolg = false;
      }
      
      if (association.getId() == null
        || association.getId() == 0)
      {
        Response.getReturnInfos().setMessage(
          "Es muss eine ID der Beziehung angegeben sein!");
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
