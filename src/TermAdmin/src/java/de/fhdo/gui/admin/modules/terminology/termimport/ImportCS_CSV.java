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
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemCancelResponseType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemStatusResponse;
import de.fhdo.terminologie.ws.administration.ImportType;
import ehd._001.KeytabsTyp;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.Response;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportCS_CSV implements IImport, javax.xml.ws.AsyncHandler<ImportCodeSystemResponse>, EventListener
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  long formatId;
  
  Response<ImportCodeSystemResponse> importWS;
  Timer timer;
  boolean importRunning = false;
  int lastValue = -1;
  
  Window window;
  
  public ImportCS_CSV(long FormatId, Window win)
  {
    formatId = FormatId;
    window = win;
  }
  
  public void preview(GenericList genericList, byte[] bytes)
  {
    try
    {
      /*
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("Name", 180, "", false, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Wert", 0, "", false, "boolean", true, true, false, false));

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      
      dataList.add(createRow("Codesystem name:",selectedCodeSystem.getName()));
      dataList.add(createRow("Version name:",csv.getName()));
      dataList.add(createRow("OID:",csv.getOid()));
      
      genericList.setButton_new(false);
      genericList.setListHeader(header);
      genericList.setDataList(dataList);

      genericList.getParent().setVisible(true);*/

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);

    }
    
  }
  
//  private GenericListRowType createRow(String key, String value)
//  {
//    GenericListRowType row = new GenericListRowType();
//
//    GenericListCellType[] cells = new GenericListCellType[2];
//    cells[0] = new GenericListCellType(key, false, "");
//    cells[1] = new GenericListCellType(value, false, "");
//
//    row.setCells(cells);
//
//    return row;
//  }

  public boolean startImport(byte[] bytes, final Progressmeter progress, Label labelInfo, CodeSystem codeSystem, ValueSet valueSet)
  {
    logger.debug("ImportCS_CSV - startImport()");
    progress.setVisible(true);
    lastValue = -1;

    // Login
    ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
    request.setLoginToken(SessionHelper.getSessionId());

    // Codesystem
    request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
    request.getCodeSystem().setId(codeSystem.getId());
    
    // Codesystem version
    types.termserver.fhdo.de.CodeSystemVersion csv = new CodeSystemVersion();
    
    if(codeSystem.getCodeSystemVersions() != null && codeSystem.getCodeSystemVersions().size() > 0)
    {
      csv.setVersionId(((de.fhdo.terminologie.db.hibernate.CodeSystemVersion)codeSystem.getCodeSystemVersions().toArray()[0]).getVersionId());
      csv.setName(((de.fhdo.terminologie.db.hibernate.CodeSystemVersion)codeSystem.getCodeSystemVersions().toArray()[0]).getName());
    }
    
    request.getCodeSystem().getCodeSystemVersions().add(csv);

    // CSV-Datei
    request.setImportInfos(new ImportType());
    request.getImportInfos().setFormatId(formatId);
    request.getImportInfos().setFilecontent(bytes);

    final org.zkoss.zk.ui.Desktop desktop = Executions.getCurrent().getDesktop();
    if (desktop.isServerPushEnabled() == false)
      desktop.enableServerPush(true);
    final EventListener el = this;

    final String sessionId = SessionHelper.getSessionId();
    logger.debug("Session-ID: " + sessionId);

    // Import durchführen
    timer = new Timer();
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        try
        {
          if (importWS == null || importWS.isCancelled() || importWS.isDone())
          {
            //desktop.enableServerPush(false);
            logger.debug("TIMER FERTIG!");

            // Import abgeschlossen
            timer.cancel();
          }
          else
          {
            logger.debug("ON EVENT!");

            ImportCodeSystemStatusRequestType request = new ImportCodeSystemStatusRequestType();
            request.setLoginToken(sessionId);
            ImportCodeSystemStatusResponse.Return response = WebServiceHelper.importCodeSystemStatus(request);

            logger.debug("Total: " + response.getTotalCount());
            logger.debug("Current: " + response.getCurrentIndex());
            int index = response.getCurrentIndex();
            int total = response.getTotalCount();

            if (importWS == null || importWS.isCancelled() || importWS.isDone())
            {
            }
            else
            {
              String msg = "";

              if (total > 0)
              {
                int prozent = (index * 100) / total;
                progress.setValue(prozent);
                msg = index + "/" + total;

              }
              else
              {
                progress.setValue(0);
                msg = index + "/unbekannt";
              }
              
              if(lastValue > 0)
              {
                int diff = index - lastValue;
                int countPerMinutes = diff * 6;
                if(countPerMinutes > 0)
                {
                  msg += " (" + countPerMinutes + "/min)";
                }
              }
              
              lastValue = index;
              

              Executions.schedule(desktop, el, new Event("updateStatus", null, msg));
            }
          }
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    },
            10000, 10000); // 1. mal nach 1 Minute, dann alle 2 Stunden

    importRunning = true;
    importWS = (Response<ImportCodeSystemResponse>) WebServiceHelper.importCodeSystemAsync(request, this);
//importWS.getContext()
    return true;
  }

  public void cancelImport()
  {
    if (importWS != null)
    {
      try
      {
        ImportCodeSystemCancelRequestType request = new ImportCodeSystemCancelRequestType();
        request.setLoginToken(SessionHelper.getSessionId());

        ImportCodeSystemCancelResponseType response = WebServiceHelper.importCodeSystemCancel(request);

        logger.debug("Response: " + response.getReturnInfos().getMessage());

        importRunning = false;
        importWS.cancel(true);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    final Progressmeter progress = (Progressmeter) window.getFellow("progress");
    progress.setVisible(false);

    ((Button) window.getFellow("buttonImport")).setDisabled(false);
    ((Button) window.getFellow("buttonCancel")).setVisible(false);
  }

  public void handleResponse(Response<ImportCodeSystemResponse> res)
  {
    // Antwort des Import-Webservices
    importRunning = false;
    String msg = "";
    try
    {
      ImportCodeSystemResponse.Return response = res.get().getReturn();
      msg = response.getReturnInfos().getMessage();
      logger.debug("Return: " + msg);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    logger.debug("Import fertig, jetzt Komponenten verstecken");

    try
    {
      Executions.activate(window.getDesktop());

      Progressmeter progress = (Progressmeter) window.getFellow("progress");
      progress.setVisible(false);
      ((Button) window.getFellow("buttonImport")).setDisabled(false);
      ((Button) window.getFellow("buttonCancel")).setVisible(false);
      ((Label) window.getFellow("labelImportStatus")).setValue(msg);

      Executions.deactivate(window.getDesktop());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void onEvent(Event event) throws Exception
  {
    if (importRunning == false)
      return;

    // In this part of code the ThreadLocals ARE available
    // Do something with result. You can touch any ZK stuff freely, just like when a normal event is posted.
    try
    {
      logger.debug("Event: " + event.getName());
      String message = event.getData().toString();

      if (event.getName().equals("end"))
      {
        Progressmeter progress = (Progressmeter) window.getFellow("progress");
        progress.setVisible(false);

        ((Button) window.getFellow("buttonImport")).setDisabled(false);
        ((Button) window.getFellow("buttonCancel")).setVisible(false);

        ((Label) window.getFellow("labelImportStatus")).setValue(message);
      }
      else
      {
        logger.debug("updateStatus: " + message);
        ((Label) window.getFellow("labelImportStatus")).setValue(message);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
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
