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
package de.fhdo.terminologie.ws.administration._export;

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.types.ExportCodeSystemContentResponseType;
import de.fhdo.terminologie.ws.search.ReturnCodeSystemDetails;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsRequestType;
import de.fhdo.terminologie.ws.search.types.ReturnCodeSystemDetailsResponseType;
import de.fhdo.terminologie.ws.types.ExportType;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */

public class ExportOWL
{
  private static Logger logger = Logger4j.getInstance().getLogger();
  ExportCodeSystemContentRequestType parameter;
  private int countExported = 0;
  private HashMap<Integer, String> paramList = null;
  private org.hibernate.Session hb_session = null;
  private OWLOntologyManager oom = null;
  private OWLDataFactory df = OWLManager.getOWLDataFactory();
  private OWLOntology ontology = null;
  private IRI iri = null;
  private CodeSystemVersion csv;
  private Set<CodeSystemEntityVersion> csevSet = new HashSet<CodeSystemEntityVersion>();
  private ExportCodeSystemContentResponseType reponse;
  ExportType exportType = new ExportType();
    
  public ExportOWL(ExportCodeSystemContentRequestType _parameter)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ExportOWL gestartet ======");

    parameter = _parameter;
  }
  
  public String exportOWL(ExportCodeSystemContentResponseType reponse)
  {    
    
    this.reponse = reponse;
    String s = "";    
    paramList = new HashMap<Integer, String>();
    hb_session = HibernateUtil.getSessionFactory().openSession();
    
    try
    {
        //Request-Parameter für ReturnCodeSystemDetails erstellen
        logger.info("[ExportOWL] Erstelle Request-Parameter für ReturnCodeSystemDetails");

        ReturnCodeSystemDetailsRequestType requestCodeSystemDetails = new ReturnCodeSystemDetailsRequestType();
        requestCodeSystemDetails.setCodeSystem(parameter.getCodeSystem());
        if(requestCodeSystemDetails.getCodeSystem() != null && requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions() != null)
            requestCodeSystemDetails.getCodeSystem().getCodeSystemVersions().add((CodeSystemVersion) parameter.getCodeSystem().getCodeSystemVersions().toArray()[0]);
        requestCodeSystemDetails.setLoginToken(parameter.getLoginToken());

        //CodeSystemDetails abrufen
        ReturnCodeSystemDetails rcsd = new ReturnCodeSystemDetails();
        ReturnCodeSystemDetailsResponseType responseCodeSystemDetails = rcsd.ReturnCodeSystemDetails(requestCodeSystemDetails, "");                
               
        String hql = "select distinct csv from CodeSystemVersion csv join csv.codeSystem cs"
        + " where cs.id=:id and"
        + " csv.versionId=:versionId";
        
        org.hibernate.Query q = hb_session.createQuery(hql);
        q.setLong("id", parameter.getCodeSystem().getId());
        q.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId()); 
        
        List<CodeSystemVersion> csvList = q.list();
        csv = null;
        if (csvList != null && csvList.size() == 1)
        {
          csv = csvList.get(0);
        }
        
        //Header Informationen Auswerten
        logger.debug("Headerinformationen Auswerten");
        owlHeaderErstellen();
        
         String hqlC = "select distinct cse from CodeSystemEntity cse join cse.codeSystemVersionEntityMemberships csvem join csvem.codeSystemVersion csv join cse.codeSystemEntityVersions csev join csev.codeSystemConcepts csc"
                + " where csv.versionId=:versionId";

        if (parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null)
        {
          // Datum für Synchronisation hinzufügen
          hqlC += " and csev.statusVisibilityDate>:dateFrom";
        }
        
        org.hibernate.Query qC = hb_session.createQuery(hqlC);
        qC.setLong("versionId", parameter.getCodeSystem().getCodeSystemVersions().iterator().next().getVersionId());

        if (parameter.getExportParameter() != null && parameter.getExportParameter().getDateFrom() != null)
        {
          // Datum für Synchronisation hinzufügen
          qC.setDate("dateFrom", parameter.getExportParameter().getDateFrom());
          logger.debug("Snych-Zeit: " + parameter.getExportParameter().getDateFrom().toString());
        }
        else
          logger.debug("keine Snych-Zeit angegeben");

        List<CodeSystemEntity> cselist = qC.list();
        
        //Concepte zu OWL Klassen wandeln
        logger.debug("Concepte zu OWL Klassen wandeln");
        owlKlassenErstellen(cselist);
        
        //Assoziationen zwischen den Klassen erstellen 
        logger.debug("Assoziationen erstellen");
        assoziationenfestlegen();
       
        //ExportInfos in Response schreiben
        reponse.setExportInfos(exportType);
        
        //Datei erstellen und versenden 
        logger.debug("Datei erstellen und versenden");
        owlDateiErstellen();
    }
    catch (Exception ex)
    {
      s = "Fehler: " + ex.getLocalizedMessage();
      ex.printStackTrace();
    }

    //hb_session.getTransaction().commit();
    hb_session.close();
    return s;  
  }
  
  public void owlHeaderErstellen()          
  {
      oom = OWLManager.createOWLOntologyManager();
      try
      {
        //Namen des Codesystems übernehmen
        iri = IRI.create("http://www.fh-dortmund.de/Terminologieserver/Terminologie/" + csv.getCodeSystem().getName() + ".owl");
        OWLOntology o = oom.createOntology(iri);
        
        //Beschreibung des CodeSystems übernehmen
        
        OWLAnnotation commentanno = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(csv.getCodeSystem().getDescription()));
        OWLAxiom cax = df.getOWLAnnotationAssertionAxiom(iri, commentanno);
        oom.applyChange(new AddAxiom(o, cax));
        
        //Version des CodeSystems übernehmen
        OWLAnnotation versionInfo = df.getOWLAnnotation(df.getOWLVersionInfo(), df.getOWLLiteral(csv.getName()));
        OWLAxiom viax = df.getOWLAnnotationAssertionAxiom(iri, versionInfo);
        oom.applyChange(new AddAxiom(o, viax));
        
      }catch(Exception e)
      {
          logger.error("Fehler beim erstellen des OWL Ontology Headers");
      }   
  }
  
  public void owlKlassenErstellen(List<CodeSystemEntity> cse)
  {      
      Set<CodeSystemConcept> cscSet = new HashSet<CodeSystemConcept>();
      
      //Einbinden der benötigten Klassen
      for(CodeSystemEntity name: cse)
      {
          csevSet = name.getCodeSystemEntityVersions();
      }
       
      for(CodeSystemEntityVersion name1: csevSet)
      {          
          cscSet = name1.getCodeSystemConcepts();
      }
      
      //Auswerten der CodeSystemConcept Klasse und Klassen in OWL generieren 
      for(CodeSystemConcept name2: cscSet)
      {   
          //Klasse erstellen mit Code als Namen
          IRI classIri = IRI.create(iri + "#" + name2.getCode());
          OWLClass temp = df.getOWLClass(classIri);
          OWLDeclarationAxiom da = df.getOWLDeclarationAxiom(temp);
          oom.addAxiom(ontology, da);
          
          //Label an Klasse anhängen mit Term als Inhalt
          OWLAnnotation classlabel = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(name2.getTerm()));
          OWLAxiom clax = df.getOWLAnnotationAssertionAxiom(classIri, classlabel);
          oom.applyChange(new AddAxiom(ontology, clax));
          
          //Comment mit description befüllen und an Klasse anhängen
          OWLAnnotation classcomment = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(name2.getDescription()));
          OWLAxiom ccax = df.getOWLAnnotationAssertionAxiom(classIri, classcomment);
          oom.applyChange(new AddAxiom(ontology, ccax)); 
          
          countExported++;          
          logger.debug("Konzept erstellt, Gesamt: ");
      }
      logger.debug("Gesamte Exportierte Concepte: " + getCountExported());    
  }
  
  public void assoziationenfestlegen()
  {
    Set<CodeSystemEntityVersionAssociation> csevaSet = new HashSet<CodeSystemEntityVersionAssociation>();      
      
    for(CodeSystemEntityVersion name: csevSet)
    {          
        csevaSet = name.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1();
    }
      
    for(CodeSystemEntityVersionAssociation name1: csevaSet)
    {
        String forward = name1.getAssociationType().getForwardName();          
        String reverse = name1.getAssociationType().getReverseName();
        OWLObjectProperty tempop = null;
          
          
          
        if(!(forward.isEmpty()))          
        {
              
            Set<OWLObjectProperty> oP = ontology.getObjectPropertiesInSignature();                             
                
            boolean schonDa = false;
            for(OWLObjectPropertyExpression name: oP) 
            {
                if(name.getNamedProperty().getIRI().toString().equals(iri + "#" + forward))
                {
                    schonDa = true;
                    tempop = df.getOWLObjectProperty(IRI.create(iri + "#" + forward));
                }
            }
            
            //Erstellen der Klassenhierachie
            if(name1.getAssociationKind() == 2)
            {
                OWLClass klasse1 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId1()));
                OWLClass klasse2 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId2()));
              
                OWLAxiom ax = df.getOWLSubClassOfAxiom(klasse1, klasse2);
                AddAxiom adax = new AddAxiom(ontology, ax);
                oom.applyChange(adax);              
            }
          
            //Erstellen der restlichen Eigenschaften
            //Erstellen der Symmetrischen Eigenschaften
            if(forward.equals(reverse) && name1.getAssociationKind() == 1)
            {                
                OWLClass klasse1 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId1()));
                OWLClass klasse2 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId2()));              
                
                if(!schonDa)
                {
                    tempop = df.getOWLObjectProperty(IRI.create(iri + "#" + forward));                            
                    OWLSymmetricObjectPropertyAxiom syax = df.getOWLSymmetricObjectPropertyAxiom(tempop);
                    oom.applyChange(new AddAxiom(ontology, syax));
                }
                
                    OWLObjectPropertyRangeAxiom axiom2 = df.getOWLObjectPropertyRangeAxiom(tempop, klasse2);
                    OWLObjectPropertyDomainAxiom axiom1 = df.getOWLObjectPropertyDomainAxiom(tempop, klasse1);                        
                    oom.applyChange(new AddAxiom(ontology, axiom1));
                    oom.applyChange(new AddAxiom(ontology, axiom2));                            
            }
            
            //Erstellen von inversen Eigenschaften
            if(!(reverse.isEmpty()) && !(forward.equals(reverse)) && name1.getAssociationKind() == 1)
            {
                
                OWLClass klasse1 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId1()));
                OWLClass klasse2 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId2()));                                                  
                
                if(!schonDa)
                {
                    tempop = df.getOWLObjectProperty(IRI.create(iri + "#" + forward));
                    OWLObjectProperty tempre = df.getOWLObjectProperty(IRI.create(iri + "#" + reverse));
                    OWLInverseObjectPropertiesAxiom iopa = df.getOWLInverseObjectPropertiesAxiom(tempop, tempre);                                       
                    oom.applyChange(new AddAxiom(ontology, iopa));
                }
                
                OWLObjectPropertyRangeAxiom axiom2 = df.getOWLObjectPropertyRangeAxiom(tempop, klasse2);
                OWLObjectPropertyDomainAxiom axiom1 = df.getOWLObjectPropertyDomainAxiom(tempop, klasse1);                        
                oom.applyChange(new AddAxiom(ontology, axiom1));
                oom.applyChange(new AddAxiom(ontology, axiom2)); 
            }
            
            //Erstellen von einfachen Eigenschaften
            if(reverse.isEmpty() && name1.getAssociationKind() == 1)
            {                
                OWLClass klasse1 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId1()));
                OWLClass klasse2 = df.getOWLClass(IRI.create(iri + "#" + name1.getCodeSystemEntityVersionByCodeSystemEntityVersionId2()));                                
                
                if(!schonDa)
                {
                    tempop = df.getOWLObjectProperty(IRI.create(iri + "#" + forward));                    
                }
                
                OWLObjectPropertyRangeAxiom axiom2 = df.getOWLObjectPropertyRangeAxiom(tempop, klasse2);
                OWLObjectPropertyDomainAxiom axiom1 = df.getOWLObjectPropertyDomainAxiom(tempop, klasse1);                        
                oom.applyChange(new AddAxiom(ontology, axiom1));
                oom.applyChange(new AddAxiom(ontology, axiom2));                            
            }                                   
        }
          
    }
      
      
  }
  
  public void owlDateiErstellen()
  {     
      File f = new File("owlOutput.owl");
      byte[] bos = new byte[(int) f.length()];
      try {
          oom.saveOntology(ontology, IRI.create(f.toURI()));
          bos = new byte[(int) f.length()];
          
          FileInputStream fileInputStream = new FileInputStream(f);
          fileInputStream.read(bos);
          fileInputStream.close();
          
      } catch (Exception ex) {
          logger.error("Fehler beim erstellen der OWL Datei");
      }     
      
      exportType.setFilecontent(bos);
      exportType.setUrl("");
      reponse.getReturnInfos().setMessage("Export abgeschlossen. " + countExported + " Konzepte exportiert.");
      exportType.setFormatId(ExportCodeSystemContentRequestType.EXPORT_CSV_ID);
      reponse.setExportInfos(exportType);
  }

  /**
   * @return the countExported
   */
  
  public int getCountExported()
  {
    return countExported;
  }
}
