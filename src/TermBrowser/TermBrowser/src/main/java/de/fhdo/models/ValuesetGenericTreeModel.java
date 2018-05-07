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
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.models.comparators.ComparatorCodesystems;
import de.fhdo.models.comparators.ComparatorDomainValues;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse;
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse;
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
public class ValuesetGenericTreeModel
{
  private static ValuesetGenericTreeModel instance = null;

  public static ValuesetGenericTreeModel getInstance()
  {
    if (instance == null)
      instance = new ValuesetGenericTreeModel();

    return instance;
  }

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean loaded = false;
  private List<ValueSet> listVS = new LinkedList();
  //private ArrayList<CodeSystemVersion> csvList = new ArrayList<CodeSystemVersion>();

  public ValuesetGenericTreeModel()
  {
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

    logger.debug("--- ValuesetGenericListModel, initData ---");

    try
    {
      //csvList.clear();
      ListValueSetsRequestType parameter = new ListValueSetsRequestType();

      // login
      if (SessionHelper.isUserLoggedIn())
      {
        parameter.setLoginToken(SessionHelper.getSessionId());
      }
      
      ListValueSetsResponse.Return response = WebServiceHelper.listValueSets(parameter);

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        listVS = response.getValueSet();
        loaded = true;
      }
    }
    catch (Exception e)
    {
      // Bei Fehler, leere Liste zurück geben
      LoggingOutput.outputException(e, this);
      listVS = new LinkedList<ValueSet>();
    }
  }
  
  public void initGenericTree(GenericTree tree, IGenericTreeActions treeActions)
  {
    initData();
    
    List<GenericTreeHeaderType> header = new LinkedList<GenericTreeHeaderType>();
    header.add(new GenericTreeHeaderType("Name", 0, "", true, "String", false, true, false));
    
    // Daten erzeugen und der Liste hinzufügen
    List<GenericTreeRowType> dataList = createModel();
    
    // Liste initialisieren
    tree.setTreeActions(treeActions);
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
      /*Map<Integer, ValueSet> mapVS = new HashMap<Integer, ValueSet>();

      // Domain Values mit untergeordneten DV,CS und CSVs
      for (ValueSet valueSet : listVS)
      {
        mapVS.put(listVS.size(), valueSet);
        if (valueSet.getOrderNo() != null)
        {
          mapDomVal.put(valueSet.getOrderNo(), valueSet);
        }
        else
        {
          //mapVS.put(listVS.size(), valueSet);
        }
      }*/
      
      for(ValueSet vs : listVS)
      {
        list.add(createTreeNode(vs));
      }

//      for (int i = 1; i < mapVS.size() + 1; i++)
//      {
//        list.add(createTreeNode(mapVS.get(i)));
//      }

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
    
    if (obj instanceof ValueSet)
    {
      ValueSet vs = (ValueSet) obj;
      
      Label label = new Label(vs.getName());
      label.setTooltiptext(vs.getDescription());
      
      Treecell tc = new Treecell();
      tc.appendChild(label);
      
      cells[0] = new GenericTreeCellType(tc, false, "");
      row.setData(vs);
    }
    else
      return null;

    row.setCells(cells);
 
    return row;
  }
}
