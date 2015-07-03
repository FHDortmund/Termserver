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
package de.fhdo.collaboration.helper;

import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.collaboration.db.classes.Role;
import de.fhdo.collaboration.db.classes.Userprivilege;
import de.fhdo.logging.LoggingOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemVersion;

/**
 *
 * @author Robert Mützner
 */
public class ProposalHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public ProposalHelper()
  {

  }
  
  public static Map<Long, Userprivilege> getAllUserPrivilegesForProposal(long proposalId)
  {
    return getAllUserPrivilegesForProposal(proposalId, null);
  }
  public static Map<Long, Userprivilege> getAllUserPrivilegesForProposal(long proposalId, Session session)
  {
    Map<Long, Userprivilege> list = new HashMap<Long, Userprivilege>();

    boolean createHibernateSession = false;
    Session hb_session = session;
    if(hb_session == null)
    {
      hb_session = HibernateUtil.getSessionFactory().openSession();
      createHibernateSession = true;
    }

    try
    {
      long objectId, objectVersionId;
      
      Proposal proposal = (Proposal) hb_session.get(Proposal.class, proposalId);
      // Ersteller hinzufügen
      Userprivilege privOwner = new Userprivilege();
      privOwner.setCollaborationuser(proposal.getCollaborationuser());
      privOwner.setRole(new Role());
      privOwner.getRole().setName("Ersteller");
      privOwner.getRole().setMayAdminProposal(true);
      privOwner.getRole().setMayChangeStatus(true);
      privOwner.getRole().setId(0l);
      list.put(proposal.getCollaborationuser().getId(), privOwner);
      
      // proposalId in objectId und objectVersionId auflösen, wenn objectId unbekannt
      objectId = proposal.getObjectId();
      objectVersionId = proposal.getObjectVersionId();
      
      
      String hql = "select distinct p from Userprivilege p "
          + " left join fetch p.collaborationuser cu"
          + " left join fetch p.role r"
          + " left join fetch cu.organisation org"
          + " where p.objectId=" + objectId + " and p.objectVersionId=" + objectVersionId;

      if (logger.isDebugEnabled())
        logger.debug("HQL: " + hql);

      org.hibernate.Query q = hb_session.createQuery(hql);
      List<Userprivilege> liste = q.list();

      for (Userprivilege priv : liste)
      {
        if (priv.getCollaborationuser() != null)
        {
          list.put(priv.getCollaborationuser().getId(), priv);
        }
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ProposalHelper.class);
    }
    finally
    {
      if(createHibernateSession)
      {
        // Session schließen
        hb_session.close();
      }
    }

    return list;
  }

  public static Map<Long, Collaborationuser> getAllUsersForProposal(long proposalId)
  {
    Map<Long, Collaborationuser> list = new HashMap<Long, Collaborationuser>();

    Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      long objectId, objectVersionId;
      
      Proposal proposal = (Proposal) hb_session.get(Proposal.class, proposalId);
      // Ersteller hinzufügen
      list.put(proposal.getCollaborationuser().getId(), proposal.getCollaborationuser());
      
      // proposalId in objectId und objectVersionId auflösen, wenn objectId unbekannt
      objectId = proposal.getObjectId();
      objectVersionId = proposal.getObjectVersionId();
      
      
      String hql = "select distinct p from Userprivilege p "
          + " left join fetch p.collaborationuser cu"
          + " left join fetch cu.organisation org"
          + " where p.objectId=" + objectId + " and p.objectVersionId=" + objectVersionId;

      if (logger.isDebugEnabled())
        logger.debug("HQL: " + hql);

      org.hibernate.Query q = hb_session.createQuery(hql);
      List<Userprivilege> liste = q.list();

      for (Userprivilege priv : liste)
      {
        if (priv.getCollaborationuser() != null)
        {
          list.put(priv.getCollaborationuser().getId(), priv.getCollaborationuser());
        }
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ProposalHelper.class);
    }
    finally
    {
      // Session schließen
      hb_session.close();
    }

    return list;
  }

  public static String getNameFull(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getFirstName() + " " + user.getName()).trim();
      if (user.getOrganisation() != null)
      {
        if (user.getOrganisation().getOrganisationAbbr() != null
            && user.getOrganisation().getOrganisationAbbr().length() > 0)
        {
          s += " (" + user.getOrganisation().getOrganisationAbbr() + ")";
        }
        else
        {
          s += " (" + user.getOrganisation().getOrganisation() + ")";
        }
      }
    }

    return s;
  }

  public static String getName(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getFirstName() + " " + user.getName()).trim();
    }

    return s;
  }

  public static String getNameReverseFull(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getName() + ", " + user.getFirstName()).trim();
      if (user.getOrganisation() != null)
      {
        if (user.getOrganisation().getOrganisationAbbr() != null
            && user.getOrganisation().getOrganisationAbbr().length() > 0)
        {
          s += " (" + user.getOrganisation().getOrganisationAbbr() + ")";
        }
        else
        {
          s += " (" + user.getOrganisation().getOrganisation() + ")";
        }
      }
    }

    return s;
  }

  public static String getOrganisation(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      if (user.getOrganisation() != null)
      {
        s += user.getOrganisation().getOrganisation();
      }
    }

    return s;
  }

  public static String getNameReverse(Collaborationuser user)
  {
    String s = "";

    if (user != null)
    {
      s = (user.getName() + ", " + user.getFirstName()).trim();
    }

    return s;
  }

  public static String getNameShort(Collaborationuser user)
  {
    String s = "";

    if (user != null && user.getName() != null)
    {
      s = user.getName().trim();
    }

    return s;
  }

  public static List<Proposalstatuschange> getStatusChangeList(Proposal proposal)
  {
    List<Proposalstatuschange> list = new LinkedList<Proposalstatuschange>();
    if (proposal != null)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      try
      {
        String hql = "from Proposalstatuschange psc "
            + " left join fetch psc.collaborationuser cu"
            + " where proposalId=" + proposal.getId()
            + " order by changeTimestamp ";

        list = hb_session.createQuery(hql).list();

        //hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        LoggingOutput.outputException(e, ProposalHelper.class);
      }
      finally
      {
        hb_session.close();
      }

      // Ersteller hinzufügen
      Proposalstatuschange psc = new Proposalstatuschange();
      psc.setProposal(proposal);
      psc.setProposalStatusTo(1); // TODO
      psc.setChangeTimestamp(proposal.getCreated());
      psc.setCollaborationuser(proposal.getCollaborationuser());
      list.add(0, psc);

      if (psc.getCollaborationuser() != null)
        logger.debug("PSC-User: " + psc.getCollaborationuser().getName());
      else
        logger.debug("PSC-User: null");

    }
    else
    {
      logger.warn("[ProposalHelper.java] getStatusChangeList() - Proposal ist null");
    }
    return list;
  }

  public static boolean isStatusDiscussion(long StatusId)
  {
    if (StatusId == 2)  // TODO Feste Variable "2" in SysParam schreiben
      return true;

    return false;
  }

  public static boolean isProposalInDiscussion(Proposal proposal)
  {

    if (proposal != null)
    {
      logger.debug("isProposalInDiscussion() mit ID: " + proposal.getId());

      /*boolean createSession = session == null;
       Session hb_session = session;
       if(createSession)
       hb_session = HibernateUtil.getSessionFactory().openSession();*/
      logger.debug("Status: " + proposal.getStatus());

      if (proposal.getStatus().longValue() == 2)  // TODO Feste Variable "2" in SysParam schreiben
      {
        Date jetzt = new Date();

        if (proposal.getValidFrom() != null)
        {
          if (jetzt.before(proposal.getValidFrom()))
          {
            return false;
          }
        }
        if (proposal.getValidTo() != null)
        {
          if (jetzt.after(proposal.getValidTo()))
            return false;
        }

        return true;  // Vorschlag ist in gültiger Diskussion
      }
    }
    else
    {
      logger.warn("[ProposalHelper.java] isProposalInDiscussion() - Proposal ist null");
    }
    return false;
  }
  
  public static void createProposalConcept(CodeSystem codeSystem, CodeSystemVersion codeSystemVersion, Window parent)
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("isExisting", false);
      data.put("source", new CodeSystemEntityVersion());
      
      data.put("codeSystemId", codeSystem.getId());
      data.put("codeSystemVersionId", codeSystemVersion.getVersionId());
      
      data.put("objectName", codeSystem.getName());
      data.put("objectVersionName", codeSystemVersion.getName());
      
      Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", parent, data);
      w.doModal();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, ProposalHelper.class);
    }
  }
}
