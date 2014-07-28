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
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class SvAssignmentDetails extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private SvAssignmentData svAssignmentData;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  DualListboxUser dlbu;

  public SvAssignmentDetails()
  {
    Map args = Executions.getCurrent().getArg();

    svAssignmentData = (SvAssignmentData)args.get("svAssignmentData");
    
    //Prepare Data
    Set<SvAssignmentData> userData = new HashSet<SvAssignmentData>();
    Set<SvAssignmentData> choosenUserData = new HashSet<SvAssignmentData>();
    
    Session hb_session_kollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
    //hb_session_kollab.getTransaction().begin();
    
    try{
        
        if(!newEntry){
            //Load User Info here
            //1.GetGroupMembers
            String hqlTermUser = "select distinct cu from Collaborationuser cu join fetch cu.organisation o join fetch cu.assignedTerms a where cu.enabled=:enabled and a.classId=:classId";
            Query qTermUser = hb_session_kollab.createQuery(hqlTermUser);
            qTermUser.setParameter("enabled", true);
            qTermUser.setParameter("classId", svAssignmentData.getClassId());
            List<Collaborationuser> cuList = qTermUser.list();

            for(Collaborationuser cu:cuList){
                SvAssignmentData dgui = new SvAssignmentData(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation());
                choosenUserData.add(dgui);
            }
        }
        //2.GetNotGroupMembers
        String hqlNotMembers = "select distinct cu from Collaborationuser cu join fetch cu.organisation o join fetch cu.roles r where cu.enabled=:enabled AND deleted=0 ";
               hqlNotMembers += " and r.name in (:nameList)";
        Query qNotMembers = hb_session_kollab.createQuery(hqlNotMembers);
        qNotMembers.setParameter("enabled", true);
        ArrayList<String> nameList = new ArrayList<String>();
        nameList.add(CODES.ROLE_INHALTSVERWALTER);
        nameList.add(CODES.ROLE_ADMIN); //=> Darf sowieso alles sehen
        qNotMembers.setParameterList("nameList", nameList);
        List<Collaborationuser> cuNotMemberList = qNotMembers.list();
        
        for(Collaborationuser cu:cuNotMemberList){
            SvAssignmentData dgui = new SvAssignmentData(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation());
            boolean member = false;
            
            if(!newEntry){
                for(SvAssignmentData info:choosenUserData){
                    if(dgui.getCollaborationuserId() == info.getCollaborationuserId()){
                        member = true;
                    }
                }
            }
            if(!member){
                userData.add(dgui);
            }
        }
    }
    catch (Exception e)
    {
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei DualList preparation(): " + e.getMessage());
    }
    finally
    {
      hb_session_kollab.close();
    }
    
    Executions.getCurrent().setAttribute("userData", userData);
    Executions.getCurrent().setAttribute("choosenUserData", choosenUserData);
  }

  public void afterCompose()
  {
    Include incUser = (Include) getFellow("incListUser");
    Window windowUser = (Window) incUser.getFellow("duallistboxUser");
    dlbu = (DualListboxUser)windowUser.getFellow("dualLBoxUser");
  }
  
  public void onOkClicked()
  {
    
    // speichern mit Hibernate
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {  
        
        if(dlbu.getChosenDataList().isEmpty()){
        
            //Check ob AT vorhanden; Wenn ja delete; Wenn nein ignore
            if(svAssignmentData.getAssignedTermId() == null){  //Vorher kein User
            
                //Do nothing
            }else{
                //Update Old
                AssignedTerm a = (AssignedTerm)hb_session.get(AssignedTerm.class, svAssignmentData.getAssignedTermId());
                
                Collaborationuser userOld = (Collaborationuser)hb_session.get(Collaborationuser.class, svAssignmentData.getCollaborationuserId());
                Long oldPrivId = -1l;
                
                String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";

                Query q = hb_session.createQuery(hql);
                q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
                q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
                List<Proposal> proposalList = q.list();

                for(Proposal prop:proposalList){  
                    
                    for(Privilege priv:prop.getPrivileges()){

                        if(priv.getCollaborationuser().getId().equals(userOld.getId()))
                            oldPrivId = priv.getId();
                    }

                    if((!oldPrivId.equals(-1l)) && !prop.getCollaborationuser().getId().equals(userOld.getId())){

                        Privilege priv_db = (Privilege) hb_session.get(Privilege.class, oldPrivId);
                        priv_db.setCollaborationuser(null);
                        priv_db.setDiscussiongroup(null);
                        priv_db.setProposal(null);
                        userOld.getPrivileges().remove(priv_db);
                        prop.getPrivileges().remove(priv_db);
                        hb_session.delete(priv_db);
                    }
                }
                
                
                
                a.setCollaborationuser(null);
                hb_session.delete(a);
                
                svAssignmentData.setAssignedTermId(null);
                svAssignmentData.setCollaborationuserId(null);
                svAssignmentData.setUsername("-");
                svAssignmentData.setFirstName("-");
                svAssignmentData.setName("-");
                svAssignmentData.setOrganisation("-");
            }
            
        }else{
        
            //Check ob AT vorhanden; Wenn ja update; Wenn nein save
            if(svAssignmentData.getAssignedTermId() == null){  //Vorher kein User
                //Neu von Leer weg
                AssignedTerm at_db = new AssignedTerm();
                at_db.setClassId(svAssignmentData.getClassId());
                at_db.setClassname(svAssignmentData.getClassname());
                Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, dlbu.getChosenDataList().iterator().next().getCollaborationuserId());
                at_db.setCollaborationuser(user);
                hb_session.save(at_db);
                
                String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";
                
                Query q = hb_session.createQuery(hql);
                q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
                q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
                List<Proposal> proposalList = q.list();
                Long privId = -1l;
                
                for(Proposal prop:proposalList){  
                    privId = -1l;
                    for(Privilege priv:prop.getPrivileges()){
                        if(priv.getCollaborationuser().getId().equals(user.getId()))
                            privId = priv.getId();
                    }
                    if(privId.equals(-1l)){
                    
                        Privilege priv = new Privilege();
                        priv.setCollaborationuser(new Collaborationuser());
                        priv.getCollaborationuser().setId(user.getId());

                        priv.setSendMail(user.getSendMail());
                        priv.setMayChangeStatus(true);
                        priv.setMayManageObjects(true);
                        
                        priv.setFromDate(new Date());
                        priv.setProposal(new Proposal());
                        priv.getProposal().setId(prop.getId());
                        priv.setDiscussiongroup(null);
                        hb_session.save(priv);
                    }else{
                    
                        Privilege priv_db = (Privilege) hb_session.get(Privilege.class, privId);
                        priv_db.setMayChangeStatus(true);
                        priv_db.setMayManageObjects(true);
                        hb_session.update(priv_db);
                    }     
                }
                
                Mail.sendMailAUT(user, M_AUT.SV_ASSIGNMENT_SUBJECT, M_AUT.getInstance().getSvAssignementText(svAssignmentData.getTermName()));
                
                svAssignmentData.setAssignedTermId(at_db.getId());
                svAssignmentData.setCollaborationuserId(user.getId());
                svAssignmentData.setUsername(user.getUsername());
                svAssignmentData.setFirstName(user.getFirstName());
                svAssignmentData.setName(user.getName());
                svAssignmentData.setOrganisation(user.getOrganisation().getOrganisation());
            }else{
                //Update gleicher oder anderer user...
                AssignedTerm a = (AssignedTerm)hb_session.get(AssignedTerm.class, svAssignmentData.getAssignedTermId());
                Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, dlbu.getChosenDataList().iterator().next().getCollaborationuserId());
                Collaborationuser userOld = (Collaborationuser)hb_session.get(Collaborationuser.class, svAssignmentData.getCollaborationuserId());
                Long oldPrivId = -1l;
                Long privId = -1l;
                
                if(!a.getCollaborationuser().getId().equals(user.getId())){
                
                     String hql = "select distinct p from Proposal p "
                                + " where p.vocabularyIdTwo=:vocabularyIdTwo and p.vocabularyNameTwo=:vocabularyNameTwo";

                     Query q = hb_session.createQuery(hql);
                     q.setParameter("vocabularyIdTwo", svAssignmentData.getClassId());
                     q.setParameter("vocabularyNameTwo", svAssignmentData.getClassname());
                     List<Proposal> proposalList = q.list();
                     
                     for(Proposal prop:proposalList){  
                         
                         for(Privilege priv:prop.getPrivileges()){
                             if(priv.getCollaborationuser().getId().equals(user.getId()))
                                 privId = priv.getId();
                             
                             if(priv.getCollaborationuser().getId().equals(userOld.getId()))
                                 oldPrivId = priv.getId();
                         }
                         
                         if(privId.equals(-1l)){

                             Privilege priv = new Privilege();
                             priv.setCollaborationuser(new Collaborationuser());
                             priv.getCollaborationuser().setId(user.getId());

                             priv.setSendMail(user.getSendMail());
                             priv.setMayChangeStatus(true);
                             priv.setMayManageObjects(true);

                             priv.setFromDate(new Date());
                             priv.setProposal(new Proposal());
                             priv.getProposal().setId(prop.getId());
                             priv.setDiscussiongroup(null);
                             hb_session.save(priv);
                         }else{
                         
                             Privilege priv_db = (Privilege) hb_session.get(Privilege.class, privId);
                             priv_db.setMayChangeStatus(true);
                             priv_db.setMayManageObjects(true);
                             hb_session.update(priv_db);
                         }
                         
                         if((!oldPrivId.equals(-1l)) && !prop.getCollaborationuser().getId().equals(userOld.getId())){
                         
                             Privilege priv_db = (Privilege) hb_session.get(Privilege.class, oldPrivId);
                             priv_db.setCollaborationuser(null);
                             priv_db.setDiscussiongroup(null);
                             priv_db.setProposal(null);
                             userOld.getPrivileges().remove(priv_db);
                             prop.getPrivileges().remove(priv_db);
                             hb_session.delete(priv_db);
                         }
                     }
                }
                
                a.setCollaborationuser(null);
                a.setCollaborationuser(user);                
                hb_session.update(a);
                
                Mail.sendMailAUT(user, M_AUT.SV_ASSIGNMENT_SUBJECT, M_AUT.getInstance().getSvAssignementText(svAssignmentData.getTermName()));
                
                svAssignmentData.setCollaborationuserId(user.getId());
                svAssignmentData.setUsername(user.getUsername());
                svAssignmentData.setFirstName(user.getFirstName());
                svAssignmentData.setName(user.getName());
                svAssignmentData.setOrganisation(user.getOrganisation().getOrganisation());
            }
        } 
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        logger.error("Fehler in SvAssignmentDetails.java (onOkClicked()): " + e.getMessage());
      }finally{
      
        hb_session.close();
      }

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(svAssignmentData, !newEntry);
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in SvAssignmentDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
    
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  /**
   * @return the user
   */
  public SvAssignmentData getSvAssignmentData()
  {
    return svAssignmentData;
  }

  /**
   * @param user the user to set
   */
  public void setSvAssignmentData(SvAssignmentData svAssignmentData)
  {
    this.svAssignmentData = svAssignmentData;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }    
}
