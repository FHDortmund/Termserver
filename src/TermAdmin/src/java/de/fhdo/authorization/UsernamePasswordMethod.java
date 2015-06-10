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
package de.fhdo.authorization;

import de.fhdo.communication.Mail;
import de.fhdo.helper.MD5;
import de.fhdo.helper.Password;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class UsernamePasswordMethod implements IAuthorization
{

  public static final int ITERATION_COUNT = 500;

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public boolean doLogin()
  {
    logger.debug("[UsernamePasswordMethod] doLogin()");

    return false;
  }

  public boolean doLogout()
  {
    logger.debug("[UsernamePasswordMethod] doLogout()");

    // Webservice aufrufen
    logger.debug("Authorization.logout()-Webservice wird aufgerufen");

    // Generische Parameterliste füllen (hier nur SessionID)
    List<String> parameterList = new LinkedList<String>();
    parameterList.add(SessionHelper.getSessionId());

    LogoutResponseType response = WebServiceHelper.logout(parameterList);
    logger.debug("Antwort: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      SessionHelper.reset();

      return true;
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
    return false;
  }

  public boolean createOrEditUser(Map<String, String> parameter, boolean createUser, String password)
  {
    logger.debug("createOrEditUser, createUser: " + createUser);

    String username = "";
    String mail = "";
    boolean isAdmin = false;
    long userId = 0;

    if (parameter != null)
    {
      if (parameter.containsKey("username"))
        username = parameter.get("username");

      if (parameter.containsKey("mail"))
        mail = parameter.get("mail");

      if (parameter.containsKey("isAdmin"))
        isAdmin = Boolean.parseBoolean(parameter.get("isAdmin") + "");
      
      if (parameter.containsKey("userId"))
        userId = Long.parseLong(parameter.get("userId") + "");
    }

    logger.debug("username: " + username);
    logger.debug("mail: " + mail);
    logger.debug("isAdmin: " + isAdmin);
    logger.debug("userId: " + userId);
    

    if (username == null || username.length() == 0
            || mail == null || mail.length() == 0)
    {
      return false;
    }

    boolean success = false;

    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      org.hibernate.Transaction tx = hb_session.beginTransaction();
      if (createUser)
      {
        if (logger.isDebugEnabled())
          logger.debug("create new user...");

        // TODO prüfen, ob User bereits existiert
        TermUser user = new TermUser();
        user.setName(username);
        user.setEmail(mail);
        user.setIsAdmin(isAdmin);

        // create Salt
        user.setSalt(Password.generateRandomSalt());

        // create random password if not set
        if(password == null || password.length() == 0)
          password = Password.generateRandomPassword(8);

        // activate user immediately
        user.setActivationTime(new Date());
        user.setEnabled(true);

        // set hashed and salted password
        user.setPassw(Password.getSaltedPassword(MD5.getMD5(password), user.getSalt(), username, ITERATION_COUNT));

        logger.debug("password: " + password);
        logger.debug("getSalt: " + user.getSalt());
        logger.debug("password: " + user.getPassw());

        hb_session.save(user);

        // send mail
        String s = Mail.sendMailNewUser(user.getName(), password, user.getActivationMd5(), user.getEmail());

        if (s.length() == 0)
        {
          tx.commit();
          success = true;
        }
        else
        {
          logger.warn("send mail error: " + s);
          tx.rollback();
          success = false;
        }

        password = "0000000000000000000000000000000000";
        password = "";
      }
      else
      {
        TermUser user = (TermUser) hb_session.get(TermUser.class, userId);
        if(user != null)
        {
          user.setEmail(mail);
          user.setIsAdmin(isAdmin);
          
          hb_session.update(user);
          success = true;
        }

        tx.commit();
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session.close();
    }

    return success;
  }

  
  public boolean resendPassword(String username)
  {
    boolean success = false;

    // Neues Passwort generieren
    String neuesPW = Password.generateRandomPassword(8);
    logger.debug("Neues Passwort: " + neuesPW);
    String mail = "";
    long userId = 0;
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      //org.hibernate.Transaction tx = hb_session.beginTransaction();
      List list = hb_session.createQuery("from TermUser where name=:p_user").setString("p_user", username).list();

      if (list.size() > 0)
      {
        TermUser user = (TermUser) list.get(0);
        userId = user.getId();
        logger.debug("userId: " + userId);

        // Email-Adresse lesen
        mail = user.getEmail();
        
        /*String salt = Password.generateRandomSalt();
        user.setPassw(Password.getSaltedPassword(neuesPW, salt, username, ITERATION_COUNT));
        user.setSalt(salt);

        logger.debug("merge");
        hb_session.merge(user);
        logger.debug("commit");
        tx.commit();*/
        //logger.debug("commit fertig");
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session.close();
    }
    logger.debug("mail: " + mail);

    // Neues Passwort per Email versenden
    if (mail.length() > 0)
    {
      String result = Mail.sendNewPassword(username, neuesPW, mail);
      if (result.length() == 0)
        success = true;
      //String result = "";

      if (result.length() == 0)
      {
        logger.debug("change Password in db...");
        hb_session = HibernateUtil.getSessionFactory().openSession();

        try
        {
          org.hibernate.Transaction tx = hb_session.beginTransaction();

          TermUser user = (TermUser) hb_session.get(TermUser.class, userId);

          
          // Neues Passwort in der Datenbank speichern
          String salt = Password.generateRandomSalt();
          
          //logger.debug("getSaltedPassword: " + username + ", " + salt + ", " + neuesPW);
          user.setPassw(Password.getSaltedPassword(MD5.getMD5(neuesPW), salt, username, ITERATION_COUNT));
          user.setSalt(salt);

          hb_session.merge(user);
          tx.commit();
          success = true;
        }
        catch (Exception e)
        {
          LoggingOutput.outputException(e, this);
        }
        finally
        {
          hb_session.close();
        }
      }
    }
    return success;
  }

  public void changePassword()
  {
    // Passwort ändern
    try
    {
      logger.debug("Erstelle Fenster...");

      Map map = new HashMap();
      map.put("user_id", SessionHelper.getUserID());

      Window win = (Window) Executions.createComponents(
              "/gui/main/masterdata/userDetails.zul", null, map);

      logger.debug("Öffne Fenster...");
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + this.getClass().getName()
              + "': " + ex.getMessage());
    }
  }

}
