package de.fhdo.mi.infobutton.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.log4j.Logger;

/**
 * REST Web Service
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@Path("/infoRequest")
public class InfobuttonService
{

  private static Logger logger = Logger.getLogger(InfobuttonService.class);
  //private static Logger logger = Logger.getRootLogger();

  @Context
  private UriInfo context;


  /**
   * Retrieves representation of an instance of
   * de.fhdortmund.mi.infobutton.service.InfobuttonService
   *
   * http://localhost:8080/TermServer/rest/infoRequest?mainSearchCriteria.v.c=J45&mainSearchCriteria.v.cs=1.2.276.0.76.5.471
   *
   * @return an instance of java.lang.String
   */
  @GET
  //@Produces("text/plain")
  @Produces("text/plain;charset=utf-8")
  //@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
  public String getText()
  {
    logger.info("InfobuttonService - GET");
    MultivaluedMap<String, String> parameters = context.getQueryParameters();

//    try
//    {
//      for (String key : parameters.keySet())
//      {
//        String value = parameters.getFirst(key);
//        logger.info(key + ": " + value);
//        //logger.info(key + ": " + URLEncoder.encode(value, "UTF-8"));
//        logger.info(key + ": " + URLDecoder.decode(value, "UTF-8"));
//
//      }
//    }
//    catch (Exception e)
//    {
//      logger.error("Parameter: ", e);
//    }

    String result;
    // Eigentliche Nachricht bearbeiten (gleiche Logik für POST und GET)
    try
    {
      InfobuttonServiceRest rest = new InfobuttonServiceRest();
      result = rest.processRequest(parameters);
      logger.debug("Process finished");

    }
    catch (Exception ex)
    {
      logger.error("Error at InfobuttonServiceRest. ", ex);
      result = ex.getMessage();
    }
    return result;
  }

//  @POST
//  public String postRequest(String body) throws Exception
//  {
//    logger.info("InfobuttonService - POST");
//
//    MultivaluedMap<String, String> parameters = context.getQueryParameters();
//
//    // Body auslesen
//    try
//    {
//      String[] parts = body.split("\n");
//      for (String part : parts)
//      {
//        String[] cols = part.split("=");
//        if (cols.length == 2)
//        {
//          parameters.add(cols[0], cols[1]);
//        }
//      }
//    }
//    catch (Exception ex)
//    {
//      logger.debug("Body: " + body);
//      logger.error("Fehler beim parsen der Parameter.", ex);
//      return "Fehler beim parsen der Parameter: " + ex.getLocalizedMessage() + "\n\nBeispielhafter Aufruf:\n"
//              + "knowledgeRequestNotification.effectiveTime.v=20060706001023 \n"
//              + "patientPerson.administrativeGenderCode.c=M \n"
//              + "age.v.v=77 \n"
//              + "age.v.u=a \n"
//              + "ageGroup.v.c=D000368 \n"
//              + "taskContext.c.c=PROBLISTREV \n"
//              + "subTopic.v.c=Q000628 \n"
//              + "subTopic.v.cs=2.16.840.1.113883.6.177 \n"
//              + "subTopic.v.dn=therapy \n"
//              + "mainSearchCriteria.v.c=D018410 \n"
//              + "mainSearchCriteria.v.cs=2.16.840.1.113883.6.177 \n"
//              + "mainSearchCriteria.v.ot=Bacterial+Pneumonia \n"
//              + "mainSearchCriteria.v.ot=Pneumonia";
//    }
//
//    if (parameters.size() == 0)
//    {
//      return "Keine Parameter angegeben, beispielhafter Aufruf:\n"
//              + "knowledgeRequestNotification.effectiveTime.v=20060706001023 \n"
//              + "patientPerson.administrativeGenderCode.c=M \n"
//              + "age.v.v=77 \n"
//              + "age.v.u=a \n"
//              + "ageGroup.v.c=D000368 \n"
//              + "taskContext.c.c=PROBLISTREV \n"
//              + "subTopic.v.c=Q000628 \n"
//              + "subTopic.v.cs=2.16.840.1.113883.6.177 \n"
//              + "subTopic.v.dn=therapy \n"
//              + "mainSearchCriteria.v.c=D018410 \n"
//              + "mainSearchCriteria.v.cs=2.16.840.1.113883.6.177 \n"
//              + "mainSearchCriteria.v.ot=Bacterial+Pneumonia \n"
//              + "mainSearchCriteria.v.ot=Pneumonia";
//    }
//
//    for (String key : parameters.keySet())
//    {
//      logger.debug(key + ": " + parameters.getFirst(key));
//    }
//
//    // Eigentliche Nachricht bearbeiten (gleiche Logik für POST und GET)
//    return processRequest(parameters);
//  }
//  /**
//   * PUT method for updating or creating an instance of InfobuttonService
//   * @param content representation for the resource
//   * @return an HTTP response with content of the updated or created resource.
//   */
//  @PUT
//  @Consumes("text/plain")
//  public void putText(String content)
//  {
//  }
}
