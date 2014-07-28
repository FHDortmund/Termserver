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

import de.fhdo.collaboration.db.Definitions;
import de.fhdo.collaboration.db.DomainHelper;
import de.fhdo.collaboration.db.HibernateUtil;
import de.fhdo.collaboration.db.classes.DomainValue;
import de.fhdo.collaboration.db.classes.File;
import de.fhdo.collaboration.db.classes.Link;
import de.fhdo.logging.LoggingOutput;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.hibernate.Session;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Messagebox;

/**

 @author Robert Mützner
 */
public class AttachmentHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static void applyListcellIcon(Listcell lc, Link attachment, EventListener onDocumentClicked, boolean rightClick)
  {
    try
    {
      DomainValue dv = DomainHelper.getInstance().getDomainValue(Definitions.DOMAINID_ATTACHMENT_TECHNICAL_TYPES, "" + attachment.getLinkType());

      if (dv != null)
      {
        //if(d.getTechnicalTypeCd().equals(DomainHelper.getInstance().getDomainValue(Definitions.TECHNICALTYPE_DOCUMENT).getDomainCode()))
        if (dv.getCode().equals("1"))
        {
          String s_icon = ImageHelper.getImageSrcFromMimeType(attachment.getMimeType());

          if (s_icon.length() > 0)
          {
            Image image = new Image(s_icon);
            image.setId("image_" + attachment.getId());

            if (attachment.getMimeType().contains("image"))
            {
              image.setTooltip("imgPopup");

              if (attachment.getDescription()!= null && attachment.getDescription().length() > 0)
                image.setAttribute("tooltip", attachment.getDescription());
              else if (attachment.getContent() != null && attachment.getContent().length() > 0)
                image.setAttribute("tooltip", attachment.getContent());

              image.setAttribute("attachment", attachment);
            }
            else
            {
              if (attachment.getDescription() != null && attachment.getDescription().length() > 0)
                image.setTooltiptext(attachment.getDescription());
              else if (attachment.getContent()!= null && attachment.getContent().length() > 0)
                image.setTooltiptext(attachment.getContent());
            }



            image.setAttribute("document_id", attachment.getId());
            image.setAttribute("mime_type", attachment.getMimeType());
            //image.setAttribute("tooltip", image)



            image.setContext("docContext");

            if (attachment.getMimeType().equals("licence") == false)
            {
              if (rightClick)
                image.addEventListener(Events.ON_RIGHT_CLICK, onDocumentClicked);

              image.addEventListener(Events.ON_CLICK, onDocumentClicked);
            }

            lc.appendChild(image);
          }
        }
        else if (dv.getCode().equals("2"))  // LINK
        {
          //logger.debug("Link vorhanden");
          Image image = new Image("/rsc/img/filetypes/link.png");
          image.setId("image_" + attachment.getId());
          image.setTooltiptext("Link: " + attachment.getDescription());
          image.setAttribute("document_id", attachment.getId());
          image.setAttribute("link", attachment.getDescription());

          image.setContext("docContext");
          if (rightClick)
            image.addEventListener(Events.ON_RIGHT_CLICK, onDocumentClicked);
          image.addEventListener(Events.ON_CLICK, onDocumentClicked);

          lc.appendChild(image);
        }
        else if (dv.getCode().equals("3"))
        {
          //logger.debug("Note vorhanden");

          Image image = new Image("/rsc/img/filetypes/note.png");
          image.setId("image_" + attachment.getId());

          image.setTooltiptext(HTMLHelper.removeTags(attachment.getDescription()));
          image.setAttribute("document_id", attachment.getId());
          image.setAttribute("text", attachment.getDescription());
          //image.setAttribute("mime_type", d.getMimeTypeCd());

          image.setContext("docContext");
          if (rightClick)
            image.addEventListener(Events.ON_RIGHT_CLICK, onDocumentClicked);

          image.addEventListener(Events.ON_CLICK, onDocumentClicked);

          lc.appendChild(image);
        }
      }
    }
    catch (Exception e)
    {
      logger.error("Fehler bei applyListcellIcon(): " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }

  public static BufferedImage resizeImage(BufferedImage image, int maxWidth, int maxHeight) throws IOException {
		Dimension largestDimension = new Dimension(maxWidth, maxHeight);

		// Original size
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		float aspectRation = (float) imageWidth / imageHeight;

		if (imageWidth > maxWidth || imageHeight > maxHeight) {
			if ((float) largestDimension.width / largestDimension.height > aspectRation) {
				largestDimension.width = (int) Math.ceil(largestDimension.height * aspectRation);
			} else {
				largestDimension.height = (int) Math.ceil(largestDimension.width / aspectRation);
			}

			imageWidth = largestDimension.width;
			imageHeight = largestDimension.height;
		}

		return getScaledImage(image, imageWidth, imageHeight);
	}

  public static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException
  {
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    double scaleX = (double) width / imageWidth;
    double scaleY = (double) height / imageHeight;
    AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
    AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

    return bilinearScaleOp.filter(
      image,
      new BufferedImage(width, height, image.getType()));
  }

  public static void GetImage(long attachmentId, int maxWidth, int maxHeight, org.zkoss.zul.Image image)
  {
    //de.fhdo.db.hibernate.File file = null;
    //org.zkoss.image.Image retImage = null;
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      
      File file = (File) hb_session.get(File.class, attachmentId);

      if (file != null)
      {
        //org.zkoss.image.Image byteImg = new AImage("Temp", file.getData());
        BufferedImage buffImage = ImageIO.read(new ByteArrayInputStream(file.getData()));
        buffImage = resizeImage(buffImage, maxWidth, maxHeight);
        image.setContent(buffImage);
        
        //retImage = new AImage("Temp", image);
        //retImage.
        //retImage = new AImage
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
        LoggingOutput.outputException(e, AttachmentHelper.class);
    }finally{
        hb_session.close();
    }
  }
  
  public static File GetFile(long attachmentId)
  {
    File file = null;
    Session hb_session = HibernateUtil.getSessionFactory().openSession();
    //hb_session.getTransaction().begin();
    try
    {
      //org.hibernate.Transaction tx = hb_session.beginTransaction();

      file = (File) hb_session.get(File.class, attachmentId);

      if (file != null)
      {
        //if(att == null)
        //  att = (Attachment) hb_session.get(Attachment.class, attachmentId);

        /* Filedownload.save(file.getData(),
         att.getMimeTypeCd(),
         att.getFilename()); */
      }
      else
      {
        // Fehlermeldung ausgeben
        Messagebox.show("Anhang konnte nicht in der Datenbank gefunden werden!", "Download", Messagebox.OK, Messagebox.INFORMATION);
      }

       //hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      //hb_session.getTransaction().rollback();
        e.printStackTrace();
      //logger.
    }finally{
        hb_session.close();
    }
    return file;
  }
}
