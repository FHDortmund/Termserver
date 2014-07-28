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
package de.fhdo.collaboration.discussion;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Discussion;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Quote;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class DiscussionEntryDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Discussion discussion;
  private Discussion discussionQuoted;
  private boolean bearbeiten;
  private IUpdateModal updateInterface;
  private Session hb_sessionS;

  public DiscussionEntryDetails()
  {

    long discussionId = ArgumentHelper.getWindowArgumentLong("discussion_id");
    hb_sessionS = HibernateUtil.getSessionFactory().openSession();
    if (discussionId > 0)
    {
      
      //hb_session.getTransaction().begin();
      try
      {
        discussion = (Discussion) hb_sessionS.get(Discussion.class, discussionId);
        long collabUserId = discussion.getCollaborationuser().getId();
        logger.debug("collabUserId: " + collabUserId);
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
      discussion = new Discussion();

      discussion.setCollaborationuser(new Collaborationuser());
      discussion.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());

      // Verbindung zu Proposal oder Discussion herstellen
      long proposalId = ArgumentHelper.getWindowArgumentLong("proposal_id");

      if (proposalId > 0)
      {
        discussion.setProposal(new Proposal());
        discussion.getProposal().setId(proposalId);

        logger.debug("Discussion gehört zu Proposal mit ID: " + proposalId);
      }

      // Zitat einfügen
      long quotedDiscussionId = ArgumentHelper.getWindowArgumentLong("quoted_discussion_id");
      if (quotedDiscussionId > 0)
      {
        
        //hb_session.getTransaction().begin();
        try
        {
          discussionQuoted = (Discussion) hb_sessionS.get(Discussion.class, quotedDiscussionId);
          long collabUserId = discussionQuoted.getCollaborationuser().getId();
          logger.debug("collabUserId-Quoted: " + collabUserId);
          //hb_session.getTransaction().commit();
        }
        catch (Exception e)
        {
          //hb_session.getTransaction().rollback();
            LoggingOutput.outputException(e, this);
        }
        
      }




      bearbeiten = false;
    }

  }

  public void afterCompose()
  {
    ((Groupbox) getFellow("gbQuote")).setVisible(discussionQuoted != null);
    String winHeight = (String)ArgumentHelper.getWindowArgument("winHeight");
    this.setHeight(winHeight);
    this.invalidate();
  }

  public void onOkClicked()
  {

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      Quote quote = null;

      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        //CKeditor edit = (CKeditor)getFellow("editor");
        //discussion.setLongDescription(edit.getValue());

        if (bearbeiten)
        {
          discussion.setChanged(new Date());

          hb_session.merge(discussion);

          /*if(file != null)
           {
           file.setLinkId(link.getId());
           hb_session.merge(file);
           }*/
        }
        else
        {
          // Neuer Eintrag
          discussion.setDate(new Date());

          // Diskussionsnummer berechnen
          String SQL_QUERY = "select max(d.postNumber) from Discussion d where d.proposal.id=" + discussion.getProposal().getId();
          Query query = hb_session.createQuery(SQL_QUERY);
          List list = query.list();
          
          Integer maxNumber = null;
          
          if(!list.isEmpty()){
          
              maxNumber = (Integer) list.get(0);
          }
          
          if(maxNumber == null || maxNumber < 0)
            maxNumber = 0;
          discussion.setPostNumber(maxNumber + 1);

          // Speichern
          hb_session.save(discussion);

          if (discussionQuoted != null)
          {
            logger.debug("Zitat speichern, mit Disk-ID " + discussion.getId() + " und " + discussionQuoted.getId());
            // Zitat speichern
            quote = new Quote();
            quote.setDiscussionByDiscussionId(new Discussion());
            quote.setDiscussionByDiscussionIdQuoted(new Discussion());

            quote.getDiscussionByDiscussionId().setId(discussion.getId());
            quote.getDiscussionByDiscussionIdQuoted().setId(discussionQuoted.getId());

            // TODO nur bestimmten Text als Zitat speichern
            // quote.setText()

            hb_session.save(quote);
          }

          /*if(file != null)
           {
           logger.debug("Speicher neue Datei mit Link-ID: " + link.getId());
           file.setLinkId(link.getId());
           hb_session.save(file);
           }*/
        }
        
        hb_session.getTransaction().commit();

        if (bearbeiten == false)
        {
          // Zur Anzeige in der Liste
          discussion.setCollaborationuser((Collaborationuser) hb_sessionS.get(Collaborationuser.class, discussion.getCollaborationuser().getId()));
          if (quote != null)
          {
            quote.setDiscussionByDiscussionIdQuoted(discussionQuoted);
            discussion.setQuotesForDiscussionId(new HashSet<Quote>());
            discussion.getQuotesForDiscussionId().add(quote);
          }

          logger.debug("Neu geladen mit user-ID: " + discussion.getCollaborationuser().getId() + " und name: " + discussion.getCollaborationuser().getName());
        }
        
        ArrayList<Collaborationuser> completeUserList = new ArrayList<Collaborationuser>();
        
        Proposal prop = (Proposal)hb_session.get(Proposal.class, discussion.getProposal().getId());
        
        //Lade alle Benutzer mit Privilegien auf Proposal
        String hqlPrivilegeUsers = "from Collaborationuser cu join fetch cu.privileges pri join fetch pri.proposal pro join fetch cu.organisation o where pro.id=:id";
        Query qPrivilegeUsers = hb_session.createQuery(hqlPrivilegeUsers);
        qPrivilegeUsers.setParameter("id", prop.getId());
        List<Collaborationuser> privUserList = qPrivilegeUsers.list();

        for(Collaborationuser cu:privUserList){
            completeUserList.add(cu);
        }

        //Lade alle Diskussionsgruppen mit Privilegien auf Proposal
        String hqlPrivilegeGroups = "from Collaborationuser cu join fetch cu.discussiongroups dg join fetch dg.privileges pri join fetch pri.proposal pro where pro.id=:id";
        Query qPrivilegeGroups = hb_session.createQuery(hqlPrivilegeGroups);
        qPrivilegeGroups.setParameter("id", prop.getId());
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
        
        Mail.sendMailAUT(adr, M_AUT.PROPOSAL_DISCUSSION_SUBJECT, M_AUT.getInstance().getProposalDiscussionEntryText(
                prop.getVocabularyName(), 
                prop.getContentType(),
                prop.getDescription(),
                discussion.getLongDescription(),
                discussion.getCollaborationuser().getFirstName() + " " + discussion.getCollaborationuser().getName()));
        
        
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

        if (updateInterface != null)
          updateInterface.update(discussion, bearbeiten);
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
    if(hb_sessionS != null)
        hb_sessionS.close();
    //de.fhdo.gui.patientrecord.modules.masterdata.CommunicationDetails cannot be cast to de.fhdo.gui.patientrecord.modules.masterdata.Mast

    //Executions.getCurrent().setAttribute("contactPerson_controller", null);
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
    hb_sessionS.close();
  }

  /**
   * @return the discussion
   */
  public Discussion getDiscussion()
  {
    return discussion;
  }

  /**
   * @param discussion the discussion to set
   */
  public void setDiscussion(Discussion discussion)
  {
    this.discussion = discussion;
  }

  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }

  /**
   * @return the discussionQuoted
   */
  public Discussion getDiscussionQuoted()
  {
    return discussionQuoted;
  }

  /**
   * @param discussionQuoted the discussionQuoted to set
   */
  public void setDiscussionQuoted(Discussion discussionQuoted)
  {
    this.discussionQuoted = discussionQuoted;
  }
}
