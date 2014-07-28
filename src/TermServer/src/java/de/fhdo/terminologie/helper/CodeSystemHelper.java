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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import java.util.Iterator;

/**
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) (robert.muetzner@fh-dortmund.de)
 */
public class CodeSystemHelper
{

  public static long getCurrentVersionId(CodeSystem codeSystem)
  {
    long codeSystemVersionId = 0;
    
    if (codeSystem.getCurrentVersionId() > 0)
      codeSystemVersionId = codeSystem.getCurrentVersionId();
    else
    {
      if(codeSystem.getCodeSystemVersions() != null)
      {
        // Die höchste ID aller Versionen ermitteln
        Iterator<CodeSystemVersion> it = codeSystem.getCodeSystemVersions().iterator();
        long id = 0;
        while(it.hasNext())
        {
          CodeSystemVersion csv = it.next();
          if(csv.getVersionId() > id)
            id = csv.getVersionId();
        }
        
        codeSystemVersionId = id;
      }
    }
    
    return codeSystemVersionId;
  }
}
