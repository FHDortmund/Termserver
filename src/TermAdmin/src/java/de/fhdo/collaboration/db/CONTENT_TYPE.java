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
package de.fhdo.collaboration.db;

/**
 *
 * @author Robert Mützner
 */
public enum CONTENT_TYPE
{

  NONE(0, ""),
  BEGRIFF(1, "Begriff"),
  VOKABULAR(2, "Code System"), //Vokabular
  BEZIEHUNG(3, "Beziehung"),
  VALUESET(4, "Value Set");
  private final int id;
  private final String bezeichnung;

  private CONTENT_TYPE(int ID, String Bezeichnung)
  {
    this.id = ID;
    this.bezeichnung = Bezeichnung;
  }

  public int id()
  {
    return id;
  }

  public String bezeichnung()
  {
    return bezeichnung;
  }

  public static CONTENT_TYPE get(int ID)
  {
    CONTENT_TYPE[] values = CONTENT_TYPE.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].id() == ID)
        return values[i];
    }
    return CONTENT_TYPE.NONE;
  }
}
