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
package de.fhdo.helper;

import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ComponentHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();  
  
  public static void setVisible(String ComponentID, boolean Visible, Window Win)
  {
    Component c = (Component) Win.getFellow(ComponentID);
    c.setVisible(Visible);
  }
  
  public static void setVisibleAndDisabled(String ComponentID, boolean Visible, boolean Disabled, Window Win)
  {
    Component c = (Component) Win.getFellow(ComponentID);
    c.setVisible(Visible);
    
    if(c instanceof Button)
      ((Button)c).setDisabled(Disabled);
    else if(c instanceof Textbox)
      ((Textbox)c).setDisabled(Disabled);
    else if(c instanceof Combobox)
      ((Combobox)c).setDisabled(Disabled);
  }
  
  public static void doDisableAll(Component component, boolean disabled, List<String>ignoredIDs)
  {
    String id = component.getId();
    
    // Ausnahmen definieren
    if(ignoredIDs != null && ignoredIDs.contains(id))
      return;
    
    if (component instanceof Combobox) // Combobox extends Textbox (deswegen vor Textbox)
    {
      ((Combobox) component).setDisabled(disabled);
    }
    else if (component instanceof Intbox)
    {
      ((Intbox) component).setReadonly(disabled);
    }
    else if (component instanceof Doublebox)
    {
      ((Doublebox) component).setReadonly(disabled);
    }
    else if (component instanceof Textbox)
    {
      ((Textbox) component).setReadonly(disabled);
    }
    else if (component instanceof Button)
    {
      Button button = (Button) component;
      button.setDisabled(disabled);
    }
    else if (component instanceof Datebox)
    {
      ((Datebox) component).setDisabled(disabled);
    }
    else if (component instanceof Timebox)
    {
      ((Timebox) component).setDisabled(disabled);
    }
    else if (component instanceof Checkbox)
    {
      ((Checkbox) component).setDisabled(disabled);
    }
    else if (component instanceof Radiogroup)
    {
      for (Radio radio : ((Radiogroup) component).getItems())
      {
        radio.setDisabled(disabled);
      }
    }
    else
    {
      for (Component com : component.getChildren())
      {
        doDisableAll(com, disabled, ignoredIDs);
      }
    }
  }
}
