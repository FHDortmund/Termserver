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
package de.fhdo.terminologie.ws.administration.svs;

import com.sun.tools.ws.wsdl.document.soap.SOAPFault;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.search.ListValueSetContents;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetails;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.types.ListValueSetContentsResponseType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnValueSetDetailsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.LinkedList;
import javax.xml.soap.SOAPException;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class RetrieveValueSet
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ValueSet ValueSetRepository_RetrieveValueSet(RetrieveValueSetRequest request) throws SVSFaultException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== ValueSetRepository_RetrieveValueSet gestartet ======");
    }

    // Parameter prüfen
    if (validateParameter(request) == false)
    {
      throw new SVSFaultException("Wrong parameter.");
    }

    // Return-Informationen anlegen
    ValueSet response = new ValueSet();

    //RetrieveValueSetResponse response = new RetrieveValueSetResponse();
    logger.debug("ValueSetRepository_RetrieveValueSet");

    ValueSetVersion vsv = null;
    String id = request.getValueSet().getId();
    logger.debug("ID: " + id);

    // read value set details
    try
    {
      ReturnValueSetDetailsRequestType requestValueSetDetails = new ReturnValueSetDetailsRequestType();
      requestValueSetDetails.setValueSet(new de.fhdo.terminologie.db.hibernate.ValueSet());
      ValueSetVersion vsvRequest = new ValueSetVersion();
      vsvRequest.setOid(id);
      requestValueSetDetails.getValueSet().getValueSetVersions().add(vsvRequest);
    //requestValueSetDetails.setLoginToken(parameter.getLoginToken());

      //ValueSetDetails abrufen
      ReturnValueSetDetails rcsd = new ReturnValueSetDetails();
      ReturnValueSetDetailsResponseType responseValueSetDetails = rcsd.ReturnValueSetDetails(requestValueSetDetails, "");

      logger.debug("Response: " + responseValueSetDetails.getReturnInfos().getMessage());

      if (responseValueSetDetails.getReturnInfos().getStatus() == ReturnType.Status.OK)
      {
        if (responseValueSetDetails.getValueSet() != null)
        {
          for (ValueSetVersion vsv_ws : responseValueSetDetails.getValueSet().getValueSetVersions())
          {
            if (vsv_ws.getOid() != null && vsv_ws.getOid().equalsIgnoreCase(id))
            {
              vsv = vsv_ws;
              vsv.setValueSet(responseValueSetDetails.getValueSet());
              break;
            }
          }
        }
      }

      if (vsv == null)
      {
        long versionId = Long.parseLong(id);
        // search again with id
        requestValueSetDetails = new ReturnValueSetDetailsRequestType();
        requestValueSetDetails.setValueSet(new de.fhdo.terminologie.db.hibernate.ValueSet());
        vsvRequest = new ValueSetVersion();
        vsvRequest.setVersionId(versionId);
        requestValueSetDetails.getValueSet().getValueSetVersions().add(vsvRequest);

        //ValueSetDetails abrufen
        responseValueSetDetails = rcsd.ReturnValueSetDetails(requestValueSetDetails, "");

        logger.debug("Response: " + responseValueSetDetails.getReturnInfos().getMessage());

        if (responseValueSetDetails.getReturnInfos().getStatus() == ReturnType.Status.OK)
        {
          if (responseValueSetDetails.getValueSet() != null)
          {
            for (ValueSetVersion vsv_ws : responseValueSetDetails.getValueSet().getValueSetVersions())
            {
              if (vsv_ws.getVersionId() != null && vsv_ws.getVersionId().longValue() == versionId)
              {
                vsv = vsv_ws;
                vsv.setValueSet(responseValueSetDetails.getValueSet());
                break;
              }
            }
          }
        }
      }
    }
    catch (Exception ex)
    {

    }

    if (vsv == null)
    {
      throw new SVSFaultException("Value Set with given ID not found.");
    }
    else
    {
      logger.debug("found vsv-id: " + vsv.getVersionId());

      ValueSet vs = new ValueSet();
      if (vsv.getOid() == null || vsv.getOid().length() == 0)
        vs.setId("" + vsv.getVersionId());
      else
        vs.setId(vsv.getOid());

      vs.setVersion(vsv.getName());
      vs.setDisplayName(vsv.getValueSet().getName() + " - " + vsv.getName());

      // read values
      ListValueSetContentsRequestType requestListCodeSystemConcepts = new ListValueSetContentsRequestType();
      requestListCodeSystemConcepts.setValueSet(new de.fhdo.terminologie.db.hibernate.ValueSet());
      requestListCodeSystemConcepts.getValueSet().getValueSetVersions().add(vsv);

      ListValueSetContents lcsc = new ListValueSetContents();
      ListValueSetContentsResponseType responseConcepts = lcsc.ListValueSetContents(requestListCodeSystemConcepts, "");
      
      logger.debug(responseConcepts.getReturnInfos().getMessage());
      
      if(responseConcepts.getReturnInfos().getStatus() == ReturnType.Status.OK &&
              responseConcepts.getReturnInfos().getCount() > 0)
      {
        ConceptList cl = new ConceptList();
        cl.setConcept(new LinkedList<Concept>());
        
        cl.setLang(vsv.getPreferredLanguageCd());
        
        // fill list
        for(CodeSystemEntity cse : responseConcepts.getCodeSystemEntity())
        {
          Concept c = new Concept();
          CodeSystemEntityVersion csv = cse.getCodeSystemEntityVersions().iterator().next();
          CodeSystemConcept csc = csv.getCodeSystemConcepts().iterator().next();
          c.setCode(csc.getCode());
          c.setDisplayName(csc.getTerm());
          
          if(cse.getCodeSystemVersionEntityMemberships() != null && cse.getCodeSystemVersionEntityMemberships().size() > 0)
          {
            CodeSystemVersionEntityMembership csvem = cse.getCodeSystemVersionEntityMemberships().iterator().next();
            if(csvem.getCodeSystemVersion() != null)
            {
              c.setCodeSystemVersion(csvem.getCodeSystemVersion().getName());
              c.setCodeSystem(csvem.getCodeSystemVersion().getOid());
              
              if(csvem.getCodeSystemVersion().getCodeSystem() != null)
                c.setCodeSystemName(csvem.getCodeSystemVersion().getCodeSystem().getName());
            }
          }
          
          cl.getConcept().add(c);
        }
        
        vs.setConceptList(cl);
      }
      else
      {
        throw new SVSFaultException(responseConcepts.getReturnInfos().getMessage());
      }

      return vs;
    }

    //return response;
  }

  private boolean validateParameter(RetrieveValueSetRequest parameter)
  {
    String s = "";
    if (parameter == null)
      return false;
    if (parameter.getValueSet() == null)
      return false;

    /*if(parameter.getCodeSystem() == null)
     {
     s = "Es muss ein Codesystem mitgegeben werden.";
     }
     else
     {
     if(parameter.getCodeSystem().getId() == null || parameter.getCodeSystem().getId().longValue() == 0)
     {
     s = "Es muss eine ID für ein Codesystem mitgegeben werden.";
     }
     }

     if(s.length() > 0)
     {
     response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
     response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
     response.getReturnInfos().setMessage(s);
     return false;
     }*/
    return true;
  }

}
