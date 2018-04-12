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
public class PagingResultType
{
  private int pageIndex, maxPageSize, count;
  private String pageSize;
  private String message;

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
  public int getPageIndex()
  {
    return pageIndex;
  }

  /**
   * @param pageIndex the pageIndex to set
   */
  public void setPageIndex(int pageIndex)
  {
    this.pageIndex = pageIndex;
  }

  /**
   * @return the maxPageSize
   */
  public int getMaxPageSize()
  {
    return maxPageSize;
  }

  /**
   * @param maxPageSize the maxPageSize to set
   */
  public void setMaxPageSize(int maxPageSize)
  {
    this.maxPageSize = maxPageSize;
  }

  /**
   * @return the message
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * @return the count
   */
  public int getCount()
  {
    return count;
  }

  /**
   * @param count the count to set
   */
  public void setCount(int count)
  {
    this.count = count;
  }
}
