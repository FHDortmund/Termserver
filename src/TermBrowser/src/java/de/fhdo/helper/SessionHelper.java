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

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.gui.main.TreeAndContent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Robert Mützner, Sven Becker
 */
public class SessionHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void reset()
  {
    logger.debug("reset()");
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    session.setAttribute("user_id", 0);
    session.setAttribute("user_name", "");
    session.setAttribute("is_admin", false);
    session.setAttribute("session_id", "");

    // RightsHelper.getInstance().clear();
  }

  public static boolean isUserLoggedIn()
  {
    String s = getSessionId();
    if (s != null && s.length() > 0)
    {
      return true;
    }
    return false;
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

  public static long getUserID(HttpSession session)
  {
    //org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session == null)
    {
      logger.debug("getUserID() - Session ist null");
      return 0;
    }

    logger.debug("getUserID(HttpSession session) mit session-id: " + session.getId());
    Enumeration en = session.getAttributeNames();
    while (en.hasMoreElements())
    {
      Object o = en.nextElement();
      logger.debug("Object in Session mit Typ: " + o.getClass().getCanonicalName());
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

  public static String getServerName()
  {
    return Sessions.getCurrent().getServerName();
  }

  public static String getUserName()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if (session == null)
    {
      //logger.debug("getUserName() - Session ist null");
      return "";
    }

    Object o = session.getAttribute("user_name");

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

  public static String getCollaborationUserName()
  {
    Object o = getValue("collaboration_user_name");

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

  public static long getUserID()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if (session == null)
    {
      //logger.debug("getUserID() - Session ist null");
      return 0;
    }

    Object o = session.getAttribute("user_id");

    if (o == null)
    {
      //logger.debug("getUserID() - o ist null");
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
    {
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
        logger.error("could not get personID: " + e.getMessage());
        return 0;
      }
    }
  }

  public static String getSessionId()
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();

    if (session == null)
    {
//      logger.debug("getSessionId() - Session ist null");
      return "";
    }

    Object o = session.getAttribute("session_id");

    if (o == null)
    {
//      logger.debug("getSessionId() - o ist null");
      return "";
    }
    else
    {
      return o.toString();
    }
  }

  public static boolean isCollaborationLoggedIn(HttpSession httpSession)
  {
    long id = getCollaborationUserID(httpSession);
    return id > 0;
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

  public static void setValue(String Name, Object Value)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session != null)
      session.setAttribute(Name, Value);
  }

  public static Object getValue(String Name)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session != null)
      return session.getAttribute(Name);
    else
      return null;
  }
  
  public static boolean getBoolValue(String Name, boolean Default)
  {
    org.zkoss.zk.ui.Session session = Sessions.getCurrent();
    if (session != null)
    {
      Object o = session.getAttribute(Name);
      if(o != null)
        return (Boolean)o;
    }
    
    return Default;
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

  /**
   * Ändert den Status der Kollaboration
   *
   * @return false: Kollaboration ist deaktiviert, true: aktiviert
   */
  public static boolean switchCollaboration()
  {

    boolean active = false;
    Object o = getValue("CollaborationActive");
    if (o != null)
    {
      active = (Boolean) o;
    }

    logger.debug("switchCollaboration(), aktuell: " + active);

    active = !active; // Zustand tauschen

    setValue("CollaborationActive", active);
    logger.debug("neu: " + active);

    return active;
  }

  public static boolean isCollaborationActive()
  {
    return isCollaborationActive(null);
  }

  public static boolean isCollaborationActive(HttpSession httpSession)
  {
    boolean active = false;
    Object o = getValue("CollaborationActive", httpSession);
    if (o != null)
    {
      active = (Boolean) o;
    }
    if (active)
    {
      return getCollaborationUserID(httpSession) > 0;
    }
    return false;
  }

  public static boolean isCollaborationFlag()
  {
    return isCollaborationActive(null);
  }

  public static boolean isCollaborationFlag(HttpSession httpSession)
  {
    boolean active = false;
    Object o = getValue("CollaborationActive", httpSession);
    if (o != null)
    {
      active = (Boolean) o;
    }
    return active;
  }

//// Neue Settings für UserAccounts //////////////////////////////////////////
//    
//    // Anzeigen von Crossmapping?
//    public static boolean isShowCrossmapping() {
//        Object o = getValue("is_ShowCrossmapping");
//
//        if (o == null) 
//            return false;
//        else 
//            return Boolean.parseBoolean(o.toString());        
//    }
//    
//    // Anzeigen von Verknüpften Konzepten?
//    public static boolean isShowLinkedConcepts() {
//        Object o = getValue("is_ShowLinkedConcepts");
//
//        if (o == null) 
//            return false;
//        else 
//            return Boolean.parseBoolean(o.toString());        
//    }
//    
  // Soll beim Anklicken eines CS/VS sofort die aktuelle Version geladen werden?
  public static boolean isLoadCurrentVersion()
  {
    Object o = getValue("is_LoadCurrentVersion");

    if (o == null)
      return false;
    else
      return Boolean.parseBoolean(o.toString());
  }

  public static String getCollaborationUserRoleFromTermAdmLogin()
  {

    String role = "";
    //Nur die Benutzer hohlen welche noch nicht zur Termserver DB "zugeordnet" sind!!!
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();

    String hqlC = "select distinct cu from Collaborationuser cu where cu.hidden=false AND deleted=0";

    try
    {

      List<Collaborationuser> userListC = hb_session.createQuery(hqlC).list();

      for (Collaborationuser cu : userListC)
      {

        if ((cu.getUsername() + "_tadm").equals(SessionHelper.getUserName()))
        {
          role = cu.getRoles().iterator().next().getName();
          break;
        }
      }

    }
    catch (Exception e)
    {
      logger.error("[Fehler bei CollabUserHelper.java createCollabUserTable(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
    return role;
  }
  
  public static ContentCSVSDefault.MODE getViewMode()
  {
    Object o = getValue("VIEW_MODE");
    if(o != null)
      return (ContentCSVSDefault.MODE) o;
    
    return ContentCSVSDefault.MODE.CODESYSTEMS;
  }
  
  public static void setViewMode(ContentCSVSDefault.MODE mode)
  {
    setValue("VIEW_MODE", mode);
  }
  
  public static TreeAndContent.MODE getMainViewMode()
  {
    Object o = getValue("VIEW_MODE");
    if(o != null)
      return (TreeAndContent.MODE) o;
    
    return TreeAndContent.MODE.CODESYSTEMS;
  }
  
  public static void setMainViewMode(TreeAndContent.MODE mode)
  {
    setValue("VIEW_MODE", mode);
  }

//    
//    // Soll beim Anklicken eines CS/VS sofort die aktuelle Version geladen werden?
//    public static boolean isShowPreferredLanguage() {
//        Object o = getValue("is_ShowPreferredLanguage");
//
//        if (o == null) 
//            return false;
//        else 
//            return Boolean.parseBoolean(o.toString());        
//    }
//    
//    // Bevorzugte Sprache
//    public static int preferredLanguage() {
//        Object o = getValue("is_preferredLanguage");
//
//        if (o == null) 
//            return -1;
//        else 
//            return Integer.valueOf(o.toString());        
//    }
  /*public static boolean isUserLoggedIn(HttpSession Session)
   {
   String s = getSessionId(Session);
   if(s != null && s.length() > 0)
   return true;
   return false;
   //return getUserID(Session) > 0;
   }*/

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
}
