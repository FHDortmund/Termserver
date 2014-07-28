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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class MaintainConceptAssociationType
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public MaintainConceptAssociationTypeResponseType MaintainConceptAssociationType(MaintainConceptAssociationTypeRequestType parameter, String ipAddress)
  {
////////// Logger //////////////////////////////////////////////////////////////
    if (logger.isInfoEnabled())
      logger.info("====== MaintainConceptAssociationType gestartet ======");

////////// Return-Informationen anlegen ////////////////////////////////////////
    MaintainConceptAssociationTypeResponseType response = new MaintainConceptAssociationTypeResponseType();
    response.setReturnInfos(new ReturnType());

////////// Parameter prüfen ////////////////////////////////////////////////////
    if (validateParameter(parameter, response) == false)
      return response;

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

////////// Der eigentliche Teil  /////////////////////////////////////////////// 
    try
    {
      // Für die Statusmeldung ob eine neue VSV angelegt oder die alte verändert wurde
      String sCreateNewVersionMessage = "";

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      boolean bNewVersion = parameter.getVersioning().getCreateNewVersion();

      CodeSystemEntity cse_Request = parameter.getCodeSystemEntity();
      CodeSystemEntity cse_db = null;
      CodeSystemEntityVersion csev_Request = (CodeSystemEntityVersion) cse_Request.getCodeSystemEntityVersions().toArray()[0];
      CodeSystemEntityVersion csev_New = null;


      try
      { // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern      
////////////////// neue Version anlegen oder alte bearbeiten?
        if (bNewVersion)
        {
          // original CSE aus DB laden
          cse_db = (CodeSystemEntity) hb_session.load(CodeSystemEntity.class, cse_Request.getId());

          // neues CodeSystemEntityVersion-Objekt erzeugen
          csev_New = new CodeSystemEntityVersion();

          // Not NULL werte setzen
          csev_New.setCodeSystemEntity(cse_db);
          csev_New.setStatusVisibility(Definitions.STATUS_CODES.ACTIVE.getCode());
          csev_New.setStatusVisibilityDate(new java.util.Date());
          csev_New.setInsertTimestamp(new java.util.Date());

          csev_New.setPreviousVersionId(cse_db.getCurrentVersionId());

          // specihern damit eine Id erzeugt wird
          hb_session.save(csev_New);

          // Revision setzen, falls angegeben
          if (csev_Request.getMajorRevision() != null)
            csev_New.setMajorRevision(csev_Request.getMajorRevision());
          if (csev_Request.getMinorRevision() != null)
            csev_New.setMinorRevision(csev_Request.getMinorRevision());

          // AssociationType erzeugen und CSEV hinzufügen
          if (csev_Request.getAssociationTypes().size() > 0)
          {
            AssociationType aType_Request = (AssociationType) csev_Request.getAssociationTypes().toArray()[0];

                        // lade aus der DB den aType mit der passenden CurrentVersionId bzw versionId von csev. Es wird previousVersionId genutzt, 
            // weil die neue CSEVersion ja schon durch Hibernate.save eine neue VersionId bekommen hat und die alte Id jetzt die prev.Id ist
            AssociationType aType_New = (AssociationType) hb_session.load(AssociationType.class, csev_New.getPreviousVersionId());

            // Eigenschaften setzen
            if (aType_Request.getForwardName() != null && aType_Request.getForwardName().length() > 0)
              aType_New.setForwardName(aType_Request.getForwardName());
            if (aType_Request.getReverseName() != null && aType_Request.getReverseName().length() > 0)
              aType_New.setReverseName(aType_Request.getReverseName());

            hb_session.save(aType_New);
          }

          
          cse_db.setCurrentVersionId(csev_New.getVersionId());
          hb_session.save(cse_db);
        }

////////////////// bestehende Version updaten:  der einfachheitshalber wird hier das csev.Objekt, dass geändet werden sollen ebenfalls als csev_New bezeichnet
        else
        {
          // original CSE aus DB laden                                  
          cse_db = (CodeSystemEntity) hb_session.load(CodeSystemEntity.class, cse_Request.getId());

          // Request CSEV, benötigt man für die versionId
          csev_Request = (CodeSystemEntityVersion) cse_Request.getCodeSystemEntityVersions().toArray()[0];

          // "neues" CodeSystemEntityVersion-Objekt wird aus der DB geladen. Jetzt können die Änderungen vorgenommen werden
          csev_New = (CodeSystemEntityVersion) hb_session.load(CodeSystemEntityVersion.class, csev_Request.getVersionId());

          // Revision setzen, falls angegeben
          if (csev_Request.getMajorRevision() != null)
            csev_New.setMajorRevision(csev_Request.getMajorRevision());
          if (csev_Request.getMinorRevision() != null)
            csev_New.setMinorRevision(csev_Request.getMinorRevision());

          // AssociationType erzeugen und CSEV hinzufügen
          if (csev_Request.getAssociationTypes().size() > 0)
          {
            AssociationType aType_Request = (AssociationType) csev_Request.getAssociationTypes().toArray()[0];

            // lade aus der DB den aType mit der passenden CurrentVersionId bzw versionId von csev
            AssociationType aType_New = (AssociationType) hb_session.load(AssociationType.class, csev_New.getVersionId());

            // Eigenschaften setzen
            if (aType_Request.getForwardName() != null && aType_Request.getForwardName().length() > 0)
              aType_New.setForwardName(aType_Request.getForwardName());
            if (aType_Request.getReverseName() != null && aType_Request.getReverseName().length() > 0)
              aType_New.setReverseName(aType_Request.getReverseName());

            hb_session.save(aType_New);
          }

          

          // Nachdem die neuen Objekte angelegt oder die alten aktualisiert wurden, speichere CSE in DB
          cse_db.setCurrentVersionId(csev_New.getVersionId());
          hb_session.save(cse_db);
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociationType', Hibernate: " + e.getLocalizedMessage());
        logger.error(response.getReturnInfos().getMessage());
      }
      finally
      {
        // Transaktion abschließen
        if (csev_New.getVersionId() > 0)
          hb_session.getTransaction().commit();
        else
        {
          logger.warn("[MaintainConceptAssociationType.java] Änderungen nicht erfolgreich");
          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }
      if (csev_New.getVersionId() > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("AssociationType erfolgreich geändert. " + sCreateNewVersionMessage);
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptAssociationType': " + e.getLocalizedMessage());
      logger.error(response.getReturnInfos().getMessage());
    }
    return response;
  }

  private boolean validateParameter(MaintainConceptAssociationTypeRequestType request, MaintainConceptAssociationTypeResponseType response)
  {
    boolean isValid = false;
    boolean bNewVersion;
    String errorMessage = "Unbekannter Fehler";
    CodeSystemEntity cse_Request = request.getCodeSystemEntity();

        // mit switch kann man sehr einfach die abfragen alle untereinander schreiben, ohne dieses ganze if-else-if-else-if... zeug
    // die abfragen müssen nur in der richtigen Reihenfolge stehen 
    abort_validation: // Label mötig um aus tieferen Schleifen in der Switch-Anweisung komplett rauszuspringen
    switch (1)
    {
      case 1:
////////////////// Versioning //////////////////////////////////////////////////
        if (request.getVersioning() == null)
        {
          errorMessage = "Versioning darf nicht NULL sein!";
          break;
        }
        if (request.getVersioning().getCreateNewVersion() == null)
        {
          errorMessage = "createNewVersion darf nicht NULL sein!";
          break;
        }
        bNewVersion = request.getVersioning().getCreateNewVersion();
////////////////// CodeSystemEntity ////////////////////////////////////////////
        if (cse_Request == null)
        {
          errorMessage = "CodeSystemEntity darf nicht NULL sein!";
          break;
        }
        // Gibt es eine CodeSystemEntityVersion Liste?
        Set<CodeSystemEntityVersion> csevSet_Request = cse_Request.getCodeSystemEntityVersions();
        if (csevSet_Request == null)
        {
          errorMessage = "CodeSystemEntityVersion-Liste darf nicht NULL sein!";
          break;
        }
        // Wenn ja, hat sie genau einen Eintrag?
        if (csevSet_Request.size() != 1)
        {
          errorMessage = "CodeSystemEntityVersion-Liste hat " + Integer.toString(csevSet_Request.size()) + " Einträge. Sie muss aber genau einen Eintrag haben!";
          break;
        }
        CodeSystemEntityVersion csev_Request = (CodeSystemEntityVersion) csevSet_Request.toArray()[0];
        // hat VSV eine gültige ID?
        if (csev_Request.getVersionId() == null || csev_Request.getVersionId() < 1)
        {
          errorMessage = "CodeSystemEntityVersion Id darf nicht NULL oder kleiner 1 sein!";
          break;
        }
        ////////////////// AssosiationType (optional) //////////////////////////////
        Set<AssociationType> aTypeSet_Request = csev_Request.getAssociationTypes();
        if (aTypeSet_Request.isEmpty() == false)
        {
          if (aTypeSet_Request.size() > 1)
          {
            errorMessage = "Falls AssociationType angegben wurde, darf es nur genau einer sein!";
            break;
          }
          // hole aus dem aTypeSet den einen aType raus
          AssociationType aType_Request = (AssociationType) aTypeSet_Request.toArray()[0];
          if (aType_Request == null)
          {
            errorMessage = "AssociationType konnte nicht aus derm AssociationTypeSet generiert werden!";
            break;
          }
          if (aType_Request.getForwardName() == null || aType_Request.getForwardName().length() == 0)
          {
            errorMessage = "Falls ein AssociationType angegeben wurde darf ForwardName nicht NULL sein!";
            break;
          }
          if (aType_Request.getReverseName() == null || aType_Request.getReverseName().length() == 0)
          {
            errorMessage = "Falls ein AssociationType angegeben wurde darf ReverseName nicht NULL sein!";
            break;
          }
        }

        
        // Falls alles OK
        isValid = true;
    }

    if (isValid == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage(errorMessage);
    }
    return isValid;
  }
}
