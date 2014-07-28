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
import types.termserver.fhdo.de.MetadataParameter;

/**
 *
 * @author Becker
 */
public class ListitemRendererMetadataParameter implements ListitemRenderer{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public void render(Listitem lstm, Object o, int index) throws Exception {
        MetadataParameter mdp = (MetadataParameter)o;        
        
        Listcell cellName           = new Listcell(mdp.getParamName());
        Listcell cellDatatype       = new Listcell(mdp.getParamDatatype()); 
        Listcell cellParameterType  = new Listcell(mdp.getMetadataParameterType());   
        
        lstm.appendChild(cellName);
        lstm.appendChild(cellDatatype);
        lstm.appendChild(cellParameterType);
    }
}
