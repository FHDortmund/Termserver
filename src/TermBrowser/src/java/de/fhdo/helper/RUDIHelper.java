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

import de.fhdo.models.TreeModelCS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Becker
 */
public class RUDIHelper {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    static int  RUDI_ACTION_READ   = 1,
                RUDI_ACTION_UPDATE = 2,
                RUDI_ACTION_DELETE = 3,
                RUDI_ACTION_INSERT = 4;
    // Map nach CSV mit weiterer Map, welcher user (long) welche Aktionen List<int> durchführen darf   
    static protected HashMap<Long, HashMap<Long, ArrayList<Integer>>> userMap = new HashMap<Long, HashMap<Long, ArrayList<Integer>>>();
    
    static public void createRUDIEntry(long userId){
        HashMap<Long, ArrayList<Integer>> csvMap = new HashMap<Long, ArrayList<Integer>>();
        ArrayList<Integer> listRights  = new ArrayList<Integer>();

        // Durchlaufe alle CS,VS,... und schaue nach den Rechten
        Iterator<CodeSystemVersion> itCSV = TreeModelCS.getInstance().getCsvList().iterator();
        while(itCSV.hasNext()){         
            CodeSystemVersion csv = itCSV.next();
            listRights.clear();

            // Suche nach Rechten
            if(csv.isUnderLicence()){
                // Rechte Laden  WS aufruf
                if(false){
                    // Rechte verteilen
                }
            }
            else{
                listRights.add(RUDI_ACTION_READ);
                listRights.add(RUDI_ACTION_UPDATE);
                listRights.add(RUDI_ACTION_DELETE);
                listRights.add(RUDI_ACTION_INSERT);            
            } 
            
            // Je eine Map<versionId, Rechteliste> in die Map einfügen
            csvMap.put(csv.getPreviousVersionId(), listRights);
        }        

        // CSVMap dem User zuordnen
        userMap.put(userId, csvMap);             
    }
    
    static public boolean actionAllowed(long userId, long versionId, int action){                
        boolean allowed = false;
        
        if(userMap.containsKey(userId) ){
            if(userMap.get(userId).containsKey(versionId)){
                if(userMap.get(userId).get(versionId).contains(action)){
                    allowed = true;    
                }
                else{
                    // Meldung: Aktion nicht in Liste => nicht erlaubt
                    allowed = false;
                }                    
            }
            else{
                try {
                    Messagebox.show(Labels.getLabel("rudiHelper.csvNotInAccessList"));
                    // Meldung: User nicht in Liste
                } catch (Exception ex) {
                    Logger.getLogger(RUDIHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
        }
        else{
            try {
                Messagebox.show(Labels.getLabel("rudiHelper.userNotInAccessList"));
                // Meldung: User nicht in Liste
            } catch (Exception ex) {
                Logger.getLogger(RUDIHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        return allowed;              
    }
    
}
