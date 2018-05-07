/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2014 FH Dortmund: Peter Haas, Robert Muetzner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License."
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
package de.fhdo.collaboration.proposal;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.proposal.newproposal.INewProposal;
import de.fhdo.collaboration.proposal.newproposal.ProposalCodeSystem;
import de.fhdo.collaboration.proposal.newproposal.ProposalConcept;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class ProposalDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal updateInterface;
  private Proposal proposal;
  private long codeSystemId, codeSystemVersionId, valueSetId, valueSetVersionId;
  private Object source;
  //private CodeSystemEntityVersion parentTerm;
  //private CodeSystemEntityVersion csev;

  private boolean isExisting = false;
  private String type = "";

  private INewProposal proposalObject;

  public ProposalDetails()
  {
    logger.debug("ProposalDetails() - Konstruktor");

    proposal = new Proposal();

    loadArguments();
    initData();
  }

  public void afterCompose()
  {
    Tab selectedTab = null;

    if (source != null)
    {
      logger.debug("source: " + source.getClass().getCanonicalName());

      if (source instanceof CodeSystem || source instanceof CodeSystemVersion)
      {
        selectedTab = (Tab) getFellow("tabCodeSystem");
        proposalObject = new ProposalCodeSystem();
      }

      if (source instanceof CodeSystemEntity || source instanceof CodeSystemEntityVersion)
      {
        selectedTab = (Tab) getFellow("tabConcept");
        proposalObject = new ProposalConcept();
      }

//      if (source instanceof CodeSystemVersion && !isExisting && type.equals("")) //Neues Concept
//      {
//        selIndex = 1;
//        ((Row) getFellow("rChooseVoc")).setVisible(false);
//        ((Row) getFellow("rListBoxVoc")).setVisible(false);
//        ((Row) getFellow("rVsCode")).setVisible(false);
//
//        if (parentTerm != null)
//        {
//          // Neuer Unterbegriff 
//          CodeSystemConcept csc = parentTerm.getCodeSystemConcepts().get(0);
//          ((Label) getFellow("lParentConcept")).setValue(csc.getCode() + " - " + csc.getTerm());
//          ((Tab) getFellow("tabSubConcept")).setLabel("Unterbegriff");
//        }
//      }
//
//      if (source instanceof CodeSystemVersion && isExisting && type.equals("concept")) //Bestehendes Concept
//      {
//        selIndex = 1;
//        csev = (CodeSystemEntityVersion) ArgumentHelper.getWindowArgument("CSEV");
//        ((Row) getFellow("rChooseVoc")).setVisible(false);
//        ((Row) getFellow("rListBoxVoc")).setVisible(false);
//        ((Row) getFellow("rVsCode")).setVisible(false);
//        ((Textbox) getFellow("tbCode")).setSclass("");
//        ((Textbox) getFellow("tbCode")).setReadonly(true);
//        ((Textbox) getFellow("tbCode")).setDisabled(true);
//        ((Textbox) getFellow("tbCode")).setValue(csev.getCodeSystemConcepts().get(0).getCode());
//        ((Textbox) getFellow("tbTerm")).setSclass("");
//        ((Textbox) getFellow("tbTerm")).setReadonly(true);
//        ((Textbox) getFellow("tbTerm")).setDisabled(true);
//        ((Textbox) getFellow("tbTerm")).setValue(csev.getCodeSystemConcepts().get(0).getTerm());
//
//        ((Textbox) getFellow("tbAbbrevation")).setReadonly(true);
//        ((Textbox) getFellow("tbAbbrevation")).setDisabled(true);
//        if (csev.getCodeSystemConcepts().get(0).getTermAbbrevation() != null)
//          ((Textbox) getFellow("tbAbbrevation")).setValue(csev.getCodeSystemConcepts().get(0).getTermAbbrevation());
//
//        ((Checkbox) getFellow("cbPreferred")).setDisabled(true);
//
//        if (csev.getCodeSystemConcepts().get(0).isIsPreferred() != null)
//          ((Checkbox) getFellow("cbPreferred")).setChecked(csev.getCodeSystemConcepts().get(0).isIsPreferred());
//
//        if (csev.getCodeSystemConcepts().get(0).getDescription() != null)
//          ((Textbox) getFellow("tbDescription")).setValue(csev.getCodeSystemConcepts().get(0).getDescription());
//
//        ((Textbox) getFellow("tbDescription")).setReadonly(true);
//        ((Textbox) getFellow("tbDescription")).setDisabled(true);
//      }
//
//      if (source instanceof CodeSystemVersion && isExisting && type.equals(""))
//      { //Existierendes Vokabular
//
//        selIndex = 0;
//        vocOrValChecked(0);
//        CodeSystemVersion csv = (CodeSystemVersion) source;
//        ((Textbox) getFellow("tbVocName")).setValue(csv.getCodeSystem().getName());
//        ((Textbox) getFellow("tbVocName")).setReadonly(true);
//        ((Textbox) getFellow("tbVocName")).setDisabled(true);
//        ((Textbox) getFellow("tbVocName")).setSclass("");
//        ((Textbox) getFellow("tbVocDescription")).setValue(csv.getCodeSystem().getDescription());
//        ((Textbox) getFellow("tbVocDescription")).setReadonly(true);
//        ((Textbox) getFellow("tbVocDescription")).setDisabled(true);
//        ((Textbox) getFellow("tbVocVersionName")).setValue(csv.getName());
//        ((Textbox) getFellow("tbVocVersionName")).setReadonly(true);
//        ((Textbox) getFellow("tbVocVersionName")).setDisabled(true);
//        ((Textbox) getFellow("tbVocVersionName")).setSclass("");
//        ((Textbox) getFellow("tbVocVersionDescription")).setValue(csv.getName());
//        ((Textbox) getFellow("tbVocVersionDescription")).setReadonly(true);
//        ((Textbox) getFellow("tbVocVersionDescription")).setDisabled(true);
//        ((Checkbox) getFellow("cbVoc")).setDisabled(true);
//        ((Checkbox) getFellow("cbVoc")).setSclass("");
//        ((Checkbox) getFellow("cbVal")).setDisabled(true);
//        ((Checkbox) getFellow("cbVal")).setSclass("");
//      }
//
//      if (source instanceof ValueSetVersion && !isExisting && type.equals("")) //Neuer ConceptMembership
//      {
//        selIndex = 1;
//        Include incVoc = (Include) getFellow("incListVoc");
//        Window windowVoc = (Window) incVoc.getFellow("duallistboxVoc");
//        dlbv = (DualListboxVoc) windowVoc.getFellow("dualLBoxVoc");
//
//        ((Row) getFellow("rAbbrevation")).setVisible(false);
//        ((Row) getFellow("rPreferred")).setVisible(false);
//        ((Row) getFellow("rDescription")).setVisible(false);
//        ((Row) getFellow("rTerm")).setVisible(false);
//        ((Row) getFellow("rCode")).setVisible(false);
//        ((Row) getFellow("rChooseVoc")).setVisible(true);
//        ((Row) getFellow("rListBoxVoc")).setVisible(true);
//        ((Row) getFellow("rVsCode")).setVisible(true);
//
//      }
//
//      if (source instanceof ValueSetVersion && isExisting && type.equals("conceptmembership")) //Bestehender ConceptMembership Infos ähnlich wie bei CSC
//      {
//        selIndex = 1;
//        csev = (CodeSystemEntityVersion) ArgumentHelper.getWindowArgument("CSEV");
//        ((Row) getFellow("rChooseVoc")).setVisible(false);
//        ((Row) getFellow("rListBoxVoc")).setVisible(false);
//        ((Row) getFellow("rVsCode")).setVisible(false);
//
//        ((Row) getFellow("rOriginalCodeSystem")).setVisible(true);
//
//        String sSource = ""; // TODO
//        //TreeNode treeNode = (TreeNode)ArgumentHelper.getWindowArgument("TreeNode");
////        String sSource = treeNode.getSourceCSV().getName();
////            if (treeNode.getSourceCSV().getOid() != null && treeNode.getSourceCSV().getOid().length() > 0)
////              sSource += " (" + treeNode.getSourceCSV().getOid() + ")";
//
//        ((Label) getFellow("lOriginalCodeSystem")).setValue(sSource);
//
//        ((Textbox) getFellow("tbCode")).setSclass("");
//        ((Textbox) getFellow("tbCode")).setReadonly(true);
//        ((Textbox) getFellow("tbCode")).setDisabled(true);
//        ((Textbox) getFellow("tbCode")).setValue(csev.getCodeSystemConcepts().get(0).getCode());
//        ((Textbox) getFellow("tbTerm")).setSclass("");
//        ((Textbox) getFellow("tbTerm")).setReadonly(true);
//        ((Textbox) getFellow("tbTerm")).setDisabled(true);
//        ((Textbox) getFellow("tbTerm")).setValue(csev.getCodeSystemConcepts().get(0).getTerm());
//
//        ((Textbox) getFellow("tbAbbrevation")).setReadonly(true);
//        ((Textbox) getFellow("tbAbbrevation")).setDisabled(true);
//        if (csev.getCodeSystemConcepts().get(0).getTermAbbrevation() != null)
//          ((Textbox) getFellow("tbAbbrevation")).setValue(csev.getCodeSystemConcepts().get(0).getTermAbbrevation());
//
//        ((Checkbox) getFellow("cbPreferred")).setDisabled(true);
//
//        if (csev.getCodeSystemConcepts().get(0).isIsPreferred() != null)
//          ((Checkbox) getFellow("cbPreferred")).setChecked(csev.getCodeSystemConcepts().get(0).isIsPreferred());
//
//        if (csev.getCodeSystemConcepts().get(0).getDescription() != null)
//          ((Textbox) getFellow("tbDescription")).setValue(csev.getCodeSystemConcepts().get(0).getDescription());
//
//        ((Textbox) getFellow("tbDescription")).setReadonly(true);
//        ((Textbox) getFellow("tbDescription")).setDisabled(true);
//
//      }
//
//      if (source instanceof ValueSetVersion && isExisting && type.equals(""))
//      { //Existierendes ValueSet
//        ((Checkbox) getFellow("cbVal")).setChecked(true);
//        ((Checkbox) getFellow("cbVoc")).setChecked(false);
//        selIndex = 0;
//        ValueSetVersion vsv = (ValueSetVersion) source;
//        ((Textbox) getFellow("tbVocName")).setValue(vsv.getValueSet().getName());
//        ((Textbox) getFellow("tbVocName")).setReadonly(true);
//        ((Textbox) getFellow("tbVocName")).setDisabled(true);
//        ((Textbox) getFellow("tbVocName")).setSclass("");
//        ((Textbox) getFellow("tbVocDescription")).setValue(vsv.getValueSet().getDescription());
//        ((Textbox) getFellow("tbVocDescription")).setReadonly(true);
//        ((Textbox) getFellow("tbVocDescription")).setDisabled(true);
//        ((Textbox) getFellow("tbVocVersionName")).setValue(vsv.getName());
//        ((Textbox) getFellow("tbVocVersionName")).setReadonly(true);
//        ((Textbox) getFellow("tbVocVersionName")).setDisabled(true);
//        ((Textbox) getFellow("tbVocVersionName")).setSclass("");
//
//        ((Label) getFellow("lVocVersionDescription")).setValue("OID: ");
//        if (vsv.getOid() != null)
//        {
//          ((Textbox) getFellow("tbVocVersionDescription")).setValue(vsv.getOid());
//        }
//        else
//        {
//          ((Textbox) getFellow("tbVocVersionDescription")).setValue("-");
//        }
//        ((Textbox) getFellow("tbVocVersionDescription")).setReadonly(true);
//        ((Textbox) getFellow("tbVocVersionDescription")).setDisabled(true);
//        ((Checkbox) getFellow("cbVoc")).setDisabled(true);
//        ((Checkbox) getFellow("cbVoc")).setSclass("");
//        ((Checkbox) getFellow("cbVal")).setDisabled(true);
//        ((Checkbox) getFellow("cbVal")).setSclass("");
//      }
//
//      if (selIndex >= 0)
//      {
//        // Bestimmten Vorschlag erstellen
//        tb.setSelectedIndex(selIndex);
//
//        // Alle anderen Tabs ausblenden
//        for (Component compTab : tb.getTabs().getChildren())
//        {
//          if (compTab instanceof Tab)
//          {
//            ((Tab) compTab).setVisible(((Tab) compTab).getIndex() == selIndex);
//          }
//        }
//      }
//      else
//      {
//        // Standard-Auswahl
//        tb.setSelectedIndex(0);
//      }
    }
    else
    {

//      int selIndex = 0;
//      Tabbox tb = (Tabbox) getFellow("tbAuswahl");
//      tb.setSelectedIndex(selIndex); //Vokabular
//      for (Component compTab : tb.getTabs().getChildren())
//      {
//        if (compTab instanceof Tab)
//        {
//          ((Tab) compTab).setVisible(((Tab) compTab).getIndex() == selIndex);
//        }
//      }
    }

    if (selectedTab != null && proposalObject != null)
    {
      Tabbox tb = (Tabbox) getFellow("tbAuswahl");
      selectedTab.setVisible(true);
      tb.setSelectedTab(selectedTab);

      proposalObject.initData(this, null);  // TODO Objekt übergeben, wenn bearbeiten
    }
    else
    {
      // TODO Fehlermeldung
      Messagebox.show("Objekt wird nicht unterstützt.");
      this.detach();
    }
  }

  private void loadArguments()
  {
    isExisting = (Boolean) ArgumentHelper.getWindowArgument("isExisting");
    type = (String) ArgumentHelper.getWindowArgumentString("type");

    source = ArgumentHelper.getWindowArgument("source");
    codeSystemId = ArgumentHelper.getWindowArgumentLong("codeSystemId");
    codeSystemVersionId = ArgumentHelper.getWindowArgumentLong("codeSystemVersionId");
    valueSetId = ArgumentHelper.getWindowArgumentLong("valueSetId");
    valueSetVersionId = ArgumentHelper.getWindowArgumentLong("valueSetVersionId");
    
    String objectName = ArgumentHelper.getWindowArgumentString("objectName");
    String objectVersionName = ArgumentHelper.getWindowArgumentString("objectVersionName");

    if (isExisting == false)
    {
      // new proposal
      if (codeSystemId > 0)
      {
        proposal.setObjectId(codeSystemId);
        proposal.setObjectVersionId(codeSystemVersionId);
      }
      else if(valueSetId > 0)
      {
        proposal.setObjectId(valueSetId);
        proposal.setObjectVersionId(valueSetVersionId);
      }
      
      proposal.setObjectName(objectName);
      proposal.setObjectVersionName(objectVersionName);
    }

//    Object o = ArgumentHelper.getWindowArgument("parentCodeSystemEntityVersion");
//    if (o != null)
//    {
//      if (o instanceof CodeSystemEntityVersion)
//      {
//        parentTerm = (CodeSystemEntityVersion) o;
//      }
//    }
  }

  public void vocOrValChecked(int i)
  {

    Checkbox cbVoc = (Checkbox) getFellow("cbVoc");
    Checkbox cbVal = (Checkbox) getFellow("cbVal");
    Label lVersion = (Label) getFellow("lVersion");
    Row rVocVersonName = (Row) getFellow("rVocVersionName");
    Row rVocVersionDescription = (Row) getFellow("rVocVersionDescription");

    if (i == 0)
    {

      if (cbVoc.isChecked())
      {

        cbVal.setChecked(false);
        lVersion.setVisible(true);
        rVocVersonName.setVisible(true);
        rVocVersionDescription.setVisible(true);
        lVersion.setValue("Code System Version");

      }
      else
      {

        cbVal.setChecked(true);
        lVersion.setVisible(true);
        rVocVersonName.setVisible(true);
        rVocVersionDescription.setVisible(false);
        lVersion.setValue("Value Set Version");
      }
    }
    else if (i == 1)
    {
      if (cbVal.isChecked())
      {
        cbVoc.setChecked(false);
        lVersion.setVisible(true);
        lVersion.setValue("Value Set Version");
        rVocVersonName.setVisible(true);
        rVocVersionDescription.setVisible(false);
      }
      else
      {
        cbVoc.setChecked(true);
        lVersion.setVisible(true);
        rVocVersonName.setVisible(true);
        rVocVersionDescription.setVisible(true);
        lVersion.setValue("Code System Version");
      }
    }

  }

  private void initData()
  {
    //proposal = new Proposal();

    /*//Init Voc Data
     ListCodeSystemsInTaxonomyRequestType parameter = new ListCodeSystemsInTaxonomyRequestType();
     ListCodeSystemsInTaxonomyResponse.Return response = WebServiceHelper.listCodeSystemsInTaxonomy(parameter);

     if (response.getReturnInfos().getStatus() == Status.OK)
     {

     for (DomainValue dv : response.getDomainValue())
     {

     for (CodeSystem cs : dv.getCodeSystems())
     {

     for (CodeSystemVersion csv : cs.getCodeSystemVersions())
     {
     VocInfo vi = new VocInfo(csv.getVersionId(), cs.getId(), cs.getName(), csv.getName());
     vocData.add(vi);
     }
     }
     }

     Executions.getCurrent().setAttribute("vocData", vocData);
     Executions.getCurrent().setAttribute("choosenVocData", choosenVocData);
     }
     else
     {

     Messagebox.show(response.getReturnInfos().getMessage(), "Fehler beim Laden der Code Systeme!", Messagebox.OK, Messagebox.ERROR);
     }*/
  }

  public void onOkClicked()
  {
    // check mandatory fields
    if (((Textbox) getFellow("tbProposal")).getText().length() == 0)
    {
      Messagebox.show("Sie müssen einen Vorschlag eingeben.");
      return;
    }

    String s = proposalObject.checkMandatoryFields(this);
    if (isNullOrEmtpy(s) == false)
    {
      Messagebox.show(s);
      return;
    }

    try
    {
      List<Object> proposalObjects = new LinkedList<Object>();

      // save proposal data
      proposal.setDescription(((Textbox) getFellow("tbProposal")).getText());
      proposal.setCreated(new Date());

      // save specific proposal data
      proposalObject.saveData(this, proposal, proposalObjects);

      ReturnType ret = ProposalWorkflow.getInstance().addProposal(proposal, proposalObjects, isExisting);
      if (ret.isSuccess())
      {
        Messagebox.show(ret.getMessage(), "Vorschlag erstellen", Messagebox.OK, Messagebox.INFORMATION);
        this.detach();
      }
      else
      {
        if (ret.getMessage() == null || ret.getMessage().length() == 0)
          Messagebox.show("Fehler beim Einfügen eines Vorschlags.");
        else
          Messagebox.show(ret.getMessage());
        return;
      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      Messagebox.show("Fehler beim Speichern des Vorschlags: " + ex.getLocalizedMessage());
    }

    //
    //ret = ProposalWorkflow.getInstance().addProposal(proposal, csev, source, valueSetVersionId, isExisting);
    /*Checkbox cbVoc = (Checkbox) getFellow("cbVoc");
     Checkbox cbVal = (Checkbox) getFellow("cbVal");
     Tabbox tb = (Tabbox) getFellow("tbAuswahl");
     boolean runS = true;

     if (cbVoc.isChecked())
     {

     ListCodeSystemsInTaxonomyRequestType para = new ListCodeSystemsInTaxonomyRequestType();

     if (SessionHelper.isCollaborationActive())
     {
     // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
     para.setLoginToken(CollaborationSession.getInstance().getSessionID());
     }
     else if (SessionHelper.isUserLoggedIn())
     {
     para.setLoginToken(SessionHelper.getSessionId());
     }

     //Search_Service service = new Search_Service();
     //Search port = service.getSearchPort();
     de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse.Return resp = WebServiceHelper.listCodeSystemsInTaxonomy(para);

     for (DomainValue dv : resp.getDomainValue())
     {

     for (CodeSystem csL : dv.getCodeSystems())
     {
     if ((((Textbox) getFellow("tbVocName")).getText()).equals(csL.getName()))
     {
     runS = false;
     }
     }
     }
     }
     else if (cbVal.isChecked())
     {

     ListValueSetsRequestType para = new ListValueSetsRequestType();

     // login
     if (SessionHelper.isCollaborationActive())
     {
     // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
     para.setLoginToken(CollaborationSession.getInstance().getSessionID());
     }
     else if (SessionHelper.isUserLoggedIn())
     {
     para.setLoginToken(SessionHelper.getSessionId());
     }

     ListValueSetsResponse.Return resp = WebServiceHelper.listValueSets(para);

     for (ValueSet vsL : resp.getValueSet())
     {
     if ((((Textbox) getFellow("tbVocName")).getText()).equals(vsL.getName()))
     {
     runS = false;
     }
     }
     }

     if (isExisting || (!isExisting && runS) || type.equals("concept") || type.equals("conceptmembership"))
     {
     if (logger.isDebugEnabled())
     logger.debug("Daten speichern");

     ReturnType ret = null;

     int selIndex = tb.getSelectedIndex();

     // Abhängig von der Auswahl den Vorschlag erstellen
     if (selIndex == 0)
     {

     if (cbVoc.isChecked())
     {

     if (!isExisting)
     {
     // Erstellt ein neues Vokabular
     CodeSystem cs = new CodeSystem();
     cs.setName(((Textbox) getFellow("tbVocName")).getText());
     cs.setDescription(((Textbox) getFellow("tbVocDescription")).getText());

     CodeSystemVersion csv = new CodeSystemVersion();
     csv.setName(((Textbox) getFellow("tbVocVersionName")).getText());
     csv.setDescription(((Textbox) getFellow("tbVocVersionDescription")).getText());
     csv.setStatus(0);
     //csv.setValidityRange(236l); //optional
     csv.setValidityRange(4l); //optional

     cs.getCodeSystemVersions().add(csv);

     proposal.setVocabularyId(0l);  // wird nach Erstellen eingefügt
     proposal.setVocabularyName(cs.getName());
     proposal.setContentType("vocabulary");
     proposal.setVocabularyNameTwo("CodeSystem");

     if (pruefePflichtfelder_Vocabulary(cs, csv))
     {
     ret = ProposalWorkflow.getInstance().addProposal(proposal, cs, isExisting);
     }
     }
     else
     {
     proposal.setVocabularyId(((CodeSystemVersion) source).getCodeSystem().getId());      //Existing Vok 
     proposal.setVocabularyName(((CodeSystemVersion) source).getCodeSystem().getName());
     proposal.setVocabularyIdTwo(((CodeSystemVersion) source).getCodeSystem().getId());
     proposal.setContentType("vocabulary");
     proposal.setVocabularyNameTwo("CodeSystem");
     ret = ProposalWorkflow.getInstance().addProposal(proposal, source, isExisting);
     }
     }
     else
     {

     if (!isExisting)
     {
     // Erstellt ein neues ValueSet
     ValueSet vs = new ValueSet();
     vs.setName(((Textbox) getFellow("tbVocName")).getText());
     vs.setDescription(((Textbox) getFellow("tbVocDescription")).getText());

     ValueSetVersion vsv = new ValueSetVersion();
     vsv.setStatus(0);
     vsv.setName(((Textbox) getFellow("tbVocVersionName")).getText());

     vs.getValueSetVersions().add(vsv);

     proposal.setVocabularyId(0l);  // wird nach Erstellen eingefügt
     proposal.setVocabularyName(vs.getName());
     proposal.setContentType("valueset");
     proposal.setVocabularyNameTwo("ValueSet");

     if (pruefePflichtfelder_ValueSet(vs))
     {
     ret = ProposalWorkflow.getInstance().addProposal(proposal, vs, isExisting);
     }
     }
     else
     {

     proposal.setVocabularyId(((ValueSetVersion) source).getValueSet().getId());  //Existing VS
     proposal.setVocabularyName(((ValueSetVersion) source).getValueSet().getName());
     proposal.setVocabularyIdTwo(((ValueSetVersion) source).getValueSet().getId());
     proposal.setContentType("valueset");
     proposal.setVocabularyNameTwo("ValueSet");
     ret = ProposalWorkflow.getInstance().addProposal(proposal, source, isExisting);
     }
     }
     }
     else if (selIndex == 1)
     {

     if (source instanceof CodeSystemVersion)
     {

     if (!isExisting)
     {
     // Erstellt einen neuen Begriff
     Object obj2 = null;
     proposal.setVocabularyId(codeSystemVersionId);
     proposal.setVocabularyName(getSourceString());
     proposal.setVocabularyIdTwo(((CodeSystemVersion) source).getCodeSystem().getId());
     proposal.setContentType("concept");
     proposal.setVocabularyNameTwo("CodeSystem");

     CodeSystemConcept csc = new CodeSystemConcept();
     csc.setCode(((Textbox) getFellow("tbCode")).getText());
     csc.setDescription(((Textbox) getFellow("tbDescription")).getText());
     csc.setIsPreferred(((Checkbox) getFellow("cbPreferred")).isChecked());
     csc.setTerm(((Textbox) getFellow("tbTerm")).getText());
     csc.setTermAbbrevation(((Textbox) getFellow("tbAbbrevation")).getText());

     // Beziehung zu Vokabular setzen
     CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
     csvem.setIsAxis(false); // TODO

     if (parentTerm == null)
     csvem.setIsMainClass(true);
     else
     csvem.setIsMainClass(false);

     csvem.setCodeSystemVersion(new CodeSystemVersion());
     csvem.getCodeSystemVersion().setVersionId(codeSystemVersionId);
     CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
     csev.setCodeSystemEntity(new CodeSystemEntity());
     csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(csvem);
     csc.setCodeSystemEntityVersion(csev);

     if (parentTerm != null)
     {
     // Neuer Unterbegriff, also auch Beziehung hinzufügen
     proposal.setContentType("subconcept");

     CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();
     cseva.setAssociationKind(2);
     cseva.setAssociationType(new AssociationType());
     cseva.getAssociationType().setCodeSystemEntityVersionId(4L); // TODO 4 ist zur Zeit Standard für Unterklasse
     cseva.setLeftId(parentTerm.getVersionId());
     cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(parentTerm);
     //cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
     //cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(parentTerm.getVersionId());

     obj2 = cseva;
     }

     if (pruefePflichtfelder_Concept(csc))
     {
     ret = ProposalWorkflow.getInstance().addProposal(proposal, csc, obj2, codeSystemId, isExisting);
     }
     }
     else
     {

     // Erstellt einen neuen Begriff
     proposal.setVocabularyId(codeSystemVersionId);  //Existing Konzept
     proposal.setVocabularyName(getSourceString());
     proposal.setVocabularyIdTwo(((CodeSystemVersion) source).getCodeSystem().getId());
     proposal.setContentType("concept");
     proposal.setVocabularyNameTwo("CodeSystem");

     ret = ProposalWorkflow.getInstance().addProposal(proposal, csev, isExisting);
     }
     }
     if (source instanceof ValueSetVersion)
     {

     if (!isExisting)
     {
     // Erstellt einen neuen Begriff
     proposal.setVocabularyId(valueSetVersionId);
     proposal.setVocabularyName(getSourceString());
     proposal.setVocabularyIdTwo(((ValueSetVersion) source).getValueSet().getId());
     proposal.setContentType("conceptVs");
     proposal.setVocabularyNameTwo("ValueSet");

     ValueSetVersion vsv = new ValueSetVersion();
     vsv.setVersionId(valueSetVersionId);
     vsv.setValueSet(null);
     //CSEV Später!

     ConceptValueSetMembership cvsm = new ConceptValueSetMembership();
     cvsm.setStatus(0);
     cvsm.setStatusDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
     cvsm.setValueSetVersion(vsv);

     if (pruefePflichtfelder_ConceptVs(cvsm))
     {

     ret = ProposalWorkflow.getInstance().addProposal(proposal, dlbv.getChosenDataList().iterator().next(), source, valueSetVersionId, ((Textbox) getFellow("tbCodeVs")).getText(), isExisting);
     }
     }
     else
     {

     // Erstellt einen neuen Begriff
     proposal.setVocabularyId(valueSetVersionId);
     proposal.setVocabularyName(getSourceString());
     proposal.setVocabularyIdTwo(((ValueSetVersion) source).getValueSet().getId());
     proposal.setContentType("conceptVs");
     proposal.setVocabularyNameTwo("ValueSet");

     ret = ProposalWorkflow.getInstance().addProposal(proposal, csev, source, valueSetVersionId, isExisting);

     }
     }
     }

     // Antwort auswerten
     if (ret != null)
     {
     if (ret.isSuccess())
     {
     // Erfolg
     if (ret.getMessage() != null && ret.getMessage().length() > 0)
     {
     Messagebox.show(ret.getMessage(), "Vorschlag erstellen", Messagebox.OK, Messagebox.INFORMATION);
     }

     // Formular schließen
     this.setVisible(false);
     this.detach();

     // Hauptansicht aktualisieren
     if (updateInterface != null)
     {
     updateInterface.update(proposal, false);
     }
     }
     else
     {
     // Fehlermeldung ausgeben
     Messagebox.show("Code wurde nicht gefunden!", "Vorschlag erstellen", Messagebox.OK, Messagebox.ERROR);
     }
     }
     }
     else
     {
     Messagebox.show("Ein CodeSystem/ValueSet mit dem selben Namen existiert bereits!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
     }*/
  }

  private boolean isNullOrEmtpy(String s)
  {
    if (s == null || s.length() == 0)
      return true;
    else
      return false;
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  public String getSourceString()
  {
    if (source != null)
    {
      if (source instanceof CodeSystemVersion)
      {
        return ((CodeSystemVersion) source).getCodeSystem().getName() + " - " + ((CodeSystemVersion) source).getName();
      }
      else if (source instanceof ValueSetVersion)
      {
        String name = "";
        name = ((ValueSetVersion) source).getValueSet().getName();
        if (((ValueSetVersion) source).getName() != null)
        {
          name += " - " + ((ValueSetVersion) source).getName();
        }
        else
        {
          name += " - " + ((ValueSetVersion) source).getOid();
        }
        return name;
      }
    }

    return "";
  }

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }

  /**
   * @return the proposal
   */
  public Proposal getProposal()
  {
    return proposal;
  }

  /**
   * @param proposal the proposal to set
   */
  public void setProposal(Proposal proposal)
  {
    this.proposal = proposal;
  }

}
