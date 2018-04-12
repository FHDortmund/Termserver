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

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsResponseType;
import de.fhdo.terminologie.ws.types.PagingResultType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListGloballySearchedConcepts
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, boolean noLimit, String ipAddress)
  {
    return ListGloballySearchedConcepts(parameter, null, noLimit, ipAddress);
  }

  public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListGloballySearchedConcepts gestartet ======");

    boolean createHibernateSession = (session == null);

    // Return-Informationen anlegen
    ListGloballySearchedConceptsResponseType response = new ListGloballySearchedConceptsResponseType();
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
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);

    /*int maxPageSizeUserSpecific = 10;
     if (parameter.getPagingParameter() != null && parameter.getPagingParameter().getUserPaging() != null)
     {
     if (parameter.getPagingParameter().getUserPaging())
     maxPageSizeUserSpecific = Integer.valueOf(parameter.getPagingParameter().getPageSize());
     }
     else
     {
     maxPageSizeUserSpecific = -1;
     }*/
    // PagingInfo
    int maxPageSize = 100;   // Gibt an, wieviele Treffer maximal zurückgegeben werden

    //Warum loggedIn hier? Das ergibt am Termbrowser folgenden Bug: Wenn man eingeloggt ist kann man sich keine HugeFlat Concept Liste mehr ansehen e.g. LOINC! => WrongValueException!
    /*if (noLimit)// || loggedIn) 
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
     logger.error("Fehler bei SysParameter.instance().getStringValue(\"maxPageSize\", null, null): " + e.getLocalizedMessage());
     }
     }*/
    int maxPageSizeSearch = 100;   // Gibt an, wieviele Treffer bei einer Suche maximal zurückgegeben werden

//    if (parameter != null && parameter.getSearchParameter() != null
//            && parameter.getSearchParameter().getTraverseConceptsToRoot() != null && parameter.getSearchParameter().getTraverseConceptsToRoot())
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
//            logger.error("Fehler bei SysParameter.instance().getStringValue(\"maxPageSizeSearch\", null, null): " + e.getLocalizedMessage());
//          }
//        }
//      }
//    }
    logger.debug("maxPageSize: " + maxPageSizeSearch);
    logger.debug("maxPageSizeSearch: " + maxPageSizeSearch);

    try
    {
      // Hibernate-Block, Session öffnen
      //org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Session hb_session = null;

      if (createHibernateSession)
        hb_session = HibernateUtil.getSessionFactory().openSession();
      else
        hb_session = session;

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {

        
        // HQL erstellen
        // Besonderheit hier: es dürfen keine Werte nachgeladen werden
        // Beim Abruf eines ICD wäre dieses sehr inperformant, da er für
        // jeden Eintrag sonst nachladen würde

//        String hql = "select distinct csc from CodeSystemConcept csc"
//                  + " join fetch csc.codeSystemEntityVersion csev"
//                  + " join fetch csev.codeSystemEntity cse"
//                  + " join fetch cse.codeSystemVersionEntityMemberships csvem";
        String hql = "select distinct cse from CodeSystemEntity cse"
                  + " join cse.codeSystemEntityVersions csev"
                  + " join csev.codeSystemConcepts csc"
                  + " join cse.codeSystemVersionEntityMemberships csvem";
        
        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getCodeSystemConcept()!= null)
        {
          // Hier alle Parameter aus der Cross-Reference einfügen
          // addParameter(String Prefix, String DBField, Object Value)
          parameterHelper.addParameter("csc.", "code", parameter.getCodeSystemConcept().getCode());
          parameterHelper.addParameter("csc.", "term", parameter.getCodeSystemConcept().getTerm());
          parameterHelper.addParameter("csc.", "termAbbrevation", parameter.getCodeSystemConcept().getTermAbbrevation());
          parameterHelper.addParameter("csc.", "isPreferred", parameter.getCodeSystemConcept().getIsPreferred());
        }

        if (loggedIn == false)
        {
          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Parameter hinzufügen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");
        hql += where;
        
        if(logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        List<CodeSystemEntity> conceptList = (java.util.List<CodeSystemEntity>) q.list();
        
        if(conceptList.size() > 0)
        {
          response.setCodeSystemEntity(new LinkedList<CodeSystemEntity>());
          
          int anzahlGesamt = conceptList.size();
          int count = 0;
          
          for(CodeSystemEntity cse : conceptList)
          {
            for(CodeSystemVersionEntityMembership csvem : cse.getCodeSystemVersionEntityMemberships())
            {
              csvem.setCodeSystemEntity(null);
              
              csvem.getCodeSystemVersion().setCodeSystemVersionEntityMemberships(null);
              csvem.getCodeSystemVersion().setLicenceTypes(null);
              csvem.getCodeSystemVersion().setLicencedUsers(null);
              csvem.getCodeSystemVersion().getCodeSystem().setCodeSystemVersions(null);
              csvem.getCodeSystemVersion().getCodeSystem().setMetadataParameters(null);
              csvem.getCodeSystemVersion().getCodeSystem().setDomainValues(null);
              
            }
            
            for(CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
            {
              csev.setCodeSystemEntity(null);
              
              csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
              csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
              csev.setAssociationTypes(null);
              csev.setValueSetMetadataValues(null);
              csev.setCodeSystemMetadataValues(null);
              
              for(CodeSystemConcept csc : csev.getCodeSystemConcepts())
              {
                csc.setCodeSystemConceptTranslations(null);
                csc.setCodeSystemEntityVersion(null);
              }
              
              for(ConceptValueSetMembership cvsm : csev.getConceptValueSetMemberships())
              {
                cvsm.setCodeSystemEntityVersion(null);
                
                //cvsm.setValueSetVersion(null);
                cvsm.getValueSetVersion().setConceptValueSetMemberships(null);
                cvsm.getValueSetVersion().getValueSet().setValueSetVersions(null);
                cvsm.getValueSetVersion().getValueSet().setMetadataParameters(null);
              }
            }
            
            response.getCodeSystemEntity().add(cse);
            
            count++;
            if(count >= maxPageSize)
            {
              break;
            }
          }
          
          
          
          // Treffermenge prüfen            
          // Paging wird aktiviert
          if (anzahlGesamt > maxPageSize)
          {
            response.setPagingInfos(new PagingResultType());
            response.getPagingInfos().setMaxPageSize(maxPageSize);
            response.getPagingInfos().setPageIndex(0);
            response.getPagingInfos().setPageSize(maxPageSize + "");
            response.getPagingInfos().setCount(anzahlGesamt);
            response.getPagingInfos().setMessage("Paging wurde aktiviert, da die Treffermenge größer ist als die maximale Seitengröße. Bitte grenzen Sie Ihre Treffer weiter ein.");
          }

          // Status an den Aufrufer weitergeben            
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl: " + count);
          response.getReturnInfos().setCount(count);
        }
        else
        {
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

        logger.error("Fehler bei 'ListCodeSystemConcepts', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
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

      logger.error("Fehler bei 'ListCodeSystemConcepts': " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    return response;
  }

  private void addTranslationToConcept(CodeSystemConcept csc, Object[] item)
  {
    if (item[19] == null)  // Term muss angegeben sein
      return;

    if (csc.getCodeSystemConceptTranslations() == null)
      csc.setCodeSystemConceptTranslations(new HashSet<CodeSystemConceptTranslation>());

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
      if (item[24] == null)  // Pflichtfeld
        return;

      CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
      cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
      cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId((Long) item[24]);
      cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
      cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId((Long) item[25]);

      if (item[26] != null)
        cseva.setLeftId((Long) item[26]);
      else
        logger.warn("LeftId ist null: " + csev.getVersionId());

      if (item[27] != null)
      {
        cseva.setAssociationType(new AssociationType());
        cseva.getAssociationType().setCodeSystemEntityVersionId((Long) item[27]);
      }

      if (item[28] != null)
        cseva.setAssociationKind((Integer) item[28]);
      if (item[29] != null)
        cseva.setStatus((Integer) item[29]);
      if (item[30] != null)
        cseva.setStatusDate((Date) item[30]);
      if (item[31] != null)
        cseva.setInsertTimestamp((Date) item[31]);

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
    catch (Exception ex)
    {
      logger.error("Fehler in addAssociationToEntityVersion(): " + ex.getLocalizedMessage());
      ex.printStackTrace();
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
  private boolean validateParameter(ListGloballySearchedConceptsRequestType Request,
          ListGloballySearchedConceptsResponseType Response)
  {
    boolean erfolg = true;

    CodeSystemConcept csc = Request.getCodeSystemConcept();
    if (csc == null)
    {
      Response.getReturnInfos().setMessage("CodeSystemConcept darf nicht leer sein!");
      erfolg = false;
    }
    else
    {
      //boolean csId = false;
      if ((csc.getCode() == null || csc.getCode().length() == 0)
              && (csc.getTerm() == null || csc.getTerm().length() == 0))
      {
        Response.getReturnInfos().setMessage(
                "Es muss entweder ein Code oder ein Term zur Suche angegeben sein.");
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

//  public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, boolean noLimit, String ipAddress)
//  {
//    return ListGloballySearchedConcepts(parameter, null, noLimit, ipAddress);
//  }
//
//  public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, org.hibernate.Session session, boolean noLimit, String ipAddress)
//  {
//    if (logger.isInfoEnabled())
//      logger.info("====== ListGloballySearchedConcepts gestartet ======");
//
//    boolean createHibernateSession = (session == null);
//
//    // Return-Informationen anlegen
//    ListGloballySearchedConceptsResponseType response = new ListGloballySearchedConceptsResponseType();
//    response.setReturnInfos(new ReturnType());
//    List<GlobalSearchResultEntry> gsreList = new ArrayList<GlobalSearchResultEntry>();
//
//    // Login-Informationen auswerten (gilt für jeden Webservice)
//    boolean loggedIn = false;
//    AuthenticateInfos loginInfoType = null;
//    if (parameter != null && parameter.getLoginToken() != null)
//    {
//      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
//      loggedIn = loginInfoType != null;
//    }
//
//    if (logger.isDebugEnabled())
//      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
//
//    // Hibernate-Block, Session öffnen
//    org.hibernate.Session hb_session = null;
//    Integer anzahl = 0;
//    
//    if (createHibernateSession)
//      hb_session = HibernateUtil.getSessionFactory().openSession();
//    else
//      hb_session = session;
//    
//    boolean code = false;
//    boolean term = false;
//    try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
//    {
//      if (parameter != null)
//      {
//
//        if (parameter.getCodeSystemConcepts() == null || parameter.getCodeSystemConcepts())
//        { // CS
//
//          String hqlGroupUsers = "select distinct csc from CodeSystemConcept csc join "
//                  + "csc.codeSystemEntityVersion csev join "
//                  + "csev.codeSystemEntity cse join "
//                  + "cse.codeSystemVersionEntityMemberships csvem join "
//                  + "csvem.codeSystemVersion csv join "
//                  + "csv.codeSystem cs where ";
//
//          if (!parameter.getCode().equals("") && parameter.getTerm().equals(""))
//          { //nur code
//
//            hqlGroupUsers += "csc.code like :code";
//            code = true;
//
//          }
//          else if (!parameter.getCode().equals("") && !parameter.getTerm().equals(""))
//          { // beide
//
//            hqlGroupUsers += "lower(csc.term) like :term and csc.code like :code";
//            code = true;
//            term = true;
//          }
//          else if (parameter.getCode().equals("") && !parameter.getTerm().equals(""))
//          { // nur term
//
//            hqlGroupUsers += "lower(csc.term) like :term";
//            term = true;
//          }
//          else
//          { // keine suche
//
//            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//            response.getReturnInfos().setStatus(ReturnType.Status.OK);
//            response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
//            response.getReturnInfos().setCount(0);
//            return response;
//          }
//
//          Query qGroupUsers = hb_session.createQuery(hqlGroupUsers);
//
//          if (term)
//            qGroupUsers.setParameter("term", "%" + parameter.getTerm() + "%");
//          if (code)
//            qGroupUsers.setParameter("code", parameter.getCode());
//
//          List<CodeSystemConcept> cscList = qGroupUsers.list();
//
//          for (CodeSystemConcept csc : cscList)
//          {
//            GlobalSearchResultEntry gsre = new GlobalSearchResultEntry();
//            gsre.setCodeSystemEntry(true);
//            gsre.setCode(csc.getCode());
//            gsre.setTerm(csc.getTerm());
//
//            gsre.setCodeSystemName(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
//                    iterator().next().getCodeSystemVersion().getCodeSystem().getName());
//            gsre.setCodeSystemVersionName(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
//                    iterator().next().getCodeSystemVersion().getName());
//
//            gsre.setCsId(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
//                    iterator().next().getCodeSystemVersion().getCodeSystem().getId());
//            gsre.setCsvId(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().
//                    iterator().next().getCodeSystemVersion().getVersionId());
//            gsre.setCsevId(csc.getCodeSystemEntityVersion().getVersionId());
//
//            gsreList.add(gsre);
//          }
//
//        }
//        else
//        {  // VS
//
//          String hqlGroupUsers = "select distinct cvsm from ConceptValueSetMembership cvsm join "
//                  + "cvsm.valueSetVersion vsv join "
//                  + "vsv.valueSet vs join "
//                  + "cvsm.codeSystemEntityVersion csev join "
//                  + "csev.codeSystemEntity cse join "
//                  + "csev.codeSystemConcepts csc join "
//                  + "cse.codeSystemVersionEntityMemberships csvem join "
//                  + "csvem.codeSystemVersion csv join "
//                  + "csv.codeSystem cs where ";
//
//          if (!parameter.getCode().equals("") && parameter.getTerm().equals(""))
//          { //nur code
//
//            hqlGroupUsers += "csc.code like :code";
//            code = true;
//
//          }
//          else if (!parameter.getCode().equals("") && !parameter.getTerm().equals(""))
//          { // beide
//
//            hqlGroupUsers += "lower(csc.term) like :term and csc.code like :code";
//            code = true;
//            term = true;
//          }
//          else if (parameter.getCode().equals("") && !parameter.getTerm().equals(""))
//          { // nur term
//
//            hqlGroupUsers += "lower(csc.term) like :term";
//            term = true;
//          }
//          else
//          { // keine suche
//
//            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//            response.getReturnInfos().setStatus(ReturnType.Status.OK);
//            response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
//            response.getReturnInfos().setCount(0);
//            return response;
//          }
//
//          Query qGroupUsers = hb_session.createQuery(hqlGroupUsers);
//
//          if (term)
//            qGroupUsers.setParameter("term", "%" + parameter.getTerm() + "%");
//          if (code)
//            qGroupUsers.setParameter("code", parameter.getCode());
//
//          List<ConceptValueSetMembership> cvsmList = qGroupUsers.list();
//
//          for (ConceptValueSetMembership cvsm : cvsmList)
//          {
//            GlobalSearchResultEntry gsre = new GlobalSearchResultEntry();
//            gsre.setCodeSystemEntry(false);
//            gsre.setCode(cvsm.getCodeSystemEntityVersion().getCodeSystemConcepts().iterator().next().getCode());
//            gsre.setTerm(cvsm.getCodeSystemEntityVersion().getCodeSystemConcepts().iterator().next().getTerm());
//
//            gsre.setValueSetName(cvsm.getValueSetVersion().getValueSet().getName());
//            gsre.setValueSetVersionName(cvsm.getValueSetVersion().getName());
//            String csName = cvsm.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().
//                    getCodeSystemVersion().getCodeSystem().getName();
//            String csvName = cvsm.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().
//                    getCodeSystemVersion().getName();
//            String oid = cvsm.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().iterator().next().
//                    getCodeSystemVersion().getOid();
//
//            gsre.setSourceCodeSystemInfo(csName + " (Version: " + csvName + " OID: " + oid + ")");
//
//            gsre.setVsId(cvsm.getValueSetVersion().getValueSet().getId());
//            gsre.setVsvId(cvsm.getValueSetVersion().getVersionId());
//            gsre.setCvsmId(cvsm.getId());
//
//            gsreList.add(gsre);
//          }
//        }
//
//        response.setGlobalSearchResultEntry(gsreList);
//
//        if (anzahl > 0)
//        {
//
//          // Status an den Aufrufer weitergeben           
//          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//          response.getReturnInfos().setStatus(ReturnType.Status.OK);
//          response.getReturnInfos().setMessage("Konzepte erfolgreich gelesen, Anzahl!");
//          response.getReturnInfos().setCount(anzahl);
//        }
//        else
//        {
//          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
//          response.getReturnInfos().setStatus(ReturnType.Status.OK);
//          response.getReturnInfos().setMessage("Keine Konzepte für die Filterkriterien vorhanden");
//          response.getReturnInfos().setCount(0);
//        }
//      }
//      else
//      {
//        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
//        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//        response.getReturnInfos().setMessage("Fehler bei 'ListGloballySearchedConcepts', parameter == null");
//      }
//    }
//    catch (Exception e)
//    {
//      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
//      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//      response.getReturnInfos().setMessage("Fehler bei 'ListGloballySearchedConcepts', Hibernate: " + e.getLocalizedMessage());
//      
//      LoggingOutput.outputException(e, this);
//    }
//    finally
//    {
//      // Transaktion abschließen
//      if (createHibernateSession)
//      {
//        if (hb_session != null)
//          hb_session.close();
//      }
//    }
//
//    return response;
//  }
}
