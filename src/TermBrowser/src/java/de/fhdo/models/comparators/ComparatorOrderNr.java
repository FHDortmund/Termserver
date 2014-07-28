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
package de.fhdo.models.comparators;

import de.fhdo.models.TreeNode;
import java.util.Comparator;
import types.termserver.fhdo.de.CodeSystemEntityVersion;

/**
 *
 * @author Philipp Urbauer
 */
public class ComparatorOrderNr implements Comparator{
    private boolean asc;
    
    public ComparatorOrderNr(boolean asc){
        this.asc = asc;
    }
    
    @Override
    public int compare(Object o1, Object o2) {
        if(o1 instanceof TreeNode && o2 instanceof TreeNode){
            TreeNode tn1 = (TreeNode)o1;
            TreeNode tn2 = (TreeNode)o2;

            int v = tn1.getCvsm().getOrderNr().compareTo(tn2.getCvsm().getOrderNr());
            return asc ? v: -v;
        }
        else if(o1 instanceof CodeSystemEntityVersion && o1 instanceof CodeSystemEntityVersion){
            CodeSystemEntityVersion csev1 = (CodeSystemEntityVersion)o1;
            CodeSystemEntityVersion csev2 = (CodeSystemEntityVersion)o2;
            if(csev1.getConceptValueSetMemberships().isEmpty() == false && csev2.getConceptValueSetMemberships().isEmpty() == false){
                int v = csev1.getConceptValueSetMemberships().get(0).getOrderNr().compareTo(csev2.getConceptValueSetMemberships().get(0).getOrderNr());
                return asc ? v: -v;
            }
        }
        return -1;
    }  
}
