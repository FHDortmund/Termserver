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
package de.fhdo.collaboration.desktop.proposal;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussiongroup;
import de.fhdo.collaboration.db.classes.Privilege;

/**
 *
 * @author PU
 */
public class PrivilegienListData {
    
    private Collaborationuser user = null;
    private Boolean isCreator = false;
    private Boolean isAdmin = false;
    private Discussiongroup discussiongroup = null;
    private Privilege privilege;

    public Collaborationuser getUser() {
        return user;
    }

    public void setUser(Collaborationuser user) {
        this.user = user;
    }

    public Discussiongroup getDiscussiongroup() {
        return discussiongroup;
    }

    public void setDiscussiongroup(Discussiongroup discussiongroup) {
        this.discussiongroup = discussiongroup;
    }

    public Boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(Boolean isCreator) {
        this.isCreator = isCreator;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }
}
