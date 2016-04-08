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
import de.fhdo.helper.LanguageHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CodesystemsContent extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private Object selectedItem;
  private Object selectedItemVersion;

  public CodesystemsContent()
  {
    selectedItem = SessionHelper.getValue("CS_SelectedItem");
    selectedItemVersion = SessionHelper.getValue("CS_SelectedItemVersion");
    //long csvId = ArgumentHelper.getWindowParameterLong("csvId");
    //long vsvId = ArgumentHelper.getWindowParameterLong("vsvId");

  }

  public void afterCompose()
  {
    loadData();
  }

  private void loadData()
  {
    if (selectedItem instanceof CodeSystem)
    {
      loadDataCS();
    }
    else if (selectedItem instanceof CodeSystemVersion)
    {
      loadDataVS();
    }
    
    if (selectedItem != null)
    {
      ((Include) getFellow("incMetadata")).setSrc(null);
      ((Include) getFellow("incMetadata")).setSrc("/gui/admin/modules/terminology/metadata/metadatenCS.zul");
      
    }
  }

  private void loadDataCS()
  {
    CodeSystem cs = (CodeSystem) selectedItem;

    if (selectedItemVersion == null)
    {
      getFellow("incDetails").setVisible(true);
      getFellow("gridDetails").setVisible(false);
    }
    else
    {
      // show details
      getFellow("incDetails").setVisible(false);
      getFellow("gridDetails").setVisible(true);

      CodeSystemVersion csv = (CodeSystemVersion) selectedItemVersion;

      logger.debug("loadDataCS with csv-id: " + csv.getVersionId());

      ((Label) getFellow("labelVersionId")).setValue(csv.getVersionId() + "");
      ((Label) getFellow("labelStatus")).setValue(Definitions.STATUS_CODES.readLabel(csv.getStatus()));
      ((Label) getFellow("labelOID")).setValue(csv.getOid());
      ((Label) getFellow("labelLanguage")).setValue(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_ISO_639_1_LANGUACECODES, csv.getPreferredLanguageCd()));
      ((Checkbox) getFellow("checkboxLicense")).setChecked(csv.getUnderLicence() != null ? csv.getUnderLicence() : false);
      getFellow("rowLicense").setVisible(true);

      ((Label) getFellow("labelLanguages")).setValue(getLanguageList(csv.getAvailableLanguages()));
      getFellow("rowLanguages").setVisible(true);
    }

    ((Include) getFellow("incTaxonomy")).setSrc(null);
    ((Include) getFellow("incTaxonomy")).setSrc("/gui/admin/modules/terminology/taxonomy.zul");
    

  }

  private void loadDataVS()
  {
    ValueSet vs = (ValueSet) selectedItem;

    if (selectedItemVersion == null)
    {
      getFellow("incDetails").setVisible(true);
      getFellow("gridDetails").setVisible(false);
    }
    else
    {
      ValueSetVersion vsv = (ValueSetVersion) selectedItemVersion;

      logger.debug("loadDataVS with vsv-id: " + vsv.getVersionId());

      // show details
      ((Label) getFellow("labelVersionId")).setValue(vsv.getVersionId() + "");
      ((Label) getFellow("labelStatus")).setValue(Definitions.STATUS_CODES.readLabel(vsv.getStatus()));
      ((Label) getFellow("labelOID")).setValue(vsv.getOid());
      ((Label) getFellow("labelLanguage")).setValue(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_ISO_639_1_LANGUACECODES, vsv.getPreferredLanguageCd()));
      getFellow("rowLicense").setVisible(false);
      getFellow("rowLanguages").setVisible(false);
    }
   

  }

  private String getLanguageList(String languageStr)
  {
    String s = "";
    for (String language : LanguageHelper.getLanguagesFromString(languageStr))
    {
      if (s.length() > 0)
        s += ", ";
      s += LanguageHelper.getLanguageNameFromCode(language);
    }
    return s;
  }

}
