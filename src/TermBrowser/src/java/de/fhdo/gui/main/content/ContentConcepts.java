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
package de.fhdo.gui.main.content;

import de.fhdo.gui.main.modules.PopupConcept;
import de.fhdo.helper.ComponentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.CodesystemGenericTreeModel;
import de.fhdo.models.comparators.ComparatorCodesystemVersions;
import de.fhdo.terminologie.ws.authoring.DeleteInfo;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.Type;
import java.util.Collections;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ContentConcepts extends Window implements AfterCompose
{

  protected static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private CodeSystem codeSystem;
  private CodeSystemVersion codeSystemVersion;
  private ValueSet valueSet;
  private ValueSetVersion valueSetVersion;

  private ConceptsTree concepts = null;

  public ContentConcepts()
  {
    logger.debug("ContentConcepts() - Konstruktor");

    // loading dynamic parameters
    codeSystem = (CodeSystem) Executions.getCurrent().getAttribute("codeSystem");
    valueSet = (ValueSet) Executions.getCurrent().getAttribute("valueSet");

    codeSystemVersion = null;
    valueSetVersion = null;

    if (codeSystem != null)
    {
      logger.debug("Codesystem given with id: " + codeSystem.getId());
      logger.debug("Count versions: " + codeSystem.getCodeSystemVersions().size());

      Object o = SessionHelper.getValue("selectedCSV");
      if (o != null)
      {
        codeSystemVersion = (CodeSystemVersion) o;

        if (codeSystemVersion.getCodeSystem() == null)
          codeSystemVersion = null;
        else
        {

          if (codeSystemVersion != null && codeSystemVersion.getCodeSystem().getId().longValue() != codeSystem.getId())
          {
            // wrong code system
            codeSystemVersion = null;
          }
          else
          {
            logger.debug("Version given with id: " + codeSystemVersion.getVersionId());
          }
        }
      }

      if (codeSystemVersion == null)
      {
        // load default version
        for (CodeSystemVersion csv : codeSystem.getCodeSystemVersions())
        {
          if (csv.getVersionId().longValue() == codeSystem.getCurrentVersionId().longValue())
          {
            codeSystemVersion = csv;
            logger.debug("Version given with default id: " + codeSystemVersion.getVersionId());
            break;
          }
        }
      }

    }

    if (valueSet != null)
    {
      logger.debug("ValueSet given with id: " + valueSet.getId());
      logger.debug("Count versions: " + valueSet.getValueSetVersions().size());

      Object o = SessionHelper.getValue("selectedVSV");
      if (o != null)
      {
        valueSetVersion = (ValueSetVersion) o;

        if (valueSetVersion.getValueSet() == null)
        {
          valueSetVersion = null;
        }
        else
        {

          if (valueSetVersion != null && valueSetVersion.getValueSet().getId().longValue() != valueSet.getId())
          {
            // wrong value set
            valueSetVersion = null;
          }
          else
          {
            logger.debug("Version given with id: " + valueSetVersion.getVersionId());
          }
        }
      }

      if (valueSetVersion == null)
      {
        // load default version
        for (ValueSetVersion vsv : valueSet.getValueSetVersions())
        {
          if (vsv.getVersionId().longValue() == valueSet.getCurrentVersionId().longValue())
          {
            valueSetVersion = vsv;
            logger.debug("Version given with id: " + valueSetVersion.getVersionId());
            break;
          }
        }
      }
    }
  }

  public void afterCompose()
  {
    logger.debug("ContentConcepts - afterCompose()");

    fillVersionList();
    loadConcepts();

    showButtons();

    Clients.clearBusy();
  }

  private void fillVersionList()
  {
    logger.debug("fillVersionList()");

    final Combobox cbVersions = (Combobox) getFellow("cbVersion");
    long selectedVersionId = 0;

    if (codeSystem != null)
    {
      List<CodeSystemVersion> list = codeSystem.getCodeSystemVersions();
      Collections.sort(list, new ComparatorCodesystemVersions(true));

      cbVersions.setModel(new ListModelList<CodeSystemVersion>(list));
      if (codeSystemVersion != null)
        selectedVersionId = codeSystemVersion.getVersionId();
    }
    else if (valueSet != null)
    {
      cbVersions.setModel(new ListModelList<ValueSetVersion>(valueSet.getValueSetVersions()));
      if (valueSetVersion != null)
        selectedVersionId = valueSetVersion.getVersionId();
    }

    final long selectedVersionIdFinal = selectedVersionId;

    cbVersions.setItemRenderer(new ComboitemRenderer()
    {
      public void render(Comboitem item, Object o, int i) throws Exception
      {
        if (o != null)
        {
          if (o instanceof CodeSystemVersion)
          {
            CodeSystemVersion csv = (CodeSystemVersion) o;
            item.setLabel(csv.getName());

            if (csv.getVersionId().longValue() == selectedVersionIdFinal)
            {
              cbVersions.setSelectedItem(item);
              cbVersions.setText(item.getLabel());
            }
          }
          else if (o instanceof ValueSetVersion)
          {
            ValueSetVersion vsv = (ValueSetVersion) o;
            item.setLabel(vsv.getName());

            if (vsv.getVersionId().longValue() == selectedVersionIdFinal)
            {
              cbVersions.setSelectedItem(item);
              cbVersions.setText(item.getLabel());
            }
          }
          item.setValue(o);
        }
        else
          item.setLabel("");
      }
    });

  }

  public void onVersionChanged()
  {
    logger.debug("onVersionChanged()");

    Combobox cbVersions = (Combobox) getFellow("cbVersion");
    Object o = cbVersions.getSelectedItem().getValue();

    if (o instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) o;
      SessionHelper.setValue("selectedCSV", csv);
      SessionHelper.setValue("selectedVSV", null);

      logger.debug("CSV selected: " + csv.getVersionId());
    }
    else if (o instanceof ValueSetVersion)
    {
      ValueSetVersion vsv = (ValueSetVersion) o;
      SessionHelper.setValue("selectedVSV", vsv);
      SessionHelper.setValue("selectedCSV", null);

      logger.debug("VSV selected: " + vsv.getVersionId());
    }

    loadConcepts();
  }

  private void loadConcepts()
  {
    logger.debug("loadConcepts()");

    Tree treeConcepts = (Tree) getFellow("treeConcepts");

    //if(concepts == null)
    concepts = new ConceptsTree(treeConcepts, this);

    if (codeSystemVersion != null)
      concepts.setCodeSystemVersionId(codeSystemVersion.getVersionId());
    else if (valueSetVersion != null)
      concepts.setValueSetVersionId(valueSetVersion.getVersionId());

    concepts.initData();

  }

  public void onConceptSelect()
  {
    concepts.onConceptSelect(false, true);
    showButtons();
  }

  public void onNewClicked()
  {
    if (SessionHelper.isUserLoggedIn())
      concepts.createConcept(PopupConcept.HIERARCHYMODE.ROOT, 0);
  }

  public void onNewSubClicked()
  {
    if (SessionHelper.isUserLoggedIn())
    {
      CodeSystemEntityVersion csev = concepts.getSelection();
      if (csev != null)
        concepts.createConcept(PopupConcept.HIERARCHYMODE.SUB, csev.getVersionId());
    }
  }

  public void onEditClicked()
  {
    if (SessionHelper.isUserLoggedIn())
      concepts.maintainConcept();
  }

  public void onDetailsClicked()
  {
    concepts.openConceptDetails();
  }

  public void onDeleteClicked()
  {
    if (SessionHelper.isUserLoggedIn())
    {
      if (codeSystem != null && codeSystemVersion != null)
      {
        logger.debug("onDeleteClicked()");
        
        if (Messagebox.show(Labels.getLabel("common.deleteCSVersion"), Labels.getLabel("common.deleteSystem"), Messagebox.YES | Messagebox.NO, Messagebox.QUESTION) 
                == Messagebox.YES)
        {
          logger.debug("deleting...");
          RemoveTerminologyOrConceptRequestType request = new RemoveTerminologyOrConceptRequestType();
          request.setLoginToken(SessionHelper.getSessionId());
          request.setDeleteInfo(new DeleteInfo());
          CodeSystem cs = new CodeSystem();
          cs.setId(codeSystem.getId());
          CodeSystemVersion csv = new CodeSystemVersion();
          csv.setVersionId(codeSystemVersion.getVersionId());
          cs.getCodeSystemVersions().add(csv);
          request.getDeleteInfo().setCodeSystem(cs);
          request.getDeleteInfo().setType(Type.CODE_SYSTEM_VERSION);
          
          RemoveTerminologyOrConceptResponseType response = WebServiceHelper.removeTerminologyOrConcept(request);
          
          Messagebox.show(response.getReturnInfos().getMessage());
          
          CodesystemGenericTreeModel.getInstance().reloadData();
          Executions.sendRedirect("");  // reload page
        }
        else
        {
          logger.debug("not deleting...");
        }
      }
    }
  }

  public void onPaging(Event event)
  {
    concepts.onPaging((PagingEvent) event);
  }

  private void showButtons()
  {
    CodeSystemEntityVersion csev = concepts.getSelection();
    ((Button) getFellow("buttonDetails")).setDisabled(csev == null);

    // edit, new, ...
    boolean loggedIn = SessionHelper.isUserLoggedIn();
    ComponentHelper.setVisible("buttonNew", loggedIn, this);
    ComponentHelper.setVisibleAndDisabled("buttonNewSub", loggedIn, csev == null, this);
    ComponentHelper.setVisibleAndDisabled("buttonEdit", loggedIn, csev == null, this);

    ComponentHelper.setVisible("buttonDeleteVersion", loggedIn, this);
    //ComponentHelper.setVisibleAndDisabled("buttonDeleteVersion", loggedIn, csev == null, this);
    
  }

}
