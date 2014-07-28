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
package de.fhdo.collaboration.db.classes;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * AssignedTerm by Philipp Urbauer
 */
@Entity
@Table(name="assigned_term"
    
)
public class AssignedTerm  implements java.io.Serializable {


     private Long id;
     private Long classId;
     private String classname;
     private Collaborationuser collaborationuser;

    public AssignedTerm() {
    }

    public AssignedTerm(Long id, Long classId, String classname, Collaborationuser collaborationuser) {
       this.id = id;
       this.classId = classId;
       this.classname = classname;
       this.collaborationuser = collaborationuser;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)
    
    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name="classId")
    public Long getClassId() {
        return this.classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    @Column(name="classname", length=65535)
    public String getClassname() {
        return this.classname;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="collaborationUserId", nullable=false)
    public Collaborationuser getCollaborationuser() {
        return this.collaborationuser;
    }
    
    public void setCollaborationuser(Collaborationuser collaborationuser) {
        this.collaborationuser = collaborationuser;
    }

}


