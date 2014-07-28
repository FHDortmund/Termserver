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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.helper.CodeSystemHelper;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListConceptAssociationTypes
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListConceptAssociationTypesResponseType ListConceptAssociationTypes(ListConceptAssociationTypesRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListConceptAssociationTypes gestartet ======");

    // Return-Informationen anlegen
    ListConceptAssociationTypesResponseType response = new ListConceptAssociationTypesResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false || parameter == null)
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
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    // TODO Lizenzen prüfen
    // TODO Status von Vok-Version prüfen (wenn nicht eingeloggt)

    try
    {
      List<AssociationType> conceptList = null;

      //long codeSystemId = 0;
      long codeSystemVersionId = 0;

      if (parameter.getCodeSystem() != null)
      {
        //codeSystemId = parameter.getCodeSystem().getId();

        if (parameter.getCodeSystem().getCodeSystemVersions() != null && parameter.getCodeSystem().getCodeSystemVersions().size() > 0)
        {
          CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];
          codeSystemVersionId = csv.getVersionId();
        }
      }


      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        /*if (codeSystemVersionId == 0 && codeSystemId > 0)
        {
          // Aktuelle Version des Vokabulars ermitteln
          CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, codeSystemId);
          codeSystemVersionId = CodeSystemHelper.getCurrentVersionId(cs);
        }*/

        // HQL erstellen
        // Besonderheit hier: es dürfen keine Werte nachgeladen werden
        // Beim Abruf eines ICD wäre dieses sehr inperformant, da er für
        // jeden Eintrag sonst nachladen würde
        String hql = "select distinct at from AssociationType at";
        hql += " join fetch at.codeSystemEntityVersion csev";
        hql += " join fetch csev.codeSystemEntity cse";
        hql += " left outer join fetch cse.codeSystemVersionEntityMemberships csvem";

        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        if (codeSystemVersionId > 0)
          parameterHelper.addParameter("", "codeSystemVersionId", codeSystemVersionId);

        if (loggedIn == false)
        {
          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");
        hql += where;

        // immer neueste Version lesen
        if (where.length() > 0)
          hql += " AND ";
        else
          hql += " WHERE ";

        hql += " csev.versionId=cse.currentVersionId";
        
        if(logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        conceptList = (java.util.List<AssociationType>) q.list();

        //tx.commit();

        if(logger.isDebugEnabled())
          logger.debug("=== HIERNACH KEINE ABFRAGEN MEHR!===");

        if (conceptList != null)
        {
          if(logger.isDebugEnabled())
            logger.debug("Anzahl: " + conceptList.size());
          
          java.util.List<CodeSystemEntity> entityList = new LinkedList<CodeSystemEntity>();

          Iterator<AssociationType> iterator = conceptList.iterator();

          while (iterator.hasNext())
          {
            AssociationType at = iterator.next();

            // neues Entity generieren, damit nicht nachgeladen werden muss
            CodeSystemEntity entity = at.getCodeSystemEntityVersion().getCodeSystemEntity();

            CodeSystemEntityVersion csev = at.getCodeSystemEntityVersion();

            csev.setCodeSystemEntity(null);

            csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
            csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
            csev.setCodeSystemMetadataValues(null);
            csev.setValueSetMetadataValues(null);
            csev.setConceptValueSetMemberships(null);
            csev.setCodeSystemConcepts(null);

            at.setCodeSystemEntityVersion(null);
            at.setCodeSystemEntityVersionAssociations(null);

            csev.setAssociationTypes(new HashSet<AssociationType>());
            csev.getAssociationTypes().add(at);

            entity.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
            entity.getCodeSystemEntityVersions().add(csev);

            // M:N Verbindung zur Vokabular-Version (ohne nachladen)
            if (entity.getCodeSystemVersionEntityMemberships() != null && entity.getCodeSystemVersionEntityMemberships().size() > 0)
            {
              CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) entity.getCodeSystemVersionEntityMemberships().toArray()[0];
              ms.setCodeSystemVersion(null);
              ms.setCodeSystemEntity(null);
              ms.setId(null);
              
              entity.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
              entity.getCodeSystemVersionEntityMemberships().add(ms);
            }
            else entity.setCodeSystemVersionEntityMemberships(null);

            

            entityList.add(entity);
          }

          int anzahl = 0;
          if (entityList != null)
            anzahl = entityList.size();
          response.setCodeSystemEntity(entityList);

          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("AssociationTypes erfolgreich gelesen, Anzahl: " + anzahl);
          response.getReturnInfos().setCount(anzahl);
        }
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListConceptAssociationTypes', Hibernate: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
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
      response.getReturnInfos().setMessage("Fehler bei 'ListConceptAssociationTypes': " + e.getLocalizedMessage());

      LoggingOutput.outputException(e, this);
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
  private boolean validateParameter(ListConceptAssociationTypesRequestType Request,
                                    ListConceptAssociationTypesResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem != null)
    {
      /*if (codeSystem.getId() == null || codeSystem.getId() == 0)
      {
        Response.getReturnInfos().setMessage(
          "Es muss eine ID für das CodeSystem angegeben sein, wenn ein CodeSystem gegeben ist. Wenn Sie alle Assoziationen auflisten möchten, geben Sie kein CodeSystemType mit.");
        erfolg = false;
      }*/

      if (codeSystem.getCodeSystemVersions() != null)
      {
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
                "Es muss eine ID für die CodeSystem-Version angegeben sein, wenn Sie ein Typ CodeSystemVersion mitgeben! Ansonsten setzen Sie die CodeSystemVersion auf NULL und es wird die aktuellste Version abgerufen.");
              erfolg = false;
            }
          }
        }
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
