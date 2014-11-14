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
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.DomainValue;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ComparatorCodesystems implements Comparator
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean asc = false;

  public ComparatorCodesystems(boolean ascending)
  {
    asc = ascending;
  }

  public int compare(Object o1, Object o2)
  {
    CodeSystem cs1 = (CodeSystem) o1,
            cs2 = (CodeSystem) o2;

    int v = cs1.getName().compareToIgnoreCase(cs2.getName());

    return asc ? v : -v;
  }
}

