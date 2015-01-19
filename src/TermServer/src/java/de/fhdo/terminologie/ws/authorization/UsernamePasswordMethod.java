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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.LicencedUser;
import de.fhdo.terminologie.db.hibernate.Session;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.Password;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.authorization.types.ChangePasswordResponseType;
import de.fhdo.terminologie.ws.authorization.types.LoginResponseType;
import de.fhdo.terminologie.ws.authorization.types.LogoutResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class UsernamePasswordMethod implements IAuthorization
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static final int ITERATION_COUNT = 500;

  /**
   * Login.
   *
   * @param IP
   * @param parameterList (1) Username, (2) Password (MD5 hash)
   * @return (1) Session-ID
   */
  public LoginResponseType Login(String IP, List<String> parameterList)
  {
    if (logger.isInfoEnabled())
      logger.info("====== UsernamePasswordMethod - Login started ======");

    // create return infos
    LoginResponseType response = new LoginResponseType();
    response.setReturnInfos(new ReturnType());

    // check parameters
    if (validateLoginParameter(parameterList, response) == false)
    {
      return response; // error, so return
    }

    response.setParameterList(new LinkedList<String>());

    try
    {
      java.util.List<TermUser> list = null;

      // hibernate block, open session
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try // 2. try-catch-block to catch hibernate errors
      {
        // Security.checkForDeadSessions(hb_session);

        // create HQL
        String hql = "select u from TermUser u";

        // use hql parameter helper to set where part
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        logger.debug("parameterList: " + parameterList);
        if (parameterList != null)
          logger.debug("parameterList size: " + parameterList.size());

        boolean requireAdmin = false;
        String username = "";
        String password_hash = "";
        if (parameterList != null && parameterList.size() >= 2)
        {
          username = parameterList.get(0);
          password_hash = parameterList.get(1);

          parameterHelper.addParameter("u.", "name", username);
          //parameterHelper.addParameter("u.", "passw", password_hash);

          if (parameterList.size() > 2)
          {
            requireAdmin = Boolean.parseBoolean(parameterList.get(2));
            if (requireAdmin)
              parameterHelper.addParameter("u.", "isAdmin", true);
          }
        }

        logger.debug("username: " + username);
        logger.debug("password_hash: " + password_hash);

        // add parameter with "and"
        hql += parameterHelper.getWhere("");

        // create query
        org.hibernate.Query q = hb_session.createQuery(hql);

        // set parameter in query
        parameterHelper.applyParameter(q);

        // do database query
        list = (java.util.List<TermUser>) q.list();

        boolean loginCorrect = false;

        if (username.length() > 0 && password_hash.length() > 0
                && list != null && list.size() > 0
                && performLogin(list.get(0), IP, username, hb_session, response))
        {
          TermUser termUser = list.get(0);
          String salt = termUser.getSalt();

          if (salt == null || salt.length() == 0)
          {
            // kein Salt vorhanden
            logger.warn("no salt value available for user");
            loginCorrect = false;
          }
          else
          {
            // Passwort prüfen (Hash + Salt)
            String passwordSalted = Password.getSaltedPassword(password_hash, salt, username, ITERATION_COUNT);
            loginCorrect = passwordSalted.equals(termUser.getPassw());
          }
        }

        if (loginCorrect)
        {
          // Login successful
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Login successful");

          //hb_session.getTransaction().commit();
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Username or password wrong");
        }
        // close transaction and commit changes (session-infos changed)
        tx.commit();
      }
      catch (Exception e)
      {
        tx.rollback();

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at 'Login', Hibernate: " + e.getLocalizedMessage());

        logger.error("Error at 'Login', Hibernate: " + e.getLocalizedMessage());
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }
    }
    catch (Exception e)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Error at 'Login': " + e.getLocalizedMessage());

      LoggingOutput.outputException(e, this);
    }

    return response;
  }

  private boolean performLogin(TermUser termUser_db, String ip, String username,
          org.hibernate.Session hb_session, LoginResponseType response)
  {
    boolean erfolg = true;

    // nun Hashwert generieren und in Tabelle mit Verbindung der UserID
    // speichern
    String newHash = "";

    UUID uuid = UUID.randomUUID();
    newHash = uuid.toString();

    // prüfen, ob bereits eine Session für den User existiert
    List<Session> sessionSet = Security.checkForExistingSessions(hb_session, termUser_db);

    if (sessionSet != null && !sessionSet.isEmpty())
    {
      for (Session session : sessionSet)
      {
        session.setTermUser(null);
        hb_session.delete(session);
      }
    }

    // Neue Session hinzufügen
    Session newSession = new Session();
    newSession.setSessionId(newHash);
    newSession.setLastTimestamp(new java.util.Date());
    newSession.setTermUser(new TermUser());
    newSession.getTermUser().setId(termUser_db.getId());
    newSession.setIpAddress(ip);

    logger.debug("IP-Adress (session): " + newSession.getIpAddress());

    hb_session.save(newSession);

    response.getParameterList().add(newHash);
    response.getParameterList().add(termUser_db.getId().toString());

    return erfolg;
  }

  public LogoutResponseType Logout(String IP, List<String> parameterList)
  {
    if (logger.isInfoEnabled())
      logger.info("====== Logout gestartet ======");

    // create return information
    LogoutResponseType response = new LogoutResponseType();
    response.setReturnInfos(new ReturnType());

    // validate parameters
    if (validateLogoutParameter(parameterList, response) == false)
    {
      return response; // error, so return
    }

    AuthenticateInfos authInfos = Authenticate(IP, parameterList.get(0));
    if (authInfos == null || authInfos.isLoggedIn() == false)
    {
      // user is not logged in
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.OK);
      response.getReturnInfos().setMessage("You have to be logged in the Terminology Server to log out!");
      return response;
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    try
    {
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        List<Session> sessionSet = Security.checkForExistingSessions(hb_session, parameterList.get(0).trim());

        if (sessionSet != null && !sessionSet.isEmpty())
        {
          for (Session session : sessionSet)
          {
            session.setTermUser(null);
            hb_session.delete(session);
          }
        }

        hb_session.getTransaction().commit();

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Logout successful");
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at 'Logout', Hibernate: " + e.getLocalizedMessage());

        logger.error("Error at 'Logout', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        // Transaktion abschließen
        hb_session.close();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Error at 'Logout': " + e.getLocalizedMessage());

      logger.error("Error at 'Logout': " + e.getLocalizedMessage());
    }

    return response;
  }

  public ChangePasswordResponseType ChangePassword(String IP, List<String> parameterList)
  {
    if (logger.isInfoEnabled())
      logger.info("====== UsernamePasswordMethod - Login started ======");

    // create return infos
    ChangePasswordResponseType response = new ChangePasswordResponseType();
    response.setReturnInfos(new ReturnType());

    // check parameters
    if (validateChangePasswordParameter(parameterList, response) == false)
    {
      return response; // error, so return
    }

    try
    {
      java.util.List<TermUser> list = null;

      // hibernate block, open session
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try // 2. try-catch-block to catch hibernate errors
      {
        // create HQL
        String hql = "select u from TermUser u";

        // use hql parameter helper to set where part
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        logger.debug("parameterList: " + parameterList);
        if (parameterList != null)
          logger.debug("parameterList size: " + parameterList.size());

        String username = "";
        String old_password_hash = "";
        String new_password_hash = "";
        
        if (parameterList != null && parameterList.size() >= 2)
        {
          username = parameterList.get(0);
          old_password_hash = parameterList.get(1);
          new_password_hash = parameterList.get(2);

          parameterHelper.addParameter("u.", "name", username);
        }

        logger.debug("username: " + username);
        //logger.debug("password_hash: " + new_password_hash);

        // add parameter with "and"
        hql += parameterHelper.getWhere("");

        // create query
        org.hibernate.Query q = hb_session.createQuery(hql);

        // set parameter in query
        parameterHelper.applyParameter(q);

        // do database query
        list = (java.util.List<TermUser>) q.list();

        boolean loginCorrect = false;

        TermUser termUser = null;
        String salt = "";

        if (username.length() > 0 && old_password_hash.length() > 0
                && list != null && list.size() > 0)
        {
          termUser = list.get(0);
          salt = termUser.getSalt();

          if (salt == null || salt.length() == 0)
          {
            // kein Salt vorhanden
            logger.warn("no salt value available for user");
            loginCorrect = false;
          }
          else
          {
            // Passwort prüfen (Hash + Salt)
            String passwordSalted = Password.getSaltedPassword(old_password_hash, salt, username, ITERATION_COUNT);
            loginCorrect = passwordSalted.equals(termUser.getPassw());
          }
        }

        if (loginCorrect)
        {
          // Login successful, change password now
          String newPasswordSalted = Password.getSaltedPassword(new_password_hash, salt, username, ITERATION_COUNT);
          termUser.setPassw(newPasswordSalted);
          hb_session.update(termUser);

          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Password successfully changed");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Username or password wrong");
        }
        // close transaction and commit changes (session-infos changed)
        tx.commit();
      }
      catch (Exception e)
      {
        tx.rollback();

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Error at 'ChangePassword', Hibernate: " + e.getLocalizedMessage());

        logger.error("Error at 'ChangePassword', Hibernate: " + e.getLocalizedMessage());
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }
    }
    catch (Exception e)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Error at 'ChangePassword': " + e.getLocalizedMessage());

      LoggingOutput.outputException(e, this);
    }

    return response;
  }

  public AuthenticateInfos Authenticate(String IP, String loginToken)
  {
    AuthenticateInfos response = new AuthenticateInfos();
    response.setIsAdmin(false);
    response.setLoggedIn(false);
    response.setUserId(0);
    response.setLicences(new LinkedList<LicencedUser>());

    // check parameters
    if (loginToken == null || loginToken.length() == 0)
    {
      logger.warn("Parameterlist is not correct for 'Authenticate', must be 1 entry containing the session id");
      response.setMessage("Parameterlist is not correct for 'Authenticate', must be 1 entry containing the session id");
      return response;
    }

    // Hibernate-Block, Session öffnen
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
    {
      List<Session> sessionSet = Security.checkForExistingSessions(hb_session, loginToken.trim());

      if (sessionSet != null && sessionSet.size() > 0)
      {
        response.setIsAdmin(sessionSet.get(0).getTermUser().isIsAdmin());
        response.setLoggedIn(true);
        response.setUserId(sessionSet.get(0).getTermUser().getId());
        response.setLicences(new LinkedList(sessionSet.get(0).getTermUser().getLicencedUsers()));
        return response;
      }
    }
    catch (Exception e)
    {
      logger.error("Error at 'Authenticate', Hibernate: " + e.getLocalizedMessage());
    }
    finally
    {
      // Transaktion abschließen
      hb_session.close();
    }

    return response;
  }

  private boolean validateLoginParameter(
          List<String> Request,
          LoginResponseType Response)
  {
    boolean erfolg = true;

    if (Request == null)
    {
      Response.getReturnInfos().setMessage("Parameter list may not be empty!");
      erfolg = false;
    }
    else
    {
      if (Request.size() < 2)
      {
        Response.getReturnInfos().setMessage("The parameter list must have at least 2 entries: (1) username, (2) md5-hashed password");
        erfolg = false;
      }
      else
      {
        if (Request.get(0).length() == 0)
        {
          Response.getReturnInfos().setMessage("The username may not be empty!");
          erfolg = false;
        }
        else if (Request.get(1).trim().length() != 32)
        {
          Response.getReturnInfos().setMessage("The md5-hashed password must have a length of 32 characters!");
          erfolg = false;
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

  private boolean validateLogoutParameter(
          List<String> Request,
          LogoutResponseType Response)
  {
    boolean erfolg = true;

    if (Request == null)
    {
      Response.getReturnInfos().setMessage("Parameter list may not be empty!");
      erfolg = false;
    }
    else
    {
      if (Request.size() != 1)
      {
        Response.getReturnInfos().setMessage("The parameter list must have 1 entrie containing the session id");
        erfolg = false;
      }
      else
      {
        if (Request.get(0).trim().length() != 36)
        {
          Response.getReturnInfos().setMessage("The session id must have a length of 36 characters!");
          erfolg = false;
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

  private boolean validateChangePasswordParameter(
          List<String> Request,
          ChangePasswordResponseType Response)
  {
    boolean erfolg = true;

    if (Request == null)
    {
      Response.getReturnInfos().setMessage("Parameter list may not be empty!");
      erfolg = false;
    }
    else
    {
      if (Request.size() != 3)
      {
        Response.getReturnInfos().setMessage("The parameter list must have 3 entries: (1) username, (2) md5-hashed old password, (3) md5-hashed new password");
        erfolg = false;
      }
      else
      {
        if (Request.get(1).trim().length() != 32)
        {
          Response.getReturnInfos().setMessage("The md5-hashed password (old) must have a length of 32 characters!");
          erfolg = false;
        }
        else if (Request.get(2).trim().length() != 32)
        {
          Response.getReturnInfos().setMessage("The md5-hashed password (new) must have a length of 32 characters!");
          erfolg = false;
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
