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
package de.fhdo.models.itemrenderer;

import de.fhdo.helper.LanguageHelper;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;

/**
 *
 * @author Becker
 */
public class ListitemRendererTranslations implements ListitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean editableTranslationsList;

  public ListitemRendererTranslations(boolean editableTranslationsList)
  {
    this.editableTranslationsList = editableTranslationsList;
  }

  public void render(Listitem lstm, Object o, int index) throws Exception
  {
    CodeSystemConceptTranslation csct = (CodeSystemConceptTranslation) o;

    Listcell cellLanguage = new Listcell();
    Listcell cellTranslation = new Listcell();

    // TODO cellLanguage.setLabel(LanguageHelper.getLanguageTable().get(String.valueOf(csct.getLanguageId())));
    if (editableTranslationsList)
    {
      Textbox textBox = new Textbox();
      textBox.setText(csct.getTerm());
      textBox.setReadonly(true);
      textBox.setHflex("1");
      cellTranslation.appendChild(textBox);
    }
    else
    {
      cellTranslation.setLabel(csct.getTerm());
    }

    lstm.appendChild(cellLanguage);
    lstm.appendChild(cellTranslation);
  }
}
