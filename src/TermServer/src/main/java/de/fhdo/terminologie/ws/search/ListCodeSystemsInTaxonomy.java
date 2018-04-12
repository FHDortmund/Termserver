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
import de.fhdo.terminologie.DomainIDs;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListCodeSystemsInTaxonomy
{
  
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   * lists code systems and value sets in a taxonomy of a specific domain
   *
   * @param parameter parameters for this webservice
   * @return all domain values (tree) with linked codesystems/valuesets
   * 
   * edited 2017-06-21 <robert.muetzner@fh-dortmund.de>: return value sets, too
   */
  public ListCodeSystemsInTaxonomyResponseType ListCodeSystemsInTaxonomy(ListCodeSystemsInTaxonomyRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListCodeSystemsInTaxonomy started ======");

    // create return information
    ListCodeSystemsInTaxonomyResponseType response = new ListCodeSystemsInTaxonomyResponseType();
    response.setReturnInfos(new ReturnType());

    // check parameters
    if (validateParameter(parameter, response) == false)
    {
      return response; // error with parameters
    }

    // check login information (valid for all webservices)
    boolean loggedIn = false;
    boolean isAdmin = false;
    AuthenticateInfos loginInfoType = null;
    if (parameter != null && parameter.getLoginToken()!= null)
    {
      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType.isLoggedIn();
      if(loggedIn)
        isAdmin = loginInfoType.isIsAdmin();
    }

    if (logger.isDebugEnabled())
      logger.debug("user is logged in: " + loggedIn);


    // read all code systems
    List<CodeSystem> allCodesystemList;
    ListCodeSystemsRequestType lcsRequest = new ListCodeSystemsRequestType();
    if (parameter != null)
      lcsRequest.setLoginToken(parameter.getLoginToken());
    ListCodeSystems lcs = new ListCodeSystems();
    ListCodeSystemsResponseType lcsResponse = lcs.ListCodeSystems(lcsRequest, ipAddress);
    allCodesystemList = lcsResponse.getCodeSystem();
    
    if(allCodesystemList == null)
      allCodesystemList = new LinkedList<CodeSystem>();
    
    // read all value sets
    List<ValueSet> allValuesetList;
    ListValueSetsRequestType lvsRequest = new ListValueSetsRequestType();
    if (parameter != null)
      lvsRequest.setLoginToken(parameter.getLoginToken());
    ListValueSets lvs = new ListValueSets();
    ListValueSetsResponseType lvsResponse = lvs.ListValueSets(lvsRequest, ipAddress);
    allValuesetList = lvsResponse.getValueSet();
    
    if(allValuesetList == null)
      allValuesetList = new LinkedList<ValueSet>();
    
    
    try
    {
      java.util.List<DomainValue> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      
      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct dmv from DomainValue dmv left join fetch dmv.codeSystems cs ";
        hql += " left join fetch cs.codeSystemVersions csv";
        
        hql += " left join fetch dmv.valueSets vs";
        hql += " left join fetch vs.valueSetVersions vsv";

        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        parameterHelper.addParameter("", "domainId", DomainIDs.CODESYSTEM_TAXONOMY);

        // Parameter hinzufügen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");

        if (loggedIn == false)
        {
          where += " and (csv.status=" + Definitions.STATUS_CODES.ACTIVE.getCode() + " or cs is null)";
          
          where += " and (vsv.status=" + Definitions.STATUS_CODES.ACTIVE.getCode() + " or vs is null)";
        }

        hql += where;

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        /*if (domain.getDisplayOrder() != null
         && domain.getDisplayOrder() == Definitions.DISPLAYORDER_ID)
         {
         hql += " order by domain_value_id";
         }
         else if (domain.getDisplayOrder() != null
         && domain.getDisplayOrder() == Definitions.DISPLAYORDER_ORDERID)
         {
         hql += " order by order_no";
         }
         else
         hql += " order by domain_display";*/

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);

        // Datenbank-Aufruf durchführen
        list = q.list();

        // Hibernate-Block wird in 'finally' geschlossen
        // Ergebnis auswerten
        // Später wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
        // dafür müssen nichtbenötigte Beziehungen gelöscht werden (auf null setzen)

        int count = 0;
        response.setDomainValue(new LinkedList<DomainValue>());

        if (list != null)
        {
          /*Iterator<DomainValue> iterator = list.iterator();
          logger.debug("Size: " + list.size());

          while (iterator.hasNext())
          {
            DomainValue dmv = iterator.next();*/
          for(DomainValue dmv : list)
          {
            if (dmv.getDomainValuesForDomainValueId1() != null && dmv.getDomainValuesForDomainValueId1().size() > 0)
            {
              // kein Root-Element
              continue;
            }
            //else logger.debug("NORMAL: " + dmv.getDomainCode());

            count += applyAllDomainValue(dmv, response.getDomainValue(), 0, allCodesystemList, allValuesetList);
            //count += applyDomainValue(dmv, response.getDomainValue(), 0, allCodesystemList);
            //count += applyVSDomainValue(dmv, response.getDomainValue(), 0, allValuesetList);
          }

          // cleanup list
          cleanUpList(response.getDomainValue());

          if (allCodesystemList.size() > 0 || allValuesetList.size() > 0)
          {
            // add all other code systems and value sets
            DomainValue otherDV = new DomainValue();
            otherDV.setDomainCode("other");
            otherDV.setDomainDisplay("Sonstige");
            //otherDV.setCodeSystems(new HashSet<CodeSystem>());

            for (int i = 0; i < allCodesystemList.size(); ++i)
            {
              if(otherDV.getCodeSystems() == null)
                otherDV.setCodeSystems(new HashSet<CodeSystem>());
              
              //otherDV.setDomainValuesForDomainValueId2(new HashSet<DomainValue>());
              otherDV.getCodeSystems().add(allCodesystemList.get(i));
            }
            
            for (int i = 0; i < allValuesetList.size(); ++i)
            {
              if(otherDV.getValueSets() == null)
                otherDV.setValueSets(new HashSet<ValueSet>());
              
              otherDV.getValueSets().add(allValuesetList.get(i));
            }

            response.getDomainValue().add(otherDV);
          }
          


          response.getReturnInfos().setCount(count);

          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("taxonomy successfully obtained");
        }

      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("error at 'ListCodeSystemsInTaxonomy', hibernate: " + e.getLocalizedMessage());

        logger.error("error at 'ListCodeSystemsInTaxonomy', hibernate: " + e.getLocalizedMessage());
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("error at 'ListCodeSystemsInTaxonomy': " + e.getLocalizedMessage());

      logger.error("error at 'ListCodeSystemsInTaxonomy': " + e.getLocalizedMessage());
      LoggingOutput.outputException(e, this);
    }

    return response;
  }

  private void cleanUpList(List<DomainValue> list)
  {
    if (list == null || list.size() == 0)
      return;

    for (int i = 0; i < list.size(); ++i)
    {
      DomainValue dv = list.get(i);
      cleanUpEntry(dv);
    }
  }

  private void cleanUpEntry(DomainValue dv)
  {
    dv.setDomainValuesForDomainValueId1(null);

    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      Iterator<DomainValue> itDV2 = dv.getDomainValuesForDomainValueId2().iterator();

      while (itDV2.hasNext())
      {
        DomainValue dv2 = itDV2.next();
        cleanUpEntry(dv2);
      }
    }
  }

  private int applyDomainValue(DomainValue dv, List<DomainValue> list, int sum, List<CodeSystem> allCodesystemList)
  {
    int count = sum;

    dv.setDomain(null);
    dv.setSysParamsForModifyLevel(null);
    dv.setSysParamsForValidityDomain(null);

    // Zugehörige Codesysteme mit zurückgeben (mit Versionen)
    // TODO Berechtigung prüfen
    if (dv.getCodeSystems() != null)
    {
      Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

      while (iteratorCS.hasNext())
      {
        CodeSystem cs = iteratorCS.next();
        cs.setDomainValues(null);
        cs.setMetadataParameters(null);

        Iterator<CodeSystemVersion> iteratorCSV = cs.getCodeSystemVersions().iterator();
        while (iteratorCSV.hasNext())
        {
          CodeSystemVersion csv = iteratorCSV.next();
          csv.setCodeSystem(null);
          csv.setCodeSystemVersionEntityMemberships(null);
          csv.setLicenceTypes(null);
          csv.setLicencedUsers(null);
        }

        // In Liste entfernen
        for (int i = 0; i < allCodesystemList.size(); ++i)
        {
          if (allCodesystemList.get(i).getId().longValue() == cs.getId().longValue())
          {
            allCodesystemList.remove(i);
            break;
          }
        }
      }
    }

    logger.debug("Pruefe: " + dv.getDomainCode());

    if (dv.getDomainValuesForDomainValueId1() != null && dv.getDomainValuesForDomainValueId1().size() > 0)
    {
      logger.debug("Value1: " + ((DomainValue) dv.getDomainValuesForDomainValueId1().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value1: null");

    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      logger.debug("Value2: " + ((DomainValue) dv.getDomainValuesForDomainValueId2().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value2: null");

    // Beziehungen
    boolean root = (dv.getDomainValuesForDomainValueId1() == null || dv.getDomainValuesForDomainValueId1().size() == 0);
    // kann hier noch nicht auf null gesetzt werden, da die Liste sonst durcheinander gerät
    //dv.setDomainValuesForDomainValueId1(null);
    //dv.setDomainValuesForDomainValueId2(null);
    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      Iterator<DomainValue> iteratorDV2 = dv.getDomainValuesForDomainValueId2().iterator();

      while (iteratorDV2.hasNext())
      {
        DomainValue dv2 = iteratorDV2.next();
        count = applyDomainValue(dv2, list, sum, allCodesystemList);
      }
    }
    else
      dv.setDomainValuesForDomainValueId2(null);

    if (root)
    {
      list.add(dv);
    }
    count++;
    return count;
  }
  
  private int applyVSDomainValue(DomainValue dv, List<DomainValue> list, int sum, List<ValueSet> allValuesetList)
  {
    int count = sum;

    dv.setDomain(null);
    dv.setSysParamsForModifyLevel(null);
    dv.setSysParamsForValidityDomain(null);

    // assigned value sets with versions
    if (dv.getValueSets() != null)
    {
      for(ValueSet vs : dv.getValueSets())
      {
        //vs.setDomainValues(null);
        vs.setMetadataParameters(null);

        for(ValueSetVersion vsv : vs.getValueSetVersions())
        {
          vsv.setValueSet(null);
          vsv.setConceptValueSetMemberships(null);
        }

        // remove from list (others)
        for (int i = 0; i < allValuesetList.size(); ++i)
        {
          if (allValuesetList.get(i).getId().longValue() == vs.getId().longValue())
          {
            allValuesetList.remove(i);
            break;
          }
        }
      }
    }

    logger.debug("Pruefe: " + dv.getDomainCode());

    if (dv.getDomainValuesForDomainValueId1() != null && dv.getDomainValuesForDomainValueId1().size() > 0)
    {
      logger.debug("Value1: " + ((DomainValue) dv.getDomainValuesForDomainValueId1().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value1: null");

    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      logger.debug("Value2: " + ((DomainValue) dv.getDomainValuesForDomainValueId2().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value2: null");

    // relations
    boolean root = (dv.getDomainValuesForDomainValueId1() == null || dv.getDomainValuesForDomainValueId1().size() == 0);
    
    // kann hier noch nicht auf null gesetzt werden, da die Liste sonst durcheinander gerät
    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      //Iterator<DomainValue> iteratorDV2 = dv.getDomainValuesForDomainValueId2().iterator();

      //while (iteratorDV2.hasNext())
      for(DomainValue dv2 : dv.getDomainValuesForDomainValueId2())
      {
        //DomainValue dv2 = iteratorDV2.next();
        count = applyVSDomainValue(dv2, list, sum, allValuesetList);
      }
    }
    else
      dv.setDomainValuesForDomainValueId2(null);

    if (root)
    {
      list.add(dv);
    }
    count++;
    return count;
  }
  
  
  private int applyAllDomainValue(DomainValue dv, List<DomainValue> list, int sum, List<CodeSystem> allCodesystemList, List<ValueSet> allValuesetList)
  {
    int count = sum;

    dv.setDomain(null);
    dv.setSysParamsForModifyLevel(null);
    dv.setSysParamsForValidityDomain(null);

    // Zugehörige Codesysteme mit zurückgeben (mit Versionen)
    // TODO Berechtigung prüfen
    if (dv.getCodeSystems() != null)
    {
      Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

      while (iteratorCS.hasNext())
      {
        CodeSystem cs = iteratorCS.next();
        cs.setDomainValues(null);
        cs.setMetadataParameters(null);

        Iterator<CodeSystemVersion> iteratorCSV = cs.getCodeSystemVersions().iterator();
        while (iteratorCSV.hasNext())
        {
          CodeSystemVersion csv = iteratorCSV.next();
          csv.setCodeSystem(null);
          csv.setCodeSystemVersionEntityMemberships(null);
          csv.setLicenceTypes(null);
          csv.setLicencedUsers(null);
        }

        // In Liste entfernen
        for (int i = 0; i < allCodesystemList.size(); ++i)
        {
          if (allCodesystemList.get(i).getId().longValue() == cs.getId().longValue())
          {
            allCodesystemList.remove(i);
            break;
          }
        }
      }
    }
    
    // assigned value sets with versions
    if (dv.getValueSets() != null)
    {
      for(ValueSet vs : dv.getValueSets())
      {
        //vs.setDomainValues(null);
        vs.setMetadataParameters(null);

        for(ValueSetVersion vsv : vs.getValueSetVersions())
        {
          vsv.setValueSet(null);
          vsv.setConceptValueSetMemberships(null);
        }

        // remove from list (others)
        for (int i = 0; i < allValuesetList.size(); ++i)
        {
          if (allValuesetList.get(i).getId().longValue() == vs.getId().longValue())
          {
            allValuesetList.remove(i);
            break;
          }
        }
      }
    }

    logger.debug("Pruefe: " + dv.getDomainCode());

    if (dv.getDomainValuesForDomainValueId1() != null && dv.getDomainValuesForDomainValueId1().size() > 0)
    {
      logger.debug("Value1: " + ((DomainValue) dv.getDomainValuesForDomainValueId1().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value1: null");

    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      logger.debug("Value2: " + ((DomainValue) dv.getDomainValuesForDomainValueId2().toArray()[0]).getDomainCode());
    }
    else
      logger.debug("Value2: null");

    // Beziehungen
    boolean root = (dv.getDomainValuesForDomainValueId1() == null || dv.getDomainValuesForDomainValueId1().size() == 0);
    // kann hier noch nicht auf null gesetzt werden, da die Liste sonst durcheinander gerät
    //dv.setDomainValuesForDomainValueId1(null);
    //dv.setDomainValuesForDomainValueId2(null);
    if (dv.getDomainValuesForDomainValueId2() != null && dv.getDomainValuesForDomainValueId2().size() > 0)
    {
      for(DomainValue dv2 : dv.getDomainValuesForDomainValueId2())
      {
        count = applyAllDomainValue(dv2, list, sum, allCodesystemList, allValuesetList);
      }
    }
    else
      dv.setDomainValuesForDomainValueId2(null);

    if (root)
    {
      list.add(dv);
    }
    count++;
    return count;
  }

  private boolean validateParameter(ListCodeSystemsInTaxonomyRequestType Request, ListCodeSystemsInTaxonomyResponseType Response)
  {
    /*boolean erfolg = true;


     if (erfolg == false)
     {
     Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
     Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
     }
     return erfolg;*
     */
    return true;

  }
}
