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

  private boolean guiConceptShowMetadata;
  private boolean guiConceptShowTranslations;
  private boolean guiConceptShowCrossMappings;
  private boolean guiConceptShowLinkedConcepts;
  private boolean guiConceptShowOntologies;

  private boolean guiShowCodesystems;
  private boolean guiShowValuesets;

  private boolean guiEditCodesystemsShowNew;
  private boolean guiEditCodesystemsShowEdit;
  private boolean guiEditCodesystemsShowDetails;

  private boolean guiEditConceptsShowNewRoot;
  private boolean guiEditConceptsShowNewSub;
  private boolean guiEditConceptsShowEdit;
  private boolean guiEditConceptsShowDelete;
  private boolean guiEditConceptsShowDetails;

  private int expandTreeAutoCount = 50;

  private long associationTaxonomyDefaultVersionId = 4;
  private long associationCrossmappingDefaultVersionId = 0;
  private long associationLinkDefaultVersionId = 0;

  private boolean guiShowOnlyVisibleConcepts;
  private boolean guiAllowShowingInvisibleConcepts;

  private String imgPath;

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

  public void reset()
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

      imgPath = config.getProperty("images.path", "/rsc/img/");
      configureImages();

      collaborationActive = getBooleanValue(config.getProperty("collaboration.active", "false"));

      guiCodesystemExpandable = getBooleanValue(config.getProperty("gui.codesystem.expandable", "false"));
      guiCodesystemMinimal = getBooleanValue(config.getProperty("gui.codesystem.minimal", "false"));
      guiCodesystemVersionExpandable = getBooleanValue(config.getProperty("gui.codesystem_version.expandable", "false"));
      guiCodesystemVersionMinimal = getBooleanValue(config.getProperty("gui.codesystem_version.minimal", "false"));
      guiConceptExpandable = getBooleanValue(config.getProperty("gui.concept.expandable", "false"));
      guiConceptMinimal = getBooleanValue(config.getProperty("gui.concept.minimal", "false"));

      guiConceptShowMetadata = getBooleanValue(config.getProperty("gui.concept.showMetadata", "true"));
      guiConceptShowTranslations = getBooleanValue(config.getProperty("gui.concept.showTranslations", "true"));
      guiConceptShowCrossMappings = getBooleanValue(config.getProperty("gui.concept.showCrossMappings", "true"));
      guiConceptShowLinkedConcepts = getBooleanValue(config.getProperty("gui.concept.showLinkedConcepts", "true"));
      guiConceptShowOntologies = getBooleanValue(config.getProperty("gui.concept.showOntologies", "true"));

      associationTaxonomyDefaultVersionId = getLongValue(config.getProperty("association.taxonomy.default.versionId", "4"), 4);
      associationCrossmappingDefaultVersionId = getLongValue(config.getProperty("association.crossmapping.default.versionId", "0"), 0);
      associationLinkDefaultVersionId = getLongValue(config.getProperty("association.link.default.versionId", "0"), 0);

      expandTreeAutoCount = getIntValue("gui.tree.expandTreeAutoCount", 50);

      guiShowOnlyVisibleConcepts = getBooleanValue(config.getProperty("gui.showOnlyVisibleConcepts", "true"));
      guiAllowShowingInvisibleConcepts = getBooleanValue(config.getProperty("gui.allowShowingInvisibleConcepts", "false"));

      guiShowCodesystems = getBooleanValue(config.getProperty("gui.showCodesystems", "true"));
      guiShowValuesets = getBooleanValue(config.getProperty("gui.showValuesets", "true"));
      guiEditCodesystemsShowNew = getBooleanValue(config.getProperty("gui.edit.codesystems.showNew", "true"));
      guiEditCodesystemsShowEdit = getBooleanValue(config.getProperty("gui.edit.codesystems.showEdit", "true"));
      guiEditCodesystemsShowDetails = getBooleanValue(config.getProperty("gui.edit.codesystems.showDetails", "true"));

      guiEditConceptsShowNewRoot = getBooleanValue(config.getProperty("gui.edit.concepts.showNewRoot", "true"));
      guiEditConceptsShowNewSub = getBooleanValue(config.getProperty("gui.edit.concepts.showNewSub", "true"));
      guiEditConceptsShowEdit = getBooleanValue(config.getProperty("gui.edit.concepts.showEdit", "true"));
      guiEditConceptsShowDelete = getBooleanValue(config.getProperty("gui.edit.concepts.showDelete", "true"));
      guiEditConceptsShowDetails = getBooleanValue(config.getProperty("gui.edit.concepts.showDetails", "true"));

      logger.debug("login_classname: " + login_classname); 
      logger.debug("termserverUrl: " + termserverUrl);
      logger.debug("termserverServiceName: " + termserverServiceName);
      logger.debug("collaborationActive: " + collaborationActive);
      logger.debug("expandTreeAutoCount: " + expandTreeAutoCount);

      logger.debug("guiShowOnlyVisibleConcepts: " + guiShowOnlyVisibleConcepts);
      logger.debug("guiAllowShowingInvisibleConcepts: " + guiAllowShowingInvisibleConcepts);
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
    catch (Exception ex)
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
    catch (Exception ex)
    {

    }

    return defaultValue;
  }

  private void configureImages()
  {
    if (imgPath != null)
    {
      if (imgPath.equalsIgnoreCase("/rsc/img/") == false)
      {
        // copy images in original folder
        
        
      }
    }
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
    catch (Exception ex)
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
   * @param associationTaxonomyDefaultVersionId the
   * associationTaxonomyDefaultVersionId to set
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
   * @param associationCrossmappingDefaultVersionId the
   * associationCrossmappingDefaultVersionId to set
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
   * @param associationLinkDefaultVersionId the associationLinkDefaultVersionId
   * to set
   */
  public void setAssociationLinkDefaultVersionId(long associationLinkDefaultVersionId)
  {
    this.associationLinkDefaultVersionId = associationLinkDefaultVersionId;
  }

  /**
   * @return the guiShowOnlyVisibleConcepts
   */
  public boolean isGuiShowOnlyVisibleConcepts()
  {
    return guiShowOnlyVisibleConcepts;
  }

  /**
   * @return the guiAllowShowingInvisibleConcepts
   */
  public boolean isGuiAllowShowingInvisibleConcepts()
  {
    return guiAllowShowingInvisibleConcepts;
  }

  /**
   * @return the guiConceptShowMetadata
   */
  public boolean isGuiConceptShowMetadata()
  {
    return guiConceptShowMetadata;
  }

  /**
   * @return the guiConceptShowTranslations
   */
  public boolean isGuiConceptShowTranslations()
  {
    return guiConceptShowTranslations;
  }

  /**
   * @return the guiConceptShowCrossMappings
   */
  public boolean isGuiConceptShowCrossMappings()
  {
    return guiConceptShowCrossMappings;
  }

  /**
   * @return the guiConceptShowLinkedConcepts
   */
  public boolean isGuiConceptShowLinkedConcepts()
  {
    return guiConceptShowLinkedConcepts;
  }

  /**
   * @return the guiConceptShowOntologies
   */
  public boolean isGuiConceptShowOntologies()
  {
    return guiConceptShowOntologies;
  }

  /**
   * @return the guiShowCodesystems
   */
  public boolean isGuiShowCodesystems()
  {
    return guiShowCodesystems;
  }

  /**
   * @return the guiShowValuesets
   */
  public boolean isGuiShowValuesets()
  {
    return guiShowValuesets;
  }

  /**
   * @return the guiEditCodesystemsShowNew
   */
  public boolean isGuiEditCodesystemsShowNew()
  {
    return guiEditCodesystemsShowNew;
  }

  /**
   * @return the guiEditCodesystemsShowEdit
   */
  public boolean isGuiEditCodesystemsShowEdit()
  {
    return guiEditCodesystemsShowEdit;
  }

  /**
   * @return the guiEditCodesystemsShowDetails
   */
  public boolean isGuiEditCodesystemsShowDetails()
  {
    return guiEditCodesystemsShowDetails;
  }

  /**
   * @return the guiEditConceptsShowNewRoot
   */
  public boolean isGuiEditConceptsShowNewRoot()
  {
    return guiEditConceptsShowNewRoot;
  }

  /**
   * @return the guiEditConceptsShowNewSub
   */
  public boolean isGuiEditConceptsShowNewSub()
  {
    return guiEditConceptsShowNewSub;
  }

  /**
   * @return the guiEditConceptsShowEdit
   */
  public boolean isGuiEditConceptsShowEdit()
  {
    return guiEditConceptsShowEdit;
  }

  /**
   * @return the guiEditConceptsShowDelete
   */
  public boolean isGuiEditConceptsShowDelete()
  {
    return guiEditConceptsShowDelete;
  }

  /**
   * @return the guiEditConceptsShowDetails
   */
  public boolean isGuiEditConceptsShowDetails()
  {
    return guiEditConceptsShowDetails;
  }

  /**
   * @return the imgPathRel
   */
  public String getImgPath()
  {
    return imgPath;
  }

  /**
   * @param imgPath the imgPath to set
   */
  public void setImgPath(String imgPath)
  {
    this.imgPath = imgPath;
  }

}
