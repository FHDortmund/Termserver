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
package de.fhdo.gui.admin.modules.collaboration;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.collaboration.db.classes.Status;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Workflow extends Window implements AfterCompose, IGenericListActions, IUpdateModal, IUpdate
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;

  public Workflow()
  {
  }

  public void afterCompose()
  {
    initList();
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Start", 40, "", true, "bool", true, true, false, true));
    header.add(new GenericListHeaderType("Ende", 40, "", true, "bool", true, true, false, true));
    header.add(new GenericListHeaderType("Status 'Von'", 170, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Status 'Zu'", 170, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Aktion", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Rollen", 240, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      //String hql = "select distinct s from Status s left join fetch s.statusrelsForStatusIdTo rel order by s.status";
      //List<Status> statusList = hb_session.createQuery(hql).list();
      String hql = "select distinct rel from Statusrel rel"
              + " join fetch rel.statusByStatusIdFrom s1"
              + " join fetch rel.statusByStatusIdTo s2"
              + " left join fetch rel.action"
              + " order by s1.status";
      List<Statusrel> statusList = hb_session.createQuery(hql).list();

      for (Statusrel status : statusList)
      {
        GenericListRowType row = createRow(status, hb_session);
        dataList.add(row);
      }
    }
    catch (Exception e)
    {
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(true);
    genericList.setButton_edit(true);
    genericList.setButton_delete(true);

    // Custom-Button
    Button buttonRollen = new Button("Rollen zuordnen...");

    buttonRollen.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        RollenZuordnen();
      }
    });

    genericList.removeCustomButtons();
    genericList.addCustomButton(buttonRollen);


    genericList.setListHeader(header);
    genericList.setDataList(dataList);
  }

  private GenericListRowType createRow(Statusrel rel, Session hb_session)
  {
    GenericListRowType row = new GenericListRowType();

    /*Status statusTo = null;
     if(status.getStatusrelsForStatusIdTo() != null && status.getStatusrelsForStatusIdTo().size() > 0)
     {
     for(Statusrel rel : status.getStatusrelsForStatusIdTo())
     {
        
     }
     }*/

    String sAction = rel.getAction().getAction();
    String sRoles = "";

    for (Role role : rel.getRoles())
    {
      if (sRoles.length() > 0)
        sRoles += "\n";
      sRoles += role.getName();
    }

    logger.debug("Action: " + sAction);
    logger.debug("Roles: " + sRoles);

    Listcell lcRoles = new Listcell();
    Label label = new Label(sRoles);
    label.setMultiline(true);
    lcRoles.appendChild(label);

    // Startzustand / Endzustand
    boolean start = false, ende = false;

    if (hb_session != null)
    {
      Status sFrom = (Status) hb_session.get(Status.class, rel.getStatusByStatusIdFrom().getId());
      start = sFrom.getStatusrelsForStatusIdTo().isEmpty();

      Status sTo = (Status) hb_session.get(Status.class, rel.getStatusByStatusIdTo().getId());
      ende = sTo.getStatusrelsForStatusIdFrom().isEmpty();
    }


    GenericListCellType[] cells = new GenericListCellType[6];
    //cells[0] = new GenericListCellType(rel.getStatusByStatusIdFrom().getStatusrelsForStatusIdTo().size() == 0, false, "");
    //cells[1] = new GenericListCellType(rel.getStatusByStatusIdTo().getStatusrelsForStatusIdFrom().size() == 0, false, "");
    cells[0] = new GenericListCellType(start, false, "");
    cells[1] = new GenericListCellType(ende, false, "");

    cells[2] = new GenericListCellType(rel.getStatusByStatusIdFrom().getStatus(), false, "");
    cells[3] = new GenericListCellType(rel.getStatusByStatusIdTo().getStatus(), false, "");
    cells[4] = new GenericListCellType(sAction, false, "");
    cells[5] = new GenericListCellType(lcRoles, false, "");

    row.setData(rel);
    row.setCells(cells);

    return row;
  }

  private void RollenZuordnen()
  {
    logger.debug("Rollen zuordnen");
    try
    {
      Object o = genericList.getSelection();
      if (o != null)
      {
        GenericListRowType row = (GenericListRowType) o;
        Statusrel rel = (Statusrel) row.getData();

        Map map = new HashMap();
        map.put("statusrel_id", rel.getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/workflowAssignRoleDetails.zul", null, map);
        ((WorkflowAssignRoleDetails) win).setUpdateListener(this);

        win.doModal();
      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  private void showStatusRelDetails(long id)
  {
    try
    {
      Map map = new HashMap();
      map.put("statusrel_id", id);

      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/collaboration/workflowDetails.zul", null, map);
      ((WorkflowDetails) win).setUpdateListInterface(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  public void onNewClicked(String id)
  {
    showStatusRelDetails(0);
  }

  public void onEditClicked(String id, Object data)
  {
    if (data != null && data instanceof Statusrel)
    {
      Statusrel sRel = (Statusrel) data;
      showStatusRelDetails(sRel.getId());
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof Statusrel)
    {
      Statusrel sRel = (Statusrel) data;

      // Beziehung aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        Statusrel sRel_db = (Statusrel) hb_session.get(Statusrel.class, sRel.getId());

        sRel_db.setAction(null);
        sRel_db.setRoles(null);
        sRel_db.setStatusByStatusIdFrom(null);
        sRel_db.setStatusByStatusIdTo(null);

        hb_session.delete(sRel_db);
        hb_session.getTransaction().commit();

        //Messagebox.show("Benutzer wurde erfolgreich deaktiviert.", "Benutzer löschen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();

        Messagebox.show("Fehler beim Löschen eines Workflow-Elements: " + e.getLocalizedMessage(), "Löschen", Messagebox.OK, Messagebox.EXCLAMATION);
      }
      finally
      {
        hb_session.close();
      }
      initList();
    }
  }

  public void onSelected(String id, Object data)
  {
  }

  public void update(Object o, boolean edited)
  {
    // Immer Liste neu laden, da Abhängigkeiten in der Liste existieren
    initList();
    
    
    /*if (o instanceof UpdateWorkflowType)
     {
     UpdateWorkflowType uType = (UpdateWorkflowType) o;

     GenericListRowType row = createRow((Statusrel) uType.getO(), uType.getHb_session());

     if (edited)
     genericList.updateEntry(row);
     else
     {
     //initList(); // Neu laden, sonst Lazy-Loading-Problem
     genericList.addEntry(row);
     }
     }*/
    /*if (o instanceof Statusrel)
     {
     // Daten aktualisiert, jetzt dem Model übergeben
     Statusrel sRel = (Statusrel) o;

     GenericListRowType row = createRow(sRel);

     if (edited)
     genericList.updateEntry(row);
     else
     {
     //initList(); // Neu laden, sonst Lazy-Loading-Problem
     genericList.addEntry(row);
     }
     }*/
  }

  public void update(Object o)
  {
    initList();
  }

  public static class UpdateWorkflowType
  {

    private Object o;
    private Session hb_session;

    public UpdateWorkflowType()
    {
    }

    /**
     * @return the o
     */
    public Object getO()
    {
      return o;
    }

    /**
     * @param o the o to set
     */
    public void setO(Object o)
    {
      this.o = o;
    }

    /**
     * @return the hb_session
     */
    public Session getHb_session()
    {
      return hb_session;
    }

    /**
     * @param hb_session the hb_session to set
     */
    public void setHb_session(Session hb_session)
    {
      this.hb_session = hb_session;
    }
  }
}
