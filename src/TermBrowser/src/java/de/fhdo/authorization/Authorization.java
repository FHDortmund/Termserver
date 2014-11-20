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
package de.fhdo.authorization;

import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.ViewHelper;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Authorization
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private static IAuthorization getAuthorizationClass()
  {
    String className = PropertiesHelper.getInstance().getLoginClassname();

    try
    {
      logger.debug("getAuthorizationClass() - get class from name: " + className);
      Class authClass = Class.forName(className);
      return (IAuthorization)authClass.newInstance();
    }
    catch (Exception ex)
    {
      logger.error("No Authorization class found, className: " + className);
      logger.error("Please specify a class name in the termserver.properties file located in tomcat/conf. Please see the documentation for more information.");
    }
    return null;
  }
  
  public static void login()
  {
    IAuthorization auth = getAuthorizationClass();
    
    if(auth != null)
    {
      if(auth.doLogin())
      {
        ViewHelper.gotoSrc(null);
      }
    }
  }
  public static void login(String username, String password)
  {
    IAuthorization auth = getAuthorizationClass();
    
    if(auth != null)
    {
      if(auth.doLogin(username, password))
      {
        logger.debug("login successful");
        //ViewHelper.gotoSrc(null);
      }
    }
  }
  public static void logout()
  {
    IAuthorization auth = getAuthorizationClass();
    
    if(auth != null)
    {
      if(auth.doLogout())
      {
        Executions.sendRedirect("/gui/main/main.zul");
      }
    }
  }
}
