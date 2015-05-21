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
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId;
import de.fhdo.terminologie.helper.HQLParameterHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CopyContent
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  /**
   * Copy all content from one codesystem version to another
   * 
   * @param previousCodeSystemVersionId
   * @param newCodeSystemVersionId
   * @param hb_session
   * @return true, if successfull
   */
  public boolean copyContent(long previousCodeSystemVersionId, long newCodeSystemVersionId, Session hb_session)
  {
    logger.debug("start copying content from " + previousCodeSystemVersionId + " to " + newCodeSystemVersionId);
    
    try
    {
      String hql = "select distinct csev from CodeSystemEntityVersion csev"
          + " join fetch csev.codeSystemEntity cse"
          + " join cse.codeSystemVersionEntityMemberships csvem"
          + " join fetch csev.codeSystemConcepts csc";
      
      HQLParameterHelper parameter = new HQLParameterHelper();
      //parameter.addParameter("csvem.", "codeSystemVersionId", previousCodeSystemVersionId);
      parameter.addParameter("", "codeSystemVersionId", previousCodeSystemVersionId);
      hql += parameter.getWhere("");
      logger.debug("HQL: " + hql);
          
      org.hibernate.Query q = hb_session.createQuery(hql);
      parameter.applyParameter(q);
      
      List<CodeSystemEntityVersion> csevList = q.list();
      
      CodeSystemVersion codeSystemVersion_new = new CodeSystemVersion();
      codeSystemVersion_new.setVersionId(newCodeSystemVersionId);
      
      Map<Long,Long> csevMapping = new HashMap<Long, Long>();
      Map<Long,Long> atMapping = new HashMap<Long, Long>();
      
      for(CodeSystemEntityVersion csev_old : csevList)
      {
        logger.debug("Copy csev with id: " + csev_old.getVersionId());
        
        // 1. copy code_system_entity
        CodeSystemEntity cse_old = csev_old.getCodeSystemEntity();
        CodeSystemEntity cse_new = cse_old.cloneObject();
        cse_new.setId(null);
        hb_session.save(cse_new);
        logger.debug("new cse-id: " + cse_new.getId());
        
        // 2. copy code_system_entity_version
        CodeSystemEntityVersion csev_new = csev_old.cloneObject();
        csev_new.setVersionId(null);
        csev_new.setCodeSystemEntity(cse_new);
        hb_session.save(csev_new);
        logger.debug("new csev-id: " + csev_new.getVersionId());
        csevMapping.put(csev_old.getVersionId(), csev_new.getVersionId());
        
        // 3. update currentVersionId
        if(cse_old.getCurrentVersionId().longValue() == csev_old.getVersionId())
        {
          logger.debug("update cse.currentVersionId: " + csev_new.getVersionId());
          cse_new.setCurrentVersionId(csev_new.getVersionId());
          hb_session.update(cse_new);
        }
        
        // 4. copy code_system_version_entity_membership
        CodeSystemVersionEntityMembership csvem_old = cse_old.getCodeSystemVersionEntityMemberships().iterator().next();
        CodeSystemVersionEntityMembership csvem_new = csvem_old.copyObject();
        csvem_new.setCodeSystemVersion(codeSystemVersion_new);
        csvem_new.setCodeSystemEntity(cse_new);
        csvem_new.setId(new CodeSystemVersionEntityMembershipId(codeSystemVersion_new.getVersionId(), cse_new.getId()));
        hb_session.save(csvem_new);
        logger.debug("new csvem created");
        
        if(csev_old.getCodeSystemConcepts() != null && csev_old.getCodeSystemConcepts().iterator().hasNext())
        {
          // 5. copy code_system_concept
          CodeSystemConcept csc_old = csev_old.getCodeSystemConcepts().iterator().next();
          CodeSystemConcept csc_new = csc_old.copyObject();
          csc_new.setCodeSystemEntityVersionId(csev_new.getVersionId());
          hb_session.save(csc_new);
          logger.debug("new csc-id: " + csev_new.getVersionId());
        
          // 6. copy code_system_concept_translation
          for(CodeSystemConceptTranslation csct_old : csc_old.getCodeSystemConceptTranslations())
          {
            CodeSystemConceptTranslation csct_new = csct_old.copyObject();
            csct_new.setId(null);
            csct_new.setCodeSystemConcept(csc_new);
            hb_session.save(csct_new);
            logger.debug("new csct-id: " + csct_new.getId());
          }
        }
        
        if(csev_old.getAssociationTypes() != null && csev_old.getAssociationTypes().iterator().hasNext())
        {
          // 7. copy association_type
          for(AssociationType at_old : csev_old.getAssociationTypes())
          {
            AssociationType at_new = at_old.copyObject();
            at_new.setCodeSystemEntityVersion(csev_new);
            hb_session.save(at_new);
            logger.debug("at created");
            atMapping.put(at_old.getCodeSystemEntityVersionId(), at_new.getCodeSystemEntityVersionId());
          }
        }
        
        // 8. copy code_system_metadata_value
        for(CodeSystemMetadataValue csmv_old : csev_old.getCodeSystemMetadataValues())
        {
          CodeSystemMetadataValue csmv_new = csmv_old.copyObject();
          csmv_new.setId(null);
          csmv_new.setCodeSystemEntityVersion(csev_new);
          hb_session.save(csmv_new);
          logger.debug("new csmv-id: " + csmv_new.getId());
        }
      }
      
      Map<Long,Boolean> associationCopied = new HashMap<Long, Boolean>();
      
      logger.debug("======================================");
      logger.debug("copy associations...");
      logger.debug("======================================");
      for(CodeSystemEntityVersion csev_old : csevList)
      {
        logger.debug("Check associations for csev with id: " + csev_old.getVersionId());
        
        if(csevMapping.containsKey(csev_old.getPreviousVersionId()))
        {
          long csev_change_id = csevMapping.get(csev_old.getVersionId());
          
          CodeSystemEntityVersion csev_change = (CodeSystemEntityVersion) hb_session.get(CodeSystemEntityVersion.class, csev_change_id);
          csev_change.setPreviousVersionId(csevMapping.get(csev_old.getPreviousVersionId()));
          hb_session.update(csev_change);
          logger.debug("previousVersionId changed from " + csev_old.getVersionId() + " to " + csev_change.getPreviousVersionId());
        }
        
        // 9. copy code_system_entity_version_association
        for(CodeSystemEntityVersionAssociation cseva_old : csev_old.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
        {
          if(associationCopied.containsKey(cseva_old.getId()))
            continue;
          
          CodeSystemEntityVersionAssociation cseva_new = cseva_old.copyObject();
          cseva_new.setId(null);
          cseva_new.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
          cseva_new.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
          cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csevMapping.get(cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId()));
          cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csevMapping.get(cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId()));
          
          if(cseva_old.getLeftId() != null)
          {
            if(cseva_old.getLeftId().longValue() == cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId())
              cseva_new.setLeftId(cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId());
            else if(cseva_old.getLeftId().longValue() == cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId())
              cseva_new.setLeftId(cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId());
          }
          
          cseva_new.setAssociationType(new AssociationType());
          if(atMapping.containsKey(cseva_old.getAssociationType().getCodeSystemEntityVersionId()))
          {
            // association belongs to csv
            cseva_new.getAssociationType().setCodeSystemEntityVersionId(atMapping.get(cseva_old.getAssociationType().getCodeSystemEntityVersionId()));
          }
          else
          {
            // association is global, so use old
            cseva_new.getAssociationType().setCodeSystemEntityVersionId(cseva_old.getAssociationType().getCodeSystemEntityVersionId());
          }
          
          hb_session.save(cseva_new);
          logger.debug("new cseva-id: " + cseva_new.getId());
          associationCopied.put(cseva_old.getId(), true);
        }
        
        for(CodeSystemEntityVersionAssociation cseva_old : csev_old.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2())
        {
          if(associationCopied.containsKey(cseva_old.getId()))
            continue;
          
          CodeSystemEntityVersionAssociation cseva_new = cseva_old.copyObject();
          cseva_new.setId(null);
          cseva_new.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
          cseva_new.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
          cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(csevMapping.get(cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId()));
          cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(csevMapping.get(cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId()));
          
          if(cseva_old.getLeftId() != null)
          {
            if(cseva_old.getLeftId().longValue() == cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId())
              cseva_new.setLeftId(cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId());
            else if(cseva_old.getLeftId().longValue() == cseva_old.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId())
              cseva_new.setLeftId(cseva_new.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId());
          }
          
          cseva_new.setAssociationType(new AssociationType());
          if(atMapping.containsKey(cseva_old.getAssociationType().getCodeSystemEntityVersionId()))
          {
            // association belongs to csv
            cseva_new.getAssociationType().setCodeSystemEntityVersionId(atMapping.get(cseva_old.getAssociationType().getCodeSystemEntityVersionId()));
          }
          else
          {
            // association is global, so use old
            cseva_new.getAssociationType().setCodeSystemEntityVersionId(cseva_old.getAssociationType().getCodeSystemEntityVersionId());
          }
          
          hb_session.save(cseva_new);
          logger.debug("new cseva-id: " + cseva_new.getId());
          associationCopied.put(cseva_old.getId(), true);
        }
      }
    }
    catch(Exception ex)
    {
      LoggingOutput.outputException(ex, this);
      return false;
    }
    
    return true;
  }
  
}
