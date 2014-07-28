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
import de.fhdo.terminologie.db.hibernate.ValueSet;

import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CreateValueSetContentRequestType
{
  private String loginToken;
  private List<CodeSystemEntity> codeSystemEntity;
  private ValueSet valueSet;
  //private ConceptValueSetMembership conceptValueSetMembership;
  
  public CreateValueSetContentRequestType()
  {
    
  }

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

  /**
   * @return the codeSystemEntity
   */
  public List<CodeSystemEntity> getCodeSystemEntity()
  {
    return codeSystemEntity;
  }

  /**
   * @param codeSystemEntity the codeSystemEntity to set
   */
  public void setCodeSystemEntity(List<CodeSystemEntity> codeSystemEntity)
  {
    this.codeSystemEntity = codeSystemEntity;
  }

  /**
   * @return the valueSetType
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSetType the valueSetType to set
   */
  public void setValueSet(ValueSet valueSetType)
  {
    this.valueSet = valueSetType;
  }

//    public ConceptValueSetMembership getConceptValueSetMembership() {
//        return conceptValueSetMembership;
//    }
//
//    public void setConceptValueSetMembership(ConceptValueSetMembership conceptValueSetMembership) {
//        this.conceptValueSetMembership = conceptValueSetMembership;
//    }
}
