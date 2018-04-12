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
package de.fhdo.terminologie.ws.administration.types;

import de.fhdo.terminologie.DomainIDs;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.ws.search.ListDomainValues;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.types.ImportType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 * 28.03.2012, Mützner: LOINC-Format hinzugefügt 07.02.2013, Mützner: KBV
 * Keytabs hinzugefügt
 *
 * @author Bernhard Rimatzki
 */
public class ImportCodeSystemRequestType
{

  public static final long IMPORT_CLAML_ID = 2;
  public static final long IMPORT_CSV_ID = 1;
  public static final long IMPORT_LOINC_ID = 3;
  public static final long IMPORT_LOINC_RELATIONS_ID = 4;
  public static final long IMPORT_KBV_KEYTABS_ID = 5;
  public static final long IMPORT_SVS_ID = 8;
  public static final long IMPORT_LeiKat_ID = 7;
  public static final long IMPORT_KAL_ID = 9;
  public static final long IMPORT_ICD_BMG_ID = 6;
  public static final long IMPORT_MESH_XML_ID = 10;
  public static final long IMPORT_LOINC_254_ID = 11;
  public static final long IMPORT_LOINC_254_RELATIONS_ID = 12;
  public static final long IMPORT_OWL = 13;
  

  public static String getPossibleFormats()
  {
    String s = "Mögliche Import-Formate sind:";

    ListDomainValuesRequestType request = new ListDomainValuesRequestType();
    request.setDomain(new Domain());
    request.getDomain().setDomainId(DomainIDs.IMPORT_TYPES_CS);
    ListDomainValuesResponseType response = new ListDomainValues().ListDomainValues(request, "");

    if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      for (DomainValue dv : response.getDomainValues())
      {
        s += "\n" + dv.getDomainCode() + ": " + dv.getDomainDisplay();
      }
    }
    return s;
  }

  private ImportType importInfos;
  private String loginToken;
  private CodeSystem codeSystem;

  /**
   * @return the importInfos
   */
  public ImportType getImportInfos()
  {
    return importInfos;
  }

  /**
   * @param importInfos the importInfos to set
   */
  public void setImportInfos(ImportType importInfos)
  {
    this.importInfos = importInfos;
  }

  /**
   * @return the login
   */
  public String getLoginToken()
  {
    return loginToken;
  }

  /**
   * @param login the login to set
   */
  public void setLoginToken(String login)
  {
    this.loginToken = login;
  }

  /**
   * @return the codeSystem
   */
  public CodeSystem getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystem the codeSystem to set
   */
  public void setCodeSystem(CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }
}
