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
package de.fhdo.gui.header;

import de.fhdo.authorization.Authorization;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.VersionHelper;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Label;
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
  //private long userID = 0;

  public void afterCompose()
  {
    String user = SessionHelper.getUserName();
    boolean isAdmin = SessionHelper.isAdmin();

    /*if (SessionHelper.isCollaborationActive())
     {
     collaboration = true;
     user = SessionHelper.getCollaborationUserName();
     }
     else
     {
     user = SessionHelper.getUserName();
     }*/
    if (user.length() > 0)
    {
      Toolbarbutton tbUser = (Toolbarbutton) getFellow("tb_user");
      //tbUser.setLabel(user + " | " + Labels.getLabel("common.doLogoff"));
      tbUser.setLabel(user);

      if (isAdmin)
        tbUser.setImage("/rsc/img/design/user_admin_16x16.png");
      else
        tbUser.setImage("/rsc/img/design/user_16x16.png");
    }

    /*
     ComponentHelper.setVisible("tb_user", user.length() > 0, this);
     ComponentHelper.setVisible("tb_logout", user.length() > 0, this);
     ComponentHelper.setVisible("tb_termadmin", user.length() > 0 && collaboration == false && SessionHelper.isAdmin() == true, this);
     */
    ComponentHelper.setVisible("tb_user", user.length() > 0, this);
    ComponentHelper.setVisible("tb_logout", user.length() > 0, this);
    ComponentHelper.setVisible("tb_termadmin", isAdmin, this);

    /*if (user.length() > 0)
     {
     Toolbarbutton tbb = (Toolbarbutton) this.getFellow("tb_loginInfo");
     tbb.setVisible(true);

      
     {
     String infoKollab = "";
     String infoTerm = "";

     if (collaboration)//show kollab Login info
     infoKollab = "Eingeloggt in Kollaborationsplattform als: \"" + SessionHelper.getCollaborationUserName() + "\"";

     if (SessionHelper.isUserLoggedIn())
     infoTerm = "Eingeloggt in Verwaltungsumgebung als: \"" + SessionHelper.getUserName() + "\"";

     if (!infoKollab.equals("") && infoTerm.equals(""))
     {//only kollab
     String t = infoKollab + " | Rolle: " + SessionHelper.getCollaborationUserRole();
     tbb.setLabel(t);
     }
     else if (infoKollab.equals("") && !infoTerm.equals(""))
     { //only termadm
     String t = infoTerm + " | Rolle: " + SessionHelper.getCollaborationUserRoleFromTermAdmLogin();// No collab Info!!!!
     tbb.setLabel(t);
     }
     else
     { //both
     String t = infoKollab + " | " + infoTerm + " | Rolle: " + SessionHelper.getCollaborationUserRole();
     tbb.setLabel(t);
     }
     }
     }*/
    // show termserver status/link
    String tsLabel
            = PropertiesHelper.getInstance().getTermserverUrl()
            + PropertiesHelper.getInstance().getTermserverServiceName();
    
    String version = VersionHelper.getInstance().getVersion();
    if(version != null && version.length() > 0)
      tsLabel += " | v" + version;
    
    ((Label) getFellow("labelTermserver")).setValue(tsLabel);
  }

  public void onCallAdminClicked()
  {
    Executions.sendRedirect("../../../TermAdmin/gui/admin/admin.zul");
  }

  public void onUserClicked()
  {
    //Authorization.logout();
    Authorization.changePassword();
    // User-Details öffnen
    /*if (collaboration)
     {
     try
     {
     Map map = new HashMap();
     map.put("user_id", SessionHelper.getCollaborationUserID());

     Window win = (Window) Executions.createComponents(
     "/collaboration/userDetails.zul", null, map);

     //((MasterdataDetails) win).setUpdateInterface(RefThis);

     win.doModal();
     }
     catch (Exception ex)
     {
     LoggingOutput.outputException(ex, this);
     }
     }*/
  }
  
  public void onLogoutClicked()
  {
    Authorization.logout();
  }
}
