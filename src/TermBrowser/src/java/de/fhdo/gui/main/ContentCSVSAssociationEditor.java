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

import de.fhdo.gui.main.modules.ContentConcepts_old;
import de.fhdo.helper.ValidityRangeHelper;
import de.fhdo.models.TreeModelCSEV;
import de.fhdo.models.TreeNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Include;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Treeitem;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Sven Becker
 */
public class ContentCSVSAssociationEditor extends ContentCSVSDefault
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Radiogroup rgMode;

  @Override
  public void afterCompose()
  {
    super.afterCompose();
    rgMode = (Radiogroup) Executions.getCurrent().getAttribute("radioGroupAssociationMode");
  }

  @Override
  public void loadConceptsBySelectedItem(boolean draggable, boolean droppable)
  {
    /* TODO
    Object source = treeActive.getSelectedItem();
    Treeitem ti = null;
    String name,
            versionName;
    int mode;
    long id,
            versionId,
            validityRange;

    // Objekte vorhanden?
    if (source != null)
    {
      ti = (Treeitem) source;
      source = ((TreeNode) ti.getValue()).getData();
    }

    // CS,VS bzw ihre aktuellen Versionen laden
    if (source instanceof CodeSystem)
    {
      // oeffne die letzte Version
      if (true)
      { // TODO: Hier muss noch der entsprechende Flag vom UserAccount benutzt werden
        selectCurrentCSV(ti);
        source = selectedCSV; // damit danach der Content der csv geladen werden kann
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
      }
      else
      {
        selectedVSV = null;
      }
    }

    // Parameter zum laden des Content angeben
    if (source instanceof CodeSystemVersion)
    {
      mode = ContentConcepts.CONTENTMODE_CODESYSTEM;
      id = selectedCSV.getCodeSystem().getId();
      versionId = selectedCSV.getVersionId();
      name = selectedCSV.getCodeSystem().getName();
      versionName = selectedCSV.getName();
      if (selectedCSV.getValidityRange() != null)
      {
        validityRange = selectedCSV.getValidityRange();
      }
      else
      {
        validityRange = 238l;
      }
    }
    else if (source instanceof ValueSetVersion)
    {
      mode = ContentConcepts.CONTENTMODE_VALUESET;
      id = selectedVSV.getValueSet().getId();
      versionId = selectedVSV.getVersionId();
      name = selectedVSV.getValueSet().getName();
//            validityRange = selectedVSV.getValidityRange();
      validityRange = -1;
      versionName = "";
    }
    else
    {
      return;
    }

    // Lade Content
    try
    {
      String validityRangeDT = ValidityRangeHelper.getValidityRangeNameById(validityRange);// Lade Range of Validity
      Include inc = (Include) getFellow("incConcepts");    // lade Concepts in include@ComponentTreeAndContent         
      TreeModelCSEV treeModel = new TreeModelCSEV(source);
      inc.setMode("instant");
      inc.setSrc(null);

      // Änderung von AssoEditor
      inc.setDynamicProperty("radioGroupAssociationMode", rgMode);

      inc.setDynamicProperty("parent", this);
      inc.setDynamicProperty("source", source);
      inc.setDynamicProperty("id", id);
      inc.setDynamicProperty("name", name);
      inc.setDynamicProperty("versionId", versionId);
      inc.setDynamicProperty("versionName", versionName);
      inc.setDynamicProperty("validityRange", validityRangeDT);
      inc.setDynamicProperty("contentMode", mode);
      inc.setDynamicProperty("droppable", droppable);
      inc.setDynamicProperty("draggable", draggable);

      // Kapitel durch Deep Links auswählen
      if (deepLinks.isEmpty() == false)
        inc.setDynamicProperty("deepLinks", deepLinks);
      else
        inc.setDynamicProperty("deepLinks", null);

      // HugeFlatData oder Baum?
      if (treeModel.getTotalSize() > 100)
      {
        inc.setSrc("modules/ContentConceptsHugeFlatData.zul");
      }
      else
      {
        inc.setDynamicProperty("treeModel", treeModel);
        inc.setSrc("modules/ContentConcepts.zul");
      }
      windowContentConcepts = (ContentConcepts) inc.getFellow("windowConcepts"); // geht nur im instant mode
    }
    catch (Exception e)
    {
      Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, e);
    }*/

//        Object o = treeActive.getSelectedItem();
//        if(o != null)
//            o = ((TreeNode)((Treeitem)o).getValue()).getData();
//        
//        String name, versionName;
//        int mode;
//        long id, versionId;
//        if(o instanceof CodeSystemVersion){
//            mode        = ContentConcepts.CONTENTMODE_CODESYSTEM;
//            id          = selectedCSV.getCodeSystem().getId();
//            versionId   = selectedCSV.getVersionId();
//            name        = selectedCSV.getCodeSystem().getName();
//            versionName = selectedCSV.getName();
//        }
//        else if(o instanceof ValueSetVersion){
//            mode        = ContentConcepts.CONTENTMODE_VALUESET;
//            id          = selectedVSV.getValueSet().getId();
//            versionId   = selectedVSV.getVersionId();
//            name        = selectedVSV.getValueSet().getName();
//            versionName = "";
//        }
//        else{
//            return;
//        }
//        try{
//            Include inc = (Include) getFellow("incConcepts");    // lade Concepts in include@ComponentTreeAndContent         
//            TreeModelCSEV treeModel = new TreeModelCSEV(id, versionId, mode);                                                                
//            inc.setMode("instant");            
//            inc.setSrc(null);
//            
//            // Änderung von AssoEditor
//            inc.setDynamicProperty("radioGroupAssociationMode", rgMode);
//            
//            inc.setDynamicProperty("parentSendBack",        this);    
//            inc.setDynamicProperty("id",            id);
//            inc.setDynamicProperty("name",          name);
//            inc.setDynamicProperty("versionId",     versionId);
//            inc.setDynamicProperty("versionName",   versionName);
//            inc.setDynamicProperty("contentMode",   mode);                                                
//            inc.setDynamicProperty("droppable",     droppable);
//            inc.setDynamicProperty("draggable",     draggable);
//
//            if(treeModel.getTotalSize() > 100){                    
//                inc.setSrc("modules/ContentConceptsHugeFlatData.zul");  
//            }
//            else{
//                inc.setDynamicProperty("treeModel", treeModel);
//                inc.setSrc("modules/ContentConcepts.zul");
//            }   
//            windowContentConcepts = (ContentConcepts)inc.getFellow("windowConcepts"); // geht nur im instant mode
//        } catch(Exception e){e.printStackTrace();} 
  }
}
