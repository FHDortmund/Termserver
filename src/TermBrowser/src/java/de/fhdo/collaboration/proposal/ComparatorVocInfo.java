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
package de.fhdo.collaboration.proposal;

import java.util.Comparator;

/**
 *
 * @author Philipp Urbauer
 */
public class ComparatorVocInfo  implements Comparator{
 
    public static final int VOC_INFO_VOCABULARY_NAME = 0;
    public static final int VOC_INFO_VERSION_NAME = 1;
    
    private boolean asc = false;
    private int mode = -1;
    public ComparatorVocInfo(boolean ascending, int choosenMode){
        asc = ascending;
        mode = choosenMode;
    }
    
    public int compare(Object o1, Object o2) {
        VocInfo dgvi1 = (VocInfo)o1,
                                dgvi2 = (VocInfo)o2;
        
        int v = -1;
        if(mode == VOC_INFO_VOCABULARY_NAME){
            v = dgvi1.getVocabularyName().compareTo(dgvi2.getVocabularyName());
        }
        if (mode == VOC_INFO_VERSION_NAME){
            v = dgvi1.getVersionName().compareTo(dgvi2.getVersionName());
        }

        return asc ? v: -v;
    } 
}
