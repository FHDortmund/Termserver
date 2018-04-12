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
package de.fhdo.terminologie.ws.administration.types;

import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CheckImportStatusResponseType
{
  private ReturnType returnInfos;
  private String currentTask ="";
  private double percentComplete=0.0;

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
   * @return the currentTask
   */
  public String getCurrentTask()
  {
    return currentTask;
  }

  /**
   * @param currentTask the currentTask to set
   */
  public void setCurrentTask(String currentTask)
  {
    this.currentTask = currentTask;
  }

  /**
   * @return the percentComplete
   */
  public double getPercentComplete()
  {
    return percentComplete;
  }

  /**
   * @param percentComplete the percentComplete to set
   */
  public void setPercentComplete(double percentComplete)
  {
    this.percentComplete = percentComplete;
  }
}
