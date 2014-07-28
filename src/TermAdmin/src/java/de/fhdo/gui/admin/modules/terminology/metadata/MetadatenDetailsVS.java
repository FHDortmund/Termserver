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
package de.fhdo.gui.admin.modules.terminology.metadata;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetMetadataValue;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.helper.HQLParameterHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author PU
 */
public class MetadatenDetailsVS extends GenericForwardComposer{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private Window parentWindow;
    private Window callingWindow;
    private AnnotateDataBinder binder;
    private MetadataParameter metadataParameter;
    private boolean newEntry = false;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp); //To change body of generated methods, choose Tools | Templates.
        parentWindow = (Window)comp;
        callingWindow = (Window)ArgumentHelper.getWindowArgument("MetadatenVS");
        
        long valueSetId = ArgumentHelper.getWindowArgumentLong("valueset_id");
        logger.debug("valueSetId: " + valueSetId);

        Object o = ArgumentHelper.getWindowArgument("mp");
        if (o != null)
        {
          metadataParameter = (MetadataParameter) o;
        }
        
        if (metadataParameter == null)
        {
          metadataParameter = new MetadataParameter();
          metadataParameter.setValueSet(new ValueSet());
          metadataParameter.getValueSet().setId(valueSetId);
          newEntry = true;
        }
        
        binder = new AnnotateDataBinder(comp);
        binder.bindBean("metadataParameter", metadataParameter);
        binder.loadAll(); 
    }

    public void onOkClicked(){
        
        // speichern mit Hibernate
        try
        {
          if (logger.isDebugEnabled())
            logger.debug("Daten speichern");

          Session hb_session = HibernateUtil.getSessionFactory().openSession();
         hb_session.getTransaction().begin();

          try
          {
            if (metadataParameter.getParamName() == null || metadataParameter.getParamName().equals(""))
            {
              Messagebox.show("Es muss ein Wert angegeben werden!", "Warning", Messagebox.OK, Messagebox.EXCLAMATION);
              return;
            }

            // prüfen, ob Wert bereits existiert
            String hql = "from MetadataParameter where valueSetId=:vs_id and paramName=:name";
            org.hibernate.Query q = hb_session.createQuery(hql);
            q.setParameter("vs_id", metadataParameter.getValueSet().getId());
            q.setParameter("name", metadataParameter.getParamName());
            List list = q.list();
            if (list != null && list.size() > 0)
            {
              Messagebox.show("Dieser Parameter ist für das ausgewählte Code System bereits vorhanden!", "Achtung", Messagebox.OK, Messagebox.EXCLAMATION);
              return;
            }


            if (newEntry)
            {
                if (logger.isDebugEnabled())
                  logger.debug("Neuer Eintrag");

                
                MetadataParameter mp = new MetadataParameter();
                mp.setValueSet(metadataParameter.getValueSet());
                mp.setMetadataParameterType(metadataParameter.getMetadataParameterType());
                mp.setParamName(metadataParameter.getParamName());
                mp.setParamDatatype(metadataParameter.getParamDatatype());
                
                // speichern
                hb_session.save(mp);

                String hqlV = "select distinct cvsm from ConceptValueSetMembership cvsm join cvsm.valueSetVersion vsv";
                hqlV += " join vsv.valueSet vs";

                HQLParameterHelper parameterHelper = new HQLParameterHelper();
                parameterHelper.addParameter("vs.", "id", mp.getValueSet().getId());

                // Parameter hinzufügen (immer mit AND verbunden)
                hqlV += parameterHelper.getWhere("");
                logger.debug("HQL: " + hqlV);

                // Query erstellen
                org.hibernate.Query qV = hb_session.createQuery(hqlV);

                // Die Parameter können erst hier gesetzt werden (übernimmt Helper)
                parameterHelper.applyParameter(qV);

                List<ConceptValueSetMembership> cvsmList = qV.list();
                Set<ValueSetMetadataValue> vsmvSet = new HashSet<ValueSetMetadataValue>(0);
                for(ConceptValueSetMembership cvsm:cvsmList){

                    ValueSetMetadataValue vsmv = new ValueSetMetadataValue();
                    vsmv.setMetadataParameter(mp);
                    vsmv.setParameterValue("");
                    vsmv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
                    vsmv.getCodeSystemEntityVersion().setVersionId(cvsm.getCodeSystemEntityVersion().getVersionId());
                    vsmv.setValuesetVersionId(cvsm.getValueSetVersion().getVersionId());
                    hb_session.save(vsmv);
                    vsmvSet.add(vsmv);
                }

                mp.setValueSetMetadataValues(vsmvSet);
                hb_session.update(mp);

              //de.fhdo.db.hibernate.Domain domain = (de.fhdo.db.hibernate.Domain) hb_session.get(de.fhdo.db.hibernate.Domain.class, domainId);
              //domainValue.setDomain(domain);


              //hb_session.save(domainValue);
            }
            else
            {
              hb_session.merge(metadataParameter);
            }


            hb_session.getTransaction().commit();
          }
          catch (Exception e)
          {
            hb_session.getTransaction().rollback();
              logger.error("Fehler in MetadatenDetails.java in onOkClicked(): " + e.getMessage());
            e.printStackTrace();
          }
          finally
          {
            hb_session.close();
          }
          ((MetadatenVS)callingWindow).fillMetadataList();
          parentWindow.setVisible(false);
          parentWindow.detach();
        }
        catch (Exception e)
        {
          // Fehlermeldung ausgeben
          logger.error("Fehler in MetadatenDetails.java: " + e.getMessage());
          e.printStackTrace();
        }
    }
    
    public void onClick$bOk(){ onOkClicked();    } 
}
