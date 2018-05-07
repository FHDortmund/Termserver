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
package de.fhdo.collaboration;

import de.fhdo.helper.SessionHelper;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Login Filter. Leitet auf die Loginseite um wenn der Benutzer nicht
 * eingelogged ist.
 *
 * @author Robert Mützner
 * @see http://forums.sun.com/thread.jspa?threadID=5377392
 * @see Markus Stäuble, Hans Jürgen Schumacher -ZK Developers Guide 2008 Packt
 * Publishing Ltd. S 100ff
 */
public class CollborationSecurityFilter implements Filter
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static String lastreq = "";

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
  {
    logger.debug("doFilter Collaboration()");

    HttpSession session = ((HttpServletRequest) request).getSession(false);
    
    //if (SessionHelper.isCollaborationActive(session))
    {
      logger.debug("Kollaboration aktiv");
      
      if (session == null)
        logger.debug("Filter-Session ist null");

      /**
       * Wenn benutzer nicht eingelogged -> Loginpage Andernfalls -> Anfrage
       * beantworten
       */
      lastreq = (((HttpServletRequest) request).getRequestURI().toString());

      if (isLoggedIn(request, response))
      {
        logger.debug("login OK (doFilter)");
        chain.doFilter(request, response);
      }
      else
      {
        logger.debug("login nicht OK (doFilter)");
        request.getRequestDispatcher("/collaboration/login.zul").forward(request, response);
      }
    }
    /*else
    {
      logger.debug("Collaboration nicht aktiv");
      chain.doFilter(request, response);
    }*/

  }

  private boolean isLoggedIn(ServletRequest request, ServletResponse response)
  {
    //logger.debug("isLoggedIn()");

    HttpSession lsession = ((HttpServletRequest) request).getSession(false);
    if (lsession == null)
    {
      logger.debug("SecurityFilter.java:isLoggedIn() - keine Session");
      return false;
    }

    return SessionHelper.isCollaborationLoggedIn(lsession);
  }

  /*
   *  Werden vorerst nicht benötigt
   */
  public void init(FilterConfig filterConfig) throws ServletException
  {
  }

  public void destroy()
  {
  }

  /**
   * @return the lastreq
   */
  public static String getLastreq()
  {
    return lastreq;
  }
}
