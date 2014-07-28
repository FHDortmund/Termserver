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
package de.fhdo.collaboration;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class UserDetails extends Window implements AfterCompose, IUpdate
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Collaborationuser user;
  //private Map args;
  private IUpdateModal updateListInterface;
  private Session hb_sessionS;

  public UserDetails()
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

      try
      {
        user = (Collaborationuser) hb_sessionS.get(Collaborationuser.class, userId);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      
    }

    if (user == null)
    {
      Messagebox.show("Benutzer nicht vorhanden!", "Achtung", Messagebox.OK, Messagebox.INFORMATION);
      this.setVisible(false);
      this.detach();
    }



  }

  public void afterCompose()
  {
    
  }
  
  public void changePassword()
  {
    try
    {
      logger.debug("erstelle Fenster...");

      Window win = (Window) Executions.createComponents(
        "/collaboration/passwordDialog.zul", null, null);

      ((PasswordDetails) win).setUpdateListInterface(this);

      logger.debug("öffne Fenster...");
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + this.getClass().getName()
        + "': " + ex.getMessage());
    }
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
        hb_session.merge(user);
        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        LoggingOutput.outputException(e, this);
      }

      hb_session.close();

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(user, true);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      LoggingOutput.outputException(e, this);
      hb_sessionS.close();
    }
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

  public void update(Object o)
  {
    if (o instanceof Collaborationuser)
    {
      user.setPassword(((Collaborationuser)o).getPassword());
      user.setSalt(((Collaborationuser)o).getSalt());
    }
   
  }
}
