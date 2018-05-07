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
public enum PO_CLASSNAME
{
  CODESYSTEM_CONCEPT("CodeSystemConcept", "Konzept"),
  CODESYSTEM("CodeSystem", "Code System"),//Vorher: Vokabular
  VALUESET("ValueSet", "Value Set"),
  VALUESET_VERSION("ValueSetVersion","Value Set Version"),
  RELATION("Relation", "Beziehung"),
  CODESYSTEM_VERSION("CodeSystemVersion", "Code System Version"), //Vorher: Vokabular-Version
  ASSOCIATION("CodeSystemEntityVersionAssociation", "Assoziation"),
  CONCEPT_VALUESET_MEMBERSHIP("ConceptValueSetMembership","Konzept Value Set Zugehörigkeit")
  ;
  
  private final String code;
  private final String bezeichnung;

  private PO_CLASSNAME(String Code, String Bezeichnung)
  {
    this.code = Code;
    this.bezeichnung = Bezeichnung;
  }

  public String code()
  {
    return code;
  }

  public String bezeichnung()
  {
    return bezeichnung;
  }

  public static PO_CLASSNAME get(String Code)
  {
    PO_CLASSNAME[] values = PO_CLASSNAME.values();
    for (int i = 0; i < values.length; ++i)
    {
      if (values[i].code().equals(Code))
        return values[i];
    }
    return PO_CLASSNAME.CODESYSTEM_CONCEPT;
  }
  
  public static String getString(String Code)
  {
    PO_CLASSNAME ct = get(Code);
    if(ct == null)
      return "";
    else return ct.bezeichnung;
  }
}
