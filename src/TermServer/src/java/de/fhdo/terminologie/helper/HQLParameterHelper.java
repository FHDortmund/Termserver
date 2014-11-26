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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.Query;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class HQLParameterHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public class HQLObject
  {
    public Object obj;
    public String prefix;
    public String operator;
    public String fieldName;

    public HQLObject(Object obj, String prefix, String fieldName)
    {
      this.obj = obj;
      this.prefix = prefix;
      this.fieldName = fieldName;
      
      if(obj instanceof String)
        this.operator = " LIKE ";
      else if(obj instanceof java.util.Date)
        this.operator = " >= ";
      else this.operator = " = ";
    }
    public HQLObject(Object obj, String prefix, String fieldName, String operator)
    {
      this.obj = obj;
      this.prefix = prefix;
      this.operator = operator;
      this.fieldName = fieldName;
    }
  }
  
  private Map<String, HQLObject> parameterMap;
  
  public HQLParameterHelper()
  {
    parameterMap = new HashMap<String, HQLObject>();
  }
  
  /**
   * Fügt einen Parameter hinzu, welcher im Wehre-Teil der HQL-Abfrage erscheinen soll
   * 
   * @param Prefix Prefix der Tabelle aus der HQL-Anweisung
   * @param FieldName Datenbank-Feld-Bezeichnung
   * @param Value Der Wert, mit dem verglichen werden soll
   */
  public void addParameter(String Prefix, String FieldName, Object Value)
  {
    if(Value != null && Value.toString().length() > 0)
    {
      parameterMap.put(FieldName, new HQLObject(Value, Prefix, FieldName));
      logger.debug("addParameter, Fieldname: " + FieldName + ", Value: " + Value + ", Prefix: " + Prefix);
    }
  }
  
  /**
   * Fügt einen Parameter hinzu, welcher im Wehre-Teil der HQL-Abfrage erscheinen soll
   * @param Prefix Prefix der Tabelle aus der HQL-Anweisung
   * @param FieldName Datenbank-Feld-Bezeichnung
   * @param Value Der Wert, mit dem verglichen werden soll
   * @param Operator Vergleichsoperator
   */
  public void addParameter(String Prefix, String FieldName, Object Value, String Operator)
  {
    if(Value != null && Value.toString().length() > 0)
      parameterMap.put(Prefix + FieldName, new HQLObject(Value, Prefix, FieldName, Operator));
  }
  
  
  public String getWhere(String Where)
  {
    String s = "";
    if(Where != null)
      s = Where;
    
    Iterator<String>it = parameterMap.keySet().iterator();
    
    while(it.hasNext())
    {
      String key = it.next();
      HQLObject obj = parameterMap.get(key);
      
      s += (s.length() > 0 ? " AND ":"") + " " + obj.prefix + obj.fieldName;
      
      s += obj.operator; // Vergleichsoperator (z.B. '=')
      
      s += ":s_" + obj.fieldName;
    }
    
    if(s.length() > 0)
      return " WHERE " + s;
    else return "";
  }
  
  public void applyParameter(Query q)
  {
    Iterator<String>it = parameterMap.keySet().iterator();
    
    while(it.hasNext())
    {
      String key = it.next();
      HQLObject obj = parameterMap.get(key);
      
      String s = "";
      
      if(obj.obj instanceof String)
      {
        s = "%" + obj.obj.toString() + "%";
      }
      else if(obj.obj instanceof java.util.Date)
      {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        s = sdf.format(obj.obj);
        
        //logger.debug("SDF: " + sdf.format(obj.obj));
      }
      else if(obj.obj instanceof Boolean)
      {
        s = Boolean.parseBoolean(obj.obj.toString()) ? "1" : "0";
        
        //logger.debug("Bool: " + s);
      }
      else if(obj.obj instanceof Integer)
      {
        s = ((Integer)obj.obj).toString();
      }
      else s = obj.obj.toString();
      
      
      q.setString("s_" + obj.fieldName, s);
    }
  }
  
  public static String getSQLDateStr(java.util.Date Datum)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(Datum);
  }
  
  public void applySQLParameter(Query q)
  {
    Iterator<String>it = parameterMap.keySet().iterator();
    
    while(it.hasNext())
    {
      String key = it.next();
      HQLObject obj = parameterMap.get(key);
      
      String s = "";
      
      if(obj.obj instanceof String)
      {
        s = "%" + obj.obj.toString() + "%";
        q.setString("s_" + obj.fieldName, s);
      }
      else if(obj.obj instanceof java.util.Date)
      {
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //s = sdf.format(obj.obj);
        
        //q.setDate("s_" + obj.fieldName, (java.util.Date)obj.obj);
        q.setTimestamp("s_" + obj.fieldName, (java.util.Date)obj.obj);
        //logger.debug("SDF: " + sdf.format(obj.obj));
      }
      else if(obj.obj instanceof Boolean)
      {
        //s = Boolean.parseBoolean(obj.obj.toString()) ? "1" : "0";
        q.setBoolean("s_" + obj.fieldName, (Boolean) obj.obj);
        //logger.debug("Bool: " + s);
      }
      else if(obj.obj instanceof Integer)
      {
        //s = ((Integer)obj.obj).toString();
        q.setInteger("s_" + obj.fieldName, (Integer) obj.obj);
      }
      else if(obj.obj instanceof Long)
      {
        //s = ((Integer)obj.obj).toString();
        q.setLong("s_" + obj.fieldName, (Long) obj.obj);
      }
      else 
      {
        s = obj.obj.toString();
        q.setString("s_" + obj.fieldName, s);
        
        logger.warn("Typ nicht gefunden: " + obj.obj.getClass().getCanonicalName());
      }
      
      
      
    }
  }
  
}
