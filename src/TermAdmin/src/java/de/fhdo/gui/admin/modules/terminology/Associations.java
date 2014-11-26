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

import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Cache;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.South;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Associations extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;

  public Associations()
  {
  }

  public void afterCompose()
  {
    initList();
  }

  private GenericListRowType createRowFromAssociation(de.fhdo.terminologie.db.hibernate.AssociationType association)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[4];
    cells[0] = new GenericListCellType(association.getCodeSystemEntityVersionId(), false, "");
    cells[1] = new GenericListCellType(association.getForwardName(), false, "");
    cells[2] = new GenericListCellType(association.getReverseName(), false, "");

    String cs = "";
    for (CodeSystemVersionEntityMembership csvem : association.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships())
    {
      String s = csvem.getCodeSystemVersion().getCodeSystem().getName() + " - " + csvem.getCodeSystemVersion().getName();
      if (cs.length() > 0)
        cs = cs + ";\n";
      cs += s;
    }

    cells[3] = new GenericListCellType(cs, false, "");

    row.setData(association);
    row.setCells(cells);

    return row;
  }

  /*@PersistenceContext
   private EntityManager em;

   public void clearHibernateCache()
   {
   Session s = (Session) em.getDelegate();
   SessionFactory sf = s.getSessionFactory();

   Map classMetadata = sf.getAllClassMetadata();
   for (Object ep : classMetadata.values())
   {
   if (((EntityPersister)ep).hasCache())
   {
   sf.evictEntity(((EntityPersister)ep).getCache().getRegionName());
   }
   }

   Map collMetadata = sf.getAllCollectionMetadata();
   for (Object acp : collMetadata.values())
   {
   if (((AbstractCollectionPersister)acp).hasCache())
   {
   sf.evictCollection(((AbstractCollectionPersister)acp).getCache().getRegionName());
   }
   }

   return;
   }*/
  private void initList()
  {
    logger.debug("initList()");
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("ID", 90, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Name vorwärts", 250, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Name rückwärts", 250, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Codesystem", 350, "", true, "String", true, true, false, false));

    //CacheManager.getInstance().clearAll();
    //clearHibernateCache();
    // Daten laden
    //SessionFactory sf = HibernateUtil.getSessionFactory();
    SessionFactory sf = HibernateUtil.getNewSessionFactory();
    

    //sf.evictQueries();
    //sf.getCurrentSession().clear();
    /*Cache cache = sf.getCache();
    
    cache.evictEntityRegions();
    cache.evictCollectionRegions();
    cache.evictDefaultQueryRegion();
    cache.evictQueryRegions();
    //cache.evictNaturalIdRegions();
    //cache.evictAllRegions();

    cache.evictAllRegions(); // Evict data from all query regions.*/
    
    Session hb_session = sf.openSession();
    
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "select distinct at from AssociationType at"
              + " join fetch at.codeSystemEntityVersion csev"
              + " join csev.codeSystemEntity cse"
              + " left join cse.codeSystemVersionEntityMemberships csvem";
      //+ " left join fetch csvem.codeSystemVersion csv"
      //+ " left join fetch csv.codeSystem";

      Query q = hb_session.createQuery(hql);
      q.setCacheable(false);
      q.setCacheMode(CacheMode.IGNORE);
       //q.setHint("toplink.refresh", "true");

      hb_session.setCacheMode(CacheMode.IGNORE);
      hb_session.clear();
      hb_session.flush();

      List<de.fhdo.terminologie.db.hibernate.AssociationType> list = q.list();

      logger.debug("Anzahl: " + list.size());

      for (int i = 0; i < list.size(); ++i)
      {
        de.fhdo.terminologie.db.hibernate.AssociationType association = list.get(i);
        GenericListRowType row = createRowFromAssociation(association);

        dataList.add(row);
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

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked()");

    try
    {
      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/terminology/associationDetails.zul", null, null);

      ((AssociationDetails) win).setiUpdateListener(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");

    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.AssociationType)
    {
      de.fhdo.terminologie.db.hibernate.AssociationType assType = (de.fhdo.terminologie.db.hibernate.AssociationType) data;
      logger.debug("Parameter: " + assType.getForwardName());

      try
      {
        Map map = new HashMap();
        map.put("id", assType.getCodeSystemEntityVersionId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/associationDetails.zul", null, map);

        ((AssociationDetails) win).setiUpdateListener(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.AssociationType)
    {
      de.fhdo.terminologie.db.hibernate.AssociationType assType = (de.fhdo.terminologie.db.hibernate.AssociationType) data;

      // aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        long csev_id = assType.getCodeSystemEntityVersionId();
        de.fhdo.terminologie.db.hibernate.AssociationType assType_db
                = (de.fhdo.terminologie.db.hibernate.AssociationType) hb_session.get(de.fhdo.terminologie.db.hibernate.AssociationType.class, csev_id);

        CodeSystemEntityVersion csev = assType_db.getCodeSystemEntityVersion();
        CodeSystemEntity cse = csev.getCodeSystemEntity();

        for (CodeSystemVersionEntityMembership csvem : cse.getCodeSystemVersionEntityMemberships())
        {
          hb_session.delete(csvem);
        }

        hb_session.delete(assType_db);
        hb_session.delete(csev);
        hb_session.delete(cse);

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
        Messagebox.show("Fehler beim Löschen: " + e.getLocalizedMessage());
        hb_session.getTransaction().rollback();
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
    /*if (o != null && o instanceof de.fhdo.terminologie.db.hibernate.AssociationType)
     {
     // Hier wird die neue Zeile erstellt und der Liste übergeben
     // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
     de.fhdo.terminologie.db.hibernate.AssociationType assType = (de.fhdo.terminologie.db.hibernate.AssociationType) o;
     GenericListRowType row = createRowFromAssociation(assType);
        
     if (edited)
     genericList.updateEntry(row);
     else
     genericList.addEntry(row);
     }*/
    initList();
  }

}
