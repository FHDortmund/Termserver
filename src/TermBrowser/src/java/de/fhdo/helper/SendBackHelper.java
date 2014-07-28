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

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Becker
 */
public class SendBackHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean active;  
  
  public static Integer SENDBACK_NAME = 1,
                        SENDBACK_DESCRIPTION = 2,
                        SENDBACK_CODE = 4,
                        SENDBACK_NAME_DESCRIPTION = 3,
                        SENDBACK_NAME_CODE = 5,
                        SENDBACK_DESCRIPTION_CODE = 6,
                        SENDBACK_NAME_DESCRIPTION_CODE = 7;

  public SendBackHelper(){
      initialize();
  }
  
  
  /** 
   * Check if sendback should be active or not.
   * Required Parameters:
   * - sbContent + length ==6  (sbContent=123456)
   * - at least one type > 0
   */
  public void initialize()
  {
    logger.debug("SendBackHelper - Initialize()");
    active = false;        
    String qureyString = Executions.getCurrent().getDesktop().getQueryString();
    if(qureyString == null || qureyString.indexOf("sbContent") == -1)
        return;
    
    int    startIndex = qureyString.indexOf("sbContent") + "sbContent".length() + 1;    
    String sbContent  = qureyString.substring(startIndex, startIndex + 6);
    
    logger.debug("SendBackHelper - Initialize() - sbContent == " + sbContent);
    String sendBackApplicationName  = ParameterHelper.getString("sbAppName");

    // Pruefe ob Sendback genutzt werden soll
    if (sbContent != null && sbContent.length() == 6){
        try{            
            SessionHelper.setValue("typeDV",   Integer.valueOf(sbContent.substring(0, 1)));
            SessionHelper.setValue("typeCS",   Integer.valueOf(sbContent.substring(1, 2)));
            SessionHelper.setValue("typeCSV",  Integer.valueOf(sbContent.substring(2, 3)));
            SessionHelper.setValue("typeVS",   Integer.valueOf(sbContent.substring(3, 4)));
            SessionHelper.setValue("typeVSV",  Integer.valueOf(sbContent.substring(4, 5)));
            SessionHelper.setValue("typeCSEV", Integer.valueOf(sbContent.substring(5, 6)));
            SessionHelper.setValue("sendBackApplicationName", sendBackApplicationName);       
            if((Integer)SessionHelper.getValue("typeDV")  > 0 || (Integer)SessionHelper.getValue("typeCS")   > 0 || 
               (Integer)SessionHelper.getValue("typeCSV") > 0 || (Integer)SessionHelper.getValue("typeVS")   > 0 ||
               (Integer)SessionHelper.getValue("typeVSV") > 0 || (Integer)SessionHelper.getValue("typeCSEV") > 0){                
                active = true;
            }
            else {
                active = false;
            }
        } catch (Exception e){
            e.printStackTrace();
            active = false;            
        }
    }
  }  
  
  public void sendBack(String text){
        logger.debug("sendBack-postMethod:");
        String javaScript = "window.top.postMessage('"+ text + "', '\\*')"; // Aus sicherheitsgruenden sollte * ersetzt werden durch die domain des TS. Auf der empf�ngerseite kann dann        
        logger.debug(javaScript);
        Clients.evalJavaScript(javaScript);            
    }

  public String getSendBackApplicationName()
  {
    String s = (String) SessionHelper.getValue("sendBackApplicationName");
 
    if (s != null)
      return s;
    return "";
  }

  public String getSendBackMethodName()
  {
    String s = (String) SessionHelper.getValue("sendBackMethodName");
    if (s != null)
      return s;
    return "";
  }

  public Integer getSendBackTypeDV()
  {
    if ((Integer) SessionHelper.getValue("typeDV") != null)
      return (Integer) SessionHelper.getValue("typeDV");
    return -1;
  }

  public Integer getSendBackTypeCS()
  {
    if ((Integer) SessionHelper.getValue("typeCS") != null)
      return (Integer) SessionHelper.getValue("typeCS");
    return -1;
  }

  public Integer getSendBackTypeCSV()
  {
    if ((Integer) SessionHelper.getValue("typeCSV") != null)
      return (Integer) SessionHelper.getValue("typeCSV");
    return -1;
  }

  public Integer getSendBackTypeVS()
  {
    if ((Integer) SessionHelper.getValue("typeVS") != null)
      return (Integer) SessionHelper.getValue("typeVS");
    return -1;
  }

  public Integer getSendBackTypeVSV()
  {
    if ((Integer) SessionHelper.getValue("typeVSV") != null)
      return (Integer) SessionHelper.getValue("typeVSV");
    return -1;
  }

  public Integer getSendBackTypeCSEV()
  {
    if ((Integer) SessionHelper.getValue("typeCSEV") != null)
      return (Integer) SessionHelper.getValue("typeCSEV");
    return -1;
  }

  public String getSendBackTypeByInteger(Integer value)
  {
    String r = "No return type";

    if (value == SENDBACK_NAME)
      r = Labels.getLabel("common.name");
    if (value == SENDBACK_DESCRIPTION)
      r = Labels.getLabel("common.description");
    if (value == SENDBACK_CODE)
      r = Labels.getLabel("common.code");

    if (value == SENDBACK_NAME_DESCRIPTION)
      r = Labels.getLabel("common.name") + " & " + Labels.getLabel("common.description");
    if (value == SENDBACK_NAME_CODE)
      r = Labels.getLabel("common.name") + " & " + Labels.getLabel("common.code");
    if (value == SENDBACK_DESCRIPTION_CODE)
      r = Labels.getLabel("common.description") + " & " + Labels.getLabel("common.code");
    if (value == SENDBACK_NAME_DESCRIPTION_CODE)
      r = Labels.getLabel("common.name") + " & " + Labels.getLabel("common.description") + " & " + Labels.getLabel("common.code");

    return r;
  }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean aActive) {
        active = aActive;
    }
    
    //  public static void callJavaScript(Object data){
//        ScriptEngineManager manager = new ScriptEngineManager();  
//        ScriptEngine engine = manager.getEngineByName("JavaScript");  
//  
//        // JavaScript code in a String  
////        String script = "function hello(name) { print('Hello, ' + name); }";  
//        String script = "top.postMessage('"+ "test" + "', '*')";
//      try {
//          Bindings bindings = new SimpleBindings();
//          bindings.put("csev", data);          
//          
//          // evaluate script with bindings
//          engine.eval(script, bindings);            
//    
//  //        // javax.script.Invocable is an optional interface.  
//  //        // Check whether your script engine implements or not!  
//  //        // Note that the JavaScript engine implements Invocable interface.  
//  //        Invocable inv = (Invocable) engine;  
//  //  
//  //        inv.invokeFunction("hello", "Scripting!!" );  
//  //        inv.invokeFunction("hello", "Scripting!!" );
//      } catch (ScriptException ex) {
//          Logger.getLogger(SendBackHelper.class.getName()).log(Level.SEVERE, null, ex);
//      }
//  }

    public static SendBackHelper getInstance() {        
//        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//        logger.debug("SendBackHelper - session == " + session.toString());
        Execution exe = Executions.getCurrent();                   
//        logger.debug("SendBackHelper - d.getExe() Id == " + exe.getDesktop().getId());
//        logger.debug("SendBackHelper - Execution.getD Id == " + Executions.getCurrent().getDesktop().getId());

        SendBackHelper sbHelper = (SendBackHelper) exe.getAttribute("SendBackHelper");
        if(sbHelper == null){              
            Executions.getCurrent().getDesktop().getQueryString();
            
            sbHelper = new SendBackHelper();
            exe.setAttribute("SendBackHelper", sbHelper);
        }
        
        return sbHelper;
    }
}