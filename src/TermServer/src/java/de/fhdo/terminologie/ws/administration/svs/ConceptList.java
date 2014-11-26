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
@XmlRootElement (name = "ConceptList", namespace = "urn:ihe:iti:svs:2008")
public class ConceptList
{
  private String lang;
  
  
  private List<Concept> concept;

  /**
   * @return the concept
   */
  @XmlElement (name = "Concept", namespace = "urn:ihe:iti:svs:2008")
  public List<Concept> getConcept()
  {
    return concept;
  }

  /**
   * @param concept the concept to set
   */
  public void setConcept(List<Concept> concept)
  {
    this.concept = concept;
  }

  /**
   * @return the lang
   */
  @XmlAttribute (namespace = "http://www.w3.org/XML/1998/namespace")
  public String getLang()
  {
    return lang;
  }

  /**
   * @param lang the lang to set
   */
  public void setLang(String lang)
  {
    this.lang = lang;
  }
}
