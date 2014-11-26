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

import javax.xml.ws.WebFault;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
@WebFault(faultBean = "de.fhdo.terminologie.ws.administration.svs.FaultBean")
public class SVSFaultException extends Exception
{

  private static final long serialVersionUID = 1L;

  private FaultBean faultBean;

  public SVSFaultException()
  {
    super();
  }

  public SVSFaultException(String message, FaultBean faultBean, Throwable cause)
  {
    super(message, cause);
    this.faultBean = faultBean;
  }

  public SVSFaultException(String message, FaultBean faultBean)
  {
    super(message);
    this.faultBean = faultBean;
  }
  
  public SVSFaultException(String message)
  {
    super(message);
  }

  public FaultBean getFaultInfo()
  {
    return faultBean;
  }

}
