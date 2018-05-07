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
package de.fhdo.collaboration.desktop.proposal.privilege;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
 
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
 /**
 *
 * @author Philipp Urbauer
 */
public class DualListboxDiscGroup extends Div implements IdSpace {
    private static final long serialVersionUID = 5183321186606483396L;
     
    @Wire
    private Listbox lbDiscGroup;
    @Wire
    private Listbox lbDiscGroupChoosen;
 
    private ListModelList<PrivilegeDiscGroupInfo> candidateModel;
    private ListModelList<PrivilegeDiscGroupInfo> chosenDataModel;
    
    public DualListboxDiscGroup() {
        Executions.createComponents("/collaboration/desktop/proposal/privilege/v_dualListboxDiscGroup.zul", this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        Set<PrivilegeDiscGroupInfo> choosenDiscGroupData = (Set<PrivilegeDiscGroupInfo>)Executions.getCurrent().getAttribute("choosenDiscGroupData");
        if(!choosenDiscGroupData.isEmpty()){
            lbDiscGroupChoosen.setModel(chosenDataModel = new ListModelList<PrivilegeDiscGroupInfo>(choosenDiscGroupData));
        }else{
            lbDiscGroupChoosen.setModel(chosenDataModel = new ListModelList<PrivilegeDiscGroupInfo>());
        }
    }
 
    @Listen("onClick = #chooseBtn")
    public void chooseItem() {
        Events.postEvent(new ChooseEvent(this, chooseOne()));
    }
 
    @Listen("onClick = #removeBtn")
    public void unchooseItem() {
        Events.postEvent(new ChooseEvent(this, unchooseOne()));
    }
 
    @Listen("onClick = #chooseAllBtn")
    public void chooseAllItem() {
        for (int i = 0, j = candidateModel.getSize(); i < j; i++) {
            chosenDataModel.add(candidateModel.getElementAt(i));
        }
        candidateModel.clear();
    }
 
    @Listen("onClick = #removeAllBtn")
    public void unchooseAll() {
        for (int i = 0, j = chosenDataModel.getSize(); i < j; i++) {
            candidateModel.add(chosenDataModel.getElementAt(i));
        }
        chosenDataModel.clear();
    }
    
    /**
     * Set new candidate ListModelList.
     *
     * @param candidate
     *            is the data of candidate list model
     */
    public void setModel(List<PrivilegeDiscGroupInfo> candidate) {
        lbDiscGroup.setModel(this.candidateModel = new ListModelList<PrivilegeDiscGroupInfo>(candidate));
        //chosenDataModel.clear();
    }
 
    /**
     * @return current chosen data list
     */
    public List<PrivilegeDiscGroupInfo> getChosenDataList() {
        return new ArrayList<PrivilegeDiscGroupInfo>(chosenDataModel);
    }
    
    public List<PrivilegeDiscGroupInfo> getDataList() {
        return new ArrayList<PrivilegeDiscGroupInfo>(candidateModel);
    }
 
    private Set<PrivilegeDiscGroupInfo> chooseOne() {
        Set<PrivilegeDiscGroupInfo> set = candidateModel.getSelection();
        for (PrivilegeDiscGroupInfo selectedItem : set) {
            chosenDataModel.add(selectedItem);
            candidateModel.remove(selectedItem);
        }
        return set;
    }
 
    private Set<PrivilegeDiscGroupInfo> unchooseOne() {
        Set<PrivilegeDiscGroupInfo> set = chosenDataModel.getSelection();
        for (PrivilegeDiscGroupInfo selectedItem : set) {
            candidateModel.add(selectedItem);
            chosenDataModel.remove(selectedItem);
        }
        return set;
    }
 
    // Customized Event
    public class ChooseEvent extends Event {
        private static final long serialVersionUID = -7334906383953342976L;
 
        public ChooseEvent(Component target, Set<PrivilegeDiscGroupInfo> data) {
            super("onChoose", target, data);
        }
    }
}