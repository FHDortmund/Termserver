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

import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner
 */
public class ParameterHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  /*public static Object getObject(String ParameterName)
   {
   try
   {
   Object o = Executions.getCurrent().getAttribute(ParameterName);
   //Executions.getCurrent().getAttribute(ParameterName)
   return o;
   }
   catch (Exception e)
   {
   e.printStackTrace();
   }

   return null;
   }*/
  public static Boolean getBoolean(String ParameterName)
  {
    Boolean b = null;
    String s = Executions.getCurrent().getParameter(ParameterName);
    try
    {
      if (s.contains("true") || s.contains("1") || Integer.valueOf(s) > 0)
        b = Boolean.TRUE;
    }
    catch (Exception e)
    {
    }

    try
    {
      if (s.contains("false") || s.contains("0") || Integer.valueOf(s) < 0)
        b = Boolean.FALSE;
    }
    catch (Exception e)
    {
    }

    return b;
  }

  public static Boolean getBoolean(String ParameterName, boolean defaultValue)
  {
    
    String s = Executions.getCurrent().getParameter(ParameterName);
    try
    {
      if (s.contains("true") || s.contains("1") || Integer.valueOf(s) > 0)
        return true;
    }
    catch (Exception e)
    {
    }

    try
    {
      if (s.contains("false") || s.contains("0") || Integer.valueOf(s) < 0)
        return false;
    }
    catch (Exception e)
    {
    }

    return defaultValue;
  }

  public static String getString(String ParameterName)
  {
    String wert = "";
    try
    {
      Object o = Executions.getCurrent().getParameter(ParameterName);
      if (o != null)
        wert = o.toString();
    }
    catch (Exception e)
    {
    }

    return wert;
  }

  public static Integer getInteger(String ParameterName)
  {
    Integer wert = 0;
    try
    {
      Object o = Executions.getCurrent().getParameter(ParameterName);
      if (o != null)
        wert = Integer.parseInt(o.toString());
    }
    catch (Exception e)
    {
    }

    return wert;
  }

  public static long getLong(String ParameterName)
  {
    long wert = 0;
    try
    {
      Object o = Executions.getCurrent().getParameter(ParameterName);
      if (o != null)
        wert = Long.parseLong(o.toString());
    }
    catch (Exception e)
    {
    }

    return wert;
  }
}
