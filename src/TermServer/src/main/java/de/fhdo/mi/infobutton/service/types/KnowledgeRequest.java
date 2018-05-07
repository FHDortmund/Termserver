package de.fhdo.mi.infobutton.service.types;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class KnowledgeRequest
{
  MultivaluedMap<String, String> parameters;
          
  public enum RESPONSE_FORMAT { TEXT_XML, APPLICATION_JSON , APPLICATION_JAVASCRIPT }
  public final RESPONSE_FORMAT DefaultResponseFormat = RESPONSE_FORMAT.TEXT_XML;

  public KnowledgeRequest(MultivaluedMap<String, String> parameters)
  {
    this.parameters = parameters;
  }
  
  
  
  public RESPONSE_FORMAT getResponseFormat()
  {
    if(parameters.containsKey("knowledgeResponseType"))
    {
      String value = parameters.getFirst("knowledgeResponseType");
      if(value != null)
      {
        if(value.equalsIgnoreCase("text/xml") || value.equalsIgnoreCase("xml"))
          return RESPONSE_FORMAT.TEXT_XML;
        else if(value.equalsIgnoreCase("application/json") || value.equalsIgnoreCase("json"))
          return RESPONSE_FORMAT.APPLICATION_JSON;
        else if(value.equalsIgnoreCase("application/javascript"))
          return RESPONSE_FORMAT.APPLICATION_JAVASCRIPT;
      }
    }
    return DefaultResponseFormat;
  }
  
  
  
  //knowledgeResponseType
}
