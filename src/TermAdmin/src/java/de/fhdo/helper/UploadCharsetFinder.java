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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Robert Mützner
 */
public class UploadCharsetFinder implements org.zkoss.zk.ui.util.CharsetFinder
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public String getCharset(String string, InputStream in) throws IOException
  {
    //logger.debug("UploadCharsetFinder, getCharset for String: " + string);
    
    if(in != null)
    {
      byte[] bytes = new byte[100];
      in.read(bytes, 0, 99);
      String s = new String(bytes);
      
      //logger.debug("String: " + s);
      
      if(s.contains("encoding=\"ISO-8859-1\""))
      {
        logger.debug("ISO-8859-1 gefunden");
        return "ISO-8859-1";
      }
      
      //<?xml version="1.0" encoding="ISO-8859-1"?>
    }
    
    return "UTF-8";
  }
  
}
