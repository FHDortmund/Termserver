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
package de.fhdo.terminologie.ws.conceptAssociation.types;

import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;

import java.util.List;

/**
 *
 * @author Nico Hänsch
 */
public class ListConceptAssociationsRequestType 
{
    private Boolean reverse;
    private Boolean directionBoth;
    private Boolean lookForward;
    private String loginToken;
    private CodeSystemEntity codeSystemEntity;
    private CodeSystemEntityVersionAssociation codeSystemEntityVersionAssociation;

    /**
     * @return the login
     */
    public String getLoginToken() {
        return loginToken;
    }

    /**
     * @param login the login to set
     */
    public void setLoginToken(String login) {
        this.loginToken = login;
    }

    /**
     * @return the codeSystemEntity
     */
    public CodeSystemEntity getCodeSystemEntity() {
        return codeSystemEntity;
    }

    /**
     * @param codeSystemEntity the codeSystemEntity to set
     */
    public void setCodeSystemEntity(CodeSystemEntity codeSystemEntity) {
        this.codeSystemEntity = codeSystemEntity;
    }

    /**
     * @return the codeSystemEntityVersionAssociation
     */
    public CodeSystemEntityVersionAssociation getCodeSystemEntityVersionAssociation() {
        return codeSystemEntityVersionAssociation;
    }

    /**
     * @param codeSystemEntityVersionAssociation the codeSystemEntityVersionAssociation to set
     */
    public void setCodeSystemEntityVersionAssociation(CodeSystemEntityVersionAssociation codeSystemEntityVersionAssociation) {
        this.codeSystemEntityVersionAssociation = codeSystemEntityVersionAssociation;
    }

    /**
     * @return the reverse
     */
    public Boolean getReverse() {
        return reverse;
    }

    /**
     * @param reverse the reverse to set
     */
    public void setReverse(Boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * @return the directionBoth
     */
    public Boolean getDirectionBoth() {
        return directionBoth;
    }

    /**
     * @param directionBoth the directionBoth to set
     */
    public void setDirectionBoth(Boolean directionBoth) {
        this.directionBoth = directionBoth;
    }

  /**
   * @return the lookForward
   */
  public Boolean getLookForward()
  {
    return lookForward;
  }

  /**
   * @param lookForward the lookForward to set
   */
  public void setLookForward(Boolean lookForward)
  {
    this.lookForward = lookForward;
  }

  
}
