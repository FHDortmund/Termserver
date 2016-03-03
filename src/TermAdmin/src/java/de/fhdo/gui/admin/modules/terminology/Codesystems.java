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
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeCellType;
import de.fhdo.tree.GenericTreeHeaderType;
import de.fhdo.tree.GenericTreeRowType;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author rober
 */
public class Codesystems extends Window implements AfterCompose, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private GenericTree genericTree;

  public Codesystems()
  {
  }

  public void afterCompose()
  {
    initTree();
  }

  public void initTree()
  {
    logger.debug("initTree()");

    // Header
    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
    header.add(new GenericTreeHeaderType("ID", 100, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType(Labels.getLabel("codesystem") + " - " + Labels.getLabel("name"), 300, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType("OID", 170, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType(Labels.getLabel("inserted"), 100, "", true, "DateTime", false, false, false));
    header.add(new GenericTreeHeaderType(Labels.getLabel("status"), 100, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType(Labels.getLabel("since"), 100, "", true, "DateTime", false, false, false));

    List<GenericTreeRowType> dataList = new LinkedList<GenericTreeRowType>();

    try
    {

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        String hql = "select distinct cs from CodeSystem cs left join fetch cs.codeSystemVersions csv order by cs.name,csv.name";

        List<CodeSystem> csList = hb_session.createQuery(hql).list();

        logger.debug("csList: " + csList.size());

        for (CodeSystem codeSystem : csList)
        {
          GenericTreeRowType row = createTreeRow(codeSystem);
          dataList.add(row);

          row.setChildRows(new LinkedList<GenericTreeRowType>());

          for (CodeSystemVersion codeSystemVersion : codeSystem.getCodeSystemVersions())
          {
            row.getChildRows().add(createTreeRow(codeSystemVersion));
          }
        }

        Include inc = (Include) getFellow("incTree");
        Window winGenericTree = (Window) inc.getFellow("winGenericTree");

        genericTree = (GenericTree) winGenericTree;
    //genericListValues.setUserDefinedId("2");

        //genericTree.setTreeActions(this);
        //genericTree.setUpdateDataListener(this);
        genericTree.setButton_new(false);
        genericTree.setButton_edit(false);
        genericTree.setButton_delete(false);
        genericTree.setListHeader(header);
        genericTree.setDataList(dataList);

        Button buttonNew = new Button(Labels.getLabel("newCodesystem") + "(" + Labels.getLabel("withoutVersion") + "...");
        buttonNew.setAttribute("disabled", false);
        buttonNew.addEventListener(Events.ON_CLICK, new EventListener<Event>()
        {
          public void onEvent(Event t) throws Exception
          {
            newCodeSystem();
          }
        });

        Button button = new Button(Labels.getLabel("changeStatus") + "...");
        button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
        {
          public void onEvent(Event t) throws Exception
          {
            changeStatus();
          }
        });

        genericTree.removeCustomButtons();
        genericTree.addCustomButton(buttonNew);
        genericTree.addCustomButton(button);
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  private GenericTreeRowType createTreeRow(Object object)
  {
    GenericTreeRowType row = new GenericTreeRowType(null);

    /*header.add(new GenericTreeHeaderType("ID", 60, "", true, "String", false, false, false));
     header.add(new GenericTreeHeaderType("Name Codesystem/Version", 300, "", true, "String", false, false, false));
     header.add(new GenericTreeHeaderType("OID", 180, "", true, "String", false, false, false));
     header.add(new GenericTreeHeaderType("Eingefügt", 100, "", true, "DateTime", false, false, false));
     header.add(new GenericTreeHeaderType("Status", 100, "", true, "String", false, false, false));
     header.add(new GenericTreeHeaderType("Status seit", 100, "", true, "DateTime", false, false, false));*/
    GenericTreeCellType[] cells = new GenericTreeCellType[6];

    //logger.debug("createTreeRow with type: " + object.getClass().getCanonicalName());
    if (object instanceof CodeSystem)
    {
      CodeSystem cs = (CodeSystem) object;
      cells[0] = new GenericTreeCellType(cs.getId(), false, "");
      cells[1] = new GenericTreeCellType(cs.getName(), false, "");
      cells[2] = new GenericTreeCellType("", false, "");
      cells[3] = new GenericTreeCellType(cs.getInsertTimestamp(), false, "");
      cells[4] = new GenericTreeCellType("-", false, "");
      cells[5] = new GenericTreeCellType("-", false, "");

    }
    else if (object instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) object;

      cells[0] = new GenericTreeCellType(csv.getVersionId(), false, "");
      cells[1] = new GenericTreeCellType(csv.getName(), false, "");
      cells[2] = new GenericTreeCellType(csv.getOid(), false, "");
      cells[3] = new GenericTreeCellType(csv.getInsertTimestamp(), false, "");
      cells[4] = new GenericTreeCellType(Definitions.STATUS_CODES.readLabel(csv.getStatus()), false, "");
      cells[5] = new GenericTreeCellType(csv.getStatusDate(), false, "");
    }

    row.setData(object);
    row.setCells(cells);

    return row;
  }

  public void newCodeSystem()
  {
    try
    {
      Map map = new HashMap();
      //map.put("csv", selectedCSV);

      Window win = (Window) Executions.createComponents(
              "/gui/templates/nameInputbox.zul", null, map);

      ((NameInputbox) win).setiUpdateListener(this);

      win.doModal();

    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der SysParamDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  public void changeStatus()
  {
    CodeSystemVersion selectedCSV = null;

    Object obj = genericTree.getSelection();
    if (obj != null && obj instanceof GenericTreeRowType)
    {
      GenericTreeRowType row = (GenericTreeRowType) obj;
      if (row.getData() instanceof CodeSystemVersion)
        selectedCSV = (CodeSystemVersion) row.getData();
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
        logger.debug("Fehler beim Öffnen der SysParamDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }

  }

  public void update(Object o, boolean edited)
  {
    if(o == null)
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
        GenericTreeRowType row = createTreeRow(csv);
        genericTree.updateEntry(row);
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

    }

    if (o instanceof String)
    {
      String newCodesystemStr = (String) o;

      if (newCodesystemStr.length() > 0)
      {
        // create new code system without a version
        Session hb_session = HibernateUtil.getSessionFactory().openSession();

        try
        {
          hb_session.getTransaction().begin();
          
          CodeSystem newCS = new CodeSystem();
          newCS.setName(newCodesystemStr);
          newCS.setInsertTimestamp(new Date());
          
          hb_session.save(newCS);

          hb_session.getTransaction().commit();
        }
        catch (Exception ex)
        {
          LoggingOutput.outputException(ex, this);
        }
        finally
        {
          hb_session.close();
        }

        // refresh tree view
        initTree();
      }
    }
  }

}
