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

import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.gui.admin.modules.collaboration.workflow.ProposalWorkflow;
import de.fhdo.gui.admin.modules.collaboration.workflow.ReturnType;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.ComparatorRowTypeName;
import de.fhdo.helper.HQLParameterHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse.Return;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.Status;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystemResponse;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations;
import de.fhdo.terminologie.ws.conceptassociation.ConceptAssociations_Service;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptassociation.CreateConceptAssociationResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.ws.soap.MTOMFeature;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner
 */
public class ImportCS_SVS extends Window implements AfterCompose, IGenericListActions
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  byte[] bytes;
  GenericList genericListVocs;
  CodeSystem selectedCodeSystem;
  Checkbox cbNewVoc;
  Textbox tbNewVoc;
  Textbox tbNewVocVersion;
  Textbox textboxDateiname;
  private String autoName = "";

  public ImportCS_SVS()
  {
  }

  public void onDateinameSelect(Event event)
  {
    try
    {
      bytes = null;
      //Media[] media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", 1, 50, true);

      //UploadEvent ue = new UploadEvent(_zclass, this, meds)
      //Media media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", true);
      Media media = ((UploadEvent) event).getMedia();

      if (media != null)
      {
        if (media.getContentType().equals("text/xml") || media.getContentType().equals("application/ms-excel") || media.getContentType().equals("text/csv"))
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

        Textbox tb = (Textbox) getFellow("textboxDateiname");
        tb.setValue(media.getName());
        String[] auto = (media.getName()).split("\\.");
        autoName = auto[0];
        cbNewVoc.setDisabled(false);
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
        if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER))
        {

          ArrayList<AssignedTerm> myTerms = AssignTermHelper.getUsersAssignedTerms();

          for (AssignedTerm at : myTerms)
          {

            if (at.getClassname().equals("CodeSystem"))
            {

              CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, at.getClassId());
              GenericListRowType row = createRowFromCodesystem(cs);
              dataList.add(row);
            }
          }
          Collections.sort(dataList, new ComparatorRowTypeName(true));
        }
        else
        {

          String hql = "from CodeSystem order by name";
          List<CodeSystem> csList = hb_session.createQuery(hql).list();

          for (int i = 0; i < csList.size(); ++i)
          {
            CodeSystem cs = csList.get(i);
            GenericListRowType row = createRowFromCodesystem(cs);

            dataList.add(row);
          }
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

  public void newVocChecked()
  {

    if (cbNewVoc.isChecked() && !textboxDateiname.getText().equals(""))
    {
      selectedCodeSystem = new CodeSystem();
      tbNewVocVersion.setDisabled(false);
      tbNewVoc.setDisabled(false);
      tbNewVoc.setText("");
      tbNewVoc.setText(autoName);
      tbNewVocVersion.setText("");
      tbNewVocVersion.setText(autoName);
      showStatus();
    }
    else
    {
      tbNewVocVersion.setDisabled(true);
      tbNewVoc.setDisabled(true);
      showStatus();
    }
  }

  public void onSelected(String id, Object data)
  {
    if (data != null && !cbNewVoc.isChecked())
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

    if (bytes == null)
    {
      s += "Bitte wählen Sie eine Datei aus.";
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
    boolean csvOnly = false;
    if (cbNewVoc.isChecked() && (tbNewVoc.getText().equals("") || tbNewVocVersion.getText().equals("")))
    {
      Messagebox.show("Bitte geben Sie Code System Bezeichnung und Code System Versionsbezeichnung ein!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
    }
    else
    {

      String vokVersion = ((Textbox) getFellow("tbVokabularVersion")).getText();
      if (!cbNewVoc.isChecked() && vokVersion.equals(""))
      {
        Messagebox.show("Bitte geben Sie eine Versionsbezeichnung ein!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
      }
      else
      {

        ((Button) getFellow("buttonImport")).setDisabled(true);
        ((Button) getFellow("buttonCancel")).setVisible(true);

        String session_id = SessionHelper.getValue("session_id").toString();

        String newVocVersion = ((Textbox) getFellow("tbNewVocVersion")).getText();

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
        //{
        progress.setVisible(true);

        de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
        de.fhdo.terminologie.ws.administration.Administration port = service.getAdministrationPort(new MTOMFeature(true));

        // Login
        ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
        request.setLoginToken(session_id);

        // Codesystem
        request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());

        if (cbNewVoc.isChecked())
        {
          request.getCodeSystem().setName(((Textbox) getFellow("tbNewVoc")).getText());
          csvOnly = false;
        }
        else
        {
          request.getCodeSystem().setId(selectedCodeSystem.getId());
          request.getCodeSystem().setName(selectedCodeSystem.getName());
          csvOnly = true;
        }

        CodeSystemVersion csv = new CodeSystemVersion();
        if (cbNewVoc.isChecked())
        {
          csv.setName(newVocVersion);
        }
        else
        {
          csv.setName(vokVersion);
        }

        request.getCodeSystem().getCodeSystemVersions().add(csv);

        // Claml-Datei
        request.setImportInfos(new ImportType());
        request.getImportInfos().setFormatId(235l); // SVS_ID
        request.getImportInfos().setFilecontent(bytes);
        if (csvOnly)
        {
          request.getImportInfos().setRole(CODES.ROLE_ADMIN);
        }
        else
        {
          request.getImportInfos().setRole(SessionHelper.getCollaborationUserRole());
        }
        Return response = port.importCodeSystem(request);
        //importWS = (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(request, this);

        msg = response.getReturnInfos().getMessage();
        logger.debug("Return: " + msg);
        ReturnType ret = null;
        String returnStr = "";

        //CodeSystemVersion
        if (response.getReturnInfos().getStatus().equals(Status.OK))
        {

          if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER) && !csvOnly)
          {

            //Erstellt ein neues Vokabular
            Proposal proposal = new Proposal();
            proposal.setVocabularyId(0l);  // wird nach Erstellen eingefügt
            proposal.setVocabularyName(response.getCodeSystem().getName());
            proposal.setContentType("vocabulary");
            proposal.setVocabularyNameTwo("CodeSystem");

            ret = ProposalWorkflow.getInstance().addProposal(proposal, response.getCodeSystem(), false);

            returnStr = msg + " | " + ret.getMessage();
          }
          else
          {
            returnStr = msg;
          }
        }
        else
        {
          returnStr = msg;
        }
              //progress = (Progressmeter) getFellow("progress");

            //}
            /*else
         {
         logger.error(ccsResponse.getReturnInfos().getStatus());
         logger.error(ccsResponse.getReturnInfos().getMessage());
         msg = ccsResponse.getReturnInfos().getMessage();
         }*/
        progress.setVisible(false);
        ((Button) getFellow("buttonImport")).setDisabled(false);
        ((Button) getFellow("buttonCancel")).setVisible(false);
        ((Label) getFellow("labelImportStatus")).setValue(returnStr);

            //Return response = port.importCodeSystem(request);
        //logger.debug(response.getReturnInfos().getMessage());
            //if(response.getReturnInfos().set)
            //return port.importCodeSystem(parameter);
        //progress.setVisible(false);
      }
    }
  }

  public void afterCompose()
  {
    cbNewVoc = (Checkbox) getFellow("cbNewVoc");
    tbNewVoc = (Textbox) getFellow("tbNewVoc");
    tbNewVocVersion = (Textbox) getFellow("tbNewVocVersion");
    textboxDateiname = (Textbox) getFellow("textboxDateiname");
    fillVocabularyList();
    showStatus();
  }

  private static CreateCodeSystemResponse.Return createCodeSystem(de.fhdo.terminologie.ws.authoring.CreateCodeSystemRequestType parameter)
  {
    de.fhdo.terminologie.ws.authoring.Authoring_Service service = new de.fhdo.terminologie.ws.authoring.Authoring_Service();
    de.fhdo.terminologie.ws.authoring.Authoring port = service.getAuthoringPort();
    return port.createCodeSystem(parameter);
  }

  //used for sorting LevelType Lists manually
  public void sort()
  {

    long listId = Long.valueOf(((Textbox) getFellow("tbSortListId")).getText());

    //get List of concepts or whatever
    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    String s = "";
    String session_id = "";
    try
    {
      session_id = SessionHelper.getValue("session_id").toString();
      String hql = "select distinct csev from CodeSystemEntityVersion csev";
      hql += " join fetch csev.codeSystemMetadataValues csmv";
      hql += " join fetch csmv.metadataParameter mp";
      hql += " join fetch csev.codeSystemEntity cse";
      hql += " join fetch cse.codeSystemVersionEntityMemberships csvem";
      hql += " join fetch csvem.codeSystemVersion csv";

      HQLParameterHelper parameterHelper = new HQLParameterHelper();
      parameterHelper.addParameter("csv.", "versionId", listId);

      // Parameter hinzufügen (immer mit AND verbunden)
      hql += parameterHelper.getWhere("");
      logger.debug("HQL: " + hql);

      // Query erstellen
      org.hibernate.Query q = hb_session.createQuery(hql);

      // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
      parameterHelper.applyParameter(q);

      List<CodeSystemEntityVersion> csevList = q.list();

      HashMap<Integer, CodeSystemEntityVersion> workList = new HashMap<Integer, CodeSystemEntityVersion>();
      ArrayList<Integer> sortList = new ArrayList<Integer>();

      Iterator<CodeSystemEntityVersion> iter = csevList.iterator();
      while (iter.hasNext())
      {
        boolean found = false;
        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) iter.next();

        //Get the Level
        int level = 0;
        Iterator<CodeSystemMetadataValue> iter1 = csev.getCodeSystemMetadataValues().iterator();
        while (iter1.hasNext())
        {
          CodeSystemMetadataValue csmv = (CodeSystemMetadataValue) iter1.next();
          if (csmv.getMetadataParameter().getParamName().equals("Level"))
          {

            String cleaned = csmv.getParameterValue();

            if (cleaned.contains(" "))
              cleaned = cleaned.replace(" ", "");

            level = Integer.valueOf(cleaned);
          }
        }

        if (level == 0)
        {

          CodeSystemVersionEntityMembershipId csvemId = null;
          Set<de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
          Iterator<de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership> it = csvemSet.iterator();
          while (it.hasNext())
          {

            de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership member = (de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership) it.next();
            if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                    && member.getCodeSystemVersion().getVersionId().equals(listId))
            {

              csvemId = member.getId();
            }
          }

          de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership c = (de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership) hb_session.get(de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership.class, csvemId);
          c.setIsMainClass(Boolean.TRUE);
          hb_session.update(c);

          workList.put(level, csev);
          sortList.add(level);
        }
        else
        {

          int size = sortList.size();
          int count = 0;
          while (!found)
          {

            if ((sortList.get(size - (1 + count)) - level) == -1)
            {

              //Setting MemberShip isMainClass false
              CodeSystemVersionEntityMembershipId csvemId = null;
              Set<de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership> csvemSet = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();
              Iterator<de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership> it = csvemSet.iterator();
              while (it.hasNext())
              {

                de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership member = (de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership) it.next();
                if (member.getCodeSystemEntity().getId().equals(csev.getCodeSystemEntity().getId())
                        && member.getCodeSystemVersion().getVersionId().equals(listId))
                {

                  csvemId = member.getId();
                }
              }

              de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership c = (de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership) hb_session.get(de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership.class, csvemId);
              c.setIsMainClass(Boolean.FALSE);
              hb_session.update(c);

              found = true;
              sortList.add(level);
              workList.put(level, csev);
              types.termserver.fhdo.de.CodeSystemEntityVersion csev1 = new types.termserver.fhdo.de.CodeSystemEntityVersion();
              types.termserver.fhdo.de.CodeSystemEntityVersion csev2 = new types.termserver.fhdo.de.CodeSystemEntityVersion();
              int assoKind = 2;
              int assoType = 4;
              CodeSystemEntityVersion csevPrev = workList.get(sortList.get(size - (1 + count)));

              csev1.setVersionId(csevPrev.getVersionId());
              csev2.setVersionId(csev.getVersionId());

              ConceptAssociations port_accociation = new ConceptAssociations_Service().getConceptAssociationsPort();
              CreateConceptAssociationRequestType parameterAssociation = new CreateConceptAssociationRequestType();
              CreateConceptAssociationResponse.Return responseAssociation = null;
              CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();

              if (csev1 != null && csev2 != null)
              {
                cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
                cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
                cseva.setAssociationKind(assoKind); // 1 = ontologisch, 2 = taxonomisch, 3 = cross mapping   
                cseva.setLeftId(csev1.getVersionId()); // immer linkes Element also csev1
                cseva.setAssociationType(new AssociationType()); // Assoziationen sind ja auch CSEs und hier muss die CSEVid der Assoziation angegben werden.
                cseva.getAssociationType().setCodeSystemEntityVersionId((long) assoType);

                // Login
                parameterAssociation.setLoginToken(session_id);

                // Association
                parameterAssociation.setCodeSystemEntityVersionAssociation(cseva);

                // Call WS and prevent loops in SOAP Message
                csev1.setCodeSystemEntity(null);
                csev2.setCodeSystemEntity(null);
                responseAssociation = port_accociation.createConceptAssociation(parameterAssociation);

                if (responseAssociation != null && responseAssociation.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.conceptassociation.Status.OK)
                  logger.error("ListSort: Error CreateConceptAssociation failure...");
              }
            }
            else
            {
              count++;
              found = false;
            }
          }
        }
      }

      hb_session.getTransaction().commit();
      s = "Success";
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();
      s = "ERROR";
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      hb_session.close();
    }
    ((Label) getFellow("labelImportStatus")).setValue(s);
  }
}
