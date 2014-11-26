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
  private String termserverUrl;
  private String termserverServiceName;
  private boolean collaborationActive = false;
  
  private boolean guiCodesystemMinimal;
  private boolean guiCodesystemVersionMinimal;
  private boolean guiConceptMinimal;
  
  private boolean guiCodesystemExpandable;
  private boolean guiCodesystemVersionExpandable;
  private boolean guiConceptExpandable;
  
  private int expandTreeAutoCount = 50;
  
  private long associationTaxonomyDefaultVersionId = 4;
  private long associationCrossmappingDefaultVersionId = 0;
  private long associationLinkDefaultVersionId = 0;
  
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
      String filename = System.getProperty("catalina.base") + "/conf/termbrowser.properties";
      getLogger().debug("filename: " + filename);

      config.load(new FileInputStream(filename));

      // load properties
      login_classname = config.getProperty("login.classname", "de.fhdo.authorization.UsernamePasswordMethod");
      termserverUrl = config.getProperty("termserver.url", "http://localhost:8080/");
      termserverServiceName = config.getProperty("termserver.serviceName", "TermServer/");
      
      collaborationActive = getBooleanValue(config.getProperty("collaboration.active", "false"));
      
      guiCodesystemExpandable = getBooleanValue(config.getProperty("gui.codesystem.expandable", "false"));
      guiCodesystemMinimal = getBooleanValue(config.getProperty("gui.codesystem.minimal", "false"));
      guiCodesystemVersionExpandable = getBooleanValue(config.getProperty("gui.codesystem_version.expandable", "false"));
      guiCodesystemVersionMinimal = getBooleanValue(config.getProperty("gui.codesystem_version.minimal", "false"));
      guiConceptExpandable = getBooleanValue(config.getProperty("gui.concept.expandable", "false"));
      guiConceptMinimal = getBooleanValue(config.getProperty("gui.concept.minimal", "false"));
      
      associationTaxonomyDefaultVersionId = getLongValue(config.getProperty("association.taxonomy.default.versionId", "4"), 4);
      associationCrossmappingDefaultVersionId = getLongValue(config.getProperty("association.crossmapping.default.versionId", "0"), 0);
      associationLinkDefaultVersionId = getLongValue(config.getProperty("association.link.default.versionId", "0"), 0);
      
      expandTreeAutoCount = getIntValue("gui.tree.expandTreeAutoCount", 50);
      
      logger.debug("login_classname: " + login_classname);
      logger.debug("termserverUrl: " + termserverUrl);
      logger.debug("termserverServiceName: " + termserverServiceName);
      logger.debug("collaborationActive: " + collaborationActive);
      logger.debug("expandTreeAutoCount: " + expandTreeAutoCount);
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
  
  private long getLongValue(String s, long defaultValue)
  {
    if (s == null || s.length() == 0)
      return defaultValue;

    try
    {
      return Long.parseLong(s);
    }
    catch(Exception ex)
    {
      
    }

    return defaultValue;
  }
  
  private int getIntValue(String s)
  {
    return getIntValue(s, 0);
  }
  private int getIntValue(String s, int defaultValue)
  {
    if (s == null || s.length() == 0)
      return defaultValue;

    try
    {
      return Integer.parseInt(s);
    }
    catch(Exception ex)
    {
      
    }

    return defaultValue;
  }

  /**
   * @return the login_classname
   */
  public String getLoginClassname()
  {
    return login_classname;
  }

  /**
   * @return the termserverUrl
   */
  public String getTermserverUrl()
  {
    return termserverUrl;
  }

  /**
   * @return the termserverServiceName
   */
  public String getTermserverServiceName()
  {
    return termserverServiceName;
  }

  /**
   * @return the collaborationActive
   */
  public boolean isCollaborationActive()
  {
    return collaborationActive;
  }

  /**
   * @return the guiCodesystemMinimal
   */
  public boolean isGuiCodesystemMinimal()
  {
    return guiCodesystemMinimal;
  }

  /**
   * @return the guiCodesystemVersionMinimal
   */
  public boolean isGuiCodesystemVersionMinimal()
  {
    return guiCodesystemVersionMinimal;
  }

  /**
   * @return the guiConceptMinimal
   */
  public boolean isGuiConceptMinimal()
  {
    return guiConceptMinimal;
  }

  /**
   * @return the guiCodesystemExpandable
   */
  public boolean isGuiCodesystemExpandable()
  {
    return guiCodesystemExpandable;
  }

  /**
   * @return the guiCodesystemVersionExpandable
   */
  public boolean isGuiCodesystemVersionExpandable()
  {
    return guiCodesystemVersionExpandable;
  }

  /**
   * @return the guiConceptExpandable
   */
  public boolean isGuiConceptExpandable()
  {
    return guiConceptExpandable;
  }

  /**
   * @return the expandTreeAutoCount
   */
  public int getExpandTreeAutoCount()
  {
    return expandTreeAutoCount;
  }

  /**
   * @param expandTreeAutoCount the expandTreeAutoCount to set
   */
  public void setExpandTreeAutoCount(int expandTreeAutoCount)
  {
    this.expandTreeAutoCount = expandTreeAutoCount;
  }

  /**
   * @return the associationTaxonomyDefaultVersionId
   */
  public long getAssociationTaxonomyDefaultVersionId()
  {
    return associationTaxonomyDefaultVersionId;
  }

  /**
   * @param associationTaxonomyDefaultVersionId the associationTaxonomyDefaultVersionId to set
   */
  public void setAssociationTaxonomyDefaultVersionId(long associationTaxonomyDefaultVersionId)
  {
    this.associationTaxonomyDefaultVersionId = associationTaxonomyDefaultVersionId;
  }

  /**
   * @return the associationCrossmappingDefaultVersionId
   */
  public long getAssociationCrossmappingDefaultVersionId()
  {
    return associationCrossmappingDefaultVersionId;
  }

  /**
   * @param associationCrossmappingDefaultVersionId the associationCrossmappingDefaultVersionId to set
   */
  public void setAssociationCrossmappingDefaultVersionId(long associationCrossmappingDefaultVersionId)
  {
    this.associationCrossmappingDefaultVersionId = associationCrossmappingDefaultVersionId;
  }

  /**
   * @return the associationLinkDefaultVersionId
   */
  public long getAssociationLinkDefaultVersionId()
  {
    return associationLinkDefaultVersionId;
  }

  /**
   * @param associationLinkDefaultVersionId the associationLinkDefaultVersionId to set
   */
  public void setAssociationLinkDefaultVersionId(long associationLinkDefaultVersionId)
  {
    this.associationLinkDefaultVersionId = associationLinkDefaultVersionId;
  }

}
