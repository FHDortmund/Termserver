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
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.helper.CODES;
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
public class Kollaboration extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Tabbox tb;
  public Kollaboration()
  {
    
  }
  
  public void afterCompose()
  {
    String id = "";
    tb = (Tabbox) getFellow("tabboxNavigation");
    Object o = SessionHelper.getValue("termadmin_kollaboration_tabid");
    if (o != null)
      id = o.toString();

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
        tabSelected("tabRepKollab"); 
        logger.warn(e.getMessage());
      }
    }
    else{

        tabSelected("tabRepKollab");
    }
    Tabs tabs = tb.getTabs();
    List<Component> tabList = tabs.getChildren();
    
    if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN)){
    
        for(Component c:tabList){
        
            if(c.getId().equals("tabBenutzer"))
                c.setVisible(true);
            
            if(c.getId().equals("tabAnfragen"))
                c.setVisible(true);
            
            if(c.getId().equals("tabSvAssignment"))
                c.setVisible(true);
            
            if(c.getId().equals("tabWorkflow"))    
                c.setVisible(true);
            
            if(c.getId().equals("tabDomain"))    
                c.setVisible(true);
            
            if(c.getId().equals("tabSysParam"))
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
      includePage("incBenutzer", "/gui/admin/modules/collaboration/benutzer.zul");
    }
    else if (ID.equals("tabAnfragen"))
    {
      includePage("incAnfragen", "/gui/admin/modules/collaboration/anfragen.zul");
    }
    else if (ID.equals("tabSvAssignment"))
    {
      includePage("incSvAssignment", "/gui/admin/modules/collaboration/svassignment.zul");
    }
    else if (ID.equals("tabWorkflow"))
    {
      includePage("incWorkflow", "/gui/admin/modules/collaboration/workflow.zul");
    }
    else if (ID.equals("tabDomain"))
    {
      includePage("incDomain", "/gui/admin/modules/collaboration/domain.zul");
    }
    else if (ID.equals("tabSysParam"))
    {
      includePage("incSysParam", "/gui/admin/modules/collaboration/sysParam.zul");
    }
    else if (ID.equals("tabRepKollab"))
    {
      includePage("incRepKollab", "/gui/admin/modules/collaboration/reportingKollab.zul");
    }
    else logger.debug("ID nicht bekannt: " + ID);

    SessionHelper.setValue("termadmin_kollaboration_tabid", ID);
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
