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
package de.fhdo.collaboration.discgroup;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.helper.SessionHelper;
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
import org.zkoss.zul.East;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class DiscGroupMain extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  GenericList genericListR;
  West leadingId;
  East participatingId;
  
  public DiscGroupMain()
  {
    
  }
  
  public void afterCompose()
  {
    if((SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN) || 
         SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER))){
          ((West) getFellow("leadingId")).setVisible(true);
          ((East) getFellow("participatingId")).setVisible(true);
          ((East) getFellow("participatingId")).setSize("50%");
      }else{
          ((West) getFellow("leadingId")).setVisible(false);
          ((East) getFellow("participatingId")).setVisible(true);
          ((East) getFellow("participatingId")).setSize("100%");
      }
    initList();
  }
  
  private GenericListRowType createRowFromGroup(DiscGroupData dgd)
  {        
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[3];
        cells[0] = new GenericListCellType(dgd.getGroup().getName(), false, "");
        cells[1] = new GenericListCellType(dgd.getHeadOfGroup().getFirstName() + " "  + dgd.getHeadOfGroup().getName(), false, "");
        cells[2] = new GenericListCellType(dgd.getHeadOfGroup().getEmail(), false, "");
        
        row.setData(dgd);
        row.setCells(cells);

        return row;
  }

  private void initList()
  {
    
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Diskussionsgruppe", 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Gruppenleiter", 200, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Gruppenleiter-eMail", 250, "", true, "String", true, true, false, false));
    
    // Daten laden
    Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
    //hb_session_kollab.getTransaction().begin();
    

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      boolean found = false;
      String hqlU = "from Collaborationuser where id=:p_id";
      Query qU = hb_session_kollab.createQuery(hqlU);
      qU.setParameter("p_id",SessionHelper.getCollaborationUserID());
      List<Collaborationuser> userList = qU.list();

      if(userList.size() == 1){
         found = true;
      }
      
      String hql = "select distinct dg from Discussiongroup dg join fetch dg.collaborationusers u group by dg";
      Query q = hb_session_kollab.createQuery(hql);
      List<Discussiongroup> groupList = q.list();
      
      for (int i = 0; i < groupList.size(); ++i)
      {

        Discussiongroup group = groupList.get(i);
        
        //Head of group
        if(found && group.getHead().equals(userList.get(0).getId())){
        
            DiscGroupData dgd = new DiscGroupData();
            dgd.setGroup(group);
            dgd.setHeadOfGroup(userList.get(0));   

            GenericListRowType row = createRowFromGroup(dgd);
            dataList.add(row);
        }
      }
    //hb_session_kollab.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session_kollab.getTransaction().rollback();
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session_kollab.close();
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
    
    ((Button)genericList.getFellow("buttonDelete")).setLabel("Löschen...");
    initListReadOnly();
  }
  
  private void initListReadOnly(){
  
      // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Diskussionsgruppe", 0, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Gruppenleiter", 200, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Gruppenleiter-eMail", 250, "", true, "String", true, true, false, false));

        // Daten laden
        Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();


        List<GenericListRowType> dataListR = new LinkedList<GenericListRowType>();
        try
        {

          String hql = "from Discussiongroup";
          Query q = hb_session_kollab.createQuery(hql);
          List<Discussiongroup> groupList = q.list();

          for (int i = 0; i < groupList.size(); ++i)
          {

            Discussiongroup group = groupList.get(i);

            //Participating in group
            for(Collaborationuser u:group.getCollaborationusers()){
                if(u.getId().equals(SessionHelper.getCollaborationUserID())){

                    DiscGroupData dgd = new DiscGroupData();
                    dgd.setGroup(group);

                    String hqlH = "from Collaborationuser where id=:p_id";
                    Query qH = hb_session_kollab.createQuery(hqlH);
                    qH.setParameter("p_id",group.getHead());
                    List<Collaborationuser> userListH = qH.list();

                    if(userListH.size() == 1){
                       dgd.setHeadOfGroup(userListH.get(0));
                       GenericListRowType row = createRowFromGroup(dgd);
                        dataListR.add(row);

                    }
                }    
            }
          }
        //hb_session_kollab.getTransaction().commit();
        }
        catch (Exception e)
        {
          //hb_session_kollab.getTransaction().rollback();
            logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
        }
        finally
        {
          hb_session_kollab.close();
        }
      
      //ReadOnlyListe
      // Liste initialisieren
      Include incR = (Include) getFellow("incListR");
      Window winGenericListR = (Window) incR.getFellow("winGenericList");
      genericListR = (GenericList) winGenericListR;

      genericListR.setListActions(new ListenerReadOnlyGroups(this));
      genericListR.setButton_new(false);
      genericListR.setButton_edit(false);
      genericListR.setButton_delete(false);
      genericListR.setListHeader(header);
      genericListR.setDataList(dataListR);
  }

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked(): " + id);

    try
    {
        Window win = (Window) Executions.createComponents(
                "/collaboration/discgroup/discGroupDetails.zul", null, null);

        ((DiscGroupDetails) win).setUpdateListInterface(this);
        win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der DiscGroupDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");

    if (data != null && data instanceof DiscGroupData)
    {
      DiscGroupData dgd = (DiscGroupData) data;

      try
      {
        Map map = new HashMap();
        map.put("dgd", dgd);

        Window win = (Window) Executions.createComponents(
                "/collaboration/discgroup/discGroupDetails.zul", null, map);

        ((DiscGroupDetails) win).setUpdateListInterface(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der DiscGroupDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof DiscGroupData)
    {
      DiscGroupData dgd = (DiscGroupData) data;
      logger.debug("Gruppe löschen: " + dgd.getGroup().getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        Discussiongroup group_db = (Discussiongroup) hb_session.get(Discussiongroup.class, dgd.getGroup().getId());
        
        for(Privilege priv:group_db.getPrivileges()){
            priv.setDiscussiongroup(null);
            hb_session.update(priv);
        }
        
        group_db.getCollaborationusers().clear();
        hb_session.delete(group_db);

        hb_session.getTransaction().commit();
        
        Messagebox.show("Gruppe wurde erfolgreich gelöscht.", "Gruppe löschen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        
        Messagebox.show("Fehler beim Löschen der Gruppe: " + e.getLocalizedMessage(), "Gruppe löschen", Messagebox.OK, Messagebox.EXCLAMATION);
        initList();
      }
      hb_session.close();
    }
    initListReadOnly();
  }

  public void onSelected(String id, Object data)
  {
  }

  public void update(Object o, boolean edited)
  {
    if (o instanceof DiscGroupData)
    {
        // Daten aktualisiert, jetzt dem Model übergeben
        DiscGroupData dgd = (DiscGroupData) o;
        
        GenericListRowType row = createRowFromGroup(dgd);

        if (edited)
        {
            // Hier wird die neue Zeile erstellt und der Liste übergeben
            // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
            genericList.updateEntry(row);
        }
        else
        { 
            genericList.addEntry(row);
            initListReadOnly();
        }
    }else{
        initListReadOnly();
    }
  }
}
