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
package de.fhdo.models.comparators;

import de.fhdo.helper.LanguageHelper;
import java.util.Comparator;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;

/**
 *
 * @author Becker
 */
public class ComparatorTranslations implements Comparator
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean asc = false;

  public ComparatorTranslations(boolean ascending)
  {
    asc = ascending;
  }

  public int compare(Object o1, Object o2)
  {
    CodeSystemConceptTranslation csct1 = (CodeSystemConceptTranslation) o1,
            csct2 = (CodeSystemConceptTranslation) o2;

    //TODO int v = LanguageHelper.getLanguageTable().get(String.valueOf(csct1.getLanguageId())).compareTo(LanguageHelper.getLanguageTable().get(String.valueOf(csct2.getLanguageId())));

    //return asc ? v : -v;
    return 0;
  }
}
