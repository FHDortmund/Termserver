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

import de.fhdo.models.comparators.ComparatorStrings;
import de.fhdo.terminologie.ws.search.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.ListDomainValuesResponse;
import de.fhdo.terminologie.ws.search.OverallErrorCategory;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.Domain;
import types.termserver.fhdo.de.DomainValue;

/**
 *
 * @author PU
 */
public class ValidityRangeHelper {    
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static HashMap<String, String> validityRanges = null;
    
    public static Long getValidityRangeIdByName(String validityRange){        
        checkForNull();
        
        if(validityRange == null || validityRange.trim().isEmpty())
            return Long.valueOf((long)-1);
        
        for(String key : ValidityRangeHelper.getValidityRangeTable().keySet()){
            if(ValidityRangeHelper.getValidityRangeTable().get(key).compareToIgnoreCase(validityRange) == 0)
                return Long.valueOf(key);
        }
        return Long.valueOf((long)-1);        
    }
    
    public static HashMap<String, String> getValidityRangeTable() {        
        checkForNull();
        
        return validityRanges;
    }
    
    public static ListModelList getListModelList(){        
        checkForNull();
        
        List listValidityRange = new ArrayList<String>();
        for(String validityRange : ValidityRangeHelper.getValidityRangeTable().values()){
            listValidityRange.add(validityRange);
        }
        ListModelList lm2 = new ListModelList(listValidityRange);
        Comparator comparator = new ComparatorStrings();
        lm2.sort(comparator, true);        
        return lm2;
    }
    
    public static String getValidityRangeNameById(Long domainValueId){    
        checkForNull();
        
        String res = validityRanges.get(String.valueOf(domainValueId));
        if(res != null){
            return res;
        }else{
            return "";
        }
    }
    
    private static void checkForNull(){
        
        if(validityRanges == null)
            createValidityRangeTables();
    }
    
    private static void createValidityRangeTables(){               
        validityRanges = new HashMap<String, String>();

        ListDomainValuesRequestType parameter   = new ListDomainValuesRequestType();

        parameter.setDomain(new Domain());
        parameter.getDomain().setDomainId((long)9); // 9 = Validity Frage: Range Extern konfigurierbar oder hardcoded...

        ListDomainValuesResponse.Return response = WebServiceHelper.listDomainValues(parameter);

        if(response != null && response.getReturnInfos().getStatus() == Status.OK){
            if(response.getReturnInfos().getOverallErrorCategory() != OverallErrorCategory.INFO){
                try {
                    Messagebox.show(Labels.getLabel("validityRangeHelper.loadValidityRangeFailed") + "\n\n" + response.getReturnInfos().getMessage());
                } catch (Exception ex) {logger.error("ValidityRangeHelper.java Error loading ValidityRange: " + ex);}
            }
            else{
                Iterator<DomainValue> it = response.getDomainValues().iterator();
                while(it.hasNext()){
                    DomainValue dv = it.next();
                    validityRanges.put(String.valueOf(dv.getDomainValueId()), dv.getDomainDisplay());
                }
            }
        }                            
    }
}
