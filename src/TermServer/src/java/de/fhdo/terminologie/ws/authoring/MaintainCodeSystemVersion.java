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
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Sven Becker
 *
 * 07.10.11
 *
 */
public class MaintainCodeSystemVersion
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public MaintainCodeSystemVersionResponseType MaintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter, String ipAddress)
  {
////////// Logger //////////////////////////////////////////////////////////////
    if (logger.isInfoEnabled())
      logger.info("====== MaintainCodeSystemVersion gestartet ======");

////////// Return-Informationen anlegen ////////////////////////////////////////
    MaintainCodeSystemVersionResponseType response = new MaintainCodeSystemVersionResponseType();
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

      // CodeSystem und CodeSystemVersion zum Speichern vorbereiten 
      CodeSystem cs_Request = parameter.getCodeSystem();
      CodeSystemVersion csv_Request = (CodeSystemVersion) cs_Request.getCodeSystemVersions().toArray()[0];
      CodeSystemVersion csvNew = null;

      try
      { // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern                                             
        // versuche cs aus der DB zu laden und in cs_db, csv_db zu speichern
        if (cs_Request.getId() != null && cs_Request.getId() > 0)
        {
          CodeSystem cs_db = (CodeSystem) hb_session.get(CodeSystem.class, cs_Request.getId());
          CodeSystemVersion csv_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, csv_Request.getVersionId());

          // Name soll in der neuen Version geändert werden können
          if (cs_Request.getName() != null && cs_Request.getName().length() > 0)
            cs_db.setName(cs_Request.getName());

          // Description soll in der neuen Version geändert werden können
          if (cs_Request.getDescription() != null)
            cs_db.setDescription(cs_Request.getDescription());

          // DescriptionEng soll in der neuen Version geändert werden können
          if (cs_Request.getDescriptionEng() != null)
            cs_db.setDescriptionEng(cs_Request.getDescriptionEng());

          // Website soll in der neuen Version geändert werden können
          if (cs_Request.getWebsite() != null)
            cs_db.setWebsite(cs_Request.getWebsite());

          // Neue Version anlegen:  Diese enthält dann nur die Angaben, die auch gemacht wurden. leere Felder werden nicht aus alten Versionen übernommen
          if (parameter.getVersioning().getCreateNewVersion())
          {
            sCreateNewVersionMessage = "Neue CSVersion angelegt.";
            csvNew = new CodeSystemVersion();

            // Alte Version auf die aktuelle setzen
            csvNew.setPreviousVersionId(cs_db.getCurrentVersionId());

            // neues CS anlegen
            csvNew.setCodeSystem(new CodeSystem());

            // dem CS die passende ID von dem CS-Objekt aus der DB übergeben
            csvNew.getCodeSystem().setId(cs_db.getId());

            //Zwingende Variable darf nicht NULL sein!
            csvNew.setValidityRange(238l); // 238 => optional

            // Ein paar Defaultwerte setzen
            csvNew.setInsertTimestamp(new java.util.Date()); // Aktuelles Datum          
            csvNew.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
            csvNew.setStatusDate(new java.util.Date()); // Aktuelles Datum 
          }
          // Alte Version editieren:  csvNew = csv aus DB
          else
          {
            sCreateNewVersionMessage = "CSVersion (" + Long.toString(csv_Request.getVersionId()) + ") überschrieben.";
            csvNew = csv_db;
            LastChangeHelper.updateLastChangeDate(true, csvNew.getVersionId(), hb_session);
          }

          // Name setzen, falls vorhanden
          if (csv_Request.getName() != null)
            csvNew.setName(csv_Request.getName());

          // release Date soll angegeben werden können
          if (csv_Request.getReleaseDate() != null)
            csvNew.setReleaseDate(csv_Request.getReleaseDate());

          // ExpirationDate setzen, falls vorhanden
          if (csv_Request.getExpirationDate() != null)
            csvNew.setExpirationDate(csv_Request.getExpirationDate());

          // Source setzen, falls vorhanden
          if (csv_Request.getSource() != null)
            csvNew.setSource(csv_Request.getSource());

          // Description setzen, falls vorhanden
          if (csv_Request.getDescription() != null)
            csvNew.setDescription(csv_Request.getDescription());

          // PreferredLanguageCd setzen, falls vorhanden
          if (csv_Request.getPreferredLanguageCd() != null)
            csvNew.setPreferredLanguageCd(csv_Request.getPreferredLanguageCd());

          // ValidityRange setzen, falls vorhanden
          if (csv_Request.getValidityRange() != null)
            csvNew.setValidityRange(csv_Request.getValidityRange());

          // Oid setzen falls vorhanden
          if (csv_Request.getOid() != null)
            csvNew.setOid(csv_Request.getOid());

          // LicenceHolder setzen falls vorhanden
          if (csv_Request.getLicenceHolder() != null)
            csvNew.setLicenceHolder(csv_Request.getLicenceHolder());

          // UnderLicence setzen falls vorhanden
          if (csv_Request.getUnderLicence() != null)
            csvNew.setUnderLicence(csv_Request.getUnderLicence());

          // csvNew schon mal in der DB Speichern, damit ggf eine Id vergeben wird die dann gleich bei den licenceTypes benötigt wird
          if (parameter.getVersioning().getCreateNewVersion())
            hb_session.save(csvNew);

          // für alle angegebenen licenceTypes
          if (csv_Request.getLicenceTypes() != null && csv_Request.getLicenceTypes().isEmpty() == false)
          {
            Iterator<LicenceType> itLt_Request = csv_Request.getLicenceTypes().iterator(),
                    itLt_db;
            LicenceType lt_Request,
                    lt_New = null;
            while (itLt_Request.hasNext())
            {
              lt_Request = itLt_Request.next();

              // Neue CSVersion: neue licenceType-Objekte anlegen, CSV und typeTxt eintragen und speichern 
              if (parameter.getVersioning().getCreateNewVersion())
              {
                lt_New = new LicenceType();
                lt_New.setTypeTxt(lt_Request.getTypeTxt());
                lt_New.setCodeSystemVersion(csvNew);
                hb_session.save(lt_New);
                csvNew.getLicenceTypes().add(lt_New);
              }
              // Alte CSVersion bearbeiten bzw. ihre licenceTypes
              else
              {
                                // licenceType aus der DB auslesen, über csvNew.getLicenceType() nur möglich wenn man mit schleife
                // das ganze set nach der passenden ID durchsucht.  Sollte über hb_session.get() schneller gehen oder?
                LicenceType lt_DB = (LicenceType) hb_session.get(LicenceType.class, lt_Request.getId());

                // die im Request angegebene lt ID ist nicht in der DB, also mache mit nächstem lt weiter
                if (lt_DB == null)
                {
                  logger.debug("licenceType ID " + Long.toString(lt_Request.getId()) + " nicht in der DB vorhanden.");
                  continue;
                }
                if (lt_DB.getCodeSystemVersion().getVersionId() != csv_Request.getVersionId())
                {
                  logger.debug("Es wird versucht den licenceType einer anderen CSV zu ändern!");
                  continue;
                }

                // typeTxt ändern und speichern
                lt_DB.setTypeTxt(lt_Request.getTypeTxt());
                hb_session.save(lt_DB);
              }
            }
          }

          // In DB speichern damit csvNew eine ID bekommt falls es eine neue Version ist, ansonsten wird das Objekt aktualisiert
          hb_session.save(csvNew);

          if (parameter.getVersioning().getCreateNewVersion())
          {
            // CodeSystem mit CurrentVersion aktualisieren und speichern
            cs_db.setCurrentVersionId(csvNew.getVersionId());

            // TODO: Nötig?  oder reicht es nur die currentVersionId zu speichern?
            cs_db.getCodeSystemVersions().clear();
            cs_db.getCodeSystemVersions().add(csvNew);
          }

          hb_session.update(cs_db);
        }
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainCodeSystemVersion', Hibernate: " + e.getLocalizedMessage());

        logger.error(response.getReturnInfos().getMessage());
      }
      finally
      {
        // Transaktion abschließen
        if (cs_Request.getId() > 0 && csvNew.getVersionId() > 0)
        {
          hb_session.getTransaction().commit();
        }
        else
        {
          // Änderungen nicht erfolgreich
          logger.warn("[MaintainCodeSystemVersion.java] Änderungen nicht erfolgreich");
          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }
      if (cs_Request.getId() > 0 && csvNew.getVersionId() > 0)
      {
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("CodeSystemVersion erfolgreich geändert. " + sCreateNewVersionMessage);
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'MaintainCodeSystemVersion': " + e.getLocalizedMessage());
      logger.error(response.getReturnInfos().getMessage());
    }
    return response;
  }

  private boolean validateParameter(MaintainCodeSystemVersionRequestType request, MaintainCodeSystemVersionResponseType response)
  {
    boolean isValid = true;
    String errorMessage = "Unbekannter Fehler";
    CodeSystem cs_Request = request.getCodeSystem();

////////// Versioning //////////////////////////////////////////////////////////
    if (request.getVersioning() == null)
    {
      errorMessage = "Versioning darf nicht NULL sein!";
      isValid = false;
    }
    else
    {
      if (request.getVersioning().getCreateNewVersion() == null)
      {
        errorMessage = "createNewVersion darf nicht NULL sein!";
        isValid = false;
      }
    }

    ///////// CodeSystem ////////////////////////////////////////////////////////////        
    if (cs_Request == null)
    {
      errorMessage = "CodeSystem darf nicht NULL sein!";
      isValid = false;
    }
    else
    {
      // Nur wenn der Name des CS geändet werden soll, benötigt man die Id des CS
      if (cs_Request.getName() != null && cs_Request.getName().length() > 0 && (cs_Request.getId() == null || cs_Request.getId() < 1))
      {
        errorMessage = "Wenn der Name des Vokabulars geändert werden soll, muss die Id des Vokabulars angegeben werden!";
        isValid = false;
      }
      else
      {
        Set<CodeSystemVersion> vsvSet_Request = cs_Request.getCodeSystemVersions();
        // Gibt es eine CodeSystemVersion Liste?
        if (vsvSet_Request == null)
        {
          errorMessage = "CodeSystemVersion-Liste darf nicht NULL sein!";
          isValid = false;
        }
        else
        {
          // Wenn ja, hat sie genau einen Eintrag?
          if (vsvSet_Request.size() != 1)
          {
            errorMessage = "CodeSystemVersion-Liste hat " + Integer.toString(vsvSet_Request.size()) + " Einträge. Sie muss aber genau einen Eintrag haben!";
            isValid = false;
          }
          else
          {
            CodeSystemVersion csv_Request = (CodeSystemVersion) vsvSet_Request.toArray()[0];

            // new version
            if (request.getVersioning().getCreateNewVersion() == true)
            {
              if (csv_Request.getName() == null || csv_Request.getName().length() <= 0)
              {
                errorMessage = "Es muss ein Name für die neue Version angegeben werden!";
                isValid = false;
              }
            }

            // edit version
            else
            {
              if (csv_Request.getVersionId() == null || csv_Request.getVersionId() < 1)
              {
                errorMessage = "Die Id der Vokabularversion darf nicht NULL oder kleiner 1 sein!";
                isValid = false;
              }
            }

            // falls licenceTypes angegeben sind, müssen diese auch eine id und typeTxt haben
            if (csv_Request.getUnderLicence() == null)
            {
            }
            else
            {
              if (isValid && csv_Request.getUnderLicence())
              {
                if (csv_Request.getLicenceTypes().size() > 0)
                {
                  Iterator<LicenceType> iLicenceType_Request = csv_Request.getLicenceTypes().iterator();
                  LicenceType licence_Request;
                  while (iLicenceType_Request.hasNext())
                  {
                    licence_Request = iLicenceType_Request.next();
                    if (licence_Request.getId() == null || licence_Request.getId() < 1)
                    {
                      errorMessage = "LicenceType.getId() darf nicht NULL oder kleiner 1 sein!" + " Size=" + Integer.toString(csv_Request.getLicenceTypes().size());
                      isValid = false;
                    }
                    if (licence_Request.getTypeTxt() == null || licence_Request.getTypeTxt().length() == 0)
                    {
                      errorMessage = "LicenceType.getTypeTxt() darf nicht NULL sein oder eine Länge von 0 haben!";
                      isValid = false;
                    }
                  }
                }
              }
            }
          }
        }
      }
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
