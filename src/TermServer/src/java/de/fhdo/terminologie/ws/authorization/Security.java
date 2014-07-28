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
package de.fhdo.terminologie.ws.authorization;

import de.fhdo.terminologie.db.hibernate.Session;
import de.fhdo.terminologie.db.hibernate.TermUser;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import de.fhdo.terminologie.helper.SysParameter;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Security
{

  public static final String COLLAB_SOFTWARE_NAME = "collaboration_software";

  public static Session getSession(org.hibernate.Session hb_session, String sessionId)
  {

    if (sessionId != null && sessionId.length() != 0)
    {
      String hql = "from Session ";

      HQLParameterHelper parameterHelper = new HQLParameterHelper();
      parameterHelper.addParameter("", "sessionId", sessionId);

      // Parameter hinzufügen (immer mit AND verbunden)
      hql += parameterHelper.getWhere("");

      // Query erstellen
      org.hibernate.Query q = hb_session.createQuery(hql);

      // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
      parameterHelper.applyParameter(q);

      // Datenbank-Aufruf durchführen
      java.util.List<Session> list = (java.util.List<Session>) q.list();

      if (list != null && list.size() > 0)
      {
        return list.get(0);
      }
      return null;
    }
    else
      return null;
  }

  public static List<Session> checkForExistingSessions(org.hibernate.Session hb_session, TermUser user)
  {
    String hql = "from Session ";

    HQLParameterHelper parameterHelper = new HQLParameterHelper();
    parameterHelper.addParameter("", "termUserId", user.getId());

    // Parameter hinzufügen (immer mit AND verbunden)
    hql += parameterHelper.getWhere("");

    // Query erstellen
    org.hibernate.Query q = hb_session.createQuery(hql);

    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
    parameterHelper.applyParameter(q);

    // Datenbank-Aufruf durchführen
    List<Session> list = (java.util.List<Session>) q.list();

    if (list != null && list.size() > 0)
    {
      return list;
    }
    return null;
  }
  
  public static List<Session> checkForExistingSessions(org.hibernate.Session hb_session, String sessionId)
  {
    String hql = "from Session s join fetch s.termUser";

    HQLParameterHelper parameterHelper = new HQLParameterHelper();
    parameterHelper.addParameter("", "sessionId", sessionId);

    // Parameter hinzufügen (immer mit AND verbunden)
    hql += parameterHelper.getWhere("");

    // Query erstellen
    org.hibernate.Query q = hb_session.createQuery(hql);

    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
    parameterHelper.applyParameter(q);

    // Datenbank-Aufruf durchführen
    List<Session> list = (java.util.List<Session>) q.list();

    if (list != null && list.size() > 0)
    {
      return list;
    }
    return null;
  }
  /*public static List<Session> checkForExistingSessions(org.hibernate.Session hb_session, LoginType login, TermUser user)
  {

    String hql = "from Session ";

    HQLParameterHelper parameterHelper = new HQLParameterHelper();
    parameterHelper.addParameter("", "termUserId", user.getId());

    // Parameter hinzufügen (immer mit AND verbunden)
    hql += parameterHelper.getWhere("");

    // Query erstellen
    org.hibernate.Query q = hb_session.createQuery(hql);

    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
    parameterHelper.applyParameter(q);

    // Datenbank-Aufruf durchführen
    List<Session> list = (java.util.List<Session>) q.list();

    if (list != null && list.size() > 0)
    {
      return list;
    }
    return null;
  }*/

  /*public static List<Session> checkForExistingKollabSessions(org.hibernate.Session hb_session, LoginType login, TermUser user)
  {

    String hql = "from Session ";

    HQLParameterHelper parameterHelper = new HQLParameterHelper();
    parameterHelper.addParameter("", "termUserId", user.getId());
    String[] str = login.getUsername().split(":");
    parameterHelper.addParameter("", "collabUsername", str[1]);

    // Parameter hinzufügen (immer mit AND verbunden)
    hql += parameterHelper.getWhere("");

    // Query erstellen
    org.hibernate.Query q = hb_session.createQuery(hql);

    // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
    parameterHelper.applyParameter(q);

    // Datenbank-Aufruf durchführen
    List<Session> list = (java.util.List<Session>) q.list();

    if (list != null && list.size() > 0)
    {
      return list;
    }
    return null;
  }*/

  public static void checkForDeadSessions(org.hibernate.Session hb_session)
  {
    //GetSessions for collab_software
    String hqlS = "from Session";
    HQLParameterHelper parameterHelperS = new HQLParameterHelper();

    hqlS += parameterHelperS.getWhere("");
    org.hibernate.Query qS = hb_session.createQuery(hqlS);
    parameterHelperS.applyParameter(qS);
    List<Session> listS = (java.util.List<Session>) qS.list();

    String sessionTimeStr = SysParameter.instance().getStringValue("killDeadSessionAfter", null, null);
    Long sessionTime = 0l;
    try
    {
      sessionTime = Long.valueOf(sessionTimeStr);
    }
    catch (Exception ex)
    {
      sessionTime = 43200000l;
    }

    //Check for each Sessions which are older than "killDeadSessionAfter"-Time
    for (Session s : listS)
    {

      Date dateOfOrigin = s.getLastTimestamp();
      Date now = new Date();
      Long difference = now.getTime() - dateOfOrigin.getTime();

      if (difference >= sessionTime)
      {
        s.setTermUser(null);
        hb_session.delete(s);
      }
    }
  }
}
