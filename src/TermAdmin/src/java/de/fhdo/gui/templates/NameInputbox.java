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
package de.fhdo.gui.templates;

import de.fhdo.helper.ArgumentHelper;
import de.fhdo.interfaces.IUpdateModal;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class NameInputbox extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal iUpdateListener;
  private String text;
  
  public NameInputbox()
  {
    text = "";
    
    Object obj = ArgumentHelper.getWindowArgument("text");
    if (obj != null && obj instanceof String)
    {
      text = (String) obj;
    }

  }

  public void afterCompose()
  {
    ((Textbox)getFellow("textbox")).setText(text);
  }
  
  public void onOkClicked()
  {
    try
    {
      String newText = ((Textbox)getFellow("textbox")).getText();
      iUpdateListener.update(newText, !newText.equals(text));
      
      this.setVisible(false);
      this.detach();
    }
    catch (Exception e)
    {
      Messagebox.show(Labels.getLabel("error") + ": " + e.getLocalizedMessage());
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  /**
   * @param iUpdateListener the iUpdateListener to set
   */
  public void setiUpdateListener(IUpdateModal iUpdateListener)
  {
    this.iUpdateListener = iUpdateListener;
  }
}
