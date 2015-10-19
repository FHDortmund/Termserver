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

import de.fhdo.Definitions;
import de.fhdo.gui.main.content.ContentAssociationEditor;
import de.fhdo.gui.main.content.ContentConcepts;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.ViewHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypesResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ValuesetEditor extends Window implements AfterCompose, IUpdate, IUpdateModal, EventListener
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  protected ContentAssociationEditor windowL, windowR;
  private ContentConcepts contentConcepts1, contentConcepts2;

  public ValuesetEditor()
  {
    if (SessionHelper.isUserLoggedIn() == false)
      ViewHelper.gotoMain();

  }

  public void afterCompose()
  {
    logger.debug("AssociationEditor, afterCompose()");

    Include incL = (Include) getFellow("incContentLeft");
    incL.setDynamicProperty("updateListener", this);
    incL.setDynamicProperty("valuesetEditor", this);
    incL.setDynamicProperty("title", Labels.getLabel("common.source") + " (" + Labels.getLabel("common.codeSystem") + ")");
    incL.setSrc(null);
    incL.setSrc("/gui/main/content/ContentAssociationEditor.zul?id=1&allowVS=false&allowCS=true");

    for (Component comp : incL.getChildren())
    {
      logger.debug("Comp: " + comp.getClass().getCanonicalName());
    }

    Include incR = (Include) getFellow("incContentRight");
    incR.setDynamicProperty("updateListener", this);
    incR.setDynamicProperty("valuesetEditor", this);
    incR.setDynamicProperty("title", Labels.getLabel("common.destination") + " (" + Labels.getLabel("common.valueSet") + ")");
    incR.setSrc(null);
    incR.setSrc("/gui/main/content/ContentAssociationEditor.zul?id=2&allowCS=false&allowVS=true");

    //initList();
  }

//  private boolean associationExists(CodeSystemEntityVersionAssociation cseva)
//  {
//    long csvId1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId();
//    ListConceptAssociationsRequestType request = new ListConceptAssociationsRequestType();
//    request.setDirectionBoth(true);
//    request.setCodeSystemEntity(new CodeSystemEntity());
//    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
//    csev.setVersionId(csvId1);
//    request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
//    
//    ListConceptAssociationsResponse.Return response = WebServiceHelper.listConceptAssociations(request);
//    if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.OK)
//    {
//      for(CodeSystemEntityVersionAssociation _cseva : response.getCodeSystemEntityVersionAssociation())
//      {
//        long csvId2 = 0;
//        
//        if(_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
//          csvId2 = _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId();
//        else if(_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
//          csvId2 = _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
//        
//        if(csvId2 > 0 && csvId2 == cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId())
//        {
//          return true;
//        }
//        
//        /*if((_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId().longValue() == 
//            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() &&
//           _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId().longValue() == 
//            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId()) ||
//            (_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId().longValue() == 
//            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() &&
//           _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId().longValue() == 
//            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId()))
//        {
//          // exists
//          return true;
//        }*/
//      }
//    }
//    
//    return false;
//  }
//  private void saveAssociations()
//  {
//    logger.debug("saveAssociations()");
//
//    final org.zkoss.zk.ui.Desktop desktop = Executions.getCurrent().getDesktop();
//    if (desktop.isServerPushEnabled() == false)
//      desktop.enableServerPush(true);
//    final EventListener el = this;
//
//    Clients.showBusy(Labels.getLabel("common.saveChanges") + "...");
//
//    final List<CodeSystemEntityVersionAssociation> failureList = new LinkedList<CodeSystemEntityVersionAssociation>();
//
//    // Import durchführen
//    Timer timer = new Timer();
//    timer.schedule(new TimerTask()
//    {
//      @Override
//      public void run()
//      {
//        try
//        {
//          String sFailure = "";
//
//          logger.debug("save associations, count: " + genericList.getListbox().getListModel().getSize());
//
//          // save associations
//          //for(Object obj : genericList.getListbox().getListModel())
//          for (int i = 0; i < genericList.getListbox().getListModel().getSize(); ++i)
//          {
//            GenericListRowType row = (GenericListRowType) genericList.getListbox().getListModel().getElementAt(i);
//            logger.debug("row type: " + row.getData().getClass().getCanonicalName());
//
//            if (row.getData() instanceof CodeSystemEntityVersionAssociation)
//            {
//              CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) row.getData();
//              //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1()
//
//              CreateConceptAssociationRequestType request = new CreateConceptAssociationRequestType();
//              request.setLoginToken(SessionHelper.getSessionId());
//              request.setCodeSystemEntityVersionAssociation(cseva);
//
//              CreateConceptAssociationResponse.Return response = WebServiceHelper.createConceptAssociation(request);
//
//              logger.debug("result: " + response.getReturnInfos().getMessage());
//
//              if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.FAILURE)
//              {
//                failureList.add(cseva);
//                sFailure = response.getReturnInfos().getMessage();
//              }
//            }
//          }
//
//          //Executions.schedule(desktop, el, new Event("finish", null, ""));
//          Executions.activate(desktop);
//
//          Clients.clearBusy();
//
//          List<GenericListRowType> rows = new LinkedList<GenericListRowType>();
//          for (CodeSystemEntityVersionAssociation cseva : failureList)
//          {
//            rows.add(createRow(cseva));
//          }
//          genericList.setDataList(rows);
//
//          // reload side lists (change mapping entries)
//          reloadLists();
//
//          if (sFailure.length() > 0)
//            throw new Exception(sFailure);
//          //  Messagebox.show(sFailure);
//
//          Executions.deactivate(desktop);
////              Executions.schedule(desktop, el, new Event("updateStatus", null, msg));
//        }
//        catch (Exception ex)
//        {
//          LoggingOutput.outputException(ex, this);
//          try
//          {
//            Executions.activate(desktop);
//
//            Clients.clearBusy();
//            Messagebox.show(ex.getLocalizedMessage());
//            Executions.deactivate(desktop);
//          }
//          catch (Exception e)
//          {
//
//          }
//        }
//      }
//    },
//        100);
//
//  }
//  public void newAssociationConcept()
//  {
//    logger.debug("createConcept()");
//
//    Map<String, Object> data = new HashMap<String, Object>();
//
//    data.put("EditMode", PopupAssociationConcept.EDITMODES.CREATE);
//
//    /*if (codeSystemVersionId > 0)
//     data.put("ContentMode", PopupConcept.CONTENTMODE.CODESYSTEM);
//     else if (valueSetVersionId > 0)
//     data.put("ContentMode", PopupConcept.CONTENTMODE.VALUESET);*/
//    //data.put("CodeSystemId", codeSystemId);
//    //data.put("ValueSetId", valueSetId);
//    try
//    {
//      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupAssociationConcept.zul", null, data);
//      ((PopupAssociationConcept) w).setUpdateListener(this);
//
//      w.doModal();
//    }
//    catch (Exception e)
//    {
//      LoggingOutput.outputException(e, this);
//    }
//
//  }
  public void update(Object o)
  {
    if (o != null)
      logger.debug("Update Association-Editor: " + o.getClass().getCanonicalName());

    if (o != null && o instanceof CodeSystemEntityVersionAssociation)
    {
      CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) o;
      //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntity(null);
      //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntity(null);

      //logger.debug("save value set entry with id " + cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() + ", id2: " + 
      //    cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId());
      
      logger.debug("1. getCodeSystemVersion: " + contentConcepts1.getCodeSystemVersion()); 
      logger.debug("1. getValueSetVersion: " + contentConcepts1.getValueSetVersion());
      
      logger.debug("2. getCodeSystemVersion: " + contentConcepts2.getCodeSystemVersion()); 
      logger.debug("2. getValueSetVersion: " + contentConcepts2.getValueSetVersion());
      
      if (contentConcepts2.getValueSetVersion() != null)
      {
        logger.debug("save value set entry with id " + cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() + ", to value set with id: "
            + contentConcepts2.getValueSetVersion().getVersionId());
        
        // save new value set membership
        if(addConceptToValueSet(cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId(), contentConcepts2.getValueSetVersion().getVersionId()))
        {
          // add entry to list (reload valueset)
          contentConcepts2.reload();
        }
      }
      else logger.warn("no value set loaded at destination");

      //GenericListRowType row = createRow(cseva);
      //genericList.addEntry(row, 0);
    }

  }
  
  private boolean addConceptToValueSet(long codeSystemEntityVersionId, long valueSetVersionId)
  {
    logger.debug("addConceptToValueSet, csev-id: " + codeSystemEntityVersionId + ", vsv-id: " + valueSetVersionId);
    // create request
    CreateValueSetContentRequestType request = new CreateValueSetContentRequestType();
    // set login
    request.setLoginToken(SessionHelper.getSessionId());
    
    // add value set version to request
    ValueSetVersion vsv = new ValueSetVersion();
    vsv.setVersionId(valueSetVersionId);
    request.setValueSet(new ValueSet());
    request.getValueSet().getValueSetVersions().add(vsv);
    
    // add concept to request
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setVersionId(codeSystemEntityVersionId);
    CodeSystemEntity cse = new CodeSystemEntity();
    cse.getCodeSystemEntityVersions().add(csev);
    request.getCodeSystemEntity().add(cse);
    
    // do webservice call
    CreateValueSetContentResponse.Return response = WebServiceHelper.createValueSetContent(request);
    
    logger.debug("Response: " + response.getReturnInfos().getMessage());
    
    if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.FAILURE)
    {
      // show error message
      Messagebox.show(response.getReturnInfos().getMessage());
      return false;
    }
    
    
    return true;
  }

  public void update(Object o, boolean edited)
  {
    // Update list
//    if (o != null)
//    {
//      if (o instanceof CodeSystemEntity)
//      {
//        fillAssociationTypeList();  // reload list
////        CodeSystemEntity cse = (CodeSystemEntity) o;
////        if(edited)
////          genericList.updateEntry(createRow(null));
//      }
//    }
  }

  public void onEvent(Event t) throws Exception
  {

  }

  public void onSelected(String string, Object o)
  {
    // TODO select entries in tables
//    if (contentConcepts1 != null && contentConcepts2 != null)
//    {
//      try
//      {
//        logger.debug("select entries in tables, " + o.getClass().getCanonicalName());
//
//        CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) o;
//        String code1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().get(0).getCode();
//        String code2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().get(0).getCode();
//
//        Tree tree1 = contentConcepts1.getConcepts().getTreeConcepts();
//        Tree tree2 = contentConcepts2.getConcepts().getTreeConcepts();
//        selectCodeInTree(tree1, null, code1);
//        selectCodeInTree(tree2, null, code2);
//
//      }
//      catch (Exception ex)
//      {
//        LoggingOutput.outputException(ex, this);
//        Messagebox.show(ex.getLocalizedMessage());
//      }
//    }
//    else
//      logger.debug("contentConcepts is null");

  }

//  private void selectCodeInTree(Tree tree, Treeitem treeItem, String code)
//  {
//    //tree.setSelectedItem(null);
//    //Collection<Treeitem> items = null;
//
//    if (treeItem != null)
//    {
//      //items = treeItem.getChildren();
//      /*TODO for (Component comp : treeItem.getChildren())
//      {
//        logger.debug("Component, comp: " + comp.getClass().getCanonicalName());
//        
//      }*/
//    }
//    else
//    {
//      if (tree != null)
//      {
//        logger.debug("selectCodeInTree, code: " + code);
//        //items = tree.getItems();
//        for (Treeitem ti : tree.getItems())
//        {
//          logger.debug("Treeitem, value: " + ti.getValue().getClass().getCanonicalName());
//          CodeSystemEntityVersion csev = ti.getValue();
//          if (csev.getCodeSystemConcepts().get(0).getCode().equals(code))
//          {
//            // item found
//            tree.setSelectedItem(ti);
//            
//            return;
//          }
//
//          selectCodeInTree(tree, ti, code);
//        }
//      }
//    }
//
//  }
  /**
   * @return the contentConcepts1
   */
  public ContentConcepts getContentConcepts1()
  {
    return contentConcepts1;
  }

  /**
   * @param contentConcepts1 the contentConcepts1 to set
   */
  public void setContentConcepts1(ContentConcepts contentConcepts1)
  {
    this.contentConcepts1 = contentConcepts1;
    logger.debug("[ValuesetEditor.java] contentConcepts1 set");
  }

  /**
   * @return the contentConcepts2
   */
  public ContentConcepts getContentConcepts2()
  {
    return contentConcepts2;
  }

  /**
   * @param contentConcepts2 the contentConcepts2 to set
   */
  public void setContentConcepts2(ContentConcepts contentConcepts2)
  {
    this.contentConcepts2 = contentConcepts2;
    logger.debug("[ValuesetEditor.java] contentConcepts2 set");
  }
}
