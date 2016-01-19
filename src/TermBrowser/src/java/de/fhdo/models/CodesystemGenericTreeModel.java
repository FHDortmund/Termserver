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

import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.comparators.ComparatorCodesystems;
import de.fhdo.models.comparators.ComparatorDomainValues;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse;
import de.fhdo.terminologie.ws.search.ListCodeSystemsRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsResponse;
import de.fhdo.terminologie.ws.search.Status;
import de.fhdo.tree.GenericTree;
import de.fhdo.tree.GenericTreeCellType;
import de.fhdo.tree.GenericTreeHeaderType;
import de.fhdo.tree.GenericTreeRowType;
import de.fhdo.tree.IGenericTreeActions;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.zkoss.zul.Label;
import org.zkoss.zul.Treecell;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 *
 * 2016-01-12 Robert Mützner changed to get licences for user (use session
 * instead of cache)
 */
public class CodesystemGenericTreeModel
{

  private static CodesystemGenericTreeModel instance = null;

  public static CodesystemGenericTreeModel getInstance()
  {
    if (instance == null)
      instance = new CodesystemGenericTreeModel();

    return instance;
  }

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private boolean loaded = false;
  //private List<DomainValue> listDV = new LinkedList();
  private List<CodeSystem> publicCodeSystemList = null;
  private String errorMessage;
  //private int count = 0;
  //private ArrayList<CodeSystemVersion> csvList = new ArrayList<CodeSystemVersion>();

  public CodesystemGenericTreeModel()
  {
    errorMessage = "";
  }

  public void reloadData()
  {
    SessionHelper.setCodesystemList(null);
    SessionHelper.setDomainValueList(null);
    
    publicCodeSystemList = null;
    //loaded = false;
    initData();
  }

  private void initData()
  {
    List<CodeSystem> listCS = SessionHelper.getCodesystemList();
    //if (loaded)
    if (listCS != null)  // data already loaded
      return;

    logger.debug("--- CodesystemGenericTreeModel, initData ---");
    errorMessage = "";

    try
    {
      //csvList.clear();
      listCS = new LinkedList<CodeSystem>();

      ListCodeSystemsInTaxonomyRequestType parameter = new ListCodeSystemsInTaxonomyRequestType();

      // login
      if (SessionHelper.isUserLoggedIn())
      {
        parameter.setLoginToken(SessionHelper.getSessionId());
      }

      ListCodeSystemsInTaxonomyResponse.Return response = WebServiceHelper.listCodeSystemsInTaxonomy(parameter);

      logger.debug("Response: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        SessionHelper.setDomainValueList(response.getDomainValue());
        //listDV = response.getDomainValue();
        //loaded = true;
      }
      else
      {
        // show error message
        errorMessage = response.getReturnInfos().getMessage();
      }
      
      initPublicCodesystemList();
    }
    catch (Exception e)
    {
      // Bei Fehler, leere Liste zurück geben
      LoggingOutput.outputException(e, this);
      //treeModel = new TreeModel(new TreeNode(null, new LinkedList()));
    }
    
    
  }
  
  private void initPublicCodesystemList()
  {
    if(publicCodeSystemList == null)
    {
      ListCodeSystemsRequestType parameter = new ListCodeSystemsRequestType();
      // don't use login!

      ListCodeSystemsResponse.Return response = WebServiceHelper.listCodeSystems(parameter);
      logger.debug("Response public: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        publicCodeSystemList = response.getCodeSystem();
      }
    }
  }

  /**
   *
   * @param tree
   * @param treeActions
   * @return count entries
   */
  public int initGenericTree(GenericTree tree, IGenericTreeActions treeActions)
  {
    initData();

    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
    header.add(new GenericTreeHeaderType("Name", 0, "", true, "String", false, true, false));

    // Daten erzeugen und der Liste hinzufügen
    //List<GenericTreeRowType> dataList = new LinkedList<GenericTreeRowType>();
    List<GenericTreeRowType> dataList = createModel();

    // Liste initialisieren
    //tree.setMultiple(true);
    tree.setTreeActions(treeActions);
    //genericList.setUpdateDataListener(this);
    tree.setButton_new(false);
    tree.setButton_edit(false);
    tree.setButton_delete(false);
    tree.setListHeader(header);
    tree.setDataList(dataList);

    return SessionHelper.getCodesystemListCount();
    //return count;
  }

  private List<GenericTreeRowType> createModel()
  {
    logger.debug("createModel()");

    SessionHelper.setCodesystemListCount(0);
    //Integer count = 0;
    List<CodeSystem> listCS = new LinkedList<CodeSystem>();

    try
    {
      //List list = new LinkedList();
      List<GenericTreeRowType> list = new LinkedList<GenericTreeRowType>();

      //provideOrderChoosen by AdminSettings
      Map<Integer, DomainValue> mapDomVal = new HashMap<Integer, DomainValue>();

      List<DomainValue> listDV = SessionHelper.getDomainValueList();

      // Domain Values mit untergeordneten DV,CS und CSVs
      for (DomainValue dv : listDV)
      {
        if (dv.getOrderNo() != null)
        {
          mapDomVal.put(dv.getOrderNo(), dv);
        }
        else
        {
          mapDomVal.put(listDV.size(), dv);
        }
      }

      for (int i = 1; i < mapDomVal.size() + 1; i++)
      {
        list.add(createTreeNode(mapDomVal.get(i), listCS));
      }

      // save values in session
      // different licences for each user
      SessionHelper.setCodesystemList(listCS);
      //SessionHelper.setCodesystemListCount(count);

      logger.debug("Model created, count cs: " + SessionHelper.getCodesystemListCount());

      return list;
      //TreeNode tn_root = new TreeNode(null, list);
      //return new TreeModel(tn_root);
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
    return new LinkedList<GenericTreeRowType>();
  }

  public GenericTreeRowType createTreeNode(Object obj, List<CodeSystem> listCS)
  {
    GenericTreeRowType row = new GenericTreeRowType(null);

    GenericTreeCellType[] cells = new GenericTreeCellType[1];

    if (obj instanceof CodeSystem)
    {
      CodeSystem cs = (CodeSystem) obj;

      Label label = new Label(cs.getName());
      label.setTooltiptext(cs.getDescription());

      Treecell tc = new Treecell();
      tc.appendChild(label);

      cells[0] = new GenericTreeCellType(tc, false, "");
      row.setData(cs);

      SessionHelper.setCodesystemListCount(SessionHelper.getCodesystemListCount() + 1);
      listCS.add(cs);

      logger.debug("CS added to list with id: " + cs.getId() + ", name: " + cs.getName());

      Object o = SessionHelper.getValue("loadCS");
      if (o != null)
      {
        long cs_id = Long.parseLong(o.toString());
        if (cs_id == cs.getId())
        {
          logger.debug("load CS with id " + cs_id);
          SessionHelper.setValue("selectedCS", cs);
          SessionHelper.setValue("loadCS", null);
        }
      }

      /*// Kinder (CodeSystemVersions) suchen und dem CodeSystem hinzufügen            
       for (CodeSystemVersion csv : cs.getCodeSystemVersions())
       {
       csv.setCodeSystem(cs);
        
       row.getChildRows().add(createTreeNode(csv));
       }*/
    }
    else if (obj instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) obj;

      cells[0] = new GenericTreeCellType(csv.getName(), false, "");
      row.setData(csv);

    }
    else if (obj instanceof DomainValue)
    {
      DomainValue dv = (DomainValue) obj;

      cells[0] = new GenericTreeCellType(dv.getDomainDisplay(), false, "", "font-weight:bold; color:green;");
      row.setData(dv);

      // Gibts DomainValues im DV? Wenn ja, einfügen
      List<DomainValue> subDomains = dv.getDomainValuesForDomainValueId2();
      Collections.sort(subDomains, new ComparatorDomainValues(true));
      for (DomainValue dv2 : subDomains)
      {
        row.getChildRows().add(createTreeNode(dv2, listCS));
      }

      // Gibts CSs im DV? Wenn ja, einfügen
      List<CodeSystem> subCSList = dv.getCodeSystems();
      Collections.sort(subCSList, new ComparatorCodesystems(true));
      for (CodeSystem cs : subCSList)
      {
        row.getChildRows().add(createTreeNode(cs, listCS));
      }
    }
    else
      return null;    // Kein DV oder CS

    row.setCells(cells);

    return row;
  }

  public CodeSystem findCodeSystem(String Name, String OID, long CodesystemVersionId)
  {
    logger.debug("findCodeSystem()");
    logger.debug("Name: " + Name);
    logger.debug("OID: " + OID);
    logger.debug("CodesystemVersionId: " + CodesystemVersionId);
    List<CodeSystem> list = getListCS();

    if(list == null)
    {
      // session might not work
      
    }
    
    if (list != null)
    {
      for (CodeSystem cs : list)
      {
        for (CodeSystemVersion csv : cs.getCodeSystemVersions())
        {
          if ((OID != null && OID.length() > 0 && OID.equalsIgnoreCase(csv.getOid()))
                  || (CodesystemVersionId > 0 && csv.getVersionId() == CodesystemVersionId))
          {
            logger.debug("Found CS with id: " + cs.getId());
            logger.debug("Found CSV with versionId: " + csv.getVersionId());
            csv.setCodeSystem(cs);
            SessionHelper.setValue("selectedCSV", csv);
            return cs;
          }
        }

        if (Name != null && Name.length() > 0 && Name.equalsIgnoreCase(cs.getName()))
        {
          logger.debug("Found CS with id: " + cs.getId());
          return cs;
        }
      }
    }

    return null;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }

  /**
   * @return the listDV
   */
  public List<DomainValue> getListDV()
  {
    return SessionHelper.getDomainValueList();
    //return listDV;
  }

  /**
   * @return the listCS
   */
  public List<CodeSystem> getListCS()
  {
    initData();
    
    List<CodeSystem> list = SessionHelper.getCodesystemList();
    if(list == null)
      return publicCodeSystemList;
    return list;
    //return listCS;
  }

}
