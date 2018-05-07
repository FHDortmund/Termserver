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

import java.util.Comparator;
import org.zkoss.zul.DefaultTreeNode;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ComparatorConceptCodeDescending implements Comparator
{

  public int compare(Object o1, Object o2)
  {
    if (o1 != null && o2 != null)
    {
      if (o1 instanceof DefaultTreeNode)
      {
        types.termserver.fhdo.de.CodeSystemEntityVersion csev1 = (types.termserver.fhdo.de.CodeSystemEntityVersion) ((DefaultTreeNode) o1).getData();
        types.termserver.fhdo.de.CodeSystemEntityVersion csev2 = (types.termserver.fhdo.de.CodeSystemEntityVersion) ((DefaultTreeNode) o2).getData();

        String term1 = csev1.getCodeSystemConcepts().get(0).getCode();
        String term2 = csev2.getCodeSystemConcepts().get(0).getCode();

        return term2.compareTo(term1);
      }

    }
    return 0;
  }

}