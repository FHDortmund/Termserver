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
package de.fhdo.helper;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.communication.Mail;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.authorization.LoginResponse.Return;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

/**
 *
 * @author Robert Mützner
 */
//public class LoginHelper
//{
//
//  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
//  private static LoginHelper instance = null;
//  private static final int activationTimespan = 259200; //72h
//
//  public LoginHelper()
//  {
//  }
//
//  public String login(String username, String password)
//  {
//    logger.debug("LOGIN mit Username: " + username);
//
//    //if(username.equalsIgnoreCase("user") && password.equals("test"))
//    //  return true;
//    String finalSessionId = "";
//    Session hb_session = HibernateUtil.getSessionFactory().openSession();
//    //hb_session.getTransaction().begin();
//    Session hb_session_kollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
//    //hb_session_kollab.getTransaction().begin();
//    boolean loggedin = false;
//    boolean isAdmin = false;
//    String cRole = "Diskussionsteilnehmer";
//    String cId = "";
//    try
//    {
//      String salt = "";
//
//      // 1. User lesen (Salt)
//      Query q = hb_session.createQuery("from TermUser WHERE name= :p_user AND enabled=1 AND activated=1");
//      q.setString("p_user", username);
//      java.util.List<TermUser> userList = q.list();
//
//      logger.debug("User-List-length: " + userList.size());
//
//      if (userList.size() == 1)
//      {
//        try
//        {
//          salt = userList.get(0).getSalt();
//        }
//        catch (Exception e)
//        {
//          logger.error("Fehler beim Lesen des Salt-Wertes: " + e.getLocalizedMessage());
//          e.printStackTrace();
//        }
//      }
//      if (salt == null)
//        salt = "";
//
//      // Super, sichere Methode
//      String passwordSalted = Password.getSaltedPassword(password, salt, username);
//
//      org.hibernate.Query q2 = hb_session.createQuery("from TermUser WHERE name=:p_user AND passw=:p_passwordSalted AND enabled=1 AND activated=1");
//      q2.setString("p_user", username);
//      q2.setString("p_passwordSalted", passwordSalted);
//      userList = (java.util.List<TermUser>) q2.list();
//
//      if (userList.size() == 1)
//      {
//        TermUser user = userList.get(0);
//        logger.debug("Login mit ID: " + user.getId());
//
//        if (user.getName().equals("termconsumer"))
//        {
//          return "termconsumer;1;2;3;4";
//        }
//
//        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//
//        session.setAttribute("user_id", user.getId());
//        session.setAttribute("is_admin", user.isIsAdmin());
//        session.setAttribute("user_name", user.getName());
//
//        logger.debug("Username: " + username);
//
//        org.hibernate.Query qCu = hb_session_kollab.createQuery("select distinct cu from Collaborationuser cu join fetch cu.roles r where cu.username=:username");
//        //AT_PU: Setting für Benutzerverwaltung
//        String str = username.substring(0, username.length() - 5);
//        logger.debug("Suche Kollab-User mit Name: " + str);
//        qCu.setString("username", str);
//        //*****************************//
//        //qCu.setString("username", username);
//        //***********************************/
//        List<Collaborationuser> cuList = qCu.list();
//
//        if (cuList != null && cuList.size() > 0)
//        {
//          cRole = cuList.get(0).getRoles().iterator().next().getName();
//          logger.debug("collaboration_user_role: " + cRole);
//
//          session.setAttribute("collaboration_user_role", cRole);
//          cId = String.valueOf(cuList.get(0).getId());
//          session.setAttribute("collaboration_user_id", cId);
//          logger.debug("collaboration_user_id: " + cId);
//
//        }
//        else
//        {
//          logger.debug("Kein Kollaborations-User");
//          session.setAttribute("collaboration_user_role", "");
//          session.setAttribute("collaboration_user_id", "0");
//          cRole = "";
//          cId = "0";
//        }
//        /*if (user.getPersons() != null && user.getPersons().size() > 0)
//         {
//         session.setAttribute("person_id", ((Person) user.getPersons().toArray()[0]).getId());
//         session.setAttribute("person_obj", user.getPersons().toArray()[0]);
//         }*/
//
//        logger.debug("user_id: " + session.getAttribute("user_id"));
//        logger.debug("is_admin: " + session.getAttribute("is_admin"));
//        logger.debug("user_name: " + session.getAttribute("user_name"));
//        //logger.debug("person_id: " + session.getAttribute("person_id"));
//
//        loggedin = true;
//
//        isAdmin = user.isIsAdmin();
//
//        session.setAttribute("session_id", "");
//
//        LoginRequestType loginRequest = new LoginRequestType();
//        loginRequest.setLogin(new LoginType());
//        loginRequest.getLogin().setUsername(username);
//        loginRequest.getLogin().setPassword(passwordSalted);
//
//        Return response = login_1(loginRequest);
//        if (response.getReturnInfos().getStatus() == Status.OK)
//        {
//          session.setAttribute("session_id", response.getLogin().getSessionID());
//          finalSessionId = (String) session.getAttribute("session_id");
//        }
//
//        logger.debug("session_id: " + session.getAttribute("session_id"));
//      }
//
//    }
//    catch (Exception e)
//    {
//      logger.error("Fehler beim Login: " + e.getLocalizedMessage());
//      e.printStackTrace();
//    }
//    finally
//    {
//      hb_session.close();
//      hb_session_kollab.close();
//    }
//
//    String returnValue = "";
//    if (loggedin)
//    {
//      returnValue = "true;" + finalSessionId;
//    }
//    else
//    {
//      returnValue = "false;" + finalSessionId;
//    }
//
//    if (isAdmin)
//    {
//      returnValue += ";true;";
//    }
//    else
//    {
//      returnValue += ";false;";
//    }
//
//    returnValue += cRole + ";" + cId;
//
//    logger.debug("Return-Value: " + returnValue);
//
//    return returnValue;
//  }
//
//  public boolean loginUserPass(String username, String password)
//  {
//    logger.debug("LOGIN mit Username: " + username);
//
//    Session hb_session = HibernateUtil.getSessionFactory().openSession();
//
//    boolean loggedin = false;
//
//    try
//    {
//      String salt = "";
//
//      // 1. User lesen (Salt)
//      Query q = hb_session.createQuery("from TermUser WHERE name= :p_user AND enabled=1 AND activated=1");
//      q.setString("p_user", username);
//      java.util.List<TermUser> userList = q.list();
//
//      logger.debug("User-List-length: " + userList.size());
//
//      if (userList.size() == 1)
//      {
//        try
//        {
//          salt = userList.get(0).getSalt();
//        }
//        catch (Exception e)
//        {
//          logger.error("Fehler beim Lesen des Salt-Wertes: " + e.getLocalizedMessage());
//          e.printStackTrace();
//        }
//      }
//      if (salt == null)
//        salt = "";
//
//      // Super, sichere Methode
//      String passwordSalted = Password.getSaltedPassword(password, salt, username);
//
//      org.hibernate.Query q2 = hb_session.createQuery("from TermUser WHERE name=:p_user AND passw=:p_passwordSalted AND enabled=1 AND activated=1");
//      q2.setString("p_user", username);
//      q2.setString("p_passwordSalted", passwordSalted);
//      userList = (java.util.List<TermUser>) q2.list();
//
//      if (userList.size() == 1)
//      {
//        TermUser user = userList.get(0);
//        logger.debug("Login mit ID: " + user.getId());
//
//        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//
//        session.setAttribute("user_id", user.getId());
//        session.setAttribute("is_admin", user.isIsAdmin());
//        session.setAttribute("user_name", user.getName());
//
//        logger.debug("Username: " + username);
//
//        loggedin = true;
//        //isAdmin = user.isIsAdmin();
//
//        session.setAttribute("session_id", "");
//
//      }
//
//    }
//    catch (Exception e)
//    {
//      logger.error("Fehler beim Login: " + e.getLocalizedMessage());
//      e.printStackTrace();
//    }
//    finally
//    {
//      hb_session.close();
//    }
//
//    // get Session-ID
//    if (loggedin)
//    {
//      LoginRequestType loginRequest = new LoginRequestType();
//      loginRequest.setLogin(new LoginType());
//      loginRequest.getLogin().setUsername(username);
//      loginRequest.getLogin().setPassword(MD5.getMD5(password));
//
//      Return response = login_1(loginRequest);
//      if (response.getReturnInfos().getStatus() == Status.OK)
//      {
//        SessionHelper.setValue("session_id", response.getLogin().getSessionID());
//        //session.setAttribute("session_id", response.getLogin().getSessionID());
//      }
//
//      logger.debug("session_id: " + SessionHelper.getValue("session_id"));
//    }
//
//    return loggedin;
//  }
//
//  public boolean storePseudonym(String username, String pseudnym)
//  {
//
//    Session hb_session = HibernateUtil.getSessionFactory().openSession();
//    hb_session.getTransaction().begin();
//    boolean stored = false;
//
//    try
//    {
//      Query q = hb_session.createQuery("from TermUser WHERE name= :p_user AND enabled=1");
//      q.setString("p_user", username);
//      java.util.List<TermUser> userList = q.list();
//
//      logger.debug("User-List-length: " + userList.size());
//
//      if (userList.size() == 1)
//      {
//        try
//        {
//          TermUser user = userList.get(0);
//          user.setPseudonym(pseudnym);
//          hb_session.saveOrUpdate(user);
//        }
//        catch (Exception e)
//        {
//          e.printStackTrace();
//        }
//      }
//
//      hb_session.getTransaction().commit();
//      stored = true;
//    }
//    catch (Exception e)
//    {
//      hb_session.getTransaction().rollback();
//      logger.error("Fehler beim speichern des pseudonyms " + e.getLocalizedMessage());
//    }
//    finally
//    {
//      hb_session.close();
//    }
//
//    return stored;
//  }
//
//  public void reset()
//  {
//    logger.debug("reset()");
//    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//
//    session.setAttribute("user_id", 0);
//    //session.setAttribute("person_id", 0);
//    session.setAttribute("user_name", "");
//    session.setAttribute("is_admin", false);
//
//    // RightsHelper.getInstance().clear();
//  }
//
//  public void logout()
//  {
//    Clients.showBusy("Abmelden...");
//
//    reset();
//    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//    session.invalidate();
//
//    //Executions.sendRedirect("/index.zul");
//  }
//
//  public boolean activate(String hash)
//  {
//    Session hb_session = HibernateUtil.getSessionFactory().openSession();
//    hb_session.getTransaction().begin();
//
//    org.hibernate.Query q = hb_session.createQuery("from TermUser WHERE activation_md5=:p_hash");
//    q.setString("p_hash", hash);
//
//    java.util.List<TermUser> userList = (java.util.List<TermUser>) q.list();
//
//    if (userList.size() == 1)
//    {
//      TermUser user = userList.get(0);
//      DateTime now = DateTime.now();
//      DateTime origin = new DateTime(user.getActivationTime());
//
//      Seconds seconds = Seconds.secondsBetween(origin, now);
//      int sec = seconds.getSeconds();
//      if (seconds.getSeconds() < activationTimespan)
//      {
//        user.setEnabled(true);
//        user.setActivated(true);
//        user.setActivationMd5("");
//
//        hb_session.merge(user);
//
//        hb_session.getTransaction().commit();
//        hb_session.close();
//
//        return true;
//      }
//      else
//      {
//        return false;
//      }
//    }
//
//    hb_session.close();
//
//    return false;
//  }
//
//  public static boolean resendPassword(boolean New, String Username)
//  {
//    boolean erfolg = false;
//
//    if (logger.isDebugEnabled())
//      logger.debug("Neues Passwort fuer Benutzer " + Username);
//
//    Session hb_session = HibernateUtil.getSessionFactory().openSession();
//    hb_session.getTransaction().begin();
//
//    try
//    {
//      List list = hb_session.createQuery("from TermUser where name=:p_user").setString("p_user", Username).list();
//
//      if (list.size() > 0)
//      {
//        TermUser user = (TermUser) list.get(0);
//
//        // Neues Passwort generieren
//        String neuesPW = Password.generateRandomPassword(8);
//
//        // Email-Adresse lesen
//        String mail = user.getEmail();
//
//        // Neues Passwort per Email versenden
//        String result = Mail.sendNewPassword(Username, neuesPW, mail);
//        if (result.length() == 0)
//          erfolg = true;
//
//        if (erfolg)
//        {
//          // Neues Passwort in der Datenbank speichern
//          /*String salt = Password.generateRandomSalt();
//           user.setPassw(Password.getSaltedPassword(neuesPW, salt, Username));
//           user.setSalt(salt);
//
//           hb_session.merge(user);*/
//          String salt = "";
//          user.setPassw(Password.getSaltedPassword(neuesPW, salt, Username));
//          user.setSalt(salt);
//
//          hb_session.merge(user);
//        }
//      }
//
//      hb_session.getTransaction().commit();
//    }
//    catch (Exception e)
//    {
//      logger.error("Fehler bei resendPassword(): " + e.getLocalizedMessage());
//    }
//    finally
//    {
//      hb_session.close();
//    }
//
//    return erfolg;
//
//  }
//
//  public static LoginHelper getInstance()
//  {
//    if (instance == null)
//      instance = new LoginHelper();
//
//    return instance;
//  }
//
//  private static Return login_1(de.fhdo.terminologie.ws.authorization.LoginRequestType parameter)
//  {
//    de.fhdo.terminologie.ws.authorization.Authorization_Service service = new de.fhdo.terminologie.ws.authorization.Authorization_Service();
//    de.fhdo.terminologie.ws.authorization.Authorization port = service.getAuthorizationPort();
//    return port.login(parameter);
//  }
//}
