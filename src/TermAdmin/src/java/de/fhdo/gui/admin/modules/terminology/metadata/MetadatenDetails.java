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
package de.fhdo.gui.admin.modules.terminology.metadata;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.HQLParameterHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class MetadatenDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private MetadataParameter metadataParameter;
  private boolean newEntry = false;
  private IUpdateModal updateListInterface;

  public MetadatenDetails()
  {

    long codeSystemId = ArgumentHelper.getWindowArgumentLong("codesystem_id");
    logger.debug("codeSystemId: " + codeSystemId);

    Object o = ArgumentHelper.getWindowArgument("mp");
    if (o != null)
    {
      metadataParameter = (MetadataParameter) o;
    }

    //hb_session = HibernateSession.getSession();
    //hb_session = HibernateUtil.getSessionFactory().openSession();

    /*args = Executions.getCurrent().getArg();
     try
     {
     Object o = args.get("domain_id");
     logger.debug("o: " + o.toString());
     domainId = Long.parseLong(o.toString());
      
     domain_value_id = Long.parseLong(args.get("domain_value_id").toString());
     }
     catch (Exception e)
     {
     //e.printStackTrace();
     }*/

    if (metadataParameter == null)
    {
      metadataParameter = new MetadataParameter();
      metadataParameter.setCodeSystem(new CodeSystem());
      metadataParameter.getCodeSystem().setId(codeSystemId);
      newEntry = true;
    }

  }

  public void afterCompose()
  {
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
        if (metadataParameter.getParamName() == null || metadataParameter.getParamName().equals(""))
        {
          Messagebox.show("Es muss ein Wert angegeben werden!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
          return;
        }

        // prüfen, ob Wert bereits existiert
        String hql = "from MetadataParameter where codeSystemId=:cs_id and paramName=:name";
        org.hibernate.Query q = hb_session.createQuery(hql);
        q.setParameter("cs_id", metadataParameter.getCodeSystem().getId());
        q.setParameter("name", metadataParameter.getParamName());
        List list = q.list();
        if (list != null && list.size() > 0)
        {
          Messagebox.show("Dieser Parameter ist für das ausgewählte Code System bereits vorhanden!", "Achtung", Messagebox.OK, Messagebox.EXCLAMATION);
          return;
        }


        if (newEntry)
        {
           if (logger.isDebugEnabled())
             logger.debug("Neuer Eintrag");

           // speichern
           hb_session.save(metadataParameter);

           String hqlV = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity ";
           hqlV += "cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csv.codeSystem cs";

           HQLParameterHelper parameterHelper = new HQLParameterHelper();
           parameterHelper.addParameter("cs.", "id", metadataParameter.getCodeSystem().getId());

           // Parameter hinzufügen (immer mit AND verbunden)
           hqlV += parameterHelper.getWhere("");
           logger.debug("HQL: " + hqlV);

           // Query erstellen
           org.hibernate.Query qV = hb_session.createQuery(hqlV);

           // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
           parameterHelper.applyParameter(qV);

           List<CodeSystemEntityVersion> csevList = qV.list();
           Set<CodeSystemMetadataValue> csmvSet = new HashSet<CodeSystemMetadataValue>(0);
           for (CodeSystemEntityVersion csev : csevList)
           {

           CodeSystemMetadataValue csmv = new CodeSystemMetadataValue();
           csmv.setMetadataParameter(metadataParameter);
           csmv.setParameterValue("");
           csmv.setCodeSystemEntityVersion(csev);
           hb_session.save(csmv);
           csmvSet.add(csmv);
           }

           metadataParameter.setCodeSystemMetadataValues(csmvSet);
           hb_session.update(metadataParameter);

          //de.fhdo.db.hibernate.Domain domain = (de.fhdo.db.hibernate.Domain) hb_session.get(de.fhdo.db.hibernate.Domain.class, domainId);
          //domainValue.setDomain(domain);


          //hb_session.save(domainValue);
        }
        else
        {
          hb_session.merge(metadataParameter);
        }


        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
          logger.error("Fehler in MetadatenDetails.java in onOkClicked(): " + e.getMessage());
        e.printStackTrace();
      }
      finally
      {
        hb_session.close();
      }

      this.setVisible(false);
      this.detach();

      if (updateListInterface != null)
        updateListInterface.update(metadataParameter, !newEntry);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in MetadatenDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
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
   * @return the metadataParameter
   */
  public MetadataParameter getMetadataParameter()
  {
    return metadataParameter;
  }

  /**
   * @param metadataParameter the metadataParameter to set
   */
  public void setMetadataParameter(MetadataParameter metadataParameter)
  {
    this.metadataParameter = metadataParameter;
  }
}
