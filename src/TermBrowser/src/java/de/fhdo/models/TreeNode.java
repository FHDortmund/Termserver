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
package de.fhdo.models;

import de.fhdo.helper.DeepLinkHelper;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger; 
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ConceptValueSetMembership;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author mathias.aschhoff
 */
public class TreeNode extends DefaultTreeNode {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private Object          customData;
    private boolean         linkedConcept     = false;       
    private boolean         crossMapping      = false;
    private boolean         hasLinkedConcepts = false;   
    private TreeModelCSEV   treeModelCSEV     = null;
    private String          deepLink          = "";
    private CodeSystemVersion sourceCSV       = null;
    private ConceptValueSetMembership cvsm = null;
    private ListConceptAssociationsResponse.Return  responseListConceptAssociations = null;   
    
    public TreeNode(Object data) {
        super(data, new ArrayList());
        
        if(data instanceof CodeSystemEntityVersion){
            CodeSystemEntityVersion csev = (CodeSystemEntityVersion)data;
            
            // OrderNr: set concept valueset membership for ComparatorOrderNr
            if(csev.getConceptValueSetMemberships() != null && csev.getConceptValueSetMemberships().isEmpty() == false)
                cvsm =csev.getConceptValueSetMemberships().get(0);           
        }
    }

    public TreeNode(Object data, List children) {
        super(data, children);
    }        

    public Object getCustomData() {
        return customData;
    }

    public void setCustomData(Object customData) {
        this.customData = customData;
    }     
    
    private void createDeepLink(){
        deepLink = getDeepLinkString("","", new ArrayList<String>());
    }
    
    private String composeDeepLinkString(String sLoadType, String sLoadName, ArrayList<String> listConcepts){
        // String bauen
        String sheme      = Executions.getCurrent().getScheme() + "://";
        String server     = Sessions.getCurrent().getServerName(); 
        String port       = (Executions.getCurrent().getServerPort() == 80 ) ? "" : (":" + Executions.getCurrent().getServerPort());       
        String contentPath= Executions.getCurrent().getContextPath();
        String uriMain    = Executions.getCurrent().getDesktop().getRequestPath() + "?";
        String concepts   = "";

        Iterator<String> itRl = listConcepts.iterator();
        int i = 1;
        while(itRl.hasNext()){                       
            concepts += "c" + String.valueOf(i++) + "=" + itRl.next() + "&";
        }

        String deepLinkString = sheme + server + port + contentPath + uriMain + sLoadType + sLoadName + concepts;

        // letztes & oder ? abschneiden
        deepLinkString = deepLinkString.substring(0, deepLinkString.length()-1);
        
        return deepLinkString;
    }
    
    private void copyStringToClipboard(String toClipboard){
        Toolkit         toolkit   = Toolkit.getDefaultToolkit();
        Clipboard       clipboard = toolkit.getSystemClipboard();
        StringSelection strSel    = new StringSelection(toClipboard);
        clipboard.setContents(strSel, null);
    }
    
    public void showDeepLinkInMessagebox(){        
        try {
            Messagebox.show("DeepLink:" + "\n\n" + getDeepLink());
        } catch (Exception ex) {
            Logger.getLogger(TreeNode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }        
    
    public void copyDeepLinkToClipboard(){        
        copyStringToClipboard(getDeepLink());
    }
    
    private String getDeepLinkString(String sLoadType, String sLoadName, ArrayList<String> listConcepts) {
        Object data            = getData(); 
        String sConcepts       = "";
        String clipboardString = "";
        
        // DeepLink zu Konzepten
        if(listConcepts.isEmpty() == false){            
            // Oberstes Element => Deeplink bauen
            if(getData() == null){
                // Liste umdrehen
                ArrayList<String> reveredList = new ArrayList<String>();
                Iterator<String> it = listConcepts.iterator();

                while(it.hasNext()){                       
                    reveredList.add(0, it.next());
                }
                
                if(treeModelCSEV.getSource() instanceof CodeSystemVersion){
                    sLoadType = "loadType=CodeSystem&";
                    sLoadName = "loadName=" + DeepLinkHelper.getConvertedString(((CodeSystemVersion) treeModelCSEV.getSource()).getName(), false) + "&";                    
                }
                else if (treeModelCSEV.getSource() instanceof ValueSetVersion){
                    sLoadType = "loadType=ValueSet&";
                    sLoadName = "loadName=" + DeepLinkHelper.getConvertedString(((ValueSetVersion) treeModelCSEV.getSource()).getValueSet().getName(), false) + "&";                    
                    try {
                        Messagebox.show("Hinweis: Das Erstellen von Deep Links in Konzepten von Value Sets ist nur für die aktuelle Value Set Version möglich!");
                    } catch (Exception ex) {Logger.getLogger(TreeNode.class.getName()).log(Level.SEVERE, null, ex);}
                }                                
                
                clipboardString = composeDeepLinkString(sLoadType, sLoadName, reveredList);
                
                return clipboardString;
            }              
        }        
  
        if(data instanceof CodeSystem){            
            sLoadType = "loadType=CodeSystem&";
            sLoadName = "loadName=" + DeepLinkHelper.getConvertedString(((CodeSystem) data).getName(), false) + "&";                       
        }
        else if(data instanceof CodeSystemVersion){
            sLoadType = "loadType=CodeSystem&";
            sLoadName = "loadName=" + DeepLinkHelper.getConvertedString(((CodeSystemVersion) data).getName(), false) + "&";            
        }
        else if(data instanceof ValueSet){
            sLoadType = "loadType=ValueSet&";
            sLoadName = "loadName=" + DeepLinkHelper.getConvertedString(((ValueSet) data).getName(), false) + "&";            
        }
        else if(data instanceof ValueSetVersion){ 
            sLoadType = "loadType=ValueSet&";
            sLoadName = "loadName=" + DeepLinkHelper.getConvertedString(((ValueSetVersion) data).getValueSet().getName(), false) + "&";
            /*try {
                Messagebox.show("Hinweis: Das Erstellen von Deep Links zu Value Set Versions ist für die akutelle Version möglich!");
            } catch (Exception ex) {Logger.getLogger(TreeNode.class.getName()).log(Level.SEVERE, null, ex);}*/
        }
        else if(data instanceof CodeSystemEntityVersion){         
            sLoadType = "loadType=CodeSystem&";
            
            sConcepts = DeepLinkHelper.getConvertedString(((CodeSystemEntityVersion)data).getCodeSystemConcepts().get(0).getTerm(), false); 
            if(sConcepts.isEmpty() == false)
                listConcepts.add(sConcepts); 
            
            if(getParent() != null)
                return ((TreeNode)getParent()).getDeepLinkString(sLoadType, sLoadName, listConcepts);                          
        }
        else{
            return "";
        }                               
        
        clipboardString = composeDeepLinkString(sLoadType, sLoadName, listConcepts);
        
        return clipboardString;                         
    }       

    public boolean isLinkedConcept() {
        return linkedConcept;
    }

    public void setLinkedConcept(boolean crossMapping) {
        this.linkedConcept = crossMapping;
    }

    public boolean isCrossMapping() {
        return crossMapping;
    }

    public void setCrossMapping(boolean crossMapping) {
        this.crossMapping = crossMapping;
    }
    
    public boolean hasLinkedConcepts(){
        return hasLinkedConcepts;
    }
    
    public void setHasLinkedConcepts(boolean b){
        hasLinkedConcepts = b;
    }

    public TreeModelCSEV getTreeModelCSEV() {
        return treeModelCSEV;
    }

    public void setTreeModelCSEV(TreeModelCSEV treeModelCSEV) {
        this.treeModelCSEV = treeModelCSEV;
    }

    public String getDeepLink() {
        if (deepLink.isEmpty())
            createDeepLink();
        return deepLink;
    }

    public CodeSystemVersion getSourceCSV() {
        return sourceCSV;
    }

    public void setSourceCSV(CodeSystemVersion sourceCSV) {
        this.sourceCSV = sourceCSV;
    }

    public ListConceptAssociationsResponse.Return getResponseListConceptAssociations() {
        return responseListConceptAssociations;
    }

    public void setResponseListConceptAssociations(ListConceptAssociationsResponse.Return response) {
        this.responseListConceptAssociations = response;
    }

    public static org.apache.log4j.Logger getLogger() {
        return logger;
    }

    public static void setLogger(org.apache.log4j.Logger logger) {
        TreeNode.logger = logger;
    }

    public ConceptValueSetMembership getCvsm() {
        return cvsm;
    }

    public void setCvsm(ConceptValueSetMembership cvsm) {
        this.cvsm = cvsm;
    }
}