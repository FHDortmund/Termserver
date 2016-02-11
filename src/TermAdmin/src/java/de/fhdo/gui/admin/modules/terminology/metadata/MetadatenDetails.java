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

import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.HQLParameterHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
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
  private long codeSystemId;
  private long valueSetId;

  public MetadatenDetails()
  {

    codeSystemId = ArgumentHelper.getWindowArgumentLong("codesystem_id");
    logger.debug("codeSystemId: " + codeSystemId);

    valueSetId = ArgumentHelper.getWindowArgumentLong("valueset_id");
    logger.debug("valueSetId: " + valueSetId);

    Object o = ArgumentHelper.getWindowArgument("mp");
    if (o != null)
    {
      metadataParameter = (MetadataParameter) o;
    }

    if (metadataParameter == null)
    {
      metadataParameter = new MetadataParameter();
      if (codeSystemId > 0)
      {
        metadataParameter.setCodeSystem(new CodeSystem());
        metadataParameter.getCodeSystem().setId(codeSystemId);
      }
      else if (valueSetId > 0)
      {
        metadataParameter.setValueSet(new ValueSet());
        metadataParameter.getValueSet().setId(valueSetId);
      }
      newEntry = true;
    }

    initDefaultValues();
  }

  public void afterCompose()
  {
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbParamType"), Definitions.DOMAINID_METADATAPARAMETER_TYPES, metadataParameter.getMetadataParameterType());
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbDatatype"), Definitions.DOMAINID_DATATYPES, metadataParameter.getParamDatatype());

    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbLanguage"), Definitions.DOMAINID_ISO_639_1_LANGUACECODES, metadataParameter.getLanguageCd());

  }

  private void initDefaultValues()
  {
    logger.debug("initDefaultValues()");

    if (newEntry || metadataParameter.getLanguageCd() == null)
    {
      // Codesystem lesen (für Default Language)
      Session hb_session = HibernateUtil.getSessionFactory().openSession();

      try
      {
        if (codeSystemId > 0)
        {
          CodeSystem cs_db = (CodeSystem) hb_session.get(CodeSystem.class, codeSystemId);

          for (CodeSystemVersion csv_db : cs_db.getCodeSystemVersions())
          {
            if (csv_db.getVersionId().longValue() == cs_db.getCurrentVersionId().longValue())
            {
              // aktuelles CodeSystem gefunden
              metadataParameter.setLanguageCd(csv_db.getPreferredLanguageCd());
              break;
            }
          }
        }

        if (valueSetId > 0)
        {
          ValueSet vs_db = (ValueSet) hb_session.get(ValueSet.class, valueSetId);

          for (ValueSetVersion vsv_db : vs_db.getValueSetVersions())
          {
            if (vsv_db.getVersionId().longValue() == vs_db.getCurrentVersionId().longValue())
            {
              // aktuelles ValueSet gefunden
              metadataParameter.setLanguageCd(vsv_db.getPreferredLanguageCd());
              break;
            }
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
    }

    if (metadataParameter.getLanguageCd() == null)
      metadataParameter.setLanguageCd("");

  }

  public void onOkClicked()
  {
    // speichern mit Hibernate
    try
    {
      boolean error = false;
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        if (metadataParameter.getParamName() == null || metadataParameter.getParamName().equals(""))
        {
          Messagebox.show(Labels.getLabel("valueMustExist"), Labels.getLabel("warning"), Messagebox.OK, Messagebox.EXCLAMATION);
          return;
        }

        // prüfen, ob Wert bereits existiert
        if (newEntry)
        {
          String hql = "";
          org.hibernate.Query q = null;

          if (codeSystemId > 0)
          {
            hql = "from MetadataParameter where codeSystemId=:cs_id and paramName=:name";
            q = hb_session.createQuery(hql);
            q.setParameter("cs_id", codeSystemId);
          }
          else if (valueSetId > 0)
          {
            hql = "from MetadataParameter where valueSetId=:vs_id and paramName=:name";
            q = hb_session.createQuery(hql);
            q.setParameter("vs_id", valueSetId);
          }

          if (q != null)
          {
            q.setParameter("name", metadataParameter.getParamName());
            List list = q.list();
            if (list != null && list.size() > 0)
            {
              Messagebox.show(Labels.getLabel("parameterExistsForCodesystem"), Labels.getLabel("warning"), Messagebox.OK, Messagebox.EXCLAMATION);
              return;
            }
          }
        }

        metadataParameter.setMetadataParameterType(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cbParamType")));
        metadataParameter.setParamDatatype(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cbDatatype")));
        metadataParameter.setLanguageCd(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cbLanguage")));

        if (newEntry)
        {
          if (logger.isDebugEnabled())
            logger.debug("Neuer Eintrag");

          // speichern
          hb_session.save(metadataParameter);

          if (codeSystemId > 0)
          {
            String hqlV = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity ";
            hqlV += "cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csv.codeSystem cs";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("cs.", "id",codeSystemId);

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
          }
          
          /*if(valueSetId > 0)
          {
            String hqlV = "select distinct csev from CodeSystemEntityVersion csev join csev.conceptValueSetMemberships cvsm ";
            hqlV += " join cvsm.valueSetVersion vsv join vsv.valueSet vs";

            HQLParameterHelper parameterHelper = new HQLParameterHelper();
            parameterHelper.addParameter("vs.", "id", valueSetId);

            // Parameter hinzufügen (immer mit AND verbunden)
            hqlV += parameterHelper.getWhere("");
            logger.debug("HQL: " + hqlV);

            // Query erstellen
            org.hibernate.Query qV = hb_session.createQuery(hqlV);

            // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
            parameterHelper.applyParameter(qV);

            List<CodeSystemEntityVersion> csevList = qV.list();
            Set<ValueSetMetadataValue> vsmvSet = new HashSet<ValueSetMetadataValue>(0);
            for (CodeSystemEntityVersion csev : csevList)
            {
              ValueSetMetadataValue vsmv = new ValueSetMetadataValue();
              vsmv.setMetadataParameter(metadataParameter);
              vsmv.setParameterValue("");
              vsmv.setValuesetVersionId(valueSetId);
              vsmv.setCodeSystemEntityVersion(csev);
              hb_session.save(vsmv);
              vsmvSet.add(vsmv);
            }

            metadataParameter.setValueSetMetadataValues(vsmvSet);
            hb_session.update(metadataParameter);
          }*/
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

        error = true;

        Messagebox.show(e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      if (error == false)
      {
        this.setVisible(false);
        this.detach();

        if (updateListInterface != null)
          updateListInterface.update(metadataParameter, !newEntry);
      }

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
  
  public void setLanguage(String code)
  {
    metadataParameter.setLanguageCd(code);
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbLanguage"), Definitions.DOMAINID_ISO_639_1_LANGUACECODES, metadataParameter.getLanguageCd());
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
