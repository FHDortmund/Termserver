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
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import java.util.ArrayList;
import org.hibernate.Query;
import org.hibernate.Session;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert Mützner
 */
public class AssignTermHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private static ArrayList<AssignedTerm> assignedTerms = null;
  private static Long userId = 0l;
  
  public AssignTermHelper()
  {
    
  }

  public static boolean isUserAllowed(Object o){
      
      if(userId.equals(0l) || !userId.equals(SessionHelper.getCollaborationUserID())){
          createAssignedTermsList();
          userId = SessionHelper.getCollaborationUserID();
      }    
      if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
        if(o instanceof CodeSystem){

          for(AssignedTerm at:assignedTerms){
            if(at.getClassId().equals(((CodeSystem)o).getId()) && at.getClassname().equals("CodeSystem"))
                    return true;
          }
        } else if(o instanceof ValueSet){
          for(AssignedTerm at:assignedTerms){
              if(at.getClassId().equals(((ValueSet)o).getId()) && at.getClassname().equals("ValueSet"))
                      return true;
          }
        }
      }else{
          return true;
      }
      return false;
  }
  
  public static boolean isUserAllowed(Long id, String type){
      
      
      
      if(userId.equals(0l) || !userId.equals(SessionHelper.getCollaborationUserID())){
          createAssignedTermsList();
          userId = SessionHelper.getCollaborationUserID();
      }    
      if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)){
        if(type.equals("CodeSystem")){

          for(AssignedTerm at:assignedTerms){
            if(at.getClassId().equals(id) && at.getClassname().equals("CodeSystem"))
                    return true;
          }
        } else if(type.equals("ValueSet")){
          for(AssignedTerm at:assignedTerms){
              if(at.getClassId().equals(id) && at.getClassname().equals("ValueSet"))
                      return true;
          }
        }
      }else{
          return true;
      }
      return false;
  }
  
  public static void assignTermToUser(Object obj)
  {
    
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    
    try
    {    
        AssignedTerm at_db = new AssignedTerm();
        Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, SessionHelper.getCollaborationUserID());

        if(obj instanceof CodeSystem){

            at_db.setClassId(((CodeSystem)obj).getId());
            at_db.setClassname("CodeSystem");
            at_db.setCollaborationuser(user);

        }else if(obj instanceof ValueSet){

            at_db.setClassId(((ValueSet)obj).getId());
            at_db.setClassname("ValueSet");
            at_db.setCollaborationuser(user);
        }
        hb_session.save(at_db);
      
        hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      logger.error("Fehler in AssignTermHelper assignTermToUser.java: " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
  }
  
  public static ArrayList<AssignedTerm> getUsersAssignedTerms(){        
        createAssignedTermsList();
        return assignedTerms;
    }
  
  public static void createAssignedTermsList()
  {
    assignedTerms = new ArrayList<AssignedTerm>();
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      
          Query q = hb_session.createQuery("from Collaborationuser WHERE id=:p_id");
          q.setParameter("p_id", SessionHelper.getCollaborationUserID());
          java.util.List<Collaborationuser> userList = q.list();
          
          if(userList.size() == 1){
              for(AssignedTerm at:userList.get(0).getAssignedTerms()){
                  assignedTerms.add(at);
              }
          }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
      logger.error("Fehler in AssignTermHelper getUserAssignedTerms(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
  }
}
