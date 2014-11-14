/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.tree;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 *
 * @author Robert Mützner
 */
public class GenericTreeItemRenderer implements TreeitemRenderer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private List<GenericTreeHeaderType> listHeader;
  private IDoubleClick doubleClickEvent;
  private IOnDrop dropEvent;
  private IUpdateData updateDataEvent;
  private boolean autoExpandAll;
  private boolean draggable;
  private Treecell currentTreecell;
  private EventListener selectRowListener;
  private Menupopup contextMenuPopup;

  public GenericTreeItemRenderer(List<GenericTreeHeaderType> _listHeader)
  {
    listHeader = _listHeader;

    selectRowListener = new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        try
        {
          /*Listcell lc = (Listcell) event.getTarget().getParent();
           Listitem li = (Listitem) lc.getParent();
           Listbox lb = (Listbox) li.getParent();*/

          Treecell lc = (Treecell) event.getTarget().getParent();
          Treerow tr = (Treerow) lc.getParent();
          Treeitem ti = (Treeitem) tr.getParent();
          Tree tree = ti.getTree();
          //Tree tree = (Tree)li.getParentItem();
          //Object listData = ti.getValue();

          tree.clearSelection();
          tree.setSelectedItem(ti);

          // TODO Button-Sichtbarkeit aktualisieren
          //if (updateListener != null)
          //  updateListener.update("ButtonSichtbarkeit");

        }
        catch (Exception e)
        {
          logger.warn("Fehler bei onEvent: " + e.getLocalizedMessage());
        }
      }
    };
  }

  public void render(Treeitem item, Object tn, int index) throws Exception
  {

    TreeNode treeNode = (TreeNode) tn;
    //Treerow dataRow = new Treerow();
    item.setAttribute("treenode", tn);


    if (contextMenuPopup != null)
    {
      item.setContext(contextMenuPopup);



      //logger.debug("contextMenuPopup");

      /*Menupopup contextMenuVS = new Menupopup();
       treeVS.setContext(contextMenuVS);
       contextMenuVS.setParent(this);   
       Menuitem miNewVS = new Menuitem(Labels.getLabel("contentCSVSDefault.newValueSet"));     
       miNewVS.addEventListener(Events.ON_CLICK, new EventListener(){
       public void onEvent(Event event) throws Exception {
       popupValueSet(PopupWindow.EDITMODE_CREATE);
       }            
       });
       if(SessionHelper.isUserLoggedIn()){
       miNewVS.setParent(contextMenuVS);            
       }*/

    }
    else
    {
      item.setContext("treePopupItem");
      //logger.debug("contextMenuPopup ist NULL");
    }

    //if(index == 2)
    //    item.setSelected(true);
    //logger.debug("render");


    /*boolean updateRow = false;
    
     if(item.getTreerow() != null)
     {
     //item.getChildren().clear(); // geht so nicht, da alle children gelöscht werden
     updateRow = true;
     }*/
    Object data = treeNode.getData();

    if (data instanceof GenericTreeRowType)
    {
      GenericTreeRowType row = (GenericTreeRowType) data;
      renderRow(item, row);

      if (row.isSelected())
        item.setSelected(true);

      //logger.debug("render: " + row.getCells()[0].getLabel());
    }
    else
    {
      if (data != null)
        logger.debug("Object-Type nicht gefunden: " + data.getClass().getCanonicalName());
      else
        logger.debug("Object-Type nicht gefunden: null");
    }

    /*Menupopup contextMenu = new Menupopup();
     dataRow.setParent(treeItem);
     treeItem.setValue(treeNode);

     if (data instanceof CodeSystemEntityVersion)
     {
     renderCSEVMouseEvents(dataRow, treeItem, treeNode);
     renderCSEVContextMenu(contextMenu, treeItem, dataRow, data);
     renderCSEVDisplay(dataRow, data, treeItem, treeNode);

     if (draggable)
     dataRow.setDraggable("true");

     // Nur für CS, da es für VS nicht vorgesehen ist, hierarchien aufzubauen
     if (droppable && contentMode == ContentConcepts.CONTENTMODE_CODESYSTEM)
     dataRow.setDroppable("true");
     }
     else
     {
     logger.debug("Object-Type nicht gefunden (data instanceof CodeSystemEntityVersion == false)");
     }*/

    item.setOpen(autoExpandAll);
  }

  private void renderRow(Treeitem item, GenericTreeRowType row)
  {
    Treerow treeRow = item.getTreerow();



    if (item.getTreerow() == null)
    {
      // Neuer Renderer
      treeRow = new Treerow();

      if (item.getEventListeners(Events.ON_DOUBLE_CLICK) == null
              || item.getEventListeners(Events.ON_DOUBLE_CLICK).iterator().hasNext() == false)
      {
        item.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            logger.debug("doubleclicked");

            if (doubleClickEvent != null)
              doubleClickEvent.onDoubleClick(event.getData());
          }
        });
      }

      if (row.isEnableDrop())
      {
        //logger.debug("treeRow.setDroppable(\"true\");");
        treeRow.setDroppable("true");

        treeRow.addEventListener(Events.ON_DROP, new EventListener()
        {
          public void onEvent(Event event) throws Exception
          {
            if (dropEvent != null)
              dropEvent.onTreeDropped((DropEvent) event);
          }
        });
      }


    }
    else
    {
      // Aktualisiert einen Eintrag
      logger.debug("Aktualisiert einen Eintrag");
      treeRow.getChildren().clear();
    }

    for (int i = 0; i < row.getCells().length; ++i)
    {
      GenericTreeCellType cell = row.getCells()[i];

      treeRow.appendChild(addCell(cell, listHeader.get(i), row));
    }
    
    if(draggable && row.isEnableDrag())
      treeRow.setDraggable("true");

    item.setValue(row);
    item.appendChild(treeRow);
  }

  private Treecell addCell(GenericTreeCellType data, final GenericTreeHeaderType header, final GenericTreeRowType rowData)
  {
    Treecell cell = null;

    if (data.getData() != null && data.getData() instanceof Treecell)
    {
      cell = (Treecell) data.getData();
    }
    else
    {
      cell = new Treecell();
      //cell.setValue(data.getData());

      updateCellEditing(cell, data, header, false, rowData);
    }

    if (cell != null)
    {
      if (data.getStyle() != null)
      {
        cell.setStyle(data.getStyle());
      }
      
      // Zelle editierbar machen
      cell.addEventListener(Events.ON_CLICK, new EventListener<Event>()
      {
        public void onEvent(Event t) throws Exception
        {
          try
          {
            logger.debug("Events.ON_CLICK");
            if (header.isAllowInlineEditing())
            {
              //logger.debug("event.getTarget: " + t.getTarget().getClass().getCanonicalName());
              Treecell lc = (Treecell) t.getTarget();
              Treerow tr = (Treerow) lc.getParent();
              Treeitem li = (Treeitem) tr.getParent();
              Object listData = li.getValue();

              if (currentTreecell != lc)
              {
                //if ((task.getCompleted() != null && task.getCompleted() == false) || task.getId() == 0)
                //  updateCellEditing(lc, task, true);
                GenericTreeCellType cellData = ((GenericTreeRowType) listData).getCells()[header.getIndex()];
                updateCellEditing(lc, cellData, header, true, rowData);
              }
              else
                logger.debug("Gleiche Listcell");
            }
          }
          catch (Exception ex)
          {
            logger.error("Fehler 1234: " + ex.getLocalizedMessage());
            ex.printStackTrace();
          }

        }
      });
    }

    return cell;
  }

  private void updateCellEditing(Treecell cell, GenericTreeCellType data,
          GenericTreeHeaderType header, boolean editing, GenericTreeRowType rowData)
  {
    //logger.debug("updateCellEditing: " + data.getLabel());
    //logger.debug("getData(): " + data.getData());

    boolean showLabel = true;

    //cell.setLabel("");


    if (header.isComponent() || editing
            || (data.getData() != null && data.getData() instanceof Component))
    {
      boolean createComponent = true;

      if (header.isComponent() && header.isAllowInlineEditing())
      {
        // immer Komponente, diese also ändern
        createComponent = changeComponent(cell, editing);
      }

      if (createComponent)
      {
        // Component erzeugen
        boolean compErfolg = addComponent(cell, data, header, editing, rowData);
        showLabel = !compErfolg;
      }
      else
        showLabel = false;

      //logger.debug("compErfolg: " + compErfolg);

      /*logger.debug("header.isComponent(): " + header.isComponent());
       logger.debug("editing: " + editing);
       logger.debug("data.getData(): " + data.getData().getClass().getCanonicalName());*/
    }

    if (showLabel)
    {
      String label = "";
      cell.disableClientUpdate(true);
      cell.getChildren().clear();

      if (data.isShowLabel())
      {
        label = data.getLabel();
        //cell.setLabel(data.getLabel());
        //logger.debug("setLabel: " + data.getLabel());
      }
      else
      {
        label = getString(data.getData());
        //cell.setLabel(getString(data.getData()));
        //logger.debug("setLabel (0): " + getString(data.getData()));
      }
      
      Label l = new Label(label);
      cell.appendChild(l);
      
      if(data.getStyle() != null)
        l.setStyle(data.getStyle());

      cell.disableClientUpdate(false);
      cell.invalidate();
    }


    if (editing)
    {
      currentTreecell = cell;
      //currentTask = task;
    }
    else
    {
      currentTreecell = null;
      //currentTask = null;
    }


    /*if (content == TaskListitemRenderer.CELL_CONTENT.Aufgabe || content == TaskListitemRenderer.CELL_CONTENT.NeueAufgabe)
     {
     Textbox tb = addNameTextbox(lc, task, editing);
     lc.disableClientUpdate(false);
     lc.invalidate();
     if (tb != null)
     {
     tb.setFocus(true);
     tb.select();
     tb.setSelectionRange(tb.getText().length(), tb.getText().length());
     }
     }*/
  }

  private String getString(Object o)
  {
    if (o == null)
      return "";

    if (o instanceof java.util.Date)
    {
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
      return sdf.format(o);
    }

    return o.toString();
  }

  private boolean changeComponent(Treecell cell, boolean editing)
  {
    boolean createComponent = true;

    for (int i = 0; i < cell.getChildren().size(); ++i)
    {
      Component comp = cell.getChildren().get(i);

      if (comp instanceof Checkbox)
      {
        ((Checkbox) comp).setDisabled(!editing);
        createComponent = false;
        logger.debug("Comp-Type: " + comp.getClass().getCanonicalName());
      }
      //logger.debug("Comp-Type: " + comp.getClass().getCanonicalName());
    }


    return createComponent;
  }

  private boolean addComponent(Treecell cell, GenericTreeCellType data,
          GenericTreeHeaderType header, boolean editing, GenericTreeRowType rowData)
  {
    //if (header.isComponent() == false)
    //  return false;
    //logger.debug("addComponent");

    if (editing == false && header.isComponent() == false)
      return false;

    if (cell == null)
      cell = new Treecell();

    cell.disableClientUpdate(true);
    cell.getChildren().clear();

    //logger.debug("addComponent from Type: " + data.getData().getClass().getCanonicalName());

    Component comp = null;
    /*if (data.getData() instanceof String)
     {
     if (header.getDatatype() instanceof String[])
     {
     comp = addCombobox(data.getData().toString(), "", cell, dataRow, data, header, editing, rowData);
     }
     else
     {
     comp = addTextbox(data.getData().toString(), "", cell, dataRow, data, header, editing, rowData);
     }
     }
     else */
    if (data.getData() instanceof String)
    {
      if (header.getDatatype() instanceof String[])
      {
        comp = addCombobox(data.getData().toString(), "", cell, data, header, editing, rowData);
      }
      else
      {
        comp = addTextbox(data.getData().toString(), "", cell, data, header, editing, rowData);
      }
    }
    else if (data.getData() instanceof Boolean)
    {
      // Checkbox
      comp = addCheckbox((Boolean) data.getData(), "", "", data, header, rowData);
    }
    else if (data.getData() instanceof Date)
    {
      //comp = addDatebox((Date) data.getData(), "", data, header.isAllowInlineEditing(), cell, dataRow, data, header, editing, rowData);
      comp = formatDate((Date) data.getData(), header.getDatatype());
    }
    else if (data.getData() instanceof Component)
    {
      comp = (Component) data.getData();
    }
    else
    {
      if (data.getData() != null)
        logger.warn("Component nicht gefunden, zeige Label. Comp: " + data.getData().getClass().getCanonicalName());
      else
        logger.warn("Component nicht gefunden, zeige Label. Comp: null");

      return false;
    }

    //logger.debug("addComponent to cell");

    if (comp != null)
      cell.appendChild(comp);

    cell.disableClientUpdate(false);
    cell.invalidate();

    // Komponente aktiv schalten
    if (comp instanceof Textbox)
    {
      //logger.debug("open Textbox");
      Textbox tb = (Textbox) comp;
      tb.setFocus(true);
      tb.select();
      tb.setSelectionRange(tb.getText().length(), tb.getText().length());
    }
    if (comp instanceof Datebox)
    {
      //logger.debug("open Datebox");
      Datebox db = (Datebox) comp;
      if (db != null)
      {
        db.setFocus(true);
        db.select();
        db.open();
      }
    }
    if (comp instanceof org.zkoss.zul.Combobox)
    {
      //logger.debug("open Combobox");

      org.zkoss.zul.Combobox cb = (org.zkoss.zul.Combobox) comp;
      if (cb != null)
      {
        cb.setFocus(true);
        //cb.select();
        cb.open();
      }

    }

    /*if(comp != null)
     logger.debug("comp: " + comp.getClass().getCanonicalName());
     else logger.debug("comp: null");*/

    return true;
  }

  private Component addTextbox(String Text, String Tooltip,
          final Treecell cell, final GenericTreeCellType data,
          final GenericTreeHeaderType header, boolean editing, final GenericTreeRowType rowData)
  {
    final Textbox item = new Textbox(Text);
    item.setAttribute("data", data);
    item.setTooltiptext(Tooltip);
    item.setWidth("99%");

    /*item.setFocus(true);
     item.select();
     item.setSelectionRange(item.getText().length(), item.getText().length());*/

    if (header.isAllowInlineEditing() == false)
    {
      item.setDisabled(true);
    }

    item.addEventListener(Events.ON_BLUR, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        try
        {
          logger.debug("ON_BLUR");
          //Textbox tb = (Textbox) event.getTarget();
          updateCellEditing(cell, data, header, false, rowData);
        }
        catch (Exception ex)
        {
          logger.error("Fehler in ON_BLUR (Name Textbox): " + ex.getLocalizedMessage());
        }
      }
    });

    item.addEventListener(Events.ON_CHANGE, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug("ON_CHANGE: " + Data.getClass().getCanonicalName());
        logger.debug("ON_CHANGE");
        Textbox tb = (Textbox) event.getTarget();
        //GenericListCellType cellType = (GenericListCellType) Data;
        data.setData(tb.getText());

        if (updateDataEvent != null)
        {
          // Benachrichtigung über geänderte Daten
          updateDataEvent.onCellUpdated(header.getIndex(), tb.getText(), rowData);
        }
      }
    });


    item.addEventListener(Events.ON_FOCUS, selectRowListener);

    return item;
  }

  private Component addCombobox(String Text, String Tooltip,
          final Treecell cell, final GenericTreeCellType data,
          final GenericTreeHeaderType header, boolean editing, final GenericTreeRowType rowData)
  {
    //logger.debug("addCombobox");
    final Combobox item = new Combobox(Text);
    item.setAttribute("data", data);
    item.setTooltiptext(Tooltip);
    item.setWidth("97%");
    item.setReadonly(true);
    item.setHflex("1");

    // Model erstellen
    item.setModel(new ListModelList((String[]) header.getDatatype()));

    if (header.isAllowInlineEditing() == false)
    {
      item.setDisabled(true);
    }

    item.addEventListener(Events.ON_BLUR, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON_BLUR");
        updateCellEditing(cell, data, header, false, rowData);
      }
    });

    item.addEventListener(Events.ON_CHANGE, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug("ON_CHANGE: " + Data.getClass().getCanonicalName());
        logger.debug("ON_CHANGE");
        Combobox cb = (Combobox) event.getTarget();
        data.setData(cb.getText());

        if (updateDataEvent != null)
        {
          // Benachrichtigung über geänderte Daten
          updateDataEvent.onCellUpdated(header.getIndex(), cb.getText(), rowData);
        }
      }
    });


    item.addEventListener(Events.ON_FOCUS, selectRowListener);

    return item;
  }

  private Component addCheckbox(boolean Checked, String Label, String Tooltip,
          final GenericTreeCellType data,
          final GenericTreeHeaderType header, final GenericTreeRowType rowData)
  {
    //Listcell cell = new Listcell();

    Checkbox item = new Checkbox(Label);
    item.setChecked(Checked);
    item.setAttribute("data", data);
    item.setTooltiptext(Tooltip);

    item.setDisabled(!header.isAllowInlineEditing());

    item.addEventListener(Events.ON_CHECK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug("ON_CHANGE: " + Data.getClass().getCanonicalName());
        logger.debug("ON_CHANGE");
        Checkbox cb = (Checkbox) event.getTarget();
        data.setData(cb.isChecked());

        if (updateDataEvent != null)
        {
          // Benachrichtigung über geänderte Daten
          updateDataEvent.onCellUpdated(header.getIndex(), cb.isChecked(), rowData);
        }
      }
    });


    return item;


  }

  private Component formatDate(Date datum, Object datatype)
  {
    Label l = new Label();

    try
    {
      SimpleDateFormat sdf;
      if (datatype != null && datatype.toString().equalsIgnoreCase("datetime"))
      {
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      }
      else
      {
        sdf = new SimpleDateFormat("dd.MM.yyyy");
      }

      l.setValue(sdf.format(datum));
    }
    catch (Exception e)
    {
    }



    return l;
  }

  /**
   * @param listHeader the listHeader to set
   */
  public void setListHeader(List<GenericTreeHeaderType> listHeader)
  {
    this.listHeader = listHeader;
  }

  /**
   * @param doubleClickEvent the doubleClickEvent to set
   */
  public void setDoubleClickEvent(IDoubleClick doubleClickEvent)
  {
    this.doubleClickEvent = doubleClickEvent;
  }

  /**
   * @param updateDataEvent the updateDataEvent to set
   */
  public void setUpdateDataEvent(IUpdateData updateDataEvent)
  {
    this.updateDataEvent = updateDataEvent;
  }

  /**
   * @param autoExpandAll the autoExpandAll to set
   */
  public void setAutoExpandAll(boolean autoExpandAll)
  {
    this.autoExpandAll = autoExpandAll;
  }

  /**
   * @param dropEvent the dropEvent to set
   */
  public void setDropEvent(IOnDrop dropEvent)
  {
    this.dropEvent = dropEvent;

    //logger.debug("[GenericTreeItemRenderer.java] setDropEvent()");
  }

  /**
   * @param contextMenuPopup the contextMenuPopup to set
   */
  public void setContextMenuPopup(Menupopup contextMenuPopup)
  {
    this.contextMenuPopup = contextMenuPopup;
  }

  /**
   * @param draggable the draggable to set
   */
  public void setDraggable(boolean draggable)
  {
    this.draggable = draggable;
  }
}
