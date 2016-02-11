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
import de.fhdo.terminologie.db.hibernate.LicencedUser;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IUpdateData;
import java.util.HashMap;
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
public class Lizenzen extends Window implements AfterCompose, IGenericListActions, IUpdateData, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private GenericList genericList;
  private GenericList genericListLizenzen;
  private TermUser selectedUser;

  public Lizenzen()
  {


    if (SessionHelper.isAdmin() == false)
    {
      Executions.getCurrent().sendRedirect("/gui/admin/admin.zul");
    }
  }

  public void afterCompose()
  {
    initList();
  }

  private GenericListRowType createRowFromUser(de.fhdo.terminologie.db.hibernate.TermUser user)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(user.getName(), false, "");
    cells[1] = new GenericListCellType(user.getUserName(), false, "");

    row.setData(user);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("username"), 120, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("name"), 0, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from TermUser tu order by tu.name";
      List<de.fhdo.terminologie.db.hibernate.TermUser> userList = hb_session.createQuery(hql).list();

      for (int i = 0; i < userList.size(); ++i)
      {
        de.fhdo.terminologie.db.hibernate.TermUser user = userList.get(i);
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
    genericList.setListId("user");

    genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
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
      Map map = new HashMap();
      map.put("user_id", selectedUser.getId());
          
      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/terminology/user/lizenzDetails.zul", null, map);

      ((LizenzDetails) win).setUpdateListInterface(this);
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der LizenzDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");

    if (id != null && id.equals("lizenzen"))
    {
      if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.LicencedUser)
      {
        de.fhdo.terminologie.db.hibernate.LicencedUser lu = (de.fhdo.terminologie.db.hibernate.LicencedUser) data;

        try
        {
          Map map = new HashMap();
          map.put("licenceduser_id", lu.getId());
          map.put("csv_id", lu.getId().getCodeSystemVersionId());
          map.put("user_id", lu.getId().getUserId());


          Window win = (Window) Executions.createComponents(
                  "/gui/admin/modules/terminology/user/lizenzDetails.zul", null, map);

          ((LizenzDetails) win).setUpdateListInterface(this);

          win.doModal();
        }
        catch (Exception ex)
        {
          logger.debug("Fehler beim Öffnen der LizenzDetails: " + ex.getLocalizedMessage());
          ex.printStackTrace();
        }
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.LicencedUser)
    {
      de.fhdo.terminologie.db.hibernate.LicencedUser lu = (de.fhdo.terminologie.db.hibernate.LicencedUser) data;
      logger.debug("LU löschen: " + lu.getId());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.terminologie.db.hibernate.LicencedUser lu_db = (de.fhdo.terminologie.db.hibernate.LicencedUser) hb_session.get(de.fhdo.terminologie.db.hibernate.LicencedUser.class, lu.getId());

        // LU löschen
        lu_db.setCodeSystemVersion(null);
        lu_db.setLicenceType(null);
        lu_db.setTermUser(null);

        hb_session.delete(lu_db);
        
        hb_session.getTransaction().commit();

        Messagebox.show(Labels.getLabel("userLicenseDeleteSuccess"), Labels.getLabel("delete"), Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();

        Messagebox.show(Labels.getLabel("userLicenseDeleteFailure") + ": " + e.getLocalizedMessage(), Labels.getLabel("delete"), Messagebox.OK, Messagebox.EXCLAMATION);
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
    if (id != null && id.equals("user"))
    {
      if (data != null && data instanceof TermUser)
      {
        selectedUser = (TermUser) data;

        logger.debug("Benutzer ausgewählt: " + selectedUser.getId());

        initListLizenzen();
      }
    }
  }

  public void onCellUpdated(int cellIndex, Object data, GenericTreeRowType row)
  {
  }

  private GenericListRowType createRowFromLicencedUser(de.fhdo.terminologie.db.hibernate.LicencedUser lu)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[5];
    cells[0] = new GenericListCellType(lu.getCodeSystemVersion().getCodeSystem().getName(), false, "");
    cells[1] = new GenericListCellType(lu.getCodeSystemVersion().getName(), false, "");
    cells[2] = new GenericListCellType(lu.getValidFrom(), false, "");
    cells[3] = new GenericListCellType(lu.getValidTo(), false, "");
    if (lu.getLicenceType() != null)
      cells[4] = new GenericListCellType(lu.getLicenceType().getTypeTxt(), false, "");
    else
      cells[4] = new GenericListCellType("generelle Lizenz", false, "");

    row.setData(lu);
    row.setCells(cells);

    return row;
  }

  private void initListLizenzen()
  {
    logger.debug("initListLizenzen()");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("codesystem"), 220, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("codesystemVersion"), 220, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("from"), 80, "", true, "Date", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("to"), 80, "", true, "Date", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("licenseType"), 160, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from LicencedUser lu left join fetch lu.licenceType join fetch lu.codeSystemVersion csv join fetch csv.codeSystem cs";
      hql += " where lu.termUser.id=" + selectedUser.getId();
      hql += " order by cs.name,csv.name";

      logger.debug("HQL: " + hql);

      List<de.fhdo.terminologie.db.hibernate.LicencedUser> luList = hb_session.createQuery(hql).list();

      for (int i = 0; i < luList.size(); ++i)
      {
        de.fhdo.terminologie.db.hibernate.LicencedUser lu = luList.get(i);
        GenericListRowType row = createRowFromLicencedUser(lu);

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
    Include inc = (Include) getFellow("incListLizenzen");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericListLizenzen = (GenericList) winGenericList;
    genericListLizenzen.setListId("lizenzen");

    genericListLizenzen.setListActions(this);
    genericListLizenzen.setButton_new(true);
    genericListLizenzen.setButton_edit(true);
    genericListLizenzen.setButton_delete(true);
    genericListLizenzen.setListHeader(header);
    genericListLizenzen.setDataList(dataList);



    //genericList.setDataList(null);
    //genericList.set


  }

  public void update(Object o, boolean edited)
  {
    if(o != null && o instanceof LicencedUser)
    {
      LicencedUser lu = (LicencedUser)o;
      GenericListRowType row = createRowFromLicencedUser(lu);
      
      if(edited)
        genericListLizenzen.updateEntry(row);
      else genericListLizenzen.addEntry(row);
      
    }
    
  }
}
