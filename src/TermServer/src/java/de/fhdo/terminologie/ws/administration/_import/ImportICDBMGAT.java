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
package de.fhdo.terminologie.ws.administration._import;

import com.csvreader.CsvReader;
import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.*;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersion;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.types.FilecontentListEntry;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.VersioningType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Philipp Urbauer
 */
public class ImportICDBMGAT
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;
  private CodeSystemVersion csVersion;
  
  private boolean onlyCSV = true; //Only CSV for this case
  private Long csId = 0L;
  private Long csvId = 0L;
  private String resultStr = "";

  public ImportICDBMGAT(ImportCodeSystemRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportICDAT gestartet ======");

    parameter = _parameter;
  }

  public String importICDBMGAT(ImportCodeSystemResponseType reponse)
  {

    String s = "";
    Date date = new Date();
    int count = 0, countFehler = 0;
    CsvReader csv;
    
    try{

        //Erstelle Strukturkapitel
        List<String> chapComSep = new ArrayList<String>();
        List<String> subChapComSep = new ArrayList<String>();
        List<String> dreierComSep = new ArrayList<String>();

        Map<String,Long> linkingMapChap = new HashMap<String, Long>();
        Map<String,Long> linkingMapSubChap = new HashMap<String, Long>();
        Map<String,Long> linkingMapDreisteller = new HashMap<String, Long>();

        //Kapitel
        byte[] kapitelBytes = null;

        //Prepare Maps & Lists (exclusive Katalog)
        List<FilecontentListEntry> fileList = parameter.getImportInfos().getFileContentList();
        for(FilecontentListEntry fle:fileList){

            if(fle.getCode() == 0){
                kapitelBytes = fle.getContent();
            }else{
                byte[] bytes = fle.getContent();
                logger.debug("wandle zu InputStream um...");
                InputStream is = new ByteArrayInputStream(bytes);

                //csv = new CsvReader("C:\\Temp\\notfallrel_diagnosen.csv");
                csv = new CsvReader(is, Charset.forName("ISO-8859-1"));
                csv.setDelimiter(';');
                csv.setTextQualifier('"');
                csv.setUseTextQualifier(true);    
                csv.readHeaders();
                logger.debug("Anzahl Header: " + csv.getHeaderCount());

                if(fle.getCode() == 1 || //Dreisteller
                   fle.getCode() == 2 || //Unterkapitel
                   fle.getCode() == 3){  //Kapitel

                    while(csv.readRecord()){

                        if(fle.getCode() == 1)
                            dreierComSep.add(csv.getRawRecord());

                        if(fle.getCode() == 2)
                            subChapComSep.add(csv.getRawRecord());

                        if(fle.getCode() == 3)
                            chapComSep.add(csv.getRawRecord());
                    }
                }

                csv.close();
            }
        }  
        
      //Build CS, CSV, Chapters, Supchapters and linking
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createCodeSystem(hb_session) == false)
        {
          // Fehlermeldung
          hb_session.getTransaction().commit();
          hb_session.close();
          return "Code System konnte nicht erstellt werden!";
        }
        
        csId = parameter.getCodeSystem().getId();
        csvId = parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId();
        
        // MetadatenParameter speichern => ICD BMG Specific
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        for (int i=0;i<11;i++)
        {
            
          String mdText ="";
          MetadataParameter mp = null;
          if(i==0){
            mdText = "Kennzeichen";
          }
          if(i==1){
            mdText = "Gruppe";
          }
          if(i==2){
            mdText = "HD_Gruppe";
          }
          if(i==3){
            mdText = "ZD_Gruppe";
          }
          if(i==4){
            mdText = "Geschlecht";
          }
          if(i==5){
            mdText = "Mindestalter";
          }
          if(i==6){
            mdText = "Höchstalter";
          }
          if(i==7){
            mdText = "Unwahrscheinlich";
          }
          if(i==8){
            mdText = "HD_ambulant";
          }
          if(i==9){
            mdText = "HD_>0_Tage";
          }
          if(i==10){
            mdText = "Meldepflicht";
          }
          if(i==11){
            mdText = "Aktion";
          }
          
          //Check if parameter already set in case of new Version!
          String hql = "select distinct mp from MetadataParameter mp";
          hql += " join fetch mp.codeSystem cs";

          HQLParameterHelper parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("mp.", "paramName", mdText);

          // Parameter hinzufügen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");
          logger.debug("HQL: " + hql);

          // Query erstellen
          org.hibernate.Query q = hb_session.createQuery(hql);
          // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
          parameterHelper.applyParameter(q);

          List<MetadataParameter> mpList= q.list(); 
          for(MetadataParameter mParameter:mpList){

              if(mParameter.getCodeSystem().getId().equals(parameter.getCodeSystem().getId()))
                  mp = mParameter;
          }
          
          if(mp == null){
            
            mp = new MetadataParameter();
            mp.setParamName(mdText);
            mp.setCodeSystem(parameter.getCodeSystem());
            hb_session.save(mp);
          } 
          
          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit Codesystem-ID: " + mp.getCodeSystem().getId() + ", MD-ID: " + mp.getId());
        }
        
        //Adding Version Information
        Iterator<CodeSystemVersion> iter = parameter.getCodeSystem().getCodeSystemVersions().iterator();
        
        while(iter.hasNext()){
        
            csVersion = (CodeSystemVersion)iter.next();
            csVersion.setInsertTimestamp(date);
            csVersion.setOid("");
            csVersion.setReleaseDate(date);
            csVersion.setStatus(1);
            csVersion.setStatusDate(date);
            csVersion.setValidityRange(235l); //Default Value for empfohlen
        }
        //VersionUpdate mit UpdateCodeSystemVersion am Ende der Methode
       
        
        //Erstelle Struktur (Hauptkapitel, Unterkapitel, Dreisteller)
        //Erstellen von Hauptkapitel
        for(String chap: chapComSep)
        {
          String[] chapInfo = chap.split(";");
          CreateConceptRequestType request = new CreateConceptRequestType();
          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();
          String code = "";
          
          if(chapInfo[0].equals("01")){
            code = "I";
          }else if(chapInfo[0].equals("02")){
            code = "II";
          }else if(chapInfo[0].equals("03")){
            code = "III";
          }else if(chapInfo[0].equals("04")){
            code = "IV";
          }else if(chapInfo[0].equals("05")){
            code = "V";
          }else if(chapInfo[0].equals("06")){
            code = "VI";
          }else if(chapInfo[0].equals("07")){
            code = "VII";
          }else if(chapInfo[0].equals("08")){
            code = "VIII";
          }else if(chapInfo[0].equals("09")){
            code = "IX";
          }else if(chapInfo[0].equals("10")){
            code = "X";
          }else if(chapInfo[0].equals("11")){
            code = "XI";
          }else if(chapInfo[0].equals("12")){
            code = "XII";
          }else if(chapInfo[0].equals("13")){
            code = "XIII";
          }else if(chapInfo[0].equals("14")){
            code = "XIV";
          }else if(chapInfo[0].equals("15")){
            code = "XV";
          }else if(chapInfo[0].equals("16")){
            code = "XVI";
          }else if(chapInfo[0].equals("17")){
            code = "XVII";
          }else if(chapInfo[0].equals("18")){
            code = "XVIII";
          }else if(chapInfo[0].equals("19")){
            code = "XIX";
          }else if(chapInfo[0].equals("20")){
            code = "XX";
          }else if(chapInfo[0].equals("21")){
            code = "XXI";
          }else if(chapInfo[0].equals("22")){
            code = "XXII";
          }else{}
          
          csc.setCode(code);
          csc.setIsPreferred(true);
          csc.setTerm(chapInfo[1]);
          csc.setTermAbbrevation("");

          logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
          membership.setIsMainClass(Boolean.TRUE);
          membership.setIsAxis(Boolean.FALSE);
          
          request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
          request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);
          
          if (csc.getCode().length() == 0)
          {
            if (csc.getTerm().length() > 0)
            {
              if (csc.getTerm().length() > 98)
                csc.setCode(csc.getTerm().substring(0, 98));
              else
                csc.setCode(csc.getTerm());
            }
          }
          else if (csc.getCode().length() > 98)
          {
            csc.setCode(csc.getCode().substring(0, 98));
          }

          if (csc.getCode().length() > 0)
          {
            // Entity-Version erstellen
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            csev.setStatusVisibility(1);
            csev.setIsLeaf(false);
            csev.setEffectiveDate(date);

            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session, "");

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {   //Auf gar keinen Fall csc.getCode() hier!
                linkingMapChap.put(chapInfo[0], response.getCodeSystemEntity().getCurrentVersionId());
                count++;
                System.out.println("C_: " + count);
            }
            else
              countFehler++;
          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht gegeben");
          }
        }
        
        //Building subChapters
        for(String subChap: subChapComSep)
        {
          
          String[] chapInfo = subChap.split(";");
          CreateConceptRequestType request = new CreateConceptRequestType();
          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(chapInfo[2] + "-" + chapInfo[3]);
          csc.setIsPreferred(true);
          csc.setTerm(chapInfo[1]);
          csc.setTermAbbrevation("");

          logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
          membership.setIsMainClass(Boolean.FALSE);
          membership.setIsAxis(Boolean.FALSE);
          
          request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
          request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);
          
          if (csc.getCode().length() == 0)
          {
            if (csc.getTerm().length() > 0)
            {
              if (csc.getTerm().length() > 98)
                csc.setCode(csc.getTerm().substring(0, 98));
              else
                csc.setCode(csc.getTerm());
            }
          }
          else if (csc.getCode().length() > 98)
          {
            csc.setCode(csc.getCode().substring(0, 98));
          }

          if (csc.getCode().length() > 0)
          {
            // Entity-Version erstellen
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            csev.setStatusVisibility(1);
            csev.setIsLeaf(false);
            csev.setEffectiveDate(date);

            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session, "");

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
                linkingMapSubChap.put(csc.getCode(), response.getCodeSystemEntity().getCurrentVersionId());
                count++;
                System.out.println("C_: " + count);
                //Zusätzlich linking
                //getParentId
                String[] subChapCode = chapInfo[0].split("\\.");
                Long parentVersionId = linkingMapChap.get(subChapCode[0]);
                
                CodeSystemEntityVersionAssociation      association                = new CodeSystemEntityVersionAssociation();         
                association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(parentVersionId);
                association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(response.getCodeSystemEntity().getCurrentVersionId());
                association.setAssociationKind(2); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                association.setLeftId(parentVersionId); // immer linkes Element also csev1
                association.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                association.getAssociationType().setCodeSystemEntityVersionId(4L);
                // Weitere Attribute setzen
                association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                association.setStatusDate(new Date());
                association.setInsertTimestamp(new Date());
                // Beziehung abspeichern
                hb_session.save(association);
                //System.out.println("C_: " + count);
            }
            else
              countFehler++;
          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht gegeben");
          }
        }
        
        //Erstelle Dreisteller
        for(String dreierChap: dreierComSep)
        {
          
          String[] chapInfo = dreierChap.split(";");
          CreateConceptRequestType request = new CreateConceptRequestType();
          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(chapInfo[0]);
          csc.setIsPreferred(true);
          csc.setTerm(chapInfo[1]);
          csc.setTermAbbrevation("");

          logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

          CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
          membership.setIsMainClass(Boolean.FALSE);
          membership.setIsAxis(Boolean.FALSE);
          
          request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
          request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);
          
          if (csc.getCode().length() == 0)
          {
            if (csc.getTerm().length() > 0)
            {
              if (csc.getTerm().length() > 98)
                csc.setCode(csc.getTerm().substring(0, 98));
              else
                csc.setCode(csc.getTerm());
            }
          }
          else if (csc.getCode().length() > 98)
          {
            csc.setCode(csc.getCode().substring(0, 98));
          }

          if (csc.getCode().length() > 0)
          {
            // Entity-Version erstellen
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            csev.setStatusVisibility(1);
            csev.setIsLeaf(false);
            csev.setEffectiveDate(date);

            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session, "");

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
                linkingMapDreisteller.put(csc.getCode(), response.getCodeSystemEntity().getCurrentVersionId());
                count++;
                System.out.println("C_: " + count);
                //Zusätzlich linking
                //getParentId
                Long parentVersionId = linkingMapSubChap.get(chapInfo[2]);
                
                CodeSystemEntityVersionAssociation      association                = new CodeSystemEntityVersionAssociation();         
                association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(parentVersionId);
                association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(response.getCodeSystemEntity().getCurrentVersionId());
                association.setAssociationKind(2); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                association.setLeftId(parentVersionId); // immer linkes Element also csev1
                association.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                association.getAssociationType().setCodeSystemEntityVersionId(4L);
                // Weitere Attribute setzen
                association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                association.setStatusDate(new Date());
                association.setInsertTimestamp(new Date());
                // Beziehung abspeichern
                hb_session.save(association);
                //System.out.println("C_: " + count);
            }
            else
              countFehler++;
          }
          else
          {
            countFehler++;
            logger.debug("Term ist nicht gegeben");
          }
        }
        
        //Add catalog
        logger.debug("wandle zu InputStream um...");
        InputStream iS = new ByteArrayInputStream(kapitelBytes);
        
        //csv = new CsvReader("C:\\Temp\\notfallrel_diagnosen.csv");
        csv = new CsvReader(iS, Charset.forName("ISO-8859-1"));
        csv.setDelimiter(';');
        csv.setTextQualifier('"');
        csv.setUseTextQualifier(true);

        csv.readHeaders();
        logger.debug("Anzahl Header: " + csv.getHeaderCount());
        
        while (csv.readRecord())
        {
          
          //Check if Code Exists
          //if yes update => no new version
          if(csv.get("Diagnose").contains(".")){
              
            CreateConceptRequestType request = new CreateConceptRequestType();

            request.setLoginToken(parameter.getLoginToken());
            request.setCodeSystem(parameter.getCodeSystem());
            request.setCodeSystemEntity(new CodeSystemEntity());
            request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

            CodeSystemConcept csc = new CodeSystemConcept();

            csc.setCode(csv.get("Diagnose"));
            csc.setIsPreferred(true);
            csc.setTerm(csv.get("Bezeichnung"));
            csc.setTermAbbrevation("");
            csc.setDescription(csv.get("Kurztext"));

            logger.debug("Code: " + csc.getCode() + ", Term: " + csc.getTerm());

            CodeSystemVersionEntityMembership membership = new CodeSystemVersionEntityMembership();
            membership.setIsMainClass(Boolean.FALSE);
            membership.setIsAxis(Boolean.FALSE);

            request.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
            request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(membership);

            if (csc.getCode().length() == 0)
            {
              if (csc.getTerm().length() > 0)
              {
                if (csc.getTerm().length() > 98)
                  csc.setCode(csc.getTerm().substring(0, 98));
                else
                  csc.setCode(csc.getTerm());
              }
            }
            else if (csc.getCode().length() > 98)
            {
              csc.setCode(csc.getCode().substring(0, 98));
            }

            if (csc.getCode().length() > 0)
            {
              // Entity-Version erstellen
              CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
              csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
              csev.getCodeSystemConcepts().add(csc);
              csev.setStatusVisibility(1);
              csev.setIsLeaf(true);            
              csev.setEffectiveDate(date);

              // Entity-Version dem Request hinzufügen
              request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

              // Dienst aufrufen (Konzept einfügen)
              CreateConcept cc = new CreateConcept();
              CreateConceptResponseType response = cc.CreateConcept(request, hb_session, "");

              if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
              {
                  count++;
                  System.out.println("C_: " + count);
                  //Association einfügen
                  Long parentVersionId = -1l;
                  for(Map.Entry<String, Long> entry : linkingMapDreisteller.entrySet()){
                      String[] sp = csv.get("Diagnose").split("\\.");
                      if(entry.getKey().equals(sp[0]))
                          parentVersionId = entry.getValue();
                  }
                  if(parentVersionId != -1l){
                  CodeSystemEntityVersionAssociation      association                = new CodeSystemEntityVersionAssociation();         
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(parentVersionId);
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(response.getCodeSystemEntity().getCurrentVersionId());
                  association.setAssociationKind(2); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                  association.setLeftId(parentVersionId); // immer linkes Element also csev1
                  association.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                  association.getAssociationType().setCodeSystemEntityVersionId(4L);
                  // Weitere Attribute setzen
                  association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                  association.setStatusDate(new Date());
                  association.setInsertTimestamp(new Date());
                  // Beziehung abspeichern
                  hb_session.save(association);
                  }

                  // MetadatenValues einfügen
                  for(int i=0;i<headerMetadataIDs.size();i++){

                      String metadataValue ="";

                      if(i == 0)
                          metadataValue = csv.get("Kennzeichen");
                      if(i == 1)
                          metadataValue = csv.get("Gruppe");
                      if(i == 2)
                          metadataValue = csv.get("HD_Gruppe");
                      if(i == 3)
                          metadataValue = csv.get("ZD_Gruppe");
                      if(i == 4)
                          metadataValue = csv.get("Geschlecht");
                      if(i == 5)
                          metadataValue = csv.get("Mindestalter");
                      if(i == 6)
                          metadataValue = csv.get("Höchstalter");
                      if(i == 7)
                          metadataValue = csv.get("Unwahrscheinlich");
                      if(i == 8)
                          metadataValue = csv.get("HD_ambulant");
                      if(i == 9)
                          metadataValue = csv.get("HD_>0_Tage");
                      if(i == 10)
                          metadataValue = csv.get("Meldepflicht");
                      if(i == 11)
                          metadataValue = csv.get("Aktion");

                      if(metadataValue.equals(" ") || metadataValue.equals("")){
                          //no Update
                      }else{
                        //Check if parameter already set in case of new Version!
                        String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                        hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                        HQLParameterHelper parameterHelper = new HQLParameterHelper();

                        if(i == 0)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Kennzeichen"));
                        if(i == 1)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Gruppe"));
                        if(i == 2)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("HD_Gruppe"));
                        if(i == 3)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("ZD_Gruppe"));
                        if(i == 4)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Geschlecht"));
                        if(i == 5)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Mindestalter"));
                        if(i == 6)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Höchstalter"));
                        if(i == 7)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Unwahrscheinlich"));
                        if(i == 8)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("HD_ambulant"));
                        if(i == 9)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("HD_>0_Tage"));
                        if(i == 10)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Meldepflicht"));
                        if(i == 11)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Aktion"));

                        parameterHelper.addParameter("csev.", "versionId", response.getCodeSystemEntity().getCurrentVersionId());

                        // Parameter hinzufügen (immer mit AND verbunden)
                        hql += parameterHelper.getWhere("");
                        logger.debug("HQL: " + hql);

                        // Query erstellen
                        org.hibernate.Query q = hb_session.createQuery(hql);
                        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                        parameterHelper.applyParameter(q);

                        List<CodeSystemMetadataValue> valueList= q.list(); 

                        if(valueList.size() == 1){
                            valueList.get(0).setParameterValue(metadataValue);
                        }

                        logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                        hb_session.update(valueList.get(0));
                      }
                  }
                  //System.out.println("C_: " + count);
              }
              else
                countFehler++;

            }
            else
            {
              countFehler++;
              logger.debug("Term ist nicht gegeben");
            }
             
          }else{
          
                Long versionId = -1l;
                for(Map.Entry<String, Long> entry : linkingMapDreisteller.entrySet()){
                    if(entry.getKey().equals(csv.get("Diagnose")))
                        versionId = entry.getValue();
                }
                
                if(versionId != -1l){
                CodeSystemConcept csc = (CodeSystemConcept)hb_session.get(CodeSystemConcept.class, versionId);
                csc.setCode(csv.get("Diagnose"));
                csc.setTerm(csv.get("Bezeichnung"));
                csc.setDescription(csv.get("Kurztext"));
                hb_session.update(csc);
                }
                
                //metadaten update
                // MetadatenValues einfügen
                for(int i=0;i<headerMetadataIDs.size();i++){

                    String metadataValue ="";

                    if(i == 0)
                        metadataValue = csv.get("Kennzeichen");
                    if(i == 1)
                        metadataValue = csv.get("Gruppe");
                    if(i == 2)
                        metadataValue = csv.get("HD_Gruppe");
                    if(i == 3)
                        metadataValue = csv.get("ZD_Gruppe");
                    if(i == 4)
                        metadataValue = csv.get("Geschlecht");
                    if(i == 5)
                        metadataValue = csv.get("Mindestalter");
                    if(i == 6)
                        metadataValue = csv.get("Höchstalter");
                    if(i == 7)
                        metadataValue = csv.get("Unwahrscheinlich");
                    if(i == 8)
                        metadataValue = csv.get("HD_ambulant");
                    if(i == 9)
                        metadataValue = csv.get("HD_>0_Tage");
                    if(i == 10)
                        metadataValue = csv.get("Meldepflicht");
                    if(i == 11)
                        metadataValue = csv.get("Aktion");

                    if(metadataValue.equals(" ") || metadataValue.equals("")){
                        //no Update
                    }else{
                        //Check if parameter already set in case of new Version!
                        String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                        hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                        HQLParameterHelper parameterHelper = new HQLParameterHelper();

                        if(i == 0)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Kennzeichen"));
                        if(i == 1)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Gruppe"));
                        if(i == 2)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("HD_Gruppe"));
                        if(i == 3)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("ZD_Gruppe"));
                        if(i == 4)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Geschlecht"));
                        if(i == 5)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Mindestalter"));
                        if(i == 6)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Höchstalter"));
                        if(i == 7)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Unwahrscheinlich"));
                        if(i == 8)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("HD_ambulant"));
                        if(i == 9)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("HD_>0_Tage"));
                        if(i == 10)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Meldepflicht"));
                        if(i == 11)
                            parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Aktion"));

                        parameterHelper.addParameter("csev.", "versionId", versionId);

                        // Parameter hinzufügen (immer mit AND verbunden)
                        hql += parameterHelper.getWhere("");
                        logger.debug("HQL: " + hql);

                        // Query erstellen
                        org.hibernate.Query q = hb_session.createQuery(hql);
                        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                        parameterHelper.applyParameter(q);

                        List<CodeSystemMetadataValue> valueList= q.list(); 

                        if(valueList.size() == 1){
                            valueList.get(0).setParameterValue(metadataValue);
                        }

                        logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                        hb_session.update(valueList.get(0));
                    }
                }
            }
        }
        if (count == 0)
        {
          hb_session.getTransaction().rollback();
          
          resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);
          
          reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          hb_session.getTransaction().commit(); 
          countImported = count;
          reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler");
        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        logger.error(ex.getMessage());
        s = "Fehler beim Import einer LeiKat-Datei: " + ex.getLocalizedMessage();

        try
        {
          hb_session.getTransaction().rollback();
          
          resultStr = DeleteTermHelperWS.deleteCS_CSV(onlyCSV, csId, csvId);
          
          logger.info("[ImportLeiKat.java] Rollback durchgeführt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportLeiKat.java] Rollback fehlgeschlagen!");
        }
      }
      finally
      {
        // Session schließen
        hb_session.close();
      }
    }
    catch (Exception ex)
    {
      //java.util.logging.Logger.getLogger(ImportCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      s = "Fehler beim Import: " + ex.getLocalizedMessage();
      logger.error(s);
      //ex.printStackTrace();
    }
    
    try {
        
        if(resultStr.equals(""))
            updateCodeSystemVersion();

    } catch (Exception ex) {
        s = "Fehler beim Import: " + ex.getLocalizedMessage();
        logger.error(s);
    }
    
    return s;
  }
  
  private boolean createCodeSystem(org.hibernate.Session hb_session)
  {
    // TODO zunächst prüfen, ob CodeSystem bereits existiert
    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
    request.setCodeSystem(parameter.getCodeSystem());
    request.setLoginToken(parameter.getLoginToken());

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      return false;
    }
    parameter.setCodeSystem(resp.getCodeSystem());

    logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    return true;
  }
  
  private void updateCodeSystemVersion() throws Exception{
  
      //CodeSystemVersion Update
    MaintainCodeSystemVersionRequestType updateParam = new MaintainCodeSystemVersionRequestType();
    VersioningType versioningType = new VersioningType();
    versioningType.setCreateNewVersion(Boolean.FALSE);

    updateParam.setLoginToken(parameter.getLoginToken());
    updateParam.setCodeSystem(parameter.getCodeSystem());
    updateParam.setVersioning(versioningType);

    MaintainCodeSystemVersion mcv = new MaintainCodeSystemVersion();
    MaintainCodeSystemVersionResponseType updateResponse = mcv.MaintainCodeSystemVersion(updateParam, "");

    if(updateResponse.getReturnInfos().getStatus() != ReturnType.Status.OK){

        throw new Exception("ImportICDAT: CodeSystemVersion Update konnte nicht durchgeführt werden: " + updateResponse.getReturnInfos().getMessage());
    }
  }
  
  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }
}
