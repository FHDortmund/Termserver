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
package de.fhdo.communication;

import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.db.classes.Collaborationuser;



/**
 *
 * @author Robert Mützner
 */
public class Mail
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  private static String getAppLink()
  {
    return DBSysParam.instance().getStringValue("weblink", null, null);
  }

  private static String getAppLinkCollaboration()
  {
    return DBSysParam.instance().getStringValue("weblink_collaboration", null, null);
  }

  private static String getTermBrowserLink()
  {
    return DBSysParam.instance().getStringValue("termbrowserlink", null, null);
  }

  public static String sendNewPasswordCollaboration(String Benutzername, String Passwort, String[] BenutzerEmail)
  {
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      String betreff = "", text = "";

      //String link = getAppLink() + "index.zul?user=" + Benutzername;


      betreff = "Kollaboration - Passwort";

      text =    "Ihr neues Passwort lautet " + Passwort + "\nBitte ändern Sie dieses nach der ersten Anmeldung über Benutzer->Bearbeiten oder durch einen Klick auf den Benutzer unten links im Fenster!\n"
              + "Ihr Benutzername ist '" + Benutzername + "'\n\n"
              /* + "Ihr Aktivierungscode '" + ActivationMD5 + "'\n\n" */
              + "Unter folgendem Link erreichen Sie die Kollaborationsplattform: "
              + getAppLinkCollaboration()+ "\n\n"
              + "Kopieren Sie den kompletten Link notfalls in Ihren Browser, falls ein Klick auf den Link nicht funktioniert.";

      //text += LeseFusszeile();

      Smtp.sendMail(BenutzerEmail, betreff, M_AUT.MAIL_START + text + M_AUT.getInstance().getMailFooter());
    }
    catch (Exception e)
    {

      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getStackTrace()[0].);

      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);
      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage());

      return "Mail konnte nicht versendet werden: " + e.getMessage();
    }
    return "";
  }

  

  public static String sendMail(String Benutzername, String Betreff, String Text, String[] BenutzerEmail)
  {
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      
      Smtp.sendMail(BenutzerEmail, Betreff, M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());
    }
    catch (Exception e)
    {

      //logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getStackTrace()[0].);

      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

      return "Mail konnte nicht versendet werden: " + e.getMessage();
    }
    return "";
  }

  public static Boolean sendMailAUT(String[] mailAddress, String Betreff, String Text)
  {
      
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("Neue Email an user senden...");

      Smtp.sendMail(mailAddress, Betreff, M_AUT.MAIL_START + Text + M_AUT.getInstance().getMailFooter());

    }
    catch (Exception e)
    {
      String fehler = "";
      for (int i = 0; i < e.getStackTrace().length; ++i)
      {
        fehler += e.getStackTrace()[i].toString() + "\n";
      }
      logger.error("[Mail.java] Fehler in 'sendMailNewUser()': " + e.getMessage() + ", " + fehler);

      return false;
    }

    return true;
  }
}
