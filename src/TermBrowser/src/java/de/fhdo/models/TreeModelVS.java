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
import de.fhdo.terminologie.ws.search.ListValueSetsRequestType;
import de.fhdo.terminologie.ws.search.ListValueSetsResponse.Return;
import de.fhdo.terminologie.ws.search.Status;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zk.ui.Desktop;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Sven Becker
 */
public class TreeModelVS
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
//    private static TreeModelVS instance;    
  private static TreeModel treeModel;
  private static Desktop desktop;
  private static ArrayList<ValueSetVersion> vsvList = new ArrayList<ValueSetVersion>();

  public static ArrayList<ValueSetVersion> getVsvList()
  {
    return vsvList;
  }

  public static void reloadData(Desktop d)
  {
    createModel(d);
  }

  private static void createModel(Desktop d)
  {
    desktop = d;
    try
    {
      vsvList.clear();
      logger.debug("ValueSetTreeModel - initData()");

      ListValueSetsRequestType parameter = new ListValueSetsRequestType();

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

      Return response = WebServiceHelper.listValueSets(parameter);

      logger.debug("Response-Msg: " + response.getReturnInfos().getMessage());

      if (response.getReturnInfos().getStatus() == Status.OK)
      {
        List<TreeNode> list = new LinkedList<TreeNode>();

        for (int i = 0; i < response.getValueSet().size(); ++i)
        {
          list.add(createTreeNode(response.getValueSet().get(i)));
        }

        TreeNode tn_root = new TreeNode(null, list);
        treeModel = new TreeModel(tn_root);
      }

      logger.debug("ValueSetTreeModel - initData(): fertig");
    }
    catch (Exception e)
    {
      logger.error("Fehler in ValueSetTreeModel, initData():" + e.getMessage());
    }
  }

  private static TreeNode createTreeNode(ValueSet vs)
  {
    logger.debug("createTreeNode: " + vs.getName());

    TreeNode tn = new TreeNode(vs);

    // Kinder suchen
    for (ValueSetVersion vsv : vs.getValueSetVersions())
    {
            //logger.debug("Version: " + vsv.getVersionId());

      vsv.setValueSet(vs);

      // liste von CSV aufbauen
      vsvList.add(vsv);

      // Nur die aktuellste version anzeigen  // TODO: Als Eigenschaft in Accountdetails regeln ob alle versionen anzeigen oder nicht
      if (SessionHelper.isUserLoggedIn())
      {
        TreeNode childTN = new TreeNode(vsv);
        tn.getChildren().add(childTN);
      }
      else
      {
        if (vs.getCurrentVersionId().equals(vsv.getVersionId()))
        {
          TreeNode childTN = new TreeNode(vsv);
          tn.getChildren().add(childTN);
          break;
        }
      }
    }

    return tn;
  }

  public static TreeModel getTreeModel(Desktop d)
  {
    if (treeModel == null || desktop == null || desktop != d)
      createModel(d);

    return treeModel;
  }
}
