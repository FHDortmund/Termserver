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

import static de.fhdo.gui.main.ContentCSVSDefault.logger;
import de.fhdo.gui.main.modules.PopupCodeSystem;
import de.fhdo.gui.main.modules.PopupValueSet;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.ParameterHelper;
import de.fhdo.helper.SendBackHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.CodesystemGenericTreeModel;
import de.fhdo.models.ValuesetGenericTreeModel;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IGenericTreeActions;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class TreeAndContent extends Window implements AfterCompose, IGenericTreeActions, IUpdateModal
{

  protected static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  

  public enum MODE
  {

    CODESYSTEMS, VALUESETS, SEARCH
  }
  private MODE mode;

  protected Window parentSendBack;
  protected String sendbackMethodName = "";

  private GenericTree genericTreeCS = null;
  private GenericTree genericTreeVS = null;
  //private GenericList genericListSearch = null;

  private boolean allowEditing = false;

  public TreeAndContent()
  {
    logger.debug("TreeAndContent() - Konstruktor");

    Clients.showBusy(Labels.getLabel("common.loading"));

    // Lade Session-Werte
    mode = SessionHelper.getMainViewMode();
    allowEditing = SessionHelper.isUserLoggedIn();

  }

  public void afterCompose()
  {
    logger.debug("afterCompose()");

    setActiveTab();
    
    processURLParameter();

    Clients.clearBusy();

    // reload opened code system
    Object o = SessionHelper.getValue("selectedCS");
    if (o != null)
    {
      // TODO codesystem in Tree auswählen

      openCodeSystem((CodeSystem) o);
    }
    else
    {
      o = SessionHelper.getValue("selectedVS");
      if (o != null)
      {
        // TODO 
        //openCodeSystem((CodeSystem) o);
      }
    }
  }
  
  private void processURLParameter()
  {
    logger.debug("processURLParameter()");
    
    // sendBackValues
    SendBackHelper.getInstance().initialize();

    // West Layout (Auswahl der CS, VS, DV) unsichtbar machen, falls gew?scht                
    if (ParameterHelper.getBoolean("hideSelection") != null)
      ((West) getFellow("westTreeCSVSDV")).setVisible(!ParameterHelper.getBoolean("hideSelection"));

    // Menue ausblenden
    if (ParameterHelper.getBoolean("hideMenu") != null)
      ((North) this.getRoot().getFellow("blMainNorth")).setVisible(!ParameterHelper.getBoolean("hideMenu"));

    // Statusleiste ausblenden
    if (ParameterHelper.getBoolean("hideStatusbar") != null)
      ((South) this.getRoot().getFellow("blMainSouth")).setVisible(!ParameterHelper.getBoolean("hideStatusbar"));

    // Deep Links ausfuehren
    // TODO expandTreeAndLoadConceptsByDeeplink();
  }

  public void onFilterTabChanged()
  {
    logger.debug("onFilterTabChanged()");

    Tab selTab = ((Tabbox) getFellow("tabboxFilter")).getSelectedTab();

    if (selTab.getIndex() == 0)
    {
      mode = MODE.CODESYSTEMS;
      createTabContent_CS();
    }
    else if (selTab.getIndex() == 1)
    {
      mode = MODE.VALUESETS;
      createTabContent_VS();
    }
    else if (selTab.getIndex() == 2)
    {
      mode = MODE.SEARCH;
      createTabContent_Search();
    }

    logger.debug("save new mode in session: " + mode.name());
    SessionHelper.setMainViewMode(mode);
  }

  private void setActiveTab()
  {
    logger.debug("setActiveTab(), mode: " + mode.name());

    Tabbox tabboxFilter = (Tabbox) getFellow("tabboxFilter");

    if (mode == MODE.CODESYSTEMS)
    {
      tabboxFilter.setSelectedIndex(0);
    }
    else if (mode == MODE.VALUESETS)
    {
      tabboxFilter.setSelectedIndex(1);
    }
    else if (mode == MODE.SEARCH)
    {
      tabboxFilter.setSelectedIndex(2);
    }

    // load tab content
    onFilterTabChanged();
  }

  private void createTabContent_CS()
  {
    if (genericTreeCS != null)
      return;

    logger.debug("createTabContent_CS()");

    Include inc = (Include) getFellow("incTreeCS");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");
    genericTreeCS = (GenericTree) winGenericTree;

    CodesystemGenericTreeModel.getInstance().initGenericTree(genericTreeCS, this);
    genericTreeCS.setTreeId("codesystems");

    genericTreeCS.setButton_new(allowEditing);
    genericTreeCS.setButton_edit(allowEditing);
    genericTreeCS.setShowRefresh(true);

    genericTreeCS.removeCustomButtons();

    Button buttonDetails = new Button(Labels.getLabel("common.details"), "/rsc/img/list/magnifier.png");
    buttonDetails.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericTreeRowType row = (GenericTreeRowType) genericTreeCS.getSelection();
        if (row != null && row.getData() instanceof CodeSystem)
        {
          CodeSystem cs = (CodeSystem) row.getData();
          popupCodeSystem(cs, PopupCodeSystem.EDITMODES.DETAILSONLY, false);
        }
      }
    });

    genericTreeCS.addCustomButton(buttonDetails);

    // add context menu
    Menupopup menu = new Menupopup();
    menu.setParent(this);

    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("popupCodeSystem.editCodeSystem"));
    Menuitem miNew = new Menuitem(Labels.getLabel("popupCodeSystem.createCodeSystem"));
    // TODO Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    //Menuitem miRemoveVS = null;// = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));

    // set icons
    miDetails.setImage("/rsc/img/list/magnifier.png");
    miEdit.setImage("/rsc/img/list/pencil.png");
    miNew.setImage("/rsc/img/list/add.png");

    miDetails.setParent(menu);

    if (allowEditing)
    {
      new Menuseparator().setParent(menu);
      miNew.setParent(menu);
      miEdit.setParent(menu);
    }

    genericTreeCS.setContextMenu(menu);

    miDetails.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericTreeRowType row = (GenericTreeRowType) genericTreeCS.getSelection();
        if (row != null && row.getData() instanceof CodeSystem)
        {
          CodeSystem cs = (CodeSystem) row.getData();
          popupCodeSystem(cs, PopupCodeSystem.EDITMODES.DETAILSONLY, false);
        }
      }
    });
    
    miNew.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        popupCodeSystem(null, PopupCodeSystem.EDITMODES.CREATE, true);
      }
    });
    
    miEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericTreeRowType row = (GenericTreeRowType) genericTreeCS.getSelection();
        if (row != null && row.getData() instanceof CodeSystem)
        {
          CodeSystem cs = (CodeSystem) row.getData();
          if (allowEditing)
            popupCodeSystem(cs, PopupCodeSystem.EDITMODES.MAINTAIN, false);
        }
      }
    });
    
    if(CodesystemGenericTreeModel.getInstance().getErrorMessage() != null &&
            CodesystemGenericTreeModel.getInstance().getErrorMessage().length() > 0)
    {
      // show error message
      ComponentHelper.setVisible("incTreeCS", false, this);
      ComponentHelper.setVisible("message", true, this);
      
      ((Label)getFellow("labelMessage")).setValue(CodesystemGenericTreeModel.getInstance().getErrorMessage());
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

    genericTreeVS.setButton_new(allowEditing);
    genericTreeVS.setButton_edit(allowEditing);
    genericTreeVS.setShowRefresh(true);

    genericTreeVS.removeCustomButtons();

    Button buttonDetails = new Button(Labels.getLabel("common.details"), "/rsc/img/list/magnifier.png");
    buttonDetails.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericTreeRowType row = (GenericTreeRowType) genericTreeVS.getSelection();
        if (row != null && row.getData() instanceof ValueSet)
        {
          ValueSet vs = (ValueSet) row.getData();
          
          popupValueSet(vs, PopupValueSet.EDITMODES.DETAILSONLY, false);
        }
      }
    });

    genericTreeVS.addCustomButton(buttonDetails);

    // add context menu
    Menupopup menu = new Menupopup();
    menu.setParent(this);

    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("common.edit"));
    Menuitem miNew = new Menuitem(Labels.getLabel("contentCSVSDefault.newValueSet"));

    // set icons
    miDetails.setImage("/rsc/img/list/magnifier.png");
    miEdit.setImage("/rsc/img/list/pencil.png");
    miNew.setImage("/rsc/img/list/add.png");

    miDetails.setParent(menu);

    if (allowEditing)
    {
      new Menuseparator().setParent(menu);
      miNew.setParent(menu);
      miEdit.setParent(menu);
    }

    genericTreeVS.setContextMenu(menu);

    miDetails.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericTreeRowType row = (GenericTreeRowType) genericTreeVS.getSelection();
        
        if (row != null && row.getData() instanceof ValueSet)
        {
          ValueSet vs = (ValueSet) row.getData();
          popupValueSet(vs, PopupValueSet.EDITMODES.DETAILSONLY, false);
        }
      }
    });
    
    miNew.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        popupValueSet(null, PopupValueSet.EDITMODES.CREATE, true);
      }
    });
    
    miEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericTreeRowType row = (GenericTreeRowType) genericTreeVS.getSelection();
        
        if (row != null && row.getData() instanceof ValueSet)
        {
          ValueSet vs = (ValueSet) row.getData();
          if (allowEditing)
            popupValueSet(vs, PopupValueSet.EDITMODES.MAINTAIN, false);
        }
      }
    });
    
    /*if(CodesystemGenericTreeModel.getInstance().getErrorMessage() != null &&
            CodesystemGenericTreeModel.getInstance().getErrorMessage().length() > 0)
    {
      // show error message
      ComponentHelper.setVisible("incTreeCS", false, this);
      ComponentHelper.setVisible("message", true, this);
      
      ((Label)getFellow("labelMessage")).setValue(CodesystemGenericTreeModel.getInstance().getErrorMessage());
    }
    else
    {
      ComponentHelper.setVisible("message", false, this);
      ComponentHelper.setVisible("incTreeCS", true, this);
    }*/
    
    
  }

  private void createTabContent_Search()
  {
//    if (genericListSearch != null)
//      return;
//
//    logger.debug("createTabContent_Search()");

  }

  public void onTreeNewClicked(String id, Object data)
  {
    if (id != null && id.equals("codesystems"))
      popupCodeSystem(null, PopupCodeSystem.EDITMODES.CREATE, true);
    else if (id != null && id.equals("valuesets"))
      popupValueSet(null, PopupValueSet.EDITMODES.CREATE, true);
  }

  public void onTreeEditClicked(String id, Object data)
  {
    if (id != null && id.equals("codesystems"))
    {
      if (data instanceof CodeSystem)
      {
        // open selected CodeSystem
        CodeSystem cs = (CodeSystem) data;

        // open Codesystem
        if (allowEditing)
          popupCodeSystem(cs, PopupCodeSystem.EDITMODES.MAINTAIN, false);
        else
          popupCodeSystem(cs, PopupCodeSystem.EDITMODES.DETAILSONLY, false);
      }
    }
    else if (id != null && id.equals("valuesets"))
    {
      if (data instanceof ValueSet)
      {
        // open selected CodeSystem
        ValueSet vs = (ValueSet) data;

        // open Codesystem
        if (allowEditing)
          popupValueSet(vs, PopupValueSet.EDITMODES.MAINTAIN, false);
        else
          popupValueSet(vs, PopupValueSet.EDITMODES.DETAILSONLY, false);
      }
    }

  }
  
  public void onTreeRefresh(String id)
  {
    if (id == null)
      return;
    
    if (id.equals("codesystems"))
    {
      genericTreeCS = null;
      CodesystemGenericTreeModel.getInstance().reloadData();

      createTabContent_CS();
    }
    else if (id.equals("valuesets"))
    {
      genericTreeVS = null;
      ValuesetGenericTreeModel.getInstance().reloadData();

      createTabContent_VS();
    }
  }

  public boolean onTreeDeleted(String id, Object data)
  {
    return true;
  }

  public void onTreeSelected(String id, Object data)
  {
    if (id == null)
      return;

    if (id.equals("codesystems"))
    {
      if (data instanceof CodeSystem)
      {
        // open selected CodeSystem
        CodeSystem cs = (CodeSystem) data;

        // open Codesystem
        openCodeSystem(cs);
      }
      else if (data instanceof DomainValue)
      {
        // open or close node
        logger.debug("open node...");
        if (genericTreeCS != null)
        {
          Treeitem ti = genericTreeCS.getTree().getSelectedItem();
          if (ti != null)
          {
            DefaultTreeModel treeModel = (DefaultTreeModel) genericTreeCS.getTree().getModel();
            TreeNode treeNode = (TreeNode) ti.getAttribute("treenode");
            if (treeNode.isLeaf() == false)
            {
              if (ti.isOpen())
              {
                treeModel.removeOpenPath(treeModel.getPath(treeNode));
              }
              else
              {
                treeModel.addOpenPath(treeModel.getPath(treeNode));
              }
            }
          }
        }
      }
    }
    else if (id.equals("valuesets"))
    {
      if (data instanceof ValueSet)
      {
        // open selected CodeSystem
        ValueSet vs = (ValueSet) data;

        // open Codesystem
        openValueSet(vs);
      }
    }

  }

  private void openCodeSystem(CodeSystem cs)
  {
    if (cs == null)
      return;

    logger.debug("openCodeSystem with id: " + cs.getId());

    // check if Codesystem already loaded
    /*Object o = SessionHelper.getValue("selectedCS");
     if (o != null)
     {
     CodeSystem cs_session = (CodeSystem) o;

     if (cs_session.getId().longValue() == cs.getId().longValue())
     {
     // already loaded
     logger.debug("already loaded");
     return;
     }
     }*/
    // remember choice
    SessionHelper.setValue("selectedCS", cs);
    SessionHelper.setValue("selectedVS", null);

    // open Codesystem
    Clients.showBusy(Labels.getLabel("common.loading"));

    Include inc = (Include) getFellow("incConcepts");
    inc.setSrc(null);  // force to reload

    // set propierties
    inc.clearDynamicProperties();
    inc.setDynamicProperty("codeSystem", cs);

    /*inc.setDynamicProperty("parent", this);
     inc.setDynamicProperty("source", source);
     inc.setDynamicProperty("id", id);
     inc.setDynamicProperty("name", name);
     inc.setDynamicProperty("versionId", versionId);
     inc.setDynamicProperty("versionName", versionName);
     inc.setDynamicProperty("validityRange", validityRangeDT);
     inc.setDynamicProperty("contentMode", contentMode);
     inc.setDynamicProperty("droppable", droppable);
     inc.setDynamicProperty("draggable", draggable);*/
    inc.setSrc("content/ContentConcepts.zul");

    setTitleCenter();
  }

  private void openValueSet(ValueSet vs)
  {
    if (vs == null)
      return;

    logger.debug("openValueSet with id: " + vs.getId());

    // remember choice
    SessionHelper.setValue("selectedCS", null);
    SessionHelper.setValue("selectedVS", vs);

    // open Codesystem
    Clients.showBusy(Labels.getLabel("common.loading"));

    Include inc = (Include) getFellow("incConcepts");
    inc.setSrc(null);  // force to reload

    // set propierties
    inc.clearDynamicProperties();
    inc.setDynamicProperty("valueSet", vs);

    /*inc.setDynamicProperty("parent", this);
     inc.setDynamicProperty("source", source);
     inc.setDynamicProperty("id", id);
     inc.setDynamicProperty("name", name);
     inc.setDynamicProperty("versionId", versionId);
     inc.setDynamicProperty("versionName", versionName);
     inc.setDynamicProperty("validityRange", validityRangeDT);
     inc.setDynamicProperty("contentMode", contentMode);
     inc.setDynamicProperty("droppable", droppable);
     inc.setDynamicProperty("draggable", draggable);*/
    inc.setSrc("content/ContentConcepts.zul");

    setTitleCenter();
  }

  private void setTitleCenter()
  {
    Center center = (Center) getFellow("center");

    Object o = SessionHelper.getValue("selectedCS");
    if (o != null)
    {
      CodeSystem cs = (CodeSystem) o;
      center.setTitle(cs.getName());
    }
    else
    {
      o = SessionHelper.getValue("selectedVS");
      if (o != null)
      {
        ValueSet vs = (ValueSet) o;
        center.setTitle(vs.getName());
      }
    }
  }

  public void popupCodeSystem(CodeSystem codeSystem, PopupCodeSystem.EDITMODES mode, boolean showVersion)
  {
    logger.debug("popupCodeSystem, mode: " + mode + ", showVersion: " + showVersion);
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("CS", codeSystem);
      if (showVersion)
        data.put("CSV", new CodeSystemVersion());
      data.put("EditMode", mode);

      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupCodeSystem.zul", this, data);
      ((PopupCodeSystem) w).setUpdateListener(this);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }
  
  public void popupValueSet(ValueSet valueSet, PopupValueSet.EDITMODES mode, boolean showVersion)
  {
    logger.debug("popupValueSet, mode: " + mode + ", showVersion: " + showVersion);
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("VS", valueSet);
      if (showVersion)
        data.put("VSV", new ValueSetVersion());
      data.put("EditMode", mode);

      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupValueSet.zul", this, data);
      ((PopupValueSet) w).setUpdateListener(this);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  public void update(Object o, boolean edited)
  {
    // update data from modal detail dialogs
    if (o != null && o instanceof CodeSystem)
    {
      CodeSystem cs = (CodeSystem) o;
      GenericTreeRowType row = CodesystemGenericTreeModel.getInstance().createTreeNode(cs);

      if (edited)
      {
        genericTreeCS.updateEntry(row);
        CodesystemGenericTreeModel.getInstance().reloadData();
      }
      else
      {
        // reload tree
        genericTreeCS = null;
        CodesystemGenericTreeModel.getInstance().reloadData();

        createTabContent_CS();
      }
    }
    else if (o != null && o instanceof ValueSet)
    {
      ValueSet vs = (ValueSet) o;
      GenericTreeRowType row = ValuesetGenericTreeModel.getInstance().createTreeNode(vs);

      if (edited)
      {
        genericTreeVS.updateEntry(row);
        ValuesetGenericTreeModel.getInstance().reloadData();
      }
      else
      {
        // reload tree
        genericTreeVS = null;
        ValuesetGenericTreeModel.getInstance().reloadData();

        createTabContent_VS();
      }
    }
  }

}
