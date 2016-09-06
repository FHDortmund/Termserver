package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.helper.DomainHelper;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.Definitions;
import de.fhdo.terminologie.db.SystemSettings;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class SystemLogs extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;

  public SystemLogs()
  {
  }

  public void afterCompose()
  {
    ((Textbox) getFellow("tbPath")).setText(SystemSettings.getInstance().getSystemLogsPath());
    initList();
  }

  public void savePath()
  {
    String newPath = ((Textbox) getFellow("tbPath")).getText();

    if (new File(newPath).exists())
    {
      SystemSettings.getInstance().setSystemLogsPath(newPath);
    }
    else
    {
      Messagebox.show("Der angegebene Pfad konnte nicht gefunden werden oder Sie haben keine Berechtigungen, diesen zu lesen. Pfad wurde nicht gespeichert.");
    }

  }

  public void reload()
  {
    initList();
  }

  private void initList()
  {
    String[] filter = DomainHelper.getInstance().getDomainStringList(Definitions.DOMAINID_VALIDITYDOMAIN);

    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("name"), 400, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("changeDate"), 130, "", true, filter, true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("size"), 130, "", true, filter, true, true, false, false));

    // Daten laden
    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      File folder = new File(((Textbox) getFellow("tbPath")).getText());
      if (folder.isDirectory())
      {
        for (File file : folder.listFiles())
        {
          GenericListRowType row = createRowFromFile(file);
          dataList.add(row);
        }
      }
    }
    catch (Exception e)
    {
      LoggingOutput.outputException(e, this);
    }
    finally
    {

    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    genericList.removeCustomButtons();
    Button buttonShow = new Button("Datei anzeigen");
    buttonShow.setImage("/rsc/img/symbols/open_16x16.png");
    buttonShow.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {

        GenericListRowType row = genericList.getSelectionRowType();
        if (row != null)
        {
          Textbox tb = (Textbox) getFellow("tbView");

          File file = (File) row.getData();
          tb.setText(readFile(file.getAbsolutePath()));
          
          //Clients.scrollIntoView(tb);
          //tb.set
          //Files.re
        }
      }
    });
    genericList.addCustomButton(buttonShow);

    Button buttonDownload = new Button("Datei herunterladen");
    buttonDownload.setImage("/rsc/img/symbols/down_16x16.png");
    buttonDownload.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        GenericListRowType row = genericList.getSelectionRowType();
        if (row != null)
        {
          File file = (File) row.getData();

          Filedownload.save(file, null);
        }
      }
    });
    genericList.addCustomButton(buttonDownload);

    Button buttonDelete = new Button("Datei(en) löschen");
    buttonDelete.setImage("/rsc/img/list/delete.png");
    buttonDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        onDeleteFiles();
      }
    });
    genericList.addCustomButton(buttonDelete);

    //genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setShowCount(true);
    genericList.getListbox().setMultiple(true);
    genericList.getListbox().setCheckmark(true);
    genericList.setCheckable(true);

    genericList.setListHeader(header);
    genericList.setDataList(dataList);

    /*Div div = new Div();
     Button button = new Button("Test-Mail: ");
     div.appendChild(button);
     Textbox tb = new Textbox();
     div.appendChild(tb);
     genericList.addCustomButton(div);*/
  }

  private String readFile(String pathname) throws IOException
  {

    File file = new File(pathname);
    StringBuilder fileContents = new StringBuilder((int) file.length());
    Scanner scanner = new Scanner(file);
    String lineSeparator = System.getProperty("line.separator");

    try
    {
      while (scanner.hasNextLine())
      {
        fileContents.append(scanner.nextLine() + lineSeparator);
      }
      return fileContents.toString();
    }
    finally
    {
      scanner.close();
    }
  }

  private GenericListRowType createRowFromFile(File file)
  {
    GenericListRowType row = new GenericListRowType();

    java.util.Date lastModified = new java.util.Date(file.lastModified());
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    long fileSize = file.length();

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(file.getName(), false, "");
    cells[1] = new GenericListCellType(sdf.format(lastModified), false, "");
    Listcell lc = new Listcell();
    Label label = new Label(humanReadableByteCount(fileSize, false));
    label.setStyle("float:right;");
    lc.appendChild(label);
    cells[2] = new GenericListCellType(lc, false, "");

    row.setData(file);
    row.setCells(cells);

    return row;
  }

  public static String humanReadableByteCount(long bytes, boolean si)
  {
    int unit = si ? 1000 : 1024;
    if (bytes < unit)
      return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public void onNewClicked(String string)
  {
  }

  public void onEditClicked(String string, Object o)
  {
  }

  public void onDeleteFiles()
  {
    if (Messagebox.show("Möchten Sie die markierten Dateien wirklich löschen?", "Löschen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
            == Messagebox.YES)
    {
      logger.debug("onDeleted");
      logger.debug("selectionCount: " + genericList.getListbox().getSelectedCount());

      for (Listitem li : genericList.getListbox().getSelectedItems())
      {
        logger.debug("Listitem: " + li.getLabel());

        GenericListRowType row = li.getValue();
        File file = (File) row.getData();

        logger.debug("Delete file: " + file.getAbsolutePath());
        file.delete();
      }

      initList();
    }

  }

  public void onSelected(String string, Object o)
  {
  }

}
