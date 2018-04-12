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

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.*;
import de.fhdo.terminologie.helper.CODES;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersion;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.VersioningType;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Philipp Urbauer
 */
public class ImportCSSVS
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportCodeSystemRequestType parameter;
  private int countImported = 0;
  private CodeSystemVersion csVersion;
  private CodeSystem cs;
  private int lowestLevel = 1;
  
  private boolean onlyCSV = false;
  private Long csId = 0L;
  private Long csvId = 0L;
  private String resultStr = "";
  AuthenticateInfos loginInfoType;
  
  public ImportCSSVS(ImportCodeSystemRequestType _parameter, AuthenticateInfos _loginInfoType)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportCS SVS gestartet ======");

    parameter = _parameter;
    loginInfoType = _loginInfoType;
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }

  public String importSVS(ImportCodeSystemResponseType reponse, String ipAddress)
  {
    String s = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    int count = 0, countFehler = 0;
   
    try
    {
      byte[] bytes = parameter.getImportInfos().getFilecontent();
      logger.debug("Wandle zu InputStream um...");
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document svsDoc = builder.parse(new ByteArrayInputStream(bytes));
      HashMap<String,String> codeListInfoMap = new HashMap<String, String>();
      ArrayList<HashMap<String,String>> conceptsList = new ArrayList<HashMap<String,String>>();
      
      Node root = svsDoc.getDocumentElement();
      if(root.getNodeName().equals("valueSet")){
          
          NamedNodeMap valueSetAttributes = root.getAttributes();
          if(valueSetAttributes.getNamedItem("name") != null){
              codeListInfoMap.put("name", valueSetAttributes.getNamedItem("name").getTextContent());                    //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("displayName") != null){
              codeListInfoMap.put("displayName", valueSetAttributes.getNamedItem("displayName").getTextContent());      //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("effectiveDate") != null){
              codeListInfoMap.put("effectiveDate", valueSetAttributes.getNamedItem("effectiveDate").getTextContent());  //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("id") != null){
              codeListInfoMap.put("id", valueSetAttributes.getNamedItem("id").getTextContent());                        //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("statusCode") != null){
              codeListInfoMap.put("statusCode", valueSetAttributes.getNamedItem("statusCode").getTextContent());        //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("website") != null){
              codeListInfoMap.put("website", valueSetAttributes.getNamedItem("website").getTextContent());        //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("version") != null){
              codeListInfoMap.put("version", valueSetAttributes.getNamedItem("version").getTextContent());        //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("beschreibung") != null){
              codeListInfoMap.put("beschreibung", valueSetAttributes.getNamedItem("beschreibung").getTextContent());        //ZUWEISUNG
          }
          if(valueSetAttributes.getNamedItem("description") != null){
              codeListInfoMap.put("description", valueSetAttributes.getNamedItem("description").getTextContent());        //ZUWEISUNG
          }
          
          NodeList children = root.getChildNodes();
          for(int i = 0;i<children.getLength();i++){

            Node conceptList = children.item(i);
            if(!(conceptList.getNodeType() == Node.TEXT_NODE)){

                if(conceptList.getNodeName().equals("conceptList")){
                    
                    NodeList concepts = conceptList.getChildNodes();
                    for(int j = 0;j<concepts.getLength();j++){

                        Node concept = concepts.item(j);
                        if(!(concept.getNodeType() == Node.TEXT_NODE)){

                            if(concept.getNodeName().equals("concept")){
                                HashMap<String,String> conceptInfo = new HashMap<String, String>();
                                
                                NamedNodeMap conceptAttributes = concept.getAttributes();
                                if(conceptAttributes.getNamedItem("code") != null){
                                    conceptInfo.put("code", conceptAttributes.getNamedItem("code").getTextContent());                //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("codeSystem") != null){
                                    conceptInfo.put("codeSystem", conceptAttributes.getNamedItem("codeSystem").getTextContent());    //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("displayName") != null){
                                    conceptInfo.put("displayName", conceptAttributes.getNamedItem("displayName").getTextContent());  //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("level") != null){
                                    conceptInfo.put("level", conceptAttributes.getNamedItem("level").getTextContent());              //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("type") != null){
                                    conceptInfo.put("type", conceptAttributes.getNamedItem("type").getTextContent());                //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("concept_beschreibung") != null){
                                    conceptInfo.put("concept_beschreibung", conceptAttributes.getNamedItem("concept_beschreibung").getTextContent());                //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("deutsch") != null){
                                    conceptInfo.put("deutsch", conceptAttributes.getNamedItem("deutsch").getTextContent());                //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("hinweise") != null){
                                    conceptInfo.put("hinweise", conceptAttributes.getNamedItem("hinweise").getTextContent());                //ZUWEISUNG
                                }
                                if(conceptAttributes.getNamedItem("relationships") != null){
                                    conceptInfo.put("relationships", conceptAttributes.getNamedItem("relationships").getTextContent());                //ZUWEISUNG
                                }
                                
                                conceptsList.add(conceptInfo);
                            }
                        }
                    }
                }
            }
          }
      }else{
      
          s = "SVS-Datei: Kein XML-Root-Node gefunden, bitte die zu importierende Datei prüfen...";
      }

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createCodeSystem(codeListInfoMap) == false)
        {
          // Fehlermeldung
          hb_session.getTransaction().commit();
          hb_session.close();
          return "Code System konnte nicht erstellt werden!";
        }
        
        // MetadatenParameter speichern => ELGA Specific Level/Type 
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        for (int i=0;i<3;i++)
        {
            
          String mdText ="";
          MetadataParameter mp = null;
          if(i==0){
            mdText = "Level";
          }
          if(i==1){
            mdText = "Type";
          }
          if(i==2){
            mdText = "Relationships";
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
        cs = (CodeSystem) parameter.getCodeSystem();
        
        for(CodeSystemVersion csv:parameter.getCodeSystem().getCodeSystemVersions()){
        
            if(csv.getVersionId().equals(cs.getCurrentVersionId())){
            
                cs.getCodeSystemVersions().clear();
                cs.getCodeSystemVersions().add(csv);
                break;
            }
        }
        parameter.setCodeSystem(cs);
        
        csId = cs.getId();
        csvId = cs.getCodeSystemVersions().iterator().next().getVersionId();
        
        csVersion = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().iterator().next();
        
        Date date = new Date();
        csVersion.setInsertTimestamp(date);

        if(csVersion.getName() == null || csVersion.getName().equals("")){
            csVersion.setName(codeListInfoMap.get("version"));
        }
        csVersion.setOid(codeListInfoMap.get("id"));
        csVersion.setReleaseDate(sdf.parse(codeListInfoMap.get("effectiveDate")));
        if(codeListInfoMap.get("statusCode").equals("final")){
            csVersion.setStatus(1);
        }else{
            csVersion.setStatus(0);
        }
        csVersion.setStatusDate(date);
        csVersion.setValidityRange(236l); //Default Value for empfohlen
        cs.setDescription(codeListInfoMap.get("beschreibung"));
        cs.setDescriptionEng(codeListInfoMap.get("description"));
        cs.setWebsite(codeListInfoMap.get("website"));
     
        //konzept integration here   
        Iterator<HashMap<String,String>> iterator = conceptsList.iterator();
        while (iterator.hasNext())
        {
          HashMap<String,String> conceptDetails = (HashMap<String, String>)iterator.next(); 
          
          CreateConceptRequestType request = new CreateConceptRequestType();

          request.setLoginToken(parameter.getLoginToken());
          request.setCodeSystem(parameter.getCodeSystem());
          request.setCodeSystemEntity(new CodeSystemEntity());
          request.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());

          CodeSystemConcept csc = new CodeSystemConcept();

          csc.setCode(conceptDetails.get("code"));
          csc.setIsPreferred(true);
          csc.setTerm(conceptDetails.get("displayName"));
          csc.setTermAbbrevation("");
          csc.setDescription(conceptDetails.get("concept_beschreibung"));
          csc.setMeaning(conceptDetails.get("deutsch"));
          csc.setHints(conceptDetails.get("hinweise"));

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
            csev.setIsLeaf(true);
            csev.setEffectiveDate(sdf.parse(codeListInfoMap.get("effectiveDate")));

            // Entity-Version dem Request hinzufügen
            request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

            // Dienst aufrufen (Konzept einfügen)
            CreateConcept cc = new CreateConcept();
            CreateConceptResponseType response = cc.CreateConcept(request, hb_session, loginInfoType);

            if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {

                count++;
                // Metadaten einfügen
                String mdLevelValue = conceptDetails.get("level");//Achtung in Maps lowerCase

                if (mdLevelValue != null && mdLevelValue.length() > 0)
                {
                    //Check if parameter already set in case of new Version!
                    String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                    hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Level"));
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
                        valueList.get(0).setParameterValue(mdLevelValue);
                    }
                    
                    if(Integer.valueOf(mdLevelValue) < lowestLevel)
                        lowestLevel = Integer.valueOf(mdLevelValue);
                    
                    logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                    hb_session.update(valueList.get(0));
                }
                String mdTypeValue = conceptDetails.get("type");////Achtung in Maps lowerCase
                if (mdTypeValue != null && mdTypeValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                    String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                    hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Type"));
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
                        valueList.get(0).setParameterValue(mdTypeValue);
                    }

                    logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                    hb_session.update(valueList.get(0));
                }
                
                String mdRelationshipsValue = conceptDetails.get("relationships");////Achtung in Maps lowerCase
                if (mdRelationshipsValue != null && mdRelationshipsValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                    String hql = "select distinct csmv from CodeSystemMetadataValue csmv";
                    hql += " join fetch csmv.metadataParameter mp join fetch csmv.codeSystemEntityVersion csev";

                    HQLParameterHelper parameterHelper = new HQLParameterHelper();
                    parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Relationships"));
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
                        valueList.get(0).setParameterValue(mdRelationshipsValue);
                    }

                    logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersion().getVersionId() + ", Wert: " + valueList.get(0).getParameterValue());

                    hb_session.update(valueList.get(0));
                }
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
        if (count == 0)
        {

          resultStr = DeleteTermHelperWS.deleteCS_CSV(hb_session, onlyCSV, csId, csvId);
          hb_session.getTransaction().rollback();
          reponse.getReturnInfos().setMessage("Keine Konzepte importiert.");
        }
        else
        {
          hb_session.getTransaction().commit();
          String sortMessage = "";
          if(sort(csVersion.getVersionId())){
              sortMessage = "Sortierung erfolgreich!";
          }else{
              sortMessage = "Sortierung fehlgeschlagen!";
          }
              
          countImported = count;
          reponse.getReturnInfos().setMessage("Import abgeschlossen. " + count + " Konzept(e) importiert, " + countFehler + " Fehler | " + sortMessage);
          CodeSystem cs_ret = new CodeSystem();
          cs_ret.setId(parameter.getCodeSystem().getId());
          cs_ret.setCurrentVersionId(parameter.getCodeSystem().getCurrentVersionId());
          cs_ret.setName(parameter.getCodeSystem().getName());
            
          CodeSystemVersion csv_ret = new CodeSystemVersion();
          csv_ret.setVersionId(parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());
          csv_ret.setName(parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
          cs_ret.getCodeSystemVersions().clear();
          cs_ret.getCodeSystemVersions().add(csv_ret);
          
          
          reponse.setCodeSystem(cs_ret);
        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        logger.error(ex.getMessage());
        s = "Fehler beim Import einer SVS-Datei: " + ex.getLocalizedMessage();

        try
        {
          
          hb_session.getTransaction().rollback();
          
          resultStr = DeleteTermHelperWS.deleteCS_CSV(hb_session, onlyCSV, csId, csvId);
          
          logger.info("[ImportSVS.java] Rollback durchgeführt!");
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportSVS.java] Rollback fehlgeschlagen!");
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
            updateCodeSystemVersion(ipAddress);

    } catch (Exception ex) {
        s = "Fehler beim Import: " + ex.getLocalizedMessage();
        logger.error(s);
    }
    
    return s;
  }
  
  private boolean createCodeSystem(HashMap<String,String> codeListInfoMap)
  {
      
    logger.debug("createCodeSystem...");
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    
    // vorhandenes CodeSystem nutzen oder neues anlegen?
    if (parameter.getCodeSystem().getId() != null && parameter.getCodeSystem().getId() > 0)
    {    
        onlyCSV = true;
        try {
            logger.debug("ID ist angegeben");
            Date date = new java.util.Date();
            
            CodeSystem        cs_db  = (CodeSystem)hb_session.get(CodeSystem.class, parameter.getCodeSystem().getId());
            
            //New Version
            CodeSystemVersion csvNew = new CodeSystemVersion();
            
            csvNew.setName(parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
            csvNew.setInsertTimestamp(date);
            
            csvNew.setStatus(1);
//            if(parameter.getImportInfos().getRole().equals(CODES.ROLE_ADMIN))
//                csvNew.setStatus(1);
//        
//            if(parameter.getImportInfos().getRole().equals(CODES.ROLE_INHALTSVERWALTER))
//                csvNew.setStatus(0);
//            
            csvNew.setStatusDate(date);
            csvNew.setCodeSystem(new CodeSystem());
            csvNew.getCodeSystem().setId(cs_db.getId());
            csvNew.setCodeSystemVersionEntityMemberships(null);
            csvNew.setLicencedUsers(null);
            csvNew.setUnderLicence(false);
            
            Set<LicenceType> licenceTypes = csvNew.getLicenceTypes();
            csvNew.setLicenceTypes(null);
            csvNew.setPreviousVersionId(cs_db.getCurrentVersionId());
            // In DB speichern damit vsvNew eine ID bekommt
            hb_session.save(csvNew);
            
            cs_db.setCurrentVersionId(csvNew.getVersionId());
            
            cs_db.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
            cs_db.getCodeSystemVersions().add(csvNew);
            hb_session.update(cs_db);
            
            // LicenceTypes speichern
            if (licenceTypes != null && csvNew.getVersionId() > 0)
            {
              Iterator<LicenceType> itLicenceType = licenceTypes.iterator();
              while (itLicenceType.hasNext())
              {
                LicenceType lt = itLicenceType.next();
                lt.setCodeSystemVersion(new CodeSystemVersion());
                lt.getCodeSystemVersion().setVersionId(csvNew.getVersionId());
                lt.setLicencedUsers(null);
                hb_session.save(lt);
              }
            }
            
            //Reload
            cs_db = (CodeSystem)hb_session.get(CodeSystem.class, cs_db.getId());
            parameter.setCodeSystem(cs_db);

        }catch(Exception ex){
        
            logger.error("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
            return false;
                
        } finally {
            Long csvId = ((CodeSystemVersion)parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId();
            if (parameter.getCodeSystem().getId() > 0 && csvId > 0) {
                hb_session.getTransaction().commit();
            } else {
                // Änderungen nicht erfolgreich
                logger.warn("[ImportCSSVS.java] CSV konnte nicht gespeichert werden");
                hb_session.getTransaction().rollback();
            }
            hb_session.close();
        }
    }else{
        onlyCSV = false;
        try {
            logger.debug("ID ist angegeben");
            Date date = new java.util.Date();
            
            //New CodeSystem
            CodeSystem cs_db = new CodeSystem();
            cs_db.setName(parameter.getCodeSystem().getName());
            cs_db.setInsertTimestamp(date);
            cs_db.setCodeSystemVersions(null);
            // In DB speichern
            hb_session.save(cs_db);
            
            //New Version
            CodeSystemVersion csvNew = new CodeSystemVersion();
            
            csvNew.setName(parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getName());
            csvNew.setInsertTimestamp(date);
            
            csvNew.setStatus(1);
//            if(parameter.getImportInfos().getRole().equals(CODES.ROLE_ADMIN))
//                csvNew.setStatus(1);
//        
//            if(parameter.getImportInfos().getRole().equals(CODES.ROLE_INHALTSVERWALTER))
//                csvNew.setStatus(0);
            
            /*
            if(codeListInfoMap.get("statusCode").equals("final")){
                csvNew.setStatus(1);
            }else{
                csvNew.setStatus(0);
            }*/
            csvNew.setStatusDate(date);
            csvNew.setCodeSystem(new CodeSystem());
            csvNew.getCodeSystem().setId(cs_db.getId());
            csvNew.setCodeSystemVersionEntityMemberships(null);
            csvNew.setLicencedUsers(null);
            csvNew.setUnderLicence(false);
            
            Set<LicenceType> licenceTypes = csvNew.getLicenceTypes();
            csvNew.setLicenceTypes(null);
            
            // In DB speichern damit vsvNew eine ID bekommt
            hb_session.save(csvNew);
            
            cs_db.setCurrentVersionId(csvNew.getVersionId());
            cs_db.setCodeSystemVersions(new HashSet<CodeSystemVersion>());
            cs_db.getCodeSystemVersions().add(csvNew);
            hb_session.update(cs_db);
            
            // LicenceTypes speichern
            if (licenceTypes != null && csvNew.getVersionId() > 0)
            {
              Iterator<LicenceType> itLicenceType = licenceTypes.iterator();
              while (itLicenceType.hasNext())
              {
                LicenceType lt = itLicenceType.next();
                lt.setCodeSystemVersion(new CodeSystemVersion());
                lt.getCodeSystemVersion().setVersionId(csvNew.getVersionId());
                lt.setLicencedUsers(null);
                hb_session.save(lt);
              }
            }
            
            //Reload
            cs_db = (CodeSystem)hb_session.get(CodeSystem.class, cs_db.getId());
            parameter.setCodeSystem(cs_db);

        }catch(Exception ex){
            
            logger.error("[ImportCSSVS.java] CSV konnte nicht gespeichert werden");
            return false;
                
        } finally {
            Long csvId = ((CodeSystemVersion)parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId();
            if (parameter.getCodeSystem().getId() > 0 && csvId > 0) {
                hb_session.getTransaction().commit();
            } else {
                // Änderungen nicht erfolgreich
                logger.warn("[ImportCSSVS.java] CSV konnte nicht gespeichert werden");
                hb_session.getTransaction().rollback();
            }
            hb_session.close();
        }
    }
    
    return true;
  }
  
  private void updateCodeSystemVersion(String ipAddress) throws Exception{
  
      //CodeSystemVersion Update
    MaintainCodeSystemVersionRequestType updateParam = new MaintainCodeSystemVersionRequestType();
    VersioningType versioningType = new VersioningType();
    versioningType.setCreateNewVersion(Boolean.FALSE);

    updateParam.setLoginToken(parameter.getLoginToken());
    updateParam.setCodeSystem(parameter.getCodeSystem());
    updateParam.setVersioning(versioningType);

    MaintainCodeSystemVersion mcv = new MaintainCodeSystemVersion();
    MaintainCodeSystemVersionResponseType updateResponse = mcv.MaintainCodeSystemVersion(updateParam, ipAddress);

    if(updateResponse.getReturnInfos().getStatus() != ReturnType.Status.OK){

        throw new Exception("ImportCSSVS: CodeSystemVersion Update konnte nicht durchgeführt werden: " + updateResponse.getReturnInfos().getMessage());
    }
  }
  
  private boolean sort(long codeSystemVersionId){
  
      //get List of concepts or whatever
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      boolean s = false;
      try
      {
        String hql = "select distinct csev from CodeSystemEntityVersion csev";
        hql += " join fetch csev.codeSystemMetadataValues csmv";
        hql += " join fetch csmv.metadataParameter mp";
        hql += " join fetch csev.codeSystemEntity cse";
        hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
        hql += " join fetch csvem.codeSystemVersion csv";
        
        
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("csv.", "versionId", codeSystemVersionId);

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");
        logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);
        
        List<CodeSystemEntityVersion> csevList = q.list();
        
        HashMap<Integer,CodeSystemEntityVersion> workList = new HashMap<Integer, CodeSystemEntityVersion>();
        ArrayList<Integer> sortList = new ArrayList<Integer>();
        
        Iterator<CodeSystemEntityVersion> iter = csevList.iterator();
        while(iter.hasNext()){
            boolean found = false;
            CodeSystemEntityVersion csev = (CodeSystemEntityVersion)iter.next();
            
            //Get the Level
            int level = 0;
            Iterator<CodeSystemMetadataValue> iter1 = csev.getCodeSystemMetadataValues().iterator();
            while(iter1.hasNext()){
                CodeSystemMetadataValue csmv = (CodeSystemMetadataValue)iter1.next();
                if(csmv.getMetadataParameter().getParamName().equals("Level")){

                    String cleaned = csmv.getParameterValue();
                    
                    if(cleaned.contains(" "))
                        cleaned = cleaned.replace(" ", "");

                    level = Integer.valueOf(cleaned);
                }
            }
            
            if(level == lowestLevel){
               
                CodeSystemVersionEntityMembershipId csvemId = null;
                Set<CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
                Iterator<CodeSystemVersionEntityMembership> it = csvemSet.iterator();
                while(it.hasNext()){
                
                    CodeSystemVersionEntityMembership member = (CodeSystemVersionEntityMembership)it.next();
                    if(member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                       && member.getCodeSystemVersion().getVersionId().equals(codeSystemVersionId)){
                    
                        csvemId = member.getId();
                    }
                }
                
                CodeSystemVersionEntityMembership c = (CodeSystemVersionEntityMembership)hb_session.get(CodeSystemVersionEntityMembership.class, csvemId);
                c.setIsMainClass(Boolean.TRUE);
                hb_session.update(c);
                       
                workList.put(level, csev);
                sortList.add(level);
            }else{
            
                int size = sortList.size();
                int count = 0;
                while(!found){
                    
                    if((sortList.get(size-(1+count))-level) == -1){
                        
                        //Setting MemberShip isMainClass false
                        CodeSystemVersionEntityMembershipId csvemId = null;
                        Set<CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
                        Iterator<CodeSystemVersionEntityMembership> it = csvemSet.iterator();
                        while(it.hasNext()){

                            CodeSystemVersionEntityMembership member = (CodeSystemVersionEntityMembership)it.next();
                            if(member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                               && member.getCodeSystemVersion().getVersionId().equals(codeSystemVersionId)){

                                csvemId = member.getId();
                            }
                        }

                        CodeSystemVersionEntityMembership c = (CodeSystemVersionEntityMembership)hb_session.get(CodeSystemVersionEntityMembership.class, csvemId);
                        c.setIsMainClass(Boolean.FALSE);
                        hb_session.update(c);
                        
                        found = true;
                        sortList.add(level);
                        workList.put(level, csev);
                        CodeSystemEntityVersion csevPrev = workList.get(sortList.get(size-(1+count)));
                      
                        CodeSystemEntityVersionAssociation      association                = new CodeSystemEntityVersionAssociation();         
                        association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
                        association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csevPrev.getVersionId());
                        association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
                        association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csev.getVersionId());
                        association.setAssociationKind(2); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                        association.setLeftId(csevPrev.getVersionId()); // immer linkes Element also csev1
                        association.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                        association.getAssociationType().setCodeSystemEntityVersionId(4L);
                        // Weitere Attribute setzen
                        association.setStatus(Definitions.STATUS_CODES.ACTIVE.getCode());
                        association.setStatusDate(new Date());
                        association.setInsertTimestamp(new Date());
                        // Beziehung abspeichern
                        hb_session.save(association);
                        
                        CodeSystemEntityVersion csevLoc = (CodeSystemEntityVersion)hb_session.get(CodeSystemEntityVersion.class, csevPrev.getVersionId());
                        if(csevLoc.getIsLeaf() != Boolean.FALSE){
                            csevLoc.setIsLeaf(Boolean.FALSE);
                            hb_session.update(csevLoc);
                        }

                    }else{
                        count++; 
                        found=false;
                    }
                } 
            }
        }
 
        hb_session.getTransaction().commit();
        s = true;
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();
        s = false;
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }
      return s;
  }
}
