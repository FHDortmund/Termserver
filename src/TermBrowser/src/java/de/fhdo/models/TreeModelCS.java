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
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.WebServiceHelper;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyRequestType;
import de.fhdo.terminologie.ws.search.ListCodeSystemsInTaxonomyResponse.Return;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Desktop;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.DomainValue;

/**
 *
 * @author Robert Mützner
 */
public class TreeModelCS
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private static Desktop desktop;
//    private static TreeModelCS instance  = null;  
  private static TreeModel treeModel = null;
  private static ArrayList<CodeSystemVersion> csvList = new ArrayList<CodeSystemVersion>();

  public static ArrayList<CodeSystemVersion> getCsvList()
  {
    return csvList;
  }

  public static void reloadData(Desktop d)
  {
    createModel(d);
  }

  private static void createModel(Desktop d)
  {
    logger.info("--- TreeModelCS, initData -------------------");

    desktop = d;
    try
    {
      csvList.clear();

      ListCodeSystemsInTaxonomyRequestType parameter = new ListCodeSystemsInTaxonomyRequestType();

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

      Return response = WebServiceHelper.listCodeSystemsInTaxonomy(parameter);

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        List list = new LinkedList();

        //provideOrderChoosen by AdminSettings
        Map<Integer, DomainValue> mapDomVal = new HashMap<Integer, DomainValue>();

        // Domain Values mit untergeordneten DV,CS und CSVs
        for (DomainValue dv : response.getDomainValue())
        {
          if (dv.getOrderNo() != null)
          {
            mapDomVal.put(dv.getOrderNo(), dv);
          }
          else
          {
            mapDomVal.put(response.getDomainValue().size(), dv);
          }
        }

        for (int i = 1; i < mapDomVal.size() + 1; i++)
        {
          list.add(createTreeNode(mapDomVal.get(i)));
        }
        TreeNode tn_root = new TreeNode(null, list);
        treeModel = new TreeModel(tn_root);
      }

    }
    catch (Exception e)
    {
      // Bei Fehler, leere Liste zurück geben
      treeModel = new TreeModel(new TreeNode(null, new LinkedList()));
      logger.error("Fehler in TreeModelCS, initData():" + e.getMessage());

      e.printStackTrace();
    }
  }

  private static TreeNode createTreeNode(Object x)
  {
    TreeNode tn = new TreeNode(x);

    if (x instanceof CodeSystem)
    {
      CodeSystem cs = (CodeSystem) x;

      // Kinder (CodeSystemVersions) suchen und dem CodeSystem hinzufügen            
      for (CodeSystemVersion csv : cs.getCodeSystemVersions())
      {
                //logger.debug("Version: " + csv.getName());

        csv.setCodeSystem(cs);

        // liste von CSV aufbauen
        csvList.add(csv);

        // CSV in das CS einh?ngen
        tn.add(new TreeNode(csv));
      }
    }
    else if (x instanceof DomainValue)
    {
      DomainValue dv = (DomainValue) x;

      // Gibts DomainValues im DV? Wenn ja, einf?gen  
      for (DomainValue dv2 : dv.getDomainValuesForDomainValueId2())
      {
        tn.add(createTreeNode(dv2));
      }

      // Gibts CSs im DV? Wenn ja, einf?gen
      for (CodeSystem cs : dv.getCodeSystems())
      {
        tn.add(createTreeNode(cs));
      }
    }
    else
      return null;    // Kein DV oder CS

    return tn;
  }

  public static TreeModel getTreeModel(Desktop d)
  {
    if (treeModel == null || desktop == null || desktop != d)
      createModel(d);

    return treeModel;
  }
}
