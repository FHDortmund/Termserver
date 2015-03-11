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
package de.fhdo.terminologie.db;

import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import java.io.File;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class DBTest
{
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    System.out.println("START DB TEST");
    
    // TODO code application logic here
    String hql = "select  term from CodeSystemConcept term\n"
        + "join fetch term.codeSystemEntityVersion csev\n"
        + "join fetch csev.codeSystemEntity cse\n"
        + "join fetch csev.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 cseva\n"
        + "join fetch cseva.associationType at\n"
        + "join cseva.codeSystemEntityVersionByCodeSystemEntityVersionId1 csev_source\n"
        + "WHERE  csev_source.versionId = 33 AND  csev.statusVisibility = 1";

    Session hb_session = buildSessionFactory().openSession();

    
    System.out.println("HQL: " + hql);
    
    // Query erstellen
    org.hibernate.Query q = hb_session.createQuery(hql);

    // Datenbank-Aufruf durchführen
    List<CodeSystemConcept> list = (java.util.List<CodeSystemConcept>) q.list();
    System.out.println("Size: " + list.size());
    
    
    
  }
  
  private static SessionFactory buildSessionFactory()
  {
    try
    {
      // Configuration path
      String path = "C:\\Privat\\Programmierung\\apache-tomcat-8.0.8\\conf\\termserver.hibernate.cfg.xml";
       
      //logger.info("Hibernate Configuration path: " + path); 
      
      // Create the SessionFactory from hibernate.cfg.xml
      File file = new File(path);
      
      SessionFactory sf = new AnnotationConfiguration().configure(file)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.LicencedUserId.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemConceptTranslation.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.Domain.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.LicencedUser.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemConcept.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.AssociationType.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystem.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.SysParam.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemEntity.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.DomainValue.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.TermUser.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.MetadataParameter.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.Session.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.ValueSet.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembershipId.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemVersion.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.ValueSetVersion.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.LicenceType.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation.class)
              .addAnnotatedClass(de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue.class)
              .buildSessionFactory();
      
      return sf;
    }
    catch (Exception ex)
    {
      // Make sure you log the exception, as it might be swallowed
      System.err.println("Initial SessionFactory creation failed." + ex.getMessage());
      throw new ExceptionInInitializerError(ex);
    }
  }

}
