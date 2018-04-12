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
import de.fhdo.terminologie.Definitions;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.AssociationType;
import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemConcept;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntityVersionAssociation;
import de.fhdo.terminologie.db.hibernate.CodeSystemMetadataValue;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersionEntityMembership;
import de.fhdo.terminologie.db.hibernate.MetadataParameter;
import de.fhdo.terminologie.helper.PropertiesHelper;
import de.fhdo.terminologie.helper.SysParameter;
import de.fhdo.terminologie.ws.administration.StaticStatus;
import static de.fhdo.terminologie.ws.administration._import.ImportClaml.currentTask;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.CreateConcept;
import de.fhdo.terminologie.ws.authoring.CreateConceptAssociationType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptAssociationTypeResponseType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateConceptResponseType;
import de.fhdo.terminologie.ws.authorization.types.AuthenticateInfos;
import de.fhdo.terminologie.ws.conceptAssociation.CreateConceptAssociation;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationRequestType;
import de.fhdo.terminologie.ws.conceptAssociation.types.CreateConceptAssociationResponseType;
import de.fhdo.terminologie.ws.search.ListCodeSystems;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.types.ListCodeSystemsResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import mesh.AllowableQualifier;
import mesh.Concept;
import mesh.ConceptRelation;
import mesh.DescriptorRecord;
import mesh.DescriptorRecordSet;
import mesh.EntryCombination;
import mesh.PharmacologicalAction;
import mesh.PreviousIndexing;
import mesh.RelatedRegistryNumber;
import mesh.SeeRelatedDescriptor;
import mesh.SemanticType;
import mesh.Term;
import mesh.ThesaurusID;
import mesh.TreeNumber;
import mesh.Year;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class ImportMeSH
{

  private static Logger logger = Logger4j.getInstance().getLogger();

  private static final String CODESYSTEM_NAME = "MeSH";
  private String languageCode = "";

  public static boolean isRunning = false;
  public static double percentageComplete = 0.0;
  String loginToken;
  private AuthenticateInfos loginInfoType;

  private HashMap metaDataMap;
  private HashMap<String, Long> entityVersionIdMap;
  private HashMap<String, Long> treeNumberMap;
  private HashMap<String, Long> relationSeeRelatedMap;
  private HashMap<String, Long> relationPharmacologicalActionMap;
  private HashMap<String, Long> relationEntryCombinationMap;

  private int countImported = 0;
  CreateConcept createConcept;
  CodeSystem codeSystem;

  private AssociationType associationTypeTaxonomy = null;
  private AssociationType associationTypeNRW = null;
  private AssociationType associationTypeBRD = null;
  private AssociationType associationTypeREL = null;

  public ImportMeSH(AuthenticateInfos _loginInfoType)
  {
    createConcept = new CreateConcept();
    loginInfoType = _loginInfoType;
  }

  public void startImport(ImportCodeSystemRequestType request) throws Exception
  {
    logger.debug("startImport, MESH...");

    StaticStatus.importTotal = 0;
    StaticStatus.importCount = 0;
    StaticStatus.importRunning = true;
    StaticStatus.exportRunning = false;
    StaticStatus.cancel = false;

    entityVersionIdMap = new HashMap<String, Long>();
    treeNumberMap = new HashMap<String, Long>();
    metaDataMap = new HashMap();

    relationSeeRelatedMap = new HashMap<String, Long>();
    relationPharmacologicalActionMap = new HashMap<String, Long>();
    relationEntryCombinationMap = new HashMap<String, Long>();

    isRunning = true;

    loginToken = request.getLoginToken();

    // Hibernate-Block, Session öffnen
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    org.hibernate.Transaction tx = hb_session.beginTransaction();

    try // try-catch-Block zum Abfangen von Hibernate-Fehlern
    {
      logger.debug("Oeffne Datei...");
      byte[] bytes = request.getImportInfos().getFilecontent();
      //ByteArrayDataSource bads = new ByteArrayDataSource(request.getImportInfos().getFilecontent(), "text/xml; charset=UTF-8");

      //logger.debug("wandle zu InputStream um...");
      //InputStream is = new ByteArrayInputStream(bytes);
      loadMeSH(bytes, hb_session);

      if (StaticStatus.cancel)
      {
        tx.rollback();
      }
      else
      {
        tx.commit();
      }
    }
    catch (Exception ex)
    {
      logger.error("ImportMeSH error: " + ex.getMessage());
      ex.printStackTrace();

      logger.debug(ex.getMessage());
      try
      {
        tx.rollback();
        logger.info("[ImportMeSH.java] Rollback durchgeführt!");
      }
      catch (Exception exRollback)
      {
        logger.info(exRollback.getMessage());
        logger.info("[ImportMeSH.java] Rollback fehlgeschlagen!");
      }

      //LoggingOutput.outputException(ex, this);
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

  private InputStream getInputStream(byte[] bytes)
  {
    try
    {
      logger.debug("check for zip file");
      //
      //ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes), Charset.forName("UTF-8"));
      //ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes), Charset.forName("ISO-8859-1"));
      ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
      ZipEntry ze = zis.getNextEntry();

      if (ze != null)
      {
        return zis;
      }
      /*if (ze != null)
       {
       byte[] buffer = new byte[1024];
       ByteArrayOutputStream baos = new ByteArrayOutputStream();

       int anzahl;
       while ((anzahl = zis.read(buffer)) != -1)
       { // -1 = Stream-Ende
       baos.write(buffer, 0, anzahl); // Nur die gelesene Zahl von Bytes schreiben
       }
       baos.close();
       bytes = baos.toByteArray();

       zis.closeEntry();
       zis.close();
       if (logger.isDebugEnabled())
       logger.debug("ZIP loaded, length of file: " + bytes.length);

       //logger.debug("FILE: " + new String(bytes));
       }*/
    }
    catch (Exception ex)
    {
      logger.debug("no zip file detected");

      //ex.printStackTrace();
    }

    logger.debug("no zip file");
    ByteArrayInputStream input = new ByteArrayInputStream(bytes);
    return input;
  }

  private void loadMeSH(byte[] bytes, Session hb_session) throws Exception
  {
    if (logger.isDebugEnabled())
      logger.debug("bytes length: " + bytes.length);

    // check if zip file
    /*try
     {
     logger.debug("check for zip file");
     ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
     ZipEntry ze = zis.getNextEntry();

     if (ze != null)
     {
     byte[] buffer = new byte[1024];
     ByteArrayOutputStream baos = new ByteArrayOutputStream();

     int anzahl;
     while ((anzahl = zis.read(buffer)) != -1)
     { // -1 = Stream-Ende
     baos.write(buffer, 0, anzahl); // Nur die gelesene Zahl von Bytes schreiben
     }
     baos.close();
     bytes = baos.toByteArray();

     zis.closeEntry();
     zis.close();
     if (logger.isDebugEnabled())
     logger.debug("ZIP loaded, length of file: " + bytes.length);

     //logger.debug("FILE: " + new String(bytes));
     }
     }
     catch (Exception ex)
     {
     logger.debug("no zip file detected");

     ex.printStackTrace();
     }

     ByteArrayInputStream input = new ByteArrayInputStream(bytes);*/
    InputStream input = getInputStream(bytes);

    /*logger.debug("SAVE FILE");
    //File file = new File("C:/temp/test_mesh.xml");
    OutputStream outputStream = null;
    try
    {
      // write the inputStream to a FileOutputStream
      outputStream = new FileOutputStream(new File("C:/temp/test_mesh.xml"));

      int read = 0;
      byte[] bytes2 = new byte[1024];

      while ((read = input.read(bytes2)) != -1)
      {
        outputStream.write(bytes2, 0, read);
      }

      System.out.println("Done!");

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (outputStream != null)
      {
        try
        {
          // outputStream.flush();
          outputStream.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    logger.debug("SAVE FILE FINISH");*/

    JAXBContext jc = JAXBContext.newInstance("mesh");
    //JAXBContext jc = JAXBContext.newInstance(DescriptorRecordSet.class);
    //Unmarshaller u = jc.createUnmarshaller();
    //u.set
    //Object rootObject = u.unmarshal(input);

    XMLInputFactory xif = XMLInputFactory.newFactory();
    xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    //XMLStreamReader xsr = xif.createXMLStreamReader(input, "UTF-8");
    //XMLStreamReader xsr = xif.createXMLStreamReader(input, "ISO-8859-1");
    XMLStreamReader xsr = xif.createXMLStreamReader(input);

    Unmarshaller unmarshaller = jc.createUnmarshaller();
    DescriptorRecordSet descriptorRecordSet = (DescriptorRecordSet) unmarshaller.unmarshal(xsr);
    languageCode = descriptorRecordSet.getLanguageCode();
    if (logger.isDebugEnabled())
      logger.debug("Language-Code: " + languageCode);

    codeSystem = createCodeSystem(hb_session, descriptorRecordSet.getLanguageCode());

    if (codeSystem != null)
    {
      if (logger.isDebugEnabled())
        logger.debug("codesystem created, begin import");

      StaticStatus.importTotal = descriptorRecordSet.getDescriptorRecord().size();
      if (logger.isDebugEnabled())
        logger.debug("Count entries: " + StaticStatus.importTotal);
      long countEvery = 0;

      for (DescriptorRecord record : descriptorRecordSet.getDescriptorRecord())
      {
        if (StaticStatus.cancel)
          break;

        if (importRecord(hb_session, record))
        {
          StaticStatus.importCount++;
          countImported++;
        }
        else
        {

        }

        // memory garbage collection
        if (countEvery % 500 == 0)
        {
          // wichtig, sonst kommt es bei größeren Dateien zum Java-Heapspace-Fehler
          hb_session.flush();
          hb_session.clear();

          if (countEvery % 1000 == 0)
          {
            // sicherheitshalber aufrufen
            System.gc();
          }
        }
        countEvery++;
      }

      // import associations from Treenumbers
      logger.info("==================================================");
      logger.info("import associations from Treenumbers, count: " + treeNumberMap.size());
      logger.info("==================================================");

      long countAssociationsImported = 0;

      for (String treenumber : treeNumberMap.keySet())
      {
        long ev_id1 = 0, ev_id2 = treeNumberMap.get(treenumber);
        String upperNumber = "";
        String[] s = treenumber.split("\\.");
        if (s != null && s.length > 0)
        {
          //for(String s2 : s)
          for (int i = 0; i < s.length - 1; ++i)
          {
            if (upperNumber.length() > 0)
              upperNumber += ".";
            upperNumber += s[i];
          }
        }

        logger.debug("treenumber: " + treenumber + ", upper: " + upperNumber);

        if (upperNumber.length() > 0)
        {
          if (treeNumberMap.containsKey(upperNumber))
          {
            ev_id1 = treeNumberMap.get(upperNumber);
          }
        }

        logger.debug("ev_id1: " + ev_id1 + ", ev_id2: " + ev_id2);
        if (ev_id1 > 0 && ev_id2 > 0)
        {
          // add association (taxonomy)
          createAssociationTaxonomy(hb_session, ev_id1, ev_id2);
          countAssociationsImported++;
        }
      }

      if (countAssociationsImported == 0 && treeNumberMap.size() > 0)
      {
        throw new Exception("No associations could be imported. Import cancelled.");
      }

      // import other associations
      AssociationType assTypeSeeRelated = null;
      for (String descriptorUI : relationSeeRelatedMap.keySet())
      {
        long ev_id1 = relationSeeRelatedMap.get(descriptorUI), ev_id2 = 0;

        if (entityVersionIdMap.containsKey(descriptorUI))
          ev_id2 = entityVersionIdMap.get(descriptorUI);

        logger.debug("SEE RELATED: ev_id1: " + ev_id1 + ", ev_id2: " + ev_id2);
        if (ev_id1 > 0 && ev_id2 > 0)
        {
          // add association
          if (assTypeSeeRelated == null)
            assTypeSeeRelated = CreateAssociationType(hb_session, "see related", "see related (backwards)");

          createAssociation(hb_session, ev_id1, ev_id2, Definitions.ASSOCIATION_KIND.ONTOLOGY, assTypeSeeRelated);
          countAssociationsImported++;
        }
      }

      AssociationType assTypePharma = null;
      for (String descriptorUI : relationPharmacologicalActionMap.keySet())
      {
        long ev_id1 = relationPharmacologicalActionMap.get(descriptorUI), ev_id2 = 0;

        if (entityVersionIdMap.containsKey(descriptorUI))
          ev_id2 = entityVersionIdMap.get(descriptorUI);

        logger.debug("PHARMA: ev_id1: " + ev_id1 + ", ev_id2: " + ev_id2);
        if (ev_id1 > 0 && ev_id2 > 0)
        {
          // add association
          if (assTypePharma == null)
            assTypePharma = CreateAssociationType(hb_session, "pharmacological action", "pharmacological action (backwards)");

          createAssociation(hb_session, ev_id1, ev_id2, Definitions.ASSOCIATION_KIND.ONTOLOGY, assTypePharma);
          countAssociationsImported++;
        }
      }
      // TODO relationEntryCombinationMap = new HashMap<String, Long>();

      if (input != null)
        input.close();
    }
    else
    {
      if (input != null)
        input.close();
      throw new Exception("Codesystem could not be created or loaded, name: " + CODESYSTEM_NAME);
    }

  }

  private String formatDate(Year year, String month, String day)
  {
    //SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    //return sdf.format(new Date())
    return day + "." + month + "." + year.getvalue();
  }

  private boolean importRecord(Session hb_session, DescriptorRecord record) throws Exception
  {
    boolean success = false;

    // import DescriptorRecord as concept
    String code = record.getDescriptorUI();
    if (logger.isDebugEnabled())
      logger.debug("importRecord with code: " + code);

    String term = "";
    if (record.getDescriptorName() != null && record.getDescriptorName().getString() != null)
      term = extractTranslation(record.getDescriptorName().getString());

    boolean isMainClass = false;

    // determinate root entries
    if (record.getTreeNumberList() != null)
    {
      for (TreeNumber treeNumber : record.getTreeNumberList().getTreeNumber())
      {
        if (treeNumber.getvalue() != null && treeNumber.getvalue().contains(".") == false)
          isMainClass = true;

      }
    }

    Date effectiveDate = new Date();
    if (record.getDateCreated() != null)
    {
      effectiveDate = new Date(Integer.parseInt(record.getDateCreated().getYear().getvalue()),
              Integer.parseInt(record.getDateCreated().getMonth()),
              Integer.parseInt(record.getDateCreated().getDay()));
    }

    // create concept
    long ev_id = createConcept(codeSystem, hb_session,
            code, term, record.getAnnotation(), "", "", "", effectiveDate,
            isMainClass, true);

    if (logger.isDebugEnabled())
      logger.debug("created concept, entityVersionId: " + ev_id);

    // add metadata
    insertMetaData(hb_session, "Consider Also", record.getConsiderAlso(), ev_id, "String");
    insertMetaData(hb_session, "History Note", record.getHistoryNote(), ev_id, "String");
    insertMetaData(hb_session, "Online Note", record.getOnlineNote(), ev_id, "String");
    insertMetaData(hb_session, "Public Mesh Note", record.getPublicMeSHNote(), ev_id, "String");
    insertMetaData(hb_session, "Running Head", record.getRunningHead(), ev_id, "String");
    insertMetaData(hb_session, "Descriptor Class", record.getDescriptorClass(), ev_id, "String");

    if (record.getPreviousIndexingList() != null)
    {
      for (PreviousIndexing pIndex : record.getPreviousIndexingList().getPreviousIndexing())
      {
        if (pIndex.getvalue() != null && pIndex.getvalue().length() > 0)
          insertMetaData(hb_session, "Previous Indexing", pIndex.getvalue(), ev_id, "String");
      }
    }

    if (record.getDateEstablished() != null)
    {
      insertMetaData(hb_session, "Date Established", formatDate(record.getDateEstablished().getYear(), record.getDateEstablished().getMonth(), record.getDateEstablished().getDay()), ev_id, "Date");
    }
    if (record.getDateRevised() != null)
    {
      insertMetaData(hb_session, "Date Revised", formatDate(record.getDateRevised().getYear(), record.getDateRevised().getMonth(), record.getDateRevised().getDay()), ev_id, "Date");
    }
    if (record.getTreeNumberList() != null)
    {
      for (TreeNumber treeNumber : record.getTreeNumberList().getTreeNumber())
      {
        if (treeNumber.getvalue() != null)
          insertMetaData(hb_session, "Tree Number", treeNumber.getvalue(), ev_id, "String");
      }
    }
    if (record.getActiveMeSHYearList() != null)
    {
      for (Year year : record.getActiveMeSHYearList().getYear())
      {
        if (year.getvalue() != null)
          insertMetaData(hb_session, "Active MeSH Year", year.getvalue(), ev_id, "int");
      }
    }

    if (record.getAllowableQualifiersList() != null)
    {
      String s = "";
      for (AllowableQualifier qualifier : record.getAllowableQualifiersList().getAllowableQualifier())
      {
        if (s.length() > 0)
          s += ";";
        s += qualifier.getAbbreviation();
      }
      insertMetaData(hb_session, "Allowable Qualifiers", s, ev_id, "String");
    }
    if (record.getRecordOriginatorsList() != null)
    {
      insertMetaData(hb_session, "Record Authorizer", record.getRecordOriginatorsList().getRecordAuthorizer(), ev_id, "String");
      insertMetaData(hb_session, "Record Maintainer", record.getRecordOriginatorsList().getRecordMaintainer(), ev_id, "String");
      insertMetaData(hb_session, "Record Originator", record.getRecordOriginatorsList().getRecordOriginator(), ev_id, "String");
    }

    // add treeNumbers to Map 
    if (record.getTreeNumberList() != null)
    {
      for (TreeNumber treeNumber : record.getTreeNumberList().getTreeNumber())
      {
        if (treeNumber.getvalue() != null)
          treeNumberMap.put(treeNumber.getvalue(), ev_id);
      }
    }

    // add associations
    if (record.getSeeRelatedList() != null)
    {
      for (SeeRelatedDescriptor srd : record.getSeeRelatedList().getSeeRelatedDescriptor())
      {
        if (srd.getDescriptorReferredTo().getDescriptorUI() != null)
          relationSeeRelatedMap.put(srd.getDescriptorReferredTo().getDescriptorUI(), ev_id);
      }
    }
    if (record.getPharmacologicalActionList() != null)
    {
      for (PharmacologicalAction entry : record.getPharmacologicalActionList().getPharmacologicalAction())
      {
        if (entry.getDescriptorReferredTo().getDescriptorUI() != null)
          relationSeeRelatedMap.put(entry.getDescriptorReferredTo().getDescriptorUI(), ev_id);
      }
    }
// TODO if (record.getEntryCombinationList() != null)
//    {
//      for (EntryCombination entry : record.getEntryCombinationList().getEntryCombination())
//      {
//        if (entry
//                .getECOUT().getDescriptorReferredTo().getDescriptorUI() != null)
//          relationSeeRelatedMap.put(entry.getDescriptorReferredTo().getDescriptorUI(), ev_id);
//      }
//    }

    // create sub-concepts
    if (ev_id > 0)
    {
      if (record.getConceptList() != null)
      {
        // import concepts as sub-concepts with association
        for (Concept concept : record.getConceptList().getConcept())
        {
          code = concept.getConceptUI();

          term = "";
          if (concept.getConceptName() != null && concept.getConceptName().getString() != null)
            term = extractTranslation(concept.getConceptName().getString());

          boolean isPreferred = concept.getPreferredConceptYN() != null && concept.getPreferredConceptYN().equalsIgnoreCase("Y");

          long concept_ev_id = createConcept(codeSystem, hb_session,
                  code, term, concept.getScopeNote(), concept.getCASN1Name(), "", "", effectiveDate, false, isPreferred);
          if (logger.isDebugEnabled())
            logger.debug("created sub-concept, entityVersionId: " + concept_ev_id);

          // add metadata
          insertMetaData(hb_session, "Registry Number", concept.getRegistryNumber(), concept_ev_id, "String");
          insertMetaData(hb_session, "Translators English Scope Note", concept.getTranslatorsEnglishScopeNote(), concept_ev_id, "String");
          insertMetaData(hb_session, "Translators Scope Note", concept.getTranslatorsScopeNote(), concept_ev_id, "String");

          if (concept.getRelatedRegistryNumberList() != null)
          {
            for (RelatedRegistryNumber value : concept.getRelatedRegistryNumberList().getRelatedRegistryNumber())
            {
              insertMetaData(hb_session, "Related Registry Number", value.getvalue(), concept_ev_id, "String");
            }
          }
          if (concept.getSemanticTypeList() != null)
          {
            for (SemanticType value : concept.getSemanticTypeList().getSemanticType())
            {
              insertMetaData(hb_session, "Semantic Type UI", value.getSemanticTypeUI() + ": " + value.getSemanticTypeName(), concept_ev_id, "String");
            }
          }

          // add association to upper concept
          createAssociationTaxonomy(hb_session, ev_id, concept_ev_id);

          // import term list as sub-concepts with association
          if (concept.getTermList() != null)
          {
            for (Term termEntry : concept.getTermList().getTerm())
            {
              code = termEntry.getTermUI();
              if (code != null && code.startsWith(languageCode)) // import only translations
              {
                term = "";
                if (termEntry.getString() != null)
                  term = extractTranslation(termEntry.getString());

                isPreferred = termEntry.getConceptPreferredTermYN() != null && termEntry.getConceptPreferredTermYN().equalsIgnoreCase("Y");

                Date effectiveDateTerm = new Date();
                if (termEntry.getDateCreated() != null)
                {
                  effectiveDateTerm = new Date(Integer.parseInt(termEntry.getDateCreated().getYear().getvalue()),
                          Integer.parseInt(termEntry.getDateCreated().getMonth()),
                          Integer.parseInt(termEntry.getDateCreated().getDay()));
                }

                long term_ev_id = createConcept(codeSystem, hb_session,
                        code, term, termEntry.getTermNote(), "", "", termEntry.getAbbreviation(), effectiveDateTerm, false, isPreferred);

                if (logger.isDebugEnabled())
                  logger.debug("created sub-term, entityVersionId: " + term_ev_id);

                // add metadata
                insertMetaData(hb_session, "Entry Version", termEntry.getEntryVersion(), term_ev_id, "String");
                insertMetaData(hb_session, "Is Permuted Term YN", termEntry.getIsPermutedTermYN(), term_ev_id, "String");
                insertMetaData(hb_session, "Lexical Tag", termEntry.getLexicalTag(), term_ev_id, "String");
                insertMetaData(hb_session, "Print Flag YN", termEntry.getPrintFlagYN(), term_ev_id, "String");
                insertMetaData(hb_session, "Record Preferred Term YN", termEntry.getRecordPreferredTermYN(), term_ev_id, "String");
                insertMetaData(hb_session, "Sort Version", termEntry.getSortVersion(), term_ev_id, "String");

                if (termEntry.getThesaurusIDlist() != null)
                {
                  for (ThesaurusID value : termEntry.getThesaurusIDlist().getThesaurusID())
                  {
                    insertMetaData(hb_session, "Thesaurus ID", value.getvalue(), term_ev_id, "String");
                  }
                }

                // add association to upper concept
                createAssociationTaxonomy(hb_session, concept_ev_id, term_ev_id);
              }
            }
          }

          // add associations to other concepts
          if (logger.isDebugEnabled())
            logger.debug("add associations to other concepts (ConceptRelation)");
          if (concept.getConceptRelationList() != null)
          {
            for (ConceptRelation conceptRelation : concept.getConceptRelationList().getConceptRelation())
            {
              if (entityVersionIdMap.containsKey(conceptRelation.getConcept1UI())
                      && entityVersionIdMap.containsKey(conceptRelation.getConcept2UI()))
              {
                Long evId1 = entityVersionIdMap.get(conceptRelation.getConcept1UI());
                Long evId2 = entityVersionIdMap.get(conceptRelation.getConcept2UI());

                if (logger.isDebugEnabled())
                  logger.debug("association ConceptRelation with IDs: " + evId1 + " -> " + evId2 + " (relation name: " + conceptRelation.getRelationName() + ")");

                // Relation-name: NRW | BRD | REL
                createAssociation(hb_session, evId1, evId2, Definitions.ASSOCIATION_KIND.ONTOLOGY, conceptRelation.getRelationName(), conceptRelation.getRelationName());
              }
            }

          }
        }
      }
    }

    return success;
  }

  private void createAssociationTaxonomy(Session hb_session, long entityVersionId1, long entityVersionId2) throws Exception
  {
    createAssociation(hb_session, entityVersionId1, entityVersionId2, Definitions.ASSOCIATION_KIND.TAXONOMY, "ist Oberklasse von", "ist Unterklasse von");
  }

  private void createAssociation(Session hb_session, long entityVersionId1, long entityVersionId2, Definitions.ASSOCIATION_KIND associationKind, String forwardName, String reverseName) throws Exception
  {
    CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();

    cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
    cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(entityVersionId1);

    cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
    cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(entityVersionId2);

    cseva.setAssociationKind(associationKind.getCode());
    cseva.setLeftId(entityVersionId1);
    cseva.setStatus(1);
    cseva.setStatusDate(new Date());

    if (associationKind == Definitions.ASSOCIATION_KIND.TAXONOMY)
    {
      if (associationTypeTaxonomy == null)
        associationTypeTaxonomy = CreateAssociationType(hb_session, forwardName, reverseName);

      cseva.setAssociationType(associationTypeTaxonomy);
    }

    if (forwardName != null)
    {
      if (forwardName.equalsIgnoreCase("nrw"))
      {
        if (associationTypeNRW == null)
          associationTypeNRW = CreateAssociationType(hb_session, forwardName, reverseName);

        cseva.setAssociationType(associationTypeNRW);
      }
      else if (forwardName.equalsIgnoreCase("brd"))
      {
        if (associationTypeBRD == null)
          associationTypeBRD = CreateAssociationType(hb_session, forwardName, reverseName);

        cseva.setAssociationType(associationTypeBRD);
      }
      else if (forwardName.equalsIgnoreCase("rel"))
      {
        if (associationTypeREL == null)
          associationTypeREL = CreateAssociationType(hb_session, forwardName, reverseName);

        cseva.setAssociationType(associationTypeREL);
      }
    }

    CreateConceptAssociationRequestType ccaRequest = new CreateConceptAssociationRequestType();

    ccaRequest.setCodeSystemEntityVersionAssociation(cseva);
    ccaRequest.setLoginToken(loginToken);

    CreateConceptAssociation cca = new CreateConceptAssociation();

    CreateConceptAssociationResponseType ccaResponse = cca.CreateConceptAssociation(ccaRequest, hb_session, loginInfoType);
    if (logger.isDebugEnabled())
      logger.debug("[ImportMeSH.java]" + ccaResponse.getReturnInfos().getMessage());

    if (ccaResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      if (logger.isDebugEnabled())
        logger.debug("[ImportMeSH.java] Create Association Erfolgreich");
    }
    else
    {
      throw new Exception("association could not be created, entityVersionId1: " + entityVersionId1 + ", entityVersionId2: " + entityVersionId2);
    }
  }

  private void createAssociation(Session hb_session, long entityVersionId1, long entityVersionId2, Definitions.ASSOCIATION_KIND associationKind, AssociationType associationType) throws Exception
  {
    CodeSystemEntityVersionAssociation cseva = new CodeSystemEntityVersionAssociation();

    cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(new CodeSystemEntityVersion());
    cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().setVersionId(entityVersionId1);

    cseva.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(new CodeSystemEntityVersion());
    cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().setVersionId(entityVersionId2);

    cseva.setAssociationKind(associationKind.getCode());
    cseva.setLeftId(entityVersionId1);
    cseva.setStatus(1);
    cseva.setStatusDate(new Date());
    cseva.setAssociationType(associationType);

    CreateConceptAssociationRequestType ccaRequest = new CreateConceptAssociationRequestType();

    ccaRequest.setCodeSystemEntityVersionAssociation(cseva);
    ccaRequest.setLoginToken(loginToken);

    CreateConceptAssociation cca = new CreateConceptAssociation();

    CreateConceptAssociationResponseType ccaResponse = cca.CreateConceptAssociation(ccaRequest, hb_session, loginInfoType);
    if (logger.isDebugEnabled())
      logger.debug("[ImportMeSH.java]" + ccaResponse.getReturnInfos().getMessage());

    if (ccaResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      if (logger.isDebugEnabled())
        logger.debug("[ImportMeSH.java] Create Association Erfolgreich");
    }
    else
    {
      throw new Exception("association could not be created, entityVersionId1: " + entityVersionId1 + ", entityVersionId2: " + entityVersionId2);
    }
  }

  private String extractTranslation(String s)
  {
    if (s == null)
      return "";

    return s.replaceAll("\\[.*?\\]", "").trim();
  }

  public Long createConcept(CodeSystem codeSystem, Session hb_session, String code, String term,
          String description, String meaning, String hints, String abbrevation,
          Date effectiveDate, boolean isMainClass, boolean isPreferred) throws Exception
  {
    if (logger.isDebugEnabled())
      logger.debug("createConcept mit Code: " + code + ", Text: " + term);

    long entityVersionId = 0;

    CreateConceptRequestType request = new CreateConceptRequestType();

    // EntityType erstellen
    CodeSystemEntity cse = new CodeSystemEntity();
    CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
    csvem.setIsAxis(false);
    csvem.setIsMainClass(isMainClass);

    cse.setCodeSystemVersionEntityMemberships(new HashSet<CodeSystemVersionEntityMembership>());
    cse.getCodeSystemVersionEntityMemberships().add(csvem);

    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setMajorRevision(1);
    csev.setMinorRevision(0);
    csev.setStatusVisibility(1);
    csev.setIsLeaf(true);  // erstmal true, wird per Trigger auf false gesetzt, wenn eine Beziehung eingefügt wird
    csev.setEffectiveDate(effectiveDate);

    CodeSystemConcept csc = new CodeSystemConcept();
    csc.setCode(code);
    csc.setTerm(term);
    csc.setTermAbbrevation(abbrevation);
    csc.setDescription(description);
    csc.setMeaning(meaning);
    csc.setHints(hints);
    csc.setIsPreferred(isPreferred);

    csev.setCodeSystemConcepts(new HashSet<CodeSystemConcept>());
    csev.getCodeSystemConcepts().add(csc);

    cse.setCodeSystemEntityVersions(new HashSet<CodeSystemEntityVersion>());
    cse.getCodeSystemEntityVersions().add(csev);

    // addAttributeMetadata(clazz, csev, csc);
    //logger.debug("isMainClass: " + csvem.getIsMainClass());
    request.setCodeSystem(codeSystem);
    request.setCodeSystemEntity(cse);
    request.setLoginToken(loginToken);

    // call webservice
    CreateConceptResponseType ccsResponse = createConcept.CreateConcept(request, hb_session, loginInfoType);

    //logger.info("[ImportClaml.java]" + ccsResponse.getReturnInfos().getMessage());
    if (ccsResponse.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
//      if (clazz.getSuperClass() != null && clazz.getSuperClass().size() > 0)
//      {
//        this.createSuperclassAssociation(code, clazz);
//      }

      //aktuelle entityVersionID aus der Response in Hashmap schreiben/merken:
//      long aktEntityVersionID = 0;
//
      if (ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().hasNext())
      {
        entityVersionId = ccsResponse.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next().getVersionId();

        // add code and ev_id to map
        logger.debug("entityVersionIdMap.put, code: " + code + ", ev-id: " + entityVersionId);
        entityVersionIdMap.put(code, entityVersionId);
      }
    }
    else
    {
      throw new Exception();
    }

    return entityVersionId;
  }

  private CodeSystem createCodeSystem(Session hb_session, String languageCode) throws Exception
  {
    CodeSystem codeSystem = null;

    // try loading codesystem with name CODESYSTEM_NAME
    ListCodeSystems lcs = new ListCodeSystems();
    ListCodeSystemsRequestType requestLCS = new ListCodeSystemsRequestType();
    requestLCS.setCodeSystem(new CodeSystem());
    requestLCS.getCodeSystem().setName(CODESYSTEM_NAME);
    requestLCS.setLoginToken(loginToken);
    ListCodeSystemsResponseType response = lcs.ListCodeSystems(requestLCS, null);

    if (logger.isDebugEnabled())
      logger.debug("LCS-Response: " + response.getReturnInfos().getMessage());

    if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      if (response.getCodeSystem() != null && response.getCodeSystem().size() > 0)
      {
        codeSystem = response.getCodeSystem().get(0);
        if (logger.isDebugEnabled())
          logger.debug("MeSH found with cs-id: " + codeSystem.getId());
      }
    }

    // create new codesystem-version
    CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();

    if (codeSystem != null && codeSystem.getId() > 0)
    {
      request.setCodeSystem(codeSystem);
    }
    else
    {
      request.setCodeSystem(new CodeSystem());
      request.getCodeSystem().setName(CODESYSTEM_NAME);
      request.getCodeSystem().setDescription("Medical Subject Headings");
      request.getCodeSystem().setDescriptionEng("Medical Subject Headings");
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

    CodeSystemVersion codeSystemVersion = new CodeSystemVersion();
    codeSystemVersion.setName(sdf.format(new Date()));
    codeSystemVersion.setDescription("");
    codeSystemVersion.setSource("DIMDI");
    codeSystemVersion.setReleaseDate(new Date());
    codeSystemVersion.setOid("");

    if (languageCode != null)
    {
      // map all values (cze|dut|eng|fin|fre|ger|ita|jpn|lav|por|scr|slv|spa)
      if (languageCode.equalsIgnoreCase("ger"))
        codeSystemVersion.setPreferredLanguageCd("de");
      else if (languageCode.equalsIgnoreCase("eng"))
        codeSystemVersion.setPreferredLanguageCd("en");
      else if (languageCode.equalsIgnoreCase("fre"))
        codeSystemVersion.setPreferredLanguageCd("fr");
      else if (languageCode.equalsIgnoreCase("ita"))
        codeSystemVersion.setPreferredLanguageCd("it");
      else if (languageCode.equalsIgnoreCase("cze"))
        codeSystemVersion.setPreferredLanguageCd("cs");
      else if (languageCode.equalsIgnoreCase("dut"))
        codeSystemVersion.setPreferredLanguageCd("nl");
      else if (languageCode.equalsIgnoreCase("fin"))
        codeSystemVersion.setPreferredLanguageCd("fi");
      else if (languageCode.equalsIgnoreCase("jpn"))
        codeSystemVersion.setPreferredLanguageCd("ja");
      else if (languageCode.equalsIgnoreCase("lav"))
        codeSystemVersion.setPreferredLanguageCd("lv");
      else if (languageCode.equalsIgnoreCase("por"))
        codeSystemVersion.setPreferredLanguageCd("pt");
      else if (languageCode.equalsIgnoreCase("scr"))
        codeSystemVersion.setPreferredLanguageCd("sc");
      else if (languageCode.equalsIgnoreCase("slv"))
        codeSystemVersion.setPreferredLanguageCd("sk");
      else if (languageCode.equalsIgnoreCase("spa"))
        codeSystemVersion.setPreferredLanguageCd("es");
    }

    // create always new version
    request.getCodeSystem().setCodeSystemVersions(new HashSet<CodeSystemVersion>());
    request.getCodeSystem().getCodeSystemVersions().add(codeSystemVersion);

    request.setLoginToken(loginToken);

    //Code System erstellen
    CreateCodeSystem ccs = new CreateCodeSystem();
    CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session, "");

    if (logger.isDebugEnabled())
      logger.debug(resp.getReturnInfos().getMessage());

    if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
    {
      throw new Exception(resp.getReturnInfos().getMessage());
    }
    codeSystem = resp.getCodeSystem();

    if (logger.isDebugEnabled())
      logger.debug("(Neue) CodeSystem-ID: " + resp.getCodeSystem().getId());
    if (logger.isDebugEnabled())
      logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());

    // Read existing metadata and add to map to avoid double entries
    String hql = "select distinct mp from MetadataParameter mp "
            + " where codeSystemId=" + resp.getCodeSystem().getId();
    List<MetadataParameter> md_list = hb_session.createQuery(hql).list();

    for (MetadataParameter mp : md_list)
    {
      metaDataMap.put(mp.getParamName(), mp.getId());

      if (logger.isDebugEnabled())
        logger.debug("found metadata: " + mp.getParamName() + " with id: " + mp.getId());
    }

    return codeSystem;
  }

  public AssociationType CreateAssociationType(Session hb_session, String forwardName, String reverseName)
  {
    //Associationtype erstellen
    CodeSystemEntity cse = new CodeSystemEntity();
    CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
    //csvem.setCodeSystemVersion(codeSystem.getCodeSystemVersions().iterator().next());
    csvem.setCodeSystemVersion(new CodeSystemVersion());
    csvem.getCodeSystemVersion().setCodeSystem(codeSystem);
    csvem.getCodeSystemVersion().setVersionId(codeSystem.getCurrentVersionId());
    cse.getCodeSystemVersionEntityMemberships().add(csvem);

    Set<AssociationType> assoList = new HashSet<AssociationType>();
    AssociationType assoctype = new AssociationType();
    assoctype.setForwardName(forwardName);
    assoctype.setReverseName(reverseName);
    assoList.add(assoctype);

    Set<CodeSystemEntityVersion> csevList = new HashSet<CodeSystemEntityVersion>();
    //EntityVersionType erstellen
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
    csev.setMajorRevision(1);
    csev.setMinorRevision(0);
    csev.setAssociationTypes(assoList);
    csev.setIsLeaf(true);

    csevList.add(csev);

    cse.setCodeSystemEntityVersions(csevList);

    CreateConceptAssociationTypeRequestType ccatrt = new CreateConceptAssociationTypeRequestType();
    ccatrt.setCodeSystemEntity(cse);
    ccatrt.setLoginToken(loginToken);

    CreateConceptAssociationType ccat = new CreateConceptAssociationType();
    CreateConceptAssociationTypeResponseType response = ccat.CreateConceptAssociationType(ccatrt, hb_session, loginInfoType);

    if (response.getReturnInfos().getStatus() == ReturnType.Status.OK)
    {
      CodeSystemEntityVersion resp_csev = response.getCodeSystemEntity().getCodeSystemEntityVersions().iterator().next();
      assoctype.setCodeSystemEntityVersionId(resp_csev.getVersionId());
      return assoctype;
      // return resp_csev.getAssociationTypes().iterator().next();
    }

    return null;
  }

  private long insertMetaData(Session hb_session, String name, String value, long codeSystemEntityVersionId, String dataType)
  {
    if (value == null || value.length() == 0)
      return 0;

    long metaDataID = 0;
    // Der SQLHelper baut die Insert-Anfrage zusammen
    if (metaDataMap.containsKey(name))
    {
      metaDataID = (Long) metaDataMap.get(name);
    }
    else
    {
      //Create metadata_parameter
      MetadataParameter mp = new MetadataParameter();
      mp.setParamName(name);
      mp.setParamDatatype(dataType);
      mp.setCodeSystem(codeSystem);

      hb_session.save(mp);

      metaDataID = mp.getId();
      logger.info("[ImportMeSH.java] new metadata_parameter mit ID: " + metaDataID + " and name: " + name);

      metaDataMap.put(name, metaDataID);
    }

    // create parameter_value
    if (metaDataID > 0)
    {
      CodeSystemMetadataValue mv = new CodeSystemMetadataValue();
      mv.setParameterValue(value);

      mv.setCodeSystemEntityVersion(new CodeSystemEntityVersion());
      mv.getCodeSystemEntityVersion().setVersionId(codeSystemEntityVersionId);

      mv.setMetadataParameter(new MetadataParameter());
      mv.getMetadataParameter().setId(metaDataID);

      hb_session.save(mv);

      metaDataID = mv.getId();
    }
    return metaDataID;
  }

  /**
   * @return the countImported
   */
  public int getCountImported()
  {
    return countImported;
  }

}
