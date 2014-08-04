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
package de.fhdo.gui.main.modules;

import de.fhdo.Definitions;
import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.TreeModel;
import de.fhdo.models.TreeNode;
import de.fhdo.models.comparators.ComparatorCsMetadata;
import de.fhdo.models.comparators.ComparatorTranslations;
import de.fhdo.models.comparators.ComparatorVsMetadata;
import de.fhdo.models.itemrenderer.ListitemRendererCrossmapping;
import de.fhdo.models.itemrenderer.ListitemRendererCsMetadataList;
import de.fhdo.models.itemrenderer.ListitemRendererLinkedConcepts;
import de.fhdo.models.itemrenderer.ListitemRendererOntologies;
import de.fhdo.models.itemrenderer.ListitemRendererTranslations;
import de.fhdo.models.itemrenderer.ListitemRendererVsMetadataList;
import de.fhdo.terminologie.ws.authoring.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse.Return;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemMetadataValue;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSetMetadataValue;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class PopupConcept extends PopupWindow
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private CodeSystemEntityVersion csev;
  private CodeSystemConcept csc;
  private CodeSystemEntity cse;
  private CodeSystemVersionEntityMembership csvem;

  private long id, versionId;
  private int hierarchyMode, contentMode;
  private List metadata = new ArrayList();
  private List translations = new ArrayList();
  private Listbox listTranslations, listMetadata, listCrossmappings, listLinkedConcepts, listOntologies;
  private Button bCreate, bMetaParaChange, bTranslationNew;
  private Checkbox cbNewVersion, cbPreferred, cbAxis, cbMainClass, cbStructureEntry, cbIsLeaf;
  private Datebox dateBoxED, dateBoxID, dateBoxSD;
  private Textbox tbTerm, tbAbbrevation, tbDescription, tbCode, tbNamePL, tbOrderNr, tbBedeutung, tbAwbeschreibung, tbHinweise, tbHints, tbMeaning;
  private Label lReq, lName, lCode, lPref;
  private TreeNode tnSelected;
  private Grid gridT;
  private Tab tabDetails;
  private ConceptValueSetMembership cvsm;
  private Row rOrderNr, rStructureEntry, rBedeutung, rAwbeschreibung, rHinweise, rHints, rMeaning;
  private Combobox cbStatus;

  @Override
  public void doAfterComposeCustom()
  {
    contentMode = (Integer) arg.get("ContentMode");
    if (arg.get("TreeNode") != null)
      tnSelected = (TreeNode) arg.get("TreeNode");
    id = (Long) arg.get("Id");
    versionId = (Long) arg.get("VersionId");
  }

  private void loadAssociations()
  {
    if (tnSelected.getResponseListConceptAssociations() == null)
    {
      // Parameter erzeugen und im folgenden zusammenbauen
      ListConceptAssociationsRequestType parameter_ListCA = new ListConceptAssociationsRequestType();

      // CSE erstellen und CSEV einsetzen  
      CodeSystemEntity cseNew = new CodeSystemEntity();
      CodeSystemEntityVersion csevNew = new CodeSystemEntityVersion();
      cseNew.setId(csev.getCodeSystemEntity().getId());
      csevNew.setVersionId(csev.getVersionId());
      cseNew.getCodeSystemEntityVersions().add(csevNew);

            // Zusatzinformationen anfordern um anzuzeigen ob noch Kinder vorhanden sind oder nicht
      //        parameter_ListCA.setLookForward(true);    
      parameter_ListCA.setDirectionBoth(true);
      parameter_ListCA.setCodeSystemEntity(cseNew);

      // Anfrage an WS (ListConceptAssociations) stellen mit parameter_ListCA                       
      csevNew.setCodeSystemEntity(null);       // damit es kein infinity Deep Problem gibt

      // Falls es beim Ausführen des WS zum Fehler kommt
      ListConceptAssociationsResponse.Return response = null;
      try
      {
        response = WebServiceHelper.listConceptAssociations(parameter_ListCA);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      tnSelected.setResponseListConceptAssociations(response);
    }
    loadCrossmappings();
    loadLinkedConcepts();
    loadOntologies();
  }

  private void loadCSEVFromArguments()
  {
    csev = (CodeSystemEntityVersion) arg.get("CSEV");
    if (csev != null)
    {
      csc = csev.getCodeSystemConcepts().get(0);
      cse = csev.getCodeSystemEntity();
      loadDetails();
    }
  }

  private void loadCsMetadata(Return responseDetails, boolean editableMetadataList)
  {
    for (CodeSystemMetadataValue csmdv : responseDetails.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemMetadataValues())
    {
      csmdv.setCodeSystemEntityVersion(responseDetails.getCodeSystemEntity().getCodeSystemEntityVersions().get(0));
      metadata.add(csmdv);
    }

    if (metadata.isEmpty())
    {
      window.getFellow("tabMetadata").setVisible(false);
      return;
    }
    window.getFellow("tabMetadata").setVisible(true);

    Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
            lh2 = new Listheader(Labels.getLabel("common.value"));
    lh1.setSortAscending(new ComparatorCsMetadata(true));
    lh1.setSortDescending(new ComparatorCsMetadata(false));
    listMetadata.getListhead().getChildren().add(lh1);
    listMetadata.getListhead().getChildren().add(lh2);

    listMetadata.setItemRenderer(new ListitemRendererCsMetadataList(editableMetadataList));
    listMetadata.setModel(new SimpleListModel(metadata));
    lh1.sort(true);
  }

  private void loadVsMetadata(ReturnValueSetConceptMetadataResponse.Return responseDetails, boolean editableMetadataList)
  {
    for (ValueSetMetadataValue vsmdv : responseDetails.getValueSetMetadataValue())
    {
      metadata.add(vsmdv);
    }

    if (metadata.isEmpty())
    {
      window.getFellow("tabMetadata").setVisible(false);
      return;
    }
    window.getFellow("tabMetadata").setVisible(true);

    Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
            lh2 = new Listheader(Labels.getLabel("common.value"));
    lh1.setSortAscending(new ComparatorVsMetadata(true));
    lh1.setSortDescending(new ComparatorVsMetadata(false));
    listMetadata.getListhead().getChildren().add(lh1);
    listMetadata.getListhead().getChildren().add(lh2);

    listMetadata.setItemRenderer(new ListitemRendererVsMetadataList(editableMetadataList));
    listMetadata.setModel(new SimpleListModel(metadata));
    lh1.sort(true);
  }

  private void loadTranslations(Return responseDetails, boolean editableTranslationsList)
  {

    for (CodeSystemConceptTranslation csct : responseDetails.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations())
    {
      translations.add(csct);
    }

    if (translations.isEmpty())
    {
      window.getFellow("tabTranslations").setVisible(false);
      return;
    }
    window.getFellow("tabTranslations").setVisible(true);

    Listheader lh1 = new Listheader(Labels.getLabel("common.language")),
            lh2 = new Listheader(Labels.getLabel("common.value"));
    lh1.setSortAscending(new ComparatorTranslations(true));
    lh1.setSortDescending(new ComparatorTranslations(false));
    listTranslations.getListhead().getChildren().add(lh1);
    listTranslations.getListhead().getChildren().add(lh2);

    listTranslations.setItemRenderer(new ListitemRendererTranslations(editableTranslationsList));
    listTranslations.setModel(new SimpleListModel(translations));
    lh1.sort(true);
  }

  private void loadCrossmappings()
  {
    ListModelList crossmappings = new ListModelList();

    for (CodeSystemEntityVersionAssociation cseva : tnSelected.getResponseListConceptAssociations().getCodeSystemEntityVersionAssociation())
    {
      if (cseva.getAssociationKind().compareTo(3) == 0 && crossmappings.contains(cseva) == false)
      {
        crossmappings.add(cseva);
      }
    }

    if (crossmappings.isEmpty())
    {
      window.getFellow("tabCrossmapping").setVisible(false);
      return;
    }
    window.getFellow("tabCrossmapping").setVisible(true);

    Listheader lh1 = new Listheader(Labels.getLabel("common.concept")),
            lh2 = new Listheader(Labels.getLabel("common.codeSystem"));
    listCrossmappings.getListhead().getChildren().add(lh1);
    listCrossmappings.getListhead().getChildren().add(lh2);
    listCrossmappings.setModel(crossmappings);

    //renderer
    listCrossmappings.setItemRenderer(new ListitemRendererCrossmapping(csev.getVersionId()));
  }

  private void loadLinkedConcepts()
  {
    ListModelList linkedConcepts = new ListModelList();

    for (CodeSystemEntityVersionAssociation cseva : tnSelected.getResponseListConceptAssociations().getCodeSystemEntityVersionAssociation())
    {
      if (cseva.getAssociationKind().compareTo(4) == 0 && linkedConcepts.contains(cseva) == false)
      {
        linkedConcepts.add(cseva);
      }
    }

    if (linkedConcepts.isEmpty())
    {
      window.getFellow("tabLinkedConcepts").setVisible(false);
      return;
    }
    window.getFellow("tabLinkedConcepts").setVisible(true);
    Listheader lh1 = new Listheader(Labels.getLabel("common.association")),
            lh2 = new Listheader(Labels.getLabel("common.concept"));
    lh1.setWidth("30%");
    listLinkedConcepts.getListhead().setSizable(true);
    listLinkedConcepts.getListhead().getChildren().add(lh1);
    listLinkedConcepts.getListhead().getChildren().add(lh2);
    listLinkedConcepts.setModel(linkedConcepts);

    // Renderer
    listLinkedConcepts.setItemRenderer(new ListitemRendererLinkedConcepts(csev.getVersionId()));
  }

  private void loadOntologies()
  {
    ListModelList ontologies = new ListModelList();

    for (CodeSystemEntityVersionAssociation cseva : tnSelected.getResponseListConceptAssociations().getCodeSystemEntityVersionAssociation())
    {
      if (cseva.getAssociationKind().compareTo(1) == 0)
      {
        if (cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1() != null || cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2() != null)
        {
          if (ontologies.contains(cseva) == false)
            ontologies.add(cseva);
        }
      }
    }

    if (ontologies.isEmpty())
    {
      window.getFellow("tabOntologies").setVisible(false);
      return;
    }
    window.getFellow("tabOntologies").setVisible(true);
    Listheader lh1 = new Listheader(Labels.getLabel("common.association")),
            lh2 = new Listheader(Labels.getLabel("common.concept")),
            lh3 = new Listheader(Labels.getLabel("common.code")),
            lh4 = new Listheader(Labels.getLabel("common.isPreferredTerm"));
    lh1.setWidth("100px");
    lh3.setWidth("100px");
    listOntologies.getListhead().setSizable(true);
    listOntologies.getListhead().getChildren().add(lh1);
    listOntologies.getListhead().getChildren().add(lh2);
    listOntologies.getListhead().getChildren().add(lh3);
    listOntologies.getListhead().getChildren().add(lh4);
    listOntologies.setModel(ontologies);

    // Renderer
    listOntologies.setItemRenderer(new ListitemRendererOntologies(csev.getVersionId()));
  }

  private void loadDetails()
  {
    // Daten einlesen
    loadDatesIntoGUI();
    
    

    // Metadaten und Uebersetzungen laden
    ReturnConceptDetailsRequestType parameter = new ReturnConceptDetailsRequestType();
    parameter.setCodeSystemEntity(cse);
    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
    //CSE aus CSEV entfernen, sonst inf,loop
    csev.setCodeSystemEntity(null);

    if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    Return response = WebServiceHelper.returnConceptDetails(parameter);

    // keine csev zurueckgekommen (wegen moeglicher Fehler beim WS)
    if (response.getCodeSystemEntity() == null)
      return;

    // das Loeschen der cse aus der csev wieder rueckgaengig machen (war nur fuer die Anfrage an WS)
    csev.setCodeSystemEntity(cse);

    // CodeSystemVersionEntityMembership nachladen
    if (response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().isEmpty() == false)
    {
      csvem = response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0);
      cse.getCodeSystemVersionEntityMemberships().clear();
      cse.getCodeSystemVersionEntityMemberships().add(csvem);
    }

    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      ReturnValueSetConceptMetadataRequestType para = new ReturnValueSetConceptMetadataRequestType();
      para.setCodeSystemEntityVersionId(csev.getVersionId());
      para.setValuesetVersionId(versionId);
      listMetadata.setAttribute("valuesetVersionId", versionId);
      listMetadata.setAttribute("codeSystemEntityVersionId", csev.getVersionId());
      listMetadata.setAttribute("contentMode", contentMode);

      listTranslations.setAttribute("cse", cse);
      listTranslations.setAttribute("csev", csev);
      listTranslations.setAttribute("csevm", csvem);

      ReturnValueSetConceptMetadataResponse.Return resp = WebServiceHelper.returnValueSetConceptMetadata(para);
      loadVsMetadata(resp, false);

    }
    else
    {
      listMetadata.setAttribute("cse", cse);
      listMetadata.setAttribute("csev", csev);
      listMetadata.setAttribute("csevm", csvem);
      listMetadata.setAttribute("contentMode", contentMode);
      listMetadata.setAttribute("versionId", versionId);

      listTranslations.setAttribute("cse", cse);
      listTranslations.setAttribute("csev", csev);
      listTranslations.setAttribute("csevm", csvem);

      loadCsMetadata(response, false);
    }

    loadTranslations(response, false);
    loadAssociations();

    loadTbStatus();
  }

  private void loadTbStatus()
  {
    
    String status_cd = "";
    long domain_id;
    
    //csev.getStatusVisibility()
            
    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {

      for (ConceptValueSetMembership cvsmL : csev.getConceptValueSetMemberships())
      {
        if (cvsmL.getId().getValuesetVersionId() == versionId)
          cvsm = cvsmL;
      }

      status_cd = String.valueOf(cvsm.getStatus());
      domain_id = Definitions.STATUS;
      //tbStatus.setValue(String.valueOf(cvsm.getStatus()));
      cbStructureEntry.setChecked(cvsm.isIsStructureEntry());
      rStructureEntry.setVisible(true);
      tbOrderNr.setValue(String.valueOf(cvsm.getOrderNr()));
      rOrderNr.setVisible(true);

      tbBedeutung.setValue(cvsm.getMeaning());
      rBedeutung.setVisible(true);
      tbAwbeschreibung.setValue(cvsm.getDescription());
      rAwbeschreibung.setVisible(true);
      tbHinweise.setValue(cvsm.getHints());
      rHinweise.setVisible(true);

      rHints.setVisible(false);
      rMeaning.setVisible(false);

    }
    else
    {
      //tbStatus.setValue(String.valueOf(csev.getStatusVisibility()));
      status_cd = String.valueOf(csev.getStatusVisibility());
      domain_id = Definitions.STATUS_CONCEPT_VISIBILITY;
    }
    
    // Combobox füllen
    DomainHelper.getInstance().fillCombobox(cbStatus, domain_id, status_cd);
  }

  private CreateConceptAssociationResponse.Return createAssociationResponse(CodeSystemEntityVersion csev1, CodeSystemEntityVersion csev2, int assoKind, int assoType)
  {
    CreateConceptAssociationRequestType parameterAssociation = new CreateConceptAssociationRequestType();
    CreateConceptAssociationResponse.Return responseAccociation = null;
    CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();

    if (csev1 != null && csev2 != null)
    {
      cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
      cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
      cseva.setAssociationKind(assoKind); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
      cseva.setLeftId(csev1.getVersionId()); // immer linkes Element also csev1
      cseva.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
      cseva.getAssociationType().setCodeSystemEntityVersionId((long) assoType);

      // Login
      parameterAssociation.setLoginToken(SessionHelper.getSessionId());

      // Association
      parameterAssociation.setCodeSystemEntityVersionAssociation(cseva);

      // Call WS and prevent loops in SOAP Message        
      long cse1id = csev1.getCodeSystemEntity().getId();
      csev1.setCodeSystemEntity(null);
      csev2.setCodeSystemEntity(null);
      responseAccociation = WebServiceHelper.createConceptAssociation(parameterAssociation);
      csev1.setCodeSystemEntity(new CodeSystemEntity());
      csev1.getCodeSystemEntity().setId(cse1id);
      csev2.setCodeSystemEntity(cse);
    }

    return responseAccociation;
  }

  @Override
  protected void initializeDatabinder()
  {
    binder = new AnnotateDataBinder(window);
    binder.bindBean("cse", cse);
    binder.bindBean("csev", csev);
    binder.bindBean("csc", csc);
    binder.bindBean("csvem", csvem);
    binder.bindBean("metadata", metadata);
    binder.bindBean("translations", translations);
    binder.bindBean("versioning", versioning);
    binder.loadAll();
  }

  @Override
  protected void loadDatesIntoGUI()
  {
    if (csev != null)
    {
      if (csev.getEffectiveDate() != null)
        dateBoxED.setValue(new Date(csev.getEffectiveDate().toGregorianCalendar().getTimeInMillis()));
      if (csev.getInsertTimestamp() != null)
        dateBoxID.setValue(new Date(csev.getInsertTimestamp().toGregorianCalendar().getTimeInMillis()));
      if (csev.getStatusVisibilityDate() != null)
        dateBoxSD.setValue(new Date(csev.getStatusVisibilityDate().toGregorianCalendar().getTimeInMillis()));
    }
    else
    {
      dateBoxED.setValue(null);
      dateBoxID.setValue(null);
      dateBoxSD.setValue(null);
    }
  }

  @Override
  protected void editmodeDetails()
  {
    loadCSEVFromArguments();
    window.setTitle(Labels.getLabel("popupConcept.showConcept"));
    cbAxis.setDisabled(true);
    cbMainClass.setDisabled(true);
    cbIsLeaf.setDisabled(true);
    cbNewVersion.setVisible(false);
    cbPreferred.setDisabled(true);
    dateBoxED.setDisabled(true);
    dateBoxID.setDisabled(true);
    dateBoxSD.setDisabled(true);
    tbTerm.setReadonly(true);
    tbAbbrevation.setReadonly(true);
    tbDescription.setReadonly(true);
    tbCode.setReadonly(true);
    cbStatus.setDisabled(true);
    lReq.setVisible(false);
    lName.setValue(Labels.getLabel("common.designation"));
    lCode.setValue(Labels.getLabel("common.code"));
    lPref.setValue(Labels.getLabel("common.preferred"));
    bCreate.setVisible(false);
    listMetadata.setDisabled(true);
    listTranslations.setDisabled(true);
    gridT.setVisible(true);
    bMetaParaChange.setDisabled(true);
    bTranslationNew.setDisabled(true);
    tbHints.setReadonly(true);
    tbMeaning.setReadonly(true);
    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      cbStructureEntry.setDisabled(true);
      tbOrderNr.setReadonly(true);
      tbBedeutung.setReadonly(true);
      tbAwbeschreibung.setReadonly(true);
      tbHinweise.setReadonly(true);
    }
  }

  @Override
  protected void editmodeCreate()
  { //Erstellen
    window.setTitle(Labels.getLabel("popupConcept.newConcept"));
    csev = new CodeSystemEntityVersion();
    csc = new CodeSystemConcept();
    cse = new CodeSystemEntity();
    csvem = new CodeSystemVersionEntityMembership();
    versioning = new VersioningType();
    csc.setIsPreferred(Boolean.TRUE);
    csev.getCodeSystemConcepts().add(csc);
    csev.setStatusVisibility(1); // TODO: 1 durch Konstante ersetzen
    csev.setIsLeaf(Boolean.TRUE);
    csvem.setIsAxis(Boolean.FALSE);
    versioning.setCreateNewVersion(Boolean.TRUE);

    hierarchyMode = (Integer) arg.get("Association");
    if (hierarchyMode == 3)
    {
      //window.setTitle(Labels.getLabel("popupConcept.createRootConcept"));
      csvem.setIsMainClass(Boolean.TRUE);
    }
    else
    {
      window.setTitle(Labels.getLabel("popupConcept.createSubconcept"));
      csvem.setIsMainClass(Boolean.FALSE);
    }

    cbAxis.setDisabled(false);
    cbMainClass.setDisabled(true);
    cbIsLeaf.setDisabled(true);
    cbNewVersion.setVisible(true);
    cbNewVersion.setDisabled(true);
    cbPreferred.setDisabled(false);
    dateBoxED.setReadonly(false);
    dateBoxID.setReadonly(true);
    dateBoxSD.setReadonly(true);
    tbTerm.setReadonly(false);
    tbTerm.setFocus(true);
    tbAbbrevation.setReadonly(false);
    tbDescription.setReadonly(false);
    tbCode.setReadonly(false);
    cbStatus.setDisabled(false);
    lReq.setVisible(true);
    lName.setValue(Labels.getLabel("common.designation") + "*");
    lCode.setValue(Labels.getLabel("common.code") + "*");
    lPref.setValue(Labels.getLabel("common.termPreferred") + "*");
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.create"));
    listMetadata.setDisabled(true);
    listTranslations.setDisabled(true);
    gridT.setVisible(true);
    bMetaParaChange.setDisabled(true);
    bTranslationNew.setDisabled(true);
  }

  @Override
  protected void editmodeMaintainVersionNew()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void editmodeMaintain()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void editmodeMaintainVersionEdit()
  {
    loadCSEVFromArguments();
    window.setTitle(Labels.getLabel("popupConcept.editConcept"));
    versioning = new VersioningType();
    versioning.setCreateNewVersion(Boolean.FALSE); // TODO: Probleme mit Assoziationen bei neuen Versionen; Vorerst keine neuen Versionen erstellbar                

    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {

      cbAxis.setDisabled(true);
      cbMainClass.setDisabled(true);
      cbIsLeaf.setDisabled(true);
      cbNewVersion.setVisible(false);
      cbNewVersion.setDisabled(true); // TODO: Probleme mit Assoziationen bei neuen Versionen; Vorerst keine neuen Versionen erstellbar
      cbPreferred.setDisabled(true);
      dateBoxED.setReadonly(true);
      dateBoxID.setReadonly(true);
      dateBoxSD.setReadonly(true);
      tbTerm.setReadonly(true);
      tbAbbrevation.setReadonly(true);
      tbDescription.setReadonly(true);
      tbCode.setReadonly(true);
      cbStatus.setDisabled(true);
      lReq.setVisible(false);
      lName.setValue(Labels.getLabel("common.designation"));
      lCode.setValue(Labels.getLabel("common.code"));
      lPref.setValue(Labels.getLabel("common.termPreferred"));
      bCreate.setVisible(true);
      bCreate.setLabel(Labels.getLabel("common.change"));
      listMetadata.setDisabled(false);
      cbStructureEntry.setDisabled(false);
      tbOrderNr.setReadonly(false);
      tbBedeutung.setReadonly(false);
      tbAwbeschreibung.setReadonly(false);
      tbHinweise.setReadonly(false);
      tbHints.setReadonly(true);
      tbMeaning.setReadonly(true);

      Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
              lh2 = new Listheader(Labels.getLabel("common.value"));
      lh1.setSortAscending(new ComparatorVsMetadata(true));
      lh1.setSortDescending(new ComparatorVsMetadata(false));

      listMetadata.setItemRenderer(new ListitemRendererVsMetadataList(true));
      listMetadata.setModel(new SimpleListModel(metadata));
      lh1.sort(true);

      gridT.setVisible(true);
      bMetaParaChange.setDisabled(false);

      listTranslations.setDisabled(false);
      Listheader lh1t = new Listheader(Labels.getLabel("common.language")),
              lh2t = new Listheader(Labels.getLabel("common.value"));
      lh1t.setSortAscending(new ComparatorTranslations(true));
      lh1t.setSortDescending(new ComparatorTranslations(false));

      listTranslations.setItemRenderer(new ListitemRendererTranslations(true));
      listTranslations.setModel(new SimpleListModel(translations));
      lh1t.sort(true);

      bTranslationNew.setDisabled(false);
    }
    else
    {

      cbAxis.setDisabled(false);
      cbMainClass.setDisabled(false);
      cbIsLeaf.setDisabled(false);
      cbNewVersion.setVisible(true);
      cbNewVersion.setDisabled(true); // TODO: Probleme mit Assoziationen bei neuen Versionen; Vorerst keine neuen Versionen erstellbar
      cbPreferred.setDisabled(false);
      dateBoxED.setReadonly(false);
      dateBoxID.setReadonly(true);
      dateBoxSD.setReadonly(true);
      tbTerm.setReadonly(false);
      tbAbbrevation.setReadonly(false);
      tbDescription.setReadonly(false);
      tbHints.setReadonly(false);
      tbMeaning.setReadonly(false);
      tbCode.setReadonly(false);
      cbStatus.setDisabled(true);
      lReq.setVisible(false);
      lName.setValue(Labels.getLabel("common.designation"));
      lCode.setValue(Labels.getLabel("common.code"));
      lPref.setValue(Labels.getLabel("common.termPreferred"));
      bCreate.setVisible(true);
      bCreate.setLabel(Labels.getLabel("common.change"));
      listMetadata.setDisabled(false);

      Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
              lh2 = new Listheader(Labels.getLabel("common.value"));
      lh1.setSortAscending(new ComparatorVsMetadata(true));
      lh1.setSortDescending(new ComparatorVsMetadata(false));

      listMetadata.setItemRenderer(new ListitemRendererCsMetadataList(true));
      listMetadata.setModel(new SimpleListModel(metadata));
      lh1.sort(true);

      gridT.setVisible(true);
      bMetaParaChange.setDisabled(false);

      listTranslations.setDisabled(false);
      Listheader lh1t = new Listheader(Labels.getLabel("common.language")),
              lh2t = new Listheader(Labels.getLabel("common.value"));
      lh1t.setSortAscending(new ComparatorTranslations(true));
      lh1t.setSortDescending(new ComparatorTranslations(false));

      listTranslations.setItemRenderer(new ListitemRendererTranslations(true));
      listTranslations.setModel(new SimpleListModel(translations));
      lh1t.sort(true);

      bTranslationNew.setDisabled(false);
    }
  }

  @Override
  protected void editmodeUpdateStatus()
  {
    loadCSEVFromArguments();

    window.setTitle(Labels.getLabel("popupConcept.editConceptStatus"));
    cbAxis.setDisabled(true);
    cbMainClass.setDisabled(true);
    cbIsLeaf.setDisabled(true);
    cbNewVersion.setVisible(false);
    cbPreferred.setDisabled(true);
    dateBoxED.setReadonly(true);
    dateBoxID.setReadonly(true);
    dateBoxSD.setReadonly(true);
    tbTerm.setReadonly(true);
    tbAbbrevation.setReadonly(true);
    tbDescription.setReadonly(true);
    tbHints.setReadonly(true);
    tbMeaning.setReadonly(true);
    tbCode.setReadonly(true);
    cbStatus.setDisabled(false);
    lReq.setVisible(false);
    lName.setValue(Labels.getLabel("common.designation"));
    lCode.setValue(Labels.getLabel("common.code"));
    lPref.setValue(Labels.getLabel("common.termPreferred"));
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.changeStatus"));
    gridT.setVisible(true);
    bMetaParaChange.setDisabled(true);
    bTranslationNew.setDisabled(true);
    listMetadata.setDisabled(true);
    listTranslations.setDisabled(true);

    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {

      cbStructureEntry.setDisabled(true);
      tbOrderNr.setReadonly(true);
      tbBedeutung.setReadonly(true);
      tbAwbeschreibung.setReadonly(true);
      tbHinweise.setReadonly(true);
    }
  }

  @Override
  protected void editmodeUpdateStatusVersion()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void create()
  {
        // Create Concept //////////////////////////////////////////////////////        
//        Authoring                   port_authoring    = new Authoring_Service().getAuthoringPort();
    logger.debug("create()");
    
    // CodeSystemVersionEntityMembership
    cse.getCodeSystemVersionEntityMemberships().clear();
    cse.getCodeSystemVersionEntityMemberships().add(csvem);

    // CodeSystemEntity(Version)
    cse.getCodeSystemEntityVersions().clear();
    cse.getCodeSystemEntityVersions().add(csev);
    
    CodeSystemConcept csc = csev.getCodeSystemConcepts().get(0);
    logger.debug("Term: " + csc.getTerm());
    logger.debug("Code: " + csc.getCode());
    

    // Daten setzen mit Convertierung von Date -> XMLGregorianCalendar
    try
    {
      if (dateBoxED != null && dateBoxED.getValue() != null)
      {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(dateBoxED.getValue().getTime());
        csev.setEffectiveDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
      }
    }
    catch (DatatypeConfigurationException ex)
    {
      Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
    }

    // CodeSystem + CodeSystemVersion
    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      CreateConceptRequestType parameterCSC = new CreateConceptRequestType();
      CreateConceptResponse.Return responseCreateConcept = null;

      // Login                         
      parameterCSC.setLoginToken(SessionHelper.getSessionId());

      // CodeSystem
      CodeSystem cs = new CodeSystem();
      CodeSystemVersion csv = new CodeSystemVersion();
      cs.setId(id);
      csv.setVersionId(versionId);
      cs.getCodeSystemVersions().add(csv);
      parameterCSC.setCodeSystem(cs);

      // CodeSystemEntity + CSEV
      parameterCSC.setCodeSystemEntity(cse);

      parameterCSC.setCodeSystem(cs);

      // WS anfrage
      csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
      responseCreateConcept = WebServiceHelper.createConcept(parameterCSC);
      csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS) 

      // Meldung falls CreateConcept fehlgeschlagen
      if (responseCreateConcept.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
        try
        {
          Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + responseCreateConcept.getReturnInfos().getMessage());
          return;
        }
        catch (Exception ex)
        {
          Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
        }

      // die neue cse(v) hat noch keine id. Für Assoziationen aber nötig => aus response auslesen
      csev.setVersionId(responseCreateConcept.getCodeSystemEntity().getCurrentVersionId());
      cse.setId(responseCreateConcept.getCodeSystemEntity().getId());
      cse.setCurrentVersionId(csev.getVersionId());
    }
    else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    { // ValueSets; Es werden nur Verknüpfungen zu CSE(V)s erstellt und keine neuen Konzepte angelegt
      // siehe ganz unten
    }

    // TreeNode erstellen und danach update, damit das neue Konzept auch angezeigt wird                
    TreeNode newTreeNode = new TreeNode(csev);

    // In Root einhängen
    if (hierarchyMode == 3)
    { // Root
      Tree t = (Tree) windowParent.getFellow("treeConcepts");
      ((TreeNode) ((TreeModel) t.getModel()).get_root()).getChildren().add(newTreeNode);
    }
    // Create Association für sub-konzepte und TreeNode einhängen
    else if (hierarchyMode == 2)
    { // sub-ebene
      // Assoziation erstellen; geht erst nachdem die neue CSE(V) erstell wurde und eine Id bekommen hat
      CodeSystemEntityVersion csevAssociated = null;
      csevAssociated = (CodeSystemEntityVersion) arg.get("CSEVAssociated"); // für assoziationen   
      CreateConceptAssociationResponse.Return responseAssociation = null;
      responseAssociation = createAssociationResponse(csevAssociated, csev, 2, 4);

      try
      {
        if (responseAssociation != null && responseAssociation.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.conceptassociation.Status.OK)
          Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.associationNotCreated") + "\n\n" + responseAssociation.getReturnInfos().getMessage());
        else
        {
          if (responseAssociation.getReturnInfos().getOverallErrorCategory() == de.fhdo.terminologie.ws.conceptassociation.OverallErrorCategory.INFO)
          {
            tnSelected.getChildren().add(newTreeNode);
          }
          else
          {
            Messagebox.show(Labels.getLabel("popupConcept.associationNotCreated") + "\n\n" + responseAssociation.getReturnInfos().getMessage());
          }
        }
      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    ((ContentConcepts) windowParent).updateModel(true);
    window.detach();
  }

  @Override
  protected void maintainVersionNew()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void maintain()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void maintainVersionEdit()
  {

    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      MaintainConceptRequestType parameter = new MaintainConceptRequestType();
      parameter.setCodeSystemVersionId(versionId);
      // Daten setzen mit Convertierung von Date -> XMLGregorianCalendar
      try
      {
        if (dateBoxED != null && dateBoxED.getValue() != null)
        {
          GregorianCalendar c = new GregorianCalendar();
          c.setTimeInMillis(dateBoxED.getValue().getTime());
          csev.setEffectiveDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        }
      }
      catch (DatatypeConfigurationException ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }

      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      // Versioning 
      VersioningType versioning = new VersioningType();
      versioning.setCreateNewVersion(cbNewVersion.isChecked());
      parameter.setVersioning(versioning);

      // CSE
      parameter.setCodeSystemEntity(cse);

      //CSEV    
      csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

      MaintainConceptResponseType response = WebServiceHelper.maintainConcept(parameter);

      csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)       

      // Meldung
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
          if (parameter.getVersioning().isCreateNewVersion())
            Messagebox.show(Labels.getLabel("popupConcept.newVersionSuccessfullyCreated"));
          else
            Messagebox.show(Labels.getLabel("popupConcept.editConceptSuccessfully"));
        else
          Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + response.getReturnInfos().getMessage());

        window.detach();
        ((ContentConcepts) windowParent).updateModel(true);
      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    else
    {
      // ValueSet
      MaintainConceptValueSetMembershipRequestType parameter = new MaintainConceptValueSetMembershipRequestType();

      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      CodeSystemEntityVersion codeSystemEntityVersion = new CodeSystemEntityVersion();
      codeSystemEntityVersion.getConceptValueSetMemberships().clear();
      ConceptValueSetMembership cvsmL = new ConceptValueSetMembership();

      cvsmL.setValueSetVersion(new ValueSetVersion());
      cvsmL.getValueSetVersion().setVersionId(cvsm.getId().getValuesetVersionId());
      cvsmL.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
      cvsmL.getCodeSystemEntityVersion().setVersionId(cvsm.getId().getCodeSystemEntityVersionId());
      cvsmL.setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));

      cvsmL.setStatus(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
      cvsmL.setIsStructureEntry(cbStructureEntry.isChecked());
      cvsmL.setOrderNr(Long.valueOf(tbOrderNr.getValue()));
      cvsmL.setMeaning(tbBedeutung.getValue());
      cvsmL.setDescription(tbAwbeschreibung.getValue());
      cvsmL.setHints(tbHinweise.getValue());

      codeSystemEntityVersion.getConceptValueSetMemberships().add(cvsmL);
      parameter.setCodeSystemEntityVersion(codeSystemEntityVersion);

      // Versioning 
      MaintainConceptValueSetMembershipResponse.Return response = WebServiceHelper.maintainConceptValueSetMembership(parameter);

      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
          Messagebox.show(Labels.getLabel("popupConcept.editConceptSuccessfully"));
        ((ContentConcepts) windowParent).updateModel(true);
        window.detach();
        cvsm.setStatus(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
        cvsm.setIsStructureEntry(cbStructureEntry.isChecked());
        cvsm.setOrderNr(Long.valueOf(tbOrderNr.getValue()));
        cvsm.setMeaning(tbBedeutung.getValue());
        cvsm.setDescription(tbAwbeschreibung.getValue());
        cvsm.setHints(tbHinweise.getValue());

      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  protected void updateStatus()
  {

    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {

      UpdateConceptStatusRequestType parameter = new UpdateConceptStatusRequestType();
      //parameter.setCodeSystemVersionId(versionId);
      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      // CSE
      parameter.setCodeSystemEntity(cse);
      csev.setStatusVisibility(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
      //csev.setStatusVisibilityDate(new Date());
      //CSEV    
      csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

      UpdateConceptStatusResponse.Return response = WebServiceHelper.updateConceptStatus(parameter);

      csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)       

      // Meldung
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
        {
          Messagebox.show(Labels.getLabel("popupConcept.editStatusSuccessfully"));
          ((ContentConcepts) windowParent).updateModel(true);
          window.detach();
        }
        else
          Messagebox.show(Labels.getLabel("common.error") + " \n" + Labels.getLabel("popupConcept.editStatusfailed") + "\n\n" + response.getReturnInfos().getMessage());
      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    else
    {

      UpdateConceptValueSetMembershipStatusRequestType parameter = new UpdateConceptValueSetMembershipStatusRequestType();
      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      CodeSystemEntityVersion codeSystemEntityVersion = new CodeSystemEntityVersion();
      codeSystemEntityVersion.getConceptValueSetMemberships().clear();
      ConceptValueSetMembership cvsmL = new ConceptValueSetMembership();

      cvsmL.setValueSetVersion(new ValueSetVersion());
      cvsmL.getValueSetVersion().setVersionId(cvsm.getId().getValuesetVersionId());
      cvsmL.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
      cvsmL.getCodeSystemEntityVersion().setVersionId(cvsm.getId().getCodeSystemEntityVersionId());
      cvsmL.setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
      cvsmL.setStatus(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
      codeSystemEntityVersion.getConceptValueSetMemberships().add(cvsmL);
      parameter.setCodeSystemEntityVersion(codeSystemEntityVersion);

      UpdateConceptValueSetMembershipStatusResponse.Return response = WebServiceHelper.updateConceptValueSetMembershipStatus(parameter);

      // Meldung
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
        {
          Messagebox.show(Labels.getLabel("popupConcept.editStatusSuccessfully"));
          ((ContentConcepts) windowParent).updateModel(true);
          window.detach();
          cvsm.setStatus(Integer.valueOf(DomainHelper.getInstance().getComboboxCd(cbStatus)));
        }
        else
          Messagebox.show(Labels.getLabel("common.error") + " \n" + Labels.getLabel("popupConcept.editStatusfailed") + "\n\n" + response.getReturnInfos().getMessage());
      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  protected void updateStatusVersion()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void onCheck$cbNewVersion(Event event)
  {
    if (cbNewVersion.isChecked())
      bCreate.setLabel(Labels.getLabel("common.create"));
    else
      bCreate.setLabel(Labels.getLabel("common.change"));
  }

  public void onClick$bCreate(Event event)
  {
    buttonAction();
  }

  public void onClick$tabDetails()
  {

    bCreate.setDisabled(false);
  }

  public void onClick$tabMetadata()
  {

    bCreate.setDisabled(true);
  }

  public void onClick$tabTranslations()
  {

    bCreate.setDisabled(true);
  }

  public void onClick$tabCrossmapping()
  {

    bCreate.setDisabled(true);
  }

  public void onClick$tabLinkedConcepts()
  {

    bCreate.setDisabled(true);
  }

  public void onClick$tabOntologies()
  {

    bCreate.setDisabled(true);
  }
}
