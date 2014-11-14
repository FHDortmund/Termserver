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
  private boolean loaded = false;
  private List<DomainValue> listDV = new LinkedList();
  private String errorMessage;
  //private ArrayList<CodeSystemVersion> csvList = new ArrayList<CodeSystemVersion>();

  public CodesystemGenericTreeModel()
  {
    errorMessage = "";
  }
  
  public void reloadData()
  {
    loaded = false;
    initData();
  }
  
  private void initData()
  {
    if (loaded)
      return;

    logger.debug("--- CodesystemGenericTreeModel, initData ---");
    errorMessage = "";

    try
    {
      //csvList.clear();

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
        listDV = response.getDomainValue();
        loaded = true;
      }
      else
      {
        // show error message
        errorMessage = response.getReturnInfos().getMessage();
      }
    }
    catch (Exception e)
    {
      // Bei Fehler, leere Liste zurück geben
      LoggingOutput.outputException(e, this);
      //treeModel = new TreeModel(new TreeNode(null, new LinkedList()));
    }
  }
  
  public void initGenericTree(GenericTree tree, IGenericTreeActions treeActions)
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
    
    
  }
  
  private List<GenericTreeRowType> createModel()
  {
    logger.debug("createModel()");

    try
    {
      //List list = new LinkedList();
      List<GenericTreeRowType> list = new LinkedList<GenericTreeRowType>();

      //provideOrderChoosen by AdminSettings
      Map<Integer, DomainValue> mapDomVal = new HashMap<Integer, DomainValue>();

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
        list.add(createTreeNode(mapDomVal.get(i)));
      }

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

  public GenericTreeRowType createTreeNode(Object obj)
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
        row.getChildRows().add(createTreeNode(dv2));
      }

      // Gibts CSs im DV? Wenn ja, einfügen
      List<CodeSystem> subCSList = dv.getCodeSystems();
      Collections.sort(subCSList, new ComparatorCodesystems(true));
      for (CodeSystem cs : subCSList)
      {
        row.getChildRows().add(createTreeNode(cs));
      }
    }
    else
      return null;    // Kein DV oder CS

    row.setCells(cells);
 
    return row;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }
  
  
}
