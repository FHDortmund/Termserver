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
package de.fhdo.gui.main.modules;

import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.list.IDoubleClick;
import de.fhdo.models.CodesystemGenericTreeModel;
import de.fhdo.models.ValuesetGenericTreeModel;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IGenericTreeActions;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ChooseCodesystem extends Window implements AfterCompose, IGenericTreeActions
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdate updateListener;
  
  private GenericTree genericTreeCS = null;
  private GenericTree genericTreeVS = null;

  public ChooseCodesystem()
  {
    
  }

  public void afterCompose()
  {
    createTabContent_CS();
    createTabContent_VS();
  }
  
  private void createTabContent_CS()
  {
    if (genericTreeCS != null)
      return;

    logger.debug("createTabContent_CS()");

    Include inc = (Include) getFellow("incTreeCS");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");
    genericTreeCS = (GenericTree) winGenericTree;

    int count = CodesystemGenericTreeModel.getInstance().initGenericTree(genericTreeCS, this);
    genericTreeCS.setTreeId("codesystems");
    logger.debug("Count: " + count);

    genericTreeCS.setButton_new(false);
    //genericTreeCS.setShowRefresh(true);
    genericTreeCS.setAutoExpandAll(count <= PropertiesHelper.getInstance().getExpandTreeAutoCount());
    

    genericTreeCS.removeCustomButtons();

    if (CodesystemGenericTreeModel.getInstance().getErrorMessage() != null
            && CodesystemGenericTreeModel.getInstance().getErrorMessage().length() > 0)
    {
      // show error message
      ComponentHelper.setVisible("incTreeCS", false, this);
      ComponentHelper.setVisible("message", true, this);

      ((Label) getFellow("labelMessage")).setValue(CodesystemGenericTreeModel.getInstance().getErrorMessage());
    }
    else
    {
      ComponentHelper.setVisible("message", false, this);
      ComponentHelper.setVisible("incTreeCS", true, this);
    }
  }

  private void createTabContent_VS()
  {
    if (genericTreeVS != null)
      return;

    logger.debug("createTabContent_VS()");

    Include inc = (Include) getFellow("incTreeVS");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");
    genericTreeVS = (GenericTree) winGenericTree;

    ValuesetGenericTreeModel.getInstance().initGenericTree(genericTreeVS, this);
    genericTreeVS.setTreeId("valuesets");

    genericTreeVS.setButton_new(false);
    //genericTreeVS.setShowRefresh(true);

    genericTreeVS.removeCustomButtons();
  }
  
  public void onOkClicked()
  {
    if(updateListener != null)
    {
      int sel = ((Tabbox) getFellow("tabboxFilter")).getSelectedIndex();
      if(sel == 0)
      {
        updateListener.update(((GenericTreeRowType)genericTreeCS.getSelection()).getData());
      }
      else if(sel == 1)
      {
        updateListener.update(((GenericTreeRowType)genericTreeVS.getSelection()).getData());
      }
      this.detach();
    }
  }
  
  public void onCancelClicked()
  {
    this.detach();
  }
  
  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdate updateListener)
  {
    this.updateListener = updateListener;
  }

  public void onTreeNewClicked(String id, Object data)
  {
  }

  public void onTreeEditClicked(String id, Object data)
  {
    // double clicked
    if(updateListener != null)
    {
      updateListener.update(data);
      this.detach();
    }
  }

  public boolean onTreeDeleted(String id, Object data)
  {
    return true;
  }

  public void onTreeSelected(String id, Object data)
  {
    ((Button)getFellow("buttonOk")).setDisabled(data == null);
  }

  public void onTreeRefresh(String id)
  {
  }

  

  
  
}
