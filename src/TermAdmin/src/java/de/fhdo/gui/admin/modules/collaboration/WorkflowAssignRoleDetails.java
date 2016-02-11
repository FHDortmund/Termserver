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
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.list.IUpdateData;
import de.fhdo.logging.LoggingOutput;
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
public class WorkflowAssignRoleDetails extends Window implements AfterCompose, IGenericListActions, IUpdateData
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;
  private IUpdate updateListener;
  long statusrelId = 0;

  public WorkflowAssignRoleDetails()
  {
    try
    {
      Map args = Executions.getCurrent().getArg();
      statusrelId = Long.parseLong(args.get("statusrel_id").toString());
      logger.debug("statusrelId: " + statusrelId);
    }
    catch (Exception e)
    {
      logger.error("Parameter 'statusrel_id' nicht gefunden");
    }
  }

  public void afterCompose()
  {
    initList();
  }

  private GenericListRowType createRowFromRole(Role role)
  {
    GenericListRowType row = new GenericListRowType();

    boolean zugewiesen = false;
    
    for (Statusrel sRel : role.getStatusrels())
    {
      if(sRel.getId().longValue() == statusrelId)
      {
        zugewiesen = true;
        break;
      }
    }
    
    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(zugewiesen, false, "");
    cells[1] = new GenericListCellType(role.getName(), false, "");
    cells[2] = new GenericListCellType(role.getAdminFlag(), false, "");

    row.setData(role);
    row.setCells(cells);

    return row;
  }
  
  public void onCancelClicked()
  {
    if(updateListener != null)
      updateListener.update("RELOAD");
    
    this.setVisible(false);
    this.detach();
  }

  public void initList()
  {

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("assigned"), 90, "", true, "Boolean", true, true, true, true));
    header.add(new GenericListHeaderType(Labels.getLabel("name"), 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("administrator"), 80, "", true, "bool", true, true, false, true));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from Role r order by r.name";
      
      //String hql = "from Role r join fetch r.applicationRoles ar where application_id=" + anwendung.getId() + " order by r.name";
      List<Role> roleList = hb_session.createQuery(hql).list();

      logger.debug("Size: " + roleList.size());

      for (int i = 0; i < roleList.size(); ++i)
      {
        GenericListRowType row = createRowFromRole(roleList.get(i));
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
    genericList.setUpdateDataListener(this);
    genericList.setButton_edit(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);

  }

  public void onNewClicked(String id)
  {
  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("Edit clicked");
    
    this.setVisible(false);
    this.detach();
    
    if(updateListener != null)
      updateListener.update(data);
  }

  public void onDeleted(String id, Object data)
  {
  }

  public void onSelected(String id, Object data)
  {
  }

  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdate updateListener)
  {
    this.updateListener = updateListener;
  }

  public void onCellUpdated(int cellIndex, Object data, GenericListRowType row)
  {
    try
    {
      if (cellIndex == 0)
      {
        Role role = (Role) row.getData();
        boolean isChecked = (Boolean) data;

        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        try
        {
          org.hibernate.Transaction tx = hb_session.beginTransaction();

          //String hql = "from Statusrel sr join fetch sr.roles r where r.id=" + role.getId() + " and sr.id=" + statusrelId;
          String hql = "from Statusrel sr where sr.id=" + statusrelId;
          logger.debug("HQL: " + hql);

          List<Statusrel> srList = hb_session.createQuery(hql).list();

          if (srList != null)
          {
            Statusrel sRel_db = srList.get(0);
            
            if(isChecked)
            {
              // hinzufügen
              sRel_db.getRoles().add(role);
              
              logger.debug("Füge Rolle hinzu mit ID: " + role.getId());
            }
            else if (isChecked == false)
            {
              // Verbindung löschen
              for (Role role_list : sRel_db.getRoles())
              {
                //logger.debug("Lösche Role mit ID: " + srRole_db.getId());
                if(role_list.getId().longValue() == role.getId().longValue())
                {
                  logger.debug("Entferne Rolle mit ID: " + role.getId());
                  
                  sRel_db.getRoles().remove(role_list);
                  break;
                }
              }
            }
            
            hb_session.update(sRel_db);
            tx.commit();
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

      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }
}
