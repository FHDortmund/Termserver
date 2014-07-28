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
package de.fhdo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Separator;
import org.zkoss.zul.South;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class GenericTree extends Window implements IDoubleClick, IUpdateData
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private List<GenericTreeRowType> dataList;
  private List<GenericTreeHeaderType> listHeader;
  private GenericTreeItemRenderer treeitemRenderer;
  private DefaultTreeModel treeModel;
  //private ListModelList listModelFilter;
  private IGenericTreeActions treeActions;
  private IOnDrop dropEvent;
  private boolean button_new;
  private boolean button_edit;
  private boolean button_delete;
  private boolean draggable;
  //private GenericTreeItemRenderer listitemRenderer;
  //private IGenericTreeActions treeActions;
  private IUpdateData updateDataListener;
  //private int lastSelectedIndex;
  private Treeitem lastSelectedTreeitem;
  private boolean oneFilter;
  private List<Button> customButtonList;
  private int countButtonsAtBegin;
  private String listId;
  private Tree tree;
  private boolean autoExpandAll;
  private Menupopup menupopup;
  private boolean showFilter;
  private boolean showExpandCollapse;
  String classFilter = "";
  private boolean multiple;

  public GenericTree()
  {
    listHeader = new LinkedList<GenericTreeHeaderType>();
    //lastSelectedIndex = -1;
    oneFilter = false;
    customButtonList = new LinkedList<Button>();

    showFilter = false;
    showExpandCollapse = false;

  }

  public void onTreeSelected(SelectEvent event)
  {
    logger.debug("onTreeSelected()");
    Object o = getSelection();
    showButtonMode(o);

    if (treeActions != null)
    {
      treeActions.onTreeSelected(listId, ((GenericTreeRowType) o).getData());
    }
  }

  public void onTreeDropped(DropEvent event)
  {
    logger.debug("onTreeDropped()");

    if (dropEvent != null)
    {
      dropEvent.onTreeDropped(event);
    }

  }

  private void showButtonMode()
  {
    Object o = getSelection();
    showButtonMode(o);
  }

  private void showButtonMode(Object o)
  {
    try
    {
      boolean disabled = (o == null);

      ((Button) getFellow("buttonEdit")).setDisabled(disabled);
      ((Button) getFellow("buttonDelete")).setDisabled(disabled);

      ((Button) getFellow("buttonNew")).setDisabled(false);

      for (int i = 0; i < customButtonList.size(); ++i)
      {
        Object ob = customButtonList.get(i).getAttribute("disabled");
        if (ob == null || (Boolean) ob == true)
          customButtonList.get(i).setDisabled(disabled);
      }
    }
    catch (Exception e)
    {
      logger.error("Fehler in showButtonMode(): " + e.getLocalizedMessage());
    }
  }

  /*public void setSelection(Object o)
   {
    
   }*/
  public Object getSelection()
  {
    //logger.debug("getSelection()");

    Object object = null;

    try
    {
      Tree tree = (Tree) getFellow("generictree");

      Treeitem ti = tree.getSelectedItem();
      if (ti != null)
      {
        object = ti.getValue();
      }
      lastSelectedTreeitem = ti;
    }
    catch (Exception ex)
    {
      logger.warn("[GenericList.java] getSelection-Fehler: " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }

    return object;
  }

  public void setSelectedItem(Treeitem ti)
  {
    initFellowTree();
    tree.setSelectedItem(ti);
  }

  public void setSelectedIndex(int index)
  {
    try
    {
      tree.setSelectedItem((Treeitem) tree.getItems().toArray()[index]);
    }
    catch (Exception e)
    {
    }
  }

  /**
   * @return the dataList
   */
  public List<GenericTreeRowType> getDataList()
  {
    return dataList;
  }

  /**
   * @param dataList the dataList to set
   */
  public void setDataList(List<GenericTreeRowType> dataList)
  {
    this.dataList = dataList;
    createTreeModel();
  }

  private List<TreeNode> createTreeNodeList(List<GenericTreeRowType> dataList)
  {
    List<TreeNode> list = new ArrayList<TreeNode>();

    for (int i = 0; i < dataList.size(); ++i)
    {
      GenericTreeRowType row = dataList.get(i);
      if (row.getChildRows().size() == 0)
      {
        //logger.debug("addLeaf: " + ((GenericTreeCellType)row.getCells()[0]).getData());
        list.add(new DefaultTreeNode(row));
      }
      else
      {
        //logger.debug("addTree: " + ((GenericTreeCellType)row.getCells()[0]).getData());
        list.add(new DefaultTreeNode(row, createTreeNodeList(row.getChildRows())));
      }
      //TreeNode treeNode = new DefaultTreeNode()
      //list.add(new DefaultTreeNode(item, ));

    }

    return list;
  }

  private void createTreeModel()
  {
    logger.debug("createTreeModel()");

    //treeModel.setMultiple(multiple);
    if(multiple)
    {
      //tree.set
      //treeModel.setMultiple(true);
      //lb.setMultiple(true);
      //lb.setCheckmark(true);
    }
    /*
     List<TreeNode> dataList = new LinkedList<TreeNode>();
     List<TreeNode> subList = new LinkedList<TreeNode>();
     subList.add(new DefaultTreeNode(createRow(new Organisation("FB 1", "", true, null))));
     subList.add(new DefaultTreeNode(createRow(new Organisation("FB 2", "", true, null))));
     //TreeNode tn1 = new DefaultTreeNode(tn, subList);
    
     GenericTreeRowType row = createRow(new Organisation("FH Dortmund", "Dortmund", true, new Date(88,4,14)));
     TreeNode tn = new DefaultTreeNode(row, subList);
    
     dataList.add(tn);
     dataList.add(new DefaultTreeNode(createRow(new Organisation("Westfalenstadion", "Dortmund", false, new Date(68,4,14)))));
     */



    //TreeNode root = new DefaultTreeNode(root)
    //TreeNode tnRoot = new DefaultTreeNode(null, dataList); 
    TreeNode tnRoot = new DefaultTreeNode(null, createTreeNodeList(dataList));
    //TreeNode tnRoot = new DefaultTreeNode(dataList); 
    treeModel = new DefaultTreeModel(tnRoot);
    
    treeModel.setMultiple(multiple);

    //Tree tree = (Tree) getFellow("generictree");
    initFellowTree();
    tree.setModel(treeModel);

    if (treeitemRenderer == null)
    {
      treeitemRenderer = new GenericTreeItemRenderer(listHeader);
      treeitemRenderer.setDoubleClickEvent(this);
      treeitemRenderer.setUpdateDataEvent(this);
      treeitemRenderer.setAutoExpandAll(autoExpandAll);
      treeitemRenderer.setDropEvent(dropEvent);
      treeitemRenderer.setContextMenuPopup(menupopup);
      treeitemRenderer.setDraggable(draggable);
      tree.setItemRenderer(treeitemRenderer);
    }


    showButtonMode();
    /*

     Listbox lb = (Listbox) getFellow("listbox");
     lb.setModel(listModel);

     if (listitemRenderer == null)
     {
     listitemRenderer = new GenericListItemRenderer(listHeader);
     listitemRenderer.setDoubleClickEvent(this);
     listitemRenderer.setUpdateDataEvent(this);
     lb.setItemRenderer(listitemRenderer);
     }

     showButtonMode();*/
  }

  private void setSouthHeight()
  {
    South south = (South) getFellow("south");

    Div div = (Div) getFellow("divEditButtons");
    if (div.isVisible())
    {
      south.setSize("33px");
      south.setVisible(true);
    }
    else
    {
      south.setSize("0px");
      south.setVisible(false);
    }
    //south
  }

  /**
   * @param button_new the button_new to set
   */
  public void setButton_new(boolean button_new)
  {
    this.button_new = button_new;

    Div div = (Div) getFellow("divEditButtons");
    div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

    setSouthHeight();

    Button button = (Button) getFellow("buttonNew");
    button.setVisible(button_new);
  }

  /**
   * @param button_edit the button_edit to set
   */
  public void setButton_edit(boolean button_edit)
  {
    this.button_edit = button_edit;

    Div div = (Div) getFellow("divEditButtons");
    div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

    setSouthHeight();

    Button button = (Button) getFellow("buttonEdit");
    button.setVisible(button_edit);
  }

  /**
   * @param button_delete the button_delete to set
   */
  public void setButton_delete(boolean button_delete)
  {
    this.button_delete = button_delete;

    Div div = (Div) getFellow("divEditButtons");
    div.setVisible(button_new || button_edit || button_delete || customButtonList.size() > 0);

    setSouthHeight();

    Button button = (Button) getFellow("buttonDelete");
    button.setVisible(button_delete);
  }

  /**
   * @param listHeader the listHeader to set
   */
  public void setListHeader(List<GenericTreeHeaderType> listHeader)
  {
    oneFilter = false;
    this.listHeader = listHeader;

    // Header zuweisen
    Treecols treecols = (Treecols) getFellow("treecols");

    //Listhead listhead = (Listhead) getFellow("listHeader");
    //listhead.getChildren().clear();
    if (treecols != null)
    {
      treecols.getChildren().clear();

      // Bestimmen, ob es einen Filter gibt
      /*for (int i = 0; i < listHeader.size(); ++i)
       {
       if (listHeader.get(i).isShowFilter())
       {
       oneFilter = true;
       break;
       }
       }*/

      for (int i = 0; i < listHeader.size(); ++i)
      {
        listHeader.get(i).setIndex(i);
        final GenericTreeHeaderType head = listHeader.get(i);

        Treecol treecol = new Treecol(head.getName());
        treecol.setImage(head.getImage());
        if (head.getWidth() > 0)
          treecol.setWidth(head.getWidth() + "px");
        //treecol.setWidth("100%");

        if (head.isHflexMin())
          treecol.setHflex("min");
        //treecol.setHflex("min");

        // TODO weitere Filter einfügen
        /*if (head.isShowFilter())
         else*/
        /*{
         // Größe anpassen ohne Filter
         logger.debug("oneFilter: " + oneFilter);
         if (oneFilter)
         {
         lh.appendChild(new Separator());
         lh.appendChild(new Separator());
         lh.appendChild(new Separator());
         lh.appendChild(new Separator());
         }
         }*/

        treecols.getChildren().add(treecol);
      }
    }

    if (treeitemRenderer != null)
    {
      treeitemRenderer.setListHeader(listHeader);
    }
  }

  public void onNew() throws InterruptedException
  {
    lastSelectedTreeitem = null;

    if (getTreeActions() != null)
    {
      getTreeActions().onTreeNewClicked(listId, null);
    }
  }

  public void onNewSubentry()
  {
    logger.debug("onNewSubentry()");
    if (getTreeActions() != null)
    {
      Object o = getSelection();

      if (o != null)
      {
        getTreeActions().onTreeNewClicked(listId, ((GenericTreeRowType) o).getData());
      }
      else
      {
        Clients.alert("Keine Auswahl! Bitte wählen Sie einen Eintrag aus der Liste aus");
      }

    }
  }

  public void onDoubleClick(Object o)
  {
    try
    {
      onEdit();
    }
    catch (InterruptedException ex)
    {
      logger.error(ex.getLocalizedMessage());
    }
  }

  public void onEdit() throws InterruptedException
  {
    logger.debug("onEdit()");

    if (getTreeActions() != null)
    {
      Object o = getSelection();

      if (o != null)
      {
        getTreeActions().onTreeEditClicked(listId, ((GenericTreeRowType) o).getData());
      }
      else
      {
        Clients.alert("Keine Auswahl! Bitte wählen Sie einen Eintrag aus der Liste aus");
      }
    }
  }

  public void onDelete() throws InterruptedException
  {
    if (getTreeActions() != null)
    {
      try
      {
        // Dialog + Eintrag löschen
        if (logger.isDebugEnabled())
          logger.debug("onDeleteClicked");

        Object o = getSelection();

        if (o != null)
        {
          if (Messagebox.show("Möchten Sie den ausgewählten Eintrag wirklich löschen?", "Löschen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION) == Messagebox.YES)
          {
            // Baum dynamisch erneuern
            if (treeActions.onTreeDeleted(listId, ((GenericTreeRowType) o).getData()))
            {
              DefaultTreeNode selectedTreeNode = (DefaultTreeNode) lastSelectedTreeitem.getAttribute("treenode");
              TreeNode parentNode = selectedTreeNode.getParent();
              logger.debug("Anzahl Node-Kinder: " + parentNode.getChildCount());
              if (parentNode.getChildCount() > 1)
              {
                parentNode.remove(selectedTreeNode);
              }
              else
              {
                // Sonderfall (!)
                Tree tree = (Tree) getFellow("generictree");
                Treeitem ti = tree.getSelectedItem();

                ti.detach();
                parentNode.remove(selectedTreeNode);
              }
            }

            //treeActions.onTreeDeleted(listId, ((GenericTreeRowType) o).getData());
          }
          else
            logger.info("Nein geklickt");
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage());

        ex.printStackTrace();
      }
    }
  }

  public void addEntry(GenericTreeRowType row, boolean rootElement)
  {
    logger.debug("addEntry(): " + row.getClass().getCanonicalName() + ", root: " + rootElement);

    DefaultTreeNode selectedTreeNode = null;
    if (rootElement || lastSelectedTreeitem == null)
    {
      selectedTreeNode = (DefaultTreeNode) treeModel.getRoot();
      selectedTreeNode.add(new DefaultTreeNode(row));
    }
    else
    {
      selectedTreeNode = (DefaultTreeNode) lastSelectedTreeitem.getAttribute("treenode");

      if (selectedTreeNode.isLeaf())
      {
        // Besonderer Fall, da es noch kein Subelement gibt
        // hier muss der TreeNode erneut mit Kindern angelegt werden

        // Aufbau ZUL
        // Tree 
        //  -> Treechildren 
        //     -> Treeitem 
        //        -> Treerow
        //           -> Component
        //        -> Treechildren
        //           -> Treeitem
        //              -> Treerow

        // Aufbau Model
        // Root
        // -> (Default)TreeNode
        //    -> (Default)TreeNode
        //    -> (Default)TreeNode
        // -> (Default)TreeNode
        //    -> (Default)TreeNode
        //    -> (Default)TreeNode
        //    -> (Default)TreeNode
        // -> (Default)TreeNode

        List<TreeNode> children = new LinkedList<TreeNode>();
        children.add(new DefaultTreeNode(row));

        TreeNode node = new DefaultTreeNode(lastSelectedTreeitem.getValue(), children);  // selectedTreeNode

        TreeNode parent = selectedTreeNode.getParent();
        int index = parent.getIndex(selectedTreeNode);
        parent.remove(selectedTreeNode);
        parent.insert(node, index);
      }
      else
      {
        selectedTreeNode.add(new DefaultTreeNode(row));
      }

    }
  }

  public void updateEntry(GenericTreeRowType row)
  {
    logger.debug("updateEntry(): " + row.getClass().getCanonicalName());

    /*DefaultTreeNode selectedTreeNode = null;
     selectedTreeNode = (DefaultTreeNode) treeModel.getRoot();
    
     selectedTreeNode.add(new DefaultTreeNode(row));*
     */

    if (lastSelectedTreeitem != null)
    {
      // Center -> Tree -> Treechildren -> Treeitem
      //logger.debug("Class1: " +lastSelectedTreeitem.getParent().getClass().getCanonicalName());
      //logger.debug("Class2: " +lastSelectedTreeitem.getParent().getParent().getClass().getCanonicalName());
      //logger.debug("Class3: " +lastSelectedTreeitem.getParent().getParent().getParent().getClass().getCanonicalName());

      // lastSelectedTreeitem.getValue() ist de.fhdo.tree.GenericTreeRowType
      DefaultTreeNode selectedTreeNode = (DefaultTreeNode) lastSelectedTreeitem.getAttribute("treenode");
      if (selectedTreeNode != null)
      {
        if (selectedTreeNode.isLeaf())
        {
          // Einfach, keine Kinder
          selectedTreeNode.setData(row);
        }
        else
        {
          // Problem: 
          logger.debug("Row ist kein Child!");
          // Aufbau ZUL
          // Tree 
          //  -> Treechildren 
          //     -> Treeitem 
          //        -> Treerow
          //           -> Component
          //        -> Treechildren
          //           -> Treeitem
          //              -> Treerow

          // Aufbau Model
          // Root
          // -> (Default)TreeNode
          //    -> (Default)TreeNode
          //    -> (Default)TreeNode
          // -> (Default)TreeNode
          //    -> (Default)TreeNode
          //    -> (Default)TreeNode
          //    -> (Default)TreeNode
          // -> (Default)TreeNode

          //selectedTreeNode.getParent()

          /*List childrenSaved = new LinkedList(selectedTreeNode.getChildren());
           logger.debug("Laenge childrenSaved: " + childrenSaved.size());
           logger.debug("isLeaf 1: " + selectedTreeNode.isLeaf());
           row.setChildRows(null);
           logger.debug("isLeaf 2: " + selectedTreeNode.isLeaf());
           selectedTreeNode.setData(row);
           logger.debug("isLeaf 3: " + selectedTreeNode.isLeaf());

           selectedTreeNode.getChildren().clear();
           logger.debug("Laenge childrenSaved: " + childrenSaved.size());
           selectedTreeNode.getChildren().addAll(childrenSaved);

           logger.debug("isLeaf 4: " + selectedTreeNode.isLeaf());*/

          TreeNode parent = selectedTreeNode.getParent();
          int index = parent.getIndex(selectedTreeNode);
          parent.remove(selectedTreeNode);
          selectedTreeNode.setData(row);
          parent.insert(selectedTreeNode, index);

        }
      }


    }
    else
      logger.debug("lastSelectedTreeitem ist null");
    /*if (lastSelectedIndex >= 0)
     {
     listModel.remove(lastSelectedIndex);
     listModel.add(lastSelectedIndex, row);
     }*/
    //treeModel.

    //getSelection()

  }

  /**
   * Klappt alle Baum-Elemente auf
   */
  public void expandAll()
  {
    logger.debug("Expand all");
    initFellowTree();

    logger.debug("Root: " + tree.getClass().getCanonicalName());
    doCollapseExpandAll(tree, true);

  }

  private void initFellowTree()
  {
    if (tree == null)
      tree = (Tree) getFellow("generictree");
  }

  /**
   * Klappt alle TreeElemente aus oder ein
   *
   * @param component
   * @param aufklappen
   */
  private void doCollapseExpandAll(Component component, boolean aufklappen)
  {
    if (component instanceof Treeitem)
    {
      logger.debug("Component ist Treeitem, jetzt aufklappen");
      Treeitem treeitem = (Treeitem) component;
      treeitem.setOpen(aufklappen);
    }
    else
      logger.debug("Component ist kein Treeitem, sondern: " + component.getClass().getCanonicalName());

    Collection<?> com = component.getChildren();
    if (com != null)
    {
      for (Iterator<?> iterator = com.iterator(); iterator.hasNext();)
      {
        doCollapseExpandAll((Component) iterator.next(), aufklappen);
      }
    }
  }

  public void setContextMenu(Menupopup mp)
  {
    //initFellowTree();
    //tree.setContext(mp);
    menupopup = mp;

    if (treeitemRenderer != null)
      treeitemRenderer.setContextMenuPopup(menupopup);

  }

  public void onCellUpdated(int cellIndex, Object data, GenericTreeRowType row)
  {
    if (updateDataListener != null)
      updateDataListener.onCellUpdated(cellIndex, data, row);
  }

  public void expandTree()
  {
    initFellowTree();
    doCollapseExpandAll(tree, true);
  }

  public void collapseTree()
  {
    initFellowTree();
    doCollapseExpandAll(tree, false);
  }

  private void setTextAndFocus(String ID, String Value)
  {
    Textbox t = (Textbox) getFellow(ID);
    t.setText(Value);
    t.setFocus(true);
    t.setSelectionRange(Value.length(), Value.length());
  }

  private void filterClasses()
  {
    initFellowTree();
    filterTree(classFilter, tree);
  }

  public void filterChanged(InputEvent ie)
  {
    classFilter = ie.getValue();
    filterClasses();
    setTextAndFocus(ie.getTarget().getId(), ie.getValue());
  }

  private void filterTree(String text, Tree tree)
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
  }

  private int filterTreeitem(String text, Treeitem treeitem)
  {
    boolean isLeaf = true;
    String s = "";

    int anzahlSub = 0;

    Iterator it = treeitem.getChildren().iterator();
    while (it.hasNext())
    {
      Object o = it.next();

      //logger.debug("Class: " + o.getClass().getCanonicalName());

      if (o instanceof Treechildren)
      {
        isLeaf = false;
        Treechildren tc = (Treechildren) o;

        //Iterator<Treeitem> it2 = tc.getChildren().iterator();

        //while (it2.hasNext())
        for (Component c_it2 : tc.getChildren())
        {
          if (c_it2 instanceof Treeitem)
          {
            Treeitem ti = (Treeitem) c_it2;
            anzahlSub += filterTreeitem(text, ti);
          }
        }
      }
      else if (o instanceof Treerow)
      {
        // Wert lesen

        Treerow tr = (Treerow) o;
        //logger.debug("Treerow erkannt, Childs: " + tr.getChildren().size());
        Iterator itTR = tr.getChildren().iterator();
        while (itTR.hasNext())
        {
          Object trObject = itTR.next();
          //logger.debug("Object: " + trObject.getClass().getCanonicalName());
          if (trObject instanceof Treecell)
          {
            Treecell tc = (Treecell) trObject;
            s = tc.getLabel();

            if (tc.getChildren().size() > 0)
            {
              Label l = (Label) tc.getChildren().get(0);
              s = l.getValue();
            }
          }
        }
      }

    }

    if (isLeaf && s.length() > 0)
    {
      // Blatt-Element, hier muss gefiltert werden


      if (s.toLowerCase().contains(text))
      {
        anzahlSub++;
        treeitem.setVisible(true);

        //logger.debug("Text (true): " + s + ",t: " + text);
      }
      else
      {
        // Das hier ausblenden
        treeitem.setVisible(false);

        //logger.debug("Text (false): " + s + ",t: " + text);
      }

      //logger.debug("Leaf: " + s);

    }
    else if (isLeaf == false && s.length() > 0)
    {
      //logger.debug("No-Leaf: " + s);
      //logger.debug("Anzahl Sub: " + anzahlSub);

      treeitem.setVisible(anzahlSub > 0);
    }

    return anzahlSub;
  }

  /**
   * @return the treeActions
   */
  public IGenericTreeActions getTreeActions()
  {
    return treeActions;
  }

  /**
   * @param treeActions the treeActions to set
   */
  public void setTreeActions(IGenericTreeActions treeActions)
  {
    this.treeActions = treeActions;
  }

  /**
   * @param updateDataListener the updateDataListener to set
   */
  public void setUpdateDataListener(IUpdateData updateDataListener)
  {
    this.updateDataListener = updateDataListener;
  }

  /**
   * @param autoExpandAll the autoExpandAll to set
   */
  public void setAutoExpandAll(boolean autoExpandAll)
  {
    this.autoExpandAll = autoExpandAll;

    if (treeitemRenderer != null)
      treeitemRenderer.setAutoExpandAll(autoExpandAll);
  }

  /**
   * @param _dropEvent the dropEvent to set
   */
  public void setDropEvent(IOnDrop _dropEvent)
  {
    logger.debug("[GenericTree.java] setDropEvent()");
    this.dropEvent = _dropEvent;

    initFellowTree();
    if (tree != null)
    {
      tree.setDroppable("true");

      Iterable<EventListener<? extends Event>> it = tree.getEventListeners(Events.ON_DROP);
      if (it != null && it.iterator() != null && it.iterator().hasNext() == false)
      {
        logger.debug("Drop-Event Listener hinzufügen");
        
        tree.addEventListener(Events.ON_DROP, new EventListener<Event>()
        {
          public void onEvent(Event t) throws Exception
          {
            logger.debug("DROP on list");

            if (dropEvent != null)
              dropEvent.onTreeDropped((DropEvent) t);
          }
        });
      }
    }

    if (treeitemRenderer != null)
      treeitemRenderer.setDropEvent(dropEvent);
  }

  /**
   * @return the tree
   */
  public Tree getTree()
  {
    initFellowTree();
    return tree;
  }

  /**
   * @param showFilter the showFilter to set
   */
  public void setShowFilter(boolean showFilter)
  {
    this.showFilter = showFilter;

    ((Div) getFellow("divFilter")).setVisible(showFilter);
  }

  /**
   * @param showExpandCollapse the showExpandCollapse to set
   */
  public void setShowExpandCollapse(boolean showExpandCollapse)
  {
    this.showExpandCollapse = showExpandCollapse;

    ((Div) getFellow("divExpandCollapse")).setVisible(showFilter);
  }

  /**
   * @return the draggable
   */
  public boolean isDraggable()
  {
    return draggable;
  }

  /**
   * @param draggable the draggable to set
   */
  public void setDraggable(boolean draggable)
  {
    this.draggable = draggable;
  }

  /**
   * @return the multiple
   */
  public boolean isMultiple()
  {
    return multiple;
  }

  /**
   * @param multiple the multiple to set
   */
  public void setMultiple(boolean multiple)
  {
    this.multiple = multiple;
  }
  
  public void cleanup()
  {
    Tree tree2 = getTree();
    tree2.getChildren().clear();
    
    Treecols cols = new Treecols();
    cols.setSizable(true);
    cols.setId("treecols");
    tree2.appendChild(cols);
  }
  
  public void addCustomButton(Button button)
  {
    logger.debug("addCustomButton");

    Object o = button.getAttribute("disabled");
    if (o == null || (Boolean) o == true)
      button.setDisabled(true);

    button.setHeight("24px");

    if (customButtonList.contains(button) == false)
    {
      customButtonList.add(button);
    }
    else
      logger.debug("Button bereits vorhanden");

    boolean alignRight = false;
    Object ob = button.getAttribute("right");
    if (ob != null && (Boolean) ob == true)
      alignRight = true;

    logger.debug("right: " + alignRight);

    if (alignRight)
    {
      Div div = (Div) getFellow("divEditButtonsRight");
      div.setVisible(true);

      Separator sep = new Separator();
      sep.setSpacing("4px");
      sep.setOrient("vertical");

      div.appendChild(sep);
      div.appendChild(button);
    }
    else
    {
      Div div = (Div) getFellow("divEditButtons");
      div.setVisible(true);

      if (countButtonsAtBegin == 0)
      {
        countButtonsAtBegin = div.getChildren().size();
      }

      Separator sep = new Separator();
      sep.setSpacing("4px");
      sep.setOrient("vertical");

      div.appendChild(sep);
      div.appendChild(button);
    }

    setSouthHeight();
  }

  /**
   * Entfernt alle benutzerdefinierte Buttons
   */
  public void removeCustomButtons()
  {
    Div div = (Div) getFellow("divEditButtons");

    if (countButtonsAtBegin == 0)
    {
      countButtonsAtBegin = div.getChildren().size();
    }

    int anzahl = div.getChildren().size();
    for (int i = countButtonsAtBegin; i < anzahl; ++i)
    {
      div.getChildren().remove(countButtonsAtBegin);
    }

    div = (Div) getFellow("divEditButtonsRight");
    div.getChildren().clear();

    customButtonList.clear();

  }
}
