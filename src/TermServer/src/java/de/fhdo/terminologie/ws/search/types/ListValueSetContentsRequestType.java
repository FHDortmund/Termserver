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

import de.fhdo.terminologie.db.hibernate.ValueSet;

import de.fhdo.terminologie.ws.types.SortingType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListValueSetContentsRequestType
{
  @XmlElement(required = false)
  private String loginToken;
  
  @XmlElement(required = true)
  private ValueSet valueSet;
  
  @XmlElement(required = false)
  private SortingType sortingParameter;
  
  @XmlElement(required = false)
  private Boolean readMetadataLevel;
  

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
   * @return the valueSet
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the valueSet to set
   */
  public void setValueSet(ValueSet valueSet)
  {
    this.valueSet = valueSet;
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
   * @return the readMetadata
   */
  public Boolean getReadMetadataLevel()
  {
    return readMetadataLevel;
  }

  /**
   * @param readMetadataLevel the readMetadata to set
   */
  public void setReadMetadataLevel(Boolean readMetadataLevel)
  {
    this.readMetadataLevel = readMetadataLevel;
  }
}
