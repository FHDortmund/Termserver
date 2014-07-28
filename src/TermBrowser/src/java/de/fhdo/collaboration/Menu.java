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
package de.fhdo.collaboration;

import de.fhdo.logging.LoggingOutput;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Menu extends Window
{
  
  public Menu()
  {
    
  }
  
  public void onBackToTermBrowser()
  {
    Executions.getCurrent().sendRedirect("/gui/main/main.zul");
  }
  
  public void onShowWorkflow()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
              "/collaboration/workflow.zul",
              null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }
  
  public void callOidRegister(){
      Executions.getCurrent().sendRedirect("https://www.gesundheit.gv.at/OID_Frontend/", "_blank");
  }
  
  public void onUeberClicked()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
              "/gui/info/about.zul",
              null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }
}
