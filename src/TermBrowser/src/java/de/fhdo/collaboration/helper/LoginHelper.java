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
package de.fhdo.collaboration.helper;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.communication.Mail;
import de.fhdo.helper.Password;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.internal.util.StringHelper;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author Robert Mützner
 */
public class LoginHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static LoginHelper instance = null;
  private static final int activationTimespan = 259200; //72h

  public LoginHelper()
  {
  }

  public boolean login(String username, String password)
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    boolean loggedin = false;

    try
    {
      String salt = "";

      // 1. User lesen (Salt)
      Query q = hb_session.createQuery("from Collaborationuser WHERE username= :p_user AND enabled=1 AND activated=1");
      q.setString("p_user", username);
      java.util.List<Collaborationuser> userList = q.list();

      logger.debug("User-List-length: " + userList.size());

      if (userList.size() == 1)
      {
        try
        {
          salt = userList.get(0).getSalt();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      if (salt == null)
        salt = "";

      logger.debug("salt: " + salt);

      // Super, sichere Methode
      String passwordSalted = Password.getSaltedPassword(password, salt, username);

      logger.debug("username: " + username);
      logger.debug("password: " + password);
      logger.debug("passwordSalted: " + passwordSalted);

      org.hibernate.Query q2 = hb_session.createQuery("from Collaborationuser WHERE username=:p_user AND password=:p_passwordSalted AND enabled=1 AND activated=1");
      q2.setString("p_user", username);
      q2.setString("p_passwordSalted", passwordSalted);
      userList = (java.util.List<Collaborationuser>) q2.list();

      logger.debug("User-List-length 2: " + userList.size());

      if (userList.size() == 1)
      {
        Collaborationuser user = userList.get(0);
        logger.debug("Login mit ID: " + user.getId());

        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        session.setAttribute("collaboration_user_id", user.getId());
        session.setAttribute("collaboration_user_name", user.getUsername());
        //session.setAttribute("collaboration_user_role", user.getRoles().iterator().next().getName());  // TODO nur 1 Rolle?
        
        Hibernate.initialize(user.getRoles());
        session.setAttribute("collaboration_user_roles", user.getRoles());

        session.setAttribute("CollaborationActive", true);

        logger.debug("collaboration_user_id: " + session.getAttribute("collaboration_user_id"));
        logger.debug("collaboration_user_name: " + session.getAttribute("collaboration_user_name"));

        loggedin = true;
      }
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
      logger.error("Fehler beim Login: " + e.getLocalizedMessage());
    }
    finally
    {
      hb_session.close();
    }

    return loggedin;
  }

  public void reset()
  {
    logger.debug("reset()");
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    session.removeAttribute("collaboration_user_id");
    session.removeAttribute("collaboration_user_name");
    session.removeAttribute("collaboration_user_roles");
    session.removeAttribute("CollaborationActive");
  }

  public void logout()
  {
    Clients.showBusy("Abmelden...");
    //collabsoftware muss abgemeldet werden!

    if (StringHelper.isEmpty(CollaborationSession.getInstance().getSessionID()))
    {
      reset();
      Executions.sendRedirect("/gui/main/main.zul");
    }
    else
    {
      logger.debug("Authorization.login()-Webservice wird aufgerufen");
      de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
      de.fhdo.terminologie.ws.authorization.Authorization port = service.getAuthorizationPort();

      List<String> paramList = new LinkedList<String>();
      paramList.add(CollaborationSession.getInstance().getSessionID());

      LogoutResponseType response = port.logout(paramList);
      logger.debug("Antwort: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        reset();
        CollaborationSession.getInstance().setSessionID(null);
      //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
        //session.invalidate();

        Executions.sendRedirect("/gui/main/main.zul");
      }
      else
      {
        try
        {
          Messagebox.show(Labels.getLabel("loginHelper.loggingOffError") + ": " + response.getReturnInfos().getMessage());
        }
        catch (Exception ex)
        {
          LoggingOutput.outputException(ex, this);
        }
      }
    }
  }

  public boolean activate(String hash)
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    org.hibernate.Query q = hb_session.createQuery("from Collaborationuser WHERE activation_md5=:p_hash");
    q.setString("p_hash", hash);

    java.util.List<Collaborationuser> userList = (java.util.List<Collaborationuser>) q.list();

    if (userList.size() == 1)
    {
      Collaborationuser user = userList.get(0);
      DateTime now = DateTime.now();
      DateTime origin = new DateTime(user.getActivationTime());
      Seconds seconds = Seconds.secondsBetween(origin, now);
      int sec = seconds.getSeconds();
      if (seconds.getSeconds() < activationTimespan)
      {

        user.setEnabled(true);
        user.setActivated(true);
        user.setActivationMd5("");

        hb_session.merge(user);

        hb_session.getTransaction().commit();
        hb_session.close();

        return true;
      }
      else
      {
        return false;
      }
    }

    hb_session.close();

    return false;
  }

  public static boolean resendPassword(boolean New, String Username)
  {
    boolean erfolg = false;

    if (logger.isDebugEnabled())
      logger.debug("Neues Passwort fuer Benutzer " + Username);

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      List list = hb_session.createQuery("from Collaborationuser where username=:p_user").setString("p_user", Username).list();

      if (list.size() > 0)
      {
        Collaborationuser user = (Collaborationuser) list.get(0);

        // Neues Passwort generieren
        String neuesPW = Password.generateRandomPassword(8);

        // Email-Adresse lesen
        String mail = user.getEmail();

        // TODO Neues Passwort per Email versenden
        String[] adr = new String[1];
        adr[0] = mail;
        String result = Mail.sendNewPasswordCollaboration(Username, neuesPW, adr);
        if (result.length() == 0)
          erfolg = true;

        if (erfolg)
        {
          // Neues Passwort in der Datenbank speichern
          String salt = Password.generateRandomSalt();
          user.setPassword(Password.getSaltedPassword(neuesPW, salt, Username));
          user.setSalt(salt);

          hb_session.merge(user);
        }
      }

      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      logger.error("Fehler bei resendPassword(): " + e.getLocalizedMessage());
    }
    finally
    {
      hb_session.close();
    }

    return erfolg;

  }

  public static LoginHelper getInstance()
  {
    if (instance == null)
      instance = new LoginHelper();

    return instance;
  }
}
