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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import java.util.Date;
import org.hibernate.Session;

/**
 *
 * @author Philipp Urbauer
 */
public class LastChangeHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static boolean updateLastChangeDate(Boolean isCodeSystemVersion, Long id, Session hb_session)
  {
    logger.debug("updateLastChangeDate");
    if(id == null || id == 0)
      return false;
    
    boolean exists = true;
    if (hb_session == null)
    {
      logger.debug("create session");
      hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      exists = false;
    }
    
    logger.debug("exists: " + exists);
    
    boolean success = false;
    try
    {
      if (isCodeSystemVersion)
      {
        CodeSystemVersion csv = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, id);
        csv.setLastChangeDate(new Date());
        hb_session.update(csv);
      }
      else
      {

        ValueSetVersion vsv = (ValueSetVersion) hb_session.get(ValueSetVersion.class, id);
        vsv.setLastChangeDate(new Date());
        hb_session.update(vsv);
      }

      if (!exists)
      {
        logger.debug("session commit");
        hb_session.getTransaction().commit();
      }

      success = true;
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, LastChangeHelper.class);
      
      if (!exists)
      {
        logger.debug("session rollback");
        hb_session.getTransaction().rollback();
      }
      success = false;
    }
    finally
    {
      if (!exists)
      {
        logger.debug("session close");
        hb_session.close();
      }
    }
    return success;
  }
}
