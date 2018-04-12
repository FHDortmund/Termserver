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

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) / warends
 */
public class ListValueSets
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListValueSetsResponseType ListValueSets(ListValueSetsRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== ListValueSets gestartet ======");
    }

    // Return-Informationen anlegen
    ListValueSetsResponseType response = new ListValueSetsResponseType();
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

      java.util.List<ValueSet> liste = null;

      try
      {
        String hql = "select distinct vs from ValueSet vs";
        hql += " join fetch vs.valueSetVersions vsv";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getValueSet() != null)
        {
          parameterHelper.addParameter("vs.", "name", parameter.getValueSet().getName());
          parameterHelper.addParameter("vs.", "description", parameter.getValueSet().getDescription());

          if (parameter.getValueSet().getValueSetVersions() != null && parameter.getValueSet().getValueSetVersions().size() > 0)
          {
            ValueSetVersion vsvFilter = (ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0];

            parameterHelper.addParameter("vsv.", "releaseDate", vsvFilter.getReleaseDate());
            parameterHelper.addParameter("vsv.", "statusDate", vsvFilter.getStatusDate());
            parameterHelper.addParameter("vsv.", "previousVersionId", vsvFilter.getPreviousVersionId());
            //parameterHelper.addParameter("vsv.", "status", vsvFilter.getStatus());
            parameterHelper.addParameter("vsv.", "validityRange", vsvFilter.getValidityRange());
            
            parameterHelper.addParameter("vsv.", "name", vsvFilter.getName());
            parameterHelper.addParameter("vsv.", "oid", vsvFilter.getOid());
          }
        }

        if (loggedIn == false)
        {
          parameterHelper.addParameter("vs.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
          parameterHelper.addParameter("vsv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

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
        response.getReturnInfos().setMessage("Fehler bei 'ListValueSets', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ListValueSets', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }

      int anzahl = 0;
      if (liste != null)
      {
        anzahl = liste.size();
        Iterator<ValueSet> itVS = liste.iterator();

        while (itVS.hasNext())
        {
          ValueSet vs = itVS.next();
          vs.setMetadataParameters(null);
//          vs.setValueSetVersions(null);
//          vs.setDescription(null);
//          vs.setStatus(null);
//          vs.setStatusDate(null);

          // ValueSetVersions
          if (vs.getValueSetVersions() != null)
          {
            Iterator<ValueSetVersion> itVSV = vs.getValueSetVersions().iterator();
            ValueSetVersion vsv;
            while (itVSV.hasNext())
            {
              vsv = itVSV.next();

              if (!loggedIn && vsv.getStatus() != null && vsv.getStatus().intValue() != Definitions.STATUS_CODES.ACTIVE.getCode())
              {
                // Nicht sichtbar, also von der Ergebnismenge entfernen
                itVSV.remove();
              }
              else
              {
                // Nicht anzuzeigende Beziehungen null setzen
                vsv.setValueSet(null);
                vsv.setConceptValueSetMemberships(null);
              }
            }

            if (!loggedIn)
            {
            }
            //parameterHelper.addParameter("vsv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
          }
        }

        // Liste der Response beifügen
        response.setValueSet(liste);
        response.getReturnInfos().setCount(liste.size());
      }

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("ValueSets erfolgreich gelesen, Anzahl: " + anzahl);
      response.getReturnInfos().setCount(anzahl);
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListValueSets': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ListValueSets': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private boolean validateParameter(ListValueSetsRequestType Request, ListValueSetsResponseType Response)
  {
    boolean erfolg = true;
    if (Request != null)
    {
      if (Request.getValueSet() != null && Request.getValueSet().getValueSetVersions() != null)
      {
        if (Request.getValueSet().getValueSetVersions().size() > 1)
        {
          Response.getReturnInfos().setMessage(
                  "Es darf maximal eine ValueSetVersion angegeben sein!");
          erfolg = false;

        }
        else
        {
          if (Request.getValueSet().getValueSetVersions().size() != 0)
          {
            ValueSetVersion vsv = (ValueSetVersion) Request.getValueSet().getValueSetVersions().toArray()[0];

            // folgende Parameter dürfen nicht angegeben sein:

            if (vsv.getVersionId() != null)
            {
              Response.getReturnInfos().setMessage(
                      "ValueSetVersion VersionId darf nicht angegeben sein!");
              erfolg = false;
            }

            if (vsv.getInsertTimestamp() != null)
            {
              Response.getReturnInfos().setMessage(
                      "ValueSetVersion InsertTimestamp darf nicht angegeben sein!");
              erfolg = false;
            }

            if (vsv.getPreferredLanguageCd() != null)
            {
              Response.getReturnInfos().setMessage(
                      "ValueSetVersion PreferredLanguageCd darf nicht angegeben sein!");
              erfolg = false;
            }
            
           

            

          }


          if (Request.getValueSet().getId() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ValueSet Id darf nicht angegeben sein!");
            erfolg = false;
          }

          if (Request.getValueSet().getCurrentVersionId() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ValueSet CurrentVersionId darf nicht angegeben sein!");
            erfolg = false;
          }

          if (Request.getValueSet().getStatus() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ValueSet Status darf nicht angegeben sein!");
            erfolg = false;
          }

          if (Request.getValueSet().getStatusDate() != null)
          {
            Response.getReturnInfos().setMessage(
                    "ValueSet StatusDate darf nicht angegeben sein!");
            erfolg = false;
          }

        }
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