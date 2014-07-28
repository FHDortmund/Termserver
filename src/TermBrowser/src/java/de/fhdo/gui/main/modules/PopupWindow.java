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

import de.fhdo.terminologie.ws.authoring.VersioningType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Window;

/**
 *
 * @author Becker
 */
public abstract class PopupWindow extends GenericForwardComposer{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    public final static int  EDITMODE_DETAILSONLY = 1,
                             EDITMODE_CREATE = 2,
                             EDITMODE_MAINTAIN_VERSION_NEW = 3,
                             EDITMODE_MAINTAIN = 4,
                             EDITMODE_MAINTAIN_VERSION_EDIT = 5,
                             EDITMODE_UPDATESTATUS = 6,
                             EDITMODE_UPDATESTATUS_VERSION = 7;    
    
    protected AnnotateDataBinder binder;
    protected VersioningType     versioning;;  
    protected int                editMode = EDITMODE_DETAILSONLY;  
    protected Window             window;
    protected Window             windowParent;  
    
    abstract public void doAfterComposeCustom();
    abstract protected void initializeDatabinder();
    abstract protected void loadDatesIntoGUI();
    
    /* Editmodes legen fest, welche GUI-Elemente sichtbar/aktiv sind und erstellen/laden die nötigen Objekte */
    abstract protected void editmodeDetails();
    abstract protected void editmodeCreate();
    abstract protected void editmodeMaintainVersionNew();
    abstract protected void editmodeMaintain();
    abstract protected void editmodeMaintainVersionEdit();
    abstract protected void editmodeUpdateStatus();
    abstract protected void editmodeUpdateStatusVersion();
        
    abstract protected void create();
    abstract protected void maintainVersionNew();
    abstract protected void maintain();
    abstract protected void maintainVersionEdit();
    abstract protected void updateStatus();
    abstract protected void updateStatusVersion();            
    
    @Override
    public final void doAfterCompose(Component comp) throws Exception{
        super.doAfterCompose(comp);
        if(arg.get("EditMode") != null)
            editMode = (Integer)arg.get("EditMode");
        window       = (Window)comp;
        windowParent = (Window)comp.getParent();

        doAfterComposeCustom();
        editMode(editMode);
    }       

    protected void buttonAction(){
        switch(editMode){            
            case EDITMODE_CREATE:
                create();
                break;
            case EDITMODE_MAINTAIN_VERSION_NEW:
                maintainVersionNew();
                break;
            case EDITMODE_MAINTAIN:
                maintain();
                break;    
            case EDITMODE_MAINTAIN_VERSION_EDIT:
                maintainVersionEdit();
                break;    
            case EDITMODE_UPDATESTATUS:
                updateStatus();
                break;
            case EDITMODE_UPDATESTATUS_VERSION:
                updateStatusVersion();
                break;
        }
    }

    protected void editMode(int eMode){
        editMode = eMode;
        switch(eMode){
            case EDITMODE_DETAILSONLY:
                editmodeDetails();
                break;
            case EDITMODE_CREATE:
                editmodeCreate();
                break;
            case EDITMODE_MAINTAIN_VERSION_NEW:
                editmodeMaintainVersionNew();
                break;
            case EDITMODE_MAINTAIN:
                editmodeMaintain();
                break;    
            case EDITMODE_MAINTAIN_VERSION_EDIT:
                editmodeMaintainVersionEdit();
                break;    
            case EDITMODE_UPDATESTATUS:
                editmodeUpdateStatus();
                break;
            case EDITMODE_UPDATESTATUS_VERSION:
                editmodeUpdateStatusVersion();
                break;
        }
        initializeData();
    }

    protected void initializeData(){
        loadDatesIntoGUI();
        initializeDatabinder();
    }       
}
