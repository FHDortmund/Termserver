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

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.types.ExportParameterType;
import de.fhdo.terminologie.ws.types.ExportType;


/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ExportValueSetContentRequestType
{
  //public static final long EXPORT_CLAML_ID = 193;
  public static final long EXPORT_CSV_ID = 194;
  public static final long EXPIRT_SVS_ID = 195;

  public static String getPossibleFormats()
  {
    String s = " Mögliche Export-Formate sind: 194(CSV) oder 195(SVS)";
    return s;
  }
  
  private ExportType exportInfos;
  private String loginToken;
  private ValueSet valueSet;
  private ExportParameterType exportParameter;

  /**
   * @return the exportInfos
   */
  public ExportType getExportInfos()
  {
    return exportInfos;
  }

  /**
   * @param exportInfos the exportInfos to set
   */
  public void setExportInfos(ExportType exportInfos)
  {
    this.exportInfos = exportInfos;
  }

  /**
   * @return the login
   */
  public String getLoginToken()
  {
    return loginToken;
  }

  /**
   * @param login the login to set
   */
  public void setLoginToken(String login)
  {
    this.loginToken = login;
  }

  /**
   * @return the ValueSet
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the ValueSet to set
   */
  public void setValueSet(ValueSet valueSet)
  {
    this.valueSet = valueSet;
  }

  /**
   * @return the exportParameter
   */
  public ExportParameterType getExportParameter()
  {
    return exportParameter;
  }

  /**
   * @param exportParameter the exportParameter to set
   */
  public void setExportParameter(ExportParameterType exportParameter)
  {
    this.exportParameter = exportParameter;
  }
}
