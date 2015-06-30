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
package de.fhdo.collaboration.desktop;

import de.fhdo.collaboration.db.Definitions;
import de.fhdo.collaboration.db.DomainHelper;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussion;
import de.fhdo.collaboration.db.classes.DomainValue;
import de.fhdo.collaboration.db.classes.File;
import de.fhdo.collaboration.db.classes.Link;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.FileCopy;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class AttachmentDetails extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal updateListInterface;
  private Link link;
  private File file;
  private List<DomainValue> technicalTypeList;
  private DomainValue selectedTechnicalType;
  private List<DomainValue> mimeTypeList;
  private DomainValue selectedMimeType;
  boolean bearbeiten = false;
  private Proposal proposal;
  private Session hb_sessionS;
  //private ComboitemRenderer subtypRenderer;

  public AttachmentDetails()
  {
    Map args;
    
    try
    {
      hb_sessionS = HibernateUtil.getSessionFactory().openSession();
      logger.debug("AttachmentDetails() - Konstruktor");

      mimeTypeList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_MIME_TYPES);
      technicalTypeList = DomainHelper.getInstance().getDomainList(Definitions.DOMAINID_ATTACHMENT_TECHNICAL_TYPES);

      logger.debug("Anzahl tech Types: " + technicalTypeList.size());

      //selectedTechnicalType = new DomainValue();
      //selectedTechnicalType.setDomainValueId(Definitions.TECHNICALTYPE_DOCUMENT);

      if (technicalTypeList.size() > 0)
        selectedTechnicalType = technicalTypeList.get(0);

      //selectedLogicalType = new DomainValue();
      //selectedMimeType = new DomainValue();

      long linkId = ArgumentHelper.getWindowArgumentLong("link_id");

      if (linkId > 0)
      {
        
        //hb_session.getTransaction().begin();
        
        try
        {
          link = (Link) hb_sessionS.get(Link.class, linkId);
          proposal = link.getProposal();
          selectedTechnicalType = DomainHelper.getInstance().getDomainValue(
                  Definitions.DOMAINID_ATTACHMENT_TECHNICAL_TYPES, link.getLinkType().toString());

          selectedMimeType = DomainHelper.getInstance().getDomainValue(
                  Definitions.DOMAINID_MIME_TYPES, link.getMimeType());

          if (selectedMimeType != null)
            logger.debug("MimeType-Code: " + selectedMimeType.getCode());
          else
            logger.debug("MimeType-Code: null");
          
          //hb_session.getTransaction().commit();
        }
        catch (Exception e)
        {
            //hb_session.getTransaction().rollback();
            LoggingOutput.outputException(e, this);
        }

        bearbeiten = true;
      }
      else
      {
        link = new Link();

        link.setCollaborationuser(new Collaborationuser());
        link.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());
        
        // Verbindung zu Proposal oder Discussion herstellen
        long proposalId = ArgumentHelper.getWindowArgumentLong("proposal_id");
        long discussionId = ArgumentHelper.getWindowArgumentLong("discussion_id");
        
        
        //hb_session.getTransaction().begin();
        
        try{
            proposal =(Proposal)hb_sessionS.get(Proposal.class, proposalId);
        }
        catch (Exception e)
        {
            //hb_session.getTransaction().rollback();
            LoggingOutput.outputException(e, this);
        }

        if (proposalId > 0)
        {
          link.setProposal(new Proposal());
          link.getProposal().setId(proposalId);

          logger.debug("Link gehört zu Proposal mit ID: " + proposalId);
        }

        if (discussionId > 0)
        {
          link.setDiscussion(new Discussion());
          link.getDiscussion().setId(discussionId);

          logger.debug("Link gehört zu Discussion mit ID: " + discussionId);
        }

        bearbeiten = false;
      }

      if (selectedMimeType == null)
        selectedMimeType = new DomainValue();
        }
        catch (Exception e)
        {
      logger.error(e.getLocalizedMessage());
    }
  }

  public void showContent()
  {
    logger.debug("showContent()");

    // Felder anpassen
    Textbox tbContent = (Textbox) getFellow("tbContent");
    Label label = (Label) getFellow("labelText");

    if (selectedTechnicalType.getCode().equals("1"))
    {
      //tbContent.setDisabled(false);
      tbContent.setRows(3);
      label.setValue("Beschreibung:");

    }
    else if (selectedTechnicalType.getCode().equals("2"))
    {
      //tbContent.setDisabled(false);
      tbContent.setRows(3);
      label.setValue("Link:");
    }
    else if (selectedTechnicalType.getCode().equals("3"))
    {
      //tbContent.setDisabled(false);
      tbContent.setRows(18);
      label.setValue("Notiz:");
    }
    else
    {
      label.setValue("Text:");
    }

    /*if (selectedTechnicalType.getCode().equals("1"))
     {
     link.setFile(null);
     }*/

    Row row = (Row) getFellow("documentRow");
    row.setVisible(selectedTechnicalType.getCode().equals("1"));

    row = (Row) getFellow("mimeTypeRow");
    row.setVisible(selectedTechnicalType.getCode().equals("1"));


  }

  public void onOkClicked()
  {

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      //if(link.getAttachment() != null)
      //  logger.debug("Tech type: " + getAttachment().getAttachment().getTechnicalTypeCd());

      if (selectedTechnicalType != null)
      {
        //link.setTechnicalTypeCd(selectedTechnicalType.getDomainCode());
        String code = selectedTechnicalType.getCode();
        if (code != null && code.length() > 0)
        {
          link.setLinkType(Integer.parseInt(code));
        }
      }


      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      
      try
      {
        if (bearbeiten)
        {
          hb_session.merge(link);
          
          if(file != null)
          {
            file.setLinkId(link.getId());
            hb_session.merge(file);
          }
        }
        else
        {
          // Neuer Eintrag
          link.setInsertTs(new Date());
          hb_session.save(link);
          
          if(file != null)
          {
            logger.debug("Speicher neue Datei mit Link-ID: " + link.getId());
            file.setLinkId(link.getId());
            hb_session.save(file);
          }
          
          
        }
        
        //Benachrichtigung Benutzer
        ArrayList<Collaborationuser> completeUserList = new ArrayList<Collaborationuser>();

        //Lade alle Benutzer mit Privilegien auf Proposal
        String hqlPrivilegeUsers = "from Collaborationuser cu join fetch cu.privileges pri join fetch pri.proposal pro join fetch cu.organisation o where pro.id=:id";
        Query qPrivilegeUsers = hb_session.createQuery(hqlPrivilegeUsers);
        qPrivilegeUsers.setParameter("id", proposal.getId());
        List<Collaborationuser> privUserList = qPrivilegeUsers.list();

        for(Collaborationuser cu:privUserList){
            completeUserList.add(cu);
        }

        //Lade alle Diskussionsgruppen mit Privilegien auf Proposal
        String hqlPrivilegeGroups = "from Collaborationuser cu join fetch cu.discussiongroups dg join fetch dg.privileges pri join fetch pri.proposal pro where pro.id=:id";
        Query qPrivilegeGroups = hb_session.createQuery(hqlPrivilegeGroups);
        qPrivilegeGroups.setParameter("id", proposal.getId());
        List<Collaborationuser> privGroupList = qPrivilegeGroups.list();

        for(Collaborationuser cu:privGroupList){

            boolean doubleEntry = false;
            for(Collaborationuser cuI:completeUserList){

                if(cu.getId().equals(cuI.getId())){
                    doubleEntry = true;
                }
            }

            if(!doubleEntry){
                completeUserList.add(cu);
            }
        }

        ArrayList<String> mailAdr = new ArrayList<String>();
        for(Collaborationuser u:completeUserList){

            if(u.getSendMail() != null && u.getSendMail())
                mailAdr.add(u.getEmail());
        }
        String[] adr = new String[mailAdr.size()];
        for(int i = 0;i<adr.length;i++){

            adr[i]= mailAdr.get(i);
        }
        // TODO
//        Mail.sendMailAUT(adr, M_AUT.PROPOSAL_LINK_SUBJECT, M_AUT.getInstance().getProposalLinkChangeText(
//                    proposal.getVocabularyName(), 
//                    proposal.getContentType(),
//                    proposal.getDescription(),
//                    selectedTechnicalType.getDisplayText(),
//                    link.getDescription()));
        
        
        hb_session.getTransaction().commit();
        
        // Collaborationuser lesen und speichern in link, damit dieser in der Liste angezeigt werden kann
        link.setCollaborationuser((Collaborationuser) hb_sessionS.get(Collaborationuser.class, SessionHelper.getCollaborationUserID()));
      }
      catch (Exception e)
      {
        LoggingOutput.outputException(e, this);
        hb_session.getTransaction().rollback();
        
        // TODO Fehlermeldung anzeigen
      }
      finally
      {
        hb_session.close();
        
        if (getUpdateListInterface() != null)
          getUpdateListInterface().update(link, bearbeiten);
      }

      
    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in onOkClicked(): " + e.getMessage());
      hb_sessionS.close();
    }

    this.setVisible(false);
    this.detach();
    hb_sessionS.close();
    //de.fhdo.gui.patientrecord.modules.masterdata.CommunicationDetails cannot be cast to de.fhdo.gui.patientrecord.modules.masterdata.Mast

    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
    if(hb_sessionS != null)
        hb_sessionS.close();
  }

  public void selectFile(Event event) throws IOException
  {
    try
    {
      //Media[] media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", 1, 50, true);

      //UploadEvent ue = new UploadEvent(_zclass, this, meds)
      //Media media = Fileupload.get("Bitte wählen Sie ein Datei aus.", "Datei wählen", true);
      Media media = ((UploadEvent) event).getMedia();

      if (media != null)
      {
        File f = new File();

        if (media.isBinary())
        {
          logger.debug("media.isBinary()");

          if (media.inMemory())
          {
            logger.debug("media.getByteData()");

            f.setData(media.getByteData());
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

            f.setData(baos.toByteArray());
            baos.close();
          }
        }
        else
        {
          logger.debug("media.isBinary() is false");

          Reader reader = FileCopy.asReader(media);
          f.setData(reader.toString().getBytes());
        }

        file = f;
        
        
        //getAttachment().setFile(f);
        link.setContent(media.getName()); // Dateiname
        link.setMimeType(media.getContentType());

        logger.debug("ct: " + media.getContentType());
        logger.debug("format: " + media.getFormat());


        Textbox tb = (Textbox) getFellow("filename");
        tb.setValue(media.getName());

        Combobox cb = (Combobox) getFellow("cbMimeType");
        cb.setValue(media.getContentType());
      }
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
    }
  }

  public void afterCompose()
  {
    showContent();



    //de.fhdo.help.Help.getInstance().addHelpToWindow(this);
  }

  /**
   * @return the updateListInterface
   */
  public IUpdateModal getUpdateListInterface()
  {
    return updateListInterface;
  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdateModal updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }

  /**
   * @return the technicalTypeList
   */
  public List<DomainValue> getTechnicalTypeList()
  {
    return technicalTypeList;
  }

  /**
   * @param technicalTypeList the technicalTypeList to set
   */
  public void setTechnicalTypeList(List<DomainValue> technicalTypeList)
  {
    this.technicalTypeList = technicalTypeList;
  }

  /**
   * @return the selectedTechnicalType
   */
  public DomainValue getSelectedTechnicalType()
  {
    return selectedTechnicalType;
  }

  /**
   * @param selectedTechnicalType the selectedTechnicalType to set
   */
  public void setSelectedTechnicalType(DomainValue selectedTechnicalType)
  {
    this.selectedTechnicalType = selectedTechnicalType;
  }

  /**
   * @return the mimeTypeList
   */
  public List<DomainValue> getMimeTypeList()
  {
    return mimeTypeList;
  }

  /**
   * @param mimeTypeList the mimeTypeList to set
   */
  public void setMimeTypeList(List<DomainValue> mimeTypeList)
  {
    this.mimeTypeList = mimeTypeList;
  }

  /**
   * @return the selectedMimeType
   */
  public DomainValue getSelectedMimeType()
  {
    return selectedMimeType;
  }

  /**
   * @param selectedMimeType the selectedMimeType to set
   */
  public void setSelectedMimeType(DomainValue selectedMimeType)
  {
    this.selectedMimeType = selectedMimeType;
  }

  /**
   * @return the link
   */
  public Link getLink()
  {
    return link;
  }

  /**
   * @param link the link to set
   */
  public void setLink(Link link)
  {
    this.link = link;
  }
}
