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
import java.util.List;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ProposalConcept implements INewProposal
{

  public String initData(Window window, Object obj)
  {
    ((Label)window.getFellow("lConceptVocabulary")).setValue("");
    
    ((Label)window.getFellow("lParentConcept")).setValue("");
    //rowParentConcept
    
    return "";
  }

  public String checkMandatoryFields(Window window)
  {
    if(((Textbox)window.getFellow("tbCode")).getText().length() == 0)
    {
      return "Sie müssen einen Code angeben.";
    }
    if(((Textbox)window.getFellow("tbTerm")).getText().length() == 0)
    {
      return "Sie müssen einen Begriff angeben.";
    }

    return "";
  }

  public String saveData(Window window, Proposal proposal, List<Object> proposalObjects)
  {

    // Codesystem
    proposal.setContentType("concept");

    CodeSystemEntity cse = new CodeSystemEntity();
    
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setMajorRevision(1);
    csev.setMinorRevision(0);
    
    CodeSystemConcept csc = new CodeSystemConcept();
    csc.setCode(((Textbox)window.getFellow("tbCode")).getText());
    csc.setTerm(((Textbox)window.getFellow("tbTerm")).getText());
    csc.setTermAbbrevation(((Textbox)window.getFellow("tbAbbrevation")).getText());
    csc.setIsPreferred(((Checkbox)window.getFellow("cbPreferred")).isChecked());
    csc.setDescription(((Textbox)window.getFellow("tbDescription")).getText());
    csc.setTerm(((Textbox)window.getFellow("tbTerm")).getText());
    
    csev.getCodeSystemConcepts().add(csc);
    cse.getCodeSystemEntityVersions().add(csev);

    proposalObjects.add(cse);

    return "";
  }
}
