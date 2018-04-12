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
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateValueSetContent
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public CreateValueSetContentResponseType CreateValueSetContent(CreateValueSetContentRequestType parameter, String ipAddress)
  {
    return CreateValueSetContent(parameter, null, ipAddress);
  }

  public CreateValueSetContentResponseType CreateValueSetContent(CreateValueSetContentRequestType parameter, org.hibernate.Session session, String ipAddress)
  {
////////// Logger //////////////////////////////////////////////////////////////
    if (logger.isInfoEnabled())
      logger.info("====== CreateValueSetContent gestartet ======");

    boolean createHibernateSession = (session == null);

////////// Return-Informationen anlegen ////////////////////////////////////////
    CreateValueSetContentResponseType response = new CreateValueSetContentResponseType();
    response.setReturnInfos(new ReturnType());

////////// Parameter prüfen ////////////////////////////////////////////////////
    if (validateParameter(parameter, response) == false)
      return response;

    // Login-Informationen auswerten (gilt für jeden Webservice)    
    boolean loggedIn = false;
    if (parameter != null && parameter.getLoginToken() != null)
    {
      AuthenticateInfos loginInfoType = Authorization.authenticate(ipAddress, parameter.getLoginToken());
      loggedIn = loginInfoType != null;
    }

    if (loggedIn == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("You have to be logged in to use this service.");
      return response;
    }

////////// Der eigentliche Teil  ///////////////////////////////////////////////        
    try
    {
      // Hibernate-Block, Session und Transaction öffnen
      org.hibernate.Session hb_session = null;

      if (createHibernateSession)
      {
        hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
      }
      else
      {
        hb_session = session;
      }

      // Objekte erstellen
      CodeSystemEntity cse = null;
      int iCount = 0;
      int countDuplicated = 0;

      // VSV Id bleibt immer die gleiche
      long idVsv = ((ValueSetVersion) (parameter.getValueSet().getValueSetVersions().toArray()[0])).getVersionId();
      long idCsev;

      // Iterator für die CSEs, da ja mehrere CSEs angegeben werden können die alle auf idVsv verweisen sollen
      Iterator<CodeSystemEntity> iterCse = parameter.getCodeSystemEntity().iterator();

      try
      { // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern        
        // Durchlaufe alle CodeSystemEntities
        while (iterCse.hasNext())
        {
          cse = iterCse.next();

          // für jedes CSE eine neue association anlegen die dann via save() in die DB geschrieben werden sollen

          // Id von der CSEVersion einlesen
          idCsev = ((CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0]).getVersionId();
          CodeSystemEntityVersion csev = (CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0];
          ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
          if (csev != null && !csev.getConceptValueSetMemberships().isEmpty())
            cvsm = csev.getConceptValueSetMemberships().iterator().next();
          
          // da Hibernate keinen Save auf Objekten ausführen kann die mehr als einen PK haben, 
          // muss der Umweg über eine spezielle, von Hibernate erzeugte Klasse, "ConceptValueSetMembershipId" 
          // gegangen werden. Diese erzeugt aus den PKs (hier 2) eine Id, unter der dann das Objekt gespeichert wird.
          //
          // Die fremdPKs müssen nicht erneut angegeben werden, da sie schon Teil bei der Erzeugung der Id waren.
          ConceptValueSetMembershipId ms_id = new ConceptValueSetMembershipId(idCsev, idVsv);

          // prüfen, ob bereits vorhanden
          if (hb_session.get(ConceptValueSetMembership.class, ms_id) == null)
          {
            cvsm.setId(ms_id);
            cvsm.setStatus(1); // Default ist 1: Wenn Vorschlag dann unbedingt update auf 0 machen!

            cvsm.setStatusDate(new Date());

            cvsm.setOrderNr(0l);
            cvsm.setIsStructureEntry(false);
              
            //if (cvsm != null)
            /*{
              if (cvsm.getValueOverride() != null)
                cvsm.setValueOverride(cvsm.getValueOverride());
              if (cvsm.getDescription() != null)
                cvsm.setDescription(cvsm.getDescription());
              if (cvsm.getMeaning() != null)
                cvsm.setMeaning(cvsm.getMeaning());
              if (cvsm.getHints() != null)
                cvsm.setHints(cvsm.getHints());
              if (cvsm.getOrderNr() != null && !cvsm.getOrderNr().equals(0l))
                cvsm.setOrderNr(cvsm.getOrderNr());
              if (cvsm.getIsStructureEntry() != null)
                cvsm.setIsStructureEntry(cvsm.getIsStructureEntry());
            }*/

            // In DB speichern mit der entsprechenden Id. 
            hb_session.save(cvsm);

            // Überprüfe ob das Speichern erfolgreich war
            if (cvsm == null || cvsm.getId() == null)
            {

            }
            else
            {

              iCount++;
              //Setup default ValueSetMetadataValue
              //Check ob MetadataParameter default Values angelegt werden müssen
              String hql = "select distinct mp from MetadataParameter mp";
              hql += " join fetch mp.valueSet vs";

              HQLParameterHelper parameterHelper = new HQLParameterHelper();
              parameterHelper.addParameter("vs.", "id", parameter.getValueSet().getId());

              // Parameter hinzufügen (immer mit AND verbunden)
              hql += parameterHelper.getWhere("");
              logger.debug("HQL: " + hql);

              // Query erstellen
              org.hibernate.Query q = hb_session.createQuery(hql);
              parameterHelper.applyParameter(q);

              List<MetadataParameter> mpList = q.list();
              if (!mpList.isEmpty())
              {
                Iterator<MetadataParameter> iter = mpList.iterator();
                while (iter.hasNext())
                {
                  MetadataParameter mp = (MetadataParameter) iter.next();
                  ValueSetMetadataValue vsmv = new ValueSetMetadataValue();
                  vsmv.setParameterValue("");
                  //vsmv.setCodeSystemEntityVersionId(idCsev);
                  vsmv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                  vsmv.getCodeSystemEntityVersion().setVersionId(idCsev);
                  vsmv.setValuesetVersionId(idVsv);
                  vsmv.setMetadataParameter(mp);
                  hb_session.save(vsmv);
                }
              }
            }
          }
          else
          {
            logger.debug("Eintrag im VS bereits vorhanden mit csev-ID: " + idCsev);
            countDuplicated++;
          }

        }

        LastChangeHelper.updateLastChangeDate(false, idVsv, hb_session);
        if (createHibernateSession)
        {
          hb_session.getTransaction().commit();
        }

        response.getReturnInfos().setCount(1);
      }
      catch (Exception e)
      {
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'CreateValueSetContent', Hibernate: " + e.getLocalizedMessage());

        logger.error(response.getReturnInfos().getMessage());

        e.printStackTrace();

        if (createHibernateSession)
          hb_session.getTransaction().rollback();
      }
      finally
      {
        if (createHibernateSession)
        {
          hb_session.close();
        }
      }

      // War alles OK?
      if (iCount > 0 || countDuplicated > 0)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Es wurden " + iCount + " ValueSetContent(s) erfolgreich erstellt, " + countDuplicated + " sind bereits vorhanden.");
        response.getReturnInfos().setCount(iCount);
      }
      else
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Es wurden keine Konzepte in das Value Set eingefügt.");
      }

    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler beim Ausführen des Webservice 'CreateValueSetContent': \n" + e.getLocalizedMessage()
              + " \n Möglicherweise wurde das Konzept schon mit dem Value Set verbunden oder die IDs der CodeSystemEntityVersion oder ValueSetVersion sind nicht gültig.");
      logger.error(response.getReturnInfos().getMessage());
      e.printStackTrace();
    }
    return response;
  }

  private boolean validateParameter(CreateValueSetContentRequestType request, CreateValueSetContentResponseType response)
  {
    boolean isValid = true;
    String errorMessage = "Unbekannter Fehler";
    ValueSet vs = request.getValueSet();

////////  CodeSystemEntity überprüfen //////////////////////////////////////////
    if (request.getCodeSystemEntity() == null)
    {
      errorMessage = "CodeSystemEntity darf nicht NULL sein!";
      isValid = false;
    }
    else
    {
      if (request.getCodeSystemEntity().isEmpty())
      {
        errorMessage = "CodeSystemEntity muss mindestens ein Element enthalten!";
        isValid = false;
      }
      else
      {
        // Überprüfe ob alle CodeSystemEntities eine CSEVersion mit gültiger Id haben
        Iterator<CodeSystemEntity> iterCse = request.getCodeSystemEntity().iterator();
        CodeSystemEntity cse;
        CodeSystemEntityVersion csev;
        while (iterCse.hasNext())
        {
          cse = iterCse.next();
          if (cse.getCodeSystemEntityVersions().toArray() == null || cse.getCodeSystemEntityVersions().toArray().length == 0)
          {
            errorMessage = "Mindestens ein CodeSystemEntity-Objekt hat eine leere oder ungültige CSEVersion-Liste";
            isValid = false;
          }
          else
          {
            csev = ((CodeSystemEntityVersion) cse.getCodeSystemEntityVersions().toArray()[0]);
            if (csev == null)
            {
              errorMessage = "Mindestens ein CodeSystemEntity-Objekt hat eine ungültige CSEVersion";
              isValid = false;
            }
            else if (csev.getVersionId() == null || csev.getVersionId() < 1)
            {
              errorMessage = "Mindestens ein CodeSystemEntity-Objekt hat eine CSEVersion mit ungültiger oder fehlender versionId";
              isValid = false;
            }
          }
        }
      }
    }
///////// ValueSet überprüfen //////////////////////////////////////////////////
    if (vs == null)
    {
      errorMessage = "ValueSet darf nicht NULL sein!";
      isValid = false;
    }
    else
    {
      ///////// ValueSetVersion //////////////////////////////////////////    
      Set<ValueSetVersion> vsvSet = vs.getValueSetVersions();
      // Gibt es eine ValueSetVersion Liste?
      if (vsvSet == null)
      {
        errorMessage = "Die ValueSetVersion-Liste darf nicht NULL sein!";
        isValid = false;
      }
      else
      { // Wenn ja, hat sie mehr oder weniger als genau einen Eintrag?
        if (vsvSet.size() != 1)
        {
          errorMessage = "Die ValueSetVersion-Liste hat " + Integer.toString(vsvSet.size()) + " Einträge. Sie muss aber genau einen Eintrag haben!";
          isValid = false;
        }
        else
        { // Hat die VSV eine ID?
          ValueSetVersion vsv = (ValueSetVersion) vsvSet.toArray()[0];
          if (vsv.getVersionId() == null || vsv.getVersionId() < 1)
          {
            errorMessage = "Die versionId von ValueSetVersion darf nicht NULL oder kleiner 1 sein!";
            isValid = false;
          }
        }
      }
    }

    if (isValid == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage(errorMessage);
    }
    return isValid;
  }
}
