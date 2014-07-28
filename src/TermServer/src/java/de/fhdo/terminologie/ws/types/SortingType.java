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
 * 
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class SortingType
{
  public enum SortType { ALPHABETICALLY, ORDER_NR};
  public enum SortByField { CODE, TERM};
  public enum SortDirection { ASCENDING, DESCENDING};
  
  
  private SortType sortType;
  private SortByField sortBy;
  private SortDirection sortDirection;
  

  /**
   * @return the sortType
   */
  public SortType getSortType()
  {
    return sortType;
  }

  /**
   * @param sortType the sortType to set
   */
  public void setSortType(SortType sortType)
  {
    this.sortType = sortType;
  }

  /**
   * @return the sortBy
   */
  public SortByField getSortBy()
  {
    return sortBy;
  }

  /**
   * @param sortBy the sortBy to set
   */
  public void setSortBy(SortByField sortBy)
  {
    this.sortBy = sortBy;
  }

  /**
   * @return the sortDirection
   */
  public SortDirection getSortDirection()
  {
    return sortDirection;
  }

  /**
   * @param sortDirection the sortDirection to set
   */
  public void setSortDirection(SortDirection sortDirection)
  {
    this.sortDirection = sortDirection;
  }
    
  
  
  
}
