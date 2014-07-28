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
package de.fhdo.collaboration.db;

import de.fhdo.collaboration.db.classes.Domain;
import de.fhdo.collaboration.db.classes.DomainValue;
import de.fhdo.collaboration.db.classes.SysParam;
import de.fhdo.helper.DES;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;

/**
 Diese Klasse ist eine Hilfsklasse zum Auslesen und Speichern von
 Parametern in der Datenbank

 @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class DBSysParam
{
  // Singleton-Muster

  private static DBSysParam instance = null;

  public static DBSysParam instance()
  {
    if (instance == null)
    {
      instance = new DBSysParam();
    }
    return instance;
  }
  // Konstanten
  public static final long VALIDITY_DOMAIN_ID = 60;
  public static final long VALIDITY_DOMAIN_SYSTEM = 1313;
  public static final long VALIDITY_DOMAIN_MODULE = 1314;
  public static final long VALIDITY_DOMAIN_SERVICE = 1315;
  public static final long VALIDITY_DOMAIN_USERGROUP = 1316;
  public static final long VALIDITY_DOMAIN_USER = 1317;

  public DBSysParam()
  {
  }

  /**
   Listet alle verfügbaren Validity-Domains auf.

   Eine Validity-Domain gibt eine Domäne an, für die ein Parameter
   gültig ist. Beispiele für Validity-Domains sind:
   1. System
   2. Modul
   3. Service
   4. Benutzergruppe
   5. Benutzer

   @return List<DomainValue> - Liste mit Validity-Domains
   */
  public List<DomainValue> getValidityDomains()
  {
    List<DomainValue> list = null;

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      org.hibernate.Query q = hb_session.createQuery("from Domain WHERE domainId=:domain_id");
      q.setParameter("domain_id", VALIDITY_DOMAIN_ID);

      java.util.List<Domain> domainList = (java.util.List<Domain>) q.list();

      if (domainList.size() == 1)
      {
        list = new LinkedList<DomainValue>(domainList.get(0).getDomainValues());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    //hb_session.getTransaction().commit();
    hb_session.close();

    return list;
  }

  /**
   Liest ein Parameter aus der Datenbank.
   Der Name des Parameters muss angegeben werden.
   Validity-Domain und ObjectID sind optional. Diese werden angegeben,
   wenn man z.B. einen Parameter für einen bestimmten Benutzer lesen
   möchte. In diesem Fall gibt man bei Validity-Domain die ID für User an
   und bei ObjectID die UserID.

   @param Name Name des Parameters
   @param ValidityDomain Validity-Domain (optional)
   @param ObjectID Objekt-ID, z.B. User-ID (otional)
   @return Parameter
   */
  public SysParam getValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam setting = null;

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    try
    {
      org.hibernate.Query q;

      if (ValidityDomain != null && ObjectID == null)
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name AND validity_domain=:vd");
        q.setParameter("name", Name);
        q.setParameter("vd", ValidityDomain);
      }
      else if (ValidityDomain != null && ObjectID != null)
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name AND validity_domain=:vd AND object_id=:objectid");
        q.setParameter("name", Name);
        q.setParameter("vd", ValidityDomain);
        q.setParameter("objectid", ObjectID);
      }
      else
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name ORDER BY validity_domain");
        q.setParameter("name", Name);
      }
      q.setMaxResults(1);

      java.util.List<SysParam> paramList = (java.util.List<SysParam>) q.list();

      if (paramList.size() > 0)
      {
        // Genau 1 Ergebnis gefunden
        setting = paramList.get(0);
      }

      if (setting == null && ObjectID != null && ObjectID > 0)
      {
        // Kein Ergebnis gefunden, aber User-ID angegeben
        // Evtl. wurde dieser Parameter jedoch nicht überschrieben
        // also den Standard-Parameter benutzen

        //hb_session.getTransaction().commit();
        hb_session.close();

        // TODO eigentlich müsste man 1 Ebene höher prüfen
        // aber die ID ist ja nicht bekannt
        // Bsp: Wenn User-Parameter nicht gefunden, dann müsste
        //      in Usergroup gesucht werden
        //      die Usergroup-ID ist jedoch nicht bekannt
        return getValue(Name, null, null);
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    //hb_session.getTransaction().commit();
    hb_session.close();

    resolveDatatype(setting);

    return setting;
  }

  public String getStringValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam param = getValue(Name, ValidityDomain, ObjectID);
    if (param != null && param.getValue() != null)
      return param.getValue();

    return "";
  }

  public Boolean getBoolValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam param = getValue(Name, ValidityDomain, ObjectID);
    try
    {
      if (param != null && param.getValue() != null)
        return Boolean.parseBoolean(param.getValue());
    }
    catch (Exception e)
    {
      return null;
    }

    return null;
  }

  private void resolveDatatype(SysParam setting)
  {
    if (setting != null && setting.getJavaDatatype() != null
      && setting.getJavaDatatype().equalsIgnoreCase("password"))
    {
      // Passwort entschlüsseln
      setting.setValue(DES.decrypt(setting.getValue()));
    }
  }

  private void applyDatatype(SysParam setting)
  {
    if (setting != null && setting.getJavaDatatype() != null
      && setting.getJavaDatatype().equalsIgnoreCase("password"))
    {
      // Passwort entschlüsseln
      setting.setValue(DES.encrypt(setting.getValue()));
    }
  }

  /* public String setValue(String Name, Long ValidityDomain, Long ObjectID)
   {
   SysParam param = new SysParam();
   param.setName(Name);
   param.setDomainValueByValidityDomain(new DomainValue());
   param.getDomainValueByValidityDomain().setDomainValueId(ValidityDomain);
   param.setObjectId(ObjectID);
   } */
  /**
   Speichert einen Parameter in der Datenbank.


   @param Parameter der Parameter
   @return String mit Fehlermeldung oder leer bei Erfolg
   */
  public String setValue(SysParam Parameter)
  {
    String ret = "";

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      applyDatatype(Parameter);

      hb_session.merge(Parameter);
    }
    catch (Exception ex)
    {
      ret = "Fehler bei 'setValue(): " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    hb_session.getTransaction().commit();
    hb_session.close();

    return ret;
  }

  /**
   Löscht einen Parameter.

   @param Parameter
   @return String mit Fehlermeldung oder leer bei Erfolg
   */
  public String deleteValue(SysParam Parameter)
  {
    String ret = "";

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      hb_session.delete(Parameter);
    }
    catch (Exception ex)
    {
      ret = "Fehler bei 'setValue(): " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    hb_session.getTransaction().commit();
    hb_session.close();

    return ret;
  }
}
