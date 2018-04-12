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
package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author Nico Hänsch
 */
public class ListConceptAssociations
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ListConceptAssociationsResponseType ListConceptAssociations(ListConceptAssociationsRequestType parameter, String ipAddress)
  {
    return ListConceptAssociations(parameter, null, ipAddress);
  }

  /**
   * Verbindungen zwischen Begriffen auflisten
   *
   * @param parameter
   * @return Liste von Entity-Assoziationen
   */
  public ListConceptAssociationsResponseType ListConceptAssociations(ListConceptAssociationsRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ListConceptAssociations gestartet ======");

    boolean createHibernateSession = (session == null);

    logger.debug("createHibernateSession: " + createHibernateSession);

    // Return-Informationen anlegen
    ListConceptAssociationsResponseType response = new ListConceptAssociationsResponseType();
    response.setReturnInfos(new ReturnType());

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt für jeden Webservice)
    boolean loggedIn = false;
    AuthenticateInfos loginInfoType = null;
    if (parameter != null && parameter.getLoginToken() != null)
    {
      loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType != null;
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("Benutzer ist eingeloggt: " + loggedIn);
    }

    try
    {
      java.util.List<CodeSystemConcept> list = null;

      // Hibernate-Block, Session öffnen
      org.hibernate.Session hb_session = session;

      if (createHibernateSession || hb_session == null)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        //hb_session.getTransaction().begin();
      }

      try //Try-Catch-Block um Hibernate-Fehler abzufangen
      {
        CodeSystemEntityVersion csev_parameter = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
        long cse_versionId = csev_parameter.getVersionId();
        logger.debug("cse_versionId: " + cse_versionId);

        // TODO leftID korrekt implementieren
        // directionBoth implementieren (reverse funktioniert)
        if (parameter.getDirectionBoth() == null)
        {
          parameter.setDirectionBoth(false);
        }

        if (parameter.getReverse() == null || parameter.getDirectionBoth())
        {
          parameter.setReverse(false);
        }

        //Hibernate Query Language erstellen
        String hql = "select distinct term from CodeSystemConcept term";
        //String hql = "select term from CodeSystemConcept term";
        hql += " join fetch term.codeSystemEntityVersion csev";

        hql += " join fetch csev.codeSystemEntity cse";

        // je nach Richtung wird die 1 oder 2 angehangen
        hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId";
        if (parameter.getReverse())
        {
          hql += "1";
        }
        else
        {
          hql += "2";
        }
        hql += " cseva";
        hql += " join fetch cseva.associationType at";

        hql += " join cseva.codeSystemEntityVersionByCodeSystemEntityVersionId";
        if (parameter.getReverse())
        {
          hql += "2";
        }
        else
        {
          hql += "1";
        }
        hql += " csev_source";

        HQLParameterHelper parameterHelper = new HQLParameterHelper();

        parameterHelper.addParameter("csev_source.", "versionId", cse_versionId);

        if (parameter.getCodeSystemEntityVersionAssociation() != null)
        {
          parameterHelper.addParameter("cseva.", "associationKind", parameter.getCodeSystemEntityVersionAssociation().getAssociationKind());
        }

        if (loggedIn == false)
        {
          parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
        }

        // Translations
        String languageCd = "";

        if (parameter.getCodeSystemEntity().getCodeSystemEntityVersions() != null
                && parameter.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
        {
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) parameter.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0];
          if (loggedIn)
          {
            parameterHelper.addParameter("csev.", "statusVisibilityDate", csev.getStatusVisibilityDate());
            parameterHelper.addParameter("csev.", "statusVisibility", csev.getStatusVisibility());
          }

          if (csev.getCodeSystemConcepts() != null && csev.getCodeSystemConcepts().size() > 0)
          {
            CodeSystemConcept csc = (CodeSystemConcept) csev.getCodeSystemConcepts().toArray()[0];

            if (csc.getCodeSystemConceptTranslations() != null && csc.getCodeSystemConceptTranslations().size() > 0)
            {
              CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) csc.getCodeSystemConceptTranslations().toArray()[0];

              if (csct.getLanguageCd() != null && csct.getLanguageCd().length() > 0)
              {
                languageCd = csct.getLanguageCd();
                if(languageCd != null && languageCd.length() > 0)
                {
                  hql += " left join term.codeSystemConceptTranslations csct ";
                  //parameterHelper.addParameter("csct.", "languageCd", languageCd);
                }
              }
            }
          }
        }
        
        logger.debug("languageCd: " + languageCd);
        

        // Parameter hinzufügen (immer mit AND verbunden)
        hql += parameterHelper.getWhere("");

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        // Query erstellen
        org.hibernate.Query q = hb_session.createQuery(hql);

        // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
        parameterHelper.applyParameter(q);
        
        //if(languageCd != null && languageCd.length() > 0)
        //  q.setString("languageCd", languageCd);

        // Datenbank-Aufruf durchführen
        list = (java.util.List<CodeSystemConcept>) q.list();

        if (logger.isDebugEnabled())
          logger.debug("size: " + list.size());

        List<CodeSystemEntityVersionAssociation> returnList = new LinkedList<CodeSystemEntityVersionAssociation>();

        //if (createHibernateSession)
        //  tx.commit();
        // Ergebnisliste befüllen
        //Iterator<CodeSystemConcept> it = list.iterator();
        //while (it.hasNext())
        for (CodeSystemConcept csc : list)
        {
          // CodeSystemEntityVersion lesen
          CodeSystemEntityVersion csev = csc.getCodeSystemEntityVersion();

          logger.debug("term found: " + csc.getCode());
          //logger.debug("csev-id: " + csc.getCodeSystemEntityVersionId());
          //logger.debug("csev-id: " + csc.getCodeSystemEntityVersion().getVersionId());
          
          if (csev != null)
          {
            //logger.debug("csev != null");
            
            csev.setAssociationTypes(null);
            //csev.setCodeSystemEntity(null);
            csev.setConceptValueSetMemberships(null);
            csev.setCodeSystemMetadataValues(null);
            csev.setValueSetMetadataValues(null);

            if (parameter != null && parameter.getReverse() != null && parameter.getReverse())
            {
              if (parameter != null && parameter.getLookForward() != null && parameter.getLookForward())
              {
                // Gibt immer die nächste Verbindung mit zurück
                Iterator<CodeSystemEntityVersionAssociation> itTemp = csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().iterator();

                while (itTemp.hasNext())
                {
                  CodeSystemEntityVersionAssociation csevaTemp = itTemp.next();
                  if (csevaTemp.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
                  {
                    csevaTemp.setAssociationType(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                  }
                  else
                  {
                    itTemp.remove();
                  }
                }
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null
                        && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().size() == 0)
                  csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
              }
              else
              {
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);
              }
            }
            else
            {
              if (parameter != null && parameter.getLookForward() != null && parameter.getLookForward())
              {
                logger.debug("lookForward");
                // Gibt immer die nächste Verbindung mit zurück
                Iterator<CodeSystemEntityVersionAssociation> itTemp = csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().iterator();

                while (itTemp.hasNext())
                {
                  logger.debug("Verbindung prüfen, iterator.next()");
                  CodeSystemEntityVersionAssociation csevaTemp = itTemp.next();
                  if (csevaTemp.getAssociationKind() == Definitions.ASSOCIATION_KIND.TAXONOMY.getCode())
                  {
                    csevaTemp.setAssociationType(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                    csevaTemp.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                    logger.debug("Verbindung taxonomisch, drin lassen");
                  }
                  else
                  {
                    logger.debug("Verbindung löschen, iterator.remove()");
                    itTemp.remove();
                  }
                }

                logger.debug("Anzahl: " + csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size());
                if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                        && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() == 0)
                  csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
              }
              else
              {
                csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
              }
              //csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
            }

            if (csev.getCodeSystemEntity() != null)
            {
              csev.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(null);
              csev.getCodeSystemEntity().setCodeSystemEntityVersions(null);
            }

            // der Version wieder das Concept hinzufügen und die Verbindungen null setzen
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csc.setCodeSystemEntityVersion(null);
            
            if(languageCd.length() == 0)
            {
              csc.setCodeSystemConceptTranslations(null);
            }
            else
            {
              // remove circle problems
              for (CodeSystemConceptTranslation trans : csc.getCodeSystemConceptTranslations())
              {
                trans.setCodeSystemConcept(null);
              }
            }
            
            csev.getCodeSystemConcepts().add(csc);

            // Assoziation lesen und Verbindungen auf null setzen
            if (parameter.getReverse() != null && parameter.getReverse())
            {
              logger.debug("adding reverse...");
              
              if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                      && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() > 0)
              {
                CodeSystemEntityVersionAssociation association = (CodeSystemEntityVersionAssociation) csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().toArray()[0];

                //association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                //association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                }

                // Verbindungen von AssociationType auf null setzen
                AssociationType at = association.getAssociationType();
                at.setCodeSystemEntityVersion(null);
                at.setCodeSystemEntityVersionAssociations(null);

                logger.debug("returnList.add(association) reverse with id: " + association.getId());
                returnList.add(association);
              }
              else logger.debug("...nothing to add");
            }
            else
            {
              //logger.debug("ass size: " + csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().size());

              if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() != null
                      && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().size() > 0)
              {
                CodeSystemEntityVersionAssociation association = (CodeSystemEntityVersionAssociation) csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().toArray()[0];

                association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);

                // Verbindungen von AssociationType auf null setzen
                AssociationType at = association.getAssociationType();
                at.setCodeSystemEntityVersion(null);
                at.setCodeSystemEntityVersionAssociations(null);

                logger.debug("returnList.add(association) with id: " + association.getId());
                returnList.add(association);
              }
            }
          }
          else
          {
            logger.debug("csev IS NULL!!");
          }
        }

        // DirectionBoth
        if (parameter.getDirectionBoth())
        {
          // Hibernate Query Language erstellen
          hql = "select distinct term from CodeSystemConcept term";
          //hql = "select term from CodeSystemConcept term";
          hql += " join fetch term.codeSystemEntityVersion csev";
          hql += " join fetch csev.codeSystemEntity cse";
          hql += " join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 cseva";
          hql += " join fetch cseva.associationType at";
          hql += " join cseva.codeSystemEntityVersionByCodeSystemEntityVersionId2 csev_source";

          parameterHelper = new HQLParameterHelper();
          parameterHelper.addParameter("csev_source.", "versionId", cse_versionId);

          if (parameter.getCodeSystemEntityVersionAssociation() != null)
          {
            parameterHelper.addParameter("cseva.", "associationKind", parameter.getCodeSystemEntityVersionAssociation().getAssociationKind());
          }

          if (loggedIn == false)
          {
            parameterHelper.addParameter("csev.", "statusVisibility", Definitions.STATUS_CODES.ACTIVE.getCode());
          }

          // Parameter hinzufügen (immer mit AND verbunden)
          hql += parameterHelper.getWhere("");

          if (logger.isDebugEnabled())
          {
            logger.debug("HQL#2 (DirectionBoth): " + hql);
            logger.debug("CSEV-VersionId: " + cse_versionId);
          }

          // Query erstellen
          Query q2 = hb_session.createQuery(hql);

          // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
          parameterHelper.applyParameter(q2);

          logger.debug("SQL: " + q2.getQueryString());

          // Datenbank-Aufruf durchführen
          java.util.List<CodeSystemConcept> list2 = (java.util.List<CodeSystemConcept>) q2.list();

          // Ergebnisliste befüllen
          for (CodeSystemConcept term : list2)
          {
            // CodeSystemConcept holen
            // CodeSystemEntityVersion lesen
            //logger.error("Term: " + term.getCodeSystemEntityVersionId() + ", " + term.getCode());

            CodeSystemEntityVersion csev = term.getCodeSystemEntityVersion();
            if (csev != null)
            {
              csev.setAssociationTypes(null);
              csev.setConceptValueSetMemberships(null);
              csev.setCodeSystemMetadataValues(null);
              csev.setValueSetMetadataValues(null);
              csev.setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(null);

              if (csev.getCodeSystemEntity() != null)
              {
                csev.getCodeSystemEntity().setCodeSystemVersionEntityMemberships(null);
                csev.getCodeSystemEntity().setCodeSystemEntityVersions(null);
              }

              // der Version wieder das Concept hinzufügen und die Verbindungen null setzen
              csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
              term.setCodeSystemEntityVersion(null);
              term.setCodeSystemConceptTranslations(null);
              csev.getCodeSystemConcepts().add(term);

              // Assoziation lesen und Verbindungen auf null setzen
              if (csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
                      && csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().size() > 0)
              {
                //CodeSystemEntityVersionAssociation association = 
                //(CodeSystemEntityVersionAssociation) csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().toArray()[0];
                CodeSystemEntityVersionAssociation association
                        = csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().iterator().next();

                if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else if (association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(null);
                }
                else
                {
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(null);
                  association.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(null);
                }

                // Verbindungen von AssociationType auf null setzen
                AssociationType at = association.getAssociationType();
                at.setCodeSystemEntityVersion(null);
                at.setCodeSystemEntityVersionAssociations(null);

                logger.debug("returnList.add(association) direction both with id: " + association.getId());
                returnList.add(association);
              }
            }
            else
            {
              logger.warn("ListConceptAssociations.java: CodeSystemEntityVersion ist null");
            }
          }

        }
        // Direction Both Ende

        response.setCodeSystemEntityVersionAssociation(returnList);
        if (returnList.isEmpty())
        {
          response.getReturnInfos().setMessage("Keine passenden Assoziationen vorhanden!");
        }
        else
        {
          response.getReturnInfos().setMessage("Assoziationen erfolgreich gelesen, Anzahl: " + returnList.size());
          response.getReturnInfos().setCount(returnList.size());
        }
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        // Fehlermeldung (Hibernate) an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'ListConceptAssociation', Hibernate: " + e.getLocalizedMessage());
        logger.error("Fehler bei 'ListConceptAssociation', Hibernate: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
      finally
      {
        // Transaktion abschließen
        if (createHibernateSession)
        {
          logger.debug("Schließe Hibernate-Session (ListConceptAssociations.java)");
          hb_session.close();
        }
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten            
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'ListConceptAssociation': " + e.getLocalizedMessage());
      logger.error("Fehler bei 'ListConceptAssociation': " + e.getLocalizedMessage());
    }
    return response;
  }

  private boolean validateParameter(ListConceptAssociationsRequestType Request, ListConceptAssociationsResponseType Response)
  {
    boolean parameterValidiert = true;

    // Prüfen ob eine CodeSystemEntity mitgegeben wurde (MUSS) 
    if (Request.getCodeSystemEntity() == null)
    {
      Response.getReturnInfos().setMessage("Es muss eine CodeSystemEntity übergeben werden.");
      parameterValidiert = false;
    }
    else
    {
      CodeSystemEntity codeSystemEntity = Request.getCodeSystemEntity();
      //Prüfen ob genau eine codeSystemEntityVersions mitgegeben wurde (MUSS)
      if (codeSystemEntity.getCodeSystemEntityVersions() == null || codeSystemEntity.getCodeSystemEntityVersions().size() != 1)
      {
        Response.getReturnInfos().setMessage("Es muss genau eine CodeSystemEntityVersions angegeben sein.");
        parameterValidiert = false;
      }
      else
      {
        //Prüfe ob eine versionID angegeben wurde (MUSS)
        CodeSystemEntityVersion vcsev = (CodeSystemEntityVersion) codeSystemEntity.getCodeSystemEntityVersions().toArray()[0];
        if (vcsev.getVersionId() == null || vcsev.getVersionId() <= 0)
        {
          Response.getReturnInfos().setMessage("Es muss eine ID für die CodeSystemEntity-Version angegeben sein!");
          parameterValidiert = false;
        }
      }

    }

    //Prüfen ob CodeSystemEntityVersionAssociation angegeben wurde (KANN)
    if (Request.getCodeSystemEntityVersionAssociation() != null)
    {
      //Prüfen ob AssociationKind angegeben wurde (MUSS)
      if (Request.getCodeSystemEntityVersionAssociation().getAssociationKind() == null
              || Definitions.isAssociationKindValid(Request.getCodeSystemEntityVersionAssociation().getAssociationKind()) == false)
      {
        Response.getReturnInfos().setMessage("AssociationKind darf nicht leer sein, wenn CodeSystemEntityVersionAssociation angegeben ist und muss einen der folgenden Werte haben: " + Definitions.readAssociationKinds());
        parameterValidiert = false;
      }
    }

    if (parameterValidiert == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return parameterValidiert;
  }
}
