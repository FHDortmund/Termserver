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

import de.fhdo.Definitions;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.gui.main.modules.PopupWindow;
import de.fhdo.helper.DeepLinkHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.ParameterHelper;
import de.fhdo.helper.SendBackHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.TreeHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.TreeModel;
import de.fhdo.models.TreeModelCS;
import de.fhdo.models.TreeModelCSEV;
import de.fhdo.models.TreeModelVS;
import de.fhdo.models.TreeNode;
import de.fhdo.models.comparators.ComparatorCsvVsv;
import de.fhdo.models.itemrenderer.TreeitemRenderer_CS_VS_DV;
import de.fhdo.terminologie.ws.authoring.DeleteInfo;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treecol;
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
 * @author Sven Becker, Robert Muetzner
 */
public class ContentCSVSDefault extends Window implements AfterCompose
{

  public enum MODE
  {

    CODESYSTEMS, VALUESETS, SEARCH
  }
  private MODE mode;

  protected static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  protected CodeSystem selectedCS;
  protected CodeSystemVersion selectedCSV;
  protected ValueSet selectedVS;
  protected ValueSetVersion selectedVSV;
  protected String filterString = "";
  protected Tree treeVS,
          treeCS,
          treeSearch;
  //treeActive;
  protected ContentConcepts windowContentConcepts;
  protected Button bNewCS,
          bNewCSV, bEditCSV, bDetailsCSV,
          bNewVS, bNewVSV, bEditVSV, bDetailsVSV,
          bEditSearch, bDetailsSearch;
  //bActiveDetails, bActiveEdit, bActiveNew, bActiveNewVersion;
  //bGlobSearchCSV, bGlobSearchVSV, bActiveGlobalSearch;
  protected Tabbox tabboxFilter;
  protected Tab tabCS, tabVS, tabSearch;//, tabActive;
  protected ArrayList<String> deepLinks = new ArrayList<String>();
  protected Window parentSendBack;
  protected String sendbackMethodName = "";
  protected West westTreeCSVSDV;
  private boolean cs_loaded = false;
  private boolean vs_loaded = false;
  private boolean search_loaded = false;

  public ContentCSVSDefault()
  {
    mode = SessionHelper.getViewMode();

  }

// Konstruktor /////////////////////////////////////////////////////////////////    
  @Override
  public void afterCompose()
  {
    // get references
    tabboxFilter = (Tabbox) getFellow("tabboxFilter");
    tabCS = (Tab) getFellow("tabCS");
    tabVS = (Tab) getFellow("tabVS");
    tabSearch = (Tab) getFellow("tabSearch");

    // Codesystem-Buttons
    bNewCS = (Button) getFellow("bNewCS");
    bNewCSV = (Button) getFellow("bNewCSV");
    bEditCSV = (Button) getFellow("bEditCSV");
    bDetailsCSV = (Button) getFellow("bDetailsCSV");

    // Valueset-Buttons
    bNewVS = (Button) getFellow("bNewVS");
    bNewVSV = (Button) getFellow("bNewVSV");
    bEditVSV = (Button) getFellow("bEditVSV");
    bDetailsVSV = (Button) getFellow("bDetailsVSV");

    bDetailsSearch = (Button) getFellow("bDetailsSearch");
    bEditSearch = (Button) getFellow("bEditSearch");

    // Tree
    treeCS = (Tree) getFellow("treeCS");
    treeVS = (Tree) getFellow("treeVS");

//        ((Borderlayout)getRoot()).getCenter().setTitle(Labels.getLabel("common.mainView"));                
    // set button visibility
    boolean bLoggedIn = SessionHelper.isUserLoggedIn();

    bNewCS.setVisible(bLoggedIn);
    bNewCSV.setVisible(bLoggedIn);
    bEditCSV.setVisible(bLoggedIn);

    bNewVS.setVisible(bLoggedIn);
    bNewVSV.setVisible(bLoggedIn);
    bEditVSV.setVisible(bLoggedIn);

    bEditSearch.setVisible(bLoggedIn);

    // Setzt den ersten Tab als aktiv
    setActiveTab();

    processURLParameter();

    Clients.clearBusy();
  }

  private void processURLParameter()
  {
    // sendBackValues
    SendBackHelper.getInstance().initialize();

    // West Layout (Auswahl der CS, VS, DV) unsichtbar machen, falls gew?scht                
    if (ParameterHelper.getBoolean("hideSelection") != null)
      ((West) getFellow("westTreeCSVSDV")).setVisible(!ParameterHelper.getBoolean("hideSelection"));

    // Men� ausblenden
    if (ParameterHelper.getBoolean("hideMenu") != null)
      ((North) this.getRoot().getFellow("blMainNorth")).setVisible(!ParameterHelper.getBoolean("hideMenu"));

    // Statusleiste ausblenden
    if (ParameterHelper.getBoolean("hideStatusbar") != null)
      ((South) this.getRoot().getFellow("blMainSouth")).setVisible(!ParameterHelper.getBoolean("hideStatusbar"));

    // Deep Links ausfuehren
    expandTreeAndLoadConceptsByDeeplink();
  }

  // Tree CodeSystem /////////////////////////////////////////////////////////////        
  protected void createCSTree()
  {
    if (cs_loaded)
      return;

    logger.debug("createCSTree()");

    try
    {
      Clients.showBusy(Labels.getLabel("common.loading"));

      treeCS.setModel(TreeModelCS.getTreeModel(getDesktop()));
      treeCS.setItemRenderer(new TreeitemRenderer_CS_VS_DV(this));

      createCSTreeContextMenu();

      sortTreeByName(treeCS);

      cs_loaded = true;
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      Messagebox.show(ex.getLocalizedMessage());
    }
    finally
    {
      Clients.clearBusy();
    }
  }

  protected void createCSTreeContextMenu()
  {
    Menupopup contextMenu = new Menupopup();
    treeCS.setContext(contextMenu);
    contextMenu.setParent(this);
    Menuitem miNewCS = new Menuitem(Labels.getLabel("contentCSVSDefault.newCodeSystem"));
    miNewCS.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        popupCodeSystem(PopupWindow.EDITMODE_CREATE, true);
      }
    });
    if (SessionHelper.isUserLoggedIn())
    {
      miNewCS.setParent(contextMenu);
    }
  }

  public void removeEntity()
  {

    if (selectedCS != null && selectedCSV == null)
    { //CS

      removeEntity(true, false, selectedCS.getId(), null);
      refreshCS();  // TODO sinnvoll?
    }
    if (selectedCS != null && selectedCSV != null)
    { //CSV

      removeEntity(true, true, selectedCS.getId(), selectedCSV.getVersionId());
      refreshCS();
    }
    if (selectedVS != null && selectedVSV == null)
    { //VS

      removeEntity(false, false, selectedVS.getId(), null);
      refreshVS();
    }
    if (selectedVS != null && selectedVSV != null)
    { //VSV

      removeEntity(false, true, selectedVS.getId(), selectedVSV.getVersionId());
      refreshVS();
    }
  }

  private void removeEntity(Boolean isCodeSystem, Boolean versionOnly, Long id, Long versionId)
  {

    RemoveTerminologyOrConceptRequestType parameter = new RemoveTerminologyOrConceptRequestType();
    parameter.setDeleteInfo(new DeleteInfo());

    // Login
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    if (isCodeSystem)
    {//CS
      CodeSystem cs = new CodeSystem();
      CodeSystemVersion csv = new CodeSystemVersion();
      cs.setId(id);
      if (versionId == null)
      {
        csv.setVersionId(null);
      }
      else
      {
        csv.setVersionId(versionId);
      }
      cs.getCodeSystemVersions().add(csv);
      parameter.getDeleteInfo().setCodeSystem(cs);
      if (versionOnly)
      {
        parameter.getDeleteInfo().setType(Type.CODE_SYSTEM_VERSION);
      }
      else
      {
        parameter.getDeleteInfo().setType(Type.CODE_SYSTEM);
      }
    }
    else
    {//VS
      ValueSet vs = new ValueSet();
      ValueSetVersion vsv = new ValueSetVersion();
      vs.setId(id);
      if (versionId == null)
      {
        vsv.setVersionId(null);
      }
      else
      {
        vsv.setVersionId(versionId);
      }
      vs.getValueSetVersions().add(vsv);
      parameter.getDeleteInfo().setValueSet(vs);
      if (versionOnly)
      {
        parameter.getDeleteInfo().setType(Type.VALUE_SET_VERSION);
      }
      else
      {
        parameter.getDeleteInfo().setType(Type.VALUE_SET);
      }
    }

    // Ausführen
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    RemoveTerminologyOrConceptResponseType response = port.removeTerminologyOrConcept(parameter);

    // Meldung
    try
    {
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        if (response.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.authoring.OverallErrorCategory.INFO)
        {
          Messagebox.show(response.getReturnInfos().getMessage(), Labels.getLabel("common.success"), Messagebox.OK, Messagebox.INFORMATION);
        }
      }
      else
        Messagebox.show(response.getReturnInfos().getMessage(), Labels.getLabel("common.error"), Messagebox.OK, Messagebox.ERROR);
    }
    catch (Exception ex)
    {
      Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

// Tree Value Set //////////////////////////////////////////////////////////////        
  protected void createVSTree()
  {
    if (vs_loaded)
      return;

    logger.debug("createCSTree()");

    try
    {
      Clients.showBusy(Labels.getLabel("common.loading"));

      treeVS.setModel(TreeModelVS.getTreeModel(getDesktop()));
      treeVS.setItemRenderer(new TreeitemRenderer_CS_VS_DV(this));

      createVSTreeContextMenu();

      sortTreeByName(treeVS);

      vs_loaded = true;
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      Messagebox.show(ex.getLocalizedMessage());
    }
    finally
    {
      Clients.clearBusy();
    }
  }

  protected void createVSTreeContextMenu()
  {
    Menupopup contextMenuVS = new Menupopup();
    treeVS.setContext(contextMenuVS);
    contextMenuVS.setParent(this);
    Menuitem miNewVS = new Menuitem(Labels.getLabel("contentCSVSDefault.newValueSet"));
    miNewVS.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        popupValueSet(PopupWindow.EDITMODE_CREATE);
      }
    });
    if (SessionHelper.isUserLoggedIn())
    {
      miNewVS.setParent(contextMenuVS);
    }
  }

// Tree Search /////////////////////////////////////////////////////////////////    
  protected void createSearchTree()
  {
    if (search_loaded)
      return;

    try
    {
      Clients.showBusy(Labels.getLabel("common.loading"));

      treeSearch = (Tree) getFellow("treeSearch");

      createSearchTreeModel();

      TreeitemRenderer_CS_VS_DV t = new TreeitemRenderer_CS_VS_DV(this);
      t.setShowType(true);
      treeSearch.setItemRenderer(t);

      sortTreeByName(treeSearch);

      search_loaded = true;
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      Messagebox.show(ex.getLocalizedMessage());
    }
    finally
    {
      Clients.clearBusy();
    }
  }

  protected void createSearchTreeModel()
  {
    List<TreeNode> list = new LinkedList<TreeNode>();

    //logger.debug("CSV-Liste-size: " + TreeModelCS.getCsvList().size());
    // alle CSV und VSV der Liste hinzufügen    
    for (CodeSystemVersion csv : TreeModelCS.getCsvList())
    {
      //logger.debug("Add to search: " + csv.getName());
      list.add(new TreeNode(csv));
    }

    for (ValueSetVersion vsv : TreeModelVS.getVsvList())
    {
      //logger.debug("Add to search: " + vsv.getVersionId());
      list.add(new TreeNode(vsv));
    }

    try
    {
      TreeNode tn_root = new TreeNode(null, list);  // TODO Fehler hier?
      treeSearch.setModel(new TreeModel(tn_root));
    }
    catch (Exception e)
    {
      logger.error("Fehler in ContentCSVSDefault.java: " + e.getLocalizedMessage());
    }
  }

  private Tree getTreeActive()
  {
    if (mode == MODE.CODESYSTEMS)
    {
      return treeCS;
    }
    else if (mode == MODE.VALUESETS)
    {
      return treeVS;
    }
    else if (mode == MODE.SEARCH)
    {
      return treeSearch;
    }
    return null;
  }

////////////////////////////////////////////////////////////////////////////////    
// Deep Links //////////////////////////////////////////////////////////////////    
  private void expandTreeAndLoadConceptsByDeeplink()
  {
    // Get Parameter by URL
    String type = ParameterHelper.getString("loadType"); // getDesktop().getExecution().getParameter("loadType");
    String name = ParameterHelper.getString("loadName"); // getDesktop().getExecution().getParameter("loadName");
    String loadId = ParameterHelper.getString("loadId"); // getDesktop().getExecution().getParameter("loadName");

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
    }

  }

  private boolean selectTreeitemByName(Treechildren tc, String s)
  {
    if (tc == null || tc.getItems() == null || tc.getItems().isEmpty())
      return false;

    String s2;
    Iterator<Treeitem> itTi = tc.getItems().iterator();
    Treeitem ti2;

    while (itTi.hasNext())
    {
      ti2 = itTi.next();

      if (((TreeNode) ti2.getValue()).getData() instanceof CodeSystem)
      {
        CodeSystem cs = (CodeSystem) (((TreeNode) ti2.getValue()).getData());
        s2 = DeepLinkHelper.getConvertedString(cs.getName(), false);
        if (s2.contains(s))
        {
          onSelect(ti2);
          loadConceptsBySelectedItem(false, false);
          return true;
        }
      }
      else if (((TreeNode) ti2.getValue()).getData() instanceof CodeSystemVersion)
      {
        CodeSystemVersion csv = (CodeSystemVersion) (((TreeNode) ti2.getValue()).getData());
        s2 = DeepLinkHelper.getConvertedString(csv.getName(), false);
        if (s2.contains(s))
        {
          onSelect(ti2);
          loadConceptsBySelectedItem(false, false);
          return true;
        }
      }
      else if (((TreeNode) ti2.getValue()).getData() instanceof ValueSet)
      {
        ValueSet vs = (ValueSet) (((TreeNode) ti2.getValue()).getData());
        s2 = DeepLinkHelper.getConvertedString(vs.getName(), false);
        if (s2.contains(s))
        {
          onSelect(ti2);
          loadConceptsBySelectedItem(false, false);
          return true;
        }
      }
      else if (((TreeNode) ti2.getValue()).getData() instanceof DomainValue)
      {
        if (selectTreeitemByName(ti2.getTreechildren(), s) == true)
          return true;
      }
    }
    return false;
  }

  private boolean selectTreeitemById(Treechildren tc, long versionId)
  {
    if (tc == null || tc.getItems() == null || tc.getItems().isEmpty())
      return false;

    Iterator<Treeitem> itTi = tc.getItems().iterator();
    Treeitem ti2;

    while (itTi.hasNext())
    {
      ti2 = itTi.next();

      if (((TreeNode) ti2.getValue()).getData() instanceof CodeSystem)
      {
        CodeSystem cs = (CodeSystem) (((TreeNode) ti2.getValue()).getData());
        //s2 = DeepLinkHelper.getConvertedString(cs.getName(), false);
        //if (s2.contains(s))
        if (cs.getCurrentVersionId().longValue() == versionId)
        {
          onSelect(ti2);
          loadConceptsBySelectedItem(false, false);
          return true;
        }
      }
      else if (((TreeNode) ti2.getValue()).getData() instanceof CodeSystemVersion)
      {
        CodeSystemVersion csv = (CodeSystemVersion) (((TreeNode) ti2.getValue()).getData());

        if (csv.getVersionId().longValue() == versionId)
        {
          onSelect(ti2);
          loadConceptsBySelectedItem(false, false);
          return true;
        }
      }
      /*else if (((TreeNode) ti2.getValue()).getData() instanceof ValueSet)
       {
       ValueSet vs = (ValueSet) (((TreeNode) ti2.getValue()).getData());
       s2 = DeepLinkHelper.getConvertedString(vs.getName(), false);
       if (s2.contains(s))
       {
       onSelect(ti2);
       loadConceptsBySelectedItem(false, false);
       return true;
       }
       }*/
      else if (((TreeNode) ti2.getValue()).getData() instanceof ValueSetVersion)
      {
        ValueSetVersion vsv = (ValueSetVersion) (((TreeNode) ti2.getValue()).getData());

        if (vsv.getVersionId().longValue() == versionId)
        {
          onSelect(ti2);
          loadConceptsBySelectedItem(false, false);
          return true;
        }
      }
      else if (((TreeNode) ti2.getValue()).getData() instanceof DomainValue)
      {
        /*if (selectTreeitemByName(ti2.getTreechildren(), s) == true)
         return true;*/
      }
    }
    return false;
  }

  private void getTreeNodesForSearchTree(List<TreeNode> lRoot, TreeNode tn)
  {
    Iterator<TreeNode> itTnCSV = tn.getChildren().iterator();
    while (itTnCSV.hasNext())
    {
      TreeNode t = itTnCSV.next();
      Object x = t.getData();
      if (x instanceof CodeSystem || x instanceof ValueSet || x instanceof DomainValue)
        getTreeNodesForSearchTree(lRoot, t);
      else if (x instanceof CodeSystemVersion || x instanceof ValueSetVersion)
        lRoot.add(t);
    }
  }

  public void openPopupExport()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("CSV", selectedCSV);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupExport.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentCSVSDefault.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public void openPopupProposalExistingCSVS(boolean CS)
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      if (CS)
      {
        data.put("source", selectedCSV);
      }
      else
      {
        data.put("source", selectedVSV);
      }
      data.put("isExisting", true);
      Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentCSVSDefault.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public void openPopupExportVS()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("VSV", selectedVSV);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupExport.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentCSVSDefault.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  protected void selectCurrentCSV(Treeitem ti)
  {
    logger.debug("selectCurrentCSV");

    Iterator<CodeSystemVersion> it = selectedCS.getCodeSystemVersions().iterator();
    while (it.hasNext())
    {
      CodeSystemVersion csv = it.next();
      if (selectedCS.getCurrentVersionId() != null && csv.getVersionId().compareTo(selectedCS.getCurrentVersionId()) == 0)
      {
        selectedCSV = csv;
        break;
      }
    }

    if (selectedCSV != null)
      logger.debug("selectedCSV: " + selectedCSV.getVersionId());

    // Focus auf CSV legen 
    ti.setOpen(true);
    Treechildren tc = ti.getTreechildren();
    Iterator<Treeitem> itTi = tc.getItems().iterator();
    Treeitem ti2;
    while (itTi.hasNext())
    {
      ti2 = itTi.next();
      CodeSystemVersion csv = (CodeSystemVersion) (((TreeNode) ti2.getValue()).getData());
      if (csv == selectedCSV)
      {
        onSelect(ti2);
        ti2.setSelected(true);
        break;
      }
    }
  }

  protected void selectCurrentVSV(Treeitem ti)
  {
    Iterator<ValueSetVersion> it = selectedVS.getValueSetVersions().iterator();
    while (it.hasNext())
    {
      ValueSetVersion vsv = it.next();
      if (vsv.getVersionId().compareTo(selectedVS.getCurrentVersionId()) == 0)
      {
        selectedVSV = vsv;
        break;
      }
    }

    // Focus auf VSV legen    
    ti.setOpen(true);
    Treechildren tc = ti.getTreechildren();
    Iterator<Treeitem> itTi = tc.getItems().iterator();
    Treeitem ti2;
    while (itTi.hasNext())
    {
      ti2 = itTi.next();
      ValueSetVersion vsv = (ValueSetVersion) (((TreeNode) ti2.getValue()).getData());
      if (vsv == selectedVSV)
      {
        ti2.setSelected(true);
        break;
      }
    }
  }

  public void loadConceptsBySelectedItem(boolean draggable, boolean droppable)
  {
    logger.debug("loadConceptsBySelectedItem");

    Tree treeActive = getTreeActive();

    Object source = treeActive.getSelectedItem();
    Treeitem ti = null;
    String name, versionName;
    int contentMode;
    long id, versionId, validityRange = 0;

    if (selectedCS != null)
      logger.debug("selectedCS: " + selectedCS.getId());
    if (selectedCSV != null)
      logger.debug("selectedCSV: " + selectedCSV.getVersionId());
    if (selectedVS != null)
      logger.debug("selectedVS: " + selectedVS.getId());
    if (selectedVSV != null)
      logger.debug("selectedVSV: " + selectedVSV.getVersionId());

    // Objekte vorhanden?
    if (source != null)
    {
      ti = (Treeitem) source;
      source = ((TreeNode) ti.getValue()).getData();
    }

    // CS,VS bzw ihre aktuellen Versionen laden
    if (source instanceof CodeSystem)
    {
      // Öffne die letzte Version
      if (true)
      { // TODO: Hier muss noch der entsprechende Flag vom UserAccount benutzt werden
        selectCurrentCSV(ti);
        source = selectedCSV; // damit danach der Content der csv geladen werden kann
        //return;
      }
      else
      {
        selectedCSV = null;
      }
    }
    else if (source instanceof ValueSet)
    {
      if (true)
      { // TODO: Eigenschaft aus UserAccount beziehen
        selectCurrentVSV(ti);
        source = selectedVSV; // damit danach der Content der csv geladen werden kann
        //return;
      }
      else
      {
        selectedVSV = null;
      }
    }

    // Parameter zum laden des Content angeben
    if (source instanceof CodeSystemVersion)
    {
      contentMode = ContentConcepts.CONTENTMODE_CODESYSTEM;
      id = selectedCSV.getCodeSystem().getId();
      versionId = selectedCSV.getVersionId();
      name = selectedCSV.getCodeSystem().getName();
      versionName = selectedCSV.getName();
      if (selectedCSV.getValidityRange() != null)
        validityRange = selectedCSV.getValidityRange();
    }
    else if (source instanceof ValueSetVersion)
    {
      contentMode = ContentConcepts.CONTENTMODE_VALUESET;
      id = selectedVSV.getValueSet().getId();
      versionId = selectedVSV.getVersionId();
      name = selectedVSV.getValueSet().getName();
      //      validityRange = selectedVSV.getValidityRange();
      validityRange = -1;
      if (selectedVSV.getName() != null)
      {
        versionName = selectedVSV.getName();
      }
      else
      {
        versionName = "";
      }
    }
    else
    {
      return;
    }

    // Lade Content
    try
    {
      //String validityRangeDT = ValidityRangeHelper.getValidityRangeNameById(validityRange);// Lade Range of Validity
      String validityRangeDT = DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_CODESYSTEMVERSION_VALIDITYRANGE, "" + validityRange);

      Include inc = (Include) getFellow("incConcepts");    // lade Concepts in include@ComponentTreeAndContent         
      TreeModelCSEV treeModel = new TreeModelCSEV(source, this);
      inc.setMode("instant");
      inc.setSrc(null);
      inc.setDynamicProperty("parent", this);
      inc.setDynamicProperty("source", source);
      inc.setDynamicProperty("id", id);
      inc.setDynamicProperty("name", name);
      inc.setDynamicProperty("versionId", versionId);
      inc.setDynamicProperty("versionName", versionName);
      inc.setDynamicProperty("validityRange", validityRangeDT);
      inc.setDynamicProperty("contentMode", contentMode);
      inc.setDynamicProperty("droppable", droppable);
      inc.setDynamicProperty("draggable", draggable);

      // Kapitel durch Deep Links auswaehlen
      if (deepLinks.isEmpty() == false)
        inc.setDynamicProperty("deepLinks", deepLinks);
      else
        inc.setDynamicProperty("deepLinks", null);

      // HugeFlatData oder Baum?
      String src;

      if (treeModel.getTotalSize() > 100 && contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
      {
        src = "modules/ContentConceptsHugeFlatData.zul";
      }
      else
      {
        inc.setDynamicProperty("treeModel", treeModel);
        src = "modules/ContentConcepts.zul";
      }

      inc.setSrc(src);
      windowContentConcepts = (ContentConcepts) inc.getFellow("windowConcepts"); // geht nur im instant mode
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void openTreeItem(Treeitem ti, boolean open)
  {
    ti.setOpen(open);
  }

  private void setActiveButtons(Boolean showDetailsButton, Boolean showEditButton, Boolean showNewButton, Boolean showNewVersionButton)
  {
    if (mode == MODE.CODESYSTEMS)
    {
      if (bDetailsCSV != null && showDetailsButton != null)
        bDetailsCSV.setDisabled(!showDetailsButton);
      if (bEditCSV != null && showEditButton != null)
        bEditCSV.setDisabled(!showEditButton);
      //if (bNewCS != null && showNewButton != null)
      //  bNewCS.setDisabled(!showNewButton);
      bNewCS.setDisabled(false);  // immer da
      if (bNewCSV != null && showNewVersionButton != null)
        bNewCSV.setDisabled(!showNewVersionButton);
    }
    else if (mode == MODE.VALUESETS)
    {
      if (bDetailsVSV != null && showDetailsButton != null)
        bDetailsVSV.setDisabled(!showDetailsButton);
      if (bEditVSV != null && showEditButton != null)
        bEditVSV.setDisabled(!showEditButton);
      //if (bNewVS != null && showNewButton != null)
      //  bNewVS.setDisabled(!showNewButton);
      bNewVS.setDisabled(false);
      if (bNewVSV != null && showNewVersionButton != null)
        bNewVSV.setDisabled(!showNewVersionButton);
    }
    else if (mode == MODE.SEARCH)
    {
      if (bDetailsSearch != null && showDetailsButton != null)
        bDetailsSearch.setDisabled(!showDetailsButton);
      if (bEditSearch != null && showEditButton != null)
        bEditSearch.setDisabled(!showEditButton);
    }

    /*if (bActiveDetails != null && bDetails != null)
     bActiveDetails.setDisabled(!bDetails);
     if (bActiveEdit != null && bEdit != null)
     bActiveEdit.setDisabled(!bEdit);
     if (bActiveNew != null && bNew != null)
     bActiveNew.setDisabled(!bNew);
     if (bActiveNewVersion != null && bNewVersion != null)
     bActiveNewVersion.setDisabled(!bNewVersion);*/
  }

  public void onSelect(Treeitem ti)
  {
    if (ti == null || ti.getValue() == null)
      return;

    logger.debug("onSelect()");

    if (mode == MODE.CODESYSTEMS)
      treeCS.setSelectedItem(ti);
    else if (mode == MODE.VALUESETS)
      treeVS.setSelectedItem(ti);
    else if (mode == MODE.SEARCH)
      treeSearch.setSelectedItem(ti);
    else
      return;

    // Auswahl zurücksetzen
    selectedCS = null;
    selectedCSV = null;
    selectedVS = null;
    selectedVSV = null;

    Object selectedObject = ((TreeNode) ti.getValue()).getData();

    // Auswahl setzen
    if (selectedObject instanceof CodeSystem)
    {
      // Auf Codesystem geklickt
      selectedCS = (CodeSystem) selectedObject;
      logger.debug("Auf Codesystem geklickt: " + selectedCS.getId());

      if (AssignTermHelper.isUserAllowed(selectedCS))
      {
        setActiveButtons(true, true, true, true);
      }
      else
      {
        setActiveButtons(true, false, true, false);
      }
    }
    else if (selectedObject instanceof CodeSystemVersion)
    {
      // Auf Codesystem-Version geklickt
      selectedCSV = (CodeSystemVersion) selectedObject;
      selectedCS = selectedCSV.getCodeSystem();

      logger.debug("Auf Codesystem-Version geklickt: " + selectedCSV.getVersionId() + ", CS: " + selectedCS.getId());

      //if (AssignTermHelper.isUserAllowed(selectedCS))
      {
        setActiveButtons(true, true, true, true);
      }

    }
    else if (selectedObject instanceof ValueSet)
    {
      // Auf Value-Set geklickt
      selectedVS = (ValueSet) selectedObject;
      //if (AssignTermHelper.isUserAllowed(selectedVS))
      {
        setActiveButtons(true, true, true, true);
      }

    }
    else if (selectedObject instanceof ValueSetVersion)
    {
      selectedVSV = (ValueSetVersion) selectedObject;
      selectedVS = selectedVSV.getValueSet();
      //if (AssignTermHelper.isUserAllowed(selectedVS))
      {
        setActiveButtons(true, true, true, true);
      }

    }
    else if (selectedObject instanceof DomainValue)
    {
      selectedCS = null;
      selectedCSV = null;
      selectedVS = null;
      selectedVSV = null;

      setActiveButtons(false, false, true, false);
    }
    else
    {
      // unbekannt selektiert           
      selectedCS = null;
      selectedCSV = null;
      selectedVS = null;
      selectedVSV = null;

      setActiveButtons(false, false, true, false);
    }
  }

  public void popupDetails(int mode)
  {
    logger.debug("popupDetails: " + mode);

    Object selectedItem = null;
    Tree treeActive = getTreeActive();

    if (treeActive.getSelectedItem() != null)
      selectedItem = ((TreeNode) treeActive.getSelectedItem().getValue()).getData();
    else
    {
      if (treeActive == treeCS)
        selectedItem = new CodeSystem();
      else if (treeActive == treeVS)
        selectedItem = new ValueSet();
      else
      {
        try
        {
          Messagebox.show("Nicht möglich");
        }
        catch (Exception ex)
        {
          Logger.getLogger(ContentCSVSDefault.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    if (selectedItem instanceof CodeSystem || selectedItem instanceof DomainValue)
    {
      logger.debug("popupCodeSystem");
      popupCodeSystem(mode, false);
    }
    else if (selectedItem instanceof CodeSystemVersion)
    {
      logger.debug("popupCodeSystem");
      popupCodeSystem(mode, true);
    }
    else if (selectedItem instanceof ValueSet)
    {
      logger.debug("popupValueSet");
      popupValueSet(mode);
    }
    else if (selectedItem instanceof ValueSetVersion)
    {
      logger.debug("popupValueSet");
      popupValueSet(mode);
    }
     //else if (selectedItem instanceof DomainValue)

     //logger.debug("selectedItem type not found: " + selectedItem.getClass().getCanonicalName());
    //popupDomainValue(mode);
    else
    {
      if (selectedItem != null)
        logger.debug("selectedItem type not found: " + selectedItem.getClass().getCanonicalName());
    }
  }

  private void popupCodeSystem(int mode, boolean showVersion)
  {
    logger.debug("popupValueSet, mode: " + mode + ", showVersion: " + showVersion);
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("CS", selectedCS);
      if (showVersion)
        data.put("CSV", selectedCSV);
      data.put("EditMode", mode);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupCodeSystem.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  private void popupValueSet(int mode)
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("VS", selectedVS);
      data.put("VSV", selectedVSV);
      data.put("EditMode", mode);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupValueSet.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentCSVSDefault.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public void filterChanged(InputEvent ie)
  {
    filterString = ie.getValue();
    Tree tree = (Tree) getFellow("treeSearch");
    TreeHelper.filterTree(filterString, tree);
    setTextAndFocus(ie.getTarget().getId(), ie.getValue());
  }

  private void setTextAndFocus(String ID, String Value)
  {
    try
    {
      Textbox t = (Textbox) getFellow(ID);
      t.setText(Value);
      t.setFocus(true);
      t.setSelectionRange(Value.length(), Value.length());
    }
    catch (Exception e)
    {
    }
  }

  public void onFilterTabChanged()
  {
    logger.debug("onFilterTabChanged()");

    Tab selTab = tabboxFilter.getSelectedTab();
    if (selTab == tabCS)
    {
      mode = MODE.CODESYSTEMS;
      createCSTree();
    }
    else if (selTab == tabVS)
    {
      mode = MODE.VALUESETS;
      createVSTree();
    }
    else if (selTab == tabSearch)
    {
      mode = MODE.SEARCH;
      createSearchTree();
    }

    logger.debug("save new mode in session: " + mode.name());
    SessionHelper.setViewMode(mode);

    // TODO: Buttons aktivieren
    
  }

  public void setActiveTab()
  {
    logger.debug("setActiveTab(), mode: " + mode.name());

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

    /*tabActive = tb.getSelectedTab();

     if (tabActive == tabCS)
     {
     treeActive = treeCS;

     bActiveDetails = bDetailsCSV;
     //      bActiveGlobalSearch = bGlobSearchCSV;
     //      bActiveGlobalSearch.setDisabled(false);
     bActiveEdit = bEditCSV;
     bActiveNew = bNewCS;
     bActiveNewVersion = bNewCSV;
     setActiveButtons(false, false, true, false);
     }
     else if (tabActive == tabVS)
     {
     treeActive = treeVS;

     bActiveDetails = bDetailsVSV;
     //      bActiveGlobalSearch = bGlobSearchVSV;
     //      bActiveGlobalSearch.setDisabled(false);
     bActiveEdit = bEditVSV;
     bActiveNew = bNewVS;
     bActiveNewVersion = bNewVSV;
     setActiveButtons(false, false, true, false);
     }
     else if (tabActive == tabSearch)
     {
     treeActive = treeSearch;

     bActiveDetails = bDetailsSearch;
     //      bActiveGlobalSearch.setDisabled(true);
     bActiveEdit = bEditSearch;
     bActiveNew = null;
     bActiveNewVersion = null;
     setActiveButtons(false, false, false, false);
     }
     // sortieren nach dem Laden des Models
     sortTreeByName(treeActive);*/
  }

  public void expandTreeCS()
  {
    TreeHelper.doCollapseExpandAll(treeCS, true);
  }

  public void collapseTreeCS()
  {
    TreeHelper.doCollapseExpandAll(treeCS, false);
  }

  public void expandTreeVS()
  {
    TreeHelper.doCollapseExpandAll(treeVS, true);
  }

  public void collapseTreeVS()
  {
    TreeHelper.doCollapseExpandAll(treeVS, false);
  }

  private void sortTreeByName(Tree t)
  {
    if (t != null)
    {
      Treecol tcName = (Treecol) t.getTreecols().getChildren().get(0);
      tcName.setSortAscending(new ComparatorCsvVsv(true));
      tcName.setSortDescending(new ComparatorCsvVsv(false));
      tcName.setSortDirection("descending");
      tcName.sort(true);
    }
  }

  public void refreshCS()
  {
    TreeModelCS.reloadData(getDesktop());
    treeCS.setModel(TreeModelCS.getTreeModel(getDesktop()));

    sortTreeByName(treeCS);
  }

  public void refreshVS()
  {
    TreeModelVS.reloadData(getDesktop());
    treeVS.setModel(TreeModelVS.getTreeModel(getDesktop()));

    sortTreeByName(treeVS);
  }

  public void refreshSearch()
  {
    refreshCS();
    refreshVS();

    setActiveButtons(false, false, true, false);
  }

  public ContentConcepts getWindowContentConcepts()
  {
    return windowContentConcepts;
  }

  public void setWindowContentConcepts(ContentConcepts windowContentConcepts)
  {
    this.windowContentConcepts = windowContentConcepts;
  }
}
