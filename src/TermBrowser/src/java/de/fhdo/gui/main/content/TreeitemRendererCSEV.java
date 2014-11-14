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

import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.helper.SessionHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class TreeitemRendererCSEV implements TreeitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  ConceptsTree conceptsTree;

  public TreeitemRendererCSEV(ConceptsTree conceptsTree)
  {
    this.conceptsTree = conceptsTree;
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

      // append data to row
      Treecell cell = new Treecell(getString(csc.getTerm()));
      cell.setStyle(style);
      treeRow.appendChild(cell);

      cell = new Treecell(getString(csc.getCode()));
      cell.setStyle(style);
      treeRow.appendChild(cell);

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

      renderCSEVContextMenu(treeItem, treeRow, csev);
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

              /*ArrayList<de.fhdo.models.TreeNode> list = new ArrayList<de.fhdo.models.TreeNode>();
               list.add(treeNode);
               treeNode.getModel().setSelection(list);

               ((de.fhdo.gui.main.modules.ContentConcepts) parentWindow).onSelect();
               ((de.fhdo.gui.main.modules.ContentConcepts) parentWindow).openNode(treeNode);*/
            }
            //else
//              ((de.fhdo.gui.main.modules.ContentConcepts) parentWindow).closeNode(treeNode);
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

  }

  protected void renderCSEVContextMenu(final Treeitem ti, Treerow dataRow, final CodeSystemEntityVersion csev)
  {
    logger.debug("renderCSEVContextMenu");
    Menupopup contextMenu = new Menupopup();
    contextMenu.setParent(conceptsTree.getConceptsWindow());
    //contextMenu.setParent(conceptsTree);
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
