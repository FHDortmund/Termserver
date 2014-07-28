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
package de.fhdo.gui.main;

import de.fhdo.authorization.Authorization;
import de.fhdo.helper.SessionHelper;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class StatusBar extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static org.zkoss.zk.ui.Session session = org.zkoss.zk.ui.Sessions.getCurrent();

  private long userID = 0;
  private long personID = 0;

  public void afterCompose()
  {
    org.zkoss.zk.ui.Session session = org.zkoss.zk.ui.Sessions.getCurrent();

    userID = SessionHelper.getUserID();
    
    String user = "";
    if (session.getAttribute("user_name") != null)
      user = session.getAttribute("user_name").toString();

    boolean isAdmin = false;
    if (session.getAttribute("is_admin") != null)
      isAdmin = Boolean.parseBoolean(session.getAttribute("is_admin").toString());

    if (session.getAttribute("person_id") != null)
      personID = Long.parseLong(session.getAttribute("person_id").toString());

    logger.debug("[StatusBar] userID: " + userID);
    logger.debug("[StatusBar] PersonID: " + personID);
    logger.debug("[StatusBar] user_name: " + user);

    Toolbarbutton tbbU = (Toolbarbutton) getFellow("tb_user");
    //tbbU.setLabel("Benutzer-Details");
    tbbU.setLabel(user);
    

    if (isAdmin)
      tbbU.setImage("/rsc/img/symbols/user_admin_16x16.png");
    else
      tbbU.setImage("/rsc/img/symbols/user_16x16.png");

    getFellow("tb_loginInfo").setVisible(false);
    /*if (user.length() > 0)
    {
      Toolbarbutton tbb = (Toolbarbutton) this.getFellow("tb_loginInfo");
      tbb.setVisible(true);
      String infoTerm = "";

      if (SessionHelper.isUserLoggedIn())
        infoTerm = "Eingeloggt in Verwaltungsumgebung als: \"" + SessionHelper.getUserName() + "\"";

      String t = infoTerm + " | Rolle: " + SessionHelper.getCollaborationUserRole();
      tbb.setLabel(t);
    }*/
  }

  public void onLogoutClicked()
  {
    Authorization.logout();
    //Executions.sendRedirect("../../../TermBrowser/gui/admin/logout.zul");
  }

  /*public void onCallBrowserClicked()
   {
   Executions.sendRedirect("../../../TermBrowser/gui/main/main.zul");
   }*/
  public void onUserClicked()
  {
    Authorization.changePassword();
    
    // User-Details öffnen
    /*try
     {

     Map map = new HashMap();
     map.put("person_id", personID);

     Window win = (Window) Executions.createComponents(
     "/gui/main/masterdata/masterdataDetails.zul", null, map);

     //((MasterdataDetails) win).setUpdateInterface(this);

     win.doModal();
     }
     catch (Exception ex)
     {
     logger.debug("Fehler beim Öffnen der Masterdaten: " + ex.getLocalizedMessage());
     ex.printStackTrace();
     //logger.error("Fehler in Klasse '" + DiagnosisDetails.class.getName()
     //  + "': " + ex.getMessage());
     }*/

    // Passwort ändern
//    try
//    {
//      logger.debug("Erstelle Fenster...");
//
//      Map map = new HashMap();
//      map.put("user_id", SessionHelper.getUserID());
//
//      Window win = (Window) Executions.createComponents(
//              "/gui/main/masterdata/userDetails.zul", null, map);
//
//      //((PasswordDetails) win).setUpdateListInterface(this);
//      logger.debug("Öffne Fenster...");
//      win.doModal();
//    }
//    catch (Exception ex)
//    {
//      logger.error("Fehler in Klasse '" + this.getClass().getName()
//              + "': " + ex.getMessage());
//    }
  }
}
