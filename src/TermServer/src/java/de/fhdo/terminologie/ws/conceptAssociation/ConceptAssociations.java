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
package de.fhdo.terminologie.ws.conceptAssociation;

import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ListConceptAssociationsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.MaintainConceptAssociationResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ReturnConceptAssociationDetailsRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.ReturnConceptAssociationDetailsResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.TraverseConceptToRootResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.UpdateConceptAssociationStatusResponseType;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@WebService(serviceName = "ConceptAssociations")
public class ConceptAssociations
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.
  @Resource
  private WebServiceContext webServiceContext;
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateConceptAssociation")
  public CreateConceptAssociationResponseType CreateConceptAssociation(@WebParam(name = "parameter") CreateConceptAssociationRequestType parameter)
  {
    return new CreateConceptAssociation().CreateConceptAssociation(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ListConceptAssociations")
  public ListConceptAssociationsResponseType ListConceptAssociations(@WebParam(name = "parameter") ListConceptAssociationsRequestType parameter)
  {
    logger.debug("WS-AUFRUF ListConceptAssociations");
    return new ListConceptAssociations().ListConceptAssociations(parameter, SecurityHelper.getIp(webServiceContext));
  }
  
  /**
  * Web service operation
  */
  @WebMethod(operationName = "ReturnConceptAssociationDetails")
  public ReturnConceptAssociationDetailsResponseType ReturnConceptAssociationDetails(@WebParam(name = "parameter") ReturnConceptAssociationDetailsRequestType parameter)
  {
    return new ReturnConceptAssociationDetails().ReturnConceptAssociationDetails(parameter, SecurityHelper.getIp(webServiceContext));
  }

 
  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainConceptAssociation")
  public MaintainConceptAssociationResponseType MaintainConceptAssociation(@WebParam(name = "parameter") MaintainConceptAssociationRequestType parameter)
  {
    return new MaintainConceptAssociation().MaintainConceptAssociation(parameter, SecurityHelper.getIp(webServiceContext));
  }
  
  /**
   * Web service operation
   */
  @WebMethod(operationName = "TraverseConceptToRoot")
  public TraverseConceptToRootResponseType TraverseConceptToRoot(@WebParam(name = "parameter") TraverseConceptToRootRequestType parameter)
  {
    return new TraverseConceptToRoot().TraverseConceptToRoot(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "UpdateConceptAssociationStatus")
  public UpdateConceptAssociationStatusResponseType UpdateConceptAssociationStatus(@WebParam(name = "parameter") UpdateConceptAssociationStatusRequestType parameter)
  {
    return new UpdateConceptAssociationStatus().UpdateConceptAssociationStatus(parameter, SecurityHelper.getIp(webServiceContext));
  }
}
