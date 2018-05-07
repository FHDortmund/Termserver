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
package de.fhdo.collaboration.proposal.newproposal;

import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ProposalCodeSystem implements INewProposal
{

  public String initData(Window window, Object obj)
  {
    
    
    return "";
  }

  public String checkMandatoryFields(Window window)
  {
    if(((Textbox)window.getFellow("tbVocName")).getText().length() == 0)
    {
      return "Sie müssen einen Namen für das Codesystem/Valueset angeben.";
    }
    if(((Textbox)window.getFellow("tbVocVersionName")).getText().length() == 0)
    {
      return "Sie müssen einen Namen für die Codesystem/Valueset-Version angeben.";
    }
    
    return "";
  }

  public String saveData(Window window, Proposal proposal, List<Object> proposalObjects)
  {
    if(((Checkbox)window.getFellow("cbVoc")).isChecked())
    {
      // Codesystem
      proposal.setContentType("vocabulary");
      
      CodeSystem cs = new CodeSystem();
      cs.setName(((Textbox)window.getFellow("tbVocName")).getText());
      cs.setDescription(((Textbox)window.getFellow("tbVocDescription")).getText());
      
      CodeSystemVersion csv = new CodeSystemVersion();
      csv.setName(((Textbox)window.getFellow("tbVocVersionName")).getText());
      csv.setDescription(((Textbox)window.getFellow("tbVocVersionDescription")).getText());
      
      cs.getCodeSystemVersions().add(csv);
      
      proposalObjects.add(cs);
      
    }
    else if(((Checkbox)window.getFellow("cbVal")).isChecked())
    {
      // Valueset
      proposal.setContentType("valueset");
      
    }
    
    
    
    return "";
  }
  
}
