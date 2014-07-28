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

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.LanguageHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.ValidityRangeHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.models.comparators.ComparatorMetadataParameter;
import de.fhdo.models.itemrenderer.ListitemRendererMetadataParameter;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Becker
 */
public class PopupCodeSystem extends PopupWindow implements IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private CodeSystem cs = null;
  private CodeSystemVersion csv = null;
  private Datebox dateBoxED,
          dateBoxRD,
          dateBoxSD;
  private Checkbox cbNewVersion, cbCSVLicenced;
  private Button bCreate, bOidBeantragen;
  private Label lReq, lName, lNameVersion, lLic;
  private Textbox tbCSName, tbCSDescription, tbCSVName, tbCSVDescription, tbCSVLicenceHolder, tbCSVSource, tbCSVOId, tbCSVStatus, tbCSDescriptionEng, tbWebsite;
  private Listbox listMetadataParameter;
  private Combobox cboxPreferredLanguage, cboxCSVValidityRange;
  //private Combobox cboxCSVValidityRange;
  private boolean toEdit = false;

  @Override
  public void doAfterComposeCustom()
  {
    //cboxPreferredLanguage.setModel(LanguageHelper.getListModelList());
    cboxCSVValidityRange.setModel(ValidityRangeHelper.getListModelList());
    
    
  }

  public void onClick$bOidBeantragen()
  {

    try
    {
      logger.debug("Öffnen des OID Antragsformulars");

      Map map = new HashMap();
      map.put("version", csv);

      Window win = (Window) Executions.createComponents(
              "/gui/main/modules/oidEnquiry.zul", null, map);
      ((OidEnquiry) win).setUpdateInterface(this);

      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der UserDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  private void loadDetails()
  {
    ReturnCodeSystemDetailsRequestType parameter = new ReturnCodeSystemDetailsRequestType();

    // Login
    if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    // CS und CSV
    CodeSystem csTemp = new CodeSystem();
    csTemp.setId(cs.getId());
    if (csv != null)
    {
      CodeSystemVersion csvTemp = new CodeSystemVersion();
      csTemp.getCodeSystemVersions().add(csvTemp);
      csvTemp.setVersionId(csv.getVersionId());
    }
    parameter.setCodeSystem(csTemp);

    ReturnCodeSystemDetailsResponse.Return response = WebServiceHelper.returnCodeSystemDetails(parameter);

    // Meldung falls CreateConcept fehlgeschlagen
    if (response.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.search.Status.OK)
    {
      try
      {
        Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupCodeSystem.loadCSDetailsFailed") + "\n\n" + response.getReturnInfos().getMessage());
        return;
      }
      catch (Exception ex)
      {
        Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    cs = response.getCodeSystem();

    // Verison laden, falls angegeben       
    if (csv != null)
    {
      Iterator<CodeSystemVersion> itCSV = cs.getCodeSystemVersions().iterator();
      while (itCSV.hasNext())
      {
        CodeSystemVersion csvTemp2 = itCSV.next();
        if (csvTemp2.getVersionId().equals(csv.getVersionId()))
        {
          csv = csvTemp2;
          break;
        }
      }
    }

    loadMetaParameter();
  }

  @Override
  protected void initializeDatabinder()
  {
    loadDatesIntoGUI();
    binder = new AnnotateDataBinder(window);
    binder.bindBean("cs", cs);
    binder.bindBean("csv", csv);
    binder.bindBean("versioning", versioning);
    binder.loadAll();
  }

  private void createNewCodeSystemVersion()
  {
    csv = new CodeSystemVersion();
    csv.setUnderLicence(Boolean.FALSE);             // sonst ist das hier null und das kann zu Problemen führen
    csv.setVersionId(Long.MAX_VALUE);               // MaintainCodeSystemVersion fordert eine versionID > 0. Hat aber keine Auswirkung        
    csv.setStatus(1);
    versioning = new VersioningType();
    versioning.setCreateNewVersion(Boolean.TRUE);   // sonst ist das hier null und das kann zu Problemen führen                
    dateBoxED.setValue(null);
    dateBoxRD.setValue(null);
  }

  private void loadMetaParameter()
  {
    // ItemRenderer
    listMetadataParameter.setItemRenderer(new ListitemRendererMetadataParameter());

    // Listhead und Listheader
    Listheader lh1 = new Listheader("Parameter"),
            lh2 = new Listheader("Datatype"),
            lh3 = new Listheader("Parametertype");
    listMetadataParameter.getListhead().getChildren().add(lh1);
    listMetadataParameter.getListhead().getChildren().add(lh2);
    listMetadataParameter.getListhead().getChildren().add(lh3);

    // Metadaten laden    
    List metadata = new ArrayList();
    for (MetadataParameter mpd : cs.getMetadataParameters())
    {
      metadata.add(mpd);
    }
    listMetadataParameter.setModel(new SimpleListModel(metadata));

    // sortieren
    lh1.setSortAscending(new ComparatorMetadataParameter(true));
    lh1.setSortDescending(new ComparatorMetadataParameter(false));
    lh1.setSortDirection("descending");
    lh1.sort(true);
  }

//  public void onInitRenderLater$cboxPreferredLanguage(Event e)
//  {
//    if (csv == null || csv.getPreferredLanguageCd() == null || csv.getPreferredLanguageCd().length() == 0)
//      return;
//
//    Iterator<Comboitem> it = cboxPreferredLanguage.getItems().iterator();
//    while (it.hasNext())
//    {
//      Comboitem ci = it.next();
//      /*TODOif (csv.getPreferredLanguageId().compareTo(LanguageHelper.getLanguageIdByName(ci.getLabel())) == 0)
//      {
//        cboxPreferredLanguage.setSelectedItem(ci);
//      }*/
//      
//      /*if (csv.getPreferredLanguageCd().compareTo(LanguageHelper.getLanguageIdByName(ci.getLabel())) == 0)
//      {
//        cboxPreferredLanguage.setSelectedItem(ci);
//      }*/
//    }
//  }

  public void onInitRenderLater$cboxCSVValidityRange(Event e)
  {
    if (csv == null || csv.getValidityRange() == null || csv.getValidityRange() < 1)
      return;

    Iterator<Comboitem> it = cboxCSVValidityRange.getItems().iterator();
    while (it.hasNext())
    {
      Comboitem ci = it.next();
      if (csv.getValidityRange().compareTo(ValidityRangeHelper.getValidityRangeIdByName(ci.getLabel())) == 0)
      {
        cboxCSVValidityRange.setSelectedItem(ci);
      }
    }
  }

  @Override
  protected void loadDatesIntoGUI()
  {
    String preferredLanguageCd = "";
    
    if (csv != null)
    {
      if (csv.getExpirationDate() != null)
        dateBoxED.setValue(new Date(csv.getExpirationDate().toGregorianCalendar().getTimeInMillis()));
      if (csv.getReleaseDate() != null)
        dateBoxRD.setValue(new Date(csv.getReleaseDate().toGregorianCalendar().getTimeInMillis()));
      if (csv.getStatusDate() != null)
        dateBoxSD.setValue(new Date(csv.getStatusDate().toGregorianCalendar().getTimeInMillis()));
      
      preferredLanguageCd = csv.getPreferredLanguageCd();
    }
    
    DomainHelper.getInstance().fillCombobox(cboxPreferredLanguage, de.fhdo.Definitions.DOMAINID_LANGUAGECODES, preferredLanguageCd);
  }

  @Override
  protected void editmodeDetails()
  {
    window.setTitle(Labels.getLabel("common.codeSystem") + " " + Labels.getLabel("common.details"));
    cs = (CodeSystem) arg.get("CS");
    csv = (CodeSystemVersion) arg.get("CSV");

    loadDetails();

    cbNewVersion.setVisible(false);
    cbCSVLicenced.setDisabled(true);
    dateBoxED.setDisabled(true);
    dateBoxRD.setDisabled(true);
    tbCSName.setReadonly(true);
    tbCSDescription.setReadonly(true);
    tbCSDescriptionEng.setReadonly(true);
    tbWebsite.setReadonly(true);
    tbCSVName.setReadonly(true);
    tbCSVDescription.setReadonly(true);
    //cboxCSVValidityRange.setReadonly(true);
    cboxCSVValidityRange.setDisabled(true);
    tbCSVLicenceHolder.setReadonly(true);
    tbCSVSource.setReadonly(true);
    tbCSVStatus.setReadonly(true);
        //bOidBeantragen.setVisible(true);
    //bOidBeantragen.setDisabled(true);
    tbCSVOId.setVisible(true);
    tbCSVOId.setReadonly(true);
    //cboxPreferredLanguage.setReadonly(true);
    cboxPreferredLanguage.setDisabled(true);
    lReq.setVisible(false);
    lName.setValue(Labels.getLabel("common.name"));
    lNameVersion.setValue(Labels.getLabel("common.name"));
    lLic.setValue(Labels.getLabel("common.licenced"));
    bCreate.setVisible(false);
  }

  @Override
  protected void editmodeCreate()
  {
    window.setTitle(Labels.getLabel("popupCodeSystem.createCodeSystem"));
    cs = new CodeSystem();
    createNewCodeSystemVersion();

    cbNewVersion.setVisible(true);
    cbNewVersion.setDisabled(true);
    cbCSVLicenced.setDisabled(false);
    dateBoxED.setDisabled(false);
    dateBoxRD.setDisabled(false);
    tbCSName.setReadonly(false);
    tbCSDescription.setReadonly(false);
    tbCSDescriptionEng.setReadonly(false);
    tbWebsite.setReadonly(false);
    tbCSVName.setReadonly(false);
    tbCSVDescription.setReadonly(false);
    //cboxCSVValidityRange.setReadonly(false);
    cboxCSVValidityRange.setDisabled(false);
    tbCSVLicenceHolder.setReadonly(true);
    tbCSVSource.setReadonly(false);
    tbCSVStatus.setReadonly(true);
        //bOidBeantragen.setVisible(true);
    //bOidBeantragen.setDisabled(false);
    tbCSVOId.setVisible(true);
    tbCSVOId.setReadonly(false);
    //cboxPreferredLanguage.setReadonly(false);
    cboxPreferredLanguage.setDisabled(false);
    lReq.setVisible(true);
    lName.setValue(Labels.getLabel("common.name") + "*");
    lNameVersion.setValue(Labels.getLabel("common.name") + "*");
    lLic.setValue(Labels.getLabel("common.licenced") + "*");
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.create"));
  }

  @Override
  protected void editmodeMaintainVersionNew()
  {
    window.setTitle(Labels.getLabel("popupCodeSystem.createCodeSystemVersion"));
    cs = (CodeSystem) arg.get("CS");
    loadDetails();
    createNewCodeSystemVersion();

    cbNewVersion.setVisible(true);
    cbNewVersion.setDisabled(false);
    cbCSVLicenced.setDisabled(false);
    dateBoxED.setDisabled(false);
    dateBoxRD.setDisabled(false);
    tbCSName.setReadonly(true);
    tbCSDescription.setReadonly(true);
    tbCSDescriptionEng.setReadonly(true);
    tbWebsite.setReadonly(true);
    tbCSVName.setReadonly(false);
    tbCSVDescription.setReadonly(false);
    //cboxCSVValidityRange.setReadonly(false);
    cboxCSVValidityRange.setDisabled(false);
    tbCSVLicenceHolder.setReadonly(true);
    tbCSVSource.setReadonly(false);
    tbCSVStatus.setReadonly(true);
        //Beim neuanlegen existiert noch keine csv, daher kann man eine OID,
    //erst anlegen wenn das CSV angelegt wurde...
    tbCSVOId.setTooltiptext("Beantragen einer OID ist nach dem Anlegen möglich! "
            + "Bitte rufen sie dazu \"Version bearbeiten\" über das Context-Menü auf! "
            + "Haben sie bereits eine OID können Sie diese hier eintragen.");
    //bOidBeantragen.setVisible(false);
    tbCSVOId.setVisible(true);
    tbCSVOId.setReadonly(false);
    /*
     if(csv != null){
     if(csv.getOid() == null || csv.getOid().length() <= 0){
     bOidBeantragen.setVisible(true);
     bOidBeantragen.setDisabled(false);
     tbCSVOId.setVisible(false);
     }else{
     bOidBeantragen.setVisible(false);
     tbCSVOId.setVisible(true);
     tbCSVOId.setReadonly(false);
     }
     }*/
    //cboxPreferredLanguage.setReadonly(false);
    cboxPreferredLanguage.setDisabled(false);
    lReq.setVisible(true);
    lName.setValue(Labels.getLabel("common.name"));
    lNameVersion.setValue(Labels.getLabel("common.name") + "*");
    lLic.setValue(Labels.getLabel("common.licenced") + "*");
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.create"));
  }

  @Override
  protected void editmodeMaintain()
  {
    window.setTitle(Labels.getLabel("popupCodeSystem.editCodeSystem"));
    cs = (CodeSystem) arg.get("CS");
    csv = (CodeSystemVersion) arg.get("CSV");   // es muss eine Version angegeben werden
    versioning = new VersioningType();
    versioning.setCreateNewVersion(Boolean.FALSE);

    loadDetails();

    cbNewVersion.setVisible(false);
    cbNewVersion.setDisabled(true);
    cbCSVLicenced.setDisabled(false);
    cbCSVLicenced.setChecked(false);
    dateBoxED.setDisabled(true);
    dateBoxRD.setDisabled(true);
    tbCSName.setReadonly(false);
    tbCSDescription.setReadonly(false);
    tbCSDescriptionEng.setReadonly(false);
    tbWebsite.setReadonly(false);
    tbCSVName.setReadonly(true);
    tbCSVDescription.setReadonly(true);
    //cboxCSVValidityRange.setReadonly(true);
    cboxCSVValidityRange.setDisabled(true);
    tbCSVLicenceHolder.setReadonly(true);
    tbCSVSource.setReadonly(false);
    tbCSVStatus.setReadonly(true);
        //bOidBeantragen.setVisible(true);
    //bOidBeantragen.setDisabled(false);
    tbCSVOId.setVisible(true);
    tbCSVOId.setReadonly(false);
    //cboxPreferredLanguage.setReadonly(true);
    cboxPreferredLanguage.setDisabled(true);
    lReq.setVisible(false);
    lName.setValue(Labels.getLabel("common.name"));
    lNameVersion.setValue(Labels.getLabel("common.name"));
    lLic.setValue(Labels.getLabel("common.licenced"));
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.change"));
  }

  @Override
  protected void editmodeMaintainVersionEdit()
  {
    window.setTitle(Labels.getLabel("popupCodeSystem.editCodeSystemVersion"));
    cs = (CodeSystem) arg.get("CS");
    csv = (CodeSystemVersion) arg.get("CSV");
    versioning = new VersioningType();
    versioning.setCreateNewVersion(Boolean.FALSE);

    loadDetails();

    cbNewVersion.setVisible(true);
    cbNewVersion.setDisabled(false);
    cbCSVLicenced.setDisabled(false);
    cbCSVLicenced.setChecked(false);
    dateBoxED.setDisabled(false);
    dateBoxRD.setDisabled(false);
    tbCSName.setReadonly(true);
    tbCSDescription.setReadonly(true);
    tbCSDescriptionEng.setReadonly(true);
    tbWebsite.setReadonly(true);
    tbCSVName.setReadonly(false);
    tbCSVDescription.setReadonly(false);
    //cboxCSVValidityRange.setReadonly(false);
    cboxCSVValidityRange.setDisabled(false);
    tbCSVLicenceHolder.setReadonly(true);
    tbCSVSource.setReadonly(false);
    tbCSVStatus.setReadonly(true);
        //bOidBeantragen.setVisible(true);
    //bOidBeantragen.setDisabled(false);
    tbCSVOId.setVisible(true);
    tbCSVOId.setReadonly(false);
    //cboxPreferredLanguage.setReadonly(false);
    cboxPreferredLanguage.setDisabled(false);
    lReq.setVisible(false);
    lName.setValue(Labels.getLabel("common.name"));
    lNameVersion.setValue(Labels.getLabel("common.name"));
    lLic.setValue(Labels.getLabel("common.licenced"));
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.change"));
  }

  @Override
  protected void editmodeUpdateStatus()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void editmodeUpdateStatusVersion()
  {
    window.setTitle(Labels.getLabel("popupCodeSystem.editCodeSystemVersion"));
    cs = (CodeSystem) arg.get("CS");
    csv = (CodeSystemVersion) arg.get("CSV");
    versioning = new VersioningType();
    versioning.setCreateNewVersion(Boolean.FALSE);

    loadDetails();

    cbNewVersion.setVisible(false);
    cbNewVersion.setDisabled(false);
    cbCSVLicenced.setDisabled(false);
    cbCSVLicenced.setChecked(false);
    dateBoxED.setDisabled(false);
    dateBoxRD.setDisabled(false);
    tbCSName.setReadonly(true);
    tbCSDescription.setReadonly(true);
    tbCSDescriptionEng.setReadonly(true);
    tbWebsite.setReadonly(true);
    tbCSVName.setReadonly(true);
    tbCSVDescription.setReadonly(true);
    //cboxCSVValidityRange.setReadonly(true);
    cboxCSVValidityRange.setDisabled(true);
    tbCSVLicenceHolder.setReadonly(true);
    tbCSVSource.setReadonly(true);
    tbCSVStatus.setReadonly(false);
        //bOidBeantragen.setVisible(true);
    //bOidBeantragen.setDisabled(true);
    tbCSVOId.setVisible(true);
    tbCSVOId.setReadonly(true);
    //cboxPreferredLanguage.setReadonly(true);
    cboxPreferredLanguage.setDisabled(true);
    lReq.setVisible(false);
    lName.setValue(Labels.getLabel("common.name"));
    lNameVersion.setValue(Labels.getLabel("common.name"));
    lLic.setValue(Labels.getLabel("common.licenced"));
    bCreate.setVisible(true);
    bCreate.setLabel(Labels.getLabel("common.changeStatus"));
  }

  @Override
  protected void create()
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
    boolean parameterCorrect = true;

    de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse.Return resp = WebServiceHelper.listCodeSystemsInTaxonomy(para);

    for (DomainValue dv : resp.getDomainValue())
    {

      for (CodeSystem csL : dv.getCodeSystems())
      {
        if (cs.getName().equals(csL.getName()))
        {
          parameterCorrect = false;
        }
      }
    }

    if (parameterCorrect)
    {
      if (cboxCSVValidityRange.getSelectedItem() != null)
      {
        // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
        cs.getCodeSystemVersions().clear();
        cs.getCodeSystemVersions().add(csv);

        // preferredLanguage
        csv.setPreferredLanguageCd(DomainHelper.getInstance().getComboboxCd(cboxPreferredLanguage));
        logger.debug("getPreferredLanguageCd: " + csv.getPreferredLanguageCd());
        /*TODO if (cboxPreferredLanguage.getSelectedItem() != null)
          csv.setPreferredLanguageId(LanguageHelper.getLanguageIdByName(cboxPreferredLanguage.getSelectedItem().getLabel()));*/

        // Range of Validity
        if (cboxCSVValidityRange.getSelectedItem() != null)
          csv.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(cboxCSVValidityRange.getSelectedItem().getLabel()));

        // Daten setzen mit Convertierung von Date -> XMLGregorianCalendar
        try
        {
          GregorianCalendar c;

          if (dateBoxRD != null && dateBoxRD.getValue() != null)
          {
            c = new GregorianCalendar();
            c.setTimeInMillis(dateBoxRD.getValue().getTime());
            csv.setReleaseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
          }

          if (dateBoxED != null && dateBoxED.getValue() != null)
          {
            c = new GregorianCalendar();
            c.setTimeInMillis(dateBoxED.getValue().getTime());
            csv.setExpirationDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
          }
        }
        catch (DatatypeConfigurationException ex)
        {
          Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
        }

        try
        {
          CreateCodeSystemRequestType parameter = new CreateCodeSystemRequestType();

          // Login, cs
          parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
          parameter.setCodeSystem(cs);

          // WS aufruf
          csv.setCodeSystem(null); // XML Zirkel verhindern
          CreateCodeSystemResponse.Return response = WebServiceHelper.createCodeSystem(parameter);
          csv.setCodeSystem(cs);  // Nach WS zirkel wiederherstellen

          // Message über Erfolg/Misserfolg                
          if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
          {

            AssignTermHelper.assignTermToUser(response.getCodeSystem());
            Messagebox.show(Labels.getLabel("popupCodeSystem.newCodeSystemsuccessfullyCreated"));
            ((ContentCSVSDefault) windowParent).refresh();
            window.detach();
          }
          else
            Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupCodeSystem.codeSystemWasNotCreated"));
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }

      }
      else
      {
        Messagebox.show(Labels.getLabel("popupCodeSystem.editCodeSystemValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
      }
    }
    else
    {
      Messagebox.show("Ein CodeSystem mit dem selben Namen existiert bereits!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
    }
  }

  @Override
  protected void maintainVersionNew()
  {

    boolean runS = true;

    if (toEdit == false)
    {
      for (CodeSystemVersion csvL : cs.getCodeSystemVersions())
      {

        if (csvL.getName().equals(csv.getName()))
        {

          runS = false;
          break;
        }
      }
    }

    if (runS)
    {
      Authoring authoring = new Authoring_Service().getAuthoringPort();


      // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
      cs.getCodeSystemVersions().clear();
      cs.getCodeSystemVersions().add(csv);

      // preferredLanguage
      /*TODO if (csv.getPreferredLanguageCd() == null || csv.getPreferredLanguageCd().length() == 0)
      {
        if (cboxPreferredLanguage.getSelectedItem() != null)
          csv.setPreferredLanguageId(LanguageHelper.getLanguageIdByName(cboxPreferredLanguage.getSelectedItem().getLabel()));
      }
      else
      {
        if (cboxPreferredLanguage.getSelectedItem() != null)
          csv.setPreferredLanguageId(LanguageHelper.getLanguageIdByName(cboxPreferredLanguage.getSelectedItem().getLabel()));
        else
          csv.setPreferredLanguageId(null);
      }*/

      boolean run = true;
      // Range of Validity
      if (csv.getValidityRange() == null)
      {
        if (cboxCSVValidityRange.getSelectedItem() != null)
        {
          csv.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(cboxCSVValidityRange.getSelectedItem().getLabel()));
        }
        else
        {
          run = false;
        }
      }
      else
      {
        if (cboxCSVValidityRange.getSelectedItem() != null)
        {
          csv.setValidityRange(ValidityRangeHelper.getValidityRangeIdByName(cboxCSVValidityRange.getSelectedItem().getLabel()));
        }
        else
        {
          csv.setValidityRange(null);
          run = false;
        }
      }

      if (run)
      {

        // Daten setzen mit Convertierung von Date -> XMLGregorianCalendar
        try
        {
          GregorianCalendar c;

          if (dateBoxRD != null && dateBoxRD.getValue() != null)
          {
            c = new GregorianCalendar();
            c.setTimeInMillis(dateBoxRD.getValue().getTime());
            csv.setReleaseDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
          }

          if (dateBoxED != null && dateBoxED.getValue() != null)
          {
            c = new GregorianCalendar();
            c.setTimeInMillis(dateBoxED.getValue().getTime());
            csv.setExpirationDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
          }
        }
        catch (DatatypeConfigurationException ex)
        {
          Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
        }

        try
        {
          MaintainCodeSystemVersionRequestType parameter = new MaintainCodeSystemVersionRequestType();

          // Login, CS, Versioning                       
          parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
          parameter.setCodeSystem(cs);
          parameter.setVersioning(versioning);

          // WS aufruf
          csv.setCodeSystem(null);    // Zirkel entfernen
          MaintainCodeSystemVersionResponse.Return response = WebServiceHelper.maintainCodeSystemVersion(parameter);
          csv.setCodeSystem(cs);      // CS wieder einfügen (falls das mal später gebracht wird)

          // Message über Erfolg/Misserfolg
          if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
          {
            if (parameter.getVersioning().isCreateNewVersion())
              Messagebox.show(Labels.getLabel("popupCodeSystem.newVersionSuccessfullyCreated"));
            else
              Messagebox.show(Labels.getLabel("popupCodeSystem.editVersionChangedSuccessfully"));
            ((ContentCSVSDefault) windowParent).refresh();
            window.detach();
          }
          else
            Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupCodeSystem.versionNotCreated"));
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      else
      {
        Messagebox.show(Labels.getLabel("popupCodeSystem.editCodeSystemValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
      }
    }
    else
    {

      Messagebox.show("Eine CodeSystemVersion mit dem gleichen Namen existiert bereits!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
    }
  }

  @Override
  protected void maintain()
  {
    // eine CSV aussuchen und behalten: Wird allerdings nicht geändert, es muss aber eine CSV für den WS-Aufruf angegeben werden
    CodeSystemVersion csvTemp = cs.getCodeSystemVersions().get(0);
    cs.getCodeSystemVersions().clear();
    cs.getCodeSystemVersions().add(csvTemp);

    try
    {
      MaintainCodeSystemVersionRequestType parameter = new MaintainCodeSystemVersionRequestType();

      // Login, CS, Versioning                       
      parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
      parameter.setCodeSystem(cs);
      parameter.setVersioning(versioning);

      // WS aufruf
      csvTemp.setCodeSystem(null);    // Zirkel entfernen
      MaintainCodeSystemVersionResponse.Return response = WebServiceHelper.maintainCodeSystemVersion(parameter);
      csvTemp.setCodeSystem(cs);      // CS wieder einfügen (falls das mal später gebracht wird)

      // Message über Erfolg/Misserfolg
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        if (parameter.getVersioning().isCreateNewVersion())
          Messagebox.show(Labels.getLabel("popupCodeSystem.newVersionSuccessfullyCreated"));
        else
          Messagebox.show(Labels.getLabel("popupCodeSystem.editVersionChangedSuccessfully"));
        ((ContentCSVSDefault) windowParent).refresh();
        window.detach();
      }
      else
        Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupCodeSystem.versionNotCreated"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  protected void maintainVersionEdit()
  {
    toEdit = true;
    maintainVersionNew();
  }

  @Override
  protected void updateStatus()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected void updateStatusVersion()
  {
    UpdateCodeSystemVersionStatusRequestType parameter = new UpdateCodeSystemVersionStatusRequestType();

    // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
    cs.getCodeSystemVersions().clear();
    cs.getCodeSystemVersions().add(csv);
    parameter.setCodeSystem(cs);

    // WS aufruf
    csv.setCodeSystem(null); // Zirkel entfernen
    UpdateCodeSystemVersionStatusResponse.Return response = WebServiceHelper.updateCodeSystemVersionStatus(parameter);
    csv.setCodeSystem(cs); // VS wieder einfügen (falls das mal später gebracht wird)

    // Medlung über Erfolg/Misserfolg
    try
    {
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        Messagebox.show(Labels.getLabel("popupCodeSystem.editCodeSystemVersionStatusSuccessfully"));
        ((ContentCSVSDefault) windowParent).refresh();
        window.detach();
      }
      else
        Messagebox.show(Labels.getLabel("common.error") + "\n\n" + response.getReturnInfos().getMessage() + "\n\n" + Labels.getLabel("popupCodeSystem.editCodeSystemVersionStatusFailed"));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void onCheck$cbCSVLicenced()
  {
    tbCSVLicenceHolder.setDisabled(!cbCSVLicenced.isChecked());
  }

  public void onCheck$cbNewVersion()
  {
    if (cbNewVersion.isChecked())
    {
      editMode = PopupWindow.EDITMODE_MAINTAIN_VERSION_NEW;
    }
    else
    {
      if (arg.get("CSV") == null)
      {
        editMode = PopupWindow.EDITMODE_MAINTAIN_VERSION_NEW;
        try
        {
          Messagebox.show(Labels.getLabel("popupCodeSystem.editVersionNotAllowedNoVersionSelected"));
        }
        catch (Exception ex)
        {
          Logger.getLogger(PopupCodeSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      else
      {
        editMode = PopupWindow.EDITMODE_MAINTAIN_VERSION_EDIT;
      }
    }
    editMode(editMode);
  }

  public void onClick$bCreate()
  {
    buttonAction();
  }

  public void update(Object o, boolean edited)
  {

  }
}
