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

import de.fhdo.helper.ParameterHelper;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Sven Becker
 */
public class ContentVSV extends Window implements AfterCompose {
    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    int lastSelection = -1;
    long ValueSetVersionId = 0, ValueSetId = 0;    

    public ContentVSV() {
        // Argumente laden
        ValueSetId = ParameterHelper.getLong("ValueSetId");
        ValueSetVersionId = ParameterHelper.getLong("ValueSetVersionId");
    }

    public void afterCompose() {
        onTabSelect();
    }

    public void onTabSelect() {
        Include inc = null;
        String src = "";
        Tabbox tb = (Tabbox) getFellow("tabboxFilter");
        int sel = tb.getSelectedIndex();

        if (sel == lastSelection) {
            return;
        }

        switch (sel) {
            case 0:  // XXX
                inc = (Include) getFellow("incXXX");
                src = "modules/ContentConceptsVSV.zul?ValueSetVersionId=" + ValueSetVersionId + "&ValueSetId=" + ValueSetId;
                break;
        }
        inc.setSrc(null);
        inc.setSrc(src);        
        lastSelection = sel;
    }

    public void onFilterSelect() {
    }
}
