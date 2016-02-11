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
package de.fhdo.gui.admin.modules.terminology;

import de.fhdo.communication.M_AUT;
import de.fhdo.communication.Smtp;
import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.list.GenericList;
import de.fhdo.list.GenericListCellType;
import de.fhdo.list.GenericListHeaderType;
import de.fhdo.list.GenericListRowType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Datenbank extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  GenericList genericList;

  public Datenbank()
  {
  }

  /**
   * Erstellt einen Datenbank-Dump
   */
  public void createDump()
  {
    try
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
      String filename = System.getProperty("catalina.base") + "/dumps/"
              + sdf.format(new Date()) + ((Textbox) getFellow("tbDateiname")).getText() + ".sql";

      String dbName = "web5db2";
      String dbUser = "web5u2";
      String dbPass = ""; // TODO Passwort aus hibernate-config auslesen

      String msg = "Ein Datenbankdump wird in die Datei '" + filename + "' geschrieben. Der Vorgang kann ein paar Minuten dauern, bitte schließen Sie die Webseite in dieser Zeit nicht. Möchten Sie die Aktion fortsetzen?";
      if (Messagebox.show(msg, "Datenbankdump erstellen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
              == Messagebox.YES)
      {
        backupDB(dbName, dbUser, dbPass, filename);

        initList();
      }
      else
        logger.error("Nein geklickt. (kein Dump)");
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Erstellen des Dumps: " + ex.getLocalizedMessage());
      Messagebox.show("Fehler beim Erstellen des Dumps: " + ex.getLocalizedMessage());
    }
  }

  private String getPath()
  {
    return System.getProperty("catalina.base") + "/dumps/";
  }

  private boolean backupDB(String dbName, String dbUserName, String dbPassword, String path)
  {
    logger.debug("backupDB()");
    logger.debug("dbName: " + dbName);
    logger.debug("dbUser: " + dbUserName);
    logger.debug("Pfad: " + path);

    String executeCmd = getPath() + "mysqldump -u " + dbUserName + " -p" + dbPassword + " --add-drop-database -B " + dbName + " -r \"" + path + "\"";

    logger.debug("Kommando: " + executeCmd);
    // /usr/local/tomcat/dumps/mysqldump -u web5u2 -pderTermBoSS# --add-drop-database -B web5db1 -r "/usr/local/tomcat/dumps/201212041241Termserver_Server.sql"

    execShellCmd(executeCmd);

    return false;
  }

  public static void execShellCmd(String cmd)
  {
    try
    {
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec(new String[]
              {
                "/bin/bash", "-c", cmd
              });
      int exitValue = process.waitFor();
      logger.debug("exit value: " + exitValue);
      BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      while ((line = buf.readLine()) != null)
      {
        logger.debug("exec response: " + line);
      }
    }
    catch (Exception e)
    {
      logger.error(e.getLocalizedMessage());
    }
  }

  public void afterCompose()
  {
    String dateiname = "";
    //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    //dateiname = sdf.format(new Date()) + "Termserver_Server";
    dateiname = "Termserver_Server";
    ((Textbox) getFellow("tbDateiname")).setText(dateiname);

    initList();
  }

  private GenericListRowType createRowFromFile(File file)
  {
    GenericListRowType row = new GenericListRowType();

    Date date = null;
    String name = file.getName();
    if (name != null && name.length() > 12)
    {
      try
      {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        date = sdf.parse(name.substring(0, 12));
      }
      catch (Exception e)
      {
      }
    }

    try
    {
      // file.getName()
      if (name != null && name.length() > 12)
      {
        name = name.substring(12);
        name = name.replace(".sql", "");
      }
    }
    catch (Exception e)
    {
    }
    //yyyyMMddHHmm

    String groesse = "";

    Double mb = file.length() / 1024.0 / 1024.0;
    groesse = new DecimalFormat("0.0").format(mb) + " MB";

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    String datum = "";
    if (date != null)
      datum = sdf.format(date);

    //if (datum.length() == 0)
    //  datum = sdf.format(new Date(file.lastModified()));

    GenericListCellType[] cells = new GenericListCellType[3];
    cells[0] = new GenericListCellType(name, false, "");
    cells[1] = new GenericListCellType(datum, false, "");
    cells[2] = new GenericListCellType(groesse, false, "");

    row.setData(file);
    row.setCells(cells);

    return row;
  }

  private void initList()
  {
    logger.debug("initList()");
    // Header
    List<GenericListHeaderType> header = new LinkedList<GenericListHeaderType>();
    header.add(new GenericListHeaderType(Labels.getLabel("name"), 400, "", true, "String", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("date"), 120, "", true, "Date", true, true, false, false));
    header.add(new GenericListHeaderType(Labels.getLabel("size"), 80, "", true, "String", true, true, false, false));


    List<GenericListRowType> dataList = new LinkedList<GenericListRowType>();
    try
    {
      File directory = new File(getPath());

      File[] dateien = directory.listFiles(new FilenameFilter()
      {
        public boolean accept(File dir, String name)
        {
          if (name.contains(".sql"))
            return true;
          else
            return false;
        }
      });

      List<File> dateienList = Arrays.asList(dateien);


      Collections.sort(dateienList, new Comparator<File>()
      {
        public int compare(File o1, File o2)
        {
          try
          {
            Date date1 = new Date(o1.lastModified());
            Date date2 = new Date(o2.lastModified());

            if (date1.before(date2))
              return 1;
            else if (date1.after(date2))
              return -1;
          }
          catch (Exception e)
          {
          }

          return 0;
        }
      });




      for (int i = 0; i < dateienList.size(); ++i)
      {
        File file = dateienList.get(i);
        GenericListRowType row = createRowFromFile(file);

        dataList.add(row);
      }
    }
    catch (Exception e)
    {
      logger.error("[" + this.getClass().getCanonicalName() + "] Fehler bei initList(): " + e.getMessage());
    }
    finally
    {
      //HibernateUtil.getSessionFactory().close();
    }

    // Liste initialisieren
    Include inc = (Include) getFellow("incList");
    Window winGenericList = (Window) inc.getFellow("winGenericList");
    genericList = (GenericList) winGenericList;

    //genericList.setUserDefinedId("1");
    //genericList.setListActions(this);
    genericList.setButton_new(false);
    genericList.setButton_edit(false);
    genericList.setButton_delete(false);
    genericList.setListHeader(header);
    genericList.setDataList(dataList);

    genericList.removeCustomButtons();

    Button button = new Button("Speichern unter...");
    button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        // Speichert den Eintrag
        Object sel = genericList.getSelection();
        if (sel != null)
        {
          GenericListRowType row = (GenericListRowType) sel;
          File file = (File) row.getData();

          saveFile(file);
        }
      }
    });
    button.setImage("/rsc/img/symbols/saveas_16x16.png");
    button.setStyle("margin-right:8px;");
    genericList.addCustomButton(button);

    Button button3 = new Button("Löschen");
    button3.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        // Löscht den Eintrag
        Object sel = genericList.getSelection();
        if (sel != null)
        {
          GenericListRowType row = (GenericListRowType) sel;
          File file = (File) row.getData();

          String msg = "Möchten Sie die Datei '" + file.getName() + "' wirklich löschen? Dieser Vorgang kann nicht rückgängig gemacht werden!";
          if (Messagebox.show(msg, "Datenbankdump erstellen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
                  == Messagebox.YES)
          {
            file.delete();
            initList();
          }
        }
      }
    });
    button3.setImage("/rsc/img/symbols/delete_16x16.png");
    button3.setStyle("margin-right:8px;");
    genericList.addCustomButton(button3);

    Button button2 = new Button("Import");
    button2.addEventListener(Events.ON_CLICK, new EventListener<Event>()
    {
      public void onEvent(Event t) throws Exception
      {
        // Importiert den Eintrag
        Object sel = genericList.getSelection();
        if (sel != null)
        {
          GenericListRowType row = (GenericListRowType) sel;
          File file = (File) row.getData();

          importFile(file);
        }
      }
    });
    button2.setImage("/rsc/img/symbols/import_16x16.png");
    genericList.addCustomButton(button2);




    //genericList.setDataList(null);
    //genericList.set


  }

  private void importFile(File file)
  {
    String msg = "Möchten Sie die Datei '" + file.getName() + "' wirklich importieren? Dabei werden alle bisherigen Daten gelöscht! Dieser Vorgang kann einige Minuten dauern, bitte schließen Sie dieses Fenster nicht!";
    if (Messagebox.show(msg, "Datenbankdump erstellen", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION)
            == Messagebox.YES)
    {
      String dbName = "web5db1";
      String dbUser = "web5u2";

      //insertDB(dbName, dbUser, file);
      restoreDB(dbName, dbUser, "derTermBoSS#", file.getAbsolutePath());

      initList();
      Messagebox.show("Datenbank importiert.");
    }
  }

  public boolean restoreDB(String dbName, String dbUserName, String dbPassword, String source)
  {

    /*String[] executeCmd = new String[]
    {
      getPath() + "mysql", "--user=" + dbUserName, "--password=" + dbPassword, dbName, "-e", "source " + source
    };*/
    
    String executeCmd = getPath() + "mysql --user=" + dbUserName + "--password=" + dbPassword + " " + dbName + "-e source " + source;
    
    execShellCmd(executeCmd);

    /*Process runtimeProcess;
    try
    {

      runtimeProcess = Runtime.getRuntime().exec(executeCmd);
      int processComplete = runtimeProcess.waitFor();

      if (processComplete == 0)
      {
        System.out.println("Backup restored successfully");
        return true;
      }
      else
      {
        System.out.println("Could not restore the backup");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }*/

    return false;
  }

  private boolean insertDB(String dbName, String dbUserName, File file)
  {
    logger.debug("backupDB()");
    logger.debug("Filename: " + file.getAbsolutePath());
    logger.debug("dbName: " + dbName);
    logger.debug("dbUser: " + dbUserName);

    String dbPassword = "derTermBoSS#";

    //dbUserName = "root";
    //dbPassword = "test";

    // /pfad/zu/mysql -u $DBUSER -p$DBPASSWD --database=$DATABASE < $FILENAME
    // mysql -u web5u2 -pderTermBoSS# --database=web5db1 < "D:\Programmierung\Apache Tomcat 7.0.14\dumps\201211301129_Termserver.sql"
    // D:\Programmierung\Apache Tomcat 7.0.14/dumps/mysql -u web5u2 -pderTermBoSS# --database=web5db1 < "D:\Programmierung\Apache Tomcat 7.0.14\dumps\201211301129_Termserver.sql"


    //String executeCmd = "\"" + getPath() + "mysql\" -u " + dbUserName + " --default-character-set=utf8 -p" + dbPassword + " --database=" + dbName + " < \"" + file.getAbsolutePath() + "\"";
    String executeCmd = getPath() + "mysql -u " + dbUserName + " --default-character-set=utf8 -p" + dbPassword + " --database=" + dbName + " < \"" + file.getAbsolutePath() + "\"";
    //String executeCmd = getPath() + "mysql -u " + dbUserName + " -p" + dbPassword + " --database=" + dbName + " < test.txt";
    //String executeCmd = getPath() + "mysql";

    //String executeCmd = getPath() + "mysql -u " + dbUserName + " --default-character-set=utf8 -p" + dbPassword + " --database=" + dbName + " < \"" + file.getAbsolutePath() + "\"";

    logger.debug("Kommando: " + executeCmd);

    Process runtimeProcess;

    try
    {
      //OutputStream stdin = runtimeProcess.getOutputStream();

      runtimeProcess = Runtime.getRuntime().exec(executeCmd);

      int processComplete = runtimeProcess.waitFor();

      if (processComplete == 0)
      {
        //runtimeProcess.getOutputStream()
        System.out.println("Backup imported successfully");
        return true;
      }
      else
      {
        System.out.println("Could not import the backup");
      }

      //InputStream is = runtimeProcess.getInputStream();
      //is.
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      logger.error(ex.getLocalizedMessage());
    }
    /*
     try
     {
     //OutputStream stdin = runtimeProcess.getOutputStream();

     runtimeProcess = Runtime.getRuntime().exec(executeCmd);

     final BufferedReader stdOut = new BufferedReader(new InputStreamReader(runtimeProcess.getInputStream()));
     final BufferedReader stdErr = new BufferedReader(new InputStreamReader(runtimeProcess.getErrorStream()));
     //final BufferedWriter stdIn = new BufferedWriter(new OutputStreamWriter(runtimeProcess.getOutputStream()));


     new Thread()
     {
     @Override
     public void run()
     {
     String line;
     try
     {
     while ((line = stdOut.readLine()) != null)
     {
     //out.write(line + newline);
     logger.debug(line + "\n");
     }
     }
     catch (Exception e)
     {
     throw new Error(e);
     }

     }
     }.start(); // Starts now

     // Thread that reads std err and feeds the writer given in input
     new Thread()
     {
     @Override
     public void run()
     {
     String line;
     try
     {
     while ((line = stdErr.readLine()) != null)
     {
     //err.write(line + newline);
     logger.error(line + "\n");
     }
     }
     catch (Exception e)
     {
     throw new Error(e);
     }

     }
     }.start(); // Starts now

     // Thread that reads the std in given in input and that feeds the input of the process
     


     // Wait until the end of the process
     try
     {
     runtimeProcess.waitFor();
     }
     catch (Exception e)
     {
     throw new Error(e);
     }


     //InputStream is = runtimeProcess.getInputStream();
     //is.
     }
     catch (Exception ex)
     {
     ex.printStackTrace();
     logger.error(ex.getLocalizedMessage());
     }*/

    return false;
  }

  private void saveFile(File file) throws InterruptedException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("openDocument() mit id: " + file.getName());

    try
    {
      Filedownload.save(file, "text/plain");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
