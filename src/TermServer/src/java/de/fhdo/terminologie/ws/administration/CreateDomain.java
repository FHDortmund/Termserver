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
import de.fhdo.terminologie.ws.administration.types.CreateDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.CreateDomainResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Session;

/**
 *
 * @author Bernhard Rimatzki
 * 2014-07-22: edited by Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CreateDomain
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * Erstellt eine neue Domäne mit den angegebenen Parametern
   *
   * @param parameter
   * @return Antwort des Webservices
   */
  public CreateDomainResponseType CreateDomain(CreateDomainRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== CreateDomain gestartet ======");
    }

    // Return-Informationen anlegen
    CreateDomainResponseType response = new CreateDomainResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (!validateParameter(parameter, response))
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
      Domain d_return = new Domain();

      // Hibernate-Block, Session öffnen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      // Domain und DomainValue zum Speichern vorbereiten
      Domain d = parameter.getDomain();
      Set<DomainValue> dv = parameter.getDomain().getDomainValues();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // Domain in der Datenbank speichern
        d.setDomainValues(null);

        hb_session.save(d);

        Iterator<DomainValue> idv = dv.iterator();
        while (idv.hasNext())
        {
          DomainValue dvItem = idv.next();

          dvItem.setDomain(new Domain());
          dvItem.getDomain().setDomainId(d.getDomainId());

          hb_session.save(dvItem);
        }

        // Antwort setzen (neue ID)
        d_return.setDomainId(d.getDomainId());
        response.setDomain(d_return);

      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        String message = "Fehler bei 'CreateDomain', Hibernate: " + e.getLocalizedMessage();
        response.getReturnInfos().setMessage(message);

        logger.error(message);
      }
      finally
      {
        // Transaktion abschließen
        if (d_return.getDomainId() > 0)
        {
          hb_session.getTransaction().commit();

        }
        else
        {
          // Änderungen nicht erfolgreich
          logger.warn("[CreateDomain.java] Änderungen nicht erfolgreich, d_return.id: "
                  + d_return.getDomainId());

          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateDomain': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'CreateDomain': " + e.getLocalizedMessage());
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
  private boolean validateParameter(CreateDomainRequestType Request,
          CreateDomainResponseType Response)
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
      if (domain.getDomainName() == null || domain.getDomainName().length() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss ein Name für die Domain angegeben sein!");
        erfolg = false;
      }

      Set<DomainValue> dvSet = domain.getDomainValues();
      if (dvSet != null)
      {
        Iterator idv = dvSet.iterator();
        while (idv.hasNext())
        {
          DomainValue dv = (DomainValue) idv.next();
          if (dv.getDomainCode() == null || dv.getDomainCode().length() == 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss ein Code für den Domain-Value angegeben sein!");
            erfolg = false;
          }
          if (dv.getDomainDisplay() != null && dv.getDomainDisplay().length() == 0)
          {
            Response.getReturnInfos().setMessage(
                    "Es muss ein Displayname für den Domain-Value angegeben sein!");
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
