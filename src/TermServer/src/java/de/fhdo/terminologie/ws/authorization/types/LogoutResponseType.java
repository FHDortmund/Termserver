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
package de.fhdo.terminologie.ws.authorization.types;

import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;


/**
 *
 * @author Robert
 */
public class LogoutResponseType
{
  private ReturnType returnInfos;
  private List<String> parameterList;

  public LogoutResponseType(){}

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
   * @return the parameterList
   */
  public List<String> getParameterList()
  {
    return parameterList;
  }

  /**
   * @param parameterList the parameterList to set
   */
  public void setParameterList(List<String> parameterList)
  {
    this.parameterList = parameterList;
  }

}
