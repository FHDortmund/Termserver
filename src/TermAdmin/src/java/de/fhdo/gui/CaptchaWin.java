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
package de.fhdo.gui;

import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class CaptchaWin extends Window implements AfterCompose
{
  private IUpdate updateInterface;
  
  public CaptchaWin()
  {
  }

  public void afterCompose()
  {
    Textbox tb = (Textbox) getFellow("tfCaptcha");
    tb.focus();
  }

  public void captchaCheck()
  {
    Textbox tb = (Textbox) getFellow("tfCaptcha");
    org.zkforge.bwcaptcha.Captcha captcha = (org.zkforge.bwcaptcha.Captcha) getFellow("cpa");

    if (captcha.getValue().toLowerCase().equals(tb.getValue().toLowerCase()))
    {
      // Captcha korrekt, in Session speichern
      SessionHelper.setValue("captcha_correct", true);
      
      // Formular schließen
      showRow("warningRow", false);
      this.setVisible(false);
      this.detach();
      
      if(updateInterface != null)
        updateInterface.update(null);
    }
    else
    {
      // Fehlermeldung ausgeben
      showRow("warningRow", true);
    }
  }
  
  private void showRow(String RowID, boolean Visible)
  {
    Row row = (Row) getFellow(RowID);
    row.setVisible(Visible);
  }
  
  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdate updateInterface)
  {
    this.updateInterface = updateInterface;
  }

}
