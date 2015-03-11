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
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import java.text.SimpleDateFormat;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Html;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class TreeitemRendererCSEV implements TreeitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  ConceptsTree conceptsTree;
  static EventListener noteListener = null;
  private boolean searchResults;
  private boolean dragAndDrop;
  private Menupopup contextMenu;
  private IUpdate updateDropListener = null;

  public TreeitemRendererCSEV(ConceptsTree conceptsTree, boolean search, boolean dragAndDrop, IUpdate updateDropListener)
  {
    this.conceptsTree = conceptsTree;
    this.searchResults = search;
    this.dragAndDrop = dragAndDrop;
    this.updateDropListener = updateDropListener;

    logger.debug("updateDropListener: " + updateDropListener);

    createContextMenu();
  }

  public void render(Treeitem treeItem, Object object, int i) throws Exception
  {
    TreeNode treeNode = (TreeNode) object;
    treeItem.setAttribute("treenode", treeNode);

    Object data = treeNode.getData();
    treeItem.setValue(data);

    Treerow treeRow = new Treerow();

    if (data instanceof CodeSystemEntityVersion)
    {
      CodeSystemEntityVersion csev = (CodeSystemEntityVersion) data;
      CodeSystemConcept csc = csev.getCodeSystemConcepts().get(0);

      String style = "color:#000000;";

      if (csev.getStatusVisibility() == Definitions.STATUS_VISIBILITY_INVISIBLE)
      {
        style = "color:#808080;";
      }

      boolean hasAssociation = false;
      boolean hasCrossMapping = false;
      boolean hasLink = false;
      boolean hasOntology = false;
      boolean hasHierachical = false;

      if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null)
      {
        for (CodeSystemEntityVersionAssociation cseva : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
        {
          if (hasCrossMapping == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.CROSS_MAPPING.getCode())
            hasCrossMapping = true;
          else if (hasLink == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.LINK.getCode())
            hasLink = true;
          else if (hasOntology == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode())
            hasOntology = true;
          else if (hasHierachical == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
            hasHierachical = true;
        }
      }
      if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null)
      {
        for (CodeSystemEntityVersionAssociation cseva : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2())
        {
          if (hasCrossMapping == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.CROSS_MAPPING.getCode())
            hasCrossMapping = true;
          else if (hasLink == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.LINK.getCode())
            hasLink = true;
          else if (hasOntology == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode())
            hasOntology = true;
          else if (hasHierachical == false && cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
            hasHierachical = true;
        }
      }

      if (hasCrossMapping || hasOntology || hasLink)
      {
        style = "color:#2374DB;";
        hasAssociation = true;
      }

      // designation
      Treecell cell = null;

      if (searchResults && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
          && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() > 0)
      {
        // search result
        CodeSystemEntityVersion csevTemp = csev;
        int indent = 20;
        String s = "";
        s = "<div style=\"padding-left:" + indent + "px; margin:0;\">" + "<b><font color=\"#000000\">" + getString(csc.getTerm()) + "</font></b>" + "</div>";

        while (csevTemp.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null && csevTemp.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() > 0)
        {
          if (csevTemp.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().get(0).getCodeSystemEntityVersionByCodeSystemEntityVersionId1() == null)
            break;
          csevTemp = csevTemp.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().get(0).getCodeSystemEntityVersionByCodeSystemEntityVersionId1();
          //if(s.length() > 0)
          //  s += " -> ";

          if (csevTemp.getCodeSystemConcepts() != null && csevTemp.getCodeSystemConcepts().size() > 0)
          {
            s = s + "<div style=\"padding-left:" + indent + "px; margin:0;\">" + csevTemp.getCodeSystemConcepts().get(0).getTerm()
                + " (" + csevTemp.getCodeSystemConcepts().get(0).getCode() + ")</div>";
          }
          //s += csevTemp.getCodeSystemConcepts().get(0).getTerm();

          indent += 20;
        }

        Html html = new Html();
        html.setContent(s);

        //cell = new Treecell(getString(csc.getTerm() + " (" + s + ")"));
        cell = new Treecell();
        cell.appendChild(html);
      }
      else
      {
        cell = new Treecell(getString(csc.getTerm()));
      }
      cell.setStyle(style);
      treeRow.appendChild(cell);

      // code
      cell = new Treecell(getString(csc.getCode()));
      cell.setStyle(style);
      treeRow.appendChild(cell);

      // details
      cell = new Treecell("");
      fillDetailsCell(null, cell, csev, csc, hasAssociation);
      cell.setStyle(style);
      treeRow.appendChild(cell);
      /*cell = new Treecell();

       if (csc.isIsPreferred() != null && csc.isIsPreferred().booleanValue() == false)
       {
       Image img = new Image("/rsc/img/symbols/tag_black_16x16.png");
       img.setTooltiptext(Labels.getLabel("common.notPreferredTerm"));
       cell.appendChild(img);
       }
       if (csc.getDescription() != null && csc.getDescription().length() > 0)
       {
       Image img = new Image("/rsc/img/filetypes/note.png");
       img.setTooltiptext(csc.getDescription());
       img.addEventListener(Events.ON_CLICK, noteListener);
       cell.appendChild(img);
       }
       if (csev.getStatusVisibility() != null && csev.getStatusVisibility() == Definitions.STATUS_VISIBILITY_INVISIBLE)
       {
       Image img = new Image("/rsc/img/symbols/hidden.png");
       img.setTooltiptext(Labels.getLabel("common.invisible"));
       cell.appendChild(img);
       }
       if (csev.getStatusDeactivated() != null && csev.getStatusDeactivated() == Definitions.STATUS_DEACTIVATED_DELETED)
       {
       Image img = new Image("/rsc/img/symbols/delete_12x12.png");
       img.setTooltiptext(Labels.getLabel("common.deleted"));
       cell.appendChild(img);
       }

       cell.setStyle(style);
       treeRow.appendChild(cell);*/
      // source (if value set)
      if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.VALUESET)
      {
        CodeSystemEntity cse = csev.getCodeSystemEntity();
        if (cse.getCodeSystemVersionEntityMemberships() != null)
        {
          String source = "";
          for (CodeSystemVersionEntityMembership csvem : cse.getCodeSystemVersionEntityMemberships())
          {
            if (source.length() > 0)
              source += ", ";
            source += csvem.getCodeSystemVersion().getName();
          }

          cell = new Treecell(getString(source));
          cell.setStyle(style);
          treeRow.appendChild(cell);
        }
      }

      //renderCSEVContextMenu(treeItem, treeRow, csev);
      treeRow.setContext(contextMenu);

      if (dragAndDrop)
      {
        treeRow.setDraggable("true");
        treeRow.setDroppable("true");

        treeRow.addEventListener(Events.ON_DROP, new EventListener<DropEvent>()
        {
          public void onEvent(DropEvent event) throws Exception
          {
            if (updateDropListener != null)
            {
              if (event.getDragged() instanceof Treerow)
              {
                Treerow row = (Treerow) event.getDragged();
                if (row.getParent() instanceof Treeitem)
                {
                  CodeSystemEntityVersion csev = ((Treeitem) row.getParent()).getValue();

                  Treeitem tiTarget = (Treeitem) ((Treerow) event.getTarget()).getParent();
                  CodeSystemEntityVersion csevTarget = tiTarget.getValue();

                  logger.debug("ON_DROP with csev: " + csev.getVersionId() + " to " + csevTarget.getVersionId());

                  // add mapping
                  CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
                  cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev);
                  //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csev.getVersionId());

                  cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csevTarget);
                  //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csevTarget.getVersionId());

                  updateDropListener.update(cseva);
                }
              }
            }
            else
              logger.debug("Dropped, but updateListener not set");

          }
        });
      }
    }

    treeRow.setParent(treeItem);

    appendListener(treeItem, treeNode);

    /*Menupopup contextMenu = new Menupopup();
     dataRow.setParent(treeItem);
     treeItem.setValue(treeNode);

     if (data instanceof CodeSystemEntityVersion)
     {
     renderCSEVMouseEvents(dataRow, treeItem, tn);
     renderCSEVContextMenu(contextMenu, treeItem, dataRow, data);
     renderCSEVDisplay(dataRow, data, treeItem, tn);

     if (draggable)
     dataRow.setDraggable("true");

     // Nur fuer CS, da es fuer VS nicht vorgesehen ist, hierarchien aufzubauen
     if (droppable && contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
     dataRow.setDroppable("true");
     }
     else
     {
     if (data != null)
     logger.debug("Object-Type nicht gefunden (data instanceof " + data.getClass().getCanonicalName() + ")");
     else
     logger.debug("Object-Type nicht gefunden (data instanceof " + "" + ")");
     }*/
  }

  public static void fillDetailsCell(Listcell listcell, Treecell treecell, CodeSystemEntityVersion csev, CodeSystemConcept csc, boolean hasAssociation)
  {
    //Listcell cell = new Listcell("");

    if (csc.isIsPreferred() != null && csc.isIsPreferred().booleanValue() == false)
    {
      Image img = new Image("/rsc/img/symbols/tag_black_16x16.png");
      img.setTooltiptext(Labels.getLabel("common.notPreferredTerm"));

      if (listcell != null)
        listcell.appendChild(img);
      if (treecell != null)
        treecell.appendChild(img);
    }
    if (csc.getDescription() != null && csc.getDescription().length() > 0)
    {
      if (noteListener == null)
      {
        noteListener = new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            if (event.getTarget() != null && event.getTarget() instanceof Image)
            {
              Image img = (Image) event.getTarget();
              Messagebox.show(img.getTooltiptext());
            }
          }
        };
      }

      Image img = new Image("/rsc/img/filetypes/note.png");
      img.setTooltiptext(csc.getDescription());
      img.addEventListener(Events.ON_CLICK, noteListener);
      if (listcell != null)
        listcell.appendChild(img);
      if (treecell != null)
        treecell.appendChild(img);
    }
    if (csev.getStatusVisibility() != null && csev.getStatusVisibility() == Definitions.STATUS_VISIBILITY_INVISIBLE)
    {
      Image img = new Image("/rsc/img/symbols/hidden.png");
      img.setTooltiptext(Labels.getLabel("common.invisible"));
      if (listcell != null)
        listcell.appendChild(img);
      if (treecell != null)
        treecell.appendChild(img);
    }
    if (csev.getStatusDeactivated() != null && csev.getStatusDeactivated() == Definitions.STATUS_DEACTIVATED_DELETED)
    {
      Image img = new Image("/rsc/img/symbols/delete_12x12.png");
      img.setTooltiptext(Labels.getLabel("common.deleted"));
      if (listcell != null)
        listcell.appendChild(img);
      if (treecell != null)
        treecell.appendChild(img);
    }

    if (hasAssociation)
    {
      if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null)
      {
        for (CodeSystemEntityVersionAssociation cseva : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
        {
          createImageCellFromAssociation(listcell, treecell, csev.getVersionId(), cseva);
        }
      }
      if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null)
      {
        for (CodeSystemEntityVersionAssociation cseva : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2())
        {
          createImageCellFromAssociation(listcell, treecell, csev.getVersionId(), cseva);
        }
      }
    }

  }

  private static void createImageCellFromAssociation(Listcell listcell, Treecell treecell, long cseVersionId, CodeSystemEntityVersionAssociation cseva)
  {
    String s_img = "";
    String tooltip = "";
    
    if(cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.CROSS_MAPPING.getCode())
    {
      s_img = Definitions.ASSOCIATION_KIND.CROSS_MAPPING.getImg();
      tooltip = Labels.getLabel("associationEditor.crossmapping");
    }
    else if(cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.LINK.getCode())
    {
      s_img = Definitions.ASSOCIATION_KIND.LINK.getImg();
      tooltip = Labels.getLabel("associationEditor.link");
    }
    else if(cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode())
    {
      s_img = Definitions.ASSOCIATION_KIND.ONTOLOGY.getImg();
      tooltip = Labels.getLabel("popupConcept.ontology");
    }
    else if(cseva.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
    {
      s_img = Definitions.ASSOCIATION_KIND.TAXONOMY.getImg();
      tooltip = Labels.getLabel("common.taxonomy");
    }
    else return; // unknown format
    
    Image img = new Image(s_img);
    img.setTooltiptext(tooltip);
    if (listcell != null)
      listcell.appendChild(img);
    if (treecell != null)
      treecell.appendChild(img);
  }

  private void appendListener(final Treeitem treeItem, final TreeNode treeNode)
  {
    // Eventlistener fuer + und - Symbole der TreeItems
    treeItem.addEventListener(Events.ON_OPEN, new EventListener()
    {
      @Override
      public void onEvent(Event event) throws Exception
      {
        try
        {
          if (event instanceof OpenEvent)
          {
            logger.debug("openEvent, open: " + ((OpenEvent) event).isOpen());

            if (((OpenEvent) event).isOpen())
            {
              // set treeNode as selected in model for scrolling
              treeItem.setSelected(true);
              conceptsTree.onConceptSelect(true, false);
            }
          }
        }
        catch (Exception e)
        {
        }
      }
    });

    treeItem.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener()
    {
      @Override
      public void onEvent(Event event) throws Exception
      {
        Object data = treeItem.getValue();

        if (data instanceof CodeSystemEntityVersion)
        {
          conceptsTree.openConceptDetails(((CodeSystemEntityVersion) data).getVersionId());
        }
      }
    });

    /*dataRow.addEventListener(Events.ON_DROP, new EventListener()
     {
     @Override
     public void onEvent(Event event) throws Exception
     {
     Treerow trSource = (Treerow) ((DropEvent) event).getDragged();
     Object sourceObject = (CodeSystemEntityVersion) (trSource).getAttribute("object"),
     targetObject = ((DropEvent) event).getTarget();
     Tree tree_target = null;
     CodeSystemEntityVersion csev_source = null,
     csev_target = null;*/
  }

  private void createContextMenu()
  {
    if (contextMenu != null)
      return;

    logger.debug("createContextMenu()");

    contextMenu = new Menupopup();
    contextMenu.setParent(conceptsTree.getConceptsWindow());
     //dataRow.setContext(contextMenu);

    // Contextmenu Items für Konzepte(CSEV)
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editConcept"));
    // TODO Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
    Menuitem miNewSubConcept = new Menuitem(Labels.getLabel("treeitemRendererCSEV.newSubConcept"));
    Menuitem miNewRootConcept = new Menuitem(Labels.getLabel("treeitemRendererCSEV.newRootConcept"));
     // TODO Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    //Menuitem miRemoveVS = null;// = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));

    // set icons
    miDetails.setImage("/rsc/img/list/magnifier.png");
    miEdit.setImage("/rsc/img/list/pencil.png");
    miNewSubConcept.setImage("/rsc/img/list/add.png");
    miNewRootConcept.setImage("/rsc/img/list/add.png");

    if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.VALUESET)
    {
      // TODO miRemoveVS = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));
    }
    else if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.CODESYSTEM)
    {
      // TODO miRemoveVS = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveCSEV"));
    }
    miDetails.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug(event.getTarget().getClass().getCanonicalName()
        conceptsTree.openConceptDetails(getCSEV_ID_fromSelection());
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.DETAILSONLY, null);
      }
    });

    miEdit.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        conceptsTree.maintainConcept(getCSEV_ID_fromSelection());
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.MAINTAIN, null);
      }
    });

    //    miStatus.addEventListener("onClick", new EventListener()
    //    {
    //      public void onEvent(Event event) throws Exception
    //      {
    //        // TODO ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_UPDATESTATUS);
    //      }
    //    });
    miNewSubConcept.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        conceptsTree.createConcept(PopupConcept.HIERARCHYMODE.SUB, getCSEV_ID_fromSelection());
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.CREATE, PopupConcept.HIERARCHYMODE.SUB);
      }
    });

    miNewRootConcept.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.CREATE, PopupConcept.HIERARCHYMODE.ROOT);
        conceptsTree.createConcept(PopupConcept.HIERARCHYMODE.ROOT, 0);
      }
    });

    //    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
    //    {
    //      public void onEvent(Event event) throws Exception
    //      {
    //        TreeNode tn2 = (TreeNode) ti.getValue();
    //        tn2.showDeepLinkInMessagebox();
    ////                tn2.getDeepLinkString("","",new ArrayList<String>());
    ////                tn2.showDeepLinkInMessagebox("","",new ArrayList<String>());
    //      }
    //    });
    //    miRemoveVS.addEventListener(Events.ON_CLICK, new EventListener()
    //    {
    //      public void onEvent(Event event) throws Exception
    //      {
    //        if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    //        {
    //          ((ContentConcepts) parentWindow).removeFromVS();
    //        }
    //        else if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    //        {
    //
    //          Messagebox.show("Hiermit löschen sie das Konzept und alle damit \nverknüpften ValueSet-Memberships!",
    //                  "Konzept löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
    //                  {
    //                    public void onEvent(Event evt) throws InterruptedException
    //                    {
    //                      if (evt.getName().equals("onYes"))
    //                      {
    //                        ((ContentConcepts) parentWindow).removeCSEV();
    //                      }
    //                    }
    //                  });
    //        }
    //      }
    //    });
    // Je nach Login Menu-items hinzufügen oder nicht
    miDetails.setParent(contextMenu);
    //    miDeepLink.setParent(contextMenu);

    if (SessionHelper.isUserLoggedIn())
    {
      new Menuseparator().setParent(contextMenu);

      if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.CODESYSTEM)
      {
        miEdit.setParent(contextMenu);
        //TODO miStatus.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewRootConcept.setParent(contextMenu);
        miNewSubConcept.setParent(contextMenu);
      }

      // Menü VS hinzufügen
      //      Menu mAddToVS = new Menu(Labels.getLabel("treeitemRendererCSEV.addToValueSet"));
      //      mAddToVS.setParent(contextMenu);
      //      Menupopup mpAddToVS = new Menupopup();
      //      mpAddToVS.setParent(mAddToVS);
      //
      //      for (TreeNode tnVS : (List<TreeNode>) TreeModelVS.getTreeModel(parentWindow.getDesktop()).get_root().getChildren())
      //      {
      //        final ValueSet vs = (ValueSet) tnVS.getData();
      //
      //        if (AssignTermHelper.isUserAllowed(vs))
      //        {
      //          Menupopup mpVS = new Menupopup();
      //          Menu mVS = new Menu(vs.getName());
      //          mVS.setParent(mpAddToVS);
      //          mpVS.setParent(mVS);
      //          for (final ValueSetVersion vsv : vs.getValueSetVersions())
      //          {
      //            Menuitem miVSV = new Menuitem(vsv.getName());
      //            miVSV.addEventListener("onClick", new EventListener()
      //            {
      //              public void onEvent(Event event) throws Exception
      //              {
      //                CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csev;
      //                CodeSystemEntity cse = csev.getCodeSystemEntity();
      //                ((ContentConcepts) parentWindow).addConceptToValueSet(cse.getId(), csev.getVersionId(), vs.getId(), vsv.getVersionId());
      //              }
      //            });
      //            miVSV.setParent(mpVS);
      //          }
      //        }
      //      }
      //TODO new Menuseparator().setParent(contextMenu);
      //TODO miRemoveVS.setParent(contextMenu);
      //      if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
      //      {
      //        //miRemoveVS.setParent(contextMenu);
      //        miStatus.setDisabled(false);
      //        miNewSubConcept.setDisabled(true);
      //        miNewRootConcept.setDisabled(true);
      //        miDeepLink.setDisabled(true);
      //        mAddToVS.setVisible(false);
      //      }
    }
  }

  private long getCSEV_ID_fromSelection()
  {
    logger.debug("getCSEV_ID_fromEvent");

    CodeSystemEntityVersion csev = conceptsTree.getSelection();
    if (csev != null)
    {
      logger.debug("id: " + csev.getVersionId());
      return csev.getVersionId();
    }
    return 0;

    /*if(event != null && event.getTarget() != null)
     {
     if(event.getTarget() instanceof org.zkoss.zul.Menuitem)
     {
     logger.debug("is menuitem");
     org.zkoss.zul.Menuitem mi = (org.zkoss.zul.Menuitem)event.getTarget();
     Menupopup menu = (Menupopup) mi.getParent();
     //menu.getParent()
        
        
        
     logger.debug("parent 1: " + mi.getParent().getClass().getCanonicalName());
     logger.debug("parent 2: " + mi.getParent().getParent().getClass().getCanonicalName());
     logger.debug("parent 3: " + mi.getParent().getParent().getParent().getClass().getCanonicalName());
        
     }
     }
     return 0;*/
  }

  protected void renderCSEVContextMenu(final Treeitem ti, Treerow dataRow, final CodeSystemEntityVersion csev)
  {
    logger.debug("renderCSEVContextMenu");
    Menupopup contextMenu = new Menupopup();
    contextMenu.setParent(conceptsTree.getConceptsWindow());
    dataRow.setContext(contextMenu);

    // Contextmenu Items für Konzepte(CSEV)
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editConcept"));
    // TODO Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
    Menuitem miNewSubConcept = new Menuitem(Labels.getLabel("treeitemRendererCSEV.newSubConcept"));
    Menuitem miNewRootConcept = new Menuitem(Labels.getLabel("treeitemRendererCSEV.newRootConcept"));
    // TODO Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    //Menuitem miRemoveVS = null;// = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));

    // set icons
    miDetails.setImage("/rsc/img/list/magnifier.png");
    miEdit.setImage("/rsc/img/list/pencil.png");
    miNewSubConcept.setImage("/rsc/img/list/add.png");
    miNewRootConcept.setImage("/rsc/img/list/add.png");

    if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.VALUESET)
    {
      // TODO miRemoveVS = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));
    }
    else if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.CODESYSTEM)
    {
      // TODO miRemoveVS = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveCSEV"));
    }
    miDetails.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug("onClick: " + event.getClass().getCanonicalName());
        //logger.debug("target: " + event.getTarget().getClass().getCanonicalName());

        //long id = getCSEV_ID_fromEvent(event);
//        08.12.2014 14:46:05 DEBUG: onClick: org.zkoss.zk.ui.event.MouseEvent
//08.12.2014 14:46:05 DEBUG: target: org.zkoss.zul.Menuitem
        conceptsTree.openConceptDetails(csev.getVersionId());
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.DETAILSONLY, null);
      }
    });

    miEdit.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        conceptsTree.maintainConcept(csev.getVersionId());
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.MAINTAIN, null);
      }
    });

//    miStatus.addEventListener("onClick", new EventListener()
//    {
//      public void onEvent(Event event) throws Exception
//      {
//        // TODO ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_UPDATESTATUS);
//      }
//    });
    miNewSubConcept.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        conceptsTree.createConcept(PopupConcept.HIERARCHYMODE.SUB, csev.getVersionId());
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.CREATE, PopupConcept.HIERARCHYMODE.SUB);
      }
    });

    miNewRootConcept.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //((ContentConcepts) parentWindow).showPopupConcept(PopupConcept.EDITMODES.CREATE, PopupConcept.HIERARCHYMODE.ROOT);
        conceptsTree.createConcept(PopupConcept.HIERARCHYMODE.ROOT, 0);
      }
    });

//    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
//    {
//      public void onEvent(Event event) throws Exception
//      {
//        TreeNode tn2 = (TreeNode) ti.getValue();
//        tn2.showDeepLinkInMessagebox();
////                tn2.getDeepLinkString("","",new ArrayList<String>());
////                tn2.showDeepLinkInMessagebox("","",new ArrayList<String>());
//      }
//    });
//    miRemoveVS.addEventListener(Events.ON_CLICK, new EventListener()
//    {
//      public void onEvent(Event event) throws Exception
//      {
//        if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
//        {
//          ((ContentConcepts) parentWindow).removeFromVS();
//        }
//        else if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
//        {
//
//          Messagebox.show("Hiermit löschen sie das Konzept und alle damit \nverknüpften ValueSet-Memberships!",
//                  "Konzept löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
//                  {
//                    public void onEvent(Event evt) throws InterruptedException
//                    {
//                      if (evt.getName().equals("onYes"))
//                      {
//                        ((ContentConcepts) parentWindow).removeCSEV();
//                      }
//                    }
//                  });
//        }
//      }
//    });
    // Je nach Login Menu-items hinzufügen oder nicht
    miDetails.setParent(contextMenu);
//    miDeepLink.setParent(contextMenu);

    if (SessionHelper.isUserLoggedIn())
    {
      new Menuseparator().setParent(contextMenu);

      if (conceptsTree.getContentType() == ConceptsTree.CONTENT_TYPE.CODESYSTEM)
      {
        miEdit.setParent(contextMenu);
        //TODO miStatus.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewRootConcept.setParent(contextMenu);
        miNewSubConcept.setParent(contextMenu);
      }

      // Menü VS hinzufügen
//      Menu mAddToVS = new Menu(Labels.getLabel("treeitemRendererCSEV.addToValueSet"));
//      mAddToVS.setParent(contextMenu);
//      Menupopup mpAddToVS = new Menupopup();
//      mpAddToVS.setParent(mAddToVS);
//
//      for (TreeNode tnVS : (List<TreeNode>) TreeModelVS.getTreeModel(parentWindow.getDesktop()).get_root().getChildren())
//      {
//        final ValueSet vs = (ValueSet) tnVS.getData();
//
//        if (AssignTermHelper.isUserAllowed(vs))
//        {
//          Menupopup mpVS = new Menupopup();
//          Menu mVS = new Menu(vs.getName());
//          mVS.setParent(mpAddToVS);
//          mpVS.setParent(mVS);
//          for (final ValueSetVersion vsv : vs.getValueSetVersions())
//          {
//            Menuitem miVSV = new Menuitem(vsv.getName());
//            miVSV.addEventListener("onClick", new EventListener()
//            {
//              public void onEvent(Event event) throws Exception
//              {
//                CodeSystemEntityVersion csev = (CodeSystemEntityVersion) csev;
//                CodeSystemEntity cse = csev.getCodeSystemEntity();
//                ((ContentConcepts) parentWindow).addConceptToValueSet(cse.getId(), csev.getVersionId(), vs.getId(), vsv.getVersionId());
//              }
//            });
//            miVSV.setParent(mpVS);
//          }
//        }
//      }
      //TODO new Menuseparator().setParent(contextMenu);
      //TODO miRemoveVS.setParent(contextMenu);
//      if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
//      {
//        //miRemoveVS.setParent(contextMenu);
//        miStatus.setDisabled(false);
//        miNewSubConcept.setDisabled(true);
//        miNewRootConcept.setDisabled(true);
//        miDeepLink.setDisabled(true);
//        mAddToVS.setVisible(false);
//      }
    }

//    if (SessionHelper.isCollaborationActive())
//    {
//      // Kollaborations-Menüitems hinzufügen
//      if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
//      {
//        if (AssignTermHelper.isAnyUserAssigned(((CodeSystemVersion) source).getCodeSystem().getId(), "CodeSystem"))
//        {//Kein IV, kein Vorschlag
//          // Nur bei Concepts, keine Valuesets
//          new Menuseparator().setParent(contextMenu);
//
//          Menuitem miNewSubEntry = new Menuitem(Labels.getLabel("collab.proposeNewSubConcept"));
//          miNewSubEntry.addEventListener("onClick", new EventListener()
//          {
//            public void onEvent(Event event) throws Exception
//            {
//              ((ContentConcepts) parentWindow).proposeNewSubConcept((CodeSystemEntityVersion) csev);
//            }
//          });
//
//          miNewSubEntry.setParent(contextMenu);
//
//          new Menuseparator().setParent(contextMenu);
//
//          Menuitem miPropExistingConcept = new Menuitem(Labels.getLabel("collab.proposeToExistingEntry"));
//          miPropExistingConcept.addEventListener("onClick", new EventListener()
//          {
//            public void onEvent(Event event) throws Exception
//            {
//              ((ContentConcepts) parentWindow).proposeForExistingConceptOrMembership();
//            }
//          });
//
//          miPropExistingConcept.setParent(contextMenu);
//        }
//      }
//
//      if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
//      {
//        if (AssignTermHelper.isAnyUserAssigned(((ValueSetVersion) source).getValueSet().getId(), "ValueSet"))
//        {//Kein IV, kein Vorschlag
//          new Menuseparator().setParent(contextMenu);
//
//          Menuitem miPropExistingConceptMembership = new Menuitem(Labels.getLabel("collab.proposeToExistingEntry"));
//          miPropExistingConceptMembership.addEventListener("onClick", new EventListener()
//          {
//            public void onEvent(Event event) throws Exception
//            {
//              ((ContentConcepts) parentWindow).proposeForExistingConceptOrMembership();
//            }
//          });
//
//          miPropExistingConceptMembership.setParent(contextMenu);
//        }
//      }
//    }
//    sendbackMenuitem(parentWindow, ti, contextMenu);
  }

  private String getString(Object o)
  {
    if (o == null)
      return "";

    if (o instanceof java.util.Date)
    {
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
      return sdf.format(o);
    }

    return o.toString();
  }

}
