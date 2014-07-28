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
public enum PO_CHANGE_TYPE
{
  NONE(0, ""),
  NEW(1, "Neu"),
  CHANGED(2, "Geändert"),
  DELETED(3, "Gelöscht");
  
  private final int id;
  private final String bezeichnung;

  private PO_CHANGE_TYPE(int ID, String Bezeichnung)
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

  public static PO_CHANGE_TYPE get(int ID)
  {
    PO_CHANGE_TYPE[] values = PO_CHANGE_TYPE.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].id() == ID)
        return values[i];
    }
    return PO_CHANGE_TYPE.NONE;
  }
  
  public static String getString(int ID)
  {
    PO_CHANGE_TYPE ct = get(ID);
    if(ct == null)
      return "";
    else return ct.bezeichnung;
  }
}
