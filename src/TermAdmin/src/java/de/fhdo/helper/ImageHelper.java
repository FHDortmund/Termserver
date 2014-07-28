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

/**
 *
 * @author Robert Mützner
 */
public class ImageHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  public static String getImageSrcFromMimeType(String MimeType)
  {
    String s_icon = "";

    if(MimeType == null || MimeType.length() == 0)
      return "/rsc/img/filetypes/white.png";


    if (MimeType.equals("licence"))
    {
      s_icon = "/rsc/img/filetypes/licence.png";
    }
    else if(MimeType.contains("image"))
    {
      s_icon = "/rsc/img/filetypes/picture.png";
    }
    else if(MimeType.contains("video"))
    {
      s_icon = "/rsc/img/filetypes/movie.png";
    }
    else if (MimeType.equals("application/msword") ||
             MimeType.equals("application/vnd.ms-word") ||
             MimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
    {
      s_icon = "/rsc/img/filetypes/word.png";
    }
    else if (MimeType.equals("application/msexcel") ||
             MimeType.equals("application/vnd.ms-excel") ||
             MimeType.equals("application/excel") ||
             MimeType.equals("application/x-ms-excel") ||
             MimeType.equals("application/x-msexcel") ||
             MimeType.equals("application/xls") ||
             MimeType.equals("application/xlsx") ||
             MimeType.equals("application/csv") ||
             MimeType.equals("text/csv") ||
             MimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    {
      s_icon = "/rsc/img/filetypes/excel.png";
    }
    else if (MimeType.equals("application/pdf"))
    {
      s_icon = "/rsc/img/filetypes/acrobat.png";
    }
    else if (MimeType.equals("application/vnd.ms-powerpoint") ||
             MimeType.equals("application/ms-powerpoint") ||
             MimeType.equals("application/mspowerpoint") ||
             MimeType.equals("application/x-powerpoint"))
    {
      s_icon = "/rsc/img/filetypes/powerpoint.png";
    }

    else if (MimeType.equals("application/x-zip-compressed"))
    {
      s_icon = "/rsc/img/filetypes/zip.png";
    }
    else if (MimeType.equals("text/plain"))
    {
      s_icon = "/rsc/img/filetypes/text.png";
    }
    else
    {
      s_icon = "/rsc/img/filetypes/white.png";
    }
    
    logger.debug("Icon from Mimetype '" + MimeType + "': " + s_icon);

    return s_icon;
  }

  public static boolean isImage(String MimeType)
  {
    if (MimeType.contains("image") && MimeType.equals("image/tiff") == false)
      return true;

    return false;
  }
}
