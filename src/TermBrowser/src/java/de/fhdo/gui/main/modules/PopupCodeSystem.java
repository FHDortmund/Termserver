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
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.DateTimeHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.CodesystemGenericTreeModel;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsResponse;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsResponse;
import de.fhdo.terminologie.ws.search.Status;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeCellType;
import de.fhdo.tree.GenericTreeHeaderType;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IUpdateData;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class PopupCodeSystem extends Window implements AfterCompose, IUpdateData
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static enum EDITMODES
  {

    NONE, DETAILSONLY, CREATE, MAINTAIN, CREATE_NEW_VERSION
  }

  private CodeSystem codeSystem = null;
  private CodeSystemVersion codeSystemVersion = null;

  private EDITMODES editMode;

  private boolean guiCodesystemMinimalVisible;
  private boolean guiCodesystemVersionMinimalVisible;
  private boolean guiCodesystemExpandableVisible;
  private boolean guiCodesystemVersionExpandableVisible;

  private boolean showVersion;

  private IUpdateModal updateListener;

  GenericList genericList;
  private GenericTree genericTreeTaxonomy;
  private List<Long> selectedTaxonomyDomainValueIDs = null;

  public PopupCodeSystem()
  {
    logger.debug("PopupCodeSystem()");

    // load from arguments
    codeSystem = (CodeSystem) ArgumentHelper.getWindowArgument("CS");
    codeSystemVersion = (CodeSystemVersion) ArgumentHelper.getWindowArgument("CSV");

    showVersion = (codeSystemVersion != null);

    editMode = EDITMODES.NONE;
    Object o = ArgumentHelper.getWindowArgument("EditMode");
    if (o != null)
    {
      try
      {
        editMode = (PopupCodeSystem.EDITMODES) o;
//        int em = Integer.parseInt(o.toString());
//        logger.debug("Edit Mode: " + em);
//        editMode = EDITMODES.values()[em];
      }
      catch (NumberFormatException ex)
      {
        LoggingOutput.outputException(ex, PopupCodeSystem.class);
      }
    }
    logger.debug("Edit Mode: " + editMode.name());

    /*if(arg.get("EditMode") != null)
     editMode = (Integer)arg.get("EditMode");
     window       = (Window)comp;
     windowParent = (Window)comp.getParent();

     doAfterComposeCustom();
     editMode(editMode);*/
    initData();
  }

  public void afterCompose()
  {
    logger.debug("afterCompose()");

    if (codeSystemVersion == null)
    {
      ComponentHelper.setVisible("gbVersion", false, this);
      codeSystemVersion = new CodeSystemVersion();
    }

    setWindowTitle();
    showDetailsVisibilty();

    // fill domain values with selected codes
    logger.debug("showVersion: " + showVersion);
    if (showVersion)
    {
      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxCSVValidityRange"), de.fhdo.Definitions.DOMAINID_CODESYSTEMVERSION_VALIDITYRANGE,
              codeSystemVersion == null ? "" : "" + codeSystemVersion.getValidityRange());

      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxStatus"), de.fhdo.Definitions.DOMAINID_STATUS,
              codeSystemVersion == null ? "" : "" + codeSystemVersion.getStatus());

      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxPreferredLanguage"), de.fhdo.Definitions.DOMAINID_LANGUAGECODES,
              codeSystemVersion == null ? "" : codeSystemVersion.getPreferredLanguageCd());

      // load data without bindings (dates, ...)
      if (codeSystemVersion.getExpirationDate() != null)
        ((Datebox) getFellow("dateBoxED")).setValue(new Date(codeSystemVersion.getExpirationDate().toGregorianCalendar().getTimeInMillis()));
      if (codeSystemVersion.getReleaseDate() != null)
        ((Datebox) getFellow("dateBoxRD")).setValue(new Date(codeSystemVersion.getReleaseDate().toGregorianCalendar().getTimeInMillis()));
      if (codeSystemVersion.getStatusDate() != null)
        ((Datebox) getFellow("dateBoxSD")).setValue(new Date(codeSystemVersion.getStatusDate().toGregorianCalendar().getTimeInMillis()));
    }

    initListMetadata();
    initTaxonomy();

    showComponents();
  }

  private void showComponents()
  {
    logger.debug("showComponents()");

    List<String> ignoreList = new LinkedList<String>();
    ignoreList.add("tabpanelMetaparameter");
    ignoreList.add("buttonExpandCS");
    ignoreList.add("buttonExpandCSV");

    boolean readOnly = (editMode == EDITMODES.DETAILSONLY || editMode == EDITMODES.NONE);
    ComponentHelper.doDisableAll(getFellow("tabboxFilter"), readOnly, ignoreList);

    logger.debug("version checked: " + (editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.CREATE ? "true" : "false"));

    ((Checkbox) getFellow("cbNewVersion")).setVisible(editMode == EDITMODES.CREATE || editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN);
    ((Checkbox) getFellow("cbNewVersion")).setChecked(editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.CREATE);
    ((Checkbox) getFellow("cbNewVersion")).setDisabled(editMode != EDITMODES.MAINTAIN);

    ComponentHelper.setVisible("bCreate", editMode == EDITMODES.CREATE || editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN, this);

    //((Button)getFellow("bCreate")).setLabel(Labels.getLabel("common.save"));
  }

  public void onOkClicked()
  {
    logger.debug("onOkClicked() - save data...");

    if (showVersion)
    {
      // save data without bindings (dates, ...)
      Date date = ((Datebox) getFellow("dateBoxED")).getValue();
      if (date != null)
        codeSystemVersion.setExpirationDate(DateTimeHelper.dateToXMLGregorianCalendar(date));
      else
        codeSystemVersion.setExpirationDate(null);

      date = ((Datebox) getFellow("dateBoxRD")).getValue();
      if (date != null)
        codeSystemVersion.setReleaseDate(DateTimeHelper.dateToXMLGregorianCalendar(date));
      else
        codeSystemVersion.setReleaseDate(null);

      codeSystemVersion.setPreferredLanguageCd(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cboxPreferredLanguage")));
      logger.debug("getPreferredLanguageCd: " + codeSystemVersion.getPreferredLanguageCd());

      // Range of Validity
      try
      {
        codeSystemVersion.setValidityRange(Long.parseLong(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cboxCSVValidityRange"))));
        logger.debug("getValidityRange: " + codeSystemVersion.getValidityRange());
      }
      catch (Exception ex)
      {
      }

      // check mandatory fields
      if (codeSystemVersion.getValidityRange() <= 0)
        Messagebox.show(Labels.getLabel("popupCodeSystem.editCodeSystemValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
    }

    boolean success = false;

    logger.debug("editMode: " + editMode.name());
    codeSystem.getDomainValues().clear();

    if (selectedTaxonomyDomainValueIDs != null)
    {
      for (Long l : selectedTaxonomyDomainValueIDs)
      {
        DomainValue dv = new DomainValue();
        dv.setDomainValueId(l);
        codeSystem.getDomainValues().add(dv);
      }
    }

    try
    {
      // -> status date can't be updated manually
      switch (editMode)
      {
        case CREATE:
          success = save_Create();
          break;
        case MAINTAIN:
          success = save_MaintainVersion();
          break;
        case CREATE_NEW_VERSION:
          success = save_MaintainVersion();
          break;
      }
    }
    catch (Exception ex)
    {
      Messagebox.show(ex.getLocalizedMessage());
      LoggingOutput.outputException(ex, this);

      success = false;
    }

    if (success)
    {
      if (updateListener != null)
      {
        if(showVersion)
        {
          codeSystem.getCodeSystemVersions().clear();
          codeSystem.getCodeSystemVersions().add(codeSystemVersion);
        }
          
        if (editMode == EDITMODES.MAINTAIN || editMode == EDITMODES.CREATE_NEW_VERSION)
        {
          updateListener.update(codeSystem, true);
        }
        else if (editMode == EDITMODES.CREATE)
        {
          updateListener.update(codeSystem, false);
        }
      }
      this.detach();
    }

  }

  public boolean save_Create()
  {
    logger.debug("save_Create()");

    if (checkIfCodeSystemExists(codeSystem.getName()))
      return false;

    // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
    codeSystem.getCodeSystemVersions().clear();
    codeSystem.getCodeSystemVersions().add(codeSystemVersion);

    CreateCodeSystemRequestType parameter = new CreateCodeSystemRequestType();

    // Login, cs
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
    parameter.setCodeSystem(codeSystem);
    
    parameter.setAssignTaxonomy(selectedTaxonomyDomainValueIDs != null);

    // WS aufruf
    codeSystemVersion.setCodeSystem(null); // XML Zirkel verhindern
    CreateCodeSystemResponse.Return response = WebServiceHelper.createCodeSystem(parameter);
    codeSystemVersion.setCodeSystem(codeSystem);  // Nach WS zirkel wiederherstellen

    // Message über Erfolg/Misserfolg                
    if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
    {
//      AssignTermHelper.assignTermToUser(response.getCodeSystem());
      Messagebox.show(Labels.getLabel("popupCodeSystem.newCodeSystemsuccessfullyCreated"));
      //((ContentCSVSDefault) this.getParent()).refreshCS();  // TODO nicht schön
      //this.detach();
    }
    else
      Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupCodeSystem.codeSystemWasNotCreated"));

    return true;
  }

  /**
   * Edit a code system with version
   *
   * @param createNewVersion
   * @return
   */
  public boolean save_MaintainVersion()
  {
    logger.debug("save_MaintainVersion()");
    boolean createNewVersion = ((Checkbox) getFellow("cbNewVersion")).isChecked();
    boolean copyConcepts = ((Checkbox) getFellow("cbCopyConcepts")).isChecked();
    
    VersioningType versioning = new VersioningType();
    versioning.setCreateNewVersion(createNewVersion);
    versioning.setCopyConcepts(copyConcepts);

    logger.debug("createNewVersion: " + createNewVersion);
    if (codeSystemVersion == null)
      logger.debug("codeSystemVersion ist null");

    if (showVersion)
    {
      // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
      codeSystem.getCodeSystemVersions().clear();
      codeSystem.getCodeSystemVersions().add(codeSystemVersion);
    }
    else
    {
      // beliebige Version nehmen, ohne Bedeutung
      CodeSystemVersion csvTemp = codeSystem.getCodeSystemVersions().get(0);
      codeSystem.getCodeSystemVersions().clear();
      codeSystem.getCodeSystemVersions().add(csvTemp);
    }

    if (createNewVersion && (codeSystemVersion.getVersionId() == null || codeSystemVersion.getVersionId() <= 0))
    {
      codeSystemVersion.setVersionId(codeSystem.getCurrentVersionId());
    }
    logger.debug("cs-versionId: " + codeSystemVersion.getVersionId());

    MaintainCodeSystemVersionRequestType parameter = new MaintainCodeSystemVersionRequestType();

    // Login, CS, Versioning                       
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
    parameter.setCodeSystem(codeSystem);
    parameter.setVersioning(versioning);
    
    parameter.setAssignTaxonomy(selectedTaxonomyDomainValueIDs != null);

    // WS aufruf
    codeSystemVersion.setCodeSystem(null);    // Zirkel entfernen
    MaintainCodeSystemVersionResponse.Return response = WebServiceHelper.maintainCodeSystemVersion(parameter);
    codeSystemVersion.setCodeSystem(codeSystem);      // CS wieder einfügen (falls das mal später gebraucht wird)

    // Message über Erfolg/Misserfolg
    if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
    {
      if (parameter.getVersioning().isCreateNewVersion())
        Messagebox.show(Labels.getLabel("popupCodeSystem.newVersionSuccessfullyCreated"));
      else
        Messagebox.show(Labels.getLabel("popupCodeSystem.editVersionChangedSuccessfully"));

      //((ContentCSVSDefault) this.getParent()).refreshCS();
    }
    else
      Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupCodeSystem.versionNotCreated"));

    return true;
  }

  public void onCancelClicked()
  {
    this.detach();
  }

  private boolean checkIfCodeSystemExists(String name)
  {
    logger.debug("checkIfCodeSystemExists with name: " + name);

    ListCodeSystemsRequestType request = new ListCodeSystemsRequestType();
    request.setCodeSystem(new CodeSystem());
    request.getCodeSystem().setName(name);

    ListCodeSystemsResponse.Return response = WebServiceHelper.listCodeSystems(request);
    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      for (CodeSystem cs : response.getCodeSystem())
      {
        if (cs.getName() == null)
          continue;

        if (cs.getName().equalsIgnoreCase(name))
          return true;
      }
    }
    else
    {
      Messagebox.show(response.getReturnInfos().getMessage());
      return true;
    }

    return false;
  }

  private void initData()
  {
    // Properties
    guiCodesystemMinimalVisible = !PropertiesHelper.getInstance().isGuiCodesystemMinimal();
    guiCodesystemVersionMinimalVisible = !PropertiesHelper.getInstance().isGuiCodesystemVersionMinimal();
    guiCodesystemExpandableVisible = PropertiesHelper.getInstance().isGuiCodesystemExpandable();
    guiCodesystemVersionExpandableVisible = PropertiesHelper.getInstance().isGuiCodesystemVersionExpandable();

    logger.debug("guiCodesystemMinimalVisible: " + guiCodesystemMinimalVisible);
    logger.debug("guiCodesystemVersionMinimalVisible: " + guiCodesystemVersionMinimalVisible);
    logger.debug("guiCodesystemExpandableVisible: " + guiCodesystemExpandableVisible);
    logger.debug("guiCodesystemVersionExpandableVisible: " + guiCodesystemVersionExpandableVisible);

    // load data
    ReturnCodeSystemDetailsRequestType parameter = new ReturnCodeSystemDetailsRequestType();

    // Login
    if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    //window.setTitle(Labels.getLabel("common.codeSystem") + " " + Labels.getLabel("common.details"));
    if (codeSystem == null || editMode == EDITMODES.CREATE)
    {
      // new codesystem
      codeSystem = new CodeSystem();
      createNewCodesystemVersion();
    }
    else
    {
      // load data from webservice
      logger.debug("CS-ID: " + codeSystem.getId());

      // load or init CS und CSV
      CodeSystem csTemp = new CodeSystem();
      csTemp.setId(codeSystem.getId());
      if (codeSystemVersion != null)
      {
        CodeSystemVersion csvTemp = new CodeSystemVersion();
        csTemp.getCodeSystemVersions().add(csvTemp);
        csvTemp.setVersionId(codeSystemVersion.getVersionId());

        logger.debug("CSV-ID: " + codeSystemVersion.getVersionId());
      }
      parameter.setCodeSystem(csTemp);

      ReturnCodeSystemDetailsResponse.Return response = WebServiceHelper.returnCodeSystemDetails(parameter);

      logger.debug("response: " + response.getReturnInfos().getMessage());

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
          LoggingOutput.outputException(ex, this);
        }
      }
      codeSystem = response.getCodeSystem();
      logger.debug("Codesystem geladen, Name: " + codeSystem.getName());

      if (editMode == EDITMODES.CREATE_NEW_VERSION)
      {
        createNewCodesystemVersion();  // hier Felder leer lassen
      }
      else
      {
        // Version laden, falls angegeben       
        if (codeSystemVersion != null)
        {
          for (CodeSystemVersion csv : codeSystem.getCodeSystemVersions())
          {
            if (csv.getVersionId().equals(codeSystemVersion.getVersionId()))
            {
              codeSystemVersion = csv;
              break;
            }
          }
        }
      }
    }

  }

  private void createNewCodesystemVersion()
  {
    logger.debug("createNewCodesystemVersion()");

    codeSystemVersion = new CodeSystemVersion();
    codeSystemVersion.setUnderLicence(Boolean.FALSE);
    //codeSystemVersion.setVersionId(Long.MAX_VALUE);
    codeSystemVersion.setStatus(1);

    DomainValue dvValidity = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_CODESYSTEMVERSION_VALIDITYRANGE);
    codeSystemVersion.setValidityRange(dvValidity == null ? 4l : Long.parseLong(dvValidity.getDomainCode()));
  }

  private void setWindowTitle()
  {
    String title;

    switch (editMode)
    {
      case CREATE:
        title = Labels.getLabel("popupCodeSystem.createCodeSystem");
        break;
      case DETAILSONLY:
        title = Labels.getLabel("common.codeSystem") + " " + Labels.getLabel("common.details");
        break;
      case MAINTAIN:
        title = Labels.getLabel("popupCodeSystem.editCodeSystem");
        break;
//      case MAINTAIN_VERSION_EDIT:
//        title = Labels.getLabel("popupCodeSystem.editCodeSystemVersion");
//        break;
      case CREATE_NEW_VERSION:
        title = Labels.getLabel("popupCodeSystem.createCodeSystemVersion");
        break;

      default:
        //title = Labels.getLabel("common.details");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    this.setTitle(title);
  }

  /**
   * You can customize how much details in the window should be shown. See
   * http://www.wiki.mi.fh-dortmund.de/cts2/index.php?title=Termbrowser.properties
   * for more infos.
   */
  private void showDetailsVisibilty()
  {
    logger.debug("showDetailsVisibilty()");

    // code system
    ComponentHelper.setVisible("rowDescriptionEng", guiCodesystemMinimalVisible, this);
    ComponentHelper.setVisible("buttonExpandCS", guiCodesystemExpandableVisible, this);

    // code system version
    ComponentHelper.setVisible("rowValidityRange", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowDateFrom", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowDateTo", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowCSVStatus", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowCSVPrefLang", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowCSVLicence", guiCodesystemVersionMinimalVisible, this);

    ComponentHelper.setVisible("buttonExpandCSV", guiCodesystemVersionExpandableVisible, this);

    Button buttonExpandCS = (Button) getFellow("buttonExpandCS");
    Button buttonExpandCSV = (Button) getFellow("buttonExpandCSV");

    // Buttons
    if (guiCodesystemMinimalVisible)
      buttonExpandCS.setImage("/rsc/img/symbols/collapse_16x16.png");
    else
      buttonExpandCS.setImage("/rsc/img/symbols/expand_16x16.png");

    if (guiCodesystemVersionMinimalVisible)
      buttonExpandCSV.setImage("/rsc/img/symbols/collapse_16x16.png");
    else
      buttonExpandCSV.setImage("/rsc/img/symbols/expand_16x16.png");
  }

  public void onClickExpandCS()
  {
    logger.debug("Expand CS...");

    guiCodesystemMinimalVisible = !guiCodesystemMinimalVisible;

    showDetailsVisibilty();
    this.invalidate();
  }

  public void onClickExpandCSV()
  {
    logger.debug("Expand CSV...");

    guiCodesystemVersionMinimalVisible = !guiCodesystemVersionMinimalVisible;
    logger.debug("guiCodesystemVersionMinimalVisible: " + guiCodesystemVersionMinimalVisible);

    showDetailsVisibilty();
    this.invalidate();
  }

  private void initTaxonomy()
  {
    if (editMode == EDITMODES.CREATE || editMode == EDITMODES.CREATE_NEW_VERSION
            || editMode == EDITMODES.MAINTAIN)
    {
      // init taxonomy tree for selection
      initDomainValueTree(Definitions.DOMAINID_CODESYSTEM_TAXONOMY);

      getFellow("tabTaxonomy").setVisible(true);
    }
    else
    {
      getFellow("tabTaxonomy").setVisible(false);
    }
  }

  private void initDomainValueTree(long DomainID)
  {
    logger.debug("initDomainValueTree: " + DomainID);

    // Header
    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
    header.add(new GenericTreeHeaderType(Labels.getLabel("common.assigned"), 100, "", true, "Bool", true, false, true));
    header.add(new GenericTreeHeaderType(Labels.getLabel("common.code"), 140, "", true, "String", false, false, false));
    header.add(new GenericTreeHeaderType(Labels.getLabel("common.designation"), 400, "", true, "String", false, false, false));

    // Daten laden
    List<GenericTreeRowType> dataList = new LinkedList<GenericTreeRowType>();
    try
    {
      //String hql = "from DomainValue where domainId=" + DomainID + " order by orderNo,domainDisplay";
      //List<de.fhdo.terminologie.db.hibernate.DomainValue> dvList = hb_session.createQuery(hql).list();

      //for (int i = 0; i < dvList.size(); ++i)
      for (DomainValue domainValue : CodesystemGenericTreeModel.getInstance().getListDV())
      {
        if(domainValue.getDomainValueId() == null || domainValue.getDomainValueId() == 0)
          continue;  // z.B. "Sonstige"
        
        if (domainValue.getDomainValuesForDomainValueId1() == null
                || domainValue.getDomainValuesForDomainValueId1().size() == 0)
        {
          // Nur root-Elemente auf oberster Ebene
          boolean zugeordnet = false;
          if (domainValue.getCodeSystems() != null && codeSystem.getId() != null && codeSystem.getId() > 0)
          {
            for (CodeSystem cs : domainValue.getCodeSystems())
            {
              if (cs.getId().longValue() == codeSystem.getId())
              {
                zugeordnet = true;
                break;
              }
            }
          }
          
          long cs_id = 0;
          if(codeSystem != null && codeSystem.getId() != null)
            cs_id = codeSystem.getId();
          
          GenericTreeRowType row = createTreeRowFromDomainValue(domainValue, zugeordnet, cs_id);

          dataList.add(row);
        }
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incTree");
    Window winGenericTree = (Window) inc.getFellow("winGenericTree");

    genericTreeTaxonomy = (GenericTree) winGenericTree;
    //genericListValues.setUserDefinedId("2");

    //genericTree.setTreeActions(this);
    genericTreeTaxonomy.setUpdateDataListener(this);
    genericTreeTaxonomy.setButton_new(false);
    genericTreeTaxonomy.setButton_edit(false);
    genericTreeTaxonomy.setButton_delete(false);
    genericTreeTaxonomy.setListHeader(header);
    genericTreeTaxonomy.setDataList(dataList);

  }

  private GenericTreeRowType createTreeRowFromDomainValue(DomainValue domainValue, boolean Zugeordnet, long CodesystemId)
  {
    GenericTreeRowType row = new GenericTreeRowType(null);

    GenericTreeCellType[] cells = new GenericTreeCellType[3];
    cells[0] = new GenericTreeCellType(Zugeordnet, false, "");
    cells[1] = new GenericTreeCellType(domainValue.getDomainCode(), false, "");
    cells[2] = new GenericTreeCellType(domainValue.getDomainDisplay(), false, "");

    if (Zugeordnet)
    {
      if (selectedTaxonomyDomainValueIDs == null)
        selectedTaxonomyDomainValueIDs = new LinkedList<Long>();
      selectedTaxonomyDomainValueIDs.add(domainValue.getDomainValueId());
    }

    row.setData(domainValue);
    row.setCells(cells);

    if (domainValue.getDomainValuesForDomainValueId2() != null)
    {
      for (DomainValue dvChild : domainValue.getDomainValuesForDomainValueId2())
      {
        boolean zugeordnet = false;
        if (dvChild.getCodeSystems() != null && codeSystem.getId() != null && codeSystem.getId() > 0)
        {
          for (CodeSystem cs : dvChild.getCodeSystems())
          {
            if (cs.getId().longValue() == CodesystemId)
            {

              zugeordnet = true;
              break;
            }
          }

        }

        row.getChildRows().add(createTreeRowFromDomainValue(dvChild, zugeordnet, CodesystemId));
      }
    }

    return row;
  }

  private void initListMetadata()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("common.metadata"), 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.parameterType"), 130, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.language"), 100, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("common.datatype"), 100, "", true, "String", true, true, false, false));

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    for (MetadataParameter meta : codeSystem.getMetadataParameters())
    {
      GenericListRowType row = createRowFromMetadataParameter(meta);
      dataList.add(row);
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    //genericList.setUserDefinedId("1");
    //genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);

  }

  private GenericListRowType createRowFromMetadataParameter(MetadataParameter meta)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[4];
    cells[0] = new GenericListCellType(meta.getParamName(), false, "");
    cells[1] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_METADATA_PARAMETER_TYPE, meta.getMetadataParameterType()), false, "");
    cells[2] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_LANGUAGECODES, meta.getLanguageCd()), false, "");
    cells[3] = new GenericListCellType(meta.getParamDatatype(), false, "");

    row.setData(meta);
    row.setCells(cells);

    return row;
  }

  public void onCellUpdated(int cellIndex, Object data, GenericTreeRowType row)
  {
    logger.debug("onCellUpdated, index: " + cellIndex + ", data: " + data);

    Boolean assigned = (Boolean) data;

    DomainValue dv = (DomainValue) row.getData();
    logger.debug("dvid: " + dv.getDomainValueId());

    if (selectedTaxonomyDomainValueIDs == null)
      selectedTaxonomyDomainValueIDs = new LinkedList<Long>();

    if (assigned)
    {
      selectedTaxonomyDomainValueIDs.add(dv.getDomainValueId());
    }
    else
    {
      if (selectedTaxonomyDomainValueIDs.contains(dv.getDomainValueId()))
        selectedTaxonomyDomainValueIDs.remove(dv.getDomainValueId());
    }

    logger.debug("SelectedTaxonomyDomainValueIDs:");
    for (Long l : selectedTaxonomyDomainValueIDs)
    {
      logger.debug(l);
    }
  }

  /**
   * @return the codeSystem
   */
  public CodeSystem getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystem the codeSystem to set
   */
  public void setCodeSystem(CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }

  /**
   * @return the codeSystemVersion
   */
  public CodeSystemVersion getCodeSystemVersion()
  {
    return codeSystemVersion;
  }

  /**
   * @param codeSystemVersion the codeSystemVersion to set
   */
  public void setCodeSystemVersion(CodeSystemVersion codeSystemVersion)
  {
    this.codeSystemVersion = codeSystemVersion;
  }

  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdateModal updateListener)
  {
    this.updateListener = updateListener;
  }
}
