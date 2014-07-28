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
package de.fhdo.login;

import de.fhdo.helper.SessionHelper;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Login Filter. Leitet auf die Loginseite um wenn der Benutzer nicht eingelogged ist.
 *
 * @author mathias aschhoff
 * @see http://forums.sun.com/thread.jspa?threadID=5377392
 * @see Markus Stäuble, Hans Jürgen Schumacher -ZK Developers Guide 2008 Packt Publishing Ltd. S 100ff
 */
public class SecurityFilter implements Filter
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static String lastreq = "";

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
  {

    logger.debug("doFilter()");

    HttpSession session = ((HttpServletRequest) request).getSession(false);
    if(session == null)
      logger.debug("Filter-Session ist null");

    /**
     * Wenn benutzer nicht eingelogged -> Loginpage
     * Andernfalls -> Anfrage beantworten
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
      request.getRequestDispatcher("/index.zul").forward(request, response);
    }
      /*if (!Authentication.getInstance().isLoggedIn(request, response))
      {
        request.getRequestDispatcher("/index.zul").forward(request, response);
      }
      else
      {

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


    //logger.debug("UserID: " + SessionHelper.getUserID());

    return SessionHelper.isUserLoggedIn(lsession);
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
