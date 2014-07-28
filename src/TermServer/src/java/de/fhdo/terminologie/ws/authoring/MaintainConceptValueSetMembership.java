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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Philipp Urbauer
 */
public class MaintainConceptValueSetMembership
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public MaintainConceptValueSetMembershipResponseType MaintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())    
      logger.info("====== MaintainConceptValueSetMembership gestartet ======");    

    MaintainConceptValueSetMembershipResponseType response = new MaintainConceptValueSetMembershipResponseType();
    response.setReturnInfos(new ReturnType());

    //Parameter prüfen
    if (validateParameter(parameter, response) == false)    
      return response; // Fehler bei den Parametern
    
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

    try{
        CodeSystemEntityVersion csev = parameter.getCodeSystemEntityVersion();
        ConceptValueSetMembership cvsm = csev.getConceptValueSetMemberships().iterator().next();
      // Hibernate-Block, Session öffnen
      org.hibernate.Session     hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        ConceptValueSetMembership cvsm_db = null;
        if(cvsm.getId() == null){
        
                ConceptValueSetMembershipId cvsmId = new ConceptValueSetMembershipId(
                        cvsm.getCodeSystemEntityVersion().getVersionId(), cvsm.getValueSetVersion().getVersionId());
                cvsm_db = (ConceptValueSetMembership)hb_session.get(ConceptValueSetMembership.class, cvsmId);
        }else{
            cvsm_db = (ConceptValueSetMembership)hb_session.get(ConceptValueSetMembership.class, cvsm.getId());
        }
        
        cvsm_db.setStatus(cvsm.getStatus());
        if(cvsm.getStatusDate() != null){
            cvsm_db.setStatusDate(cvsm.getStatusDate());
        }
        if(cvsm.getIsStructureEntry() != null)
            cvsm_db.setIsStructureEntry(cvsm.getIsStructureEntry());
        if(cvsm.getOrderNr() != null)
            cvsm_db.setOrderNr(cvsm.getOrderNr());
        if(cvsm.getValueOverride() != null)
            cvsm_db.setValueOverride(cvsm.getValueOverride());
        if(cvsm.getDescription()!= null)
            cvsm_db.setDescription(cvsm.getDescription());
        if(cvsm.getMeaning()!= null)
            cvsm_db.setMeaning(cvsm.getMeaning());
        if(cvsm.getHints()!= null)
            cvsm_db.setHints(cvsm.getHints());
        
        hb_session.update(cvsm_db);
        
          LastChangeHelper.updateLastChangeDate(false, cvsm_db.getId().getValuesetVersionId(),hb_session);
          hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptValueSetMembership': " + e.getLocalizedMessage());

        logger.error("Fehler bei 'MaintainConceptValueSetMembership'-Hibernate: " + e.getLocalizedMessage());
        
        e.printStackTrace();
      }
      finally
      {
        hb_session.close();
      }
      if (true) {
            // Status an den Aufrufer weitergeben
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Status erfolgreich geändert.");
        }
      
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'MaintainConceptValueSetMembership': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'MaintainConceptValueSetMembership': " + e.getLocalizedMessage());
      
      e.printStackTrace();
    }
    return response;
  }

  private boolean validateParameter(MaintainConceptValueSetMembershipRequestType Request, MaintainConceptValueSetMembershipResponseType Response)
  {
    boolean erfolg = true;

    
    CodeSystemEntityVersion csev = Request.getCodeSystemEntityVersion();
    if(csev != null){

        if(csev.getConceptValueSetMemberships().size() > 1){

            Response.getReturnInfos().setMessage("Es darf nur genau ein ConceptValueSetMembership beinhaltet sein!");
            erfolg = false;
        }
    }
    else
    {
        Response.getReturnInfos().setMessage("CodeSystemEntity-Version darf nicht NULL sein!");
        erfolg = false;
    }
    
    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
