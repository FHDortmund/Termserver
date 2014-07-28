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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.types.ReturnConceptAssociationDetailsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ReturnConceptAssociationDetailsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Nico Hänsch
 */
public class ReturnConceptAssociationDetails
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Metadaten zu einer Begriffsbeziehung erhalten
   *
   * @param parameter
   * @return Metadaten der Assoziation
   */
  public ReturnConceptAssociationDetailsResponseType ReturnConceptAssociationDetails(ReturnConceptAssociationDetailsRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnConceptAssociationDetails gestartet ======");

    // Return-Informationen anlegen
    ReturnConceptAssociationDetailsResponseType response = new ReturnConceptAssociationDetailsResponseType();
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

    if (logger.isDebugEnabled())
    {
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    }

    try
    {

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try //Try-Catch-Block um Hibernate-Fehler abzufangen
      {
        String hql = "select distinct cseva from CodeSystemEntityVersionAssociation cseva";
        hql += " join fetch cseva.associationType at";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("cseva.", "id", parameter.getCodeSystemEntityVersionAssociation().getId());

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        if (logger.isDebugEnabled())
        {
          logger.debug("HQL: " + hql);
        }

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        List list = (java.util.List<CodeSystemEntityVersionAssociation>) q.list();
                     //hb_session.getTransaction().commit();

        // TODO Lizenzen prüfen (?)
        Iterator<CodeSystemEntityVersionAssociation> it = list.iterator();
        CodeSystemEntityVersionAssociation cseva = null;

        while (it.hasNext())
        {
          cseva = it.next();

          // Metadaten lesen
          CodeSystemEntityVersion csev1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1();
          CodeSystemEntityVersion csev2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();
          CodeSystemEntity cse1 = csev1.getCodeSystemEntity();
          CodeSystemEntity cse2 = csev2.getCodeSystemEntity();
          AssociationType at = cseva.getAssociationType();

          // Verbindungen auf null setzen
          at.setCodeSystemEntityVersion(null);
          at.setCodeSystemEntityVersionAssociations(null);
          cse1.setCodeSystemEntityVersions(null);
          cse1.setCodeSystemVersionEntityMemberships(null);
          cse2.setCodeSystemEntityVersions(null);
          cse2.setCodeSystemVersionEntityMemberships(null);

          // Ergbnis vorbereiten
          csev1.setCodeSystemEntity(cse1);
          csev2.setCodeSystemEntity(cse2);

          Iterator<CodeSystemConcept> cscit1 = csev1.getCodeSystemConcepts().iterator();
          while (cscit1.hasNext())
          {
            CodeSystemConcept csc1 = cscit1.next();
            csev1.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csc1.setCodeSystemEntityVersion(null);
            csc1.setCodeSystemConceptTranslations(null);
            csev1.getCodeSystemConcepts().add(csc1);
          }

          Iterator<CodeSystemConcept> cscit2 = csev2.getCodeSystemConcepts().iterator();
          while (cscit2.hasNext())
          {
            CodeSystemConcept csc2 = cscit2.next();
            csev2.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csc2.setCodeSystemEntityVersion(null);
            csc2.setCodeSystemConceptTranslations(null);
            csev2.getCodeSystemConcepts().add(csc2);
          }

          // Verbindungen löschen und CodeSystemEntityVersions neu setzen
          csev1.setAssociationTypes(null);
          csev1.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
          csev1.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
          csev1.setCodeSystemMetadataValues(null);
          csev1.setValueSetMetadataValues(null);
          csev1.setConceptValueSetMemberships(null);

          csev2.setAssociationTypes(null);
          csev2.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
          csev2.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
          csev2.setCodeSystemMetadataValues(null);
          csev2.setValueSetMetadataValues(null);
          csev2.setConceptValueSetMemberships(null);

          // Ergebnis befüllen
          cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
          cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
          cseva.setAssociationType(at);
        }
        // Falls Assoziation nicht vorhanden
        if (cseva == null)
        {
          response.getReturnInfos().setMessage("Keine Assoziation mit der angegebenen ID vorhanden!");
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        else
        {
          // Assoziation zu response hinzufügen
          response.setCodeSystemEntityVersionAssociation(cseva);
          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Assoziation erfolgreich gelesen!");
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung (Hibernate) an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptAssociationDetails', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnConceptAssociationDetails', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        // Transaktion abschließen
        hb_session.close();
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten            
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptAssociationDetails', Hibernate: " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnConceptAssociationDetails': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnConceptAssociationDetailsRequestType Request, ReturnConceptAssociationDetailsResponseType Response)
  {
    boolean parameterValidiert = true;

    
    // Prüfen ob CodeSystemEntityVersionAssociation übergeben wurde (MUSS)
    if (Request.getCodeSystemEntityVersionAssociation() == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemEntityVersionAssociation darf nicht NULL sein.");
      parameterValidiert = false;
    }
    else
    {
      // Prüfen ob CodeSystemEntityVersionAssociation-ID übergeben wurde (MUSS)
      if (Request.getCodeSystemEntityVersionAssociation().getId() == null || Request.getCodeSystemEntityVersionAssociation().getId() <= 0)
      {
        Response.getReturnInfos().setMessage("Es muss eine gültige ID der Assoziation angegeben sein!");
        parameterValidiert = false;
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
