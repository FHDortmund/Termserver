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
package de.fhdo.terminologie.ws.search.types;

import de.fhdo.logging.LoggingOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@XmlRootElement
@XmlType(name = "", propOrder = { "version", "date"})
public class GetTermserverVersionResponseType
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private String version;
  private Date date;

  public GetTermserverVersionResponseType()
  {
    logger.debug("GetTermserverVersionResponseType()");
    try
    {
      ResourceBundle rb = ResourceBundle.getBundle("version");
      version = rb.getString("application.version");
      logger.debug("version: " + version);
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
      date = sdf.parse(rb.getString("application.date"));
      logger.debug("date: " + sdf.format(date));
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  /**
   * @return the version
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * @return the date
   */
  public Date getDate()
  {
    return date;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date)
  {
    this.date = date;
  }

}
