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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**

 @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListDomainValues
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /**
   Listet Domains des Terminologieservers auf

   @param parameter Die Parameter des Webservices
   @return Ergebnis des Webservices, alle gefundenen Domains mit angegebenen Filtern
   */
  public ListDomainValuesResponseType ListDomainValues(ListDomainValuesRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListDomainValues gestartet ======");

    // Return-Informationen anlegen
    ListDomainValuesResponseType response = new ListDomainValuesResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    try
    {
      java.util.List<DomainValue> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct dmv from DomainValue dmv left join fetch dmv.codeSystems cs ";

        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        if (parameter != null && parameter.getDomain() != null)
        {
          // Hier alle Parameter aus der Cross-Reference einfügen
          // addParameter(String Prefix, String DBField, Object Value)
          //parameterHelper.addParameter("dmv.", "domain.domainId", parameter.getDomain().getDomainId());
          parameterHelper.addParameter("", "domainId", parameter.getDomain().getDomainId());
        }

        //parameterHelper.addParameter("dmv.", "domainValuesForDomainValueId1", "NULL");

        // Parameter hinzufügen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");
        hql += where;

        /*if(where.length() < 5)
         hql += " WHERE ";
         else hql += " AND ";
        
         //hql += " dmv.domainValuesForDomainValueId1 is null";
         hql += " dmv.domainValuesForDomainValueId1 is null";*/

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

        //tx.commit();

        // Hibernate-Block wird in 'finally' geschlossen
        // Ergebnis auswerten
        // Später wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
        // dafür müssen nichtbenötigte Beziehungen gelöscht werden (auf null setzen)

        int count = 0;
        response.setDomainValues(new LinkedList<DomainValue>());

        if (list != null)
        {
          Iterator<DomainValue> iterator = list.iterator();
          logger.debug("Size: " + list.size());

          while (iterator.hasNext())
          {
            DomainValue dmv = iterator.next();

            if (dmv.getDomainValuesForDomainValueId1() != null && dmv.getDomainValuesForDomainValueId1().size() > 0)
            {
              //logger.debug("CONTINUE: " + dmv.getDomainCode());
              continue;
            }
            //else logger.debug("NORMAL: " + dmv.getDomainCode());

            count += applyDomainValue(dmv, response.getDomainValues(), 0);
          }

          // Liste bereinigen
          cleanUpList(response.getDomainValues());

          response.getReturnInfos().setCount(count);

          // Status an den Aufrufer weitergeben
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setMessage("DomainValues erfolgreich gelesen");
        }


        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
          // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'DomainValues', Hibernate: " + e.getLocalizedMessage());

        logger.error("Fehler bei 'DomainValues', Hibernate: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        hb_session.close();
      }


    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'DomainValues': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'DomainValues': " + e.getLocalizedMessage());
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

  private int applyDomainValue(DomainValue dv, List<DomainValue> list, int sum)
  {
    int count = sum;

    dv.setDomain(null);
    dv.setSysParamsForModifyLevel(null);
    dv.setSysParamsForValidityDomain(null);

    // Zugehörige Codesysteme mit zurückgeben (ohne Versionen)
    if (dv.getCodeSystems() != null)
    {
      Iterator<CodeSystem> iteratorCS = dv.getCodeSystems().iterator();

      while (iteratorCS.hasNext())
      {
        CodeSystem cs = iteratorCS.next();
        cs.setCodeSystemVersions(null);
        cs.setDomainValues(null);
        cs.setCodeSystemType(null);
        cs.setDescription(null);
        cs.setCurrentVersionId(null);
        cs.setInsertTimestamp(null);
        cs.setMetadataParameters(null);
      }
    }

    /*logger.debug("Pruefe: " + dv.getDomainCode());

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
      logger.debug("Value2: null");*/

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
        count = applyDomainValue(dv2, list, sum);
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

  private boolean validateParameter(ListDomainValuesRequestType Request, ListDomainValuesResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getDomain() == null)
    {
      Response.getReturnInfos().setMessage(
        "Es muss eine Domain angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDomainId() == null)
    {
      Response.getReturnInfos().setMessage(
        "Es muss eine DomainId angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDomainName() != null)
    {
      Response.getReturnInfos().setMessage(
        "DomainName darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDomainOid() != null)
    {
      Response.getReturnInfos().setMessage(
        "DomainOid darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDescription() != null)
    {
      Response.getReturnInfos().setMessage(
        "Description darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDisplayText() != null)
    {
      Response.getReturnInfos().setMessage(
        "DisplayText darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getIsOptional() != null)
    {
      Response.getReturnInfos().setMessage(
        "IsOptional darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDefaultValue() != null)
    {
      Response.getReturnInfos().setMessage(
        "DefaultValue darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDomainType() != null)
    {
      Response.getReturnInfos().setMessage(
        "DomainType darf nicht angegeben sein!");
      erfolg = false;
    }
    else if (Request.getDomain().getDisplayOrder() != null)
    {
      Response.getReturnInfos().setMessage(
        "DisplayOrder darf nicht angegeben sein!");
      erfolg = false;
    }
    else if ((Request.getDomain().getDomainValues() != null)
      && (Request.getDomain().getDomainValues().size() > 0))
    {
      Response.getReturnInfos().setMessage(
        "Werte zu domainValue dürfen nicht angegeben sein!");
      erfolg = false;
    }


    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;

  }
}
