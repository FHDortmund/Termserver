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
package de.fhdo.collaboration.desktop.proposal.privilege;

import java.util.Comparator;

/**
 *
 * @author Philipp Urbauer
 */
public class PrivilegeComparatorUserInfo  implements Comparator{
 
    public static final int USER_INFO_FIRST_NAME = 0;
    public static final int USER_INFO_NAME = 1;
    public static final int USER_INFO_ORGANISTION = 2;
    
    private boolean asc = false;
    private int mode = -1;
    
    public PrivilegeComparatorUserInfo(boolean ascending, int choosenMode){
        asc = ascending;
        mode = choosenMode;
    }
    
    public int compare(Object o1, Object o2) {
        PrivilegeUserInfo dcui1 = (PrivilegeUserInfo)o1,
                                dcui2 = (PrivilegeUserInfo)o2;
        
        int v = -1;
        if(mode == USER_INFO_FIRST_NAME){
            v = dcui1.getFirstName().compareTo(dcui2.getFirstName());
        }
        if(mode == USER_INFO_NAME){
            v = dcui1.getName().compareTo(dcui2.getName());
        }
        if(mode == USER_INFO_ORGANISTION){
            v = dcui1.getOrganisation().compareTo(dcui2.getOrganisation());
        }
      
        return asc ? v: -v;
    } 
}
