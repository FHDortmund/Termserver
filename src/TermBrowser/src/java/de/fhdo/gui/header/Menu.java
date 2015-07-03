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
import de.fhdo.collaboration.helper.LoginHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.ViewHelper;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import org.zkoss.zk.ui.event.EventListener;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;

/**
 *
 *
 * @author Robert Mützner
 */
public class Menu extends Window implements org.zkoss.zk.ui.ext.AfterCompose //public class Menu extends GenericAutowireComposer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private String headerStr;
  private boolean isCollaboration;
  transient EventListener onMenuitemClicked;

  public Menu()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("[Menu.java] Konstruktor");
    }

    //isCollaboration = SessionHelper.isCollaborationActive();
    isCollaboration = PropertiesHelper.getInstance().isCollaborationActive();

    if (isCollaboration)
    {
      headerStr = Labels.getLabel("collab.collaborationArea");
    }
    else
    {
      headerStr = Labels.getLabel("common.terminologyBrowser");
    }
    logger.debug("isCollaboration: " + isCollaboration);
  }

  public void afterCompose()
  {
    // Sichtbarkeiten Login-Status
    //String user = SessionHelper.getUserName();
    boolean loggedIn = SessionHelper.isUserLoggedIn();

    ((Menuitem) getFellow("menuitemAnmelden")).setDisabled(loggedIn);
    ((Menuitem) getFellow("menuitemAbmelden")).setDisabled(!loggedIn);
    ((Menuitem) getFellow("menuitemChangePassword")).setDisabled(!loggedIn);

    // collaboration
    if (PropertiesHelper.getInstance().isCollaborationActive())
    {
      boolean collabLoggedIn = SessionHelper.isCollaborationLoggedIn();
      
      ((Menuitem) getFellow("menuitemCollabLogin")).setDisabled(collabLoggedIn);
      ((Menuitem) getFellow("menuitemCollabLogoff")).setDisabled(!collabLoggedIn);
    //((Menuitem)getFellow("menuitemChangePassword")).setDisabled(!collabLoggedIn);

      ((Toolbarbutton) getFellow("tbb_CollabDesktop")).setDisabled(!collabLoggedIn);
      ((Toolbarbutton) getFellow("tbb_Vorschlag")).setDisabled(!collabLoggedIn);
    }
    getFellow("menuCollaboration").setVisible(PropertiesHelper.getInstance().isCollaborationActive());

    // Sichtbarkeit Ansicht
    boolean viewVisible = loggedIn && PropertiesHelper.getInstance().isGuiAllowShowingInvisibleConcepts();
    logger.debug("viewVisible: " + viewVisible);
    getFellow("menuView").setVisible(viewVisible);

    if (viewVisible)
    {
      ((Menuitem) getFellow("menuitemVisibleConcepts")).setChecked(SessionHelper.getBoolValue("showInvisibleConcepts", !PropertiesHelper.getInstance().isGuiShowOnlyVisibleConcepts()));
    }

    /*<menu label="${labels.common.view}" id="menuView">
     <menupopup>
     <menuitem id="menuitemVisibleConcepts" label="${labels.common.showInvisbleConcept}" onClick="win.changeShowVisibleConcepts()" ></menuitem>
     </menupopup>
     </menu>*/
  }

  public void changeShowVisibleConcepts()
  {
    boolean b = SessionHelper.getBoolValue("showInvisibleConcepts", !PropertiesHelper.getInstance().isGuiShowOnlyVisibleConcepts());
    SessionHelper.setValue("showInvisibleConcepts", !b);

    Executions.sendRedirect(null); // reload page
  }

  public void onLogoBRZClicked()
  {
    logger.debug("onLogoBRZClicked()");
    System.gc();
    Executions.getCurrent().sendRedirect("http://www.brz.gv.at/", "_blank");
  }

  private void clearHeader()
  {
  }

  public void onLogoutClicked()
  {
    if (isCollaboration)
    {
      de.fhdo.collaboration.helper.LoginHelper.getInstance().logout();
      Executions.sendRedirect("/gui/main/main.zul");
    }
    else
    {
      Authorization.logout();
      Executions.sendRedirect("../../../TermAdmin/gui/admin/logout.zul");
    }
  }

  public void onGlobalSearch()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("EditMode", 97);  // TODO ???
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupGlobalSearch.zul", null, data);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  public void onLogoEBPGClicked()
  {
    logger.debug("onLogoEBPGClicked()");
    Executions.getCurrent().sendRedirect("http://www.ebpg-nrw.de/", "_blank");
  }

  public void onLogoFHClicked()
  {
    logger.debug("onLogoFHClicked()");
    Executions.getCurrent().sendRedirect("http://www.fh-dortmund.de/", "_blank");
  }

  public void onLogoNRWClicked()
  {
    logger.debug("onLogoNRWClicked()");
    Executions.getCurrent().sendRedirect("http://www.nrw.de/", "_blank");
  }

  public void onLogoEUClicked()
  {
    logger.debug("onLogoEUClicked()");
    Executions.getCurrent().sendRedirect("http://europa.eu/", "_blank");
  }

  public void onLogoBMGClicked()
  {
    logger.debug("onLogoBMGClicked()");
    Executions.getCurrent().sendRedirect("http://www.bmg.bund.de/", "_blank");
  }

  public void onLogoBMGATClicked()
  {
    logger.debug("onLogoBMGATClicked()");
    Executions.getCurrent().sendRedirect("http://www.bmgf.gv.at/", "_blank");
  }

  public void onLogoFHTWClicked()
  {
    logger.debug("onLogoFHTWClicked()");
    Executions.getCurrent().sendRedirect("http://www.technikum-wien.at/", "_blank");
  }

  public void onLogoELGAClicked()
  {
    logger.debug("onLogoELGAClicked()");
    Executions.getCurrent().sendRedirect("http://www.elga.gv.at/", "_blank");
  }

  public void gotoMainView()
  {
    redirect("/gui/main/main.zul", Labels.getLabel("menu.pleaseWait"), null);
  }

  public void viewActualProceedings()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
          "/gui/info/actualProceedings.zul",
          null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
          + "': " + ex.getMessage());
    }
  }

  public void onImpressumClicked()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
          "/gui/info/impressum.zul",
          null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
          + "': " + ex.getMessage());
    }
  }

  public void viewAssociationEditor()
  {
    if (SessionHelper.isUserLoggedIn())
      redirect("/gui/main/modules/AssociationEditor.zul", Labels.getLabel("menu.pleaseWait"), null);
    else
    {
      Messagebox.show(Labels.getLabel("menu.loginRequiredForAssociationEditor"));
    }
  }

  private void redirect(String Src, String WaitText, String Parameter)
  {
    if (WaitText.length() > 0)
    {
      Clients.showBusy(WaitText);
    }

    clearHeader();
    if (Src.equals("null"))
    {
      //PersonHelper.getInstance().freeData();
      ViewHelper.gotoSrc(null);
    }
    else
    {
      if (Parameter != null && Parameter.contains("extern_link"))
      {
        Executions.getCurrent().sendRedirect(Src, "_blank");
      } /*else if(Parameter != null && Parameter.contains("PARAMETER"))
       {
       map.put(value[1], 1);
            
       //logger.debug("Parameter entdeckt: " + value[1]);
       }*/ else
      {
        ViewHelper.gotoSrc(Src);
      }
    }
    //initHeader();
  }

  public static void openModalDialog(String Src, String Parameter)
  {
    try
    {
      Map map = null;
      boolean fehler = false;

      logger.debug("openModalDialog mit Param: " + Parameter);

      //if (Parameter != null && Parameter.length() > 0 && Parameter.contains("SESSION"))
      if (Parameter != null && Parameter.length() > 0)
      {
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        String[] param = Parameter.split(";");
        map = new HashMap();

        for (int i = 0; i < param.length; ++i)
        {
          String[] value = param[i].split(":");
          if (value.length > 1 && value[0].equals("SESSION_LONG"))
          {
            try
            {
              map.put(value[1], Long.parseLong(session.getAttribute(value[1]).toString()));

              logger.debug("Parameter (Long) entdeckt: " + value[1]);
            }
            catch (Exception e)
            {
              Messagebox.show(Labels.getLabel("menu.noSelection"));
              fehler = true;
            }
          }
          else if (value.length > 1 && value[0].equals("SESSION_STR"))
          {
            map.put(value[1], session.getAttribute(value[1]).toString());

            logger.debug("Parameter entdeckt: " + value[1]);
          }
          else if (value.length > 1 && value[0].equals("PARAMETER"))
          {
            map.put(value[1], 1);

            logger.debug("Parameter entdeckt: " + value[1]);
          }
        }
      }

      if (fehler == false)
      {
        Window win = (Window) Executions.createComponents(
            Src,
            null, map);
        win.setMaximizable(true); //TODO Manche module müssen Maximiert werden können! Bitte im Modul setMax false machen!
        win.doModal();
      }
    }
    catch (SuspendNotAllowedException ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
          + "': " + ex.getMessage());
    }
  }

  /**
   * Klick auf den "Über"-Button
   */
  public void onUeberClicked()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
          "/gui/info/about.zul",
          null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
          + "': " + ex.getMessage());
    }
  }

  public void login()
  {
    Authorization.login();
  }

  public void logout()
  {
    Authorization.logout();
  }

  public void changePassword()
  {
    Authorization.changePassword();
  }
  
  public void onCollabLogoutClicked()
  {
    LoginHelper.getInstance().logout();
  }
  

  public void showUADetails()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      //data.put("EditMode", PopupWindow.EDITMODE_DETAILSONLY);
      data.put("EditMode", 1);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupUserDetails.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void callAdmin()
  {
    Executions.getCurrent().sendRedirect("../../../TermAdmin/gui/admin/admin.zul");
  }

  public void callEnquiry()
  {
    /*Productive_AT_PU*************************************************************************************************************************/
    /**/ Executions.getCurrent().sendRedirect("../../../TermBrowser/gui/info/enquiry.zul");                           // test, kollab prod
      /**/ //Executions.getCurrent().sendRedirect("https://termcollab.gesundheit.gv.at/TermBrowser/gui/info/enquiry.zul");  // public prod
    /**
     * ***************************************************************************************************************************************
     */
  }

  public void callOidRegister()
  {
    //For AT!
    Executions.getCurrent().sendRedirect("https://www.gesundheit.gv.at/OID_Frontend/", "_blank");
  }

  public void onForwardCollab()
  {
    /*Productive_AT_PU*********************************************************************************************************************************/
    /**/ Executions.getCurrent().sendRedirect("https://termcollab.gesundheit.gv.at/TermBrowser/index.zul");
    /**
     * ***********************************************************************************************************************************************
     */
  }

  public void onForwardPub()
  {

    /*Productive_AT_PU**********************************************************************************************************************************/
    /**/ Executions.getCurrent().sendRedirect("https://termpub.gesundheit.gv.at/TermBrowser/index.zul");
    /**
     * ************************************************************************************************************************************************
     */
  }

  public void collaborationClicked()
  {
    //SessionHelper.switchCollaboration();
    //Executions.getCurrent().sendRedirect(null);  // Seite neu laden
    Executions.getCurrent().sendRedirect("/collaboration/login.zul");  // Seite neu laden
  }

  public void onNewProposalClicked()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("isExisting", false);
      data.put("source", new CodeSystem());
      
      Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  public void onDesktopClicked()
  {
    Executions.getCurrent().sendRedirect("/collaboration/desktop/main.zul");
  }

  /**
   * @return the headerStr
   */
  public String getHeaderStr()
  {
    return headerStr;
  }

  /**
   * @return the isCollaboration
   */
  public boolean isIsCollaboration()
  {
    return isCollaboration;
  }

  /**
   * @param isCollaboration the isCollaboration to set
   */
  public void setIsCollaboration(boolean isCollaboration)
  {
    this.isCollaboration = isCollaboration;
  }

  public boolean isNotCollaboration()
  {

    return !isCollaboration;
  }

}
