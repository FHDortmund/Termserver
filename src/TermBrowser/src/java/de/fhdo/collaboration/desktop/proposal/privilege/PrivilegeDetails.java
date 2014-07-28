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
package de.fhdo.collaboration.desktop.proposal.privilege;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.collaboration.db.classes.Discussiongroupobject;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.desktop.proposal.PrivilegienListData;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class PrivilegeDetails extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private IUpdateModal updateListInterface;
  DualListboxDiscGroup dlbdg;
  DualListboxUser dlbu;
  private Proposal prop;

  public PrivilegeDetails()
  {
    
    Map args = Executions.getCurrent().getArg();
    try
    {
      prop = (Proposal)args.get("proposal");
    }
    catch (Exception ex)
    {
    }
      
    //Build Vok List
    Set<PrivilegeDiscGroupInfo> discGroupData = new HashSet<PrivilegeDiscGroupInfo>();
    Set<PrivilegeDiscGroupInfo> choosenDiscGroupData = new HashSet<PrivilegeDiscGroupInfo>();
    Set<PrivilegeUserInfo> userData = new HashSet<PrivilegeUserInfo>();
    Set<PrivilegeUserInfo> choosenUserData = new HashSet<PrivilegeUserInfo>();
    
    Session hb_session_kollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
    //hb_session_kollab.getTransaction().begin();
    try
    {
        
        //Lade alle Benutzer mit Privilegien auf Proposal
        String hqlPrivilegeUsers = "from Collaborationuser cu join fetch cu.privileges pri join fetch pri.proposal pro join fetch cu.organisation o where pro.id=:id";
        Query qPrivilegeUsers = hb_session_kollab.createQuery(hqlPrivilegeUsers);
        qPrivilegeUsers.setParameter("id", prop.getId());
        List<Collaborationuser> privUserList = qPrivilegeUsers.list();
        
        for(Collaborationuser cu:privUserList){
            Long id = -1l;
            for(Privilege p:cu.getPrivileges()){
                if(p.getProposal().getId().equals(prop.getId())){
                    id = p.getId();
                    break;
                }
            }
            
            PrivilegeUserInfo dgui = new PrivilegeUserInfo(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation(),true, id);
            choosenUserData.add(dgui);
        }
        
        //Lade alle Benutzer welche enabled==true und hidden =false
        String hqlAllUsers = "from Collaborationuser cu join fetch cu.organisation o where cu.enabled=:enabled and cu.hidden=:hidden";
        Query qAllUsers = hb_session_kollab.createQuery(hqlAllUsers);
        qAllUsers.setParameter("enabled", true);
        qAllUsers.setParameter("hidden", false);
        List<Collaborationuser> allUserList = qAllUsers.list();
        
        for(Collaborationuser cu:allUserList){
            PrivilegeUserInfo dgui = new PrivilegeUserInfo(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation(),false, null);
            boolean member = false;

            for(PrivilegeUserInfo info:choosenUserData){
                if(dgui.getCollaborationuserId() == info.getCollaborationuserId()){
                    member = true;
                }
            }
            
            if(!member){
                userData.add(dgui);
            }
        }
        
        //Lade alle Diskussionsgruppen mit Privilegien auf Proposal
        String hqlPrivilegeGroups = "from Discussiongroup dg join fetch dg.privileges priv join fetch priv.proposal pro where pro.id=:id";
        Query qPrivilegeGroups = hb_session_kollab.createQuery(hqlPrivilegeGroups);
        qPrivilegeGroups.setParameter("id", prop.getId());
        List<Discussiongroup> privGroupList = qPrivilegeGroups.list();
        
        for(Discussiongroup dg:privGroupList){
            
            Long id = -1l;
            for(Privilege p:dg.getPrivileges()){
                if(p.getProposal().getId().equals(prop.getId())){
                    id = p.getId();
                    break;
                }
            }
            
            Collaborationuser u = (Collaborationuser)hb_session_kollab.get(Collaborationuser.class, dg.getHead());
            PrivilegeDiscGroupInfo pdgi = new PrivilegeDiscGroupInfo(dg.getId(), dg.getHead(), dg.getName(), (u.getFirstName() + " " + u.getName()), true, id);
            choosenDiscGroupData.add(pdgi);
        }
        
        String hqlAllGroups = "from Discussiongroup";
        Query qAllGroups = hb_session_kollab.createQuery(hqlAllGroups);
        List<Discussiongroup> allGroupList = qAllGroups.list();
        
        for(Discussiongroup dg:allGroupList){
            
            Collaborationuser u = (Collaborationuser)hb_session_kollab.get(Collaborationuser.class, dg.getHead());
            PrivilegeDiscGroupInfo pdgi = new PrivilegeDiscGroupInfo(dg.getId(), dg.getHead(), dg.getName(), (u.getFirstName() + " " + u.getName()),false, null);
            boolean member = false;

            for(PrivilegeDiscGroupInfo info:choosenDiscGroupData){
                if(pdgi.getDiscussionGroupId() == info.getDiscussionGroupId()){
                    member = true;
                }
            }
            
            if(!member){
                discGroupData.add(pdgi);
            }
        }
    //hb_session_kollab.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session_kollab.getTransaction().rollback();
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei DualList preparation(): " + e.getMessage());
    }
    finally
    {
      hb_session_kollab.close();
    }
    
    Executions.getCurrent().setAttribute("discGroupData", discGroupData);
    Executions.getCurrent().setAttribute("choosenDiscGroupData", choosenDiscGroupData);
    Executions.getCurrent().setAttribute("userData", userData);
    Executions.getCurrent().setAttribute("choosenUserData", choosenUserData);
  }

  public void afterCompose()
  {
    Include incDiscGroup = (Include) getFellow("incListDiscGroup");
    Window windowDiscGroup = (Window) incDiscGroup.getFellow("duallistboxDiscGroup");
    dlbdg = (DualListboxDiscGroup)windowDiscGroup.getFellow("dualLBoxDiscGroup");
    Include incUser = (Include) getFellow("incListUser");
    Window windowUser = (Window) incUser.getFellow("duallistboxUser");
    dlbu = (DualListboxUser)windowUser.getFellow("dualLBoxUser");
  }

  public void onOkClicked()
  {
    
    if(dlbdg.getChosenDataList().isEmpty() && dlbu.getChosenDataList().isEmpty())
    {
      Messagebox.show("Achtung, Sie haben keine Diskussionsgruppen oder Diskussionsteilnehmer ausgewählt!", "Information", Messagebox.OK, Messagebox.INFORMATION);
    }

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      try
      {    
        if (logger.isDebugEnabled())
          logger.debug("Neuer Privilege Eintrag, fuege hinzu!");

        for(PrivilegeUserInfo user:dlbu.getChosenDataList()){
            
            if(!user.getPrivExists()){
            
                Privilege priv = new Privilege();
                priv.setCollaborationuser(new Collaborationuser());
                priv.getCollaborationuser().setId(user.getCollaborationuserId());

                Collaborationuser u = (Collaborationuser)hb_session.get(Collaborationuser.class, SessionHelper.getCollaborationUserID());
                if(u.getSendMail() != null){
                    priv.setSendMail(u.getSendMail());
                }else{
                    priv.setSendMail(false);
                }
                if(u.getRoles().iterator().next().getName().equals(CODES.ROLE_ADMIN) ||
                   u.getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER)){
                    priv.setMayChangeStatus(true);
                    priv.setMayManageObjects(true);
                }else{
                    priv.setMayChangeStatus(false);
                    priv.setMayManageObjects(false);
                }
                priv.setFromDate(new Date());
                priv.setProposal(new Proposal());
                priv.getProposal().setId(prop.getId());
                priv.setDiscussiongroup(null);

                hb_session.save(priv);
            }else{
                //do nothing
            }
        }
        
        for(PrivilegeUserInfo user:dlbu.getDataList()){
            
            if(user.getPrivExists()){

                Privilege priv_db = (Privilege) hb_session.get(Privilege.class, user.getPrivId());

                priv_db.setCollaborationuser(null);
                priv_db.setDiscussiongroup(null);
                priv_db.setProposal(null);

                hb_session.delete(priv_db);

            }else{
                //do nothing
            }
        }

        //Discussionsgruppenzuordnung
        for(PrivilegeDiscGroupInfo dg:dlbdg.getChosenDataList()){
            
            if(!dg.getPrivExists()){
            
                Privilege priv = new Privilege();
                priv.setDiscussiongroup(new Discussiongroup());
                priv.getDiscussiongroup().setId(dg.getDiscussionGroupId());
                
                priv.setSendMail(true);
                priv.setMayChangeStatus(false);
                priv.setMayManageObjects(false);
                
                priv.setFromDate(new Date());
                priv.setProposal(new Proposal());
                priv.getProposal().setId(prop.getId());
                priv.setCollaborationuser(null);

                hb_session.save(priv);
            }else{
                //do nothing
            }
        }
        
        for(PrivilegeDiscGroupInfo dg:dlbdg.getDataList()){
            
            if(dg.getPrivExists()){

                Privilege priv_db = (Privilege) hb_session.get(Privilege.class, dg.getPrivId());

                priv_db.setDiscussiongroup(null);
                priv_db.setCollaborationuser(null);
                priv_db.setProposal(null);

                hb_session.delete(priv_db);

            }else{
                //do nothing
            }
        }
        
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        logger.error("Fehler in PrivilegeDetails.java (onOkClicked()): " + e.getMessage());
      }

      hb_session.close();

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null){
          updateListInterface.update(null, true);
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in PrivilegeDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }
}
