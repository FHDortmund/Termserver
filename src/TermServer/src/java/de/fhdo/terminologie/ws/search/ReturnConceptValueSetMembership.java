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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Philipp Urbauer
 */
public class ReturnConceptValueSetMembership
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ReturnConceptValueSetMembershipResponseType ReturnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnConceptValueSetMembership gestartet ======");

    // Return-Informationen anlegen
    ReturnConceptValueSetMembershipResponseType response = new ReturnConceptValueSetMembershipResponseType();
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

    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      ConceptValueSetMembership cvsm_db = null;

      try
      {
        ConceptValueSetMembershipId cvsm_id
                = new ConceptValueSetMembershipId(parameter.getCodeSystemEntityVersion().getVersionId(),
                        parameter.getValueSetVersion().getVersionId());
        cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsm_id);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptValueSetMembership', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnConceptValueSetMembership', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

      if (cvsm_db != null)
      {
        cvsm_db.setCodeSystemEntityVersion(null);
        cvsm_db.setValueSetVersion(null);

        // Liste der Response beifügen
        response.setConceptValueSetMembership(cvsm_db);

        response.getReturnInfos().setCount(1);
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("ConceptValueSetMembership erfolgreich gelesen");
      }
      else
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Keine Verbindung zu den gegebenen IDs gefunden");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptValueSetMembership': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnConceptValueSetMembership': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnConceptValueSetMembershipRequestType Request, ReturnConceptValueSetMembershipResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getCodeSystemEntityVersion() == null || Request.getValueSetVersion() == null)
    {

      Response.getReturnInfos().setMessage("CodeSystemEntityVersion und ValueSetVersion darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {

      if (Request.getCodeSystemEntityVersion().getVersionId() == null || Request.getCodeSystemEntityVersion().getVersionId() == 0)
      {

        Response.getReturnInfos().setMessage("CodeSystemEntityVersion.versionId muss korrekt angegeben werden!");
        erfolg = false;
      }

      if (Request.getValueSetVersion().getVersionId() == null || Request.getValueSetVersion().getVersionId() == 0)
      {

        Response.getReturnInfos().setMessage("ValueSetVersion.versionId muss korrekt angegeben werden!");
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
