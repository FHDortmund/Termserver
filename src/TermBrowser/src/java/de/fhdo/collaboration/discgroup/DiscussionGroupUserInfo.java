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
package de.fhdo.collaboration.discgroup;
/**
 *
 * @author Philipp Urbauer
 */
public class DiscussionGroupUserInfo {
    private Long collaborationuserId;
    private String firstName = "";
    private String name = "";
    private String organisation = "";
 
    public DiscussionGroupUserInfo(String firstName, String name, String organisation) {
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
    }

    public DiscussionGroupUserInfo(Long collaborationuserId, String firstName, String name, String organisation) {
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
}
