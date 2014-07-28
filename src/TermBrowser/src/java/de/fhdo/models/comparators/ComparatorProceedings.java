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

import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListRowType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import types.termserver.fhdo.de.CodeSystemMetadataValue;

/**
 *
 * @author Becker
 */
public class ComparatorProceedings implements Comparator{
    private boolean asc = false;
    
    public ComparatorProceedings(boolean ascending){
        asc = ascending;
    }
    
    public int compare(Object o1, Object o2) {
        GenericListRowType row1 = (GenericListRowType)o1,
                                row2 = (GenericListRowType)o2;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GenericListCellType[] arr1 = row1.getCells();
        GenericListCellType[] arr2 = row2.getCells();
        Date dat1 = null;
        Date dat2 = null;
        try {
            dat1 = sdf.parse((String)arr1[4].getData());
            dat2 = sdf.parse((String)arr2[4].getData());
        } catch (Exception ex) {
            dat1 = null;
            dat2 = null;
        }
        int v;
        if(dat1 == null || dat2 == null){
            v=0;
        }else{
            v = dat1.compareTo(dat2);
        }
        
        return asc ? v: -v;
    }  
}
