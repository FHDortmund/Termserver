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
package de.fhdo.collaboration.workflow;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.PO_CHANGE_TYPE;
import de.fhdo.collaboration.db.PO_CLASSNAME;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.collaboration.db.classes.Statusrel;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import de.fhdo.terminologie.ws.authoring.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptResponse;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetResponse;
import de.fhdo.terminologie.ws.authoring.Status;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.search.SearchType;
import de.fhdo.terminologie.ws.search.SortByField;
import de.fhdo.terminologie.ws.search.SortDirection;
import de.fhdo.terminologie.ws.search.SortingType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class ProposalWorkflow
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  // Singleton-Muster
  private static ProposalWorkflow instance;

  public static ProposalWorkflow getInstance()
  {
    if (instance == null)
      instance = new ProposalWorkflow();

    return instance;
  }

  // Konstruktor
  public ProposalWorkflow()
  {
  }

  /**
   * Fügt einen neuen Vorschlag hinzu und benachrichtigt alle verantwortlichen
   * Personen.
   *
   * @param proposal Vorschlag
   * @param obj Einzufügendes Objekt (z.B. CodeSystemConcept)
   * @param obj2 Einzufügendes Objekt 2 (z.B. CodeSystemConcept)
   * @return
   */
  public ReturnType addProposal(Proposal proposal, List<Object> proposalObjects, Boolean isExisting)
  {
    logger.debug("addProposal: " + proposal.getContentType());
    if (proposalObjects != null)
      logger.debug("Anzahl proposalObjects: " + proposalObjects.size());

    ReturnType returnInfos = new ReturnType();

    // TODO erst prüfen, ob Benutzer exisitert (Collaborationuser)
    List<Proposalobject> proposalObjectList = new java.util.LinkedList<Proposalobject>();

    boolean tsDataInserted = false;

    // 1. create objects in Terminology Server
    try
    {
      long insertedConceptId = 0;

      for (Object proposalObject : proposalObjects)
      {
        logger.debug("check object with type: " + proposalObject.getClass().getCanonicalName());

        if (proposalObject instanceof CodeSystem)
        {
          logger.debug("PROPOSAL: add Codesystem...");

          // Codesystem with version
          CodeSystem codeSystem = (CodeSystem) proposalObject;

          CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
          request.setLoginToken(CollaborationSession.getInstance().getSessionID());

          codeSystem.getCodeSystemVersions().get(0).setStatus(0); // not published yet

          // Codesystem angeben
          request.setCodeSystem(codeSystem);

          // Webservice aufrufen
          CreateCodeSystemResponse.Return ret = createCodeSystem(request);
          logger.debug("WS-Response: " + ret.getReturnInfos().getMessage());

          if (ret.getReturnInfos().getStatus() == Status.OK)
          {
            tsDataInserted = true;   // Erfolg

//            AssignTermHelper.assignTermToUser(ret.getCodeSystem());
            //proposal.setObjectId(ret.getCodeSystem().getCurrentVersionId());
            //proposal.setObjectVersionId(ret.getCodeSystem().getId());
            // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
            Proposalobject po = new Proposalobject();
            po.setClassId(ret.getCodeSystem().getId());
            po.setClassname("CodeSystem");
            po.setName(codeSystem.getName());
            po.setChangeType(PO_CHANGE_TYPE.NEW.id());
            proposalObjectList.add(po);

            po = new Proposalobject();
            po.setClassId(ret.getCodeSystem().getCurrentVersionId());
            po.setClassname("CodeSystemVersion");
            po.setName(codeSystem.getCodeSystemVersions().get(0).getName());
            po.setChangeType(PO_CHANGE_TYPE.NEW.id());
            proposalObjectList.add(po);

            proposal.setObjectId(ret.getCodeSystem().getId());
            proposal.setObjectVersionId(ret.getCodeSystem().getCurrentVersionId());
            proposal.setObjectName(codeSystem.getName());
            proposal.setObjectVersionName(codeSystem.getCodeSystemVersions().get(0).getName());
          }
          else
          {
            logger.debug("Kein Erfolg beim Einfügen eines CodeSystems: " + ret.getReturnInfos().getMessage());
            //Messagebox.show(ret.getReturnInfos().getMessage());

            returnInfos.setSuccess(false);
            returnInfos.setMessage("Fehler beim Einfügen eines Vorschlags: " + ret.getReturnInfos().getMessage());
            return returnInfos;
          }

        }
        else if (proposalObject instanceof CodeSystemEntity)
        {
          CodeSystemEntity cse = (CodeSystemEntity) proposalObject;
          CodeSystemEntityVersion csev = cse.getCodeSystemEntityVersions().get(0);
          CodeSystemConcept csc = null;
          if(csev.getCodeSystemConcepts().size() > 0)
            csc = csev.getCodeSystemConcepts().get(0);

          // Concept
          if (isExisting)
          {
            logger.debug("PROPOSAL: edit Concept...");

            // TODO
          }
          else
          {
            // new concept
            logger.debug("PROPOSAL: add Concept...");

            // create concept in Terminology Server
            CreateConceptRequestType request = new CreateConceptRequestType();
            request.setLoginToken(CollaborationSession.getInstance().getSessionID());

            // Codesystem angeben
            CodeSystemVersion csv = new CodeSystemVersion();
            csv.setVersionId(proposal.getObjectVersionId());
            request.setCodeSystem(new CodeSystem());
            request.getCodeSystem().setId(proposal.getObjectId());
            request.getCodeSystem().getCodeSystemVersions().add(csv);

            // set concept
            cse.getCodeSystemEntityVersions().get(0).setStatusVisibility(0); // not published yet
            cse.getCodeSystemEntityVersions().get(0).setIsLeaf(true);
            request.setCodeSystemEntity(cse);

            // Axis
            CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
            csvem.setCodeSystemVersion(csv);
            csvem.setIsAxis(false);
            csvem.setIsMainClass(true);
            request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(csvem);

            // call webservice
            CreateConceptResponse.Return ret = WebServiceHelper.createConcept(request);

            if (ret.getReturnInfos().getStatus() == Status.OK)
            {
              tsDataInserted = true;   // Erfolg

              // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
              Proposalobject po = new Proposalobject();
              po.setClassId(ret.getCodeSystemEntity().getCurrentVersionId());
              po.setClassname("CodeSystemConcept");
              po.setName(csc.getTerm() + " (" + csc.getCode() + ")");
              po.setChangeType(PO_CHANGE_TYPE.NEW.id());
              proposalObjectList.add(po);
            }

          }

          

        }

      }

      /*if (proposal.getContentType().equals("vocabulary"))
       {
       
       }
       else if (proposal.getContentType().equals("valueset"))
       {

       if (!isExisting)
       {
       logger.debug("ValueSet einfügen mit...");
       ValueSet vs = (ValueSet) obj;

       CreateValueSetRequestType request = new CreateValueSetRequestType();
       request.setLoginToken(CollaborationSession.getInstance().getSessionID());

       // Codesystem angeben
       request.setValueSet(vs);

       // Webservice aufrufen
       CreateValueSetResponse.Return ret = createValueSet(request);

       if (ret.getReturnInfos().getStatus() == Status.OK)
       {
       tsDataInserted = true;   // Erfolg
       AssignTermHelper.assignTermToUser(ret.getValueSet());

       proposal.setVocabularyId(ret.getValueSet().getCurrentVersionId());
       proposal.setVocabularyIdTwo(ret.getValueSet().getId());

       // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
       Proposalobject po = new Proposalobject();
       po.setClassId(ret.getValueSet().getId());
       po.setClassname("ValueSet");
       po.setName(vs.getName());
       po.setChangeType(PO_CHANGE_TYPE.NEW.id());
       proposalObjectList.add(po);

       po = new Proposalobject();
       po.setClassId(ret.getValueSet().getCurrentVersionId());
       po.setClassname("ValueSetVersion");
       po.setName(ret.getValueSet().getValueSetVersions().get(0).getName());
       po.setChangeType(PO_CHANGE_TYPE.NEW.id());
       proposalObjectList.add(po);
       }
       else
       {
       logger.debug("Kein Erfolg beim Einfügen eines ValueSets: " + ret.getReturnInfos().getMessage());
       Messagebox.show(ret.getReturnInfos().getMessage());
       }
       }
       else
       {

       logger.debug("ValueSet existiert. Proposal vorbereiten...");
       ValueSetVersion vsv = (ValueSetVersion) obj;

       tsDataInserted = true;   // Erfolg

       proposal.setVocabularyId(vsv.getValueSet().getCurrentVersionId());
       proposal.setVocabularyIdTwo(vsv.getValueSet().getId());

       // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
       Proposalobject po = new Proposalobject();
       po.setClassId(vsv.getValueSet().getId());
       po.setClassname("ValueSet");
       po.setName(vsv.getValueSet().getName());
       po.setChangeType(PO_CHANGE_TYPE.CHANGED.id());
       proposalObjectList.add(po);

       po = new Proposalobject();
       po.setClassId(vsv.getValueSet().getCurrentVersionId());
       po.setClassname("ValueSetVersion");
       po.setName(vsv.getName());
       po.setChangeType(PO_CHANGE_TYPE.CHANGED.id());
       proposalObjectList.add(po);
       }
       }
       else if (proposal.getContentType().equals("concept") || proposal.getContentType().equals("subconcept"))
       {
       if (!isExisting)
       {
       logger.debug("Konzept einfügen mit...");

       CodeSystemConcept csc = (CodeSystemConcept) obj;

       CreateConceptRequestType request = new CreateConceptRequestType();
       request.setLoginToken(CollaborationSession.getInstance().getSessionID());

       // Codesystem angeben
       CodeSystemVersion csv = new CodeSystemVersion();
       csv.setVersionId(proposal.getVocabularyId());
       request.setCodeSystem(new CodeSystem());
       request.getCodeSystem().setId(codeSystemVersionId);
       request.getCodeSystem().getCodeSystemVersions().add(csv);
       logger.debug("...Codesystem-Version-ID: " + proposal.getVocabularyId());

       // Konzept angeben
       CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
       csev.setMajorRevision(1);
       csev.setMinorRevision(0);
       csev.setStatusVisibility(0); // noch nicht publiziert
       csev.getCodeSystemConcepts().add(csc);
       csev.setIsLeaf(true);
       request.setCodeSystemEntity(new CodeSystemEntity());
       request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

       logger.debug("...Konzept-Code: " + csc.getCode());
       logger.debug("...Konzept-Term: " + csc.getTerm());

       // Axis
       if (csc.getCodeSystemEntityVersion() != null
       && csc.getCodeSystemEntityVersion().getCodeSystemEntity() != null
       && csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships() != null
       && csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().size() > 0)
       {
       logger.debug("verknüpfe Codesystem mit Begriff");
       request.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(csc.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0));
       }

       // Webservice aufrufen
       CreateConceptResponse.Return ret = WebServiceHelper.createConcept(request);

       if (ret.getReturnInfos().getStatus() == Status.OK)
       {
       tsDataInserted = true;   // Erfolg

       // Wird später in DB eingefügt
       Proposalobject po = new Proposalobject();
       po.setClassId(ret.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getVersionId());
       po.setClassname("CodeSystemConcept");
       po.setName(csc.getCode() + " (" + csc.getTerm() + ")");
       po.setChangeType(PO_CHANGE_TYPE.NEW.id()); // 1 = hinzugefügt
       proposalObjectList.add(po);

       insertedConceptId = po.getClassId();

       if (obj2 != null)
       {
       if (obj2 instanceof CodeSystemEntityVersionAssociation)
       {
       // Beziehung einfügen
       CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation) obj2;
       CodeSystemConcept cscParent = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().get(0);

       cseva.setStatus(0); // noch nicht publiziert

       // Übergeordnete Begriffs-ID
       cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
       cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(cscParent.getCodeSystemEntityVersionId());

       // Untergeordnete Begriffs-ID
       if (cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() == null)
       {
       // gerade eingefügte Konzept-ID übernehmen
       logger.debug("gerade eingefügte Konzept-ID übernehmen: " + insertedConceptId);
       cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
       cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(insertedConceptId);
       }

       logger.debug("Beziehung einfügen");

       CreateConceptAssociationRequestType request2 = new CreateConceptAssociationRequestType();
       request2.setLoginToken(CollaborationSession.getInstance().getSessionID());
       request2.setCodeSystemEntityVersionAssociation(cseva);

       // Webservice aufrufen
       CreateConceptAssociationResponse.Return ret2 = WebServiceHelper.createConceptAssociation(request2);

       if (ret2.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.OK)
       {
       tsDataInserted = true;   // Erfolg

       //CodeSystemConcept cscParent = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getCodeSystemConcepts().get(0);
       // Wird später in DB eingefügt
       po = new Proposalobject();
       po.setClassId(ret2.getCodeSystemEntityVersionAssociation().getId());
       po.setClassname("CodeSystemEntityVersionAssociation");
       po.setName(cscParent.getCode() + " (" + cscParent.getTerm() + ") -> " + csc.getCode());
       po.setChangeType(PO_CHANGE_TYPE.NEW.id()); // 1 = hinzugefügt
       proposalObjectList.add(po);
       }
       else
       {
       logger.debug("Kein Erfolg beim Einfügen eine Beziehung: " + ret2.getReturnInfos().getMessage());
       Messagebox.show(ret2.getReturnInfos().getMessage());
       }
       }
       }
       }
       else
       {
       logger.debug("Kein Erfolg beim Einfügen eines Konzeptes: " + ret.getReturnInfos().getMessage());
       Messagebox.show(ret.getReturnInfos().getMessage());
       }
       }
       else
       {

       logger.debug("Konzept existiert. Proposal vorbereiten...");
       CodeSystemEntityVersion csev = (CodeSystemEntityVersion) obj;

       tsDataInserted = true;   // Erfolg

       // Wird später in DB eingefügt
       Proposalobject po = new Proposalobject();
       po.setClassId(csev.getVersionId());
       po.setClassname("CodeSystemConcept");
       po.setName(csev.getCodeSystemConcepts().get(0).getCode() + " (" + csev.getCodeSystemConcepts().get(0).getTerm() + ")");
       po.setChangeType(PO_CHANGE_TYPE.CHANGED.id()); // 1 = hinzugefügt
       proposalObjectList.add(po);

       }
       }
       else if (proposal.getContentType().equals("conceptVs")) //ConceptValueSetMembership
       {
       if (!isExisting)
       {
       VocInfo vocInfo = null;
       if (obj instanceof VocInfo)
       vocInfo = (VocInfo) obj;

       if (obj2 != null && obj2 instanceof ValueSetVersion)
       {

       ValueSetVersion valueSetVersion = (ValueSetVersion) obj2;

       ListCodeSystemConceptsRequestType parameter = new ListCodeSystemConceptsRequestType();
       // CodeSystemEntity
       parameter.setCodeSystemEntity(new CodeSystemEntity());

       // CodeSystem(VersionsID) angeben
       CodeSystemVersion csv = new CodeSystemVersion();
       csv.setVersionId(vocInfo.getVersionId());
       parameter.setCodeSystem(new CodeSystem());
       parameter.getCodeSystem().setId(vocInfo.getCsId());
       parameter.getCodeSystem().getCodeSystemVersions().add(csv);

       logger.debug("Codesystem-ID: " + vocInfo.getCsId() + ", csv-id: " + vocInfo.getVersionId());

       // login
       if (SessionHelper.isCollaborationActive())
       {
       // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
       parameter.setLoginToken(CollaborationSession.getInstance().getSessionID());
       }
       else if (SessionHelper.isUserLoggedIn())
       {
       parameter.setLoginToken(SessionHelper.getSessionId());
       }

       SearchType searchType = new SearchType();
       searchType.setCaseSensitive(true);
       searchType.setStartsWith(false);
       searchType.setTraverseConceptsToRoot(false);
       searchType.setWholeWords(true);

       // SearchType: Parameter für die Suche nach Konzepten mit bestimmten "term"
       if (searchCode != null)
       {
       parameter.setSearchParameter(searchType);
       CodeSystemEntity cse = new CodeSystemEntity();
       CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
       CodeSystemConcept csc = new CodeSystemConcept();

       cse.getCodeSystemEntityVersions().add(csev);
       csev.getCodeSystemConcepts().add(csc);
       csc.setCode(searchCode);
       // TODO Muss noch als Parameter, der in der GUI mittels Checkbox/Radiogroup gesetzt werden kann, eingelesen werden
       csc.setIsPreferred(true);

       parameter.setCodeSystemEntity(cse);
       }

       // damit Linked Concepts gefunden werden (muss nach erstellung von SearchParameter erfolgen und false sein, falls traverse to root genutzt wird)    
       if (parameter.getSearchParameter() != null)
       parameter.setLookForward(!parameter.getSearchParameter().isTraverseConceptsToRoot());
       else
       parameter.setLookForward(true);

       // Sortierung
       parameter.setSortingParameter(createSortingParameter());

       ListCodeSystemConceptsResponse.Return response = WebServiceHelper.listCodeSystemConcepts(parameter);

       if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
       {
       CreateValueSetContentRequestType parameter2 = new CreateValueSetContentRequestType();

       // Login
       parameter2.setLoginToken(CollaborationSession.getInstance().getSessionID());

       // valueset
       ValueSet vs = new ValueSet();
       ValueSetVersion vsv = new ValueSetVersion();
       vs.setId(valueSetVersion.getValueSet().getId());
       vsv.setVersionId(valueSetVersion.getVersionId());
       vs.getValueSetVersions().add(vsv);
       parameter2.setValueSet(vs);

       // CSEV (Concept)
       CodeSystemEntity cse = new CodeSystemEntity();
       CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
       cse.setId(response.getCodeSystemEntity().get(0).getId());
       csev.setVersionId(response.getCodeSystemEntity().get(0).getCodeSystemEntityVersions().get(0).getVersionId());
       cse.getCodeSystemEntityVersions().add(csev);
       parameter2.getCodeSystemEntity().add(cse);

       // WS Aufruf
       CreateValueSetContentResponse.Return response2 = WebServiceHelper.createValueSetContent(parameter2);

       if (response2.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
       {
       if (response2.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.authoring.OverallErrorCategory.WARN)
       {
       Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response2.getReturnInfos().getMessage());
       }
       tsDataInserted = true;
       ReturnConceptValueSetMembershipRequestType param = new ReturnConceptValueSetMembershipRequestType();
       // Login
       param.setLoginToken(CollaborationSession.getInstance().getSessionID());
       param.setCodeSystemEntityVersion(csev);
       param.setValueSetVersion(vsv);

       ReturnConceptValueSetMembershipResponse.Return resp = WebServiceHelper.returnConceptValueSetMembership(param);

       if (resp.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.search.Status.OK)
       {

       UpdateConceptValueSetMembershipStatusRequestType request = new UpdateConceptValueSetMembershipStatusRequestType();
       //login vom oberen authoring
       request.setLoginToken(CollaborationSession.getInstance().getSessionID());

       CodeSystemEntityVersion codeSystemEntityVersion = new CodeSystemEntityVersion();
       codeSystemEntityVersion.getConceptValueSetMemberships().clear();
       resp.getConceptValueSetMembership().setStatus(0);
       resp.getConceptValueSetMembership().setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
       codeSystemEntityVersion.getConceptValueSetMemberships().add(resp.getConceptValueSetMembership());
       request.setCodeSystemEntityVersion(codeSystemEntityVersion);

       UpdateConceptValueSetMembershipStatusResponse.Return respo = WebServiceHelper.updateConceptValueSetMembershipStatus(request);

       if (respo.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
       {
       tsDataInserted = true;
       // Wird später in DB eingefügt
       Proposalobject po = new Proposalobject();
       po.setClassId(request.getCodeSystemEntityVersion().getConceptValueSetMemberships().get(0).getId().getCodeSystemEntityVersionId());
       po.setClassId2(request.getCodeSystemEntityVersion().getConceptValueSetMemberships().get(0).getId().getValuesetVersionId());
       po.setClassname("ConceptValueSetMembership");
       po.setName(response.getCodeSystemEntity().get(0).getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCode()
       + " ("
       + response.getCodeSystemEntity().get(0).getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getTerm()
       + ")");
       po.setChangeType(PO_CHANGE_TYPE.NEW.id()); // 1 = hinzugefügt
       proposalObjectList.add(po);

       logger.debug("CVSM Update erfolgreich!" + response.getReturnInfos().getMessage());
       }
       else
       {
       logger.debug("CVSM Update fehlgeschlagen!" + response.getReturnInfos().getMessage());
       Messagebox.show("CVSM Update fehlgeschlagen!", "Vorschlag erstellen", Messagebox.OK, Messagebox.INFORMATION);
       }

       }
       else
       {

       logger.debug("CVSM konnte nicht zu einem Update geholt werden!" + response.getReturnInfos().getMessage());
       Messagebox.show("CVSM konnte nicht zu einem Update geholt werden!", "Vorschlag erstellen", Messagebox.OK, Messagebox.INFORMATION);
       }
       }
       }
       else
       {

       logger.debug("Code wurde nicht gefunden!" + response.getReturnInfos().getMessage());
       Messagebox.show("Code wurde nicht gefunden!", "Vorschlag erstellen", Messagebox.OK, Messagebox.INFORMATION);
       }
       }
       else
       {

       logger.debug("Kein Erfolg bei der Concept-zu-ValueSet Zuweisung!");
       Messagebox.show("Kein Erfolg bei der Concept-zu-ValueSet Zuweisung!", "Vorschlag erstellen", Messagebox.OK, Messagebox.INFORMATION);
       }
       }
       else
       {
       logger.debug("ConceptValueSetMembership existiert. Proposal vorbereiten...");
       CodeSystemEntityVersion csev = (CodeSystemEntityVersion) obj;
       ValueSetVersion vsv = (ValueSetVersion) obj2;

       tsDataInserted = true;
       // Wird später in DB eingefügt
       Proposalobject po = new Proposalobject();
       po.setClassId(csev.getVersionId());
       po.setClassId2(vsv.getVersionId());
       po.setClassname("ConceptValueSetMembership");
       po.setName(csev.getCodeSystemConcepts().get(0).getCode()
       + " ("
       + csev.getCodeSystemConcepts().get(0).getTerm()
       + ")");
       po.setChangeType(PO_CHANGE_TYPE.CHANGED.id()); // 1 = hinzugefügt
       proposalObjectList.add(po);
       }
       }*/
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

    logger.debug("tsDataInserted: " + tsDataInserted);

    if (tsDataInserted)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        // 2. add proposal to db
        proposal.setStatus(1); // TODO
        proposal.setStatusDate(new Date());
        proposal.setCreated(new Date());
//        proposal.setLastChangeDate(proposal.getCreated());
        proposal.setCollaborationuser(new Collaborationuser());
        proposal.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());

        hb_session.save(proposal);
        logger.debug("new proposal saved with id: " + proposal.getId());

        // 3. create proposal objects in DB
        for (Proposalobject po : proposalObjectList)
        {
          po.setProposal(proposal);
          hb_session.save(po);
        }

        // Add creator Default privilege
//        Privilege priv = new Privilege();
//        priv.setCollaborationuser(new Collaborationuser());
//        priv.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());
//
//        Collaborationuser u = (Collaborationuser) hb_session.get(Collaborationuser.class, SessionHelper.getCollaborationUserID());
//
//        priv.setSe
//        if (u.isSendMail()!= null)
//        {
//          priv.setSendMail(u.getSendMail());
//        }
//        else
//        {
//          priv.setSendMail(false);
//        }
//        if (u.getRoles().iterator().next().getName().equals(CODES.ROLE_ADMIN)  // TODO
//            || u.getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER))
//        {
//          priv.setMayChangeStatus(true);
//          priv.setMayManageObjects(true);
//        }
//        else
//        {
//          priv.setMayChangeStatus(false);
//          priv.setMayManageObjects(false);
//        }
//        priv.setFromDate(new Date());
//        priv.setProposal(new Proposal());
//        priv.getProposal().setId(proposal.getId());
//        priv.setDiscussiongroup(null);
//
//        hb_session.save(priv);
        returnInfos.setSuccess(true);
        returnInfos.setMessage("Vorschlag erfolgreich eingefügt.");

//        if (u.i() != null && u.getSendMail())
//        {
//          // TODO send mail
//          
////          String[] adr = new String[1];
////          adr[0] = u.getEmail();
////          Mail.sendMailAUT(adr, M_AUT.PROPOSAL_SUBJECT, M_AUT.getInstance().getProposalText(
////              proposal.getVocabularyName(),
////              proposal.getContentType(),
////              proposal.getDescription()));
//        }
        /*Long id = 0l;
         String classname = "";
         if (proposal.getContentType().equals("vocabulary"))
         {

         id = proposal.getVocabularyIdTwo();
         classname = "CodeSystem";

         }
         else if (proposal.getContentType().equals("valueset"))
         {

         id = proposal.getVocabularyIdTwo();
         classname = "ValueSet";

         }
         else if (proposal.getContentType().equals("concept") || proposal.getContentType().equals("subconcept"))
         {

         id = proposal.getVocabularyIdTwo();
         classname = "CodeSystem";
         }
         else if (proposal.getContentType().equals("conceptVs"))
         {

         id = proposal.getVocabularyIdTwo();
         classname = "ValueSet";
         }

         String termHead = "from Collaborationuser cu join fetch cu.assignedTerms at where at.classId=:classId and at.classname=:classname";
         Query qTermHead = hb_session.createQuery(termHead);
         qTermHead.setParameter("classId", id);
         qTermHead.setParameter("classname", classname);
         List<Collaborationuser> userList = qTermHead.list();

         if (userList.size() == 1)
         {

         if (userList.get(0).getId().equals(u.getId()))
         {
         //SV == Antragsteller => Do nothing
         }
         else
         {

         //Erstelle privilegien für den SV
         Privilege privSv = new Privilege();
         privSv.setCollaborationuser(new Collaborationuser());
         privSv.getCollaborationuser().setId(userList.get(0).getId());

         if (userList.get(0).getSendMail() != null)
         {
         privSv.setSendMail(userList.get(0).getSendMail());
         }
         else
         {
         privSv.setSendMail(false);
         }
         privSv.setMayChangeStatus(true);
         privSv.setMayManageObjects(true);

         privSv.setFromDate(new Date());
         privSv.setProposal(new Proposal());
         privSv.getProposal().setId(proposal.getId());
         privSv.setDiscussiongroup(null);

         hb_session.save(privSv);

         if (userList.get(0).getSendMail() != null && userList.get(0).getSendMail())
         {

         String[] adr = new String[1];
         adr[0] = userList.get(0).getEmail();
         Mail.sendMailAUT(adr, M_AUT.PROPOSAL_SUBJECT, M_AUT.getInstance().getProposalSelbstVerwText(
         proposal.getVocabularyName(),
         proposal.getContentType(),
         proposal.getDescription()));
         }
         }
         }*/
        hb_session.getTransaction().commit();
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
        hb_session.getTransaction().rollback();

        returnInfos.setSuccess(false);
        returnInfos.setMessage("Fehler beim Einfügen eines Vorschlags: " + ex.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }
    }

    return returnInfos;
  }

  public ReturnType changeProposalStatus(Proposal proposal, long statusTo, String reason, Date discDateFrom, Date discDateTo)
  {
    ReturnType returnInfos = new ReturnType();

    if (logger.isDebugEnabled())
      logger.debug("changeProposalStatus from " + proposal.getStatus() + " to " + statusTo);

    long statusFrom = proposal.getStatus();

    // 1. prüfen, ob Statusänderung möglich ist
    Statusrel rel = ProposalStatus.getInstance().getStatusRel(statusFrom, statusTo);
    if (rel != null)
    {
      if (logger.isDebugEnabled())
        logger.debug("Statusänderung möglich");

      // 2. Rechte prüfen, ob angemeldeter Benutzer die Statusänderung durchführen darf
      if (ProposalStatus.getInstance().isUserAllowed(rel, SessionHelper.getCollaborationUserID()))
      {
        if (logger.isDebugEnabled())
          logger.debug("Rechte vorhanden!");

        // 3. Statusänderung durchführen
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();

        try
        {
          // Proposal ändern (Status + StatusDate)
          logger.debug("Ändere Status vom Vorschlag mit Proposal-ID: " + proposal.getId());
          Proposal proposal_db = (Proposal) hb_session.get(Proposal.class, proposal.getId());
          proposal_db.setStatus((int) statusTo);
          proposal_db.setStatusDate(new Date());

          proposal_db.setValidFrom(discDateFrom);
          proposal_db.setValidTo(discDateTo);

          if (discDateFrom != null)
            logger.debug("Datum von: " + discDateFrom);
          else
            logger.debug("Datum von: null");

          hb_session.update(proposal_db);

          // Statusänderung hinzufügen
          Proposalstatuschange psc = new Proposalstatuschange();
          psc.setProposal(proposal_db);
          //psc.getProposal().setId(proposal.getId());
          psc.setChangeTimestamp(new Date());
          psc.setCollaborationuser(new Collaborationuser());
          psc.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());
          psc.setProposalStatusFrom((int) statusFrom);
          psc.setProposalStatusTo((int) statusTo);
          psc.setReason(reason);
          hb_session.save(psc);

          hb_session.getTransaction().commit();

          // 4. Status in Terminologieserver ändern
          for (Proposalobject po : proposal_db.getProposalobjects())
          {
            changeTerminologyServerStatus(rel.getStatusByStatusIdTo(), po, returnInfos);
          }

          // TODO 5. Benutzer benachrichtigen
//          ArrayList<Collaborationuser> completeUserList = new ArrayList<Collaborationuser>();
//
//          //Lade alle Benutzer mit Privilegien auf Proposal
//          String hqlPrivilegeUsers = "from Collaborationuser cu join fetch cu.privileges pri join fetch pri.proposal pro join fetch cu.organisation o where pro.id=:id";
//          Query qPrivilegeUsers = hb_session.createQuery(hqlPrivilegeUsers);
//          qPrivilegeUsers.setParameter("id", proposal.getId());
//          List<Collaborationuser> privUserList = qPrivilegeUsers.list();
//
//          for (Collaborationuser cu : privUserList)
//          {
//            completeUserList.add(cu);
//          }
//
//          //Lade alle Diskussionsgruppen mit Privilegien auf Proposal
//          String hqlPrivilegeGroups = "from Collaborationuser cu join fetch cu.discussiongroups dg join fetch dg.privileges pri join fetch pri.proposal pro where pro.id=:id";
//          Query qPrivilegeGroups = hb_session.createQuery(hqlPrivilegeGroups);
//          qPrivilegeGroups.setParameter("id", proposal.getId());
//          List<Collaborationuser> privGroupList = qPrivilegeGroups.list();
//
//          for (Collaborationuser cu : privGroupList)
//          {
//
//            boolean doubleEntry = false;
//            for (Collaborationuser cuI : completeUserList)
//            {
//
//              if (cu.getId().equals(cuI.getId()))
//              {
//                doubleEntry = true;
//              }
//            }
//
//            if (!doubleEntry)
//            {
//              completeUserList.add(cu);
//            }
//          }
//
//          ArrayList<String> mailAdr = new ArrayList<String>();
//          for (Collaborationuser u : completeUserList)
//          {
//
//            if (u.isSendMail())
//              mailAdr.add(u.getEmail());
//          }
//          String[] adr = new String[mailAdr.size()];
//          for (int i = 0; i < adr.length; i++)
//          {
//
//            adr[i] = mailAdr.get(i);
//          }
          // TODO
//          Mail.sendMailAUT(adr, M_AUT.PROPOSAL_STATUS_SUBJECT, M_AUT.getInstance().getProposalStatusChangeText(
//              proposal.getVocabularyName(),
//              proposal.getContentType(),
//              proposal.getDescription(),
//              ProposalStatus.getInstance().getStatusStr(statusFrom),
//              ProposalStatus.getInstance().getStatusStr(statusTo),
//              reason));
        }
        catch (Exception ex)
        {
          LoggingOutput.outputException(ex, this);
          hb_session.getTransaction().rollback();

          returnInfos.setSuccess(false);
          returnInfos.setMessage("Fehler beim Ändern des Status: " + ex.getLocalizedMessage());
          return returnInfos;
        }
        finally
        {
          // Session schließen
          hb_session.close();
        }

        returnInfos.setSuccess(true);
        returnInfos.setMessage("Status erfolgreich geändert zu: " + rel.getStatusByStatusIdTo().getStatus() + "\n\nAndere Benutzer wurden über die Statusänderung per Email informiert.");
      }
      else
      {
        // Statusänderung nicht möglich, da keine Rechte
        if (logger.isDebugEnabled())
          logger.debug("keine Rechte vorhanden!");

        returnInfos.setMessage("Sie besitzen nicht die nötigen Rechte für diese Statusänderung!");
        return returnInfos;
      }

    }
    else
    {
      // Statusänderung nicht möglich, da nicht in DB vorgesehen
      returnInfos.setMessage("Die angegebene Statusänderung ist nicht möglich!");
      return returnInfos;
    }

    return returnInfos;
  }

  /**
   * Ändert den Status des Objektes im Terminologieserver
   *
   * @param statusTo
   * @param obj
   * @param returnInfos
   */
  private static void changeTerminologyServerStatus(de.fhdo.collaboration.db.classes.Status statusTo, Proposalobject po, ReturnType returnInfos)
  {
    logger.debug("changeTerminologyServerStatus(), classId: " + po.getClassId() + ", classname: " + po.getClassname());
    PO_CLASSNAME classname = PO_CLASSNAME.get(po.getClassname());
    PO_CHANGE_TYPE changeType = PO_CHANGE_TYPE.get(po.getChangeType());

    int newStatus = 0;
    if (statusTo.getIsPublic())
    {
      newStatus = 1; // Schwarz
    }
    else if (statusTo.getIsDeleted())
    {
      newStatus = 2; // Durchgestrichen
    }
    else
    {
      newStatus = 0; // Grau
    }

    logger.debug("Neuer Status: " + newStatus);
    if (changeType != PO_CHANGE_TYPE.CHANGED)
    {
      if (classname == PO_CLASSNAME.CODESYSTEM)
      {
        // nichts, da es keinen Status für CodeSystem gibt (nur Version)
      }
      else if (classname == PO_CLASSNAME.CODESYSTEM_VERSION)
      {
        // Status der Codesystem-Version ändern
        UpdateCodeSystemVersionStatusRequestType request = new UpdateCodeSystemVersionStatusRequestType();
        request.setLoginToken(CollaborationSession.getInstance().getSessionID());

        // Codesystem angeben
        request.setCodeSystem(new CodeSystem());
        CodeSystemVersion csv = new CodeSystemVersion();
        csv.setVersionId(po.getClassId());
        csv.setStatus(newStatus);
        request.getCodeSystem().getCodeSystemVersions().add(csv);

        // Webservice aufrufen
        UpdateCodeSystemVersionStatusResponse.Return ret = updateCodeSystemVersionStatus(request);

        logger.debug("Ergebnis updateCodeSystemVersionStatus: " + ret.getReturnInfos().getMessage());
        if (ret.getReturnInfos().getStatus() == Status.OK)
        {
        }
      }
      else if (classname == PO_CLASSNAME.CODESYSTEM_CONCEPT)
      {
        // Status des Konzepts ändern
        UpdateConceptStatusRequestType request = new UpdateConceptStatusRequestType();
        //TODO prüfen request.setCodeSystemVersionId(po.getProposal().getVocabularyId());
        request.setLoginToken(CollaborationSession.getInstance().getSessionID());

        // Codesystem angeben
        request.setCodeSystemEntity(new CodeSystemEntity());
        CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
        csev.setVersionId(po.getClassId());
        csev.setStatusVisibility(newStatus);
        request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

        // Webservice aufrufen
        UpdateConceptStatusResponse.Return ret = updateConceptStatus(request);

        logger.debug("Ergebnis updateConceptStatus: " + ret.getReturnInfos().getMessage());
        if (ret.getReturnInfos().getStatus() == Status.OK)
        {
        }
      }
      else if (classname == PO_CLASSNAME.RELATION)
      {
        // TODO
      }
      else if (classname == PO_CLASSNAME.VALUESET)
      {
        //Wäre gut wenn wir das analog zum CS halten und den status hier auch weglassen...

        /*
         // Status des Konzepts ändern
         UpdateValueSetStatusRequestType request = new UpdateValueSetStatusRequestType();

         request.setLogin(new LoginType());
         request.getLogin().setSessionID(CollaborationSession.getInstance().getSessionID());

         // Codesystem angeben
         ValueSet vs = new ValueSet();
         request.setValueSet(vs);
         vs.setId(po.getClassId());
         vs.setStatus(newStatus);

         // Webservice aufrufen
         UpdateValueSetStatusResponse.Return ret = updateValueSetStatus(request);

         logger.debug("Ergebnis updateValueSetStatus: " + ret.getReturnInfos().getMessage());
         if (ret.getReturnInfos().getStatus() == Status.OK)
         {
         }*/
      }
      else if (classname == PO_CLASSNAME.VALUESET_VERSION)
      {
        // Status der Codesystem-Version ändern
        UpdateValueSetStatusRequestType request = new UpdateValueSetStatusRequestType();
        request.setLoginToken(CollaborationSession.getInstance().getSessionID());

        // Codesystem angeben
        request.setValueSet(new ValueSet());
        ValueSetVersion vsv = new ValueSetVersion();
        vsv.setVersionId(po.getClassId());
        vsv.setStatus(newStatus);
        request.getValueSet().getValueSetVersions().add(vsv);

        // Webservice aufrufen
        UpdateValueSetStatusResponse.Return ret = updateValueSetStatus(request);

        logger.debug("Ergebnis updateValueSetVersionStatus: " + ret.getReturnInfos().getMessage());
        if (ret.getReturnInfos().getStatus() == Status.OK)
        {
        }
      }
      else if (classname == PO_CLASSNAME.CONCEPT_VALUESET_MEMBERSHIP)
      {
        // Status der Codesystem-Version ändern
        UpdateConceptValueSetMembershipStatusRequestType request = new UpdateConceptValueSetMembershipStatusRequestType();
        request.setLoginToken(CollaborationSession.getInstance().getSessionID());

        // Codesystem angeben
        request.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
        request.getCodeSystemEntityVersion().getConceptValueSetMemberships().clear();

        ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
        cvsm.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
        cvsm.setValueSetVersion(new ValueSetVersion());

        cvsm.getCodeSystemEntityVersion().setVersionId(po.getClassId());
        cvsm.getValueSetVersion().setVersionId(po.getClassId2());
        cvsm.setStatus(newStatus);
        request.getCodeSystemEntityVersion().getConceptValueSetMemberships().add(cvsm);

        // Webservice aufrufen
        UpdateConceptValueSetMembershipStatusResponse.Return ret = updateConceptValueSetMembershipStatus(request);

        logger.debug("Ergebnis updateValueSetVersionStatus: " + ret.getReturnInfos().getMessage());
        if (ret.getReturnInfos().getStatus() == Status.OK)
        {
        }
      }
    }
  }

  private static CreateCodeSystemResponse.Return createCodeSystem(de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }

  private static CreateValueSetResponse.Return createValueSet(de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createValueSet(parameter);
  }

  private static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.updateCodeSystemVersionStatus(parameter);
  }

  private static UpdateConceptStatusResponse.Return updateConceptStatus(de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.updateConceptStatus(parameter);
  }

  private static UpdateValueSetStatusResponse.Return updateValueSetStatus(de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.updateValueSetStatus(parameter);
  }

  private static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.updateConceptValueSetMembershipStatus(parameter);
  }

  private SortingType createSortingParameter()
  {
    SortingType st = null;
    Object o = SessionHelper.getValue("SortByField");
    if (o != null)
    {
      st = new SortingType();
      if (o.toString().equals("term"))
      {
        st.setSortBy(SortByField.TERM);
      }
      else
      {
        st.setSortBy(SortByField.CODE);
      }
    }
    o = SessionHelper.getValue("SortDirection");
    if (o != null)
    {
      if (st == null)
        st = new SortingType();

      if (o.toString().equals("descending"))
      {
        st.setSortDirection(SortDirection.DESCENDING);
      }
      else
      {
        st.setSortDirection(SortDirection.ASCENDING);
      }
    }
    return st;
  }
}
