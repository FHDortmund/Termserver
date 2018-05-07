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
package de.fhdo.collaboration.proposal;

import de.fhdo.collaboration.db.classes.Status;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.helper.SessionHelper;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner
 */
public class ProposalStatus
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static ProposalStatus instance;

  public static ProposalStatus getInstance()
  {
    if (instance == null)
      instance = new ProposalStatus();

    return instance;
  }
  // Klasse
  //private List<Status> statusList;
  private Map<Long, Status> statusMap;
  private Map<Long, Statusrel> statusrelMap;

  public ProposalStatus()
  {
    statusMap = null;
    initData();
  }

  public void reloadData()
  {
    statusMap = null;
  }

  private void initData()
  {
    if (statusMap == null)
    {
      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        statusMap = new HashMap<Long, Status>();
        statusrelMap = new HashMap<Long, Statusrel>();

        String hql = "select distinct s from Status s"
            + " left join fetch s.statusrelsForStatusIdFrom rel"
            + " left join fetch rel.action";
        //+ " left join fetch rel.statusByStatusIdTo";

        List<Status> statusList = hb_session.createQuery(hql).list();

        for (Status status : statusList)
        {
          statusMap.put(status.getId(), status);

          /*for(Statusrel rel : status.getStatusrelsForStatusIdFrom())
           {
           logger.debug("Status: " + status.getId() + " von " + rel.getStatusByStatusIdFrom().getId() + " zu " + rel.getStatusByStatusIdTo().getId());
           }*/
        }

        hql = "select distinct rel from Statusrel rel"
            + " left join fetch rel.roles roles"
            + " left join fetch rel.action";

        List<Statusrel> statusrelList = hb_session.createQuery(hql).list();

        for (Statusrel rel : statusrelList)
        {
          statusrelMap.put(rel.getId(), rel);
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

  /**
   * Liest den Status-Text von einem Zahlenwert
   *
   * @param status
   * @return
   */
  public String getStatusStr(long status)
  {
    initData();

    if (statusMap.containsKey(status))
    {
      return statusMap.get(status).getStatus();
    }

    return "";
  }

  public Status getStatus(long status)
  {
    initData();

    if (statusMap.containsKey(status))
    {
      return statusMap.get(status);
    }

    return null;
  }

  public Object getHeaderFilter()
  {
    initData();
    try
    {
      String s[] = new String[statusMap.values().size()];
      int count = 0;
      for (Status status : statusMap.values())
      {
        s[count++] = status.getStatus();
      }
      return s;
    }
    catch (Exception e)
    {
    }
    return "String";
  }

  public Set<Statusrel> getStatusChilds(long status)
  {
    initData();

    if (statusMap.containsKey(status))
    {
      return statusMap.get(status).getStatusrelsForStatusIdFrom();
    }

    return new HashSet<Statusrel>();
  }

  public Statusrel getStatusRel(long statusFrom, long statusTo)
  {
    initData();

    for (Statusrel rel : statusrelMap.values())
    {
      if (rel.getStatusByStatusIdFrom().getId().longValue() == statusFrom
          && rel.getStatusByStatusIdTo().getId().longValue() == statusTo)
      {
        // Statusänderung
        return rel;
      }
    }

    return null;
  }

  public boolean isStatusChangePossible(long statusFrom, long statusTo)
  {
    initData();

    if (getStatusRel(statusFrom, statusTo) == null)
      return false;
    else
      return true;
  }

  public boolean isUserAllowed(Statusrel rel, long collabUserId)
  {
    boolean erlaubt = false;
    if (logger.isDebugEnabled())
      logger.debug("isUserAllowed() mit userId: " + collabUserId);

    if (collabUserId == SessionHelper.getCollaborationUserID())
    {
      Object o = SessionHelper.getValue("collaboration_user_roles");
      if (o != null)
      {
        Set<Role> roles = (Set<Role>) o;
        //logger.debug("user has roles: " + roles.size());
        //logger.debug("Status change need one of the following roles: ");
//        for (Role roleCompare : rel.getRoles())
//        {
//          logger.debug(roleCompare.getName());
//        }
        
        for (Role role : roles)
        {
          for (Role roleCompare : rel.getRoles())
          {
            if (role.getId().longValue() == roleCompare.getId().longValue())
            {
              // Berechtigung vorhanden
              erlaubt = true;
              break;
            }
          }
          if (erlaubt)
            break;
        }
        
      }
      else
      {
        logger.debug("user has no roles");
        return false;
      }

    }
    else
    {
      initData();
      
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      try
      {
        String hql = "select distinct r from Role r"
            + " join r.collaborationusers cu"
            + " where cu.id=" + collabUserId;

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        List<Role> roleList = hb_session.createQuery(hql).list();

        //if(logger.isDebugEnabled())
        //  logger.debug("Anzahl: " + roleList.size());
        for (Role role : roleList)
        {
          //if(logger.isDebugEnabled())
          //  logger.debug("Rolle: " + role.getName() + ", id: " + role.getId());

          for (Role roleCompare : rel.getRoles())
          {
            //if(logger.isDebugEnabled())
            //  logger.debug("  vergleiche mit Rolle: " + roleCompare.getName() + ", id: " + roleCompare.getId());

            if (role.getId().longValue() == roleCompare.getId().longValue())
            {
              // Berechtigung vorhanden
              erlaubt = true;
              break;
            }
          }
          if (erlaubt)
            break;
        }
        //hb_session.getTransaction().commit();
      }
      catch (Exception ex)
      {
        //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(ex, this);
      }
      finally
      {
        // Session schließen
        hb_session.close();
      }
    }

    return erlaubt;
  }
}
