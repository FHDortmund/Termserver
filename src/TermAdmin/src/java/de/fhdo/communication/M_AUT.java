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
package de.fhdo.communication;

//import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.classes.Collaborationuser;

/**
 *
 * @author PU
 */
public class M_AUT {
 
    public static final String MAIL_START = "Sehr geehrte/r Terminologieserver BenutzerIn, \n\n";
    public static final String STATUS_CHANGE_SUBJECT = "Änderung des Status eines Vorschlags";
    public static final String SV_ASSIGNMENT_SUBJECT = "Zuweisung Inhaltsverwaltung";
    public static final String PROPOSAL_SUBJECT = "Terminologie Vorschlag";
    
    private String MAIL_FOOTER;
    private String WEBLINK_COLLAB;
    private static M_AUT instance = null;
    
    public M_AUT(){
    
        getWeblink();
        getFooter();
    }
    
    public String getSvAssignementText(String terminologie){
    
        String s = "Ihnen wurde die Terminologie: " + terminologie + " \n"
                    + "zur Inhaltsverwaltung zugewiesen.";
        return s;
    }
    
    public String getUserName(Collaborationuser u){
    
        String s = "";
        if(u.getFirstName() != null && !u.getFirstName().equals("") && u.getName() != null && !u.getName().equals("")){
        
            s = u.getFirstName() + " " + u.getName();
        }else{
            s = u.getUsername();
        }
        
        return s;
    }
    
    public String getMailFooter(){
    
        return this.MAIL_FOOTER;
    }
    
    private void getWeblink(){
    
//        WEBLINK_COLLAB = DBSysParam.instance().getStringValue("weblink", null, null) + "/gui/info/enquiry.zul";
      WEBLINK_COLLAB = "";
    }
    
    private void getFooter(){
        
      MAIL_FOOTER = "";
//        MAIL_FOOTER =       "\n\nMit freundlichen Grüßen,\n" +
//                            "Ihr Terminologieserver - Team" +
//                            "\n------------------------------------------------\n" +
//                            "Liebe Benutzerin/lieber Benutzer,\n\n" +
//                            "Über diese E-Mail Adresse können keine Anfragen bearbeitet werden. Für Ihre Anliegen haben wir ein Webformular eingerichtet.\n" +
//                            "Daher bitten wir, Anfragen nicht per E-Mail sondern über das Formular zu übermitteln: " + WEBLINK_COLLAB + "\n\n" +
//                            "Mit freundlichen Grüßen,\n" +
//                            "Ihr Terminologieserver - Team";
    }
    
    public String getProposalText(String vocabularyName, String contentType, String description){
    
        String s = "Ihr Vorschlag zu der Terminologie " + vocabularyName + " (" + contentType + ") \n"
                 + "wurde im System aufgenommen.\n Sollten sie NICHT der InhaltsverwalterIn der Terminologie \n"
                 + "sein, wurde auch ein eMail an den/die TerminologieverwalterIn versandt.\n\n"
                 + "Ihr Ansuchen: " + description;
                 
        return s;
    }
    
    public String getProposalSelbstVerwText(String vocabularyName, String contentType, String description){
    
        String s = "Zu der von Ihnen verwalteten Terminologie " + vocabularyName + " (" + contentType + ") \n"
                 + "wurde ein Vorschlag im System aufgenommen.\n"
                 + "Das Ansuchen: " + description;
        return s;
    }
    
    public static M_AUT getInstance()
    {
      if (instance == null)
        instance = new M_AUT();

      return instance;
    }
}
