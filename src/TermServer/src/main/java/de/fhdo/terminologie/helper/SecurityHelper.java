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
package de.fhdo.terminologie.helper;


import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class SecurityHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
//  public static void applyIPAdress(LoginType login, WebServiceContext webServiceContext)
//  {
//    if (login != null)
//    {
//      login.setIp(getIp(webServiceContext));
//      logger.debug("IP: " + login.getIp());
//    }
//  }
  
  /**
   * Diese Methode gibt die Client IP Adresse zurück.
   * Beim Aufruf eines WebServices wird diese über den WebServiceContext mitgegeben.
   *
   * @param webServiceContext
   * @return
   */
  public static String getIp(WebServiceContext wsc)
  {
    try
    {
      //if (logger.isDebugEnabled())
      //  logger.debug("Get IP Adress");

      MessageContext msgCtxt = wsc.getMessageContext();
      HttpServletRequest ht_request = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
      String clientIP = ht_request.getRemoteAddr();
      /*if (logger.isInfoEnabled())
      {
        logger.info("Zugriffszeitpunkt: " + new java.util.Date() + " | Client IP: " + clientIP);
      }*/
      return clientIP;
    }
    catch (Exception e)
    {
      /*if (logger.isDebugEnabled())
      {
        logger.debug("[Security] Fehler beim ermitteln der Clien IP: " + e.getMessage());
      }*/
      return "";
    }

  }
  
}
