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

import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.comparators.ComparatorCsMetadata;
import de.fhdo.models.comparators.ComparatorVsMetadata;
import de.fhdo.models.itemrenderer.ListitemRendererCsMetadataList;
import de.fhdo.models.itemrenderer.ListitemRendererVsMetadataList;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemMetadataValue;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.ValueSetMetadataValue;

/**
 *
 * @author Philipp Urbauer
 */
public class MetaDataComposer extends SelectorComposer<Tabpanel>
{

  private List<ValueSetMetadataValue> valueSetMetadataValue = null;
  private List<CodeSystemMetadataValue> codeSystemMetadataValue = null;

  @Wire
  public Listbox listMetadata;
  @Wire
  public Button bMetaParaChange;

  private void loadVsMetadata()
  {
    List metadata = new ArrayList();
    for (ValueSetMetadataValue vsmdv : valueSetMetadataValue)
    {
      metadata.add(vsmdv);
    }

    Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
            lh2 = new Listheader(Labels.getLabel("common.value"));
    lh1.setSortAscending(new ComparatorVsMetadata(true));
    lh1.setSortDescending(new ComparatorVsMetadata(false));

    listMetadata.setItemRenderer(new ListitemRendererVsMetadataList(true));
    listMetadata.setModel(new SimpleListModel(metadata));
    lh1.sort(true);
  }

  private void loadCsMetadata()
  {
    List metadata = new ArrayList();
    for (CodeSystemMetadataValue csmdv : codeSystemMetadataValue)
    {
      metadata.add(csmdv);
    }

    Listheader lh1 = new Listheader(Labels.getLabel("common.metadata")),
            lh2 = new Listheader(Labels.getLabel("common.value"));
    lh1.setSortAscending(new ComparatorCsMetadata(true));
    lh1.setSortDescending(new ComparatorCsMetadata(false));

    listMetadata.setItemRenderer(new ListitemRendererCsMetadataList(true));
    listMetadata.setModel(new SimpleListModel(metadata));
    lh1.sort(true);
  }

  @Listen("onSelect=#listMetadata")
  public void onSelectListMetadata()
  {

    if (!listMetadata.isDisabled())
    {
      Listitem selectedItem = listMetadata.getSelectedItem();

      for (Listitem item : listMetadata.getItems())
      {
        for (Component c : item.getChildren())
        {
          for (Component c1 : c.getChildren())
          {
            if (c1 instanceof Textbox)
            {
              if (item.equals(selectedItem))
              {
                ((Textbox) c1).setReadonly(false);
              }
              else
              {
                ((Textbox) c1).setReadonly(true);
              }
            }
          }
        }
      }
    }
  }

  private void loadValueSetMetadataValue()
  {

    //Search port_Search = new Search_Service().getSearchPort();
    ReturnValueSetConceptMetadataRequestType para = new ReturnValueSetConceptMetadataRequestType();
    para.setCodeSystemEntityVersionId((Long) listMetadata.getAttribute("codeSystemEntityVersionId"));
    para.setValuesetVersionId((Long) listMetadata.getAttribute("valuesetVersionId"));
    ReturnValueSetConceptMetadataResponse.Return resp = WebServiceHelper.returnValueSetConceptMetadata(para);
    valueSetMetadataValue = resp.getValueSetMetadataValue();
  }

  private void loadCodeSystemMetadataValue()
  {

    // Metadaten und Übersetzungen laden
    ReturnConceptDetailsRequestType parameter = new ReturnConceptDetailsRequestType();
    CodeSystemEntity cse = (CodeSystemEntity) listMetadata.getAttribute("cse");
    CodeSystemEntityVersion csev = (CodeSystemEntityVersion) listMetadata.getAttribute("csev");
    CodeSystemVersionEntityMembership csvem = (CodeSystemVersionEntityMembership) listMetadata.getAttribute("csvem");

    parameter.setCodeSystemEntity(cse);
    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
    //CSE aus CSEV entfernen, sonst inf,loop
    csev.setCodeSystemEntity(null);

    ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(parameter);

    // keine csev zurückgekommen (wegen möglicher Fehler beim WS)
    if (response.getCodeSystemEntity() == null)
      return;

    // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)
    csev.setCodeSystemEntity(cse);

    // CodeSystemVersionEntityMembership nachladen
    if (response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().isEmpty() == false)
    {
      csvem = response.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0);
      cse.getCodeSystemVersionEntityMemberships().clear();
      cse.getCodeSystemVersionEntityMemberships().add(csvem);
    }
    codeSystemMetadataValue = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemMetadataValues();
    for (CodeSystemMetadataValue csmv : codeSystemMetadataValue)
    {
      csmv.setCodeSystemEntityVersion(response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0));
    }

  }

  @Listen("onClick=#bMetaParaChange")
  public void changeMetaPara()
  {

    int contentMode = (Integer) listMetadata.getAttribute("contentMode");
    Long versionId = (Long) listMetadata.getAttribute("versionId");

    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {

      if (valueSetMetadataValue == null)
        loadValueSetMetadataValue();

      Authoring port_authoring = new Authoring_Service().getAuthoringPort();
      // Login
      MaintainValueSetConceptMetadataValueRequestType parameter = new MaintainValueSetConceptMetadataValueRequestType();
      parameter.setLoginToken(SessionHelper.getSessionId());

      List<Listitem> itemList = listMetadata.getItems();
      for (Listitem item : itemList)
      {

        List<Component> list = item.getChildren();
        String paramName = ((Listcell) list.get(0)).getLabel();
        String value = ((Textbox) list.get(1).getLastChild()).getText();
        Long valuesetVersionId = (Long) listMetadata.getAttribute("valuesetVersionId");
        Long codeSystemEntityVersionId = (Long) listMetadata.getAttribute("codeSystemEntityVersionId");
        Iterator<ValueSetMetadataValue> iter = valueSetMetadataValue.iterator();
        while (iter.hasNext())
        {
          ValueSetMetadataValue vsmv = (ValueSetMetadataValue) iter.next();
          if (vsmv.getCodeSystemEntityVersion().getVersionId().equals(codeSystemEntityVersionId) && vsmv.getValuesetVersionId().equals(valuesetVersionId))
          {
            if ((vsmv.getMetadataParameter().getParamName()).equals(paramName))
            {
              vsmv.setParameterValue(value);
              parameter.getValueSetMetadataValues().add(vsmv);
            }
          }
        }
      }

      de.fhdo.terminologie.ws.authoring.MaintainValueSetConceptMetadataValueResponse.Return resp = port_authoring.maintainValueSetConceptMetadataValue(parameter);
      //finally reload MetadataList
      valueSetMetadataValue = resp.getValueSetMetadataValues();
      loadVsMetadata();
      if (resp.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        Messagebox.show(Labels.getLabel("common.error"), "Information", Messagebox.OK, Messagebox.INFORMATION);
      }
      else
      {
        Messagebox.show(Labels.getLabel("popupConcept.metadataChangeSuccess"), "Information", Messagebox.OK, Messagebox.INFORMATION);
      }
    }

    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {

      if (codeSystemMetadataValue == null)
        loadCodeSystemMetadataValue();

      Authoring port_authoring = new Authoring_Service().getAuthoringPort();
      // Login
      MaintainCodeSystemConceptMetadataValueRequestType parameter = new MaintainCodeSystemConceptMetadataValueRequestType();
      parameter.setLoginToken(SessionHelper.getSessionId());
      parameter.setCodeSystemVersionId(versionId);

      List<Listitem> itemList = listMetadata.getItems();
      for (Listitem item : itemList)
      {

        List<Component> list = item.getChildren();
        String paramName = ((Listcell) list.get(0)).getLabel();
        String value = ((Textbox) list.get(1).getLastChild()).getText();
        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) listMetadata.getAttribute("csev");
        Long codeSystemEntityVersionId = csev.getVersionId();
        Iterator<CodeSystemMetadataValue> iter = codeSystemMetadataValue.iterator();
        while (iter.hasNext())
        {
          CodeSystemMetadataValue csmv = (CodeSystemMetadataValue) iter.next();
          if ((csmv.getCodeSystemEntityVersion().getVersionId()).equals(codeSystemEntityVersionId))
          {
            if ((csmv.getMetadataParameter().getParamName()).equals(paramName))
            {
              csmv.setParameterValue(value);
              CodeSystemEntityVersion version = new CodeSystemEntityVersion();
              version.setVersionId(csmv.getCodeSystemEntityVersion().getVersionId());
              csmv.setCodeSystemEntityVersion(version);
              parameter.getCodeSystemMetadataValues().add(csmv);
            }
          }
        }
      }

      de.fhdo.terminologie.ws.authoring.MaintainCodeSystemConceptMetadataValueResponse.Return resp = port_authoring.maintainCodeSystemConceptMetadataValue(parameter);
      //finally reload MetadataList
      codeSystemMetadataValue = resp.getCodeSystemMetadataValues();
      loadCsMetadata();
      if (resp.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        Messagebox.show(Labels.getLabel("common.error"), "Information", Messagebox.OK, Messagebox.INFORMATION);
      }
      else
      {
        Messagebox.show(Labels.getLabel("popupConcept.metadataChangeSuccess"), "Information", Messagebox.OK, Messagebox.INFORMATION);
      }

    }
  }
}
