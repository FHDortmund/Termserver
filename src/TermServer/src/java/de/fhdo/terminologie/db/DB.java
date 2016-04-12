package de.fhdo.terminologie.db;

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.SysParam;
import de.fhdo.terminologie.helper.SysParameter;
import org.hibernate.HibernateException;

/**
 * Datenbank-Initialisierung und Updates
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class DB
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private static DB instance;

  public static DB getInstance()
  {
    if (instance == null)
    {
      instance = new DB();
    }

    return instance;
  }

  private boolean initialized = false;

  public DB()
  {

  }

  // =================================================================
  // Die DB-Versionsnummer muss bei jeder Änderung hochgesetzt werden!
  // =================================================================
  private static final int CURRENT_DB_VERSION = 5;
  // =================================================================

  /**
   * Prüft die Datenbank auf Updates. Wird nach jedem Neustart des Servers vor
   * der ersten Datenbankverbindung geprüft.
   *
   */
  public void checkForUpdates()
  {
    if (initialized)
    {
      return;
    }

    initialized = true;

    logger.debug("DB - checkForUpdates()");

    try
    {
      boolean updated = false;

      // check version
      int currentVersion = readCurrentDBVersion();

      logger.debug("Database DB-Version: " + currentVersion);
      logger.debug("Application DB-Version: " + CURRENT_DB_VERSION);

      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      org.hibernate.Transaction tx = hb_session.beginTransaction();

      try
      {
        // Achtung: Die DB-Versionsnummer (CURRENT_DB_VERSION) muss bei jeder Änderung hochgesetzt werden!

        // Prüfen, ob die Datenbank-Version kleiner als die aktuelle ist
        if (currentVersion < 2)
        {
          hb_session.createSQLQuery("ALTER TABLE metadata_parameter ADD COLUMN description TEXT NULL DEFAULT NULL;").executeUpdate();
          hb_session.createSQLQuery("ALTER TABLE metadata_parameter ADD COLUMN paramNameDisplay TEXT NULL DEFAULT NULL;").executeUpdate();
          hb_session.createSQLQuery("ALTER TABLE metadata_parameter ADD COLUMN maxLength INT NULL DEFAULT NULL;").executeUpdate();

          updated = true;
        }
        
        if (currentVersion < 3)
        {
          hb_session.createSQLQuery("ALTER TABLE value_set_version ADD COLUMN virtualCodeSystemVersionId BIGINT NULL DEFAULT NULL;").executeUpdate();
          
          updated = true;
        }
        
        if (currentVersion < 4)
        {
          hb_session.createSQLQuery("ALTER TABLE licenced_user CHANGE COLUMN validFrom validFrom TIMESTAMP NULL DEFAULT NULL;").executeUpdate();
          hb_session.createSQLQuery("ALTER TABLE licenced_user CHANGE COLUMN validTo validTo TIMESTAMP NULL DEFAULT NULL;").executeUpdate();
          //hb_session.createSQLQuery("ALTER TABLE licenced_user MODIFY validTo TIMESTAMP;").executeUpdate();
          
          //CHANGE COLUMN `validFrom` `validFrom` TIMESTAMP NULL DEFAULT NULL COMMENT '' ,
          //CHANGE COLUMN `validTo` `validTo` TIMESTAMP NULL DEFAULT NULL COMMENT '' 

          
          updated = true;
        }
        
        if(currentVersion < 5)
        {
          // allow all characters for metadata value
          hb_session.createSQLQuery("ALTER TABLE code_system_metadata_value MODIFY COLUMN parameterValue TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;").executeUpdate();
          hb_session.createSQLQuery("ALTER TABLE value_set_metadata_value MODIFY COLUMN parameterValue TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;").executeUpdate();
          hb_session.createSQLQuery("ALTER TABLE code_system_concept MODIFY COLUMN term TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;").executeUpdate();
          hb_session.createSQLQuery("ALTER TABLE code_system_concept_translation MODIFY COLUMN term TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;").executeUpdate();
          
          updated = true;
        }
        
        // Änderungen speichern
        tx.commit();
      }
      catch (HibernateException ex)
      {
        LoggingOutput.outputException(ex, this);
        initialized = false;
      }
      finally
      {
        hb_session.close();
      }

      if (updated)
      {
        // Neue Version in SysParam setzen
        logger.debug("save new db version in SysParam: " + CURRENT_DB_VERSION);
        //SystemSettings.getInstance().setDbVersion(CURRENT_DB_VERSION);
        saveCurrentDBVersion(CURRENT_DB_VERSION);

        logger.info("Database was successfully updated to version: " + CURRENT_DB_VERSION);

        //Clients.showNotification("Die Datenbank wurde erfolgreich auf die Version " + CURRENT_DB_VERSION + " aktualisiert!", Clients.NOTIFICATION_TYPE_INFO, null,
        //        "middle_center", 0, true);
      }

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      initialized = false;
    }
  }

  private int readCurrentDBVersion()
  {
    int version = 0;

    SysParam sp = SysParameter.instance().getValue("db_version", null, null);
    if (sp != null)
      version = Integer.parseInt(sp.getValue());

    return version;
  }
  
  private void saveCurrentDBVersion(int version)
  {
    SysParam sp = SysParameter.instance().getValue("db_version", null, null);

    if (sp == null)
    {
      sp = new SysParam("db_version");
      sp.setJavaDatatype("int");
      DomainValue dv = new DomainValue();
      dv.setDomainValueId(188l); // System
      sp.setDomainValueByModifyLevel(dv);
      sp.setDomainValueByValidityDomain(dv);
    }

    sp.setValue("" + version);

    SysParameter.instance().setValue(sp);
  }

}
