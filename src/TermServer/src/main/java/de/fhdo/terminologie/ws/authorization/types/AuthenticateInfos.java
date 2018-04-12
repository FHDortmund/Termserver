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
package de.fhdo.terminologie.ws.authorization.types;

import de.fhdo.terminologie.db.hibernate.LicencedUser;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */

public class AuthenticateInfos
{
  private boolean loggedIn;
  private boolean isAdmin;
  private long userId;
  private List<LicencedUser> licences;
  private String message;

  public AuthenticateInfos()
  {
    loggedIn = false;
    isAdmin = false;
    userId = 0;
    licences = new LinkedList<LicencedUser>();
    message = "";
  }

  
  
  /**
   * @return the loggedIn
   */
  public boolean isLoggedIn()
  {
    return loggedIn;
  }

  /**
   * @param loggedIn the loggedIn to set
   */
  public void setLoggedIn(boolean loggedIn)
  {
    this.loggedIn = loggedIn;
  }

  /**
   * @return the isAdmin
   */
  public boolean isIsAdmin()
  {
    return isAdmin;
  }

  /**
   * @param isAdmin the isAdmin to set
   */
  public void setIsAdmin(boolean isAdmin)
  {
    this.isAdmin = isAdmin;
  }

  /**
   * @return the userId
   */
  public long getUserId()
  {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(long userId)
  {
    this.userId = userId;
  }

  /**
   * @return the licences
   */
  public List<LicencedUser> getLicences()
  {
    return licences;
  }

  /**
   * @param licences the licences to set
   */
  public void setLicences(List<LicencedUser> licences)
  {
    this.licences = licences;
  }

  /**
   * @return the message
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  
}
