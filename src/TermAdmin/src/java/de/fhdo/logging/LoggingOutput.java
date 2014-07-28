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
package de.fhdo.logging;

/**
 *
 * @author Robert Mützner
 */
public class LoggingOutput
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static void outputException(Exception ex, Object classObj)
  {
    ex.printStackTrace();
    
    if(classObj != null && classObj.getClass() != null)
      logger.error("Fehler in '" + classObj.getClass().getCanonicalName() + "': " + ex.getLocalizedMessage());
    else logger.error("Fehler in 'null': " + ex.getLocalizedMessage());
    
    //Messagebox.show("Fehler: " + ex.getLocalizedMessage());
  }
  
}
