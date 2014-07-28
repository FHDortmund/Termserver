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
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Organisation;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.communication.Mail;
import de.fhdo.helper.CollabUserRoleHelper;
import de.fhdo.helper.MD5;
import de.fhdo.helper.Password;
import de.fhdo.interfaces.IUpdateModal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class BenutzerDetails extends Window implements AfterCompose, EventListener<Event>
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Collaborationuser user;
  //private Map args;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  private Combobox cbUserRole;
  private Session hb_sessionS;

  public BenutzerDetails()
  {
    Map args = Executions.getCurrent().getArg();
    long userId = 0;
    try
    {
      userId = Long.parseLong(args.get("user_id").toString());
    }
    catch (Exception ex)
    {
    }

    if (userId > 0)
    {
      // Domain laden
      hb_sessionS = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      
      user = (Collaborationuser) hb_sessionS.get(Collaborationuser.class, userId);


    }

    if (user == null)
    {
      user = new Collaborationuser();
      user.setEnabled(true);
      user.setHidden(false);
      user.setDeleted(false);
      user.setSendMail(true);
      user.getRoles().clear();
      user.getRoles().add(new Role());
      user.setOrganisation(new Organisation());
      newEntry = true;
    }
  }

  public void afterCompose()
  {
    ((Textbox) getFellow("tb_Benutzername")).setReadonly(!newEntry);
    ((Checkbox) getFellow("cb_aktiv")).setChecked(!newEntry);                                
    ((Checkbox) getFellow("cb_aktiv")).setDisabled(newEntry);
    cbUserRole = (Combobox) getFellow("cb_UserRole");
    cbUserRole.setModel(CollabUserRoleHelper.getListModelList());
    cbUserRole.addEventListener("onInitRenderLater", this);
  }
  
  public void onEvent(Event event) throws Exception {
   
        if(user == null || 
           user.getRoles() == null || 
           user.getRoles().isEmpty() || user.getRoles().iterator().next() == null ||
           user.getRoles().iterator().next().getId() == null)
              return;
        
        Iterator<Comboitem> it = cbUserRole.getItems().iterator();
        while(it.hasNext()){
            Comboitem ci = it.next();
            if(user.getRoles().iterator().next().getId().compareTo(CollabUserRoleHelper.getCollabUserRoleIdByName(ci.getLabel())) == 0){
                cbUserRole.setSelectedItem(ci);                
            }
        }  
  }
  
  public void onOkClicked()
  {
    String mailResponse = "";
    
    // speichern mit Hibernate
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
          // Pflichtfelder prüfen
          if(user.getUsername() == null || user.getUsername().length() == 0 || 
             user.getEmail() == null || user.getEmail().length() == 0 || user.getEmail().contains("@") == false ||
             cbUserRole.getSelectedItem() == null || user.getOrganisation().getOrganisation() == null || user.getOrganisation().getOrganisation().equals(""))
          {
            Messagebox.show("Sie müssen einen Benutzernamen, eine gültige Email-Adresse, Benutzerrolle und Organisation angeben.");
            hb_session.close();
            return;
          }
          
          // prüfen, ob Benutzer bereits existiert
          String hql = "from Collaborationuser where username=:user";
          Query q = hb_session.createQuery(hql);
          q.setParameter("user", user.getUsername());
          List userList = q.list();
          if(userList != null && userList.size() > 0)
          {
            Messagebox.show("Benutzer existiert bereits. Bitte wählen Sie einen anderen Benutzernamen.");
            hb_session.close();
            return;
          }
          
          if (logger.isDebugEnabled())
            logger.debug("Neuer Eintrag, fuege hinzu!");

          // Passwort und Salt generieren
          String neuesPW = Password.generateRandomPassword(8);
          String salt = Password.generateRandomSalt();
          user.setPassword(Password.getSaltedPassword(neuesPW, salt, user.getUsername(), 1000));
          user.setSalt(salt);
          user.setActivated(false);
          user.setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));
          user.setActivationTime(new Date());
          
          Role r = (Role)hb_session.get(Role.class, CollabUserRoleHelper.getCollabUserRoleIdByName(cbUserRole.getSelectedItem().getLabel()));
          user.getRoles().clear();
          user.getRoles().add(r);
          // Benutzer speichern
          hb_session.save(user);
          user.getOrganisation().getCollaborationusers().clear();
          user.getOrganisation().getCollaborationusers().add(user);
          
          hb_session.save(user.getOrganisation());
          
          // Benachrichtigung senden
          mailResponse = Mail.sendMailCollaborationNewUser(user.getUsername(), neuesPW,
                   user.getEmail(), user.getActivationMd5());
          
          if (mailResponse.length() == 0)
          {
            hb_session.getTransaction().commit();
            Messagebox.show("Benutzer wurde erfolgreich angelegt und Aktivierungs-Email verschickt.");
          }
          else
          {
            Messagebox.show("Fehler beim Anlegen eines Benutzers: " + mailResponse);
            hb_session.getTransaction().rollback();
          }
          
          neuesPW = "                       ";
        }
        else
        {
          Role r = (Role)hb_session.get(Role.class, CollabUserRoleHelper.getCollabUserRoleIdByName(cbUserRole.getSelectedItem().getLabel()));
          user.getRoles().clear();
          user.getRoles().add(r);
          hb_session.merge(user);
          hb_session.getTransaction().commit();
        }
        
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        logger.error("Fehler in DomainDetails.java (onOkClicked()): " + e.getMessage());
      }

      hb_session.close();

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(user, !newEntry);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in BenutzerDetails.java: " + e.getMessage());
      hb_sessionS.close();
    }
    if(hb_sessionS != null)
        hb_sessionS.close();
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
    if(hb_sessionS != null)
        hb_sessionS.close();
  }

  /**
   * @return the user
   */
  public Collaborationuser getUser()
  {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(Collaborationuser user)
  {
    this.user = user;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }    
}
