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
import de.fhdo.collaboration.db.classes.Action;
import de.fhdo.collaboration.db.classes.Status;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class WorkflowDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Statusrel statusrel;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  Combobox cbStatusFrom, cbStatusTo, cbAction;

  public WorkflowDetails()
  {
    long statusrelId = ArgumentHelper.getWindowArgumentLong("statusrel_id");

    if (statusrelId > 0)
    {
      // Eintrag bearbeiten
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        // Eintrag öffnen
        statusrel = (Statusrel) hb_session.get(Statusrel.class, statusrelId);



      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
      }
      finally
      {
        hb_session.close();
      }

    }
    else
    {
      // Neuer Eintrag
      newEntry = true;

      statusrel = new Statusrel();
      //statusrel.setStatusByStatusIdFrom(new Status());
      //statusrel.setStatusByStatusIdTo(new Status());
      //statusrel.setAction(new Action());
    }

    /*if (userId > 0)
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
     user.setSendMail(false);
     user.getRoles().clear();
     user.getRoles().add(new Role());
     user.setOrganisation(new Organisation());
     newEntry = true;
     }*/



  }

  public void afterCompose()
  {
    cbStatusFrom = (Combobox) getFellow("cb_StatusFrom");
    cbStatusTo = (Combobox) getFellow("cb_StatusTo");
    cbAction = (Combobox) getFellow("cb_Aktion");

    // Combobox-Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    try
    {
      // Statuslisten
      String hql = "from Status s order by s.status";
      List<Status> listStatus = hb_session.createQuery(hql).list();

      ListModelList lmlFrom = new ListModelList(listStatus);
      cbStatusFrom.setModel(lmlFrom);

      ListModelList lmlTo = new ListModelList(listStatus);
      cbStatusTo.setModel(lmlTo);

      // Aktionen
      hql = "from Action a order by a.action";
      List<Action> listAction = hb_session.createQuery(hql).list();

      ListModelList lmlAction = new ListModelList(listAction);
      cbAction.setModel(lmlAction);

      // Itemrenderer
      cbStatusFrom.setItemRenderer(new ComboitemRenderer<Status>()
      {
        public void render(Comboitem cmbtm, Status t, int i) throws Exception
        {
          cmbtm.setLabel(t.getStatus());
          cmbtm.setValue(t);

          if (statusrel.getStatusByStatusIdFrom() != null && statusrel.getStatusByStatusIdFrom().getId().longValue() == t.getId().longValue())
          {
            cbStatusFrom.setSelectedItem(cmbtm);
          }
        }
      });
      cbStatusTo.setItemRenderer(new ComboitemRenderer<Status>()
      {
        public void render(Comboitem cmbtm, Status t, int i) throws Exception
        {
          cmbtm.setLabel(t.getStatus());
          cmbtm.setValue(t);

          if (statusrel.getStatusByStatusIdTo() != null && statusrel.getStatusByStatusIdTo().getId().longValue() == t.getId().longValue())
          {
            cbStatusTo.setSelectedItem(cmbtm);
          }
        }
      });

      cbAction.setItemRenderer(new ComboitemRenderer()
      {
        public void render(Comboitem cmbtm, Object t, int i) throws Exception
        {
          Action action = (Action) t;
          cmbtm.setLabel(action.getAction());
          cmbtm.setValue(action);

          if (statusrel.getAction() != null && statusrel.getAction().getId().longValue() == action.getId().longValue())
          {
            cbAction.setSelectedItem(cmbtm);
          }
        }
      });
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close();
    }

    // Auswahl treffen
    if (statusrel.getAction() != null)
    {
      /*for(Comboitem item : cbAction.getItems())
       {
       if(item.)
       }*/
      //cbAction.setSelectedItem(null);
    }

  }

  public void onOkClicked()
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      if (cbAction.getSelectedItem() != null)
      {
        statusrel.setAction((Action) cbAction.getSelectedItem().getValue());
        logger.debug("Action: " + statusrel.getAction().getAction());
      }

      if (cbStatusFrom.getSelectedItem() != null)
      {
        statusrel.setStatusByStatusIdFrom((Status) cbStatusFrom.getSelectedItem().getValue());
        logger.debug("Status From: " + statusrel.getStatusByStatusIdFrom().getStatus());
      }

      if (cbStatusTo.getSelectedItem() != null)
      {
        statusrel.setStatusByStatusIdTo((Status) cbStatusTo.getSelectedItem().getValue());
        logger.debug("Status From: " + statusrel.getStatusByStatusIdTo().getStatus());
      }

      if (statusrel.getAction() == null
              || statusrel.getStatusByStatusIdFrom() == null
              || statusrel.getStatusByStatusIdTo() == null)
      {
        Messagebox.show("Bitte füllen Sie alle Pflichtfelder aus.");
        hb_session.close();
        return;
      }

      org.hibernate.Transaction tx = hb_session.beginTransaction();

      if (newEntry)
      {
        // Neuen Eintrag speichern

        hb_session.save(statusrel);

      }
      else
      {
        // Eintrag bearbeiten


        hb_session.merge(statusrel);
      }

      
      
      /*if (updateListInterface != null)
      {
        logger.debug("Lade neu mit ID: " + statusrel.getId());
        
        statusrel = (Statusrel) hb_session.get(Statusrel.class, statusrel.getId());
        
      
        
        UpdateWorkflowType uType = new UpdateWorkflowType();
        uType.setHb_session(hb_session);
        uType.setO(statusrel);
        
        updateListInterface.update(uType, !newEntry);
      }*/
      
      
      tx.commit();
      
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      logger.debug("SESSION SCHLIESSEN!");
      hb_session.close();
    }

    // Fenster schließen und Liste aktualisieren
    this.setVisible(false);
    this.detach();

    if(updateListInterface != null)
      updateListInterface.update(null, !newEntry);


    /*String mailResponse = "";
    
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
     Messagebox.show("Benutzers existiert bereits. Bitte wählen Sie einen anderen Benutzernamen.");
     hb_session.close();
     return;
     }
          
     if (logger.isDebugEnabled())
     logger.debug("Neuer Eintrag, fuege hinzu!");

     // Passwort und Salt generieren
     String neuesPW = Password.generateRandomPassword(8);
     String salt = Password.generateRandomSalt();
     user.setPassword(Password.getSaltedPassword(neuesPW, salt, user.getUsername()));
     user.setSalt(salt);
     user.setActivated(false);
     user.setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));     
          
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
     hb_sessionS.close();*/
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

  /**
   * @return the statusrel
   */
  public Statusrel getStatusrel()
  {
    return statusrel;
  }

  /**
   * @param statusrel the statusrel to set
   */
  public void setStatusrel(Statusrel statusrel)
  {
    this.statusrel = statusrel;
  }
}
