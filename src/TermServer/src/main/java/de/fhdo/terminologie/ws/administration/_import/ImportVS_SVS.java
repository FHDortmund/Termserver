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
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.CODES;
import de.fhdo.terminologie.helper.DateComparator;
import de.fhdo.terminologie.helper.DeleteTermHelper;
import de.fhdo.terminologie.helper.DeleteTermHelperWS;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContent;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class ImportVS_SVS
{

  private static Logger logger = Logger4j.getInstance().getLogger();
  ImportValueSetRequestType parameter;
  private Boolean orderCVSM = false;

  private boolean onlyVSV = true; //Only CSV for this case
  private Long vsId = 0L;
  private Long vsvId = 0L;
  private String resultStr = "";

  public ImportVS_SVS(ImportValueSetRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportVS_SVS gestartet ======");

    parameter = _parameter;
  }

  public void importSVS(ImportValueSetResponseType ws_response)
  {
    String s = "";

    try
    {

      byte[] bytes = parameter.getImportInfos().getFilecontent();
      logger.debug("Wandle zu InputStream um...");

      orderCVSM = parameter.getImportInfos().getOrder();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document svsDoc = builder.parse(new ByteArrayInputStream(bytes));
      HashMap<String, String> codeListInfoMap = new HashMap<String, String>();
      ArrayList<HashMap<String, String>> conceptsList = new ArrayList<HashMap<String, String>>();

      Node root = svsDoc.getDocumentElement();
      if (root.getNodeName().equals("valueSet"))
      {

        NamedNodeMap valueSetAttributes = root.getAttributes();
        if (valueSetAttributes.getNamedItem("name") != null)
        {
          codeListInfoMap.put("name", valueSetAttributes.getNamedItem("name").getTextContent());                    //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("displayName") != null)
        {
          codeListInfoMap.put("displayName", valueSetAttributes.getNamedItem("displayName").getTextContent());      //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("effectiveDate") != null)
        {
          codeListInfoMap.put("effectiveDate", valueSetAttributes.getNamedItem("effectiveDate").getTextContent());  //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("id") != null)
        {
          codeListInfoMap.put("id", valueSetAttributes.getNamedItem("id").getTextContent());                        //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("statusCode") != null)
        {
          codeListInfoMap.put("statusCode", valueSetAttributes.getNamedItem("statusCode").getTextContent());        //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("website") != null)
        {
          codeListInfoMap.put("website", valueSetAttributes.getNamedItem("website").getTextContent());        //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("version") != null)
        {
          codeListInfoMap.put("version", valueSetAttributes.getNamedItem("version").getTextContent());        //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("beschreibung") != null)
        {
          codeListInfoMap.put("beschreibung", valueSetAttributes.getNamedItem("beschreibung").getTextContent());        //ZUWEISUNG
        }
        if (valueSetAttributes.getNamedItem("description") != null)
        {
          codeListInfoMap.put("description", valueSetAttributes.getNamedItem("description").getTextContent());        //ZUWEISUNG
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {

          Node conceptList = children.item(i);
          if (!(conceptList.getNodeType() == Node.TEXT_NODE))
          {

            if (conceptList.getNodeName().equals("conceptList"))
            {

              NodeList concepts = conceptList.getChildNodes();
              for (int j = 0; j < concepts.getLength(); j++)
              {

                Node concept = concepts.item(j);
                if (!(concept.getNodeType() == Node.TEXT_NODE))
                {

                  if (concept.getNodeName().equals("concept"))
                  {
                    HashMap<String, String> conceptInfo = new HashMap<String, String>();

                    NamedNodeMap conceptAttributes = concept.getAttributes();
                    if (conceptAttributes.getNamedItem("code") != null)
                    {
                      conceptInfo.put("code", conceptAttributes.getNamedItem("code").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("codeSystem") != null)
                    {
                      conceptInfo.put("codeSystem", conceptAttributes.getNamedItem("codeSystem").getTextContent());    //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("displayName") != null)
                    {
                      conceptInfo.put("displayName", conceptAttributes.getNamedItem("displayName").getTextContent());  //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("level") != null)
                    {
                      conceptInfo.put("level", conceptAttributes.getNamedItem("level").getTextContent());              //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("type") != null)
                    {
                      conceptInfo.put("type", conceptAttributes.getNamedItem("type").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("concept_beschreibung") != null)
                    {
                      conceptInfo.put("concept_beschreibung", conceptAttributes.getNamedItem("concept_beschreibung").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("deutsch") != null)
                    {
                      conceptInfo.put("deutsch", conceptAttributes.getNamedItem("deutsch").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("hinweise") != null)
                    {
                      conceptInfo.put("hinweise", conceptAttributes.getNamedItem("hinweise").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("relationships") != null)
                    {
                      conceptInfo.put("relationships", conceptAttributes.getNamedItem("relationships").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("einheit_print") != null)
                    {
                      conceptInfo.put("einheit_print", conceptAttributes.getNamedItem("einheit_print").getTextContent());                //ZUWEISUNG
                    }
                    if (conceptAttributes.getNamedItem("einheit_codiert") != null)
                    {
                      conceptInfo.put("einheit_codiert", conceptAttributes.getNamedItem("einheit_codiert").getTextContent());                //ZUWEISUNG
                    }
                    conceptsList.add(conceptInfo);
                  }
                }
              }
            }
          }
        }
      }
      else
      {

        throw new Exception("SVS-Datei: Kein XML-Root-Node gefunden, bitte die zu importierende Datei prüfen...");
      }
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try // try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (createValueSet(codeListInfoMap) == false)
        {
          // Fehlermeldung
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          ws_response.getReturnInfos().setMessage("ValueSet konnte nicht erstellt werden!");
          return;
        }

        // MetadatenParameter speichern => ELGA Specific Level/Type 
        Map<String, Long> headerMetadataIDs = new HashMap<String, Long>();
        for (int i = 0; i < 5; i++)
        {

          String mdText = "";
          MetadataParameter mp = null;
          if (i == 0)
          {
            mdText = "Level";
          }
          if (i == 1)
          {
            mdText = "Type";
          }
          if (i == 2)
          {
            mdText = "Relationships";
          }
          if (i == 3)
          {
            mdText = "Einheit print";
          }
          if (i == 4)
          {
            mdText = "Einheit codiert";
          }

          //Check if parameter already set in case of new Version!
          String hql = "select distinct mp from MetadataParameter mp";
          hql += " join fetch mp.valueSet vs";

          HQLParameterHelper parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("mp.", "paramName", mdText);

          // Parameter hinzufügen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");
          logger.debug("HQL: " + hql);

          // Query erstellen
          org.hibernate.Query q = hb_session.createQuery(hql);
          // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
          parameterHelper.applyParameter(q);

          List<MetadataParameter> mpList = q.list();
          for (MetadataParameter mParameter : mpList)
          {

            if (mParameter.getValueSet().getId().equals(parameter.getValueSet().getId()))
              mp = mParameter;
          }

          if (mp == null)
          {

            mp = new MetadataParameter();
            mp.setParamName(mdText);
            mp.setValueSet(parameter.getValueSet());
            hb_session.save(mp);
          }

          headerMetadataIDs.put(mdText, mp.getId());

          logger.debug("Speicher/Verlinke Metadata-Parameter: " + mdText + " mit ValueSet-ID: " + mp.getValueSet().getId() + ", MD-ID: " + mp.getId());
        }

        //ConceptLinking
        CreateValueSetContentRequestType request = new CreateValueSetContentRequestType();
        request.setLoginToken(parameter.getLoginToken());

        //Nur letzte Version
        ValueSet vs = parameter.getValueSet();

        for (ValueSetVersion vsv : parameter.getValueSet().getValueSetVersions())
        {

          if (vsv.getVersionId().equals(vs.getCurrentVersionId()))
          {

            vs.getValueSetVersions().clear();
            vs.getValueSetVersions().add(vsv);
            break;
          }
        }
        parameter.setValueSet(vs);

        vsId = vs.getId();
        vsvId = vs.getValueSetVersions().iterator().next().getVersionId();

        request.setValueSet(parameter.getValueSet());
        request.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());

        Long orderCounter = 1l;

        Iterator<HashMap<String, String>> iterator = conceptsList.iterator();
        while (iterator.hasNext())
        {

          HashMap<String, String> conceptDetails = (HashMap<String, String>) iterator.next();
          CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
          ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
          cvsm.setDescription(conceptDetails.get("concept_beschreibung"));
          cvsm.setMeaning(conceptDetails.get("deutsch"));
          cvsm.setHints(conceptDetails.get("hinweise"));

          if (orderCVSM)
          {
            cvsm.setOrderNr(orderCounter);
            ++orderCounter;
          }
          else
          {
            cvsm.setOrderNr(0l);
          }

          // Version-ID muss anhand des Codes bestimmt werden
          String code = conceptDetails.get("code");

          logger.debug("Entity zu Code '" + code + "' wird gesucht...");
          String oid = conceptDetails.get("codeSystem");

          String hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                  + " where csc.code=:code and"
                  + " csv.oid=:oid";

          org.hibernate.Query q = hb_session.createQuery(hql);
          q.setString("code", code);
          q.setString("oid", oid);

          List<CodeSystemEntityVersion> csevList = q.list();
          if (csevList != null && csevList.size() >= 1)
          {
            //Im falle einer Lebenden Liste soll der aktuelle Code genommen werden
            if (csevList.size() > 1)
            {
              Collections.sort(csevList, new DateComparator());
            }

            // Version-ID gefunden, nun übergeben
            logger.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());
            csev.setVersionId(csevList.get(0).getVersionId());
          }
          else
          {

            String hql2 = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                    + " where csc.code=:code and"
                    + " csv.oid=:oid";
            String adaptedCode = "_" + code;
            org.hibernate.Query q2 = hb_session.createQuery(hql2);
            q2.setString("code", adaptedCode);
            q2.setString("oid", oid);

            List<CodeSystemEntityVersion> csevList2 = q2.list();
            if (csevList2 != null && csevList2.size() >= 1)
            {
              //Im falle einer Lebenden Liste soll der aktuelle Code genommen werden
              if (csevList2.size() > 1)
              {
                Collections.sort(csevList2, new DateComparator());
              }

              // Version-ID gefunden, nun übergeben
              logger.debug("Version-ID anhand des Codes bestimmt: " + csevList2.get(0).getVersionId());
              csev.setVersionId(csevList2.get(0).getVersionId());
              /*
               if(conceptDetails.get("displayName") != null && !conceptDetails.get("displayName").equals("")){
               //translation anlegen
               CodeSystemConceptTranslation csct = new CodeSystemConceptTranslation();
               csct.setLanguageId(33l);
               csct.setTerm(conceptDetails.get("displayName"));

               CodeSystemConcept csc = (CodeSystemConcept)hb_session.get(CodeSystemConcept.class, csevList.get(0).getVersionId());
               csct.setCodeSystemConcept(csc);
               hb_session.save(csct);
               }*/
            }
            else
            {
              logger.debug("Entity zu Code '" + code + "/" + adaptedCode + "' nicht gefunden!");
              throw new Exception("Entity zu Code '" + code + "/" + adaptedCode + "' nicht gefunden!");
            }
          }

          csev.getConceptValueSetMemberships().clear();
          csev.getConceptValueSetMemberships().add(cvsm);
          // Konzept hinzufügen
          CodeSystemEntity cse = new CodeSystemEntity();
          cse.getCodeSystemEntityVersions().add(csev);
          request.getCodeSystemEntity().add(cse);
        }

        hb_session.getTransaction().commit();
        CreateValueSetContent createValueSetContent = new CreateValueSetContent();
        CreateValueSetContentResponseType response = createValueSetContent.CreateValueSetContent(request, hb_session, "");

        //Hier erst daten für ValueSetMetadataValue einfügen!!!
        if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
          Iterator<HashMap<String, String>> it = conceptsList.iterator();
          while (it.hasNext())
          {

            HashMap<String, String> conceptDetails = (HashMap<String, String>) it.next();
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();

            // Version-ID muss anhand des Codes bestimmt werden
            String code = conceptDetails.get("code");

            logger.debug("Entity zu Code '" + code + "' wird gesucht...");
            String oid = conceptDetails.get("codeSystem");

            String hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                    + " where csc.code=:code and"
                    + " csv.oid=:oid";

            org.hibernate.Query q = hb_session.createQuery(hql);
            q.setString("code", code);
            q.setString("oid", oid);

            List<CodeSystemEntityVersion> csevList = q.list();
            if (csevList != null && csevList.size() >= 1)
            {
              if (csevList.size() > 1)
              {
                Collections.sort(csevList, new DateComparator());
              }
              // Version-ID gefunden, nun übergeben
              logger.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());

              // Metadaten einfügen
              String mdLevelValue = conceptDetails.get("level");//Achtung in Maps lowerCase
              if (mdLevelValue != null && mdLevelValue.length() > 0)
              {
                //Check if parameter already set in case of new Version!
                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Level"));
                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                // Parameter hinzufügen (immer mit AND verbunden)
                hql2 += parameterHelper.getWhere("");
                logger.debug("HQL: " + hql2);

                // Query erstellen
                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q1);

                List<ValueSetMetadataValue> valueList = q1.list();

                if (valueList.size() == 1)
                {
                  valueList.get(0).setParameterValue(mdLevelValue);
                }

                        //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                hb_session.update(valueList.get(0));
              }

              String mdTypeValue = conceptDetails.get("type");////Achtung in Maps lowerCase
              if (mdTypeValue != null && mdTypeValue.length() > 0)
              {
                //Check if parameter already set in case of new Version!
                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Type"));
                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                if (mdTypeValue.equals("A"))
                {

                  ConceptValueSetMembership cvsm_db = null;
                  ConceptValueSetMembershipId cvsmId = new ConceptValueSetMembershipId(
                          csevList.get(0).getVersionId(), request.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                  cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsmId);

                  cvsm_db.setIsStructureEntry(true);

                  hb_session.update(cvsm_db);
                }

                // Parameter hinzufügen (immer mit AND verbunden)
                hql2 += parameterHelper.getWhere("");
                logger.debug("HQL: " + hql2);

                // Query erstellen
                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q1);

                List<ValueSetMetadataValue> valueList = q1.list();

                if (valueList.size() == 1)
                {
                  valueList.get(0).setParameterValue(mdTypeValue);
                }

                        //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                hb_session.update(valueList.get(0));
              }

              String mdRelationshipsValue = conceptDetails.get("relationships");////Achtung in Maps lowerCase
              if (mdRelationshipsValue != null && mdRelationshipsValue.length() > 0)
              {
                //Check if parameter already set in case of new Version!
                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Relationships"));
                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                // Parameter hinzufügen (immer mit AND verbunden)
                hql2 += parameterHelper.getWhere("");
                logger.debug("HQL: " + hql2);

                // Query erstellen
                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q1);

                List<ValueSetMetadataValue> valueList = q1.list();

                if (valueList.size() == 1)
                {
                  valueList.get(0).setParameterValue(mdRelationshipsValue);
                }

                        //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                hb_session.update(valueList.get(0));
              }
              String mdEinheit_printValue = conceptDetails.get("einheit_print");////Achtung in Maps lowerCase
              if (mdEinheit_printValue != null && mdEinheit_printValue.length() > 0)
              {
                //Check if parameter already set in case of new Version!
                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit print"));
                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                // Parameter hinzufügen (immer mit AND verbunden)
                hql2 += parameterHelper.getWhere("");
                logger.debug("HQL: " + hql2);

                // Query erstellen
                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q1);

                List<ValueSetMetadataValue> valueList = q1.list();

                if (valueList.size() == 1)
                {
                  valueList.get(0).setParameterValue(mdEinheit_printValue);
                }

                        //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                hb_session.update(valueList.get(0));
              }

              String mdEinheit_codiertValue = conceptDetails.get("einheit_codiert");////Achtung in Maps lowerCase
              if (mdEinheit_codiertValue != null && mdEinheit_codiertValue.length() > 0)
              {
                //Check if parameter already set in case of new Version!
                String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit codiert"));
                parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                // Parameter hinzufügen (immer mit AND verbunden)
                hql2 += parameterHelper.getWhere("");
                logger.debug("HQL: " + hql2);

                // Query erstellen
                org.hibernate.Query q1 = hb_session.createQuery(hql2);
                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(q1);

                List<ValueSetMetadataValue> valueList = q1.list();

                if (valueList.size() == 1)
                {
                  valueList.get(0).setParameterValue(mdEinheit_codiertValue);
                }

                        //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                hb_session.update(valueList.get(0));
              }
            }
            else
            {

              csev = new CodeSystemEntityVersion();

              // Version-ID muss anhand des Codes bestimmt werden
              code = "_" + code;

              logger.debug("Entity zu Code '" + code + "' wird gesucht...");

              hql = "select distinct csev from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join csev.codeSystemConcepts csc"
                      + " where csc.code=:code and"
                      + " csv.oid=:oid";

              q = hb_session.createQuery(hql);
              q.setString("code", code);
              q.setString("oid", oid);

              csevList = q.list();
              if (csevList != null && csevList.size() >= 1)
              {
                if (csevList.size() > 1)
                {
                  Collections.sort(csevList, new DateComparator());
                }
                // Version-ID gefunden, nun übergeben
                logger.debug("Version-ID anhand des Codes bestimmt: " + csevList.get(0).getVersionId());

                // Metadaten einfügen
                String mdLevelValue = conceptDetails.get("level");//Achtung in Maps lowerCase
                if (mdLevelValue != null && mdLevelValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                  String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                  hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                  HQLParameterHelper parameterHelper = new HQLParameterHelper();
                  parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Level"));
                  parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                  parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                  // Parameter hinzufügen (immer mit AND verbunden)
                  hql2 += parameterHelper.getWhere("");
                  logger.debug("HQL: " + hql2);

                  // Query erstellen
                  org.hibernate.Query q1 = hb_session.createQuery(hql2);
                  // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                  parameterHelper.applyParameter(q1);

                  List<ValueSetMetadataValue> valueList = q1.list();

                  if (valueList.size() == 1)
                  {
                    valueList.get(0).setParameterValue(mdLevelValue);
                  }

                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                  hb_session.update(valueList.get(0));
                }

                String mdTypeValue = conceptDetails.get("type");////Achtung in Maps lowerCase
                if (mdTypeValue != null && mdTypeValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                  String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                  hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                  HQLParameterHelper parameterHelper = new HQLParameterHelper();
                  parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Type"));
                  parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                  parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                  if (mdTypeValue.equals("A"))
                  {

                    ConceptValueSetMembership cvsm_db = null;
                    ConceptValueSetMembershipId cvsmId = new ConceptValueSetMembershipId(
                            csevList.get(0).getVersionId(), request.getValueSet().getValueSetVersions().iterator().next().getVersionId());
                    cvsm_db = (ConceptValueSetMembership) hb_session.get(ConceptValueSetMembership.class, cvsmId);

                    cvsm_db.setIsStructureEntry(true);

                    hb_session.update(cvsm_db);
                  }

                  // Parameter hinzufügen (immer mit AND verbunden)
                  hql2 += parameterHelper.getWhere("");
                  logger.debug("HQL: " + hql2);

                  // Query erstellen
                  org.hibernate.Query q1 = hb_session.createQuery(hql2);
                  // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                  parameterHelper.applyParameter(q1);

                  List<ValueSetMetadataValue> valueList = q1.list();

                  if (valueList.size() == 1)
                  {
                    valueList.get(0).setParameterValue(mdTypeValue);
                  }

                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                  hb_session.update(valueList.get(0));
                }

                String mdRelationshipsValue = conceptDetails.get("relationships");////Achtung in Maps lowerCase
                if (mdRelationshipsValue != null && mdRelationshipsValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                  String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                  hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                  HQLParameterHelper parameterHelper = new HQLParameterHelper();
                  parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Relationships"));
                  parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                  parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                  // Parameter hinzufügen (immer mit AND verbunden)
                  hql2 += parameterHelper.getWhere("");
                  logger.debug("HQL: " + hql2);

                  // Query erstellen
                  org.hibernate.Query q1 = hb_session.createQuery(hql2);
                  // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                  parameterHelper.applyParameter(q1);

                  List<ValueSetMetadataValue> valueList = q1.list();

                  if (valueList.size() == 1)
                  {
                    valueList.get(0).setParameterValue(mdRelationshipsValue);
                  }

                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                  hb_session.update(valueList.get(0));
                }
                String mdEinheit_printValue = conceptDetails.get("einheit_print");////Achtung in Maps lowerCase
                if (mdEinheit_printValue != null && mdEinheit_printValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                  String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                  hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                  HQLParameterHelper parameterHelper = new HQLParameterHelper();
                  parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit print"));
                  parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                  parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                  // Parameter hinzufügen (immer mit AND verbunden)
                  hql2 += parameterHelper.getWhere("");
                  logger.debug("HQL: " + hql2);

                  // Query erstellen
                  org.hibernate.Query q1 = hb_session.createQuery(hql2);
                  // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                  parameterHelper.applyParameter(q1);

                  List<ValueSetMetadataValue> valueList = q1.list();

                  if (valueList.size() == 1)
                  {
                    valueList.get(0).setParameterValue(mdEinheit_printValue);
                  }

                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                  hb_session.update(valueList.get(0));
                }
                String mdEinheit_codiertValue = conceptDetails.get("einheit_codiert");////Achtung in Maps lowerCase
                if (mdEinheit_codiertValue != null && mdEinheit_codiertValue.length() > 0)
                {
                  //Check if parameter already set in case of new Version!
                  String hql2 = "select distinct vsmv from ValueSetMetadataValue vsmv";
                  hql2 += " join fetch vsmv.metadataParameter mp join fetch vsmv.codeSystemEntityVersion csev";

                  HQLParameterHelper parameterHelper = new HQLParameterHelper();
                  parameterHelper.addParameter("mp.", "id", headerMetadataIDs.get("Einheit codiert"));
                  parameterHelper.addParameter("csev.", "versionId", csevList.get(0).getVersionId());
                  parameterHelper.addParameter("vsmv.", "valuesetVersionId", request.getValueSet().getValueSetVersions().iterator().next().getVersionId());

                  // Parameter hinzufügen (immer mit AND verbunden)
                  hql2 += parameterHelper.getWhere("");
                  logger.debug("HQL: " + hql2);

                  // Query erstellen
                  org.hibernate.Query q1 = hb_session.createQuery(hql2);
                  // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                  parameterHelper.applyParameter(q1);

                  List<ValueSetMetadataValue> valueList = q1.list();

                  if (valueList.size() == 1)
                  {
                    valueList.get(0).setParameterValue(mdEinheit_codiertValue);
                  }

                                //logger.debug("Metadaten einfügen, MP-ID " + valueList.get(0).getMetadataParameter().getId() + ", CSEV-ID " + valueList.get(0).getCodeSystemEntityVersionId() + ", Wert: " + valueList.get(0).getParameterValue());
                  hb_session.update(valueList.get(0));
                }
              }
              else
              {

                logger.debug("Entity zu Code '" + code + "' nicht gefunden!");
                throw new Exception("Entity zu Code '" + code + "' nicht gefunden!");
              }
            }
          }

          hb_session.getTransaction().commit();
          ws_response.getReturnInfos().setCount(response.getReturnInfos().getCount());
          ws_response.getReturnInfos().setMessage("Import abgeschlossen. " + response.getReturnInfos().getCount() + " Konzept(e) dem Value Set hinzugefügt.\n"
                  + response.getReturnInfos().getMessage());
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          ws_response.getReturnInfos().setStatus(ReturnType.Status.OK);
          ValueSet vs_ret = new ValueSet();
          vs_ret.setId(parameter.getValueSet().getId());
          vs_ret.setCurrentVersionId(parameter.getValueSet().getCurrentVersionId());
          vs_ret.setName(parameter.getValueSet().getName());

          ValueSetVersion vsv_ret = new ValueSetVersion();
          vsv_ret.setVersionId(parameter.getValueSet().getValueSetVersions().iterator().next().getVersionId());
          vsv_ret.setName(parameter.getValueSet().getValueSetVersions().iterator().next().getName());
          vs_ret.getValueSetVersions().clear();
          vs_ret.getValueSetVersions().add(vsv_ret);

          ws_response.setValueSet(vs_ret);
        }
        else
        {
          hb_session.getTransaction().rollback();

          resultStr = DeleteTermHelper.deleteVS_VSV(onlyVSV, vsId, vsvId);

          ws_response.getReturnInfos().setMessage("Fehler beim Importieren von Value Set-Inhalten: " + response.getReturnInfos().getMessage());
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
      }
      catch (Exception ex)
      {
        //ex.printStackTrace();
        logger.error(ex.getMessage());

        try
        {
          hb_session.getTransaction().rollback();

          resultStr = DeleteTermHelper.deleteVS_VSV(onlyVSV, vsId, vsvId);

          logger.info("[ImportCSV.java] Rollback durchgeführt!");

          ws_response.getReturnInfos().setMessage("Fehler beim Import eines Value Sets: " + ex.getLocalizedMessage());
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        }
        catch (Exception exRollback)
        {
          logger.info(exRollback.getMessage());
          logger.info("[ImportCSV.java] Rollback fehlgeschlagen!");
          ws_response.getReturnInfos().setMessage("Rollback fehlgeschlagen! Fehler beim Import eines Value Sets: " + exRollback.getLocalizedMessage());
          ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
          ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
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
      //java.util.logging.Logger.getLogger(ImportValueSet.class.getName()).log(Level.SEVERE, null, ex);
      ws_response.getReturnInfos().setMessage("Fehler beim Importieren von Value Set-Inhalten: " + ex.getLocalizedMessage());
      ws_response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      ws_response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);

      logger.error(s);
      //ex.printStackTrace();
    }
  }

  private boolean createValueSet(HashMap<String, String> codeListInfoMap)
  {

    logger.debug("createValueSet...");
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    // vorhandenes Value Set nutzen oder neues anlegen?
    if (parameter.getValueSet().getId() != null && parameter.getValueSet().getId() > 0)
    {

      onlyVSV = true;
      try
      {
        logger.debug("ID ist angegeben");
        Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        ValueSet vs_db = (ValueSet) hb_session.get(ValueSet.class, parameter.getValueSet().getId());

        ValueSetVersion vsvNew = new ValueSetVersion();
        vsvNew.setValidityRange(4l); // optional
        vsvNew.setName(codeListInfoMap.get("version"));
        vsvNew.setInsertTimestamp(date);
        vsvNew.setOid(codeListInfoMap.get("id"));
        vsvNew.setReleaseDate(sdf.parse(codeListInfoMap.get("effectiveDate")));

        vsvNew.setStatus(1);
//        if (parameter.getImportInfos().getRole().equals(CODES.ROLE_ADMIN))
//          vsvNew.setStatus(1);
//
//        if (parameter.getImportInfos().getRole().equals(CODES.ROLE_INHALTSVERWALTER))
//          vsvNew.setStatus(0);

        /*
         if(codeListInfoMap.get("statusCode").equals("final")){
         vsvNew.setStatus(1);
         }else{
         vsvNew.setStatus(0);
         }*/
        vsvNew.setStatusDate(date);
        vsvNew.setValidityRange(236l);
        vsvNew.setConceptValueSetMemberships(null);
        vsvNew.setPreviousVersionId(vs_db.getCurrentVersionId());
        vsvNew.setValueSet(new ValueSet());
        vsvNew.getValueSet().setId(vs_db.getId());

        // In DB speichern damit vsvNew eine ID bekommt
        hb_session.save(vsvNew);
        vs_db.setCurrentVersionId(vsvNew.getVersionId());
        hb_session.update(vs_db);
        //Reload
        vs_db = (ValueSet) hb_session.get(ValueSet.class, parameter.getValueSet().getId());
        parameter.setValueSet(vs_db);

      }
      catch (Exception ex)
      {

        logger.error("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
        return false;

      }
      finally
      {
        Long vsvId = ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).getVersionId();
        if (parameter.getValueSet().getId() > 0 && vsvId > 0)
        {
          hb_session.getTransaction().commit();
        }
        else
        {
          // Änderungen nicht erfolgreich
          logger.warn("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }
    }
    else
    {

      onlyVSV = false;
      try
      {
        logger.debug("ID ist angegeben");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new java.util.Date();

        //New ValueSet
        ValueSet vs = new ValueSet();
        vs.setName(parameter.getValueSet().getName());
        if (codeListInfoMap.get("statusCode").equals("final"))
        {
          vs.setStatus(1);
        }
        else
        {
          vs.setStatus(0);
        }
        vs.setStatusDate(date);
        vs.setValueSetVersions(null);
        vs.setWebsite(codeListInfoMap.get("website"));
        vs.setDescription(codeListInfoMap.get("beschreibung"));
        vs.setDescriptionEng(codeListInfoMap.get("description"));

        // In DB speichern
        hb_session.save(vs);

        //New Version
        ValueSetVersion vsvNew = new ValueSetVersion();
        vsvNew.setValidityRange(4l); // empfohlen
        vsvNew.setName(codeListInfoMap.get("version"));
        vsvNew.setInsertTimestamp(date);
        vsvNew.setOid(codeListInfoMap.get("id"));
        vsvNew.setReleaseDate(sdf.parse(codeListInfoMap.get("effectiveDate")));

        vsvNew.setStatus(1);
//        if (parameter.getImportInfos().getRole().equals(CODES.ROLE_ADMIN))
//          vsvNew.setStatus(1);
//
//        if (parameter.getImportInfos().getRole().equals(CODES.ROLE_INHALTSVERWALTER))
//          vsvNew.setStatus(0);

        /*
         if(codeListInfoMap.get("statusCode").equals("final")){
         vsvNew.setStatus(1);
         }else{
         vsvNew.setStatus(0);
         }*/
        vsvNew.setStatusDate(date);
        vsvNew.setConceptValueSetMemberships(null);
        vsvNew.setValueSet(new ValueSet());
        vsvNew.getValueSet().setId(vs.getId());
        vsvNew.setPreferredLanguageCd("de-DE"); // TODO immer German?

        // In DB speichern damit vsvNew eine ID bekommt
        hb_session.save(vsvNew);

        vs.setCurrentVersionId(vsvNew.getVersionId());
        vs.setValueSetVersions(new HashSet<ValueSetVersion>());
        vs.getValueSetVersions().add(vsvNew);
        hb_session.update(vs);
        //Reload
        vs = (ValueSet) hb_session.get(ValueSet.class, vs.getId());
        parameter.setValueSet(vs);

      }
      catch (Exception ex)
      {

        logger.error("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
        return false;

      }
      finally
      {
        Long vsvId = ((ValueSetVersion) parameter.getValueSet().getValueSetVersions().toArray()[0]).getVersionId();
        if (parameter.getValueSet().getId() > 0 && vsvId > 0)
        {
          hb_session.getTransaction().commit();
        }
        else
        {
          // Änderungen nicht erfolgreich
          logger.warn("[ImportVSSVS.java] VSV konnte nicht gespeichert werden");
          hb_session.getTransaction().rollback();
        }
        hb_session.close();
      }
    }

    return true;
  }
}
