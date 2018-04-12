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
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListValueSetContentsByTermOrCode
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListValueSetContentsByTermOrCodeResponseType ListValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListValueSetContentsByTermOrCode gestartet ======");

    // Return-Informationen anlegen
    ListValueSetContentsByTermOrCodeResponseType response = new ListValueSetContentsByTermOrCodeResponseType();
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

      List<CodeSystemEntity> entityList = null;//new LinkedList<CodeSystemEntity>();

      try
      {
        String hql = "select distinct cse from CodeSystemEntity cse";
        hql += " join fetch cse.codeSystemEntityVersions csev";
        hql += " join fetch csev.codeSystemConcepts term";
        hql += " join fetch csev.conceptValueSetMemberships ms";
        //hql += " join fetch ms.valueSetVersion vsv";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter.getValueSet().getValueSetVersions() != null)
        {
          ValueSetVersion vsv = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];
          //parameterHelper.addParameter("vsv.", "versionId", vsv.getVersionId());
          parameterHelper.addParameter("ms.id.", "valuesetVersionId", vsv.getVersionId());

          if (parameter.getSearchTerm() != null && parameter.getSearchTerm().length() > 0)
          {
            parameterHelper.addParameter("term.", "term", parameter.getSearchTerm());
          }

          if (parameter.getSearchCode() != null && parameter.getSearchCode().length() > 0)
          {
            parameterHelper.addParameter("term.", "code", parameter.getSearchCode());
          }

          if (vsv.getConceptValueSetMemberships() != null && vsv.getConceptValueSetMemberships().size() > 0)
          {
            ConceptValueSetMembership cvsm = vsv.getConceptValueSetMemberships().iterator().next();
            if (cvsm.getStatusDate() != null)
              parameterHelper.addParameter("cvsm.", "statusDate", cvsm.getStatusDate());
          }
        }
        else
        {
          // TODO ValueSet und currentVersion lesen, dann als Parameter hinzufügen
        }

        if (!loggedIn)
        {
          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        entityList = q.list();

        if (entityList != null)
        {
          Iterator<CodeSystemEntity> itEntities = entityList.iterator();

          while (itEntities.hasNext())
          {
            CodeSystemEntity codeSystemEntity = itEntities.next();

            // Zugehörigkeit zu einer CSV
//            codeSystemEntity.setCodeSystemVersionEntityMemberships(null);
            CodeSystemVersionEntityMembership csevm = (CodeSystemVersionEntityMembership) codeSystemEntity.getCodeSystemVersionEntityMemberships().toArray()[0];
            CodeSystemVersion csv = new CodeSystemVersion();
            csv.setVersionId(csevm.getCodeSystemVersion().getVersionId());
            csevm.setCodeSystemEntity(null);
            csevm.setCodeSystemVersion(csv);

            if (codeSystemEntity.getCodeSystemEntityVersions() != null)
            {
              Iterator<CodeSystemEntityVersion> itVersions = codeSystemEntity.getCodeSystemEntityVersions().iterator();

              while (itVersions.hasNext())
              {
                CodeSystemEntityVersion csev = itVersions.next();

                csev.setCodeSystemEntity(null);
                //csev.setConceptValueSetMemberships(null);
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
                csev.setCodeSystemMetadataValues(null); // TODO
                csev.setValueSetMetadataValues(null);
                csev.setAssociationTypes(null);

                // Verbindung (evtl. überschriebenen Code anzeigen)
                if (csev.getConceptValueSetMemberships() != null)
                {
                  Iterator<ConceptValueSetMembership> itMs = csev.getConceptValueSetMemberships().iterator();

                  while (itMs.hasNext())
                  {
                    ConceptValueSetMembership membership = itMs.next();
                    membership.setCodeSystemEntityVersion(null);
                    membership.setValueSetVersion(null);
                  }
                }

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

              }
            }
          }

          response.setCodeSystemEntity(entityList);
          response.getReturnInfos().setCount(entityList.size());
        }

        if (entityList == null)
        {
          response.getReturnInfos().setMessage("Zu dem angegebenen ValueSet wurden keine Konzepte gefunden!");
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        else
        {
          response.getReturnInfos().setMessage("Konzepte zu einem ValueSet erfolgreich gelesen");
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
        }
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());

        //logger.error("Fehler bei 'ListValueSetContents', Hibernate: " + e.getLocalizedMessage());
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());

      //logger.error("Fehler bei 'ListValueSetContents': " + e.getLocalizedMessage());
      LoggingOutput.outputException(e, this);
    }

    return response;
  }

  private boolean validateParameter(ListValueSetContentsByTermOrCodeRequestType Request, ListValueSetContentsByTermOrCodeResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getValueSet() == null)
    {
      Response.getReturnInfos().setMessage(
              "ValueSet darf nicht NULL sein!");
      erfolg = false;
    }
    else if (Request.getValueSet().getId() == null || Request.getValueSet().getId() == 0)
    {
      Response.getReturnInfos().setMessage(
              "Die ID im ValueSet darf nicht NULL oder 0 sein!");
      erfolg = false;
    }
    else if (Request.getValueSet().getValueSetVersions() != null)
    {
      if (Request.getValueSet().getValueSetVersions().size() != 1)
      {
        Response.getReturnInfos().setMessage(
                "Die ValueSetVersion-Liste muss genau einen Eintrag haben oder die Liste ist NULL!");
        erfolg = false;
      }
      else
      {
        ValueSetVersion vsv = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];
        if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
        {
          Response.getReturnInfos().setMessage(
                  "Die ValueSetVersion muss eine ID größer als 0 beinhalten!");
          erfolg = false;
        }
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return true;
  }
}
