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
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class Enquiry extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  
  public Enquiry()
  {
    
  }

  public void afterCompose()
  {
    initList();
  }
  
  private GenericListRowType createRowFromEnquiry(de.fhdo.collaboration.db.classes.Enquiry enquiry)
  {
    GenericListRowType row = new GenericListRowType();

    
    GenericListCellType[] cells = new GenericListCellType[5];
    cells[0] = new GenericListCellType(enquiry.getCollaborationuser().getFirstName(), false, "");
    cells[1] = new GenericListCellType(enquiry.getCollaborationuser().getName(), false, "");
    cells[2] = new GenericListCellType(enquiry.getCollaborationuser().getOrganisation().getOrganisation(), false, "");
    cells[3] = new GenericListCellType(enquiry.getRequestType(), false, "");
    String status = "";
    if(!enquiry.getClosedFlag()){
        status = "Offen";
    }else{
        status = "Geschlossen";
    }
    cells[4] = new GenericListCellType(status, false, "");

    row.setData(enquiry);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    
    header.add(new GenericListHeaderType("Vorname", 150, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Nachname", 250, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Organisation", 270, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Anfrage Typ", 450, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Status", 100, "", true, "String", true, true, false, false));
    
    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from Enquiry order by requestType";
      List<de.fhdo.collaboration.db.classes.Enquiry> enquiryList = hb_session.createQuery(hql).list();

      for (int i = 0; i < enquiryList.size(); ++i)
      {
        de.fhdo.collaboration.db.classes.Enquiry enquiry = enquiryList.get(i);
        GenericListRowType row = createRowFromEnquiry(enquiry);

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
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
    
    ((Button)genericList.getFellow("buttonNew")).setVisible(false);
  }
  
  
  //Button wird nicht verwendet!
  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked(): " + id); //Out of Order
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");

    if (data != null && data instanceof de.fhdo.collaboration.db.classes.Enquiry)
    {
      de.fhdo.collaboration.db.classes.Enquiry enquiry = (de.fhdo.collaboration.db.classes.Enquiry) data;

      try
      {
        Map map = new HashMap();
        map.put("enquiry_id", enquiry.getId());


        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/anfrageDetails.zul", null, map);
        ((EnquiryDetails) win).setUpdateListInterface(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der EnquiryDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data) //Not used
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.collaboration.db.classes.Enquiry)
    {
      de.fhdo.collaboration.db.classes.Enquiry enquiry = (de.fhdo.collaboration.db.classes.Enquiry) data;
      logger.debug("Enquiry löschen: " + enquiry.getId());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.collaboration.db.classes.Enquiry enquiry_db = (de.fhdo.collaboration.db.classes.Enquiry) hb_session.get(de.fhdo.collaboration.db.classes.Enquiry.class, enquiry.getId());

        hb_session.delete(enquiry_db);

        if(enquiry_db.getCollaborationuser().getHidden()){
          hb_session.delete(enquiry_db.getCollaborationuser());
          hb_session.delete(enquiry_db.getCollaborationuser().getOrganisation());
        }
        
        if(enquiry_db.getCollaborationuserExtPerson() != null && enquiry_db.getCollaborationuserExtPerson().getHidden()){
             hb_session.delete(enquiry_db.getCollaborationuserExtPerson());
             
             if(enquiry_db.getCollaborationuserExtPerson().getOrganisation() != null){
                hb_session.delete(enquiry_db.getCollaborationuserExtPerson().getOrganisation());
             }  
        }
       
        hb_session.getTransaction().commit();

        Messagebox.show("Anfrage wurde erfolgreich gelöscht.", "Anfrage löschen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        
        Messagebox.show("Fehler beim Löschen der Anfrage: " + e.getLocalizedMessage(), "Anfrage löschen", Messagebox.OK, Messagebox.EXCLAMATION);
        initList();
      }finally{
        hb_session.close();
      }
    }
  }

  public void onSelected(String id, Object data)
  {
    
  }

  public void update(Object o, boolean edited)
  {
    if (o instanceof de.fhdo.collaboration.db.classes.Enquiry)
    {
      // Daten aktualisiert, jetzt dem Model übergeben
      de.fhdo.collaboration.db.classes.Enquiry enquiry = (de.fhdo.collaboration.db.classes.Enquiry) o;

      GenericListRowType row = createRowFromEnquiry(enquiry);

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
