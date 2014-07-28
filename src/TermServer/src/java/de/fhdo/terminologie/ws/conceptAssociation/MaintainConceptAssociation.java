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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import org.hibernate.ObjectNotFoundException;

/**
 *
 * @author Nico Hänsch
 */
public class MaintainConceptAssociation
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Verändert eine Beziehung zwischen 2 Konzepten.
   *
   * @param parameter
   * @return
   */
  public MaintainConceptAssociationResponseType MaintainConceptAssociation(MaintainConceptAssociationRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== MaintainConceptAssociation gestartet ======");

    // Return-Informationen anlegen
    MaintainConceptAssociationResponseType response = new MaintainConceptAssociationResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten
    boolean loggedIn = false;
    AuthenticateInfos loginInfoType = null;
    if (parameter != null && parameter.getLoginToken() != null)
    {
      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType != null;
    }

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
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      // Neue CodeSystemEntityVersionAssociation
      CodeSystemEntityVersionAssociation cseva_New = parameter.getCodeSystemEntityVersionAssociation().get(0);
      CodeSystemEntityVersionAssociation cseva_db = new CodeSystemEntityVersionAssociation();

      try
      { // 2. Try-Catch-Block zum Abfangen von Hibernate-Fehlern 

        //Origianl CSEVA aus DB laden
        //TODO Prüfen, ob Id vorhanden ist (Catch funktioniert nicht richtig, Fehler wird zu spät gefangen)
        try
        {
          cseva_db = (CodeSystemEntityVersionAssociation) hb_session.load(CodeSystemEntityVersionAssociation.class, cseva_New.getId());
        }
        catch (ObjectNotFoundException e)
        {
          // Fehlermeldung an den Aufrufer weiterleiten
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Keine CodeSystemEntityVersionAssociation mit der angegeben ID vorhanden.");
          logger.error(response.getReturnInfos().getMessage());
        }

        // Beziehungen ändern                
        // codeSystemEntityVersionByCodeSystemEntityVersionIdX neu erstellen
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null); -> nicht mehr möglich!
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null); -> nicht mehr möglich!
        // codeSystemEntityVersionByCodeSystemEntityVersionIdX zuweisen -> nicht mehr möglich!
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        //cseva_db.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(cseva_New.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId());
        //cseva_db.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        //cseva_db.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(cseva_New.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId());
        // Attribute ändern
        // cseva_db.setLeftId(cseva_New.getLeftId()); -> nicht mehr möglich!
        //cseva_db.setAssociationKind(cseva_New.getAssociationKind()); -> nicht mehr möglich!
        // AssociationType ändern, falls angegeben
        if (cseva_New.getAssociationType() != null)
        {
          //cseva_db.getAssociationType().setCodeSystemEntityVersionId(cseva_New.getAssociationType().getCodeSystemEntityVersionId());
          cseva_db.setAssociationType(new AssociationType());
          cseva_db.getAssociationType().setCodeSystemEntityVersionId(cseva_New.getAssociationType().getCodeSystemEntityVersionId());
        }
        
        if(cseva_New.getLeftId() != null && cseva_New.getLeftId() > 0)
        {
          if(cseva_New.getLeftId().longValue() == cseva_db.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId().longValue() ||
             cseva_New.getLeftId().longValue() == cseva_db.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId().longValue())
          {
            cseva_db.setLeftId(cseva_New.getLeftId());
          }
        }
        
        if(cseva_New.getAssociationKind() != null && cseva_New.getAssociationKind() > 0)
        {
          cseva_db.setAssociationKind(cseva_New.getAssociationKind());
        }
        
        cseva_db.setStatusDate(new Date());

        // prüfen, ob AssociationTypeId auch eine Association ist
        if (hb_session.get(AssociationType.class, cseva_db.getAssociationType().getCodeSystemEntityVersionId()) == null)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Sie müssen eine gültige ID für ein AssociationType angeben. Das Konzept mit der ID '" + cseva_db.getAssociationType().getCodeSystemEntityVersionId() + "' ist kein AssociationType!");

          logger.info("ungültige ID für AssociationType");
        }
        else
        {
          // Beziehung abspeichern
          hb_session.merge(cseva_db);
          associationId = cseva_db.getId();
          
          response.getReturnInfos().setCount(1);
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociation', Hibernate: " + e.getLocalizedMessage());
        logger.error(response.getReturnInfos().getMessage());
        LoggingOutput.outputException(e, this);
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (associationId > 0)
        {
          hb_session.getTransaction().commit();
        }
        else
        {
          // Änderungen nicht erfolgreich
          logger.warn("[MaintainConceptAssociation.java] Änderungen nicht erfolgreich");
          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }
      if (associationId > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation erfolgreich geändert. ");
        logger.info(response.getReturnInfos().getMessage());
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociation': " + e.getLocalizedMessage());
      logger.error(response.getReturnInfos().getMessage());
    }
    return response;
  }

  private boolean validateParameter(MaintainConceptAssociationRequestType Request, MaintainConceptAssociationResponseType Response)
  {
    boolean parameterValidiert = true;

    //Prüfen ob Login übergeben wurde (KANN)
    if (Request.getLoginToken() == null || Request.getLoginToken().length() == 0)
    {
      Response.getReturnInfos().setMessage(
              "Das Login-Token darf nicht leer sein!");
      parameterValidiert = false;
    }

    if (Request.getCodeSystemEntityVersionAssociation() == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation darf nicht NULL sein.");
      parameterValidiert = false;
    }
    else
    {
      if (Request.getCodeSystemEntityVersionAssociation().size() > 1)
      {
        Response.getReturnInfos().setMessage("Es muss genau eine CodeSystemEntityVersionAssociation angegeben sein.");
        parameterValidiert = false;
      }
      else
      {
        CodeSystemEntityVersionAssociation csev = Request.getCodeSystemEntityVersionAssociation().get(0);
        if (csev.getId() == null || csev.getId() == 0)
        {
          Response.getReturnInfos().setMessage("Es muss eine ID der Beziehung angegeben sein!");
          parameterValidiert = false;
        }
        else if (csev.getAssociationType() != null && csev.getAssociationType().getCodeSystemEntityVersionId() == 0)
        {
          Response.getReturnInfos().setMessage("Wenn ein AssociationType angegeben wird muss auch eine codeSystemEntityVersionId angegeben werden.");
          parameterValidiert = false;
        }
      }
    }

    if (parameterValidiert == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return parameterValidiert;
  }
}
