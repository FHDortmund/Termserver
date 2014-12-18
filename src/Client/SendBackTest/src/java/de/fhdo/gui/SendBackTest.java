package de.fhdo.gui;

import org.apache.tomcat.util.codec.binary.Base64;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */
public class SendBackTest extends Window
{
  private Window wBrowser;

  public SendBackTest()
  {

  }

  public void openCodesystem()
  {
    System.out.println("openCodesystem()");

    wBrowser = new Window();
    openBrowser("Diagnose auswählen", "ICD", "onSendBack", wBrowser, this);
  }

  public static void openBrowser(String title, String codeSystemName, String sendbackMethodName, Window wBrowser, Window parent)
  {
    String uri = "http://localhost:8080/TermBrowser/gui/main/main.zul?loadType=CodeSystem&loadName=ICD&hideMenu=1&hideStatusbar=1&hideSelection=1&sendBack=true";

    // create window
    wBrowser.setHeight("90%");
    wBrowser.setWidth("90%");
    wBrowser.setClosable(true);
    wBrowser.setTitle(title);
    wBrowser.setBorder("normal");

    Iframe iFrame = new Iframe();
    iFrame.setHeight("100%");
    iFrame.setWidth("100%");
    iFrame.setSrc(uri);
    iFrame.setParent(wBrowser);
    wBrowser.appendChild(iFrame);
    parent.appendChild(wBrowser);

    try
    {
      wBrowser.doModal();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * This method is called from the Terminology Browser when a concept is selected
   * and send back. The result is in the data string, the values are comma separated.
   * Term and description are base64 encoded.
   * 
   * @param e 
   */
  public void onSendBack(Event e)
  {
    System.out.println("onSendBack()");
    try
    {
      Object str = e.getData();
      System.out.println("str: " + str);
      
      String s_array[] = str.toString().split(";");
      for(String s_part : s_array)
      {
        String s[] = s_part.split("=");
        if(s != null && s.length == 2)
        {
          String s_result = "";
          
          if(s[0].equalsIgnoreCase("term") || s[0].equalsIgnoreCase("description"))
            s_result = new String(Base64.decodeBase64(s[1]));
          else s_result = s[1];
          
          System.out.println(s[0] + ": " + s_result);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    wBrowser.detach();
  }
}
