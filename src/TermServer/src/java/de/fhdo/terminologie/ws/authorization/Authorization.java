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

import com.sun.xml.ws.developer.SchemaValidation;
import de.fhdo.terminologie.helper.PropertiesHelper;
import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.authorization.types.LoginResponseType;
import de.fhdo.terminologie.ws.authorization.types.LogoutResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@WebService(serviceName = "Authorization")
@SchemaValidation
public class Authorization
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  @Resource
  private WebServiceContext webServiceContext;

  @WebMethod(operationName = "Login")
  public LoginResponseType Login(@WebParam(name = "parameter") List<String> parameterList)
  {
    //SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
    IAuthorization auth = getAuthorizationClass();

    if(auth != null)
      return auth.Login(SecurityHelper.getIp(webServiceContext), parameterList);
    else
    {
      LoginResponseType response = new LoginResponseType();
      response.setReturnInfos(new ReturnType());
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("No Authorization class found, please specify a class name in the termserver.properties file located in tomcat/conf. Please see the documentation for more information.");
      return response;
    }
    //Login login = new Login();
    //return login.Login(parameter);
  }

  @WebMethod(operationName = "Logout")
  public LogoutResponseType Logout(@WebParam(name = "parameter") List<String> parameterList)
  {
    /*SecurityHelper.applyIPAdress(parameter.getLogin(), webServiceContext);
     Logout logout = new Logout();
     return logout.Logout(parameter);*/

    IAuthorization auth = getAuthorizationClass();
    
    if(auth != null)
      return auth.Logout(SecurityHelper.getIp(webServiceContext), parameterList);
    else
    {
      LogoutResponseType response = new LogoutResponseType();
      response.setReturnInfos(new ReturnType());
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("No Authorization class found, please specify a class name in the termserver.properties file located in tomcat/conf. Please see the documentation for more information.");
      return response;
    }
  }

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
  
  public static AuthenticateInfos authenticate(String ip, String loginToken)
  {
    IAuthorization auth = getAuthorizationClass();
    
    if(auth != null)
      return auth.Authenticate(ip, loginToken);
    
    return null;
  }
}
