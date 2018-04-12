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
package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsRequestType;
import de.fhdo.terminologie.ws.administration.types.ActualProceedingsResponseType;
import de.fhdo.terminologie.ws.types.ActualProceeding;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Months;

/**
 *
 * @author Philipp Urbauer
 */
public class ActualProceedings
{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

    private static final String PROCEEDING_NEW = "NEU";
    private static final String PROCEEDING_CHANGED = "GEÄNDERT";
    private static final String PROCEEDING_OBSOLETE = "OBSOLET";
    
    /**
     * Erstellt eine neue Domäne mit den angegebenen Parametern
     * 
     * @param parameter
     * @return Antwort des Webservices
     */
    public ActualProceedingsResponseType ActualProceedings(ActualProceedingsRequestType parameter, String ipAddress)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("====== Actual Proceedings gestartet ======");
        }

        ActualProceedingsResponseType response = new ActualProceedingsResponseType();
        response.setReturnInfos(new ReturnType());
        List<ActualProceeding> apList = new ArrayList<ActualProceeding>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss");
        Integer months = 6;
       
        try
        {
            Session hb_session = HibernateUtil.getSessionFactory().openSession();
            //hb_session.getTransaction().begin();

            try // 2. try-catch-Block zum Abfangen von Hibernate-Fehlern
            {
                
                //CodeSystemVersion
                String hqlCsv = "select distinct csv from CodeSystemVersion csv join fetch csv.codeSystem cs where cs.currentVersionId=csv.versionId";
                Query qCsv = hb_session.createQuery(hqlCsv);
                List<CodeSystemVersion> csvList = qCsv.list();
                
                for(CodeSystemVersion csv:csvList){
                
                    ActualProceeding ap = new ActualProceeding();
                    
                    ap.setTerminologieName(csv.getCodeSystem().getName());
                    ap.setTerminologieVersionName(csv.getName());
                    ap.setTerminologieType("Code System");
                    
                    Date insertDate =  csv.getInsertTimestamp();
                    Date lastChangeDate = csv.getLastChangeDate();
                    Date statusDate = csv.getStatusDate();       
                    
                    if(lastChangeDate != null){  //Änderung
                        if(csv.getStatus() == 2){ // Obsolet
                            ap.setStatus(PROCEEDING_OBSOLETE);
                            ap.setLastChangeDate(sdf.format(statusDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months){
                                apList.add(ap);
                            }
                        }else{ // Änderung
                            ap.setStatus(PROCEEDING_CHANGED);
                            ap.setLastChangeDate(sdf.format(lastChangeDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(lastChangeDate)).getMonths()) <= months){
                                apList.add(ap);
                            } 
                        }
                    }else{     
                        if(csv.getStatus() == 2){
                            ap.setStatus(PROCEEDING_OBSOLETE);
                            ap.setLastChangeDate(sdf.format(statusDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months){
                                apList.add(ap);
                            }
                        }else{
                            //New
                            ap.setStatus(PROCEEDING_NEW);
                            ap.setLastChangeDate(sdf.format(insertDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(insertDate)).getMonths()) <= months){
                                apList.add(ap);
                            }
                        }
                    }
                }
                
                //ValueSetVersion
                String hqlVsv = "select distinct vsv from ValueSetVersion vsv join fetch vsv.valueSet vs where vs.currentVersionId=vsv.versionId";
                Query qVsv = hb_session.createQuery(hqlVsv);
                List<ValueSetVersion> vsvList = qVsv.list();
                
                for(ValueSetVersion vsv:vsvList){
                
                    ActualProceeding ap = new ActualProceeding();
                    
                    ap.setTerminologieName(vsv.getValueSet().getName());
                    if(vsv.getName() != null)
                        ap.setTerminologieVersionName(vsv.getName());
                    else{
                        ap.setTerminologieVersionName("");
                    }
                    ap.setTerminologieType("Value Set");
                    
                    Date insertDate =  vsv.getInsertTimestamp();
                    Date lastChangeDate = vsv.getLastChangeDate();
                    Date statusDate = vsv.getStatusDate();       
                    
                    if(lastChangeDate != null){  //Änderung
                        if(vsv.getStatus() == 2){ // Obsolet
                            ap.setStatus(PROCEEDING_OBSOLETE);
                            ap.setLastChangeDate(sdf.format(statusDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months){
                                apList.add(ap);
                            }
                        }else{ // Änderung
                            ap.setStatus(PROCEEDING_CHANGED);
                            ap.setLastChangeDate(sdf.format(lastChangeDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(lastChangeDate)).getMonths()) <= months){
                                apList.add(ap);
                            } 
                        }
                    }else{     
                        if(vsv.getStatus() == 2){
                            ap.setStatus(PROCEEDING_OBSOLETE);
                            ap.setLastChangeDate(sdf.format(statusDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(statusDate)).getMonths()) <= months){
                                apList.add(ap);
                            }
                        }else{
                            //New
                            ap.setStatus(PROCEEDING_NEW);
                            ap.setLastChangeDate(sdf.format(insertDate));
                            if(Math.abs(Months.monthsBetween(new DateTime(new Date()), new DateTime(insertDate)).getMonths()) <= months){
                                apList.add(ap);
                            }
                        }
                    }
                }
                
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
                response.getReturnInfos().setStatus(ReturnType.Status.OK);
                String message = "Abfrage Erfolgreich!";
                response.getReturnInfos().setMessage(message);
                response.setActualProceedings(apList);
                //hb_session.getTransaction().commit();
            } catch (Exception e)
            {
                hb_session.getTransaction().rollback();
                // Fehlermeldung an den Aufrufer weiterleiten
                response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
                response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
                String message = "Fehler bei 'ActualProceedings', Hibernate: " + e.getLocalizedMessage();
                response.getReturnInfos().setMessage(message);
                logger.error(message);
            }finally{
                hb_session.close();
            }
        } catch (Exception e)
        {
            // Fehlermeldung an den Aufrufer weiterleiten
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
            response.getReturnInfos().setMessage("Fehler bei 'CreateDomain': " + e.getLocalizedMessage());
            logger.error("Fehler bei 'CreateDomain': " + e.getLocalizedMessage());
        }

        return response;
    }
}
