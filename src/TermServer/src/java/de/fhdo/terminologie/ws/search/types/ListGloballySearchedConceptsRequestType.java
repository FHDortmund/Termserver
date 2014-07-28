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
package de.fhdo.terminologie.ws.search.types;

import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Philipp Urbauer
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListGloballySearchedConceptsRequestType
{
  @XmlElement(required = false)
  private String loginToken;
  
  @XmlElement(required = false)
  private CodeSystemConcept codeSystemConcept;
  
//  @XmlElement(required = false)
//  private String loginToken;
//
//  @XmlElement(required = false)
//  private Boolean codeSystemConceptSearch;
//
//  @XmlElement(required = false)
//  private String term;
//
//  @XmlElement(required = false)
//  private String code;
//
//  public String getLoginToken()
//  {
//    return loginToken;
//  }
//
//  public void setLoginToken(String login)
//  {
//    this.loginToken = login;
//  }
//
//  public Boolean getCodeSystemConcepts()
//  {
//    return codeSystemConceptSearch;
//  }
//
//  public void setCodeSystemConcepts(Boolean codeSystemConcepts)
//  {
//    this.codeSystemConceptSearch = codeSystemConcepts;
//  }
//
//  public String getTerm()
//  {
//    return term;
//  }
//
//  public void setTerm(String term)
//  {
//    this.term = term;
//  }
//
//  public String getCode()
//  {
//    return code;
//  }
//
//  public void setCode(String code)
//  {
//    this.code = code;
//  }

  /**
   * @return the codeSystemConcept
   */
  public CodeSystemConcept getCodeSystemConcept()
  {
    return codeSystemConcept;
  }

  /**
   * @param codeSystemConcept the codeSystemConcept to set
   */
  public void setCodeSystemConcept(CodeSystemConcept codeSystemConcept)
  {
    this.codeSystemConcept = codeSystemConcept;
  }

  /**
   * @return the loginToken
   */
  public String getLoginToken()
  {
    return loginToken;
  }

  /**
   * @param loginToken the loginToken to set
   */
  public void setLoginToken(String loginToken)
  {
    this.loginToken = loginToken;
  }
}
