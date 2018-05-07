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
public class PrivilegeComparatorDiscGroupInfo  implements Comparator{
 
    public static final int DG_INFO_GROUP_NAME = 0;
    public static final int DG_INFO_GROUP_HEAD = 1;
    
    private boolean asc = false;
    private int mode = -1;
    public PrivilegeComparatorDiscGroupInfo(boolean ascending, int choosenMode){
        asc = ascending;
        mode = choosenMode;
    }
    
    public int compare(Object o1, Object o2) {
        PrivilegeDiscGroupInfo dgvi1 = (PrivilegeDiscGroupInfo)o1,
                                dgvi2 = (PrivilegeDiscGroupInfo)o2;
        
        int v = -1;
        if(mode == DG_INFO_GROUP_NAME){
            v = dgvi1.getDiscussionGroupName().compareTo(dgvi2.getDiscussionGroupName());
        }
        if (mode == DG_INFO_GROUP_HEAD){
            v = dgvi1.getDiscussionGroupHead().compareTo(dgvi2.getDiscussionGroupHead());
        }

        return asc ? v: -v;
    } 
}
