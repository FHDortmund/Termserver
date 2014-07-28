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
package de.fhdo.gui.admin;

import de.fhdo.helper.CookieHelper;
import de.fhdo.helper.MD5;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Login extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean erfolg = false;
  int loginCount = 0;

  public Login()
  {
  }

  public void afterCompose()
  {
    String username = CookieHelper.getCookie("username");
    ((Textbox) getFellow("name")).setText(username);
    
    if(username != null && username.length() > 0)
    {
      ((Textbox) getFellow("pwd")).setFocus(true);
      ((Textbox) getFellow("pwd")).focus();
    }
  }
  
  public void loginCheck()
  {
    try
    {
      // Daten aus dem Formular auslesen
      Textbox tbUser = (Textbox) getFellow("name");
      Textbox tbPass = (Textbox) getFellow("pwd");

      if (logger.isDebugEnabled())
        logger.debug("Login wird durchgefuehrt...");

      // Webservice-Aufruf
      // Generische Parameterliste füllen (hier nur SessionID)
      List<String> parameterList = new LinkedList<String>();
      parameterList.add(tbUser.getText());
      parameterList.add(MD5.getMD5(tbPass.getText()));

      LoginResponse.Return response = WebServiceHelper.login(parameterList);
      logger.debug("Antwort: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK &&
          response.getParameterList() != null && response.getParameterList().size() > 0)
      {
        // Hauptseite aufrufen
        CookieHelper.removeCookie("show_captcha");
        CookieHelper.setCookie("username", tbUser.getText());
        
        logger.debug("Login erfolgreich, Session-ID: " + response.getParameterList().get(0));
        SessionHelper.setValue("session_id", response.getParameterList().get(0));
        
        SessionHelper.setValue("user_name", tbUser.getText());

        Clients.showBusy("Login erfolgreich\n\nTermBrowser wird geladen...");
        Executions.sendRedirect("/gui/main/main.zul");
        //Executions.getCurrent().sendRedirect("../../TermBrowser/gui/main/main.zul?" + "p1=" + userAndPseudEnc);
      }
      else
      {
        // Login falsch
        loginCount++;

        /*if (loginCount > 2)
         {
         CookieHelper.setCookie("show_captcha", "1");
         showCaptcha();
         }*/
        showRow("warningRow", true);

        //Label label = (Label) getFellow("temp");
        //label.setValue("Versuch " + loginCount);
      }
      
    }
    catch (Exception e)
    {
      logger.error(e.getLocalizedMessage());
      LoggingOutput.outputException(e, this);
    }

    /*Button b = (Button) getFellow("loginButton");
     b.setDisabled(true);
    
     // Daten aus dem Formular auslesen
     Textbox tbUser = (Textbox) getFellow("name");
     Textbox tbPass = (Textbox) getFellow("pwd");

     if (logger.isDebugEnabled())
     logger.debug("Login wird durchgefuehrt...");    
    
     // Login-Daten ueberpruefen
     boolean loginCorrect = LoginHelper.getInstance().login(tbUser.getValue(), tbPass.getValue(),false,"bogous");
     if(loginCorrect)
     {
     logger.debug("Login correct");
     Clients.showBusy(Labels.getLabel("login.LoginOK") + "\n\n" + Labels.getLabel("login.ReloadTB"));
      
     ComponentHelper.setVisible("warningRow", false, this);
      
     this.setVisible(false);
     this.detach();
      
     TreeModelVS.reloadData(getDesktop()); // neu Laden, da alte Versionen nur nach login angezeigt werden
     TreeModelCS.reloadData(getDesktop()); // neu Laden, da alte Versionen nur nach login angezeigt werden
      
     erfolg = true;      
     //Executions.getCurrent().sendRedirect(null, "_blank");
     //ViewHelper.gotoSrc(null);
     }
     else
     {
     ComponentHelper.setVisible("warningRow", true, this);
     Label lMsg = (Label) getFellow("mesg");
     lMsg.setValue(Labels.getLabel("login.usernameUnknownOrErro"));
     erfolg = false;
     }
    
     b.setDisabled(false);*/
  }

  private void showRow(String RowID, boolean Visible)
  {
    Row row = (Row) getFellow(RowID);
    row.setVisible(Visible);
  }

  /**
   * @return the erfolg
   */
  public boolean isErfolg()
  {
    return erfolg;
  }

  
}
