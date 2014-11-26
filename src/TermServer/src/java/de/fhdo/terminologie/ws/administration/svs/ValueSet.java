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

package de.fhdo.terminologie.ws.administration.svs;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@XmlRootElement
@XmlType(name = "ValueSet", propOrder = { "displayName", "version", "id", "conceptList"} )
public class ValueSet
{
  private String id;
  private String version;
  private String displayName;
  
  private ConceptList conceptList;

  /**
   * @return the id
   */
  @XmlAttribute
  public String getId()
  {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * @return the version
   */
  @XmlAttribute
  public String getVersion()
  {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  /**
   * @return the displayName
   */
  @XmlAttribute
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * @param displayName the displayName to set
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * @return the conceptList
   */
  @XmlElement (name = "ConceptList", namespace = "urn:ihe:iti:svs:2008")
  public ConceptList getConceptList()
  {
    return conceptList;
  }

  /**
   * @param conceptList the conceptList to set
   */
  public void setConceptList(ConceptList conceptList)
  {
    this.conceptList = conceptList;
  }

  
  
}
