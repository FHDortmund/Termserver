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
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalstatuschange;
import de.fhdo.logging.LoggingOutput;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

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


  public static Map<Long, Collaborationuser> getAllUsersForProposal(long proposalId)
  {
    Map<Long, Collaborationuser> list = new HashMap<Long, Collaborationuser>();

    DiscussionGroupUserHelper.getInstance().initData(); // vor Session Daten initalisieren

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      String hql = "select distinct p from Privilege p "
              + " left join p.proposal prop"
              + " left join fetch p.collaborationuser cu"
              + " left join fetch cu.organisation org"
              + " left join fetch p.discussiongroup dg"
              + " where prop.id=" + proposalId;

      if (logger.isDebugEnabled())
        logger.debug("HQL: " + hql);

      org.hibernate.Query q = hb_session.createQuery(hql);
      List<Privilege> liste = q.list();

      for (Privilege priv : liste)
      {
        //logger.debug("Privileg mit ID: " + priv.getId());

        if (priv.getCollaborationuser() != null)
        {
          list.put(priv.getCollaborationuser().getId(), priv.getCollaborationuser());
        }

        if (priv.getDiscussiongroup() != null)
        {
          for (Collaborationuser cu : priv.getDiscussiongroup().getCollaborationusers())
          {
            list.put(cu.getId(), cu);
          }
          /*Discussiongroup dg = DiscussionGroupUserHelper.getInstance().getDiscussionGroup(priv.getDiscussiongroup().getId());
           if (dg != null)
           {
           for (Collaborationuser cu : dg.getCollaborationusers())
           {
           list.put(cu.getId(), cu);
           }

           }*/
        }

      }
      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
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
      {hb_session.close();
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
    if(StatusId == 2)  // TODO Feste Variable "2" in SysParam schreiben
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
      
      if(proposal.getStatus().longValue() == 2)  // TODO Feste Variable "2" in SysParam schreiben
      {
        Date jetzt = new Date();
        
        if(proposal.getValidFrom() != null)
        {
          if(jetzt.before(proposal.getValidFrom()))
          {
            return false;
          }
        }
        if(proposal.getValidTo() != null)
        {
          if(jetzt.after(proposal.getValidTo()))         
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
}
