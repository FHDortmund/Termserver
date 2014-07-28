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

import java.util.Enumeration;
import javax.servlet.http.HttpSession;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Robert Mützner
 */
public class SessionHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static org.zkoss.zk.ui.Session session = Sessions.getCurrent();

  public static void reset()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    session.setAttribute("user_id", null);
    session.setAttribute("user_name", null);
    session.setAttribute("is_admin", false);
    session.setAttribute("session_id", null);
    
    
  }
  
  public static boolean isUserLoggedIn()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    Object o = session.getAttribute("session_id");
    return o != null && o.toString().length() > 0;
  }
  
  public static boolean isUserLoggedIn(HttpSession Session)
  {
    Object o = Session.getAttribute("session_id");

    return o != null && o.toString().length() > 0;
    //String sessionId = getSessionId(Session);
    //return  getUserID(Session) > 0;
  }

  public static boolean isAdmin()
  {
    Object o = getValue("is_admin");

    if (o == null)
    {
      return false;
    }
    else
    {
      return Boolean.parseBoolean(o.toString());
    }
  }





  /*public static void checkUserLoggedIn()
  {
    boolean login = isUserLoggedIn();

    if (login == false)
    {
      Clients.showBusy("Nicht eingelogged...");

      LoginHelper.getInstance().reset();
      
      //session.setAttribute("user_id", 0);
      Executions.sendRedirect("/index.zul");
    }

  }*/



  public static long getUserID(HttpSession session)
  {
    //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if(session == null)
    {
      logger.debug("getUserID() - Session ist null");
      return 0;
    }

    logger.debug("getUserID(HttpSession session) mit session-id: " + session.getId());
    /*Enumeration en = session.getAttributeNames();
    while(en.hasMoreElements())
    {
      Object o = en.nextElement();
      logger.debug("Object in Session mit Typ: " + o.getClass().getCanonicalName());
    }*/
    
    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      logger.debug("getUserID() - o ist null");
      return 0;
    }
    else
    {
      try
      {
        return Long.parseLong(o.toString());
      }
      catch (Exception e)
      {
        logger.error("getUserID() - Fehler: " + e.getMessage());
        return 0;
      }
    }
  }

  public static String getUserName()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if(session == null)
    {
      logger.debug("getUserName() - Session ist null");
      return "";
    }

    Object o = session.getAttribute("user_name");

    if (o == null)
    {
      logger.debug("getUserName() - o ist null");
      return "";
    }
    else
    {
      return o.toString();
    }
  }

  public static long getUserID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if(session == null)
    {
      logger.debug("getUserID() - Session ist null");
      return 0;
    }

    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      logger.debug("getUserID() - o ist null");
      return 0;
    }
    else
    {
      try
      {
        return Long.parseLong(o.toString());
      }
      catch (Exception e)
      {
        logger.error("getUserID() - Fehler: " + e.getMessage());
        return 0;
      }
    }
  }

  public static long getPersonID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    Object o = session.getAttribute("person_id");

    if (o == null)
      return 0;
    else
    {
      try
      {
        return Long.parseLong(o.toString());
      }
      catch (Exception e)
      {
        logger.error("could not get personID: " + e.getMessage());
        return 0;
      }
    }
  }

  public static void setValue(String Name, Object Value)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    session.setAttribute(Name, Value);
    
    logger.debug("SessionHelper.setValue(): " + Name + ", " + Value);
  }

  public static Object getValue(String Name)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if(session.hasAttribute(Name))
      return session.getAttribute(Name);
    else return null;
  }
  
  public static String getCollaborationUserRole()
  {
    Object o = getValue("collaboration_user_role");

    if (o == null)
    {
      //logger.debug("getUserName() - o ist null");
      return "";
    }
    else
    {
      return o.toString();
    }
  }
  
  public static String getSessionId()
  {
    Object o = getValue("session_id");

    if (o == null)
    {
      return "";
    }
    else
    {
      return o.toString();
    }
  }
  
  public static long getCollaborationUserID()
  {
    return getCollaborationUserID(null);
  }
  public static long getCollaborationUserID(HttpSession httpSession)
  {
    //return 5l; 

    Object o = getValue("collaboration_user_id", httpSession);

    try
    {
      return Long.parseLong(o.toString());
    }
    catch (Exception e)
    {
      //logger.error("getCollaborationUserID() - Fehler: " + e.getMessage() + ", Objekt: " + o);
      return 0;
    }
  }
  
  public static Object getValue(String Name, HttpSession httpSession)
  {
    if (httpSession != null)
    {
      return httpSession.getAttribute(Name);
    }

    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session != null)
      return session.getAttribute(Name);
    else
      return null;
  }
}
