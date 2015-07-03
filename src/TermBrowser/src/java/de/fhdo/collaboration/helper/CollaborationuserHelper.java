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
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.helper.SessionHelper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner
 */
public class CollaborationuserHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();


  
  
   public static Role getCollaborationuserRoleByName(String role){
   
        Session hb_session_kollab = HibernateUtil.getSessionFactory().openSession();
        //hb_session_kollab.getTransaction().begin();
        
        List<Role> roleList = null;
        try{
            String hql = "select distinct r from Role r where r.name=:role";
            Query q = hb_session_kollab.createQuery(hql);
            q.setParameter("role", role);
            roleList = q.list();
            if(roleList.size() == 1){
                return roleList.get(0);
            }
        }catch(Exception e){
          logger.error("[Fehler bei CollabUserRoleHelper.java createCollabUserRoleTable(): " + e.getMessage());
        }
        finally
        {
          hb_session_kollab.close();
        }    
      return null;
   }
}
