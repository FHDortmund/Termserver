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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LoginHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  // Singleton-Muster
  private static LoginHelper instance;

  /**
   * @return the instance
   */
  public static LoginHelper getInstance()
  {
    if (instance == null)
      instance = new LoginHelper();
    return instance;
  }
  private HashMap<String, LoginInfoType> userMap;

  public LoginHelper()
  {
    userMap = new HashMap<String, LoginInfoType>();
  }

  /*public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired)
  {
    return doLogin(login, returnType, loginRequired, null);
  }

  public boolean doLogin(LoginType login, ReturnType returnType, boolean loginRequired, org.hibernate.Session hb_session)
  {
    boolean loggedIn = false;

    if (login != null && returnType != null)
    {
      LoginInfoType loginInfoType = null;
      loginInfoType = LoginHelper.getInstance().getLoginInfos(login, hb_session);
      loggedIn = loginInfoType != null;

      if (logger.isDebugEnabled())
        logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    }

    // Statusmeldung
    if (loggedIn == false && loginRequired)
    {
      returnType.setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      returnType.setStatus(ReturnType.Status.OK);
      returnType.setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
    }
    return loggedIn;
  }*/

  /*public LoginInfoType getLoginInfos(LoginType Login)
  {
    return getLoginInfos(Login, null);
  }*/

  /**
   * Überprüft anhand der Session-ID, ob der Benutzer angemeldet ist und ob die
   * Session noch gültig ist-
   *
   * @param Login LoginType mit der Session-ID
   * @return LoginInfoType bei Erfolg, sonst null
   */
  /*public LoginInfoType getLoginInfos(LoginType Login, org.hibernate.Session session)
  {
    LoginInfoType loginInfoType = null;

    boolean createHibernateSession = (session == null);

    if (Login == null || Login.getSessionID() == null || Login.getSessionID().length() == 0)
    {
      logger.debug("Keine Session-ID angegeben!");
      return null;
    }
    
    if (logger.isDebugEnabled())
    {
      logger.debug("Überprüfe Session mit Session-ID: " + Login.getSessionID());
      logger.debug("createHibernateSession: " + createHibernateSession);
    }

    long session_timeout = 30 * 60000; // 30 Minuten, TODO aus DB lesen

    // Map vewenden (damit nicht für jede Aktion ein Datenbankaufruf stattfindet)
    if (userMap.containsKey(Login.getSessionID()))
    {
      //LoginInfoType sessionInfo = userMap.get(Login.getSessionID());
      loginInfoType = userMap.get(Login.getSessionID());

      // IP überprüfen (nur, falls angegeben)
      //logger.debug("List-IP: " + loginInfoType.getLastIP());
//      if (Login.getIp() != null && Login.getIp().length() > 0)
//      {
//        if (!(loginInfoType.getLastIP() != null
//                && loginInfoType.getLastIP().equals(Login.getIp())))
//        {
//          // IP stimmt nicht überein
//          if (logger.isDebugEnabled())
//            logger.debug("IP stimmt nicht überein (" + Login.getIp() + ")!");
//
//          return null;
//        }
//      }

      // Timeout überprüfen
      long now = new java.util.Date().getTime();
      long timestamp = loginInfoType.getLastTimestamp().getTime();

      if (now - session_timeout < timestamp)
      {
        // OK
        // Zeitstempel aktualisieren
        loginInfoType.setLastTimestamp(new Date());
        userMap.put(Login.getSessionID(), loginInfoType);
        
        // Logintype zurückgeben (alles in Ordnung)
        return loginInfoType;
      }
      else
      {
        // Timestamp abgelaufen
        if (logger.isDebugEnabled())
        {
          logger.debug("Zeitstempel abgelaufen! (wird im Debug-Modus ignoriert)");

          //TODO: Das hier kann später weider raus; dient nur dazu, dass im DebugModus der Zeitstempel nicht ablaufen kann
          // Logintype zurückgeben (Nur der Zeitstempel ist abgelaufen, das ist aber egal im DebugModus)        
          // Zeitstempel aktualisieren
          loginInfoType.setLastTimestamp(new Date());
          userMap.put(Login.getSessionID(), loginInfoType);

          // Logintype zurückgeben (alles in Ordnung)
          return loginInfoType;
        }
        else
        {
          return null;
        }
      }
    }
    else
      logger.debug("Session-ID nicht in userMap vorhanden: " + Login.getSessionID());

    // Session aus DB lesen und in Map speichern
    // Hibernate-Block, Session öffnen
    //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //org.hibernate.Transaction tx = hb_session.beginTransaction();
    org.hibernate.Session hb_session = null;

    if (createHibernateSession)
    {
      hb_session = HibernateUtil.getSessionFactory().openSession();
    }
    else
    {
      hb_session = session;
    }

    try
    {
      // TODO CONHIT !!! danach wieder rausnehmen
      String hql = "select distinct tu from TermUser tu where tu.name=:name";

      //HQLParameterHelper parameterHelper = new HQLParameterHelper();
      //parameterHelper.addParameter("tu.", "name", Login.getSessionID());

        // TODO Session-Timeout überprüfen
      // Parameter hinzufügen (immer mit AND verbunden)
      //hql += parameterHelper.getWhere("");

      logger.debug("HQL: " + hql);
      
      // Query erstellen
      org.hibernate.Query q = hb_session.createQuery(hql);
      
      q.setString("name", Login.getSessionID());

      // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
      //parameterHelper.applyParameter(q);

      List<TermUser> liste = q.list();

      if (liste != null && liste.size() > 0)
      {
        logger.debug("Session existiert!");

        TermUser s_user = liste.get(0);

        // Antwort erstellen
        loginInfoType = new LoginInfoType();
        loginInfoType.setLastTimestamp(new Date());
        loginInfoType.setLastIP("");
        loginInfoType.setTermUser(s_user);
        loginInfoType.getTermUser().setIsAdmin(s_user.isIsAdmin());
        loginInfoType.setLogin(new LoginType());
        loginInfoType.getLogin().setUsername(s_user.getName());
        loginInfoType.getLogin().setSessionID(s_user.getName());

        // in Map speichern
        userMap.put(Login.getSessionID(), loginInfoType);
        
        logger.debug("Füge '" + Login.getSessionID() + "' zu userMap hinzu!");
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      logger.error("Fehler bei 'getLoginInfos', Hibernate: " + e.getLocalizedMessage());
    }
    finally
    {
      if (createHibernateSession)
      {
        logger.debug("Schließe Hibernate-Session (LoginHelper.java)");
        hb_session.close();
      }
    }

    return loginInfoType;
  }*/

  /*public boolean isUserPermitted(LoginType Login)
  {
    LoginInfoType loginInfo = getLoginInfos(Login);

    return loginInfo != null;
  }*/
}
