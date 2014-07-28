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

import de.fhdo.gui.main.modules.details.LanguageDetails;
import de.fhdo.helper.LanguageHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.models.comparators.ComparatorTranslations;
import de.fhdo.models.itemrenderer.ListitemRendererTranslations;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
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
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;

/**
 *
 * @author Philipp Urbauer
 */
public class TranslationComposer extends SelectorComposer<Tabpanel> implements IUpdate
{

  private List<CodeSystemConceptTranslation> codeSystemConceptTranslations = null;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  @Wire
  public Listbox listTranslations;
  @Wire
  public Button bTranslationNew;
  @Wire
  public Button bTranslationChange;
  @Wire
  public Button bTranslationDelete;

  private CodeSystemEntity cse = null;
  private CodeSystemEntityVersion csev = null;
  private CodeSystemVersionEntityMembership csvem = null;

  private void loadTranslations()
  {
    List translations = new ArrayList();
    for (CodeSystemConceptTranslation csct : codeSystemConceptTranslations)
    {
      translations.add(csct);
    }

    Listheader lh1 = new Listheader(Labels.getLabel("common.translation")),
            lh2 = new Listheader(Labels.getLabel("common.value"));
    lh1.setSortAscending(new ComparatorTranslations(true));
    lh1.setSortDescending(new ComparatorTranslations(false));

    listTranslations.setItemRenderer(new ListitemRendererTranslations(true));
    listTranslations.setModel(new SimpleListModel(translations));
    lh1.sort(true);
  }

  @Listen("onSelect=#listTranslations")
  public void onSelectListTranslations()
  {

    if (!listTranslations.isDisabled())
    {

      bTranslationDelete.setDisabled(false);
      bTranslationChange.setDisabled(false);
      Listitem selectedItem = listTranslations.getSelectedItem();

      for (Listitem item : listTranslations.getItems())
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

  private void loadCodeSystemConceptTranslations()
  {

    // Metadaten und Übersetzungen laden
    ReturnConceptDetailsRequestType parameter = new ReturnConceptDetailsRequestType();
    cse = (CodeSystemEntity) listTranslations.getAttribute("cse");
    csev = (CodeSystemEntityVersion) listTranslations.getAttribute("csev");
    csvem = (CodeSystemVersionEntityMembership) listTranslations.getAttribute("csvem");

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

    codeSystemConceptTranslations = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations();
    for (CodeSystemConceptTranslation csct : codeSystemConceptTranslations)
    {
      csct.setCodeSystemConcept(response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0));
    }
  }

  @Listen("onClick=#bTranslationNew")
  public void newTranslation()
  {

    try
    {

      Window win = (Window) Executions.createComponents(
              "/gui/main/modules/details/languageDetails.zul", null, null);

      ((LanguageDetails) win).setUpdateListInterface(this);

      win.doModal();

    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der MetadatenDetails: " + ex.getLocalizedMessage());
    }
  }

  @Listen("onClick=#bTranslationChange")
  public void changeTranslation()
  {

    if (codeSystemConceptTranslations == null)
      loadCodeSystemConceptTranslations();

    Authoring port_authoring = new Authoring_Service().getAuthoringPort();
    // Login
    MaintainConceptRequestType parameter = new MaintainConceptRequestType();
    parameter.setLoginToken(SessionHelper.getSessionId());
    parameter.setCodeSystemVersionId(csvem.getId().getCodeSystemVersionId());

    // Versioning 
    VersioningType versioning = new VersioningType();
    versioning.setCreateNewVersion(false);
    parameter.setVersioning(versioning);

    // CSE
    parameter.setCodeSystemEntity(cse);

    //CSEV    
    csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
    csev.getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations().clear();

    List<Listitem> itemList = listTranslations.getItems();
    for (Listitem item : itemList)
    {

      List<Component> list = item.getChildren();
      String language = ((Listcell) list.get(0)).getLabel();
      String value = ((Textbox) list.get(1).getLastChild()).getText();
      Long codeSystemEntityVersionId = csev.getVersionId();
      Iterator<CodeSystemConceptTranslation> iter = codeSystemConceptTranslations.iterator();
      while (iter.hasNext())
      {
        CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) iter.next();
        if (((Long) (csct.getCodeSystemConcept().getCodeSystemEntityVersionId())).equals(codeSystemEntityVersionId))
        {
          /*TODO if ((LanguageHelper.getLanguageTable().get(String.valueOf(csct.getLanguageId()))).equals(language))
          {

            CodeSystemConceptTranslation csct_new = new CodeSystemConceptTranslation();

            if (csct.getId() != null)
              csct_new.setId(csct.getId());

            csct_new.setLanguageId(csct.getLanguageId());
            if (csct.getDescription() != null)
              csct_new.setDescription(csct.getDescription());
            if (csct.getTerm() != null)
              csct_new.setTerm(value);
            if (csct.getTermAbbrevation() != null)
              csct_new.setTermAbbrevation(csct.getTermAbbrevation());
                        //csct_new.setCodeSystemConcept(null);

            csev.getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations().add(csct_new);
          }*/
        }
      }
    }

    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

    de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType resp = port_authoring.maintainConcept(parameter);

    if (resp.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
    {
      Messagebox.show(Labels.getLabel("common.error"), "Information", Messagebox.OK, Messagebox.INFORMATION);

    }
    else
    {

      // Metadaten und Übersetzungen laden
      ReturnConceptDetailsRequestType param = new ReturnConceptDetailsRequestType();
      param.setCodeSystemEntity(cse);
      param.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
      param.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
      //CSE aus CSEV entfernen, sonst inf,loop
      csev.setCodeSystemEntity(null);

      ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(param);

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

      //finally reload MetadataList
      codeSystemConceptTranslations = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations();
      for (CodeSystemConceptTranslation csct : codeSystemConceptTranslations)
      {
        csct.setCodeSystemConcept(response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0));
      }
      loadTranslations();

      Messagebox.show(Labels.getLabel("popupConcept.translationChangeSuccess"), "Information", Messagebox.OK, Messagebox.INFORMATION);
    }
  }

  @Listen("onClick=#bTranslationDelete")
  public void deleteTranslation()
  {
    if (codeSystemConceptTranslations == null)
      loadCodeSystemConceptTranslations();

    Authoring port_authoring = new Authoring_Service().getAuthoringPort();
    // Login
    MaintainConceptRequestType parameter = new MaintainConceptRequestType();
    parameter.setLoginToken(SessionHelper.getSessionId());
    parameter.setCodeSystemVersionId(csvem.getId().getCodeSystemVersionId());

    // Versioning 
    VersioningType versioning = new VersioningType();
    versioning.setCreateNewVersion(false);
    parameter.setVersioning(versioning);

    // CSE
    parameter.setCodeSystemEntity(cse);

    //CSEV    
    csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
    csev.getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations().clear();

    Listitem item = listTranslations.getSelectedItem();
    List<Component> list = item.getChildren();
    String language = ((Listcell) list.get(0)).getLabel();
    String value = ((Textbox) list.get(1).getLastChild()).getText();

    Long codeSystemEntityVersionId = csev.getVersionId();
    Iterator<CodeSystemConceptTranslation> iter = codeSystemConceptTranslations.iterator();
    while (iter.hasNext())
    {
      CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) iter.next();
      if (((Long) (csct.getCodeSystemConcept().getCodeSystemEntityVersionId())).equals(codeSystemEntityVersionId))
      {
        // TODO if ((LanguageHelper.getLanguageTable().get(String.valueOf(csct.getLanguageId()))).equals(language))
        {

          CodeSystemConceptTranslation csct_new = new CodeSystemConceptTranslation();

          if (csct.getId() != null)
            csct_new.setId(csct.getId());

          csct_new.setLanguageCd("");
          csct_new.setDescription(null);
          csct_new.setTerm(null);
          csct_new.setTermAbbrevation(null);
          csct_new.setCodeSystemConcept(null);

          csev.getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations().add(csct_new);
        }
      }
    }

    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
    parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

    de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType resp = port_authoring.maintainConcept(parameter);

    if (resp.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
    {
      Messagebox.show(Labels.getLabel("common.error"), "Information", Messagebox.OK, Messagebox.INFORMATION);

    }
    else
    {

      // Metadaten und Übersetzungen laden
      ReturnConceptDetailsRequestType param = new ReturnConceptDetailsRequestType();
      param.setCodeSystemEntity(cse);
      param.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
      param.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
      //CSE aus CSEV entfernen, sonst inf,loop
      csev.setCodeSystemEntity(null);

      ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(param);

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

      //finally reload MetadataList
      codeSystemConceptTranslations = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations();
      for (CodeSystemConceptTranslation csct : codeSystemConceptTranslations)
      {
        csct.setCodeSystemConcept(response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0));
      }
      loadTranslations();

      Messagebox.show(Labels.getLabel("popupConcept.translationDeleteSuccess"), "Information", Messagebox.OK, Messagebox.INFORMATION);
    }
  }

  public void update(Object o)
  {

    if (o instanceof CodeSystemConceptTranslation)
    {
      CodeSystemConceptTranslation csct_new = (CodeSystemConceptTranslation) o;

      if (codeSystemConceptTranslations == null)
        loadCodeSystemConceptTranslations();

      Authoring port_authoring = new Authoring_Service().getAuthoringPort();
      // Login
      MaintainConceptRequestType parameter = new MaintainConceptRequestType();
      parameter.setLoginToken(SessionHelper.getSessionId());
      parameter.setCodeSystemVersionId(csvem.getId().getCodeSystemVersionId());

      // Versioning 
      VersioningType versioning = new VersioningType();
      versioning.setCreateNewVersion(false);
      parameter.setVersioning(versioning);

      // CSE
      parameter.setCodeSystemEntity(cse);

      //CSEV    
      csev.setCodeSystemEntity(null);     //CSE aus CSEV entfernen, sonst inf,loop
      csev.getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations().clear();
      csev.getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations().add(csct_new);

      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

      de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType resp = port_authoring.maintainConcept(parameter);

      if (resp.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        Messagebox.show(Labels.getLabel("common.error"), "Information", Messagebox.OK, Messagebox.INFORMATION);

      }
      else
      {

        // Metadaten und Übersetzungen laden
        ReturnConceptDetailsRequestType param = new ReturnConceptDetailsRequestType();
        param.setCodeSystemEntity(cse);
        param.getCodeSystemEntity().getCodeSystemEntityVersions().clear();
        param.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
        //CSE aus CSEV entfernen, sonst inf,loop
        csev.setCodeSystemEntity(null);

        ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(param);

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

        //finally reload MetadataList
        codeSystemConceptTranslations = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0).getCodeSystemConceptTranslations();
        for (CodeSystemConceptTranslation csct : codeSystemConceptTranslations)
        {
          csct.setCodeSystemConcept(response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getCodeSystemConcepts().get(0));
        }
        loadTranslations();

        Messagebox.show(Labels.getLabel("popupConcept.translationNewSuccess"), "Information", Messagebox.OK, Messagebox.INFORMATION);
      }
    }
  }
}
