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
package de.fhdo.terminologie.ws.administration._export;

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ExportOWL
{
  private static Logger logger = Logger4j.getInstance().getLogger();
  ExportCodeSystemContentRequestType parameter;
  private int countExported = 0;
  
  
  public ExportOWL(ExportCodeSystemContentRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ExportCSV gestartet ======");

    parameter = _parameter;
  }
  
  public String exportCSV(ExportCodeSystemContentResponseType reponse)
  {
    String s = "";  // Status-Meldung
    
   
    return s;
  }

  /**
   * @return the countExported
   */
  public int getCountExported()
  {
    return countExported;
  }
}
