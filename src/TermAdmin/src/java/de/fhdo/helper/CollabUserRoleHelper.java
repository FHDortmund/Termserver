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
package de.fhdo.helper;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.logging.LoggingOutput;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;

/**
 *
 * @author Philipp Urbauer
 */
public class CollabUserRoleHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private static CollabUserRoleHelper instance = null;
  
  public static CollabUserRoleHelper getInstance()
  {
    if(instance == null)
      instance = new CollabUserRoleHelper();
    
    return instance;
  }
  
  private List<Role> collabUserRoles = null;

  public CollabUserRoleHelper()
  {
    initData();
  }
  
  private void initData()
  {
    if(collabUserRoles == null)
    {
      loadRolesFromDB();
    }
  }
  
  
  
  //private static HashMap<String, String> collabUserRoles = null;
  

//  public static Long getCollabUserRoleIdByName(String collabUserRole)
//  {
//    checkForNull();
//
//    if (collabUserRole == null || collabUserRole.trim().isEmpty())
//      return Long.valueOf((long) -1);
//
//    for (String key : CollabUserRoleHelper.getCollabUserRoleTable().keySet())
//    {
//      if (CollabUserRoleHelper.getCollabUserRoleTable().get(key).compareToIgnoreCase(collabUserRole) == 0)
//        return Long.valueOf(key);
//    }
//    return Long.valueOf((long) -1);
//  }

//  public static HashMap<String, String> getCollabUserRoleTable()
//  {
//    checkForNull();
//
//    return collabUserRoles;
//  }

  public void fillCombobox(final Combobox cb, final String SelectedName, final boolean disabled)
  {
    logger.debug("fillCombobox mit Roles");

    initData();
    //List<DomainValue> list = DomainHelper.getInstance().getDomainList(DomainId);

    // Renderer erstellen
    cb.setItemRenderer(new ComboitemRenderer<Role>()
    {
      public void render(Comboitem item, Role role, int i) throws Exception
      {
        if (role != null)
        {
          item.setValue(role);
          item.setLabel(role.getName());

          if (disabled)
            item.setDisabled(true);

          if (SelectedName != null && role.getName()!= null && SelectedName.equals(role.getName()))
          {
            cb.setSelectedItem(item);
            cb.setText(item.getLabel());
          }
        }
        else
          item.setLabel("");
      }
    });

    // Model erstellen und setzen
    cb.setModel(new ListModelList<Role>(getCollabUserRoles()));
  }
  
//  public ListModelList getListModelList()
//  {
//    initData();
//
//    ListModelList lml = new ListModelList(collabUserRoles);
//    return lml;
//    
//    /*List<String> listCollabUserRole = new ArrayList<String>();
//    for (String collabUserRole : CollabUserRoleHelper.getCollabUserRoleTable().values())
//    {
//      listCollabUserRole.add(collabUserRole);
//    }
//    ListModelList lm2 = new ListModelList(listCollabUserRole);
//    ComparatorStrings comparator = new ComparatorStrings();
//    lm2.sort(comparator, true);
//    return lm2;*/
//  }

//  public static String getCollabUserRoleNameById(Long domainValueId)
//  {
//    checkForNull();
//
//    String res = collabUserRoles.get(String.valueOf(domainValueId));
//    if (res != null)
//    {
//      return res;
//    }
//    else
//    {
//      return "";
//    }
//  }

  
  private void loadRolesFromDB()
  {
    collabUserRoles = new LinkedList<Role>();

    Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();

    try
    {
      String hql = "select distinct r from Role r order by name";
      collabUserRoles = hb_session_kollab.createQuery(hql).list();

//      Iterator<Role> it = roleList.iterator();
//      while (it.hasNext())
//      {
//        Role r = it.next();
//        if (!r.getName().equals("Rezensent"))
//          collabUserRoles.put(String.valueOf(r.getId()), r.getName());
//      }

    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session_kollab.close();
    }
  }

  /**
   * @return the collabUserRoles
   */
  public List<Role> getCollabUserRoles()
  {
    initData();
    
    return collabUserRoles;
  }
}
