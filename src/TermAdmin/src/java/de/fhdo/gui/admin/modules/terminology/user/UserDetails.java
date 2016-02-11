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
package de.fhdo.gui.admin.modules.terminology.user;

import de.fhdo.authorization.Authorization;
import de.fhdo.authorization.IAuthorization;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * user details for Terminology Server no collaboration details here
 *
 * @author Robert Mützner
 */
public class UserDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private de.fhdo.terminologie.db.hibernate.TermUser user;
  private boolean newEntry = false;

  private IUpdateModal updateListInterface;

  public UserDetails()
  {
    logger.debug("UserDetails()");
    
    long userId = ArgumentHelper.getWindowArgumentLong("user_id");
    logger.debug("userId: " + userId);

    if (userId > 0)
    {
      // load user details
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      try
      {
        user = (de.fhdo.terminologie.db.hibernate.TermUser) hb_session.get(de.fhdo.terminologie.db.hibernate.TermUser.class, userId);
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

    if (user == null)
    {
      user = new de.fhdo.terminologie.db.hibernate.TermUser();
      newEntry = true;
    }

  }

  public void afterCompose()
  {
    getFellow("rowUserId").setVisible(!newEntry);

    ((Textbox) getFellow("tb_Benutzername")).setReadonly(!newEntry);
    
    showPasswordBox();

    //((Checkbox) getFellow("cb_aktiv")).setDisabled(newEntry);
    //((Checkbox) getFellow("cb_MailAktiv")).setDisabled(newEntry);
    /*tb_Email = (Textbox) getFellow("tb_Email");
     tb_Name = (Textbox) getFellow("tb_Name");
     cb_Benutzername = (Combobox) getFellow("cb_Benutzername");
     cb_Benutzername.setModel(CollabUserHelper.getListModelList());
     cb_Benutzername.addEventListener("onInitRenderLater", this);

     rComboUsername = (Row) getFellow("rComboUsername");
     rUsername = (Row) getFellow("rUsername");

     if (newEntry)
     {
     rComboUsername.setVisible(true);
     rUsername.setVisible(false);
     }
     else
     {
     rComboUsername.setVisible(false);
     rUsername.setVisible(true);
     }*/
    
  }
  
  public void showPasswordBox()
  {
    getFellow("rowPassword").setVisible(newEntry);
    getFellow("tb_Password").setVisible(newEntry && ((Checkbox)getFellow("cbPasswordMail")).isChecked() == false);
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      // Pflichtfelder beachten
      if (user.getName().length() == 0
              || user.getEmail().length() == 0)
      {
        Messagebox.show(Labels.getLabel("mandatoryFields"));
      }

      // Parameter füllen
      Map<String, String> param = new HashMap<String, String>();
      param.put("username", user.getName());
      param.put("mail", user.getEmail());
      param.put("isAdmin", user.isIsAdmin() ? "true" : "false");
      if(user.getId() != null && user.getId() > 0)
        param.put("userId", user.getId().toString());

      String password = null;
      
      if(newEntry)
      {
        if(((Checkbox)getFellow("cbPasswordMail")).isChecked() == false)
        {
          password = ((Textbox)getFellow("tb_Password")).getText();
        }
      }
      
      IAuthorization auth = Authorization.getAuthorizationClass();
      if (auth.createOrEditUser(param, newEntry, password))
      {
        if(newEntry)
          Messagebox.show(Labels.getLabel("userCreatedSuccess"));

        this.setVisible(false);
        this.detach();

        if (updateListInterface != null)
          updateListInterface.update(user, !newEntry);
      }
      else
      {
        Messagebox.show(Labels.getLabel("userCreateFailure2"));
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      LoggingOutput.outputException(e, this);
    }
    finally
    {

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
  public de.fhdo.terminologie.db.hibernate.TermUser getUser()
  {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(de.fhdo.terminologie.db.hibernate.TermUser user)
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
