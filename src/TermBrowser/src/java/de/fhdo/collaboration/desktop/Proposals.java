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
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.helper.CollaborationuserHelper;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.helper.ParameterHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.logging.LoggingOutput;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.Session;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Proposals extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  MODES mode;
  private enum MODES {ALL, MINE}

  public Proposals()
  {
    // init mode
    String s_mode = ParameterHelper.getString("mode");
    if(s_mode.equalsIgnoreCase("mine"))
    {
      mode = MODES.MINE;
    }
    else
    {
      mode = MODES.ALL;  // default, show all proposals
    }
    
    logger.debug("Proposals() - Mode: " + mode.name());
    
    
    
  }

  public void afterCompose()
  {
    initList();
  }
  
  
  private void initList()
  {
    logger.debug("Proposals(): initList()");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Terminologie", 225, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Vorschlag", 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Typ", 140, "", true, DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_PROPOSAL_TYPES), true, true, false, false));
    header.add(new GenericListHeaderType("Status", 120, "", true, ProposalStatus.getInstance().getHeaderFilter(), true, true, false, false));
    header.add(new GenericListHeaderType("Datum", 110, "", true, "DateTime", true, true, false, false));
    header.add(new GenericListHeaderType("Disk.Ende", 100, "", true, "Date", true, true, false, false));
    header.add(new GenericListHeaderType("Rest", 80, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Autor", 140, "", true, "String", true, true, false, false));

    // Daten laden
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();

    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      List<Long> discussionGroups = CollaborationuserHelper.GetDiscussionGroupIDsForCurrentUser(hb_session);

      // PRIVILEGIEN
      if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))
      {
        String hql;
        hql = "select distinct p from Proposal p";

        // Sortierung
        hql += " order by p.created desc";

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        List<Proposal> proposalList = hb_session.createQuery(hql).list();

        for (int i = 0; i < proposalList.size(); ++i)
        {
          Proposal proposal = proposalList.get(i);
          GenericListRowType row = createRow(proposal);
          dataList.add(row);
        }

      }
      else
      {        //Jedesmal wenn jemand einen Vorschlag macht wird geprüft ob der Vorschlagende der TermVerwalter ist => wenn nicht werden
        //dem Inhaltsverwalter automatisch Privilegien zugesprochen => Auch bei der Zuweisung von Terminologien zu Inhaltsverwaltern im
        //Admin wird das nachträglich gecheckt!
        String hql;
        hql = "select distinct p from Proposal p join fetch p.collaborationuser u "
                + " left join p.privileges priv"
                // - alle Vorschläge anzeigen, die Privilegien mit User-ID haben
                + " where (priv.collaborationuser.id=" + SessionHelper.getCollaborationUserID();

        // - alle Vorschläge anzeigen, die Privilegien mit Discussion-ID haben
        if (discussionGroups != null && discussionGroups.size() > 0)
        {
          hql += " or priv.discussiongroup.id in (" + CollaborationuserHelper.ConvertDiscussionGroupListToCommaString(discussionGroups) + ")";
        }

        // - alle eigenen Vorschläge dürfen in Liste angezeigt werden (aber nicht Detail-Ansicht)
        hql += " or p.collaborationuser.id=" + SessionHelper.getCollaborationUserID();

        hql += ")";

        // Sortierung
        hql += " order by p.created desc";

        if (logger.isDebugEnabled())
          logger.debug("HQL: " + hql);

        List<Proposal> proposalList = hb_session.createQuery(hql).list();

        for (int i = 0; i < proposalList.size(); ++i)
        {
          Proposal proposal = proposalList.get(i);
          GenericListRowType row = createRow(proposal);
          dataList.add(row);
        }
      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
      LoggingOutput.outputException(e, this);
      //logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
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
    //genericList.setButton_new(true);
    genericList.setButton_edit(true);
    //genericList.setButton_delete(true);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);
  }

  private GenericListRowType createRow(Proposal proposal)
  {
    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[8];
    cells[0] = new GenericListCellType(proposal.getObjectName(), false, "");
    cells[1] = new GenericListCellType(proposal.getDescription(), false, "");
    cells[2] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_PROPOSAL_TYPES, proposal.getContentType()), false, "");
    cells[3] = new GenericListCellType(ProposalStatus.getInstance().getStatusStr(proposal.getStatus()), false, "");
    cells[4] = new GenericListCellType(proposal.getCreated(), false, "");
    cells[5] = new GenericListCellType(proposal.getValidTo(), false, "");
    cells[6] = new GenericListCellType(calculateRest(proposal.getValidTo()), false, "");
    cells[7] = new GenericListCellType(getListName(proposal.getCollaborationuser()), false, "");

    /*for(int i=0;i<pt.getProposalStatusChangeList().size();++i)
     {
     ProposalStatusChangeType psct = pt.getProposalStatusChangeList().get(i);
     if(psct.getProposalStatusTo() == pt.getStatus())
     {
     label.setToolTipText(psct.getReason());
     break;
     }
     }
     label.setText(StatusData.instance().GetStatusFromID(pt.getStatus()));
     werte[COLUMN_STATUS] = label;*/
    row.setData(proposal);
    row.setCells(cells);

    return row;
  }
  
  
  
}
