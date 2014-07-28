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
package de.fhdo.terminologie.ws.types;

/**

 @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class PagingType
{
  private String pageSize;
  private Integer pageIndex;
  private Boolean allEntries;
  private Boolean userPaging = false;

  /**
   * @return the pageSize
   */
  public String getPageSize()
  {
    return pageSize;
  }

  /**
   * @param pageSize the pageSize to set
   */
  public void setPageSize(String pageSize)
  {
    this.pageSize = pageSize;
  }

  /**
   * @return the pageIndex
   */
  public Integer getPageIndex()
  {
    return pageIndex;
  }

  /**
   * @param pageIndex the pageIndex to set
   */
  public void setPageIndex(Integer pageIndex)
  {
    this.pageIndex = pageIndex;
  }

  /**
   * @return the allEntries
   */
  public Boolean isAllEntries()
  {
    return allEntries;
  }

  /**
   * @param allEntries the allEntries to set
   */
  public void setAllEntries(Boolean allEntries)
  {
    this.allEntries = allEntries;
  }

    public Boolean getUserPaging() {
        return userPaging;
    }

    public void setUserPaging(Boolean userPaging) {
        this.userPaging = userPaging;
    }
}
