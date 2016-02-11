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

import de.fhdo.collaboration.db.Definitions;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.DomainHelper;
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
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class SysParam extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;

  public SysParam()
  {
    logger.debug("SysParam-Konstruktor");

    if (SessionHelper.isAdmin() == false)
    {
      Executions.getCurrent().sendRedirect("/gui/main/main.zul");
    }
  }

  private GenericListRowType createRowFromSysParam(de.fhdo.collaboration.db.classes.SysParam sysParam)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[6];
    cells[0] = new GenericListCellType(sysParam.getName(), false, "");
    cells[1] = new GenericListCellType(sysParam.getDomainValueByValidityDomain().getDisplayText(), false, "");
    cells[2] = new GenericListCellType(sysParam.getDomainValueByModifyLevel().getDisplayText(), false, "");
    cells[3] = new GenericListCellType(sysParam.getJavaDatatype(), false, "");
    cells[4] = new GenericListCellType(sysParam.getValue(), false, "");
    cells[5] = new GenericListCellType(sysParam.getDescription(), false, "");

    row.setData(sysParam);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    String[] filter = DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_VALIDITYDOMAIN);

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("name"), 230, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("validityRange"), 130, "", true, filter, true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("modifyLevel"), 130, "", true, filter, true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("datatype"), 80, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("value"), 700, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("description"), 400, "", true, "String", true, true, false, false));
    
    
    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from SysParam p join fetch p.domainValueByModifyLevel join fetch p.domainValueByValidityDomain where p.objectId is null order by p.name";
      List<de.fhdo.collaboration.db.classes.SysParam> paramList = hb_session.createQuery(hql).list();

      for (int i = 0; i < paramList.size(); ++i)
      {
        de.fhdo.collaboration.db.classes.SysParam sysParam = paramList.get(i);
        GenericListRowType row = createRowFromSysParam(sysParam);

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
  }

  public void afterCompose()
  {
    initList();
  }

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked()");
    //throw new UnsupportedOperationException("Not supported yet.");

    try
    {
      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/collaboration/sysParamDetails.zul", null, null);

      ((SysParamDetails) win).setiUpdateListener(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der SysParamDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }

  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");
    //throw new UnsupportedOperationException("Not supported yet.");
    if (data != null && data instanceof de.fhdo.collaboration.db.classes.SysParam)
    {
      de.fhdo.collaboration.db.classes.SysParam sysParam = (de.fhdo.collaboration.db.classes.SysParam) data;
      logger.debug("Parameter: " + sysParam.getName());

      try
      {
        Map map = new HashMap();
        map.put("sysparam_id", sysParam.getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/collaboration/sysParamDetails.zul", null, map);

        ((SysParamDetails) win).setiUpdateListener(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der SysParamDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.collaboration.db.classes.SysParam)
    {
      de.fhdo.collaboration.db.classes.SysParam sysParam = (de.fhdo.collaboration.db.classes.SysParam) data;
      logger.debug("Person: " + sysParam.getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.collaboration.db.classes.SysParam sysParamDB = (de.fhdo.collaboration.db.classes.SysParam) hb_session.get(de.fhdo.collaboration.db.classes.SysParam.class, sysParam.getId());
        hb_session.delete(sysParamDB);

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler beim Löschen eines Eintrags: " + e.getMessage());
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
    if (o != null && o instanceof de.fhdo.collaboration.db.classes.SysParam)
    {
      // Hier wird die neue Zeile erstellt und der Liste übergeben
      // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
      de.fhdo.collaboration.db.classes.SysParam sysParam = (de.fhdo.collaboration.db.classes.SysParam) o;
      GenericListRowType row = createRowFromSysParam(sysParam);
        
      if (edited)
      {
        // Daten aktualisiert, jetzt dem Model übergeben
        logger.debug("Daten aktualisiert: " + sysParam.getName());
        
        genericList.updateEntry(row);
      }
      else
      {
        genericList.addEntry(row);
      }
    }
  }
}
