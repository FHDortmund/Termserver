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
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Iterator;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ListCodeSystems
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  /**
   * Listet Vokabulare des Terminologieservers auf
   * 
   * @param parameter Die Parameter des Webservices
   * @return Ergebnis des Webservices, alle gefundenen Vokabulare mit angegebenen Filtern
   */
  public ListCodeSystemsResponseType ListCodeSystems(ListCodeSystemsRequestType parameter, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListCodeSystems gestartet ======");
    
    // Return-Informationen anlegen
    ListCodeSystemsResponseType response = new ListCodeSystemsResponseType();
    response.setReturnInfos(new ReturnType());
    
    // Parameter prüfen
    if(validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }
    
    // Login-Informationen auswerten (gilt für jeden Webservice)
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
    
    if(logger.isDebugEnabled())
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    
    try
    {
      java.util.List<CodeSystem> list = null;
        
      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      
      try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
      {
        // HQL erstellen
        String hql = "select distinct cs from CodeSystem cs";
        hql += " join fetch cs.codeSystemVersions csv";
        
        if(loggedIn)
        {
          hql += " left outer join csv.licencedUsers lu";
        }
        
        // Parameter dem Helper hinzufügen
        // bitte immer den Helper verwenden oder manuell Parameter per Query.setString() hinzufügen,
        // sonst sind SQL-Injections möglich
        HQLParameterHelper parameterHelper = new HQLParameterHelper(); 
        
        if(parameter != null && parameter.getCodeSystem() != null)
        {
          // Hier alle Parameter aus der Cross-Reference einfügen
          // addParameter(String Prefix, String DBField, Object Value)
          parameterHelper.addParameter("cs.", "name", parameter.getCodeSystem().getName());
          parameterHelper.addParameter("cs.", "description", parameter.getCodeSystem().getDescription());
          parameterHelper.addParameter("cs.", "insertTimestamp", parameter.getCodeSystem().getInsertTimestamp()); // z.B. '2011-09-26T15:40:00'
          
          if(parameter.getCodeSystem().getCodeSystemVersions() != null &&
            parameter.getCodeSystem().getCodeSystemVersions().size() > 0)
          {
            CodeSystemVersion csv = (CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0];
            
            parameterHelper.addParameter("csv.", "description", csv.getDescription());
            parameterHelper.addParameter("csv.", "expiredDate", csv.getExpirationDate());
            parameterHelper.addParameter("csv.", "insertTimestamp", csv.getInsertTimestamp());
            parameterHelper.addParameter("csv.", "description", csv.getLicenceHolder());
            parameterHelper.addParameter("csv.", "name", csv.getName());
            parameterHelper.addParameter("csv.", "oid", csv.getOid());
            parameterHelper.addParameter("csv.", "preferredLanguageCd", csv.getPreferredLanguageCd());
            parameterHelper.addParameter("csv.", "releaseDate", csv.getReleaseDate());
            parameterHelper.addParameter("csv.", "source", csv.getSource());
            parameterHelper.addParameter("csv.", "underLicence", csv.getUnderLicence());
            parameterHelper.addParameter("csv.", "validityRange", csv.getValidityRange());
            
            if(loggedIn)  // nur möglich, wenn eingeloggt
              parameterHelper.addParameter("csv.", "status", csv.getStatus());
          }
        }
        
        // hier: immer nur aktive Vokabulare abrufen
        if(isAdmin == false)
          parameterHelper.addParameter("csv.", "status", Definitions.STATUS_CODES.ACTIVE.getCode());
        
        if(loggedIn == false)
        {
          // ohne Login keine Vokabulare mit Lizenzen abrufen
          parameterHelper.addParameter("csv.", "underLicence", 0);
        }
        
        // Parameter hinzufügen (immer mit AND verbunden)
        String where = parameterHelper.getWhere("");
        hql += where;
        
        if(loggedIn)
        {
          // jetzt auf eine gültige Lizenz prüfen
          // muss manuell hinzugefügt werden (für Helper zu komplex, wg. OR)
          logger.debug("WHERE: "+ where);
          if(where.length() > 2)
            hql += " AND ";
          else hql += " WHERE ";
          
          /*hql += " (csv.underLicence = 0 OR ";
          hql += " (lu.validFrom < '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += " AND lu.validTo > '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += " AND lu.id.codeSystemVersionId=csv.versionId";
          hql += " AND lu.id.userId=" + loginInfoType.getUserId();
          hql += " ))";*/
          hql += " (csv.underLicence = 0 OR ";
          hql += " ";
          hql += " (";
          hql += "   (";
          hql += "     (";
          hql += "       lu.validFrom is NULL OR ";
          hql += "       lu.validFrom < '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += "     )";
          hql += "     AND";
          hql += "     (";
          hql += "       lu.validTo is NULL OR ";
          hql += "       lu.validTo > '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += "     )";
          // hql += "       AND lu.validTo > '" + HQLParameterHelper.getSQLDateStr(new java.util.Date()) + "'";
          hql += "   )";
          hql += "   AND lu.id.codeSystemVersionId=csv.versionId";
          hql += "   AND lu.id.userId=" + loginInfoType.getUserId();
          hql += " ))";
        }
        
        hql += " ORDER BY cs.name, csv.name";
        
        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);
        
        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);
        
        logger.debug("Query: " + q.getQueryString());
        
        // Datenbank-Aufruf durchführen
        list = (java.util.List<CodeSystem>) q.list();
        
        // Hibernate-Block wird in 'finally' geschlossen, erst danach
        // Auswertung der Daten
        // Achtung: hiernach können keine Tabellen/Daten mehr nachgeladen werden
      }
      catch(Exception e)
      {
        //hb_session.getTransaction().rollback();
          // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystems', Hibernate: " + e.getLocalizedMessage());
        
        logger.error("Fehler bei 'ListCodeSystems', Hibernate: " + e.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }
      
      // Ergebnis auswerten
      // Später wird die Klassenstruktur von Jaxb in die XML-Struktur umgewandelt
      // dafür müssen nichtbenötigte Beziehungen gelöscht werden (auf null setzen)
      if(list != null)
      {
        Iterator<CodeSystem> iterator = list.iterator();
        
        while(iterator.hasNext())
        {
          CodeSystem cs = iterator.next();
          
          //logger.debug("CS: " + cs.getName());
          
          if(cs.getCodeSystemVersions() != null)
          {
            Iterator<CodeSystemVersion> iteratorVV = cs.getCodeSystemVersions().iterator();
            
            while(iteratorVV.hasNext())
            {
              CodeSystemVersion csv = iteratorVV.next();
              
              //logger.debug("CSV: " + csv.getName());
              //logger.debug("ValidityRange: " + csv.getValidityRange());
              
              // Nicht anzuzeigende Beziehungen null setzen
              csv.setLicenceTypes(null);
              csv.setLicencedUsers(null);
              csv.setCodeSystemVersionEntityMemberships(null);
              csv.setCodeSystem(null);
            }
          }
          
          cs.setDomainValues(null);  // Keine zugehörigen Domänen zurückgeben
          cs.setMetadataParameters(null);
          
          // bereinigte Liste der Antwort beifügen
          response.setCodeSystem(list);
          response.getReturnInfos().setCount(list.size());
        }
        
        // Status an den Aufrufer weitergeben
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("CodeSysteme erfolgreich gelesen");
      }
    }
    catch(Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListCodeSystems': " + e.getLocalizedMessage());
      
      logger.error("Fehler bei 'ListCodeSystems': " + e.getLocalizedMessage());
    }
    
    return response;
  }
  
  
  private boolean validateParameter(ListCodeSystemsRequestType Request,
						    ListCodeSystemsResponseType Response)
  {
    // hier muss nichts geprüft werden, da man bei der Suche auch
    // alle Angaben leer lassen kann
    return true;
  }
}
