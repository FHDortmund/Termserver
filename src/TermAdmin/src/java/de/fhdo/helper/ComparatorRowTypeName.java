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
package de.fhdo.helper;

import de.fhdo.list.GenericListRowType;
import java.util.Comparator;

/**
 *
 * @author Becker
 */
public class ComparatorRowTypeName implements Comparator{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean asc = false;
    
    public ComparatorRowTypeName(boolean ascending){
        asc = ascending;
    }
    
    public int compare(Object o1, Object o2) {
        GenericListRowType csmv1 = (GenericListRowType)o1,
                           csmv2 = (GenericListRowType)o2;
        
        int v = ((String)csmv1.getCells()[1].getData()).compareTo(((String)csmv2.getCells()[1].getData()));
        
        return asc ? v: -v;
    }  
}
