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
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetails;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.Date;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class PopupAssociationConcept extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static enum EDITMODES
  {

    NONE, DETAILSONLY, CREATE, MAINTAIN, CREATE_NEW_VERSION
  }
  private EDITMODES editMode;

  private IUpdateModal updateListener;

  //private CodeSystem codeSystem = null;
  //private CodeSystemVersion codeSystemVersion = null;
  private AssociationType associationType;

  private CodeSystemVersionEntityMembership csvem;
  private CodeSystemEntityVersion csev;
  private CodeSystemEntity cse;

  long conceptId = 0;

  public PopupAssociationConcept()
  {
    logger.debug("PopupConcept() - Konstruktor");

    // load arguments
    conceptId = ArgumentHelper.getWindowArgumentLong("ConceptId");

    logger.debug("conceptId: " + conceptId);

    editMode = EDITMODES.NONE;
    Object o = ArgumentHelper.getWindowArgument("EditMode");
    if (o != null)
    {
      try
      {
        editMode = (EDITMODES) o;
      }
      catch (NumberFormatException ex)
      {
        LoggingOutput.outputException(ex, PopupCodeSystem.class);
      }
    }
    logger.debug("Edit Mode: " + editMode.name());

    initData();
  }

  public void afterCompose()
  {
    logger.debug("PopupConcept() - afterCompose()");

    //setWindowTitle();
    //showDetailsVisibilty();
    // fill domain values with selected codes
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbStatus"), de.fhdo.Definitions.DOMAINID_STATUS,
        csev == null ? "" : "" + csev.getStatusVisibility());

    // load data without bindings (dates, ...)
    if (csev.getStatusVisibilityDate() != null)
      ((Datebox) getFellow("dateBoxSD")).setValue(new Date(csev.getStatusVisibilityDate().toGregorianCalendar().getTimeInMillis()));
    if (csev.getInsertTimestamp() != null)
      ((Datebox) getFellow("dateBoxID")).setValue(new Date(csev.getInsertTimestamp().toGregorianCalendar().getTimeInMillis()));
    //if (csev.getEffectiveDate() != null)
    //  ((Datebox) getFellow("dateBoxReleasedAt")).setValue(new Date(csev.getEffectiveDate().toGregorianCalendar().getTimeInMillis()));

    ComponentHelper.setVisible("divId", editMode == EDITMODES.CREATE_NEW_VERSION || editMode == EDITMODES.MAINTAIN
        || editMode == EDITMODES.DETAILSONLY, this);

    // change size when resizing window
    this.addEventListener(Events.ON_SIZE, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        logger.debug("ON_SIZE");

        ((Borderlayout) getFellow("borderlayout")).setVflex("100%");
        ((Tabbox) getFellow("tabboxFilter")).setVflex("100%");

        invalidate();
      }
    });
  }

  private void initData()
  {
    // Properties
    if (editMode == EDITMODES.CREATE)
    {
      logger.debug("new entry");

      // new entry
      csev = new CodeSystemEntityVersion();
      associationType = new AssociationType();
      cse = new CodeSystemEntity();
      csvem = new CodeSystemVersionEntityMembership();

      csev.getAssociationTypes().add(associationType);
      csev.setStatusVisibility(Definitions.STATUS_VISIBILITY_VISIBLE);
      csev.setIsLeaf(Boolean.TRUE);
      csev.setStatusVisibilityDate(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
      csev.setInsertTimestamp(DateTimeHelper.dateToXMLGregorianCalendar(new Date()));
      csvem.setIsAxis(Boolean.FALSE);
      csvem.setIsMainClass(Boolean.FALSE);
    }
    else
    {
      logger.debug("load association details");

      // load concept details
      ReturnConceptAssociationTypeDetailsRequestType parameter = new ReturnConceptAssociationTypeDetailsRequestType();
      parameter.setCodeSystemEntity(new CodeSystemEntity());
      CodeSystemEntityVersion csev_ws = new CodeSystemEntityVersion();
      csev_ws.setVersionId(conceptId);
      parameter.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev_ws);

      if (SessionHelper.isUserLoggedIn())
      {
        parameter.setLoginToken(SessionHelper.getSessionId());
      }

      ReturnConceptAssociationTypeDetailsResponse.Return response = WebServiceHelper.returnConceptAssociationTypeDetails(parameter);

      // keine csev zurueckgekommen (wegen moeglicher Fehler beim WS)
      if (response.getCodeSystemEntity() == null)
        return;

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        // load entities
        cse = response.getCodeSystemEntity();
        for (CodeSystemEntityVersion csev_db : cse.getCodeSystemEntityVersions())
        {
          if (csev_db.getVersionId().longValue() == cse.getCurrentVersionId())
          {
            csev = csev_db;
            associationType = csev.getAssociationTypes().get(0);
            break;
          }
        }

        for (CodeSystemVersionEntityMembership csvem_db : cse.getCodeSystemVersionEntityMemberships())
        {
          //if (csvem_db.getId() != null && codeSystemVersionId == csvem_db.getId().getCodeSystemVersionId())
          {
            csvem = csvem_db;
            logger.debug("csvem found");
            break;
          }
        }
      }
    }
  }

  public void onOkClicked()
  {
    logger.debug("onOkClicked() - save data...");

    // check mandatory fields
    if ((associationType.getForwardName() == null || associationType.getForwardName().length() == 0)
        || (associationType.getReverseName() == null || associationType.getReverseName().length() == 0))
    {
      Messagebox.show(Labels.getLabel("common.mandatoryFields"), Labels.getLabel("common.requiredField"), Messagebox.OK, Messagebox.EXCLAMATION);
      return;
    }

    // build structure for webservice
    //cse.getCodeSystemVersionEntityMemberships().clear();
    //cse.getCodeSystemVersionEntityMemberships().add(csvem);
    csev.getAssociationTypes().clear();
    csev.getAssociationTypes().add(associationType);

    cse.getCodeSystemEntityVersions().clear();
    cse.getCodeSystemEntityVersions().add(csev);

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

    if (updateListener != null
        && (editMode == EDITMODES.MAINTAIN
        || editMode == EDITMODES.CREATE_NEW_VERSION
        || editMode == EDITMODES.CREATE))
    {
      // update tree
      updateListener.update(cse, editMode == EDITMODES.MAINTAIN || editMode == EDITMODES.CREATE_NEW_VERSION);
    }

    if (success)
      this.detach();
  }

  public boolean save_Create()
  {
    logger.debug("save_Create()");

    // Liste leeren, da hier so viele CSVs drin stehen wie es Versionen gibt. Als Parameter darf aber nur genau EINE CSV drin stehen.
    CreateConceptAssociationTypeRequestType parameter = new CreateConceptAssociationTypeRequestType();

    // set parameter
    parameter.setLoginToken(de.fhdo.helper.SessionHelper.getSessionId());

    //parameter.setCodeSystem(new CodeSystem());
    //parameter.getCodeSystem().setId(codeSystemId);
    //CodeSystemVersion csv = new CodeSystemVersion();
    //csv.setVersionId(codeSystemVersionId);
    //parameter.getCodeSystem().getCodeSystemVersions().add(csv);
    parameter.setCodeSystemEntity(cse); // cse structure build before

    // WS aufruf
    CreateConceptAssociationTypeResponse.Return response = WebServiceHelper.createConceptAssociationType(parameter);

    // Message über Erfolg/Misserfolg                
    if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
    {
      // die neue cse(v) hat noch keine id. Für Assoziationen aber nötig => aus response auslesen
      csev.setVersionId(response.getCodeSystemEntity().getCurrentVersionId());
      cse.setId(response.getCodeSystemEntity().getId());
      cse.setCurrentVersionId(csev.getVersionId());

      logger.debug("new Entity-ID: " + cse.getId());
      logger.debug("new Version-ID: " + csev.getVersionId());
    }
    else
    {
      Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + response.getReturnInfos().getMessage());
    }

    return true;
  }

  public boolean save_MaintainVersion()
  {
    logger.debug("save_MaintainVersion()");

    Checkbox cbNewVersion = (Checkbox) getFellow("cbNewVersion");

    MaintainConceptAssociationTypeRequestType parameter = new MaintainConceptAssociationTypeRequestType();

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

    MaintainConceptAssociationTypeResponse.Return response = WebServiceHelper.maintainConceptAssociationType(parameter);

    csev.setCodeSystemEntity(cse);  // das Löschen der cse aus der csev wieder rückgängig machen (war nur für die Anfrage an WS)       

    // Meldung
    try
    {
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.authoring.Status.OK)
      {
        this.detach();
      }
      else
        Messagebox.show(Labels.getLabel("common.error") + "\n" + Labels.getLabel("popupConcept.conceptNotCreated") + "\n\n" + response.getReturnInfos().getMessage());
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

    return true;
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdateModal updateListener)
  {
    this.updateListener = updateListener;
  }

  /**
   * @return the associationType
   */
  public AssociationType getAssociationType()
  {
    return associationType;
  }

  /**
   * @param associationType the associationType to set
   */
  public void setAssociationType(AssociationType associationType)
  {
    this.associationType = associationType;
  }
}
