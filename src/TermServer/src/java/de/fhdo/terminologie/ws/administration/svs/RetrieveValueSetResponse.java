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

package de.fhdo.terminologie.ws.administration.svs;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@XmlRootElement
@XmlType(name = "RetrieveValueSetResponse", namespace = "urn:ihe:iti:svs:2008" )
public class RetrieveValueSetResponse
{
  private ValueSet ValueSet;

  /**
   * @return the ValueSet
   */
  public ValueSet getValueSet()
  {
    return ValueSet;
  }

  /**
   * @param ValueSet the ValueSet to set
   */
  public void setValueSet(ValueSet ValueSet)
  {
    this.ValueSet = ValueSet;
  }

  
}
