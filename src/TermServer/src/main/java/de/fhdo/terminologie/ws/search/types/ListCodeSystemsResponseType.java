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
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Die Xml-Annotationen am Anfang bestimmen die Reihenfolge der
 * XML-Elemente in der Response - returnInfos sollten immer zuerst erscheinen
 * 
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@XmlRootElement
@XmlType(name = "", propOrder = { "returnInfos", "codeSystem"})
public class ListCodeSystemsResponseType
{
  private ReturnType returnInfos;
  private List<CodeSystem> codeSystem;

 
  /**
   * @return the codeSystemList
   */
  public List<CodeSystem> getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystemList the codeSystemList to set
   */
  public void setCodeSystem(List<CodeSystem> codeSystem)
  {
    this.codeSystem = codeSystem;
  }

  /**
   * @return the _returnInfos
   */
  public ReturnType getReturnInfos()
  {
    return returnInfos;
  }

  /**
   * @param returnInfos the _returnInfos to set
   */
  public void setReturnInfos(ReturnType returnInfos)
  {
    this.returnInfos = returnInfos;
  }

  
}
