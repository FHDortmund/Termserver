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
package de.fhdo.gui.main.content;

import de.fhdo.Definitions;
import de.fhdo.gui.main.TreeAndContent;
import de.fhdo.gui.main.modules.AssociationEditor;
import de.fhdo.gui.main.modules.PopupCodeSystem;
import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.gui.main.modules.PopupExport;
import de.fhdo.gui.main.modules.PopupValueSet;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.ParameterHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SendBackHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.CodesystemGenericTreeModel;
import de.fhdo.models.ValuesetGenericTreeModel;
import de.fhdo.models.comparators.ComparatorCodesystemVersions;
import de.fhdo.models.comparators.ComparatorConceptCodeAscending;
import de.fhdo.models.comparators.ComparatorConceptCodeDescending;
import de.fhdo.models.comparators.ComparatorConceptDesignationAscending;
import de.fhdo.models.comparators.ComparatorConceptDesignationDescending;
import de.fhdo.terminologie.ws.authoring.Status;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse;
import de.fhdo.terminologie.ws.search.SearchType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ContentConcepts extends Window implements AfterCompose, IUpdateModal
{

  protected static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private CodeSystem codeSystem;
  private CodeSystemVersion codeSystemVersion;
  private ValueSet valueSet;
  private ValueSetVersion valueSetVersion;

  private ConceptsTree concepts = null;
  private boolean searchActive = false;

  private TreeAndContent.LOADTYPE paramLoadType = TreeAndContent.LOADTYPE.NONE;
  private String paramLoadName = "";
  private String paramLoadOid = "";
  private long paramLoadId = 0;
  private boolean externMode = false;
  private boolean paramSearch = false;
  private boolean lookForward = false;

  private boolean sendBack = false;
  private boolean dragAndDrop = false;
  
  private boolean collaborationActive = false;

  private IUpdate updateListener = null;
  private AssociationEditor associationEditor;

  private TreeAndContent treeAndContent;

  public ContentConcepts()
  {
    logger.debug("ContentConcepts() - Konstruktor");

    Executions.getCurrent().setAttribute("instance", this);

    getURLParameter();
    
    collaborationActive = SessionHelper.isCollaborationActive();

    // loading dynamic parameters
    codeSystem = (CodeSystem) Executions.getCurrent().getAttribute("codeSystem");
    valueSet = (ValueSet) Executions.getCurrent().getAttribute("valueSet");
    treeAndContent = (TreeAndContent) Executions.getCurrent().getAttribute("parent");

    String windowId = ArgumentHelper.getWindowParameterString("id");

    if (Executions.getCurrent().hasAttribute("updateListener"))
    {
      updateListener = (IUpdate) Executions.getCurrent().getAttribute("updateListener");
      logger.debug("[ContentConcepts.java] UpdateListener found");
    }

    if (Executions.getCurrent().hasAttribute("associationEditor"))
    {
      associationEditor = (AssociationEditor) Executions.getCurrent().getAttribute("associationEditor");
      logger.debug("[ContentConcepts.java] AssociationEditor found");

      if (windowId != null)
      {
        if (windowId.equals("1"))
          associationEditor.setContentConcepts1(this);
        else
          associationEditor.setContentConcepts2(this);
      }
    }

    codeSystemVersion = null;
    valueSetVersion = null;

    if (codeSystem != null)
    {
      logger.debug("Codesystem given with id: " + codeSystem.getId());
      logger.debug("Count versions: " + codeSystem.getCodeSystemVersions().size());

      Object o = SessionHelper.getValue("selectedCSV");
      if (o != null)
      {
        logger.debug("selectedCSV is not null");

        codeSystemVersion = (CodeSystemVersion) o;

        if (codeSystemVersion.getCodeSystem() == null)
        {
          logger.debug("codesystem in codesyste version is null, so load default");
          codeSystemVersion = null;
        }
        else
        {

          if (codeSystemVersion != null && codeSystemVersion.getCodeSystem().getId().longValue() != codeSystem.getId())
          {
            // wrong code system
            codeSystemVersion = null;
          }
          else
          {
            logger.debug("Version given with id: " + codeSystemVersion.getVersionId());
          }
        }
      }

      if (codeSystemVersion == null)
      {
        Object obj = SessionHelper.getValue("loadCSV");
        long loadCsvId = 0;
        if (obj != null)
        {
          loadCsvId = Long.parseLong(obj.toString());
          SessionHelper.setValue("loadCSV", null);
        }
        // load default version
        for (CodeSystemVersion csv : codeSystem.getCodeSystemVersions())
        {
          if (loadCsvId == csv.getVersionId().longValue() || (loadCsvId == 0 && csv.getVersionId().longValue() == codeSystem.getCurrentVersionId().longValue()))
          {
            codeSystemVersion = csv;
            logger.debug("Version given with default id: " + codeSystemVersion.getVersionId());
            break;
          }
        }
      }

    }

    if (valueSet != null)
    {
      logger.debug("ValueSet given with id: " + valueSet.getId());
      logger.debug("Count versions: " + valueSet.getValueSetVersions().size());

      Object o = SessionHelper.getValue("selectedVSV");
      if (o != null)
      {
        valueSetVersion = (ValueSetVersion) o;

        if (valueSetVersion.getValueSet() == null)
        {
          valueSetVersion = null;
        }
        else
        {

          if (valueSetVersion != null && valueSetVersion.getValueSet().getId().longValue() != valueSet.getId())
          {
            // wrong value set
            valueSetVersion = null;
          }
          else
          {
            logger.debug("Version given with id: " + valueSetVersion.getVersionId());
          }
        }
      }

      if (valueSetVersion == null)
      {
        // load default version
        for (ValueSetVersion vsv : valueSet.getValueSetVersions())
        {
          if (vsv.getVersionId().longValue() == valueSet.getCurrentVersionId().longValue())
          {
            valueSetVersion = vsv;
            logger.debug("Version given with id: " + valueSetVersion.getVersionId());
            break;
          }
        }
      }
    }
  }

  public void afterCompose()
  {
    logger.debug("ContentConcepts - afterCompose()");

    fillVersionList();
    loadConcepts();

    showButtons();

    if (paramSearch)
      onSwitchSearch();

    Clients.clearBusy();
  }

  private void getURLParameter()
  {
    logger.debug("ContentConcepts.java - getURLParameter()");

    paramSearch = ParameterHelper.getBoolean("search", false);
    sendBack = ParameterHelper.getBoolean("sendBack", false);

    lookForward = ParameterHelper.getBoolean("lookForward", false);

    String loadType = ParameterHelper.getString("loadType");
    paramLoadName = ParameterHelper.getString("loadName");
    paramLoadId = ParameterHelper.getLong("loadId");
    paramLoadOid = ParameterHelper.getString("loadOID");

    paramLoadType = TreeAndContent.LOADTYPE.NONE;
    if (loadType != null)
    {
      if (loadType.equalsIgnoreCase("CodeSystem") || loadType.equalsIgnoreCase("CS"))
      {
        paramLoadType = TreeAndContent.LOADTYPE.CODESYSTEM;
      }
      else if (loadType.equalsIgnoreCase("ValueSet") || loadType.equalsIgnoreCase("VS"))
      {
        paramLoadType = TreeAndContent.LOADTYPE.VALUESET;
      }
    }

    dragAndDrop = ParameterHelper.getBoolean("dragAndDrop", false);

    logger.debug("sendBack: " + isSendBack());
    logger.debug("paramSearch: " + paramSearch);
    logger.debug("paramLoadType: " + paramLoadType);
    logger.debug("paramLoadName: " + paramLoadName);
    logger.debug("paramLoadId: " + paramLoadId);
    logger.debug("paramLoadOid: " + paramLoadOid);

    if (paramLoadId > 0
        || (paramLoadType != null && paramLoadType != TreeAndContent.LOADTYPE.NONE)
        || (paramLoadName != null && paramLoadName.length() > 0)
        || (paramLoadOid != null && paramLoadOid.length() > 0))
    {
      externMode = true;
    }
    logger.debug("externMode: " + externMode);
  }

  private void fillVersionList()
  {
    logger.debug("fillVersionList()");

    final Combobox cbVersions = (Combobox) getFellow("cbVersion");
    long selectedVersionId = 0;

    cbVersions.setDisabled(externMode);  // disable in extern mode (do not allow choosing)

    if (codeSystem != null)
    {
      List<CodeSystemVersion> list = codeSystem.getCodeSystemVersions();
      Collections.sort(list, new ComparatorCodesystemVersions(true));

      cbVersions.setModel(new ListModelList<CodeSystemVersion>(list));
      if (codeSystemVersion != null)
        selectedVersionId = codeSystemVersion.getVersionId();

      // select cached version
      //for(CodeSystemVersion csv_list : cbVersions.getItems())
      //cbVersions.setSelectedIndex(MODAL);
    }
    else if (valueSet != null)
    {
      cbVersions.setModel(new ListModelList<ValueSetVersion>(valueSet.getValueSetVersions()));
      if (valueSetVersion != null)
        selectedVersionId = valueSetVersion.getVersionId();
    }

    final long selectedVersionIdFinal = selectedVersionId;

    cbVersions.setItemRenderer(new ComboitemRenderer()
    {
      public void render(Comboitem item, Object o, int i) throws Exception
      {
        if (o != null)
        {
          if (o instanceof CodeSystemVersion)
          {

            CodeSystemVersion csv = (CodeSystemVersion) o;
            item.setLabel(csv.getName());

            //logger.debug("render csv with id: " + csv.getVersionId());
            if (csv.getVersionId().longValue() == selectedVersionIdFinal)
            {
              cbVersions.setSelectedItem(item);
              cbVersions.setText(item.getLabel());
            }
          }
          else if (o instanceof ValueSetVersion)
          {
            ValueSetVersion vsv = (ValueSetVersion) o;
            item.setLabel(vsv.getName());

            if (vsv.getVersionId().longValue() == selectedVersionIdFinal)
            {
              cbVersions.setSelectedItem(item);
              cbVersions.setText(item.getLabel());
            }
          }
          item.setValue(o);
        }
        else
          item.setLabel("");
      }
    });

  }

  public void onVersionChanged()
  {
    logger.debug("onVersionChanged()");

    Combobox cbVersions = (Combobox) getFellow("cbVersion");
    Object o = cbVersions.getSelectedItem().getValue();

    if (o instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) o;
      csv.setCodeSystem(codeSystem);
      SessionHelper.setValue("selectedCSV", csv);
      SessionHelper.setValue("selectedVSV", null);

      logger.debug("CSV selected: " + csv.getVersionId());
      //codeSystem = csv.getCodeSystem();
      codeSystemVersion = csv;

      logger.debug("CS-ID: " + codeSystem.getId());
      logger.debug("CSV-ID: " + codeSystemVersion.getVersionId());
    }
    else if (o instanceof ValueSetVersion)
    {
      ValueSetVersion vsv = (ValueSetVersion) o;
      vsv.setValueSet(valueSet);
      SessionHelper.setValue("selectedVSV", vsv);
      SessionHelper.setValue("selectedCSV", null);

      logger.debug("VSV selected: " + vsv.getVersionId());
      //valueSet = vsv.getValueSet();
      valueSetVersion = vsv;
    }

    // change title string
    treeAndContent.setTitleCenter();

    loadConcepts();
  }

  private void loadConcepts()
  {
    logger.debug("loadConcepts()");

    Tree treeConcepts = (Tree) getFellow("treeConcepts");

    //if(concepts == null)
    concepts = new ConceptsTree(treeConcepts, this);
    concepts.setDragAndDrop(dragAndDrop);
    concepts.setUpdateDropListener(updateListener);
    
    ((Treecol)getFellow("tcTerm")).setSortAscending(new ComparatorConceptDesignationAscending());
    ((Treecol)getFellow("tcTerm")).setSortDescending(new ComparatorConceptDesignationDescending());
    
    ((Treecol)getFellow("tcCode")).setSortAscending(new ComparatorConceptCodeAscending());
    ((Treecol)getFellow("tcCode")).setSortDescending(new ComparatorConceptCodeDescending());

    if (codeSystem != null)
      concepts.setCodeSystemId(codeSystem.getId());
    else if (valueSet != null)
      concepts.setValueSetId(valueSet.getId());

    if (codeSystemVersion != null)
      concepts.setCodeSystemVersionId(codeSystemVersion.getVersionId());
    else if (valueSetVersion != null)
      concepts.setValueSetVersionId(valueSetVersion.getVersionId());

    concepts.initData();

  }

  public void onConceptSelect(Event event)
  {
    logger.debug("Event: " + event.getClass().getCanonicalName());
    if (event != null && event instanceof org.zkoss.zk.ui.event.SelectEvent)
    {
      org.zkoss.zk.ui.event.SelectEvent selEvent = (org.zkoss.zk.ui.event.SelectEvent) event;
      logger.debug("Keys: " + selEvent.getKeys());

      if (selEvent.getKeys() == MouseEvent.RIGHT_CLICK)
      {
        showButtons();
        return;
      }
    }

    concepts.onConceptSelect(false, true);
    showButtons();
  }

  public void onNewClicked()
  {
    if (SessionHelper.isUserLoggedIn())
      getConcepts().createConcept(PopupConcept.HIERARCHYMODE.ROOT, 0);
  }

  public void onNewSubClicked()
  {
    if (SessionHelper.isUserLoggedIn())
    {
      CodeSystemEntityVersion csev = getConcepts().getSelection();
      if (csev != null)
        getConcepts().createConcept(PopupConcept.HIERARCHYMODE.SUB, csev.getVersionId());
    }
  }

  public void onEditClicked()
  {
    if (SessionHelper.isUserLoggedIn())
      getConcepts().maintainConcept();
  }

  public void onDetailsClicked()
  {
    getConcepts().openConceptDetails();
  }

  public void onDeleteClicked()
  {
    if (SessionHelper.isUserLoggedIn())
      getConcepts().deleteConcept();
  }

  public void onDeleteVersionClicked()
  {
    if (SessionHelper.isUserLoggedIn())
    {
      try
      {
        if (Messagebox.show(Labels.getLabel("common.deactivateCSVersion"), Labels.getLabel("common.deleteSystem"), Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
            == Messagebox.YES)
        {
          if (codeSystem != null)
          {
            UpdateCodeSystemVersionStatusRequestType request = new UpdateCodeSystemVersionStatusRequestType();
            request.setLoginToken(SessionHelper.getSessionId());
            request.setCodeSystem(new CodeSystem());
            CodeSystemVersion csv = new CodeSystemVersion();
            csv.setVersionId(codeSystemVersion.getVersionId());
            csv.setStatus(Definitions.STATUS_CODESYSTEMVERSION_INVISIBLE);
            request.getCodeSystem().getCodeSystemVersions().add(csv);

            UpdateCodeSystemVersionStatusResponse.Return response = WebServiceHelper.updateCodeSystemVersionStatus(request);

            if (response.getReturnInfos().getStatus() == Status.OK)
            {
              // reload cs tree
              SessionHelper.setValue("selectedCS", null);
              SessionHelper.setValue("selectedVS", null);

              CodesystemGenericTreeModel.getInstance().reloadData();

              Executions.sendRedirect(null);
              return;
            }
            else
            {
              Messagebox.show(response.getReturnInfos().getMessage(), Labels.getLabel("common.error"), Messagebox.OK, Messagebox.ERROR);
            }
          }
          else if (valueSet != null)
          {
            UpdateValueSetStatusRequestType request = new UpdateValueSetStatusRequestType();
            request.setLoginToken(SessionHelper.getSessionId());
            request.setValueSet(new ValueSet());
            ValueSetVersion vsv = new ValueSetVersion();
            vsv.setVersionId(valueSetVersion.getVersionId());
            vsv.setStatus(Definitions.STATUS_CODESYSTEMVERSION_INVISIBLE);
            request.getValueSet().getValueSetVersions().add(vsv);

            UpdateValueSetStatusResponse.Return response = WebServiceHelper.updateValueSetStatus(request);

            if (response.getReturnInfos().getStatus() == Status.OK)
            {
              // reload vs tree
              SessionHelper.setValue("selectedCS", null);
              SessionHelper.setValue("selectedVS", null);

              ValuesetGenericTreeModel.getInstance().reloadData();

              Executions.sendRedirect(null);
              return;
            }
            else
            {
              Messagebox.show(response.getReturnInfos().getMessage(), Labels.getLabel("common.error"), Messagebox.OK, Messagebox.ERROR);
            }
          }
          else
          {
            logger.debug("codeSystem and valueSet are null");
          }
        }
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }

      //if (allowEditing)
      //TreeAndContent.popupCodeSystem(codeSystem, PopupCodeSystem.EDITMODES.MAINTAIN, false, this);
      //else
      //  TreeAndContent.popupCodeSystem(cs, PopupCodeSystem.EDITMODES.DETAILSONLY, false, this);
    }
  }

  public void onDetailVersionClicked()
  {
    try
    {
      if (codeSystem != null)
      {
        logger.debug("show codeSystem version details");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("CS", codeSystem);
        data.put("CSV", codeSystemVersion);
        data.put("EditMode", PopupCodeSystem.EDITMODES.DETAILSONLY);

        Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupCodeSystem.zul", this, data);
        ((PopupCodeSystem) w).setUpdateListener(this);
        w.doModal();
      }
      else if (valueSet != null)
      {
        logger.debug("show valueSet version details");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("VS", valueSet);
        data.put("VSV", valueSetVersion);
        data.put("EditMode", PopupValueSet.EDITMODES.DETAILSONLY);

        Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupValueSet.zul", this, data);
        ((PopupValueSet) w).setUpdateListener(this);
        w.doModal();
      }
      else
      {
        logger.debug("codeSystem and valueSet are null");
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  public void onEditVersionClicked()
  {
    if (SessionHelper.isUserLoggedIn())
    {
      try
      {
        if (codeSystem != null)
        {
          logger.debug("show codeSystem version");
          Map<String, Object> data = new HashMap<String, Object>();
          data.put("CS", codeSystem);
          data.put("CSV", codeSystemVersion);
          data.put("EditMode", PopupCodeSystem.EDITMODES.MAINTAIN);

          Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupCodeSystem.zul", this, data);
          ((PopupCodeSystem) w).setUpdateListener(this);
          w.doModal();
        }
        else if (valueSet != null)
        {
          logger.debug("show valueSet version");
          Map<String, Object> data = new HashMap<String, Object>();
          data.put("VS", valueSet);
          data.put("VSV", valueSetVersion);
          data.put("EditMode", PopupValueSet.EDITMODES.MAINTAIN);

          Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupValueSet.zul", this, data);
          ((PopupValueSet) w).setUpdateListener(this);
          w.doModal();
        }
        else
        {
          logger.debug("codeSystem and valueSet are null");
        }
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }

      //if (allowEditing)
      //TreeAndContent.popupCodeSystem(codeSystem, PopupCodeSystem.EDITMODES.MAINTAIN, false, this);
      //else
      //  TreeAndContent.popupCodeSystem(cs, PopupCodeSystem.EDITMODES.DETAILSONLY, false, this);
    }
    //concepts..createConcept(PopupConcept.HIERARCHYMODE.ROOT, 0);
  }

  public void onExportClicked()
  {
    logger.debug("onExportClicked()");
    if (codeSystemVersion != null)
    {
      codeSystemVersion.setCodeSystem(codeSystem);
      PopupExport.doModal(codeSystemVersion, null);
    }
    else if (valueSetVersion != null)
    {
      valueSetVersion.setValueSet(valueSet);
      PopupExport.doModal(null, valueSetVersion);
    }
  }

  public void onSwitchSearch()
  {
    Component compSearch = getFellow("searchContainer");
    compSearch.setVisible(!compSearch.isVisible());

    this.invalidate();

    if (compSearch.isVisible())
    {
      ((Textbox) getFellow("tbSearchTerm")).focus();
    }
    else
    {
      if (searchActive)
      {
        getConcepts().initData();
        searchActive = false;
      }
    }
  }

  public void onSearchClicked()
  {
    logger.debug("onSearchClicked()");

    String code = ((Textbox) getFellow("tbSearchCode")).getText();
    String term = ((Textbox) getFellow("tbSearchTerm")).getText();

    // check mandatory fields
    if (code.length() == 0 && term.length() == 0)
    {
      Messagebox.show(Labels.getLabel("popupSearch.mandatoryFields"));
      return;
    }

    // define search type
    SearchType st = new SearchType();
    st.setTraverseConceptsToRoot(((Checkbox) getFellow("cbShowHierachyDetails")).isChecked());

    Boolean preferred = null;
    Radiogroup rgPreferred = (Radiogroup) getFellow("rgPreferred");
    if (rgPreferred.getSelectedIndex() == 0)
      preferred = true;
    else if (rgPreferred.getSelectedIndex() == 1)
      preferred = false;

    // start search and display results
    getConcepts().startSearch(code, term, st, preferred);
    searchActive = true;
  }

  /*public void onDeleteClicked()
   {
   if (SessionHelper.isUserLoggedIn())
   {
   if (codeSystem != null && codeSystemVersion != null)
   {
   logger.debug("onDeleteClicked()");
        
   if (Messagebox.show(Labels.getLabel("common.deleteCSVersion"), Labels.getLabel("common.deleteSystem"), Messagebox.YES | Messagebox.NO, Messagebox.QUESTION) 
   == Messagebox.YES)
   {
   logger.debug("deleting...");
   RemoveTerminologyOrConceptRequestType request = new RemoveTerminologyOrConceptRequestType();
   request.setLoginToken(SessionHelper.getSessionId());
   request.setDeleteInfo(new DeleteInfo());
   CodeSystem cs = new CodeSystem();
   cs.setId(codeSystem.getId());
   CodeSystemVersion csv = new CodeSystemVersion();
   csv.setVersionId(codeSystemVersion.getVersionId());
   cs.getCodeSystemVersions().add(csv);
   request.getDeleteInfo().setCodeSystem(cs);
   request.getDeleteInfo().setType(Type.CODE_SYSTEM_VERSION);
          
   RemoveTerminologyOrConceptResponseType response = WebServiceHelper.removeTerminologyOrConcept(request);
          
   Messagebox.show(response.getReturnInfos().getMessage());
          
   CodesystemGenericTreeModel.getInstance().reloadData();
   Executions.sendRedirect("");  // reload page
   }
   else
   {
   logger.debug("not deleting...");
   }
   }
   }
   }*/
  public void onPaging(Event event)
  {
    getConcepts().onPaging((PagingEvent) event);
  }

  private void showButtons()
  {
    CodeSystemEntityVersion csev = getConcepts().getSelection();
    //((Button) getFellow("buttonDetails")).setDisabled(csev == null);

    // edit, new, ...
    boolean loggedIn = SessionHelper.isUserLoggedIn();
    
    boolean isCodesystem = codeSystemVersion != null;
    boolean isValueset = valueSetVersion != null;
    
    
    ComponentHelper.setVisible("buttonNew", loggedIn && PropertiesHelper.getInstance().isGuiEditConceptsShowNewRoot() && isCodesystem, this);
    ComponentHelper.setVisibleAndDisabled("buttonNewSub", loggedIn && PropertiesHelper.getInstance().isGuiEditConceptsShowNewSub() && isCodesystem, csev == null, this);
    ComponentHelper.setVisibleAndDisabled("buttonEdit", loggedIn && PropertiesHelper.getInstance().isGuiEditConceptsShowEdit() && isCodesystem, csev == null, this);
    ComponentHelper.setVisibleAndDisabled("buttonDelete", loggedIn && PropertiesHelper.getInstance().isGuiEditConceptsShowDelete() && isCodesystem, csev == null, this);
    ComponentHelper.setVisibleAndDisabled("buttonDetails", PropertiesHelper.getInstance().isGuiEditConceptsShowDetails(), csev == null, this);

    // show button, if concept can be send back
    ComponentHelper.setVisibleAndDisabled("buttonAssumeConcept", isSendBack(), csev == null, this);

    ComponentHelper.setVisible("buttonEditVersion", loggedIn, this);
    ComponentHelper.setVisible("buttonDeleteVersion", loggedIn, this);

    // hide buttons, when in SELECT-Mode
    ComponentHelper.setVisible("buttonExport", externMode == false, this);
    
    // Collaboration
    boolean collabLoggedIn = SessionHelper.isCollaborationLoggedIn();
    ComponentHelper.setVisible("divCollaboration", collaborationActive, this);
    
    if(collaborationActive)
    {
      ComponentHelper.setVisibleAndDisabled("buttonCollabNewSub", collabLoggedIn && isCodesystem, csev == null, this);
      ComponentHelper.setVisibleAndDisabled("buttonCollabEdit", collabLoggedIn && isCodesystem, csev == null, this);
      ComponentHelper.setVisibleAndDisabled("buttonCollabDelete", collabLoggedIn && isCodesystem, csev == null, this);
    }
    
  
    

    //ComponentHelper.setVisible("buttonDeleteVersion", loggedIn, this);
    //ComponentHelper.setVisibleAndDisabled("buttonDeleteVersion", loggedIn, csev == null, this);
  }
  
  public void onCollabNewClicked()
  {
    
  }
  public void onCollabNewSubClicked()
  {
    
  }
  public void onCollabEditClicked()
  {
    
  }
  public void onCollabDeletedClicked()
  {
    
  }

  public void onAssumeConcept()
  {
    if (isSendBack() == false)
      return;

    SendBackHelper.sendBack(getConcepts().getSelection());
    //CodeSystemEntityVersion csev = concepts.getSelection();
    /*if (csev != null)
     {
     logger.debug("sendBack-postMethod:");
     //String javaScript = "window.top.postMessage('test', '\\*')"; // Aus sicherheitsgruenden sollte * ersetzt werden durch die domain des TS. Auf der empf�ngerseite kann dann        
     String javaScript = "window.top.postMessage('" + csev.getVersionId() + "', '\\*')"; // Aus sicherheitsgruenden sollte * ersetzt werden durch die domain des TS. Auf der empf�ngerseite kann dann        
     logger.debug(javaScript);
     Clients.evalJavaScript(javaScript);
      
     }*/
  }

  public void update(Object o, boolean edited)
  {
    // CSEV edited?
    if (o != null && o instanceof CodeSystem)
    {
      CodeSystem cs = (CodeSystem) o;

      /*codeSystem = cs;
       if(cs.getCodeSystemVersions() != null && cs.getCodeSystemVersions().size() > 0)
       codeSystemVersion = cs.getCodeSystemVersions().get(0);
      
       fillVersionList();*/
      SessionHelper.setValue("loadCS", cs.getId());

      if (cs.getCodeSystemVersions() != null && cs.getCodeSystemVersions().size() > 0)
      {
        SessionHelper.setValue("loadCSV", cs.getCodeSystemVersions().get(0).getVersionId());
      }

      SessionHelper.setValue("selectedCS", null);
      SessionHelper.setValue("selectedVS", null);

      CodesystemGenericTreeModel.getInstance().reloadData();
      Executions.sendRedirect(null);  // page reload
    }

  }

  /**
   * @return the lookForward
   */
  public boolean isLookForward()
  {
    return lookForward;
  }

  /**
   * @return the concepts
   */
  public ConceptsTree getConcepts()
  {
    return concepts;
  }

  /**
   * @return the sendBack
   */
  public boolean isSendBack()
  {
    return sendBack;
  }

}
