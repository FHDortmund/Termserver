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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;


/**
 *
 * @author Philipp Urbauer
 */


public class DeleteTermHelper {
    
    public static String deleteVS_VSV(Boolean onlyVSV,Long valueSetId, Long valueSetVersionId){
    
        //get Missing Id's for all CVSM
        String result="\n";
        List<ConceptValueSetMembership> cvsmList = null;
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        
        try
        {
            String hqlCvsm = "select distinct cvsm from ConceptValueSetMembership cvsm join cvsm.valueSetVersion vsv where vsv.versionId=:valueSetVersionId";
            Query q_Cvsm = hb_session.createQuery(hqlCvsm);
            q_Cvsm.setParameter("valueSetVersionId", valueSetVersionId);
            cvsmList = q_Cvsm.list();
            //Start deleting
            hb_session.getTransaction().begin();

            int rowCountVsmv = 0;
            int rowCountCvsm = 0;
            int rowCountVsv = 0;
            int rowCountMp = 0;
            int rowCountVs = 0;
            
            for(ConceptValueSetMembership cvsm:cvsmList){
                
                String hql_vsmv = "delete from ValueSetMetadataValue vsmv join fetch vsmv.codeSystemEntityVersion csev where vsmv.valuesetVersionId=:vsvId and csev.versionId=:csevId";
                Query q_vsmv = hb_session.createQuery(hql_vsmv);
                q_vsmv.setParameter("vsvId", cvsm.getValueSetVersion().getVersionId());
                q_vsmv.setParameter("csevId", cvsm.getCodeSystemEntityVersion().getVersionId());
                rowCountVsmv += q_vsmv.executeUpdate();
                
                try{
                    hb_session.delete(cvsm);
                    ++rowCountCvsm;
                }catch(Exception ex){
                }
            }
            hb_session.getTransaction().commit();
            
            hb_session.getTransaction().begin();
            
            String hql_vsv = "delete from ValueSetVersion where versionId=:vsvId";
            Query q_vsv = hb_session.createQuery(hql_vsv);
            q_vsv.setParameter("vsvId", valueSetVersionId);
            rowCountVsv += q_vsv.executeUpdate();
            
            hb_session.getTransaction().commit();
            
            result += "vsmv: " + rowCountVsmv + "\n";
            result += "cvsm: " + rowCountCvsm + "\n";
            result += "vsv: " + rowCountVsv + "\n";
            
            if(!onlyVSV){
                
                //Check for VSMV Reste and delete
                String hqlMpSearch = "from MetadataParameter where valueSetId=:vsId";
                Query q_MpSearch = hb_session.createQuery(hqlMpSearch);
                q_MpSearch.setParameter("vsId", valueSetId);
                List<MetadataParameter> mpList = q_MpSearch.list();
                
                hb_session.getTransaction().begin();
                
                for(MetadataParameter mp:mpList){
                    String hql_vsmv2 = "delete from ValueSetMetadataValue where metadataParameterId=:mpId";
                    Query q_vsmv2 = hb_session.createQuery(hql_vsmv2);
                    q_vsmv2.setParameter("mpId", mp.getId());
                    rowCountVsmv += q_vsmv2.executeUpdate();
                }
                
                //Mp
                String hql_mp = "delete from MetadataParameter where valueSetId=:vsId";
                Query q_mp = hb_session.createQuery(hql_mp);
                q_mp.setParameter("vsId", valueSetId);
                rowCountMp += q_mp.executeUpdate();
                
                //CS
                String hql_vs = "delete from ValueSet where id=:vsId";
                Query q_vs = hb_session.createQuery(hql_vs);
                q_vs.setParameter("vsId", valueSetId);
                rowCountVs += q_vs.executeUpdate();
                
                hb_session.getTransaction().commit();
                
                result += "mp: " + rowCountMp + "\n";
                result += "vs: " + rowCountVs + "\n";
            }
        }
        catch (Exception e)
        {
            hb_session.getTransaction().rollback();
            result += "An Error occured: " + e.getMessage();
        }finally{
            hb_session.close();
        }
        return result;
    }
    
    public static String deleteCS_CSV(Boolean onlyCSV,Long codeSystemId, Long codeSystemVersionId){
    
        //get Missing Id's for all CSEV
        String result="\n";
        List csevIds = null;
        Session hb_session = HibernateUtil.getSessionFactory().openSession();
        
        try
        {
            
            String hqlCsevNumber = "select csev.versionId,csev.codeSystemEntity.id from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem";
                  hqlCsevNumber += " join csvem.codeSystemVersion csv where csv.versionId=:versionId";
            Query q_CsevNumber = hb_session.createQuery(hqlCsevNumber);
            q_CsevNumber.setParameter("versionId", codeSystemVersionId);
            csevIds = q_CsevNumber.list();
            //Start deleting
            hb_session.getTransaction().begin();
            //Delete Translations, CodeSystemConcept
            int rowCountCsct = 0;
            int rowCountCsc = 0;
            int rowCountCvsm = 0;
            int rowCountCsmv = 0;
            int rowCountCseva = 0;
            int rowCountAt = 0;
            int rowCountCsev = 0;
            int rowCountCsvem = 0;
            int rowCountCse = 0;
            int rowCountCsv = 0;
            int rowCountLu = 0;
            int rowCountLt = 0;
            int rowCountMp = 0;
            int rowCountDvhcs = 0;
            int rowCountCs = 0;
            
            for(Object o:csevIds){
                Object[] x = (Object[])o;
                Long csevId = (Long)x[0];
                Long cseId = (Long)x[1];
                
                String hql_csct = "delete from CodeSystemConceptTranslation where codeSystemConcept.codeSystemEntityVersionId=:csevId";
                Query q_csct = hb_session.createQuery(hql_csct);
                q_csct.setParameter("csevId", csevId);
                rowCountCsct += q_csct.executeUpdate();
                
                String hql_csc = "delete from CodeSystemConcept where codeSystemEntityVersionId=:csevId";
                Query q_csc = hb_session.createQuery(hql_csc);
                q_csc.setParameter("csevId", csevId);
                rowCountCsc += q_csc.executeUpdate();
                
                String hql_cvsm = "delete from ConceptValueSetMembership where codeSystemEntityVersionId=:csevId";
                Query q_cvsm = hb_session.createQuery(hql_cvsm);
                q_cvsm.setParameter("csevId", csevId);
                rowCountCvsm += q_cvsm.executeUpdate();
                
                String hql_csmv = "delete from CodeSystemMetadataValue where codeSystemEntityVersionId=:csevId";
                Query q_csmv = hb_session.createQuery(hql_csmv);
                q_csmv.setParameter("csevId", csevId);
                rowCountCsmv += q_csmv.executeUpdate();
                
                String hql_cseva1 = "delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId1=:csevId";
                Query q_cseva1 = hb_session.createQuery(hql_cseva1);
                q_cseva1.setParameter("csevId", csevId);
                rowCountCseva += q_cseva1.executeUpdate();
                
                String hql_cseva2 = "delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId2=:csevId";
                Query q_cseva2 = hb_session.createQuery(hql_cseva2);
                q_cseva2.setParameter("csevId", csevId);
                rowCountCseva += q_cseva2.executeUpdate();
                
                String hql_at = "delete from AssociationType where codeSystemEntityVersionId=:csevId";
                Query q_at = hb_session.createQuery(hql_at);
                q_at.setParameter("csevId", csevId);
                rowCountAt += q_at.executeUpdate();
                
                String hql_csev = "delete from CodeSystemEntityVersion where versionId=:csevId";
                Query q_csev = hb_session.createQuery(hql_csev);
                q_csev.setParameter("csevId", csevId);
                rowCountCsev += q_csev.executeUpdate();
                
                String hql_csvem = "delete from CodeSystemVersionEntityMembership where codeSystemVersionId=:csvId";
                Query q_csvem = hb_session.createQuery(hql_csvem);
                q_csvem.setParameter("csvId", codeSystemVersionId);
                rowCountCsvem += q_csvem.executeUpdate();
                
                String hql_cse = "delete from CodeSystemEntity where id=:cseId";
                Query q_cse = hb_session.createQuery(hql_cse);
                q_cse.setParameter("cseId", cseId);
                rowCountCse += q_cse.executeUpdate();
            }
           
            //lu
            String hql_lu = "delete from LicencedUser where codeSystemVersionId=:csvId";
            Query q_lu = hb_session.createQuery(hql_lu);
            q_lu.setParameter("csvId", codeSystemVersionId);
            rowCountLu += q_lu.executeUpdate();
            
            //lt
            String hql_lt = "delete from LicenceType where codeSystemVersionId=:csvId";
            Query q_lt = hb_session.createQuery(hql_lt);
            q_lt.setParameter("csvId", codeSystemVersionId);
            rowCountLt += q_lt.executeUpdate();
            
            //CSV löschen
            String hql_csv = "delete from CodeSystemVersion where versionId=:versionId";
            Query q_csv = hb_session.createQuery(hql_csv);
            q_csv.setParameter("versionId", codeSystemVersionId);
            rowCountCsv += q_csv.executeUpdate();
            
            result += "csct: " + rowCountCsct + "\n";
            result += "csc: " + rowCountCsc + "\n";
            result += "cvsm: " + rowCountCvsm + "\n";
            result += "csmv: " + rowCountCsmv + "\n";
            result += "cseva: " + rowCountCseva + "\n";
            result += "at: " + rowCountAt + "\n";
            result += "csev: " + rowCountCsev + "\n";
            result += "csvem: " + rowCountCsvem + "\n";
            result += "cse: " + rowCountCse + "\n";
            result += "lu: " + rowCountLu + "\n";
            result += "lt: " + rowCountLt + "\n";
            result += "csv: " + rowCountCsv + "\n";
            
            if(!onlyCSV){
                
                //Check for VSMV Reste and delete
                String hqlMpSearch = "from MetadataParameter where codeSystemId=:csId";
                Query q_MpSearch = hb_session.createQuery(hqlMpSearch);
                q_MpSearch.setParameter("csId", codeSystemId);
                List<MetadataParameter> mpList = q_MpSearch.list();
                
                for(MetadataParameter mp:mpList){
                    String hql_csmv2 = "delete from CodeSystemMetadataValue where metadataParameterId=:mpId";
                    Query q_csmv2 = hb_session.createQuery(hql_csmv2);
                    q_csmv2.setParameter("mpId", mp.getId());
                    rowCountCsmv += q_csmv2.executeUpdate();
                }
                
                //Mp
                String hql_mp = "delete from MetadataParameter where codeSystemId=:csId";
                Query q_mp = hb_session.createQuery(hql_mp);
                q_mp.setParameter("csId", codeSystemId);
                rowCountMp += q_mp.executeUpdate();
                
                //Dvhcs
                Query q_dvhcs = hb_session.createSQLQuery(
                    "delete from domain_value_has_code_system "
                    + "where code_system_id = ?");
                q_dvhcs.setLong(0, codeSystemId);
                rowCountDvhcs += q_dvhcs.executeUpdate();
                
                //CS
                String hql_cs = "delete from CodeSystem where id=:id";
                Query q_cs = hb_session.createQuery(hql_cs);
                q_cs.setParameter("id", codeSystemId);
                rowCountCs += q_cs.executeUpdate();
                
                result += "mp: " + rowCountMp + "\n";
                result += "dvhcs: " + rowCountDvhcs + "\n";
                result += "cs: " + rowCountCs + "\n";
            }
          
            hb_session.getTransaction().commit();
        }
        catch (Exception e)
        {
            hb_session.getTransaction().rollback();
            result += "An Error occured: " + e.getMessage();
        }finally{
            hb_session.close();
        }
        return result;
    }
}
