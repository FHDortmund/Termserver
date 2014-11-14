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
package de.fhdo.models;

import de.fhdo.collaboration.db.CollaborationSession;
import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.models.comparators.ComparatorOrderNr;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsRequestType;
import de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsByTermOrCodeRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsByTermOrCodeResponse;
import de.fhdo.terminologie.ws.search.ListValueSetContentsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetContentsResponse;
import de.fhdo.terminologie.ws.search.PagingResultType;
import de.fhdo.terminologie.ws.search.PagingType;
import de.fhdo.terminologie.ws.search.Status;
import de.fhdo.terminologie.ws.search.SearchType;
import de.fhdo.terminologie.ws.search.SortByField;
import de.fhdo.terminologie.ws.search.SortDirection;
import de.fhdo.terminologie.ws.search.SortingType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Messagebox;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemConcept;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.CodeSystemVersionEntityMembership;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner
 */
public class TreeModelCSEV
{

  private ContentCSVSDefault contentCSVSDefault;
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private Object source;
  private TreeModel treeModel;
  private long versionId, id;
  private SearchType searchTypeWS;
  private PagingType pagingTypeWS;
  private String searchTerm,
          searchCode;
  private int contentMode;
  private Boolean preferred;
  private int totalSize = 0;
  private boolean cacheValid = true;
  private boolean popUpSearch = false;
  private HashMap<String, List<TreeNode>> cache = new HashMap<String, List<TreeNode>>();
  private List<CodeSystemEntityVersion> listCSEVResponse = new ArrayList<CodeSystemEntityVersion>();
  private Object response;

  public TreeModelCSEV(Object oSource) throws Exception
  {
    this(oSource, null, null, null, null, null, false, null);

    logger.debug("TreeModelCSEV() - Konstruktor ohne Parameter");
  }

  public TreeModelCSEV(Object oSource, ContentCSVSDefault content) throws Exception
  {
    this(oSource, null, null, null, null, null, false, content);

    logger.debug("TreeModelCSEV() - Konstruktor 2 ohne Search Parameter");
  }

  public TreeModelCSEV(Object oSource, SearchType s, String term, String code, PagingType p, Boolean pref, Boolean pop, ContentCSVSDefault content) throws Exception
  {
    logger.debug("TreeModelCSEV() - alles");
    if (content == null)
      logger.debug("KEIN SEARCH");
    else
      logger.debug("SEARCH vorhanden");
    contentCSVSDefault = content;

    source = oSource;
    if (source instanceof CodeSystemVersion)
    {
      id = ((CodeSystemVersion) source).getCodeSystem().getId();
      versionId = ((CodeSystemVersion) source).getVersionId();
      contentMode = ContentConcepts.CONTENTMODE_CODESYSTEM;
    }
    else if (source instanceof ValueSetVersion)
    {
      id = ((ValueSetVersion) source).getValueSet().getId();
      versionId = ((ValueSetVersion) source).getVersionId();
      contentMode = ContentConcepts.CONTENTMODE_VALUESET;
    }

    preferred = pref;
    pagingTypeWS = p;
    searchTypeWS = s;
    searchTerm = term;
    searchCode = code;
    if (pop != null)
      popUpSearch = pop;

    initData();
  }

  public void loadDataByPageIndex(int index) throws Exception
  {
    pagingTypeWS.setPageIndex(index);
    initData();
  }

  /**
   * Es gibt 2 Arten Daten zu holen. a) Eine Suche nach bestimmten Kriterien
   * starten b) Nur die Mainclasses Konzepte anzeigen, bzw. wenn das
   * Vokabular/ValueSet keine Mainclasses hat, alle Konzepte anzeigen
   */
  private void initData() throws Exception
  {
    logger.debug("TreeModelCSEV - initData()");
    List<TreeNode> list; // liste der neuen Elemente als TreeNodes (CSEV sind die "Data" von den TreeNodes)                

    if (cacheValid == false)
      clearCache();

    // Liegen Daten im Cache oder nicht?
    if (cacheValid && pagingTypeWS != null && cache.containsKey(String.valueOf(pagingTypeWS.getPageIndex())))
    {
      list = cache.get(String.valueOf(pagingTypeWS.getPageIndex()));
    }
    else
    {
      // Fuer Suche von Begriffen
      if (searchTypeWS != null && (searchTerm != null || searchCode != null) && pagingTypeWS != null)
      {
        list = createRootTreeNodesForModel(false);
      }
      // Keine Suche => Nur MainClasses anzeigen, bzw alle Konzepte //                
      else
      {
        list = createRootTreeNodesForModel(true);

        // Vokabular hat evtl. keine Achsen oder Hauptklassen. Also alle, nicht nur die MainClasses, laden
        if (list != null && list.isEmpty())
          list = createRootTreeNodesForModel(false);
      }

      if (list != null)
      {
        list = postProcessList(list);

        // speichere bearbeitete Liste im Cache
        if (pagingTypeWS != null)
        {
          cache.put(pagingTypeWS.getPageIndex().toString(), list);
        }
      }
    }
    // Create model by list (list can come from cache or newly created)
    if (list != null)
    {
      // Tree aufbauen mit der Liste an TreeNodes                
      TreeNode tnRoot = new TreeNode(null, list);
      tnRoot.setTreeModelCSEV(this);
      treeModel = new TreeModel(tnRoot);
      treeModel.setContentCSVSDefault(contentCSVSDefault);

      // Paging
      doResponsePaging(response);

    }
    logger.debug("TreeModelCSEV - initData(): fertig");
  }

  private List<CodeSystemEntityVersion> getCSEVList(Object response)
  {
    List<CodeSystemEntity> listCSE = null;
    List<CodeSystemEntityVersion> listCSEV = new ArrayList<CodeSystemEntityVersion>();
    // CODE_SYSTEM
    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return r = (de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return) response;
      listCSE = r.getCodeSystemEntity();

    }
    // VALUE_SET
    else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      if (popUpSearch)
      {
        ListValueSetContentsByTermOrCodeResponse.Return r = (ListValueSetContentsByTermOrCodeResponse.Return) response;
        listCSE = r.getCodeSystemEntity();
      }
      else
      {
        ListValueSetContentsResponse.Return r = (ListValueSetContentsResponse.Return) response;
        listCSE = r.getCodeSystemEntity();
      }
    }

    // if list is ok
    if (listCSE != null)
    {
      // Save CSEVs in a list sorted by OrderNr  
      for (CodeSystemEntity cse : listCSE)
      {
        CodeSystemEntityVersion csevCurrent = getCurrentCSEV(cse);

        // die CSE wird bei der Abfrage nach den Assoziationen über ListConceptAssosiations benötigt                        
        csevCurrent.setCodeSystemEntity(cse);
        listCSEV.add(csevCurrent);
      }

      // sort listCSE for VS
      if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
      {
        Collections.sort(listCSEV, new ComparatorOrderNr(true));
      }

      return listCSEV;
    }
    else
      return new ArrayList<CodeSystemEntityVersion>();
  }

  private List<TreeNode> getRootNodesFromWSResponse(Object response)
  {
    listCSEVResponse = getCSEVList(response);
    List<TreeNode> treeNodesRoot = new ArrayList<TreeNode>();
    HashMap<String, TreeNode> currentParent = new HashMap<String, TreeNode>();

    // Build TreeNodes ist with CSEV as Data
    for (CodeSystemEntityVersion csev : listCSEVResponse)
    {
      // current Version found -> create node
      if (csev != null)
      {
        TreeNode tn = new TreeNode(csev);
        tn.setTreeModelCSEV(this);          // Fuer Deeplinks, damit sie wissen zu welchem CS/VS sie gehören                                            

        // CODE SYSTEMS
        if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
        {
          // add node with csev to list
          treeNodesRoot.add(tn);

          // Dummy einbauen falls Linked Concepts?
          addDummyToNodeForLinkedConcepts(tn);
        }
        // VALUE SETS
        else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
        {
          int level = getLevel(csev);

          //set to currentParent at level l
          currentParent.put(String.valueOf(level), tn);

          // searchParent
          TreeNode tn_Parent = currentParent.get(String.valueOf(level - 1));

          if (tn_Parent == null)
          {
            treeNodesRoot.add(tn);
          }
          else
          {
            tn_Parent.add(tn);
          }

          // Quell-CSV fuer Concepts in VSVs finden: csvId aus Response holen und mit Ids aus der CSVListe des Browser vergleichen.
          findSourceCSV(tn);
        }
      }
    }

    return treeNodesRoot;
  }

  /**
   * SearchCS: no pagingResultType is not needed ->
   *
   */
  private void doResponsePaging(Object response)
  {
    PagingResultType paging = null;

    // CODE SYSTEM (normal + search)
    if (response instanceof ListCodeSystemConceptsResponse.Return)
    {
      ListCodeSystemConceptsResponse.Return r = (ListCodeSystemConceptsResponse.Return) response;

      // Search with few results => no pagingResultType
      if (r.getPagingInfos() != null)
      {
        paging = r.getPagingInfos();
      }
      else if (searchTypeWS != null)
        totalSize = r.getReturnInfos().getCount();
    }
    // VALUE_SET
    else if (response instanceof ListValueSetContentsResponse.Return)
    {
      ListValueSetContentsResponse.Return r = (ListValueSetContentsResponse.Return) response;
      totalSize = r.getReturnInfos().getCount();
    }
    // VALUE_SET SEARCH
    else if (response instanceof ListValueSetContentsByTermOrCodeResponse.Return)
    {
      ListValueSetContentsByTermOrCodeResponse.Return r = (ListValueSetContentsByTermOrCodeResponse.Return) response;
      paging = new PagingResultType();
      paging.setCount(r.getReturnInfos().getCount());
      paging.setPageSize("100");
      paging.setMaxPageSize(1000);
    }

    if (paging == null)
      return;

    totalSize = paging.getCount();
    if (pagingTypeWS != null)
    {
      // 
      if (Integer.valueOf(pagingTypeWS.getPageSize()) > paging.getMaxPageSize())
        pagingTypeWS.setPageSize(String.valueOf(paging.getMaxPageSize()));

      // modify pageSize to value of response
      if (Integer.valueOf(pagingTypeWS.getPageSize()) < Integer.valueOf(paging.getPageSize()))
        pagingTypeWS.setPageSize(paging.getPageSize());
    }
  }

  private List<TreeNode> createRootTreeNodesForModel(boolean OnlyMainClasses)
  {
    logger.debug("createRootTreeNodesForModel");
    
    List<TreeNode> list;

    // paging for search
    if (popUpSearch && pagingTypeWS != null)
      pagingTypeWS.setUserPaging(true);

    // get CodeSystemEntitys by web service calls
    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      // Request 
      ListCodeSystemConceptsRequestType parameter = createParameterForCodeSystems(OnlyMainClasses);

      // Response
      response = WebServiceHelper.listCodeSystemConcepts(parameter);
    }
    else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      if (popUpSearch)
      {
        // request
        ListValueSetContentsByTermOrCodeRequestType parameter = createParameterForValueSetsByTermOrCode();

        // Response                
        response = WebServiceHelper.listValueSetContentsByTermOrCode(parameter);
      }
      else
      {
        // Request 
        ListValueSetContentsRequestType parameter = createParameterForValueSets();
        parameter.setReadMetadataLevel(true);

        // Response                                
        response = WebServiceHelper.listValueSetContents(parameter);
      }
    }

    // universal for all contents //////////////////////////////////////////
    // check if response is valid
    if (checkResponse(response) == false)
    {
      return new ArrayList<TreeNode>();
    }

    // generate (root)nodes if Status is OK
    if (getStatus(response) == Status.OK)
    {
//            // Paging for CS
//            if(contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM){          
//                // Paging for CS (and VS, but VS has no pagingInfo attribute => create them with default values)
//                if(popUpSearch)
//                    doResponsePaging(response);
//            }
//            else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET){
//                doResponsePaging(response);
//            }

      // get all Nodes from response
      list = getRootNodesFromWSResponse(response);
    }
    // status not ok -> error message
    else
    {
      try
      {
        //            Messagebox.show(response.getReturnInfos().getMessage());
        logger.debug("TreeModelCSEV - listCodeSystemConcepts: Error in request; response is not OK: "/* + response.getReturnInfos().getMessage()*/);
      }
      catch (Exception ex)
      {
        Logger.getLogger(TreeModelCSEV.class.getName()).log(Level.SEVERE, null, ex);
      }
      finally
      {
        return new ArrayList<TreeNode>();
      }
    }

    return list;
  }

  private void addDummiesForChildren(List<TreeNode> list)
  {
    for (TreeNode t : list)
    {
      // if there are children,  do nothing
      if (t.getChildCount() > 0)
        continue;

      CodeSystemEntityVersion csev = (CodeSystemEntityVersion) t.getData();

      if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
      {
        // Check associations if there are children
        if (t.hasLinkedConcepts() || csev.isIsLeaf() == Boolean.FALSE)
          addDummyNode(t);
      }
//            else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET){
//                // check level of following CSEV is == level+1
//                int index = listCSEVResponse.indexOf(csev);
//                if(index < listCSEVResponse.size()-1 && getLevel(listCSEVResponse.get(index+1)) == getLevel(csev)+1)
//                    addDummyNode(t);                                                    
//            }
    }
  }

  /**
   * Anzeige der Konzepte �ndern f�r Suchen (Hierarchien als HTML-String in
   * CustomData speichern)
   *
   */
  private List<TreeNode> postProcessList(List<TreeNode> list)
  {
    // create a copy of original list
    List<TreeNode> listRoot = new ArrayList<TreeNode>();
    listRoot.addAll(list);

    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {

    }
    else if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      // load-on-demand: add dummies to show "+" Symbols if neccessary (AssoKind 2 u. 4) to enable 
      addDummiesForChildren(listRoot);

      // check for associations (Crossmapping or Link), if user wishes to see them
      // TODO erstmal wieder rausgenommen, weil es unglaublich lange dauert bei flachen Vokabularen!
      //if (SessionHelper.isUserLoggedIn() && true) // TODO: Einstellungen fuer das Anzeigen von CM und Linked Concepts
      //  setAssociationFlagsForTreeNodes(listRoot);

      // Fuer Suche traverseToRoot (Hierarchie) im Namen anzeigen
      if (searchTypeWS != null && searchTypeWS.isTraverseConceptsToRoot() && searchTerm != null)
      {
        // (Flache) Liste mit Hierarchie und HTML            
        for (TreeNode treeNode : listRoot)
        {
          if (treeNode.getData() instanceof CodeSystemEntityVersion)
          {
            treeNode.setCustomData(new ArrayList<CodeSystemEntityVersion>());// sonst wird im HTML string immer noch ein "null" angezeigt
            buildHtmlTagHierarchy(treeNode, (CodeSystemEntityVersion) treeNode.getData());
          }
        }
      }
    }
    return listRoot;
  }

  public void loadChildren(TreeNode tn_Parent, int associationKind)
  {
    logger.debug("loadChildren");
    
    CodeSystemEntityVersion csevParent = (CodeSystemEntityVersion) tn_Parent.getData();
    // VS => load childs by parameter level
    if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
//          int levelParent = getLevel(csevParent);
//                    
//          // start search for childerin at orderNr+1 in list of all CSEVs. Stop 
//          for(int i = listCSEVResponse.indexOf(csevParent)+1 ; i<listCSEVResponse.size() ;i++){
//              CodeSystemEntityVersion csevTemp = listCSEVResponse.get(i);              
//              int levelNext = getLevel(csevTemp);
//              if(levelNext <= levelParent)
//                  break;
//              else if(levelNext == levelParent+1){
//                  TreeNode tn_Child = new TreeNode(csevTemp);
//                  tn_Parent.add(tn_Child);                  
//                  
//                  // Get Source CSV
//                  findSourceCSV(tn_Child);
//              }              
//          }
//          
//          
//          // check for childs children
//          addDummiesForChildren(tn_Parent.getChildren());
    }
    else if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      // Parameter erzeugen und im folgenden zusammenbauen
      ListConceptAssociationsRequestType parameter_ListCA = new ListConceptAssociationsRequestType();

      if (SessionHelper.isCollaborationActive())
      {
        parameter_ListCA.setLoginToken(CollaborationSession.getInstance().getSessionID());
      }
      else
        parameter_ListCA.setLoginToken(SessionHelper.getSessionId());

      // CSE erstellen und CSEV einsetzen (CSE Nachladen falls noetig)  
      CodeSystemEntity cseParent = csevParent.getCodeSystemEntity();
      cseParent.getCodeSystemEntityVersions().clear();         // damit nicht mehr als 1 Eintrag in der Liste ist
      cseParent.getCodeSystemEntityVersions().add(csevParent); // die CSEV hinzufuegen für die Abfrage        

      // Zusatzinformationen anfordern um anzuzeigen ob noch Kinder vorhanden sind oder nicht
      parameter_ListCA.setLookForward(true);
      parameter_ListCA.setDirectionBoth(true);
      parameter_ListCA.setCodeSystemEntity(cseParent);

      // Anfrage an WS (ListConceptAssociations) stellen mit parameter_ListCA                       
      csevParent.setCodeSystemEntity(null);       // damit es kein infinity Deep Problem gibt

      // Falls es beim Ausfuehren des WS zum Fehler kommt
      de.fhdo.terminologie.ws.conceptassociation.ListConceptAssociationsResponse.Return response;
      try
      {
        logger.debug("loadChildren");
        response = WebServiceHelper.listConceptAssociations(parameter_ListCA);

        // response Speichern fuer spaetere Verwendung?
        tn_Parent.setResponseListConceptAssociations(response);

        if (response.getCodeSystemEntityVersionAssociation() == null)
          return;
      }
      catch (Exception e)
      {
        e.printStackTrace();
        try
        {
          Messagebox.show(Labels.getLabel("common.error") + "\n\n" + Labels.getLabel("treeModelCSEV.loadDataFromWsFailed") + "\n\n" + e.getLocalizedMessage());
        }
        catch (Exception ex)
        {
          Logger.getLogger(ContentConcepts.class.getName()).log(Level.SEVERE, null, ex);
        }
        return;
      }

      csevParent.setCodeSystemEntity(cseParent);  // das Löschen der CSEV ("csevTarget.setCodeSystemEntity(null);") für die WS-Abfrage wieder rückgängig machen

      // das TreeModel um die entsprechenden (unter)Konzepte erweitern
      // Für alle Kinder: Dem Baum erweitern und ggf. DUMMIES in die neuen Kinder einfügen
      if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.OK)
      {
        for (CodeSystemEntityVersionAssociation cseva : response.getCodeSystemEntityVersionAssociation())
        {
          // Assiziierte CSEV
          CodeSystemEntityVersion csev_Child = cseva.getCodeSystemEntityVersionByCodeSystemEntityVersionId2();

          // Pruefen auf null
          if (csev_Child == null)
            continue;

          // Assoziationstyp speichern
          csev_Child.getAssociationTypes().add(cseva.getAssociationType());

          TreeNode tn_Child;

          // je nach Art der Assoziation muessen andere aktionen durchgefuehrt werden
          switch (cseva.getAssociationKind())
          {
            case 1: // Ontologisch => (noch) nichts
              // Fuege die ontologischen Assoziationen den Details von tn hinzu                        
              break;

            case 2: // Taxonomisch => Knoten erstellen und dem Baum hinzufuegen                
              // Knoten erstellen
              tn_Child = new TreeNode(csev_Child);

              // Baum erweitern
              tn_Parent.add(tn_Child);

//                // Beim neuen Knoten Dummy einfügen, falls noetig
//                if (csev_Child.isIsLeaf() == false)
//                  addDummyNode(tn_Child);
              break;

            case 3: // Crossmapping => markiere den Elternknoten tn als Knoten mit Crossmappings
              tn_Parent.setCrossMapping(true);
              break;

            case 4: // Linked Concept => Markiere Elternknoten tn als Knoten mit LinkedConcepts und fuege Kindsknoten hinzu                   
              tn_Child = new TreeNode(csev_Child);   // Knoten erstellen 
              tn_Child.setLinkedConcept(true);        // Markiere Kindsknoten als LinkedConcept

              tn_Parent.setHasLinkedConcepts(true);          // Markiere Elternknoten als ein Knoten, welcher ?ber LinkedConcepts verfuegt
              tn_Parent.add(tn_Child);                       // Baum erweitern

//                // Beim neuen Knoten Dummy einfuegen, falls noetig
//                if (csev_Child.isIsLeaf() == false)
//                  addDummyNode(tn_Child);
              break;
          }
        }
        addDummiesForChildren(tn_Parent.getChildren());
      }
    }
  }

  /**
   * Adds a Dummy Node to tn if necessary. To decide that, tread CodeSystems and
   * ValueSets separately.
   */
  private void addDummyToNodeForLinkedConcepts(TreeNode tn)
  {
    CodeSystemEntityVersion csev = (CodeSystemEntityVersion) tn.getData();
    if (contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
    {
      // check associations
      for (CodeSystemEntityVersionAssociation cseva : csev.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1())
      {
        if (cseva.getAssociationKind().compareTo(4) == 0)
        {
          addDummyNode(tn);
          break;
        }
      }
    }
    else if (contentMode == ContentConcepts.CONTENTMODE_VALUESET)
    {
      // check levels
    }
  }

  private void addDummyNode(TreeNode t)
  {
    // keine Dummies bei der Suche
    if (searchTypeWS != null)
      return;

    TreeNode dummy = new TreeNode(Labels.getLabel("treeModelCSEV.treeNodeDummyMessage"));
    t.add(dummy);
  }

  private void clearCache()
  {
    cache = new HashMap<String, List<TreeNode>>();
    cacheValid = true;
  }

  /**
   * Das Level ist ein Metadata von CSEV
   *
   * @param csev
   * @return
   */
  private int getLevel(CodeSystemEntityVersion csev)
  {
    try
    {
      return Integer.valueOf(csev.getValueSetMetadataValues().get(0).getParameterValue());
    }
    catch (Exception e)
    {
      return 0;
    }
  }

  private CodeSystemEntityVersion getCurrentCSEV(CodeSystemEntity cse)
  {
    for (CodeSystemEntityVersion csev : cse.getCodeSystemEntityVersions())
    {
      if (csev.getVersionId().equals(cse.getCurrentVersionId()))
        return csev;
    }
    return null;
  }

  private void findSourceCSV(TreeNode tn)
  {
    CodeSystemEntityVersion csev = (CodeSystemEntityVersion) tn.getData();

    long csvId = csev.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().get(0).getCodeSystemVersion().getVersionId();
    for (CodeSystemVersion csv : TreeModelCS.getInstance().getCsvList())
    {
      if (csv.getVersionId().equals(csvId))
      {
        tn.setSourceCSV(csv);
        break;
      }
    }
  }

  private Status getStatus(Object response)
  {
    if (response instanceof de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return)
    {
      de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return r = (de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return) response;
      return r.getReturnInfos().getStatus();
    }
    // VALUE_SET
    else if (response instanceof ListValueSetContentsResponse.Return)
    {
      ListValueSetContentsResponse.Return r = (ListValueSetContentsResponse.Return) response;
      return r.getReturnInfos().getStatus();
    }
    else if (response instanceof ListValueSetContentsByTermOrCodeResponse.Return)
    {
      ListValueSetContentsByTermOrCodeResponse.Return r = (ListValueSetContentsByTermOrCodeResponse.Return) response;
      return r.getReturnInfos().getStatus();
    }
    return null;
  }

  private boolean checkResponse(Object response)
  {
    boolean isOk = true;
    String message = "Response from web service is faulty";
    if (response == null)
    {
      isOk = false;
    }
    else
    {
      // CODE_SYSTEM
      if (response instanceof de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return)
      {
        de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return r = (de.fhdo.terminologie.ws.search.ListCodeSystemConceptsResponse.Return) response;
        if (r.getReturnInfos() == null)
        {
          isOk = false;
        }
      }
      // VALUE_SET
      else if (response instanceof ListValueSetContentsResponse.Return)
      {
        ListValueSetContentsResponse.Return r = (ListValueSetContentsResponse.Return) response;
        if (r.getReturnInfos() == null)
          return false;
      }
    }

    if (isOk)
    {
      return true;
    }
    else
    {
      try
      {
        Messagebox.show("ERROR: " + message);
      }
      catch (Exception ex)
      {
        Logger.getLogger(TreeModelCSEV.class.getName()).log(Level.SEVERE, null, ex);
      }
      return false;
    }
  }

  private ListValueSetContentsRequestType createParameterForValueSets()
  {
    ListValueSetContentsRequestType parameter = new ListValueSetContentsRequestType();

    // ValueSet und ValueSetVersion
    ValueSet vs = new ValueSet();
    ValueSetVersion vsv = new ValueSetVersion();
    vs.setId(id);
    vs.getValueSetVersions().add(vsv);
    vsv.setVersionId(versionId);
    parameter.setValueSet(vs);

    // Sortierung
    parameter.setSortingParameter(createSortingParameter());

    return parameter;
  }

  private ListValueSetContentsByTermOrCodeRequestType createParameterForValueSetsByTermOrCode()
  {
    ListValueSetContentsByTermOrCodeRequestType parameter = new ListValueSetContentsByTermOrCodeRequestType();

    // ValueSet und ValueSetVersion
    ValueSet vs = new ValueSet();
    ValueSetVersion vsv = new ValueSetVersion();
    vs.setId(id);
    vs.getValueSetVersions().add(vsv);
    vsv.setVersionId(versionId);
    parameter.setValueSet(vs);
    parameter.setSearchCode(searchCode);
    parameter.setSearchTerm(searchTerm);

    return parameter;
  }

  private ListCodeSystemConceptsRequestType createParameterForCodeSystems(boolean OnlyMainClasses)
  {
    ListCodeSystemConceptsRequestType parameter = new ListCodeSystemConceptsRequestType();

    // CodeSystemEntity
    parameter.setCodeSystemEntity(new CodeSystemEntity());

    // Nur Hauptachsen zurückgeben? (CodeSystemVersionEntityMembership)
    if (OnlyMainClasses)
    {
      CodeSystemVersionEntityMembership csvem = new CodeSystemVersionEntityMembership();
      csvem.setIsMainClass(true);
      parameter.getCodeSystemEntity().getCodeSystemVersionEntityMemberships().add(csvem);
    }

    // CodeSystem(VersionsID) angeben
    CodeSystemVersion csv = new CodeSystemVersion();
    csv.setVersionId(versionId);
    parameter.setCodeSystem(new CodeSystem());
    parameter.getCodeSystem().setId(id);
    parameter.getCodeSystem().getCodeSystemVersions().add(csv);

    logger.debug("Codesystem-ID: " + id + ", csv-id: " + versionId);

    // login
    if (SessionHelper.isCollaborationActive())
    {
      // Kollaborationslogin verwenden (damit auch nicht-aktive Begriffe angezeigt werden können)
      parameter.setLoginToken(CollaborationSession.getInstance().getSessionID());
    }
    else if (SessionHelper.isUserLoggedIn())
    {
      parameter.setLoginToken(SessionHelper.getSessionId());
    }

    // PagingParameter
    if (pagingTypeWS != null)
      parameter.setPagingParameter(pagingTypeWS);

    // SearchType: Parameter für die Suche nach Konzepten mit bestimmten "term"
    if (searchTypeWS != null && (searchTerm != null || searchCode != null))
    {
      parameter.setSearchParameter(searchTypeWS);
      CodeSystemEntity cse = new CodeSystemEntity();
      CodeSystemEntityVersion csev = new CodeSystemEntityVersion();
      CodeSystemConcept csc = new CodeSystemConcept();

      cse.getCodeSystemEntityVersions().add(csev);
      csev.getCodeSystemConcepts().add(csc);
      csc.setTerm(searchTerm);
      csc.setCode(searchCode);
      // TODO Muss noch als Parameter, der in der GUI mittels Checkbox/Radiogroup gesetzt werden kann, eingelesen werden
      csc.setIsPreferred(preferred);

      parameter.setCodeSystemEntity(cse);
    }

    // damit Linked Concepts gefunden werden (muss nach erstellung von SearchParameter erfolgen und false sein, falls traverse to root genutzt wird)    
    if (parameter.getSearchParameter() != null)
      parameter.setLookForward(!parameter.getSearchParameter().isTraverseConceptsToRoot());
    else
      parameter.setLookForward(true);

    // Sortierung
    parameter.setSortingParameter(createSortingParameter());

    return parameter;
  }

  private SortingType createSortingParameter()
  {
    SortingType st = null;
    Object o = SessionHelper.getValue("SortByField");
    if (o != null)
    {
      st = new SortingType();
      if (o.toString().equals("term"))
      {
        st.setSortBy(SortByField.TERM);
      }
      else
      {
        st.setSortBy(SortByField.CODE);
      }
    }
    o = SessionHelper.getValue("SortDirection");
    if (o != null)
    {
      if (st == null)
        st = new SortingType();

      if (o.toString().equals("descending"))
      {
        st.setSortDirection(SortDirection.DESCENDING);
      }
      else
      {
        st.setSortDirection(SortDirection.ASCENDING);
      }
    }
    return st;
  }

  /**
   * Sets flags from TreeNode according to their Data (CSEV).
   * ListConceptAssociations has to be called for each TreeNode (CSEV)
   *
   * @param list
   */
  private void setAssociationFlagsForTreeNodes(List<TreeNode> list)
  {
    logger.debug("checkForAccociations()");

    for (TreeNode treeNode : list)
    {
      if (treeNode.getData() instanceof CodeSystemEntityVersion)
      {
        CodeSystemEntityVersion csev = (CodeSystemEntityVersion) treeNode.getData();

        ListConceptAssociationsRequestType parameter = new ListConceptAssociationsRequestType();

        // Parameter Login
        if (SessionHelper.isCollaborationActive())
          parameter.setLoginToken(CollaborationSession.getInstance().getSessionID());
        else
          parameter.setLoginToken(SessionHelper.getSessionId());

        // Parameter CSEV
        CodeSystemEntity cseParameter = new CodeSystemEntity();
        CodeSystemEntityVersion csevParameter = new CodeSystemEntityVersion();
        cseParameter.setId(csev.getCodeSystemEntity().getId());
        cseParameter.getCodeSystemEntityVersions().add(csevParameter);
        csevParameter.setVersionId(csev.getVersionId());
        parameter.setCodeSystemEntity(cseParameter);

        logger.debug("setAssociationFlagsForTreeNodes()");
        ListConceptAssociationsResponse.Return response = WebServiceHelper.listConceptAssociations(parameter);

        // Wenn Antwort ok, setze die entsprechenden Flags
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.conceptassociation.Status.OK)
        {
          for (CodeSystemEntityVersionAssociation cseva : response.getCodeSystemEntityVersionAssociation())
          {
            switch (cseva.getAssociationKind())
            {
              case 3: // Crossmapping
                treeNode.setCrossMapping(true);
                break;
              case 4: // Link
                // TODO: Ersetzen bzw löschen wenn Trigger für Links das Flag "isLeaf" in der DB umsetzten können
                treeNode.setHasLinkedConcepts(true);
                break;
            }
          }
        }
        else
        { // Fehler aufgetreten, Antwort nicht ok; Keine Rückgabeliste
          logger.debug("TreeModelCSEV - listConceptAssociations: response is not OK");
        }
      }
    }
  }

  private void buildHtmlTagHierarchy(TreeNode tnStart, CodeSystemEntityVersion csevCurrent)
  {
    CodeSystemEntityVersion csevAncestor = null;

    if (csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() != null
            && csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().isEmpty() == false)
      csevAncestor = csevCurrent.getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().get(0).getCodeSystemEntityVersionByCodeSystemEntityVersionId1();

    if (csevAncestor != null)
    {
      // Liste erweitern
      ((ArrayList<CodeSystemEntityVersion>) tnStart.getCustomData()).add(csevAncestor);

      // naechstes li hinzufuegen oder ul beenden
      buildHtmlTagHierarchy(tnStart, csevAncestor);
    }
  }
////////////////////////////////////////////////////////////////////////////////    
////// GETTER AND SETTER ///////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

  public TreeModel getTreeModel()
  {
    return treeModel;
  }

  public int getContentMode()
  {
    return contentMode;
  }

  public void setContentMode(int contentMode)
  {
    this.contentMode = contentMode;
  }

  public int getTotalSize()
  {
    return totalSize;
  }

  public void setTotalSize(int totalSize)
  {
    this.totalSize = totalSize;
  }

  public Object getSource()
  {
    return source;
  }

  public void setSource(Object source)
  {
    this.source = source;
  }
}
