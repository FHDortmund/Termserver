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
package de.fhdo.gui.admin.modules.collaboration.workflow;

import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.db.classes.Proposalobject;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.PO_CHANGE_TYPE;
import de.fhdo.collaboration.db.classes.Privilege;
import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Mail;
import de.fhdo.helper.AssignTermHelper;
import de.fhdo.helper.CODES;
import de.fhdo.helper.SessionHelper;
import de.fhdo.logging.LoggingOutput;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert Mützner
 */
public class ProposalWorkflow
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  // Singleton-Muster
  private static ProposalWorkflow instance;

  public static ProposalWorkflow getInstance()
  {
    if (instance == null)
      instance = new ProposalWorkflow();

    return instance;
  }

  // Konstruktor
  public ProposalWorkflow()
  {
  }

  /**
   * Fügt einen neuen Vorschlag hinzu und benachrichtigt alle verantwortlichen
   * Personen.
   *
   * @param proposal Vorschlag
   * @param obj Einzufügendes Objekt (z.B. CodeSystemConcept)
   * @return
   */
  public ReturnType addProposal(Proposal proposal, Object obj, Boolean isExisting)
  {
    return addProposal(proposal, obj, null, null, "", isExisting);
  }

  public ReturnType addProposal(Proposal proposal, Object obj, Object obj2, Long csId, Boolean isExisting)
  {
    return addProposal(proposal, obj, obj2, csId, "", isExisting);
  }
  /**
   * Fügt einen neuen Vorschlag hinzu und benachrichtigt alle verantwortlichen
   * Personen.
   *
   * @param proposal Vorschlag
   * @param obj Einzufügendes Objekt (z.B. CodeSystemConcept)
   * @param obj2 Einzufügendes Objekt 2 (z.B. CodeSystemConcept)
   * @return
   */
  public ReturnType addProposal(Proposal proposal, Object obj, Object obj2, Long csId, String searchCode, Boolean isExisting)
  {
    ReturnType returnInfos = new ReturnType();

    // TODO erst prüfen, ob Benutzer exisitert (Collaborationuser)
    // SessionHelper.getCollaborationUserID()

    List<Proposalobject> proposalObjectList = new java.util.LinkedList<Proposalobject>();

    boolean tsDataInserted = false;

    // 1. Objekte in Terminologieserver erstellen
    try
    {

      if (proposal.getContentType().equals("vocabulary"))
      {
            CodeSystem cs = (CodeSystem)obj;

            tsDataInserted = true;   // Erfolg
            AssignTermHelper.assignTermToUser(cs);

            proposal.setVocabularyId(cs.getCurrentVersionId());
            proposal.setVocabularyIdTwo(cs.getId());
            proposal.setDescription("Dieses Code System wurde von einem Inhaltsverwalter über den Import eingefügt.");
           
            // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
            Proposalobject po = new Proposalobject();
            po.setClassId(cs.getId());
            po.setClassname("CodeSystem");
            po.setName(cs.getName());
            po.setChangeType(PO_CHANGE_TYPE.NEW.id());
            proposalObjectList.add(po);

            po = new Proposalobject();
            po.setClassId(cs.getCurrentVersionId());
            po.setClassname("CodeSystemVersion");
            po.setName(cs.getCodeSystemVersions().get(0).getName());
            po.setChangeType(PO_CHANGE_TYPE.NEW.id());
            proposalObjectList.add(po);
          
      }else if (proposal.getContentType().equals("valueset")){
      
              ValueSet vs = (ValueSet)obj;
              tsDataInserted = true;   // Erfolg
              AssignTermHelper.assignTermToUser(vs);

              proposal.setVocabularyId(vs.getCurrentVersionId());
              proposal.setVocabularyIdTwo(vs.getId());
              proposal.setDescription("Dieses ValueSet wurde von einem Inhaltsverwalter über den Import eingefügt.");

              // Wird später in DB eingefügt (Codesystem + CodesystemVersion)
              Proposalobject po = new Proposalobject();
              po.setClassId(vs.getId());
              po.setClassname("ValueSet");
              po.setName(vs.getName());
              po.setChangeType(PO_CHANGE_TYPE.NEW.id());
              proposalObjectList.add(po);

              po = new Proposalobject();
              po.setClassId(vs.getCurrentVersionId());
              po.setClassname("ValueSetVersion");      
              po.setName(vs.getValueSetVersions().get(0).getName());
              po.setChangeType(PO_CHANGE_TYPE.NEW.id());
              proposalObjectList.add(po);
      }
    }
    catch (Exception ex)
    {
      LoggingOutput.outputException(ex, this);
    }

    logger.debug("tsDataInserted: " + tsDataInserted);

    if (tsDataInserted)
    {
      Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();

      try
      {
        // 2. Vorschlag in DB hinzufügen
        proposal.setStatus(1); // TODO
        proposal.setStatusDate(new Date());
        proposal.setCreated(new Date());
        proposal.setCollaborationuser(new Collaborationuser());
        proposal.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());
        
        hb_session.save(proposal);

        // 3. Objekte in DB hinzufügen
        for (Proposalobject po : proposalObjectList)
        {
          po.setProposal(proposal);
          hb_session.save(po);
        }

        //Add creator Default privilege
        Privilege priv = new Privilege();
        priv.setCollaborationuser(new Collaborationuser());
        priv.getCollaborationuser().setId(SessionHelper.getCollaborationUserID());
               
        Collaborationuser u = (Collaborationuser)hb_session.get(Collaborationuser.class, SessionHelper.getCollaborationUserID());
        
        priv.setSendMail(u.getSendMail());
        
        if(u.getRoles().iterator().next().getName().equals(CODES.ROLE_ADMIN) ||
           u.getRoles().iterator().next().getName().equals(CODES.ROLE_INHALTSVERWALTER)){
            priv.setMayChangeStatus(true);
            priv.setMayManageObjects(true);
        }else{
            priv.setMayChangeStatus(false);
            priv.setMayManageObjects(false);
        }
        priv.setFromDate(new Date());
        priv.setProposal(new Proposal());
        priv.getProposal().setId(proposal.getId());
        priv.setDiscussiongroup(null);
        
        hb_session.save(priv);

        returnInfos.setSuccess(true);
        returnInfos.setMessage("Vorschlag erfolgreich eingefügt.");
        
        if(u.getSendMail()){
            //String[] adr = new String[1];
            //adr[0] = u.getEmail();
            Mail.sendMailAUT(u, M_AUT.PROPOSAL_SUBJECT, M_AUT.getInstance().getProposalText(
                        proposal.getVocabularyName(), 
                        proposal.getContentType(),
                        proposal.getDescription()));
        }
        Long id=0l;
        String classname="";
        if(proposal.getContentType().equals("vocabulary")){
        
            id = proposal.getVocabularyIdTwo();
            classname = "CodeSystem";
            
        }else if(proposal.getContentType().equals("valueset")){
        
            id = proposal.getVocabularyIdTwo();
            classname = "ValueSet";
             
        }else if(proposal.getContentType().equals("concept") || proposal.getContentType().equals("subconcept")){
        
            id = proposal.getVocabularyIdTwo();
            classname = "CodeSystem";
        }else if(proposal.getContentType().equals("conceptVs")){
        
            id = proposal.getVocabularyIdTwo();
            classname = "ValueSet";
        }
        
        String termHead = "from Collaborationuser cu join fetch cu.assignedTerms at where at.classId=:classId and at.classname=:classname";
        Query qTermHead = hb_session.createQuery(termHead);
        qTermHead.setParameter("classId", id);
        qTermHead.setParameter("classname", classname);
        List<Collaborationuser> userList = qTermHead.list();
        
        if(userList.size() == 1){
        
            if(userList.get(0).getId().equals(u.getId())){
                //SV == Antragsteller => Do nothing
            }else{
                
                //Erstelle privilegien für den SV
                Privilege privSv = new Privilege();
                privSv.setCollaborationuser(new Collaborationuser());
                privSv.getCollaborationuser().setId(userList.get(0).getId());

                privSv.setSendMail(userList.get(0).getSendMail());

                privSv.setMayChangeStatus(true);
                privSv.setMayManageObjects(true);
                
                privSv.setFromDate(new Date());
                privSv.setProposal(new Proposal());
                privSv.getProposal().setId(proposal.getId());
                privSv.setDiscussiongroup(null);

                hb_session.save(privSv);
                
                if(userList.get(0).getSendMail()){
                    
                    String[] adr = new String[1];
                    adr[0] = userList.get(0).getEmail();
                    Mail.sendMailAUT_MultiUser(adr, M_AUT.PROPOSAL_SUBJECT, M_AUT.getInstance().getProposalSelbstVerwText(
                        proposal.getVocabularyName(), 
                        proposal.getContentType(),
                        proposal.getDescription()));
                }
            }    
        }
      hb_session.getTransaction().commit();  
      }
      catch (Exception ex)
      {
        LoggingOutput.outputException(ex, this);
        hb_session.getTransaction().rollback();

        returnInfos.setSuccess(false);
        returnInfos.setMessage("Fehler beim Einfügen eines Vorschlags: " + ex.getLocalizedMessage());
      }
      finally
      {
        hb_session.close();
      }
    }

    return returnInfos;
  }
}
