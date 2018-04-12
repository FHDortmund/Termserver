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
package de.fhdo.terminologie.ws.search;

import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.search.types.GetTermserverVersionResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemConceptsResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsInTaxonomyResponseType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesRequestType;
import de.fhdo.terminologie.ws.search.types.ListConceptAssociationTypesResponseType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.search.types.ListDomainsRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainsResponseType;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsRequestType;
import de.fhdo.terminologie.ws.search.types.ListGloballySearchedConceptsResponseType;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterRequestType;
import de.fhdo.terminologie.ws.search.types.ListMetadataParameterResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsByTermOrCodeResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsResponseType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptAssociationTypeDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptDetailsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetConceptMetadataRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetConceptMetadataResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsResponseType;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import com.sun.xml.ws.developer.SchemaValidation;
import javax.servlet.ServletContext;

/**
 * Search umfasst das Retrieval von Codesystemen, Konzepten sowie ValueSets.
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 * @version 1.0
 */
@WebService(serviceName = "Search")
@SchemaValidation
public class Search
{

  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.

  @Resource
  private WebServiceContext webServiceContext;
  
//  @Resource
//  private ServletContext servletContext;
  
  @WebMethod(operationName = "GetTermserverVersion")
  public GetTermserverVersionResponseType GetTermserverVersion()
  {
  
    return new GetTermserverVersionResponseType();
  }

  /**
   * <b>Liefert alle verfügbaren Vokabulare aus dem Terminologieserver.</b><br>
   * Vokabulare mit Lizenzen werden nur den angemeldeten Benutzern angezeigt,
   * welche Zugriff auf die entsprechenden Lizenzen haben.
   *
   * @param parameter Anfrage-Parameter
   * @return Antwort
   */
  @WebMethod(operationName = "ListCodeSystems")
  public ListCodeSystemsResponseType ListCodeSystems(@WebParam(name = "parameter") ListCodeSystemsRequestType parameter)
  {
    ListCodeSystems lcs = new ListCodeSystems();
    return lcs.ListCodeSystems(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListValueSets")
  public ListValueSetsResponseType ListValueSets(@WebParam(name = "parameter") ListValueSetsRequestType parameter)
  {
    return new ListValueSets().ListValueSets(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnValueSetDetails")
  public ReturnValueSetDetailsResponseType ReturnValueSetDetails(@WebParam(name = "parameter") ReturnValueSetDetailsRequestType parameter)
  {
    return new ReturnValueSetDetails().ReturnValueSetDetails(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnConceptDetails")
  public ReturnConceptDetailsResponseType ReturnConceptDetails(@WebParam(name = "parameter") ReturnConceptDetailsRequestType parameter)
  {
    return new ReturnConceptDetails().ReturnConceptDetails(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnCodeSystemDetails")
  public ReturnCodeSystemDetailsResponseType ReturnCodeSystemDetails(@WebParam(name = "parameter") ReturnCodeSystemDetailsRequestType parameter)
  {
    return new ReturnCodeSystemDetails().ReturnCodeSystemDetails(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListValueSetContents")
  public ListValueSetContentsResponseType ListValueSetContents(@WebParam(name = "parameter") ListValueSetContentsRequestType parameter)
  {
    return new ListValueSetContents().ListValueSetContents(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListCodeSystemConcepts")
  public ListCodeSystemConceptsResponseType ListCodeSystemConcepts(@WebParam(name = "parameter") ListCodeSystemConceptsRequestType parameter)
  {
    return new ListCodeSystemConcepts().ListCodeSystemConcepts(parameter, false, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListConceptAssociationTypes")
  public ListConceptAssociationTypesResponseType ListConceptAssociationTypes(@WebParam(name = "parameter") ListConceptAssociationTypesRequestType parameter)
  {
    return new ListConceptAssociationTypes().ListConceptAssociationTypes(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListDomains")
  public ListDomainsResponseType ListDomains(@WebParam(name = "parameter") ListDomainsRequestType parameter)
  {
    return new ListDomains().ListDomains(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnConceptAssociationTypeDetails")
  public ReturnConceptAssociationTypeDetailsResponseType ReturnConceptAssociationTypeDetails(@WebParam(name = "parameter") ReturnConceptAssociationTypeDetailsRequestType parameter)
  {
    return new ReturnConceptAssociationTypeDetails().ReturnConceptAssociationTypeDetails(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListDomainValues")
  public ListDomainValuesResponseType ListDomainValues(@WebParam(name = "parameter") ListDomainValuesRequestType parameter)
  {
    return new ListDomainValues().ListDomainValues(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListCodeSystemsInTaxonomy")
  public ListCodeSystemsInTaxonomyResponseType ListCodeSystemsInTaxonomy(@WebParam(name = "parameter") ListCodeSystemsInTaxonomyRequestType parameter)
  {
    return new ListCodeSystemsInTaxonomy().ListCodeSystemsInTaxonomy(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnValueSetConceptMetadata")
  public ReturnValueSetConceptMetadataResponseType ReturnValueSetConceptMetadata(@WebParam(name = "parameter") ReturnValueSetConceptMetadataRequestType parameter)
  {
    return new ReturnVsConceptMetadata().ReturnVsConceptMetadata(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListMetadataParameter")
  public ListMetadataParameterResponseType ListMetadataParameter(@WebParam(name = "parameter") ListMetadataParameterRequestType parameter)
  {
    return new ListMetadataParameter().ListMetadataParameter(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  /*@WebMethod(operationName = "ListValueSetContentsByTermOrCode")
  public ListValueSetContentsByTermOrCodeResponseType ListValueSetContentsByTermOrCode(@WebParam(name = "parameter") ListValueSetContentsByTermOrCodeRequestType parameter)
  {
    return new ListValueSetContentsByTermOrCode().ListValueSetContentsByTermOrCode(parameter, SecurityHelper.getIp(webServiceContext));
  }*/

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ReturnConceptValueSetMembership")
  public ReturnConceptValueSetMembershipResponseType ReturnConceptValueSetMembership(@WebParam(name = "parameter") ReturnConceptValueSetMembershipRequestType parameter)
  {
    return new ReturnConceptValueSetMembership().ReturnConceptValueSetMembership(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListGloballySearchedConcepts")
  public ListGloballySearchedConceptsResponseType ListGloballySearchedConcepts(@WebParam(name = "parameter") ListGloballySearchedConceptsRequestType parameter)
  {
    return new ListGloballySearchedConcepts().ListGloballySearchedConcepts(parameter, false, SecurityHelper.getIp(webServiceContext));
  }
}
