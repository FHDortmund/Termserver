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

import de.fhdo.models.TreeModelCSEV;
import de.fhdo.terminologie.ws.search.PagingType;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

/**
 *
 * @author Becker
 */
public class ContentConceptsHugeFlatData extends ContentConcepts_old{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    protected PagingType pagingTypeWS;
    
    @Override
    protected void initDataCreateModel() throws Exception{          
        pagingTypeWS = new PagingType();
        pagingTypeWS.setPageSize("100");
        pagingTypeWS.setPageIndex(0); 
        treeModel = new TreeModelCSEV(source, null, null, null, pagingTypeWS, false,false, this.parent);                 
    }
    
    @Override
    protected void initDataTreeProperties(){
        super.initDataTreeProperties();
        
        Paging paging = (Paging)getFellow("paging");              
        paging.setPageSize(Integer.valueOf(pagingTypeWS.getPageSize()));
        paging.setActivePage(pagingTypeWS.getPageIndex());
        paging.setTotalSize(treeModel.getTotalSize());
        paging.addEventListener("onPaging", new EventListener(){
            public void onEvent(Event event) throws Exception {
                if(event instanceof PagingEvent){
                    PagingEvent pe = (PagingEvent)event;                                        
                    treeModel.loadDataByPageIndex(pe.getActivePage());                                                            
                    updateModel(true);
                }                
            }            
        });        
    }
}
