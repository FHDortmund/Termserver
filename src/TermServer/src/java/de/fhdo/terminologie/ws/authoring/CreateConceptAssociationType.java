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

import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;

import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateConceptAssociationType
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateConceptAssociationTypeResponseType CreateConceptAssociationType(
    CreateConceptAssociationTypeRequestType parameter, String ipAddress)
  {
    return CreateConceptAssociationType(parameter, null, ipAddress);
  }
  
  public CreateConceptAssociationTypeResponseType CreateConceptAssociationType(
    CreateConceptAssociationTypeRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== CreateConceptAssociationType gestartet ======");
    
    // Return-Informationen anlegen
    CreateConceptAssociationTypeResponseType response = new CreateConceptAssociationTypeResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }
    
    
    CreateConcept cc = new CreateConcept();
    CreateConceptResponseType responseCC = new CreateConceptResponseType();
    responseCC.setReturnInfos(response.getReturnInfos());
    
    CodeSystem paramCodeSystem = null;
    CodeSystemEntity paramCodeSystemEntity = null;
    
    if(parameter != null)
    {
      paramCodeSystem = parameter.getCodeSystem();
      paramCodeSystemEntity = parameter.getCodeSystemEntity();
    }
    
    cc.CreateConceptOrAssociationType(responseCC, parameter.getLoginToken(), paramCodeSystem, paramCodeSystemEntity, session, ipAddress);
    
    response.setReturnInfos(responseCC.getReturnInfos());
    response.setCodeSystemEntity(responseCC.getCodeSystemEntity());
    
    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   * 
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(CreateConceptAssociationTypeRequestType Request,
                                    CreateConceptAssociationTypeResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem != null)
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
              "Es muss eine ID für die CodeSystem-Version angegeben sein, in welchem Sie das Konzept einfügen möchten!");
            erfolg = false;
          }
        }
      }
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

          Set<AssociationType> assTypesSet = csev.getAssociationTypes();
          if (assTypesSet != null && assTypesSet.size() == 1)
          {
            AssociationType assType = (AssociationType) assTypesSet.toArray()[0];

            if (assType.getForwardName() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Forward-Namen für das Konzept vergeben!");
              erfolg = false;
            }
            else if (assType.getReverseName() == null)
            {
              Response.getReturnInfos().setMessage("Sie müssen einen Reverse-Namen für das Konzept vergeben!");
              erfolg = false;
            }
          }
          else
          {
            Response.getReturnInfos().setMessage("AssociationType-Liste darf nicht NULL sein und muss genau 1 Eintrag haben!");
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
