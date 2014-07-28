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
/**
 *
 * @author Philipp Urbauer
 */
public class SvAssignmentData {
    
    private String termName;
    private Long classId;
    private String classname;
    private Long assignedTermId;
    private Long collaborationuserId;
    private String firstName = "";
    private String name = "";
    private String username = "";
    private String organisation = "";
 
    public SvAssignmentData(){
    
    }
    
    public SvAssignmentData(String firstName, String name, String organisation) {
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
    }

    public SvAssignmentData(Long collaborationuserId, String firstName, String name, String organisation) {
        this.collaborationuserId = collaborationuserId;
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
    }

    public Long getCollaborationuserId() {
        return collaborationuserId;
    }

    public void setCollaborationuserId(Long collaborationuserId) {
        this.collaborationuserId = collaborationuserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getAssignedTermId() {
        return assignedTermId;
    }

    public void setAssignedTermId(Long assignedTermId) {
        this.assignedTermId = assignedTermId;
    }
}
