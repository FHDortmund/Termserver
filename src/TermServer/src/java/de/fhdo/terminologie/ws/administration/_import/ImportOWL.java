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
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.logging.Logger4j;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import static de.fhdo.terminologie.ws.administration._import.ImportClaml.currentTask;
import static de.fhdo.terminologie.ws.administration._import.ImportClaml.isRunning;
import static de.fhdo.terminologie.ws.administration._import.ImportClaml.percentageComplete;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import java.util.Map;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
        
/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportOWL
{
  private static Logger logger = Logger4j.getInstance().getLogger();
  private AuthenticateInfos loginInfoType;
  private int countImported = 0;
  private String loginToken;
  private CodeSystem codeSystem;
  private org.hibernate.Session hb_session;
  private CreateConceptAssociationTypeResponseType ccatrespt;
  private CreateConceptAssociationResponseType ccarespt;
  private CreateConceptResponseType ccsResponse;
  
  public ImportOWL(AuthenticateInfos _loginInfoType)
  {
    if (logger.isInfoEnabled())
      logger.info("====== ImportOWL Constructor ======");

    loginInfoType = _loginInfoType;
  }
  
  public void startImport(ImportCodeSystemRequestType request) throws Exception
  {
    this.loginToken = request.getLoginToken();
    this.codeSystem = request.getCodeSystem();
    
    // Hibernate-Block, Session öffnen
    hb_session = HibernateUtil.getSessionFactory().openSession();
    hb_session.getTransaction().begin();
    
    logger.debug("Oeffne Datei...");
    byte[] bytes = request.getImportInfos().getFilecontent();
    logger.debug("wandle zu InputStream um...");
    InputStream is = new ByteArrayInputStream(bytes);
    
    try 
    {
        //Auswerten der Ontologieinformationen 
        logger.debug("Ontologyinformationen auswerten");
        OWLOntology owlOntology = ontologieInfoAuswerten(bytes, is);
        
        //Einfaches erstellen der Concepte 
        logger.debug("Concepte erstellen");
        concepteErstellen(owlOntology);
        
        //Assoziationstypen erstellen
        logger.debug("Assoziationstypen auswerten");
        associationType(owlOntology); 
        
        //Hierachie aufbauen
        logger.debug("Unterklassen Erstellen");
        subKlassenErstellen(owlOntology);
        
        //Assoziationen erstellen die nicht für Hierachie nötig sind
        logger.debug("Azzoziationen erstellen");
        assoziationenErstellen(owlOntology);
        
     if (StaticStatus.cancel)
      {
        hb_session.getTransaction().rollback();
      }
      else
        hb_session.getTransaction().commit();
    }
    catch (Exception ex)
    {
      logger.error("ImportOWL error: " + ex.getLocalizedMessage());
      //ex.printStackTrace();

      logger.debug(ex.getMessage());
      try
      {
        hb_session.getTransaction().rollback();
        logger.info("[ImportOWL.java] Rollback durchgeführt!");
      }
      catch (Exception exRollback)
      {
        logger.info(exRollback.getMessage());
        logger.info("[ImportOWL.java] Rollback fehlgeschlagen!");
      }

      LoggingOutput.outputException(ex, this);

      throw ex;
    }
    finally
    {
      currentTask = "";
      percentageComplete = 0.0;

      // Session schließen
      hb_session.close();

      isRunning = false;
    }            
  }
  
     //zum auswerten des OntologyTag, entspricht createCodeSystem
    public OWLOntology ontologieInfoAuswerten(byte[] b, InputStream is) throws Exception
    {
        //Namen der Ontology erfragen
        String onto2 = "Kein Name";
        String s = new String(b);
        OWLOntology ontology = null;
        try
        {
            logger.debug("Analyze OntologyTag");                    
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            
            //einladen der Ontology, um mir OWL API verarbeiten zu können
            ontology = manager.loadOntologyFromOntologyDocument(is);            
            String Onto = ontology.getOntologyID().toString();
            
            //API gibt nur ganze IRI zurück daher String zerlegung in eigentlich wichtigen Teil
            if(Onto.indexOf("<") > -1 && Onto.indexOf(">") > -1)
            {
                String Onto1 = Onto.substring(Onto.indexOf("<"), Onto.indexOf(">"));
                if(Onto1.lastIndexOf("/") > -1)
                {
                    onto2 = Onto1.substring(Onto1.lastIndexOf("/")+1, Onto1.length()-4);
                }
            }          
        }catch(Exception e)
        {
            logger.error("Fehler beim einlesen der Ontologie");
        }
        //Weitere Informationen zur Ontology entnehmen, API ermöglicht nicht auswerter der Ontologie hinsichtlich VersionInfo und Comments
        //Daher zerlegung des TAGs und herraussuchen der ggf. angegebenen Informationen, Achtung: Funktioniert nur bei RDF/XML Format, sonst beide Informationen leer.
        String such1 = "owl:ontology";
        String such2 = "/owl:ontology";
        String s1 = s.toLowerCase();   
        String versionName = "";
        String description = "";
        
        //prüfen ob weitere Informationen in Ontology gesetzt sind
        if(s1.indexOf(such1) > -1 && s1.indexOf(such2) > -1)
        {
            String s2 = s1.substring(s1.indexOf(such1)+12, s1.indexOf(such2));
            String s3;
            //prüfen ob VersionInfo gesetzt ist
            such1 = "owl:versioninfo";
            such2 = "/owl:versioninfo";
            if(s2.indexOf(such1) > -1 && s2.indexOf(such2) > -1)
            {
                s3 = s2.substring(s2.indexOf(such1), s2.indexOf(such2));
                versionName = s3.substring(s3.indexOf(">")+1, s3.indexOf("<"));
            }
            
            //prüfen ob Comment gesetzt ist
            such1 = "rdfs:comment";
            such2 = "/rdfs:comment";
            if(s2.indexOf(such1) > -1 && s2.indexOf(such2) > -1)
            {
                s3 = s2.substring(s2.indexOf(such1), s2.indexOf(such2));
                description = s3.substring(s3.indexOf(">")+1, s3.indexOf("<"));
            }
        }
        
        logger.debug("Daten aus OntologyTag bereit für Termserver");
        //Herrausgesuchte Daten werden an Terminologieserver gesendet.
        CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();    

        request.setCodeSystem(new CodeSystem());
       
        request.getCodeSystem().setName(onto2);
        request.getCodeSystem().setDescription(description);
 
        CodeSystemVersion codeSystemVersion = new CodeSystemVersion();
        codeSystemVersion.setName(onto2 + " " + versionName);
        codeSystemVersion.setDescription(description);
 
        request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
        request.getCodeSystem().getCodeSystemVersions().add(codeSystemVersion);

        request.setLoginToken(loginToken);
        
        CreateCodeSystem ccs = new CreateCodeSystem();
        CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");
        
        logger.debug(resp.getReturnInfos().getMessage());

        if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
        {
            throw new Exception();
        }
        this.codeSystem = resp.getCodeSystem();

        logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
        logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
        return ontology;
    }
    
    //Concepte auswerten und erstellen
    public void concepteErstellen(OWLOntology ontology)
    {
        
        
        //Auflisten aller Klassen
        Set<OWLClass> OWLclasses = ontology.getClassesInSignature();
        
        logger.debug("Concepte werden erstellt und im Termserver gespeichert");        
        for(OWLClass name: OWLclasses)
        {
          CodeSystemEntity cse = new CodeSystemEntity();
        CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();                            
        CodeSystemConcept csc = new CodeSystemConcept();                
        
            //Code der jeweiligen Klasse herraussuchen
            String s = name.toString();
            String s1 = s.substring(s.indexOf("#") + 1, s.indexOf(">"));
            csc.setCode(s1);        
                
            //Herraussuchen der Termen bei Klassen bei denen Label verwendet wurde
            for(OWLAnnotationAssertionAxiom a : ontology.getAnnotationAssertionAxioms(name.getIRI())) 
            {
                if(a.getProperty().isLabel()) 
                {
                    if(a.getValue() instanceof OWLLiteral) 
                    {
                        OWLLiteral val = (OWLLiteral) a.getValue();
                        if(val.getLiteral().isEmpty())
                            csc.setTerm("Fehler Term 1");
                        else
                            csc.setTerm(val.getLiteral());                    
                    }
                }
            }
                
            //Herraussuchen der Terme bei Klassen bei denen Induviduals/Instanzen verwendet wurden                
            OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
            OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
            if(reasoner.getInstances(name, true).isEmpty())
            {}else
            {
                s = reasoner.getInstances(name, true).toString();
                s1 = s.substring(s.indexOf("#") + 1, s.indexOf(">"));
                if(s1.isEmpty())
                    csc.setTerm("Fehler term 2");
                else
                    csc.setTerm(s1);            
            }           
                
            //Herraussuchen der Kommentare an den Klassen für description
            for(OWLAnnotationAssertionAxiom a : ontology.getAnnotationAssertionAxioms(name.getIRI())) 
            {
                if(a.getProperty().isComment()) 
                {
                    if(a.getValue() instanceof OWLLiteral) 
                    {
                        OWLLiteral val = (OWLLiteral) a.getValue();
                        csc.setDescription(val.getLiteral());                    
                    }
                }
            }
            if(csc.getTerm() == null)
                csc.setTerm("Fehler term 3");
                
            //isAxis bzw. isMainClass festlegen, wenn kein subClassOf Tag in der Klasse ist es eine MainKlasse
            if(ontology.getSubClassAxiomsForSubClass(name).isEmpty())
            {
                csvem.setIsAxis(true);
                csvem.setIsMainClass(true);
            }
            
            //Bevorzugt immer gesetzt, Herraussuchen andere Schreibweisen noch nicht umgesetzt
            csc.setIsPreferred(true);                                
            
            //Daten an Terminologieserver senden             
            logger.debug("createNotPrefferdTerm mit Code: " + csc.getCode() + ", Text: " + csc.getTerm());
            CreateConceptRequestType request = new CreateConceptRequestType();
        
            cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
            cse.getCodeSystemVersionEntityMemberships().add(csvem);
        
            CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
            csev.setMajorRevision(0);
            csev.setMinorRevision(0);
            csev.setStatusVisibility(1);
            
            csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
            csev.getCodeSystemConcepts().add(csc);
            
            cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
            cse.getCodeSystemEntityVersions().add(csev);
            
            request.setCodeSystem(codeSystem);
            request.setCodeSystemEntity(cse);
            request.setLoginToken(loginToken);
        
            CreateConcept cc = new CreateConcept();
            this.ccsResponse = cc.CreateConcept(request, hb_session, loginInfoType);
            
            logger.info("[ImportOWL.java]" + ccsResponse.getReturnInfos().getMessage());
            countImported++;
            StaticStatus.importCount++;          
        }                            
    }
    
    //Auslesen der Informationen, für AssoziationType, hier nur das erstellen neuer AssoziationsTypen
    private Map<String, AssociationType> associationTypeMap = null;
    public void associationType(OWLOntology ontology)
    {
        String forwardName = "";
        String reverseName = "";               
        
        logger.debug("Analysiere Assoziationsdaten");
        Set<AssociationType> assoList = new HashSet<AssociationType>();
        AssociationType assoctype = new AssociationType();
        
        //SubClass und SuperClass anlegen um Unterklassen zu definieren
        assoctype.setForwardName("subClassIs");
        assoctype.setReverseName("superClassIs");
        assoList.add(assoctype);
        
        //laden auf den Terminologieserver
        CodeSystemEntity etAssoc = new CodeSystemEntity();
        
        Set<CodeSystemEntityVersion> evlistAssoc = new HashSet<CodeSystemEntityVersion>();        
        CodeSystemEntityVersion evtAssoc = new CodeSystemEntityVersion();
        evtAssoc.setAssociationTypes(assoList);
     
        evlistAssoc.add(evtAssoc);

        etAssoc.setCodeSystemEntityVersions(evlistAssoc);
        
        CreateConceptAssociationTypeRequestType ccatrt = new CreateConceptAssociationTypeRequestType();
        ccatrt.setCodeSystemEntity(etAssoc);
        
        ccatrt.setLoginToken(loginToken);
                    
        CreateConceptAssociationType ccat = new CreateConceptAssociationType();
        this.ccatrespt = ccat.CreateConceptAssociationType(ccatrt, hb_session, loginInfoType);    
        
        //Herraussuchen aller Assoziationen die im OWL angelegt sind
        Set<OWLObjectProperty> oP = ontology.getObjectPropertiesInSignature();
                      
        //durch laufen der Assoziationen und forwardName eintragen
        for(OWLObjectPropertyExpression name: oP) 
        {            
            //String zerlegung, da komplette IRI zurückgegeben wird
            String s = name.toString();
            String s1 = s.substring(s.indexOf("#")+1, s.length()-1);
            forwardName = s1;
            
            //bei Sysmetrischen Assoziationen ist ForwardName und ReverseName gleich
            if ((ontology.getSymmetricObjectPropertyAxioms(name)).isEmpty())
            {} else
            {
                reverseName = s1;
            }
            
            //bei Inversen Assoziationen wird der inverse name als ReversName verwendet
            if ((ontology.getInverseObjectPropertyAxioms(name)).isEmpty())
            {} else
            {
                //String zerlegung, da komplette IRI zurückgegeben wird
                s = ontology.getInverseObjectPropertyAxioms(name).toString();
                String s2 = s.substring(s.indexOf("#")+1, s.indexOf(">"));
                if (s1.equals(s2))
                {
                    s2 = s.substring(s.lastIndexOf("#")+1, s.lastIndexOf(">"));        
                }
                reverseName = s2;                    
            }
            
            //Zusammenfassen der Namen
            assoctype = new AssociationType();
            assoctype.setForwardName(forwardName);
            assoctype.setReverseName(reverseName);
            assoList.add(assoctype);

            //senden an Terminologieserver
            etAssoc = new CodeSystemEntity();
        
            evlistAssoc = new HashSet<CodeSystemEntityVersion>();        
            evtAssoc = new CodeSystemEntityVersion();
            evtAssoc.setAssociationTypes(assoList);
     
            evlistAssoc.add(evtAssoc);

            etAssoc.setCodeSystemEntityVersions(evlistAssoc);
        
            ccatrt = new CreateConceptAssociationTypeRequestType();
            ccatrt.setCodeSystemEntity(etAssoc);
        
            ccatrt.setLoginToken(loginToken);
                    
            ccat = new CreateConceptAssociationType();
            this.ccatrespt = ccat.CreateConceptAssociationType(ccatrt, hb_session, loginInfoType);    
            
            logger.debug("AssoziationType erstellt: " + this.ccatrespt.getReturnInfos().getMessage());
            if(this.ccatrespt.getReturnInfos().getStatus() == ReturnType.Status.OK)
            {
              long newVersionId = ((CodeSystemEntityVersion)ccatrespt.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0]).getVersionId();
              logger.debug("newVersionId: " + newVersionId);
            }
            //AssociationType at = (AssociationType) ((CodeSystemEntityVersion)ccatrespt.getCodeSystemEntity().getCodeSystemEntityVersions().toArray()[0]).getAssociationTypes().toArray()[0];
            //logger.debug("Neue ID: " + at.getCodeSystemEntityVersionId());
        }          
    }
    
    //Erstellen der Assoziationen Unterklassen und Oberklassen
    public void subKlassenErstellen(OWLOntology ontology)
    {
        
           
        //Herraussuchen und durchlaufen aller Klassen
        Set<OWLClass> OWLclasses = ontology.getClassesInSignature();         
        for(OWLClass name: OWLclasses)
        {
          CodeSystemEntityVersionAssociation aseva = new CodeSystemEntityVersionAssociation();
        CodeSystemEntityVersion csev1 = new CodeSystemEntityVersion();
        CodeSystemEntityVersion csev2 = new CodeSystemEntityVersion();
        CodeSystemEntityVersion hilf = new CodeSystemEntityVersion();
        Set<AssociationType> asSet = new HashSet<AssociationType>();
        Set<CodeSystemConcept> csc = new HashSet<CodeSystemConcept>();
          
            if(ontology.getSubClassAxiomsForSubClass(name).isEmpty())
            {}else
            {
                //String zerlegung, da komplette IRI zurückgegeben wird
                String s = ontology.getSubClassAxiomsForSubClass(name).toString();
                String klasse2 = s.substring(s.indexOf("#") + 1, s.indexOf(">"));
                String klasse1 = s.substring(s.lastIndexOf("#") +1, s.lastIndexOf(">"));
                
                //Abfragen auf Server ob Klasse mit dem Code da ist und zurückgeben lassen um diese für VersionByCodeSystemEntityVersionId zu verwenden                
                csc = csev1.getCodeSystemConcepts();
                
                for(CodeSystemConcept name1: csc)
                {
                    if(name1.getCode() == klasse2)
                    {
                        csev2 = name1.getCodeSystemEntityVersion();                        
                        aseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
                    }
                    
                    if(name1.getCode() == klasse1)
                    {
                        csev1 = name1.getCodeSystemEntityVersion();
                        aseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
                    }
                    hilf = name1.getCodeSystemEntityVersion();
                    asSet = hilf.getAssociationTypes();
                    
                    //Im AssoziationType nach Assozieationsnamen suchen um passende ID zu bekommen
                    for(AssociationType name2: asSet)
                    {
                        if(name2.getForwardName().equals("subClassIs"))
                        {
                            aseva.setAssociationType(name2);
                        }
                    }
                }                
                aseva.setAssociationKind(2);  
                logger.debug("Unterklasse Erstellt"); 
            }                                     
            //Daten an Terminologieserver senden
            CreateConceptAssociationRequestType ccart = new CreateConceptAssociationRequestType();
            ccart.setCodeSystemEntityVersionAssociation(aseva);
        
            ccart.setLoginToken(loginToken);
                    
            CreateConceptAssociation ccat = new CreateConceptAssociation();
            this.ccarespt = ccat.CreateConceptAssociation(ccart, hb_session, loginInfoType);
        }
    }
    
    public void assoziationenErstellen(OWLOntology ontology)
    {        
        
     
        //Herraussuchen und durchlaufen aller Eigenschaften des OWL Dokumentes
        Set<OWLObjectProperty> oP = ontology.getObjectPropertiesInSignature();              
        String klasse1 = "";
        String klasse2 = "";
        
        for(OWLObjectPropertyExpression name: oP) 
        {        
          CodeSystemEntityVersionAssociation aseva = new CodeSystemEntityVersionAssociation();
          CodeSystemEntityVersion csev1 = new CodeSystemEntityVersion();
          CodeSystemEntityVersion csev2 = new CodeSystemEntityVersion();
          CodeSystemEntityVersion hilf = new CodeSystemEntityVersion();
          Set<AssociationType> asSet = new HashSet<AssociationType>();
          Set<CodeSystemConcept> csc = new HashSet<CodeSystemConcept>();
          
          
            //String zerlegung, da komplette IRI zurückgegeben wird
            String s = name.toString();
            String s1 = s.substring(s.indexOf("#")+1, s.length()-1);
            if(ontology.getObjectPropertyDomainAxioms(name).isEmpty())
            {}else
            {
                //Herrausuchen der ersten Klasse der Assoziation
                String k1 = ontology.getObjectPropertyDomainAxioms(name).toString();
                klasse1 = k1.substring(k1.lastIndexOf("#") + 1, k1.lastIndexOf(">"));
            }
            if(ontology.getObjectPropertyRangeAxioms(name).isEmpty())
            {}else
            {
                //Herrausuchen der zweiten Klasse der Assoziation
                String k2 = ontology.getObjectPropertyRangeAxioms(name).toString();
                klasse2 = k2.substring(k2.lastIndexOf("#") + 1, k2.lastIndexOf(">"));
            }
            
            csc = csev1.getCodeSystemConcepts();
            
            //Klassen an hand der Namen vom Server Suchen um ID zu erfahren
            for(CodeSystemConcept name1: csc)
            {
                if(name1.getCode() == klasse2)
                {
                    csev2 = name1.getCodeSystemEntityVersion();                        
                    aseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(csev2);
                }
                    
                if(name1.getCode() == klasse1)
                {
                    csev1 = name1.getCodeSystemEntityVersion();
                    aseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csev1);
                }
                hilf = name1.getCodeSystemEntityVersion();
                
                //Im AssoziationType nach Assozieationsnamen suchen um passende ID zu bekommen
                asSet = hilf.getAssociationTypes();
                for(AssociationType name2: asSet)
                {                
                    if(name2.getForwardName().equals(s1))
                    {
                        aseva.setAssociationType(name2);
                    }
                }
                aseva.setAssociationKind(1);
                logger.debug("Assozitaion Erstellt"); 
            } 
            
            //Daten an Terminologieserver senden
            CreateConceptAssociationRequestType ccart = new CreateConceptAssociationRequestType();
            ccart.setCodeSystemEntityVersionAssociation(aseva);
        
            ccart.setLoginToken(loginToken);
                    
            CreateConceptAssociation ccat = new CreateConceptAssociation();
            this.ccarespt = ccat.CreateConceptAssociation(ccart, hb_session, loginInfoType);            
        }
    }   
    
  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }
}
