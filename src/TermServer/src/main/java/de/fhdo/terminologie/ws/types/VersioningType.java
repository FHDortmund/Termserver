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
public class VersioningType
{
  private Boolean createNewVersion;
  private Boolean majorUpdate;
  private Boolean minorUpdate;
  private Boolean copyConcepts;

  /**
   * @return the createNewVersion
   */
  public Boolean getCreateNewVersion()
  {
    return createNewVersion;
  }

  /**
   * @param createNewVersion the createNewVersion to set
   */
  public void setCreateNewVersion(Boolean createNewVersion)
  {
    this.createNewVersion = createNewVersion;
  }

  /**
   * @return the majorUpdate
   */
  public Boolean getMajorUpdate()
  {
    return majorUpdate;
  }

  /**
   * @param majorUpdate the majorUpdate to set
   */
  public void setMajorUpdate(Boolean majorUpdate)
  {
    this.majorUpdate = majorUpdate;
  }

  /**
   * @return the minorUpdate
   */
  public Boolean getMinorUpdate()
  {
    return minorUpdate;
  }

  /**
   * @param minorUpdate the minorUpdate to set
   */
  public void setMinorUpdate(Boolean minorUpdate)
  {
    this.minorUpdate = minorUpdate;
  }

  /**
   * @return the copyConcepts
   */
  public Boolean getCopyConcepts()
  {
    return copyConcepts;
  }

  /**
   * @param copyConcepts the copyConcepts to set
   */
  public void setCopyConcepts(Boolean copyConcepts)
  {
    this.copyConcepts = copyConcepts;
  }
}
