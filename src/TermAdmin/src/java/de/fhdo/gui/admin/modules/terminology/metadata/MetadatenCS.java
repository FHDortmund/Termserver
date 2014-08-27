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

import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.HQLParameterHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class MetadatenCS extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{
  enum Mode {CODESYSTEM, VALUESET};
  
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  byte[] bytes;
  GenericList genericListVocs;
  GenericList genericListMetadata;
  CodeSystem selectedCodeSystem;
  ValueSet selectedValueSet;
  MetadataParameter selectedMetadataParameter;
  
  private Mode mode;

  public MetadatenCS()
  {
    String modeStr = "";
    Object o = ArgumentHelper.getWindowParameter("mode");
    if(o != null)
      modeStr = o.toString();
    
    logger.debug("Mode: " + modeStr);
    
    if(modeStr.equals("vs"))
      mode = Mode.VALUESET;
    else mode = Mode.CODESYSTEM;
  }
  
  public void afterCompose()
  {
    fillVocabularyList();
    showStatus();
  }

  private void fillVocabularyList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {

        /*if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER))
        {

          ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();

          for (AssignedTerm at : myTerms)
          {

            if (at.getClassname().equals("CodeSystem"))
            {

              CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, at.getClassId());
              GenericListRowType row = createRowFromCodesystem(cs);
              dataList.add(row);
            }
          }
          Collections.sort(dataList, new ComparatorRowTypeName(true));
        }
        else*/
        
        if(mode == Mode.VALUESET)
        {
          String hql = "from ValueSet order by name";
          List<ValueSet> vsList = hb_session.createQuery(hql).list();

          for (int i = 0; i < vsList.size(); ++i)
          {
            ValueSet vs = vsList.get(i);
            GenericListRowType row = createRowFromValueSet(vs);

            dataList.add(row);
          }
        }
        else
        {
          String hql = "from CodeSystem order by name";
          List<CodeSystem> csList = hb_session.createQuery(hql).list();

          for (int i = 0; i < csList.size(); ++i)
          {
            CodeSystem cs = csList.get(i);
            GenericListRowType row = createRowFromCodesystem(cs);

            dataList.add(row);
          }
        }
      }
      catch (Exception e)
      {
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      //Vokabular Liste
      Include inc = (Include) getFellow("incVocList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListVocs = (GenericList) winGenericList;
      //genericListVocs.setId("0");

      genericListVocs.setListActions(this);
      genericListVocs.setButton_new(false);
      genericListVocs.setButton_edit(false);
      genericListVocs.setButton_delete(false);
      genericListVocs.setListHeader(header);
      genericListVocs.setDataList(dataList);
    }
    catch (Exception ex)
    {
      logger.error("Fehler in MetadatenCS.java: " + ex.getMessage());
    }
  }

  private void fillMetadataList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      //header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Typ", 100, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Sprache", 100, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Datentyp", 100, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        
        String hql = "from MetadataParameter mp";
        
        
        if(mode == Mode.VALUESET)
        {
          hql += " join fetch mp.valueSet vs";
          parameterHelper.addParameter("vs.", "id", selectedValueSet.getId());
        }
        else 
        {
          hql += " join fetch mp.codeSystem cs";
          parameterHelper.addParameter("cs.", "id", selectedCodeSystem.getId());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        hql += " order by mp.paramName";
        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        List<MetadataParameter> mpList = q.list();

        /*if (mpList.size() == 0)
         {
         ((Label) getFellow("labelStatus")).setValue("Keine Metadaten Parameter zu dieser Liste vorhanden!");
         }*/
        for (int i = 0; i < mpList.size(); ++i)
        {
          MetadataParameter mp = mpList.get(i);
          GenericListRowType row = createRowFromMetadataParameter(mp);

          dataList.add(row);
        }
      }
      catch (Exception e)
      {
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      //Metadaten Liste
      Include incMeta = (Include) getFellow("incMetadataList");
      Window winGenericListMeta = (Window) incMeta.getFellow("winGenericList");
      genericListMetadata = (GenericList) winGenericListMeta;
      genericListMetadata.setListId("metadataList");

      genericListMetadata.setListActions(this);
      genericListMetadata.setButton_new(true);
      genericListMetadata.setButton_edit(true);
      genericListMetadata.setButton_delete(true);
      genericListMetadata.setListHeader(header);
      genericListMetadata.setDataList(dataList);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private GenericListRowType createRowFromCodesystem(CodeSystem cs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(cs.getId(), false, "");
    cells[1] = new GenericListCellType(cs.getName(), false, "");

    row.setData(cs);
    row.setCells(cells);

    return row;
  }
  
  private GenericListRowType createRowFromValueSet(ValueSet vs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(vs.getId(), false, "");
    cells[1] = new GenericListCellType(vs.getName(), false, "");

    row.setData(vs);
    row.setCells(cells);

    return row;
  }

  private GenericListRowType createRowFromMetadataParameter(MetadataParameter mp)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[5];
    cells[0] = new GenericListCellType(mp.getId(), false, "");
    cells[1] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_METADATAPARAMETER_TYPES, mp.getMetadataParameterType()), false, "");
    cells[2] = new GenericListCellType(mp.getParamName(), false, "");
    cells[3] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_ISO_639_1_LANGUACECODES, mp.getLanguageCd()), false, "");
    cells[4] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_DATATYPES, mp.getParamDatatype()), false, "");

    row.setData(mp);
    row.setCells(cells);

    return row;
  }

  public void onSelected(String id, Object data)
  {
    if (data != null)
    {

      if (data instanceof CodeSystem)
      {
        selectedValueSet = null;
        selectedCodeSystem = (CodeSystem) data;
        logger.debug("Selected Codesystem: " + selectedCodeSystem.getName());
        fillMetadataList();

        showStatus();
      }
      else if (data instanceof ValueSet)
      {
        selectedCodeSystem = null;
        selectedValueSet = (ValueSet) data;
        logger.debug("Selected Valueset: " + selectedValueSet.getName());
        fillMetadataList();

        showStatus();
      }
      else if (data instanceof MetadataParameter)
      {
        selectedMetadataParameter = (MetadataParameter) data;
        logger.debug("Selected Metadata Parameter: " + selectedMetadataParameter.getParamName());

        showStatus();
      }
      else
      {
        logger.debug("data: " + data.getClass().getCanonicalName());
      }
    }
  }

  public void onNewClicked(String id)
  {
    try
    {
      if (id != null && id.equals("metadataList"))
      {
        Map map = new HashMap();
        if(mode == Mode.VALUESET)
          map.put("valueset_id", selectedValueSet.getId());
        else map.put("codesystem_id", selectedCodeSystem.getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/metadata/metadatenDetails.zul", null, map);

        ((MetadatenDetails) win).setUpdateListInterface(this);
        win.doModal();
      }

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

  }

  public void onEditClicked(String id, Object data)
  {
    try
    {
      if (id != null && id.equals("metadataList"))
      {
        MetadataParameter mp = (MetadataParameter) data;

        Map map = new HashMap();
        if(mode == Mode.VALUESET)
          map.put("valueset_id", selectedValueSet.getId());
        else map.put("codesystem_id", selectedCodeSystem.getId());
        
        map.put("mp", mp);

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/metadata/metadatenDetails.zul", null, map);

        ((MetadatenDetails) win).setUpdateListInterface(this);
        win.doModal();
      }

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  public void onDeleted(String id, Object data)
  {
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    try
    {
      MetadataParameter mp = (MetadataParameter) data;
      MetadataParameter mp_db = (MetadataParameter) hb_session.get(MetadataParameter.class, mp.getId());
      hb_session.delete(mp_db);

      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session.close();
    }
  }

  private void showStatus()
  {
    /*String s = "";

     if (selectedCodeSystem == null)
     {
     s = "\nBitte wählen Sie ein Codesystem aus.";
     }

     if (selectedCodeSystem != null)
     {
     s = "\nJetzt können Sie einen Metadaten Parameter auswählen.";
     }
     if (selectedMetadataParameter != null)
     {
     s = "\nMetadaten Parameter ausgewählt.";
     }*/

    //((Label) getFellow("labelStatus")).setValue(s);
  }

  

  public void update(Object o, boolean edited)
  {
    if (o != null && o instanceof MetadataParameter)
    {
      GenericListRowType row = createRowFromMetadataParameter((MetadataParameter) o);
      if (edited)
        genericListMetadata.updateEntry(row);
      else
        genericListMetadata.addEntry(row);
    }
  }
}
