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
package de.fhdo.gui.admin.modules.collaboration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Philipp Urbauer
 */
public class ReportingKollabData {
    
    //Header-Bezeichner
    public static final String GROUP_NAME_Header = "Name der Gruppe";
    public static final String CUSTODIAN_NAME_Header = "Verantwortlicher";
    public static final String NUMBER_PARTICIPANTS_Header = "#Teilnehmer";
    public static final String NUMBER_DISCUSSIONS_Header = "#Diskussionen";
    public static final String NUMBER_CodeSystems_Header = "#Code Systeme";
    public static final String NUMBER_ValueSets_Header = "#Value Sets";
    public static final String NUMBER_Concepts_Header = "#Konzepte";
    public static final String NUMBER_ConMemberships_Header = "#Konzept-Memberships";
    public static final String NUMBER_ACTIVITIES_PreHeader = "#Aktivitäten ";
    
    private String groupName;
    private String custodianName;
    private String numberParticipants;
    private String numberDiskussions;
    private String numberCodeSystems;
    private String numberValueSets;
    private String numberConcepts;
    private String numberConMemberships;
    private ArrayList<String> activitiesPerYear = new ArrayList<String>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCustodianName() {
        return custodianName;
    }

    public void setCustodianName(String custodianName) {
        this.custodianName = custodianName;
    }

    public String getNumberParticipants() {
        return numberParticipants;
    }

    public void setNumberParticipants(String numberParticipants) {
        this.numberParticipants = numberParticipants;
    }

    public String getNumberDiskussions() {
        return numberDiskussions;
    }

    public void setNumberDiskussions(String numberDiskussions) {
        this.numberDiskussions = numberDiskussions;
    }

    public String getNumberCodeSystems() {
        return numberCodeSystems;
    }

    public void setNumberCodeSystems(String numberCodeSystems) {
        this.numberCodeSystems = numberCodeSystems;
    }

    public String getNumberValueSets() {
        return numberValueSets;
    }

    public void setNumberValueSets(String numberValueSets) {
        this.numberValueSets = numberValueSets;
    }

    public String getNumberConcepts() {
        return numberConcepts;
    }

    public void setNumberConcepts(String numberConcepts) {
        this.numberConcepts = numberConcepts;
    }

    public String getNumberConMemberships() {
        return numberConMemberships;
    }

    public void setNumberConMemberships(String numberConMemberships) {
        this.numberConMemberships = numberConMemberships;
    }

    public ArrayList<String> getActivitiesPerYear() {
        return activitiesPerYear;
    }

    public void setActivitiesPerYear(ArrayList<String> activitiesPerYear) {
        this.activitiesPerYear = activitiesPerYear;
    }
}
