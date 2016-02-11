/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.authorization;

import de.fhdo.helper.ArgumentHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.TermUser;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ResendPasswordDialog extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private String username;

  public ResendPasswordDialog()
  {
    username = ArgumentHelper.getWindowArgumentString("username");
    logger.debug("Username: " + username);
  }

  public void afterCompose()
  {

  }

  /**
   * Im Anmeldefenster wurde "Return" gedrückt
   *
   * @param event
   */
  public void onOkPressed(KeyEvent event)
  {
    if (logger.isDebugEnabled())
      logger.debug("Enter gedrueckt!");

    Button b = (Button) getFellow("okButton");
    b.setDisabled(true);

    onOkClicked();

    b.setDisabled(false);
  }

  public void onOkClicked()
  {
    boolean mail_success = false;
    String mail = ((Textbox) getFellow("tbEmail")).getText();

    // E-Mail Adresse mit Benutzer überprüfen
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    
    try
    {
      //logger.debug("User-ID: " + SessionHelper.getUserID());
      //User user_db = (User) hb_session.get(User.class, (int) SessionHelper.getUserID());
      org.hibernate.Query q = hb_session.createQuery("from TermUser where name=:username");
      q.setString("username", username);
      List<TermUser> list = (List<TermUser>) q.list();

      if (list.size() == 1)
      {
        TermUser user_db = list.get(0);

        if (user_db != null && user_db.getEmail()!= null && user_db.getEmail().equalsIgnoreCase(mail))
        {
          logger.debug("User: " + user_db.getName());
          mail_success = true;
        }
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

    if (mail_success)
    {
      boolean erfolg = Authorization.resendPassword(username);

      if (erfolg)
      {
        Messagebox.show(Labels.getLabel("newPasswortSendSuccessfully"),
                Labels.getLabel("newPassword"), Messagebox.OK, Messagebox.INFORMATION);
      }
      else
      {
        Messagebox.show(Labels.getLabel("newPasswordSendFailure"),
                Labels.getLabel("newPassword"), Messagebox.OK, Messagebox.EXCLAMATION);
      }
    }
    else
    {
      Messagebox.show(Labels.getLabel("noValidMail"),
                  Labels.getLabel("newPassword"), Messagebox.OK, Messagebox.EXCLAMATION);
    }

    this.setVisible(false);
    this.detach();
    //de.fhdo.gui.patientrecord.modules.masterdata.PasswordDetails cannot be cast to de.fhdo.gui.patientrecord.modules.masterdata.Mast

    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

}
