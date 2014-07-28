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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.LicencedUser;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LicenceHelper
{
  // Singleton
  private static LicenceHelper instance;

  public static LicenceHelper getInstance()
  {
    if (instance == null)
      instance = new LicenceHelper();
    return instance;
  }
  // Klasse
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Map<String, LicencedUser> licenceMap;

  public LicenceHelper()
  {
    licenceMap = new HashMap<String, LicencedUser>();
  }

  public boolean userHasLicence(long userId, long codeSystemVersionId)
  {
    boolean valid = false;
    
    // Versuchen Lizenz zu laden
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      valid = userHasLicence(userId, codeSystemVersionId, hb_session);
      hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      logger.error("Fehler bei 'userHasLicence' [LicenceHelper.java], Hibernate: " + e.getLocalizedMessage());
    }
    finally
    {
      hb_session.close();
    }
    
    return valid;
  }

  public boolean userHasLicence(long userId, long codeSystemVersionId, Session hb_session)
  {
    if(isCodeSystemVersionUnderLicence(codeSystemVersionId, hb_session) == false)
    {
      return true;
    }
    
    if (codeSystemVersionId <= 0 || userId == 0)
    {
      logger.debug("codeSystemVersionId <= 0 oder login == null || login.getTermUser() == null");
      return false;
    }
    
    String key = getMapKey(codeSystemVersionId, userId);

    if (licenceMap.containsKey(key))
    {
      // Lizenz erneut prüfen
      LicencedUser lu = licenceMap.get(key);
      boolean valid = isLicenceValid(lu);

      if (valid == false)
      {
        // Lizenz entfernen und neue DB-Abfrage
        licenceMap.remove(key);

        return userHasLicence(userId, codeSystemVersionId);
      }
      else
      {
        return true;  // Lizenz gültig
      }
    }
    else
    {
      boolean valid = false;


      String hql = "from LicencedUser lu";

      HQLParameterHelper parameterHelper = new HQLParameterHelper();

      parameterHelper.addParameter("", "userId", userId);
      parameterHelper.addParameter("", "codeSystemVersionId", codeSystemVersionId);

      // Parameter hinzufügen (immer mit AND verbunden)
      hql += parameterHelper.getWhere("");

      logger.debug("HQL: " + hql);

      // Query erstellen
      org.hibernate.Query q = hb_session.createQuery(hql);

      // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
      parameterHelper.applyParameter(q);

      List<LicencedUser> liste = q.list();

      if (liste != null && liste.size() > 0)
      {
        LicencedUser lu = liste.get(0);

        if (isLicenceValid(lu))
        {
          licenceMap.put(key, lu);
          valid = true;
        }
      }

      return valid;
    }
  }
  
  private boolean isCodeSystemVersionUnderLicence(long codeSystemVersionId, Session hb_session)
  {
    CodeSystemVersion csv = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, codeSystemVersionId);
    if(csv != null)
    {
      return csv.getUnderLicence();
    }
    
    return false;
  }

  private boolean isLicenceValid(LicencedUser licence)
  {
    if (licence == null)
      return false;

    Date now = new Date();

    if (licence.getValidFrom() != null
      && licence.getValidFrom().after(now))
    {
      return false;  // Lizenz noch nicht gültig
    }

    if (licence.getValidTo() != null
      && licence.getValidTo().before(now))
    {
      return false;  // Lizenz nicht mehr gültig
    }

    return true; // Lizenz ist noch gültig
  }

  private String getMapKey(long codeSystemVersionId, long userId)
  {
    String s = codeSystemVersionId + "_" + userId;
    return s;
  }
}
