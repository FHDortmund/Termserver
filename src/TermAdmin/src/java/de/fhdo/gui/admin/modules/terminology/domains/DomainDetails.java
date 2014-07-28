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
package de.fhdo.gui.admin.modules.terminology.domains;

import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.helper.DomainHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Row;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class DomainDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Domain domain;
  private Map args;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;
  private List<DomainValue> sortTypeList;
  private DomainValue selectedSortType;
  private String filterName = "";
  private Session hb_sessionS;

  public DomainDetails()
  {
    sortTypeList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_DISPLAY_ORDER);

    args = Executions.getCurrent().getArg();
    long domainId = 0;
    
    try
    {
      domainId = Long.parseLong(args.get("domain_id").toString());
      //domain_value_id = Long.parseLong(args.get("domain_value_id").toString());
    }
    catch(Exception ex)
    {
      
    }
    

    if (domainId > 0)
    {
      // Domain laden
      hb_sessionS = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      domain = (Domain) hb_sessionS.get(Domain.class, domainId);

      //hb_session.getTransaction().commit();
      

      if (domain.getDisplayOrder() != null)
        selectedSortType = DomainHelper.getInstance().getDomainValue(domain.getDisplayOrder());
    }

    if (domain == null)
    {
      domain = new Domain();

      newEntry = true;
    }

    if (selectedSortType == null)
    {
      // Standard-Werte auswaehlen
      selectedSortType = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_DISPLAY_ORDER);
    }
    if (selectedSortType == null)
      selectedSortType = new DomainValue();
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      if (getSelectedSortType() != null)
        domain.setDisplayOrder(getSelectedSortType().getDomainValueId());

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        if (newEntry)
        {
          if (logger.isDebugEnabled())
            logger.debug("Neuer Eintrag, fuege der Domain hinzu!");

          //Domain d = (Domain) hb_session.get(Domain.class, domainId);
          //d.setDomain(domain);

          hb_session.save(domain);
        }
        else
        {
          hb_session.merge(domain);
        }
      }
      catch (Exception e)
      {
        logger.error("Fehler in DomainDetails.java (onOkClicked()): " + e.getMessage());
      }

      hb_session.getTransaction().commit();
      hb_session.close();

      //hb_session.close();

      this.setVisible(false);
      this.detach();

      if (getUpdateListInterface() != null)
        getUpdateListInterface().update(getDomain(), !newEntry);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in DomainDetails.java: " + e.getMessage());
      e.printStackTrace();
      if(hb_sessionS != null)
      hb_sessionS.close();
    }
    if(hb_sessionS != null)
    hb_sessionS.close();
  }

  public void onCancelClicked()
  {
    //if (logger.isDebugEnabled())
    //    logger.debug("onCancelClicked()");
    this.setVisible(false);
    this.detach();
    if(hb_sessionS != null)
        hb_sessionS.close();
  }

  public void filterChangedName(InputEvent ie)
  {
    filterName = ie.getValue();
    //filterChanged(ie.getValue(), "domainDisplay", false);
    showStandardList();
  }

  private void filterChanged(String Value, String Key, boolean param)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("Filter changed!");
    }

    showStandardList();
  }

  public void filterRemove()
  {
    Bandbox bb = (Bandbox) getFellow("bbStandard");
    bb.setValue("-");

    domain.setDefaultValue("");
    //bb.close();
  }

  public void filterSelected(Object o)
  {
    logger.debug("filterSelected");

    //onSelect="bbStandard.value=self.selectedItem.getValue().getDomainDisplay(); bbStandard.close(); winDomainDetails.filterSelected(self.selectedItem.getValue());">

    logger.debug("Name: " + o.getClass().getCanonicalName());
    if (o instanceof Listitem)
    {
      DomainValue dv = (DomainValue) ((Listitem) o).getAttribute("object");
      //logger.debug("Name: " + ((Listitem)o).getValue().getClass().getCanonicalName());
      Bandbox bb = (Bandbox) getFellow("bbStandard");
      bb.setValue(dv.getDomainDisplay());

      domain.setDefaultValue("" + dv.getDomainValueId());
      bb.close();
    }


  }

  private void showDefaultValue()
  {
    Bandbox bb = (Bandbox) getFellow("bbStandard");

    if (domain.getDefaultValue() != null && domain.getDefaultValue().length() > 0)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try
      {
        Long dvId = Long.parseLong(domain.getDefaultValue());
        DomainValue dv = (DomainValue) hb_session.get(DomainValue.class, dvId);

        bb.setValue(dv.getDomainDisplay());
      }
      catch (Exception e)
      {
        logger.error("Fehler in DomainDetails.java (showDefaultValue()): " + e.getMessage());
      }

      
      hb_session.close();
    }
    else
    {
      bb.setValue("-");
    }
  }

  public void showStandardList()
  {

    //SelectEvent event;
    //event.getData()
    if (domain.getDomainId() > 0)
    {
      Listbox lb = (Listbox) getFellow("standardList");
      //lb.getSelectedItem().get

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try
      {
        String hql = "from DomainValue where domainId=" + domain.getDomainId();

        if (filterName.length() > 0)
          hql += " and domainDisplay like '%" + filterName + "%'";

        List<DomainValue> dvList = hb_session.createQuery(hql).list();

        // Filter
        /*if(filter.size() > 0 && filter.containsKey("domain_id") == false)
         {
         filter.put("domain_id", domain.getDomainId());
         }*/
        /*Set<String> keys = filter.keySet();
         Iterator<String> iterator = keys.iterator();

         //logger.debug("Anzahl Filter: " + keys.size());
         while (iterator.hasNext())
         {
         String key = iterator.next();
         //dvList = hb_session.createCriteria(DomainValue.class).add(Restrictions.like(key, filter.get(key) + "%")).list();
         dvList = hb_session.createCriteria(DomainValue.class).add(Restrictions.like(key, filter.get(key) + "%")).list();
         }*/


        lb.setModel(new SimpleListModel(dvList));
        lb.setItemRenderer(new ListitemRenderer()
        {
          private String formatString(Object s)
          {
            if (s == null)
              return "";
            else
              return s.toString();
          }

          public void render(Listitem lstm, Object o, int i) throws Exception
          {
            DomainValue item = (DomainValue) o;
            new Listcell(formatString(item.getDomainDisplay())).setParent(lstm);
            lstm.setAttribute("object", item);
          }
        });
      }
      catch (Exception e)
      {
        logger.error("Fehler in DomainDetails.java (showStandardList()): " + e.getMessage());
      }

      
      hb_session.close();
    }


  }

  /**
   * @return the domain
   */
  public Domain getDomain()
  {
    return domain;
  }

  /**
   * @param domain the domain to set
   */
  public void setDomain(Domain domain)
  {
    this.domain = domain;
  }

  /**
   * @return the updateListInterface
   */
  public IUpdateModal getUpdateListInterface()
  {
    return updateListInterface;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }

  /**
   * @return the sortTypeList
   */
  public List<DomainValue> getSortTypeList()
  {
    return sortTypeList;
  }

  /**
   * @param sortTypeList the sortTypeList to set
   */
  public void setSortTypeList(List<DomainValue> sortTypeList)
  {
    this.sortTypeList = sortTypeList;
  }

  /**
   * @return the selectedSortType
   */
  public DomainValue getSelectedSortType()
  {
    return selectedSortType;
  }

  /**
   * @param selectedSortType the selectedSortType to set
   */
  public void setSelectedSortType(DomainValue selectedSortType)
  {
    this.selectedSortType = selectedSortType;
  }

  public void afterCompose()
  {
    showDefaultValue();
    //showStandardList();

    if (domain == null || domain.getDomainId() == null || domain.getDomainId() == 0)
    {
      Row row = (Row) getFellow("defaultRow");
      row.setVisible(false);
    }

    //de.fhdo.help.Help.getInstance().addHelpToWindow(this);
  }
}
