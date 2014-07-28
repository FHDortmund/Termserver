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
import de.fhdo.helper.DeleteTermHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.South;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class SysParam extends Window implements AfterCompose, IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;

  public SysParam()
  {
    logger.debug("SysParam-Konstruktor");

    if (SessionHelper.isAdmin() == false)
    {
      Executions.getCurrent().sendRedirect("/gui/main/main.zul");
    }
  }

  private GenericListRowType createRowFromSysParam(de.fhdo.terminologie.db.hibernate.SysParam sysParam)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[6];
    cells[0] = new GenericListCellType(sysParam.getName(), false, "");
    cells[1] = new GenericListCellType(sysParam.getDomainValueByValidityDomain().getDomainDisplay(), false, "");
    cells[2] = new GenericListCellType(sysParam.getDomainValueByModifyLevel().getDomainDisplay(), false, "");
    cells[3] = new GenericListCellType(sysParam.getJavaDatatype(), false, "");
    cells[4] = new GenericListCellType(sysParam.getValue(), false, "");
    cells[5] = new GenericListCellType(sysParam.getDescription(), false, "");

    row.setData(sysParam);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    String[] filter = DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_VALIDITYDOMAIN);

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Name", 230, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Gültigkeitsbereich", 130, "", true, filter, true, true, false, false));
    header.add(new GenericListHeaderType("Modify-Level", 130, "", true, filter, true, true, false, false));
    header.add(new GenericListHeaderType("Datentyp", 80, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Wert", 700, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Beschreibung", 400, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      String hql = "from SysParam p join fetch p.domainValueByModifyLevel join fetch p.domainValueByValidityDomain where p.objectId is null order by p.name";
      List<de.fhdo.terminologie.db.hibernate.SysParam> paramList = hb_session.createQuery(hql).list();

      for (int i = 0; i < paramList.size(); ++i)
      {
        de.fhdo.terminologie.db.hibernate.SysParam sysParam = paramList.get(i);
        GenericListRowType row = createRowFromSysParam(sysParam);

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

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(true);
    genericList.setButton_edit(true);
    genericList.setButton_delete(true);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
  }

  public void afterCompose()
  {
    initList();
    
    South s = (South)getFellow("id_south");
    if(SessionHelper.getUserName().equals("urbauer_tadm")){
        s.setVisible(true);
    }
  }

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked()");
    //throw new UnsupportedOperationException("Not supported yet.");

    try
    {
      Window win = (Window) Executions.createComponents(
              "/gui/admin/modules/terminology/sysParamDetails.zul", null, null);

      ((SysParamDetails) win).setiUpdateListener(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der SysParamDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }

  }

  public void onEditClicked(String id, Object data)
  {
    logger.debug("onEditClicked()");
    //throw new UnsupportedOperationException("Not supported yet.");
    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.SysParam)
    {
      de.fhdo.terminologie.db.hibernate.SysParam sysParam = (de.fhdo.terminologie.db.hibernate.SysParam) data;
      logger.debug("Parameter: " + sysParam.getName());

      try
      {
        Map map = new HashMap();
        map.put("sysparam_id", sysParam.getId());

        Window win = (Window) Executions.createComponents(
                "/gui/admin/modules/terminology/sysParamDetails.zul", null, map);

        ((SysParamDetails) win).setiUpdateListener(this);

        win.doModal();
      }
      catch (Exception ex)
      {
        logger.debug("Fehler beim Öffnen der SysParamDetails: " + ex.getLocalizedMessage());
        ex.printStackTrace();
      }
    }
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof de.fhdo.terminologie.db.hibernate.SysParam)
    {
      de.fhdo.terminologie.db.hibernate.SysParam sysParam = (de.fhdo.terminologie.db.hibernate.SysParam) data;
      logger.debug("Person: " + sysParam.getName());

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        de.fhdo.terminologie.db.hibernate.SysParam sysParamDB = (de.fhdo.terminologie.db.hibernate.SysParam) hb_session.get(de.fhdo.terminologie.db.hibernate.SysParam.class, sysParam.getId());
        hb_session.delete(sysParamDB);

        hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
          logger.error("[" + this.getClass().getCanonicalName() + "] Fehler beim Löschen eines Eintrags: " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }
    }
  }

  public void onSelected(String id, Object data)
  {
  }

  public void update(Object o, boolean edited)
  {
    if (o != null && o instanceof de.fhdo.terminologie.db.hibernate.SysParam)
    {
      // Hier wird die neue Zeile erstellt und der Liste übergeben
      // dadurch wird nur diese 1 Zeile neu gezeichnet, nicht die ganze Liste
      de.fhdo.terminologie.db.hibernate.SysParam sysParam = (de.fhdo.terminologie.db.hibernate.SysParam) o;
      GenericListRowType row = createRowFromSysParam(sysParam);
        
      if (edited)
      {
        // Daten aktualisiert, jetzt dem Model übergeben
        logger.debug("Daten aktualisiert: " + sysParam.getName());
        
        genericList.updateEntry(row);
      }
      else
      {
        genericList.addEntry(row);
      }
    }
  }
  
  public void deleteTerm(){
  
    Checkbox cb_isCodeSystem = (Checkbox) getFellow("cbIsCodeSystem");
    Checkbox cb_isOnlyVersion = (Checkbox) getFellow("cbIsOnlyVersion");  
    Textbox tb_csvsId = (Textbox) getFellow("tb_CSVSId");
    Textbox tb_csvsVersionId = (Textbox) getFellow("tb_CSVSVersionId");
    String result="";
    if(cb_isCodeSystem.isChecked()){//CS
        result = DeleteTermHelper.deleteCS_CSV(cb_isOnlyVersion.isChecked(), Long.valueOf(tb_csvsId.getText()), Long.valueOf(tb_csvsVersionId.getText()));
    }else{//VS
        result = DeleteTermHelper.deleteVS_VSV(cb_isOnlyVersion.isChecked(), Long.valueOf(tb_csvsId.getText()), Long.valueOf(tb_csvsVersionId.getText()));
    }
    
    Messagebox.show("Übersicht:\n\n" + result, "Terminologie löschen", Messagebox.OK, Messagebox.INFORMATION);
  }
  
  //This method is for fixing Purposes ONLY for Tech-Admins!!
  public void fixIt(){
      /*
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        int i = 0;
        try
        {
            hb_session.getTransaction().begin();

            //Create metadata_parameter STATUS
            MetadataParameter mp = new MetadataParameter();
            mp.setParamName("STATUS");
            mp.setCodeSystem(new CodeSystem());
            mp.getCodeSystem().setId(169l);
            mp.setValueSet(null);
            hb_session.save(mp);
            
            String hqlCsev = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemConcepts csc join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv WHERE csv.versionId=:versionId";
            Query q_Csev = hb_session.createQuery(hqlCsev);
            q_Csev.setParameter("versionId", 178l);
            List<CodeSystemEntityVersion> csevList = q_Csev.list();
           
            for(CodeSystemEntityVersion csev:csevList){
                i++;
                CodeSystemMetadataValue csmv = new CodeSystemMetadataValue();
                csmv.setMetadataParameter(mp);
                csmv.setCodeSystemEntityVersion(csev);
                
                if(csev.getStatus() == 1){
                    csmv.setParameterValue("ACTIVE");
                }
                
                if(csev.getStatus() == 2){
                    csmv.setParameterValue("DEPRECATED");
                }
                
                if(csev.getStatus() == 3){
                    csmv.setParameterValue("DISCOURAGED");
                }
                
                if(csev.getStatus() == 4){
                    csmv.setParameterValue("TRIAL");
                }
                
                hb_session.save(csmv);
                csev.setStatus(1);
                hb_session.update(csev);
                System.out.println("--- Success_Done Nr: " + i);
            }
            hb_session.getTransaction().commit();
            Textbox tb_csvsId = (Textbox) getFellow("tb_CSVSId");
            tb_csvsId.setText("Success!: " + i);
        }
        catch (Exception e)
        {
            System.out.println("--- EXCEPTION_Done Nr: " + i);
            hb_session.getTransaction().rollback();
        }finally{
            hb_session.close();
        }
        */
  }
  
  public void fixItA(){
  
      /*  
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
        int i = 0;
        try
        {
            
            
            String hqlCsev = "select distinct csev.versionId from CodeSystemEntityVersion csev join csev.codeSystemConcepts csc join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv WHERE csv.versionId=:versionId AND csc.description IS NULL";
            Query q_Csev = hb_session.createQuery(hqlCsev);
            q_Csev.setParameter("versionId", 178l);
            List csevList = q_Csev.list();
            
            System.out.println("---Number of remaining Concepts: " + csevList.size());
            
            hb_session.getTransaction().begin();
            Iterator iter = csevList.iterator();
            while(iter.hasNext()){
                hb_session.getTransaction().begin();
                i++;
                Long id = (Long)iter.next();
                CodeSystemConcept csc_db = (CodeSystemConcept)hb_session.load(CodeSystemConcept.class,id);
                String text = "";           
                List l = null;
                l = hb_session.createSQLQuery("SELECT csmv.parameterValue FROM code_system_metadata_value csmv JOIN metadata_parameter mp ON mp.id = csmv.metadataParameterId WHERE csmv.codeSystemEntityVersionId=" + id + " AND mp.paramName='COMPONENT'").addScalar("csmv.parameterValue",Hibernate.TEXT).list();
                if(l.size() > 0)
                    text += (String)l.get(0) + " | ";
                l = hb_session.createSQLQuery("SELECT csmv.parameterValue FROM code_system_metadata_value csmv JOIN metadata_parameter mp ON mp.id = csmv.metadataParameterId WHERE csmv.codeSystemEntityVersionId=" + id + " AND mp.paramName='PROPERTY'").addScalar("csmv.parameterValue",Hibernate.TEXT).list();
                if(l.size() > 0)
                    text += (String)l.get(0) + " | ";
                l = hb_session.createSQLQuery("SELECT csmv.parameterValue FROM code_system_metadata_value csmv JOIN metadata_parameter mp ON mp.id = csmv.metadataParameterId WHERE csmv.codeSystemEntityVersionId=" + id + " AND mp.paramName='TIME_ASPCT'").addScalar("csmv.parameterValue",Hibernate.TEXT).list();
                if(l.size() > 0)
                    text += (String)l.get(0) + " | ";
                l = hb_session.createSQLQuery("SELECT csmv.parameterValue FROM code_system_metadata_value csmv JOIN metadata_parameter mp ON mp.id = csmv.metadataParameterId WHERE csmv.codeSystemEntityVersionId=" + id + " AND mp.paramName='SYSTEM'").addScalar("csmv.parameterValue",Hibernate.TEXT).list();
                if(l.size() > 0)
                    text += (String)l.get(0) + " | ";
                l = hb_session.createSQLQuery("SELECT csmv.parameterValue FROM code_system_metadata_value csmv JOIN metadata_parameter mp ON mp.id = csmv.metadataParameterId WHERE csmv.codeSystemEntityVersionId=" + id + " AND mp.paramName='SCALE_TYP'").addScalar("csmv.parameterValue",Hibernate.TEXT).list();
                if(l.size() > 0)
                    text += (String)l.get(0) + " | ";
                l = hb_session.createSQLQuery("SELECT csmv.parameterValue FROM code_system_metadata_value csmv JOIN metadata_parameter mp ON mp.id = csmv.metadataParameterId WHERE csmv.codeSystemEntityVersionId=" + id + " AND mp.paramName='METHOD_TYP'").addScalar("csmv.parameterValue",Hibernate.TEXT).list();
                if(l.size() > 0)
                    text += (String)l.get(0) + " | ";
                
                text = text.substring(0, text.length()-2);
                csc_db.setDescription(text);
                hb_session.update(csc_db);
                System.out.println("--- Success_Done Nr: " + i);
                hb_session.getTransaction().commit();
            }
            Textbox tb_csvsId = (Textbox) getFellow("tb_CSVSId");
            tb_csvsId.setText("Success!: " + i);
        }
        catch (Exception e)
        {
            hb_session.getTransaction().rollback();
            System.out.println("--- EXCEPTION_Done Nr: " + i);
        }finally{
            hb_session.close();
        }
        */
  }
}
