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

import java.util.GregorianCalendar;
import org.hibernate.type.TimestampType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class DateHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static java.util.Date getDateFromObject(Object o)
  {
    if(o == null)
      return null;
    
    if(o instanceof GregorianCalendar)
    {
      GregorianCalendar gregcal = (GregorianCalendar)o;
      return gregcal.getTime();
    }
    if(o instanceof java.util.Date)
      return (java.util.Date)o;
    
    if(o instanceof TimestampType)
    {
      TimestampType tt = (TimestampType)o;
      logger.debug("TimestampType: " + tt.toString());
      return new java.util.Date(tt.toString());
    }
    
    
    return null;
  }
  
}
