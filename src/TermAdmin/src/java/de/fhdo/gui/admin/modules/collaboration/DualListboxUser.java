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
package de.fhdo.gui.admin.modules.collaboration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.zkoss.util.resource.Labels;
 
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
import org.zkoss.zul.Messagebox;
 /**
 *
 * @author Philipp Urbauer
 */
public class DualListboxUser extends Div implements IdSpace {
    private static final long serialVersionUID = 5183321186606483396L;
     
    @Wire
    private Listbox lbUser;
    @Wire
    private Listbox lbUserChoosen;
 
    private ListModelList<SvAssignmentData> candidateModel;
    private ListModelList<SvAssignmentData> chosenDataModel;
 
    public DualListboxUser() {
        Executions.createComponents("/gui/admin/modules/collaboration/v_dualListboxUser.zul", this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        Set<SvAssignmentData> choosenUserData = (Set<SvAssignmentData>) Executions.getCurrent().getAttribute("choosenUserData");
        if(!choosenUserData.isEmpty()){
            lbUserChoosen.setModel(chosenDataModel = new ListModelList<SvAssignmentData>(choosenUserData));
        }else{
            lbUserChoosen.setModel(chosenDataModel = new ListModelList<SvAssignmentData>());
        }
    }
 
    @Listen("onClick = #chooseBtn")
    public void chooseItem() {
        if(lbUserChoosen.getItemCount() != 1){
            Events.postEvent(new DualListboxUser.ChooseEvent(this, chooseOne()));
        }else{
            Messagebox.show(Labels.getLabel("onlySingleSelection"), Labels.getLabel("info"), Messagebox.OK, Messagebox.EXCLAMATION);
        }
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
    public void setModel(List<SvAssignmentData> candidate) {
        lbUser.setModel(this.candidateModel = new ListModelList<SvAssignmentData>(candidate));
        //chosenDataModel.clear();
    }
 
    /**
     * @return current chosen data list
     */
    public List<SvAssignmentData> getChosenDataList() {
        return new ArrayList<SvAssignmentData>(chosenDataModel);
    }
 
    private Set<SvAssignmentData> chooseOne() {
        Set<SvAssignmentData> set = candidateModel.getSelection();
        for (SvAssignmentData selectedItem : set) {
            chosenDataModel.add(selectedItem);
            candidateModel.remove(selectedItem);
        }
        return set;
    }
 
    private Set<SvAssignmentData> unchooseOne() {
        Set<SvAssignmentData> set = chosenDataModel.getSelection();
        for (SvAssignmentData selectedItem : set) {
            candidateModel.add(selectedItem);
            chosenDataModel.remove(selectedItem);
        }
        return set;
    }
 
    // Customized Event
    public class ChooseEvent extends Event {
        private static final long serialVersionUID = -7334906383953342976L;
 
        public ChooseEvent(Component target, Set<SvAssignmentData> data) {
            super("onChoose", target, data);
        }
    }
}