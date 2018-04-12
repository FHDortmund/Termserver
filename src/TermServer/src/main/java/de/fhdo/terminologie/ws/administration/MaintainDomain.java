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
package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Bernhard Rimatzki
 */
public class MaintainDomain
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Verändert ein bestehende Domäne mit den angegebenen Parametern
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public MaintainDomainResponseType MaintainDomain(MaintainDomainRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== MaintainDomain gestartet ======");
    }

    // Return-Informationen anlegen
    MaintainDomainResponseType response = new MaintainDomainResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (!validateParameter(parameter, response))
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

    if (logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    if (loggedIn == false)
    {
      // Benutzer muss für diesen Webservice eingeloggt sein
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
      return response;
    }

    try
    {
      // Hibernate-Block, Session öffnen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      // Domain und DomainValue zum Speichern vorbereiten
      Domain d = parameter.getDomain();
      Set<DomainValue> dv = parameter.getDomain().getDomainValues();

      String warnString = "d.id: ";
      Long Id = d.getDomainId();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        //Anpassen der veränderten Attribute
        Domain d_db = (Domain) hb_session.get(Domain.class, d.getDomainId());
        if (d.getDomainName() != null && d.getDomainName().length() > 0)
          d_db.setDomainName(d.getDomainName());
        if (d.getDisplayText() != null && d.getDisplayText().length() > 0)
          d_db.setDisplayText(d.getDisplayText());
        if (d.getDomainOid() != null && d.getDomainOid().length() > 0)
          d_db.setDomainOid(d.getDomainOid());
        if (d.getDescription() != null && d.getDescription().length() > 0)
          d_db.setDescription(d.getDescription());
        if (d.getIsOptional() != null)
          d_db.setIsOptional(d.getIsOptional());
        if (d.getDefaultValue() != null && d.getDefaultValue().length() > 0)
          d_db.setDefaultValue(d.getDefaultValue());
        if (d.getDomainType() != null && d.getDomainType().length() > 0)
          d_db.setDomainType(d.getDomainType());
        if (d.getDisplayOrder() != null)
          d_db.setDisplayOrder(d.getDisplayOrder());

        // Domain in der Datenbank updaten
        hb_session.update(d_db);

        //TODO was soll geschehen, wenn sich die Values in einer Domäne ändern. sprich: wenn values rausfallen bzw hinzukommen
        Iterator<DomainValue> idv = dv.iterator();
        warnString = "dv.id: ";
        while (idv.hasNext())
        {
          DomainValue dvItem = idv.next();
          Id = dvItem.getDomainValueId();

          //Anpassen der veränderten Attribute
          DomainValue dv_db = (DomainValue) hb_session.get(DomainValue.class, dvItem.getDomainValueId());
          if (dvItem.getDomainCode() != null && dvItem.getDomainCode().length() > 0)
            dv_db.setDomainCode(dvItem.getDomainCode());
          if (dvItem.getDomainDisplay() != null && dvItem.getDomainDisplay().length() > 0)
            dv_db.setDomainDisplay(dvItem.getDomainDisplay());
          if (dvItem.getAttribut1classname() != null && dvItem.getAttribut1classname().length() > 0)
            dv_db.setAttribut1classname(dvItem.getAttribut1classname());
          if (dvItem.getAttribut1value() != null && dvItem.getAttribut1value().length() > 0)
            dv_db.setAttribut1value(dvItem.getAttribut1value());
          if (dvItem.getOrderNo() != null)
            dv_db.setOrderNo(dvItem.getOrderNo());
          if (dvItem.getImageFile() != null && dvItem.getImageFile().length() > 0)
            dv_db.setImageFile(dvItem.getImageFile());

          // DomainValue in der Datenbank updaten
          hb_session.update(dv_db);
        }

        hb_session.getTransaction().commit();

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setCount(1);
        String message = "Domain successfully edited";
        response.getReturnInfos().setMessage(message);
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        String message = "Fehler bei 'MaintainDomain', Hibernate: " + e.getLocalizedMessage();
        response.getReturnInfos().setMessage(message);

        logger.error(message);
        // Änderungen nicht erfolgreich
        logger.warn("[MaintainDomain.java] Änderungen nicht erfolgreich, " + warnString + "" + Id);

        hb_session.getTransaction().rollback();
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
      response.getReturnInfos().setMessage("Fehler bei 'MaintainDomain': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'MaintainDomain': " + e.getLocalizedMessage());
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
  private boolean validateParameter(MaintainDomainRequestType Request, MaintainDomainResponseType Response)
  {
    boolean erfolg = true;

    Domain domain = Request.getDomain();
    if (domain == null)
    {
      Response.getReturnInfos().setMessage("Domain darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      // Hibernate-Block, Session öffnen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();
      //Ist die mitgegebene ID leer?
      if (domain.getDomainId() == null || domain.getDomainId() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss eine ID für die Domain angegeben sein!");
        erfolg = false;
      }
      //Ist die mitgegebene ID in der Datenhaltung vorhanden?
      else if (hb_session.get(Domain.class, domain.getDomainId()) == null)
      {
        Response.getReturnInfos().setMessage(
                "Die angegebene ID existiert für die Domain nicht!");
        erfolg = false;
      }

      Set<DomainValue> dvSet = domain.getDomainValues();
      if (dvSet != null)
      {
        Iterator idv = dvSet.iterator();
        while (idv.hasNext())
        {
          DomainValue dvItem = (DomainValue) idv.next();
          //Ist die mitgegebene ID leer?
          if (dvItem.getDomainValueId() == null || dvItem.getDomainValueId() == 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss eine ID für den Domain-Value angegeben sein!");
            erfolg = false;
          }
          //Ist die mitgegebene ID in der Datenhaltung vorhanden?
          else if (hb_session.get(DomainValue.class, dvItem.getDomainValueId()) == null)
          {
            Response.getReturnInfos().setMessage(
                    "Die angegebene ID existiert für den Domain-Value nicht!");
            erfolg = false;
          }
          //Gehört die mitgegebene ID des Values überhaupt zu der Domain?
          else if (((DomainValue) hb_session.get(DomainValue.class, dvItem.getDomainValueId())).getDomain().getDomainId() != domain.getDomainId())
          {
            Response.getReturnInfos().setMessage(
                    "Das angegebene Domain-Value gehört nicht zu der Domain");
            erfolg = false;
          }
        }
      }
      //hb_session.getTransaction().commit();
      hb_session.close();
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;

  }
}
