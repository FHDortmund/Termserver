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

import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse.Return;
import de.fhdo.terminologie.ws.administration.ExportParameterType;
import de.fhdo.terminologie.ws.administration.ExportType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class PopupExport extends GenericForwardComposer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private CodeSystemVersion csv;
  private ValueSetVersion vsv;
  private Window window;
  private Combobox cboxFormat;
  private Label lProgress;
  private Row rowProgress;
  private Row rowHinweis;
  SimpleDateFormat sdfFilename = new SimpleDateFormat("yyyyMMdd");

  @Override
  public void doAfterCompose(Component comp) throws Exception
  {
    super.doAfterCompose(comp);
    window = (Window) comp;

    if (arg.get("CSV") != null)
      csv = (CodeSystemVersion) arg.get("CSV");

    if (arg.get("VSV") != null)
      vsv = (ValueSetVersion) arg.get("VSV");

    if (csv != null)
    {
      /* formatId: 193 = ClaML, 194 = CSV, 195 = SVS*/
      cboxFormat.appendItem("CSV");
      cboxFormat.appendItem("ClaML");  // TODO Werte aus Domain lesen
      cboxFormat.appendItem("SVS");
      
      cboxFormat.setSelectedIndex(0);

      //logger.debug("CSV: " + csv.getVersionId() + ", " + csv.getName());
    }
    else if (vsv != null)
    {
      cboxFormat.appendItem("CSV");
      cboxFormat.appendItem("SVS");
      cboxFormat.setSelectedIndex(0);
    }
  }

  /* formatId: 193 = ClaML, 194 = CSV */
  public void export(long formatId)
  {
    if (csv != null)
    {
      ExportCodeSystemContentRequestType parameter = new ExportCodeSystemContentRequestType();

      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      parameter.setCodeSystem(new CodeSystem());
      parameter.getCodeSystem().setId(csv.getCodeSystem().getId());
      CodeSystemVersion csvE = new CodeSystemVersion();
      csvE.setVersionId(csv.getVersionId());
      parameter.getCodeSystem().getCodeSystemVersions().add(csvE);

      // Export Tyep
      ExportType eType = new ExportType();
      eType.setFormatId(formatId);            // TODO 193 = ClaML, 194 = CSV, 195 SVS
      eType.setUpdateCheck(false);
      parameter.setExportInfos(eType);

      // Optional: ExportParameter
      ExportParameterType eParameterType = new ExportParameterType();
      eParameterType.setAssociationInfos("");
      eParameterType.setCodeSystemInfos(true);
      eParameterType.setTranslations(true);
      parameter.setExportParameter(eParameterType);

      logger.debug("Export-Service-Aufruf...");

      // WS-Aufruf
      Return response = WebServiceHelper.exportCodeSystemContent(parameter);

      // WS-Antwort
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.administration.Status.OK)
        {
          //Messagebox.show(Labels.getLabel("contentCSVSDefault.exportClaMLSuccessful"));

          rowProgress.setVisible(true);
          rowHinweis.setVisible(false);
          lProgress.setValue(response.getReturnInfos().getMessage());

          
          downloadFile(formatId, response.getExportInfos().getFilecontent(), sdfFilename.format(new Date()) + "_" + csv.getCodeSystem().getName() + "_" + csv.getName());
        }
        else
          Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("contentCSVSDefault.exportClaMLFailed"));
      }
      catch (Exception e)
      {
        logger.error("Fehler beim Speichern einer Datei: " + e.getLocalizedMessage());
      }
    }
    else if(vsv != null)
    {
      // ValueSet-Export
      
      ExportValueSetContentRequestType parameter = new ExportValueSetContentRequestType();

      // Login
      parameter.setLoginToken(SessionHelper.getSessionId());

      parameter.setValueSet(new ValueSet());
      parameter.getValueSet().setId(vsv.getValueSet().getId());
      ValueSetVersion vsvE = new ValueSetVersion();
      vsvE.setVersionId(vsv.getVersionId());
      parameter.getValueSet().getValueSetVersions().add(vsvE);

      // Export Type
      ExportType eType = new ExportType();
      eType.setFormatId(formatId);            // TODO 193 = ClaML, 194 = CSV, 195 = SVS
      eType.setUpdateCheck(false);
      parameter.setExportInfos(eType);

      // Optional: ExportParameter
      ExportParameterType eParameterType = new ExportParameterType();
      eParameterType.setAssociationInfos("");
      eParameterType.setCodeSystemInfos(false);
      eParameterType.setTranslations(false);
      parameter.setExportParameter(eParameterType);

      logger.debug("Export-Service-Aufruf...");

      // WS-Aufruf
      ExportValueSetContentResponse.Return response = WebServiceHelper.exportValueSetContent(parameter);

      // WS-Antwort
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.administration.Status.OK)
        {
          rowProgress.setVisible(true);
          rowHinweis.setVisible(false);
          lProgress.setValue(response.getReturnInfos().getMessage());

          downloadFile(formatId, response.getExportInfos().getFilecontent(), sdfFilename.format(new Date()) + "_" + vsv.getValueSet().getName() + "_" + vsv.getName());
        }
        else
          Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("contentCSVSDefault.exportClaMLFailed"));
      }
      catch (Exception e)
      {
        logger.error("Fehler beim Speichern einer Datei: " + e.getLocalizedMessage());
      }
    }

  }

  private void downloadFile(long formatId, byte[] bytes, String name)
  {
    if (formatId == 193 || formatId == 195)
    {
      Filedownload.save(bytes,
              "application/xml",
              name + ".xml");
    }
    else if (formatId == 194)
    {
      Filedownload.save(bytes,
              "text/csv",
              name + ".csv");
    }
    else
    {
      Filedownload.save(bytes,
              "text/plain",
              name + ".txt");
    }
  }

  /* formatId: 193 = ClaML, 194 = CSV */
  public void onClick$bExport()
  {
    long idFormat;

    if (cboxFormat.getValue().contains("ClaML")) // TODO Wert aus Domain
      idFormat = 193l;
    else if (cboxFormat.getValue().contains("CSV"))
      idFormat = 194l;
    else if (cboxFormat.getValue().contains("SVS"))
      idFormat = 195l;
    else
      return;

    export(idFormat);
  }

  public void onClick$bClose()
  {
    window.detach();
  }
}
