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

/**
 *
 * @author Bernhard Rimatzki
 */
public class ExportType 
{
    private Long formatId;
    private byte[] filecontent;
    private String url;
    //private boolean updateCheck = false;

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

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

//    public boolean isUpdateCheck() {
//        return updateCheck;
//    }
//
//    public void setUpdateCheck(boolean updateCheck) {
//        this.updateCheck = updateCheck;
//    }
}
