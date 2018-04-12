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
package de.fhdo.terminologie.ws.types;

import java.util.Date;

/**
 *
 * @author Nico Hänsch
 */
public class ExportParameterType {
    private boolean codeSystemInfos;
    private boolean translations;
    private String associationInfos;
    private Date dateFrom;

    /**
     * @return the codeSystemInfos
     */
    public boolean getCodeSystemInfos() {
        return codeSystemInfos;
    }

    /**
     * @param codeSystemInfos the codeSystemInfos to set
     */
    public void setCodeSystemInfos(boolean codeSystemInfos) {
        this.codeSystemInfos = codeSystemInfos;
    }

    /**
     * @return the translations
     */
    public boolean getTranslations() {
        return translations;
    }

    /**
     * @param translations the translations to set
     */
    public void setTranslations(boolean translations) {
        this.translations = translations;
    }

    /**
     * @return the associationInfos
     */
    public String getAssociationInfos() {
        return associationInfos;
    }

    /**
     * @param associationInfos the associationInfos to set
     */
    public void setAssociationInfos(String associationInfos) {
        this.associationInfos = associationInfos;
    }

  /**
   * @return the dateFrom
   */
  public Date getDateFrom()
  {
    return dateFrom;
  }

  /**
   * @param dateFrom the dateFrom to set
   */
  public void setDateFrom(Date dateFrom)
  {
    this.dateFrom = dateFrom;
  }
    
}
