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
package de.fhdo.collaboration.db;

import java.io.File;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Robert Mützner
 */
public class HibernateUtil
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private static SessionFactory sessionFactory = null;

  private static SessionFactory buildSessionFactory()
  {
    try
    {
      String path = System.getProperty("catalina.base") + "/conf/collaboration.hibernate.cfg.xml";

      // Create the SessionFactory from hibernate.cfg.xml
      File file = new File(path);

      SessionFactory sf = createSessionFactory(new AnnotationConfiguration().configure(file));

      //SessionFactory sf = new AnnotationConfiguration().configure(file).buildSessionFactory();
      //return new Configuration().configure().buildSessionFactory();
      return sf;
    }
    catch (Throwable ex)
    {
      // Make sure you log the exception, as it might be swallowed
      System.err.println("Initial SessionFactory creation failed." + ex);
      throw new ExceptionInInitializerError(ex);
    }
  }

  private static SessionFactory createSessionFactory(AnnotationConfiguration aconfig)
  {
    return aconfig
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Action.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.AssignedTerm.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.ClassAttribute.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Collaborationuser.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Discussion.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Discussiongroup.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Discussiongroupobject.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Domain.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.DomainValue.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Enquiry.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.File.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Link.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Organisation.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Privilege.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Proposal.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Proposalobject.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Proposalstatuschange.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Quote.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Rating.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Role.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Status.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.Statusrel.class)
        .addAnnotatedClass(de.fhdo.collaboration.db.classes.SysParam.class)
        .buildSessionFactory();
  }

  public static SessionFactory getSessionFactory()
  {
    if (sessionFactory == null)
      sessionFactory = buildSessionFactory();
    
    logger.debug("getSessionFactory() collab");

    return sessionFactory;
  }

}
