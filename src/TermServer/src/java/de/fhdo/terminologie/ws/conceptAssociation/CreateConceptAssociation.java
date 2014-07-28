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
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateConceptAssociation
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateConceptAssociationResponseType CreateConceptAssociation(CreateConceptAssociationRequestType parameter, String ipAddress)
  {
    return CreateConceptAssociation(parameter, null, ipAddress);
  }

  /**
   * Stellt eine Beziehung zwischen 2 Konzepten her. Die erstegenannte
   * Verbindung ist hier immer die "Left-ID".
   *
   * @param parameter
   * @return
   */
  public CreateConceptAssociationResponseType CreateConceptAssociation(CreateConceptAssociationRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== CreateConceptAssociation gestartet ======");
    }
    // Return-Informationen anlegen
    CreateConceptAssociationResponseType response = new CreateConceptAssociationResponseType();
    response.setReturnInfos(new ReturnType());

    boolean createHibernateSession = (session == null);

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

    if (logger.isDebugEnabled())
    {
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    }

        // TODO Lizenzen prüfen (?)
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

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
      }
      else
      {
        hb_session = session;
        //hb_session.getTransaction().begin();
      }

      CodeSystemEntityVersionAssociation association = parameter.getCodeSystemEntityVersionAssociation();
      long entityVersionId1 = association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId();
      long entityVersionId2 = association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
      long associationTypeId = association.getAssociationType().getCodeSystemEntityVersionId();

      // Beziehungen neu erstellen, damit NUR IDs drin stehen
      association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
      association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);

      association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
      association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(entityVersionId1);

      association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
      association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(entityVersionId2);

      association.setAssociationType(null);
      association.setAssociationType(new AssociationType());
      association.getAssociationType().setCodeSystemEntityVersionId(associationTypeId);

      // Weitere Attribute setzen
      association.setLeftId(entityVersionId1);  // TODO ist die 1. für uns immer die "links"?
      association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
      association.setStatusDate(new Date());
      association.setInsertTimestamp(new Date());

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // prüfen, ob AssociationTypeId auch eine Association ist
        if (hb_session.get(AssociationType.class, associationTypeId) == null)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Sie müssen eine gültige ID für ein AssociationType angeben. Das Konzept mit der ID '" + associationTypeId + "' ist kein AssociationType!");

          logger.info("ungültige ID für AssociationType");
        }
        else
        {
          // Beziehung abspeichern
          hb_session.save(association);
          associationId = association.getId();
        }

        // Transaktion abschließen
        if (createHibernateSession)
        {
          if (associationId > 0)
          {

            if (association.getAssociationKind() == 2 && association.getAssociationType().getCodeSystemEntityVersionId().equals(4L))
            {

              //Check parentCSEV isLeaf or childs are not shown!
              CodeSystemEntityVersion csev_Parent = (CodeSystemEntityVersion) hb_session.get(CodeSystemEntityVersion.class, entityVersionId1);
              if (csev_Parent.getIsLeaf().booleanValue())
              {

                csev_Parent.setIsLeaf(false);
                hb_session.update(csev_Parent);
              }
            }

            hb_session.getTransaction().commit();
          }
          else
          {
            // Änderungen nicht erfolgreich
            logger.warn("[CreateConceptAssociation.java] Änderungen nicht erfolgreich");

            hb_session.getTransaction().rollback();
          }
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'CreateConceptAssociation', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'CreateConceptAssociation', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        if (createHibernateSession)
        {
          hb_session.close();
        }
      }

      // Antwort zusammenbauen
      if (associationId > 0)
      {
        response.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());
        response.getCodeSystemEntityVersionAssociation().setId(association.getId());

        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Beziehung zwischen 2 Konzepten erfolgreich erstellt");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateConceptAssociation': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'CreateConceptAssociation': " + e.getLocalizedMessage());
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
  private boolean validateParameter(CreateConceptAssociationRequestType Request,
          CreateConceptAssociationResponseType Response)
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
      if (association.getAssociationType() == null
              || association.getAssociationType().getCodeSystemEntityVersionId() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss ein AssociationType mit einer ID angegeben sein!");
        erfolg = false;
      }
      else if (association.getAssociationKind() == null || Definitions.isAssociationKindValid(association.getAssociationKind()) == false)
      {
        Response.getReturnInfos().setMessage(
                "Es muss ein Association-Kind angegeben sein, mögliche Werte: " + Definitions.readAssociationKinds());
        erfolg = false;
      }
      else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() == null
              || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() == null
              || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss eine ID für die 1. CodeSystemEntityVersion angegeben sein!");
        erfolg = false;
      }
      else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() == null
              || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId() == null
              || association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss eine ID für die 2. CodeSystemEntityVersion angegeben sein!");
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
