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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicenceType;
import de.fhdo.terminologie.db.hibernate.LicencedUserId;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class LizenzDetails extends Window implements AfterCompose, IGenericListActions, IUpdate
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private de.fhdo.terminologie.db.hibernate.LicencedUser licencedUser;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  private boolean showValidTo, showValidFrom, showLicenceType;
  GenericList genericList;
  private List<LicenceType> licenceTypeList;

  public LizenzDetails()
  {
    logger.debug("LizenzDetails()");

    showValidTo = false;
    showValidFrom = false;

    long userId = ArgumentHelper.getWindowArgumentLong("user_id");
    long csvId = ArgumentHelper.getWindowArgumentLong("csv_id");

    //Object luId = ArgumentHelper.getWindowArgument("liceceduser_id");

    //logger.debug("liceceduser_id: " + luId);
    /*if(luId != null)
     {
     logger.debug("LU-Class: " + luId.getClass().getCanonicalName());
     }
     else logger.debug("LU ist null");*/

    //if (luId > 0)
    //if (luId != null && luId instanceof LicencedUserId)
    if (userId > 0 && csvId > 0)
    {
      // Domain laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        licencedUser = (de.fhdo.terminologie.db.hibernate.LicencedUser) hb_session.get(de.fhdo.terminologie.db.hibernate.LicencedUser.class, new LicencedUserId(userId, csvId));

        logger.debug("LU gelesen mit CSV: " + licencedUser.getCodeSystemVersion().getName());

        long cs_id = licencedUser.getCodeSystemVersion().getCodeSystem().getId();  // drin lassen wegen lazy loading
        logger.debug("cs_id: " + cs_id);
        logger.debug("cs_name: " + licencedUser.getCodeSystemVersion().getCodeSystem().getName());

        if (licencedUser.getValidFrom() != null)
          showValidFrom = true;
        if (licencedUser.getValidTo() != null)
          showValidTo = true;
        if (licencedUser.getLicenceType() != null)
          showLicenceType = true;
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

    if (licencedUser == null)
    {
      // Neuer Eintrag


      licencedUser = new de.fhdo.terminologie.db.hibernate.LicencedUser();
      licencedUser.setTermUser(new TermUser());
      licencedUser.getTermUser().setId(userId);


      newEntry = true;
    }

    if (licenceTypeList == null)
      licenceTypeList = new LinkedList<LicenceType>();

  }

  public void afterCompose()
  {
    //initCSVList();

    showCSVName();
    initLicenceTypeList();
  }

  private void initLicenceTypeList()
  {
    licenceTypeList = new LinkedList<LicenceType>();

    if (licencedUser != null && licencedUser.getCodeSystemVersion() != null)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        String hql = "from LicenceType where codeSystemVersionId=" + licencedUser.getCodeSystemVersion().getVersionId() + " order by typeTxt";
        logger.debug("HQL: " + hql);
        licenceTypeList = hb_session.createQuery(hql).list();


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

    logger.debug("size: " + licenceTypeList.size());

    // Combobox Model erstellen und anzeigen
    final Combobox cb = (Combobox) getFellow("comboLicenceType");
    cb.setModel(new ListModelList(licenceTypeList));

    cb.setItemRenderer(new ComboitemRenderer<LicenceType>()
    {
      public void render(Comboitem cmbtm, LicenceType licenceType, int i) throws Exception
      {
        cmbtm.setLabel(licenceType.getTypeTxt());
        cmbtm.setValue(licenceType);

        if (licencedUser.getLicenceType() != null
                && licenceType.getId().longValue() == licencedUser.getLicenceType().getId().longValue())
        {
          cb.setSelectedItem(cmbtm);
        }
        /*if (statusrel.getStatusByStatusIdFrom() != null && statusrel.getStatusByStatusIdFrom().getId().longValue() == t.getId().longValue())
         {
         cbStatusFrom.setSelectedItem(cmbtm);
         }*/
      }
    });

    // Auswahl anzeigen
    /*if (licencedUser.getLicenceType() != null)
     {
     for (int i = 0; i < licenceTypeList.size(); ++i)
     {
     if (licenceTypeList.get(i).getId().longValue() == licencedUser.getLicenceType().getId().longValue())
     {
     cb.setSelectedIndex(i);
     break;
     }
     }
     }*/
  }

  /**
   * Initialisiert die Liste mit Codesystemen. Wird erst geladen, wenn die
   * Bandbox geöffnet wird.
   *
   */
  public void initCSVList()
  {
    logger.debug("Fülle Liste mit Codesystemen...");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("codesystem"), 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("versionName"), 200, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "select distinct csv from CodeSystemVersion csv join fetch csv.codeSystem cs order by cs.name";
      List<CodeSystemVersion> csvList = hb_session.createQuery(hql).list();

      logger.debug("Anzahl: " + csvList.size());

      for (int i = 0; i < csvList.size(); ++i)
      {
        GenericListRowType row = createRow(csvList.get(i));
        dataList.add(row);
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

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
  }

  private GenericListRowType createRow(CodeSystemVersion csv)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(csv.getCodeSystem().getName(), false, "");
    cells[1] = new GenericListCellType(csv.getName(), false, "");

    row.setData(csv);
    row.setCells(cells);

    return row;
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      if (newEntry)
      {
        if (licencedUser.getCodeSystemVersion() == null
                || licencedUser.getCodeSystemVersion().getVersionId() == null
                || licencedUser.getCodeSystemVersion().getVersionId().longValue() == 0)
        {
          Messagebox.show(Labels.getLabel("selectCodesystemMsg2"));
          return;
        }
      }

      // Datumsangaben speichern
      if (showValidFrom == false)
      {
        licencedUser.setValidFrom(null);
      }
      if (showValidTo == false)
      {
        licencedUser.setValidTo(null);
      }

      // Lizenz-Typ
      if (showLicenceType)
      {
        try
        {
          Combobox cb = (Combobox) getFellow("comboLicenceType");
          licencedUser.setLicenceType((LicenceType) cb.getSelectedItem().getValue());
          logger.debug("Licencetype: " + licencedUser.getLicenceType().getId() + ", " + licencedUser.getLicenceType().getTypeTxt());
        }
        catch (Exception e)
        {
        }
      }
      else
      {
        licencedUser.setLicenceType(null);
        logger.debug("Licencetype: null");
      }


      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        hb_session.getTransaction().begin();
        if (newEntry)
        {
          if (logger.isDebugEnabled())
            logger.debug("Neuer Eintrag");

          licencedUser.setId(new LicencedUserId(licencedUser.getTermUser().getId(), licencedUser.getCodeSystemVersion().getVersionId()));

          hb_session.save(licencedUser);
        }
        else
        {
          hb_session.merge(licencedUser);
        }

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        logger.error("Fehler in LizenzDetails.java (onOkClicked()): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(licencedUser, !newEntry);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in DomainDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  public void editLicenceTypes()
  {
    try
    {
      logger.debug("CSV-ID: " + licencedUser.getCodeSystemVersion().getVersionId());
      
      Map map = new HashMap();
      map.put("csv_id", licencedUser.getCodeSystemVersion().getVersionId());

      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/lizenzTypen.zul", null, map);

      ((LizenzTypen) win).setUpdateInterface(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      Messagebox.show(Labels.getLabel("selectCodesystemMsg2"));
      
      //logger.debug("Fehler beim Öffnen der LizenzDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  /**
   * @return the licencedUser
   */
  public de.fhdo.terminologie.db.hibernate.LicencedUser getLicencedUser()
  {
    return licencedUser;
  }

  /**
   * @param licencedUser the licencedUser to set
   */
  public void setLicencedUser(de.fhdo.terminologie.db.hibernate.LicencedUser licencedUser)
  {
    this.licencedUser = licencedUser;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }

  /**
   * @return the showValidTo
   */
  public boolean isShowValidTo()
  {
    return showValidTo;
  }

  /**
   * @param showValidTo the showValidTo to set
   */
  public void setShowValidTo(boolean showValidTo)
  {
    this.showValidTo = showValidTo;
  }

  /**
   * @return the showValidFrom
   */
  public boolean isShowValidFrom()
  {
    return showValidFrom;
  }

  /**
   * @param showValidFrom the showValidFrom to set
   */
  public void setShowValidFrom(boolean showValidFrom)
  {
    this.showValidFrom = showValidFrom;
  }

  public void onNewClicked(String id)
  {
  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
  }

  public void onSelected(String id, Object data)
  {
    // Auswahl übernehmen
    if (data instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) data;
      logger.debug("CSV: " + csv.getName());

      licencedUser.setCodeSystemVersion(csv);

      // Name anzeigen
      showCSVName();

      initLicenceTypeList();
    }

  }

  private void showCSVName()
  {
    logger.debug("showCSVName(), newEntry: " + newEntry);

    Bandbox bb = (Bandbox) getFellow("bbStandard");
    bb.close();
    if (licencedUser != null && licencedUser.getCodeSystemVersion() != null)
      bb.setText(licencedUser.getCodeSystemVersion().getCodeSystem().getName() + " - " + licencedUser.getCodeSystemVersion().getName());
    else
      bb.setText("");

    if (newEntry == false)
    {
      bb.setReadonly(true);
      bb.setDisabled(true);
    }
  }

  /**
   * @return the showLicenceType
   */
  public boolean isShowLicenceType()
  {
    return showLicenceType;
  }

  /**
   * @param showLicenceType the showLicenceType to set
   */
  public void setShowLicenceType(boolean showLicenceType)
  {
    this.showLicenceType = showLicenceType;
  }

  /**
   * @return the licenceTypeList
   */
  public List<LicenceType> getLicenceTypeList()
  {
    return licenceTypeList;
  }

  /**
   * @param licenceTypeList the licenceTypeList to set
   */
  public void setLicenceTypeList(List<LicenceType> licenceTypeList)
  {
    this.licenceTypeList = licenceTypeList;
  }

  public void update(Object o)
  {
    initLicenceTypeList();
  }
}
