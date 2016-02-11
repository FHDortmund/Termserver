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
import de.fhdo.collaboration.db.classes.DomainValue;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeCellType;
import de.fhdo.tree.GenericTreeHeaderType;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IGenericTreeActions;
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
public class Domain extends Window implements AfterCompose, IGenericListActions, IUpdateModal, IGenericTreeActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  private GenericTree genericTree;
  //GenericList genericListValues;

  public Domain()
  {
    logger.debug("Domain-Konstruktor");

    if (SessionHelper.isAdmin() == false)
    {
      Executions.getCurrent().sendRedirect("/gui/main/main.zul");
    }
  }

  private GenericListRowType createRowFromPerson(de.fhdo.collaboration.db.classes.Domain user)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[1];
    cells[0] = new GenericListCellType(user.getName(), false, "");

    row.setData(user);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("domain"), 0, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from Domain order by name";
      List<de.fhdo.collaboration.db.classes.Domain> personList = hb_session.createQuery(hql).list();

      for (int i = 0; i < personList.size(); ++i)
      {
        de.fhdo.collaboration.db.classes.Domain user = personList.get(i);
        GenericListRowType row = createRowFromPerson(user);

        dataList.add(row);
      }

      //tx.commit();
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

  private GenericListRowType createRowFromDomainValue(de.fhdo.collaboration.db.classes.DomainValue domainValue)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[5];
    cells[0] = new GenericListCellType(domainValue.getId(), false, "");
    cells[1] = new GenericListCellType(domainValue.getCode(), false, "");
    cells[2] = new GenericListCellType(domainValue.getDisplayText(), false, "");
    cells[3] = new GenericListCellType(domainValue.getOrderNo(), false, "");
    cells[4] = new GenericListCellType(domainValue.getImageFile(), false, "");

    row.setData(domainValue);
    row.setCells(cells);

    return row;
  }

  private GenericTreeRowType createTreeRowFromDomainValue(de.fhdo.collaboration.db.classes.DomainValue domainValue)
  {
    GenericTreeRowType row = new GenericTreeRowType(null);

    GenericTreeCellType[] cells = new GenericTreeCellType[5];
    cells[0] = new GenericTreeCellType(domainValue.getId(), false, "");
    cells[1] = new GenericTreeCellType(domainValue.getCode(), false, "");
    cells[2] = new GenericTreeCellType(domainValue.getDisplayText(), false, "");
    cells[3] = new GenericTreeCellType(domainValue.getOrderNo(), false, "");
    cells[4] = new GenericTreeCellType(domainValue.getImageFile(), false, "");

    row.setData(domainValue);
    row.setCells(cells);

    if (domainValue.getDomainValuesForDomainValueIdChild() != null)
    {
      Iterator<DomainValue> dvIt = domainValue.getDomainValuesForDomainValueIdChild().iterator();

      while (dvIt.hasNext())
      {
        DomainValue dvChild = dvIt.next();
        row.getChildRows().add(createTreeRowFromDomainValue(dvChild));
      }
    }

    return row;
  }

  /*private void initDomainValueList(long DomainID)
   {
   logger.debug("initDomainValueList: " + DomainID);

   // Header
   List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
   header.add(new GenericListHeaderType("ID", 80, "", true, "long", true, true, false, false));
   header.add(new GenericListHeaderType("Code", 140, "", true, "String", true, true, false, false));
   header.add(new GenericListHeaderType("Anzeige-Text", 250, "", true, "String", true, true, false, false));
   header.add(new GenericListHeaderType("Order-Nr", 70, "", false, "int", true, true, false, false));
   header.add(new GenericListHeaderType("Bild", 250, "", true, "String", true, true, false, false));

   // Daten laden
   Session hb_session = HibernateUtil.getSessionFactory().openSession();
   //org.hibernate.Transaction tx = hb_session.beginTransaction();

   List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
   try
   {
   String hql = "from DomainValue where domainId=" + DomainID + " order by orderNo,domainDisplay";
   List<de.fhdo.db.hibernate.DomainValue> dvList = hb_session.createQuery(hql).list();

   for (int i = 0; i < dvList.size(); ++i)
   {
   de.fhdo.db.hibernate.DomainValue domainValue = dvList.get(i);
   GenericListRowType row = createRowFromDomainValue(domainValue);

   dataList.add(row);
   logger.debug("DV: " + domainValue.getDomainCode());
   }

   //tx.commit();
   }
   catch (Exception e)
   {
   logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initDomainValueList(): " + e.getMessage());
   }
   finally
   {
   HibernateUtil.getSessionFactory().close();
   }

   // Liste initialisieren
   Include inc = (Include) getFellow("incListValues");
   Window winGenericList = (Window) inc.getFellow("winGenericList");

   genericListValues = (GenericList) winGenericList;
   //genericListValues.setUserDefinedId("2");

   genericListValues.setListActions(this);
   genericListValues.setButton_new(true);
   genericListValues.setButton_edit(true);
   genericListValues.setButton_delete(true);
   genericListValues.setListHeader(header);
   genericListValues.setDataList(dataList);


   }*/
  private void initDomainValueTree(long DomainID)
  {
    logger.debug("initDomainValueTree: " + DomainID);

    // Header
    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
    header.add(new GenericTreeHeaderType("ID", 80, "", true, "long", false, false, false));
    header.add(new GenericTreeHeaderType("Code", 140, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType("Anzeige-Text", 250, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType("Order-Nr", 70, "", false, "int", false, false, false));
    header.add(new GenericTreeHeaderType("Bild", 250, "", true, "String", false, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericTreeRowType> dataList = new LinkedList<GenericTreeRowType>();
    try
    {
      String hql = "from DomainValue dv join fetch dv.domain d where d.id=" + DomainID + " order by dv.orderNo,dv.displayText";
      List<de.fhdo.collaboration.db.classes.DomainValue> dvList = hb_session.createQuery(hql).list();

      for (int i = 0; i < dvList.size(); ++i)
      {
        de.fhdo.collaboration.db.classes.DomainValue domainValue = dvList.get(i);

        if (domainValue.getDomainValuesForDomainValueIdParent() == null
                || domainValue.getDomainValuesForDomainValueIdChild().size() == 0)
        {
          // Nur root-Elemente auf oberster Ebene
          GenericTreeRowType row = createTreeRowFromDomainValue(domainValue);

          dataList.add(row);
          //logger.debug("DV: " + domainValue.getDomainCode());
        }
        //else
        //  logger.debug("kein Child DV: " + domainValue.getDomainCode() + ", " + domainValue.getDomainValuesForDomainValueId1().size());

      }

      //tx.commit();
    }
    catch (Exception e)
    {
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initDomainValueList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incTree");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");

    genericTree = (GenericTree) winGenericTree;
    //genericListValues.setUserDefinedId("2");

    genericTree.setTreeActions(this);
    genericTree.setButton_new(true);
    genericTree.setButton_edit(true);
    genericTree.setButton_delete(true);
    genericTree.setListHeader(header);
    genericTree.setDataList(dataList);

  }

  public void afterCompose()
  {
    initList();
    showDomainValueList();
  }

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked(): " + id);

    try
    {
      //if (id.equals("1"))
      {
        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/domainDetails.zul", null, null);

        ((DomainDetails) win).setUpdateListInterface(this);
        win.doModal();
      }
      /*else
       {
       //logger.debug("initDomainValueList: " + DomainID);
       Object o = SessionHelper.getValue("termadmin_domainId");
       if (o != null)
       {
       logger.debug("domainValueDetails mit id: " + o.toString());
       Map map = new HashMap();
       map.put("domain_id", o);

       Window win = (Window) Executions.createComponents(
       "/gui/admin/modules/domainValueDetails.zul", null, map);

       ((DomainValueDetails) win).setUpdateListInterface(this);
       win.doModal();
       }
       else
       {
       logger.warn("termadmin_domainId ist null");
       }
       }*/
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der DomainDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");

    if (data != null && data instanceof de.fhdo.collaboration.db.classes.Domain)
    {
      de.fhdo.collaboration.db.classes.Domain domain = (de.fhdo.collaboration.db.classes.Domain) data;
      //logger.debug("Person: " + user.getName());

      try
      {
        Map map = new HashMap();
        map.put("domain_id", domain.getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/domainDetails.zul", null, map);

        ((DomainDetails) win).setUpdateListInterface(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der DomainDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
    else if (data != null && data instanceof de.fhdo.collaboration.db.classes.DomainValue)
    {
      de.fhdo.collaboration.db.classes.DomainValue domainValue = (de.fhdo.collaboration.db.classes.DomainValue) data;

      try
      {
        Map map = new HashMap();
        //map.put("domainValue_id", domainValue.getDomainValueId());
        map.put("domain_value", domainValue);
        map.put("domain_id", domainValue.getDomain().getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/domainValueDetails.zul", null, map);

        ((DomainValueDetails) win).setUpdateListInterface(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der Domain(Value)Details: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.collaboration.db.classes.Domain)
    {
      de.fhdo.collaboration.db.classes.Domain domain = (de.fhdo.collaboration.db.classes.Domain) data;
      logger.debug("Domain löschen: " + domain.getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.collaboration.db.classes.Domain domain_db = (de.fhdo.collaboration.db.classes.Domain) hb_session.get(de.fhdo.collaboration.db.classes.Domain.class, domain.getId());

        Iterator<de.fhdo.collaboration.db.classes.DomainValue> itSession = domain_db.getDomainValues().iterator();
        while (itSession.hasNext())
        {
          de.fhdo.collaboration.db.classes.DomainValue dv = itSession.next();
          hb_session.delete(dv);
        }

        hb_session.delete(domain_db);

        hb_session.getTransaction().commit();

        Messagebox.show(Labels.getLabel("domainDeleteSuccess"), Labels.getLabel("deleteDomain"), Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();

        Messagebox.show(Labels.getLabel("domainDeleteFailure") + ": " + e.getLocalizedMessage(), Labels.getLabel("deleteDomain"), Messagebox.OK, Messagebox.EXCLAMATION);
        initList();
      }
      finally
      {
        hb_session.close();
      }
    }
    else if (data != null && data instanceof de.fhdo.collaboration.db.classes.DomainValue)
    {
      de.fhdo.collaboration.db.classes.DomainValue domainValue = (de.fhdo.collaboration.db.classes.DomainValue) data;
      logger.debug("DomainValue löschen: " + domainValue.getCode());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.collaboration.db.classes.DomainValue domain_db = (de.fhdo.collaboration.db.classes.DomainValue) hb_session.get(de.fhdo.collaboration.db.classes.DomainValue.class, domainValue.getId());

        /*Iterator<de.fhdo.db.hibernate.DomainValue> itSession = domain_db.getDomainValuesForDomainValueId2().iterator();
         while (itSession.hasNext())
         {
         de.fhdo.db.hibernate.DomainValue dv = itSession.next();
         hb_session.delete(dv);
         }*/
        // Erst alle darunterliegenden löschen, dann Beziehungen löschen (?)
        hb_session.delete(domain_db);

        hb_session.getTransaction().commit();

        //Messagebox.show("Domain wurde erfolgreich gelöscht.", "Domain löschen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();

        Messagebox.show(Labels.getLabel("domainValueDeleteFailure") + ": " + e.getLocalizedMessage(), Labels.getLabel("deleteDomain"), Messagebox.OK, Messagebox.EXCLAMATION);
        showDomainValueList();
      }
      finally
      {
        hb_session.close();
      }
    }
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  public void update(Object o, boolean edited)
  {
    if (o instanceof de.fhdo.collaboration.db.classes.Domain)
    {
      // Daten aktualisiert, jetzt dem Model übergeben
      de.fhdo.collaboration.db.classes.Domain person = (de.fhdo.collaboration.db.classes.Domain) o;

      GenericListRowType row = createRowFromPerson(person);

      if (edited)
      {
        //logger.debug("Daten aktualisiert: " + person.getName());

        // Hier wird die neue Zeile erstellt und der Liste übergeben
        // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
        genericList.updateEntry(row);
      }
      else
      {
        //logger.debug("Daten hinzugefügt: " + person.getName());

        genericList.addEntry(row);
      }
    }
    else if (o instanceof de.fhdo.collaboration.db.classes.DomainValue)
    {
      logger.debug("update DomainValue");
      // Daten aktualisiert, jetzt dem Model übergeben
      de.fhdo.collaboration.db.classes.DomainValue dv = (de.fhdo.collaboration.db.classes.DomainValue) o;

      GenericTreeRowType row = createTreeRowFromDomainValue(dv);
      if (edited)
      {
        genericTree.updateEntry(row);
      }
      else
      {
        genericTree.addEntry(row, dv.getDomainValuesForDomainValueIdParent() == null && dv.getDomainValuesForDomainValueIdParent().size() == 0);
      }

    }
  }

  private void showDomainValueList()
  {
    long domainId = 0;
    Object o = SessionHelper.getValue("termadmin_domainId");
    if (o != null)
    {
      domainId = Long.parseLong(o.toString());
    }

    int index = -1;
    o = SessionHelper.getValue("termadmin_domain_index");
    if (o != null)
    {
      index = Integer.parseInt(o.toString());
    }

    if (index >= 0)
    {
      //TODO genericList.setSelectedIndex(index);
      genericList.setSelectedIndex(index);
    }

    //Div div = (Div)getFellow("noDataDiv");
    //Include inc = (Include) getFellow("incListValues");
    //div.setVisible(domainId == 0);
    //inc.setVisible(domainId != 0);
    //initDomainValueList(domainId);
    initDomainValueTree(domainId);
  }

  public void onSelected(String id, Object data)
  {
    logger.debug("OnSelected: " + data.getClass().getCanonicalName());

    if (data instanceof de.fhdo.collaboration.db.classes.Domain)
    {
      de.fhdo.collaboration.db.classes.Domain domain = (de.fhdo.collaboration.db.classes.Domain) data;
      SessionHelper.setValue("termadmin_domainId", domain.getId());
      int index = genericList.getListbox().getSelectedIndex();
      SessionHelper.setValue("termadmin_domain_index", index);
      showDomainValueList();
    }
  }

  public void onTreeNewClicked(String id, Object data)
  {
    Object o = SessionHelper.getValue("termadmin_domainId");
    if (o != null)
    {
      logger.debug("domainValueDetails mit id: " + o.toString());
      Map map = new HashMap();
      map.put("domain_id", o);

      if (data != null)
      {
        // Subeintrag
        DomainValue dv = (DomainValue) data;
        map.put("domain_value_id", dv.getId());
      }

      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/collaboration/domainValueDetails.zul", null, map);

      ((DomainValueDetails) win).setUpdateListInterface(this);
      win.doModal();
    }
    else
    {
      logger.warn("termadmin_domainId ist null");
    }

  }

  public void onTreeEditClicked(String id, Object data)
  {
    de.fhdo.collaboration.db.classes.DomainValue domainValue = (de.fhdo.collaboration.db.classes.DomainValue) data;

    try
    {
      Map map = new HashMap();
      map.put("domain_value", domainValue);
      map.put("domain_id", domainValue.getDomain().getId());

      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/collaboration/domainValueDetails.zul", null, map);

      ((DomainValueDetails) win).setUpdateListInterface(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der Domain(Value)Details: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  private void deleteDomainChilds(de.fhdo.collaboration.db.classes.DomainValue domainValue, Session hb_session)
  {
    logger.debug("deleteDomainChilds()");
    if (domainValue.getDomainValuesForDomainValueIdChild() != null)
    {
      logger.debug("deleteDomainChilds(), Anzahl: " + domainValue.getDomainValuesForDomainValueIdChild().size());

      Iterator<DomainValue> dvIt = domainValue.getDomainValuesForDomainValueIdChild().iterator();

      while (dvIt.hasNext())
      {
        DomainValue dvChild = dvIt.next();
        deleteDomainChilds(dvChild, hb_session);

        logger.debug("Lösche code: " + dvChild.getCode());
        dvChild.setDomainValuesForDomainValueIdParent(null);
        hb_session.delete(dvChild);
      }
    }
  }

  public boolean onTreeDeleted(String id, Object data)
  {
    de.fhdo.collaboration.db.classes.DomainValue domainValue = (de.fhdo.collaboration.db.classes.DomainValue) data;
    logger.debug("DomainValue löschen (Tree): " + domainValue.getCode());

    // Person aus der Datenbank löschen
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      de.fhdo.collaboration.db.classes.DomainValue domain_db = (de.fhdo.collaboration.db.classes.DomainValue) hb_session.get(de.fhdo.collaboration.db.classes.DomainValue.class, domainValue.getId());

      deleteDomainChilds(domain_db, hb_session);

      logger.debug("Lösche code: " + domain_db.getCode());
      domain_db.setDomainValuesForDomainValueIdParent(null);
      hb_session.delete(domain_db);

      hb_session.getTransaction().commit();

      //Messagebox.show("Domain wurde erfolgreich gelöscht.", "Domain löschen", Messagebox.OK, Messagebox.INFORMATION);
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();

      Messagebox.show(Labels.getLabel("") + ": " + e.getLocalizedMessage(), Labels.getLabel("deleteDomain"), Messagebox.OK, Messagebox.EXCLAMATION);
      showDomainValueList();
    }
    finally
    {
      hb_session.close();
    }
    return true;
  }

  public void onTreeSelected(String id, Object data)
  {
  }
}
