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

import com.csvreader.CsvWriter;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;

import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;


/**
 *
 * @author Philipp Urbauer
 */
public class ReportingTerm extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  GenericList genericListTerm;
  List<ReportingTermData> listRepTermData = new ArrayList<ReportingTermData>(0);
  
  public ReportingTerm()
  {
  }
  
  private List<GenericListRowType> buildTermStat(){
  
        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
        Session hb_session = de.fhdo.terminologie.db.HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin(); 
        try
        {
          //1.CS
          String hql_CS = "select distinct cs from CodeSystem cs join fetch cs.codeSystemVersions csv";
          Query q_CS = hb_session.createQuery(hql_CS);

          List<CodeSystem> resList_CS = q_CS.list();
          
          Iterator<CodeSystem> iterCs = resList_CS.iterator();
          while(iterCs.hasNext()){
          
              CodeSystem cs = (CodeSystem)iterCs.next();
              Map<Long,String> versionMap = new HashMap<Long, String>();
              List<Long> idList = new ArrayList<Long>();
              
              Iterator<CodeSystemVersion> iterCsv = cs.getCodeSystemVersions().iterator();
              while(iterCsv.hasNext()){
                  CodeSystemVersion csv = (CodeSystemVersion)iterCsv.next();
                  idList.add(csv.getVersionId());
              }
              Collections.sort(idList);
              for(int i=0;i<idList.size();i++){
              
                  versionMap.put(idList.get(i), String.valueOf(i+1));
              }
              
              iterCsv = cs.getCodeSystemVersions().iterator();
              while(iterCsv.hasNext()){
              
                  ReportingTermData rtd = new ReportingTermData();
                  CodeSystemVersion csv = (CodeSystemVersion)iterCsv.next();
                  
                  if(cs.getName() != null){
                      rtd.setVokabularyName(cs.getName());
                  }else{
                      rtd.setVokabularyName("");
                  }
                  if(csv.getName() != null){
                      rtd.setVersionName(csv.getName());
                  }else{
                      rtd.setVersionName("");
                  }
                  if(csv.getOid() != null){
                      rtd.setOid(csv.getOid());
                  }else{
                      rtd.setOid("");
                  }
                  rtd.setVersionnumber(versionMap.get(csv.getVersionId()));
                  rtd.setType("Codeliste");
                  //Number of Concepts
                  String hqlCseNumber = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.codeSystemEntity cse join fetch cse.codeSystemVersionEntityMemberships csvem";
                         hqlCseNumber += " join fetch csvem.codeSystemVersion csv where csv.versionId=:versionId";
                  Query q_CseNumber = hb_session.createQuery(hqlCseNumber);
                  q_CseNumber.setParameter("versionId", csv.getVersionId());
                  List<CodeSystemEntityVersion> csevList = q_CseNumber.list();
                  if(csevList == null || csevList.isEmpty()){
                      rtd.setNumberConcepts("0");
                  }else{
                      rtd.setNumberConcepts(String.valueOf(csevList.size()));
                  }
                  if(csv.getStatus() != null){
                      rtd.setStatus(String.valueOf(csv.getStatus()));
                  }else{
                      rtd.setStatus("");
                  }
                  if(csv.getReleaseDate() != null){
                      rtd.setReleaseDate(sdf.format(csv.getReleaseDate()));
                  }else{
                      rtd.setReleaseDate("");
                  }
                  
                  listRepTermData.add(rtd);
                  GenericListRowType row = createRowFromTermData(rtd);
                  dataList.add(row);
              }
          }

          //1.VS
          String hql_VS = "select distinct vs from ValueSet vs join fetch vs.valueSetVersions vsv";
          Query q_VS = hb_session.createQuery(hql_VS);

          List<ValueSet> resList_VS = q_VS.list();
          
          Iterator<ValueSet> iterVs = resList_VS.iterator();
          while(iterVs.hasNext()){
          
              ValueSet vs = (ValueSet)iterVs.next();
              Map<Long,String> versionMap = new HashMap<Long, String>();
              List<Long> idList = new ArrayList<Long>();
              
              Iterator<ValueSetVersion> iterVsv = vs.getValueSetVersions().iterator();
              while(iterVsv.hasNext()){
                  ValueSetVersion vsv = (ValueSetVersion)iterVsv.next();
                  idList.add(vsv.getVersionId());
              }
              Collections.sort(idList);
              for(int i=0;i<idList.size();i++){
              
                  versionMap.put(idList.get(i), String.valueOf(i+1));
              }
              
              iterVsv = vs.getValueSetVersions().iterator();
              while(iterVsv.hasNext()){
              
                  ReportingTermData rtd = new ReportingTermData();
                  ValueSetVersion vsv = (ValueSetVersion)iterVsv.next();
                  
                  if(vs.getName() != null){
                      rtd.setVokabularyName(vs.getName());
                  }else{
                      rtd.setVokabularyName("");
                  }
                  if(vsv.getName()!= null){
                      rtd.setVersionName(vsv.getName());
                  }else{
                      rtd.setVersionName("");
                  }
                  if(vsv.getOid() != null){
                      rtd.setOid(vsv.getOid());
                  }else{
                      rtd.setOid("");
                  }
                  rtd.setVersionnumber(versionMap.get(vsv.getVersionId()));
                  rtd.setType("ValueSet");
                  //Number of Concepts
                  String hqlCseNumber = "select distinct csev from CodeSystemEntityVersion csev join fetch csev.conceptValueSetMemberships cvsm join fetch cvsm.valueSetVersion vsv";
                         hqlCseNumber += " where vsv.versionId=:versionId";
                  Query q_CseNumber = hb_session.createQuery(hqlCseNumber);
                  q_CseNumber.setParameter("versionId", vsv.getVersionId());
                  List<CodeSystemEntityVersion> csevList = q_CseNumber.list();
                  if(csevList == null || csevList.isEmpty()){
                      rtd.setNumberConcepts("0");
                  }else{
                      rtd.setNumberConcepts(String.valueOf(csevList.size()));
                  }
                  if(vsv.getStatus() != null){
                      rtd.setStatus(String.valueOf(vsv.getStatus()));
                  }else{
                      rtd.setStatus("");
                  }
                  if(vsv.getReleaseDate() != null){
                      rtd.setReleaseDate(sdf.format(vsv.getReleaseDate()));
                  }else{
                      rtd.setReleaseDate("");
                  }
                  
                  listRepTermData.add(rtd);
                  GenericListRowType row = createRowFromTermData(rtd);
                  dataList.add(row);
              }
          }  
        }
        catch (Exception e)
        {
          logger.error("Fehler bei ReportingDetails.java@ExportTermStat() TermServHibUtil: " + e.getMessage());
        }
        finally
        {
          hb_session.close();
        }
      
        return dataList;
  }

  private GenericListRowType createRowFromTermData(ReportingTermData rtd)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[8];
    cells[0] = new GenericListCellType(rtd.getVokabularyName(), false, "");
    cells[1] = new GenericListCellType(rtd.getVersionName(), false, "");
    cells[2] = new GenericListCellType(rtd.getOid(), false, "");
    cells[3] = new GenericListCellType(rtd.getVersionnumber(), false, "");
    cells[4] = new GenericListCellType(rtd.getType(), false, "");
    cells[5] = new GenericListCellType(rtd.getNumberConcepts(), false, "");
    cells[6] = new GenericListCellType(rtd.getStatus(), false, "");
    cells[7] = new GenericListCellType(rtd.getReleaseDate(), false, "");

    row.setData(rtd);
    row.setCells(cells);

    return row;
  }
  
  private void initTermStatList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType(ReportingTermData.VOC_NAME_Header, 275, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.VERSION_NAME_Header, 275, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.OID_Header, 180, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.VERSION_NUMBER_Header, 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.TYPE_Header, 80, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.NUMBER_CONCEPTS_Header, 80, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.STATUS_Header, 70, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingTermData.PUBLIC_DATE_Header, 160, "", true, "String", true, true, false, false));

      List<GenericListRowType> dataList  = new LinkedList<GenericListRowType>();
      
      // Liste initialisieren
      Include inc = (Include) getFellow("incTermList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListTerm = (GenericList) winGenericList;

      genericListTerm.setListActions(this);
      genericListTerm.setButton_new(false);
      genericListTerm.setButton_edit(false);
      genericListTerm.setButton_delete(false);
      genericListTerm.setListHeader(header);
      genericListTerm.setDataList(dataList);
      
      ((Button)genericListTerm.getFellow("buttonEdit")).setVisible(false);
      ((Button)genericListTerm.getFellow("buttonDelete")).setVisible(false);
      ((Button)genericListTerm.getFellow("buttonNew")).setVisible(false);
      
      ((Button)getFellow("bTermListExp")).setVisible(true);
      ((Button)getFellow("bTermListExp")).setTooltip("Exportiert die generierte Statistik als CSV-Datei");
      ((Button)getFellow("bTermListExp")).setDisabled(false);
      ((Button)getFellow("bTermListExp")).setLabel("Statistik exportieren");
      ((Button)getFellow("bTermListExp")).setImage("/rsc/img/symbols/email_go_16x16.png");
      
      ((Button)getFellow("bTermListGen")).setVisible(true);
      ((Button)getFellow("bTermListGen")).setTooltip("Generiert die Statistik");
      ((Button)getFellow("bTermListGen")).setDisabled(false);
      ((Button)getFellow("bTermListGen")).setLabel("Statistik generieren");
      ((Button)getFellow("bTermListGen")).setImage("/rsc/img/symbols/refresh.png");

    }
    catch (Exception ex)
    {
      logger.error("Error in ReportingDetails.java initTermStatList()" + ex.getMessage());
    }
  }
  
  public void exportTermStat(){
  
      String fileName="";
      Long formatId = -1l;
      SimpleDateFormat sdfFilename = new SimpleDateFormat("yyyyMMdd");
      CsvWriter csv;
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      csv = new CsvWriter(bos, ';', Charset.forName("ISO-8859-1"));
      
      try{

        fileName = "_TermStat";
        formatId = 194l;

        //Header erstellen
        csv.write(ReportingTermData.VOC_NAME_Header);
        csv.write(ReportingTermData.VERSION_NAME_Header);
        csv.write(ReportingTermData.OID_Header);
        csv.write(ReportingTermData.VERSION_NUMBER_Header);
        csv.write(ReportingTermData.TYPE_Header);
        csv.write(ReportingTermData.NUMBER_CONCEPTS_Header);
        csv.write(ReportingTermData.STATUS_Header);
        csv.write(ReportingTermData.PUBLIC_DATE_Header);
        csv.endRecord();

        Iterator<ReportingTermData> iter = listRepTermData.iterator();
        while(iter.hasNext()){

            ReportingTermData rtd = (ReportingTermData)iter.next();

            csv.write(rtd.getVokabularyName());
            csv.write(rtd.getVersionName());
            csv.write(rtd.getOid());
            csv.write(rtd.getVersionnumber());
            csv.write(rtd.getType());
            csv.write(rtd.getNumberConcepts());
            csv.write(rtd.getStatus());
            csv.write("\"" + rtd.getReleaseDate() + "\"");
            csv.endRecord();
        }

        csv.close();
        
        if(bos.toByteArray() != null && !fileName.equals("") && !formatId.equals(-1l))
          downloadFile(formatId, bos.toByteArray() ,sdfFilename.format(new Date()) + fileName);

      }catch(Exception ex){
        logger.error("Error in ReportingDetails.java: " + ex.getMessage());
      }
  }
  
   private void downloadFile(long formatId, byte[] bytes, String name)
  {
    if (formatId == 193 || formatId == 195)
    {
      Filedownload.save(bytes,
              "application/xml",
              name + ".xml");
    }
    else if (formatId == 194)
    {
      Filedownload.save(bytes,
              "text/csv",
              name + ".csv");
    }
    else
    {
      Filedownload.save(bytes,
              "text/plain",
              name + ".txt");
    }
  }
  
  public void onTermExpClicked(){
  
      exportTermStat();
  }
  
  public void onTermGenClicked(){
  
      genericListTerm.setDataList(buildTermStat());
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

  public void afterCompose()
  {
    initTermStatList();
  }

    public void onSelected(String id, Object data) {
        
    }
}
