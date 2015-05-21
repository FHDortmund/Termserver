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
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Benutzer extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  
  public Benutzer()
  {
    logger.debug("Benutzer() Konstruktor");
  }

  public void afterCompose()
  {
    initList();
  }
  
  private GenericListRowType createRowFromBenutzer(Collaborationuser user)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[6];
    cells[0] = new GenericListCellType(user.getUsername(), false, "");
    cells[1] = new GenericListCellType(user.getEnabled() != null ? user.getEnabled() : false, false, "");
    cells[2] = new GenericListCellType(user.getName(), false, "");
    cells[3] = new GenericListCellType(user.getFirstName(), false, "");
    cells[4] = new GenericListCellType(user.getEmail(), false, "");
    cells[5] = new GenericListCellType(user.getActivated() != null ? user.getActivated() : false, false, "");

    row.setData(user);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Benutzername", 200, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Aktiv", 60, "", true, "boolean", true, true, false, true));
    header.add(new GenericListHeaderType("Name", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Vorname", 150, "", true, "String", true, true, false, false));
    //header.add(new GenericListHeaderType("Admin", 50, "", true, "boolean", true, true, false, true));
    header.add(new GenericListHeaderType("Email", 300, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Mail-Aktiviert", 100, "", true, "boolean", true, true, false, true));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      hb_session.flush();
      
      //String hql = "from Collaborationuser where hidden=0 AND deleted=0 order by name";
      String hql = "from Collaborationuser order by name";
      logger.debug("hql: " + hql);
      
      List<Collaborationuser> personList = hb_session.createQuery(hql).list();
      
      logger.debug("size: " + personList.size());
      

      for (int i = 0; i < personList.size(); ++i)
      {
        Collaborationuser user = personList.get(i);
        GenericListRowType row = createRowFromBenutzer(user);

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
    
    ((Button)genericList.getFellow("buttonDelete")).setVisible(true);
  }
  
  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked(): " + id);

    try
    {
        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/benutzerDetails.zul", null, null);

        ((BenutzerDetails) win).setUpdateListInterface(this);
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

    if (data != null && data instanceof Collaborationuser)
    {
      Collaborationuser user = (Collaborationuser) data;

      try
      {
        Map map = new HashMap();
        map.put("user_id", user.getId());


        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/benutzerDetails.zul", null, map);
        ((BenutzerDetails) win).setUpdateListInterface(this);

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

    if (data != null && data instanceof Collaborationuser)
    {
      Collaborationuser user = (Collaborationuser) data;
      logger.debug("User deaktivieren: " + user.getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        Collaborationuser user_db = (Collaborationuser) hb_session.get(Collaborationuser.class, user.getId());

        user_db.getRoles().clear();
        user_db.getDiscussiongroups().clear();
        user_db.getPrivileges().clear();
        user_db.setUsername("");
        user_db.setPassword("");
        user_db.setSalt("");
        user_db.setCity("");
        user_db.setCountry("");
        user_db.setEmail("");
        user_db.setNote("");
        user_db.setPhone("");
        user_db.setStreet("");
        user_db.setTitle("");
        user_db.setZip("");
        user_db.setSendMail(false);
        user_db.setActivated(false);
        user_db.setActivationTime(null);
        user_db.setActivationMd5("");
        user_db.setHidden(false);
        user_db.setEnabled(false);
        user_db.setDeleted(true);
        hb_session.update(user_db);
        
        //Check for userAssignment
        Query q = hb_session.createQuery("from Discussiongroup WHERE head= :p_head");
        q.setLong("p_head", user_db.getId());
        List<Discussiongroup> dgList = q.list();
        
        for(Discussiongroup dg:dgList){
            
            Discussiongroup dg_db = (Discussiongroup)hb_session.get(Discussiongroup.class, dg.getId());
            dg.getCollaborationusers().clear();
            dg.getPrivileges().clear();
            dg.getDiscussiongroupobjects().clear();
            hb_session.delete(dg_db);
        }
        
        hb_session.getTransaction().commit();

        Messagebox.show("Benutzer wurde erfolgreich gelöscht.", "Benutzer löschen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        
        Messagebox.show("Fehler beim Löschen des Benutzers: " + e.getLocalizedMessage(), "Benutzer löschen", Messagebox.OK, Messagebox.EXCLAMATION);
        initList();
      }
      finally
      {
        hb_session.close();
      }
    }
  }

  public void onSelected(String id, Object data)
  {
    
  }

  public void update(Object o, boolean edited)
  {
    if (o instanceof Collaborationuser)
    {
      // Daten aktualisiert, jetzt dem Model übergeben
      Collaborationuser user = (Collaborationuser) o;

      GenericListRowType row = createRowFromBenutzer(user);

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
