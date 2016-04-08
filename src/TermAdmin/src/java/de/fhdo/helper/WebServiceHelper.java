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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.administration.Administration;
import de.fhdo.terminologie.ws.administration.Administration_Service;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelResponseType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusResponse;
import de.fhdo.terminologie.ws.administration.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.ImportValueSetResponse;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.Authorization_Service;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.soap.SOAPBinding;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class WebServiceHelper
{

  private static String optimizeUrl(String url)
  {
    if (url.startsWith("http://") == false && url.startsWith("https://") == false)
      return "http://" + url;
    if (url.endsWith("/") == false)
      return url + "/";
    if (url.startsWith("/"))
      return url.substring(1);

    return url;
  }

  public static LoginResponse.Return login(List<String> parameter) throws Exception
  {
    return login(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static LoginResponse.Return login(List<String> parameter, String urlHost) throws Exception
  {
    return login(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static LoginResponse.Return login(List<String> parameter, String urlHost, String urlService) throws Exception
  {
    Authorization_Service service;
    Authorization port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Authorization_Service(new URL(optimizeUrl(urlHost) + urlService + "Authorization?wsdl"),
              new QName("http://authorization.ws.terminologie.fhdo.de/", "Authorization"));
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, WebServiceHelper.class);

      throw ex;
    }
    port = service.getAuthorizationPort();
    return port.login(parameter);
  }

  public static LogoutResponseType logout(List<String> parameter)
  {
    return logout(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static LogoutResponseType logout(List<String> parameter, String urlHost)
  {
    return logout(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static LogoutResponseType logout(List<String> parameter, String urlHost, String urlService)
  {
    Authorization_Service service;
    Authorization port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Authorization_Service(new URL(optimizeUrl(urlHost) + urlService + "Authorization?wsdl"),
              new QName("http://authorization.ws.terminologie.fhdo.de/", "Authorization"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service oeffnen
      service = new Authorization_Service();
    }
    port = service.getAuthorizationPort();
    return port.logout(parameter);
  }

  public static ImportValueSetResponse.Return importValueSet(ImportValueSetRequestType parameter)
  {
    return importValueSet(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ImportValueSetResponse.Return importValueSet(ImportValueSetRequestType parameter, String urlHost)
  {
    return importValueSet(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ImportValueSetResponse.Return importValueSet(ImportValueSetRequestType parameter, String urlHost, String urlService)
  {
    Administration_Service service;
    Administration port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Administration_Service(new URL(optimizeUrl(urlHost) + urlService + "Administration?wsdl"),
              new QName("http://administration.ws.terminologie.fhdo.de/", "Administration"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new Administration_Service();
    }
    port = service.getAdministrationPort();
    return port.importValueSet(parameter);
  }

  public static ImportCodeSystemResponse.Return importCodeSystem(ImportCodeSystemRequestType parameter)
  {
    return importCodeSystem(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ImportCodeSystemResponse.Return importCodeSystem(ImportCodeSystemRequestType parameter, String urlHost)
  {
    return importCodeSystem(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ImportCodeSystemResponse.Return importCodeSystem(ImportCodeSystemRequestType parameter, String urlHost, String urlService)
  {
    Administration_Service service;
    Administration port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Administration_Service(new URL(optimizeUrl(urlHost) + urlService + "Administration?wsdl"),
              new QName("http://administration.ws.terminologie.fhdo.de/", "Administration"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new Administration_Service();
    }
    port = service.getAdministrationPort();
    return port.importCodeSystem(parameter);
  }

  public static Response<ImportCodeSystemResponse> importCodeSystemAsync(ImportCodeSystemRequestType parameter, AsyncHandler<ImportCodeSystemResponse> handler)
  {
    return importCodeSystemAsync(parameter, handler, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static Response<ImportCodeSystemResponse> importCodeSystemAsync(ImportCodeSystemRequestType parameter, AsyncHandler<ImportCodeSystemResponse> handler, String urlHost)
  {
    return importCodeSystemAsync(parameter, handler, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static Response<ImportCodeSystemResponse> importCodeSystemAsync(ImportCodeSystemRequestType parameter, AsyncHandler<ImportCodeSystemResponse> handler,
          String urlHost, String urlService)
  {
    Administration_Service service;
    Administration port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Administration_Service(new URL(optimizeUrl(urlHost) + urlService + "Administration?wsdl"),
              new QName("http://administration.ws.terminologie.fhdo.de/", "Administration"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new Administration_Service();
    }
    port = service.getAdministrationPort();

    //enable MTOM in client
    BindingProvider bp = (BindingProvider) port;
    SOAPBinding binding = (SOAPBinding) bp.getBinding();
    binding.setMTOMEnabled(true);

    return (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(parameter, handler);
  }

  public static ImportCodeSystemStatusResponse.Return importCodeSystemStatus(ImportCodeSystemStatusRequestType parameter)
  {
    return importCodeSystemStatus(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ImportCodeSystemStatusResponse.Return importCodeSystemStatus(ImportCodeSystemStatusRequestType parameter, String urlHost)
  {
    return importCodeSystemStatus(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ImportCodeSystemStatusResponse.Return importCodeSystemStatus(ImportCodeSystemStatusRequestType parameter, String urlHost, String urlService)
  {
    Administration_Service service;
    Administration port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Administration_Service(new URL(optimizeUrl(urlHost) + urlService + "Administration?wsdl"),
              new QName("http://administration.ws.terminologie.fhdo.de/", "Administration"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new Administration_Service();
    }
    port = service.getAdministrationPort();
    return port.importCodeSystemStatus(parameter);
  }

  public static ImportCodeSystemCancelResponseType importCodeSystemCancel(ImportCodeSystemCancelRequestType parameter)
  {
    return importCodeSystemCancel(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ImportCodeSystemCancelResponseType importCodeSystemCancel(ImportCodeSystemCancelRequestType parameter, String urlHost)
  {
    return importCodeSystemCancel(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ImportCodeSystemCancelResponseType importCodeSystemCancel(ImportCodeSystemCancelRequestType parameter, String urlHost, String urlService)
  {
    Administration_Service service;
    Administration port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Administration_Service(new URL(optimizeUrl(urlHost) + urlService + "Administration?wsdl"),
              new QName("http://administration.ws.terminologie.fhdo.de/", "Administration"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new Administration_Service();
    }
    port = service.getAdministrationPort();
    return port.importCodeSystemCancel(parameter);
  }

  public static CreateConceptAssociationTypeResponse.Return createConceptAssociationType(CreateConceptAssociationTypeRequestType parameter)
  {
    return createConceptAssociationType(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static CreateConceptAssociationTypeResponse.Return createConceptAssociationType(CreateConceptAssociationTypeRequestType parameter, String urlHost)
  {
    return createConceptAssociationType(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static CreateConceptAssociationTypeResponse.Return createConceptAssociationType(CreateConceptAssociationTypeRequestType parameter, String urlHost, String urlService)
  {
    Authoring_Service service;
    Authoring port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Authoring_Service(new URL(optimizeUrl(urlHost) + urlService + "Authoring?wsdl"),
              new QName("http://authoring.ws.terminologie.fhdo.de/", "Authoring"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.createConceptAssociationType(parameter);
  }

  public static MaintainConceptAssociationTypeResponse.Return maintainConceptAssociationType(MaintainConceptAssociationTypeRequestType parameter)
  {
    return maintainConceptAssociationType(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static MaintainConceptAssociationTypeResponse.Return maintainConceptAssociationType(MaintainConceptAssociationTypeRequestType parameter, String urlHost)
  {
    return maintainConceptAssociationType(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static MaintainConceptAssociationTypeResponse.Return maintainConceptAssociationType(MaintainConceptAssociationTypeRequestType parameter, String urlHost, String urlService)
  {
    Authoring_Service service;
    Authoring port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Authoring_Service(new URL(optimizeUrl(urlHost) + urlService + "Authoring?wsdl"),
              new QName("http://authoring.ws.terminologie.fhdo.de/", "Authoring"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.maintainConceptAssociationType(parameter);
  }

  public static ReturnConceptAssociationTypeDetailsResponse.Return returnConceptAssociationTypeDetails(ReturnConceptAssociationTypeDetailsRequestType parameter)
  {
    return returnConceptAssociationTypeDetails(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ReturnConceptAssociationTypeDetailsResponse.Return returnConceptAssociationTypeDetails(ReturnConceptAssociationTypeDetailsRequestType parameter, String urlHost)
  {
    return returnConceptAssociationTypeDetails(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ReturnConceptAssociationTypeDetailsResponse.Return returnConceptAssociationTypeDetails(ReturnConceptAssociationTypeDetailsRequestType parameter, String urlHost, String urlService)
  {
    Search_Service service;
    Search port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new Search_Service(new URL(optimizeUrl(urlHost) + urlService + "Search?wsdl"),
              new QName("http://search.ws.terminologie.fhdo.de/", "Search"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnConceptAssociationTypeDetails(parameter);
  }
}
