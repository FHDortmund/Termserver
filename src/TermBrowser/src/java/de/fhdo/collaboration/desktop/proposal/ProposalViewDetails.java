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

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.PO_CHANGE_TYPE;
import de.fhdo.collaboration.db.PO_CLASSNAME;
import de.fhdo.collaboration.db.TERMSERVER_STATUS;
import de.fhdo.collaboration.db.classes.AssignedTerm;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.collaboration.desktop.ProposalView;
import de.fhdo.collaboration.desktop.proposal.privilege.PrivilegeDetails;
import de.fhdo.collaboration.helper.AssignTermHelper;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.collaboration.helper.ProposalHelper;
import de.fhdo.collaboration.proposal.ProposalStatus;
import de.fhdo.collaboration.workflow.ReturnType;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.list.IGenericListActions;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptDetailsResponse;
import de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipResponse;
import de.fhdo.terminologie.ws.search.ReturnValueSetDetailsResponse;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class ProposalViewDetails extends Window implements IGenericListActions, IUpdateModal
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private ProposalView proposalView;
  private GenericList genericListStatus;

  public ProposalViewDetails()
  {
  }

  public void initListVerlauf()
  {
    if (logger.isDebugEnabled())
      logger.debug("ProposalViewDetails: initListVerlauf()");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Datum", 80, "", false, "Date", true, false, false, false));
    header.add(new GenericListHeaderType("Status", 0, "", false, "String", true, false, false, false));
    header.add(new GenericListHeaderType("", 36, "", false, "String", true, false, false, false));
    header.add(new GenericListHeaderType("Person", 140, "", false, "String", true, false, false, false));

    // Daten laden
    List<Proposalstatuschange> pscList = ProposalHelper.getStatusChangeList(proposalView.getProposal());
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    for (Proposalstatuschange psc : pscList)
    {
      GenericListRowType row = createStatusRow(psc);
      dataList.add(row);
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incListVerlauf");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericListStatus = (GenericList) winGenericList;

    //genericListStatus.setListActions(this);
    genericListStatus.setListHeader(header);
    genericListStatus.setDataList(dataList);
    genericListStatus.setListId("status");
    genericListStatus.setButton_new(false);
  }

  private GenericListRowType createStatusRow(final Proposalstatuschange psc)
  {
    GenericListRowType row = new GenericListRowType();

    Listcell lcNotiz = new Listcell();
    if (psc.getReason() != null && psc.getReason().length() > 0)
    {
      Image image = new Image();
      image.setSrc("/rsc/img/filetypes/note.png");
      image.setTooltiptext(psc.getReason());

      image.addEventListener(Events.ON_CLICK, new EventListener<Event>()
      {
        public void onEvent(Event t) throws Exception
        {
          Messagebox.show(psc.getReason());
        }
      });

      lcNotiz.appendChild(image);
    }

    GenericListCellType[] cells = new GenericListCellType[4];
    cells[0] = new GenericListCellType(psc.getChangeTimestamp(), false, "");
    cells[1] = new GenericListCellType(ProposalStatus.getInstance().getStatusStr(psc.getProposalStatusTo()), false, "");
    cells[2] = new GenericListCellType(lcNotiz, false, "");
    cells[3] = new GenericListCellType(ProposalHelper.getNameShort(psc.getCollaborationuser()), false, "");

    // TODO Reason als Tooltip oder Dokument (neue Spalte)

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
    row.setData(psc);
    row.setCells(cells);

    return row;
  }

  public void initListPrivilegien()
  {
    if (logger.isDebugEnabled())
      logger.debug("ProposalViewDetails: initListPrivilegien()");

    // Header
    /*TODO List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
     header.add(new GenericListHeaderType("", 30, "", false, "String", true, false, false, false));
     header.add(new GenericListHeaderType("Benutzer/Diskussiongruppe", 0, "", true, "String", true, false, false, false));
     header.add(new GenericListHeaderType("Organisation/Gruppenleiter", 0, "", true, "String", true, false, false, false));
    
     Session hb_session = HibernateUtil.getSessionFactory().openSession();
     //hb_session.getTransaction().begin();
     // Daten laden
     long proposalId = proposalView.getProposal().getId();
     Collaborationuser u = (Collaborationuser)hb_session.get(Collaborationuser.class, proposalView.getProposal().getCollaborationuser().getId());
    
     List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
     PrivilegienListData data = new PrivilegienListData();
     data.setDiscussiongroup(null); //Nur User
     data.setIsCreator(true);
     data.setUser(u);
    
     String hqlPriv = "from Privilege priv "
     + " left join fetch priv.collaborationuser cu"
     + " left join fetch cu.organisation"
     + " where proposalId=" + proposalId
     + " and cu.id=" + u.getId()
     + " order by cu.name ";

     List<Privilege> listPriv = hb_session.createQuery(hqlPriv).list();
      
     if(listPriv.size() == 1){
     data.setPrivilege(listPriv.get(0));
     }
      
     boolean sv = false;
     for(AssignedTerm at:u.getAssignedTerms()){
     if(at.getClassId().equals(proposalView.getProposal().getVocabularyIdTwo()) && at.getClassname().equals(proposalView.getProposal().getVocabularyNameTwo()))
     sv = true;
     }
      
     // Ersteller als 1. hinzufügen
     dataList.add(createPrivilegienRow(data,sv));

     // Benutzer und/oder Benutzergruppen hinzufügen
     try
     {
     String hql = "from Privilege priv "
     + " left join fetch priv.collaborationuser cu"
     + " left join fetch cu.organisation"
     + " left join fetch priv.discussiongroup dc"
     + " where proposalId=" + proposalId
     + " order by cu.name ";

     List<Privilege> list = hb_session.createQuery(hql).list();

     for (Privilege link : list)
     {
     if (link.getCollaborationuser() != null && link.getCollaborationuser().getId().longValue()
     != proposalView.getProposal().getCollaborationuser().getId().longValue())
     {
     PrivilegienListData d = new PrivilegienListData();
     d.setUser(link.getCollaborationuser()); //Nur User
     d.setPrivilege(link);
     d.setDiscussiongroup(null);
          
     if(d.getUser().getRoles().iterator().next().getName().equals(CODES.ROLE_ADMIN)){
     d.setIsAdmin(true);
     }else{d.setIsAdmin(false);}
     d.setIsCreator(false);
          
     boolean sV = false;
     Collaborationuser uL = (Collaborationuser)hb_session.get(Collaborationuser.class, d.getUser().getId());
     for(AssignedTerm at:uL.getAssignedTerms()){
     if(at.getClassId().equals(proposalView.getProposal().getVocabularyIdTwo()) && at.getClassname().equals(proposalView.getProposal().getVocabularyNameTwo()))
     sV = true;
     }
          
     GenericListRowType row = createPrivilegienRow(d,sV);
     dataList.add(row);
     }else if(link.getDiscussiongroup() != null){
        
     PrivilegienListData d = new PrivilegienListData();
     d.setDiscussiongroup(link.getDiscussiongroup());
     //Get DG Leader
     Collaborationuser user = (Collaborationuser)hb_session.get(Collaborationuser.class, link.getDiscussiongroup().getHead());
     d.setUser(user); 
     d.setPrivilege(link);
     d.setIsAdmin(false);
     d.setIsCreator(false);
            
     GenericListRowType row = createPrivilegienRow(d,false);
     dataList.add(row);
     }else{}
        
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
     Include inc = (Include) getFellow("incListPrivilegien");
     Window winGenericList = (Window) inc.getFellow("winGenericList");
     genericListStatus = (GenericList) winGenericList;

     //genericListStatus.setListActions(this);
     genericListStatus.setListHeader(header);
     genericListStatus.setDataList(dataList);
     genericListStatus.setListId("privilegien");
     genericListStatus.setListActions(this);
    
    
    
    
     if(SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN) ||
     (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER) && 
     AssignTermHelper.isUserAllowed(proposalView.getProposal().getVocabularyIdTwo(),proposalView.getProposal().getVocabularyNameTwo()))){
     genericListStatus.setButton_new(true);
     ((Button)genericListStatus.getFellow("buttonNew")).setLabel("Hinzufügen");
     genericListStatus.setButton_delete(true);
     ((Button)genericListStatus.getFellow("buttonDelete")).setLabel("Entfernen");
     }*/
    /*
     Set<Statusrel> statusChilds = ProposalStatus.getInstance().getStatusChilds(proposal.getStatus());

     if (logger.isDebugEnabled())
     logger.debug("Anzahl childs: " + statusChilds.size());

     for (final Statusrel child : statusChilds)
     {
     // Buttons nicht anzeigen oder deaktivieren bei fehlenden Rechten
     boolean allowed = ProposalStatus.getInstance().isUserAllowed(child, SessionHelper.getCollaborationUserID());

     Button button = new Button(child.getAction().getAction());
     button.setAutodisable("true");
     if (allowed)
     {
     button.setTooltiptext("Ändert den Status zu: " + child.getStatusByStatusIdTo().getStatus());
     }
     else
     {
     button.setDisabled(true);
     button.setTooltiptext("Sie besitzen nicht die nötigen Rechte, um diesen Status zu ändern");
     }
    
     genericListStatus.setButton_new(true);
     genericListStatus.setButton_edit(true);
    
     genericListStatus.setButton_new(false);
     genericListStatus.setButton_edit(false);
     */
  }

  private GenericListRowType createPrivilegienRow(PrivilegienListData data, boolean sv)
  {
    // Benutzer-Icon
    Listcell lc = new Listcell("");
    Image image = new Image();
    String f1 = "";
    String f2 = "";

    if (data.getDiscussiongroup() != null)
    { //Discussiongroup

      image.setSrc("/rsc/img/symbols/useraccounts.png");
      image.setTooltiptext("Gruppe");
      f1 = data.getDiscussiongroup().getName();
      f2 = data.getUser().getFirstName() + " " + data.getUser().getName();

    }
    else
    {                                  //Single User

      if (data.getIsAdmin())
      {
        image.setSrc("/rsc/img/symbols/user_admin_16x16.png");
        image.setTooltiptext(CODES.ROLE_ADMIN);
      }
      else if (data.getIsCreator())
      {
        image.setSrc("/rsc/img/symbols/user_admin_16x16.png");
        image.setTooltiptext("Ersteller");
      }
      else
      {
        image.setSrc("/rsc/img/symbols/user_16x16.png");
        image.setTooltiptext(CODES.ROLE_BENUTZER);
      }

      if (sv)
      {
        image.setSrc("/rsc/img/symbols/user_admin_16x16.png");
        image.setTooltiptext(CODES.ROLE_INHALTSVERWALTER);
      }

      f1 = data.getUser().getFirstName() + " " + data.getUser().getName();
      f2 = data.getUser().getOrganisation().getOrganisation();
    }

    lc.appendChild(image);

    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(lc, false, "");
    cells[1] = new GenericListCellType(f1, false, "");
    cells[2] = new GenericListCellType(f2, false, "");

    row.setData(data);
    row.setCells(cells);

    return row;
  }

  public void initListObjects()
  {
    if (logger.isDebugEnabled())
      logger.debug("ProposalViewDetails: initListObjects()");

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType("Aktion", 100, "", false, "String", true, false, false, false));
    header.add(new GenericListHeaderType("Typ", 150, "", false, "String", true, false, false, false));
    header.add(new GenericListHeaderType("Konzept/Objekt", 0, "", false, "String", true, false, false, false));
    header.add(new GenericListHeaderType("Status", 100, "", false, "String", true, false, false, false));

    // Daten laden
    Set<Proposalobject> poList = proposalView.getProposal().getProposalobjects();
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();

    for (Proposalobject po : poList)
    {
      GenericListRowType row = createObjectRow(po);
      dataList.add(row);
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incListObjects");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericListStatus = (GenericList) winGenericList;

    //genericListStatus.setListActions(this);
    genericListStatus.setListHeader(header);
    genericListStatus.setDataList(dataList);
    genericListStatus.setListId("object");
    genericListStatus.setButton_new(false);
  }

  private GenericListRowType createObjectRow(Proposalobject po)
  {
    // Status des Objektes aus Termserver abrufen
    logger.debug("Suche Details zu: " + po.getClassId());
    String objStatus = "";

    if (po.getClassname().equalsIgnoreCase("CodeSystemConcept"))
    {
      de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType request = new de.fhdo.terminologie.ws.search.ReturnConceptDetailsRequestType();
      request.setLoginToken(CollaborationSession.getInstance().getSessionID());
      request.setCodeSystemEntity(new CodeSystemEntity());
      CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
      csev.setVersionId(po.getClassId());
      request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);
      ReturnConceptDetailsResponse.Return response = WebServiceHelper.returnConceptDetails(request);
      logger.debug("Antwort returnConceptDetails: " + response.getReturnInfos().getMessage());
      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        if (response.getCodeSystemEntity().getCodeSystemEntityVersions() != null
            && response.getCodeSystemEntity().getCodeSystemEntityVersions().size() > 0)
        {
          int status = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getStatusVisibility();
          objStatus = TERMSERVER_STATUS.getString(status);
        }
      }
      else if (response.getReturnInfos().getStatus() == Status.FAILURE)
      {
        objStatus = "nicht vorhanden";
      }
    }
    else if (po.getClassname().equalsIgnoreCase("CodeSystemVersion"))
    {
      de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType request = new de.fhdo.terminologie.ws.search.ReturnCodeSystemDetailsRequestType();
      request.setLoginToken(CollaborationSession.getInstance().getSessionID());
      request.setCodeSystem(new CodeSystem());
      CodeSystemVersion csv = new CodeSystemVersion();
      csv.setVersionId(po.getClassId());
      request.getCodeSystem().getCodeSystemVersions().add(csv);

      ReturnCodeSystemDetailsResponse.Return response = WebServiceHelper.returnCodeSystemDetails(request);
      logger.debug("Antwort returnCodeSystemDetails: " + response.getReturnInfos().getMessage());
      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        if (response.getCodeSystem() != null
            && response.getCodeSystem().getCodeSystemVersions() != null && response.getCodeSystem().getCodeSystemVersions().size() > 0)
        {
          int status = response.getCodeSystem().getCodeSystemVersions().get(0).getStatus();
          objStatus = TERMSERVER_STATUS.getString(status);
        }
        else
          objStatus = "Fehler";
      }
      else if (response.getReturnInfos().getStatus() == Status.FAILURE)
      {
        objStatus = "nicht vorhanden";
      }
    }
    else if (po.getClassname().equalsIgnoreCase("ValueSetVersion"))
    {
      de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType request = new de.fhdo.terminologie.ws.search.ReturnValueSetDetailsRequestType();
      request.setLoginToken(CollaborationSession.getInstance().getSessionID());
      request.setValueSet(new ValueSet());
      ValueSetVersion vsv = new ValueSetVersion();
      vsv.setVersionId(po.getClassId());
      request.getValueSet().getValueSetVersions().add(vsv);

      ReturnValueSetDetailsResponse.Return response = WebServiceHelper.returnValueSetDetails(request);
      logger.debug("Antwort returnCodeSystemDetails: " + response.getReturnInfos().getMessage());
      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        if (response.getValueSet() != null
            && response.getValueSet().getValueSetVersions() != null && response.getValueSet().getValueSetVersions().size() > 0)
        {
          int status = response.getValueSet().getValueSetVersions().get(0).getStatus();
          objStatus = TERMSERVER_STATUS.getString(status);
        }
        else
          objStatus = "Fehler";
      }
      else if (response.getReturnInfos().getStatus() == Status.FAILURE)
      {
        objStatus = "nicht vorhanden";
      }
    }
    else if (po.getClassname().equalsIgnoreCase("ConceptValueSetMembership"))
    {
      de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipRequestType request = new de.fhdo.terminologie.ws.search.ReturnConceptValueSetMembershipRequestType();
      request.setLoginToken(CollaborationSession.getInstance().getSessionID());
      request.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
      request.setValueSetVersion(new ValueSetVersion());
      request.getCodeSystemEntityVersion().setVersionId(po.getClassId());
      request.getValueSetVersion().setVersionId(po.getClassId2());

      ReturnConceptValueSetMembershipResponse.Return response = WebServiceHelper.returnConceptValueSetMembership(request);
      logger.debug("Antwort returnCodeSystemDetails: " + response.getReturnInfos().getMessage());
      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        if (response.getConceptValueSetMembership() != null)
        {
          int status = response.getConceptValueSetMembership().getStatus();
          objStatus = TERMSERVER_STATUS.getString(status);
        }
        else
          objStatus = "Fehler";
      }
      else if (response.getReturnInfos().getStatus() == Status.FAILURE)
      {
        objStatus = "nicht vorhanden";
      }
    }
    else
    {
      objStatus = "kein Status";
    }

    GenericListRowType row = new GenericListRowType();

    GenericListCellType[] cells = new GenericListCellType[4];
    cells[0] = new GenericListCellType(PO_CHANGE_TYPE.getString(po.getChangeType()), false, "");
    cells[1] = new GenericListCellType(PO_CLASSNAME.getString(po.getClassname()), false, "");
    cells[2] = new GenericListCellType(po.getName(), false, "");
    cells[3] = new GenericListCellType(objStatus, false, "");

    row.setData(po);
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

    initListObjects();
    initListVerlauf();
    initListPrivilegien();
    initProposal();
  }

  public void onNewClicked(String id)
  {
    logger.debug("onNewClicked(): " + id);

    try
    {
      Map map = new HashMap();
      map.put("proposal", proposalView.getProposal());

      Window win = (Window) Executions.createComponents(
          "/collaboration/desktop/proposal/privilege/privilegeDetails.zul", null, map);

      ((PrivilegeDetails) win).setUpdateListInterface(this);
      win.doModal();
    }
    catch (Exception ex)
    {
      logger.debug("Fehler beim Öffnen der PrivilegeDetails: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  public void onEditClicked(String id, Object data)
  {
  }

  public void onDeleted(String id, Object data)
  {
    logger.debug("onDeleted()");

    if (data != null && data instanceof PrivilegienListData)
    {
      PrivilegienListData pld = (PrivilegienListData) data;

      // Person aus der Datenbank löschen
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        Privilege priv_db = (Privilege) hb_session.get(Privilege.class, pld.getPrivilege().getId());

        priv_db.setCollaborationuser(null);
        priv_db.setDiscussiongroup(null);
        priv_db.setProposal(null);

        hb_session.delete(priv_db);

        hb_session.getTransaction().commit();

        Messagebox.show("Eintrag wurde erfolgreich entfernt.", "Eintrag entfernen", Messagebox.OK, Messagebox.INFORMATION);
      }
      catch (Exception e)
      {
        hb_session.getTransaction().rollback();

        Messagebox.show("Fehler beim Entfernen des Eintrags: " + e.getLocalizedMessage(), "Eintrag entfernen", Messagebox.OK, Messagebox.EXCLAMATION);
        initListPrivilegien();
      }
      hb_session.close();
    }
  }

  public void onSelected(String id, Object data)
  {

  }

  public void update(Object o, boolean edited)
  {

    if (edited)
    { //einfacher weil derzeit mehrere rows entstehen können...
      initListPrivilegien();
    }
  }

  public void reSaveProposal()
  {

    // Person aus der Datenbank löschen
    /*TODO Session hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();

    try
    {
      Proposal p = (Proposal) hb_session.get(Proposal.class, proposalView.getProposal().getId());
      p.setDescription(proposalView.getProposal().getDescription());
      p.setNote(proposalView.getProposal().getNote());
      proposalView.getProposal().setLastChangeDate(new Date());
      p.setLastChangeDate(proposalView.getProposal().getLastChangeDate());
      ((Datebox) getFellow("db_lastChangeDate")).setValue(p.getLastChangeDate());
      hb_session.update(p);
      hb_session.getTransaction().commit();

      de.fhdo.collaboration.workflow.ReturnType rt = new ReturnType();
      rt.setSuccess(true);
      rt.setMessage("InlinePropUpdate");//don't change!
      proposalView.update(rt, false);

        //Mailversandt
      //Benachrichtigung Benutzer
      ArrayList<Collaborationuser> completeUserList = new ArrayList<Collaborationuser>();

      //Lade alle Benutzer mit Privilegien auf Proposal
      String hqlPrivilegeUsers = "from Collaborationuser cu join fetch cu.privileges pri join fetch pri.proposal pro join fetch cu.organisation o where pro.id=:id";
      Query qPrivilegeUsers = hb_session.createQuery(hqlPrivilegeUsers);
      qPrivilegeUsers.setParameter("id", proposalView.getProposal().getId());
      List<Collaborationuser> privUserList = qPrivilegeUsers.list();

      for (Collaborationuser cu : privUserList)
      {
        completeUserList.add(cu);
      }

      //Lade alle Diskussionsgruppen mit Privilegien auf Proposal
      String hqlPrivilegeGroups = "from Collaborationuser cu join fetch cu.discussiongroups dg join fetch dg.privileges pri join fetch pri.proposal pro where pro.id=:id";
      Query qPrivilegeGroups = hb_session.createQuery(hqlPrivilegeGroups);
      qPrivilegeGroups.setParameter("id", proposalView.getProposal().getId());
      List<Collaborationuser> privGroupList = qPrivilegeGroups.list();

      for (Collaborationuser cu : privGroupList)
      {

        boolean doubleEntry = false;
        for (Collaborationuser cuI : completeUserList)
        {

          if (cu.getId().equals(cuI.getId()))
          {
            doubleEntry = true;
          }
        }

        if (!doubleEntry)
        {
          completeUserList.add(cu);
        }
      }

      ArrayList<String> mailAdr = new ArrayList<String>();
      for (Collaborationuser u : completeUserList)
      {

        if (u.getSendMail() != null && u.getSendMail())
          mailAdr.add(u.getEmail());
      }
      String[] adr = new String[mailAdr.size()];
      for (int i = 0; i < adr.length; i++)
      {

        adr[i] = mailAdr.get(i);
      }
      Mail.sendMailAUT(adr, M_AUT.PROPOSAL_CHANGE_DESCRIPTION, M_AUT.getInstance().getProposalDescriptionChangeText(
          proposalView.getProposal().getVocabularyName(),
          proposalView.getProposal().getContentType(),
          proposalView.getProposal().getDescription(), proposalView.getProposal().getNote()));
    }
    catch (Exception e)
    {
      hb_session.getTransaction().rollback();

      Messagebox.show("Fehler beim aktualisieren der Daten: " + e.getLocalizedMessage(), "Update", Messagebox.OK, Messagebox.EXCLAMATION);
      initListPrivilegien();
    }
    hb_session.close();*/
  }

  public void initProposal()
  {
/* TODO 
    Long userId = SessionHelper.getCollaborationUserID();
    if (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN)
        || (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)
        && AssignTermHelper.isUserAllowed(proposalView.getProposal().getVocabularyIdTwo(), proposalView.getProposal().getVocabularyNameTwo()))
        || (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_BENUTZER)
        && userId == proposalView.getProposal().getCollaborationuser().getId()))
    {
      ((Button) getFellow("buttonEditProposal")).setDisabled(false);
    }

    if (proposalView.getProposal().getCollaborationuser().getId().equals(userId)
        && (ProposalStatus.getInstance().getStatusStr(proposalView.getProposal().getStatus())).equals("vorgeschlagen"))
    {
      ((Button) getFellow("buttonEditProposal")).setDisabled(false);
    }*/
  }
}
