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

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Philipp Urbauer edited 2014-09-30 by Robert Mützner
 * <robert.muetzner@fh-dortmund.de>
 */
public class DeleteTermHelperWS
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static String deleteVS_VSV(Session hb_session, Boolean onlyVSV, Long valueSetId, Long valueSetVersionId)
  {
    //get Missing Id's for all CVSM
    String result = "\n";
    //List<ConceptValueSetMembership> cvsmList = null;
    //Session hb_session = HibernateUtil.getSessionFactory().openSession();
    int rowCountVsmv = 0;
    int rowCountCvsm = 0;
    int rowCountVsv = 0;
    int rowCountMp = 0;
    int rowCountVs = 0;

    //try
    //{
    ValueSet vs = (ValueSet) hb_session.get(ValueSet.class, valueSetId);

    if (vs == null)
      return "Value Set with given id '" + valueSetId + "' does not exist.";

    if (valueSetVersionId == null)
    {

      for (ValueSetVersion vsv : vs.getValueSetVersions())
      {
        result += "Version: " + vsv.getName();
        result += deleteVS_VSV(hb_session, false, valueSetId, vsv.getVersionId());
      }

      // Check for VSMV pieces and delete
      //String sql = "DELETE FROM metadata_parameter WHERE valueSetId=" + valueSetId;
      String sql = "UPDATE metadata_parameter SET valueSetId=null WHERE valueSetId=" + valueSetId;
      logger.debug("SQL: " + sql);
      rowCountVsmv += hb_session.createSQLQuery(sql).executeUpdate();

      /*String hqlMpSearch = "from MetadataParameter where valueSetId=:vsId";
       Query q_MpSearch = hb_session.createQuery(hqlMpSearch);
       q_MpSearch.setParameter("vsId", valueSetId);
       List<MetadataParameter> mpList = q_MpSearch.list();

       for (MetadataParameter mp : mpList)
       {
       String hql_vsmv2 = "delete from ValueSetMetadataValue where metadataParameterId=:mpId";
       Query q_vsmv2 = hb_session.createQuery(hql_vsmv2);
       q_vsmv2.setParameter("mpId", mp.getId());
       rowCountVsmv += q_vsmv2.executeUpdate();
       }*/
        //Mp
        /*String hql_mp = "delete from MetadataParameter where valueSetId=:vsId";
       Query q_mp = hb_session.createQuery(hql_mp);
       q_mp.setParameter("vsId", valueSetId);
       rowCountMp += q_mp.executeUpdate();*/
      // Value Set löschen
      logger.debug("HQL: delete from ValueSet where id=:vsId");
      String hql_vs = "delete from ValueSet where id=:vsId";
      Query q_vs = hb_session.createQuery(hql_vs);
      q_vs.setParameter("vsId", valueSetId);
      rowCountVs += q_vs.executeUpdate();

      result += "mp: " + rowCountMp + "\n";
      result += "vs: " + rowCountVs + "\n\n";
    }
    else
    {

      if (vs.getValueSetVersions().size() == 1 && onlyVSV)
        return "Ein Value Set MUSS eine Version haben! \nBitte legen sie eine neue an bevor sie diese löschen. \nSie können alternativ das gesamte Value Set löschen.";

      logger.debug("DELETE FROM value_set_metadata_value WHERE valueSetVersionId=" + valueSetVersionId);
      rowCountVsmv += hb_session.createSQLQuery("DELETE FROM value_set_metadata_value WHERE valueSetVersionId=" + valueSetVersionId).executeUpdate();

      logger.debug("DELETE FROM concept_value_set_membership WHERE valueSetVersionId=" + valueSetVersionId);
      rowCountCvsm += hb_session.createSQLQuery("DELETE FROM concept_value_set_membership WHERE valueSetVersionId=" + valueSetVersionId).executeUpdate();


      /*String hqlCvsm = "select distinct cvsm from ConceptValueSetMembership cvsm join cvsm.valueSetVersion vsv where vsv.versionId=:valueSetVersionId";
       Query q_Cvsm = hb_session.createQuery(hqlCvsm);
       q_Cvsm.setParameter("valueSetVersionId", valueSetVersionId);
       cvsmList = q_Cvsm.list();
       //Start deleting

       for (ConceptValueSetMembership cvsm : cvsmList)
       {

       String hql_vsmv = "select distinct vsmv from ValueSetMetadataValue vsmv join fetch vsmv.codeSystemEntityVersion csev where vsmv.valuesetVersionId=:vsvId and csev.versionId=:csevId";
       Query q_vsmv = hb_session.createQuery(hql_vsmv);
       q_vsmv.setParameter("vsvId", cvsm.getValueSetVersion().getVersionId());
       q_vsmv.setParameter("csevId", cvsm.getCodeSystemEntityVersion().getVersionId());
       List<ValueSetMetadataValue> listVsmv = q_vsmv.list();

       for (ValueSetMetadataValue vsmv : listVsmv)
       {

       hb_session.delete(vsmv);
       ++rowCountVsmv;
       }

       try
       {
       hb_session.delete(cvsm);
       ++rowCountCvsm;
       }
       catch (Exception ex)
       {
       }
       }*/
      logger.debug("HQL: delete from ValueSetVersion where versionId=:vsvId");
      String hql_vsv = "delete from ValueSetVersion where versionId=:vsvId";
      Query q_vsv = hb_session.createQuery(hql_vsv);
      q_vsv.setParameter("vsvId", valueSetVersionId);
      rowCountVsv += q_vsv.executeUpdate();

      result += "Value Set: " + vs.getName() + "\n";
      result += "vsmv: " + rowCountVsmv + "\n";
      result += "cvsm: " + rowCountCvsm + "\n";
      result += "vsv: " + rowCountVsv + "\n\n";
    }
    /*}
     catch (Exception e)
     {
     result += "An Error occured: " + e.getMessage();
     }*/

    return result;
  }

  private static int removeConcept(long cseId, long csevId, long codeSystemVersionId, Session hb_session)
  {
    int count = 0;
    //logger.debug("removeConcept with cse-version-id: " + csevId);

    // DON'T delete associated concepts (no need to and maybe cross mapping)
    /*String hql = "select csev.versionId,csev.codeSystemEntity.id from CodeSystemEntityVersion csev "
               + " join csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 cseva where codeSystemEntityVersionId1=:versionId";
    
    Query q_CsevNumber = hb_session.createQuery(hql);
    q_CsevNumber.setParameter("versionId", csevId);
    List csevIds = q_CsevNumber.list();

    for (Object o : csevIds)
    {
      Object[] x = (Object[]) o;
      Long subCsevId = (Long) x[0];
      Long subCseId = (Long) x[1];
      
      //logger.debug("Found sub concept with csev-id: " + subCsevId);
      
      count += removeConcept(subCseId, subCsevId, codeSystemVersionId, hb_session);
    }*/
    
    // remove content
    String hql_csct = "delete from CodeSystemConceptTranslation where codeSystemConcept.codeSystemEntityVersionId=:csevId";
    Query q_csct = hb_session.createQuery(hql_csct);
    q_csct.setParameter("csevId", csevId);
    q_csct.executeUpdate();

    String hql_csc = "delete from CodeSystemConcept where codeSystemEntityVersionId=:csevId";
    Query q_csc = hb_session.createQuery(hql_csc);
    q_csc.setParameter("csevId", csevId);
    count += q_csc.executeUpdate();

    String hql_cvsm = "delete from ConceptValueSetMembership where codeSystemEntityVersionId=:csevId";
    Query q_cvsm = hb_session.createQuery(hql_cvsm);
    q_cvsm.setParameter("csevId", csevId);
    q_cvsm.executeUpdate();

    String hql_csmv = "delete from CodeSystemMetadataValue where codeSystemEntityVersionId=:csevId";
    Query q_csmv = hb_session.createQuery(hql_csmv);
    q_csmv.setParameter("csevId", csevId);
    q_csmv.executeUpdate();

    String hql_cseva1 = "delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId1=:csevId";
    Query q_cseva1 = hb_session.createQuery(hql_cseva1);
    q_cseva1.setParameter("csevId", csevId);
    q_cseva1.executeUpdate();

    String hql_cseva2 = "delete from CodeSystemEntityVersionAssociation where codeSystemEntityVersionId2=:csevId";
    Query q_cseva2 = hb_session.createQuery(hql_cseva2);
    q_cseva2.setParameter("csevId", csevId);
    q_cseva2.executeUpdate();

    String hql_at = "delete from AssociationType where codeSystemEntityVersionId=:csevId";
    Query q_at = hb_session.createQuery(hql_at);
    q_at.setParameter("csevId", csevId);
    q_at.executeUpdate();

    String hql_csev = "delete from CodeSystemEntityVersion where versionId=:csevId";
    Query q_csev = hb_session.createQuery(hql_csev);
    q_csev.setParameter("csevId", csevId);
    q_csev.executeUpdate();

    String hql_csvem = "delete from CodeSystemVersionEntityMembership where codeSystemVersionId=:csvId";
    Query q_csvem = hb_session.createQuery(hql_csvem);
    q_csvem.setParameter("csvId", codeSystemVersionId);
    q_csvem.executeUpdate();

    String hql_cse = "delete from CodeSystemEntity where id=:cseId";
    Query q_cse = hb_session.createQuery(hql_cse);
    q_cse.setParameter("cseId", cseId);
    q_cse.executeUpdate();
    
    return count;
  }

  public static String deleteCS_CSV(Session hb_session, Boolean onlyCSV, Long codeSystemId, Long codeSystemVersionId)
  {

    //get Missing Id's for all CSEV
    String result = "\n";
    List csevIds = null;

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

    //try
    // {
    CodeSystem cs = (CodeSystem) hb_session.get(CodeSystem.class, codeSystemId);

    if (codeSystemVersionId == null)
    {
      for (CodeSystemVersion csv : cs.getCodeSystemVersions())
      {
        result += "Version: " + csv.getName();
        result += deleteCS_CSV(hb_session, false, codeSystemId, csv.getVersionId());
      }

      //Check for VSMV Reste and delete
      String hqlMpSearch = "from MetadataParameter where codeSystemId=:csId";
      Query q_MpSearch = hb_session.createQuery(hqlMpSearch);
      q_MpSearch.setParameter("csId", codeSystemId);
      List<MetadataParameter> mpList = q_MpSearch.list();

      for (MetadataParameter mp : mpList)
      {
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

      result += "CodeSystem: " + cs.getName() + "\n";
      result += "mp: " + rowCountMp + "\n";
      result += "dvhcs: " + rowCountDvhcs + "\n";
      result += "cs: " + rowCountCs + "\n\n";

      //hb_session.getTransaction().commit();
    }
    else
    {

      if (cs.getCodeSystemVersions().size() < 1 && onlyCSV)
        //return "Ein Code System MUSS eine Version haben! \nBitte legen sie eine neue an bevor sie diese löschen. \nSie können alternativ das gesamte Code System löschen.";
        return "A code system must have a version.";

      String hqlCsevNumber = "select csev.versionId,csev.codeSystemEntity.id from CodeSystemEntityVersion csev join csev.codeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem";
      hqlCsevNumber += " join csvem.codeSystemVersion csv where csv.versionId=:versionId";
      Query q_CsevNumber = hb_session.createQuery(hqlCsevNumber);
      q_CsevNumber.setParameter("versionId", codeSystemVersionId);
      csevIds = q_CsevNumber.list();
      //Start deleting
      int count = 0;

      for (Object o : csevIds)
      {
        if (count % 100 == 0)
          logger.debug("Count: " + count);

        Object[] x = (Object[]) o;
        Long csevId = (Long) x[0];
        Long cseId = (Long) x[1];

        rowCountCsc += removeConcept(cseId, csevId, codeSystemVersionId, hb_session);

        count++;
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

      result += "Codesystem Version successfully deleted.\n\n";
      result += "Concepts: " + rowCountCsc + "\n";
      //result += "Translations: " + rowCountCsct + "\n";

//        result += "csct: " + rowCountCsct + "\n";
//        result += "csc: " + rowCountCsc + "\n";
//        result += "cvsm: " + rowCountCvsm + "\n";
//        result += "csmv: " + rowCountCsmv + "\n";
//        result += "cseva: " + rowCountCseva + "\n";
//        result += "at: " + rowCountAt + "\n";
//        result += "csev: " + rowCountCsev + "\n";
//        result += "csvem: " + rowCountCsvem + "\n";
//        result += "cse: " + rowCountCse + "\n";
//        result += "lu: " + rowCountLu + "\n";
//        result += "lt: " + rowCountLt + "\n";
//        result += "csv: " + rowCountCsv + "\n\n";
      //hb_session.getTransaction().commit();
    }
//    }
//    catch (Exception e)
//    {
//      //hb_session.getTransaction().rollback();
//      result += "An Error occured: " + e.getMessage();
//    }
//    finally
//    {
//      //hb_session.close();
//    }
    return result;
  }

  public static String deleteCSEV(Session hb_session, Long csevId, Long csvId)
  {

    //get Missing Id's for all CSEV
    String result = "\n";
    Boolean deleteCse = false;
    //Session hb_session = HibernateUtil.getSessionFactory().openSession();

    //try
    //{
    //Start deleting
    //hb_session.getTransaction().begin();
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

    CodeSystemEntityVersion csev = (CodeSystemEntityVersion) hb_session.get(CodeSystemEntityVersion.class, csevId);
    CodeSystemEntity cse = csev.getCodeSystemEntity();
    if (cse.getCodeSystemEntityVersions().size() == 1)
      deleteCse = true;

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

    if (deleteCse)
    {

      String hql_csvem = "delete from CodeSystemVersionEntityMembership where codeSystemVersionId=:csvId and codeSystemEntityId=:cseId";
      Query q_csvem = hb_session.createQuery(hql_csvem);
      q_csvem.setParameter("csvId", csvId);
      q_csvem.setParameter("cseId", cse.getId());
      rowCountCsvem += q_csvem.executeUpdate();

      String hql_cse = "delete from CodeSystemEntity where id=:cseId";
      Query q_cse = hb_session.createQuery(hql_cse);
      q_cse.setParameter("cseId", cse.getId());
      rowCountCse += q_cse.executeUpdate();
    }

    result += "csct: " + rowCountCsct + "\n";
    result += "csc: " + rowCountCsc + "\n";
    result += "cvsm: " + rowCountCvsm + "\n";
    result += "csmv: " + rowCountCsmv + "\n";
    result += "cseva: " + rowCountCseva + "\n";
    result += "at: " + rowCountAt + "\n";
    result += "csev: " + rowCountCsev + "\n";

    if (deleteCse)
    {
      result += "csvem: " + rowCountCsvem + "\n";
      result += "cse: " + rowCountCse + "\n";
    }
    //hb_session.getTransaction().commit();
//    }
//    catch (Exception e)
//    {
//      //hb_session.getTransaction().rollback();
//      result += "An Error occured: " + e.getMessage();
//    }

    return result;
  }
}
