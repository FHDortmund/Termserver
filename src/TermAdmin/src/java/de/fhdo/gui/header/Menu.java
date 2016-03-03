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
import de.fhdo.helper.ViewHelper;
import java.util.HashMap;
import org.zkoss.zk.ui.event.EventListener;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 *
 * @author Robert Mützner
 */
public class Menu extends Window implements org.zkoss.zk.ui.ext.AfterCompose
//public class Menu extends GenericAutowireComposer
{

  //private static org.zkoss.zk.ui.Session session = Sessions.getCurrent();
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private String headerStr;
  transient EventListener onMenuitemClicked;

  public Menu()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] Konstruktor");
    /*Productive_AT_PU**********************************************************************/
    headerStr = Labels.getLabel("administration_area");  // TODO übersetzen
    /***************************************************************************************/
    
    
    //onMenuitemClicked = new OnClickListener(this);
  }

  public void afterCompose()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] afterCompose()");

    createMenu();
    initHeader();
  }

  private void clearHeader()
  {
  }

  public void onLogoutClicked()
  {
    Authorization.logout();
  }

  /*public void onBackToTermBrowser()
  {
    Executions.sendRedirect("../../../TermBrowser/gui/main/main.zul");
  }*/

  public void init()
  {
    //if (logger.isDebugEnabled())
    //  logger.debug("[Menu.java] init()");
    //initHeader();
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

  public void onLogoBRZClicked()
  {
    logger.debug("onLogoBRZClicked()");
    Executions.getCurrent().sendRedirect("http://www.brz.gv.at/", "_blank");
  }

  private void createMenu()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] createMenu()");


  }

  private void redirect(String Src, String WaitText, String Parameter)
  {
    if (WaitText.length() > 0)
      Clients.showBusy(WaitText);

    clearHeader();
    if (Src.equals("null"))
    {
      ViewHelper.gotoSrc(null);
    }
    else
    {
      if (Parameter != null && Parameter.contains("extern_link"))
      {
        Executions.getCurrent().sendRedirect(Src, "_blank");
      }
      else
        ViewHelper.gotoSrc(Src);
    }
    initHeader();
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
              Messagebox.show(Labels.getLabel("noSelection"));
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
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
              + "': " + ex.getMessage());
    }

  }

  private void removeAllChildren(Component Comp)
  {
    List childs = Comp.getChildren();
    if (childs != null)
      childs.clear();
  }

  private void initHeader()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] initHeader()");

  }

  public void onPatientlistClicked()
  {
    //clearHeader();
    //ViewHelper.gotoPatientlist();
    //initHeader();
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
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
              + "': " + ex.getMessage());
    }

  }

  /**
   * @return the headerStr
   */
  public String getHeaderStr()
  {
    return headerStr;
  }
}
