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
package de.fhdo.terminologie.ws.types;

import de.fhdo.terminologie.db.hibernate.TermUser;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LoginInfoType
{
  private String loginToken;
  private TermUser termUser;
  private java.util.Date lastTimestamp;
  private String lastIP;

  /**
   * @return the login
   */
  public String getLoginToken()
  {
    return loginToken;
  }

  /**
   * @param login the login to set
   */
  public void setLoginToken(String login)
  {
    this.loginToken = login;
  }

  /**
   * @return the termUser
   */
  public TermUser getTermUser()
  {
    return termUser;
  }

  /**
   * @param termUser the termUser to set
   */
  public void setTermUser(TermUser termUser)
  {
    this.termUser = termUser;
  }

  /**
   * @return the lastTimestamp
   */
  public java.util.Date getLastTimestamp()
  {
    return lastTimestamp;
  }

  /**
   * @param lastTimestamp the lastTimestamp to set
   */
  public void setLastTimestamp(java.util.Date lastTimestamp)
  {
    this.lastTimestamp = lastTimestamp;
  }

  /**
   * @return the lastIP
   */
  public String getLastIP()
  {
    return lastIP;
  }

  /**
   * @param lastIP the lastIP to set
   */
  public void setLastIP(String lastIP)
  {
    this.lastIP = lastIP;
  }
  
}
