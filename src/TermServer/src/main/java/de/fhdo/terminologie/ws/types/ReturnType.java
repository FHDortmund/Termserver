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
public class ReturnType
{

  /**
   * @return the overallErrorCategory
   */
  public OverallErrorCategory getOverallErrorCategory()
  {
    return overallErrorCategory;
  }

  /**
   * @param overallErrorCategory the overallErrorCategory to set
   */
  public void setOverallErrorCategory(OverallErrorCategory overallErrorCategory)
  {
    this.overallErrorCategory = overallErrorCategory;
  }

  /**
   * @return the status
   */
  public Status getStatus()
  {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(Status status)
  {
    this.status = status;
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
  public enum OverallErrorCategory { INFO, WARN, ERROR};
  public enum Status { OK, FAILURE};
  
  private OverallErrorCategory overallErrorCategory;
  private Status status;
  private String message;
  private int count;
}
