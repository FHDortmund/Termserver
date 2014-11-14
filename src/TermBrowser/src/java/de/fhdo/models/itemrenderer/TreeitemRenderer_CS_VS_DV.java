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
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.gui.main.ContentCSVSAssociationEditor;
import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.gui.main.modules.PopupCodeSystem;
import de.fhdo.gui.main.modules.PopupValueSet;
import de.fhdo.helper.SendBackHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.UtilHelper;
import de.fhdo.helper.ValidityRangeHelper;
import de.fhdo.models.TreeNode;
import java.util.Iterator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Sven Becker
 */
public class TreeitemRenderer_CS_VS_DV implements TreeitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  protected boolean draggable = false;
  protected boolean droppable = false;
  protected boolean showType = false;
  protected ContentCSVSDefault window = null;

  public TreeitemRenderer_CS_VS_DV(Window parentWindow)
  {
    window = (ContentCSVSDefault) parentWindow;
  }

  // DOMAINVALUE             
  protected void renderDVMouseEvents(final Treerow dataRow, final Treeitem ti)
  {
    dataRow.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON TREE CLICK - renderDVMouseEvents");
        window.openTreeItem(ti, true);
        window.onSelect(ti);
      }
    });
    dataRow.addEventListener(Events.ON_RIGHT_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.onSelect(ti);
      }
    });
  }

  protected void renderDVContextMenu(Menupopup contextMenu, final Treeitem ti)
  {
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    miDetails.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
//                window.popupDomainValue(PopupWindow.EDITMODE_DETAILSONLY);
      }
    });

    // Einfügen ins Menü je nach LoginStatus
    //miDetails.setParent(contextMenu);  
    createMenuitems(window, ti, contextMenu);
  }

  protected void renderDVDisplay(Object data, Treerow dataRow)
  {
    Treecell treeCell = new Treecell();
    DomainValue dv = (DomainValue) data;

    dataRow.appendChild(treeCell);
    dataRow.setAttribute("id", dv.getDomainValueId());
    dataRow.setAttribute("object", dv);
    Label lName = new Label(dv.getDomainDisplay());
    treeCell.appendChild(lName);

    // Style ändern
    String style = "";
    style += "font-weight:bold; color:green;";
    lName.setStyle(style);

    // Tooltip
//        treeCell.setTooltiptext(Labels.getLabel("xxx"));               
  }

  // CODESYSTEM    
  protected void renderCSMouseEvents(final Treerow dataRow, final Treeitem ti)
  {
    dataRow.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON TREE DOUBLE CLICK - renderCSMouseEvents");
        window.onSelect(ti);
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.DETAILSONLY, true);
      }
    });

    dataRow.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON TREE CLICK - renderCSMouseEvents");
        // TODO hier doppelte Klicke abfangen

        window.onSelect(ti);

        boolean dragNdrop = false;
        if (window instanceof ContentCSVSAssociationEditor)
        {
          dragNdrop = true;
        }
        window.loadConceptsBySelectedItem(dragNdrop, dragNdrop);
      }
    });

    dataRow.addEventListener(Events.ON_RIGHT_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON TREE RIGHT CLICK - renderCSMouseEvents");
        window.onSelect(ti);
      }
    });
  }

  protected void renderCSContextMenu(Menupopup contextMenu, final Treeitem ti)
  {
    // Menu Items
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));

    Menuitem miNewCSV = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.createNewVersion"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("common.edit"));
    Menuitem miRemoveCS = new Menuitem(Labels.getLabel("common.deleteSystem"));

    // Event Listener
    miDetails.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.DETAILSONLY, false);
      }
    });

    miNewCSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.CREATE_NEW_VERSION, true);
      }
    });

    miRemoveCS.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        Messagebox.show("Hiermit löschen sie das Code System!",
                "Code System löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                {
                  public void onEvent(Event evt) throws InterruptedException
                  {
                    if (evt.getName().equals("onYes"))
                    {
                      window.removeEntity();
                    }
                  }
                });
      }
    });

    miEdit.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.MAINTAIN, false);
      }
    });

    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        TreeNode tNode = (TreeNode) ti.getValue();
//                tNode.getDeepLinkString("","",new ArrayList<String>());
        tNode.showDeepLinkInMessagebox();
      }
    });

    // Einfügen ins Menü je nach LoginStatus
    miDetails.setParent(contextMenu);
    miDeepLink.setParent(contextMenu);
    if (SessionHelper.isUserLoggedIn())
    {
      /*TreeNode tn = (TreeNode) ti.getValue();
       Object data = tn.getData();
       boolean allowed = true;
       if (data instanceof CodeSystem)
       {
       allowed = AssignTermHelper.isUserAllowed(data);
       }
       else if (data instanceof CodeSystemVersion)
       {
       allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion) data).getCodeSystem());
       }
       else if (data instanceof ValueSet)
       {
       allowed = AssignTermHelper.isUserAllowed(data);
       }
       else if (data instanceof ValueSetVersion)
       {
       allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion) data).getValueSet());
       }

       if (allowed)*/
      {
        new Menuseparator().setParent(contextMenu);
        miEdit.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewCSV.setParent(contextMenu);

        //if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
        {
          new Menuseparator().setParent(contextMenu);
          miRemoveCS.setParent(contextMenu);
        }
      }
    }

    createMenuitems(window, ti, contextMenu);
  }

  protected void renderCSDisplay(Object data, Treerow dataRow)
  {
    Treecell treeCell = new Treecell();
    CodeSystem cs = (CodeSystem) data;

    dataRow.appendChild(treeCell);
    dataRow.setAttribute("id", cs.getId());
    dataRow.setAttribute("object", cs);

    Label lName = new Label(cs.getName());
    lName.setStyle("");
    if (cs.getCodeSystemVersions() != null
            && cs.getCodeSystemVersions().size() == 1
            && cs.getCodeSystemVersions().get(0).getStatus() != null
            && cs.getCodeSystemVersions().get(0).getStatus() != 1)
    {
      changeStyleByStatus(lName, cs.getCodeSystemVersions().get(0).getStatus());
    }

    String text = lName.getStyle();
    treeCell.appendChild(lName);

    // Tooltip
    if (cs.getCodeSystemVersions().get(0) != null && cs.getCodeSystemVersions().get(0).getSource() != null && cs.getCodeSystemVersions().get(0).getSource().isEmpty() == false)
      treeCell.setTooltiptext(Labels.getLabel("common.source") + ": " + cs.getCodeSystemVersions().get(0).getSource());
    else
      treeCell.setTooltiptext(Labels.getLabel("common.source") + ": " + Labels.getLabel("common.unknown"));

    //treeCell.setStyle("color:#000000;");
  }

  // CODESYSTEMVERSION    
  protected void renderCSVMouseEvents(Treerow dataRow, final Treeitem ti)
  {
    dataRow.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.onSelect(ti);
        boolean dragNdrop = false;
        if (window instanceof ContentCSVSAssociationEditor)
        {
          dragNdrop = true;
        }
        window.loadConceptsBySelectedItem(dragNdrop, dragNdrop);
      }
    });
    dataRow.addEventListener(Events.ON_RIGHT_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //window.OnSelectCS();
        window.onSelect(ti);
      }
    });
  }

  protected void renderCSVContextMenu(Menupopup contextMenu, final Treeitem ti)
  {
    // Menu Items
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miNewCSV = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.createNewVersion"));
    Menuitem miEditCSV = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.editVersion"));
//        Menu     export         = new Menu    (Labels.getLabel("treeitemRenderer_CS_VS_DV.exportCodeSystemVersion"));
    Menuitem export = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.exportCodeSystemVersion"));  // treeitemRenderer_CS_VS_DV.exportCodeSystemVersionClaml
//        Menuitem exportCSVCvs   = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.exportCodeSystemVersionCsv"));        
    Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
//        Menupopup mpExport      = new Menupopup();    
    Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
    Menuitem miRemoveCSV = new Menuitem(Labels.getLabel("common.deleteVersion"));

    miRemoveCSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        Messagebox.show("Hiermit löschen sie die Code System Version!",
                "Code System Version löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                {
                  public void onEvent(Event evt) throws InterruptedException
                  {
                    if (evt.getName().equals("onYes"))
                    {
                      window.removeEntity();
                    }
                  }
                });
      }
    });

    // Event Listener        
    miDetails.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.DETAILSONLY, true);
      }
    });

    miNewCSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.CREATE_NEW_VERSION, true);
      }
    });

    miEditCSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupCodeSystem(PopupCodeSystem.EDITMODES.MAINTAIN, true);
      }
    });

    export.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.openPopupExport();
      }
    });

//        exportCSVCvs.addEventListener(Events.ON_CLICK, new EventListener() {
//            public void onEvent(Event event) throws Exception {
//                window.export(2);
//            }
//        });
    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        TreeNode tNode = (TreeNode) ti.getValue();
//                tNode.getDeepLinkString("","",new ArrayList<String>());
        tNode.showDeepLinkInMessagebox();
      }
    });

    miStatus.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        // TODO window.popupDetails(PopupWindow.EDITMODE_UPDATESTATUS_VERSION);
      }
    });

//        mpExport.setParent(export);
//        exportCSVCvs.setParent(mpExport);        
    miDetails.setParent(contextMenu);
    miDeepLink.setParent(contextMenu);
    if (SessionHelper.isUserLoggedIn())
    {
      // TODO COLLAB
      /*TreeNode tn = (TreeNode) ti.getValue();
      Object data = tn.getData();
      boolean allowed = true;
      if (data instanceof CodeSystem)
      {
        allowed = AssignTermHelper.isUserAllowed(data);
      }
      else if (data instanceof CodeSystemVersion)
      {
        allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion) data).getCodeSystem());
      }
      else if (data instanceof ValueSet)
      {
        allowed = AssignTermHelper.isUserAllowed(data);
      }
      else if (data instanceof ValueSetVersion)
      {
        allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion) data).getValueSet());
      }

      if (allowed)*/
      {
        new Menuseparator().setParent(contextMenu);
        miEditCSV.setParent(contextMenu);
        miStatus.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewCSV.setParent(contextMenu);
        //if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
        {
          new Menuseparator().setParent(contextMenu);
          miRemoveCSV.setParent(contextMenu);
        }
      }
    }

    new Menuseparator().setParent(contextMenu);
    export.setParent(contextMenu);

//        export.setParent(contextMenu);
    if (SessionHelper.isCollaborationActive())
    {

      TreeNode tn = (TreeNode) ti.getValue();
      Object data = tn.getData();
      boolean allowed = true;
      if (data instanceof CodeSystem)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((CodeSystem) data).getId(), "CodeSystem");
      }
      else if (data instanceof CodeSystemVersion)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((CodeSystemVersion) data).getCodeSystem().getId(), "CodeSystem");
      }
      else if (data instanceof ValueSet)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((ValueSet) data).getId(), "ValueSet");
      }
      else if (data instanceof ValueSetVersion)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((ValueSetVersion) data).getValueSet().getId(), "ValueSet");
      }
      if (allowed)
      {//Kein IV, kein Vorschlag
        new Menuseparator().setParent(contextMenu);
        Menuitem miProposal = new Menuitem(Labels.getLabel("collab.proposeToExistingEntry"));
        miProposal.addEventListener(Events.ON_CLICK, new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            window.openPopupProposalExistingCSVS(true);
          }
        });
        miProposal.setParent(contextMenu);
      }
    }

    createMenuitems(window, ti, contextMenu);
  }

  protected void renderCSVDisplay(Object data, Treerow dataRow)
  {
    CodeSystemVersion csv = (CodeSystemVersion) data;

    Treecell tcName = new Treecell();
    dataRow.appendChild(tcName);
    dataRow.setAttribute("id", csv.getVersionId());
    dataRow.setAttribute("object", csv);
    Label lName = new Label();
    lName.setStyle("");
    lName.setStyle("font-style:italic;");
    changeStyleByStatus(lName, csv.getStatus());
    tcName.appendChild(lName);
//        tcName.setSclass("treeWrapFix");        

    String vrangeStr = ValidityRangeHelper.getValidityRangeNameById(csv.getValidityRange());
    if (vrangeStr != null && vrangeStr.length() > 0)
      vrangeStr = " (" + vrangeStr + ")";

    // Zeige den Typ an fuer die Liste aller CSV und VSV im Suchen-Reiter
    if (showType)
    {
      Treecell tcType = new Treecell();
      tcType.setTooltiptext(Labels.getLabel("common.codeSystemVersion"));
      tcType.appendChild(new Label(Labels.getLabel("common.codeSystem")));
      dataRow.appendChild(tcType);

      // Umbenennen, falls Versionsname nicht aussagekraefig genug ist
      lName.setValue(UtilHelper.getDisplayNameLong(csv) + vrangeStr);
    }
    else
    {
      lName.setValue(csv.getName() + vrangeStr);
    }

    if (csv.getSource() != null && csv.getSource().isEmpty() == false)
      tcName.setTooltiptext(Labels.getLabel("common.source") + ": " + csv.getSource());
    else
      tcName.setTooltiptext(Labels.getLabel("common.source") + ": " + Labels.getLabel("common.unknown"));
  }

  // VALUESET    
  protected void renderVSMouseEvents(final Treerow dataRow, final Treeitem ti)
  {
    dataRow.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.onSelect(ti);
        boolean dragNdrop = false;
        if (window instanceof ContentCSVSAssociationEditor)
        {
          dragNdrop = true;
        }
        window.loadConceptsBySelectedItem(dragNdrop, dragNdrop);
      }
    });
    dataRow.addEventListener(Events.ON_RIGHT_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //window.OnSelectVS();
        window.onSelect(ti);
      }
    });
  }

  protected void renderVSContextMenu(Menupopup contextMenu, final Treeitem ti)
  {
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miNewVSV = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.createNewVersion"));
    Menuitem miEdit = new Menuitem(Labels.getLabel("common.edit"));
    Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
    Menuitem miRemoveVS = new Menuitem(Labels.getLabel("common.deleteSystem"));

    miRemoveVS.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        Messagebox.show("Hiermit löschen sie das Value Set!",
                "Value Set löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                {
                  public void onEvent(Event evt) throws InterruptedException
                  {
                    if (evt.getName().equals("onYes"))
                    {
                      window.removeEntity();
                    }
                  }
                });
      }
    });

    miDetails.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupValueSet(PopupValueSet.EDITMODES.DETAILSONLY, false);
      }
    });
    miNewVSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupValueSet(PopupValueSet.EDITMODES.CREATE_NEW_VERSION, true);
      }
    });
    miEdit.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupValueSet(PopupValueSet.EDITMODES.MAINTAIN, false);
      }
    });
    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        TreeNode tn2 = (TreeNode) ti.getValue();
//                tn2.getDeepLinkString("","",new ArrayList<String>());
        tn2.showDeepLinkInMessagebox();
      }
    });
    miStatus.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        // TODO
        //window.popupValueSet(PopupValueSet.EDITMODES.MAINTAIN, false);
//        window.popupDetails(PopupWindow.EDITMODE_UPDATESTATUS);
      }
    });

    // Einfügen ins Menü je nach LoginStatus
    miDetails.setParent(contextMenu);
    miDeepLink.setParent(contextMenu);
    if (SessionHelper.isUserLoggedIn())
    {

      TreeNode tn = (TreeNode) ti.getValue();
      Object data = tn.getData();
      boolean allowed = true;
      if (data instanceof CodeSystem)
      {
        allowed = AssignTermHelper.isUserAllowed(data);
      }
      else if (data instanceof CodeSystemVersion)
      {
        allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion) data).getCodeSystem());
      }
      else if (data instanceof ValueSet)
      {
        allowed = AssignTermHelper.isUserAllowed(data);
      }
      else if (data instanceof ValueSetVersion)
      {
        allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion) data).getValueSet());
      }

      if (allowed)
      {
        new Menuseparator().setParent(contextMenu);
        miEdit.setParent(contextMenu);
        miStatus.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewVSV.setParent(contextMenu);
        if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
        {
          new Menuseparator().setParent(contextMenu);
          miRemoveVS.setParent(contextMenu);
        }
      }
    }
    createMenuitems(window, ti, contextMenu);
  }

  protected void renderVSDisplay(Object data, Treerow dataRow)
  {
    Treecell treeCell = new Treecell();
    ValueSet vs = (ValueSet) data;

    dataRow.appendChild(treeCell);
    dataRow.setAttribute("id", vs.getId());
    dataRow.setAttribute("object", vs);

    Label lName = new Label(vs.getName());
    lName.setStyle("");
    if (vs.getValueSetVersions() != null
            && vs.getValueSetVersions().size() == 1
            && vs.getValueSetVersions().get(0).getStatus() != null
            && vs.getValueSetVersions().get(0).getStatus() != 1)
    {
      changeStyleByStatus(lName, vs.getValueSetVersions().get(0).getStatus());
    }
    treeCell.appendChild(lName);

    if (vs.getDescription() != null)
      treeCell.setTooltiptext(vs.getDescription());
    treeCell.setStyle("color:#000000;");
  }

  // VALUESETVERSION    
  protected void renderVSVMouseEvents(Treerow dataRow, final Treeitem ti)
  {
    dataRow.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.onSelect(ti);
        boolean dragNdrop = false;
        if (window instanceof ContentCSVSAssociationEditor)
        {
          dragNdrop = true;
        }
        window.loadConceptsBySelectedItem(dragNdrop, dragNdrop);
      }
    });
    dataRow.addEventListener(Events.ON_RIGHT_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.onSelect(ti);
      }
    });
  }

  protected void renderVSVContextMenu(Menupopup contextMenu, final Treeitem ti)
  {
    // Menu Items
    Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
    Menuitem miNewVSV = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.createNewVersion"));
    Menuitem miEditVSV = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.editVersion"));
    Menuitem miDeepLink = new Menuitem(Labels.getLabel("treeitemRendererCSEV.miCreateDeepLink"));
    Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
    Menuitem export = new Menuitem(Labels.getLabel("treeitemRenderer_CS_VS_DV.exportCodeSystemVersion"));
    Menuitem miRemoveVSV = new Menuitem(Labels.getLabel("common.deleteVersion"));

    miRemoveVSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        Messagebox.show("Hiermit löschen sie die Value Set Version!",
                "Value Set Version löschen", Messagebox.YES | Messagebox.NO, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener()
                {
                  public void onEvent(Event evt) throws InterruptedException
                  {
                    if (evt.getName().equals("onYes"))
                    {
                      window.removeEntity();
                    }
                  }
                });
      }
    });

    // Eventlistener
    miDetails.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupValueSet(PopupValueSet.EDITMODES.DETAILSONLY, true);
      }
    });

    miNewVSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupValueSet(PopupValueSet.EDITMODES.CREATE_NEW_VERSION, true);
      }
    });

    miEditVSV.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.popupValueSet(PopupValueSet.EDITMODES.MAINTAIN, true);
      }
    });

    miDeepLink.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        TreeNode tn2 = (TreeNode) ti.getValue();
//                tn2.getDeepLinkString("","",new ArrayList<String>());
        tn2.showDeepLinkInMessagebox();
      }
    });

    miStatus.addEventListener("onClick", new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        // TODO
        // TODO    window.popupDetails(PopupWindow.EDITMODE_UPDATESTATUS_VERSION);
      }
    });

    export.addEventListener(Events.ON_CLICK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        window.openPopupExportVS();
      }
    });

    miDetails.setParent(contextMenu);
    miDeepLink.setParent(contextMenu);
    if (SessionHelper.isUserLoggedIn())
    {
      // TODO Rechte
      /*TreeNode tn = (TreeNode) ti.getValue();
      Object data = tn.getData();
      boolean allowed = true;
      if (data instanceof CodeSystem)
      {
        allowed = AssignTermHelper.isUserAllowed(data);
      }
      else if (data instanceof CodeSystemVersion)
      {
        allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion) data).getCodeSystem());
      }
      else if (data instanceof ValueSet)
      {
        allowed = AssignTermHelper.isUserAllowed(data);
      }
      else if (data instanceof ValueSetVersion)
      {
        allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion) data).getValueSet());
      }

      if (allowed)*/
      {
        new Menuseparator().setParent(contextMenu);
        miEditVSV.setParent(contextMenu);
        miStatus.setParent(contextMenu);
        new Menuseparator().setParent(contextMenu);
        miNewVSV.setParent(contextMenu);
        if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
        {
          new Menuseparator().setParent(contextMenu);
          miRemoveVSV.setParent(contextMenu);
        }
      }
    }

    new Menuseparator().setParent(contextMenu);
    export.setParent(contextMenu);

    if (SessionHelper.isCollaborationActive())
    {

      TreeNode tn = (TreeNode) ti.getValue();
      Object data = tn.getData();
      boolean allowed = true;
      if (data instanceof CodeSystem)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((CodeSystem) data).getId(), "CodeSystem");
      }
      else if (data instanceof CodeSystemVersion)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((CodeSystemVersion) data).getCodeSystem().getId(), "CodeSystem");
      }
      else if (data instanceof ValueSet)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((ValueSet) data).getId(), "ValueSet");
      }
      else if (data instanceof ValueSetVersion)
      {
        allowed = AssignTermHelper.isAnyUserAssigned(((ValueSetVersion) data).getValueSet().getId(), "ValueSet");
      }
      if (allowed)
      {//Kein IV, kein Vorschlag
        new Menuseparator().setParent(contextMenu);
        Menuitem miProposal = new Menuitem(Labels.getLabel("collab.proposeToExistingEntry"));
        miProposal.addEventListener(Events.ON_CLICK, new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            window.openPopupProposalExistingCSVS(false);
          }
        });
        miProposal.setParent(contextMenu);
      }
    }
  }

  protected void renderVSVDisplay(Object data, Treerow dataRow)
  {
    if (data == null || dataRow == null)
      return;

    Treecell treeCell = new Treecell();
    ValueSetVersion vsv = (ValueSetVersion) data;

    dataRow.appendChild(treeCell);
    dataRow.setAttribute("id", vsv.getVersionId());
    dataRow.setAttribute("object", vsv);

    Label lName = new Label();
    lName.setStyle("");
    lName.setStyle("font-style:italic;");
    changeStyleByStatus(lName, vsv.getStatus());
    treeCell.appendChild(lName);

    String vrangeStr = ValidityRangeHelper.getValidityRangeNameById(vsv.getValidityRange());
    if (vrangeStr != null && vrangeStr.length() > 0)
      vrangeStr = " (" + vrangeStr + ")";

    // Zeige den Typ an fuer die Liste aller CSV und VSV im Suchen-Reiter
    if (showType)
    {
      lName.setValue(UtilHelper.getDisplayNameLong(vsv) + vrangeStr);

      // Display Type of Object in 2nd Column
      Treecell tcType = new Treecell();
      tcType.setTooltiptext(Labels.getLabel("common.valueSetVersion"));
      tcType.appendChild(new Label(Labels.getLabel("common.valueSet")));
      dataRow.appendChild(tcType);
    }
    else
    {
      lName.setValue(vsv.getName() + vrangeStr);
    }
  }

  // RENDERER    
  @Override
  public void render(Treeitem treeItem, Object treeNode, int index) throws Exception
  {
    Treerow dataRow = new Treerow();
    Menupopup contextMenu = new Menupopup();
    TreeNode tn = (TreeNode) treeNode;
    Object data = tn.getData();

    treeItem.appendChild(dataRow);
    //dataRow.setParent(treeItem);        
    dataRow.setContext(contextMenu);
    treeItem.setValue(tn);
    contextMenu.setParent(window);  // Kontextmenü muss dem Fenster zugeordnet werden?                         

    if (data instanceof CodeSystem)
    {
      renderCSMouseEvents(dataRow, treeItem);
      renderCSContextMenu(contextMenu, treeItem);
      renderCSDisplay(data, dataRow);
    }
    else if (data instanceof CodeSystemVersion)
    {
      renderCSVMouseEvents(dataRow, treeItem);
      renderCSVContextMenu(contextMenu, treeItem);
      renderCSVDisplay(data, dataRow);
    }
    else if (data instanceof DomainValue)
    {
      renderDVMouseEvents(dataRow, treeItem);
      renderDVContextMenu(contextMenu, treeItem);
      renderDVDisplay(data, dataRow);
    }
    else if (data instanceof ValueSet)
    {
      renderVSMouseEvents(dataRow, treeItem);
      renderVSContextMenu(contextMenu, treeItem);
      renderVSDisplay(data, dataRow);
    }
    else if (data instanceof ValueSetVersion)
    {
      renderVSVMouseEvents(dataRow, treeItem);
      renderVSVContextMenu(contextMenu, treeItem);
      renderVSVDisplay(data, dataRow);
    }
    else
    {
      logger.debug("Object-Type nicht gefunden");
    }

    if (draggable)
      dataRow.setDraggable("true");
    else
      dataRow.setDraggable("false");

    if (droppable)
      dataRow.setDroppable("true");
    else
      dataRow.setDroppable("false");
  }

////////////////////////////////////////////////////////////////////////////////    
////////////////////////////////////////////////////////////////////////////////        
  public void createMenuitems(final Window window, Treeitem ti, Menupopup contextMenu)
  {
    if (SendBackHelper.getInstance().getSendBackMethodName().isEmpty())
      return;

    final Object o = ((TreeNode) ti.getValue()).getData();
    EventListener el = null;
    String sAppName = SendBackHelper.getInstance().getSendBackApplicationName(),
            sTypeName = "";

    final Integer type;

    if (o instanceof DomainValue)
    {
      type = SendBackHelper.getInstance().getSendBackTypeDV();
      if (type != null)
      {
        sTypeName = SendBackHelper.getInstance().getSendBackTypeByInteger(type);
        el = new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            String sendBackString = "";
            DomainValue dv = (DomainValue) o;
            if (type == SendBackHelper.SENDBACK_NAME)
              sendBackString = dv.getDomainDisplay();
            else if (type == SendBackHelper.SENDBACK_DESCRIPTION)
              sendBackString = dv.getDomainCode();
            SendBackHelper.getInstance().sendBack(sendBackString);
          }
        };
      }
    }
    else if (o instanceof CodeSystem)
    {
      type = SendBackHelper.getInstance().getSendBackTypeCS();
      if (type != null)
      {
        sTypeName = SendBackHelper.getInstance().getSendBackTypeByInteger(type);
        el = new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            String s = "";
            CodeSystem cs = (CodeSystem) o;
            if (type == SendBackHelper.SENDBACK_NAME)
              s = cs.getName();
            else if (type == SendBackHelper.SENDBACK_DESCRIPTION)
              s = cs.getDescription();
            SendBackHelper.getInstance().sendBack(s);
//                        sendB(s);
          }
        };
      }
    }
    else if (o instanceof CodeSystemVersion)
    {
      type = SendBackHelper.getInstance().getSendBackTypeCSV();
      if (type != null)
      {
        sTypeName = SendBackHelper.getInstance().getSendBackTypeByInteger(type);
        el = new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            String s = "";
            CodeSystemVersion csv = (CodeSystemVersion) o;
            if (type == SendBackHelper.SENDBACK_NAME)
              s = csv.getName();
            else if (type == SendBackHelper.SENDBACK_DESCRIPTION)
              s = csv.getDescription();
            SendBackHelper.getInstance().sendBack(s);
//                        sendB(s);
          }
        };
      }
    }
    else if (o instanceof ValueSet)
    {
      type = SendBackHelper.getInstance().getSendBackTypeVS();
      if (type != null)
      {
        sTypeName = SendBackHelper.getInstance().getSendBackTypeByInteger(type);
        el = new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            String s = "";
            ValueSet vs = (ValueSet) o;
            if (type == SendBackHelper.SENDBACK_NAME)
              s = vs.getName();
            else if (type == SendBackHelper.SENDBACK_DESCRIPTION)
              s = vs.getDescription();
            SendBackHelper.getInstance().sendBack(s);
//                        sendB(s);
          }
        };
      }
    }
    else if (o instanceof ValueSetVersion)
    {
    }
    else if (o instanceof CodeSystemEntityVersion)
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
            CodeSystemEntityVersion csev = (CodeSystemEntityVersion) o;
            if (type == SendBackHelper.SENDBACK_NAME)
              s = csev.getCodeSystemConcepts().get(0).getTerm();
            else if (type == SendBackHelper.SENDBACK_DESCRIPTION)
              s = csev.getCodeSystemConcepts().get(0).getDescription();
            else if (type == SendBackHelper.SENDBACK_CODE)
              s = csev.getCodeSystemConcepts().get(0).getCode();
            SendBackHelper.getInstance().sendBack(s);
          }
        };
      }
    }

    // Menueitem einf?gen
    if (el != null)
    {
      Menuitem miSendBack = new Menuitem(sTypeName + " an " + sAppName + " senden");
      miSendBack.addEventListener(Events.ON_CLICK, el);
      new Menuseparator().setParent(contextMenu);
      miSendBack.setParent(contextMenu);
    }
  }

  private void changeStyleByStatus(Label l, int status)
  {
    // Style ändern
    String style = "";
    if (l.getStyle() != null)
      style = l.getStyle();

    switch (status)
    {
      case 0: // Deaktiviert
        style += "color:grey;";
        break;
      case 2: // Gelöscht
        style += "text-decoration:line-through;";
        break;
      default:
        break;
    }
    l.setStyle(style);
  }

  public boolean isShowType()
  {
    return showType;
  }

  public void setShowType(boolean showType)
  {
    this.showType = showType;
  }

  public boolean isDraggable()
  {
    return draggable;
  }

  public void setDraggable(boolean draggable)
  {
    this.draggable = draggable;
  }

  public boolean isDroppable()
  {
    return droppable;
  }

  public void setDroppable(boolean droppable)
  {
    this.droppable = droppable;
  }

  private ValueSetVersion getVSVById(ValueSet vs, long versionId)
  {
    ValueSetVersion vsvX;

    Iterator<ValueSetVersion> itVsv = vs.getValueSetVersions().iterator();
    while (itVsv.hasNext())
    {
      vsvX = itVsv.next();
      if (vsvX.getVersionId() == versionId)
      {
        return vsvX;
      }
    }
    return null;
  }
}
//        ValueSetVersions durchnummerieren
//
//        ValueSet          vs       = vsv.getValueSet();
//        int               count    = vs.getValueSetVersions().size();
//        int               iNumber  = count;
//        ValueSetVersion   vsvTemp  = vs.getValueSetVersions().get(0);
//        
//       //  finde Current Version
//        while(vsvTemp == null){
//            vsvTemp = getVSVById(vs, vs.getCurrentVersionId());
//        }
//        
//        
//        // starte bei Current VSV und gehe über prevID nach unten
//        for(int i=0;i<count;i++){
//            if(vsv.getVersionId() == vsvTemp.getVersionId()){
//                break;
//            }
//            else{
//                iNumber--;
//                vsvTemp =  getVSVById(vs, vsvTemp.getPreviousVersionId());
//            }
//        }
