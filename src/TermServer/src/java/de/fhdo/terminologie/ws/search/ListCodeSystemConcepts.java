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
package de.fhdo.terminologie.ws.search;

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.helper.CodeSystemHelper;
import de.fhdo.terminologie.helper.DateHelper;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LicenceHelper;
import de.fhdo.terminologie.helper.SysParameter;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.TraverseConceptToRoot;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.types.PagingResultType;
import de.fhdo.terminologie.ws.types.ReturnType;
import de.fhdo.terminologie.ws.types.SortingType;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListCodeSystemConcepts
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private static String lastHQLCount = "";
  //private static long lastCountResult = 0;

  public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, boolean noLimit, String ipAddress)
  {
    return ListCodeSystemConcepts(parameter, null, noLimit, ipAddress, false);
  }

//  public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit, String ipAddress)
//  {
//    if (logger.isInfoEnabled())
//      logger.info("====== ListCodeSystemConcepts gestartet ======");
//
//    boolean createHibernateSession = (session == null);
//
//    // Return-Informationen anlegen
//    ListCodeSystemConceptsResponseType response = new ListCodeSystemConceptsResponseType();
//    response.setReturnInfos(new ReturnType());
//
//    // Parameter prüfen
//    if (validateParameter(parameter, response) == false)
//    {
//      logger.debug("Parameter falsch");
//      return response; // Fehler bei den Parametern
//    }
//
//    // Login-Informationen auswerten (gilt für jeden Webservice)
//    boolean loggedIn = false;
//    AuthenticateInfos loginInfoType = new AuthenticateInfos();
//    if (parameter != null && parameter.getLoginToken() != null)
//    {
//      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
//      loggedIn = loginInfoType != null;
//    }
//
//    if (logger.isDebugEnabled())
//    {
//      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
//      logger.debug("isLookForward: " + parameter.isLookForward());
//    }
//
//    int maxPageSizeUserSpecific = 10;
//    //int maxPageSizeUserSpecific = 1;
//    if (parameter.getPagingParameter() != null && parameter.getPagingParameter().getUserPaging() != null)
//    {
//      if (parameter.getPagingParameter().getUserPaging())
//        maxPageSizeUserSpecific = Integer.valueOf(parameter.getPagingParameter().getPageSize());
//    }
//    else
//    {
//      maxPageSizeUserSpecific = -1;
//    }
//
//    // PagingInfo
//    int maxPageSize = 100;   // Gibt an, wieviele Treffer maximal zurückgegeben werden
//
//    //Warum loggedIn hier? Das ergibt am Termbrowser folgenden Bug: Wenn man eingeloggt ist kann man sich keine HugeFlat Concept Liste mehr ansehen e.g. LOINC! => WrongValueException!
//    if (noLimit)// || loggedIn) 
//    {
//      maxPageSize = -1;
//    }
//    else
//    {
//      String maxPageSizeStr = SysParameter.instance().getStringValue("maxPageSize", null, null);
//      try
//      {
//        maxPageSize = Integer.parseInt(maxPageSizeStr);
//      }
//      catch (Exception e)
//      {
//        LoggingOutput.outputException(e, this);
//      }
//    }
//
//    boolean traverseConceptsToRoot = false;
//    int maxPageSizeSearch = 5;   // Gibt an, wieviele Treffer bei einer Suche maximal zurückgegeben werden
//
//    if (parameter != null && parameter.getSearchParameter() != null
//        && parameter.getSearchParameter().getTraverseConceptsToRoot() != null && parameter.getSearchParameter().getTraverseConceptsToRoot())
//    {
//      traverseConceptsToRoot = true;
//
//      String maxPageSizeSearchStr = SysParameter.instance().getStringValue("maxPageSizeSearch", null, null);
//      if (parameter != null && parameter.getSearchParameter() != null)
//      {
//        if (maxPageSizeSearchStr != null && maxPageSizeSearchStr.length() > 0)
//        {
//          try
//          {
//            maxPageSizeSearch = Integer.parseInt(maxPageSizeSearchStr);
//          }
//          catch (Exception e)
//          {
//            LoggingOutput.outputException(e, this);
//          }
//        }
//      }
//    }
//
//    //maxPageSizeSearch = 2;
//    //maxPageSize = 2;
//    logger.debug("maxPageSize: " + maxPageSizeSearch);
//    logger.debug("maxPageSizeSearch: " + maxPageSizeSearch);
//
//    logger.debug("traverseConceptsToRoot: " + traverseConceptsToRoot);
//
//    try
//    {
//      //List<CodeSystemConcept> conceptList = null;
//
//      String codeSystemVersionOid = "";
//      long codeSystemVersionId = 0;
//      if (parameter.getCodeSystem().getCodeSystemVersions() != null && parameter.getCodeSystem().getCodeSystemVersions().size() > 0)
//      {
//        CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];
//        if (csv.getVersionId() != null)
//          codeSystemVersionId = csv.getVersionId();
//        if (csv.getOid() != null)
//          codeSystemVersionOid = csv.getOid();
//      }
//
//      // Lizenzen prüfen
//      boolean validLicence = false;
//      if (codeSystemVersionOid != null && codeSystemVersionOid.length() > 0)
//        validLicence = LicenceHelper.getInstance().userHasLicence(loginInfoType.getUserId(), codeSystemVersionOid);
//      else
//        validLicence = LicenceHelper.getInstance().userHasLicence(loginInfoType.getUserId(), codeSystemVersionId);
//
//      if (validLicence == false)
//      {
//        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
//        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//        response.getReturnInfos().setMessage("Sie besitzen keine gültige Lizenz für dieses Vokabular!");
//        return response;
//      }
//      else
//        logger.debug("Lizenz für Vokabular vorhanden!");
//
//      // Hibernate-Block, Session öffnen
//      //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
//      org.hibernate.Session hb_session = null;
//      //org.hibernate.Transaction tx = null;
//
//      if (createHibernateSession)
//      {
//        hb_session = HibernateUtil.getSessionFactory().openSession();
//        //hb_session.getTransaction().begin();
//        //tx = hb_session.beginTransaction();
//      }
//      else
//      {
//        hb_session = session;
//        //hb_session.getTransaction().begin();
//      }
//
//      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
//      {
//        if (codeSystemVersionOid != null && codeSystemVersionOid.length() > 0)
//        {
//          logger.debug("get csv-id from oid");
//          // get csv-id from oid
//          String hql = "from CodeSystemVersion csv"
//              + " where csv.oid=:oid";
//          Query q = hb_session.createQuery(hql);
//          q.setString("oid", codeSystemVersionOid);
//          List<CodeSystemVersion> csvList = q.list();
//          if (csvList != null && csvList.size() > 0)
//          {
//            codeSystemVersionId = csvList.get(0).getVersionId();
//            logger.debug("found versionId from oid: " + codeSystemVersionId);
//          }
//          else
//          {
//            response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
//            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
//            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//            response.getReturnInfos().setMessage("Codesystem with given OID can't be found.");
//            response.getReturnInfos().setCount(0);
//            return response;
//          }
//        }
//
//        if (codeSystemVersionId == 0 && parameter.getCodeSystem().getId() != null)
//        {
//          // Aktuelle Version des Vokabulars ermitteln
//          long codeSystemId = parameter.getCodeSystem().getId();
//
//          CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, codeSystemId);
//          if (cs != null)
//            codeSystemVersionId = CodeSystemHelper.getCurrentVersionId(cs);
//        }
//
//        // HQL erstellen
//        // Besonderheit hier: es dürfen keine Werte nachgeladen werden
//        // Beim Abruf eines ICD wäre dieses sehr inperformant, da er für
//        // jeden Eintrag sonst nachladen würde
//
//        /*
//         SELECT * FROM code_system_concept csc
//         JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId
//         JOIN code_system_entity cse ON csev.versionId=cse.id
//         JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId
//         LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId
//         WHERE csvem.codeSystemVersionId=10
//         */
//        String languageCd = "";
//
//        /*SELECT * FROM
//         (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.* FROM code_system_concept csc
//         JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId
//         JOIN code_system_entity cse ON csev.versionId=cse.id
//         JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId
//         WHERE csvem.codeSystemVersionId=10 LIMIT 2) csc2
//         LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc2.codeSystemEntityVersionId*/
//        //
//        //String sql = "SELECT * FROM (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.* FROM code_system_concept csc"
//        String sql = "SELECT * FROM (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.*, csct.term translation_term, csct.termAbbrevation translation_termAbbrevation, csct.description translation_description, csct.languageCd translation_languageCd, csct.id translation_id "
//            + " FROM code_system_concept csc"
//            + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
//            + " JOIN code_system_entity cse ON csev.codeSystemEntityId=cse.id"
//            + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
//            + " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId AND languageCd=:languageCd"
//            //+ " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId AND_LANGUAGE_TERM"
//            + " WHERE_TEIL) csc2"
//            //+ " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc2.codeSystemEntityVersionId";
//            + " LEFT JOIN code_system_entity_version_association cseva1 ON cseva1.codeSystemEntityVersionId1=csc2.versionId"
//            + " LEFT JOIN code_system_entity_version_association cseva2 ON cseva2.codeSystemEntityVersionId2=csc2.versionId";
//
//        String sqlCount = "SELECT COUNT(*) FROM code_system_concept csc"
//            + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
//            + " JOIN code_system_entity cse ON csev.versionId=cse.id"
//            + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
//            + " WHERE_TEIL";
//
//        /*String sql = " FROM code_system_concept csc"
//         + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
//         + " JOIN code_system_entity cse ON csev.versionId=cse.id"
//         + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
//         + " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId";*/
//        //+ " WHERE csvem.codeSystemVersionId=:codeSystemVersionId"
//        //+ " GROUP BY csc.code"
//        //+ " ORDER BY csc.code";
//        // Parameter dem Helper hinzufügen
//        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
//        // sonst sind SQL-Injections möglich
//        HQLParameterHelper parameterHelper = new HQLParameterHelper();
//        parameterHelper.addParameter("", "csvem.codeSystemVersionId", codeSystemVersionId);
//
//        if (parameter != null && parameter.getCodeSystemEntity() != null)
//        {
//          if (parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships() != null
//              && parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().size() > 0)
//          {
//            CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().toArray()[0];
//            parameterHelper.addParameter("csvem.", "isAxis", ms.getIsAxis());
//            parameterHelper.addParameter("csvem.", "isMainClass", ms.getIsMainClass());
//          }
//
//          if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null
//              && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
//          {
//            CodeSystemEntityVersion csev = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
//            parameterHelper.addParameter("csev.", "statusVisibilityDate", csev.getStatusVisibilityDate());
//            parameterHelper.addParameter("csev.", "statusVisibility", csev.getStatusVisibility());
//
//            if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
//            {
//              CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];
//              parameterHelper.addParameter("csc.", "code", csc.getCode());
//              parameterHelper.addParameter("csc.", "term", csc.getTerm());
//              parameterHelper.addParameter("csc.", "termAbbrevation", csc.getTermAbbrevation());
//              parameterHelper.addParameter("csc.", "isPreferred", csc.getIsPreferred());
//
//              if (csc.getCodeSystemConceptTranslations() != null && csc.getCodeSystemConceptTranslations().size() > 0)
//              {
//                CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) csc.getCodeSystemConceptTranslations().toArray()[0];
//                parameterHelper.addParameter("csct.", "term", csct.getTerm());
//                parameterHelper.addParameter("csct.", "termAbbrevation", csct.getTermAbbrevation());
//                if (csct.getLanguageCd() != null && csct.getLanguageCd().length() > 0)
//                {
//                  languageCd = csct.getLanguageCd();
//                }
//              }
//            }
//          }
//        }
//
//        /*if(languageCd.length() == 0)
//         sql = sql.replaceAll("AND_LANGUAGE_TERM", "");
//         else 
//         sql = sql.replaceAll("AND_LANGUAGE_TERM", "AND languageCd=:languageCd");*/
//        if (loggedIn == false)
//        {
//          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
//        }
//
//        // Parameter hinzufügen (immer mit AND verbunden)
//        // Gesamt-Anzahl lesen
//        String where = parameterHelper.getWhere("");
//
//        //sqlCount = "SELECT COUNT(DISTINCT cse.id) FROM " + sqlCount.replaceAll("WHERE_TEIL", where);
//        sqlCount = sqlCount.replaceAll("WHERE_TEIL", where);
//
//        //q.addScalar("csc.code", Hibernate.TEXT);  // Index: 0
//        logger.debug("SQL-Count: " + sqlCount);
//        SQLQuery qCount = hb_session.createSQLQuery(sqlCount);
//        parameterHelper.applySQLParameter(qCount);
//        BigInteger anzahlGesamt = (BigInteger) qCount.uniqueResult();
//
//        logger.debug("Anzahl Gesamt: " + anzahlGesamt.longValue());
//
//        if (anzahlGesamt.longValue() > 0)
//        {
//          // Suche begrenzen
//          int pageSize = -1;
//          int pageIndex = 0;
//          boolean allEntries = false;
//
//          if (parameter != null && parameter.getPagingParameter() != null)
//          {
//            logger.debug("Search-Parameter angegeben");
//            if (parameter.getPagingParameter().isAllEntries() != null && parameter.getPagingParameter().isAllEntries().booleanValue() == true)
//            {
//              if (loggedIn)
//                allEntries = true;
//            }
//
//            if (parameter.getPagingParameter().getPageSize() != null)
//              pageSize = Integer.valueOf(parameter.getPagingParameter().getPageSize());
//            if (parameter.getPagingParameter().getPageIndex() != null)
//              pageIndex = parameter.getPagingParameter().getPageIndex();
//          }
//
//          // MaxResults mit Wert aus SysParam prüfen
//          if (traverseConceptsToRoot)
//          {
//            if (pageSize < 0 || (maxPageSizeSearch > 0 && pageSize > maxPageSizeSearch))
//              pageSize = maxPageSizeSearch;
//          }
//          else
//          {
//            if (pageSize < 0 || (maxPageSize > 0 && pageSize > maxPageSize))
//              pageSize = maxPageSize;
//          }
//          if (pageIndex < 0)
//            pageIndex = 0;
//
//          logger.debug("pageIndex: " + pageIndex);
//          logger.debug("pageSize: " + pageSize);
//
//          /*String sortStr = " ORDER BY csc.code";
//
//           if (parameter.getSortingParameter() != null)
//           {
//           if (parameter.getSortingParameter().getSortType() == null
//           || parameter.getSortingParameter().getSortType() == SortingType.SortType.ALPHABETICALLY)
//           {
//           sortStr = " ORDER BY";
//
//           if (parameter.getSortingParameter().getSortBy() != null
//           && parameter.getSortingParameter().getSortBy() == SortingType.SortByField.TERM)
//           {
//           sortStr += " csc.term";
//           }
//           else
//           {
//           sortStr += " csc.code";
//           }
//
//           if (parameter.getSortingParameter().getSortDirection() != null
//           && parameter.getSortingParameter().getSortDirection() == SortingType.SortDirection.DESCENDING)
//           {
//           sortStr += " desc";
//           }
//
//           }
//           }*/
//          String sortStr = " ORDER BY code";
//
//          if (parameter.getSortingParameter() != null)
//          {
//            if (parameter.getSortingParameter().getSortType() == null
//                || parameter.getSortingParameter().getSortType() == SortingType.SortType.ALPHABETICALLY)
//            {
//              sortStr = " ORDER BY";
//
//              if (parameter.getSortingParameter().getSortBy() != null
//                  && parameter.getSortingParameter().getSortBy() == SortingType.SortByField.TERM)
//              {
//                sortStr += " term";
//              }
//              else
//              {
//                sortStr += " code";
//              }
//
//              if (parameter.getSortingParameter().getSortDirection() != null
//                  && parameter.getSortingParameter().getSortDirection() == SortingType.SortDirection.DESCENDING)
//              {
//                sortStr += " desc";
//              }
//
//            }
//          }
//
//          /*String where_all = where + sortStr;
//
//           if (pageSize > 0 && allEntries == false)
//           {
//           where_all += " LIMIT " + (pageIndex * pageSize) + "," + pageSize;
//           }
//
//           sql = sql.replaceAll("WHERE_TEIL", where_all);*/
//          if (pageSize > 0 && allEntries == false)
//          {
//            sortStr += " LIMIT " + (pageIndex * pageSize) + "," + pageSize;
//          }
//
//          sql = sql.replaceAll("WHERE_TEIL", where);
//          sql += sortStr;
//
//          int anzahl = 0;
//          //logger.debug("SQL: " + sql);
//          // Query erstellen
//          SQLQuery q = hb_session.createSQLQuery(sql);
//          q.addScalar("csc2.code", StandardBasicTypes.TEXT);  // Index: 0
//          q.addScalar("csc2.term", StandardBasicTypes.TEXT);
//          q.addScalar("csc2.termAbbrevation", StandardBasicTypes.TEXT);
//          q.addScalar("csc2.description", StandardBasicTypes.TEXT);
//          q.addScalar("csc2.isPreferred", StandardBasicTypes.BOOLEAN);
//          q.addScalar("csc2.codeSystemEntityVersionId", StandardBasicTypes.LONG);
//
//          q.addScalar("csc2.effectiveDate", StandardBasicTypes.TIMESTAMP);  // Index: 6
//          q.addScalar("csc2.insertTimestamp", StandardBasicTypes.TIMESTAMP);
//          q.addScalar("csc2.isLeaf", StandardBasicTypes.BOOLEAN);
//          q.addScalar("csc2.majorRevision", StandardBasicTypes.INTEGER);
//          q.addScalar("csc2.minorRevision", StandardBasicTypes.INTEGER);
//          q.addScalar("csc2.statusVisibility", StandardBasicTypes.INTEGER);
//          q.addScalar("csc2.statusVisibilityDate", StandardBasicTypes.TIMESTAMP);
//          q.addScalar("csc2.versionId", StandardBasicTypes.LONG);
//          q.addScalar("csc2.codeSystemEntityId", StandardBasicTypes.LONG);
//
//          q.addScalar("csc2.id", StandardBasicTypes.LONG);  // Index: 15
//          q.addScalar("csc2.currentVersionId", StandardBasicTypes.LONG);
//
//          q.addScalar("csc2.isAxis", StandardBasicTypes.BOOLEAN);  // Index: 17
//          q.addScalar("csc2.isMainClass", StandardBasicTypes.BOOLEAN);
//
//          q.addScalar("translation_term", StandardBasicTypes.TEXT);  // Index: 19
//          q.addScalar("translation_termAbbrevation", StandardBasicTypes.TEXT);
//          q.addScalar("translation_languageCd", StandardBasicTypes.TEXT);
//          q.addScalar("translation_description", StandardBasicTypes.TEXT);
//          q.addScalar("translation_id", StandardBasicTypes.LONG);
//
//          q.addScalar("cseva1.codeSystemEntityVersionId1", StandardBasicTypes.LONG); // Index: 24
//          q.addScalar("cseva1.codeSystemEntityVersionId2", StandardBasicTypes.LONG);
//          q.addScalar("cseva1.leftId", StandardBasicTypes.LONG);
//          q.addScalar("cseva1.associationTypeId", StandardBasicTypes.LONG);
//          q.addScalar("cseva1.associationKind", StandardBasicTypes.INTEGER);
//          q.addScalar("cseva1.status", StandardBasicTypes.INTEGER);
//          q.addScalar("cseva1.statusDate", StandardBasicTypes.TIMESTAMP);
//          q.addScalar("cseva1.insertTimestamp", StandardBasicTypes.TIMESTAMP);
//
//          q.addScalar("csc2.meaning", StandardBasicTypes.TEXT); //Index: 32
//          q.addScalar("csc2.hints", StandardBasicTypes.TEXT);
//
//          q.addScalar("csc2.statusDeactivated", StandardBasicTypes.INTEGER); // Index: 34
//          q.addScalar("csc2.statusDeactivatedDate", StandardBasicTypes.TIMESTAMP);
//          q.addScalar("csc2.statusWorkflow", StandardBasicTypes.INTEGER);
//          q.addScalar("csc2.statusWorkflowDate", StandardBasicTypes.TIMESTAMP);
//
//          q.addScalar("cseva2.codeSystemEntityVersionId1", StandardBasicTypes.LONG); // Index: 38
//          q.addScalar("cseva2.codeSystemEntityVersionId2", StandardBasicTypes.LONG);
//          q.addScalar("cseva2.leftId", StandardBasicTypes.LONG);
//          q.addScalar("cseva2.associationTypeId", StandardBasicTypes.LONG);
//          q.addScalar("cseva2.associationKind", StandardBasicTypes.INTEGER);
//          q.addScalar("cseva2.status", StandardBasicTypes.INTEGER);
//          q.addScalar("cseva2.statusDate", StandardBasicTypes.TIMESTAMP);
//          q.addScalar("cseva2.insertTimestamp", StandardBasicTypes.TIMESTAMP);
//
//          parameterHelper.applySQLParameter(q);
//          //if(languageCd.length() > 0)
//          q.setString("languageCd", languageCd);
//
//          response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
//
//          logger.debug("SQL: " + q.getQueryString());
//
//          List conceptList = (List) q.list();
//
//          Iterator it = conceptList.iterator();
//
//          long lastCodeSystemEntityVersionId = 0;
//          CodeSystemEntity cse = new CodeSystemEntity();
//          CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
//          CodeSystemConcept csc = new CodeSystemConcept();
//          CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
//          boolean fertig = false;
//
//          while (it.hasNext())
//          {
//            Object[] item = null;
//            long codeSystemEntityVersionId = 0;
//            do
//            {
//              if (it.hasNext() == false)
//              {
//                fertig = true;
//                break;
//              }
//
//              item = (Object[]) it.next();
//
//              // Prüfen, ob Translation (1:N)
//              codeSystemEntityVersionId = (Long) item[5];
//              if (lastCodeSystemEntityVersionId == codeSystemEntityVersionId)
//              {
//                // Gleiches Konzept, Assoziation hinzufügen
//                if (parameter.isLookForward())
//                  addAssociationToEntityVersion(csev, item);
//              }
//            }
//            while (lastCodeSystemEntityVersionId == codeSystemEntityVersionId);
//
//            if (fertig)
//              break;
//
//            // Konzepte zusammenbauen
//            cse = new CodeSystemEntity();
//            csev = new CodeSystemEntityVersion();
//            csc = new CodeSystemConcept();
//            csvem = new CodeSystemVersionEntityMembership();
//
//            // Konzept
//            if (item[0] != null)
//              csc.setCode(item[0].toString());
//            if (item[1] != null)
//              csc.setTerm(item[1].toString());
//            if (item[2] != null)
//              //csc.setTermAbbrevation(new String((char[])item[2]));
//              csc.setTermAbbrevation(item[2].toString());
//            if (item[3] != null)
//              csc.setDescription(item[3].toString());
//            if (item[4] != null)
//              csc.setIsPreferred((Boolean) item[4]);
//            if (item[5] != null)
//              csc.setCodeSystemEntityVersionId((Long) item[5]);
//
//            if (item[32] != null)
//              csc.setMeaning(item[32].toString());
//            if (item[33] != null)
//              csc.setHints(item[33].toString());
//
//            // Entity Version
//            if (item[6] != null)
//              csev.setEffectiveDate(DateHelper.getDateFromObject(item[6]));
//            if (item[7] != null)
//              csev.setInsertTimestamp(DateHelper.getDateFromObject(item[7]));
//            if (item[8] != null)
//              csev.setIsLeaf((Boolean) item[8]);
//            if (item[9] != null)
//              csev.setMajorRevision((Integer) item[9]);
//            if (item[10] != null)
//              csev.setMinorRevision((Integer) item[10]);
//            if (item[11] != null)
//              csev.setStatusVisibility((Integer) item[11]);
//            if (item[12] != null)
//              csev.setStatusVisibilityDate(DateHelper.getDateFromObject(item[12]));
//            if (item[13] != null)
//              csev.setVersionId((Long) item[13]);
//
//            if (item[34] != null)
//              csev.setStatusDeactivated((Integer) item[34]);
//            if (item[35] != null)
//              csev.setStatusDeactivatedDate(DateHelper.getDateFromObject(item[35]));
//            if (item[36] != null)
//              csev.setStatusWorkflow((Integer) item[36]);
//            if (item[37] != null)
//              csev.setStatusWorkflowDate(DateHelper.getDateFromObject(item[37]));
//
//            // Code System Entity
//            if (item[15] != null)
//              cse.setId((Long) item[15]);
//            if (item[16] != null)
//              cse.setCurrentVersionId((Long) item[16]);
//
//            // Entity Membership
//            if (item[17] != null)
//              csvem.setIsAxis((Boolean) item[17]);
//            if (item[18] != null)
//              csvem.setIsMainClass((Boolean) item[18]);
//
//            // Translation
//            addTranslationToConcept(csc, item);
//
//            // Assoziation
//            if (parameter.isLookForward())
//              addAssociationToEntityVersion(csev, item);
//
//            if (traverseConceptsToRoot)
//            {
//              // Alle Elemente bis zum Root ermitteln (für Suche)
//              TraverseConceptToRoot traverse = new TraverseConceptToRoot();
//              TraverseConceptToRootRequestType requestTraverse = new TraverseConceptToRootRequestType();
//              requestTraverse.setLoginToken(parameter.getLoginToken());
//              requestTraverse.setCodeSystemEntity(new CodeSystemEntity());
//              CodeSystemEntityVersion csevRequest = new CodeSystemEntityVersion();
//              csevRequest.setVersionId(csev.getVersionId());
//              requestTraverse.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
//              requestTraverse.getCodeSystemEntity().getCodeSystemEntityVersions().add(csevRequest);
//
//              requestTraverse.setDirectionToRoot(true);
//              //TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, hb_session); // die Session übergeben, damit diese nicht geschlossen wird
//              TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, null);
//
//              //logger.debug("responseTraverse: " + responseTraverse.getReturnInfos().getMessage());
//              if (responseTraverse.getReturnInfos().getStatus() == ReturnType.Status.OK)
//              {
//                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() == null)
//                {
//                  csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(
//                      responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
//                }
//                else
//                {
//                  csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().addAll(
//                      responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
//                }
//              }
//            }
//
//            if (parameter.isLoadMetadata() != null && parameter.isLoadMetadata().booleanValue())
//            {
//              String hql = "from CodeSystemMetadataValue mv "
//                  + " join fetch mv.metadataParameter mp "
//                  + " where codeSystemEntityVersionId=:csev_id";
//
//              Query query = hb_session.createQuery(hql);
//              query.setLong("csev_id", csev.getVersionId());
//              csev.setCodeSystemMetadataValues(new HashSet<CodeSystemMetadataValue>(query.list()));
//
//              // remove circle problems
//              for (CodeSystemMetadataValue mv : csev.getCodeSystemMetadataValues())
//              {
//                mv.setCodeSystemEntityVersion(null);
//                mv.getMetadataParameter().setCodeSystem(null);
//                mv.getMetadataParameter().setValueSet(null);
//                mv.getMetadataParameter().setValueSetMetadataValues(null);
//                mv.getMetadataParameter().setCodeSystemMetadataValues(null);
//                mv.getMetadataParameter().setDescription(null);
//                mv.getMetadataParameter().setMetadataParameterType(null);
//              }
//            }
//
//            if (parameter.isLoadTranslation() != null && parameter.isLoadTranslation().booleanValue())
//            {
//              String hql = "from CodeSystemConceptTranslation csct "
//                  + " where codeSystemEntityVersionId=:csev_id";
//              //+ " order by csct.languageCd";
//
//              Query query = hb_session.createQuery(hql);
//              query.setLong("csev_id", csev.getVersionId());
//              csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>(query.list()));
//
//              // remove circle problems
//              for (CodeSystemConceptTranslation trans : csc.getCodeSystemConceptTranslations())
//              {
//                trans.setCodeSystemConcept(null);
//              }
//            }
//
//            //logger.debug(csc.getCode());
//            //logger.debug("Type: " + csc.getClass().getCanonicalName());
//            /*Object[] o = (Object[]) csc;
//             for(int i=0;i<o.length;++i)
//             {
//             //logger.debug(i + ": " + o.toString());
//             if(o[i] != null)
//             {
//             logger.debug(i + ": " + o[i].toString());
//             logger.debug(i + ": " + o[i].getClass().getCanonicalName());
//             }
//             else logger.debug(i + ": null");
//              
//             //for(int j=0;j<)
//             }*/
//            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
//            csev.getCodeSystemConcepts().add(csc);
//            cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
//            cse.getCodeSystemEntityVersions().add(csev);
//            cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
//            cse.getCodeSystemVersionEntityMemberships().add(csvem);
//            response.getCodeSystemEntity().add(cse);
//
//            lastCodeSystemEntityVersionId = codeSystemEntityVersionId;
//
//            anzahl++;
//          }
//
//          // Treffermenge prüfen            
//          // Paging wird aktiviert
//          if (anzahlGesamt.longValue() > maxPageSize)
//          {
//            response.setPagingInfos(new PagingResultType());
//            response.getPagingInfos().setMaxPageSize(maxPageSize);
//            response.getPagingInfos().setPageIndex(pageIndex);
//            response.getPagingInfos().setPageSize(String.valueOf(pageSize));
//            response.getPagingInfos().setCount(anzahlGesamt.intValue());
//            if (parameter != null && parameter.getPagingParameter() != null)
//            {
//              response.getPagingInfos().setMessage("Paging wurde aktiviert, da die Treffermenge größer ist als die maximale Seitengröße.");
//            }
//          }
//          else
//          {
//
//            if ((maxPageSizeUserSpecific != -1) && anzahlGesamt.longValue() > maxPageSizeUserSpecific)
//            {
//
//              response.setPagingInfos(new PagingResultType());
//              response.getPagingInfos().setMaxPageSize(maxPageSizeUserSpecific);
//              response.getPagingInfos().setPageIndex(pageIndex);
//              response.getPagingInfos().setPageSize(String.valueOf(maxPageSizeUserSpecific));
//              response.getPagingInfos().setCount(anzahlGesamt.intValue());
//              if (parameter != null && parameter.getPagingParameter() != null)
//              {
//                response.getPagingInfos().setMessage("Paging wurde aktiviert, da popUpSearchCS spezifische Seitenanzahl.");
//              }
//            }
//          }
//
//          // Status an den Aufrufer weitergeben            
//          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//          response.getReturnInfos().setStatus(ReturnType.Status.OK);
//          response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl: " + anzahl);
//          response.getReturnInfos().setCount(anzahl);
//        }
//        else
//        {
//          response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
//          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//          response.getReturnInfos().setStatus(ReturnType.Status.OK);
//          response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
//          response.getReturnInfos().setCount(0);
//        }
//        /*String hql = "select distinct csc from CodeSystemConcept csc";
//         hql += " join fetch csc.codeSystemEntityVersion csev";
//         hql += " join fetch csev.codeSystemEntity cse";
//         hql += " left outer join fetch csc.codeSystemConceptTranslations csct";
//         hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
//        
//         //if (parameter.isLookForward())
//         //{
//         //  hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 ass1";
//         //  hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 ass2";
//         //}
//
//         // Parameter dem Helper hinzufügen
//         // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
//         // sonst sind SQL-Injections möglich
//         HQLParameterHelper parameterHelper = new HQLParameterHelper();
//         parameterHelper.addParameter("", "codeSystemVersionId", codeSystemVersionId);
//
//         if (parameter != null && parameter.getCodeSystemEntity() != null)
//         {
//         if (parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships() != null
//         && parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().size() > 0)
//         {
//         CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().toArray()[0];
//         parameterHelper.addParameter("csvem.", "isAxis", ms.getIsAxis());
//         parameterHelper.addParameter("csvem.", "isMainClass", ms.getIsMainClass());
//         }
//
//         if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null
//         && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
//         {
//         CodeSystemEntityVersion csev = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
//         parameterHelper.addParameter("csev.", "statusDate", csev.getStatusDate());
//
//         if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
//         {
//         CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];
//         parameterHelper.addParameter("csc.", "code", csc.getCode());
//         parameterHelper.addParameter("csc.", "term", csc.getTerm());
//         parameterHelper.addParameter("csc.", "termAbbrevation", csc.getTermAbbrevation());
//         parameterHelper.addParameter("csc.", "isPreferred", csc.getIsPreferred());
//
//         if (csc.getCodeSystemConceptTranslations() != null && csc.getCodeSystemConceptTranslations().size() > 0)
//         {
//         CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) csc.getCodeSystemConceptTranslations().toArray()[0];
//         parameterHelper.addParameter("csct.", "term", csct.getTerm());
//         parameterHelper.addParameter("csct.", "termAbbrevation", csct.getTermAbbrevation());
//         if (csct.getLanguageId() > 0)
//         parameterHelper.addParameter("csct.", "languageId", csct.getLanguageId());
//         }
//         }
//         }
//         }
//
//         if (loggedIn == false)
//         {
//         parameterHelper.addParameter("csev.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
//         }
//
//         // Parameter hinzufügen (immer mit AND verbunden)
//         String where = parameterHelper.getWhere("");
//         hql += where;
//
//         // immer neueste Version lesen
//         hql += " AND csev.id=cse.currentVersionId";
//
//         hql += " ORDER BY csc.code";
//
//
//
//         // Suche begrenzen
//         int pageSize = -1;
//         int pageIndex = 0;
//         boolean allEntries = false;
//
//         if (parameter != null && parameter.getPagingParameter() != null)
//         {
//         // vorher aber noch die Gesamtanzahl berechnen
//         //Integer count = (Integer) hb_session.createQuery("select count(*) from ....").uniqueResult();
//
//         if (parameter.getPagingParameter().isAllEntries() != null && parameter.getPagingParameter().isAllEntries().booleanValue() == true)
//         {
//         if (loggedIn)
//         allEntries = true;
//         }
//
//         pageSize = parameter.getPagingParameter().getPageSize();
//         pageIndex = parameter.getPagingParameter().getPageIndex();
//         }
//
//         // MaxResults mit Wert aus SysParam prüfen
//         if (traverseConceptsToRoot)
//         {
//         if (pageSize < 0 || (maxPageSizeSearch > 0 && pageSize > maxPageSizeSearch))
//         pageSize = maxPageSizeSearch;
//         }
//         else
//         {
//         if (pageSize < 0 || (maxPageSize > 0 && pageSize > maxPageSize))
//         pageSize = maxPageSize;
//         }
//         if (pageIndex < 0)
//         pageIndex = 0;
//
//         // Gesamt-Anzahl lesen
//         String hqlCount = "select count(term) from CodeSystemConcept csc";
//         hqlCount += " join  csc.codeSystemEntityVersion csev";
//         hqlCount += " join  csev.codeSystemEntity cse";
//         hqlCount += " join  cse.codeSystemVersionEntityMemberships csvem";
//         hqlCount += where;
//
//         //hql = hql.replace("distinct csc", "count(term)");
//         logger.debug("HQL-Count: " + hqlCount);
//         org.hibernate.Query q = hb_session.createQuery(hqlCount);
//
//         // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
//         parameterHelper.applyParameter(q);
//         long anzahlGesamt = (Long) q.uniqueResult();
//
//         // Anzahl zählen Datenbank-Aufruf durchführen
//         //int anzahlGesamt = q.list().size();
//         //int anzahlGesamt = 100;  // TODO Gesamt-Anzahl herausbekommen
//         logger.debug("Anzahl Gesamt: " + anzahlGesamt);
//
//
//         logger.debug("HQL: " + hql);
//         // Query erstellen
//         q = hb_session.createQuery(hql);
//
//         // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
//         parameterHelper.applyParameter(q);
//
//
//         //conceptList = (java.util.List<CodeSystemConcept>) q.list();
//         if (anzahlGesamt > 0)
//         {
//         //hb_session.setFlushMode(FlushMode.AUTO);
//
//         ScrollableResults scrollResults = q.scroll();
//
//         int itCount = 0;
//
//         if (scrollResults != null)
//         {
//         java.util.List<CodeSystemEntity> entityList = new LinkedList<CodeSystemEntity>();
//
//         if (pageIndex > 0 && allEntries == false && anzahlGesamt > 0)
//         {
//         // Vorspulen
//         //if(pageSize * pageIndex < anzahlGesamt)
//         //  scrollResults.setRowNumber(pageSize * pageIndex);
//         for (int i = 0; i < pageSize * pageIndex && i < anzahlGesamt; ++i)
//         {
//         if (scrollResults.next() == false)
//         break;
//
//         if (i % 50 == 0)
//         {
//         // wichtig, da Speicher sonst voll läuft
//         hb_session.flush();
//         hb_session.clear();
//         }
//         }
//         }
//
//         //Iterator<CodeSystemConcept> iterator = conceptList.iterator();
//         //while (iterator.hasNext())
//
//         try
//         {
//         while (scrollResults.next())
//         {
//         if (itCount >= pageSize && allEntries == false)
//         break;
//
//         if (itCount % 50 == 0)
//         {
//         // wichtig, da Speicher sonst voll läuft
//         //hb_session.flush();
//         hb_session.clear();
//         }
//         itCount++;
//
//         //CodeSystemConcept csc = iterator.next();
//         CodeSystemConcept csc = (CodeSystemConcept) scrollResults.get(0);
//
//         // neues Entity generieren, damit nicht nachgeladen werden muss
//         CodeSystemEntity entity = csc.getCodeSystemEntityVersion().getCodeSystemEntity();
//
//         CodeSystemEntityVersion csev = csc.getCodeSystemEntityVersion();
//
//         csev.setCodeSystemEntity(null);
//
//         if (parameter.isLookForward())
//         {
//         // Verbindungen suchen
//         if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null)
//         {
//         for (CodeSystemEntityVersionAssociation ass : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2())
//         {
//         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
//         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
//         ass.setAssociationType(null);
//         }
//         }
//         if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null)
//         {
//         for (CodeSystemEntityVersionAssociation ass : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
//         {
//         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
//         ass.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
//         ass.setAssociationType(null);
//         }
//         }
//         }
//         else
//         {
//         if (traverseConceptsToRoot)
//         {
//         // Alle Elemente bis zum Root ermitteln (für Suche)
//         TraverseConceptToRoot traverse = new TraverseConceptToRoot();
//         TraverseConceptToRootRequestType requestTraverse = new TraverseConceptToRootRequestType();
//         requestTraverse.setLoginToken(parameter.getLoginToken());
//         requestTraverse.setCodeSystemEntity(new CodeSystemEntity());
//         CodeSystemEntityVersion csevRequest = new CodeSystemEntityVersion();
//         csevRequest.setVersionId(csev.getVersionId());
//         requestTraverse.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
//         requestTraverse.getCodeSystemEntity().getCodeSystemEntityVersions().add(csevRequest);
//
//         requestTraverse.setDirectionToRoot(true);
//         requestTraverse.setReadEntityDetails(false);
//         TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, hb_session); // die Session übergeben, damit diese nicht geschlossen wird
//
//         //logger.debug("responseTraverse: " + responseTraverse.getReturnInfos().getMessage());
//
//         if (responseTraverse.getReturnInfos().getStatus() == ReturnType.Status.OK)
//         {
//         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(
//         responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
//         }
//         else
//         {
//         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
//         }
//         }
//         else
//         {
//         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
//         }
//         csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
//         }
//
//         csev.setCodeSystemMetadataValues(null);
//         csev.setConceptValueSetMemberships(null);
//         csev.setPropertyVersions(null);
//         csev.setAssociationTypes(null);
//
//         csc.setCodeSystemEntityVersion(null);
//
//         logger.debug("Akt Code: " + csc.getCode() + ", " + csc.getTerm());
//
//         //Translations
//         if (csc.getCodeSystemConceptTranslations() != null)
//         {
//         Iterator<CodeSystemConceptTranslation> itTrans = csc.getCodeSystemConceptTranslations().iterator();
//
//         while (itTrans.hasNext())
//         {
//         CodeSystemConceptTranslation csct = itTrans.next();
//         csct.setCodeSystemConcept(null);
//         }
//         }
//
//
//
//         csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
//         csev.getCodeSystemConcepts().add(csc);
//
//         entity.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
//         entity.getCodeSystemEntityVersions().add(csev);
//
//         // M:N Verbindung zur Vokabular-Version (ohne nachladen)
//         CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) entity.getCodeSystemVersionEntityMemberships().toArray()[0];
//         ms.setCodeSystemVersion(null);
//         ms.setCodeSystemEntity(null);
//         ms.setId(null);
//
//         entity.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
//         entity.getCodeSystemVersionEntityMemberships().add(ms);
//
//         entityList.add(entity);
//         }
//         }
//         catch (org.hibernate.exception.GenericJDBCException ex)
//         {
//         logger.debug("Keine Eintraege");
//         ex.printStackTrace();
//         }
//
//         int anzahl = 0;
//         if (entityList != null)
//         anzahl = entityList.size();
//         response.setCodeSystemEntity(entityList);
//
//         // Treffermenge prüfen
//         if (anzahlGesamt > anzahl)
//         {
//         // Paging wird aktiviert
//         response.setPagingInfos(new PagingResultType());
//         response.getPagingInfos().setMaxPageSize(maxPageSize);
//         response.getPagingInfos().setPageIndex(pageIndex);
//         response.getPagingInfos().setPageSize(pageSize);
//         response.getPagingInfos().setCount((int) anzahlGesamt);
//         if (parameter != null && parameter.getPagingParameter() != null)
//         {
//         response.getPagingInfos().setMessage("Paging wurde aktiviert, da die Treffermenge größer ist als die maximale Seitengröße.");
//         }
//         //response.getPagingInfos().setMessage();
//         }
//         //response.setPagingInfos(null);
//
//         // Status an den Aufrufer weitergeben
//         response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//         response.getReturnInfos().setStatus(ReturnType.Status.OK);
//         response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl: " + anzahl);
//         response.getReturnInfos().setCount(anzahl);
//
//         }
//         }
//         else
//         {
//         response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
//         response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//         response.getReturnInfos().setStatus(ReturnType.Status.OK);
//         response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
//         response.getReturnInfos().setCount(0);
//         }*/
//        // Hibernate-Block wird in 'finally' geschlossen, erst danach
//        // Auswertung der Daten
//        // Achtung: hiernach können keine Tabellen/Daten mehr nachgeladen werden
//        //if(createHibernateSession)
//        //hb_session.getTransaction().commit();
//      }
//      catch (Exception e)
//      {
//        //if(createHibernateSession)
//        //hb_session.getTransaction().rollback();
//        // Fehlermeldung an den Aufrufer weiterleiten
//        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
//        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//        response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemConcepts', Hibernate: " + e.getLocalizedMessage());
//
//        LoggingOutput.outputException(e, this);
//      }
//      finally
//      {
//        // Transaktion abschließen
//        if (createHibernateSession)
//        {
//          hb_session.close();
//        }
//      }
//
//    }
//    catch (Exception e)
//    {
//      // Fehlermeldung an den Aufrufer weiterleiten
//      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
//      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//      response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemConcepts': " + e.getLocalizedMessage());
//
//      LoggingOutput.outputException(e, this);
//    }
//
//    return response;
//  }
  public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit, String ipAddress, boolean reloaded)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListCodeSystemConcepts gestartet ======");

    boolean createHibernateSession = (session == null);

    // Return-Informationen anlegen
    ListCodeSystemConceptsResponseType response = new ListCodeSystemConceptsResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      logger.debug("Parameter falsch");
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    AuthenticateInfos loginInfoType = new AuthenticateInfos();
    if (parameter != null && parameter.getLoginToken() != null)
    {
      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType != null;
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
      logger.debug("isLookForward: " + parameter.isLookForward());
    }

    boolean isHierachical = false;

    int maxPageSizeUserSpecific = 10;
    //int maxPageSizeUserSpecific = 1;
    if (parameter.getPagingParameter() != null && parameter.getPagingParameter().getUserPaging() != null)
    {
      if (parameter.getPagingParameter().getUserPaging())
        maxPageSizeUserSpecific = Integer.valueOf(parameter.getPagingParameter().getPageSize());
    }
    else
    {
      maxPageSizeUserSpecific = -1;
    }

    // PagingInfo
    int maxPageSize = 100;   // Gibt an, wieviele Treffer maximal zurückgegeben werden

    //Warum loggedIn hier? Das ergibt am Termbrowser folgenden Bug: Wenn man eingeloggt ist kann man sich keine HugeFlat Concept Liste mehr ansehen e.g. LOINC! => WrongValueException!
    if (noLimit)// || loggedIn) 
    {
      maxPageSize = -1;
    }
    else
    {
      String maxPageSizeStr = SysParameter.instance().getStringValue("maxPageSize", null, null);
      try
      {
        maxPageSize = Integer.parseInt(maxPageSizeStr);
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
    }

    boolean traverseConceptsToRoot = false;
    int maxPageSizeSearch = 5;   // Gibt an, wieviele Treffer bei einer Suche maximal zurückgegeben werden

    if (parameter != null && parameter.getSearchParameter() != null
        && parameter.getSearchParameter().getTraverseConceptsToRoot() != null && parameter.getSearchParameter().getTraverseConceptsToRoot())
    {
      traverseConceptsToRoot = true;

      String maxPageSizeSearchStr = SysParameter.instance().getStringValue("maxPageSizeSearch", null, null);
      if (parameter != null && parameter.getSearchParameter() != null)
      {
        if (maxPageSizeSearchStr != null && maxPageSizeSearchStr.length() > 0)
        {
          try
          {
            maxPageSizeSearch = Integer.parseInt(maxPageSizeSearchStr);
          }
          catch (Exception e)
          {
            LoggingOutput.outputException(e, this);
          }
        }
      }
    }

    //maxPageSizeSearch = 2;
    //maxPageSize = 2;
    logger.debug("maxPageSize: " + maxPageSizeSearch);
    logger.debug("maxPageSizeSearch: " + maxPageSizeSearch);

    logger.debug("traverseConceptsToRoot: " + traverseConceptsToRoot);

    try
    {
      //List<CodeSystemConcept> conceptList = null;

      String codeSystemVersionOid = "";
      long codeSystemVersionId = 0;
      if (parameter.getCodeSystem().getCodeSystemVersions() != null && parameter.getCodeSystem().getCodeSystemVersions().size() > 0)
      {
        CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];
        if (csv.getVersionId() != null)
          codeSystemVersionId = csv.getVersionId();
        if (csv.getOid() != null)
          codeSystemVersionOid = csv.getOid();
      }

      // Lizenzen prüfen
      boolean validLicence = false;
      if (codeSystemVersionOid != null && codeSystemVersionOid.length() > 0)
        validLicence = LicenceHelper.getInstance().userHasLicence(loginInfoType.getUserId(), codeSystemVersionOid);
      else
        validLicence = LicenceHelper.getInstance().userHasLicence(loginInfoType.getUserId(), codeSystemVersionId);

      if (validLicence == false)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Sie besitzen keine gültige Lizenz für dieses Vokabular!");
        return response;
      }
      else
        logger.debug("Lizenz für Vokabular vorhanden!");

      // Hibernate-Block, Session öffnen
      //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Session hb_session = null;
      //org.hibernate.Transaction tx = null;

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();
        //tx = hb_session.beginTransaction();
      }
      else
      {
        hb_session = session;
        //hb_session.getTransaction().begin();
      }

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        if (codeSystemVersionOid != null && codeSystemVersionOid.length() > 0)
        {
          logger.debug("get csv-id from oid");
          // get csv-id from oid (current version)
          String hql = "select distinct csv from CodeSystemVersion csv"
              + " join csv.codeSystem cs"
              + " where csv.oid=:oid and cs.currentVersionId=csv.versionId";
          Query q = hb_session.createQuery(hql);
          q.setString("oid", codeSystemVersionOid);
          
          List<CodeSystemVersion> csvList = q.list();
          
          if (csvList != null && csvList.size() > 0)
          {
            codeSystemVersionId = csvList.get(0).getVersionId();
            logger.debug("found versionId from oid: " + codeSystemVersionId);
          }
          else
          {
            response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Codesystem with given OID can't be found.");
            response.getReturnInfos().setCount(0);
            return response;
          }
        }

        if (codeSystemVersionId == 0 && parameter.getCodeSystem().getId() != null)
        {
          // Aktuelle Version des Vokabulars ermitteln
          long codeSystemId = parameter.getCodeSystem().getId();

          CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, codeSystemId);
          if (cs != null)
            codeSystemVersionId = CodeSystemHelper.getCurrentVersionId(cs);
        }

        // HQL erstellen
        // Besonderheit hier: es dürfen keine Werte nachgeladen werden
        // Beim Abruf eines ICD wäre dieses sehr inperformant, da er für
        // jeden Eintrag sonst nachladen würde

        /*
         SELECT * FROM code_system_concept csc
         JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId
         JOIN code_system_entity cse ON csev.versionId=cse.id
         JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId
         LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId
         WHERE csvem.codeSystemVersionId=10
         */
        String languageCd = "";
        boolean isLookForward = parameter.isLookForward();

        /*SELECT * FROM
         (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.* FROM code_system_concept csc
         JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId
         JOIN code_system_entity cse ON csev.versionId=cse.id
         JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId
         WHERE csvem.codeSystemVersionId=10 LIMIT 2) csc2
         LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc2.codeSystemEntityVersionId*/
        //
        //String sql = "SELECT * FROM (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.* FROM code_system_concept csc"
        String sql = "SELECT * FROM (SELECT csc.*, csev.*, csvem.isAxis, csvem.isMainClass, cse.*, csct.term translation_term, csct.termAbbrevation translation_termAbbrevation, csct.description translation_description, csct.languageCd translation_languageCd, csct.id translation_id "
            + " FROM code_system_concept csc"
            + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
            + " JOIN code_system_entity cse ON csev.codeSystemEntityId=cse.id"
            + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
            + " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId AND languageCd=:languageCd"
            //+ " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId AND_LANGUAGE_TERM"
            + " WHERE_TEIL) csc2";
        //+ " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc2.codeSystemEntityVersionId";

        if (isLookForward)
        {
          sql += " LEFT JOIN code_system_entity_version_association cseva1 ON cseva1.codeSystemEntityVersionId1=csc2.versionId"
              + " LEFT JOIN code_system_entity_version_association cseva2 ON cseva2.codeSystemEntityVersionId2=csc2.versionId";
        }

        
        
        String sqlCount = "SELECT COUNT(*) FROM code_system_concept csc"
            + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
            + " JOIN code_system_entity cse ON csev.codeSystemEntityId=cse.id"
            + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
            + " WHERE_TEIL";

        /*String sql = " FROM code_system_concept csc"
         + " JOIN code_system_entity_version csev ON csc.codeSystemEntityVersionId=csev.versionId"
         + " JOIN code_system_entity cse ON csev.versionId=cse.id"
         + " JOIN code_system_version_entity_membership csvem ON cse.id=csvem.codeSystemEntityId"
         + " LEFT JOIN code_system_concept_translation csct ON csct.codeSystemEntityVersionId=csc.codeSystemEntityVersionId";*/
        //+ " WHERE csvem.codeSystemVersionId=:codeSystemVersionId"
        //+ " GROUP BY csc.code"
        //+ " ORDER BY csc.code";
        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();
        parameterHelper.addParameter("", "csvem.codeSystemVersionId", codeSystemVersionId);

        if (parameter != null && parameter.getCodeSystemEntity() != null)
        {
          if (parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships() != null
              && parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().size() > 0)
          {
            CodeSystemVersionEntityMembership ms = (CodeSystemVersionEntityMembership) parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().toArray()[0];
            
            //parameterHelper.addParameter("csvem.", "isAxis", ms.getIsAxis());
            //parameterHelper.addParameter("csvem.", "isMainClass", ms.getIsMainClass());

            // add condition later
            if (ms.getIsAxis() != null && ms.getIsAxis().booleanValue())
              isHierachical = true;
            if (ms.getIsMainClass() != null && ms.getIsMainClass().booleanValue())
              isHierachical = true;
          }

          if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null
              && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
          {
            CodeSystemEntityVersion csev = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
            parameterHelper.addParameter("csev.", "statusVisibilityDate", csev.getStatusVisibilityDate());
            parameterHelper.addParameter("csev.", "statusVisibility", csev.getStatusVisibility());

            if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
            {
              CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];
              parameterHelper.addParameter("csc.", "code", csc.getCode());
              parameterHelper.addParameter("csc.", "term", csc.getTerm());
              parameterHelper.addParameter("csc.", "termAbbrevation", csc.getTermAbbrevation());
              parameterHelper.addParameter("csc.", "isPreferred", csc.getIsPreferred());

              if (csc.getCodeSystemConceptTranslations() != null && csc.getCodeSystemConceptTranslations().size() > 0)
              {
                CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) csc.getCodeSystemConceptTranslations().toArray()[0];
                parameterHelper.addParameter("csct.", "term", csct.getTerm());
                parameterHelper.addParameter("csct.", "termAbbrevation", csct.getTermAbbrevation());
                if (csct.getLanguageCd() != null && csct.getLanguageCd().length() > 0)
                {
                  languageCd = csct.getLanguageCd();
                }
              }
            }
          }
        }
        
        /*if(languageCd.length() == 0)
         sql = sql.replaceAll("AND_LANGUAGE_TERM", "");
         else 
         sql = sql.replaceAll("AND_LANGUAGE_TERM", "AND languageCd=:languageCd");*/
        if (loggedIn == false)
        {
          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        // Gesamt-Anzahl lesen
        String where = parameterHelper.getWhere("");
        
        where += " AND csev.versionId=cse.currentVersionId";
        
        if(isHierachical)
        {
          where += " AND (csvem.isAxis=1 OR csvem.isMainClass=1) ";
          //parameterHelper.addParameter("csvem.", "isAxis", ms.getIsAxis());
          //parameterHelper.addParameter("csvem.", "isMainClass", ms.getIsMainClass());
        }

        //sqlCount = "SELECT COUNT(DISTINCT cse.id) FROM " + sqlCount.replaceAll("WHERE_TEIL", where);
        sqlCount = sqlCount.replaceAll("WHERE_TEIL", where);

        //q.addScalar("csc.code", Hibernate.TEXT);  // Index: 0
        logger.debug("SQL-Count: " + sqlCount);
        SQLQuery qCount = hb_session.createSQLQuery(sqlCount);
        parameterHelper.applySQLParameter(qCount);
        BigInteger anzahlGesamt = (BigInteger) qCount.uniqueResult();

        logger.debug("Anzahl Gesamt: " + anzahlGesamt.longValue());

        if (anzahlGesamt.longValue() > 0)
        {
          // Suche begrenzen
          int pageSize = -1;
          int pageIndex = 0;
          boolean allEntries = false;

          if (parameter != null && parameter.getPagingParameter() != null)
          {
            logger.debug("Search-Parameter angegeben");
            if (parameter.getPagingParameter().isAllEntries() != null && parameter.getPagingParameter().isAllEntries().booleanValue() == true)
            {
              if (loggedIn)
                allEntries = true;
            }

            if (parameter.getPagingParameter().getPageSize() != null)
              pageSize = Integer.valueOf(parameter.getPagingParameter().getPageSize());
            if (parameter.getPagingParameter().getPageIndex() != null)
              pageIndex = parameter.getPagingParameter().getPageIndex();
          }

          if (isHierachical)
            allEntries = true;

          // MaxResults mit Wert aus SysParam prüfen
          if (traverseConceptsToRoot)
          {
            if (pageSize < 0 || (maxPageSizeSearch > 0 && pageSize > maxPageSizeSearch))
              pageSize = maxPageSizeSearch;
          }
          else
          {
            if (pageSize < 0 || (maxPageSize > 0 && pageSize > maxPageSize))
              pageSize = maxPageSize;
          }
          if (pageIndex < 0)
            pageIndex = 0;

          logger.debug("pageIndex: " + pageIndex);
          logger.debug("pageSize: " + pageSize);

          String sortStr = " ORDER BY code";

          if (parameter.getSortingParameter() != null)
          {
            if (parameter.getSortingParameter().getSortType() == null
                || parameter.getSortingParameter().getSortType() == SortingType.SortType.ALPHABETICALLY)
            {
              sortStr = " ORDER BY";

              if (parameter.getSortingParameter().getSortBy() != null
                  && parameter.getSortingParameter().getSortBy() == SortingType.SortByField.TERM)
              {
                sortStr += " term";
              }
              else
              {
                sortStr += " code";
              }

              if (parameter.getSortingParameter().getSortDirection() != null
                  && parameter.getSortingParameter().getSortDirection() == SortingType.SortDirection.DESCENDING)
              {
                sortStr += " desc";
              }

            }
          }

          /*String where_all = where + sortStr;

           if (pageSize > 0 && allEntries == false)
           {
           where_all += " LIMIT " + (pageIndex * pageSize) + "," + pageSize;
           }

           sql = sql.replaceAll("WHERE_TEIL", where_all);*/
          if (pageSize > 0 && allEntries == false)
          {
            sortStr += " LIMIT " + (pageIndex * pageSize) + "," + pageSize;
          }

          sql = sql.replaceAll("WHERE_TEIL", where);
          sql += sortStr;

          int anzahl = 0;
          //logger.debug("SQL: " + sql);
          // Query erstellen
          SQLQuery q = hb_session.createSQLQuery(sql);
          q.addScalar("csc2.code", StandardBasicTypes.TEXT);  // Index: 0
          q.addScalar("csc2.term", StandardBasicTypes.TEXT);
          q.addScalar("csc2.termAbbrevation", StandardBasicTypes.TEXT);
          q.addScalar("csc2.description", StandardBasicTypes.TEXT);
          q.addScalar("csc2.isPreferred", StandardBasicTypes.BOOLEAN);
          q.addScalar("csc2.codeSystemEntityVersionId", StandardBasicTypes.LONG);

          q.addScalar("csc2.effectiveDate", StandardBasicTypes.TIMESTAMP);  // Index: 6
          q.addScalar("csc2.insertTimestamp", StandardBasicTypes.TIMESTAMP);
          q.addScalar("csc2.isLeaf", StandardBasicTypes.BOOLEAN);
          q.addScalar("csc2.majorRevision", StandardBasicTypes.INTEGER);
          q.addScalar("csc2.minorRevision", StandardBasicTypes.INTEGER);
          q.addScalar("csc2.statusVisibility", StandardBasicTypes.INTEGER);
          q.addScalar("csc2.statusVisibilityDate", StandardBasicTypes.TIMESTAMP);
          q.addScalar("csc2.versionId", StandardBasicTypes.LONG);
          q.addScalar("csc2.codeSystemEntityId", StandardBasicTypes.LONG);

          q.addScalar("csc2.id", StandardBasicTypes.LONG);  // Index: 15
          q.addScalar("csc2.currentVersionId", StandardBasicTypes.LONG);

          q.addScalar("csc2.isAxis", StandardBasicTypes.BOOLEAN);  // Index: 17
          q.addScalar("csc2.isMainClass", StandardBasicTypes.BOOLEAN);

          q.addScalar("translation_term", StandardBasicTypes.TEXT);  // Index: 19
          q.addScalar("translation_termAbbrevation", StandardBasicTypes.TEXT);
          q.addScalar("translation_languageCd", StandardBasicTypes.TEXT);
          q.addScalar("translation_description", StandardBasicTypes.TEXT);
          q.addScalar("translation_id", StandardBasicTypes.LONG);
          
          q.addScalar("csc2.meaning", StandardBasicTypes.TEXT); //Index: 24
          q.addScalar("csc2.hints", StandardBasicTypes.TEXT);

          q.addScalar("csc2.statusDeactivated", StandardBasicTypes.INTEGER); // Index: 26
          q.addScalar("csc2.statusDeactivatedDate", StandardBasicTypes.TIMESTAMP);
          q.addScalar("csc2.statusWorkflow", StandardBasicTypes.INTEGER);
          q.addScalar("csc2.statusWorkflowDate", StandardBasicTypes.TIMESTAMP);

          if (isLookForward)
          {
            q.addScalar("cseva1.codeSystemEntityVersionId1", StandardBasicTypes.LONG); // Index: 30
            q.addScalar("cseva1.codeSystemEntityVersionId2", StandardBasicTypes.LONG);
            q.addScalar("cseva1.leftId", StandardBasicTypes.LONG);
            q.addScalar("cseva1.associationTypeId", StandardBasicTypes.LONG);
            q.addScalar("cseva1.associationKind", StandardBasicTypes.INTEGER);
            q.addScalar("cseva1.status", StandardBasicTypes.INTEGER);
            q.addScalar("cseva1.statusDate", StandardBasicTypes.TIMESTAMP);
            q.addScalar("cseva1.insertTimestamp", StandardBasicTypes.TIMESTAMP);
          }

          if (isLookForward)
          {
            q.addScalar("cseva2.codeSystemEntityVersionId1", StandardBasicTypes.LONG); // Index: 38
            q.addScalar("cseva2.codeSystemEntityVersionId2", StandardBasicTypes.LONG);
            q.addScalar("cseva2.leftId", StandardBasicTypes.LONG);
            q.addScalar("cseva2.associationTypeId", StandardBasicTypes.LONG);
            q.addScalar("cseva2.associationKind", StandardBasicTypes.INTEGER);
            q.addScalar("cseva2.status", StandardBasicTypes.INTEGER);
            q.addScalar("cseva2.statusDate", StandardBasicTypes.TIMESTAMP);
            q.addScalar("cseva2.insertTimestamp", StandardBasicTypes.TIMESTAMP);
          }

          parameterHelper.applySQLParameter(q);
          //if(languageCd.length() > 0)
          q.setString("languageCd", languageCd);

          response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());

          logger.debug("SQL: " + q.getQueryString());

          List conceptList = (List) q.list();

          Iterator it = conceptList.iterator();

          long lastCodeSystemEntityVersionId = 0;
          CodeSystemEntity cse = new CodeSystemEntity();
          CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
          CodeSystemConcept csc = new CodeSystemConcept();
          CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
          boolean fertig = false;

          while (it.hasNext())
          {
            Object[] item = null;
            long codeSystemEntityVersionId = 0;
            do
            {
              if (it.hasNext() == false)
              {
                fertig = true;
                break;
              }

              item = (Object[]) it.next();

              // Prüfen, ob Translation (1:N)
              codeSystemEntityVersionId = (Long) item[5];
              if (lastCodeSystemEntityVersionId == codeSystemEntityVersionId)
              {
                // Gleiches Konzept, Assoziation hinzufügen
                if (parameter.isLookForward())
                  addAssociationToEntityVersion(csev, item);
              }
            }
            while (lastCodeSystemEntityVersionId == codeSystemEntityVersionId);

            if (fertig)
              break;

            // Konzepte zusammenbauen
            cse = new CodeSystemEntity();
            csev = new CodeSystemEntityVersion();
            csc = new CodeSystemConcept();
            csvem = new CodeSystemVersionEntityMembership();

            // Konzept
            if (item[0] != null)
              csc.setCode(item[0].toString());
            if (item[1] != null)
              csc.setTerm(item[1].toString());
            if (item[2] != null)
              //csc.setTermAbbrevation(new String((char[])item[2]));
              csc.setTermAbbrevation(item[2].toString());
            if (item[3] != null)
              csc.setDescription(item[3].toString());
            if (item[4] != null)
              csc.setIsPreferred((Boolean) item[4]);
            if (item[5] != null)
              csc.setCodeSystemEntityVersionId((Long) item[5]);

            if (item[24] != null)
              csc.setMeaning(item[24].toString());
            if (item[25] != null)
              csc.setHints(item[25].toString());

            // Entity Version
            if (item[6] != null)
              csev.setEffectiveDate(DateHelper.getDateFromObject(item[6]));
            if (item[7] != null)
              csev.setInsertTimestamp(DateHelper.getDateFromObject(item[7]));
            if (item[8] != null)
              csev.setIsLeaf((Boolean) item[8]);
            if (item[9] != null)
              csev.setMajorRevision((Integer) item[9]);
            if (item[10] != null)
              csev.setMinorRevision((Integer) item[10]);
            if (item[11] != null)
              csev.setStatusVisibility((Integer) item[11]);
            if (item[12] != null)
              csev.setStatusVisibilityDate(DateHelper.getDateFromObject(item[12]));
            if (item[13] != null)
              csev.setVersionId((Long) item[13]);

            if (item[26] != null)
              csev.setStatusDeactivated((Integer) item[26]);
            if (item[27] != null)
              csev.setStatusDeactivatedDate(DateHelper.getDateFromObject(item[27]));
            if (item[28] != null)
              csev.setStatusWorkflow((Integer) item[28]);
            if (item[29] != null)
              csev.setStatusWorkflowDate(DateHelper.getDateFromObject(item[29]));

            // Code System Entity
            if (item[15] != null)
              cse.setId((Long) item[15]);
            if (item[16] != null)
              cse.setCurrentVersionId((Long) item[16]);

            // Entity Membership
            if (item[17] != null)
              csvem.setIsAxis((Boolean) item[17]);
            if (item[18] != null)
              csvem.setIsMainClass((Boolean) item[18]);

            // Translation
            addTranslationToConcept(csc, item);

            // Assoziation
            if (parameter.isLookForward())
              addAssociationToEntityVersion(csev, item);

            if (traverseConceptsToRoot)
            {
              // Alle Elemente bis zum Root ermitteln (für Suche)
              TraverseConceptToRoot traverse = new TraverseConceptToRoot();
              TraverseConceptToRootRequestType requestTraverse = new TraverseConceptToRootRequestType();
              requestTraverse.setLoginToken(parameter.getLoginToken());
              requestTraverse.setCodeSystemEntity(new CodeSystemEntity());
              CodeSystemEntityVersion csevRequest = new CodeSystemEntityVersion();
              csevRequest.setVersionId(csev.getVersionId());
              requestTraverse.getCodeSystemEntity().setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
              requestTraverse.getCodeSystemEntity().getCodeSystemEntityVersions().add(csevRequest);

              requestTraverse.setDirectionToRoot(true);
              //TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, hb_session); // die Session übergeben, damit diese nicht geschlossen wird
              TraverseConceptToRootResponseType responseTraverse = traverse.TraverseConceptToRoot(requestTraverse, null);

              //logger.debug("responseTraverse: " + responseTraverse.getReturnInfos().getMessage());
              if (responseTraverse.getReturnInfos().getStatus() == ReturnType.Status.OK)
              {
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() == null)
                {
                  csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(
                      responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
                }
                else
                {
                  csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().addAll(
                      responseTraverse.getCodeSystemEntityVersionRoot().getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1());
                }
              }
            }

            if (parameter.isLoadMetadata() != null && parameter.isLoadMetadata().booleanValue())
            {
              String hql = "from CodeSystemMetadataValue mv "
                  + " join fetch mv.metadataParameter mp "
                  + " where codeSystemEntityVersionId=:csev_id";

              Query query = hb_session.createQuery(hql);
              query.setLong("csev_id", csev.getVersionId());
              csev.setCodeSystemMetadataValues(new HashSet<CodeSystemMetadataValue>(query.list()));

              // remove circle problems
              for (CodeSystemMetadataValue mv : csev.getCodeSystemMetadataValues())
              {
                mv.setCodeSystemEntityVersion(null);
                mv.getMetadataParameter().setCodeSystem(null);
                mv.getMetadataParameter().setValueSet(null);
                mv.getMetadataParameter().setValueSetMetadataValues(null);
                mv.getMetadataParameter().setCodeSystemMetadataValues(null);
                mv.getMetadataParameter().setDescription(null);
                mv.getMetadataParameter().setMetadataParameterType(null);
              }
            }

            if (parameter.isLoadTranslation() != null && parameter.isLoadTranslation().booleanValue())
            {
              String hql = "from CodeSystemConceptTranslation csct "
                  + " where codeSystemEntityVersionId=:csev_id";
              //+ " order by csct.languageCd";

              Query query = hb_session.createQuery(hql);
              query.setLong("csev_id", csev.getVersionId());
              csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>(query.list()));

              // remove circle problems
              for (CodeSystemConceptTranslation trans : csc.getCodeSystemConceptTranslations())
              {
                trans.setCodeSystemConcept(null);
              }
            }

            //logger.debug(csc.getCode());
            //logger.debug("Type: " + csc.getClass().getCanonicalName());
            /*Object[] o = (Object[]) csc;
             for(int i=0;i<o.length;++i)
             {
             //logger.debug(i + ": " + o.toString());
             if(o[i] != null)
             {
             logger.debug(i + ": " + o[i].toString());
             logger.debug(i + ": " + o[i].getClass().getCanonicalName());
             }
             else logger.debug(i + ": null");
              
             //for(int j=0;j<)
             }*/
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
            cse.getCodeSystemEntityVersions().add(csev);
            cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
            cse.getCodeSystemVersionEntityMemberships().add(csvem);
            response.getCodeSystemEntity().add(cse);

            lastCodeSystemEntityVersionId = codeSystemEntityVersionId;

            anzahl++;
          }

          // Treffermenge prüfen            
          // Paging wird aktiviert
          if (anzahlGesamt.longValue() > maxPageSize)
          {
            response.setPagingInfos(new PagingResultType());
            response.getPagingInfos().setMaxPageSize(maxPageSize);
            response.getPagingInfos().setPageIndex(pageIndex);
            response.getPagingInfos().setPageSize(String.valueOf(pageSize));
            response.getPagingInfos().setCount(anzahlGesamt.intValue());
            if (parameter != null && parameter.getPagingParameter() != null)
            {
              response.getPagingInfos().setMessage("Paging wurde aktiviert, da die Treffermenge größer ist als die maximale Seitengröße.");
            }
          }
          else
          {

            if ((maxPageSizeUserSpecific != -1) && anzahlGesamt.longValue() > maxPageSizeUserSpecific)
            {

              response.setPagingInfos(new PagingResultType());
              response.getPagingInfos().setMaxPageSize(maxPageSizeUserSpecific);
              response.getPagingInfos().setPageIndex(pageIndex);
              response.getPagingInfos().setPageSize(String.valueOf(maxPageSizeUserSpecific));
              response.getPagingInfos().setCount(anzahlGesamt.intValue());
              if (parameter != null && parameter.getPagingParameter() != null)
              {
                response.getPagingInfos().setMessage("Paging wurde aktiviert, da popUpSearchCS spezifische Seitenanzahl.");
              }
            }
          }

          // Status an den Aufrufer weitergeben            
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl: " + anzahl);
          response.getReturnInfos().setCount(anzahl);
        }
        else
        {
          if(isHierachical && reloaded == false)
          {
            // try listing without axis/mainclass
            parameter.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(null);
            return ListCodeSystemConcepts(parameter, hb_session, noLimit, ipAddress, true); // set reloaded = true to prevent recursive loads
          }
          response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
          response.getReturnInfos().setCount(0);
        }
        
      }
      catch (Exception e)
      {
        //if(createHibernateSession)
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemConcepts', Hibernate: " + e.getLocalizedMessage());

        LoggingOutput.outputException(e, this);
      }
      finally
      {
        // Transaktion abschließen
        if (createHibernateSession)
        {
          hb_session.close();
        }
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystemConcepts': " + e.getLocalizedMessage());

      LoggingOutput.outputException(e, this);
    }

    return response;
  }

  private void addTranslationToConcept(CodeSystemConcept csc, Object[] item)
  {
    //logger.debug("addTranslationToConcept...");
    if (item[19] == null)  // Term muss angegeben sein
    {
      //logger.debug("item[19] ist null");
      return;
    }

    if (csc.getCodeSystemConceptTranslations() == null)
      csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());

    //logger.debug("term: " + item[19].toString());
    CodeSystemConceptTranslation csct = new CodeSystemConceptTranslation();
    csct.setTerm(item[19].toString());
    if (item[20] != null)
      csct.setTermAbbrevation(item[20].toString());
    if (item[21] != null)
      csct.setLanguageCd(item[21].toString());
    if (item[22] != null)
      csct.setDescription(item[22].toString());
    if (item[23] != null)
      csct.setId((Long) item[23]);

    /*q.addScalar("csct.term", Hibernate.TEXT);  // Index: 19
     q.addScalar("csct.termAbbrevation", Hibernate.TEXT);
     q.addScalar("csct.languageId", Hibernate.LONG);
     q.addScalar("csct.description", Hibernate.TEXT);*/
    csc.getCodeSystemConceptTranslations().add(csct);
  }

  private void addAssociationToEntityVersion(CodeSystemEntityVersion csev, Object[] item)
  {
    try
    {
      if (item[30] != null)  // Pflichtfeld
      {

        logger.debug("addAssociationToEntityVersion, csev-id: " + csev.getVersionId() + ", item[30]: " + item[30]);

        CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
        cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId((Long) item[30]);
        cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId((Long) item[31]);

        if (item[32] != null)
          cseva.setLeftId((Long) item[32]);
        else
          logger.warn("LeftId ist null: " + csev.getVersionId());

        if (item[33] != null)
        {
          cseva.setAssociationType(new AssociationType());
          cseva.getAssociationType().setCodeSystemEntityVersionId((Long) item[33]);
        }

        if (item[34] != null)
          cseva.setAssociationKind((Integer) item[34]);
        if (item[35] != null)
          cseva.setStatus((Integer) item[35]);
        if (item[36] != null)
          cseva.setStatusDate(DateHelper.getDateFromObject(item[36]));
        if (item[37] != null)
          cseva.setInsertTimestamp(DateHelper.getDateFromObject(item[37]));

        if (cseva.getLeftId() == null || cseva.getLeftId().longValue() == csev.getVersionId().longValue())
        {
          if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() == null)
            csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(new HashSet<CodeSystemEntityVersionAssociation>());

          csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().add(cseva);
        }
        else
        {
          if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() == null)
            csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(new HashSet<CodeSystemEntityVersionAssociation>());

          csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().add(cseva);
        }
      }
      else if (item[38] != null)  // Pflichtfeld
      {
        logger.debug("addAssociationToEntityVersion, csev-id: " + csev.getVersionId() + ", item[38]: " + item[38]);

        CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
        cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
        cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId((Long) item[38]);
        cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
        cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId((Long) item[39]);

        if (item[40] != null)
          cseva.setLeftId((Long) item[40]);
        else
          logger.warn("LeftId ist null: " + csev.getVersionId());

        if (item[41] != null)
        {
          cseva.setAssociationType(new AssociationType());
          cseva.getAssociationType().setCodeSystemEntityVersionId((Long) item[41]);
        }

        if (item[42] != null)
          cseva.setAssociationKind((Integer) item[42]);
        if (item[43] != null)
          cseva.setStatus((Integer) item[43]);
        if (item[44] != null)
          cseva.setStatusDate(DateHelper.getDateFromObject(item[44]));
        if (item[45] != null)
          cseva.setInsertTimestamp(DateHelper.getDateFromObject(item[45]));

        if (cseva.getLeftId() == null || cseva.getLeftId().longValue() == csev.getVersionId().longValue())
        {
          if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() == null)
            csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(new HashSet<CodeSystemEntityVersionAssociation>());

          csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().add(cseva);
        }
        else
        {
          if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() == null)
            csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(new HashSet<CodeSystemEntityVersionAssociation>());

          csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().add(cseva);
        }
      }

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

    /*q.addScalar("cseva1.codeSystemEntityVersionId1", Hibernate.LONG); // Index: 24
     q.addScalar("cseva1.codeSystemEntityVersionId2", Hibernate.LONG);
     q.addScalar("cseva1.leftId", Hibernate.LONG);
     q.addScalar("cseva1.associationTypeId", Hibernate.LONG);
     q.addScalar("cseva1.associationKind", Hibernate.INTEGER);
     q.addScalar("cseva1.status", Hibernate.INTEGER);
     q.addScalar("cseva1.statusDate", Hibernate.DATE);
     q.addScalar("cseva1.insertTimestamp", Hibernate.TIMESTAMP);*/
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ListCodeSystemConceptsRequestType Request,
                                    ListCodeSystemConceptsResponseType Response)
  {
    boolean erfolg = true;

    CodeSystem codeSystem = Request.getCodeSystem();
    if (codeSystem == null)
    {
      Response.getReturnInfos().setMessage("CodeSystem my not be empty!");
      erfolg = false;
    }
    else
    {
      //boolean csId = false;
      boolean csvId = false;

      //csId = codeSystem.getId() != null && codeSystem.getId() > 0;

      /* if (codeSystem.getId() == null || codeSystem.getId() == 0)
       {
       Response.getReturnInfos().setMessage(
       "Es muss eine ID für das CodeSystem angegeben sein!");
       erfolg = false;
       } */
      if (codeSystem.getCodeSystemVersions() != null)
      {
        Set<CodeSystemVersion> csvSet = codeSystem.getCodeSystemVersions();
        if (csvSet != null)
        {
          if (csvSet.size() > 1)
          {
            Response.getReturnInfos().setMessage(
                "The codesystem version list must have exactly one entry!");
            erfolg = false;
          }
          else if (csvSet.size() == 1)
          {
            CodeSystemVersion csv = (CodeSystemVersion) csvSet.toArray()[0];

            if ((csv.getVersionId() == null || csv.getVersionId() == 0)
                && (csv.getOid() == null || csv.getOid().length() == 0))
            {
              Response.getReturnInfos().setMessage(
                  "You have to specify the Version-ID or OID from the codesystem version!");
              erfolg = false;
            }
            else
              csvId = true;
          }
        }
      }

      if (csvId == false)
      {
        Response.getReturnInfos().setMessage(
            "You have to specify the Version-ID or OID from the codesystem version!");
        erfolg = false;
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
