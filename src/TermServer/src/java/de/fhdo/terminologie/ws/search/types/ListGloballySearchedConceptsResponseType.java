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

import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.ws.types.PagingResultType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Die Xml-Annotationen am Anfang bestimmen die Reihenfolge der
 * XML-Elemente in der Response - returnInfos sollten immer zuerst erscheinen
 * 
 * @author Philipp Urbauer
 */
@XmlRootElement
@XmlType(name = "", propOrder = { "returnInfos", "pagingInfos", "codeSystemEntity"})
public class ListGloballySearchedConceptsResponseType
{
  private ReturnType returnInfos;
  private PagingResultType pagingInfos;
  private List<CodeSystemEntity> codeSystemEntity;
//  private List<GlobalSearchResultEntry> globalSearchResultEntry;
//
//  /**
//   * @return the returnInfos
//   */
//  public ReturnType getReturnInfos()
//  {
//    return returnInfos;
//  }
//
//  /**
//   * @param returnInfos the returnInfos to set
//   */
//  public void setReturnInfos(ReturnType returnInfos)
//  {
//    this.returnInfos = returnInfos;
//  }
//
//    public List<GlobalSearchResultEntry> getGlobalSearchResultEntry() {
//        return globalSearchResultEntry;
//    }
//
//    public void setGlobalSearchResultEntry(List<GlobalSearchResultEntry> globalSearchResultEntry) {
//        this.globalSearchResultEntry = globalSearchResultEntry;
//    }

  /**
   * @return the returnInfos
   */
  public ReturnType getReturnInfos()
  {
    return returnInfos;
  }

  /**
   * @param returnInfos the returnInfos to set
   */
  public void setReturnInfos(ReturnType returnInfos)
  {
    this.returnInfos = returnInfos;
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
   * @return the pagingInfos
   */
  public PagingResultType getPagingInfos()
  {
    return pagingInfos;
  }

  /**
   * @param pagingInfos the pagingInfos to set
   */
  public void setPagingInfos(PagingResultType pagingInfos)
  {
    this.pagingInfos = pagingInfos;
  }
}
