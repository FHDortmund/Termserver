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
package de.fhdo.terminologie.ws.authoring;

import com.sun.xml.ws.developer.SchemaValidation;
import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.DeleteValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.DeleteValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainCodeSystemVersionResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptValueSetMembershipRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainConceptValueSetMembershipResponseType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetConceptMetadataValueRequestType;
import de.fhdo.terminologie.ws.authoring.types.MaintainValueSetConceptMetadataValueResponseType;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveTerminologyOrConceptResponseType;
import de.fhdo.terminologie.ws.authoring.types.RemoveValueSetContentRequestType;
import de.fhdo.terminologie.ws.authoring.types.RemoveValueSetContentResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateCodeSystemVersionStatusResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptStatusResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptValueSetMembershipStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateConceptValueSetMembershipStatusResponseType;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusResponseType;
import de.fhdo.terminologie.ws.authorization.Authorization;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@WebService(serviceName = "Authoring")
@SchemaValidation
public class Authoring
{
  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.

  @Resource
  private WebServiceContext webServiceContext;

  @WebMethod(operationName = "CreateCodeSystem")
  public CreateCodeSystemResponseType CreateCodeSystem(@WebParam(name = "parameter") CreateCodeSystemRequestType parameter)
  {

    CreateCodeSystem ccs = new CreateCodeSystem();
    return ccs.CreateCodeSystem(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateValueSet")
  public CreateValueSetResponseType CreateValueSet(@WebParam(name = "parameter") CreateValueSetRequestType parameter)
  {

    return new CreateValueSet().CreateValueSet(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainValueSet")
  public MaintainValueSetResponseType MaintainValueSet(@WebParam(name = "parameter") MaintainValueSetRequestType parameter)
  {

    return new MaintainValueSet().MaintainValueSet(parameter, SecurityHelper.getIp(webServiceContext));
  }

  @WebMethod(operationName = "CreateConcept")
  public CreateConceptResponseType CreateConcept(@WebParam(name = "parameter") CreateConceptRequestType parameter)
  {
    AuthenticateInfos authenticateInfos = authorize(parameter != null ? parameter.getLoginToken() : "");
    return new CreateConcept().CreateConcept(parameter, authenticateInfos);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateConceptAssociationType")
  public CreateConceptAssociationTypeResponseType CreateConceptAssociationType(@WebParam(name = "parameter") CreateConceptAssociationTypeRequestType parameter)
  {
    AuthenticateInfos authenticateInfos = authorize(parameter != null ? parameter.getLoginToken() : "");
    return new CreateConceptAssociationType().CreateConceptAssociationType(parameter, authenticateInfos);
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateValueSetContent")
  public CreateValueSetContentResponseType CreateValueSetContent(@WebParam(name = "parameter") CreateValueSetContentRequestType parameter)
  {

    return new CreateValueSetContent().CreateValueSetContent(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateCodeSystemVersionStatus")
  public UpdateCodeSystemVersionStatusResponseType UpdateCodeSystemVersionStatus(@WebParam(name = "parameter") UpdateCodeSystemVersionStatusRequestType parameter)
  {

    return new UpdateCodeSystemVersionStatus().UpdateCodeSystemVersionStatus(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateConceptStatus")
  public UpdateConceptStatusResponseType UpdateConceptStatus(@WebParam(name = "parameter") UpdateConceptStatusRequestType parameter)
  {

    return new UpdateConceptStatus().UpdateConceptStatus(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateValueSetStatus")
  public UpdateValueSetStatusResponseType UpdateValueSetStatus(@WebParam(name = "parameter") UpdateValueSetStatusRequestType parameter)
  {

    return new UpdateValueSetStatus().updateValueSetStatus(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainCodeSystemVersion")
  public MaintainCodeSystemVersionResponseType MaintainCodeSystemVersion(@WebParam(name = "parameter") MaintainCodeSystemVersionRequestType parameter)
  {

    return new MaintainCodeSystemVersion().MaintainCodeSystemVersion(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConceptAssociationType")
  public MaintainConceptAssociationTypeResponseType MaintainConceptAssociationType(@WebParam(name = "parameter") MaintainConceptAssociationTypeRequestType parameter)
  {

    return new MaintainConceptAssociationType().MaintainConceptAssociationType(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConcept")
  public MaintainConceptResponseType MaintainConcept(@WebParam(name = "parameter") MaintainConceptRequestType parameter)
  {

    return new MaintainConcept().MaintainConcept(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "RemoveValueSetContent")
  public RemoveValueSetContentResponseType RemoveValueSetContent(@WebParam(name = "parameter") RemoveValueSetContentRequestType parameter)
  {

    return new RemoveValueSetContent().RemoveValueSetContent(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateValueSetConceptMetadataValue")
  public CreateValueSetConceptMetadataValueResponseType CreateValueSetConceptMetadataValue(@WebParam(name = "parameter") CreateValueSetConceptMetadataValueRequestType parameter)
  {

    return new CreateValueSetConceptMetadataValue().CreateValueSetConceptMetadataValue(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "DeleteValueSetConceptMetadataValue")
  public DeleteValueSetConceptMetadataValueResponseType DeleteValueSetConceptMetadataValue(@WebParam(name = "parameter") DeleteValueSetConceptMetadataValueRequestType parameter)
  {

    return new DeleteValueSetConceptMetadataValue().DeleteValueSetConceptMetadataValue(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainValueSetConceptMetadataValue")
  public MaintainValueSetConceptMetadataValueResponseType MaintainValueSetConceptMetadataValue(@WebParam(name = "parameter") MaintainValueSetConceptMetadataValueRequestType parameter)
  {

    return new MaintainValueSetConceptMetadataValue().MaintainValueSetConceptMetadataValue(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainCodeSystemConceptMetadataValue")
  public MaintainCodeSystemConceptMetadataValueResponseType MaintainCodeSystemConceptMetadataValue(@WebParam(name = "parameter") MaintainCodeSystemConceptMetadataValueRequestType parameter)
  {

    return new MaintainCodeSystemConceptMetadataValue().MaintainCodeSystemConceptMetadataValue(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateConceptValueSetMembershipStatus")
  public UpdateConceptValueSetMembershipStatusResponseType UpdateConceptValueSetMembershipStatus(@WebParam(name = "parameter") UpdateConceptValueSetMembershipStatusRequestType parameter)
  {

    return new UpdateConceptValueSetMembershipStatus().UpdateConceptValueSetMembershipStatus(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConceptValueSetMembership")
  public MaintainConceptValueSetMembershipResponseType MaintainConceptValueSetMembership(@WebParam(name = "parameter") MaintainConceptValueSetMembershipRequestType parameter)
  {

    return new MaintainConceptValueSetMembership().MaintainConceptValueSetMembership(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "RemoveTerminologyOrConcept")
  public RemoveTerminologyOrConceptResponseType RemoveTerminologyOrConcept(@WebParam(name = "parameter") RemoveTerminologyOrConceptRequestType parameter)
  {

    return new RemoveTerminologyOrConcept().RemoveTerminologyOrConcept(parameter, SecurityHelper.getIp(webServiceContext));
  }

  private AuthenticateInfos authorize(String loginToken)
  {
    String ipAddress = SecurityHelper.getIp(webServiceContext);

    AuthenticateInfos loginInfoType = null;
    if (loginToken != null)
    {
      return Authorization.authenticate(ipAddress, loginToken);
    }
    return null;
  }
}
