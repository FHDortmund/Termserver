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
import de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetResponse;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.MetadataParameter;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class PopupValueSet extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static enum EDITMODES
  {

    NONE, DETAILSONLY, CREATE, MAINTAIN, CREATE_NEW_VERSION
  }

  private ValueSet valueSet = null;
  private ValueSetVersion valueSetVersion = null;

  private EDITMODES editMode;

  private boolean guiCodesystemMinimalVisible;
  private boolean guiCodesystemVersionMinimalVisible;
  private boolean guiCodesystemExpandableVisible;
  private boolean guiCodesystemVersionExpandableVisible;

  private boolean showVersion;

  private IUpdateModal updateListener;

  GenericList genericList;

  public PopupValueSet()
  {
    logger.debug("PopupValueSet()");

    // load from arguments
    valueSet = (ValueSet) ArgumentHelper.getWindowArgument("VS");
    valueSetVersion = (ValueSetVersion) ArgumentHelper.getWindowArgument("VSV");

    showVersion = (valueSetVersion != null);

    editMode = PopupValueSet.EDITMODES.NONE;
    Object o = ArgumentHelper.getWindowArgument("EditMode");
    if (o != null)
    {
      try
      {
        editMode = (PopupValueSet.EDITMODES) o;
      }
      catch (NumberFormatException ex)
      {
        LoggingOutput.outputException(ex, PopupValueSet.class);
      }
    }
    logger.debug("Edit Mode: " + editMode.name());

    initData();
  }

  public void afterCompose()
  {
    logger.debug("afterCompose()");

    if (valueSetVersion == null)
    {
      ComponentHelper.setVisible("gbVersion", false, this);
      valueSetVersion = new ValueSetVersion();
    }

    setWindowTitle();
    showDetailsVisibilty();

    // fill domain values with selected codes
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxStatusVS"), de.fhdo.Definitions.DOMAINID_STATUS,
            valueSet == null || valueSet.getStatus() == null ? "" : "" + valueSet.getStatus());

    if (valueSet.getStatusDate() != null)
      ((Datebox) getFellow("dateBoxStatusVS")).setValue(new Date(valueSet.getStatusDate().toGregorianCalendar().getTimeInMillis()));

    if (showVersion)
    {
      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxCSVValidityRange"), de.fhdo.Definitions.DOMAINID_CODESYSTEMVERSION_VALIDITYRANGE,
              valueSetVersion == null ? "" : "" + valueSetVersion.getValidityRange());

      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxStatusVSV"), de.fhdo.Definitions.DOMAINID_STATUS,
              valueSetVersion == null ? "" : "" + valueSetVersion.getStatus());

      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cboxPreferredLanguage"), de.fhdo.Definitions.DOMAINID_LANGUAGECODES,
              valueSetVersion == null ? "" : valueSetVersion.getPreferredLanguageCd());

      // load data without bindings (dates, ...)
      if (valueSetVersion.getInsertTimestamp() != null)
        ((Datebox) getFellow("dateBoxID")).setValue(new Date(valueSetVersion.getInsertTimestamp().toGregorianCalendar().getTimeInMillis()));
      if (valueSetVersion.getReleaseDate() != null)
        ((Datebox) getFellow("dateBoxRD")).setValue(new Date(valueSetVersion.getReleaseDate().toGregorianCalendar().getTimeInMillis()));

      if (valueSetVersion.getStatusDate() != null)
        ((Datebox) getFellow("dateBoxStatusVSV")).setValue(new Date(valueSetVersion.getStatusDate().toGregorianCalendar().getTimeInMillis()));
    }

    initVVS();
    initListMetadata();

    showComponents();
  }

  private void showComponents()
  {
    logger.debug("showComponents()");

    List<String> ignoreList = new LinkedList<String>();
    ignoreList.add("tabpanelMetaparameter");
    ignoreList.add("buttonExpandCS");
    ignoreList.add("buttonExpandCSV");

    boolean readOnly = (editMode == PopupValueSet.EDITMODES.DETAILSONLY || editMode == PopupValueSet.EDITMODES.NONE);
    ComponentHelper.doDisableAll(getFellow("tabboxFilter"), readOnly, ignoreList);

    logger.debug("version checked: " + (editMode == PopupValueSet.EDITMODES.CREATE_NEW_VERSION || editMode == PopupValueSet.EDITMODES.CREATE ? "true" : "false"));

    ((Checkbox) getFellow("cbNewVersion")).setVisible(editMode == PopupValueSet.EDITMODES.CREATE || editMode == PopupValueSet.EDITMODES.CREATE_NEW_VERSION || editMode == PopupValueSet.EDITMODES.MAINTAIN);
    ((Checkbox) getFellow("cbNewVersion")).setChecked(editMode == PopupValueSet.EDITMODES.CREATE_NEW_VERSION || editMode == PopupValueSet.EDITMODES.CREATE);
    ((Checkbox) getFellow("cbNewVersion")).setDisabled(editMode != PopupValueSet.EDITMODES.MAINTAIN);

    ComponentHelper.setVisible("bCreate", editMode == PopupValueSet.EDITMODES.CREATE || editMode == PopupValueSet.EDITMODES.CREATE_NEW_VERSION || editMode == PopupValueSet.EDITMODES.MAINTAIN, this);
  }

  public void onOkClicked()
  {
    logger.debug("onOkClicked() - save data..., showVersion: " + showVersion);

    if (showVersion)
    {
      // save data without bindings (dates, ...)
      Date date = ((Datebox) getFellow("dateBoxRD")).getValue();
      if (date != null)
        valueSetVersion.setReleaseDate(DateTimeHelper.dateToXMLGregorianCalendar(date));
      else
        valueSetVersion.setReleaseDate(null);

      valueSetVersion.setPreferredLanguageCd(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cboxPreferredLanguage")));
      logger.debug("getPreferredLanguageCd: " + valueSetVersion.getPreferredLanguageCd());

      valueSetVersion.setVirtualCodeSystemVersionId(null);
      Combobox cb = (Combobox) getFellow("cboxVVS");
      if (cb != null && cb.getSelectedItem() != null)
      {
        Object o = cb.getSelectedItem().getValue();
        if (o != null && o instanceof CodeSystemVersion)
        {
          CodeSystemVersion csv = (CodeSystemVersion) o;
          valueSetVersion.setVirtualCodeSystemVersionId(csv.getVersionId());
        }
      }
      
      logger.debug("getVirtualCodeSystemVersionId: " + valueSetVersion.getVirtualCodeSystemVersionId());

      // Range of Validity
      try
      {
        valueSetVersion.setValidityRange(Long.parseLong(DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cboxCSVValidityRange"))));
        logger.debug("getValidityRange: " + valueSetVersion.getValidityRange());
      }
      catch (Exception ex)
      {
      }

      // check mandatory fields
      /*if (valueSetVersion.getValidityRange() <= 0)
      {
        Messagebox.show(Labels.getLabel("popupValueSet.editValueSetValidityRangeFailed"), "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
        return;
      }*/
      if (valueSetVersion.getName() == null || valueSetVersion.getName().length() == 0)
      {
        Messagebox.show(Labels.getLabel("common.mandatoryFields"), Labels.getLabel("common.requiredField"), Messagebox.OK, Messagebox.EXCLAMATION);
        return;
      }
    }

    // check mandatory fields
    if (valueSet.getName() == null || valueSet.getName().length() == 0)
    {
      Messagebox.show(Labels.getLabel("common.mandatoryFields"), Labels.getLabel("common.requiredField"), Messagebox.OK, Messagebox.EXCLAMATION);
      return;
    }

    boolean success = false;

    logger.debug("editMode: " + editMode.name());

    try
    {
      // -> status date can't be updated manually
      switch (editMode)
      {
        case CREATE:
          success = save_Create();
          break;
        case MAINTAIN:
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
        if (editMode == EDITMODES.MAINTAIN || editMode == EDITMODES.CREATE_NEW_VERSION)
        {
          updateListener.update(valueSet, true);
        }
        else if (editMode == EDITMODES.CREATE)
        {
          updateListener.update(valueSet, false);
        }
      }
      this.detach();
    }

  }

  public boolean save_Create()
  {
    logger.debug("save_Create()");

    if (checkIfValueSetExists(valueSet.getName()))
      return false;

    // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
    valueSet.getValueSetVersions().clear();
    valueSet.getValueSetVersions().add(valueSetVersion);

    CreateValueSetRequestType parameter = new CreateValueSetRequestType();

    // Login, cs
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
    parameter.setValueSet(valueSet);

    // WS aufruf
    valueSetVersion.setValueSet(null); // XML Zirkel verhindern
    CreateValueSetResponse.Return response = WebServiceHelper.createValueSet(parameter);
    valueSetVersion.setValueSet(valueSet);  // Nach WS zirkel wiederherstellen

    // Message über Erfolg/Misserfolg                
    if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
    {
//      AssignTermHelper.assignTermToUser(response.getValueSet());
      Messagebox.show(Labels.getLabel("popupValueSet.createValueSetSuccessfully"));
      //((ContentCSVSDefault) this.getParent()).refreshVS(); // TODO nicht schön
      //this.detach();
    }
    else
      Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupValueSet.createValueSetFailed"));

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
    VersioningType versioning = new VersioningType();
    versioning.setCreateNewVersion(createNewVersion);

    logger.debug("createNewVersion: " + createNewVersion);
    if (valueSetVersion == null)
      logger.debug("valueSetVersion ist null");

    if (showVersion)
    {
      // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
      valueSet.getValueSetVersions().clear();
      valueSet.getValueSetVersions().add(valueSetVersion);
    }
    else
    {
      // beliebige Version nehmen, ohne Bedeutung
      ValueSetVersion csvTemp = valueSet.getValueSetVersions().get(0);
      valueSet.getValueSetVersions().clear();
      valueSet.getValueSetVersions().add(csvTemp);
    }

    if (createNewVersion && (valueSetVersion.getVersionId() == null || valueSetVersion.getVersionId() <= 0))
    {
      valueSetVersion.setVersionId(valueSet.getCurrentVersionId());
    }
    logger.debug("cs-versionId: " + valueSetVersion.getVersionId());

    MaintainValueSetRequestType parameter = new MaintainValueSetRequestType();

    // Login, CS, Versioning                       
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());
    parameter.setValueSet(valueSet);
    parameter.setVersioning(versioning);

    // WS aufruf
    valueSetVersion.setValueSet(null);    // Zirkel entfernen
    MaintainValueSetResponse.Return response = WebServiceHelper.maintainValueSet(parameter);
    valueSetVersion.setValueSet(valueSet);      // CS wieder einfügen (falls das mal später gebraucht wird)

    // Message über Erfolg/Misserfolg
    if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
    {
      if (parameter.getVersioning().isCreateNewVersion())
        Messagebox.show(Labels.getLabel("popupValueSet.createValueSetVerisonSuccessfully"));
      else
        Messagebox.show(Labels.getLabel("popupValueSet.editValueSetVerisonSuccessfully"));

      //((ContentCSVSDefault) this.getParent()).refreshVS();
    }
    else
      Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("popupValueSet.createValueSetVerisonFailed"));

    return true;
  }

  public void onCancelClicked()
  {
    this.detach();
  }

  private boolean checkIfValueSetExists(String name)
  {
    logger.debug("checkIfValueSetExists with name: " + name);

    ListValueSetsRequestType request = new ListValueSetsRequestType();
    request.setValueSet(new ValueSet());
    request.getValueSet().setName(name);

    ListValueSetsResponse.Return response = WebServiceHelper.listValueSets(request);
    if (response.getReturnInfos().getStatus() == Status.OK)
    {
      for (ValueSet cs : response.getValueSet())
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
    logger.debug("initData()");

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
    ReturnValueSetDetailsRequestType parameter = new ReturnValueSetDetailsRequestType();

    // Login
    if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    if (valueSet == null || editMode == PopupValueSet.EDITMODES.CREATE)
    {
      // new value set
      valueSet = new ValueSet();
      createNewValuesetVersion();
    }
    else
    {
      // load data from webservice
      logger.debug("VS-ID: " + valueSet.getId());

      // load or init CS und CSV
      ValueSet csTemp = new ValueSet();
      csTemp.setId(valueSet.getId());
      if (valueSetVersion != null)
      {
        ValueSetVersion csvTemp = new ValueSetVersion();
        csTemp.getValueSetVersions().add(csvTemp);
        csvTemp.setVersionId(valueSetVersion.getVersionId());

        logger.debug("VSV-ID: " + valueSetVersion.getVersionId());
      }
      parameter.setValueSet(csTemp);

      ReturnValueSetDetailsResponse.Return response = WebServiceHelper.returnValueSetDetails(parameter);

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
      valueSet = response.getValueSet();
      logger.debug("Valueset geladen, Name: " + valueSet.getName());

      if (editMode == PopupValueSet.EDITMODES.CREATE_NEW_VERSION)
      {
        createNewValuesetVersion();  // hier Felder leer lassen
      }
      else
      {
        // Version laden, falls angegeben       
        if (valueSetVersion != null)
        {
          for (ValueSetVersion csv : valueSet.getValueSetVersions())
          {
            if (csv.getVersionId().equals(valueSetVersion.getVersionId()))
            {
              valueSetVersion = csv;
              break;
            }
          }
        }
      }
    }

  }

  private void initVVS()
  {
    final Combobox cb = (Combobox) getFellow("cboxVVS");

    List<CodeSystem> csList = CodesystemGenericTreeModel.getInstance().getListCS();
    List<CodeSystemVersion> csvList = new LinkedList<CodeSystemVersion>();
    
    CodeSystemVersion csvTemp = new CodeSystemVersion();
    csvTemp.setCodeSystem(new CodeSystem());
    csvTemp.getCodeSystem().setId(0l);
    csvTemp.setVersionId(0l);
    csvList.add(csvTemp); // no selection
    
    for (CodeSystem cs : csList)
    {
      for (CodeSystemVersion csv : cs.getCodeSystemVersions())
      {
        csv.setCodeSystem(cs);
        csvList.add(csv);
      }
    }

    // create renderer
    cb.setItemRenderer(new ComboitemRenderer<CodeSystemVersion>()
    {
      public void render(Comboitem item, CodeSystemVersion csv, int i) throws Exception
      {
        if (csv != null)
        {
          item.setValue(csv);
          if(csv.getName() == null || csv.getCodeSystem().getName() == null)
            item.setLabel("-");
          else item.setLabel(csv.getCodeSystem().getName() + " - " + csv.getName());

          if (valueSetVersion.getVirtualCodeSystemVersionId() != null && valueSetVersion.getVirtualCodeSystemVersionId() > 0
                  && csv.getVersionId() != null && valueSetVersion.getVirtualCodeSystemVersionId().longValue() == csv.getVersionId())
          {
            cb.setSelectedItem(item);
            cb.setText(item.getLabel());
          }
        }
        else
          item.setLabel("");
      }
    });

    // create model
    cb.setModel(new ListModelList<CodeSystemVersion>(csvList));
  }

  private void createNewValuesetVersion()
  {
    logger.debug("createNewValuesetVersion()");

    valueSetVersion = new ValueSetVersion();
    valueSetVersion.setStatus(1);

    DomainValue dvValidity = DomainHelper.getInstance().getDefaultValue(Definitions.DOMAINID_CODESYSTEMVERSION_VALIDITYRANGE);
    valueSetVersion.setValidityRange(dvValidity == null ? 4l : Long.parseLong(dvValidity.getDomainCode()));
  }

  private void setWindowTitle()
  {
    String title;

    switch (editMode)
    {
      case CREATE:
        title = Labels.getLabel("popupValueSet.createValueSet");
        break;
      case DETAILSONLY:
        title = Labels.getLabel("common.valueSet") + " " + Labels.getLabel("common.details");
        break;
      case MAINTAIN:
        title = Labels.getLabel("popupValueSet.editValueSetVersion");
        break;
      case CREATE_NEW_VERSION:
        title = Labels.getLabel("popupValueSet.createValueSetVersion");
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

    // value set
    ComponentHelper.setVisible("rowDescriptionEng", guiCodesystemMinimalVisible, this);
    ComponentHelper.setVisible("rowWebsite", guiCodesystemMinimalVisible, this);
    ComponentHelper.setVisible("rowVSStatus", guiCodesystemMinimalVisible, this);
    ComponentHelper.setVisible("buttonExpandCS", guiCodesystemExpandableVisible, this);

    // value set version
    ComponentHelper.setVisible("rowValidityRange", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowReleasedAt", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowVSVStatus", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowInsertedAt", guiCodesystemVersionMinimalVisible, this);
    ComponentHelper.setVisible("rowPrefLanguage", guiCodesystemVersionMinimalVisible, this);

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

  private void initListMetadata()
  {
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Metadata", 0, "", true, "String", true, true, false, false));  // TODO Lokalisierung
    header.add(new GenericListHeaderType("Parameter-Typ", 130, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Sprache", 100, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Datentyp", 100, "", true, "String", true, true, false, false));

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    for (MetadataParameter meta : valueSet.getMetadataParameters())
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

  /**
   * @return the valueSet
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the valueSet to set
   */
  public void setValueSet(ValueSet valueSet)
  {
    this.valueSet = valueSet;
  }

  /**
   * @return the valueSetVersion
   */
  public ValueSetVersion getValueSetVersion()
  {
    return valueSetVersion;
  }

  /**
   * @param valueSetVersion the valueSetVersion to set
   */
  public void setValueSetVersion(ValueSetVersion valueSetVersion)
  {
    this.valueSetVersion = valueSetVersion;
  }

  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdateModal updateListener)
  {
    this.updateListener = updateListener;
  }

}
