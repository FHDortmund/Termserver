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
import org.hibernate.util.StringHelper;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Main extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public Main()
  {
  }
  
  public void afterCompose()
  {
    // load content
    initTabSelection();
    
  }
  
  private void initTabSelection()
  {
    String collabMode = SessionHelper.getStringValue("collab_mode");
    logger.debug("initTabSelection, collabMode: " + collabMode);
    
    if(collabMode.length() == 0)
      collabMode = "tabDesktop"; // default view
      
    Tabbox tabbox = (Tabbox)getFellow("tabboxNavigation");
    tabbox.setSelectedTab(((Tab)getFellow(collabMode)));
    
    onNavigationSelect();
  }
  
  
  public void onNavigationSelect()
  {
    logger.debug("onNavigationSelect");
    
    Tabbox tabbox = (Tabbox)getFellow("tabboxNavigation");
    if(tabbox.getSelectedTab() != null)
    {
      String id = tabbox.getSelectedTab().getId();
      SessionHelper.setValue("collab_mode", id);
      
      String incId = id.replaceFirst("tab", "inc");
      Include inc = (Include) getFellow(incId);
      
      if(inc.getSrc() == null || inc.getSrc().length() == 0)
      {
        // init/load include
        if(inc.getId().equals("incDesktop"))
          inc.setSrc("desktop.zul");
        else if(inc.getId().equals("incProposals"))
          inc.setSrc("proposals.zul?mode=all");
        else if(inc.getId().equals("incMyProposals"))
          inc.setSrc("proposals.zul?mode=mine");
      }
      else
      {
        // reload ?
      }
      
    }
  }

  
  
  
}
