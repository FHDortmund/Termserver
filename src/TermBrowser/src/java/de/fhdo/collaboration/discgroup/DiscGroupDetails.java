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
package de.fhdo.collaboration.discgroup;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class DiscGroupDetails extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private DiscGroupData dgd;
  
  //private Map args;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  DualListboxUser dlbu;
  Boolean showOnly = false;

  public DiscGroupDetails()
  {
    Map args = Executions.getCurrent().getArg();
    try
    {
      dgd = (DiscGroupData)args.get("dgd");
      if(dgd == null){
          dgd = new DiscGroupData();
          dgd.setGroup(new Discussiongroup());
          dgd.getGroup().setName("");
          newEntry = true;
      }
      
      if((Boolean)args.get("showOnly") == null){
          showOnly = false;
      }else{
          showOnly = true;
      }
      
    }
    catch (Exception ex)
    {
    }
    //Build Vok List
    Set<DiscussionGroupUserInfo> userData = new HashSet<DiscussionGroupUserInfo>();
    Set<DiscussionGroupUserInfo> choosenUserData = new HashSet<DiscussionGroupUserInfo>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    Session hb_session_kollab = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
    //hb_session_kollab.getTransaction().begin();
    try
    {
        
        if(!newEntry){
            //Load User Info here
            //1.GetGroupMembers
            String hqlGroupUsers = "select distinct cu from Collaborationuser cu join fetch cu.organisation o join fetch cu.discussiongroups d where cu.enabled=:enabled and d.id=:id";
            Query qGroupUsers = hb_session_kollab.createQuery(hqlGroupUsers);
            qGroupUsers.setParameter("enabled", true);
            qGroupUsers.setParameter("id", dgd.getGroup().getId());
            List<Collaborationuser> cuList = qGroupUsers.list();

            for(Collaborationuser cu:cuList){
                DiscussionGroupUserInfo dgui = new DiscussionGroupUserInfo(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation());
                choosenUserData.add(dgui);
            }
        }
        //2.GetNotGroupMembers
        String hqlNotMembers = "select distinct cu from Collaborationuser cu join fetch cu.organisation o where cu.enabled=:p_enabled";
        Query qNotMembers = hb_session_kollab.createQuery(hqlNotMembers);
        qNotMembers.setParameter("p_enabled", true);
        //qNotMembers.setParameter("hidden", false);
        List<Collaborationuser> cuNotMemberList = qNotMembers.list();
        
        for(Collaborationuser cu:cuNotMemberList){
            
            if(cu.getHidden() == false){
                
                DiscussionGroupUserInfo dgui = new DiscussionGroupUserInfo(cu.getId(),cu.getFirstName(),cu.getName(),cu.getOrganisation().getOrganisation());
                boolean member = false;

                if(!newEntry){
                    for(DiscussionGroupUserInfo info:choosenUserData){
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
    
    Executions.getCurrent().setAttribute("userData", userData);
    Executions.getCurrent().setAttribute("choosenUserData", choosenUserData);
  }

  public void afterCompose()
  {

    Include incUser = (Include) getFellow("incListUser");
    Window windowUser = (Window) incUser.getFellow("duallistboxUser");
    dlbu = (DualListboxUser)windowUser.getFellow("dualLBoxUser");
    ((Textbox)getFellow("tb_DiscussiongroupName")).setText(dgd.getGroup().getName());
    
    if(showOnly){
    
        ((Textbox)getFellow("tb_DiscussiongroupName")).setReadonly(true);
        ((Listbox)dlbu.getFellow("lbUser")).setDisabled(true);
        ((Listbox)dlbu.getFellow("lbUserChoosen")).setDisabled(true);
        ((Image)dlbu.getFellow("chooseAllBtn")).setVisible(false);
        ((Image)dlbu.getFellow("chooseBtn")).setVisible(false);
        ((Image)dlbu.getFellow("removeBtn")).setVisible(false);
        ((Image)dlbu.getFellow("removeAllBtn")).setVisible(false);
        ((Button)getFellow("bAbort")).setVisible(false);
        ((Row)getFellow("r_cbAbmelden")).setVisible(true);
    }
  }

  public void onOkClicked()
  {
    
    if(!showOnly){
        boolean run = true;
        dgd.getGroup().setName(((Textbox)getFellow("tb_DiscussiongroupName")).getText());
        if(dgd.getGroup().getName() == null || dgd.getGroup().getName().length() == 0)
        {
          Messagebox.show("Sie müssen einen Namen für die Diskussionsgruppe und Teilnehmer angeben!", "Information", Messagebox.OK, Messagebox.EXCLAMATION);
          run=false;
        }
        if(dlbu.getChosenDataList().isEmpty()){
            Messagebox.show("Eine Diskussionsgruppe muss mindestens einen Teilnehmer haben!", "Information", Messagebox.OK, Messagebox.EXCLAMATION);
            run=false;
        }
        if(run){
            try
            {
              if (logger.isDebugEnabled())
                logger.debug("Daten speichern");

              Session hb_session = HibernateUtil.getSessionFactory().openSession();
              hb_session.getTransaction().begin();
              try
              {
                if (newEntry)
                {       
                    // prüfen, ob Gruppe bereits existiert
                    String hql = "from Discussiongroup where name=:group";
                    Query q = hb_session.createQuery(hql);
                    q.setParameter("group", dgd.getGroup().getName());
                    List dGroupList = q.list();
                    if(dGroupList != null && dGroupList.size() > 0)
                    {
                      hb_session.close();
                      run=false;
                      Messagebox.show("Diskussionsgruppe existiert bereits. Bitte wählen Sie einen anderen Namen!", "Information", Messagebox.OK, Messagebox.EXCLAMATION); 
                    }
                    if(run){
                        if (logger.isDebugEnabled())
                          logger.debug("Neuer Eintrag, füge hinzu!");

                        Discussiongroup group = new Discussiongroup(dgd.getGroup().getName());
                        for(DiscussionGroupUserInfo user:dlbu.getChosenDataList()){
                            Collaborationuser cu = (Collaborationuser)hb_session.get(de.fhdo.collaboration.db.classes.Collaborationuser.class, user.getCollaborationuserId());
                            group.getCollaborationusers().add(cu);
                        }

                        group.setHead(SessionHelper.getCollaborationUserID());

                        hb_session.save(group);
                        dgd.setGroup(group);

                        // prüfen, ob Gruppe bereits existiert
                        String hqlU = "from Collaborationuser where id=:p_id";
                        Query qU = hb_session.createQuery(hqlU);
                        qU.setParameter("p_id", SessionHelper.getCollaborationUserID());
                        List<Collaborationuser> userList = qU.list();
                        if(userList.size() == 1)
                        {
                            dgd.setHeadOfGroup(userList.get(0));
                        }

                        hb_session.getTransaction().commit();
                    }
                }
                else
                {
                    dgd.getGroup().getCollaborationusers().clear();

                    for(DiscussionGroupUserInfo user:dlbu.getChosenDataList()){  
                        Collaborationuser cu = (Collaborationuser)hb_session.get(de.fhdo.collaboration.db.classes.Collaborationuser.class, user.getCollaborationuserId());
                        dgd.getGroup().getCollaborationusers().add(cu);
                    }

                    hb_session.merge(dgd.getGroup());
                    hb_session.getTransaction().commit();
                }

              }
              catch (Exception e)
              {
                hb_session.getTransaction().rollback();
                logger.error("Fehler in DiskussionsgruppenDetails.java (onOkClicked()): " + e.getMessage());
              }

              if(run){
                hb_session.close();

                this.setVisible(false);
                this.detach();

                if (updateListInterface != null){

                    updateListInterface.update(dgd, !newEntry);
                }
              }
            }
            catch (Exception e)
            {
              // Fehlermeldung ausgeben
              logger.error("Fehler in DiskussionsgruppenDetails.java: " + e.getMessage());
              e.printStackTrace();
            }
        }
    }else{
        Boolean run = false;
        if(((Checkbox)getFellow("cb_cbAbmelden")).isChecked()){
        
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            hb_session.getTransaction().begin();
            try{
                Discussiongroup dg = (Discussiongroup)hb_session.get(Discussiongroup.class, dgd.getGroup().getId());
                
                for(Collaborationuser u:dg.getCollaborationusers()){
                    if(u.getId().equals(SessionHelper.getCollaborationUserID())){
                        dg.getCollaborationusers().remove(u);
                        break;
                    }
                }
                hb_session.update(dg);
                hb_session.getTransaction().commit();
                run = true;
            }catch(Exception e){
                  hb_session.getTransaction().rollback();
                  logger.error("Fehler in DiskussionsgruppenDetails.java (onOkClicked()): " + e.getMessage());
            }finally{
                hb_session.close();
            }
        }
        this.setVisible(false);
        this.detach();
        if(run){
            if (updateListInterface != null){

                updateListInterface.update(run, false); //ResetList
            }
        }
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

    public DiscGroupData getDgd() {
        return dgd;
    }

    public void setDgd(DiscGroupData dgd) {
        this.dgd = dgd;
    }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }
}
