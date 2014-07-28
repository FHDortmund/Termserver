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
package de.fhdo.list;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class GenericListItemRenderer implements ListitemRenderer<GenericListRowType>
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private List<GenericListHeaderType> listHeader;
  private IDoubleClick doubleClickEvent;
  private IUpdateData updateDataEvent;
  private Listcell currentListcell;
  private EventListener selectRowListener;

  public GenericListItemRenderer(List<GenericListHeaderType> _listHeader)
  {
    listHeader = _listHeader;

    selectRowListener = new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        try
        {
          Listcell lc = (Listcell) event.getTarget().getParent();
          Listitem li = (Listitem) lc.getParent();
          Listbox lb = (Listbox) li.getParent();
          lb.clearSelection();
          lb.setSelectedItem(li);

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

  public void render(Listitem item, GenericListRowType data, int index) throws Exception
  {

    for (int i = 0; i < data.getCells().length; ++i)
    {
      // TODO Sichtbarkeiten prüfen
      GenericListCellType cell = data.getCells()[i];

      addCell(i, item, cell, listHeader.get(i), data);
      //item.setValue(data.getData());


      //GenericListRowType
    }

    item.setValue(data);
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

  private Component addCheckbox(boolean Checked, String Label, String Tooltip,
          final GenericListCellType data,
          final GenericListHeaderType header, boolean editing, final GenericListRowType rowData)
  {
    //Listcell cell = new Listcell();

    Checkbox item = new Checkbox(Label);
    item.setChecked(Checked);
    item.setAttribute("data", data);
    item.setTooltiptext(Tooltip);

    if (header.isAllowInlineEditing() == false)
      item.setDisabled(true);

    //item.setAttribute("rowIndex", i);

    item.addEventListener(Events.ON_CHECK, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        //logger.debug("ON_CHANGE: " + Data.getClass().getCanonicalName());
        logger.debug("ON_CHANGE");
        Checkbox cb = (Checkbox) event.getTarget();
        //GenericListCellType cellType = (GenericListCellType) Data;
        data.setData(cb.isChecked());


        if (updateDataEvent != null)
        {
          // Benachrichtigung über geänderte Daten
          updateDataEvent.onCellUpdated(header.getIndex(), cb.isChecked(), rowData);
        }
      }
    });
    /*item.addEventListener(Events.ON_CHECK, new EventListener()
     {
     public void onEvent(Event event) throws Exception
     {
     logger.debug("onCheck");

     if (checkedEvent != null)
     {
     Checkbox cb = (Checkbox) event.getTarget();
     checkedEvent.onChecked(cb, cb.isChecked());

     task.setCompleted(cb.isChecked());
     if (cb.isChecked())
     cb.setTooltiptext("Abgeschlossen am " + sdfDT.format(new Date()));
     else
     cb.setTooltiptext("Klicken, um Aufgabe abzuschließen");

     // Anzahl-Label ändern
     if (updateCountEvent != null)
     updateCountEvent.updateCount();


     //updateEntry(task, window);
     // Farben ändern (ausgegraut oder nicht)
     //Listitem li = (Listitem) cb.getParent().getParent();
     //changeColor(li, cb.isChecked());
     }
     }
     });*/



    return item;


  }

  private Component addTextbox(String Text, String Tooltip,
          final Listcell cell, final Listitem dataRow, final GenericListCellType data,
          final GenericListHeaderType header, boolean editing, final GenericListRowType rowData)
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
          updateCellEditing(cell, dataRow, data, header, false, rowData);
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
    /*item.addEventListener(Events.ON_FOCUS, new EventListener()
     {
     // Wählt die ganze Zeile aus, wenn man auf ein Objekt klickt
     public void onEvent(Event event) throws Exception
     {
     try
     {
     if (event != null && event.getTarget() != null)
     {
     Listcell lc = (Listcell) event.getTarget().getParent();
     Listitem li = (Listitem) lc.getParent();
     Listbox lb = (Listbox) li.getParent();
     lb.setSelectedItem(li);

     // TODO
     //if (updateListener != null)
     //  updateListener.update("ButtonSichtbarkeit");
     }
     }
     catch (Exception ex)
     {
     logger.error("Fehler 1234: " + ex.getLocalizedMessage());
     }
     }
     });*/





    return item;
  }

  private Component addDatebox(Date Value, String Tooltip, final Object Data, boolean allowEditing,
          final Listcell cell, final Listitem dataRow, final GenericListCellType data,
          final GenericListHeaderType header, boolean editing, final GenericListRowType rowData)
  {
    final Datebox item = new Datebox(Value);
    item.setAttribute("data", Data);
    item.setTooltiptext(Tooltip);
    item.setWidth("99%");
    //item.setInplace(true);

    logger.debug("Data-Type: " + Data.getClass().getCanonicalName());

    /*item.setFocus(true);
     item.select();
     item.setSelectionRange(item.getText().length(), item.getText().length());*/

    if (allowEditing == false)
    {
      item.setDisabled(true);
    }

    item.addEventListener(Events.ON_BLUR, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON_BLUR");
        updateCellEditing(cell, dataRow, data, header, false, rowData);
      }
    });

    item.addEventListener(Events.ON_CHANGE, new EventListener()
    {
      public void onEvent(Event event) throws Exception
      {
        logger.debug("ON_CHANGE");
        Datebox db = (Datebox) event.getTarget();

        GenericListCellType cellType = (GenericListCellType) Data;
        cellType.setData(db.getValue());

        if (updateDataEvent != null)
        {
          // Benachrichtigung über geänderte Daten
          updateDataEvent.onCellUpdated(header.getIndex(), db.getValue(), rowData);
        }
      }
    });


    item.addEventListener(Events.ON_FOCUS, selectRowListener);





    return item;
  }

  private Component addCombobox(String Text, String Tooltip,
          final Listcell cell, final Listitem dataRow, final GenericListCellType data,
          final GenericListHeaderType header, boolean editing, final GenericListRowType rowData)
  {
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
        updateCellEditing(cell, dataRow, data, header, false, rowData);
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

  private boolean changeComponent(Listcell cell, Listitem dataRow, GenericListCellType data,
          GenericListHeaderType header, boolean editing, GenericListRowType rowData)
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

  private boolean addComponent(Listcell cell, Listitem dataRow, GenericListCellType data,
          GenericListHeaderType header, boolean editing, GenericListRowType rowData)
  {
    //if(header.isAllowInlineEditing() == false)
    if (editing == false && header.isComponent() == false)
      return false;

    cell.disableClientUpdate(true);
    cell.getChildren().clear();

    //logger.debug("addComponent from Type: " + data.getData().getClass().getCanonicalName());

    Component comp = null;
    if (data.getData() instanceof String)
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
    else if (data.getData() instanceof Boolean)
    {
      // Checkbox
      comp = addCheckbox((Boolean) data.getData(), "", "", data, header, editing, rowData);
    }
    else if (data.getData() instanceof Date)
    {
      comp = addDatebox((Date) data.getData(), "", data, header.isAllowInlineEditing(), cell, dataRow, data, header, editing, rowData);
    }
    else if (data.getData() instanceof Component)
    {
      comp = (Component) data.getData();
    }
    else
    {
      if(data.getData() != null)
        logger.warn("Component nicht gefunden, zeige Label. Comp: " + data.getData().getClass().getCanonicalName());
      else logger.warn("Component nicht gefunden, zeige Label. Comp: null");
      
      return false;
    }


    if (comp != null)
      cell.appendChild(comp);

    //if (Style.length() > 0)
    //  cell.setStyle(Style);
    dataRow.appendChild(cell);

    cell.disableClientUpdate(false);
    cell.invalidate();

    // Komponente aktiv schalten
    if (comp instanceof Textbox)
    {
      Textbox tb = (Textbox) comp;
      tb.setFocus(true);
      tb.select();
      tb.setSelectionRange(tb.getText().length(), tb.getText().length());
    }
    else if (comp instanceof Datebox)
    {
      Datebox db = (Datebox) comp;
      if (db != null)
      {
        db.setFocus(true);
        db.select();
        db.open();
      }
    }
    else if (comp instanceof Combobox)
    {
      Combobox cb = (Combobox) comp;
      if (cb != null)
      {
        cb.setFocus(true);
        //cb.select();
        cb.open();
      }

    }

    return true;
  }

  private void updateCellEditing(Listcell cell, Listitem dataRow, GenericListCellType data,
          GenericListHeaderType header, boolean editing, GenericListRowType rowData)
  {
    //logger.debug("updateCellEditing: " + task.getId());
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
        createComponent = changeComponent(cell, dataRow, data, header, editing, rowData);
      }

      if (createComponent)
      {
        // Component erzeugen
        boolean compErfolg = addComponent(cell, dataRow, data, header, editing, rowData);
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
        label = getString(data.getData(), header.getDatatype());
        //cell.setLabel(getString(data.getData()));
        //logger.debug("setLabel (0): " + getString(data.getData()));
      }

      Label l = new Label(label);
      cell.appendChild(l);

      cell.disableClientUpdate(false);
      cell.invalidate();
    }


    if (editing)
    {
      currentListcell = cell;
      //currentTask = task;
    }
    else
    {
      currentListcell = null;
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

  private void addCell(int i, final Listitem dataRow, GenericListCellType data, final GenericListHeaderType header, final GenericListRowType rowData)
  {
    Listcell cell;

    if (data.getData() != null && data.getData() instanceof Listcell)
    {
      cell = (Listcell) data.getData();
    }
    else
    {
      cell = new Listcell();
      cell.setValue(data.getData());

      updateCellEditing(cell, dataRow, data, header, false, rowData);
    }

    if (cell != null)
    {
      if (data.getStyle() != null)
        cell.setStyle(data.getStyle());

      dataRow.appendChild(cell);

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
              Listcell lc = (Listcell) t.getTarget();
              Listitem li = (Listitem) lc.getParent();
              Object listData = li.getValue();

              if (currentListcell != lc)
              {
                //if ((task.getCompleted() != null && task.getCompleted() == false) || task.getId() == 0)
                //  updateCellEditing(lc, task, true);
                GenericListCellType cellData = ((GenericListRowType) listData).getCells()[header.getIndex()];
                updateCellEditing(lc, dataRow, cellData, header, true, rowData);
              }
              else
                logger.debug("Gleiche Listcell");
            }
          }
          catch (Exception ex)
          {
            logger.error("Fehler 1234: " + ex.getLocalizedMessage());
          }

        }
      });
    }


    /*cell.addEventListener(Events.ON_CLICK, new EventListener<Event>()
     {
     public void onEvent(Event t) throws Exception
     {
     logger.debug("Events.ON_CLICK");
     //logger.debug("event.getTarget: " + t.getTarget().getClass().getCanonicalName());
     Listcell lc = (Listcell) t.getTarget();
     Listitem li = (Listitem) lc.getParent();
     Task task = li.getValue();
     //logger.debug("task: " + task.getName() + ", ID: " + task.getId());

     if (currentListcell != lc)
     {

     if ((task.getCompleted() != null && task.getCompleted() == false) || task.getId() == 0)
     updateCellEditing(lc, task, true);
     }
     else
     logger.debug("Gleiche Listcell");

     }
     });*/

  }

  private String getString(Object o, Object datatype)
  {
    if (o == null)
      return "";

    if (o instanceof java.util.Date)
    {
      SimpleDateFormat sdf;
      if (datatype != null && datatype.toString().equalsIgnoreCase("datetime"))
      {
        sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      }
      else sdf = new SimpleDateFormat("dd.MM.yyyy");
      
      return sdf.format(o);
    }

    return o.toString();
  }

  /**
   * @param listHeader the listHeader to set
   */
  public void setListHeader(List<GenericListHeaderType> listHeader)
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
}
