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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.DeleteInfo.Type;
import de.fhdo.terminologie.ws.types.ReturnType;
import org.hibernate.Session;

/**
 *
 * @author Philipp Urbauer
 */
public class RemoveTerminologyOrConcept
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter, String ipAddress)
  {
    return RemoveTerminologyOrConcept(parameter, null, ipAddress);
  }

  /**
   * Entfernt Konzepte aus einem Value Set
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== RemoveEntity started ======");

    // Return-Informationen anlegen
    RemoveTerminologyOrConceptResponseType response = new RemoveTerminologyOrConceptResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

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

    String result = "";
    Type t = parameter.getDeleteInfo().getType();

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    org.hibernate.Transaction tx = hb_session.beginTransaction();

    try
    {
      
      
      switch (t)
      {
        case CODE_SYSTEM:
          result = DeleteTermHelperWS.deleteCS_CSV(hb_session, true, parameter.getDeleteInfo().getCodeSystem().getId(), null);
          break;
        case CODE_SYSTEM_VERSION:
          result = DeleteTermHelperWS.deleteCS_CSV(hb_session, true, parameter.getDeleteInfo().getCodeSystem().getId(), parameter.getDeleteInfo().getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
          break;
        case VALUE_SET:
          result = DeleteTermHelperWS.deleteVS_VSV(hb_session, true, parameter.getDeleteInfo().getValueSet().getId(), null);
          break;
        case VALUE_SET_VERSION:
          result = DeleteTermHelperWS.deleteVS_VSV(hb_session, true, parameter.getDeleteInfo().getValueSet().getId(), parameter.getDeleteInfo().getValueSet().getValueSetVersions().iterator().next().getVersionId());
          break;
        case CODE_SYSTEM_ENTITY_VERSION:
          result = DeleteTermHelperWS.deleteCSEV(hb_session, parameter.getDeleteInfo().getCodeSystemEntityVersion().getVersionId(), parameter.getDeleteInfo().getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
          break;
        default:
          //result = "Please specify a valid type.";
          result = "";
          break;
      }
      
      tx.commit();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      result = "An Error occured: " + ex.getLocalizedMessage();
      
      tx.rollback();
    }
    finally
    {
      hb_session.close();
    }

    if (result == null || result.equals("") || result.contains("An Error occured:"))
    //if (result.length() > 0)
    {
      if(result == null)
        result = "Error: null";
      else if(result.equals(""))
        result = "Please specify a valid type.";
      // Error
      response.getReturnInfos().setMessage(result);
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    else
    {
      // Success
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage(result);
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
  private boolean validateParameter(RemoveTerminologyOrConceptRequestType Request, RemoveTerminologyOrConceptResponseType Response)
  {
    boolean erfolg = true;
    String sErrorMessage = "";

    if (Request == null)
    {
      sErrorMessage = "No parameters found!";
      erfolg = false;
    }
    else
    {
      if (erfolg)
      {
        if (Request.getDeleteInfo() == null || Request.getLoginToken() == null)
        {
          sErrorMessage = "Neither DeleteInfo nor Login may not be null!";
          erfolg = false;
        }
        else
        {
          if (erfolg)
          {
            if (Request.getDeleteInfo().getCodeSystem() == null
                    && Request.getDeleteInfo().getValueSet() == null
                    && Request.getDeleteInfo().getCodeSystemEntityVersion() == null)
            {
              sErrorMessage = "Either CodeSystem, ValueSet or CodeSystemEntityVersion may not be null!";
              erfolg = false;
            }
          }
        }
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setMessage(sErrorMessage);
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;
  }
}
