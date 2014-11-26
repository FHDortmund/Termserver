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
package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.MaintainConceptAssociationTypeResponse;
import de.fhdo.terminologie.ws.authoring.VersioningType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsRequestType;
import de.fhdo.terminologie.ws.search.ReturnConceptAssociationTypeDetailsResponse;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.AssociationType;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class AssociationDetails extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private IUpdateModal iUpdateListener;
  //private de.fhdo.terminologie.db.hibernate.AssociationType associationType;
  private types.termserver.fhdo.de.AssociationType associationType;
  boolean newEntry = false;

  public AssociationDetails()
  {
    logger.debug("AssociationDetails() - Konstruktor");

    long id = ArgumentHelper.getWindowArgumentLong("id");
    logger.debug("id: " + id);

    try
    {
      if (id > 0)
      {
        newEntry = false;

//        Session hb_session = HibernateUtil.getSessionFactory().openSession();
//        associationType = (de.fhdo.terminologie.db.hibernate.AssociationType) hb_session.get(de.fhdo.terminologie.db.hibernate.SysParam.class, id);
//        Hibernate.initialize(associationType.getCodeSystemEntityVersion());
//        Hibernate.initialize(associationType.getCodeSystemEntityVersion().getCodeSystemEntity());
//        Hibernate.initialize(associationType.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships());
//        hb_session.close();
        ReturnConceptAssociationTypeDetailsRequestType request = new ReturnConceptAssociationTypeDetailsRequestType();
        request.setLoginToken(SessionHelper.getSessionId());
        request.setCodeSystemEntity(new types.termserver.fhdo.de.CodeSystemEntity());
        types.termserver.fhdo.de.CodeSystemEntityVersion csev = new types.termserver.fhdo.de.CodeSystemEntityVersion();
        csev.setVersionId(id);
        request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

        ReturnConceptAssociationTypeDetailsResponse.Return response = WebServiceHelper.returnConceptAssociationTypeDetails(request);

        if (response.getReturnInfos().getStatus() != de.fhdo.terminologie.ws.search.Status.OK)
        {
          Messagebox.show(response.getReturnInfos().getMessage());
          this.detach();
        }
        else
        {
          associationType = response.getCodeSystemEntity().getCodeSystemEntityVersions().get(0).getAssociationTypes().get(0);
        }
      }
      else
      {
        // Neuer Eintrag
        newEntry = true;

        associationType = new AssociationType();
        //associationType.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
        //associationType.getCodeSystemEntityVersion().setCodeSystemEntity(new CodeSystemEntity());
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);

      Messagebox.show(e.getLocalizedMessage());
      this.detach();
    }
  }

  public void afterCompose()
  {

  }

  public void onOkClicked()
  {
    // speichern mit Hibernate

    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Daten speichern");

      //Session hb_session = HibernateUtil.getSessionFactory().openSession();
      //hb_session.getTransaction().begin();
      try
      {
        if (newEntry)
        {
          CreateConceptAssociationTypeRequestType request = new CreateConceptAssociationTypeRequestType();
          request.setLoginToken(SessionHelper.getSessionId());
          request.setCodeSystemEntity(new types.termserver.fhdo.de.CodeSystemEntity());

          types.termserver.fhdo.de.CodeSystemEntityVersion csev = new types.termserver.fhdo.de.CodeSystemEntityVersion();
          request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

          csev.getAssociationTypes().add(associationType);

          CreateConceptAssociationTypeResponse.Return response = WebServiceHelper.createConceptAssociationType(request);

          Messagebox.show(response.getReturnInfos().getMessage());

          /*if (response.getReturnInfos().getStatus() == Status.OK)
          {
            // solve hibernate session problem
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            de.fhdo.terminologie.db.hibernate.AssociationType at = (de.fhdo.terminologie.db.hibernate.AssociationType)
                    hb_session.get(de.fhdo.terminologie.db.hibernate.AssociationType.class, response.getCodeSystemEntity().getCurrentVersionId());
            
            hb_session.update(at);
          }*/
        }
        else
        {
          if (logger.isDebugEnabled())
            logger.debug("Daten aktualisieren");

          //hb_session.merge(associationType);
          MaintainConceptAssociationTypeRequestType request = new MaintainConceptAssociationTypeRequestType();
          request.setLoginToken(SessionHelper.getSessionId());
          request.setCodeSystemEntity(new types.termserver.fhdo.de.CodeSystemEntity());

          types.termserver.fhdo.de.CodeSystemEntityVersion csev = new types.termserver.fhdo.de.CodeSystemEntityVersion();
          csev.setVersionId(associationType.getCodeSystemEntityVersionId());
          request.getCodeSystemEntity().getCodeSystemEntityVersions().add(csev);

          csev.getAssociationTypes().add(associationType);
          
          request.setVersioning(new VersioningType());
          request.getVersioning().setCreateNewVersion(false);
          request.getVersioning().setMajorUpdate(false);
          request.getVersioning().setMinorUpdate(true);

          MaintainConceptAssociationTypeResponse.Return response = WebServiceHelper.maintainConceptAssociationType(request);

          Messagebox.show(response.getReturnInfos().getMessage());

        }

        // hb_session.getTransaction().commit();
      }
      catch (Exception e)
      {
        //hb_session.getTransaction().rollback();
        //  logger.error("Fehler in onOkClicked() bei hibernate: " + e.getMessage());
      }
      finally
      {
        //hb_session.close();
      }

      this.setVisible(false);

      if (iUpdateListener != null)
      {
        iUpdateListener.update(associationType, !newEntry);
      }

      this.detach();
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();
  }

  /**
   * @param iUpdateListener the iUpdateListener to set
   */
  public void setiUpdateListener(IUpdateModal iUpdateListener)
  {
    this.iUpdateListener = iUpdateListener;
  }

  /**
   * @return the associationType
   */
  public AssociationType getAssociationType()
  {
    return associationType;
  }

  /**
   * @param associationType the associationType to set
   */
  public void setAssociationType(AssociationType associationType)
  {
    this.associationType = associationType;
  }

}
