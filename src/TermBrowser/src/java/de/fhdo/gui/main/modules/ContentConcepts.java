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

import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.helper.DeepLinkHelper;
import de.fhdo.helper.ParameterHelper;
import de.fhdo.helper.SendBackHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.TreeHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.TreeModel;
import de.fhdo.models.TreeModelCSEV;
import de.fhdo.models.itemrenderer.TreeitemRendererCSEV;
import de.fhdo.models.TreeNode;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.DeleteInfo;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.RemoveValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.Type;
import java.util.ArrayList;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Sven Becker
 */
public class ContentConcepts extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  public static int CONTENTMODE_CODESYSTEM = 1;
  public static int CONTENTMODE_VALUESET = 2;
  protected Object source;
  protected long versionId = 0,
          id = 0;
  protected CodeSystemEntityVersion csevSelected;
  protected TreeitemRendererCSEV treeitemRenderer;
  protected TreeModelCSEV treeModel;
  protected Tree treeConcepts;
  protected TreeNode tnSelected;
  protected int contentMode;   // 1=CodeSystem, 2=ValueSets    
  protected boolean draggable = false;
  protected boolean droppable = false;
  protected boolean showLinkedConcepts = true;
//  private LinkedList<TreeNode> openPaths = new LinkedList<TreeNode>();
  protected ContentCSVSDefault parent;
  protected Radiogroup rgAssociationMode;
  private boolean isCollaboration;

  // Constructor
  public ContentConcepts()
  {
    isCollaboration = SessionHelper.isCollaborationActive();
  }

  @Override
  public void afterCompose(){
    treeConcepts        = (Tree) getFellow("treeConcepts");
    
    // load Parameter
    parent              = (ContentCSVSDefault) Executions.getCurrent().getAttribute("parent");
    source              = Executions.getCurrent().getAttribute("source");
    draggable           = (Boolean) Executions.getCurrent().getAttribute("draggable");
    droppable           = (Boolean) Executions.getCurrent().getAttribute("droppable");
    rgAssociationMode   = (Radiogroup) Executions.getCurrent().getAttribute("radioGroupAssociationMode");
    treeModel           = (TreeModelCSEV) Executions.getCurrent().getAttribute("treeModel");      

    String sTab = "";
    
    // id, vId und ContentMode bestimmen
    if (source instanceof CodeSystemVersion){
      id = ((CodeSystemVersion) source).getCodeSystem().getId();
      versionId = ((CodeSystemVersion) source).getVersionId();
      contentMode = ContentConcepts.CONTENTMODE_CODESYSTEM;
      
      ((Toolbar)getFellow("tbarExpandCollapse")).setVisible(false);      
      
      sTab = Labels.getLabel("common.codeSystem") + ": ";
    }
    else if (source instanceof ValueSetVersion){
      id = ((ValueSetVersion) source).getValueSet().getId();
      versionId = ((ValueSetVersion) source).getVersionId();
      contentMode = ContentConcepts.CONTENTMODE_VALUESET;
      
      ((Toolbar)getFellow("tbarExpandCollapse")).setVisible(true);
      sTab = Labels.getLabel("common.valueSet") + ": ";
      
      // Z.B.  Fuer den Editor
      if(droppable){
            treeConcepts.setDroppable("true");
            treeConcepts.addEventListener(Events.ON_DROP, new EventListener(){
              public void onEvent(Event event) throws Exception{
                if (event instanceof DropEvent){
                  Treerow trSource = (Treerow) ((DropEvent) event).getDragged();
                  Object targetObject = ((DropEvent) event).getTarget();
                  Object sourceObject = (CodeSystemEntityVersion) (trSource).getAttribute("object");

                  // Target bestimmen
                  if (targetObject == treeConcepts && sourceObject instanceof CodeSystemEntityVersion){
                    CodeSystemEntityVersion csev_source = (CodeSystemEntityVersion) sourceObject;

                    if (addConceptToValueSet(csev_source.getCodeSystemEntity().getId(), csev_source.getVersionId(), id, versionId))
                    {
                      ((TreeNode) ((TreeModel) treeConcepts.getModel()).get_root()).getChildren().add(new TreeNode(csev_source));
                      updateModel(true);
                    }
                  }
                }
              }
            });
      }
      
    }

    // Label vom Tab setzen     
    sTab += Executions.getCurrent().getAttribute("name") + " | " + Labels.getLabel("common.version") + ": " + Executions.getCurrent().getAttribute("versionName");
    //sTab += " | " + Labels.getLabel("common.validityRange") + ": " + Executions.getCurrent().getAttribute("validityRange");
    ((Tab) getFellow("tabConcepts")).setLabel(sTab);

    
    // Sortierreihenfolge anzeigen
    logger.debug("Show Sort()");
    
    try
    {
      Object o = SessionHelper.getValue("SortByField");
      if (o != null)
      {
        logger.debug("SortByField: " + o.toString());

        boolean ascending = true;
        Object oDirection = SessionHelper.getValue("SortDirection");
        if (oDirection != null && oDirection.equals("descending"))
          ascending = false;

        if (o.toString().equals("term"))
        {
          if (ascending)
            ((Treecol) getFellow("tcTerm")).setSortDirection("ascending");
          else
            ((Treecol) getFellow("tcTerm")).setSortDirection("descending");
        }
        else
        {
          if (ascending)
            ((Treecol) getFellow("tcCode")).setSortDirection("ascending");
          else
            ((Treecol) getFellow("tcCode")).setSortDirection("descending");
        }
      }
      else
      {
        // Default-Sortierung
        ((Treecol) getFellow("tcCode")).setSortDirection("ascending");
      }
    }
    catch (Exception ex)
    {
      logger.warn("Sortierung kann nicht initialisiert werden: " + ex.getLocalizedMessage());
    }


    //if(head.getInitSearchDirection()!= null && head.getInitSearchDirection().length() > 0)
    //      lh.setSortDirection(head.getInitSearchDirection());

    try
    {
      logger.debug("Show Button Assume, Method: " + ParameterHelper.getString("sbMethodName"));
      ((Button) getFellow("bAssume")).setVisible(SendBackHelper.getInstance().isActive());
    }
    catch (Exception e)
    {
    }

    // zeige Buttons wenn angemeldet        
    ((Button) getFellow("bEdit")).setVisible(SessionHelper.isUserLoggedIn());
    
    if(SessionHelper.isUserLoggedIn()){
    
        boolean allowed = true;
        if(source instanceof ValueSetVersion){
            ((Button) getFellow("bNew")).setVisible(false);
        }else if(source instanceof CodeSystemVersion){
      
            if(AssignTermHelper.isUserAllowed(((CodeSystemVersion)source).getCodeSystem())){
            
                ((Button) getFellow("bNew")).setVisible(true);
            }else{
                ((Button) getFellow("bNew")).setVisible(false);
            }
        }
    }
    
    ((Button) getFellow("bSearch")).setVisible(true);

    try
    {
      // Kollaborationssichtbarkeit
      if(isCollaboration){
          ((Button) getFellow("bProposeNewConcept")).setVisible(true);
          
          boolean res = false;
          if(contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
            res = AssignTermHelper.isAnyUserAssigned(id,"CodeSystem");
          
          if(contentMode == ContentConcepts.CONTENTMODE_VALUESET)
            res = AssignTermHelper.isAnyUserAssigned(id,"ValueSet");
          
          if(res){
              ((Button) getFellow("bProposeNewConcept")).setDisabled(false);
          }else{
              ((Button) getFellow("bProposeNewConcept")).setDisabled(true);
          }
      }else{
          ((Button) getFellow("bProposeNewConcept")).setVisible(false);
      }
    }
    catch (Exception e){e.printStackTrace();}

    initData();

    openDeepLinks();

    //Clients.clearBusy();
    logger.debug("AfterCompose fertig");
  }

  public void proposeNewConcept()
  {
    logger.debug("proposeNewConcept()");

    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      if (contentMode == CONTENTMODE_CODESYSTEM)
      {
        data.put("codeSystemId", id);
        data.put("codeSystemVersionId", versionId);
      }
      else if (contentMode == CONTENTMODE_VALUESET)
      {
        data.put("valueSetId", id);
        data.put("valueSetVersionId", versionId);
      }

      data.put("source", source);
      data.put("isExisting", false);

      Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }

  }

  public void proposeNewSubConcept(CodeSystemEntityVersion csev)
  {
    logger.debug("proposeNewSubConcept()");

    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      if (contentMode == CONTENTMODE_CODESYSTEM)
      {
        data.put("codeSystemId", id);
        data.put("codeSystemVersionId", versionId);

        data.put("parentCodeSystemEntityVersion", csev);
        data.put("source", source);
        data.put("isExisting", false);

        Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", this, data);
        w.doModal();
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }

  }

  public void proposeForExistingConceptOrMembership()
  {
    logger.debug("proposeForExistingConceptOrMembership()");

    try
    {
        Map<String, Object> data = new HashMap<String, Object>();
        if (contentMode == CONTENTMODE_CODESYSTEM)
        {
          data.put("codeSystemId", id);
          data.put("codeSystemVersionId", versionId);
          data.put("type", "concept");
        }
        else if (contentMode == CONTENTMODE_VALUESET)
        {
          data.put("valueSetId", id);
          data.put("valueSetVersionId", versionId);
          data.put("type", "conceptmembership");
        }

        data.put("TreeNode", tnSelected);
        data.put("CSEV", csevSelected);
        data.put("source", source);
        data.put("isExisting", true);
        

        Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", this, data);
        w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }

  }
  
  public void assumeConcept()
  {
    logger.debug("assumeConcept()");

    if (tnSelected != null)
    {
      logger.debug("csev: " + ParameterHelper.getString("sbContent"));

      Integer type = SendBackHelper.getInstance().getSendBackTypeCSEV();
      if (type != null && type.intValue() >= 0)
      {
        //sTypeName = SendBackHelper.getSendBackTypeByInteger(type);

        String s = "";
        //String separator = "^";
        String separator = ";;";

        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) tnSelected.getData();

        if (type == SendBackHelper.SENDBACK_NAME)
          s = csev.getCodeSystemConcepts().get(0).getTerm();
        else if (type == SendBackHelper.SENDBACK_DESCRIPTION)
          s = csev.getCodeSystemConcepts().get(0).getDescription();
        else if (type == SendBackHelper.SENDBACK_CODE)
          s = csev.getCodeSystemConcepts().get(0).getCode();
        else if (type == SendBackHelper.SENDBACK_NAME_DESCRIPTION)
          s = csev.getCodeSystemConcepts().get(0).getTerm() + separator + csev.getCodeSystemConcepts().get(0).getDescription();
        else if (type == SendBackHelper.SENDBACK_NAME_CODE)
          s = csev.getCodeSystemConcepts().get(0).getTerm() + separator + csev.getCodeSystemConcepts().get(0).getCode();
        else if (type == SendBackHelper.SENDBACK_DESCRIPTION_CODE)
          s = csev.getCodeSystemConcepts().get(0).getDescription() + separator + csev.getCodeSystemConcepts().get(0).getCode();
        else if (type == SendBackHelper.SENDBACK_NAME_DESCRIPTION_CODE)
          s = csev.getCodeSystemConcepts().get(0).getTerm() + separator + csev.getCodeSystemConcepts().get(0).getDescription() + separator + csev.getCodeSystemConcepts().get(0).getCode();

        logger.debug("Send back string: " + s);
        
        SendBackHelper.getInstance().sendBack(s);
      }
      else
      {
        logger.debug("sendback-Type ist null");
      }
    }
    else
    {
      // TODO Fehlermeldung ausgeben, dass ein Begriff ausgewählt werden muss
      logger.warn("Es muss ein Begriff ausgewählt sein");
    }
  }

  private TreeNode openNodeByName(TreeNode tnStart, String name)
  {
    Iterator<TreeNode> it = tnStart.getChildren().iterator();
    while (it.hasNext())
    {
      TreeNode tn2 = it.next();
      if (tn2.getData() instanceof CodeSystemEntityVersion && ((CodeSystemEntityVersion) tn2.getData()).getCodeSystemConcepts().isEmpty() == false)
      {
        CodeSystemConcept csc = ((CodeSystemEntityVersion) tn2.getData()).getCodeSystemConcepts().get(0);
        String term = csc.getTerm();

        term = DeepLinkHelper.getConvertedString(term, false);

        if (term.equalsIgnoreCase(name))
        {
          openNode(tn2);
          return tn2;
        }
      }
    }

    // nicht gefunden => null
    return null;
  }

  protected void openDeepLinks()
  {
    try
    {
      ArrayList<String> deepLinks = (ArrayList<String>) Executions.getCurrent().getAttribute("deepLinks");

      if (deepLinks == null || deepLinks.isEmpty())
        return;

      int i = 0;
      TreeNode tnSource = (TreeNode) treeModel.getTreeModel().getRoot();
      String name;

      while (tnSource != null)
      {//&& deepLinks.containsKey(String.valueOf(i))){
        //name     = deepLinks.get(String.valueOf(i));            
        name = deepLinks.get(i);
        tnSource = openNodeByName(tnSource, name);
        i++;
      }
    }
    catch (Exception e)
    {
    }
  }

  public void reloadData()
  {
    treeModel = null;
    initData();
  }

  protected void initDataCreateModel() throws Exception
  {
    if (treeModel == null){
      treeModel = new TreeModelCSEV(source, parent);
    }
    
    // Paging    
    if(contentMode == CONTENTMODE_VALUESET && treeModel.getTotalSize() > 200){
        treeConcepts.setMold("paging");
        treeConcepts.setPageSize(20);                        
    }
    else{
        treeConcepts.setMold("default");
    }
  }

  protected void initDataCreateItemRenderer()
  {
    int associationMode = 0;
    if (rgAssociationMode != null && rgAssociationMode.getSelectedItem() != null)
    {
      try
      {
        associationMode = Integer.valueOf(rgAssociationMode.getSelectedItem().getValue().toString());   // TODO überprüfen, ob .toString() funktionert
      }
      catch (Exception e)
      {
        associationMode = 0;
      }
    }

    treeitemRenderer = new TreeitemRendererCSEV(this, draggable, droppable, associationMode,source);
    treeitemRenderer.setContentMode(contentMode);
  }

  protected void initDataTreeProperties()
  {
    treeConcepts.setZclass("z-dottree");

    // 3. Spalte fuer Quell-CS
    if (contentMode == CONTENTMODE_VALUESET)
    {
      Treecol tcolSource = new Treecol(Labels.getLabel("common.source"));
      tcolSource.setParent(treeConcepts.getTreecols());

      // Breiten der Spalten anpassen
      tcolSource.setHflex("min");
      tcolSource.setId("tcolSource");
    }
  }

  protected void intiDataContextMenuCS(Menupopup contextMenu)
  {
    Menuitem miNewC1 = new Menuitem(Labels.getLabel("contentConcepts.newRootConcept"));
    miNewC1.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        showPopupConcept(PopupWindow.EDITMODE_CREATE, 3);
      }
    });

    if (SessionHelper.isUserLoggedIn())
    {
      miNewC1.setParent(contextMenu);
    }
  }

  protected void initData()
  {
    try
    {
      initDataCreateModel();
      initDataCreateItemRenderer();
      initDataTreeProperties();

      treeConcepts.setModel(treeModel.getTreeModel());
      treeConcepts.setItemRenderer(treeitemRenderer);

      // Context menu for TreeConcepts  (CSEV)
      Menupopup contextMenu = new Menupopup();
      treeConcepts.setContext(contextMenu);
      contextMenu.setParent(this);

      if (contentMode == CONTENTMODE_CODESYSTEM)
      {
        // Context menu for TreeConcepts  
        intiDataContextMenuCS(contextMenu);
      }
      else if (contentMode == CONTENTMODE_VALUESET)
      {
      }
      updateModel(false); // aktualisieren, sonst wird nix angezeigt
    }
    catch (Exception e)
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("Exception", e);
      data.put("ExceptionMessage", e.toString());
      Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupErrorMessage.zul", this, data);
      try
      {
        w.doHighlighted();
      }
      catch (SuspendNotAllowedException ex)
      {
        Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void showPopupConcept(int mode)
  {
    showPopupConcept(mode, 0);
  }

  public void showPopupConcept(int editMode, int hierarchyMode)
  {
    // kein Element ausgewählt, also nichts machen
    if (tnSelected == null && editMode != PopupWindow.EDITMODE_CREATE)
    {
      try
      {
        Messagebox.show(Labels.getLabel("contentConcepts.cantShowDetailsNoConceptSelected"));
      }
      catch (Exception ex)
      {
        Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, ex);
      }
      finally
      {
        return;
      }
    }

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("EditMode", editMode);          // PopupWindow.EDITMODE_
    data.put("ContentMode", contentMode);       // 1=CS, 2=VS
    data.put("Tree", treeConcepts);
    data.put("Id", id);
    data.put("VersionId", versionId);
    data.put("TreeNode", tnSelected);

    if (editMode == PopupWindow.EDITMODE_CREATE)
    {
      data.put("Association", hierarchyMode);  /* mode 1 = gleiche ebene, 2 = subebene, 3 = oberste ebene */
      if (csevSelected != null)
      {
        data.put("CSEVAssociated", csevSelected); // für assoziationen 
      }
    }
    else
    {
      data.put("CSEV", csevSelected);
    }
    try
    {
      final Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupConcept.zul", this, data);
      w.doOverlapped();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public void showPopupConceptSearch()
  {
    // kein login erforderlich
    if (true /*SessionHelper.isUserLoggedIn()*/)
    {
      try
      {
        Map<String, Object> data = new HashMap<String, Object>();
//                data.put("Id" , id);
//                data.put("VId", versionId);   
//                data.put("ContentMode", contentMode);   
        data.put("source", source);
        final Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupSearch.zul", null, data);
        w.doOverlapped();
      }
      catch (Exception e)
      {
        Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
      }
    }
    else
    {
      try
      {
        Messagebox.show(Labels.getLabel("common.loginRequired"));
      }
      catch (Exception ex)
      {
        Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  public void removeFromVS()
  {
    if (contentMode != CONTENTMODE_VALUESET)
      return;

    RemoveValueSetContentRequestType parameter = new RemoveValueSetContentRequestType();

    // Login
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    // valueset
    ValueSet vs = new ValueSet();
    ValueSetVersion vsv = new ValueSetVersion();
    vs.setId(id);
    vsv.setVersionId(versionId);
    vs.getValueSetVersions().add(vsv);
    parameter.setValueSet(vs);

    // CSEV (Concept)
    CodeSystemEntity cse = new CodeSystemEntity();
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    cse.setId(csevSelected.getCodeSystemEntity().getId());
    csev.setVersionId(csevSelected.getVersionId());
    cse.getCodeSystemEntityVersions().add(csev);
    parameter.getCodeSystemEntity().add(cse);

    // Ausführen
    RemoveValueSetContentResponseType response = WebServiceHelper.removeValueSetContent(parameter);

    // Meldung
    try
    {
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        if (response.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.authoring.OverallErrorCategory.WARN)
        {
          Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage());
        }
      }
      else
        Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage());
    }
    catch (Exception ex)
    {
      Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void removeCSEV()
  {
    if (contentMode != CONTENTMODE_CODESYSTEM)
      return;

    RemoveTerminologyOrConceptRequestType parameter = new RemoveTerminologyOrConceptRequestType();
    parameter.setDeleteInfo(new DeleteInfo());
    
    // Login
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    CodeSystem cs = new CodeSystem();
    CodeSystemVersion csv = new CodeSystemVersion();
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    
    cs.setId(id);
    csv.setVersionId(versionId);
    cs.getCodeSystemVersions().add(csv);
    parameter.getDeleteInfo().setCodeSystem(cs);
    csev.setVersionId(csevSelected.getVersionId());
    parameter.getDeleteInfo().setCodeSystemEntityVersion(csev);
    parameter.getDeleteInfo().setType(Type.CODE_SYSTEM_ENTITY_VERSION);

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

  public boolean addConceptToValueSet(long cseId, long csevId, long vsId, long vsvId)
  {
    CreateValueSetContentRequestType parameter = new CreateValueSetContentRequestType();

    // Login
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    // valueset
    ValueSet vs = new ValueSet();
    ValueSetVersion vsv = new ValueSetVersion();
    vs.setId(vsId);
    vsv.setVersionId(vsvId);
    vs.getValueSetVersions().add(vsv);
    parameter.setValueSet(vs);

    // CSEV (Concept)
    CodeSystemEntity cse = new CodeSystemEntity();
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    cse.setId(cseId);
    csev.setVersionId(csevId);
    cse.getCodeSystemEntityVersions().add(csev);
    parameter.getCodeSystemEntity().add(cse);

    // WS Aufruf
    CreateValueSetContentResponse.Return response = WebServiceHelper.createValueSetContent(parameter);

    // Meldung
    try
    {
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        if (response.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.authoring.OverallErrorCategory.WARN)
        {
          Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage());
        }
        else
          return true;
      }
      else
        Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage());
    }
    catch (Exception ex)
    {
      Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public void onSelect(){
      onSelect(null);
  }
  
  public void onSelect(Treeitem ti)
  {
      if(ti != null)
          treeConcepts.setSelectedItem(ti);
      
    /* Tree Item mit links oder rechts angeklickt */
    if (treeConcepts != null && treeConcepts.getSelectedItem() != null && treeConcepts.getSelectedItem().getValue() != null)
    {
      tnSelected   = (TreeNode) treeConcepts.getSelectedItem().getValue();
      csevSelected = (CodeSystemEntityVersion) tnSelected.getData();
      
      boolean allowed = true;
      if(source instanceof ValueSetVersion){
      
          allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion)source).getValueSet());
            
      }else if(source instanceof CodeSystemVersion){
      
          allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion)source).getCodeSystem());
      }
          
      if(allowed){
          ((Button) getFellow("bEdit")).setDisabled(false);
      }
      
      ((Button) getFellow("bDetails")).setDisabled(false);
    }
  }

  public void selectTreeNode(TreeNode tn)
  {
    tnSelected   = tn;
    csevSelected = (CodeSystemEntityVersion) tnSelected.getData();
  }

  public void openNode(TreeNode tn){
      openNode(tn, true);
  }
  
  public void openNode(TreeNode tn, boolean updateModel)
  {     
    // TreeNode hat keine Kinder => nichts machen. (dummies wurden schon früher mal gelöscht)
    if (tn.isLeaf() || tn.getChildCount() < 1)
      return;

    // => Kinder vorhanden

    // Kinder von tnSelected sind entweder a) "richtige" Knoten oder b) DUMMYIES
    // a) Richtige Kinder: Daten wurden schon frueher geladen und muessen nur angezeigt werden wenn tnSelect angeklickt wird
    if (((TreeNode) tn.getChildren().get(0)).getData() instanceof CodeSystemEntityVersion)
    {
//            renderAllChildren(); // nicht updateModel weil sonst selektion verloren geht
    }
    // b) DUMMY-Kinder: DUMMIES löschen und die richtigen Daten nachladen
    else
    {
      // Dummies entfernen
      tn.getChildren().clear();

      // taxonomische Assoziationen laden   assoType 2 = taxonomisch
      loadChildren(tn, 2, updateModel);

      // LinkedConcepts laden   assoType 4 = link
      // TODO: Einstellung in UserAccount ob Linked Concepts angezeigt werdne sollen
//            if(user.showLinkedConcepts())
//                loadChildren(tn,4);
    }
    treeModel.getTreeModel().addOpenPath(treeModel.getTreeModel().getPath(tn));    
  }

  public void closeNode(TreeNode tn)
  {
    treeConcepts.removeAttribute("isOpen"); // TODO: N?tig?       
  }

  private void loadChildren(TreeNode tn, int associationKind){
      loadChildren(tn, associationKind, true);
  }
  
  private void loadChildren(TreeNode tn, int associationKind, boolean updateModel)
  {
    treeModel.loadChildren(tn, associationKind);

    // Modell neu laden und Baum neu rendern (die neuen Knoten anzeigen) falls neue Knoten nachgeladen wurden 
    updateModel(updateModel);
  }

  public void updateModel(boolean updatePath)
  {
    // TODO: Selektion geht bei setModel verloren,  daher erneut selektieren
    // geht aber nicht so einfach, da das tiSelect ja auch futsch ist.  also irgendwie ne ID oder sowas speichern?  k.A.             
    treeConcepts.setModel(treeModel.getTreeModel());
  }
  
  public void expandTree()
  {
    TreeHelper.doCollapseExpandAll(treeConcepts, true, this);
  }

  public void collapseTree()
  {
    TreeHelper.doCollapseExpandAll(treeConcepts, false, this);
  }
  
  public CodeSystemEntityVersion getCsev_Selected()
  {
    return csevSelected;
  }

  public void setCsev_Selected(CodeSystemEntityVersion csev_Selected)
  {
    this.csevSelected = csev_Selected;
  }

  public void setAssociationMode(int i)
  {
    rgAssociationMode.setSelectedIndex(i);
  }

  public int getAssociationMode()
  {
    return Integer.valueOf(rgAssociationMode.getSelectedItem().getValue().toString());  // TODO überprüfen, ob .toString() funktionert
  }

  public void setContentMode(int contentMode)
  {
    this.contentMode = contentMode;
  }

  public int getContentMode()
  {
    return contentMode;
  }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
  
  /**
   * @return the isCollaboration
   */
  public boolean isIsCollaboration()
  {
    return isCollaboration;
  }

  /**
   * @param isCollaboration the isCollaboration to set
   */
  public void setIsCollaboration(boolean isCollaboration)
  {
    this.isCollaboration = isCollaboration;
  }
}