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

import de.fhdo.logging.LoggingOutput;
import de.fhdo.terminologie.db.DBSysParam;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

/**
 *
 * @author PU
 */
public class Smtp
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public Smtp()
  {
  }

  public String sendMail(
    String sToAdr, String sToRealName,
    String sSubject, String sText, Multipart mp)
  {
    try
    {
      Message message = new MimeMessage(getSession());
      message.setHeader("Content-Transfer-Encoding", "8bit");
      message.addHeader("Content-Type", "text/plain; charset=ISO-8859-1");
      message.saveChanges();
      //message.

      message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(sToAdr, sToRealName));

      String mailSender = DBSysParam.instance().getStringValue("mail_sender", null, null);
      String mailName = DBSysParam.instance().getStringValue("mail_name", null, null);

      message.addFrom(new InternetAddress[]
        {
          new InternetAddress(mailSender, mailName)
        });


      message.setSubject(MimeUtility.encodeText(sSubject, "ISO-8859-1", null));
      //message.setSubject(new String(sSubject.getBytes(), "ISO-8859-1"));

      if (mp != null)
      {
        MimeBodyPart htmlPart = new MimeBodyPart();
        //htmlPart.setContent(new String(sText.getBytes(), "ISO-8859-1"), "text/plain; charset=\"ISO-8859-1\"");
        //htmlPart.setContent(MimeUtility.encodeText(sText, "ISO-8859-1", null), "text/plain; charset=\"ISO-8859-1\"");
        htmlPart.setContent(sText, "text/plain; charset=\"ISO-8859-1\"");
        mp.addBodyPart(htmlPart);
        
        message.setContent(mp);
      }
      else
      {
        //message.setContent(new String(sText.getBytes(), "ISO-8859-1"), "text/plain; charset=\"ISO-8859-1\"");
        //message.setContent(MimeUtility.encodeText(sText, "ISO-8859-1", null), "text/plain; charset=\"ISO-8859-1\"");
        message.setContent(sText, "text/plain; charset=\"ISO-8859-1\"");
      }


      /* HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
       public boolean verify(String hostname, SSLSession session) {
       // Always return true indicating that the host name is an acceptable match
       // with the server's authentication scheme.
       return true;
       }
       }); */

      Transport.send(message);
      
      //message.setContent(sText, "text/plain; charset=\"ISO-8859-1\"");
      //Transport.send(message);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return e.getLocalizedMessage();
    }
    return "";
  }

  private Session getSession()
  {
    Authenticator authenticator = new Authenticator();

    //String mailSender = DBSysParam.instance().getStringValue("mail_sender", null, null);
    String mailHost = DBSysParam.instance().getStringValue("mail_host", null, null);
    //String mailName = DBSysParam.instance().getStringValue("mail_name", null, null);
    String mailPort = DBSysParam.instance().getStringValue("mail_port", null, null);
    if (mailPort == null || mailPort.length() == 0)
      mailPort = "25";

    logger.debug("mailHost: " + mailHost);
    logger.debug("mailPort: " + mailPort);
    
    String mailSSL = DBSysParam.instance().getStringValue("mail_ssl_enable", null, null);
    if(mailSSL == null || mailSSL.length() == 0)
      mailSSL = "false";
    
    logger.debug("mailSSL: " + mailSSL);
    
    String mailAuth = DBSysParam.instance().getStringValue("mail_auth", null, null);
    if(mailAuth == null || mailAuth.length() == 0)
      mailAuth = "true";

    Properties properties = new Properties();
    properties.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
    //properties.setProperty("mail.smtp.submitter", mailName);
    properties.setProperty("mail.smtp.auth", mailAuth);
    properties.setProperty("mail.smtp.ssl.enable", mailSSL);
    properties.setProperty("mail.smtp.host", mailHost); //"mail.example.com"
    properties.setProperty("mail.smtp.port", mailPort);
    //properties.setProperty("mail.smtp.ssl.trust", "*");


    return Session.getInstance(properties, authenticator);
  }

  private class Authenticator extends javax.mail.Authenticator
  {

    private PasswordAuthentication authentication;

    public Authenticator()
    {
      String username = DBSysParam.instance().getStringValue("mail_user", null, null);
      String password = DBSysParam.instance().getStringValue("mail_password", null, null);
      authentication = new PasswordAuthentication(username, password);
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
      return authentication;
    }
  }
//  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
//  
//  public static String sendMail(String[] sToAdr, String sSubject, String sText)
//  {
//
//    Properties props = new Properties();
//    String mailHost = DBSysParam.instance().getStringValue("mail_host", null, null);
//    String mailPort = DBSysParam.instance().getStringValue("mail_port", null, null);
//    String mailUser = DBSysParam.instance().getStringValue("mail_user", null, null);
//    String mailSender = DBSysParam.instance().getStringValue("mail_sender", null, null);
//    String password = DBSysParam.instance().getStringValue("mail_password", null, null);
//    String mailSSL = DBSysParam.instance().getStringValue("mail_ssl_enable", null, null);
//    
//    logger.debug("mailHost: " + mailHost);
//    logger.debug("mailPort: " + mailPort);
//    logger.debug("mailUser: " + mailUser);
//    logger.debug("mailSender: " + mailSender);
//    logger.debug("password: " + password);
//    logger.debug("mailSSL: " + mailSSL);
//
//    props.put("mail.stmp.user", mailUser);
//    props.put("mail.smtp.host", mailHost);
//    props.put("mail.smtp.port", mailPort);
//    props.put("mail.smtp.password", password);
//    props.put("mail.smtp.auth", true);
//    props.put("mail.smtp.ssl.trust", "*");
//    props.put("mail.smtp.ssl.checkserveridentity", "false");
//    
//    if (mailSSL.equals("true"))
//    {
//      //If you want you use TLS 
//      
//      props.put("mail.smtp.starttls.enable", "true");
//      
//      props.put("mail.smtp.socketFactory.port", mailPort);
//      props.put("mail.smtp.socketFactory.class",
//              "javax.net.ssl.SSLSocketFactory");
//    }
//    
//
//    Session session = Session.getDefaultInstance(props, new Authenticator()
//    {
//      @Override
//      protected PasswordAuthentication getPasswordAuthentication()
//      {
//        String username = DBSysParam.instance().getStringValue("mail_user", null, null);
//        String password = DBSysParam.instance().getStringValue("mail_password", null, null);
//        
//        return new PasswordAuthentication(username, password);
//      }
//    });
//
//    MimeMessage msg = new MimeMessage(session);
//    try
//    {
//      msg.setFrom(new InternetAddress(mailSender));
//      Address[] addr = new Address[sToAdr.length];
//      for (int i = 0; i < sToAdr.length; i++)
//      {
//
//        addr[i] = new InternetAddress(sToAdr[i]);
//      }
//      msg.setRecipients(MimeMessage.RecipientType.TO, addr);
//      msg.setSubject(sSubject, "ISO-8859-1");
//      msg.setText(sText, "ISO-8859-1");
//      Transport transport = session.getTransport("smtp");
//      transport.send(msg);
//      System.out.println("E-mail sent !");
//    }
//    catch (Exception exc)
//    {
//      LoggingOutput.outputException(exc, Smtp.class);
//      return exc.getMessage();
//    }
//    return "";
//  }
}
