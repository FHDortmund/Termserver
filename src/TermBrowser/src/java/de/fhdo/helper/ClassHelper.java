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
package de.fhdo.helper;

import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ClassHelper
{

  /**
   * Get the current codesystem version object.
   * 
   * @param codeSystem
   * @return The current codesystem version object
   */
  public static CodeSystemVersion getCurrentCodesystemVersion(CodeSystem codeSystem)
  {
    CodeSystemVersion csv = null;

    if (codeSystem != null)
    {
      for (CodeSystemVersion csv_obj : codeSystem.getCodeSystemVersions())
      {
        if(csv_obj.getVersionId().longValue() == codeSystem.getCurrentVersionId())
        {
          csv = csv_obj;
          break;
        }
      }
    }

    return csv;
  }
  
  /**
   * Get the current valueset version object.
   * 
   * @param valueSet
   * @return The current valueset version object
   */
  public static ValueSetVersion getCurrentValuesetVersion(ValueSet valueSet)
  {
    ValueSetVersion vsv = null;

    if (valueSet != null)
    {
      for (ValueSetVersion vsv_obj : valueSet.getValueSetVersions())
      {
        if(vsv_obj.getVersionId().longValue() == valueSet.getCurrentVersionId())
        {
          vsv = vsv_obj;
          break;
        }
      }
    }

    return vsv;
  }
}
