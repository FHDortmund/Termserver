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
package de.fhdo.collaboration.desktop.proposal;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Rating;
import de.fhdo.collaboration.desktop.ProposalView;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.logging.LoggingOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zul.Button;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ProposalViewVote extends Window
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private ProposalView proposalView;
  private GenericList genericList;
  long proposalId;
  private boolean inDiscussion;

  public ProposalViewVote()
  {
    
    
  }

  private void loadData()
  {
    // Bestimmen, ob Vorschlag in Diskussion ist und Zeitraum eingehalten wird
    inDiscussion = ProposalHelper.isProposalInDiscussion(proposalView.getProposal());
    
    loadVoting();
    loadStatistics();
  }

  private void loadStatistics()
  {
    // TODO Rechte beachten (!!)
    // nur Inhaltsverwalter dürfen Statistiken sehen (!)
    // bzw. Personen, welche den 
    
    Map<Long, Collaborationuser> users = ProposalHelper.getAllUsersForProposal(proposalId);

    int anzahlTeilnehmer = users.size();
    int anzahlStimmen = 0;
    int anzahlAblehnungen = 0;
    int anzahlZustimmungen = 0;
    int anzahlZustimmungenModifikation = 0;
    
    String []stimmeHeader = new String[]{"Ablehnung", "Zustimmung", "Zustimmung mit Modifikation"};

    // Header für Liste
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Benutzer", 250, "", true, "String", true, false, false, false));
    header.add(new GenericListHeaderType("Stimme", 150, "", true, stimmeHeader, true, true, false, false));
    header.add(new GenericListHeaderType("Beschreibung", 0, "", false, "String", true, true, false, false));

    // Daten-Liste
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "from Rating r "
              + " join fetch r.collaborationuser cu"
              + " where proposalId=" + proposalId;

      List<Rating> list = hb_session.createQuery(hql).list();
      if (list != null && list.size() > 0)
      {
          Iterator<Rating> iter = list.iterator();
        while(iter.hasNext()){
          // Stimmen lesen und anzeigen
            Rating r = iter.next();

            if (r.getValue() == 1)
            {
              anzahlAblehnungen++;
              anzahlStimmen++;
            }
            else if (r.getValue() == 2)
            {
              anzahlZustimmungen++;
              anzahlStimmen++;
            }
            else if (r.getValue() == 3)
            {
              anzahlZustimmungenModifikation++;
              anzahlStimmen++;
            }

            if (r.getValue() > 0)
            {
              // Diese Stimme in die Liste eintragen
              dataList.add(createRatingRow(r));
            }
        }
      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
        //hb_session.getTransaction().rollback();
      LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session.close();
    }

    // Gesamt-Statistiken
    String prozentStimmen = "";
    String prozentAblehnung = "";
    String prozentZustimmung = "";
    String prozentZustimmungModifikation = "";
    
    if(anzahlTeilnehmer > 0)
    {
      prozentStimmen = " (" + (anzahlStimmen * 100 / anzahlTeilnehmer) + "%)";
    }
    
    if(anzahlStimmen > 0)
    {
      prozentAblehnung = " (" + (anzahlAblehnungen * 100 / anzahlStimmen) + "%)";
      prozentZustimmung = " (" + (anzahlZustimmungen * 100 / anzahlStimmen) + "%)";
      prozentZustimmungModifikation = " (" + (anzahlZustimmungenModifikation * 100 / anzahlStimmen) + "%)";
    }
    
    ((Label) getFellow("labelAnzTeilnehmer")).setValue("" + anzahlTeilnehmer);
    ((Label) getFellow("labelAnzStimmen")).setValue("" + anzahlStimmen + prozentStimmen);
    ((Label) getFellow("labelAblehnungen")).setValue("" + anzahlAblehnungen + prozentAblehnung);
    ((Label) getFellow("labelZustimmungen")).setValue("" + anzahlZustimmungen + prozentZustimmung);
    ((Label) getFellow("labelZustimmungenModifikation")).setValue("" + anzahlZustimmungenModifikation + prozentZustimmungModifikation);

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    //genericList.setListActions(this);
    genericList.setListHeader(header);
    genericList.setButton_new(false);
    genericList.setDataList(dataList);
    genericList.setShowCount(true);

  }
  
  private String getTextFromRating(Rating r)
  {
    String stimme = "";
    if (r.getValue() == 1)
      stimme = "Ablehnung";
    else if (r.getValue() == 2)
      stimme = "Zustimmung";
    else if (r.getValue() == 3)
      stimme = "Zustimmung mit Modifikation";
    return stimme;
  }

  private GenericListRowType createRatingRow(Rating r)
  {
    GenericListRowType row = new GenericListRowType();

    String stimme = getTextFromRating(r);
    

    // Dokument-Icon
    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(ProposalHelper.getNameReverseFull(r.getCollaborationuser()), false, "");  // TODO Dokument mit Download-Link anzeigen
    cells[1] = new GenericListCellType(stimme, false, "");
    cells[2] = new GenericListCellType(r.getText(), false, "");

    row.setData(r);
    row.setCells(cells);

    return row;
  }

  private void loadVoting()
  {
    boolean stimmeVorhanden = false;
    Textbox tb = (Textbox) getFellow("tbBegruendung");
    Radiogroup rg = (Radiogroup) getFellow("rgVote");

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "from Rating r "
              + " join fetch r.collaborationuser cu"
              + " where proposalId=" + proposalId
              + " and cu.id=" + SessionHelper.getCollaborationUserID();

      List<Rating> list = hb_session.createQuery(hql).list();
      if (list != null && list.size() > 0)
      {
        // Stimme lesen und anzeigen
        Rating r = list.get(0);

        tb.setText(r.getText());
        if (r.getValue() >= 0 && r.getValue() < 4)
        {
          rg.setSelectedIndex(r.getValue());
          stimmeVorhanden = true;
        }
        else
          rg.setSelectedIndex(0);
        
        // Ergebnis anzeigen
        ((Label)getFellow("labelStimmeErgebnis")).setValue(getTextFromRating(r));
      }

      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(e, this);
    }
    finally
    {
      hb_session.close();
    }
    
    ((Groupbox)getFellow("gb")).setVisible(inDiscussion);
    ((Groupbox)getFellow("gbStimmergebnis")).setVisible(inDiscussion == false && stimmeVorhanden);
    
    ((Vbox)getFellow("vboxAbstimmung")).setVisible(inDiscussion);
    ((Vbox)getFellow("vboxAbstimmungErgebnis")).setVisible(inDiscussion == false && stimmeVorhanden);
    ((Vbox)getFellow("vboxAbstimmungVorher")).setVisible(inDiscussion == false && stimmeVorhanden == false);
  }

  /**
   * Speichert die Abstimmung für den angemeldeten Benutzer
   */
  public void saveVoting()
  {
    Textbox tb = (Textbox) getFellow("tbBegruendung");
    Radiogroup rg = (Radiogroup) getFellow("rgVote");
    int index = rg.getSelectedIndex();
    Rating r = null;
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      String hql = "from Rating r "
              + " join fetch r.collaborationuser cu"
              + " where proposalId=" + proposalId
              + " and cu.id=" + SessionHelper.getCollaborationUserID();
      
      List<Rating> list = hb_session.createQuery(hql).list();
      if (list == null || list.size() == 0)
      {
        // Neuer Eintrag
        r = new Rating();
        r.setValue(index);

        r.setCollaborationuser(new Collaborationuser());
        r.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());

        r.setProposal(new Proposal());
        r.getProposal().setId(proposalId);

        r.setText(tb.getText());

        hb_session.save(r);
      }
      else
      {
        // Eintrag ändern
        r = list.get(0);

        r.setValue(index);
        r.setText(tb.getText());

        hb_session.update(r);
      }

      hb_session.getTransaction().commit();

      // Erfolg, Meldung ausgeben
      Messagebox.show("Ihre Stimme wurde erfolgreich gespeichert.", "Abstimmung", Messagebox.OK, Messagebox.INFORMATION);
      ((Button) getFellow("buttonSpeichern")).setDisabled(true);
      ((Textbox) getFellow("tbBegruendung")).setDisabled(true);

        ArrayList<Collaborationuser> completeUserList = new ArrayList<Collaborationuser>();
        
        Proposal prop = (Proposal)hb_session.get(Proposal.class, proposalId);
        Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, SessionHelper.getCollaborationUserID());

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

        // TODO
        Mail.sendMailAUT(adr, M_AUT.PROPOSAL_RATING_SUBJECT, M_AUT.getInstance().getProposalRatingText(
                prop.getObjectName(), 
                prop.getContentType(),
                prop.getDescription(),
                getTextFromRating(r),
                r.getText(),
                user.getFirstName() + " " + user.getName()));
      
      
      // Statistiken aktualisieren
      loadStatistics();
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
    }
  }

  /**
   * @return the proposalView
   */
  public ProposalView getProposalView()
  {
    return proposalView;
  }

  /**
   * @param proposalView the proposalView to set
   */
  public void setProposalView(ProposalView proposalView)
  {
    this.proposalView = proposalView;

    proposalId = proposalView.getProposal().getId();

    loadData();

    //initListLinks();
    //initListObjects();
  }

  /**
   * @return the inDiscussion
   */
  public boolean isInDiscussion()
  {
    return inDiscussion;
  }

  /**
   * @param inDiscussion the inDiscussion to set
   */
  public void setInDiscussion(boolean inDiscussion)
  {
    this.inDiscussion = inDiscussion;
  }

  
}
