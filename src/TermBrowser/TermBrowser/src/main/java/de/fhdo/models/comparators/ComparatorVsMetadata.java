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

import java.util.Comparator;
import types.termserver.fhdo.de.ValueSetMetadataValue;

/**
 *
 * @author Becker
 */
public class ComparatorVsMetadata implements Comparator{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private boolean asc = false;
    
    public ComparatorVsMetadata(boolean ascending){
        asc = ascending;
    }
    
    public int compare(Object o1, Object o2) {
        ValueSetMetadataValue vsmv1 = (ValueSetMetadataValue)o1,
                                vsmv2 = (ValueSetMetadataValue)o2;
        
        int v = vsmv1.getMetadataParameter().getParamName().compareTo(vsmv2.getMetadataParameter().getParamName());
        
        return asc ? v: -v;
    }  
}
