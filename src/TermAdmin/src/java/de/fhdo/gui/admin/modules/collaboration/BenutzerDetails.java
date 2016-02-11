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
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IUpdateData;
import de.fhdo.logging.LoggingOutput;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class BenutzerDetails extends Window implements AfterCompose, IUpdateData //, EventListener<Event>
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Collaborationuser user;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  private GenericList genericList;

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
      logger.debug("load user with id: " + userId);

      // load data
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      try
      {
        user = (Collaborationuser) hb_session.get(Collaborationuser.class, userId);
        Hibernate.initialize(user.getRoles());
        Hibernate.initialize(user.getOrganisation());
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
      // new user
      user = new Collaborationuser();
      user.setEnabled(true);
      user.setHidden(false);
      user.setDeleted(false);
      user.setSendMail(true);
      //user.getRoles().clear();
      user.setRoles(new HashSet<Role>());
      //user.getRoles().add(new Role());
      user.setOrganisation(new Organisation());
      newEntry = true;
    }
  }

  public void afterCompose()
  {
    ((Textbox) getFellow("tb_Benutzername")).setReadonly(!newEntry);
    ((Checkbox) getFellow("cb_aktiv")).setChecked(!newEntry);
    ((Checkbox) getFellow("cb_aktiv")).setDisabled(newEntry);
    //cbUserRole = (Combobox) getFellow("cb_UserRole");

    initListRoles();
    //cbUserRole.setModel(CollabUserRoleHelper.getInstance().getListModelList());

    //CollabUserRoleHelper.getInstance().fillCombobox(cbUserRole, "", newEntry);
    //cbUserRole.addEventListener("onInitRenderLater", this);
  }

//  public void onEvent(Event event) throws Exception
//  {
//
//    if (user == null
//        || user.getRoles() == null
//        || user.getRoles().isEmpty() || user.getRoles().iterator().next() == null
//        || user.getRoles().iterator().next().getId() == null)
//      return;
//
//    Iterator<Comboitem> it = cbUserRole.getItems().iterator();
//    while (it.hasNext())
//    {
//      Comboitem ci = it.next();
//      if (user.getRoles().iterator().next().getId().compareTo(CollabUserRoleHelper.getCollabUserRoleIdByName(ci.getLabel())) == 0)
//      {
//        cbUserRole.setSelectedItem(ci);
//      }
//    }
//  }
  private GenericListRowType createRowFromRoleAssign(Role role, boolean isChecked)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];

    cells[0] = new GenericListCellType("", false, "");
    cells[1] = new GenericListCellType(role.getName(), false, "");

    row.setData(role);
    row.setCells(cells);

    return row;
  }

  private void initListRoles()
  {
    logger.debug("init role list");
    List<Role> roleList = CollabUserRoleHelper.getInstance().getCollabUserRoles();

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(" ", 40, "", false, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("role"), 0, "", false, "String", true, true, false, false));

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    for (Role role : roleList)
    {
      boolean isChecked = false;

      logger.debug("Check role for '" + role.getName() + "'");

      for (Role roleUser : user.getRoles())
      {
        if (roleUser.getId().longValue() == role.getId())
        {
          isChecked = true;
          break;
        }
      }

      dataList.add(createRowFromRoleAssign(role, isChecked));
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incListRoles");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    //genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setCheckable(true);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
    genericList.setUpdateDataListener(this);

    int count = 0;
    for (Role role : roleList)
    {
      boolean isChecked = false;
      logger.debug("Check role for '" + role.getName() + "'");
      for (Role roleUser : user.getRoles())
      {
        if (roleUser.getId().longValue() == role.getId())
        {
          isChecked = true;
          break;
        }
      }

      logger.debug("isChecked: " + isChecked);
      genericList.getListbox().getItemAtIndex(count).setSelected(isChecked);

      count++;
    }
  }

  private boolean userExists(String name, Session hb_session)
  {
    String hql = "from Collaborationuser where username=:user";
    Query q = hb_session.createQuery(hql);
    q.setParameter("user", name);
    List userList = q.list();
    if (userList != null && userList.size() > 0)
    {
      return true;
    }
    return false;
  }

  public void onOkClicked()
  {
    String mailResponse = "";

    // Pflichtfelder prüfen
    if (user.getUsername() == null || user.getUsername().length() == 0
        || user.getEmail() == null || user.getEmail().length() == 0 || user.getEmail().contains("@") == false
        || user.getOrganisation().getOrganisation() == null || user.getOrganisation().getOrganisation().equals(""))
    {
      Messagebox.show(Labels.getLabel("userMandatoryMsg"));
      return;
    }

    String neuesPW = "";
    if (newEntry)
    {
      // Passwort und Salt generieren
      neuesPW = Password.generateRandomPassword(8);
      user.setActivationMd5(MD5.getMD5(Password.generateRandomPassword(6)));
      String salt = Password.generateRandomSalt();
      user.setPassword(Password.getSaltedPassword(neuesPW, salt, user.getUsername(), 1000));
      user.setSalt(salt);
      user.setActivated(false);
      user.setActivationTime(new Date());

      mailResponse = Mail.sendMailCollaborationNewUser(user.getUsername(), neuesPW,
          user.getEmail(), user.getActivationMd5());

      if (mailResponse.length() == 0)
      {
        //Messagebox.show("Benutzer wurde erfolgreich angelegt und Aktivierungs-Email verschickt.");
      }
      else
      {
        Messagebox.show(Labels.getLabel("userCreateFailure") + ": " + mailResponse);
        return;
      }
    }

    // save data with Hibernate
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    Transaction tx = hb_session.beginTransaction();

    //boolean sessionClosed = false;

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      user.setRoles(new HashSet<Role>());
      for (Listitem li : genericList.getListbox().getSelectedItems())
      {
        Object object = li.getValue();
        Role selectedRole = (Role) ((GenericListRowType) object).getData();
        user.getRoles().add(selectedRole);
        logger.debug("add role: " + selectedRole.getName());
      }

      if (newEntry)
      {
        // prüfen, ob Benutzer bereits existiert
        if (userExists(user.getUsername(), hb_session))
        {
          Messagebox.show(Labels.getLabel("userAlreadyExistsMsg"));
          return;
        }

        if (logger.isDebugEnabled())
          logger.debug("Neuer Eintrag, fuege hinzu!");

        // TODO Rollen speichern
//          Role r = (Role) hb_session.get(Role.class, CollabUserRoleHelper.getCollabUserRoleIdByName(cbUserRole.getSelectedItem().getLabel()));
//          user.getRoles().clear();
//          user.getRoles().add(r);
        // Benutzer speichern
        hb_session.save(user);
        user.getOrganisation().getCollaborationusers().clear();
        user.getOrganisation().getCollaborationusers().add(user);

        hb_session.save(user.getOrganisation());

        tx.commit();
        Messagebox.show(Labels.getLabel("userCreateSuccess"));
        //logger.debug("Speicher Rollen...");
        // Benachrichtigung senden
//        mailResponse = Mail.sendMailCollaborationNewUser(user.getUsername(), neuesPW,
//            user.getEmail(), user.getActivationMd5());
//
//        if (mailResponse.length() == 0)
//        {
//          tx.commit();
//          Messagebox.show("Benutzer wurde erfolgreich angelegt und Aktivierungs-Email verschickt.");
//        }
//        else
//        {
//          Messagebox.show("Fehler beim Anlegen eines Benutzers: " + mailResponse);
//          tx.rollback();
//        }
        neuesPW = "                       ";
      }
      else
      {
//        TODO  Role r = (Role) hb_session.get(Role.class, CollabUserRoleHelper.getCollabUserRoleIdByName(cbUserRole.getSelectedItem().getLabel()));
//          user.getRoles().clear();
//          user.getRoles().add(r);
//          hb_session.merge(user);
//          hb_session.getTransaction().commit();

        // prüfen auf gelöschte Rollen
//          for (Role roleUser : user.getRoles())
//          {
//            boolean gefunden = false;
//
//            for (Listitem li : genericList.getListbox().getSelectedItems())
//            {
//              Object object = li.getValue();
//              Role role = (Role) ((GenericListRowType) object).getData();
//
//              if (roleUser.getId().longValue() == role.getId())
//              {
//                gefunden = true;
//                break;
//              }
//            }
//
//            if (gefunden == false)
//            {
//              logger.debug("Rolle gelöscht: " + roleUser.getName()+ ", ID: " + roleUser.getId());
//              hb_session.createSQLQuery("DELETE FROM role2collaborationuser WHERE id=" + roleUser.getId()).executeUpdate();
//            //user.getRoles().remove(role);
//              //hb_session.delete(role);
//            }
//          }
//
//          // prüfen auf neue Rollen
//          for (Listitem li : genericList.getListbox().getSelectedItems())
//          {
//            Object object = li.getValue();
//            DomainValue dv = (DomainValue) ((GenericListRowType) object).getData();
//
//            boolean gefunden = false;
//            for (RoleAssign role : user.getRoles())
//            {
//              if (role.getRoleCd().equals(dv.getDomainCode()))
//              {
//                gefunden = true;
//                break;
//              }
//            }
//
//            if (gefunden == false)
//            {
//              RoleAssign role = new RoleAssign();
//              role.setRoleCd(dv.getDomainCode());
//              role.setUser(user);
//              //user.getRoles().add(role);
//              logger.debug("Rolle hinzugefügt: " + role.getRoleCd());
//              hb_session.save(role);
//            }
//          }
        hb_session.merge(user);
        tx.commit();
      }

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(user, !newEntry);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session.close();
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

  public void onCellUpdated(int cellIndex, Object data, GenericListRowType row)
  {

  }

  public void nameChanged()
  {
    logger.debug("nameChanged(), newEntry: " + newEntry);

    try
    {
      if (newEntry)
      {
        logger.debug("build username");

        String username = ((Textbox) getFellow("tb_Benutzername")).getText();

        logger.debug("username: " + username);

        //if(username == null || username.length() == 0)
        String name = ((Textbox) getFellow("tbName")).getText();
        String vorname = ((Textbox) getFellow("tbVorname")).getText();

        if (name == null)
          name = "";
        if (vorname == null)
          vorname = "";

        logger.debug("name: " + name);
        logger.debug("vorname: " + vorname);

        if (vorname.length() > 0 && name.length() > 0)
          username = formatUsernameStr(vorname).substring(0, 1) + formatUsernameStr(name);
        else if (name.length() > 0)
          username = formatUsernameStr(name);

        logger.debug("new username: " + username);

        user.setUsername(username);
        ((Textbox) getFellow("tb_Benutzername")).setText(username);
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }

  }

  private String formatUsernameStr(String s)
  {
    if (s == null)
      return "";

    return s.replaceAll("ü", "ue")
        .replaceAll("ä", "ae")
        .replaceAll("ö", "oe")
        .replaceAll("Ü", "Ue")
        .replaceAll("Ä", "Ae")
        .replaceAll("Ö", "Oe")
        .replaceAll("ß", "ss")
        .toLowerCase();
  }
}
