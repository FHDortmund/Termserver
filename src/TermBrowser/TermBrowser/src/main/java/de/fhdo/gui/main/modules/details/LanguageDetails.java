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
package de.fhdo.gui.main.modules.details;

import de.fhdo.helper.LanguageHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;

/**
 *
 * @author Robert Mützner
 */
public class LanguageDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private IUpdate updateListInterface;
  private Combobox cbLanguage;
  private Textbox tbValueOfLanguage;

  public LanguageDetails()
  {

  }

  public void afterCompose()
  {
      cbLanguage = (Combobox)getFellow("cbLanguage");
      tbValueOfLanguage = (Textbox)getFellow("tbValueOfLanguage");
      cbLanguage.setModel(LanguageHelper.getListModelList());
  }

  public void onOkClicked()
  {
  
      if(cbLanguage.getSelectedItem() == null || tbValueOfLanguage.getText() == null || tbValueOfLanguage.getText().equals(""))
      {
        Messagebox.show(Labels.getLabel("common.notemptyTranslationLanguage"), Labels.getLabel("common.error"), Messagebox.OK, Messagebox.INFORMATION);
        return;
      }
      
      CodeSystemConceptTranslation csct = new CodeSystemConceptTranslation();
      try
      {
        
        csct.setTerm(tbValueOfLanguage.getText());
        // TODO csct.setLanguageId(LanguageHelper.getLanguageIdByName(cbLanguage.getSelectedItem().getLabel()));

        this.setVisible(false);
        this.detach();

        if (updateListInterface != null)
            updateListInterface.update(csct);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in LanguageDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();

  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdate updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }
}
