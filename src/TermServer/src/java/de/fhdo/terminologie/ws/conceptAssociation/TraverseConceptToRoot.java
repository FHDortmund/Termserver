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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class TraverseConceptToRoot
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public TraverseConceptToRootResponseType TraverseConceptToRoot(TraverseConceptToRootRequestType parameter, String ipAddress)
  {
    return TraverseConceptToRoot(parameter, null, ipAddress);
  }

  public TraverseConceptToRootResponseType TraverseConceptToRoot(TraverseConceptToRootRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== TraverseConceptToRoot gestartet ======");

    // Return-Informationen anlegen
    TraverseConceptToRootResponseType response = new TraverseConceptToRootResponseType();
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
      List<CodeSystemEntityVersionAssociation> liste = new LinkedList<CodeSystemEntityVersionAssociation>();

      CodeSystemEntityVersion vsvFilter = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
      long versionId = vsvFilter.getVersionId();

      ListConceptAssociations lcaService = new ListConceptAssociations();

      do
      {
        ListConceptAssociationsRequestType lcaRequest = new ListConceptAssociationsRequestType();
        lcaRequest.setLoginToken(parameter.getLoginToken());
        lcaRequest.setCodeSystemEntity(new CodeSystemEntity());

        CodeSystemEntityVersion csevRequest = new CodeSystemEntityVersion();
        csevRequest.setVersionId(versionId);
        lcaRequest.setReverse(true);  // Beziehungen nach "oben" suchen
        lcaRequest.getCodeSystemEntity().getCodeSystemEntityVersions().add(csevRequest);

        lcaRequest.setCodeSystemEntityVersionAssociation(new CodeSystemEntityVersionAssociation());
        lcaRequest.getCodeSystemEntityVersionAssociation().setAssociationKind(Definitions.ASSOCIATION_KIND.TAXONOMY.getCode());

        ListConceptAssociationsResponseType lcaResponse = lcaService.ListConceptAssociations(lcaRequest, session, ipAddress);

        versionId = 0;
        if (lcaResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
          logger.debug("Erg: " + lcaResponse.getReturnInfos().getMessage());

          if (lcaResponse.getCodeSystemEntityVersionAssociation() != null
                  && lcaResponse.getCodeSystemEntityVersionAssociation().size() > 0)
          {
            // normalerweise sollte natürlich nur 1 "Oberbegriff" zurück kommen
            CodeSystemEntityVersionAssociation assTemp = lcaResponse.getCodeSystemEntityVersionAssociation().get(0);
            //CodeSystemConcept cc = (CodeSystemConcept) assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().toArray()[0];
            //logger.error("TERM: " + cc.getTerm());

            liste.add(assTemp);
            versionId = assTemp.getLeftId();
          }
        }

      }
      while (versionId > 0);

      CodeSystemEntityVersion csevRoot = null;
      CodeSystemEntityVersion csevCurrent = null;

      if (liste.size() > 0)
      {
        if (parameter.getDirectionToRoot() != null && parameter.getDirectionToRoot())
        {
          // Die Liste vorwärts abarbeiten

          // Anfrage-Konzept als root setzen (nur ID)
          CodeSystemEntityVersion csevParam = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
          csevParam.setCodeSystemEntity(parameter.getCodeSystemEntity());
          csevParam.getCodeSystemEntity().setCodeSystemEntityVersions(null);
          csevRoot = csevParam;
          csevCurrent = csevRoot;

          for (int i = 0; i < liste.size(); ++i)
          {
            CodeSystemEntityVersionAssociation assTemp = liste.get(i);
            CodeSystemEntityVersion csevAdd = null;

            // Prüfen, welche ID nicht die Left-ID ist (= "rightID") und das Konzept als Root-Element festlegen
            if (assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null
                    && assTemp.getLeftId() != assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId())
            {
              csevAdd = assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1();
            }
            else if (assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null
                    && assTemp.getLeftId() != assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId())
            {
              csevAdd = assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();
            }

            // Beziehung hinzufügen
            if (csevCurrent != null)
            {
              csevCurrent.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(new HashSet<CodeSystemEntityVersionAssociation>());
              assTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
              assTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
              csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().add(assTemp);

              logger.debug("Beziehung: " + assTemp.getId());

              // Konzept hinzufügen
              CodeSystemEntityVersionAssociation existingAssociation = (CodeSystemEntityVersionAssociation) csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().toArray()[0];
              existingAssociation.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csevAdd);

              csevCurrent = existingAssociation.getCodeSystemEntityVersionByCodeSystemEntityVersionId1();

              if (csevCurrent != null)
                logger.debug("Konzept: " + csevCurrent.getVersionId());
            }
          }
        }
        else
        {
          // Die Liste rückwärts abarbeiten
          for (int i = liste.size() - 1; i >= 0; --i)
          {
            CodeSystemEntityVersionAssociation assTemp = liste.get(i);

            //logger.debug("Ass.id: " + assTemp.getId());
            CodeSystemEntityVersion csevAdd = null;

            // Prüfen, welche ID nicht die Left-ID ist (= "rightID") und das Konzept als Root-Element festlegen
            if (assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null
                    && assTemp.getLeftId() != assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId())
            {
              csevAdd = assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId1();
            }
            else if (assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null
                    && assTemp.getLeftId() != assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId())
            {
              csevAdd = assTemp.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();
            }

            // Konzept hinzufügen
            if (csevCurrent == null)
            {
              // 1. Element
              csevRoot = csevAdd;

              csevCurrent = csevRoot;
            }
            else
            {
              CodeSystemEntityVersionAssociation existingAssociation = (CodeSystemEntityVersionAssociation) csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().toArray()[0];
              existingAssociation.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csevAdd);

              csevCurrent = existingAssociation.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();
            }
            logger.debug("Konzept: " + csevCurrent.getVersionId());

            // Beziehung hinzufügen
            if (csevCurrent != null)
            {
              csevCurrent.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(new HashSet<CodeSystemEntityVersionAssociation>());
              assTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
              assTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
              csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().add(assTemp);

              logger.debug("Beziehung: " + assTemp.getId());
            }
          }

          // zum Schluss noch das Anfrage-Konzept hinzufügen (nur ID)
          if (csevCurrent != null)
          {
            CodeSystemEntityVersion csevAdd = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
            csevAdd.setCodeSystemEntity(parameter.getCodeSystemEntity());
            csevAdd.getCodeSystemEntity().setCodeSystemEntityVersions(null);

            CodeSystemEntityVersionAssociation existingAssociation = (CodeSystemEntityVersionAssociation) csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().toArray()[0];
            existingAssociation.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csevAdd);
          }
        }

        response.setCodeSystemEntityVersionRoot(csevRoot);

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Root-Element erfolgreich ermittelt, Anzahl Traversierungen: " + liste.size());
        response.getReturnInfos().setCount(liste.size());
      }
      else
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Zum gegebenen Konzept konnte kein Root-Element ermittelt werden!");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnConceptDetails': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private boolean validateParameter(TraverseConceptToRootRequestType Request, TraverseConceptToRootResponseType Response)
  {
    boolean erfolg = true;

//    if (Request.getLoginToken() == null || Request.getLoginToken().length() == 0)
//    {
//      Response.getReturnInfos().setMessage(
//              "Das Login-Token darf nicht leer sein!");
//      erfolg = false;
//    }
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
