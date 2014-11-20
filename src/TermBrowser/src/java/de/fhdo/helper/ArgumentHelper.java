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

import de.fhdo.logging.LoggingOutput;
import java.util.Map;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Robert Mützner
 */
public class ArgumentHelper
{
  
  public static Object getWindowArgument(String argName)
  {
    try
    {
      Map args = Executions.getCurrent().getArg();

      if (args.get(argName) != null)
        return args.get(argName);
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return null;
  }
  
  public static long getWindowArgumentLong(String argName)
  {
    long l = 0;
    try
    {
      Object o = getWindowArgument(argName);

      if (o != null)
        l = (Long) o;
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return l;
  }
  
  public static boolean getWindowArgumentBool(String argName)
  {
    boolean b = false;
    try
    {
      Object o = getWindowArgument(argName);

      if (o != null)
        b = (Boolean) o;
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return b;
  }
  
  public static int getWindowArgumentInt(String argName)
  {
    int l = 0;
    try
    {
      Object o = getWindowArgument(argName);

      if (o != null)
        l = (Integer) o;
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return l;
  }
  
  public static String getWindowArgumentString(String argName)
  {
    try
    {
      Object o = getWindowArgument(argName);

      if (o != null)
        return o.toString();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return "";
  }
  
  public static Object getWindowParameter(String argName)
  {
    try
    {
      return Executions.getCurrent().getParameter(argName);
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return null;
  }
  
  public static String getWindowParameterString(String argName)
  {
    try
    {
      Object o = Executions.getCurrent().getParameter(argName);
      if(o != null)
        return o.toString();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return "";
  }
  
  public static boolean getWindowParameterBool(String argName)
  {
    try
    {
      Object o = Executions.getCurrent().getParameter(argName);
      if(o != null)
        return Boolean.parseBoolean(o.toString());
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ArgumentHelper.class);
    }

    return false;
  }
  
}
