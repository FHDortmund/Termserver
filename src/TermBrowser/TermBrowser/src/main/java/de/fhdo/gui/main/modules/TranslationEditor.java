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
package de.fhdo.gui.main.modules;

import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Center;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class TranslationEditor extends Window implements AfterCompose, IUpdate
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private CodeSystem codeSystem;

  public TranslationEditor()
  {
    logger.debug("TranslationEditor - Constructor");

  }

  public void afterCompose()
  {
    DomainHelper.getInstance().fillCombobox((Combobox) getFellow("comboTranslationLanguage"), de.fhdo.Definitions.DOMAINID_LANGUAGECODES,
            SessionHelper.getStringValue("selectedTranslationLanguageCd", null));
    
    Object obj = SessionHelper.getValue("selectedTranslationCS", null);
    if(obj != null)
      codeSystem = (CodeSystem) obj;
    
    loadData();
  }

  public void chooseCodesystem()
  {
    // open codesystem or value set
    try
    {
      Map data = new HashMap();
      data.put("allowCS", true);
      data.put("allowVS", false);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/ChooseCodesystem.zul", null, data);
      ((ChooseCodesystem) w).setUpdateListener(this);

      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  public void update(Object o)
  {
    // load codesystem or valueset
    if (o != null)
    {
      logger.debug("update with object: " + o.getClass().getCanonicalName());
      if (o instanceof CodeSystem)
      {
        codeSystem = (CodeSystem) o;
      }
//      else if(o instanceof ValueSet)
//      {
//        valueSet = (ValueSet) o;
//      }

      loadData();
    }

    Clients.clearBusy();
  }

  public void loadData()
  {
    logger.debug("loadData()");
    
    String languageCd = DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("comboTranslationLanguage"));
    if (languageCd != null && languageCd.length() > 0)
    {
      SessionHelper.setValue("selectedTranslationLanguageCd", languageCd);
    }
    
    boolean languageCdFound = languageCd != null && languageCd.length() > 0;
    logger.debug("languageCd: " + languageCd);
    

    String name = "";
    if (codeSystem != null)
    {
      name = codeSystem.getName();
      
      // remember choice
      SessionHelper.setValue("selectedTranslationCS", codeSystem);

      if (languageCdFound)
      {
        openCodeSystem(codeSystem);
      }
      
    }
//    else if(valueSet != null)
//    {
//      name = valueSet.getName();
//      
//      openValueSet(valueSet);
//    }
    
    if(languageCdFound == false)
    {
      Include inc = (Include) getFellow("incContent");
      inc.setSrc(null);  // force to reload
      inc.setSrc("/gui/main/content/NoContent.zul");
    }

    // set label on GUI
    ((Label) getFellow("labelSelectedCodesystem")).setValue(name);

    Clients.clearBusy();
  }

  private void openCodeSystem(CodeSystem cs)
  {
    if (cs == null)
      return;

    logger.debug("openCodeSystem with id: " + cs.getId());

    // open Codesystem
    Clients.showBusy(Labels.getLabel("common.loading"));

    Include inc = (Include) getFellow("incContent");
    inc.setSrc(null);  // force to reload

    // set propierties
    inc.clearDynamicProperties();
    inc.setDynamicProperty("codeSystem", cs);
    inc.setDynamicProperty("updateListener", this);
    inc.setDynamicProperty("translationEditor", this);

    String src = "/gui/main/content/ContentConcepts.zul?translation=true&editTranslation=true";

    logger.debug("src: " + src);
    inc.setSrc(src);
  }

//  public void languageChanged(Event event)
//  {
//    logger.debug("languageChanged, event-type: " + event.getClass().getCanonicalName());
//
//    String languageCd = DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("comboTranslationLanguage"));
//
//    logger.debug("languageCd: " + languageCd);
//    if (languageCd != null && languageCd.length() > 0)
//    {
//      SessionHelper.setValue("selectedTranslationLanguageCd", languageCd);
//      loadData();
//    }
//  }

}
