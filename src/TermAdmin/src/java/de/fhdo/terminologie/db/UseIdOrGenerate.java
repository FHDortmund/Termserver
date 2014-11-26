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

/**
 *
 * @author Robert Mützner
 */
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.terminologie.db.hibernate.TermUser;
import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentityGenerator;

public class UseIdOrGenerate extends IdentityGenerator
{

  @Override
  public Serializable generate(SessionImplementor session, Object obj) throws HibernateException
  {
    if (obj == null)
      throw new HibernateException(new NullPointerException());

    if (obj instanceof TermUser)
    {
      if ((((TermUser) obj).getId()) == null)
      {
        Serializable id = super.generate(session, obj);
        return id;
      }
      else
      {
        return ((TermUser) obj).getId();
      }
    }
    else if (obj instanceof Collaborationuser)
    {
      if ((((Collaborationuser) obj).getId()) == null)
      {
        Serializable id = super.generate(session, obj);
        return id;
      }
      else
      {
        return ((Collaborationuser) obj).getId();
      }
    }
    
    Serializable id = super.generate(session, obj);
    return id;
  }
}
