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
package de.fhdo.helper;

import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ViewHelper
{

  private static org.zkoss.zk.ui.Session session = Sessions.getCurrent();

  public static void gotoSrc(String Src)
  {
    session.setAttribute("current_domain", Src);
    Executions.sendRedirect(Src);
  }

  public static void gotoAdmin()
  {
    //session.setAttribute("current_domain", Definitions.DOMAIN_ADMIN);
    //Executions.sendRedirect(Definitions.DOMAIN_ADMIN);
  }

  public static void gotoMain()
  {
    //session.setAttribute("current_domain", Definitions.DOMAIN_MAIN);
    //Executions.sendRedirect(Definitions.DOMAIN_MAIN);
  }

  public static void removeAllChildren(Component Comp)
  {
    List childs = Comp.getChildren();
    if (childs != null)
      childs.clear();
  }

  public static void showComponent(Window Win, String Comp, boolean Visible)
  {
    try
    {
      Component comp = Win.getFellow(Comp);
      if (comp != null)
      {
        comp.setVisible(Visible);
      }
    }
    catch (Exception e)
    {
    }
  }

  public static void showComponent(Component Comp, boolean Visible)
  {
    if (Comp != null)
    {
      Comp.setVisible(Visible);
    }
  }

  /*public static void gotoPatientlist()
  {
  session.setAttribute("current_domain", Definitions.DOMAIN_PATIENTLIST);
  PersonHelper.getInstance().freeData();
  Executions.sendRedirect("/gui/patientlist/main.zul");
  }*/

  /*public static void gotoPatientrecord()
  {
  //System.out.println("gotoPatientrecord");
  session.setAttribute("current_domain", OphepaDefs.DOMAIN_PATIENTRECORD);
  Executions.sendRedirect("/gui/patientrecord/main.zul");
  }*/
}
