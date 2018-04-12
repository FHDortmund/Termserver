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
public class RubricKinds
{
  public static enum RUBRICKINDS
  {
    preferred("preferred", "The attribute kind='preferred' defines a specific unique term that identifies the meaning of a class."), 
    inclusion("inclusion", "The attribute kind='inclusion' shall be used for additional terms that can be used within a class."), 
    exclusion("exclusion", "The attribute kind='exclusion' shall be used for terms that are excluded from a class."),
    coding_hint("coding_hint", "Coding instructions"), 
    definition("definition", "Otherwise unspecified texts added to rubrics. Should be used for a descriptive phrase for a given concept in a healthcare classification system."), 
    note("note", "General remark"), 
    text("text", "e.g. a text for a Modifier"), 
    title("title", "A title for a text rubric"), 
    introduction("introduction", "A long text at the beginning of a chapter."),
    footnote("footnote", "As in the printed versions of ICD."), 
    etiology("etiology", "The basic cause or underlying disease process is assigned a code marked with a dagger (†)."), 
    manifestation("manifestation", "The clinical manifestation is marked with an asterisk (*).");
    
    private String code, noteAttr;

    private RUBRICKINDS(String c, String s)
    {
      code = c;
      noteAttr = s;
    }

    public String getCode()
    {
      return code;
    }

    /*public static boolean isStatusCodeValid(Integer StatusCode)
    {
      RUBRICKINDS[] codes = RUBRICKINDS.values();

      for (int i = 0; i < codes.length; ++i)
      {
        if (codes[i].getCode().equals(StatusCode))
          return true;
      }
      return false;
    }*/

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

    /**
     * @return the noteAttr
     */
    public String getNoteAttr()
    {
      return noteAttr;
    }
  }

  
  
}
