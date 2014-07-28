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
package de.fhdo.helper;

import java.util.Collection;
import java.util.Iterator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Treeitem;

/**
 *
 * @author Robert Mützner
 */
public class TreeHelper
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void doCollapseExpandAll(Component component, boolean aufklappen)
  {
    if (component == null)
      return;

    try
    {
      if (component instanceof Treeitem)
      {
        Treeitem treeitem = (Treeitem) component;
        if (treeitem != null)
          treeitem.setOpen(aufklappen);
      }
      Collection<?> com = component.getChildren();
      if (com != null)
      {
        for (Iterator<?> iterator = com.iterator(); iterator.hasNext();)
        {
          doCollapseExpandAll((Component) iterator.next(), aufklappen);

        }
      }
    }
    catch (Exception e)
    {
    }
  }

  /*public static void filterTree(String text, Tree tree)
  {
  logger.debug("Filter tree");
  
  text = text.toLowerCase();
  
  doCollapseExpandAll(tree, true);
  
  Iterator<Treeitem> it = tree.getItems().iterator();
  
  while (it.hasNext())
  {
  Treeitem ti = it.next();
  filterTreeitem(text, ti);
  }
  
  }*/

  /*private static int filterTreeitem(String text, Treeitem treeitem)
  {
  boolean isLeaf = true;
  String s = "";
  
  int anzahlSub = 0;
  
  Iterator it = treeitem.getChildren().iterator();
  while (it.hasNext())
  {
  Object o = it.next();
  
  if (o instanceof Treechildren)
  {
  isLeaf = false;
  Treechildren tc = (Treechildren) o;
  
  Iterator<Treeitem> it2 = tc.getChildren().iterator();
  
  while (it2.hasNext())
  {
  Treeitem ti = it2.next();
  anzahlSub += filterTreeitem(text, ti);
  }
  }
  else if (o instanceof Treerow)
  {
  // Wert lesen
  Treerow tr = (Treerow) o;
  
  Iterator itTR = tr.getChildren().iterator();
  while (itTR.hasNext())
  {
  Object trObject = itTR.next();
  
  if (trObject instanceof Treecell)
  {
  Treecell tc = (Treecell) trObject;
  s = tc.getLabel();
  
  if(tc.getChildren().size() > 0)
  {
  Label l = (Label)tc.getChildren().get(0);
  s = l.getValue();
  }
  }
  }
  }
  }
  
  if (isLeaf && s.length() > 0)
  {
  // Blatt-Element, hier muss gefiltert werden
  if(s.toLowerCase().contains(text))
  {
  anzahlSub++;
  treeitem.setVisible(true);
  }
  else
  {
  // Das hier ausblenden
  treeitem.setVisible(false);
  }
  }
  else if(isLeaf == false && s.length() > 0)
  {
  treeitem.setVisible(anzahlSub > 0);
  }
  
  return anzahlSub;
  }*/
}
