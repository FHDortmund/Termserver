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
package de.fhdo.models.itemrenderer;

import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.gui.main.modules.PopupWindow;
import de.fhdo.helper.SendBackHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.UtilHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.TreeModelVS;
import de.fhdo.models.TreeNode;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.OverallErrorCategory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class TreeitemRendererCSEV implements TreeitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  protected boolean draggable = false;
  protected boolean droppable = false;
  protected int rootBond = -1;
  protected ContentConcepts parentWindow = null;
  protected int contentMode = 0;
  protected int associationMode = 0;
//  protected TreeNode tn;
  protected Object source;

  // Constructors
  public TreeitemRendererCSEV(ContentConcepts parentWindow, boolean Draggable, boolean Droppable, int associationMode, Object source)
  {
    draggable = Draggable;
    droppable = Droppable;
    this.parentWindow = parentWindow;
    this.associationMode = associationMode;
    this.source = source;
  }

  public TreeitemRendererCSEV(ContentConcepts parentWindow, boolean Draggable, boolean droppable, int associationMode, int bond, Object source)
  {
    this(parentWindow, Draggable, droppable, associationMode, source);
    rootBond = bond;
  }

  // CSEV
  protected void renderCSEVMouseEvents(final Treerow dataRow, final Treeitem treeItem, final TreeNode treeNode)
  {
    // DropEvents falls droppable
    if (droppable)
    {
      if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
      {
        dataRow.addEventListener(Events.ON_DROP, new EventListener()
        {
          @Override
          public void onEvent(Event event) throws Exception
          {
            Treerow trSource = (Treerow) ((DropEvent) event).getDragged();
            Object sourceObject = (CodeSystemEntityVersion) (trSource).getAttribute("object"),
                    targetObject = ((DropEvent) event).getTarget();
            Tree tree_target = null;
            CodeSystemEntityVersion csev_source = null,
                    csev_target = null;

            switch (parentWindow.getAssociationMode())
            {
              case 1: // Crossmapping ////////////////////////////                                                                                                                                                                                                                             
                // Target bestimmen
                if (targetObject instanceof Treerow)
                {
                  csev_target = (CodeSystemEntityVersion) treeNode.getData();
                }

                // Source
                if (sourceObject instanceof CodeSystemEntityVersion)
                {
                  csev_source = (CodeSystemEntityVersion) sourceObject;
                }

                if (csev_source != null && csev_target != null)
                {
                  // pruefe ob source == target
                  if (csev_source.getCodeSystemEntity().getId().compareTo(csev_target.getCodeSystemEntity().getId()) == 0)
                  {
                    Messagebox.show(Labels.getLabel("treeitemRendererCSEV.errorSourceEqualsTarget"));
                    return;
                  }

                  // Request Parameter
                  CreateConceptAssociationRequestType parameter = new CreateConceptAssociationRequestType();

                  // Login                            
                  parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

                  // CSEVA
                  CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
                  cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev_target);
                  cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev_source);
                  cseva.setLeftId(csev_target.getVersionId());
                  cseva.setAssociationKind(3);    // 3 = Crossmapping

                  // AssociationType
                  AssociationType at = new AssociationType();
                  at.setCodeSystemEntityVersionId(29844L);
                  cseva.setAssociationType(at);
                  parameter.setCodeSystemEntityVersionAssociation(cseva);

                  // Response with loop deletion for SOAP Message
                  long cseId_source = csev_source.getCodeSystemEntity().getId(),
                          cseId_target = csev_target.getCodeSystemEntity().getId(),
                          cseCurrentVersionId_source = csev_source.getCodeSystemEntity().getCurrentVersionId(),
                          cseCurrentVersionId_target = csev_target.getCodeSystemEntity().getCurrentVersionId();
                  csev_target.setCodeSystemEntity(null);
                  csev_source.setCodeSystemEntity(null);

                  de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse.Return response = WebServiceHelper.createConceptAssociation(parameter);

                  csev_target.setCodeSystemEntity(new CodeSystemEntity());
                  csev_source.setCodeSystemEntity(new CodeSystemEntity());
                  csev_source.getCodeSystemEntity().setId(cseId_source);
                  csev_target.getCodeSystemEntity().setId(cseId_target);
                  csev_source.getCodeSystemEntity().setCurrentVersionId(cseCurrentVersionId_source);
                  csev_target.getCodeSystemEntity().setCurrentVersionId(cseCurrentVersionId_target);

                  if (response.getReturnInfos().getOverallErrorCategory() == OverallErrorCategory.INFO)
                  {
                    ((ContentConcepts) parentWindow).updateModel(true);
                  }
                  else
                  {
                    Messagebox.show(Labels.getLabel("common.error") + "\n\n" + Labels.getLabel("treeitemRendererCSEV.errorCreateAccociationFailed") + "\n\n" + response.getReturnInfos().getMessage());
                  }
                }
                break;

              case 2: // LINK ////////////////////////////////////////
                trSource = (Treerow) ((DropEvent) event).getDragged();
                targetObject = ((DropEvent) event).getTarget();
                sourceObject = (CodeSystemEntityVersion) (trSource).getAttribute("object");

                // Target bestimmen
                if (targetObject instanceof Treerow)
                {
                  csev_target = (CodeSystemEntityVersion) treeNode.getData();
                }

                // Source
                if (sourceObject instanceof CodeSystemEntityVersion)
                {
                  csev_source = (CodeSystemEntityVersion) sourceObject;
                }

                if (csev_source != null && csev_target != null)
                {
                  // pruefe ob source == target
                  if (csev_source.getCodeSystemEntity().getId().compareTo(csev_target.getCodeSystemEntity().getId()) == 0)
                  {
                    Messagebox.show(Labels.getLabel("treeitemRendererCSEV.errorSourceEqualsTarget"));
                    return;
                  }

                  // Request Parameter
                  CreateConceptAssociationRequestType parameter = new CreateConceptAssociationRequestType();

                  // Login                            
                  parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

                  // CSEVA
                  CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
                  cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev_target);
                  cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev_source);
                  cseva.setLeftId(csev_target.getVersionId());
                  cseva.setAssociationKind(4);    // 4 fuer Einhaengen von Konzepten in andere Vokabulare

                  // AssociationType
                  AssociationType at = new AssociationType();
                  at.setCodeSystemEntityVersionId(294908L);
                  cseva.setAssociationType(at);
                  parameter.setCodeSystemEntityVersionAssociation(cseva);

                  // Response with loop deletion for SOAP Message
                  long cseId_source = csev_source.getCodeSystemEntity().getId(),
                          cseId_target = csev_target.getCodeSystemEntity().getId(),
                          cseCurrentVersionId_source = csev_source.getCodeSystemEntity().getCurrentVersionId(),
                          cseCurrentVersionId_target = csev_target.getCodeSystemEntity().getCurrentVersionId();
                  csev_target.setCodeSystemEntity(null);
                  csev_source.setCodeSystemEntity(null);

                  de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse.Return response = WebServiceHelper.createConceptAssociation(parameter);

                  csev_target.setCodeSystemEntity(new CodeSystemEntity());
                  csev_source.setCodeSystemEntity(new CodeSystemEntity());
                  csev_source.getCodeSystemEntity().setId(cseId_source);
                  csev_target.getCodeSystemEntity().setId(cseId_target);
                  csev_source.getCodeSystemEntity().setCurrentVersionId(cseCurrentVersionId_source);
                  csev_target.getCodeSystemEntity().setCurrentVersionId(cseCurrentVersionId_target);

                  if (response.getReturnInfos().getOverallErrorCategory() == OverallErrorCategory.INFO)
                  {
                    // TreeNode erstellen und danach update, damit das neue Konzept auch angezeigt wird                                        
                    TreeNode tnNew = new TreeNode(csev_source);
                    tnNew.setLinkedConcept(true);
                    treeNode.getChildren().add(tnNew);

                    // TODO: Wenn der neue TN Kinder hat, müsste ein + beim öffnen des neu Assoziierten angezeigt werden
                    ((ContentConcepts) parentWindow).updateModel(true);

                  }
                  else
                  {
                    Messagebox.show(Labels.getLabel("common.error") + "\n\n"
                            + Labels.getLabel("treeitemRendererCSEV.errorCreateAccociationFailed") + "\n\n"
                            + response.getReturnInfos().getMessage());
                  }
                  break;
                }
            }
          }
        });
      }
      else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
      {
        // kein Drop auf CSEV, nur auf den Tree
      }
    }

    // Doppelklick nicht unterstützt von ZK? Es wird auch immer der normale Klick ausgeführt
    dataRow.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener()
    {
      @Override
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).onSelect();
        ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_DETAILSONLY);
      }
    });

    dataRow.addEventListener(Events.ON_CLICK, new EventListener()
    {
      @Override
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).onSelect();
        ((ContentConcepts) parentWindow).openNode(treeNode);
      }
    });

    dataRow.addEventListener(Events.ON_RIGHT_CLICK, new EventListener()
    {
      @Override
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).onSelect();
      }
    });

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
            if (((OpenEvent) event).isOpen())
            {
              // set treeNode as selected in model for scrolling
              ArrayList<TreeNode> list = new ArrayList<TreeNode>();
              list.add(treeNode);
              treeNode.getModel().setSelection(list);

              ((ContentConcepts) parentWindow).onSelect();
              ((ContentConcepts) parentWindow).openNode(treeNode);
            }
            else
              ((ContentConcepts) parentWindow).closeNode(treeNode);
          }
        }
        catch (Exception e)
        {
        }
      }
    });
  }

  protected void renderCSEVDisplay(Treerow dataRow, Object data, Treeitem treeItem, TreeNode treeNode)
  {
    Treecell tcName = new Treecell(),
            tcCode = new Treecell();
    CodeSystemEntityVersion csev = (CodeSystemEntityVersion) data;
    ConceptValueSetMembership cvsm = null;
    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      ValueSetVersion vsv = (ValueSetVersion) ((ContentConcepts) parentWindow).getSource();

      for (ConceptValueSetMembership cvsm2 : csev.getConceptValueSetMemberships())
      {
        Long idCvsm2Vsv = cvsm2.getId().getValuesetVersionId();
        Long idVsv = vsv.getVersionId();
        if (idCvsm2Vsv.equals(idVsv))
        {
          cvsm = cvsm2;
          break;
        }
      }
    }

    dataRow.setAttribute("id", csev.getVersionId());
    dataRow.setAttribute("object", csev);
    dataRow.appendChild(tcName);
    dataRow.appendChild(tcCode);

    if (csev.getCodeSystemConcepts().size() > 0)
    {
      CodeSystemConcept csc = csev.getCodeSystemConcepts().get(0);
      Label lCode = new Label();

      if (contentMode == ContentConcepts.CONTENTMODE_VALUESET && cvsm != null)
      {
        if ((cvsm.getStatus()) == 1 && cvsm.isIsStructureEntry())
        {
          lCode.setValue("");
        }
        else
        {
          lCode.setValue(csc.getCode());
        }
      }
      else
      {
        lCode.setValue(csc.getCode());
      }

      Label lName = new Label(csc.getTerm());
      tcName.appendChild(lName);
      tcCode.appendChild(lCode);

      // Fuer VS noch das CS anzeigen    
      if (parentWindow.getContentMode() == ContentConcepts.CONTENTMODE_VALUESET)
      {
        try
        { // TODO: Geht bestimmt noch eleganter
          if (treeItem.getTree().getFellow("tcolSource") != null)
          {
            Treecell tcSource = new Treecell();
            String sSource = "";
            if (treeNode.getSourceCSV() != null)
            {
              sSource = UtilHelper.getDisplayNameLong(treeNode.getSourceCSV()); //treeNode.getSourceCSV().getName();
              if (treeNode.getSourceCSV().getOid() != null && treeNode.getSourceCSV().getOid().length() > 0)
                sSource += " (" + treeNode.getSourceCSV().getOid() + ")";
            }
            Label lSource = new Label(sSource);
            tcSource.appendChild(lSource);
            dataRow.setAttribute("source", null);
            dataRow.appendChild(tcSource);
          }
        }
        catch (ComponentNotFoundException e)
        {
          e.printStackTrace();
        }
      }

      // uebersetzen falls gewuenscht
      if (true)
      { // TODO: UserAccount.getShowPrefferedLanguage()
        Iterator<CodeSystemConceptTranslation> it = csc.getCodeSystemConceptTranslations().iterator();
        while (it.hasNext())
        {
          CodeSystemConceptTranslation csct = it.next();
          //if (csct.getLanguageId() == 33)
          if (csct.getLanguageCd() != null && csct.getLanguageCd().equalsIgnoreCase("de-DE")) // TODO
          { // User.getLanguageId();
            lName.setValue(csct.getTerm());
            break;
          }
        }
      }
      tcName.setStyle("color:#000000;");
      tcCode.setStyle("color:#000000;");

      if (rootBond > -1 && (rootBond == 0 || rootBond > treeItem.getLevel()))
      {
        lName.setStyle(lName.getStyle() + ";font-weight:bold");
      }

      if (parentWindow.getContentMode() == ContentConcepts.CONTENTMODE_CODESYSTEM)
      {

        if (csev.getStatusVisibility()!= 1) // TODO Status
        {
          if (csev.getStatusVisibility() == 0)  // TODO Status
          {
            // inaktiv
            lName.setStyle(lName.getStyle() + ";color:#A0A0A0");
            lCode.setStyle(lCode.getStyle() + ";color:#A0A0A0");
            lName.setTooltiptext("Status: inaktiv");
            lCode.setTooltiptext(lName.getTooltiptext());
          }
          else if (csev.getStatusVisibility() == 2)  // TODO Status
          {
            // geloescht
            lName.setStyle(lName.getStyle() + ";color:#000000;text-decoration:line-through;");
            lCode.setStyle(lCode.getStyle() + ";color:#000000;text-decoration:line-through;");
            lName.setTooltiptext("Status: gelöscht");
            lCode.setTooltiptext(lName.getTooltiptext());
          }
        }
        else
        {
          if (treeNode.isCrossMapping())
          {
            lName.setStyle(lName.getStyle() + ";color:blue");
          }
        }
      }
      else
      {

        if (cvsm != null && cvsm.getStatus() != 1)
        {
          if (cvsm.getStatus() == 0)
          {
            // inaktiv
            lName.setStyle(lName.getStyle() + ";color:#A0A0A0");
            lCode.setStyle(lCode.getStyle() + ";color:#A0A0A0");
            lName.setTooltiptext("Status: inaktiv");
            lCode.setTooltiptext(lName.getTooltiptext());
          }
          else if (cvsm.getStatus() == 2)
          {
            // gelöscht
            lName.setStyle(lName.getStyle() + ";color:#000000;text-decoration:line-through;");
            lCode.setStyle(lCode.getStyle() + ";color:#000000;text-decoration:line-through;");
            lName.setTooltiptext("Status: gelöscht");
            lCode.setTooltiptext(lName.getTooltiptext());
          }
        }
        else
        {
          if (treeNode.isCrossMapping())
          {
            lName.setStyle(lName.getStyle() + ";color:blue");
          }
        }
      }

      if (treeNode.isLinkedConcept())
      {
        lName.setValue(lName.getValue() + "[LC]");
      }
      if (treeNode.hasLinkedConcepts())
      {
        lName.setValue(lName.getValue() + "[HL]");
      }

      lCode.setMultiline(false);
      lName.setMultiline(false);

    }
    else if (csev.getAssociationTypes().size() > 0)
    {
      AssociationType at = csev.getAssociationTypes().get(0);
      tcCode.setLabel(at.getForwardName() + "/" + at.getReverseName());
    }
  }

  protected void renderCSEVContextMenu(Menupopup contextMenu, final Treeitem ti, Treerow dataRow, final Object data)
  {
    contextMenu.setParent(parentWindow);
    dataRow.setContext(contextMenu);

    // Contextmenu Items für Konzepte(CSEV)
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editConcept"));
    Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
    Menuitem miNewC2 = new Menuitem(Labels.getLabel("treeitemRendererCSEV.newSubConcept"));
    Menuitem miNewC3 = new Menuitem(Labels.getLabel("treeitemRendererCSEV.newRootConcept"));
    Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    Menuitem miRemoveVS = null;// = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));
    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      miRemoveVS = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveFromVS"));
    }
    else if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      miRemoveVS = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miRemoveCSEV"));
    }
    miDetails.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_DETAILSONLY);
      }
    });

    miEdit.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_MAINTAIN_VERSION_EDIT);
      }
    });

    miStatus.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_UPDATESTATUS);
      }
    });

    miNewC2.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_CREATE, 2);
      }
    });

    miNewC3.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        ((ContentConcepts) parentWindow).showPopupConcept(PopupWindow.EDITMODE_CREATE, 3);
      }
    });

    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        TreeNode tn2 = (TreeNode) ti.getValue();
        tn2.showDeepLinkInMessagebox();
//                tn2.getDeepLinkString("","",new ArrayList<String>());
//                tn2.showDeepLinkInMessagebox("","",new ArrayList<String>());
      }
    });

    miRemoveVS.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
        {
          ((ContentConcepts) parentWindow).removeFromVS();
        }
        else if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
        {

          Messagebox.show("Hiermit löschen sie das Konzept und alle damit \nverknüpften ValueSet-Memberships!",
                  "Konzept löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                  {
                    public void onEvent(Event evt) throws InterruptedException
                    {
                      if (evt.getName().equals("onYes"))
                      {
                        ((ContentConcepts) parentWindow).removeCSEV();
                      }
                    }
                  });
        }
      }
    });

    // Je nach Login Menu-items hinzufügen oder nicht
    miDetails.setParent(contextMenu);
    miDeepLink.setParent(contextMenu);

    if (SessionHelper.isUserLoggedIn())
    {
      new Menuseparator().setParent(contextMenu);
      // Menü VS hinzufügen
      Menu mAddToVS = new Menu(Labels.getLabel("treeitemRendererCSEV.addToValueSet"));
      mAddToVS.setParent(contextMenu);
      Menupopup mpAddToVS = new Menupopup();
      mpAddToVS.setParent(mAddToVS);

      for (TreeNode tnVS : (List<TreeNode>) TreeModelVS.getTreeModel(parentWindow.getDesktop()).get_root().getChildren())
      {
        final ValueSet vs = (ValueSet) tnVS.getData();

        if (AssignTermHelper.isUserAllowed(vs))
        {
          Menupopup mpVS = new Menupopup();
          Menu mVS = new Menu(vs.getName());
          mVS.setParent(mpAddToVS);
          mpVS.setParent(mVS);
          for (final ValueSetVersion vsv : vs.getValueSetVersions())
          {
            Menuitem miVSV = new Menuitem(vsv.getName());
            miVSV.addEventListener("onClick", new EventListener()
            {
              public void onEvent(Event event) throws Exception
              {
                CodeSystemEntityVersion csev = (CodeSystemEntityVersion) data;
                CodeSystemEntity cse = csev.getCodeSystemEntity();
                ((ContentConcepts) parentWindow).addConceptToValueSet(cse.getId(), csev.getVersionId(), vs.getId(), vsv.getVersionId());
              }
            });
            miVSV.setParent(mpVS);
          }
        }
      }

      boolean allowed = true;
      if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
      {
        allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion) source).getCodeSystem());
      }
      else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
      {

        mAddToVS.setVisible(false);
        allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion) source).getValueSet());
      }

      if (allowed)
      {

        new Menuseparator().setParent(contextMenu);
        miEdit.setParent(contextMenu);
        miStatus.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewC2.setParent(contextMenu);
        miNewC3.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miRemoveVS.setParent(contextMenu);

      //            Iterator<TreeNode> itVS = TreeModelVS.getTreeModel(parentWindow.getDesktop()).get_root().getChildren().iterator();
        //            while(itVS.hasNext()){
        //                final ValueSet  vs      = (ValueSet)itVS.next().getData();
        //                Menu            mVS     = new Menu(vs.getName());
        //                Menupopup       mpVS    = new Menupopup();
        //                mVS.setParent(mpAddToVS);                
        //                mpVS.setParent(mVS);
        //
        //                // VS Versionen einfuegen
        //                Iterator<ValueSetVersion> itVSV = vs.getValueSetVersions().iterator();
        //                while(itVSV.hasNext()){
        //                    final ValueSetVersion vsv = itVSV.next();
        //                    Menuitem miVSV = new Menuitem(vsv.getInsertTimestamp().toString());
        //                    miVSV.addEventListener("onClick", new EventListener(){
        //                        public void onEvent(Event event) throws Exception {                            
        //                            CodeSystemEntityVersion csev = (CodeSystemEntityVersion) data;
        //                            CodeSystemEntity cse = csev.getCodeSystemEntity();                            
        //                            ((ContentConcepts)parentWindow).addConceptToValueSet(cse.getId(),csev.getVersionId(), vs.getId(), vsv.getVersionId());
        //                        }            
        //                    });
        //                    miVSV.setParent(mpVS);
        //                }
        //            }    
        if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
        {
          //miRemoveVS.setParent(contextMenu);
          miStatus.setDisabled(false);
          miNewC2.setDisabled(true);
          miNewC3.setDisabled(true);
          miDeepLink.setDisabled(true);
          mAddToVS.setVisible(false);
        }
      }
    }

    if (SessionHelper.isCollaborationActive())
    {
      // Kollaborations-Menüitems hinzufügen
      if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
      {
        if (AssignTermHelper.isAnyUserAssigned(((CodeSystemVersion) source).getCodeSystem().getId(), "CodeSystem"))
        {//Kein IV, kein Vorschlag
          // Nur bei Concepts, keine Valuesets
          new Menuseparator().setParent(contextMenu);

          Menuitem miNewSubEntry = new Menuitem(Labels.getLabel("collab.proposeNewSubConcept"));
          miNewSubEntry.addEventListener("onClick", new EventListener()
          {
            public void onEvent(Event event) throws Exception
            {
              ((ContentConcepts) parentWindow).proposeNewSubConcept((CodeSystemEntityVersion) data);
            }
          });

          miNewSubEntry.setParent(contextMenu);

          new Menuseparator().setParent(contextMenu);

          Menuitem miPropExistingConcept = new Menuitem(Labels.getLabel("collab.proposeToExistingEntry"));
          miPropExistingConcept.addEventListener("onClick", new EventListener()
          {
            public void onEvent(Event event) throws Exception
            {
              ((ContentConcepts) parentWindow).proposeForExistingConceptOrMembership();
            }
          });

          miPropExistingConcept.setParent(contextMenu);
        }
      }

      if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
      {
        if (AssignTermHelper.isAnyUserAssigned(((ValueSetVersion) source).getValueSet().getId(), "ValueSet"))
        {//Kein IV, kein Vorschlag
          new Menuseparator().setParent(contextMenu);

          Menuitem miPropExistingConceptMembership = new Menuitem(Labels.getLabel("collab.proposeToExistingEntry"));
          miPropExistingConceptMembership.addEventListener("onClick", new EventListener()
          {
            public void onEvent(Event event) throws Exception
            {
              ((ContentConcepts) parentWindow).proposeForExistingConceptOrMembership();
            }
          });

          miPropExistingConceptMembership.setParent(contextMenu);
        }
      }
    }

    sendbackMenuitem(parentWindow, ti, contextMenu);
  }

  // Renderer
  @Override
  public void render(Treeitem ti, Object treeNode, int index) throws Exception
  {
    TreeNode tn = (TreeNode) treeNode;
    Treerow dataRow = new Treerow();
    Treeitem treeItem = ti;
    Object data = tn.getData();
    Menupopup contextMenu = new Menupopup();
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
    }
  }

  // DIV    
  protected void sendbackMenuitem(final Window window, Treeitem ti, Menupopup contextMenu)
  {
    if (SendBackHelper.getInstance().isActive() == false)
      return;

    final Integer type;
    final Object o = ((TreeNode) ti.getValue()).getData();
    EventListener el = null;
    String sAppName = SendBackHelper.getInstance().getSendBackApplicationName(),
            sTypeName = "";

    if (o instanceof CodeSystemEntityVersion)
    {
      type = SendBackHelper.getInstance().getSendBackTypeCSEV();
      if (type != null)
      {
        sTypeName = SendBackHelper.getInstance().getSendBackTypeByInteger(type);
        el = new EventListener()
        {
          @Override
          public void onEvent(Event event) throws Exception
          {
            String s = "";
            String separator = ";;";
            CodeSystemEntityVersion csev = (CodeSystemEntityVersion) o;

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
            SendBackHelper.getInstance().sendBack(s);
          }
        };
      }
    }

    // Menueitem einfuegen
    if (el != null)
    {
      Menuitem miSendBack = new Menuitem(sTypeName + " an " + sAppName + " senden");
      miSendBack.addEventListener(Events.ON_CLICK, el);
      new Menuseparator().setParent(contextMenu);
      miSendBack.setParent(contextMenu);
    }
  }

  public int getContentMode()
  {
    return contentMode;
  }

  public void setContentMode(int contentMode)
  {
    this.contentMode = contentMode;
  }
}
