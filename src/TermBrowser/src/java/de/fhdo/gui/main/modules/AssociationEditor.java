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

import de.fhdo.gui.main.ContentCSVSAssociationEditor;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Window;

/**
 *
 * @author Becker
 */
public class AssociationEditor extends Window implements AfterCompose {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    protected ContentCSVSAssociationEditor windowL,
                                           windowR;        
        
    public void afterCompose() {  
//        ((Borderlayout)getRoot()).setTitle(Labels.getLabel("common.editor"));        
        
        Radiogroup rgMode = (Radiogroup)getFellow("rgMode");
        rgMode.addEventListener(Events.ON_CHECK, new EventListener() {
            public void onEvent(Event event) throws Exception {                                
//                if(windowL != null && windowL.getWindowContentConcepts() != null)
//                    windowL.getWindowContentConcepts().setAssociationMode(getAssociationMode());
//                if(windowR != null && windowR.getWindowContentConcepts() != null)
//                    windowR.getWindowContentConcepts().setAssociationMode(getAssociationMode());
            }
        });
        
        Include incL = (Include) getFellow("incInhalteLinks");                                                        
        incL.setDynamicProperty("radioGroupAssociationMode", rgMode);
        incL.setSrc(null);        
        incL.setSrc("./ContentCSVSAssociationEditor.zul");                
        windowL = (ContentCSVSAssociationEditor)incL.getFellow("windowCSVS");
        
        Include incR = (Include) getFellow("incInhalteRechts");                                                                
        incR.setDynamicProperty("radioGroupAssociationMode", rgMode);
        incR.setSrc(null);
        incR.setSrc("./ContentCSVSAssociationEditor.zul");
        windowR = (ContentCSVSAssociationEditor)incR.getFellow("windowCSVS");                        
    }

    public int getAssociationMode() {
        return Integer.valueOf(((Radiogroup)getFellow("rgMode")).getSelectedItem().getValue().toString());  // TODO überprüfen, ob .toString() funktionert
    } 
}