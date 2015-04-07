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
import de.fhdo.terminologie.ws.administration.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.administration.Administration;
import de.fhdo.terminologie.ws.administration.Administration_Service;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.Authoring;
import de.fhdo.terminologie.ws.authoring.Authoring_Service;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptResponse;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetContentResponse;
import de.fhdo.terminologie.ws.authoring.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.CreateValueSetResponse;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainCodeSystemVersionResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainValueSetResponse;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.RemoveValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.RemoveValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateCodeSystemVersionStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateConceptValueSetMembershipStatusResponse;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.UpdateValueSetStatusResponse;
import de.fhdo.terminologie.ws.authorization.AuthenticateResponse;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.Authorization_Service;
import de.fhdo.terminologie.ws.authorization.ChangePassword;
import de.fhdo.terminologie.ws.authorization.ChangePasswordResponseType;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.authorization.LogoutResponseType;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations_Service;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.GetTermserverVersionResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsResponse;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.ListConceptAssociationTypesResponse;
import de.fhdo.terminologie.ws.search.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.ListDomainValuesResponse;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListGloballySearchedConceptsResponse;
import de.fhdo.terminologie.ws.search.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.ListMetadataParameterResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetConceptMetadataResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsResponse;
import de.fhdo.terminologie.ws.search.Search;
import de.fhdo.terminologie.ws.search.Search_Service;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Becker
 */
public class WebServiceHelper
{

  // Administration //////////////////////////////////////////////////////////

  private static String optimizeUrl(String url)
  {
    if (url.startsWith("http://") == false)
      return "http://" + url;
    if (url.endsWith("/") == false)
      return url + "/";
    if (url.startsWith("/"))
      return url.substring(1);
    
    return url;
  }

  public static ExportCodeSystemContentResponse.Return exportCodeSystemContent(ExportCodeSystemContentRequestType parameter)
  {
    return exportCodeSystemContent(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ExportCodeSystemContentResponse.Return exportCodeSystemContent(ExportCodeSystemContentRequestType parameter, String urlHost)
  {
    return exportCodeSystemContent(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ExportCodeSystemContentResponse.Return exportCodeSystemContent(ExportCodeSystemContentRequestType parameter, String urlHost, String urlService)
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
    return port.exportCodeSystemContent(parameter);
  }

  public static ActualProceedingsResponseType actualProceedings(ActualProceedingsRequestType parameter)
  {
    return actualProceedings(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ActualProceedingsResponseType actualProceedings(ActualProceedingsRequestType parameter, String urlHost)
  {
    return actualProceedings(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ActualProceedingsResponseType actualProceedings(ActualProceedingsRequestType parameter, String urlHost, String urlService)
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
    return port.actualProceedings(parameter);
  }

  public static ExportValueSetContentResponse.Return exportValueSetContent(ExportValueSetContentRequestType parameter)
  {
    return exportValueSetContent(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ExportValueSetContentResponse.Return exportValueSetContent(ExportValueSetContentRequestType parameter, String urlHost)
  {
    return exportValueSetContent(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ExportValueSetContentResponse.Return exportValueSetContent(ExportValueSetContentRequestType parameter, String urlHost, String urlService)
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
    return port.exportValueSetContent(parameter);
  }

  // Authoring ///////////////////////////////////////////////////////////////
  public static CreateConceptResponse.Return createConcept(CreateConceptRequestType parameter)
  {
    return createConcept(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static CreateConceptResponse.Return createConcept(CreateConceptRequestType parameter, String urlHost)
  {
    return createConcept(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static CreateConceptResponse.Return createConcept(CreateConceptRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.createConcept(parameter);
  }

  public static RemoveValueSetContentResponseType removeValueSetContent(RemoveValueSetContentRequestType parameter)
  {
    return removeValueSetContent(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static RemoveValueSetContentResponseType removeValueSetContent(RemoveValueSetContentRequestType parameter, String urlHost)
  {
    return removeValueSetContent(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static RemoveValueSetContentResponseType removeValueSetContent(RemoveValueSetContentRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.removeValueSetContent(parameter);
  }

  public static CreateValueSetContentResponse.Return createValueSetContent(CreateValueSetContentRequestType parameter)
  {
    return createValueSetContent(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static CreateValueSetContentResponse.Return createValueSetContent(CreateValueSetContentRequestType parameter, String urlHost)
  {
    return createValueSetContent(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static CreateValueSetContentResponse.Return createValueSetContent(CreateValueSetContentRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.createValueSetContent(parameter);
  }

  public static CreateCodeSystemResponse.Return createCodeSystem(CreateCodeSystemRequestType parameter)
  {
    return createCodeSystem(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static CreateCodeSystemResponse.Return createCodeSystem(CreateCodeSystemRequestType parameter, String urlHost)
  {
    return createCodeSystem(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static CreateCodeSystemResponse.Return createCodeSystem(CreateCodeSystemRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }

  public static CreateValueSetResponse.Return createValueSet(CreateValueSetRequestType parameter)
  {
    return createValueSet(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static CreateValueSetResponse.Return createValueSet(CreateValueSetRequestType parameter, String urlHost)
  {
    return createValueSet(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static CreateValueSetResponse.Return createValueSet(CreateValueSetRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.createValueSet(parameter);
  }

  public static MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter)
  {
    return maintainCodeSystemVersion(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter, String urlHost)
  {
    return maintainCodeSystemVersion(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static MaintainCodeSystemVersionResponse.Return maintainCodeSystemVersion(MaintainCodeSystemVersionRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.maintainCodeSystemVersion(parameter);
  }

  public static MaintainValueSetResponse.Return maintainValueSet(MaintainValueSetRequestType parameter)
  {
    return maintainValueSet(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static MaintainValueSetResponse.Return maintainValueSet(MaintainValueSetRequestType parameter, String urlHost)
  {
    return maintainValueSet(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static MaintainValueSetResponse.Return maintainValueSet(MaintainValueSetRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.maintainValueSet(parameter);
  }

  public static MaintainConceptResponseType maintainConcept(MaintainConceptRequestType parameter)
  {
    return maintainConcept(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static MaintainConceptResponseType maintainConcept(MaintainConceptRequestType parameter, String urlHost)
  {
    return maintainConcept(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static MaintainConceptResponseType maintainConcept(MaintainConceptRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.maintainConcept(parameter);
  }

  public static MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter)
  {
    return maintainConceptValueSetMembership(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter, String urlHost)
  {
    return maintainConceptValueSetMembership(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static MaintainConceptValueSetMembershipResponse.Return maintainConceptValueSetMembership(MaintainConceptValueSetMembershipRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.maintainConceptValueSetMembership(parameter);
  }

  public static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter)
  {
    return updateCodeSystemVersionStatus(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter, String urlHost)
  {
    return updateCodeSystemVersionStatus(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static UpdateCodeSystemVersionStatusResponse.Return updateCodeSystemVersionStatus(UpdateCodeSystemVersionStatusRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.updateCodeSystemVersionStatus(parameter);
  }

  public static UpdateValueSetStatusResponse.Return updateValueSetStatus(UpdateValueSetStatusRequestType parameter)
  {
    return updateValueSetStatus(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static UpdateValueSetStatusResponse.Return updateValueSetStatus(UpdateValueSetStatusRequestType parameter, String urlHost)
  {
    return updateValueSetStatus(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static UpdateValueSetStatusResponse.Return updateValueSetStatus(UpdateValueSetStatusRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.updateValueSetStatus(parameter);
  }

  public static UpdateConceptStatusResponse.Return updateConceptStatus(UpdateConceptStatusRequestType parameter)
  {
    return updateConceptStatus(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static UpdateConceptStatusResponse.Return updateConceptStatus(UpdateConceptStatusRequestType parameter, String urlHost)
  {
    return updateConceptStatus(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static UpdateConceptStatusResponse.Return updateConceptStatus(UpdateConceptStatusRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.updateConceptStatus(parameter);
  }

  public static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter)
  {
    return updateConceptValueSetMembershipStatus(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter, String urlHost)
  {
    return updateConceptValueSetMembershipStatus(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static UpdateConceptValueSetMembershipStatusResponse.Return updateConceptValueSetMembershipStatus(UpdateConceptValueSetMembershipStatusRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.updateConceptValueSetMembershipStatus(parameter);
  }

  // Authorization ///////////////////////////////////////////////////////////       
  public static LoginResponse.Return login(List<String> parameter)
  {
    return login(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static LoginResponse.Return login(List<String> parameter, String urlHost)
  {
    return login(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static LoginResponse.Return login(List<String> parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authorization_Service();
    }
    port = service.getAuthorizationPort();
    return port.login(parameter);
  }
  
  
  public static AuthenticateResponse.Return authenticate(List<String> parameter)
  {
    return authenticate(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static AuthenticateResponse.Return authenticate(List<String> parameter, String urlHost)
  {
    return authenticate(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static AuthenticateResponse.Return authenticate(List<String> parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authorization_Service();
    }
    port = service.getAuthorizationPort();
    return port.authenticate(parameter);
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
  
  
  public static ChangePasswordResponseType changePassword(List<String> parameter)
  {
    return changePassword(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ChangePasswordResponseType changePassword(List<String> parameter, String urlHost)
  {
    return changePassword(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ChangePasswordResponseType changePassword(List<String> parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authorization_Service();
    }
    port = service.getAuthorizationPort();
    return port.changePassword(parameter);
  }
  

  // ConceptAssociations  ////////////////////////////////////////////////////  
  public static ListConceptAssociationsResponse.Return listConceptAssociations(ListConceptAssociationsRequestType parameter)
  {
    return listConceptAssociations(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListConceptAssociationsResponse.Return listConceptAssociations(ListConceptAssociationsRequestType parameter, String urlHost)
  {
    return listConceptAssociations(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListConceptAssociationsResponse.Return listConceptAssociations(ListConceptAssociationsRequestType parameter, String urlHost, String urlService)
  {
    ConceptAssociations_Service service;
    ConceptAssociations port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new ConceptAssociations_Service(new URL(optimizeUrl(urlHost) + urlService + "ConceptAssociations?wsdl"),
              new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ConceptAssociations"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new ConceptAssociations_Service();
    }
    port = service.getConceptAssociationsPort();
    return port.listConceptAssociations(parameter);
  }

  public static CreateConceptAssociationResponse.Return createConceptAssociation(CreateConceptAssociationRequestType parameter)
  {
    return createConceptAssociation(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static CreateConceptAssociationResponse.Return createConceptAssociation(CreateConceptAssociationRequestType parameter, String urlHost)
  {
    return createConceptAssociation(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static CreateConceptAssociationResponse.Return createConceptAssociation(CreateConceptAssociationRequestType parameter, String urlHost, String urlService)
  {
    ConceptAssociations_Service service;
    ConceptAssociations port;
    try
    {
      // Service mit bestimmter URL ?ffnen
      service = new ConceptAssociations_Service(new URL(optimizeUrl(urlHost) + urlService + "ConceptAssociations?wsdl"),
              new QName("http://conceptAssociation.ws.terminologie.fhdo.de/", "ConceptAssociations"));
    }
    catch (Exception ex)
    {
      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);

      // Standard Service ?ffnen
      service = new ConceptAssociations_Service();
    }
    port = service.getConceptAssociationsPort();
    return port.createConceptAssociation(parameter);
  }

  // Search //////////////////////////////////////////////////////////////////    
  public static ListMetadataParameterResponse.Return listMetadataParameter(ListMetadataParameterRequestType parameter)
  {
    return listMetadataParameter(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListMetadataParameterResponse.Return listMetadataParameter(ListMetadataParameterRequestType parameter, String urlHost)
  {
    return listMetadataParameter(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListMetadataParameterResponse.Return listMetadataParameter(ListMetadataParameterRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listMetadataParameter(parameter);
  }

  public static ListDomainValuesResponse.Return listDomainValues(ListDomainValuesRequestType parameter)
  {
    return listDomainValues(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListDomainValuesResponse.Return listDomainValues(ListDomainValuesRequestType parameter, String urlHost)
  {
    return listDomainValues(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListDomainValuesResponse.Return listDomainValues(ListDomainValuesRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listDomainValues(parameter);
  }

  public static ListCodeSystemsInTaxonomyResponse.Return listCodeSystemsInTaxonomy(ListCodeSystemsInTaxonomyRequestType parameter)
  {
    return listCodeSystemsInTaxonomy(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListCodeSystemsInTaxonomyResponse.Return listCodeSystemsInTaxonomy(ListCodeSystemsInTaxonomyRequestType parameter, String urlHost)
  {
    return listCodeSystemsInTaxonomy(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListCodeSystemsInTaxonomyResponse.Return listCodeSystemsInTaxonomy(ListCodeSystemsInTaxonomyRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listCodeSystemsInTaxonomy(parameter);
  }

  public static ListValueSetsResponse.Return listValueSets(ListValueSetsRequestType parameter)
  {
    return listValueSets(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListValueSetsResponse.Return listValueSets(ListValueSetsRequestType parameter, String urlHost)
  {
    return listValueSets(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListValueSetsResponse.Return listValueSets(ListValueSetsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listValueSets(parameter);
  }

  public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(long codeSystemVersionId)
  {
    ListCodeSystemConceptsRequestType request = new ListCodeSystemConceptsRequestType();
    request.setLoginToken(SessionHelper.getSessionId());
    request.setCodeSystem(new CodeSystem());
    CodeSystemVersion csv = new CodeSystemVersion();
    csv.setVersionId(codeSystemVersionId);
    request.getCodeSystem().getCodeSystemVersions().add(csv);
    return listCodeSystemConcepts(request, PropertiesHelper.getInstance().getTermserverUrl());
  }
  
  public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter)
  {
    return listCodeSystemConcepts(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, String urlHost)
  {
    return listCodeSystemConcepts(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListCodeSystemConceptsResponse.Return listCodeSystemConcepts(ListCodeSystemConceptsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listCodeSystemConcepts(parameter);
  }

  
  public static ListCodeSystemsResponse.Return listCodeSystems(ListCodeSystemsRequestType parameter)
  {
    return listCodeSystems(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListCodeSystemsResponse.Return listCodeSystems(ListCodeSystemsRequestType parameter, String urlHost)
  {
    return listCodeSystems(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListCodeSystemsResponse.Return listCodeSystems(ListCodeSystemsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listCodeSystems(parameter);
  }
  
  
  public static ListGloballySearchedConceptsResponse.Return listGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter)
  {
    return listGloballySearchedConcepts(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListGloballySearchedConceptsResponse.Return listGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, String urlHost)
  {
    return listGloballySearchedConcepts(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListGloballySearchedConceptsResponse.Return listGloballySearchedConcepts(ListGloballySearchedConceptsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listGloballySearchedConcepts(parameter);
  }

  public static ListValueSetContentsResponse.Return listValueSetContents(ListValueSetContentsRequestType parameter)
  {
    return listValueSetContents(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListValueSetContentsResponse.Return listValueSetContents(ListValueSetContentsRequestType parameter, String urlHost)
  {
    return listValueSetContents(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListValueSetContentsResponse.Return listValueSetContents(ListValueSetContentsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listValueSetContents(parameter);
  }

//  public static ListValueSetContentsByTermOrCodeResponse.Return listValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter)
//  {
//    return listValueSetContentsByTermOrCode(parameter, PropertiesHelper.getInstance().getTermserverUrl());
//  }
//
//  public static ListValueSetContentsByTermOrCodeResponse.Return listValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter, String urlHost)
//  {
//    return listValueSetContentsByTermOrCode(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
//  }
//
//  public static ListValueSetContentsByTermOrCodeResponse.Return listValueSetContentsByTermOrCode(ListValueSetContentsByTermOrCodeRequestType parameter, String urlHost, String urlService)
//  {
//    Search_Service service;
//    Search port;
//    try
//    {
//      // Service mit bestimmter URL ?ffnen
//      service = new Search_Service(new URL(optimizeUrl(urlHost) + urlService + "Search?wsdl"),
//              new QName("http://search.ws.terminologie.fhdo.de/", "Search"));
//    }
//    catch (Exception ex)
//    {
//      Logger.getLogger(WebServiceHelper.class.getName()).log(Level.SEVERE, null, ex);
//
//      // Standard Service ?ffnen
//      service = new Search_Service();
//    }
//    port = service.getSearchPort();
//    return port.listValueSetContentsByTermOrCode(parameter);
//  }

  public static ReturnCodeSystemDetailsResponse.Return returnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter)
  {
    return returnCodeSystemDetails(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ReturnCodeSystemDetailsResponse.Return returnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter, String urlHost)
  {
    return returnCodeSystemDetails(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ReturnCodeSystemDetailsResponse.Return returnCodeSystemDetails(ReturnCodeSystemDetailsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnCodeSystemDetails(parameter);
  }

  public static ReturnValueSetDetailsResponse.Return returnValueSetDetails(ReturnValueSetDetailsRequestType parameter)
  {
    return returnValueSetDetails(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ReturnValueSetDetailsResponse.Return returnValueSetDetails(ReturnValueSetDetailsRequestType parameter, String urlHost)
  {
    return returnValueSetDetails(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ReturnValueSetDetailsResponse.Return returnValueSetDetails(ReturnValueSetDetailsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnValueSetDetails(parameter);
  }

  public static ReturnConceptDetailsResponse.Return returnConceptDetails(ReturnConceptDetailsRequestType parameter)
  {
    return returnConceptDetails(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ReturnConceptDetailsResponse.Return returnConceptDetails(ReturnConceptDetailsRequestType parameter, String urlHost)
  {
    return returnConceptDetails(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ReturnConceptDetailsResponse.Return returnConceptDetails(ReturnConceptDetailsRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnConceptDetails(parameter);
  }

  public static ReturnConceptValueSetMembershipResponse.Return returnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter)
  {
    return returnConceptValueSetMembership(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ReturnConceptValueSetMembershipResponse.Return returnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter, String urlHost)
  {
    return returnConceptValueSetMembership(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ReturnConceptValueSetMembershipResponse.Return returnConceptValueSetMembership(ReturnConceptValueSetMembershipRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnConceptValueSetMembership(parameter);
  }

  public static ReturnValueSetConceptMetadataResponse.Return returnValueSetConceptMetadata(ReturnValueSetConceptMetadataRequestType parameter)
  {
    return returnValueSetConceptMetadata(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ReturnValueSetConceptMetadataResponse.Return returnValueSetConceptMetadata(ReturnValueSetConceptMetadataRequestType parameter, String urlHost)
  {
    return returnValueSetConceptMetadata(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ReturnValueSetConceptMetadataResponse.Return returnValueSetConceptMetadata(ReturnValueSetConceptMetadataRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnValueSetConceptMetadata(parameter);
  }
  
  
  public static GetTermserverVersionResponse.Return getVersion()
  {
    Search_Service service;
    Search port;
    try
    {
      String url = PropertiesHelper.getInstance().getTermserverUrl();
      // Service mit bestimmter URL ?ffnen
      service = new Search_Service(new URL(optimizeUrl(url) + PropertiesHelper.getInstance().getTermserverServiceName() + "Search?wsdl"),
              new QName("http://search.ws.terminologie.fhdo.de/", "Search"));
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, WebServiceHelper.class);

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.getTermserverVersion();
  }
  
  
  public static RemoveTerminologyOrConceptResponseType removeTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter)
  {
    return removeTerminologyOrConcept(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static RemoveTerminologyOrConceptResponseType removeTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter, String urlHost)
  {
    return removeTerminologyOrConcept(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static RemoveTerminologyOrConceptResponseType removeTerminologyOrConcept(RemoveTerminologyOrConceptRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.removeTerminologyOrConcept(parameter);
  }
  

  public static ListConceptAssociationTypesResponse.Return listConceptAssociationTypes(ListConceptAssociationTypesRequestType parameter)
  {
    return listConceptAssociationTypes(parameter, PropertiesHelper.getInstance().getTermserverUrl());
  }

  public static ListConceptAssociationTypesResponse.Return listConceptAssociationTypes(ListConceptAssociationTypesRequestType parameter, String urlHost)
  {
    return listConceptAssociationTypes(parameter, urlHost, PropertiesHelper.getInstance().getTermserverServiceName());
  }

  public static ListConceptAssociationTypesResponse.Return listConceptAssociationTypes(ListConceptAssociationTypesRequestType parameter, String urlHost, String urlService)
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.listConceptAssociationTypes(parameter);
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

      // Standard Service ?ffnen
      service = new Search_Service();
    }
    port = service.getSearchPort();
    return port.returnConceptAssociationTypeDetails(parameter);
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

      // Standard Service ?ffnen
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

      // Standard Service ?ffnen
      service = new Authoring_Service();
    }
    port = service.getAuthoringPort();
    return port.maintainConceptAssociationType(parameter);
  }
}
