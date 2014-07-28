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

import com.csvreader.CsvWriter;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;

import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
public class ReportingKollab extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  GenericList genericListKollab;
  List<ReportingKollabData> listRepKollabData = new ArrayList<ReportingKollabData>(0);
  ArrayList<String> yearList = new ArrayList<String>();
  private int now;
  private static final int NUMBER_OF_YEARS_TO_SHOW_ON_EXPORT = 5;
  
  public ReportingKollab()
  {
      now = GregorianCalendar.getInstance().get(Calendar.YEAR);
  }
  
  private List<GenericListRowType> buildKollabStat(){
  
      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
        
        Session hb_session = de.fhdo.collaboration.db.HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();
            
        try
        {
          //1. Get Info
          String hql =  "select distinct dg from Discussiongroup dg";
                 hql += " join fetch dg.collaborationusers cu";
                 hql += " join fetch dg.privileges pri";
                 hql += " join fetch pri.proposal pro";
                 hql += " join fetch pro.proposalobjects probj";
                 hql += " group by dg.id";
                 
          Query q = hb_session.createQuery(hql);
          List<Discussiongroup> resList = q.list();
          
          Iterator<Discussiongroup> iter = resList.iterator();
          while(iter.hasNext()){
          
            Discussiongroup dg = (Discussiongroup)iter.next();
            ReportingKollabData rkd = new ReportingKollabData();
            ArrayList<String> activitiesPerYearList = new ArrayList<String>();
            Long nCSCounter = 0l;
            Long nVSCounter = 0l;
            Long nCCounter = 0l;
            Long nCMCounter = 0l;
            Long nAYearOne = 0l;
            Long nAYearTwo = 0l;
            Long nAYearThree = 0l;
            Long nAYearFour = 0l;
            Long nAYearFive = 0l;
            
            rkd.setGroupName(dg.getName()); //Gruppen Namen setzten
            
            for(Collaborationuser user:dg.getCollaborationusers()){
                if(user.getId().equals(dg.getHead())){
                    rkd.setCustodianName(user.getFirstName() + " " + user.getName()); //Gruppen Head setzen
                }
            }
            
            rkd.setNumberParticipants(String.valueOf(dg.getCollaborationusers().size())); //Anzahl der Discussiongroup Participants
            rkd.setNumberDiskussions(String.valueOf(dg.getPrivileges().size())); //jedes Proposal hat eine Diskussion
            
            for(Privilege priv:dg.getPrivileges()){
            
                Set<Proposalobject> poList = priv.getProposal().getProposalobjects();
               
                Calendar createdOn = Calendar.getInstance();
                createdOn.setTime(priv.getProposal().getCreated());
                int y = createdOn.get(Calendar.YEAR);
                if(y == now){
                    ++nAYearOne;
                }else if(y == (now-1)){
                    ++nAYearTwo;
                }else if(y == (now-2)){
                    ++nAYearThree;
                }else if(y == (now-3)){
                    ++nAYearFour;
                }else if(y == (now-4)){
                    ++nAYearFive;
                }
                
                for(Proposalobject po:poList){
                    if(po.getClassname().equals("CodeSystem")){
                        ++nCSCounter;
                    }else if(po.getClassname().equals("ValueSet")){
                        ++nVSCounter;
                    }else if(po.getClassname().equals("CodeSystemConcept")){
                        ++nCCounter;
                    }else if(po.getClassname().equals("ConceptValueSetMembership")){
                        ++nCMCounter; 
                    }
                }
            }
            
            rkd.setNumberCodeSystems(String.valueOf(nCSCounter));
            rkd.setNumberValueSets(String.valueOf(nVSCounter));
            rkd.setNumberConcepts(String.valueOf(nCCounter));
            rkd.setNumberConMemberships(String.valueOf(nCMCounter));
            
            activitiesPerYearList.add(String.valueOf(nAYearOne));
            activitiesPerYearList.add(String.valueOf(nAYearTwo));
            activitiesPerYearList.add(String.valueOf(nAYearThree));
            activitiesPerYearList.add(String.valueOf(nAYearFour));
            activitiesPerYearList.add(String.valueOf(nAYearFive));
            
            rkd.setActivitiesPerYear(activitiesPerYearList);

            listRepKollabData.add(rkd);
            GenericListRowType row = createRowFromKollabData(rkd);
            dataList.add(row);
          }  
        }
        catch (Exception e)
        {
          logger.error("Fehler bei ReportingDetails.java@ExportKollabStat() TermServHibUtil: " + e.getMessage());
        }
        finally
        {
          hb_session.close();
        }
      
        return dataList;
  }
  
  private GenericListRowType createRowFromKollabData(ReportingKollabData rkd)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[(8+rkd.getActivitiesPerYear().size())];
    cells[0] = new GenericListCellType(rkd.getGroupName(), false, "");
    cells[1] = new GenericListCellType(rkd.getCustodianName(), false, "");
    cells[2] = new GenericListCellType(rkd.getNumberParticipants(), false, "");
    cells[3] = new GenericListCellType(rkd.getNumberDiskussions(), false, "");
    cells[4] = new GenericListCellType(rkd.getNumberCodeSystems(), false, "");
    cells[5] = new GenericListCellType(rkd.getNumberValueSets(), false, "");
    cells[6] = new GenericListCellType(rkd.getNumberConcepts(), false, "");
    cells[7] = new GenericListCellType(rkd.getNumberConMemberships(), false, "");
    
    for(int i = 0;i < NUMBER_OF_YEARS_TO_SHOW_ON_EXPORT ;i++){
        
        if(i<rkd.getActivitiesPerYear().size()){
            cells[(8+i)] = new GenericListCellType(rkd.getActivitiesPerYear().get(i), false, "");
        }else{
            cells[(8+i)] = new GenericListCellType("-", false, "");
        }
    }
    row.setData(rkd);
    row.setCells(cells);

    return row;
  }
  
  private void initKollabStatList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType(ReportingKollabData.GROUP_NAME_Header, 190, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.CUSTODIAN_NAME_Header, 160, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_PARTICIPANTS_Header, 80, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_DISCUSSIONS_Header, 95, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_CodeSystems_Header, 100, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_ValueSets_Header, 80, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_Concepts_Header, 70, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_ConMemberships_Header, 145, "", true, "String", true, true, false, false));
      
      for(int i=0;i<NUMBER_OF_YEARS_TO_SHOW_ON_EXPORT;i++){
        header.add(new GenericListHeaderType(ReportingKollabData.NUMBER_ACTIVITIES_PreHeader + String.valueOf(now-i), 110, "", true, "String", true, true, false, false));
      }
      List<GenericListRowType> dataList  = new LinkedList<GenericListRowType>();
      
      // Liste initialisieren
      Include inc = (Include) getFellow("incKollabList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListKollab = (GenericList) winGenericList;

      genericListKollab.setListActions(this);
      genericListKollab.setButton_new(false);
      genericListKollab.setButton_edit(false);
      genericListKollab.setButton_delete(false);
      genericListKollab.setListHeader(header);
      genericListKollab.setDataList(dataList);
      
      ((Button)genericListKollab.getFellow("buttonNew")).setVisible(false);
      ((Button)genericListKollab.getFellow("buttonDelete")).setVisible(false);
      ((Button)genericListKollab.getFellow("buttonEdit")).setVisible(false);
      
      ((Button)getFellow("bKollabListExp")).setVisible(true);
      ((Button)getFellow("bKollabListExp")).setTooltip("Exportiert die generierte Statistik als CSV-Datei");
      ((Button)getFellow("bKollabListExp")).setDisabled(false);
      ((Button)getFellow("bKollabListExp")).setLabel("Statistik exportieren");
      ((Button)getFellow("bKollabListExp")).setImage("/rsc/img/symbols/email_go_16x16.png");

      ((Button)getFellow("bKollabListGen")).setVisible(true);
      ((Button)getFellow("bKollabListGen")).setTooltip("Generiert die Statistik");
      ((Button)getFellow("bKollabListGen")).setDisabled(false);
      ((Button)getFellow("bKollabListGen")).setLabel("Statistik generieren");
      ((Button)getFellow("bKollabListGen")).setImage("/rsc/img/symbols/refresh.png");
      
    }
    catch (Exception ex)
    {
      logger.error("Error in ReportingDetails.java initKollabStatList()" + ex.getMessage());
    }
  }

  public void exportKollabStat(){
  
      String fileName="";
      Long formatId = -1l;
      SimpleDateFormat sdfFilename = new SimpleDateFormat("yyyyMMdd");
      CsvWriter csv;
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      csv = new CsvWriter(bos, ';', Charset.forName("ISO-8859-1"));
      
      try{
      
        fileName = "_KollabStat";
        formatId = 194l;

        //Header erstellen
        csv.write(ReportingKollabData.GROUP_NAME_Header);
        csv.write(ReportingKollabData.CUSTODIAN_NAME_Header);
        csv.write(ReportingKollabData.NUMBER_PARTICIPANTS_Header);
        csv.write(ReportingKollabData.NUMBER_DISCUSSIONS_Header);
        csv.write(ReportingKollabData.NUMBER_CodeSystems_Header);
        csv.write(ReportingKollabData.NUMBER_ValueSets_Header);
        csv.write(ReportingKollabData.NUMBER_Concepts_Header);
        csv.write(ReportingKollabData.NUMBER_ConMemberships_Header);
        
        for(int i=0;i<NUMBER_OF_YEARS_TO_SHOW_ON_EXPORT;i++){
            csv.write(ReportingKollabData.NUMBER_ACTIVITIES_PreHeader + String.valueOf(now-i));
        }
        csv.endRecord();

        Iterator<ReportingKollabData> iter = listRepKollabData.iterator();
        while(iter.hasNext()){

            ReportingKollabData rkd = (ReportingKollabData)iter.next();

            csv.write(rkd.getGroupName());
            csv.write(rkd.getCustodianName());
            csv.write(rkd.getNumberParticipants());
            csv.write(rkd.getNumberDiskussions());
            csv.write(rkd.getNumberCodeSystems());
            csv.write(rkd.getNumberValueSets());
            csv.write(rkd.getNumberConcepts());
            csv.write(rkd.getNumberConMemberships());
            
            for(int i = 0;i < NUMBER_OF_YEARS_TO_SHOW_ON_EXPORT ;i++){

                if(i<rkd.getActivitiesPerYear().size()){
                    csv.write(rkd.getActivitiesPerYear().get(i));
                }else{
                    csv.write("-");
                }
            }
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
   
  public void onKollabGenClicked(){
  
      genericListKollab.setDataList(buildKollabStat());
  }
  
  public void onKollabExpClicked(){
  
      exportKollabStat();
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
    initKollabStatList();
  }

    public void onSelected(String id, Object data) {
        
    }
}
