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
package de.fhdo.gui.main.modules;

import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.interfaces.IUpdateModal;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.web.servlet.http.Encodes;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 *
 * @author Philipp Urbauer
 */
public class OidEnquiry extends Window implements org.zkoss.zk.ui.ext.AfterCompose //public class Menu extends GenericAutowireComposer
{
  private IUpdateModal updateInterface;
  private Textbox  tb_symbName, tb_kaNumber, tb_commentG, tb_descriptionDe, tb_descriptionEn, tb_forname, tb_surename, tb_titlePre, tb_titlePost,
                   tb_address, tb_address2, tb_address3, tb_postalNr, tb_city, tb_country, tb_eMail, tb_phone, tb_fax, tb_url, tb_orgOid,
                   tb_orgName, tb_maintenanceNr, tb_comment;
  private Combobox cb_category;
  private Object obj;
  
  public OidEnquiry()
  {
      Map args = Executions.getCurrent().getArg();
      
        try
        {
          obj = args.get("version");
        }
        catch (Exception ex)
        {
        }
  }

  private String getELabel(String gLabel){
  
        String eLabel = "";
        if(gLabel.equals("Kodierschema")){
            eLabel = "codingscheme";
        }else if(gLabel.equals("Organization")){
            eLabel = "organization";
        }else if(gLabel.equals("Person")){
            eLabel = "person";
        }else if(gLabel.equals("Identifikationsschema")){
            eLabel = "identificationscheme";
        }else if(gLabel.equals("Dokument")){
            eLabel = "document";
        }else if(gLabel.equals("Richtlinie")){
            eLabel = "policy";
        }else if(gLabel.equals("Experimentell")){
            eLabel = "experimental";
        }else if(gLabel.equals("Extern")){
            eLabel = "external";    
        }else if(gLabel.equals("Alias")){
            eLabel = "alias";
        }else if(gLabel.equals("Vorlage")){
            eLabel = "template";
        }else if(gLabel.equals("Instanz Identifikator")){
            eLabel = "instance-identifier";
        }else if(gLabel.equals("Service")){ 
            eLabel = "service";
        }else if(gLabel.equals("Andere")){
            eLabel = "other";
        }else{
            eLabel = "other";
        }

        return eLabel;
  }
  
  public void onOid(){
  
        if(cb_category.getSelectedItem() == null || tb_symbName.getText().length() == 0 ||
           tb_descriptionDe.getText().length() == 0 || tb_descriptionEn.getText().length() == 0 ||
           tb_forname.getText().length() == 0 || tb_surename.getText().length() == 0 ||
           tb_address.getText().length() == 0 || tb_postalNr.getText().length() == 0 || 
           tb_city.getText().length() == 0 || tb_country.getText().length() == 0 || 
           tb_eMail.getText().length() == 0 || tb_phone.getText().length() == 0 || 
           tb_orgName.getText().length() == 0){     
            Messagebox.show("Sie haben nicht alle Pflichtfelder ausgefüllt!", "OID Antrag", Messagebox.OK, Messagebox.INFORMATION);
            return;
        }
        /**********************DEBUG*ERROR*INFO*ALL******************************************************/
        /**/ String aim = "https://signon.portal.at/OID_Frontend/application.htm";   //link für das TestSystem
        /************************************************************************************************/
        aim  += "?";
        aim += "symbolicName" + "=" + URLEncoder.encode(tb_symbName.getText()) + "&";
        aim += "oid_category" + "=" + URLEncoder.encode(getELabel(cb_category.getSelectedItem().getLabel())) + "&";
        aim += "kaNummer" + "=" + URLEncoder.encode(tb_kaNumber.getText()) + "&";
        aim += "comment" + "=" + URLEncoder.encode(tb_commentG.getText()) + "&";
        aim += "descriptionDe" + "=" + URLEncoder.encode(tb_descriptionDe.getText()) + "&";
        aim += "descriptionEn" + "=" + URLEncoder.encode(tb_descriptionEn.getText()) + "&";
        aim += "res_person_name_given" + "=" + URLEncoder.encode(tb_forname.getText()) + "&";
        aim += "res_person_name_family" + "=" + URLEncoder.encode(tb_surename.getText()) + "&";
        aim += "res_person_name_prefix" + "=" + URLEncoder.encode(tb_titlePre.getText()) + "&";
        aim += "res_person_name_suffix" + "=" + URLEncoder.encode(tb_titlePost.getText()) + "&";
        aim += "res_person_addr_streetAdressline1" + "=" + URLEncoder.encode(tb_address.getText()) + "&";
        aim += "res_person_addr_streetAdressline2" + "=" + URLEncoder.encode(tb_address2.getText()) + "&";
        aim += "res_person_addr_streetAdressline3" + "=" + URLEncoder.encode(tb_address3.getText()) + "&";
        aim += "res_person_addr_postalCode" + "=" + URLEncoder.encode(tb_postalNr.getText()) + "&";
        aim += "res_person_addr_city" + "=" + URLEncoder.encode(tb_city.getText()) + "&";
        aim += "res_person_addr_country" + "=" + URLEncoder.encode(tb_country.getText()) + "&";
        aim += "res_mailto" + "=" + URLEncoder.encode(tb_eMail.getText()) + "&";
        aim += "res_tel" + "=" + URLEncoder.encode(tb_phone.getText()) + "&";
        aim += "res_fax" + "=" + URLEncoder.encode(tb_fax.getText()) + "&";
        aim += "res_url" + "=" + URLEncoder.encode(tb_url.getText()) + "&";
        aim += "res_organization_oid" + "=" + URLEncoder.encode(tb_orgOid.getText()) + "&";
        aim += "res_organization_name" + "=" + URLEncoder.encode(tb_orgName.getText()) + "&";
        aim += "res_vkz" + "=" + URLEncoder.encode(tb_maintenanceNr.getText()) + "&";
        aim += "res_organization_comment" + "=" + URLEncoder.encode(tb_comment.getText()) + "&";
        aim += "submitOidClaim" + "=true" + "&";
        if(obj instanceof CodeSystemVersion){
          aim += "versionType" + "=CodeSystemVersion" + "&";
          aim += "versionId" + "=" + ((CodeSystemVersion)obj).getVersionId();
        }else{
          aim += "versionType" + "=ValueSetVersion" + "&";
          aim += "versionId" + "=" + ((ValueSetVersion)obj).getVersionId();
        }
        
        //Executions.getCurrent().sendRedirect(aim);
      BufferedReader rd = null;
      HttpURLConnection conn;
      String line;
      String result = "";
      try {

          URL url = new URL(aim);
          conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         while ((line = rd.readLine()) != null) {
            result += line;
         }
         rd.close();
      } catch (MalformedURLException ex) {
          Logger.getLogger(OidEnquiry.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
          Logger.getLogger(OidEnquiry.class.getName()).log(Level.SEVERE, null, ex);
      }
        
      if(result.equals("OK")){
      
          Messagebox.show("Der Antrag wurde erfolgreich übermittelt! \nBei Vergabe wird die OID automatisch im System eingetragen.", "Information", Messagebox.OK, Messagebox.INFORMATION);
      }else{
      
          Messagebox.show("Der Antrag konnte nicht übermittelt werden. \nBitte versuchen sie es alternativ über das OID Portal!", "Fehler", Messagebox.OK, Messagebox.INFORMATION);
      }
    
      if(updateInterface != null)
          updateInterface.update(null, false);
      this.setVisible(false);
      this.detach();
  }
  
  
  public void afterCompose()
  {
    tb_symbName = (Textbox)getFellow("tb_symbName");
    cb_category = (Combobox)getFellow("cb_category");
    tb_kaNumber = (Textbox)getFellow("tb_kaNumber");
    tb_commentG = (Textbox)getFellow("tb_commentG");
    tb_descriptionDe = (Textbox)getFellow("tb_descriptionDe");
    tb_descriptionEn = (Textbox)getFellow("tb_descriptionEn");
    tb_forname = (Textbox)getFellow("tb_forname");
    tb_surename = (Textbox)getFellow("tb_surename");
    tb_titlePre = (Textbox)getFellow("tb_titlePre");
    tb_titlePost = (Textbox)getFellow("tb_titlePost");
    tb_address = (Textbox)getFellow("tb_address");
    tb_address2 = (Textbox)getFellow("tb_address2");
    tb_address3 = (Textbox)getFellow("tb_address3");
    tb_postalNr = (Textbox)getFellow("tb_postalNr");
    tb_city = (Textbox)getFellow("tb_city");
    tb_country = (Textbox)getFellow("tb_country");
    tb_eMail = (Textbox)getFellow("tb_eMail");
    tb_phone = (Textbox)getFellow("tb_phone");
    tb_fax = (Textbox)getFellow("tb_fax");
    tb_url = (Textbox)getFellow("tb_url");
    tb_orgOid = (Textbox)getFellow("tb_orgOid");
    tb_orgName = (Textbox)getFellow("tb_orgName");
    tb_maintenanceNr = (Textbox)getFellow("tb_maintenanceNr");
    tb_comment = (Textbox)getFellow("tb_comment");
    
    cb_category.appendItem("Kodierschema");
    cb_category.appendItem("Organization");
    cb_category.appendItem("Person");
    cb_category.appendItem("Identifikationsschema");
    cb_category.appendItem("Dokument");
    cb_category.appendItem("Richtlinie");
    cb_category.appendItem("Experimentell");
    cb_category.appendItem("Extern");
    cb_category.appendItem("Alias");
    cb_category.appendItem("Vorlage");
    cb_category.appendItem("Instanz Identifikator");
    cb_category.appendItem("Service");
    cb_category.appendItem("Andere");
    cb_category.setSelectedIndex(0);
  }
  
  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdateModal updateInterface)
  {
    this.updateInterface = updateInterface;
  }
}
