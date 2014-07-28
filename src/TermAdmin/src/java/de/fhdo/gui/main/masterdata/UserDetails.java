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
package de.fhdo.gui.main.masterdata;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class UserDetails extends Window implements AfterCompose, IUpdate
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private TermUser user;
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
        user = (TermUser) hb_sessionS.get(TermUser.class, userId);
      }
      catch (Exception e)
      {
        logger.debug("Fehler in UserDetails.java: " + e.getMessage());
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
        "/gui/main/masterdata/passwordDialog.zul", null, null);

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
        logger.debug("Fehler in UserDetails.java @ onOkClicked() innerCatch: " + e.getMessage());
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
      logger.debug("Fehler in UserDetails.java @ onOkClicked() outerCatch: " + e.getMessage());
      if(hb_sessionS != null)
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
  public TermUser getUser()
  {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(TermUser user)
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
    if (o instanceof TermUser)
    {
      user.setPassw(((TermUser)o).getPassw());
      user.setSalt(((TermUser)o).getSalt());
    }
   
  }
}
