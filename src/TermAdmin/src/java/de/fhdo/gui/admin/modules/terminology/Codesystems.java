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
package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.gui.templates.NameInputbox;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.ws.authoring.DeleteInfo;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;

/**
 *
 * @author rober
 */
public class Codesystems extends Window implements AfterCompose, IUpdateModal, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  enum Mode
  {

    CODESYSTEM, VALUESET
  };
  private Mode mode;

  enum CreateMode
  {

    ITEM, VERSION_ITEM, NONE
  };
  private CreateMode createMode;

  GenericList genericList;
  GenericList genericListVersion;
  private Object selectedItem;
  private Object selectedItemVersion;

  public Codesystems()
  {
    String modeStr = "";
    Object o = ArgumentHelper.getWindowParameter("mode");
    if (o != null)
      modeStr = o.toString();

    logger.debug("Mode: " + modeStr);

    if (modeStr.equals("vs") || modeStr.equals("valueset"))
      mode = Mode.VALUESET;
    else
      mode = Mode.CODESYSTEM;

    selectedItem = SessionHelper.getValue("CS_SelectedItem");
    selectedItemVersion = SessionHelper.getValue("CS_SelectedItemVersion");
  }

  public void afterCompose()
  {
    initList();

  }

  public void update(Object o, boolean edited)
  {
    if (o == null)
      return;

    // update status in db
    if (o instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) o;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        Transaction tx = hb_session.beginTransaction();

        CodeSystemVersion csv_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, csv.getVersionId());
        csv_db.setStatus(csv.getStatus());

        hb_session.update(csv_db);
        tx.commit();

        // show changes in tree view
        GenericListRowType row = createRowFromCodesystemVersion(csv);
        genericListVersion.updateEntry(row);
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

      initDetails();

    }

    if (o instanceof String)
    {
      String inputStr = (String) o;

      if (createMode == CreateMode.ITEM)
      {
        createNewCodeSystem(inputStr);
      }
      else if (createMode == CreateMode.VERSION_ITEM)
      {
        createNewCodeSystemVersion(inputStr);
      }
    }
  }

  private void initList()
  {
    logger.debug("initList()");

    try
    {
      West title = (West) getFellow("titleItem");
      int selectedIndex = -1;

      // header information for list view
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(Labels.getLabel("name"), 0, "", true, "String", true, true, false, false));

      // load data from db
      SessionFactory sf = HibernateUtil.getNewSessionFactory();
      Session hb_session = sf.openSession();
      //Session hb_session = HibernateUtil.getSessionFactory().openSession();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
        if (mode == Mode.VALUESET)
        {
          ValueSet selectedVS = null;
          
          if(selectedItem instanceof ValueSet)
            selectedVS = (ValueSet) selectedItem;

          String hql = "from ValueSet order by name";

          Query q = hb_session.createQuery(hql);
          q.setCacheable(false);
          q.setCacheMode(CacheMode.IGNORE);

          hb_session.setCacheMode(CacheMode.IGNORE);
          hb_session.clear();
          hb_session.flush();
          
          logger.debug("hql: " + hql);

          List<ValueSet> vsList = q.list();

          for (int i = 0; i < vsList.size(); ++i)
          {
            ValueSet vs = vsList.get(i);
            GenericListRowType row = createRowFromValueSet(vs);

            dataList.add(row);

            if (selectedVS != null)
            {
              if (vs.getId().longValue() == selectedVS.getId())
                selectedIndex = i;
            }
          }

          // set title
          title.setTitle(Labels.getLabel("valuesets"));
        }
        else
        {
          CodeSystem selectedCS = null;
          
          if(selectedItem instanceof CodeSystem)
            selectedCS = (CodeSystem) selectedItem;

          String hql = "from CodeSystem order by name";
          Query q = hb_session.createQuery(hql);
          q.setCacheable(false);
          q.setCacheMode(CacheMode.IGNORE);

          hb_session.setCacheMode(CacheMode.IGNORE);
          hb_session.clear();
          hb_session.flush();

          logger.debug("hql: " + hql);
          List<CodeSystem> csList = q.list();

          for (int i = 0; i < csList.size(); ++i)
          {
            CodeSystem cs = csList.get(i);
            GenericListRowType row = createRowFromCodesystem(cs);

            dataList.add(row);

            if (selectedCS != null)
            {
              if (cs.getId().longValue() == selectedCS.getId())
                selectedIndex = i;
            }
          }

          // set title
          title.setTitle(Labels.getLabel("codesystems"));
        }
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

      // initialize list
      Include inc = (Include) getFellow("incList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericList = (GenericList) winGenericList;
      genericList.setListId("list");

      genericList.setListActions(this);
      genericList.setButton_new(true);
      genericList.setButton_edit(false);
      genericList.setButton_delete(true);
      genericList.setListHeader(header);
      genericList.setDataList(dataList);

      if (selectedIndex >= 0)
        genericList.setSelectedIndex(selectedIndex);
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

    initListVersion();
  }

  private void initListVersion()
  {
    logger.debug("initListVersion()");

    logger.debug("selectedItem: " + selectedItem);

    Include incVersions = (Include) getFellow("incListVersions");
    West title = (West) getFellow("titleVersion");

    if (selectedItem == null)
    {
      logger.debug("show empty message");
      if(mode == Mode.VALUESET)
        title.setTitle(Labels.getLabel("valuesetVersion"));
      else title.setTitle(Labels.getLabel("codesystemVersion"));

      incVersions.setSrc(null);
      incVersions.setSrc("/gui/templates/MessageInclude.zul?msg=" + Labels.getLabel("noSelection"));
    }
    else
    {
      logger.debug("show version list");

      int selectedIndex = -1;

      incVersions.setSrc(null);
      incVersions.setSrc("/gui/templates/GenericList.zul");

      try
      {
        // header information for list view
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType(Labels.getLabel("name"), 0, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType(Labels.getLabel("status"), 80, "", true, "String", true, true, false, false));

        // load data from db
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.setCacheMode(org.hibernate.CacheMode.IGNORE);
        hb_session.clear();

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
        try
        {
          if (mode == Mode.VALUESET)
          {
            // fill version list with value set versions
            ValueSet selectedVS = (ValueSet) selectedItem;
            ValueSetVersion selectedVSV = (ValueSetVersion) selectedItemVersion;

            String hql = "from ValueSetVersion where valueSetId=:vs_id order by name";
            Query q = hb_session.createQuery(hql);
            q.setParameter("vs_id", selectedVS.getId());

            q.setCacheable(false);
            q.setCacheMode(CacheMode.IGNORE);

            hb_session.setCacheMode(CacheMode.IGNORE);
            hb_session.clear();
            hb_session.flush();

            List<ValueSetVersion> vsList = q.list();

            for (int i = 0; i < vsList.size(); ++i)
            {
              ValueSetVersion vsv = vsList.get(i);
              GenericListRowType row = createRowFromValueSetVersion(vsv);

              dataList.add(row);

              if (selectedVSV != null)
              {
                if (vsv.getVersionId().longValue() == selectedVSV.getVersionId())
                  selectedIndex = i;
              }
            }

            // set title
            title.setTitle(Labels.getLabel("valuesetVersion") + " - " + selectedVS.getName());
          }
          else
          {
            // fill version list with code system versions
            CodeSystem selectedCS = (CodeSystem) selectedItem;
            CodeSystemVersion selectedCSV = (CodeSystemVersion) selectedItemVersion;

            String hql = "from CodeSystemVersion where codeSystemId=:cs_id order by name";
            Query q = hb_session.createQuery(hql);
            q.setParameter("cs_id", selectedCS.getId());

            q.setCacheable(false);
            q.setCacheMode(CacheMode.IGNORE);

            hb_session.setCacheMode(CacheMode.IGNORE);
            hb_session.clear();
            hb_session.flush();

            List<CodeSystemVersion> csList = q.list();

            for (int i = 0; i < csList.size(); ++i)
            {
              CodeSystemVersion csv = csList.get(i);
              GenericListRowType row = createRowFromCodesystemVersion(csv);

              dataList.add(row);

              if (selectedCSV != null)
              {
                if (csv.getVersionId().longValue() == selectedCSV.getVersionId())
                  selectedIndex = i;
              }
            }

            // set title
            title.setTitle(Labels.getLabel("codesystemVersion") + " - " + selectedCS.getName());
          }
        }
        catch (Exception e)
        {
          LoggingOutput.outputException(e, this);
        }
        finally
        {
          hb_session.close();
        }

        // initialize list
        Window winGenericList = (Window) incVersions.getFellow("winGenericList");
        genericListVersion = (GenericList) winGenericList;
        genericListVersion.setListId("listVersion");

        genericListVersion.setListActions(this);
        genericListVersion.setButton_new(true);
        genericListVersion.setButton_edit(false);
        genericListVersion.setButton_delete(true);
        genericListVersion.setListHeader(header);
        genericListVersion.setDataList(dataList);

        if (selectedIndex >= 0)
          genericListVersion.setSelectedIndex(selectedIndex);

        Button button = new Button(Labels.getLabel("changeStatus") + "...");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
        {
          public void onEvent(Event t) throws Exception
          {
            changeStatus();
          }
        });

        genericListVersion.removeCustomButtons();
        genericListVersion.addCustomButton(button);
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
    }

    initDetails();
  }

  
  
  private void initDetails()
  {
    logger.debug("initDetails()");

    logger.debug("selectedItemVersion: " + selectedItemVersion);

    Center title = (Center) getFellow("titleContent");

    Include incContent = (Include) getFellow("incContent");
    //if (selectedItemVersion == null)
    if (selectedItem == null)
    {
      logger.debug("show empty message");

      title.setTitle(Labels.getLabel("details"));

      incContent.setSrc(null);
      incContent.setSrc("/gui/templates/MessageInclude.zul?msg=" + Labels.getLabel("noSelection"));
    }
    else
    {
      logger.debug("show details");
      incContent.setSrc(null);
      //incContent.setSrc("/gui/templates/GenericList.zul");

      if (mode == Mode.VALUESET && selectedItem instanceof ValueSet)
      {
        ValueSet selectedVS = (ValueSet) selectedItem;
        ValueSetVersion selectedVSV = (ValueSetVersion) selectedItemVersion;

        title.setTitle(Labels.getLabel("details") + " | " + selectedVS.getName() + " - " + (selectedVSV == null ? "" : selectedVSV.getName()));
      }
      else if(selectedItem instanceof CodeSystem)
      {
        CodeSystem selectedCS = (CodeSystem) selectedItem;
        CodeSystemVersion selectedCSV = (CodeSystemVersion) selectedItemVersion;

        title.setTitle(Labels.getLabel("details") + " | " + selectedCS.getName() + " - " + (selectedCSV == null ? "" : selectedCSV.getName()));
      }

      incContent.setSrc("/gui/admin/modules/terminology/codesystemsContent.zul");
    }
  }

  private GenericListRowType createRowFromCodesystem(CodeSystem cs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(cs.getId(), false, "");
    cells[1] = new GenericListCellType(cs.getName(), false, "");

    row.setData(cs);
    row.setCells(cells);

    return row;
  }

  private GenericListRowType createRowFromValueSet(ValueSet vs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(vs.getId(), false, "");
    cells[1] = new GenericListCellType(vs.getName(), false, "");

    row.setData(vs);
    row.setCells(cells);

    return row;
  }

  private GenericListRowType createRowFromCodesystemVersion(CodeSystemVersion csv)
  {
    GenericListRowType row = new GenericListRowType();

    logger.debug("render csv with id: " + csv.getVersionId());

    CodeSystem selectedCS = (CodeSystem) selectedItem;
    String style = "";

    if (selectedCS != null && selectedCS.getCurrentVersionId() != null && selectedCS.getCurrentVersionId().longValue() == csv.getVersionId())
    {
      style = "font-weight: bold; color: #000;";
    }

    logger.debug("style: " + style);

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(createListcellText(csv.getVersionId() + "", style), false, csv.getVersionId() + "");
    cells[1] = new GenericListCellType(createListcellText(csv.getName(), style), false, csv.getName());
    cells[2] = new GenericListCellType(createListcellText(Definitions.STATUS_CODES.readLabel(csv.getStatus()), style), false, Definitions.STATUS_CODES.readLabel(csv.getStatus()));

    row.setData(csv);
    row.setCells(cells);

    return row;
  }

  private Listcell createListcellText(String text, String style)
  {
    Listcell lc = new Listcell();
    Label label = new Label(text);
    label.setStyle(style);
    lc.appendChild(label);
    return lc;
  }

  private GenericListRowType createRowFromValueSetVersion(ValueSetVersion vsv)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(vsv.getVersionId(), false, "");
    cells[1] = new GenericListCellType(vsv.getName(), false, "");
    cells[2] = new GenericListCellType(Definitions.STATUS_CODES.readLabel(vsv.getStatus()), false, "");

    row.setData(vsv);
    row.setCells(cells);

    return row;
  }

  public void onNewClicked(String id)
  {
    if (id == null || id.equals("list"))
    {
      // create new code system / value set without version
      createMode = CreateMode.ITEM;
      showNameInputDialog();
    }
    else if (id.equals("listVersion"))
    {
      // create new code system version / value set version
      createMode = CreateMode.VERSION_ITEM;
      showNameInputDialog();
    }
  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
    if (id == null || id.equals("list"))
    {
      logger.debug("Type: " + data.getClass().getCanonicalName());

      RemoveTerminologyOrConceptRequestType request = new RemoveTerminologyOrConceptRequestType();
      request.setLoginToken(SessionHelper.getSessionId());
      request.setDeleteInfo(new DeleteInfo());

      if (data instanceof ValueSet)
      {
        // deletes a whole value set with all versions
        ValueSet vs = (ValueSet) data;

        request.getDeleteInfo().setType(Type.VALUE_SET);
        request.getDeleteInfo().setValueSet(new types.termserver.fhdo.de.ValueSet());
        request.getDeleteInfo().getValueSet().setId(vs.getId());
      }
      else if (data instanceof CodeSystem)
      {
        // deletes a whole code system with all versions
        CodeSystem cs = (CodeSystem) data;

        request.getDeleteInfo().setType(Type.CODE_SYSTEM);
        request.getDeleteInfo().setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
        request.getDeleteInfo().getCodeSystem().setId(cs.getId());
      }

      // do webservice call
      RemoveTerminologyOrConceptResponseType response = WebServiceHelper.removeTerminologyOrConcept(request);
      logger.debug("reponse: " + response.getReturnInfos().getMessage());

      Messagebox.show(response.getReturnInfos().getMessage());
      //initList();
      
      SessionHelper.setValue("CS_SelectedItem", null);
      SessionHelper.getValue("CS_SelectedItemVersion", null);
      SessionHelper.setValue("VS_SelectedItem", null);
      SessionHelper.getValue("VS_SelectedItemVersion", null);
      Executions.sendRedirect(null);
    }
    else if (id.equals("listVersion"))
    {
      logger.debug("Type: " + data.getClass().getCanonicalName());

      RemoveTerminologyOrConceptRequestType request = new RemoveTerminologyOrConceptRequestType();
      request.setLoginToken(SessionHelper.getSessionId());
      request.setDeleteInfo(new DeleteInfo());

      if (data instanceof ValueSetVersion)
      {
        // deletes a whole value set with all versions
        ValueSetVersion vsv = (ValueSetVersion) data;

        request.getDeleteInfo().setType(Type.VALUE_SET_VERSION);
        request.getDeleteInfo().setValueSet(new types.termserver.fhdo.de.ValueSet());
        request.getDeleteInfo().getValueSet().setId(vsv.getValueSet().getId());

        types.termserver.fhdo.de.ValueSetVersion vsv_ws = new types.termserver.fhdo.de.ValueSetVersion();
        vsv_ws.setVersionId(vsv.getVersionId());
        request.getDeleteInfo().getValueSet().getValueSetVersions().add(vsv_ws);
      }
      else if (data instanceof CodeSystemVersion)
      {
        // deletes a whole code system with all versions
        CodeSystemVersion csv = (CodeSystemVersion) data;

        request.getDeleteInfo().setType(Type.CODE_SYSTEM_VERSION);
        request.getDeleteInfo().setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
        request.getDeleteInfo().getCodeSystem().setId(csv.getCodeSystem().getId());
        types.termserver.fhdo.de.CodeSystemVersion csv_ws = new types.termserver.fhdo.de.CodeSystemVersion();
        csv_ws.setVersionId(csv.getVersionId());
        request.getDeleteInfo().getCodeSystem().getCodeSystemVersions().add(csv_ws);
      }

      // do webservice call
      RemoveTerminologyOrConceptResponseType response = WebServiceHelper.removeTerminologyOrConcept(request);
      logger.debug("reponse: " + response.getReturnInfos().getMessage());

      Messagebox.show(response.getReturnInfos().getMessage());
      //initList();
      //initListVersion();
      SessionHelper.setValue("CS_SelectedItem", null);
      SessionHelper.getValue("CS_SelectedItemVersion", null);
      SessionHelper.setValue("VS_SelectedItem", null);
      SessionHelper.getValue("VS_SelectedItemVersion", null);
      Executions.sendRedirect(null);
    }
  }

  public void onSelected(String id, Object data)
  {
    if (data != null)
      logger.debug("selected item type: " + data.getClass().getCanonicalName());

    if (id == null || id.equals("list"))
    {
      selectedItem = data;
      SessionHelper.setValue("CS_SelectedItem", data);

      selectedItemVersion = null;
      SessionHelper.setValue("CS_SelectedItemVersion", selectedItemVersion);

      initListVersion();
    }
    else if (id.equals("listVersion"))
    {
      selectedItemVersion = data;
      SessionHelper.setValue("CS_SelectedItemVersion", data);

      initDetails();
    }
  }

  public void showNameInputDialog()
  {
    try
    {
      Map map = new HashMap();

      Window win = (Window) Executions.createComponents(
              "/gui/templates/nameInputbox.zul", null, map);

      ((NameInputbox) win).setiUpdateListener(this);

      win.doModal();

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  private void createNewCodeSystem(String name)
  {
    if (name.length() > 0)
    {
      // create new code system without a version
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        hb_session.getTransaction().begin();

        if (mode == Mode.VALUESET)
        {
          ValueSet newVS = new ValueSet();
          newVS.setName(name);
          newVS.setStatusDate(new Date());

          hb_session.save(newVS);
          selectedItem = newVS;
        }
        else
        {
          CodeSystem newCS = new CodeSystem();
          newCS.setName(name);
          newCS.setInsertTimestamp(new Date());

          hb_session.save(newCS);
          selectedItem = newCS;
        }

        hb_session.getTransaction().commit();

        selectedItemVersion = null;

        SessionHelper.setValue("CS_SelectedItem", selectedItem);
        SessionHelper.setValue("CS_SelectedItemVersion", selectedItemVersion);
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
      finally
      {
        hb_session.close();
      }

      // refresh list view
      initList();
    }
  }

  private void createNewCodeSystemVersion(String versionName)
  {
    if (versionName.length() > 0)
    {
      // create new code system without a version
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        hb_session.getTransaction().begin();

        if (mode == Mode.VALUESET)
        {
          ValueSet selectedVS = (ValueSet) selectedItem;
          ValueSet vs_db = (ValueSet) hb_session.get(ValueSet.class, selectedVS.getId());

          ValueSetVersion newVSV = new ValueSetVersion();
          newVSV.setName(versionName);
          newVSV.setInsertTimestamp(new Date());
          newVSV.setValueSet(selectedVS);

          newVSV.setStatusDate(new Date());
          newVSV.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());

          newVSV.setPreviousVersionId(vs_db.getCurrentVersionId());

          hb_session.save(newVSV);
          selectedItemVersion = newVSV;

          vs_db.setCurrentVersionId(newVSV.getVersionId());
          hb_session.update(vs_db);

          // update cache
          selectedItem = vs_db;
          SessionHelper.setValue("CS_SelectedItem", selectedItem);
        }
        else //if (mode == Mode.CODESYSTEM)
        {
          CodeSystem selectedCS = (CodeSystem) selectedItem;

          CodeSystem cs_db = (CodeSystem) hb_session.get(CodeSystem.class, selectedCS.getId());

          CodeSystemVersion newCSV = new CodeSystemVersion();
          newCSV.setName(versionName);
          newCSV.setInsertTimestamp(new Date());
          newCSV.setLastChangeDate(new Date());
          newCSV.setStatusDate(new Date());
          newCSV.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
          newCSV.setCodeSystem(selectedCS);
          newCSV.setPreviousVersionId(cs_db.getCurrentVersionId());

          hb_session.save(newCSV);
          selectedItemVersion = newCSV;

          cs_db.setCurrentVersionId(newCSV.getVersionId());
          hb_session.update(cs_db);

          // update cache
          selectedItem = cs_db;
          SessionHelper.setValue("CS_SelectedItem", selectedItem);
        }

        hb_session.getTransaction().commit();

        SessionHelper.setValue("CS_SelectedItemVersion", selectedItemVersion);
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
      finally
      {
        hb_session.close();
      }

      // refresh list view
      initListVersion();
    }
  }

  public void changeStatus()
  {
    CodeSystemVersion selectedCSV = null;

    Object obj = genericListVersion.getSelection();

    if (obj != null && obj instanceof de.fhdo.list.GenericListRowType)
    {
      logger.debug("obj: " + obj.getClass().getCanonicalName());
      selectedCSV = (CodeSystemVersion) ((de.fhdo.list.GenericListRowType) obj).getData();
    }

    if (selectedCSV == null)
    {
      Messagebox.show(Labels.getLabel("selectCodesystemMsg"));
    }
    else
    {
      logger.debug("Change status from csv-id: " + selectedCSV.getVersionId());

      try
      {
        Map map = new HashMap();
        map.put("csv", selectedCSV);

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/codesystemStatus.zul", null, map);

        ((CodesystemStatus) win).setiUpdateListener(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
    }

  }

//  public void initTree()
//  {
//    logger.debug("initTree()");
//
//    // Header
//    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
//    header.add(new GenericTreeHeaderType("ID", 100, "", true, "String", false, false, false));
//    header.add(new GenericTreeHeaderType(Labels.getLabel("codesystem") + " - " + Labels.getLabel("name"), 300, "", true, "String", false, false, false));
//    header.add(new GenericTreeHeaderType("OID", 170, "", true, "String", false, false, false));
//    header.add(new GenericTreeHeaderType(Labels.getLabel("inserted"), 100, "", true, "DateTime", false, false, false));
//    header.add(new GenericTreeHeaderType(Labels.getLabel("status"), 100, "", true, "String", false, false, false));
//    header.add(new GenericTreeHeaderType(Labels.getLabel("since"), 100, "", true, "DateTime", false, false, false));
//
//    List<GenericTreeRowType> dataList = new LinkedList<GenericTreeRowType>();
//
//    try
//    {
//
//      // Hibernate-Block, Session öffnen
//      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
//
//      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
//      {
//        String hql = "select distinct cs from CodeSystem cs left join fetch cs.codeSystemVersions csv order by cs.name,csv.name";
//
//        List<CodeSystem> csList = hb_session.createQuery(hql).list();
//
//        logger.debug("csList: " + csList.size());
//
//        for (CodeSystem codeSystem : csList)
//        {
//          GenericTreeRowType row = createTreeRow(codeSystem);
//          dataList.add(row);
//
//          row.setChildRows(new LinkedList<GenericTreeRowType>());
//
//          for (CodeSystemVersion codeSystemVersion : codeSystem.getCodeSystemVersions())
//          {
//            row.getChildRows().add(createTreeRow(codeSystemVersion));
//          }
//        }
//
//        Include inc = (Include) getFellow("incTree");
//        Window winGenericTree = (Window) inc.getFellow("winGenericTree");
//
//        genericTree = (GenericTree) winGenericTree;
//    //genericListValues.setUserDefinedId("2");
//
//        //genericTree.setTreeActions(this);
//        //genericTree.setUpdateDataListener(this);
//        genericTree.setButton_new(false);
//        genericTree.setButton_edit(false);
//        genericTree.setButton_delete(false);
//        genericTree.setListHeader(header);
//        genericTree.setDataList(dataList);
//
//        Button buttonNew = new Button(Labels.getLabel("newCodesystem") + " (" + Labels.getLabel("withoutVersion") + ")...");
//        buttonNew.setAttribute("disabled", false);
//        buttonNew.addEventListener(Events.ON_CLICK, new EventListener<Event>()
//        {
//          public void onEvent(Event t) throws Exception
//          {
//            newCodeSystem();
//          }
//        });
//
//        Button button = new Button(Labels.getLabel("changeStatus") + "...");
//        button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
//        {
//          public void onEvent(Event t) throws Exception
//          {
//            changeStatus();
//          }
//        });
//
//        genericTree.removeCustomButtons();
//        genericTree.addCustomButton(buttonNew);
//        genericTree.addCustomButton(button);
//      }
//      catch (Exception e)
//      {
//        LoggingOutput.outputException(e, this);
//      }
//      finally
//      {
//        hb_session.close();
//      }
//    }
//    catch (Exception e)
//    {
//      LoggingOutput.outputException(e, this);
//    }
//  }
//
//  private GenericTreeRowType createTreeRow(Object object)
//  {
//    GenericTreeRowType row = new GenericTreeRowType(null);
//
//    /*header.add(new GenericTreeHeaderType("ID", 60, "", true, "String", false, false, false));
//     header.add(new GenericTreeHeaderType("Name Codesystem/Version", 300, "", true, "String", false, false, false));
//     header.add(new GenericTreeHeaderType("OID", 180, "", true, "String", false, false, false));
//     header.add(new GenericTreeHeaderType("Eingefügt", 100, "", true, "DateTime", false, false, false));
//     header.add(new GenericTreeHeaderType("Status", 100, "", true, "String", false, false, false));
//     header.add(new GenericTreeHeaderType("Status seit", 100, "", true, "DateTime", false, false, false));*/
//    GenericTreeCellType[] cells = new GenericTreeCellType[6];
//
//    //logger.debug("createTreeRow with type: " + object.getClass().getCanonicalName());
//    if (object instanceof CodeSystem)
//    {
//      CodeSystem cs = (CodeSystem) object;
//      cells[0] = new GenericTreeCellType(cs.getId(), false, "");
//      cells[1] = new GenericTreeCellType(cs.getName(), false, "");
//      cells[2] = new GenericTreeCellType("", false, "");
//      cells[3] = new GenericTreeCellType(cs.getInsertTimestamp(), false, "");
//      cells[4] = new GenericTreeCellType("-", false, "");
//      cells[5] = new GenericTreeCellType("-", false, "");
//
//    }
//    else if (object instanceof CodeSystemVersion)
//    {
//      CodeSystemVersion csv = (CodeSystemVersion) object;
//
//      cells[0] = new GenericTreeCellType(csv.getVersionId(), false, "");
//      cells[1] = new GenericTreeCellType(csv.getName(), false, "");
//      cells[2] = new GenericTreeCellType(csv.getOid(), false, "");
//      cells[3] = new GenericTreeCellType(csv.getInsertTimestamp(), false, "");
//      cells[4] = new GenericTreeCellType(Definitions.STATUS_CODES.readLabel(csv.getStatus()), false, "");
//      cells[5] = new GenericTreeCellType(csv.getStatusDate(), false, "");
//    }
//
//    row.setData(object);
//    row.setCells(cells);
//
//    return row;
//  }
//
//
//
//  public void update(Object o, boolean edited)
//  {
//    if(o == null)
//      return;
//    
//    // update status in db
//    if (o instanceof CodeSystemVersion)
//    {
//      CodeSystemVersion csv = (CodeSystemVersion) o;
//
//      // Hibernate-Block, Session öffnen
//      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
//
//      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
//      {
//        Transaction tx = hb_session.beginTransaction();
//
//        CodeSystemVersion csv_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, csv.getVersionId());
//        csv_db.setStatus(csv.getStatus());
//
//        hb_session.update(csv_db);
//        tx.commit();
//
//        // show changes in tree view
//        GenericTreeRowType row = createTreeRow(csv);
//        genericTree.updateEntry(row);
//      }
//      catch (Exception e)
//      {
//        LoggingOutput.outputException(e, this);
//      }
//      finally
//      {
//        hb_session.close();
//      }
//
//    }
//
//    if (o instanceof String)
//    {
//      String newCodesystemStr = (String) o;
//
//      if (newCodesystemStr.length() > 0)
//      {
//        // create new code system without a version
//        Session hb_session = HibernateUtil.getSessionFactory().openSession();
//
//        try
//        {
//          hb_session.getTransaction().begin();
//          
//          CodeSystem newCS = new CodeSystem();
//          newCS.setName(newCodesystemStr);
//          newCS.setInsertTimestamp(new Date());
//          
//          hb_session.save(newCS);
//
//          hb_session.getTransaction().commit();
//        }
//        catch (Exception ex)
//        {
//          LoggingOutput.outputException(ex, this);
//        }
//        finally
//        {
//          hb_session.close();
//        }
//
//        // refresh tree view
//        initTree();
//      }
//    }
//  }
}
