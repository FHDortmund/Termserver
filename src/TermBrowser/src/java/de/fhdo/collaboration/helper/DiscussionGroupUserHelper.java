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
package de.fhdo.collaboration.helper;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner
 */
public class DiscussionGroupUserHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  // SINGLETON-MUSTER
  private static DiscussionGroupUserHelper instance;

  public static DiscussionGroupUserHelper getInstance()
  {
    if (instance == null)
      instance = new DiscussionGroupUserHelper();

    return instance;
  }
  // KLASSE
  private Map<Long, Discussiongroup> discussionGroupMap;
  //private Map<Long, Collaborationuser> userMap;

  public DiscussionGroupUserHelper()
  {
  }
  
  public int countUsersInDiscussionGroup(long DiscussionGroupId)
  {
    initData();
    
    if(discussionGroupMap != null)
    {
      if(discussionGroupMap.containsKey(DiscussionGroupId))
      {
        Discussiongroup dg = discussionGroupMap.get(DiscussionGroupId);
        return dg.getCollaborationusers().size();
      }
    }
    
    return 0;
  }
  
  public Discussiongroup getDiscussionGroup(long DiscussionGroupId)
  {
    initData();
    
    if(discussionGroupMap != null)
    {
      if(discussionGroupMap.containsKey(DiscussionGroupId))
      {
        return discussionGroupMap.get(DiscussionGroupId);
      }
    }
    
    return null;
  }
  

  public void reloadData()
  {
    discussionGroupMap = null;
  }

  public void initData()
  {
    if (discussionGroupMap == null)
    {
      discussionGroupMap = new HashMap<Long, Discussiongroup>();
      //userMap = new HashMap<Long, Collaborationuser>();

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      try
      {
        String hql = "from Discussiongroup dg "
                + " left join fetch dg.collaborationusers cu "
                + " left join fetch cu.organisation org";

        List<Discussiongroup> list = hb_session.createQuery(hql).list();
        if (list != null && list.size() > 0)
        {
          for (Discussiongroup dg : list)
          {
            discussionGroupMap.put(dg.getId(), dg);
          }
        }
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
          //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

    }

  }
}
