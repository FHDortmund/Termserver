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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Robert Mützner
 */
public class DateTimeHelper
{
  public static String ConvertXMLGregorianCalenderToString(XMLGregorianCalendar Calendar)
  {
    return ConvertXMLGregorianCalenderToString(Calendar, "dd.MM.yyyy");
  }

  public static String ConvertXMLGregorianCalenderToString(
          XMLGregorianCalendar Calendar, String Format)
  {
    if (Calendar == null)
      return "";

    SimpleDateFormat sdf = new SimpleDateFormat(Format);
    GregorianCalendar gregorianCalendar = Calendar.toGregorianCalendar();
    return sdf.format(gregorianCalendar.getTime());
  }

  public static long GetDateDiffInDays(XMLGregorianCalendar Calendar)
  {
    if (Calendar == null)
      return -1;

    /** The date at the end of the last century */
    Date d1 = Calendar.toGregorianCalendar().getTime();

    /** Today's date */
    Date today = new Date();

    if(today.after(d1))
      return -1;

    // Get msec from each, and subtract.
    long diff = d1.getTime() - today.getTime();

    return (diff / (1000 * 60 * 60 * 24)) + 1;
    //System.out.println("The 21st century (up to " + today + ") is "
    //    + (diff / (1000 * 60 * 60 * 24)) + " days old.");
  }

  public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date)
  {
    GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
    gc.setTime(date);
    DatatypeFactory dataTypeFactory = null;
    try
    {
      dataTypeFactory = DatatypeFactory.newInstance();
    }
    catch (DatatypeConfigurationException ex)
    {
      ex.printStackTrace();
      //Logger.getLogger(InstallmentBean.class.getName()).log(Level.SEVERE, null, ex);
    }
    XMLGregorianCalendar value = dataTypeFactory.newXMLGregorianCalendar(gc);
    return value;
  }
  
  public static Date ConvertXMLGregorianCalenderToDate(XMLGregorianCalendar Calendar)
  {
    if (Calendar == null)
      return null;
    
    Calendar cal = Calendar.toGregorianCalendar();
    return cal.getTime();
  }
}
