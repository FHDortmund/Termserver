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

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.DomainValue;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class Test
{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {

    //logger.debug("count: " + values.size());
    String hql = "select distinct dv from DomainValue dv "
            + " left join dv.codeSystems cs"
            + " where cs.id=:id";

    org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();

    try
    {
      System.out.println("hql: " + hql);
      
      Query q = hb_session.createQuery(hql);
      q.setParameter("id", 5l);

      List<DomainValue> dvList_db = q.list();
      System.out.println("count domain values: " + dvList_db.size());

      // check adding new values
      for (DomainValue dv_db : dvList_db)
      {
        System.out.println("dv: " + dv_db.getDomainValueId());
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      hb_session.close();
    }
  }

}
