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
import de.fhdo.gui.main.modules.ContentConcepts_old;
import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.gui.main.modules.PopupSearch;
import de.fhdo.gui.main.modules.PopupWindow;
import de.fhdo.helper.SessionHelper;
import de.fhdo.models.TreeNode;
import java.util.ArrayList;
import java.util.Iterator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Sven Becker
 */
public class TreeitemRendererCSEVSearch extends TreeitemRendererCSEV{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private PopupSearch popupSearch;
    private Window      parentWindow;   // Hide Field ist ok, bzw notwendig
    private int contMode;
    private Object source;
    private long vsvVersionId;
    
    public TreeitemRendererCSEVSearch(Window pW, PopupSearch parentWindow, boolean Draggable, boolean droppable, int associationMode, int contMode, long vsvVersionId, Object source) {        
        super(null, Draggable, droppable, associationMode,source);
        this.parentWindow = pW;
        this.popupSearch  = parentWindow;
        this.contMode = contMode;
        this.vsvVersionId = vsvVersionId;
        this.source = source;
        
    }

    public TreeitemRendererCSEVSearch(Window pW,PopupSearch parentWindow, boolean Draggable, boolean droppable, int associationMode, int bond, int contMode, long vsvVersionId, Object source) {
        super(null, Draggable, droppable, associationMode, bond, source);
        this.popupSearch  = parentWindow;
        this.parentWindow = pW;
        this.contMode = contMode;
        this.vsvVersionId = vsvVersionId;
        this.source = source;
    }
    
    @Override
    protected void renderCSEVDisplay(Treerow dataRow, Object data, Treeitem treeItem, TreeNode treeNode){               
        Treecell tcName = new Treecell(),
                 tcCode = new Treecell();
        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) data;
        ConceptValueSetMembership cvsm = null;
        if(contMode == ContentConcepts_old.CONTENTMODE_VALUESET){
            
            for(ConceptValueSetMembership cvsm2:csev.getConceptValueSetMemberships()){
                if(cvsm2.getId().getValuesetVersionId() == vsvVersionId){
                    cvsm = cvsm2;
                    break;
                }   
            }
        }
    
        dataRow.setAttribute("id", csev.getVersionId());
        dataRow.setAttribute("object", csev);
        dataRow.appendChild(tcName);
        dataRow.appendChild(tcCode);        

        if (csev.getCodeSystemConcepts().size() > 0) {          
            CodeSystemConcept csc = csev.getCodeSystemConcepts().get(0);
            Label lCode = new Label();
      
            if(contMode == ContentConcepts_old.CONTENTMODE_VALUESET){
                if((cvsm.getStatus()) == 1 && cvsm.isIsStructureEntry()){
                    lCode.setValue("");
                }else{
                    lCode.setValue(csc.getCode());
                }
            }else{
                lCode.setValue(csc.getCode());
            }
               
            Html  lName = new Html(); 
            String html;
            
            // Hierarchiedetails
            if(treeNode.getCustomData() != null){
                int size = ((ArrayList<CodeSystemEntityVersion>)treeNode.getCustomData()).size();  
                int indent = 20;
                html = "<div style=\"padding-left:"+ size*indent +"px; margin:0;\">" + "<b><font color=\"#000000\">" + csc.getTerm() + "</font></b>" + "</div>";
                Iterator<CodeSystemEntityVersion> it = ((ArrayList<CodeSystemEntityVersion>)treeNode.getCustomData()).iterator();
                             
                while(it.hasNext()){ 
                    CodeSystemEntityVersion csevTemp    = it.next();
                    CodeSystemConcept       cscTemp     = csevTemp.getCodeSystemConcepts().get(0);
                    String                  term        = cscTemp.getTerm();                    
                    html = "<div style=\"padding-left:"+ --size*indent +"px; margin:0;\">" + term + "</div>" + html;
                }
            }   
            else
                html = "<div style=\"margin:0; padding:0;\">" + "<b><font color=\"#000000\">" + csc.getTerm() + "</font></b>" + "</div>";            
            
            lName.setContent(html);
            
            if(rootBond > -1 && (rootBond == 0 || rootBond > treeItem.getLevel()))                   
                lName.setStyle(lName.getStyle() + ";font-weight:bold");  
            
            
            if(treeNode.isLinkedConcept())
                lName.setStyle(lName.getStyle() + ";color:blue");   
            
            lCode.setMultiline(false);                     
            tcName.appendChild(lName);
            tcCode.appendChild(lCode);
        } else if (csev.getAssociationTypes().size() > 0) {
            AssociationType at = csev.getAssociationTypes().get(0);
            tcCode.setLabel(at.getForwardName() + "/" + at.getReverseName());
        }                 
    }
    
    @Override
    protected void renderCSEVMouseEvents(Treerow dataRow, Treeitem treeItem, final TreeNode treeNode){
        dataRow.addEventListener(Events.ON_DOUBLE_CLICK,  new EventListener() {					
            public void onEvent(Event event) throws Exception {                  
                popupSearch.showPopupConcept(PopupConcept.EDITMODES.DETAILSONLY);
            }
        });
        
        dataRow.addEventListener(Events.ON_CLICK,  new EventListener() {					
            public void onEvent(Event event) throws Exception {                  
                popupSearch.onSelect();                
            }
        });
        dataRow.addEventListener(Events.ON_RIGHT_CLICK,  new EventListener() {					
            public void onEvent(Event event) throws Exception {                  
                popupSearch.onSelect();                
            }
        });
    }
            
    @Override
    protected void renderCSEVContextMenu(Menupopup contextMenu, Treeitem ti, Treerow dataRow, final Object data)        {
        contextMenu.setParent(this.parentWindow);
        dataRow.setContext(contextMenu);
        
        Menuitem miDetails = new Menuitem(Labels.getLabel("common.details"));
        Menuitem miEdit = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editConcept"));
        Menuitem miStatus = new Menuitem(Labels.getLabel("treeitemRendererCSEV.editStatus"));
        miDetails.addEventListener("onClick", new EventListener(){
            public void onEvent(Event event) throws Exception {
                popupSearch.showPopupConcept(PopupConcept.EDITMODES.DETAILSONLY);
            }            
        });
        
        miEdit.addEventListener("onClick", new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            popupSearch.showPopupConcept(PopupConcept.EDITMODES.MAINTAIN);
          }
        });

        miStatus.addEventListener("onClick", new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            // TODO popupSearch.showPopupConcept(PopupWindow.EDITMODE_UPDATESTATUS);
          }
        });
        miDetails.setParent(contextMenu);
        
        if (SessionHelper.isUserLoggedIn())
        {
            /*boolean allowed = true;
            if (contMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
            {
                allowed = AssignTermHelper.isUserAllowed(((CodeSystemVersion)source).getCodeSystem());
            } else if(contMode == ContentConcepts.CONTENTMODE_VALUESET){

                allowed = AssignTermHelper.isUserAllowed(((ValueSetVersion)source).getValueSet());
            }

            if(allowed)*/{
            
                new Menuseparator().setParent(contextMenu);
                miEdit.setParent(contextMenu);
                //miStatus.setParent(contextMenu);
            }
        }
        sendbackMenuitem(parentWindow, ti, contextMenu);
    }    
}