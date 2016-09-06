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

import de.fhdo.helper.DomainHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zss.api.Importer;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CodesystemTranslations extends Window implements AfterCompose//, EventListener
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  boolean importRunning;
  boolean cancelImport = false;
  Window window;

  CodeSystem codeSystem;
  CodeSystemVersion codeSystemVersion;

  Map<String, DomainValue> languageMap;

  public CodesystemTranslations()
  {
    importRunning = false;
    window = this;

    Object selectedItem = SessionHelper.getValue("CS_SelectedItem");
    Object selectedItemVersion = SessionHelper.getValue("CS_SelectedItemVersion");

    codeSystem = (CodeSystem) selectedItem;
    codeSystemVersion = (CodeSystemVersion) selectedItemVersion;

  }

  public void afterCompose()
  {
    InitData();
  }

  private void InitData()
  {
    Spreadsheet ss = (Spreadsheet) getFellow("spreadsheet");
    //ss.setColumntitles("Test");
    //ss.setSrc(null);
    //ss.setSrc("/WEB-INF/blank2.xlsx");

    ss.setColumntitles(Labels.getLabel("code") + "," + Labels.getLabel("translation") + "," + Labels.getLabel("languageCd"));
    ss.setColumnwidth(180);

  }

  public void startImport()
  {
    //Timer timer;

    cancelImport = false;

    logger.debug("startImport()");
    final Progressmeter progress = (Progressmeter) getFellow("progress");
    final Button buttonCancel = (Button) getFellow("buttonCancelImport");
    final Button buttonImport = (Button) getFellow("buttonImport");
    final North northLogs = (North) getFellow("northLogs");
    final Spreadsheet ss = (Spreadsheet) getFellow("spreadsheet");

    Textbox tb = (Textbox) getFellow("tbLogs");

    // enable server push (asynchron)
    final org.zkoss.zk.ui.Desktop desktop = Executions.getCurrent().getDesktop();
    if (desktop.isServerPushEnabled() == false)
      desktop.enableServerPush(true);
    //final EventListener el = this;

    tb.setText("");

    // set buttons
    progress.setVisible(true);
    progress.setValue(0);
    buttonCancel.setDisabled(false);
    buttonCancel.setVisible(true);
    buttonImport.setDisabled(true);
    northLogs.setVisible(true);

    Thread t = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          languageMap = DomainHelper.getInstance().getDomainMap(Definitions.DOMAINID_ISO_639_1_LANGUACECODES);

          int rowsSuccess = 0;
          int rowsError = 0;

          int firstRow = ss.getBook().getSheetAt(0).getFirstRow();
          int lastRow = ss.getBook().getSheetAt(0).getLastRow() + 1;
          int countRows = lastRow - firstRow;

          logger.debug("firstRow: " + firstRow);
          logger.debug("lastRow: " + lastRow);
          logger.debug("countRows: " + countRows);

          Sheet sheet = ss.getBook().getSheetAt(0);
          SSheet ssheet = (SSheet) sheet.getInternalSheet();
          Executions.activate(desktop);

          Session hb_session = HibernateUtil.getSessionFactory().openSession();
          try
          {
            hb_session.getTransaction().begin();

            CodeSystemVersion csv_db = (CodeSystemVersion) hb_session.get(CodeSystemVersion.class, codeSystemVersion.getVersionId());
            String availableLanguages = "";
            if (csv_db.getAvailableLanguages() != null)
              availableLanguages = csv_db.getAvailableLanguages();

            // iterate all rows in the sheet
            for (int i = firstRow; i < lastRow; ++i)
            {
              logger.debug("import row " + i);

              SCell cell = ssheet.getCell(i, 0);

              String code = getCellValue(cell);

              String translation = getCellValue(ssheet.getCell(i, 1));
              String languageCd = getCellValue(ssheet.getCell(i, 2));

              logger.debug("code: " + code + ", translation: " + translation + ", languageCd: " + languageCd);

              // show progress
              if (i % 5 == 0)
              {
                progress.setValue((i - firstRow) * 100 / (countRows));
              }

              // import row
              String msg = importTranslationForCode(hb_session, code, translation, languageCd);
              if (msg.length() == 0)
              {
                rowsSuccess++;

                // check, if languageCd is in available languages for codesystem version
                if (availableLanguages.contains(languageCd) == false)
                {
                  availableLanguages += (availableLanguages.length() > 0 ? ";" : "") + languageCd;
                }
              }
              else
              {
                rowsError++;
                addLogMessage("[line " + (i + 1) + "] " + msg);
              }

              if (cancelImport)
              {
                // cancel button pressed
                addLogMessage("import cancelled");
                return;
              }
            }

            if (rowsError == 0)
            {
              // insert language(s) to available languages to code system version
              if (availableLanguages.equals(csv_db.getAvailableLanguages()) == false)
              {
                // available languages changed, update in database
                csv_db.setAvailableLanguages(availableLanguages);
                hb_session.update(csv_db);
              }

              // commit to database
              hb_session.getTransaction().commit();
            }

          }
          catch (Exception ex)
          {
            addLogMessage("import failure: " + ex.getLocalizedMessage());

            LoggingOutput.outputException(ex, this);
          }
          finally
          {
            hb_session.close();
          }

          //Executions.deactivate(desktop);
          // import end
          logger.debug("end of import");
          //Executions.activate(desktop);
          if (rowsError > 0 || rowsSuccess > 0)
          {
            if (rowsError > 0)
            {
              addLogMessage("import finished with errors, rows success: " + rowsSuccess + ", rows error: " + rowsError);
              addLogMessage("DATA NOT IMPORTED");
            }
            else
            {
              addLogMessage("import finished, rows success: " + rowsSuccess);
            }
          }

          progress.setVisible(false);
          buttonCancel.setVisible(false);
          buttonImport.setDisabled(false);
          northLogs.setVisible(true);

          Executions.deactivate(desktop);
        }
        catch (Exception ex)
        {
          // TODO Ausgabe
          LoggingOutput.outputException(ex, this);
        }

      }
    });

    importRunning = true;
    t.start();
  }

  private String getCellValue(SCell cell)
  {
    if(cell == null)
      return "";
    
    //logger.debug("cell1: " + cell.getFormulaValue());
    //logger.debug("cell2: " + cell.getReferenceString());
    
    //logger.debug("cell3: " + cell.getStringValue());
    //logger.debug("cell4: " + cell.getRichTextValue());
    //logger.debug("cell5: " + cell.getValue());
    
    
    //return cell.getReferenceString();
    //cell.setCellStyle(CellStyle.);
    if(cell.getType() == SCell.CellType.STRING)
      return cell.getStringValue();
    else if(cell.getType() == SCell.CellType.NUMBER)
    {
      Double dbl = cell.getNumberValue();
      if(dbl != null)
      {
        return Long.toString(dbl.longValue());
      }
      //return String.valueOf(cell.getNumberValue().longValue());
    }
    else if(cell.getValue() != null)
      return String.valueOf(cell.getValue());
    return "";
  }

  private String importTranslationForCode(Session hb_session, String code, String translation, String languageCd)
  {
    String msg = "";

    // 1. check if languageCd exists
    if (languageMap.containsKey(languageCd) == false)
    {
      msg = "languageCd '" + languageCd + "' does not exist, please see domain for available language codes";
      return msg;
    }

    // 2. check if concept with given code exists
    String hql = "select distinct csc from CodeSystemConcept csc"
            + " join csc.codeSystemEntityVersion csev"
            + " join csev.codeSystemEntity cse"
            + " join cse.codeSystemVersionEntityMemberships csvem"
            + " left join fetch csc.codeSystemConceptTranslations csct"
            + " where codeSystemVersionId=" + codeSystemVersion.getVersionId()
            + " and csc.code=:code";

    logger.debug("HQL: " + hql);
    Query q = hb_session.createQuery(hql);
    q.setString("code", code);
    List<CodeSystemConcept> cscList = q.list();

    logger.debug("import translation, list size: " + cscList.size());

    if (cscList == null || cscList.size() == 0)
    {
      // no match
      msg = "code '" + code + "' not found for selected code system version";
    }
    else
    {

      // 3. check if translation for given languageCd exists
      CodeSystemConcept csc = cscList.get(0);
      CodeSystemConceptTranslation csc_translation = null;

      for (CodeSystemConceptTranslation csct : csc.getCodeSystemConceptTranslations())
      {
        if (csct.getLanguageCd() != null && csct.getLanguageCd().equals(languageCd))
        {
          csc_translation = csct;
          break;
        }
      }

      if (csc_translation == null)
      {
        // translation does not exist
        logger.debug("translation does not exist, create new");
        csc_translation = new CodeSystemConceptTranslation();
        csc_translation.setCodeSystemConcept(csc);
      }

      // set translation designation
      csc_translation.setLanguageCd(languageCd);
      csc_translation.setTerm(translation);

      // save to database
      hb_session.saveOrUpdate(csc_translation);
    }

    return msg;
  }

  private void addLogMessage(String text)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    String newLine = sdf.format(new Date()) + ": " + text;

    Textbox tb = (Textbox) getFellow("tbLogs");
    tb.setText(newLine + "\n" + tb.getText());
  }

  public void cancelImport()
  {
    Button buttonCancel = (Button) getFellow("buttonCancelImport");
    buttonCancel.setDisabled(true);

    cancelImport = true;
  }

  public void openFile(Event event)
  {
    // open an excel sheet from local filesystem
    try
    {
      byte[] bytes = null;
      Media media = ((UploadEvent) event).getMedia();

      if (media != null)
      {
        if (media.getContentType().equals("application/ms-excel") || media.getContentType().equals("text/csv")
                || media.getContentType().equals("application/vnd.ms-excel")
                || media.getContentType().contains("excel")
                || media.getContentType().contains("csv")
                || media.getContentType().contains("text/plain")
                || media.getContentType().contains("sheet")
                || media.getContentType().equals("application/x-zip-compressed")
                || media.getContentType().contains("zip"))
        {
          if (media.isBinary())
          {
            logger.debug("media.isBinary()");

            if (media.inMemory())
            {
              logger.debug("media.getByteData()");

              bytes = media.getByteData();
            }
            else
            {
              logger.debug("media.getStreamData()");

              InputStream input = media.getStreamData();
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              int bytesRead;
              byte[] tempBuffer = new byte[8192 * 2];
              while ((bytesRead = input.read(tempBuffer)) != -1)
              {
                baos.write(tempBuffer, 0, bytesRead);
              }

              bytes = baos.toByteArray();
              baos.close();
            }
          }
          else
          {
            logger.debug("media.isBinary() is false");
            bytes = media.getStringData().getBytes("UTF-8");
          }
        }
        else
        {
          Messagebox.show(Labels.getLabel("foundNotSupportedDatatype") + ": " + media.getContentType());
        }

        logger.debug("ct: " + media.getContentType());
        String format = media.getFormat();
        logger.debug("format: " + format);

        if (bytes != null)
        {
          logger.debug("byte-length: " + bytes.length);
        }

        // load data into sheet
        InputStream input = new ByteArrayInputStream(bytes);
        Spreadsheet ss = (Spreadsheet) getFellow("spreadsheet");

        Importer importer = Importers.getImporter();
        Book book = importer.imports(input, "translation");
        ss.setBook(book);

        InitData();
      }
    }
    catch (Exception ex)
    {
      Messagebox.show(Labels.getLabel("docLoadFailure") + ": " + ex.getMessage());
      LoggingOutput.outputException(ex, this);
    }
  }

//  public void onEvent(Event event) throws Exception
//  {
//    if (importRunning == false)
//      return;
//
//    // In this part of code the ThreadLocals ARE available
//    // Do something with result. You can touch any ZK stuff freely, just like when a normal event is posted.
//    try
//    {
//      logger.debug("Event: " + event.getName());
//      String message = event.getData().toString();
//
//      if (event.getName().equals("end"))
//      {
//        Progressmeter progress = (Progressmeter) window.getFellow("progress");
//        progress.setVisible(false);
//
//        ((Button) window.getFellow("buttonImport")).setDisabled(false);
//        ((Button) window.getFellow("buttonCancel")).setVisible(false);
//
//        ((Label) window.getFellow("labelImportStatus")).setValue(message);
//      }
//      else
//      {
//        logger.debug("updateStatus: " + message);
//        ((Label) window.getFellow("labelImportStatus")).setValue(message);
//      }
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
//  }
}
