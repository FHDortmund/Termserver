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
import de.fhdo.helper.DomainHelper;
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
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypesResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class AssociationEditor extends Window implements AfterCompose, IUpdate, IUpdateModal, EventListener, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  protected ContentAssociationEditor windowL, windowR;
  GenericList genericList;
  private ContentConcepts contentConcepts1, contentConcepts2;

  public AssociationEditor()
  {
    if(SessionHelper.isUserLoggedIn() == false)
      ViewHelper.gotoMain();
      //Executions.
      
    // TODO remove
    /*CodeSystem cs = new CodeSystem();
    cs.setId(5l);
    cs.setCurrentVersionId(6l);
    cs.setName("authorSpeciality");
    CodeSystemVersion csv = new CodeSystemVersion();
    csv.setVersionId(6l);
    csv.setName("authorSpeciality");
    cs.getCodeSystemVersions().add(csv);

    SessionHelper.setValue("selectedCS1", cs);

    cs = new CodeSystem();
    cs.setId(6l);
    cs.setCurrentVersionId(7l);
    cs.setName("practiceSettingCode");
    csv = new CodeSystemVersion();
    csv.setVersionId(7l);
    csv.setName("practiceSettingCode");
    cs.getCodeSystemVersions().add(csv);
    SessionHelper.setValue("selectedCS2", cs);*/
  }

  public void afterCompose()
  {
    logger.debug("AssociationEditor, afterCompose()");

    //((Borderlayout)getRoot()).setTitle(Labels.getLabel("common.associationEditor"));        
//    Radiogroup rgMode = (Radiogroup) getFellow("rgMode");
//    rgMode.addEventListener(Events.ON_CHECK, new EventListener()
//    {
//      public void onEvent(Event event) throws Exception
//      {
////                if(windowL != null && windowL.getWindowContentConcepts() != null)
////                    windowL.getWindowContentConcepts().setAssociationMode(getAssociationMode());
////                if(windowR != null && windowR.getWindowContentConcepts() != null)
////                    windowR.getWindowContentConcepts().setAssociationMode(getAssociationMode());
//      }
//    });
//
    //
    Include incL = (Include) getFellow("incContentLeft");
    incL.setDynamicProperty("updateListener", this);
    incL.setDynamicProperty("associationEditor", this);
    incL.setSrc(null);
    incL.setSrc("/gui/main/content/ContentAssociationEditor.zul?id=1");

    for (Component comp : incL.getChildren())
    {
      logger.debug("Comp: " + comp.getClass().getCanonicalName());
    }

//    windowL = (ContentAssociationEditor) incL.getFellow("winAss");
//    windowL.setUpdateListener(this);
    Include incR = (Include) getFellow("incContentRight");
    incR.setDynamicProperty("updateListener", this);
    incR.setDynamicProperty("associationEditor", this);
    incR.setSrc(null);
    incR.setSrc("/gui/main/content/ContentAssociationEditor.zul?id=2");
//    windowR = (ContentAssociationEditor) incR.getFellow("winAss");
//    windowR.setUpdateListener(this);

    fillAssociationTypeList();

    initList();
  }

  private void fillAssociationTypeList()
  {
    // cbRelation
    logger.debug("fillAssociationTypeList()");

    ListConceptAssociationTypesResponse.Return response = WebServiceHelper.listConceptAssociationTypes(new ListConceptAssociationTypesRequestType());
    logger.debug("response: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      final Combobox cb = (Combobox) getFellow("cbRelation");

      cb.setItemRenderer(new ComboitemRenderer<CodeSystemEntity>()
      {
        public void render(Comboitem item, CodeSystemEntity cse, int i) throws Exception
        {
          item.setValue(cse);
          CodeSystemEntityVersion csev = cse.getCodeSystemEntityVersions().get(0);
          AssociationType assType = csev.getAssociationTypes().get(0);
          item.setLabel(assType.getForwardName() + " <-> " + assType.getReverseName());

          if (cb.getSelectedItem() == null)
            cb.setSelectedItem(item);
        }
      });

      ListModelList lml = new ListModelList<CodeSystemEntity>(response.getCodeSystemEntity());
      cb.setModel(lml);

      logger.debug("association count: " + lml.getSize());

      //if(lml.getSize() > 0)
      //  cb.setSelectedIndex(0);
    }
  }

  private void initList()
  {
    //incList
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("common.code"), 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.relation"), 130, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.code"), 0, "", true, "String", true, true, false, false));

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    /*for (MetadataParameter meta : valueSet.getMetadataParameters())
     {
     GenericListRowType row = createRowFromMetadataParameter(meta);
     dataList.add(row);
     }*/
    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    //genericList.setUserDefinedId("1");
    genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(true);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);

    genericList.removeCustomButtons();

    Button buttonAuto = new Button(Labels.getLabel("common.automatic") + "...", "/rsc/img/design/automatic_16x16.png");
    buttonAuto.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        automaticAssociations();
      }
    });
    buttonAuto.setAttribute("disabled", false);
    buttonAuto.setAttribute("right", true);
    genericList.addCustomButton(buttonAuto);

    Button button = new Button(Labels.getLabel("common.save"), "/rsc/img/design/save_16x16.png");
    button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        saveAssociations();
      }
    });
    button.setAttribute("disabled", false);
    button.setAttribute("right", true);

    genericList.addCustomButton(button);

  }

  public void automaticAssociations()
  {
    logger.debug("automaticAssociations()");

    if (Messagebox.show(Labels.getLabel("common.automaticMsg"), Labels.getLabel("common.automatic"), Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
        == Messagebox.YES)
    {
      final org.zkoss.zk.ui.Desktop desktop = Executions.getCurrent().getDesktop();
      if (desktop.isServerPushEnabled() == false)
        desktop.enableServerPush(true);

      Clients.showBusy(Labels.getLabel("loginHelper.working"));

      final CodeSystem cs1 = (CodeSystem) SessionHelper.getValue("selectedCS1");
      final CodeSystem cs2 = (CodeSystem) SessionHelper.getValue("selectedCS2");

      // start timer to enable busy message
      Timer timer = new Timer();
      timer.schedule(new TimerTask()
      {
        @Override
        public void run()
        {
          try
          {
            //List<CodeSystemEntityVersionAssociation> list = new LinkedList<CodeSystemEntityVersionAssociation>();
            logger.debug("cs1: " + cs1.getName());
            logger.debug("cs2: " + cs2.getName());

            long csvId1 = cs1.getCodeSystemVersions().get(0).getVersionId();
            long csvId2 = cs2.getCodeSystemVersions().get(0).getVersionId();

            logger.debug("csvId1: " + csvId1);
            logger.debug("csvId2: " + csvId2);

            //ListCodeSystemConceptsRequestType 
            ListCodeSystemConceptsResponse.Return response1 = WebServiceHelper.listCodeSystemConcepts(csvId1);
            ListCodeSystemConceptsResponse.Return response2 = WebServiceHelper.listCodeSystemConcepts(csvId2);

            logger.debug("result 1: " + response1.getReturnInfos().getMessage());
            logger.debug("result 2: " + response2.getReturnInfos().getMessage());

            Executions.activate(desktop);

            if (response1.getReturnInfos().getStatus() == Status.OK
                && response2.getReturnInfos().getStatus() == Status.OK)
            {

              for (CodeSystemEntity cse1 : response1.getCodeSystemEntity())
              {
                CodeSystemEntityVersion csev1 = cse1.getCodeSystemEntityVersions().get(0);
                CodeSystemConcept csc1 = csev1.getCodeSystemConcepts().get(0);

                for (CodeSystemEntity cse2 : response2.getCodeSystemEntity())
                {
                  CodeSystemEntityVersion csev2 = cse2.getCodeSystemEntityVersions().get(0);
                  CodeSystemConcept csc2 = csev2.getCodeSystemConcepts().get(0);

                  // check same code
                  if (csc1.getCode().equals(csc2.getCode()))
                  {
                    // match
                    addAutomaticMatch(csev1, csev2);
                  }

                  // check containing text
                  if (csc1.getTerm().toLowerCase().contains(csc2.getTerm().toLowerCase()))
                  {
                    addAutomaticMatch(csev1, csev2);
                  }
                  if (csc2.getTerm().toLowerCase().contains(csc1.getTerm().toLowerCase()))
                  {
                    addAutomaticMatch(csev2, csev1);
                  }
                }

              }
            }

            // read data of both codesystems
            /*Include incL = (Include) getFellow("incContentLeft");
             ContentAssociationEditor contentEditor1 = (ContentAssociationEditor)incL.getDynamicProperty("instance");
            
             Include incR = (Include) getFellow("incContentRight");
             ContentAssociationEditor contentEditor2 = (ContentAssociationEditor)incL.getDynamicProperty("instance");
            
             ContentConcepts content1 = contentEditor1.getContent();
             ContentConcepts content2 = contentEditor2.getContent();
            
             TreeModel model1 = content1.getConcepts().getTreeConcepts().getModel();
             logger.debug("root: " + model1.getRoot().getClass().getCanonicalName());*/
            //Executions.schedule(desktop, el, new Event("finish", null, ""));
            Clients.clearBusy();

            Executions.deactivate(desktop);
          }
          catch (Exception ex)
          {
            LoggingOutput.outputException(ex, this);
            try
            {
              Executions.activate(desktop);

              Clients.clearBusy();
              Messagebox.show(ex.getLocalizedMessage());
              Executions.deactivate(desktop);
            }
            catch (Exception e)
            {

            }
          }
        }
      }, 10);
    }
  }

  private void addAutomaticMatch(CodeSystemEntityVersion csev1, CodeSystemEntityVersion csev2)
  {
    CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
    cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
    cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
    
    // check if association exists
    if(associationExists(cseva) == false)
    {
      update(cseva);
    }
  }
  
  private boolean associationExists(CodeSystemEntityVersionAssociation cseva)
  {
    long csvId1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId();
    ListConceptAssociationsRequestType request = new ListConceptAssociationsRequestType();
    request.setDirectionBoth(true);
    request.setCodeSystemEntity(new CodeSystemEntity());
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setVersionId(csvId1);
    request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
    
    ListConceptAssociationsResponse.Return response = WebServiceHelper.listConceptAssociations(request);
    if(response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.OK)
    {
      for(CodeSystemEntityVersionAssociation _cseva : response.getCodeSystemEntityVersionAssociation())
      {
        long csvId2 = 0;
        
        if(_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
          csvId2 = _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId();
        else if(_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
          csvId2 = _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId();
        
        if(csvId2 > 0 && csvId2 == cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId())
        {
          return true;
        }
        
        /*if((_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId().longValue() == 
            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() &&
           _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId().longValue() == 
            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId()) ||
            (_cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId().longValue() == 
            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId() &&
           _cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId().longValue() == 
            cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId()))
        {
          // exists
          return true;
        }*/
      }
    }
    
    return false;
  }

  private void saveAssociations()
  {
    logger.debug("saveAssociations()");

    final org.zkoss.zk.ui.Desktop desktop = Executions.getCurrent().getDesktop();
    if (desktop.isServerPushEnabled() == false)
      desktop.enableServerPush(true);
    final EventListener el = this;

    Clients.showBusy(Labels.getLabel("common.saveChanges") + "...");

    final List<CodeSystemEntityVersionAssociation> failureList = new LinkedList<CodeSystemEntityVersionAssociation>();

    // Import durchführen
    Timer timer = new Timer();
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        try
        {
          String sFailure = "";

          logger.debug("save associations, count: " + genericList.getListbox().getListModel().getSize());

          // save associations
          //for(Object obj : genericList.getListbox().getListModel())
          for (int i = 0; i < genericList.getListbox().getListModel().getSize(); ++i)
          {
            GenericListRowType row = (GenericListRowType) genericList.getListbox().getListModel().getElementAt(i);
            logger.debug("row type: " + row.getData().getClass().getCanonicalName());

            if (row.getData() instanceof CodeSystemEntityVersionAssociation)
            {
              CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) row.getData();
              //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1()

              CreateConceptAssociationRequestType request = new CreateConceptAssociationRequestType();
              request.setLoginToken(SessionHelper.getSessionId());
              request.setCodeSystemEntityVersionAssociation(cseva);

              CreateConceptAssociationResponse.Return response = WebServiceHelper.createConceptAssociation(request);

              logger.debug("result: " + response.getReturnInfos().getMessage());

              if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.FAILURE)
              {
                failureList.add(cseva);
                sFailure = response.getReturnInfos().getMessage();
              }
            }
          }

          //Executions.schedule(desktop, el, new Event("finish", null, ""));
          Executions.activate(desktop);

          Clients.clearBusy();

          List<GenericListRowType> rows = new LinkedList<GenericListRowType>();
          for (CodeSystemEntityVersionAssociation cseva : failureList)
          {
            rows.add(createRow(cseva));
          }
          genericList.setDataList(rows);

          // reload side lists (change mapping entries)
          reloadLists();

          if (sFailure.length() > 0)
            throw new Exception(sFailure);
          //  Messagebox.show(sFailure);

          Executions.deactivate(desktop);
//              Executions.schedule(desktop, el, new Event("updateStatus", null, msg));
        }
        catch (Exception ex)
        {
          LoggingOutput.outputException(ex, this);
          try
          {
            Executions.activate(desktop);

            Clients.clearBusy();
            Messagebox.show(ex.getLocalizedMessage());
            Executions.deactivate(desktop);
          }
          catch (Exception e)
          {

          }
        }
      }
    },
        100);

  }

  public void newAssociationConcept()
  {
    logger.debug("createConcept()");

    Map<String, Object> data = new HashMap<String, Object>();

    data.put("EditMode", PopupAssociationConcept.EDITMODES.CREATE);

    /*if (codeSystemVersionId > 0)
     data.put("ContentMode", PopupConcept.CONTENTMODE.CODESYSTEM);
     else if (valueSetVersionId > 0)
     data.put("ContentMode", PopupConcept.CONTENTMODE.VALUESET);*/
    //data.put("CodeSystemId", codeSystemId);
    //data.put("ValueSetId", valueSetId);
    try
    {
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupAssociationConcept.zul", null, data);
      ((PopupAssociationConcept) w).setUpdateListener(this);

      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }

  }

  private void reloadLists()
  {
    Include incL = (Include) getFellow("incContentLeft");
    incL.setSrc(null);
    incL.setSrc("/gui/main/content/ContentAssociationEditor.zul?id=1");

    Include incR = (Include) getFellow("incContentRight");
    incR.setSrc(null);
    incR.setSrc("/gui/main/content/ContentAssociationEditor.zul?id=2");
  }

  /*public int getAssociationMode()
   {
   return Integer.valueOf(((Radiogroup) getFellow("rgMode")).getSelectedItem().getValue().toString());  // TODO überprüfen, ob .toString() funktionert
   }*/
  public void update(Object o)
  {
    if (o != null)
      logger.debug("Update Association-Editor: " + o.getClass().getCanonicalName());

    if (o != null && o instanceof CodeSystemEntityVersionAssociation)
    {
      CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) o;
      cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntity(null);
      cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntity(null);

      Radiogroup rg = (Radiogroup) getFellow("rgAssociationKind");
      if (rg.getSelectedIndex() == 0)
        cseva.setAssociationKind(Definitions.ASSOCIATION_KIND.ONTOLOGY.getCode());
      else if (rg.getSelectedIndex() == 1)
        cseva.setAssociationKind(Definitions.ASSOCIATION_KIND.TAXONOMY.getCode());
      else if (rg.getSelectedIndex() == 2)
        cseva.setAssociationKind(Definitions.ASSOCIATION_KIND.CROSS_MAPPING.getCode());
      else if (rg.getSelectedIndex() == 3)
        cseva.setAssociationKind(Definitions.ASSOCIATION_KIND.LINK.getCode());

      GenericListRowType row = createRow(cseva);
      genericList.addEntry(row, 0);
    }

  }

  private GenericListRowType createRow(CodeSystemEntityVersionAssociation cseva)
  {
    GenericListRowType row = new GenericListRowType();

    CodeSystemConcept csc1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().get(0);
    CodeSystemConcept csc2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().get(0);

    String relation = "";
    Comboitem item = ((Combobox) getFellow("cbRelation")).getSelectedItem();
    if (item != null)
    {
      CodeSystemEntity cse = item.getValue();
      cseva.setAssociationType(cse.getCodeSystemEntityVersions().get(0).getAssociationTypes().get(0));
      relation = cseva.getAssociationType().getForwardName();
    }

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(csc1.getCode(), false, "", "", csc1.getTerm());
    cells[1] = new GenericListCellType(relation, false, "");
    cells[2] = new GenericListCellType(csc2.getCode(), false, "", "", csc2.getTerm());

    row.setData(cseva);
    row.setCells(cells);

    return row;
  }

  public void update(Object o, boolean edited)
  {
    // Update list
    if (o != null)
    {
      if (o instanceof CodeSystemEntity)
      {
        fillAssociationTypeList();  // reload list
//        CodeSystemEntity cse = (CodeSystemEntity) o;
//        if(edited)
//          genericList.updateEntry(createRow(null));
      }
    }
  }

  public void onEvent(Event t) throws Exception
  {

  }

  public void onNewClicked(String string)
  {
  }

  public void onEditClicked(String string, Object o)
  {
  }

  public void onDeleted(String string, Object o)
  {
  }

  public void onSelected(String string, Object o)
  {
    // TODO select entries in tables
    if (contentConcepts1 != null && contentConcepts2 != null)
    {
      try
      {
        logger.debug("select entries in tables, " + o.getClass().getCanonicalName());

        CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) o;
        String code1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().get(0).getCode();
        String code2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getCodeSystemConcepts().get(0).getCode();

        Tree tree1 = contentConcepts1.getConcepts().getTreeConcepts();
        Tree tree2 = contentConcepts2.getConcepts().getTreeConcepts();
        selectCodeInTree(tree1, null, code1);
        selectCodeInTree(tree2, null, code2);

      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
        Messagebox.show(ex.getLocalizedMessage());
      }
    }
    else
      logger.debug("contentConcepts is null");

  }

  private void selectCodeInTree(Tree tree, Treeitem treeItem, String code)
  {
    //tree.setSelectedItem(null);
    //Collection<Treeitem> items = null;

    if (treeItem != null)
    {
      //items = treeItem.getChildren();
      /*TODO for (Component comp : treeItem.getChildren())
      {
        logger.debug("Component, comp: " + comp.getClass().getCanonicalName());
        
      }*/
    }
    else
    {
      if (tree != null)
      {
        logger.debug("selectCodeInTree, code: " + code);
        //items = tree.getItems();
        for (Treeitem ti : tree.getItems())
        {
          logger.debug("Treeitem, value: " + ti.getValue().getClass().getCanonicalName());
          CodeSystemEntityVersion csev = ti.getValue();
          if (csev.getCodeSystemConcepts().get(0).getCode().equals(code))
          {
            // item found
            tree.setSelectedItem(ti);
            
            return;
          }

          selectCodeInTree(tree, ti, code);
        }
      }
    }

  }

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
  }
}
