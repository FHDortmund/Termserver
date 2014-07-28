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
package de.fhdo.terminologie.ws.types;

import java.util.List;

/**
 *
 * @author Bernhard Rimatzki
 */
public class ImportType 
{
    private Long formatId;
    private byte[] filecontent;
    private List<FilecontentListEntry> fileContentList;
    private Boolean order;
    //private String role;

    /**
     * @return the formatId
     */
    public Long getFormatId() {
        return formatId;
    }

    /**
     * @param formatId the formatId to set
     */
    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    /**
     * @return the filecontent
     */
    public byte[] getFilecontent() {
        return filecontent;
    }

    /**
     * @param filecontent the filecontent to set
     */
    public void setFilecontent(byte[] filecontent) {
        this.filecontent = filecontent;
    }

    public List<FilecontentListEntry> getFileContentList() {
        return fileContentList;
    }

    public void setFileContentList(List<FilecontentListEntry> fileContentList) {
        this.fileContentList = fileContentList;
    }

    public Boolean getOrder() {
        return order;
    }

    public void setOrder(Boolean order) {
        this.order = order;
    }

//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(String role) {
//        this.role = role;
//    }
}
