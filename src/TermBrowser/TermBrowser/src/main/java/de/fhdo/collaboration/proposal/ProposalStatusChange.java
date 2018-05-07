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
package de.fhdo.collaboration.proposal;

import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.interfaces.IUpdateModal;
import java.util.Date;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ProposalStatusChange extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal updateInterface;
  private Proposal proposal;
  private long statusToId;
  boolean isDiscussion;

  public ProposalStatusChange()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("ProposalStatusChange() - Konstruktor");
      logger.debug("lade Parameter...");
    }

    proposal = (Proposal) ArgumentHelper.getWindowArgument("proposal");
    statusToId = ArgumentHelper.getWindowArgumentLong("status_to_id");

    if (logger.isDebugEnabled())
    {
      logger.debug("status_to_id: " + statusToId);
      logger.debug("proposal-ID: " + proposal.getId());
    }


  }

  public void afterCompose()
  {
    isDiscussion = ProposalHelper.isStatusDiscussion(statusToId);
    ((Row)getFellow("rowZeitraum")).setVisible(isDiscussion);
    
    
  }

  public void onOkClicked()
  {
    // Statusänderung durchführen
    String reason = ((Textbox) getFellow("tbReason")).getValue();
    Date dateFrom = ((Datebox)getFellow("dateVon")).getValue();
    Date dateTo = ((Datebox)getFellow("dateBis")).getValue();
    
    if(dateFrom != null)
      logger.debug("Datum von: " + dateFrom);
    else logger.debug("Datum von: null");
    
    ReturnType ret = ProposalWorkflow.getInstance().changeProposalStatus(proposal, statusToId, reason, 
            dateFrom, dateTo);

    // Fenster schließen
    this.setVisible(false);
    this.detach();

    // Vorschlag-Fenster aktualisieren
    if (updateInterface != null)
    {
      updateInterface.update(ret, false);
    }

  }

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }
}
