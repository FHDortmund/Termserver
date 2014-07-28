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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.authorization.types.LoginResponseType;
import de.fhdo.terminologie.ws.authorization.types.LogoutResponseType;
import java.util.List;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */


public interface IAuthorization
{
  /**
   * Performs a login on the terminology server.
   * 
   * @param IP
   * @param parameterList A list of authentication credentials, can be username and password
   * @return information about the login
   */
  public LoginResponseType Login(String IP, List<String> parameterList);
  
  /**
   * Perfoms a logout on the terminology server.
   * 
   * @param IP
   * @param parameterList A list of parameters, can obtain a sessionId
   * @return information about the logout
   */
  public LogoutResponseType Logout(String IP, List<String> parameterList);
  
  /**
   * Checks wheter a user is logged in or not. Is used in various services.
   * 
   * @param IP
   * @param parameterList
   * @return true, if user is logged in
   */
  public AuthenticateInfos Authenticate(String IP, String loginToken);
  
  //public boolean HasRights(); // TODO
}
