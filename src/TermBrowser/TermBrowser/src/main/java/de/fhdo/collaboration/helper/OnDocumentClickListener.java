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
package de.fhdo.collaboration.helper;


import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.File;
import de.fhdo.collaboration.db.classes.Link;
import java.io.IOException;
import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Image;
//import de.fhdo.gui.viewer.ImageViewer;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**

 @author Robert Mützner
 */
public class OnDocumentClickListener implements EventListener
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  Object referClass;

  public OnDocumentClickListener(Object ReferClass)
  {
    referClass = ReferClass;
  }

  public void onEvent(Event event) throws Exception
  {
    if (logger.isDebugEnabled())
      logger.debug("onEvent(): ");

    logger.debug("name: " + event.getName());

    if (event.getName().equals("onRightClick"))
    {
      //image.setContext("docContext");
      if (event.getTarget() != null)
      {
        if (event.getTarget() instanceof Image)
        {
          Image image = (Image) event.getTarget();
          //image.setContext("docContext");

          // TODO
          if (referClass == null)
            logger.warn("TODO - Event abarbeiten, Class: " + referClass.getClass().getCanonicalName());
          /* if (referClass != null && referClass instanceof Standards)
           {
           ((Standards) referClass).setClickedDocument(image.getAttribute("document_id").toString());
           } */

          /* if (referClass != null && referClass instanceof Standards)
           {
           ((Standards) referClass).setClickedDocument(image.getAttribute("document_id").toString());
           } */



        }
      }
    }
    else
    {
      if (event.getTarget() != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("Classname: " + event.getTarget().getClass().getName());

        if (event.getTarget() instanceof Image)
        {
          Image image = (Image) event.getTarget();

          String documentID = image.getAttribute("document_id").toString();
          Object textObj = image.getAttribute("text");
          Object linkObj = image.getAttribute("link");

          if (textObj != null)
          {
            openNote(textObj.toString());
          }
          else if (linkObj != null)
          {
            openLink(linkObj.toString());
          }
          else
          {
            if (logger.isDebugEnabled())
              logger.debug("Image identifiziert mit id: " + documentID);

            openDocument(documentID);
          }
        }


      }
    }
  }

  private void openNote(String Text) throws InterruptedException
  {
    //Messagebox.show(Text, "Notiz", Messagebox.OK, Messagebox.INFORMATION);
    Map map = new HashMap();
    map.put("note", Text);
    logger.debug("erstelle Fenster...");
    Window win = (Window) Executions.createComponents(
      "/gui/main/modules/attachmentNote.zul", null, map);

    logger.debug("öffne Fenster...");
    win.doModal();
  }

  private void openLink(String Text) throws InterruptedException
  {
    //Messagebox.show(Text, "Link", Messagebox.OK, Messagebox.INFORMATION);
    String link = Text;

    if (link.startsWith("http://") == false)
      link = "http://" + link;

    Executions.getCurrent().sendRedirect(link, "_blank");
  }

  private void openDocument(String Attachment) throws InterruptedException, IOException
  {
    if (logger.isDebugEnabled())
      logger.debug("openDocument() mit id: " + Attachment);

    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      //org.hibernate.Transaction tx = hb_session.beginTransaction();

      File file = (File) hb_session.get(File.class, Long.parseLong(Attachment));

      if (file != null)
      {
        Link att = (Link) hb_session.get(Link.class, Long.parseLong(Attachment));

        Filedownload.save(file.getData(),
          att.getMimeType(),
          att.getContent());
      }
      else
      {
        // Fehlermeldung ausgeben
        Messagebox.show("Anhang konnte nicht in der Datenbank gefunden werden!", "Download", Messagebox.OK, Messagebox.INFORMATION);
      }


      //Collection result = new LinkedHashSet(q.list());

      //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
        //hb_session.getTransaction().rollback();
    }finally{
        hb_session.close();
    }

  }
}
