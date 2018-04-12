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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@XmlRootElement
@XmlType(name = "Concept" , namespace = "urn:ihe:iti:svs:2008")
public class Concept
{
  private String displayName;
  private String codeSystemName;
  private String codeSystemVersion;
  private String codeSystem;
  private String code;
  private String originalText;
  //private List<String> translation;
  //private List<String> qualifier;
  //private List<String> translation;

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
   * @return the codeSystemName
   */
  @XmlAttribute
  public String getCodeSystemName()
  {
    return codeSystemName;
  }

  /**
   * @param codeSystemName the codeSystemName to set
   */
  public void setCodeSystemName(String codeSystemName)
  {
    this.codeSystemName = codeSystemName;
  }

  /**
   * @return the codeSystemVersion
   */
  @XmlAttribute
  public String getCodeSystemVersion()
  {
    return codeSystemVersion;
  }

  /**
   * @param codeSystemVersion the codeSystemVersion to set
   */
  public void setCodeSystemVersion(String codeSystemVersion)
  {
    this.codeSystemVersion = codeSystemVersion;
  }

  /**
   * @return the codeSystem
   */
  @XmlAttribute
  public String getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystem the codeSystem to set
   */
  public void setCodeSystem(String codeSystem)
  {
    this.codeSystem = codeSystem;
  }

  /**
   * @return the code
   */
  @XmlAttribute
  public String getCode()
  {
    return code;
  }

  /**
   * @param code the code to set
   */
  public void setCode(String code)
  {
    this.code = code;
  }

  /**
   * @return the originalText
   */
  @XmlAttribute
  public String getOriginalText()
  {
    return originalText;
  }

  /**
   * @param originalText the originalText to set
   */
  public void setOriginalText(String originalText)
  {
    this.originalText = originalText;
  }
}
