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
package de.fhdo.gui.admin.modules.terminology;

/**
 *
 * @author Philipp Urbauer
 */
public class ReportingTermData {
    
    //Header-Bezeichner
    public static final String VOC_NAME_Header = "Name CS/VS";
    public static final String VERSION_NAME_Header = "Versionsname";
    public static final String OID_Header = "OID";
    public static final String VERSION_NUMBER_Header = "Version";
    public static final String TYPE_Header = "Type";
    public static final String NUMBER_CONCEPTS_Header = "#Konzepte";
    public static final String STATUS_Header = "Status";
    public static final String PUBLIC_DATE_Header = "Publikationsdatum"; 
    
    private String vokabularyName;
    private String versionName;
    private String oid;
    private String versionnumber;
    private String type;
    private String numberConcepts;
    private String status;
    private String releaseDate;

    public String getVokabularyName() {
        return vokabularyName;
    }

    public void setVokabularyName(String vokabularyName) {
        this.vokabularyName = vokabularyName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getVersionnumber() {
        return versionnumber;
    }

    public void setVersionnumber(String versionnumber) {
        this.versionnumber = versionnumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumberConcepts() {
        return numberConcepts;
    }

    public void setNumberConcepts(String numberConcepts) {
        this.numberConcepts = numberConcepts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
