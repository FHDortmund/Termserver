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

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptDetailsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ReturnConceptDetails
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ReturnConceptDetailsResponseType ReturnConceptDetails(ReturnConceptDetailsRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnConceptDetails gestartet ======");

    // Return-Informationen anlegen
    ReturnConceptDetailsResponseType response = new ReturnConceptDetailsResponseType();
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
      logger.debug("Eingelogged: " + loggedIn);

    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      CodeSystemEntity codeSystemEntity = null;

      try
      {
        String hql = "select distinct cse from CodeSystemEntity cse";
        hql += " join fetch cse.codeSystemEntityVersions csev";
        hql += " join fetch csev.codeSystemConcepts term";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getCodeSystemEntity() != null)
        {
          //parameterHelper.addParameter("cse.", "id", parameter.getCodeSystemEntity().getId());

          if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
          {
            CodeSystemEntityVersion vsvFilter = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];

            parameterHelper.addParameter("csev.", "versionId", vsvFilter.getVersionId());
          }
        }

        if (!loggedIn)
        {
          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        List<CodeSystemEntity> liste = q.list();

        if (logger.isDebugEnabled())
          logger.debug("Anzahl Ergebnisse: " + liste.size());

        if (liste != null && liste.size() > 0)
          codeSystemEntity = liste.get(0);

        if (codeSystemEntity != null)
        {
          // M:N zu Vokabular
          if (codeSystemEntity.getCodeSystemVersionEntityMemberships() != null)
          {
            Iterator<CodeSystemVersionEntityMembership> itMember = codeSystemEntity.getCodeSystemVersionEntityMemberships().iterator();

            while (itMember.hasNext())
            {
              CodeSystemVersionEntityMembership member = itMember.next();

              member.setCodeSystemEntity(null);
              member.setCodeSystemVersion(null);
            }
          }

          if (codeSystemEntity.getCodeSystemEntityVersions() != null)
          {
            Iterator<CodeSystemEntityVersion> itVersions = codeSystemEntity.getCodeSystemEntityVersions().iterator();

            while (itVersions.hasNext())
            {
              CodeSystemEntityVersion csev = itVersions.next();

              csev.setCodeSystemEntity(null);
              csev.setConceptValueSetMemberships(null);
              csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
              csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
              csev.setAssociationTypes(null);

              // Konzepte (Terms)
              if (csev.getCodeSystemConcepts() != null)
              {
                Iterator<CodeSystemConcept> itConcepts = csev.getCodeSystemConcepts().iterator();

                while (itConcepts.hasNext())
                {
                  CodeSystemConcept term = itConcepts.next();

                  term.setCodeSystemEntityVersion(null);

                  // Translations
                  Iterator<CodeSystemConceptTranslation> itTranslations = term.getCodeSystemConceptTranslations().iterator();

                  while (itTranslations.hasNext())
                  {
                    CodeSystemConceptTranslation translation = itTranslations.next();

                    translation.setCodeSystemConcept(null);
                  }
                }
              }

              // Metadata-Values auslesen (performanter)
              // ohne Abfrage würde für jede Metadata-Value 1 Abfrage ausgeführt,
              // so wie jetzt wird 1 Abfrage pro Entity-Version ausgeführt
              hql = "select distinct mdv from CodeSystemMetadataValue mdv";
              hql += " join fetch mdv.metadataParameter mp";
              hql += " join mdv.codeSystemEntityVersion csev";

              parameterHelper = new HQLParameterHelper();
              parameterHelper.addParameter("csev.", "versionId", csev.getVersionId());

              // Parameter hinzufügen (immer mit AND verbunden)
              hql += parameterHelper.getWhere("");
              logger.debug("HQL: " + hql);

              // Query erstellen
              q = hb_session.createQuery(hql);

              // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
              parameterHelper.applyParameter(q);

              //List<CodeSystemMetadataValue> metadataList = q.list();
              csev.setCodeSystemMetadataValues(new HashSet(q.list()));

              if (csev.getCodeSystemMetadataValues() != null)
              {
                Iterator<CodeSystemMetadataValue> itMV = csev.getCodeSystemMetadataValues().iterator();

                while (itMV.hasNext())
                {
                  CodeSystemMetadataValue mValue = itMV.next();

                  mValue.setCodeSystemEntityVersion(null);

                  if (mValue.getMetadataParameter() != null)
                  {
                    mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
                    mValue.getMetadataParameter().setCodeSystem(null);
                    mValue.getMetadataParameter().setValueSet(null);
                    mValue.getMetadataParameter().setValueSetMetadataValues(null);
                  }
                }
              }

              // Valueset-Metadata-Values auslesen
              hql = "select distinct mdv from ValueSetMetadataValue mdv";
              hql += " join fetch mdv.metadataParameter mp";
              hql += " join mdv.codeSystemEntityVersion csev";

              parameterHelper = new HQLParameterHelper();
              parameterHelper.addParameter("csev.", "versionId", csev.getVersionId());

              // Parameter hinzufügen (immer mit AND verbunden)
              hql += parameterHelper.getWhere("");
              logger.debug("HQL: " + hql);

              // Query erstellen und Parameter übernehmen
              q = hb_session.createQuery(hql);
              parameterHelper.applyParameter(q);

              csev.setValueSetMetadataValues(new HashSet(q.list()));

              if (csev.getValueSetMetadataValues() != null)
              {
                Iterator<ValueSetMetadataValue> itMV = csev.getValueSetMetadataValues().iterator();

                while (itMV.hasNext())
                {
                  ValueSetMetadataValue mValue = itMV.next();

                  mValue.setCodeSystemEntityVersion(null);

                  if (mValue.getMetadataParameter() != null)
                  {
                    mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
                    mValue.getMetadataParameter().setCodeSystem(null);
                    mValue.getMetadataParameter().setValueSet(null);
                    mValue.getMetadataParameter().setValueSetMetadataValues(null);
                  }
                }
              }

              // Liste der Response beifügen
              response.setCodeSystemEntity(codeSystemEntity);
              //response.setCodeSystemEntity(null);
            }

          }
        }
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnConceptDetails', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {

        hb_session.close();
      }

      if (codeSystemEntity == null)
      {
        response.getReturnInfos().setMessage("Zu den angegebenen IDs wurde kein CodeSystemEntity gefunden!");
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      }
      else
      {
        response.getReturnInfos().setCount(1);
        response.getReturnInfos().setMessage("CodeSystemEntity-Details erfolgreich gelesen");
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
      }
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnConceptDetailsRequestType Request, ReturnConceptDetailsResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getCodeSystemEntity() == null)
    {
      Response.getReturnInfos().setMessage(
              "CodeSystemEntity darf nicht null sein!");
      erfolg = false;
    }
    else
    {
      if (Request.getCodeSystemEntity().getCodeSystemEntityVersions() != null)
      {
        if (Request.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 1)
        {
          Response.getReturnInfos().setMessage(
                  "Es darf maximal eine CodeSystemEntityVersion angegeben sein!");
          erfolg = false;
        }
        else if (Request.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
        {
          CodeSystemEntityVersion vsv = (CodeSystemEntityVersion) Request.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
          if (vsv.getVersionId() == null || vsv.getVersionId() <= 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss eine ID für die CodeSystemEntity-Version angegeben sein!");
            erfolg = false;
          }
        }
      }
      else
      {
        /*if(Request.getCodeSystemEntity().getId() == null || Request.getCodeSystemEntity().getId() == 0)
         {
         Response.getReturnInfos().setMessage(
         "CodeSystemEntityVersion darf nicht null sein, wenn keine Entity-ID angegeben ist!");
         erfolg = false;
         }*/
        Response.getReturnInfos().setMessage(
                "CodeSystemEntityVersion darf nicht null sein!");
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
