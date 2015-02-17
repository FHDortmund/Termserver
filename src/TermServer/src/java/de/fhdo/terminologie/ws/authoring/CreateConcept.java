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

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateConcept
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateConceptResponseType CreateConcept(CreateConceptRequestType parameter, AuthenticateInfos loginInfoType)
  {
    return CreateConcept(parameter, null, loginInfoType);
  }

  public CreateConceptResponseType CreateConcept(CreateConceptRequestType parameter, org.hibernate.Session session, AuthenticateInfos loginInfoType)
  {
    if (logger.isInfoEnabled())
      logger.info("====== CreateConcept gestartet ======");

    // Return-Informationen anlegen
    CreateConceptResponseType response = new CreateConceptResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    CodeSystem paramCodeSystem = null;
    CodeSystemEntity paramCodeSystemEntity = null;

    if (parameter != null)
    {
      paramCodeSystem = parameter.getCodeSystem();
      paramCodeSystemEntity = parameter.getCodeSystemEntity();
    }

    CreateConceptOrAssociationType(response, parameter.getLoginToken(), paramCodeSystem, paramCodeSystemEntity, session, loginInfoType);

    return response;
  }

  public void CreateConceptOrAssociationType(CreateConceptResponseType response,
          String loginToken, CodeSystem paramCodeSystem,
          CodeSystemEntity paramCodeSystemEntity,
          org.hibernate.Session session, AuthenticateInfos loginInfoType)
  {
    boolean createHibernateSession = (session == null);

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = loginInfoType != null;

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Sie müssen am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return;
    }

    // TODO Lizenzen prüfen (?)
    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = null;
      org.hibernate.Transaction tx = null;

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

      // CodeSystem und CodeSystem-Version zum Speichern vorbereiten
      long codeSystemVersionId = 0;
      long codeSystemEntityVersionId = 0;

      if (paramCodeSystem != null && paramCodeSystem.getCodeSystemVersions() != null
              && paramCodeSystem.getCodeSystemVersions().size() > 0)
      {
        codeSystemVersionId = ((CodeSystemVersion) paramCodeSystem.getCodeSystemVersions().toArray()[0]).getVersionId();
      }
      logger.debug("codeSystemVersionId: " + codeSystemVersionId);

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // Neue Entity und Entity-Version erstellen
        CodeSystemEntity entity = new CodeSystemEntity();
        hb_session.save(entity);

        CodeSystemEntityVersion entityVersion = (CodeSystemEntityVersion) paramCodeSystemEntity.getCodeSystemEntityVersions().toArray()[0];

        CodeSystemConcept concept = null;
        if (entityVersion.getCodeSystemConcepts() != null && entityVersion.getCodeSystemConcepts().size() > 0)
          concept = (CodeSystemConcept) entityVersion.getCodeSystemConcepts().toArray()[0];

        AssociationType assType = null;
        if (entityVersion.getAssociationTypes() != null && entityVersion.getAssociationTypes().size() > 0)
          assType = (AssociationType) entityVersion.getAssociationTypes().toArray()[0];

        Set<CodeSystemMetadataValue> listMetadataCS = entityVersion.getCodeSystemMetadataValues();
        
        //entityVersion.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        //entityVersion.setStatus();  
        if (entityVersion.getStatusVisibility() == null)
          entityVersion.setStatusVisibility(Definitions.STATUS_CODES.ACTIVE.getCode());
        entityVersion.setStatusVisibilityDate(new Date());
        entityVersion.setInsertTimestamp(new Date());
        entityVersion.setCodeSystemEntity(entity);
        entityVersion.setCodeSystemConcepts(null);
        entityVersion.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
        entityVersion.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
        entityVersion.setCodeSystemMetadataValues(null);
        entityVersion.setValueSetMetadataValues(null);
        entityVersion.setConceptValueSetMemberships(null);
        entityVersion.setAssociationTypes(null);

        // new attributes, set if they are null
        if (entityVersion.getStatusDeactivated() == null)
        {
          entityVersion.setStatusDeactivated(1);
          entityVersion.setStatusDeactivatedDate(new Date());
        }
        if (entityVersion.getStatusWorkflow() == null)
        {
          entityVersion.setStatusWorkflow(0);
          entityVersion.setStatusWorkflowDate(new Date());
        }
        if (entityVersion.getEffectiveDate() == null)
          entityVersion.setEffectiveDate(new Date());

        hb_session.save(entityVersion);

        // Antwort erstellen
        codeSystemEntityVersionId = entityVersion.getVersionId();

        CodeSystemEntityVersion entityVersionReturn = new CodeSystemEntityVersion();
        entityVersionReturn.setVersionId(codeSystemEntityVersionId);
        //entityVersionReturn.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
        entityVersionReturn.setStatusVisibility(entityVersion.getStatusVisibility());

        response.setCodeSystemEntity(new CodeSystemEntity());
        response.getCodeSystemEntity().setId(entity.getId());
        response.getCodeSystemEntity().setCurrentVersionId(entityVersion.getVersionId());
        response.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
        response.getCodeSystemEntity().getCodeSystemEntityVersions().add(entityVersionReturn);

        logger.debug("EntityId: " + entity.getId());
        logger.debug("EntityVersionId: " + codeSystemEntityVersionId);

        // CurrentVersion in der Entity speichern
        entity.setCurrentVersionId(entityVersion.getVersionId());
        hb_session.update(entity);

        logger.debug("CurrentVersionId: " + entity.getCurrentVersionId());

        // Konzept speichern (inkl. Translations)
        if (concept != null)
        {
          concept.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
          concept.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);
          concept.setCodeSystemEntityVersionId(codeSystemEntityVersionId);

          Iterator<CodeSystemConceptTranslation> itTranslation = concept.getCodeSystemConceptTranslations().iterator();
          while (itTranslation.hasNext())
          {
            CodeSystemConceptTranslation cTranslation = itTranslation.next();
            cTranslation.setCodeSystemConcept(concept);
          }

          hb_session.save(concept);
        }
        if (assType != null)
        {
          assType.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
          assType.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);
          assType.setCodeSystemEntityVersionId(codeSystemEntityVersionId);

          hb_session.save(assType);
        }

        logger.debug("save metadata...");
        if (listMetadataCS != null)
        {
          logger.debug("check values...");

          for (CodeSystemMetadataValue mv : listMetadataCS)
          {
            if (mv.getParameterValue() != null && mv.getParameterValue().length() > 0)
            {
              // add new md value
              CodeSystemMetadataValue mv_db = new CodeSystemMetadataValue(mv.getParameterValue());
              mv_db.setMetadataParameter(new MetadataParameter());
              mv_db.getMetadataParameter().setId(mv.getMetadataParameter().getId());
              mv_db.setCodeSystemEntityVersion(entityVersion);
              hb_session.save(mv_db);

              logger.debug("save metadata-value with mp-id: " + mv_db.getMetadataParameter().getId() + ", value: " + mv_db.getParameterValue());
            }
          }
        }

        // Beziehung zum Vokabular speichern
        if (codeSystemVersionId > 0)
        {
          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();

          membership.setId(new CodeSystemVersionEntityMembershipId());
          membership.getId().setCodeSystemEntityId(entity.getId());
          membership.getId().setCodeSystemVersionId(codeSystemVersionId);

          membership.setIsAxis(Boolean.FALSE);
          membership.setIsMainClass(Boolean.FALSE);

          if (paramCodeSystemEntity.getCodeSystemVersionEntityMemberships() != null
                  && paramCodeSystemEntity.getCodeSystemVersionEntityMemberships().size() > 0)
          {
            CodeSystemVersionEntityMembership memberRequest
                    = (CodeSystemVersionEntityMembership) paramCodeSystemEntity.getCodeSystemVersionEntityMemberships().toArray()[0];

            if (memberRequest != null)
            {
              membership.setIsAxis(memberRequest.getIsAxis());
              membership.setIsMainClass(memberRequest.getIsMainClass());
            }
          }

          hb_session.save(membership);
        }

        /*if (paramCodeSystem != null && paramCodeSystem.getId() != null)
         {
         //Check ob MetadataParameter default Values angelegt werden müssen
         String hql = "select distinct mp from MetadataParameter mp";
         hql += " join fetch mp.codeSystem cs";

         HQLParameterHelper parameterHelper = new HQLParameterHelper();
         parameterHelper.addParameter("cs.", "id", paramCodeSystem.getId());

         // Parameter hinzufügen (immer mit AND verbunden)
         hql += parameterHelper.getWhere("");
         logger.debug("HQL: " + hql);

         // Query erstellen
         org.hibernate.Query q = hb_session.createQuery(hql);
         parameterHelper.applyParameter(q);

         List<MetadataParameter> mpList = q.list();
         if (!mpList.isEmpty())
         {
         Iterator<MetadataParameter> iter = mpList.iterator();
         while (iter.hasNext())
         {
         MetadataParameter mp = (MetadataParameter) iter.next();
         if (mp.getCodeSystem() != null && mp.getCodeSystem().getName() != null)
         {
         if (!mp.getCodeSystem().getName().equals("LOINC"))
         {
         CodeSystemMetadataValue csmv = new CodeSystemMetadataValue();

         csmv.setParameterValue("");
         csmv.setMetadataParameter(mp);
         csmv.setCodeSystemEntityVersion(entityVersion);

         hb_session.save(csmv);
         }
         }
         }
         }
         }*/
        response.getReturnInfos().setCount(1);
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'CreateConcept', Hibernate: " + e.getLocalizedMessage());
        response.setCodeSystemEntity(null);
        codeSystemEntityVersionId = 0;

        logger.error("Fehler bei 'CreateConcept', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (createHibernateSession)
        {
          if (codeSystemEntityVersionId > 0)
          {

            if (codeSystemVersionId > 0)
            {
              LastChangeHelper.updateLastChangeDate(true, codeSystemVersionId, hb_session);
            }
            hb_session.getTransaction().commit();
          }
          else
          {
            // Änderungen nicht erfolgreich
            logger.warn("[CreateConcept.java] Änderungen nicht erfolgreich, codeSystemEntityVersionId: "
                    + codeSystemEntityVersionId);

            hb_session.getTransaction().rollback();

            // Status an den Aufrufer weitergeben
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Konzept konnte nicht erstellt werden!");
            response.setCodeSystemEntity(null);
          }
          hb_session.close();
        }
      }

      // Antwort zusammenbauen
      if (codeSystemEntityVersionId > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Konzept erfolgreich erstellt");
        response.getReturnInfos().setCount(1);
      }
      else
        response.getReturnInfos().setCount(0);

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateConcept': " + e.getLocalizedMessage());
      response.setCodeSystemEntity(null);

      logger.error("Fehler bei 'CreateConcept': " + e.getLocalizedMessage());

      e.printStackTrace();
    }
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(CreateConceptRequestType Request, CreateConceptResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem == null)
    {
      Response.getReturnInfos().setMessage("CodeSystem darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      /*if (codeSystem.getId() == null || codeSystem.getId() == 0)
       {
       Response.getReturnInfos().setMessage(
       "Es muss eine ID für das CodeSystem angegeben sein, in welchem Sie das Konzept einfügen möchten!");
       erfolg = false;
       }*/

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
                    "Es muss eine ID für die CodeSystem-Version angegeben sein, in welcher Sie das Konzept einfügen möchten!");
            erfolg = false;
          }
        }
      }

      //Request.getCodeSystemEntity()
    }

    CodeSystemEntity codeSystemEntity = Request.getCodeSystemEntity();
    if (codeSystemEntity == null)
    {
      Response.getReturnInfos().setMessage("CodeSystem-Entity darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      Set<CodeSystemEntityVersion> csevSet = codeSystemEntity.getCodeSystemEntityVersions();
      if (csevSet != null)
      {
        if (csevSet.size() > 1)
        {
          Response.getReturnInfos().setMessage(
                  "Die CodeSystem-Entity-Version-Liste darf maximal einen Eintrag haben!");
          erfolg = false;
        }
        else if (csevSet.size() == 1)
        {
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csevSet.toArray()[0];

          Set<CodeSystemConcept> conceptSet = csev.getCodeSystemConcepts();
          if (conceptSet != null && conceptSet.size() == 1)
          {
            CodeSystemConcept concept = (CodeSystemConcept) conceptSet.toArray()[0];

            if (concept.getCode() == null || concept.getCode().isEmpty())
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Code für das Konzept angeben!");
              erfolg = false;
            }
            else if (concept.getIsPreferred() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen 'isPreferred' für das Konzept angeben!");
              erfolg = false;
            }

            if (concept.getCodeSystemConceptTranslations() != null)
            {
              Iterator<CodeSystemConceptTranslation> itTrans = concept.getCodeSystemConceptTranslations().iterator();

              while (itTrans.hasNext())
              {
                CodeSystemConceptTranslation translation = itTrans.next();
                if (translation.getTerm() == null)
                {
                  Response.getReturnInfos().setMessage("Sie müssen einen Term für eine Konzept-Übersetzung angeben!");
                  erfolg = false;
                }
                else if (translation.getLanguageCd() == null)
                {
                  Response.getReturnInfos().setMessage("Sie müssen einen 'LanguageCd' für eine Konzept-Übersetzung angeben!");
                  erfolg = false;
                }
              }
            }
          }
          else
          {
            Response.getReturnInfos().setMessage("CodeSystemConcept-Liste darf nicht NULL sein und muss genau 1 Eintrag haben!");
            erfolg = false;
          }
        }
      }
      else
      {
        Response.getReturnInfos().setMessage("CodeSystemEntityVersion darf nicht NULL sein!");
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
