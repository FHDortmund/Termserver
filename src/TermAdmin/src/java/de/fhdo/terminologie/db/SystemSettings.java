/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.db;

import de.fhdo.helper.SysParameter;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.SysParam;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Robert
 */
public class SystemSettings
{

  private static SystemSettings instance;

  public static SystemSettings getInstance()
  {
    if (instance == null)
    {
      instance = new SystemSettings();
    }

    return instance;
  }

  boolean loaded;

  private String systemLogsPath = "";
  int dbVersion;
  

  public enum MESSAGEBOX_TYPES
  {

    Messagebox, Notification
  }
  private MESSAGEBOX_TYPES messageboxType;

  public SystemSettings()
  {
    loaded = false;
    load();
  }

  public void reset()
  {
    loaded = false;
    load();
  }

  private void load()
  {
    if (loaded == false)
    {
      dbVersion = getIntValue("db_version", 0);
      
      systemLogsPath = SysParameter.instance().getStringValue("system_logs_path", null, null);

      loaded = true;
    }
  }

  

  private int getIntValue(String ParamName, int DefaultValue)
  {
    SysParam sp = SysParameter.instance().getValue(ParamName, null, null);
    if (sp != null)
    {
      return Integer.parseInt(sp.getValue());
    }
    else
    {
      return DefaultValue;
    }
  }

  private boolean getBoolValue(String ParamName, boolean DefaultValue)
  {
    SysParam sp = SysParameter.instance().getValue(ParamName, null, null);
    if (sp != null)
    {
      return Boolean.parseBoolean(sp.getValue());
    }
    else
    {
      return DefaultValue;
    }
  }


  /**
   * @param dbVersion the dbVersion to set
   */
  public void setDbVersion(int dbVersion)
  {
    this.dbVersion = dbVersion;

    SysParam sp = SysParameter.instance().getValue("db_version", null, null);

    if (sp == null)
    {
      sp = new SysParam("db_version");
      sp.setJavaDatatype("int");
      DomainValue dv = new DomainValue();
      dv.setDomainValueId(SysParameter.VALIDITY_DOMAIN_SYSTEM); // System
      sp.setDomainValueByModifyLevel(dv);
      sp.setDomainValueByValidityDomain(dv);
    }

    sp.setValue("" + dbVersion);

    SysParameter.instance().setValue(sp);
  }

  /**
   * @return the systemLogsPath
   */
  public String getSystemLogsPath()
  {
    return systemLogsPath;
  }

  /**
   * @param systemLogsPath the systemLogsPath to set
   */
  public void setSystemLogsPath(String systemLogsPath)
  {
    this.systemLogsPath = systemLogsPath;
    
    SysParam sp = SysParameter.instance().getValue("system_logs_path", null, null);

    if (sp == null)
    {
      sp = new SysParam("system_logs_path");
      sp.setJavaDatatype("String");
      DomainValue dv = new DomainValue();
      dv.setDomainValueId(SysParameter.VALIDITY_DOMAIN_SYSTEM); // System
      sp.setDomainValueByValidityDomain(dv);

      dv = new DomainValue();
      dv.setDomainValueId(SysParameter.VALIDITY_DOMAIN_SYSTEM);
      sp.setDomainValueByModifyLevel(dv);
    }

    sp.setValue(systemLogsPath);

    SysParameter.instance().setValue(sp);
  }

}
