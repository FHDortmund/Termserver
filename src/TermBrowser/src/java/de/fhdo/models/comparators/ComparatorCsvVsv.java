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

import de.fhdo.helper.UtilHelper;
import de.fhdo.models.TreeNode;
import org.zkoss.zul.TreeitemComparator;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class ComparatorCsvVsv extends TreeitemComparator{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean     ascending = false;   
    
    public ComparatorCsvVsv(boolean asc){
        ascending = asc;      
    }
    
    @Override
    public int compare(Object o1, Object o2) {  
        String s1="", s2="";
        o1 = ((TreeNode)o1).getData();
        o2 = ((TreeNode)o2).getData();
        
        if(o1 instanceof CodeSystemVersion)
            s1 = UtilHelper.getDisplayNameLong(o1);
        else if(o1 instanceof ValueSetVersion)
            s1 = UtilHelper.getDisplayNameLong(o1);    
        else if (o1 instanceof ValueSet)
            s1 = UtilHelper.getDisplayNameLong(o1);      
        else if (o1 instanceof CodeSystem)
            s1 = UtilHelper.getDisplayNameLong(o1);       
        
        if(o2 instanceof CodeSystemVersion)
            s2 = UtilHelper.getDisplayNameLong(o2);      
        else if(o2 instanceof ValueSetVersion)
            s2 = UtilHelper.getDisplayNameLong(o2);       
        else if (o2 instanceof ValueSet)
            s2 = UtilHelper.getDisplayNameLong(o2);   
        else if (o2 instanceof CodeSystem)
            s2 = UtilHelper.getDisplayNameLong(o2);  
     
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int v = s1.compareTo(s2);        
        return ascending ? v: -v;            
    }    
}