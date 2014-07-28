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

import de.fhdo.helper.SessionHelper;
import de.fhdo.models.itemrenderer.TreeitemRendererCSEVSearch;
import de.fhdo.models.TreeModelCSEV;
import de.fhdo.models.TreeNode;
import de.fhdo.terminologie.ws.search.PagingType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Tree;
import de.fhdo.terminologie.ws.search.SearchType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class PopupSearch extends GenericForwardComposer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private TreeModelCSEV treeModel;
  private Tree tree;
  private Object source;
  private SearchType searchType = new SearchType();

  private PagingType pagingType = new PagingType();

  private AnnotateDataBinder binder;
  private Textbox tbTerm, tbCode;
  private Radiogroup rgPreferred;
  private long id,
          versionId;
  private int contentMode;
  private Paging paging;
  private Window parentWindow;
  private Checkbox cbShowHierachyDetails;
  private Label lHitsPerPage;
  private Textbox tbHitsPerPage;
  private Label l_status;

  @Override
  public void doAfterCompose(Component comp) throws Exception
  {
    super.doAfterCompose(comp);
    source = arg.get("source");

    if (source instanceof CodeSystemVersion)
    {
      id = ((CodeSystemVersion) source).getCodeSystem().getId();
      versionId = ((CodeSystemVersion) source).getVersionId();
      contentMode = ContentConcepts.CONTENTMODE_CODESYSTEM;
    }
    else if (source instanceof ValueSetVersion)
    {
      id = ((ValueSetVersion) source).getValueSet().getId();
      versionId = ((ValueSetVersion) source).getVersionId();
      contentMode = ContentConcepts.CONTENTMODE_VALUESET;
      //not used by VS Search
      rgPreferred.setVisible(false);
      cbShowHierachyDetails.setVisible(false);
      lHitsPerPage.setVisible(false);
      tbHitsPerPage.setVisible(false);
    }

    parentWindow = (Window) comp;

    tree = (Tree) parentWindow.getFellow("treeSearch");
    tree.setItemRenderer(new TreeitemRendererCSEVSearch(parentWindow, this, true, false, 1, contentMode, versionId, source)); // anderer Renderer, damit der name als html element angezeigt wird
    tree.setZclass("z-dottree");

    int initialPageSize = 5;

    rgPreferred.setSelectedIndex(0);
    searchType.setCaseSensitive(true);
    searchType.setStartsWith(false);
    searchType.setTraverseConceptsToRoot(true);
    searchType.setWholeWords(false);
    pagingType.setPageIndex(0);
    pagingType.setPageSize(String.valueOf(initialPageSize));

    paging = (Paging) comp.getFellow("paging");
    paging.setPageSize(Integer.valueOf(pagingType.getPageSize()));
    paging.setActivePage(pagingType.getPageIndex());
    paging.addEventListener("onPaging", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        if (event instanceof PagingEvent)
        {
          PagingEvent pe = (PagingEvent) event;
          treeModel.loadDataByPageIndex(pe.getActivePage());
          tree.setModel(treeModel.getTreeModel());
        }
      }
    });

    binder = new AnnotateDataBinder(comp);
    binder.bindBean("searchType", searchType);
    binder.bindBean("pagingType", pagingType);
    binder.loadAll();
  }

  private void showMessage_PageSizeChanged(int oldValue, int newValue, String reason)
  {
    try
    {
      if (reason != null && reason.length() > 0)
      {
        Messagebox.show(Labels.getLabel("popupSearch.tooManyHitsPerPage") + "\n\n"
                + Labels.getLabel("popupSearch.hitsPerPageChangedTo") + " " + newValue + "\n\n"
                + Labels.getLabel("common.reason") + ": " + reason);
      }
      else
      {
        Messagebox.show(Labels.getLabel("popupSearch.tooManyHitsPerPage") + "\n\n"
                + Labels.getLabel("popupSearch.hitsPerPageChangedTo") + " " + newValue);
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(PopupSearch.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void startNewSearch()
  {
    int pageSize = 10;
    try
    {
      pageSize = Integer.valueOf(pagingType.getPageSize());
    }
    catch (NumberFormatException ex)
    {
    }

    if (searchType.isTraverseConceptsToRoot() && SessionHelper.isUserLoggedIn() == false && pageSize > 5)
    {
      showMessage_PageSizeChanged(pageSize, 5, Labels.getLabel("popupSearch.reasonHitsChanged_hierachyAndNoLogin"));
      pageSize = 5;
    }

    pagingType.setPageSize(String.valueOf(pageSize));
    binder.loadAll();

    Boolean prefered = null;

    // Details-Button disablen
    ((Button) parentWindow.getFellow("bDetails")).setDisabled(true);

    // Muss angepasst werden, wenn die Values der Radiogroup geГ¤ndert werden
    if (rgPreferred.getSelectedItem().getValue().toString().contains("true"))  // TODO Ueberpruefen, ob .toString() funktionert
      prefered = Boolean.TRUE;
    else if (rgPreferred.getSelectedItem().getValue().toString().contains("false"))  // TODO Ueberpruefen, ob .toString() funktionert
      prefered = Boolean.FALSE;

    pagingType.setPageIndex(0);
    paging.setActivePage(0);
    paging.setPageSize(Integer.valueOf(pagingType.getPageSize()));
    try
    {
      int iPageSizeTemp = Integer.valueOf(pagingType.getPageSize());
      treeModel = new TreeModelCSEV(source, searchType, tbTerm.getValue(), tbCode.getValue(), pagingType, prefered, true, null);

      // falls die Pagesize die MaxPageSize des TS Ueberschreitet, muss diese angepasst werden
      if (iPageSizeTemp != Integer.valueOf(pagingType.getPageSize()))
      {
        paging.setPageSize(Integer.valueOf(pagingType.getPageSize()));
        binder.loadAll();
        showMessage_PageSizeChanged(iPageSizeTemp, paging.getPageSize(), Labels.getLabel("popupSearch.reasonHitsChanged_TSLimit"));
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(PopupSearch.class.getName()).log(Level.SEVERE, null, ex);
    }

    paging.setTotalSize(treeModel.getTotalSize());
    if (treeModel.getTotalSize() == 0)
    {
      //Messagebox.show("Ihre Suche ergab keine Resultate!"); // TODO übersetzen
      Messagebox.show(Labels.getLabel("popupSearch.NoResults"));

    }
    /*else
     {
     l_status.setValue("");
     }*/
    tree.setModel(treeModel.getTreeModel());
  }

  public void showPopupConcept(int editMode)
  {
    // kein Element ausgewГ¤hlt, also nichts machen                        
    if (tree == null || tree.getSelectedItem() == null || (CodeSystemEntityVersion) ((TreeNode) tree.getSelectedItem().getValue()).getData() == null)
    {
      try
      {
        Messagebox.show(Labels.getLabel("popupSearch.cantShowDetailsNoConceptSelected"));
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
    else
    {
      TreeNode tnSelected = (TreeNode) tree.getSelectedItem().getValue();
      CodeSystemEntityVersion csevSelected = (CodeSystemEntityVersion) tnSelected.getData();

      Map<String, Object> data = new HashMap<String, Object>();
      data.put("EditMode", editMode);  // PopupWindow.EDITMODE_
      data.put("ContentMode", contentMode);       // 1=CS, 2=VS
      data.put("Tree", tree);
      data.put("Id", id);
      data.put("VersionId", versionId);
      data.put("TreeNode", tnSelected);
      data.put("CSEV", csevSelected);

      try
      {
        final Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupConcept.zul", null, data);
        w.doOverlapped();
      }
      catch (Exception e)
      {
        Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  public void onSelect()
  {
    ((Button) parentWindow.getFellow("bDetails")).setDisabled(false);
  }

  public void onOK$tbTerm()
  {
    startNewSearch();
  }
  
  public void onOK$windowSearch()
  {
    startNewSearch();
  }

  public void onClick$bSearch()
  {
    startNewSearch();
  }

  public void onClick$bDetails()
  {
    showPopupConcept(PopupWindow.EDITMODE_DETAILSONLY);
  }
}
