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
import de.fhdo.terminologie.db.hibernate.Domain;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.search.ListDomainValues;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesRequestType;
import de.fhdo.terminologie.ws.search.types.ListDomainValuesResponseType;
import de.fhdo.terminologie.ws.types.ImportType;
import de.fhdo.terminologie.ws.types.ReturnType.Status;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportValueSetRequestType
{

  public static final long IMPORT_CSV_ID = 1;
  public static final long IMPORT_SVS_ID = 2;

  public static String getPossibleFormats()
  {
    //String s = "Mögliche Import-Formate sind:\n300: CSV\n301: SVS";
    String s = "Mögliche Import-Formate sind:";
    
    ListDomainValuesRequestType request = new ListDomainValuesRequestType();
    request.setDomain(new Domain());
    request.getDomain().setDomainId(DomainIDs.IMPORT_TYPES_VS);
    ListDomainValuesResponseType response = new ListDomainValues().ListDomainValues(request, "");
    
    if(response.getReturnInfos().getStatus() == Status.OK)
    {
      for(DomainValue dv : response.getDomainValues())
      {
        s += "\n" + dv.getDomainCode() + ": " + dv.getDomainDisplay();
      }
    }
    
    return s;
  }

  private ImportType importInfos;
  private String loginToken;
  private ValueSet valueSet;

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
   * @return the valueSet
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the valueSet to set
   */
  public void setValueSet(ValueSet valueSet)
  {
    this.valueSet = valueSet;
  }

}
