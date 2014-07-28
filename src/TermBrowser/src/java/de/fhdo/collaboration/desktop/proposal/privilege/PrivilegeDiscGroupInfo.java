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
package de.fhdo.collaboration.desktop.proposal.privilege;
/**
 *
 * @author Philipp Urbauer
 */
public class PrivilegeDiscGroupInfo{
    private Long discussionGroupId;
    private Long discussionGroupHeadId;
    private String discussionGroupName = "";
    private String discussionGroupHead = "";
    private Boolean privExists;
    private Long privId;
 
    public PrivilegeDiscGroupInfo(String discussionGroupName, String discussionGroupHead) {
        this.discussionGroupName = discussionGroupName;
        this.discussionGroupHead = discussionGroupHead;
    }

    public PrivilegeDiscGroupInfo(Long discussionGroupId, Long discussionGroupHeadId, String discussionGroupName, String discussionGroupHead, Boolean privExists, Long privId) {
        this.discussionGroupId = discussionGroupId;
        this.discussionGroupHeadId = discussionGroupHeadId;
        this.discussionGroupName = discussionGroupName;
        this.discussionGroupHead = discussionGroupHead;
        this.privExists = privExists;
        this.privId = privId;
    }

    public Long getDiscussionGroupId() {
        return discussionGroupId;
    }

    public void setDiscussionGroupId(Long discussionGroupId) {
        this.discussionGroupId = discussionGroupId;
    }

    public Long getDiscussionGroupHeadId() {
        return discussionGroupHeadId;
    }

    public void setDiscussionGroupHeadId(Long discussionGroupHeadId) {
        this.discussionGroupHeadId = discussionGroupHeadId;
    }

    public String getDiscussionGroupName() {
        return discussionGroupName;
    }

    public void setDiscussionGroupName(String discussionGroupName) {
        this.discussionGroupName = discussionGroupName;
    }

    public String getDiscussionGroupHead() {
        return discussionGroupHead;
    }

    public void setDiscussionGroupHead(String discussionGroupHead) {
        this.discussionGroupHead = discussionGroupHead;
    }

    public Boolean getPrivExists() {
        return privExists;
    }

    public void setPrivExists(Boolean privExists) {
        this.privExists = privExists;
    }

    public Long getPrivId() {
        return privId;
    }

    public void setPrivId(Long privId) {
        this.privId = privId;
    }
}