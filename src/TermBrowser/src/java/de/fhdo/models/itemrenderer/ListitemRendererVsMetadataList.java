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
package de.fhdo.models.itemrenderer;

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.ValueSetMetadataValue;

/**
 *
 * @author Becker
 */
public class ListitemRendererVsMetadataList implements ListitemRenderer {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean editableMetadataList;

    public ListitemRendererVsMetadataList(boolean editableMetadataList) {
        this.editableMetadataList = editableMetadataList;
    }
    
    public void render(Listitem lstm, Object o, int index) throws Exception {
        ValueSetMetadataValue vsmv = (ValueSetMetadataValue)o;        
        
        Listcell cellMetadata = new Listcell();
        Listcell cellValue    = new Listcell();
        
        cellMetadata.setLabel(vsmv.getMetadataParameter().getParamName());
        if(editableMetadataList){
            Textbox textBox = new Textbox();
            textBox.setText(vsmv.getParameterValue());
            textBox.setReadonly(true);
            cellValue.appendChild(textBox);
        }else{
            cellValue.setLabel(vsmv.getParameterValue());
        }
        
        lstm.appendChild(cellMetadata);
        lstm.appendChild(cellValue);
    }    
}
