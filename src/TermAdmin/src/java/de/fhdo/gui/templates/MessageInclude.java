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
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class MessageInclude extends Window implements AfterCompose
{
  String message = "";
  
  public MessageInclude()
  {
    message = ArgumentHelper.getWindowParameterString("msg");
    
    if(message == null || message.length() == 0)
      message = Labels.getLabel("noSelection");
  }
  
  public void afterCompose()
  {
    ((Label)getFellow("labelMessage")).setValue(message);
  }
  
  
  
}
