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

import com.sun.xml.ws.developer.SchemaValidation;
import de.fhdo.terminologie.helper.SecurityHelper;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.administration.types.CreateDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.CreateDomainResponseType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportValueSetContentResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemCancelRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemCancelResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemStatusResponseType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportValueSetResponseType;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainRequestType;
import de.fhdo.terminologie.ws.administration.types.MaintainDomainResponseType;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author Bernhard Rimatzki
 */
@WebService(serviceName = "Administration")
@SchemaValidation
public class Administration
{

  // Mit Hilfe des WebServiceContext lässt sich die ClientIP bekommen.

  @Resource
  private WebServiceContext webServiceContext;

  /**
   * Web service operation
   */
  @WebMethod(operationName = "CreateDomain")
  public CreateDomainResponseType CreateDomain(@WebParam(name = "parameter") CreateDomainRequestType parameter)
  {
    CreateDomain cd = new CreateDomain();
    return cd.CreateDomain(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "MaintainDomain")
  public MaintainDomainResponseType MaintainDomain(@WebParam(name = "parameter") MaintainDomainRequestType parameter)
  {
    MaintainDomain md = new MaintainDomain();
    return md.MaintainDomain(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportCodeSystem")
  public ImportCodeSystemResponseType ImportCodeSystem(@WebParam(name = "parameter") ImportCodeSystemRequestType parameter)
  {
    ImportCodeSystem ics = new ImportCodeSystem();
    return ics.ImportCodeSystem(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportCodeSystemStatus")
  public ImportCodeSystemStatusResponseType ImportCodeSystemStatus(@WebParam(name = "parameter") ImportCodeSystemStatusRequestType parameter)
  {
    ImportCodeSystemStatus ics = new ImportCodeSystemStatus();
    return ics.ImportCodeSystemStatus(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ExportCodeSystemContent")
  public ExportCodeSystemContentResponseType ExportCodeSystemContent(@WebParam(name = "parameter") ExportCodeSystemContentRequestType parameter)
  {
    ExportCodeSystemContent ecsc = new ExportCodeSystemContent();
    return ecsc.ExportCodeSystemContent(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ExportValueSetContent")
  public ExportValueSetContentResponseType ExportValueSetContent(@WebParam(name = "parameter") ExportValueSetContentRequestType parameter)
  {
    ExportValueSetContent evsc = new ExportValueSetContent();
    return evsc.ExportValueSetContent(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  /*@WebMethod(operationName = "CheckImportStatus")
   public CheckImportStatusResponseType CheckImportStatus()
   {
   CheckImportStatusResponseType response = new CheckImportStatusResponseType();
   response.setReturnInfos(new ReturnType());

   if (ImportClaml.isRunning)
   {
   response.setPercentComplete(ImportClaml.percentageComplete);
   response.setCurrentTask(ImportClaml.currentTask);
      
   response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
   response.getReturnInfos().setStatus(ReturnType.Status.OK);
   response.getReturnInfos().setMessage("Status gelesen");
   }
   else
   {
   response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
   response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
   response.getReturnInfos().setMessage("Der Import läuft zur Zeit nicht.");
   }
   return response;
   }*/
  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportCodeSystemCancel")
  public ImportCodeSystemCancelResponseType ImportCodeSystemCancel(@WebParam(name = "parameter") ImportCodeSystemCancelRequestType parameter)
  {
    ImportCodeSystemCancel ics = new ImportCodeSystemCancel();
    return ics.ImportCodeSystemCancel(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ImportValueSet")
  public ImportValueSetResponseType ImportValueSet(@WebParam(name = "parameter") ImportValueSetRequestType parameter)
  {
    ImportValueSet ics = new ImportValueSet();
    return ics.ImportValueSet(parameter, SecurityHelper.getIp(webServiceContext));
  }

  /**
   * Web service operation
   */
  @WebMethod(operationName = "ActualProceedings")
  public ActualProceedingsResponseType ActualProceedings(@WebParam(name = "parameter") ActualProceedingsRequestType parameter)
  {
    ActualProceedings rap = new ActualProceedings();
    return rap.ActualProceedings(parameter, SecurityHelper.getIp(webServiceContext));
  }
}
