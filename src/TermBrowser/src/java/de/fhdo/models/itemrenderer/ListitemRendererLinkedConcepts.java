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
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;

/**
 *
 * @author Becker
 */
public class ListitemRendererLinkedConcepts implements ListitemRenderer {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private long versionId;
    
    public ListitemRendererLinkedConcepts(long id){
        versionId = id;
    }
    
    public void render(Listitem lstm, Object o, int index) throws Exception {  
        CodeSystemEntityVersionAssociation cseva = (CodeSystemEntityVersionAssociation)o;                        
        
        Listcell cellAssociationType = new Listcell();
        Listcell cellEntity          = new Listcell(); 
        
        CodeSystemEntityVersion csev1 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1(),
                                csev2 = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2(),
                                csev  = null;
        
        if(csev1 != null && csev1.getVersionId().equals(versionId) == false)
            csev = csev1;                      
        else if(csev2 != null && csev2.getVersionId().equals(versionId) == false)
            csev = csev2;                           
                
        if(cseva.getLeftId().equals(versionId))
            cellAssociationType.setLabel(cseva.getAssociationType().getReverseName());        
        else
            cellAssociationType.setLabel(cseva.getAssociationType().getForwardName());                
        
        if(csev != null)
            cellEntity.setLabel(csev.getCodeSystemConcepts().get(0).getTerm());        
        
        lstm.appendChild(cellAssociationType);
        lstm.appendChild(cellEntity);        
    }        
}