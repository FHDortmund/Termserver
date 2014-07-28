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
package de.fhdo.list;

/**

 @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class GenericListRowType
{
  private Object data;
  private GenericListCellType []cells;
  private String color;
  private long id;

  /**
   * @return the data
   */
  public Object getData()
  {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(Object data)
  {
    this.data = data;
  }

  /**
   * @return the cells
   */
  public GenericListCellType[] getCells()
  {
    return cells;
  }

  /**
   * @param cells the cells to set
   */
  public void setCells(GenericListCellType[] cells)
  {
    this.cells = cells;
  }

  /*public int compareTo(Object o)
  {
    if(o instanceof GenericListRowType)
    {
      GenericListRowType o2 = (GenericListRowType)o;
      
      
    }
    
    return 0;
  }*/

  /**
   * @return the color
   */
  public String getColor()
  {
    return color;
  }

  /**
   * @param color the color to set
   */
  public void setColor(String color)
  {
    this.color = color;
  }

  /**
   * @return the id
   */
  public long getId()
  {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(long id)
  {
    this.id = id;
  }
}
