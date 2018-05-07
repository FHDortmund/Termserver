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
import types.termserver.fhdo.de.ValueSet;

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
    String name1 = "";
    String name2 = "";
    
    if(o1 instanceof CodeSystem)
      name1 = ((CodeSystem)o1).getName();
    else if(o1 instanceof ValueSet)
      name1 = ((ValueSet)o1).getName();
    
    if(o2 instanceof CodeSystem)
      name2 = ((CodeSystem)o2).getName();
    else if(o2 instanceof ValueSet)
      name2 = ((ValueSet)o2).getName();
    
    //CodeSystem cs1 = (CodeSystem) o1,
    //        cs2 = (CodeSystem) o2;

    int v = name1.compareToIgnoreCase(name2);

    return asc ? v : -v;
  }
}

