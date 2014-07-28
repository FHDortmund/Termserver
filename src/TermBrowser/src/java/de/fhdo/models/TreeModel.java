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

import de.fhdo.gui.main.ContentCSVSDefault;
import de.fhdo.helper.SessionHelper;
import java.util.Comparator;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeitemComparator;
import org.zkoss.zul.ext.Sortable;

/**
 *
 * @author mathias.aschhoff
 */
public class TreeModel extends DefaultTreeModel implements Sortable
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private DefaultTreeNode _root;
  private ContentCSVSDefault contentCSVSDefault;

  public TreeModel(DefaultTreeNode root)
  {
    super(root, true);
    _root = root;
  }

  public DefaultTreeNode get_root()
  {
    return this._root;
  }

  @Override
  public void sort(Comparator cmpr, final boolean ascending)
  {
    if (contentCSVSDefault != null)
    {
      logger.debug("SORTING!!!");
      
      if (cmpr instanceof TreeitemComparator)
      {
        // Sortierung festlegen
        TreeitemComparator tiComp = (TreeitemComparator) cmpr;
        if (tiComp.getTreecol().getColumnIndex() == 0)
        {
          // Name
          SessionHelper.setValue("SortByField", "term");
        }
        else if (tiComp.getTreecol().getColumnIndex() == 1)
        {
          // Code
          SessionHelper.setValue("SortByField", "code");
        }

        if (ascending)
          SessionHelper.setValue("SortDirection", "ascending");
        else
          SessionHelper.setValue("SortDirection", "descending");

        // Vokabular neu laden und anzeigen
        contentCSVSDefault.loadConceptsBySelectedItem(false, false);
      }
      else
        logger.debug(cmpr.toString());
    }
    else 
//        logger.debug("SORTING, null");   
        try{
            super.sort(cmpr, ascending); // TODO: hier kommt es zu einer Desktop == null Exception beim ersten Laden nach 24h?   
        } catch (Exception e){
            e.printStackTrace();
        }
  }

  /**
   * @param contentCSVSDefault the contentCSVSDefault to set
   */
  public void setContentCSVSDefault(ContentCSVSDefault contentCSVSDefault)
  {
    this.contentCSVSDefault = contentCSVSDefault;
  }
}