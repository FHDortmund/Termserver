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
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetConceptMetadataResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ReturnVsConceptMetadata
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ReturnValueSetConceptMetadataResponseType ReturnVsConceptMetadata(ReturnValueSetConceptMetadataRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ReturnVsConceptMetadata gestartet ======");

    // Return-Informationen anlegen
    ReturnValueSetConceptMetadataResponseType response = new ReturnValueSetConceptMetadataResponseType();
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

    List<ValueSetMetadataValue> valueSetMetadataValues = null;
    // Hibernate-Block, Session öffnen
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      try
      {

            // Metadata-Values auslesen (performanter)
        // ohne Abfrage würde für jede Metadata-Value 1 Abfrage ausgeführt,
        // so wie jetzt wird 1 Abfrage pro Entity-Version ausgeführt
        String hql = "select distinct vsmv from ValueSetMetadataValue vsmv";
        hql += " join fetch vsmv.metadataParameter mp";
        hql += " join vsmv.codeSystemEntityVersion csev";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("csev.", "versionId", parameter.getCodeSystemEntityVersionId());
        parameterHelper.addParameter("vsmv.", "valuesetVersionId", parameter.getValuesetVersionId());

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        valueSetMetadataValues = q.list();

        if (valueSetMetadataValues != null)
        {
          Iterator<ValueSetMetadataValue> itMV = valueSetMetadataValues.iterator();

          while (itMV.hasNext())
          {
            ValueSetMetadataValue mValue = itMV.next();

            if (mValue.getMetadataParameter() != null)
            {
              mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
              mValue.getMetadataParameter().setCodeSystem(null);
              mValue.getMetadataParameter().setValueSet(null);
              mValue.getMetadataParameter().setValueSetMetadataValues(null);
            }
          }
        }

        // Liste der Response beifügen
        List<ValueSetMetadataValue> l = new ArrayList<ValueSetMetadataValue>();
        for (ValueSetMetadataValue ent : valueSetMetadataValues)
        {
          ValueSetMetadataValue v = new ValueSetMetadataValue();
          v.setId(ent.getId());
          v.setParameterValue(ent.getParameterValue());
          v.setValuesetVersionId(ent.getValuesetVersionId());

          v.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
          v.getCodeSystemEntityVersion().setVersionId(ent.getCodeSystemEntityVersion().getVersionId());

          v.setMetadataParameter(new MetadataParameter());
          v.getMetadataParameter().setId(ent.getMetadataParameter().getId());
          v.getMetadataParameter().setMetadataParameterType(ent.getMetadataParameter().getParamDatatype());
          v.getMetadataParameter().setParamDatatype(ent.getMetadataParameter().getParamDatatype());
          v.getMetadataParameter().setParamName(ent.getMetadataParameter().getParamName());

          l.add(v);
        }
        response.setValueSetMetadataValue(l);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
            //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ReturnVsConceptMetadata', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'ReturnVsConceptMetadata', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        hb_session.close();
      }

      if (valueSetMetadataValues.isEmpty())
      {
        response.getReturnInfos().setMessage("Zu dem angegebenen ValueSet-Concept wurden kein Metadaten gefunden!");
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      }
      else
      {
        response.getReturnInfos().setCount(1);
        response.getReturnInfos().setMessage("ValueSet-Concept Metadaten erfolgreich gelesen");
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
      }

      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ReturnVsConceptMetadata': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'ReturnVsConceptMetadata': " + e.getLocalizedMessage());
    }

    return response;
  }

  private boolean validateParameter(ReturnValueSetConceptMetadataRequestType Request, ReturnValueSetConceptMetadataResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getCodeSystemEntityVersionId() == null)
    {
      Response.getReturnInfos().setMessage(
              "CodeSystemEntityVersionId darf nicht null sein!");
      erfolg = false;
    }
    if (Request.getValuesetVersionId() == null)
    {
      Response.getReturnInfos().setMessage(
              "ValuesetVersionId darf nicht null sein!");
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
