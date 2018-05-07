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

import de.fhdo.helper.MD5;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authorization.ChangePasswordResponseType;
import de.fhdo.terminologie.ws.authorization.LoginResponse;
import de.fhdo.terminologie.ws.authorization.Status;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class PasswordDialog extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdate updateListInterface;
  private boolean erfolg = false;

  public PasswordDialog()
  {
    logger.debug("PasswordDialog() - Konstruktor");
  }

  /**
   * Im Anmeldefenster wurde "Return" gedrückt
   *
   * @param event
   */
  public void onOkPressed(KeyEvent event)
  {
    if (logger.isDebugEnabled())
      logger.debug("Enter gedrueckt!");

    Button b = (Button) getFellow("okButton");
    b.setDisabled(true);

    onOkClicked();

    b.setDisabled(false);
  }

  public void onOkClicked()
  {
    // speichern mit Hibernate
    try
    {
      Textbox tb1 = (Textbox)getFellow("pw1");
      Textbox tb2 = (Textbox)getFellow("pw2");
      Textbox tbAlt = (Textbox)getFellow("pwAlt");

      if(tb1.getValue().equals(tb2.getValue()) == false)
      {
        Messagebox.show("Das eingegebene Passwort stimmt nicht überein!", "Fehler", Messagebox.OK, Messagebox.ERROR);
        return;
      }

      // Webservice-Aufruf
      // Generische Parameterliste füllen (hier nur SessionID)
      List<String> parameterList = new LinkedList<String>();
      parameterList.add(SessionHelper.getUserName());
      parameterList.add(MD5.getMD5(tbAlt.getText()));
      parameterList.add(MD5.getMD5(tb1.getText()));

      ChangePasswordResponseType response = WebServiceHelper.changePassword(parameterList);
      logger.debug("Antwort: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        Messagebox.show(Labels.getLabel("common.passwordChanged"), Labels.getLabel("common.password"), Messagebox.OK, Messagebox.INFORMATION);
      }
      else
      {
        Messagebox.show(response.getReturnInfos().getMessage(), Labels.getLabel("common.password"), Messagebox.OK, Messagebox.EXCLAMATION);
      }

      //Messagebox.show("Passwort erfolgreich geändert.", "Passwort ändern", Messagebox.OK, Messagebox.INFORMATION);

      tb1.setValue("");
      tb2.setValue("");
      tbAlt.setValue("");

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        this.setVisible(false);
        this.detach();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      LoggingOutput.outputException(e, this);
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }


  public void afterCompose()
  {
    
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdate updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }

  /**
   * @return the erfolg
   */
  public boolean isErfolg()
  {
    return erfolg;
  }


}