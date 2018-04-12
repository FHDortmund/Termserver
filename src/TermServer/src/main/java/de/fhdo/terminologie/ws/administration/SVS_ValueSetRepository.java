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

package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.ws.administration.svs.RetrieveValueSet;
import de.fhdo.terminologie.ws.administration.svs.RetrieveValueSetRequest;
import de.fhdo.terminologie.ws.administration.svs.SVSFaultException;
import de.fhdo.terminologie.ws.administration.svs.ValueSet;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@WebService(serviceName = "SVS_ValueSetRepository", targetNamespace = "urn:ihe:iti:svs:2008")
//@HandlerChain(file = "SVS_ValueSetRepository_handler.xml")
@HandlerChain(file = "/SVS_ValueSetRepository_handler.xml")
public class SVS_ValueSetRepository
{
  @WebMethod(operationName = "RetrieveValueSet")
  @WebResult(name = "ValueSet", targetNamespace = "urn:ihe:iti:svs:2008")
  public ValueSet RetrieveValueSet(
          @WebParam(name="RetrieveValueSetRequest", targetNamespace = "urn:ihe:iti:svs:2008")
                  RetrieveValueSetRequest request)
           throws SVSFaultException
  {
    return new RetrieveValueSet().ValueSetRepository_RetrieveValueSet(request);
  }
  
}
