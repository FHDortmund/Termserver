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
import de.fhdo.gui.main.modules.PopupCodeSystem;
import de.fhdo.gui.main.modules.PopupValueSet;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.ParameterHelper;
import de.fhdo.helper.PropertiesHelper;
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
import org.hibernate.util.StringHelper;
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

  private boolean externMode = false;

  public enum LOADTYPE
  {

    NONE, CODESYSTEM, VALUESET
  }
  private LOADTYPE paramLoadType = LOADTYPE.NONE;
  //private String paramLoadType = "";
  private String paramLoadName = "";
  private String paramLoadOid = "";
  private long paramLoadId = 0;
  private Boolean paramHideSelection = null;
  private Boolean paramHideStatusbar = null;
  private Boolean paramHideMenu = null;

  public TreeAndContent()
  {
    logger.debug("TreeAndContent() - Konstruktor");

    Clients.showBusy(Labels.getLabel("common.loading"));

    // get window parameter
    getURLParameter();

    // Lade Session-Werte
    mode = SessionHelper.getMainViewMode();

    String sessionIdParameter = ArgumentHelper.getWindowParameterString("sessionId");

    if (sessionIdParameter != null && sessionIdParameter.length() > 0)
    {
      // try login
      logger.debug("Try login with sessionId...");
      Authorization.authenticate(sessionIdParameter);
    }

    String username = ArgumentHelper.getWindowParameterString("usr");
    String password = ArgumentHelper.getWindowParameterString("pw");

    if (username.length() > 0 && password.length() > 0)
    {
      // try login
      logger.debug("Try login...");

      Authorization.login(username, password);
      //WebServiceHelper.login(null)

    }

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

  private void getURLParameter()
  {
    logger.debug("TreeAndContent.java - getURLParameter()");

    paramHideSelection = ParameterHelper.getBoolean("hideSelection");
    paramHideMenu = ParameterHelper.getBoolean("hideMenu");
    paramHideStatusbar = ParameterHelper.getBoolean("hideStatusbar");

    String loadType = ParameterHelper.getString("loadType");
    paramLoadName = ParameterHelper.getString("loadName");
    paramLoadId = ParameterHelper.getLong("loadId");
    paramLoadOid = ParameterHelper.getString("loadOID");

    paramLoadType = LOADTYPE.NONE;
    if (loadType != null)
    {
      if (loadType.equalsIgnoreCase("CodeSystem") || loadType.equalsIgnoreCase("CS"))
      {
        paramLoadType = LOADTYPE.CODESYSTEM;
      }
      else if (loadType.equalsIgnoreCase("ValueSet") || loadType.equalsIgnoreCase("VS"))
      {
        paramLoadType = LOADTYPE.VALUESET;
      }
    }

    logger.debug("paramHideSelection: " + paramHideSelection);
    logger.debug("paramHideMenu: " + paramHideMenu);
    logger.debug("paramHideStatusbar: " + paramHideStatusbar);

    logger.debug("paramLoadType: " + paramLoadType);
    logger.debug("paramLoadName: " + paramLoadName);
    logger.debug("paramLoadId: " + paramLoadId);
    logger.debug("paramLoadOid: " + paramLoadOid);

    if (paramLoadId > 0
        || (paramLoadType != null && paramLoadType != LOADTYPE.NONE)
        || (paramLoadName != null && paramLoadName.length() > 0)
        || (paramLoadOid != null && paramLoadOid.length() > 0))
    {
      externMode = true;
    }
    logger.debug("externMode: " + externMode);
  }

  private void processURLParameter()
  {
    logger.debug("processURLParameter()");

    // sendBackValues
    //SendBackHelper.getInstance().initialize();
    // West Layout (Auswahl der CS, VS, DV) unsichtbar machen, falls gewünscht
    if (paramHideSelection != null)
      ((West) getFellow("westTreeCSVSDV")).setVisible(!paramHideSelection);

    // Menue ausblenden
    if (paramHideMenu != null)
      ((North) this.getRoot().getFellow("blMainNorth")).setVisible(!paramHideMenu);

    // Statusleiste ausblenden
    if (paramHideStatusbar != null)
      ((South) this.getRoot().getFellow("blMainSouth")).setVisible(!paramHideStatusbar);

    if (externMode)
    {
      // load codesystem/valueset and save in session
      if (paramLoadType == LOADTYPE.CODESYSTEM)
      {
        // load codesystem
        CodeSystem cs = CodesystemGenericTreeModel.getInstance().findCodeSystem(paramLoadName, paramLoadOid, paramLoadId);
        SessionHelper.setValue("selectedCS", cs);
      }
      else if (paramLoadType == LOADTYPE.VALUESET)
      {
        // load valueset
        // TODO load valueset
      }
    }

    /*// Get Parameter by URL
    

     if (type == null || (name == null && loadId == null))
     return;

     logger.debug("expandTreeAndLoadConceptsByDeeplink");

     Tree treeActive = null;

     boolean isCodesystem = true;
     if (type.equalsIgnoreCase("CodeSystem"))
     {
     tabboxFilter.setSelectedTab(tabCS);
     treeActive = treeCS;
     }
     else if (type.equalsIgnoreCase("ValueSet"))
     {
     tabboxFilter.setSelectedTab(tabVS);
     isCodesystem = false;
     treeActive = treeVS;
     }
     else
     return;

     setActiveTab();

     // DeepLink Map erstellen für Kapitel
     // Muss vor selectTreeitemByName erstellt werden damit deepLinks im Konstruktor von ContetnConcepts verwendet werden kann
     int i = 1;
     String chapter = getDesktop().getExecution().getParameter("c" + i);
     while (chapter != null && chapter.isEmpty() == false)
     {
     deepLinks.add(chapter);
     i++;
     chapter = getDesktop().getExecution().getParameter("c" + i);
     }

     if (isCodesystem)
     expandTreeCS();
     else
     expandTreeVS();

     if (name != null && name.length() > 0)
     selectTreeitemByName(treeActive.getTreechildren(), name);
     else if (loadId != null && loadId.length() > 0)
     {
     try
     {
     long id = 0;
     id = Long.parseLong(loadId);
     selectTreeitemById(treeActive.getTreechildren(), id);
     }
     catch (Exception ex)
     {
     logger.warn("Fehler beim Laden des Codesystems: " + ex.getLocalizedMessage());
     }
     }*/
    // Deep Links ausfuehren
    // TODO expandTreeAndLoadConceptsByDeeplink();
  }

  public void onFilterTabChanged()
  {
    logger.debug("onFilterTabChanged()");

    Tab selTab = ((Tabbox) getFellow("tabboxFilter")).getSelectedTab();

    //if (selTab.getIndex() == 0)
    if (selTab.getId().equals("tabCS"))
    {
      mode = MODE.CODESYSTEMS;
      createTabContent_CS();
    }
    else if (selTab.getId().equals("tabVS"))
    {
      mode = MODE.VALUESETS;
      createTabContent_VS();
    }
    else if (selTab.getId().equals("tabSearch"))
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
    
    getFellow("tabCS").setVisible(PropertiesHelper.getInstance().isGuiShowCodesystems());
    getFellow("tabVS").setVisible(PropertiesHelper.getInstance().isGuiShowValuesets());

    if (mode == MODE.CODESYSTEMS)
    {
      //tabboxFilter.setSelectedIndex(0);
      tabboxFilter.setSelectedTab((Tab)getFellow("tabCS"));
    }
    else if (mode == MODE.VALUESETS)
    {
      //tabboxFilter.setSelectedIndex(1);
      tabboxFilter.setSelectedTab((Tab)getFellow("tabVS"));
    }
    
    // load tab content
    onFilterTabChanged();
  }

  private void createTabContent_CS()
  {
    if (genericTreeCS != null || externMode)
      return;

    logger.debug("createTabContent_CS()");

    Include inc = (Include) getFellow("incTreeCS");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");
    genericTreeCS = (GenericTree) winGenericTree;

    int count = CodesystemGenericTreeModel.getInstance().initGenericTree(genericTreeCS, this);
    genericTreeCS.setTreeId("codesystems");
    logger.debug("Count: " + count);

    genericTreeCS.setButton_new(allowEditing && PropertiesHelper.getInstance().isGuiEditCodesystemsShowNew());
    genericTreeCS.setButton_edit(allowEditing && PropertiesHelper.getInstance().isGuiEditCodesystemsShowEdit());
    genericTreeCS.setShowRefresh(true);
    genericTreeCS.setAutoExpandAll(count <= PropertiesHelper.getInstance().getExpandTreeAutoCount());

    genericTreeCS.removeCustomButtons();

    if (PropertiesHelper.getInstance().isGuiEditCodesystemsShowDetails())
    {
      Button buttonDetails = new Button(Labels.getLabel("common.details"), "/rsc/img/design/details_16x16.png");
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
    }

    // add context menu
    Menupopup menu = new Menupopup();
    menu.setParent(this);

    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("popupCodeSystem.editCodeSystem"));
    Menuitem miNew = new Menuitem(Labels.getLabel("popupCodeSystem.createCodeSystem"));
    // TODO Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    //Menuitem miRemoveVS = null;// = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));

    // set icons
    miDetails.setImage("/rsc/img/design/details_16x16.png");
    miEdit.setImage("/rsc/img/design/edit_16x16.png");
    miNew.setImage("/rsc/img/design/add_16x16.png");

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

//    logger.debug("Count: " + count);
//    logger.debug("Expand till: " + PropertiesHelper.getInstance().getExpandTreeAutoCount());
//    
//    if(count <= PropertiesHelper.getInstance().getExpandTreeAutoCount())
//    {
//      genericTreeCS.expandAll();
//    }
  }

  private void createTabContent_VS()
  {
    if (genericTreeVS != null || externMode)
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

    Button buttonDetails = new Button(Labels.getLabel("common.details"), "/rsc/img/design/details_16x16.png");
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
    miDetails.setImage("/rsc/img/design/details_16x16.png");
    miEdit.setImage("/rsc/img/list/pencil.png");
    miNew.setImage("/rsc/img/design/add_16x16.png");

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
//    if(count <= PropertiesHelper.getInstance().getExpandTreeAutoCount())
//    {
//      genericTreeVS.expandAll();
//    }
  }

  private void createTabContent_Search()
  {
//    if (genericListSearch != null || externMode)
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
    inc.setDynamicProperty("parent", this);

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
    inc.setDynamicProperty("parent", this);

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

  public void setTitleCenter()
  {
    Center center = (Center) getFellow("center");

    Object o = SessionHelper.getValue("selectedCS");
    String name = "";
    if (o != null)
    {
      CodeSystem cs = (CodeSystem) o;
      name = cs.getName();

      Object oVersion = SessionHelper.getValue("selectedCSV");
      if (oVersion != null && oVersion instanceof CodeSystemVersion)
      {
        CodeSystemVersion csv = (CodeSystemVersion) oVersion;
        if (StringHelper.isNotEmpty(csv.getName()))
        {
          name += " - " + csv.getName();
          if (StringHelper.isNotEmpty(csv.getOid()))
          {
            name += " (" + csv.getOid() + ")";
          }
        }
      }
    }
    else
    {
      o = SessionHelper.getValue("selectedVS");
      if (o != null)
      {
        ValueSet vs = (ValueSet) o;
        name = vs.getName();
      }
    }

    center.setTitle(name);
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
