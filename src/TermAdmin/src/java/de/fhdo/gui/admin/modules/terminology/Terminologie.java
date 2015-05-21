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
package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.helper.SessionHelper;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Terminologie extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Tabbox tb;

  public Terminologie()
  {

  }

  public void afterCompose()
  {
    String id = "";

    Object o = SessionHelper.getValue("termadmin_terminologie_tabid");
    if (o != null)
      id = o.toString();
    tb = (Tabbox) getFellow("tabboxNavigation");
    //Tabpanel panel = (Tabpanel) getFellow("tabboxNavigation");
    if (id != null && id.length() > 0)
    {
      logger.debug("Goto Page: " + id);
      try
      {
        Tab tab = (Tab) getFellow(id);
        int index = tab.getIndex();
        logger.debug("Index: " + index);

        tb.setSelectedIndex(index);

        tabSelected(id);
      }
      catch (Exception e)
      {
        tabSelected("tabMetaVok");
        logger.warn(e.getMessage());
      }
    }
    else
    {

      tabSelected("tabMetaVok");
    }

    Tabs tabs = tb.getTabs();
    List<Component> tabList = tabs.getChildren();

    // TODO Kollaboration hier rausnehmen bzw. anders abfragen
    //if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
    if(SessionHelper.isAdmin())
    {
      for (Component c : tabList)
      {

        if (c.getId().equals("tabBenutzer"))
          c.setVisible(true);

        if (c.getId().equals("tabLizenzen"))
          c.setVisible(true);

        if (c.getId().equals("tabTaxonomie"))
          c.setVisible(true);

        if (c.getId().equals("tabDomains"))
          c.setVisible(true);

        if (c.getId().equals("tabSysPara"))
          c.setVisible(true);

        if (c.getId().equals("tabDatenbank"))
          c.setVisible(true);
      }
    }
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

    if (ID.equals("tabBenutzer"))
    {
      includePage("incBenutzer", "/gui/admin/modules/terminology/user/userTab.zul");
    }
    
    else if (ID.equals("tabMetaVok"))
    {
      includePage("incMetaVok", "/gui/admin/modules/metadatenCS.zul");
    }
    else if (ID.equals("tabMetaVal"))
    {
      includePage("incMetaVal", "/gui/admin/modules/metadatenVS.zul");
    }
    else if (ID.equals("tabDomains"))
    {
      includePage("incDomains", "/gui/admin/modules/domain.zul");
    }
    else if (ID.equals("tabSysPara"))
    {
      includePage("incSysPara", "/gui/admin/modules/sysParam.zul");
    }
    else if (ID.equals("tabRepoTerm"))
    {
      includePage("incRepoTerm", "/gui/admin/modules/reportingTerm.zul");
    }
    
    else
      logger.debug("ID nicht bekannt: " + ID);

    SessionHelper.setValue("termadmin_terminologie_tabid", ID);
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

}
