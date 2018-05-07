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
package de.fhdo.collaboration.desktop;

import de.fhdo.helper.SessionHelper;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class MainCollab extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public MainCollab()
  {
    /*if(SessionHelper.isAdmin() == false)
     {
        Executions.getCurrent().sendRedirect("../../TermBrowser/gui/main/main.zul");
     }*/
  }

  public void onNavigationSelect(SelectEvent event)
  {
    if (logger.isDebugEnabled())
      logger.debug("onNavigationSelect()");

    logger.debug("class: " + event.getReference().getClass().getCanonicalName());
    Tab tab = (Tab) event.getReference();
    tabSelected(tab.getId());
  }

  private void tabSelected(String ID)
  {
    if (ID == null || ID.length() == 0)
      return;

    if (ID.equals("tabProposal"))
    {
      includePage("incProposal", "/collaboration/desktop/desktop.zul");
    }
    else if (ID.equals("tabDiscGroup"))
    {
      includePage("incDiscGroup", "/collaboration/discgroup/discGroupMain.zul");
    }
    else logger.debug("ID nicht bekannt: " + ID);

    SessionHelper.setValue("collab_tabid", ID);
  }

  private void includePage(String ID, String Page)
  {
    try
    {
      Include inc = (Include) getFellow(ID);
      inc.setSrc(null);

      logger.debug("includePage: " + ID + ", Page: " + Page);
      inc.setSrc(Page);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void afterCompose()
  {
    String id = "";

    Object o = SessionHelper.getValue("collab_tabid");
    if (o != null)
      id = o.toString();

    if (id != null && id.length() > 0)
    {
      logger.debug("Goto Page: " + id);
      try
      {
        Tabbox tb = (Tabbox) getFellow("tabboxNavigation");
        //Tabpanel panel = (Tabpanel) getFellow("tabboxNavigation");
        Tab tab = (Tab) getFellow(id);
        int index = tab.getIndex();
        logger.debug("Index: " + index);

        tb.setSelectedIndex(index);

        tabSelected(id);
      }
      catch (Exception e)
      {
        tabSelected("tabProposal");
        logger.warn(e.getMessage());
      }
    }
    else
      tabSelected("tabProposal");
  }
}
