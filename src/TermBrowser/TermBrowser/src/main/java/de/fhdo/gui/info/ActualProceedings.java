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
package de.fhdo.gui.info;

import de.fhdo.helper.WebServiceHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.administration.ActualProceeding;
import de.fhdo.terminologie.ws.administration.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.administration.Status;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 *
 * @author Philipp Urbauer
 */
public class ActualProceedings extends Window implements org.zkoss.zk.ui.ext.AfterCompose,IGenericListActions //public class Menu extends GenericAutowireComposer
{
 
    GenericList genericList;
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
    public ActualProceedings()
    {
    }

    public void afterCompose()
    {
        initList();
    }

    
    private void initList(){
        
        logger.debug("ActualProceedings(): initList()");

        // Header
        List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
        header.add(new GenericListHeaderType("Terminologie", 0, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Version", 250, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Typ", 100, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Änderung", 100, "", true, "String", true, true, false, false));
        header.add(new GenericListHeaderType("Datum der Änderung", 150, "", true, "String", true, true, false, false));

        List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
        
        ActualProceedingsRequestType aprequt = new ActualProceedingsRequestType();
        ActualProceedingsResponseType aprespt = WebServiceHelper.actualProceedings(aprequt);
        
        if(aprespt.getReturnInfos().getStatus() == Status.OK){
            for(ActualProceeding ap:aprespt.getActualProceedings()){

                GenericListRowType row = createMyTermRow(ap);
                dataList.add(row);
            }
        }
        // Liste initialisieren
        Include inc = (Include) getFellow("incListActualProceedings");
        Window winGenericList = (Window) inc.getFellow("winGenericList");
        genericList = (GenericList) winGenericList;

        genericList.setListActions(this);
        genericList.setButton_new(false);
        genericList.setButton_edit(false);
        genericList.setButton_delete(false);
        genericList.setListHeader(header);
        genericList.setDataList(dataList);
    }

    private GenericListRowType createMyTermRow(Object obj)
    {
        GenericListRowType row = new GenericListRowType();

        GenericListCellType[] cells = new GenericListCellType[5]; //Size
        
        if(obj instanceof ActualProceeding){
           
            cells[0] = new GenericListCellType(((ActualProceeding)obj).getTerminologieName(), false, "");
            cells[1] = new GenericListCellType(((ActualProceeding)obj).getTerminologieVersionName(), false, "");
            cells[2] = new GenericListCellType(((ActualProceeding)obj).getTerminologieType(), false, "");
            cells[3] = new GenericListCellType(((ActualProceeding)obj).getStatus(), false, "");
            cells[4] = new GenericListCellType(((ActualProceeding)obj).getLastChangeDate(), false, "");
        }
        
        row.setData(obj);
        row.setCells(cells);

        return row;
    } 
    
    public void onNewClicked(String id) {
        this.setVisible(false);
        this.detach();
    }

    public void onEditClicked(String id, Object data) {
        
    }

    public void onDeleted(String id, Object data) {
        
    }

    public void onSelected(String id, Object data) {
        
    }
}
