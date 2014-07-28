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
package de.fhdo.gui.info;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Organisation;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.helper.CollaborationuserHelper;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.Password;
import java.util.List;
import java.util.UUID;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class Enquiry extends Window implements AfterCompose
{

  private static final String ENQUIRY_TYPE_General_Enquiry = "Allgemeine Anfrage";
  private static final String ENQUIRY_TYPE_Discussion_Participant = "Anfrage zur Registrierung als Diskussionsteilnehmer";
  private static final String ENQUIRY_TYPE_Content_Admin = "Anfrage zur Registrierung als Inhaltsverwalter";
    
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Collaborationuser user;
  private Collaborationuser userExt;
  private Organisation userOrg;
  private Organisation userExtOrg;
  private de.fhdo.collaboration.db.classes.Enquiry enquiry;
  private String selectedLabel = Enquiry.ENQUIRY_TYPE_General_Enquiry;
  private boolean extraPerson = false;

  public Enquiry()
  {
      enquiry = new de.fhdo.collaboration.db.classes.Enquiry();
      user = new Collaborationuser();
      user.setEnabled(false);
      user.setHidden(true);
      user.setSendMail(true);
      userOrg = new Organisation();
      userExt = new Collaborationuser();
      userExt.setEnabled(false);
      userExt.setHidden(true);
      userExt.setSendMail(true);
      userExtOrg = new Organisation();
      
      enquiry.setCollaborationuser(user);
      user.setOrganisation(userOrg);
      userExt.setOrganisation(userExtOrg);
  }

  public void afterCompose()
  {
   
  }
  
  public void setSelection(String selectedLabel){
      this.selectedLabel = selectedLabel;
      if(selectedLabel.equals(Enquiry.ENQUIRY_TYPE_General_Enquiry)){
          
          //((Row)getFellow("rVocName")).setVisible(false);
          //((Row)getFellow("rVocDescription")).setVisible(false);
          ((Row)getFellow("row_Username")).setVisible(false);
          ((Row)getFellow("row_Username")).setVisible(false);
          ((Row)getFellow("row_TermName")).setVisible(false);
          ((Row)getFellow("row_TermDescription")).setVisible(false);
          ((Row)getFellow("row_ExtendedInfo")).setVisible(false);
          ((Row)getFellow("row_IntendedValidityRange")).setVisible(false);
          ((Row)getFellow("row_MoreExtendedInfo")).setVisible(false);
          ((Row)getFellow("row_UsernameExt")).setVisible(false);
          ((Row)getFellow("row_VornameExt")).setVisible(false);
          ((Row)getFellow("row_NachnameExt")).setVisible(false);
          ((Row)getFellow("row_OrganisationExt")).setVisible(false);
          ((Row)getFellow("row_EmailExt")).setVisible(false);
          ((Row)getFellow("row_PhoneExt")).setVisible(false);
          
      }
      
      if(selectedLabel.equals(Enquiry.ENQUIRY_TYPE_Discussion_Participant)){
          
          ((Row)getFellow("row_Username")).setVisible(true);
          ((Row)getFellow("row_ExtendedInfo")).setVisible(true);
          ((Row)getFellow("row_TermName")).setVisible(true);
          ((Row)getFellow("row_TermDescription")).setVisible(false);
          ((Row)getFellow("row_IntendedValidityRange")).setVisible(false);
          ((Row)getFellow("row_MoreExtendedInfo")).setVisible(false);
          ((Row)getFellow("row_UsernameExt")).setVisible(false);
          ((Row)getFellow("row_VornameExt")).setVisible(false);
          ((Row)getFellow("row_NachnameExt")).setVisible(false);
          ((Row)getFellow("row_OrganisationExt")).setVisible(false);
          ((Row)getFellow("row_EmailExt")).setVisible(false);
          ((Row)getFellow("row_PhoneExt")).setVisible(false);
      }
      
      if(selectedLabel.equals(Enquiry.ENQUIRY_TYPE_Content_Admin)){
      
          ((Row)getFellow("row_Username")).setVisible(true);
          ((Row)getFellow("row_ExtendedInfo")).setVisible(true);
          ((Row)getFellow("row_TermName")).setVisible(true);
          ((Row)getFellow("row_TermDescription")).setVisible(true);
          ((Row)getFellow("row_IntendedValidityRange")).setVisible(true);
          ((Row)getFellow("row_MoreExtendedInfo")).setVisible(true);   
          ((Row)getFellow("row_UsernameExt")).setVisible(true);        
          ((Row)getFellow("row_VornameExt")).setVisible(true);         
          ((Row)getFellow("row_NachnameExt")).setVisible(true);        
          ((Row)getFellow("row_OrganisationExt")).setVisible(true);    
          ((Row)getFellow("row_EmailExt")).setVisible(true);           
          ((Row)getFellow("row_PhoneExt")).setVisible(true);           
      }
  }
  
  public void onOkClicked(){
  
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
          // Allg. Pflichtfelder prüfen
          if(user.getFirstName() == null || user.getFirstName().length() == 0 ||
             user.getName() == null || user.getName().length() == 0 ||
             user.getOrganisation().getOrganisation() == null || user.getOrganisation().getOrganisation().length() == 0 ||
             user.getEmail() == null || user.getEmail().length() == 0 || user.getEmail().contains("@") == false ||
             user.getPhone() == null || user.getPhone().length() == 0 ||
             enquiry.getRequestDescription() == null || enquiry.getRequestDescription().length() == 0){
            
            Messagebox.show("Achtung bitte füllen Sie alle rot markierten Pflichtfelder korrekt aus!", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
            hb_session.close();
            return;
          }
          if(selectedLabel.equals(Enquiry.ENQUIRY_TYPE_General_Enquiry)){
            //Generate default user for General enquiries
            user.setUsername(UUID.randomUUID().toString());
            user.getRoles().add(CollaborationuserHelper.getCollaborationuserRoleByName(CODES.ROLE_REZENSENT));
          }
          if(selectedLabel.equals(Enquiry.ENQUIRY_TYPE_Discussion_Participant)){

              // Pflichtfelder prüfen
            if(user.getUsername() == null || user.getUsername().length() == 0){

              Messagebox.show("Achtung bitte füllen Sie alle rot markierten Pflichtfelder aus!", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
              hb_session.close();
              return;
            }
            
            user.getRoles().add(CollaborationuserHelper.getCollaborationuserRoleByName(CODES.ROLE_BENUTZER));
          }
          
          if(selectedLabel.equals(Enquiry.ENQUIRY_TYPE_Content_Admin)){
          
            if((userExt.getUsername() != null && userExt.getUsername().length() != 0) ||
               (userExt.getFirstName() != null && userExt.getFirstName().length() != 0) ||
               (userExt.getName() != null && userExt.getName().length() != 0) ||
               (userExt.getOrganisation().getOrganisation() != null && userExt.getOrganisation().getOrganisation().length() != 0) ||
               (userExt.getEmail() != null && userExt.getEmail().length() != 0) ||     
               (userExt.getPhone() != null && userExt.getPhone().length() != 0)){
               
                if( userExt.getUsername() == null || userExt.getUsername().length() == 0 || 
                    userExt.getFirstName() == null || userExt.getFirstName().length() == 0 ||
                    userExt.getName() == null || userExt.getName().length() == 0 ||
                    userExt.getOrganisation().getOrganisation() == null || userExt.getOrganisation().getOrganisation().length() == 0 ||
                    userExt.getEmail() == null || userExt.getEmail().length() == 0 || userExt.getEmail().contains("@") == false ||
                    userExt.getPhone() == null || userExt.getPhone().length() == 0){
                    
                    Messagebox.show("Wenn Sie den Inhaltsverwalter extra angeben, \n dann bitte alle Felder ausfüllen!", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
                    hb_session.close();
                    return;

                }else{
                    extraPerson = true;
                    user.setUsername(UUID.randomUUID().toString());
                    user.getRoles().add(CollaborationuserHelper.getCollaborationuserRoleByName(CODES.ROLE_REZENSENT));
                    userExt.getRoles().add(CollaborationuserHelper.getCollaborationuserRoleByName(CODES.ROLE_INHALTSVERWALTER));
                }
            }else{
                if(user.getUsername() == null || user.getUsername().length() == 0){
                  Messagebox.show("Achtung bitte füllen Sie alle rot markierten Pflichtfelder aus!", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
                  hb_session.close();
                  return;
                }
                user.getRoles().add(CollaborationuserHelper.getCollaborationuserRoleByName(CODES.ROLE_INHALTSVERWALTER));
            }  
          }
          /*
          /*Textbox tb = (Textbox) getFellow("tfCaptcha");
          org.zkforge.bwcaptcha.Captcha captcha = (org.zkforge.bwcaptcha.Captcha) getFellow("cpa");
          if(!captcha.getValue().toLowerCase().equals(tb.getValue().toLowerCase())){
          
              Messagebox.show("Sicherheitsabfrage fehlerhaft! \n Bitte versuchen Sie es erneut.", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
              hb_session.close();
              return;
          }*/
          
          // prüfen, ob Benutzer bereits existiert
          String hql = "from Collaborationuser where username=:user";
          Query q = hb_session.createQuery(hql);
          q.setParameter("user", user.getUsername());
          List userList = q.list();
          if(userList != null && userList.size() > 0)
          {
            Messagebox.show("Benutzers existiert bereits. Bitte wählen Sie einen anderen Benutzernamen.");
            //hb_session.getTransaction().commit();
            hb_session.close();
            return;
          }
          
          String neuesPW = Password.generateRandomPassword(8);
          String salt = Password.generateRandomSalt();
          user.setPassword(Password.getSaltedPassword(neuesPW, salt, user.getUsername()));
          user.setSalt(salt);
          
          enquiry.setRequestType(selectedLabel);
          enquiry.setClosedFlag(false);
          
          if(extraPerson){
            
            // prüfen, ob Benutzer bereits existiert
            String hqlExt = "from Collaborationuser where username=:user";
            Query qExt = hb_session.createQuery(hqlExt);
            qExt.setParameter("user", userExt.getUsername());
            List userListExt = qExt.list();
            if(userListExt != null && userListExt.size() > 0)
            {
              Messagebox.show("Benutzers für den Inhaltsverwalter existiert bereits. Bitte wählen Sie einen anderen Benutzernamen.");
              //hb_session.getTransaction().commit();
              hb_session.close();
              return;
            }  
            
            String neuesPWExt = Password.generateRandomPassword(8);
            String saltExt = Password.generateRandomSalt();
            userExt.setPassword(Password.getSaltedPassword(neuesPWExt, saltExt, userExt.getUsername()));
            userExt.setSalt(saltExt);
            
            enquiry.setCollaborationuserExtPerson(userExt);
              
            hb_session.save(enquiry.getCollaborationuser().getOrganisation());
            hb_session.save(enquiry.getCollaborationuser());
            hb_session.save(enquiry.getCollaborationuserExtPerson());
            hb_session.save(enquiry.getCollaborationuserExtPerson().getOrganisation());
            hb_session.save(enquiry);
          }else{
          
            hb_session.save(enquiry.getCollaborationuser().getOrganisation());
            hb_session.save(enquiry.getCollaborationuser());
            enquiry.setCollaborationuserExtPerson(null);
            hb_session.save(enquiry);
          }
          
          hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        logger.error("Fehler in Enquiry.java (onOkClicked()): " + e.getMessage());
      }finally{

        hb_session.close();
      }
      String[] adr = new String[1];
      adr[0]=user.getEmail();
      Mail.sendMailAUT(adr, "Terminologieserver: Ihre Anfrage", "Ihre Anfrage wurde erhalten und wird bearbeitet.");
      
      if(extraPerson){
      
          String[] adrExt = new String[1];
          adrExt[0]=userExt.getEmail();
          Mail.sendMailAUT(adrExt, "Terminologieserver: Inhaltsverwalter Nominierung", "Sie wurden von " + user.getFirstName() + " " + user.getName() + " zum Inhaltsverwalter nominiert. \nEin/Eine Terminologieadministrator/in wird mit Ihnen Kontakt aufnehmen.");
      }
      
      //Messagebox.show("Ihre Anfrage wird bearbeitet! \n\n Sie werden nun auf die Startseite umgeleitet!",
      Messagebox.show("Ihre Anfrage wird bearbeitet! \n\n Sie werden nun auf die Startseite der Publikationsumgebung umgeleitet!",
                      "Anfrage Erfolgreich", Messagebox.OK, Messagebox.INFORMATION, new org.zkoss.zk.ui.event.EventListener() {
        public void onEvent(Event evt) throws InterruptedException {
            if (evt.getName().equals("onOK")) {
                
                /*Productive_AT_PU*************************************************************************************************************************/  
                /**/ Executions.getCurrent().sendRedirect("../../../TermBrowser/gui/main/main.zul");                           // test
                /**/ //Executions.getCurrent().sendRedirect("https://termpub.gesundheit.gv.at/TermBrowser/gui/main/main.zul");  // public & kollab prod
                /******************************************************************************************************************************************/ 
                
            }
        }
      });
    }
    catch (Exception e)
    {
      logger.error("Fehler in Enquiry.java: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  public void onCancelClicked(){
      
    Executions.getCurrent().sendRedirect("../../../TermBrowser/gui/main/main.zul");
  }

    public Collaborationuser getUser() {
        return user;
    }

    public void setUser(Collaborationuser user) {
        this.user = user;
    }

    public Collaborationuser getUserExt() {
        return userExt;
    }

    public void setUserExt(Collaborationuser userExt) {
        this.userExt = userExt;
    }

    public de.fhdo.collaboration.db.classes.Enquiry getEnquiry() {
        return enquiry;
    }

    public void setEnquiry(de.fhdo.collaboration.db.classes.Enquiry enquiry) {
        this.enquiry = enquiry;
    }
}
