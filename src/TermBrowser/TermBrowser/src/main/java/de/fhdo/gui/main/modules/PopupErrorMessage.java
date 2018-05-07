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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author Becker
 */
public class PopupErrorMessage extends  GenericForwardComposer{     
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
//    private CodeSystem          cs = null;
//    private CodeSystemVersion   csv = null;          
    private Window   window;
//    private Content  windowParent;       
    private Label    lErrorMessage;
    
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {        
        super.doAfterCompose(comp); 
        Exception e = (Exception)arg.get("Exception");
        String sMessage = (String)arg.get("ExceptionMessage");
        window       = (Window)comp;
        lErrorMessage.setValue(sMessage);
//        windowParent = (Content)comp.getParent();    
    }  
        
//    public void onClick$bClose(){          
//        window.detach();                                              
//    }
}