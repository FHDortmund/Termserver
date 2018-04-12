/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class SVSHandler implements SOAPHandler<SOAPMessageContext>
{

  private void fixPrefix(Iterator Elements)
  {

    while (Elements.hasNext())
    {
      Object o2 = Elements.next();
      //System.out.println("Object: " + o2.getClass().getCanonicalName());

      if (o2 instanceof com.sun.xml.messaging.saaj.soap.impl.ElementImpl)
      {
        com.sun.xml.messaging.saaj.soap.impl.ElementImpl elem2 = (com.sun.xml.messaging.saaj.soap.impl.ElementImpl) o2;
        //System.out.println("elem2: " + elem2.getTagName());

        elem2.setPrefix("");
        elem2.removeNamespaceDeclaration("ns2");
        
        fixPrefix(elem2.getChildElements());
      }
    }
  }

  public boolean handleMessage(SOAPMessageContext messageContext)
  {
    SOAPMessage msg = messageContext.getMessage();

    System.out.println("handleMessage");

    try
    {
      //msg.getSOAPBody().setPrefix("");
      Iterator it = msg.getSOAPBody().getChildElements();

      while (it.hasNext())
      {
        Object o = it.next();
        System.out.println("Object: " + o.getClass().getCanonicalName());
        if (o instanceof com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl)
        {
          String name = ((com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl) o).getTagName();
          System.out.println("Name: " + name);

          if (name.contains("RetrieveValueSetResponse"))
          {
            com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl elem = (com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl) o;
            elem.setPrefix("");
            if (elem.getNamespacePrefixes().hasNext())
              System.out.println("ns: " + elem.getNamespacePrefixes().next());
            elem.removeNamespaceDeclaration("ns2");

            fixPrefix(elem.getChildElements());
            /*Iterator it2 = elem.getChildElements();
             while (it2.hasNext())
             {
             Object o2 = it2.next();
             System.out.println("Object: " + o2.getClass().getCanonicalName());

             if (o2 instanceof com.sun.xml.messaging.saaj.soap.impl.ElementImpl)
             {
             com.sun.xml.messaging.saaj.soap.impl.ElementImpl elem2 = (com.sun.xml.messaging.saaj.soap.impl.ElementImpl) o2;
             System.out.println("elem2: " + elem2.getTagName());

             elem2.setPrefix("");
             elem2.removeNamespaceDeclaration("ns2");
             }
             }*/
          }

          //((com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl)o).getNamespaceContextNodes().
          //break;
          //((com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl)o).s
          //((com.sun.xml.messaging.saaj.soap.ver1_1.BodyElement1_1Impl)o).set
        }
      }

//      if (msg.getSOAPBody().getNamespacePrefixes().hasNext())
//        System.out.println("O: " + msg.getSOAPBody().getNamespacePrefixes().next());
//      if (msg.getSOAPBody().getNamespacePrefixes().hasNext())
//        System.out.println("O: " + msg.getSOAPBody().getNamespacePrefixes().next());
//      if (msg.getSOAPBody().getNamespacePrefixes().hasNext())
//        System.out.println("O: " + msg.getSOAPBody().getNamespacePrefixes().next());
//      if (msg.getSOAPBody().getNamespacePrefixes().hasNext())
//        System.out.println("O: " + msg.getSOAPBody().getNamespacePrefixes().next());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return true;
  }

  public Set<QName> getHeaders()
  {
    return Collections.EMPTY_SET;
  }

  public boolean handleFault(SOAPMessageContext messageContext)
  {
    return true;
  }

  public void close(MessageContext context)
  {
    System.out.println("close()");

  }

}
