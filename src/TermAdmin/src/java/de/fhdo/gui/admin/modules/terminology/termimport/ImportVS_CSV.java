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
package de.fhdo.gui.admin.modules.terminology.termimport;

import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.list.GenericList;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.ImportValueSetRequestType;
import de.fhdo.terminologie.ws.administration.ImportValueSetResponse;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportVS_CSV implements IImport
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  long formatId;
  
  public ImportVS_CSV(long FormatId)
  {
    formatId = FormatId;
  }

  public void preview(GenericList genericList, byte[] bytes)
  {
    logger.debug("ImportVS_CSV - preview()");

  }

  /**
   * 
   * @param bytes
   * @param progress
   * @param labelInfo
   * @param codeSystem
   * @param valueSet
   * @return true, wenn asynchroner Aufruf
   */
  public boolean startImport(byte[] bytes, final Progressmeter progress,
          final Label labelInfo,
          CodeSystem codeSystem, de.fhdo.terminologie.db.hibernate.ValueSet valueSet)
  {
    logger.debug("ImportVS_CSV - startImport()");

    // Login
    ImportValueSetRequestType request = new ImportValueSetRequestType();
    request.setLoginToken(SessionHelper.getSessionId());

    // Codesystem
    request.setValueSet(new types.termserver.fhdo.de.ValueSet());
    request.getValueSet().setId(valueSet.getId());
    
    types.termserver.fhdo.de.ValueSetVersion vsv = new types.termserver.fhdo.de.ValueSetVersion();
    vsv.setName(((de.fhdo.terminologie.db.hibernate.ValueSetVersion)(valueSet.getValueSetVersions().toArray()[0])).getName());
    vsv.setVersionId(((de.fhdo.terminologie.db.hibernate.ValueSetVersion)(valueSet.getValueSetVersions().toArray()[0])).getVersionId());
    request.getValueSet().getValueSetVersions().add(vsv);
    
    request.setImportInfos(new ImportType());
    request.getImportInfos().setFilecontent(bytes);
    request.getImportInfos().setFormatId(formatId);
    //request.getImportInfos().setOrder(cbOrder.isChecked());

    ImportValueSetResponse.Return response = WebServiceHelper.importValueSet(request);
    //importWS = (Response<ImportValueSetResponse>) port.importValueSetAsync(request, this);
    String msg = response.getReturnInfos().getMessage();
    logger.debug("Import abgeschlossen: " + msg);
    
    labelInfo.setValue(msg);

    progress.setVisible(false);
//    ((Button) getFellow("buttonImport")).setDisabled(false);
//    ((Button) getFellow("buttonCancel")).setVisible(false);
//    ((Label) getFellow("labelImportStatus")).setValue(returnStr);
   return false;
  }

  public void cancelImport()
  {
  }

  public boolean supportsFormat(String format)
  {
    if (format != null)
    {
      if (format.equalsIgnoreCase("csv") || format.equalsIgnoreCase("txt") || format.equalsIgnoreCase("xls"))
        return true;
    }
    return false;
  }

  public boolean mustSpecifyCodesystem()
  {
    return true;
  }

  public boolean mustSpecifyCodesystemVersion()
  {
    return true;
  }

}
