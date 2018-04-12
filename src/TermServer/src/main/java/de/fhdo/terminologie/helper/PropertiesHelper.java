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

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */

public class PropertiesHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static PropertiesHelper instance;

  /**
   * @return the logger
   */
  public static org.apache.log4j.Logger getLogger()
  {
    return logger;
  }

  /**
   * @param aLogger the logger to set
   */
  public static void setLogger(org.apache.log4j.Logger aLogger)
  {
    logger = aLogger;
  }

  private String login_classname;
  
  private char exportCsvQualifier;
  private boolean exportCsvForceQualifier;
  private char delimiter;
  
  public static PropertiesHelper getInstance()
  {
    if (instance == null)
      instance = new PropertiesHelper();

    return instance;
  }

  public PropertiesHelper()
  {
    loadData();
  }

  private void loadData()
  {
    getLogger().debug("Load properties...");

    Properties config = new Properties();
    try
    {
      String filename = System.getProperty("catalina.base") + "/conf/termserver.properties";
      getLogger().debug("filename: " + filename);

      config.load(new FileInputStream(filename));

      // load properties
      login_classname = config.getProperty("login.classname", "UsernamePasswordMethod");
      
      String qf = config.getProperty("export.csv.qualifier", "\"");
      if(qf != null && qf.length() > 0)
        exportCsvQualifier = qf.charAt(0);
      else exportCsvQualifier = '"';
      
      qf = config.getProperty("export.csv.delimiter", ";");
      if(qf != null && qf.length() > 0)
        delimiter = qf.charAt(0);
      else delimiter = ';';
      
      //
      
      qf = config.getProperty("export.csv.forceQualifier", "false");
      if(qf != null && qf.length() > 0)
        exportCsvForceQualifier = getBooleanValue(qf);
      else exportCsvForceQualifier = false;
      
      //login_classname = config.getProperty("login.classname", "kjshdf");
      
      logger.debug("login_classname: " + login_classname);
      logger.debug("exportCsvQualifier: " + exportCsvQualifier);
      logger.debug("delimiter: " + delimiter);
      logger.debug("exportCsvForceQualifier: " + exportCsvForceQualifier);
    }
    catch (Exception e)
    {
      logger.error("[PropertiesHelper] error: " + e.getMessage());
    }

  }

  private boolean getBooleanValue(String s)
  {
    if (s == null || s.length() == 0)
      return false;

    try
    {
      return Boolean.parseBoolean(s);
    }
    catch(Exception ex)
    {
      
    }

    return false;
  }

  /**
   * @return the login_classname
   */
  public String getLoginClassname()
  {
    return login_classname;
  }

  /**
   * @return the exportCsvQualifier
   */
  public char getExportCsvQualifier()
  {
    return exportCsvQualifier;
  }

  /**
   * @return the exportCsvForceQualifier
   */
  public boolean isExportCsvForceQualifier()
  {
    return exportCsvForceQualifier;
  }

  /**
   * @return the delimiter
   */
  public char getDelimiter()
  {
    return delimiter;
  }

}
