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

import java.io.File;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class HibernateUtil
{

  //private static final SessionFactory sessionFactory = buildSessionFactory();
  private static SessionFactory sessionFactory = null;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private static SessionFactory buildSessionFactory()
  {
    try
    {
      // Configuration path
      String path = System.getProperty("catalina.base") + "/conf/termserver.hibernate.cfg.xml";
       
      logger.info("Hibernate Configuration path: " + path);
      
      // Create the SessionFactory from hibernate.cfg.xml
      File file = new File(path);
      SessionFactory sf = new AnnotationConfiguration().configure(file).buildSessionFactory();
      return sf;
    }
    catch (Exception ex)
    {
      // Make sure you log the exception, as it might be swallowed
      System.err.println("Initial SessionFactory creation failed." + ex.getMessage());
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static SessionFactory getSessionFactory()
  {
    if(sessionFactory == null)
      sessionFactory = buildSessionFactory();
    
    return sessionFactory;
  }
 
}
