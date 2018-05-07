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
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;

/**
 *
 * @author Becker
 */
public class PopupGlobalSearch extends Window implements IGenericListActions, IUpdateModal, AfterCompose
{

  GenericList genericList;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private Integer mode = -1;
  private Label l_status;

  public void afterCompose()
  {
    //mode = (Integer) Executions.getCurrent().getArg().get("EditMode");
    initList();
  }

  private void initList()
  {
    logger.debug("Desktop(): initList()");
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    //if (mode == 97)
    {
      header.add(new GenericListHeaderType("Begriff", 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Code", 225, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("CodeSystem", 200, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("CodeSystemVersion", 200, "", true, "String", true, true, false, false));
    }
    /*else
    { //mode == 98

      header.add(new GenericListHeaderType("Begriff", 225, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Code", 0, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("ValueSet", 200, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("ValueSetVersion", 200, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Quelle", 400, "", true, "String", true, true, false, false));
    }*/
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    // Liste initialisieren
    l_status = (Label) getFellow("l_status");
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);

    genericList.getListbox().setMold("paging");
    genericList.getListbox().setPageSize(20);
  }

  public void onSearchClicked()
  {

    initList();

    ListGloballySearchedConceptsRequestType parameter = new ListGloballySearchedConceptsRequestType();
    ListGloballySearchedConceptsResponse.Return response = null;
    //List<ListGloballySearchedConceptsResponse.Return> gsreList = null;

    /*if (mode == 97)
     {  //CS Global Search
     //parameter.
     parameter.setCodeSystemConceptSearch(true);
     }

     if (mode == 98)
     {  //VS Global Search
     parameter.setCodeSystemConceptSearch(false);
     }*/
    if (SessionHelper.isCollaborationActive())
    {
      // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
      parameter.setLoginToken(CollaborationSession.getInstance().getSessionID());
    }
    else if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    Textbox tbTerm = (Textbox) getFellow("tbTerm");
    Textbox tbCode = (Textbox) getFellow("tbCode");

    parameter.setCodeSystemConcept(new CodeSystemConcept());

    if (tbTerm.getText() != null)
    {
      parameter.getCodeSystemConcept().setTerm(tbTerm.getText());
    }
    if (tbCode.getText() != null)
    {
      parameter.getCodeSystemConcept().setCode(tbCode.getText());
    }

    response = WebServiceHelper.listGloballySearchedConcepts(parameter);
    if (response != null)
    {
      //response.getCodeSystemEntity()
      //gsreList = response.getGlobalSearchResultEntry();

      if (response.getReturnInfos().getStatus().equals(Status.OK))
      {
        if (response.getCodeSystemEntity().isEmpty())
        {
          l_status.setValue("Ihre Suche ergab keine Resultate!");  // TODO übersetzen
        }
        else
        {
          l_status.setValue("");
        }
        for (CodeSystemEntity entry : response.getCodeSystemEntity())
        {
          GenericListRowType row = createMyTermRow(entry);
          genericList.addEntry(row);
        }
      }
    }
  }

  private GenericListRowType createMyTermRow(CodeSystemEntity cse)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = null;

    CodeSystemEntityVersion csev = cse.getCodeSystemEntityVersions().get(0);
    CodeSystemConcept csc = csev.getCodeSystemConcepts().get(0);
    
    CodeSystemVersion csv = new CodeSystemVersion();
    CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
    if(cse.getCodeSystemVersionEntityMemberships() != null)
    {
      csvem = cse.getCodeSystemVersionEntityMemberships().get(0);
      csv = csvem.getCodeSystemVersion();
    }
    
    cells = new GenericListCellType[4];
    cells[0] = new GenericListCellType(csc.getTerm(), false, "");
    cells[1] = new GenericListCellType(csc.getCode(), false, "");
    cells[2] = new GenericListCellType(csv.getCodeSystem().getName(), false, "");
    cells[3] = new GenericListCellType(csv.getName(), false, "");

    /*if (cse).isCodeSystemEntry())
     {
     cells = new GenericListCellType[4];
     cells[0] = new GenericListCellType(((GlobalSearchResultEntry) cse).getTerm(), false, "");
     cells[1] = new GenericListCellType(((GlobalSearchResultEntry) cse).getCode(), false, "");
     cells[2] = new GenericListCellType(((GlobalSearchResultEntry) cse).getCodeSystemName(), false, "");
     cells[3] = new GenericListCellType(((GlobalSearchResultEntry) cse).getCodeSystemVersionName(), false, "");
     }
     else
     {
     cells = new GenericListCellType[5];
     cells[0] = new GenericListCellType(((GlobalSearchResultEntry) cse).getTerm(), false, "");
     cells[1] = new GenericListCellType(((GlobalSearchResultEntry) cse).getCode(), false, "");
     cells[2] = new GenericListCellType(((GlobalSearchResultEntry) cse).getValueSetName(), false, "");
     cells[3] = new GenericListCellType(((GlobalSearchResultEntry) cse).getValueSetVersionName(), false, "");
     cells[4] = new GenericListCellType(((GlobalSearchResultEntry) cse).getSourceCodeSystemInfo(), false, "");
     }*/
    row.setData(cse);
    row.setCells(cells);

    return row;
  }

  public void onNewClicked(String id)
  {

  }

  public void onEditClicked(String id, Object data)
  {

  }

  public void onDeleted(String id, Object data)
  {

  }

  public void onSelected(String id, Object data)
  {

  }

  public void update(Object o, boolean edited)
  {

  }
}
