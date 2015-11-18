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

import de.fhdo.terminologie.ws.search.GetTermserverVersionResponse;
import java.util.Date;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class VersionHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static VersionHelper instance;

  public static VersionHelper getInstance()
  {
    if (instance == null)
      instance = new VersionHelper();
    return instance;
  }

  private static int RELOAD_AFTER_MS = 86400000;

  Date lastDate;
  String termserverVersion = "";
  Date termserverDate;

  public VersionHelper()
  {
    logger.debug("New VersionHelper()");
    lastDate = null;
  }

  public String getVersion()
  {

    if (lastDate == null || termserverVersion.length() == 0 || (new Date().getTime() > lastDate.getTime() + RELOAD_AFTER_MS))
    {
      logger.debug("Lade Version vom Terminologieserver (WebService-Aufruf):");

      logger.debug("LastDate: " + lastDate);
      logger.debug("termserverVersion: " + termserverVersion);
      logger.debug("RELOAD_AFTER_MS: " + RELOAD_AFTER_MS);
      GetTermserverVersionResponse.Return ret = WebServiceHelper.getVersion();
      termserverVersion = ret.getVersion();
      termserverDate = DateTimeHelper.ConvertXMLGregorianCalenderToDate(ret.getDate());

      lastDate = new Date();
    }
    else
      logger.debug("verwende Version aus dem Cache");

    return termserverVersion;
  }

  public Date getDate()
  {
    if (lastDate == null || termserverVersion.length() == 0 || lastDate.getTime() + RELOAD_AFTER_MS > new Date().getTime())
    {
      logger.debug("Lade Datum vom Terminologieserver (WebService-Aufruf):");

      logger.debug("LastDate: " + lastDate);
      logger.debug("termserverVersion: " + termserverVersion);
      logger.debug("RELOAD_AFTER_MS: " + RELOAD_AFTER_MS);
      
      GetTermserverVersionResponse.Return ret = WebServiceHelper.getVersion();
      termserverVersion = ret.getVersion();
      termserverDate = DateTimeHelper.ConvertXMLGregorianCalenderToDate(ret.getDate());

      lastDate = new Date();
    }

    return termserverDate;
  }

}
