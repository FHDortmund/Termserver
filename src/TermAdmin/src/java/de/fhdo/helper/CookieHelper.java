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
package de.fhdo.helper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class CookieHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void setCookie(String name, String value)
  {
    logger.debug("Speicher Cookie: " + name + ", mit Wert: " + value);

    ((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(new Cookie(
      name, value));
  }

  public static String getCookie(String name)
  {
    Cookie[] cookies = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getCookies();

    logger.debug("Suche Cookie mit Name: " + name);

    if (cookies != null)
    {
      //logger.debug("Cookies gefunden, Anzahl: " + cookies.length);

      for (Cookie cookie : cookies)
      {
        //logger.debug("Cookie: " + cookie.getName());

        if (cookie.getName().equals(name))
        {
          logger.debug("Cookie '" + name + "' gefunden, Wert: " + cookie.getValue());
          
          return cookie.getValue();
        }
      }
    }
    else 
    {
      
    }

    logger.debug("Kein Cookie fuer '" + name + "' gefunden!");
    return null;
  }

  public static void removeCookie(String name)
  {
    logger.debug("Loesche Cookie: " + name);

    Cookie c = new Cookie(name, "");
    c.setMaxAge(0);

    ((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(c);
  }
}
