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
package de.fhdo.gui;

import de.fhdo.helper.PropertiesHelper;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Index extends Window
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private String loginZul;
  
  public Index()
  {
    logger.debug("Index.zul - load login page...");
    loginZul = PropertiesHelper.getInstance().getLoginZul();
    logger.debug("zul: " + loginZul);
  }

  /**
   * @return the loginZul
   */
  public String getLoginZul()
  {
    return loginZul;
  }

  /**
   * @param loginZul the loginZul to set
   */
  public void setLoginZul(String loginZul)
  {
    this.loginZul = loginZul;
  }
  
  
  
  
}
