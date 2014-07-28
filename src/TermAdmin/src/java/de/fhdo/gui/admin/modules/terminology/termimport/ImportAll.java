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

import de.fhdo.helper.DomainHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import javax.xml.ws.Response;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.West;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportAll extends Window implements AfterCompose, IGenericListActions, javax.xml.ws.AsyncHandler<ImportCodeSystemResponse>, EventListener
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  final int KIND_CODESYSTEM = 0;
  final int KIND_VALUESET = 1;

  byte[] bytes;
  String format;
  GenericList genericList;
  GenericList genericListPreview;
  //Timer timer;
  boolean importRunning = false;
  IImport importClass = null;
  ValueSet selectedVS;
  CodeSystem selectedCS;

  public ImportAll()
  {
    //timer = new Timer();
    format = "";
  }

  public void afterCompose()
  {
    resetData();
    /*Include inc = (Include) getFellow("incList");
     Window winGenericList = (Window) inc.getFellow("winGenericList");
     genericList = (GenericList) winGenericList;*/
  }

  private void resetData()
  {
    // Vorschau etc.
    getFellow("westCS").setVisible(false);
    //getFellow("eastImport").setVisible(false);

    importClass = null;

    selectedVS = null;
    selectedCS = null;

    showStatus();
    //setStatus("Bitte wählen Sie eine Datei aus.", false);
  }

  public void onDateinameSelect(Event event)
  {
    try
    {
      resetData();

      bytes = null;
      //Media[] media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", 1, 50, true);

      //UploadEvent ue = new UploadEvent(_zclass, this, meds)
      //Media media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", true);
      Media media = ((UploadEvent) event).getMedia();

      if (media != null)
      {
        if (media.getContentType().equals("text/xml") || media.getContentType().equals("application/ms-excel") || media.getContentType().equals("text/csv")
                || media.getContentType().equals("application/vnd.ms-excel")
                || media.getContentType().contains("excel")
                || media.getContentType().contains("csv")
                || media.getContentType().contains("text/plain"))
        {
          if (media.isBinary())
          {
            logger.debug("media.isBinary()");

            if (media.inMemory())
            {
              logger.debug("media.getByteData()");

              bytes = media.getByteData();
              //f.setData(media.getByteData());
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
              //f.setData(baos.toByteArray());
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
          Messagebox.show("Nicht unterstützter Dateityp gefunden, Content Type: " + media.getContentType());
        }

        logger.debug("ct: " + media.getContentType());
        format = media.getFormat();
        logger.debug("format: " + format);

        if (bytes != null)
        {
          logger.debug("byte-length: " + bytes.length);
          //logger.debug("bytes: " + bytes);
        }

        Textbox tb = (Textbox) getFellow("textboxDateiname");
        tb.setValue(media.getName());

        preview();
      }
    }
    catch (Exception ex)
    {
      Messagebox.show("Fehler beim Laden eines Dokuments: " + ex.getMessage());
      //logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());

      LoggingOutput.outputException(ex, this);
    }

  }

  public void kindChanged()
  {
    int kind = ((Radiogroup) getFellow("rgKind")).getSelectedIndex();
    if (kind == KIND_CODESYSTEM)
    {
      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbFormat"), Definitions.DOMAINID_IMPORT_FORMATS_CS, null);
      fillVocabularyList();
    }
    else if (kind == KIND_VALUESET)
    {
      DomainHelper.getInstance().fillCombobox((Combobox) getFellow("cbFormat"), Definitions.DOMAINID_IMPORT_FORMATS_VS, null);
      fillValuesetList();
    }

    //getFellow("rgFormatCodesystem").setVisible(kind == KIND_CODESYSTEM);
    //getFellow("rgFormatValueset").setVisible(kind == KIND_VALUESET);
    preview();
  }

  public void preview()
  {
    logger.debug("preview()");

    // Interface bauen
    importClass = null;

    int kind = ((Radiogroup) getFellow("rgKind")).getSelectedIndex();
    String cd = DomainHelper.getInstance().getComboboxCd((Combobox) getFellow("cbFormat"));

    if (cd != null && cd.length() > 0)
    {
      long formatId = Long.parseLong(cd);

      if (kind == KIND_CODESYSTEM)
      {
        logger.debug("KIND_CODESYSTEM");
        final String TYPE_CLAML = "2";
        final String TYPE_CSV = "1";
        final String TYPE_ELGASVS = "8";
        final String TYPE_LOINC = "3";
        final String TYPE_KBV = "5";
        final String TYPE_ICD_AUSTRIA = "6";
        final String TYPE_LEITLINIEN_AUSTRIA = "7";

        if (cd.equals(TYPE_CSV))
        {
          importClass = new ImportCS_CSV(formatId);
        }
        else if (cd.equals(TYPE_CLAML))
        {
          importClass = new ImportClaML(formatId, this);

        }
        else if (cd.equals(TYPE_ELGASVS))
        {

        }
        else if (cd.equals(TYPE_LOINC))
        {
          importClass = new ImportCS_LOINC(formatId, this);
        }
        else if (cd.equals(TYPE_KBV))
        {
          importClass = new ImportCS_KBV(formatId);
        }
      }
      else if (kind == KIND_VALUESET)
      {
        logger.debug("KIND_VALUESET");

        final String TYPE_CSV = "1";
        final String TYPE_ELGASVS = "2";

        if (cd.equals(TYPE_CSV))
        {
          importClass = new ImportVS_CSV(formatId);
          logger.debug("ImportVS_CSV");
        }
        else if (cd.equals(TYPE_ELGASVS))
        {

        }
      }
    }

    showStatus();
    if (bytes == null)
    {
      return;
    }

    if (importClass == null)
    {
      //setStatus("Die Datei kann nicht verarbeitet werden. Bitte wählen Sie die Import-Art sowie das Format aus.", false);
    }
    else
    {
      //setStatus("OK", true);

      // Liste initialisieren
      Include inc = (Include) getFellow("incListPreview");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListPreview = (GenericList) winGenericList;

      // Vorschau geben
      importClass.preview(genericListPreview, bytes);
    }

  }

  private void fillValuesetList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      //header.add(new GenericListHeaderType("V-ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));
      //header.add(new GenericListHeaderType("Version", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
//        String hql = "select distinct vsv from ValueSetVersion vsv"
//                + " join fetch vsv.valueSet vs"
//                + " order by vs.name,vsv.name";
        String hql = "from ValueSet order by name";

        List<ValueSet> csList = hb_session.createQuery(hql).list();

        for (int i = 0; i < csList.size(); ++i)
        {
          GenericListRowType row = createRowFromValueset(csList.get(i));

          dataList.add(row);
        }

      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

      // Liste initialisieren
      Include inc = (Include) getFellow("incList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericList = (GenericList) winGenericList;

      genericList.setListActions(this);
      genericList.setButton_new(false);
      genericList.setButton_edit(false);
      genericList.setButton_delete(false);
      genericList.setListHeader(header);
      genericList.setDataList(dataList);
      genericList.setListId("1");

      ((West) getFellow("westCS")).setTitle("Valueset Auswahl");
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }

  private void fillVocabularyList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      //header.add(new GenericListHeaderType("V-ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));
      //header.add(new GenericListHeaderType("Version", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
//        String hql = "select distinct csv from CodeSystemVersion csv"
//                + " join fetch csv.codeSystem cs"
//                + " order by cs.name,csv.name";
        String hql = "from CodeSystem order by name";
        List<CodeSystem> csList = hb_session.createQuery(hql).list();

        for (int i = 0; i < csList.size(); ++i)
        {
          GenericListRowType row = createRowFromCodesystem(csList.get(i));

          dataList.add(row);
        }
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
      }
      finally
      {
        hb_session.close();
      }

      // Liste initialisieren
      Include inc = (Include) getFellow("incList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericList = (GenericList) winGenericList;

      genericList.setListActions(this);
      genericList.setButton_new(false);
      genericList.setButton_edit(false);
      genericList.setButton_delete(false);
      genericList.setListHeader(header);
      genericList.setDataList(dataList);
      genericList.setListId("0");

      //getFellow("westCS").setVisible(true);
      ((West) getFellow("westCS")).setTitle("Codesystem Auswahl");

    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

  }

  private GenericListRowType createRowFromCodesystem(CodeSystem cs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(cs.getId(), false, "");
    //cells[1] = new GenericListCellType(cs.getVersionId(), false, "");
    //cells[2] = new GenericListCellType(cs.getCodeSystem().getName(), false, "");
    cells[1] = new GenericListCellType(cs.getName(), false, "");

    row.setData(cs);
    row.setCells(cells);

    return row;
  }

  private GenericListRowType createRowFromValueset(ValueSet vs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(vs.getId(), false, "");
    //cells[1] = new GenericListCellType(vs.getVersionId(), false, "");
    //cells[2] = new GenericListCellType(vs.getValueSet().getName(), false, "");
    cells[1] = new GenericListCellType(vs.getName(), false, "");

    row.setData(vs);
    row.setCells(cells);

    return row;
  }

  public void showStatus()
  {
    logger.debug("showStatus()");

    boolean mustSpecifyCodesystem = true;
    boolean mustSpecifyCodesystemVersion = true;

    String s = "";
    if (bytes == null)
    {
      s += "Bitte wählen Sie eine Datei aus. ";
    }

    if (importClass == null)
    {
      logger.debug("ImportClass ist null");
      s += "Bitte wählen Sie die Import-Art sowie ein Format aus. ";
      getFellow("westCS").setVisible(false);
    }
    else
    {
      getFellow("westCS").setVisible(true);

      if (importClass.supportsFormat(format) == false)
      {
        s += "Das Format '" + format + "' wird für die Import-Art nicht unterstützt. ";
      }

      mustSpecifyCodesystem = importClass.mustSpecifyCodesystem();
      mustSpecifyCodesystemVersion = importClass.mustSpecifyCodesystemVersion();
    }

    if (mustSpecifyCodesystem)
    {
      if (selectedCS == null && selectedVS == null)
      {
        s += "Bitte wählen Sie ein Codesystem oder Valueset aus. ";
      }
    }
    else
    {
      getFellow("westCS").setVisible(false);
    }

    if (mustSpecifyCodesystemVersion)
    {
      String versionName = ((Textbox) getFellow("tbVersion")).getText();
      if (versionName == null || versionName.length() == 0)
      {
        s += "Bitte geben Sie einen Versions-Namen ein. ";
      }
      else
      {
        if (selectedVS != null)
        {
          ValueSetVersion vsv = new ValueSetVersion();
          vsv.setName(versionName);
          selectedVS.setValueSetVersions(new LinkedHashSet<ValueSetVersion>());
          selectedVS.getValueSetVersions().add(vsv);
        }
        if (selectedCS != null)
        {
          CodeSystemVersion csv = new CodeSystemVersion();
          csv.setName(versionName);
          selectedCS.setCodeSystemVersions(new LinkedHashSet<CodeSystemVersion>());
          selectedCS.getCodeSystemVersions().add(csv);
        }
      }

      getFellow("labelVersion").setVisible(true);
      getFellow("tbVersion").setVisible(true);
    }
    else
    {
      getFellow("labelVersion").setVisible(false);
      getFellow("tbVersion").setVisible(false);
    }

    ((Label) getFellow("labelStatus")).setValue(s);

    boolean importVisible = s.length() == 0;

    ((Button) getFellow("buttonImport")).setDisabled(!importVisible);
    //getFellow("eastImport").setVisible(importVisible);

  }

  public void startImport()
  {
    if (importClass != null && bytes != null)
    {
      ((Button) getFellow("buttonImport")).setDisabled(true);
      ((Button) getFellow("buttonCancel")).setVisible(true);

      final Progressmeter progress = (Progressmeter) getFellow("progress");
      final Label label = (Label) getFellow("labelStatus");
      progress.setVisible(true);

      if (importClass.startImport(bytes, progress, label, selectedCS, selectedVS) == false)
      {
        // wenn das Ergebnis false ist, dann ist es ein synchroner Aufruf
        ((Button) getFellow("buttonImport")).setDisabled(false);
        ((Button) getFellow("buttonCancel")).setVisible(false);
      }
    }
    else
    {
      Messagebox.show("Bitte füllen Sie alle Felder aus.");
    }
  }

  public void cancelImport()
  {
    if (importClass != null)
    {
      importClass.cancelImport();
    }
  }

  /*private void setStatuss(String s, boolean ok)
   {
    
   ((Label) getFellow("labelStatus")).setValue(s);
    
   getFellow("westCS").setVisible(ok);
   //((Button)getFellow("buttonImport")).setDisabled(!ok);
   }*/
  public void onNewClicked(String id)
  {
  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
  }

  public void onSelected(String id, Object data)
  {
    if (data != null)
    {
      if (data instanceof CodeSystem)
      {
        selectedCS = (CodeSystem) data;
        selectedVS = null;
        logger.debug("Selected CodeSystem: " + selectedCS.getName());

        showStatus();
      }
      else if (data instanceof ValueSet)
      {
        selectedVS = (ValueSet) data;
        selectedCS = null;
        logger.debug("Selected Valueset: " + selectedVS.getName());

        showStatus();
      }
      else
        logger.debug("data: " + data.getClass().getCanonicalName());
    }
  }

  public void handleResponse(Response<ImportCodeSystemResponse> res)
  {
  }

  public void onEvent(Event t) throws Exception
  {
  }

}
