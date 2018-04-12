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
package de.fhdo.terminologie.ws.authoring.types;

import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;

import de.fhdo.terminologie.ws.types.VersioningType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class MaintainConceptRequestType
{
  private String loginToken;
  
  // wird nicht benötigt in MaintainConceptRequest
  //private CodeSystem codeSystem;
  private CodeSystemEntity codeSystemEntity;
  
  // Versioning hinzugefügt, da benötigt
  private VersioningType versioning;
  private Long codeSystemVersionId;

  /**
   * @return the login
   */
  public String getLoginToken()
  {
    return loginToken;
  }

  /**
   * @param login the login to set
   */
  public void setLoginToken(String login)
  {
    this.loginToken = login;
  }

//  /**
//   * @return the codeSystem
//   */
//  public CodeSystem getCodeSystem()
//  {
//    return codeSystem;
//  }
//
//  /**
//   * @param codeSystem the codeSystem to set
//   */
//  public void setCodeSystem(CodeSystem codeSystem)
//  {
//    this.codeSystem = codeSystem;
//  }

  /**
   * @return the codeSystemEntity
   */
  public CodeSystemEntity getCodeSystemEntity()
  {
    return codeSystemEntity;
  }

  /**
   * @param codeSystemEntity the codeSystemEntity to set
   */
  public void setCodeSystemEntity(CodeSystemEntity codeSystemEntity)
  {
    this.codeSystemEntity = codeSystemEntity;
  }

    /**
     * @return the versioning
     */
    public VersioningType getVersioning() {
        return versioning;
    }

    /**
     * @param versioning the versioning to set
     */
    public void setVersioning(VersioningType versioning) {
        this.versioning = versioning;
    }

    public Long getCodeSystemVersionId() {
        return codeSystemVersionId;
    }

    public void setCodeSystemVersionId(Long codeSystemVersionId) {
        this.codeSystemVersionId = codeSystemVersionId;
    }
    
}
