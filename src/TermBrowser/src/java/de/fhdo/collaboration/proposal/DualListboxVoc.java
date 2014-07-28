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
package de.fhdo.collaboration.proposal;
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
import org.zkoss.zul.Messagebox;
 /**
 *
 * @author Philipp Urbauer
 */
public class DualListboxVoc extends Div implements IdSpace {
    private static final long serialVersionUID = 5183321186606483396L;
    
    @Wire
    private Listbox lbVoc;
    @Wire
    private Listbox lbVocChoosen;
 
    private ListModelList<VocInfo> candidateModel;
    private ListModelList<VocInfo> chosenDataModel;
    
    public DualListboxVoc() {
        Executions.createComponents("/collaboration/proposal/v_dualListboxVoc.zul", this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        Set<VocInfo> choosenVocData = (Set<VocInfo>)Executions.getCurrent().getAttribute("choosenVocData");
        if(!choosenVocData.isEmpty()){
            lbVocChoosen.setModel(chosenDataModel = new ListModelList<VocInfo>(choosenVocData));
        }else{
            lbVocChoosen.setModel(chosenDataModel = new ListModelList<VocInfo>());
        }
    }
 
    @Listen("onClick = #chooseBtn")
    public void chooseItem() {
        if(lbVocChoosen.getItemCount() != 1){
            Events.postEvent(new ChooseEvent(this, chooseOne()));   
        }else{
            Messagebox.show("Es darf nur ein Eintrag ausgewählt werden!", "Information", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }
 
    @Listen("onClick = #removeBtn")
    public void unchooseItem() {
        
        Events.postEvent(new ChooseEvent(this, unchooseOne()));
    }
 
    /**
     * Set new candidate ListModelList.
     *
     * @param candidate
     *            is the data of candidate list model
     */
    public void setModel(List<VocInfo> candidate) {
        lbVoc.setModel(this.candidateModel = new ListModelList<VocInfo>(candidate));
        //chosenDataModel.clear();
    }
 
    /**
     * @return current chosen data list
     */
    public List<VocInfo> getChosenDataList() {
        return new ArrayList<VocInfo>(chosenDataModel);
    }
 
    private Set<VocInfo> chooseOne() {
        Set<VocInfo> set = candidateModel.getSelection();
        for (VocInfo selectedItem : set) {
            chosenDataModel.add(selectedItem);
            candidateModel.remove(selectedItem);
        }
        return set;
    }
 
    private Set<VocInfo> unchooseOne() {
        Set<VocInfo> set = chosenDataModel.getSelection();
        for (VocInfo selectedItem : set) {
            candidateModel.add(selectedItem);
            chosenDataModel.remove(selectedItem);
        }

        return set;
    }
 
    // Customized Event
    public class ChooseEvent extends Event {
        private static final long serialVersionUID = -7334906383953342976L;
 
        public ChooseEvent(Component target, Set<VocInfo> data) {
            super("onChoose", target, data);
        }
    }
}