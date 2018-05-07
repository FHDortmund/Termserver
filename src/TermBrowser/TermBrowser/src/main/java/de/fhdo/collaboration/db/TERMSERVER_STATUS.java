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
public enum TERMSERVER_STATUS
{
  INACTIVE(0, "inaktiv"),
  PUBLIC(1, "öffentlich"),
  DELETED(2, "gelöscht");
  
  private final int id;
  private final String bezeichnung;

  private TERMSERVER_STATUS(int ID, String Bezeichnung)
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

  public static TERMSERVER_STATUS get(int ID)
  {
    TERMSERVER_STATUS[] values = TERMSERVER_STATUS.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].id() == ID)
        return values[i];
    }
    return TERMSERVER_STATUS.INACTIVE;
  }
  
  public static String getString(int ID)
  {
    TERMSERVER_STATUS ct = get(ID);
    if(ct == null)
      return "";
    else return ct.bezeichnung;
  }
}
