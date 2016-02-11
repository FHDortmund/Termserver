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

import java.util.Locale;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class LocalizationHelper
{

  public static void switchLocalization(Locale language)
  {
    Session session = Sessions.getCurrent();

    Locale preferredLocale = org.zkoss.util.Locales.getLocale(language);
    session.setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, preferredLocale);
    Executions.sendRedirect(null);
  }
  
  public static void switchLocalization(String languageCd)
  {
    Session session = Sessions.getCurrent();

    Locale preferredLocale = org.zkoss.util.Locales.getLocale(languageCd);
    session.setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, preferredLocale);
    Executions.sendRedirect(null);
    
    CookieHelper.setCookie("languageCd", languageCd);
  }
  
  public static void initLocalization()
  {
    Object o = CookieHelper.getCookie("languageCd");
    if(o != null)
    {
      String languageCd = (String) o;
      if(languageCd.length() > 0)
      {
        Session session = Sessions.getCurrent();
        Locale preferredLocale = org.zkoss.util.Locales.getLocale(languageCd);
        session.setAttribute(org.zkoss.web.Attributes.PREFERRED_LOCALE, preferredLocale);
      }
    }
  }
}
