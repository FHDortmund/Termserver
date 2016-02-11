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
package de.fhdo.gui.admin.modules.terminology.user;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.helper.CollabUserHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class User extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  
  public User()
  {
    if (SessionHelper.isAdmin() == false)
    {
      Executions.getCurrent().sendRedirect("/gui/main/main.zul");
    }
  }

  public void afterCompose()
  {
    initList();
  }
  
  private GenericListRowType createRowFromUser(de.fhdo.terminologie.db.hibernate.TermUser user)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[6];
    cells[0] = new GenericListCellType(user.getName(), false, "");
    cells[1] = new GenericListCellType(user.getEnabled() != null ? user.getEnabled() : false, false, "");
    cells[2] = new GenericListCellType(user.isIsAdmin(), false, "");
    cells[3] = new GenericListCellType(user.getUserName(), false, "");
    cells[4] = new GenericListCellType(user.getEmail(), false, "");
    cells[5] = new GenericListCellType(user.getActivationTime() != null, false, "");
    

    row.setData(user);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("username"), 200, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("active"), 60, "", true, "boolean", true, true, false, true));
    header.add(new GenericListHeaderType(Labels.getLabel("administrator"), 60, "", true, "boolean", true, true, false, true));
    header.add(new GenericListHeaderType(Labels.getLabel("name"), 250, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("mail"), 300, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("mailActivated"), 100, "", true, "boolean", true, true, false, true));
    

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from TermUser order by name";
      List<de.fhdo.terminologie.db.hibernate.TermUser> personList = hb_session.createQuery(hql).list();

      for (int i = 0; i < personList.size(); ++i)
      {
        de.fhdo.terminologie.db.hibernate.TermUser user = personList.get(i);
        GenericListRowType row = createRowFromUser(user);

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

    //genericList.setUserDefinedId("1");
    genericList.setListActions(this);
    genericList.setButton_new(true);
    genericList.setButton_edit(true);
    genericList.setButton_delete(true);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);



    //genericList.setDataList(null);
    //genericList.set


  }

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked(): " + id);

    try
    {
        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/user/userDetails.zul", null, null);

        ((UserDetails) win).setUpdateListInterface(this);
        win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der UserDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");

    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.TermUser)
    {
      de.fhdo.terminologie.db.hibernate.TermUser user = (de.fhdo.terminologie.db.hibernate.TermUser) data;

      try
      {
        Map map = new HashMap();
        map.put("user_id", user.getId());


        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/user/userDetails.zul", null, map);

        ((UserDetails) win).setUpdateListInterface(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der UserDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.TermUser)
    {
      de.fhdo.terminologie.db.hibernate.TermUser user = (de.fhdo.terminologie.db.hibernate.TermUser) data;
      logger.debug("User löschen: " + user.getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.terminologie.db.hibernate.TermUser user_db = (de.fhdo.terminologie.db.hibernate.TermUser) hb_session.get(de.fhdo.terminologie.db.hibernate.TermUser.class, user.getId());

        Iterator<de.fhdo.terminologie.db.hibernate.Session> itSession = user_db.getSessions().iterator();
        while (itSession.hasNext())
        {
          de.fhdo.terminologie.db.hibernate.Session obj = itSession.next();
          hb_session.delete(obj);
        }
        Iterator<de.fhdo.terminologie.db.hibernate.LicencedUser> itLUser = user_db.getLicencedUsers().iterator();
        while (itLUser.hasNext())
        {
          de.fhdo.terminologie.db.hibernate.LicencedUser obj = itLUser.next();
          hb_session.delete(obj);
        }

        hb_session.delete(user_db);

        hb_session.getTransaction().commit();

        Messagebox.show(Labels.getLabel("userDeletedMsg"), Labels.getLabel("delete"), Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        
        Messagebox.show(Labels.getLabel("deleteUserFailure") + ": " + e.getLocalizedMessage(), Labels.getLabel("delete"), Messagebox.OK, Messagebox.EXCLAMATION);
        initList();
      }
      hb_session.close();
      CollabUserHelper.reloadModel();
    }
  }

  public void onSelected(String id, Object data)
  {
    
  }

  public void update(Object o, boolean edited)
  {
    if (o instanceof de.fhdo.terminologie.db.hibernate.TermUser)
    {
      // Daten aktualisiert, jetzt dem Model übergeben
      de.fhdo.terminologie.db.hibernate.TermUser user = (de.fhdo.terminologie.db.hibernate.TermUser) o;

      GenericListRowType row = createRowFromUser(user);

      if (edited)
      {
        // Hier wird die neue Zeile erstellt und der Liste übergeben
        // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
        genericList.updateEntry(row);
      }
      else
      {
        genericList.addEntry(row);
      }
    }
  }
  
  
  
}
