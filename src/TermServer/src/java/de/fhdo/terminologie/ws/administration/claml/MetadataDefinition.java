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
package de.fhdo.terminologie.ws.administration.claml;

/**
 *
 * @author Robert Mützner
 */
public class MetadataDefinition
{
  public static enum METADATA_ATTRIBUTES
  {
    termAbbrevation("TS_ATTRIBUTE_TERMABBREVATION"), 
    meaning("TS_ATTRIBUTE_MEANING"), 
    hints("TS_ATTRIBUTE_HINTS"), 
    minorRevision("TS_ATTRIBUTE_MINORREVISION"), 
    majorRevision("TS_ATTRIBUTE_MAJORREVISION"), 
    status("TS_ATTRIBUTE_STATUS"), 
    statusDate("TS_ATTRIBUTE_STATUSDATE"), 
    isLeaf("TS_ATTRIBUTE_ISLEAF")
    ;
    
    private String claml_name;

    private METADATA_ATTRIBUTES(String c)
    {
      claml_name = c;
    }

    public String getCode()
    {
      return claml_name;
    }
    
    public static boolean isCodeValid(String Code)
    {
      METADATA_ATTRIBUTES[] codes = METADATA_ATTRIBUTES.values();

      for (int i = 0; i < codes.length; ++i)
      {
        if (codes[i].getCode().equals(Code))
          return true;
      }
      return false;
    }

    /*public static String readStatusCodes()
    {
      String s = "";
      RUBRICKINDS[] codes = RUBRICKINDS.values();

      for (int i = 0; i < codes.length; ++i)
      {
        s += "\n" + codes[i].name() + " (" + codes[i].getCode() + ")";
      }
      return s;
    }*/


  
  }
}
