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
import de.fhdo.gui.main.modules.PopupConcept.EDITMODES;
import de.fhdo.helper.DomainHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IUpdateData;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class PopupConceptTranslations implements IUpdateData
{

  long tempId = Long.MAX_VALUE - 1;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericListTranslation = null;

  private EDITMODES editMode;
  private boolean inlineEditing;

  private List<CodeSystemConceptTranslation> translationList;
  private List<Long> deleteIdList;

  public PopupConceptTranslations(EDITMODES editMode)
  {
    deleteIdList = new LinkedList<Long>();
    this.editMode = editMode;

    inlineEditing = editMode == PopupConcept.EDITMODES.CREATE || editMode == PopupConcept.EDITMODES.CREATE_NEW_VERSION || editMode == PopupConcept.EDITMODES.MAINTAIN;
    logger.debug("inlineEditing: " + inlineEditing);
  }

  public void completeList()
  {
    logger.debug("completeList");

    // remove temporary IDs
    for (CodeSystemConceptTranslation csct : translationList)
    {
      if (csct.getId() >= tempId)
      {
        csct.setId(null);
        
        logger.debug("remove temporary ID");
        logger.debug("id: " + csct.getId() + ", cd: " + csct.getLanguageCd() + ", term: " + csct.getTerm());
      }
    }
    
    // add delete IDs
    for(Long deleteId : deleteIdList)
    {
      CodeSystemConceptTranslation csctDelete = new CodeSystemConceptTranslation();
      csctDelete.setId(deleteId);
      translationList.add(csctDelete);
      
      logger.debug("delete tranlsation with id: " + deleteId);
    }

    logger.debug("count: " + translationList.size());
  }

  public List<CodeSystemConceptTranslation> getTranslationList()
  {
    logger.debug("getTranslationList()");
    /*

     List<CodeSystemConceptTranslation> list = new LinkedList<CodeSystemConceptTranslation>();

     ListModelList lml = (ListModelList) genericListTranslation.getListbox().getListModel();

     logger.debug("count: " + lml.size());

     for (Object o : lml)
     {
     logger.debug("Type: " + o.getClass().getCanonicalName());
     GenericListRowType row = (GenericListRowType) o;
     CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) row.getData();
     list.add(csct);
     logger.debug("add '" + csct.getLanguageCd() + "', " + csct.getTerm());
     }*/

    /*for(GenericListRowType rows : genericListTranslation.getDataList())
     {
     CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation)rows.getData();
     list.add(csct);
     logger.debug("add '" + csct.getLanguageCd() + "', " + csct.getTerm());
     }*/
    // remove temporary IDs
    for (CodeSystemConceptTranslation csct : translationList)
    {
      if (csct.getId() >= tempId)
      {
        csct.setId(null);
      }
    }

    logger.debug("count: " + translationList.size());

    return translationList;
  }

  public void initListTranslation(CodeSystemConcept csc, PopupConcept parent)
  {
    if (genericListTranslation == null)
    {
      logger.debug("initListTranslation()");

      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();

      /*if (inlineEditing)
       {

       header.add(new GenericListHeaderType(Labels.getLabel("common.language"), 160, "", true,
       DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_LANGUAGECODES), true, true, inlineEditing, true));
       }
       else*/
      header.add(new GenericListHeaderType(Labels.getLabel("common.language"), 160, "", true, "String", true, true, false, false));

      header.add(new GenericListHeaderType(Labels.getLabel("common.translation"), 0, "", true, "String", true, true, inlineEditing, false));
      header.add(new GenericListHeaderType(Labels.getLabel("common.abbrevation"), 150, "", true, "String", true, true, inlineEditing, false));

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

      translationList = csc.getCodeSystemConceptTranslations();
      if (translationList == null)
        translationList = new LinkedList<CodeSystemConceptTranslation>();

      logger.debug("Anzahl: " + translationList.size());

      for (CodeSystemConceptTranslation data : translationList)
      {
        GenericListRowType row = createRowFromTranslation(data);
        dataList.add(row);
      }

      // Liste initialisieren
      Include inc = (Include) parent.getFellow("incListTranslation");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListTranslation = (GenericList) winGenericList;
      genericListTranslation.setListId("translation");

      //genericList.setListActions(this);
      genericListTranslation.setButton_new(inlineEditing);
      genericListTranslation.setButton_edit(false);
      genericListTranslation.setButton_delete(inlineEditing);
      genericListTranslation.setListHeader(header);
      genericListTranslation.setDataList(dataList);
      genericListTranslation.setListActions(parent);

      genericListTranslation.setUpdateDataListener(this);

      // Buttons are used to add or remove rows
      // they can be edited directly inline
      genericListTranslation.getButtonNew().setLabel("");
      genericListTranslation.getButtonDelete().setLabel("");

      // show list count in tab header
      ((Tab) parent.getFellow("tabTranslations")).setLabel(Labels.getLabel("common.translations") + " (" + dataList.size() + ")");
    }
  }

  private GenericListRowType createRowFromTranslation(CodeSystemConceptTranslation data)
  {
    final GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[3];

    if (inlineEditing)
    {
      //DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_LANGUAGECODES)
      Listcell lc = new Listcell();
      Combobox cb = new Combobox();
      cb.setInplace(true);
      DomainHelper.getInstance().fillCombobox(cb, Definitions.DOMAINID_LANGUAGECODES, data.getLanguageCd());
      lc.appendChild(cb);

      cb.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
      {
        public void onEvent(Event event) throws Exception
        {
          logger.debug("ON_CHANGE");
          Combobox cb = (Combobox) event.getTarget();
          //data.setData(cb.getText());

          // Benachrichtigung über geänderte Daten
          onCellUpdated(0, DomainHelper.getInstance().getComboboxCd(cb), row);
        }
      });

      cells[0] = new GenericListCellType(lc, false, "");
    }
    else
      cells[0] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_LANGUAGECODES, data.getLanguageCd()), false, "");

    cells[1] = new GenericListCellType(data.getTerm(), false, "");
    cells[2] = new GenericListCellType(data.getTermAbbrevation(), false, "");

    row.setData(data);
    row.setCells(cells);

    return row;
  }

  public void addRow()
  {
    logger.debug("addRow - Translations");

    CodeSystemConceptTranslation trans = new CodeSystemConceptTranslation();

    // need to set empty string to allow inline editing
    trans.setId(tempId--);
    trans.setLanguageCd("");
    trans.setTerm("");
    trans.setTermAbbrevation("");

    // adds entry to list
    genericListTranslation.addEntry(createRowFromTranslation(trans));
    translationList.add(trans);
  }

  public void removeRow(Object o)
  {
    logger.debug("removeRow - Translations");
    if (o != null)
    {
      logger.debug("object type: " + o.getClass().getCanonicalName());

      if (o instanceof CodeSystemConceptTranslation)
      {
        CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) o;

        // todo remove from concept
        if(csct.getId() < tempId)
        {
          deleteIdList.add(csct.getId());
        }
        
        for(CodeSystemConceptTranslation csct_temp : translationList)
        {
          if(csct_temp.getId().longValue() == csct.getId())
          {
            translationList.remove(csct_temp);
            break;
          }
        }
        //genericListTranslation.r
      }
    }
  }

  public void onCellUpdated(int cellIndex, Object data, GenericListRowType row)
  {
    logger.debug("onCellUpdated translation(), data: " + data.toString());

    if (row != null && row.getData() != null && row.getData() instanceof CodeSystemConceptTranslation
        && data != null && data instanceof String)
    {

      CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) row.getData();

      logger.debug("set value in list with id: " + csct.getId() + ", list size: " + translationList.size());

      for (CodeSystemConceptTranslation csctTemp : translationList)
      {

        if (csctTemp.getId().longValue() == csct.getId())
        {
          logger.debug("found id, update value!");

          if (cellIndex == 1)
            csctTemp.setTerm(data.toString());
          else if (cellIndex == 2)
            csctTemp.setTermAbbrevation(data.toString());
          else if (cellIndex == 0)
            csctTemp.setLanguageCd(data.toString()); // TODO aus Domäne
          //csctTemp.setParameterValue(data.toString());
          break;
        }
      }

    }
  }

}
