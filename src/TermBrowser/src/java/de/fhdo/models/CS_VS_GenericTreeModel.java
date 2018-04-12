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
import types.termserver.fhdo.de.ValueSet;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class CS_VS_GenericTreeModel
{

  private static CS_VS_GenericTreeModel instance = null;

  public static CS_VS_GenericTreeModel getInstance()
  {
    if (instance == null)
      instance = new CS_VS_GenericTreeModel();

    return instance;
  }

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  //private List<CodeSystem> publicCodeSystemList = null;
  private String errorMessage;

  public CS_VS_GenericTreeModel()
  {
    errorMessage = "";
  }

  public void reloadData()
  {
    SessionHelper.setCodesystemList(null);
    SessionHelper.setDomainValueList(null);

    //publicCodeSystemList = null;
    //loaded = false;
    initData();
  }

  private void initData()
  {
    //List<CodeSystem> listCS = SessionHelper.getCodesystemList();
    List<DomainValue> listDV = SessionHelper.getDomainValueList();

    if (listDV != null)  // data already loaded
      return;

    logger.debug("--- CS_VS_GenericTreeModel, initData ---");
    errorMessage = "";

    try
    {
      //csvList.clear();
      //listCS = new LinkedList<CodeSystem>();

      ListCodeSystemsInTaxonomyRequestType parameter = new ListCodeSystemsInTaxonomyRequestType();

      // login
      if (SessionHelper.isUserLoggedIn())
      {
        parameter.setLoginToken(SessionHelper.getSessionId());
      }

      ListCodeSystemsInTaxonomyResponse.Return response = WebServiceHelper.listCodeSystemsInTaxonomy(parameter);

      logger.debug("Response: " + response.getReturnInfos().getMessage() + ", count: " + response.getReturnInfos().getCount());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        SessionHelper.setDomainValueList(response.getDomainValue());
      }
      else
      {
        // show error message
        errorMessage = response.getReturnInfos().getMessage();
      }

//      initPublicCodesystemList();
    }
    catch (Exception e)
    {
      // Bei Fehler, leere Liste zurück geben
      LoggingOutput.outputException(e, this);
      //treeModel = new TreeModel(new TreeNode(null, new LinkedList()));
    }

  }

  /*private void initPublicCodesystemList()
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
   }*/
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
    header.add(new GenericTreeHeaderType("Typ", 50, "", true, "String", false, true, false));

    // create data and add to tree
    List<GenericTreeRowType> dataList;

    if (SessionHelper.getBoolValue("useFilterTaxonomyId", false))
    {
      // only subtree
      dataList = createModel(SessionHelper.getLongValue("filterTaxonomyId", 0));
    }
    else
    {
      dataList = createModel(0);
    }

    // init tree
    //tree.setMultiple(true);
    tree.setTreeActions(treeActions);
    tree.setButton_new(false);
    tree.setButton_edit(false);
    tree.setButton_delete(false);
    tree.setListHeader(header);
    tree.setDataList(dataList);

    return SessionHelper.getCodesystemListCount();
    //return count;
  }

  private List<GenericTreeRowType> createModel(long filterDomainValueId)
  {
    logger.debug("createModel(), filterDomainValueId: " + filterDomainValueId);

    SessionHelper.setCodesystemListCount(0);
    //Integer count = 0;
    List<CodeSystem> listCS = new LinkedList<CodeSystem>();
    List<ValueSet> listVS = new LinkedList<ValueSet>();

    try
    {
      //List list = new LinkedList();
      List<GenericTreeRowType> list = new LinkedList<GenericTreeRowType>();

      //provideOrderChoosen by AdminSettings
      //Map<Integer, DomainValue> mapDomVal = new HashMap<Integer, DomainValue>();

      List<DomainValue> listDV = SessionHelper.getDomainValueList();

      if (filterDomainValueId > 0)
      {
        List<DomainValue> newListDV = new LinkedList<DomainValue>();
        getDomainValueListFromId(listDV, newListDV, filterDomainValueId);
        if(newListDV.size() > 0)
          listDV = newListDV;
      }

      // Domain Values mit untergeordneten DV,CS und CSVs
      /*for (DomainValue dv : listDV)
       {
       logger.debug("createModel(), root domain value: " + dv.getDomainCode() + ", display: " + dv.getDomainDisplay());
        
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
       list.add(createTreeNode(mapDomVal.get(i), listCS, listVS));
       }*/
      for (DomainValue dv : listDV)
      {
        logger.debug("createModel(), root domain value: " + dv.getDomainCode() + ", display: " + dv.getDomainDisplay());

        list.add(createTreeNode(dv, listCS, listVS));
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

  private void getDomainValueListFromId(List<DomainValue> listDV, List<DomainValue> newListDV, long domainValueId)
  {
    logger.debug("getDomainValueListFromId(), filterDomainValueId: " + domainValueId);
    
    for (DomainValue dv : listDV)
    {
      if(dv == null || dv.getDomainValueId() == null)
        continue;
      
      logger.debug("check domain value with id: " + dv.getDomainValueId() + ", name: " + dv.getDomainDisplay());
      
      if (dv.getDomainValueId() == domainValueId)
      {
        logger.debug("subdomain found, filling list...");
        // found subdomain
        //List<DomainValue> list = new LinkedList<DomainValue>();
        newListDV.add(dv);
      }

      // check subdomains
      List<DomainValue> subDomains = dv.getDomainValuesForDomainValueId2();
      if (subDomains != null)
      {
        getDomainValueListFromId(subDomains, newListDV, domainValueId);
      }
    }

    // nothing found, return default list
    logger.debug("nothing found, return default list");
  }

  public GenericTreeRowType createTreeNode(Object obj, List<CodeSystem> listCS, List<ValueSet> listVS)
  {
    GenericTreeRowType row = new GenericTreeRowType(null);

    GenericTreeCellType[] cells = new GenericTreeCellType[2];

    if (obj instanceof CodeSystem)
    {
      CodeSystem cs = (CodeSystem) obj;

      Label label = new Label(cs.getName());
      label.setTooltiptext(cs.getDescription());

      Treecell tc = new Treecell();
      tc.appendChild(label);

      cells[0] = new GenericTreeCellType(tc, false, "");
      cells[1] = new GenericTreeCellType("CS", false, "");
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
          //SessionHelper.setValue("selectedCS", cs);
          SessionHelper.setSelectedCatalog(cs);
          SessionHelper.setValue("loadCS", null);
        }
      }

    }
    else if (obj instanceof CodeSystemVersion)
    {
      CodeSystemVersion csv = (CodeSystemVersion) obj;

      cells[0] = new GenericTreeCellType(csv.getName(), false, "");
      cells[1] = new GenericTreeCellType("CSV", false, "");
      row.setData(csv);

    }
    else if (obj instanceof ValueSet)
    {
      ValueSet vs = (ValueSet) obj;

      Label label = new Label(vs.getName());
      label.setTooltiptext(vs.getDescription());

      Treecell tc = new Treecell();
      tc.appendChild(label);

      cells[0] = new GenericTreeCellType(tc, false, "");
      cells[1] = new GenericTreeCellType("VS", false, "");
      row.setData(vs);
    }
    else if (obj instanceof DomainValue)
    {
      DomainValue dv = (DomainValue) obj;

      cells[0] = new GenericTreeCellType(dv.getDomainDisplay(), false, "", "font-weight:bold; color:green;");
      cells[1] = new GenericTreeCellType("", false, "");
      row.setData(dv);

      // Gibts DomainValues im DV? Wenn ja, einfügen
      List<DomainValue> subDomains = dv.getDomainValuesForDomainValueId2();
      if (subDomains != null)
      {
        Collections.sort(subDomains, new ComparatorDomainValues(true));
        for (DomainValue dv2 : subDomains)
        {
          row.getChildRows().add(createTreeNode(dv2, listCS, listVS));
        }
      }

      // Gibts CSs im DV? Wenn ja, einfügen
      List<CodeSystem> subCSList = dv.getCodeSystems();
      if (subCSList != null)
      {
        Collections.sort(subCSList, new ComparatorCodesystems(true));
        for (CodeSystem cs : subCSList)
        {
          row.getChildRows().add(createTreeNode(cs, listCS, listVS));
        }
      }

      // Gibts VSs im DV? Wenn ja, einfügen
      List<ValueSet> subVSList = dv.getValueSets();
      if (subVSList != null)
      {
        Collections.sort(subVSList, new ComparatorCodesystems(true));
        for (ValueSet vs : subVSList)
        {
          row.getChildRows().add(createTreeNode(vs, listCS, listVS));
        }
      }
    }
    else
      return null;    // Kein DV oder CS

    row.setCells(cells);

    return row;
  }

//  public CodeSystem findCodeSystem(String Name, String OID, long CodesystemVersionId)
//  {
//    logger.debug("findCodeSystem()");
//    logger.debug("Name: " + Name);
//    logger.debug("OID: " + OID);
//    logger.debug("CodesystemVersionId: " + CodesystemVersionId);
//    //List<CodeSystem> list = getListCS();
//
//    if(list == null)
//    {
//      // session might not work
//      
//    }
//    
//    if (list != null)
//    {
//      for (CodeSystem cs : list)
//      {
//        for (CodeSystemVersion csv : cs.getCodeSystemVersions())
//        {
//          if ((OID != null && OID.length() > 0 && OID.equalsIgnoreCase(csv.getOid()))
//                  || (CodesystemVersionId > 0 && csv.getVersionId() == CodesystemVersionId))
//          {
//            logger.debug("Found CS with id: " + cs.getId());
//            logger.debug("Found CSV with versionId: " + csv.getVersionId());
//            csv.setCodeSystem(cs);
//            SessionHelper.setValue("selectedCSV", csv);
//            return cs;
//          }
//        }
//
//        if (Name != null && Name.length() > 0 && Name.equalsIgnoreCase(cs.getName()))
//        {
//          logger.debug("Found CS with id: " + cs.getId());
//          return cs;
//        }
//      }
//    }
//
//    return null;
//  }
  /**
   * @return the errorMessage
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }

  /**
   * @return the listCS
   */
//  public List<CodeSystem> getListCS()
//  {
//    initData();
//    
//    List<CodeSystem> list = SessionHelper.getCodesystemList();
//    if(list == null)
//      return publicCodeSystemList;
//    return list;
//    //return listCS;
//  }
}
