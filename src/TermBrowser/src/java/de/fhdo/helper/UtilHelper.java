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

import java.text.SimpleDateFormat;
import java.util.Date;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class UtilHelper {
  
    public static String getDisplayNameLong(Object o){
        String s = "x";
        
        if(o instanceof CodeSystem)
            s = ((CodeSystem)o).getName();
        else if (o instanceof CodeSystemVersion){
            CodeSystemVersion csv = (CodeSystemVersion)o;
            
            if(csv.getName().contains(csv.getCodeSystem().getName()) == false)                             
                s = csv.getCodeSystem().getName() + ": "+csv.getName();                                                                         
            else
                s = csv.getName();
        }
        else if(o instanceof ValueSet){
            s = ((ValueSet)o).getName();
        }
        else if (o instanceof ValueSetVersion){
            ValueSetVersion vsv = (ValueSetVersion)o;
            Date d = vsv.getInsertTimestamp().toGregorianCalendar().getTime();  
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");        
            s = vsv.getValueSet().getName() + ": " +  sdf.format(d);
        }
        else if (o instanceof DomainValue){
            s = ((DomainValue)o).getDomainDisplay();
        }     
        else if(o instanceof CodeSystemEntityVersion){
            throw new UnsupportedOperationException("displayName of CSEV not implemented yet!");
        }
        else if(o instanceof CodeSystemEntityVersionAssociation){
            throw new UnsupportedOperationException("displayName of CSEVA not implemented yet!");
        }
        else if(o instanceof CodeSystemConcept){
            throw new UnsupportedOperationException("displayName of CSC not implemented yet!");
        }
        else if(o instanceof CodeSystemConceptTranslation){
            throw new UnsupportedOperationException("displayName of CSCT not implemented yet!");
        }
        
        return s;
    }
}
