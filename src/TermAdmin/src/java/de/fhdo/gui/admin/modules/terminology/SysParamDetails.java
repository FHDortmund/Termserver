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
package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.helper.DomainHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class SysParamDetails extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal iUpdateListener;
  private de.fhdo.terminologie.db.hibernate.SysParam sysParam;
  boolean newEntry = false;
  private List<DomainValue> validityDomainList;
  //private DomainValue selectedValidityDomain;
  
  private List<DomainValue> modifyLevelList;
  //private DomainValue selectedModifyLevel;
  private Session hb_sessionS;

  public SysParamDetails()
  {
    try
    {
      // Domain-Listen laden
      validityDomainList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_VALIDITYDOMAIN);
      modifyLevelList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_VALIDITYDOMAIN);
      
      Map args = Executions.getCurrent().getArg();

      long paramId = 0;

      try
      {
        paramId = Long.parseLong(args.get("sysparam_id").toString());
        logger.debug("SysParam-ID: " + paramId);
      }
      catch (Exception e)
      {
        logger.debug("Parameter 'sysparam_id' nicht gefunden");
      }

      logger.debug("SysParamDetails() - Konstruktor");

      if (paramId > 0)
      {
        newEntry = false;

        hb_sessionS = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();

        //person = PersonHelper.getInstance().getCurrentPatient();
        sysParam = (de.fhdo.terminologie.db.hibernate.SysParam) hb_sessionS.get(de.fhdo.terminologie.db.hibernate.SysParam.class, paramId);

        
      }
      else
      {
        // Neuer Eintrag
        newEntry = true;

        sysParam = new de.fhdo.terminologie.db.hibernate.SysParam();
        
        sysParam.setDomainValueByModifyLevel(DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN));
        sysParam.setDomainValueByValidityDomain(DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN));
        //selectedValidityDomain = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN);
        //selectedModifyLevel = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_VALIDITYDOMAIN);
      }

    }
    catch (Exception e)
    {
      logger.error("Fehler im Konstruktor: " + e.getMessage());
    }
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");
      
      //sysParam.setDomainValueByModifyLevel(selectedModifyLevel);
      //attachment.getAttachment().setTechnicalTypeCd(selectedTechnicalType.getDomainCode());
      

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      try
      {
        

        if (newEntry)
        {
          hb_session.save(sysParam);
        }
        else
        {
          if (logger.isDebugEnabled())
            logger.debug("Daten aktualisieren");
          
          hb_session.merge(sysParam);
        }

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
          logger.error("Fehler in onOkClicked() bei hibernate: " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      this.setVisible(false);

      if (iUpdateListener != null)
      {
        iUpdateListener.update(sysParam, !newEntry);
      }

      this.detach();
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in onOkClicked(): " + e.getMessage());
      e.printStackTrace();
      if(hb_sessionS != null)
      hb_sessionS.close();
    }
    if(hb_sessionS != null)
    hb_sessionS.close();
    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }

  public void afterCompose()
  {

    //Listbox contactListBox = (Listbox) getFellow("lbCommunication");
    //contactListBox.setModel(communicationListModel);
    //contactListBox.setItemRenderer(communicationRenderer);

    

    /*tb = (Textbox)getFellow("tb_Email");
     tb.setVisible(newEntry);*/

    //row = (Row) getFellow("row_kontakt");
    //row.setVisible(!newEntry);

    //row = (Row) getFellow("row_Email");
    //row.setVisible(newEntry);


    //de.fhdo.help.Help.getInstance().addHelpToWindow(this);
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
    if(hb_sessionS != null)
        hb_sessionS.close();
    //Executions.getCurrent().setAttribute("doctor_controller", null);
  }

  /**
   * @return the iUpdateListener
   */
  public IUpdateModal getiUpdateListener()
  {
    return iUpdateListener;
  }

  /**
   * @param iUpdateListener the iUpdateListener to set
   */
  public void setiUpdateListener(IUpdateModal iUpdateListener)
  {
    this.iUpdateListener = iUpdateListener;
  }

 

  /**
   * @return the sysParam
   */
  public de.fhdo.terminologie.db.hibernate.SysParam getSysParam()
  {
    return sysParam;
  }

  /**
   * @param sysParam the sysParam to set
   */
  public void setSysParam(de.fhdo.terminologie.db.hibernate.SysParam sysParam)
  {
    this.sysParam = sysParam;
  }

  /**
   * @return the validityDomainList
   */
  public List<DomainValue> getValidityDomainList()
  {
    return validityDomainList;
  }

  /**
   * @param validityDomainList the validityDomainList to set
   */
  public void setValidityDomainList(List<DomainValue> validityDomainList)
  {
    this.validityDomainList = validityDomainList;
  }

  /**
   * @return the modifyLevelList
   */
  public List<DomainValue> getModifyLevelList()
  {
    return modifyLevelList;
  }

  /**
   * @param modifyLevelList the modifyLevelList to set
   */
  public void setModifyLevelList(List<DomainValue> modifyLevelList)
  {
    this.modifyLevelList = modifyLevelList;
  }
}
