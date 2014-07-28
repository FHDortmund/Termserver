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
package de.fhdo.terminologie;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class Definitions
{
  public static final String LANGUAGE_GERMAN_CD = "de";
  public static final String LANGUAGE_ENGLISH_CD = "en";
  
  public final static String APP_KEY = "TERMSERVER";

  public static enum STATUS_CODES
  {
    INACTIVE(0), ACTIVE(1), DELETED(2);
    private int code;

    private STATUS_CODES(int c)
    {
      code = c;
    }

    public int getCode()
    {
      return code;
    }

    public static boolean isStatusCodeValid(Integer StatusCode)
    {
      STATUS_CODES[] codes = STATUS_CODES.values();

      for (int i = 0; i < codes.length; ++i)
      {
        if (codes[i].getCode() == StatusCode)
          return true;
      }
      return false;
    }

    public static String readStatusCodes()
    {
      String s = "";
      STATUS_CODES[] codes = STATUS_CODES.values();

      for (int i = 0; i < codes.length; ++i)
      {
        s += "\n" + codes[i].name() + " (" + codes[i].getCode() + ")";
      }
      return s;
    }
  }

  public static enum ASSOCIATION_KIND
  {
    ONTOLOGY(1), TAXONOMY(2), CROSS_MAPPING(3), LINK(4);
    private int code;

    private ASSOCIATION_KIND(int c)
    {
      code = c;
    }

    public int getCode()
    {
      return code;
    }
  }

  public static boolean isAssociationKindValid(Integer kind)
  {
    ASSOCIATION_KIND[] kinds = ASSOCIATION_KIND.values();

    for (int i = 0; i < kinds.length; ++i)
    {
      if (kinds[i].getCode() == kind)
        return true;
    }
    return false;
  }

  public static String readAssociationKinds()
  {
    String s = "";
    ASSOCIATION_KIND[] kinds = ASSOCIATION_KIND.values();

    for (int i = 0; i < kinds.length; ++i)
    {
      s += "\n" + kinds[i].name() + " (" + kinds[i].getCode() + ")";
    }
    return s;
  }
}
