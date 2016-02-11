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
package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.DomainHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author rober
 */
public class CodesystemStatus extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  CodeSystemVersion codeSystemVersion;
  private IUpdateModal iUpdateListener;

  public CodesystemStatus()
  {

    Object obj = ArgumentHelper.getWindowArgument("csv");
    if (obj != null && obj instanceof CodeSystemVersion)
    {
      codeSystemVersion = (CodeSystemVersion) obj;
    }

  }

  public void afterCompose()
  {
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbStatus"), Definitions.DOMAINID_STATUS, "" + codeSystemVersion.getStatus());
  }

  public void onOkClicked()
  {
    try
    {
      String code = DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cbStatus"));
      codeSystemVersion.setStatus(Integer.parseInt(code));

      iUpdateListener.update(codeSystemVersion, true);
      
      this.setVisible(false);
      this.detach();
    }
    catch (Exception e)
    {
      Messagebox.show(Labels.getLabel("changeStatusFailure") + ": " + e.getLocalizedMessage());
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
