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
package de.fhdo.gui.main.content;

import de.fhdo.gui.main.modules.AssociationEditor;
import de.fhdo.gui.main.modules.ChooseCodesystem;
import de.fhdo.gui.main.modules.ValuesetEditor;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Center;
import org.zkoss.zul.Include;
import org.zkoss.zul.North;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ContentAssociationEditor extends Window implements AfterCompose, IUpdate
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private CodeSystem codeSystem;
  private ValueSet valueSet;
  
  private String windowId = "";
  private IUpdate updateListener;
  
  private AssociationEditor associationEditor;
  private ValuesetEditor valuesetEditor;
  
  private boolean allowCS, allowVS;
  private String title;
  
  public ContentAssociationEditor()
  {
    windowId = ArgumentHelper.getWindowParameterString("id");
    logger.debug("windowId: " + windowId);
    title = "";
    
    allowCS = ArgumentHelper.getWindowParameterBool("allowCS", true);
    allowVS = ArgumentHelper.getWindowParameterBool("allowVS", true);
    
    logger.debug("allowCS: " + allowCS);
    logger.debug("allowVS: " + allowVS);
    
    Object o = SessionHelper.getValue("selectedCS" + windowId, null);
    if(o != null)
      codeSystem = (CodeSystem)o;
    
    o = SessionHelper.getValue("selectedVS" + windowId, null);
    if(o != null)
      valueSet = (ValueSet)o;
    
    if(Executions.getCurrent().hasAttribute("updateListener"))
    {
      updateListener = (IUpdate) Executions.getCurrent().getAttribute("updateListener");
      logger.debug("[ContentAssociationEditor.java] UpdateListener found");
    }
    
    if(Executions.getCurrent().hasAttribute("associationEditor"))
    {
      associationEditor = (AssociationEditor) Executions.getCurrent().getAttribute("associationEditor");
      logger.debug("[ContentAssociationEditor.java] AssociationEditor found");
    }
    if(Executions.getCurrent().hasAttribute("valuesetEditor"))
    {
      valuesetEditor = (ValuesetEditor) Executions.getCurrent().getAttribute("valuesetEditor");
      logger.debug("[ContentAssociationEditor.java] ValuesetEditor found");
    }
    
    
    
    if(Executions.getCurrent().hasAttribute("title"))
    {
      title = (String) Executions.getCurrent().getAttribute("title");
    }
    
    
    
    //Executions.getCurrent().setAttribute("instance", this);
    
  }
  
  public void afterCompose()
  {
    loadData();
    
    if(title != null && title.length() > 0)
      ((North)getFellow("title")).setTitle(title);
    
    Clients.clearBusy();
  }
  
  public ContentConcepts getContent()
  {
    Include inc = (Include) getFellow("incConcepts");
    return (ContentConcepts) inc.getDynamicProperty("instance");
  }
  
  public void open()
  {
    // open codesystem or value set
    try
    {
      Map data = new HashMap();
      data.put("allowCS", allowCS);
      data.put("allowVS", allowVS);
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
    if(o != null)
    {
      logger.debug("update with object: " + o.getClass().getCanonicalName());
      if(o instanceof CodeSystem)
      {
        codeSystem = (CodeSystem) o;
      }
      else if(o instanceof ValueSet)
      {
        valueSet = (ValueSet) o;
      }
      
      loadData();
    }
    
    Clients.clearBusy();
  }
  
  private void loadData()
  {
    String name = "";
    if(codeSystem != null)
    {
      name = codeSystem.getName();
      
      openCodeSystem(codeSystem);
    }
    else if(valueSet != null)
    {
      name = valueSet.getName();
      
      openValueSet(valueSet);
    }
    
    ((Center)getFellow("center")).setTitle(name);
    
    
  }
  
  private void openCodeSystem(CodeSystem cs)
  {
    if (cs == null)
      return;

    logger.debug("openCodeSystem with id: " + cs.getId() + ", windowId: " + windowId);

    // remember choice
    SessionHelper.setValue("selectedCS" + windowId, cs);
    SessionHelper.setValue("selectedVS" + windowId, null);

    // open Codesystem
    Clients.showBusy(Labels.getLabel("common.loading"));

    Include inc = (Include) getFellow("incConcepts");
    inc.setSrc(null);  // force to reload

    // set propierties
    inc.clearDynamicProperties();
    inc.setDynamicProperty("codeSystem", cs);
    inc.setDynamicProperty("updateListener", updateListener);
    inc.setDynamicProperty("associationEditor", associationEditor);
    inc.setDynamicProperty("valuesetEditor", valuesetEditor);
    
    logger.debug("src: " + "/gui/main/content/ContentConcepts.zul?dragAndDrop=true&lookForward=true&windowId=" + windowId);
    inc.setSrc("/gui/main/content/ContentConcepts.zul?dragAndDrop=true&lookForward=true&windowId=" + windowId);
  }

  private void openValueSet(ValueSet vs)
  {
    if (vs == null)
      return;

    logger.debug("openValueSet with id: " + vs.getId());

    // remember choice
    SessionHelper.setValue("selectedCS" + windowId, null);
    SessionHelper.setValue("selectedVS" + windowId, vs);

    // open Codesystem
    Clients.showBusy(Labels.getLabel("common.loading"));

    Include inc = (Include) getFellow("incConcepts");
    inc.setSrc(null);  // force to reload

    // set propierties
    inc.clearDynamicProperties();
    inc.setDynamicProperty("valueSet", vs);
    inc.setDynamicProperty("updateListener", updateListener);
    inc.setDynamicProperty("associationEditor", associationEditor);
    inc.setDynamicProperty("valuesetEditor", valuesetEditor);

    inc.setSrc("/gui/main/content/ContentConcepts.zul?dragAndDrop=true&dragAndDropTree=true&windowId=" + windowId);
  }

  /**
   * @return the updateListener
   */
  public IUpdate getUpdateListener()
  {
    return updateListener;
  }

  /**
   * @param updateListener the updateListener to set
   */
  public void setUpdateListener(IUpdate updateListener)
  {
    this.updateListener = updateListener;
  }

  
  
  
  
}
