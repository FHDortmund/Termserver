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

import de.fhdo.gui.main.modules.ContentConcepts;
import de.fhdo.models.TreeNode;
import java.util.Collection;
import java.util.Iterator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Robert Mützner, Sven Becker
 */
public class TreeHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public static void filterTree(String text, Tree tree)
  {
    logger.debug("Filter tree: " + tree.getId());
    text = text.toLowerCase();
    doCollapseExpandAll(tree, true);

    Iterator<Treeitem> it = tree.getItems().iterator();

    while (it.hasNext())
    {
      Treeitem ti = it.next();
      Object data = ((TreeNode)ti.getValue()).getData();
      if(data instanceof CodeSystemVersion){                   
          String s = ((CodeSystemVersion)data).getCodeSystem().getName() + " : " + ((CodeSystemVersion)data).getName();
          ti.setVisible(s.toLowerCase().contains(text));
          //ti.setVisible(((CodeSystemVersion)data).getName().toLowerCase().contains(text));
      }
      else if (data instanceof ValueSetVersion){
          String s = ((ValueSetVersion)data).getValueSet().getName() + " : " + ((ValueSetVersion)data).getVersionId();
          ti.setVisible(s.toLowerCase().contains(text));
      }
      //filterTreeitem(text, ti);  // alte Version, bei der nach namen der Versionen gefiltert wurde
    }
  }
  
  public static void doCollapseExpandAll(Component component, boolean aufklappen){
      doCollapseExpandAll(component, aufklappen, null);
  }
  
  public static void doCollapseExpandAll(Component component, boolean aufklappen, ContentConcepts window)
  {
    if (component instanceof Treeitem)
    {
        
      Treeitem treeitem = (Treeitem) component;
      
      // replace dummy with real children
      if(aufklappen == true && window != null && ((TreeNode)treeitem.getValue()).getChildren().isEmpty() == false){
          TreeNode dummy = (TreeNode)((TreeNode)treeitem.getValue()).getChildren().get(0);
          if(dummy.getData() instanceof String)  {
              window.openNode((TreeNode)treeitem.getValue(), false); 
              window.updateModel(true);
          }          
      }
      treeitem.setOpen(aufklappen);     
    }
    Collection<?> com = component.getChildren();
    if (com != null)
    {
      for (Iterator<?> iterator = com.iterator(); iterator.hasNext();)
      {          
        doCollapseExpandAll((Component) iterator.next(), aufklappen, window);
      }
    }
  }
}


// Für Filterung nach Namen der Versionen, wird vorraussichtlich nicht mehr benötigt

//private static int filterTreeitem(String text, Treeitem treeitem)
//  {
//    boolean isLeaf = true;
//    String s = "";
//
//    int anzahlSub = 0;
//
//    Iterator it = treeitem.getChildren().iterator();
//    while (it.hasNext())
//    {
//      Object o = it.next();
//
//      if (o instanceof Treechildren)
//      {
//        isLeaf = false;
//        Treechildren tc = (Treechildren) o;
//
//        Iterator<Treeitem> it2 = tc.getChildren().iterator();
//        
//        while (it2.hasNext())
//        {
//          Treeitem ti = it2.next();
//          anzahlSub += filterTreeitem(text, ti);
//        }
//      }
//      else if (o instanceof Treerow)
//      {
//        // Wert lesen
//
//        Treerow tr = (Treerow) o;
//        //logger.debug("Treerow erkannt, Childs: " + tr.getChildren().size());
//        Iterator itTR = tr.getChildren().iterator();
//        while (itTR.hasNext())
//        {
//          Object trObject = itTR.next();
//          //logger.debug("Object: " + trObject.getClass().getCanonicalName());
//          if (trObject instanceof Treecell)
//          {
//            Treecell tc = (Treecell) trObject;
//            s = tc.getLabel();
//
//            if(tc.getChildren().size() > 0)
//            {
//              Label l = (Label)tc.getChildren().get(0);
//              s = l.getValue();
//            }
//          }
//        }
//      }
//    }
//
//    if (isLeaf && s.length() > 0)
//    {
//      // Blatt-Element, hier muss gefiltert werden
//      if(s.toLowerCase().contains(text))
//      {
//        anzahlSub++;
//        treeitem.setVisible(true);
//      }
//      else
//      {
//        // Das hier ausblenden
//        treeitem.setVisible(false);
//      }
//    }
//    else if(isLeaf == false && s.length() > 0)
//    {
//      treeitem.setVisible(anzahlSub > 0);
//    }
//
//    return anzahlSub;
//  }
//
//  private static String getTextFromData(Object data)
//  {
//    if (data instanceof CodeSystem)
//    {
//      return ((CodeSystem) data).getName();
//    }
//    else if (data instanceof CodeSystemVersion)
//    {
//      return ((CodeSystemVersion) data).getName();
//    }
//
//    return "";
//  }