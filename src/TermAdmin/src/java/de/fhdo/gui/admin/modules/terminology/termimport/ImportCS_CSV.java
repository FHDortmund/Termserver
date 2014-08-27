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

import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.list.GenericList;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.Status;
import javax.xml.ws.soap.MTOMFeature;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportCS_CSV implements IImport
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  long formatId;
  
  public ImportCS_CSV(long FormatId)
  {
    formatId = FormatId;
  }
  
  public void preview(GenericList genericList, byte[] bytes)
  {
    
    
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
  public boolean startImport(byte[] bytes, Progressmeter progress, Label labelInfo, CodeSystem codeSystem, ValueSet valueSet)
  {
    logger.debug("ImportCS_CSV - startImport()");
    
    
    progress.setVisible(true);

    // Login
    ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
    request.setLoginToken(SessionHelper.getSessionId());

    // Codesystem
    request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
    request.getCodeSystem().setId(codeSystem.getId());

    types.termserver.fhdo.de.CodeSystemVersion csv = new CodeSystemVersion();
    
    if(codeSystem.getCodeSystemVersions() != null && codeSystem.getCodeSystemVersions().size() > 0)
    {
      csv.setVersionId(((de.fhdo.terminologie.db.hibernate.CodeSystemVersion)codeSystem.getCodeSystemVersions().toArray()[0]).getVersionId());
      csv.setName(((de.fhdo.terminologie.db.hibernate.CodeSystemVersion)codeSystem.getCodeSystemVersions().toArray()[0]).getName());
    }
    
    request.getCodeSystem().getCodeSystemVersions().add(csv);

    // Claml-Datei
    request.setImportInfos(new ImportType());
    request.getImportInfos().setFormatId(formatId); // CSV_ID
    request.getImportInfos().setFilecontent(bytes);
    
    ImportCodeSystemResponse.Return response = WebServiceHelper.importCodeSystem(request);

    String msg = response.getReturnInfos().getMessage();
    logger.debug("Return: " + msg);

    //CodeSystemVersion
    if (response.getReturnInfos().getStatus().equals(Status.OK))
    {

      
    }

    labelInfo.setValue(msg);
    progress.setVisible(false);
    
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
