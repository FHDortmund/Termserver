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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) / warends
 */
public class ListMetadataParameter
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListMetadataParameterResponseType ListMetadataParameter(ListMetadataParameterRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== ListMetadataParameter gestartet ======");
    }

    // Return-Informationen anlegen
    ListMetadataParameterResponseType response = new ListMetadataParameterResponseType();
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

      java.util.List<MetadataParameter> liste = null;

      try
      {
        String hql = "select distinct mp from MetadataParameter mp"
                + " left join fetch mp.codeSystem cs"
                + " left join fetch mp.valueSet vs";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        liste = q.list();
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListMetadataParameters', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ListMetadataParameter', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

      int anzahl = 0;
      if (liste != null)
      {
        anzahl = liste.size();
        Iterator<MetadataParameter> itMP = liste.iterator();

        while (itMP.hasNext())
        {
          MetadataParameter mp = itMP.next();

          /*if (mp.getCodeSystemMetadataValues() != null)
          {
            mp.setCodeSystemMetadataValues(null);
            mp.setCodeSystem(null);
          }
          if (mp.getValueSetMetadataValues() != null)
          {
            mp.setValueSetMetadataValues(null);
            mp.setValueSet(null);
          }*/
          /*if (mp.getCodeSystemMetadataValues() != null)
          {
            mp.setCodeSystemMetadataValues(null);
            mp.setCodeSystem(null);
          }
          if (mp.getValueSetMetadataValues() != null)
          {
            mp.setValueSetMetadataValues(null);
            mp.setValueSet(null);
          }*/
          mp.setCodeSystemMetadataValues(null);
          mp.setValueSetMetadataValues(null);
          
          if(mp.getCodeSystem() != null)
          {
            mp.getCodeSystem().setMetadataParameters(null);
            mp.getCodeSystem().setCodeSystemVersions(null);
            mp.getCodeSystem().setDomainValues(null);
          }
          
          if(mp.getValueSet() != null)
          {
            mp.getValueSet().setMetadataParameters(null);
            mp.getValueSet().setValueSetVersions(null);
          }
        }

        // Liste der Response beifügen
        response.setMetadataParameter(liste);
        response.getReturnInfos().setCount(liste.size());
      }

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("MetadataParameter erfolgreich gelesen, Anzahl: " + anzahl);
      response.getReturnInfos().setCount(anzahl);
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListMetadataParameter': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ListMetadataParameter': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private boolean validateParameter(ListMetadataParameterRequestType Request, ListMetadataParameterResponseType Response)
  {
    boolean erfolg = true;
    

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
