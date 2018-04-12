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
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Mathias Aschhoff
 */
public class UpdateValueSetStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateValueSetStatusResponseType updateValueSetStatus(UpdateValueSetStatusRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== UpdateValueSetStatus gestartet ======");
    }

    UpdateValueSetStatusResponseType response = new UpdateValueSetStatusResponseType();
    response.setReturnInfos(new ReturnType());

    //Parameter prüfen
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

    try
    {

      //VSet VSetVersion
      ValueSet vs = parameter.getValueSet();
      ValueSetVersion vsv = null;

      if (vs.getValueSetVersions() != null && vs.getValueSetVersions().size() > 0)
        vsv = (ValueSetVersion) vs.getValueSetVersions().toArray()[0];

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        boolean changedVSStatus = false;
        // VS und VSV aus DB auslesen, Status ändern und wieder speichern
        
        ValueSet vs_db = null;
        
        if(vs.getId() != null){
           vs_db = (ValueSet) hb_session.get(ValueSet.class, vs.getId());

            if (vs.getStatus() != null)
            {
              vs_db.setStatus(vs.getStatus());
              hb_session.update(vs_db);
            }
        }else{
        
            String hql = "select distinct vsv from ValueSetVersion vsv join fetch vsv.valueSet vs WHERE vsv.versionId=" + vsv.getVersionId();
            org.hibernate.Query q = hb_session.createQuery(hql);
            List vsv_list = q.list();
            if(!vsv_list.isEmpty()){
                vs_db = ((ValueSetVersion)vsv_list.get(0)).getValueSet();
            }
        }
        
        if (vsv != null)
        {
          ValueSetVersion vsv_db = (ValueSetVersion) hb_session.get(ValueSetVersion.class, vsv.getVersionId());
          
          if(vsv.getStatus() != null){
            vsv_db.setStatus(vsv.getStatus());
            hb_session.update(vsv_db);
          }
          ChangePreviousVersionStatus(vsv_db, hb_session, vsv.getStatus());

          // prüfen, ob ValueSet-Version die letzte Version ist, dann auch VS-Status ändern (!)
          String hql = "select distinct vsv from ValueSetVersion vsv where vsv.previousVersionId=" + vsv_db.getVersionId();
          org.hibernate.Query q = hb_session.createQuery(hql);
          List vsv_list = q.list();
          if (vsv_list == null || vsv_list.size() == 0)
          {
            // ValueSetVersion ist letzte Version, jetzt also auch ValueSet-Status ändern
            vs_db.setStatus(vsv.getStatus());
            hb_session.update(vs_db);
            changedVSStatus = true;
          }
          LastChangeHelper.updateLastChangeDate(false, vsv_db.getVersionId(),hb_session);
        }
        hb_session.getTransaction().commit();

        response.getReturnInfos().setCount(1);
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        
        response.getReturnInfos().setMessage("Status erfolgreich aktualisiert.");
        if(changedVSStatus)
          response.getReturnInfos().setMessage(response.getReturnInfos().getMessage() + "\nStatus der ValueSet-Version wurde ebenfalls aktualisiert, da alle ValueSet-Versionen von der Änderung betroffen waren.");
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateValueSetStatus': " + e.getLocalizedMessage());

        logger.error("Fehler bei 'UpdateValueSetStatus' a: " + e.getLocalizedMessage());
        e.printStackTrace();
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
      response.getReturnInfos().setMessage("Fehler bei 'UpdateValueSetStatus': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'UpdateValueSetStatus': b" + e.getLocalizedMessage());
    }

    // Alles OK
    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
    response.getReturnInfos().setStatus(ReturnType.Status.OK);
    response.getReturnInfos().setMessage("Status erfolgreich geändert");
    
    return response;
  }

  private void ChangePreviousVersionStatus(ValueSetVersion vsv, org.hibernate.Session hb_session, int new_status)
  {
    if (vsv != null && vsv.getPreviousVersionId() != null && vsv.getPreviousVersionId() > 0)
    {
      ValueSetVersion vsv_db = (ValueSetVersion) hb_session.get(ValueSetVersion.class, vsv.getPreviousVersionId());
      vsv_db.setStatus(new_status);
      hb_session.update(vsv_db);

      ChangePreviousVersionStatus(vsv_db, hb_session, new_status);
    }
  }

  private boolean validateParameter(UpdateValueSetStatusRequestType Request, UpdateValueSetStatusResponseType Response)
  {
    boolean erfolg = true;

    ValueSet vs = Request.getValueSet();
    if (vs == null)
    {
      Response.getReturnInfos().setMessage("ValueSet darf nicht NULL sein!");
      erfolg = false;
    }

    Set<ValueSetVersion> vsvSet = vs.getValueSetVersions();
    if (vsvSet.size() > 1)
    {
      Response.getReturnInfos().setMessage(
              "Die ValueSet-Version-Liste darf maximal einen Eintrag haben!");
      erfolg = false;
    }
    else if (vsvSet.size() == 1)
    {
      ValueSetVersion vsv = (ValueSetVersion) vs.getValueSetVersions().toArray()[0];

      if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss eine ID für die ValueSet-Version angegeben werden!");
        erfolg = false;
      }

      if (vsv.getStatus() == null)
      {
        Response.getReturnInfos().setMessage(
                "Es muss ein Status für die ValueSet-Version angegeben werden!");
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
