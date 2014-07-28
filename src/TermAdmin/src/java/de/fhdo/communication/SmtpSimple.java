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
import java.net.*;
import java.io.*;

public class SmtpSimple
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private DataOutputStream os = null;
  private BufferedReader is = null;
  private String sRt = "";

  public synchronized final String sendEmail(
          
          String sToAdr, String sToRealName,
          String sSubject, String sText)
          throws IOException, Exception
  {
    Socket so = null;
    try
    {
      String mailHost = DBSysParam.instance().getStringValue("mail_host", null, null);
      String mailPort = DBSysParam.instance().getStringValue("mail_port", null, null);
      
      String sFromAdr = DBSysParam.instance().getStringValue("mail_sender", null, null);
      String sFromRealName = DBSysParam.instance().getStringValue("mail_name", null, null);
      /*String mailAuth = DBSysParam.instance().getStringValue("mail_auth", null, null);
      String mailSender = DBSysParam.instance().getStringValue("mail_sender", null, null);
      String password = DBSysParam.instance().getStringValue("mail_password", null, null);
      String mailSSL = DBSysParam.instance().getStringValue("mail_ssl_enable", null, null);*/
      //logger.debug("Text: " + sText);
      
      if(mailPort == null || mailPort.length() == 0)
        mailPort = "25";
      int port = Integer.parseInt(mailPort);

      sRt = "";
      if (null == mailHost || 0 >= mailHost.length()
              || null == sFromAdr || 0 >= sFromAdr.length()
              || null == sToAdr || 0 >= sToAdr.length()
              || ((null == sSubject || 0 >= sSubject.length()) && (null == sText || 0 >= sText.length())))
        throw new Exception("Invalid Parameters for SmtpSimple.sendEmail().");
      if (null == sFromRealName || 0 >= sFromRealName.length())
        sFromRealName = sFromAdr;
      if (null == sToRealName || 0 >= sToRealName.length())
        sToRealName = sToAdr;
      so = new Socket(mailHost, port);
      os = new DataOutputStream(so.getOutputStream());
      is = new BufferedReader(
              new InputStreamReader(so.getInputStream()));
      so.setSoTimeout(10000);
      writeRead(true, "220", null);
      writeRead(true, "250", "HELO " + mailHost + "\n");
      writeRead(true, "250", "RSET\n");
      writeRead(true, "250", "MAIL FROM:<" + sFromAdr + ">\n");
      writeRead(true, "250", "RCPT TO:<" + sToAdr + ">\n");
      writeRead(true, "354", "DATA\n");
      writeRead(false, null, "To: " + sToRealName + " <" + sToAdr + ">\n");
      writeRead(false, null, "From: " + sFromRealName + " <" + sFromAdr + ">\n");
      writeRead(false, null, "Subject: " + sSubject + "\n");
      writeRead(false, null, "Mime-Version: 1.0\n");
      //writeRead(false, null, "Content-Type: text/plain; charset=\"iso-8859-1\"\n");
      //writeRead(false, null, "Content-Type: text/plain; charset=\"utf-8\"\n");
      //writeRead(false, null, "Content-Transfer-Encoding: quoted-printable\n\n");
      writeRead(false, null, sText + "\n");
      writeRead(true, "250", ".\n");
      writeRead(true, "221", "QUIT\n");
      return sRt;
    }
    finally
    {
      if (is != null)
        try
        {
          is.close();
        }
        catch (Exception ex)
        {
        }
      if (os != null)
        try
        {
          os.close();
        }
        catch (Exception ex)
        {
        }
      if (so != null)
        try
        {
          so.close();
        }
        catch (Exception ex)
        {
        }
      is = null;
      os = null;
    }
  }

  private void writeRead(boolean bReadAnswer,
          String sAnswerMustStartWith,
          String sWrite)
          throws IOException, Exception
  {
    if (null != sWrite && 0 < sWrite.length())
    {
      sRt += sWrite;
      os.writeBytes(sWrite);
    }
    if (bReadAnswer)
    {
      String sRd = is.readLine() + "\n";
      sRt += sRd;
      if (null != sAnswerMustStartWith && 0 < sAnswerMustStartWith.length() && !sRd.startsWith(sAnswerMustStartWith))
        throw new Exception(sRt);
    }
  }
}
