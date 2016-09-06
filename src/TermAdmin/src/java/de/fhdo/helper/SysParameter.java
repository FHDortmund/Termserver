package de.fhdo.helper;

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.SysParam;
import java.util.Set;

/**
 * Diese Klasse ist eine Hilfsklasse zum Auslesen und Speichern von Parametern
 * in der Datenbank
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class SysParameter
{
  // Singleton-Muster

  private static SysParameter instance = null;

  public static SysParameter instance()
  {
    if (instance == null)
    {
      instance = new SysParameter();
    }
    return instance;
  }
  // Konstanten
  public static final long VALIDITY_DOMAIN_ID = 2;
  public static final long VALIDITY_DOMAIN_SYSTEM = 188;
  public static final long VALIDITY_DOMAIN_MODULE = 189;
  public static final long VALIDITY_DOMAIN_SERVICE = 190;
  public static final long VALIDITY_DOMAIN_USERGROUP = 191;
  public static final long VALIDITY_DOMAIN_USER = 192;

  // Inhalt
  public SysParameter()
  {
    
  }

  /**
   * Listet alle verfügbaren Validity-Domains auf.
   *
   * Eine Validity-Domain gibt eine Domäne an, für die ein Parameter gültig ist.
   * Beispiele für Validity-Domains sind: 1. System 2. Modul 3. Service 4.
   * Benutzergruppe 5. Benutzer
   *
   * @return List<DomainValueType> - Liste mit Validity-Domains
   */
  public Set<DomainValue> getValidityDomains()
  {
    Set<DomainValue> list = null;

    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession(); 
    try
    {
      org.hibernate.Query q = hb_session.createQuery("from Domain WHERE domainId=:domain_id");
      q.setParameter("domain_id", VALIDITY_DOMAIN_ID);

      java.util.List<Domain> domainList = (java.util.List<Domain>) q.list();

      if (domainList.size() == 1)
      {
        list = domainList.get(0).getDomainValues();
      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close(); 
    }

    return list;
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
    if (param != null && param.getValue() != null)
    {
      return Boolean.valueOf(param.getValue());
    }

    return null;
  }

  /**
   * Liest ein Parameter aus der Datenbank. Der Name des Parameters muss
   * angegeben werden. Validity-Domain und ObjectID sind optional. Diese werden
   * angegeben, wenn man z.B. einen Parameter für einen bestimmten Benutzer
   * lesen möchte. In diesem Fall gibt man bei Validity-Domain die ID für User
   * an und bei ObjectID die UserID.
   *
   * @param Name Name des Parameters
   * @param ValidityDomain Validity-Domain (optional)
   * @param ObjectID Objekt-ID, z.B. User-ID (otional)
   * @return Parameter
   */
  public SysParam getValue(String Name, Long ValidityDomain, Long ObjectID)
  {
    SysParam setting = null;

    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      org.hibernate.Query q;

      if (ValidityDomain != null && ObjectID == null)
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd");
        q.setParameter("name", Name);
        q.setParameter("vd", ValidityDomain);
      }
      else if (ValidityDomain != null && ObjectID != null)
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name AND validityDomain=:vd AND objectId=:objectid");
        q.setParameter("name", Name);
        q.setParameter("vd", ValidityDomain);
        q.setParameter("objectid", ObjectID);
      }
      else
      {
        q = hb_session.createQuery("from SysParam WHERE name=:name ORDER BY validityDomain");
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
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close(); 
    }

    return setting;
  }

  /**
   * Speichert einen Parameter in der Datenbank.
   *
   *
   * @param Parameter der Parameter
   * @return String mit Fehlermeldung oder leer bei Erfolg
   */
  public String setValue(SysParam Parameter)
  {
    String ret = "";

    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      org.hibernate.Transaction tx = hb_session.beginTransaction();
      hb_session.saveOrUpdate(Parameter);
      tx.commit();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close();
    }

    return ret;
  }
  
  public String setValue(String paramName, String value)
  {
    String ret = "";
    
    SysParam param = getValue(paramName, null, null);

    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession(); 

    try
    {
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      if(param == null)
      {
        param = new SysParam(paramName);
        param.setValue(value);
        param.setJavaDatatype("string");
        DomainValue dv = new DomainValue();
        dv.setDomainValueId(VALIDITY_DOMAIN_SYSTEM); // System
        param.setDomainValueByValidityDomain(dv);

        dv = new DomainValue();
        dv.setDomainValueId(VALIDITY_DOMAIN_USER); // Benutzer
        param.setDomainValueByModifyLevel(dv);
      }
      else
      {
        param.setValue(value);
        hb_session.merge(param);
      }
      
      tx.commit();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close(); 
    }

    return ret;
  }

  /**
   * Löscht einen Parameter.
   *
   * @param Parameter
   * @return String mit Fehlermeldung oder leer bei Erfolg
   */
  public String deleteValue(SysParam Parameter)
  {
    String ret = "";

    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession(); 
    
    try
    {
      org.hibernate.Transaction tx =
              hb_session.beginTransaction();
      hb_session.delete(Parameter);
      tx.commit();
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
    finally
    {
      hb_session.close(); 
    }

    return ret;
  }
}
