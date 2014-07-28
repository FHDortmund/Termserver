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

import de.fhdo.helper.LanguageHelper;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.TermUser;

/**
 *
 * @author Becker
 */
public class PopupUserDetails extends GenericForwardComposer{     
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private AnnotateDataBinder  binder;
    private TermUser user;       
    private int      editMode;  
    private Window   window;
//    private Content  windowParent;  
//    private Datebox  dateBoxED;   
    private Checkbox cbUAIsAdmin;    
    private Button   bCreate;
    private Label    lReq, lName;
    private Textbox  tbUAName;
    private Combobox cboxLanguage;
    
    private void loadUserDetailsFormSession(){
        org.zkoss.zk.ui.Session sess = org.zkoss.zk.ui.Sessions.getCurrent();    
        user = new TermUser();                               
//        user.setIsAdmin(Boolean.parseBoolean(sess.getAttribute("is_admin").toString())); // isAdmin wird im moment nicht vom TS? unterstützt?
        user.setName(sess.getAttribute("user_name").toString());        
    }
    
    /**
     * 
     * @param mode PopupWindow.EDITMODE_
     */     
    public void editMode(int mode){                
        switch(mode){
            case PopupWindow.EDITMODE_DETAILSONLY:  
                loadUserDetailsFormSession();
                
                tbUAName.setReadonly(true);       
                cbUAIsAdmin.setDisabled(true);
                bCreate.setVisible(false);
                lReq.setVisible(false);   
                lName.setValue(Labels.getLabel("common.name"));
                cbUAIsAdmin.setValue(Labels.getLabel("common.administrator"));
                break;                
            case PopupWindow.EDITMODE_CREATE:  
                lReq.setVisible(true);   
                lName.setValue(Labels.getLabel("common.name") + "*");
                cbUAIsAdmin.setValue(Labels.getLabel("common.administrator") + "*");
                break;                
            case PopupWindow.EDITMODE_MAINTAIN_VERSION_NEW:                 
                break;                
             case PopupWindow.EDITMODE_MAINTAIN_VERSION_EDIT:                
                break;                  
        }      
        initializeDatabinder();
    }
    
    private void initializeDatabinder(){
        showDates();
        
        binder = new AnnotateDataBinder(window);
        binder.bindBean("user"        , user);
        binder.loadAll();                
    }
    
    private void showDates(){
        if(user != null){
//            if(csv.getExpirationDate() != null)
//                dateBoxED.setValue(new Date(csv.getExpirationDate().toGregorianCalendar().getTimeInMillis()));            
        }
    }
    
    private void createNewUser(){               
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {        
        super.doAfterCompose(comp);
        
        window       = (Window)comp;
                
        editMode = (Integer)arg.get("EditMode");  // PopupWindow.EDITMODE_
        editMode(editMode);  
       
        // Sprachen als ListModelList in die cBox laden
        cboxLanguage.setModel(LanguageHelper.getListModelList()); 
    }       
    
    
    
    public void onClick$bCreate(){                   
    }
}