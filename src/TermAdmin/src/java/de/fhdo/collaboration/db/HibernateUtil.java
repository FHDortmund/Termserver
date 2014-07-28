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
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author Robert Mützner
 */
public class HibernateUtil
{
  private static SessionFactory sessionFactory = null;

  private static SessionFactory buildSessionFactory()
  {
    try
    {
      
      /*Configuration************************************************************************************************************/  
      /**/ String path = System.getProperty("catalina.base") + "/conf/kollaboration.hibernate.cfg.xml";       // test
      /*Productive_AT_PU*********************************************************************************************************/
      /**/ //String path = System.getProperty("catalina.base") + "/conf/kollaborationPub.hibernate.cfg.xml";    // test public
      /**/ //String path = "/data0/web/tomcat_pub/conf/kollaborationPub.hibernate.cfg.xml";                     // public prod
      /**/ //String path = "/data0/web/tomcat_col/conf/kollaboration.hibernate.cfg.xml";                        // kollab prod
      /**************************************************************************************************************************/
      /**/  //String path = "/data0/web/tomcat_term1/conf/kollaboration.hibernate.cfg.xml";                     // testSystem BRZ
      /**************************************************************************************************************************/  
      
      // Create the SessionFactory from hibernate.cfg.xml
      File file = new File(path);
      SessionFactory sf = new AnnotationConfiguration().configure(file).buildSessionFactory();
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

  public static SessionFactory getSessionFactory()
  {
      if(sessionFactory == null)
          sessionFactory = buildSessionFactory();
      
      return sessionFactory;
  }
  
}
