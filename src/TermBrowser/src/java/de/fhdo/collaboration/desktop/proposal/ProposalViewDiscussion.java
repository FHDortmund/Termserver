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
import de.fhdo.collaboration.db.classes.Discussion;
import de.fhdo.collaboration.desktop.ProposalView;
import de.fhdo.collaboration.discussion.DiscussionEntry;
import de.fhdo.collaboration.discussion.DiscussionEntryDetails;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ProposalViewDiscussion extends Window implements IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private ProposalView proposalView;
  private GenericList genericList;
  private boolean inDiscussion;

  public ProposalViewDiscussion()
  {
  }

  private void initListDiscussion()
  {
    if (logger.isDebugEnabled())
      logger.debug("ProposalViewLinks: initListLinks()");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("", 0, "", false, "Object", true, false, false, false));
    /*header.add(new GenericListHeaderType("Datum", 130, "", true, "DateTime", true, true, false, false));
     header.add(new GenericListHeaderType("Typ", 120, "", true, DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_ATTACHMENT_TECHNICAL_TYPES), true, true, false, false));
     header.add(new GenericListHeaderType("Beschreibung/Link", 0, "", true, "String", true, true, false, false));
     header.add(new GenericListHeaderType("Dateiname", 300, "", true, "String", true, true, false, false));
     header.add(new GenericListHeaderType("Benutzer", 120, "", true, "String", true, true, false, false));*/

    // Bestimmen, ob Vorschlag in Diskussion ist und Zeitraum eingehalten wird
    inDiscussion = ProposalHelper.isProposalInDiscussion(proposalView.getProposal());
    
    // Daten laden
    long proposalId = proposalView.getProposal().getId();
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "select distinct d from Discussion d "
              + " join fetch d.collaborationuser cu"
              + " left join fetch d.quotesForDiscussionId q"
              + " left join fetch cu.organisation org"
              + " where proposalId=" + proposalId
              + " order by d.date desc";

      List<Discussion> list = hb_session.createQuery(hql).list();

      //List<AttachmentFileType>attList = new LinkedList<AttachmentFileType>();

      for (Discussion d : list)
      {
        GenericListRowType row = createDiscussionRow(d);
        dataList.add(row);
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

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.setListActions(this);
    genericList.setListHeader(header);
    genericList.setButton_new(inDiscussion);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setDataList(dataList);
    genericList.setShowCount(true);
    
    // Paging einschalten
    genericList.getListbox().setMold("paging");
    genericList.getListbox().setPagingPosition("both");
    genericList.getListbox().setPageSize(10);
    
    if(inDiscussion)
      genericList.getListbox().setEmptyMessage("Keine Diskussionseinträge vorhanden. Fügen Sie mit 'Neu...' welche hinzu.");
    else 
      genericList.getListbox().setEmptyMessage("Diskussion ist zurzeit nicht aktiv.");
    
  }

  private GenericListRowType createDiscussionRow(Discussion d)
  {
    GenericListRowType row = new GenericListRowType();

    logger.debug("createDiscussionRow");

    Listcell lc = new Listcell("");
    Window win = (Window) Executions.createComponents("/collaboration/discussion/discussionEntry.zul", null, null);
    DiscussionEntry winDE = (DiscussionEntry) win;
    winDE.setInDiscussion(inDiscussion);
    winDE.setDiscussion(d);
    winDE.setUpdateInterface(this);
    
    lc.getChildren().add(win);


    GenericListCellType[] cells = new GenericListCellType[1];
    //cells[0] = new GenericListCellType(d.getLongDescription(), false, ""); 
    cells[0] = new GenericListCellType(lc, false, "");

    row.setData(d);
    row.setCells(cells);

    return row;
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

    initListDiscussion();
  }

  public void onNewClicked(String id)
  {
    // Neuer Diskussionseintrag
    try
    {
      logger.debug("onNewClicked()");

      Map map = new HashMap();
      //map.put("discussion_id", this.discussion.getId());
      map.put("proposal_id", proposalView.getProposal().getId());
      map.put("winHeight", "300px");

      logger.debug("erstelle Fenster...");

      Window win = (Window) Executions.createComponents(
              "/collaboration/discussion/discussionEntryDetails.zul", null, map);

      ((DiscussionEntryDetails) win).setUpdateInterface(this);

      logger.debug("öffne Fenster...");
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + this.getClass().getName()
              + "': " + ex.getMessage());
    }

  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
  }

  public void onSelected(String id, Object data)
  {
  }

  public void update(Object o, boolean edited)
  {
    if(o != null && o instanceof Discussion)
    {
      if(edited == false)
      {
        // Neuer Diskussionseintrag
        GenericListRowType row = createDiscussionRow((Discussion)o);
        genericList.addEntry(row,0); // Letzter Eintrag immer an die oberste Stelle!
        
        // nach unten scrollen
        /*Listbox lb = genericList.getListbox();
        if(lb.getItemCount() > 0)
        {
          Clients.scrollIntoView(lb.getItems().get(lb.getItemCount() - 1));
        }*/
        //genericList.getListbox().set
      }
      
    }
  }
}
