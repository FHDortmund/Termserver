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

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;

import de.fhdo.terminologie.ws.types.PagingType;
import de.fhdo.terminologie.ws.types.SearchType;
import de.fhdo.terminologie.ws.types.SortingType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListCodeSystemConceptsRequestType
{
  @XmlElement(required = false)
  private String loginToken;
  
  @XmlElement(required = true)
  private CodeSystem codeSystem;
  
  @XmlElement(required = false)
  private CodeSystemEntity codeSystemEntity;

  @XmlElement(required = false)
  private SearchType searchParameter;
  
  @XmlElement(required = false)
  private PagingType pagingParameter;
  
  @XmlElement(required = false)
  private boolean lookForward;
  
  @XmlElement(required = false)
  private SortingType sortingParameter;
  
  @XmlElement(required = false)
  private Boolean loadMetadata;
  
  @XmlElement(required = false)
  private Boolean loadTranslation;
  
  
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
   * @return the codeSystem
   */
  public CodeSystem getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystem the codeSystem to set
   */
  public void setCodeSystem(CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }

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
   * @return the searchParameter
   */
  public SearchType getSearchParameter()
  {
    return searchParameter;
  }

  /**
   * @param searchParameter the searchParameter to set
   */
  public void setSearchParameter(SearchType searchParameter)
  {
    this.searchParameter = searchParameter;
  }

  /**
   * @return the pagingParameter
   */
  public PagingType getPagingParameter()
  {
    return pagingParameter;
  }

  /**
   * @param pagingParameter the pagingParameter to set
   */
  public void setPagingParameter(PagingType pagingParameter)
  {
    this.pagingParameter = pagingParameter;
  }

  /**
   * @return the lookForward
   */
  public boolean isLookForward()
  {
    return lookForward;
  }

  /**
   * @param lookForward the lookForward to set
   */
  public void setLookForward(boolean lookForward)
  {
    this.lookForward = lookForward;
  }

  /**
   * @return the sortingParameter
   */
  public SortingType getSortingParameter()
  {
    return sortingParameter;
  }

  /**
   * @param sortingParameter the sortingParameter to set
   */
  public void setSortingParameter(SortingType sortingParameter)
  {
    this.sortingParameter = sortingParameter;
  }

  /**
   * @return the loadMetadata
   */
  public Boolean isLoadMetadata()
  {
    return loadMetadata;
  }

  /**
   * @param loadMetadata the loadMetadata to set
   */
  public void setLoadMetadata(Boolean loadMetadata)
  {
    this.loadMetadata = loadMetadata;
  }

  /**
   * @return the loadTranslation
   */
  public Boolean isLoadTranslation()
  {
    return loadTranslation;
  }

  /**
   * @param loadTranslation the loadTranslation to set
   */
  public void setLoadTranslation(Boolean loadTranslation)
  {
    this.loadTranslation = loadTranslation;
  }
}
