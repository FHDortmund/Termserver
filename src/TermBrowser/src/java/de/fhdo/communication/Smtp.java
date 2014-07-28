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
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author PU
 */
public class Smtp {
    
    public static String sendMail(String[] sToAdr, String sSubject, String sText) {
     
     Properties props = new Properties();
     String mailHost = DBSysParam.instance().getStringValue("mail_host", null, null);
     String mailPort = DBSysParam.instance().getStringValue("mail_port", null, null);
     String mailAuth = DBSysParam.instance().getStringValue("mail_auth", null, null);
     String mailSender = DBSysParam.instance().getStringValue("mail_sender", null, null);
     String password = DBSysParam.instance().getStringValue("mail_password", null, null);
     String mailSSL = DBSysParam.instance().getStringValue("mail_ssl_enable", null, null);
      
     props.put("mail.smtp.host", mailHost);
     props.put("mail.stmp.user", mailSender);          
     
     if(mailSSL.equals("true")){
        //If you want you use TLS 
        props.put("mail.smtp.auth", mailAuth);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.socketFactory.port", mailPort);
        props.put("mail.smtp.socketFactory.class",
                               "javax.net.ssl.SSLSocketFactory");
     }
     props.put("mail.smtp.port", mailPort);
     
     Session session = Session.getDefaultInstance(props, new Authenticator() {
          @Override
               protected PasswordAuthentication getPasswordAuthentication() {
                  String username = DBSysParam.instance().getStringValue("mail_user", null, null);
                  String password = DBSysParam.instance().getStringValue("mail_password", null, null);
               return new PasswordAuthentication(username,password); 
               }
        });
     
      
      MimeMessage msg = new MimeMessage(session);
         try {
           msg.setFrom(new InternetAddress(mailSender));
           Address[] addr = new Address[sToAdr.length];
           for(int i=0;i<sToAdr.length;i++){
           
               addr[i] = new InternetAddress(sToAdr[i]);
           }
           msg.setRecipients(MimeMessage.RecipientType.TO, addr);
           msg.setSubject(sSubject,"ISO-8859-1");
           msg.setText(sText, "ISO-8859-1");
           Transport transport = session.getTransport("smtp");
           transport.send(msg);
           System.out.println("E-mail sent !");
            }   catch(Exception exc) {
                return exc.getMessage();
             }
         return "";
    }
}
