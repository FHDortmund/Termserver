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
import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.ListMetadataParameterResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContents;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.search.PagingResultType;
import de.fhdo.terminologie.ws.search.PagingType;
import de.fhdo.terminologie.ws.search.SortingType;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.MetadataParameter;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ConceptsTree implements IUpdateModal
{

  protected static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Tree treeConcepts;
  private DefaultTreeModel treeModel;

  private enum DISPLAY_MODE
  {

    TREEVIEW, LARGEDATA
  }
  private DISPLAY_MODE displayMode;

  public enum CONTENT_TYPE
  {

    CODESYSTEM, VALUESET
  }
  private CONTENT_TYPE contentType;

  private long codeSystemId;
  private long valueSetId;
  private long codeSystemVersionId;
  private long valueSetVersionId;

  private ContentConcepts conceptsWindow;
  private String msg;

  private PagingResultType paging;
  private int currentPageIndex;

  private List<MetadataParameter> listMetadata;

  public ConceptsTree()
  {
  }

  public ConceptsTree(Tree treeConcepts, ContentConcepts concepsWindow)
  {
    this.treeConcepts = treeConcepts;
    this.conceptsWindow = concepsWindow;
  }

  public void initData()
  {
    logger.debug("ConceptsTree - initData()");
    msg = "";

    currentPageIndex = 0;

    displayMode = DISPLAY_MODE.TREEVIEW;

    if (valueSetVersionId > 0)
      contentType = CONTENT_TYPE.VALUESET;
    else
      contentType = CONTENT_TYPE.CODESYSTEM;

    fillTree();
    loadMetadata();
  }

  private void loadMetadata()
  {
    logger.debug("loadMetadata()");
    listMetadata = new LinkedList<MetadataParameter>();

    ListMetadataParameterRequestType request = new ListMetadataParameterRequestType();
    request.setLoginToken(SessionHelper.getSessionId());
    if (contentType == CONTENT_TYPE.CODESYSTEM)
    {
      request.setCodeSystem(new CodeSystem());
      request.getCodeSystem().setId(codeSystemId);
      logger.debug("... with code system id: " + codeSystemId);
    }
    else if (contentType == CONTENT_TYPE.VALUESET)
    {
      request.setValueSet(new ValueSet());
      request.getValueSet().setId(valueSetId);
      logger.debug("... with value set id: " + valueSetId);
    }

    ListMetadataParameterResponse.Return response = WebServiceHelper.listMetadataParameter(request);
    logger.debug("Webservice result: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      listMetadata = response.getMetadataParameter();
    }

  }

  private void fillTree()
  {
    ComponentHelper.setVisible("treecolSource", valueSetVersionId > 0, conceptsWindow);

    List<CodeSystemEntity> cseList = null;

    if (displayMode == DISPLAY_MODE.LARGEDATA)
    {
      // load lists without main classes
      cseList = createRootTreeNodesForModel(false);
    }
    else
    {
      // load root concepts, either from a code system or a value set
      cseList = createRootTreeNodesForModel(true);

      if (cseList != null && cseList.size() == 0)
      {
        // load lists without main classes
        cseList = createRootTreeNodesForModel(false);
        displayMode = DISPLAY_MODE.LARGEDATA;
      }
    }

    treeModel = null;
    if (cseList != null)
    {
      logger.debug("CSE list found, count: " + cseList.size());

      // load root concepts to tree
      TreeNode tnRoot = new DefaultTreeNode(null, createTreeNodeCSEList(cseList));
      treeModel = new DefaultTreeModel(tnRoot);

    }
    else
    {
      // TODO Fehlermeldung ausgeben (Ausrufezeichen mit Meldung)
      //msg = 
    }

    if (treeModel != null)
    {
      logger.debug("set model and renderer");
      treeConcepts.setItemRenderer(new TreeitemRendererCSEV(this));
      treeConcepts.setModel(treeModel);
    }

    if (msg.length() == 0)
    {
      // no error, show tree
      conceptsWindow.getFellow("message").setVisible(false);
      conceptsWindow.getFellow("treeConcepts").setVisible(true);
    }
    else
    {
      // error, show message
      conceptsWindow.getFellow("treeConcepts").setVisible(false);
      conceptsWindow.getFellow("message").setVisible(true);

      ((Label) conceptsWindow.getFellow("labelMessage")).setValue(msg);
    }
  }

  private List<CodeSystemEntity> createRootTreeNodesForModel(boolean onlyMainClasses)
  {
    List<CodeSystemEntity> cseList = null;

    if (codeSystemVersionId > 0)
    {
      // do web service call
      ListCodeSystemConceptsRequestType parameter = createParameterForCodeSystems(onlyMainClasses);
      ListCodeSystemConceptsResponse.Return response = WebServiceHelper.listCodeSystemConcepts(parameter);

      logger.debug("ListCodeSystemConceptsResponse: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        cseList = response.getCodeSystemEntity();

        if (response.getPagingInfos() != null)
        {
          // Paging Parameter auswerten
          enablePaging(response.getPagingInfos());
        }
        else
        {
          disablePaging();
        }
      }
      else
      {
        // Fehlermeldung ausgeben (Ausrufezeichen mit Meldung)
        msg = response.getReturnInfos().getMessage();
      }
    }
    else if (valueSetVersionId > 0)
    {
      ListValueSetContentsRequestType parameter = createParameterForValueSets();
      ListValueSetContentsResponse.Return response = WebServiceHelper.listValueSetContents(parameter);

      logger.debug("ListValueSetContentsResponse: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        cseList = response.getCodeSystemEntity();

        disablePaging();
      }
      else
      {
        // Fehlermeldung ausgeben (Ausrufezeichen mit Meldung)
        msg = response.getReturnInfos().getMessage();
      }
    }

    return cseList;
  }

  private void enablePaging(PagingResultType _paging)
  {
    paging = _paging;
    logger.debug("do paging: " + paging.getMessage());
    logger.debug("count: " + paging.getCount());
    logger.debug("page size: " + paging.getPageSize());
    logger.debug("max page size: " + paging.getMaxPageSize());
    logger.debug("currentPageIndex: " + currentPageIndex);

//    treeConcepts.setMold("paging");
//    
//    treeConcepts.getPaginal().setTotalSize(paging.getCount());
//    treeConcepts.getPaginal().setActivePage(currentPageIndex);
//    treeConcepts.getPaginal().setDetailed(true);
//    
//    treeConcepts.setPageSize(Integer.parseInt(paging.getPageSize()));
    Paging pagingComp = (Paging) conceptsWindow.getFellow("paging");
    pagingComp.setTotalSize(paging.getCount());
    pagingComp.setActivePage(currentPageIndex);
    pagingComp.setPageSize(Integer.parseInt(paging.getPageSize()));
    pagingComp.setDetailed(true);
    pagingComp.setVisible(true);

    //pagingComp.removeEventListener(msg, logger)
    pagingComp.addEventListener(Events.ON_SELECT, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        logger.debug("ON_SELECT");
      }
    });

    pagingComp.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        logger.debug("ON_CHANGE");
      }
    });
  }

  private void disablePaging()
  {
    treeConcepts.setMold("");

    paging = null;

    Paging pagingComp = (Paging) conceptsWindow.getFellow("paging");
    pagingComp.setVisible(false);
  }

  public void onPaging(PagingEvent event)
  {
    logger.debug("onPagingEvent, pageIndex: " + event.getActivePage());
    currentPageIndex = event.getActivePage();

    // reload data with current index
    fillTree();
  }

  private List<TreeNode> createTreeNodeCSEList(List<CodeSystemEntity> dataList)
  {
    List<TreeNode> list = new ArrayList<TreeNode>();

    for (CodeSystemEntity cse : dataList)
    {
      CodeSystemEntityVersion csev = cse.getCodeSystemEntityVersions().get(0);
      csev.setCodeSystemEntity(cse);

      if ((csev.isIsLeaf() != null && csev.isIsLeaf().booleanValue())
              || contentType == CONTENT_TYPE.VALUESET)
      {
        // end element (leaf)
        list.add(new DefaultTreeNode(csev));
      }
      else
      {
        list.add(new DefaultTreeNode(csev, new LinkedList<TreeNode>()));  // temporäre Liste, wird nachgeladen, wenn Klick darauf
        //list.add(new DefaultTreeNode(cse, createTreeNodeCSEVList(csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2())));
      }
    }

    return list;
  }

  public CodeSystemEntityVersion getSelection()
  {
    if (treeConcepts != null)
    {
      Treeitem treeItem = treeConcepts.getSelectedItem();
      if (treeItem != null)
      {
        Object data = treeItem.getValue();
        if (data != null && data instanceof CodeSystemEntityVersion)
        {
          return (CodeSystemEntityVersion) data;
        }
      }
    }
    return null;
  }

  public void onConceptSelect(boolean forceLoading, boolean openIfClosed)
  {
    logger.debug("onConceptSelect()");

    CodeSystemEntityVersion csev = getSelection();
    //Treeitem treeItem = treeConcepts.getSelectedItem();
    //Object data = treeItem.getValue();
    //if (data != null && data instanceof CodeSystemEntityVersion)
    if (csev != null)
    {
      Treeitem treeItem = treeConcepts.getSelectedItem();
      logger.debug("isOpen: " + treeItem.isOpen());

      Object o = treeItem.getAttribute("loaded");
      if (o != null && ((Boolean) o).booleanValue())
      {
        // data loaded
        logger.debug("data loaded");
        return;
      }

      if (treeItem.isOpen() == false || forceLoading)
      {
        logger.debug("data is CodeSystemEntityVersion, versionId: " + csev.getVersionId());

        TreeNode treeNode = (TreeNode) treeItem.getAttribute("treenode");
        if (treeNode.isLeaf())
        {
          // nothing to do
        }
        else
        {
          // load children
          logger.debug("load children");

          if (openIfClosed)
          {
            // open path in view
            treeModel.addOpenPath(treeModel.getPath(treeNode));
          }

          loadChildren(csev.getVersionId(), treeNode);
          treeItem.setAttribute("loaded", true);
        }
      }

    }

  }

  /**
   * open concept details with selected entry
   */
  public void openConceptDetails()
  {
    CodeSystemEntityVersion csev = getSelection();
    if (csev != null)
      openConceptDetails(csev.getVersionId());
  }

  public void createConcept(PopupConcept.HIERARCHYMODE hierarchyMode, long CSEVAssociatedVersionId)
  {
    logger.debug("createConcept()");

    Map<String, Object> data = new HashMap<String, Object>();

    data.put("EditMode", PopupConcept.EDITMODES.CREATE);

    if (codeSystemVersionId > 0)
      data.put("ContentMode", PopupConcept.CONTENTMODE.CODESYSTEM);
    else if (valueSetVersionId > 0)
      data.put("ContentMode", PopupConcept.CONTENTMODE.VALUESET);

    data.put("CodeSystemId", codeSystemId);
    data.put("ValueSetId", valueSetId);

    data.put("CodeSystemVersionId", codeSystemVersionId);
    data.put("ValueSetVersionId", valueSetVersionId);

    data.put("Association", hierarchyMode);  // mode 1 = gleiche ebene, 2 = subebene, 3 = oberste ebene 
    data.put("CSEVAssociated", CSEVAssociatedVersionId);

    data.put("MetadataList", listMetadata);

    try
    {
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupConcept.zul", null, data);
      ((PopupConcept) w).setUpdateListener(this);

      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public void deleteConcept()
  {
    CodeSystemEntityVersion csev = getSelection();
    if (csev != null)
      deleteConcept(csev.getVersionId());
  }

  public void deleteConcept(long csev_id)
  {
    logger.debug("deleteConcept, csev-id: " + csev_id);

    if (Messagebox.show(Labels.getLabel("common.deleteConcept"), Labels.getLabel("common.delete"), Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
            == Messagebox.YES)
    {
      UpdateConceptStatusRequestType request = new UpdateConceptStatusRequestType();
      request.setLoginToken(SessionHelper.getSessionId());
      request.setCodeSystemEntity(new CodeSystemEntity());
      CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
      csev.setVersionId(csev_id);
      csev.setStatusDeactivated(Definitions.STATUS_DEACTIVATED_DELETED);
      csev.setStatusDeactivatedDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
      csev.setStatusVisibility(Definitions.STATUS_VISIBILITY_INVISIBLE);
      csev.setStatusVisibilityDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
      request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

      UpdateConceptStatusResponse.Return response = WebServiceHelper.updateConceptStatus(request);

      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        // remove item from tree
        Treeitem treeItem = treeConcepts.getSelectedItem();
        if (treeItem != null)
        {
          TreeNode selectedTreeNode = (TreeNode) treeItem.getAttribute("treenode");

          if (selectedTreeNode != null)
          {
            TreeNode parentNode = selectedTreeNode.getParent();
            logger.debug("Anzahl Node-Kinder: " + parentNode.getChildCount());
            if (parentNode.getChildCount() > 1)
            {
              parentNode.remove(selectedTreeNode);
            }
            else
            {
              // Sonderfall (!)
              treeItem.detach();
              parentNode.remove(selectedTreeNode);
            }
          }
        }

      }
      else
      {
        Messagebox.show(response.getReturnInfos().getMessage());
      }
    }

  }

  public void maintainConcept()
  {
    CodeSystemEntityVersion csev = getSelection();
    if (csev != null)
    {
      if(csev.getStatusVisibility() == Definitions.STATUS_VISIBILITY_VISIBLE && 
         csev.getStatusDeactivated() <= Definitions.STATUS_DEACTIVATED_ACTIVE)
      {
        maintainConcept(csev.getVersionId());
      }
    }
  }

  public void maintainConcept(long csev_id)
  {
    logger.debug("maintainConcept, csev-id: " + csev_id);

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("VersionId", csev_id);
    data.put("EditMode", PopupConcept.EDITMODES.MAINTAIN);

    if (codeSystemVersionId > 0)
      data.put("ContentMode", PopupConcept.CONTENTMODE.CODESYSTEM);
    else if (valueSetVersionId > 0)
      data.put("ContentMode", PopupConcept.CONTENTMODE.VALUESET);

    data.put("CodeSystemId", codeSystemId);
    data.put("ValueSetId", valueSetId);

    data.put("CodeSystemVersionId", codeSystemVersionId);
    data.put("ValueSetVersionId", valueSetVersionId);

    data.put("MetadataList", listMetadata);

    try
    {
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupConcept.zul", null, data);
      ((PopupConcept) w).setUpdateListener(this);

      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  public void openConceptDetails(long csev_id)
  {
    logger.debug("openConceptDetails, csev-id: " + csev_id);

    Map<String, Object> data = new HashMap<String, Object>();
    data.put("VersionId", csev_id);
    data.put("EditMode", PopupConcept.EDITMODES.DETAILSONLY);

    if (codeSystemVersionId > 0)
      data.put("ContentMode", PopupConcept.CONTENTMODE.CODESYSTEM);
    else if (valueSetVersionId > 0)
      data.put("ContentMode", PopupConcept.CONTENTMODE.VALUESET);

    data.put("CodeSystemId", codeSystemId);
    data.put("ValueSetId", valueSetId);

    data.put("CodeSystemVersionId", codeSystemVersionId);
    data.put("ValueSetVersionId", valueSetVersionId);

    data.put("MetadataList", listMetadata);

    /*    
     data.put("Tree", treeConcepts);
     data.put("Id", id);
     data.put("TreeNode", tnSelected);

     if (editMode == PopupConcept.EDITMODES.CREATE)
     {
     data.put("Association", hierarchyMode);  // mode 1 = gleiche ebene, 2 = subebene, 3 = oberste ebene 
     if (csevSelected != null)
     {
     data.put("CSEVAssociated", csevSelected); // für assoziationen
     logger.debug("CSEVAssociated: " + csevSelected.getVersionId());
     }
     }
     else
     {
     data.put("CSEV", csevSelected);
     }*/
    try
    {
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupConcept.zul", null, data);
      //((PopupConcept)w).setIU

      w.doModal();
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  /**
   * loads children for a specific TreeNode and append them
   *
   * @param csev_id
   * @param treeNode
   */
  private void loadChildren(long csev_id, TreeNode treeNode)
  {
    logger.debug("loadChildren with csev_id: " + csev_id);

    try
    {
      // TODO Ladebalken erzeugen

      ListConceptAssociationsRequestType parameter_ListCA = new ListConceptAssociationsRequestType();

      parameter_ListCA.setLoginToken(SessionHelper.getSessionId());

      // CSE erstellen und CSEV einsetzen (CSE Nachladen falls noetig)
      parameter_ListCA.setCodeSystemEntity(new CodeSystemEntity());
      CodeSystemEntityVersion csev_ws = new CodeSystemEntityVersion();
      csev_ws.setVersionId(csev_id);
      parameter_ListCA.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev_ws);

      // Zusatzinformationen anfordern um anzuzeigen ob noch Kinder vorhanden sind oder nicht
      parameter_ListCA.setLookForward(true);
      parameter_ListCA.setDirectionBoth(true);

      // call webservice
      de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse.Return response;
      response = WebServiceHelper.listConceptAssociations(parameter_ListCA);

      logger.debug("webservice response: " + response.getReturnInfos().getMessage());

      // response Speichern fuer spaetere Verwendung?
      //tn_Parent.setResponseListConceptAssociations(response);
      if (response.getCodeSystemEntityVersionAssociation() == null)
        return;

      // das TreeModel um die entsprechenden (unter)Konzepte erweitern
      // Für alle Kinder: Dem Baum erweitern und ggf. DUMMIES in die neuen Kinder einfügen
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.OK)
      {
        logger.debug("Anzahl: " + response.getCodeSystemEntityVersionAssociation().size());

        for (CodeSystemEntityVersionAssociation cseva : response.getCodeSystemEntityVersionAssociation())
        {
          // Assiziierte CSEV
          CodeSystemEntityVersion csev_Child = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();

          // Pruefen auf null
          if (csev_Child == null)
            continue;

          switch (cseva.getAssociationKind())
          {
            case 1:
              // ontological, do nothing for now
              break;
            case 2:
              // taxonomic, add childs to TreeNode
              addChildElement(csev_Child, treeNode);
              break;
            case 3:
              // cross-mapping
              break;
            case 4:
              // link
              break;
          }

        }

      }
    }
    catch (Exception ex)
    {
      Messagebox.show(Labels.getLabel("common.error") + "\n\n" + Labels.getLabel("treeModelCSEV.loadDataFromWsFailed") + "\n\n" + ex.getLocalizedMessage());
      LoggingOutput.outputException(ex, this);
    }

  }

  private void addChildElement(CodeSystemEntityVersion csevChild, TreeNode treeNode)
  {

    if (treeNode.isLeaf())
    {
      logger.debug("addChildElement: " + csevChild.getVersionId() + " - isLeaf");
      // Besonderer Fall, da es noch kein Subelement gibt
      // hier muss der TreeNode erneut mit Kindern angelegt werden
      // Aufbau ZUL
      // Tree 
      //  -> Treechildren 
      //     -> Treeitem 
      //        -> Treerow
      //           -> Component
      //        -> Treechildren
      //           -> Treeitem
      //              -> Treerow
      // Aufbau Model
      // Root
      // -> (Default)TreeNode
      //    -> (Default)TreeNode
      //    -> (Default)TreeNode
      // -> (Default)TreeNode
      //    -> (Default)TreeNode
      //    -> (Default)TreeNode
      //    -> (Default)TreeNode
      // -> (Default)TreeNode
      List<TreeNode> children = new LinkedList<TreeNode>();
      children.add(new DefaultTreeNode(csevChild));

      //TreeNode node = new DefaultTreeNode(lastSelectedTreeitem.getValue(), children);  // selectedTreeNode
      TreeNode node = new DefaultTreeNode(treeNode.getData(), children);  // selectedTreeNode

      TreeNode parent = treeNode.getParent();
      int index = parent.getIndex(treeNode);
      parent.remove(treeNode);
      parent.insert(node, index);
    }
    else
    {
      logger.debug("addChildElement: " + csevChild.getVersionId());
      //if(csevChild.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null &&
      //        csevChild.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().size() > 0)
      if (csevChild.isIsLeaf() != null && csevChild.isIsLeaf().booleanValue())
      {
        treeNode.add(new DefaultTreeNode(csevChild));
      }
      else
      {
        treeNode.add(new DefaultTreeNode(csevChild, new LinkedList<TreeNode>()));
      }

    }
  }

  private ListValueSetContentsRequestType createParameterForValueSets()
  {
    ListValueSetContentsRequestType parameter = new ListValueSetContentsRequestType();

    // CodeSystemEntity
    parameter.setValueSet(new ValueSet());
    ValueSetVersion vsv = new ValueSetVersion();
    vsv.setVersionId(valueSetVersionId);
    parameter.getValueSet().getValueSetVersions().add(vsv);

    parameter.setReadMetadataLevel(false);

    logger.debug("create reqeust parameter for vsv-id: " + valueSetVersionId);

    // login
    if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    // sort parameter
    parameter.setSortingParameter(createSortingParameter());

    return parameter;
  }

  private ListCodeSystemConceptsRequestType createParameterForCodeSystems(boolean onlyMainClasses)
  {
    ListCodeSystemConceptsRequestType parameter = new ListCodeSystemConceptsRequestType();

    // CodeSystemEntity
    parameter.setCodeSystemEntity(new CodeSystemEntity());

    // Nur Hauptachsen zurückgeben? (CodeSystemVersionEntityMembership)
    if (onlyMainClasses)
    {
      CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
      csvem.setIsMainClass(true);
      parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(csvem);
    }

    // CodeSystem(VersionsID) angeben
    CodeSystemVersion csv = new CodeSystemVersion();
    csv.setVersionId(codeSystemVersionId);
    parameter.setCodeSystem(new CodeSystem());
    parameter.getCodeSystem().getCodeSystemVersions().add(csv);

    logger.debug("create reqeust parameter for csv-id: " + codeSystemVersionId);

    // login
    if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    // PagingParameter
    if (paging != null)
    {
      parameter.setPagingParameter(new PagingType());
      parameter.getPagingParameter().setPageIndex(currentPageIndex);
      parameter.getPagingParameter().setPageSize(paging.getPageSize());
      //parameter.getPagingParameter().setUserPaging(true);
    }
    // TODO if (pagingTypeWS != null)
    //  parameter.setPagingParameter(pagingTypeWS);
    // SearchType: Parameter für die Suche nach Konzepten mit bestimmten "term"
    /*TODO if (searchTypeWS != null && (searchTerm != null || searchCode != null))
     {
     parameter.setSearchParameter(searchTypeWS);
     CodeSystemEntity cse = new CodeSystemEntity();
     CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
     CodeSystemConcept csc = new CodeSystemConcept();

     cse.getCodeSystemEntityVersions().add(csev);
     csev.getCodeSystemConcepts().add(csc);
     csc.setTerm(searchTerm);
     csc.setCode(searchCode);
     // TODO Muss noch als Parameter, der in der GUI mittels Checkbox/Radiogroup gesetzt werden kann, eingelesen werden
     csc.setIsPreferred(preferred);

     parameter.setCodeSystemEntity(cse);
     }*/
    // damit Linked Concepts gefunden werden (muss nach erstellung von SearchParameter erfolgen und false sein, falls traverse to root genutzt wird)    
    if (parameter.getSearchParameter() != null)
      parameter.setLookForward(!parameter.getSearchParameter().isTraverseConceptsToRoot());
    else
      parameter.setLookForward(true);

    // sort parameter
    parameter.setSortingParameter(createSortingParameter());

    return parameter;
  }

  private SortingType createSortingParameter()
  {
    SortingType st = null;
    /*TODO Object o = SessionHelper.getValue("SortByField");
     if (o != null)
     {
     st = new SortingType();
     if (o.toString().equals("term"))
     {
     st.setSortBy(SortByField.TERM);
     }
     else
     {
     st.setSortBy(SortByField.CODE);
     }
     }
     o = SessionHelper.getValue("SortDirection");
     if (o != null)
     {
     if (st == null)
     st = new SortingType();

     if (o.toString().equals("descending"))
     {
     st.setSortDirection(SortDirection.DESCENDING);
     }
     else
     {
     st.setSortDirection(SortDirection.ASCENDING);
     }
     }*/
    return st;
  }

  public void update(Object o, boolean edited)
  {
    logger.debug("update tree, edited: " + edited);

    if (o instanceof CodeSystemEntity)
    {
      CodeSystemEntity cse = (CodeSystemEntity) o;

      //TreeNode newTreeNode = new TreeNode(csev);
      //TreeNode tn = new DefaultTreeNode(null, createTreeNodeCSEList(cseList));
      CodeSystemEntityVersion csev = cse.getCodeSystemEntityVersions().get(0);
      csev.setCodeSystemEntity(cse);

      TreeNode tn = null;

      if (edited)
      {
        // update data in Treeitem
        logger.debug("update data in Treeitem");

        Treeitem treeItem = treeConcepts.getSelectedItem();
        if (treeItem != null)
        {
          TreeNode selectedTreeNode = (TreeNode) treeItem.getAttribute("treenode");

          if (selectedTreeNode != null)
          {
            if (selectedTreeNode.isLeaf())
            {
              selectedTreeNode.setData(csev);
            }
            else
            {
              TreeNode parent = selectedTreeNode.getParent();
              int index = parent.getIndex(selectedTreeNode);
              parent.remove(selectedTreeNode);
              selectedTreeNode.setData(csev);
              parent.insert(selectedTreeNode, index);

              selectedTreeNode.getChildren().clear();

              // select treeitem and set loaded = fals to force reloading
              logger.debug("search treeitem...");
              for (Treeitem ti : treeConcepts.getItems())
              {
                CodeSystemEntityVersion csev_ti = ti.getValue();
                if (csev_ti.getVersionId().longValue() == csev.getVersionId())
                {
                  // found
                  logger.debug("found");
                  ti.setAttribute("loaded", false);
                  treeConcepts.selectItem(ti);
                  break;
                }
              }
            }
          }
        }

      }
      else
      {
        if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() > 0)
        {
          // new sub concept
          Treeitem treeItem = treeConcepts.getSelectedItem();
          if (treeItem != null)
          {
            tn = new DefaultTreeNode(csev);

            TreeNode selectedTreeNode = (TreeNode) treeItem.getAttribute("treenode");
            if (selectedTreeNode.isLeaf())
            {
              // parent has to be recreated
              logger.debug("recreate treenode");
              List<TreeNode> children = new LinkedList<TreeNode>();
              //children.add(tn);

              TreeNode node = new DefaultTreeNode(treeItem.getValue(), children);

              TreeNode parent = selectedTreeNode.getParent();
              int index = parent.getIndex(selectedTreeNode);
              parent.remove(selectedTreeNode);
              parent.insert(node, index);
            }
            else
            {
              // add to existing childs
              logger.debug("add new treenode");
              selectedTreeNode.add(tn);
            }
          }
        }
        else
        {
          // new root concept
          tn = new DefaultTreeNode(csev);

          TreeNode tnRoot = (DefaultTreeNode) treeModel.getRoot();
          tnRoot.add(tn);
        }

        //treeConcepts.getModel().
        // addChildElement
      }

      //treeModel = new DefaultTreeModel(tnRoot);
    }
  }

  /**
   * @return the codeSystemVersionId
   */
  public long getCodeSystemVersionId()
  {
    return codeSystemVersionId;
  }

  /**
   * @param codeSystemVersionId the codeSystemVersionId to set
   */
  public void setCodeSystemVersionId(long codeSystemVersionId)
  {
    this.codeSystemVersionId = codeSystemVersionId;
  }

  /**
   * @return the valueSetVersionId
   */
  public long getValueSetVersionId()
  {
    return valueSetVersionId;
  }

  /**
   * @param valueSetVersionId the valueSetVersionId to set
   */
  public void setValueSetVersionId(long valueSetVersionId)
  {
    this.valueSetVersionId = valueSetVersionId;
  }

  /**
   * @return the contentType
   */
  public CONTENT_TYPE getContentType()
  {
    return contentType;
  }

  /**
   * @return the treeConcepts
   */
  public Tree getTreeConcepts()
  {
    return treeConcepts;
  }

  /**
   * @return the conceptsWindow
   */
  public ContentConcepts getConceptsWindow()
  {
    return conceptsWindow;
  }

  /**
   * @param codeSystemId the codeSystemId to set
   */
  public void setCodeSystemId(long codeSystemId)
  {
    this.codeSystemId = codeSystemId;
  }

  /**
   * @param valueSetId the valueSetId to set
   */
  public void setValueSetId(long valueSetId)
  {
    this.valueSetId = valueSetId;
  }
}
