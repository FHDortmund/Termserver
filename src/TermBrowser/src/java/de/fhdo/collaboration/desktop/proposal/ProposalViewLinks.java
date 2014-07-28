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

import de.fhdo.collaboration.db.Definitions;
import de.fhdo.collaboration.db.DomainHelper;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Link;
import de.fhdo.collaboration.desktop.AttachmentDetails;
import de.fhdo.collaboration.desktop.ProposalView;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.helper.OnDocumentClickListener;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.helper.AttachmentHelper;
import de.fhdo.helper.SessionHelper;
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
public class ProposalViewLinks extends Window implements IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private ProposalView proposalView;
  private GenericList genericList;
  private OnDocumentClickListener onDocumentClicked;

  public ProposalViewLinks()
  {
    onDocumentClicked = new OnDocumentClickListener(this);
  }

  public void initListLinks()
  {
    if (logger.isDebugEnabled())
      logger.debug("ProposalViewLinks: initListLinks()");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("", 30, "", false, "String", true, false, false, false));
    header.add(new GenericListHeaderType("Datum", 130, "", true, "DateTime", true, true, false, false));
    header.add(new GenericListHeaderType("Typ", 120, "", true, DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_ATTACHMENT_TECHNICAL_TYPES), true, true, false, false));
    header.add(new GenericListHeaderType("Beschreibung/Link", 0, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Dateiname", 300, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType("Benutzer", 120, "", true, "String", true, true, false, false));

    // Daten laden
    long proposalId = proposalView.getProposal().getId();
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "from Link link "
              + " join fetch link.collaborationuser cu"
              + " where proposalId=" + proposalId
              + " order by insert_ts desc ";

      List<Link>list = hb_session.createQuery(hql).list();
      
      //List<AttachmentFileType>attList = new LinkedList<AttachmentFileType>();
      
      for(Link link : list)
      {
        GenericListRowType row = createLinkRow(link);
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
    genericList.setButton_new(true);
    
    genericList.setDataList(dataList);
    genericList.setShowCount(true);
    
    if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN) ||
       (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER) && 
        AssignTermHelper.isUserAllowed(proposalView.getProposal().getVocabularyIdTwo(),proposalView.getProposal().getVocabularyNameTwo()))){
        genericList.setButton_edit(true);
        genericList.setButton_delete(true);
    }
  }

  private GenericListRowType createLinkRow(Link link)
  {
    GenericListRowType row = new GenericListRowType();
    
    // Dokument-Icon
    Listcell lc = new Listcell("");
    if(onDocumentClicked != null)
      AttachmentHelper.applyListcellIcon(lc, link, onDocumentClicked, false);
    
    GenericListCellType[] cells = new GenericListCellType[6];
    cells[0] = new GenericListCellType(lc, false, "");  // TODO Dokument mit Download-Link anzeigen
    cells[1] = new GenericListCellType(link.getInsertTs(), false, "");
    cells[2] = new GenericListCellType(DomainHelper.getInstance().getDomainValueDisplayText(Definitions.DOMAINID_ATTACHMENT_TECHNICAL_TYPES, "" + link.getLinkType()) , false, ""); 
    cells[3] = new GenericListCellType(link.getDescription(), false, "");
    cells[4] = new GenericListCellType(link.getContent(), false, "");
    cells[5] = new GenericListCellType(ProposalHelper.getNameShort(link.getCollaborationuser()), false, "");

    row.setData(link);
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

    initListLinks();
    //initListObjects();
  }

  public void onNewClicked(String id)
  {
    showAttachmentDetails(0);
  }

  public void onEditClicked(String id, Object data)
  {
    if(data != null && data instanceof Link)
    {
      Link link = (Link)data;
      showAttachmentDetails(link.getId());
    }
  }

  public void onDeleted(String id, Object data)
  {
    // TODO
  }

  public void onSelected(String id, Object data)
  {
  }
  
  private void showAttachmentDetails(long LinkId)
  {
    try
    {
      logger.debug("showAttachmentDetails()");

      Map domainValueMap = new HashMap();
      if (LinkId > 0)
        domainValueMap.put("link_id", LinkId);
      
      domainValueMap.put("proposal_id", proposalView.getProposal().getId());

      logger.debug("erstelle Fenster...");

      Window win = (Window) Executions.createComponents(
              "/collaboration/desktop/attachmentDetails.zul", null, domainValueMap);

      ((AttachmentDetails) win).setUpdateListInterface(this);

      logger.debug("öffne Fenster...");
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + this.getClass().getName()
              + "': " + ex.getMessage());
    }
  }

  public void update(Object o, boolean edited)
  {
    if(o != null)
    {
      if(o instanceof Link)
      {
        Link link = (Link)o;
        GenericListRowType row = createLinkRow(link);
        if(edited)
          genericList.updateEntry(row);
        else genericList.addEntry(row);
      }
    }
  }
}
