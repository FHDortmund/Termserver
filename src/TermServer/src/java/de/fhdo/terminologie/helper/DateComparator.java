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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import java.util.Comparator;


/**
 *
 * @author Philipp Urbauer
 */
public class DateComparator implements Comparator{
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    
    //desc date sort
    public int compare(Object o1, Object o2) {
        CodeSystemEntityVersion csev1 = (CodeSystemEntityVersion)o1,
                                csev2 = (CodeSystemEntityVersion)o2;
        
        long t1 = csev1.getInsertTimestamp().getTime();
        long t2 = csev2.getInsertTimestamp().getTime();
        
        if(t2 > t1)
            return 1;
        else if(t1 > t2)
            return -1;
        else
            return 0;
    }  
}
