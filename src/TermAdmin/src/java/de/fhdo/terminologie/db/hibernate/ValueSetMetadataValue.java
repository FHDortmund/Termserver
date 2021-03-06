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
package de.fhdo.terminologie.db.hibernate;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//added by PU

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(namespace = "de.fhdo.termserver.types")
@Entity
@Table(name = "value_set_metadata_value")
public class ValueSetMetadataValue implements java.io.Serializable
{

  private Long id;
     private CodeSystemEntityVersion codeSystemEntityVersion;
     private MetadataParameter metadataParameter;
     private String parameterValue;
     private Long valuesetVersionId;

    public ValueSetMetadataValue() {
    }

	
    public ValueSetMetadataValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
    public ValueSetMetadataValue(CodeSystemEntityVersion codeSystemEntityVersion, MetadataParameter metadataParameter, String parameterValue, Long valuesetVersionId) {
       this.codeSystemEntityVersion = codeSystemEntityVersion;
       this.metadataParameter = metadataParameter;
       this.parameterValue = parameterValue;
       this.valuesetVersionId = valuesetVersionId;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)
    
    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemEntityVersionId")
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return this.codeSystemEntityVersion;
    }
    
    public void setCodeSystemEntityVersion(CodeSystemEntityVersion codeSystemEntityVersion) {
        this.codeSystemEntityVersion = codeSystemEntityVersion;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="metadataParameterId")
    public MetadataParameter getMetadataParameter() {
        return this.metadataParameter;
    }
    
    public void setMetadataParameter(MetadataParameter metadataParameter) {
        this.metadataParameter = metadataParameter;
    }
    
    @Column(name="parameterValue", nullable=false, length=65535)
    public String getParameterValue() {
        return this.parameterValue;
    }
    
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
    
    @Column(name="valuesetVersionId")
    public Long getValuesetVersionId() {
        return this.valuesetVersionId;
    }
    
    public void setValuesetVersionId(Long valuesetVersionId) {
        this.valuesetVersionId = valuesetVersionId;
    }
}
