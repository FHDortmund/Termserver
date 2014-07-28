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
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
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
      /**
       * Configuration*********************************************************************************************************
       */
      String path = System.getProperty("catalina.base") + "/conf/termserver.hibernate.cfg.xml";

      File file = new File(path);
      SessionFactory sf = new AnnotationConfiguration().configure(file).buildSessionFactory();

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
    if (sessionFactory == null)
      sessionFactory = buildSessionFactory();

    return sessionFactory;
  }

}
