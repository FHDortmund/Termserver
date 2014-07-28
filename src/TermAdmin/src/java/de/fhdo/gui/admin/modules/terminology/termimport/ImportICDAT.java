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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.administration.FilecontentListEntry;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.ws.soap.MTOMFeature;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner
 */
public class ImportICDAT extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  HashMap<Integer, byte[]> fileContentMap;
  GenericList genericListVocs;
  CodeSystem selectedCodeSystem;
  boolean fileKatalog = false;
  boolean fileDreiSteller = false;
  boolean fileUnterkapitel = false;
  boolean fileKapitel = false;
  

  public ImportICDAT()
  {
  }

  public void onDateinameSelect(Event event, int fileCode)
  {
    try
    {
      //Media[] media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", 1, 50, true);

      //UploadEvent ue = new UploadEvent(_zclass, this, meds)
      //Media media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", true);
      Media media = ((UploadEvent) event).getMedia();
      byte[] bytes = null;

      if (media != null)
      {
        
         if(media.getContentType().equals("text/xml") || media.getContentType().equals("application/ms-excel") || media.getContentType().equals("text/csv")){  
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
              //Reader reader = FileCopy.asReader(media);
              //bytes = reader.toString().getBytes();

            }
        }
        //getAttachment().setFile(f);
        //getAttachment().getAttachment().setFilename(media.getName());
        //getAttachment().getAttachment().setMimeTypeCd(media.getContentType());

        logger.debug("ct: " + media.getContentType());
        logger.debug("format: " + media.getFormat());
        logger.debug("byte-length: " + bytes.length);
        logger.debug("bytes: " + bytes);

        
        if(fileCode == 0){
            Textbox tb = (Textbox) getFellow("tbKatalog");
            tb.setValue(media.getName());
            fileKatalog = true;
            fileContentMap.put(fileCode, bytes);
        }else if(fileCode == 1){
            Textbox tb = (Textbox) getFellow("tbDreiSteller");
            tb.setValue(media.getName());
            fileDreiSteller = true;
            fileContentMap.put(fileCode, bytes);
        }else if(fileCode == 2){
            Textbox tb = (Textbox) getFellow("tbUnterkapitel");
            tb.setValue(media.getName());
            fileUnterkapitel = true;
            fileContentMap.put(fileCode, bytes);
        }else if(fileCode == 3){
            Textbox tb = (Textbox) getFellow("tbKapitel");
            tb.setValue(media.getName());
            fileKapitel = true;
            fileContentMap.put(fileCode, bytes);
        }
      }
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
    }
    
    showStatus();

  }

  private void fillVocabularyList()
  {
    try
    {
      // Header
      List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
      header.add(new GenericListHeaderType("ID", 60, "", true, "String", true, true, false, false));
      header.add(new GenericListHeaderType("Name", 0, "", true, "String", true, true, false, false));

      // Daten laden
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();

      List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
      try
      {
        String hql = "from CodeSystem order by name";
        List<CodeSystem> csList = hb_session.createQuery(hql).list();

        for (int i = 0; i < csList.size(); ++i)
        {
          CodeSystem cs = csList.get(i);
          GenericListRowType row = createRowFromCodesystem(cs);

          dataList.add(row);
        }

        //tx.commit();
      }
      catch (Exception e)
      {
        logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
      }
      finally
      {
        hb_session.close();
      }

      // Liste initialisieren
      Include inc = (Include) getFellow("incList");
      Window winGenericList = (Window) inc.getFellow("winGenericList");
      genericListVocs = (GenericList) winGenericList;
      //genericListVocs.setId("0");

      genericListVocs.setListActions(this);
      genericListVocs.setButton_new(false);
      genericListVocs.setButton_edit(false);
      genericListVocs.setButton_delete(false);
      genericListVocs.setListHeader(header);
      genericListVocs.setDataList(dataList);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }


  }

  private GenericListRowType createRowFromCodesystem(CodeSystem cs)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[2];
    cells[0] = new GenericListCellType(cs.getId(), false, "");
    cells[1] = new GenericListCellType(cs.getName(), false, "");

    row.setData(cs);
    row.setCells(cells);

    return row;
  }

  public void onSelected(String id, Object data)
  {
    if (data != null)
    {
      if (data instanceof de.fhdo.list.GenericListRowType)
      {
        de.fhdo.list.GenericListRowType row = (de.fhdo.list.GenericListRowType) data;
        selectedCodeSystem = (CodeSystem) row.getData();
        logger.debug("Selected Codesystem: " + selectedCodeSystem.getName());

        showStatus();
      }
      else if (data instanceof CodeSystem)
      {
        selectedCodeSystem = (CodeSystem) data;
        logger.debug("Selected Codesystem: " + selectedCodeSystem.getName());

        showStatus();
      }
      else
        logger.debug("data: " + data.getClass().getCanonicalName());
    }

  }

  public void onNewClicked(String id)
  {
    ((Label) getFellow("labelImportStatus")).setValue("Bitte legen sie ein neues CodeSystem über den Terminologie-Browser an!");
  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
  }

  private void showStatus()
  {
    String s = "";
    
    if(!fileKatalog || !fileDreiSteller || !fileUnterkapitel || !fileKapitel){
    
        s += "Bitte wählen sie die Dateien aus.";
    }
        
    if (selectedCodeSystem == null)
    {
      s += "\nBitte wählen Sie ein Codesystem aus.";
    }

    ((Label) getFellow("labelImportStatus")).setValue(s);
    ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
  }

  public void startImport()
  {
      
    if(((Textbox) getFellow("tbVokabularVersion")).getText().equals("")){
        Messagebox.show("Bitte geben sie eine Versionsbezeichnung ein.", "Information", Messagebox.OK, Messagebox.INFORMATION);
    }else{
        ((Button) getFellow("buttonImport")).setDisabled(true);
        ((Button) getFellow("buttonCancel")).setVisible(true);

        String session_id = SessionHelper.getValue("session_id").toString();

        String vokVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();

        String msg = "";
        Progressmeter progress = (Progressmeter) getFellow("progress");

        // Vok-Version anlegen
        /*CreateCodeSystemRequestType ccsRequest = new CreateCodeSystemRequestType();
        ccsRequest.setLogin(new de.fhdo.terminologie.ws.authoring.LoginType());
        ccsRequest.getLogin().setSessionID("" + session_id);

        ccsRequest.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
        //ccsRequest.setCodeSystem(selectedCodeSystem);
        ccsRequest.getCodeSystem().setId(selectedCodeSystem.getId());
        ccsRequest.getCodeSystem().setName(selectedCodeSystem.getName());
        CodeSystemVersion csv = new CodeSystemVersion();
        csv.setName(vokVersion);
        ccsRequest.getCodeSystem().getCodeSystemVersions().add(csv);

        CreateCodeSystemResponse.Return ccsResponse = createCodeSystem(ccsRequest);

        if (ccsResponse.getReturnInfos().getStatus() == Status.OK)*/

          progress.setVisible(true);

          de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
          de.fhdo.terminologie.ws.administration.Administration port = service.getAdministrationPort(new MTOMFeature(true));

          // Login
          ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
          request.setLoginToken(SessionHelper.getSessionId());
          //logger.debug("Session-ID: ");

          // Codesystem
          request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
          request.getCodeSystem().setId(selectedCodeSystem.getId());
          request.getCodeSystem().setName(selectedCodeSystem.getName());
          //request.getCodeSystem().setName("ICD-10 BMG");
          
          CodeSystemVersion csv = new CodeSystemVersion();
          csv.setName(vokVersion);
          request.getCodeSystem().getCodeSystemVersions().add(csv);

          request.setImportInfos(new ImportType());
            
          request.getImportInfos().setFormatId(502l); // ICD10BMGAT
          
          for (Map.Entry pair : fileContentMap.entrySet()) {
              FilecontentListEntry entry = new FilecontentListEntry();
              entry.setCode((Integer)pair.getKey());
              entry.setContent((byte[])pair.getValue());
              request.getImportInfos().getFileContentList().add(entry);
          }
          
          Return response = port.importCodeSystem(request);
          //importWS = (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(request, this);

          msg = response.getReturnInfos().getMessage();
          logger.debug("Return: " + msg);


          //progress = (Progressmeter) getFellow("progress");


        /*else
        {
          logger.error(ccsResponse.getReturnInfos().getStatus());
          logger.error(ccsResponse.getReturnInfos().getMessage());
          msg = ccsResponse.getReturnInfos().getMessage();
        }*/

        progress.setVisible(false);
        ((Button) getFellow("buttonImport")).setDisabled(false);
        ((Button) getFellow("buttonCancel")).setVisible(false);
        ((Label) getFellow("labelImportStatus")).setValue(msg);


        //Return response = port.importCodeSystem(request);
        //logger.debug(response.getReturnInfos().getMessage());

        //if(response.getReturnInfos().set)

        //return port.importCodeSystem(parameter);

        //progress.setVisible(false); 
    }
  }

  public void afterCompose()
  {
    fileKatalog = false;
    fileDreiSteller = false;
    fileUnterkapitel = false;
    fileKapitel = false;
    fileContentMap = new HashMap<Integer, byte[]>();
    fillVocabularyList();
    showStatus();
  }

  private static CreateCodeSystemResponse.Return createCodeSystem(de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }
}
