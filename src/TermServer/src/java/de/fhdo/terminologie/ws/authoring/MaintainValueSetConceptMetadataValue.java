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
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Philipp Urbauer
 */
public class MaintainValueSetConceptMetadataValue
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public MaintainValueSetConceptMetadataValueResponseType MaintainValueSetConceptMetadataValue(MaintainValueSetConceptMetadataValueRequestType parameter, String ipAddress)
  {

    return MaintainValueSetConceptMetadataValue(parameter, null, ipAddress);
  }

  public MaintainValueSetConceptMetadataValueResponseType MaintainValueSetConceptMetadataValue(MaintainValueSetConceptMetadataValueRequestType parameter, org.hibernate.Session session, String ipAddress)
  {

    if (logger.isInfoEnabled())
      logger.info("====== MaintainValueSetConceptMetadataValue gestartet ======");

    boolean createHibernateSession = (session == null);
    logger.debug("createHibernateSession: " + createHibernateSession);

    // Return-Informationen anlegen
    MaintainValueSetConceptMetadataValueResponseType response = new MaintainValueSetConceptMetadataValueResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      logger.debug("Parameter falsch");
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
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = null;
      org.hibernate.Transaction tx = null;

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
      }
      else
      {
        hb_session = session;
        //hb_session.getTransaction().begin();
      }

      // Die Rückgabevariablen erzeugen
      List<ValueSetMetadataValue> valueSetMetadataValues = null;

      // Zum Speichern vorbereiten          
      List<ValueSetMetadataValue> vsmvList = parameter.getValueSetMetadataValues();  // bereits geprüft, ob null  

      try
      {
        if (!vsmvList.isEmpty())
        {
          Iterator<ValueSetMetadataValue> iter = vsmvList.iterator();
          ValueSetMetadataValue vsmv = null;
          while (iter.hasNext())
          {

            vsmv = (ValueSetMetadataValue) iter.next();
            hb_session.update(vsmv);
          }

          if (vsmv != null)
          {
                    // Metadata-Values auslesen (performanter)
            // ohne Abfrage würde für jede Metadata-Value 1 Abfrage ausgeführt,
            // so wie jetzt wird 1 Abfrage pro Entity-Version ausgeführt
            String hql = "select distinct vsmv from ValueSetMetadataValue vsmv";
            hql += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            //parameterHelper.addParameter("vsmv.", "codeSystemEntityVersionId", vsmv.getCodeSystemEntityVersionId());
            parameterHelper.addParameter("csev.", "versionId", vsmv.getCodeSystemEntityVersion().getVersionId());
            parameterHelper.addParameter("vsmv.", "valuesetVersionId", vsmv.getValuesetVersionId());

            // Parameter hinzufügen (immer mit AND verbunden)
            hql += parameterHelper.getWhere("");
            logger.debug("HQL: " + hql);

            // Query erstellen
            org.hibernate.Query q = hb_session.createQuery(hql);

            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
            parameterHelper.applyParameter(q);

            valueSetMetadataValues = q.list();

            LastChangeHelper.updateLastChangeDate(false, vsmv.getValuesetVersionId(), hb_session);

            if (createHibernateSession)
              hb_session.getTransaction().commit();

            if (valueSetMetadataValues != null)
            {
              Iterator<ValueSetMetadataValue> itMV = valueSetMetadataValues.iterator();

              while (itMV.hasNext())
              {
                ValueSetMetadataValue mValue = itMV.next();

                if (mValue.getMetadataParameter() != null)
                {
                  mValue.getMetadataParameter().setCodeSystemMetadataValues(null);
                  mValue.getMetadataParameter().setValueSetMetadataValues(null);
                  mValue.getMetadataParameter().setCodeSystem(null);
                  mValue.getMetadataParameter().setValueSet(null);
                }
              }
            }
            // Liste der Response beifügen
            response.setValueSetMetadataValues(valueSetMetadataValues);
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("ValueSetConceptMetadataValue erfolgreich bearbeitet");
          }
        }
        else
        {

          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Es gibt keine ValueSetMetadataValues welche geändert werden können");
        }
      }
      catch (Exception e)
      {
        if (createHibernateSession)
          hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'MaintainValueSetConceptMetadataValue', Hibernate: " + e.getLocalizedMessage());
        logger.error(response.getReturnInfos().getMessage());
        e.printStackTrace();
      }
      finally
      {
        if (createHibernateSession)
          hb_session.close();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'MaintainValueSetConceptMetadataValue': " + e.getLocalizedMessage());// + " ; Mögliche Ursache: Verwendete Id(" + Long.toString(parameter.getValueSet().getId()) +  ") nicht in Datenbank.");           
      logger.error(response.getReturnInfos().getMessage());
      e.printStackTrace();
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
  private boolean validateParameter(MaintainValueSetConceptMetadataValueRequestType Request, MaintainValueSetConceptMetadataValueResponseType Response)
  {
    boolean erfolg = true;

    List<ValueSetMetadataValue> valueSetMetadataValues = Request.getValueSetMetadataValues();
    if (valueSetMetadataValues == null || valueSetMetadataValues.isEmpty())
    {
      Response.getReturnInfos().setMessage("ValueSetMetadataValues darf nicht NULL sein!");
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
